/* Copyright (c) 2001-2002, The HSQL Development Group
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


import java.io.File;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.util.Enumeration;

import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.HsqlHashSet;
import org.hsqldb.lib.HsqlHashMap;
import org.hsqldb.lib.StopWatch;
import org.hsqldb.lib.ValuePool;

import org.hsqldb.lib.enum.ArrayEnumeration;
import org.hsqldb.lib.enum.CompositeEnumeration;
import org.hsqldb.lib.enum.EmptyEnumeration;

// fredt@users - 1.7.2 - structural modifications to allow inheritance
// boucherb@users - 1.7.2 - 20020225  
// - factored out all reusable code into DIXXX support classes

/**
 * Produces tables which form a view of the system data dictionary.
 *
 * Implementations use a group of arrays of equal size to store various
 * attributes or cached instances of system tables.<p>
 *
 * Two fixed static lists of reserved table names are kept in String[] and
 * HsqlName[] forms. These are shared by all implementations of
 * DatabaseInformtion.<p>
 *
 * Each implementation keeps a lookup set of names for those tables whose
 * contents are never cached (nonCachedTablesSet).
 *
 * An instance of this class uses three lists named sysTablexxxx for caching
 * system tables.<p>
 *
 * sysTableSessionDependent indicates which tables contain data that is
 * dependent on the user rights of the User associatiod with the Session.<p>
 *
 * sysTableSessions contains the Session with whose rights each cached table
 * was built.<p>
 *
 * sysTables contains the cached tables.<p>
 *
 * At the time of instantiation, which is part of the Database.open() method
 * invocation, an empty table is created and placed in sysTables with calls to
 * generateTable(int) for each name in sysTableNames. Some of these
 * table entries are null as this implementation does not produce them.<p>
 *
 * Calls to getSystemTable(String, Session) return a cached table if various
 * caching rules are met (see below), or it will delete all rows of the table
 * and rebuild the contents via generateTable(int).<p>
 *
 * generateTable(int) calls the appropriate single method for each table.
 * These methods either build and return an empty table (if sysTables
 * contains null for the table slot) or populate the table with up-to-date
 * rows. <p>
 *
 * When the setDirty() call is made externally, the internal isDirty flag
 * is set. This flag is used next time a call to
 * getSystemTable(String, Session) is made.
 *
 * Rules for caching are applied as follows:
 *
 * When a call to getSystemTable(String, Session) is made, if the isDirty flag
 * is true, then the contents of all cached tables are cleared and the
 * sysTableUsers slot for all tables is set to null.
 *
 * If a table has non-cached contents, its contents are cleared and rebuilt.
 *
 * For the rest of the tables, if the sysTableSessions slot is null or if the
 * Session parameter is not the same as the Session object
 * in that slot, the table contents are cleared and rebuilt.
 *
 * (fredt@users)
 *
 * @author Campbell Boucher-Burnet, Camco & Associates Consulting
 * @version 1.7.2
 * @since HSQLDB 1.7.1
 */
class DatabaseInformationFull extends DatabaseInformationMain {


    /**
     * A <code>Result</code> object that holds the unchanging rows of
     * the <code>SYSTEM_PROPERTIES</code> table.
     */
    protected Result rsp;

    /** Provides SQL function/procedure reporting support.  */
    protected DIProcedureInfo pi;
    
    DatabaseInformationFull(Database db) throws SQLException {
        super(db);
        pi = new DIProcedureInfo(ns);
    }


    /** Retrieves the system table corresponding to the specified index.
     * @param tableIndex index identifying the system table to generate
     * @throws SQLException if a database access error occurs
     * @return the system table corresponding to the specified index
     */    
    protected Table generateTable(int tableIndex) throws SQLException {

        switch (tableIndex) {
            
            case SYSTEM_PROCEDURECOLUMNS:
                return SYSTEM_PROCEDURECOLUMNS();
                
            case SYSTEM_PROCEDURES:
                return SYSTEM_PROCEDURES();

            case SYSTEM_SUPERTABLES :
                return SYSTEM_SUPERTABLES();

            case SYSTEM_SUPERTYPES :
                return SYSTEM_SUPERTYPES();

            case SYSTEM_UDTATTRIBUTES :
                return SYSTEM_UDTATTRIBUTES();

            case SYSTEM_UDTS :
                return SYSTEM_UDTS();

            case SYSTEM_VERSIONCOLUMNS :
                return SYSTEM_VERSIONCOLUMNS();

            // HSQLDB-specific
            case SYSTEM_ALIASES :
                return SYSTEM_ALIASES();

            case SYSTEM_BYTECODE :
                return null;

            case SYSTEM_CACHEINFO :
                return SYSTEM_CACHEINFO();

            case SYSTEM_CLASSPRIVILEGES :
                return SYSTEM_CLASSPRIVILEGES();

            case SYSTEM_CONNECTIONINFO :
                return SYSTEM_CONNECTIONINFO();

            case SYSTEM_PROPERTIES :
                return SYSTEM_PROPERTIES();

            case SYSTEM_SESSIONS :
                return SYSTEM_SESSIONS();

            case SYSTEM_TRIGGERCOLUMNS :
                return SYSTEM_TRIGGERCOLUMNS();

            case SYSTEM_TRIGGERS :
                return SYSTEM_TRIGGERS();

            case SYSTEM_ALLTYPEINFO :
                return SYSTEM_ALLTYPEINFO();

            case SYSTEM_VIEWSOURCE :
                return SYSTEM_VIEWSOURCE();

            default :
                return super.generateTable(tableIndex);
        }
    }


