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

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Calendar;
import org.hsqldb.lib.Iterator;
import org.hsqldb.lib.StringConverter;
import org.hsqldb.store.ValuePool;

// fredt@users 20020320 - patch 1.7.0 - JDBC 2 support and error trapping
// JDBC 2 methods can now be called from jdk 1.1.x - see javadoc comments
// boucherb@users 20020509 - added "throws SQLException" to all methods where
// it was missing here but specified in the java.sql.PreparedStatement and
// java.sqlCallableStatement interfaces, updated generic documentation to
// JDK 1.4, and added JDBC3 methods and docs
// boucherb@users and fredt@users 20020409/20020505 extensive review and update
// of docs and behaviour to comply with previous and latest java.sql specification
// fredt@users 20030620 - patch 1.7.2 - rewritten to support real prepared statements
// boucherb@users 20030801 - patch 1.7.2 - support for batch execution
// boucherb@users 20030801 - patch 1.7.2 - support for getMetaData and getParameterMetadata
// boucherb@users 20030801 - patch 1.7.2 - updated some setXXX methods

/**
 * <!-- start generic documentation -->
 *
 * An object that represents a precompiled SQL statement. <p>
 *
 * An SQL statement is precompiled and stored in a
 * <code>PreparedStatement</code> object. This object can then be used to
 * efficiently execute this statement multiple times.
 *
 * <P><B>Note:</B> The setter methods (<code>setShort</code>,
 * <code>setString</code>, and so on) for setting IN parameter values
 * must specify types that are compatible with the defined SQL type of
 * the input parameter. For instance, if the IN parameter has SQL type
 * <code>INTEGER</code>, then the method <code>setInt</code> should be
 * used. <p>
 *
 * If arbitrary parameter type conversions are required, the method
 * <code>setObject</code> should be used with a target SQL type.
 * <P>
 * In the following example of setting a parameter, <code>con</code>
 * represents an active connection:
 * <PRE>
 * PreparedStatement pstmt = con.prepareStatement("UPDATE EMPLOYEES
 *                               SET SALARY = ? WHERE ID = ?");
 * pstmt.setBigDecimal(1, 153833.00)
 * pstmt.setInt(2, 110592)
 * </PRE> <p>
 * <!-- end generic documentation -->
 * <!-- start Release-specific documentation -->
 * <b>Multi thread use:</b> <p>
 *
 * A PreparedStatement object is stateful and should not normally be shared
 * by multiple threads. If it has to be shared, the calls to set the
 * parameters, calls to add batch statements, the execute call and any
 * post-execute calls should be made within a block synchronized on the
 * PreparedStatement Object.<p>
 *
 * <b>JRE 1.1.x Notes:</b> <p>
 *
 * In general, JDBC 2 support requires Java 1.2 and above, and JDBC3 requires
 * Java 1.4 and above. In HSQLDB, support for methods introduced in different
 * versions of JDBC depends on the JDK version used for compiling and building
 * HSQLDB.<p>
 *
 * Since 1.7.0, it is possible to build the product so that
 * all JDBC 2 methods can be called while executing under the version 1.1.x
 * <em>Java Runtime Environment</em><sup><font size="-2">TM</font></sup>.
 * However, in addition to requiring explicit casts to the org.hsqldb.jdbcXXX
 * interface implementations, some of these method calls require
 * <code>int</code> values that are defined only in the JDBC 2 or greater
 * version of
 * <a href="http://java.sun.com/j2se/1.4/docs/api/java/sql/ResultSet.html">
 * <code>ResultSet</code></a> interface.  For this reason, when the
 * product is compiled under JDK 1.1.x, these values are defined in
 * {@link jdbcResultSet jdbcResultSet}.<p>
 *
 * In a JRE 1.1.x environment, calling JDBC 2 methods that take or return the
 * JDBC2-only <code>ResultSet</code> values can be achieved by referring
 * to them in parameter specifications and return value comparisons,
 * respectively, as follows: <p>
 *
 * <CODE class="JavaCodeExample">
 * jdbcResultSet.FETCH_FORWARD<br>
 * jdbcResultSet.TYPE_FORWARD_ONLY<br>
 * jdbcResultSet.TYPE_SCROLL_INSENSITIVE<br>
 * jdbcResultSet.CONCUR_READ_ONLY<br>
 * </code> <p>
 *
 * However, please note that code written in such a manner will not be
 * compatible for use with other JDBC 2 drivers, since they expect and use
 * <code>ResultSet</code>, rather than <code>jdbcResultSet</code>.  Also
 * note, this feature is offered solely as a convenience to developers
 * who must work under JDK 1.1.x due to operating constraints, yet wish to
 * use some of the more advanced features available under the JDBC 2
 * specification.<p>
 *
 * (fredt@users)<br>
 * (boucherb@users)<p>
 *
 * </span>
 * <!-- end Release-specific documentation -->
 *
 * @see jdbcConnection#prepareStatement
 * @see jdbcResultSet
 */
