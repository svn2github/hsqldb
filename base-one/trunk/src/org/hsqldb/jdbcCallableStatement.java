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

import java.sql.*;
import java.math.*;
import java.util.*;
import java.io.*;
import org.hsqldb.lib.Iterator;
import org.hsqldb.lib.IntKeyIntValueHashMap;

// boucherb@users patch 1.7.2 - CallableStatement impl removed
// from jdbcPreparedStatement and moved here; sundry changes elsewhere to
// comply
// TODO: 1.7.2 Alpha N
//       maybe implement set-by-parameter-name.  We have an informal spec,
//       being "@1" => 1, "@2" => 2, etc.  Problems: return value is "@0"
//       and there is no support for registering the return value as an out
//       parameter.
// TODO: 1.7.2 RC
//       engine and client-side mechanisms for adding, retrieving,
//       navigating (and perhaps controlling holdability of) multiple
//       single-statement-scope result sets.

/**
 * <!-- start generic documentation -->
 *
 * The interface used to execute SQL stored procedures.  The JDBC API
 * provides a stored procedure SQL escape syntax that allows stored
 * procedures to be called in a standard way for all RDBMSs. This escape
 * syntax has one form that includes a result parameter and one that does
 * not. If used, the result parameter must be registered as an OUT parameter.
 * The other parameters can be used for input, output or both. Parameters
 * are referred to sequentially, by number, with the first parameter being 1.
 * <PRE>
 *   {?= call &lt;procedure-name&gt;[&lt;arg1&gt;,&lt;arg2&gt;, ...]}
 *   {call &lt;procedure-name&gt;[&lt;arg1&gt;,&lt;arg2&gt;, ...]}
 * </PRE>
 * <P>
 * IN parameter values are set using the <code>set</code> methods inherited from
 * {@link PreparedStatement}.  The type of all OUT parameters must be
 * registered prior to executing the stored procedure; their values
 * are retrieved after execution via the <code>get</code> methods provided here.
 * <P>
 * A <code>CallableStatement</code> can return one {@link ResultSet} object or
 * multiple <code>ResultSet</code> objects.  Multiple
 * <code>ResultSet</code> objects are handled using operations
 * inherited from {@link Statement}.
 * <P>
 * For maximum portability, a call's <code>ResultSet</code> objects and
 * update counts should be processed prior to getting the values of output
 * parameters.
 * <P>
 * <!-- end generic documentation -->
 * <!-- start Release-specific documentation -->
 * <span class="ReleaseSpecificDocumentation">
 * <B>HSQLDB-Specific Information:</B> <p>
 *
 * Up to and including HSQLDB 1.7.2, support for stored procedures is
 * not provided in the conventional fashion, if there is such a thing. <p>
 *
 * Stored procedures are typically supported in ways that vary greatly
 * from one DBMS implementation to the next.  So, it is almost
 * guaranteed that the code for a stored procedure written under a
 * specific DBMS product will not work without modification in the
 * context of another vendor's product or even across a single vendor's
 * product lines.  Moving stored procedures from one DBMS product line to
 * another almost invariably involves complex porting issues and often
 * may not be possible at all.  Be warned. <p>
 *
 * Up to and including 1.7.2, HSQLDB stored procedures map directly onto the
 * methods of compiled Java classes found on the classpath of the engine.
 * This is done in a non-standard but fairly efficient way by issuing a class
 * grant (and possibly method aliases) of the form: <p>
 *
 * <PRE>
 * GRANT ALL ON CLASS "package.class" TO [user_name | PUBLIC]
 * CREATE ALIAS call_name FOR ""package.class.method" -- optional
 * </PRE>
 *
 * This has the effect of allowing the specified user(s) to access to the
 * set of uniquely named public static methods of the specified class,
 * in either the role of SQL functions or stored procedures.

 * For example: <p>
 *
 * <PRE>
 * GRANT ALL ON CLASS "java.lang.Math" TO PUBLIC;
 * CONNECT anyuser PASSWORD *****;
 * SELECT "java.lang.Math.abs"(column_1) FROM table_1;
 * CREATE ALIAS abs FOR "java.lang.Math.abs"
 * CALL 2 + abs(-5);
 * </PRE>
 *
 * Please note the use of the term "uniquely named" above.  Up to and including
 * HSQLDB 1.7.2, no support is provided to deterministically resolve overloaded
 * method names or inherited public static methods, so it is strongly
 * recommended that developers creating stored procedure libraries for HSQLDB
 * do not design them such that the SQL stored procedure call interface
 * includes: <p>
 *
 * <ol>
 * <li>inherited public static methods
 * <li>overloaded public static methods
 * </ol>
 *
 * Also, please note that no support for more advanced features is provided at
 * this time. That is, the <code>CallableStatement</code> methods for working
 * with <code>OUT</code> parameters are not yet supported because there is
 * no low level support for this yet written into the engine internals.
 *
 * So, while some systems may <I>require</I> working with <code>OUT</code>
 * parameters when calling stored procedures, this is currently never
 * the case for HSQLDB; attempting to do so will always result in
 * throwing a <code>SQLException</code> stating that the function
 * is not supported. <p>
 *
 * Although there is currently no support for <code>OUT</code>
 * parameters, a future version of the HSQLDB product <i>may</i> include
 * this feature. <p>
 *
 * Please also note that the HSQLDB stored procedure mechanism is essentially
 * a thin wrap of the HSQLDB SQL function mechanism, in combination with the
 * more general HSQLDB sql expression evaluation mechanism, allowing
 * simple SQL expressions, possibly containing Java method invocations, to
 * be evaluated outside of an <code>INSERT</code>, <code>UPDATE</code>,
 * <code>DELETE</code> or <code>SELECT</code> statement context.
 * That is, issuing a <code>CALL</code> statement returning an
 * opaque (OTHER type) or known scalar object reference (an instance of
 * a Java class automatically mapped to a supported HSQLDB data type) has
 * virtually the same effect as:
 *
 * <PRE>
 * CREATE TABLE DUAL (dummy VARCHAR);
 * INSERT INTO DUAL VALUES(NULL);
 * SELECT <simple-expression> FROM DUAL;
 * </PRE>
 *
 * The exception is the case where one wishes to allow the client to
 * materialize a custom-built result set by calling a stored procedure.
 * In this case, the stored procedure's Java method descriptor must specify a
 * return type of java.lang.Object and must return an instance of
 * either: <p>
 *
 * 1.) org.hsqldb.jdbcResultSet <p>
 *
 * - can be obtained, for instance, by executing a query against connection to
 * an HSQLDB database instance. <p>
 *
 * 2.) org.hsqldb.Result <p>
 *
 * - can be created, manipulated and returned if the stored procedure method is
 * written inside the org.hsqldb package.  This would be done, for
 * instance, to allow advanced/more efficient interaction with the database
 * internals when executing the body of the stored procedure. <p>
 *
 * Starting with 1.7.2, when HSQLDB detects that the class of the Object
 * returned by evaluating a CALL expression is either of the above, then an
 * automatic unwrapping is performed, and the arity of of the underlying result
 * is exposed to the client in the form of a ResultSet with possibly more (or
 * fewer) than one row and column. <p>
 *
 * Here is a very simple example of an HSQLDB stored procedure returning a
 * result set: <p>
 *
 * <PRE>
 * package mypackage;
 *
 * class myclass {
 *
 *      public static Object my_stored_procedure(Connection conn) throws Exception {
 *          return conn.createStatement().executeQuery("select * from names");
 *      }
 * }
 * </PRE>
 *
 * Here is a more advanced example of an HSQLDB stored procedure using the
 * internal API to create, fill and return a result: <p>
 *
 * <PRE>
 * package org.hsqldb;
 *
 * class myclass {
 *
 *      public static Object my_stored_procedure() {
 *          Result r;
 *
 *          r = Result.newSingleColumnResult("NAME", Types.VARCHAR);
 *          r.add( new Object[]{ "TOM" } );
 *          r.add( new Object[]{ "DICK" } );
 *          r.add( new Object[]{ "HARRY" } );
 *
 *          r.sortResult( new int[1] { 0 }, new int[] { 1 } );
 *
 *          return r;
 *      }
 * }
 * </PRE>
 *
 * Please note that by using the internal API to build up results, one is
 * forgoing the SQL/Java integrity checks typically enforced by the engine
 * when building results purely via execution of SQL statements.  That is,
 * for instance, given the above example, one is free to add Object[] rows
 * to the output result object whose length do not match the column count
 * and whose elements are not String objects.  Be warned that doing so may
 * very well cause either obvious or subtle data integrity problems or
 * otherwise unexpected exceptions to be thrown. <p>
 *
 * Please note also that the mechanisms described above are subject to
 * change over both point and major releases as work progresses. <p>
 *
 * (boucherb@users)
 * </span>
 * <!-- end Release-specific documentation -->
 *
 * @see jdbcConnection#prepareCall
 * @see jdbcResultSet
 */