    /** Retrieves a <code>Table</code> object describing the aliases defined
     * within this database <p>
     *
     * Currently two types of alias are reported: DOMAIN alaises ( alternate
     * names for column data types when issuing "CREATE TABLE" DDL) and
     * ROUTINE aliases (alternate names that can be used when invoking
     * routines as SQL functions or stored procedures). <P>
     *
     * Each row is an alias description with the following columns: <p>
     *
     * <pre>
     * OBJECT_TYPE  VARCHAR   type of the aliased object
     * OBJECT_CAT   VARCHAR   catalog of the aliased object
     * OBJECT_SCHEM VARCHAR   schema of the aliased object
     * OBJECT_NAME  VARCHAR   identifier of the aliased object
     * ALIAS_CAT    VARCHAR   catalog in which alias is defined
     * ALIAS_SCHEM  VARCHAR   schema in which alias is defined
     * ALIAS        VARCHAR   alias for the indicated object
     * </pre>
     *
     * <b>Note:</b> Up to and including HSQLDB 1.7.2, user-defined aliases
     * are supported only for SQL function and stored procedure calls
     * (indicated by the value "ROUTINE" in the <code>OBJECT_TYPE</code>
     * column), and there is no syntax for dropping aliases, only for
     * creating them. <p>
     * @return a Table object describing the accessisble
     *    aliases w.r.t the calling Session
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_ALIASES() throws SQLException {

        Table t = sysTables[SYSTEM_ALIASES];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_ALIASES]);

            addColumn(t, "OBJECT_TYPE", VARCHAR, false);    // not null
            addColumn(t, "OBJECT_CAT", VARCHAR);
            addColumn(t, "OBJECT_SCHEM", VARCHAR);
            addColumn(t, "OBJECT_NAME", VARCHAR, false);    // not null
            addColumn(t, "ALIAS_CAT", VARCHAR);
            addColumn(t, "ALIAS_SCHEM", VARCHAR);
            addColumn(t, "ALIAS", VARCHAR, false);          // not null
            // order: OBJECT_TYPE, OBJECT_NAME, ALIAS.  
            // true primary key. 
            t.createPrimaryKey(null, new int[]{0,3,6},true);

            return t;
        }

        // Holders for calculated column values
        String cat;
        String schem;
        String alias;
        String objName;
        String objType;

        // Intermediate holders 
        HsqlHashMap hAliases;
        Enumeration aliases;
        Object[]    row;
        int         pos;

        // Column number mappings
        final int ialias_object_type  = 0;
        final int ialias_object_cat   = 1;
        final int ialias_object_schem = 2;
        final int ialias_object_name  = 3;
        final int ialias_cat          = 4;
        final int ialias_schem        = 5;
        final int ialias              = 6;

        // Initialization        
        hAliases   = database.getAlias();
        aliases    = hAliases.keys();
        objType    = "ROUTINE";
        
        // Do it.
        while (aliases.hasMoreElements()) {
            row        = t.getNewRow();
            alias      = (String) aliases.nextElement();
            objName    = (String) hAliases.get(alias);

            // must have class grant to see method call aliases
            pos = objName.lastIndexOf('.');

            if (
                pos <= 0 || !session.isAccessible(objName.substring(0, pos))
            ) {
                continue;
            }
            
            cat = ns.getCatalogName(objName);
            schem = ns.getSchemaName(objName);

            row[ialias_object_type]  = objType;
            row[ialias_object_cat]   = cat;  
            row[ialias_object_schem] = schem;
            row[ialias_object_name]  = objName;
            row[ialias_cat]          = cat;
            row[ialias_schem]        = schem;
            row[ialias]              = alias;

            t.insert(row,session);
        }

        // must have create/alter table rights to see domain aliases
        if (session.isAdmin()) {
            aliases    = Column.hTypes.keys();
            objType    = "DOMAIN";

            while (aliases.hasMoreElements()) {
                row   = t.getNewRow();
                alias = (String) aliases.nextElement();

                Integer tn = (Integer) Column.hTypes.get(alias);
                objName    = Column.getTypeString(tn.intValue());

                if (alias.equals(objName)) {
                    continue;
                }
                
                cat = ns.getCatalogName(objName);
                schem = ns.getSchemaName(objName);

                row[ialias_object_type]  = objType;
                row[ialias_object_cat]   = cat;
                row[ialias_object_schem] = schem;
                row[ialias_object_name]  = objName;
                row[ialias_cat]          = cat;
                row[ialias_schem]        = schem;
                row[ialias]              = alias;

                t.insert(row, session);
            }
        }
        
        t.setDataReadOnly(true);

        return t;
    }

    /** Retrieves a <code>Table</code> object describing the current
     * state of all row caching objects for the accessible
     * tables defined within this database. <p>
     *
     * Currently, the row caching objects for which state is reported are: <p>
     *
     * <OL>
     * <LI> the system-wide <code>Cache</code> object used by CACHED tables.
     * <LI> any <code>TextCache</code> objects in use by [TEMP] TEXT tables.
     * </OL> <p>
     *
     * Each row is a cache object state description with the following
     * columns: <p>
     *
     * <pre>
     * CACHE_CLASS        VARCHAR   FQN of cache class
     * CACHE_FILE         VARCHAR   absolute path of cache file
     * CACHE_LENGTH       INTEGER   length row data array
     * CACHE_SIZE         INTEGER   number of rows currently cached
     * FREE_BYTES         INTEGER   size sum of available allocation units
     * SMALLEST_FREE_ITEM INTEGER   smallest allocation unit available
     * LARGEST_FREE_ITEM  INTEGER   largest allocation unit available
     * FREE_COUNT         INTEGER   total number of allocation units available
     * FREE_POS           INTEGER   largest file position allocated + 1
     * MAX_CACHE_SIZE     INTEGER   maximum allowable cached Row objects
     * MULTIPLIER_MASK    VARCHAR   binary mask for calc. row data array indices
     * WRITER_LENGTH      INTEGER   length of row write buffer array
     * </pre> <p>
     *
     * <b>Notes:</b> <p>
     *
     * <code>TextCache</code> objects do not maintain a free list because
     * deleted rows are only marked deleted and never reused. As such, the
     * columns FREE_BYTES, SMALLEST_FREE_ITEM, LARGEST_FREE_ITEM, and
     * FREE_COUNT are always reported as zero for rows reporting on
     * <code>TextCache</code> objects. <p>
     *
     * Currently, CACHE_SIZE, FREE_BYTES, SMALLEST_FREE_ITEM, LARGEST_FREE_ITEM,
     * FREE_COUNT and FREE_POS are the only dynamically changing values.
     * All others are constant for the life of a cache object. In a future
     * release, other column values may also change over the life of a cache
     * object, as SQL syntax may eventually be introduced to allow runtime
     * modification of certain cache properties. <p>
     * @return a description of the current state of all row caching objects associated
     * with the accessible tables of the database
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_CACHEINFO() throws SQLException {

        Table t = sysTables[SYSTEM_CACHEINFO];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_CACHEINFO]);

            addColumn(t, "CACHE_CLASS", VARCHAR, false);           // not null
            addColumn(t, "CACHE_FILE", VARCHAR);
            addColumn(t, "CACHE_LENGTH", INTEGER, false);          // not null
            addColumn(t, "CACHE_SIZE", INTEGER, false);            // not null
            addColumn(t, "FREE_BYTES", INTEGER, false);            // not null
            addColumn(t, "SMALLEST_FREE_ITEM", INTEGER, false);    // not null
            addColumn(t, "LARGEST_FREE_ITEM", INTEGER, false);     // not null
            addColumn(t, "FREE_COUNT", INTEGER, false);            // not null
            addColumn(t, "FREE_POS", INTEGER, false);              // not null
            addColumn(t, "MAX_CACHE_SIZE", INTEGER, false);        // not null
            addColumn(t, "MULTIPLIER_MASK", VARCHAR, false);       // not null
            addColumn(t, "WRITER_LENGTH", INTEGER, false);         // not null
            // order: CACHE_CLASS, CACHE_FILE
            t.createPrimaryKey(null, new int[]{0,1},false);

            return t;
        }

        Cache       cache;
        Object[]    row;
        HsqlHashSet cacheSet;
        Enumeration caches;
        Enumeration tables;
        Table       table;
        CacheFree   cacheFree;
        int         iFreeBytes;
        int         iLargestFreeItem;
        long        lSmallestFreeItem;

        // column number mappings
        final int icache_class        = 0;
        final int icache_file         = 1;
        final int icache_length       = 2;
        final int icache_size         = 3;
        final int ifree_bytes         = 4;
        final int is_free_item        = 5;
        final int il_free_item        = 6;
        final int ifree_count         = 7;
        final int ifree_pos           = 8;
        final int imax_cache_sz       = 9;
        final int imult_mask          = 10;
        final int iwriter_length      = 11;

        // Initialization
        cacheSet = new HsqlHashSet();
        // dynamic system tables are never cached
        tables   = database.getTables().elements(); 

        while (tables.hasMoreElements()) {
            table = (Table) tables.nextElement();

            if (table.isCached() && isAccessibleTable(table)) {
                cache = table.cCache;

                if (cache != null) {
                    cacheSet.add(cache);
                }
            }
        }

        caches = cacheSet.elements();

        // Do it.
        while (caches.hasMoreElements()) {
            cache             = (Cache) caches.nextElement();
            row               = t.getNewRow();
            cacheFree         = new CacheFree();
            iFreeBytes        = 0;
            iLargestFreeItem  = 0;
            lSmallestFreeItem = Long.MAX_VALUE;
            cacheFree.fNext   = cache.fRoot;

            while (cacheFree.fNext != null) {
                cacheFree  = cacheFree.fNext;
                iFreeBytes += cacheFree.iLength;

                if (cacheFree.iLength > iLargestFreeItem) {
                    iLargestFreeItem = cacheFree.iLength;
                }

                if (cacheFree.iLength < lSmallestFreeItem) {
                    lSmallestFreeItem = cacheFree.iLength;
                }
            }

            if (lSmallestFreeItem > Integer.MAX_VALUE) {
                lSmallestFreeItem = 0;
            }

            row[icache_class]   = cache.getClass().getName();
            row[icache_file]    = (new File(cache.sName)).getAbsolutePath();
            row[icache_length]  = ValuePool.getInt(cache.cacheLength);
            row[icache_size]    = ValuePool.getInt(cache.iCacheSize);
            row[ifree_bytes]    = ValuePool.getInt(iFreeBytes);
            row[is_free_item]   = ValuePool.getInt((int) lSmallestFreeItem);
            row[il_free_item]   = ValuePool.getInt(iLargestFreeItem);
            row[ifree_count]    = ValuePool.getInt(cache.iFreeCount);
            row[ifree_pos]      = ValuePool.getInt(cache.iFreePos);
            row[imax_cache_sz]  = ValuePool.getInt(cache.maxCacheSize);
            row[imult_mask]     = Integer.toHexString(cache.multiplierMask);
            row[iwriter_length] = ValuePool.getInt(cache.writerLength);

            t.insert(row,session);
        }

        t.setDataReadOnly(true);

        return t;
    }

    /** Retrieves a <code>Table</code> object describing the visible
     * access rights for all accessible Java Class objects defined
     * within this database.<p>
     *
     * Each row is a Class privilege description with the following columns: <p>
     *
     * <pre>
     * CLASS_CAT    VARCHAR   catalog in which the class is defined
     * CLASS_SCHEM  VARCHAR   schema in which the class is defined
     * CLASS_NAME   VARCHAR   fully qualified name of class
     * GRANTOR      VARCHAR   grantor of access
     * GRANTEE      VARCHAR   grantee of access
     * PRIVILEGE    VARCHAR   name of access
     * IS_GRANTABLE VARCHAR   grantable?: "YES" , "NO" , NULL (unknown)
     * </pre>
     *
     * <b>Note:</b> Users with the administrative privilege implicily have
     * full and unrestricted access to all Classes available to the database
     * class loader.  However, only explicitly granted rights are reported
     * in this table.  Explicit Class grants/revokes to admin users have no
     * effect in reality, but are reported in this table anyway.
     * @return a <code>Table</code> object describing the visible
     *        access rights for all accessible Java Class
     *        objects defined within this database
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_CLASSPRIVILEGES() throws SQLException {

        Table t = sysTables[SYSTEM_CLASSPRIVILEGES];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_CLASSPRIVILEGES]);

            addColumn(t, "CLASS_CAT", VARCHAR);
            addColumn(t, "CLASS_SCHEM", VARCHAR);
            addColumn(t, "CLASS_NAME", VARCHAR, false);      // not null
            addColumn(t, "GRANTOR", VARCHAR, false);         // not null
            addColumn(t, "GRANTEE", VARCHAR, false);         // not null
            addColumn(t, "PRIVILEGE", VARCHAR, false);       // not null
            addColumn(t, "IS_GRANTABLE", VARCHAR, false);    // not null
            t.createPrimaryKey(null, new int[] {2,4,5}, true);

            return t;
        }

        // calculated column values
        String clsCat;
        String clsSchem;
        String clsName;
        String grantorName;
        String granteeName;
        String privilege;
        String isGrantable;

        // secondary variables required to calculate column values and
        // produce a sorted set of rows for insertion into the output table
        UserManager   um;
        HsqlArrayList users;
        Enumeration   classNames;
        User          granteeUser;
        Object[]      row;

        // column number mappings
        final int icls_cat      = 0;
        final int icls_schem    = 1;
        final int icls_name     = 2;
        final int igrantor      = 3;
        final int igrantee      = 4;
        final int iprivilege    = 5;
        final int iis_grntbl    = 6;

        // Initialization
        
        grantorName  = UserManager.SYS_USER_NAME;
        um           = database.getUserManager();
        users        = um.listVisibleUsers(session, true);

        // Do it.
        for (int i = 0; i < users.size(); i++) {
            granteeUser = (User) users.get(i);
            granteeName = granteeUser.getName();
            isGrantable = granteeUser.isAdmin() ? "YES" : "NO";
            classNames  = granteeUser.getGrantedClassNames(false).elements();
            privilege   = "EXECUTE"; // TODO:  Check the SQL nnnn spec.

            while (classNames.hasMoreElements()) {
                clsName         = (String) classNames.nextElement();
                clsCat          = ns.getCatalogName(clsName);
                clsSchem        = ns.getSchemaName(clsName);
                row             = t.getNewRow();
                row[icls_cat]   = clsCat;
                row[icls_schem] = clsSchem;
                row[icls_name]  = clsName;
                row[igrantor]   = grantorName;
                row[igrantee]   = granteeName;
                row[iprivilege] = privilege;
                row[iis_grntbl] = isGrantable;
                
                t.insert(row,session);
            }

            classNames = ns.enumAccessibleTriggerClassNames(granteeUser);
            privilege  = "FIRE"; // TODO:  Check the SQL nnnn spec.

            while (classNames.hasMoreElements()) {
                clsName         = (String) classNames.nextElement();
                clsCat          = ns.getCatalogName(clsName);
                clsSchem        = ns.getSchemaName(clsName);
                row             = t.getNewRow();
                row[icls_cat]   = clsCat;
                row[icls_schem] = clsSchem;
                row[icls_name]  = clsName;
                row[igrantor]   = grantorName;
                row[igrantee]   = granteeName;
                row[iprivilege] = privilege;
                row[iis_grntbl] = isGrantable;

                t.insert(row,session);
            }
        }

        t.setDataReadOnly(true);

        return t;
    }

    /** Retrieves a <code>Table</code> object describing attributes
     * for the calling session.<p>
     *
     * The rows report the following {key,value} pairs:<p>
     *
     * <pre>
     * KEY (VARCHAR)       VALUE (VARCHAR)
     * ------------------- ---------------
     * SESSION_ID          the id of the this session
     * AUTOCOMMIT          YES: session is in autocommit mode, else NO
     * USER                the name of user connected in the specified session
     * (was READ_ONLY)
     * CONNECTION_READONLY TRUE: session is in read-only mode, else FALSE
     * (new)
     * DATABASE_READONLY   TRUE: database is in read-only mode, else FALSE
     * MAXROWS             the MAXROWS setting for the specified session
     * DATABASE            the name of the database
     * IDENTITY            the last identity value used by this session
     * </pre>
     *
     * <b>Note:</b>  This table <em>may</em> become deprecated in a future
     * release, as the information it reports now duplicates information
     * reported in the newer SYSTEM_SESSIONSS and SYSTEM_PROPERTIES
     * tables. <p>
     * @return a <code>Table</code> object describing the
     *        attributes of the connection associated
     *        with the current execution context
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_CONNECTIONINFO() throws SQLException {

        Table t = sysTables[SYSTEM_CONNECTIONINFO];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_CONNECTIONINFO]);

            addColumn(t, "KEY", VARCHAR, false);      // not null
            addColumn(t, "VALUE", VARCHAR, false);    // not null
            t.createPrimaryKey(null);

            return t;
        }

        Object[] row;

        row    = t.getNewRow();
        row[0] = "SESSION_ID";
        row[1] = String.valueOf(session.getId());

        t.insert(row, null);

        row    = t.getNewRow();
        row[0] = "AUTOCOMMIT";
        row[1] = session.getAutoCommit() ? "TRUE"
                                          : "FALSE";

        t.insert(row, null);

        row    = t.getNewRow();
        row[0] = "USER";
        row[1] = session.getUsername();

        t.insert(row, null);

        row    = t.getNewRow();
        row[0] = "CONNECTION_READONLY";
        row[1] = session.isReadOnly() ? "TRUE"
                                       : "FALSE";

        t.insert(row, null);

        row    = t.getNewRow();
        row[0] = "DATABASE_READONLY";
        row[1] = database.bReadOnly ? "TRUE"
                                    : "FALSE";

        t.insert(row, null);

        row    = t.getNewRow();
        row[0] = "MAXROWS";
        row[1] = String.valueOf(session.getMaxRows());

        t.insert(row, null);

        row    = t.getNewRow();
        row[0] = "DATABASE";
        row[1] = String.valueOf(database.getName());

        t.insert(row, null);

        row    = t.getNewRow();
        row[0] = "IDENTITY";
        row[1] = String.valueOf(session.getLastIdentity());

        t.insert(row, null);
        t.setDataReadOnly(true);

        return t;
    }

    /** Retrieves a <code>Table</code> object describing the capabilities
     * and operating parameter properties for the engine hosting this
     * database, as well as their applicability in terms of scope and
     * name space.
     *
     * Reported properties include all static JDBC <code>DatabaseMetaData</code>
     * capabilities values as well as certain <code>Database</code> object properties
     * and attribute values. <p>
     *
     * It is intended that all <code>Database</code> attributes and
     * properties that can be set via the database properties file,
     * JDBC connection properties or SQL SET/ALTER statements will
     * eventually be reported here. <p>
     *
     * Currently, in addition to the static JDBC <code>DatabaseMetaData</code>
     * capabilities values, the database properties reported are: <p>
     *
     * <OL>
     *     <LI>LOGSIZSE - # bytes to which REDO log grows before auto-checkpoint
     *     <LI>LOGTYPE - 0 : TEXT, 1 : BINARY
     *     <LI>WRITEDELAY - does REDO log currently use buffered write strategy?
     *     <LI>IGNORECASE - currently ignoring case in string comparisons?
     *     <LI>REFERENTIAL_INTEGITY - currently enforcing referential integrity?
     *     <LI>sql.month - TRUE: output range is 1..12, else 0..11
     *     <LI>sql.enforce_size - column length specifications enforced?
     *     <LI>sql.compare_in_locale - is JVM Locale used in collations?
     *     <LI>sql.strict_fk - TRUE: FK must reference pre-existing unique
     *     <LI>sql.strong_fk - TRUE: autogen referenced unique, else plain index
     *     <LI>hsqldb.cache_scale - base-2 exponent of row cache size
     *     <LI>hsqldb.gc_interval - # new records between forced gc ({0|NULL}=>never)
     * </OL> <p>
     *
     * <b>Notes:</b> <p>
     *
     * Since DatabaseMetaData return values are embedded in the jdbcDatabaseMetaData
     * class (assumption is driver version matches server version), the possibility
     * exists that a remote server has different DatabaseMetaData values than the
     * driver being used.  Rather than impose a great deal of overhead on the
     * engine and jdbcDatabaseMetaData classes by making all metadata calls query
     * the (possibly remote) engine directly, this table can be used to resolve such
     * differences.
     *
     * Also, as engine capabilities and supported properties/attributes are
     * added, this list will be expanded, if required, to include all relevant
     * additions supported at that time. <p>
     * @return table describing database and session operating parameters and capabilities
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_PROPERTIES() throws SQLException {

        Table t = sysTables[SYSTEM_PROPERTIES];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_PROPERTIES]);

            addColumn(t, "PROPERTY_SCOPE", VARCHAR, false);
            addColumn(t, "PROPERTY_NAMESPACE", VARCHAR, false);
            addColumn(t, "PROPERTY_NAME", VARCHAR, false);
            addColumn(t, "PROPERTY_VALUE", VARCHAR);
            addColumn(t, "PROPERTY_CLASS", VARCHAR, false);
            t.createPrimaryKey(null, new int[] {0,1,2}, true);

            return t;
        }

        // calculated column values
        String scope;
        String ns;
        String name;
        Object value;

        // secondary varaibles required to calculate column values and
        // produce a sorted set of rows for insertion into the output table
        DatabaseMetaData md;
        Method[]         methods;
        Class            returnType;
        Method           method;
        Object[]         emptyParms;
        Object[]         row;
        Result           r;
        HsqlProperties   props;

        // column number mappings
        final int iscope     = 0;
        final int ins = 1;
        final int iname      = 2;
        final int ivalue     = 3;
        final int iclass     = 4;

        // Do it:
        if (rsp == null) {

            // First, we want the names and values for
            // all JDBC capabilities constants
            scope             = "SESSION";
            ns                = "java.sql.DatabaseMetaData";
            md                = session.getInternalConnection().getMetaData();
            methods           = DatabaseMetaData.class.getMethods();
            emptyParms        = new Object[]{};
            rsp               = DITableInfo.createResultProto(t);

            for (int i = 0; i < methods.length; i++) {
                method     = methods[i];
                returnType = method.getReturnType();

                if (method.getParameterTypes().length > 0
                        ||!(returnType.isPrimitive() || String.class
                            .isAssignableFrom(returnType)) ||

                // not really a "property" of the database
                "getUserName".equals(method.getName())) {}
                else {
                    try {
                        name            = method.getName();
                        value           = method.invoke(md, emptyParms);
                        row             = t.getNewRow();
                        row[iscope]     = scope;
                        row[ins] = ns;
                        row[iname]      = name;
                        row[ivalue]     = String.valueOf(value);
                        row[iclass]     = returnType.getName();

                        rsp.add(row);
                    } catch (Exception e) {}
                }
            }

            props     = database.getProperties();
            ns = "database.properties";

            // sql.month
            row             = t.getNewRow();
            row[iscope]     = scope;
            row[ins] = ns;
            row[iname]      = "sql.month";
            row[ivalue]     = props.getProperty("sql.month");
            row[iclass]     = "boolean";

            rsp.add(row);

            // sql.enforce_size
            row             = t.getNewRow();
            row[iscope]     = scope;
            row[ins] = ns;
            row[iname]      = "sql.enforce_size";
            row[ivalue]     = props.getProperty("sql.enforce_size");
            row[iclass]     = "boolean";

            rsp.add(row);

            // sql.compare_in_locale
            row             = t.getNewRow();
            row[iscope]     = scope;
            row[ins] = ns;
            row[iname]      = "sql.compare_in_locale";
            row[ivalue]     = props.getProperty("sql.compare_in_locale");
            row[iclass]     = "boolean";

            rsp.add(row);

            // sql.strict_fk
            row             = t.getNewRow();
            row[iscope]     = scope;
            row[ins] = ns;
            row[iname]      = "sql.strict_fk";
            row[ivalue]     = props.getProperty("sql.strict_fk");
            row[iclass]     = "boolean";

            rsp.add(row);

            // sql.strong_fk
            row             = t.getNewRow();
            row[iscope]     = scope;
            row[ins] = ns;
            row[iname]      = "sql.strong_fk";
            row[ivalue]     = props.getProperty("sql.strong_fk");
            row[iclass]     = "boolean";

            rsp.add(row);

            // hsqldb.cache_scale
            row             = t.getNewRow();
            row[iscope]     = scope;
            row[ins] = ns;
            row[iname]      = "hsqldb.cache_scale";
            row[ivalue]     = props.getProperty("hsqldb.cache_scale");
            row[iclass]     = "int";

            rsp.add(row);

            // hsqldb.gc_interval
            row             = t.getNewRow();
            row[iscope]     = scope;
            row[ins] = ns;
            row[iname]      = "hsqldb.gc_interval";
            row[ivalue]     = props.getProperty("hsqldb.gc_interval");
            row[iclass]     = "int";

            rsp.add(row);
        }

        // Now get a snapshot of the properties that may change over
        // the lifetime of the session
        scope     = "TRANSACTION";
        ns        = "org.hsqldb.Database";
        r         = DITableInfo.createResultProto(t);

        // log size
        Log log     = database.logger.lLog;
        int logSize = (log == null) ? 0
                                    : log.maxLogSize * 1 << 20;

        row             = t.getNewRow();
        row[iscope]     = scope;
        row[ins]        = ns;
        row[iname]      = "LOGSIZE";
        row[ivalue]     = String.valueOf(logSize);
        row[iclass]     = "int";

        r.add(row);

        Integer logType = (log == null) ? null
                                        : ValuePool.getInt(log.logType);

        row             = t.getNewRow();
        row[iscope]     = scope;
        row[ins]        = ns;
        row[iname]      = "LOGTYPE";
        row[ivalue]     = logType == null ? null
                                          : String.valueOf(logType);
        row[iclass]     = "int";

        r.add(row);

        // write delay
        row = t.getNewRow();

        Integer writeDelay = (log == null) ? null
                                           : ValuePool.getInt(log.writeDelay);

        row[ins] = ns;
        row[iscope]     = scope;
        row[iname]      = "WRITE_DELAY";
        row[ivalue]     = String.valueOf(writeDelay);
        row[iclass]     = "int";

        r.add(row);

        // ignore case
        row             = t.getNewRow();
        row[iscope]     = scope;
        row[ins]        = ns;
        row[iname]      = "IGNORECASE";
        row[ivalue]     = String.valueOf(database.isIgnoreCase());
        row[iclass]     = "boolean";

        r.add(row);

        // referential integrity
        row             = t.getNewRow();
        row[iscope]     = scope;
        row[ins]        = ns;
        row[iname]      = "REFERENTIAL_INTEGRITY";
        row[ivalue]     = String.valueOf(database.isReferentialIntegrity());
        row[iclass]     = "boolean";

        r.add(row);    
        
        t.insert(rsp,session);
        t.insert(r, session);
        t.setDataReadOnly(true);

        return t;
    }

    /** Retrieves a <code>Table</code> object describing all visible
     * sessions. ADMIN users see *all* sessions, while non-admin users
     * see only their own session.<p>
     *
     * Each row is a session state description with the following columns: <p>
     *
     * <pre>
     * SESSION_ID         INTEGER   session identifier
     * CONNECTED          TIMESTAMP time at which session was created
     * USER_NAME          VARCHAR   db user name of current session user
     * IS_ADMIN           BIT       is session user an admin user?
     * AUTOCOMMIT         BIT       is session in autocommit mode?
     * READONLY           BIT       is session in read-only mode?
     * MAXROWS            INTEGER   session's MAXROWS setting
     * LAST_IDENTITY      INTEGER   last identity value used by this session
     * TRANSACTION_SIZE   INTEGER   # of undo items in current transaction
     * </pre>
     * @return a <code>Table</code> object describing all visible
     * sessions
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_SESSIONS() throws SQLException {

        Table t = sysTables[SYSTEM_SESSIONS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_SESSIONS]);

            addColumn(t, "SESSION_ID", INTEGER, false);
            addColumn(t, "CONNECTED", TIMESTAMP, false);
            addColumn(t, "USER_NAME", VARCHAR, false);
            addColumn(t, "IS_ADMIN", BIT, false);
            addColumn(t, "AUTOCOMMIT", BIT, false);
            addColumn(t, "READONLY", BIT, false);
            addColumn(t, "MAXROWS", INTEGER, false);
            // Note: some sessions may have a NULL LAST_IDENTITY value
            addColumn(t, "LAST_IDENTITY", BIGINT);
            addColumn(t, "TRANSACTION_SIZE", INTEGER, false);
            
            // order:  SESSION_ID
            // true primary key
            t.createPrimaryKey(null, new int[]{0}, true );

            return t;
        }

        // secondary variables required to calculate column values and
        // produce a sorted set of rows for insertion into the output table
        HsqlArrayList   sessions;
        Session         s;
        Object[]        row;

        // column number mappings
        final int isid        = 0;
        final int ict         = 1;
        final int iuname      = 2;
        final int iis_admin   = 3;
        final int iautocmt    = 4;
        final int ireadonly   = 5;
        final int imaxrows    = 6;
        final int ilast_id    = 7;
        final int it_size     = 8;

        // Initialisation
        sessions = ns.listVisibleSessions(session);

        // Do it.
        for (int i = 0; i < sessions.size(); i++) {            
            s              = (Session) sessions.get(i);
            
            row            = t.getNewRow();
            row[isid]      = ValuePool.getInt(s.getId());
            row[ict]       = new Timestamp(s.getConnectTime());
            row[iuname]    = s.getUsername();
            row[iis_admin] = ValuePool.getBoolean(s.isAdmin());
            row[iautocmt]  = ValuePool.getBoolean(s.getAutoCommit());
            row[ireadonly] = ValuePool.getBoolean(s.isReadOnly());
            row[imaxrows]  = ValuePool.getInt(s.getMaxRows());
            row[ilast_id]  = ValuePool.getLong((Number) s.getLastIdentity());
            row[it_size]   = ValuePool.getInt(s.getTransactionSize());

            t.insert(row,session);
        }

        t.setDataReadOnly(true);

        return t;
    }

    /** Retrieves a <code>Table</code> object describing the accessible
     * direct super table, if any, of each accessible table defined
     * within the this database. <p>
     *
     * Each row is a super table description with the following columns: <p>
     *
     * <pre>
     * TABLE_CAT       VARCHAR   the table's catalog
     * TABLE_SCHEM     VARCHAR   table schema
     * TABLE_NAME      VARCHAR   table name
     * SUPERTABLE_NAME VARCHAR   the direct super table's name
     * </pre>
     * @return a <code>Table</code> object describing the accessible
     *        direct supertable, if any, of each accessible
     *        table defined within the this databas
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_SUPERTABLES() throws SQLException {

        Table t = sysTables[SYSTEM_SUPERTABLES];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_SUPERTABLES]);

            addColumn(t, "TABLE_CAT", VARCHAR);
            addColumn(t, "TABLE_SCHEM", VARCHAR);
            addColumn(t, "TABLE_NAME", VARCHAR, false);      // not null
            addColumn(t, "SUPERTABLE_NAME", VARCHAR, false); // not null
            t.createPrimaryKey(null);

            return t;
        }

        t.setDataReadOnly(true);

        return t;
    }

    /** Retrieves a <code>Table</code> object describing the accessible
     * direct super type, if any, of each accessible user-defined type (UDT)
     * defined within this database. <p>
     *
     * Each row is a super type description with the following columns: <p>
     *
     * <pre>
     * TYPE_CAT        VARCHAR   the UDT's catalog
     * TYPE_SCHEM      VARCHAR   UDT's schema
     * TYPE_NAME       VARCHAR   type name of the UDT
     * SUPERTYPE_CAT   VARCHAR   the direct super type's catalog
     * SUPERTYPE_SCHEM VARCHAR   the direct super type's schema
     * SUPERTYPE_NAME  VARCHAR   the direct super type's name
     * </pre>
     * @return a <code>Table</code> object describing the accessible
     *        direct supertype, if any, of each accessible
     *        user-defined type (UDT) defined within this database
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_SUPERTYPES() throws SQLException {

        Table t = sysTables[SYSTEM_SUPERTYPES];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_SUPERTYPES]);

            addColumn(t, "TYPE_CAT", VARCHAR);
            addColumn(t, "TYPE_SCHEM", VARCHAR);
            addColumn(t, "TYPE_NAME", VARCHAR, false);      // not null
            addColumn(t, "SUPERTYPE_CAT", VARCHAR);
            addColumn(t, "SUPERTYPE_SCHEM", VARCHAR);
            addColumn(t, "SUPERTYPE_NAME", VARCHAR, false); // not null
            t.createPrimaryKey(null);

            return t;
        }

        t.setDataReadOnly(true);

        return t;
    }

    /** Retrieves a <code>Table</code> object describing of the usage
     * of accessible columns in accessible triggers defined within
     * the database. <p>
     *
     * Rows are ordered by TRIGGER_CAT, TRIGGER_SCHEM, TRIGGER_NAME, TABLE_CAT,
     * TABLE_SCHEM, TABLE_NAME, COLUMN_NAME, COLUMN_LIST, COLUMN_USAGE. <p>
     *
     * Each column usage description has the following columns: <p>
     *
     * <pre>
     * TRIGGER_CAT   VARCHAR   Trigger catalog
     * TRIGGER_SCHEM VARCHAR   Trigger schema
     * TRIGGER_NAME  VARCHAR   Trigger name
     * TABLE_CAT     VARCHAR   Catalog of table on which the trigger is defined
     * TABLE_SCHEM   VARCHAR   Schema of table on which the trigger is defined
     * TABLE_NAME    VARCHAR   Table on which the trigger is defined
     * COLUMN_NAME   VARCHAR   Name of the column used in the trigger
     * COLUMN_LIST   VARCHAR   Specified in UPDATE clause?: ("Y" | "N" }
     * COLUMN_USAGE  VARCHAR   { NEW | OLD | IN | OUT | IN OUT }
     * </pre>
     * @return a <code>Table</code> object describing of the usage
     *        of accessible columns in accessible triggers
     *        defined within the database
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_TRIGGERCOLUMNS() throws SQLException {

        Table t = sysTables[SYSTEM_TRIGGERCOLUMNS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_TRIGGERCOLUMNS]);

            addColumn(t, "TRIGGER_CAT", VARCHAR);
            addColumn(t, "TRIGGER_SCHEM", VARCHAR);
            addColumn(t, "TRIGGER_NAME", VARCHAR);
            addColumn(t, "TABLE_CAT", VARCHAR);
            addColumn(t, "TABLE_SCHEM", VARCHAR);
            addColumn(t, "TABLE_NAME", VARCHAR);
            addColumn(t, "COLUMN_NAME", VARCHAR);
            addColumn(t, "COLUMN_LIST", VARCHAR);
            addColumn(t, "COLUMN_USAGE", VARCHAR);
            // order:  all columns, in order, as each column
            // of each table may be listed under various 
            // capacities
            // false PK, as cat and schem may be null
            t.createPrimaryKey(null, new int[] {0,1,2,3,4,5,6,7,8}, false);
            // fast lookup by trigger ident
            addIndex(t, null, new int[]{1},false);
            addIndex(t, null, new int[]{2},false);
            // fast lookup by table ident
            addIndex(t, null, new int[]{3},false);
            addIndex(t, null, new int[]{4},false);
            addIndex(t, null, new int[]{5},false);

            return t;
        }
        
        java.sql.ResultSet rs;
        
        rs = session.getInternalConnection().createStatement().executeQuery(
        "select " + 
        "   a.TRIGGER_CAT, " +
        "   a.TRIGGER_SCHEM, " +
        "   a.TRIGGER_NAME, " +
        "   a.TABLE_CAT, " +
        "   a.TABLE_SCHEM, " +
        "   a.TABLE_NAME, " +
        "   b.COLUMN_NAME, " +
        "   'Y', " +
        "   'IN' " +
        "from " +
        "   SYSTEM_TRIGGERS a, " +
        "   SYSTEM_COLUMNS b " +
        " where " +
        "   a.TABLE_NAME = b.TABLE_NAME");
        
        t.insert(((jdbcResultSet)rs).rResult,session);
        t.setDataReadOnly(true);
        return t;
        
//        // calculated column values
//        String triggerCatalog;
//        String triggerSchema;
//        String triggerName;
//        String tableCatalog;
//        String tableSchema;
//        String tableName;
//        String columnName;
//        String columnList;
//        String columnUsage;
//
//        // secondary variables required to calculate column values and
//        // produce a sorted set of rows for insertion into an output table
//        Enumeration     tables;
//        Table           table;
//        HsqlArrayList[] vTrigs;
//        HsqlArrayList   triggerList;
//        TriggerDef      def;
//        Column          column;
//        int             columnCount;
//        Object          row[];
//        Result          r;
//
//        // column number mappings
//        final int itrigger_cat   = 0;
//        final int itrigger_schem = 1;
//        final int itrigger_name  = 2;
//        final int itable_cat     = 3;
//        final int itable_schem   = 4;
//        final int itable_name    = 5;
//        final int icolumn_name   = 6;
//        final int icolumn_list   = 7;
//        final int icolumn_usage  = 8;
//
//        // Initialization
//        // dynamic system tables can/must not easily have triggers
//        // they are disallowed because of this, so we consider only "user" 
//        // tables.  Note that there is no reason why
//        // static system tables can't exist in the user table list.
//        tables = userTableList.elements();
//
//        // currently, the only supported types
//        columnList  = "Y";
//        columnUsage = "IN";
//
//        // Do it.
//        while (tables.hasMoreElements()) {
//            table  = (Table) tables.nextElement();
//            vTrigs = table.vTrigs;
//
//            if (vTrigs == null) {
//                continue;
//            }
//
//            if (!isAccessibleTable(table)) {
//                continue;
//            }
//
//            tableCatalog   = ns.getCatalogName(table);
//            triggerCatalog = tableCatalog;
//            tableSchema    = ns.getSchemaName(table);
//            triggerSchema  = tableSchema;
//            tableName      = table.getName().name;
//            columnCount    = table.getColumnCount();
//
//            for (int i = 0; i < vTrigs.length; i++) {
//                triggerList = vTrigs[i];
//
//                if (triggerList == null) {
//                    continue;
//                }
//
//                for (int j = 0; j < triggerList.size(); j++) {
//                    def = (TriggerDef) triggerList.get(j);
//
//                    if (def == null) {
//                        continue;
//                    }
//
//                    triggerName = def.name.name;
//
//                    for (int k = 0; k < columnCount; k++) {
//                        column              = table.getColumn(k);
//                        columnName          = column.columnName.name;
//                        row                 = t.getNewRow();
//                        row[itrigger_cat]   = triggerCatalog;
//                        row[itrigger_schem] = triggerSchema;
//                        row[itrigger_name]  = triggerName;
//                        row[itable_cat]     = tableCatalog;
//                        row[itable_schem]   = tableSchema;
//                        row[itable_name]    = tableName;
//                        row[icolumn_name]   = columnName;
//                        row[icolumn_list]   = columnList;
//                        row[icolumn_usage]  = columnUsage;
//
//                        t.insert(row,session);
//                    }
//                }
//            }
//        }
//
//        t.setDataReadOnly(true);
//
//        return t;
    }

    /** Retrieves a <code>Table</code> object describing the accessible
     * triggers defined within the database. <p>
     *
     * Each row is a trigger description with the following columns: <p>
     *
     * <pre>
     * TRIGGER_CAT       VARCHAR   Trigger catalog
     * TRIGGER_SCHEM     VARCHAR   Trigger Schema
     * TRIGGER_NAME      VARCHAR   Trigger Name
     * TRIGGER_TYPE      VARCHAR   { [ BEFORE | AFTER ] STATEMENT | EACH ROW }
     * TRIGGERING_EVENT  VARCHAR   { INSERT | UPDATE | DELETE } )
     * TABLE_CAT         VARCHAR   Table's catalog 
     * TABLE_SCHEM       VARCHAR   Table's schema
     * BASE_OBJECT_TYPE  VARCHAR   TABLE ( future: VIEW, SCHEMA, DATABASE )
     * TABLE_NAME        VARCHAR   Table on which trigger is defined
     * COLUMN_NAME       VARCHAR   NULL (future: name of the nested table column)
     * REFERENCING_NAMES VARCHAR   ROW, OLD, NEW, etc.
     * WHEN_CLAUSE       VARCHAR   Condition causing trigger to fire (NULL: always)
     * STATUS            VARCHAR   { "ENABLED" | "DISABLED" }
     * DESCRIPTION       VARCHAR   typically, the trigger's DDL
     * ACTION_TYPE       VARCHAR   "CALL" (future: embedded language name)
     * TRIGGER_BODY      VARCHAR   Statement(s) executed
     * </pre>
     * @return a <code>Table</code> object describing the accessible
     * triggers defined within the database.
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_TRIGGERS() throws SQLException {

        Table t = sysTables[SYSTEM_TRIGGERS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_TRIGGERS]);

            addColumn(t, "TRIGGER_CAT", VARCHAR);
            addColumn(t, "TRIGGER_SCHEM", VARCHAR);
            addColumn(t, "TRIGGER_NAME", VARCHAR, false);
            addColumn(t, "TRIGGER_TYPE", VARCHAR, false);
            addColumn(t, "TRIGGERING_EVENT", VARCHAR, false);
            addColumn(t, "TABLE_CAT", VARCHAR);
            addColumn(t, "TABLE_SCHEM", VARCHAR);
            addColumn(t, "BASE_OBJECT_TYPE", VARCHAR, false);
            addColumn(t, "TABLE_NAME", VARCHAR, false);
            addColumn(t, "COLUMN_NAME", VARCHAR);
            addColumn(t, "REFERENCING_NAMES", VARCHAR, false);
            addColumn(t, "WHEN_CLAUSE", VARCHAR);
            addColumn(t, "STATUS", VARCHAR, false);
            addColumn(t, "DESCRIPTION", VARCHAR, false);
            addColumn(t, "ACTION_TYPE", VARCHAR, false);
            addColumn(t, "TRIGGER_BODY", VARCHAR, false);
            // order: TRIGGER_TYPE, TRIGGER_SCHEM, TRIGGER_NAME
            // false PK, as trigger_schem may be null
            t.createPrimaryKey(null, new int[]{3,1,2}, false);            
            // fast lookup by trigger ident
            addIndex(t, null, new int[]{0},false);
            addIndex(t, null, new int[]{1},false);
            addIndex(t, null, new int[]{2},false);
            // fast lookup by table ident
            addIndex(t, null, new int[]{5},false);
            addIndex(t, null, new int[]{6},false);
            addIndex(t, null, new int[]{8},false);

            return t;
        }

        // calculated column values
        String triggerCatalog;
        String triggerSchema;
        String triggerName;
        String triggerType;
        String triggeringEvent;
        String tableCatalog;
        String tableSchema;
        String baseObjectType;
        String tableName;
        String columnName;
        String referencingNames;
        String whenClause;
        String status;
        String description;
        String actionType;
        String triggerBody;

        // Intermediate holders
        Enumeration     tables;
        Table           table;
        HsqlArrayList[] vTrigs;
        HsqlArrayList   triggerList;
        TriggerDef      def;
        Object          row[];

        // column number mappings
        final int itrigger_cat       = 0;
        final int itrigger_schem     = 1;
        final int itrigger_name      = 2;
        final int itrigger_type      = 3;
        final int itriggering_event  = 4;
        final int itable_cat         = 5;
        final int itable_schem       = 6;
        final int ibase_object_type  = 7;
        final int itable_name        = 8;
        final int icolumn_name       = 9;
        final int ireferencing_names = 10;
        final int iwhen_clause       = 11;
        final int istatus            = 12;
        final int idescription       = 13;
        final int iaction_type       = 14;
        final int itrigger_body      = 15;

        // Initialization
        tables = database.getTables().elements();
        // these are the only values supported, currently
        actionType       = "CALL";
        baseObjectType   = "TABLE";
        columnName       = null;
        referencingNames = "row";
        whenClause       = null;

        // Do it.        
        while (tables.hasMoreElements()) {
            table  = (Table) tables.nextElement();
            vTrigs = table.vTrigs;

            if (vTrigs == null) {
                continue;
            }

            if (!isAccessibleTable(table)) {
                continue;
            }

            tableCatalog   = ns.getCatalogName(table);
            triggerCatalog = tableCatalog;
            tableSchema    = ns.getSchemaName(table);
            triggerSchema  = tableSchema;
            tableName      = table.getName().name;

            for (int i = 0; i < vTrigs.length; i++) {
                triggerList = vTrigs[i];

                if (triggerList == null) {
                    continue;
                }

                for (int j = 0; j < triggerList.size(); j++) {
                    def = (TriggerDef) triggerList.get(j);

                    if (def == null) {
                        continue;
                    }

                    triggerName = def.name.name;
                    description = def.toBuf().toString();
                    status      = def.valid ? "ENABLED" : "DISABLED";
                    triggerBody = def.fire;
                    triggerType = def.when;

                    if (def.forEachRow) {
                        triggerType += " EACH ROW";
                    }

                    triggeringEvent         = def.operation;
                    
                    row                     = t.getNewRow();
                    
                    row[itrigger_cat]       = triggerCatalog;
                    row[itrigger_schem]     = triggerSchema;
                    row[itrigger_name]      = triggerName;
                    row[itrigger_type]      = triggerType;
                    row[itriggering_event]  = triggeringEvent;
                    row[itable_cat]         = tableCatalog;
                    row[itable_schem]       = tableSchema;
                    row[ibase_object_type]  = baseObjectType;
                    row[itable_name]        = tableName;
                    row[icolumn_name]       = columnName;
                    row[ireferencing_names] = referencingNames;
                    row[iwhen_clause]       = whenClause;
                    row[istatus]            = status;
                    row[idescription]       = description;
                    row[iaction_type]       = actionType;
                    row[itrigger_body]      = triggerBody;

                    t.insert(row,session);
                }
            }
        }

        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the accessible
     * attrubutes of the accessible user-defined type (UDT) objects
     * defined within this database. <p>
     *
     * Rows are ordered by TYPE_SCHEM, TYPE_NAME and ORDINAL_POSITION.  <p>
     *
     * This description does not contain inherited attributes. <p>
     *
     * <B>Note:</B> Currently, neither the hsqldb engine nor the JDBC driver
     * support UDTs, so an empty table is returned.     
     *
     * @return a <code>Table</code> object describing the accessible
     *        attrubutes of the accessible user-defined type
     *        (UDT) objects defined within this database
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_UDTATTRIBUTES() throws SQLException {

        Table t = sysTables[SYSTEM_UDTATTRIBUTES];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_UDTATTRIBUTES]);

            addColumn(t, "TYPE_CAT", VARCHAR);
            addColumn(t, "TYPE_SCHEM", VARCHAR);
            addColumn(t, "TYPE_NAME", VARCHAR, false);           // not null
            addColumn(t, "ATTR_NAME", VARCHAR, false);           // not null
            addColumn(t, "DATA_TYPE", SMALLINT, false);          // not null
            addColumn(t, "ATTR_TYPE_NAME", VARCHAR, false);      // not null
            addColumn(t, "ATTR_SIZE", INTEGER);
            addColumn(t, "DECIMAL_DIGITS", INTEGER);
            addColumn(t, "NUM_PREC_RADIX", INTEGER);
            addColumn(t, "NULLABLE", INTEGER);
            addColumn(t, "REMARKS", VARCHAR);
            addColumn(t, "ATTR_DEF", VARCHAR);
            addColumn(t, "SQL_DATA_TYPE", INTEGER);
            addColumn(t, "SQL_DATETIME_SUB", INTEGER);
            addColumn(t, "CHAR_OCTET_LENGTH", INTEGER);
            addColumn(t, "ORDINAL_POSITION", INTEGER, false);    // not null
            addColumn(t, "IS_NULLABLE", VARCHAR, false);         // not null
            addColumn(t, "SCOPE_CATALOG", VARCHAR);
            addColumn(t, "SCOPE_SCHEMA", VARCHAR);
            addColumn(t, "SCOPE_TABLE", VARCHAR);
            addColumn(t, "SOURCE_DATA_TYPE", SMALLINT);
            t.createPrimaryKey(null);

            return t;
        }

        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the accessible
     * user-defined types defined in this database. <p>
     *
     * Schema-specific UDTs may have type JAVA_OBJECT, STRUCT, or DISTINCT.
     *
     * <P>Rows are ordered by DATA_TYPE, TYPE_SCHEM and TYPE_NAME.
     *
     * <P>Each row has the following columns:
     * <OL>
     *   <LI><B>TYPE_CAT</B> <code>VARCHAR</code> => the type's catalog
     *   <LI><B>TYPE_SCHEM</B> <code>VARCHAR</code> => type's schema
     *   <LI><B>TYPE_NAME</B> <code>VARCHAR</code> => type name
     *   <LI><B>CLASS_NAME</B> <code>VARCHAR</code> => Java class name
     *   <LI><B>DATA_TYPE</B> <code>VARCHAR</code> =>
     *         type value defined in <code>java.sql.Types</code>;
     *         one of <code>JAVA_OBJECT</code>, <code>STRUCT</code>, or
     *        <code>DISTINCT</code>
     *   <LI><B>REMARKS</B> <code>VARCHAR</code> =>
     *          explanatory comment on the type
     *   <LI><B>BASE_TYPE</B><code>SMALLINT</code> =>
     *          type code of the source type of a DISTINCT type or the
     *          type that implements the user-generated reference type of the
     *          SELF_REFERENCING_COLUMN of a structured type as defined in
     *          java.sql.Types (null if DATA_TYPE is not DISTINCT or not
     *          STRUCT with REFERENCE_GENERATION = USER_DEFINED)
     *
     * </OL> <p>
     *
     * <B>Note:</B> Currently, neither the hsqldb engine nor the JDBC driver
     * support UDTs, so an empty table is returned. <p>
     *
     * @return a <code>Table</code> object describing the accessible
     * user-defined types defined in this database
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_UDTS() throws SQLException {

        Table t = sysTables[SYSTEM_UDTS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_UDTS]);

            addColumn(t, "TYPE_CAT", VARCHAR);
            addColumn(t, "TYPE_SCHEM", VARCHAR);
            addColumn(t, "TYPE_NAME", VARCHAR, false);     // not null
            addColumn(t, "CLASS_NAME", VARCHAR, false);    // not null
            addColumn(t, "DATA_TYPE", VARCHAR, false);     // not null
            addColumn(t, "REMARKS", VARCHAR);
            addColumn(t, "BASE_TYPE ", SMALLINT);
            t.createPrimaryKey(null);

            return t;
        }

        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the
     * visible <code>Users</code> defined within this database.
     * @return table containing information about the users defined within
     *      this database
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_USERS() throws SQLException {

        Table t = sysTables[SYSTEM_USERS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_USERS]);

            addColumn(t, "USER", VARCHAR, false);
            addColumn(t, "ADMIN", BIT, false);
            // order: USER
            // true PK
            t.createPrimaryKey(null, new int[]{0}, true);

            return t;
        }

        // Intermediate holders
        HsqlArrayList users;
        User          user;
        int           userCount;
        Object[]      row;


        // Initialization
        users = database.getUserManager().listVisibleUsers(session, false);

        // Do it.
        for (int i = 0; i < users.size(); i++) {
            row     = t.getNewRow();
            user    = (User) users.get(i);
            row[0]  = user.getName();
            row[1]  = ValuePool.getBoolean(user.isAdmin());

            t.insert(row, null);
        }

        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the accessible
     * columns that are automatically updated when any value in a row
     * is updated. <p>
     *
     * Rows are unordered. <p>
     *
     * <P>Each column description has the following columns:
     * <OL>
     * <LI><B>SCOPE</B> <code>SMALLINT</code> => is not used
     * <LI><B>COLUMN_NAME</B> <code>VARCHAR</code> => column name
     * <LI><B>DATA_TYPE</B> <code>SMALLINT</code> =>
     *        SQL data type from java.sql.Types
     * <LI><B>TYPE_NAME</B> <code>SMALLINT</code> =>
     *       Data source dependent type name
     * <LI><B>COLUMN_SIZE</B> <code>INTEGER</code> => precision
     * <LI><B>BUFFER_LENGTH</B> <code>INTEGER</code> =>
     *        length of column value in bytes
     * <LI><B>DECIMAL_DIGITS</B> <code>SMALLINT</code> => scale
     * <LI><B>PSEUDO_COLUMN</B> <code>SMALLINT</code> =>
     *        is this a pseudo column like an Oracle <code>ROWID</code>:<BR>
     *        (as defined in <code>java.sql.DatabaseMetadata</code>)
     * <UL>
     *    <LI><code>versionColumnUnknown</code> - may or may not be
     *        pseudo column
     *    <LI><code>versionColumnNotPseudo</code> - is NOT a pseudo column
     *    <LI><code>versionColumnPseudo</code> - is a pseudo column
     * </UL>
     * </OL> <p>
     *
     * <B>Note:</B> Currently, the hsqldb engine does not support version 
     * columns, so an empty table is returned. <p>
     *
     * @return a <code>Table</code> object describing the columns
     *        that are automatically updated when any value
     *        in a row is updated
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_VERSIONCOLUMNS() throws SQLException {

        Table t = sysTables[SYSTEM_VERSIONCOLUMNS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_VERSIONCOLUMNS]);

            // ----------------------------------------------------------------
            // required by DatabaseMetaData.getVersionColumns result set
            // ----------------------------------------------------------------
            addColumn(t, "SCOPE", INTEGER);
            addColumn(t, "COLUMN_NAME", VARCHAR, false);       // not null
            addColumn(t, "DATA_TYPE", SMALLINT, false);        // not null
            addColumn(t, "TYPE_NAME", VARCHAR, false);         // not null
            addColumn(t, "COLUMN_SIZE", SMALLINT);
            addColumn(t, "BUFFER_LENGTH", INTEGER);
            addColumn(t, "DECIMAL_DIGITS", SMALLINT);
            addColumn(t, "PSEUDO_COLUMN", SMALLINT, false);    // not null

            // -----------------------------------------------------------------
            // required by DatabaseMetaData.getVersionColumns filter parameters
            // -----------------------------------------------------------------
            addColumn(t, "TABLE_CAT", VARCHAR);
            addColumn(t, "TABLE_SCHEM", VARCHAR);
            addColumn(t, "TABLE_NAME", VARCHAR, false);        // not null

            // -----------------------------------------------------------------
            t.createPrimaryKey(null);

            return t;
        }

        t.setDataReadOnly(true);

        return t;
    }    

    /**
     * Retrieves a <code>Table</code> object describing the DDL
     * source of all <code>View</code> objects accessible to
     * the current user. <p>
     *
     * @return a tabular description of the text source of all
     *        <code>View</code> objects accessible to
     *        the user.
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_VIEWSOURCE() throws SQLException {

        Table t = sysTables[SYSTEM_VIEWSOURCE];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_VIEWSOURCE]);

            addColumn(t, "VIEW_CAT", VARCHAR);
            addColumn(t, "VIEW_SCHEM", VARCHAR);
            addColumn(t, "VIEW_NAME", VARCHAR, true); // not null
            addColumn(t, "VIEW_SQL", VARCHAR, true); // not null
            addColumn(t, "VALID", BIT, true); // not null
            // order VIEW_NAME
            // VIEW_SCHEM added for unique
            // false PK, as VIEW_SCHEM may be null
            t.createPrimaryKey(null, new int[]{1,2}, false);
            // fast lookup by view ident
            addIndex(t, null, new int[]{0},false);
            addIndex(t, null, new int[]{2},false);

            return t;
        }
        
        Parser      parser;
        String      source;
        Select      select;
        Tokenizer   tokenizer;
        Enumeration tables;
        Table       table;
        Object[]    row;
        
        
        final int   iview_cat   = 0;
        final int   iview_schem = 1;
        final int   iview_name  = 2;
        final int   iview_sql   = 3;
        final int   ivalid      = 4;

        tables = database.getTables().elements();

        while (tables.hasMoreElements()) {
            table = (Table) tables.nextElement();

            if (!table.isView()) {
                continue;
            }

            if (!isAccessibleTable(table)) {
                continue;
            }
            
            row                 = t.getNewRow();
            source              = ((View) table).getStatement();
            
            row[iview_cat]      = ns.getCatalogName(table);
            row[iview_schem]    = ns.getSchemaName(table);
            row[iview_name]     = table.getName().name;
            row[iview_sql]      = source;
            
            try {                
                tokenizer = new Tokenizer(source);
                tokenizer.getThis("SELECT");
                parser = new Parser(database,tokenizer,session);
                select = parser.parseSelect();
                select.resolve();
                select.checkResolved();
                row[ivalid] = Boolean.TRUE;
            } catch (Exception e) {
                row[ivalid] = Boolean.FALSE;
            }
            
            t.insert(row,session);
        }

        t.setDataReadOnly(true);

        return t;
    }
    
    /** Retrieves a <code>Table</code> object describing the
     * return, parameter and result columns of the accessible
     * routines defined within this database.<p>
     *
     * Each row is a procedure column description with the following
     * columns: <p>
     *
     * <pre>
     * PROCEDURE_CAT   VARCHAR   procedure catalog
     * PROCEDURE_SCHEM VARCHAR   procedure schema
     * PROCEDURE_NAME  VARCHAR   procedure name
     * COLUMN_NAME     VARCHAR   column/parameter name
     * COLUMN_TYPE     SMALLINT  kind of column/parameter
     * DATA_TYPE       SMALLINT  SQL type from java.sql.Types
     * TYPE_NAME       VARCHAR   SQL type name
     * PRECISION       INTEGER   precision (length) of type
     * LENGTH          INTEGER   length--in bytes--of data
     * SCALE           SMALLINT  scale
     * RADIX           SMALLINT  radix
     * NULLABLE        SMALLINT  can column contain NULL?
     * REMARKS         VARCHAR   comment on { return value | parameter | result column }
     * </pre> <p>
     * @return a <code>Table</code> object describing the
     *        return, parameter and result columns
     *        of the accessible routines defined
     *        within this database.
     * @throws SQLException if an error occurs while producing the table
     *
     */
    Table SYSTEM_PROCEDURECOLUMNS() throws SQLException {
        
        Table t = super.SYSTEM_PROCEDURECOLUMNS();
        
        // calculated column values
        String  procedureCatalog;
        String  procedureSchema;
        String  procedureName;
        String  columnName;
        Integer columnType;
        Integer dataType;
        String  dataTypeName;
        Integer precision;
        Integer length;
        Integer scale;
        Integer radix;
        Integer nullability;
        String  remark;
        String  sig;
        
        // secondary variables required to calculate column values and
        // produce a sorted set of rows for insertion into an output table
        
        HsqlArrayList   aliasList;
        Object[]        info;
        Method          method;
        Enumeration     methods;
        Object[]        row;
        DITypeInfo      ti;
        
        // Initialization
        methods = ns.enumAllAccessibleMethods(session,true);// and aliases
        ti      = new DITypeInfo();
        
        ti.setTypeSub(TYPE_SUB_DEFAULT);
        
        // Do it.
        while (methods.hasMoreElements()) {
            info             = (Object[]) methods.nextElement();
            method           = (Method) info[0];
            aliasList        = (HsqlArrayList) info[1];
            
            procedureCatalog = ns.getCatalogName(method);
            procedureSchema  = ns.getSchemaName(method);
            
            pi.setMethod(method);
            
            sig              = pi.getSignature();
            procedureName    = pi.getFQN();
            
            for(int i = 0; i < pi.getColCount(); i++) {
                
                ti.setTypeCode(pi.getColTypeCode(i));
                
                columnName      = pi.getColName(i);
                columnType      = pi.getColUsage(i);
                dataType        = pi.getColDataType(i);
                dataTypeName    = ti.getTypeName();
                precision       = ti.getPrecision();
                length          = pi.getColLen(i);
                scale           = ti.getDefaultScale();
                radix           = ti.getNumPrecRadix();
                nullability     = pi.getColNullability(i);
                remark          = pi.getColRemark(i);
                
                addPColRows(t, aliasList, procedureCatalog,
                procedureSchema, procedureName, columnName,
                columnType, dataType, dataTypeName, precision,
                length, scale, radix, nullability, remark, sig,
                i);
            }
        }
        
        t.setDataReadOnly(true);
        
        return t;
    }
    
