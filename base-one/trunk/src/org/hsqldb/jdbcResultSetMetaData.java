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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSetMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.StringConverter;
import org.hsqldb.lib.StopWatch;
import org.hsqldb.store.ValuePool;

/** <!-- start generic documentation -->
 * An object that can be used to get information about the types
 * and properties of the columns in a <code>ResultSet</code> object.
 * The following code fragment creates the <code>ResultSet</code>
 * object rs, creates the <code>ResultSetMetaData</code> object rsmd,
 * and uses rsmd to find out how many columns rs has and whether the
 * first column in rs can be used in a <code>WHERE</code> clause.
 * <PRE>
 *
 * ResultSet rs = stmt.executeQuery("SELECT a, b, c FROM TABLE2");
 * ResultSetMetaData rsmd = rs.getMetaData();
 * int numberOfColumns = rsmd.getColumnCount();
 * boolean b = rsmd.isSearchable(1);
 *
 * </PRE>
 * <!-- end generic documentation -->
 *
 * <!-- start release-specific documentation -->
 * <span class="ReleaseSpecificDocumentation">
 * <b>HSQLDB-Specific:</b> <p>
 *
 * HSQLDB supports a subset of the <code>ResultSetMetaData</code> interface.<p>
 *
 * The JDBC specification for <code>ResultSetMetaData</code> is in part very
 * vague. This causes potential incompatibility between interpretations of the
 * specification as realized in different JDBC driver implementations. As such,
 * deciding to what degree reporting ResultSetMetaData is accurate has been
 * considered very carefully. Hopefully, the design decisions made in light of
 * these considerations have yeilded precisely the subset of full
 * ResultSetMetaData support that is most commonly needed and that is most
 * important, while also providing, under the most common use-cases, the
 * fastest access with the least overhead and the best comprimise between
 * speed, accuracy, jar-foootprint and retention of JDBC resources. <p>
 *
 * (fredt@users) <br>
 * (boucherb@users)<p>
 * </span>
 * <!-- end release-specific documentation -->
 * @see jdbcStatement#executeQuery
 * @see jdbcStatement#getResultSet
 * @see java.sql.ResultSetMetaData
 */
public class jdbcResultSetMetaData implements ResultSetMetaData {

    /**
     * The minimum value that this object returns in response to
     * calling {@link #getColumnDisplaySize(int) getColumnDisplaySize()}.
     */
    public static final int MIN_DISPLAY_SIZE = 1;

    /**
     * The maximum value that this object returns in response to
     * calling {@link #getColumnDisplaySize(int) getColumnDisplaySize()}.
     */
    public static final int MAX_DISPLAY_SIZE = 255;

    /**
     * The maximum number of rows in this object's parent ResultSet that
     * will be scanned to calculate an approximation of
     * {@link #getColumnDisplaySize(int) getColumnDisplaySize()}, when the
     * value is not to be determined statically from the known maximum
     * length or precision of the column's data type.
     */
    public static final int MAX_SCAN = 512;

    /**
     * An array of objects, each of which represents the reported attributes
     * for  a single column of this object's parent ResultSet.
     */
    private jdbcColumnMetaData[] columnMetaData;

    /** The number of columns in this object's parent ResultSet. */
    private int columnCount;

    /**
     * Whether to use the underlying column name or label when reporting
     * getColumnName().
     */
    private boolean useColumnName;

    /**
     * If true, then timings for init() are printed
     * to the console.
     */
    private static final boolean TRACE = false;

    /**
     * Constructs a new jdbcResultSetMetaData object from the specified
     * jdbcResultSet and HsqlProprties objects.
     *
     * @param rs the jdbcResultSet object from which to construct a new
     *        jdbcResultSetMetaData object
     * @param props the HsqlProperties object from which to construct a
     *        new jdbcResultSetMetaData object
     * @throws SQLException if a database access error occurs
     */
    jdbcResultSetMetaData(jdbcResultSet rs,
                          HsqlProperties props) throws SQLException {
        init(rs, props);
    }

