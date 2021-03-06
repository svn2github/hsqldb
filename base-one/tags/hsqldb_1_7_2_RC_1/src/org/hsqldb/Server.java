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

import java.io.IOException;
import java.io.File;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;
import org.hsqldb.lib.Iterator;
import org.hsqldb.lib.HashSet;
import org.hsqldb.lib.ArrayUtil;
import org.hsqldb.lib.StopWatch;
import org.hsqldb.lib.java.javaSystem;
import org.hsqldb.lib.WrapperIterator;
import org.hsqldb.resources.BundleHandler;

// fredt@users 20020215 - patch 1.7.0
// methods reorganised to use new HsqlServerProperties class
// fredt@users 20020424 - patch 1.7.0 - shutdown without exit
// see the comments in ServerConnection.java
// unsaved@users 20021113 - patch 1.7.2 - SSL support
// boucherb@users 20030510-14 - 1.7.2 - SSL support moved to factory interface
// boucherb@users 20030510-14 - 1.7.2 - service control, JavaBean API
// fredt@users 20030916 - 1.7.2 - review and simplification

/**
 * The HSQLDB HSQL protocol network database server. <p>
 *
 * A Server object acts as a network database server and is one way of using
 * the client-server mode of HSQLDB Database Engine. Instances of this
 * class handle native HSQL protocol connections exclusively, allowing database
 * queries to be performed efficienly across the network.  Server's direct
 * descendent, WebServer, handles HTTP protocol connections exclusively,
 * allowing HSQL protocol to be tunneled over HTTP to avoid sandbox and
 * firewall issues, albeit less efficiently. <p>
 *
 * There are a number of ways to configure and start a Server instance. <p>
 *
 * When started from the command line or programatically via the main(String[])
 * method, configuration occurs in three phases, with later phases overriding
 * properties set by previous phases:
 *
 * <ol>
 *   <li>Upon construction, a Server object is assigned a set of default
 *       properties. <p>
 *
 *   <li>If it exists, properties are loaded from a file named
 *       'server.properties' in the present working directory. <p>
 *
 *   <li>The command line arguments (alternatively, the String[] passed to
 *       main()) are parsed and used to further configure the Server's
 *       properties. <p>
 *
 * </ol> <p>
 *
 * From the command line, the options are as follows: <p>
 * <pre>
 * +----------------+-------------+----------+------------------------------+
 * |    OPTION      |    TYPE     | DEFAULT  |         DESCRIPTION          |
 * +----------------+-------------+----------+------------------------------|
 * | -?             | --          | --       | prints this message          |
 * | -address       | name|number | any      | server inet address          |
 * | -port          | number      | 9001/544 | port at which server listens |
 * | -database.i    | [type]spec  | 0=test   | name of database i           |
 * | -dbname.i      | alias       | --       | url alias for database i     |
 * | -silent        | true|false  | true     | false => display all queries |
 * | -trace         | true|false  | false    | display JDBC trace messages  |
 * | -tls           | true|false  | false    | TLS/SSL (secure) sockets     |
 * | -no_system_exit| true|false  | false    | do not issue System.exit()   |
 * +----------------+-------------+----------+------------------------------+
 * </pre>
 *
 * The <em>database.i</em> and <em>dbname.i</em> options need further
 * explanation:
 *
 * <ul>
 *   <li>The value of <em>i</em> is currently limited to the range 0..9. <p>
 *
 *   <li>The value assigned to <em>database.i</em> is interpreted using the
 *       format <b>'[type]spec'</b>, where the optional <em>type</em> component
 *       is one of <b>'file:'</b>, <b>'res:'</b> or <b>'mem:'</b> and the
 *       <em>spec</em> component is interpreted in the context of the
 *       <em>type</em> component.  <p>
 *
 *       If omitted, the <em>type</em> component is taken to be
 *       <b>'file:'</b>.  <p>
 *
 *        A full description of how
 *       <b>'[type]spec'</b> values are interpreted appears in the overview for
 *       {@link org.hsqldb.jdbcConnection jdbcConnection}. <p>
 *
 *   <li>The value assigned to <em>dbname.i</em> is taken to be the key used to
 *       look up the desired database instance and thus corresponds to the
 *       <b>&lt;alias&gt;</b> component of the HSQLDB HSQL protocol database
 *       connection url:
 *       'jdbc:hsqldb:hsql[s]://host[port][/<b>&lt;alias&gt;</b>]'. <p>
 *
 *   <li>The value of <em>database.0</em> is special in that the corresponding
 *       database instance is the one to which a connection is made when
 *       the <b>&lt;alias&gt;</b> component of an HSQLDB HSQL protocol database
 *       connection url is omitted. <p>
 *
 *       This behaviour allows the previous
 *       database connection url format to work with essentially unchanged
 *       semantics.
 * </ul> <p>
 *
 * From the 'server.properties' file, options can be set similarly, using a
 * slightly different format. <p>
 *
 * Here is an example 'server.properties' file:
 *
 * <pre>
 * server.port=9001
 * server.database.0=test
 * server.dbname.0=...
 * ...
 * server.database.n=...
 * server.dbname.n=...
 * server.silent=true
 * </pre>
 *
 * Starting with 1.7.2, Server has been refactored to become a simple JavaBean
 * with non-blocking start() and stop() service methods.  It is possible to
 * configure a Server instance through the JavaBean API as well, but this
 * part of the public interface is still under review and will not be finalized
 * or documented fully until the final 1.7.2 release. <p>
 *
 * <b>Note:</b> <p>
 *
 * The 'no_system_exit' property is of particular interest. <p>
 *
 * If a Server instance is to run embedded in, say, an application server,
 * such as when the jdbcDataSource or HsqlServerFactory classes are used, it
 * is typically necessary to avoid calling System.exit() when the Server
 * instance shuts down. <p>
 *
 * By default, 'no_system_exit' is set: <p>
 *
 * <ol>
 *    <li><b>true</b> when a Server is started directly from the start()
 *        method. <p>
 *
 *    <li><b>false</b> when a Server is started from the main(String[])
 *         method.
 * </ol> <p>
 *
 * These values are natural to their context because the first case allows
 * the JVM to exit by default on Server shutdown when a Server instance is
 * started from a command line environment, whereas the second case prevents
 * a typically unwanted JVM exit on Server shutdown when a Server intance
 * is started as part of a larger framework. <p>
 *
 * @version 1.7.2
 *
 * @jmx.mbean
 *    description="HSQLDB Server"
 */
public class Server implements HsqlSocketRequestHandler {

//
    static final String serverName = "HSQLDB/1.7.2";
    protected static final int serverBundleHandle =
        BundleHandler.getBundleHandle("org_hsqldb_Server_messages", null);

//
    HashSet        serverConnSet;
    String[]       dbAlias = new String[1];
    String[]       dbType  = new String[1];
    String[]       dbPath  = new String[1];
    int[]          dbID    = new int[1];
    HsqlProperties serverProperties;

//  Currently unused
    private int maxConnections;

//
    protected String            serverId;
    protected int               serverProtocol;
    protected ThreadGroup       serverConnectionThreadGroup;
    protected HsqlSocketFactory socketFactory;
    protected ServerSocket      socket;

//
    private Thread       serverThread;
    private Throwable    serverError;
    private volatile int serverState;
    private PrintWriter  logWriter;
    private PrintWriter  errWriter;

//

