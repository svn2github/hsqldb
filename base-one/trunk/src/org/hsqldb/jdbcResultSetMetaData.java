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

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.*;     // for Array, Blob, Clob, Ref
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.sql.SQLWarning;
import java.util.*;    // for Map
import java.util.Calendar;
import org.hsqldb.lib.AsciiStringInputStream;
import org.hsqldb.lib.StringInputStream;

// fredt@users 20020320 - patch 1.7.0 - JDBC 2 support and error trapping
// JDBC 2 methods can now be called from jdk 1.1.x - see javadoc comments
// SCROLL_INSENSITIVE and FORWARD_ONLY types for ResultSet are now supported
// fredt@users 20020315 - patch 497714 by lakuhns@users - scrollable ResultSet
// all absolute and relative positioning methods defined
// boucherb@users 20020409 - added "throws SQLException" to all methods where
// it was missing here but specified in the java.sql.ResultSet and
// java.sql.ResultSetMetaData interfaces, updated generic documentation to
// JDK 1.4, and added JDBC3 methods and docs
// boucherb@users and fredt@users 20020409/20020505 extensive review and update
// of docs and behaviour to comply with previous and latest java.sql specification
// tony_lai@users 20020820 - patch 595073 - duplicated exception msg

/**
 * Implements both the <code>java.sql.ResultSet</code> and
 * <code>java.sql.ResultSetMetaData</code> interfaces. <p>
 *
 * <span class="ReleaseSpecificDocumentation">
 * In short: <p>
 *
 * <UL>
 * <LI>A <code>ResultSet</code> object is essentially--but not limited to
 *    being--a table of data representing a database result set, which
 *    is usually generated by executing a statement that queries the
 *    database.</LI>
 * <LI>A <code>ResultSetMetaData</code> object is one that can be used to
 *    get information about the types and properties of the columns in a
 *    <code>ResultSet</code> object.</LI>
 * </UL>
 * <p>
 *
 * The following is composed of three sections:
 * <OL>
 * <LI>The generic overview for <code>ResultSet</code>.</LI>
 * <LI>The generic overview for <code>ResultSetMetaData</code>.</LI>
 * <LI>A discussion of some HSQLDB-specific concerns.</LI>
 * </OL>
 * </span> <p>
 * <!-- end Release-specific documentation -->
 *
 * <!-- start java.sql.ResultSet generaic documentation -->
 * <B>From <code>ResultSet</code>:</B><p>
 *
 * A table of data representing a database result set, which
 * is usually generated by executing a statement that queries the database.
 *
 * <P>A <code>ResultSet</code> object  maintains a cursor pointing
 * to its current row of data.  Initially the cursor is positioned
 * before the first row. The <code>next</code> method moves the
 * cursor to the next row, and because it returns <code>false</code>
 * when there are no more rows in the <code>ResultSet</code> object,
 * it can be used in a <code>while</code> loop to iterate through
 * the result set.
 * <P>
 * A default <code>ResultSet</code> object is not updatable and
 * has a cursor that moves forward only.  Thus, you can
 * iterate through it only once and only from the first row to the
 * last row. It is possible to
 * produce <code>ResultSet</code> objects that are scrollable and/or
 * updatable.  The following code fragment, in which <code>con</code>
 * is a valid <code>Connection</code> object, illustrates how to make
 * a result set that is scrollable and insensitive to updates by others,
 * and that is updatable. See <code>ResultSet</code> fields for other
 * options.
 * <PRE>
 *
 * Statement stmt = con.createStatement(
 *                            ResultSet.TYPE_SCROLL_INSENSITIVE,
 *                            ResultSet.CONCUR_UPDATABLE);
 * ResultSet rs = stmt.executeQuery("SELECT a, b FROM TABLE2");
 * // rs will be scrollable, will not show changes made by others,
 * // and will be updatable
 *
 * </PRE>
 * The <code>ResultSet</code> interface provides
 * <i>getter</i> methods (<code>getBoolean</code>, <code>getLong</code>,
 * and so on) for retrieving column values from the current row.
 * Values can be retrieved using either the index number of the
 * column or the name of the column.  In general, using the
 * column index will be more efficient.  Columns are numbered from 1.
 * For maximum portability, result set columns within each row should be
 * read in left-to-right order, and each column should be read only once.
 *
 * <P>For the getter methods, a JDBC driver attempts
 * to convert the underlying data to the Java type specified in the
 * getter method and returns a suitable Java value.  The JDBC specification
 * has a table showing the allowable mappings from SQL types to Java types
 * that can be used by the <code>ResultSet</code> getter methods.
 * <P>
 * <P>Column names used as input to getter methods are case
 * insensitive.  When a getter method is called  with
 * a column name and several columns have the same name,
 * the value of the first matching column will be returned.
 * The column name option is
 * designed to be used when column names are used in the SQL
 * query that generated the result set.
 * For columns that are NOT explicitly named in the query, it
 * is best to use column numbers. If column names are used, there is
 * no way for the programmer to guarantee that they actually refer to
 * the intended columns.
 * <P>
 * A set of updater methods were added to this interface
 * in the JDBC 2.0 API (Java<sup><font size=-2>TM</font></sup> 2 SDK,
 * Standard Edition, version 1.2). The comments regarding parameters
 * to the getter methods also apply to parameters to the
 * updater methods.
 * <P>
 * The updater methods may be used in two ways:
 * <ol>
 * <LI>to update a column value in the current row.  In a scrollable
 * <code>ResultSet</code> object, the cursor can be moved backwards
 * and forwards, to an absolute position, or to a position
 * relative to the current row.
 * The following code fragment updates the <code>NAME</code> column
 * in the fifth row of the <code>ResultSet</code> object
 * <code>rs</code> and then uses the method <code>updateRow</code>
 * to update the data source table from which <code>rs</code> was
 * derived.
 * <PRE>
 *
 * rs.absolute(5); // moves the cursor to the fifth row of rs
 * rs.updateString("NAME", "AINSWORTH"); // updates the
 * // <code>NAME</code> column of row 5 to be <code>AINSWORTH</code>
 * rs.updateRow(); // updates the row in the data source
 *
 * </PRE>
 * <LI>to insert column values into the insert row.  An updatable
 * <code>ResultSet</code> object has a special row associated with
 * it that serves as a staging area for building a row to be inserted.
 * The following code fragment moves the cursor to the insert row, builds
 * a three-column row, and inserts it into <code>rs</code> and into
 * the data source table using the method <code>insertRow</code>.
 * <PRE>
 *
 * rs.moveToInsertRow(); // moves cursor to the insert row
 * rs.updateString(1, "AINSWORTH"); // updates the
 * // first column of the insert row to be <code>AINSWORTH</code>
 * rs.updateInt(2,35); // updates the second column to be <code>35</code>
 * rs.updateBoolean(3, true); // updates the third row to <code>true</code>
 * rs.insertRow();
 * rs.moveToCurrentRow();
 *
 * </PRE>
 * </ol>
 * <P>A <code>ResultSet</code> object is automatically closed when the
 * <code>Statement</code> object that
 * generated it is closed, re-executed, or used
 * to retrieve the next result from a sequence of multiple results.
 *
 * <P>The number, types and properties of a <code>ResultSet</code>
 * object's columns are provided by the <code>ResulSetMetaData</code>
 * object returned by the <code>ResultSet.getMetaData</code> method. <p>
 * <!-- end java.sql.ResultSet generic documentation -->
 *
 * <!-- start java.sql.ResultSetMetaData generic documentation-->
 * <B>From <code>ResultSetMetaData</code>:</B><p>
 *
 * An object that can be used to get information about the types
 * and properties of the columns in a <code>ResultSet</code> object.
 * The following code fragment creates the <code>ResultSet</code>
 * object rs, creates the <code>ResultSetMetaData</code> object rsmd,
 * and uses rsmd
 * to find out how many columns rs has and whether the first column in rs
 * can be used in a <code>WHERE</code> clause.
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
 * <B>HSQLDB-Specific Information:</B> <p>
 *
 * As stated above, <code>jdbcResultSet</code> implements both the
 * <code>ResultSet</code> and <code>ResultSetMetaData</code> interfaces.
 * However, to gain access to the interface methods of
 * <code>ResultSetMetaData</code> in a driver independent way, the
 * traditional call to the {@link #getMetaData getMetaData} method should
 * be used, rather than casting objects known to be of type
 * <code>jdbcResultSet</code> to type <code>ResultSetMetaData</code>. <p>
 *
 * A <code>ResultSet</code> object generated by HSQLDB is, as is standard
 * JDBC behavior, by default of <code>ResultSet.TYPE_FORWARD_ONLY</code>
 * and does not allow the use of absolute and relative positioning
 * methods.  However, starting with 1.7.0, if a statement is created
 * with:<p>
 *
 * <code class="JavaCodeExample">
 * Statement stmt createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
 *                                ResultSet.CONCUR_READ_ONLY);
 * </code> <p>
 *
 * then the <code>ResultSet</code> objects it produces support
 * using all of  the absolute and relative positioning methods of JDBC2
 * to set the position of the current row, for example:<p>
 *
 * <code class="JavaCodeExample">
 * rs.absolute(5);<br>
 * String fifthRowValue = rs.getString(1);<br>
 * rs.relative(4);<br>
 * String ninthRowValue = rs.getString(1);<br>
 * </code>
 * <p>
 *
 * Note: An HSQLDB <code>ResultSet</code> object persists, even after its
 * connection is closed.  This is regardless of the operational mode of
 * the {@link Database Database} from which it came.  That is, they
 * persist whether originating from a <code>Server</code>,
 * <code>WebServer</code> or in-process mode <code>Database.</code>
 * <p>
 *
 * Up to and including HSQLDB 1.7.0, there is no support for any of
 * the methods introduced in JDBC 2 relating to updateable result sets.
 * These methods include all updateXXX methods, as well as the
 * {@link #insertRow}, {@link #updateRow}, {@link #deleteRow},
 * {@link #moveToInsertRow} (and so on) methods.  A call to any such
 * unsupported method will simply result in throwing a
 * <code>SQLException</code> which states that the function is not
 * supported.  It is not anticipated that HSQLDB-native support for
 * updateable <code>ResultSet</code> objects will be introduced in the
 * HSQLDB 1.7.x series.  Such features <I>may</I> be part of the
 * HSQLDB 2.x series, but no decisions have been made at this point.<p>
 *
 * <b>JRE 1.1.x Notes:</b> <p>
 *
 * In general, JDBC 2 support requires Java 1.2 and above, and JDBC 3 requires
 * Java 1.4 and above. In HSQLDB, support for methods introduced in different
 * versions of JDBC depends on the JDK version used for compiling and building
 * HSQLDB.<p>
 *
 * Since 1.7.0, it is possible to build the product so that
 * all JDBC 2 methods can be called while executing under the version 1.1.x
 * <em>Java Runtime Environment</em><sup><font size="-2">TM</font></sup>.
 * However, some of these method calls require <code>int</code> values that
 * are defined only in the JDBC 2 or greater version of the
 * <a href="http://java.sun.com/j2se/1.4/docs/api/java/sql/ResultSet.html">
 * <code>ResultSet</code></a> interface.  For this reason, when the
 * product is compiled under JDK 1.1.x, these values are defined in
 * here, in this class. <p>
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
 * <b>ResultSetMetaData Implementation Notes:</b> <p>
 *
 * HSQLDB supports a subset of <code>ResultSetMetaData</code> interface.
 * The JDBC specification for <code>ResultSetMetaData</code> is in part very
 * vague. Several methods are exclusively for columns that are database
 * table columns. There is a complete lack of specification on how these
 * methods are supposed to distinguish between updatable and non-updatable
 * <code>ResultSet</code> objects or between columns that are database
 * table columns and those that are results of calculations or functions.
 * This causes potential incompatibility between interpretations of the
 * specifications in different JDBC drivers.<p>
 *
 * As such, <code>DatabaseMetadata</code> reporting will be enhanced
 * in future 1.7.x and greater versions, but enhancements to reporting
 * <code>ResultSetMetaData</code> have to be considered carefully as they
 * impose a performance penalty on all <code>ResultSet</code> objects
 * returned from HSQLDB, whether or not the <code>ResultSetMetaData</code>
 * methods are used.<p>
 *
 * (fredt@users) <br>
 * (boucherb@users)<p>
 *
 * </span>
 * @see jdbcStatement#executeQuery
 * @see jdbcStatement#getResultSet
 * @see <a href=
 * "http://java.sun.com/j2se/1.4/docs/api/java/sql/ResultSetMetaData.html">
 * <code>ResultSetMetaData</code></a>
 */
