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

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.security.Security;
import java.security.Provider;
import org.hsqldb.lib.java.javaSystem;

// fredt@users 20020215 - patch 1.7.0 by fredt
// method rorganised to use new HsqlServerProperties class
// unsaved@users 20021113 - patch 1.7.2 - SSL support

/**
 *  WebServer acts as an HTTP server and is one way of
 *  using the client / server mode of HSQL Database Engine. This server can
 *  deliver static files and can also process database queries. An applet
 *  will need only the JDBC classes to access the database. The WebServer
 *  can be configured with the file 'webserver.properties'. This is an
 *  example of the file: <pre>
 * server.port=80
 * server.database=test
 * server.root=./
 * server.default_page=index.html
 * server.silent=true
 *
 * .htm=text/html
 * .html=text/html
 * .txt=text/plain
 * .gif=image/gif
 * .class=application/octet-stream
 * .jpg=image/jpeg
 * .jgep=image/jpeg
 * .zip=application/x-zip-compressed</pre>
 *  Root: use / as separator even for DOS/Windows, it will be replaced<BR>
 *  Mime-types: file ending must be lowercase<BR>
 *
 * @version 1.7.0
 */
public class WebServer extends Server {

    static final String mServerName = "HSQLDB/1.7.2";
    String              mRoot;
    String              mDefaultFile;
    char                mPathSeparatorChar;
    boolean             isTls        = false;
    private Method      AcceptMethod = null;

    /**
     *  Method declaration
     *
     * @param  arg
     */
    public static void main(String arg[]) {

        if (arg.length > 0) {
            String p = arg[0];

            if ((p != null) && p.startsWith("-?")) {
                printHelp();

                return;
            }
        }

        WebServer      server = new WebServer();
        HsqlProperties props  = HsqlProperties.argArrayToProps(arg, "server");

        server.setProperties(props);
        server.run();
    }

    void setProperties(HsqlProperties props) {

        isTls = (System.getProperty("javax.net.ssl.keyStore") != null);
        serverProperties = new HsqlProperties("webserver");

        try {
            serverProperties.load();
        } catch (Exception e) {
            Trace.printSystemOut(
                "webserver.properties"
                + " not found, using command line or default properties");
        }

        serverProperties.addProperties(props);
        serverProperties.setPropertyIfNotExists("server.database", "test");
        serverProperties.setPropertyIfNotExists("server.port", isTls ? "443"
                                                                     : "80");

        mRoot = serverProperties.setPropertyIfNotExists("server.root", "./");
        mDefaultFile =
            serverProperties.setPropertyIfNotExists("server.default_page",
                "index.html");

        if (serverProperties.isPropertyTrue("server.trace")) {
            javaSystem.setLogToSystem(true);
        }

        traceMessages = !serverProperties.isPropertyTrue("server.silent",
                true);
    }