    /**
     * A specialized Thread inner class in which the run() method of this
     * server executes.
     */
    private class ServerThread extends Thread {

        /**
         * Constructs a new thread in which to execute the run method
         * of this server.
         *
         * @param tg The thread group
         * @param name The thread name
         */
        ServerThread(String name) {

            super(name);

            setName(name + '@' + Integer.toString(hashCode(), 16));
        }

        /**
         * Executes the run() method of this server
         */
        public void run() {
            Server.this.run();
            trace("ServerThread.run() exited");
        }
    }

    /**
     * Creates a new Server instance handling HSQL protocol connections.
     */
    public Server() {
        this(ServerConstants.SC_PROTOCOL_HSQL);
    }

    /**
     * Creates a new Server instance handling the specified connection
     * protocol. <p>
     *
     * For example, the no-args WebServer constructor invokes this constructor
     * with ServerConstants.SC_PROTOCOL_HTTP, while the Server() no args
     * contructor invokes this constructor with
     * ServerConstants.SC_PROTOCOL_HSQL. <p>
     *
     * @param protocol the ServerConstants code indicating which
     *      connection protocol to handle
     */
    protected Server(int protocol) {
        init(protocol);
    }

    /**
     * Creates and starts a new Server.  <p>
     *
     * Allows starting a Server via the command line interface. <p>
     *
     * @param args the command line arguments for the Server instance
     */
    public static void main(String args[]) {

        Server         server;
        HsqlProperties props;
        String         propsPath;

        if (args.length > 0) {
            String p = args[0];

            if ((p != null) && p.startsWith("-?")) {
                printHelp("server.help");

                return;
            }
        }

        props = HsqlProperties.argArrayToProps(args,
                                               ServerConstants.SC_KEY_PREFIX);

        String defaultdb = props.getProperty(ServerConstants.SC_KEY_DATABASE);

        if (defaultdb != null) {
            props.setProperty(ServerConstants.SC_KEY_DATABASE + ".0",
                              defaultdb);
        }

        // Standard behaviour when started from the command line
        // is to halt the VM when the server shuts down.  This may, of
        // course, be overridden by whatever, if any, security policy
        // is in place.
        props.setPropertyIfNotExists(ServerConstants.SC_KEY_NO_SYSTEM_EXIT,
                                     "false");

        server    = new Server();
        propsPath = server.getDefaultPropertiesPath();

        server.print("Startup sequence initiated from main() method");
        server.print("Loading properties from [" + propsPath
                     + ".properties]");

        if (!server.putPropertiesFromFile(propsPath)) {
            server.print("Could not load properties from file");
            server.print("Using cli/default properties only");
        }

        server.setProperties(props);
        server.start();
    }

    /**
     * Closes all connections to this Server.
     *
     * @jmx.managed-operation
     *  impact="ACTION"
     *  description="Closes all open connections"
     */
    public synchronized void signalCloseAllServerConnections() {

        Iterator it;

        trace("signalCloseAllServerConnections() entered");

        synchronized (serverConnSet) {

            // snapshot
            it = new WrapperIterator(serverConnSet.toArray(null));
        }

        for (; it.hasNext(); ) {
            ServerConnection sc = (ServerConnection) it.next();

            trace("Closing " + sc);

            // also removes all but one connection from serverConnSet
            sc.signalClose();
        }

        trace("signalCloseAllServerConnections() exited");
    }

    protected void finalize() throws Throwable {

        if (serverThread != null) {
            releaseServerSocket();
        }
    }

    /**
     * Retrieves, in string form, this server's host address.
     *
     * @return this server's host address
     *
     * @jmx.managed-attribute
     *  access="read-write"
     *  description="Host InetAddress"
     */
    public String getAddress() {

        return socket == null
               ? serverProperties.getProperty(ServerConstants.SC_KEY_ADDRESS)
               : socket.getInetAddress().toString();
    }

    /**
     * Retrieves the url alias (external name) of the i'th database
     * that this Server hosts.
     *
     * @return the url alias of the i'th database
     *      that this Server hosts.
     *
     * @jmx.managed-attribute
     *  access="read-write"
     *  description="For hosted database"
     */
    public String getDatabaseName(int index) {
        return serverProperties.getProperty(ServerConstants.SC_KEY_DBNAME
                                            + "." + index);
    }

    /**
     * Retrieves the HSQLDB database path descriptor of the i'th database
     * that this Server hosts.
     *
     * @return the HSQLDB database path descriptor of the i'th database
     *      that this Server hosts
     *
     * @jmx.managed-attribute
     *  access="read-write"
     *  description="For hosted database"
     */
    public String getDatabasePath(int index) {
        return serverProperties.getProperty(ServerConstants.SC_KEY_DATABASE
                                            + "." + index);
    }

    /**
     * Retrieves the default port that this Server will try to use in the
     * abscence of an explicitly specified one, given the specified
     * value for whether or not to use secure sockets.
     *
     * @param isTls if true, retrieve the default port when using secure
     *      sockets, else the default port when using plain sockets
     * @return the default port used in the abscence of an explicit
     *      specification.
     */
    public int getDefaultPort(boolean isTls) {
        return isTls ? ServerConstants.SC_DEFAULT_HSQLS_SERVER_PORT
                     : ServerConstants.SC_DEFAULT_HSQL_SERVER_PORT;
    }

    /**
     * Retrieves the path that will be used by default if a null or zero-length
     * path is specified to putPropertiesFromFile().  This path does not
     * include the '.properties' file extention, which is implicit.
     *
     * @return The path that will be used by default if null is specified to
     *      putPropertiesFromFile()
     *
     * @jmx.managed-attribute
     *  access="read-only"
     *  description="Read by putPropertiesFromFile()"
     */
    public String getDefaultPropertiesPath() {
        return (new File("server")).getAbsolutePath();
    }

    /**
     * Retrieves the name of the web page served when no page is specified.
     * This attribute is relevant only when server protocol is HTTP(S).
     *
     * @return the name of the web page served when no page is specified
     *
     * @jmx.managed-attribute
     *  access="read-write"
     *  description="Used when server protocol is HTTP(S)"
     */
    public String getDefaultWebPage() {
        return "[IGNORED]";
    }

    /**
     * Retrieves a String object describing the command line and
     * properties options for this Server.
     *
     * @return the command line and properties options help for this Server
     */
    public String getHelpString() {
        return BundleHandler.getString(serverBundleHandle, "server.help");
    }

    /**
     * Retrieves the PrintWriter to which server errors are printed.
     *
     * @return the PrintWriter to which server errors are printed.
     */
    public PrintWriter getErrWriter() {
        return errWriter;
    }

