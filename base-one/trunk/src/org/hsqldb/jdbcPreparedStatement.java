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
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.*;     // for Array, Blob, Clob, Ref
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;    // for Map
import java.util.Calendar;
import java.util.Vector;
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
// fredt@users 20030620 - patch 1.7.2 - core rewritten to support real
// prepared statements
// boucherb@users 20030801 - patch 1.7.2 - Support for batch execution,
// getMetaData and getParameterMetadata, restricted support for setArray,
// setBlob, setClob, setURL, bug fix for setting OTHER parameter values,
// more efficient method invocation prechecks and parameter conversion,
// simplifications, misc javadoc updates.

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
    protected int statementType;

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
// fredt@users 20020428 - patch 1.7.0 - method overrides the one in jdbcStatement
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
    public synchronized boolean execute() throws SQLException {

        checkClosed();

        resultIn = null;

        try {
            resultOut.setParameterData(parameterValues);

            resultIn = connection.sessionProxy.execute(resultOut);
        } catch (HsqlException e) {
            throw jdbcDriver.sqlException(e);
        }

        if (resultIn.iMode == ResultConstants.ERROR) {
            throw new SQLException(resultIn.getMainString(),
                                   resultIn.getSubString(),
                                   resultIn.getStatementID());
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
    public synchronized ResultSet executeQuery() throws SQLException {

        checkClosed();
        checkIsRowCount(false);

        resultIn = null;

        try {
            resultOut.setParameterData(parameterValues);

            resultIn = connection.sessionProxy.execute(resultOut);
        } catch (HsqlException e) {
            throw jdbcDriver.sqlException(e);
        }

        if (resultIn.iMode == ResultConstants.ERROR) {
            throw new SQLException(resultIn.getMainString(),
                                   resultIn.getSubString(),
                                   resultIn.getStatementID());
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
    public synchronized int executeUpdate() throws SQLException {

        checkClosed();
        checkIsRowCount(true);

        resultIn = null;

        try {
            resultOut.setParameterData(parameterValues);

            resultIn = connection.sessionProxy.execute(resultOut);
        } catch (HsqlException e) {
            throw jdbcDriver.sqlException(e);
        }

        if (resultIn.iMode == ResultConstants.ERROR) {
            throw new SQLException(resultIn.getMainString(),
                                   resultIn.getSubString(),
                                   resultIn.getStatementID());
        } else if (resultIn.iMode != ResultConstants.UPDATECOUNT) {
            String msg = "Expected but did not recieve a row count";

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
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param sqlType the SQL type code defined in <code>java.sql.Types</code>
     * @exception SQLException if a database access error occurs
     */
    public void setNull(int parameterIndex, int sqlType) throws SQLException {

        checkSetParameterIndex(parameterIndex);
        checkClosed();
        setParameter(parameterIndex, null, Types.NULL, false);
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

        checkSetParameterIndex(parameterIndex);
        checkClosed();
        setParameter(parameterIndex, x ? Boolean.TRUE
                                       : Boolean.FALSE, Types.BIT, false);
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

        checkSetParameterIndex(parameterIndex);
        checkClosed();
        setParameter(parameterIndex, ValuePool.getInt(x), Types.TINYINT,
                     false);
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

        checkSetParameterIndex(parameterIndex);
        checkClosed();
        setParameter(parameterIndex, ValuePool.getInt(x), Types.SMALLINT,
                     false);
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

        checkSetParameterIndex(parameterIndex);
        checkClosed();
        setParameter(parameterIndex, ValuePool.getInt(x), Types.INTEGER,
                     false);
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

        checkSetParameterIndex(parameterIndex);
        checkClosed();
        setParameter(parameterIndex, ValuePool.getLong(x), Types.BIGINT,
                     false);
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
     * Up to 1.6.1, HSQLDB did not handle Java positive/negative Infinity or
     * NaN <code>float</code> values properly. <p>
     *
     * With 1.7.0, these values are converted to SQL <code>NULL</code>. <p>
     *
     * With 1.7.1 and greater, these values are sent to the database. <p>
     *
     * </span>
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     */

// fredt@users 20020325 - patch 448691 NaN by fredt
// fredt@users 20021013 - patch 1.7.1 - NaN and infinity preserved
    public void setFloat(int parameterIndex, float x) throws SQLException {

        checkSetParameterIndex(parameterIndex);
        checkClosed();
        setParameter(parameterIndex,
                     ValuePool.getDouble(Double.doubleToLongBits((double) x)),
                     Types.DOUBLE, false);
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
     * Up to 1.6.1, HSQLDB did not handle Java positive/negative Infinity or
     * NaN <code>float</code> values properly.  <p>
     *
     * With 1.7.0, these values are converted to SQL <code>NULL</code>. <p>
     *
     * With 1.7.1 and greater, these values are sent to the database. <p>
     *
     * </span>
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     */

// fredt@users 20020325 - patch 448691 NaN by fredt
// fredt@users 20021013 - patch 1.7.1 - NaN and infinity preserved
    public void setDouble(int parameterIndex, double x) throws SQLException {

        checkSetParameterIndex(parameterIndex);
        checkClosed();
        setParameter(parameterIndex,
                     ValuePool.getDouble(Double.doubleToLongBits(x)),
                     Types.DOUBLE, false);
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

        checkSetParameterIndex(parameterIndex);
        checkClosed();
        setParameter(parameterIndex, ValuePool.getBigDecimal(x),
                     Types.DECIMAL,    // NUMERIC?
                     false);
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
     * Up to and including HSQLDB 1.7.2, the engine stores all XXXCHAR values as
     * java.lang.String objects, so there is no appreciable difference between
     * VARCHAR and LONGVARCHAR.  The driver does not (and does not need to) test
     * the argument's size, relative to limits on VARCHAR values. <p>
     *
     * </span>
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     */
    public void setString(int parameterIndex, String x) throws SQLException {

        checkSetParameterIndex(parameterIndex);
        checkClosed();
        setParameter(parameterIndex, ValuePool.getString(x),
                     Types.LONGVARCHAR, false);
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
     * Up to and including HSQLDB 1.7.2, the engine stores all XXXBINARY values
     * the same way and there is no appreciable difference between
     * VARBINARY and LONGVARBINARY.  The driver does not (and does not need to)
     * test the argument's size, relative to limits on VARBINARY values. <p>
     *
     * </span>
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     */
    public void setBytes(int parameterIndex, byte x[]) throws SQLException {

        checkSetParameterIndex(parameterIndex);
        checkClosed();
        setParameter(parameterIndex, x == null ? null
                                               : new Binary(x), Types
                                               .LONGVARBINARY, false);
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

        checkSetParameterIndex(parameterIndex);
        checkClosed();
        setParameter(parameterIndex, x, Types.DATE, false);
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

        checkSetParameterIndex(parameterIndex);
        checkClosed();
        setParameter(parameterIndex, x, Types.TIME, false);
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

        checkSetParameterIndex(parameterIndex);
        checkClosed();
        setParameter(parameterIndex, x, Types.TIMESTAMP, false);
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
        checkClosed();

        if (x == null) {
            String msg = "input stream is null";

            throw jdbcDriver.sqlException(Trace.INVALID_JDBC_ARGUMENT, msg);
        }

        try {
            setParameter(parameterIndex, x == null ? null
                                                   : ValuePool.getString(
                                                   StringConverter
                                                       .inputStreamToString(
                                                           x)), Types
                                                               .LONGVARCHAR, false);
        } catch (IOException e) {
            throw jdbcDriver.sqlException(Trace.INVALID_CHARACTER_ENCODING);
        }
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
        checkClosed();

        if (x == null) {
            String msg = "input stream is null";

            throw jdbcDriver.sqlException(Trace.INVALID_JDBC_ARGUMENT, msg);
        }

        StringBuffer sb = new StringBuffer(length / 2);

        try {
            for (int i = 0; i < sb.length(); i++) {
                int c = x.read();

                if (c == -1) {
                    break;
                }

                int c1 = x.read();

                if (c1 == -1) {
                    break;
                }

                int character = c << 8 | c1;

                sb.append(character);
            }
        } catch (IOException e) {
            throw jdbcDriver.sqlException(Trace.TRANSFER_CORRUPTED);
        }

        setParameter(parameterIndex, ValuePool.getString(sb.toString()),
                     Types.LONGVARCHAR, false);
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
     * Up to and including HSQLDB 1.7.1, a binary stream was converted to
     * a SQL string consisting of hexidecimal digits that represent the
     * stream. <p>
     *
     * <b>Example:</b> <p>
     *
     * <PRE>
     *    PreparedStatement ps =
     *    connection.prepareStatement("SELECT * FROM t WHERE col = ?");
     *    ps.setBinaryStream(1, myStream, 4);
     *    ps.execute();
     * </PRE>
     *
     * Given that the first 4 bytes of the stream are 0xff, 0xff, 0xff, 0xff,
     * the above code fragement would perform the equivalent to the
     * following SQL:
     *
     * <PRE>
     *    SELECT * FROM t WHERE col = 'ffffffff'
     * </PRE>
     *
     * Zero-length specifications resulted in zero bytes being read from the
     * stream.  In such cases, the parameter was compiled to an empty SQL
     * string.  If the length specified in the above code fragment was zero,
     * the the emitted SQL would be:
     *
     * <PRE>
     *    SELECT * FROM t WHERE col = ''
     * </PRE>
     *
     * <hr>
     *
     * Starting with 1.7.2, a byte[] is read from the input stream and
     * submitted directly to the engine in binary form, possibly after
     * conversion to the type of the parameter . Also, unlike previous
     * releases, the stream is no longer closed when the read is
     * finished, as this was incorrect. <p>
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
        checkClosed();

        if (x == null) {
            String msg = "input stream is null";

            throw jdbcDriver.sqlException(Trace.INVALID_JDBC_ARGUMENT, msg);
        }

        byte b[] = new byte[length];
        int  len;

        try {
            len = x.read(b, 0, length);

            // NO:
            // Caller should be responsible for this.
            // What if client wants to read other things from the stream
            // later?
            //x.close();
        } catch (IOException e) {
            throw jdbcDriver.sqlException(Trace.INPUTSTREAM_ERROR,
                                          e.getMessage());
        }

        // CHECKME:  What if fewer than length bytes are read?
        //           shouldn't we trim b?  Or is it definite from the
        //           spec that b must always be length long?
        if (len < length) {
            byte[] b2 = new byte[len];

            System.arraycopy(b, 0, b2, 0, len);

            b = b2;
        }

        setParameter(parameterIndex, new Binary(b), Types.LONGVARBINARY,
                     false);
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
    public synchronized void clearParameters() throws SQLException {

        // NOTE: synchronized because we wouldn't want
        //       the partial effects of clearing the array
        //       to be visible to an executeXXX call.
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
     * Starting with 1.7.2, calling this method is identical to
     * {@link #setObject(int, Object) setObject(int, Object)}.  This stems
     * from the fact that the driver now knows the SQL type of each parameter
     * as the result of the prepare and performs a conversion locally to that
     * type.  As such, the targetSqlType is currently relatively meaningless.
     * That is, if targetSqlType does not match the SQL type of the parameter,
     * then performing the conversion to targetSqlType is wasteful, since
     * another conversion will still be performed to the SQL type of the
     * parameter. On the other hand, even if targetSqlType matches the SQL
     * type of the parameter, this is currently at best a hint, since there
     * is no guarantee that the class of x is the standard mapping to
     * targetSqlType or that x will not need conversion to be
     * compatible with the SQL type of the parameter. <p>
     *
     * When SQLData support is implemented for the driver, the situation
     * described for HSQLDB 1.7.2, above, is likely to change. <p>
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
     * Up to HSQLDB 1.6.1, this method did not work properly with all
     * combinations of object class and targetSqlType. <p>
     *
     * Starting with 1.7.0, this has been corrected. <p>
     *
     * Starting with 1.7.2, calling this method is identical to
     * {@link #setObject(int, Object) setObject(int, Object)}.  This stems
     * from the fact that the driver now knows the SQL type of each parameter
     * as the result of the prepare and performs a conversion locally to that
     * type.  As such, the targetSqlType is currently relatively meaningless.
     * That is, if targetSqlType does not match the SQL type of the parameter,
     * then performing the conversion to targetSqlType is wasteful, since
     * another conversion will still be performed to the SQL type of the
     * parameter. On the other hand, even if targetSqlType matches the SQL
     * type of the parameter, this is currently at best a hint, since there
     * is no guarantee that the class of x is the standard mapping to
     * targetSqlType or that x will not need conversion to be
     * compatible with the SQL type of the parameter. <p>
     *
     * When SQLData support is implemented for the driver, the situation
     * described for HSQLDB 1.7.2, above, is likely to change. <p>
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

// NO:  Redundant.  setParameter() now, if required, automatically,
//      directly and locally converts x to an object whose class
//      is that of the internal storage format of the SQL type of the
//      parameter, or fails in the attempt, in which case this method will
//      throw. Moreover, we do not yet support UDTs of any sort or handling
//      of SQLData, so there's not much good in paying attention to the
//      targetSqlType argument at this point; if it's different from the
//      parameter SQL type, there's no use in doing an intermediate conversion,
//      as setParameter() will still have to do another conversion.  And if it
//      is the same as the parameter type, then the requested conversion is the
//      same one that <i>may</i> be performed by setParameter(), so there is no
//      point in doing it twice.
//
//        if (x == null) {
//            setParameter(parameterIndex, null);
//
//            return;
//        }
//
//// fredt@users 20020328 -  patch 482109 by fredt - OBJECT handling
//        if (targetSqlType != Types.OTHER) {
//            try {
//                x = Column.convertObject(x, targetSqlType);
//            } catch (HsqlException e) {
//                jdbcDriver.throwError(e);
//            }
//        }
//
//        setObjectInType(parameterIndex, x, targetSqlType);
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
     * Up to and including 1.7.1, this method called the apropriate setXXX
     * method when it detected that the specified Object was one that has a
     * standard mapping to a java.sql.Types type.  However, if it was known
     * that the parameter would correspond to a value for (or comparison against)
     * a column of type OTHER, then the method
     * <code>setObject(i,x,Types.OTHER)</code> was to be used instead;
     * in HSQLDB, columns of type OTHER are reserved strictly for storing
     * serialized Java Objects.  That is, when attempting to insert or update
     * using values other than null for OTHER column values, an exception is
     * thrown if the value is not a serializable Java Object. <p>
     *
     * Starting with HSQLDB 1.7.2, a prepared statement object knows, apriori,
     * the SQL type of each parameter, as a result of preparing the supplied
     * SQL character sequence.  As such, this method no longer calls out to
     * setXXX methods and instead the supplied object is paased directly into
     * the internal setParameter method where the need for conversion is
     * detected and, if required, only a single, direct conversion to the
     * internal representation of the type of the parameter is performed. <p>
     *
     * </span>
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the object containing the input parameter value
     * @exception SQLException if a database access error occurs or the type
     *      of the given object is ambiguous
     */
    public void setObject(int parameterIndex, Object x) throws SQLException {

        /** @todo fredt - implement SQLData support */
        boolean search;
        int     type;

        checkSetParameterIndex(parameterIndex);
        checkClosed();

        if (x instanceof byte[]) {
            x      = new Binary((byte[]) x);
            search = false;
        } else {
            search = true;
        }

        type = org.hsqldb.Types.getWidestTypeNrNoConvert(x);

        setParameter(parameterIndex, x, type, false);

// NO:  This is now redundant.  When required, setParameter() automatically
//      converts x to an object whose class is that of the internal
//      representation used for the SQL type of the paramter, or fails in
//      the attempt.
//      setObjectInType is now obsoleted as well, since setParameter()
//      will, under the correct conditions, attempt to fish for an equivalent
//      instance of the (maybe converted) parameter value object in the
//      ValuePool. The "correct condition" is if the fromPool argument is false
//      and the class/constraints (indicated SQL data type) of supplied object
//      indicate that no conversion is required or if conversion is required,
//      regarless of the value of fromPool.
//        if (x == null) {
//            setParameter(parameterIndex, null);
//
//            return;
//        }
//
//        int type = Types.OTHER;
//
//        if (x instanceof String) {
//            type = Types.VARCHAR;
//        } else if (x instanceof BigDecimal) {
//            type = Types.NUMERIC;
//        } else if (x instanceof Integer) {
//            type = Types.INTEGER;
//        } else if (x instanceof Long) {
//            type = Types.BIGINT;
//        } else if (x instanceof Float) {
//            type = Types.REAL;
//        } else if (x instanceof Double) {
//            type = Types.DOUBLE;
//        } else if (x instanceof byte[]) {
//            type = Types.BINARY;
//        } else if (x instanceof java.sql.Date) {
//            type = Types.DATE;
//        } else if (x instanceof Time) {
//            type = Types.TIME;
//        } else if (x instanceof Timestamp) {
//            type = Types.TIMESTAMP;
//        } else if (x instanceof Boolean) {
//            type = Types.BIT;
//        } else if (x instanceof Byte) {
//            type = Types.TINYINT;
//        } else if (x instanceof Short) {
//            type = Types.SMALLINT;
//        }
//
//        setObjectInType(parameterIndex, x, type);
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
    public synchronized void addBatch() throws SQLException {

// boucherb@users 20030801 - method implemented
        Object[] bpValues;
        int      len;

        checkClosed();

        len      = parameterValues.length;
        bpValues = new Object[len];

        System.arraycopy(parameterValues, 0, bpValues, 0, len);
        batch.add(bpValues);
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
// boucherb@users 20030801 - patch 1.7.2 - should set only the number
// of characters actually read fron the stream, not invariably the number of
// characters specified by the length argument.
    public void setCharacterStream(int parameterIndex, java.io.Reader reader,
                                   int length) throws SQLException {

        checkSetParameterIndex(parameterIndex);
        checkClosed();

        if (reader == null) {
            String msg = "reader is null";

            throw jdbcDriver.sqlException(Trace.INVALID_JDBC_ARGUMENT, msg);
        }

        char[] buffer = new char[length];
        int    len;
        String s;

        try {
            len = reader.read(buffer);

            if (len == -1) {
                throw new IOException();
            }
        } catch (IOException e) {
            throw jdbcDriver.sqlException(Trace.TRANSFER_CORRUPTED,
                                          e.toString());
        }

        s = ValuePool.getString(new String(buffer, 0, len));

        setParameter(parameterIndex, s, Types.LONGVARCHAR, false);
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
     * Starting with HSQLDB 1.7.2, this feature is supported, but
     * if and only if the product is built and run under a
     * JDBC 3 capable JVM. Lack of support for JDBC 1/2 builds is due
     * to lack of support for SQLData coupled with the fact that
     * Ref.getObject() is JDBC 3 only.  In 1.7.2, under JDBC 3,
     * setRef(i,x) is roughly equivalent (null handling not shown) to:
     *
     * <pre>
     * setObject(i, x.getObject());
     * </pre><p>
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

// boucherb@users 20030801 - method implemented
        checkSetParameterIndex(i);
        checkClosed();

// Note:  Ref surrogate in jdbcStubs cannot support this
//        since Ref.getObject() is JDBC3-only.  Need SQLData support
//        to do any better than this.
//#ifdef JDBC3
/*
        Object o = x == null ? null : x.getObject();
        int type = org.hsqldb.Types.getWidestTypeNrNoConvert(o);
        setParameter(i, o, type, true);
*/

//#else
        throw jdbcDriver.notSupportedJDBC3;

//#endif JDBC3
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
     * less than or equal to Integer.MAX_VALUE.  In 1.7.2, setBlob(i,x) is roughly
     * equivalent  (null and length handling not shown) to:
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
    public void setBlob(int i, Blob x) throws SQLException {

// boucherb@users 20030801 - method implemented
        checkSetParameterIndex(i);
        checkClosed();

        if (x == null) {
            setParameter(i, null, Types.NULL, false);
        }

        long   length;
        String msg;

        length = x.length();

        if (length > Integer.MAX_VALUE) {
            msg = "Maximum Blob input stream length exceeded: " + length;

            throw jdbcDriver.sqlException(Trace.INPUTSTREAM_ERROR, msg);
        }

        byte b[] = new byte[(int) length];
        int  len;

        try {
            len = x.getBinaryStream().read(b, 0, (int) length);
        } catch (IOException e) {
            throw jdbcDriver.sqlException(Trace.INPUTSTREAM_ERROR,
                                          e.getMessage());
        }

        // CHECKME:  What if fewer than length bytes are read?
        //           shouldn't  we trim b?  Or is it definite from the
        //           spec that b must always be length long?
        if (len < length) {
            byte[] b2 = new byte[len];

            System.arraycopy(b, 0, b2, 0, len);

            b = b2;
        }

        setParameter(i, new Binary(b), Types.LONGVARBINARY, false);
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
     * less than or equal to Integer.MAX_VALUE.  In 1.7.2, setClob(i,x) is rougly
     * equivalent (null and length handling not shown) to: <p>
     *
     * <pre>
     * setCharacterStream(i, x.getCharacterStream(), (int) x.length());
     * </pre>
     * </span>
     * <!-- end release-specific documentation -->
     * @param i the first parameter is 1, the second is 2, ...
     * @param x a <code>Clob</code> object that maps an SQL <code>CLOB</code>
     *      value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcPreparedStatement)
     */
    public void setClob(int i, Clob x) throws SQLException {

// boucherb@users 20030801 - method implemented
        checkSetParameterIndex(i);
        checkClosed();

        if (x == null) {
            setParameter(i, null, Types.LONGVARCHAR, true);
        }

        long   len;
        String msg;

        len = x.length();

        if (len > Integer.MAX_VALUE) {
            msg = "Maximum Clob input stream length exceeded: " + len;

            throw jdbcDriver.sqlException(Trace.INPUTSTREAM_ERROR, msg);
        }

        char[] buffer = new char[(int) len];
        String s;

        try {
            len = x.getCharacterStream().read(buffer);

            if (len == -1) {
                throw new IOException();
            }
        } catch (IOException e) {
            throw jdbcDriver.sqlException(Trace.TRANSFER_CORRUPTED,
                                          e.toString());
        }

        s = ValuePool.getString(new String(buffer, 0, (int) len));

        setParameter(i, s, Types.LONGVARCHAR, false);
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
     * Up to and including HSQLDB 1.7.1, this feature is not supported. <p>
     *
     * Starting with HSQLDB 1.7.2, this feature is supported and is
     * roughly equivatent (null handling not shown) to:
     *
     * <pre>
     * setObject(i, x.getArray());
     * </pre>
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

// boucherb@users 20030801 - method implemented
        Object o;
        int    type;

        checkSetParameterIndex(i);
        checkClosed();

        o = x == null ? null
                      : x.getArray();

        if (o instanceof byte[]) {
            o    = new Binary((byte[]) o);
            type = Types.LONGVARBINARY;
        } else {
            type = Types.OTHER;
        }

        // This will work in general if the component type of the array is
        // serializable and the type of the parameter is OTHER
        // Otherwise, Column.convertObject(o, type) will almost certainly
        // fail.
        setParameter(i, o, type, false);
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
    public ResultSetMetaData getMetaData() throws SQLException {

// boucherb@users 20030801 - method implemented
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

        checkSetParameterIndex(parameterIndex);
        checkClosed();

        try {
            setParameter(parameterIndex, x == null ? null
                                                   : HsqlDateTime
                                                   .getDateString(
                                                       x, cal), Types
                                                           .LONGVARCHAR, true);
        } catch (Exception e) {
            throw jdbcDriver.sqlException(Trace.INVALID_ESCAPE,
                                          e.getMessage());
        }
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

        checkSetParameterIndex(parameterIndex);
        checkClosed();

        try {
            setParameter(parameterIndex, x == null ? null
                                                   : HsqlDateTime
                                                   .getTimeString(
                                                       x, cal), Types
                                                           .LONGVARCHAR, true);
        } catch (Exception e) {
            throw jdbcDriver.sqlException(Trace.INVALID_ESCAPE,
                                          e.getMessage());
        }
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
        checkClosed();

        try {
            setParameter(parameterIndex, x == null ? null
                                                   : HsqlDateTime
                                                   .getTimestampString(
                                                       x, cal), Types
                                                           .LONGVARCHAR, true);
        } catch (Exception e) {
            throw jdbcDriver.sqlException(Trace.INVALID_ESCAPE,
                                          e.getMessage());
        }
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
     * Up to and including HSQLDB 1.7.2, the driver does not need the type
     * code and name information, so it is ignored.  That is, this method
     * is equivalent to setNull(int,int). <p>
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

        //setNull(paramIndex, sqlType);
        setParameter(paramIndex, null, sqlType, false);
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
     * Starting with HSQLDB 1.7.2, this feature is supported, but
     * if and only if the product is built and run under a
     * JDBC 3 capable JVM.   However, the DATALINK SQL type is not
     * supported in 1.7.2, so this method is roughly equivalent to: <p>
     *
     * <pre>
     * setObject(i, x);
     * </pre><p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the <code>java.net.URL</code> object to be set
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public void setURL(int parameterIndex,
                       java.net.URL x) throws SQLException {

// boucherb@users 20030801 - method implemented
        checkSetParameterIndex(parameterIndex);
        checkClosed();

        // This will work if the parameter type is OTHER since URL is
        // always serializable.  This will also work if the parameter
        // type is one that allows a conversion using x.toString() to
        // succeed;
        setParameter(parameterIndex, x, Types.OTHER, false);
    }
*/

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
//#ifdef JDBC3
/*
    public ParameterMetaData getParameterMetaData() throws SQLException {

// boucherb@users 20030801 - method implemented

        checkClosed();

        if (pmd == null) {
            pmd = new jdbcParameterMetaData(pmdDescriptor);
        }

        // NOTE:  pmd is declared as Object to avoid yet another #ifdef.
        return (ParameterMetaData) pmd;
    }
*/

//#endif JDBC3
    //-------------------- Internal Implementation -----------------------------

    /**
     * Constructs a statement that produces results of the requested
     * <code>type</code>.
     * @param c the Connection used execute this statement
     * @param sql the SQL statement this object represents
     * @param type the type of result this statement will produce
     * @throws HsqlException if a database access error occurs
     */
// TODO:  Maybe we need to be able to communicate that a statement
//        can generate multiple hetrogeneous results and/or sequences
//        of alternating (possible hetrogeneous) results and
//        update counts.
//
//        What to do?
//
//        PreparedStatement and CallableStatement specify a method
//        getMetaData() that allows describing only a single
//        result set apriori actually executing the statement and
//        retrieving a result set.  That is not good enough if a
//        statement can generate a hetrogeneous sequence.
//        Indeed, the spec is ambiguous on this matter, as it
//        is unclear whether to return null or throw when
//        calling getMetaData if a statement can generate a
//        hetrogeneous sequence. Furthermore, it is basically left up
//        to the reader to infer that null is to be returned if the
//        statement generates only update counts, never results.
//
//        The exact wording is:
//
//        Returns:
//          the description of a ResultSet object's columns or null if
//          the driver cannot return a ResultSetMetaData object
//
//        What does "cannot" mean in this context?
    jdbcPreparedStatement(jdbcConnection c, String sql,
                          int type) throws HsqlException {

        super(c, type);

        Iterator i;
        Result   in;
        Result   modesResult;
        Object[] row;

        resultOut.setResultType(ResultConstants.SQLPREPARE);
        resultOut.setMainString(sql);

        if (this instanceof jdbcCallableStatement) {
            resultOut.setStatementType(CompiledStatement.CALL);
        } else {
            resultOut.setStatementType(CompiledStatement.UNKNOWN);
        }

        in = connection.sessionProxy.execute(resultOut);

        if (in.iMode == ResultConstants.ERROR) {
            throw new HsqlException(in);
        }

        // else its a MULTI result encapsulating three sub results:
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
        //     to speed up internal execution of prepared stateements by
        //     dispensing with generating most rsmd values while generating
        //     the result, safe in the knowlege that the client already
        //     has a copy of the rsmd.  In general, only the colTypes array
        //     must be available at the engine, and only for network
        //     commincations so that the row output and row input
        //     interfaces can do their work.  One caveat is the the
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
            parameterTypes  = pmdDescriptor.getParameterTypes();
            parameterValues = new Object[parameterTypes.length];
            parameterModes  = pmdDescriptor.paramMode;
        } catch (Exception e) {
            throw Trace.error(Trace.GENERAL_ERROR, e.toString());
        }

        resultOut = new Result(ResultConstants.SQLEXECUTE,
                               pmdDescriptor.colType, statementID);

        // for toString()
        this.sql = sql;
    }

// boucherb@users - 20030805 - 1.7.2
// NOTE: Obsoleted by changes to setParameter
//    /**
//     * Internal setObject implementation. <p>
//     *
//     * @param parameterIndex the first parameter is 1, the second is 2, ...
//     * @param x the object containing the input parameter value
//     * @param type the SQL type (as defined in java.sql.Types) to be
//     *                sent to the database
//     * @throws SQLException if a database access error occurs
//     */
//    private void setObjectInType(int parameterIndex, Object x,
//                                 int type) throws SQLException {
//        if (x == null) {
//            setParameter(parameterIndex, null);
//
//            return;
//        }
//
//        switch (type) {
//
//            case Types.BIT :
//                setParameter(parameterIndex, x);
//                break;
//
//            case Types.TINYINT :
//                setByte(parameterIndex, ((Number) x).byteValue());
//                break;
//
//            case Types.SMALLINT :
//                setShort(parameterIndex, ((Number) x).shortValue());
//                break;
//
//            case Types.INTEGER :
//                setInt(parameterIndex, ((Number) x).intValue());
//                break;
//
//            case Types.BIGINT :
//                setLong(parameterIndex, ((Number) x).longValue());
//                break;
//
//            case Types.REAL :
//            case Types.FLOAT :
//            case Types.DOUBLE :
//                setDouble(parameterIndex, ((Number) x).doubleValue());
//                break;
//
//            case Types.NUMERIC :
//                setBigDecimal(parameterIndex, (BigDecimal) x);
//                break;
//
//            case Types.CHAR :
//            case Types.VARCHAR :
//            case Types.LONGVARCHAR :
//                setString(parameterIndex, (String) x);
//                break;
//
//            case Types.DATE :
//                setDate(parameterIndex, (java.sql.Date) x);
//                break;
//
//            case Types.TIME :
//                setTime(parameterIndex, (Time) x);
//                break;
//
//            case Types.TIMESTAMP :
//                setTimestamp(parameterIndex, (Timestamp) x);
//                break;
//
//            case Types.BINARY :
//            case Types.VARBINARY :
//            case Types.LONGVARBINARY :
//                setBytes(parameterIndex, (byte[]) x);
//                break;
//
//            case Types.OTHER :
//                setParameter(parameterIndex, x);
//                break;
//
//            default :
//                setParameter(parameterIndex, x.toString());
//                break;
//        }
//    }
    private void checkIsRowCount(boolean yes) throws SQLException {

        if (yes != isRowCount) {
            String msg = "Statement does not generate a " + (yes ? "row count"
                                                                 : "result set");

            throw jdbcDriver.sqlException(Trace.ASSERT_FAILED, msg);
        }
    }

    /**
     * Checks if the specified parameter index value is valid
     * @param i The parameter index to check
     * @throws SQLException if the specified parameter index is invalid
     */
    protected void checkSetParameterIndex(int i) throws SQLException {

        int    mode;
        String msg;

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
     * The internal parameter value setter. <p>
     *
     * @param i the first parameter is 1, the second is 2, ...
     * @param o the parameter value
     * @param inType The SQL type of the argument, o.  It is always safe to
     *    supply a value of OTHER here, as this will cause the driver to
     *    unconditionaly attempt to perform the appropriate conversion
     *    to the SQL type of the parameter, throwing if the conversion
     *    is not possible.  When passing any other value, inType *MUST
     *    properly indicate at least the widest SQL type to which the
     *    standard mappng of the class of the argument, o, promotes
     *    without conversion.
     *
     * @param searchPool Whether to search the ValuePool for an equivalent.
     *
     *    If true and the parent connection is to an in-process database,
     *    then an attempt is made to substitute the argument, o, with an
     *    equivalent object from the pool.  However, this occurs if and only
     *    if it is determined that the inType promotes without conversion to
     *    the SQL type of the parameter.  Otherwise, o is converted to the
     *    SQL type of the parameter.  Once again, if the parent connection is
     *    to an in-process database, then an attempt is made to substitute
     *    the object resulting from the conversion with and equivalent object
     *    from the pool. <p>
     *
     *    In general, this value is set false only from the interface
     *    implementation methods where it makes sense to pre-fetch from the
     *    pool before entry here, such as methods taking primitive values. <p>
     *
     * @throws SQLException If the argument o is not already in a form
     *    compatible with or cannot be converted to the SQL type of the
     *    indicated parameter position.
     */
    protected synchronized void setParameter(int i, Object o, int inType,
            boolean searchPool) throws SQLException {

        // synchronized, as we would not want the effect of setting
        // a parameter in one thread during an executeXXX call in
        // another to be visible to the execute call.
        // Already passed parameter index precheck
        i--;

        if (o == null) {
            parameterValues[i] = null;

            return;
        }

        int outType = parameterTypes[i];

        if (outType == Types.OTHER) {
            parameterValues[i] = (o instanceof JavaObject) ? o
                                                           : new JavaObject(
                                                           o);
        } else if (org.hsqldb.Types.promotesWithoutConversion(inType,
                outType)) {

            // PRE:
            // Safe, since  setArray, setBytes, setBlob, setBinaryStream
            // and setObject all preconvert to Binary if required.
            parameterValues[i] = (!searchPool || isNetConn) ? o
                                                            : ValuePool
                                                            .getObject(o);
        } else {
            try {
                o = Column.convertObject(o, outType);
            } catch (HsqlException e) {
                jdbcDriver.throwError(e);
            }

            // No point in getting from pool if net connection, since value
            // will be written out to socket, never directly end up referenced
            // as a field or expression value at the engine.
            parameterValues[i] = (isNetConn) ? o
                                             : ValuePool.getObject(o);
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

    /**
     * Implements the public close() method so as to avoid excessive calls
     * to close this connection's open statment objects. Overrides closeImpl()
     * in jdbcStatement.
     *
     * @param isDisconnect if true, called from Connection.close, else from
     *      this.close
     * @throws SQLException if a database access error occurs
     */
    synchronized void closeImpl(boolean isDisconnect) throws SQLException {

        HsqlException he;

        if (isClosed()) {
            return;
        }

        he = null;

        // If the parent connection is closing, then the CompiledStatementManger
        // instance at the other end of the connection will perform a
        // processDisconnect(sid), which free's, in one go, all of the
        // CompiledStatement objects that the Session has open.  If the
        // connection is a network connection and has any prepared or
        // callable statements open, we certainly do not want to make a
        // separate call across the network to free each one when the
        // connection closes; not only would this be a waste considering the
        // above, it could also represent a large time delay, since the latency
        // for each execute cycle across the network may, for internet and WAN
        // situations, easily be as much as ~100-200 ms (or even worse, for
        // example over a dialup connection).
        if (!isDisconnect) {
            try {
                connection.sessionProxy.execute(
                    Result.newFreeStmtResult(statementID));
            } catch (HsqlException e) {
                he = e;
            }
        }

        parameterValues = null;
        parameterTypes  = null;
        rsmdDescriptor  = null;
        pmdDescriptor   = null;
        rsmd            = null;
        pmd             = null;

        super.closeImpl(isDisconnect);

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

        StringBuffer sb;

        sb = new StringBuffer();

        sb.append(super.toString());

        try {
            if (!isClosed()) {
                sb.append("[sql=[").append(sql).append("]");

// Represent batch parameter values?
// No:  That could be a huge string.
                if (parameterValues.length > 0) {
                    sb.append(", parameters=[");

                    for (int i = 0; i < parameterValues.length; i++) {
                        sb.append('[');
                        sb.append(parameterValues[i]);
                        sb.append("], ");
                    }

                    sb.setLength(sb.length() - 2);
                    sb.append(']');
                }

                sb.append(']');
            }
        } catch (Exception e) {}

        return sb.toString();
    }
}
