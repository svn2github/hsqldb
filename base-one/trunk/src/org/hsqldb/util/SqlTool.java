/* Copyright (c) 2001-2003, The HSQL Development Group
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
import org.hsqldb.lib.java.javaSystem;

/**
 * Sql Tool.  A command-line and/or interactive SQL tool.
 *
 * @version $Revision: 1.1 $
 * (Note:  For every Javadoc block comment, I'm using a single blank line 
 *  immediately after the description, just like's Sun's examples in
 *  their Coding Conventions document).
 */
public class SqlTool {
    /*
     * This is starting off from a copy of ScriptTool.java.
     * I intend to use existing Hsqldb objects as much as possible.
     */

    private static Properties pProperties = new Properties();
    private Connection        cConn;
    private Statement         sStatement;
    //private boolean           BATCH = true;
    private boolean           BATCH = false;
    private String            EKW   = new String("go");
    private boolean           EOF   = false;

    final static private String DEFAULT_RCFILE =
            System.getProperty("user.home") + "/hsqldb.rc";

    /**
     * All the info we need to connect up to a database.
     * If it is anticipated that SqlTool.execute() will be executed
     * from other code directly rather than from SqlTool.main(), then
     * make this a global-level class.
     */
    private static class ConnectData {
        public void report() {
            System.err.println("urlid: " + id + ", url: " + url
                + ", username: " + username + ", password: " + password);
        }
        public ConnectData(String inFile, String dbKey) throws Exception {
            File file = new File((inFile == null) ? DEFAULT_RCFILE : inFile);
            if (!file.canRead()) {
                throw new IOException("Please set up rc file '"
                       + file + "'");
            }
            // System.err.println("Using RC file '" + file + "'");
            StringTokenizer tokenizer = null;
            boolean thisone = false;
            String s;
            String keyword, value;
            BufferedReader br = new BufferedReader(new FileReader(file));
            int linenum = 0;
            while ((s = br.readLine()) != null) {
                ++linenum;
                s = s.trim();
                if (s.length() == 0) {
                    continue;
                }
                if (s.charAt(0) == '#') {
                    continue;
                }
                tokenizer = new StringTokenizer(s);
                if (tokenizer.countTokens() != 2) {
                    throw new Exception("Bad line " + linenum + " in '" + file
                            + "':  " + s);
                }
                keyword = tokenizer.nextToken();
                value = tokenizer.nextToken();
                if (keyword.equals("urlid")) {
                    if (value.equals(dbKey)) {
                        if (id == null) {
                            id  = dbKey;
                            thisone = true;
                        } else {
                            throw new Exception("Key '" + dbKey
                                    + " redefined at"
                                    + " line " + linenum + " in '" + file);
                        }
                    } else {
                        thisone = false;
                    }
                    continue;
                }
                if (thisone) {
                    if (keyword.equals("url")) {
                        url = value;
                    } else if (keyword.equals("username")) {
                        username = value;
                    } else if (keyword.equals("password")) {
                        password = value;
                    } else {
                        throw new Exception("Bad line " + linenum + " in '"
                                + file + "':  " + s);
                    }
                }
            }
            if (url == null || username == null || password == null) {
                throw new Exception("url or username or password not set "
                        + "for '" + dbKey + "' in file '" + file + "'");
            }
        }
        String id = null;
        String url = null;
        String username = null;
        String password = null;
    }

    static final private String SYNTAX_MESSAGE =
            "Usage: java SqlTool [--optname [optval...]] urlid [file1.sql...]\n"
            + "where arguments are:\n"
            + "    --help                   Prints this message\n"
            + "    --rcfile /file/path.rc   Connect Info File\n"
            + "    --driver a.b.c.Driver    Non-hsqldb JDBC driver class\n"
        + "    urlid                    ID of url/userame/password in rcfile\n"
            + "    file1.sql...             SQL files to be executed";

    private static class BadCmdline extends Exception {};
    private static BadCmdline bcl = new BadCmdline();