    /**
     * Retrieves the PrintWriter to which server messages are printed.
     *
     * @return the PrintWriter to which server messages are printed.
     */
    public PrintWriter getLogWriter() {
        return logWriter;
    }

    /**
     * Retrieves this server's host port.
     *
     * @return this server's host port
     *
     * @jmx.managed-attribute
     *  access="read-write"
     *  description="Of ServerSocket"
     */
    public int getPort() {
        return serverProperties.getIntegerProperty(
            ServerConstants.SC_KEY_PORT, getDefaultPort(isTls()));
    }

    /**
     * Retrieves this server's product name.  <p>
     *
     * Typically, this will be something like: "HSQLDB xxx server".
     *
     * @return the product name of this server
     *
     * @jmx.managed-attribute
     *  access="read-only"
     *  description="Of Server"
     */
    public String getProductName() {
        return "HSQLDB server";
    }

    /**
     * Retrieves the server's product version, as a String.  <p>
     *
     * Typically, this will be something like: "1.x.x" or "2.x.x" and so on.
     *
     * @return the product version of the server
     *
     * @jmx.managed-attribute
     *  access="read-only"
     *  description="Of Server"
     */
    public String getProductVersion() {
        return jdbcDriver.VERSION;
    }

    /**
     * Retrieves a string respresentaion of the network protocol
     * this server offers, typically one of 'HTTP', HTTPS', 'HSQL' or 'HSQLS'.
     *
     * @return string respresentation of this server's protocol
     *
     * @jmx.managed-attribute
     *  access="read-only"
     *  description="Used to handle connections"
     */
    public String getProtocol() {
        return isTls() ? "HSQLS"
                       : "HSQL";
    }

    /**
     * Retrieves a Throwable indicating the last server error, if any. <p>
     *
     * @return a Throwable indicating the last server error
     *
     * @jmx.managed-attribute
     *  access="read-only"
     *  description="Indicating last exception state"
     */
    public Throwable getServerError() {
        return serverError;
    }

    /**
     * Retrieves a String identifying this Server object.
     *
     * @return a String identifying this Server object
     *
     * @jmx.managed-attribute
     *  access="read-only"
     *  description="Identifying Server"
     */
    public String getServerId() {
        return serverId;
    }

    /**
     * Retrieves current state of this server in numerically coded form. <p>
     *
     * Typically, this will be one of: <p>
     *
     * <ol>
     * <li>SERVER_ONLINE
     * <li>SERVER_OPENING
     * <li>SERVER_CLOSING
     * <li>SERVER_SHUTDOWN
     * </ol>
     *
     * @return this server's state code.
     *
     * @jmx.managed-attribute
     *  access="read-only"
     *  description="1:ONLINE 4:OPENING 8:CLOSING, 16:SHUTDOWN"
     */
    public int getState() {
        return serverState;
    }

    /**
     * Retrieves a character sequence describing this server's current state,
     * including the message of the last exception, if there is one and it
     * is still in context.
     *
     * @return this server's state represented as a character sequence.
     *
     * @jmx.managed-attribute
     *  access="read-only"
     *  description="State [: exception ]"
     */
    public String getStateDescriptor() {

        String    state;
        Throwable t = getServerError();

        switch (serverState) {

            case ServerConstants.SERVER_STATE_SHUTDOWN :
                state = "SHUTDOWN";
                break;

            case ServerConstants.SERVER_STATE_OPENING :
                state = "OPENING";
                break;

            case ServerConstants.SERVER_STATE_CLOSING :
                state = "CLOSING";
                break;

            case ServerConstants.SERVER_STATE_ONLINE :
                state = "ONLINE";
                break;

            default :
                state = "UNKNOWN";
                break;
        }

        return state;
    }

    /**
     * Retrieves the root context (directory) from which web content
     * is served.  This property is relevant only when the server
     * protocol is HTTP(S).  Although unlikely, it may be that in the future
     * other contexts, such as jar urls may be supported, so that pages can
     * be served from the contents of a jar or from the JVM class path.
     *
     * @return the root context (directory) from which web content is served
     *
     * @jmx.managed-attribute
     *  access="read-write"
     *  description="Context (directory)"
     */
    public String getWebRoot() {
        return "[IGNORED]";
    }

    /**
     * Assigns the specified socket to a new conection handler and
     * starts the handler in a new Thread.
     *
     * @param s the socket to connect
     */
    public void handleConnection(Socket s) {

        Thread   t;
        Runnable r;
        String   ctn;

        trace("handleConnection(" + s + ") entered");

        if (!allowConnection(s)) {
            try {
                s.close();
            } catch (Exception e) {}

            trace("allowConnection(): connection refused");
            trace("handleConnection() exited");

            return;
        }

        // Maybe set up socket options, SSL
        // Session tracing/callbacks, etc.
        if (socketFactory != null) {
            socketFactory.configureSocket(s);
        }

        if (serverProtocol == ServerConstants.SC_PROTOCOL_HSQL) {
            r   = new ServerConnection(s, this);
            ctn = ((ServerConnection) r).getConnectionThreadName();

            synchronized (serverConnSet) {
                serverConnSet.add(r);
            }
        } else {
            r   = new WebServerConnection(s, (WebServer) this);
            ctn = ((WebServerConnection) r).getConnectionThreadName();
        }

        t = new Thread(serverConnectionThreadGroup, r, ctn);

        t.start();
        trace("handleConnection() exited");
    }

    /**
     * Retrieves whether this server calls System.exit() when shutdown.
     *
     * @return true if this server does not call System.exit()
     *
     * @jmx.managed-attribute
     *  access="read-write"
     *  description="When Shutdown"
     */
    public boolean isNoSystemExit() {
        return serverProperties.isPropertyTrue(
            ServerConstants.SC_KEY_NO_SYSTEM_EXIT);
    }

    /**
     * Retrieves whether this server restarts on shutdown.
     *
     * @return true this server restarts on shutdown
     *
     * @jmx.managed-attribute
     *  access="read-write"
     *  description="Automatically"
     */
    public boolean isRestartOnShutdown() {
        return serverProperties.isPropertyTrue(
            ServerConstants.SC_KEY_AUTORESTART_SERVER);
    }

    /**
     * Retrieves whether silent mode operation was requested in
     * the server properties.
     *
     * @return if true, silent mode was requested, else trace messages
     *      are to be printed
     *
     * @jmx.managed-attribute
     *  access="read-write"
     *  description="No trace messages?"
     */
    public boolean isSilent() {
        return serverProperties.isPropertyTrue(ServerConstants.SC_KEY_SILENT);
    }

    /**
     * Retreives whether the use of secure sockets was requested in the
     * server properties.
     *
     * @return if true, secure sockets are requested, else not
     *
     * @jmx.managed-attribute
     *  access="read-write"
     *  description="Use TLS/SSL sockets?"
     */
    public boolean isTls() {
        return serverProperties.isPropertyTrue(ServerConstants.SC_KEY_TLS);
    }