public class jdbcResultSetMetaData implements ResultSetMetaData {

    Result        rResult;
    jdbcResultSet resultSet;

    jdbcResultSetMetaData(jdbcResultSet resultSet) {
        this.resultSet = resultSet;
        rResult        = resultSet.rResult;
    }

    /**
     * <!-- start generic documentation -->
     * Returns the number of columns in this <code>ResultSet</code>
     * object. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * See discussion at: {@link #getMetaData} <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @return the number of columns
     * @exception SQLException if a database access error occurs
     */
    public int getColumnCount() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        return resultSet.iColumnCount;
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
     * HSQLDB 1.7.1 does not support this feature.  <p>
     *
     * The return value from this method depends on whether the
     * <code>jdbc.strict_md</code>
     * connection property is specified as true. When this property is true:
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * See discussion at: {@link #getMetaData} <p>
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

        // FIXME:
        //
        // We should throw:  this returns false even for columns
        // that *are* autoincrement (identity) columns,
        // which is incorrect behaviour.
        //
        // I realize that it makes no diff. w.r.t. result set updatability,
        // since we do not support updateable results, but that's
        // not the only reason client code wants to know something
        // like this.
        //
        // The (shudder...) alternative is to fix the
        // Result class to provide this info and do a
        // massive sweep of the engine code to ensure
        // the info is set correctly everywhere when
        // generating Result objects.
        // boucherb@users 20025013
        // MISSING:
        // checkColumn(column); ?
        // boucherb@users 20020413
        if (resultSet.strictMetaData) {
            throw getNotSupported();
        }

        return false;
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
     * HSQLDB 1.7.1 does not support this feature.  <p>
     *
     * The return value from this method depends on whether the
     * <code>jdbc.strict_md</code>
     * connection property is specified as true. When this property is true:
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * See discussion at: {@link #getMetaData} <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean isCaseSensitive(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // MISSING:
        // checkColumn(column); ?
        // boucherb@users 20020413
        if (resultSet.strictMetaData) {
            throw getNotSupported();
        }

        return true;
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
     * HSQLDB 1.7.1 does not support this feature.  <p>
     *
     * The return value from this method depends on whether the
     * <code>jdbc.strict_md</code>
     * connection property is specified as true. When this property is true:
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * See discussion at: {@link #getMetaData} <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean isSearchable(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // CHECKME:
        // is this absolutely always true?
        // javav object (other) columns , for instance,
        // are always equal if not null.
        // Does that qualify as searchable?
        // We need to go back and read the spec. for searchable.
        // boucherb@users 20020413
        // MISSING:
        // checkColumn(column); ?
        // boucherb@users 20020413
// fredt@users - OTHER can be used in a WHERE clause but we don't know if
// RS column is a DB column or a computed value
        if (resultSet.strictMetaData) {
            throw getNotSupported();
        }

        return true;
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
     * Up to and including HSQLDB 1.7.0, this method always returns false.<p>
     *
     * See discussion at: {@link #getMetaData} <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean isCurrency(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        resultSet.checkColumn(column);

        // FIXME:
        // we should throw until this not a todo
        // isCurrency <==> DECIMAL?
        // Re-read the DatabaseMetadata.getTypeInfo spec.
        // boucherb@users 20020413
        // MISSING:
        // boucherb@users 20020413
// fredt@users - 20020413 - DECIMAL has variable scale so it is not currency
        return false;
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
     * The return value from this method depends on whether the
     * <code>jdbc.strict_md</code>
     * connection property is specified as true. When this property is true:
     * Always returns <code>columnNullableUnknown</code>. <p>
     *
     * See discussion at: {@link #getMetaData} <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return the nullability status of the given column; one of
     *   <code>columnNoNulls</code>,
     *   <code>columnNullable</code> or <code>columnNullableUnknown</code>
     * @exception SQLException if a database access error occurs
     */
    public int isNullable(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // FIXME:
        //
        // We should throw:  this returns columnNullable even for columns
        // that are NOT NULL constrained columns,
        // which is incorrect behaviour.
        //
        // I realize that it makes no diff. w.r.t. result set updatability,
        // since we do not support updateable results, but that's
        // not the only reason client code wants to know something
        // like this.
        //
        // The (shudder...) alternative is to fix the
        // Result class to provide this info and do a
        // massive sweep of the engine code to ensure
        // the info is set correctly everywhere when
        // generating Result objects.
        // boucherb@users 20025013
        // MISSING:
        // checkColumn(column); ?
        // boucherb@users 20020413
        if (resultSet.strictMetaData) {
            return columnNullableUnknown;
        }

        return columnNullable;
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
     * HSQLDB 1.7.1 adds support for this feature.  <p>
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

        resultSet.checkColumn(column);

        int type = rResult.colType[column - 1];
        int size = Column.numericTypes.length;

        for (int i = 0; i < size; i++) {
            if (type == Column.numericTypes[i]) {
                return true;
            }
        }

        return false;
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
     * Up to and including HSQLDB 1.7.0, this method always returns
     * 0 (no limit/unknown).<p>
     *
     * See discussion at: {@link #getMetaData} <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return the normal maximum number of characters allowed as the width
     *    of the designated column
     * @exception SQLException if a database access error occurs
     */
    public int getColumnDisplaySize(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        resultSet.checkColumn(column);

        // Some program expect that this is the maximum allowed length
        // for this column, so it is dangerous to return the size required
        // to display all records
        return 0;
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
     * See discussion at: {@link #getMetaData} <p>
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

        resultSet.checkColumn(column);

        return rResult.sLabel[--column];
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
     * See discussion at: {@link #getMetaData} <p>
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

        resultSet.checkColumn(column);

        if (resultSet.getColumnName) {
            return rResult.sName[--column];
        } else {
            return rResult.sLabel[--column];
        }
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
     * Up to and including 1.7.1, HSQLDD does not support schema names. <p>
     *
     * This method always returns an empty <code>String</code>.<p>
     *
     * See discussion at: {@link #getMetaData} <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return schema name or "" if not applicable
     * @exception SQLException if a database access error occurs
     */
    public String getSchemaName(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        resultSet.checkColumn(column);

        return "";
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
     * Up to and including HSQLDB 1.7.0, this method always returns
     * 0 (unknown/no limit).<p>
     *
     * See discussion at: {@link #getMetaData} <p>
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

        resultSet.checkColumn(column);

        return 0;
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
     * Up to and including HSQLDB 1.7.0, this method always returns
     * 0 (unknown).<p>
     *
     * See discussion at: {@link #getMetaData} <p>
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

        resultSet.checkColumn(column);

        return 0;
    }

    /**
     * <!-- start generic documentation -->
     * Gets the designated column's table name. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * See discussion at: {@link #getMetaData} <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return table name or "" if not applicable
     * @exception SQLException if a database access error occurs
     */
    public String getTableName(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        resultSet.checkColumn(column);

        return rResult.sTable[--column];
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
     * Up to and including 1.7.1, HSQLDD does not support catalogs. <p>
     *
     * This method always returns an empty <code>String</code>.<p>
     *
     * See discussion at: {@link #getMetaData} <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return the name of the catalog for the table in which the given column
     *     appears or "" if not applicable
     * @exception SQLException if a database access error occurs
     */
    public String getCatalogName(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        resultSet.checkColumn(column);

        return "";
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
     * See discussion at: {@link #getMetaData} <p>
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

        resultSet.checkColumn(column);

        return rResult.colType[--column];
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the designated column's database-specific type name. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * See above at: (@link #getColumnType)<p>
     *
     * See discussion at: {@link #getMetaData} <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
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

        resultSet.checkColumn(column);

        return Column.getTypeString(rResult.colType[--column]);
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
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * The return value from this method depends on whether the
     * <code>jdbc.strict_md</code>
     * connection property is specified as true. When this property is true:
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * See discussion at: {@link #getMetaData} <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean isReadOnly(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // FIXME:
        // we should throw until this is not a todo
        // isReadOnly <==>
        //     db readonly |
        //     connection readonly |
        //     table readonly |
        //     user not granted insert or update or delete on table |
        //     user not granted update on column (not yet supported)
        // boucherb@users 20020413
        // MISSING:
        // checkColumn(column); ?
        // boucherb@users 20020413
        // fredt@users - 20020413 - also if the RS column is a DB column
        if (resultSet.strictMetaData) {
            throw getNotSupported();
        }

        return false;
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
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * The return value from this method depends on whether the
     * <code>jdbc.strict_md</code>
     * connection property is specified as true. When this property is true:
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * See discussion at: {@link #getMetaData} <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean isWritable(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // FIXME:
        // we should throw until this is not a todo
        // we just don't know as it is: how can we say true?
        // isWritable <==>
        // !isReadOnly &
        // user *is* granted update on table (if column has non-null table)
        // boucherb@users 20020413
        // MISSING:
        // checkColumn(column); ?
        // boucherb@users 20020413
        // fredt@users - 20020413 - also if the RS column is a DB column
        if (resultSet.strictMetaData) {
            throw getNotSupported();
        }

        return true;
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
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * The return value from this method depends on whether the
     * <code>jdbc.strict_md</code>
     * connection property is specified as true. When this property is true:
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * See discussion at: {@link #getMetaData} <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean isDefinitelyWritable(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // FIXME:
        // we should throw until this is not a todo
        // we just don't know as it is: how can we say true?
        // isDefinitelyWritable <==>
        //     isWritable &
        //     ???
        // boucherb@users 20020413
        // MISSING:
        // checkColumn(column); ?
        // boucherb@users 20020413
        // fredt@users - 20020413 - also if the RS column is a DB column
        if (resultSet.strictMetaData) {
            throw getNotSupported();
        }

        return true;
    }

    //--------------------------JDBC 2.0-----------------------------------

    /**
     * <!-- start generic documentation -->
     * Returns the fully-qualified name of the Java class whose instances
     * are manufactured if the method <code>ResultSet.getObject</code>
     * is called to retrieve a value
     * from the column.  <code>ResultSet.getObject</code> may return a
     * subclass of the class returned by this method. <p>
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
     * See discussion at: {@link #getMetaData} <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param column the first column is 1, the second is 2, ...
     * @return the fully-qualified name of the class in the Java programming
     *   language that would be used by the method
     *   <code>ResultSet.getObject</code> to retrieve the value in the
     *   specified column. This is the class name used for custom mapping.
     * @exception SQLException if a database access error occurs
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview for
     *  jdbcResultSet)
     */
    public String getColumnClassName(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        // fredt@users - 20020413
        // need a reverse mapping list between SQL and Java types used
        // in HSQLDB.
        throw getNotSupported();
    }

    //---------------------------- Private ---------------------------------

    /**
     * Convenience method for throwing FUNCTION_NOT_SUPPORTED
     *
     * @return a SQLException object whose message states that the function is
     * not supported
     */
    private SQLException getNotSupported() {
        return Trace.error(Trace.FUNCTION_NOT_SUPPORTED);
    }

    /**
     * Convenience method for throwing FUNCTION_NOT_SUPPORTED for JDBC 3
     * methods.
     *
     * @return a SQLException object whose message states that the function is
     * not supported and is a JDBC 3 method
     */
    private SQLException getNotSupportedJDBC3() {
        return Trace.error(Trace.FUNCTION_NOT_SUPPORTED, "JDBC3");
    }
}
