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

//import java.lang.reflect.Field;
//import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.DatabaseMetaData;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Enumeration;
import java.util.Hashtable;
/*
import org.hsqldb.clazz.classfile.AttributeInfo;
import org.hsqldb.clazz.classfile.ClassFile;
import org.hsqldb.clazz.classfile.Instruction;
import org.hsqldb.clazz.classfile.MemberInfo;
import org.hsqldb.clazz.classfile.OpCodeHelper;
import org.hsqldb.clazz.classfile.io.ClassFileInputStream;
*/
import org.hsqldb.lib.ArrayUtil;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.HsqlHashSet;

//import org.hsqldb.lib.HsqlIntKeyHashMap;
import org.hsqldb.lib.HsqlIntKeyIntValueHashMap;
import org.hsqldb.lib.HsqlHashMap;
import org.hsqldb.lib.HsqlList;
import org.hsqldb.lib.HsqlStringBuffer;
import org.hsqldb.lib.StopWatch;
import org.hsqldb.lib.ValuePool;
import org.hsqldb.lib.enum.ArrayEnumeration;
import org.hsqldb.lib.enum.CompositeEnumeration;
import org.hsqldb.lib.enum.EmptyEnumeration;
import org.hsqldb.lib.enum.SingletonEnumeration;
import org.hsqldb.resources.BundleHandler;

// fredt@users - 1.7.2 - structural modifications to allow inheritance

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
final class DatabaseInformationFull extends DatabaseInformation {

    // HsqlName objects for the system tables
    protected static final HsqlName[] sysTableHsqlNames;

    static {
        sysTableHsqlNames = new HsqlName[sysTableNames.length];

        for (int i = 0; i < sysTableNames.length; i++) {
            sysTableHsqlNames[i] = HsqlName.newAutoName(null,
                    sysTableNames[i]);
        }
    }

    /** current user for each cached system table */
    protected Session[] sysTableSessions = new Session[sysTableNames.length];

    /** true if the contents of a cached system table depends on the session */
    protected boolean[] sysTableSessionDependent =
        new boolean[sysTableNames.length];

    /** cache of system tables */
    protected Table[] sysTables = new Table[sysTableNames.length];

    /**
     * The <code>Class</code> of the procedure column, if any, under
     * consideration in the current execution context.
     */
    private Class _class;

    /**
     * The <code>Column</code> object, if any, under consideration in the
     * current executution context.
     */

//    private Column _column;

    /**
     * The name of the <code>Column</code> object, if any, under
     * consideration in the current execution context.
     */
//    private String _columnName;

    /**
     * The name of the database, as known to the <code>Database</code> object
     * for which this object is producing sytem tables
     */
    private String _databaseName;

    /**
     * Used in buffer and character octet length calculations.  <p>
     *
     * Basically, a character data type whose precision is > _HALF_MAX_INT
     * does not have a representable buffer or character octet length,
     * since the JDBC/SQL CLI contract states that these columns/variables
     * contain <code>int</code> values.  Therefore, this value is used
     * to determine when to return null in buffer and character octet
     * length calculations.
     */
    private static final int _HALF_MAX_INT = Integer.MAX_VALUE >>> 1;

    /**
     * Map: <code>Class</code> FQN <code>String</code> object =>
     * <code>ClassFile</code> object.
     */
    private static HsqlHashMap _hClazzClassFiles;

    /**
     * Map: <code>Method</code> FQN <code>String</code> object =>
     * <code>Instruction[]</code> object.
     */
    private static HsqlHashMap _hClazzInstructions;

    /**
     * Map: <code>Class</code> object => <code>Integer</code> internal data
     * type code.
     */
    private static HsqlHashMap _hCls;

    /**
     * Map: <code>Method</code> FQN <code>String</code> object =>
     * <code>ResultSetMetaData</code> or <code>Table</code> object.
     */
    private static HsqlHashMap _hResultDescriptions;

    /**
     * MAP: <code>Constraint</code> update/delete action <code>int</code> =>
     * <code>DatabaseMetaData</code> update/delete rule <code>Integer</code>
     * object.
     */

//    private static HsqlIntKeyHashMap _hfkRules;

    /**
     * Set { <code>Class</code> FQN <code>String</code> objects }. <p>
     *
     * The Set contains the names of the classes providing the public static
     * methods that are automatically made accessible to the PUBLIC user in
     * support of the expected SQL CLI scalar functions and other core
     * HSQLDB SQL functions and stored procedures.  <P>
     *
     * This object exists in support of the <code>ORIGIN</code> column of the
     * <code>SYSTEM_PROCEDURES</code> table. <p>
     */
    private static HsqlHashSet _hsBuiltInLibs;

    /** Set: { names of system tables that are not to be cached } */
    private static HsqlHashSet nonCachedTablesSet;

    /** Set { names of all tables generated directly by this object }. */

//    private static HsqlHashSet _hsSysTableNames;

    /**
     * Map: simple <code>Table</code> name <code>String</code> object =>
     * <code>HsqlName</code> object.
     */
//    private static HsqlHashMap _hSysTableHsqlNames;

    /**
     * Map: simple <code>Column</code> name <code>String</code> object =>
     * <code>HsqlName</code> object.
     */
    private static HsqlHashMap _hSysTableColumnHsqlNames;

    /**
     * Map: simple <code>Index</code> name <code>String</code> object =>
     * <code>HsqlName</code> object.
     */
    private static HsqlHashMap _hSysTableIndexHsqlNames;

    /**
     * Map: cache key <code>String</code> = > <code>Table</code> object. <p>
     *
     * This is a cache of system table instances used to speed up system table
     * queries and reduce CPU workload. <p>
     *
     * The basic idea is to return a cached version of a table iff: <p>
     *
     * <UL>
     *    <LI>such a table is currently cached, and
     *    <LI>it still accurately reflects the information it is
     *        supposed to convey
     * </UL>
     *
     * Otherwise, a fresh system table is constucted, filled with row data,
     * cached for potential future use and returned. <p>
     * @see #_cacheKey
     */

//    private HsqlHashMap _hSystemTableCache;

    /**
     * Map: <code>Table</code> name <code>String</code> =>
     * table generation <code>Method</code>.
     * @see #_generateTable
     */
//    private static HsqlHashMap _hSysTableMethodCache;

    /**
     * Map: external (typically JDBC) data type code <code>int</code> =>
     * internal data type code <code>int</code>.
     */
    private static HsqlIntKeyIntValueHashMap _hTNum;

    /**
     * The internal value used to represent the data type of the table or
     * procedure column currently under consideration.
     */
    private int _iInternalType = I_NULL;

    /**
     * <code>true</code> if current <code>_session</code> attribute user is
     * admin, <code>_session</code> user has been granted any access to
     * the current <code>_table</code> attribute or <code>_table</code> is
     * temp (but not system) and is owned by _session.
     */
    private boolean _isAccessibleTable = false;

    /**
     * <code>true</code> after {@link #_initStatic1()} is called the first
     * time. <p>
     *
     * Code in <code>_initStatic1()</code> is executed only on the very
     * first construction of this object to implement lazy
     * inititialization of static things needed immediately after
     * th very first construction  (e.g. things needed to return the
     * correct value from the <code>produces()</code> method). <p>
     *
     * This value  acts as a guard to ensure that the body of
     * <code>_initStatic1()</code> is executed only once in a
     * JVM session. <p>
     */

//    private static boolean _isInitProduces = false;

    /**
     * <code>true</code> after {@link #_initStatic2()} is called the
     * first time.  <p>
     *
     * Code in <code>_initStatic2()</code> is executed on first call to
     * {@link #produceTable()} to implement lazy inititialization of
     * static things not needed until first call to
     * <code>prodcueTable()</code>.  <p>
     *
     * This value acts as a guard to ensure that the body of
     * <code>_initStatic2()</code> is executed only once in
     * a JVM session. <p>
     */
    private static boolean _isInitProduceTable = false;

    /**
     * <code>true</code> iff the <code>Table</code> last set as the
     * <code>_table</code> attribute is a system table.
     */
    private boolean _isSystemTable = false;

    /**
     * <code>true </code> iff the <code>Table</code> last set as the
     * <code>_table</code> attribute is a temp table.
     */
    private boolean _isTempTable = false;

    /** Used in _generateTable() to reflectively invoke a table producing method. */

    //private static final Class[] _pTypes = new Class[]{Boolean.TYPE};

    /**
     * Used in {@link #_generateTable()} to reflectively invoke a
     * table producing method.
     */
    private static final Object[] _pValues = new Object[]{};

    /**
     * A <code>Result</code> object that holds the unchanging rows of
     * the <code>SYSTEM_PROPERTIES</code> table.
     */
    private Result rStaticProperties;

    /**
     * A handle for the resource bundle used to obtain
     * localized SYSTEM_TABLES.REMARKS values for system tables.
     */
    private static int _rbHndTableRemarks = -1;

    /**
     * A handle for the resource bundle used to obtain
     * localized SYSTEM_TABLES.REMARKS values for
     * system table columns.
     */
    private static int _rbHndTColumnRemarks = -1;

    /**
     * A handle for the resource bundle used to obtain
     * localized SYSTEM_TYPEINFO.CREATE_PARAMETERS values.
     */
    private static int _rbHndTICreateParams = -1;

    /**
     * A handle for the resource bundle used to obtain
     * localized SYSTEM_TYPEINFO.LOCAL_NAME values.
     */
    private static int _rbHndTILocalNames = -1;

    /**
     * A handle for the resource bundle used to obtain
     * localized SYSTEM_TYPEINFO.REMARKS values.
     */
    private static int _rbHndTIRemarks = -1;

    /**
     * An <code>HsqlStringBuffer</code> object that is reused for building
     * lookup keys for system table column values.
     */
    private static HsqlStringBuffer _sb;

    /**
     * The <code>Session</code> object under consideration in the current
     * executution context.
     */
    protected Session _session;

    /**
     * A list of <code>Table</code> object surrogates representing the
     * intrinsic aspects (the columns, table type, table name, etc.) of
     * the tables that this  object produces directly (it may also produce
     * tables through a process of name substitution combined with lookup
     * in the database user table repository). <p>
     *
     * This list facilitates lightweight reporting of system table metadata.
     * That is, under dynamically built and cached system tables, is is
     * much faster and memory efficient to generate only once and reflect
     * on a list of empty, prebuilt <code>Table</code> objects, rather than
     * having to recreate and fill each table with content before reporting
     * on its structure.
     */

//    private static HsqlArrayList _sysTableProtos;

    /**
     * Map:  simple table name <code>String</code> object
     * => surrogate <code>Table</code> object.
     */
//    private static HsqlHashMap _hSysTableProtos;

    /**
     * The <code>Table</code> object, if any, under consideration in the
     * current exectution context.
     */
    private Table _table;

    /**
     * The simple name <code>String</code> of current
     * <code>_table</code> attribute.
     */
    private String _tableName;

    /**
     * A work-around attribute to ease difficulties imposed by
     * by the nature of {@link HsqlName} hashCode and equality
     * in combination with the relflective nature of this object's
     * table generation algorithm. <p>
     *
     * The nature of <code>HsqlName</code> hashCode and equality is such that
     * rights maps (see {@link User}} continue to work under alteration of
     * database object <code>HsqlName</code> name attributes.  This is a fine
     * thing but requires that a database object's <code>HsqlName</code> stays
     * constant for the lifetime of the database in a JVM session.  <p>
     *
     * Unfortunately, this imposes certain difficulties upon this system
     * table producer implementation in that each user may potententially own
     * a different, periodically newly constructed version of each system table
     * showing different visiblity of database objects based on the user's
     * rights, which implies that it must be ensured that system tables
     * whose simple <code>String</code> names are equal actually share the same
     * <code>HsqlName</code> instance which stays the same for the life of the
     * database in a JVM session.
     *
     * On the other hand, this class has been designed so that developers can add
     * system table producing methods with very little work and almost no
     * detailed knowlege of the inner working of this class.  That is, all that is
     * required is to add a method whose name starts with "SYSTEM_" and that the
     * method return a <code>Table</code> object.  These tow facts are automatically
     * pickled up via relflection both by {@link #produceTable} and by
     * {@link #_initStatic1()} that creates the system table surrogate list.  <p>
     *
     * However the above described nature of <code>HsqlName</code> requires that
     * either all system table producing methods "know" the name of the table they
     * are to produce and do a lookup in a static repository of <code>HsqlName</code>
     * objects to assign the one-and-only correct <code>HsqlName</code> instance
     * to produced tables (which is at cross-concerns with using reflection to
     * determine the names of available tables and selecting the correct method
     * for a submitted table name <code>String</code>), or that the reflective
     * table generation process handle this directly.  Due to the cross-concerns
     * conflict and in the interest of simplicity and avoiding requiring developers
     * to write repetitive code, it was deemed better form to delegate to the
     * reflection process. So, the selected <code>HsqlName</code> of the table
     * to be produced is *not* passed to system table producing methods but is
     * instead set momentarily as the value of this attriute by the
     * reflective table generation process, immediately prior to relflectively
     * invoking the selected table producing method.  Then when the selected
     * table producing method calls {@link #_createTable} to get its table primoid
     * (no rows or columns yet, just a primoidal named <code>Table</code> object),
     * the <code>_createTable</code> method uses the correct <code>HsqlName</code>
     * instance selected from the static HsqlName object repository, i.e. the value
     * of this attibute just selected by the reflective table generation process.
     * @see _findOrCreateHsqlName
     * @see HsqlName
     */

//    private HsqlName _tableHsqlName;

    /** The table types HSQLDB supports. */
    private static final String[] _tableTypes = new String[] {
        "GLOBAL TEMPORARY", "SYSTEM TABLE", "TABLE", "VIEW"
    };

    /**
     * The user-level security management object from the <code>Database</code>
     * object for which the object is producing tables.
     */
    private UserManager _userManager;

    /**
     * The list of user tables from the <code>Database</code> object
     * for which this object is producing tables. <p>
     */
    private HsqlList _userTableList;

    /**
     * Internal type code corresponding to the external
     * <code>ARRAY</code> type code.
     */
    private static final int I_ARRAY = 0;

    /**
     * Internal type code corresponding to the external
     * <code>BIGINT</code> type code.
     */
    private static final int I_BIGINT = 1;

    /**
     * Internal type code corresponding to the external
     * <code>BINARY</code> type code.
     */
    private static final int I_BINARY = 2;

    /**
     * Internal type code corresponding to the external
     * <code>BIT</code> type code.
     */
    private static final int I_BIT = 3;

    /**
     * Internal type code corresponding to the external
     * <code>BLOB</code> type code.
     */
    private static final int I_BLOB = 4;

    /**
     * Internal type code corresponding to the external
     * <code>CHAR</code> type code.
     */
    private static final int I_CHAR = 5;

    /**
     * Internal type code corresponding to the external
     * <code>CLOB</code> type code.
     */
    private static final int I_CLOB = 6;

    /**
     * Internal type code corresponding to the external
     * <code>DATALINK</code> type code.
     */
    private static final int I_DATALINK = 7;

    /**
     * Internal type code corresponding to the external
     * <code>DATE</code> type code.
     */
    private static final int I_DATE = 8;

    /**
     * Internal type code corresponding to the external
     * <code>DECIMAL</code> type code.
     */
    private static final int I_DECIMAL = 9;

    /**
     * Internal type code corresponding to the external
     * <code>DISTINCT</code> type code.
     */
    private static final int I_DISTINCT = 10;

    /**
     * Internal type code corresponding to the external
     * <code>DOUBLE</code> type code.
     */
    private static final int I_DOUBLE = 11;

    /**
     * Internal type code corresponding to the external
     * <code>FLOAT</code> type code.
     */
    private static final int I_FLOAT = 12;

    /**
     * Internal type code corresponding to the external
     * <code>INTEGER</code> type code.
     */
    private static final int I_INTEGER = 13;

    /**
     * Internal type code corresponding to the external
     * <code>INTEGER</code> type code.  This is a special value used
     * to indicate that the column type is <code>INTEGER</code> *and
     * that the column is an identity column.
     */
    private static final int I_INTEGER_IDENTITY = 14;

    /**
     * Internal type code corresponding to the external
     * <code>JAVA_OBJECT</code> type code.
     */
    private static final int I_JAVA_OBJECT = 15;

    /**
     * Internal type code corresponding to the external
     * <code>LONGVARBINARY</code> type code.
     */
    private static final int I_LONGVARBINARY = 16;

    /**
     * Internal type code corresponding to the external
     * <code>LONGVARCHAR</code> type code.
     */
    private static final int I_LONGVARCHAR = 17;

    /**
     * Internal type code corresponding to the external
     * <code>NULL</code> type code.
     */
    private static final int I_NULL = 18;

    /**
     * Internal type code corresponding to the external
     * <code>NUMERIC</code> type code.
     */
    private static final int I_NUMERIC = 19;

    /**
     * Internal type code corresponding to the external
     * <code>OTHER</code> type code.
     */
    private static final int I_OTHER = 20;

    /**
     * Internal type code corresponding to the external
     * <code>REAL</code> type code.
     */
    private static final int I_REAL = 21;

    /**
     * Internal type code corresponding to the external
     * <code>REF</code> type code.
     */
    private static final int I_REF = 22;

    /**
     * Internal type code corresponding to the external
     * <code>SMALLINT</code> type code.
     */
    private static final int I_SMALLINT = 23;

    /**
     * Internal type code corresponding to the external
     * <code>STRUCT</code> type code.
     */
    private static final int I_STRUCT = 24;

    /**
     * Internal type code corresponding to the external
     * <code>TIME</code> type code.
     */
    private static final int I_TIME = 25;

    /**
     * Internal type code corresponding to the external
     * <code>TIMESTAMP</code> type code.
     */
    private static final int I_TIMESTAMP = 26;

    /**
     * Internal type code corresponding to the external
     * <code>TINYINT</code> type code.
     */
    private static final int I_TINYINT = 27;

    /**
     * Internal type code corresponding to the external
     * <code>VARBINARY</code> type code.
     */
    private static final int I_VARBINARY = 28;

    /**
     * Internal type code corresponding to the external
     * <code>VARCHAR</code> type code.
     */
    private static final int I_VARCHAR = 29;

    /**
     * Internal type code corresponding to the external
     * <code>VARCHAR_IGNORECASE</code> type code.
     */
    private static final int I_VARCHAR_IGNORECASE = 30;

    /**
     * Internal type code corresponding to the external
     * <code>XML</code> type code.
     */
    private static final int I_XML = 31;

    /**
     * Array of primitive <code>int</code> values corresponding to each
     * assignable HSQLDB database object access right.
     */
    private static final int[] IA_ALL_RIGHTS = new int[] {
        UserManager.ALL, UserManager.SELECT, UserManager.INSERT,
        UserManager.UPDATE, UserManager.DELETE
    };

    /** Array Map: index (i.e. internal data type code) => external data type code. */
    private static final Integer[] IA_JDBC_DATA_TYPES = {

        /*ARRAY*/
        ValuePool.getInt(2003),

        /*BIGINT*/
        ValuePool.getInt(-5),

        /*BINARY*/
        ValuePool.getInt(-2),

        /*BIT*/
        ValuePool.getInt(-7),

        /*BLOB*/
        ValuePool.getInt(2004),

        /*CHAR*/
        ValuePool.getInt(1),

        /*CLOB*/
        ValuePool.getInt(2005),

        /*DATALINK*/
        ValuePool.getInt(70),

        /*DATE*/
        ValuePool.getInt(91),

        /*DECIMAL*/
        ValuePool.getInt(3),

        /*DISTINCT*/
        ValuePool.getInt(2001),

        /*DOUBLE*/
        ValuePool.getInt(8),

        /*FLOAT*/
        ValuePool.getInt(6),

        /*INTEGER*/
        ValuePool.getInt(4),

        /*INTEGER IDENTITY*/
        ValuePool.getInt(4),

        /*JAVA_OBJECT*/
        ValuePool.getInt(2000),

        /*LONGVARBINARY*/
        ValuePool.getInt(-4),

        /*LONGVARCHAR*/
        ValuePool.getInt(-1),

        /*NULL*/
        ValuePool.getInt(0),

        /*NUMERIC*/
        ValuePool.getInt(2),

        /*OTHER*/
        ValuePool.getInt(1111),

        /*REAL*/
        ValuePool.getInt(7),

        /*REF*/
        ValuePool.getInt(2006),

        /*SMALLINT*/
        ValuePool.getInt(5),

        /*STRUCT*/
        ValuePool.getInt(2002),

        /*TIME*/
        ValuePool.getInt(92),

        /*TIMESTAMP*/
        ValuePool.getInt(93),

        /*TINYINT*/
        ValuePool.getInt(-6),

        /*VARBINARY*/
        ValuePool.getInt(-3),

        /*VARCHAR*/
        ValuePool.getInt(12),

        /*VARCHAR_IGNORECASE*/
        ValuePool.getInt(Column.VARCHAR_IGNORECASE),

        /*XML*/
        ValuePool.getInt(137)
    };

    /** Array Map: index (i.e. internal data type code) => SQL CLI data type code. */
    private static final Integer[] IA_SQL_DATA_TYPES = {

        // values from SQL200n SQL CLI spec, or java.sql.Types if
        // there was no corresponding value in SQL CLI

        /*ARRAY*/
        ValuePool.getInt(50),      // SQL_ARRAY

        /*BIGINT*/
        ValuePool.getInt(25),      // SQL_BIGINT

        /*BINARY*/
        ValuePool.getInt(15),      // SQL_BIT_VARYING

        /*BIT*/
        ValuePool.getInt(16),      // SQL_BOOLEAN

        /*BLOB*/
        ValuePool.getInt(30),      // SQL_BLOB

        /*CHAR*/
        ValuePool.getInt(1),       // SQL_CHAR

        /*CLOB*/
        ValuePool.getInt(40),      // SQL_CLOB

        /*DATALINK*/
        ValuePool.getInt(70),      // SQL_DATALINK

        /*DATE*/
        ValuePool.getInt(9),       // SQL_DATETIME

        /*DECIMAL*/
        ValuePool.getInt(3),       // SQL_DECIMAL

        /*DISTINCT*/
        ValuePool.getInt(17),      // SQL_UDT

        /*DOUBLE*/
        ValuePool.getInt(8),       // SQL_DOUBLE

        /*FLOAT*/
        ValuePool.getInt(6),       // SQL_FLOAT

        /*INTEGER*/
        ValuePool.getInt(4),       // SQL_INTEGER

        /*INTEGER IDENTITY*/
        ValuePool.getInt(4),       // SQL_INTEGER

        /*JAVA_OBJECT*/
        ValuePool.getInt(2000),    // N/A - maybe SQL_UDT?

        /*LONGVARBINARY*/
        ValuePool.getInt(15),      // SQL_BIT_VARYING

        /*LONGVARCHAR*/
        ValuePool.getInt(-1),      // N/A - use java.sql.Types

        /*NULL*/
        ValuePool.getInt(0),       // SQL_ALL_TYPES

        /*NUMERIC*/
        ValuePool.getInt(2),       // SQL_NUMERIC

        /*OTHER*/
        ValuePool.getInt(1111),    // N/A - maybe SQL_UDT?

        /*REAL*/
        ValuePool.getInt(7),       // SQL_REAL

        /*REF*/
        ValuePool.getInt(20),      // SQL_REF

        /*SMALLINT*/
        ValuePool.getInt(5),       // SQL_SMALLINTEGER

        /*STRUCT*/
        ValuePool.getInt(17),      // SQL_UDT

        /*TIME*/
        ValuePool.getInt(9),       // SQL_DATETIME

        /*TIMESTAMP*/
        ValuePool.getInt(9),       // SQL_DATETIME

        /*TINYINT*/
        ValuePool.getInt(-6),      // N/A - use java.sql.Types

        /*VARBINARY*/
        ValuePool.getInt(15),      // SQL_BIT_VARYING

        /*VARCHAR*/
        ValuePool.getInt(12),      // SQL_VARCHAR

        /*VARCHAR_IGNORECASE*/
        ValuePool.getInt(Column.VARCHAR_IGNORECASE),

        /*XML*/

        ValuePool.getInt(137)      // SQL_XML
    };

    /** The <code>DEFINITION_SCHEMA</code> schema name. */
    static final String QS_DEFN_SCHEMA = "DEFINITION_SCHEMA";

    /**
     * The <code>DEFINITION_SCHEMA</code> schema name plus the schema
     * separator character.
     */
    static final String QS_DEFN_SCHEMA_DOT = QS_DEFN_SCHEMA + ".";

    /** Length of <code>QS_DEFN_SCHEMA_DOT</code>. */
    static final int QS_DEFN_SCHEMA_DOT_LEN = QS_DEFN_SCHEMA_DOT.length();

    /** The <code>INFORMATION_SCHEMA</code> schema name. */
    static final String QS_INFO_SCHEMA = "INFORMATION_SCHEMA";

    /**
     * The <code>INFORMATION_SCHEMA</code> schema name plus the schema
     * separator character.
     */
    static final String QS_INFO_SCHEMA_DOT = QS_INFO_SCHEMA + ".";

    /** Length of <code>QS_INFO_SCHEMA_DOT</code>. */
    static final int QS_INFO_SCHEMA_DOT_LEN = QS_INFO_SCHEMA_DOT.length();

    /** The <code>PUBLIC</code> schema name. */
    static final String QS_PUB_SCHEMA = "PUBLIC";

    /** The <code>PUBLIC</code> schema name plus the schema separator character. */
    static final String QS_PUB_SCHEMA_DOT = QS_PUB_SCHEMA + ".";

    /** Length of <code>QS_PUB_SCHEMA_DOT</code>. */
    static final int QS_PUB_SCHEMA_DOT_LEN = QS_PUB_SCHEMA_DOT.length();

    /** Array Map: index (i.e. internal data type code) => data type name. */
    private static final String[] SA_DATA_TYPE_NAMES = {
        "ARRAY", "BIGINT", "BINARY", "BIT", "BLOB", "CHAR", "CLOB",
        "DATALINK", "DATE", "DECIMAL", "DISTINCT", "DOUBLE", "FLOAT",
        "INTEGER", "INTEGER IDENTITY", "JAVA_OBJECT", "LONGVARBINARY",
        "LONGVARCHAR", "NULL", "NUMERIC", "OTHER", "REAL", "REF", "SMALLINT",
        "STRUCT", "TIME", "TIMESTAMP", "TINYINT", "VARBINARY", "VARCHAR",
        "VARCHAR_IGNORECASE", "XML"
    };

    /**
     * Constructs a table producer which provides system tables
     * for the specified <code>Database</code> object. <p>
     *
     * <b>Note:</b> it is important to observe that by specifying an instance
     * of this class to handle system table production, the default permissions
     * and aliases of the indicated database are upgraded, meaning that it may
     * not be possible to properly open the same database again if using a less
     * capable system table producer instance. Even if it is, the permission and
     * alias requirments are almost certain to be different, so care must be taken
     * to resove these issues, possibly by manual modifiaction of the database's
     * REDO log (script file). <p>
     *
     * For now: BE WARNED. <p>
     *
     * In a future release, it may be that system-generated permissions
     * and aliases are not recorded in the REDO log, removing the associated
     * <em>dangers</em>. This may well be possible to implement with little or
     * no side-effects, since these permissions and aliases must always be
     * present for proper core operation, meaning that they can and probably
     * should be programatically reintroduced on each startup and protected
     * from modification for the life of the database instance, separate from
     * permissions and aliases indroduced externally via user SQL. <p>
     * @param db the <code>Database</code> object for which this object produces
     *      system tables
     * @throws SQLException if a database access error occurs
     */
    DatabaseInformationFull(Database db) throws SQLException {

        super(db);

        _userTableList = database.getTables();
        _databaseName  = db.getName();
        _userManager   = db.getUserManager();

        Trace.doAssert(_userManager != null, "user manager is null");
        _initProduces();
    }

    /**
     * Adds a <code>Column</code> object with the specified name, data type
     * and nullability to the specified <code>Table</code> object.
     * @param t the table to which to add the specified column
     * @param name the name of the column
     * @param type the data type of the column (generally from java.sql.Types)
     * @param nullable <code>true</code> if the column is to allow null values,
     *    else <code>false</code>
     * @throws SQLException if a problem occurs when adding the column
     */
    private static void _addColumn(Table t, String name, int type,
                                   boolean nullable) throws SQLException {

        HsqlName cn = _findOrCreateHsqlName(name, _hSysTableColumnHsqlNames);
        Column   c = new Column(cn, nullable, type, 0, 0, false, false, null);

        t.addColumn(c);
    }

    /**
     * Adds a nullable <code>Column</code> object with the specified name and
     * data type to the specified <code>Table</code> object.
     * @param t the table to which to add the specified column
     * @param name the name of the column
     * @param type the data type of the column (generally from java.sql.Types)
     * @throws SQLException if a problem occurs when adding the column
     */
    private static void _addColumn(Table t, String name,
                                   int type) throws SQLException {
        _addColumn(t, name, type, true);
    }

    /**
     * Adds an <code>Index</code> object for the specifiec columns and having
     * the specified uniqueness property to the specified <code>Table</code>
     * object.
     * @param t the table to which to add the specified index
     * @param indexName the simple name <code>String</code> for the index
     * @param cols array of zero-based column numbers specifying the columns
     *    to include in the index
     * @param unique <code>true</code> if a unique index is desired,
     *    else <code>false</code>
     * @throws SQLException if there is a problem adding the specified index to the specified table
     */
    private static void _addIndex(Table t, String indexName, int[] cols,
                                  boolean unique) throws SQLException {

        HsqlName name;

        if (indexName == null || cols == null) {}
        else {
            name = _findOrCreateHsqlName(indexName, _hSysTableIndexHsqlNames);

            t.createIndex(cols, name, unique);
        }
    }

    /**
     * Creates and adds a set of procedure column description rows to the
     * <code>Result</code> object specified by the <code>r</code> argument
     * using the <code>Table</code> specified by the <code>t</code> argument
     * to construct new rows and using the remaining arguments to calculate
     * the column values for the added rows
     * @param t the table in which the rows will eventually be inserted
     * @param r the <code>Result</code> object to which to add the rows created
     *        by this method
     * @param l the list of procedure name aliases to which the specified column values apply
     * @param cat the procedure catalog name
     * @param schem the procedure schema name
     * @param pName the base (non-alias) procedure name
     * @param cName the procedure column name
     * @param cType the column type (return, parameter, result)
     * @param dType the procedure column data type (generally from java.sql.Types)
     * @param tName the procedure column data type name
     * @param prec the procedure column precision
     * @param len the procedure column buffer length
     * @param scale the procedure column scale (decimal digits)
     * @param radix the procedure column numeric precision radix
     *    (usually 10 or null)
     * @param nullability the procedure column java.sql.DatbaseMetaData
     *    nullabiliy code
     * @param remark a human-readable remark on the procedure column
     * @param sig the signature of the procedure (typically a
     *    java Method.toString() value)
     * @param seq a sort sequence helper value
     * @throws SQLException if there is problem generating or adding the specified rows
     *    to the specified hashmap
     */
    private void _addPColRows(Table t, Result r, HsqlArrayList l, String cat,
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

        r.add(row);

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

                r.add(row);
            }
        }
    }

    /**
     * Creates and adds a set of procedure description rows to the <code>Result</code>
     * object specified by the <code>r</code> argument using the <code>Table</code>
     * object specified by the <code>t</code> argument to construct new rows
     * and using the remaining arguments to calculate the column values of the
     * added rows.
     * @param t the table into which the specified rows will eventually be inserted
     * @param r the <code>Result</code> object to which to add the rows created
     *        by this method
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
     * @param remark a human-readable remark on the procedure
     * @param pType the procedure type code, indicating whether it is a function,
     *    procedure, or uncatagorized (i.e. returns a value, does not
     *    return a value, or it is unknown if it returns a value)
     * @param sig the signature of the procedure (typically but not limited to a
     * java.lang.reflect.Method.toString() value)
     * @throws SQLException if there is a problem generating or adding the specified rows
     * to the specified hashmap
     */
    private void _addProcRows(Table t, Result r, HsqlArrayList l, Class c,
                              String src, String cat, String schem,
                              String pName, Integer ip, Integer op,
                              Integer rs, String remark, Integer pType,
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
        row[iporigin]      = _getProcOrigin(false, src, c);
        row[isig]          = sig;

        r.add(row);

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
                row[iporigin]      = _getProcOrigin(true, null, null);
                row[isig]          = sig;

                r.add(row);
            }
        }
    }

    /**
     * Indicates if the given <code>String</code> object represents the
     *    name of a system table that is elligible for caching.
     * @param name table name to test
     * @return <code>true</code> iff the given <code>String</code>
     *    object represents the name of a system table that
     *    is elligible for caching.
     */
