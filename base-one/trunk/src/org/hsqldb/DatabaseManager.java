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
                              String password) throws HsqlException {

        Database db = getDatabase(type, path);

        return db.connect(user, password);
    }

    static Session getSession(String type, String path,
                              int id) throws HsqlException {

        Database db = lookupDatabaseObject(type, path);

        return db.sessionManager.getSession(id);
    }

    /**
     * This has to be improved once a threading model is in place.
     */

    static Database getDatabase(String type,
                                String path) throws HsqlException {

        Database db = getDatabaseObject(type, path);

        switch (db.getState()) {

            case Database.DATABASE_ONLINE :
                break;
            case Database.DATABASE_SHUTDOWN :
                db.open();
                break;

            case Database.DATABASE_CLOSING :
            case Database.DATABASE_OPENING :

        }

        return db;
    }

    /**
     * The access counters maintained by this and other methods are not
     * actually used in the engine.
     */

    static synchronized Database getDatabaseObject(String type,
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

        db = (Database) databaseMap.get(key);

        if (db == null) {
            db = new Database(path, path, type);

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

    static synchronized void releaseSession(Database database) throws HsqlException {

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
     * url: the original url
     * connection_type: a static string that indicate the protocol. If the
     * url does not begin with a valid protocol, null is returned by this
     * method instead of the properties object.<br>
     * database: database name. For memory and networked modes, this is
     * returned in lowercase, for file or resource databases the original
     * case of characters is preserved. Returns empty string if name is not
     * present in the url. For Servlet connections, database consists of the
     * absolute path of the HsqlServlet/database name<br>
     * host: name of host in networked modes in lowercase<br>
     * port: port number in networked mode. Uses the default port number
     * for each protocol if port number is not in the url <br>
     *
     * Additional connection properties specified as key/value pairs.
     *
     * @return null returned if url does not begin with valid protocol or the
     * part that should represent the port is not an integer.
     *
     */
    public static HsqlProperties parseURL(String url, boolean hasPrefix) {

        String urlImage = url.toLowerCase();

        if (hasPrefix &&!urlImage.startsWith(S_URL_PREFIX)) {
            return null;
        }

        HsqlProperties props     = new HsqlProperties();
        int            pos       = hasPrefix ? S_URL_PREFIX.length()
                                                 : 0;
        //
        String host;

        int            port      = 0;
        String database;

        String         type      = null;
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

        int nextPos = url.indexOf(';', pos);

        if (nextPos < 0) {
            nextPos = url.length();
        }

        props.setProperty("connection_type", type);
/*
        if (type == S_FILE || type == S_RES) {
            url = url.substring(pos, nextPos);
        } else {
            url = urlImage.substring(pos, nextPos);
        }
*/
        url = url.substring(pos, nextPos);

        if (nextPos != urlImage.length()) {
            String arguments = urlImage.substring(nextPos + 1,
                                                  urlImage.length());
            HsqlProperties extraProps =
                HsqlProperties.delimitedArgPairsToProps(arguments, "=", ";",
                    null);

            //todo - check if properties have valid names / values
            props.addProperties(extraProps);
        }

        if (isNetwork) {
            pos = url.indexOf('/');

            if (pos < 0) {
                database = "";
            } else {
                database = url.substring(pos + 1, url.length());
                url      = url.substring(0, pos);
            }

            pos = url.indexOf(':');

            if (pos >= 0) {
                try {
                    port = Integer.parseInt(url.substring(pos + 1));
                } catch (NumberFormatException e) {
                    return null;
                }

                host = url.substring(0, pos);
            } else {
                host = url;
            }

            props.setProperty("port", port);
            props.setProperty("host", host);
        } else {
            database = url;
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
}