public class jdbcPreparedStatement extends org.hsqldb.jdbcStatement
implements java.sql.PreparedStatement {

    /** The parameter values for the next non-batch execution. */
    protected Object[] parameterValues;

    /** The SQL types of the parameters. */
    protected int[] parameterTypes;

    /** The (IN, IN OUT, or OUT) modes of parameters */
    protected int[] parameterModes;

    /**
     * Description of result set metadata. <p>
     *
     * Note that getColumnDisplaySize() will not
     * necessarily the same as that returned by a
     * a retrieved ResultSet object's ResultSetMetaData
     * object.  This is because we currently approximate
     * the value by scanning certain columns of the row data
     * to find the approximate max length of the String
     * representation
     */
    protected Result rsmdDescriptor;

    /** Description of parameter metadata. */
    protected Result pmdDescriptor;

    /** This object's one and one ResultSetMetaData object. */
    protected jdbcResultSetMetaData rsmd;

// NOTE:  pmd is declared as Object to avoid yet another #ifdef.

    /** This object's one and only ParameterMetaData object. */
    protected Object pmd;

    /** The SQL character sequence that this object represents. */
    protected String sql;

    /**
     * The id with which this object's corresponding CompiledStatement
     * object is registered in the engine's CompiledStatementManager object.
     */
    protected int statementID;

    /**
     * The type of this statement, from org.hsqldb.CompiledStatement
     */
    protected int compiledStatementType;

    /**
     * Whether this statement generates only a single row update count in
     * response to execution.
     */
    protected boolean isRowCount;

// fredt@users 20020215 - patch 517028 by peterhudson@users - method defined
// fredt@users 20020215 - patch 517028 by peterhudson@users - method defined
//
// changes by fredt
// SimpleDateFormat objects moved out of methods to improve performance
// this is safe because only one thread at a time should access a
// PreparedStatement object until it has finished executing the statement
// fredt@users 20020215 - patch 517028 by peterhudson@users - method defined
// minor changes by fredt

    /**
     * <!-- start generic documentation -->
     * Sets escape processing on or off. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Starting with HSQLDB 1.7.0, the implementation follows the standard
     * behaviour by overriding the same method in jdbcStatement class. <p>
     *
     * In other words, calling this method has no effect. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param enable <code>true</code> to enable escape processing;
     *     <code>false</code> to disable it
     * @exception SQLException if a database access error occurs
     */
    public void setEscapeProcessing(boolean enable) throws SQLException {
        checkClosed();
    }

    /**
     * <!-- start generic documentation -->
     * Executes the SQL statement in this <code>PreparedStatement</code>
     * object, which may be any kind of SQL statement.
     * Some prepared statements return multiple results; the
     * <code>execute</code> method handles these complex statements as well
     * as the simpler form of statements handled by the methods
     * <code>executeQuery</code>and <code>executeUpdate</code>. <p>
     *
     * The <code>execute</code> method returns a <code>boolean</code> to
     * indicate the form of the first result.  You must call either the method
     * <code>getResultSet</code> or <code>getUpdateCount</code>
     * to retrieve the result; you must call <code>getMoreResults</code> to
     * move to any subsequent result(s). <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including HSQLDB 1.7.0, statements never return multiple
     * result sets. <p>
     *
     * Starting with HSQLDB 1.7.2, statements <i>may</i> return multiple result
     * sets under certain conditions. <p>
     *
     * With 1.7.2 (contrary to the generic documentation above) support for
     * preparation of DDL statements and character sequences representing
     * multiple SQL commands is not available.  Support for preparation of
     * single, non-parametric DDL commands may become supported before the
     * final release of 1.7.2 or in a subsequent point release. Limited support
     * for parametric DDL may or may not become supported. <p>
     *
     * </span>
     * @return <code>true</code> if the first result is a <code>ResultSet</code>
     *    object; <code>false</code> if the first result is an update
     *    count or there is no result
     * @exception SQLException if a database access error occurs or an argument
     *       is supplied to this method
     * @see jdbcStatement#execute
     * @see jdbcStatement#getResultSet
     * @see jdbcStatement#getUpdateCount
     * @see jdbcStatement#getMoreResults
     */
    public boolean execute() throws SQLException {

        checkClosed();
        connection.clearWarningsNoCheck();

        if (compiledStatementType == CompiledStatement.UNKNOWN) {
            return super.execute(sql);
        }

        resultIn = null;

        try {
            resultOut.setParameterData(parameterValues);

            resultIn = connection.sessionProxy.execute(resultOut);
        } catch (HsqlException e) {
            throw jdbcDriver.sqlException(e);
        }

        if (resultIn.iMode == ResultConstants.ERROR) {
            jdbcDriver.throwError(resultIn);
        }

        return resultIn.iMode == ResultConstants.DATA ? true
                                                      : false;
    }

    /**
     * <!-- start generic documentation -->
     * Executes the SQL query in this <code>PreparedStatement</code> object
     * and returns the <code>ResultSet</code> object generated by the query.<p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     *
     * @return a <code>ResultSet</code> object that contains the data produced
     *    by the query; never <code>null</code>
     * @exception SQLException if a database access error occurs or the SQL
     *       statement does not return a <code>ResultSet</code> object
     */
    public ResultSet executeQuery() throws SQLException {

        checkClosed();
        connection.clearWarningsNoCheck();
        checkIsRowCount(false);

        resultIn = null;

        try {
            resultOut.setParameterData(parameterValues);

            resultIn = connection.sessionProxy.execute(resultOut);
        } catch (HsqlException e) {
            throw jdbcDriver.sqlException(e);
        }

        if (resultIn.iMode == ResultConstants.ERROR) {
            jdbcDriver.throwError(resultIn);
        } else if (resultIn.iMode != ResultConstants.DATA) {
            String msg = "Expected but did not recieve a result set";

            throw jdbcDriver.sqlException(Trace.UNEXPECTED_EXCEPTION, msg);
        }

        return new jdbcResultSet(this, resultIn, connection.connProperties);
    }

    /**
     * <!-- start generic documentation -->
     * Executes the SQL statement in this <code>PreparedStatement</code>
     * object, which must be an SQL <code>INSERT</code>,
     * <code>UPDATE</code> or <code>DELETE</code> statement; or an SQL
     * statement that returns nothing, such as a DDL statement.<p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     *
     * @return either (1) the row count for <code>INSERT</code>,
     *     <code>UPDATE</code>, or <code>DELETE</code>
     *     statements or (2) 0 for SQL statements that
     *     return nothing
     * @exception SQLException if a database access error occurs or the SQL
     *        statement returns a <code>ResultSet</code> object
     */
    public int executeUpdate() throws SQLException {

        checkClosed();
        connection.clearWarningsNoCheck();
        checkIsRowCount(true);

        if (compiledStatementType == CompiledStatement.UNKNOWN) {
            return super.executeUpdate(sql);
        }

        resultIn = null;

        try {
            resultOut.setParameterData(parameterValues);

            resultIn = connection.sessionProxy.execute(resultOut);
        } catch (HsqlException e) {
            throw jdbcDriver.sqlException(e);
        }

        if (resultIn.iMode == ResultConstants.ERROR) {
            jdbcDriver.throwError(resultIn);
        } else if (resultIn.iMode != ResultConstants.UPDATECOUNT) {
            String msg = "Expected but did not recieve a row update count";

            throw jdbcDriver.sqlException(Trace.UNEXPECTED_EXCEPTION, msg);
        }

        return resultIn.getUpdateCount();
    }

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to SQL <code>NULL</code>. <p>
     *
     * <B>Note:</B> You must specify the parameter's SQL type.<p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to HSQLDB 1.7.2, the sqlType argument is ignored. <p>
     *
     * </span>
     * @param paramIndex the first parameter is 1, the second is 2, ...
     * @param sqlType the SQL type code defined in <code>java.sql.Types</code>
     * @exception SQLException if a database access error occurs
     */
    public void setNull(int paramIndex, int sqlType) throws SQLException {
        setParameter(paramIndex, null);
    }

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given Java <code>boolean</code>
     * value.  The driver converts this to an SQL <code>BIT</code> value
     * when it sends it to the database.<p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     */
    public void setBoolean(int parameterIndex,
                           boolean x) throws SQLException {

        Boolean b = x ? Boolean.TRUE
                      : Boolean.FALSE;

        setParameter(parameterIndex, b);
    }

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given Java <code>byte</code> value.
     * The driver converts this to an SQL <code>TINYINT</code> value when
     * it sends it to the database.<p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     */
    public void setByte(int parameterIndex, byte x) throws SQLException {
        setIntParameter(parameterIndex, x);
    }

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given Java <code>short</code>
     * value. The driver converts this to an SQL <code>SMALLINT</code>
     * value when it sends it to the database.<p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     */
    public void setShort(int parameterIndex, short x) throws SQLException {
        setIntParameter(parameterIndex, x);
    }

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given Java <code>int</code> value.
     * The driver converts this to an SQL <code>INTEGER</code> value when
     * it sends it to the database.<p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     */
    public void setInt(int parameterIndex, int x) throws SQLException {
        setIntParameter(parameterIndex, x);
    }

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given Java <code>long</code> value.
     * The driver converts this to an SQL <code>BIGINT</code> value when
     * it sends it to the database.<p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     */
    public void setLong(int parameterIndex, long x) throws SQLException {
        setLongParameter(parameterIndex, x);
    }

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given Java <code>float</code> value.
     * The driver converts this to an SQL <code>FLOAT</code> value when
     * it sends it to the database.<p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * With 1.7.1 and greater, HSQLDB handleS Java positive/negative Infinity or
     * NaN <code>float</code> values properly. These values are sent to the
     * database and stored there. <p>
     *
     * </span>
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     */
    public void setFloat(int parameterIndex, float x) throws SQLException {
        setDouble(parameterIndex, (double) x);
    }

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given Java <code>double</code> value.
     * The driver converts this to an SQL <code>DOUBLE</code> value when it
     * sends it to the database.<p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * With 1.7.1 and greater, HSQLDB handleS Java positive/negative Infinity or
     * NaN <code>float</code> values properly. These values are sent to the
     * database and stored there. <p>
     *
     * </span>
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     */
    public void setDouble(int parameterIndex, double x) throws SQLException {

        Double d = ValuePool.getDouble(Double.doubleToLongBits(x));

        setParameter(parameterIndex, d);
    }

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given
     * <code>java.math.BigDecimal</code> value.
     * The driver converts this to an SQL <code>NUMERIC</code> value when
     * it sends it to the database.<p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     */
    public void setBigDecimal(int parameterIndex,
                              BigDecimal x) throws SQLException {

        BigDecimal bd = ValuePool.getBigDecimal(x);

        setParameter(parameterIndex, bd);
    }

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given Java <code>String</code> value.
     * The driver converts this
     * to an SQL <code>VARCHAR</code> or <code>LONGVARCHAR</code> value
     * (depending on the argument's
     * size relative to the driver's limits on <code>VARCHAR</code> values)
     * when it sends it to the database.<p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB 1.7.2 stores all XXXCHAR values as
     * java.lang.String objects, so there is no appreciable difference between
     * VARCHAR and LONGVARCHAR.<p>
     *
     * </span>
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     */
    public void setString(int parameterIndex, String x) throws SQLException {

        String s = ValuePool.getString(x);

        setParameter(parameterIndex, s);
    }

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given Java array of bytes.
     * The driver converts this to an SQL <code>VARBINARY</code> or
     * <code>LONGVARBINARY</code> (depending on the argument's size relative
     * to the driver's limits on <code>VARBINARY</code> values) when it
     * sends it to the database.<p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB stores all XXXBINARY values
     * the same way and there is no appreciable difference between
     * VARBINARY and LONGVARBINARY.<p>
     *
     * </span>
     * @param paramIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     */
    public void setBytes(int paramIndex, byte[] x) throws SQLException {
        setParameter(paramIndex, x);
    }

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given
     * <code>java.sql.Date</code> value.  The driver converts this
     * to an SQL <code>DATE</code> value when it sends it to the database.<p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     */
    public void setDate(int parameterIndex,
                        java.sql.Date x) throws SQLException {
        setParameter(parameterIndex, x);
    }

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given <code>java.sql.Time</code>
     * value. The driver converts this to an SQL <code>TIME</code> value when it
     * sends it to the database.<p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     */
    public void setTime(int parameterIndex,
                        java.sql.Time x) throws SQLException {
        setParameter(parameterIndex, x);
    }

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given
     * <code>java.sql.Timestamp</code> value.  The driver converts this to
     * an SQL <code>TIMESTAMP</code> value when it sends it to the
     * database.<p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     */
    public void setTimestamp(int parameterIndex,
                             java.sql.Timestamp x) throws SQLException {
        setParameter(parameterIndex, x);
    }

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given input stream, which will have
     * the specified number of bytes.
     * When a very large ASCII value is input to a <code>LONGVARCHAR</code>
     * parameter, it may be more practical to send it via a
     * <code>java.io.InputStream</code>. Data will be read from the stream
     * as needed until end-of-file is reached.  The JDBC driver will
     * do any necessary conversion from ASCII to the database char format. <p>
     *
     * <b>Note:</b> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface.<p>
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * This method uses the default platform character encoding to convert bytes
     * from the stream into the characters of a String. In the future this is
     * likely to change to always treat the stream as ASCII.<p>
     *
     * Before HSQLDB 1.7.0, <code>setAsciiStream</code> and
     * <code>setUnicodeStream</code> were identical. <p>
     *
     * </span>
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the Java input stream that contains the ASCII parameter value
     * @param length the number of bytes in the stream
     * @exception SQLException if a database access error occurs
     */
    public void setAsciiStream(int parameterIndex, java.io.InputStream x,
                               int length) throws SQLException {

        checkSetParameterIndex(parameterIndex);

        String s;

        if (x == null) {
            s = "input stream is null";

            throw jdbcDriver.sqlException(Trace.INVALID_JDBC_ARGUMENT, s);
        }

        s = null;    // else compiler complains

        try {
            s = StringConverter.inputStreamToString(x);
        } catch (IOException e) {
            throw jdbcDriver.sqlException(Trace.INVALID_CHARACTER_ENCODING);
        }

        setParameter(parameterIndex, s);
    }

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given input stream, which
     * will have the specified number of bytes. A Unicode character has
     * two bytes, with the first byte being the high byte, and the second
     * being the low byte.
     *
     * When a very large Unicode value is input to a <code>LONGVARCHAR</code>
     * parameter, it may be more practical to send it via a
     * <code>java.io.InputStream</code> object. The data will be read from the
     * stream as needed until end-of-file is reached.  The JDBC driver will
     * do any necessary conversion from Unicode to the database char format.
     *
     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface.<p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Beginning with HSQLDB 1.7.0, this method complies with behavior as
     * defined by the JDBC3 specification. <p>
     *
     * </span>
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x a <code>java.io.InputStream</code> object that contains the
     *      Unicode parameter value as two-byte Unicode characters
     * @param length the number of bytes in the stream
     * @exception SQLException if a database access error occurs
     * @deprecated Sun does not include a reason, but presumably
     *      this is because setCharacterStream is now prefered
     */
    public void setUnicodeStream(int parameterIndex, java.io.InputStream x,
                                 int length) throws SQLException {

        checkSetParameterIndex(parameterIndex);

        if (x == null) {
            String msg = "input stream is null";

            throw jdbcDriver.sqlException(Trace.INVALID_JDBC_ARGUMENT, msg);
        }

// NOTE:
//
// No longer using StringBuffer, as chlen may end up exceeding
// chread.  The new way ensures that the slack is taken up so
// that no larger a character array than necessary is ever
// made internal to the database.  On the down side, this requires
// up to twice the intermediate memory, as chars is copied, whereas
// the undocumented behavior of String is to share the array with
// the StringBuffer from which it was created, until such time,
// if any, that the StringBuffer is later modified.
// CHECKME:
//
// what about when length is odd?
        int    chlen  = length / 2;
        int    chread = 0;
        char[] chars  = new char[chlen];
        int    hi;
        int    lo;
        String s;

        try {
            for (; chread < chlen; chread++) {
                hi = x.read();

                if (hi == -1) {
                    break;
                }

                lo = x.read();

                if (lo == -1) {
                    break;
                }

                chars[chread] = (char) (hi << 8 | lo);
            }
        } catch (IOException e) {
            throw jdbcDriver.sqlException(Trace.TRANSFER_CORRUPTED);
        }

        s = new String(chars, 0, chread);

        setParameter(parameterIndex, s);
    }

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given input stream, which will have
     * the specified number of bytes.
     * When a very large binary value is input to a <code>LONGVARBINARY</code>
     * parameter, it may be more practical to send it via a
     * <code>java.io.InputStream</code> object. The data will be read from the
     * stream as needed until end-of-file is reached.
     *
     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface.<p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Starting with 1.7.2, this method works according to the standard. <p>
     *
     * </span>
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the java input stream which contains the binary parameter value
     * @param length the number of bytes in the stream
     * @exception SQLException if a database access error occurs
     */
    public void setBinaryStream(int parameterIndex, java.io.InputStream x,
                                int length) throws SQLException {

        checkSetParameterIndex(parameterIndex);

        if (x == null) {
            String msg = "input stream is null";

            throw jdbcDriver.sqlException(Trace.INVALID_JDBC_ARGUMENT, msg);
        }

        byte b[] = new byte[length];
        int  bytesread;

        try {
            bytesread = x.read(b, 0, length);
        } catch (IOException e) {
            throw jdbcDriver.sqlException(Trace.INPUTSTREAM_ERROR,
                                          e.getMessage());
        }

        if (bytesread < length) {
            byte[] old = b;

            b = new byte[bytesread];

            System.arraycopy(old, 0, b, 0, bytesread);
        }

        setParameter(parameterIndex, b);
    }

    /**
     * <!-- start generic documentation -->
     * Clears the current parameter values immediately. <p>
     *
     * In general, parameter values remain in force for repeated use of a
     * statement. Setting a parameter value automatically clears its
     * previous value.  However, in some cases it is useful to immediately
     * release the resources used by the current parameter values; this can
     * be done by calling the method <code>clearParameters</code>.<p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     *
     * @exception SQLException if a database access error occurs
     */
    public void clearParameters() throws SQLException {
        checkClosed();
        org.hsqldb.lib.ArrayUtil.fillArray(parameterValues, null);
    }

    //----------------------------------------------------------------------
    // Advanced features:

    /**
     * <!-- start generic documentation -->
     * Sets the value of the designated parameter with the given object. <p>
     *
     * The second argument must be an object type; for integral values, the
     * <code>java.lang</code> equivalent objects should be used. <p>
     *
     * The given Java object will be converted to the given targetSqlType
     * before being sent to the database.
     *
     * If the object has a custom mapping (is of a class implementing the
     * interface <code>SQLData</code>),
     * the JDBC driver should call the method <code>SQLData.writeSQL</code> to
     * write it to the SQL data stream.
     * If, on the other hand, the object is of a class implementing
     * <code>Ref</code>, <code>Blob</code>, <code>Clob</code>,
     * <code>Struct</code>, or <code>Array</code>, the driver should pass it
     * to the database as a value of the corresponding SQL type. <p>
     *
     * Note that this method may be used to pass database-specific
     * abstract data types.<p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including HSQLDB 1.7.1, calling this method is identical to
     * {@link #setObject(int, Object, int) setObject(int, Object, int)}.
     * That is, this method simply calls setObject(int, Object, int),
     * ignoring the scale specification. <p>
     *
     * In 1.7.2, this method supports conversions listed in the conversion
     * table B-5 of the JDBC 3 specification. The scale argument is not
     * used.<p>
     *
     * </span>
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the object containing the input parameter value
     * @param targetSqlType the SQL type (as defined in java.sql.Types) to be
     * sent to the database. The scale argument may further qualify this type.
     * @param scale for java.sql.Types.DECIMAL or java.sql.Types.NUMERIC types,
     *     this is the number of digits after the decimal point.  For all
     *     other types, this value will be ignored. <p>
     *
     *     Up to and including HSQLDB 1.7.0, this parameter is ignored.
     * @exception SQLException if a database access error occurs
     * @see java.sql.Types
     * @see #setObject(int,Object,int)
     */
    public void setObject(int parameterIndex, Object x, int targetSqlType,
                          int scale) throws SQLException {

        /** @todo fredt - implement SQLData support */
        setObject(parameterIndex, x);
    }

    /**
     * <!-- start generic documentation -->
     * Sets the value of the designated parameter with the given object.
     * This method is like the method <code>setObject</code>
     * above, except that it assumes a scale of zero. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * In 1.7.2, this method supports conversions listed in the conversion
     * table B-5 of the JDBC 3 specification.<p>
     *
     * </span>
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the object containing the input parameter value
     * @param targetSqlType the SQL type (as defined in java.sql.Types) to be
     *                sent to the database
     * @exception SQLException if a database access error occurs
     * @see #setObject(int,Object)
     */
    public void setObject(int parameterIndex, Object x,
                          int targetSqlType) throws SQLException {
        setObject(parameterIndex, x);
    }

    /**
     * <!-- start generic documentation -->
     * Sets the value of the designated parameter using the given object. <p>
     *
     * The second parameter must be of type <code>Object</code>; therefore,
     * the <code>java.lang</code> equivalent objects should be used for
     * built-in types. <p>
     *
     * The JDBC specification specifies a standard mapping from
     * Java <code>Object</code> types to SQL types.  The given argument
     * will be converted to the corresponding SQL type before being
     * sent to the database. <p>
     *
     * Note that this method may be used to pass datatabase-
     * specific abstract data types, by using a driver-specific Java
     * type.  If the object is of a class implementing the interface
     * <code>SQLData</code>, the JDBC driver should call the method
     * <code>SQLData.writeSQL</code> to write it to the SQL data stream.
     * If, on the other hand, the object is of a class implementing
     * <code>Ref</code>, <code>Blob</code>, <code>Clob</code>,
     * <code>Struct</code>, or <code>Array</code>, the driver should pass
     * it to the database as a value of the corresponding SQL type. <p>
     *
     * This method throws an exception if there is an ambiguity, for
     * example, if the object is of a class implementing more than one
     * of the interfaces named above.<p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b><p>
     *
     * In 1.7.2, this method supports conversions listed in the conversion
     * table B-5 of the JDBC 3 specification.<p>
     *
     * </span>
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the object containing the input parameter value
     * @exception SQLException if a database access error occurs or the type
     *      of the given object is ambiguous
     */
    public void setObject(int parameterIndex, Object x) throws SQLException {
        setParameter(parameterIndex, x);
    }

    //--------------------------JDBC 2.0-----------------------------

    /**
     * <!-- start generic documentation -->
     * Adds a set of parameters to this <code>PreparedStatement</code>
     * object's batch of commands. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Since HSQLDB 1.7.2, this feature is supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @exception SQLException if a database access error occurs
     * @see jdbcStatement#addBatch
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcPreparedStatement)
     */
// boucherb@users 20030801 - method implemented
    public void addBatch() throws SQLException {

        checkClosed();
        checkAddBatch();

        int      len      = parameterValues.length;
        Object[] bpValues = new Object[len];

        System.arraycopy(parameterValues, 0, bpValues, 0, len);
        batchResultOut.add(bpValues);
    }

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
     * HSQLDB stores CHARACTER and related SQL types as Unicode so
     * this method does not perform any conversion.<p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param reader the <code>java.io.Reader</code> object that contains the
     * Unicode data
     * @param length the number of characters in the stream
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcPreparedStatement)
     */

// fredt@users 20020429 - patch 1.7.0 - method defined
// fredt@users 20020627 - patch 574234 by ohioedge@users
// boucherb@users 20030801 - patch 1.7.2 - updated
    public void setCharacterStream(int parameterIndex, java.io.Reader reader,
                                   int length) throws SQLException {

        checkSetParameterIndex(parameterIndex);

        if (reader == null) {
            String msg = "reader is null";

            throw jdbcDriver.sqlException(Trace.INVALID_JDBC_ARGUMENT, msg);
        }

        char[] buffer = new char[length];
        int    chread;

        try {
            chread = reader.read(buffer);

            if (chread == -1) {
                throw new IOException(
                    Trace.getMessage(
                        Trace.jdbcPreparedStatement_setCharacterStream));
            }
        } catch (IOException e) {
            throw jdbcDriver.sqlException(Trace.TRANSFER_CORRUPTED,
                                          e.toString());
        }

        setParameter(parameterIndex, new String(buffer, 0, chread));
    }

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given
     * <code>REF(&lt;structured-type&gt;)</code> value.
     * The driver converts this to an SQL <code>REF</code> value when it
     * sends it to the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.2 does not support the SQL REF type. Calling this method
     * throws an exception.
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param i the first parameter is 1, the second is 2, ...
     * @param x an SQL <code>REF</code> value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcPreparedStatement)
     */
    public void setRef(int i, Ref x) throws SQLException {
        throw jdbcDriver.notSupported;
    }

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given <code>Blob</code> object.
     * The driver converts this to an SQL <code>BLOB</code> value when it
     * sends it to the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including HSQLDB 1.7.1, this feature is not supported. <p>
     *
     * Starting with 1.7.2, setBlob is supported for Blob objects of length
     * less than or equal to Integer.MAX_VALUE.  In 1.7.2, setBlob(i,x) is
     * roughly equivalent (null and length handling not shown) to:
     *
     * <pre>
     * setBinaryStream(i, x.getBinaryStream(), (int) x.length());
     * </pre>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param i the first parameter is 1, the second is 2, ...
     * @param x a <code>Blob</code> object that maps an SQL <code>BLOB</code>
     *     value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcPreparedStatement)
     */

