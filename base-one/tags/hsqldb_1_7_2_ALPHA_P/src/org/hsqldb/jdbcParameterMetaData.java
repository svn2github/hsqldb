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

import java.sql.ParameterMetaData;
import java.sql.SQLException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Method;

// boucherb@users 200307?? - patch 1.7.2
// TODO:
// Engine side complementary work corresponding to the TODO items
// listed below

/**
 * An object that can be used to get information about the types and
 * properties of the parameters in a PreparedStatement object.
 *
 * @author boucherb@users.sourceforge.net
 * @version 1.7.2
 * @since JDK 1.4, HSQLDB 1.7.2
 */
public class jdbcParameterMetaData implements ParameterMetaData {

    /** Helper used to translate numeric data type codes to attributes. */
    DITypeInfo ti = new DITypeInfo();

    /** The numeric data type codes of the parameters. */
    int[] types;

    /** Parameter mode values */
    int[] modes;

    /** whether param is assigned directly to identity column */
    boolean[] isIdentity;

    /** nullability code for site to which param is bound */
    int[] nullability;

    /**
     * The fully-qualified name of the Java class whose instances should
     * be passed to the method PreparedStatement.setObject. <p>
     *
     * Note that changes to Function.java and Types.java allow passing
     * objects of any class implementing java.io.Serializable and that,
     * as such, the parameter expression resolution mechanism has been
     * upgraded to provide the precise FQN for SQL function and stored
     * procedure arguments, rather than the more generic
     * org.hsqldb.JavaObject class that is used internally to represent
     * and transport objects whose class is not in the standard mapping.
     */
    String[] classNames;
    int      parameterCount;

    /**
     * Creates a new instance of jdbcParameterMetaData
     * @param types The numeric data type codes of the parameters
     */
    jdbcParameterMetaData(Result r) throws SQLException {

        String                msg;
        Result.ResultMetaData rmd;

        if (r == null) {
            msg = "r is null";

            throw jdbcDriver.sqlException(Trace.INVALID_JDBC_ARGUMENT, msg);
        }

        rmd            = r.metaData;
        types          = rmd.colType;
        parameterCount = types.length;
        nullability    = rmd.nullability;
        isIdentity     = rmd.isIdentity;
        classNames     = rmd.sClassName;
        modes          = rmd.paramMode;
    }

    /**
     * Checks if the param argument indicates a valid parameter position.
     *
     * @param param position to check
     * @throws SQLException if the param argument does not indicate a
     *      valid parameter position
     */
    void checkRange(int param) throws SQLException {

        if (param < 1 || param > parameterCount) {
            String msg = param + " is out of range";

            throw jdbcDriver.sqlException(Trace.INVALID_JDBC_ARGUMENT, msg);
        }
    }

    /**
     * Retrieves the fully-qualified name of the Java class whose instances
     * should be passed to the method PreparedStatement.setObject
     *
     * @param param the first parameter is 1, the second is 2, ...
     * @throws SQLException if a database access error occurs
     * @return the fully-qualified name of the class in the
     *        Java programming language that would be
     *        used by the method PreparedStatement.setObject
     *        to set the value in the specified parameter.
     *        This is the class name used for custom mapping.
     * @since JDK 1.4, HSQLDB 1.7.2
     */
    public String getParameterClassName(int param) throws SQLException {

        checkRange(param);

        return classNames[--param];
    }

    /**
     * Retrieves the number of parameters in the PreparedStatement object for
     * which this ParameterMetaData object contains information.
     *
     * @throws SQLException if a database access error occurs
     * @return the number of parameters
     * @since JDK 1.4, HSQLDB 1.7.2
     */
    public int getParameterCount() throws SQLException {
        return parameterCount;
    }

    /**
     * Retrieves the designated parameter's mode.
     * @param param the first parameter is 1, the second is 2, ...
     * @throws SQLException if a database access error occurs
     * @return mode of the parameter; one of
     *        ParameterMetaData.parameterModeIn,
     *        ParameterMetaData.parameterModeOut,
     *        ParameterMetaData.parameterModeInOut,
     *        ParameterMetaData.parameterModeUnknown
     * @since JDK 1.4, HSQLDB 1.7.2
     */
    public int getParameterMode(int param) throws SQLException {

        checkRange(param);

        return modes[--param];
    }

    /**
     * Retrieves the designated parameter's SQL type.
     * @param param the first parameter is 1, the second is 2, ...
     * @throws SQLException if a database access error occurs
     * @return SQL type from java.sql.Types
     * @since JDK 1.4, HSQLDB 1.7.2
     * @see java.sql.Types
     */
    public int getParameterType(int param) throws SQLException {

        int t;

        checkRange(param);

        t = types[--param];

        return t == Types.VARCHAR_IGNORECASE ? Types.VARCHAR
                                             : t;
    }

