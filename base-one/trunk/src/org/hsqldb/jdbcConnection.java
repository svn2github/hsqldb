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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.sql.*;     // for Savepoint
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.*;    // for Map
import org.hsqldb.lib.StringConverter;

// fredt@users 20020320 - patch 1.7.0 - JDBC 2 support and error trapping
// JDBC 2 methods can now be called from jdk 1.1.x - see javadoc comments
// boucherb@users 20020509 - added "throws SQLException" to all methods where
// it was missing here but specified in the java.sql.Connection interface,
// updated generic documentation to JDK 1.4, and added JDBC3 methods and docs
// boucherb@users and fredt@users 20020505 - extensive review and update
// of docs and behaviour to comply with java.sql specification
// fredt@users 20020830 - patch 487323 by xclayl@users - better synchronization
// fredt@users 20020930 - patch 1.7.1 - support for connection properties
// kneedeepincode@users 20021110 - patch 635816 - correction to properties
// unsaved@users 20021113 - patch 1.7.2 - SSL support
// fredt@users 20030620 - patch 1.7.2 - reworked to use a SessionInterface

/**
 * <!-- start generic documentation -->
 * A connection (session) with a specific database. Within the context
 * of a Connection, SQL statements are executed and results
 * are returned. <p>
 *
 * A Connection's database is able to provide information describing
 * its tables, its supported SQL grammar, its stored procedures, the
 * capabilities of this connection, and so on. This information is
 * obtained with the <code>getMetaData</code> method. <p>
 *
 * <B>Note:</B> By default the Connection automatically commits
 * changes after executing each statement. If auto commit has been
 * disabled, an explicit commit must be done or database changes will
 * not be saved. <p>
 *
 * <!-- end generic documentation -->
 * <!-- start release-specific documentation -->
 * <span class="ReleaseSpecificDocumentation">
 * <b>HSQLDB-Specific Information:</b> <p>
 *
 * To get a <code>Connection</code> to an HSQLDB database, the
 * following code may be used: <p>
 *
 * <code class="JavaCodeExample">
 * Class.<b>forName</b> (<span class="JavaStringLiteral">
 * "org.hsqldb.jdbcDriver"</span> );<br>
 * Connection c = DriverManager.<b>getConnection</b>
 * (url,user,password); </code><p>
 *
 * For HSQLDB connections, the <B>url</B> must start with <B>
 * 'jdbc:hsqldb'</B> .<p>
 *
 * The {@link Server Server} database <B>url</B> is <B>
 * 'jdbc:hsqldb:hsql://host[:port]'</B> <BR>
 * OR<BR>
 * <B>'jdbc:hsqldb:hsqls://host[:port]'</B> (with TLS). <p>
 *
 * The {@link WebServer WebServer} database <B>url</B> is <B>
 * 'jdbc:hsqldb:http://host[:port]'</B> <BR>
 * OR<BR>
 * <B>'jdbc:hsqldb:https://host[:port]'</B> (with TLS). <p>
 *
 * The In-Memory (diskless, in-process) database <B>url</B> is <B>
 * 'jdbc:hsqldb:.'</B> . <p>
 *
 * The Standalone (in-process) database connection <B>url</B> is <B>
 * 'jdbc:hsqldb:name'</B> . <p>
 *
 * <B>'name'</B> is the common prefix of the files that compose the
 * database, including the <B>path</B> .<p>
 *
 * For example: <B>'jdbc:hsqldb:test'</B> connects to a database
 * named <B>'test'</B> , which is composed of the files <B>
 * 'test.properties'</B> , <B>'test.data'</B> and <B>'test.script'
 * </B>, all located in the working directory fixed at the time the
 * JVM is started. <p>
 *
 * Under <em>Windows</em> <sup><font size="-2">TM</font> </sup> , <B>
 * 'jdbc:hsqldb:c:\databases\test'</B> connects to a database named
 * <B>'test'</B> , located on drive c: in the directory <B>
 * 'databases'</B> , composed of the files: <B>'test.properties'</B>
 * , <B>'test.data'</B> and <B>'test.script'</B> . <p>
 *
 * Under most variations of UNIX, <B>'jdbc:hsqldb:/databases/test'
 * </B> connects to a database named <B>'test'</B> located in the
 * directory <B>'databases'</B> directly under root, and composed of
 * the files: <B>'test.properties'</B> , <B>'test.data'</B> and <B>
 * 'test.script'</B> . <p>
 *
 * <B>Some Guidelines:</B>
 * <OL>
 * <LI> Both relative and absolute paths are supported.</LI>
 * <LI> Relative paths can be specified in a platform independent
 * manner as: <B>'[dir1/dir2/.../dirn/]name'.</B> </LI>
 * <LI> Specification of absolute paths is operating-system
 * specific.<br>
 * Please read your OS file system documentation.</LI>
 * <LI> Typically, special care must be taken w.r.t. path
 * specifications containing whitespace, and mixed-case may also be
 * a concern.<br>
 * Please read your OS file system documentation.</LI>
 * </OL>
 * <B>Note:</B> Previous versions of HSQLDB did not support creating
 * directories along the path specified in Standalone mode jdbc urls,
 * in the case that they did not already exist. As of HSQLDB 1.7.0,
 * directories <i>will</i> be created if they do not already exist
 * (but not if the HSQLDB Jar is built under JDK 1.1.x).
 * <p>
 *
 * For more information about HSQLDB file structure, please read the
 * {@link Files Files} section of the documentation. <p>
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
 * However, some of these method calls require <code>int</code> values that
 * are defined only in the JDBC 2 or greater version of
 * <a href="http://java.sun.com/j2se/1.4/docs/api/java/sql/ResultSet.html">
 * <code>ResultSet</code></a> interface.  For this reason, when the
 * product is compiled under JDK 1.1.x, these values are defined
 * in {@link org.hsqldb.jdbcResultSet jdbcResultSet}. <p>
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
 * </span> <!-- end release-specific documentation -->
 *
 * @see jdbcDriver
 * @see jdbcStatement
 * @see jdbcResultSet
 * @see jdbcDatabaseMetaData
 */
public class jdbcConnection implements Connection {

// ---------------------------- Common Attributes --------------------------

    /**
     * Properties for the connection
     *
     */
    HsqlProperties connProperties;

    /**
     * This connection's interface to the corresponding Session
     * object in the database engine.
     */
    SessionInterface sessionProxy;

    /**
     * Is this an internal connection.
     *
     */
    boolean isInternal;

// ----------------------------------- JDBC 1 -------------------------------

    /**
     * <!-- start generic documentation -->
     * Creates a <code>Statement</code>
     * object for sending SQL statements to the database. SQL
     * statements without parameters are normally executed using
     * <code>Statement</code> objects. If the same SQL statement is
     * executed many times, it may be more efficient to use a
     * <code>PreparedStatement</code> object.<p>
     *
     * Result sets created using the returned <code>Statement</code>
     * object will by default be type <code>TYPE_FORWARD_ONLY</code>
     * and have a concurrency level of <code>CONCUR_READ_ONLY</code>.<p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * The standard java.sql API documentation above suggests that if
     * the same SQL statement is executed many times, it may be more
     * efficient to use a <code>PreparedStatement</code> object. As
     * of HSQLDB 1.7.0, this is still not the case. However, this
     * feature <I>is</I> slated to be part of the HSQLDB 1.7.x
     * series. <p>
     *
     * Up to 1.6.1, HSQLDB supported <code>TYPE_FORWARD_ONLY</code> -
     * <code>CONCUR_READ_ONLY</code> results only, so <code>ResultSet</code>
     * objects created using the returned <code>Statement</code>
     * object would <I>always</I> be type <code>TYPE_FORWARD_ONLY</code>
     * with <code>CONCUR_READ_ONLY</code> concurrency. <p>
     *
     * Starting with 1.7.0, HSQLDB also supports
     * <code>TYPE_SCROLL_INSENSITIVE</code> results. <p>
     *
     * <b>Notes:</b> <p>
     *
     * Up to 1.6.1, calling this method returned <code>null</code> if the
     * connection was already closed. This was possibly counter-intuitive
     * to the expectation that an exception would be thrown for
     * closed connections. Starting with 1.7.0. the behaviour is to throw a
     * <code>SQLException</code> if the connection is closed. <p>
     *
     * </span> <!-- end release-specific documentation -->
     *
     * @return a new default Statement object
     * @throws SQLException if a database access error occurs<p>
     * @see #createStatement(int,int)
     * @see #createStatement(int,int,int)
     */
    public Statement createStatement() throws SQLException {

        checkClosed();

        return new jdbcStatement(this, jdbcResultSet.TYPE_FORWARD_ONLY);
    }

