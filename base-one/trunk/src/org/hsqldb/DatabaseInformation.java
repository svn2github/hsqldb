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

import java.lang.reflect.Constructor;
import java.sql.SQLException;
import org.hsqldb.lib.HsqlObjectToIntMap;

// fredt@users - 1.7.2 - structural modifications to allow inheritance

/**
 * Base class for system tables. Inclues a factory method which returns the
 * most complete implementation available in the jar. This base implementation
 * knows the names of all system tables but returns null for any system table.
 * <p>
 * This class has been developed from scratch to replace the previous
 * DatabaseInformation implementations.
 *
 * @author  boucherb@users.sourceforge.net
 * @version 1.7.2
 * @since 1.7.2
 */
class DatabaseInformation {

    // ids for system table names strictly in order of sysName[]
    protected static final int SYSTEM_BESTROWIDENTIFIER = 0;
    protected static final int SYSTEM_CATALOGS          = 1;
    protected static final int SYSTEM_COLUMNPRIVILEGES  = 2;
    protected static final int SYSTEM_COLUMNS           = 3;
    protected static final int SYSTEM_CROSSREFERENCE    = 4;
    protected static final int SYSTEM_INDEXINFO         = 5;
    protected static final int SYSTEM_PRIMARYKEYS       = 6;
    protected static final int SYSTEM_PROCEDURECOLUMNS  = 7;
    protected static final int SYSTEM_PROCEDURES        = 8;
    protected static final int SYSTEM_SCHEMAS           = 9;
    protected static final int SYSTEM_SUPERTABLES       = 10;
    protected static final int SYSTEM_SUPERTYPES        = 11;
    protected static final int SYSTEM_TABLEPRIVILEGES   = 12;
    protected static final int SYSTEM_TABLES            = 13;
    protected static final int SYSTEM_TABLETYPES        = 14;
    protected static final int SYSTEM_TYPEINFO          = 15;
    protected static final int SYSTEM_UDTATTRIBUTES     = 16;
    protected static final int SYSTEM_UDTS              = 17;
    protected static final int SYSTEM_USERS             = 18;
    protected static final int SYSTEM_VERSIONCOLUMNS    = 19;

    // HSQLDB-specific
    protected static final int SYSTEM_ALIASES         = 20;
    protected static final int SYSTEM_BYTECODE        = 21;
    protected static final int SYSTEM_CACHEINFO       = 22;
    protected static final int SYSTEM_CLASSPRIVILEGES = 23;
    protected static final int SYSTEM_CONNECTIONINFO  = 24;
    protected static final int SYSTEM_PROPERTIES      = 25;
    protected static final int SYSTEM_SESSIONS        = 26;
    protected static final int SYSTEM_TRIGGERCOLUMNS  = 27;
    protected static final int SYSTEM_TRIGGERS        = 28;
    protected static final int SYSTEM_ALLTYPEINFO     = 29;
    protected static final int SYSTEM_VIEWSOURCE      = 30;

