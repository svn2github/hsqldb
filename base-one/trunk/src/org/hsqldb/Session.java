/* Copyright (c) 1995-2000, The Hypersonic SQL Group.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the Hypersonic SQL Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE HYPERSONIC SQL GROUP,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software consists of voluntary contributions made by many individuals 
 * on behalf of the Hypersonic SQL Group.
 *
 *
 * For work added by the HSQL Development Group:
 *
 * Copyright (c) 2001-2005, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the HSQL Development Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL HSQL DEVELOPMENT GROUP, HSQLDB.ORG,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package org.hsqldb;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import org.hsqldb.HsqlNameManager.HsqlName;
import org.hsqldb.jdbc.jdbcConnection;
import org.hsqldb.lib.ArrayUtil;
import org.hsqldb.lib.HashMappedList;
import org.hsqldb.lib.IntKeyHashMap;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.store.ValuePool;

// fredt@users 20020320 - doc 1.7.0 - update
// fredt@users 20020315 - patch 1.7.0 - switch for scripting
// fredt@users 20020130 - patch 476694 by velichko - transaction savepoints
// additions in different parts to support savepoint transactions
// fredt@users 20020910 - patch 1.7.1 by fredt - database readonly enforcement
// fredt@users 20020912 - patch 1.7.1 by fredt - permanent internal connection
// boucherb@users 20030512 - patch 1.7.2 - compiled statements
//                                       - session becomes execution hub
// boucherb@users 20050510 - patch 1.7.2 - generalized Result packet passing
//                                         based command execution
//                                       - batch execution handling
// fredt@users 20030628 - patch 1.7.2 - session proxy support
// fredt@users 20040509 - patch 1.7.2 - SQL conformance for CURRENT_TIMESTAMP and other datetime functions

/**
 *  Implementation of a user session with the database. In 1.7.2 Session
 *  becomes the public interface to an HSQLDB database, accessed locally or
 *  remotely via SessionInterface.
 *
 *  When as Session is closed, all references to internal engine objects are
 *  set to null. But the session id and scripting mode may still be used for
 *  scripting
 *
 * @version  1.7.2
 */
public class Session implements SessionInterface {

    //
    private volatile boolean isAutoCommit;
    private volatile boolean isReadOnly;
    private volatile boolean isClosed;
    int                      isolation;

    //
    Database               database;
    private User           user;
    HsqlArrayList          transactionList;
    private boolean        isNestedTransaction;
    private int            nestedOldTransIndex;
    private int            currentMaxRows;
    private int            sessionMaxRows;
    private Number         lastIdentity = ValuePool.getInt(0);
    private final int      sessionId;
    HashMappedList         savepoints;
    private boolean        script;
    private jdbcConnection intConnection;
    private Tokenizer      tokenizer;
    private Parser         parser;
    private long           sessionSCN;
    static final Result emptyUpdateCount =
        new Result(ResultConstants.UPDATECOUNT);

    // schema
    public HsqlName  currentSchema;
    public HsqlName  loggedSchema;
    private HsqlName oldSchema;

    // query processing
    boolean isProcessingScript;
    boolean isProcessingLog;

    // temp tables
    private IntKeyHashMap indexArrayMap;
    private IntKeyHashMap indexArrayKeepMap;

    /** @todo fredt - clarify in which circumstances Session has to disconnect */
    Session getSession() {
        return this;
    }

    /**
     * Constructs a new Session object.
     *
     * @param  db the database to which this represents a connection
     * @param  user the initial user
     * @param  autocommit the initial autocommit value
     * @param  readonly the initial readonly value
     * @param  id the session identifier, as known to the database
     */
    Session(Database db, User user, boolean autocommit, boolean readonly,
            int id) {

        sessionId                 = id;
        database                  = db;
        this.user                 = user;
        transactionList           = new HsqlArrayList();
        savepoints                = new HashMappedList(4);
        isAutoCommit              = autocommit;
        isReadOnly                = readonly;
        dbCommandInterpreter      = new DatabaseCommandInterpreter(this);
        compiledStatementExecutor = new CompiledStatementExecutor(this);
        compiledStatementManager  = db.compiledStatementManager;
        tokenizer                 = new Tokenizer();
        parser                    = new Parser(this, database, tokenizer);
        currentSchema = database.schemaManager.getDefaultSchemaHsqlName();
    }

    /**
     *  Retrieves the session identifier for this Session.
     *
     * @return the session identifier for this Session
     */
    public int getId() {
        return sessionId;
    }

