/* Copyright (c) 2001-2002, The HSQL Development Group
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

import org.hsqldb.lib.HsqlTimer;
import org.hsqldb.lib.HashMap;
import org.hsqldb.lib.IntValueHashMap;
import org.hsqldb.lib.Iterator;
import java.io.File;

/**
 * Multifunction class with all static methods.<p>
 *
 * Handles initial attempts to connect to HSQLDB databases within the JVM
 * (or a classloader within the JVM). Opens the database if it is not open
 * or connects to it if it is already open. This allows the same database to
 * be used by different instances of Server and by direct connections.<p>
 *
 * Maintains a map of Server instances and notifies each server when its
 * database has shut down.<p>
 *
 * Maintains a reference to the timer used for file locks and logging.<p>
 *
 * Parses a connection URL into parts.
 *
 *
 * @author fred@users
 * @version 1.7.2
 * @since 1.7.2
 */
class DatabaseManager {

    static void gc() {

        if ((Record.gcFrequency > 0)
                && (Record.memoryRecords > Record.gcFrequency)) {
            if (Trace.TRACE) {
                Trace.trace("gc at " + Record.memoryRecords);
            }

            Record.memoryRecords = 0;

            System.gc();
        }
    }

    //
    private static HsqlTimer timer = new HsqlTimer();

    static HsqlTimer getTimer() {
        return timer;
    }

    //
    static HashMap         memDatabaseMap    = new HashMap();
    static HashMap         fileDatabaseMap   = new HashMap();
    static HashMap         resDatabaseMap    = new HashMap();
    static IntValueHashMap databaseAccessMap = new IntValueHashMap();
    static HashMap         databaseFileMap   = new HashMap();

    static Session newSession(String type, String path, String user,
                              String password,
                              boolean ifexists) throws HsqlException {

        Database db = getDatabase(type, path, ifexists);

        return db.connect(user, password);
    }

    /**
     * This returns an existing session. Used with repeat HTTP connections
     * belonging to the same JDBC Conenction / HSQL Session pair.
     *
     */
    static Session getSession(String type, String path,
                              int id) throws HsqlException {

        Database db = lookupDatabaseObject(type, path);

        return db.sessionManager.getSession(id);
    }

    /**
     * This has to be improved once a threading model is in place.
     * Current behaviour:
     *
     * Attempts to connect to different databases do not block. Two db's can
     * open simultaneously.
     *
     * Attempts to connect to a db while it is opening or closing will block
     * until the db is open or closed. At this point the db state is either
     * DATABASE_ONLINE (after db.open() has returned) which allows a new
     * connection to be made, or the state is DATABASE_SHUTDOWN which means
     * the db can be reopened for the new connection).
     *
     */
    static Database getDatabase(String type, String path,
                                boolean ifexists) throws HsqlException {

        Database db = getDatabaseObject(type, path, ifexists);

        synchronized (db) {
            switch (db.getState()) {

                case Database.DATABASE_ONLINE :
                    break;

                case Database.DATABASE_SHUTDOWN :
                    db.open();
                    break;

                case Database.DATABASE_CLOSING :
                case Database.DATABASE_OPENING :
                    throw Trace.error(
                        Trace.DATABASE_ALREADY_IN_USE,
                        "attempt to connect while db opening /closing");
            }
        }

        return db;
    }

    /**
     * The access counters maintained by this and other methods are not
     * actually used in the engine.
     */
    static synchronized Database getDatabaseObject(String type, String path,
            boolean ifexists) throws HsqlException {

        Database db;
        Object   key = path;
        HashMap  databaseMap;

        if (type == S_FILE) {
            databaseMap = fileDatabaseMap;
            key         = new File(path);
        } else if (type == S_RES) {
            databaseMap = resDatabaseMap;
        } else {
            databaseMap = memDatabaseMap;
        }

        db = (Database) databaseMap.get(key);

        if (db == null) {
            db = new Database(path, path, type);

            if (ifexists && db.isNew) {
                throw Trace.error(Trace.DATABASE_NOT_EXISTS, type + path);
            }

            databaseMap.put(key, db);
            databaseAccessMap.put(db, 1);
        } else {
            int accessCount = databaseAccessMap.get(db, Integer.MIN_VALUE);

            if (accessCount == Integer.MIN_VALUE) {
                throw Trace.error(Trace.GENERAL_ERROR,
                                  "problem in db access count");
            }

            databaseAccessMap.put(db, ++accessCount);
        }

        return db;
    }

