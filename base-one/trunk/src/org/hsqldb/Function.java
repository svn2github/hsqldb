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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Calendar;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.HashMap;
import org.hsqldb.lib.StringConverter;
import org.hsqldb.store.ValuePool;

// fredt@users 20020912 - patch 1.7.1 - shortcut treatment of identity() call
// fredt@users 20020912 - patch 1.7.1 - cache java.lang.reflect.Method objects
// fredt@users 20021013 - patch 1.7.1 - ignore non-static methods
// boucherb@users 20030201 - patch 1.7.2 - direct calls for org.hsqldb.Library
// fredt@users 20030621 - patch 1.7.2 - shortcut treatment of session calls

/**
 * Provides services to evaluate and invoke Java methods in the context of
 * SQL function and stored procedure calls.
 *
 * @version 1.7.0
 */
class Function {

    private Session        cSession;
    private String         sFunction;
    private Method         mMethod;
    private Class          cReturnClass;
    private Class[]        aArgClasses;
    private int            iReturnType;
    private int            iArgCount;
    private int            iSqlArgCount;
    private int            iSqlArgStart;
    private int            iArgType[];
    private boolean        bArgNullable[];
    private Object         oArg[];
    private Expression     eArg[];
    private boolean        bConnection;
    private static HashMap methodCache = new HashMap();
    private int            fID;
    String                 name;    // name used to call function

    /**
     * Constructs a new Function object with the given function call name
     * and using the specified Session context. <p>
     *
     * The call name is the fully qualified name of a static Java method, in
     * the form "package.class.method."  This implies that Java
     * methods with the same fully qualified name but different signatures
     * cannot be used properly as HSQLDB SQL functions or stored procedures.
     * For instance, it is impossible to call both System.getProperty(String)
     * and System.getProperty(String,String) under this arrangement, because
     * the HSQLDB Function object is unable to differentiate between the two;
     * it simply chooses the first method matching the FQN in the array of
     * methods obtained from calling getMethods() on an instance of the
     * Class indicated in the FQN, hiding all other methods with the same
     * FQN. <p>
     *
     * The function FQN must match at least one static Java method FQN in the
     * specified class or construction cannot procede and an HsqlException is
     * thrown. <p>
     *
     * The Session paramter is the connected context in which this
     * Function object will evaluate.  if checkPrivs is true and it is
     * determined that the connected user does not have the right to evaluate
     * this Function, construction cannot proceed and a HsqlException is
     * thrown. If checkPrivs is false, the construction proceeds without
     * a privilege check.  This behaviour is in support of VIEW resolution,
     * wherein tables and routines may be involved to which the session
     * user has been granted no privileges but should be allowed to
     * select from the VIEW regardless, by virtue of being granted SELECT
     * on the VIEW object.
     *
     * @param fqn the fully qualified name of a Java method
     * @param session the connected context in which this Function object will
     *      evaluate
     * @param checkPrivs if true, the session user's routine invocation
     *      privileges are checked against the declaring class of the fqn
     * @throws HsqlException if the specified function FQN corresponds to no
     *      Java method or the session user at the time of
     *      construction does not have the right to evaluate
     *      this Function.
     */
    Function(String name, String fqn, Session session) throws HsqlException {

        this.name = name;
        cSession  = session;
        sFunction = fqn;
        fID       = Library.functionID(fqn);

        int i = fqn.lastIndexOf('.');

        Trace.check(i != -1, Trace.UNEXPECTED_TOKEN, fqn);

        String classname = fqn.substring(0, i);

        mMethod = (Method) methodCache.get(fqn);

        if (mMethod == null) {
            String methodname    = fqn.substring(i + 1);
            Class  classinstance = null;

            try {
                classinstance = Class.forName(classname);
            } catch (Exception e) {
                throw Trace.error(Trace.ERROR_IN_FUNCTION,
                                  Trace.Function_Function, new Object[] {
                    classname, e
                });
            }

            Method methods[] = classinstance.getMethods();

            for (i = 0; i < methods.length; i++) {
                Method m = methods[i];

                if (m.getName().equals(methodname)
                        && Modifier.isStatic(m.getModifiers())) {
                    mMethod = m;

                    break;
                }
            }

            Trace.check(mMethod != null, Trace.UNKNOWN_FUNCTION, methodname);
            methodCache.put(fqn, mMethod);
        }

        cReturnClass = mMethod.getReturnType();

        if (cReturnClass.equals(org.hsqldb.Result.class)
                || cReturnClass.equals(org.hsqldb.jdbcResultSet.class)) {

            // For now, people can write stored procedures with
            // descriptor having above return types to indicate
            // result of arbitrary arity.  Later, this must be
            // replaced with a better system.
            iReturnType = Types.OTHER;
        } else {

            // Now we can return an object of any Class,
            // as long as it's a primitive array, directly
            // implements java.io.Serializable directly or is a
            // non-primitive array whose base component implements
            // java.io.Serializable
            iReturnType = Types.getParameterTypeNr(cReturnClass);
        }

        aArgClasses  = mMethod.getParameterTypes();
        iArgCount    = aArgClasses.length;
        iArgType     = new int[iArgCount];
        bArgNullable = new boolean[iArgCount];

        for (i = 0; i < aArgClasses.length; i++) {
            Class  a    = aArgClasses[i];
            String type = a.getName();

            if ((i == 0) && a.equals(java.sql.Connection.class)) {

                // TODO: make this obsolete, providing
                // jdbc:default:connection url functionality
                // instead
                // only the first parameter can be a Connection
                bConnection = true;
            } else {

                // Now we can pass values of any Class to args of any
                // Class, as long as they are primitive arrays, directly
                // implement java.io.Serializable or are non-primitive
                // arrays whose base component implements java.io.Serializable
                iArgType[i]     = Types.getParameterTypeNr(a);
                bArgNullable[i] = !a.isPrimitive();
            }
        }

        iSqlArgCount = iArgCount;

        if (bConnection) {
            iSqlArgCount--;

            iSqlArgStart = 1;
        } else {
            iSqlArgStart = 0;
        }
        ;

        eArg = new Expression[iArgCount];
        oArg = new Object[iArgCount];
    }