/*
    private boolean _cacheCandidate(String name) {
        return !nonCachedTablesSet.contains(name);
    }
*
    /** Clears the contents of cached system tables and resets users to null */
    private void _cacheClear() throws SQLException {

        int i = sysTables.length;

        while (--i > 0) {
            Table t = sysTables[i];

            if (t != null) {
                t.clearAllRows();
            }

            sysTableSessions[i] = null;
        }

        isDirty = false;
    }

    /**
     * Retrives a cached system table corresponding to system table name
     * represented by the given <code>String</code> object, or
     * <code>null</code> if there is no such table cached.
     * @param name name of the system table to retrieve from the cache
     * @return a cached system table corresponding to given name or <code>null</code>
     *    if there is no such table in the cache
     */
/*
    private Table _cacheGet(String name) {
        return (Table) _hSystemTableCache.get(_cacheKey(name));
    }
*/

    /**
     * Retrieves the actual key object against which a system table of the
     * given name is cached relative to the current execution context. <p>
     *
     * The calculation is based on the table name, the name of the current
     * session user (as represented by the _session member variable) and the
     * dmin status of the _session user. <p>
     *
     * All admins see the same view of the database, so for all admin
     * users only one copy of each system table needs to be cached.
     * Similarly, all sessions whose user is the same see the same view
     * of the database, so for all sessions whose user is the same, only
     * one copy of each system table needs to be cached.
     * @param name table name for which to produce the actual cache key
     * @return the actual key object against which the system table of the given
     *    name is cached relative to the current execution context
     */
/*
    private String _cacheKey(String name) {
        return _session.isAdmin()
        ? name + UserManager.SYS_USER_NAME
        : name + _session.getUsername() ;
    }
*/

    /**
     * Puts the specified <code>Table</code> object in the system table
     * cache, mapping it to the specified table name a way that is
     * relative to the current execution context.
     * @param name name of the system table to put in the cache
     * @param t The <code>Table</code> object to put in the cache
     */
/*
    private void _cachePut(String name, Table t) {
        _hSystemTableCache.put(_cacheKey(name), t);
    }
*/

    /**
     * Creates a primary key contraint for the specified
     * <code>Table</code> object on the specified columns.
     * @param t the table for which to create the specified primary key constraint
     * @param cols array of zero-based column numbers specifying the columns
     *    of the constraint.  If null, an invisible integer identity
     *    column is added and used to back an invisible internal
     *    primary key constraint
     * @throws SQLException if there is a problem creating the constraint
     */
/*
    private static void _createPk(Table t, int[] cols) throws SQLException {

       t.createPrimaryKey(cols);
    }
*/

    /**
     * Retrieves a new <code>Result</code> object whose metadata matches that
     * of the specified table.
     * @param t The table from which to construct the Result object
     * @return a new <code>Result</code> object whose metadata matches that of the
     * specified table.
     */
    private Result _createResultProto(Table t) {

        Column   column;
        HsqlName columnHsqlName;
        String   columnName;
        int      columnCount;
        String   tableName;
        Result   r;

        tableName   = t.getName().name;
        columnCount = t.getColumnCount();
        r           = new Result(columnCount);

        for (int i = 0; i < columnCount; i++) {
            r.sTable[i]        = tableName;
            column             = t.getColumn(i);
            columnHsqlName     = column.columnName;
            columnName         = columnHsqlName.name;
            r.sLabel[i]        = columnName;
            r.sName[i]         = columnName;
            r.isLabelQuoted[i] = columnHsqlName.isNameQuoted;
            r.colType[i]       = column.getType();
            r.colSize[i]       = column.getSize();
            r.colScale[i]      = column.getScale();
        }

        return r;
    }

    /**
     * Retreives the <code>Class</code> object specified by the
     * <code>name</code> argument using the database class loader.
     * @param name the fully qulified name of the <code>Class</code> object to retrieve.
     * @throws ClassNotFoundException if the specified class object cannot be found
     * @return the <code>Class</code> object specified by the
     * <code>name</code> argument
     */
    private Class _classForName(String name) throws ClassNotFoundException {

        ClassLoader cl = database.classLoader;

        return (cl == null) ? Class.forName(name)
                            : Class.forName(name, true, database.classLoader);
    }

    /**
     * Retrieves the internal data type code corresponding to specified
     * external data type code. The retrieved value acts as a handle to
     * the intrinsic data type information regarding the data type.<p>
     * @return internal type code corresponding to the specified external type code.
     * @param type external data type code for which to retrieve the corresponding
     *     internal type code.
     * @throws SQLException - if there is no internal type code corresponding to the specified
     *        external type code
     */
    private static int findInternalType(int type) {

        int[] internalType = _hTNum.get(type);

        return internalType[0];
    }

    /**
     * Retrieves the internal data type code corresponding to the specifed
     * <code>Class</code> object in the context of that object functioning
     * as a SQL routine call parameter or return type.  The retrieved value
     * acts as a handle to the intrinsic information regarding the data type.
     * @param c the <code>Class</code> object for which to retrieve the
     *        corrseponding internal data type code
     * @throws SQLException if the specified <code>Class</code> object
     *        is <code>null</code>.
     * @return the internal data type code corresponding to the specified
     *    <code>Class</code> object
     */
    private static int _findInternalType(Class c) throws SQLException {

        Trace.doAssert(c != null, "Class is null: unknown SQL Type");

        Integer internalType = (Integer) _hCls.get(c);

        if (internalType != null) {
            return internalType.intValue();
        }

        Class to;

        // ARRAY (dimension 1)
        // HSQLDB does not yet support ARRAY, but we can at least make
        // an attempt to report a reasonable estimate of what *would*
        // be required in the way of a SQL type (conversion) to pass or
        // retrieve values
        if (c.isArray() &&!c.getComponentType().isArray()) {
            return I_ARRAY;
        }

        try {
            to = Class.forName("java.sql.Array");

            if (to.isAssignableFrom(c)) {
                return I_ARRAY;
            }
        } catch (Exception e) {}

        // NUMERIC
        if (java.lang.Number.class.isAssignableFrom(c)) {
            return I_NUMERIC;
        }

        // TIMESTAMP
        try {
            to = Class.forName("java.sql.Timestamp");

            if (to.isAssignableFrom(c)) {
                return I_TIMESTAMP;
            }
        } catch (Exception e) {}

        // TIME
        try {
            to = Class.forName("java.sql.Time");

            if (to.isAssignableFrom(c)) {
                return I_TIMESTAMP;
            }
        } catch (Exception e) {}

        // DATE
        try {
            to = Class.forName("java.sql.Date");

            if (to.isAssignableFrom(c)) {
                return I_DATE;
            }
        } catch (Exception e) {}

        // BLOB
        try {
            to = Class.forName("java.sql.Blob");

            if (to.isAssignableFrom(c)) {
                return I_BLOB;
            }
        } catch (Exception e) {}

        // CLOB
        try {
            to = Class.forName("java.sql.Clob");

            if (to.isAssignableFrom(c)) {
                return I_TIMESTAMP;
            }
        } catch (Exception e) {}

        // REF
        try {
            to = Class.forName("java.sql.Ref");

            if (to.isAssignableFrom(c)) {
                return I_REF;
            }
        } catch (Exception e) {}

        // STRUCT
        try {
            to = Class.forName("java.sql.Struct");

            if (to.isAssignableFrom(c)) {
                return I_STRUCT;
            }
        } catch (Exception e) {}

        // LONGVARCHAR
        try {

            // @since JDK1.4
            to = Class.forName("java.lang.CharSequence");

            if (to.isAssignableFrom(c)) {
                return I_LONGVARCHAR;
            }
        } catch (Exception e) {}

        // we have no standard mapping for the specified class
        // at this point...is it even storable?
        if (java.io.Serializable.class.isAssignableFrom(c)
                || java.io.Externalizable.class.isAssignableFrom(c)) {

            // yes. It is storable, as an OTHER.
            return I_OTHER;
        }

        // no. It is not storable (by HSQLDB), except by conversion
        // (perhaps to VARCHAR via the generic Object.toString(),
        // which helps little at this point).
        // default:
        // We do not yet know explicitly how to handle this...
        //
        // ...unless the product is built with the option to include
        // an AbstractResultFactory and we are dealing with a class of
        // object that that passes the accepts() method of the
        // currently installed AbstractResultFactory, and then only
        // if it unnests to a Result from a CALL context or if it unnests to
        // an object whose class is in the the set of classes supported
        // in the standard mapping.  So, at this point, we have the class of
        // an object that we do not know how to store but that still may
        // at least be possible to pass about in the course of executing
        // SQL.  That is, we cannot be totally sure that we should fail at this
        // point, so just pass back the most generic description possible.
        return I_JAVA_OBJECT;
    }

    /**
     * Retrieves the one-and-only correct <code>HsqlName</code> instance
     * for the current JVM session, using the s argument as a key to
     * look up the <code>HsqlName</code> instance in the repository
     * specified by the map argument.
     * @param s the lookup key
     * @param map the HsqlName instance repository
     * @return the one-and-only correct <code>HsqlName</code> instance for the
     * specified key, <code>s</code>, in the current JVM session.
     * @see _tableHsqlName
     * @see HsqlName
     */
    private static HsqlName _findOrCreateHsqlName(String s,
            HsqlHashMap map) throws SQLException {

        HsqlName name = (HsqlName) map.get(s);

        if (name == null) {
            name = new HsqlName(s, false);

            map.put(s, name);
        }

        return name;
    }

    /**
     * Finds a <code>GLOBAL TEMPORARY</code> table, if any, corresponding to
     * the given database object identifier, relative to the current
     * execution context.<p>
     *
     * Basically, the name of the user represented by the current
     * value of the <code>_session</code> attribute, in the form of a
     * schema qualifier, is removed from the specified database object
     * identifier and then the usual process for finding a temp user
     * table is performed using the resulting simple identifier.
     * @return the <code>GLOBAL TEMPORARY</code> table, if any, corresponding to
     * the given table name, relative to the current execution context.
     * @param name the name of the table to find, possibly prefixed with a schema qualifier
     */
    private Table _findUserSchemaTable(String name) {

        if (name == null || _session == null) {
            return null;
        }

        String prefix = _session.getUsername() + ".";

        if (name.startsWith(prefix)) {
            return database.findUserTable(name.substring(prefix.length()),
                                          _session);
        }

        return null;
    }

    /**
     * Finds a regular (non-temp, non-system) table or view, if any,
     * corresponding to the given table name, relative to the current
     * execution context.<p>
     *
     * Basically, the PUBLIC schema name, in the form of a schema qualifier,
     * is removed from the specified database object identifier and then the
     * usual process for finding a non-temp, non-system table or view is
     * performed using the resulting simple identifier.
     * @return the non-temp, non-system user table, if any, corresponding to
     * the given table name, relative to the current execution context.
     * @param name the name of the table to find, possibly prefixed with a schema qualifier
     */
    private Table _findPubSchemaTable(String name) {

        return (name == null ||!name.startsWith(QS_PUB_SCHEMA_DOT)) ? null
                                                                    : database.findUserTable(
                                                                    name.substring(
                                                                        QS_PUB_SCHEMA_DOT_LEN));
    }

    /**
     * Retrieves a freshly generated system table corresponding to the
     * specified simple name or <code>null</code> if there is no
     * system table corresponding to that name.
     * @param name the simple name of a system table
     * @return A freshly generated system table with the specified simple name or null
     */
/*
    private Table _generateTable(String name) {

        Method m = (Method)_hSysTableMethodCache.get(name);

        if (m == null) return null;

        _tableHsqlName = sysTableHsqlNames[sysTableNamesMap.get(name)];

        try {
            return (Table) m.invoke(this,_pValues);
        } catch (Exception e) {
            e.printStackTrace();
            if (Trace.TRACE) {
                Trace.trace(e.getMessage());
            }
        }
        return null;
    }
*/
    private Table generateTable(int tableIndex) throws SQLException {

        Table t = sysTables[tableIndex];

        switch (tableIndex) {

            case SYSTEM_BESTROWIDENTIFIER :
                return SYSTEM_BESTROWIDENTIFIER(tableIndex);

            case SYSTEM_CATALOGS :
                return SYSTEM_CATALOGS(tableIndex);

            case SYSTEM_COLUMNPRIVILEGES :
                return SYSTEM_COLUMNPRIVILEGES(tableIndex);

            case SYSTEM_COLUMNS :
                return SYSTEM_COLUMNS(tableIndex);

            case SYSTEM_CROSSREFERENCE :
                return SYSTEM_CROSSREFERENCE(tableIndex);

            case SYSTEM_INDEXINFO :
                return SYSTEM_INDEXINFO(tableIndex);

            case SYSTEM_PRIMARYKEYS :
                return SYSTEM_PRIMARYKEYS(tableIndex);

            case SYSTEM_PROCEDURECOLUMNS :
                return SYSTEM_PROCEDURECOLUMNS(tableIndex);

            case SYSTEM_PROCEDURES :
                return SYSTEM_PROCEDURES(tableIndex);

            case SYSTEM_SCHEMAS :
                return SYSTEM_SCHEMAS(tableIndex);

            case SYSTEM_SUPERTABLES :
                return SYSTEM_SUPERTABLES(tableIndex);

            case SYSTEM_SUPERTYPES :
                return SYSTEM_SUPERTYPES(tableIndex);

            case SYSTEM_TABLEPRIVILEGES :
                return SYSTEM_TABLEPRIVILEGES(tableIndex);

            case SYSTEM_TABLES :
                return SYSTEM_TABLES(tableIndex);

            case SYSTEM_TABLETYPES :
                return SYSTEM_TABLETYPES(tableIndex);

            case SYSTEM_TYPEINFO :
                return SYSTEM_TYPEINFO(tableIndex);

            case SYSTEM_UDTATTRIBUTES :
                return SYSTEM_UDTATTRIBUTES(tableIndex);

            case SYSTEM_UDTS :
                return SYSTEM_UDTS(tableIndex);

            case SYSTEM_USERS :
                return SYSTEM_USERS(tableIndex);

            case SYSTEM_VERSIONCOLUMNS :
                return SYSTEM_VERSIONCOLUMNS(tableIndex);

            // HSQLDB-specific
            case SYSTEM_ALIASES :
                return SYSTEM_ALIASES(tableIndex);

            case SYSTEM_BYTECODE :
                return null;

            case SYSTEM_CACHEINFO :
                return SYSTEM_CACHEINFO(tableIndex);

            case SYSTEM_CLASSPRIVILEGES :
                return SYSTEM_CLASSPRIVILEGES(tableIndex);

            case SYSTEM_CONNECTIONINFO :
                return SYSTEM_CONNECTIONINFO(tableIndex);

            case SYSTEM_PROPERTIES :
                return SYSTEM_PROPERTIES(tableIndex);

            case SYSTEM_SESSIONS :
                return SYSTEM_SESSIONS(tableIndex);

            case SYSTEM_TRIGGERCOLUMNS :
                return SYSTEM_TRIGGERCOLUMNS(tableIndex);

            case SYSTEM_TRIGGERS :
                return SYSTEM_TRIGGERS(tableIndex);

            case SYSTEM_ALLTYPEINFO :
                return null;

            case SYSTEM_VIEWSOURCE :
                return SYSTEM_VIEWSOURCE(tableIndex);

            default :
                return null;
        }
    }

    /**
     * Retrives a <code>String</code> object representing the type of alias
     * represented by the specified object. <p>
     *
     * Currrently, the only type of declarable aliases supported by HSQLDB
     * are the SQL call aliases for Java methods.  As such, this method
     * currently always returns a <code>String</code> object whose value
     * is <code>"ROUTINE"</code>.
     * @return "ROUTINE"
     * @param alias not currently used
     */
    private String _getAliasType(Object alias) {
        return "ROUTINE";
    }

    /**
     * Retrieves the <code>java.sql.DatabaseMetaData</code> best row
     * identifier pseudocolumn type code relative to the current
     * execution context. <p>
     *
     * Currently, HSQLDB does not support pseudo columns of any type,
     * so this method always returns
     * <code>DatabaseMetaData.bestRowNotPseudo</code>.
     * @return <code>DatabaseMetaData.bestRowNotPseudo</code>
     */
    private Integer _getBRIPseudo() {
        return ValuePool.getInt(DatabaseMetaData.bestRowNotPseudo);
    }

    /**
     * Retrieves the best row identifier column scope value relative to the
     * current execution context, as described in the table generation rules in
     * {@link #SYSTEM_BESTROWIDENTIFIER SYSTEM_BESTROWIDENTIFIER}.
     * @return the best row identifier column scope value relative to the
     * current execution context
     */
    private Integer _getBRIScope() {

        return (database.bReadOnly || (_isTempTable &&!_isSystemTable))
               ? ValuePool.getInt(DatabaseMetaData.bestRowSession)
               : ValuePool.getInt(DatabaseMetaData.bestRowTemporary);
    }

    /**
     * Retrieves the name of the catalog corresponding to the indicated
     * object, relative to the current execution context. <p>
     *
     * <B>Note:</B> <code>_databaseName</code> is  returned whenever
     * a non-null parameter is specified.  This is an over-simplification
     * in the interest of performance that will be used until such time,
     * if ever, that the engine actually supports the concept of
     * multiple hosted catalogs. <p>
     * @return the specified object's catalog name, or null if the parameter
     *          is null.
     * @param o the object whose catalog name is to be retrieved
     * @throws SQLException never (reserved for future use)
     */
    private String _getCatalogName(Object o) throws SQLException {
        return (o == null) ? null
                           : _databaseName;
    }

    /**
     * Retrieves the {@link org.hsqldb.clazz.classfile.ClassFile}
     * object, if any, corresponding to the specified <code>Class</code>
     * FQN <code>String</code>, using, if possible, the <code>classLoader</code>
     * attribute of database for which this object is producing tables. <p>
     *
     * A cache of <code>ClassFile</code> objects is first consulted, and, if
     * found, the cached <code>ClassFile</code> object is returned. <p>
     *
     * If no cached <code>ClassFile</code> object is found, then an attempt
     * is made to read one, as specified by: <p>
     *
     * <UL>
     * <LI>
     * {@link org.hsqldb.clazz.classfile.io.ClassFileInputStream#forName(java.lang.String,java.lang.ClassLoader)
     *    ClassFileInputStream.forName(String,ClassLoader)} and,
     * <LI>
     * {@link
     *    org.hsqldb.clazz.classfile.io.ClassFileInputStream#readClassFile()
     *    ClassFileInputStream.readClassFile()
     * </UL <p>
     *
     * If an exception occurs while attempting to read in the specified
     * <code>ClassFile</code> object, <code>null</code> is returned.  Otherwise
     * the object is read in, cached against its <code>Class</code> FQN and
     * returned. <p>
     * @param className the FQN of the <code>Class</code> for which to retrieve
     *        the corresponding <code>ClassFile</code> object
     * @return a <code>ClassFile</code> object corresponding to the specified
     *        <code>Class</code> FQN, or <code>null</code> if no such object
     *        can be retrieved
     */
/*
    private ClassFile _getClazzClassFile(String className) {

        ClassFile cf = (ClassFile) _hClazzClassFiles.get(className);

        if (cf == null) {
            try {
                cf =
                ClassFileInputStream
                .forName(className,database.classLoader)
                .readClassFile();
                // cache if read succeeds
                _hClazzClassFiles.put(className, cf);
            } catch (Exception e) {
                // return null: not found
            }
        }

        return cf;
    }
*/

    /**
     * Retrieves an ordered array of the <code>Instruction</code> objects
     * from the <code>ClassFile</code> object corresponding to the specified
     * <code>Method</code> object.
     * @param method the <code>Method</code> object for which to retrieve the
     *        corresponding ordered array of
     *        <code>Instruction</code> objects
     * @return an ordered array of the <code>Intruction</code> objects
     *        from the <code>ClassFile</code> object corresponding
     *        to the specified <code>Method</code> object, or
     *        <code>null</code> if such an array cannot be retrieved
     *        (e.g. the method is native or it is impossible to read
     *        the underlying class file resource within the current
     *        Database/JVM/Thread/SecurityManager context)
     */
/*
    private Instruction[] _getClazzInstructions(Method method) {

        if (method == null) return null;

        ClassFile               cf;
        Integer                 index;
        AttributeInfo           code;

        cf = _getClazzClassFile(method.getDeclaringClass().getName());

        if (cf == null) return null;

        index = (Integer) cf.hMembers.get(method.toString());

        if (index == null) return null;

        code = cf.methods[index.intValue()].code;

        return (code == null) ? null : code.instructions;
    }
*/

    /**
     * Retrieves, if possible, the original source code parameter names for the
     * specified <code>Method</code>object. <p>
     *
     * An attempt is made to read the local variable table from the
     * the <code>Method</code> object's class file resource. If no
     * corresponding class file can be found or the class file does not
     * contain a local variable table for the specified <code>Method</code>,
     * object, then <code>null</code> is returned. <p>
     * @param method the <code>Method</code> object for which to retrieve the
     *        original source code parameter names
     * @return a <code>String[]</code> array containing the the original source
     *        code parameter names in declaration order, or
     *        <code>null</code> if the retrieval is not possible
     */
