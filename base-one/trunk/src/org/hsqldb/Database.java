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
 * Copyright (c) 2001-2002, The HSQL Development Group
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

import java.sql.SQLException;
import java.sql.Types;
import java.util.Enumeration;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.HsqlHashMap;
import org.hsqldb.lib.HsqlObjectToIntMap;
import org.hsqldb.lib.StopWatch;

/**
 *  Database is the root class for HSQL Database Engine database. <p>
 *
 *  Although it either directly or indirectly provides all or most of the
 *  services required for DBMS functionality, this class should not be used
 *  directly by an application. Instead, to achieve portability and
 *  generality, the jdbc* classes should be used.
 *
 * @version  1.7.0
 */

// fredt@users 20020130 - patch 476694 by velichko - transaction savepoints
// additions to different parts to support savepoint transactions
// fredt@users 20020215 - patch 1.7.0 - new HsqlProperties class
// support use of properties from database.properties file
// fredt@users 20020218 - patch 1.7.0 - DEFAULT keyword
// support for default values for table columns
// fredt@users 20020305 - patch 1.7.0 - restructuring
// some methods move to Table.java, some removed
// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP) - restructuring
// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP) - error trapping
// boucherb@users 20020130 - patch 1.7.0 - use lookup for speed
// idents listed in alpha-order for easy check of stats...
// fredt@users 20020420 - patch523880 by leptipre@users - VIEW support
// fredt@users 20020430 - patch 549741 by velichko - ALTER TABLE RENAME
// fredt@users 20020405 - patch 1.7.0 - other ALTER TABLE statements
// boucherb@users - doc 1.7.0 - added javadoc comments
// tony_lai@users 20020820 - patch 595099 - use user-defined PK name
// tony_lai@users 20020820 - patch 595073 - duplicated exception msg
// tony_lai@users 20020820 - patch 595156 - violation of constraint name
// tony_lai@users 20020820 - changes to shutdown compact to save memory
// boucherb@users 20020828 - allow reconnect to local db that has shutdown
// fredt@users 20020912 - patch 1.7.1 by fredt - drop duplicate name triggers
// fredt@users 20020912 - patch 1.7.1 by fredt - log alter statements
// fredt@users 20021112 - patch 1.7.2 by Nitin Chauhan - use of switch
// rewrite of the majority of multiple if(){}else if(){} chains with switch()
// boucherb@users 20020310 - class loader update for JDK 1.1 compliance
// boucherb@users 20020310 - disable ALTER TABLE DDL on VIEWs (avoid NPE)
// fredt@users 20020314 - patch 1.7.2 by gilead@users - drop table syntax
class Database {

    private String        sName;
    private UserManager   aAccess;
    private HsqlArrayList tTable;
    DatabaseInformation   dInfo;
    ClassLoader           classLoader;

// ----------------------------------------------------------------------------
// boucherb@users - pluggable system table producer strategy for metadata 1.7.2

    /** true only while this database is in the open() method */
    private boolean isOpening;

// ----------------------------------------------------------------------------
    Logger  logger;
    boolean databaseReadOnly;    // all tables are readonly

// ----------------------------------------------------------------------------
// akede@users - 1.7.2 patch Files readonly

    /** true means that all file based table will automaticly become readonly */
    boolean filesReadOnly;       // cached tables are readonly

// ----------------------------------------------------------------------------
    boolean                        sqlEnforceSize;
    int                            firstIdentity;
    private boolean                bShutdown;
    private HsqlHashMap            hAlias;
    private boolean                bIgnoreCase;
    private boolean                bReferentialIntegrity;
    SessionManager                 sessionManager;
    private HsqlDatabaseProperties databaseProperties;
    private Tokenizer              tokenizer;
    DatabaseObjectNames            triggerNameList;
    DatabaseObjectNames            indexNameList;

    //for execute()
    private static final int CALL                  = 1;
    private static final int CHECKPOINT            = 2;
    private static final int COMMIT                = 3;
    private static final int CONNECT               = 4;
    private static final int CREATE                = 5;
    private static final int DELETE                = 6;
    private static final int DISCONNECT            = 7;
    private static final int DROP                  = 8;
    private static final int GRANT                 = 9;
    private static final int INSERT                = 10;
    private static final int REVOKE                = 11;
    private static final int ROLLBACK              = 12;
    private static final int SAVEPOINT             = 13;
    private static final int SCRIPT                = 14;
    private static final int SELECT                = 15;
    private static final int SET                   = 16;
    private static final int SHUTDOWN              = 17;
    private static final int UPDATE                = 18;
    private static final int SEMICOLON             = 19;
    private static final int ALTER                 = 20;
    private static final int ADD                   = 24;
    private static final int ALIAS                 = 35;
    private static final int AUTOCOMMIT            = 43;
    private static final int CACHED                = 31;
    private static final int COLUMN                = 27;
    private static final int CONSTRAINT            = 25;
    private static final int FOREIGN               = 26;
    private static final int IGNORECASE            = 41;
    private static final int INDEX                 = 22;
    private static final int LOGSIZE               = 39;
    private static final int LOGTYPE               = 40;
    private static final int MAXROWS               = 42;
    private static final int MEMORY                = 30;
    private static final int PASSWORD              = 37;
    private static final int PRIMARY               = 36;
    private static final int PROPERTY              = 47;
    private static final int READONLY              = 38;
    private static final int REFERENTIAL_INTEGRITY = 46;
    private static final int RENAME                = 23;
    private static final int SOURCE                = 44;
    private static final int TABLE                 = 21;
    private static final int TEXT                  = 29;
    private static final int TRIGGER               = 33;
    private static final int UNIQUE                = 28;
    private static final int USER                  = 34;
    private static final int VIEW                  = 32;
    private static final int WRITE_DELAY           = 45;
    private static final HsqlObjectToIntMap commandSet =
        new HsqlObjectToIntMap(67);

    static {
        commandSet.put("ALTER", ALTER);
        commandSet.put("CALL", CALL);
        commandSet.put("CHECKPOINT", CHECKPOINT);
        commandSet.put("COMMIT", COMMIT);
        commandSet.put("CONNECT", CONNECT);
        commandSet.put("CREATE", CREATE);
        commandSet.put("DELETE", DELETE);
        commandSet.put("DISCONNECT", DISCONNECT);
        commandSet.put("DROP", DROP);
        commandSet.put("GRANT", GRANT);
        commandSet.put("INSERT", INSERT);
        commandSet.put("REVOKE", REVOKE);
        commandSet.put("ROLLBACK", ROLLBACK);
        commandSet.put("SAVEPOINT", SAVEPOINT);
        commandSet.put("SCRIPT", SCRIPT);
        commandSet.put("SELECT", SELECT);
        commandSet.put("SET", SET);
        commandSet.put("SHUTDOWN", SHUTDOWN);
        commandSet.put("UPDATE", UPDATE);
        commandSet.put(";", SEMICOLON);

        //
        commandSet.put("TABLE", TABLE);
        commandSet.put("INDEX", INDEX);
        commandSet.put("RENAME", RENAME);
        commandSet.put("ADD", ADD);
        commandSet.put("CONSTRAINT", CONSTRAINT);
        commandSet.put("FOREIGN", FOREIGN);
        commandSet.put("COLUMN", COLUMN);
        commandSet.put("UNIQUE", UNIQUE);
        commandSet.put("TEXT", TEXT);
        commandSet.put("MEMORY", MEMORY);
        commandSet.put("CACHED", CACHED);
        commandSet.put("VIEW", VIEW);
        commandSet.put("TRIGGER", TRIGGER);
        commandSet.put("USER", USER);
        commandSet.put("ALIAS", ALIAS);
        commandSet.put("PASSWORD", PASSWORD);
        commandSet.put("PRIMARY", PRIMARY);
        commandSet.put("PROPERTY", PROPERTY);
        commandSet.put("READONLY", READONLY);
        commandSet.put("LOGSIZE", LOGSIZE);
        commandSet.put("LOGTYPE", LOGTYPE);
        commandSet.put("IGNORECASE", IGNORECASE);
        commandSet.put("MAXROWS", MAXROWS);
        commandSet.put("AUTOCOMMIT", AUTOCOMMIT);
        commandSet.put("SOURCE", SOURCE);
        commandSet.put("WRITE_DELAY", WRITE_DELAY);
        commandSet.put("REFERENTIAL_INTEGRITY", REFERENTIAL_INTEGRITY);
    }

    /**
     *  Constructs a new Database object that mounts or creates the database
     *  files specified by the supplied name.
     *
     * @param  name the path to and common name shared by the database files
     *      this Database uses
     * @exception  SQLException if the specified path and common name
     *      combination is illegal or unavailable, or the database files the
     *      name resolves to are in use by another process
     */
    Database(String name) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        sName  = name.trim();
        logger = new Logger();

        // does not need to be done more than once
        try {
            classLoader = getClass().getClassLoader();
        } catch (Exception e) {

            // strict security policy:  just use the system/boot loader
            classLoader = null;
        }