    /**
     *  Method declaration
     *
     */
    private void run() {

        ServerSocket socket = null;

        try {
            int port = serverProperties.getIntegerProperty("server.port",
                isTls ? 443
                      : 80);
            String database = serverProperties.getProperty("server.database");

            Trace.printSystemOut("Opening database: " + database);
            printTraceMessages();

            mPathSeparatorChar = File.separatorChar;
            mDatabase          = new Database(database);
            socket             = null;

            if (isTls) {
                try {
                    Object[]    oaInt  = { new Integer(port) };
                    Class[]     caInt  = { int.class };
                    ClassLoader loader = getClass().getClassLoader();

                    if (loader == null) {
                        throw new IncompatibleClassChangeError(
                            "Failed to retrieve a ClassLoader (Java 1.1?).  Cannot do TLS");
                    }

                    try {
                        Security.addProvider(
                            (Provider) loader.loadClass(
                                "com.sun.net.ssl.internal.ssl.Provider")
                                    .newInstance());

                        // User may have some other Provider loaded.
                        // If not, error will be caught later
                    } catch (Exception e) {}

                    Class SSLSSF = loader.loadClass(
                        "javax.net.ssl.SSLServerSocketFactory");

                    AcceptMethod = loader.loadClass(
                        "javax.net.ssl.SSLServerSocket").getMethod(
                        "accept", null);
                    socket = (ServerSocket) SSLSSF.getMethod(
                        "createServerSocket", caInt).invoke(
                        SSLSSF.getMethod("getDefault", null).invoke(
                            null, null), oaInt);

                    Trace.printSystemOut(
                        new java.util.Date(System.currentTimeMillis())
                        + " Running with TLS/SSL-encrypted JDBC");
                } catch (SecurityException se) {
                    throw new Exception(
                        "You do not have permission to use the needed SSL resources");
                } catch (IllegalAccessException iae) {
                    throw new Exception(
                        "You do not have permission to use the needed SSL resources");
                } catch (ClassNotFoundException cnfe) {
                    throw new ClassNotFoundException("JSSE not installed");
                } catch (NoSuchMethodException nsme) {
                    throw new Exception(
                        "Failed to find an SSL method even though JSSE "
                        + "is installed:\n" + nsme);

                    // Need to unwrap the following exceptions
                } catch (InvocationTargetException ite) {
                    Throwable t = ite.getTargetException();

                    if (t.toString().endsWith("no SSL Server Sockets")) {
                        throw new Exception(
                            t.toString()
                            + "\n(If you are running Java 1.2 or 1.3, keystore could be "
                            + "invalid or password wrong)");
                    } else {
                        throw ((t instanceof Exception) ? ((Exception) t)
                                                        : ite);
                    }
                } catch (ExceptionInInitializerError eiie) {
                    Throwable t = eiie.getException();

                    if (t instanceof Exception) {
                        throw (Exception) t;
                    } else {
                        throw eiie;
                    }
                }
            } else {
                socket = new ServerSocket(port);
            }
        } catch (Exception e) {
            traceError("WebServer.run/init: " + e);

            return;
        }

        try {
            while (true) {
                Socket s = null;

                if (isTls) {
                    try {
                        s = (Socket) AcceptMethod.invoke(socket, null);
                    } catch (IllegalAccessException iae) {
                        throw new IOException(
                            "You do not have permission to use the needed SSL resources");

                        // Need to unwrap the following exceptions
                    } catch (InvocationTargetException ite) {
                        Throwable t = ite.getTargetException();

                        throw new IOException((t instanceof Exception)
                                              ? ((Exception) t).toString()
                                              : ite.toString());
                    } catch (ExceptionInInitializerError eiie) {
                        Throwable t = eiie.getException();

                        throw new IOException((t instanceof Exception)
                                              ? ((Exception) t).toString()
                                              : eiie.toString());
                    }
                } else {
                    s = socket.accept();
                }

                WebServerConnection c = new WebServerConnection(s, this,
                    isTls);

                thread = new Thread(c);

                thread.start();
            }
        } catch (IOException e) {
            traceError("WebServer.run/loop: " + e.getMessage());
        }
    }

    static void printHelp() {

        Trace.printSystemOut(
            "Usage: java WebServer [-options]\n" + "where options include:\n"
            + "    -port <nr>            port where the server is listening\n"
            + "    -database <name>      name of the database\n"
            + "    -root <path>          root path for sending files\n"
            + "    -default_page <file>  default page when page name is missing\n"
            + "    -silent <true/false>  false means display all queries\n"
            + "    -trace <true/false>   display JDBC trace messages\n"
            + "The command line arguments override the values in the webserver.properties file.");
    }

    void printTraceMessages() {

        trace("server.port        ="
              + serverProperties.getProperty("server.port"));
        trace("server.database    ="
              + serverProperties.getProperty("server.database"));
        trace("server.root        ="
              + serverProperties.getProperty("server.root"));
        trace("server.default_page="
              + serverProperties.getProperty("server.default_page"));
        trace("server.silent      ="
              + serverProperties.getProperty("server.silent"));
        Trace.printSystemOut("HSQLDB web server " + jdbcDriver.VERSION
                             + " is running");
        Trace.printSystemOut(
            "Use SHUTDOWN to close normally. Use [Ctrl]+[C] to abort abruptly");
    }
}