    /**
     * Retrieves the designated parameter's database-specific type name.
     * @param param the first parameter is 1, the second is 2, ...
     * @throws SQLException if a database access error occurs
     * @return type the name used by the database.
     *        If the parameter type is a user-defined
     *        type, then a fully-qualified type name is
     *        returned.
     * @since JDK 1.4, HSQLDB 1.7.2
     */
    public String getParameterTypeName(int param) throws SQLException {

        int t;
        int ts;

        checkRange(param);
        setupTypeInfo(--param);

        return ti.getTypeName();
    }

    /**
     * Retrieves the designated parameter's number of decimal digits.
     * @param param the first parameter is 1, the second is 2, ...
     * @throws SQLException if a database access error occurs
     * @return precision
     * @since JDK 1.4, HSQLDB 1.7.2
     */
    public int getPrecision(int param) throws SQLException {

        checkRange(param);
        setupTypeInfo(--param);

        Integer p = ti.getPrecision();

        // TODO:
        // parameters assigned directly to table columns
        // should report the precision of the column if it is
        // defined, otherwise the default (intrinsic) precision
        // of the undecorated type
        return p == null ? 0
                         : p.intValue();
    }

    /**
     * Retrieves the designated parameter's number of digits to right of
     * the decimal point.
     * @param param the first parameter is 1, the second is 2, ...
     * @throws SQLException if a database access error occurs
     * @return scale
     * @since JDK 1.4, HSQLDB 1.7.2
     */
    public int getScale(int param) throws SQLException {

        checkRange(param);

        // TODO:
        // parameters assigned directly to DECIMAL/NUMERIC columns
        // should report the declared scale of the column
        // For now, to be taken as "default or unknown"
        return 0;
    }

    /**
     * Retrieves whether null values are allowed in the designated parameter.
     * @param param the first parameter is 1, the second is 2, ...
     * @throws SQLException if a database access error occurs
     * @return the nullability status of the given parameter; one of
     *        ParameterMetaData.parameterNoNulls,
     *        ParameterMetaData.parameterNullable or
     *        ParameterMetaData.parameterNullableUnknown
     * @since JDK 1.4, HSQLDB 1.7.2
     */
    public int isNullable(int param) throws SQLException {

        checkRange(param);

        return nullability[--param];
    }

    /**
     * Retrieves whether values for the designated parameter can be
     * signed numbers.
     *
     * @param param the first parameter is 1, the second is 2, ...
     * @throws SQLException if a database access error occurs
     * @return true if so; false otherwise
     * @since JDK 1.4, HSQLDB 1.7.2
     */
    public boolean isSigned(int param) throws SQLException {

        checkRange(param);
        setupTypeInfo(--param);

        Boolean b = ti.isUnsignedAttribute();

        return b != null &&!b.booleanValue() &&!isIdentity[param];
    }

    private void setupTypeInfo(int param) {

        int t;
        int ts;

        t = types[param];

        if (t == Types.VARCHAR_IGNORECASE) {
            t  = Types.VARCHAR;
            ts = Types.TYPE_SUB_IGNORECASE;
        } else if (isIdentity[param]) {
            ts = Types.TYPE_SUB_IDENTITY;
        } else {
            ts = Types.TYPE_SUB_DEFAULT;
        }

        ti.setTypeCode(t);
        ti.setTypeSub(ts);
    }

    public String toString() {

        try {
            return toStringImpl();
        } catch (Throwable t) {
            return super.toString() + "[toStringImpl_exception=" + t + "]";
        }
    }

    private String toStringImpl() throws Exception {

        StringBuffer sb;
        Method[]     methods;
        Method       method;
        int          count;

        sb = new StringBuffer();

        sb.append(super.toString());

        count = getParameterCount();

        if (count == 0) {
            sb.append("[parameterCount=0]");

            return sb.toString();
        }

        methods = getClass().getDeclaredMethods();

        sb.append('[');

        int len = methods.length;

        for (int i = 0; i < count; i++) {
            sb.append('\n');
            sb.append("    parameter_");
            sb.append(i + 1);
            sb.append('=');
            sb.append('[');

            for (int j = 0; j < len; j++) {
                method = methods[j];

                if (!Modifier.isPublic(method.getModifiers())) {
                    continue;
                }

                if (method.getParameterTypes().length != 1) {
                    continue;
                }

                sb.append(method.getName());
                sb.append('=');
                sb.append(method.invoke(this,
                                        new Object[]{ new Integer(i + 1) }));

                if (j + 1 < len) {
                    sb.append(',');
                    sb.append(' ');
                }
            }

            sb.append(']');

            if (i + 1 < count) {
                sb.append(',');
                sb.append(' ');
            }
        }

        sb.append('\n');
        sb.append(']');

        return sb.toString();
    }
}
