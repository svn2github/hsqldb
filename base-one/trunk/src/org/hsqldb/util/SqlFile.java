/*
 * $Id: SqlFile.java,v 1.6 2004/01/20 17:06:18 unsaved Exp $
 *
 * Copyright (c) 2001-2003, The HSQL Development Group
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

import java.io.IOException;
import java.io.PrintStream;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

/**
 * Definitions.
 * COMMAND = Statement || Special
 * Statement = SQL Statement
 * Special =   Special Command like "\x arg..."
 */
public class SqlFile {
    private File file;
    private boolean interactive;
    private String primaryPrompt = "sql> ";
    private String contPrompt    = "  +> ";
    private Connection conn = null;

    final private static String BANNER =
"********   N.b. I have updated this explanations, but the behavior of\n" +
"           command termination has not been fixed yet.   ***********\n\n" +
            "SqlFile processor.  Enter \"\\?\" for help, \"\\q\" to quit.\n"
       + "REMEMBER TO TERMINATE EVERY SQL STATEMENT WITH EITHER\n"
       + "    ';' AT THE END OF A LINE in order to execute it\n"
       + "OR\n"
       + "    A BLANK LINE to clear the command without executing.\n"
       + "Just hit the ENTER key after a Special command to execute it.";
    final private static String HELP_TEXT =
"**********    MOST OF THE SPECIAL COMMANDS DO NOT WORK YET!!!  *******\n" +
      "SPECIAL Commands all begin with '\\', SQL Statements do not.\n\n"
      + "SPECIAL Commands begin with '\\' and execute when you hit ENTER.\n"
      + "SQL Statement lines ending with ';' cause the current statement to be executed.\n"
      + "An empty line within an SQL Statement clears it (but it can "
      + "be recalled).\n\n"
        + "SPECIAL Commands:  (* commands only available for interactive use)\n"
        + "    \\?                   Help\n"
        + "    \\! [command to run]  * Shell out\n"
        + "    \\e                   * Open last command in external editor\n"
        + "    \\p [line to print]   Print string to stdout\n"
        + "    \\s                   * Show previous commands \n"
        + "    \\-                   * reload last command\n"
        + "    \\-2                  * reload 2nd-to-last command, etc.\n"
        + "    \\q                   Quit (alternatively, end input,\n"
        + "                           like Ctrl-Z or Ctrl-D)\n\n"
        + "EXAMPLE:  To show previous commands then execute the 3rd-to-last:\n"
        + "    \\s\n"
        + "    \\-3\n"
        + "    ;\n";

    /**
     * @param inFile  inFile of null means to read stdin.
     * @param inInteractive  If true, print prompts and continue if errors
     *                       are encountered.
     */
    SqlFile(File inFile, boolean inInteractive) throws IOException {
        file = inFile;
        interactive = inInteractive;
        if (file != null && !file.canRead()) {
            throw new IOException("Can't read SQL file '" + file + "'");
        }
    }

    SqlFile(boolean inInteractive) throws IOException {
        this(null, inInteractive);
    }

    public void execute(Connection conn) throws IOException, SqlToolError,
            SQLException {
        execute(conn, System.out, System.err);
    }

    private String curCommand = null;
    private int curLinenum = -1;