    /**
     * Retrieves the value this Function evaluates to, given the current
     * state of this object's {@link #resolve(TableFilter) resolved}
     * TableFilter, if any, and any mapping of expressions to this
     * Function's parameter list that has been performed via
     * {link #setArgument(int,Expression) setArgument}.
     *
     *
     * @return the value resulting from evaluating this Function
     * @throws HsqlException if an invocation exception is encountered when
     * calling the Java
     * method underlying this object
     */
    Object getValue() throws HsqlException {

        switch (fID) {

            case Library.identity :
                return cSession.getLastIdentity();

            case Library.database :
                return cSession.getDatabase().getPath();

            case Library.user :
                return cSession.getUser().getName();

            case Library.isReadOnlyConnection :
                return cSession.isReadOnly() ? Boolean.TRUE
                                             : Boolean.FALSE;

            case Library.getAutoCommit :
                return cSession.isAutoCommit() ? Boolean.TRUE
                                               : Boolean.FALSE;

            case Library.isReadOnlyDatabase :
                return cSession.getDatabase().databaseReadOnly ? Boolean.TRUE
                                                               : Boolean
                                                               .FALSE;

            case Library.isReadOnlyDatabaseFiles :
                return cSession.getDatabase().filesReadOnly ? Boolean.TRUE
                                                            : Boolean.FALSE;
        }

        int i = 0;

        if (bConnection) {
            oArg[i] = cSession.getInternalConnection();

            i++;
        }

        for (; i < iArgCount; i++) {
            Expression e = eArg[i];
            Object     o = null;

            if (e != null) {

                // no argument: null
                o = e.getValue(iArgType[i]);
            }

            if ((o == null) &&!bArgNullable[i]) {

                // null argument for primitive datatype: don't call
                return null;
            }

            if (o instanceof JavaObject) {
                o = ((JavaObject) o).getObject();
            } else if (o instanceof Binary) {
                o = ((Binary) o).getBytes();
            }

            oArg[i] = o;
        }

        try {
            if (fID == Library.month) {
                return ValuePool.getInt(
                    Library.getDateTimePart((Date) oArg[0], Calendar.MONTH)
                    + cSession.getDatabase().sqlMonth);
            }

            Object ret = (fID >= 0) ? Library.invoke(fID, oArg)
                                    : mMethod.invoke(null, oArg);

            //if (ret instanceof byte[] || ret instanceof Object) {
            // it's always an instanceof Object
            //ret =
            return Column.convertObject(ret, iReturnType);

            //}
            //return ret;
// boucherb@users - patch 1.7.2 - better function invocation error reporting
        } catch (Throwable t) {
            String s = sFunction;

            if (t instanceof InvocationTargetException) {
                while (t instanceof InvocationTargetException) {
                    t = ((InvocationTargetException) t).getTargetException();
                    s += ": " + t.toString();
                }

                s += ": " + t.toString();

                throw Trace.error(Trace.UNKNOWN_FUNCTION, s);
            } else {
                s = sFunction + ": " + t.toString();

                throw Trace.error(Trace.GENERAL_ERROR, s);
            }
        }
    }