    /**
     * Closes this Session.
     */
    public void close() {

        if (isClosed) {
            return;
        }

        synchronized (database) {

            // test again inside block
            if (isClosed) {
                return;
            }

            try {
                database.logger.writeToLog(this, Token.T_DISCONNECT);
            } catch (HsqlException e) {}

            database.sessionManager.removeSession(this);
            rollback();
            clearIndexRoots();
            clearIndexRootsKeep();
            compiledStatementManager.removeSession(sessionId);

            database                  = null;
            user                      = null;
            transactionList           = null;
            savepoints                = null;
            intConnection             = null;
            compiledStatementExecutor = null;
            compiledStatementManager  = null;
            dbCommandInterpreter      = null;
            lastIdentity              = null;
            isClosed                  = true;
        }
    }

    /**
     * Retrieves whether this Session is closed.
     *
     * @return true if this Session is closed
     */
    public boolean isClosed() {
        return isClosed;
    }

    public void setIsolation(int level) throws HsqlException {
        isolation = level;
    }

    public int getIsolation() throws HsqlException {
        return isolation;
    }

    /**
     * Setter for iLastIdentity attribute.
     *
     * @param  i the new value
     */
    void setLastIdentity(Number i) {
        lastIdentity = i;
    }

    /**
     * Getter for iLastIdentity attribute.
     *
     * @return the current value
     */
    Number getLastIdentity() {
        return lastIdentity;
    }

    /**
     * Retrieves the Database instance to which this
     * Session represents a connection.
     *
     * @return the Database object to which this Session is connected
     */
    Database getDatabase() {
        return database;
    }

    /**
     * Retrieves the name, as known to the database, of the
     * user currently controlling this Session.
     *
     * @return the name of the user currently connected within this Session
     */
    String getUsername() {
        return user.getName();
    }

    /**
     * Retrieves the User object representing the user currently controlling
     * this Session.
     *
     * @return this Session's User object
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets this Session's User object to the one specified by the
     * user argument.
     *
     * @param  user the new User object for this session
     */
    void setUser(User user) {
        this.user = user;
    }

    int getMaxRows() {
        return currentMaxRows;
    }

    int getSQLMaxRows() {
        return sessionMaxRows;
    }

    /**
     * The SQL command SET MAXROWS n will override the Statement.setMaxRows(n)
     * until SET MAXROWS 0 is issued.
     *
     * NB this is dedicated to the SET MAXROWS sql statement and should not
     * otherwise be called. (fredt@users)
     */
    void setSQLMaxRows(int rows) {
        currentMaxRows = sessionMaxRows = rows;
    }

    /**
     * Checks whether this Session's current User has the privileges of
     * the ADMIN role.
     *
     * @throws HsqlException if this Session's User does not have the
     *      privileges of the ADMIN role.
     */
    void checkAdmin() throws HsqlException {
        user.checkAdmin();
    }

    /**
     * Checks whether this Session's current User has the set of rights
     * specified by the right argument, in relation to the database
     * object identifier specified by the object argument.
     *
     * @param  object the database object to check
     * @param  right the rights to check for
     * @throws  HsqlException if the Session User does not have such rights
     */
    void check(Object object, int right) throws HsqlException {
        user.check(object, right);
    }

    /**
     * This is used for reading - writing to existing tables.
     * @throws  HsqlException
     */
    void checkReadWrite() throws HsqlException {

        if (isReadOnly) {
            throw Trace.error(Trace.DATABASE_IS_READONLY);
        }
    }

    /**
     * This is used for creating new database objects such as tables.
     * @throws  HsqlException
     */
    void checkDDLWrite() throws HsqlException {

        if (database.isFilesReadOnly() &&!user.isSys()) {
            throw Trace.error(Trace.DATABASE_IS_READONLY);
        }
    }

    /**
     *  Adds a single-row deletion step to the transaction UNDO buffer.
     *
     * @param  table the table from which the row was deleted
     * @param  row the deleted row
     * @throws  HsqlException
     */
    boolean addTransactionDelete(Table table, Row row) throws HsqlException {

        if (!isAutoCommit || isNestedTransaction) {
            Transaction t = new Transaction(true, table, row, sessionSCN);

            transactionList.add(t);
            database.txManager.addTransaction(this, t);

            return true;
        }

        return false;
    }

    /**
     *  Adds a single-row inssertion step to the transaction UNDO buffer.
     *
     * @param  table the table into which the row was inserted
     * @param  row the inserted row
     * @throws  HsqlException
     */
    boolean addTransactionInsert(Table table, Row row) throws HsqlException {

        if (!isAutoCommit || isNestedTransaction) {
            Transaction t = new Transaction(false, table, row, sessionSCN);

            transactionList.add(t);
            database.txManager.addTransaction(this, t);

            return true;
        }

        return false;
    }

    /**
     *  Setter for the autocommit attribute.
     *
     * @param  autocommit the new value
     * @throws  HsqlException
     */
    public void setAutoCommit(boolean autocommit) {

        if (isClosed) {
            return;
        }

        synchronized (database) {
            if (autocommit != isAutoCommit) {
                commit();

                isAutoCommit = autocommit;

                try {
                    database.logger.writeToLog(this,
                                               getAutoCommitStatement());
                } catch (HsqlException e) {}
            }
        }
    }