    static synchronized Database lookupDatabaseObject(String type,
            String path) throws HsqlException {

        Database db;
        Object   key = path;
        HashMap  databaseMap;

        if (type == S_FILE) {
            databaseMap = fileDatabaseMap;
            key         = new File(path);
        } else if (type == S_RES) {
            databaseMap = resDatabaseMap;
        } else {
            databaseMap = memDatabaseMap;
        }

        return (Database) databaseMap.get(key);
    }

    static synchronized void releaseSession(Database database)
    throws HsqlException {

        int accessCount = databaseAccessMap.get(database, Integer.MIN_VALUE);

        if (accessCount == Integer.MIN_VALUE || accessCount == 0) {
            throw Trace.error(Trace.GENERAL_ERROR,
                              "problem in db access count");
        }

        databaseAccessMap.put(database, --accessCount);
    }

    static synchronized void releaseDatabase(String type,
            String path) throws HsqlException {

        Database database = lookupDatabaseObject(type, path);

        if (database == null) {
            return;
        }

        int accessCount = databaseAccessMap.get(database, Integer.MIN_VALUE);

        if (accessCount == Integer.MIN_VALUE || accessCount == 0) {
            throw Trace.error(Trace.GENERAL_ERROR,
                              "problem in db access count");
        }

        databaseAccessMap.put(database, --accessCount);
    }

    static void removeDatabase(Database database) {

        String  type = database.getType();
        String  path = database.getPath();
        Object  key  = path;
        HashMap databaseMap;

        if (type == S_FILE) {
            databaseMap = fileDatabaseMap;
            key         = new File(path);
        } else if (type == S_RES) {
            databaseMap = resDatabaseMap;
        } else {
            databaseMap = memDatabaseMap;
        }

        databaseMap.remove(key);
        databaseAccessMap.remove(database);
        notifyServers(database);
    }

    //
    private static HashMap serverMap = new HashMap();

    static void registerServer(Server server, Database database) {
        serverMap.put(server, database);
    }

    static void notifyServers(Database database) {

        Iterator it = serverMap.keySet().iterator();

        for (; it.hasNext(); ) {
            Server server = (Server) it.next();

            if (serverMap.get(server).equals(database)) {
                server.notify(ServerConstants.SC_DATABASE_SHUTDOWN);
            }
        }

        it = serverMap.values().iterator();

        for (; it.hasNext(); ) {
            it.next().equals(database);
            it.remove();
        }
    }

