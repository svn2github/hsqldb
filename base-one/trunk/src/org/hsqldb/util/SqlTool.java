/* Copyright (c) 2001-2004, The HSQL Development Group
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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;

/* $Id: SqlTool.java,v 1.25 2004/05/14 15:19:44 unsaved Exp $ */

/**
 * Sql Tool.  A command-line and/or interactive SQL tool.
 * (Note:  For every Javadoc block comment, I'm using a single blank line
 *  immediately after the description, just like's Sun's examples in
 *  their Coding Conventions document).
 *
 * See JavaDocs for the main method for syntax of how to run.
 *
 * @see @main()
 * @version $Revision: 1.25 $
 * @author Blaine Simpson
 */
public class SqlTool {
    final static private String DEFAULT_JDBC_DRIVER = "org.hsqldb.jdbcDriver";
    static private Connection   conn;
    // N.b. the following is static!
    static private boolean noexit;   // Whether System.exit() may be called.
    final static private String DEFAULT_RCFILE =
        System.getProperty("user.home") + "/sqltool.rc";

    /**
     * All the info we need to connect up to a database.
     * If it is anticipated that SqlTool.execute() will be executed
     * from other code directly rather than from SqlTool.main(), then
     * make this a global-level class.
     * I expect other Java code to invoke SqlFile.execute(), but not
     * anything in this class.
     */
    private static class ConnectData {
        /**
         * Just for testing and debugging.
         */
        public void report() {
            System.err.println("urlid: " + id + ", url: " + url
                    + ", username: " + username + ", password: " + password);
        }

        /**
         * Creates a ConnectDataObject by looking up the given key in the
         * given authentication file.
         *
         * @param String dbKey Key to look up in the file.
         * @param inFile File containing the authentication information.
         */
        public ConnectData(String inFile, String dbKey) throws Exception {
            File file = new File((inFile == null) ? DEFAULT_RCFILE : inFile);
            if (!file.canRead()) {
                throw new IOException("Please set up authentication file '"
                                      + file + "'");
            }
            // System.err.println("Using RC file '" + file + "'");
            StringTokenizer tokenizer = null;
            boolean         thisone   = false;
            String          s;
            String          keyword, value;
            int             linenum = 0;
            BufferedReader  br = new BufferedReader(new FileReader(file));
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
                if (tokenizer.countTokens() == 1) {
                    keyword = tokenizer.nextToken();
                    value   = "";
                } else if (tokenizer.countTokens() > 1) {
                    keyword = tokenizer.nextToken();
                    value   = tokenizer.nextToken("").trim();
                } else {
                    throw new Exception("Corrupt line " + linenum + " in '"
                                        + file + "':  " + s);
                }
                if (dbKey == null) {
                    if (keyword.equals("urlid")) {
                        System.out.println(value);
                    }
                    continue;
                }
                if (keyword.equals("urlid")) {
                    if (value.equals(dbKey)) {
                        if (id == null) {
                            id      = dbKey;
                            thisone = true;
                        } else {
                            throw new Exception("Key '" + dbKey
                                    + " redefined at" + " line "
                                    + linenum + " in '" + file);
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
                    } else if (keyword.equals("driver")) {
                        driver = value;
                    } else if (keyword.equals("charset")) {
                        charset = value;
                    } else if (keyword.equals("password")) {
                        password = value;
                    } else {
                        throw new Exception("Bad line " + linenum + " in '"
                                            + file + "':  " + s);
                    }
                }
            }
            if (dbKey == null) {
                return;
            }
            if (url == null || username == null || password == null) {
                throw new Exception("url or username or password not set "
                        + "for '" + dbKey + "' in file '" + file + "'");
            }
        }
        String id       = null;
        String url      = null;
        String username = null;
        String password = null;
        String driver   = null;
        String charset  = null;
    }

