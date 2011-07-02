/* Copyright (c) 2001-2011, The HSQL Development Group
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
package org.hsqldb.jdbc;

import org.hsqldb.DatabaseUtils;
import org.hsqldb.SessionInterface;
import org.hsqldb.persist.HsqlProperties;

/**
 * Utility for accessing extended connection information.
 *
 * @author boucherb@users
 */
public class ConnectionUtils {

    /**
     * Construction Disabled.
     */
    private ConnectionUtils() {
        throw new RuntimeException("Construction Disabled.");
    }

    /**
     * Retrieves a copy of the given connection object's properties.
     *
     * @param conn the connection object from which to retrieve the properties.
     * @return a copy of the given connection object's properties.
     * @exception NullPointerException if the given connection object
     *      reference is <tt>null</tt>.
     */
    public static HsqlProperties GetConnectionProperties(jdbcConnection conn) {
        HsqlProperties properties = new HsqlProperties();

        HsqlProperties connProperties = conn.connProperties;

        if (connProperties != null) {
            properties.addProperties(connProperties);
        }

        return properties;
    }

    /**
     * Retrieves the given connection object's session interface.
     *
     * @param conn the connection object from which to retrieve
     *      the session interface.
     * @return the given connection object's session interface.
     * @exception NullPointerException if the given connection object
     *      reference is null.
     */
    public static SessionInterface GetSessionInterface(jdbcConnection conn) {
        return conn.sessionProxy;
    }

    /**
     * Retrieves the JDBC connection URL for the given connection object.
     *
     * @param conn the connection object from which to retrieve the
     *      JDBC connection URL.
     * @return the JDBC connection URL for the given connection object.
     * @exception java.sql.SQLException if the connection object is closed.
     * @exception NullPointerException if the given connection object
     *      reference is null.
     */
    public static String GetJdbcUrl(jdbcConnection conn) throws java.sql.SQLException {
        return conn.getURL();
    }

    /**
     * Retrieves whether the given connection object is internal.
     *
     * @param conn the connection object for which to make the determination.
     * @return <tt>true</tt> if the given connection object is internal;
     *      else <tt>false</tt>.
     * @exception NullPointerException if the given connection object
     *      reference is <tt>null</tt>.
     */
    public static boolean IsInternal(jdbcConnection conn) {
        return conn.isInternal;
    }

    /**
     * Retrieves whether the given connection object uses a network protocol
     * to communicate with the database instance.
     *
     * @param conn the connection object for which to make the determination.
     * @return <tt>true</tt> if the given connection object uses a network
     *      protocol to communicate with the database instance;
     *      else <tt>false</tt>.
     * @exception NullPointerException if the given connection object
     *      reference is <tt>null</tt>.
     */
    public static boolean IsNetwork(jdbcConnection conn) {
        return conn.isNetConn;
    }

    /**
     * Retrieves the name of the collation in effect for the database
     * to which the connection is attached. <p>
     * 
     * Note: For now, this works only for embedded mode operation.
     * 
     * @param conn The connection
     * @return The collation name
     */
    public static String GetDatabaseCollationName(java.sql.Connection conn) {
        return DatabaseUtils.GetDatabaseCollationName(((jdbcConnection) conn).sessionProxy);
    }
}