// boucherb@users 20030801 - method implemented
    public void setBlob(int i, Blob x) throws SQLException {

        checkSetParameterIndex(i);

        if (x == null) {
            setParameter(i, null);

            return;
        }

        long   length;
        String msg;

        length = x.length();

        if (length > Integer.MAX_VALUE) {
            msg = "Maximum Blob input octet length exceeded: " + length;

            throw jdbcDriver.sqlException(Trace.INPUTSTREAM_ERROR, msg);
        }

        int  len = (int) length;
        byte b[] = new byte[len];

        try {
            len = x.getBinaryStream().read(b, 0, len);
        } catch (IOException e) {
            throw jdbcDriver.sqlException(Trace.INPUTSTREAM_ERROR,
                                          e.getMessage());
        }

        if (len < length) {
            byte[] old = b;

            b = new byte[len];

            System.arraycopy(old, 0, b, 0, len);
        }

        setParameter(i, b);
    }

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given <code>Clob</code> object.
     * The driver converts this to an SQL <code>CLOB</code> value when it
     * sends it to the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including HSQLDB 1.7.1, this feature was not supported. <p>
     *
     * Starting with 1.7.2, setClob is supported for Clob objects of length
     * less than or equal to Integer.MAX_VALUE.  In 1.7.2, setClob(i,x) is
     * rougly equivalent (null and length handling not shown) to: <p>
     *
     * <pre>
     * setCharacterStream(i, x.getCharacterStream(), (int) x.length());
     * </pre>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param i the first parameter is 1, the second is 2, ...
     * @param x a <code>Clob</code> object that maps an SQL <code>CLOB</code>
     *      value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcPreparedStatement)
     */