        open();
    }

    /**
     * Opens the database.  The database can be opened by the constructor,
     * or reopened by the close(int closemode) method during a
     * "shutdown compact".
     * @see close(int closemode)
     */

    // tony_lai@users 20020820
    private void open() throws SQLException {

        tTable  = new HsqlArrayList();
        aAccess = new UserManager();
        hAlias  = Library.getAliasMap();

//        logger                = new Logger();
        tokenizer             = new Tokenizer();
        triggerNameList       = new DatabaseObjectNames();
        indexNameList         = new DatabaseObjectNames();
        bReferentialIntegrity = true;

        boolean newdatabase = false;

// boucherb@users 20021128 - metadata/classloader 1.7.2 sys user
        User sysUser = aAccess.createSysUser(this);

        sessionManager = new SessionManager(this, sysUser);

// -------------------------------------------------------------------
        databaseProperties = new HsqlDatabaseProperties(this);
        dInfo              = DatabaseInformation.newDatabaseInformation(this);

        if (sName.length() == 0) {
            throw Trace.error(Trace.GENERAL_ERROR, "bad database name");
        } else if (sName.equals(".")) {
            newdatabase = true;
        } else {
            newdatabase = logger.openLog(this, sName);
        }

        if (newdatabase) {
            execute("CREATE USER SA PASSWORD \"\" ADMIN",
                    sessionManager.getSysSession());
        }

// boucherb@users 20021128 - metadata 1.7.2 system tables
        isOpening = false;

        dInfo.setWithContent(true);

// -------------------------------------------------------
    }

    /**
     *  Retrieves this Database object's name, as know to this Database
     *  object.
     *
     * @return  this Database object's name
     */
    String getName() {
        return sName;
    }

    /**
     *  Retrieves this Database object's properties.
     *
     * @return  this Database object's properties object
     */
    HsqlDatabaseProperties getProperties() {
        return databaseProperties;
    }

    /**
     *  isShutdown attribute getter.
     *
     * @return  the value of this Database object's isShutdown attribute
     */
    boolean isShutdown() {
        return bShutdown;
    }

    /**
     *  Constructs a new Session that operates within (is connected to) the
     *  context of this Database object. <p>
     *
     *  If successful, the new Session object initially operates on behalf of
     *  the user specified by the supplied user name.
     *
     * @param  username the name of the initial user of this session. The user
     *      must already exist in this Database object.
     * @param  password the password of the specified user. This must match
     *      the password, as known to this Database object, of the specified
     *      user
     * @return  a new Session object that initially that initially operates on
     *      behalf of the specified user
     * @throws  SQLException if the specified user does not exist or a bad
     *      password is specified
     */
    synchronized Session connect(String username,
                                 String password) throws SQLException {

        User user = aAccess.getUser(username.toUpperCase(),
                                    password.toUpperCase());
        Session session = sessionManager.newSession(this, user,
            databaseReadOnly);

        logger.writeToLog(session,
                          "CONNECT USER " + username + " PASSWORD \""
                          + password + "\"");

        return session;
    }

    /**
     *  A specialized SQL statement executor, tailored for use by {@link
     *  WebServerConnection}. Calling this method fully connects the specified
     *  user, executes the specifed statement, and then disconects.
     *
     * @param  user the name of the user for which to execute the specified
     *      statement. The user must already exist in this Database object.
     * @param  password the password of the specified user. This must match
     *      the password, as known to this Database object, of the specified
     *      user
     * @param  statement the SQL statement to execute
     * @return  the result of executing the specified statement, in a form
     *      already suitable for transmitting as part of an HTTP response.
     */
    byte[] execute(String user, String password, String statement) {

        Result r = null;

        try {
            Session session = connect(user, password);

            r = execute(statement, session);

            sessionManager.processDisconnect(session);

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
        } catch (SQLException e) {
            r = new Result(e.getMessage(), e.getErrorCode());
        } catch (Exception e) {
            r = new Result(e.getMessage(), Trace.GENERAL_ERROR);
        }

        try {
            return r.getBytes();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    /**
     *  The main SQL statement executor. <p>
     *
     *  All requests to execute SQL statements against this Database object
     *  eventually go through this method.
     *
     * @param  statement the SQL statement to execute
     * @param  session an object representing a connected user and a
     *      collection of session state attributes
     * @return  the result of executing the specified statement, in a form
     *      suitable for either wrapping in a local ResultSet object or for
     *      transmitting to a remote client via the native HSQLDB protocol
     */
    synchronized Result execute(String statement, Session session) {

        if (Record.gcFrequency != 0
                && Record.memoryRecords > Record.gcFrequency) {
            System.gc();
            Trace.printSystemOut("gc at " + Record.memoryRecords);

            Record.memoryRecords = 0;
        }

        if (Trace.TRACE) {
            Trace.trace(statement);
        }

        Result rResult = null;

        try {

            //tokenizer.reset(statement);
            Tokenizer tokenizer = new Tokenizer(statement);
            Parser    p         = new Parser(this, tokenizer, session);

            if (Trace.DOASSERT) {
                Trace.doAssert(!session.isNestedTransaction());
            }

            Trace.check(session != null, Trace.ACCESS_IS_DENIED);
            Trace.check(!bShutdown, Trace.DATABASE_IS_SHUTDOWN);

            while (true) {
                tokenizer.setPartMarker();
                session.setScripting(false);

                String sToken = tokenizer.getString();

                if (sToken.length() == 0) {
                    break;
                }

                switch (commandSet.get(sToken)) {

                    case SELECT :
                        rResult = p.processSelect();

// boucherb@users  - metadata 1.7.2 - system tables
                        setMetaDirty(rResult);

// --
                        break;

                    case INSERT :
                        rResult = p.processInsert();
                        break;

                    case UPDATE :
                        rResult = p.processUpdate();
                        break;

                    case DELETE :
                        rResult = p.processDelete();
                        break;

                    case CALL :
                        rResult = p.processCall();
                        break;

                    case SET :
                        rResult = processSet(tokenizer, session);
                        break;

                    case COMMIT :
                        rResult = processCommit(tokenizer, session);

                        session.setScripting(true);
                        break;

                    case ROLLBACK :
                        rResult = processRollback(tokenizer, session);

                        session.setScripting(true);
                        break;

                    case SAVEPOINT :
                        rResult = processSavepoint(tokenizer, session);

                        session.setScripting(true);
                        break;

                    case CREATE :
                        rResult = processCreate(tokenizer, session);

// boucherb@users  - metadata 1.7.2 - system tables
                        setMetaDirty(null);

// --
                        break;

                    case ALTER :
                        rResult = processAlter(tokenizer, session);

// boucherb@users  - metadata 1.7.2 - system tables
                        setMetaDirty(null);

// --
                        break;

                    case DROP :
                        rResult = processDrop(tokenizer, session);

// boucherb@users  - metadata 1.7.2 - system tables
                        setMetaDirty(null);

// --
                        break;

                    case GRANT :
                        rResult = processGrantOrRevoke(tokenizer, session,
                                                       true);

// boucherb@users  - metadata 1.7.2 - system tables
                        setMetaDirty(null);

// --
                        break;

                    case REVOKE :
                        rResult = processGrantOrRevoke(tokenizer, session,
                                                       false);

// boucherb@users  - metadata 1.7.2 - system tables
                        setMetaDirty(null);

// --
                        break;

                    case CONNECT :
                        rResult = processConnect(tokenizer, session);

// boucherb@users  - metadata 1.7.2 - system tables
                        setMetaDirty(null);

// --
                        break;

                    case DISCONNECT :
                        rResult = sessionManager.processDisconnect(session);
                        break;

                    case SCRIPT :
                        rResult = processScript(tokenizer, session);
                        break;

                    case SHUTDOWN :
                        rResult = processShutdown(tokenizer, session);
                        break;

                    case CHECKPOINT :
                        rResult = processCheckpoint(tokenizer, session);
                        break;

                    case SEMICOLON :
                        break;

                    default :
                        throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
                }

                if (session.getScripting()) {
                    logger.writeToLog(session, tokenizer.getLastPart());
                }
            }
        } catch (SQLException e) {

            // e.printStackTrace();
// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
// tony_lai@users 20020820 - patch 595073
//            rResult = new Result(Trace.getMessage(e) + " in statement ["
            rResult = new Result(e.getMessage() + " in statement ["
                                 + statement + "]", e.getErrorCode());
        } catch (Exception e) {
            e.printStackTrace();

            String s = Trace.getMessage(Trace.GENERAL_ERROR) + " " + e;

            rResult = new Result(s + " in statement [" + statement + "]",
                                 Trace.GENERAL_ERROR);
        } catch (java.lang.OutOfMemoryError e) {
            e.printStackTrace();

            rResult = new Result("out of memory", Trace.GENERAL_ERROR);
        }

        return rResult == null ? new Result()
                               : rResult;
    }

    /**
     *  Puts this Database object in global read-only mode. That is, after
     *  this call, all existing and future sessions are limited to read-only
     *  transactions. Any following attempts to update the state of the
     *  database will result in throwing a SQLException.
     */
    void setReadOnly() {
        databaseReadOnly = true;
        filesReadOnly    = true;
    }

// ----------------------------------------------------------------------------
// akede@users - 1.7.2 patch Files readonly

    /**
     * Puts this Database object in a special read-only mode that only
     * affects file bases tables such as cached or text tables.
     * After this call all tables that use a file based format will automaticly
     * be set to read-only modus.
     * All changes that are done to memory based tables (e.g. Temp-Tables or
     * normall Memory-Tables will <b>NOT</b> be stored or updated in the script
     * file.
     * This mode is speciall for all uses on read-only media but with the need
     * of using Temp-Tables for queries or not persistent changes.
     */
    void setFilesReadOnly() {
        filesReadOnly = true;
    }

    boolean isFilesReadOnly() {
        return filesReadOnly;
    }

// ----------------------------------------------------------------------------

    /**
     *  Retrieves a HsqlArrayList containing references to all non-system
     *  tables and views. This includes all tables and views registered with
     *  this Database object via a call to {@link #linkTable linkTable}.
     *
     * @return  a HsqlArrayList of all registered non-system tables and views
     */
    HsqlArrayList getTables() {
        return tTable;
    }

    /**
     *  Retrieves the UserManager object for this Database.
     *
     * @return  UserManager object
     */
    UserManager getUserManager() {
        return aAccess;
    }

    /**
     *  isReferentialIntegrity attribute setter.
     *
     * @param  ref if true, this Database object enforces referential
     *      integrity, else not
     */
    void setReferentialIntegrity(boolean ref) {
        bReferentialIntegrity = ref;
    }

    /**
     *  isReferentialIntegrity attribute getter.
     *
     * @return  indicates whether this Database object is currently enforcing
     *      referential integrity
     */
    boolean isReferentialIntegrity() {
        return bReferentialIntegrity;
    }

    /**
     *  Retrieves a map from Java method-call name aliases to the
     *  fully-qualified names of the Java methods themsleves.
     *
     * @return  a map in the form of a HsqlHashMap
     */
    HsqlHashMap getAlias() {
        return hAlias;
    }

    /**
     *  Retieves a Java method's fully qualified name, given a String that is
     *  supposedly an alias for it. <p>
     *
     *  This is somewhat of a misnomer, since it is not an alias that is being
     *  retrieved, but rather what the supplied alias maps to. If the
     *  specified alias does not map to any registered Java method
     *  fully-qualified name, then the specified String itself is returned.
     *
     * @param  s a call name alias that supposedly maps to a registered Java
     *      method
     * @return  a Java method fully-qualified name, or null if no method is
     *      registered with the given alias
     */
    String getAlias(String s) {

        String alias = (String) hAlias.get(s);

        return (alias == null) ? s
                               : alias;
    }

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
// temp tables should be accessed by the owner and not scripted in the log

    /**
     *  Retrieves the specified user defined table or view visible within the
     *  context of the specified Session, or any system table of the given
     *  name. This excludes any temp tables created in different Sessions.
     *
     * @param  name of the table or view to retrieve
     * @param  session the Session within which to search for user tables
     * @return  the user table or view, or system table
     * @throws  SQLException if there is no such table or view
     */
    Table getTable(String name, Session session) throws SQLException {

        Table t = findUserTable(name, session);

        if (t == null) {
            t = dInfo.getSystemTable(name, session);
        }

        if (t == null) {
            throw Trace.error(Trace.TABLE_NOT_FOUND, name);
        }

        return t;
    }

    /**
     *  get a user
     *
     * @param  name
     * @param  session
     * @return
     * @throws  SQLException
     */
    Table getUserTable(String name, Session session) throws SQLException {

        Table t = findUserTable(name, session);

        if (t == null) {
            throw Trace.error(Trace.TABLE_NOT_FOUND, name);
        }

        return t;
    }

    Table getUserTable(String name) throws SQLException {

        Table t = findUserTable(name);

        if (t == null) {
            throw Trace.error(Trace.TABLE_NOT_FOUND, name);
        }

        return t;
    }

    Table findUserTable(String name) {

        for (int i = 0, tsize = tTable.size(); i < tsize; i++) {
            Table t = (Table) tTable.get(i);

            if (t.equals(name)) {
                return t;
            }
        }

        return null;
    }

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
    Table findUserTable(String name, Session session) {

        for (int i = 0, tsize = tTable.size(); i < tsize; i++) {
            Table t = (Table) tTable.get(i);

            if (t.equals(name, session)) {
                return t;
            }
        }

        return null;
    }

    /**
     *  Attempts to register the specified table or view with this Database
     *  object.
     *
     * @param  t the table of view to register
     * @throws  SQLException if there is a problem
     */
    void linkTable(Table t) throws SQLException {
        tTable.add(t);
    }

    /**
     *  isIgnoreCase attribute getter.
     *
     * @return  the value of this Database object's isIgnoreCase attribute
     */
    boolean isIgnoreCase() {
        return bIgnoreCase;
    }

    /**
     *  Responsible for parsing and executing the SCRIPT SQL statement
     *
     * @param  c the tokenized representation of the statement being processed
     * @param  session
     * @return
     * @throws  SQLException
     */
    private Result processScript(Tokenizer c,
                                 Session session)
                                 throws java.io.IOException, SQLException {

        String sToken = c.getString();

        if (c.wasValue()) {
            sToken = (String) c.getAsValue();

            StopWatch stopw = new StopWatch();
            DatabaseScriptWriter sw = new DatabaseScriptWriter(this, sToken,
                true, true);

            sw.writeAll();
            sw.close();
            System.out.println("text script" + stopw.elapsedTime());

            return new Result();
        } else {
            c.back();
            session.checkAdmin();

            return DatabaseScript.getScript(this, false);
        }
    }

    /**
     *  Responsible for handling the parse and execution of CREATE SQL
     *  statements.
     *
     *  All CREATE command require an ADMIN user except
     *  CREATE TEMP [MEMORY] TABLE
     *
     * @param  c the tokenized representation of the statement being processed
     * @param  session
     * @return
     * @throws  SQLException
     */
    private Result processCreate(Tokenizer c,
                                 Session session) throws SQLException {

        session.checkReadWrite();

        String  sToken = c.getString();
        boolean isTemp = false;

        if (sToken.equals("TEMP")) {
            isTemp = true;
            sToken = c.getString();

            switch (commandSet.get(sToken)) {

                case TEXT :
                    session.checkAdmin();
                case TABLE :
                case MEMORY :
                    session.setScripting(false);
                    break;

                default :
                    throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
            }
        } else {
            session.checkAdmin();
            session.checkDDLWrite();
            session.setScripting(true);
        }

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
        boolean unique    = false;
        int     tableType = 0;

        switch (commandSet.get(sToken)) {

            case TABLE :
                tableType = isTemp ? Table.TEMP_TABLE
                                   : Table.MEMORY_TABLE;

                processCreateTable(c, session, tableType);
                break;

            case MEMORY :
                c.getThis("TABLE");

                tableType = isTemp ? Table.TEMP_TABLE
                                   : Table.MEMORY_TABLE;

                processCreateTable(c, session, tableType);
                break;

            case CACHED :
                c.getThis("TABLE");
                processCreateTable(c, session, Table.CACHED_TABLE);
                break;

            case TEXT :
                c.getThis("TABLE");

                tableType = isTemp ? Table.TEMP_TEXT_TABLE
                                   : Table.TEXT_TABLE;

                processCreateTable(c, session, tableType);
                break;

            case VIEW :
                processCreateView(c, session);
                break;

            case TRIGGER :
                processCreateTrigger(c, session);
                break;

            case USER :
                String u = c.getStringToken();

                c.getThis("PASSWORD");

                String  p     = c.getStringToken();
                boolean admin = c.getString().equals("ADMIN");

                aAccess.createUser(u, p, admin);
                break;

            case ALIAS :
                String aName = c.getString();

                sToken = c.getString();

                Trace.check(sToken.equals("FOR"), Trace.UNEXPECTED_TOKEN,
                            sToken);

                sToken = c.getString();

// fredt@users 20010701 - patch 1.6.1 by fredt - open <1.60 db files
// convert org.hsql.Library aliases from versions < 1.60 to org.hsqldb
// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP) - ABS function
                if (sToken.startsWith("org.hsql.Library.")) {
                    sToken = "org.hsqldb.Library."
                             + sToken.substring("org.hsql.Library.".length());
                } else if (sToken.equals("java.lang.Math.abs")) {
                    sToken = "org.hsqldb.Library.abs";
                }

                hAlias.put(aName, sToken);
                break;

            case UNIQUE :
                unique = true;

                c.getThis("INDEX");

            //fall thru
            case INDEX :
                String  name         = c.getName();
                boolean isnamequoted = c.wasQuotedIdentifier();

                c.getThis("ON");

                Table t = getTable(c.getName(), session);

                addIndexOn(c, session, name, isnamequoted, t, unique);
                break;

            default : {
                throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
            }
        }

        return new Result();
    }

    /**
     *  Process a bracketed column list as used in the declaration of SQL
     *  CONSTRAINTS and return an array containing the indexes of the columns
     *  within the table.
     *
     * @param  c
     * @param  t table that contains the columns
     * @return
     * @throws  SQLException if a column is not found or is duplicated
     */
    private int[] processColumnList(Tokenizer c,
                                    Table t) throws SQLException {

        HsqlArrayList v = new HsqlArrayList();
        HsqlHashMap   h = new HsqlHashMap();

        c.getThis("(");

        while (true) {
            String colname = c.getName();

            v.add(colname);
            h.put(colname, colname);

            String sToken = c.getString();

            if (sToken.equals(",")) {
                continue;
            }

            if (sToken.equals(")")) {
                break;
            }

            throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
        }

        int s = v.size();

        if (s != h.size()) {
            throw Trace.error(Trace.COLUMN_ALREADY_EXISTS,
                              "duplicate column in list");
        }

        int col[] = new int[s];

        for (int i = 0; i < s; i++) {
            col[i] = t.getColumnNr((String) v.get(i));
        }

        return col;
    }

    /**
     *  Indexes defined in DDL scripts are handled by this method. If the
     *  name of an existing index begins with "SYS_", the name is changed to
     *  begin with "USER_". The name should be unique within the database.
     *  For compatibility with old database, non-unique names are modified
     *  and assigned a new name<p>
     *
     *  In 1.7.2 no new index is created if an equivalent already exists.
     *  (fredt@users)
     *
     * @param  c
     * @param  session
     * @param  name
     * @param  t
     * @param  unique
     * @param  namequoted The feature to be added to the IndexOn attribute
     * @throws  SQLException
     */
    private void addIndexOn(Tokenizer c, Session session, String name,
                            boolean namequoted, Table t,
                            boolean unique) throws SQLException {

        HsqlName indexname;
        int      col[] = processColumnList(c, t);

        if (HsqlName.isReservedIndexName(name)) {
            indexname = HsqlName.newAutoName("USER", name);
        } else {
            indexname = new HsqlName(name, namequoted);
        }

        if (this.indexNameList.containsName(name)) {
            throw Trace.error(Trace.INDEX_ALREADY_EXISTS);
        }

        session.commit();
        session.setScripting(!t.isTemp());

        TableWorks tw = new TableWorks(t);

        tw.createIndex(col, indexname, unique);
    }

    /**
     *  Finds the table that has an index with the given name in the
     *  whole database and visible in this session.
     *
     */
    private Table findUserTableForIndex(String name, Session session) {

        HsqlName hsqlname = indexNameList.getOwner(name);

        if (hsqlname == null) {
            return null;
        }

        return findUserTable(hsqlname.name, session);
    }

    /**
     *  Retrieves the index of a table or view in the HsqlArrayList that
     *  contains these objects for a Database.
     *
     * @param  table the Table object
     * @return  the index of the specified table or view, or -1 if not found
     */
    int getTableIndex(Table table) {

        for (int i = 0, tsize = tTable.size(); i < tsize; i++) {
            Table t = (Table) tTable.get(i);

            if (t == table) {
                return i;
            }
        }

        return -1;
    }

    /**
     *  Responsible for handling the execution of CREATE TRIGGER SQL
     *  statements. <p>
     *
     *  typical sql is: CREATE TRIGGER tr1 AFTER INSERT ON tab1 CALL "pkg.cls"
     *
     * @param  c the tokenized representation of the statement being processed
     * @param  session
     * @throws  SQLException
     */
    private void processCreateTrigger(Tokenizer c,
                                      Session session) throws SQLException {

        Table   t;
        boolean bForEach   = false;
        boolean bNowait    = false;
        int     nQueueSize = TriggerDef.getDefaultQueueSize();
        String  sTrigName  = c.getName();
        boolean namequoted = c.wasQuotedIdentifier();

        Trace.doAssert(!triggerNameList.containsName(sTrigName),
                       " trigger " + sTrigName + "exists");

// --
        String sWhen = c.getString();
        String sOper = c.getString();

        c.getThis("ON");

        String sTableName = c.getString();

        t = getTable(sTableName, session);

// boucherb@users 20021128 - disallow triggers on system tables
        if (t.isView() || t.tableType == Table.SYSTEM_TABLE) {
            throw Trace.error(Trace.NOT_A_TABLE);
        }

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
        session.setScripting(!t.isTemp());

        // "FOR EACH ROW" or "CALL"
        String tok = c.getString();

        if (tok.equals("FOR")) {
            tok = c.getString();

            if (tok.equals("EACH")) {
                tok = c.getString();

                if (tok.equals("ROW")) {
                    bForEach = true;
                    tok      = c.getString();    // should be 'NOWAIT' or 'QUEUE' or 'CALL'
                } else {
                    throw Trace.error(Trace.UNEXPECTED_END_OF_COMMAND, tok);
                }
            } else {
                throw Trace.error(Trace.UNEXPECTED_END_OF_COMMAND, tok);
            }
        }

        if (tok.equals("NOWAIT")) {
            bNowait = true;
            tok     = c.getString();    // should be 'CALL' or 'QUEUE'
        }

        if (tok.equals("QUEUE")) {
            nQueueSize = Integer.parseInt(c.getString());
            tok        = c.getString();    // should be 'CALL'
        }

        if (!tok.equals("CALL")) {
            throw Trace.error(Trace.UNEXPECTED_END_OF_COMMAND, tok);
        }

        String     sClassName = c.getString();    // double quotes have been stripped
        TriggerDef td;
        Trigger    o;

        try {
            Class cl = Class.forName(sClassName);    // dynamically load class

            o = (Trigger) cl.newInstance();          // dynamically instantiate it
            td = new TriggerDef(sTrigName, namequoted, sWhen, sOper,
                                bForEach, t, o, "\"" + sClassName + "\"",
                                bNowait, nQueueSize);

            if (td.isValid()) {
                t.addTrigger(td);
                td.start();                          // start the trigger thread
            } else {
                String msg = "Error in parsing trigger command ";

                throw Trace.error(Trace.UNEXPECTED_TOKEN, msg);
            }
        } catch (Exception e) {
            String msg = "Exception in loading trigger class "
                         + e.getMessage();

            throw Trace.error(Trace.UNKNOWN_FUNCTION, msg);
        }

// boucherb@users 20021128 - enforce unique trigger names
        triggerNameList.addName(sTrigName, t.getName());

// --
    }

    /**
     *  Responsible for handling the creation of table columns during the
     *  process of executing CREATE TABLE statements.
     *
     * @param  c the tokenized representation of the statement being processed
     * @param  t target table
     * @return
     * @throws  SQLException
     */
    private Column processCreateColumn(Tokenizer c,
                                       Table t) throws SQLException {

        boolean identity     = false;
        boolean primarykey   = false;
        String  sToken       = c.getString();
        String  sColumn      = sToken;
        boolean isnamequoted = c.wasQuotedIdentifier();
        String  typestring   = c.getString();
        int     iType        = Column.getTypeNr(typestring);

        Trace.check(!sColumn.equals(Table.DEFAULT_PK),
                    Trace.COLUMN_ALREADY_EXISTS, sColumn);

        if (typestring.equals("IDENTITY")) {
            identity   = true;
            primarykey = true;
        }

        if (iType == Types.VARCHAR && bIgnoreCase) {
            iType = Column.VARCHAR_IGNORECASE;
        }

        sToken = c.getString();

        if (iType == Types.DOUBLE && sToken.equals("PRECISION")) {
            sToken = c.getString();
        }

// fredt@users 20020130 - patch 491987 by jimbag@users
        String sLen = "";

        if (sToken.equals("(")) {

            // read length
            while (true) {
                sToken = c.getString();

                if (sToken.equals(")")) {
                    break;
                }

                sLen += sToken;
            }

            sToken = c.getString();
        }

        int iLen   = 0;
        int iScale = 0;

        // see if we have a scale specified
        int index;

        if ((index = sLen.indexOf(",")) != -1) {
            String sScale = sLen.substring(index + 1, sLen.length());

            sLen = sLen.substring(0, index);

            try {
                iScale = Integer.parseInt(sScale.trim());
            } catch (NumberFormatException ne) {
                throw Trace.error(Trace.UNEXPECTED_TOKEN, sLen);
            }
        }

        // convert the length
        if (sLen.trim().length() > 0) {
            try {
                iLen = Integer.parseInt(sLen.trim());
            } catch (NumberFormatException ne) {
                throw Trace.error(Trace.UNEXPECTED_TOKEN, sLen);
            }
        }

        String defaultvalue = null;

        if (sToken.equals("DEFAULT")) {
            defaultvalue = processCreateDefaultValue(c, iType, iLen);
            sToken       = c.getString();
        }

        boolean nullable = true;

        if (sToken.equals("NULL")) {
            sToken = c.getString();
        } else if (sToken.equals("NOT")) {
            c.getThis("NULL");

            nullable = false;
            sToken   = c.getString();
        }

        if (sToken.equals("IDENTITY")) {
            identity   = true;
            sToken     = c.getString();
            primarykey = true;
        }

        if (sToken.equals("PRIMARY")) {
            c.getThis("KEY");

            primarykey = true;
        } else {
            c.back();
        }

        return new Column(new HsqlName(sColumn, isnamequoted), nullable,
                          iType, iLen, iScale, identity, primarykey,
                          defaultvalue);
    }

    String processCreateDefaultValue(Tokenizer c, int iType,
                                     int iLen) throws SQLException {

        String  defaultvalue = c.getString();
        boolean wasminus     = false;

        // see if it is a negative number
        if (defaultvalue.equals("-") && c.getType() != Types.VARCHAR) {
            wasminus     = true;
            defaultvalue += c.getString();
        }

        if (c.wasValue() && iType != Types.BINARY && iType != Types.OTHER) {
            Object sv = c.getAsValue();

            if (wasminus) {
                sv = Column.negate(sv, iType);
            }

            if (sv != null) {

                // check conversion of literals to values and size constraints
                try {
                    Column.convertObject(sv, iType);
                } catch (Exception e) {
                    throw Trace.error(Trace.WRONG_DEFAULT_CLAUSE,
                                      defaultvalue);
                }

                String tempdefaultvalue = Column.convertObject(sv);

                // ensure char trimming does not affect the value
                String testdefault =
                    sqlEnforceSize
                    ? (String) Table.enforceSize(tempdefaultvalue, iType,
                                                 iLen, false)
                    : tempdefaultvalue;

                // if default value is too long for fixed size column
                if (!tempdefaultvalue.equals(testdefault)) {
                    throw Trace.error(Trace.WRONG_DEFAULT_CLAUSE,
                                      defaultvalue);
                }
            }
        } else {
            throw Trace.error(Trace.WRONG_DEFAULT_CLAUSE, defaultvalue);
        }

        return defaultvalue;
    }

    private HsqlArrayList processCreateConstraints(Tokenizer c,
            Session session, Table t, boolean constraint,
            int[] primarykeycolumn) throws SQLException {

        String sToken;

// fredt@users 20020225 - comment
// HSQLDB relies on primary index to be the first one defined
// and needs original or system added primary key before any non-unique index
// is created
        HsqlArrayList tempConstraints = new HsqlArrayList();
        TempConstraint tempConst = new TempConstraint(null, primarykeycolumn,
            null, null, Constraint.MAIN, Constraint.NO_ACTION,
            Constraint.NO_ACTION);

// tony_lai@users 20020820 - patch 595099
        HsqlName pkName = null;

        tempConstraints.add(tempConst);

        if (!constraint) {
            return tempConstraints;
        }

        int i = 0;

        while (true) {
            sToken = c.getString();

            HsqlName cname = null;

            i++;

            if (sToken.equals("CONSTRAINT")) {
                cname  = new HsqlName(c.getName(), c.wasQuotedIdentifier());
                sToken = c.getString();
            }

            switch (commandSet.get(sToken)) {

                case PRIMARY : {
                    c.getThis("KEY");

// tony_lai@users 20020820 - patch 595099
                    pkName = cname;

                    int col[] = processColumnList(c, t);
                    TempConstraint mainConst =
                        (TempConstraint) tempConstraints.get(0);

                    Trace.check(mainConst.localCol == null,
                                Trace.SECOND_PRIMARY_KEY);

                    mainConst.localCol = col;

                    break;
                }
                case UNIQUE : {
                    int col[] = processColumnList(c, t);

                    if (cname == null) {
                        cname = HsqlName.newAutoName("CT");
                    }

                    tempConst = new TempConstraint(cname, col, null, null,
                                                   Constraint.UNIQUE,
                                                   Constraint.NO_ACTION,
                                                   Constraint.NO_ACTION);

                    tempConstraints.add(tempConst);

                    break;
                }
                case FOREIGN : {
                    c.getThis("KEY");

                    tempConst = processCreateFK(c, session, t, cname);

                    if (tempConst.expCol == null) {
                        TempConstraint mainConst =
                            (TempConstraint) tempConstraints.get(0);

                        tempConst.expCol = mainConst.localCol;

                        if (tempConst.expCol == null) {
                            throw Trace.error(Trace.INDEX_NOT_FOUND,
                                              "table has no primary key");
                        }
                    }

                    if (tempConst.updateAction == Constraint.SET_DEFAULT
                            || tempConst.deleteAction
                               == Constraint.SET_DEFAULT) {
                        for (int j = 0; j < tempConst.localCol.length; j++) {
                            if (t.getColumn(tempConst.localCol[j])
                                    .getDefaultString() == null) {
                                throw Trace.error(
                                    Trace.COLUMN_TYPE_MISMATCH,
                                    "missing DEFAULT value on column '"
                                    + t.getColumn(
                                        tempConst.localCol[j]).columnName
                                            .name + "'");
                            }
                        }
                    }

                    t.checkColumnsMatch(tempConst.localCol,
                                        tempConst.expTable, tempConst.expCol);
                    tempConstraints.add(tempConst);
                }
            }

            sToken = c.getString();

            if (sToken.equals(",")) {
                continue;
            }

            if (sToken.equals(")")) {
                break;
            }

            throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
        }

        return tempConstraints;
    }

// fredt@users 20020225 - patch 509002 by fredt
// temporary attributes for constraints used in processCreateTable()

    /**
     *  temporary attributes for constraints used in processCreateTable()
     */
    private class TempConstraint {

        HsqlName name;
        int[]    localCol;
        Table    expTable;
        int[]    expCol;
        int      type;
        int      deleteAction;
        int      updateAction;

        TempConstraint(HsqlName name, int[] localCol, Table expTable,
                       int[] expCol, int type, int deleteAction,
                       int updateAction) {

            this.name         = name;
            this.type         = type;
            this.localCol     = localCol;
            this.expTable     = expTable;
            this.expCol       = expCol;
            this.deleteAction = deleteAction;
            this.updateAction = updateAction;
        }
    }

// fredt@users 20020225 - patch 509002 by fredt
// process constraints after parsing to include primary keys defined as
// constraints
// fredt@users 20020225 - patch 489777 by fredt
// better error trapping
// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)

    /**
     *  Responsible for handling the execution CREATE TABLE SQL statements.
     *
     * @param  c
     * @param  session
     * @param  type Description of the Parameter
     * @throws  SQLException
     */
    private void processCreateTable(Tokenizer c, Session session,
                                    int type) throws SQLException {

        Table   t;
        String  sToken       = c.getName();
        boolean isnamequoted = c.wasQuotedIdentifier();

// boucherb@users - metadata 1.7.2
        if (dInfo.isSystemTable(sToken)
                || findUserTable(sToken, session) != null) {
            throw Trace.error(Trace.TABLE_ALREADY_EXISTS, sToken);
        }

// --
        if (type == Table.TEMP_TEXT_TABLE || type == Table.TEXT_TABLE) {
            t = new TextTable(this, new HsqlName(sToken, isnamequoted), type,
                              session.getId());
        } else {
            t = new Table(this, new HsqlName(sToken, isnamequoted), type,
                          session.getId());
        }

        c.getThis("(");

        int[]   primarykeycolumn = null;
        int     column           = 0;
        boolean constraint       = false;

        while (true) {
            sToken       = c.getString();
            isnamequoted = c.wasQuotedIdentifier();

// fredt@users 20020225 - comment
// we can check here for reserved words used with quotes as column names
            switch (commandSet.get(sToken)) {

                case CONSTRAINT :
                case PRIMARY :
                case FOREIGN :
                case UNIQUE :
                    constraint = true;
            }

            c.back();

            if (constraint) {
                break;
            }

            Column newcolumn = processCreateColumn(c, t);

            t.addColumn(newcolumn);

            if (newcolumn.isPrimaryKey()) {
                Trace.check(primarykeycolumn == null,
                            Trace.SECOND_PRIMARY_KEY, "column " + column);

                primarykeycolumn = new int[]{ column };
            }

            sToken = c.getString();

            if (sToken.equals(",")) {
                column++;

                continue;
            }

            if (sToken.equals(")")) {
                break;
            }

            throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
        }

        HsqlArrayList tempConstraints = processCreateConstraints(c, session,
            t, constraint, primarykeycolumn);

        try {
            session.commit();

// fredt@users 20020225 - patch 509002 by fredt
// it is essential to stay compatible with existing cached tables
// so we create all constraints and indexes (even duplicates) for cached
// tables
// CONSTRAINT PRIMARY KEY can appear in user scripts and new tables only so
// we can safely apply it correctly
// first apply any primary key constraint
// then set all the constriants
// also, duplicate indexes can be avoided if we choose to in the future but
// currently we have to accept them to stay compatible with existing cached
// tables that include them
            TempConstraint tempConst =
                (TempConstraint) tempConstraints.get(0);

// tony_lai@users 20020820 - patch 595099
            t.createPrimaryKey(tempConst.name, tempConst.localCol, true);

            boolean logDDL = false;

            for (int i = 1; i < tempConstraints.size(); i++) {
                tempConst = (TempConstraint) tempConstraints.get(i);

                if (tempConst.type == Constraint.UNIQUE) {
                    TableWorks tw = new TableWorks(t);

                    tw.createUniqueConstraint(tempConst.localCol,
                                              tempConst.name);

                    t = tw.getTable();
                }

                if (tempConst.type == Constraint.FOREIGN_KEY) {
                    TableWorks tw = new TableWorks(t);

                    tw.createForeignKey(tempConst.localCol, tempConst.expCol,
                                        tempConst.name, tempConst.expTable,
                                        tempConst.deleteAction,
                                        tempConst.updateAction);

                    t = tw.getTable();
                }
            }

            linkTable(t);
        } catch (SQLException e) {

// fredt@users 20020225 - comment
// if a SQLException is thrown while creating table, any foreign key that has
// been created leaves it modification to the expTable in place
// need to undo those modifications. This should not happen in practice.
            removeExportedKeys(t);

            throw e;
        }
    }

    TempConstraint processCreateFK(Tokenizer c, Session session, Table t,
                                   HsqlName cname) throws SQLException {

        int localcol[] = processColumnList(c, t);

        c.getThis("REFERENCES");

        String expTableName = c.getString();
        Table  expTable;

// fredt@users 20020221 - patch 520213 by boucherb@users - self reference FK
// allows foreign keys that reference a column in the same table
        if (t.equals(expTableName)) {
            expTable = t;
        } else {
            expTable = getTable(expTableName, session);
        }

        int    expcol[] = null;
        String sToken   = c.getString();

        c.back();

// fredt@users 20020503 - patch 1.7.0 by fredt -  FOREIGN KEY on table
        if (sToken.equals("(")) {
            expcol = processColumnList(c, expTable);
        } else {

            // the exp table must have a user defined primary key
            Index expIndex = expTable.getPrimaryIndex();

            if (expIndex != null) {
                expcol = expIndex.getColumns();

                if (expcol[0] == expTable.getColumnCount()) {
                    throw Trace.error(Trace.INDEX_NOT_FOUND,
                                      expTableName + " has no primary key");
                }
            }

            // with CREATE TABLE, (expIndex == null) when self referencing FK
            // is declared in CREATE TABLE
            // null will be returned for expCol and will be checked
            // in caller method
            // with ALTER TABLE, (expIndex == null) when table has no PK
        }

        sToken = c.getString();

        // -- In a while loop we parse a maximium of two
        // -- "ON" statements following the foreign key
        // -- definition this can be
        // -- ON [UPDATE|DELETE] [CASCADE|SET [NULL|DEFAULT]]
        int deleteAction = Constraint.NO_ACTION;
        int updateAction = Constraint.NO_ACTION;

        while (sToken.equals("ON")) {
            sToken = c.getString();

            if (deleteAction == Constraint.NO_ACTION
                    && sToken.equals("DELETE")) {
                sToken = c.getString();

                if (sToken.equals("SET")) {
                    sToken = c.getString();

                    if (sToken.equals("DEFAULT")) {
                        deleteAction = Constraint.SET_DEFAULT;
                    } else if (sToken.equals("NULL")) {
                        deleteAction = Constraint.SET_NULL;
                    } else {
                        throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
                    }
                } else if (sToken.equals("CASCADE")) {
                    deleteAction = Constraint.CASCADE;
                } else {
                    throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
                }
            } else if (updateAction == Constraint.NO_ACTION
                       && sToken.equals("UPDATE")) {
                sToken = c.getString();

                if (sToken.equals("SET")) {
                    sToken = c.getString();

                    if (sToken.equals("DEFAULT")) {
                        updateAction = Constraint.SET_DEFAULT;
                    } else if (sToken.equals("NULL")) {
                        updateAction = Constraint.SET_NULL;
                    } else {
                        throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
                    }
                } else if (sToken.equals("CASCADE")) {
                    updateAction = Constraint.CASCADE;
                }
            } else {
                throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
            }

            sToken = c.getString();
        }

        c.back();

        if (cname == null) {
            cname = HsqlName.newAutoName("FK");
        }

        return new TempConstraint(cname, localcol, expTable, expcol,
                                  Constraint.FOREIGN_KEY, deleteAction,
                                  updateAction);
    }

// fredt@users 20020420 - patch523880 by leptipre@users - VIEW support

    /**
     *  Responsible for handling the execution CREATE VIEW SQL statements.
     *
     * @param  session
     * @param  c
     * @throws  SQLException
     */
    private void processCreateView(Tokenizer c,
                                   Session session) throws SQLException {

        View   v;
        String sToken      = c.getName();
        int    logposition = c.getPartMarker();

        if (findUserTable(sToken, session) != null) {
            throw Trace.error(Trace.VIEW_ALREADY_EXISTS, sToken);
        }

        v = new View(this, new HsqlName(sToken, c.wasQuotedIdentifier()));

        c.getThis("AS");
        c.setPartMarker();
        c.getThis("SELECT");

        Result rResult;
        Parser p       = new Parser(this, c, session);
        int    maxRows = session.getMaxRows();

        try {
            Select select = p.parseSelect();

            if (select.sIntoTable != null) {
                throw (Trace.error(Trace.TABLE_NOT_FOUND));
            }

            select.setPreProcess();

            rResult = select.getResult(1);
        } catch (SQLException e) {
            throw e;
        }

        v.setStatement(c.getLastPart());
        v.addColumns(rResult);
        session.commit();
        tTable.add(v);
        c.setPartMarker(logposition);
    }

    private void processRenameTable(Tokenizer c, Session session,
                                    String tablename) throws SQLException {

        String  newname  = c.getName();
        boolean isquoted = c.wasQuotedIdentifier();
        Table   t        = findUserTable(tablename);

        // this ensures temp table belongs to this session
        if (t == null ||!t.equals(tablename, session)) {
            throw Trace.error(Trace.TABLE_NOT_FOUND, tablename);
        }

        Table ttemp = findUserTable(newname);

        if (ttemp != null && ttemp.equals(ttemp.getName().name, session)) {
            throw Trace.error(Trace.TABLE_ALREADY_EXISTS, tablename);
        }

        session.commit();
        session.setScripting(!t.isTemp());
        t.setName(newname, isquoted);
    }

    /**
     * ALSTER TABLE statements.
     * ALTER TABLE <name> RENAME TO <newname>
     * ALTER INDEX <name> RENAME TO <newname>
     *
     * ALTER TABLE <name> ADD CONSTRAINT <constname> FOREIGN KEY (<col>, ...)
     * REFERENCE <other table> (<col>, ...) [ON DELETE CASCADE]
     *
     * ALTER TABLE <name> ADD CONSTRAINT <constname> UNIQUE (<col>, ...)
     *
     * @param  c
     * @param  session
     * @return  Result
     * @throws  SQLException
     */
    private Result processAlter(Tokenizer c,
                                Session session) throws SQLException {

        session.checkDDLWrite();
        session.checkAdmin();
        session.setScripting(true);

        String sToken = c.getString();

        switch (commandSet.get(sToken)) {

            case TABLE :
                processAlterTable(c, session);
                break;

            case INDEX :
                processAlterIndex(c, session);
                break;

            default :
                throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
        }

        return new Result();
    }

    private void processAlterTable(Tokenizer c,
                                   Session session) throws SQLException {

        String     tablename = c.getString();
        Table      t         = getUserTable(tablename, session);
        TableWorks tw        = new TableWorks(t);
        String     sToken    = c.getString();

        if (t.isView()) {
            throw Trace.error(Trace.NOT_A_TABLE);
        }

        session.setScripting(!t.isTemp());

        switch (commandSet.get(sToken)) {

            default :
                throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
            case RENAME :
                c.getThis("TO");
                processRenameTable(c, session, tablename);

                return;

            case ADD : {
                sToken = c.getString();

                switch (commandSet.get(sToken)) {

                    default :
                        throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
                    case CONSTRAINT :
                        HsqlName cname =
                            new HsqlName(c.getName(),
                                         c.wasQuotedIdentifier());

                        sToken = c.getString();

                        switch (commandSet.get(sToken)) {

                            default :
                                throw Trace.error(Trace.UNEXPECTED_TOKEN,
                                                  sToken);
                            case FOREIGN :
                                c.getThis("KEY");

                                TempConstraint tc =
                                    processCreateFK(c, session, t, cname);

                                t.checkColumnsMatch(tc.localCol, tc.expTable,
                                                    tc.expCol);

                                if (tc.deleteAction == Constraint
                                        .SET_DEFAULT || tc
                                        .deleteAction == Constraint
                                        .SET_NULL || tc
                                        .updateAction != Constraint
                                        .NO_ACTION) {
                                    throw Trace.error(
                                        Trace.FOREIGN_KEY_NOT_ALLOWED,
                                        "only ON UPDATE NO ACTION and ON DELETE CASCADE possible");
                                }

                                session.commit();
                                tw.createForeignKey(tc.localCol, tc.expCol,
                                                    tc.name, tc.expTable,
                                                    tc.deleteAction,
                                                    tc.updateAction);

                                return;

                            case UNIQUE :
                                int col[] = processColumnList(c, t);

                                session.commit();
                                tw.createUniqueConstraint(col, cname);

                                return;
                        }
                    case COLUMN :
                        int    colindex = t.getColumnCount();
                        Column column   = processCreateColumn(c, t);

                        sToken = c.getString();

                        if (sToken.equals("BEFORE")) {
                            sToken   = c.getName();
                            colindex = t.getColumnNr(sToken);
                        } else {
                            c.back();
                        }

                        if (column.isIdentity() || column.isPrimaryKey()
                                || (!t.isEmpty()
                                    && column.isNullable() == false
                                    && column.getDefaultString() == null)) {
                            throw Trace.error(
                                Trace.BAD_ADD_COLUMN_DEFINITION);
                        }

                        session.commit();
                        tw.addOrDropColumn(column, colindex, 1);

                        return;
                }
            }
            case DROP : {
                sToken = c.getString();

                switch (commandSet.get(sToken)) {

                    default :
                        throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
                    case CONSTRAINT :
                        String cname = c.getName();

                        session.commit();
                        tw.dropConstraint(cname);

                        return;

                    case COLUMN :
                        sToken = c.getName();

                        int colindex = t.getColumnNr(sToken);

                        session.commit();
                        tw.addOrDropColumn(null, colindex, -1);

                        return;
                }
            }
        }
    }

    private void processAlterIndex(Tokenizer c,
                                   Session session) throws SQLException {

        String indexname = c.getName();

        c.getThis("RENAME");
        c.getThis("TO");

        String  newname  = c.getName();
        boolean isQuoted = c.wasQuotedIdentifier();
        Table   t        = findUserTableForIndex(indexname, session);

        if (t == null) {
            throw Trace.error(Trace.INDEX_NOT_FOUND, indexname);
        }

        Table ttemp = findUserTableForIndex(newname, session);

        if (ttemp != null) {
            throw Trace.error(Trace.INDEX_ALREADY_EXISTS, indexname);
        }

        if (HsqlName.isReservedIndexName(indexname)) {
            throw Trace.error(Trace.SYSTEM_INDEX, indexname);
        }

        if (HsqlName.isReservedIndexName(newname)) {
            throw Trace.error(Trace.BAD_INDEX_CONSTRAINT_NAME, indexname);
        }

        session.setScripting(!t.isTemp());
        session.commit();
        t.getIndex(indexname).setName(newname, isQuoted);
        indexNameList.rename(indexname, newname);
    }

// fredt@users 20020221 - patch 1.7.0 chnaged IF EXISTS syntax
// new syntax DROP TABLE tablename IF EXISTS
// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)

    /**
     *  Method declaration
     *
     * @param  c
     * @param  session
     * @return
     * @throws  SQLException
     */
    private Result processDrop(Tokenizer c,
                               Session session) throws SQLException {

        session.checkReadWrite();
        session.checkAdmin();
        session.setScripting(true);

        String  sToken = c.getString();
        boolean isview = false;

        switch (commandSet.get(sToken)) {

            case VIEW :
                isview = true;    //fall thru
            case TABLE :
                String  tablename = c.getString();
                boolean dropmode  = false;

                if (tablename.equals("IF")) {
                    sToken = c.getString();

                    if (sToken.equals("EXISTS")) {
                        dropmode  = true;
                        tablename = c.getString();
                    } else if (sToken.equals("IF")) {
                        c.getThis("EXISTS");

                        dropmode = true;
                    } else {
                        c.back();
                    }
                } else {
                    sToken = c.getString();

                    if (sToken.equals("IF")) {
                        c.getThis("EXISTS");

                        dropmode = true;
                    } else {
                        c.back();
                    }
                }

                Table t = findUserTable(tablename, session);

                if (t != null &&!t.isTemp()) {
                    session.checkDDLWrite();
                }

                dropTable(tablename, dropmode, isview, session);
                break;

            case USER :
                session.checkDDLWrite();
                aAccess.dropUser(c.getStringToken());
                break;

            case TRIGGER :
                session.checkDDLWrite();
                dropTrigger(c.getString(), session);
                break;

            case INDEX :
                session.checkDDLWrite();

                String indexname = c.getName();

                dropIndex(indexname, session);
                break;

            default :
                throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
        }

        return new Result();
    }

    void dropIndex(String indexname, Session session) throws SQLException {

        Table t = findUserTableForIndex(indexname, session);

        if (t == null) {
            throw Trace.error(Trace.INDEX_NOT_FOUND, indexname);
        }

        t.checkDropIndex(indexname, null);

// fredt@users 20020405 - patch 1.7.0 by fredt - drop index bug
// see Table.moveDefinition();
        session.commit();
        session.setScripting(!t.isTemp());

        TableWorks tw = new TableWorks(t);

        tw.dropIndex(indexname);
    }

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)

    /**
     *  Responsible for handling the execution of GRANT and REVOKE SQL
     *  statements.
     *
     * @param  c
     * @param  session
     * @param  grant
     * @return  Description of the Return Value
     * @throws  SQLException
     */
    private Result processGrantOrRevoke(Tokenizer c, Session session,
                                        boolean grant) throws SQLException {

        session.checkDDLWrite();
        session.checkAdmin();

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
        session.setScripting(true);

        int    right = 0;
        String sToken;

        do {
            String sRight = c.getString();

            right  |= UserManager.getRight(sRight);
            sToken = c.getString();
        } while (sToken.equals(","));

        if (!sToken.equals("ON")) {
            throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
        }

        String namestring = c.getString();
        Object objectname = null;

        if (namestring.equals("CLASS")) {

            // object is saved as 'CLASS "java.lang.Math"'
            // tables like 'CLASS "xy"' should not be created
            objectname = c.getString();
        } else {

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
// to make sure the table exists
            Table t = getTable(namestring, session);

            objectname = t.getName();

            session.setScripting(!t.isTemp());
        }

        c.getThis("TO");

        String user = c.getStringToken();

        if (grant) {
            aAccess.grant(user, objectname, right);
        } else {
            aAccess.revoke(user, objectname, right);
        }

        return new Result();
    }

    /**
     *  Responsible for handling the execution CONNECT SQL statements
     *
     * @param  c
     * @param  session
     * @return
     * @throws  SQLException
     */
    private Result processConnect(Tokenizer c,
                                  Session session) throws SQLException {

        c.getThis("USER");

        String username = c.getStringToken();

        c.getThis("PASSWORD");

        String password = c.getStringToken();
        User   user     = aAccess.getUser(username, password);

        session.commit();
        session.setUser(user);

        return new Result();
    }

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)

    /**
     *  Responsible for handling the execution SET SQL statements
     *
     * @param  c
     * @param  session
     * @return
     * @throws  SQLException
     */
    private Result processSet(Tokenizer c,
                              Session session) throws SQLException {

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
        session.setScripting(true);

        String sToken = c.getString();

        switch (commandSet.get(sToken)) {

            case PROPERTY :
                session.checkAdmin();

                sToken = c.getString().toLowerCase();

                databaseProperties.setProperty(sToken, c.getString());
                databaseProperties.setDatabaseVariables();
                sToken = c.getString();
                break;

            case PASSWORD : {
                session.checkDDLWrite();
                session.setPassword(c.getStringToken());

                break;
            }
            case READONLY : {
                session.commit();
                session.setReadOnly(processTrueOrFalse(c));

                break;
            }
            case LOGSIZE : {
                session.checkAdmin();
                session.checkDDLWrite();

                int i = Integer.parseInt(c.getString());

                logger.setLogSize(i);

                break;
            }
            case LOGTYPE : {
                session.checkAdmin();
                session.checkDDLWrite();
                session.setScripting(false);

                int i = Integer.parseInt(c.getString());

                if (i == 0 || i == 1 || i == 3) {
                    logger.setLogType(i);
                }

                break;
            }
            case IGNORECASE : {
                session.checkAdmin();
                session.checkDDLWrite();

                bIgnoreCase = processTrueOrFalse(c);

                break;
            }
            case MAXROWS : {
                int i = Integer.parseInt(c.getString());

                session.setMaxRows(i);

                break;
            }
            case AUTOCOMMIT : {
                session.setAutoCommit(processTrueOrFalse(c));

                break;
            }
            case TABLE : {

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
// support for SET TABLE <table> READONLY [TRUE|FALSE]
// sqlbob@users 20020427 support for SET TABLE <table> SOURCE "file" [DESC]
                session.checkDDLWrite();

                Table t = getTable(c.getString(), session);

                sToken = c.getString();

                session.setScripting(!t.isTemp());

                switch (commandSet.get(sToken)) {

                    case SOURCE : {
                        if (!t.isTemp()) {
                            session.checkAdmin();
                        }

                        sToken = c.getString();

                        if (!c.wasQuotedIdentifier()) {
                            throw Trace.error(Trace.TEXT_TABLE_SOURCE);
                        }

                        boolean isDesc = false;

                        if (c.getString().equals("DESC")) {
                            isDesc = true;
                        } else {
                            c.back();
                        }

                        t.setDataSource(sToken, isDesc, session);

                        break;
                    }
                    case READONLY : {
                        session.checkAdmin();
                        t.setDataReadOnly(processTrueOrFalse(c));

                        break;
                    }
                    case INDEX : {
                        session.checkAdmin();
                        c.getString();
                        t.setIndexRoots((String) c.getAsValue());

                        break;
                    }
                    default : {
                        throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
                    }
                }

                break;
            }
            case REFERENTIAL_INTEGRITY : {
                session.checkAdmin();
                session.checkDDLWrite();

                bReferentialIntegrity = processTrueOrFalse(c);

                break;
            }
            case WRITE_DELAY : {
                session.checkAdmin();
                session.checkDDLWrite();

                int    delay = 0;
                String s     = c.getString();

                if (s.equals("TRUE")) {
                    delay = 60;
                } else if (s.equals("FALSE")) {
                    delay = 0;
                } else {
                    delay = Integer.parseInt(s);
                }

                logger.setWriteDelay(delay);

                break;
            }
            default : {
                throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
            }
        }

        return new Result();
    }

    /**
     *  Method declaration
     *
     * @param  c
     * @return
     * @throws  SQLException
     */
    private boolean processTrueOrFalse(Tokenizer c) throws SQLException {

        String sToken = c.getString();

        if (sToken.equals("TRUE")) {
            return true;
        } else if (sToken.equals("FALSE")) {
            return false;
        } else {
            throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
        }
    }

    /**
     *  Responsible for handling the execution COMMIT SQL statements
     *
     * @param  c
     * @param  session
     * @return
     * @throws  SQLException
     */
    private Result processCommit(Tokenizer c,
                                 Session session) throws SQLException {

        String sToken = c.getString();

        if (!sToken.equals("WORK")) {
            c.back();
        }

        session.commit();

        return new Result();
    }

    /**
     *  Responsible for handling the execution ROLLBACK SQL statementsn
     *
     * @param  c
     * @param  session
     * @return
     * @throws  SQLException
     */
    private Result processRollback(Tokenizer c,
                                   Session session) throws SQLException {

        String sToken = c.getString();

        if (sToken.equals("TO")) {
            String sToken1 = c.getString();

            if (!sToken1.equals("SAVEPOINT")) {
                throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken1);
            }

            sToken1 = c.getString();

            if (sToken1.length() == 0) {
                throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken1);
            }

            session.rollbackToSavepoint(sToken1);

            return new Result();
        }

        if (!sToken.equals("WORK")) {
            c.back();
        }

        session.rollback();

        return new Result();
    }

    /**
     *  Responsible for handling the execution of SAVEPOINT SQL statements.
     *
     * @param  c Description of the Parameter
     * @param  session Description of the Parameter
     * @return  Description of the Return Value
     * @throws  SQLException
     */
    private Result processSavepoint(Tokenizer c,
                                    Session session) throws SQLException {

        String sToken = c.getString();

        if (sToken.length() == 0) {
            throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
        }

        session.savepoint(sToken);

        return new Result();
    }

    /**
     *  Called by the garbage collector on this Databases object when garbage
     *  collection determines that there are no more references to it.
     */
    public void finalize() {

        try {
            close(-1);
        } catch (SQLException e) {    // it's too late now
        }
    }

    /**
     *  Method declaration
     *
     * @param  closemode Description of the Parameter
     * @throws  SQLException
     */
    private void close(int closemode) throws SQLException {

        logger.closeLog(closemode);

        // tony_lai@users 20020820
        // The database re-open and close has been moved from
        // Log#close(int closemode) for saving memory usage.
        // Doing so the instances of Log and other objects are no longer
        // referenced, and therefore can be garbage collected if necessary.
        if (closemode == 1) {
            open();
            logger.closeLog(0);
        }

        bShutdown = true;

        jdbcConnection.removeDatabase(this);
        logger.tryRelease();

        classLoader = null;
    }

    /**
     *  Responsible for handling the execution SHUTDOWN SQL statements
     *
     * @param  c
     * @param  session
     * @return
     * @throws  SQLException
     */
    private Result processShutdown(Tokenizer c,
                                   Session session) throws SQLException {

        if (!session.isClosed()) {
            session.checkAdmin();
        }

        int    closemode = 0;
        String token     = c.getString();

        // fredt - todo - catch misspelt qualifiers here and elsewhere
        if (token.equals("IMMEDIATELY")) {
            closemode = -1;
        } else if (token.equals("COMPACT")) {
            closemode = 1;
        } else {
            c.back();
        }

        sessionManager.closeAllSessions();
        sessionManager.clearAll();
        close(closemode);
        sessionManager.processDisconnect(session);

        return new Result();
    }

    /**
     *  Responsible for handling the parse and execution of CHECKPOINT SQL
     *  statements.
     *
     * @param  session
     * @return
     * @throws  SQLException
     */
    private Result processCheckpoint(Tokenizer c,
                                     Session session) throws SQLException {

        session.checkAdmin();
        session.checkDDLWrite();

        boolean defrag = false;
        String  token  = c.getString();

        // fredt - todo - catch misspelt qualifiers here and elsewhere
        if (token.equals("DEFRAG")) {
            defrag = true;
        }

        logger.checkpoint(defrag);

        return new Result();
    }

    /**
     * @param  ownerSession
     */
    void dropTempTables(Session ownerSession) {

        int i = tTable.size();

        while (i-- > 0) {
            Table toDrop = (Table) tTable.get(i);

            if (toDrop.isTemp()
                    && toDrop.getOwnerSessionId() != ownerSession.getId()) {
                tTable.remove(i);
            }
        }
    }

