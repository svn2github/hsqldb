/* Copyright (c) 2001-2004, The HSQL Development Group
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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.hsqldb.lib.WrapperIterator;
import org.hsqldb.lib.Iterator;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.HashMap;
import org.hsqldb.lib.HashSet;
import org.hsqldb.HsqlNameManager.HsqlName;

/**
 * Provides catalog and schema name related definitions and functionality,
 * as well as accessibility, enumeration and alias mapping functions regarding
 * Java Classes and Methods within the context of this name space. <p>
 *
 * @author  boucherb@users.sourceforge.net
 * @version 1.7.2
 * @since HSQLDB 1.7.2
 */

/** @todo fredt - move Trace.doAssert() literals to Trace */
final class DINameSpace {

    /** The Database for which the name space functionality is provided */
    private Database database;

// NO:   Properties are not initialized until log open, but DatabaseInformation
//       must be constructed before this happens to provide at least
//       structurally complete (if not contentful) system tables, just in case
//       there are views referencing them.  Better just to check the
//       database properties each time, as we might decide to introduce SQL
//       syntax to allow these values to change at runtime.  New methods,
//       isReportCatalogs() and isReportSchemas() have been introduced to do
//       this.
// TODO: Make this more efficient.  It seems like we've gone back a step,
//       since hitting the database properties object for every row of almost
//       every system table seems like a big waste of CPU.  Consider messaging
//       this object any time there is a change instead?
//
//    /** controls reporting of Catalog */
//    private boolean reportCatalogs;
//
//    /** Controls reporting of Schema */
//    private boolean reportSchemas;

    /**
     * Set { <code>Class</code> FQN <code>String</code> objects }. <p>
     *
     * The Set contains the names of the classes providing the public static
     * methods that are automatically made accessible to the PUBLIC user in
     * support of the expected SQL CLI scalar functions and other core
     * HSQLDB SQL functions and stored procedures. <p>
     */
    private static HashSet builtin = new HashSet();

    /** The <code>DEFINITION_SCHEMA</code> schema name. */
    static final String DEFN_SCHEMA = "DEFINITION_SCHEMA";

    /**
     * The <code>DEFINITION_SCHEMA</code> schema name plus the schema
     * separator character.
     */
    private static final String DEFN_SCHEMA_DOT = DEFN_SCHEMA + ".";

    /** Length of <code>DEFN_SCHEMA_DOT</code>. */
    private static final int DEFN_SCHEMA_DOT_LEN = DEFN_SCHEMA_DOT.length();

    /** The <code>INFORMATION_SCHEMA</code> schema name. */
    static final String INFO_SCHEMA = "INFORMATION_SCHEMA";

    /**
     * The <code>INFORMATION_SCHEMA</code> schema name plus the schema
     * separator character.
     */
    private static final String INFO_SCHEMA_DOT = INFO_SCHEMA + ".";

    /** Length of <code>INFO_SCHEMA_DOT</code>. */
    private static final int INFO_SCHEMA_DOT_LEN = INFO_SCHEMA_DOT.length();

    /** The <code>PUBLIC</code> schema name. */
    static final String PUB_SCHEMA = "PUBLIC";

    /**
     * The <code>PUBLIC</code> schema name plus the schema
     * separator character.
     */
    private static final String PUB_SCHEMA_DOT = PUB_SCHEMA + ".";

    /** Length of <code>PUB_SCHEMA_DOT</code>. */
    private static final int PUB_SCHEMA_DOT_LEN = PUB_SCHEMA_DOT.length();

    /**
     * List of system schema names:
     * { DEFINITION_SCHEMA, INFORMATION_SCHEMA, PUBLIC }
     */
    private static final HsqlArrayList sysSchemas = new HsqlArrayList();

    static {
        sysSchemas.add(DEFN_SCHEMA);
        sysSchemas.add(INFO_SCHEMA);
        sysSchemas.add(PUB_SCHEMA);
        builtin.add("org.hsqldb.Library");
        builtin.add("org.hsqldb.DatabaseClassLoader");
        builtin.add("java.lang.Math");
    }

    /**
     * Constructs a new name space support object for the
     * specified Database object. <p>
     *
     * @param database The Database object for which to provide name
     *      space support
     * @throws HsqlException if a database access error occurs
     */
    public DINameSpace(Database database) throws HsqlException {

        HsqlProperties p;

        Trace.doAssert(database != null, "database is null");

        this.database = database;

// NO:  This is too early
//        p              = database.getProperties();
//        reportCatalogs = p.isPropertyTrue("hsqldb.catalogs");
//        reportSchemas  = p.isPropertyTrue("hsqldb.schemas");
    }