    /**
     * Commits any uncommited transaction this Session may have open
     *
     * @throws  HsqlException
     */
    public void commit() {

        if (isClosed) {
            return;
        }

        synchronized (database) {
            if (!transactionList.isEmpty()) {
                try {
                    database.logger.writeCommitStatement(this);
                } catch (HsqlException e) {}
            }

            database.txManager.commit(this);
            clearIndexRoots();
        }
    }

    /**
     * Rolls back any uncommited transaction this Session may have open.
     *
     * @throws  HsqlException
     */
    public void rollback() {

        if (isClosed) {
            return;
        }

        synchronized (database) {
            if (transactionList.size() != 0) {
                try {
                    database.logger.writeToLog(this, Token.T_ROLLBACK);
                } catch (HsqlException e) {}
            }

            database.txManager.rollback(this);
            clearIndexRoots();
        }
    }

    /**
     * No-op in this implementation
     */
    public void resetSession() throws HsqlException {
        throw new HsqlException("", "", 0);
    }

    /**
     *  Implements a transaction SAVEPOINT. A new SAVEPOINT with the
     *  name of an existing one replaces the old SAVEPOINT.
     *
     * @param  name of the savepoint
     * @throws  HsqlException if there is no current transaction
     */
    void savepoint(String name) throws HsqlException {

        savepoints.remove(name);
        savepoints.add(name, ValuePool.getInt(transactionList.size()));

        try {
            database.logger.writeToLog(this, Token.T_SAVEPOINT + " " + name);
        } catch (HsqlException e) {}
    }

    /**
     *  Implements a partial transaction ROLLBACK.
     *
     * @param  name Name of savepoint that was marked before by savepoint()
     *      call
     * @throws  HsqlException
     */
    void rollbackToSavepoint(String name) throws HsqlException {

        if (isClosed) {
            return;
        }

        try {
            database.logger.writeToLog(this,
                                       Token.T_ROLLBACK + " " + Token.T_TO
                                       + " " + Token.T_SAVEPOINT + " "
                                       + name);
        } catch (HsqlException e) {}

        database.txManager.rollbackSavepoint(this, name);
    }

    /**
     * Implements release of named SAVEPOINT.
     *
     * @param  name Name of savepoint that was marked before by savepoint()
     *      call
     * @throws  HsqlException if name does not correspond to a savepoint
     */
    void releaseSavepoint(String name) throws HsqlException {

        // remove this and all later savepoints
        int index = savepoints.getIndex(name);

        Trace.check(index >= 0, Trace.SAVEPOINT_NOT_FOUND, name);

        while (savepoints.size() > index) {
            savepoints.remove(savepoints.size() - 1);
        }
    }

    /**
     * Starts a nested transaction.
     *
     * @throws  HsqlException
     */
    void beginNestedTransaction() throws HsqlException {

        if (isNestedTransaction) {
            Trace.doAssert(false, "beginNestedTransaction");
        }

        nestedOldTransIndex = transactionList.size();
        isNestedTransaction = true;
    }

    /**
     * Ends a nested transaction.
     *
     * @param  rollback true to roll back or false to commit the nested transaction
     * @throws  HsqlException
     */
    void endNestedTransaction(boolean rollback) throws HsqlException {

        if (!isNestedTransaction) {
            Trace.doAssert(false, "endNestedTransaction");
        }

        if (rollback) {
            database.txManager.rollbackTransactions(this,
                    nestedOldTransIndex, true);
        }

        // reset after the rollback
        isNestedTransaction = false;

        if (isAutoCommit == true) {
            transactionList.clear();
        }
    }

    /**
     * Setter for readonly attribute.
     *
     * @param  readonly the new value
     */
    public void setReadOnly(boolean readonly) throws HsqlException {

        if (!readonly && database.databaseReadOnly) {
            throw Trace.error(Trace.DATABASE_IS_READONLY);
        }

        isReadOnly = readonly;
    }

    /**
     *  Getter for readonly attribute.
     *
     * @return the current value
     */
    public boolean isReadOnly() {
        return isReadOnly;
    }

    /**
     *  Getter for nestedTransaction attribute.
     *
     * @return the current value
     */
    boolean isNestedTransaction() {
        return isNestedTransaction;
    }

    /**
     *  Getter for autoCommit attribute.
     *
     * @return the current value
     */
    public boolean isAutoCommit() {
        return isAutoCommit;
    }

