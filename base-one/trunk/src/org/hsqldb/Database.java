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

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.Iterator;
import org.hsqldb.lib.HashMap;
import org.hsqldb.lib.HashMappedList;
import org.hsqldb.HsqlNameManager.HsqlName;

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
// boucherb@users - doc 1.7.0 - added javadoc comments
// tony_lai@users 20020820 - patch 595073 - duplicated exception msg
// tony_lai@users 20020820 - changes to shutdown compact to save memory
// boucherb@users 20020828 - allow reconnect to local db that has shutdown
// fredt@users 20020912 - patch 1.7.1 by fredt - drop duplicate name triggers
// fredt@users 20021112 - patch 1.7.2 by Nitin Chauhan - use of switch
// rewrite of the majority of multiple if(){}else if(){} chains with switch()
// boucherb@users 20020310 - class loader update for JDK 1.1 compliance
// fredt@users 20030401 - patch 1.7.2 by akede@users - data files readonly
// fredt@users 20030401 - patch 1.7.2 by Brendan Ryan - data files in Jar
// boucherb@users 20030405 - removed 1.7.2 lint - updated JavaDocs
// boucherb@users 20030425 - DDL methods are moved to DatabaseCommandInterpreter.java
// boucherb@users - fredt@users 200305..200307 - patch 1.7.2 - DatabaseManager upgrade

/**
 *  Database is the root class for HSQL Database Engine database. <p>
 *
 *  Although it either directly or indirectly provides all or most of the
 *  services required for DBMS functionality, this class should not be used
 *  directly by an application. Instead, to achieve portability and
 *  generality, the JDBC interface classes should be used.
 *
 * @version  1.7.2
 */
class Database {

    int            databaseID;
    private String sType;
    private String sName;

// loosecannon1@users 1.7.2 patch properties on the JDBC URL
    private HsqlProperties urlProperties;
    private String         sPath;
    boolean                isNew;
    private UserManager    userManager;
    private HsqlArrayList  tTable;
    DatabaseInformation    dInfo;
    ClassLoader            classLoader;

    /** indicates the state of the database */
    private int dbState;
    Logger      logger;
    boolean     databaseReadOnly;    // all tables are readonly

// ----------------------------------------------------------------------------
// akede@users - 1.7.2 patch Files readonly

    /** true means that all file based table will automaticly become readonly */
    boolean filesReadOnly;           // cached tables are readonly

// ----------------------------------------------------------------------------
    boolean                        filesInJar;
    boolean                        sqlEnforceSize;
    boolean                        sqlEnforceStrictSize;
    int                            sqlMonth;
    int                            firstIdentity;
    private HashMap                hAlias;
    private boolean                bIgnoreCase;
    private boolean                bReferentialIntegrity;
    SessionManager                 sessionManager;
    private HsqlDatabaseProperties databaseProperties;
    HsqlNameManager                nameManager;
    DatabaseObjectNames            triggerNameList;
    DatabaseObjectNames            indexNameList;
    SequenceManager                sequenceManager;
    final static int               DATABASE_ONLINE       = 1;
    final static int               DATABASE_OPENING      = 4;
    final static int               DATABASE_CLOSING      = 8;
    final static int               DATABASE_SHUTDOWN     = 16;
    final static int               CLOSEMODE_IMMEDIATELY = -1;
    final static int               CLOSEMODE_NORMAL      = 0;
    final static int               CLOSEMODE_COMPACT     = 1;
    final static int               CLOSEMODE_SCRIPT      = 2;

    /**
     *  Constructs a new Database object.
     *
     * @param name is an identifier for the database, for future use
     * @param path is the canonical path to the database files
     * @param ifexists if true, prevents creation of a new database if it
     * does not exist. Only valid for file-system databases.
     * @param props property overrides placed on the connect URL
     * @exception  HsqlException if the specified name and path
     *      combination is illegal or unavailable, or the database files the
     *      name and path resolves to are in use by another process
     */

// loosecannon1@users 1.7.2 patch properties on the JDBC URL    
    Database(String type, String path, String name, boolean ifexists,
             HsqlProperties props) throws HsqlException {

        urlProperties = props;

        setState(Database.DATABASE_SHUTDOWN);

        sName = name;
        sType = type;
        sPath = path;

        if (sType == DatabaseManager.S_RES) {
            filesInJar    = true;
            filesReadOnly = true;
            ifexists      = true;
        }

        // does not need to be done more than once
        try {
            classLoader = getClass().getClassLoader();
        } catch (Exception e) {

            // strict security policy:  just use the system/boot loader
            classLoader = null;
        }

        try {
            isNew = (sType == DatabaseManager.S_MEM
                     ||!HsqlProperties.checkFileExists(path, isFilesInJar(),
                         getClass()));
        } catch (IOException e) {}

        if (isNew && ifexists) {
            throw Trace.error(Trace.DATABASE_NOT_EXISTS, type + path);
        }

        logger                   = new Logger();
        compiledStatementManager = new CompiledStatementManager(this);
    }

