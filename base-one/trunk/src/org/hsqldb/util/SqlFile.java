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
import java.util.StringTokenizer;

/* $Id: SqlFile.java,v 1.29 2004/02/16 15:05:09 unsaved Exp $ */

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
 * in history (as opposed to Special commands and comments).
 */
public class SqlFile {

    private File             file;
    private boolean          interactive;
    private String           primaryPrompt    = "sql> ";
    private String           contPrompt       = "  +> ";
    private Connection       curConn          = null;
    private String[]         statementHistory = new String[10];
    private boolean          htmlMode         = false;
    final private static int SEP_LEN          = 2;    // Ascii field separator blanks
    final private static String DIVIDER =
        "-----------------------------------------------------------------";
    final private static String SPACES =
        "                                                                 ";
    final private static String BANNER =
        "SqlFile processor.  Enter \"\\q\" to quit,\n"
        + "    \"\\?\" to list Special Commands, "
        + "\"/?\" to list Buffer/Editing commands\n\n"
        + "SPECIAL Commands begin with '\\' and execute when you hit ENTER.\n"
        + "BUFFER Commands begin with ':' and execute when you hit ENTER.\n"
        + "An empty line within an SQL Statement moves it into the buffer.\n"
        + "All other lines comprise SQL Statements.\n"
        + "SQL Statement lines ending with ';' cause the current Statement to be executed.\n"
        + "SQL Statements consisting of only /* SQL comment */ are not executed, therefore\n"
        + "    you can comment scripts like \"/* This is a comment */;\"\n";
    final private static String BUFFER_HELP_TEXT =
          "BUFFER Commands (only available for interactive use).\n"
        + "In place of \"3\" below, you can use nothing for the previous command, or\n"
        + "an integer \"X\" to indicate the Xth previous command.\n\n"
        + "    :?          Help\n"
        + "    :a          Enter append mode with contents of buffer as current command\n"
        + "    :l          List current contents of buffer\n"
        + "    :s/from/to/ Switch all occurrences of \"from\" to \"to\"\n"
        + "                ('$'s in \"from\" will match line separators)\n"
        + "                (use \":s/from//\" to delete 'from' strings)\n"
        + "    :;          Execute current buffer as an SQL Statement\n"
        ;
    final private static String HELP_TEXT =
        "**********    SPECIAL COMMANDS MARKED !!! DO NOT WORK YET!!!  *******\n"
        + "SPECIAL Commands.\n"
        + "* commands only available for interactive use.\n"
        + "In place of \"3\" below, you can use nothing for the previous command, or\n"
        + "an integer \"X\" to indicate the Xth previous command.\n\n"
        + "    \\?                   Help\n"
        + " !!!\\! [command to run]  * Shell out\n"
        + "    \\p [line to print]   Print string to stdout\n"
        + "    \\dt                  List tables\n"
        + "    \\d TABLENAME         Describe table\n"
        + "    \\H                   Toggle HTML output mode\n"
        + "    \\* [true|false]      Continue upon errors (a.o.t. abort upon error)\n"
        + "    \\s                   * Show previous commands\n"
        + "    \\-[3]                * reload a previous command for appending\n"
        + "    \\-[3];               * reload and execute a previous command\n"
        + " !!!\\e [3]               * Edit a previous command in external editor\n"
        + "    \\q                   Quit (alternatively, end input like Ctrl-Z or Ctrl-D)\n\n"
        + "EXAMPLE:  To show previous commands then edit and execute the 3rd-to-last:\n"
        + "    \\s\n" + "    \\e 3\n" + "    ;\n";

    /**
     * @param inFile  inFile of null means to read stdin.
     * @param inInteractive  If true, prompts are printed, the interactive
     *                       Special commands are enabled, and
     *                       continueOnError defaults to true.
     */
    SqlFile(File inFile, boolean inInteractive) throws IOException {

        file        = inFile;
        interactive = inInteractive;

        if (file != null &&!file.canRead()) {
            throw new IOException("Can't read SQL file '" + file + "'");
        }
    }

    SqlFile(boolean inInteractive) throws IOException {
        this(null, inInteractive);
    }

    public void execute(Connection conn)
    throws IOException, SqlToolError, SQLException {
        execute(conn, System.out, System.err);
    }