    /**
     * <!-- start generic documentation -->
     * Creates a <code>PreparedStatement</code>
     * object for sending parameterized SQL statements to the
     * database. <p>
     *
     * A SQL statement with or without IN parameters can be
     * pre-compiled and stored in a <code>PreparedStatement</code>
     * object. This object can then be used to efficiently execute
     * this statement multiple times. <p>
     *
     * <B>Note:</B> This method is optimized for handling parametric
     * SQL statements that benefit from precompilation. If the driver
     * supports precompilation, the method <code>prepareStatement</code>
     * will send the statement to the database for precompilation.
     * Some drivers may not support precompilation. In this case, the
     * statement may not be sent to the database until the
     * <code>PreparedStatement</code> object is executed. This has no
     * direct effect on users; however, it does affect which methods
     * throw certain <code>SQLException</code> objects.<p>
     *
     * Result sets created using the returned <code>PreparedStatement</code>
     * object will by default be type <code>TYPE_FORWARD_ONLY</code>
     * and have a concurrency level of <code>CONCUR_READ_ONLY</code>.<p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * The standard java.sql API documentation above suggests that if
     * the same SQL statement is executed many times, it may be more
     * efficient to use a <code>PreparedStatement</code> object. From
     * HSQLDB 1.7.2, precompilation is supported. In previous versins, the
     * statement was stored on the client and was not sent to the
     * database until the <code>PreparedStatement</code> object was
     * executed.<p>
     *
     * Up to 1.6.1, HSQLDB supported <code>TYPE_FORWARD_ONLY</code> -
     * <code>CONCUR_READ_ONLY</code> results only, so <code>ResultSet</code>
     * objects created using the returned <code>PreparedStatement</code>
     * object would <I>always</I> be type <code>TYPE_FORWARD_ONLY</code>
     * with <code>CONCUR_READ_ONLY</code> concurrency. <p>
     *
     * Starting with 1.7.0, HSQLDB also supports
     * <code>TYPE_SCROLL_INSENSITIVE</code> results. <p>
     *
     * <b>Notes:</b> <p>
     *
     * Up to 1.6.1, calling this method returned <code>null</code> if the
     * connection was already closed. Starting with 1.7.0. the behaviour is
     * to throw a <code>SQLException</code> if the connection is closed. <p>
     *
     * </span> <!-- end release-specific documentation -->
     *
     * @param sql an SQL statement that may contain one or more '?'
     *    IN parameter placeholders
     * @return a new default <code>PreparedStatement</code> object
     *    containing the pre-compiled SQL statement
     * @exception SQLException if a database access error occurs <p>
     * @see #prepareStatement(String,int,int)
     */
    public PreparedStatement prepareStatement(String sql)
    throws SQLException {

        checkClosed();

        try {
            return new jdbcPreparedStatement(this, sql,
                                             jdbcResultSet.TYPE_FORWARD_ONLY);
        } catch (HsqlException e) {
            throw jdbcDriver.sqlException(e);
        }
    }

    /**
     * <!-- start generic documentation -->
     * Creates a <code>CallableStatement</code>
     * object for calling database stored procedures. The
     * <code>CallableStatement</code> object provides methods for setting up
     * its IN and OUT  parameters, and methods for executing the call to a
     * stored procedure. <p>
     *
     * <b>Note:</b> This method is optimized for handling stored
     * procedure call statements. Some drivers may send the call
     * statement to the database when the method <code>prepareCall</code>
     * is done; others may wait until the <code>CallableStatement</code>
     * object is executed. This has no direct effect on users;
     * however, it does affect which method throws certain
     * SQLExceptions. <p>
     *
     * Result sets created using the returned <code>CallableStatement</code>
     * object will by default be type <code>TYPE_FORWARD_ONLY</code>
     * and have a concurrency level of <code>CONCUR_READ_ONLY</code>.<p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * The standard java.sql API documentation above suggests that if
     * the same stored procedure is executed many times, it may be
     * more efficient to use a <code>CallableStatement</code> object.
     * As of HSQLDB 1.7.0, this is still not the case. Rather, the
     * statement is stored on the client and is not sent to the
     * database until the <code>CallableStatement</code> object is
     * executed. However, protocol optimizations and statement
     * precompilation on the database for optimization of stored
     * procedure execution <I>are</I> a features slated to be part of
     * the 1.7.x series. <p>
     *
     * Up to and including 1.7.1, HSQLDB supports only the default
     * <code>TYPE_FORWARD_ONLY</code> - <code>CONCUR_READ_ONLY</code> for
     * results obtained from <code>CallableStatement</code> objects. <p>
     *
     * <B>Notes:</B> <p>
     *
     * Up to 1.6.1, calling this method returned <code>null</code> if the
     * connection was already closed. This was possibly counter-intuitive
     * to the expectation that an exception would be thrown for
     * closed connections. Starting with 1.7.0. the behaviour is to throw
     * a <code>SQLException</code> if the connection is closed.<p>
     *
     * Up to and including 1.7.1, each HSQLDB stored procedure returns
     * only a single value wrapped in a <code>ResultSet</code> object. That
     * is, HSQLDB stored procedures act very much like SQL functions
     * and can actually always be used in such a capacity. As such,
     * there is really no point in supporting anything but
     * <code>TYPE_FORWARD_ONLY</code>, since the result obtained by
     * executing a <code>CallableStatement</code> object has
     * always just one column and one row.  Be aware that this
     * behaviour will change in HSQLDB 1.7.1, in that support will be
     * added for Java stored procedures that return multi-column,
     * multi-row results. At that point, support will be added for
     * <code>CallableStatement</code> objects that return
     * <code>TYPE_SCROLL_INSENSITIVE</code> <code>ResultSet</code>
     * objects. <p>
     *
     * New to 1.7.0, HSQLDB now allows calling <code>void</code> Java
     * methods as SQL functions and stored procedures, the result being
     * a SQL <code>NULL</code> value or a result with one column and one
     * row whose single field is the SQL <code>NULL</code> value,
     * respectiviely.  Previously, calling such Java methods
     * in either context resulted in throwing a <code>SQLException</code>.
     *
     * Finally, up to and including 1.7.1, the returned
     * <code>CallableStatement</code> object does not support any
     * getXXX methods. That is, HSQLDB stored procedures do not
     * support <code>OUT</code> or <code>IN OUT</code> parameters. This
     * behaviour <I>may</I> change at some point in the 1.7.x series, but
     * no decisions have yet been made. <p>
     *
     * </span> <!-- end release-specific documentation -->
     *
     * @param sql a String object that is the SQL statement to be
     *  sent to the database; may contain one or more ?
     *  parameters. <p>
     *
     *  <B>Note:</B> Typically the SQL statement is a JDBC
     *  function call escape string.
     * @return a new default <code>CallableStatement</code> object
     *  containing the pre-compiled SQL statement
     * @exception SQLException if a database access error occurs <p>
     * @see #prepareCall(String,int,int)
     */
    public CallableStatement prepareCall(String sql) throws SQLException {

        checkClosed();

        try {
            return new jdbcPreparedStatement(this, sql,
                                             jdbcResultSet.TYPE_FORWARD_ONLY);
        } catch (HsqlException e) {
            throw jdbcDriver.sqlException(e);
        }
    }

