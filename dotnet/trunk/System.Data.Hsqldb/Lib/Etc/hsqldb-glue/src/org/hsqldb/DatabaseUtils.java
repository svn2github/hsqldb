package org.hsqldb;

import org.hsqldb.jdbc.ConnectionUtils;
import org.hsqldb.persist.HsqlProperties;

/**
 *
 * @author boucherb@users
 */
public class DatabaseUtils {
    
    private DatabaseUtils() {}
    
    /**
     * Retrieves the name of the database collation that is currently
     * in effect for the given Session.
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
     * @param conn the connection
     * @return the database collation name
     */
    public static String GetDatabaseCollationName(java.sql.Connection conn) {
        return ConnectionUtils.GetDatabaseCollationName(conn);
    }
    
    /**
     * Creates a new Session for the given database, user and password.
     * @param databaseId the numeric identifier of the database
     * @param user the name of the initial session user
     * @param password the password for the intial session user.
     * @throws org.hsqldb.HsqlException when a database access error occurs.
     * @return the new sesssion.
     */
    public static Session CreateSession(
            int databaseId,
            String user,
            String password) throws HsqlException {
        return DatabaseManager.newSession(databaseId, user, password);
    }

    /**
     * Retrieves the database instance with the given type and path.
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
     * This translation is occasonally required because internal type
     * comparisons use identity equality (==) rather than object equality
     * (x.equals(y)) regarding  the DatabaseURL.S_FILE, DatabaseURL.S_MEM
     * and DatabaseURL.S_RES constants.
     *
     * @param type for which to retrieve the matching internal type string.
     * @return the matching internal type string; the given string itself when
     * there is not match.
     */
    public static String InternDatabaseType(String type) {
        if(DatabaseURL.S_FILE.equals(type)) {
            return DatabaseURL.S_FILE;
        } else if (DatabaseURL.S_MEM.equals(type)) {
            return  DatabaseURL.S_MEM;
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
     * @param server that is no longer interested in recieving database
     * state change notification.
     */
    public static void UnRegisterServer(Server server) {
        DatabaseManager.deRegisterServer(server);
    }
}