    /**
     * Retrieves the number of parameters that must be supplied to evaluate
     * this Function object from SQL.  <p>
     *
     * This value may be different than the number of parameters of the
     * underlying Java method.  This is because HSQLDB automatically detects
     * if the first parameter is of type java.sql.Connection, and supplies a
     * live Connection object constructed from the evaluating session context
     * if so.
     *
     * @return the number of arguments this Function takes, as known to the
     * calling SQL context
     */
    int getArgCount() {
        return iSqlArgCount;
    }

    /**
     * Remnoves the Table filters from Expression parameters to this Function.
     *
     * @throws HsqlException if there is a problem resolving a parameter
     * against the specified TableFilter
     */
/*
    void removeFilters() throws HsqlException {

        Expression e;

        for (int i = iSqlArgStart; i < iArgCount; i++) {
            e = eArg[i];

            if (e != null) {
                e.removeFilters();
            }
        }
    }
*/

    /**
     * Checks the Expresion parameters to this Function object against the
     * set of TableFilter.
     *
     * @param fa the array of TableFilter against which to resolve this Function
     * object's arguments
     * @throws HsqlException if there is a problem resolving a parameter
     * against the specified TableFilter
     */
    void checkTables(HsqlArrayList fa) throws HsqlException {

        Expression e;

        for (int i = iSqlArgStart; i < iArgCount; i++) {
            e = eArg[i];

            if (e != null) {
                e.checkTables(fa);
            }
        }
    }

    /**
     * Resolves the Expression parameters to this Function object against the
     * specified TableFilter.
     *
     * @param f the TableFilter against which to resolve this Function
     * object's arguments
     * @throws HsqlException if there is a problem resolving a parameter
     * against the specified TableFilter
     */
    void resolveTables(TableFilter f) throws HsqlException {

        Expression e;

        for (int i = iSqlArgStart; i < iArgCount; i++) {
            e = eArg[i];

            if (e != null) {
                e.resolveTables(f);
            }
        }
    }

    /**
     * Resolves the type of this expression and performs certain
     * transformations and optimisations of the expression tree.
     *
     * @throws HsqlException if there is a problem resolving the expression
     */
    void resolveType() throws HsqlException {

        Expression e;

        for (int i = iSqlArgStart; i < iArgCount; i++) {
            e = eArg[i];

            if (e != null) {
                if (e.isParam()) {
                    e.setDataType(iArgType[i]);

                    e.nullability    = getArgNullability(i);
                    e.valueClassName = getArgClass(i).getName();
                } else {
                    e.resolveTypes();
                }
            }
        }
    }