// boucherb@users 20030801 - method implemented
    public void setClob(int i, Clob x) throws SQLException {

        checkSetParameterIndex(i);

        if (x == null) {
            setParameter(i, null);

            return;
        }

        long   length;
        String msg;

        length = x.length();

        if (length > Integer.MAX_VALUE) {
            msg = "Maximum Clob input character length exceeded: " + length;

            throw jdbcDriver.sqlException(Trace.INPUTSTREAM_ERROR, msg);
        }

        char[] buffer = new char[(int) length];
        int    chread;

        try {
            chread = x.getCharacterStream().read(buffer);

            if (chread == -1) {
                throw new IOException(
                    Trace.getMessage(Trace.jdbcPreparedStatement_setClob));
            }
        } catch (IOException e) {
            throw jdbcDriver.sqlException(Trace.TRANSFER_CORRUPTED,
                                          e.toString());
        }

        setParameter(i, new String(buffer, 0, chread));
    }

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given <code>Array</code> object.
     * The driver converts this to an SQL <code>ARRAY</code> value when it
     * sends it to the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.2 does not support the SQL ARRAY type. Calling this method
     * throws an exception.
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param i the first parameter is 1, the second is 2, ...
     * @param x an <code>Array</code> object that maps an SQL <code>ARRAY</code>
     *       value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcPreparedStatement)
     */
    public void setArray(int i, Array x) throws SQLException {
        throw jdbcDriver.notSupported;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves a <code>ResultSetMetaData</code> object that contains
     * information about the columns of the <code>ResultSet</code> object
     * that will be returned when this <code>PreparedStatement</code> object
     * is executed.
     * <P>
     * Because a <code>PreparedStatement</code> object is precompiled, it is
     * possible to know about the <code>ResultSet</code> object that it will
     * return without having to execute it.  Consequently, it is possible
     * to invoke the method <code>getMetaData</code> on a
     * <code>PreparedStatement</code> object rather than waiting to execute
     * it and then invoking the <code>ResultSet.getMetaData</code> method
     * on the <code>ResultSet</code> object that is returned.
     * <P>
     * <B>NOTE:</B> Using this method may be expensive for some drivers due
     * to the lack of underlying DBMS support. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Starting with HSQLDB 1.7.2, this feature is supported.  If the statement
     * generates an update count, then a ResultSetMetaData object is retrieved
     * whose column count is zero.
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @return the description of a <code>ResultSet</code> object's columns or
     *    <code>null</code> if the driver cannot return a
     *    <code>ResultSetMetaData</code> object
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcPreparedStatement)
     */