    /**
     * Opens this database.  The database should be opened after construction.
     *
     * @see #close(int closemode)
     * @throws HsqlException if a database access error occurs
     */
    synchronized void open() throws HsqlException {

        if (!isShutdown()) {
            return;
        }

        reopen();
    }

    /**
     * Opens this database.  The database should be opened after construction.
     * or reopened by the close(int closemode) method during a
     * "shutdown compact". Closes the log if there is an error.
     *
     * @throws HsqlException if a database access error occurs
     */
    void reopen() throws HsqlException {

        setState(DATABASE_OPENING);

        try {
            User sysUser;

            isNew = (sType == DatabaseManager.S_MEM
                     ||!HsqlProperties.checkFileExists(sPath, isFilesInJar(),
                         getClass()));
            databaseProperties = new HsqlDatabaseProperties(this);

            databaseProperties.load();

// loosecannon1@users 1.7.2 patch properties on the JDBC URL    
            // overload file database properties with any passed on URL line
            if (urlProperties != null) {
                for (Enumeration e = urlProperties.propertyNames();
                        e.hasMoreElements(); ) {
                    String propertyName = (String) e.nextElement();

                    databaseProperties.setProperty(
                        propertyName,
                        urlProperties.getProperty(propertyName));
                }
            }

            compiledStatementManager.reset();

            tTable                = new HsqlArrayList();
            userManager           = new UserManager();
            hAlias                = Library.getAliasMap();
            nameManager           = new HsqlNameManager();
            triggerNameList       = new DatabaseObjectNames();
            indexNameList         = new DatabaseObjectNames();
            sequenceManager       = new SequenceManager();
            bReferentialIntegrity = true;
            sysUser               = userManager.createSysUser(this);
            sessionManager        = new SessionManager(this, sysUser);
            dInfo = DatabaseInformation.newDatabaseInformation(this);

            if (sType != DatabaseManager.S_MEM) {
                logger.openLog(this);
            }

            if (isNew) {
                sessionManager.getSysSession().sqlExecuteDirectNoPreChecks(
                    "CREATE USER SA PASSWORD \"\" ADMIN");
            }

            dInfo.setWithContent(true);
        } catch (Throwable e) {
            logger.closeLog(Database.CLOSEMODE_IMMEDIATELY);
            logger.releaseLock();
            clearStructures();
            setState(DATABASE_SHUTDOWN);

            if (!(e instanceof HsqlException)) {
                e = Trace.error(Trace.GENERAL_ERROR, e.toString());
            }

            throw (HsqlException) e;
        }

        setState(DATABASE_ONLINE);
    }

    void clearStructures() {

        isNew           = false;
        tTable          = null;
        userManager     = null;
        hAlias          = null;
        nameManager     = null;
        triggerNameList = null;
        indexNameList   = null;
        sequenceManager = null;
        sessionManager  = null;
        dInfo           = null;
    }

    /**
     *  Name is a lowercase identifier.
     *
     * @return  this Database object's name
     */
    String getType() {
        return sType;
    }

