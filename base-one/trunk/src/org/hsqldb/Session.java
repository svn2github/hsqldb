/* Copyrights and Licenses
 *
 * This product includes Hypersonic SQL.
 * Originally developed by Thomas Mueller and the Hypersonic SQL Group. 
 *
 * Copyright (c) 1995-2000 by the Hypersonic SQL Group. All rights reserved. 
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met: 
 *     -  Redistributions of source code must retain the above copyright notice, this list of conditions
 *         and the following disclaimer. 
 *     -  Redistributions in binary form must reproduce the above copyright notice, this list of
 *         conditions and the following disclaimer in the documentation and/or other materials
 *         provided with the distribution. 
 *     -  All advertising materials mentioning features or use of this software must display the
 *        following acknowledgment: "This product includes Hypersonic SQL." 
 *     -  Products derived from this software may not be called "Hypersonic SQL" nor may
 *        "Hypersonic SQL" appear in their names without prior written permission of the
 *         Hypersonic SQL Group. 
 *     -  Redistributions of any form whatsoever must retain the following acknowledgment: "This
 *          product includes Hypersonic SQL." 
 * This software is provided "as is" and any expressed or implied warranties, including, but
 * not limited to, the implied warranties of merchantability and fitness for a particular purpose are
 * disclaimed. In no event shall the Hypersonic SQL Group or its contributors be liable for any
 * direct, indirect, incidental, special, exemplary, or consequential damages (including, but
 * not limited to, procurement of substitute goods or services; loss of use, data, or profits;
 * or business interruption). However caused any on any theory of liability, whether in contract,
 * strict liability, or tort (including negligence or otherwise) arising in any way out of the use of this
 * software, even if advised of the possibility of such damage. 
 * This software consists of voluntary contributions made by many individuals on behalf of the
 * Hypersonic SQL Group.
 *
 *
 * For work added by the HSQL Development Group:
 *
 * Copyright (c) 2001-2004, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer, including earlier
 * license statements (above) and comply with all above license conditions.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution, including earlier
 * license statements (above) and comply with all above license conditions.
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

import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.HashSet;
import org.hsqldb.lib.HashMappedList;
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

/**
 *  Implementation of a user session with the database. In 1.7.2 Session
 *  becomes the public interface to an HSQLDB database, accessed locally or
 *  remotely via SessionInterface.
 *
 * @version  1.7.2
 */

/** @todo fredt - move error and assert string literals to Trace */
class Session implements SessionInterface {

    private Database       dDatabase;
    private User           uUser;
    private HsqlArrayList  tTransaction;
    private boolean        isAutoCommit;
    private boolean        isNestedTransaction;
    private boolean        isNestedOldAutoCommit;
    private int            nestedOldTransIndex;
    private boolean        isReadOnly;
    private int            currentMaxRows;
    private int            sessionMaxRows;
    private Number         iLastIdentity = ValuePool.getInt(0);
    private boolean        isClosed;
    private int            iId;
    private HashMappedList savepoints;
    private boolean        script;
    private jdbcConnection intConnection;
    private Tokenizer      tokenizer;
    private Parser         parser;
    final static Result emptyUpdateCount =
        new Result(ResultConstants.UPDATECOUNT);

/** @todo fredt - clarify in which circumstances Session has to disconnect */
    public Session getSession() {
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

        iId                       = id;
        dDatabase                 = db;
        uUser                     = user;
        tTransaction              = new HsqlArrayList();
        savepoints                = new HashMappedList(4);
        isAutoCommit              = autocommit;
        isReadOnly                = readonly;
        dbCommandInterpreter      = new DatabaseCommandInterpreter(this);
        compiledStatementExecutor = new CompiledStatementExecutor(this);
        compiledStatementManager  = db.compiledStatementManager;
        tokenizer                 = new Tokenizer();
        parser                    = new Parser(dDatabase, tokenizer, this);
    }

    /**
     *  Retrieves the session identifier for this Session.
     *
     * @return the session identifier for this Session
     */
    public int getId() {
        return iId;
    }

    /**
     * Closes this Session.
     */
    public synchronized void close() {

        if (!isClosed) {
            synchronized (dDatabase) {
                dDatabase.sessionManager.processDisconnect(this);
            }
        }
    }

