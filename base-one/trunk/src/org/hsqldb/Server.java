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
 * Server acts as a database server and is one way of using
 * the client / server mode of HSQL Database Engine. This server
 * can only process database queries.
 * An applet will need only the JDBC classes to access the database.
 *
 * The Server can be configured with the file 'Server.properties'.
 * This is an example of the file:
 * <pre>
 * port=9001
 * database=test
 * silent=true
 * </font>
 */
public class Server {

    boolean  mSilent;
    Database mDatabase;

    /**
     * Method declaration
     *
     *
     * @param arg
     */
    public static void main(String arg[]) {

        Server server = new Server();

        server.run(arg);
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

            // load parameters from properties file
            File f = new File("Server.properties");

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

            int port = jdbcConnection.DEFAULT_HSQL_PORT;

            port = Integer.parseInt(prop.getProperty("port", "" + port));

            String database = prop.getProperty("database", "test");

            mSilent = prop.getProperty("silent",
                                       "true").equalsIgnoreCase("true");

            if (prop.getProperty("trace", "false").equalsIgnoreCase("true")) {
                DriverManager.setLogStream(System.out);
            }

            socket = new ServerSocket(port);

            trace("port    =" + port);
            trace("database=" + database);
            trace("silent  =" + mSilent);

            mDatabase = new Database(database);

            System.out.println("Server " + jdbcDriver.VERSION
                               + " is running");
            System.out.println("Press [Ctrl]+[C] to abort");
        } catch (Exception e) {
            traceError("Server.run/init: " + e);
            e.printStackTrace();

            return;
        }

        try {
            while (true) {
                Socket           s = socket.accept();
                ServerConnection c = new ServerConnection(s, this);

                c.start();
            }
        } catch (IOException e) {
            traceError("Server.run/loop: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Method declaration
     *
     */
    void printHelp() {

        System.out.println(
            "Usage: java Server [-options]\n" + "where options include:\n"
            + "    -port <nr>            port where the server is listening\n"
            + "    -database <name>      name of the database\n"
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