    /** Retrieves a <code>Table</code> object describing the accessible
     * routines defined within the this database.
     *
     * Each row is a procedure description with the following
     * columns: <p>
     *
     * <pre>
     * PROCEDURE_CAT     VARCHAR   catalog in which procedure is defined
     * PROCEDURE_SCHEM   VARCHAR   schema in which procedure is defined
     * PROCEDURE_NAME    VARCHAR   procedure identifier
     * NUM_INPUT_PARAMS  INTEGER   number of procedure input parameters
     * NUM_OUTPUT_PARAMS INTEGER   number of procedure output parameters
     * NUM_RESULT_SETS   INTEGER   number of result sets returned by procedure
     * REMARKS           VARCHAR   explanatory comment on the procedure
     * PROCEDURE_TYPE    SMALLINT  kind of procedure: { Unknown | No Result | Returns Result }
     * ORIGIN            VARCHAR   { ALIAS | ([BUILTIN | USER DEFINED] ROUTINE | TRIGGER | ...)}
     * SIGNATURE         VARCHAR   typically, but not restricted to a Java Method signature
     * </pre>
     * @return a <code>Table</code> object describing the accessible
     *        routines defined within the this database
     * @throws SQLException if an error occurs while producing the table
     *
     */
    Table SYSTEM_PROCEDURES() throws SQLException {
        
        Table t = super.SYSTEM_PROCEDURES();

        // calculated column values
        String  catalog;
        String  schema;
        String  procName;
        Integer numInputParams;
        Integer numOutputParams;
        Integer numResultSets;
        String  remarks;
        Integer procRType;
        
        // -------------------
        String  procOrigin;
        String  procSignature;
        
        // secondary variables required to calculate column values and
        // produce a sorted set of rows for insertion into an output table
        String        alias;
        HsqlArrayList aliasList;
        Enumeration   methods;
        Object[]      methodInfo;
        Method        method;
        String        methodOrigin;
        Object[]      row;
        
        // Initialization
        methods = ns.enumAllAccessibleMethods(session,true);//and aliases
        
        // Do it.
        while (methods.hasMoreElements()) {
            
            methodInfo    = (Object[]) methods.nextElement();
            method        = (Method) methodInfo[0];
            aliasList     = (HsqlArrayList) methodInfo[1];
            methodOrigin  = (String) methodInfo[2];
            
            pi.setMethod(method);
            
            catalog         = ns.getCatalogName(method);
            schema          = ns.getSchemaName(method);
            procName        = pi.getFQN();
            numInputParams  = pi.getInputParmCount();
            numOutputParams = pi.getOutputParmCount();
            numResultSets   = pi.getResultSetCount();
            remarks         = pi.getRemark();
            procRType       = pi.getResultType(methodOrigin);
            procOrigin      = pi.getOrigin(methodOrigin);
            procSignature   = pi.getSignature();
            
            addProcRows(t, aliasList, pi.getDeclaringClass(),
            methodOrigin, catalog, schema, procName,
            numInputParams, numOutputParams, numResultSets,
            remarks, procRType, procOrigin, procSignature);
        }
        
        t.setDataReadOnly(true);
        
        return t;
    }
    