    /**
     * Run SQL in the file through the given database connection.
     *
     * This is synchronized so that I can use object variables to keep
     * track of current line number and command.
     */
    public synchronized void execute(Connection conn, PrintStream psStd,
            PrintStream psErr) throws IOException, SqlToolError, SQLException {
        StringBuffer buffer = new StringBuffer();
        curLinenum = -1;
        int index, nextsem;
        String inputLine;
        String multicommand;

        BufferedReader br = new BufferedReader(new InputStreamReader(
                (file == null) ? System.in : new FileInputStream(file)));
        // psErr.println("Executing '" + file + "'");
        curLinenum = 0;
        if (interactive) psStd.println(BANNER);
        while (true) {
            if (interactive) psStd.print(
                    (buffer.length() == 0) ? primaryPrompt : contPrompt);
            inputLine = br.readLine();
            if (inputLine == null) break;
            curLinenum++;
            // This is just to filter out useless newlines at beginning of 
            // commands.
            if (buffer.length() == 0 && inputLine.trim().length() == 0)
                continue;
            buffer.append("\n" + inputLine);
            if (inputLine.indexOf(';') < 0) continue;
            multicommand = buffer.toString();
            index = -1; // Previous sem
            while (true) {
                // This WILL succeed at least once.
                nextsem = multicommand.indexOf(';', index + 1);
                if (nextsem < 0) break;
                try {
                    curCommand =
                        multicommand.substring(index + 1, nextsem).trim();
                    if (curCommand.length() == 0) {
                        ; // Permit null command, but don't pass to DB.
                    } else if (curCommand.charAt(0) == '\\') {
                        processSpecial();
                    } else {
                        processStatement(conn, psStd);
                    }
                } catch (QuitNow qn) {
                    return;
                } catch (BadSpecial bs) {
                    psErr.println("Error at '"
                        + ((file == null) ? "stdin" : file.toString())
                        + "' line " + curLinenum
                        + ":\n\"" + curCommand + "\"\n" + bs.getMessage());
                    if (!interactive) throw new SqlToolError(bs);
                } catch (SQLException se) {
                    psErr.println("SQL Error at '"
                        + ((file == null) ? "stdin" : file.toString())
                        + "' line " + curLinenum
                        + ":\n\"" + curCommand + "\"\n" + se.getMessage());
                    if (!interactive) throw se;
                }
                index = nextsem;
            }
            while (++index < multicommand.length())
                if (!Character.isWhitespace(multicommand.charAt(index))) break;
            buffer.delete(0, index);
        }
        if (buffer.length() != 0)
            psErr.println("Unterminated input:  [" + buffer + ']');
    }
    private class BadSpecial extends Exception {
        private BadSpecial(String s) { super(s); }
    }

    private class QuitNow extends Exception { }

    /**
     * Process a Special Command.
     */
    private void processSpecial() throws BadSpecial, QuitNow {
        int index = 0;
        int special;
        String other = null;

        // Put an assertion here to verify that curCommand[0] == '\\'
        if (curCommand.length() < 2)
            throw new BadSpecial("Null special command");
        if (curCommand.length() > 2) {
            other = curCommand.substring(1).trim();
            if (other.length() < 1) other = null;
        }
        switch (curCommand.charAt(1)) {
            case 'q':
                throw new QuitNow();
            case '?':
                System.out.println(HELP_TEXT);
                break;
            case '!':
                System.err.println("Run '"
                        + ((other == null) ? "SHELL" : other) + "'");
                break;
            default:
                throw new BadSpecial("Unknown Special Command");
        }
    }

    private void processStatement(Connection conn,
            PrintStream printStream) throws SQLException {
        //System.out.println(Integer.toString(curLinenum) + ": " + curCommand);
        Statement statement = conn.createStatement();

        statement.execute(curCommand);
        ResultSet r = statement.getResultSet();
        int       updateCount = statement.getUpdateCount();

        switch (updateCount) {
            case -1:
                if (r == null) {
                    printStream.println("No result");
                    break;
                }
                ResultSetMetaData m      = r.getMetaData();
                int               col    = m.getColumnCount();
                StringBuffer      strbuf = new StringBuffer();
                for (int i = 1; i <= col; i++) {
                    strbuf.append(m.getColumnLabel(i) + "\t");
                }
                strbuf = strbuf.append("\n");
                while (r.next()) {
                    for (int i = 1; i <= col; i++) {
                        strbuf = strbuf.append(r.getString(i) + "\t");
                        if (r.wasNull()) {
                            strbuf = strbuf.append("(null)\t");
                        }
                    }
                }
                printStream.println(strbuf.toString());
                break;
            default:
                printStream.println("update count " + updateCount);
                break;
        }
    }
}