    /**
     * Main method.
     * Like most main methods, this is not intended to be thread-safe.
     *
     * @param arg
     */
    public static void main(String arg[]) {
        String rcFile = null;
        String driver = null;
        String targetDb = null;
        File[] scriptFiles = null;
        int i = -1;

        try {
            while ((i + 1 < arg.length) && arg[i+1].startsWith("--")) {
                i++;
                if (arg[i].length() == 2) break; // "--"
                if (arg[i].substring(2).equals("help")) {
                    System.out.println(SYNTAX_MESSAGE);
                    System.exit(0);
                }
                if (arg[i].substring(2).equals("rcfile")) {
                    if (++i == arg.length) throw bcl;
                    rcFile = arg[i];
                    continue;
                }
                if (arg[i].substring(2).equals("driver")) {
                    if (++i == arg.length) throw bcl;
                    driver = arg[i];
                    continue;
                }
                throw bcl;
            }
            if (++i == arg.length) throw bcl;
            targetDb = arg[i];
            int scriptIndex = 0;
            if (arg.length > i + 1) {
                scriptFiles = new File[arg.length - i - 1];
System.err.println("scriptFiles has " + scriptFiles.length + " elements");
                while (i + 1 < arg.length) {
                    scriptFiles[scriptIndex++]  = new File(arg[++i]);
                }
            }
        } catch (BadCmdline bcl) {
            System.err.println(SYNTAX_MESSAGE);
            System.exit(2);
        }
        SerialFileReader sfr = null;
        try {
            sfr = new SerialFileReader(scriptFiles);
        } catch (Exception e) {
            System.err.println(
                    "Failed to obtain SQL script file(s).  " + e + ": "
                     + e.getMessage());
            System.exit(1);
        }
        ConnectData conData = null;
        try {
            conData = new ConnectData(rcFile, targetDb);
        } catch (Exception e) {
            System.err.println(
                    "Failed to retrieve connection info for database '"
                    + targetDb + "': " + e.getMessage());
            System.exit(1);
        }
        // conData.report();

        (new SqlTool()).execute(conData, sfr, driver);
        System.exit(0);
    }    // end main

    /**
     * Execute the given sql files.
     * To run this without SqlTool.main(), change the scope of
     * ConnectData as needed.
     * It's thread-safe but single-threaded (synchronized).  See the
     * class Javadocs for justification.
     */
    public synchronized void execute(ConnectData cData,
            SerialFileReader sfReader, String inDriver) {
        EOF = false;

        Properties     p        = pProperties;
        String driver = ((inDriver == null)
                         ? "org.hsqldb.jdbcDriver"
                         : inDriver);
        boolean log = p.getProperty("log", "false").equalsIgnoreCase("true");

        //BATCH = p.getProperty("batch", "true").equalsIgnoreCase("true");

        if (log) {
            trace("driver   = " + driver);
            trace("url      = " + cData.url);
            trace("username = " + cData.username);
            trace("password = " + cData.password);
            //trace("scripts  = " + script);
            trace("log      = " + log);
            trace("batch    = " + BATCH);
            javaSystem.setLogToSystem(true);
        }

        try {
            // As described in the JDBC FAQ:
            // http://java.sun.com/products/jdbc/jdbc-frequent.html;
            // Why doesn't calling class.forName() load my JDBC driver?
            // There is a bug in the JDK 1.1.x that can cause Class.forName() to fail.
            // new org.hsqldb.jdbcDriver();
            Class.forName(driver).newInstance();

            cConn = DriverManager.getConnection(cData.url, cData.username,
                                                cData.password);
        } catch (Exception e) {
            System.err.println(
                    "Failed to get a connection to " + cData.url
                    + ".  " + e.getMessage());
            //e.printStackTrace();
            // How about not continuing as if nothing is wrong?
            throw new RuntimeException(e);
        }

        try {
            sStatement = cConn.createStatement();

            String sql;

            while ((sql = fileToString(sfReader)) != null) {
                if (sql.length() == 1) {
                    continue;
                }

                if (log) {
                    trace("SQL (" + sfReader.getCurFileName() + ':'
                            + sfReader.getCurLine() + ") : "
                          + sql.substring(0, sql.length() - 2));
                }

                sStatement.execute(sql);

                ResultSet results     = sStatement.getResultSet();
                int       updateCount = sStatement.getUpdateCount();

                if (updateCount == -1) {
                    trace(toString(results));
                } else {
                    trace("update count " + updateCount);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error at "
                    + sfReader.getCurFileName() + ':'
                    + sfReader.getCurLine() + ": " + e);
        }

        try {
            cConn.close();
            sfReader.close();
        } catch (Exception ce) {}
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
    private String fileToString(BufferedReader in) {

        if (EOF) {
            return null;
        }

        EOF = true;

        StringBuffer a = new StringBuffer();

        try {
            String line;

            while ((line = in.readLine()) != null) {
                if (BATCH) {
                    if (line.startsWith("print ")) {
                        trace("\n" + line.substring(5));

                        continue;
                    }

                    if (line.equalsIgnoreCase(EKW)) {
                        EOF = false;

                        break;
                    }
                }

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
}