    /**
     * Checks each of this object's arguments for resolution, throwing a
     * HsqlException if any arguments have not yet been resolved. <p>
     *
     * @throws HsqlException if any arguments have not yet been resolved
     */
    boolean checkResolved(boolean check) throws HsqlException {

        boolean result = true;

        for (int i = iSqlArgStart; i < iArgCount; i++) {
            if (eArg[i] != null) {
                result = result && eArg[i].checkResolved(check);
            }
        }

        return result;
    }

    /**
     * Retrieves the java.sql.Types type of the argument at the specified
     * offset in this Function object's paramter list. <p>
     *
     * @param i the offset of the desired argument in this Function object's
     * paramter list
     * @return the specified argument's java.sql.Types type
     */
    int getArgType(int i) {
        return iArgType[i];
    }

    /**
     * Retrieves the java.sql.Types type of this Function
     * object's return type. <p>
     *
     * @return this Function object's java.sql.Types return type
     */
    int getReturnType() {
        return iReturnType;
    }

    /**
     * Binds the specified expression to the specified argument in this
     * Function object's paramter list. <p>
     *
     * @param i the position of the agument to bind to
     * @param e the expression to bind
     */
    void setArgument(int i, Expression e) {

        if (bConnection) {
            i++;
        }

        eArg[i] = e;
    }

    /**
     * Retrieves a DDL representation of this object. <p>
     *
     * @return
     */
    String getDLL() throws HsqlException {

        StringBuffer sb = new StringBuffer();

        // get the name as used by the CHECK statement
        String ddlName = name;

        // special case for TRIM
        if (Token.T_TRIM.equals(name)) {
            sb.append(name).append('(');

            boolean leading  = eArg[2].test();
            boolean trailing = eArg[3].test();

            if (leading && trailing) {
                sb.append(Token.T_BOTH);
            } else {
                sb.append(leading ? Token.T_LEADING
                                  : Token.T_TRAILING);
            }

            // to do change to string
            sb.append(' ');

            String charval = (String) eArg[1].getValue();

            sb.append(Column.createSQLString(charval)).append(' ');
            sb.append(Token.T_FROM).append(' ');
            sb.append(eArg[0].getDDL()).append(')');

            return sb.toString();
        }

        if (sFunction.equals(name)) {
            ddlName = StringConverter.toQuotedString(name, '"', true);
        }

        sb.append(ddlName).append('(');

        for (int i = iSqlArgStart; i < eArg.length; i++) {
            sb.append(eArg[i].getDDL());

            if (i < eArg.length - 1) {
                sb.append(',');
            }
        }

        sb.append(')');

        return sb.toString();
    }

    /**
     * Retrieves a String representation of this object. <p>
     *
     * @return a String representation of this object
     */
    public String toString() {

        StringBuffer sb = new StringBuffer();

        sb.append(super.toString()).append("=[\n");
        sb.append(sFunction).append("(");

        for (int i = iSqlArgStart; i < eArg.length; i++) {
            sb.append("[").append(eArg[i]).append("]");
        }

        sb.append(") returns ").append(Types.getTypeString(getReturnType()));
        sb.append("]\n");

        return sb.toString();
    }

    /**
     * Retrieves the Java Class of the object returned by getValue(). <p>
     *
     * @return the Java Class of the objects retrieved by calls to
     *      getValue()
     */
    Class getReturnClass() {
        return cReturnClass;
    }

    /**
     * Retreives the Java Class of the i'th argument. <p>
     *
     * @return the Java Class of the i'th argument
     */
    Class getArgClass(int i) {
        return aArgClasses[i];
    }

    /**
     * Retrieves the SQL nullability code of the i'th argument. <p>
     *
     * @return the SQL nullability code of the i'th argument
     */
    int getArgNullability(int i) {
        return bArgNullable[i] ? Expression.NULLABLE
                               : Expression.NO_NULLS;
    }
}