    /**
     * Retrieves the declaring <code>Class</code> object for the specified
     * fully qualified method name, using (if possible) the classLoader
     * attribute of this object's database. <p>
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

    /**
     * Retrieves the <code>Class</code> object specified by the
     * <code>name</code> argument, using, if possible, the
     * classLoader attribute of the database. <p>
     *
     * @param name the fully qualified name of the <code>Class</code>
     *      object to retrieve.
     * @throws ClassNotFoundException if the specified class object
     *      cannot be found in the context of this name space
     * @return the <code>Class</code> object specified by the
     *      <code>name</code> argument
     */
    Class classForName(String name) throws ClassNotFoundException {

        try {
            return (database.classLoader == null) ? Class.forName(name)
                                                  : database.classLoader
                                                  .loadClass(name);
        } catch (NoClassDefFoundError err) {
            throw new ClassNotFoundException(err.toString());
        }
    }

    /**
     * Retrieves an enumeration whose elements form the set of distinct
     * names of all visible catalogs, relative to this object's database.
     * If catalog reporting is turned off, then this is
     * an empty enumeration. <p>
     *
     * <b>Note:</b> in the present implementation, if catalog reporting is
     * turned on, then the returned <code>Enumeration</code> object is a
     * <code>SingletonEnumeration</code> whose single element is the name
     * of this object's database; HSQLDB  currently does not support the
     * concept a single engine hosting multiple catalogs. <p>
     *
     * @return An enumeration of <code>String</code> objects naming all
     *      visible catalogs, relative to this object's database.
     * @throws HsqlException never (reserved for future use)
     */
    Iterator enumCatalogNames() throws HsqlException {
        return isReportCatalogs() ? new WrapperIterator(database.getPath())
                                  : new WrapperIterator();
    }

    /**
     * Retrieves an enumeration whose elements form the set of distinct names
     * of system schemas visible in this object's database. If schema reporting
     * is turned off, then this is an empty enumeration. <p>
     *
     * @return An enumeration of <code>String</code> objects naming the
     *      system schemas
     * @throws HsqlException never (reserved for future use)
     */
    Iterator enumSysSchemaNames() throws HsqlException {
        return isReportSchemas() ? sysSchemas.iterator()
                                 : new WrapperIterator();
    }

    /**
     * Retrieves an enumeration of the names of schemas visible in
     * the context of the specified session. If schema reporting
     * is turned off or a null session is specified, the this is
     * an empty enumeration. <p>
     *
     * @return An enumeration of <code>Strings</code> naming the schemas
     *      visible to the specified session
     * @param session The context in which to provide the enumeration
     * @throws HsqlException if a database access error occurs
     */
    Iterator enumVisibleSchemaNames(Session session) throws HsqlException {

        HsqlArrayList users;
        HsqlArrayList userNames;
        UserManager   userManager;

        if (!isReportSchemas() || session == null) {
            return new WrapperIterator();
        }

        userManager = database.getUserManager();
        users       = userManager.listVisibleUsers(session, false);
        userNames   = new HsqlArrayList();

        for (int i = 0; i < users.size(); i++) {
            User u = (User) users.get(i);

            userNames.add(u.getName());
        }

        return new WrapperIterator(enumSysSchemaNames(),
                                   userNames.iterator());
    }

    /**
     * Retrieves the one-and-only correct <code>HsqlName</code> instance
     * relative to the specified map, using the s argument as a key to
     * look up the instance. <p>
     *
     * @param s the lookup key
     * @param map the <code>HsqlName</code> instance repository
     * @return the one-and-only correct <code>HsqlName</code> instance
     *      for the specified key, <code>s</code>, relative to the
     *      specified map
     * @see HsqlName
     */
    HsqlName findOrCreateHsqlName(String s, HashMap map) {

        HsqlName name = (HsqlName) map.get(s);

        if (name == null) {
            try {
                name = database.nameManager.newHsqlName(s, false);

                map.put(s, name);
            } catch (Exception e) {}
        }

        return name;
    }

