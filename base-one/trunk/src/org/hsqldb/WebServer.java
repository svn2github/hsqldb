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

import java.sql.*;
import java.net.*;
import java.io.*;
import java.util.*;

/**
 * <font color="#009900">
 * WebServer acts as a HTTP server and is one way of using
 * the client / server mode of HSQL Database Engine. This server
 * can deliver static files and can also process database queries.
 * An applet will need only the JDBC classes to access the database.
 *
 * The WebServer can be configured with the file 'WebServer.properties'.
 * This is an example of the file:
 * <pre>
 * port=80
 * database=test
 * root=./
 * default=index.html
 * silent=true
 *
 * .htm=text/html
 * .html=text/html
 * .txt=text/plain
 * .gif=image/gif
 * .class=application/octet-stream
 * .jpg=image/jpeg
 * .jgep=image/jpeg
 * .zip=application/x-zip-compressed</pre>
 * Root: use / as separator even for DOS/Windows, it will be replaced<BR>
 * Mime-types: file ending must be lowercase<BR>
 * </font>
 */
public class WebServer {

    static final String mServerName = "HypersonicSQL/1.0";
    String              mRoot;
    String              mDefaultFile;
    char                mPathSeparatorChar;
    boolean             mSilent;
    Database            mDatabase;
    Properties          mProperties;

    /**
     * Method declaration
     *
     *
     * @param arg
     */
    public static void main(String arg[]) {

        WebServer w = new WebServer();

        w.run(arg);
    }

    /**
     * Method declaration
     *
     *
     * @param arg
     */
    private void run(String arg[]) {

        ServerSocket socket = null;

        try {
            Properties prop = new Properties();

            mProperties = prop;

            // load parameters from properties file
            File f = new File("WebServer.properties");

            if (f.exists()) {
                FileInputStream fi = new FileInputStream(f);

                prop.load(fi);
                fi.close();
            }

            // overwrite parameters with command line parameters
            for (int i = 0; i < arg.length; i++) {
                String p = arg[i];

                if (p.equals("-?")) {
                    printHelp();
                }

                if (p.charAt(0) == '-') {
                    prop.put(p.substring(1), arg[i + 1]);

                    i++;
                }
            }

            int    port = Integer.parseInt(prop.getProperty("port", "80"));
            String database = prop.getProperty("database", "test");

            mRoot        = prop.getProperty("root", "./");
            mDefaultFile = prop.getProperty("default", "index.html");
            mSilent = prop.getProperty("silent",
                                       "true").equalsIgnoreCase("true");

            if (prop.getProperty("trace", "false").equalsIgnoreCase("true")) {
                DriverManager.setLogStream(System.out);
            }

            socket             = new ServerSocket(port);
            mPathSeparatorChar = File.separatorChar;

            trace("port    =" + port);
            trace("database=" + database);
            trace("root    =" + mRoot);
            trace("default =" + mDefaultFile);
            trace("silent  =" + mSilent);

            mDatabase = new Database(database);

            System.out.println("WebServer " + jdbcDriver.VERSION
                               + " is running");
            System.out.println("Press [Ctrl]+[C] to abort");
        } catch (Exception e) {
            traceError("WebServer.run/init: " + e);

            return;
        }

        try {
            while (true) {
                Socket              s = socket.accept();
                WebServerConnection c = new WebServerConnection(s, this);

                c.start();
            }
        } catch (IOException e) {
            traceError("WebServer.run/loop: " + e.getMessage());
        }
    }

    /**
     * Method declaration
     *
     */
    void printHelp() {

        System.out.println(
            "Usage: java WebServer [-options]\n" + "where options include:\n"
            + "    -port <nr>            port where the server is listening\n"
            + "    -database <name>      name of the database\n"
            + "    -root <path>          root path for sending files\n"
            + "    -default <file>       default file when filename is missing\n"
            + "    -silent <true/false>  false means display all queries\n"
            + "    -trace <true/false>   display print JDBC trace messages\n"
            + "The command line arguments override the values in the properties file.");
        System.exit(0);
    }

    /**
     * Method declaration
     *
     *
     * @param s
     */
    void trace(String s) {

        if (!mSilent) {
            System.out.println(s);
        }
    }

    /**
     * Method declaration
     *
     *
     * @param s
     */
    void traceError(String s) {
        System.out.println(s);
    }
}
