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


package org.hsqldb.util;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.sql.*;
import java.net.*;
import java.io.*;
import java.util.*;

// fredt@users 20011220 - patch 481239 by yfl@users - new class

/**
 * Script tool - command line tool to read in sql script and execute it.
 *
 *
 * @version 1.7.0
 */
public class ScriptTool {

    private static Properties pProperties = new Properties();
    private Connection        cConn;
    private Statement         sStatement;

    /**
     * Main method
     *
     *
     * @param arg
     */
    public static void main(String arg[]) {

        for (int i = 0; i < arg.length; i++) {
            String p = arg[i];

            if (p.equals("-?")) {
                printHelp();
            }

            if (p.charAt(0) == '-') {
                pProperties.put(p.substring(1), arg[i + 1]);

                i++;
            }
        }

        ScriptTool tool = new ScriptTool();

        tool.execute();
    }

    public void execute() {

        Properties p        = pProperties;
        String     driver = p.getProperty("driver", "org.hsqldb.jdbcDriver");
        String     url      = p.getProperty("url", "jdbc:hsqldb:");
        String     database = p.getProperty("database", "test");
        String     user     = p.getProperty("user", "sa");
        String     password = p.getProperty("password", "");
        String     script   = p.getProperty("script", "import.sql");
        boolean log = p.getProperty("log", "false").equalsIgnoreCase("true");

        try {
            if (log) {
                trace("driver  =" + driver);
                trace("url     =" + url);
                trace("database=" + database);
                trace("user    =" + user);
                trace("password=" + password);
                trace("script  =" + script);
                trace("log     =" + log);
                jdbcSystem.setLogToSystem(true);
            }

            // As described in the JDBC FAQ:
            // http://java.sun.com/products/jdbc/jdbc-frequent.html;
            // Why doesn't calling class.forName() load my JDBC driver?
            // There is a bug in the JDK 1.1.x that can cause Class.forName() to fail.
//            new org.hsqldb.jdbcDriver();
            Class.forName(driver).newInstance();

            cConn = DriverManager.getConnection(url + database, user,
                                                password);
        } catch (Exception e) {
            System.out.println("QueryTool.init: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            sStatement = cConn.createStatement();

            String sql = fileToString(script);

            if (log) {
                trace("SQL : " + sql);
            }

            sStatement.execute(sql);

            ResultSet results     = sStatement.getResultSet();
            int       updateCount = sStatement.getUpdateCount();

            if (updateCount == -1) {
                trace(toString(results));
            } else {
                trace("update count " + updateCount);
            }
        } catch (SQLException e) {
            System.out.println("SQL Error : " + e);
        }

        System.exit(0);
    }

    /**
     * Translate ResultSet to String representation
     * @param r
     */
    private String toString(ResultSet r) {

        try {
            if (r == null) {
                return "No Result";
            }

            ResultSetMetaData m      = r.getMetaData();
            int               col    = m.getColumnCount();
            StringBuffer      strbuf = new StringBuffer();

            for (int i = 1; i <= col; i++) {
                strbuf = strbuf.append(m.getColumnLabel(i) + "\t");
            }

            strbuf = strbuf.append("\n");

            while (r.next()) {
                for (int i = 1; i <= col; i++) {
                    strbuf = strbuf.append(r.getString(i) + "\t");

                    if (r.wasNull()) {
                        strbuf = strbuf.append("(null)\t");
                    }
                }

                strbuf = strbuf.append("\n");
            }

            return strbuf.toString();
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * Read file and convert it to string.
     */
    private String fileToString(String filename) {

        StringBuffer a = new StringBuffer();

        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            String         line;

            while ((line = in.readLine()) != null) {
                a.append(line);
                a.append('\n');
            }

            a.append('\n');

            return a.toString();
        } catch (Exception e) {
            e.printStackTrace();

            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Method declaration
     *
     *
     * @param s
     */
    private void trace(String s) {
        System.out.println(s);
    }

    /**
     * Method declaration
     *
     */
    private static void printHelp() {

        System.out.println(
            "Usage: java ScriptTool [-options]\n"
            + "where options include:\n"
            + "    -driver <classname>  name of the driver class\n"
            + "    -url <name>          first part of the jdbc url\n"
            + "    -database <name>     second part of the jdbc url\n"
            + "    -user <name>         username used for connection\n"
            + "    -password <name>     password for this user\n"
            + "    -log <true/false>    write log to system out\n"
            + "    -script <script file>   reads from script file\n");
        System.exit(0);
    }
}
