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
import org.hsqldb.lib.ValuePool;

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
 * considered very carefully. <p>
 *
 * Careful consideration was also required because a poor design choice
 * (say, in favour of fully accurate end-to-end support, such as eagerly
 * calculating all ResultSetMetaData values at the engine each time a query is
 * parsed for execution) has the potential to impose a significant performance
 * penalty on all transations processed by the engine as well as upon all
 * ResultSet objects returned from HSQLDB, regardless of whether
 * ResultSetMetaData methods are actually invoked by the client. <p>
 *
 * Hopefully, the design decisions made in light of these considerations have
 * yeilded precisely the subset of full ResultSetMetaData support that is most
 * commonly needed and that is most important, while also providing, under the
 * most common use-cases, the fastest access with the least overhead and the
 * best comprimise between speed, accuracy, jar-foootprint and prolonged
 * retention of JDBC resources. <p>
 *
 * (fredt@users) <br>
 * (boucherb@users)<p>
 *
 * <hr>
 *
 * Unlike previous releases, 1.7.2 breaks the ResultSetMetaData interface out
 * of the jdbcResultSet class into this new interface implementation.  It does
 * so for a number of reasons, including: <p>
 *
 * <ol>
 * <li> It allows ResultSetMetaData functionality to be seemlessly dropped
 *      from the distribution or to be easily replaced by a different
 *      implementation. This serves as yet another move toward better
 *      modularity and also as a move to improve facilitation of smaller
 *      jar size under memory constrained use cases. <p>
 *
 * <li> It allows ResultSetMetaData to persist beyond the life of the
 *      ResultSet from which it is obtained, without retaining expensive
 *      JDBC resources any longer than required. <p>
 *
 * </ol> <p>
 *
 * In light of some of the latest design goals and in addition to the points
 * listed above, special consideration should be given to using HSQLDB 1.7.2
 * jdbcResultSetMetaData objects. In particular, it is necessary to understand
 * that the initialization of the values this object returns has been
 * reimplemented as three fail-safe partitions and that these initializations
 * are one-time affairs. This means that the values belonging to each partition
 * are, after its initialization, cached thereafter.  Consequentially, values
 * in the Connection-dependent partition may not reflect later changes in the
 * state of the database, relative to any result set columns that represent
 * database table columns. <p>
 *
 * The design goals that prompted the decision to use one-shot initialization
 * and to break it into partitions were: <p>
 *
 * <ol>
 * <li> avoid the transaction processing and network overhead incurred by
 *      a design path that requires the engine core to build and transmit
 *      more than the absolutely essential ResultSetMetaData values with
 *      every ResultSet requested by the client. <p>
 *
 * <li> avoid the client-side overhead of deriving less commonly used values
 *      where possible, by ensuring that they are calculated if and only if
 *      the client actually calls a dependent method. <p>
 *
 * <li> avoid repeatedly deriving values in cases where a particular method
 *      may be called more than once, relative to a particular column
 *      index. <p>
 *
 * <li> given 1.) through 3.) above, also avoid greater than one call back to
 *      the database to obtain further information, in cases where requested
 *      values are neither transmitted with the parent ResultSet or directly
 *      derivable from them.
 * </ol> <p>
 *
 * In other words, the overall goal was to avoid or delay all costs associated
 * with obtaining derived ResultSetMetaData values of secondary importance
 * until made necessary in direct response to specific client requests, while
 * at the same time avoiding accumulation of costs due to repeated client
 * requests, most importantly network round-trips and redundant
 * SQL-processing. <p>
 *
 * The current partion is as follows: <p>
 *
 * <ol>
 * <li>Core<p>
 *
 *     Core ResultSetMetaData values are essentially those that are passed
 *     directly from the engine to the client connection a part of retrieving
 *     a result set.  The only exception is the data type name value, which is
 *     included in this group none-the-less, since it is derived using a core
 *     class (Column) and is considered of core importance and frequency of
 *     use. <p>
 *
 *     Core values are initialized at the instant of construction.<p>
 *
 *     ResultSetMetaData methods retrieving Core values are: <p>
 *
 *     <ol>
 *     <li>{@link #getColumnCount() getColumnCount()}
 *     <li>{@link #getTableName(int) getTableName()}
 *     <li>{@link #getColumnName(int) getColumnName()}
 *     <li>{@link #getColumnLabel(int) getColumnLabel()}
 *     <li>{@link #getColumnType(int) getColumnType()}
 *     <li>{@link #getColumnTypeName(int) getColumnTypeName()}
 *     </ol> <p>
 *
 * <li>Derived<p>
 *
 *     Derived ResultSetMetaData values are essentially those that can
 *     be calculated with complete accuracy, directly from the information
 *     transmitted with this object's parent ResultSet.  The only potential
 *     future exception is the precision value, which may eventually require
 *     a call back to the engine.  However, since HSQLDB does not currently
 *     guarantee that table column create parameters are strictly enforced
 *     under all conditions, the derivation instead uses the statically known
 *     maximum length or numeric precision associated with a column's data
 *     type. <p>
 *
 *     All Derived values are initialized together, just once, at first call to
 *     any method retrieving a Derived value. <p>
 *
 *     ResultSetMetaData methods retrieving Derived values are: <p>
 *
 *     <ol>
 *     <li>{@link #isCaseSensitive(int) isCaseSensitive()}
 *     <li>{@link #isSearchable(int) isSearchable()}
 *     <li>{@link #isSigned(int) isSigned()}
 *     <li>{@link #getColumnDisplaySize(int) getColumnDisplaySize()}
 *     <li>{@link #getPrecision(int) getPrecision()}
 *     <li>{@link #getColumnClassName(int) getColumnClassName()}
 *     </ol> <p>
 *
 *     The methods {@link #isCurrency(int) isCurrency()} and
 *     {@link #getScale(int) getScale()} are not listed above at this time
 *     because they always return false and 0, respectively, due to lack of
 *     support for true fixed (precision,scale) data types in the engine.
 *     Initialization of these particular values currently depends upon
 *     the Java default member attribute initialization policy; there is no
 *     code that explicitly performs their initialization. <p>
 *
 * <li>Connection-dependent<p>
 *
 *     Connection-dependent ResultSetMetaData values are essentially those that
 *     are neither transmitted with this object's parent ResultSet or Derived,
 *     as described above.  Instead, a call must be made back to the engine to
 *     retrieve such values, since they naturally originate from metadata
 *     found in system tables. In this implementation of ResultSetMetaData, a
 *     builtin stored procedure is called that works directly with the internal
 *     structures of the database and produces the same result as selecting the
 *     required information from sytem tables. This is done because it is far
 *     faster and far more efficient to do so. <p>
 *
 *     All Connection-dependent values are initialized together, just once, at
 *     first call to any method retrieving a Connection-dependent value.
 *     If the initialization fails, reporting falls back to 1.7.1 handling,
 *     as described in the documentation for each method retrieving a
 *     onnection-dependent value. <p>
 *
 *     ResultSetMetaData methods retrieving Connection-dependent values may
 *     be the following: <p>
 *
 *     <ol>
 *     <li>{@link #isAutoIncrement(int) isAutoIncrement()}
 *     <li>{@link #isNullable(int) isNullable()}
 *     <li>{@link #getSchemaName(int) getSchemaName()}
 *     <li>{@link #getCatalogName(int) getCatalogName()}
 *     <li>{@link #isReadOnly(int) isReadOnly()}
 *     <li>{@link #isWritable(int) isWritable()}
 *     </ol><p>
 *
 *     But it may also be possible by the time 1.7.2 reaches production release
 *     status that one will be able to override this behaviour via client
 *     properties such that the previous 1.7.1 handling is always used and
 *     calls back to the engine never occur in support of accurate reporting of
 *     Connection-dependent values. <p>
 *
 *     The method {@link #isDefinitelyWritable(int) isDefinitelyWritable()} is
 *     not listed above at this time because, in this implementation, it always
 *     returns false.  This descision stems from the fact that, under unique
 *     and foreign key constraints (not to mention triggers and other factors)
 *     the correct answer to this question cannot always be made
 *     deterministically and would be very resource intensive to make,
 *     regardless. Initialization of this value to false currently depends upon
 *     the Java default member attribute initialization policy; there is no 
 *     code that explicitly initializes this value.
 * </ol> <p>
 *
 * The decision to make the Connection-dependent value partition a one-shot
 * initialization and delay that initialization until a first dependent method
 * invocation by the client is of special importance for a number of 
 * reasons: <p>
 *
 * <ol>
 * <li> This implies that the state of the database may become different than
 *      the cached connection-dependent values indicate.
 * <li> It also implies that the intialization may actually fail, in the case
 *      where the parent connection is closed before a first dependent method
 *      invocation.
 * </ol>
 *
 * However, this descision is justified by a number of observations regarding
 * typical use patterns, as well as by benchmark timings noted during
 * the development of this class. Specifically, it is expected that,
 * under most applications, only a subset of the ResultSetMetaData values will
 * ever be requested. It is also expected that such subsets are most likely 
 * to include either none of the connection-dependent values or practically 
 * all of them. So it is deemed highly desirable to avoid retrieving related 
 * data until a request is actually made by the client to obtain a dependent 
 * value. But at the same time it is deemed highly desirable to try to 
 * ameliorate the retrieval of the values into a single call back to the 
 * database, in order to avoid excessive setup, network round trips and SQL 
 * statement processing.  This is especially true, given that timings noted 
 * during development indicated that, on a moderately powerful workstation 
 * under the best case (a connection to an embedded mode database instance),
 * there is a fixed ~5-10 ms overhead associated with setup and database 
 * call back to determine controlling information (such as whether or not to
 * report catalog and schema values), plus an additional best case ~2-10 ms 
 * overhead associated with making a number of system table query calls back 
 * to the database for each column, in order to retrieve its 
 * connection-dependent metadata.  Under such figures, it is easy understand 
 * that failing to carefully chose the correct design can instantly lead to 
 * completely unacceptable performance. <p>
 *
 * Because it is expected that the database state will not change frequently
 * regarding connection-dependent values in a production environment, it was
 * felt that a one-shot initialization does not impose a significant burden on
 * the end-developer and yeilds justifyable advantages.  Further, a change in
 * the values of either isNullable() or isAutoIncrement() currently implies that
 * either a column or an entire table has been dropped and recreated, leaving
 * isReadOnly() and isWritable() as the only values that might change and be
 * desirable to refresh without also retrieving a new ResultSet representing
 * a re-execution of the original query.  In fact, because HSQLDB does not yet
 * support ResultSet.CONCUR_UPDATABLE or any of the ResultSet.rowXXX methods,
 * it is (in general) practically demanded that the parent ResultSet be
 * regenerated by re-executing its parent Statement, any time it is suspected
 * or known that the database state has changed in some way since last execute
 * and fetch cyle. Of course, if it is suspected or known that
 * connection-dependent values have changed since constructing and fully reading
 * a jdbcResultSetMetaData object, it is a simple matter (as long as the parent
 * ResultSet and Connection are still open) to request a new ResultSetMetaData
 * instance from the parent ResultSet and re-read the connection-dependent
 * values from the new instance. <p>
 *
 * This implementation is not yet written in stone; all critisisms and
 * helpful suggestions welcomed. <p>
 *
 * (boucherb@users) <p>
 *
 * </span>
 * <!-- end release-specific documentation -->
 * @see jdbcStatement#executeQuery
 * @see jdbcStatement#getResultSet
 * @see java.sql.ResultSetMetaData
 */
public class jdbcResultSetMetaData implements ResultSetMetaData {
    
    /** The minimum value that this object returns in response to
     * calling {@link getDisplaySize(int) getDisplaySize()}.
     */       
    public static final int MIN_DISPLAY_SIZE = 6;
    /** The maximum value that this object returns in response to
     * calling {@link getDisplaySize(int) getDisplaySize()}.
     */    
    public static final int MAX_DISPLAY_SIZE = 255;
    
    /** The maximum number of rows in this object's parent ResultSet that
     * will be scanned to calculate an approximation of
     * {@link getDisplaySize(int) getDisplaySize()}, when the value is not to
     * be determined statically from the known maximum length or
     * precision of the column's data type.
     */    
    public static final int MAX_SCAN = 512;
    
    /** The Connection object from which the content of this object's parent
     * ResultSet was retrieved.
     */    
    private Connection              conn;
    /** The row content (if any) of this object's parent ResultSet. */    
    private Result                  rResult;
    /** An array of objects, each of which represents the reported attributes for
     * a single column of this object's parent ResultSet.
     */    
    private jdbcColumnMetaData[]    acmd;
    /** The number of columns in this object's parent ResultSet. */    
    private int                     columnCount;
    /** Flag determining whether getColumnName(int) retrieves the column name (true)
     * or column label (false).
     */    
    private boolean                 useColumnName;
    /** Flag determining whether certain connection-dependent methods
     * throw an exception or provide a strict or reasonable default
     * value if the accurate value cannot be retrieved through
     * this object's parent Connection for some reason.
     */    
    private boolean                 strictMetaData;
    /** Flag guarding against multiple initializations of the return
     * values that depend only on statically known and derivable
     * information.
     */    
    private boolean                 derivedInitialized;
    /** Flag guarding against multiple initializations of the return
     * values that depend on additional information not transmitted
     * with this object's parent ResultSet and that, consequentially,
     * must be retrieved at some later point through the Connection that
     * retrieved this object's parent ResultSet.
     */    
    private boolean                 cDependentInitialized;
    /** Flag indicating the success or failure of the single initialization
     * of connection-dependent return values.
     */    
    private boolean                 reportCDependent;
    
    /** For trace times. */    
    private StopWatch               sw;
    
    /** if true, then timings for initializations are printed to the console. */    
    private static final boolean TRACE = false;
    
    /** Constructs a new jdbcResultSetMetaData object for the specified
     * jdbcResultSet object.
     * @param rs the jdbcResultSet object for which to construct a new
     *        jdbcResultSetMetaData object
     * @throws SQLException if a database access error occurs
     */    
    jdbcResultSetMetaData(jdbcResultSet rs) throws SQLException { 
        Statement          stmnt;
        jdbcColumnMetaData cmd;
        
        if (TRACE) {
            sw = new StopWatch();
        }
        
        Trace.doAssert(rs != null, "result set is null"); 
                              
        rResult = rs.rResult;
        
        if (rResult != null && rResult.iMode != Result.DATA) {
            // Not a DATA mode result; it has no columns
            // No further init required, as setting columnCount to
            // zero ensures that checkColumn will guard against 
            // entry into sections attmepting to 
            // access uninitialized member attributes.
            columnCount = 0;            
            return;
        }   
        
        columnCount = rs.iColumnCount;
        
        if (columnCount == 0) {
            // still not a DATA mode result; as above
            return;
        }
        
        Trace.doAssert(rResult != null, "result set is closed");                
        
        // Typically, these assertions are not required, but they are
        // not guaranteed to be true either.  It is far more terse,
        // code-wise, to do the checks here and fail, than to place
        // all related code further along in try-catch blocks and wrap 
        // things like array bounds and null pointer exceptions with 
        // SQLExceptions.
        
        Trace.doAssert(
            rResult.colType != null && rResult.colType.length >= columnCount,
            "rResult.colType array is null or too small");
        
        Trace.doAssert(
            rResult.sTable != null && rResult.sTable.length >= columnCount,
            "rResult.sTable array is null or too small");        
        
        Trace.doAssert(
            rResult.sLabel != null && rResult.sLabel.length >= columnCount,
            "rResult.sLabel array is null or too small"); 
        
        Trace.doAssert(
            rResult.sName != null && rResult.sName.length >= columnCount,
            "rResult.sName array is null or too small");         
        
        stmnt = rs.getStatement();

        Trace.doAssert(stmnt != null, "statement is null");
        
        conn = stmnt.getConnection();
        
        Trace.doAssert(conn != null, "connection is null");        
        Trace.doAssert(!conn.isClosed(), "connection is closed");
                 
        strictMetaData    = rs.strictMetaData;
        acmd              = new jdbcColumnMetaData[columnCount];
        
// TODO: maybe some of this could be moved to the jdbcColumnMetaData.<init>?        
        for (int i = 0; i < columnCount; i++) {                        
            cmd                     = new jdbcColumnMetaData();            
            acmd[i]                 = cmd;
            cmd.catalogName         = "";
            cmd.schemaName          = "";
            // Typically, these null checks are not needed, but as
            // above, it is not _guaranteed_ that these values
            // will be non-null.   So, it is better to do the work
            // here than have to perform checks and conversions later.
            cmd.tableName           = rResult.sTable[i] == null 
                                        ? "" 
                                        : rResult.sTable[i];
            cmd.columnName          = rResult.sName[i] == null 
                                        ? "" 
                                        : rResult.sName[i];
            cmd.columnLabel         = rResult.sLabel[i] == null 
                                        ? "" 
                                        : rResult.sLabel[i];
            // TODO: No check on valid data type here.  implement?
            // default Java initialization dictates that this will
            // be zero if not explicitly set...  
            // zero maps to NULL or ANY data type, which is not handled
            // properly by our server row input/output interface 
            // implementations...this is a future concern for transmitting
            // call parameters back to the engine in the form of serialized 
            // Result objects
            cmd.columnType          = rResult.colType[i];
            // CHECKME: this might be null...it that OK? or do we want ""?
            cmd.columnTypeName      = Column.getTypeString(cmd.columnType);
            // This is the only explicit boolean initializer required, as 
            // the JVM's implicit false init value for boolean is the correct 
            // default for the other column metadata boolean attributes
            cmd.isReadOnly          = true; 
            // This is the only explicit int initializer required, as 
            // the JVM's implicit false init value for int (0) is the correct 
            // default for the other column metadata int attributes 
            // (i.e. precision and scale...display size must wait)
            cmd.isNullable          = columnNullableUnknown;
        } 
        
// boolean member attributes are false by default init anyway        
//        cDependentInitialized   = false;
//        reportCDependent        = false;
//        derivedInitialized      = false;
        
        if (TRACE) {
            Trace.printSystemOut(sw.elapsedTimeToMessage(this + ".<init>"));
        }
    }    

    /** <!-- start generic documentation -->
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

    /** <!-- start generic documentation -->
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
     * an application must not assume and cannot determine in a standard way that
     * auto-increment values start at any particular point, increment by any
     * particular value or have any of the other attributes generally associated
     * with SQL SEQUENCE generators. Further still, without full support for both
     * feature T174 and T176, if a unique value is supplied by an application or
     * provided by a declared or implicit default value expression, then whether
     * that value is used or substituted with one from the automatic unique value
     * source is implementation-defined and cannot be known in a standard way.
     * Finally, without full support for features T174 and T176, it is also
     * implementation-defined and cannot be know in a standard way whether an
     * exception is thrown or a unique value is automatically substituted when
     * an application or default value expression supplies a non-NULL,
     * non-unique value. <p>
     *
     * Up to and including HSQLDB 1.7.2, values supplied by an application or
     * default value expression are used if they are indeed non-NULL unique values,
     * while an exception is thrown if either possible value source for the site
     * attempts to supply a non-NULL, non-unique value.  This is very
     * likely to remain the behaviour of HSQLDB for its foreseable lifetime
     * and at the very least for the duration of the 1.7.x release series.<p>
     *
     * <hr>
     *
     * Regardless of the new and better support for reporting isAutoIncrement(),
     * it is still possible under certain conditions that accurate reporting may
     * be impossible. For example, if this object's parent Connection is closed
     * before the first call to this method or to any other method of this class
     * that initializes the connection-dependent  ResultSetMetaData values,
     * then it is impossible to report accurately for result set columns that
     * directly represent table column values  <p>
     *
     * Under such special circumstances, the result of calling this method
     * depends on whether the <code>jdbc.strict_md</code> connection property
     * was set true when this object was constructed. When the property is
     * true and the specified column index corresponds to a result set column
     * that directly represents table column values, then an
     * <code>SQLException</code> is thrown, stating that the function is not
     * supported. Othewise, false is returned, which is the correct value for
     * result set columns that do not directly represent table column values and
     * is the better substitute for "unknown" for result set columns that do. <p>
     *
     * Those wishing to determine the auto-increment status of a table column in
     * isolation from ResultSetMetaData can do so by inspecting the corresponding
     * value of the SYSTEM_COLUMNS.IS_IDENTITY BIT column which is also currently
     * included (in a fashion proprietary to HSQLDB) as the last column of the
     * jdbcDatabaseMetaData.getColumns() result.
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param column the first column is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean isAutoIncrement(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }
        
        checkColumn(column);
        initCDependent();  
        column--;
        if (!reportCDependent && strictMetaData && isTableColumn(column)) {
               throw jdbcDriver.notSupported;
        } 
        return acmd[column].isAutoIncrement;
    }

    /** <!-- start generic documentation -->
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
        initDerived();
        return acmd[--column].isCaseSensitive;
    }

    /** <!-- start generic documentation -->
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
        initDerived();
        return acmd[--column].isSearchable;
    }

    /** <!-- start generic documentation -->
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
        return acmd[--column].isCurrency;
    } 

    /** <!-- start generic documentation -->
     * Indicates the nullability of values in the designated column. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to 1.7.1, HSQLDB did not support accurately reporting this value. <p>
     *
     * Starting with 1.7.2, this feature is better supported.  <p>
     *
     * Regardless of the new and better support for reporting isNullable(),
     * it is still possible under certain conditions that accurate reporting may
     * be impossible. For example, if this object's parent Connection is closed
     * before the first call to this method or to any other method of this class
     * that initializes the connection-dependent ResultSetMetaData values,
     * then it is impossible to report accurately for result set columns that
     * directly represent table column values .<p>
     *
     * When it is impossible to accurately report this value for result set
     * columns that directly represent table column values, then the result of
     * calling this method depends on whether the <code>jdbc.strict_md</code>
     * connection property was set true when this object was constructed. When
     * the property is true, then columnNullableUnknown is returned. Othewise,
     * columnNullable is returned. <p>
     *
     * columnNullableUnknown is always returned for result set columns that
     * do not directly represent table column values (i.e. are calculated), while
     * whenever it is possible to successfully perform the connection-dependent
     * initialization partition, the corresponding value in SYSTEM_COLUMNS.NULLABLE
     * is returned for result set columns that do directly represent table column
     * values. <p>
     *
     * Those wishing to determine the nullable status of a table column in
     * isolation from ResultSetMetaData and in a DBMS-independent fashion
     * can do so by calling DatabaseMetaData.getColumns() with the appropriate
     * filter values and inspecting the result at the position described in the API
     * documentation. <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
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
        
        checkColumn(column);
        initCDependent();
        column--;
        if (!reportCDependent && isTableColumn(column)) {
            return strictMetaData
                ? columnNullableUnknown
                : columnNullable;
        }
        return acmd[column].isNullable;
                          
    }

    /** <!-- start generic documentation -->
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
     * @param column the first column is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean isSigned(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkColumn(column);
        initDerived();
        return acmd[--column].isSigned;
    }

    /** <!-- start generic documentation -->
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
     * For colums whose data type has a known, fixed maximum length or
     * precision, the following rules apply:
     *
     * <ol>
     * <li> if the value is in [0,MIN_DISPLAY_SIZE],  MIN_DISPLAY_SIZE is
     * reported.  <p>
     *
     * <li> if the value is in [MIN_DISPLAY_SIZE + 1,MAX_DISPLAY_SIZE],
     * the value itself is reported.
     * </ol> <p>
     *
     * In the standard distribution, MIN_DISPLAY_SIZE is 6, the minimum number of
     * characters required to display a character sequence representing the Java
     * String representation of null, bracketed with two additional characters
     * (e.g. "(null)"), while MAX_DISPLAY_SIZE is 255, the typical maximum size
     * for character display in graphical presentation manangers. <p>
     *
     * In all other cases, up to the first MAX_SCAN (512 in the standard
     * distribution) rows of the result set are scanned to calculate an
     * approximation of the maximum width, in characters, that would be required
     * to display the column's data if each value were retrieved as a Java String
     * using ResultSet.getString(). If the scan at any time determines that the
     * approximation will result in a value greater than or equal to
     * MAX_DISPLAY_SIZE, then the scan is terminated and MAX_DISPLAY_SIZE is
     * reported.  Othersize, the fully approximated display size is reported.
     * MIN_DISPLAY_SIZE is reported if the scan encounters that the parent result
     * set has no rows, while the minimum value calculated by the approximation is
     * also MIN_DISPLAY_SIZE, in the case where the first MAX_SCAN (or fewer) values
     * of the column are all null, zero-length or less than MIN_DISPLAY_SIZE
     * characters when retreived using ResultSet.getString().<p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param column the first column is 1, the second is 2, ...
     * @return the normal maximum number of characters allowed as the width
     *    of the designated column
     * @exception SQLException if a database access error occurs
     */
    public int getColumnDisplaySize(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }
        
        checkColumn(column);
        initDerived();        
        return acmd[--column].columnDisplaySize;
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
        return acmd[--column].columnLabel;
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
        
        return useColumnName 
            ? acmd[column].columnName
            : acmd[column].columnLabel;
    }

    /** <!-- start generic documentation -->
     * Get the designated column's table's schema. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to 1.7.1, HSQLDB did not support the notion of schemas at all, including
     * schema names in result set metadata and this method always returned "". <p>
     *
     * Staring with 1.7.2, this is supported, but only as an optional, experimental
     * feature that is disabled by default. Enabling this feature requires
     * manually adding an entry ("hsqldb.schemas=true") to the database properties
     * file and is thus unavailable in MEMORY mode database instances in the
     * default distribution. <p>
     *
     * Specifically, when this feature is enabled under 1.7.2, only very limited
     * support is provided by the engine for executing SQL containing
     * schema-qualified database object identifiers.  That is, when this feature
     * is enabled under 1.7.2, it is not yet possible in most cases to use what
     * would otherwise be the correct, canonical SQL calculated from result set
     * metadata. <p>
     *
     * Support, whether enabled or disabled, is not accurately indicated in the
     * corresponding DatabaseMetaData.supportsXXX() method return values and
     * likely will not be until it meets some minimum level of
     * standardization. <p>
     *
     * For greater detail, see discussion at: {@link jdbcDatabaseMetaData} <p>
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
        initCDependent();
        // will safely be "" here, under any combination of
        // 1.) initCDependent fails
        // 2.) not currently reporting schema names
        // 3.) result column does not directly correspond to a table column
        return acmd[--column].schemaName;
    }

    /** <!-- start generic documentation -->
     * Get the designated column's number of decimal digits. <p>
     * <!-- end generic documentation -->
     *
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <B>HSQLDB-Specific Information:</B> <p>
     *
     * Up to and including HSQLDB 1.7.1, this method always returned 0, which was
     * intended to convey that the precision was <em>unknown</em>,
     * </em>undefined</em> or that <em>no applicable limit</em> was imposed. <p>
     *
     * Starting with 1.7.2, HSQLDB reports the maximum length or
     * precision intrinsic to the data type of each result set column. <p>
     *
     * This method does not yet make any attempt to report the declared
     * length or precision specifiers for table columns (if they are defined,
     * which up to 1.7.2 is not a requirement in DDL), as these values may or
     * may not be enforced, depending on the value of the database property: <p>
     *
     * <pre>
     * sql.enforce_size
     * </pre>
     *
     * Because the property may change from one instantiation of a Database
     * to the next and because, when set true, is not applied to existing values
     * in table columns (only to new values introduced by following inserts and
     * updates), the length and/or precision specifiers for table columns still
     * do not neccessarily accurately reflect true constraints upon the contents
     * of the columns. This situation may or may not change in a future
     * release. <p>
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
        initDerived();    
        return acmd[--column].precision;
    }

     /** <!-- start generic documentation -->
      * Gets the designated column's number of digits to right of the
      * decimal point. <p>
      * <!-- end generic documentation -->
      *
      * <!-- start release-specific documentation -->
      * <span class="ReleaseSpecificDocumentation">
      * <B>HSQLDB-Specific Information:</B> <p>
      *
      * Up to and including HSQLDB 1.7.2, this method always returns 0 (which is the
      * actual scale for integral types and is to be interpreted as <em>unknown</em>
      * or <em>not applicable</em> for all other types). <p>
      *
      * HSQLDB currently implements DECIMAL and NUMERIC--the only HSQLDB types to
      * which this value would apply, if supported--as a thin wrap of BigDecimal and
      * thus does not presently ever enforce scale declarations.  Those wishing to
      * determine the declared--intended, but not enforced--scale of a DECIMAL and
      * NUMERIC table column should instead consult the DatabaseMetaData.getColumns()
      * result using the required filter parameters. <p>
      *
      * </span>
      * <!-- end release-specific documentation -->
      * @param column the first column is 1, the second is 2, ...
      * @return scale
      * @exception SQLException if a database access error occurs
      */
    public int getScale(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkColumn(column);    
        return acmd[--column].scale;
    }

    /** <!-- start generic documentation -->
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
        return acmd[--column].tableName;
    }

    /** <!-- start generic documentation -->
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
     * Starting with 1.7.2, HSQLDB supports this as an optional, experimental
     * feature only which is disabled by default. Enabling this feature requires
     * adding an entry ("hsqldb.catalogs=true") to the database properties file
     * and is thus unavailable in MEMORY mode database instances in the default
     * distribution. When enabled, the catalog name for table columns is reported
     * as the name by which the Database knows itself. <p>
     *
     * HSQLDB does not yet support any use of catalog qualification in DLL or DML.
     * This fact is accurately indicated in the corresponding
     * DatabaseMetaData.supportsXXX() method return values. <p>
     *
     * For greater detail, see discussion at: {@link jdbcDatabaseMetaData} <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param column the first column is 1, the second is 2, ...
     * @return the name of the catalog for the table in which the given column
     *     appears or "" if not applicable
     * @exception SQLException if a database access error occurs
     */
    public String getCatalogName(int column) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        checkColumn(column);
        initCDependent();
        // will safely be "" here, under any combination of
        // 1.) initCDependent fails
        // 2.) not currently reporting catalog name
        // 3.) column does not directly correspond to a table column        
        return acmd[--column].catalogName;
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
        int type =  acmd[--column].columnType;
        // TODO: 
        // we're trying to get away from reporting non-standard type codes
        // for what really amount to standard types with slighly different
        // behaviour.
        //
        // The data type name is still reported correctly as:
        //
        // "VARCHAR_IGNORECASE"
        //
        // ... and the fact that this column type is not case sensitive is
        // reflected properly in the return value of isCaseSensitive(), 
        // as well as in the SYSTEM_TYPEINFO and SYSTEM_COLUMNS system tables,
        // (by way of their newly introduced TYPE_SUB columns) so there is no 
        // good reason to report Column.VARCHAR_IGNORECASE (100) as the type 
        // code here.
        //
        // Thus eventually, we should transmit both the SQL type code and
        // the HSQLDB typeSub values with the Result.  We can, for instance,
        // encode these into a single value for transmission (e.g. using high 
        // bytes for type sub and low bytes for SQL type), reducing transport 
        // overhead.
        return type == Column.VARCHAR_IGNORECASE ? DITypes.VARCHAR : type;
    }

    /** <!-- start generic documentation -->
     * Retrieves the designated column's database-specific type name. <p>
     * <!-- end generic documentation -->
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
        return acmd[--column].columnTypeName;
    }

    /** <!-- start generic documentation -->
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
     * Regardless of the new and better support for reporting isReadOnly(),
     * it is still possible under certain conditions that accurate reporting may
     * be impossible. For example, if this object's parent Connection is closed
     * before the first call to this method or to any other method of this class
     * that initializes the connection-dependent ResultSetMetaData values,
     * then it is impossible to report accurately for result set columns that
     * directly represent table column values. <p>
     *
     * Under such special circumstances, the result of calling this method
     * depends on whether the <code>jdbc.strict_md</code> connection property
     * was set true when this object was constructed. When the property is
     * true and the specified column index corresponds to a result set column
     * that directly represents table column values, then an
     * <code>SQLException</code> is thrown, stating that the function is not
     * supported. Othewise, false is reported, which is the better choice
     * when this value is, in truth, "unknown" for a table column. <p>
     *
     * true is always reported for result set columns that do not directly
     * represent table column values (i.e. are calculated). <p>
     *
     * </span>
     * <!-- end release-specific documentation -->
     * @param column the first column is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean isReadOnly(int column) throws SQLException {
        checkColumn(column);
        initCDependent();
        column--;
        if (!reportCDependent && isTableColumn(column)) {
            if (!strictMetaData) {
                return false;
            } 
            throw jdbcDriver.notSupported;
        }
        return acmd[column].isReadOnly;
        
    }

    /** <!-- start generic documentation -->
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
     * Regardless of the new and better support for reporting isWritable(),
     * it is still possible under certain conditions that accurate reporting may
     * be impossible. For example, if this object's parent Connection is closed
     * before the first call to this method or to any other method of this class
     * that initializes the connection-dependent  ResultSetMetaData values,
     * then it is impossible to report accurately for result set columns that
     * directly represent table column values. <p>
     *
     * Under such special circumstances, the result of calling this method
     * depends on whether the <code>jdbc.strict_md</code> connection property
     * was set true when this object was constructed. When the property is
     * true and the specified column index corresponds to a result set column
     * that directly represents table column values, then an
     * <code>SQLException</code> is thrown, stating that the function is not
     * supported. Othewise, true is reported, which is the better choice
     * when this value is, in truth, "unknown" for a table column. <p>
     *
     * false is always reported for result set columns that do not directly
     * represent table column values (i.e. are calculated). <p>
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
        initCDependent();
        column--;
        if (!reportCDependent && isTableColumn(column)) {
            if (!strictMetaData) {
                return true;
            }
            throw jdbcDriver.notSupported;                      
        } 
        return acmd[column].isWritable;
    }

    /** <!-- start generic documentation -->
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
     * Starting with HSQLDB 1.7.2, this method always returns false, since
     * it is always false for calculated columns, is generally impossible
     * to calculate deterministically true for table columns under all conditions,
     * deemed to require too much overhead to even attempt to calculate accurately
     * for table columns and deemed to be currently of dubious usefulness,
     * regardless. <p>
     *
     * This situation may change at some point in the future, if,  for instance,
     * updateable result sets become supported, including "SELECT ... FOR UPDATE"
     * style locking.  However, this is not anticipated to occur any time in the
     * 1.7.x release series. <p>
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
        return acmd[--column].isDefinitelyWritable;
    }

    //--------------------------JDBC 2.0-----------------------------------

    /** <!-- start generic documentation -->
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

        checkColumn(column);
        initDerived();        
        return acmd[--column].columnClassName;
    }
    
// ------------------------- Internal Implementation ---------------------------
    
    /** Initializes connection-dependent return values not transmitted with this
     * object's parent ResultSet and that, consequentially, must be retrieved
     * at some later point through this object's parent connection.
     */    
    private void initCDependent() {        
        if (cDependentInitialized) {
            return;
        }
        cDependentInitialized = true;
        
        if (TRACE) {
            sw.zero();
        }
        
        try {
            initCDependentImpl();
            reportCDependent = true;
        } catch (Exception e) {
            //e.printStackTrace();
            reportCDependent = false;            
        }        
        // regardless of exception status, 
        // we don't need this anymore...
        // so free up for gc, just in case nobody 
        // else holds a reference either.
        conn = null;  
        
        if (TRACE) {
            Trace.printSystemOut(
                sw.elapsedTimeToMessage(this + ".initCDependent"));
        }
    }
    
    /** Initializes the return values that depend on statically
     * known information, possibly in combination with the
     * column values of this object's parent ResultSet.
     */    
    private void initDerived() {
        
        DITypeInfo          ti;
        ResultSet           rs;
        jdbcColumnMetaData  cmd;
        Result              rResult;
        
        if (derivedInitialized) {
            return;
        }
        derivedInitialized = true;
        
        // regardless of exception status below, 
        // we won't need this anymore...
        // so free up for gc, just in case nobody 
        // else holds a reference either.        
        
        rResult = this.rResult;
        this.rResult = null;
        
        if (TRACE) {
            sw.zero();
        }
        
        ti = new DITypeInfo();
        
        for (int i = 0; i < columnCount; i++) {
            
            cmd = acmd[i];
            
            int ditype     = cmd.columnType;
            int ditype_sub = DITypes.TYPE_SUB_DEFAULT;
        
            if (cmd.columnType == Column.VARCHAR_IGNORECASE) {
                ditype = DITypes.VARCHAR;
                ditype_sub = DITypes.TYPE_SUB_IGNORECASE;
            }
        
            ti.setTypeCode(ditype);
            ti.setTypeSub(ditype_sub);
            
            cmd.columnClassName = ti.getColStClsName();            
            Integer precision   = ti.getPrecision();
            cmd.precision       = precision == null ? 0 : precision.intValue();
            Boolean iua         = ti.isUnsignedAttribute();
            cmd.isSigned        = iua != null && !iua.booleanValue();
            Boolean ics         = ti.isCaseSensitive();
            cmd.isCaseSensitive = ics != null && ics.booleanValue();
            Integer sc          = ti.getSearchability();
            cmd.isSearchable    = sc != null && 
                    sc.intValue() != DatabaseMetaData.typePredNone;
            

            if (cmd.precision <= MIN_DISPLAY_SIZE){
                cmd.columnDisplaySize = MIN_DISPLAY_SIZE;
            } else if (cmd.precision <= MAX_DISPLAY_SIZE){
                cmd.columnDisplaySize = cmd.precision;
            } else {
                
                String  val;
                int     rc;
                int     len;
                int     max;
                                
                rc  = 0;
                max = MIN_DISPLAY_SIZE;
                
                try {
                    rs = new jdbcResultSet(rResult,null);
                    while (rs.next() && rc < MAX_SCAN) {                        
                        val = rs.getString(i+1);
                        
                        if (val != null) {
                            len = val.length();
                            if (len >= MAX_DISPLAY_SIZE) {
                                max = MAX_DISPLAY_SIZE;
                                break;
                            } else if (len > max) {
                                max = len;
                            }
                        }
                        rc++;
                    }
                } catch (Exception e) { /* Should never happen */}
                cmd.columnDisplaySize = max;
            }
        }
        
        if (TRACE) {
            Trace.printSystemOut(
                sw.elapsedTimeToMessage(this + ".initDerived"));
        }
    }
    

    /** Internal implementation for initializing connection-dependent return
     * values not transmitted with this object's parent ResultSet and that,
     * consequentially, must be retrieved at some later point through this
     * object's parent Connection object.
     * @throws Exception If a database access error occurs
     */    
    private void initCDependentImpl() throws Exception {
              
        ResultSet           rs;
        jdbcColumnMetaData  cmd;
        StringBuffer        sb;
        HsqlArrayList       tcn;
        int                 i;
        int                 size;
                
        // required init
        tcn         = new HsqlArrayList(); 
        sb          = null;

        for (i = 0; i < columnCount; i++) {
            if (isTableColumn(i)) {
                if (tcn.size() == 0) {
                    // this may never happen:
                    // we have encountered the first 
                    // candidate table column descriptor 
                    // so now we need to start building the list
                    // of (table,column) pairs in the string buffer
                    sb = new StringBuffer();                    
                } else {
                  sb.append(',');  
                }
                tcn.add(ValuePool.getInt(i));
                cmd = acmd[i];
                sb.append(Column.createSQLString(cmd.tableName));
                sb.append(',');
                sb.append(Column.createSQLString(cmd.columnName));
            }
        }
        
        size = tcn.size();
        
        if ( size > 0) {
            String sql = 
                "call \"org.hsqldb.Library.getCDColumnMetaData\"(" +
                Column.createSQLString(sb.toString()) + 
                ")";
            rs = conn.createStatement().executeQuery(sql);
            i = 0;
            while (rs.next() && i < size) {
               cmd                  = acmd[((Integer)tcn.get(i++)).intValue()];
               cmd.catalogName      = rs.getString(1);
               cmd.schemaName       = rs.getString(2);
               cmd.isAutoIncrement  = rs.getBoolean(3);
               cmd.isNullable       = rs.getBoolean(4) 
                                          ? columnNullable 
                                          : rs.wasNull() 
                                              ? columnNullableUnknown 
                                              : columnNoNulls;
              cmd.isReadOnly        = rs.getBoolean(5);
              cmd.isWritable        = !cmd.isReadOnly;
            }
        }
    }    
    
    /** Performs an internal check for column index validity.
     * @param column index of column to check
     * @throws SQLException when this object's parent ResultSet has 
     * no such column
     */
    private void checkColumn(int column) throws SQLException {        
        
        if (column < 1 || column > columnCount) {
            throw Trace.error(Trace.COLUMN_NOT_FOUND, column);
        }
    }
    
    private boolean isTableColumn(int column) {
        return 
        acmd[column].tableName.length() > 0 && 
        acmd[column].columnName.length() > 0;
    }
}
