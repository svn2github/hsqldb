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

import java.sql.*;
import java.util.*;
import java.lang.reflect.*;

/**
 * Function class declaration
 *
 *
 * @version 1.0.0.1
 */
class Function {

    private Channel    cChannel;
    private String     sFunction;
    private Method     mMethod;
    private int        iReturnType;
    private int        iArgCount;
    private int        iArgType[];
    private boolean    bArgNullable[];
    private Object     oArg[];
    private Expression eArg[];
    private boolean    bConnection;

    /**
     * Constructor declaration
     *
     *
     * @param function
     * @param channel
     */
    Function(String function, Channel channel) throws SQLException {

        cChannel  = channel;
        sFunction = function;

        int i = function.lastIndexOf('.');

        Trace.check(i != -1, Trace.UNEXPECTED_TOKEN, function);

        String classname = function.substring(0, i);

        channel.check("CLASS \"" + classname + "\"", Access.ALL);

        String methodname    = function.substring(i + 1);
        Class  classinstance = null;

        try {
            classinstance = Class.forName(classname);
        } catch (Exception e) {
            throw Trace.error(Trace.ERROR_IN_FUNCTION, classname + " " + e);
        }

        Method method[] = classinstance.getMethods();

        for (i = 0; i < method.length; i++) {
            Method m = method[i];

            if (m.getName().equals(methodname)) {
                Trace.check(mMethod == null, Trace.UNKNOWN_FUNCTION,
                            methodname);

                mMethod = m;
            }
        }

        Trace.check(mMethod != null, Trace.UNKNOWN_FUNCTION, methodname);

        Class returnclass = mMethod.getReturnType();

        iReturnType = Column.getTypeNr(returnclass.getName());

        Class arg[] = mMethod.getParameterTypes();

        iArgCount    = arg.length;
        iArgType     = new int[iArgCount];
        bArgNullable = new boolean[iArgCount];

        for (i = 0; i < arg.length; i++) {
            Class  a    = arg[i];
            String type = a.getName();

            if (i == 0 && type.equals("java.sql.Connection")) {

                // only the first parameter can be a Connection
                bConnection = true;
            } else {
                if (type.equals("[B")) {
                    type = "byte[]";
                }

                iArgType[i]     = Column.getTypeNr(type);
                bArgNullable[i] = !a.isPrimitive();
            }
        }

        eArg = new Expression[iArgCount];
        oArg = new Object[iArgCount];
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @throws SQLException
     */
    Object getValue() throws SQLException {

        int i = 0;

        if (bConnection) {
            oArg[i] = new jdbcConnection(cChannel);

            i++;
        }

        for (; i < iArgCount; i++) {
            Expression e = eArg[i];
            Object     o = null;

            if (e != null) {

                // no argument: null
                o = e.getValue(iArgType[i]);
            }

            if (o == null &&!bArgNullable[i]) {

                // null argument for primitive datatype: don't call & return null
                return null;
            }

            oArg[i] = o;
        }

        try {
            return mMethod.invoke(null, oArg);
        } catch (Exception e) {
            String s = sFunction + ": " + e.toString();

            throw Trace.getError(Trace.FUNCTION_NOT_SUPPORTED, s);
        }
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    int getArgCount() {
        return iArgCount - (bConnection ? 1
                                        : 0);
    }

    /**
     * Method declaration
     *
     *
     * @param f
     *
     * @throws SQLException
     */
    void resolve(TableFilter f) throws SQLException {

        for (int i = 0; i < iArgCount; i++) {
            if (eArg[i] != null) {
                eArg[i].resolve(f);
            }
        }
    }

    /**
     * Method declaration
     *
     *
     * @throws SQLException
     */
    void checkResolved() throws SQLException {

        for (int i = 0; i < iArgCount; i++) {
            if (eArg[i] != null) {
                eArg[i].checkResolved();
            }
        }
    }

    /**
     * Method declaration
     *
     *
     * @param i
     *
     * @return
     */
    int getArgType(int i) {
        return iArgType[i];
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    int getReturnType() {
        return iReturnType;
    }

    /**
     * Method declaration
     *
     *
     * @param i
     * @param e
     */
    void setArgument(int i, Expression e) {

        if (bConnection) {
            i++;
        }

        eArg[i] = e;
    }
}
