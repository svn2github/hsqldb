package org.hsqldb.jdbc;

import org.hsqldb.DatabaseUtils;
import org.hsqldb.SessionInterface;
import org.hsqldb.persist.HsqlProperties;

/**
 * Utility for accessing extended jdbcConnection information.
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
    public static HsqlProperties GetConnectionProperties(jdbcConnection conn)
    {
        HsqlProperties properties = new HsqlProperties();

        HsqlProperties connProperties = conn.connProperties;

        if (connProperties != null)
        {
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
    public static SessionInterface GetSessionInterface(jdbcConnection conn)
    {
        return conn.sessionProxy;
    }

    /**
     * Retieves the JDBC connection URL for the given connection object.
     *
     * @param conn the connection object from which to retrieve the
     *      JDBC connection URL.
     * @return the JDBC connection URL for the given connection object.
     * @exception java.sql.SQLException if the connection object is closed.
     * @exception NullPointerException if the given connection object
     *      reference is null.
     */
    public static String GetJdbcUrl(jdbcConnection conn) throws java.sql.SQLException
    {
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
    public static boolean IsInternal(jdbcConnection conn)
    {
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
    public static boolean IsNetwork(jdbcConnection conn)
    {
        return conn.isNetConn;
    }

    /**
     * Retrives the name of the collation in effect for the database
     * to which the connection is attached.
     * @param conn The connection
     * @return The collation name
     */
    public static String GetDatabaseCollationName(java.sql.Connection conn)
    {
        return DatabaseUtils.GetDatabaseCollationName(((jdbcConnection)conn).sessionProxy);
    }
}