    static final private String SYNTAX_MESSAGE =
        "Usage: java [-Dsqlfile.charset=x.y.z*] org.hsqldb.util.SqlTool \\\n"
        + "    [--optname [optval...]] urlid [file1.sql...]\n"
        + "where arguments are:\n"
        + "    --help                   Prints this message\n"
        + "    --list                   List urlids in the rcfile\n"
        + "    --noinput                Do not read stdin (dflt if sql file(s) given)\n"
        + "    --debug                  Print Debug info to stderr\n"
        + "    --sql \"SQL;\"             Execute given SQL before stdin/files,\n"
        + "                             where \"SQL;\" consists of SQL command(s) like\n"
        + "                             in an SQL file, and may contain line breaks\n"
        + "    --rcfile /file/path.rc   Connect Info File [$HOME/sqltool.rc]\n"
        + "    --driver a.b.c.Driver*   JDBC driver class ["
        + DEFAULT_JDBC_DRIVER + "]\n"
        + "    urlid                    ID of url/userame/password in rcfile\n"
        + "    file1.sql...             SQL files to be executed [stdin]\n"
        + "                             "
        + "(Use '-' for non-interactively stdin)\n"
        + "* items may, alternatively, be set per-urlid in the rc file.";

    /** Utility nested class for internal use. */
    private static class BadCmdline extends Exception {};

    /** Utility object for internal use. */
    private static BadCmdline bcl = new BadCmdline();

    /** Nested class for external callers of SqlTool.main() */
    public static class SqlToolException extends Exception {
        public SqlToolException() { super(); }
        public SqlToolException(String s) { super(s); }
    }

    /**
     * Exit the main() method by either throwing an exception or exiting JVM.
     *
     * Call return() right after you call this method, because this method
     * will not exit if (noexit is true && retval == 0).
     */
    static private void exitMain(int retval) throws SqlToolException {
        exitMain(retval, null);
    }

    /**
     * Exit the main() method by either throwing an exception or exiting JVM.
     *
     * Call return() right after you call this method, because this method
     * will not exit if (noexit is true && retval == 0).
     */
    static private void exitMain(int retval, String msg)
    throws SqlToolException {
        if (noexit) {
            if (retval == 0) {
                return;
            } else if (msg == null) {
                throw new SqlToolException();
            } else {
                throw new SqlToolException(msg);
            }
        } else {
            if (msg != null) {
                ((retval == 0) ? System.out : System.err).println(msg);
            }
            System.exit(retval);
        }
    }

