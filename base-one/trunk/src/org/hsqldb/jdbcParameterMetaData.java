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

// TEMPORARY - will change to accommodate listed TODO items

    /** Helper used to translate numeric data type codes to attributes. */
    DITypeInfo ti = new DITypeInfo();

    /** The numeric data type codes of the parameters. */
    int[] types;

    /**
     * Creates a new instance of jdbcParameterMetaData
     * @param types The numeric data type codes of the parameters
     */
    jdbcParameterMetaData(int[] types) {
        this.types = types;
    }

// -- END TEMPORARY

    /**
     * Checks if the param argument indicates a valid parameter position.
     *
     * @param param position to check
     * @throws SQLException if the param argument does not indicate a
     *      valid parameter position
     */
    void checkRange(int param) throws SQLException {

        if (param < 1 || param > types.length) {
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
        ti.setTypeCode(types[--param]);

        return ti.getColStClsName();
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
        return types.length;
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

        // we only support IN parameters at this point.
        return parameterModeIn;
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

        checkRange(param);

        return types[--param];
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

        checkRange(param);
        ti.setTypeCode(types[--param]);

        // TODO:
        // parameters assigned directly to table columns
        // should report the type declared (eg INTEGER IDENTITY,
        // VARCHAR_IGNORECASE), otherwise the generic type name of the
        // undecorated type
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
        ti.setTypeCode(types[--param]);

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
        // should report the scale of the column
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

        // TODO:
        //
        // Parameters assigned directly to not null table columns or to
        // Java primitive method arguments in SQL function/sp calls
        // should be reported no nulls.
        //
        // Parameters assigned directly
        // to nullable table columns should be reported nullable.
        //
        // Parameters assigned directly to Java Object method arguments
        // in SQL function / sp calls should be reported nullable unknown.
        //
        // Parameters not directly assigned to table columns or Java method
        // arguments should be reported nullable.
        return parameterNullableUnknown;
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

        // CHECKME: interpretation.
        //
        // Yet another great and totally clear JDBC spec point ;-)
        //
        // Does this mean whether the parameter's value will
        // eventually be used as a signed number, or does this mean the
        // parameter can accept a signed number input?  There is a big difference.
        //
        // For Example:  we can convert a signed number input to just about
        // any SQL type, so if the parameter is used as, say, the insert value
        // to a VARCHAR column, then we can certainly _accept_ a signed number.
        //
        // OTOH, if the parameter value is to be assigned to a column (table or proceedure)
        // whose type is some number type, we cannot always make the conversion if a setXXX
        // is used where XXX is not some number type (indeed, there is also a
        // truncation issue to consider).
        //
        // Further, an otherwise guaranteed signed number assignment will fail
        // if the value is to be used for an identity table column and the value
        // is negative.
        //
        // Proposed truth table, displaying values for a number of assumed
        // interpretations:
        //
        // ******************************************************************************************
        // * Interpretation   | setXXX type      | Param Type  | Answer                             *
        // ******************************************************************************************
        // * accepts sn       | number           | number      | narrow && (!identity || sign >= 0) *
        // ******************************************************************************************
        // * accepts sn       | number           | not number  | true                               *
        // ******************************************************************************************
        // *                  |                  |             | convert (incl. narrow) &&          *
        // * accepts sn       | not number       | number      | (!identity || sign(convert) >= 0)  *
        // ******************************************************************************************
        // * accepts sn       | not number       | not number  | true                               *
        // ******************************************************************************************
        // * will use as sn   | number           | number      | narrow && (!identity || sign >= 0) *
        // ******************************************************************************************
        // * will use as sn   | number           | not number  | false                              *
        // ******************************************************************************************
        // *                  |                  |             | convert (incl. narrow) &&          *
        // * will use as sn   | not number       | number      | (!identity || sign(convert) >= 0)  *
        // ******************************************************************************************
        // * will use as sn   | not number       | not number  | false                              *
        // ******************************************************************************************
        //
        // Since we cannot predict apriori the convertability to number under setXXX where XXX is
        // not a number type (or indeed, the convertability, where the conversion is a narrowing
        // from one number type to another, the most accurate conclusions possible under the two
        // assumptions are:
        //
        // Given interpretation "accepts sn", we should always report true (maybe), as it is impossible
        // to refuse signed numbers without first knowing their value...one must at least
        // wait to test for non-negativity under assignment to identity column.  The same
        // applies to all other types, with the addition of conversion and possibly narrowing.
        //
        // Given interpretation "will be used as sn", we should return true (maybe) whenever param target type
        // is number and it's value will not be used to assign directly to an identity column.
        //
        // The current implementation reflects the "will be used as sn" assumption (my opinion
        // is that it conveys at least some information, which is far better than the alternate),
        // but does not yet include the identity column assigment test, as this depends on work
        // remaining to be done in Expression.resolve and corresponding work in Result to transmit
        // this data without calls back to the database.
        //
        // TODO:
        // parameter values assigned directly to identity column cannot
        // be signed
        checkRange(param);
        ti.setTypeCode(types[--param]);

        Boolean b = ti.isUnsignedAttribute();

        return b != null &&!b.booleanValue();
    }
}