/*
    private String[] _getClazzMethodParmNames(Method method) {

        ClassFile               cf;
        Integer                 index;
        MemberInfo              mi;
        AttributeInfo           code;
        String[]                names;
        int                     offset;
        String[]                parmNames;
        Class[]                 parmTypes;

        // no method, no parm names
        if (method == null) return null;

        cf = _getClazzClassFile(method.getDeclaringClass().getName());

        // no class file, no parm names
        if (cf == null) return null;

        index = (Integer) cf.hMembers.get(method.toString());

        // no member info, no parm names
        if (index == null) return null;

        mi = cf.methods[index.intValue()];

        // no member info, no parm names
        if (mi == null) return null;

        code = mi.code;

        // no code attribute, no parm names
        if (code == null) return null;

        names = code.slot_to_name;

        // no local variable table, no parm names
        if (names == null) return null;

        parmTypes = method.getParameterTypes();

        // if static, no "this" at slot zero
        offset = Modifier.isStatic(method.getModifiers()) ? 0 : 1;

        parmNames = new String[parmTypes.length];

        for (int i = 0; i < parmTypes.length; i++) {
            if (names[offset] == null) {
                offset++;
            }
            parmNames[i] = names[offset++];
        }

        return parmNames;

    }
*/

    /**
     * Retrieves the buffer length attribute of the <code>Column</code> object
     * (current value of the <code>_column</code> attribute), if any, under
     * consideration in the current executution context.
     *
     * The buffer length attribute is the maximum length in bytes of data,
     * if definitely known, that would be transferred to a buffer on a
     * SQL CLI fetch operation.  For numeric data, this size may be different
     * than the size of the data stored on the data source.  This value is the
     * same as the COLUMN_SIZE column for binary data. This value is the twice
     * the COLUMN_SIZE column for character data.  If the actual value is larger
     * than can be represented in an INTEGER column value, this is NULL.
     * @return buffer length attribute of the <code>Column</code> object currently under
     *    consideration
     */
    private Integer getColBufLen(Column column) {

        int size;

        switch (_iInternalType) {

            case I_CHAR :
            case I_CLOB :
            case I_LONGVARCHAR :
            case I_VARCHAR : {
                size = column.getSize();

                if (size == 0) {}
                else if (size > _HALF_MAX_INT) {
                    size = 0;
                } else {
                    size = 2 * size;
                }

                break;
            }
            case I_BINARY :
            case I_BLOB :
            case I_LONGVARBINARY :
            case I_VARBINARY : {
                size = column.getSize();

                break;
            }
            case I_BIGINT :
            case I_DOUBLE :
            case I_FLOAT :
            case I_DATE :
            case I_REAL :
            case I_TIME :
            case I_TIMESTAMP : {
                size = 8;

                break;
            }
            case I_INTEGER :
            case I_INTEGER_IDENTITY :
            case I_SMALLINT :
            case I_TINYINT : {
                size = 4;

                break;
            }
            case I_BIT : {
                size = 1;

                break;
            }
            default : {
                size = 0;

                break;
            }
        }

        return (size > 0) ? ValuePool.getInt(size)
                          : null;
    }

    /**
     * Retrieves the character octet length attribute of the <code>Column</code>
     * object (current value of the <code>_column</code> attribute), if any,
     * under consideration in the current executution context.
     * @return character octet length attribute of the <code>Column</code> object
     *    currently under consideration
     */
    private Integer getColCharOctLen(Column column) {

        int size;

        switch (_iInternalType) {

            case I_CHAR :
            case I_CLOB :
            case I_LONGVARCHAR :
            case I_VARCHAR : {
                size = column.getSize();

                if (size == 0) {}
                else if (size > _HALF_MAX_INT) {
                    size = 0;
                } else {
                    size = 2 * size;
                }

                break;
            }
            default : {
                size = 0;

                break;
            }
        }

        return (size == 0) ? null
                           : ValuePool.getInt(size);
    }

    /**
     * Retrieves the default value attribute of the <code>Column</code> object
     * (current value of the <code>_column</code> attribute), if any, under
     * consideration in the current executution context.
     * @return default value attribute of the <code>Column</code> object
     *    currently under consideration
     */
    private String getColDefault(Column column) {
        return (column == null) ? null
                                : column.getDefaultString();
    }

    /**
     * Retrieves the <code>java.sql.DatabaseMetaData.getColumns</code>
     * <code>IS_NULLABLE</code> column value for the <code>Column</code>
     * object (current value of the <code>_column</code> attribute), if any,
     * under consideration in the current executution context.<p>
     *
     * That is, if the column definitely does not allow <code>NULL</code>
     * values, then <code>"NO"</code> is returned, else if the column
     * might allow <code>NULL</code> values, then <code>"YES"</code> is
     * returned, while an empty string (<code>""</code>) is returned if
     * it is unknown whether the column might allow <code>NULL</code>
     * values.
     * @return "NO" if the <code>Column</code> object under consideration definitely
     *    does not allow NULL values; "YES" if it might allow NULL values;
     *    "" if it is not known whether or not it might allow NULL values.
     */
    private String getColIsNullable(Column column) {

        return (column == null) ? ""
                                : (column.isNullable() ||!column.isIdentity())
                                  ? "YES"
                                  : "NO";
    }

    /**
     * Retrieves the simple name the <code>Column</code> object
     * (current value of the <code>_column</code> attribute), if any,
     * under consideration in the current executution context.
     * @return the simple name the <code>Column</code> object under consideration.
     */
    private String getColName(Column column) {
        return (column == null) ? null
                                : column.columnName.name;
    }

    /**
     * Retrieves the simple name of the <code>Column</code> object corresponding
     * to the given <code>Table</code> object and column number.
     * @param t The <code>Table</code> object containing the <code>Column</code> object
     * @param i The column number of the column
     * @return the simple name of the <code>Column</code> object corresponding
     *    to the given <code>Table</code> object and column number.
     */
    private String _getColName(Table t, int i) {
        return t.getColumn(i).columnName.name;
    }

    /**
     * Retrieves the <code>java.sql.DatabaseMetaData.getColumns</code>
     * <code>NULLABLE</code> column value for the <code>Column</code>
     * object (current value of the <code>_column</code> attribute),
     * if any, under consideration in the current executution context.<p>
     *
     * That is, if the column might not allow <code>NULL</code>
     * values, then <code>columnNoNulls</code> is returned, else if the
     * column definitely allows <code>NULL</code> values, then
     * <code>columnNullable</code> is returned, while
     * <code>columnNullableUnknown</code> is returned if it is unknown
     * whether or not the column might allow <code>NULL</code> values.
     * @return the <code>java.sql.DatabaseMetaData.getColumns</code>
     *    <code>NULLABLE</code> column value for the <code>Column</code>
     *    object under consideration in the current executution context.<p>
     */
    private Integer getColNullability(Column column) {

        return (column == null) ? null
                                : (column.isNullable() &&!column.isIdentity())
                                  ? ValuePool.getInt(
                                      DatabaseMetaData.columnNullable)
                                  : ValuePool.getInt(
                                      DatabaseMetaData.columnNoNulls);
    }

    /**
     * Retrieves the localized column remarks corresponding to the
     * <code>Column</code> object (current value of the
     * <code>_column</code> attribute), if any, under consideration
     * in the current executution context.<p>
     * @return localized column remarks for the <code>Column</code> object under
     *    consideration in the current executution context.<p>
     */
    private String getColRemarks(Column column) {

        return (_isSystemTable)
               ? BundleHandler.getString(_rbHndTColumnRemarks,
                                         _tableName + "_"
                                         + getColName(column))
               : null;
    }

    /**
     * Retrieves the scale attribute of the <code>Column</code> object
     * (current value of the <code>_column</code> attribute), if any,
     * under consideration in the current executution context.
     * @return scale attribute of the <code>Column</code> object
     *    under consideration in the current executution
     *    context.
     */
    private Integer getColScale(Column column) {

        int internalType = getInternalType(column);

        switch (internalType) {

            case I_DECIMAL :
            case I_NUMERIC : {
                return ValuePool.getInt(column.getScale());
            }
            default :
                return getTIDefScale(internalType);
        }
    }

    /**
     * Retrieves the scope catalog attribute of the <code>Column</code> object
     * (current value of the <code>_column</code> attribute), if any, under
     * consideration in the current executution context.
     * @return scope catalog attribute of the <code>Column</code> object
     *    under consideration in the current executution
     *    context.
     */
    private String _getColScopeCat() {
        return null;
    }

    /**
     * Retrieves the scope schema attribute of the <code>Column</code> object
     * (current value of the <code>_column</code> attribute), if any, under
     * consideration in the current executution context.
     * @return scope schema attribute of the <code>Column</code> object
     *    under consideration in the current executution
     *    context.
     */
    private String _getColScopeSchem() {
        return null;
    }

    /**
     * Retrieves the scope table attribute of the <code>Column</code> object
     * (current value of the <code>_column</code> attribute), if any, under
     * consideration in the current executution context.
     * @return scope table attribute of the <code>Column</code> object
     *    under consideration in the current executution
     *    context.
     */
    private String _getColScopeTable() {
        return null;
    }

    /**
     * Retrieves the size attribute of the <code>Column</code> object
     * (current value of the <code>_column</code> attribute), if any,
     * under consideration in the current executution context.  <p>
     *
     * <UL
     * <LI>For columns whose data type allows the <code>length</code>
     * create parameter, if the length is defined for the column then it is
     * returned, else the maximum possible length, in characters, for the column's
     * data type is returned. If the real maximum is >
     * <code>Integer.MAX_VALUE</code>, null is returned.
     *
     * <LI>For columns whose data type allows the <code>precision,scale</code> create
     * parameter pair, if the precision value is defined for the column then it
     * is returned, else the maximum or default precision, if any, for the column's
     * data type is returned. If the real maximum is >
     * <code>Integer.MAX_VALUE</code>, null is returned.
     *
     * <LI>For columns whose data type is numeric but does not allow the
     * <code>precision,scale</code> create  parameter pair, the maximum
     * numeric precision of the data type is returned.
     *
     * <LI>For columns whose data type does not allow either the <code>length</code>
     * create parameter or the <code>precision,scale</code> create
     * parameter pair and whose data type is not strictly numeric (i.e.
     * datetime columns), the maximum possible length, in characters, for the
     * data type is returned. If the real maximum is >
     * <code>Integer.MAX_VALUE</code>, null is returned.
     * </UL>
     * @return size attribute of the <code>Column</code> object
     *    under consideration in the current executution
     *    context.  <p>
     */
    private Integer getColSize(Column column) {

        int size;

        switch (getInternalType(column)) {

            // sized or decimal types
            case I_BINARY :
            case I_BLOB :
            case I_CHAR :
            case I_CLOB :
            case I_DECIMAL :
            case I_LONGVARBINARY :
            case I_LONGVARCHAR :
            case I_NUMERIC :
            case I_VARBINARY :
            case I_VARCHAR : {
                size = column.getSize();

                break;
            }
            default : {
                size = 0;

                break;
            }
        }

        return (size == 0) ? _getTIPrec()
                           : ValuePool.getInt(size);
    }

    /**
     * Retrieves an <code>Enumeration</code> object whose elements form the set of
     * all distinct <code>Table</code> objects in this database, including the
     * list of system table surrogates maintained by this object.
     * @throws SQLException if a database access error occurs
     * @return the set of all distinct <code>Table</code> objects in this database, as an
     * <code>Enumeration</code> object
     */
    private Enumeration _enumerateAllTables() throws SQLException {
        return new CompositeEnumeration(_enumerateUserTables(),
                                        enumerateSysTables());
    }

    /**
     * Retrieves an <code>Enumeration</code> object whose elements form the
     * set of distinct names of all catalogs visible in the current execution
     * context. <p>
     *
     * <b>Note:</b> in the present implementation, the returned
     * <code>Enumeration</code> object is a <code>SingletonEnumeration</code>
     * whose single element is <code>_databaseName</code>.  HSQLDB currently does
     * not support the concept a single engine hosting multiple catalogs.
     * @return An enumeration of <code>String</code> objects naming the
     *      catalogs visible to the specified <code>Session</code>
     * @throws SQLException never (reserved for future use)
     */
    private Enumeration _enumerateCatalogNames() throws SQLException {
        return new SingletonEnumeration(_databaseName);
    }

    /**
     * Retrieves an <code>Enumeration</code> over the set of distinct
     * <code>Class</code> FQNs that have both been registered via the
     * HSQLDB-specific SQL command: <code>GRANT xxx on CLASS "..."</code>
     * and that are also accessible within the current execution context.
     * @return an <code>Enumeration</code> object whose elements are the set of
     *        distinct <code>Class</code> FQNs registered with the system
     *        via the SQL <code>Class</code> grant mechanism and that
     *        are accessible as SQL functions / stored procedures in the current
     *        execution context.
     * @throws SQLException if a database access error occurs
     */
    private Enumeration _enumerateGrantedClassNames() throws SQLException {
        return _session.getGrantedClassNames(true).elements();
    }

    /**
     * Retrieves an <code>Enumeration</code> object describing the Java
     * <code>Method</code> objects that are both the entry points
     * to executable SQL database objects, such as SQL functions and
     * stored procedures, and that are accessible within the current
     * execution context.
     *
     * Each element of the <code>Enumeration</code> is an <code>Object[3]</code>
     * whose elements are: <p>
     *
     * <ol>
     * <li>a <code>Method</code> object.
     * <li>an <code>HsqlArrayList</code> object whose elements are the SQL call
     *     aliases for the method.
     * <li>the <code>String</code> "ROUTINE"
     * </ol>
     *
     * <b>Note:</b> Admin users are actually free to invoke *any* public
     * static non-abstract Java Method that can be found through the database
     * class loading process, either as a SQL stored procedure or SQL function,
     * as long as its parameters and return type are compatible with the engine's
     * supported SQL type / Java <code>Class</code> mappings. <p>
     * @return <code>Enumeration</code> object whose elements represent the set of
     *         distinct <code>Method</code> objects accessible as
     *         executable as SQL routines within the current execution
     *         context.<p>
     *
     *         Elements are <code>Object[]</code> instances, with [0] being a
     *         <code>Method</code> object, [1] being an alias list object and
     *         [2] being the <code>String</code> "ROUTINE"<p>
     *
     *         If the <code>Method</code> object at index [0] has aliases,
     *         and the <code>andAliases</code> parameter was specified
     *         as <code>true</code>, then there is an alias list
     *         at index [1] whose elements are <code>String</code> objects
     *         whose values are the SQL call aliases for the method.
     *         Otherwise, the value of index [1] is <code>null</code>.
     * @param className The fully qualified name of the class for which to
     * retrive the enumeration
     * @param andAliases if <code>true</code>, qualifying <code>Method</code>
     *        alias lists for qualifting methods are additionally
     *        retrieved.
     * @throws SQLException if a database access error occurs
     */
    private Enumeration _enumerateAllRoutineMethods(String className,
            boolean andAliases) throws SQLException {

        Class         clazz;
        Method[]      methods;
        Method        method;
        int           mods;
        Object[]      info;
        HsqlArrayList aliasList;
        HsqlHashSet   methodSet;
        HsqlHashMap   invAliasMap;

        // we want all methods listed into an enumerable set
        methodSet   = new HsqlHashSet();
        invAliasMap = andAliases ? _getInverseAliasMap()
                                 : null;

        try {
            clazz = _classForName(className);
        } catch (ClassNotFoundException e) {
            return methodSet.elements();
        }

        // we are interested in inherited methods too,
        // so we use getDeclaredMethods() first.
        // However, under Applet execution or
        // under restrictive SecurityManager policies
        // this may fail, so we use getMethods()
        // if getDeclaredMethods() fails.
        try {
            methods = clazz.getDeclaredMethods();
        } catch (Exception e) {
            methods = clazz.getMethods();
        }

        // add all public static methods to the set
        for (int i = 0; i < methods.length; i++) {
            method = methods[i];
            mods   = method.getModifiers();

            if (!(Modifier.isPublic(mods) && Modifier.isStatic(mods))) {
                continue;
            }

            info = new Object[] {
                method, null, "ROUTINE"
            };

            if (andAliases) {
                info[1] = invAliasMap.get(_getMethodFQN(method));
            }

            methodSet.add(info);
        }

        // return the enumeration
        return methodSet.elements();
    }

    /**
     * Retrieves an <code>Enumeration</code> object describing the Java
     * distinct <code>Method</code> objects that are both the entry points
     * to trigger body implementations and that are accessible (can potentially
     * be fired) within the current execution context. <p>
     *
     * The elements of the Enumeration have the same format as those for
     * {@link #_enumerateAllRoutineMethods}, except that position [1] of the
     * Object[] is always null (there are no aliases for trigger bodies)
     * and position [2] is always "TRIGGER"
     * @return an <code>Enumeration</code> object describing the Java
     * <code>Method</code> objects that are both the entry points
     * to trigger body implementations and that are accessible (can potentially
     * be fired) within the current execution context.
     * @throws SQLException if a database access error occurs.
     */
    private Enumeration _enumerateAccessibleTriggerMethods()
    throws SQLException {

        Table           table;
        Class           clazz;
        Method          method;
        HsqlArrayList   methodList;
        HsqlHashSet     dupCheck;
        Class[]         pTypes;
        TriggerDef      triggerDef;
        HsqlArrayList[] triggerLists;
        HsqlArrayList   triggerList;
        int             listSize;

        pTypes     = new Class[] {
            String.class, String.class, Object[].class
        };
        methodList = new HsqlArrayList();
        dupCheck   = new HsqlHashSet();

        for (int i = 0; i < _userTableList.size(); i++) {
            table = (Table) _userTableList.get(i);

            // Not any more.  SYSTEM_LOBS is a system table in the
            // "user" table list.
            //if (table.tableType == Table.SYSTEM_TABLE) continue;
            if (!_session.isAccessible(table.getName())) {
                continue;
            }

            triggerLists = table.vTrigs;

            if (triggerLists == null) {
                continue;
            }

            for (int j = 0; j < triggerLists.length; j++) {
                triggerList = triggerLists[j];

                if (triggerList == null) {
                    continue;
                }

                listSize = triggerList.size();

                for (int k = 0; k < listSize; k++) {
                    try {
                        triggerDef = (TriggerDef) triggerList.get(k);

                        if (triggerDef == null) {
                            continue;
                        }

                        clazz = triggerDef.trig.getClass();

                        if (dupCheck.contains(clazz.getName())) {
                            continue;
                        } else {
                            dupCheck.add(clazz.getName());
                        }

                        method = clazz.getMethod("fire", pTypes);

                        methodList.add(new Object[] {
                            method, null, "TRIGGER"
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return methodList.elements();
    }

    /**
     * Retrieves an <code>Enumeration</code> object describing the
     * fully qualified names of all Java <code>Class</code> objects
     * that are both trigger body implementations and that are accessible
     * (whose fire method can potentially be invoked) by actions upon the
     * database by the specified <code>User</code>. <p>
     * @param user the <code>User</code> for which to retrieve the <code>Enumeration</code>
     * @throws SQLException if a database access error occurs
     * @return an <code>Enumeration</code> object describing the
     *        fully qualified names of all Java <code>Class</code>
     *        objects that are both trigger body implementations
     *        and that are accessible (whose fire method can
     *        potentially be invoked) by actions upon the database by the
     *        specified <code>User</code>.
     */
    private Enumeration _enumerateAccessibleTriggerClassNames(User user)
    throws SQLException {

        Table           table;
        Class           clazz;
        HsqlHashSet     classSet;
        TriggerDef      triggerDef;
        HsqlArrayList[] triggerLists;
        HsqlArrayList   triggerList;
        int             listSize;

        classSet = new HsqlHashSet();

        for (int i = 0; i < _userTableList.size(); i++) {
            table = (Table) _userTableList.get(i);

            if (!user.isAccessible(table.getName())) {
                continue;
            }

            triggerLists = table.vTrigs;

            if (triggerLists == null) {
                continue;
            }

            for (int j = 0; j < triggerLists.length; j++) {
                triggerList = triggerLists[j];

                if (triggerList == null) {
                    continue;
                }

                listSize = triggerList.size();

                for (int k = 0; k < listSize; k++) {
                    triggerDef = (TriggerDef) triggerList.get(k);

                    if (triggerDef == null ||!triggerDef.valid
                            || triggerDef.trig == null
                            ||!user.isAccessible(
                                table, TriggerDef.indexToRight(k))) {
                        continue;
                    }

                    classSet.add(triggerDef.trig.getClass().getName());
                }
            }
        }

        return classSet.elements();
    }

    /**
     * Retrieves a composite enumeration consiting of the elements from
     * {@link #_enumerateAllRoutineMethods} and
     * {@link #_enumerateAllTriggerMethods}.
     * @param andAliases true if the alias lists for routine method elements are to be generated.
     * @throws SQLException if a database access error occurs
     * @return a composite enumeration consiting of the elements from
     * {@link #_enumerateAllRoutineMethods} and
     * {@link #_enumerateAllTriggerMethods}.
     */
    private Enumeration _enumerateAllAccessibleMethods(boolean andAliases)
    throws SQLException {

        Enumeration out;
        Enumeration classNames;
        Enumeration methods;
        String      className;

        out        = EmptyEnumeration.instance;
        classNames = _enumerateGrantedClassNames();

        while (classNames.hasMoreElements()) {
            className = (String) classNames.nextElement();
            methods   = _enumerateAllRoutineMethods(className, andAliases);
            out       = new CompositeEnumeration(out, methods);
        }

        return new CompositeEnumeration(out,
                                        _enumerateAccessibleTriggerMethods());
    }

    /**
     * Retrieves an enumeration of the names of schemas visible in the context of
     * the specified <code>Session</code>. <p>
     *
     * @return An enumeration of <code>Strings</code> naming the catalogs
     *      visible to the specified <code>Session</code>
     */
    private Enumeration _enumerateSchemaNames() {

        HsqlArrayList schemaList = new HsqlArrayList();

        schemaList.add(QS_DEFN_SCHEMA);
        schemaList.add(QS_INFO_SCHEMA);
        schemaList.add(QS_PUB_SCHEMA);
        schemaList.add(_session.getUsername());

        return schemaList.elements();
    }

    /**
     * Retrieves an <code>Enumeration</code> object whose elements are all of the
     * <code>Table</code> objects from the repository of user tables maintained
     * by the datatabase for which this object is producing tables.
     * @return an <code>Enumeration</code> object whose elements are
     *        all of the <code>Table</code> objects from the
     *        repository of user tables maintained by the
     *        datatabase for which this object is producing
     *        tables.
     */
    private Enumeration _enumerateUserTables() {
        return _userTableList.elements();
    }

    /**
     * Retrieves the cardinality of the database object specified by the
     * <code>o</code> argument. <p>
     *
     * Currently, this operation is empty and always returns null.
     * @param o the database object for which to retrieve the cardinality.
     * @return The cardinality of the specified database object
     */
    private Integer _getIndexInfoCardinality(Object o) {

        // not supported yet: need cardinality attribute
        // in Index objects first, or system statistics table
        // and ANALYZE command to update values in system
        // statistics table
        return null;
    }

    /**
     * Retrieves the index direction ("A" for ascending, "D" for descending)
     * of the column position specified by the <code>columnPosition</code>
     * argument for the <code>Index</code> object specified by the
     * <code>index</code> argument.
     * @param index The Index obect against which to perform the query
     * @param columnPosition the column position for which to perform the query
     * @return the index direction ("A" for ascending, "D" for descending)
     */
    private String _getIndexInfoColDirection(Index index,
            int columnPosition) {

        // so far, hsqldb only supports completely ascending indexes
        return "A";
    }

    /**
     * Retrieves the number of storage pages consumed by the specified database
     * object. <p>
     *
     * Currently, this is an empty method and always returns null.
     * @param o the database object for which to make the determination.
     * @return the number of storage pages consumed by the specified database
     * object.
     */
    private Integer _getIndexInfoPages(Object o) {

        // not supported yet: hsqldb does not even know what a "page" is
        return null;
    }

    /**
     * Retrieves a map from <code>Method</code> FQN values to a list of SQL call
     * alaises for the method, as known within the current execution context. <p>
     * @return map from <code>Method</code> FQNs to SQL call alias list
     * @throws SQLException if a database access error occurs
     */
    private HsqlHashMap _getInverseAliasMap() throws SQLException {

        HsqlHashMap   hAlias;
        HsqlHashMap   hInverseAlias;
        Enumeration   keys;
        Object        key;
        Object        value;
        HsqlArrayList aliasList;

        // PRE:  we assume database.getAlias() never returns null
        hAlias        = database.getAlias();
        hInverseAlias = new HsqlHashMap();
        keys          = hAlias.keys();

        while (keys.hasMoreElements()) {
            key       = keys.nextElement();
            value     = hAlias.get(key);
            aliasList = (HsqlArrayList) hInverseAlias.get(value);

            if (aliasList == null) {
                aliasList = new HsqlArrayList();

                hInverseAlias.put(value, aliasList);
            }

            aliasList.add(key);
        }

        return hInverseAlias;
    }

    /**
     * Retrieves the fully qualified name of the specified method object. <p>
     *
     * This is a convenience method that is equivalent to the expression: <p>
     *
     * <code>method.getDeclaringClass.getName() + "." + method.getName()</code>
     * @param method the Method object whose fully qualified name is to be retrieved.
     * @return the fully qualified name of the specified method object
     */
    private String _getMethodFQN(Method method) {
        return _getSB().append(method.getDeclaringClass().getName()).append(
            '.').append(method.getName()).toString();
    }

    /**
     * Retrieves the declaring <code>Class</code> object for the specified
     * fully qualified method name, using, if possible, the classLoader
     * attribute of the database for which this object is producing tables.<p>
     *
     * This is a convenience method that is roughly equivalent to the
     * expression: <p>
     *
     * <pre>
     * (database.classLoader == null) ?
     * Class.forName(fqn.substring(0,fqn.lastIndexOf('.'))
     * Class.forName(fqn.substring(0,fqn.lastIndexOf('.'),true, database.classLoader)
     * </pre>
     * @param fqn the fully qualified name of the method for which to retrieve the
     *        declaring <code>Class</code> object.
     * @return the declaring <code>Class</code> object for the specified fully
     *        qualified method name
     */
    private Class _getClassForMethodFQN(String fqn) {

        try {
            return _classForName(fqn.substring(0, fqn.lastIndexOf('.')));
        } catch (Exception e) {}

        return null;
    }

    /**
     * Retrieves the column value for the SYSTEM_PROCEDURECOOLUMNS.COLUMN_LENGTH
     * column
     * @return column value for the SYSTEM_PROCEDURECOOLUMNS.COLUMN_LENGTH
     * column
     */
    private Integer _getPColLen() {

        int size;

        switch (_iInternalType) {

            case I_BINARY :
            case I_LONGVARBINARY :
            case I_VARBINARY : {
                size = Integer.MAX_VALUE;

                break;
            }
            case I_BIGINT :
            case I_DOUBLE :
            case I_DATE :
            case I_TIME :
            case I_TIMESTAMP : {
                size = 8;

                break;
            }
            case I_FLOAT :
            case I_REAL :
            case I_INTEGER :
            case I_INTEGER_IDENTITY : {
                size = 4;

                break;
            }
            case I_SMALLINT : {
                size = 2;

                break;
            }
            case I_TINYINT :
            case I_BIT : {
                size = 1;

                break;
            }
            default :
                size = 0;
        }

        return (size == 0) ? null
                           : ValuePool.getInt(size);
    }

    /**
     * Retrieves the nullability of the Class of the
     * procedure column under current consideration.
     * @return
     */
    private Integer _getPColNullability() {

        return _class.isPrimitive()
               ? ValuePool.getInt(DatabaseMetaData.procedureNoNulls)
               : ValuePool.getInt(DatabaseMetaData.procedureNullable);
    }

    /**
     * @return
     */
    private Integer _getPColType() {
        return ValuePool.getInt(DatabaseMetaData.procedureColumnIn);
    }

    /**
     * @return
     * @param parms
     */
    private Integer _getProcInputParmCount(Class[] parms) {
        return ValuePool.getInt(parms.length);
    }

    /**
     * @param fqn
     * @param alias
     * @return
     */
    private String _getProcName(String fqn, String alias) {
        return (alias == null) ? fqn
                               : alias;
    }

    /**
     * @return
     * @param clazz
     * @param alias
     */
    private String _getProcOrigin(boolean alias, String src, Class c) {

        return (alias) ? "ALIAS"
                       : ("ROUTINE".equals(src)
                          ? (_isBuiltinLib(c) ? "BUILTIN "
                                              : "USER DEFINED ") + src
                          : src);
    }

    /**
     * @return
     * @param parms
     */
    private Integer _getProcOutputParmCount(Class[] parms) {
        return ValuePool.getInt(0);
    }

    /**
     * @param m
     * @return
     */
    static String _getProcRemarkKeyPrefix(Method m) {

        try {
            StringBuffer sb = new StringBuffer();

            sb.append(m.getName()).append('(');

            Class[] params = m.getParameterTypes();

            for (int j = 0; j < params.length; j++) {
                sb.append(params[j].getName());

                if (j < (params.length - 1)) {
                    sb.append(",");
                }
            }

            sb.append(')');

            String s = sb.toString();

            //System.out.println(s);
            return s;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @param keyPrefix
     * @param parmNum
     * @return
     */
    static String _getProcRemarkKey(String keyPrefix, int parmNum) {
        return (parmNum < 0) ? keyPrefix
                             : keyPrefix + '@' + parmNum;
    }

    /**
     * @param methodFQN
     * @return
     */
    private String _getProcRemark(int hnd, String key) {

        //System.out.println(key);
        return BundleHandler.getString(hnd, key);
    }

    /**
     * @return
     * @param c
     */
    private Integer _getProcResultSetCount(Class returnType) {
        return (returnType == Void.TYPE) ? ValuePool.getInt(0)
                                         : ValuePool.getInt(1);
    }

    /**
     * @param clazz
     * @return
     */
    private Integer _getProcType(Class returnType, String methodOrigin) {

        if (methodOrigin == null ||!"ROUTINE".equals(methodOrigin)) {
            return ValuePool.getInt(DatabaseMetaData.procedureResultUnknown);
        }

        return (returnType == null)
               ? ValuePool.getInt(DatabaseMetaData.procedureResultUnknown)
               : Void.TYPE.isAssignableFrom(returnType)
                 ? ValuePool.getInt(DatabaseMetaData.procedureNoResult)
                 : ValuePool.getInt(DatabaseMetaData.procedureReturnsResult);
    }

    /**
     * Retrieves the name of the schema corresponding to the indicated object,
     * in the context of the specified <code>Session</code>.<p>
     *
     * The currrent implementation makes the determination as follows: <p>
     *
     * <OL>
     * <LI> if the specifed object is <code>null</code>, then <code>null</code> is
     *      returned.
     *
     * <LI> if the specified object is a <code>java.lang.reflect.Method</code>
     *      instance, then "PUBLIC" is returned.
     *
     * <LI> if the specified object is an <code>org.hsqldb.Index</code> instance,
     *      then either the name of the schema of the table containing the index
     *      is returned, or null is returned if no table containing the index
     *      object can be found in the context of the specified
     *      <code>Database</code> object.
     *
     * <LI> if the specified object is an <code>org.hsqldb.Table</code> instance and
     *      it is a system table, then "DEFINITION_SCHEMA" is returned.
     *
     * <LI> if the specified object is an <code>org.hsqldb.Table</code> instance and
     *      it is a temp table, then either the name of the session user is returned
     *      (if the specified <code>Session</code> object is non-null) or null is
     *      returned (if the specified <code>Session</code> object is null).
     *
     * <LI> if the specified object is an <code>org.hsqldb.Table</code> instance
     *      and is a system view, then "INFORMATION_SCHEMA" is returned.
     *
     * <LI> if the specified object is an <code>org.hsqldb.Table</code> instance and
     *      it is a user table, then "PUBLIC" is returned.
     * </OL> <p>
     * @return the specified object's catalog name, or null if any paramter
     *          is null.
     * @param o the object whose schema name is to be retrieved
     */
    private String _getSchemaName(Object o) {

        if (o == null) {
            return null;
        }

        if (o instanceof String) {
            return _isBuiltinLib((String) o) ? QS_DEFN_SCHEMA
                                             : QS_PUB_SCHEMA;
        }

        Class c = null;

        if (o instanceof Method) {
            c = ((Method) o).getDeclaringClass();
        } else if (o instanceof Class) {
            c = (Class) o;
        }

        if (c != null) {
            return (_isBuiltinLib(c)) ? QS_DEFN_SCHEMA
                                      : QS_PUB_SCHEMA;
        }

        Table table = null;

        if (o instanceof Table) {
            table = (Table) o;
        } else if (o instanceof Index) {
            table = _tableForIndex((Index) o);
        }

        if (table == null) {
            return null;
        } else if (table.tableType == Table.SYSTEM_VIEW) {
            return QS_INFO_SCHEMA;
        } else if (table.tableType == Table.SYSTEM_TABLE) {
            return QS_DEFN_SCHEMA;
        } else if (table.isTemp()) {
            return _session.getUsername();
        } else {
            return QS_PUB_SCHEMA;
        }
    }

    /**
     * @return
     */
    private HsqlStringBuffer _getSB() {

        _sb.setLength(0);

        return _sb;
    }

    /**
     * @return
     */
    private boolean _isTablePkVisible() {

        int[] iPrimaryKey = _table.iPrimaryKey;

        if (iPrimaryKey == null) {
            return false;
        }

        HsqlName cn = _table.getColumn(iPrimaryKey[0]).columnName;

        return cn != null && cn.name != null && cn.name.length() > 0;
    }

    /**
     * @return
     */
    private Enumeration _enumerateTableIndicies() {

        Enumeration   e;
        HsqlArrayList vIndex;

        if (_isTablePkVisible()) {
            e = _table.vIndex.elements();
        } else if (_table.vIndex.size() == 0) {
            e = null;
        } else {
            vIndex = _table.vIndex;

            HsqlArrayList list         = new HsqlArrayList(vIndex.size() - 1);
            Object        primaryIndex = _table.getPrimaryIndex();

            for (int i = 0; i < vIndex.size(); i++) {
                Object element = vIndex.get(i);

                if (element != primaryIndex) {
                    list.add(element);
                }
            }

            e = list.elements();
        }

        return e;
    }

    /**
     * @throws SQLException
     * @return
     */
/*
    private Object[] _getBRIInfo() throws SQLException {

        HsqlArrayList vIndex;
        HsqlArrayList vColumn;
        Index         index;
        int           nnullCount;
        int[]         cols;
        HsqlHashMap   hAltKeys;
        HsqlHashMap   hUniqueCandidates;
        Integer       key;
        boolean       akFound = false;
        Object[]      info    = null;

        if (_isTablePkVisible()) {
            return new Object[] {
                Boolean.TRUE, _table.iPrimaryKey
            };
        } else {
            hAltKeys          = new HsqlHashMap();
            hUniqueCandidates = new HsqlHashMap();
            vIndex            = _table.vIndex;

            for (int i = 0; i < vIndex.size(); i++) {
                index = (Index) vIndex.get(i);

                if (index == _table.getPrimaryIndex() ||!index.isUnique()) {
                    continue;
                }

                nnullCount = 0;
                cols       = index.getColumns();
                vColumn    = _table.vColumn;

                for (int j = 0; j < cols.length; j++) {
                    if (((Column) vColumn.get(cols[j])).isNullable()) {
                        continue;
                    }

                    nnullCount++;
                }

                if (nnullCount == cols.length) {
                    akFound = true;
                    key     = ValuePool.getInt(nnullCount);

                    if (!hAltKeys.containsKey(key)) {
                        hAltKeys.put(key, cols);
                    }
                } else if (nnullCount > 0 &&!akFound) {
                    key = ValuePool.getInt(nnullCount);

                    if (!hUniqueCandidates.containsKey(key)) {
                        hUniqueCandidates.put(key, cols);
                    }
                }
            }
        }

        if (akFound) {
            Object[] sorted = ArrayUtil.getSortedKeys(hAltKeys);

            if (sorted.length > 0) {
                info = new Object[] {
                    Boolean.TRUE, hAltKeys.get(sorted[0])
                };
            }
        } else if (hUniqueCandidates.size() > 0) {
            Object[] sorted = ArrayUtil.getSortedKeys(hUniqueCandidates);

            if (sorted.length > 0) {
                info = new Object[] {
                    Boolean.FALSE, hUniqueCandidates.get(sorted[0])
                };
            }
        }

        return info;
    }
*/

    /**
     * @throws SQLException
     * @return
     */
/*
    private Object[] _getBRIInfo_version2() throws SQLException {

        HsqlArrayList vIndex;
        HsqlArrayList vColumn;
        Index         index;
        int[]         cols;
        int           nnullCount;
        Result        rAK;
        int           bestAkColCount;
        boolean       akFound;
        Result        rUQ;
        int           bestUqColCount;
        double        typeFactor;
        Object[]      info;

        info = null;

        if (_isTablePkVisible()) {
            return new Object[] {
                Boolean.TRUE, _table.iPrimaryKey
            };
        } else {
            rAK            = new Result(3);
            rAK.colType    = new int[] {
                Types.INTEGER, Types.DOUBLE
            };
            rUQ            = new Result(4);
            rUQ.colType    = new int[] {
                Types.INTEGER, Types.INTEGER, Types.DOUBLE
            };
            vIndex         = _table.vIndex;
            akFound        = false;
            bestAkColCount = Integer.MAX_VALUE;
            bestUqColCount = bestAkColCount;

            for (int i = 0; i < vIndex.size(); i++) {
                index = (Index) vIndex.get(i);

                if (index == _table.getPrimaryIndex() ||!index.isUnique()) {
                    continue;
                }

                nnullCount = 0;
                typeFactor = 1D;
                cols       = index.getColumns();
                vColumn    = _table.vColumn;

                for (int j = 0; j < cols.length; j++) {
                    Column c = (Column) vColumn.get(cols[j]);

                    if (c.isNullable()) {
                        continue;
                    }

                    nnullCount++;

                    //typeFactor *= _getBRITypeFactor(c);
                }

                if (nnullCount == cols.length
                        && cols.length <= bestAkColCount) {
                    rAK.add(new Object[] {
                        new Integer(cols.length), new Double(typeFactor), cols
                    });

                    akFound        = true;
                    bestAkColCount = cols.length;
                } else if (!akFound && nnullCount > 0
                           && cols.length <= bestUqColCount) {
                    rUQ.add(new Object[] {
                        new Integer(cols.length), new Integer(nnullCount),
                        new Double(typeFactor), cols
                    });

                    bestUqColCount = cols.length;
                }
            }
        }

        if (akFound) {
            rAK.sortResult(new int[] {
                0, 1
            }, new int[] {
                1, 1
            });

            //rAK.print();
            info = new Object[] {
                Boolean.TRUE, rAK.rRoot.data[2]
            };
        } else if (rUQ.getSize() > 0) {
            rUQ.sortResult(new int[] {
                0, 1, 2
            }, new int[] {
                1, -1, 1
            });

            //rUQ.print();
            info = new Object[] {
                Boolean.FALSE, rUQ.rRoot.data[3]
            };
        }

        return info;
    }
*/

    /**
     * @return
     */
    private String _getTableCachePath() {

        return (_table.cCache == null) ? null
                                       : new java.io.File(_table.cCache.sName)
                                           .getAbsolutePath();
    }

    /**
     * @return
     */
    private String _getTableName() {
        return _table.getName().name;
    }

    /**
     * @return
     */
    private Long _getTableNextIdentity() {

        return (_table.iIdentityColumn > -1 && _isTablePkVisible())
               ? ValuePool.getLong(_table.iIdentityId)
               : null;
    }

    /**
     * @return
     */
    private Boolean _getTableIsReadOnly() {
        return valueOf(_table.isDataReadOnly());
    }

    /**
     * @return
     * @throws SQLException
     */
    private String _getTableRemark() throws SQLException {

        return (_isSystemTable)
               ? BundleHandler.getString(_rbHndTableRemarks, _tableName)
               : null;
    }

    /**
     * @throws SQLException
     * @return
     */
    private String _getTableDataSource() throws SQLException {
        return _table.getDataSource();
    }

    /**
     * @throws SQLException
     * @return
     */
    private Boolean _getTableIsDescDataSource() throws SQLException {
        return valueOf(_table.isDescDataSource());
    }

    /**
     * @return
     */
    private String _getTableType() {

        switch (_table.tableType) {

            case Table.VIEW :
                return "VIEW";

            case Table.TEMP_TABLE :
            case Table.TEMP_TEXT_TABLE :
                return "GLOBAL TEMPORARY";

            case Table.SYSTEM_TABLE :
                return "SYSTEM TABLE";

            default :
                return "TABLE";
        }
    }

    /**
     * @return
     */
    private String _getTableHsqlType() {

        switch (_table.tableType) {

            case Table.SYSTEM_TABLE :
            case Table.MEMORY_TABLE :
            case Table.TEMP_TABLE :
                return "MEMORY";

            case Table.CACHED_TABLE :
                return _table.isCached() ? "CACHED"
                                         : "MEMORY";

            case Table.TEMP_TEXT_TABLE :
            case Table.TEXT_TABLE :
                return "TEXT";

            case Table.VIEW :
            default :
                return null;
        }
    }

    /**
     * @return
     */
    private Boolean _getTIAutoIncrement() {

        switch (_iInternalType) {

            case I_BIGINT :
            case I_DECIMAL :
            case I_DOUBLE :
            case I_FLOAT :
            case I_INTEGER :
            case I_NUMERIC :
            case I_REAL :
            case I_SMALLINT :
            case I_TINYINT :
                return Boolean.FALSE;

            case I_INTEGER_IDENTITY :
                return Boolean.TRUE;

            default :
                return null;
        }
    }

    /**
     * @return
     */
    private Boolean _getTICaseSensitive() {

        switch (_iInternalType) {

            case I_ARRAY :
            case I_BLOB :
            case I_CLOB :
            case I_DISTINCT :
            case I_JAVA_OBJECT :
            case I_NULL :
            case I_REF :
            case I_STRUCT :
                return null;

            case I_CHAR :
            case I_DATALINK :
            case I_LONGVARCHAR :
            case I_OTHER :
            case I_VARCHAR :
            case I_XML :
                return Boolean.TRUE;

            default :
                return Boolean.FALSE;
        }
    }

    /**
     * @return
     */
    private Integer _getTICharOctLen() {
        return null;
    }

    /**
     * @return
     */
    private Long _getTICharOctLenAct() {

        switch (_iInternalType) {

            case I_CHAR :
            case I_LONGVARCHAR :
            case I_VARCHAR :
            case I_VARCHAR_IGNORECASE :
                return ValuePool.getLong(2L * Integer.MAX_VALUE);

            case I_CLOB :
                return ValuePool.getLong(Long.MAX_VALUE);

            default :
                return null;
        }
    }

    /**
     * @return
     */
    private String _getTIColStClsName() {

        switch (_iInternalType) {

            case I_BIGINT :
                return "java.lang.Long";

            case I_BINARY :
            case I_LONGVARBINARY :
            case I_OTHER :
            case I_VARBINARY :
                return "[B";

            case I_BIT :
                return "java.lang.Boolean";

            case I_CHAR :
            case I_LONGVARCHAR :
            case I_VARCHAR :
            case I_VARCHAR_IGNORECASE :
            case I_XML :
                return "java.lang.String";

            case I_DATALINK :
                return "java.net.URL";

            case I_DATE :
                return "java.sql.Date";

            case I_DECIMAL :
            case I_NUMERIC :
                return "java.math.BigDecimal";

            case I_DOUBLE :
            case I_FLOAT :
            case I_REAL :
                return "java.lang.Double";

            case I_INTEGER :
            case I_INTEGER_IDENTITY :
            case I_SMALLINT :
            case I_TINYINT :
                return "java.lang.Integer";

            case I_TIME :
                return "java.sql.Time";

            case I_TIMESTAMP :
                return "java.sql.Timestamp";

            default :
                return null;
        }
    }

    /**
     * @return
     */
    private Boolean _getTIColStClsSup() {

        return (_iInternalType == I_NULL) ? Boolean.TRUE
                                          : valueOf(_getTIColStClsName()
                                          != null);
    }

    /**
     * Retrieves a localized verision of the type's create parameters, for
     * display purposes only.
     *
     * @return a localized verision of the type's create parameters
     */
    private String _getTICreateParams() {

        String key;

        switch (_iInternalType) {

            case I_BINARY :
            case I_BLOB :
            case I_CHAR :
            case I_CLOB :
            case I_LONGVARBINARY :
            case I_LONGVARCHAR :
            case I_VARBINARY :
            case I_VARCHAR :
            case I_VARCHAR_IGNORECASE :
                key = "SIZED";
                break;

            case I_DECIMAL :
            case I_NUMERIC :
                key = "DECIMAL";
                break;

            default :
                key = null;
                break;
        }

        return BundleHandler.getString(_rbHndTICreateParams, key);
    }

    /**
     * @return
     */
    private String _getTICstMapClsName() {

        switch (_iInternalType) {

            case I_ARRAY :
                return "org.hsqldb.jdbcArray";

            case I_BLOB :
                return "org.hsqldb.jdbcBlob";

            case I_CLOB :
                return "org.hsqldb.jdbcClob";

            case I_DISTINCT :
                return "org.hsqldb.jdbcDistinct";

            case I_REF :
                return "org.hsqldb.jdbcRef";

            case I_STRUCT :
                return "org.hsqldb.jdbcStruct";

            default :
                return null;
        }
    }

    /**
     * @return
     */
    private Integer _getTIDataType() {
        return IA_JDBC_DATA_TYPES[_iInternalType];
    }

    private Integer getTIDataType(int internalType) {
        return IA_JDBC_DATA_TYPES[internalType];
    }

    /**
     * @return
     *
     *
     *
     *
     */
    private Integer _getTIDefScale() {

        switch (_iInternalType) {

            case I_BIGINT :
            case I_INTEGER :
            case I_INTEGER_IDENTITY :
            case I_SMALLINT :
            case I_TINYINT :
                return ValuePool.getInt(0);

            default :
                return null;
        }
    }

    private Integer getTIDefScale(int internalType) {

        switch (internalType) {

            case I_BIGINT :
            case I_INTEGER :
            case I_INTEGER_IDENTITY :
            case I_SMALLINT :
            case I_TINYINT :
                return ValuePool.getInt(0);

            default :
                return null;
        }
    }

    /**
     * @return
     */
    private Boolean _getTIFixedPrecScale() {

        switch (_iInternalType) {

            case I_BIGINT :
            case I_DECIMAL :
            case I_DOUBLE :
            case I_FLOAT :
            case I_INTEGER :
            case I_INTEGER_IDENTITY :
            case I_NUMERIC :
            case I_REAL :
            case I_SMALLINT :
            case I_TINYINT :
                return Boolean.FALSE;

            default :
                return null;
        }
    }

    /**
     * @return
     */
    private Integer _getTIIntervalPrec() {
        return null;
    }

    /**
     * @return
     */
    private String _getTILitPref() {

        switch (_iInternalType) {

            case I_BINARY :
            case I_BLOB :
            case I_CHAR :
            case I_CLOB :
            case I_LONGVARBINARY :
            case I_LONGVARCHAR :
            case I_VARBINARY :
            case I_VARCHAR :
            case I_VARCHAR_IGNORECASE :
                return "'";

            case I_DATALINK :
                return "{url '";

            case I_DATE :
                return "{d '";

            case I_OTHER :
                return "{o '";

            case I_TIME :
                return "{t '";

            case I_TIMESTAMP :
                return "{ts '";

            case I_XML :
                return "{xml '";

            default :
                return null;
        }
    }

    /**
     * @return
     */
    private String _getTILitSuf() {

        switch (_iInternalType) {

            case I_BINARY :
            case I_BLOB :
            case I_CHAR :
            case I_CLOB :
            case I_LONGVARBINARY :
            case I_LONGVARCHAR :
            case I_VARBINARY :
            case I_VARCHAR :
            case I_VARCHAR_IGNORECASE :
                return "'";

            case I_DATALINK :
            case I_DATE :
            case I_OTHER :
            case I_TIME :
            case I_TIMESTAMP :
            case I_XML :
                return "'}";

            default :
                return null;
        }
    }

    /**
     * Retrieves a localized verision of the type name, for display
     * purposes only.
     *
     * @param name the type's SQL name
     * @return a localized verision of the type name
     *
     *
     *
     *
     */
    private String _getTILocalName() {

        return BundleHandler.getString(_rbHndTILocalNames,
                                       (_iInternalType == I_INTEGER_IDENTITY)
                                       ? "IDENTITY"
                                       : SA_DATA_TYPE_NAMES[_iInternalType]);
    }

    /**
     * @return
     */
    private Integer _getTIMaxScale() {

        switch (_iInternalType) {

            case I_BIGINT :
            case I_DATE :
            case I_INTEGER :
            case I_INTEGER_IDENTITY :
            case I_SMALLINT :
            case I_TINYINT :
                return ValuePool.getInt(0);

            case I_DECIMAL :
            case I_NUMERIC :
                return ValuePool.getInt(Short.MAX_VALUE);

            case I_DOUBLE :
                return ValuePool.getInt(306);

            case I_FLOAT :
            case I_REAL :
                return ValuePool.getInt(38);

            default :
                return null;
        }
    }

    /**
     * @return
     */
    private Integer _getTIMaxScaleAct() {

        switch (_iInternalType) {

            case I_DECIMAL :
            case I_NUMERIC :
                return ValuePool.getInt(Integer.MAX_VALUE);

            default :
                return _getTIMaxScale();
        }
    }

    /**
     * @return
     */
    private Integer _getTIMinScale() {

        switch (_iInternalType) {

            case I_BIGINT :
            case I_DATE :
            case I_INTEGER :
            case I_INTEGER_IDENTITY :
            case I_SMALLINT :
            case I_TINYINT :
                return ValuePool.getInt(0);

            case I_DECIMAL :
            case I_NUMERIC :
                return ValuePool.getInt(Short.MIN_VALUE);

            case I_DOUBLE :
                return ValuePool.getInt(-324);

            case I_FLOAT :
            case I_REAL :
                return ValuePool.getInt(-45);

            default :
                return null;
        }
    }

    /**
     * @return
     */
    private Integer _getTIMinScaleAct() {

        switch (_iInternalType) {

            case I_DECIMAL :
            case I_NUMERIC :
                return ValuePool.getInt(Integer.MIN_VALUE);

            default :
                return _getTIMinScale();
        }
    }

    /**
     * @return
     */
    private Integer _getTINullable() {

        return (_iInternalType == I_INTEGER_IDENTITY)
               ? ValuePool.getInt(DatabaseMetaData.columnNoNulls)
               : ValuePool.getInt(DatabaseMetaData.columnNullable);
    }

    /**
     * @return
     */
    private Integer _getTINumPrecRadix() {

        switch (_iInternalType) {

            case I_BIGINT :
            case I_DECIMAL :
            case I_DOUBLE :
            case I_FLOAT :
            case I_INTEGER :
            case I_INTEGER_IDENTITY :
            case I_NUMERIC :
            case I_REAL :
            case I_SMALLINT :
            case I_TINYINT :
                return ValuePool.getInt(10);

            default :
                return null;
        }
    }

    /**
     * @return
     */
    private Integer _getTIPrec() {

        switch (_iInternalType) {

            case I_BINARY :
            case I_CHAR :
            case I_LONGVARBINARY :
            case I_LONGVARCHAR :
            case I_OTHER :
            case I_VARBINARY :
            case I_VARCHAR :
            case I_VARCHAR_IGNORECASE :
            case I_XML :
                return ValuePool.getInt(Integer.MAX_VALUE);

            case I_BIGINT :
                return ValuePool.getInt(19);

            case I_BIT :
                return ValuePool.getInt(1);

            case I_DATALINK :
                return ValuePool.getInt(2004);

            case I_DECIMAL :
            case I_NUMERIC :
                return ValuePool.getInt(80807123);

            case I_DATE :
            case I_INTEGER :
            case I_INTEGER_IDENTITY :
                return ValuePool.getInt(10);

            case I_DOUBLE :
                return ValuePool.getInt(17);

            case I_FLOAT :
            case I_REAL :
            case I_TIME :
                return ValuePool.getInt(8);

            case I_SMALLINT :
                return ValuePool.getInt(5);

            case I_TIMESTAMP :
                return ValuePool.getInt(29);

            case I_TINYINT :
                return ValuePool.getInt(3);

            default :
                return null;
        }
    }

    /**
     * @return
     */
    private Long _getTIPrecAct() {

        switch (_iInternalType) {

            case I_ARRAY :
            case I_BLOB :
            case I_CLOB :
                return ValuePool.getLong(Long.MAX_VALUE);
        }

        Integer prec = _getTIPrec();

        return (prec == null) ? null
                              : ValuePool.getLong(prec.longValue());
    }

    /**
     * Retrieves a localized verision of any remarks regarding the type,
     * for display purposes only.
     *
     * @return a localized verision of any remarks regarding the type
     */
    private String _getTIRemarks() {

        return BundleHandler.getString(_rbHndTIRemarks,
                                       (_iInternalType == I_INTEGER_IDENTITY)
                                       ? "IDENTITY"
                                       : SA_DATA_TYPE_NAMES[_iInternalType]);
    }

    /**
     * @return
     */
    private Integer _getTISearchable() {

        switch (_iInternalType) {

            case I_ARRAY :
            case I_BLOB :
            case I_CLOB :
            case I_JAVA_OBJECT :
            case I_STRUCT :
                return ValuePool.getInt(DatabaseMetaData.typePredNone);

            case I_OTHER :
                return ValuePool.getInt(DatabaseMetaData.typePredChar);

            default :
                return ValuePool.getInt(DatabaseMetaData.typeSearchable);
        }
    }

    /**
     * @return
     */
    private Float _getTISortKey() {

        float key = _getTIDataType().floatValue();

        if (_iInternalType == I_INTEGER_IDENTITY) {
            key += 0.1F;
        }

        return (ValuePool.getFloat(key));
    }

    /**
     * @return
     */
    private Integer _getTISqlDataType() {
        return IA_SQL_DATA_TYPES[_iInternalType];
    }

    /**
     * @return
     */
    private Integer _getTISqlDateTimeSub() {

        switch (_iInternalType) {

            case I_DATE :
                return ValuePool.getInt(1);

            case I_TIME :
                return ValuePool.getInt(2);

            case I_TIMESTAMP :
                return ValuePool.getInt(3);

            default :
                return null;
        }
    }

    /**
     * @return
     */
    private String _getTIStdMapClsName() {

        switch (_iInternalType) {

            case I_ARRAY :
                return "java.sql.Array";

            case I_BIGINT :
                return "long";

            case I_BINARY :
            case I_LONGVARBINARY :
            case I_VARBINARY :
                return "[B";

            case I_BIT :
                return "boolean";

            case I_BLOB :
                return "java.sql.Blob";

            case I_CHAR :
            case I_LONGVARCHAR :
            case I_VARCHAR :
            case I_VARCHAR_IGNORECASE :
                return "java.lang.String";

            case I_CLOB :
                return "java.sql.Clob";

            case I_DATALINK :
                return "java.net.URL";

            case I_DATE :
                return "java.sql.Date";

            case I_DECIMAL :
            case I_NUMERIC :
                return "java.math.BigDecimal";

            case I_DISTINCT :
            case I_JAVA_OBJECT :
            case I_OTHER :
            case I_XML :
                return "java.lang.Object";    // ??? String ???

            case I_DOUBLE :
                return "double";

            case I_FLOAT :
            case I_REAL :
                return "float";

            case I_INTEGER :
            case I_INTEGER_IDENTITY :
                return "int";

            case I_NULL :
                return "null";

            case I_REF :
                return "java.sql.Ref";

            case I_SMALLINT :
                return "short";

            case I_STRUCT :
                return "java.sql.Struct";

            case I_TIME :
                return "java.sql.Time";

            case I_TIMESTAMP :
                return "java.sql.Timestamp";

            case I_TINYINT :
                return "byte";

            default :
                return null;
        }
    }

    /**
     * @return
     */
    private Boolean _getTIStdMapClsSup() {

        boolean isSup = false;

        switch (_iInternalType) {

            case I_ARRAY : {
                try {
                    _classForName("java.sql.Array");

                    isSup = true;
                } catch (Exception e) {
                    isSup = false;
                }

                break;
            }
            case I_BLOB : {
                try {
                    _classForName("java.sql.Blob");

                    isSup = true;
                } catch (Exception e) {
                    isSup = false;
                }

                break;
            }
            case I_CLOB : {
                try {
                    _classForName("java.sql.Clob");

                    isSup = true;
                } catch (Exception e) {
                    isSup = false;
                }

                break;
            }
            case I_DISTINCT : {
                isSup = false;

                break;
            }
            case I_REF : {
                try {
                    _classForName("java.sql.Ref");

                    isSup = true;
                } catch (Exception e) {
                    isSup = false;
                }

                break;
            }
            case I_STRUCT : {
                try {
                    _classForName("java.sql.Struct");

                    isSup = true;
                } catch (Exception e) {
                    isSup = false;
                }

                break;
            }
            default : {
                isSup = (_getTIStdMapClsName() != null);

                break;
            }
        }

        return valueOf(isSup);
    }

    /**
     * @return
     */
    private Boolean _getTISupAsPCol() {

        switch (_iInternalType) {

            case I_NULL :           // for void return type
            case I_JAVA_OBJECT :    // for Connection as first parm
            case I_ARRAY :          // for Object[] row of Trigger.fire()
                return Boolean.TRUE;

            default :
                return _getTISupAsTCol();
        }
    }

    /**
     * @return
     */
    private Boolean _getTISupAsTCol() {

        String typeName;

        if (_iInternalType == I_NULL) {
            return Boolean.FALSE;
        }

        typeName = Column.getTypeString(
            IA_JDBC_DATA_TYPES[_iInternalType].intValue());

        return (typeName == null || typeName.length() == 0) ? Boolean.FALSE
                                                            : Boolean.TRUE;
    }

    /**
     * @return
     */
    private String _getTITypeName() {
        return SA_DATA_TYPE_NAMES[_iInternalType];
    }

    private String getTITypeName(int internalType) {
        return SA_DATA_TYPE_NAMES[internalType];
    }

    /**
     * @return
     */
    private Boolean _getTIUAttr() {

        switch (_iInternalType) {

            case I_BIGINT :
            case I_DECIMAL :
            case I_DOUBLE :
            case I_FLOAT :
            case I_INTEGER :
            case I_INTEGER_IDENTITY :
            case I_NUMERIC :
            case I_REAL :
            case I_SMALLINT :
            case I_TINYINT :
                return Boolean.FALSE;

            default :
                return null;
        }
    }

    /** one time initialisation of instance at construction time */
    private void _initProduces() throws SQLException {

/*
        if (_isInitProduces) {
            return;
        }

        _isInitProduces = true;
*/
        StopWatch sw = null;

        if (Trace.TRACE) {
            sw = new StopWatch();
        }

//        _sysTableProtos             = new HsqlArrayList();
//        _hSysTableProtos            = new HsqlHashMap();
//        _hSysTableMethodCache       = new HsqlHashMap();
//        _hsSysTableNames            = new HsqlHashSet();
//        _hSysTableHsqlNames         = new HsqlHashMap();
        _hSysTableColumnHsqlNames = new HsqlHashMap();
        _hSysTableIndexHsqlNames  = new HsqlHashMap();
        nonCachedTablesSet        = new HsqlHashSet();

        // build the set of non-cached tables
        nonCachedTablesSet.add("SYSTEM_CACHEINFO");
        nonCachedTablesSet.add("SYSTEM_CONNECTIONINFO");
        nonCachedTablesSet.add("SYSTEM_SESSIONS");
        nonCachedTablesSet.add("SYSTEM_PROPERTIES");

        // flag the Session-dependent cached tables
        sysTableSessionDependent[SYSTEM_COLUMNPRIVILEGES] =
            sysTableSessionDependent[SYSTEM_CROSSREFERENCE] =
            sysTableSessionDependent[SYSTEM_INDEXINFO] =
            sysTableSessionDependent[SYSTEM_PRIMARYKEYS] =
            sysTableSessionDependent[SYSTEM_TABLES] =
            sysTableSessionDependent[SYSTEM_TRIGGERCOLUMNS] =
            sysTableSessionDependent[SYSTEM_TRIGGERS] =
            sysTableSessionDependent[SYSTEM_VIEWSOURCE] = true;

/*
        Method[] methods = getClass().getMethods();
        Method   method;
        String   methodName;
*/
        String tableName;

//        HsqlName hsqlTableName;
        Table t;

//        Class    tableClass = Table.class;
        Session oldSession = _session;

        _session = database.getSysSession();

        Trace.check(_session != null, Trace.USER_NOT_FOUND,
                    UserManager.SYS_USER_NAME);

        for (int i = 0; i < sysTables.length; i++) {
            tableName = sysTableNames[i];
            t         = sysTables[i] = generateTable(i);

            if (t != null) {
                t.setDataReadOnly(true);
            }
        }

/*
        for (int i = 0; i < methods.length; i++) {
            method = methods[i];
            methodName = method.getName();

            if (!methodName.startsWith("SYSTEM_")) { continue; }

            try {
                _tableHsqlName = sysTableHsqlNames[sysTableNamesMap.get(methodName)];
                t = (Table) method.invoke(this, _pValues);
                tableName = methodName;
                t.setDataReadOnly(true);
                _sysTableProtos.add(t);
                _hSysTableProtos.put(tableName, t);
                _hsSysTableNames.add(tableName);
                _hsSysTableNames.add(QS_DEFN_SCHEMA_DOT + tableName);
                _hSysTableMethodCache.put(tableName, method);
            } catch (Exception e) {

//                System.out.println("Method name: " + methodName);
//                if (e instanceof InvocationTargetException) {
//                    ((InvocationTargetException)e).printStackTrace();
//                } else {
//                    e.printStackTrace();
//                }

                if (Trace.TRACE) {
                    Trace.trace(e.getMessage());
                }
            }
        }
*/
/*
        Enumeration sysTableNames = _hSysTableHsqlNames.elements();

        while (sysTableNames.hasMoreElements()) {
             HsqlName name = (HsqlName) sysTableNames.nextElement();
            _userManager.grant("PUBLIC",name,UserManager.SELECT);
        }
*/
        for (int i = 0; i < sysTableHsqlNames.length; i++) {
            if (sysTables[i] != null) {
                _userManager.grant("PUBLIC", sysTableHsqlNames[i],
                                   UserManager.SELECT);
            }
        }

        _session = oldSession;

        if (Trace.TRACE) {
            Trace.trace(this + ".initProduces() in " + sw.elapsedTime()
                        + " ms.");
        }
    }

    /**
     * Loads the localization resources from the class path and initializes
     *  type lookup support
     * @throws SQLException
     */
    private void _initProduceTable() throws SQLException {

        if (_isInitProduceTable) {
            return;
        }

        _isInitProduceTable = true;

        StopWatch sw = null;

        if (Trace.TRACE) {
            sw = new StopWatch();
        }

        _hClazzClassFiles    = new HsqlHashMap();
        _hClazzInstructions  = new HsqlHashMap();
        _hResultDescriptions = new HsqlHashMap();
        _hTNum               = new HsqlIntKeyIntValueHashMap();

        _hTNum.put(2003, I_ARRAY);
        _hTNum.put(-5, I_BIGINT);
        _hTNum.put(-2, I_BINARY);
        _hTNum.put(-7, I_BIT);
        _hTNum.put(2004, I_BLOB);
        _hTNum.put(1, I_CHAR);
        _hTNum.put(2005, I_CLOB);
        _hTNum.put(70, I_DATALINK);
        _hTNum.put(91, I_DATE);
        _hTNum.put(3, I_DECIMAL);
        _hTNum.put(2001, I_DISTINCT);
        _hTNum.put(8, I_DOUBLE);
        _hTNum.put(6, I_FLOAT);
        _hTNum.put(4, I_INTEGER);
        _hTNum.put(2000, I_JAVA_OBJECT);
        _hTNum.put(-4, I_LONGVARBINARY);
        _hTNum.put(-1, I_LONGVARCHAR);
        _hTNum.put(0, I_NULL);
        _hTNum.put(2, I_NUMERIC);
        _hTNum.put(1111, I_OTHER);
        _hTNum.put(7, I_REAL);
        _hTNum.put(2006, I_REF);
        _hTNum.put(5, I_SMALLINT);
        _hTNum.put(2002, I_STRUCT);
        _hTNum.put(92, I_TIME);
        _hTNum.put(93, I_TIMESTAMP);
        _hTNum.put(-6, I_TINYINT);
        _hTNum.put(-3, I_VARBINARY);
        _hTNum.put(Column.VARCHAR_IGNORECASE, I_VARCHAR_IGNORECASE);
        _hTNum.put(12, I_VARCHAR);
        _hTNum.put(137, I_XML);

        ClassLoader cl = database.classLoader;

        _rbHndTableRemarks = BundleHandler.getBundleHandle("table-remarks",
                cl);
        _rbHndTColumnRemarks = BundleHandler.getBundleHandle("column-remarks",
                cl);
        _rbHndTILocalNames = BundleHandler.getBundleHandle("data-type-names",
                cl);
        _rbHndTICreateParams =
            BundleHandler.getBundleHandle("data-type-create-parameters", cl);
        _rbHndTIRemarks = BundleHandler.getBundleHandle("data-type-remarks",
                cl);

        // could be quite a hit...wait till BYTECODE, PROCEDURES,
        // or PROCEDURECOLUMNS table is requested
        //_getClazzClassFile("org.hsqldb.Library");
        //_getClazzClassFile("java.lang.Math");
        _hsBuiltInLibs = new HsqlHashSet();

        _hsBuiltInLibs.add("org.hsqldb.Library");
        _hsBuiltInLibs.add("org.hsqldb.DatabaseClassLoader");
        _hsBuiltInLibs.add("java.lang.Math");

        // TODO: wouldn't it be easier just to define these
        // values the same as the DatabaseMetaData versions?
/*
        _hfkRules = new HsqlIntKeyHashMap();

        _hfkRules.put(
            Constraint.CASCADE,
            ValuePool.getInt(DatabaseMetaData.importedKeyCascade)
        );
        _hfkRules.put(
            Constraint.NO_ACTION,
            ValuePool.getInt(DatabaseMetaData.importedKeyNoAction)
        );
        _hfkRules.put(
            Constraint.SET_DEFAULT,
            ValuePool.getInt(DatabaseMetaData.importedKeySetDefault)
        );
        _hfkRules.put(
            Constraint.SET_NULL,
            ValuePool.getInt(DatabaseMetaData.importedKeySetNull)
        );
*/
        _sb   = new HsqlStringBuffer(1024);
        _hCls = new HsqlHashMap();

        Class   c;
        Integer internalType;

        // can only speed up test significantly for java.lang.Object,
        // final classes, primitive types and hierachy parents.
        // Must still check later if assignable from candidate classes, where
        // hierarchy parent is not final.
        //ARRAY
        try {
            c = _classForName("org.hsqldb.jdbcArray");

            _hCls.put(c, ValuePool.getInt(I_ARRAY));
        } catch (Exception e) {}

        // BIGINT
        internalType = ValuePool.getInt(I_BIGINT);

        _hCls.put(Long.TYPE, internalType);
        _hCls.put(Long.class, internalType);

        // BIT
        internalType = ValuePool.getInt(I_BIT);

        _hCls.put(Boolean.TYPE, internalType);
        _hCls.put(Boolean.class, internalType);

        // BLOB
        internalType = ValuePool.getInt(I_BLOB);

        try {
            c = _classForName("org.hsqldb.jdbcBlob");

            _hCls.put(c, internalType);
        } catch (Exception e) {}

        // CHAR
        internalType = ValuePool.getInt(I_CHAR);

        _hCls.put(Character.TYPE, internalType);
        _hCls.put(Character.class, internalType);
        _hCls.put(Character[].class, internalType);
        _hCls.put(char[].class, internalType);

        // CLOB
        internalType = ValuePool.getInt(I_CLOB);

        try {
            c = _classForName("org.hsqldb.jdbcClob");

            _hCls.put(c, internalType);
        } catch (Exception e) {}

        // DATALINK
        internalType = ValuePool.getInt(I_DATALINK);

        _hCls.put(java.net.URL.class, internalType);

        // DATE
        internalType = ValuePool.getInt(I_DATE);

        _hCls.put(java.util.Date.class, internalType);
        _hCls.put(java.sql.Date.class, internalType);

        // DECIMAL
        internalType = ValuePool.getInt(I_DECIMAL);

        try {
            c = _classForName("java.math.BigDecimal");

            _hCls.put(c, internalType);
        } catch (Exception e) {}

        // DISTINCT
        try {
            c = _classForName("org.hsqldb.jdbcDistinct");

            _hCls.put(c, ValuePool.getInt(I_DISTINCT));
        } catch (Exception e) {}

        // DOUBLE
        internalType = ValuePool.getInt(I_DOUBLE);

        _hCls.put(Double.TYPE, internalType);
        _hCls.put(Double.class, internalType);

        // FLOAT
        internalType = ValuePool.getInt(I_FLOAT);

        _hCls.put(Float.TYPE, internalType);
        _hCls.put(Float.class, internalType);

        // INTEGER
        internalType = ValuePool.getInt(I_INTEGER);

        _hCls.put(Integer.TYPE, internalType);
        _hCls.put(Integer.class, internalType);

        // JAVA_OBJECT
        internalType = ValuePool.getInt(I_JAVA_OBJECT);

        _hCls.put(Object.class, internalType);

        // LONGVARBINARY
        internalType = ValuePool.getInt(I_LONGVARBINARY);

        _hCls.put(byte[].class, internalType);

        // LONGVARCHAR
        internalType = ValuePool.getInt(I_LONGVARCHAR);

        _hCls.put(String.class, internalType);

        // NULL
        internalType = ValuePool.getInt(I_NULL);

        _hCls.put(Void.TYPE, internalType);
        _hCls.put(Void.class, internalType);

        // REF
        internalType = ValuePool.getInt(I_REF);

        try {
            c = _classForName("org.hsqldb.jdbcRef");

            _hCls.put(c, internalType);
        } catch (Exception e) {}

        // SMALLINT
        internalType = ValuePool.getInt(I_SMALLINT);

        _hCls.put(Short.TYPE, internalType);
        _hCls.put(Short.class, internalType);

        // STRUCT
        internalType = ValuePool.getInt(I_STRUCT);

        try {
            c = _classForName("org.hsqldb.jdbcStruct");

            _hCls.put(c, internalType);
        } catch (Exception e) {}

        // TIME
        internalType = ValuePool.getInt(I_TIME);

        _hCls.put(java.sql.Time.class, internalType);

        // TIMESTAMP
        internalType = ValuePool.getInt(I_TIMESTAMP);

        _hCls.put(java.sql.Timestamp.class, internalType);

        // TINYINT
        internalType = ValuePool.getInt(I_TINYINT);

        _hCls.put(Byte.TYPE, internalType);
        _hCls.put(Byte.class, internalType);

        // XML
        internalType = ValuePool.getInt(I_XML);

        try {
            c = _classForName("org.w3c.dom.Document");

            _hCls.put(c, internalType);

            c = _classForName("org.w3c.dom.DocumentFragment");

            _hCls.put(c, internalType);
        } catch (Exception e) {}

        if (Trace.TRACE) {
            Trace.trace(this + "_initProduceTable() in " + sw.elapsedTime()
                        + " ms.");
        }
    }

    /**
     * @param fqn
     * @param t
     * @throws SQLException
     */
    private void _putResultDescription(String pName,
                                       Table t) throws SQLException {

        _setTable(t);

        int           count = t.getColumnCount();
        HsqlArrayList list  = new HsqlArrayList(count);

        for (int i = 1; i < count; i++) {
            Column column = t.getColumn(i);

            list.add(new Object[] {
                getColName(column), _getTIDataType(), _getTITypeName(),
                getColSize(column), getColBufLen(column), getColScale(column),
                _getTINumPrecRadix(), getColNullability(column),
                getColRemarks(column)
            });
        }

        _hResultDescriptions.put(pName, list);
    }

    /**
     * @param pName
     * @param rsmd
     * @throws SQLException
     */
    private void _putResultDescription(String pName,
                                       ResultSetMetaData rsmd)
                                       throws SQLException {

        int           count = rsmd.getColumnCount();
        HsqlArrayList list  = new HsqlArrayList(count);
        HsqlName      hname;
        String        name;
        int           type;
        String        typeName;
        int           precision;
        int           buflen;
        int           scale;
        int           radix;
        int           nullability;
        String        remarks;

        for (int i = 0; i < count; i++) {
            name        = rsmd.getColumnName(i);
            type        = rsmd.getColumnType(i);
            typeName    = rsmd.getColumnTypeName(i);
            precision   = rsmd.getPrecision(i);
            scale       = rsmd.getScale(i);
            nullability = rsmd.isNullable(i);

            Column column = new Column(new HsqlName(name, false), true, type,
                                       precision, scale, false, false, null);

            list.add(new Object[] {
                name, ValuePool.getInt(type), typeName, getColSize(column),
                getColBufLen(column), ValuePool.getInt(scale),
                _getTINumPrecRadix(), ValuePool.getInt(nullability), null
            });
        }
    }

    /**
     * @param fqn
     * @return
     */
    private static HsqlList _getResultColumnsDescription(String fqn) {
        return (HsqlList) _hResultDescriptions.get(fqn);
    }

    /**
     * @param rows
     * @param table
     * @throws SQLException
     */
/*
    private static void _insertSorted(HsqlHashMap rows,
                                      Table table) throws SQLException {

        Object[] keyArray;

        try {
            keyArray = ArrayUtil.getSortedKeys(rows);
        } catch (Exception e) {
            throw Trace.error(Trace.GENERAL_ERROR, e.getMessage());
        }

        for (int i = 0; i < keyArray.length; i++) {
            table.insert((Object[]) rows.get(keyArray[i]), null);
        }
    }
*/

    /**
     * @param list
     * @return
     */
    private String _intArrayToCSV(int[] list) {

        if (list == null || list.length == 0) {
            return null;
        }

        StringBuffer sb     = new StringBuffer();
        int          length = list.length;

        for (int i = 0; i < length; i++) {
            sb.append(list[i]);
            sb.append(',');
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }

        return sb.toString();
    }

    /**
     * Retrieves whether any form of SQL access is allowed against the database
     * object with the specified name w.r.t the database access rights
     * assigned to the specified <code>Session</code> user. <p>
     *
     * Returns true if the specified <code>Session</code> object currently
     * represents admin user or if a test equivalent to
     * <code>Session.check()</code> does not throw for at least one type of
     * right w.r.t. the database object specified by
     * <code>objectName</code>. <p>
     *
     * <b>Note:</b> This is mainly used as a fail-fast method when
     * generating system tables. It might be better, in terms of efficiency,
     * to implement this method directly in <code>org.hsqldb.Session</code>
     * and/or <code>org.hsqldb.User</code> (perhaps as
     * <code>checkAny(String)</code> method). But for now, this works fine
     * and does not require additional baggage in what are more strictly *core
     * classes.<p>
     *
     * @return <code>false</code> if absolutely no form of access is possible,
     *            else <code>true</code>
     * @param objectName to check
     */
    private boolean _isAccessible(String dbobject) throws SQLException {
        return _session.isAccessible(dbobject);
    }

    private boolean _isAccessible(Table table) throws SQLException {

        boolean access = _session.isAccessible(table.getName());

        if (access == false) {
            return access;
        }

        if (table.isTemp() && table.tableType != Table.SYSTEM_TABLE) {
            return (table.getOwnerSessionId() == _session.getId());
        }

        return true;
    }

    /**
     * @return
     */
    private boolean _isAccessibleTable() {
        return _isAccessibleTable;
    }

    /**
     * @return
     */
    private boolean _isAccessibleTableColumn() {
        return _isAccessibleTable;
    }

    /**
     * @return
     * @param clazz
     */
    private static boolean _isBuiltinLib(Class clazz) {
        return clazz == null ? false
                             : _isBuiltinLib(clazz.getName());
    }

    /**
     * @param className
     * @return
     */
    private static boolean _isBuiltinLib(String className) {
        return _hsBuiltInLibs.contains(className);
    }

    /**
     * @return
     */
    private HsqlArrayList _listVisibleSessions() {

        HsqlArrayList in;
        HsqlArrayList out;
        Session       session;
        boolean       isRequestorAdmin = _session.isAdmin();
        int           requestorId      = _session.getId();

        in  = database.cSession;
        out = new HsqlArrayList();

        for (int i = 0; i < in.size(); i++) {
            session = (Session) in.get(i);

            if (session == null) {}
            else if (isRequestorAdmin || session.getId() == requestorId) {
                out.add(session);
            }
        }

        return out;
    }

    /**
     * @param c
     * @throws SQLException
     */
    private void _setClass(Class c) throws SQLException {

        Trace.doAssert(c != null, "Class is null.");

        _class         = c;
        _iInternalType = _findInternalType(c);
    }

    /**
     * @param c
     * @throws SQLException
     */
/*
    private void _setColumn(Column c) throws SQLException {

        Trace.doAssert(c != null, "Column is null.");

        _column = c;

        if (_column.isIdentity()) {
            _iInternalType = I_INTEGER_IDENTITY;
        } else {
            _iInternalType = _findInternalType(_column.getType());
        }

        _columnName = getColName(c);
    }
*/
    private int getInternalType(Column c) {

        if (c.isIdentity()) {
            return I_INTEGER_IDENTITY;
        } else {
            return findInternalType(c.getType());
        }
    }

    /**
     * @param t
     * @throws SQLException
     */
    private void _setTable(Table t) throws SQLException {

        Trace.doAssert(t != null, "table is null");

        _table             = t;
        _isSystemTable     = (t.tableType == Table.SYSTEM_TABLE);
        _isTempTable       = t.isTemp();
        _tableName         = _getTableName();
        _isAccessibleTable = _isAccessible(_table);
    }

    /**
     * @return
     * @param index
     */
    private Table _tableForIndex(Index index) {

        String   indexName = index.getName().name;
        HsqlList tables    = _userTableList;
        int      size      = tables.size();
        Table    table;

        for (int i = 0; i < size; i++) {
            table = (Table) tables.get(i);

            if (table.getIndex(indexName) != null) {
                return table;
            }
        }

        return null;
    }

    /**
     * @param name
     * @return
     */
    private String _withoutCatQual(String name) {

        String cat = _databaseName;
        String out;

        if (name.startsWith(cat + ".")) {
            out = name.substring((cat + ".").length());
        } else {
            out = name;
        }

        return out;
    }

    /**
     * @return
     * @param name
     */
    private static String _withoutDefnSchemQual(String name) {

        return name.startsWith(QS_DEFN_SCHEMA_DOT)
               ? name.substring(QS_DEFN_SCHEMA_DOT_LEN)
               : name;
    }

    /**
     * @return
     * @param name
     */
    private static String _withoutInfoSchemQual(String name) {

        return name.startsWith(QS_INFO_SCHEMA_DOT)
               ? name.substring(QS_INFO_SCHEMA_DOT_LEN)
               : name;
    }

    /**
     * Creates a new primoidal system table with the specified name.
     * @return a new system table
     * @param name of the table
     * @throws SQLException if a database access error occurs
     */
    private Table createBlankTable(HsqlName name) throws SQLException {
        return new Table(database, name, Table.SYSTEM_TABLE, 0);
    }

    /**
     * @throws SQLException
     * @return
     */
    protected Enumeration enumerateSysTables() throws SQLException {
        return new ArrayEnumeration(sysTables, true);
    }

    /**
     * Does this object produce a table with specified name? <P>
     * @return true if this object produces a table with the specified name, else false
     * @param name to check
     * @throws SQLException if a database access error occurs
     */
    boolean isSystemTable(String name) {
        return sysTableNamesMap.containsKey(name);
    }

    /**
     * Retrieves the system <code>Table</code> object corresponding to
     * the <code>name</code> and <code>session</code> arguments.
     * @param name a String identifying the desired table
     * @param session the Session object requesting the table
     * @throws SQLException if there is a problem producing the table or a database access error occurs
     * @return a system table corresponding to the <code>name</code> and
     * <code>session</code> arguments
     */
    Table getSystemTable(String name, Session session) throws SQLException {

        Table t;
        int   tableIndex;

        Trace.doAssert(name != null, "name is null");
        Trace.doAssert(session != null, "session is null");

        // must come first...many methods depend on this being set properly
        _session = session;

        // - removes the "cat." part of qualified SQL indentifiers
        // - useless until Tokenizer can produce cat.schem.id tokens properly
        // name = _withoutCatQual(name,session);
        // finds a non-temp table that was not alread found
        // by Database.getTable() before we are called.  We report
        // such tables in the PUBLIC schema, so this method tests for
        // the "PUBLIC." prefix.  If it is present, ti strips
        // off "PUBLIC." and then does a Database.findUserTable(name);
        t = _findPubSchemaTable(name);

        if (t != null) {
            return t;
        }

        // finds a temp table not found
        // by Database.getTable() before we are called.  We report
        // such tables in a schema named after the session user,
        // so this method tests for a prefix of
        // _session.getUsername() + ".".  If it is present, then it
        // is stripped off and Database.findUserTable(name, _session)
        // is performed;
        t = _findUserSchemaTable(name);

        if (t != null) {
            return t;
        }

        // there are not yet any objects "in" the INFORMATION_SCHEMA
        // TODO:  provide built-in views against system tables
        // t = database.findUserTable(_withoutInfoSchemQual(name), session);
        // if (t != null) return t;
        // strips off "DEFINITION_SCHEMA." if it is present.
        name = _withoutDefnSchemQual(name);

        // - maybe SYSTEM_LOBS, a "special" system table that must
        // be in database.tTable in order to get persisted
        //
        // TODO:  This should really be something like:
        //
        // if (hsPersistedSystemTables.contains(name)) {
        //      return database.findUserTable(name);
        // }
        //
        // but there is only one such table.  Will change if/when
        // there are ever others to worry about.
        if ("SYSTEM_LOBS".equals(name)) {
            return database.findUserTable(name);
        }

        // In addition to avoiding overhead of calling _generateTable(),
        // calling produces() here is required to guarantee that
        // initStatic1() has been called before proceding, ensuring that
        // _hSysTableProtos is intialized and making other things ready
        // for the second stage of initialization in initStatic2(), which
        // in turn is required before calling _generateTable();
        if (!isSystemTable(name)) {
            return null;
        }

        tableIndex = sysTableNamesMap.get(name);
        t          = sysTables[tableIndex];

        // fredt - any system table that is not supported will be null here
        if (t == null) {
            return t;
        }

        // at the time of openning the database no content is needed
        if (!withContent) {
            return t;
        }

        // first call to produceTable() with intent of retrieving
        // a table with actual content...we need to finish initializing
        // our lookup stuctures, etc.
        if (!_isInitProduceTable) {
            _initProduceTable();
        }

/*
        // Never cache tables that are not cache candidates.
        // i.e. system tables with content that changes
        // outside the scope of DDL or with conent that
        // changes rapidly enough inside the scope
        // of DDL that caching of other tables would get
        // broken too often to be worth the savings
        boolean isCacheCandidate = _cacheCandidate(name);

        if (isCacheCandidate) {
            if (isDirty()) {
                _cacheClear();
                if (Trace.TRACE) {
                    Trace.trace("System table cache cleared.");
                }
            } else {
                t  = _cacheGet(name);
                // got a cache hit. return cached table
                if (t != null) {
                    if (Trace.TRACE) {
                        Trace.trace("Using cached system table: " + name);
                    }
                    return t;
                }
            }
        }
*/
        StopWatch sw = null;

        if (Trace.TRACE) {
            sw = new StopWatch();
        }

        if (isDirty) {
            _cacheClear();

            if (Trace.TRACE) {
                Trace.trace("System table cache cleared.");
            }
        }

        Session oldSession = sysTableSessions[tableIndex];
        boolean tableValid = oldSession != null;

        // user has changed and table is user-dependent
        if (session != oldSession && sysTableSessionDependent[tableIndex]) {
            tableValid = false;
        }

        if (nonCachedTablesSet.contains(name)) {
            tableValid = false;
        }

        // any valid cached table will be returned here
        if (tableValid) {
            return t;
        }

        // fredt - clear the contents of table and set new User
        t.clearAllRows();

        sysTableSessions[tableIndex] = session;

        // match and if found, generate.
        t = generateTable(tableIndex);

        // t will b null at this point, if this implementation
        // does not support the particular table
        if (Trace.TRACE) {
            Trace.trace("generated system table: " + name + " in "
                        + sw.elapsedTime() + " ms.");
        }

        // send back what we found or generated
        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the aliases defined
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
     *
     * OBJECT_TYPE  VARCHAR   type of the aliased object
     * OBJECT_CAT   VARCHAR   catalog of the aliased object
     * OBJECT_SCHEM VARCHAR   schema of the aliased object
     * OBJECT_NAME  VARCHAR   identifier of the aliased object
     * ALIAS_CAT    VARCHAR   catalog in which alias is defined
     * ALIAS_SCHEM  VARCHAR   schema in which alias is defined
     * ALIAS        VARCHAR   alias for the indicated object
     *
     * </pre>
     *
     * <b>Note:</b> Up to and including HSQLDB 1.7.2, user-defined aliases
     * are supported only for SQL function and stored procedure calls
     * (indicated by the value "ROUTINE" in the <code>OBJECT_TYPE</code>
     * column), and there is no syntax for dropping aliases, only for
     * creating them. <p>
     * @return a Table object describing the aliases accessible within the
     * specified <code>Session</code> context.
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_ALIASES(int tableIndex) throws SQLException {

        Table t = sysTables[tableIndex];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[tableIndex]);

            _addColumn(t, "OBJECT_TYPE", Types.VARCHAR, false);    // not null
            _addColumn(t, "OBJECT_CAT", Types.VARCHAR);
            _addColumn(t, "OBJECT_SCHEM", Types.VARCHAR);
            _addColumn(t, "OBJECT_NAME", Types.VARCHAR, false);    // not null
            _addColumn(t, "ALIAS_CAT", Types.VARCHAR);
            _addColumn(t, "ALIAS_SCHEM", Types.VARCHAR);
            _addColumn(t, "ALIAS", Types.VARCHAR, false);          // not null
            t.createPrimaryKey(null);

            return t;
        }

        // Holders for calculated column values
        String cat;
        String schem;
        String alias;
        String objectName;
        String objectType;

        // Intermediate holders for items needed to calculate column values
        // and produce a sorted set of rows for insertion into the output table
        HsqlHashMap hAliases;
        Enumeration aliases;
        Object[]    row;
        Result      r;
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
        cat   = _getCatalogName(database);
        schem = QS_DEFN_SCHEMA;
        r     = _createResultProto(t);

        // Do it.
        hAliases   = database.getAlias();
        aliases    = hAliases.keys();
        objectType = "ROUTINE";

        while (aliases.hasMoreElements()) {
            row        = t.getNewRow();
            alias      = (String) aliases.nextElement();
            objectName = (String) hAliases.get(alias);

            // must have class grant to see method call aliases
            pos = objectName.lastIndexOf('.');

            if (pos >= 0 &&!_isAccessible(objectName.substring(0, pos))) {
                continue;
            }

            row[ialias_object_type]  = objectType;
            row[ialias_object_cat]   = cat;      // good enough for now
            row[ialias_object_schem] = schem;    // ditto
            row[ialias_object_name]  = objectName;
            row[ialias_cat]          = cat;
            row[ialias_schem]        = schem;
            row[ialias]              = alias;

            r.add(row);
        }

        // must have create/alter table rights to see domain aliases
        if (_session.isAdmin()) {
            aliases    = Column.hTypes.keys();
            objectType = "DOMAIN";

            while (aliases.hasMoreElements()) {
                row   = t.getNewRow();
                alias = (String) aliases.nextElement();

                Integer tn = (Integer) Column.hTypes.get(alias);

                objectName = Column.getTypeString(tn.intValue());

                if (alias.equals(objectName)) {
                    continue;
                }

                row[ialias_object_type]  = objectType;
                row[ialias_object_cat]   = cat;      // good enough for now
                row[ialias_object_schem] = schem;    // ditto
                row[ialias_object_name]  = objectName;
                row[ialias_cat]          = cat;
                row[ialias_schem]        = schem;
                row[ialias]              = alias;

                r.add(row);
            }
        }

        r.sortResult(new int[] {
            ialias_object_type, ialias_object_name, ialias
        }, new int[] {
            1, 1, 1
        });
        t.insert(r, _session);
        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the optimal
     * set of visible columns that uniquely identifies a row
     * for each accessible table defined within this database. <p>
     *
     * Each row describes a single column of the best row indentifier column
     * set for a particular table, where TABLE_CAT, TABLE_SCHEM, TABLE_NAME,
     * COLUMN_NAME forms the primary key.  Each row has the following
     * columns: <p>
     *
     * <pre>
     * SCOPE          SMALLINT  scope of applicability
     * COLUMN_NAME    VARCHAR   column name
     * DATA_TYPE      SMALLINT  SQL data type from java.sql.Types
     * TYPE_NAME      VARCHAR   Data source dependent type name
     * COLUMN_SIZE    INTEGER   precision
     * BUFFER_LENGTH  INTEGER   not used
     * DECIMAL_DIGITS SMALLINT  scale
     * PSEUDO_COLUMN  SMALLINT  is this a pseudo column like an Oracle ROWID?
     * TABLE_CAT      VARCHAR   table catalog
     * TABLE_SCHEM    VARCHAR   table schema
     * TABLE_NAME     VARCHAR   table name
     * NULLABLE       SMALLINT  is column nullable?
     * IN_KEY         BOOLEAN   belongs to a primary or alternate key?
     * </pre> <p>
     *
     * <code>jdbcDatabaseMetaData.getBestRowIdentifier</code> uses its
     * nullable parameter to filter the rows of this table in the following
     * manner: <p>
     *
     * If the nullable parameter is <code>false</code>, then rows are reported
     * only if, in addition to satisfying the other specified filter values,
     * the IN_KEY column value is TRUE. If the nullable parameter is
     * <code>true</code>, then the IN_KEY column value is ignored. <p>
     *
     * There is not yet infrastructure in place to make some of the ranking
     * descisions described below, and it is anticipated that mechanisms
     * upon which cost descisions could be based will change significantly over
     * the next few releases.  Hence, in the interest of simplicity and of not
     * making overly complex dependency on features that will almost certainly
     * change significantly in the near future, the current implementation,
     * while perfectly adequate for all but the most demanding or exacting
     * purposes, is actually sub-optimal in the strictest sense. <p>
     *
     * A description of the current implementation follows: <p>
     *
     * <b>DEFINTIONS:</b>  <p>
     *
     * <b>Alternate key</b> <p>
     *
     *  <UL>
     *   <LI> An attribute of a table that, by virtue of its having a set of
     *        columns that are both the full set of columns participating in a
     *        unique constraint or index and are all not null, yeilds the same
     *        selectability characteristic that would obtained by declaring a
     *        primary key on those columns.
     *  </UL> <p>
     *
     * <b>Column set performance ranking</b> <p>
     *
     *  <UL>
     *  <LI> The ranking of the expected average performance of a subset of a
     *       table's columns used to select and/or compare rows, as taken in
     *       relation to all other distinct candidate subsets under
     *       consideration. This can be estimated by comparing each cadidate
     *       subset in terms of total column count, relative peformance of
     *       comparisons amongst the data types of the columns and differences
     *       in other costs involved in the execution plans generated using
     *       each subset under consideration for row selection/comparison.
     *  </UL> <p>
     *
     *
     * <b>Rules:</b> <p>
     *
     * Given the above definitions, the rules currently in effect for reporting
     * best row identifier are as follows, in order of precedence: <p>
     *
     * <OL>
     * <LI> if the table under consideration has a primary key contraint, then
     *      the columns of the primary key are reported, with no consideration
     *      given to the column set performance ranking over the set of
     *      candidate keys. Each row has its IN_KEY column set to TRUE.
     *
     * <LI> if 1.) does not hold, then if there exits one or more alternate
     *      keys, then the columns of the alternate key with the lowest column
     *      count are reported, with no consideration given to the column set
     *      performance ranking over the set of candidate keys. If there
     *      exists a tie for lowest column count, then the colunmns of the
     *      first key in the list sorted by column count are reported.
     *      Each row has its IN_KEY column set to TRUE.
     *
     * <LI> if both 1.) and 2.) do not hold, then if there exists one or more
     *      unique constraints/indicies where a subset of the participating
     *      columns are not null, then the columns of the contraint/index with
     *      the lowest non-zero not null column count are reported, with no
     *      consideration given to the column set performance ranking over the
     *      set of candidate column sets. If there exists a tie for lowest
     *      non-zero not null column count, then the columns of the unique
     *      constraint/index corresponding to the first column set in the
     *      list sorted by not null column count are reported. Each row has
     *      its IN_KEY column set to FALSE. <br> <br>
     *
     *      <b>Note:</b> This method does not consider the columns of a
     *      UNIQUE constraint to be a candidate set unless there is also at
     *      least one column that is not nullable. The reason for this is that
     *      the standardized definition a UNIQUE constraint states: <br> <br>
     *
     *      <em>A unique constraint is satisfied if and only if no two rows in
     *      a table have the same non-null values in the unique
     *      columns.</em><br><br>
     *
     *      which implies that a table having more than one row with
     *      all null values for the unique columns still satisfies
     *      a unique constraint, despite the fact that the current HSQLDB
     *      implementation prevents this state from occuring.
     *
     * <LI> if 1.), 2.) and 3.) do not hold, then no best row identifier
     *      column set is reported for the table under consideration.
     * </OL> <p>
     *
     * The scope reported for a best row identifier column set is determined
     * thus: <p>
     *
     * <OL>
     * <LI> if the database containing the table under consideration is in
     *      read-only mode or the table under consideration is GLOBAL TEMPORARY
     *      ( a TEMP or TEMP TEXT table, in HSQLDB parlance), then the scope
     *      is reported as
     *      <code>java.sql.DatabaseMetaData.bestRowSession</code>.
     *
     * <LI> if 1.) does not hold, then the scope is reported as
     *      <code>java.sql.DatabaseMetaData.bestRowTemporary</code>.
     * </OL> <p>
     * @return a <code>Table</code> object describing the optimal
     * set of visible columns that uniquely identifies a row
     * for each accessible table defined within this database
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_BESTROWIDENTIFIER(int tableIndex) throws SQLException {

        Table t = sysTables[tableIndex];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[tableIndex]);

            _addColumn(t, "SCOPE", Types.SMALLINT, false);            // not null
            _addColumn(t, "COLUMN_NAME", Types.VARCHAR, false);       // not null
            _addColumn(t, "DATA_TYPE", Types.SMALLINT, false);        // not null
            _addColumn(t, "TYPE_NAME", Types.VARCHAR, false);         // not null
            _addColumn(t, "COLUMN_SIZE", Types.INTEGER);
            _addColumn(t, "BUFFER_LENGTH", Types.INTEGER);            // unused
            _addColumn(t, "DECIMAL_DIGITS", Types.SMALLINT);
            _addColumn(t, "PSEUDO_COLUMN", Types.SMALLINT, false);    // not null
            _addColumn(t, "TABLE_CAT", Types.VARCHAR);
            _addColumn(t, "TABLE_SCHEM", Types.VARCHAR);
            _addColumn(t, "TABLE_NAME", Types.VARCHAR, false);        // not null
            _addColumn(t, "NULLABLE", Types.SMALLINT, false);         // not null
            _addColumn(t, "IN_KEY", Types.BIT, false);                // not null
            t.createPrimaryKey(null);

            return t;
        }

        // calculated column values
        Integer scope;           // scope: { temp, transaction, session }
        String  columnName;

        //-------------------------------------------
        // required for restriction of results via
        // DatabaseMetaData filter parameters, but
        // not actually  included in
        // DatabaseMetaData.getBestRowIdentifier()
        // result set
        //-------------------------------------------
        String  tableCatalog;    // table calalog
        String  tableSchema;     // table schema
        String  tableName;       // table name
        Boolean inKey;           // column participates in PK or AK?

        //-------------------------------------------
        // TODO:  Maybe include:
        //        - backing index (constraint) name?
        //        - column sequence in index (constraint)?
        //-------------------------------------------
        // secondary variables required to calculate column values
        // and produce a sorted set of rows for insertion into output table
        Enumeration tables;
        Table       table;