    /** Inserts a set of procedure column description rows into the
     * <code>Table</code> specified by the <code>t</code> argument.
     * @param t the table in which the rows are to be inserted
     * @param l the list of procedure name aliases to which the
     *        specified column values apply
     * @param cat the procedure's catalog name
     * @param schem the procedure's schema name
     * @param pName the procedure's simple base (non-alias) name
     * @param cName the procedure column name
     * @param cType the column type (return, parameter, result)
     * @param dType the column's data type code
     * @param tName the column's canonical data type name
     * @param prec the column's precision
     * @param len the column's buffer length
     * @param scale the column's scale (decimal digits)
     * @param radix the column's numeric precision radix
     * @param nullability the column's java.sql.DatbaseMetaData
     *    nullabiliy code
     * @param remark a human-readable remark regarding the column
     * @param sig the signature of the procedure
     *    (typically but not limited to
     *    a java method signature)
     * @param seq helper value to allow JDBC contract order
     * @throws SQLException if there is problem inserting the specified rows
     *    in the table
     *
     */
    protected void addPColRows(Table t, HsqlArrayList l, String cat, String schem, String pName, String cName, Integer cType, Integer dType, String tName, Integer prec, Integer len, Integer scale, Integer radix, Integer nullability, String remark, String sig, int seq) throws SQLException {
        
        // column number mappings
        final int icat       = 0;
        final int ischem     = 1;
        final int iname      = 2;
        final int icol_name  = 3;
        final int icol_type  = 4;
        final int idata_type = 5;
        final int itype_name = 6;
        final int iprec      = 7;
        final int ilength    = 8;
        final int iscale     = 9;
        final int iradix     = 10;
        final int inullable  = 11;
        final int iremark    = 12;
        final int isig       = 13;
        final int iseq       = 14;
        
        Object[]  row        = t.getNewRow();
        
        row[icat]       = cat;
        row[ischem]     = schem;
        row[iname]      = pName;
        row[icol_name]  = cName;
        row[icol_type]  = cType;
        row[idata_type] = dType;
        row[itype_name] = tName;
        row[iprec]      = prec;
        row[ilength]    = len;
        row[iscale]     = scale;
        row[iradix]     = radix;
        row[inullable]  = nullability;
        row[iremark]    = remark;
        row[isig]       = sig;
        row[iseq]       = ValuePool.getInt(seq);
        
        t.insert(row,session);
        
        if (l != null) {
            int size = l.size();
            
            for (int i = 0; i < size; i++) {
                row             = t.getNewRow();
                pName           = (String) l.get(i);
                row[icat]       = cat;
                row[ischem]     = schem;
                row[iname]      = pName;
                row[icol_name]  = cName;
                row[icol_type]  = cType;
                row[idata_type] = dType;
                row[itype_name] = tName;
                row[iprec]      = prec;
                row[ilength]    = len;
                row[iscale]     = scale;
                row[iradix]     = radix;
                row[inullable]  = nullability;
                row[iremark]    = remark;
                row[isig]       = sig;
                row[iseq]       = ValuePool.getInt(seq);
                
                t.insert(row,session);
            }
        }
    }
    