    /**
     * Retrieves whether JDBC trace messages are to go to System.out or the
     * DriverManger PrintStream/PrintWriter, if any.
     *
     * @return true if tracing is on (JDBC trace messages to system out)
     *
     * @jmx.managed-attribute
     *  access="read-write"
     *  description="JDBC -> System.out?"
     */
    public boolean isTrace() {
        return serverProperties.isPropertyTrue(ServerConstants.SC_KEY_TRACE);
    }

    /**
     * Attempts to put properties from the file
     * with the specified path.  If the path is null or
     * zero length, the default path is assumed.  The file
     * extention '.properties' is implicit and should not
     * be included in the path specification.
     *
     * @param path the path of the desired properties file, without the
     *      '.properties' file extention
     * @throws RuntimeException if this server is running
     * @return true if the indicated file was read sucessfully, else false
     *
     * @jmx.managed-operation
     *  impact="ACTION"
     *  description="Reads in properties"
     *
     * @jmx.managed-operation-parameter
     *   name="path"
     *   type="java.lang.String"
     *   position="0"
     *   description="null/zero-length => default path"
     */
    public boolean putPropertiesFromFile(String path)
    throws RuntimeException {

        HsqlProperties p;
        boolean        loaded;

        if (org.hsqldb.lib.StringUtil.isEmpty(path)) {
            path = getDefaultPropertiesPath();
        } else {
            path = (new File(path)).getAbsolutePath();
        }

        trace("putPropertiesFromFile(): [" + path + ".properties]");

        p      = new HsqlProperties(path);
        loaded = false;

        try {
            loaded = p.load();
        } catch (Exception e) {}

        if (loaded) {
            setProperties(p);
        }

        return loaded;
    }

    /**
     * Puts properties from the supplied string argument.  The relevant
     * key value pairs are the same as those for the (web)server.properties
     * file format, except that the 'server.' prefix does not need to
     * be specified.
     *
     * @param s semicolon-delimited key=value pair string,
     *      e.g. k1=v1;k2=v2;k3=v3...
     * @throws RuntimeException if this server is running
     *
     * @jmx.managed-operation
     *   impact="ACTION"
     *   description="'server.' key prefix automatically supplied"
     *
     * @jmx.managed-operation-parameter
     *   name="s"
     *   type="java.lang.String"
     *   position="0"
     *   description="semicolon-delimited key=value pairs"
     */
    public void putPropertiesFromString(String s) throws RuntimeException {

        HsqlProperties p;

        if (s == null || s.length() == 0) {
            return;
        }

        trace("putPropertiesFromString(): [" + s + "]");

        p = HsqlProperties.delimitedArgPairsToProps(s, "=", ";",
                ServerConstants.SC_KEY_PREFIX);

        setProperties(p);
    }

    /**
     * Sets the InetAddress with which this servers ServerSocket  will be
     * constructed.  The special value "any" can be used to bypass explicit
     * selection, causing the ServerSocket to be constructed
     * without specifying an InetAddress.
     *
     * @param address A string representing the desired InetAddress as would be
     *    retrieved by InetAddres.getByName(), or "any" (case insensitive) to
     *    signify that server sockets should be constructed using the signature
     *    that does not specify the InetAddress.
     * @throws RuntimeException if this server is running
     *
     * @jmx.managed-operation
     */
    public void setAddress(String address) throws RuntimeException {

        if (org.hsqldb.lib.StringUtil.isEmpty(address)) {
            address = ServerConstants.SC_DEFAULT_ADDRESS;
        }

        trace("setAddress(" + address + ")");
        serverProperties.setProperty(ServerConstants.SC_KEY_ADDRESS, address);
    }

    /**
     * Sets the external name (url alias) of the i'th hosted database.
     *
     * @param name external name (url alias) of the i'th HSQLDB database
     *      instance this server is to host.
     * @throws RuntimeException if this server is running
     *
     * @jmx.managed-operation
     */
    public void setDatabaseName(int index,
                                String name) throws RuntimeException {

        trace("setDatabaseName(" + index + "," + name + ")");
        serverProperties.setProperty(ServerConstants.SC_KEY_DBNAME + "."
                                     + index, name);
    }

    /**
     * Sets the path of the hosted database.
     *
     * @param path The path of the HSQLDB database instance this server
     *      is to host. The special value '.' can be used to specify a
     *      non-persistent, 100% in-memory mode database instance.
     * @throws RuntimeException if this server is running
     *
     * @jmx.managed-operation
     */
    public void setDatabasePath(int index,
                                String path) throws RuntimeException {

        trace("setDatabasePath(" + index + "," + path + ")");
        serverProperties.setProperty(ServerConstants.SC_KEY_DATABASE + "."
                                     + index, path);
    }

    /**
     * Sets the name of the web page served when no page is specified.
     *
     * @param file the name of the web page served when no page is specified
     *
     * @jmx.managed-operation
     */
    public void setDefaultWebPage(String file) {

        trace("setDefaultWebPage(" + file + ")");

        if (serverProtocol != ServerConstants.SC_PROTOCOL_HTTP) {
            return;
        }

        serverProperties.setProperty(ServerConstants.SC_KEY_WEB_DEFAULT_PAGE,
                                     file);
    }

    /**
     * Sets the server listen port.
     *
     * @param port the port at which the server listens
     * @throws RuntimeException if this server is running
     *
     * @jmx.managed-operation
     */
    public void setPort(int port) throws RuntimeException {
        trace("setPort(" + port + ")");
        serverProperties.setProperty(ServerConstants.SC_KEY_PORT, port);
    }

    /**
     * Sets the PrintWriter to which server errors are logged.
     *
     * @param pw the PrintWriter to which server messages are logged
     */
    public void setErrWriter(PrintWriter pw) {
        errWriter = pw;
    }

    /**
     * Sets the PrintWriter to which server messages are logged.
     *
     * @param pw the PrintWriter to which server messages are logged
     */
    public void setLogWriter(PrintWriter pw) {
        logWriter = pw;
    }

    /**
     * Sets whether the server calls System.exit() when shutdown.
     *
     * @param noExit if true, System.exit() will not be called.
     *
     * @jmx.managed-operation
     */
    public void setNoSystemExit(boolean noExit) {

        trace("setNoSystemExit(" + noExit + ")");
        serverProperties.setProperty(ServerConstants.SC_KEY_NO_SYSTEM_EXIT,
                                     noExit);
    }

    /**
     * Sets whether this server restarts on shutdown.
     *
     * @param restart if true, this server restarts on shutdown
     *
     * @jmx.managed-operation
     */
    public void setRestartOnShutdown(boolean restart) {

        trace("setRestartOnShutdown(" + restart + ")");
        serverProperties.setProperty(
            ServerConstants.SC_KEY_AUTORESTART_SERVER, restart);
    }