//        Object[]    briInfo;
        int[]    columnPositions;
        Object[] row;
        Result   r;

        // Column number mappings
        final int iscope          = 0;
        final int icolumn_name    = 1;
        final int idata_type      = 2;
        final int itype_name      = 3;
        final int icolumn_size    = 4;
        final int ibuffer_length  = 5;
        final int idecimal_digits = 6;
        final int ipseudo_column  = 7;
        final int itable_cat      = 8;
        final int itable_schem    = 9;
        final int itable_name     = 10;
        final int inullable       = 11;
        final int iinKey          = 12;

        // Initialization
        tables = _enumerateAllTables();
        r      = _createResultProto(t);

        // Do it.
        while (tables.hasMoreElements()) {
            table = (Table) tables.nextElement();

            _setTable(table);

            if (!_isAccessibleTable()) {
                continue;
            }

            columnPositions = table.getBestRowIdentifiers();

            if (columnPositions == null) {
                continue;
            }

            inKey        = table.isBestRowIdentifiersStrict() ? Boolean.TRUE
                                                              : Boolean.FALSE;
            tableName    = _getTableName();
            tableCatalog = _getCatalogName(table);
            tableSchema  = _getSchemaName(table);
            scope        = _getBRIScope();

            for (int i = 0; i < columnPositions.length; i++) {
                Column column = table.getColumn(columnPositions[i]);

                columnName           = getColName(column);
                row                  = t.getNewRow();
                row[iscope]          = scope;
                row[icolumn_name]    = columnName;
                row[idata_type]      = getTIDataType(getInternalType(column));
                row[itype_name]      = getTITypeName(getInternalType(column));
                row[icolumn_size]    = getColSize(column);
                row[ibuffer_length]  = getColBufLen(column);
                row[idecimal_digits] = getColScale(column);
                row[ipseudo_column]  = _getBRIPseudo();
                row[itable_cat]      = tableCatalog;
                row[itable_schem]    = tableSchema;
                row[itable_name]     = tableName;
                row[inullable]       = getColNullability(column);
                row[iinKey]          = inKey;

                r.add(row);
            }
        }

        r.sortResult(new int[] {
            iscope, itable_cat, itable_schem, itable_name, icolumn_name
        }, new int[] {
            1, 1, 1, 1, 1
        });
        t.insert(r, _session);
        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the
     * <em>Java Virual Machine</em><sup> <font size="-2">TM</font></sup>
     * bytecode instructions composing the <em>Java Language</em><sup>
     * <font size="-2">TM</font></sup> methods that implement the
     * accessible routines defined within this database. <p>
     *
     * Each row is a bytecode instruction description with the following
     * columns: <p>
     *
     * <pre>
     * OBJECT_CAT              VARCHAR   catalog of executable object
     * OBJECT_SCHEM            VARCHAR   schema of executable object
     * OBJECT_NAME             VARCHAR   name of executable object
     * OBJECT_TYPE             VARCHAR   type of executable object
     * SIGNATURE               VARCHAR   Java Method signature
     * LINE                    INTEGER   instruction line number
     * PC                      INTEGER   instruction program counter
     * OPCODE                  INTEGER   instruction opcode
     * MNEMONIC                VARCHAR   instruction mnemonic
     * OPERANDS                VARCHAR   instruction operands
     * OPERANDS_DESCRIPTION    VARCHAR   description of instruction operands
     * INSTRUCTION_DESCRIPTION VARCHAR   description of the instruction
     * </pre>
     *
     * <b>Note:</b>  Currently, bytecode instructions are reported only for the
     * public static methods made accessible via the HSQLDB class grant
     * mechanism and the methods called by triggers defined within the database.
     * In a future release, bytecode for routines defined via embedded
     * procedural language extention client modules and functionality
     * delivered through pluggable system modules may also be reported. <p>
     * @return a description of the bytecode instructions implementing accessible
     * routines, such as stored procedures, functions and trigger bodies
     * @throws SQLException if an error occurs while producing the table
     */
