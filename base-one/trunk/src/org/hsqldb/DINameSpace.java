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

/*
 * originally created as DINameSpace.java on February 21, 2003, 11:24 PM
 */


import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.Enumeration;

import org.hsqldb.lib.enum.CompositeEnumeration;
import org.hsqldb.lib.enum.EmptyEnumeration;
import org.hsqldb.lib.enum.SingletonEnumeration;

import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.HsqlHashMap;
import org.hsqldb.lib.HsqlHashSet;

/**
 * Provides catalog and schema name related definitions and functionality.
 *
 * @author  boucherb@users.sourceforge.net
 * @version 1.7.2
 * @since HSQLDB 1.7.2
 */
final class DINameSpace {

    private Database database;

    private boolean reportCatalogs = true;
    private boolean reportSchemas = true;

    /** Set { <code>Class</code> FQN <code>String</code> objects }. <p>
     *
     * The Set contains the names of the classes providing the public static
     * methods that are automatically made accessible to the PUBLIC user in
     * support of the expected SQL CLI scalar functions and other core
     * HSQLDB SQL functions and stored procedures. <p>
     */
    private static HsqlHashSet builtin = new HsqlHashSet();

    /** The <code>DEFINITION_SCHEMA</code> schema name.    */
    static final String DEFN_SCHEMA = "DEFINITION_SCHEMA";

    /** The <code>DEFINITION_SCHEMA</code> schema name plus the schema
     * separator character. */
    private static final String DEFN_SCHEMA_DOT = DEFN_SCHEMA + ".";

    /** Length of <code>QS_DEFN_SCHEMA_DOT</code>. */
    private static final int DEFN_SCHEMA_DOT_LEN = DEFN_SCHEMA_DOT.length();

    /** The <code>INFORMATION_SCHEMA</code> schema name. */
    static final String INFO_SCHEMA = "INFORMATION_SCHEMA";

    /** The <code>INFORMATION_SCHEMA</code> schema name plus the schema
     * separator character. */
    private static final String INFO_SCHEMA_DOT = INFO_SCHEMA + ".";

    /** Length of <code>INFO_SCHEMA_DOT</code>. */
    private static final int INFO_SCHEMA_DOT_LEN = INFO_SCHEMA_DOT.length();

    /** The <code>PUBLIC</code> schema name.    */
    static final String PUB_SCHEMA = "PUBLIC";

    /** The <code>PUBLIC</code> schema name plus the schema
    * separator character.*/
    private static final String PUB_SCHEMA_DOT = PUB_SCHEMA + ".";

    /** Length of <code>PUB_SCHEMA_DOT</code>.    */
    private static final int PUB_SCHEMA_DOT_LEN = PUB_SCHEMA_DOT.length();

    /** List of system schema names:
     * { DEFINITION_SCHEMA, INFORMATION_SCHEMA, PUBLIC } */
    private static final HsqlArrayList sysSchemas = new HsqlArrayList();

    static {
        sysSchemas.add(DEFN_SCHEMA);
        sysSchemas.add(INFO_SCHEMA);
        sysSchemas.add(PUB_SCHEMA);

        builtin.add("org.hsqldb.Library");
        builtin.add("org.hsqldb.DatabaseClassLoader");
        builtin.add("java.lang.Math");
    }

    public DINameSpace(Database database) throws SQLException {
        Trace.doAssert(database != null, "database is null");
        this.database = database;
        HsqlProperties p = database.getProperties();
        reportCatalogs = p.isPropertyTrue("hsqldb.catalogs",reportCatalogs);
        reportSchemas = p.isPropertyTrue("hsqldb.schemas",reportSchemas);

    }

    /** Retrieves the declaring <code>Class</code> object for the specified
     * fully qualified method name, using, if possible, the classLoader
     * attribute of the database.<p>
     *
     * @param fqn the fully qualified name of the method for which to
     *        retrieve the declaring <code>Class</code> object.
     * @return the declaring <code>Class</code> object for the
     *        specified fully qualified method name
     */
    Class classForMethodFQN(String fqn) {

        try {
            return classForName(fqn.substring(0, fqn.lastIndexOf('.')));
        } catch (Exception e) {}

        return null;
    }

    /** Retreives the <code>Class</code> object specified by the
     * <code>name</code> argument using the database class loader.
     * @param name the fully qulified name of the <code>Class</code>
     *      object to retrieve.
     * @throws ClassNotFoundException if the specified class object
     *      cannot be found
     * @return the <code>Class</code> object specified by the
     * <code>name</code> argument
     */
    Class classForName(String name) throws ClassNotFoundException {

        return (database.classLoader == null)
        ? Class.forName(name)
        : Class.forName(name, true, database.classLoader);
    }

