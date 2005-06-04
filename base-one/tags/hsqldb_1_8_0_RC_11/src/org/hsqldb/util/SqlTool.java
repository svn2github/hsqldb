/* Copyright (c) 2001-2005, The HSQL Development Group
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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.StringTokenizer;

/* $Id: SqlTool.java,v 1.46 2005/05/11 14:03:22 fredt Exp $ */

/**
 * Sql Tool.  A command-line and/or interactive SQL tool.
 * (Note:  For every Javadoc block comment, I'm using a single blank line
 *  immediately after the description, just like's Sun's examples in
 *  their Coding Conventions document).
 *
 * See JavaDocs for the main method for syntax of how to run.
 *
 * @see @main()
 * @version $Revision: 1.46 $
 * @author Blaine Simpson
 */
public class SqlTool {

    private static final String DEFAULT_RCFILE =
        System.getProperty("user.home") + "/sqltool.rc";
    private static Connection conn;

    // N.b. the following is static!
    private static boolean noexit;    // Whether System.exit() may be called.
    private static String  revnum = null;

    static {
        revnum = "$Revision: 1.46 $".substring("$Revision: ".length(),
                                               "$Revision: 1.46 $".length()
                                               - 2);
    }

    private static final String SYNTAX_MESSAGE =
        "Usage: java [-Dsqlfile.X=Y...] org.hsqldb.util.SqlTool \\\n"
        + "    [--optname [optval...]] urlid [file1.sql...]\n"
        + "where arguments are:\n"
        + "    --help                   Displays this message\n"
        + "    --list                   List urlids in the rcfile\n"
        + "    --noinput                Do not read stdin (default if sql file given\n"
        + "                             or --sql switch used).\n"
        + "    --stdinput               Read stdin IN ADDITION to sql files/--sql input\n"
        + "    --debug                  Print Debug info to stderr\n"
        + "    --noAutoFile             Do not execute auto.sql from home dir\n"
        + "    --autoCommit             Auto-commit JDBC DML commands\n"
        + "    --sql \"SQL; Statements\"  Execute given SQL instead of stdin (before\n"
        + "                             SQL files if any are specified) where \"SQL\"\n"
        + "                             consists of SQL command(s).  See the Guide.\n"
        + "    --rcfile /file/path.rc   Connect Info File [$HOME/sqltool.rc]\n"
        + "    --abortOnErr             Abort on Error (overrides defaults)\n"
        + "    --continueOnErr          Continue on Error (overrides defaults)\n"
        + "    --setvar NAME1=val1[,NAME2=val2...]   PL variables\n"
        + "    --driver a.b.c.Driver    JDBC driver class ["
        + RCData.DEFAULT_JDBC_DRIVER + "]\n"
        + "    urlid                    ID of url/userame/password in rcfile\n"
        + "    file1.sql...             SQL files to be executed [stdin]\n"
        + "                             "
        + "(Use '-' for non-interactively stdin).\n"
        + "See the SqlTool Manual for the supported sqltool.* System Properties.\n"
        + "SqlTool v. " + revnum + ".";

    /** Utility nested class for internal use. */
    private static class BadCmdline extends Exception {}
    ;

    /** Utility object for internal use. */
    private static BadCmdline bcl = new BadCmdline();

    /** Nested class for external callers of SqlTool.main() */
    public static class SqlToolException extends Exception {

        public SqlToolException() {
            super();
        }

        public SqlToolException(String s) {
            super(s);
        }
    }

    /**
     * Exit the main() method by either throwing an exception or exiting JVM.
     *
     * Call return() right after you call this method, because this method
     * will not exit if (noexit is true && retval == 0).
     */
    private static void exitMain(int retval) throws SqlToolException {
        exitMain(retval, null);
    }