    /**
     * Finds the regular (non-temp, non-system) table or view (if any)
     * corresponding to the given database object identifier, relative to
     * this object's database.<p>
     *
     * Basically, the PUBLIC schema name, in the form of a schema qualifier,
     * is removed from the specified database object identifier and then the
     * usual process for finding a non-temp, non-system table or view is
     * performed using the resulting simple identifier. <p>
     *
     * @return the non-temp, non-system user table or view object (if any)
     *      corresponding to the given name.
     * @param name a database object identifier string representing the
     *      table/view object to find, possibly prefixed
     *      with the PUBLIC schema qualifier
     */
    Table findPubSchemaTable(String name) {

        return (!isReportSchemas() || name == null ||!name.startsWith(PUB_SCHEMA_DOT))
               ? null
               : database.findUserTable(name.substring(PUB_SCHEMA_DOT_LEN));
    }

    /**
     * Finds a TEMP [TEXT] table (if any) corresponding to
     * the given database object identifier, relative to the
     * this object's database and the specified session. <p>
     *
     * @return the TEMP [TEXT] table (if any) corresponding to
     *      the given database object identifier, relative to
     *      this object's database and the he specified session.
     * @param session The context in which to find the table
     * @param name a database object identifier string representing the
     *      table to find, possibly prefixed with a schema qualifier
     */
    Table findUserSchemaTable(String name, Session session) {

        String prefix;

        if (!isReportSchemas() || name == null || session == null) {
            return null;
        }

        // PRE:  we assume user name is never null or ""
        prefix = session.getUsername() + ".";

        return name.startsWith(prefix)
               ? database.findUserTable(name.substring(prefix.length()),
                                        session)
               : null;
    }

    /**
     * Retrieves the name of the catalog corresponding to the indicated
     * object. <p>
     *
     * <B>Note:</B> the name of this object's database is returned whenever
     * catalog reporting is turned on and a non-null parameter is specified.
     * This a stub that will be used until such time (if ever) that the
     * engine actually supports the concept of multiple hosted
     * catalogs. <p>
     *
     * @return the name of specified object's qualifying catalog, or null if
     *      the object is null or if catalog reporting is turned off.
     * @param o the object for which the name of its qualifying catalog
     *      is to be retrieved
     */
    String getCatalogName(Object o) {
        return (!isReportCatalogs() || o == null) ? null
                                                  : database.getPath();
    }

    /**
     * Retrieves a map from each distinct value of this object's database
     * SQL routine CALL alias map to the list of keys in the input map
     * mapping to that value. <p>
     *
     * @return The requested map
     */
    HashMap getInverseAliasMap() {

        HashMap       mapIn;
        HashMap       mapOut;
        Iterator      keys;
        Object        key;
        Object        value;
        HsqlArrayList keyList;

        // TODO:
        // update Database to dynamically maintain its own
        // inverse alias map.  This will make things *much*
        // faster for our  purposes here, without appreciably
        // slowing down Database
        mapIn  = database.getAlias();
        mapOut = new HashMap();
        keys   = mapIn.keySet().iterator();

        while (keys.hasNext()) {
            key     = keys.next();
            value   = mapIn.get(key);
            keyList = (HsqlArrayList) mapOut.get(value);

            if (keyList == null) {
                keyList = new HsqlArrayList();

                mapOut.put(value, keyList);
            }

            keyList.add(key);
        }

        return mapOut;
    }

    /**
     * Retrieves the fully qualified name of the specified Method object. <p>
     *
     * @param m The Method object for which to retreive the fully
     *      qualified name
     * @return the fully qualified name of the specified Method object.
     */
    static String getMethodFQN(Method m) {

        return m == null ? null
                         : m.getDeclaringClass().getName() + '.'
                           + m.getName();
    }

