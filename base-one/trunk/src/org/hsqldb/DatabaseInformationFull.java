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
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.HashSet;
import org.hsqldb.lib.HashMap;
import org.hsqldb.lib.Iterator;
import org.hsqldb.store.ValuePool;

// fredt@users - 1.7.2 - structural modifications to allow inheritance
// boucherb@users - 1.7.2 - 20020225
// - factored out all reusable code into DIXXX support classes
// - completed Fred's work on allowing inheritance
// boucherb@users - 1.7.2 - 20020304 - bug fixes, refinements, better java docs

/**
 * Extends DatabaseInformationMain to provide additional system table
 * support. <p>
 *
 * @author boucherb@users.sourceforge.net
 * @version 1.7.2
 * @since HSQLDB 1.7.2
 */
final class DatabaseInformationFull extends DatabaseInformationMain {

    /** Provides SQL function/procedure reporting support. */
    protected DIProcedureInfo pi;

    /**
     * Constructs a new DatabaseInformationFull instance. <p>
     *
     * @param db the database for which to produce system tables.
     * @throws SQLException if a database access error occurs.
     */
    DatabaseInformationFull(Database db) throws SQLException {

        super(db);

        pi = new DIProcedureInfo(ns);
    }

    /**
     * Retrieves the system table corresponding to the specified index. <p>
     *
     * @param tableIndex index identifying the system table to generate
     * @throws SQLException if a database access error occurs
     * @return the system table corresponding to the specified index
     */
    protected Table generateTable(int tableIndex) throws SQLException {

        switch (tableIndex) {

            case SYSTEM_PROCEDURECOLUMNS :
                return SYSTEM_PROCEDURECOLUMNS();

            case SYSTEM_PROCEDURES :
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

            case SYSTEM_VIEWS :
                return SYSTEM_VIEWS();

            case SYSTEM_TEXTTABLES :
                return SYSTEM_TEXTTABLES();

            default :
                return super.generateTable(tableIndex);
        }
    }