/*
    public Table SYSTEM_BYTECODE() throws SQLException {

        Table t = createBlankTable(i);

        _addColumn(t,"OBJECT_CAT", Types.VARCHAR);
        _addColumn(t,"OBJECT_SCHEM", Types.VARCHAR);
        _addColumn(t,"OBJECT_NAME", Types.VARCHAR, false); // not null
        _addColumn(t,"OBJECT_TYPE", Types.VARCHAR, false); // not null
        _addColumn(t,"SIGNATURE", Types.VARCHAR, false); // not null
        _addColumn(t,"LINE", Types.INTEGER, false); // not null
        _addColumn(t,"PC", Types.INTEGER, false); // not null
        _addColumn(t,"OPCODE", Types.INTEGER, false); // not null
        _addColumn(t,"MNEMONIC", Types.VARCHAR, false); // not null
        _addColumn(t,"OPERANDS", Types.VARCHAR);
        _addColumn(t,"OPERANDS_DESCRIPTION", Types.VARCHAR);
        _addColumn(t,"INSTRUCTION_DESCRIPTION", Types.VARCHAR);

        t.createPrimaryKey(null);
        _addIndex(t, null, new int[]{4},false);

        if (!isWithContent()) {
            return t;
        }

        // calculated column values

        String                  cat;
        String                  schem;
        String                  name;
        String                  type;
        String                  sig;


        // secondary variable require to calculate column values and
        // produce a sorted set of rows for insertion into output table

        String                  className;
        ClassFile               classFile;
        Enumeration             methods;
        Object[]                methodInfo;
        Method                  method;
        HsqlArrayList           methodList;
        Integer                 methodIndex;

        MemberInfo              memberInfo;
        Instruction[]           instructions;
        Instruction             instruction;
        int                     descriptorCount;
        Object[]                row;
        Result                  r;

        // column number mappings

        final int               iobject_cat    = 0;
        final int               iobject_schem  = 1;
        final int               iobject_name   = 2;
        final int               iobject_type   = 3;
        final int               isignature     = 4;
        final int               iline          = 5;
        final int               ipc            = 6;
        final int               iopcode        = 7;
        final int               imnemonic      = 8;
        final int               ioperands      = 9;
        final int               ioperands_type = 10;
        final int               idescription   = 11;

        // Initialization

        r                       = _createResultProto(t);
        methods                 = _enumerateAllAccessibleMethods(false);

        // Do it.

        while (methods.hasMoreElements()) {

            methodInfo          = (Object[]) methods.nextElement();
            method              = (Method) methodInfo[0];
            type                = (String) methodInfo[2];
            className           = method.getDeclaringClass().getName();
            classFile           = _getClazzClassFile(className);

            if (classFile == null) continue;

            sig                 = method.toString();
            methodIndex         = (Integer) classFile.hMembers.get(sig);

            if (methodIndex == null) continue;

            instructions        = _getClazzInstructions(method);

            if (instructions == null) continue;

            memberInfo          = classFile.methods[methodIndex.intValue()];
            name                =  _getMethodFQN(method);
            cat                 = _getCatalogName(method);
            schem               = _getSchemaName(method);

            for(int i = 0; i < instructions.length; i++) {

                instruction         = instructions[i];
                row                 = t.getNewRow();

                row[iobject_cat]    = cat;
                row[iobject_schem]  = schem;
                row[iobject_name]   = name;
                row[isignature]     = sig;
                row[iobject_type]   = type;
                row[iline]          = ValuePool.getInt(i);
                row[ipc]            = ValuePool.getInt(instruction.pc);
                row[iopcode]        = ValuePool.getInt(instruction.opcode);
                row[imnemonic]      = OpCodeHelper.toMnemonic(instruction.opcode);
                row[ioperands]      = _intArrayToCSV(instruction.operands);
                row[ioperands_type] =
                OpCodeHelper.getOperandsDescription(instruction.opcode);
                // instruction descriptions are expensive to generate
                // and not every use of org.hsqldb.clazz requires them,
                // so we only generate them here, caching them directly in
                // the associated Intruction objects
                if (instruction.description == null) {
                    instruction.description =
                    OpCodeHelper.getInstructionDescription(
                        instruction,
                        classFile.constant_pool,
                        memberInfo
                    );
                }

                row[idescription]   = instruction.description;

                r.add(row);
            }
        }

        r.sortResult(
            new int[] {
                iobject_cat,
                iobject_schem,
                iobject_name,
                iobject_type,
                isignature,
                iline},
            new int[] {1,1,1,1,1,1}
        );

        t.insert(r,_session);

        t.setDataReadOnly(true);

        return t;
    }
*/

    /**
     * Retrieves a <code>Table</code> object desribing the current
     * state of any row caching objects used by the accessible
     * tables defined within this database. <p>
     *
     * The row caching objects for which state is currently reported are: <p>
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
     * @return a description of the current state of any accessible
     *      <code>Cache</code> or <code>TextCache</code> objects
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_CACHEINFO(int tableIndex) throws SQLException {

        Table t = sysTables[tableIndex];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[tableIndex]);

            _addColumn(t, "CACHE_CLASS", Types.VARCHAR, false);           // not null
            _addColumn(t, "CACHE_FILE", Types.VARCHAR);
            _addColumn(t, "CACHE_LENGTH", Types.INTEGER, false);          // not null
            _addColumn(t, "CACHE_SIZE", Types.INTEGER, false);            // not null
            _addColumn(t, "FREE_BYTES", Types.INTEGER, false);            // not null
            _addColumn(t, "SMALLEST_FREE_ITEM", Types.INTEGER, false);    // not null
            _addColumn(t, "LARGEST_FREE_ITEM", Types.INTEGER, false);     // not null
            _addColumn(t, "FREE_COUNT", Types.INTEGER, false);            // not null
            _addColumn(t, "FREE_POS", Types.INTEGER, false);              // not null
            _addColumn(t, "MAX_CACHE_SIZE", Types.INTEGER, false);        // not null
            _addColumn(t, "MULTIPLIER_MASK", Types.VARCHAR, false);       // not null
            _addColumn(t, "WRITER_LENGTH", Types.INTEGER, false);         // not null
            t.createPrimaryKey(null);

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
        Result      r;

        // column number mappings
        final int icache_class        = 0;
        final int icache_file         = 1;
        final int icache_length       = 2;
        final int icache_size         = 3;
        final int ifree_bytes         = 4;
        final int ismallest_free_item = 5;
        final int ilargest_free_item  = 6;
        final int ifree_count         = 7;
        final int ifree_pos           = 8;
        final int imax_cache_size     = 9;
        final int imultiplier_mask    = 10;
        final int iwriter_length      = 11;

        // Initialization
        cacheSet = new HsqlHashSet();
        tables   = _enumerateUserTables();
        r        = _createResultProto(t);

        while (tables.hasMoreElements()) {
            table = (Table) tables.nextElement();

            _setTable(table);

            if (table.isCached() && _isAccessibleTable()) {
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

            row[icache_class] = cache.getClass().getName();
            row[icache_file] =
                (new java.io.File(cache.sName)).getAbsolutePath();
            row[icache_length] = ValuePool.getInt(cache.cacheLength);
            row[icache_size]   = ValuePool.getInt(cache.iCacheSize);
            row[ifree_bytes]   = ValuePool.getInt(iFreeBytes);
            row[ismallest_free_item] =
                ValuePool.getInt((int) lSmallestFreeItem);
            row[ilargest_free_item] = ValuePool.getInt(iLargestFreeItem);
            row[ifree_count]        = ValuePool.getInt(cache.iFreeCount);
            row[ifree_pos]          = ValuePool.getInt(cache.iFreePos);
            row[imax_cache_size]    = ValuePool.getInt(cache.maxCacheSize);
            row[imultiplier_mask] = Integer.toHexString(cache.multiplierMask);
            row[iwriter_length]     = ValuePool.getInt(cache.writerLength);

            r.add(row);
        }

        r.sortResult(new int[] {
            icache_class, icache_file
        }, new int[] {
            1, 1
        });
        t.insert(r, _session);
        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object naming the accessible catalogs
     * defined within this database. <p>
     *
     * Each row is a catalog name description with the following column: <p>
     *
     * <pre>
     * TABLE_CAT   VARCHAR   catalog name
     * </pre> <p>
     * @return a <code>Table</code> object naming the accessible
     *        catalogs defined within this database
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_CATALOGS(int tableIndex) throws SQLException {

        Table t = sysTables[tableIndex];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[tableIndex]);

            _addColumn(t, "TABLE_CAT", Types.VARCHAR, false);    // not null
            t.createPrimaryKey(null);

            return t;
        }

        Object[]    row;
        Enumeration catalogs;
        String      catalogName;
        Result      r;

        catalogs = _enumerateCatalogNames();
        r        = _createResultProto(t);

        while (catalogs.hasMoreElements()) {
            catalogName = (String) catalogs.nextElement();
            row         = t.getNewRow();
            row[0]      = catalogName;

            r.add(row);
        }

        r.sortResult(new int[]{ 0 }, new int[]{ 1 });
        t.insert(r, _session);
        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the visible
     * access rights for all accessible Java Class objects defined
     * within this database.<p>
     * @return a <code>Table</code> object describing the visible
     * access rights for all accessible Java Class objects defined
     * within this database
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_CLASSPRIVILEGES(int tableIndex) throws SQLException {

        Table t = sysTables[tableIndex];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[tableIndex]);

            _addColumn(t, "CLASS_CAT", Types.VARCHAR);
            _addColumn(t, "CLASS_SCHEM", Types.VARCHAR);
            _addColumn(t, "CLASS_NAME", Types.VARCHAR, false);      // not null
            _addColumn(t, "GRANTOR", Types.VARCHAR, false);         // not null
            _addColumn(t, "GRANTEE", Types.VARCHAR, false);         // not null
            _addColumn(t, "PRIVILEGE", Types.VARCHAR, false);       // not null
            _addColumn(t, "IS_GRANTABLE", Types.VARCHAR, false);    // not null
            t.createPrimaryKey(null);

            return t;
        }

        // calculated column values
        String classCatalog;
        String classSchema;
        String className;
        String grantorName;
        String granteeName;
        String privilege;
        String isGrantable;

        // secondary variables required to calculate column values and
        // produce a sorted set of rows for insertion into the output table
        HsqlArrayList users;
        Enumeration   classNames;
        User          granteeUser;
        Object[]      row;
        Result        r;

        // column number mappings
        final int iclass_cat    = 0;
        final int iclass_schem  = 1;
        final int iclass_name   = 2;
        final int igrantor      = 3;
        final int igrantee      = 4;
        final int iprivilege    = 5;
        final int iis_grantable = 6;

        // Initialization
        classCatalog = _getCatalogName(database);
        grantorName  = UserManager.SYS_USER_NAME;
        users        = _userManager.listVisibleUsers(_session, true);
        r            = _createResultProto(t);

        // Do it.
        for (int i = 0; i < users.size(); i++) {
            granteeUser = (User) users.get(i);
            granteeName = granteeUser.getName();
            isGrantable = granteeUser.isAdmin() ? "YES"
                                                : "NO";
            classNames  = granteeUser.getGrantedClassNames(false).elements();
            privilege   = "EXECUTE";

            while (classNames.hasMoreElements()) {
                className          = (String) classNames.nextElement();
                classSchema        = _getSchemaName(className);
                row                = t.getNewRow();
                row[iclass_cat]    = classCatalog;
                row[iclass_schem]  = classSchema;
                row[iclass_name]   = className;
                row[igrantor]      = grantorName;
                row[igrantee]      = granteeName;
                row[iprivilege]    = privilege;
                row[iis_grantable] = isGrantable;

                r.add(row);
            }

            classNames = _enumerateAccessibleTriggerClassNames(granteeUser);
            privilege  = "FIRE";

            while (classNames.hasMoreElements()) {
                className          = (String) classNames.nextElement();
                classSchema        = _getSchemaName(className);
                row                = t.getNewRow();
                row[iclass_cat]    = classCatalog;
                row[iclass_schem]  = classSchema;
                row[iclass_name]   = className;
                row[igrantor]      = grantorName;
                row[igrantee]      = granteeName;
                row[iprivilege]    = privilege;
                row[iis_grantable] = isGrantable;

                r.add(row);
            }
        }

        r.sortResult(new int[] {
            iclass_name, igrantee, iprivilege
        }, new int[] {
            1, 1, 1
        });
        t.insert(r, _session);
        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the visible
     * access rights for all visible columns of all accessible
     * tables defined within this database.<p>
     *
     * Each row is a column privilege description with the following columns: <p>
     *
     * <pre>
     * TABLE_CAT    VARCHAR   table catalog
     * TABLE_SCHEM  VARCHAR   table schema
     * TABLE_NAME   VARCHAR   table name
     * COLUMN_NAME  VARCHAR   column name
     * GRANTOR      VARCHAR   grantor of access
     * GRANTEE      VARCHAR   grantee of access
     * PRIVILEGE    VARCHAR   name of access
     * IS_GRANTABLE VARCHAR   grantable?: YES - grant to others, else NO
     * </pre>
     *
     * <b>Note:</b> As of 1.7.2, HSQLDB does not support column level
     * privileges. However, it does support table-level privileges, so they
     * are reflected here.  That is, the table-level privileges reported
     * in SYSTEM_TABLEPRIVILEGES are repeated, one row for each pairing
     * of table privilege and column. <p>
     * @return a <code>Table</code> object describing the visible
     *        access rights for all visible columns of
     *        all accessible tables defined within this
     *        database
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_COLUMNPRIVILEGES(int tableIndex) throws SQLException {

        Table t = sysTables[tableIndex];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[tableIndex]);

            _addColumn(t, "TABLE_CAT", Types.VARCHAR);
            _addColumn(t, "TABLE_SCHEM", Types.VARCHAR);
            _addColumn(t, "TABLE_NAME", Types.VARCHAR, false);      // not null
            _addColumn(t, "COLUMN_NAME", Types.VARCHAR, false);     // not null
            _addColumn(t, "GRANTOR", Types.VARCHAR, false);         // not null
            _addColumn(t, "GRANTEE", Types.VARCHAR, false);         // not null
            _addColumn(t, "PRIVILEGE", Types.VARCHAR, false);       // not null
            _addColumn(t, "IS_GRANTABLE", Types.VARCHAR, false);    // not null
            t.createPrimaryKey(null);

            return t;
        }

        //calculated column values
        String tableCatalog;
        String tableSchema;
        String tableName;
        String columnName;
        String grantorName;
        String granteeName;
        String privilege;
        String isGrantable;

        // secondary variables required to calculate column values and
        // produce a sorted set of rows for insertion into the output table
        HsqlArrayList users;
        User          user;
        Enumeration   tables;
        HsqlArrayList tablePrivileges;
        Table         table;
        HsqlName      accessKey;
        int           columnCount;
        Object[]      row;
        Result        r;

        // column number mappings
        final int itable_cat    = 0;
        final int itable_schem  = 1;
        final int itable_name   = 2;
        final int icolumn_name  = 3;
        final int igrantor      = 4;
        final int igrantee      = 5;
        final int iprivilege    = 6;
        final int iis_grantable = 7;

        // Initialization
        grantorName = UserManager.SYS_USER_NAME;
        users       = _userManager.listVisibleUsers(_session, true);
        tables      = _enumerateAllTables();
        r           = _createResultProto(t);

        // Do it.
        while (tables.hasMoreElements()) {
            table     = (Table) tables.nextElement();
            accessKey = table.getName();

            _setTable(table);

            // Only show table grants if session user is admin, has some right,
            // or the special PUBLIC user has some right.
            if (!_isAccessibleTable()) {
                continue;
            }

            tableName    = _getTableName();
            tableCatalog = _getCatalogName(table);
            tableSchema  = _getSchemaName(table);
            columnCount  = table.getColumnCount();

            for (int i = 0; i < users.size(); i++) {
                user            = (User) users.get(i);
                granteeName     = user.getName();
                tablePrivileges = user.listTablePrivileges(accessKey);
                isGrantable     = (user.isAdmin()) ? "YES"
                                                   : "NO";

                for (int j = 0; j < tablePrivileges.size(); j++) {
                    privilege = (String) tablePrivileges.get(j);

                    for (int k = 0; k < columnCount; k++) {
                        columnName = table.getColumn(k).columnName.name;
                        row                = t.getNewRow();
                        row[itable_cat]    = tableCatalog;
                        row[itable_schem]  = tableSchema;
                        row[itable_name]   = tableName;
                        row[icolumn_name]  = columnName;
                        row[igrantor]      = grantorName;
                        row[igrantee]      = granteeName;
                        row[iprivilege]    = privilege;
                        row[iis_grantable] = isGrantable;

                        r.add(row);
                    }
                }
            }
        }

        r.sortResult(new int[] {
            icolumn_name, iprivilege, igrantee
        }, new int[] {
            1, 1, 1
        });
        t.insert(r, _session);
        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the
     * visible columns of all accessible tables defined
     * within this database.<p>
     *
     * Each row is a column description with the following columns: <p>
     *
     * <pre>
     * TABLE_CAT         VARCHAR   table catalog
     * TABLE_SCHEM       VARCHAR   table schema
     * TABLE_NAME        VARCHAR   table name
     * COLUMN_NAME       VARCHAR   column name
     * DATA_TYPE         SMALLINT  SQL type from java.sql.Types
     * TYPE_NAME         VARCHAR   Data source dependent type name
     * COLUMN_SIZE       INTEGER   column size (length/precision)
     * BUFFER_LENGTH     INTEGER   not used
     * DECIMAL_DIGITS    INTEGER   # of fractional digits (scale)
     * NUM_PREC_RADIX    INTEGER   Radix
     * NULLABLE          INTEGER   is NULL allowed?
     * REMARKS           VARCHAR   comment describing column
     * COLUMN_DEF        VARCHAR   default value
     * SQL_DATA_TYPE     VARCHAR   unused
     * SQL_DATETIME_SUB  INTEGER   unused
     * CHAR_OCTET_LENGTH INTEGER   maximum number of bytes in the column
     * ORDINAL_POSITION  INTEGER   index of column in table (starting at 1)
     * IS_NULLABLE       VARCHAR   is column nullable?
     * SCOPE_CATLOG      VARCHAR   catalog of REF attribute scope table
     * SCOPE_SCHEMA      VARCHAR   schema of REF attribute scope table
     * SCOPE_TABLE       VARCHAR   name of REF attribute scope table
     * SOURCE_DATA_TYPE  VARCHAR   source type of REF attribute
     * <pre> <p>
     * @return a <code>Table</code> object describing the
     *        visible columns of all accessible
     *        tables defined within this database.<p>
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_COLUMNS(int tableIndex) throws SQLException {

        Table t = sysTables[tableIndex];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[tableIndex]);

            _addColumn(t, "TABLE_CAT", Types.VARCHAR);
            _addColumn(t, "TABLE_SCHEM", Types.VARCHAR);
            _addColumn(t, "TABLE_NAME", Types.VARCHAR, false);          // not null
            _addColumn(t, "COLUMN_NAME", Types.VARCHAR, false);         // not null
            _addColumn(t, "DATA_TYPE", Types.SMALLINT, false);          // not null
            _addColumn(t, "TYPE_NAME", Types.VARCHAR, false);           // not null
            _addColumn(t, "COLUMN_SIZE", Types.INTEGER);
            _addColumn(t, "BUFFER_LENGTH", Types.INTEGER);
            _addColumn(t, "DECIMAL_DIGITS", Types.INTEGER);
            _addColumn(t, "NUM_PREC_RADIX", Types.INTEGER);
            _addColumn(t, "NULLABLE", Types.INTEGER, false);            // not null
            _addColumn(t, "REMARKS", Types.VARCHAR);
            _addColumn(t, "COLUMN_DEF", Types.VARCHAR);
            _addColumn(t, "SQL_DATA_TYPE", Types.INTEGER);
            _addColumn(t, "SQL_DATETIME_SUB", Types.INTEGER);
            _addColumn(t, "CHAR_OCTET_LENGTH", Types.INTEGER);
            _addColumn(t, "ORDINAL_POSITION", Types.INTEGER, false);    // not null
            _addColumn(t, "IS_NULLABLE", Types.VARCHAR, false);         // not null
            _addColumn(t, "SCOPE_CATLOG", Types.VARCHAR);
            _addColumn(t, "SCOPE_SCHEMA", Types.VARCHAR);
            _addColumn(t, "SCOPE_TABLE", Types.VARCHAR);
            _addColumn(t, "SOURCE_DATA_TYPE", Types.VARCHAR);
            t.createPrimaryKey(null);

            return t;
        }

        // calculated column values
        String tableCatalog;
        String tableSchema;
        String tableName;

        // secondary variables required to calculate column values and
        // produce a sorted set of rows for insertion into an output table
        int         columnCount;
        Enumeration tables;
        Table       table;
        int         ordinalPosition;
        Object[]    row;
        Result      r;

        // column number mappings
        final int itable_cat         = 0;
        final int itable_schem       = 1;
        final int itable_name        = 2;
        final int icolumn_name       = 3;
        final int idata_type         = 4;
        final int itype_name         = 5;
        final int icolumn_size       = 6;
        final int ibuffer_length     = 7;
        final int idecimal_digits    = 8;
        final int inum_prec_radix    = 9;
        final int inullable          = 10;
        final int iremark            = 11;
        final int icolumn_def        = 12;
        final int isql_data_type     = 13;
        final int isql_datetime_sub  = 14;
        final int ichar_octet_length = 15;
        final int iordinal_position  = 16;
        final int iis_nullable       = 17;

        // Initialization
        tables = _enumerateAllTables();
        r      = _createResultProto(t);

        // Do it.
        while (tables.hasMoreElements()) {
            table = (Table) tables.nextElement();

            _setTable(table);

            if (!_isAccessibleTable()) {
                continue;
            }

            tableCatalog = _getCatalogName(table);
            tableSchema  = _getSchemaName(table);
            tableName    = _getTableName();
            columnCount  = table.getColumnCount();

            for (int i = 0; i < columnCount; i++) {
                Column column = table.getColumn(i);

                row                     = t.getNewRow();
                ordinalPosition         = i + 1;
                row[itable_cat]         = tableCatalog;
                row[itable_schem]       = tableSchema;
                row[itable_name]        = tableName;
                row[icolumn_name]       = getColName(column);
                row[idata_type] = getTIDataType(getInternalType(column));
                row[itype_name] = getTITypeName(getInternalType(column));
                row[icolumn_size]       = getColSize(column);
                row[ibuffer_length]     = getColBufLen(column);
                row[idecimal_digits]    = getColScale(column);
                row[inum_prec_radix]    = _getTINumPrecRadix();
                row[inullable]          = getColNullability(column);
                row[iremark]            = getColRemarks(column);
                row[icolumn_def]        = getColDefault(column);
                row[isql_data_type]     = _getTISqlDataType();
                row[isql_datetime_sub]  = _getTISqlDateTimeSub();
                row[ichar_octet_length] = getColCharOctLen(column);
                row[iordinal_position]  = ValuePool.getInt(ordinalPosition);
                row[iis_nullable]       = getColIsNullable(column);

                r.add(row);
            }
        }

        r.sortResult(new int[] {
            itable_schem, itable_name, iordinal_position
        }, new int[] {
            1, 1, 1
        });
        t.insert(r, _session);
        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the attributes
     * of the connection associated with the current execution context.<p>
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
    Table SYSTEM_CONNECTIONINFO(int tableIndex) throws SQLException {

        Table t = sysTables[tableIndex];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[tableIndex]);

            _addColumn(t, "KEY", Types.VARCHAR, false);      // not null
            _addColumn(t, "VALUE", Types.VARCHAR, false);    // not null
            t.createPrimaryKey(null);

            return t;
        }

        Object[] row;

        row    = t.getNewRow();
        row[0] = "SESSION_ID";
        row[1] = String.valueOf(_session.getId());

        t.insert(row, null);

        row    = t.getNewRow();
        row[0] = "AUTOCOMMIT";
        row[1] = _session.getAutoCommit() ? "TRUE"
                                          : "FALSE";

        t.insert(row, null);

        row    = t.getNewRow();
        row[0] = "USER";
        row[1] = _session.getUsername();

        t.insert(row, null);

        row    = t.getNewRow();
        row[0] = "CONNECTION_READONLY";
        row[1] = _session.isReadOnly() ? "TRUE"
                                       : "FALSE";

        t.insert(row, null);

        row    = t.getNewRow();
        row[0] = "DATABASE_READONLY";
        row[1] = database.bReadOnly ? "TRUE"
                                    : "FALSE";

        t.insert(row, null);

        row    = t.getNewRow();
        row[0] = "MAXROWS";
        row[1] = String.valueOf(_session.getMaxRows());

        t.insert(row, null);

        row    = t.getNewRow();
        row[0] = "DATABASE";
        row[1] = String.valueOf(database.getName());

        t.insert(row, null);

        row    = t.getNewRow();
        row[0] = "IDENTITY";
        row[1] = String.valueOf(_session.getLastIdentity());

        t.insert(row, null);
        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing for each
     * accessible rererencing and referenced table, how the referencing
     * tables import, for the purposes of referential integrity,
     * the columns of the referenced tables.<p>
     *
     * Each row is a foreign key column description with the following
     * columns: <p>
     *
     * <pre>
     * PKTABLE_CAT   VARCHAR   primary key table catalog
     * PKTABLE_SCHEM VARCHAR   primary key table schema
     * PKTABLE_NAME  VARCHAR   primary key table name
     * PKCOLUMN_NAME VARCHAR   primary key column name
     * FKTABLE_CAT   VARCHAR   foreign key table catalog being exported
     * FKTABLE_SCHEM VARCHAR   foreign key table schema being exported
     * FKTABLE_NAME  VARCHAR   foreign key table name being exported
     * FKCOLUMN_NAME VARCHAR   foreign key column name being exported
     * KEY_SEQ       SMALLINT  sequence number within foreign key
     * UPDATE_RULE   SMALLINT
     *    { Cascade | Set Null | Set Default | Restrict (No Action)}?
     * DELETE_RULE   SMALLINT
     *    { Cascade | Set Null | Set Default | Restrict (No Action)}?
     * FK_NAME       VARCHAR   foreign key name
     * PK_NAME       VARCHAR   primary key name
     * DEFERRABILITY SMALLINT
     *    { initially deferred | initially immediate | not deferrable }
     * <pre> <p>
     * @return a <code>Table</code> object describing how accessible tables import
     * other accessible tables' keys
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_CROSSREFERENCE(int tableIndex) throws SQLException {

        Table t = sysTables[tableIndex];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[tableIndex]);

            _addColumn(t, "PKTABLE_CAT", Types.VARCHAR);
            _addColumn(t, "PKTABLE_SCHEM", Types.VARCHAR);
            _addColumn(t, "PKTABLE_NAME", Types.VARCHAR, false);      // not null
            _addColumn(t, "PKCOLUMN_NAME", Types.VARCHAR, false);     // not null
            _addColumn(t, "FKTABLE_CAT", Types.VARCHAR);
            _addColumn(t, "FKTABLE_SCHEM", Types.VARCHAR);
            _addColumn(t, "FKTABLE_NAME", Types.VARCHAR, false);      // not null
            _addColumn(t, "FKCOLUMN_NAME", Types.VARCHAR, false);     // not null
            _addColumn(t, "KEY_SEQ", Types.SMALLINT, false);          // not null
            _addColumn(t, "UPDATE_RULE", Types.SMALLINT, false);      // not null
            _addColumn(t, "DELETE_RULE", Types.SMALLINT, false);      // not null
            _addColumn(t, "FK_NAME", Types.VARCHAR);
            _addColumn(t, "PK_NAME", Types.VARCHAR);
            _addColumn(t, "DEFERRABILITY", Types.SMALLINT, false);    // not null
            t.createPrimaryKey(null);

            return t;
        }

        // calculated column values
        String  pkTableCatalog;
        String  pkTableSchema;
        String  pkTableName;
        String  pkColumnName;
        String  fkTableCatalog;
        String  fkTableSchema;
        String  fkTableName;
        String  fkColumnName;
        Integer keySequence;
        Integer updateRule;
        Integer deleteRule;
        String  fkName;
        String  pkName;
        Integer deferrability;

        // secondary variables required to calculate column values and
        // produce a sorted set of rows for insertion into an output table
        Enumeration   tables;
        Table         table;
        Table         fkTable;
        Table         pkTable;
        int           columnCount;
        int[]         mainCols;
        int[]         refCols;
        HsqlArrayList constraints;
        Constraint    constraint;
        int           constraintCount;
        HsqlArrayList fkConstraintsList;
        Object[]      row;
        Result        r;

        // column number mappings
        final int ipk_table_cat   = 0;
        final int ipk_table_schem = 1;
        final int ipk_table_name  = 2;
        final int ipk_column_name = 3;
        final int ifk_table_cat   = 4;
        final int ifk_table_schem = 5;
        final int ifk_table_name  = 6;
        final int ifk_column_name = 7;
        final int ikey_seq        = 8;
        final int iupdate_rule    = 9;
        final int idelete_rule    = 10;
        final int ifk_name        = 11;
        final int ipk_name        = 12;
        final int ideferrability  = 13;

        // TODO:
        // disallow DDL that creates references to system tables
        // Initialization
        tables = _enumerateUserTables();
        r      = _createResultProto(t);

        // the only deferrability rule currently supported by hsqldb is:
        deferrability =
            ValuePool.getInt(DatabaseMetaData.importedKeyNotDeferrable);

        // We must consider all the constraints in all the user tables, since
        // this is where reference relationships are recorded.  However, we
        // are only concerned with Constraint.FOREIGN_KEY constraints here
        // because their corresponing Constraint.MAIN entries are essentially
        // duplicate data recorded in the referenced rather than the
        // referencing table.
        fkConstraintsList = new HsqlArrayList();

        while (tables.hasMoreElements()) {
            table           = (Table) tables.nextElement();
            constraints     = table.getConstraints();
            constraintCount = constraints.size();

            for (int i = 0; i < constraintCount; i++) {
                constraint = (Constraint) constraints.get(i);

                if (constraint.getType() == Constraint.FOREIGN_KEY) {
                    fkConstraintsList.add(constraint);
                }
            }
        }

        // Now that we have all of the desired constraints, we need to
        // process them, generating one row in our ouput table
        // for each column in each table participating in each constraint,
        // skipping constraints that refer to columns in tables to which the
        // session user has no access (may not make references)
        // Do it.
        for (int i = 0; i < fkConstraintsList.size(); i++) {
            constraint = (Constraint) fkConstraintsList.get(i);
            pkTable    = constraint.getMain();

            _setTable(pkTable);

            if (!_isAccessibleTable()) {
                continue;
            }

            pkTableName = _getTableName();
            fkTable     = constraint.getRef();

            _setTable(fkTable);

            if (!_isAccessibleTable()) {
                continue;
            }

            fkTableName    = _getTableName();
            pkTableCatalog = _getCatalogName(pkTable);
            pkTableSchema  = _getSchemaName(pkTable);
            fkTableCatalog = _getCatalogName(fkTable);
            fkTableSchema  = _getSchemaName(fkTable);
            mainCols       = constraint.getMainColumns();
            refCols        = constraint.getRefColumns();
            columnCount    = refCols.length;
            fkName         = constraint.getFkName();

            // CHECKME:
            // this should be what gives the correct name:
            // pkName   = constraint.getPkName(); ???
            pkName = constraint.getMainIndex().getName().name;

            switch (constraint.getDeleteAction()) {

                case Constraint.CASCADE :
                    deleteRule =
                        ValuePool.getInt(DatabaseMetaData.importedKeyCascade);
                    break;

                case Constraint.SET_DEFAULT :
                    deleteRule = ValuePool.getInt(
                        DatabaseMetaData.importedKeySetDefault);
                    break;

                case Constraint.SET_NULL :
                    deleteRule =
                        ValuePool.getInt(DatabaseMetaData.importedKeySetNull);
                    break;

                case Constraint.NO_ACTION :
                default :
                    deleteRule = ValuePool.getInt(
                        DatabaseMetaData.importedKeyNoAction);
            }

            switch (constraint.getUpdateAction()) {

                case Constraint.CASCADE :
                    updateRule =
                        ValuePool.getInt(DatabaseMetaData.importedKeyCascade);
                    break;

                case Constraint.SET_DEFAULT :
                    updateRule = ValuePool.getInt(
                        DatabaseMetaData.importedKeySetDefault);
                    break;

                case Constraint.SET_NULL :
                    updateRule =
                        ValuePool.getInt(DatabaseMetaData.importedKeySetNull);
                    break;

                case Constraint.NO_ACTION :
                default :
                    updateRule = ValuePool.getInt(
                        DatabaseMetaData.importedKeyNoAction);
            }

            for (int j = 0; j < columnCount; j++) {
                keySequence          = ValuePool.getInt(j + 1);
                pkColumnName         = _getColName(pkTable, mainCols[j]);
                fkColumnName         = _getColName(fkTable, refCols[j]);
                row                  = t.getNewRow();
                row[ipk_table_cat]   = pkTableCatalog;
                row[ipk_table_schem] = pkTableSchema;
                row[ipk_table_name]  = pkTableName;
                row[ipk_column_name] = pkColumnName;
                row[ifk_table_cat]   = fkTableCatalog;
                row[ifk_table_schem] = fkTableSchema;
                row[ifk_table_name]  = fkTableName;
                row[ifk_column_name] = fkColumnName;
                row[ikey_seq]        = keySequence;
                row[iupdate_rule]    = updateRule;
                row[idelete_rule]    = deleteRule;
                row[ifk_name]        = fkName;
                row[ipk_name]        = pkName;
                row[ideferrability]  = deferrability;

                r.add(row);
            }
        }

        r.sortResult(new int[] {
            ifk_table_cat, ifk_table_schem, ifk_table_name, ikey_seq
        }, new int[] {
            1, 1, 1, 1
        });
        t.insert(r, _session);
        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the visible
     * <code>Index</code> objects for each accessible table defined
     * within this database.<p>
     * @return a <code>Table</code> object describing the visible
     *        <code>Index</code> objects for each accessible
     *        table defined within this database.
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_INDEXINFO(int tableIndex) throws SQLException {

        Table t = sysTables[tableIndex];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[tableIndex]);

            _addColumn(t, "TABLE_CAT", Types.VARCHAR);
            _addColumn(t, "TABLE_SCHEM", Types.VARCHAR);
            _addColumn(t, "TABLE_NAME", Types.VARCHAR, false);    // NOT NULL
            _addColumn(t, "NON_UNIQUE", Types.BIT, false);        // NOT NULL
            _addColumn(t, "INDEX_QUALIFIER", Types.VARCHAR);
            _addColumn(t, "INDEX_NAME", Types.VARCHAR);
            _addColumn(t, "TYPE", Types.SMALLINT, false);         // NOT NULL
            _addColumn(t, "ORDINAL_POSITION", Types.SMALLINT);
            _addColumn(t, "COLUMN_NAME", Types.VARCHAR);
            _addColumn(t, "ASC_OR_DESC", Types.VARCHAR);
            _addColumn(t, "CARDINALITY", Types.INTEGER);
            _addColumn(t, "PAGES", Types.INTEGER);
            _addColumn(t, "FILTER_CONDITION", Types.VARCHAR);
            t.createPrimaryKey(null);

            return t;
        }

        // calculated column values
        String  tableCatalog;
        String  tableSchema;
        String  tableName;
        Boolean nonUnique;
        String  indexQualifier;
        String  indexName;
        Integer indexType;
        Integer ordinalPosition;
        String  columnName;
        String  ascOrDesc;
        Integer cardinality;
        Integer pages;
        String  filterCondition;

        // secondary variables required to calculate column values and
        // producing sorted set of rows for insertion into the output table
        Enumeration tables;
        Table       table;
        Enumeration indicies;
        Index       index;
        int[]       indexColumnPositions;
        int         indexColumnPosition;
        int         indexColumnCount;
        boolean     isIndexUnique;
        Object      row[];
        Result      r;

        // column number mappings
        final int itable_cat        = 0;
        final int itable_schem      = 1;
        final int itable_name       = 2;
        final int inon_unique       = 3;
        final int iindex_qualifier  = 4;
        final int iindex_name       = 5;
        final int itype             = 6;
        final int iordinal_position = 7;
        final int icolumn_name      = 8;
        final int iasc_or_desc      = 9;
        final int icardinality      = 10;
        final int ipages            = 11;
        final int ifilter_condition = 12;

        // Initialization
        tables = _enumerateAllTables();
        r      = _createResultProto(t);

        // the only hsqldb index type, so far
        indexType = ValuePool.getInt(DatabaseMetaData.tableIndexOther);

        // Do it.
        while (tables.hasMoreElements()) {
            table = (Table) tables.nextElement();

            _setTable(table);

            if (!_isAccessibleTable()) {
                continue;
            }

            tableName       = _getTableName();
            tableCatalog    = _getCatalogName(table);
            tableSchema     = _getSchemaName(table);
            filterCondition = null;    // not supported yet
            indicies        = _enumerateTableIndicies();

            if (indicies == null) {
                continue;
            }

            // process all of the visible indicies for this table
            while (indicies.hasMoreElements()) {
                index                = (Index) indicies.nextElement();
                indexName            = index.getName().name;
                isIndexUnique        = index.isUnique();
                indexQualifier       = _getCatalogName(index);
                nonUnique            = valueOf(!isIndexUnique);
                cardinality          = _getIndexInfoCardinality(index);
                pages                = _getIndexInfoPages(index);
                indexColumnPositions = index.getColumns();
                indexColumnCount     = index.getVisibleColumns();

                for (int k = 0; k < indexColumnCount; k++) {
                    ordinalPosition     = ValuePool.getInt(k + 1);
                    indexColumnPosition = indexColumnPositions[k];
                    columnName = _getColName(table, indexColumnPosition);
                    ascOrDesc =
                        _getIndexInfoColDirection(index, indexColumnPosition);
                    row                    = t.getNewRow();
                    row[itable_cat]        = tableCatalog;
                    row[itable_schem]      = tableSchema;
                    row[itable_name]       = tableName;
                    row[inon_unique]       = nonUnique;
                    row[iindex_qualifier]  = indexQualifier;
                    row[iindex_name]       = indexName;
                    row[itype]             = indexType;
                    row[iordinal_position] = ordinalPosition;
                    row[icolumn_name]      = columnName;
                    row[iasc_or_desc]      = ascOrDesc;
                    row[icardinality]      = cardinality;
                    row[ipages]            = pages;
                    row[ifilter_condition] = filterCondition;

                    r.add(row);
                }
            }
        }

        r.sortResult(new int[] {
            inon_unique, itype, iindex_name, iordinal_position
        }, new int[] {
            1, 1, 1, 1
        });
        t.insert(r, _session);
        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the visible
     * primary key columns of each accessible table defined within
     * this database. <p>
     *
     * Each row is a PRIMARY KEY column description with the following
     * columns: <p>
     *
     * <pre>
     * TABLE_CAT   VARCHAR   table catalog
     * TABLE_SCHEM VARCHAR   table schema
     * TABLE_NAME  VARCHAR   table name
     * COLUMN_NAME VARCHAR   column name
     * KEY_SEQ     SMALLINT  sequence number within primary key
     * PK_NAME     VARCHAR   primary key name
     * </pre>
     * @return a <code>Table</code> object describing the visible
     *        primary key columns of each accessible table
     *        defined within this database.
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_PRIMARYKEYS(int tableIndex) throws SQLException {

        Table t = sysTables[tableIndex];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[tableIndex]);

            _addColumn(t, "TABLE_CAT", Types.VARCHAR);
            _addColumn(t, "TABLE_SCHEM", Types.VARCHAR);
            _addColumn(t, "TABLE_NAME", Types.VARCHAR, false);     // not null
            _addColumn(t, "COLUMN_NAME", Types.VARCHAR, false);    // not null
            _addColumn(t, "KEY_SEQ", Types.SMALLINT, false);       // not null
            _addColumn(t, "PK_NAME", Types.VARCHAR);
            t.createPrimaryKey(null);

            return t;
        }

        // calculated column values
        String  tableCatalog;
        String  tableSchema;
        String  tableName;
        String  columnName;
        Integer keySequence;
        String  primaryKeyName;

        // secondary variables required to calculate column values and
        // produce a sorted set of rows for insertion into an output table
        Enumeration tables;
        Table       table;
        Object[]    row;
        Index       index;
        int[]       columnPositions;
        int         columnCount;
        Result      r;

        // column number mappings
        final int itable_cat   = 0;
        final int itable_schem = 1;
        final int itable_name  = 2;
        final int icolumn_name = 3;
        final int ikey_seq     = 4;
        final int ipk_name     = 5;

        // Initialization
        tables = _enumerateAllTables();
        r      = _createResultProto(t);

        while (tables.hasMoreElements()) {
            table = (Table) tables.nextElement();
            index = table.getPrimaryIndex();

            if (index == null) {
                continue;
            }

            _setTable(table);

            if (!_isAccessibleTable() ||!_isTablePkVisible()) {
                continue;
            }

            tableCatalog    = _getCatalogName(table);
            tableSchema     = _getSchemaName(table);
            tableName       = _getTableName();
            primaryKeyName  = index.getName().name;
            columnPositions = index.getColumns();
            columnCount     = columnPositions.length;

            for (int j = 0; j < columnCount; j++) {
                columnName        = _getColName(table, columnPositions[j]);
                keySequence       = ValuePool.getInt(j + 1);
                row               = t.getNewRow();
                row[itable_cat]   = tableCatalog;
                row[itable_schem] = tableSchema;
                row[itable_name]  = tableName;
                row[icolumn_name] = columnName;
                row[ikey_seq]     = keySequence;
                row[ipk_name]     = primaryKeyName;

                r.add(row);
            }
        }

        r.sortResult(new int[]{ icolumn_name }, new int[]{ 1 });
        t.insert(r, _session);
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
     * REMARKS         VARCHAR   comment describing parameter/column
     * </pre> <p>
     * @return a <code>Table</code> object describing the
     *        return, parameter and result columns
     *        of the accessible routines defined
     *        within this database.
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_PROCEDURECOLUMNS(int tableIndex) throws SQLException {

        Table t = sysTables[tableIndex];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[tableIndex]);

            // ----------------------------------------------------------------
            // required
            // ----------------------------------------------------------------
            _addColumn(t, "PROCEDURE_CAT", Types.VARCHAR);
            _addColumn(t, "PROCEDURE_SCHEM", Types.VARCHAR);
            _addColumn(t, "PROCEDURE_NAME", Types.VARCHAR, false);    // not null
            _addColumn(t, "COLUMN_NAME", Types.VARCHAR, false);       // not null
            _addColumn(t, "COLUMN_TYPE", Types.SMALLINT, false);      // not null
            _addColumn(t, "DATA_TYPE", Types.SMALLINT, false);        // not null
            _addColumn(t, "TYPE_NAME", Types.VARCHAR, false);         // not null
            _addColumn(t, "PRECISION", Types.INTEGER);
            _addColumn(t, "LENGTH", Types.INTEGER);
            _addColumn(t, "SCALE", Types.SMALLINT);
            _addColumn(t, "RADIX", Types.SMALLINT);
            _addColumn(t, "NULLABLE", Types.SMALLINT, false);         // not null
            _addColumn(t, "REMARKS", Types.VARCHAR);

            // ----------------------------------------------------------------
            // extended
            // ----------------------------------------------------------------
            _addColumn(t, "SIGNATURE", Types.VARCHAR);

            // ----------------------------------------------------------------
            // required for JDBC sort contract
            // ----------------------------------------------------------------
            _addColumn(t, "SEQ", Types.INTEGER);

            // ----------------------------------------------------------------
            t.createPrimaryKey(null);
            _addIndex(t, null, new int[]{ 13 }, false);

            return t;
        }

        // calculated column values
        String  procedureCatalog;
        String  procedureSchema;
        String  procedureName;
        String  columnName;
        Integer columnType;
        Integer dataType;
        String  typeName;
        Integer precision;
        Integer length;
        Integer scale;
        Integer radix;
        Integer nullability;
        String  remark;
        String  sig;

        // secondary variables required to calculate column values and
        // produce a sorted set of rows for insertion into an output table
        String        alias;
        HsqlArrayList aliasList;
        String        className;
        Object[]      info;
        boolean       isPrimitive;
        Method        method;
        String        methodFQN;
        int           methodModifier;
        Enumeration   methods;
        String        parameterKey;
        String[]      parameterNames;
        Class[]       parameterTypes;
        Class         parameterType;
        Class         returnType;
        HsqlList      resultColumns;
        Object[]      row;
        Result        r;
        String        remarkKey;
        int           hnd;
        ClassLoader   cl;

        // Initialization
        methods = _enumerateAllAccessibleMethods(true);    // and aliases
        r       = _createResultProto(t);
        cl      = database.classLoader;

        // Do it.
        while (methods.hasMoreElements()) {
            info             = (Object[]) methods.nextElement();
            method           = (Method) info[0];
            aliasList        = (HsqlArrayList) info[1];
            methodModifier   = method.getModifiers();
            procedureCatalog = _getCatalogName(method);
            procedureSchema  = _getSchemaName(method);
            sig              = method.toString();
            methodFQN        = _getMethodFQN(method);
            procedureName    = methodFQN;
            returnType       = method.getReturnType();
            parameterTypes   = method.getParameterTypes();
            remarkKey        = _getProcRemarkKeyPrefix(method);
            className        = method.getDeclaringClass().getName();
            hnd = BundleHandler.getBundleHandle(className.replace('.', '_'),
                                                cl);

            // process return columns
            if (Void.TYPE.isAssignableFrom(returnType)) {

                // don't report return column
            } else {
                columnName = "@0";    // arbitrary name
                columnType =
                    ValuePool.getInt(DatabaseMetaData.procedureColumnReturn);

                _setClass(returnType);

                dataType  = _getTIDataType();
                typeName  = _getTITypeName();
                precision = _getTIPrec();
                length    = _getPColLen();
                scale     = _getTIDefScale();
                radix     = _getTINumPrecRadix();
                nullability =
                    ValuePool.getInt(DatabaseMetaData.procedureNullable);
                remark = _getProcRemark(hnd, _getProcRemarkKey(remarkKey, 0));

                _addPColRows(t, r, aliasList, procedureCatalog,
                             procedureSchema, procedureName, columnName,
                             columnType, dataType, typeName, precision,
                             length, scale, radix, nullability, remark, sig,
                             0);
            }

            // Process parameter columns
//            parameterNames      = _getClazzMethodParmNames(method);
            parameterNames = null;

            for (int j = 0; j < parameterTypes.length; j++) {
                parameterType = parameterTypes[j];
                parameterKey  = "@" + String.valueOf(j + 1);
                columnName    = (parameterNames == null) ? parameterKey
                                                         : parameterNames[j];

                _setClass(parameterType);

                columnType  = _getPColType();
                dataType    = _getTIDataType();
                typeName    = _getTITypeName();
                precision   = _getTIPrec();
                length      = _getPColLen();
                scale       = _getTIDefScale();
                radix       = _getTINumPrecRadix();
                nullability = _getPColNullability();
                remark = _getProcRemark(hnd,
                                        _getProcRemarkKey(remarkKey, j + 1));

                _addPColRows(t, r, aliasList, procedureCatalog,
                             procedureSchema, procedureName, columnName,
                             columnType, dataType, typeName, precision,
                             length, scale, radix, nullability, remark, sig,
                             j + 1);
            }

            // Process result columns
            resultColumns =
                (HsqlList) _getResultColumnsDescription(methodFQN);

            if (resultColumns == null) {
                continue;
            }

            columnType =
                ValuePool.getInt(DatabaseMetaData.procedureColumnResult);

            int count = resultColumns.size();

            for (int i = 0; i < count; i++) {
                row         = t.getNewRow();
                info        = (Object[]) resultColumns.get(i);
                columnName  = (String) info[0];
                dataType    = (Integer) info[1];
                typeName    = (String) info[2];
                precision   = (Integer) info[3];
                length      = (Integer) info[4];
                scale       = (Integer) info[5];
                radix       = (Integer) info[6];
                nullability = (Integer) info[7];
                remark      = (String) info[8];

                _addPColRows(t, r, aliasList, procedureCatalog,
                             procedureSchema, procedureName, columnName,
                             columnType, dataType, typeName, precision,
                             length, scale, radix, nullability, remark, sig,
                             parameterTypes.length + i + 2);
            }
        }

        r.sortResult(new int[] {
            1,     // procedure schem
            2,     // procedure name
            13,    // procedure signature
            14     // column sequence
        }, new int[] {
            1, 1, 1, 1
        });
        t.insert(r, _session);
        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the accessible
     * routines defined within the this database.
     * @return a <code>Table</code> object describing the accessible
     *        routines defined within the this database
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_PROCEDURES(int tableIndex) throws SQLException {

        Table t = sysTables[tableIndex];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[tableIndex]);

            // ----------------------------------------------------------------
            // required
            // ---------------------------------------------------------------
            _addColumn(t, "PROCEDURE_CAT", Types.VARCHAR);
            _addColumn(t, "PROCEDURE_SCHEM", Types.VARCHAR);
            _addColumn(t, "PROCEDURE_NAME", Types.VARCHAR, false);     // not null
            _addColumn(t, "NUM_INPUT_PARAMS", Types.INTEGER);
            _addColumn(t, "NUM_OUTPUT_PARAMS", Types.INTEGER);
            _addColumn(t, "NUM_RESULT_SETS", Types.INTEGER);
            _addColumn(t, "REMARKS", Types.VARCHAR);
            _addColumn(t, "PROCEDURE_TYPE", Types.SMALLINT, false);    // not null

            // ----------------------------------------------------------------
            // extended
            // ----------------------------------------------------------------
            _addColumn(t, "ORIGIN", Types.VARCHAR);
            _addColumn(t, "SIGNATURE", Types.VARCHAR);

            // ----------------------------------------------------------------
            t.createPrimaryKey(null);
            _addIndex(t, null, new int[]{ 9 }, false);

            return t;
        }

        // calculated column values
        String  catalog;
        String  schema;
        String  procName;
        String  remark;
        Integer procType;
        Integer numInputParams;
        Integer numOutputParams;
        Integer numResultSets;
        String  procOrigin;
        String  procSignature;

        // secondary variables required to calculate column values and
        // produce a sorted set of rows for insertion into an output table
        String        alias;
        HsqlArrayList aliasList;
        String        className;
        Enumeration   methods;
        Object[]      methodInfo;
        Method        method;
        Class         methodRType;
        Class[]       methodPTypes;
        String        methodFQN;
        String        methodOrigin;
        Object[]      row;
        Result        r;
        HsqlHashSet   hMethodNames;
        int           hnd;
        ClassLoader   cl;

        // Initialization
        methods = _enumerateAllAccessibleMethods(true);    // and aliases
        r       = _createResultProto(t);
        cl      = database.classLoader;

        // Do it.
        while (methods.hasMoreElements()) {
            methodInfo    = (Object[]) methods.nextElement();
            method        = (Method) methodInfo[0];
            methodFQN     = _getMethodFQN(method);
            aliasList     = (HsqlArrayList) methodInfo[1];
            methodOrigin  = (String) methodInfo[2];
            procSignature = method.toString();
            methodPTypes  = method.getParameterTypes();
            methodRType   = method.getReturnType();
            className     = method.getDeclaringClass().getName();
            hnd = BundleHandler.getBundleHandle(className.replace('.', '_'),
                                                cl);
            catalog         = _getCatalogName(method);
            schema          = _getSchemaName(method);
            procName        = methodFQN;
            numInputParams  = _getProcInputParmCount(methodPTypes);
            numOutputParams = _getProcOutputParmCount(methodPTypes);
            numResultSets   = _getProcResultSetCount(methodRType);
            remark = _getProcRemark(
                hnd, _getProcRemarkKey(_getProcRemarkKeyPrefix(method), -1));
            procType = _getProcType(methodRType, methodOrigin);

            _addProcRows(t, r, aliasList, method.getDeclaringClass(),
                         methodOrigin, catalog, schema, procName,
                         numInputParams, numOutputParams, numResultSets,
                         remark, procType, procSignature);
        }

        r.sortResult(new int[] {
            1,    // procedure schema
            2,    // procedure name
            9     // procedure sig
        }, new int[] {
            1, 1, 1
        });
        t.insert(r, _session);
        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the capabilities
     * and operating parameter properties for the engine hosting this
     * database, as well as their applicability in terms of scope and
     * namespace.
     *
     * Reported properties include all static JDBC <code>DatabaseMetaData</code>
     * capabilities values and certain <code>Database</code> object properties
     * and attribute values. <p>
     *
     * It is intended that all <code>Database</code> attributes and
     * properties that can be set via the database properties file,
     * JDBC connection properties or SQL SET/ALTER statements will
     * eventaully be reported here. <p>
     *
     * So if, in the future, ALTER DATABASE and/or ALTER SYSTEM commands
     * are added, then the database properties they affect will be
     * reported in this table as well. <p>
     *
     * Currently, the database properties reported in addition to the
     * static JDBC <code>DatabaseMetaData</code> capabilities values
     * are: <p>
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
     *     <LI>hsqldb.gc_interval - # new records between forced gc (0|NULL=>never)
     * </OL> <p>
     *
     * As database capabilities and supported proerties and attributes are added,
     * this list will be expanded, if required, to include all relevant additional
     * properties supported at that time. <p>
     * @return table of database and session operating parameters
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_PROPERTIES(int tableIndex) throws SQLException {

        Table t = sysTables[tableIndex];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[tableIndex]);

            _addColumn(t, "PROPERTY_SCOPE", Types.VARCHAR, false);
            _addColumn(t, "PROPERTY_NAMESPACE", Types.VARCHAR, false);
            _addColumn(t, "PROPERTY_NAME", Types.VARCHAR, false);
            _addColumn(t, "PROPERTY_VALUE", Types.VARCHAR);
            _addColumn(t, "PROPERTY_CLASS", Types.VARCHAR, false);
            t.createPrimaryKey(null);

            return t;
        }

        // calculated column values
        String scope;
        String namespace;
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
        final int inamespace = 1;
        final int iname      = 2;
        final int ivalue     = 3;
        final int iclass     = 4;

        // Do it:
        if (rStaticProperties == null) {

            // First, we want the names and values for
            // all JDBC capabilities constants
            scope             = "SESSION";
            namespace         = "java.sql.DatabaseMetaData";
            md = _session.getInternalConnection().getMetaData();
            methods           = DatabaseMetaData.class.getMethods();
            emptyParms        = new Object[]{};
            rStaticProperties = _createResultProto(t);

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
                        row[inamespace] = namespace;
                        row[iname]      = name;
                        row[ivalue]     = String.valueOf(value);
                        row[iclass]     = returnType.getName();

                        rStaticProperties.add(row);
                    } catch (Exception e) {}
                }
            }

            props     = database.getProperties();
            namespace = "database.properties";

            // sql.month
            row             = t.getNewRow();
            row[iscope]     = scope;
            row[inamespace] = namespace;
            row[iname]      = "sql.month";
            row[ivalue]     = props.getProperty("sql.month");
            row[iclass]     = "boolean";

            rStaticProperties.add(row);

            // sql.enforce_size
            row             = t.getNewRow();
            row[iscope]     = scope;
            row[inamespace] = namespace;
            row[iname]      = "sql.enforce_size";
            row[ivalue]     = props.getProperty("sql.enforce_size");
            row[iclass]     = "boolean";

            rStaticProperties.add(row);

            // sql.compare_in_locale
            row             = t.getNewRow();
            row[iscope]     = scope;
            row[inamespace] = namespace;
            row[iname]      = "sql.compare_in_locale";
            row[ivalue]     = props.getProperty("sql.compare_in_locale");
            row[iclass]     = "boolean";

            rStaticProperties.add(row);

            // sql.strict_fk
            row             = t.getNewRow();
            row[iscope]     = scope;
            row[inamespace] = namespace;
            row[iname]      = "sql.strict_fk";
            row[ivalue]     = props.getProperty("sql.strict_fk");
            row[iclass]     = "boolean";

            rStaticProperties.add(row);

            // sql.strong_fk
            row             = t.getNewRow();
            row[iscope]     = scope;
            row[inamespace] = namespace;
            row[iname]      = "sql.strong_fk";
            row[ivalue]     = props.getProperty("sql.strong_fk");
            row[iclass]     = "boolean";

            rStaticProperties.add(row);

            // hsqldb.cache_scale
            row             = t.getNewRow();
            row[iscope]     = scope;
            row[inamespace] = namespace;
            row[iname]      = "hsqldb.cache_scale";
            row[ivalue]     = props.getProperty("hsqldb.cache_scale");
            row[iclass]     = "int";

            rStaticProperties.add(row);

            // hsqldb.gc_interval
            row             = t.getNewRow();
            row[iscope]     = scope;
            row[inamespace] = namespace;
            row[iname]      = "hsqldb.gc_interval";
            row[ivalue]     = props.getProperty("hsqldb.gc_interval");
            row[iclass]     = "int";

            rStaticProperties.add(row);
        }

        // Now get a snapshot of the properties that may change over
        // the lifetime of the session
        scope     = "TRANSACTION";
        namespace = "org.hsqldb.Database";
        r         = _createResultProto(t);

        // log size
        Log log     = database.logger.lLog;
        int logSize = (log == null) ? 0
                                    : log.maxLogSize * 1 << 20;

        row             = t.getNewRow();
        row[iscope]     = scope;
        row[inamespace] = namespace;
        row[iname]      = "LOGSIZE";
        row[ivalue]     = String.valueOf(logSize);
        row[iclass]     = "int";

        r.add(row);

        Integer logType = (log == null) ? null
                                        : ValuePool.getInt(log.logType);

        row             = t.getNewRow();
        row[iscope]     = scope;
        row[inamespace] = namespace;
        row[iname]      = "LOGTYPE";
        row[ivalue]     = logType == null ? null
                                          : String.valueOf(logType);
        row[iclass]     = "int";

        r.add(row);

        // write delay
        row = t.getNewRow();

        Integer writeDelay = (log == null) ? null
                                           : ValuePool.getInt(log.writeDelay);

        row[inamespace] = namespace;
        row[iscope]     = scope;
        row[iname]      = "WRITE_DELAY";
        row[ivalue]     = String.valueOf(writeDelay);
        row[iclass]     = "int";

        r.add(row);

        // ignore case
        row             = t.getNewRow();
        row[iscope]     = scope;
        row[inamespace] = namespace;
        row[iname]      = "IGNORECASE";
        row[ivalue]     = String.valueOf(database.isIgnoreCase());
        row[iclass]     = "boolean";

        r.add(row);

        // referential integrity
        row             = t.getNewRow();
        row[iscope]     = scope;
        row[inamespace] = namespace;
        row[iname]      = "REFERENTIAL_INTEGRITY";
        row[ivalue]     = String.valueOf(database.isReferentialIntegrity());
        row[iclass]     = "boolean";

        r.add(row);
        r.addAll(rStaticProperties);
        r.sortResult(new int[] {
            iscope, inamespace, iname
        }, new int[] {
            1, 1, 1
        });
        t.insert(r, _session);

        r = null;

        t.setDataReadOnly(true);

        return t;
    }

    /**
     * <P>Retrieves a tabular description of the schemas accessible within the
     * specified <code>Session</code> context. <p>
     * @return table containing information about schemas defined within the database
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_SCHEMAS(int tableIndex) throws SQLException {

        Table t = sysTables[tableIndex];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[tableIndex]);

            _addColumn(t, "TABLE_SCHEM", Types.VARCHAR, false);    // not null
            _addColumn(t, "TABLE_CATALOG", Types.VARCHAR);
            t.createPrimaryKey(null);

            return t;
        }

        String      schemaName;
        Enumeration schemas;
        HsqlHashMap hRows;
        Object[]    row;
        Result      r;

        // column number mappings
        final int itable_schem = 0;
        final int itable_cat   = 1;

        // Initialization
        schemas = _enumerateSchemaNames();
        r       = _createResultProto(t);

        // Do it.
        while (schemas.hasMoreElements()) {
            row               = t.getNewRow();
            schemaName        = (String) schemas.nextElement();
            row[itable_schem] = schemaName;
            row[itable_cat]   = _getCatalogName(schemaName);

            r.add(row);
        }

        r.sortResult(new int[]{ itable_schem }, new int[]{ 1 });
        t.insert(r, _session);
        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing all visible
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
     * TRANSACTION_SIZE   INTEGER   # undo items in current transaction
     * TRANSACTION_NESTED BIT       is transaction nested?
     * </pre>
     * @return a <code>Table</code> object describing all visible
     * sessions
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_SESSIONS(int tableIndex) throws SQLException {

        Table t = sysTables[tableIndex];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[tableIndex]);

            _addColumn(t, "SESSION_ID", Types.INTEGER, false);
            _addColumn(t, "CONNECTED", Types.TIMESTAMP, false);
            _addColumn(t, "USER_NAME", Types.VARCHAR, false);
            _addColumn(t, "IS_ADMIN", Types.BIT, false);
            _addColumn(t, "AUTOCOMMIT", Types.BIT, false);
            _addColumn(t, "READONLY", Types.BIT, false);
            _addColumn(t, "MAXROWS", Types.INTEGER, false);

            // some sessions do not have a LAST_IDENTITY value
            _addColumn(t, "LAST_IDENTITY", Types.BIGINT);
            _addColumn(t, "TRANSACTION_SIZE", Types.INTEGER, false);
            _addColumn(t, "TRANSACTION_NESTED", Types.BIT, false);
            t.createPrimaryKey(null);

            return t;
        }

        // secondary variables required to calculate column values and
        // produce a sorted set of rows for insertion into the output table
        HsqlList sessions;
        Session  session;
        int      sessionId;
        Object[] row;
        Result   r;

        // column number mappings
        final int isession_id = 0;
        final int iconnected  = 1;
        final int iuser_name  = 2;
        final int iis_admin   = 3;
        final int iautocommit = 4;
        final int ireadonly   = 5;
        final int imaxrows    = 6;
        final int ilast_id    = 7;
        final int it_size     = 8;
        final int it_nested   = 9;

        // Initialisation
        sessions = _listVisibleSessions();
        r        = _createResultProto(t);

        // Do it.
        for (int i = 0; i < sessions.size(); i++) {
            session          = (Session) sessions.get(i);
            sessionId        = session.getId();
            row              = t.getNewRow();
            row[isession_id] = ValuePool.getInt(sessionId);
            row[iconnected]  = new Timestamp(session.getConnectTime());
            row[iuser_name]  = session.getUsername();
            row[iis_admin]   = valueOf(session.isAdmin());
            row[iautocommit] = valueOf(session.getAutoCommit());
            row[ireadonly]   = valueOf(session.isReadOnly());
            row[imaxrows]    = ValuePool.getInt(session.getMaxRows());

            Number tempLastId = (Number) session.getLastIdentity();

            row[ilast_id] = tempLastId == null ? null
                                               : ValuePool.getLong(
                                                   tempLastId.longValue());
            row[it_size] = ValuePool.getInt(session.getTransactionSize());

            // fredt - this is redundant as nested transactions do not endure beyond a single statement
            row[it_nested] = valueOf(session.isNestedTransaction());

            r.add(row);
        }

        r.sortResult(new int[]{ isession_id }, new int[]{ 1 });
        t.insert(r, _session);
        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the accessible
     * direct supertable, if any, of each accessible table defined
     * within the this database.
     * @return a <code>Table</code> object describing the accessible
     *        direct supertable, if any, of each accessible
     *        table defined within the this databas
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_SUPERTABLES(int tableIndex) throws SQLException {

        Table t = sysTables[tableIndex];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[tableIndex]);

            _addColumn(t, "TABLE_CAT", Types.VARCHAR);
            _addColumn(t, "TABLE_SCHEM", Types.VARCHAR);
            _addColumn(t, "TABLE_NAME", Types.VARCHAR, false);         // not null
            _addColumn(t, "SUPERTABLE_NAME", Types.VARCHAR, false);    // not null
            t.createPrimaryKey(null);

            return t;
        }

        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the accessible
     * direct supertype, if any, of each accessible user-defined type (UDT)
     * defined within this database.
     * @return a <code>Table</code> object describing the accessible
     *        direct supertype, if any, of each accessible
     *        user-defined type (UDT) defined within this database
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_SUPERTYPES(int tableIndex) throws SQLException {

        Table t = sysTables[tableIndex];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[tableIndex]);

            _addColumn(t, "TYPE_CAT", Types.VARCHAR);
            _addColumn(t, "TYPE_SCHEM", Types.VARCHAR);
            _addColumn(t, "TYPE_NAME", Types.VARCHAR, false);         // not null
            _addColumn(t, "SUPERTYPE_CAT", Types.VARCHAR);
            _addColumn(t, "SUPERTYPE_SCHEM", Types.VARCHAR);
            _addColumn(t, "SUPERTYPE_NAME", Types.VARCHAR, false);    // not null
            t.createPrimaryKey(null);

            return t;
        }

        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the visible access
     * rights for each accessible table definied within this database.
     *
     * In general, a row is included for each distinct right granted to
     * each visible user for each accesible table. This means that
     * ADMIN users see any rights that have been granted to any user
     * on any table, while regular users see only the rights granted
     * to themselves directly or logically through the PUBLIC user,
     * that make the table under consideration accessible in some way
     * (i.e. grants them any ability to insert, update, delete or select
     * from that table). However, in the case that ALL has been granted
     * to a non-ADMIN user, either explicitly by direct application
     * of the GRANT ALL command or logically by in some way having come
     * into posession of INSERT, UPDATE, DELETE and SELECT, then only
     * the ALL access right is reported, since it is all-inclusive.
     * The ALL access right alone is reported for all ADMIN users,
     * regardless of any explicit or implicit grants made to them or
     * revoked from them, since all ADMIN users posses the irrevokable
     * ADMIN role that grants them all access to all objects,
     * always. <p>
     *
     * <b>Note:</b> Up to and including HSQLDB 1.7.2, the access rights granted
     * on a table apply to all of the columns of that table as well. <p>
     * @return a <code>Table</code> object describing the visible
     *        access rights for each accessible table
     *        definied within this database
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_TABLEPRIVILEGES(int tableIndex) throws SQLException {

        Table t = sysTables[tableIndex];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[tableIndex]);

            _addColumn(t, "TABLE_CAT", Types.VARCHAR);
            _addColumn(t, "TABLE_SCHEM", Types.VARCHAR);
            _addColumn(t, "TABLE_NAME", Types.VARCHAR, false);      // not null
            _addColumn(t, "GRANTOR", Types.VARCHAR, false);         // not null
            _addColumn(t, "GRANTEE", Types.VARCHAR, false);         // not null
            _addColumn(t, "PRIVILEGE", Types.VARCHAR, false);       // not null
            _addColumn(t, "IS_GRANTABLE", Types.VARCHAR, false);    // not null
            t.createPrimaryKey(null);

            return t;
        }

        // calculated column values
        String tableCatalog;
        String tableSchema;
        String tableName;
        String grantorName;
        String granteeName;
        String privilege;
        String isGrantable;

        // secondary variables required to calculate column values and
        // produce a sorted set of rows for insertion into the output table
        HsqlArrayList users;
        User          user;
        HsqlArrayList tablePrivileges;
        Enumeration   tables;
        Table         table;
        HsqlName      accessKey;
        Object[]      row;
        Result        r;

        // column number mappings
        final int itable_cat    = 0;
        final int itable_schem  = 1;
        final int itable_name   = 2;
        final int igrantor      = 3;
        final int igrantee      = 4;
        final int iprivilege    = 5;
        final int iis_grantable = 6;

        // Initialization
        grantorName = UserManager.SYS_USER_NAME;
        users       = _userManager.listVisibleUsers(_session, true);
        tables      = _enumerateAllTables();
        r           = _createResultProto(t);

        // Do it.
        while (tables.hasMoreElements()) {
            table     = (Table) tables.nextElement();
            accessKey = table.getName();

            _setTable(table);

            // Only show table grants if session user is admin, has some right,
            // or the special PUBLIC user has some right.
            if (!_isAccessibleTable()) {
                continue;
            }

            tableName    = _getTableName();
            tableCatalog = _getCatalogName(table);
            tableSchema  = _getSchemaName(table);

            for (int i = 0; i < users.size(); i++) {
                user            = (User) users.get(i);
                granteeName     = user.getName();
                tablePrivileges = user.listTablePrivileges(accessKey);
                isGrantable     = (user.isAdmin()) ? "YES"
                                                   : "NO";

                for (int j = 0; j < tablePrivileges.size(); j++) {
                    privilege          = (String) tablePrivileges.get(j);
                    row                = t.getNewRow();
                    row[itable_cat]    = tableCatalog;
                    row[itable_schem]  = tableSchema;
                    row[itable_name]   = tableName;
                    row[igrantor]      = grantorName;
                    row[igrantee]      = granteeName;
                    row[iprivilege]    = privilege;
                    row[iis_grantable] = isGrantable;

                    r.add(row);
                }
            }
        }

        r.sortResult(new int[] {
            itable_schem, itable_name, iprivilege
        }, new int[] {
            1, 1, 1
        });
        t.insert(r, _session);
        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the accessible
     * tables defined within this database.
     * @return a <code>Table</code> object describing the accessible
     * tables defined within this database
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_TABLES(int tableIndex) throws SQLException {

        Table t = sysTables[tableIndex];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[tableIndex]);

            // -------------------------------------------------------------
            // required
            // -------------------------------------------------------------
            _addColumn(t, "TABLE_CAT", Types.VARCHAR);
            _addColumn(t, "TABLE_SCHEM", Types.VARCHAR);
            _addColumn(t, "TABLE_NAME", Types.VARCHAR, false);    // not null
            _addColumn(t, "TABLE_TYPE", Types.VARCHAR, false);    // not null
            _addColumn(t, "REMARKS", Types.VARCHAR);

            // -------------------------------------------------------------
            // JDBC3
            // -------------------------------------------------------------
            _addColumn(t, "TYPE_CAT", Types.VARCHAR);
            _addColumn(t, "TYPE_SCHEM", Types.VARCHAR);
            _addColumn(t, "TYPE_NAME", Types.VARCHAR);
            _addColumn(t, "SELF_REFERENCING_COL_NAME", Types.VARCHAR);
            _addColumn(t, "REF_GENERATION", Types.VARCHAR);

            // -------------------------------------------------------------
            // extended
            // ------------------------------------------------------------
            _addColumn(t, "NEXT_IDENTITY", Types.INTEGER);
            _addColumn(t, "READ_ONLY", Types.BIT);
            _addColumn(t, "HSQLDB_TYPE", Types.VARCHAR);
            _addColumn(t, "CACHE_FILE", Types.VARCHAR);
            _addColumn(t, "DATA_SOURCE", Types.VARCHAR);
            _addColumn(t, "IS_DESC", Types.BIT);

            // ------------------------------------------------------------
            t.createPrimaryKey(null);

            return t;
        }

        // calculated column values
        String tableName;
        String tableType;

        // secondary variables required to calculate column values and
        // produce a sorted set of rows for insertion into an output table
        Enumeration tables;
        Table       table;
        Object      row[];
        Result      r;
        HsqlName    accessKey;

        // column number mappings
        final int itable_cat   = 0;
        final int itable_schem = 1;
        final int itable_name  = 2;
        final int itable_type  = 3;
        final int iremark      = 4;
        final int itype_cat    = 5;
        final int itype_schem  = 6;
        final int itype_name   = 7;
        final int isref_cname  = 8;
        final int iref_gen     = 9;
        final int inext_id     = 10;
        final int iread_only   = 11;
        final int ihsqldb_type = 12;
        final int icache_file  = 13;
        final int idata_source = 14;
        final int iis_desc     = 15;

        // Initialization
        tables = _enumerateAllTables();
        r      = _createResultProto(t);

        // Do it.
        while (tables.hasMoreElements()) {
            table = (Table) tables.nextElement();

            _setTable(table);

            if (!_isAccessibleTable()) {
                continue;
            }

            tableName         = _getTableName();
            tableType         = _getTableType();
            row               = t.getNewRow();
            row[itable_cat]   = _getCatalogName(table);
            row[itable_schem] = _getSchemaName(table);
            row[itable_name]  = tableName;
            row[itable_type]  = tableType;
            row[iremark]      = _getTableRemark();
            row[inext_id]     = _getTableNextIdentity();
            row[iread_only]   = _getTableIsReadOnly();
            row[ihsqldb_type] = _getTableHsqlType();
            row[icache_file]  = _getTableCachePath();
            row[idata_source] = _getTableDataSource();
            row[iis_desc]     = _getTableIsDescDataSource();

            r.add(row);
        }

        r.sortResult(new int[] {
            itable_type, itable_schem, itable_name
        }, new int[] {
            1, 1, 1
        });
        t.insert(r, _session);
        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the table types
     * available in this database. <p>
     *
     * In general, the range of values that may be commonly encounted across
     * most DBMS implementations is: <p>
     *
     * <UL>
     *   <LI><FONT color='#FF00FF'>"TABLE"</FONT>
     *   <LI><FONT color='#FF00FF'>"VIEW"</FONT>
     *   <LI><FONT color='#FF00FF'>"SYSTEM TABLE"</FONT>
     *   <LI><FONT color='#FF00FF'>"GLOBAL TEMPORARY"</FONT>
     *   <LI><FONT color='#FF00FF'>"LOCAL TEMPORARY"</FONT>
     *   <LI><FONT color='#FF00FF'>"ALIAS"</FONT>
     *   <LI><FONT color='#FF00FF'>"SYNONYM"</FONT>
     * </UL> <p>
     *
     * As of HSQLDB 1.7.2, the engine supports and thus reports only a subset
     * of this range: <p>
     *
     * <UL>
     *   <LI><FONT color='#FF00FF'>"TABLE"</FONT>
     *    (HSQLDB MEMORY, CACHED and TEXT tables)
     *   <LI><FONT color='#FF00FF'>"VIEW"</FONT>  (Views)
     *   <LI><FONT color='#FF00FF'>"SYSTEM TABLE"</FONT>
     *    (The tables generated by this object)
     *   <LI><FONT color='#FF00FF'>"GLOBAL TEMPORARY"</FONT>
     *    (HSQLDB TEMP and TEMP TEXT tables)
     * </UL> <p>
     * @return a <code>Table</code> object describing the table types
     *        available in this database
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_TABLETYPES(int tableIndex) throws SQLException {

        Table t = sysTables[tableIndex];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[tableIndex]);

            _addColumn(t, "TABLE_TYPE", Types.VARCHAR, false);    // not null
            t.createPrimaryKey(null);

            return t;
        }

        Object[] row;
        Result   r;
        String   type;

        r = _createResultProto(t);

        for (int i = 0; i < _tableTypes.length; i++) {
            row    = t.getNewRow();
            type   = _tableTypes[i];
            row[0] = type;

            r.add(row);
        }

        r.sortResult(new int[]{ 0 }, new int[]{ 1 });
        t.insert(r, _session);
        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing of the usage
     * of accessible columns in accessible triggers defined within
     * the database. <p>
     *
     * Rows are ordered by TRIGGER_CAT, TRIGGER_SCHEM, TRIGGER_NAME, TABLE_CAT,
     * TABLE_SCHEM, TABLE_NAME, COLUMN_NAME, COLUMN_LIST, COLUMN_USAGE. <p>
     *
     * Each column usage description has the following columns: <p>
     *
     * <OL>
     * <LI><B>TRIGGER_CAT</B> <code>VARCHAR</code> (NULL) => Trigger catalog
     * <LI><B>TRIGGER_SCHEM</B> <code>VARCHAR</code> (NULL) => Trigger schema
     * <LI><B>TRIGGER_NAME</B> <code>VARCHAR</code> (NOT NULL) => Trigger name
     * <LI><B>TABLE_CAT</B> <code>VARCHAR</code> (NULL) =>
     *        Catalog of the table on which the trigger is defined
     * <LI><B>TABLE_SCHEM</B> <code>VARCHAR</code> (NULL) =>
     *        Schema of the table on which the trigger is defined
     * <LI><B>TABLE_NAME</B> <code>VARCHAR</code> (NOT NULL) =>
     *        Table on which the trigger is defined
     * <LI><B>COLUMN_NAME</B> <code>LONGVARCHAR</code> (NOT NULL) =>
     *        Name of the column used in the trigger
     * <LI><B>COLUMN_LIST</B> <code>VARCHAR</code> (NOT NULL) =>
     *        Column specified in UPDATE clause: Y/N
     * <LI><B>COLUMN_USAGE</B> <code>VARCHAR</code> (NOT NULL) =>
     *        How the column is used in the trigger?
     *        All applicable combinations of NEW, OLD, IN, OUT, and IN OUT.
     * </OL>
     * @return a <code>Table</code> object describing of the usage
     *        of accessible columns in accessible triggers
     *        defined within the database
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_TRIGGERCOLUMNS(int tableIndex) throws SQLException {

        Table t = sysTables[tableIndex];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[tableIndex]);

            _addColumn(t, "TRIGGER_CAT", Types.VARCHAR);
            _addColumn(t, "TRIGGER_SCHEM", Types.VARCHAR);
            _addColumn(t, "TRIGGER_NAME", Types.VARCHAR);
            _addColumn(t, "TABLE_CAT", Types.VARCHAR);
            _addColumn(t, "TABLE_SCHEM", Types.VARCHAR);
            _addColumn(t, "TABLE_NAME", Types.VARCHAR);
            _addColumn(t, "COLUMN_NAME", Types.VARCHAR);
            _addColumn(t, "COLUMN_LIST", Types.VARCHAR);
            _addColumn(t, "COLUMN_USAGE", Types.VARCHAR);
            t.createPrimaryKey(null);

            return t;
        }

        // calculated column values
        String triggerCatalog;
        String triggerSchema;
        String triggerName;
        String tableCatalog;
        String tableSchema;
        String tableName;
        String columnName;
        String columnList;
        String columnUsage;

        // secondary variables required to calculate column values and
        // produce a sorted set of rows for insertion into an output table
        Enumeration     tables;
        Table           table;
        HsqlArrayList[] vTrigs;
        HsqlArrayList   triggerList;
        TriggerDef      def;
        int             columnCount;
        Object          row[];
        Result          r;

        // column number mappings
        final int itrigger_cat   = 0;
        final int itrigger_schem = 1;
        final int itrigger_name  = 2;
        final int itable_cat     = 3;
        final int itable_schem   = 4;
        final int itable_name    = 5;
        final int icolumn_name   = 6;
        final int icolumn_list   = 7;
        final int icolumn_usage  = 8;

        // Initialization
        tables = _enumerateUserTables();
        r      = _createResultProto(t);

        // currently, the only supported types
        columnList  = "Y";
        columnUsage = "IN";

        // Do it.
        while (tables.hasMoreElements()) {
            table  = (Table) tables.nextElement();
            vTrigs = table.vTrigs;

            if (vTrigs == null) {
                continue;
            }

            _setTable(table);

            if (!_isAccessibleTable()) {
                continue;
            }

            tableCatalog   = _getCatalogName(table);
            triggerCatalog = tableCatalog;
            tableSchema    = _getSchemaName(table);
            triggerSchema  = tableSchema;
            tableName      = _getTableName();
            columnCount    = table.getColumnCount();

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

                    for (int k = 0; k < columnCount; k++) {
                        columnName          = _getColName(table, k);
                        row                 = t.getNewRow();
                        row[itrigger_cat]   = triggerCatalog;
                        row[itrigger_schem] = triggerSchema;
                        row[itrigger_name]  = triggerName;
                        row[itable_cat]     = tableCatalog;
                        row[itable_schem]   = tableSchema;
                        row[itable_name]    = tableName;
                        row[icolumn_name]   = columnName;
                        row[icolumn_list]   = columnList;
                        row[icolumn_usage]  = columnUsage;

                        r.add(row);
                    }
                }
            }
        }

        r.sortResult(new int[] {
            itrigger_cat, itrigger_schem, itrigger_name, itable_cat,
            itable_schem, itable_name, icolumn_name, icolumn_list,
            icolumn_usage
        }, new int[] {
            1, 1, 1, 1, 1, 1, 1, 1, 1
        });
        t.insert(r, _session);
        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the accessible
     * triggers defined within the database. <p>
     *
     * Rows are ordered by TRIGGER_TYPE, TRIGGER_CAT, TRIGGER_SCHEM,
     * TRIGGER_NAME, TABLE_CAT, TABLE_SCHEM, TABLE_NAME. <p>
     *
     * Each trigger description has the following columns: <p>
     *
     * <OL>
     * <LI><B>TRIGGER_CAT</B> <code>VARCHAR</code> (NULL) => Trigger catalog
     * <LI><B>TRIGGER_SCHEM</B> <code>VARCHAR</code> (NULL) => Trigger Schema
     * <LI><B>TRIGGER_NAME</B> <code>VARCHAR</code> (NOT NULL) =>Trigger Name
     * <LI><B>TRIGGER_TYPE</B> <code>VARCHAR</code> (NOT NULL) =>
     *        When the trigger fires:
     *        BEFORE STATEMENT, BEFORE EACH ROW, BEFORE EVENT, AFTER STATEMENT,
     *        AFTER EACH ROW, and AFTER EVENT
     * <LI><B>TRIGGERING_EVENT</B> <code>VARCHAR</code> (NOT NULL) =>
     *        Events that fire the trigger: from INSERT, UPDATE, DELETE,
     *        STARTUP, SHUTDOWN, ERROR, LOGON, LOGOFF, CREATE, ALTER, DROP, SET
     * <LI><B>TABLE_CAT</B> <code>VARCHAR</code> (NULL) =>
     *        Catalog of the table on which the trigger is defined
     * <LI><B>TABLE_SCHEM</B> <code>VARCHAR</code> (NULL) =>
     *        for BASE_OBJECT_TYPE = TABLE or VIEW: the schema on which the
     *        trigger is defined (May be NULL);
     *        For BASE_OBJECT_TYPE = SCHEMA: the USER for which CREATE, ALTER,
     *        DROP, or SET statement fires trigger;
     *        For BASE_OBJECT_TYPE = DATABASE, NULL
     * <LI><B>BASE_OBJECT_TYPE</B> <code>VARCHAR</code> (NOT NULL) =>
     *        The base object on which the trigger is defined:
     *        TABLE, VIEW, SCHEMA, or DATABASE
     * <LI><B>TABLE_NAME</B> <code>VARCHAR</code> (NULL) =>
     *        If the base object type of the trigger is SCHEMA or DATABASE,
     *        then this column is NULL;
     *        if the base object type of the trigger is TABLE or VIEW, this
     *        column indicates the table/view name on which the trigger is
     *        defined
     * <LI><B>COLUMN_NAME</B> <code>VARCHAR</code> (NULL) => Name of the nested
     *        table column (if nested table trigger), else NULL
     * <LI><B>REFERENCING_NAMES</B> <code>VARCHAR</code> (NOT NULL) =>
     *        Name(s) used for referencing column value holders from within the
     *        trigger body code (e.g. "row", "old", "new")
     * <LI><B>WHEN_CLAUSE</B> <code>LONGVARCHAR</code> (NULL) =>
     *        An expression that must evaluate to TRUE for TRIGGER_BODY
     *        to execute.  If NULL, the trigger body always executes
     * <LI><B>STATUS</B> <code>VARCHAR</code> (NOT NULL) =>
     *        Whether the trigger is enabled: "ENABLED" or "DISABLED"
     * <LI><B>DESCRIPTION</B> <code>LONGVARCHAR</code> (NULL) => Trigger
     *        description. Useful for re-creating a trigger creation statement.
     * <LI><B>ACTION_TYPE</B> <code>VARCHAR</code> (NOT NULL) =>
     *        The action type of the trigger body:
     *       "CALL" or embeded language name
     *       (e.g. "JAVASCRIPT", "PROLOG", "SQLJ", "PL/SQL", etc)
     * <LI><B>TRIGGER_BODY</B> <code>LONGVARCHAR</code> (NOT NULL) =>
     *      Statement(s) executed, in the manner described by ACTION_TYPE,
     *      when the trigger fires
     * </OL>
     * @return a <code>Table</code> object describing the accessible
     * triggers defined within the database.
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_TRIGGERS(int tableIndex) throws SQLException {

        Table t = sysTables[tableIndex];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[tableIndex]);

            _addColumn(t, "TRIGGER_CAT", Types.VARCHAR);
            _addColumn(t, "TRIGGER_SCHEM", Types.VARCHAR);
            _addColumn(t, "TRIGGER_NAME", Types.VARCHAR, false);
            _addColumn(t, "TRIGGER_TYPE", Types.VARCHAR, false);
            _addColumn(t, "TRIGGERING_EVENT", Types.VARCHAR, false);
            _addColumn(t, "TABLE_CAT", Types.VARCHAR);
            _addColumn(t, "TABLE_SCHEM", Types.VARCHAR);
            _addColumn(t, "BASE_OBJECT_TYPE", Types.VARCHAR, false);
            _addColumn(t, "TABLE_NAME", Types.VARCHAR, false);
            _addColumn(t, "COLUMN_NAME", Types.VARCHAR);
            _addColumn(t, "REFERENCING_NAMES", Types.VARCHAR, false);
            _addColumn(t, "WHEN_CLAUSE", Types.VARCHAR);
            _addColumn(t, "STATUS", Types.VARCHAR, false);
            _addColumn(t, "DESCRIPTION", Types.VARCHAR, false);
            _addColumn(t, "ACTION_TYPE", Types.VARCHAR, false);
            _addColumn(t, "TRIGGER_BODY", Types.VARCHAR, false);
            t.createPrimaryKey(null);

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

        // secondary variables required to calculate column values and
        // produce a sorted set of rows for insertion into the output table
        Enumeration     tables;
        Table           table;
        HsqlArrayList[] vTrigs;
        HsqlArrayList   triggerList;
        TriggerDef      def;
        Object          row[];
        Result          r;

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
        tables = _enumerateUserTables();
        r      = _createResultProto(t);

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

            _setTable(table);

            if (!_isAccessibleTable()) {
                continue;
            }

            tableCatalog   = _getCatalogName(table);
            triggerCatalog = tableCatalog;
            tableSchema    = _getSchemaName(table);
            triggerSchema  = tableSchema;
            tableName      = _getTableName();

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

                    r.add(row);
                }
            }
        }

        r.sortResult(new int[] {
            itrigger_type, itrigger_cat, itrigger_schem, itrigger_name,
            itable_cat, itable_schem, itable_name
        }, new int[] {
            1, 1, 1, 1, 1, 1, 1
        });
        t.insert(r, _session);
        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the
     * JDBC-expected result for system-defined SQL types
     * supported as table columns.
     *
     * @return a <code>Table</code> object describing the
     *      system-defined SQL types supported as table columns
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_TYPEINFO(int tableIndex) throws SQLException {

        Table t = sysTables[tableIndex];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[tableIndex]);

            //-------------------------------------------
            // required by JDBC:
            // ------------------------------------------
            _addColumn(t, "TYPE_NAME", Types.VARCHAR, false);
            _addColumn(t, "DATA_TYPE", Types.SMALLINT, false);
            _addColumn(t, "PRECISION", Types.INTEGER);
            _addColumn(t, "LITERAL_PREFIX", Types.VARCHAR);
            _addColumn(t, "LITERAL_SUFFIX", Types.VARCHAR);
            _addColumn(t, "CREATE_PARAMS", Types.VARCHAR);
            _addColumn(t, "NULLABLE", Types.SMALLINT);
            _addColumn(t, "CASE_SENSITIVE", Types.BIT);
            _addColumn(t, "SEARCHABLE", Types.SMALLINT);
            _addColumn(t, "UNSIGNED_ATTRIBUTE", Types.BIT);
            _addColumn(t, "FIXED_PREC_SCALE", Types.BIT);
            _addColumn(t, "AUTO_INCREMENT", Types.BIT);
            _addColumn(t, "LOCAL_TYPE_NAME", Types.VARCHAR);
            _addColumn(t, "MINIMUM_SCALE", Types.SMALLINT);
            _addColumn(t, "MAXIMUM_SCALE", Types.SMALLINT);
            _addColumn(t, "SQL_DATA_TYPE", Types.INTEGER);
            _addColumn(t, "SQL_DATETIME_SUB", Types.INTEGER);
            _addColumn(t, "NUM_PREC_RADIX", Types.INTEGER);

            //-------------------------------------------
            // for JDBC sort contract:
            //-------------------------------------------
            _addColumn(t, "SEQ", Types.FLOAT);
            t.createPrimaryKey(null);

            return t;
        }

        Object[]  row;
        int       typeNumber;
        Result    r;
        final int itype_name          = 0;
        final int idata_type          = 1;
        final int iprecision          = 2;
        final int iliteral_prefix     = 3;
        final int iliteral_suffix     = 4;
        final int icreate_params      = 5;
        final int inullable           = 6;
        final int icase_sensitive     = 7;
        final int isearchable         = 8;
        final int iunsigned_attribute = 9;
        final int ifixed_prec_scale   = 10;
        final int iauto_increment     = 11;
        final int ilocal_type_name    = 12;
        final int iminimum_scale      = 13;
        final int imaximum_scale      = 14;
        final int isql_data_type      = 15;
        final int isql_datetime_sub   = 16;
        final int inum_prec_radix     = 17;

        //---------------------------------------
        final int iseq = 18;

        r = _createResultProto(t);

        for (int i = I_ARRAY; i <= I_XML; i++) {
            _iInternalType = i;

            Boolean supAsTabCol = _getTISupAsTCol();

            if (supAsTabCol == null ||!supAsTabCol.booleanValue()) {
                continue;
            }

            row                      = t.getNewRow();
            row[itype_name]          = _getTITypeName();
            row[idata_type]          = _getTIDataType();
            row[iprecision]          = _getTIPrec();
            row[iliteral_prefix]     = _getTILitPref();
            row[iliteral_suffix]     = _getTILitSuf();
            row[icreate_params]      = _getTICreateParams();
            row[inullable]           = _getTINullable();
            row[icase_sensitive]     = _getTICaseSensitive();
            row[isearchable]         = _getTISearchable();
            row[iunsigned_attribute] = _getTIUAttr();
            row[ifixed_prec_scale]   = _getTIFixedPrecScale();
            row[iauto_increment]     = _getTIAutoIncrement();
            row[ilocal_type_name]    = _getTILocalName();
            row[iminimum_scale]      = _getTIMinScale();
            row[imaximum_scale]      = _getTIMaxScale();
            row[isql_data_type]      = _getTISqlDataType();
            row[isql_datetime_sub]   = _getTISqlDateTimeSub();
            row[inum_prec_radix]     = _getTINumPrecRadix();

            // ------------------------------------------------
            row[iseq] = _getTISortKey();

            r.add(row);
        }

        r.sortResult(new int[]{ iseq }, new int[]{ 1 });
        t.insert(r, _session);
        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing,  in an extended
     * fashion, all of the standard (not user-defined) SQL types known to
     * this database, including its level of support for them (which may
     * be no support at all). <p>
     *
     * @return a <code>Table</code> object describing all of the
     *        standard SQL types known to this database
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_TYPEINFO_EXT(int tableIndex) throws SQLException {

        Table t = sysTables[tableIndex];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[tableIndex]);

            //-------------------------------------------
            // same as SYSTEM_TYPEINFO:
            // ------------------------------------------
            _addColumn(t, "TYPE_NAME", Types.VARCHAR, false);
            _addColumn(t, "DATA_TYPE", Types.SMALLINT, false);
            _addColumn(t, "PRECISION", Types.INTEGER);
            _addColumn(t, "LITERAL_PREFIX", Types.VARCHAR);
            _addColumn(t, "LITERAL_SUFFIX", Types.VARCHAR);
            _addColumn(t, "CREATE_PARAMS", Types.VARCHAR);
            _addColumn(t, "NULLABLE", Types.SMALLINT);
            _addColumn(t, "CASE_SENSITIVE", Types.BIT);
            _addColumn(t, "SEARCHABLE", Types.SMALLINT);
            _addColumn(t, "UNSIGNED_ATTRIBUTE", Types.BIT);
            _addColumn(t, "FIXED_PREC_SCALE", Types.BIT);
            _addColumn(t, "AUTO_INCREMENT", Types.BIT);
            _addColumn(t, "LOCAL_TYPE_NAME", Types.VARCHAR);
            _addColumn(t, "MINIMUM_SCALE", Types.SMALLINT);
            _addColumn(t, "MAXIMUM_SCALE", Types.SMALLINT);
            _addColumn(t, "SQL_DATA_TYPE", Types.INTEGER);
            _addColumn(t, "SQL_DATETIME_SUB", Types.INTEGER);
            _addColumn(t, "NUM_PREC_RADIX", Types.INTEGER);

            //-------------------------------------------
            // SQL CLI / ODBC - not in JDBC spec
            // ------------------------------------------
            _addColumn(t, "INTERVAL_PRECISION", Types.INTEGER);

            //-------------------------------------------
            // extended:
            //-------------------------------------------
            // level of support
            //-------------------------------------------
            _addColumn(t, "AS_TAB_COL", Types.BIT);

            // for instance, some executable methods take Connection
            // which does not map to a supported table column type
            // but which we show as JAVA_OBJECT in SYSTEM_PROCEDURECOLUMNS
            _addColumn(t, "AS_PROC_COL", Types.BIT);

            //-------------------------------------------
            // actual values for attributes that cannot be represented
            // within the limitations of the SQL CLI / JDBC interface
            //-------------------------------------------
            _addColumn(t, "MAX_PREC_ACT", Types.BIGINT);
            _addColumn(t, "MIN_SCALE_ACT", Types.INTEGER);
            _addColumn(t, "MAX_SCALE_ACT", Types.INTEGER);

            //-------------------------------------------
            // how do we store this internally
            //-------------------------------------------
            _addColumn(t, "COL_ST_CLS_NAME", Types.VARCHAR);
            _addColumn(t, "COL_ST_IS_SUP", Types.BIT);

            //-------------------------------------------
            // what is the standard Java mapping for the type?
            //-------------------------------------------
            _addColumn(t, "STD_MAP_CLS_NAME", Types.VARCHAR);
            _addColumn(t, "STD_MAP_IS_SUP", Types.BIT);

            //-------------------------------------------
            // what, if any, custom mapping do we provide?
            //-------------------------------------------
            _addColumn(t, "CST_MAP_CLS_NAME", Types.VARCHAR);
            _addColumn(t, "CST_MAP_IS_SUP", Types.BIT);

            //-------------------------------------------
            // what is the max representable and actual
            // character octet length, if applicable?
            //-------------------------------------------
            _addColumn(t, "MCOL_JDBC", Types.INTEGER);
            _addColumn(t, "MCOL_ACT", Types.BIGINT);

            //-------------------------------------------
            // what is the default or fixed scale, if applicable?
            //-------------------------------------------
            _addColumn(t, "DEF_OR_FIXED_SCALE", Types.INTEGER);

            //-------------------------------------------
            // Any type-specific remarks can go here
            //-------------------------------------------
            _addColumn(t, "REMARKS", Types.VARCHAR);

            //-------------------------------------------
            // required for JDBC sort contract:
            //-------------------------------------------
            _addColumn(t, "SEQ", Types.FLOAT);
            t.createPrimaryKey(null);

            return t;
        }

        Object[] row;
        int      typeNumber;
        Result   r;

        //-----------------------------------------
        // Same as SYSTEM_TYPEINFO
        //-----------------------------------------
        final int itype_name          = 0;
        final int idata_type          = 1;
        final int iprecision          = 2;
        final int iliteral_prefix     = 3;
        final int iliteral_suffix     = 4;
        final int icreate_params      = 5;
        final int inullable           = 6;
        final int icase_sensitive     = 7;
        final int isearchable         = 8;
        final int iunsigned_attribute = 9;
        final int ifixed_prec_scale   = 10;
        final int iauto_increment     = 11;
        final int ilocal_type_name    = 12;
        final int iminimum_scale      = 13;
        final int imaximum_scale      = 14;
        final int isql_data_type      = 15;
        final int isql_datetime_sub   = 16;
        final int inum_prec_radix     = 17;

        //------------------------------------------
        // Extentions
        //------------------------------------------
        final int iinterval_precision = 18;

        //------------------------------------------
        final int iis_sup_as_tcol = 19;
        final int iis_sup_as_pcol = 20;

        //------------------------------------------
        final int imax_prec_or_len_act = 21;
        final int imin_scale_actual    = 22;
        final int imax_scale_actual    = 23;

        //------------------------------------------
        final int ics_cls_name         = 24;
        final int ics_cls_is_supported = 25;

        //------------------------------------------
        final int ism_cls_name         = 26;
        final int ism_cls_is_supported = 27;

        //------------------------------------------
        final int icm_cls_name         = 28;
        final int icm_cls_is_supported = 29;

        //------------------------------------------
        final int imax_char_oct_len_jdbc = 30;
        final int imax_char_oct_len_act  = 31;

        //------------------------------------------
        final int idef_or_fixed_scale = 32;

        //------------------------------------------
        final int iremarks = 33;

        //------------------------------------------
        final int iseq = 34;

        r = _createResultProto(t);

        for (int i = I_ARRAY; i <= I_XML; i++) {
            _iInternalType           = i;
            row                      = t.getNewRow();
            row[itype_name]          = _getTITypeName();
            row[idata_type]          = _getTIDataType();
            row[iprecision]          = _getTIPrec();
            row[iliteral_prefix]     = _getTILitPref();
            row[iliteral_suffix]     = _getTILitSuf();
            row[icreate_params]      = _getTICreateParams();
            row[inullable]           = _getTINullable();
            row[icase_sensitive]     = _getTICaseSensitive();
            row[isearchable]         = _getTISearchable();
            row[iunsigned_attribute] = _getTIUAttr();
            row[ifixed_prec_scale]   = _getTIFixedPrecScale();
            row[iauto_increment]     = _getTIAutoIncrement();
            row[ilocal_type_name]    = _getTILocalName();
            row[iminimum_scale]      = _getTIMinScale();
            row[imaximum_scale]      = _getTIMaxScale();
            row[isql_data_type]      = _getTISqlDataType();
            row[isql_datetime_sub]   = _getTISqlDateTimeSub();
            row[inum_prec_radix]     = _getTINumPrecRadix();

            //------------------------------------------
            row[iinterval_precision] = _getTIIntervalPrec();

            //------------------------------------------
            row[iis_sup_as_tcol] = _getTISupAsTCol();
            row[iis_sup_as_pcol] = _getTISupAsPCol();

            //------------------------------------------
            row[imax_prec_or_len_act] = _getTIPrecAct();
            row[imin_scale_actual]    = _getTIMinScaleAct();
            row[imax_scale_actual]    = _getTIMaxScaleAct();

            //------------------------------------------
            row[ics_cls_name]         = _getTIColStClsName();
            row[ics_cls_is_supported] = _getTIColStClsSup();

            //------------------------------------------
            row[ism_cls_name]         = _getTIStdMapClsName();
            row[ism_cls_is_supported] = _getTIStdMapClsSup();

            //------------------------------------------
            row[icm_cls_name] = _getTICstMapClsName();

            try {
                _classForName((String) row[icm_cls_name]);

                row[icm_cls_is_supported] = Boolean.TRUE;
            } catch (Exception e) {
                row[icm_cls_is_supported] = Boolean.FALSE;
            }

            //------------------------------------------
            row[imax_char_oct_len_jdbc] = _getTICharOctLen();
            row[imax_char_oct_len_act]  = _getTICharOctLenAct();

            //------------------------------------------
            row[idef_or_fixed_scale] = _getTIDefScale();

            //------------------------------------------
            row[iremarks] = _getTIRemarks();

            //------------------------------------------
            row[iseq] = _getTISortKey();

            r.add(row);
        }

        r.sortResult(new int[]{ iseq }, new int[]{ 1 });
        t.insert(r, _session);
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
     * The Table object has the following columns: <p>
     * @return a <code>Table</code> object describing the accessible
     *        attrubutes of the accessible user-defined type
     *        (UDT) objects defined within this database
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_UDTATTRIBUTES(int tableIndex) throws SQLException {

        Table t = sysTables[tableIndex];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[tableIndex]);

            _addColumn(t, "TYPE_CAT", Types.VARCHAR);
            _addColumn(t, "TYPE_SCHEM", Types.VARCHAR);
            _addColumn(t, "TYPE_NAME", Types.VARCHAR, false);           // not null
            _addColumn(t, "ATTR_NAME", Types.VARCHAR, false);           // not null
            _addColumn(t, "DATA_TYPE", Types.SMALLINT, false);          // not null
            _addColumn(t, "ATTR_TYPE_NAME", Types.VARCHAR, false);      // not null
            _addColumn(t, "ATTR_SIZE", Types.INTEGER);
            _addColumn(t, "DECIMAL_DIGITS", Types.INTEGER);
            _addColumn(t, "NUM_PREC_RADIX", Types.INTEGER);
            _addColumn(t, "NULLABLE", Types.INTEGER);
            _addColumn(t, "REMARKS", Types.VARCHAR);
            _addColumn(t, "ATTR_DEF", Types.VARCHAR);
            _addColumn(t, "SQL_DATA_TYPE", Types.INTEGER);
            _addColumn(t, "SQL_DATETIME_SUB", Types.INTEGER);
            _addColumn(t, "CHAR_OCTET_LENGTH", Types.INTEGER);
            _addColumn(t, "ORDINAL_POSITION", Types.INTEGER, false);    // not null
            _addColumn(t, "IS_NULLABLE", Types.VARCHAR, false);         // not null
            _addColumn(t, "SCOPE_CATALOG", Types.VARCHAR);
            _addColumn(t, "SCOPE_SCHEMA", Types.VARCHAR);
            _addColumn(t, "SCOPE_TABLE", Types.VARCHAR);
            _addColumn(t, "SOURCE_DATA_TYPE", Types.SMALLINT);
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
     * support UDTs, so an empty table is returned.
     * @return a <code>Table</code> object describing the accessible
     * user-defined types defined in this database
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_UDTS(int tableIndex) throws SQLException {

        Table t = sysTables[tableIndex];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[tableIndex]);

            _addColumn(t, "TYPE_CAT", Types.VARCHAR);
            _addColumn(t, "TYPE_SCHEM", Types.VARCHAR);
            _addColumn(t, "TYPE_NAME", Types.VARCHAR, false);     // not null
            _addColumn(t, "CLASS_NAME", Types.VARCHAR, false);    // not null
            _addColumn(t, "DATA_TYPE", Types.VARCHAR, false);     // not null
            _addColumn(t, "REMARKS", Types.VARCHAR);
            _addColumn(t, "BASE_TYPE ", Types.SMALLINT);
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
    Table SYSTEM_USERS(int tableIndex) throws SQLException {

        Table t = sysTables[tableIndex];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[tableIndex]);

            _addColumn(t, "USER", Types.VARCHAR, false);
            _addColumn(t, "ADMIN", Types.BIT, false);
            t.createPrimaryKey(null);

            return t;
        }

        // secondary variables required to calculate column values and
        // produce a sorted set of rows for insertion into the output table
        HsqlArrayList users;
        User          user;
        int           userCount;
        String        userName;
        Object[]      row;

        // column number mappings
        final int iuser_name = 0;
        final int iis_admin  = 1;

        // Initialization
        users = _userManager.listVisibleUsers(_session, false);

        // Do it.
        for (int i = 0; i < users.size(); i++) {
            row             = t.getNewRow();
            user            = (User) users.get(i);
            row[iuser_name] = user.getName();
            row[iis_admin]  = valueOf(user.isAdmin());

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
     * @return a <code>Table</code> object describing the columns
     *        that are automatically updated when any value
     *        in a row is updated
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_VERSIONCOLUMNS(int tableIndex) throws SQLException {

        Table t = sysTables[tableIndex];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[tableIndex]);

            // ----------------------------------------------------------------
            // required by DatabaseMetaData.getVersionColumns result set
            // ----------------------------------------------------------------
            _addColumn(t, "SCOPE", Types.INTEGER);
            _addColumn(t, "COLUMN_NAME", Types.VARCHAR, false);       // not null
            _addColumn(t, "DATA_TYPE", Types.SMALLINT, false);        // not null
            _addColumn(t, "TYPE_NAME", Types.VARCHAR, false);         // not null
            _addColumn(t, "COLUMN_SIZE", Types.SMALLINT);
            _addColumn(t, "BUFFER_LENGTH", Types.INTEGER);
            _addColumn(t, "DECIMAL_DIGITS", Types.SMALLINT);
            _addColumn(t, "PSEUDO_COLUMN", Types.SMALLINT, false);    // not null

            // -----------------------------------------------------------------
            // required by DatabaseMetaData.getVersionColumns filter parameters
            // -----------------------------------------------------------------
            _addColumn(t, "TABLE_CAT", Types.VARCHAR);
            _addColumn(t, "TABLE_SCHEM", Types.VARCHAR);
            _addColumn(t, "TABLE_NAME", Types.VARCHAR, false);        // not null

            // -----------------------------------------------------------------
            t.createPrimaryKey(null);

            return t;
        }

        t.setDataReadOnly(true);

        return t;
    }

    /**
     * Retrieves a <code>Table</code> object describing the text
     * source of all <code>View</code> objects accessible to
     * the current user.
     * @return a tabular description of the text source of all
     *        <code>View</code> objects accessible to
     *        the user.
     * @throws SQLException if an error occurs while producing the table
     */
    Table SYSTEM_VIEWSOURCE(int tableIndex) throws SQLException {

        Table t = sysTables[tableIndex];

        if (t == null) {
            t = createBlankTable(sysTableHsqlNames[tableIndex]);

            _addColumn(t, "VIEW_CAT", Types.VARCHAR);
            _addColumn(t, "VIEW_SCHEM", Types.VARCHAR);
            _addColumn(t, "VIEW_NAME", Types.VARCHAR);
            _addColumn(t, "VIEW_SQL", Types.VARCHAR);
            t.createPrimaryKey(null);

            return t;
        }

        String      viewCatalog;
        String      viewSchema;
        String      viewName;
        String      viewSql;
        Enumeration tables;
        Table       table;
        Object[]    row;
        Result      r;
        final int   iview_cat   = 0;
        final int   iview_schem = 1;
        final int   iview_name  = 2;
        final int   iview_sql   = 3;

        tables = _enumerateUserTables();
        r      = _createResultProto(t);

        while (tables.hasMoreElements()) {
            table = (Table) tables.nextElement();

            if (!table.isView()) {
                continue;
            }

            _setTable(table);

            if (!_isAccessibleTable()) {
                continue;
            }

            viewCatalog = _getCatalogName(table);
            viewSchema  = _getSchemaName(table);
            viewName    = _getTableName();
            viewSql     = ((View) table).getStatement();
            row         = t.getNewRow();
            row[0]      = viewCatalog;
            row[1]      = viewSchema;
            row[2]      = viewName;
            row[3]      = viewSql;

            r.add(row);
        }

        r.sortResult(new int[] {
            0, 1, 2
        }, new int[] {
            1, 1, 1
        });
        t.insert(r, _session);
        t.setDataReadOnly(true);

        return t;
    }

// fredt@users
    private Boolean valueOf(boolean value) {
        return value ? Boolean.TRUE
                     : Boolean.FALSE;
    }
}