    /**
     * Sets silent mode operation
     *
     * @param silent if true, then silent mode, else trace messages
     *  are to be printed
     *
     * @jmx.managed-operation
     */
    public void setSilent(boolean silent) {
        trace("setSilent(" + silent + ")");
        serverProperties.setProperty(ServerConstants.SC_KEY_SILENT, silent);
    }

    /**
     * Sets whether to use secure sockets
     *
     * @param tls true for secure sockets, else false
     * @throws RuntimeException if this server is running
     *
     * @jmx.managed-operation
     */
    public void setTls(boolean tls) throws RuntimeException {
        trace("setTls(" + tls + ")");
        serverProperties.setProperty(ServerConstants.SC_KEY_TLS, tls);
    }

    /**
     * Sets whether trace messages go to System.out or the
     * DriverManger PrintStream/PrintWriter, if any.
     *
     * @param trace if true, trace to System.out
     *
     * @jmx.managed-operation
     */
    public void setTrace(boolean trace) {

        trace("setTrace(" + trace + ")");
        serverProperties.setProperty(ServerConstants.SC_KEY_TRACE, trace);
        javaSystem.setLogToSystem(trace);
    }

    /**
     * Sets the path of the root directory from which web content is served.
     *
     * @param root the root (context) directory from which web content
     *      is served
     *
     * @jmx.managed-operation
     */
    public void setWebRoot(String root) {

        root = (new File(root)).getAbsolutePath();

        trace("setWebRoot(" + root + ")");

        if (serverProtocol != ServerConstants.SC_PROTOCOL_HTTP) {
            return;
        }

        serverProperties.setProperty(ServerConstants.SC_KEY_WEB_ROOT, root);
    }

    /**
     * Sets properties using the specified properties object
     *
     * @param p The object containing properties to set
     * @throws RuntimeException if this server is running
     */
    public void setProperties(HsqlProperties p) throws RuntimeException {

        boolean loaded;
        String  path;

        if (p != null) {
            serverProperties.addProperties(p);
            translateAddressProperty(serverProperties);
        }

        maxConnections =
            p.getIntegerProperty(ServerConstants.SC_KEY_MAX_CONNECTIONS, 16);

        javaSystem.setLogToSystem(isTrace());
    }

    /**
     * Starts this server. <p>
     *
     * If already started, this method returns immediately. Otherwise, it
     * blocks only until the server's background thread notifies the calling
     * thread that the server has either started successfully
     * of failed to do so.  If the return value is not SERVER_ONLINE,
     * getStateDescriptor() and getServerError() can be called to retrieve
     * greater detail about the failure.
     *
     * @return the server status at the point this call exits
     *
     * @jmx.managed-operation
     *  impact="ACTION_INFO"
     *  description="Invokes startup sequence; returns resulting state"
     */
    public int start() {

        trace("start() entered");

        if (serverThread != null) {
            trace("start(): serverThread != null; no action taken");

            return getState();
        }

        serverThread = new ServerThread("HSQLDB Server ");

        serverThread.start();
        trace("start() exiting");

        return getState();
    }

    /**
     * Stops this server. <p>
     *
     * If already stopped, this method returns immediately. Otherwise, it
     * blocks only until the server's background thread notifies the calling
     * thread that the server has either shtudown successfully
     * or has failed to do so.  If the return value is not SERVER_SHUTDOWN,
     * getStateDescriptor() can be called to retrieve a description of
     * the failure.
     *
     * @return the server status at the point this call exits
     *
     * @jmx.managed-operation
     *  impact="ACTION_INFO"
     *  description="Invokes shutdown sequence; returns resulting state"
     */
    public int stop() {

        trace("stop() entered");

        if (serverThread == null) {
            trace("stop() serverThread is null; no action taken");

            return getState();
        }

        releaseServerSocket();
        trace("stop() exiting");

        return getState();
    }

    /**
     * Retrieves whether the specified socket should be allowed
     * to make a connection.  By default, this method always returns
     * true, but it can be overidden to implement hosts allow-deny
     * functionality.
     *
     * @param socket the socket to test.
     */
    protected boolean allowConnection(Socket socket) {
        return true;
    }

    /**
     * Initializes this server, setting the accepted connection protocol.
     *
     * @param protocol typically either SC_PROTOCOL_HTTP or SC_PROTOCOL_HSQL
     */
    protected void init(int protocol) {

        // PRE:  This method is only called from the constructor
        serverState      = ServerConstants.SERVER_STATE_SHUTDOWN;
        serverConnSet    = new HashSet();
        serverId         = toString();
        serverId         = serverId.substring(serverId.lastIndexOf('.') + 1);
        serverProtocol   = protocol;
        serverProperties = newDefaultProperties();
        logWriter        = new PrintWriter(System.out);
        errWriter        = new PrintWriter(System.err);

        javaSystem.setLogToSystem(isTrace());
    }

    /**
     * Sets the server state value.
     *
     * @param state the new value
     */
    protected final synchronized void setState(int state) {
        serverState = state;
    }

// Package visibility for related classes and intefaces
// that may need to make calls back here.

    /**
     * This is called from org.hsqldb.DatabaseManager when a database is
     * shutdown. This shuts the server down if it is the last database
     *
     * @param action a code indicating what has happend
     */
    final void notify(int action, int id) {

        trace("notifiy(" + action + "," + id + ") entered");

        if (action != ServerConstants.SC_DATABASE_SHUTDOWN) {
            return;
        }

        releaseDatabase(id);

        boolean shutdown = true;

        for (int i = 0; i < dbID.length; i++) {
            if (dbAlias[i] != null) {
                shutdown = false;
            }
        }

        if (shutdown) {
            stop();
        }
    }

    /**
     * This releases the resources used for a database
     */
    final synchronized void releaseDatabase(int id) {

        Iterator it;

        trace("releaseDatabase(" + id + ") entered");

        for (int i = 0; i < dbID.length; i++) {
            if (dbID[i] == id) {
                dbID[i]    = 0;
                dbAlias[i] = null;
                dbPath[i]  = null;
                dbType[i]  = null;
            }
        }

        synchronized (serverConnSet) {
            it = new WrapperIterator(serverConnSet.toArray(null));
        }

        while (it.hasNext()) {
            ServerConnection sc = (ServerConnection) it.next();

            if (sc.dbID == id) {
                sc.signalClose();
                serverConnSet.remove(sc);
            }
        }

        trace("releaseDatabase(" + id + ") exiting");
    }

    /**
     * Prints the specified message, s, formatted to identify that the print
     * operation is against this server instance.
     *
     * @param msg The message to print
     */
    final synchronized void print(String msg) {

        PrintWriter writer = logWriter;

        if (writer != null) {
            writer.println("[" + serverId + "]: " + msg);
            writer.flush();
        }
    }

    /**
     * Prints value from server's resource bundle, formatted to
     * identify that the print operation is against this server instance.
     * Value may be localized according to the default JVM locale
     *
     * @param key the resource key
     */
    final void printResource(String key) {

        String          resource;
        StringTokenizer st;

        if (serverBundleHandle < 0) {
            return;
        }

        resource = BundleHandler.getString(serverBundleHandle, key);

        if (resource == null) {
            return;
        }

        st = new StringTokenizer(resource, "\n\r");

        while (st.hasMoreTokens()) {
            print(st.nextToken());
        }
    }