    /** Retrieves an <code>Enumeration</code> object whose elements form the
     * set of distinct names of all catalogs visible in specified Database. <p>
     *
     * <b>Note:</b> in the present implementation, the returned
     * <code>Enumeration</code> object is a <code>SingletonEnumeration</code>
     * whose single element is the database name.  HSQLDB currently does
     * not support the concept a single engine hosting multiple catalogs.
     * @return An enumeration of <code>String</code> objects naming the
     *      catalogs visible to the specified <code>Session</code>
     * @throws SQLException never (reserved for future use)
     */
    Enumeration enumCatalogNames() throws SQLException {
        return reportCatalogs
        ? new SingletonEnumeration(database.getName())
        : EmptyEnumeration.instance;
    }

    Enumeration enumSysSchemaNames() throws SQLException {
        return reportSchemas
        ? sysSchemas.elements()
        : EmptyEnumeration.instance;
    }

    /** Retrieves an enumeration of the names of schemas visible in
     * the context of the specified <code>Session</code>. <p>
     *
     * @return An enumeration of <code>Strings</code> naming the schemas
     *      visible to the specified <code>Session</code>
     */
    Enumeration enumVisibleSchemaNames(Session session) throws SQLException {

        HsqlArrayList   users;
        HsqlArrayList   userNames;
        UserManager     userManager;

        if (!reportSchemas || session == null) {
            return EmptyEnumeration.instance;
        }

        userManager     = database.getUserManager();
        users           = userManager.listVisibleUsers(session,false);
        userNames       = new HsqlArrayList();

        for (int i = 0; i < users.size(); i++) {
            User u = (User) users.get(i);
            userNames.add(u.getName());
        }

        return new
        CompositeEnumeration(
            enumSysSchemaNames(),
            userNames.elements()
        );
    }

    /** Retrieves the one-and-only correct <code>HsqlName</code> instance
     * for the current JVM session, using the s argument as a key to
     * look up the <code>HsqlName</code> instance in the repository
     * specified by the map argument.
     * @param s the lookup key
     * @param map the HsqlName instance repository
     * @return the one-and-only correct <code>HsqlName</code> instance
     *      for the specified key, <code>s</code>, in the current
     *      JVM session.
     * @see HsqlName
     */
    HsqlName findOrCreateHsqlName(String s, HsqlHashMap map) {

        HsqlName name = (HsqlName) map.get(s);

        if (name == null) {
            try {
                name = new HsqlName(s, false);
                map.put(s, name);
            } catch (Exception e) {}
        }

        return name;
    }

    /** Finds a regular (non-temp, non-system) table or view, if any,
     * corresponding to the given table name, relative to this object's
     * database attribute.<p>
     *
     * Basically, the PUBLIC schema name, in the form of a schema qualifier,
     * is removed from the specified database object identifier and then the
     * usual process for finding a non-temp, non-system table or view is
     * performed using the resulting simple identifier.
     * @return the non-temp, non-system user table, if any,
     *      corresponding to the given table name,.
     * @param name the name of the table to find, possibly prefixed
     *      with the PUBLIC schema qualifier
     */
    Table findPubSchemaTable(String name) {

        return (
            !reportSchemas ||
            name == null ||
            !name.startsWith(PUB_SCHEMA_DOT)
        )
        ? null
        : database.findUserTable(name.substring(PUB_SCHEMA_DOT_LEN));
    }

    /** Finds a TEMP [TEXT] table, if any, corresponding to
     * the given database object identifier, relative to the specified
     * session.<p>
     *
     * @return the TEMP [TEXT] table, if any, corresponding to
     * the given table name, relative to the specified session.
     * @param name the name of the table to find, possibly prefixed with
     * a schema qualifier
     */
    Table findUserSchemaTable(String name, Session session) {
        String prefix;

        if (name == null || session == null) {
            return null;
        }

        // PRE:  we assume user name is never null or ""
        prefix = session.getUsername() + ".";

        return name.startsWith(prefix) && reportSchemas
        ? database.findUserTable(name.substring(prefix.length()),session)
        : null;

    }

    /** Retrieves the name of the catalog corresponding to the indicated
     * object. <p>
     *
     * <B>Note:</B> <code>database.getName()</code> is returned whenever
     * all a non-null parameter is specified.  This a stub that will be used
     * until such time, if ever, that the engine actually supports the
     * concept of multiple hosted catalogs. <p>
     * @return the specified object's catalog name, or null if the object
     *          is null.
     * @param o the object whose catalog name is to be retrieved
     */
    String getCatalogName(Object o) {
        return (!reportCatalogs || o == null)
        ? null
        : database.getName();
    }