    /**
     *  A switch to set scripting on the basis of type of statement executed.
     *  A method in DatabaseCommandInterpreter.java sets this value to false
     *  before other  methods are called to act on an SQL statement, which may
     *  set this to true. Afterwards the method reponsible for logging uses
     *  getScripting() to determine if logging is required for the executed
     *  statement. (fredt@users)
     *
     * @param  script The new scripting value
     */
    void setScripting(boolean script) {
        this.script = script;
    }

    /**
     * Getter for scripting attribute.
     *
     * @return  scripting for the last statement.
     */
    boolean getScripting() {
        return script;
    }

    public String getAutoCommitStatement() {
        return isAutoCommit ? "SET AUTOCOMMIT TRUE"
                            : "SET AUTOCOMMIT FALSE";
    }

    /**
     * Retrieves an internal Connection object equivalent to the one
     * that created this Session.
     *
     * @return  internal connection.
     */
    jdbcConnection getInternalConnection() throws HsqlException {

        if (intConnection == null) {
            intConnection = new jdbcConnection(this);
        }

        return intConnection;
    }

// boucherb@users 20020810 metadata 1.7.2
//----------------------------------------------------------------
    private final long connectTime = System.currentTimeMillis();

// more effecient for MetaData concerns than checkAdmin

    /**
     * Getter for admin attribute.
     *
     * @ return the current value
     */
    boolean isAdmin() {
        return user.isAdmin();
    }

    /**
     * Getter for connectTime attribute.
     *
     * @return the value
     */
    long getConnectTime() {
        return connectTime;
    }

    /**
     * Getter for transactionSise attribute.
     *
     * @return the current value
     */
    int getTransactionSize() {
        return transactionList.size();
    }

    /**
     * Retrieves whether the database object identifier by the dbobject
     * argument is accessible by the current Session User.
     *
     * @return true if so, else false
     */
    boolean isAccessible(Object dbobject) throws HsqlException {
        return user.isAccessible(dbobject);
    }

// boucherb@users 20030417 - patch 1.7.2 - compiled statement support
//-------------------------------------------------------------------
    DatabaseCommandInterpreter dbCommandInterpreter;
    CompiledStatementExecutor  compiledStatementExecutor;
    CompiledStatementManager   compiledStatementManager;

    private CompiledStatement sqlCompileStatement(String sql)
    throws HsqlException {

        parser.reset(sql);

        CompiledStatement cs;
        int               brackets = 0;
        String            token    = tokenizer.getString();
        int               cmd      = Token.get(token);

        switch (cmd) {

            case Token.OPENBRACKET : {
                brackets = Parser.parseOpenBrackets(tokenizer) + 1;

                tokenizer.getThis(Token.T_SELECT);
            }
            case Token.SELECT : {
                cs = parser.compileSelectStatement(brackets);

                break;
            }
            case Token.INSERT : {
                cs = parser.compileInsertStatement();

                break;
            }
            case Token.UPDATE : {
                cs = parser.compileUpdateStatement();

                break;
            }
            case Token.DELETE : {
                cs = parser.compileDeleteStatement();

                break;
            }
            case Token.CALL : {
                cs = parser.compileCallStatement();

                break;
            }
            default : {

                // DDL statements
                cs = new CompiledStatement(currentSchema);

                break;
            }
        }

        // In addition to requiring that the compilation was successful,
        // we also require that the submitted sql represents a _single_
        // valid DML or DDL statement. We do not check the DDL yet.
        // fredt - now accepts semicolon and whitespace at the end of statement
        // fredt - investigate if it should or not for prepared statements
        if (cs.type != CompiledStatement.DDL) {
            while (tokenizer.getPosition() < tokenizer.getLength()) {
                token = tokenizer.getString();

                if (token.length() != 0 &&!token.equals(Token.T_SEMICOLON)) {
                    throw Trace.error(Trace.UNEXPECTED_TOKEN, token);
                }
            }
        }

        // - need to be able to key cs against its sql in statement pool
        // - also need to be able to revalidate its sql occasionally
        cs.sql = sql;

        return cs;
    }