    /**
     * Connect to a JDBC Database and execute the commands given on
     * stdin or in SQL file(s).
     * Like most main methods, this is not intended to be thread-safe.
     *
     * @param arg  Run "java... org.hsqldb.util.SqlTool --help" for syntax.
     * @throws SqlToolException May be thrown only if the system property
     *                          'sqltool.noexit' is set (to anything).
     */
    public static void main(String arg[]) throws SqlToolException {
        /*
         * The big picture is, we parse input args; load a ConnectData;
         * get a JDBC Connection with the ConnectData; instantiate and
         * execute as many SqlFiles as we need to.
         */
        String  rcFile      = null;
        File    tmpFile     = null;
        String  sqlText     = null;
        String  driver      = null;
        String  targetDb    = null;
        boolean debug       = false;
        File[]  scriptFiles = null;
        int     i           = -1;
        boolean listMode    = false;
        boolean interactive = false;
        boolean noinput     = false;

        noexit = System.getProperty("sqltool.noexit") != null;
        try {
            while ((i + 1 < arg.length) && arg[i + 1].startsWith("--")) {
                i++;
                if (arg[i].length() == 2) {
                    break;    // "--"
                }
                if (arg[i].substring(2).equals("help")) {
                    exitMain(0, SYNTAX_MESSAGE);
                    return;
                }
                if (arg[i].substring(2).equals("list")) {
                    listMode = true;
                    continue;
                }
                if (arg[i].substring(2).equals("rcfile")) {
                    if (++i == arg.length) {
                        throw bcl;
                    }
                    rcFile = arg[i];
                    continue;
                }
                if (arg[i].substring(2).equals("sql")) {
                    if (++i == arg.length) {
                        throw bcl;
                    }
                    sqlText = arg[i];
                    continue;
                }
                if (arg[i].substring(2).equals("debug")) {
                    debug = true;
                    continue;
                }
                if (arg[i].substring(2).equals("noinput")) {
                    noinput = true;
                    continue;
                }
                if (arg[i].substring(2).equals("driver")) {
                    if (++i == arg.length) {
                        throw bcl;
                    }
                    driver = arg[i];
                    continue;
                }
                throw bcl;
            }
            if (!listMode) {
                if (++i == arg.length) {
                    throw bcl;
                }
                targetDb = arg[i];
            }
            int scriptIndex = 0;
            if (sqlText != null) {
                try {
                    tmpFile = File.createTempFile("sqltool-", ".sql");
                    //(new java.io.FileWriter(tmpFile)).write(sqlText);
                    java.io.FileWriter fw = new java.io.FileWriter(tmpFile);
                    fw.write(sqlText);
                    fw.flush();
                    fw.close();
                } catch (IOException ioe) {
                    exitMain(4, 
                        "Failed to write given sql to temp file: " + ioe);
                    return;
                }
            }
            interactive = (arg.length <= i + 1);
            if ((arg.length > i + 1)
                    && (arg.length != i + 2 ||!arg[i + 1].equals("-"))) {
                // I.e., if there are any SQL files specified.
                noinput     = true;
                scriptFiles = new File[arg.length - i - 1];
                if (debug) {
                    System.err.println("scriptFiles has "
                                       + scriptFiles.length + " elements");
                }
                while (i + 1 < arg.length) {
                    scriptFiles[scriptIndex++] = new File(arg[++i]);
                }
            }
        } catch (BadCmdline bcl) {
            exitMain(2, SYNTAX_MESSAGE);
            return;
        }
        ConnectData conData = null;
        try {
            conData = new ConnectData(rcFile, targetDb);
        } catch (Exception e) {
            exitMain(1, "Failed to retrieve connection info for database '"
                + targetDb + "': " + e.getMessage());
            return;
        }
        if (listMode) {
            exitMain(0);
            return;
        }
        if (debug) {
            conData.report();
        }
        if (driver == null) {
            // If user didn't set driver on command-line.
            driver = ((conData.driver == null) ? DEFAULT_JDBC_DRIVER
                                               : conData.driver);
        }
        if (System.getProperty("sqlfile.charset") == null
                && conData.charset != null) {
            System.setProperty("sqlfile.charset", conData.charset);
        }
        try {

            // As described in the JDBC FAQ:
            // http://java.sun.com/products/jdbc/jdbc-frequent.html;
            // Why doesn't calling class.forName() load my JDBC driver?
            // There is a bug in the JDK 1.1.x that can cause Class.forName()
            // to fail. // new org.hsqldb.jdbcDriver();
            Class.forName(driver).newInstance();
            conn = DriverManager.getConnection(conData.url, conData.username,
                                               conData.password);
        } catch (Exception e) {
            //e.printStackTrace();
            // Let's not continue as if nothing is wrong.
            exitMain(10, "Failed to get a connection to " + conData.url
                   + ".  " + e.getMessage());
            return;
        }
        File[] emptyFileArray      = {};
        File[] singleNullFileArray = { null };
        if (scriptFiles == null) {
            // I.e., if no SQL files given on command-line.
            // Input file list is either nothing or {null} to read stdin.
            scriptFiles = (noinput ? emptyFileArray
                                   : singleNullFileArray);
        }
        SqlFile[] sqlFiles = new SqlFile[scriptFiles.length
                + ((tmpFile == null) ? 0 : 1)];
        try {
            int fileIndex = 0;
            if (tmpFile != null) {
                sqlFiles[fileIndex++] = new SqlFile(tmpFile, false);
            }
            for (int j = 0; j < scriptFiles.length; j++) {
                sqlFiles[fileIndex++] = new SqlFile(scriptFiles[j],
                                                    interactive);
            }
        } catch (IOException ioe) {
            try {
                conn.close();
            } catch (Exception e) {}
            exitMain(2, ioe.getMessage());
            return;
        }
        int retval = 0;    // Value we will return via System.exit().
        try {
            for (int j = 0; j < sqlFiles.length; j++) {
                sqlFiles[j].execute(conn);
            }
        } catch (IOException ioe) {
            System.err.println("Failed to execute SQL:  " + ioe.getMessage());
            retval = 3;
            // These two Exception types are handled properly inside of SqlFile.
            // We just need to return an appropriate error status.
        } catch (SqlToolError ste) {
            retval = 2;
        } catch (SQLException se) {
            retval = 1;
        }
        try {
            conn.close();
        } catch (Exception e) {}
        if (tmpFile != null &&!tmpFile.delete()) {
            System.err.println(
                "Error occurred while trying to remove temp file '" + tmpFile
                + "'");
        }
        exitMain(retval);
        return;
    }
}