    /** Retrieves a map from each distinct
     * value of the database alias map to the list of keys
     * in the input map mapping to that value
     */
    HsqlHashMap getInverseAliasMap() {

        HsqlHashMap   mapIn;
        HsqlHashMap   mapOut;
        Enumeration   keys;
        Object        key;
        Object        value;
        HsqlArrayList keyList;

        // TODO:  This could be made a whole lot faster if HsqlHashMap supported
        // a cached hashCode based equals() method.  As it is, two HsqlHashMap
        // references are considered equal iff they refer to the indentical
        // object, which is useless for this application

        mapIn         = database.getAlias();
        mapOut        = new HsqlHashMap();

        keys          = mapIn.keys();

        while (keys.hasMoreElements()) {
            key       = keys.nextElement();
            value     = mapIn.get(key);
            keyList = (HsqlArrayList) mapOut.get(value);

            if (keyList == null) {
                keyList = new HsqlArrayList();

                mapOut.put(value, keyList);
            }

            keyList.add(key);
        }

        return mapOut;
    }

    static String getMethodFQN(Method m) {
        return m == null
        ? null
        : m.getDeclaringClass().getName() + '.' + m.getName();
    }

    /** Retrieves the name of the schema corresponding to the indicated object,
     * in the context of the specified <code>Session</code>.<p>
     *
     * The current implementation makes the determination as follows: <p>
     *
     * <OL>
     * <LI> if the specifed object is <code>null</code>, then <code>null</code> is
     *      returned immediately.
     *
     * <LI> if the specified object is a Method or Class instance,
     *      then "DEFINITION_SCHEMA" is returned if the object can be classified
     *      as builtin (made available automatically by the engine).  Otherwise,
     *      "PUBLIC" is returned, indicating a user-defined database object.
     *
     * <LI> if the specified object is an <code>org.hsqldb.Index</code> instance,
     *      then either the name of the schema of the table containing the index
     *      is returned, or null is returned if no table containing the index
     *      object can be found in the context of the specified
     *      <code>Session</code> object.
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
    String getSchemaName(Object o) {

        Class c;

        if (!reportSchemas || o == null) {
            return null;
        }

        if (o instanceof String) {
           // maybe the name of a DOMAIN?
            if (Column.hTypes.get(o) != null) {
                return DEFN_SCHEMA;
            }
            // Class name?
            if (isBuiltin((String) o)) {
                return DEFN_SCHEMA;
            }
            try {
                o = classForName((String)o);
            } catch (Exception e) {
                return null;
            }
        }

        c = null;

        if (o instanceof Method) {
            c = ((Method) o).getDeclaringClass();
        } else if (o instanceof Class) {
            c = (Class) o;
        }

        if (c != null) {
            return (isBuiltin(c)) ? DEFN_SCHEMA : PUB_SCHEMA;
        }

        Table table = null;

        if (o instanceof Table) {
            table = (Table) o;
        } else if (o instanceof Index) {
            table = tableForIndex((Index) o);
        }

        if (table == null) {
            return null;
        } else if (table.tableType == Table.SYSTEM_VIEW) {
            return INFO_SCHEMA;
        } else if (table.tableType == Table.SYSTEM_TABLE) {
            return DEFN_SCHEMA;
        } else if (table.isTemp()) {
            int id = table.ownerSessionId;
            HsqlArrayList sList = database.cSession;
            int size = sList.size();
            for (int i = 0; i < size; i++) {
                Session s = (Session) sList.get(i);
                if (s != null && s.getId() == id) {
                    return s.getUsername();
                }
            }
            return null;
        } else {
            return PUB_SCHEMA;
        }
    }

    /** @return
     * @param clazz
     */
    boolean isBuiltin(Class clazz) {
        return clazz == null
        ? false
        : builtin.contains(clazz.getName());
    }

    /** @param className
     * @return
     */
    boolean isBuiltin(String name) {
        return (name==null)
        ? false
        : builtin.contains(name);
    }

    /** @return
     * @param index
     */
    Table tableForIndex(Index index) {
        return index == null
        ? null
        : tableForIndexName(index.getName().name);
    }

    Table tableForIndexName(String indexName) {

        HsqlArrayList tables;
        int      size;
        Table    table;

        if (indexName == null) {
            return null;
        }

        // PRE:  we assume that if the session is non-null,
        // then it has a valid, open-for-business database
        // member attribute

        tables    = database.getTables();
        size      = tables.size();

        for (int i = 0; i < size; i++) {
            table = (Table) tables.get(i);

            if (table.getIndex(indexName) != null) {
                return table;
            }
        }

        return null;
    }