    /**
     * Constructs a new jdbcResultSetMetaData object from the specified
     * Result and HsqlProprties objects.
     *
     * @param rs the Result object from which to construct a new
     *        jdbcResultSetMetaData object
     * @param props the HsqlProperties object from which to construct a
     *        new jdbcResultSetMetaData object
     * @throws SQLException if a database access error occurs
     */
    jdbcResultSetMetaData(Result r,
                          HsqlProperties props) throws SQLException {
        init(r, props);
    }

    /**
     * Initializes this jdbcResultSetMetaData object from the specified
     * jdbcResultSet and HsqlProperties objects.
     *
     * @param rs the jdbcResultSet object from which to initialize this
     *        jdbcResultSetMetaData object
     * @param props the HsqlProperties object from which to initialize this
     *        jdbcResultSetMetaData object
     * @throws SQLException if a database access error occurs
     */
    void init(jdbcResultSet rs, HsqlProperties props) throws SQLException {

        if (rs == null) {
            throw jdbcDriver.sqlException(
                Trace.GENERAL_ERROR,
                Trace.jdbcResultSetMetaData_jdbcResultSetMetaData, null);
        }

        init(rs.rResult, props);
    }

    /**
     * Initializes this jdbcResultSetMetaData object from the specified
     * Result and HsqlProperties objects.
     *
     * @param rs the Result object from which to initialize this
     *        jdbcResultSetMetaData object
     * @param props the HsqlProperties object from which to initialize this
     *        jdbcResultSetMetaData object
     * @throws SQLException if a database access error occurs
     */
    void init(Result r, HsqlProperties props) throws SQLException {

        jdbcColumnMetaData    cmd;
        DITypeInfo            ti;
        StopWatch             sw;
        int                   ditype;
        int                   ditype_sub;
        Result.ResultMetaData rmd;

        if (r == null) {
            throw jdbcDriver.sqlException(
                Trace.GENERAL_ERROR,
                Trace.jdbcResultSetMetaData_jdbcResultSetMetaData_2, null);
        }

        if (r.iMode != ResultConstants.DATA) {
            return;
        }

        columnCount    = r.getColumnCount();
        useColumnName  = props.isPropertyTrue("get_column_name");
        columnMetaData = new jdbcColumnMetaData[columnCount];
        rmd            = r.metaData;
        ti             = new DITypeInfo();

        for (int i = 0; i < columnCount; i++) {
            cmd               = new jdbcColumnMetaData();
            columnMetaData[i] = cmd;

            // Typically, these null checks are not needed, but as
            // above, it is not _guaranteed_ that these values
            // will be non-null.   So, it is better to do the work
            // here than have to perform checks and conversions later.
            cmd.catalogName     = rmd.sCatalog[i] == null ? ""
                                                          : rmd.sCatalog[i];
            cmd.schemaName      = rmd.sSchema[i] == null ? ""
                                                         : rmd.sSchema[i];
            cmd.tableName       = rmd.sTable[i] == null ? ""
                                                        : rmd.sTable[i];
            cmd.columnName      = rmd.sName[i] == null ? ""
                                                       : rmd.sName[i];
            cmd.columnLabel     = rmd.sLabel[i] == null ? ""
                                                        : rmd.sLabel[i];
            cmd.columnType      = rmd.colType[i];
            cmd.columnTypeName  = Types.getTypeString(cmd.columnType);
            cmd.columnClassName = rmd.sClassName[i];
            cmd.isWritable      = rmd.isWritable[i];
            cmd.isReadOnly      = !cmd.isWritable;

            // default: cmd.isDefinitelyWritable = false;
            cmd.isAutoIncrement = rmd.isIdentity[i];
            cmd.isNullable      = rmd.nullability[i];
            ditype              = cmd.columnType;

            if (cmd.columnType == Types.VARCHAR_IGNORECASE) {
                ditype     = Types.VARCHAR;
                ditype_sub = Types.TYPE_SUB_IGNORECASE;
            } else if (cmd.isAutoIncrement) {
                ditype_sub = Types.TYPE_SUB_IDENTITY;
            } else {
                ditype_sub = Types.TYPE_SUB_DEFAULT;
            }

            ti.setTypeCode(ditype);
            ti.setTypeSub(ditype_sub);

            if (cmd.columnClassName == null
                    || cmd.columnClassName.length() == 0) {
                cmd.columnClassName = ti.getColStClsName();
            }

            Integer precision = ti.getPrecision();

            cmd.precision = precision == null ? 0
                                              : precision.intValue();

            Boolean iua = ti.isUnsignedAttribute();

            cmd.isSigned = iua != null &&!iua.booleanValue();

            Boolean ics = ti.isCaseSensitive();

            cmd.isCaseSensitive = ics != null && ics.booleanValue();

            Integer sc = ti.getSearchability();

            cmd.isSearchable = sc != null
                               && sc.intValue()
                                  != DatabaseMetaData.typePredNone;
            cmd.columnDisplaySize = ti.getMaxDisplaySize();

            if (cmd.columnDisplaySize > 0
                    && cmd.columnDisplaySize <= MIN_DISPLAY_SIZE) {
                cmd.columnDisplaySize = MIN_DISPLAY_SIZE;
            } else if (cmd.columnDisplaySize <= MAX_DISPLAY_SIZE) {

                // do nothing
            } else {
                String s;
                int    rc;
                int    len;
                int    max;
                Record rec;

                rc  = 0;
                max = MIN_DISPLAY_SIZE;
                rec = r.rRoot;

                while (rec != null && rc < MAX_SCAN) {
                    s = null;

                    try {
                        s = (String) Column.convertObject(rec.data[i],
                                                          Types.CHAR);
                    } catch (Exception e) {

                        // If this this fails for one, it
                        // will probably fail for all,
                        // due to the column being OTHER and
                        // the local JVM being unable to
                        // deserialize, so break early.
                        break;
                    }

                    len = (s == null) ? 3    // arbitrary: "null".length()
                                      : s.length();

                    if (len >= MAX_DISPLAY_SIZE) {
                        max = MAX_DISPLAY_SIZE;

                        break;
                    } else if (len > max) {
                        max = len;
                    }

                    rc++;

                    rec = rec.next;
                }

                cmd.columnDisplaySize = max;
            }
        }
    }