    /**
     * <!-- start generic documentation -->
     * Converts the given SQL statement
     * into the system's native SQL grammar. A driver may convert the
     * JDBC SQL grammar into its system's native SQL grammar prior to
     * sending it. This method returns the native form of the
     * statement that the driver would have sent. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB converts the JDBC SQL
     * grammar into the system's native SQL grammar prior to sending
     * it; this method returns the native form of the statement that
     * the driver would send in place of client-specified JDBC SQL
     * grammar. <p>
     *
     * </span> <!-- end release-specific documentation -->
     *
     * @param sql a SQL statement that may contain one or more '?'
     *     parameter placeholders
     * @return the native form of this statement
     * @throws SQLException if a database access error occurs <p>
     */
    public String nativeSQL(String sql) throws SQLException {

        //boucherb@users 20030405
        //FIXME: does not properly escape even the JDBC escape syntax we
        //       say we support.
        //       e.g.  {call ...(...,{dt '...'},....)} does not work
        checkClosed();

        if (sql.indexOf('{') == -1) {
            return sql;
        }

        char    s[]     = sql.toCharArray();
        boolean changed = false;
        int     state   = 0;
        int     len     = s.length;

        for (int i = 0; i < len; i++) {
            char c = s[i];

            switch (state) {

                case 0 :    // normal
                    if (c == '\'') {
                        state = 1;
                    } else if (c == '"') {
                        state = 2;
                    } else if (c == '{') {
                        s[i]    = ' ';
                        changed = true;

                        String sub = sql.substring(i + 1).toUpperCase();

                        if (sub.startsWith("?=")) {
                            i += 2;
                        } else if (sub.startsWith("CALL")) {
                            i += 4;
                        } else if (sub.startsWith("ESCAPE")) {
                            i += 6;
                        }

                        state = 3;
                    }
                    break;

                case 1 :    // inside ' '
                case 5 :    // inside { } and ' '
                    if (c == '\'') {
                        state -= 1;
                    }
                    break;

                case 2 :    // inside " "
                case 6 :    // inside { } and " "
                    if (c == '"') {
                        state -= 2;
                    }
                    break;

                case 3 :    // inside { } before whitespace
                    if (c == ' ') {
                        state = 4;
                    } else {
                        s[i]    = ' ';
                        changed = true;
                    }
                    break;

                case 4 :    // inside { } after whitespace
                    if (c == '\'') {
                        state = 5;
                    } else if (c == '"') {
                        state = 6;
                    } else if (c == '}') {
                        s[i]    = ' ';
                        changed = true;
                        state   = 0;
                    }
            }
        }

        if (changed) {
            sql = new String(s);

            if (Trace.TRACE) {
                Trace.trace(s + " > " + sql);
            }
        }

        return sql;
    }

    /**
     * <!-- start generic documentation -->
     * Sets this connection's auto-commit mode to the given state.
     * If a connection is in auto-commit mode, then all its SQL
     * statements will be executed and committed as individual transactions.
     * Otherwise, its SQL statements are grouped into transactions that
     * are terminated by a call to either the method <code>commit</code> or
     * the method <code>rollback</code>. By default, new connections are
     * in auto-commit mode. <p>
     *
     * The commit occurs when the statement completes or the next
     * execute occurs, whichever comes first. In the case of
     * statements returning a <code>ResultSet</code> object, the
     * statement completes when the last row of the <code>ResultSet</code>
     * object has been retrieved or the <code>ResultSet</code> object
     * has been closed. In advanced cases, a single statement may
     * return multiple results as well as output parameter values. In
     * these cases, the commit occurs when all results and output
     * parameter values have been retrieved. <p>
     *
     * <B>NOTE:</B> If this method is called during a transaction,
     * the transaction is committed. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including HSQLDB 1.7.0, <p>
     *
     *
     * <OL>
     *   <LI> All rows of a result set are retrieved internally <I>
     *   before</I> the first row can actually be fetched.<br>
     *   Therefore, a statement can be considered complete as soon as
     *   any XXXStatement.executeXXX method returns. </LI>
     *   <LI> Multiple result sets and output parameters are not yet
     *   supported. </LI>
     * </OL>
     * <p>
     *
     * (boucherb@users) </span> <!-- end release-specific
     * documentation -->
     *
     * @param autoCommit <code>true</code> to enable auto-commit
     *     mode; <code>false</code> to disable it
     * @exception SQLException if a database access error occurs
     * @see #getAutoCommit
     */
    public void setAutoCommit(boolean autoCommit) throws SQLException {

        try {
            sessionProxy.setAutoCommit(autoCommit);
        } catch (HsqlException e) {
            throw jdbcDriver.sqlException(e);
        }
    }

    /**
     *  Gets the current auto-commit state.
     *
     * @return  the current state of auto-commit mode
     * @exception  SQLException Description of the Exception
     * @see  #setAutoCommit
     */
    public boolean getAutoCommit() throws SQLException {

        try {
            return sessionProxy.isAutoCommit();
        } catch (HsqlException e) {
            throw jdbcDriver.sqlException(e);
        }
    }

    /**
     * <!-- start generic documentation -->
     * Makes all changes made since the
     * previous commit/rollback permanent and releases any database
     * locks currently held by the Connection. This method should be
     * used only when auto-commit mode has been disabled. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * As of HSQLDB 1.7.0, SQL Savepoints are supported.  As such,
     * successfully calling this method now also removes all
     * Savepoints from this connection's {@link Session Session}. <p>
     *
     * Up to 1.6.1, HSQLDB did not support Savepoints in
     * transactions, named or anonymous. <p>
     *
     * As of 1.7.0, HSQLDB supports an arbitrary number of named
     * Savepoints per transaction and allows explicitly rolling back
     * to any one of them. At this time, HSQLDB does not support
     * anonymous Savepoints. However, this feature <i>is</i> slated
     * for the 1.7.x series. <p>
     *
     * Also, JDBC 3 support for java.sql.Savepoint has not yet been
     * implemented. At present, rather, the following SQL syntax must
     * be used: <p>
     *
     * <code class="JavaCodeExample">
     * SAVEPOINT savepoint_name1;<br>
     * ... -- perform some work<br>
     * SAVEPOINT savepoint_name2;<br>
     * ...-- perform some work<br>
     * ROLLABACK TO SAVEPOINT savepoint_name2<br>
     * ...-- perform some work<br>
     * ROLLABACK TO SAVEPOINT savepoint_name1; </code> <p>
     *
     * <B>Note:</B> If two or more Savepoints with the same name are
     * performed during the same transaction, the latest one replaces
     * the previous one, so that it is impossible to roll back to the
     * previous one. </span> <p>
     *
     * <!-- end release-specific documentation -->
     *
     * @exception SQLException if a database access error occurs
     * @see #setAutoCommit
     */
    public void commit() throws SQLException {

        try {
            sessionProxy.commit();
        } catch (HsqlException e) {
            throw jdbcDriver.sqlException(e);
        }
    }

    /**
     * <!-- start generic documentation -->
     * Drops all changes made since the
     * previous commit/rollback and releases any database locks
     * currently held by this Connection. This method should be used
     * only when auto- commit has been disabled. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * As of HSQLDB 1.7.0, SQL Savepoints are supported.  As such,
     * successfully calling this method also removes all Savepoints
     * from this <code>Connection</code>'s {@link Session Session}.
     * <p>
     *
     * Up to 1.6.1, HSQLDB did not support Savepoints in
     * transactions, named or anonymous. <p>
     *
     * As of 1.7.0, HSQLDB supports an arbitrary number of named
     * Savepoints per transaction and allows explicitly rolling back
     * to any one of them. At this time, HSQLDB does not support
     * anonymous Savepoints. However, this feature <i>is</i> slated
     * for the 1.7.x series. <p>
     *
     * Also, as of 1.7.0, JDBC 3 support for java.sql.Savepoint has
     * not yet been implemented. At present, rather, the following
     * SQL syntax must be used: <p>
     *
     * <code class="JavaCodeExample">
     * SAVEPOINT savepoint_name1;<br>
     * ...-- perform some work<br>
     * SAVEPOINT savepoint_name2;<br>
     * ...-- perform some work<br>
     * ROLLABACK TO SAVEPOINT savepoint_name2<br>
     * ...-- perform some work<br>
     * ROLLABACK TO SAVEPOINT savepoint_name1; </code> <p>
     *
     * <code>Note:</code> If two or more Savepoints with the same
     * name are performed during the same transaction, the latest one
     * replaces the previous one, making it impossible to roll back
     * to the previous one. </span> <p>
     *
     * <!-- end release-specific documentation -->
     *
     * @exception SQLException if a database access error occurs
     * @see #setAutoCommit
     */
    public void rollback() throws SQLException {

        try {
            sessionProxy.rollback();
        } catch (HsqlException e) {
            throw jdbcDriver.sqlException(e);
        }
    }