    String withoutCatalog(String name) {

        if (!reportCatalogs) {
            return name;
        }

        String cat_dot = getCatalogName(name) + ".";

        String out;

        if (name.startsWith(cat_dot)) {
            out = name.substring(cat_dot.length());
        } else {
            out = name;
        }

        return out;
    }

    String withoutDefnSchema(String name) {

        return !reportSchemas && name.startsWith(DEFN_SCHEMA_DOT)
        ? name.substring(DEFN_SCHEMA_DOT_LEN)
        : name;
    }

    String withoutInfoSchema(String name) {

        return !reportSchemas && name.startsWith(INFO_SCHEMA_DOT)
        ? name.substring(INFO_SCHEMA_DOT_LEN)
        : name;
    }

    /** Retrieves an <code>Enumeration</code> object describing the Java
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
     *
     */
    Enumeration enumRoutineMethods(String className, boolean andAliases) throws SQLException {

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
        invAliasMap = andAliases ? getInverseAliasMap()
        : null;

        try {
            clazz = classForName(className);
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
                info[1] = invAliasMap.get(getMethodFQN(method));
            }

            methodSet.add(info);
        }

        // return the enumeration
        return methodSet.elements();
    }

    /** Retrieves an <code>Enumeration</code> object describing the
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
     *
     */
    Enumeration enumAccessibleTriggerClassNames(User user) throws SQLException {

        Table           table;
        Class           clazz;
        HsqlHashSet     classSet;
        TriggerDef      triggerDef;
        HsqlArrayList[] triggerLists;
        HsqlArrayList   triggerList;
        HsqlArrayList   tableList;
        int             listSize;

        classSet = new HsqlHashSet();
        tableList = database.getTables();

        for (int i = 0; i < tableList.size(); i++) {
            table = (Table) tableList.get(i);

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

    /** Retrieves an <code>Enumeration</code> object describing the Java
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
     *
     */
    Enumeration enumAccessibleTriggerMethods(Session session) throws SQLException {

        Table           table;
        Class           clazz;
        Method          method;
        HsqlArrayList   methodList;
        HsqlHashSet     dupCheck;
        Class[]         pTypes;
        TriggerDef      triggerDef;
        HsqlArrayList[] triggerLists;
        HsqlArrayList   triggerList;
        HsqlArrayList   tableList;
        int             listSize;

        pTypes     = new Class[] {
            String.class, String.class, Object[].class
        };
        methodList = new HsqlArrayList();
        tableList  = database.getTables();
        dupCheck   = new HsqlHashSet();

        for (int i = 0; i < tableList.size(); i++) {
            table = (Table) tableList.get(i);

            if (!session.isAccessible(table.getName().name)) {
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

    /** Retrieves a composite enumeration consiting of the elements from
     * {@link #_enumerateAllRoutineMethods} and
     * {@link #_enumerateAllTriggerMethods}.
     * @param andAliases true if the alias lists for routine method elements are to be generated.
     * @throws SQLException if a database access error occurs
     * @return a composite enumeration consiting of the elements from
     * {@link #_enumerateAllRoutineMethods} and
     * {@link #_enumerateAllTriggerMethods}.
     *
     */
    Enumeration enumAllAccessibleMethods(Session session, boolean andAliases) throws SQLException {

        Enumeration out;
        Enumeration classNames;
        Enumeration methods;
        String      className;

        out        = EmptyEnumeration.instance;
        classNames = session.getGrantedClassNames(true).elements();

        while (classNames.hasMoreElements()) {
            className = (String) classNames.nextElement();
            methods   = enumRoutineMethods(className, andAliases);
            out       = new CompositeEnumeration(out, methods);
        }

        return new CompositeEnumeration(out,enumAccessibleTriggerMethods(session));
    }

    /** @return
     *
     */
    HsqlArrayList listVisibleSessions(Session session) {

        HsqlArrayList in;
        HsqlArrayList out;
        Session       observed;
        boolean       isObserverAdmin = session.isAdmin();
        int           observerId      = session.getId();

        in  = database.cSession;
        out = new HsqlArrayList();

        for (int i = 0; i < in.size(); i++) {
            observed = (Session) in.get(i);

            if (observed == null) {
                // do nothing
            } else if (isObserverAdmin || observed.getId() == observerId) {
                out.add(observed);
            }
        }

        return out;
    }

}