    /**
     * Retrieves the name of the schema corresponding to the indicated object,
     * in the context of this name space. <p>
     *
     * The current implementation makes the determination as follows: <p>
     *
     * <OL>
     * <LI> if schema reporting is turned off, then null is returned
     *      immediately.
     *
     * <LI> if the specifed object is <code>null</code>, then <code>null</code>
     *      is returned immediately.
     *
     * <LI> if the specified object is an <code>org.hsqldb.Table</code>
     *      instance and it is a system table, then "DEFINITION_SCHEMA" is
     *      returned.
     *
     * <LI> if the specified object is an <code>org.hsqldb.Table</code>
     *      instance and is a system view, then "INFORMATION_SCHEMA" is
     *      returned.
     *
     * <LI> if the specified object is an <code>org.hsqldb.Table</code>
     *      instance and it is a temp table, then either the name of the
     *      owning session user is returned, or null is returned if the owning
     *      session cannot be found in the context of this name space.
     *
     * <LI> if the specified object is an <code>org.hsqldb.Table</code>
     *      instance and it is has not been covered by any of the previous
     *      cases, then it is assumed to be a regular user-defined table
     *      and "PUBLIC" is returned.
     *
     * <LI> if the specified object is an <code>org.hsqldb.Index</code>
     *      instance, then either the name of the schema of the table
     *      containing the index is returned, or null is returned if no table
     *      containing the index object can be found in the context of this
     *      name space.
     *
     * <LI> if the specified object is a String instance, then it is checked to
     *      see if it names a built in DOMAIN or Class.  If it does, then
     *      "DEFINITION_SCHEMA" is returned.  If it does not, then an attempt
     *      is made to retrieve a Class object named by the string.  If the
     *      string names a Class accessible within this name space, then the
     *      corresponding Class object is passed on to the next step.
     *
     * <LI> if the specified object is a Method or Class instance,
     *      then "DEFINITION_SCHEMA" is returned if the object can be
     *      classified as builtin (made available automatically by the engine).
     *      Otherwise, "PUBLIC" is returned, indicating a user-defined database
     *      object.
     *
     * <LI> if none of the above points are satisfied, null is returned.
     *
     * </OL> <p>
     *
     * @return the name of the schema qualifying the specified object, or null
     *      if schema reporting is turned off or the specified object is null
     *      or cannot be qualified.
     * @param o the object for which the name of its qualifying schema is to
     *      be retrieved
     */
    String getSchemaName(Object o) {

        Class c;
        Table table;

        if (!isReportSchemas() || o == null) {
            return null;
        }

        if (o instanceof Table) {
            return ((Table) o).getSchemaName();
        }

        if (o instanceof Index) {
            table = tableForIndex((Index) o);

            return (table == null) ? null
                                   : table.getSchemaName();
        }

        if (o instanceof String) {

            // maybe the name of a DOMAIN?
            if (Types.typeAliases.get(o, Integer.MIN_VALUE)
                    != Integer.MIN_VALUE) {
                return DEFN_SCHEMA;
            }

            // ----------
            // Class name?
            if (isBuiltin((String) o)) {
                return DEFN_SCHEMA;
            }

            try {
                o = classForName((String) o);
            } catch (Exception e) {
                return null;
            }

            // ----------
        }

        c = null;

        if (o instanceof Method) {
            c = ((Method) o).getDeclaringClass();
        } else if (o instanceof Class) {
            c = (Class) o;
        }

        return (c == null) ? null
                           : isBuiltin(c) ? DEFN_SCHEMA
                                          : PUB_SCHEMA;
    }

    /**
     * Retrieves whether the indicated Class object is systematically
     * granted to PUBLIC in support of core operation. <p>
     *
     * @return whether the indicated Class object is systematically
     * granted to PUBLIC in support of core operation
     * @param clazz The Class object for which to make the determination
     */
    boolean isBuiltin(Class clazz) {
        return clazz == null ? false
                             : builtin.contains(clazz.getName());
    }

    /**
     * Retrieves whether the Class object indicated by the fully qualified
     * class name is systematically granted to PUBLIC in support of
     * core operation. <p>
     *
     * @return true if system makes grant, else false
     * @param name fully qualified name of a Class
     */
    boolean isBuiltin(String name) {
        return (name == null) ? false
                              : builtin.contains(name);
    }

    /**
     * Retrieves the Table object enclosing the specified Index object. <p>
     *
     * @return the Table object enclosing the specified Index
     *        object or null if no such Table exists
     *        in the context of this name space
     * @param index The index object for which to perform the search
     */
    Table tableForIndex(Index index) {
        return index == null ? null
                             : tableForIndexName(index.getName().name);
    }

    /**
     * Retrieves the Table object enclosing the Index object with the
     * specified name. <p>
     *
     * @param indexName The name if the Index object for which to
     *        perform the search
     * @return the Table object enclosing the specified Index
     *        object or null if no such Table exists
     *        in the context of this name space
     */
    Table tableForIndexName(String indexName) {

        HsqlName tableName = database.indexNameList.getOwner(indexName);

        return database.findUserTable(tableName.name);
    }