    /**
     * Executes the command encapsulated by the cmd argument.
     *
     * @param cmd the command to execute
     * @return the result of executing the command
     */
    public Result execute(Result cmd) {

        try {
            Trace.check(!isClosed, Trace.ACCESS_IS_DENIED,
                        Trace.getMessage(Trace.Session_execute));
        } catch (Throwable t) {
            return new Result(t, null);
        }

        synchronized (database) {
            int type = cmd.mode;

            if (sessionMaxRows == 0) {
                currentMaxRows = cmd.updateCount;
            }

            // we simply get the next system change number - no matter what type of query
            sessionSCN = database.nextDMLSCN();

            DatabaseManager.gc();

            switch (type) {

                case ResultConstants.SQLEXECUTE : {
                    Result resultout = sqlExecute(cmd);

                    resultout = performPostExecute(resultout);

                    return resultout;
                }
                case ResultConstants.BATCHEXECUTE : {
                    Result resultout = sqlExecuteBatch(cmd);

                    resultout = performPostExecute(resultout);

                    return resultout;
                }
                case ResultConstants.SQLEXECDIRECT : {
                    Result resultout =
                        sqlExecuteDirectNoPreChecks(cmd.getMainString());

                    resultout = performPostExecute(resultout);

                    return resultout;
                }
                case ResultConstants.BATCHEXECDIRECT : {
                    Result resultout = sqlExecuteBatchDirect(cmd);

                    resultout = performPostExecute(resultout);

                    return resultout;
                }
                case ResultConstants.SQLPREPARE : {
                    return sqlPrepare(cmd.getMainString());
                }
                case ResultConstants.SQLFREESTMT : {
                    return sqlFreeStatement(cmd.getStatementID());
                }
                case ResultConstants.GETSESSIONATTR : {
                    return getAttributes();
                }
                case ResultConstants.SETSESSIONATTR : {
                    return setAttributes(cmd);
                }
                case ResultConstants.SQLENDTRAN : {
                    switch (cmd.getEndTranType()) {

                        case ResultConstants.COMMIT :
                            commit();
                            break;

                        case ResultConstants.ROLLBACK :
                            rollback();
                            break;

                        case ResultConstants.SAVEPOINT_NAME_RELEASE :
                            try {
                                String name = cmd.getMainString();

                                releaseSavepoint(name);
                            } catch (Throwable t) {
                                return new Result(t, null);
                            }
                            break;

                        case ResultConstants.SAVEPOINT_NAME_ROLLBACK :
                            try {
                                rollbackToSavepoint(cmd.getMainString());
                            } catch (Throwable t) {
                                return new Result(t, null);
                            }
                            break;

                        // not yet
                        //                        case ResultConstants.COMMIT_AND_CHAIN :
                        //                        case ResultConstants.ROLLBACK_AND_CHAIN :
                    }

                    return emptyUpdateCount;
                }
                case ResultConstants.SQLSETCONNECTATTR : {
                    switch (cmd.getConnectionAttrType()) {

                        case ResultConstants.SQL_ATTR_SAVEPOINT_NAME :
                            try {
                                savepoint(cmd.getMainString());
                            } catch (Throwable t) {
                                return new Result(t, null);
                            }

                        // case ResultConstants.SQL_ATTR_AUTO_IPD
                        //   - always true
                        // default: throw - case never happens
                    }

                    return emptyUpdateCount;
                }
                case ResultConstants.SQLDISCONNECT : {
                    close();

                    return emptyUpdateCount;
                }
                default : {
                    return new Result(
                        Trace.runtimeError(
                            Trace.INTERNAL_session_operation_not_supported,
                            null), null);
                }
            }
        }
    }

    private Result performPostExecute(Result r) {

        try {
            if (database != null) {
                database.schemaManager.logSequences(this, database.logger);

                if (isAutoCommit) {
                    database.logger.synchLog();
                }
            }

            return r;
        } catch (Exception e) {
            return new Result(e, null);
        }
    }

    public Result sqlExecuteDirectNoPreChecks(String sql) {

        synchronized (database) {
            return dbCommandInterpreter.execute(sql);
        }
    }

    Result sqlExecuteCompiledNoPreChecks(CompiledStatement cs) {
        return compiledStatementExecutor.execute(cs);
    }

    /**
     * Retrieves a MULTI Result describing three aspects of the
     * CompiledStatement prepared from the SQL argument for execution
     * in this session context. <p>
     *
     * <ol>
     * <li>A PREPARE_ACK mode Result describing id of the statement
     *     prepared by this request.  This is used by the JDBC implementation
     *     to later identify to the engine which prepared statement to execute.
     *
     * <li>A DATA mode result describing the statement's result set metadata.
     *     This is used to generate the JDBC ResultSetMetaData object returned
     *     by PreparedStatement.getMetaData and CallableStatement.getMetaData.
     *
     * <li>A DATA mode result describing the statement's parameter metdata.
     *     This is used to by the JDBC implementation to determine
     *     how to send parameters back to the engine when executing the
     *     statement.  It is also used to construct the JDBC ParameterMetaData
     *     object for PreparedStatements and CallableStatements.
     *
     * @param sql a string describing the desired statement object
     * @throws HsqlException is a database access error occurs
     * @return a MULTI Result describing the compiled statement.
     */
    private Result sqlPrepare(String sql) {

        int csid = compiledStatementManager.getStatementID(currentSchema,
            sql);
        CompiledStatement cs = compiledStatementManager.getStatement(csid);
        Result            rmd;
        Result            pmd;

        if (cs == null) {

            // compile
            try {
                Session sys =
                    database.sessionManager.getSysSession(currentSchema.name,
                        false);

                cs = sys.sqlCompileStatement(sql);
            } catch (Throwable t) {
                return new Result(t, sql);
            }

            csid = compiledStatementManager.registerStatement(csid, cs);
        } else if (!cs.isValid) {

            // revalidate with the original contexts schema
            try {
                Session sys = database.sessionManager.getSysSession(
                    cs.schemaHsqlName.name, false);

                cs = sys.sqlCompileStatement(sql);
            } catch (Throwable t) {
                compiledStatementManager.freeStatement(csid, sessionId);

                return new Result(t, sql);
            }

            csid = compiledStatementManager.registerStatement(csid, cs);
        }

        compiledStatementManager.linkSession(csid, sessionId);

        rmd = cs.describeResult();
        pmd = cs.describeParameters();

        return Result.newPrepareResponse(csid, rmd, pmd);
    }