// fredt@users 20020221 - patch 521078 by boucherb@users - DROP TABLE checks
// avoid dropping tables referenced by foreign keys - also bug 451245
// additions by fredt@users
// remove redundant constrains on tables referenced by the dropped table
// avoid dropping even with referential integrity off

    /**
     *  Drops the specified user-defined view or table from this Database
     *  object. <p>
     *
     *  The process of dropping a table or view includes:
     *  <OL>
     *    <LI> checking that the specified Session's currently connected User
     *    has the right to perform this operation and refusing to proceed if
     *    not by throwing.
     *    <LI> checking for referential constraints that conflict with this
     *    operation and refusing to proceed if they exist by throwing.</LI>
     *
     *    <LI> removing the specified Table from this Database object.
     *    <LI> removing any exported foreign keys Constraint objects held by
     *    any tables referenced by the table to be dropped. This is especially
     *    important so that the dropped Table ceases to be referenced,
     *    eventually allowing its full garbage collection.
     *    <LI>
     *  </OL>
     *  <p>
     *
     *
     *
     * @param  name of the table or view to drop
     * @param  ifExists if true and if the Table to drop does not exist, fail
     *      silently, else throw
     * @param  isView true if the name argument refers to a View
     * @param  session the connected context in which to perform this
     *      operation
     * @throws  SQLException if any of the checks listed above fail
     */
    void dropTable(String name, boolean ifExists, boolean isView,
                   Session session) throws SQLException {

        Table       toDrop            = null;
        int         dropIndex         = -1;
        int         refererIndex      = -1;
        Enumeration constraints       = null;
        Constraint  currentConstraint = null;
        Table       refTable          = null;
        boolean     isRef             = false;
        boolean     isSelfRef         = false;

        for (int i = 0; i < tTable.size(); i++) {
            toDrop = (Table) tTable.get(i);

            if (toDrop.equals(name, session) && isView == toDrop.isView()) {
                dropIndex = i;

                break;
            } else {
                toDrop = null;
            }
        }

        if (dropIndex == -1) {
            if (ifExists) {
                return;
            } else {
                throw Trace.error(isView ? Trace.VIEW_NOT_FOUND
                                         : Trace.TABLE_NOT_FOUND, name);
            }
        }

        constraints = toDrop.getConstraints().elements();

        while (constraints.hasMoreElements()) {
            currentConstraint = (Constraint) constraints.nextElement();

            if (currentConstraint.getType() != Constraint.MAIN) {
                continue;
            }

            refTable  = currentConstraint.getRef();
            isRef     = (refTable != null);
            isSelfRef = (isRef && toDrop.equals(refTable));

            if (isRef &&!isSelfRef) {

                // cover the case where the referencing table
                // may have already been dropped
                for (int k = 0; k < tTable.size(); k++) {
                    if (refTable.equals(tTable.get(k))) {
                        refererIndex = k;

                        break;
                    }
                }

                if (refererIndex != -1) {

// tony_lai@users 20020820 - patch 595156
                    throw Trace.error(Trace.INTEGRITY_CONSTRAINT_VIOLATION,
                                      currentConstraint.getName().name
                                      + " table: " + refTable.getName().name);
                }
            }
        }

        tTable.remove(dropIndex);
        removeExportedKeys(toDrop);
        aAccess.removeDbObject(toDrop.getName());
        triggerNameList.removeOwner(toDrop.tableName);
        indexNameList.removeOwner(toDrop.tableName);
        toDrop.drop();
        session.setScripting(!toDrop.isTemp());
        session.commit();
    }

    /**
     *  Removes any foreign key Constraint objects (exported keys) held by any
     *  tables referenced by the specified table. <p>
     *
     *  This method is called as the last step of a successful call to
     *  dropTable() in order to ensure that the dropped Table ceases to be
     *  referenced when enforcing referential integrity.
     *
     * @param  toDrop The table to which other tables may be holding keys.
     *      This is typically a table that is in the process of being dropped.
     */
    void removeExportedKeys(Table toDrop) {

        for (int i = 0; i < tTable.size(); i++) {
            HsqlArrayList constraintvector =
                ((Table) tTable.get(i)).getConstraints();

            for (int j = constraintvector.size() - 1; j >= 0; j--) {
                Constraint currentConstraint =
                    (Constraint) constraintvector.get(j);
                Table refTable = currentConstraint.getRef();

                if (toDrop == refTable) {
                    constraintvector.remove(j);
                }
            }
        }
    }

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)

    /**
     *  Method declaration
     *
     * @param  name
     * @param  session
     * @throws  SQLException
     */
    private void dropTrigger(String name,
                             Session session) throws SQLException {

        boolean found = false;

        // look in each trigger list of each type of trigger for each table
        for (int i = 0, tsize = tTable.size(); i < tsize; i++) {
            Table t        = (Table) tTable.get(i);
            int   numTrigs = TriggerDef.numTrigs();

            for (int tv = 0; tv < numTrigs; tv++) {
                HsqlArrayList v = t.vTrigs[tv];

                for (int tr = v.size() - 1; tr >= 0; tr--) {
                    TriggerDef td = (TriggerDef) v.get(tr);

                    if (td.name.equals(name)) {

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
                        session.setScripting(!td.table.isTemp());
                        v.remove(tr);

                        found = true;

                        if (Trace.TRACE) {
                            Trace.trace("Trigger dropped " + name);
                        }
                    }
                }
            }
        }

        Trace.check(found, Trace.TRIGGER_NOT_FOUND, name);

// boucherb@users 20021128 - enforce unique trigger names
        triggerNameList.removeName(name);

// ---
    }

    /**
     * Ensures that under the correct conditions the system table producer's table
     * cache, if any, is set dirty so that up-to-date versions are generated if
     * necessary in response to following system table requests. <p>
     *
     * The result argument, if non-null, is checked for update status.  If it is an
     * update result with an update count, then the call must have come from a
     * successful SELECT INTO statement, in which a case a new table was created
     * and all system tables reporting in the tables, columns, indexes, etc are
     * dirty. <p>
     *
     * If the Result argument is null, then the call must have come from a DDL
     * statement other than a set statement, in wich case a database object
     * was created, dropped, altered or a permission was granted or revoked,
     * meaning that potentially all cached ssytem table are dirty.
     *
     * <B>Note:</B>  the SYSTEM _PARAMETERS is no longer cached by the full sytem table
     * producer implementation precisely becuase it takes very little time to
     * regenerate and because it is often the case the clients regularly issue
     * edundant SET statments, such as SET MAXROWS, otherwise seriously breaking the
     * caching effect that the full system table producer works so hard to maintain.
     *
     * If the database is opening, then this method does nothing, the rationale
     * being that no actual users will be querying system table content during the
     * open sequence of the database.  The system table producer implementation
     * must ensure that at least structurally correct system table surrogates
     * are available duringthe open sequence, as the may be DDL on the log the
     * references system tables, such as user view declarations.  However, it should
     * never be the case that system tables produced directly by the system table
     * producer are queried for content during the open sequence.
     * @param r A Result to test for update status, indicating the a table was created as the
     * result of executing a SELECT INTO statement.
     * @throws SQLException never
     *
     */
    private void setMetaDirty(Result r) throws SQLException {

        if (isOpening) {}
        else if (r == null
                 || (r.iMode == Result.UPDATECOUNT && r.iUpdateCount > 0)) {
            dInfo.setDirty();
        }
    }
}