    // system table names strictly in order of their ids
    protected static final String sysNames[] = {
        "SYSTEM_BESTROWIDENTIFIER",                   //
        "SYSTEM_CATALOGS",                            //
        "SYSTEM_COLUMNPRIVILEGES",                    //
        "SYSTEM_COLUMNS",                             //
        "SYSTEM_CROSSREFERENCE",                      //
        "SYSTEM_INDEXINFO",                           //
        "SYSTEM_PRIMARYKEYS",                         //
        "SYSTEM_PROCEDURECOLUMNS",                    //
        "SYSTEM_PROCEDURES",                          //
        "SYSTEM_SCHEMAS",                             //
        "SYSTEM_SUPERTABLES",                         //
        "SYSTEM_SUPERTYPES",                          //
        "SYSTEM_TABLEPRIVILEGES",                     //
        "SYSTEM_TABLES",                              //
        "SYSTEM_TABLETYPES",                          //
        "SYSTEM_TYPEINFO",                            //
        "SYSTEM_UDTATTRIBUTES",                       //
        "SYSTEM_UDTS",                                //
        "SYSTEM_USERS",                               //
        "SYSTEM_VERSIONCOLUMNS",                      //

        // HSQLDB-specific
        "SYSTEM_ALIASES",                             //
        "SYSTEM_BYTECODE",                            //
        "SYSTEM_CACHEINFO",                           //
        "SYSTEM_CLASSPRIVILEGES",                     //
        "SYSTEM_CONNECTIONINFO",                      //
        "SYSTEM_PROPERTIES",                          //
        "SYSTEM_SESSIONS",                            //
        "SYSTEM_TRIGGERCOLUMNS",                      //
        "SYSTEM_TRIGGERS",                            //
        "SYSTEM_ALLTYPEINFO",                         //
        "SYSTEM_VIEWSOURCE"                           //

        // Future use
//        "SYSTEM_ASSERTIONS",
//        "SYSTEM_ATTRIBUTES",
//        "SYSTEM_CHARACTER_ENCODING_FORMS",
//        "SYSTEM_CHARACTER_REPERTOIRES",
//        "SYSTEM_CHARACTER_SETS",
//        "SYSTEM_CHECK_COLUMN_USAGE",
//        "SYSTEM_CHECK_CONSTRAINT_ROUTINE_USAGE",
//        "SYSTEM_CHECK_CONSTRAINTS",
//        "SYSTEM_CHECK_TABLE_USAGE",
//        "SYSTEM_COLLATION_CHARACTER_SET_APPLICABILITY",
//        "SYSTEM_COLLATIONS",
//        "SYSTEM_COLUMN_COLUMN_USAGE",
//        "SYSTEM_COLUMN_OPTIONS",
//        "SYSTEM_COLUMN_PRIVILEGES",
//        "SYSTEM_COLUMNS",
//        "SYSTEM_DATA_TYPE_DESCRIPTOR",
//        "SYSTEM_DIRECT_SUPERTABLES",
//        "SYSTEM_DIRECT_SUPERTYPES",
//        "SYSTEM_DOMAIN_CONSTRAINTS",
//        "SYSTEM_DOMAINS",
//        "SYSTEM_ELEMENT_TYPES",
//        "SYSTEM_FIELDS",
//        "SYSTEM_FOREIGN_DATA_WRAPPER_OPTIONS",
//        "SYSTEM_FOREIGN_DATA_WRAPPERS",
//        "SYSTEM_FOREIGN_SERVER_OPTIONS",
//        "SYSTEM_FOREIGN_SERVERS",
//        "SYSTEM_FOREIGN_TABLE_OPTIONS",
//        "SYSTEM_FOREIGN_TABLES",
//        "SYSTEM_JAR_JAR_USAGE",
//        "SYSTEM_JARS",
//        "SYSTEM_KEY_COLUMN_USAGE",
//        "SYSTEM_METHOD_SPECIFICATION_PARAMETERS",
//        "SYSTEM_METHOD_SPECIFICATIONS",
//        "SYSTEM_MODULE_COLUMN_USAGE",
//        "SYSTEM_MODULE_PRIVILEGES",
//        "SYSTEM_MODULE_TABLE_USAGE",
//        "SYSTEM_MODULES",
//        "SYSTEM_PARAMETERS",
//        "SYSTEM_REFERENCED_TYPES",
//        "SYSTEM_REFERENTIAL_CONSTRAINTS",
//        "SYSTEM_ROLE_AUTHORIZATION_DESCRIPTORS",
//        "SYSTEM_ROLES",
//        "SYSTEM_ROUTINE_COLUMN_USAGE",
//        "SYSTEM_ROUTINE_JAR_USAGE",
//        "SYSTEM_ROUTINE_MAPPING_OPTIONS",
//        "SYSTEM_ROUTINE_MAPPINGS",
//        "SYSTEM_ROUTINE_PRIVILEGES",
//        "SYSTEM_ROUTINE_ROUTINE_USAGE",
//        "SYSTEM_ROUTINE_SEQUENCE_USAGE",
//        "SYSTEM_ROUTINE_TABLE_USAGE",
//        "SYSTEM_ROUTINES",
//        "SYSTEM_SCHEMATA",
//        "SYSTEM_SEQUENCES",
//        "SYSTEM_SQL_FEATURES",
//        "SYSTEM_SQL_IMPLEMENTATION_INFO",
//        "SYSTEM_SQL_LANGUAGES",
//        "SYSTEM_SQL_SIZING",
//        "SYSTEM_SQL_SIZING_PROFILES",
//        "SYSTEM_TABLE_CONSTRAINTS",
//        "SYSTEM_TABLE_METHOD_PRIVILEGES",
//        "SYSTEM_TABLE_PRIVILEGES",
//        "SYSTEM_TABLES",
//        "SYSTEM_TRANSFORMS",
//        "SYSTEM_TRANSLATIONS",
//        "SYSTEM_TRIGGER_COLUMN_USAGE",
//        "SYSTEM_TRIGGER_ROUTINE_USAGE",
//        "SYSTEM_TRIGGER_SEQUENCE_USAGE",
//        "SYSTEM_TRIGGER_TABLE_USAGE",
//        "SYSTEM_TRIGGERED_UPDATE_COLUMNS",
//        "SYSTEM_TRIGGERS",
//        "SYSTEM_TYPE_JAR_USAGE",
//        "SYSTEM_USAGE_PRIVILEGES",
//        "SYSTEM_USER_DEFINED_TYPE_PRIVILEGES",
//        "SYSTEM_USER_DEFINED_TYPES",
//        "SYSTEM_USER_MAPPING_OPTIONS",
//        "SYSTEM_USER_MAPPINGS",
//        "SYSTEM_USERS",
//        "SYSTEM_VIEW_COLUMN_USAGE",
//        "SYSTEM_VIEW_ROUTINE_USAGE",
//        "SYSTEM_VIEW_TABLE_USAGE",
//        "SYSTEM_VIEWS",
    };