    /**
     * <!-- start generic documentation -->
     * Releases this <code>Connection</code>
     * object's database and JDBC resources immediately instead of
     * waiting for them to be automatically released.<p>
     *
     * Calling the method <code>close</code> on a <code>Connection</code>
     * object that is already closed is a no-op. <p>
     *
     * <B>Note:</B> A <code>Connection</code> object is automatically
     * closed when it is garbage collected. Certain fatal errors also
     * close a <code>Connection</code> object. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * In 1.7.2, <code>INTERNAL</code> <code>Connection</code>
     * objects are not closable from JDBC client code, and disconnect
     * statements issued on <code>INTERNAL</code> <code>Connection</code>
     * objects will be ignored. <p>
     *
     * </span> <!-- end release-specific documentation -->
     *
     * @exception SQLException if a database access error occurs
     */
    public void close() throws SQLException {

        if (isInternal) {
            return;
        }

        sessionProxy.close();

        sessionProxy = null;
    }

    /**
     *  Tests to see if a Connection is closed.
     *
     * @return  true if the connection is closed; false if it's still
     *      open
     */
    public boolean isClosed() {

        if (sessionProxy == null || sessionProxy.isClosed()) {
            return true;
        }

        return false;
    }

    /**
     * <!-- start generic documentation -->
     * Gets the metadata regarding this connection's database.
     * A Connection's database is able to  provide information describing
     * its tables, its supported SQL grammar, its stored procedures,
     * the capabilities of this connection, and so on. This information
     * is made available through a <code>DatabaseMetaData</code> object. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB does not provide accurate
     * results for the full range of <code>DatabaseMetaData</code>
     * methods returning <code>ResultSet</code>. Some of these
     * methods may always return empty result sets, even though they
     * should contain information. Other methods may not accurately
     * reflect all of the MetaData for the category they report on.
     * Also, some methods may ignore the filters provided as
     * parameters, returning an unfiltered result each time. <p>
     *
     * As of version 1.7.1, the only completely accurate
     * <code>DatabaseMetaData</code>
     * methods returning <code>ResultSet</code> are {@link
     * jdbcDatabaseMetaData#getTables getTables},
     * {@link jdbcDatabaseMetaData#getColumns getColumns},
     * {@link jdbcDatabaseMetaData#getColumns getPrimaryKeys},
     * and {@link jdbcDatabaseMetaData#getIndexInfo getIndexInfo}.
     * Also, the majority of methods returning <code>ResultSet</code>
     * throw a <code>SQLException</code> when accessed by a non-admin
     * user. In order to provide non-admin users access to these methods,
     * an admin user must explicitly grant SELECT to such users or to
     * the PUBLIC user on each HSQLDB system table corresponding to a
     * DatabaseMetaData method that returns <code>ResultSet</code>.
     * For example, to provide access to {@link
     * jdbcDatabaseMetaData#getTables getTables} to all users, the
     * following must be issued by an admin user:<p>
     *
     * <code class = "JavaCodeExample">
     * GRANT SELECT ON SYSTEM_TABLES TO PUBLIC
     * </code> <p>
     *
     * Under 1.7.1 and previous verersions of the database engine,
     * care should be taken when making such grants, however, since
     * HSQLDB makes no attempt to filter such information, based on
     * the grants of the accessing user. That is, in the example
     * above, getTables will return information about all tables
     * (except system tables, which are never listed in MetaData),
     * regardless of whether the calling user has any rights on any
     * of the tables. <p>
     *
     * <hr>
     *
     * Starting with HSQLDB 1.7.2, full metadate is supported. <p>
     *
     * For discussion in greater detail, please follow the link to the
     * overview for jdbcDatabaseMetaData, below.
     *
     * </span> <!-- end release-specific documentation -->
     *
     * @return a DatabaseMetaData object for this Connection
     * @throws SQLException if a database access error occurs
     * @see jdbcDatabaseMetaData
     * @see DatabaseInformation
     * @see DatabaseInformationMain
     * @see DatabaseInformationFull
     */
    public DatabaseMetaData getMetaData() throws SQLException {
        return new jdbcDatabaseMetaData(this);
    }

    /**
     * <!-- start generic documentation -->
     * Puts this connection in read-only mode as a hint to enable
     * database optimizations. <p>
     *
     * <B>Note:</B> This method should not be called while in the
     * middle of a transaction. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB will commit the current
     * transaction automatically when this method is called. <p>
     *
     * Additionally, HSQLDB provides a way to put a whole database in
     * read-only mode. This is done by manually adding the line
     * 'readonly=true' to the database's .properties file while the
     * database is offline. Upon restart, all connections will be
     * readonly, since the entire database will be readonly. To take
     * a database out of readonly mode, simply take the database
     * offline and remove the line 'readonly=true' from the
     * database's .properties file. Upon restart, the database will
     * be in regular (read-write) mode. <p>
     *
     * When a database is put in readonly mode, its files are opened
     * in readonly mode, making it possible to create CDROM-based
     * readonly databases. To create a CDROM-based readonly database
     * that has CACHED tables and whose .data file is suspected of
     * being highly fragmented, it is recommended that the database
     * first be SHUTDOWN COMPACTed, taken off-line, restarted,
     * SHUTDOWN and taken off-line again before copying the database
     * files to CDROM. This will reduce the space required and may
     * improve access times against the .data file which holds the
     * CACHED table data. <p>
     *
     * </span> <!-- end release-specific documentation -->
     *
     * @param readonly The new readOnly value
     * @exception SQLException if a database access error occurs
     */
    public void setReadOnly(boolean readonly) throws SQLException {

        try {
            sessionProxy.setReadOnly(readonly);
        } catch (HsqlException e) {
            throw jdbcDriver.sqlException(e);
        }
    }

    /**
     *  Tests to see if the connection is in read-only mode.
     *
     * @return  true if connection is read-only and false otherwise
     * @exception  SQLException if a database access error occurs
     */
    public boolean isReadOnly() throws SQLException {

        try {
            return sessionProxy.isReadOnly();
        } catch (HsqlException e) {
            throw jdbcDriver.sqlException(e);
        }
    }

    /**
     * <!-- start generic documentation -->
     * Sets a catalog name in order to
     * select a subspace of this Connection's database in which to
     * work. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB does not yet support catalogs and simply ignores this
     * request. <p>
     * </span>
     * <!-- end release-specific documentation -->
     *
     * @param catalog the name of a catalog (subspace in this
     *     Connection object's database) in which to work (Ignored)
     * @throws SQLException if a database access error occurs <p>
     */
    public void setCatalog(String catalog) throws SQLException {}

    /**
     * <!-- start generic documentation -->
     * Returns the Connection's current catalog name. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB does not yet support catalogs and always returns null.
     * <p>
     *
     * </span> <!-- end release-specific documentation -->
     *
     * @return the current catalog name or null <p>
     *
     *     For HSQLDB, this is always null.
     * @exception SQLException Description of the Exception
     */
    public String getCatalog() throws SQLException {

        checkClosed();

        return null;
    }