    /**
     * Closes this Session, freeing any resources associated with it
     * and rolling back any uncommited transaction it may have open.
     */
    synchronized void disconnect() {

        // PRE:  disconnect() is called _only_ from SessionManager
        if (isClosed) {
            return;
        }

        rollback();
        dDatabase.dropTempTables(this);
        compiledStatementManager.processDisconnect(iId);

        dDatabase                 = null;
        uUser                     = null;
        tTransaction              = null;
        savepoints                = null;
        intConnection             = null;
        compiledStatementExecutor = null;
        compiledStatementManager  = null;
        dbCommandInterpreter      = null;
        iLastIdentity             = null;
        isClosed                  = true;
    }

    /**
     * Retrieves whether this Session is closed.
     *
     * @return true if this Session is closed
     */
    public boolean isClosed() {
        return isClosed;
    }

    /**
     * Setter for iLastIdentity attribute.
     *
     * @param  i the new value
     */
    void setLastIdentity(Number i) {
        iLastIdentity = i;
    }

    /**
     * Getter for iLastIdentity attribute.
     *
     * @return the current value
     */
    Number getLastIdentity() {
        return iLastIdentity;
    }

    /**
     * Retrieves the Database instance to which this
     * Session represents a connection.
     *
     * @return the Database object to which this Session is connected
     */
    Database getDatabase() {
        return dDatabase;
    }

    /**
     * Retrieves the name, as known to the database, of the
     * user currently controlling this Session.
     *
     * @return the name of the user currently connected within this Session
     */
    String getUsername() {
        return uUser.getName();
    }

    /**
     * Retrieves the User object representing the user currently controlling
     * this Session.
     *
     * @return this Session's User object
     */
    User getUser() {
        return uUser;
    }

    /**
     * Sets this Session's User object to the one specified by the
     * user argument.
     *
     * @param  user the new User object for this session
     */
    void setUser(User user) {
        uUser = user;
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
        uUser.checkAdmin();
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
        uUser.check(object, right);
    }

    /**
     * This is used for reading - writing to existing tables.
     * @throws  HsqlException
     */
    void checkReadWrite() throws HsqlException {
        Trace.check(!isReadOnly, Trace.DATABASE_IS_READONLY);
    }

    /**
     * This is used for creating new database objects such as tables.
     * @throws  HsqlException
     */
    void checkDDLWrite() throws HsqlException {

        boolean condition = uUser.isSys() ||!dDatabase.filesReadOnly;

        Trace.check(condition, Trace.DATABASE_IS_READONLY);
    }

    /**
     * Sets the session user's password to the value of the argument, s.
     *
     * @param  s
     */
    void setPassword(String s) {
        uUser.setPassword(s);
    }

    /**
     *  Adds a single-row deletion step to the transaction UNDO buffer.
     *
     * @param  table the table from which the row was deleted
     * @param  row the deleted row
     * @throws  HsqlException
     */
    void addTransactionDelete(Table table,
                              Object row[]) throws HsqlException {

        if (!isAutoCommit) {
            Transaction t = new Transaction(true, isNestedTransaction, table,
                                            row);

            tTransaction.add(t);
        }
    }

    /**
     *  Adds a single-row inssertion step to the transaction UNDO buffer.
     *
     * @param  table the table into which the row was inserted
     * @param  row the inserted row
     * @throws  HsqlException
     */
    void addTransactionInsert(Table table,
                              Object row[]) throws HsqlException {

        if (!isAutoCommit) {
            Transaction t = new Transaction(false, isNestedTransaction,
                                            table, row);

            tTransaction.add(t);
        }
    }

    /**
     *  Setter for the autocommit attribute.
     *
     * @param  autocommit the new value
     * @throws  HsqlException
     */
    public void setAutoCommit(boolean autocommit) {

        if (autocommit != isAutoCommit) {
            commit();

            isAutoCommit = autocommit;

            try {
                dDatabase.logger.writeToLog(this, getAutoCommitStatement());
            } catch (HsqlException e) {}
        }
    }

    /**
     * Commits any uncommited transaction this Session may have open
     *
     * @throws  HsqlException
     */
    public void commit() {

        if (!tTransaction.isEmpty()) {
            try {
                dDatabase.logger.writeToLog(this, Token.T_COMMIT);
            } catch (HsqlException e) {}

            tTransaction.clear();
        }

        savepoints.clear();
    }