    /**
     * Exit the main() method by either throwing an exception or exiting JVM.
     *
     * Call return() right after you call this method, because this method
     * will not exit if (noexit is true && retval == 0).
     */
    private static void exitMain(int retval,
                                 String msg) throws SqlToolException {

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
                ((retval == 0) ? System.out
                               : System.err).println(msg);
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
    public static void main(String[] arg) throws SqlToolException {

        /*
         * The big picture is, we parse input args; load a RCData;
         * get a JDBC Connection with the RCData; instantiate and
         * execute as many SqlFiles as we need to.
         */
        String  rcFile           = null;
        File    tmpFile          = null;
        String  sqlText          = null;
        String  driver           = null;
        String  targetDb         = null;
        String  varSettings      = null;
        boolean debug            = false;
        File[]  scriptFiles      = null;
        int     i                = -1;
        boolean listMode         = false;
        boolean interactive      = false;
        boolean noinput          = false;
        boolean noautoFile       = false;
        boolean autoCommit       = false;
        Boolean coeOverride      = null;
        Boolean stdinputOverride = null;

        noexit = System.getProperty("sqltool.noexit") != null;

        try {
            while ((i + 1 < arg.length) && arg[i + 1].startsWith("--")) {
                i++;

                if (arg[i].length() == 2) {
                    break;             // "--"
                }

                if (arg[i].substring(2).equals("help")) {
                    exitMain(0, SYNTAX_MESSAGE);

                    return;
                }

                if (arg[i].substring(2).equals("abortOnErr")) {
                    if (coeOverride != null) {
                        exitMain(
                            0, "Switches '--abortOnErr' and "
                            + "'--continueOnErr' are mutually exclusive");

                        return;
                    }

                    coeOverride = Boolean.FALSE;

                    continue;
                }

                if (arg[i].substring(2).equals("continueOnErr")) {
                    if (coeOverride != null) {
                        exitMain(
                            0, "Switches '--abortOnErr' and "
                            + "'--continueOnErr' are mutually exclusive");

                        return;
                    }

                    coeOverride = Boolean.TRUE;

                    continue;
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

                if (arg[i].substring(2).equals("setvar")) {
                    if (++i == arg.length) {
                        throw bcl;
                    }

                    varSettings = arg[i];

                    continue;
                }

                if (arg[i].substring(2).equals("sql")) {
                    noinput = true;    // but turn back on if file "-" specd.

                    if (++i == arg.length) {
                        throw bcl;
                    }

                    sqlText = arg[i];

                    if (sqlText.charAt(sqlText.length() - 1) != ';') {
                        sqlText += ";";
                    }

                    continue;
                }

                if (arg[i].substring(2).equals("debug")) {
                    debug = true;

                    continue;
                }

                if (arg[i].substring(2).equals("noAutoFile")) {
                    noautoFile = true;

                    continue;
                }

                if (arg[i].substring(2).equals("autoCommit")) {
                    autoCommit = true;

                    continue;
                }

                if (arg[i].substring(2).equals("stdinput")) {
                    noinput          = false;
                    stdinputOverride = Boolean.TRUE;

                    continue;
                }

                if (arg[i].substring(2).equals("noinput")) {
                    noinput          = true;
                    stdinputOverride = Boolean.FALSE;

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

                    fw.write("/* " + (new java.util.Date()) + ".  "
                             + SqlTool.class.getName()
                             + " command-line SQL. */\n\n");
                    fw.write(sqlText + '\n');
                    fw.flush();
                    fw.close();
                } catch (IOException ioe) {
                    exitMain(4, "Failed to write given sql to temp file: "
                             + ioe);

                    return;
                }
            }

            if (stdinputOverride != null) {
                noinput = !stdinputOverride.booleanValue();
            }

            interactive = (!noinput) && (arg.length <= i + 1);

            if (arg.length == i + 2 && arg[i + 1].equals("-")) {
                if (stdinputOverride == null) {
                    noinput = false;
                }
            } else if (arg.length > i + 1) {

                // I.e., if there are any SQL files specified.
                scriptFiles =
                    new File[arg.length - i - 1 + ((stdinputOverride == null ||!stdinputOverride.booleanValue()) ? 0
                                                                                                                 : 1)];

                if (debug) {
                    System.err.println("scriptFiles has "
                                       + scriptFiles.length + " elements");
                }

                while (i + 1 < arg.length) {
                    scriptFiles[scriptIndex++] = new File(arg[++i]);
                }

                if (stdinputOverride != null
                        && stdinputOverride.booleanValue()) {
                    scriptFiles[scriptIndex++] = null;
                    noinput                    = true;
                }
            }
        } catch (BadCmdline bcl) {
            exitMain(2, SYNTAX_MESSAGE);

            return;
        }

        RCData conData = null;

        try {
            conData = new RCData(new File((rcFile == null) ? DEFAULT_RCFILE
                                                           : rcFile), targetDb);
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

        try {
            conn = conData.getConnection(
                driver, System.getProperty("sqlfile.charset"),
                System.getProperty("javax.net.ssl.trustStore"));

            conn.setAutoCommit(autoCommit);

            DatabaseMetaData md = null;

            if (interactive && (md = conn.getMetaData()) != null) {
                System.out.println("JDBC Connection established to a "
                                   + md.getDatabaseProductName() + " v. "
                                   + md.getDatabaseProductVersion()
                                   + " database as '" + md.getUserName()
                                   + "'.");
            }
        } catch (Exception e) {
            e.printStackTrace();

            // Let's not continue as if nothing is wrong.
            exitMain(10,
                     "Failed to get a connection to " + conData.url + " as "
                     + conData.username + ".  " + e.getMessage());

            return;
        }

        File[] emptyFileArray      = {};
        File[] singleNullFileArray = { null };
        File   autoFile            = null;

        if (interactive &&!noautoFile) {
            autoFile = new File(System.getProperty("user.home")
                                + "/auto.sql");

            if ((!autoFile.isFile()) ||!autoFile.canRead()) {
                autoFile = null;
            }
        }

        if (scriptFiles == null) {

            // I.e., if no SQL files given on command-line.
            // Input file list is either nothing or {null} to read stdin.
            scriptFiles = (noinput ? emptyFileArray
                                   : singleNullFileArray);
        }

        int numFiles = scriptFiles.length;

        if (tmpFile != null) {
            numFiles += 1;
        }

        if (autoFile != null) {
            numFiles += 1;
        }

        SqlFile[] sqlFiles = new SqlFile[numFiles];
        HashMap   userVars = new HashMap();

        if (varSettings != null) {
            int             equals;
            String          curSetting, var, val;
            StringTokenizer allvars = new StringTokenizer(varSettings, ",");

            while (allvars.hasMoreTokens()) {
                curSetting = allvars.nextToken().trim();
                equals     = curSetting.indexOf('=');

                if (equals < 1) {
                    exitMain(24, "Var settings not of format NAME=var[,...]");

                    return;
                }

                var = curSetting.substring(0, equals).trim();
                val = curSetting.substring(equals + 1).trim();

                if (var.length() < 1 || val.length() < 1) {
                    exitMain(24, "Var settings not of format NAME=var[,...]");

                    return;
                }

                userVars.put(var, val);
            }
        }

        // We print version before execing this one.
        int interactiveFileIndex = -1;

        try {
            int fileIndex = 0;

            if (autoFile != null) {
                sqlFiles[fileIndex++] = new SqlFile(autoFile, false,
                                                    userVars);
            }

            if (tmpFile != null) {
                sqlFiles[fileIndex++] = new SqlFile(tmpFile, false, userVars);
            }

            for (int j = 0; j < scriptFiles.length; j++) {
                if (interactiveFileIndex < 0 && interactive) {
                    interactiveFileIndex = fileIndex;
                }

                sqlFiles[fileIndex++] = new SqlFile(scriptFiles[j],
                                                    interactive, userVars);
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
                if (j == interactiveFileIndex) {
                    System.out.print("SqlTool v. " + revnum
                                     + ".                        ");
                }

                sqlFiles[j].execute(conn, coeOverride);
            }
        } catch (IOException ioe) {
            System.err.println("Failed to execute SQL:  " + ioe.getMessage());

            retval = 3;

            // These two Exception types are handled properly inside of SqlFile.
            // We just need to return an appropriate error status.
        } catch (SqlToolError ste) {
            retval = 2;

            // Should not be handling SQLExceptions here!  SqlFile should handle
            // them.
        } catch (SQLException se) {
            retval = 1;
        } finally {
            try {
                conn.close();
            } catch (Exception e) {}
        }

        // Taking file removal out of final block because this is good debug
        // info to keep around if the program aborts.
        if (tmpFile != null &&!tmpFile.delete()) {
            System.err.println(
                "Error occurred while trying to remove temp file '" + tmpFile
                + "'");
        }

        exitMain(retval);

        return;
    }
}