    String getPath() {
        return sPath;
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
    synchronized boolean isShutdown() {
        return dbState == DATABASE_SHUTDOWN;
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
     * @throws  HsqlException if the specified user does not exist or a bad
     *      password is specified
     */
    synchronized Session connect(String username,
                                 String password) throws HsqlException {

        User user = userManager.getUser(username, password);
        Session session = sessionManager.newSession(this, user,
            databaseReadOnly);

        logger.logConnectUser(session, user.getName(), user.getPassword());

        return session;
    }

    /**
     *  Puts this Database object in global read-only mode. That is, after
     *  this call, all existing and future sessions are limited to read-only
     *  transactions. Any following attempts to update the state of the
     *  database will result in throwing an HsqlException.
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
     * normal Memory-Tables will <b>NOT</b> be stored or updated in the script
     * file.
     * This mode is special for all uses on read-only media but with the need
     * of using Temp-Tables for queries or not persistent changes.
     */
    void setFilesReadOnly() {
        filesReadOnly = true;
    }

    boolean isFilesReadOnly() {
        return filesReadOnly;
    }

// ----------------------------------------------------------------------------
    boolean isFilesInJar() {
        return filesInJar;
    }

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
        return userManager;
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
     * @return  a map in the form of a HashMap
     */
    HashMap getAlias() {
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
     *  Retrieves the specified user-defined table or view visible within the
     *  context of the specified Session, or any system table of the given
     *  name. This excludes any temp tables created in different Sessions.
     *
     * @param  name of the table or view to retrieve
     * @param  session the Session within which to search for user tables
     * @return  the user table or view, or system table
     * @throws  HsqlException if there is no such table or view
     */
    Table getTable(String name, Session session) throws HsqlException {

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
     * Retrieves the user table object with the specified
     * name from this datbase, using the specified session
     * context. In particular, this method will succeed iff
     * such a table exists <i>and</i> it is considered visible
     * by the specified session.
     *
     * @param name of the table to retrieve
     * @param session the retrieval context
     * @return the user table object with the specified
     *      name
     * @throws HsqlException if the user table object with the specified
     *      name cannot be found, given the specified
     *      session context
     */
    Table getUserTable(String name, Session session) throws HsqlException {

        Table t = findUserTable(name, session);

        if (t == null) {
            throw Trace.error(Trace.TABLE_NOT_FOUND, name);
        }

        return t;
    }

    /**
     * Retrieves the user table object with the specified
     * name from this datbase.
     *
     * @param name of the table to retrieve
     * @return the user table object with the specified
     *      name
     * @throws HsqlException if the user table object with the specified
     *      name cannot be found
     */
    Table getUserTable(String name) throws HsqlException {

        Table t = findUserTable(name);

        if (t == null) {
            throw Trace.error(Trace.TABLE_NOT_FOUND, name);
        }

        return t;
    }

    /**
     * Retrieves the user table object with the specified
     * name from this datbase.
     *
     * @param name of the table to retrieve
     * @return the user table object with the specified
     *  name, or null if not found
     */
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

    /**
     * Retrieves the user table object with the specified
     * name from this datbase, using the specified session
     * context. In particular, this method will succeed iff
     * such a table exists <i>and</i> it is considered visible
     * by the specified session.
     * @param name of the table to retrieve
     * @param session the retrieval context
     * @return the user table object with the specified
     *  name, or null if not found
     */
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
     * @throws  HsqlException if there is a problem
     */
    void linkTable(Table t) throws HsqlException {
        tTable.add(t);
    }

    /**
     * Setter for the isIgnoreCase attribute.
     * @param b the new value for the isIgnoreCase attribute
     */
    void setIgnoreCase(boolean b) {
        bIgnoreCase = b;
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
     * Finds the table that has an index with the given name in the
     * whole database and visible in this session.
     *
     * @param name of index
     * @param session visibility context
     * @return the table that encloses the index with the specific name,
     *      or null if the table or index are not visible in the specified
     *      session context
     */
    Table findUserTableForIndex(String name, Session session) {

        HsqlName hsqlname = indexNameList.getOwner(name);

        if (hsqlname == null) {
            return null;
        }

        return findUserTable(hsqlname.name, session);
    }

    /**
     *  Retrieves the index of a table or view in the HsqlArrayList that
     *  contains these objects for this Database.
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
     * Drops the index with the specified name from this database.
     * @param indexname the name of the index to drop
     * @param  ifExists if true and if the Index to drop does not exist, fail
     *      silently, else throw
     * @param session the execution context
     * @throws HsqlException if the index does not exist, the session lacks the permission
     *        or the operation violates database integrity
     */
    void dropIndex(String indexname, boolean ifExists,
                   Session session) throws HsqlException {

        Table t = findUserTableForIndex(indexname, session);

        if (t == null) {
            if (ifExists) {
                return;
            } else {
                throw Trace.error(Trace.INDEX_NOT_FOUND, indexname);
            }
        }

        t.checkDropIndex(indexname, null);

// fredt@users 20020405 - patch 1.7.0 by fredt - drop index bug
// see Table.moveDefinition();
        session.commit();
        session.setScripting(!t.isTemp());

        TableWorks tw = new TableWorks(t);

        tw.dropIndex(indexname);
    }

    /**
     *  Called by the garbage collector on this Databases object when garbage
     *  collection determines that there are no more references to it.
     */
    public void finalize() {

        if (Trace.TRACE) {
            Trace.trace(this + ".finalize(): state: " + getStateString());
        }

        if (getState() != DATABASE_ONLINE) {
            return;
        }

        try {
            close(CLOSEMODE_IMMEDIATELY);
        } catch (HsqlException e) {    // it's too late now
            if (Trace.TRACE) {
                Trace.trace(e.toString());
            }
        }
    }

    // tony_lai@users 20020820
    // The database re-open and close has been moved from
    // Log#close(int closemode) for saving memory usage.
    // Doing so the instances of Log and other objects are no longer
    // referenced, and therefore can be garbage collected if necessary.

    /**
     *  Closes this Database using the specified mode. <p>
     *
     * <ol>
     *  <LI> closemode -1 performs SHUTDOWN IMMEDIATELY, equivalent
     *       to  a poweroff or crash.
     *
     *  <LI> closemode 0 performs a normal SHUTDOWN that
     *      checkpoints the database normally.
     *
     *  <LI> closemode 1 performs a shutdown compact that scripts
     *       out the contents of any CACHED tables to the log then
     *       deletes the existing *.data file that contains the data
     *       for all CACHED table before the normal checkpoint process
     *       which in turn creates a new, compact *.data file.
     * </ol>
     *
     * @param  closemode which type of close to perform
     * @throws  HsqlException if a database access error occurs
     * @see Logger#closeLog(int)
     */
    void close(int closemode) throws HsqlException {

        HsqlException he = null;

        setState(DATABASE_CLOSING);
        sessionManager.closeAllSessions();
        sessionManager.clearAll();

        // fredt - impact of possible error conditions in closing the log
        // should be investigated for the CLOSEMODE_COMPACT mode
        logger.closeLog(closemode);

        try {
            if (closemode == CLOSEMODE_COMPACT &&!filesReadOnly) {
                reopen();
                setState(DATABASE_CLOSING);
                logger.closeLog(CLOSEMODE_NORMAL);
            }
        } catch (Throwable t) {
            if (t instanceof HsqlException) {
                he = (HsqlException) t;
            } else {
                he = Trace.error(Trace.GENERAL_ERROR, t.toString());
            }
        }

        classLoader = null;

        logger.releaseLock();
        clearStructures();
        setState(DATABASE_SHUTDOWN);

        // fredt - this should change to avoid removing a db from the
        // DatabaseManager repository if there are pending getDatabase()
        // calls
        DatabaseManager.removeDatabase(this);

        if (he != null) {
            throw he;
        }
    }

    /**
     * Drops from this Database any temporary tables owned by the specified
     * Session.
     *
     * @param  ownerSession the owning context
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
     * @param  name of the table or view to drop
     * @param  ifExists if true and if the Table to drop does not exist, fail
     *      silently, else throw
     * @param  isView true if the name argument refers to a View
     * @param  session the connected context in which to perform this
     *      operation
     * @throws  HsqlException if any of the checks listed above fail
     */
    void dropTable(String name, boolean ifExists, boolean isView,
                   Session session) throws HsqlException {

        Table toDrop    = null;
        int   dropIndex = -1;

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

        checkTableIsReferenced(toDrop);
        checkTableIsInView(toDrop);
        tTable.remove(dropIndex);
        removeExportedKeys(toDrop);
        userManager.removeDbObject(toDrop.getName());
        triggerNameList.removeOwner(toDrop.tableName);
        indexNameList.removeOwner(toDrop.tableName);
        toDrop.drop();
        session.setScripting(!toDrop.isTemp());
        session.commit();
    }

    /**
     * Throws if the table is referenced in a foreign key constraint.
     */
    private void checkTableIsReferenced(Table toDrop) throws HsqlException {

        Iterator   constraints       = toDrop.getConstraints().iterator();
        Constraint currentConstraint = null;
        Table      refTable          = null;
        boolean    isRef             = false;
        boolean    isSelfRef         = false;
        int        refererIndex      = -1;

        while (constraints.hasNext()) {
            currentConstraint = (Constraint) constraints.next();

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
                    throw Trace.error(Trace.TABLE_REFERENCED_CONSTRAINT,
                                      Trace.Database_dropTable, new Object[] {
                        currentConstraint.getName().name,
                        refTable.getName().name
                    });
                }
            }
        }
    }

    /**
     * Throws if the table is referenced in a view.
     */
    void checkTableIsInView(Table table) throws HsqlException {

        for (int i = 0; i < tTable.size(); i++) {
            Table t = (Table) tTable.get(i);

            if (t.isView()) {
                if (((View) t).hasTable(table)) {
                    throw Trace.error(Trace.TABLE_REFERENCED_VIEW,
                                      t.getName().name);
                }
            }
        }
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
     *  Drops a trigger with the specified name from this Database
     *
     * @param name of the trigger to drop
     * @param session execution context
     * @throws HsqlException if a database access error occurs
     */
    void dropTrigger(String name, Session session) throws HsqlException {

        boolean found = triggerNameList.containsName(name);

        Trace.check(found, Trace.TRIGGER_NOT_FOUND, name);

        HsqlName tableName = (HsqlName) triggerNameList.removeName(name);
        Table    t         = this.findUserTable(tableName.name, session);

        t.dropTrigger(name);
        session.setScripting(!t.isTemp());
    }

    /**
     * Ensures that under the correct conditions the system table producer's
     * table cache, if any, is set dirty. <p>
     *
     * This call is require to ensure that up-to-date versions are
     * generated if necessary in response to following system table
     * requests. <p>
     *
     * The result argument, if non-null, is checked for update status.
     * If it is an update result with an update count, then the call must
     * have come from a successful SELECT INTO statement, in which a case a
     * new table was created and all system tables reporting in the tables,
     * columns, indexes, etc. are dirty. <p>
     *
     * If the Result argument is null, then the call must have come from a DDL
     * statement other than a set statement, in which case a database object
     * was created, dropped, altered or a permission was granted or revoked,
     * meaning that potentially all cached ssytem table are dirty.
     *
     * @param r A Result to test for update status, indicating the a table
     *      was created as the result of executing a SELECT INTO statement.
     */
    void setMetaDirty(Result r) {

        if (r == null
                || (r.iMode == ResultConstants.UPDATECOUNT
                    && r.iUpdateCount > 0)) {
            nextDDLSCN();
            dInfo.setDirty();
        }
    }

// boucherb@users - patch 1.7.2 - system change number support
//
// NOTE: dml_scn sketched in but not used.  Will only be required
// when we start implementing various levels of read consistency.
// This will require either a change in the .data file format and
// changes to in-memory row representation, or changes only to
// in-memory row representation along with pinning all dependency
// rows in memory until dependent transactions are committed or
// rolled back.  Index scans will have to be changed to
// ignore rows that are not in transaction or statement scn
// scope (are not commited), and undo buffer items will have
// to be tagged for similar purposes.
// ---------------------------------
    CompiledStatementManager compiledStatementManager;
    private long             scn     = 0;
    private long             ddl_scn = 0;
    private long             dml_scn = 0;

    synchronized long getSCN() {
        return scn;
    }

    private synchronized void setSCN(long l) {
        scn = l;
    }

    private synchronized long nextSCN() {

        scn++;

        return scn;
    }

    synchronized long getDDLSCN() {
        return ddl_scn;
    }

    synchronized long getDMLSCN() {
        return dml_scn;
    }

    synchronized long nextDDLSCN() {

        ddl_scn = nextSCN();

        return ddl_scn;
    }

    synchronized long nextDMLSCN() {

        dml_scn = nextSCN();

        return dml_scn;
    }

    private synchronized void setState(int state) {
        dbState = state;
    }

    synchronized int getState() {
        return dbState;
    }

    String getStateString() {

        int state = getState();

        switch (state) {

            case DATABASE_CLOSING :
                return "DATABASE_CLOSING";

            case DATABASE_ONLINE :
                return "DATABASE_ONLINE";

            case DATABASE_OPENING :
                return "DATABASE_OPENING";

            case DATABASE_SHUTDOWN :
                return "DATABASE_SHUTDOWN";

            default :
                return "UNKNOWN";
        }
    }
}