// boucherb@users 20030801 - method implemented
    public ResultSetMetaData getMetaData() throws SQLException {

        checkClosed();

// CHECKME:
// Is this correct?  Or is it supposed to be a zero-column rsmd object?
        if (isRowCount) {
            return null;
        }

        if (rsmd == null) {
            rsmd = new jdbcResultSetMetaData(rsmdDescriptor,
                                             connection.connProperties);
        }

        return rsmd;
    }

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given <code>java.sql.Date</code>
     * value, using the given <code>Calendar</code> object.  The driver uses
     * the <code>Calendar</code> object to construct an SQL <code>DATE</code>
     * value,which the driver then sends to the database.  With a
     * a <code>Calendar</code> object, the driver can calculate the date
     * taking into account a custom timezone.  If no
     * <code>Calendar</code> object is specified, the driver uses the default
     * timezone, which is that of the virtual machine running the
     * application. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value
     * @param cal the <code>Calendar</code> object the driver will use
     *       to construct the date
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcPreparedStatement)
     */

// fredt@users 20020414 - patch 517028 by peterhudson@users - method defined
// changes by fredt - moved conversion to HsqlDateTime
    public void setDate(int parameterIndex, java.sql.Date x,
                        Calendar cal) throws SQLException {

// CHECKME:  What happens if the client specifies a null Calendar object?
//           If this happens, do we properly use the default
//           timezone, which is that of the virtual machine running the
//           application?
//
//        if (cal == null) {
//            cal = ??? java.util.Calendar.getInstance();
//        }
        String s;

        if (x == null) {
            s = null;
        } else {
            try {
                s = HsqlDateTime.getDateString(x, cal);
            } catch (Exception e) {
                throw jdbcDriver.sqlException(Trace.INVALID_ESCAPE,
                                              e.getMessage());
            }
        }

        setParameter(parameterIndex, s);
    }

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
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value
     * @param cal the <code>Calendar</code> object the driver will use
     *       to construct the time
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcPreparedStatement)
     */