    private Result sqlExecuteBatch(Result cmd) {

        int               csid;
        Record            record;
        Result            out;
        CompiledStatement cs;
        Expression[]      parameters;
        int[]             updateCounts;
        int               count;

        csid = cmd.getStatementID();
        cs   = compiledStatementManager.getStatement(csid);

        if (cs == null ||!cs.isValid) {
            cs = recompileStatement(cs, csid);

            if (cs == null) {

                // invalid sql has been removed already
                return new Result(
                    Trace.runtimeError(
                        Trace.INVALID_PREPARED_STATEMENT, null), null);
            }
        }

        parameters   = cs.parameters;
        count        = 0;
        updateCounts = new int[cmd.getSize()];
        record       = cmd.rRoot;

        while (record != null) {
            Result   in;
            Object[] pvals = record.data;

            try {
                for (int i = 0; i < parameters.length; i++) {
                    parameters[i].bind(pvals[i]);
                }

                in = compiledStatementExecutor.execute(cs);
            } catch (Throwable t) {
                in = new Result(ResultConstants.ERROR);

                // t.printStackTrace();
                // Trace.printSystemOut(t.toString());
                // if (t instanceof OutOfMemoryError) {
                // System.gc();
                // }
                // "in" alread equals "err"
                // maybe test for OOME and do a gc() ?
                // t.printStackTrace();
            }

            // On the client side, iterate over the vals and throw
            // a BatchUpdateException if a batch status value of
            // esultConstants.EXECUTE_FAILED is encountered in the result
            if (in.mode == ResultConstants.UPDATECOUNT) {
                updateCounts[count++] = in.updateCount;
            } else if (in.mode == ResultConstants.DATA) {

                // FIXME:  we don't have what it takes yet
                // to differentiate between things like
                // stored procedure calls to methods with
                // void return type and select statements with
                // a single row/column containg null
                updateCounts[count++] = ResultConstants.SUCCESS_NO_INFO;
            } else {
                updateCounts = ArrayUtil.arraySlice(updateCounts, 0, count);

                break;
            }

            record = record.next;
        }

        out = new Result(ResultConstants.SQLEXECUTE, updateCounts, 0);

        return out;
    }

    private Result sqlExecuteBatchDirect(Result cmd) {

        Record record;
        Result out;
        int[]  updateCounts;
        int    count;

        count        = 0;
        updateCounts = new int[cmd.getSize()];
        record       = cmd.rRoot;

        while (record != null) {
            Result in;
            String sql = (String) record.data[0];

            try {
                in = dbCommandInterpreter.execute(sql);
            } catch (Throwable t) {
                in = new Result(ResultConstants.ERROR);

                // if (t instanceof OutOfMemoryError) {
                // System.gc();
                // }
                // "in" alread equals "err"
                // maybe test for OOME and do a gc() ?
                // t.printStackTrace();
            }

            // On the client side, iterate over the colType vals and throw
            // a BatchUpdateException if a batch status value of
            // ResultConstants.EXECUTE_FAILED is encountered
            if (in.mode == ResultConstants.UPDATECOUNT) {
                updateCounts[count++] = in.updateCount;
            } else if (in.mode == ResultConstants.DATA) {

                // FIXME:  we don't have what it takes yet
                // to differentiate between things like
                // stored procedure calls to methods with
                // void return type and select statements with
                // a single row/column containg null
                updateCounts[count++] = ResultConstants.SUCCESS_NO_INFO;
            } else {
                updateCounts = ArrayUtil.arraySlice(updateCounts, 0, count);

                break;
            }

            record = record.next;
        }

        out = new Result(ResultConstants.SQLEXECUTE, updateCounts, 0);

        return out;
    }

