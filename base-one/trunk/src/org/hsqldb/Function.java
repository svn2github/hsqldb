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
import java.util.Hashtable;
import java.util.Date;
import java.util.Calendar;
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

    private Session          cSession;
    private String           sFunction;
    private Method           mMethod;
    private int              iReturnType;
    private int              iArgCount;
    private int              iArgType[];
    private boolean          bArgNullable[];
    private Object           oArg[];
    private Expression       eArg[];
    private boolean          bConnection;
    private static Hashtable methodCache = new Hashtable();
    private int              fID;
    private String           fname;

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
     * specified class or construction cannot procede and a HsqlException is
     * thrown. <p>
     *
     * The Session paramter is the connected context in which this
     * Function object will evaluate.  If it is determined that the
     * connected user does not have the right to evaluate this Function,
     * construction cannot proceed and a HsqlException is thrown.
     *
     *
     * @param function the fully qualified name of a Java method
     * @param session the connected context in which this Function object will
     *                evaluate
     * @throws HsqlException if the specified function FQN corresponds to no
     *                      Java method or the session user at the time of
     *                      construction does not have the right to evaluate
     *                      this Function.
     */
    Function(String function, Session session) throws HsqlException {

        cSession  = session;
        sFunction = function;
        fname     = function;
        fID       = Library.functionID(function);

        int i = function.lastIndexOf('.');

        Trace.check(i != -1, Trace.UNEXPECTED_TOKEN, function);

        String classname = function.substring(0, i);

        session.check(classname, UserManager.ALL);

        mMethod = (Method) methodCache.get(function);

        if (mMethod == null) {
            String methodname    = function.substring(i + 1);
            Class  classinstance = null;

            try {
                classinstance = Class.forName(classname);
            } catch (Exception e) {
                throw Trace.error(Trace.ERROR_IN_FUNCTION,
                                  Trace.Function_Function, new Object[] {
                    classname, e
                });
            }

            Method method[] = classinstance.getMethods();

            for (i = 0; i < method.length; i++) {
                Method m = method[i];

                if (m.getName().equals(methodname)
                        && Modifier.isStatic(m.getModifiers())) {
                    mMethod = m;

                    break;
                }
            }

            Trace.check(mMethod != null, Trace.UNKNOWN_FUNCTION, methodname);
            methodCache.put(function, mMethod);
        }

        Class returnclass = mMethod.getReturnType();

        iReturnType = Types.getTypeNr(returnclass.getName());

        Class arg[] = mMethod.getParameterTypes();

        iArgCount    = arg.length;
        iArgType     = new int[iArgCount];
        bArgNullable = new boolean[iArgCount];

        for (i = 0; i < arg.length; i++) {
            Class  a    = arg[i];
            String type = a.getName();

            if ((i == 0) && type.equals("java.sql.Connection")) {

                // only the first parameter can be a Connection
                bConnection = true;
            } else {

// fredt@users - byte[] is now supported directly as "[B"
//                if (type.equals("[B")) {
//                    type = "byte[]";
//                }
                iArgType[i]     = Types.getTypeNr(type);
                bArgNullable[i] = !a.isPrimitive();
            }
        }

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

        int i = 0;

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

            if (ret instanceof byte[] || ret instanceof Object) {
                ret = Column.convertObject(ret, iReturnType);
            }

            return ret;

// boucherb@users - patch 1.7.2 - better function invocation error reporting
        } catch (Throwable t) {
            String s = sFunction;

            if (t instanceof InvocationTargetException) {
                while (t instanceof InvocationTargetException) {
                    t = ((InvocationTargetException) t).getTargetException();
                    s += ": " + t.toString();
                }

                s += ": " + t.toString();

                throw Trace.getError(Trace.UNKNOWN_FUNCTION, s);
            } else {
                s = sFunction + ": " + t.toString();

                throw Trace.getError(Trace.GENERAL_ERROR, s);
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
     *
     * @return the number of arguments this Function takes, as known to the
     * calling SQL context
     */
    int getArgCount() {
        return iArgCount - (bConnection ? 1
                                        : 0);
    }

    /**
     * Resolves the arguments supplied to this Function object against the
     * specified TableFilter.
     *
     *
     * @param f the TableFilter against which to resolve this Function
     * object's arguments
     * @throws HsqlException if there is a problem resolving an argument
     * against the specified TableFilter
     */
    void resolve(TableFilter f) throws HsqlException {

        int i;

        i = bConnection ? 1
                        : 0;

        for (; i < iArgCount; i++) {
            if (eArg[i] != null) {
                if (eArg[i].isParam()) {
                    eArg[i].setDataType(iArgType[i]);
                } else {
                    eArg[i].resolve(f);
                }
            }
        }
    }

    /**
     * Checks each of this object's arguments for resolution, throwing a
     * HsqlException if any arguments have not yet been resolved.
     *
     *
     * @throws HsqlException if any arguments have not yet been resolved
     */
    void checkResolved() throws HsqlException {

        for (int i = 0; i < iArgCount; i++) {
            if (eArg[i] != null) {
                eArg[i].checkResolved();
            }
        }
    }

    /**
     * Retrieves the java.sql.Types type of the argument at the specified
     * offset in this Function object's paramter list
     *
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
     * object's return type
     *
     *
     * @return this Function object's java.sql.Types return type
     */
    int getReturnType() {
        return iReturnType;
    }

    /**
     * Binds the specified expression to the specified argument in this
     * Function object's paramter list.
     *
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

    public String toString() {

        StringBuffer sb = new StringBuffer();

        sb.append(super.toString()).append("=[\n");
        sb.append(sFunction).append("(");

        for (int i = 0; i < eArg.length; i++) {
            sb.append("[").append(eArg[i]).append("]");
        }

        sb.append(")\n");
        sb.append("returns ").append(mMethod.getReturnType());
        sb.append("]\n");

        return sb.toString();
    }
}