// fredt@users 20020414 - patch 517028 by peterhudson@users - method defined
// changes by fredt - moved conversion to HsqlDateTime
    public void setTime(int parameterIndex, java.sql.Time x,
                        Calendar cal) throws SQLException {

// CHECKME:  What happens if the client specifies a null Calendar object?
//           If this happens, do we properly use the default
//           timezone, which is that of the virtual machine running the
//           application?
//
//        if (cal == null) {
//            cal = ??? java.util.Calendar.getInstance();
//        }
        String s;

        if (x == null) {
            s = null;
        } else {
            try {
                s = HsqlDateTime.getTimeString(x, cal);
            } catch (Exception e) {
                throw jdbcDriver.sqlException(Trace.INVALID_ESCAPE,
                                              e.getMessage());
            }
        }

        setParameter(parameterIndex, s);
    }

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given <code>java.sql.Timestamp</code>
     * value, using the given <code>Calendar</code> object.  The driver uses
     * the <code>Calendar</code> object to construct an SQL <code>TIMESTAMP</code>
     * value, which the driver then sends to the database.  With a
     * <code>Calendar</code> object, the driver can calculate the timestamp
     * taking into account a custom timezone.  If no
     * <code>Calendar</code> object is specified, the driver uses the default
     * timezone, which is that of the virtual machine running the application. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * </span>
     * <!-- end release-specific documentation -->
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value
     * @param cal the <code>Calendar</code> object the driver will use
     *       to construct the timestamp
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcPreparedStatement)
     */

// fredt@users 20020414 - patch 517028 by peterhudson@users - method defined
// changes by fredt - moved conversion to HsqlDateTime
    public void setTimestamp(int parameterIndex, java.sql.Timestamp x,
                             Calendar cal) throws SQLException {

        checkSetParameterIndex(parameterIndex);

// CHECKME:  What happens if the client specifies a null Calendar object?
//           If this happens, do we properly use the default
//           timezone, which is that of the virtual machine running the
//           application?
//
//        if (cal == null) {
//            cal = ??? java.util.Calendar.getInstance();
//        }
        String s;

        if (x == null) {
            s = null;
        } else {
            try {
                s = HsqlDateTime.getTimestampString(x, cal);
            } catch (Exception e) {
                throw jdbcDriver.sqlException(Trace.INVALID_ESCAPE,
                                              e.getMessage());
            }
        }

        setParameter(parameterIndex, s);
    }

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to SQL <code>NULL</code>.
     * This version of the method <code>setNull</code> should
     * be used for user-defined types and REF type parameters.  Examples
     * of user-defined types include: STRUCT, DISTINCT, JAVA_OBJECT, and
     * named array types.
     *
     * <P><B>Note:</B> To be portable, applications must give the
     * SQL type code and the fully-qualified SQL type name when specifying
     * a NULL user-defined or REF parameter.  In the case of a user-defined
     * type the name is the type name of the parameter itself.  For a REF
     * parameter, the name is the type name of the referenced type.  If
     * a JDBC driver does not need the type code or type name information,
     * it may ignore it.
     *
     * Although it is intended for user-defined and Ref parameters,
     * this method may be used to set a null parameter of any JDBC type.
     * If the parameter does not have a user-defined or REF type, the given
     * typeName is ignored. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB ignores the typeName argument. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param paramIndex the first parameter is 1, the second is 2, ...
     * @param sqlType a value from <code>java.sql.Types</code>
     * @param typeName the fully-qualified name of an SQL user-defined type;
     * ignored if the parameter is not a user-defined type or REF
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcPreparedStatement)
     */
    public void setNull(int paramIndex, int sqlType,
                        String typeName) throws SQLException {
        setParameter(paramIndex, null);
    }

    //------------------------- JDBC 3.0 -----------------------------------

    /**
     * <!-- start generic documentation -->
     * Sets the designated parameter to the given <code>java.net.URL</code>
     * value. The driver converts this to an SQL <code>DATALINK</code> value
     * when it sends it to the database. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.2 does not support DATALINK SQL type for which this method
     * is intended. Calling this method throws an exception.
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the <code>java.net.URL</code> object to be set
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
    public void setURL(int parameterIndex,
                       java.net.URL x) throws SQLException {
        throw jdbcDriver.notSupported;
    }

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the number, types and properties of this
     * <code>PreparedStatement</code> object's parameters. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Starting with HSQLDB 1.7.2, this feature is supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @return a <code>ParameterMetaData</code> object that contains information
     *    about the number, types and properties of this
     *    <code>PreparedStatement</code> object's parameters
     * @exception SQLException if a database access error occurs
     * @see java.sql.ParameterMetaData
     * @since JDK 1.4, HSQL 1.7.0
     */