    /**
     * Retrieves the result of executing the prepared statement whose csid
     * and parameter values/types are encapsulated by the cmd argument.
     *
     * @return the result of executing the statement
     */
    private Result sqlExecute(Result cmd) {

        int               csid  = cmd.getStatementID();
        Object[]          pvals = cmd.getParameterData();
        CompiledStatement cs    = compiledStatementManager.getStatement(csid);
        Expression[]      parameters;

        if (cs == null ||!cs.isValid) {
            cs = recompileStatement(cs, csid);

            if (cs == null) {

                // invalid sql has been removed already
                return new Result(
                    Trace.runtimeError(
                        Trace.INVALID_PREPARED_STATEMENT, null), null);
            }
        }

        parameters = cs.parameters;

        // Don't bother with array length or type checks...trust the client
        // to send pvals with length at least as long
        // as parameters array and with each pval already converted to the
        // correct internal representation corresponding to the type
        try {
            for (int i = 0; i < parameters.length; i++) {
                parameters[i].bind(pvals[i]);
            }
        } catch (Throwable t) {
            return new Result(t, cs.sql);
        }

        return compiledStatementExecutor.execute(cs);
    }

    /**
     * Recompile a prepard statement or free it if no longer valid
     */
    private CompiledStatement recompileStatement(CompiledStatement cs,
            int csid) {

        String sql = compiledStatementManager.getSql(csid);

        if (sql == null) {

            // invalid sql has been removed already
            return null;
        }

        Result r = sqlPrepare(sql);

        if (r.mode == ResultConstants.ERROR) {

            // sql is invalid due to DDL changes
            compiledStatementManager.freeStatement(csid, sessionId);

            return null;
        }

        return compiledStatementManager.getStatement(csid);
    }

    /**
     * Retrieves the result of freeing the statement with the given id.
     *
     * @param csid the numeric identifier of the statement
     */
    private Result sqlFreeStatement(int csid) {

        Result result;

        compiledStatementManager.freeStatement(csid, sessionId);

        result             = new Result(ResultConstants.UPDATECOUNT);
        result.updateCount = 1;

        return result;
    }

// session DATETIME functions
    long          currentDateTimeSCN;
    long          currentMillis;
    java.sql.Date currentDate;
    Time          currentTime;
    Timestamp     currentTimestamp;

    /**
     * Returns the current date, unchanged for the duration of the current
     * execution unit (statement).<p>
     *
     * SQL standards require that CURRENT_DATE, CURRENT_TIME and
     * CURRENT_TIMESTAMP are all evaluated at the same point of
     * time in the duration of each SQL statement, no matter how long the
     * SQL statement takes to complete.<p>
     *
     * When this method or a corresponding method for CURRENT_TIME or
     * CURRENT_TIMESTAMP is first called in the scope of a system change
     * number, currentMillis is set to the current system time. All further
     * CURRENT_XXXX calls in this scope will use this millisecond value.
     * (fredt@users)
     */
    Date getCurrentDate() {

        if (currentDateTimeSCN != sessionSCN) {
            currentDateTimeSCN = sessionSCN;
            currentMillis      = System.currentTimeMillis();
            currentDate        = HsqlDateTime.getCurrentDate(currentMillis);
            currentTime        = null;
            currentTimestamp   = null;
        } else if (currentDate == null) {
            currentDate = HsqlDateTime.getCurrentDate(currentMillis);
        }

        return currentDate;
    }

    /**
     * Returns the current time, unchanged for the duration of the current
     * execution unit (statement)
     */
    Time getCurrentTime() {

        if (currentDateTimeSCN != sessionSCN) {
            currentDateTimeSCN = sessionSCN;
            currentMillis      = System.currentTimeMillis();
            currentDate        = null;
            currentTime =
                new Time(HsqlDateTime.getNormalisedTime(currentMillis));
            currentTimestamp = null;
        } else if (currentTime == null) {
            currentTime =
                new Time(HsqlDateTime.getNormalisedTime(currentMillis));
        }

        return currentTime;
    }

    /**
     * Returns the current timestamp, unchanged for the duration of the current
     * execution unit (statement)
     */
    Timestamp getCurrentTimestamp() {

        if (currentDateTimeSCN != sessionSCN) {
            currentDateTimeSCN = sessionSCN;
            currentMillis      = System.currentTimeMillis();
            currentDate        = null;
            currentTime        = null;
            currentTimestamp   = HsqlDateTime.getTimestamp(currentMillis);
        } else if (currentTimestamp == null) {
            currentTimestamp = HsqlDateTime.getTimestamp(currentMillis);
        }

        return currentTimestamp;
    }

// fredt@users - only INFO_AUTOCOMMIT and INFO_CONNECTION_READONLY are used
    static final int INFO_DATABASE            = 0;
    static final int INFO_USER                = 1;
    static final int INFO_SESSION_ID          = 2;
    static final int INFO_ISOLATION           = 3;
    static final int INFO_AUTOCOMMIT          = 4;
    static final int INFO_DATABASE_READONLY   = 5;
    static final int INFO_CONNECTION_READONLY = 6;