    /**
     * Rolls back any uncommited transaction this Session may have open.
     *
     * @throws  HsqlException
     */
    public void rollback() {

        int i = tTransaction.size();

        synchronized (dDatabase) {
            while (i-- > 0) {
                Transaction t = (Transaction) tTransaction.get(i);

                t.rollback(this);
            }
        }

        if (!tTransaction.isEmpty()) {
            try {
                dDatabase.logger.writeToLog(this, Token.T_ROLLBACK);
            } catch (HsqlException e) {}

            tTransaction.clear();
        }

        savepoints.clear();
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
        savepoints.add(name, ValuePool.getInt(tTransaction.size()));

        try {
            dDatabase.logger.writeToLog(this, Token.T_SAVEPOINT + " " + name);
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

        int index = savepoints.getIndex(name);

        Trace.check(index >= 0, Trace.SAVEPOINT_NOT_FOUND, name);

        Integer oi = (Integer) savepoints.get(index);

        index = oi.intValue();

        int i = tTransaction.size() - 1;

        for (; i >= index; i--) {
            Transaction t = (Transaction) tTransaction.get(i);

            t.rollback(this);
            tTransaction.remove(i);
        }

        releaseSavepoint(name);

        try {
            dDatabase.logger.writeToLog(this,
                                        Token.T_ROLLBACK + " " + Token.T_TO
                                        + " " + Token.T_SAVEPOINT + " "
                                        + name);
        } catch (HsqlException e) {}
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

        Trace.doAssert(!isNestedTransaction, "beginNestedTransaction");

        isNestedOldAutoCommit = isAutoCommit;

        // now all transactions are logged
        isAutoCommit        = false;
        nestedOldTransIndex = tTransaction.size();
        isNestedTransaction = true;
    }

    /**
     * Ends a nested transaction.
     *
     * @param  rollback true to roll back or false to commit the nested transaction
     * @throws  HsqlException
     */
    void endNestedTransaction(boolean rollback) throws HsqlException {

        Trace.doAssert(isNestedTransaction, "endNestedTransaction");

        if (rollback) {
            int i = tTransaction.size();

            while (i-- > nestedOldTransIndex) {
                Transaction t = (Transaction) tTransaction.get(i);

                t.rollback(this);
            }
        }

        // reset after the rollback
        isNestedTransaction = false;
        isAutoCommit        = isNestedOldAutoCommit;

        if (isAutoCommit == true) {
            tTransaction.setSize(nestedOldTransIndex);
        }
    }

    /**
     * Setter for readonly attribute.
     *
     * @param  readonly the new value
     */
    public void setReadOnly(boolean readonly) throws HsqlException {

        if (!readonly && dDatabase.databaseReadOnly) {
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

    String getAutoCommitStatement() {
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

// boucherb@users.sf.net 20020810 metadata 1.7.2
//----------------------------------------------------------------
    private final long connectTime = System.currentTimeMillis();

// more effecient for MetaData concerns than checkAdmin

    /**
     * Getter for admin attribute.
     *
     * @ return the current value
     */
    boolean isAdmin() {
        return uUser.isAdmin();
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
        return tTransaction.size();
    }

    /**
     * Retrieves whether the database object identifier by the dbobject
     * argument is accessible by the current Session User.
     *
     * @return true if so, else false
     */
    boolean isAccessible(Object dbobject) throws HsqlException {
        return uUser.isAccessible(dbobject);
    }

    /**
     * Retrieves the set of the fully qualified names of the classes on
     * which this Session's current user has been granted execute access.
     * If the current user has the privileges of the ADMIN role, the
     * set of all class grants made to all users is returned, including
     * the PUBLIC user, regardless of the value of the andToPublic argument.
     * In reality, ADMIN users have the right to invoke the methods of any
     * and all classes on the class path, but this list is still useful in
     * an ADMIN user context, for other reasons.
     *
     * @param andToPublic if true, grants to public are included
     * @return the list of the fully qualified names of the classes on
     *      which this Session's current user has been granted execute
     *      access.
     */
    HashSet getGrantedClassNames(boolean andToPublic) {
        return (isAdmin()) ? dDatabase.getUserManager().getGrantedClassNames()
                           : uUser.getGrantedClassNames(andToPublic);
    }

// boucherb@users 20030417 - patch 1.7.2 - compiled statement support
//-------------------------------------------------------------------
    DatabaseCommandInterpreter dbCommandInterpreter;
    CompiledStatementExecutor  compiledStatementExecutor;
    CompiledStatementManager   compiledStatementManager;

    private CompiledStatement sqlCompileStatement(String sql,
            int type) throws HsqlException {

        String            token;
        int               cmd;
        CompiledStatement cs;
        boolean           isCmdOk;

        parser.reset(sql);

        token   = tokenizer.getString();
        cmd     = Token.get(token);
        isCmdOk = true;

        switch (cmd) {

            case Token.SELECT : {
                cs = parser.compileSelectStatement(false);

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
                if (type != CompiledStatement.CALL) {
                    throw Trace.error(Trace.ASSERT_FAILED,
                                      "not a CALL statement");
                }

                cs = parser.compileCallStatement();

                break;
            }
            default : {
                isCmdOk = false;
                cs      = null;

                break;
            }
        }

        // In addition to requiring that the compilation was successful,
        // we also require that the submitted sql represents a _single_
        // valid DML statement.
        if (!isCmdOk) {
            throw Trace.error(Trace.UNEXPECTED_TOKEN, token);
        }

        // fredt - now accepts semicolon and whitespace at the end of statement
        // fredt - investigate if it should or not for prepared statements
        while (tokenizer.getPosition() < tokenizer.getLength()) {
            token = tokenizer.getString();

            if (token.length() != 0 &&!token.equals(Token.T_SEMICOLON)) {
                throw Trace.error(Trace.UNEXPECTED_TOKEN, token);
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
            if (Trace.DOASSERT) {
                Trace.doAssert(!isNestedTransaction);
            }

            Trace.check(!isClosed, Trace.ACCESS_IS_DENIED,
                        Trace.getMessage(Trace.Session_execute));
        } catch (Throwable t) {
            return new Result(t, null);
        }

        int type = cmd.iMode;

        synchronized (dDatabase) {
            if (sessionMaxRows == 0) {
                currentMaxRows = cmd.iUpdateCount;
            }

            DatabaseManager.gc();

            switch (type) {

                case ResultConstants.SQLEXECUTE : {
                    return sqlExecute(cmd);
                }
                case ResultConstants.BATCHEXECUTE : {
                    return sqlExecuteBatch(cmd);
                }
                case ResultConstants.SQLEXECDIRECT : {
                    return sqlExecuteDirectNoPreChecks(cmd.getMainString());
                }
                case ResultConstants.BATCHEXECDIRECT : {
                    return sqlExecuteBatchDirect(cmd);
                }
                case ResultConstants.SQLPREPARE : {
                    return sqlPrepare(cmd.getMainString(),
                                      cmd.getStatementType());
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
                    return Trace.toResult(
                        Trace.error(
                            Trace.INTERNAL_session_operation_not_supported));
                }
            }
        }
    }

    /**
     * Directly executes all of the sql statements in the list
     * represented by the sql argument string.
     *
     * @param sql a sql string
     * @return the result of the last sql statement in the list
     */
    synchronized Result sqlExecuteDirect(String sql) {

        try {
            if (Trace.DOASSERT) {
                Trace.doAssert(!isNestedTransaction);
            }

            Trace.check(!isClosed, Trace.ACCESS_IS_DENIED,
                        Trace.getMessage(Trace.Session_sqlExecuteDirect));

            synchronized (dDatabase) {
                Trace.check(!dDatabase.isShutdown(),
                            Trace.DATABASE_IS_SHUTDOWN);

                return dbCommandInterpreter.execute(sql);
            }
        } catch (Throwable t) {
            return new Result(t, null);
        }
    }

    Result sqlExecuteDirectNoPreChecks(String sql) {
        return dbCommandInterpreter.execute(sql);
    }

    /**
     * Executes the statement represented by the compiled statement argument.
     *
     * @param cs the compiled statement to execute
     * @return the result of executing the compiled statement
     */
    synchronized Result sqlExecuteCompiled(CompiledStatement cs) {

        try {
            if (Trace.DOASSERT) {
                Trace.doAssert(!isNestedTransaction);
            }

            Trace.check(!isClosed, Trace.ACCESS_IS_DENIED,
                        Trace.getMessage(Trace.Session_sqlExecuteCompiled));

            synchronized (dDatabase) {
                Trace.check(!dDatabase.isShutdown(),
                            Trace.DATABASE_IS_SHUTDOWN);

                return compiledStatementExecutor.execute(cs);
            }
        } catch (Throwable t) {
            return new Result(t, null);
        }
    }

    Result sqlExecuteCompiledNoPreChecks(CompiledStatement cs) {
        return compiledStatementExecutor.execute(cs);
    }

    /**
     * Retrieves a MULTI Result describing three aspects of the
     * CompiledStatement prepared from the SQL argument for execution
     * in this session context: <p>
     *
     * <ol>
     * <li>An PREPARE_ACK mode Result describing id of the statement
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
    private Result sqlPrepare(String sql, int type) {

        CompiledStatement cs   = null;
        int               csid = compiledStatementManager.getStatementID(sql);
        Result            rsmd;
        Result            pmd;

        // ...check valid...
        if (csid > 0 && compiledStatementManager.isValid(csid, iId)) {
            cs   = compiledStatementManager.getStatement(csid);
            rsmd = cs.describeResultSet();
            pmd  = cs.describeParameters();

            return Result.newPrepareResponse(csid, rsmd, pmd);
        }

        // ...compile or (re)validate
        try {
            cs = sqlCompileStatement(sql, type);
        } catch (Throwable t) {
            return new Result(t, sql);
        }

// boucherb@users
// TODO:  It is still unclear to me as to whether, in the case of revalidation
//        v.s. first compilation, the newly created CompiledStatement
//        object should replace the old one in the CompiledStatementManager
//        repository.  If, for instance, a table column has been dropped and
//        then a column with the same name is added with different data type,
//        constraints, etc., the existing CompiledStatement object is not
//        equivalent in its effect and perhaps runs the risk of corrupting
//        the database.  For instance, a CompiledStatement contains
//        fixed mappings from positions in a column value expression array
//        to column positions in the target table.  Thus, an alteration to a
//        target table may leave an existing CompiledStatement's SQL
//        character sequence valid, but not its execution plan.
//        OTOH, simply replacing the old execution plan with a new one
//        may also be undesirable, as the intended and actual effects
//        may become divergent. Once again, for example, if a column name
//        comes to mean a different column, then by blindly replacing the
//        old CompiledStatement with the new, inserting, updating
//        or predicating happens upon an unintended column.
//        The only DDL operations that raise such dangers are sequences
//        involving dropping a columns and then adding an incompatible one
//        of the same name at the same position or alterations that
//        change the positions of columns.  All other alterations to
//        database objects should, in theory, allow the original
//        CompiledStatement to continue to operate as intended.
        if (csid <= 0) {
            csid = compiledStatementManager.registerStatement(cs);
        }

        compiledStatementManager.setValidated(csid, iId,
                                              dDatabase.getDDLSCN());

        rsmd = cs.describeResultSet();
        pmd  = cs.describeParameters();

        return Result.newPrepareResponse(csid, rsmd, pmd);
    }

    private Result sqlExecuteBatch(Result cmd) {

        int               csid;
        Object[]          pvals;
        Record            record;
        Result            in;
        Result            out;
        Result            err;
        CompiledStatement cs;
        Expression[]      parameters;
        int[]             updateCounts;
        int               count;

        csid = cmd.getStatementID();
        cs   = compiledStatementManager.getStatement(csid);

        if (cs == null) {
            return Trace.toResult(
                Trace.error(Trace.INTERNAL_ivalid_compiled_statement_id));
        }

        if (!compiledStatementManager.isValid(csid, iId)) {
            out = sqlPrepare(cs.sql, cs.type);

            if (out.iMode == ResultConstants.ERROR) {
                return out;
            }
        }

        parameters   = cs.parameters;
        count        = 0;
        updateCounts = new int[cmd.getSize()];
        record       = cmd.rRoot;
        out = new Result(ResultConstants.SQLEXECUTE, updateCounts, 0);
        err          = new Result(ResultConstants.ERROR);

        while (record != null) {
            pvals = record.data;
            in    = err;

            try {
                for (int i = 0; i < parameters.length; i++) {
                    parameters[i].bind(pvals[i]);
                }

                in = compiledStatementExecutor.execute(cs);
            } catch (Throwable t) {

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
            switch (in.iMode) {

                case ResultConstants.UPDATECOUNT : {
                    updateCounts[count++] = in.iUpdateCount;

                    break;
                }
                case ResultConstants.DATA : {

                    // FIXME:  we don't have what it takes yet
                    // to differentiate between things like
                    // stored procedure calls to methods with
                    // void return type and select statements with
                    // a single row/column containg null
                    updateCounts[count++] = ResultConstants.SUCCESS_NO_INFO;

                    break;
                }
                case ResultConstants.ERROR :
                default : {
                    updateCounts[count++] = ResultConstants.EXECUTE_FAILED;

                    break;
                }
            }

            record = record.next;
        }

        return out;
    }

    private Result sqlExecuteBatchDirect(Result cmd) {

        Record record;
        Result in;
        Result out;
        Result err;
        int[]  updateCounts;
        int    count;
        String sql;

        count        = 0;
        updateCounts = new int[cmd.getSize()];
        record       = cmd.rRoot;
        out = new Result(ResultConstants.SQLEXECUTE, updateCounts, 0);
        err          = new Result(ResultConstants.ERROR);

        while (record != null) {
            sql = (String) record.data[0];
            in  = err;

            try {
                in = dbCommandInterpreter.execute(sql);
            } catch (Throwable t) {

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
            switch (in.iMode) {

                case ResultConstants.UPDATECOUNT : {
                    updateCounts[count++] = in.iUpdateCount;

                    break;
                }
                case ResultConstants.DATA : {

                    // FIXME:  we don't have what it takes yet
                    // to differentiate between things like
                    // stored procedure calls to methods with
                    // void return type and select statements with
                    // a single row/column containg null
                    updateCounts[count++] = ResultConstants.SUCCESS_NO_INFO;

                    break;
                }
                case ResultConstants.ERROR :
                default : {
                    updateCounts[count++] = ResultConstants.EXECUTE_FAILED;

                    break;
                }
            }

            record = record.next;
        }

        return out;
    }

    /**
     * Retrieves the result of executing the prepared statement whose csid
     * and parameter values/types are encapsulated by the cmd argument.
     *
     * @return the result of executing the statement
     */
    private Result sqlExecute(Result cmd) {

        int               csid;
        Object[]          pvals;
        CompiledStatement cs;
        Expression[]      parameters;

        csid  = cmd.getStatementID();
        pvals = cmd.getParameterData();
        cs    = compiledStatementManager.getStatement(csid);

        if (cs == null) {
            return Trace.toResult(
                Trace.error(Trace.INTERNAL_ivalid_compiled_statement_id));
        }

        if (!compiledStatementManager.isValid(csid, iId)) {
            Result r = sqlPrepare(cs.sql, cs.type);

            if (r.iMode == ResultConstants.ERROR) {

                // TODO:
                // maybe compiledStatementManager.freeStatement(csid,iId);?
                return r;
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
     * Retrieves the result of freeing the statement with the given id.
     *
     * @param csid the numeric identifier of the statement
     * @return the result of freeing the indicated statement
     */
    private Result sqlFreeStatement(int csid) {

        boolean existed;
        Result  result;

        existed = compiledStatementManager.freeStatement(csid, iId);
        result  = new Result(ResultConstants.UPDATECOUNT);

        if (existed) {
            result.iUpdateCount = 1;
        }

        return result;
    }

//------------------------------------------------------------------------------
    static final int INFO_DATABASE            = 0;
    static final int INFO_USER                = 1;
    static final int INFO_SESSION_ID          = 2;
    static final int INFO_IDENTITY            = 3;
    static final int INFO_AUTOCOMMIT          = 4;
    static final int INFO_DATABASE_READONLY   = 5;
    static final int INFO_CONNECTION_READONLY = 6;

    Result getAttributes() {

        Result r = new Result(ResultConstants.DATA, 7);

        r.metaData.sName = r.metaData.sLabel = r.metaData.sTable =
            new String[] {
            "", "", "", "", "", "", ""
        };
        r.metaData.colType = new int[] {
            Types.VARCHAR, Types.VARCHAR, Types.INTEGER,
            iLastIdentity instanceof Long ? Types.BIGINT
                                          : Types.INTEGER, Types.BOOLEAN,
            Types.BOOLEAN, Types.BOOLEAN
        };

        Object[] row = new Object[] {
            dDatabase.getPath(), getUsername(), ValuePool.getInt(iId),
            iLastIdentity, ValuePool.getBoolean(isAutoCommit),
            ValuePool.getBoolean(dDatabase.databaseReadOnly),
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
}