    /**
     * Retrieves a <code>Table</code> object describing the aliases defined
     * within this database <p>
     *
     * Currently two types of alias are reported: DOMAIN alaises (alternate
     * names for column data types when issuing "CREATE TABLE" DDL) and
     * ROUTINE aliases (alternate names that can be used when invoking
     * routines as SQL functions or stored procedures). <p>
     *
     * Each row is an alias description with the following columns: <p>
     *
     * <pre>
     * OBJECT_TYPE  VARCHAR   type of the aliased object
     * OBJECT_CAT   VARCHAR   catalog of the aliased object
     * OBJECT_SCHEM VARCHAR   schema of the aliased object
     * OBJECT_NAME  VARCHAR   simple identifier of the aliased object
     * ALIAS_CAT    VARCHAR   catalog in which alias is defined
     * ALIAS_SCHEM  VARCHAR   schema in which alias is defined
     * ALIAS        VARCHAR   alias for the indicated object
     * </pre> <p>
     *
     * <b>Note:</b> Up to and including HSQLDB 1.7.2, user-defined aliases
     * are supported only for SQL function and stored procedure calls
     * (indicated by the value "ROUTINE" in the OBJECT_TYPE
     * column), and there is no syntax for dropping aliases, only for
     * creating them. <p>
     * @return a Table object describing the accessisble
     *      aliases in the context of the calling session
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
            // true PK.
            t.createPrimaryKey(null, new int[] {
                0, 3, 6
            }, true);

            return t;
        }

        // Holders for calculated column values
        String cat;
        String schem;
        String alias;
        String objName;
        String objType;

        // Intermediate holders
        HashMap  hAliases;
        Iterator aliases;
        Object[] row;
        int      pos;

        // Column number mappings
        final int ialias_object_type  = 0;
        final int ialias_object_cat   = 1;
        final int ialias_object_schem = 2;
        final int ialias_object_name  = 3;
        final int ialias_cat          = 4;
        final int ialias_schem        = 5;
        final int ialias              = 6;

        // Initialization
        hAliases = database.getAlias();
        aliases  = hAliases.keySet().iterator();
        objType  = "ROUTINE";

        // Do it.
        while (aliases.hasNext()) {
            row     = t.getNewRow();
            alias   = (String) aliases.next();
            objName = (String) hAliases.get(alias);

            // must have class grant to see method call aliases
            pos = objName.lastIndexOf('.');

            if (pos <= 0 ||!session.isAccessible(objName.substring(0, pos))) {
                continue;
            }

            cat                      = ns.getCatalogName(objName);
            schem                    = ns.getSchemaName(objName);
            row[ialias_object_type]  = objType;
            row[ialias_object_cat]   = cat;
            row[ialias_object_schem] = schem;
            row[ialias_object_name]  = objName;
            row[ialias_cat]          = cat;
            row[ialias_schem]        = schem;
            row[ialias]              = alias;

            t.insert(row, session);
        }

        // must have create/alter table rights to see domain aliases
        if (session.isAdmin()) {
            Iterator typeAliases = Column.hTypes.keySet().iterator();

            objType = "DOMAIN";

            while (typeAliases.hasNext()) {
                row   = t.getNewRow();
                alias = (String) typeAliases.next();

                Integer tn = (Integer) Column.hTypes.get(alias);

                objName = Column.getTypeString(tn.intValue());

                if (alias.equals(objName)) {
                    continue;
                }

                cat                      = ns.getCatalogName(objName);
                schem                    = ns.getSchemaName(objName);
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

// boucherb@users - 20020305 - added cache_hash column to cover the case
// of multiple text tables sharing single text file.  This is possible
// without file corruption under read-only text tables, but really should be
// disallowed under all other cases, else corruption is sure to result.

    /**
     * Retrieves a <code>Table</code> object describing the current
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
     * CACHE_CLASS        VARCHAR   FQN of Cache class
     * CACHE_HASH         INTEGER   in-memory hashCode() value of Cache object
     * CACHE_FILE         VARCHAR   absolute path of cache data file
     * CACHE_LENGTH       INTEGER   length of row data array
     * CACHE_SIZE         INTEGER   number of rows currently cached
     * FREE_BYTES         INTEGER   total bytes in available allocation units
     * SMALLEST_FREE_ITEM INTEGER   bytes of smallest available allocation unit
     * LARGEST_FREE_ITEM  INTEGER   bytes of largest available allocation unit
     * FREE_COUNT         INTEGER   total # of allocation units available
     * FREE_POS           INTEGER   largest file position allocated + 1
     * MAX_CACHE_SIZE     INTEGER   maximum allowable cached Row objects
     * MULTIPLIER_MASK    VARCHAR   binary mask for calc'n of row data indices
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
     *
     * @return a description of the current state of all row caching
     *      objects associated with the accessible tables of the database
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_CACHEINFO() throws SQLException {

        Table t = sysTables[SYSTEM_CACHEINFO];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_CACHEINFO]);

            addColumn(t, "CACHE_CLASS", VARCHAR, false);           // not null
            addColumn(t, "CACHE_HASH", INTEGER, false);            // not null
            addColumn(t, "CACHE_FILE", VARCHAR, false);            // not null
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
            // added for unique: CACHE_HASH
            // true PK
            t.createPrimaryKey(null, new int[] {
                0, 2, 1
            }, true);

            return t;
        }

        Cache     cache;
        Object[]  row;
        HashSet   cacheSet;
        Iterator  caches;
        Iterator  tables;
        Table     table;
        CacheFree cacheFree;
        int       iFreeBytes;
        int       iLargestFreeItem;
        long      lSmallestFreeItem;

        // column number mappings
        final int icache_class   = 0;
        final int icache_hash    = 1;
        final int icache_file    = 2;
        final int icache_length  = 3;
        final int icache_size    = 4;
        final int ifree_bytes    = 5;
        final int is_free_item   = 6;
        final int il_free_item   = 7;
        final int ifree_count    = 8;
        final int ifree_pos      = 9;
        final int imax_cache_sz  = 10;
        final int imult_mask     = 11;
        final int iwriter_length = 12;

        // Initialization
        cacheSet = new HashSet();

        // dynamic system tables are never cached
        tables = database.getTables().iterator();

        while (tables.hasNext()) {
            table = (Table) tables.next();

            if (table.isCached() && isAccessibleTable(table)) {
                cache = table.cCache;

                if (cache != null) {
                    cacheSet.add(cache);
                }
            }
        }

        caches = cacheSet.iterator();

        // Do it.
        while (caches.hasNext()) {
            cache             = (Cache) caches.next();
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
            row[icache_hash]    = ValuePool.getInt(cache.hashCode());
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

            t.insert(row, session);
        }

        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the visible
     * access rights for all accessible Java Class objects defined
     * within this database.<p>
     *
     * Each row is a Class privilege description with the following
     * columns: <p>
     *
     * <pre>
     * CLASS_CAT    VARCHAR   catalog in which the class is defined
     * CLASS_SCHEM  VARCHAR   schema in which the class is defined
     * CLASS_NAME   VARCHAR   fully qualified name of class
     * GRANTOR      VARCHAR   grantor of access
     * GRANTEE      VARCHAR   grantee of access
     * PRIVILEGE    VARCHAR   name of access: {"EXECUTE" | "TRIGGER"}
     * IS_GRANTABLE VARCHAR   grantable?: {"YES" | "NO" | NULL (unknown)}
     * </pre>
     *
     * <b>Note:</b> Users with the administrative privilege implicily have
     * full and unrestricted access to all Classes available to the database
     * class loader.  However, only explicitly granted rights are reported
     * in this table.  Explicit Class grants/revokes to admin users have no
     * effect in reality, but are reported in this table anyway for
     * completeness. <p>
     *
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
            t.createPrimaryKey(null, new int[] {
                2, 4, 5
            }, true);

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

        // intermediate holders
        UserManager   um;
        HsqlArrayList users;
        Iterator      classNames;
        User          granteeUser;
        Object[]      row;

        // column number mappings
        final int icls_cat   = 0;
        final int icls_schem = 1;
        final int icls_name  = 2;
        final int igrantor   = 3;
        final int igrantee   = 4;
        final int iprivilege = 5;
        final int iis_grntbl = 6;

        // Initialization
        grantorName = UserManager.SYS_USER_NAME;
        um          = database.getUserManager();
        users       = um.listVisibleUsers(session, true);

        // Do it.
        for (int i = 0; i < users.size(); i++) {
            granteeUser = (User) users.get(i);
            granteeName = granteeUser.getName();
            isGrantable = granteeUser.isAdmin() ? "YES"
                                                : "NO";
            classNames  = granteeUser.getGrantedClassNames(false).iterator();

// boucherb@users 20030305 - TODO completed.
// "EXECUTE" is closest to correct (from: SQL 200n ROUTINE_PRIVILEGES)
// There is nothing even like CLASS_PRIVILEGES table under SQL 200n spec.
            privilege = "EXECUTE";

            while (classNames.hasNext()) {
                clsName         = (String) classNames.next();
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

                t.insert(row, session);
            }

            classNames = ns.enumAccessibleTriggerClassNames(granteeUser);

// boucherb@users 20030305 - TODO completed.
// "TRIGGER" is closest to correct. (from: SQL 200n TABLE_PRIVILEGES)
// There is nothing even like CLASS_PRIVILEGES table under SQL 200n spec.
            privilege = "TRIGGER";

            while (classNames.hasNext()) {
                clsName         = (String) classNames.next();
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

                t.insert(row, session);
            }
        }

        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing attributes
     * for the calling session context.<p>
     *
     * The rows report the following {key,value} pairs:<p>
     *
     * <pre>
     * KEY (VARCHAR)       VALUE (VARCHAR)
     * ------------------- ---------------
     * SESSION_ID          the id of the calling session
     * AUTOCOMMIT          YES: session is in autocommit mode, else NO
     * USER                the name of user connected in the calling session
     * (was READ_ONLY)
     * CONNECTION_READONLY TRUE: session is in read-only mode, else FALSE
     * (new)
     * DATABASE_READONLY   TRUE: database is in read-only mode, else FALSE
     * MAXROWS             the MAXROWS setting in the calling session
     * DATABASE            the name of the database
     * IDENTITY            the last identity value used by calling session
     * </pre>
     *
     * <b>Note:</b>  This table <em>may</em> become deprecated in a future
     * release, as the information it reports now duplicates information
     * reported in the newer SYSTEM_SESSIONS and SYSTEM_PROPERTIES
     * tables. <p>
     *
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
        row[1] = database.databaseReadOnly ? "TRUE"
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

    /**
     * Retrieves a <code>Table</code> object describing the capabilities
     * and operating parameter properties for the engine hosting this
     * database, as well as their applicability in terms of scope and
     * name space. <p>
     *
     * Reported properties include all static JDBC <code>DatabaseMetaData</code>
     * capabilities values as well as certain <code>Database</code> object
     * properties and attribute values. <p>
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
     *     <LI>LOGTYPE - 0 : TEXT, 1 : BINARY, ...
     *     <LI>WRITEDELAY - does REDO log currently use buffered write strategy?
     *     <LI>IGNORECASE - currently ignoring case in character comparisons?
     *     <LI>REFERENTIAL_INTEGITY - currently enforcing referential integrity?
     *     <LI>sql.month - TRUE: output range is 1..12; FALSE: 0..11
     *     <LI>sql.enforce_size - column length specifications enforced?
     *     <LI>sql.compare_in_locale - is JVM Locale used in collations?
     *     <LI>sql.strict_fk - TRUE: FK must reference pre-existing unique
     *     <LI>sql.strong_fk - TRUE: autogen referenced unique, else plain index
     *     <LI>hsqldb.cache_scale - base-2 exponent of row cache size
     *     <LI>hsqldb.gc_interval - # new records forcing gc ({0|NULL}=>never)
     * </OL> <p>
     *
     * <b>Notes:</b> <p>
     *
     * Since DatabaseMetaData return values are embedded in the
     * jdbcDatabaseMetaData class (assumption is driver version matches server
     * version), the possibility exists that a remote server actually has
     * different DatabaseMetaData values than returned by the local driver
     * being used. Rather than impose a great deal of overhead on the engine
     * and jdbcDatabaseMetaData classes by making all metadata calls query
     * the (possibly remote) engine directly, this table can be used to
     * resolve such differences.
     *
     * Also, as engine capabilities and supported properties/attributes are
     * added, this list will be expanded, if required, to include all relevant
     * additions supported at that time. <p>
     *
     * @return table describing database and session operating parameters
     *      and capabilities
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

            // order PROPERTY_SCOPE, PROPERTY_NAMESPACE, PROPERTY_NAME
            // true PK
            t.createPrimaryKey(null, new int[] {
                0, 1, 2
            }, true);

            return t;
        }

        // calculated column values
        String scope;
        String nameSpace;
        String name;
        Object value;

        // intermediate holders
        DatabaseMetaData md;
        Method[]         methods;
        Class            returnType;
        Method           method;
        Object[]         emptyParms;
        Object[]         row;
        Result           r;
        HsqlProperties   props;

        // column number mappings
        final int iscope = 0;
        final int ins    = 1;
        final int iname  = 2;
        final int ivalue = 3;
        final int iclass = 4;

        // First, we want the names and values for
        // all JDBC capabilities constants
        scope      = "SESSION";
        nameSpace  = "java.sql.DatabaseMetaData";
        md         = session.getInternalConnection().getMetaData();
        methods    = DatabaseMetaData.class.getMethods();
        emptyParms = new Object[]{};

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
                    name        = method.getName();
                    value       = method.invoke(md, emptyParms);
                    row         = t.getNewRow();
                    row[iscope] = scope;
                    row[ins]    = nameSpace;
                    row[iname]  = name;
                    row[ivalue] = String.valueOf(value);
                    row[iclass] = returnType.getName();

                    t.insert(row, session);
                } catch (Exception e) {}
            }
        }

        props     = database.getProperties();
        nameSpace = "database.properties";

        // hsqldb.catalogs
        row         = t.getNewRow();
        row[iscope] = scope;
        row[ins]    = nameSpace;
        row[iname]  = "hsqldb.catalogs";
        row[ivalue] = props.getProperty("hsqldb.catalogs", "false");
        row[iclass] = "boolean";

        t.insert(row, session);

        // hsqldb.schemas
        row         = t.getNewRow();
        row[iscope] = scope;
        row[ins]    = nameSpace;
        row[iname]  = "hsqldb.schemas";
        row[ivalue] = props.getProperty("hsqldb.schemas", "false");
        row[iclass] = "boolean";

        t.insert(row, session);

        // sql.month
        row         = t.getNewRow();
        row[iscope] = scope;
        row[ins]    = nameSpace;
        row[iname]  = "sql.month";
        row[ivalue] = props.getProperty("sql.month", "false");
        row[iclass] = "boolean";

        t.insert(row, session);

        // sql.enforce_size
        row         = t.getNewRow();
        row[iscope] = scope;
        row[ins]    = nameSpace;
        row[iname]  = "sql.enforce_size";
        row[ivalue] = props.getProperty("sql.enforce_size", "false");
        row[iclass] = "boolean";

        t.insert(row, session);

        // sql.compare_in_locale
        row         = t.getNewRow();
        row[iscope] = scope;
        row[ins]    = nameSpace;
        row[iname]  = "sql.compare_in_locale";
        row[ivalue] = props.getProperty("sql.compare_in_locale", "false");
        row[iclass] = "boolean";

        t.insert(row, session);

        // hsqldb.files_readonly
        row         = t.getNewRow();
        row[iscope] = scope;
        row[ins]    = nameSpace;
        row[iname]  = "hsqldb.files_readonly";
        row[ivalue] = props.getProperty("hsqldb.files_readonly", "false");
        row[iclass] = "boolean";

        t.insert(row, session);

        // hsqldb.files_in_jar
        row         = t.getNewRow();
        row[iscope] = scope;
        row[ins]    = nameSpace;
        row[iname]  = "hsqldb.files_in_jar";
        row[ivalue] = props.getProperty("hsqldb.files_in_jar", "false");
        row[iclass] = "boolean";

        t.insert(row, session);

        // hsqldb.first_identity
        row         = t.getNewRow();
        row[iscope] = scope;
        row[ins]    = nameSpace;
        row[iname]  = "hsqldb.first_identity";
        row[ivalue] = props.getProperty("hsqldb.first_identity", "0");
        row[iclass] = "int";

        t.insert(row, session);

        // hsqldb.cache_scale
        row         = t.getNewRow();
        row[iscope] = scope;
        row[ins]    = nameSpace;
        row[iname]  = "hsqldb.cache_scale";
        row[ivalue] = props.getProperty("hsqldb.cache_scale");
        row[iclass] = "int";

        t.insert(row, session);

        // hsqldb.gc_interval
        row         = t.getNewRow();
        row[iscope] = scope;
        row[ins]    = nameSpace;
        row[iname]  = "hsqldb.gc_interval";
        row[ivalue] = props.getProperty("hsqldb.gc_interval", "0");
        row[iclass] = "int";

        t.insert(row, session);

        // Now get a snapshot of the properties that may change over
        // the lifetime of the session
        scope     = "TRANSACTION";
        nameSpace = "org.hsqldb.Database";

        // log size
        Log log     = database.logger.lLog;
        int logSize = (log == null) ? 0
                                    : log.maxLogSize * 1 << 20;

        row         = t.getNewRow();
        row[iscope] = scope;
        row[ins]    = nameSpace;
        row[iname]  = "LOGSIZE";
        row[ivalue] = String.valueOf(logSize);
        row[iclass] = "int";

        t.insert(row, session);

        Integer logType = (log == null) ? null
                                        : ValuePool.getInt(log.logType);

        row         = t.getNewRow();
        row[iscope] = scope;
        row[ins]    = nameSpace;
        row[iname]  = "LOGTYPE";
        row[ivalue] = logType == null ? null
                                      : String.valueOf(logType);
        row[iclass] = "int";

        t.insert(row, session);

        // write delay
        row = t.getNewRow();

        Integer writeDelay = (log == null) ? null
                                           : ValuePool.getInt(log.writeDelay);

        row[iscope] = scope;
        row[ins]    = nameSpace;
        row[iname]  = "WRITE_DELAY";
        row[ivalue] = (writeDelay == null) ? null
                                           : String.valueOf(writeDelay);
        row[iclass] = "int";

        t.insert(row, session);

        // ignore case
        row         = t.getNewRow();
        row[iscope] = scope;
        row[ins]    = nameSpace;
        row[iname]  = "IGNORECASE";
        row[ivalue] = String.valueOf(database.isIgnoreCase());
        row[iclass] = "boolean";

        t.insert(row, session);

        // referential integrity
        row         = t.getNewRow();
        row[iscope] = scope;
        row[ins]    = nameSpace;
        row[iname]  = "REFERENTIAL_INTEGRITY";
        row[ivalue] = String.valueOf(database.isReferentialIntegrity());
        row[iclass] = "boolean";

        t.insert(row, session);
        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing all visible
     * sessions. ADMIN users see *all* sessions, including the SYS session,
     * while non-admin users see only their own session.<p>
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
     * </pre> <p>
     *
     * @return a <code>Table</code> object describing all visible
     *      sessions
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
            t.createPrimaryKey(null, new int[]{ 0 }, true);

            return t;
        }

        // intermediate holders
        HsqlArrayList sessions;
        Session       s;
        Object[]      row;

        // column number mappings
        final int isid      = 0;
        final int ict       = 1;
        final int iuname    = 2;
        final int iis_admin = 3;
        final int iautocmt  = 4;
        final int ireadonly = 5;
        final int imaxrows  = 6;
        final int ilast_id  = 7;
        final int it_size   = 8;

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
            row[ilast_id]  = ValuePool.getLong(s.getLastIdentity().longValue());
            row[it_size]   = ValuePool.getInt(s.getTransactionSize());

            t.insert(row, session);
        }

        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the accessible
     * direct super table (if any) of each accessible table defined
     * within this database. <p>
     *
     * Each row is a super table description with the following columns: <p>
     *
     * <pre>
     * TABLE_CAT       VARCHAR   the table's catalog
     * TABLE_SCHEM     VARCHAR   table schema
     * TABLE_NAME      VARCHAR   table name
     * SUPERTABLE_NAME VARCHAR   the direct super table's name
     * </pre> <p>
     * @return a <code>Table</code> object describing the accessible
     *        direct supertable (if any) of each accessible
     *        table defined within this database
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_SUPERTABLES() throws SQLException {

        Table t = sysTables[SYSTEM_SUPERTABLES];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_SUPERTABLES]);

            addColumn(t, "TABLE_CAT", VARCHAR);
            addColumn(t, "TABLE_SCHEM", VARCHAR);
            addColumn(t, "TABLE_NAME", VARCHAR, false);         // not null
            addColumn(t, "SUPERTABLE_NAME", VARCHAR, false);    // not null
            t.createPrimaryKey(null);

            return t;
        }

        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the accessible
     * direct super type (if any) of each accessible user-defined type (UDT)
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
     * </pre> <p>
     * @return a <code>Table</code> object describing the accessible
     *        direct supertype (if any) of each accessible
     *        user-defined type (UDT) defined within this database
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_SUPERTYPES() throws SQLException {

        Table t = sysTables[SYSTEM_SUPERTYPES];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_SUPERTYPES]);

            addColumn(t, "TYPE_CAT", VARCHAR);
            addColumn(t, "TYPE_SCHEM", VARCHAR);
            addColumn(t, "TYPE_NAME", VARCHAR, false);         // not null
            addColumn(t, "SUPERTYPE_CAT", VARCHAR);
            addColumn(t, "SUPERTYPE_SCHEM", VARCHAR);
            addColumn(t, "SUPERTYPE_NAME", VARCHAR, false);    // not null
            t.createPrimaryKey(null);

            return t;
        }

        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the TEXT TABLE objects
     * defined within this database. The table contains one row for each row
     * in the SYSTEM_TABLES table with a HSQLDB_TYPE of ’TEXT’. <p>
     *
     * Each row is a description of the attributes that defines its TEXT TABLE,
     * with the following columns:
     *
     * <pre>
     * TABLE_CAT                 VARCHAR   table's catalog name
     * TABLE_SCHEM               VARCHAR   table's simple schema name
     * TABLE_NAME                VARCHAR   table's simple name
     * DATA_SOURCE_DEFINITION    VARCHAR   the "spec" proption of the table's
     *                                     SET TABLE ... SOURCE DDL declaration
     * FILE_PATH                 VARCHAR   absolute file path.
     * FILE_ENCODING             VARCHAR   endcoding of table's text file
     * FIELD_SEPARATOR           VARCHAR   default field separator
     * VARCHAR_SEPARATOR         VARCAHR   varchar field separator
     * LONGVARCHAR_SEPARATOR     VARCHAR   longvarchar field separator
     * IS_IGNORE_FIRST           BIT       ignores first line of file?
     * IS_ALL_QUOTED             BIT       every field is quoted?
     * IS_DESC                   BIT       read rows starting at end of file?
     * </pre> <p>
     *
     * @return a <code>Table</code> object describing the text attributes
     * of the accessible text tables defined within this database
     * @throws SQLException if an error occurs while producing the table
     *
     */
    final Table SYSTEM_TEXTTABLES() throws SQLException {

        Table t = sysTables[SYSTEM_TEXTTABLES];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_TEXTTABLES]);

            addColumn(t, "TABLE_CAT", VARCHAR);
            addColumn(t, "TABLE_SCHEM", VARCHAR);
            addColumn(t, "TABLE_NAME", VARCHAR, false);    // not null
            addColumn(t, "DATA_SOURCE_DEFINTION", VARCHAR);
            addColumn(t, "FILE_PATH", VARCHAR);
            addColumn(t, "FILE_ENCODING", VARCHAR);
            addColumn(t, "FIELD_SEPARATOR", VARCHAR);
            addColumn(t, "VARCHAR_SEPARATOR", VARCHAR);
            addColumn(t, "LONGVARCHAR_SEPARATOR", VARCHAR);
            addColumn(t, "IS_IGNORE_FIRST", BIT);
            addColumn(t, "IS_ALL_QUOTED", BIT);
            addColumn(t, "IS_DESC", BIT);

            // ------------------------------------------------------------
            t.createPrimaryKey();

            return t;
        }

        // intermediate holders
        Iterator    tables;
        Table       table;
        Object      row[];
        DITableInfo ti;
        TextCache   tc;

        // column number mappings
        final int itable_cat   = 0;
        final int itable_schem = 1;
        final int itable_name  = 2;
        final int idsd         = 3;
        final int ifile_path   = 4;
        final int ifile_enc    = 5;
        final int ifs          = 6;
        final int ivfs         = 7;
        final int ilvfs        = 8;
        final int iif          = 9;
        final int iiaq         = 10;
        final int iid          = 11;

        // Initialization
        tables = database.getTables().iterator();

        // Do it.
        while (tables.hasNext()) {
            table = (Table) tables.next();

            if (!table.isText() ||!isAccessibleTable(table)) {
                continue;
            }

            row               = t.getNewRow();
            row[itable_cat]   = ns.getCatalogName(table);
            row[itable_schem] = ns.getSchemaName(table);
            row[itable_name]  = table.getName().name;

            if (table.cCache != null && table.cCache instanceof TextCache) {
                tc              = (TextCache) table.cCache;
                row[idsd]       = table.getDataSource();
                row[ifile_path] = new File(tc.sName).getAbsolutePath();
                row[ifile_enc]  = tc.stringEncoding;
                row[ifs]        = tc.fs;
                row[ivfs]       = tc.vs;
                row[ilvfs]      = tc.lvs;
                row[iif]        = ValuePool.getBoolean(tc.ignoreFirst);
                row[iiaq]       = ValuePool.getBoolean(tc.rowIn.allQuoted);
                row[iid] = ValuePool.getBoolean(table.isDescDataSource());
            }

            t.insert(row, session);
        }

        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing of the usage
     * of accessible columns in accessible triggers defined within
     * the database. <p>
     *
     * Each column usage description has the following columns: <p>
     *
     * <pre>
     * TRIGGER_CAT   VARCHAR   Trigger catalog.
     * TRIGGER_SCHEM VARCHAR   Trigger schema.
     * TRIGGER_NAME  VARCHAR   Trigger name.
     * TABLE_CAT     VARCHAR   Catalog of table on which the trigger is defined.
     * TABLE_SCHEM   VARCHAR   Schema of table on which the trigger is defined.
     * TABLE_NAME    VARCHAR   Table on which the trigger is defined.
     * COLUMN_NAME   VARCHAR   Name of the column used in the trigger.
     * COLUMN_LIST   VARCHAR   Specified in UPDATE clause?: ("Y" | "N"}
     * COLUMN_USAGE  VARCHAR   {"NEW" | "OLD" | "IN" | "OUT" | "IN OUT"}
     * </pre> <p>
     * @return a <code>Table</code> object describing of the usage
     *        of accessible columns in accessible triggers
     *        defined within this database
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
            // of each table may eventually be listed under various capacities
            // (when a more comprehensive trugger system is put in place)
            // false PK, as cat and schem may be null
            t.createPrimaryKey(null, new int[] {
                0, 1, 2, 3, 4, 5, 6, 7, 8
            }, false);

            // fast lookup by trigger ident
            addIndex(t, null, new int[]{ 1 }, false);
            addIndex(t, null, new int[]{ 2 }, false);

            // fast lookup by table ident
            addIndex(t, null, new int[]{ 3 }, false);
            addIndex(t, null, new int[]{ 4 }, false);
            addIndex(t, null, new int[]{ 5 }, false);

            return t;
        }

        java.sql.ResultSet rs;

        // - used appends to make class file constant pool smaller
        // - saves ~ 100 bytes jar space
        rs = session.getInternalConnection().createStatement().executeQuery(
            (new StringBuffer(185)).append("select").append(' ').append(
                "a.").append("TRIGGER_CAT").append(',').append("a.").append(
                "TRIGGER_SCHEM").append(',').append("a.").append(
                "TRIGGER_NAME").append(',').append("a.").append(
                "TABLE_CAT").append(',').append("a.").append(
                "TABLE_SCHEM").append(',').append("a.").append(
                "TABLE_NAME").append(',').append("b.").append(
                "COLUMN_NAME").append(',').append("'Y'").append(',').append(
                "'IN'").append(' ').append("from").append(' ').append(
                "SYSTEM_TRIGGERS").append(" a,").append(
                "SYSTEM_COLUMNS").append(" b ").append("where").append(
                ' ').append("a.").append("TABLE_NAME").append('=').append(
                "b.").append("TABLE_NAME").toString());

        t.insert(((jdbcResultSet) rs).rResult, session);
        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the accessible
     * triggers defined within the database. <p>
     *
     * Each row is a trigger description with the following columns: <p>
     *
     * <pre>
     * TRIGGER_CAT       VARCHAR   Trigger catalog.
     * TRIGGER_SCHEM     VARCHAR   Trigger Schema.
     * TRIGGER_NAME      VARCHAR   Trigger Name.
     * TRIGGER_TYPE      VARCHAR   {("BEFORE" | "AFTER") + [" EACH ROW"] }
     * TRIGGERING_EVENT  VARCHAR   {"INSERT" | "UPDATE" | "DELETE"}
     *                             (future?: "INSTEAD OF " + ("SELECT" | ...))
     * TABLE_CAT         VARCHAR   Table's catalog.
     * TABLE_SCHEM       VARCHAR   Table's schema.
     * BASE_OBJECT_TYPE  VARCHAR   "TABLE"
     *                             (future?: "VIEW" | "SCHEMA" | "DATABASE")
     * TABLE_NAME        VARCHAR   Table on which trigger is defined
     * COLUMN_NAME       VARCHAR   NULL (future?: nested table column name)
     * REFERENCING_NAMES VARCHAR   ROW, OLD, NEW, etc.
     * WHEN_CLAUSE       VARCHAR   Condition firing trigger (NULL => always)
     * STATUS            VARCHAR   {"ENABLED" | "DISABLED"}
     * DESCRIPTION       VARCHAR   typically, the trigger's DDL
     * ACTION_TYPE       VARCHAR   "CALL" (future?: embedded language name)
     * TRIGGER_BODY      VARCHAR   Statement(s) executed
     * </pre> <p>
     *
     * @return a <code>Table</code> object describing the accessible
     *    triggers defined within this database.
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
            // added for unique: TRIGGER_CAT
            // false PK, as TRIGGER_SCHEM and/or TRIGGER_CAT may be null
            t.createPrimaryKey(null, new int[] {
                3, 1, 2, 0
            }, false);

            // fast lookup by trigger ident
            addIndex(t, null, new int[]{ 0 }, false);
            addIndex(t, null, new int[]{ 1 }, false);
            addIndex(t, null, new int[]{ 2 }, false);

            // fast lookup by table ident
            addIndex(t, null, new int[]{ 5 }, false);
            addIndex(t, null, new int[]{ 6 }, false);
            addIndex(t, null, new int[]{ 8 }, false);

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
        Iterator        tables;
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
        tables = database.getTables().iterator();

        // these are the only values supported, currently
        actionType       = "CALL";
        baseObjectType   = "TABLE";
        columnName       = null;
        referencingNames = "ROW";
        whenClause       = null;

        // Do it.
        while (tables.hasNext()) {
            table  = (Table) tables.next();
            vTrigs = table.vTrigs;

            // faster test first
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
                    status      = def.valid ? "ENABLED"
                                            : "DISABLED";
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

                    t.insert(row, session);
                }
            }
        }

        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the accessible
     * attributes of the accessible user-defined type (UDT) objects
     * defined within this database. <p>
     *
     * This description does not contain inherited attributes. <p>
     *
     * Each row is a user-defined type attributes description with the
     * following columns:
     *
     * <pre>
     * TYPE_CAT          VARCHAR   type catalog
     * TYPE_SCHEM        VARCHAR   type schema
     * TYPE_NAME         VARCHAR   type name
     * ATTR_NAME         VARCHAR   attribute name
     * DATA_TYPE         SMALLINT  attribute's SQL type from DITypes
     * ATTR_TYPE_NAME    VARCHAR   UDT: fully qualified type name
     *                            REF: fully qualified type name of target type of
     *                            the reference type.
     * ATTR_SIZE         INTEGER   column size.
     *                            char or date types => maximum number of characters;
     *                            numeric or decimal types => precision.
     * DECIMAL_DIGITS    INTEGER   # of fractional digits (scale) of number type
     * NUM_PREC_RADIX    INTEGER   Radix of number type
     * NULLABLE          INTEGER   whether NULL is allowed
     * REMARKS           VARCHAR   comment describing attribute
     * ATTR_DEF          VARCHAR   default attribute value
     * SQL_DATA_TYPE     INTEGER   expected value of SQL CLI SQL_DESC_TYPE in the SQLDA
     * SQL_DATETIME_SUB  INTEGER   DATETIME/INTERVAL => datetime/interval subcode
     * CHAR_OCTET_LENGTH INTEGER   for char types:  max bytes in column
     * ORDINAL_POSITION  INTEGER   index of column in table (starting at 1)
     * IS_NULLABLE       VARCHAR   "NO" => strictly no NULL values;
     *                             "YES" => maybe NULL values;
     *                             "" => unknown.
     * SCOPE_CATALOG     VARCHAR   catalog of REF attribute scope table or NULL
     * SCOPE_SCHEMA      VARCHAR   schema of REF attribute scope table or NULL
     * SCOPE_TABLE       VARCHAR   name of REF attribute scope table or NULL
     * SOURCE_DATA_TYPE  SMALLINT  For DISTINCT or user-generated REF DATA_TYPE:
     *                            source SQL type from DITypes
     *                            For other DATA_TYPE values:  NULL
     * </pre>
     *
     * <B>Note:</B> Currently, neither the HSQLDB engine or the JDBC driver
     * support UDTs, so an empty table is returned. <p>
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
     * <P>Each row is a UDT descripion with the following columns:
     * <OL>
     *   <LI><B>TYPE_CAT</B> <code>VARCHAR</code> => the type's catalog
     *   <LI><B>TYPE_SCHEM</B> <code>VARCHAR</code> => type's schema
     *   <LI><B>TYPE_NAME</B> <code>VARCHAR</code> => type name
     *   <LI><B>CLASS_NAME</B> <code>VARCHAR</code> => Java class name
     *   <LI><B>DATA_TYPE</B> <code>VARCHAR</code> =>
     *         type value defined in <code>DITypes</code>;
     *         one of <code>JAVA_OBJECT</code>, <code>STRUCT</code>, or
     *        <code>DISTINCT</code>
     *   <LI><B>REMARKS</B> <code>VARCHAR</code> =>
     *          explanatory comment on the type
     *   <LI><B>BASE_TYPE</B><code>SMALLINT</code> =>
     *          type code of the source type of a DISTINCT type or the
     *          type that implements the user-generated reference type of the
     *          SELF_REFERENCING_COLUMN of a structured type as defined in
     *          DITypes (null if DATA_TYPE is not DISTINCT or not
     *          STRUCT with REFERENCE_GENERATION = USER_DEFINED)
     *
     * </OL> <p>
     *
     * <B>Note:</B> Currently, neither the HSQLDB engine or the JDBC driver
     * support UDTs, so an empty table is returned. <p>
     *
     * @return a <code>Table</code> object describing the accessible
     *      user-defined types defined in this database
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
            addColumn(t, "BASE_TYPE", SMALLINT);
            t.createPrimaryKey(null);

            return t;
        }

        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the accessible
     * columns that are automatically updated when any value in a row
     * is updated. <p>
     *
     * Each row is a version column description with the following columns: <p>
     *
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
     * <B>Note:</B> Currently, the HSQLDB engine does not support version
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
     * Retrieves a <code>Table</code> object describing the VIEW objects
     * defined within this database. The table contains one row for each row
     * in the SYSTEM_TABLES table with a TABLE_TYPE of ’VIEW’. <p>
     *
     * Each row is a description of the query expression that defines its view,
     * with the following columns:
     *
     * <pre>
     * TABLE_CATALOG    VARCHAR     name of view's defining catalog.
     * TABLE_SCHEMA     VARCHAR     unqualified name of view's defining schema.
     * TABLE_NAME       VARCHAR     the simple name of the view.
     * VIEW_DEFINITION  VARCHAR     the character representation of the
     *                              &lt;query expression&gt; contained in the
     *                              corresponding &lt;view descriptor&gt;.
     * CHECK_OPTION     VARCHAR     {"CASCADED" | "LOCAL" | "NONE"}
     * IS_UPDATABLE     VARCHAR     {"YES" | "NO"}
     * VALID            BIT         TRUE: VIEW_DEFINITION currently represents
     *                              a valid &lt;query expression&gt.
     *                              FALSE: VIEW_DEFINITION cannot currently be
     *                              parsed to a valid &lt;query expression&gt;.
     *                              This can happen if, say, a table, column or
     *                              routine upon which the view depends has
     *                              been incompatibly altered, has been
     *                              dropped since the view was created or is no
     *                              longer accessible due to changes in assigned
     *                              access permissions
     * </pre> <p>
     *
     * @return a tabular description of the text source of all
     *        <code>View</code> objects accessible to
     *        the user.
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_VIEWS() throws SQLException {

        Table t = sysTables[SYSTEM_VIEWS];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[SYSTEM_VIEWS]);

            addColumn(t, "TABLE_CATALOG", VARCHAR);
            addColumn(t, "TABLE_SCHEMA", VARCHAR);
            addColumn(t, "TABLE_NAME", VARCHAR, true);         // not null
            addColumn(t, "VIEW_DEFINITION", VARCHAR, true);    // not null
            addColumn(t, "CHECK_OPTION", VARCHAR, true);       // not null
            addColumn(t, "IS_UPDATABLE", VARCHAR, true);       // not null
            addColumn(t, "VALID", BIT, true);                  // not null

            // order TABLE_NAME
            // added for unique: TABLE_SCHEMA, TABLE_CATALOG
            // false PK, as TABLE_SCHEMA and/or TABLE_CATALOG may be null
            t.createPrimaryKey(null, new int[] {
                1, 2, 0
            }, false);

            // fast lookup by view ident
            addIndex(t, null, new int[]{ 0 }, false);

            //addIndex(t, null, new int[]{1},false);
            addIndex(t, null, new int[]{ 2 }, false);

            return t;
        }

        Parser    parser;
        String    defn;
        Select    select;
        Tokenizer tokenizer;
        Iterator  tables;
        Table     table;
        Object[]  row;
        final int icat   = 0;
        final int ischem = 1;
        final int iname  = 2;
        final int idefn  = 3;
        final int icopt  = 4;
        final int iiupd  = 5;
        final int ivalid = 6;

        tables = database.getTables().iterator();

        while (tables.hasNext()) {
            table = (Table) tables.next();

            if (!table.isView() ||!isAccessibleTable(table)) {
                continue;
            }

            row         = t.getNewRow();
            defn        = ((View) table).getStatement();
            row[icat]   = ns.getCatalogName(table);
            row[ischem] = ns.getSchemaName(table);
            row[iname]  = table.getName().name;
            row[idefn]  = defn;
            row[icopt]  = "NONE";
            row[iiupd]  = "NO";

            try {
                tokenizer = new Tokenizer(defn);

                tokenizer.getThis("SELECT");

                parser = new Parser(database, tokenizer, session);
                select = parser.parseSelect();

                select.resolve();
                select.checkResolved();

                row[ivalid] = Boolean.TRUE;
            } catch (Exception e) {
                row[ivalid] = Boolean.FALSE;
            }

            t.insert(row, session);
        }

        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the
     * return, parameter and result columns of the accessible
     * routines defined within this database.<p>
     *
     * Each row is a procedure column description with the following
     * columns: <p>
     *
     * <pre>
     * PROCEDURE_CAT   VARCHAR   routine catalog
     * PROCEDURE_SCHEM VARCHAR   routine schema
     * PROCEDURE_NAME  VARCHAR   routine name
     * COLUMN_NAME     VARCHAR   column/parameter name
     * COLUMN_TYPE     SMALLINT  kind of column/parameter
     * DATA_TYPE       SMALLINT  SQL type from DITypes
     * TYPE_NAME       VARCHAR   SQL type name
     * PRECISION       INTEGER   precision (length) of type
     * LENGTH          INTEGER   transfer size, in bytes, if definitely known
     *                           (roughly equivalent to BUFFER_SIZE for table
     *                           columns)
     * SCALE           SMALLINT  scale
     * RADIX           SMALLINT  radix
     * NULLABLE        SMALLINT  can column contain NULL?
     * REMARKS         VARCHAR   explanatory comment on column
     * SIGNATURE       VARCHAR   typically (but not restricted to) a
     *                           Java Method signature
     * SEQ             INTEGER   The JDBC-specified order within
     *                           runs of PROCEDURE_SCHEM, PROCEDURE_NAME,
     *                           SIGNATURE, which is:
     *
     *                           return value (0), if any, first, followed
     *                           by the parameter descriptions in call order
     *                           (1..n1), followed by the result column
     *                           descriptions in column number order
     *                           (n1 + 1..n1 + n2)
     * </pre> <p>
     *
     * @return a <code>Table</code> object describing the
     *        return, parameter and result columns
     *        of the accessible routines defined
     *        within this database.
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_PROCEDURECOLUMNS() throws SQLException {

        Table t = sysTables[SYSTEM_PROCEDURECOLUMNS];

        if (t == null) {
            return super.SYSTEM_PROCEDURECOLUMNS();
        }

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

        // intermediate holders
        HsqlArrayList aliasList;
        Object[]      info;
        Method        method;
        Iterator      methods;
        Object[]      row;
        DITypeInfo    ti;

        // Initialization
        methods = ns.enumAllAccessibleMethods(session, true);    // and aliases
        ti      = new DITypeInfo();

        // no such thing as identity or ignorecase return/parameter
        // procedure columns.  Future: may need to worry about this if
        // result columns are ever reported
        ti.setTypeSub(TYPE_SUB_DEFAULT);

        // Do it.
        while (methods.hasNext()) {
            info             = (Object[]) methods.next();
            method           = (Method) info[0];
            aliasList        = (HsqlArrayList) info[1];
            procedureCatalog = ns.getCatalogName(method);
            procedureSchema  = ns.getSchemaName(method);

            pi.setMethod(method);

            sig           = pi.getSignature();
            procedureName = pi.getFQN();

            for (int i = 0; i < pi.getColCount(); i++) {
                ti.setTypeCode(pi.getColTypeCode(i));

                columnName   = pi.getColName(i);
                columnType   = pi.getColUsage(i);
                dataType     = pi.getColDataType(i);
                dataTypeName = ti.getTypeName();
                precision    = ti.getPrecision();
                length       = pi.getColLen(i);
                scale        = ti.getDefaultScale();
                radix        = ti.getNumPrecRadix();
                nullability  = pi.getColNullability(i);
                remark       = pi.getColRemark(i);

                addPColRows(t, aliasList, procedureCatalog, procedureSchema,
                            procedureName, columnName, columnType, dataType,
                            dataTypeName, precision, length, scale, radix,
                            nullability, remark, sig, i);
            }
        }

        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the accessible
     * routines defined within this database.
     *
     * Each row is a procedure description with the following
     * columns: <p>
     *
     * <pre>
     * PROCEDURE_CAT     VARCHAR   catalog in which routine is defined
     * PROCEDURE_SCHEM   VARCHAR   schema in which routine is defined
     * PROCEDURE_NAME    VARCHAR   simple routine identifier
     * NUM_INPUT_PARAMS  INTEGER   number of input parameters
     * NUM_OUTPUT_PARAMS INTEGER   number of output parameters
     * NUM_RESULT_SETS   INTEGER   number of result sets returned
     * REMARKS           VARCHAR   explanatory comment on the routine
     * PROCEDURE_TYPE    SMALLINT  { Unknown | No Result | Returns Result }
     * ORIGIN            VARCHAR   {ALIAS |
     *                             [BUILTIN | USER DEFINED] ROUTINE |
     *                             [BUILTIN | USER DEFINED] TRIGGER |
     *                              ...}
     * SIGNATURE         VARCHAR   typically (but not restricted to) a
     *                             Java Method signature
     * </pre> <p>
     *
     * @return a <code>Table</code> object describing the accessible
     *        routines defined within the this database
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_PROCEDURES() throws SQLException {

        Table t = sysTables[SYSTEM_PROCEDURES];

        if (t == null) {
            return super.SYSTEM_PROCEDURES();
        }

        // calculated column values
        // ------------------------
        // required
        // ------------------------
        String  catalog;
        String  schema;
        String  procName;
        Integer numInputParams;
        Integer numOutputParams;
        Integer numResultSets;
        String  remarks;
        Integer procRType;

        // -------------------
        // extended
        // -------------------
        String procOrigin;
        String procSignature;

        // intermediate holders
        String        alias;
        HsqlArrayList aliasList;
        Iterator      methods;
        Object[]      methodInfo;
        Method        method;
        String        methodOrigin;
        Object[]      row;

        // Initialization
        methods = ns.enumAllAccessibleMethods(session, true);    //and aliases

        // Do it.
        while (methods.hasNext()) {
            methodInfo   = (Object[]) methods.next();
            method       = (Method) methodInfo[0];
            aliasList    = (HsqlArrayList) methodInfo[1];
            methodOrigin = (String) methodInfo[2];

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

            addProcRows(t, aliasList, catalog, schema, procName,
                        numInputParams, numOutputParams, numResultSets,
                        remarks, procRType, procOrigin, procSignature);
        }

        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Inserts a set of procedure column description rows into the
     * <code>Table</code> specified by the <code>t</code> argument. <p>
     *
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
     *      nullabiliy code
     * @param remark a human-readable remark regarding the column
     * @param sig the signature of the procedure
     *      (typically but not limited to
     *      a java method signature)
     * @param seq helper value to allow JDBC contract order
     * @throws SQLException if there is problem inserting the specified rows
     *      in the table
     *
     */
    protected void addPColRows(Table t, HsqlArrayList l, String cat,
                               String schem, String pName, String cName,
                               Integer cType, Integer dType, String tName,
                               Integer prec, Integer len, Integer scale,
                               Integer radix, Integer nullability,
                               String remark, String sig,
                               int seq) throws SQLException {

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
        Integer   sequence   = ValuePool.getInt(seq);

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
        row[iseq]       = sequence;

        t.insert(row, session);

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
                row[iseq]       = sequence;

                t.insert(row, session);
            }
        }
    }

    /**
     * Inserts a set of procedure description rows into the <code>Table</code>
     * object specified by the <code>t</code> argument. <p>
     *
     * @param t the table into which the specified rows will eventually
     *      be inserted
     * @param l the list of procedure name aliases to which the specified column
     *      values apply
     * @param cat the procedure catalog name
     * @param schem the procedure schema name
     * @param pName the base (non-alias) procedure name
     * @param ip the procedure input parameter count
     * @param op the procedure output parameter count
     * @param rs the procedure result column count
     * @param remark a human-readable remark regarding the procedure
     * @param pType the procedure type code, indicating whether it is a
     *      function, procedure, or uncatagorized (i.e. returns
     *      a value, does not return a value, or it is unknown
     *      if it returns a value)
     * @param origin origin of the procedure, e.g.
     *      (["BUILTIN" | "USER DEFINED"] "ROUTINE" | "TRIGGER") | "ALIAS", etc.
     * @param sig the signature of the procedure
     *      (typically but not limited to a
     *      java method signature)
     * @throws SQLException if there is problem inserting the specified rows
     *      in the table
     *
     */
    protected void addProcRows(Table t, HsqlArrayList l, String cat,
                               String schem, String pName, Integer ip,
                               Integer op, Integer rs, String remark,
                               Integer pType, String origin,
                               String sig) throws SQLException {

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

        t.insert(row, session);

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

                t.insert(row, session);
            }
        }
    }