    // map for id lookup
    protected static final HsqlObjectToIntMap sysTableNames;

    static {
        sysTableNames = new HsqlObjectToIntMap(47);

        for (int i = 0; i < sysNames.length; i++) {
            sysTableNames.put(sysNames[i], i);
        }
    }

    /** Database for which to produce tables */
    protected Database database;

    /**
     * Simple object-wide flag indicating that all of this object's cached
     * data is dirty.
     */
    protected boolean dirty = true;

    /**
     * state flag -- if true, contentful tables are to be produced, else
     * empty (surrogate) tables are to be produced.  This allows faster
     * database startup where user views reference system tables and faster
     * system table structural reflection for table metadata.
     */
    protected boolean withContent = false;

    /**
     * Factory method retuns the fullest system table producer
     * implementation available.  This instantiates implementations beginning
     * with the most complet, finally choosing an empty table producer
     * implemenation (this class) if no better instance can be constructed.
     *
     * @throws SQLException never - required by TableProducer.<init>
     */
    static DatabaseInformation newDatabaseInformation(Database db)
    throws SQLException {

        String[] impls = new String[] {
            "org.hsqldb.DatabaseInformationFull",
            "org.hsqldb.DatabaseInformationMain",
            "org.hsqldb.DatabaseInformation"
        };
        Class               clazz;
        Class[]             ctorParmTypes = new Class[]{ Database.class };
        Object[]            ctorParms     = new Object[]{ db };
        DatabaseInformation impl          = null;
        Constructor         ctor;

        for (int i = 0; i < impls.length; i++) {
            try {
                clazz = Class.forName(impls[i]);
                ctor  = clazz.getDeclaredConstructor(ctorParmTypes);
                impl  = (DatabaseInformation) ctor.newInstance(ctorParms);

                if (impl != null) {
                    break;
                }
            } catch (Exception e) {
                if (Trace.TRACE) {
                    Trace.trace(e.getMessage());
                }
            }
        }

        return impl;
    }

    /**
     * Constructor
     */
    DatabaseInformation(Database db) throws SQLException {
        database = db;
    }

    /**
     * Tests if the specified name is that of a system table.
     *
     * @param name the name to test
     * @return true if the specified name is that of a system table
     *
     */
    boolean isSystemTable(String name) {
        return sysTableNames.containsKey(name);
    }

    /**
     * Retrieves a table with the specified name whose content may depend on
     * the execution context indicated by the session argument as well as the
     * current value of <code>isWithContent()</code>.
     *
     * @param name the name of the table to produce
     * @param session the context in which to produce the table
     * @throws SQLException if a database access error occurs
     * @return a table corresponding to the name and session arguments, or
     *      <code>null</code> if there is no such table to be produced
     */
    Table getSystemTable(String name, Session session) throws SQLException {
        return null;
    }