    /**
     * <!-- start generic documentation -->
     * Attempts to change the transaction isolation level for this
     * <code>Connection</code> object to the one given. The constants
     * defined in the interface <code>Connection</code> are the
     * possible transaction isolation levels. <p>
     *
     * <B>Note:</B> If this method is called during a transaction,
     * the result is implementation-defined. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB supports only
     * <code>Connection.TRANSACTION_READ_UNCOMMITTED</code>. <p>
     *
     * </span> <!-- end release-specific documentation -->
     *
     * @param level one of the following <code>Connection</code>
     *     constants: <code>Connection.TRANSACTION_READ_UNCOMMITTED</code>
     *     , <code>Connection.TRANSACTION_READ_COMMITTED</code>,
     *     <code>Connection.TRANSACTION_REPEATABLE_READ</code>, or
     *     <code>Connection.TRANSACTION_SERIALIZABLE</code>. (Note
     *     that <code>Connection.TRANSACTION_NONE</code> cannot be
     *     used because it specifies that transactions are not
     *     supported.)
     * @exception SQLException if a database access error occurs or
     *     the given parameter is not one of the <code>Connection</code>
     *     constants <p>
     * @see jdbcDatabaseMetaData#supportsTransactionIsolationLevel
     * @see #getTransactionIsolation
     */
    public void setTransactionIsolation(int level) throws SQLException {

        if (level != Connection.TRANSACTION_READ_UNCOMMITTED) {
            throw jdbcDriver.notSupported;
        }

        checkClosed();
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves this <code>Connection</code>
     * object's current transaction isolation level. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB always returns
     * <code>Connection.TRANSACTION_READ_UNCOMMITED</code>. <p>
     *
     * </span> <!-- end release-specific documentation -->
     *
     * @return the current transaction isolation level, which will be
     *    one of the following constants:
     *    <code>Connection.TRANSACTION_READ_UNCOMMITTED</code>
     *    , <code>Connection.TRANSACTION_READ_COMMITTED</code>,
     *    <code>Connection.TRANSACTION_REPEATABLE_READ</code>,
     *    <code>Connection.TRANSACTION_SERIALIZABLE</code>, or
     *    <code>Connection.TRANSACTION_NONE</code> <p>
     *
     *    Up to and including 1.7.1, TRANSACTION_READ_UNCOMMITTED is
     *    always returned
     * @exception SQLException if a database access error occurs <p>
     * @see jdbcDatabaseMetaData#supportsTransactionIsolationLevel
     * @see #setTransactionIsolation setTransactionIsolation
     */
    public int getTransactionIsolation() throws SQLException {

        checkClosed();

        return Connection.TRANSACTION_READ_UNCOMMITTED;
    }

    /**
     * <!-- start generic documentation -->
     * Retrieves the first warning reported by calls on this
     * <code>Connection</code> object. If there is more than one
     * warning, subsequent warnings will be chained to the first
     * one and can be retrieved by calling the method
     * <code>SQLWarning.getNextWarning</code> on the warning
     * that was retrieved previously. <p>
     *
     * This method may not be called on a closed connection; doing so
     * will cause an <code>SQLException</code> to be thrown. <p>
     *
     * <B>Note:</B> Subsequent warnings will be chained to this
     * SQLWarning. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including 1.7.1, HSQLDB never produces warnings,
     * always returns null.<p>
     *
     * </span> <!-- end release-specific documentation -->
     *
     * @return the first <code>SQLWarning</code> object or <code>null</code>
     *     if there are none<p>
     * @exception SQLException if a database access error occurs or
     *     this method is called on a closed connection <p>
     * @see SQLWarning
     */
    public SQLWarning getWarnings() throws SQLException {

        checkClosed();

        return null;
    }

    /**
     * An internal check for unsupported combinations of
     * <code>ResultSet</code> type and concurrency. <p>
     *
     * Up to and including HSQLDB 1.7.0, the only supported
     * combinations are type <code>TYPE_FORWARD_ONLY</code> or
     * <code>TYPE_SCROLL_INSENSITIVE</code>, combined with
     * concurrency <code>CONCUR_READ_ONLY</code>.
     *
     * @param type of <code>ResultSet</code>; one of
     *     <code>ResultSet.TYPE_XXX</code>
     * @param concurrency of <code>ResultSet</code>; one of
     *     <code>ResultSet.CONCUR_XXX</code>
     * @throws SQLException when the specified combination of type
     *     and concurrency is not supported
     */
    static void checkTypeConcurrency(int type,
                                     int concurrency) throws SQLException {

        if ((type != jdbcResultSet.TYPE_FORWARD_ONLY && type != jdbcResultSet
                .TYPE_SCROLL_INSENSITIVE) || concurrency != jdbcResultSet
                    .CONCUR_READ_ONLY) {
            throw jdbcDriver.notSupported;
        }
    }

    /**
     * An internal check for closed connections. <p>
     *
     * @throws SQLException when the connection is closed
     */
    void checkClosed() throws SQLException {

        if (sessionProxy == null || sessionProxy.isClosed()) {
            throw jdbcDriver.sqlException(Trace.CONNECTION_IS_CLOSED);
        }
    }

    /**
     * <!-- start generic documentation -->
     * Clears all warnings reported for this <code>Connection</code>
     * object. After a call to this method, the method
     * <code>getWarnings</code> returns null until
     * a new warning is reported for this Connection. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including HSQLDB 1.7.0, <code>SQLWarning</code> is not
     * supported, and calls to this method are simply ignored. <p>
     *
     * </span> <!-- end release-specific documentation -->
     *
     * @exception SQLException if a database access error occurs <p>
     */
    public void clearWarnings() throws SQLException {
        checkClosed();
    }

    //--------------------------JDBC 2.0-----------------------------

    /**
     * <!-- start generic documentation -->
     * Creates a <code>Statement</code> object that will generate
     * <code>ResultSet</code> objects with the given type and
     * concurrency. This method is the same as the
     * <code>createStatement</code> method above, but it allows the
     * default result set type and result set concurrency type to be
     * overridden. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to HSQLDB 1.6.1, support was provided only for type
     * <code>TYPE_FORWARD_ONLY</code>
     * and concurrency <code>CONCUR_READ_ONLY</code>. <p>
     *
     * Starting with HSQLDB 1.7.0, support is now provided for types
     * <code>TYPE_FORWARD_ONLY</code>, <I>and</I>
     * <code>TYPE_SCROLL_INSENSITIVE</code>,
     * with concurrency <code>CONCUR_READ_ONLY</code>. Specifying
     * any other values will throw a <code>SQLException</code>.<p>
     *
     * <B>Notes:</B> <p>
     *
     * Up to 1.6.1, calling this method returned <code>null</code> if the
     * connection was already closed and a supported combination of type and
     * concurrency was specified. This was possibly counter-intuitive
     * to the expectation that an exception would be thrown for
     * closed connections. Starting with 1.7.0. the behaviour is to throw a
     * <code>SQLException</code> if the connection is closed.<p>
     *
     * </span> <!-- end release-specific documentation -->
     *
     * @param type a result set type; one of
     *  <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *  <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *  <code>ResultSet.TYPE_SCROLL_SENSITIVE</code> (not
     *  supported)
     * @param concurrency a concurrency type; one of
     *  <code>ResultSet.CONCUR_READ_ONLY</code>
     *  or <code>ResultSet.CONCUR_UPDATABLE</code> (not supported)
     * @return a new <code>Statement</code> object that will, within
     *  the release-specific documented limitations of support,
     *  generate <code>ResultSet</code> objects with the given
     *  type and concurrency
     * @exception SQLException if a database access error occurs or
     *  the given parameters are not ResultSet constants
     *  indicating a supported type and concurrency
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *  for jdbcConnection)
     */
    public Statement createStatement(int type,
                                     int concurrency) throws SQLException {

        checkClosed();
        checkTypeConcurrency(type, concurrency);

        return new jdbcStatement(this, type);
    }

    /**
     * <!-- start generic documentation -->
     * Creates a <code>PreparedStatement</code>  object that will
     * generate <code>ResultSet</code> objects with the given type
     * and concurrency. This method is the same as the
     * <code>prepareStatement</code> method above, but it allows the
     * default result set type and result set concurrency type to be
     * overridden. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to HSQLDB 1.6.1, support was provided only for type
     * <code>TYPE_FORWARD_ONLY</code>
     * and concurrency <code>CONCUR_READ_ONLY</code>. <p>
     *
     * Starting with HSQLDB 1.7.0, support is now provided for types
     * <code>TYPE_FORWARD_ONLY</code>, <I>and</I>
     * <code>TYPE_SCROLL_INSENSITIVE</code>,
     * with concurrency <code>CONCUR_READ_ONLY</code>. Specifying
     * any other values will throw a SQLException.<p>
     *
     * <B>Notes:</B> <p>
     *
     * Up to 1.6.1, calling this method returned <code>null</code> if the
     * connection was already closed and a supported combination of type and
     * concurrency was specified. This was possibly counter-intuitive
     * to the expectation that an exception would be thrown for
     * closed connections. Starting with 1.7.0. the behaviour is to throw a
     * <code>SQLException</code> if the connection is closed.<p>
     *
     * </span> <!-- end release-specific documentation -->
     *
     * @param sql a String object that is the SQL statement to be
     *  sent to the database; may contain one or more ? IN
     *  parameters
     * @param type a result set type; one of
     *  <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *  <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *  <code>ResultSet.TYPE_SCROLL_SENSITIVE</code> (not
     *  supported)
     * @param concurrency a concurrency type; one of
     *  <code>ResultSet.CONCUR_READ_ONLY</code>
     *  or <code>ResultSet.CONCUR_UPDATABLE</code> (not supported)
     * @return a new PreparedStatement object containing the
     *  pre-compiled SQL statement that will produce
     *  <code>ResultSet</code>
     *  objects with the given type and concurrency
     * @exception SQLException if a database access error occurs or
     *  the given parameters are not ResultSet constants
     *  indicating a supported type and concurrency
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *  for jdbcConnection)
     */
    public PreparedStatement prepareStatement(String sql, int type,
            int concurrency) throws SQLException {

        checkTypeConcurrency(type, concurrency);
        checkClosed();

        try {
            return new jdbcPreparedStatement(this, sql, type);
        } catch (HsqlException e) {
            throw jdbcDriver.sqlException(e);
        }
    }

    /**
     * <!-- start generic documentation -->
     * Creates a <code>CallableStatement</code>
     * object that will generate <code>ResultSet</code> objects with
     * the given type and concurrency. This method is the same as the
     * <code>prepareCall</code> method above, but it allows the
     * default result set type and result set concurrency type to be
     * overridden. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * Up to and including HSQLDB 1.7.0, support is provided only for
     * type <code>TYPE_FORWARD_ONLY</code> and concurrency
     * <code>CONCUR_READ_ONLY</code>.
     * Specifying any other values will throw a SQLException.<p>
     *
     * <B>Notes:</B> <p>
     *
     * Up to 1.6.1, calling this method returned null if the connection
     * was already closed and a supported combination of type and
     * concurrency was specified. This was possibly counter-intuitive
     * to the expectation that an exception would be thrown for
     * closed connections. Starting with 1.7.0. the behaviour is to throw
     * a <code>SQLException</code> if the connection is closed.<p>
     *
     * Up to and including 1.7.1, each HSQLDB stored procedure returns
     * only a single value wrapped in a <code>ResultSet</code> object. That
     * is, HSQLDB stored procedures act very much like SQL functions,
     * and can actually always be used in such a capacity. As such,
     * there is really no point in supporting anything but
     * <code>TYPE_FORWARD_ONLY</code>, since the result obtained by
     * executing a <code>CallableStatement</code> object has
     * always just one column and one row.  Be aware that this
     * behaviour will change in HSQLDB 1.7., in that support will be
     * added for Java stored procedures that return multi-column,
     * multi-row results. At that point, support will be added for
     * <code>CallableStatement</code> objects that return
     * <code>TYPE_FORWARD_ONLY</code> <code>ResultSet</code> objects. <p>
     *
     * New to 1.7.0, HSQLDB now allows calling <code>void</code> Java
     * methods as SQL functions and stored procedures, the result being a
     * SQL <code>NULL</code> value or a result with one column and one row
     * whose single field is the SQL <code>NULL</code> value, respectiviely.
     * Previously, calling such Java methods in either context resulted in
     * throwing a <code>SQLException</code>.
     *
     * Finally, up to and including 1.7.1, the returned
     * <code>CallableStatement</code> object does not support any
     * getXXX methods. That is, HSQLDB stored procedures do not
     * support <code>OUT</code> or <code>IN OUT</code> parameters.
     * This behaviour <I>may</I> change at some point in the 1.7.x series,
     * but no decisions have yet been made. <p>
     *
     * </span> <!-- end release-specific documentation -->
     *
     * @param sql a String object that is the SQL statement to be
     * sent to the database; may contain one or more ? parameters
     * @param resultSetType a result set type; one of
     * <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     * <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, (not
     * supported) or <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * (not supported)
     * @param resultSetConcurrency a concurrency type; one of
     * <code>ResultSet.CONCUR_READ_ONLY</code>
     * or <code>ResultSet.CONCUR_UPDATABLE</code> (not supported)
     * @return a new CallableStatement object containing the
     * pre-compiled SQL statement
     * @exception SQLException if a database access error occurs or
     * the given parameters are not <code>ResultSet</code>
     * constants indicating a supported type and concurrency
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     * for jdbcConnection)
     */
    public CallableStatement prepareCall(String sql, int resultSetType,
                                         int resultSetConcurrency)
                                         throws SQLException {

        checkTypeConcurrency(resultSetType, resultSetConcurrency);
        checkClosed();

        return prepareCall(sql);
    }

    /**
     * <!-- start generic documentation -->
     * Gets the type map object associated with this connection. Unless
     * the application has added an entry to the type map, the map
     * returned will be empty.<p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. Calling this
     * method always throws a <code>SQLException</code>, stating that the
     * function is not supported. <p>
     *
     * </span> <!-- end release-specific documentation -->
     *
     * @return the <code>java.util.Map</code> object associated with
     *     this <code>Connection</code> object
     * @exception SQLException if a database access error occurs
     *     (always, up to HSQLDB 1.7.0, inclusive)
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *     for jdbcConnection)
     */
    public Map getTypeMap() throws SQLException {
        throw jdbcDriver.notSupported;
    }

    /**
     * <!-- start generic documentation -->
     * Installs the given <code>TypeMap</code>
     * object as the type map for this <code>Connection</code>
     * object. The type map will be used for the custom mapping of
     * SQL structured types and distinct types.<p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. Calling this
     * method always throws a <code>SQLException</code>, stating that
     * the function is not supported. <p>
     *
     * </span> <!-- end release-specific documentation -->
     *
     * @param map the <code>java.util.Map</code> object to install as
     *     the replacement for this <code>Connection</code> object's
     *     default type map
     * @exception SQLException if a database access error occurs or
     *     the given parameter is not a <code>java.util.Map</code>
     *     object (always, up to HSQLDB 1.7.0, inclusive)
     * @since JDK 1.2 (JDK 1.1.x developers: read the new overview
     *     for jdbcConnection)
     * @see #getTypeMap
     */
    public void setTypeMap(Map map) throws SQLException {
        throw jdbcDriver.notSupported;
    }

// boucherb@users 20020409 - javadocs for all JDBC 3 methods
// boucherb@users 20020509 - todo
// start adding implementations where it is easy:  Savepoints
    //--------------------------JDBC 3.0-----------------------------

    /**
     * <!-- start generic documentation -->
     * Changes the holdability of
     * <code>ResultSet</code> objects created using this
     * <code>Connection</code> object to the given holdability. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span> <!-- end release-specific documentation -->
     *
     * @param holdability a <code>ResultSet</code> holdability
     *     constant; one of <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code>
     *     or <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
     * @throws SQLException if a database access occurs, the given
     *     parameter is not a <code>ResultSet</code> constant
     *     indicating holdability, or the given holdability is not
     *     supported
     * @see #getHoldability
     * @see ResultSet
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
/*
    public void setHoldability(int holdability) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Retrieves the current
     * holdability of <code>ResultSet</code> objects created using
     * this <code>Connection</code> object.<p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span> <!-- end release-specific documentation -->
     *
     * @return the holdability, one of
     *     <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code>
     *     or <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
     * @throws SQLException if a database access occurs
     * @see #setHoldability
     * @see ResultSet
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
/*
    public int getHoldability() throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Creates an unnamed savepoint in
     * the current transaction and returns the new <code>Savepoint</code>
     * object that represents it.<p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span> <!-- end release-specific documentation -->
     *
     * @return the new <code>Savepoint</code> object
     * @exception SQLException if a database access error occurs or
     *     this <code>Connection</code> object is currently in
     *     auto-commit mode
     * @see jdbcSavepoint
     * @see java.sql.Savepoint
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
/*
    public Savepoint setSavepoint() throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Creates a savepoint with the
     * given name in the current transaction and returns the new
     * <code>Savepoint</code> object that represents it. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span> <!-- end release-specific documentation -->
     *
     * @param name a <code>String</code> containing the name of the savepoint
     * @return the new <code>Savepoint</code> object
     * @exception SQLException if a database access error occurs or
     *     this <code>Connection</code> object is currently in
     *     auto-commit mode
     *
     * @see jdbcSavepoint
     * @see java.sql.Savepoint
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
/*
    public Savepoint setSavepoint(String name) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Undoes all changes made after
     * the given <code>Savepoint</code> object was set. <p>
     *
     * This method should be used only when auto-commit has been
     * disabled. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span> <!-- end release-specific documentation -->
     *
     * @param savepoint the <code>Savepoint</code> object to roll back to
     * @exception SQLException if a database access error occurs,
     *           the <code>Savepoint</code> object is no longer valid,
     *           or this <code>Connection</code> object is currently in
     *           auto-commit mode
     * @see jdbcSavepoint
     * @see java.sql.Savepoint
     * @see #rollback
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
/*
    public void rollback(Savepoint savepoint) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Removes the given <code>Savepoint</code>
     * object from the current transaction. Any reference to the
     * savepoint after it have been removed will cause an
     * <code>SQLException</code> to be thrown. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span> <!-- end release-specific documentation -->
     *
     * @param savepoint the <code>Savepoint</code> object to be removed
     * @exception SQLException if a database access error occurs or
     *           the given <code>Savepoint</code> object is not a valid
     *           savepoint in the current transaction
     *
     * @see jdbcSavepoint
     * @see java.sql.Savepoint
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
/*
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Creates a <code>Statement</code>
     * object that will generate <code>ResultSet</code> objects with
     * the given type, concurrency, and holdability. This method is
     * the same as the <code>createStatement</code> method above, but
     * it allows the default result set type, concurrency, and
     * holdability to be overridden. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span> <!-- end release-specific documentation -->
     *
     * @param resultSetType one of the following <code>ResultSet</code>
     *     constants: <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *     <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>,
     *     or <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @param resultSetConcurrency one of the following
     *     <code>ResultSet</code>
     *     constants: <code>ResultSet.CONCUR_READ_ONLY</code> or
     *     <code>ResultSet.CONCUR_UPDATABLE</code>
     * @param resultSetHoldability one of the following
     *     code>ResultSet</code>
     *     constants: <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code>
     *     or <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
     * @return a new <code>Statement</code> object that will generate
     *     <code>ResultSet</code> objects with the given type,
     *     concurrency, and holdability
     * @exception SQLException if a database access error occurs or
     *     the given parameters are not <code>ResultSet</code>
     *     constants indicating type, concurrency, and holdability
     * @see ResultSet
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
/*
    public Statement createStatement(int resultSetType,
                                     int resultSetConcurrency,
                                     int resultSetHoldability)
                                     throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Creates a <code>PreparedStatement</code>
     * object that will generate <code>ResultSet</code> objects with
     * the given type, concurrency, and holdability. <p>
     *
     * This method is the same as the <code>prepareStatement</code>
     * method above, but it allows the default result set type,
     * concurrency, and holdability to be overridden. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span> <!-- end release-specific documentation -->
     *
     * @param sql a <code>String</code> object that is the SQL
     *     statement to be sent to the database; may contain one or
     *     more ? IN parameters
     * @param resultSetType one of the following <code>ResultSet</code>
     *     constants: <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *     <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>,
     *     or <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @param resultSetConcurrency one of the following
     *     <code>ResultSet</code>
     *     constants: <code>ResultSet.CONCUR_READ_ONLY</code> or
     *     <code>ResultSet.CONCUR_UPDATABLE</code>
     * @param resultSetHoldability one of the following
     *     <code>ResultSet</code>
     *     constants: <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code>
     *     or <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
     * @return a new <code>PreparedStatement</code> object,
     *     containing the pre-compiled SQL statement, that will
     *     generate <code>ResultSet</code> objects with the given
     *     type, concurrency, and holdability
     * @exception SQLException if a database access error occurs or
     *     the given parameters are not <code>ResultSet</code>
     *     constants indicating type, concurrency, and holdability
     * @see ResultSet
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
/*
    public PreparedStatement prepareStatement(String sql, int resultSetType,
            int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Creates a <code>CallableStatement</code>
     * object that will generate <code>ResultSet</code> objects with
     * the given type and concurrency. This method is the same as the
     * <code>prepareCall</code> method above, but it allows the
     * default result set type, result set concurrency type and
     * holdability to be overridden. <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span> <!-- end release-specific documentation -->
     *
     * @param sql a <code>String</code> object that is the SQL
     *     statement to be sent to the database; may contain on or
     *     more ? parameters
     * @param resultSetType one of the following <code>ResultSet</code>
     *     constants: <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *     <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *     <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @param resultSetConcurrency one of the following
     *     <code>ResultSet</code>
     *     constants: <code>ResultSet.CONCUR_READ_ONLY</code> or
     *     <code>ResultSet.CONCUR_UPDATABLE</code>
     * @param resultSetHoldability one of the following
     *     <code>ResultSet</code>
     *     constants: <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code>
     *     or <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
     * @return a new <code>CallableStatement</code> object,
     *     containing the pre-compiled SQL statement, that will
     *     generate <code>ResultSet</code> objects with the given
     *     type, concurrency, and holdability
     * @exception SQLException if a database access error occurs or
     *     the given parameters are not <code>ResultSet</code>
     *     constants indicating type, concurrency, and holdability
     * @see ResultSet
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
/*
    public CallableStatement prepareCall(String sql, int resultSetType,
                                         int resultSetConcurrency,
                                         int resultSetHoldability)
                                         throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Creates a default <code>PreparedStatement</code>
     * object that has the capability to retrieve auto-generated
     * keys. The given constant tells the driver whether it should
     * make auto-generated keys available for retrieval. This
     * parameter is ignored if the SQL statement is not an
     * <code>INSERT</code>  statement. <p>
     *
     * <B>Note:</B> This method is optimized for handling parametric
     * SQL statements that benefit from precompilation. If the driver
     * supports precompilation, the method <code>prepareStatement</code>
     * will send the statement to the database for precompilation.
     * Some drivers may not support precompilation. In this case, the
     * statement may not be sent to the database until the
     * <code>PreparedStatement</code>
     * object is executed. This has no direct effect on users;
     * however, it does affect which methods throw certain
     * SQLExceptions. <p>
     *
     * Result sets created using the returned <code>PreparedStatement</code>
     * object will by default be type <code>TYPE_FORWARD_ONLY</code>
     * and have a concurrency level of <code>CONCUR_READ_ONLY</code>.
     * <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span> <!-- end release-specific documentation -->
     *
     * @param sql an SQL statement that may contain one or more '?'
     *     IN parameter placeholders
     * @param autoGeneratedKeys a flag indicating that auto-generated
     *     keys should be returned, one of
     *     code>Statement.RETURN_GENERATED_KEYS</code>
     *     or <code>Statement.NO_GENERATED_KEYS</code>.
     * @return a new <code>PreparedStatement</code> object,
     *     containing the pre-compiled SQL statement, that will have
     *     the capability of returning auto-generated keys
     * @exception SQLException if a database access error occurs or
     *     the given parameter is not a <code>Statement</code>
     *     constant indicating whether auto-generated keys should be
     *     returned
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
/*
    public PreparedStatement prepareStatement(String sql,
            int autoGeneratedKeys) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Creates a default <code>PreparedStatement</code>
     * object capable of returning the auto-generated keys designated
     * by the given array. This array contains the indexes of the
     * columns in the target table that contain the auto-generated
     * keys that should be made available. This array is ignored if
     * the SQL statement is not an <code>INSERT</code> statement. <p>
     *
     * An SQL statement with or without IN parameters can be
     * pre-compiled and stored in a <code>PreparedStatement</code>
     * object. This object can then be used to efficiently execute
     * this statement multiple times. <p>
     *
     * <B>Note:</B> This method is optimized for handling parametric
     * SQL statements that benefit from precompilation. If the driver
     * supports precompilation, the method <code>prepareStatement</code>
     * will send the statement to the database for precompilation.
     * Some drivers may not support precompilation. In this case, the
     * statement may not be sent to the database until the
     * <code>PreparedStatement</code>
     * object is executed. This has no direct effect on users;
     * however, it does affect which methods throw certain
     * SQLExceptions. <p>
     *
     * Result sets created using the returned <code>PreparedStatement</code>
     * object will by default be type <code>TYPE_FORWARD_ONLY</code>
     * and have a concurrency level of <code>CONCUR_READ_ONLY</code>.
     * <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span> <!-- end release-specific documentation -->
     *
     * @param sql an SQL statement that may contain one or more '?'
     *     IN parameter placeholders
     * @param columnIndexes an array of column indexes indicating the
     *     columns that should be returned from the inserted row or
     *     rows
     * @return a new <code>PreparedStatement</code> object,
     *     containing the pre-compiled statement, that is capable of
     *     returning the auto-generated keys designated by the given
     *     array of column indexes
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
/*
    public PreparedStatement prepareStatement(String sql,
            int columnIndexes[]) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3

    /**
     * <!-- start generic documentation -->
     * Creates a default <code>PreparedStatement</code>
     * object capable of returning the auto-generated keys designated
     * by the given array. This array contains the names of the
     * columns in the target table that contain the auto-generated
     * keys that should be returned. This array is ignored if the SQL
     * statement is not an <code>INSERT</code> statement. <p>
     *
     * An SQL statement with or without IN parameters can be
     * pre-compiled and stored in a <code>PreparedStatement</code>
     * object. This object can then be used to efficiently execute
     * this statement multiple times. <p>
     *
     * <B>Note:</B> This method is optimized for handling parametric
     * SQL statements that benefit from precompilation. If the driver
     * supports precompilation, the method <code>prepareStatement</code>
     * will send the statement to the database for precompilation.
     * Some drivers may not support precompilation. In this case, the
     * statement may not be sent to the database until the
     * <code>PreparedStatement</code>
     * object is executed. This has no direct effect on users;
     * however, it does affect which methods throw certain
     * SQLExceptions. <p>
     *
     * Result sets created using the returned <code>PreparedStatement</code>
     * object will by default be type <code>TYPE_FORWARD_ONLY</code>
     * and have a concurrency level of <code>CONCUR_READ_ONLY</code>.
     * <p>
     *
     * <!-- end generic documentation -->
     * <!-- start release-specific documentation -->
     * <span class="ReleaseSpecificDocumentation">
     * <b>HSQLDB-Specific Information:</b> <p>
     *
     * HSQLDB 1.7.1 does not support this feature. <p>
     *
     * Calling this method always throws a <code>SQLException</code>,
     * stating that the function is not supported. <p>
     *
     * </span> <!-- end release-specific documentation -->
     *
     * @param sql an SQL statement that may contain one or more '?'
     *     IN parameter placeholders
     * @param columnNames an array of column names indicating the
     *     columns that should be returned from the inserted row or
     *     rows
     * @return a new <code>PreparedStatement</code> object,
     *     containing the pre-compiled statement, that is capable of
     *     returning the auto-generated keys designated by the given
     *     array of column names
     * @exception SQLException if a database access error occurs
     * @since JDK 1.4, HSQLDB 1.7
     */
//#ifdef JDBC3
/*
    public PreparedStatement prepareStatement(String sql,
            String columnNames[]) throws SQLException {
        throw jdbcDriver.notSupportedJDBC3;
    }
*/

//#endif JDBC3
//---------------------- internal implementation ---------------------------

    /**
     * Constructs a new standard <code>Connection</code> to an HSQLDB
     * <code>Database</code>. <p>
     *
     * This constructor is called on behalf of the
     * <code>java.sql.DriverManager</code> when getting a
     * <code>Connection</code> for use in normal (external)
     * client code. <p>
     *
     * Internal client code, that being code located in HSQLDB SQL
     * functions and stored procedures, receives an INTERNAL
     * connection constructed by the {@link #jdbcConnection(Session)
     * jdbcConnection(Session)} constructor.
     * @param props A <code>Properties</code> object containing the connection
     * properties such as the user name and password
     * @param s A connection specifier.<p>
     *
     *     Essentially, this is the name of the target <code>Database</code>,
     *     possibly decorated with a protocol specifier prefix such
     *     as <span class="JavaStringLiteral">"hsql://"</span> or
     *     <span class="JavaStringLiteral">"http://"</span> in the
     *     case that a network connection rather than a connection to
     *     an in-process <code>Database</code> is desired. <p>
     * @exception SQLException when the user/password combination is
     *     invalid, the connection specifier is invalid, or the
     *     <code>Database</code> is unavailable. <p>
     *
     *     The <code>Database</code> may be unavailable for a number
     *     of reasons, including network problems or the fact that it
     *     may already be in use by another process.
     */
    jdbcConnection(HsqlProperties props) throws SQLException {

        String user     = (String) props.getProperty("user");
        String password = (String) props.getProperty("password");
        boolean ifExists = props.isPropertyTrue("ifexists");
        String connType = (String) props.getProperty("connection_type");
        String dbString = (String) props.getProperty("database");
        String host     = props.getProperty("host");
        int    port     = props.getIntegerProperty("port", 0);
        boolean isTLS = (connType == DatabaseManager.S_HSQLS
                         || connType == DatabaseManager.S_HTTPS);

        try {
            if (connType == DatabaseManager.S_FILE
                    || connType == DatabaseManager.S_MEM
                    || connType == DatabaseManager.S_RES) {
                sessionProxy = DatabaseManager.newSession(connType, dbString,
                        user, password, ifExists);
            } else if (connType == DatabaseManager.S_HSQL
                       || connType == DatabaseManager.S_HSQLS) {
                sessionProxy = new HSQLClientConnection(host, port, dbString,
                        isTLS, user, password);
            } else if (connType == DatabaseManager.S_HTTP
                       || connType == DatabaseManager.S_HTTPS) {
                sessionProxy = new HTTPClientConnection(host, port, dbString,
                        isTLS, user, password);
            }

            connProperties = props;
        } catch (HsqlException e) {
            throw jdbcDriver.sqlException(e);
        }
    }

    /**
     * Constructs an {@link #INTERNAL INTERNAL} <code>Connection</code>,
     * using the specified {@link Session Session}. <p>
     *
     * This constructor is called only on behalf of an existing
     * <code>Session</code> (the internal parallel of a
     * <code>Connection</code>), to be used as a parameter to a SQL
     * function or stored procedure that needs to execute in the context
     * of that <code>Session</code>. <p>
     *
     * When a Java SQL function or stored procedure is called and its
     * first parameter is of type <code>Connection</code>, HSQLDB
     * automatically notices this and constructs an <code>INTERNAL</code>
     * <code>Connection</code> using the current <code>Session</code>.
     * HSQLDB then passes this <code>Connection</code> in the first
     * parameter position, moving any other parameter values
     * specified in the SQL statement to the right by one position.
     * <p>
     *
     * To read more about this, see {@link Function#getValue()}. <p>
     *
     * <B>Notes:</B> <p>
     *
     * In a future release, <code>INTERNAL</code> connections may not
     * be closable from JDBC client code and disconnect statements
     * issued on <code>INTERNAL</code> connections may be ignored.
     * <p>
     *
     * For HSQLDB developers not involved with writing database
     * internals, this change will only apply to connections obtained
     * automatically from the database as the first parameter to
     * stored procedures and SQL functions. This is mainly an issue
     * to developers writing custom SQL function and stored procedure
     * libraries for HSQLDB. As we anticipate this change, it is
     * recommended that SQL function and stored procedure code avoid
     * depending on closing or issuing a disconnect on a connection
     * obtained in this manner. <p>
     *
     *
     *
     * @param c the Session requesting the construction of this
     *     Connection
     * @exception SQLException when the specified Session is null
     * @see Function
     */
    jdbcConnection(Session c) throws HsqlException {
        isInternal   = true;
        sessionProxy = c;
    }

    /**
     *  The internal statement execution request router. <p>
     *
     *  All submitted SQL statements eventually go through this
     *  method. This method determines what type of Connection this is
     *  and calls the appropriate type-specific executor based on this
     *  determination. <p>
     *
     *
     *
     * @param  s the SQL statement to execute
     * @return  the result of executing the specified statement
     * @throws  SQLException when the specified statement cannot be
     *      executed or the connection is broken or closed <p>
     *
     *      The typical reasons a statement cannot be executed are:<p>
     *
     *
     *      <OL>
     *        <LI> The statement cannot be parsed</LI>
     *        <LI> The statement refers to invalid or non-existent
     *        database objects</LI>
     *        <LI> The statement violates database integrity</LI>
     *        <LI> A security violation occurs</LI>
     *        <LI> The database is shut down</LI>
     *        <LI> The session has been disconnected</LI>
     *        <LI> The connection is closed</LI>
     *      </OL>
     *
     * @see  #executeHSQL executeHSQL
     * @see  #executeHTTP executeHTTP
     * @see  #executeStandalone executeStandalone
     */

    /**
     *  The default implementation simply attempts to silently {@link
     *  #close() close()} this <code>Connection</code>
     */
    public void finalize() {

        try {
            close();
        } catch (SQLException e) {
            if (Trace.TRACE) {
                Trace.trace(e.toString());
            }
        }
    }

    String getURL() {
        return connProperties.getProperty("url");
    }
}