    /**
     * Retrieves the specified database object name, with the catalog
     * qualifier removed. <p>
     *
     * @param name the database object name from which to remove
     *        the catalog qualifier
     * @return the specified database object name, with the
     *        catalog qualifier removed
     */
    String withoutCatalog(String name) {

        if (!isReportCatalogs()) {
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

    /**
     * Retrieves the specified database object name, with the
     * DEFINTION_SCHEMA qualifier removed. <p>
     *
     * @param name the database object name from which to remove
     *        the schema qualifier
     * @return the specified database object name, with the
     *        schema qualifier removed
     */
    String withoutDefnSchema(String name) {

        return isReportSchemas() && name.startsWith(DEFN_SCHEMA_DOT)
               ? name.substring(DEFN_SCHEMA_DOT_LEN)
               : name;
    }

    /**
     * Retrieves the specified database object name, with the
     * INFORMATION_SCHEMA qualifier removed. <p>
     *
     * @param name the database object name from which to remove
     *        the schema qualifier
     * @return the specified database object name, with the
     *        schema qualifier removed
     */
    String withoutInfoSchema(String name) {

        return isReportSchemas() && name.startsWith(INFO_SCHEMA_DOT)
               ? name.substring(INFO_SCHEMA_DOT_LEN)
               : name;
    }

    /**
     * Retrieves an <code>Enumeration</code> object describing the Java
     * <code>Method</code> objects that are both the entry points
     * to executable SQL database objects (such as SQL functions and
     * stored procedures) within the context of this name space. <p>
     *
     * Each element of the <code>Enumeration</code> is an Object[3] array
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
     * as long as its parameters and return type are compatible with the
     * engine's supported SQL type / Java <code>Class</code> mappings. <p>
     *
     * @return <code>Enumeration</code> object whose elements represent the set
     *        of distinct <code>Method</code> objects accessible as
     *        executable as SQL routines within the current execution
     *        context.<p>
     *
     *        Elements are <code>Object[]</code> instances, with [0] being a
     *        <code>Method</code> object, [1] being an alias list object and
     *        [2] being the <code>String</code> "ROUTINE"<p>
     *
     *        If the <code>Method</code> object at index [0] has aliases,
     *        and the <code>andAliases</code> parameter is specified
     *        as <code>true</code>, then there is an alias list
     *        at index [1] whose elements are <code>String</code> objects
     *        whose values are the SQL call aliases for the method.
     *        Otherwise, the value of index [1] is <code>null</code>.
     * @param className The fully qualified name of the class for which to
     *        retrive the enumeration
     * @param andAliases if <code>true</code>, alias lists for qualifying
     *        methods are additionally retrieved.
     * @throws HsqlException if a database access error occurs
     *
     */
    Iterator enumRoutineMethods(String className,
                                boolean andAliases) throws HsqlException {

        Class         clazz;
        Method[]      methods;
        Method        method;
        int           mods;
        Object[]      info;
        HsqlArrayList aliasList;
        HsqlArrayList methodList;
        HashMap       invAliasMap;

        invAliasMap = andAliases ? getInverseAliasMap()
                                 : null;

        try {
            clazz = classForName(className);
        } catch (ClassNotFoundException e) {
            return new WrapperIterator();
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

        methodList = new HsqlArrayList(methods.length);

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

            methodList.add(info);
        }

        // return the enumeration
        return methodList.iterator();
    }

    /**
     * Retrieves an <code>Enumeration</code> object describing the
     * fully qualified names of all Java <code>Class</code> objects
     * that are both trigger body implementations and that are accessible
     * (whose fire method can potentially be invoked) by actions upon this
     * object's database by the specified <code>User</code>. <p>
     *
     * @param user the <code>User</code> for which to retrieve the
     *      <code>Enumeration</code>
     * @throws HsqlException if a database access error occurs
     * @return an <code>Enumeration</code> object describing the
     *        fully qualified names of all Java <code>Class</code>
     *        objects that are both trigger body implementations
     *        and that are accessible (whose fire method can
     *        potentially be invoked) by actions upon this object's database
     *        by the specified <code>User</code>.
     */
    Iterator enumAccessibleTriggerClassNames(User user) throws HsqlException {

        Table           table;
        Class           clazz;
        HashSet         classSet;
        TriggerDef      triggerDef;
        HsqlArrayList[] triggerLists;
        HsqlArrayList   triggerList;
        HsqlArrayList   tableList;
        int             listSize;

        classSet  = new HashSet();
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

        return classSet.iterator();
    }

    /**
     * Retrieves an <code>Enumeration</code> object describing the distinct
     * Java <code>Method</code> objects that are both the entry points
     * to trigger body implementations and that are accessible (can potentially
     * be fired) within the execution context of User currently
     * represented by the specified session. <p>
     *
     * The elements of the Enumeration have the same format as those for
     * {@link #enumRoutineMethods}, except that position [1] of each
     * Object[] element is always null (there are no aliases for trigger bodies)
     * and position [2] is always "TRIGGER". <p>
     * @return an <code>Enumeration</code> object describing the Java
     *      <code>Method</code> objects that are both the entry points
     *      to trigger body implementations and that are accessible (can
     *      potentially be fired) within the execution context of User
     *      currently represented by the specified session.
     * @param session The context in which to produce the enumeration
     * @throws HsqlException if a database access error occurs.
     */
    Iterator enumAccessibleTriggerMethods(Session session)
    throws HsqlException {

        Table           table;
        Class           clazz;
        String          className;
        Method          method;
        HsqlArrayList   methodList;
        HashSet         dupCheck;
        Class[]         pTypes;
        TriggerDef      triggerDef;
        HsqlArrayList[] triggerLists;
        HsqlArrayList   triggerList;
        HsqlArrayList   tableList;
        int             listSize;

        pTypes     = new Class[] {
            Integer.TYPE,      // trigger type
            String.class,      // trigger name
            String.class,      // table name
            Object[].class,    // old row
            Object[].class     // new row
        };
        methodList = new HsqlArrayList();
        tableList  = database.getTables();
        dupCheck   = new HashSet();

        for (int i = 0; i < tableList.size(); i++) {
            table = (Table) tableList.get(i);

            if (!session.isAccessible(table.getName())) {
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

                        clazz     = triggerDef.trig.getClass();
                        className = clazz.getName();

                        if (dupCheck.contains(className)) {
                            continue;
                        } else {
                            dupCheck.add(className);
                        }

                        method = clazz.getMethod("fire", pTypes);

                        methodList.add(new Object[] {
                            method, null, "TRIGGER"
                        });
                    } catch (Exception e) {

                        //e.printStackTrace();
                    }
                }
            }
        }

        return methodList.iterator();
    }

