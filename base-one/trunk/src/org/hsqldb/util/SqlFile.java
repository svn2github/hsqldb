/*
 * $Id: SqlFile.java,v 1.18 2004/01/21 23:16:12 unsaved Exp $
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
import java.util.ArrayList;

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
    private boolean htmlMode = false;

    final private static int SEP_LEN = 2;  // Ascii field separator blanks
    final private static String DIVIDER =
        "-----------------------------------------------------------------";
    final private static String SPACES =
        "                                                                 ";

    final private static String BANNER =
            "SqlFile processor.  Enter \"\\?\" to list Special Commands, "
        + "\"\\q\" to quit.\n\n"
      + "SPECIAL Commands begin with '\\' and execute when you hit ENTER.\n"
      + "An empty line within an SQL Statement clears it (but it can be reloaded later).\n"
      + "All other lines comprise SQL Statements.\n"
      + "SQL Statement lines ending with ';' cause the current Statement to be executed.\n"
  + "SQL Statements consisting of only /* SQL comment */ are not executed, therefore\n"
      + "you can comment scripts like \"/* This is a comment */; \"\n";
    final private static String HELP_TEXT =
"**********    SPECIAL COMMANDS MARKED !!! DO NOT WORK YET!!!  *******\n" +
          "SPECIAL Commands.\n"
        + "* commands only available for interactive use.\n"
        + "In place of \"3\" below, you can use nothing for the previous command, or\n"
        + "an integer \"X\" to indicate the Xth previous command.\n\n"
        + "    \\?                   Help\n"
        + " !!!\\! [command to run]  * Shell out\n"
        + "    \\p [line to print]   Print string to stdout\n"
        + "    \\dt                  List tables\n"
        + "    \\H                   Toggle HTML output mode\n"
        + "    \\* [true|false]      Continue upon errors (a.o.t. abort upon error)\n"
        + "    \\s                   * Show previous commands\n"
        + "    \\-[3]                * reload a previous command for appending\n"
        + "    \\-[3];               * reload and execute a previous command\n"
        + " !!!\\e[3]                * Edit a previous command in external editor\n"
        + "    \\q                   Quit (alternatively, end input like Ctrl-Z or Ctrl-D)\n\n"
        + "EXAMPLE:  To show previous commands then edit and execute the 3rd-to-last:\n"
        + "    \\s\n"
        + "    \\e 3\n"
        + "    ;\n";

    /**
     * @param inFile  inFile of null means to read stdin.
     * @param inInteractive  If true, prompts are printed, the interactive
     *                       Special commands are enabled, and 
     *                       continueOnError defaults to true.
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

    /*
     * This is reset upon each execute() invocation (to true if interactive,
     * false otherwise).
     */
    private boolean continueOnError = false;

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
        String trimmedInput;
        String deTerminated;

        continueOnError = interactive;
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
            trimmedInput = inputLine.trim();
            try {
                // This is the try for SQLException.  SQLExceptions are
                // normally thrown below in Statement processing, but 
                // could be called up above if a Special processing
                // executes a SQL command from history.
                if (curBuffer.length() == 0) {
                    // This is just to filter out useless newlines at 
                    // beginning of commands.
                    if (trimmedInput.length() == 0) continue;
                    if (trimmedInput.charAt(0) == '\\') {
                        try {
                            processSpecial(trimmedInput.substring(1));
                        } catch (QuitNow qn) {
                            return;
                        } catch (BadSpecial bs) {
                            psErr.println("Error at '"
                                + ((file == null) ? "stdin" : file.toString())
                                + "' line " + curLinenum
                                + ":\n\"" + inputLine + "\"\n" + bs.getMessage());
                            if (!continueOnError) throw new SqlToolError(bs);
                        }
                        continue;
                    }
                }
                if (trimmedInput.length() == 0) {
                    if (interactive) {
                        setHist(curBuffer.toString());
                        curBuffer.setLength(0);
                        psStd.println(
                                "Buffer stored into history then cleared");
                    }
                    continue;
                }
                deTerminated = deTerminated(inputLine);
                // A null terminal line (i.e., /\s*;\s*$/) is never useful.
                if (!trimmedInput.equals(";")) {
                    if (curBuffer.length() > 0) curBuffer.append('\n');
                    curBuffer.append((deTerminated == null)
                            ? inputLine
                            : deTerminated);
                }
                if (deTerminated == null) continue;
                curCommand = curBuffer.toString();
                trimmedCommand = curCommand.trim();
                if (trimmedCommand.length() == 0) {
                    throw new SQLException("Empty SQL Statement");
                }
                // If not completely SQL comment
                if ((!trimmedCommand.startsWith("/*"))
                        || (!trimmedCommand.endsWith("*/"))
                        || (trimmedCommand.indexOf("/*", 2) > -1)
                        || (trimmedCommand.lastIndexOf("*/", 
                                trimmedCommand.length()-4) > -1)) {
                    if (interactive) setHist(curCommand);
                    processStatement();
                }
            } catch (SQLException se) {
                psErr.println("SQL Error at '"
                    + ((file == null) ? "stdin" : file.toString())
                    + "' line " + curLinenum
                    + ":\n\"" + curCommand + "\"\n" + se.getMessage());
                if (!continueOnError) throw se;
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
            case 'H':
                htmlMode = !htmlMode;
                lonePrintln("htmlMode is set to: " + htmlMode);
                return;
            case 'd':
                if (other.equals("t")) {
                    listTables();
                    return;
                }
                break;
            case 'p':
                if (other == null) psStd.println();
                else psStd.println(other);
                return;
            case '*':
                if (other != null) {
                    // But remember that we have to abort on some I/O errors.
                    continueOnError = Boolean.valueOf(other).booleanValue();
                }
                psStd.println("Continue-on-error is set to: "
                        + continueOnError);
                return;
            case 's':
                showHistory();
                return;
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
                return;
            case '?':
                System.out.println(HELP_TEXT);
                return;
            case '!':
                System.err.println("Run '"
                        + ((other == null) ? "SHELL" : other) + "'");
                return;
        }
        throw new BadSpecial("Unknown Special Command");
    }

    private void lonePrintln(String s) {
        psStd.println(htmlMode
                ? ("<P>" + s + "</P>")
                : s
        );
    }

    private void listTables() throws SQLException {
        int[] listTableCols = { 3 };
        displayResultSet(null,
                curConn.getMetaData().getTables(null, null, null, null),
                listTableCols, "SYSTEM_");
    }

    private void processStatement() throws SQLException {
        Statement statement = curConn.createStatement();

        statement.execute(curCommand);
        displayResultSet(statement, statement.getResultSet(), null, null);
    }

    private void displayResultSet(Statement statement,
            ResultSet r, int[] incCols, String exclPref) throws SQLException {
        int updateCount = (statement == null) ? -1 : statement.getUpdateCount();
        switch (updateCount) {
            case -1:
                if (r == null) {
                    lonePrintln("No result");
                    break;
                }
                ResultSetMetaData m      = r.getMetaData();
                int cols    = m.getColumnCount();
                int incCount = (incCols == null) ? cols : incCols.length;
                String val;
                ArrayList rows = new ArrayList();
                String[] headerArray = null;
                String[] fieldArray;
                int[] maxWidth = new int[incCount];
                int insi;
                boolean skip;

                // STEP 1: GATHER DATA
                if (!htmlMode) {
                    for (int i = 0; i < maxWidth.length; i++) maxWidth[i] = 0;
                }
                if (incCount > 1) {
                    insi = -1;
                    headerArray = new String[incCount];
                    for (int i = 1; i <= cols; i++) {
                        if (incCols != null) {
                            skip = true;
                            for (int j = 0; j < incCols.length; j++)
                                if (i == incCols[j]) skip = false;
                            if (skip) continue;
                        }
                        headerArray[++insi] = m.getColumnLabel(i);
                        if (htmlMode) continue;
                        if (headerArray[insi].length() > maxWidth[insi])
                            maxWidth[insi] = headerArray[insi].length();
                    }
                }
                EACH_ROW:
                while (r.next()) {
                    fieldArray = new String[incCount];
                    insi = -1;
                    for (int i = 1; i <= cols; i++) {
                        if (incCols != null) {
                            skip = true;
                            for (int j = 0; j < incCols.length; j++)
                                if (i == incCols[j]) skip = false;
                            if (skip) continue;
                        }
                        val = r.getString(i);
                        if (exclPref != null && val != null
                                && val.startsWith(exclPref)) continue EACH_ROW;
                        fieldArray[++insi] = r.wasNull() ?
                                (htmlMode ? "<I>null</I>" : "null") : val;
                        if (htmlMode) continue;
                        if (fieldArray[insi].length() > maxWidth[insi])
                            maxWidth[insi] = fieldArray[insi].length();
                    }
                    rows.add(fieldArray);
                }

                // STEP 2: DISPLAY DATA
                if (htmlMode) psStd.println("<TABLE border='1'>");
                if (headerArray != null) {
                    if (htmlMode) psStd.print(htmlRow(COL_HEAD) + '\n'
                            + PRE_TD);
                    for (int i = 0; i < headerArray.length; i++) {
                        psStd.print(htmlMode
                                ? ("<TD>" + headerArray[i] + "</TD>")
                                : (((i > 0) ? spaces(2) : "")
                                    + pad(headerArray[i], maxWidth[i], false))
                        );
                    }
                    psStd.println(htmlMode ? ("\n" + PRE_TR + "</TR>") : "");
                    if (!htmlMode) {
                        for (int i = 0; i < headerArray.length; i++) {
                            psStd.print(((i > 0) ? spaces(2) : "")
                                    + divider(maxWidth[i]));
                        }
                        psStd.println();
                    }
                }
                for (int i = 0; i < rows.size(); i++) {
                    if (htmlMode) psStd.print(htmlRow(
                            ((i % 2) == 0) ? COL_EVEN : COL_ODD)
                            + '\n' + PRE_TD);
                    fieldArray = (String[]) rows.get(i);
                    for (int j = 0; j < fieldArray.length; j++) {
                        psStd.print(htmlMode
                            ? ("<TD>" + fieldArray[j] + "</TD>")
                            : (((j > 0) ? spaces(2) : "")
                                    + pad(fieldArray[j], maxWidth[j],
                                            false))
                        );
                    };
                    psStd.println(htmlMode ? ("\n" + PRE_TR + "</TR>") : "");
                }
                if (htmlMode) psStd.println("</TABLE>");
                lonePrintln("\n" + rows.size() + " rows");
                break;
            default:
                lonePrintln(((updateCount == 0) ? "no" 
                            : Integer.toString(updateCount))
                        + "row" + ((updateCount == 1) ? "" : "s") + "updated");
                break;
        }
    }

    final static private int
        COL_HEAD = 0,
        COL_ODD = 1,
        COL_EVEN = 2
    ;
    static private final String PRE_TR = spaces(4);
    static private final String PRE_TD = spaces(8);
    static private String htmlRow(int colType) {
        switch (colType) {
            case COL_HEAD:
                return PRE_TR + "<TR style='font-weight: bold;'>";
            case COL_ODD:
                return PRE_TR + "<TR style='background: #94d6ef; font: normal normal 10px/10px Arial, Helvitica, sans-serif;'>";
            case COL_EVEN:
                return PRE_TR + "<TR style='background: silver; font: normal normal 10px/10px Arial, Helvitica, sans-serif;'>";
        }
        return null;
    }

    static private String divider(int len) {
        return (len > DIVIDER.length()) ? DIVIDER : DIVIDER.substring(0, len);
    }

    static private String spaces(int len) {
        return (len > SPACES.length()) ? SPACES : SPACES.substring(0, len);
    }

    static private String pad(String inString, int fulllen,
            boolean rightJustify) {
        int len = fulllen - inString.length();
        if (len < 1) return inString;
        String pad = spaces(len);
        return ((rightJustify ? pad : "") + inString
                + (rightJustify ? "" : pad));
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