    /**
     * Prints the stack trace of the Throwable, t, to this Server object's
     * errWriter. <p>
     *
     * @param t the Throwable whose stack trace is to be printed
     */
    final synchronized void printStackTrace(Throwable t) {

        if (errWriter != null) {
            t.printStackTrace(errWriter);
            errWriter.flush();
        }
    }

    /**
     * Prints the specified message, s, prepended with a timestamp representing
     * the current date and time, formatted to identify that the print
     * operation is against this server instance.
     *
     * @param msg the message to print
     */
    final void printWithTimestamp(String msg) {
        print(new Timestamp(System.currentTimeMillis()) + " " + msg);
    }

    /**
     * Prints a message formatted similarly to print(String), additionally
     * identifying the current (calling) thread.
     *
     * @param s the message to print
     */
    final void trace(String msg) {

        if (!isSilent()) {
            print("[" + Thread.currentThread() + "]: " + msg);
        }
    }

    /**
     * Prints an error message to this Server object's errWriter.
     * The message is formatted similarly to print(String),
     * additionally identifying the current (calling) thread.
     *
     * @param msg the message to print
     */
    final synchronized void printError(String msg) {

        PrintWriter writer = errWriter;

        if (writer != null) {
            writer.print("[" + serverId + "]: ");
            writer.print("[" + Thread.currentThread() + "]: ");
            writer.println(msg);
            writer.flush();
        }
    }

    /**
     * Prints a description of the request encapsulated by the
     * Result argument, r.
     *
     * Printing occurs iff isSilent() is false. <p>
     *
     * The message is formatted similarly to print(String), additionally
     * indicating the connection identifier.  <p>
     *
     * For Server instances, cid is typically the value assigned to each
     * ServerConnection object that is unique amongst all such identifiers
     * in each distinct JVM session / class loader
     * context. <p>
     *
     * For WebServer instances, a single logical connection actually spawns
     * a new physical WebServerConnection object for each request, so the
     * cid is typically the underlying session id, since that does not
     * change for the duration of the logical connection.
     *
     * @param cid the connection identifier
     * @param r the request whose description is to be printed
     */
    final void traceRequest(int cid, Result r) {

        if (isSilent()) {
            return;
        }

        StringBuffer sb = new StringBuffer();

        sb.append(cid);
        sb.append(':');

        switch (r.iMode) {

            case ResultConstants.SQLPREPARE : {
                sb.append("SQLCLI:SQLPREPARE ");
                sb.append(r.getMainString());

                break;
            }
            case ResultConstants.SQLEXECDIRECT : {
                if (r.getSize() < 2) {
                    sb.append(r.getMainString());
                } else {
                    sb.append("SQLCLI:SQLEXECDIRECT:BATCHMODE\n");

                    Iterator it = r.iterator();

                    while (it.hasNext()) {
                        Object[] data = (Object[]) it.next();

                        sb.append(data[0]).append('\n');
                    }
                }

                break;
            }
            case ResultConstants.SQLEXECUTE : {
                sb.append("SQLCLI:SQLEXECUTE:");

                if (r.getSize() > 1) {
                    sb.append("BATCHMODE:");
                }

                sb.append(r.getStatementID());

                break;
            }
            case ResultConstants.SQLFREESTMT : {
                sb.append("SQLCLI:SQLFREESTMT:");
                sb.append(r.getStatementID());

                break;
            }
            case ResultConstants.GETSESSIONATTR : {
                sb.append("HSQLCLI:GETSESSIONATTR");

                break;
            }
            case ResultConstants.SETSESSIONATTR : {
                sb.append("HSQLCLI:SETSESSIONATTR:");
                sb.append("AUTOCOMMIT ");
                sb.append(r.rRoot.data[Session.INFO_AUTOCOMMIT]);
                sb.append(" CONNECTION_READONLY ");
                sb.append(r.rRoot.data[Session.INFO_CONNECTION_READONLY]);

                break;
            }
            case ResultConstants.SQLENDTRAN : {
                sb.append("SQLCLI:SQLENDTRAN:");

                switch (r.getEndTranType()) {

                    case ResultConstants.COMMIT :
                        sb.append("COMMIT");
                        break;

                    case ResultConstants.ROLLBACK :
                        sb.append("ROLLBACK");
                        break;

                    case ResultConstants.SAVEPOINT_NAME_RELEASE :
                        sb.append("SAVEPOINT_NAME_RELEASE ");
                        sb.append(r.getMainString());
                        break;

                    case ResultConstants.SAVEPOINT_NAME_ROLLBACK :
                        sb.append("SAVEPOINT_NAME_ROLLBACK ");
                        sb.append(r.getMainString());
                        break;

                    default :
                        sb.append(r.getEndTranType());
                }

                break;
            }
            case ResultConstants.SQLSTARTTRAN : {
                sb.append("SQLCLI:SQLSTARTTRAN");

                break;
            }
            case ResultConstants.SQLDISCONNECT : {
                sb.append("SQLCLI:SQLDISCONNECT");

                break;
            }
            case ResultConstants.SQLSETCONNECTATTR : {
                sb.append("SQLCLI:SQLSETCONNECTATTR:");

                switch (r.getConnectionAttrType()) {

                    case ResultConstants.SQL_ATTR_SAVEPOINT_NAME : {
                        sb.append("SQL_ATTR_SAVEPOINT_NAME ");
                        sb.append(r.getMainString());

                        break;
                    }
                    default : {
                        sb.append(r.getConnectionAttrType());
                    }
                }

                break;
            }
            default : {
                sb.append("SQLCLI:MODE:");
                sb.append(r.iMode);

                break;
            }
        }

        print(sb.toString());
    }

    /**
     * Retrieves a new default properties object for this server
     *
     * @return a new default properties object
     */
    private final HsqlProperties newDefaultProperties() {

        HsqlProperties p;
        boolean        isTls;

        p = new HsqlProperties();

        p.setProperty(ServerConstants.SC_KEY_AUTORESTART_SERVER,
                      ServerConstants.SC_DEFAULT_SERVER_AUTORESTART);
        p.setProperty(ServerConstants.SC_KEY_ADDRESS,
                      ServerConstants.SC_DEFAULT_ADDRESS);
        p.setProperty(ServerConstants.SC_KEY_DATABASE + "." + 0,
                      ServerConstants.SC_DEFAULT_DATABASE);
        p.setProperty(ServerConstants.SC_KEY_DBNAME + "." + 0, "");
        p.setProperty(ServerConstants.SC_KEY_NO_SYSTEM_EXIT,
                      ServerConstants.SC_DEFAULT_NO_SYSTEM_EXIT);

        isTls = ServerConstants.SC_DEFAULT_TLS;

        try {
            isTls = System.getProperty("javax.net.ssl.keyStore") != null;
        } catch (Exception e) {}

        p.setProperty(ServerConstants.SC_KEY_PORT, getDefaultPort(isTls));
        p.setProperty(ServerConstants.SC_KEY_SILENT,
                      ServerConstants.SC_DEFAULT_SILENT);
        p.setProperty(ServerConstants.SC_KEY_TLS, isTls);
        p.setProperty(ServerConstants.SC_KEY_TRACE,
                      ServerConstants.SC_DEFAULT_TRACE);
        p.setProperty(ServerConstants.SC_KEY_WEB_DEFAULT_PAGE,
                      ServerConstants.SC_DEFAULT_WEB_PAGE);
        p.setProperty(ServerConstants.SC_KEY_WEB_ROOT,
                      ServerConstants.SC_DEFAULT_WEB_ROOT);

        return p;
    }

