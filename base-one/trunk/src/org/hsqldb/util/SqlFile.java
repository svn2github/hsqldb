/*
 * $Id: SqlFile.java,v 1.11 2004/01/20 19:59:28 unsaved Exp $
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
 * Statement = SQL statement like "SQL Statement;"
 * Special =   Special Command like "\x arg..."
 *
 * In general, the special commands mirror those of Postgresql's psql,
 * but SqlFile handles command editing much different from Postgresql
 * because of Java's lack of support for raw tty I/O.
 * The \p special command, in particular, is very different from psql's.
 * Also, to keep the code simpler, we're sticking to only single-char
 * special commands until we really need more.
 *
 * For now, using Sql*plus's method of only keeping SQL statements
 * in history.
 */
public class SqlFile {
    private File file;
    private boolean interactive;
    private String primaryPrompt = "sql> ";
    private String contPrompt    = "  +> ";
    private Connection curConn = null;
    private String[] statementHistory = new String[10];

    final private static String DIVIDER =
        "-----------------------------------------------------------------";

    final private static String BANNER =
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
        + "    \\-2;                 * reload and run 2nd-to-last command, etc.\n"
        + "    \\q                   Quit (alternatively, end input,\n"
        + "                           like Ctrl-Z or Ctrl-D)\n\n"
        + "EXAMPLE:  To show previous commands then edit then execute the 3rd-to-last:\n"
        + "    \\s\n"
        + "    \\-3\n"
        + "    \\e\n"
        + "    ;";

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
    private int curHist = -1;
    private PrintStream psStd = null;
    private PrintStream psErr = null;
    StringBuffer curBuffer = new StringBuffer();