    Result getAttributes() {

        Result   r   = Result.newSessionAttributesResult();
        Object[] row = new Object[] {
            database.getURI(), getUsername(), ValuePool.getInt(sessionId),
            ValuePool.getInt(isolation), ValuePool.getBoolean(isAutoCommit),
            ValuePool.getBoolean(database.databaseReadOnly),
            ValuePool.getBoolean(isReadOnly)
        };

        r.add(row);

        return r;
    }

    Result setAttributes(Result r) {

        Object[] row = r.rRoot.data;

        for (int i = 0; i < row.length; i++) {
            Object value = row[i];

            if (value == null) {
                continue;
            }

            try {
                switch (i) {

                    case INFO_AUTOCOMMIT : {
                        this.setAutoCommit(((Boolean) value).booleanValue());

                        break;
                    }
                    case INFO_CONNECTION_READONLY :
                        this.setReadOnly(((Boolean) value).booleanValue());
                        break;
                }
            } catch (HsqlException e) {
                return new Result(e, null);
            }
        }

        return emptyUpdateCount;
    }

    // DatabaseMetaData.getURL should work as specified for
    // internal connections too.
    public String getInternalConnectionURL() {
        return DatabaseManager.S_URL_PREFIX + database.getURI();
    }

    boolean isProcessingScript() {
        return isProcessingScript;
    }

    boolean isProcessingLog() {
        return isProcessingLog;
    }

    boolean isSchemaDefintion() {
        return oldSchema != null;
    }

    void startSchemaDefinition(String schema) throws HsqlException {

        if (isProcessingScript) {
            setSchema(schema);

            return;
        }

        oldSchema = currentSchema;

        setSchema(schema);
    }

    void endSchemaDefinition() throws HsqlException {

        if (oldSchema == null) {
            return;
        }

        currentSchema = oldSchema;
        oldSchema     = null;

        database.logger.writeToLog(this,
                                   "SET SCHEMA "
                                   + currentSchema.statementName);
    }

    // schema object methods
    public void setSchema(String schema) throws HsqlException {
        currentSchema = database.schemaManager.getSchemaHsqlName(schema);
    }

    /**
     * If schemaName is null, return the current schema name, else return
     * the HsqlName object for the schema. If schemaName does not exist,
     * throw.
     */
    HsqlName getSchemaHsqlName(String name) throws HsqlException {
        return name == null ? currentSchema
                            : database.schemaManager.getSchemaHsqlName(name);
    }

    /**
     * Same as above, but return string
     */
    public String getSchemaName(String name) throws HsqlException {
        return name == null ? currentSchema.name
                            : database.schemaManager.getSchemaName(name);
    }

    /**
     * If schemaName is null, return the current schema name, else return
     * the HsqlName object for the schema. If schemaName does not exist, or
     * schema readonly, throw.
     */
    HsqlName getSchemaHsqlNameForWrite(String name) throws HsqlException {

        HsqlName schema = getSchemaHsqlName(name);

        if (database.schemaManager.isSystemSchema(schema)) {
            throw Trace.error(Trace.INVALID_SCHEMA_NAME_NO_SUBCLASS);
        }

        return schema;
    }

    /**
     * Same as above, but return string
     */
    public String getSchemaNameForWrite(String name) throws HsqlException {

        HsqlName schema = getSchemaHsqlNameForWrite(name);

        return schema.name;
    }

    /**
     * get the root for a temp table index
     */
    Node getIndexRoot(HsqlName index) {

        if (indexArrayMap == null) {
            return null;
        }

        Node node = (Node) indexArrayMap.get(index.hashCode());

        return node;
    }

    void setIndexRoot(HsqlName index, boolean preserve, Node root) {

        if (preserve) {
            if (indexArrayKeepMap == null) {
                if (root == null) {
                    return;
                }
            }

            indexArrayKeepMap = new IntKeyHashMap();
        } else {
            if (indexArrayMap == null) {
                if (root == null) {
                    return;
                }

                indexArrayMap = new IntKeyHashMap();
            }

            indexArrayMap.put(index.hashCode(), root);
        }
    }

    void dropIndex(HsqlName index, boolean preserve) {

        if (preserve) {
            if (indexArrayKeepMap != null) {
                indexArrayKeepMap.remove(index.hashCode());
            }
        } else {
            if (indexArrayMap != null) {
                indexArrayMap.remove(index.hashCode());
            }
        }
    }

    /**
     *
     */
    void clearIndexRoots() {

        if (indexArrayMap != null) {
            indexArrayMap.clear();
        }
    }

    void clearIndexRootsKeep() {

        if (indexArrayKeepMap != null) {
            indexArrayKeepMap.clear();
        }
    }
}
