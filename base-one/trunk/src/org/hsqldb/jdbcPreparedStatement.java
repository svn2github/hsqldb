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
 * However, some of these method calls require <code>int</code> values that
 * are defined only in the JDBC 2 or greater version of
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

    /**
     * The parameters for the next call
     */
    private final Object[] parameters;

    /**
     * The types of parameters
     */
    private final int[] types;

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
     * HSQLDB 1.7.0 follows the standard behaviour by overriding the same
     * method in jdbcStatement class. <p>
     *
     * Calling this method will have no effect.
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param enable <code>true</code> to enable escape processing;
     *     <code>false</code> to disable it
     * @exception SQLException if a database access error occurs
     */
// fredt@users 20020428 - patch 1.7.0 - method orerrides the one in jdbcStatement
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
     * result sets.  However, be aware that this behaviour <i>may</i>
     * change in a future release.
     * </span>
     *
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
            convertParameterTypes();
            resultOut.setParameterData(parameters);

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

        resultIn = null;

        try {
            convertParameterTypes();
            resultOut.setParameterData(parameters);

            resultIn = connection.sessionProxy.execute(resultOut);
        } catch (HsqlException e) {
            throw jdbcDriver.sqlException(e);
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

        resultIn = null;

        try {
            convertParameterTypes();
            resultOut.setParameterData(parameters);

            resultIn = connection.sessionProxy.execute(resultOut);
        } catch (HsqlException e) {
            throw jdbcDriver.sqlException(e);
        }

        if (resultIn == null || resultIn.iMode == ResultConstants.DATA) {
            throw new SQLException(
                "executeUpdate() cannot be used with this statement");
        } else if (resultIn.iMode == ResultConstants.ERROR) {
            throw new SQLException(resultIn.getMainString(),
                                   resultIn.getSubString(),
                                   resultIn.getStatementID());
        }

        return resultIn.getUpdateCount();
    }

    /**
     * called just before binding call
     */
    private void convertParameterTypes() throws HsqlException {

        for (int i = 0; i < types.length; i++) {
            if (parameters[i] == null) {
                continue;
            }

            if (types[i] == Types.OTHER) {
                parameters[i] = new JavaObject(parameters[i]);
            } else {
                parameters[i] = Column.convertObject(parameters[i], types[i]);
            }
        }
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
     * </span>
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param sqlType the SQL type code defined in <code>java.sql.Types</code>
     * @exception SQLException if a database access error occurs
     */
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        setNull(parameterIndex);
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
        setParameter(parameterIndex, x ? Boolean.TRUE
                                       : Boolean.FALSE);
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
        setParameter(parameterIndex, ValuePool.getInt(x));
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
        setParameter(parameterIndex, ValuePool.getInt(x));
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
        setParameter(parameterIndex, ValuePool.getInt(x));
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
        setParameter(parameterIndex, ValuePool.getLong(x));
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
     * NaN <code>float</code> values properly.  With 1.7.0,
     * these values are converted to SQL <code>NULL</code>. With 1.7.1 these
     * values are sent to the database.
     * </span>
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     */

// fredt@users 20020325 - patch 448691 NaN by fredt
// fredt@users 20021013 - patch 1.7.1 - NaN and infinity preserved
    public void setFloat(int parameterIndex, float x) throws SQLException {

        long l = Double.doubleToLongBits((double) x);

        setParameter(parameterIndex, ValuePool.getDouble(l));
/*
        if (Float.isInfinite(x) || Float.isNaN(x)) {
            setNull(parameterIndex);
        } else {
            String s = String.valueOf(x);

            // ensure the engine treats the value as a DOUBLE, not DECIMAL
            if (s.indexOf('E') < 0) {
                s = s.concat("E0");
            }

            setParameter(parameterIndex, s);
        }
*/
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
     * NaN <code>float</code> values properly.  With 1.7.0,
     * these values are converted to SQL <code>NULL</code>. With 1.7.1 these
     * values are sent to the database.
     * </span>
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     */

// fredt@users 20020325 - patch 448691 NaN by fredt
// fredt@users 20021013 - patch 1.7.1 - NaN and infinity preserved
    public void setDouble(int parameterIndex, double x) throws SQLException {

        long l = Double.doubleToLongBits(x);

        setParameter(parameterIndex, ValuePool.getDouble(l));
/*
        if (Double.isInfinite(x) || Double.isNaN(x)) {
            setNull(parameterIndex);
        } else {
            String s = String.valueOf(x);

            // ensure the engine treats the value as a DOUBLE, not DECIMAL
            if (s.indexOf('E') < 0) {
                s = s.concat("E0");
            }

            setParameter(parameterIndex, s);
        }
*/
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
     * </span>
     *
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
     * </span>
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the parameter value
     * @exception SQLException if a database access error occurs
     */
    public void setBytes(int parameterIndex, byte x[]) throws SQLException {

        if (x == null) {
            setNull(parameterIndex);
        } else {
            setParameter(parameterIndex, x);
        }
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
     * This uses the default platform character encoding to convert bytes
     * into characters of the String. In future this is likely to change to
     * always treat the stream as ASCII.<p>
     *
     * Before HSQLDB 1.7.0, <code>setAsciiStream</code> and
     * <code>setUnicodeStream</code> were identical.
     * </span>
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the Java input stream that contains the ASCII parameter value
     * @param length the number of bytes in the stream
     * @exception SQLException if a database access error occurs
     */
    public void setAsciiStream(int parameterIndex, java.io.InputStream x,
                               int length) throws SQLException {

        try {
            if (x == null) {
                setNull(parameterIndex);
            } else {
                setString(parameterIndex,
                          StringConverter.inputStreamToString(x));
            }
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
     * Beginning with HSQLDB 1.7.0, this complies with JDBC3 specification.
     * </span>
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x a <code>java.io.InputStream</code> object that contains the
     *  Unicode parameter value as two-byte Unicode characters
     * @param length the number of bytes in the stream
     * @exception SQLException if a database access error occurs
     * @deprecated Sun does not include a reason, but presumably setCharacterStream is now prefered?
     */
    public void setUnicodeStream(int parameterIndex, java.io.InputStream x,
                                 int length) throws SQLException {

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

        setParameter(parameterIndex, sb.toString());
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
     * Up to and including HSQLDB 1.7.0, a binary stream is converted to
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
     * the above code fragement would emit the following SQL:
     *
     * <PRE>
     *    SELECT * FROM t WHERE col = 'ffffffff'
     * </PRE>
     *
     * Zero-length specifications result in zero bytes being read from the
     * stream.  In such cases, the parameter is compiled to an empty SQL
     * string.  If the length specified in the above code fragment was zero,
     * the the emitted SQL would be:
     *
     * <PRE>
     *    SELECT * FROM t WHERE col = ''
     * </PRE>
     *
     * This behaviour <i>may</i> change in a future release.
     * </span>
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the java input stream which contains the binary parameter value
     * @param length the number of bytes in the stream
     * @exception SQLException if a database access error occurs
     */
    public void setBinaryStream(int parameterIndex, java.io.InputStream x,
                                int length) throws SQLException {

        // todo: is this correct?
        // what if length=0?
        // fredt@users - that seems to be fine, zero length value
        byte b[] = new byte[length];

        try {
            x.read(b, 0, length);
            x.close();
        } catch (IOException e) {
            throw jdbcDriver.sqlException(Trace.INPUTSTREAM_ERROR,
                                          e.getMessage());
        }

        setBytes(parameterIndex, b);
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
        org.hsqldb.lib.ArrayUtil.fillArray(parameters, null);
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
     * Up to and including HSQLDB 1.7.0, calling this method is identical to
     * calling
     * {@link #setObject(int, Object, int) setObject(int, Object, int)}.
     * That is, this method simply calls setObject(int, Object, int),
     * ignoring the scale specification.
     * </span>
     *
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
     */

    /** @todo fredt - implement SQLData support */
    public void setObject(int parameterIndex, Object x, int targetSqlType,
                          int scale) throws SQLException {
        setObject(parameterIndex, x, targetSqlType);
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
     * </span>
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the object containing the input parameter value
     * @param targetSqlType the SQL type (as defined in java.sql.Types) to be
     *                sent to the database
     * @exception SQLException if a database access error occurs
     */
    public void setObject(int parameterIndex, Object x,
                          int targetSqlType) throws SQLException {

        if (x == null) {
            setNull(parameterIndex);

            return;
        }

// fredt@users 20020328 -  patch 482109 by fredt - OBJECT handling
        if (targetSqlType != Types.OTHER) {
            try {
                x = Column.convertObject(x, targetSqlType);
            } catch (HsqlException e) {
                jdbcDriver.throwError(e);
            }
        }

        setObjectInType(parameterIndex, x, targetSqlType);
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
     * This method will call the apropriate setXXX method when it detects that
     * the specified Object is one that has a standard mapping to a
     * java.sql.Types type.  However, if it known that the parameter will
     * correspond to a value for (or comparison against) a column of type
     * OTHER, then the method <code>setObject(i,x,Types.OTHER)</code>
     * should be used instead; in HSQLDB, columns of type OTHER are
     * reserved strictly for storing serialized Java Objects.  That is,
     * when attempting to insert or update using values other than
     * null for OTHER column values, an exception is thrown if the value
     * is not a serializable Java Object. <p>
     *
     * </span>
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the object containing the input parameter value
     * @exception SQLException if a database access error occurs or the type
     *      of the given object is ambiguous
     */

    /** @todo fredt - implement SQLData support */
    public void setObject(int parameterIndex, Object x) throws SQLException {

        if (x == null) {
            setNull(parameterIndex);

            return;
        }

        int type = Types.OTHER;

        if (x instanceof String) {
            type = Types.VARCHAR;
        } else if (x instanceof BigDecimal) {
            type = Types.NUMERIC;
        } else if (x instanceof Integer) {
            type = Types.INTEGER;
        } else if (x instanceof Long) {
            type = Types.BIGINT;
        } else if (x instanceof Float) {
            type = Types.REAL;
        } else if (x instanceof Double) {
            type = Types.DOUBLE;
        } else if (x instanceof byte[]) {
            type = Types.BINARY;
        } else if (x instanceof java.sql.Date) {
            type = Types.DATE;
        } else if (x instanceof Time) {
            type = Types.TIME;
        } else if (x instanceof Timestamp) {
            type = Types.TIMESTAMP;
        } else if (x instanceof Boolean) {
            type = Types.BIT;
        } else if (x instanceof Byte) {
            type = Types.TINYINT;
        } else if (x instanceof Short) {
            type = Types.SMALLINT;
        }

        setObjectInType(parameterIndex, x, type);
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
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @exception SQLException if a database access error occurs
     * @see jdbcStatement#addBatch
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcPreparedStatement)
     */
    public void addBatch() throws SQLException {

        Object[] bparms;
        int      len;

        try {
            convertParameterTypes();
        } catch (HsqlException e) {
            throw jdbcDriver.sqlException(e);
        }

        len    = parameters.length;
        bparms = new Object[len];

        System.arraycopy(parameters, 0, bparms, 0, len);
        batch.add(bparms);
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
     * HSQLDB 1.7.0 stores CHARACTER and related SQL types as Unicode so
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
    public void setCharacterStream(int parameterIndex, java.io.Reader reader,
                                   int length) throws SQLException {

        char[] buffer = new char[length];

        try {
            int result = reader.read(buffer);

            if (result == -1) {
                throw new IOException();
            }
        } catch (IOException e) {
            throw jdbcDriver.sqlException(Trace.TRANSFER_CORRUPTED);
        }

        setString(parameterIndex, new String(buffer));
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
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     * </span>
     * <!-- end release-specific documentation -->
     *
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
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param i the first parameter is 1, the second is 2, ...
     * @param x a <code>Blob</code> object that maps an SQL <code>BLOB</code>
     *     value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     * jdbcPreparedStatement)
     */
    public void setBlob(int i, Blob x) throws SQLException {
        throw jdbcDriver.notSupported;
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
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param i the first parameter is 1, the second is 2, ...
     * @param x a <code>Clob</code> object that maps an SQL <code>CLOB</code>
     *      value
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcPreparedStatement)
     */
    public void setClob(int i, Clob x) throws SQLException {
        throw jdbcDriver.notSupported;
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
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
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
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @return the description of a <code>ResultSet</code> object's columns or
     *    <code>null</code> if the driver cannot return a
     *    <code>ResultSetMetaData</code> object
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *   jdbcPreparedStatement)
     */
    public ResultSetMetaData getMetaData() throws SQLException {
        throw jdbcDriver.notSupported;
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

        if (x == null) {
            setNull(parameterIndex);
        } else {
            try {
                String dateString = HsqlDateTime.getDateString(x, cal);

                setParameter(parameterIndex, dateString);
            } catch (Exception e) {
                throw jdbcDriver.sqlException(Trace.INVALID_ESCAPE,
                                              e.getMessage());
            }
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

        if (x == null) {
            setNull(parameterIndex);
        } else {
            try {
                String dateString = HsqlDateTime.getTimeString(x, cal);

                setParameter(parameterIndex,
                             Column.createSQLString(dateString));
            } catch (Exception e) {
                throw jdbcDriver.sqlException(Trace.INVALID_ESCAPE,
                                              e.getMessage());
            }
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

        if (x == null) {
            setNull(parameterIndex);
        } else {
            try {
                String dateString = HsqlDateTime.getTimestampString(x, cal);

                setParameter(parameterIndex, dateString);
            } catch (Exception e) {
                throw jdbcDriver.sqlException(Trace.INVALID_ESCAPE,
                                              e.getMessage());
            }
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
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
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
        setNull(paramIndex, sqlType);
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
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the <code>java.net.URL</code> object to be set
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQL 1.7.0
     */
//#ifdef JDBC3
/*
    public void setURL(int parameterIndex,
                       java.net.URL x) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
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
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
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
        checkClosed();
        return new jdbcParameterMetaData(types);
    }
*/

//#endif JDBC3
    //-------------------- Internal Implementation -----------------------------

    /**
     * Constructs a statement that produces results of the requested
     * <code>type</code>.
     *
     * @param c the Connection used execute this statement
     * @param s the SQL statement this object represents
     * @param type the type of result this statement will produce
     */
    jdbcPreparedStatement(jdbcConnection c, String sql,
                          int type) throws HsqlException {

        super(c, type);

        resultOut.setResultType(ResultConstants.SQLPREPARE);
        resultOut.setMainString(sql);

        Result resultIn = connection.sessionProxy.execute(resultOut);

        if (resultIn.iMode == ResultConstants.ERROR) {
            throw new HsqlException(resultIn);
        }

        resultOut  = resultIn;
        types      = resultOut.getParameterTypes();
        parameters = new Object[types.length];

        //resultOut.setParameterData(parameters);
    }

    /**
     * Retrieves the SQL representation of the value for the
     * statement parameter at the specified index, as it would appear
     * resolved in a Java <code>String</code> representation of the
     * SQL statement this object represents.
     *
     * @param i the index of the desired parameter
     * @return the SQL rep'n of parameter i's value
     */
    protected String getParameter(int i) {

        if (i >= parameters.length) {
            return null;
        }

        return parameters[i].toString();
    }

    /**
     * Internal parameter nuller. <p>
     *
     * This causes the parameter to be represented as "NULL" in the SQL
     * submitted when executing this statement.
     *
     * @param parameterIndex of parameter to be set null
     */
    private void setNull(int parameterIndex) throws SQLException {
        setParameter(parameterIndex, null);
    }

    /**
     * Internal setObject implementation. <p>
     *
     * @param parameterIndex the first parameter is 1, the second is 2, ...
     * @param x the object containing the input parameter value
     * @param type the SQL type (as defined in java.sql.Types) to be
     *                sent to the database
     * @throws SQLException if a database access error occurs
     */
    private void setObjectInType(int parameterIndex, Object x,
                                 int type) throws SQLException {

        if (x == null) {
            setNull(parameterIndex);

            return;
        }

        switch (type) {

            case Types.BIT :
                setBoolean(parameterIndex, ((Boolean) x).booleanValue());
                break;

            case Types.TINYINT :
                setByte(parameterIndex, ((Number) x).byteValue());
                break;

            case Types.SMALLINT :
                setShort(parameterIndex, ((Number) x).shortValue());
                break;

            case Types.INTEGER :
                setInt(parameterIndex, ((Number) x).intValue());
                break;

            case Types.BIGINT :
                setLong(parameterIndex, ((Number) x).longValue());
                break;

            case Types.REAL :
            case Types.FLOAT :
            case Types.DOUBLE :
                setDouble(parameterIndex, ((Number) x).doubleValue());
                break;

            case Types.NUMERIC :
                setBigDecimal(parameterIndex, (BigDecimal) x);
                break;

            case Types.CHAR :
            case Types.VARCHAR :
            case Types.LONGVARCHAR :
                setString(parameterIndex, (String) x);
                break;

            case Types.DATE :
                setDate(parameterIndex, (java.sql.Date) x);
                break;

            case Types.TIME :
                setTime(parameterIndex, (Time) x);
                break;

            case Types.TIMESTAMP :
                setTimestamp(parameterIndex, (Timestamp) x);
                break;

            case Types.BINARY :
            case Types.VARBINARY :
            case Types.LONGVARBINARY :
                setBytes(parameterIndex, (byte[]) x);
                break;

            case Types.OTHER :
                setParameter(parameterIndex, x);
                break;

            default :
                setParameter(parameterIndex, x.toString());
                break;
        }
    }

    /**
     * Internal parameter value setter. <p>
     *
     * @param i the first parameter is 1, the second is 2, ...
     * @param s the parameter value, which must be already compiled to a
     *     Java String representing the SQL value for the parameter
     */
    private void setParameter(int i, Object s) throws SQLException {

        if (i > 0 && i <= parameters.length) {
            parameters[--i] = s;

            return;
        }

        throw new SQLException("out of range");
    }

    /**
     * This method should always throw if called for a PreparedStatement.
     */
    public void addBatch(String sql) throws java.sql.SQLException {
        throw jdbcDriver.notSupported;
    }

    /**
     * This method should always throw if called for a PreparedStatement.
     */
    public ResultSet executeQuery(String sql) throws java.sql.SQLException {
        throw jdbcDriver.notSupported;
    }

    /**
     * This method should always throw if called for a PreparedStatement.
     */
    public boolean execute(String sql) throws java.sql.SQLException {
        throw jdbcDriver.notSupported;
    }

    /**
     * This method should always throw if called for a PreparedStatement.
     */
    public int executeUpdate(String sql) throws java.sql.SQLException {
        throw jdbcDriver.notSupported;
    }

/** @todo  fredt - implement*/
    public String toString() {
        return null;
    }
}