    /**
     * Opens this server's database instances.
     *
     * @throws HsqlException if a database access error occurs
     */
    final void openDatabases() throws HsqlException {

        StopWatch      sw;
        String[]       dblist;
        Enumeration    enum;
        int            maxindex;
        String         key;
        int            dbnum;
        HsqlProperties dbURL;
        final String   prefix    = "server.dbname.";
        final int      prefixLen = prefix.length();

        trace("openDatabases() entered");

        dblist   = new String[10];
        enum     = serverProperties.propertyNames();
        maxindex = 0;

        try {
            for (int i = 0; enum.hasMoreElements(); ) {
                key = (String) enum.nextElement();

                if (!key.startsWith(prefix)) {
                    continue;
                }

                dbnum         = Integer.parseInt(key.substring(prefixLen));
                maxindex      = dbnum < maxindex ? maxindex
                                                 : dbnum;
                dblist[dbnum] = serverProperties.getProperty(key);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            trace("dblist: " + e.toString());
        }

        dbAlias = new String[maxindex + 1];
        dbType  = new String[maxindex + 1];
        dbPath  = new String[maxindex + 1];
        dbID    = new int[maxindex + 1];

        ArrayUtil.copyArray(dblist, dbAlias, maxindex + 1);

        for (int i = 0; i < dbAlias.length; i++) {
            dbURL     = DatabaseManager.parseURL(getDatabasePath(i), false);
            dbPath[i] = dbURL.getProperty("database");
            dbType[i] = dbURL.getProperty("connection_type");

            trace("Opening database: [" + dbType[i] + dbPath[i] + "]");

            sw = new StopWatch();

            int id = DatabaseManager.getDatabase(dbType[i], dbPath[i], this);

            sw.stop();

            dbID[i] = id;

            String msg = "Database [index=" + i + ", id=" + id + ", " + "db="
                         + dbType[i] + dbPath[i] + ", alias=" + dbAlias[i]
                         + "] opened sucessfully";

            print(sw.elapsedTimeToMessage(msg));
        }

        trace("openDatabases() exiting");
    }

    /**
     * Constructs and installs a new ServerSocket instance for this server.
     *
     * @throws Exception if it is not possible to construct and install
     *      a new ServerSocket
     */
    private final void openServerSocket() throws Exception {

        String    address;
        int       port;
        Vector    candidateAddrs;
        String    emsg;
        StopWatch sw;

        trace("openServerSocket() entered");

        if (isTls()) {
            trace("Requesting TLS/SSL-encrypted JDBC");
        }

        sw            = new StopWatch();
        socketFactory = HsqlSocketFactory.getInstance(isTls());
        address       = getAddress();
        port          = getPort();

        if (org.hsqldb.lib.StringUtil.isEmpty(address)
                || ServerConstants.SC_DEFAULT_ADDRESS.equalsIgnoreCase(
                    address.trim())) {
            socket = socketFactory.createServerSocket(port);
        } else {
            try {
                socket = socketFactory.createServerSocket(port, address);
            } catch (UnknownHostException e) {
                candidateAddrs = listLocalInetAddressNames();

                int      messageID;
                Object[] messageParameters;

                if (candidateAddrs.size() > 0) {
                    messageID         = Trace.Server_openServerSocket;
                    messageParameters = new Object[] {
                        address, candidateAddrs
                    };
                } else {
                    messageID         = Trace.Server_openServerSocket2;
                    messageParameters = new Object[]{ address };
                }

                throw new UnknownHostException(Trace.getMessage(messageID,
                        true, messageParameters));
            }
        }

        /*
         * Following line necessary for Java 1.3 on UNIX.  See accept()
         * comment elsewhere in this file.
         */
        socket.setSoTimeout(1000);
        trace("Got server socket: " + socket);
        print(sw.elapsedTimeToMessage("Server socket opened successfully"));

        if (socketFactory.isSecure()) {
            print("Using TLS/SSL-encrypted JDBC");
        }

        trace("openServerSocket() exiting");
    }

    /** Prints a timestamped message indicating that this server is online */
    private final void printServerOnlineMessage() {

        String s = getProductName() + " " + getProductVersion()
                   + " is online";

        printWithTimestamp(s);
        printResource("online.help");
    }

    /**
     * Prints a description of the server properties iff !isSilent().
     */
    private final void traceProperties() {

        Enumeration e;
        String      key;
        String      value;

        // Avoid the waste of generating each description,
        // only for trace() to silently discard it
        if (isSilent()) {
            return;
        }

        e = serverProperties.propertyNames();

        while (e.hasMoreElements()) {
            key   = (String) e.nextElement();
            value = serverProperties.getProperty(key);

            trace(key + "=" + value);
        }
    }

    /**
     * Puts this server into the SERVER_CLOSING state, closes the ServerSocket
     * and nullifies the reference to it. If the ServerSocket is already null,
     * this method exists immediately, otherwise, the result is to fully
     * shut down the server.
     */
    private final void releaseServerSocket() {

        trace("releaseServerSocket() entered");

        if (socket != null) {
            trace("Releasing server socket: [" + socket + "]");
            setState(ServerConstants.SERVER_STATE_CLOSING);

            try {
                socket.close();
            } catch (IOException e) {
                printError("Exception closing server socket");
                printError("releaseServerSocket(): " + e);
            }

            socket = null;
        }

        trace("releaseServerSocket() exited");
    }

    /**
     * Attempts to bring this server fully online by opening
     * a new ServerSocket, obtaining the hosted databases,
     * notifying the status waiter thread (if any) and
     * finally entering the listen loop if all else succeeds.
     * If any part of the process fails, then this server enters
     * its shutdown sequence.
     */
    private final void run() {

        StopWatch   sw;
        ThreadGroup tg;
        String      tgName;

        trace("run() entered");
        print("Initiating startup sequence...");
        traceProperties();

        sw = new StopWatch();

        setState(ServerConstants.SERVER_STATE_OPENING);
        setServerError(null);

        try {

            // Faster init first:
            // It is huge waste to fully open the databases, only
            // to find that the socket address is already in use
            openServerSocket();
        } catch (Exception e) {
            setServerError(e);
            printError("run()/openServerSocket(): ");
            printStackTrace(e);
            shutdown(true);

            return;
        }

        tgName = "HSQLDB Connections @"
                 + Integer.toString(serverThread.hashCode(), 16);
        tg = new ThreadGroup(tgName);

        tg.setDaemon(false);

        serverConnectionThreadGroup = tg;

        try {

            // Mount the databases this server is supposed to host.
            // This may take some time if the databases are not all
            // already open.
            openDatabases();
        } catch (Exception e) {
            setServerError(e);
            printError("run()/openDatabases(): ");
            printStackTrace(e);
            shutdown(true);

            return;
        }

        // At this point, we have a valid server socket and
        // a valid hosted database set, so its OK to start
        // listeneing for connections.
        setState(ServerConstants.SERVER_STATE_ONLINE);
        print(sw.elapsedTimeToMessage("Startup sequence completed"));
        printServerOnlineMessage();

        try {
            /*
             * This loop is necessary for UNIX w/ Sun Java 1.3 because
             * in that case the socket.close() elsewhere will not
             * interrupt this accept().
             */
            while (true) {
                try {
                    handleConnection(socket.accept());
                } catch (java.io.InterruptedIOException iioe) {}
            }
        } catch (IOException ioe) {
            if (getState() == ServerConstants.SERVER_STATE_ONLINE) {
                setServerError(ioe);
                printError(this + ".run()/handleConnection(): ");
                printStackTrace(ioe);
            }
        } catch (Throwable t) {
            trace(t.toString());
        } finally {
            shutdown(false);    // or maybe getServerError() != null?
        }
    }

    /**
     * Sets this Server's last encountered error state.
     *
     * @param t The new value for the server error
     */
    private final void setServerError(Throwable t) {
        serverError = t;
    }

    /**
     * Shuts down this server.
     *
     * @param error true if shutdown is in response to an error
     *      state, else false
     */
    private final void shutdown(boolean error) {

        StopWatch sw;

        trace("shutdown() entered");

        sw = new StopWatch();

        print("Initiating shutdown sequence...");
        releaseServerSocket();
        DatabaseManager.deRegisterServer(this);

        for (int i = 0; i < dbPath.length; i++) {
            releaseDatabase(i);
        }

        // Be nice and let applications exit if there are no
        // running connection threads
        if (serverConnectionThreadGroup != null) {
            if (!serverConnectionThreadGroup.isDestroyed()) {
                for (int i = 0; serverConnectionThreadGroup.activeCount() > 0;
                        i++) {
                    int count;

                    try {
                        wait(100);
                    } catch (Exception e) {

                        // e.getMessage();
                    }
                }

                try {
                    serverConnectionThreadGroup.destroy();
                    trace(serverConnectionThreadGroup.getName()
                          + " destroyed");
                } catch (Throwable t) {
                    trace(serverConnectionThreadGroup.getName()
                          + " not destroyed");
                    trace(t.toString());
                }
            }

            serverConnectionThreadGroup = null;
        }

        serverThread = null;

        setState(ServerConstants.SERVER_STATE_SHUTDOWN);
        print(sw.elapsedTimeToMessage("Shutdown sequence completed"));

        if (isNoSystemExit()) {
            printWithTimestamp("SHUTDOWN : System.exit() was not called");
            trace("shutdown() exited");
        } else {
            printWithTimestamp("SHUTDOWN : System.exit() is called next");
            trace("shutdown() exiting...");

            try {
                System.exit(0);
            } catch (Throwable t) {
                trace(t.toString());
            }
        }
    }

    /**
     * Prints message for the specified key, without any special
     * formatting. The message content comes from the server
     * resource bundle and thus may localized according to the default
     * JVM locale.<p>
     *
     * Uses System.out directly instead of Trace.printSystemOut() so it
     * always prints, regardless of Trace settings.
     *
     * @param key for message
     */
    protected static void printHelp(String key) {
        System.out.print(BundleHandler.getString(serverBundleHandle, key));
    }

    /**
     * Retrieves a list of Strings naming the distinct, known to be valid local
     * InetAddress names for this machine.  The process is to collect and
     * return the union of the following sets:
     *
     * <ol>
     * <li> InetAddress.getAllByName(InetAddress.getLocalHost().getHostAddress())
     * <li> InetAddress.getAllByName(InetAddress.getLocalHost().getHostName())
     * <li> InetAddress.getAllByName(InetAddress.getByName(null).getHostAddress())
     * <li> InetAddress.getAllByName(InetAddress.getByName(null).getHostName())
     * <li> InetAddress.getByName("loopback").getHostAddress()
     * <li> InetAddress.getByName("loopback").getHostname()
     * </ol>
     *
     * @return the distinct, known to be valid local
     *        InetAddress names for this machine
     */
    public static Vector listLocalInetAddressNames() {

        InetAddress   addr;
        InetAddress[] addrs;
        HashSet       set;
        Vector        out;
        StringBuffer  sb;

        set = new HashSet();
        out = new Vector();

        try {
            addr  = InetAddress.getLocalHost();
            addrs = InetAddress.getAllByName(addr.getHostAddress());

            for (int i = 0; i < addrs.length; i++) {
                set.add(addrs[i].getHostAddress());
                set.add(addrs[i].getHostName());
            }

            addrs = InetAddress.getAllByName(addr.getHostName());

            for (int i = 0; i < addrs.length; i++) {
                set.add(addrs[i].getHostAddress());
                set.add(addrs[i].getHostName());
            }
        } catch (Exception e) {}

        try {
            addr  = InetAddress.getByName(null);
            addrs = InetAddress.getAllByName(addr.getHostAddress());

            for (int i = 0; i < addrs.length; i++) {
                set.add(addrs[i].getHostAddress());
                set.add(addrs[i].getHostName());
            }

            addrs = InetAddress.getAllByName(addr.getHostName());

            for (int i = 0; i < addrs.length; i++) {
                set.add(addrs[i].getHostAddress());
                set.add(addrs[i].getHostName());
            }
        } catch (Exception e) {}

        try {
            set.add(InetAddress.getByName("loopback").getHostAddress());
            set.add(InetAddress.getByName("loopback").getHostName());
        } catch (Exception e) {}

        for (Iterator i = set.iterator(); i.hasNext(); ) {
            out.addElement(i.next());
        }

        return out;
    }

    /**
     * Translates null or zero length value for address key to the
     * special value "any" which causes ServerSockets to be constructed
     * without specifying an InetAddress.
     *
     * @param p The properties object upon which to perform the translation
     */
    private void translateAddressProperty(HsqlProperties p) {

        String address;

        if (p == null) {
            return;
        }

        address = p.getProperty(ServerConstants.SC_KEY_ADDRESS);

        if (org.hsqldb.lib.StringUtil.isEmpty(address)) {
            p.setProperty(ServerConstants.SC_KEY_ADDRESS,
                          ServerConstants.SC_DEFAULT_ADDRESS);
        }
    }
}