// boucherb@users - 20020305
// EXPERIMENTAL:  NONE OF THESE ARE REALLY WORKABLE UNTIL WE STOP USING
// AN INVISIBLE INTERMEDAIATE "SYSTEM_SUBQUERY" TABLE TO ROLL UP
// SUBQUERIES.  NEED TO EXTENDING TableFilter/Select TO PRESERVE
// THE ENTIRE EXPRESSION TREE TO ARBITRARY NESTING DEPTH.
// WILL ALSO ALLOW PROPER QUERY OPTIMIZATION.  RIGHT NOW, INDEX INFO IS
// LOST WHEN CREATING INTERMEDAIATE "SYSTEM_SUBQUERY" TABLES, SO IT IS
// IMPOSSSIBLE TO CHOOSE "optimal" INDEX USAGE.
//    Table SYSTEM_VIEW_COLUMN_USAGE() throws SQLException {
//
//        Table t = sysTables[SYSTEM_VIEW_COLUMN_USAGE];
//
//        if (t == null) {
//            t = createBlankTable(sysTableHsqlNames[SYSTEM_VIEW_COLUMN_USAGE]);
//
//            addColumn(t, "VIEW_CATALOG", VARCHAR);
//            addColumn(t, "VIEW_SCHEMA", VARCHAR);
//            addColumn(t, "VIEW_NAME", VARCHAR, true); // not null
//            addColumn(t, "TABLE_CATALOG", VARCHAR);
//            addColumn(t, "TABLE_SCHEMA", VARCHAR);
//            addColumn(t, "TABLE_NAME", VARCHAR, true); // not null
//            addColumn(t, "COLUMN_NAME", VARCHAR, true);
//
//            t.createPrimaryKey(null);
//
//            return t;
//        }
//
//        String      vcat;
//        String      vschem;
//        String      vname;
//        String      tcat;
//        String      tschem;
//        String      tname;
//        String      tcname;
//
//        Parser      parser;
//        String      defn;
//        Select      select;
//        Tokenizer   tokenizer;
//        Iterator tables;
//        Table       table;
//        Object[]    row;
//
//
//        final int   ivcat   = 0;
//        final int   ivschem = 1;
//        final int   ivname  = 2;
//        final int   itcat   = 3;
//        final int   itschem = 4;
//        final int   itname  = 5;
//        final int   itcname = 6;
//
//        tables = database.getTables().iterator();
//
//        while (tables.hasNext()) {
//            table = (Table) tables.next();
//
//            if (!table.isView()) {
//                continue;
//            }
//
//            if (!isAccessibleTable(table)) {
//                continue;
//            }
//
//            //row                 = t.getNewRow();
//            defn                = ((View) table).getStatement();
//
//            vcat           = ns.getCatalogName(table);
//            vschem         = ns.getSchemaName(table);
//            vname          = table.getName().name;
//
//            try {
//                tokenizer = new Tokenizer(defn);
//                tokenizer.getThis("SELECT");
//                parser = new Parser(database,tokenizer,session);
//                select = parser.parseSelect();
//                select.resolve();
//
//                Expression expr = new Expression(select);
//                HsqlHashMap tableColumns = new HsqlHashMap();
//
//                expr.collectAllTableColumns(tableColumns);
//
//                Iterator e = tableColumns.keys();
//
//                while (e.hasNext()) {
//                    tname = (String) e.next();
//                    tcat = ns.getCatalogName(tname);
//                    tschem = null;
//                    if ("SYSTEM_SUBQUERY".equals(tname)) {
//
//                    } else {
//                        try {
//                            tschem = ns.getSchemaName(database.getTable(tname,session));
//                        } catch (Exception e2) {
//
//                        }
//                    }
//                    Iterator columns = ((HashSet)tableColumns.get(tname)).iterator();
//
//                    while (columns.hasNext()) {
//
//                        row = t.getNewRow();
//
//                        row[ivcat] = vcat;
//                        row[ivschem] = vschem;
//                        row[ivname] = vname;
//                        row[itcat] = tcat;
//                        row[itschem] = tschem;
//                        row[itname] = tname;
//                        row[itcname] = (String) columns.next();
//
//                        t.insert(row,session);
//
//                    }
//                }
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }
//
//        t.setDataReadOnly(true);
//
//        return t;
//    }
//
//    Table SYSTEM_VIEW_ROUTINE_USAGE() throws SQLException {
//        Table t = sysTables[SYSTEM_VIEW_ROUTINE_USAGE];
//
//        if (t == null) {
//            t = createBlankTable(sysTableHsqlNames[SYSTEM_VIEW_ROUTINE_USAGE]);
//
//            addColumn(t, "TABLE_CATALOG", VARCHAR);
//            addColumn(t, "TABLE_SCHEMA", VARCHAR);
//            addColumn(t, "TABLE_NAME", VARCHAR, true); // not null
//            addColumn(t, "SPECIFIC_CATALOG", VARCHAR);
//            addColumn(t, "SPECIFIC_SCHEMA", VARCHAR);
//            addColumn(t, "SPECIFIC_NAME", VARCHAR, true); // not null
//
//            t.createPrimaryKey(null);
//
//            return t;
//        }
//
//        String      tcat;
//        String      tschem;
//        String      tname;
//        String      scat;
//        String      sschem;
//        String      sname;
//
//        Parser      parser;
//        String      defn;
//        Select      select;
//        Tokenizer   tokenizer;
//        Iterator tables;
//        Table       table;
//        Object[]    row;
//
//
//        final int   itcat   = 0;
//        final int   itschem = 1;
//        final int   itname  = 2;
//        final int   iscat   = 3;
//        final int   isschem = 4;
//        final int   isname  = 5;
//
//        tables = database.getTables().iterator();
//
//        while (tables.hasNext()) {
//            table = (Table) tables.next();
//
//            if (!table.isView()) {
//                continue;
//            }
//
//            if (!isAccessibleTable(table)) {
//                continue;
//            }
//
//            defn                = ((View) table).getStatement();
//
//            tcat           = ns.getCatalogName(table);
//            tschem         = ns.getSchemaName(table);
//            tname          = table.getName().name;
//
//            try {
//                tokenizer = new Tokenizer(defn);
//                tokenizer.getThis("SELECT");
//                parser = new Parser(database,tokenizer,session);
//                select = parser.parseSelect();
//                select.resolve();
//
//                Expression expr = new Expression(select);
//
//                HashSet routineNames = new HashSet();
//
//                expr.collectAllRoutineNames(routineNames);
//
//                Iterator e = routineNames.iterator();
//
//                while (e.hasNext()) {
//
//                    sname = (String) e.next();
//                    scat = ns.getCatalogName(sname);
//                    sschem = ns.getSchemaName(ns.classForMethodFQN(sname));
//
//                    row = t.getNewRow();
//
//                    row[itcat] = tcat;
//                    row[itschem] = tschem;
//                    row[itname] = tname;
//                    row[iscat] = scat;
//                    row[isschem] = sschem;
//                    row[isname] = sname;
//
//                    t.insert(row,session);
//                }
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }
//
//        t.setDataReadOnly(true);
//
//        return t;
//    }
//
//    Table SYSTEM_VIEW_TABLE_USAGE() throws SQLException {
//        Table t = sysTables[SYSTEM_VIEW_TABLE_USAGE];
//
//        if (t == null) {
//            t = createBlankTable(sysTableHsqlNames[SYSTEM_VIEW_TABLE_USAGE]);
//
//            addColumn(t, "VIEW_CATALOG", VARCHAR);
//            addColumn(t, "VIEW_SCHEMA", VARCHAR);
//            addColumn(t, "VIEW_NAME", VARCHAR, true); // not null
//            addColumn(t, "TABLE_CATALOG", VARCHAR);
//            addColumn(t, "TABLE_SCHEMA", VARCHAR);
//            addColumn(t, "TABLE_NAME", VARCHAR, true); // not null
//
//            t.createPrimaryKey(null);
//
//            return t;
//        }
//
//        String      vcat;
//        String      vschem;
//        String      vname;
//        String      tcat;
//        String      tschem;
//        String      tname;
//
//        Parser      parser;
//        String      defn;
//        Select      select;
//        Tokenizer   tokenizer;
//        Iterator tables;
//        Table       table;
//        Object[]    row;
//
//
//        final int   ivcat   = 0;
//        final int   ivschem = 1;
//        final int   ivname  = 2;
//        final int   itcat   = 3;
//        final int   itschem = 4;
//        final int   itname  = 5;
//
//        tables = database.getTables().iterator();
//
//        while (tables.hasNext()) {
//            table = (Table) tables.next();
//
//            if (!table.isView()) {
//                continue;
//            }
//
//            if (!isAccessibleTable(table)) {
//                continue;
//            }
//
//            defn                = ((View) table).getStatement();
//
//            vcat           = ns.getCatalogName(table);
//            vschem         = ns.getSchemaName(table);
//            vname          = table.getName().name;
//
//            try {
//                tokenizer = new Tokenizer(defn);
//                tokenizer.getThis("SELECT");
//                parser = new Parser(database,tokenizer,session);
//                select = parser.parseSelect();
//                select.resolve();
//
//                Expression expr = new Expression(select);
//
//                HsqlHashMap tableColumns = new HsqlHashMap();
//
//                expr.collectAllTableColumns(tableColumns);
//
//                Iterator e = tableColumns.keys();
//
//                while (e.hasNext()) {
//                    tname = (String) e.next();
//                    tcat = ns.getCatalogName(tname);
//                    tschem = null;
//                    if ("SYSTEM_SUBQUERY".equals(tname)) {
//                      // do nothing
//                    } else {
//                        try {
//                            tschem = ns.getSchemaName(database.getTable(tname,session));
//                        } catch (Exception e2) {}
//                    }
//                    row = t.getNewRow();
//
//                    row[ivcat] = vcat;
//                    row[ivschem] = vschem;
//                    row[ivname] = vname;
//                    row[itcat] = tcat;
//                    row[itschem] = tschem;
//                    row[itname] = tname;
//
//
//                    t.insert(row,session);
//                }
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }
//
//        t.setDataReadOnly(true);
//
//        return t;
//    }
// PATCH FOR:
// Expression.java
// EXPERIMENTAL SUPPORT FOR VIEW USAGE SYSTEM TABLES
// import org.hsqldb.lib.HashSet;
// boucherb@users - support for view usage system tables
// EXPERIMENTAL:  NONE OF THESE ARE REALLY WORKABLE UNTIL WE STOP USING
// AN INVISIBLE INTERMEDAIATE "SYSTEM_SUBQUERY" TABLE TO ROLL UP SUBQUERIES
// NEED TO REWORK TableFilter/Select INSTEAD TO PRESERVE FULL EXPESSION
// TREE TO ARBITRARY NESTING DEPTH
//    boolean collectTableColumn(HsqlHashMap tableNames) {
//        //System.out.println("Processing: " + this);
//        if (iType == COLUMN) {
//            HashSet s = (HashSet) tableNames.get(sTable);
//            if (s == null) {
//                s = new HashSet();
//                tableNames.put(sTable,s);
//            }
//            s.add(sColumn);
//            return true;
//        } else if (iType == QUERY || iType == SELECT) {
//            Expression eColumn[] = sSelect.eColumn;
//            for (int i = 0; i < eColumn.length; i++) {
//                eColumn[i].collectAllTableColumns(tableNames);
//            }
//            if (sSelect.eCondition != null) {
//                sSelect.eCondition.collectAllTableColumns(tableNames);
//            }
//            if (sSelect.havingCondition != null) {
//                sSelect.havingCondition.collectAllTableColumns(tableNames);
//            }
//            //System.out.println("Processed Select: " + sSelect);
//            return true;
//        } else if (iType == FUNCTION) {
//            Expression eArg[] = fFunction.eArg;
//
//            for (int i = 0; i < eArg.length; i++) {
//                eArg[i].collectAllTableColumns(tableNames);
//            }
//            //System.out.println("Processed Function: " + fFunction);
//            return true;
//        }
//
//        return false;
//    }
//
//    void collectAllTableColumns(HsqlHashMap tableNames) {
//        if (!collectTableColumn(tableNames)) {
//            if (eArg != null) {
//                eArg.collectAllTableColumns(tableNames);
//            }
//
//            if (eArg2 != null) {
//                eArg2.collectAllTableColumns(tableNames);
//            }
//        }
//    }
//
//    boolean collectRoutineName(HashSet routineNames) {
//        //System.out.println("Processing: " + this);
//
//        if (iType == QUERY || iType == SELECT) {
//            Expression eColumn[] = sSelect.eColumn;
//            for (int i = 0; i < eColumn.length; i++) {
//                eColumn[i].collectAllRoutineNames(routineNames);
//            }
//            if (sSelect.eCondition != null) {
//                sSelect.eCondition.collectAllRoutineNames(routineNames);
//            }
//            if (sSelect.havingCondition != null) {
//                sSelect.havingCondition.collectAllRoutineNames(routineNames);
//            }
//            //System.out.println("Processed Select: " + sSelect);
//            return true;
//        } else if (iType == FUNCTION) {
//            Expression eArg[] = fFunction.eArg;
//
//            if (eArg != null) {
//                for (int i = 0; i < eArg.length; i++) {
//                    if (eArg[i] != null) {
//                        eArg[i].collectAllRoutineNames(routineNames);
//                    }
//                }
//            }
//            routineNames.add(fFunction.fname);
//            //System.out.println("Processed Function: " + fFunction);
//            return true;
//        }
//
//        return false;
//    }
//
//    void collectAllRoutineNames(HashSet routineNames) {
//        if (!collectRoutineName(routineNames)) {
//            if (eArg != null) {
//                eArg.collectAllRoutineNames(routineNames);
//            }
//
//            if (eArg2 != null) {
//                eArg2.collectAllRoutineNames(routineNames);
//            }
//        }
//    }
}