    /** Inserts a set of procedure description rows into the <code>Table</code>
     * object specified by the <code>t</code> argument.
     * @param t the table into which the specified rows will eventually be inserted
     * @param l the list of procedure name aliases to which the specified column
     *    values apply
     * @param c the Java class, if any, that provides the procedure entry point
     * @param src a String indicating whether the non-aliased procedure is built-in or is
     *    visible via a user class grant
     * @param cat the procedure catalog name
     * @param schem the procedure schema name
     * @param pName the base (non-alias) procedure name
     * @param ip the procedure input parameter count
     * @param op the procedure output parameter count
     * @param rs the procedure result column count
     * @param remark a human-readable remark regarding the procedure
     * @param pType the procedure type code, indicating whether it is a
     *    function, procedure, or uncatagorized (i.e. returns
     *    a value, does not return a value, or it is unknown
     *    if it returns a value)
     * @param origin origin of the procedure, e.g.
     *    (callable) "ROUTINE", "TRIGGER",
     *    "ALIAS", etc.
     * @param sig the signature of the procedure
     *        (typically but not limited to a
     *        java method signature)
     * @throws SQLException if there is problem inserting the specified rows
     *    in the table
     *
     */
    protected void addProcRows(Table t, HsqlArrayList l, Class c, String src, String cat, String schem, String pName, Integer ip, Integer op, Integer rs, String remark, Integer pType, String origin, String sig) throws SQLException {
        
        // column number mappings
        final int icat          = 0;
        final int ischem        = 1;
        final int ipname        = 2;
        final int iinput_parms  = 3;
        final int ioutput_parms = 4;
        final int iresult_sets  = 5;
        final int iremark       = 6;
        final int iptype        = 7;
        final int iporigin      = 8;
        final int isig          = 9;
        
        Object[]  row           = t.getNewRow();
        
        row[icat]          = cat;
        row[ischem]        = schem;
        row[ipname]        = pName;
        row[iinput_parms]  = ip;
        row[ioutput_parms] = op;
        row[iresult_sets]  = rs;
        row[iremark]       = remark;
        row[iptype]        = pType;
        row[iporigin]      = origin;
        row[isig]          = sig;
        
        t.insert(row,session);
        
        if (l != null) {
            int size = l.size();
            
            for (int i = 0; i < size; i++) {
                
                row                = t.getNewRow();
                
                pName              = (String) l.get(i);
                row[icat]          = cat;
                row[ischem]        = schem;
                row[ipname]        = pName;
                row[iinput_parms]  = ip;
                row[ioutput_parms] = op;
                row[iresult_sets]  = rs;
                row[iremark]       = remark;
                row[iptype]        = pType;
                row[iporigin]      = "ALIAS";
                row[isig]          = sig;
                
                t.insert(row,session);
            }
        }
    }
    
}