    /**
     * <!-- start generic documentation -->
     * Returns the number of columns in this <code>ResultSet</code>
     * object. <p>
     * <!-- end generic documentation -->
     * @return the number of columns
     * @exception SQLException if a database access error occurs
     */
    public int getColumnCount() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return columnCount;
    }

    /**
     * <!-- start generic documentation -->
     * Indicates whether the designated column is automatically numbered,
     * thus read-only. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 did not support accurately reporting this value,
     * either always throwing or always returning false, depending
     * upon client property values.<p>
     *
     * Starting with HSQLDB 1.7.2, this feature is better supported. <p>
     *
     * <hr>
     *
     * However, it must be stated here that, contrary to the generic
     * documentation above, HSQLDB automatically numbered columns
     * (IDENTITY columns, in HSQLDB parlance) are not read-only. <p>
     *
     * In fact, the generic documentation above seems to contradict the general
     * definition of what, at minimum, an auto-increment column is: <p>
     *
     * Simply, an auto-increment column is one that guarantees it has a
     * unique value after a successful insert or update operation, even if
     * no value is supplied or NULL is explicitly specified by the application
     * or a default value expression. <p>
     *
     * Further, without SQL Feature T176, Sequence generator support, the
     * attributes of the internal source consulted for unique values are not
     * defined. That is, unlike for a standard SQL SEQUENCE object or a system
     * with full SQL 9x or 200n support for SQL Feature T174, Identity columns,
     * an application must not assume and cannot determine in a standard way
     * that auto-increment values start at any particular point, increment by
     * any particular value or have any of the other attributes generally
     * associated with SQL SEQUENCE generators. Further still, without full
     * support for both feature T174 and T176, if a unique value is supplied
     * by an application or provided by a declared or implicit default value
     * expression, then whether that value is used or substituted with one
     * from the automatic unique value  source is implementation-defined
     * and cannot be known in a standard way. Finally, without full support
     * for features T174 and T176, it is also implementation-defined and
     * cannot be know in a standard way whether an exception is thrown or
     * a unique value is automatically substituted when an application or
     * default value expression supplies a non-NULL,
     * non-unique value. <p>
     *
     * Up to and including HSQLDB 1.7.2, values supplied by an application or
     * default value expression are used if they are indeed non-NULL unique
     * values, while an exception is thrown if either possible value source
     * for the site attempts to supply a non-NULL, non-unique value.  This is
     * very likely to remain the behaviour of HSQLDB for its foreseable
     * lifetime and at the very least for the duration of the 1.7.x release
     * series.<p>
     *
     * <hr>
     *
     * Regardless of the new and better support for reporting
     * isAutoIncrement(), it is still possible under certain conditions that
     * accurate reporting may be impossible. For example, if this object's
     * parent Connection is closed before the first call to this method or to
     * any other method of this class that initializes the connection-dependent
     * ResultSetMetaData values, then it is impossible to report accurately for
     * result set columns that directly represent table column values.<p>
     *
     * Under such special circumstances, the driver rethrows the exception that
     * occured during the initialization, or a SQLException wrapping it. <p>
     *
     * Those wishing to determine the auto-increment status of a table column
     * in isolation from ResultSetMetaData can do so by inspecting the
     * corresponding value of the SYSTEM_COLUMNS.IS_IDENTITY BIT column which
     * is also currently included (in a fashion proprietary to HSQLDB) as the
     * last column of the jdbcDatabaseMetaData.getColumns() result.
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean isAutoIncrement(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkColumn(column);

        return columnMetaData[--column].isAutoIncrement;
    }

    /**
     * <!-- start generic documentation -->
     * Indicates whether a column's case matters. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 did not support accurately reporting this value.  <p>
     *
     * Starting with 1.7.2, this feature is better supported.  <p>
     *
     * This method returns true for any column whose data type is a character
     * type, with the exception of VARCHAR_IGNORECASE for which it returns
     * false. It also returns false for any column whose data type is a
     * not a character data type. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param column the first column is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean isCaseSensitive(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkColumn(column);

        return columnMetaData[--column].isCaseSensitive;
    }

    /**
     * <!-- start generic documentation -->
     * Indicates whether the designated column can be used in a where
     * clause. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 did not support accurately reporting this value.  <p>
     *
     * Starting with 1.7.2, this feature is better supported.  <p>
     *
     * If the data type of the column is definitely known to be searchable
     * in any way under HSQLDB, then true is returned, else false.  That is,
     * if the type is reported in DatabaseMetaData.getTypeInfo() as having
     * DatabaseMetaData.typePredNone or is not reported, then false is
     * returned, else true.
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param column the first column is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean isSearchable(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkColumn(column);

        return columnMetaData[--column].isSearchable;
    }

    /**
     * <!-- start generic documentation -->
     * Indicates whether the designated column is a cash value. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including HSQLDB 1.7.2, this method always returns
     * false. <p>
     *
     * This is because true fixed (precision,scale) data types are not yet
     * supported.  That is, DECIMAL and NUMERIC types are implemented
     * as a thin wrap of java.math.BigDecimal, which cannot, without
     * additional, as yet unimplemented constraint enforcement code, be
     * said to be a fixed (precision,scale) types. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param column the first column is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean isCurrency(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkColumn(column);

        return columnMetaData[--column].isCurrency;
    }

    /**
     * <!-- start generic documentation -->
     * Indicates the nullability of values in the designated column. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to 1.7.1, HSQLDB did not support accurately reporting this
     * value. <p>
     *
     * Starting with 1.7.2, this feature is better supported.  <p>
     *
     * columnNullableUnknown is always returned for result set columns that
     * do not directly represent table column values (i.e. are calculated),
     * while the corresponding value in SYSTEM_COLUMNS.NULLABLE is returned
     * for result set columns that do directly represent table column values. <p>
     *
     * Those wishing to determine the nullable status of a table column in
     * isolation from ResultSetMetaData and in a DBMS-independent fashion
     * can do so by calling DatabaseMetaData.getColumns() with the appropriate
     * filter values and inspecting the result at the position described in
     * the API documentation. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param column the first column is 1, the second is 2, ...
     * @return the nullability status of the given column; one of
     *      <code>columnNoNulls</code>,
     *      <code>columnNullable</code> or <code>columnNullableUnknown</code>
     * @exception SQLException if a database access error occurs
     */
    public int isNullable(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkColumn(column);

        return columnMetaData[--column].isNullable;
    }

    /**
     * <!-- start generic documentation -->
     * Indicates whether values in the designated column are signed
     * numbers. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 introduced support for this feature and 1.7.2
     * reports identical values (although using a slightly different
     * implementation).<p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean isSigned(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkColumn(column);

        return columnMetaData[--column].isSigned;
    }

    /**
     * <!-- start generic documentation -->
     * Indicates the designated column's normal maximum width in
     * characters. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including HSQLDB 1.7.1, this method always returned
     * 0, which was intended to convey unknown display size.
     * Unfortunately, this value is not universally handled by all
     * clients and in the worst case can cause some applications to
     * crash. <p>
     *
     * Starting with 1.7.2, this feature is better supported.  <p>
     *
     * For colums whose data type has a known maximum display size (not zero),
     * the following rules apply:
     *
     * <ol>
     *
     * <li> if the value is in [1,MIN_DISPLAY_SIZE],  MIN_DISPLAY_SIZE is
     * reported.  <p>
     *
     * <li> if the value is in [MIN_DISPLAY_SIZE + 1, MAX_DISPLAY_SIZE],
     * the value itself is reported.
     * </ol> <p>
     *
     * In the standard distribution, MIN_DISPLAY_SIZE is 6, the minimum number
     * of characters required to display a character sequence representing the
     * Java String representation of null, bracketed with two additional
     * characters (e.g. "(null)"), while MAX_DISPLAY_SIZE is 255, the typical
     * maximum size for character display in graphical presentation
     * manangers. <p>
     *
     * In all other cases, up to the first MAX_SCAN (512 in the standard
     * distribution) rows of the result set are scanned to calculate an
     * approximation of the maximum width, in characters, that would be
     * required to display the column's data if each value were retrieved
     * as a Java String using ResultSet.getString(). If the scan at any time
     * determines that the approximation will result in a value greater than
     * or equal to MAX_DISPLAY_SIZE, then the scan is terminated and
     * MAX_DISPLAY_SIZE is reported.  Othersize, the fully approximated
     * display size is reported. MIN_DISPLAY_SIZE is reported if the scan
     * encounters that the parent result set has no rows, while the minimum
     * value calculated by the approximation is also MIN_DISPLAY_SIZE, in the
     * case where the first MAX_SCAN (or fewer) values of the column are all
     * null, zero-length or less than MIN_DISPLAY_SIZE characters when
     * retreived using ResultSet.getString(). <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param column the first column is 1, the second is 2, ...
     * @return the normal maximum number of characters allowed as the width
     *      of the designated column
     * @exception SQLException if a database access error occurs
     */
    public int getColumnDisplaySize(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkColumn(column);

        return columnMetaData[--column].columnDisplaySize;
    }

    /**
     * <!-- start generic documentation -->
     * Gets the designated column's suggested title for use in printouts and
     * displays. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * In HSQLDB a <code>ResultSet</code> column label is determined in the
     * following order of precedence:<p>
     *
     * <OL>
     * <LI>The label (alias) specified in the generating query.</LI>
     * <LI>The name of the underlying column, if no label is specified.<br>
     *    This also applies to aggregate functions.</LI>
     * <LI>An empty <code>String</code>.</LI>
     * </OL> <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return the suggested column title
     * @exception SQLException if a database access error occurs
     */
    public String getColumnLabel(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkColumn(column);

        return columnMetaData[--column].columnLabel;
    }

    /**
     * <!-- start generic documentation -->
     * Get the designated column's name. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * In HSQLDB a ResultSet column name is determined in the following
     * order of prcedence:<p>
     *
     * <OL>
     * <LI>The name of the underlying columnm, if the ResultSet column
     *   represents a column in a table.</LI>
     * <LI>The label or alias specified in the generating query.</LI>
     * <LI>An empty <code>String</code>.</LI>
     * </OL> <p>
     *
     * If the <code>jdbc.get_column_name</code> property of the database
     * has been set to false, this method returns the same value as
     * {@link #getColumnLabel(int)}.<p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return column name
     * @exception SQLException if a database access error occurs
     */
    public String getColumnName(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkColumn(column);

        column--;

        return useColumnName ? columnMetaData[column].columnName
                             : columnMetaData[column].columnLabel;
    }

    /**
     * <!-- start generic documentation -->
     * Get the designated column's table's schema. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to 1.7.1, HSQLDB did not support the notion of schemas at all,
     * including schema names in result set metadata; this method always
     * returned "". <p>
     *
     * Staring with 1.7.2, schema name reporting is supported only as an
     * optional, experimental feature that is disabled by default.
     * Enabling this feature requires setting the database property
     * "hsqldb.schemas=true". <p>
     *
     * Specifically, when this feature is enabled under 1.7.2, only very
     * limited support is provided by the engine for executing SQL containing
     * schema-qualified database object identifiers.  That is, when this
     * feature is enabled under 1.7.2, it is not yet possible in most cases
     * to use what would otherwise be the correct, canonical SQL calculated
     * from ResultSetMetaData. <p>
     *
     * Regardless, reporting is done only in system table content and is
     * not yet carried over to ResultSetMetaData. <p>
     *
     * For greater detail, see discussion at:
     * {@link jdbcDatabaseMetaData}. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param column the first column is 1, the second is 2, ...
     * @return schema name or "" if not applicable
     * @exception SQLException if a database access error occurs
     */
    public String getSchemaName(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkColumn(column);

        return columnMetaData[--column].schemaName;
    }

    /**
     * <!-- start generic documentation -->
     * Get the designated column's number of decimal digits. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including HSQLDB 1.7.1, this method always returned 0,
     * which was intended to convey that the precision was <em>unknown</em>,
     * </em>undefined</em> or that <em>no applicable limit</em> was
     * imposed. <p>
     *
     * Starting with 1.7.2, HSQLDB reports the maximum length or
     * precision intrinsic to the data type of each result set column. <p>
     *
     * This method does not yet make any attempt to report the declared
     * length or precision specifiers for table columns (if they are defined,
     * which up to 1.7.2 is not a requirement in DDL), as these values may or
     * may not be enforced, depending on the value of the database
     * property: <p>
     *
     * <pre>
     * sql.enforce_size
     * </pre>
     *
     * Because the property may change from one instantiation of a Database
     * to the next and because, when set true, is not applied to existing
     * values in table columns (only to new values introduced by following
     * inserts and updates), the length and/or precision specifiers for table
     * columns still do not neccessarily accurately reflect true constraints
     * upon the contents of the columns. This situation may or may not change
     * in a future release. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param column the first column is 1, the second is 2, ...
     * @return precision
     * @exception SQLException if a database access error occurs
     */
    public int getPrecision(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkColumn(column);

        return columnMetaData[--column].precision;
    }

    /**
     * <!-- start generic documentation -->
     * Gets the designated column's number of digits to right of the
     * decimal point. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including HSQLDB 1.7.2, this method always returns 0
     * (which is the actual scale for integral types and is to be interpreted
     * as <em>unknown</em> or <em>not applicable</em> for all other
     * types). <p>
     *
     * HSQLDB currently implements DECIMAL and NUMERIC--the only HSQLDB
     * types to which this value would apply, if supported--as a thin wrap
     * of BigDecimal and thus does not presently ever enforce scale
     * declarations.  Those wishing to determine the declared--intended, but
     * not enforced--scale of a DECIMAL and NUMERIC table column should
     * instead consult the DatabaseMetaData.getColumns() result using the
     * required filter parameters. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return scale
     * @exception SQLException if a database access error occurs
     */
    public int getScale(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkColumn(column);

        return columnMetaData[--column].scale;
    }

    /**
     * <!-- start generic documentation -->
     * Gets the designated column's table name. <p>
     * <!-- end generic documentation -->
     * @param column the first column is 1, the second is 2, ...
     * @return table name or "" if not applicable
     * @exception SQLException if a database access error occurs
     */
    public String getTableName(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkColumn(column);

        return columnMetaData[--column].tableName;
    }

    /**
     * <!-- start generic documentation -->
     * Gets the designated column's table's catalog name. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB did not support the notion of
     * catalog and this method always returned "". <p>
     *
     * Starting with 1.7.2, HSQLDB supports catalog reporting only as an
     * optional, experimental feature that is disabled by default. Enabling
     * this feature requires setting the database property
     * "hsqldb.catalogs=true". When enabled, the catalog name for table columns
     * is reported as the name by which the hosting Database knows itself. <p>
     *
     * HSQLDB does not yet support any use of catalog qualification in
     * DLL or DML. This fact is accurately indicated in the corresponding
     * DatabaseMetaData.supportsXXX() method return values. <p>
     *
     * Regardless, reporting is done only in system table content and is
     * not yet carried over to ResultSetMetaData. <p>
     *
     * For greater detail, see discussion at:
     * {@link jdbcDatabaseMetaData}. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return the name of the catalog for the table in which the given column
     *      appears or "" if not applicable
     * @exception SQLException if a database access error occurs
     */
    public String getCatalogName(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkColumn(column);

        return columnMetaData[--column].catalogName;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the designated column's SQL type. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * This reports the SQL type of the column. HSQLDB can return Objects in
     * any Java integral type wider than <code>Integer</code> for an SQL
     * integral type.<p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     *
     * @param column the first column is 1, the second is 2, ...
     * @return SQL type from java.sql.Types
     * @exception SQLException if a database access error occurs
     * @see java.sql.Types
     */
    public int getColumnType(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkColumn(column);

        int type = columnMetaData[--column].columnType;

        return type == Types.VARCHAR_IGNORECASE ? Types.VARCHAR
                                                : type;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the designated column's database-specific type name. <p>
     * <!-- end generic documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return type name used by the database. If the column type is
     * a user-defined type, then a fully-qualified type name is returned.
     * @exception SQLException if a database access error occurs
     */
    public String getColumnTypeName(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkColumn(column);

        return columnMetaData[--column].columnTypeName;
    }

    /**
     * <!-- start generic documentation -->
     * Indicates whether the designated column is definitely not writable.<p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB did not support accurately reporting
     * this value. <p>
     *
     * Starting with HSQLDB 1.7.2, this feature is better supported. <p>
     *
     * For result set columns that do not directly
     * represent table column values (i.e. are calculated), true is reported.
     * Otherwise, the read only status of the table and the database are used
     * in the calculation, but not the read-only status of the session. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param column the first column is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean isReadOnly(int column) throws SQLException {

        checkColumn(column);

        return columnMetaData[--column].isReadOnly;
    }

    /**
     * <!-- start generic documentation -->
     * Indicates whether it is possible for a write on the designated
     * column to succeed. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including 1.7.1, HSQLDB did not support accurately reporting
     * this value. <p>
     *
     * Starting with HSQLDB 1.7.2, this feature is better supported. <p>
     *
     * In essense, the negation of isReadOnly() is reported. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param column the first column is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean isWritable(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkColumn(column);

        return columnMetaData[--column].isWritable;
    }

    /**
     * <!-- start generic documentation -->
     * Indicates whether a write on the designated column will definitely
     * succeed. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 did not support reporting this value accurately. <p>
     *
     * Starting with HSQLDB 1.7.2, this method always returns false. The
     * reason for this is that it is generally either very complex or
     * simply impossible to calculate deterministically true for table columns
     * under all concievable conditions. The value is of dubious usefulness, except
     * perhaps if there were support for updateable result sets using
     * "SELECT ... FOR UPDATE" style locking.  However, this is not anticipated to
     * occur any time in the 1.7.x release series. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param column the first column is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean isDefinitelyWritable(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkColumn(column);

// NOTE:
// boucherb@users - 20020329
// currently, we can't tell _for sure_ that this is true without a
// great deal more work (and even then, not necessarily deterministically),
// plus it is of dubious usefulness to know this is true _for sure_ anyway.
// It's not like one can do an insert or update without a try-catch block
// using JDBC, even if one knows that a column is definitely writable.  And
// the catch will always let us know if there is a failure and why.  Also,
// it is typically completely useless to know that, although it is  _possible_
// that a write may succeed (as indicated by a true value of isWritable()),
// it also might fail (as indicated by a false returned here).
        // as of 1.7.2, always false
        return columnMetaData[--column].isDefinitelyWritable;
    }

    //--------------------------JDBC 2.0-----------------------------------

    /**
     * <!-- start generic documentation -->
     * Returns the fully-qualified name of the Java class whose instances
     * are manufactured if the method <code>ResultSet.getObject</code>
     * is called to retrieve a value from the column.
     * <code>ResultSet.getObject</code> may return a subclass of the class
     * returned by this method. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * HSQLDB 1.7.1 did not support this feature; calling this method
     * always caused an <code>SQLException</code> to be thrown,
     * stating that the function was not supported. <p>
     *
     * Starting with HSQLDB 1.7.2, this method returns the value of
     * SYSTEM_ALLTYPEINFO.COL_ST_CLS_NAME for the table row
     * corresponding to the data type of the specified ResultSet
     * column.
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return the fully-qualified name of the class in the Java programming
     *      language that would be used by the method
     *      <code>ResultSet.getObject</code> to retrieve the value in the
     *      specified column. This is the class name used for custom mapping.
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *      jdbcResultSet)
     */
    public String getColumnClassName(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkColumn(column);

        return columnMetaData[--column].columnClassName;
    }

    public String toString() {

        StringBuffer sb = new StringBuffer();

        sb.append(super.toString());

        if (columnCount == 0) {
            sb.append("[columnCount=0]");

            return sb.toString();
        }

        sb.append('[');

        for (int i = 0; i < columnCount; i++) {
            sb.append('\n');
            sb.append("   column_");
            sb.append(i + 1);
            sb.append('=');
            sb.append(columnMetaData[i]);
            sb.append(',');
            sb.append(' ');
        }

        sb.setLength(sb.length() - 2);
        sb.append('\n');
        sb.append(']');

        return sb.toString();
    }

// ------------------------- Internal Implementation ---------------------------

    /**
     * Performs an internal check for column index validity. <p>
     *
     * @param column index of column to check
     * @throws SQLException when this object's parent ResultSet has
     *      no such column
     */
    private void checkColumn(int column) throws SQLException {

        if (column < 1 || column > columnCount) {
            throw jdbcDriver.sqlException(Trace.COLUMN_NOT_FOUND,
                                          String.valueOf(column));
        }
    }
}