// boucherb@users 20030801 - method implemented
//#ifdef JDBC3
    public ParameterMetaData getParameterMetaData() throws SQLException {

        checkClosed();

        if (pmd == null) {
            pmd = new jdbcParameterMetaData(pmdDescriptor);
        }

        // NOTE:  pmd is declared as Object to avoid yet another #ifdef.
        return (ParameterMetaData) pmd;
    }

//#endif JDBC3
    //-------------------- Internal Implementation -----------------------------

    /**
     * Constructs a statement that produces results of the requested
     * <code>type</code>.<br>
     *
     * A prepared statement must be a single SQL statement.<br>
     *
     * @param c the Connection used execute this statement
     * @param sql the SQL statement this object represents
     * @param type the type of result this statement will produce
     * @throws HsqlException if the statement is not accepted by the database
     * @throws SQLException if preprocessing by driver fails
     */
    jdbcPreparedStatement(jdbcConnection c, String sql,
                          int type) throws HsqlException, SQLException {

        super(c, type);

        Iterator i;
        Result   in;
        Result   modesResult;
        Object[] row;

        sql                   = c.nativeSQL(sql);
        compiledStatementType = guessCompiledStatementType(sql);

        // If true, then its either DDL or its invalid.
        // In either case, handle it like we are a plain old jdbcStatement
        if (compiledStatementType == CompiledStatement.UNKNOWN) {

            // Presently, only SELECT and CALL generate result sets
            isRowCount = true;

            // Presently, DDL statements cannot take parameters
            parameterTypes  = parameterModes = new int[0];
            parameterValues = new Object[0];

            // for toString()
            this.sql = sql;

            return;
        }

        resultOut.setResultType(ResultConstants.SQLPREPARE);
        resultOut.setMainString(sql);
        resultOut.setStatementType(compiledStatementType);

        in = connection.sessionProxy.execute(resultOut);

        if (in.iMode == ResultConstants.ERROR) {
            jdbcDriver.throwError(in);
        }

        // else it's a MULTI result encapsulating three sub results:
        // 1.) a PREPARE_ACK
        //
        //     Indicates the statement id to be communicated in SQLEXECUTE
        //     requests to allow the engine to find the corresponding
        //     CompiledStatement object, parameterize and execute it.
        //
        // 2.) a description of the statement's result set metadata
        //
        //     This is communicated in the same way as for result sets. That is,
        //     the metadata arrays of Result, such as colTypes, are used in the
        //     "conventional" fashion.  With some work, it may be possible
        //     to speed up internal execution of prepared statements by
        //     dispensing with generating most rsmd values while generating
        //     the result, safe in the knowlege that the client already
        //     has a copy of the rsmd.  In general, only the colTypes array
        //     must be available at the engine, and only for network
        //     communications so that the row output and row input
        //     interfaces can do their work.  One caveat is that the
        //     columnDisplaySize values are not accurate, as we do
        //     not consistently enforce column size yet and instead
        //     approximate the value when a result with actual data is
        //     retrieved
        //
        // 3.) a description of the statement's parameter metadata
        //
        //     This is communicated in a similar fashion to 2.), but has
        //     a slighly different layout to allow the parameter modes
        //     to be transmitted.  The values of this object are used
        //     to set up the parameter management of this class.  The
        //     object is also used to construct the jdbcParameterMetaData
        //     object later, if requested.  That is, it holds information
        //     additional to that used by this class, so it should not be
        //     altered or disposed of
        //
        //  (boucherb@users)
        i = in.iterator();

        try {

            // PREPARE_ACK
            row         = (Object[]) i.next();
            statementID = ((Result) row[0]).getStatementID();

            // DATA - isParameterDescription == false
            row            = (Object[]) i.next();
            rsmdDescriptor = (Result) row[0];
            isRowCount = rsmdDescriptor.iMode == ResultConstants.UPDATECOUNT;

            // DATA - isParameterDescription == true
            row             = (Object[]) i.next();
            pmdDescriptor   = (Result) row[0];
            parameterTypes  = pmdDescriptor.metaData.getParameterTypes();
            parameterValues = new Object[parameterTypes.length];
            parameterModes  = pmdDescriptor.metaData.paramMode;
        } catch (Exception e) {
            throw Trace.error(Trace.GENERAL_ERROR, e.toString());
        }

        resultOut = new Result(ResultConstants.SQLEXECUTE, parameterTypes,
                               statementID);
        batchResultOut = new Result(ResultConstants.BATCHEXECUTE,
                                    parameterTypes, statementID);

        // for toString()
        this.sql = sql;
    }

    /**
     * Checks if execution does or does not generate a single row
     * update count, throwing if the argument, yes, does not match. <p>
     *
     * @param yes if true, check that execution generates a single
     *      row update count, else check that execution generates
     *      something other than a single row update count.
     * @throws SQLException if the argument, yes, does not match
     */
    protected void checkIsRowCount(boolean yes) throws SQLException {

        if (yes != isRowCount) {
            int msg = yes ? Trace.JDBC_STATEMENT_NOT_ROW_COUNT
                          : Trace.JDBC_STATEMENT_NOT_RESULTSET;

            throw jdbcDriver.sqlException(msg);
        }
    }

    /**
     * Checks if the specified parameter index value is valid in terms of
     * setting an IN or IN OUT parameter value. <p>
     *
     * @param i The parameter index to check
     * @throws SQLException if the specified parameter index is invalid
     */
    protected void checkSetParameterIndex(int i) throws SQLException {

        int    mode;
        String msg;

        checkClosed();

        if (i < 1 || i > parameterValues.length) {
            msg = "parameter index out of range: " + i;

            throw jdbcDriver.sqlException(Trace.INVALID_JDBC_ARGUMENT, msg);
        }

        mode = parameterModes[i - 1];

        switch (mode) {

            default :
                msg = "Not IN or IN OUT mode: " + mode + " for parameter: "
                      + i;

                throw jdbcDriver.sqlException(Trace.INVALID_JDBC_ARGUMENT,
                                              msg);
            case Expression.PARAM_IN :
            case Expression.PARAM_IN_OUT :
                break;
        }
    }

    /**
     * The internal parameter value setter always converts the parameter to
     * the Java type required for data transmission. Target BINARY and OTHER
     * types are converted directly. All other target types are converted
     * by Column.convertObject(). This also normalizes DATETIME values.
     *
     * @param i parameter index
     * @param o object
     * @throws SQLException if either argument is not acceptable.
     */
    private void setParameter(int i, Object o) throws SQLException {

        checkSetParameterIndex(i);

        i--;

        if (o == null) {
            parameterValues[i] = null;

            return;
        }

        int outType = parameterTypes[i];

        try {
            if (outType == Types.OTHER) {
                o = new JavaObject(o);
            } else if (outType == Types.BINARY) {
                if (!(o instanceof byte[])) {
                    throw jdbcDriver.sqlException(
                        Trace.error(Trace.INVALID_CONVERSION));
                }

                o = new Binary((byte[]) o);
            } else {
                o = Column.convertObject(o, outType);
            }
        } catch (HsqlException e) {
            jdbcDriver.throwError(e);
        }

        parameterValues[i] = o;
    }

    /**
     * Used with int and narrower integral primitives
     * @param i parameter index
     * @param value object to set
     * @throws SQLException if either argument is not acceptable
     */
    private void setIntParameter(int i, int value) throws SQLException {

        checkSetParameterIndex(i);

        int outType = parameterTypes[i - 1];

        switch (outType) {

            case Types.TINYINT :
            case Types.SMALLINT :
            case Types.INTEGER :
                Object o = ValuePool.getInt(value);

                parameterValues[i - 1] = o;
                break;

            default :
                setLongParameter(i, value);
        }
    }

    /**
     * Used with long and narrower integral primitives. Conversion to BINARY
     * or OTHER types will throw here and not passed to setParameter().
     *
     * @param i parameter index
     * @param value object to set
     * @throws SQLException if either argument is not acceptable
     */
    private void setLongParameter(int i, long value) throws SQLException {

        checkSetParameterIndex(i);

        int outType = parameterTypes[i - 1];

        switch (outType) {

            case Types.BIGINT :
                Object o = ValuePool.getLong(value);

                parameterValues[i - 1] = o;
                break;

            case Types.BINARY :
            case Types.OTHER :
                throw jdbcDriver.sqlException(
                    Trace.error(Trace.INVALID_CONVERSION));
            default :
                setParameter(i, new Long(value));
        }
    }

    /**
     * This method should always throw if called for a PreparedStatement or
     * CallableStatment.
     *
     * @param sql ignored
     * @throws SQLException always
     */
    public void addBatch(String sql) throws java.sql.SQLException {
        throw jdbcDriver.notSupported;
    }

    /**
     * This method should always throw if called for a PreparedStatement or
     * CallableStatment.
     *
     * @param sql ignored
     * @throws SQLException always
     * @return nothing
     */
    public ResultSet executeQuery(String sql) throws java.sql.SQLException {
        throw jdbcDriver.notSupported;
    }

    /**
     * This method should always throw if called for a PreparedStatement or
     * CallableStatment.
     *
     * @param sql ignored
     * @throws SQLException always
     * @return nothing
     */
    public boolean execute(String sql) throws java.sql.SQLException {
        throw jdbcDriver.notSupported;
    }

    /**
     * This method should always throw if called for a PreparedStatement or
     * CallableStatment.
     *
     * @param sql ignored
     * @throws SQLException always
     * @return nothing
     */
    public int executeUpdate(String sql) throws java.sql.SQLException {
        throw jdbcDriver.notSupported;
    }

    public void close() throws java.sql.SQLException {

        HsqlException he;

        if (isClosed()) {
            return;
        }

        he = null;

        try {

            // fredt - if this is called by Connection.close() then there's no
            // need to free the prepared statements on the server - it is done
            // by Connection.close()
            if (!connection.isClosed) {
                connection.sessionProxy.execute(
                    Result.newFreeStmtRequest(statementID));
            }
        } catch (HsqlException e) {
            he = e;
        }

        parameterValues = null;
        parameterTypes  = null;
        parameterModes  = null;
        rsmdDescriptor  = null;
        pmdDescriptor   = null;
        rsmd            = null;
        pmd             = null;

        super.close();

        if (he != null) {
            throw jdbcDriver.sqlException(he);
        }
    }

    /**
     * Retrieves a String representation of this object.  <p>
     *
     * The representation is of the form: <p>
     *
     * class-name@hash[sql=[char-sequence], parameters=[p1, ...pi, ...pn]] <p>
     *
     * p1, ...pi, ...pn are the String representations of the currently set
     * parameter values that will be used with the non-batch execution
     * methods. <p>
     *
     * @return a String representation of this object
     */
    public String toString() {

        StringBuffer sb = new StringBuffer();
        String       sql;
        Object[]     pv;

        sb.append(super.toString());

        sql = this.sql;
        pv  = parameterValues;

        if (sql == null || pv == null) {
            sb.append("[closed]");

            return sb.toString();
        }

        sb.append("[sql=[").append(sql).append("]");

        if (pv.length > 0) {
            sb.append(", parameters=[");

            for (int i = 0; i < pv.length; i++) {
                sb.append('[');
                sb.append(pv[i]);
                sb.append("], ");
            }

            sb.setLength(sb.length() - 2);
            sb.append(']');
        }

        sb.append(']');

        return sb.toString();
    }

    /**
     * Checks if this statement allows batch execution.  DDL statements,
     * for instance, do not make sense to batch as prepared statements. <p>
     *
     * @throws SQLException if this is not a batchable statement
     */
    protected void checkAddBatch() throws SQLException {

        if (compiledStatementType == CompiledStatement.UNKNOWN) {
            String msg =
                "prepared DDL statements do not support batch execution";

            throw jdbcDriver.sqlException(Trace.ASSERT_FAILED, msg);
        }
    }

    static int guessCompiledStatementType(String sql) throws SQLException {

        if (sql == null) {
            return CompiledStatement.UNKNOWN;
        }

        Tokenizer tokenizer = new Tokenizer(sql);
        int       token     = Token.UNKNOWN;

        try {
            token = Token.get(tokenizer.getString());
        } catch (HsqlException e) {
            jdbcDriver.throwError(e);
        }

        switch (token) {

            case Token.INSERT :
            case Token.UPDATE :
            case Token.DELETE :
                return CompiledStatement.DML;

            case Token.SELECT :
                return CompiledStatement.DQL;

            case Token.CALL :
                return CompiledStatement.CALL;

            // In the future, we can do a test for DDL as well,
            // for instance, pre-validating all statement preparation.
            // For now, this is a quick 'n dirty to allow execution of
            // DDL via the PreparedStatement internface implementation
            default :
                return CompiledStatement.UNKNOWN;
        }
    }
}