    /**
     * Parses the url into the following components returned in the properties
     * object: <p>
     * <ul>
     * url: the original url<p>
     * connection_type: a static string that indicate the protocol. If the
     * url does not begin with a valid protocol, null is returned by this
     * method instead of the properties object.<p>
     * host: name of host in networked modes in lowercase<p>
     * port: port number in networked mode, or 0 if not present<p>
     * path: path of the resource on server in networked modes,
     * / (slash) in all cases apart from
     * servlet path which is / (slash) plus the name of the servlet<p>
     * database: database name. For memory and networked modes, this is
     * returned in lowercase, for file or resource databases the original
     * case of characters is preserved. Returns empty string if name is not
     * present in the url.<p>
     * for each protocol if port number is not in the url<p>
     * Additional connection properties specified as key/value pairs.
     * </ul>
     * @return null returned if url does not begin with valid protocol or the
     * part that should represent the port is not an integer.
     *
     */
    public static HsqlProperties parseURL(String url, boolean hasPrefix) {

        String urlImage = url.toLowerCase();

        if (hasPrefix &&!urlImage.startsWith(S_URL_PREFIX)) {
            return null;
        }

        HsqlProperties props = new HsqlProperties();
        int            pos   = hasPrefix ? S_URL_PREFIX.length()
                                         : 0;
        String         type  = null;
        String         host;
        int            port = 0;
        String         database;
        String         path;
        boolean        isNetwork = false;

        props.setProperty("url", url);

        if (urlImage.startsWith(S_DOT, pos)) {
            type = S_DOT;
        } else if (urlImage.startsWith(S_MEM, pos)) {
            type = S_MEM;
        } else if (urlImage.startsWith(S_FILE, pos)) {
            type = S_FILE;
        } else if (urlImage.startsWith(S_RES, pos)) {
            type = S_RES;
        } else if (urlImage.startsWith(S_ALIAS, pos)) {
            type = S_ALIAS;
        } else if (urlImage.startsWith(S_HSQL, pos)) {
            type      = S_HSQL;
            port      = ServerConstants.SC_DEFAULT_HSQL_SERVER_PORT;
            isNetwork = true;
        } else if (urlImage.startsWith(S_HSQLS, pos)) {
            type      = S_HSQLS;
            port      = ServerConstants.SC_DEFAULT_HSQLS_SERVER_PORT;
            isNetwork = true;
        } else if (urlImage.startsWith(S_HTTP, pos)) {
            type      = S_HTTP;
            port      = ServerConstants.SC_DEFAULT_HTTP_SERVER_PORT;
            isNetwork = true;
        } else if (urlImage.startsWith(S_HTTPS, pos)) {
            type      = S_HTTPS;
            port      = ServerConstants.SC_DEFAULT_HTTPS_SERVER_PORT;
            isNetwork = true;
        }

        if (type == null) {
            type = S_FILE;
        } else if (type == S_DOT) {
            type = S_MEM;

            // keep pos
        } else {
            pos += type.length();
        }

        props.setProperty("connection_type", type);

        int semicolpos = url.indexOf(';', pos);

        if (semicolpos < 0) {
            semicolpos = url.length();
        } else {
            String arguments = urlImage.substring(semicolpos + 1,
                                                  urlImage.length());
            HsqlProperties extraProps =
                HsqlProperties.delimitedArgPairsToProps(arguments, "=", ";",
                    null);

            //todo - check if properties have valid names / values
            props.addProperties(extraProps);
        }

        if (isNetwork) {
            int slashpos = url.indexOf('/', pos);

            if (slashpos < pos || slashpos > semicolpos) {
                slashpos = semicolpos;
            }

            int colonpos = url.indexOf(':', pos);

            if (colonpos < pos || colonpos > slashpos) {
                colonpos = slashpos;
            } else {
                try {
                    port = Integer.parseInt(url.substring(colonpos + 1,
                                                          slashpos));
                } catch (NumberFormatException e) {
                    return null;
                }
            }

            host = urlImage.substring(pos, colonpos);

            int secondslashpos = url.lastIndexOf('/', semicolpos);

            if (secondslashpos < pos) {
                path     = "/";
                database = "";
            } else if (secondslashpos == slashpos) {
                path     = "/";
                database = urlImage.substring(secondslashpos + 1, semicolpos);
            } else {
                path     = url.substring(slashpos, secondslashpos);
                database = urlImage.substring(secondslashpos + 1, semicolpos);
            }

            props.setProperty("port", port);
            props.setProperty("host", host);
            props.setProperty("path", path);
        } else {
            if (type == S_MEM) {
                database = urlImage.substring(pos, semicolpos);
            } else {
                database = url.substring(pos, semicolpos);
            }
        }

        props.setProperty("database", database);

        return props;
    }

    static final String S_DOT        = ".";
    static final String S_MEM        = "mem:";
    static final String S_FILE       = "file:";
    static final String S_RES        = "res:";
    static final String S_ALIAS      = "alias:";
    static final String S_HSQL       = "hsql://";
    static final String S_HSQLS      = "hsqls://";
    static final String S_HTTP       = "http://";
    static final String S_HTTPS      = "https://";
    static final String S_URL_PREFIX = "jdbc:hsqldb:";
    /*
    public static void main(String[] argv) {

        parseURL("JDBC:hsqldb:HSQL://localhost:9000/mydb", true);
        parseURL(
            "JDBC:hsqldb:Http://localhost:8080/servlet/org.hsqldb.Servlet/mydb;ifexists=true",
            true);
        parseURL(
            "JDBC:hsqldb:Http://localhost/servlet/org.hsqldb.Servlet/", true);
        parseURL(
            "JDBC:hsqldb:hsql://myhost", true);
    }
    */
}
