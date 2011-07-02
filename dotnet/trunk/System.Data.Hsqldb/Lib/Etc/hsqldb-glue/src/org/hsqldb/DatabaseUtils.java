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
package org.hsqldb;

import org.hsqldb.jdbc.ConnectionUtils;
import org.hsqldb.persist.HsqlProperties;

/**
 * Utility for accessing extended {@link org.hsqldb.Database}  information.
 * 
 * @author boucherb@users
 */
public final class DatabaseUtils {

    private DatabaseUtils() {
        throw new RuntimeException("Construction Disabled.");
    }

    /**
     * Retrieves the name of the database collation that is currently
     * in effect for the given Session. <P>
     * 
     * Note:  this works only for embedded mode operation.
     * 
     * @param sessionInterface for which to make the determination
     * @return  The current database collation name
     */
    public static String GetDatabaseCollationName(
            SessionInterface sessionInterface) {
        if (sessionInterface instanceof Session) {
            Session session = (Session) sessionInterface;

            return (session.database != null
                    && session.database.collation != null)
                    ? session.database.collation.name
                    : "UCS_BASIC";
        } else {
            return "UCS_BASIC";
        }
    }

    /**
     * Retrieves the name of the database collation that is
     * currently in effect for the given Connection object.
     * 
     * @param conn the connection
     * @return the database collation name
     */
    public static String GetDatabaseCollationName(java.sql.Connection conn) {
        return ConnectionUtils.GetDatabaseCollationName(conn);
    }

    /**
     * Creates a new Session for the given database, user and password.
     * 
     * @param databaseId the numeric identifier of the database
     * @param user the name of the initial session user
     * @param password the password for the initial session user.
     * @throws org.hsqldb.HsqlException when a database access error occurs.
     * @return the new session.
     */
    public static Session CreateSession(
            int databaseId,
            String user,
            String password) throws HsqlException {
        return DatabaseManager.newSession(databaseId, user, password);
    }

    /**
     * Retrieves the database instance with the given type and path.
     * 
     * @param type a valid in-process database URI scheme component
     * @param path a valid in-process database URI path component
     * @param properties used to control the lookup and/or configure the instance
     * @throws org.hsqldb.HsqlException when a database access error occurs
     * @return the matching database instance.
     */
    public static Database GetDatabase(
            String type,
            String path,
            HsqlProperties properties) throws HsqlException {
        return DatabaseManager.getDatabase(
                type,
                path,
                properties);
    }

    /**
     * Retrieves the session with the given numeric identifier, connected
     * to the database with the given numeric identifier.
     * 
     * @return the matching session; null if there is no matching
     * database or session.
     * @param databaseId that identifies the database instance.
     * @param sessionId that identifies the session instance.
     */
    public SessionInterface GetSession(int databaseId, int sessionId) {
        return DatabaseManager.getSession(databaseId, sessionId);
    }

    /**
     * Returns the internal type string matching the given type string. <p>
     *
     * This translation is occasionally required because internal type
     * comparisons use identity equality (==) rather than object equality
     * (x.equals(y)) regarding  the DatabaseURL.S_FILE, DatabaseURL.S_MEM
     * and DatabaseURL.S_RES constants.
     *
     * @param type for which to retrieve the matching internal type string.
     * @return the matching internal type string; the given string itself when
     * there is not match.
     */
    public static String InternDatabaseType(String type) {
        if (DatabaseURL.S_FILE.equals(type)) {
            return DatabaseURL.S_FILE;
        } else if (DatabaseURL.S_MEM.equals(type)) {
            return DatabaseURL.S_MEM;
        } else if (DatabaseURL.S_RES.equals(type)) {
            return DatabaseURL.S_RES;
        } else {
            return type;
        }
    }

    /**
     * Registers the given server for database state change notification and
     * returns the internal numeric identifier of the matching database
     * instance.
     *
     * @param server to register for database state change notification.
     * @param type a valid in-process database URI scheme component.
     * @param path a valid in-process database URI path component.
     * @param properties used to control the lookup and/or configure the instance.
     * @throws org.hsqldb.HsqlException when a database access error occurs.
     * @return the internal numeric identifier of the matching database instance.
     */
    public static int RegisterServer(
            Server server,
            String type,
            String path,
            HsqlProperties properties) throws HsqlException {
        return DatabaseManager.getDatabase(
                InternDatabaseType(type),
                path,
                server,
                properties);
    }

    /**
     * Instructs the database internals that the given server is no longer
     * interested in receiving database state change notification.
     *
     * @param server that is no longer interested in relieving database
     * state change notification.
     */
    public static void UnRegisterServer(Server server) {
        DatabaseManager.deRegisterServer(server);
    }
}