    /**
     * Run SQL in the file through the given database connection.
     *
     * This is synchronized so that I can use object variables to keep
     * track of current line number, command, connection, i/o streams, etc.
     */
    public synchronized void execute(Connection conn, PrintStream stdIn,
            PrintStream errIn) throws IOException, SqlToolError, SQLException {
        psStd = stdIn;
        psErr = errIn;
        curConn = conn;
        curLinenum = -1;
        String inputLine;
        String trimmedCommand;
        String deTerminated;

        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(
                (file == null) ? System.in : new FileInputStream(file)));
        // psErr.println("Executing '" + file + "'");
        curLinenum = 0;
        if (interactive) psStd.println(BANNER);
        while (true) {
            if (interactive) psStd.print(
                    (curBuffer.length() == 0) ? primaryPrompt : contPrompt);
            inputLine = br.readLine();
            if (inputLine == null) break;
            curLinenum++;
            trimmedCommand = inputLine.trim();
            try {
                // This is the try for SQLException.  SQLExceptions are
                // normally thrown below in Statement processing, but 
                // could be called up above if a Special processing
                // executes a SQL command from history.
                if (curBuffer.length() == 0) {
                    // This is just to filter out useless newlines at 
                    // beginning of commands.
                    if (trimmedCommand.length() == 0) continue;
                    if (trimmedCommand.charAt(0) == '\\') {
                        try {
                            processSpecial(trimmedCommand.substring(1));
                        } catch (QuitNow qn) {
                            return;
                        } catch (BadSpecial bs) {
                            psErr.println("Error at '"
                                + ((file == null) ? "stdin" : file.toString())
                                + "' line " + curLinenum
                                + ":\n\"" + inputLine + "\"\n" + bs.getMessage());
                            if (!interactive) throw new SqlToolError(bs);
                        }
                        continue;
                    }
                }
                if (trimmedCommand.length() == 0) {
                    if (interactive) {
                        setHist(curBuffer.toString());
                        curBuffer.setLength(0);
                        psStd.println("Buffer stored into history then cleared");
                    }
                    continue;
                }
                deTerminated = deTerminated(inputLine);
                // A null terminal line (i.e., /\s*;\s*$/) is never useful.
                if (!trimmedCommand.equals(";")) {
                    if (curBuffer.length() > 0) curBuffer.append('\n');
                    curBuffer.append((deTerminated == null)
                            ? inputLine
                            : deTerminated);
                }
                if (deTerminated == null) continue;
                curCommand = curBuffer.toString();
                if (curCommand.trim().length() == 0) {
                    throw new SQLException("Empty SQL Statement");
                }
                if (interactive) setHist(curCommand);
                processStatement();
            } catch (SQLException se) {
                psErr.println("SQL Error at '"
                    + ((file == null) ? "stdin" : file.toString())
                    + "' line " + curLinenum
                    + ":\n\"" + curCommand + "\"\n" + se.getMessage());
                if (!interactive) throw se;
            }
            curBuffer.setLength(0);
        }
        if (curBuffer.length() != 0)
            psErr.println("Unterminated input:  [" + curBuffer + ']');
        } finally {
            if (br != null) br.close();
        }
    }

    /**
     * Returns a copy of given string without a terminating semicolon.
     * If there is no terminating semicolon, null is returned.
     */
    static private String deTerminated(String inString) {
        int index = inString.lastIndexOf(';');
        if (index < 0) return null;
        for (int i = index + 1; i < inString.length(); i++) {
            if (!Character.isWhitespace(inString.charAt(i))) return null;
        }
        return inString.substring(0, index);
    }

    private class BadSpecial extends Exception {
        private BadSpecial(String s) { super(s); }
    }

    private class QuitNow extends Exception { }

    /**
     * Process a Special Command.
     *
     * @throws SQLException Passed through from processStatement()
     */
    private void processSpecial(String inString)
        throws BadSpecial, QuitNow, SQLException {
        int index = 0;
        int special;
        String other = null;

        // Put an assertion here to verify that inString[0] == '\\'
        if (inString.length() < 1)
            throw new BadSpecial("Null special command");
        if (inString.length() > 1) {
            other = inString.substring(1).trim();
            if (other.length() < 1) other = null;
        }
        switch (inString.charAt(0)) {
            case 'q':
                throw new QuitNow();
            case 's':
                showHistory();
                break;
            case '-':
                boolean execute = false;
                int commandsAgo = 0;
                if (other != null && other.charAt(other.length() - 1) == ';') {
                    execute = true;
                    other = (other.length() == 1)
                            ? null
                            : other.substring(0, other.length() - 1);
                }
                if (other == null) {
                    commandsAgo = 0;
                } else try {
                    commandsAgo = Integer.parseInt(other) - 1;
                } catch (NumberFormatException nfe) {
                    throw new BadSpecial("Malformatted command number");
                }
                String replacement = commandFromHistory(commandsAgo);
                // Don't add to history if executing without modification
                if (!execute) {
                    curBuffer.setLength(0);
                    curBuffer.append(replacement);
                }
                psStd.println("RESTORED.  "
                        + (execute ? "Executing the following command:"
                                   : ("You are now appending.  "
                                        + "Just enter ; to re-execute."))
                        + '\n' + replacement);
                if (execute) {
                    curCommand = replacement;
                    processStatement();
                }
                break;
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

    /**
     * Most of this code taken directly from ScriptTool.java
     *
     * TODO:  Completely rework.  All the data is in RAM anyways (in
     * a StringBuffer).  We might as well store in a more primitive
     * form and calculate maximul lengths, then print out in a good
     * table where things will always line up, instead of the hit and
     * miss method below of using tabs.
     */
    private void processStatement() throws SQLException {
        //System.out.println(Integer.toString(curLinenum) + ": " + curCommand);
        Statement statement = curConn.createStatement();

        statement.execute(curCommand);
        ResultSet r = statement.getResultSet();
        int       updateCount = statement.getUpdateCount();

        switch (updateCount) {
            case -1:
                if (r == null) {
                    psStd.println("No result");
                    break;
                }
                ResultSetMetaData m      = r.getMetaData();
                int               col    = m.getColumnCount();
                StringBuffer      strbuf = new StringBuffer();
                String val;
                StringBuffer dividerBuffer = new StringBuffer();
                if (col > 1) {
                    for (int i = 1; i <= col; i++) {
                        val = m.getColumnLabel(i);
                        strbuf.append(val + '\t');
                        dividerBuffer.append(divider(val.length()) + '\t');
                    }
                    strbuf = strbuf.append("\n"
                            + dividerBuffer.toString() + '\n');
                }
                while (r.next()) {
                    for (int i = 1; i <= col; i++) {
                        strbuf = strbuf.append(r.getString(i) + '\t');
                        if (r.wasNull()) {
                            strbuf = strbuf.append("(null)\t");
                        }
                    }
                    strbuf.append('\n');
                }
                psStd.print(strbuf.toString());
                break;
            default:
                psStd.println("Updated row(s):  " + updateCount);
                break;
        }
    }

    private String divider(int len) {
        return (len > DIVIDER.length()) ? DIVIDER : DIVIDER.substring(0, len);
    }

    private void showHistory() {
        int ctr = -1;
        String s;
        String[] reversedList = new String[statementHistory.length];
        try {
            for (int i = curHist; i >= 0; i--) {
                s = statementHistory[i];
                if (s == null) return;
                reversedList[++ctr] = s;
            }
            for (int i = 9; i > curHist; i--) {
                s = statementHistory[i];
                if (s == null) return;
                reversedList[++ctr] = s;
            }
        } finally {
            if (ctr < 0) {
                psStd.println("<<<    No history yet    >>>");
                return;
            }
            for (int i = ctr; i >= 0; i--) {
                psStd.println("-" + (i + 1)
                        + "  **********************************************\n"
                        + reversedList[i]);
            }
            psStd.println("<<<    Restore for append like \"\\-3\"       "
                    + "Re-execute like \"\\-4;\"    >>>");
        }
    }

    private String commandFromHistory(int commandsAgo) throws BadSpecial {
        if (commandsAgo >= statementHistory.length)
            throw new BadSpecial(
                    "History can only hold up to " + statementHistory.length
                    + " commands");
        String s = statementHistory[
                (statementHistory.length + curHist - commandsAgo)
                % statementHistory.length];
        if (s == null)
            throw new BadSpecial("History doesn't go back that far");
        return s;
    }

    private void setHist(String inString) {
        curHist++;
        if (curHist == statementHistory.length) curHist = 0;
        statementHistory[curHist] = inString;
    }
}