    /**
     * Retrieves a composite enumeration consisting of the elements from
     * {@link #enumRoutineMethods} for each Class granted to the
     * specified session and {@link #enumAccessibleTriggerMethods} for
     * the specified session. <p>
     *
     * @return a composite enumeration consisting of the elements from
     *      {@link #enumRoutineMethods} and
     *      {@link #enumAccessibleTriggerMethods}
     * @param session The context in which to produce the enumeration
     * @param andAliases true if the alias lists for the "ROUTINE" type method
     *      elements are to be generated.
     * @throws HsqlException if a database access error occurs
     */
    Iterator enumAllAccessibleMethods(Session session,
                                      boolean andAliases)
                                      throws HsqlException {

        Iterator out;
        Iterator classNames;
        Iterator methods;
        String   className;

        out        = new WrapperIterator();
        classNames = session.getGrantedClassNames(true).iterator();

        while (classNames.hasNext()) {
            className = (String) classNames.next();
            methods   = enumRoutineMethods(className, andAliases);
            out       = new WrapperIterator(out, methods);
        }

        return new WrapperIterator(out,
                                   enumAccessibleTriggerMethods(session));
    }

    /**
     * Retrieves the set of distinct, visible sessions connected to this
     * object's database, as a list. <p>
     *
     * @param session The context in which to produce the list
     * @return the set of distinct, visible sessions connected
     *        to this object's database, as a list.
     */
    HsqlArrayList listVisibleSessions(Session session) {
        return database.sessionManager.listVisibleSessions(session);
    }

    /**
     * Retrieves whether this object is reporting catalog qualifiers.
     * @return true if this object is reporting catalog qualifiers, else false.
     */
    boolean isReportCatalogs() {
        return database.getProperties().isPropertyTrue("hsqldb.catalogs");
    }

    /**
     * Retrieves whether this object is reporting schema qualifiers.
     * @return true if this object is reporting schema qualifiers, else false.
     */
    boolean isReportSchemas() {
        return database.getProperties().isPropertyTrue("hsqldb.schemas");
    }
}