public class jdbcCallableStatement extends jdbcPreparedStatement
implements CallableStatement {

    private String[]              parameterNames;
    private IntKeyIntValueHashMap outRegistrationMap;

    /** Creates a new instance of jdbcCallableStatement */
    public jdbcCallableStatement(jdbcConnection c, String sql,
                                 int type) throws HsqlException {

        super(c, sql, type);

        outRegistrationMap = new IntKeyIntValueHashMap();
        parameterNames     = pmdDescriptor.metaData.sName;
    }

    /**
     * Performs an internal check for column index validity. <p>
     *
     * @param column index of column to check
     * @throws SQLException when this object's parent ResultSet has
     *      no such column
     */
    private void checkIsGetParameterIndex(int i) throws SQLException {

        String msg;

        if (i < 1 || i > parameterModes.length) {
            msg = "Parameter index out of bounds: " + i;

            throw jdbcDriver.sqlException(Trace.INVALID_JDBC_ARGUMENT, msg);
        }

        int mode = parameterModes[i - 1];

        switch (mode) {

            default :
                msg = "Not OUT or IN OUT mode: " + mode + " for parameter: "
                      + i;

                throw jdbcDriver.sqlException(Trace.INVALID_JDBC_ARGUMENT,
                                              msg);
            case Expression.PARAM_IN_OUT :
            case Expression.PARAM_OUT :
                break;

            // this is OK
        }
    }

    void checkIsRegisteredParameterIndex(int parameterIndex)
    throws SQLException {

        int    type;
        String msg;

        type = outRegistrationMap.get(parameterIndex, Integer.MIN_VALUE);

        if (type == Integer.MIN_VALUE) {
            msg = "Parameter not registered: " + parameterIndex;

            throw jdbcDriver.sqlException(Trace.INVALID_JDBC_ARGUMENT, msg);
        }
    }

// ----------------------------------- JDBC 1 ----------------------------------

    /**
     * <!-- start generic documentation -->
     * Registers the OUT parameter in ordinal position
     * <code>parameterIndex</code> to the JDBC type
     * <code>sqlType</code>.  All OUT parameters must be registered
     * before a stored procedure is executed.
     * <p>
     * The JDBC type specified by <code>sqlType</code> for an OUT
     * parameter determines the Java type that must be used
     * in the <code>get</code> method to read the value of that parameter.
     * <p>
     * If the JDBC type expected to be returned to this output parameter
     * is specific to this particular database, <code>sqlType</code>
     * should be <code>java.sql.Types.OTHER</code>.  The method
     * {@link #getObject} retrieves the value. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *   and so on
     * @param sqlType the JDBC type code defined by <code>java.sql.Types</code>.
     *   If the parameter is of JDBC type <code>NUMERIC</code>
     *   or <code>DECIMAL</code>, the version of
     *   <code>registerOutParameter</code> that accepts a scale value
     *   should be used.
     * @exception SQLException if a database access error occurs
     * @see java.sql.Types
     */
    public void registerOutParameter(int parameterIndex,
                                     int sqlType) throws SQLException {

        checkIsGetParameterIndex(parameterIndex);
        outRegistrationMap.put(parameterIndex, sqlType);

        throw jdbcDriver.notSupported;
    }

    /**
     * <!-- start generic documentation -->
     * Registers the parameter in ordinal position
     * <code>parameterIndex</code> to be of JDBC type
     * <code>sqlType</code>.  This method must be called
     * before a stored procedure is executed.
     * <p>
     * The JDBC type specified by <code>sqlType</code> for an OUT
     * parameter determines the Java type that must be used
     * in the <code>get</code> method to read the value of that parameter.
     * <p>
     * This version of <code>registerOutParameter</code> should be
     * used when the parameter is of JDBC type <code>NUMERIC</code>
     * or <code>DECIMAL</code>. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @param sqlType the SQL type code defined by <code>java.sql.Types</code>.
     * @param scale the desired number of digits to the right of the
     * decimal point.  It must be greater than or equal to zero.
     * @exception SQLException if a database access error occurs
     * @see java.sql.Types
     */
    public void registerOutParameter(int parameterIndex, int sqlType,
                                     int scale) throws SQLException {
        registerOutParameter(parameterIndex, sqlType);
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves whether the last OUT parameter read had the value of
     * SQL <code>NULL</code>.  Note that this method should be called only
     * after calling a getter method; otherwise, there is no value to use in
     * determining whether it is <code>null</code> or not. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return <code>true</code> if the last parameter read was SQL
     * <code>NULL</code>; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean wasNull() throws SQLException {

        checkClosed();

        throw jdbcDriver.notSupported;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>CHAR</code>,
     * <code>VARCHAR</code>, or <code>LONGVARCHAR</code> parameter as a
     * <code>String</code> in the Java programming language.
     * <p>
     * For the fixed-length type JDBC <code>CHAR</code>,
     * the <code>String</code> object
     * returned has exactly the same value the JDBC
     * <code>CHAR</code> value had in the
     * database, including any padding added by the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @return the parameter value. If the value is SQL <code>NULL</code>,
     *    the result
     *    is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #setString
     */
    public String getString(int parameterIndex) throws SQLException {

        checkClosed();
        checkIsRegisteredParameterIndex(parameterIndex);

        throw jdbcDriver.notSupported;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>BIT</code> parameter
     * as a <code>boolean</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *  and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     *   the result is <code>false</code>.
     * @exception SQLException if a database access error occurs
     * @see #setBoolean
     */
    public boolean getBoolean(int parameterIndex) throws SQLException {

        checkClosed();
        checkIsRegisteredParameterIndex(parameterIndex);

        throw jdbcDriver.notSupported;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>TINYINT</code>
     * parameter as a <code>byte</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.2 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     * the result is <code>0</code>.
     * @exception SQLException if a database access error occurs
     * @see #setByte
     */
    public byte getByte(int parameterIndex) throws SQLException {

        checkClosed();
        checkIsRegisteredParameterIndex(parameterIndex);

        throw jdbcDriver.notSupported;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>SMALLINT</code>
     * parameter as a <code>short</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     * the result is <code>0</code>.
     * @exception SQLException if a database access error occurs
     * @see #setShort
     */
    public short getShort(int parameterIndex) throws SQLException {

        checkClosed();
        checkIsRegisteredParameterIndex(parameterIndex);

        throw jdbcDriver.notSupported;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>INTEGER</code>
     * parameter as an <code>int</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     * the result is <code>0</code>.
     * @exception SQLException if a database access error occurs
     * @see #setInt
     */
    public int getInt(int parameterIndex) throws SQLException {

        checkClosed();
        checkIsRegisteredParameterIndex(parameterIndex);

        throw jdbcDriver.notSupported;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>BIGINT</code>
     * parameter as a <code>long</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     * the result is <code>0</code>.
     * @exception SQLException if a database access error occurs
     * @see #setLong
     */
    public long getLong(int parameterIndex) throws SQLException {

        checkClosed();
        checkIsRegisteredParameterIndex(parameterIndex);

        throw jdbcDriver.notSupported;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>FLOAT</code>
     * parameter as a <code>float</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *  and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>, the
     *   result is <code>0</code>.
     * @exception SQLException if a database access error occurs
     * @see #setFloat
     */
    public float getFloat(int parameterIndex) throws SQLException {

        checkClosed();
        checkIsRegisteredParameterIndex(parameterIndex);

        throw jdbcDriver.notSupported;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>DOUBLE</code>
     * parameter as a <code>double</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *   and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     *    the result is <code>0</code>.
     * @exception SQLException if a database access error occurs
     * @see #setDouble
     */
    public double getDouble(int parameterIndex) throws SQLException {

        checkClosed();
        checkIsRegisteredParameterIndex(parameterIndex);

        throw jdbcDriver.notSupported;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>NUMERIC</code>
     * parameter as a <code>java.math.BigDecimal</code> object with
     * <i>scale</i> digits to the right of the decimal point. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *  and so on
     * @param scale the number of digits to the right of the decimal point
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     *   the result is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @deprecated use <code>getBigDecimal(int parameterIndex)</code>
     *       or <code>getBigDecimal(String parameterName)</code>
     * @see #setBigDecimal
     */
    public BigDecimal getBigDecimal(int parameterIndex,
                                    int scale) throws SQLException {

        checkClosed();
        checkIsRegisteredParameterIndex(parameterIndex);

        throw jdbcDriver.notSupported;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>BINARY</code> or
     * <code>VARBINARY</code> parameter as an array of <code>byte</code>
     * values in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param parameterIndex the first parameter is 1, the second is 2,
     *   and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     *    the result is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #setBytes
     */
    public byte[] getBytes(int parameterIndex) throws SQLException {

        checkClosed();
        checkIsRegisteredParameterIndex(parameterIndex);

        throw jdbcDriver.notSupported;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>DATE</code> parameter
     * as a <code>java.sql.Date</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param parameterIndex the first parameter is 1, the second is 2,
     *   and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>, the
     *    result is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #setDate
     */
    public java.sql.Date getDate(int parameterIndex) throws SQLException {

        checkClosed();
        checkIsRegisteredParameterIndex(parameterIndex);

        throw jdbcDriver.notSupported;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>TIME</code> parameter
     * as a <code>java.sql.Time</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *   and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     *    the result is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #setTime
     */
    public java.sql.Time getTime(int parameterIndex) throws SQLException {

        checkClosed();
        checkIsRegisteredParameterIndex(parameterIndex);

        throw jdbcDriver.notSupported;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>TIMESTAMP</code>
     * parameter as a <code>java.sql.Timestamp</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *   and so on
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     *    the result is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #setTimestamp
     */
    public java.sql.Timestamp getTimestamp(int parameterIndex)
    throws SQLException {

        checkClosed();
        checkIsRegisteredParameterIndex(parameterIndex);

        throw jdbcDriver.notSupported;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated parameter as an <code>Object</code>
     * in the Java programming language. If the value is an SQL <code>NULL</code>,
     * the driver returns a Java <code>null</code>.
     * <p>
     * This method returns a Java object whose type corresponds to the JDBC
     * type that was registered for this parameter using the method
     * <code>registerOutParameter</code>.  By registering the target JDBC
     * type as <code>java.sql.Types.OTHER</code>, this method can be used
     * to read database-specific abstract data types. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *   and so on
     * @return A <code>java.lang.Object</code> holding the OUT parameter value
     * @exception SQLException if a database access error occurs
     * @see java.sql.Types
     * @see #setObject
     */
    public Object getObject(int parameterIndex) throws SQLException {

        checkClosed();
        checkIsRegisteredParameterIndex(parameterIndex);

        throw jdbcDriver.notSupported;
    }

// ----------------------------------- JDBC 2 ----------------------------------

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>NUMERIC</code>
     * parameter as a <code>java.math.BigDecimal</code> object with as many
     * digits to the right of the decimal point as the value contains. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @return the parameter value in full precision.  If the value is
     * SQL <code>NULL</code>, the result is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #setBigDecimal
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcPreparedStatement)
     */
    public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {

        checkClosed();
        checkIsRegisteredParameterIndex(parameterIndex);

        throw jdbcDriver.notSupported;
    }

    /**
     * <!-- start generic documentation -->
     * Returns an object representing the value of OUT parameter
     * <code>i</code> and uses <code>map</code> for the custom
     * mapping of the parameter value.
     * <p>
     * This method returns a Java object whose type corresponds to the
     * JDBC type that was registered for this parameter using the method
     * <code>registerOutParameter</code>.  By registering the target
     * JDBC type as <code>java.sql.Types.OTHER</code>, this method can
     * be used to read database-specific abstract data types. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param i the first parameter is 1, the second is 2, and so on
     * @param map the mapping from SQL type names to Java classes
     * @return a <code>java.lang.Object</code> holding the OUT parameter value
     * @exception SQLException if a database access error occurs
     * @see #setObject
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcPreparedStatement)
     */
    public Object getObject(int i, Map map) throws SQLException {

        checkClosed();
        checkIsRegisteredParameterIndex(i);

        throw jdbcDriver.notSupported;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC
     * <code>REF(&lt;structured-type&gt;)</code> parameter as a
     * {@link java.sql.Ref} object in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param i the first parameter is 1, the second is 2,
     * and so on
     * @return the parameter value as a <code>Ref</code> object in the
     * Java programming language.  If the value was SQL <code>NULL</code>,
     * the value <code>null</code> is returned.
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcPreparedStatement)
     */
    public Ref getRef(int i) throws SQLException {

        checkClosed();
        checkIsRegisteredParameterIndex(i);

        throw jdbcDriver.notSupported;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>BLOB</code>
     * parameter as a {@link java.sql.Blob} object in the Java
     * programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param i the first parameter is 1, the second is 2,
     * and so on
     * @return the parameter value as a <code>Blob</code> object in the
     * Java programming language.  If the value was SQL <code>NULL</code>,
     * the value <code>null</code> is returned.
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcPreparedStatement)
     */
    public Blob getBlob(int i) throws SQLException {

        checkClosed();
        checkIsRegisteredParameterIndex(i);

        throw jdbcDriver.notSupported;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>CLOB</code>
     * parameter as a <code>Clob</code> object in the Java programming l
     * anguage. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param i the first parameter is 1, the second is 2, and
     * so on
     * @return the parameter value as a <code>Clob</code> object in the
     * Java programming language.  If the value was SQL <code>NULL</code>, the
     * value <code>null</code> is returned.
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcPreparedStatement)
     */
    public Clob getClob(int i) throws SQLException {

        checkClosed();
        checkIsRegisteredParameterIndex(i);

        throw jdbcDriver.notSupported;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>ARRAY</code>
     * parameter as an {@link Array} object in the Java programming
     * language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param i the first parameter is 1, the second is 2, and
     * so on
     * @return the parameter value as an <code>Array</code> object in
     * the Java programming language.  If the value was SQL <code>NULL</code>,
     * the value <code>null</code> is returned.
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcPreparedStatement)
     */
    public Array getArray(int i) throws SQLException {

        checkClosed();
        checkIsRegisteredParameterIndex(i);

        throw jdbcDriver.notSupported;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>DATE</code>
     * parameter as a <code>java.sql.Date</code> object, using
     * the given <code>Calendar</code> object
     * to construct the date.
     * With a <code>Calendar</code> object, the driver
     * can calculate the date taking into account a custom timezone and
     * locale.  If no <code>Calendar</code> object is specified, the driver
     * uses the default timezone and locale. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     *      and so on
     * @param cal the <code>Calendar</code> object the driver will use
     *      to construct the date
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     *      the result is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #setDate
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *      jdbcPreparedStatement)
     */
    public java.sql.Date getDate(int parameterIndex,
                                 Calendar cal) throws SQLException {

        try {
            return HsqlDateTime.getDate(getString(parameterIndex), cal);
        } catch (Exception e) {
            throw jdbcDriver.sqlException(Trace.INVALID_ESCAPE,
                                          e.getMessage());
        }
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>TIME</code>
     * parameter as a <code>java.sql.Time</code> object, using
     * the given <code>Calendar</code> object
     * to construct the time.
     * With a <code>Calendar</code> object, the driver
     * can calculate the time taking into account a custom timezone and locale.
     * If no <code>Calendar</code> object is specified, the driver uses the
     * default timezone and locale. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @param cal the <code>Calendar</code> object the driver will use
     *        to construct the time
     * @return the parameter value; if the value is SQL <code>NULL</code>,
     *     the result is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #setTime
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *    jdbcPreparedStatement)
     */
    public java.sql.Time getTime(int parameterIndex,
                                 Calendar cal) throws SQLException {

        try {
            return HsqlDateTime.getTime(getString(parameterIndex), cal);
        } catch (Exception e) {
            throw jdbcDriver.sqlException(Trace.INVALID_ESCAPE,
                                          e.getMessage());
        }
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>TIMESTAMP</code>
     * parameter as a <code>java.sql.Timestamp</code> object, using
     * the given <code>Calendar</code> object to construct
     * the <code>Timestamp</code> object.
     * With a <code>Calendar</code> object, the driver
     * can calculate the timestamp taking into account a custom timezone and
     * locale. If no <code>Calendar</code> object is specified, the driver
     * uses the default timezone and locale. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     *
     *
     * @param parameterIndex the first parameter is 1, the second is 2,
     * and so on
     * @param cal the <code>Calendar</code> object the driver will use
     *        to construct the timestamp
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     *        the result is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #setTimestamp
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *    jdbcPreparedStatement)
     */
    public java.sql.Timestamp getTimestamp(int parameterIndex,
                                           Calendar cal) throws SQLException {

        try {
            return HsqlDateTime.getTimestamp(getString(parameterIndex), cal);
        } catch (Exception e) {
            throw jdbcDriver.sqlException(Trace.INVALID_ESCAPE,
                                          e.getMessage());
        }
    }

    /**
     * <!-- start generic documentation -->
     * Registers the designated output parameter.  This version of
     * the method <code>registerOutParameter</code>
     * should be used for a user-defined or <code>REF</code> output parameter.
     * Examples of user-defined types include: <code>STRUCT</code>,
     * <code>DISTINCT</code>, <code>JAVA_OBJECT</code>, and named array types.
     *
     * Before executing a stored procedure call, you must explicitly
     * call <code>registerOutParameter</code> to register the type from
     * <code>java.sql.Types</code> for each
     * OUT parameter.  For a user-defined parameter, the fully-qualified SQL
     * type name of the parameter should also be given, while a
     * <code>REF</code> parameter requires that the fully-qualified type name
     * of the referenced type be given.  A JDBC driver that does not need the
     * type code and type name information may ignore it.   To be portable,
     * however, applications should always provide these values for
     * user-defined and <code>REF</code> parameters.
     *
     * Although it is intended for user-defined and <code>REF</code> parameters,
     * this method may be used to register a parameter of any JDBC type.
     * If the parameter does not have a user-defined or <code>REF</code> type,
     * the <i>typeName</i> parameter is ignored.
     *
     * <P><B>Note:</B> When reading the value of an out parameter, you
     * must use the getter method whose Java type corresponds to the
     * parameter's registered SQL type. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param paramIndex the first parameter is 1, the second is 2,...
     * @param sqlType a value from {@link java.sql.Types}
     * @param typeName the fully-qualified name of an SQL structured type
     * @exception SQLException if a database access error occurs
     * @see java.sql.Types
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcPreparedStatement)
     *
     */
    public void registerOutParameter(int paramIndex, int sqlType,
                                     String typeName) throws SQLException {
        registerOutParameter(paramIndex, sqlType);
    }

// ----------------------------------- JDBC 3 ----------------------------------

    /**
     * <!-- start generic documentation -->
     * Registers the OUT parameter named
     * <code>parameterName</code> to the JDBC type
     * <code>sqlType</code>.  All OUT parameters must be registered
     * before a stored procedure is executed.
     * <p>
     * The JDBC type specified by <code>sqlType</code> for an OUT
     * parameter determines the Java type that must be used
     * in the <code>get</code> method to read the value of that parameter.
     * <p>
     * If the JDBC type expected to be returned to this output parameter
     * is specific to this particular database, <code>sqlType</code>
     * should be <code>java.sql.Types.OTHER</code>.  The method
     * {@link #getObject} retrieves the value. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param parameterName the name of the parameter
     * @param sqlType the JDBC type code defined by <code>java.sql.Types</code>.
     * If the parameter is of JDBC type <code>NUMERIC</code>
     * or <code>DECIMAL</code>, the version of
     * <code>registerOutParameter</code> that accepts a scale value
     * should be used.
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQL 1.7.0
     * @see java.sql.Types
     */
//#ifdef JDBC3
/*
    public void registerOutParameter(String parameterName,
                                     int sqlType) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Registers the parameter named
     * <code>parameterName</code> to be of JDBC type
     * <code>sqlType</code>.  This method must be called
     * before a stored procedure is executed.
     * <p>
     * The JDBC type specified by <code>sqlType</code> for an OUT
     * parameter determines the Java type that must be used
     * in the <code>get</code> method to read the value of that parameter.
     * <p>
     * This version of <code>registerOutParameter</code> should be
     * used when the parameter is of JDBC type <code>NUMERIC</code>
     * or <code>DECIMAL</code>. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param parameterName the name of the parameter
     * @param sqlType SQL type code defined by <code>java.sql.Types</code>.
     * @param scale the desired number of digits to the right of the
     * decimal point.  It must be greater than or equal to zero.
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQL 1.7.0
     * @see java.sql.Types
     */
//#ifdef JDBC3
/*
    public void registerOutParameter(String parameterName, int sqlType,
                                     int scale) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Registers the designated output parameter.  This version of
     * the method <code>registerOutParameter</code>
     * should be used for a user-named or REF output parameter.  Examples
     * of user-named types include: STRUCT, DISTINCT, JAVA_OBJECT, and
     * named array types.
     *
     * Before executing a stored procedure call, you must explicitly
     * call <code>registerOutParameter</code> to register the type from
     * <code>java.sql.Types</code> for each
     * OUT parameter.  For a user-named parameter the fully-qualified SQL
     * type name of the parameter should also be given, while a REF
     * parameter requires that the fully-qualified type name of the
     * referenced type be given.  A JDBC driver that does not need the
     * type code and type name information may ignore it.   To be portable,
     * however, applications should always provide these values for
     * user-named and REF parameters.
     *
     * Although it is intended for user-named and REF parameters,
     * this method may be used to register a parameter of any JDBC type.
     * If the parameter does not have a user-named or REF type, the
     * typeName parameter is ignored.
     *
     * <P><B>Note:</B> When reading the value of an out parameter, you
     * must use the <code>getXXX</code> method whose Java type XXX corresponds
     * to the parameter's registered SQL type. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param sqlType a value from {@link java.sql.Types}
     * @param typeName the fully-qualified name of an SQL structured type
     * @exception SQLException if a database access error occurs
     * @see java.sql.Types
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public void registerOutParameter(String parameterName, int sqlType,
                                     String typeName) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of the designated JDBC <code>DATALINK</code>
     * parameter as a <code>java.net.URL</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2,...
     * @return a <code>java.net.URL</code> object that represents the
     *   JDBC <code>DATALINK</code> value used as the designated
     *   parameter
     * @exception SQLException if a database access error occurs,
     *      or if the URL being returned is
     *      not a valid URL on the Java platform
     * @see #setURL
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public java.net.URL getURL(int parameterIndex) throws SQLException {
        checkClosed();
        checkIsRegisteredParameterIndex(parameterIndex);
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given <code>java.net.URL</code>
     * object.  The driver converts this to an SQL <code>DATALINK</code>
     * value when it sends it to the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param val the parameter value
     * @exception SQLException if a database access error occurs,
     *      or if a URL is malformed
     * @see #getURL
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public void setURL(String parameterName,
                       java.net.URL val) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to SQL <code>NULL</code>.
     *
     * <P><B>Note:</B> You must specify the parameter's SQL type. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param sqlType the SQL type code defined in <code>java.sql.Types</code>
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public void setNull(String parameterName, int sqlType) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given Java <code>boolean</code>
     * value. The driver converts this to an SQL <code>BIT</code> value when
     * it sends it to the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     * @see #getBoolean
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public void setBoolean(String parameterName, boolean x) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given Java <code>byte</code> value.
     * The driver converts this to an SQL <code>TINYINT</code> value when it
     * sends it to the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     * @see #getByte
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public void setByte(String parameterName, byte x) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given Java <code>short</code> value.
     * The driver converts this to an SQL <code>SMALLINT</code> value when
     * it sends it to the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     * @see #getShort
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public void setShort(String parameterName, short x) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given Java <code>int</code> value.
     * The driver converts this to an SQL <code>INTEGER</code> value when it
     * sends it to the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     * @see #getInt
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public void setInt(String parameterName, int x) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given Java <code>long</code> value.
     * The driver converts this to an SQL <code>BIGINT</code> value when it
     * sends it to the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     * @see #getLong
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public void setLong(String parameterName, long x) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given Java <code>float</code> value.
     * The driver converts this to an SQL <code>FLOAT</code> value when it
     * sends it to the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     * @see #getFloat
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public void setFloat(String parameterName, float x) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given Java <code>double</code> value.
     * The driver converts this to an SQL <code>DOUBLE</code> value when it
     * sends it to the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     * @see #getDouble
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public void setDouble(String parameterName, double x) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given
     * <code>java.math.BigDecimal</code> value.
     * The driver converts this to an SQL <code>NUMERIC</code> value when
     * it sends it to the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     * @see #getBigDecimal
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public void setBigDecimal(String parameterName,
                              BigDecimal x) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given Java <code>String</code>
     * value. The driver converts this to an SQL <code>VARCHAR</code>
     * or <code>LONGVARCHAR</code> value (depending on the argument's
     * size relative to the driver's limits on <code>VARCHAR</code> values)
     * when it sends it to the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     * @see #getString
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public void setString(String parameterName, String x) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given Java array of bytes.
     * The driver converts this to an SQL <code>VARBINARY</code> or
     * <code>LONGVARBINARY</code> (depending on the argument's size relative
     * to the driver's limits on <code>VARBINARY</code> values) when it sends
     * it to the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     * @see #getBytes
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public void setBytes(String parameterName, byte[] x) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given <code>java.sql.Date</code>
     * value.  The driver converts this to an SQL <code>DATE</code> value
     * when it sends it to the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     * @see #getDate
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public void setDate(String parameterName,
                        java.sql.Date x) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given <code>java.sql.Time</code>
     * value.  The driver converts this to an SQL <code>TIME</code> value
     * when it sends it to the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     * @see #getTime
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public void setTime(String parameterName,
                        java.sql.Time x) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given
     * <code>java.sql.Timestamp</code> value. The driver
     * converts this to an SQL <code>TIMESTAMP</code> value when it
     * sends it to the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     * @see #getTimestamp
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public void setTimestamp(String parameterName,
                             java.sql.Timestamp x) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given input stream, which will
     * have the specified number of bytes.
     * When a very large ASCII value is input to a <code>LONGVARCHAR</code>
     * parameter, it may be more practical to send it via a
     * <code>java.io.InputStream</code>. Data will be read from the stream
     * as needed until end-of-file is reached.  The JDBC driver will
     * do any necessary conversion from ASCII to the database char format.
     *
     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the Java input stream that contains the ASCII parameter value
     * @param length the number of bytes in the stream
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public void setAsciiStream(String parameterName, java.io.InputStream x,
                               int length) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given input stream, which will
     * have the specified number of bytes.
     * When a very large binary value is input to a <code>LONGVARBINARY</code>
     * parameter, it may be more practical to send it via a
     * <code>java.io.InputStream</code> object. The data will be read from
     * the stream as needed until end-of-file is reached.
     *
     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the java input stream which contains the binary parameter value
     * @param length the number of bytes in the stream
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public void setBinaryStream(String parameterName, java.io.InputStream x,
                                int length) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the value of the designated parameter with the given object.
     * The second argument must be an object type; for integral values, the
     * <code>java.lang</code> equivalent objects should be used.
     *
     * <p>The given Java object will be converted to the given targetSqlType
     * before being sent to the database.
     *
     * If the object has a custom mapping (is of a class implementing the
     * interface <code>SQLData</code>),
     * the JDBC driver should call the method <code>SQLData.writeSQL</code>
     * to write it to the SQL data stream.
     * If, on the other hand, the object is of a class implementing
     * <code>Ref</code>, <code>Blob</code>, <code>Clob</code>,
     * <code>Struct</code>, or <code>Array</code>, the driver should pass it
     * to the database as a value of the corresponding SQL type.
     * <P>
     * Note that this method may be used to pass datatabase-
     * specific abstract data types. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the object containing the input parameter value
     * @param targetSqlType the SQL type (as defined in java.sql.Types) to be
     * sent to the database. The scale argument may further qualify this type.
     * @param scale for java.sql.Types.DECIMAL or java.sql.Types.NUMERIC types,
     *    this is the number of digits after the decimal point.  For all
     *    other types, this value will be ignored.
     * @exception SQLException if a database access error occurs
     * @see java.sql.Types
     * @see #getObject
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public void setObject(String parameterName, Object x, int targetSqlType,
                          int scale) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the value of the designated parameter with the given object.
     * This method is like the method <code>setObject</code>
     * above, except that it assumes a scale of zero. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the object containing the input parameter value
     * @param targetSqlType the SQL type (as defined in java.sql.Types) to be
     *                 sent to the database
     * @exception SQLException if a database access error occurs
     * @see #getObject
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public void setObject(String parameterName, Object x,
                          int targetSqlType) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the value of the designated parameter with the given object.
     * The second parameter must be of type <code>Object</code>; therefore,
     * the <code>java.lang</code> equivalent objects should be used for
     * built-in types.
     *
     * <p>The JDBC specification specifies a standard mapping from
     * Java <code>Object</code> types to SQL types.  The given argument
     * will be converted to the corresponding SQL type before being
     * sent to the database.
     *
     * <p>Note that this method may be used to pass datatabase-
     * specific abstract data types, by using a driver-specific Java
     * type.
     *
     * If the object is of a class implementing the interface
     * <code>SQLData</code>, the JDBC driver should call the method
     * <code>SQLData.writeSQL</code> to write it to the SQL data stream.
     * If, on the other hand, the object is of a class implementing
     * <code>Ref</code>, <code>Blob</code>, <code>Clob</code>,
     * <code>Struct</code>, or <code>Array</code>, the driver should pass it
     * to the database as a value of the corresponding SQL type.
     * <P>
     * This method throws an exception if there is an ambiguity, for example,
     * if the object is of a class implementing more than one of the
     * interfaces named above. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the object containing the input parameter value
     * @exception SQLException if a database access error occurs or if the given
     *      <code>Object</code> parameter is ambiguous
     * @see #getObject
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public void setObject(String parameterName, Object x) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given <code>Reader</code>
     * object, which is the given number of characters long.
     * When a very large UNICODE value is input to a <code>LONGVARCHAR</code>
     * parameter, it may be more practical to send it via a
     * <code>java.io.Reader</code> object. The data will be read from the
     * stream as needed until end-of-file is reached.  The JDBC driver will
     * do any necessary conversion from UNICODE to the database char format.
     *
     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param reader the <code>java.io.Reader</code> object that
     *  contains the UNICODE data used as the designated parameter
     * @param length the number of characters in the stream
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public void setCharacterStream(String parameterName, java.io.Reader reader,
                                   int length) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given <code>java.sql.Date</code>
     * value, using the given <code>Calendar</code> object.  The driver uses
     * the <code>Calendar</code> object to construct an SQL <code>DATE</code>
     * value, which the driver then sends to the database.  With a
     * a <code>Calendar</code> object, the driver can calculate the date
     * taking into account a custom timezone.  If no
     * <code>Calendar</code> object is specified, the driver uses the default
     * timezone, which is that of the virtual machine running the
     * application. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the parameter value
     * @param cal the <code>Calendar</code> object the driver will use
     *      to construct the date
     * @exception SQLException if a database access error occurs
     * @see #getDate
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public void setDate(String parameterName, java.sql.Date x,
                        Calendar cal) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given <code>java.sql.Time</code>
     * value, using the given <code>Calendar</code> object.  The driver uses
     * the <code>Calendar</code> object to construct an SQL <code>TIME</code>
     * value, which the driver then sends to the database.  With a
     * a <code>Calendar</code> object, the driver can calculate the time
     * taking into account a custom timezone.  If no
     * <code>Calendar</code> object is specified, the driver uses the default
     * timezone, which is that of the virtual machine running the
     * application. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the parameter value
     * @param cal the <code>Calendar</code> object the driver will use
     *      to construct the time
     * @exception SQLException if a database access error occurs
     * @see #getTime
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public void setTime(String parameterName, java.sql.Time x,
                        Calendar cal) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given
     * <code>java.sql.Timestamp</code> value, using the given
     * <code>Calendar</code> object.  The driver uses the
     * <code>Calendar</code> object to construct an SQL
     * <code>TIMESTAMP</code> value, which the driver then sends to the
     * database.  With a <code>Calendar</code> object, the driver can
     * calculate the timestamp taking into account a custom timezone.  If no
     * <code>Calendar</code> object is specified, the driver uses the default
     * timezone, which is that of the virtual machine running the
     * application. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param x the parameter value
     * @param cal the <code>Calendar</code> object the driver will use
     *      to construct the timestamp
     * @exception SQLException if a database access error occurs
     * @see #getTimestamp
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public void setTimestamp(String parameterName, java.sql.Timestamp x,
                             Calendar cal) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to SQL <code>NULL</code>.
     * This version of the method <code>setNull</code> should
     * be used for user-defined types and <code>REF</code> type parameters.
     * Examples of user-defined types include: <code>STRUCT</code>,
     * <code>DISTINCT</code>, <code>JAVA_OBJECT</code>, and
     * named array types.
     *
     * <P><B>Note:</B> To be portable, applications must give the
     * SQL type code and the fully-qualified SQL type name when specifying
     * a <code>NULL</code> user-defined or <code>REF</code> parameter.
     * In the case of a user-defined type the name is the type name of the
     * parameter itself.  For a <code>REF</code> parameter, the name is the
     * type name of the referenced type.  If a JDBC driver does not need
     * the type code or type name information, it may ignore it.
     *
     * Although it is intended for user-defined and <code>Ref</code>
     * parameters, this method may be used to set a null parameter of
     * any JDBC type. If the parameter does not have a user-defined or
     * <code>REF</code> type, the given <code>typeName</code> is ignored. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @param parameterName the name of the parameter
     * @param sqlType a value from <code>java.sql.Types</code>
     * @param typeName the fully-qualified name of an SQL user-defined type;
     *  ignored if the parameter is not a user-defined type or
     *  SQL <code>REF</code> value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public void setNull(String parameterName, int sqlType,
                        String typeName) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>CHAR</code>, <code>VARCHAR</code>,
     * or <code>LONGVARCHAR</code> parameter as a <code>String</code> in
     * the Java programming language.
     * <p>
     * For the fixed-length type JDBC <code>CHAR</code>,
     * the <code>String</code> object
     * returned has exactly the same value the JDBC
     * <code>CHAR</code> value had in the
     * database, including any padding added by the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param parameterName the name of the parameter
     * @return the parameter value. If the value is SQL <code>NULL</code>,
     * the result is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #setString
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public String getString(String parameterName) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>BIT</code> parameter as a
     * <code>boolean</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param parameterName the name of the parameter
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     * the result is <code>false</code>.
     * @exception SQLException if a database access error occurs
     * @see #setBoolean
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public boolean getBoolean(String parameterName) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>TINYINT</code> parameter as a
     * <code>byte</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param parameterName the name of the parameter
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     * the result is <code>0</code>.
     * @exception SQLException if a database access error occurs
     * @see #setByte
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public byte getByte(String parameterName) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>SMALLINT</code> parameter as
     * a <code>short</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param parameterName the name of the parameter
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     * the result is <code>0</code>.
     * @exception SQLException if a database access error occurs
     * @see #setShort
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public short getShort(String parameterName) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>INTEGER</code> parameter as
     * an <code>int</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     *   the result is <code>0</code>.
     * @exception SQLException if a database access error occurs
     * @see #setInt
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public int getInt(String parameterName) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>BIGINT</code> parameter as
     * a <code>long</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     *   the result is <code>0</code>.
     * @exception SQLException if a database access error occurs
     * @see #setLong
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public long getLong(String parameterName) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>FLOAT</code> parameter as
     * a <code>float</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param parameterName the name of the parameter
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     *   the result is <code>0</code>.
     * @exception SQLException if a database access error occurs
     * @see #setFloat
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public float getFloat(String parameterName) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>DOUBLE</code> parameter as
     * a <code>double</code> in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param parameterName the name of the parameter
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     *   the result is <code>0</code>.
     * @exception SQLException if a database access error occurs
     * @see #setDouble
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public double getDouble(String parameterName) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>BINARY</code> or
     * <code>VARBINARY</code> parameter as an array of <code>byte</code>
     * values in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param parameterName the name of the parameter
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     *      the result is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #setBytes
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public byte[] getBytes(String parameterName) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>DATE</code> parameter as a
     * <code>java.sql.Date</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     *      the result is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #setDate
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public java.sql.Date getDate(String parameterName) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>TIME</code> parameter as a
     * <code>java.sql.Time</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     *      the result is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #setTime
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public java.sql.Time getTime(String parameterName) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>TIMESTAMP</code> parameter as a
     * <code>java.sql.Timestamp</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param parameterName the name of the parameter
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     *      the result is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #setTimestamp
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public java.sql.Timestamp getTimestamp(String parameterName)
    throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a parameter as an <code>Object</code> in the Java
     * programming language. If the value is an SQL <code>NULL</code>, the
     * driver returns a Java <code>null</code>.
     * <p>
     * This method returns a Java object whose type corresponds to the JDBC
     * type that was registered for this parameter using the method
     * <code>registerOutParameter</code>.  By registering the target JDBC
     * type as <code>java.sql.Types.OTHER</code>, this method can be used
     * to read database-specific abstract data types. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param parameterName the name of the parameter
     * @return A <code>java.lang.Object</code> holding the OUT parameter value.
     * @exception SQLException if a database access error occurs
     * @see java.sql.Types
     * @see #setObject
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public Object getObject(String parameterName) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>NUMERIC</code> parameter as a
     * <code>java.math.BigDecimal</code> object with as many digits to the
     * right of the decimal point as the value contains. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param parameterName the name of the parameter
     * @return the parameter value in full precision.  If the value is
     * SQL <code>NULL</code>, the result is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #setBigDecimal
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public BigDecimal getBigDecimal(String parameterName) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Returns an object representing the value of OUT parameter
     * <code>i</code> and uses <code>map</code> for the custom
     * mapping of the parameter value.
     * <p>
     * This method returns a Java object whose type corresponds to the
     * JDBC type that was registered for this parameter using the method
     * <code>registerOutParameter</code>.  By registering the target
     * JDBC type as <code>java.sql.Types.OTHER</code>, this method can
     * be used to read database-specific abstract data types. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param parameterName the name of the parameter
     * @param map the mapping from SQL type names to Java classes
     * @return a <code>java.lang.Object</code> holding the OUT parameter value
     * @exception SQLException if a database access error occurs
     * @see #setObject
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public Object getObject(String parameterName,
                            Map map) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>REF(&lt;structured-type&gt;)</code>
     * parameter as a {@link Ref} object in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @return the parameter value as a <code>Ref</code> object in the
     *    Java programming language.  If the value was SQL <code>NULL</code>,
     *    the value <code>null</code> is returned.
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public Ref getRef(String parameterName) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>BLOB</code> parameter as a
     * {@link Blob} object in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @return the parameter value as a <code>Blob</code> object in the
     *    Java programming language.  If the value was SQL <code>NULL</code>,
     *    the value <code>null</code> is returned.
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public Blob getBlob(String parameterName) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>CLOB</code> parameter as a
     * <code>Clob</code> object in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param parameterName the name of the parameter
     * @return the parameter value as a <code>Clob</code> object in the
     *    Java programming language.  If the value was SQL <code>NULL</code>,
     *    the value <code>null</code> is returned.
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public Clob getClob(String parameterName) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>ARRAY</code> parameter as an
     * {@link Array} object in the Java programming language. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @return the parameter value as an <code>Array</code> object in
     *    Java programming language.  If the value was SQL <code>NULL</code>,
     *    the value <code>null</code> is returned.
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public Array getArray(String parameterName) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>DATE</code> parameter as a
     * <code>java.sql.Date</code> object, using
     * the given <code>Calendar</code> object
     * to construct the date.
     * With a <code>Calendar</code> object, the driver
     * can calculate the date taking into account a custom timezone and
     * locale.  If no <code>Calendar</code> object is specified, the d
     * river uses the default timezone and locale. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param cal the <code>Calendar</code> object the driver will use
     *      to construct the date
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     * the result is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #setDate
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public java.sql.Date getDate(String parameterName,
                                 Calendar cal) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>TIME</code> parameter as a
     * <code>java.sql.Time</code> object, using
     * the given <code>Calendar</code> object
     * to construct the time.
     * With a <code>Calendar</code> object, the driver
     * can calculate the time taking into account a custom timezone and
     * locale. If no <code>Calendar</code> object is specified, the driver
     * uses the default timezone and locale. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @param cal the <code>Calendar</code> object the driver will use
     *      to construct the time
     * @return the parameter value; if the value is SQL <code>NULL</code>,
     *      the result is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #setTime
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public java.sql.Time getTime(String parameterName,
                                 Calendar cal) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>TIMESTAMP</code> parameter as a
     * <code>java.sql.Timestamp</code> object, using
     * the given <code>Calendar</code> object to construct
     * the <code>Timestamp</code> object.
     * With a <code>Calendar</code> object, the driver
     * can calculate the timestamp taking into account a custom timezone
     * and locale.  If no <code>Calendar</code> object is specified, the
     * driver uses the default timezone and locale. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @param parameterName the name of the parameter
     * @param cal the <code>Calendar</code> object the driver will use
     *      to construct the timestamp
     * @return the parameter value.  If the value is SQL <code>NULL</code>,
     *      the result is <code>null</code>.
     * @exception SQLException if a database access error occurs
     * @see #setTimestamp
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public java.sql.Timestamp getTimestamp(String parameterName,
                                           Calendar cal) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the value of a JDBC <code>DATALINK</code> parameter as a
     * <code>java.net.URL</code> object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterName the name of the parameter
     * @return the parameter value as a <code>java.net.URL</code> object in the
     *      Java programming language.  If the value was SQL
     *      <code>NULL</code>, the value <code>null</code> is returned.
     * @exception SQLException if a database access error occurs,
     *      or if there is a problem with the URL
     * @see #setURL
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public java.net.URL getURL(String parameterName) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3
}