    /**
     * Controls caching of all tables produced by this object. <p>
     *
     * Subclasses are free to ignore this, since they may choose an
     * implementation that does not dynamically generate and/or cache
     * table content on an as-needed basis. <p>
     *
     * If not ignored, this call indicates to this object that all cached
     * table data may be dirty, requiring a complete cache clear at some
     * point.<p>
     *
     * Subclasses are free to delay cache clear until next produceTable().
     * However, subclasses may have to be aware of additional methods with
     * semantics similar to produceTable() and act accordingly (e.g. clearing
     * earlier than next invocation of produceTable()).
     *
     * @throws SQLException if a database access error occurs
     */
    void setDirty() throws SQLException {
        dirty = true;
    }

    /**
     * Controls caching of the named table. <p>
     *
     * Note that a table with the indicated name may not be produced
     * by this object. As a general policy, it is not stritly an error
     * to specify such a name, but if it is specified, implementors
     * should probably ignore the call (i.e. is should have no effect on
     * the cache state).<p>
     *
     * Subclasses are free to ignore this call completely, since they may choose an
     * implementation that does not dynamically generate and cache table content
     * on an as-needed basis. <p>
     *
     * If not ignored, this call indicates to this object that cached table
     * data for the specified table is dirty for all sessions, requiring a
     * regeneration of the data for the indicated table only.<p>
     *
     * Subclasses are free to delay cache clear until next get.  However,
     * they may have to be aware of additional methods with semantics similar to
     * produceTable() and act accordingly.
     *
     * @param name of table whose data is dirty
     * @throws SQLException if a database access error occurs
     */
    void setDirty(String name) throws SQLException {
        setDirty();
    }

    /**
     * Controls caching of the named table for the specified session. <p>
     *
     * Note that a table with the indicated name may not be produced
     * by this object for the specified session. As a general policy,
     * it is not stritly an error to specify such a name/session pair,
     * but if it is specified, implementors should probably ignore the
     * call (i.e. is should have no effect on the cache state).<p>
     *
     * Subclasses are free to ignore this call completely, since they may
     * choose an implementation that does not dynamically generate and
     * cache table content on an as-needed basis. <p>
     *
     * If not ignored, this call indicates to this object that cached table
     * data for the specified table and session is dirty, requiring a
     * regeneration of the data for the {table, session} pair.<p>
     *
     * Subclasses are free to delay cache clear until the next invokation of
     * produceTable().  However, they may have to be aware of additional
     * methods with semantics similar to produceTable() and act accordingly.
     *
     * @param name the name of the table whose cached data is dirty
     * @param session whose cached data is dirty
     * @throws SQLException if a database access error occurs
     */
    void setDirty(String name, Session session) throws SQLException {
        setDirty(name);
    }

    /**
     * Retrieves whether this object's entire table cache, if any,
     * is dirty.
     *
     * @return true if this object's entire table cache is dirty
     *
     */
    boolean isDirty() {
        return dirty;
    }

    /**
     * Retrieves whether the cached data, if any, of the table with the
     * specified name is dirty.
     *
     * @return true if the cached data of the table with the specified name
     * is dirty.
     * @param name the name of the table to test
     */
    boolean isDirty(String name) {
        return isDirty();
    }

    /**
     * Retrieves whether the cached data, if any, of the table with the
     * specified name is dirty within the context of the specified session.
     *
     * @return true if he cached data, if any, of the table with the
     * specified name is dirty within the context of the specified session.
     * @param name the name of the table to test
     * @param session the context within which to perform the test
     */
    boolean isDirty(String name, Session session) {
        return isDirty(name);
    }

    /**
     * Retrieves whether this table producer is presently producing empty
     * (surrogate) or contentful tables.
     *
     * @return true if this table producer is presently producing
     * contentful tables, else false
     */
    boolean isWithContent() {
        return withContent;
    }

    /**
     * Switches this table producer between producing empty (surrogate)
     * or contentful tables.
     *
     * @param withContent if true, then produce contentful tables, else
     *        produce emtpy (surrogate) tables
     */
    void setWithContent(boolean withContent) {
        this.withContent = withContent;
    }
}