    private String      curCommand = null;
    private int         curLinenum = -1;
    private int         curHist    = -1;
    private PrintStream psStd      = null;
    private PrintStream psErr      = null;
    StringBuffer        stringBuffer  = new StringBuffer();
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
                                     PrintStream errIn)
                                     throws IOException, SqlToolError,
                                         SQLException {

        psStd      = stdIn;
        psErr      = errIn;
        curConn    = conn;
        curLinenum = -1;

        String inputLine;
        String trimmedCommand;
        String trimmedInput;
        String deTerminated;

        continueOnError = interactive;

        BufferedReader br = null;

        try {
            br = new BufferedReader(new InputStreamReader((file == null)
                    ? System.in
                    : new FileInputStream(file)));
            curLinenum = 0;

            if (interactive) {
                stdprint(BANNER);
            }

            while (true) {
                if (interactive) {
                    psStd.print((stringBuffer.length() == 0) ? primaryPrompt
                                                          : contPrompt);
                }

                inputLine = br.readLine();

                if (inputLine == null) {
                    break;
                }

                curLinenum++;

                trimmedInput = inputLine.trim();

                try {

                    // This is the try for SQLException.  SQLExceptions are
                    // normally thrown below in Statement processing, but
                    // could be called up above if a Special processing
                    // executes a SQL command from history.
                    if (stringBuffer.length() == 0) {

                        // This is just to filter out useless newlines at
                        // beginning of commands.
                        if (trimmedInput.length() == 0) {
                            continue;
                        }

                        if (trimmedInput.charAt(0) == '\\') {
                            try {
                                processSpecial(trimmedInput.substring(1));
                            } catch (QuitNow qn) {
                                return;
                            } catch (BadSpecial bs) {
                                errprint("Error at '"
                                         + ((file == null) ? "stdin"
                                                           : file.toString()) + "' line "
                                                           + curLinenum
                                                           + ":\n\""
                                                           + inputLine
                                                           + "\"\n"
                                                           + bs.getMessage());

                                if (!continueOnError) {
                                    throw new SqlToolError(bs);
                                }
                            }

                            continue;
                        }
                        if (interactive && trimmedInput.charAt(0) == ':') {
                            try {
                                processBuffer(trimmedInput.substring(1));
                            } catch (BadSpecial bs) {
                                errprint("Error at '"
                                         + ((file == null) ? "stdin"
                                                           : file.toString()) + "' line "
                                                           + curLinenum
                                                           + ":\n\""
                                                           + inputLine
                                                           + "\"\n"
                                                           + bs.getMessage());

                                if (!continueOnError) {
                                    throw new SqlToolError(bs);
                                }
                            }

                            continue;
                        }
                    }

                    if (trimmedInput.length() == 0) {
                        if (interactive) {
                            setBuf(stringBuffer.toString());
                            stringBuffer.setLength(0);
                            stdprint("Current input moved into buffer.");
                        }
                        continue;
                    }

                    deTerminated = deTerminated(inputLine);

                    // A null terminal line (i.e., /\s*;\s*$/) is never useful.
                    if (!trimmedInput.equals(";")) {
                        if (stringBuffer.length() > 0) {
                            stringBuffer.append('\n');
                        }

                        stringBuffer.append((deTerminated == null) ? inputLine
                                                                : deTerminated);
                    }

                    if (deTerminated == null) {
                        continue;
                    }
                    // If we reach here, then stringBuffer contains a complete
                    // SQL command.

                    curCommand     = stringBuffer.toString();
                    trimmedCommand = curCommand.trim();

                    if (trimmedCommand.length() == 0) {
                        throw new SQLException("Empty SQL Statement");
                    }

                    // If not completely SQL comment
                    if ((!trimmedCommand.startsWith(
                            "/*")) || (!trimmedCommand.endsWith(
                            "*/")) || (trimmedCommand.indexOf(
                                "/*", 2) > -1) || (trimmedCommand.lastIndexOf(
                                "*/", trimmedCommand.length() - 4) > -1)) {
                        if (interactive) {
                            setBuf(curCommand);
                        }

                        processStatement();
                    }
                } catch (SQLException se) {
                    errprint("SQL Error at '" + ((file == null) ? "stdin"
                                                                : file.toString()) + "' line "
                                                                + curLinenum
                                                                    + ":\n\""
                                                                        + curCommand
                                                                            + "\"\n"
                                                                                + se
                                                                                .getMessage());

                    if (!continueOnError) {
                        throw se;
                    }
                }

                stringBuffer.setLength(0);
            }

            if (stringBuffer.length() != 0) {
                errprint("Unterminated input:  [" + stringBuffer + ']');
            }
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }

    /**
     * Returns a copy of given string without a terminating semicolon.
     * If there is no terminating semicolon, null is returned.
     */
    static private String deTerminated(String inString) {

        int index = inString.lastIndexOf(';');

        if (index < 0) {
            return null;
        }

        for (int i = index + 1; i < inString.length(); i++) {
            if (!Character.isWhitespace(inString.charAt(i))) {
                return null;
            }
        }

        return inString.substring(0, index);
    }

    private class BadSpecial extends Exception {
        private BadSpecial(String s) {
            super(s);
        }
    }

    private class QuitNow extends Exception {}

    private class BadSwitch extends Exception {
        private BadSwitch(int i) {
            super(Integer.toString(i));
        }
    }

    /**
     * Process a Buffer Command.
     *
     * @throws SQLException Passed through from processStatement()
     */
    private void processBuffer(String inString)
    throws BadSpecial, SQLException {
        int    index = 0;
        int    special;
        char   commandChar = 'i';
        String other = null;

        if (inString.length() > 0) {
            commandChar = inString.charAt(0);
            other = inString.substring(1).trim();
            if (other.length() == 0) other = null;
        }

        switch (commandChar) {
            case ';':
                curCommand = commandFromHistory(0);
                stdprint("Executing command from buffer:\n"
                        + curCommand + '\n');
                processStatement();
                return;
            case 'a':
            case 'A':
                stringBuffer.append(commandFromHistory(0));
                return;
            case 'l':
            case 'L':
                stdprint("Current Buffer:\n" + commandFromHistory(0));
                return;
            case 's':
            case 'S':
                try {
                    StringBuffer sb = new StringBuffer(commandFromHistory(0));
                    if (other == null) throw new BadSwitch(0);
                    StringTokenizer toker =
                        new StringTokenizer(other, "/", true);
                    if (toker.countTokens() < 4
                            || !toker.nextToken().equals("/")) {
                        throw new BadSwitch(1);
                            }
                    String from = toker.nextToken().replace('$', '\n');
                    if (!toker.nextToken().equals("/")) {
                        throw new BadSwitch(2);
                    }
                    String to = toker.nextToken();
                    if (to.equals("/")) {
                        to = "";
                    } else {
                        if (toker.countTokens() < 1
                         || !toker.nextToken().equals("/")) {
                            throw new BadSwitch(3);
                         }
                    }
                    if (toker.countTokens() > 0) {
                        throw new BadSwitch(4);
                    }
                    int i = sb.length();
                    while ((i = sb.lastIndexOf(from, i-1)) > -1) {
                        sb.replace(i, i + from.length(), to);
                    }
                    statementHistory[curHist] = sb.toString();
                    stdprint("Current Buffer:\n" + commandFromHistory(0));
                } catch (BadSwitch badswitch) {
                    throw new BadSpecial(
                            "Switch syntax:  \":s/from this/to that/\".  "
                            + "Use '$' for line separations.  ["
                            + badswitch.getMessage() + ']');
                }
                return;
            case '?' :
                stdprint(BUFFER_HELP_TEXT);
                return;
        }
        throw new BadSpecial("Unknown Buffer Command");
    }

    /**
     * Process a Special Command.
     *
     * @throws SQLException Passed through from processStatement()
     */
    private void processSpecial(String inString)
    throws BadSpecial, QuitNow, SQLException {

        int    index = 0;
        int    special;
        String arg1,
               other = null;

        if (inString.length() < 1) {
            throw new BadSpecial("Null special command");
        }

        StringTokenizer toker = new StringTokenizer(inString);

        arg1 = toker.nextToken();

        if (toker.hasMoreTokens()) {
            other = toker.nextToken("").trim();
        }

        switch (arg1.charAt(0)) {

            case 'q' :
                throw new QuitNow();
            case 'H' :
                htmlMode = !htmlMode;

                stdprint(htmlMode ? "<HTML>"
                                  : "</HTML>");

                return;

            case 'd' :
                if (arg1.length() > 1 && arg1.charAt(1) == 't') {
                    listTables();

                    return;
                }

                if (arg1.length() == 1 && other != null) {
                    describe(other);

                    return;
                }
                break;

            case 'p' :
                if (other == null) {
                    stdprint();
                } else {
                    stdprint(other);
                }

                return;

            case '*' :
                if (other != null) {

                    // But remember that we have to abort on some I/O errors.
                    continueOnError = Boolean.valueOf(other).booleanValue();
                }

                stdprint("Continue-on-error is set to: " + continueOnError);

                return;

            case 's' :
                showHistory();

                return;

            case '-' :
                int     commandsAgo = 0;
                String  numStr;

                numStr = (arg1.length() == 1) ? null
                                              : arg1.substring(1,
                                              arg1.length());
                if (numStr == null) {
                    commandsAgo = 0;
                } else {
                    try {
                        commandsAgo = Integer.parseInt(numStr);
                    } catch (NumberFormatException nfe) {
                        throw new BadSpecial("Malformatted command number");
                    }
                }

                setBuf(commandFromHistory(commandsAgo));
                stdprint("RESTORED following command to buffer.  Enter \"/?\" "
                        + "to see buffer commands:\n" + commandFromHistory(0));
                return;

            case '?' :
                stdprint(HELP_TEXT);

                return;

            case '!' :
                System.err.println("Run '" + ((other == null) ? "SHELL"
                                                              : other) + "'");

                return;
        }

        throw new BadSpecial("Unknown Special Command");
    }

    private void stdprint() {

        if (htmlMode) {
            psStd.println("<BR>");
        } else {
            psStd.println();
        }
    }

    private void errprint(String s) {

        psErr.println(htmlMode
                      ? ("<DIV style='color:white; background: red; "
                         + "font-weight: bold'>" + s + "</DIV>")
                      : s);
    }

    private void stdprint(String s) {
        psStd.println(htmlMode ? ("<P>" + s + "</P>")
                               : s);
    }

    private void listTables() throws SQLException {

        int[] listTableCols = { 3 };
        java.sql.DatabaseMetaData md = curConn.getMetaData();
        String dbProductName = md.getDatabaseProductName();
        //System.err.println("DB NAME = (" + dbProductName + ')');

        // Database-specific table filtering.
        String excludePrefix = null;
        if (dbProductName.indexOf("HSQL") > -1) {
            excludePrefix = "SYSTEM_";
        } else if (dbProductName.indexOf("Oracle") > -1) {
        } else {
            // Don't know the DB, so use no filter.
        }

        displayResultSet(
            null, md.getTables(null, null, null, null),
            listTableCols, excludePrefix);
    }

    private void processStatement() throws SQLException {

        Statement statement = curConn.createStatement();

        statement.execute(curCommand);
        displayResultSet(statement, statement.getResultSet(), null, null);
    }

    private void displayResultSet(Statement statement, ResultSet r,
                                  int[] incCols,
                                  String exclPref) throws SQLException {

        int updateCount = (statement == null) ? -1
                                              : statement.getUpdateCount();

        switch (updateCount) {

            case -1 :
                if (r == null) {
                    stdprint("No result");

                    break;
                }

                ResultSetMetaData m        = r.getMetaData();
                int               cols     = m.getColumnCount();
                int               incCount = (incCols == null) ? cols
                                                               : incCols
                                                                   .length;
                String            val;
                ArrayList         rows        = new ArrayList();
                String[]          headerArray = null;
                String[]          fieldArray;
                int[]             maxWidth = new int[incCount];
                int               insi;
                boolean           skip;
                String            dataType;

                // STEP 1: GATHER DATA
                if (!htmlMode) {
                    for (int i = 0; i < maxWidth.length; i++) {
                        maxWidth[i] = 0;
                    }
                }

                boolean[] rightJust = new boolean[incCount];

                if (incCount > 1) {
                    insi        = -1;
                    headerArray = new String[incCount];

                    for (int i = 1; i <= cols; i++) {
                        if (incCols != null) {
                            skip = true;

                            for (int j = 0; j < incCols.length; j++) {
                                if (i == incCols[j]) {
                                    skip = false;
                                }
                            }

                            if (skip) {
                                continue;
                            }
                        }

                        headerArray[++insi] = m.getColumnLabel(i);
                        dataType            = m.getColumnTypeName(i);
                        rightJust[insi] = dataType.equals("INTEGER")
                                          || dataType.equals("NUMBER");

                        if (htmlMode) {
                            continue;
                        }

                        if (headerArray[insi].length() > maxWidth[insi]) {
                            maxWidth[insi] = headerArray[insi].length();
                        }
                    }
                }

                EACH_ROW:
                while (r.next()) {
                    fieldArray = new String[incCount];
                    insi       = -1;

                    for (int i = 1; i <= cols; i++) {
                        if (incCols != null) {
                            skip = true;

                            for (int j = 0; j < incCols.length; j++) {
                                if (i == incCols[j]) {
                                    skip = false;
                                }
                            }

                            if (skip) {
                                continue;
                            }
                        }

                        val = r.getString(i);

                        if (exclPref != null && val != null
                                && val.startsWith(exclPref)) {
                            continue EACH_ROW;
                        }

                        fieldArray[++insi] = r.wasNull()
                                             ? (htmlMode ? "<I>null</I>"
                                                         : "null")
                                             : val;

                        if (htmlMode) {
                            continue;
                        }

                        if (fieldArray[insi].length() > maxWidth[insi]) {
                            maxWidth[insi] = fieldArray[insi].length();
                        }
                    }

                    rows.add(fieldArray);
                }

                // STEP 2: DISPLAY DATA
                if (htmlMode) {
                    psStd.println("<TABLE border='1'>");
                }

                if (headerArray != null) {
                    if (htmlMode) {
                        psStd.print(htmlRow(COL_HEAD) + '\n' + PRE_TD);
                    }

                    for (int i = 0; i < headerArray.length; i++) {
                        psStd.print(htmlMode
                                    ? ("<TD>" + headerArray[i] + "</TD>")
                                    : (((i > 0) ? spaces(2)
                                                : "") + pad(headerArray[i],
                                                            maxWidth[i],
                                                            rightJust[i])));
                    }

                    psStd.println(htmlMode ? ("\n" + PRE_TR + "</TR>")
                                           : "");

                    if (!htmlMode) {
                        for (int i = 0; i < headerArray.length; i++) {
                            psStd.print(((i > 0) ? spaces(2)
                                                 : "") + divider(
                                                     maxWidth[i]));
                        }

                        psStd.println();
                    }
                }

                for (int i = 0; i < rows.size(); i++) {
                    if (htmlMode) {
                        psStd.print(htmlRow(((i % 2) == 0) ? COL_EVEN
                                                           : COL_ODD) + '\n'
                                                           + PRE_TD);
                    }

                    fieldArray = (String[]) rows.get(i);

                    for (int j = 0; j < fieldArray.length; j++) {
                        psStd.print(htmlMode
                                    ? ("<TD>" + fieldArray[j] + "</TD>")
                                    : (((j > 0) ? spaces(2)
                                                : "") + pad(fieldArray[j],
                                                            maxWidth[j],
                                                            rightJust[j])));
                    }
                    ;

                    psStd.println(htmlMode ? ("\n" + PRE_TR + "</TR>")
                                           : "");
                }

                if (htmlMode) {
                    psStd.println("</TABLE>");
                }

                if (rows.size() != 1) {
                    stdprint("\n" + rows.size() + " rows");
                }
                break;

            default :
                if (updateCount != 0) {
                    stdprint(Integer.toString(updateCount) + " row"
                             + ((updateCount == 1) ? ""
                                                   : "s") + " updated");
                }
                break;
        }
    }

    final static private int    COL_HEAD = 0,
                                COL_ODD  = 1,
                                COL_EVEN = 2
    ;
    static private final String PRE_TR   = spaces(4);
    static private final String PRE_TD   = spaces(8);

    static private String htmlRow(int colType) {

        switch (colType) {

            case COL_HEAD :
                return PRE_TR + "<TR style='font-weight: bold;'>";

            case COL_ODD :
                return PRE_TR
                       + "<TR style='background: #94d6ef; font: normal normal 10px/10px Arial, Helvitica, sans-serif;'>";

            case COL_EVEN :
                return PRE_TR
                       + "<TR style='background: silver; font: normal normal 10px/10px Arial, Helvitica, sans-serif;'>";
        }

        return null;
    }

    static private String divider(int len) {
        return (len > DIVIDER.length()) ? DIVIDER
                                        : DIVIDER.substring(0, len);
    }

    static private String spaces(int len) {
        return (len > SPACES.length()) ? SPACES
                                       : SPACES.substring(0, len);
    }

    static private String pad(String inString, int fulllen,
                              boolean rightJustify) {

        int len = fulllen - inString.length();

        if (len < 1) {
            return inString;
        }

        String pad = spaces(len);

        return ((rightJustify ? pad
                              : "") + inString + (rightJustify ? ""
                                                               : pad));
    }

    private void showHistory() {

        int      ctr = -1;
        String   s;
        String[] reversedList = new String[statementHistory.length];

        try {
            for (int i = curHist; i >= 0; i--) {
                s = statementHistory[i];

                if (s == null) {
                    return;
                }

                reversedList[++ctr] = s;
            }

            for (int i = 9; i > curHist; i--) {
                s = statementHistory[i];

                if (s == null) {
                    return;
                }

                reversedList[++ctr] = s;
            }
        } finally {
            if (ctr < 0) {
                stdprint("<<<    No history yet    >>>");

                return;
            }

            for (int i = ctr; i >= 0; i--) {
                psStd.println(
                    ((i == 0) ? "BUFR" : ("-" + i + "  "))
                    + " **********************************************\n"
                    + reversedList[i]);
            }

            psStd.println("\n<<<  Copy a command to buffer like \"\\-3\"       "
                          + "Re-execute buffer like \"/;\"  >>>");
        }
    }

    private String commandFromHistory(int commandsAgo) throws BadSpecial {

        if (commandsAgo >= statementHistory.length) {
            throw new BadSpecial("History can only hold up to "
                                 + statementHistory.length + " commands");
        }

        String s =
            statementHistory[(statementHistory.length + curHist - commandsAgo) % statementHistory.length];

        if (s == null) {
            throw new BadSpecial("History doesn't go back that far");
        }

        return s;
    }

    private void setBuf(String inString) {
        curHist++;

        if (curHist == statementHistory.length) {
            curHist = 0;
        }
        statementHistory[curHist] = inString;
    }

    private void describe(String tableName) throws SQLException {

        Statement statement = curConn.createStatement();

        statement.execute("SELECT * FROM " + tableName + " WHERE 1 = 2");

        ResultSet         r    = statement.getResultSet();
        ResultSetMetaData m    = r.getMetaData();
        int               cols = m.getColumnCount();
        String            val;
        ArrayList         rows        = new ArrayList();
        String[]          headerArray = {
            "name", "datatype", "width", "no-nulls"
        };
        String[]          fieldArray;
        int[]             maxWidth  = {
            0, 0, 0, 0
        };
        boolean[]         rightJust = {
            false, false, true, false
        };

        // STEP 1: GATHER DATA
        for (int i = 0; i < headerArray.length; i++) {
            if (htmlMode) {
                continue;
            }

            if (headerArray[i].length() > maxWidth[i]) {
                maxWidth[i] = headerArray[i].length();
            }
        }

        for (int i = 0; i < cols; i++) {
            fieldArray    = new String[4];
            fieldArray[0] = m.getColumnName(i + 1);
            fieldArray[1] = m.getColumnTypeName(i + 1);
            fieldArray[2] = Integer.toString(m.getColumnDisplaySize(i + 1));
            fieldArray[3] =
                ((m.isNullable(i + 1) == java.sql.ResultSetMetaData.columnNullable)
                 ? (htmlMode ? "&nbsp;"
                             : "")
                 : "*");

            rows.add(fieldArray);

            for (int j = 0; j < fieldArray.length; j++) {
                if (fieldArray[j].length() > maxWidth[j]) {
                    maxWidth[j] = fieldArray[j].length();
                }
            }
        }

        // STEP 2: DISPLAY DATA
        if (htmlMode) {
            psStd.println("<TABLE border='1'>");
        }

        if (htmlMode) {
            psStd.print(htmlRow(COL_HEAD) + '\n' + PRE_TD);
        }

        for (int i = 0; i < headerArray.length; i++) {
            psStd.print(htmlMode ? ("<TD>" + headerArray[i] + "</TD>")
                                 : (((i > 0) ? spaces(2)
                                             : "") + pad(headerArray[i],
                                             maxWidth[i], rightJust[i])));
        }

        psStd.println(htmlMode ? ("\n" + PRE_TR + "</TR>")
                               : "");

        if (!htmlMode) {
            for (int i = 0; i < headerArray.length; i++) {
                psStd.print(((i > 0) ? spaces(2)
                                     : "") + divider(maxWidth[i]));
            }

            psStd.println();
        }

        for (int i = 0; i < rows.size(); i++) {
            if (htmlMode) {
                psStd.print(htmlRow(((i % 2) == 0) ? COL_EVEN
                                                   : COL_ODD) + '\n'
                                                   + PRE_TD);
            }

            fieldArray = (String[]) rows.get(i);

            for (int j = 0; j < fieldArray.length; j++) {
                psStd.print(htmlMode ? ("<TD>" + fieldArray[j] + "</TD>")
                                     : (((j > 0) ? spaces(2)
                                                 : "") + pad(fieldArray[j],
                                                 maxWidth[j], rightJust[j])));
            }
            ;

            psStd.println(htmlMode ? ("\n" + PRE_TR + "</TR>")
                                   : "");
        }

        if (htmlMode) {
            stdprint("\n</TABLE>");
        }
    }
}
