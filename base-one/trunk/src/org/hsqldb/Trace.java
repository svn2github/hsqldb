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

import java.sql.DriverManager;
import java.io.PrintWriter;
import org.hsqldb.lib.FileUtil;

/**
 * handles creation and reporting of error messages and throwing HsqlException
 *
 * @version 1.7.0
 */

// fredt@users 20020130 - patch 476694 by velichko@users - savepoints
// additions in different parts to support savepoint transactions
// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP) - error reporting
// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP) - setting trace
// the system property hsqldb.trace == true is now used for setting tracing on
// the system property hsqldb.tracesystemout == true is now used for printing
// trace message to System.out
// fredt@users 20020305 - patch 1.7.0 - various new messages added
// tony_lai@users 20020820 - patch 595073 - Duplicated exception msg
// fredt@users 20021230 - patch 488118 by xclay@users - allow multithreading
//
// fredt - todo - 20021022 management of nested throws inside the program in
// such a way that it is possible to return exactly the text of the error
// thrown at a given level withou higher level messages being added and to
// preserve the orignial erro code
public class Trace extends PrintWriter {

    public static boolean       TRACE          = false;
    public static boolean       TRACESYSTEMOUT = false;
    public static final boolean STOP           = false;
    public static final boolean DOASSERT       = true;
    private static final Trace  tTracer        = new Trace();
    private static String       sTrace;
    private static int          iStop = 0;

    //
    public static final int                                 //
         DATABASE_ALREADY_IN_USE             = 1,
     CONNECTION_IS_CLOSED                = 2,
     CONNECTION_IS_BROKEN                = 3,
     DATABASE_IS_SHUTDOWN                = 4,
     COLUMN_COUNT_DOES_NOT_MATCH         = 5,
     DIVISION_BY_ZERO                    = 6,
     INVALID_ESCAPE                      = 7,
     INTEGRITY_CONSTRAINT_VIOLATION      = 8,
     VIOLATION_OF_UNIQUE_INDEX           = 9,
     TRY_TO_INSERT_NULL                  = 10,
     UNEXPECTED_TOKEN                    = 11,
     UNEXPECTED_END_OF_COMMAND           = 12,
     UNKNOWN_FUNCTION                    = 13,
     NEED_AGGREGATE                      = 14,
     SUM_OF_NON_NUMERIC                  = 15,
     WRONG_DATA_TYPE                     = 16,
     SINGLE_VALUE_EXPECTED               = 17,
     SERIALIZATION_FAILURE               = 18,
     TRANSFER_CORRUPTED                  = 19,
     FUNCTION_NOT_SUPPORTED              = 20,
     TABLE_ALREADY_EXISTS                = 21,
     TABLE_NOT_FOUND                     = 22,
     INDEX_ALREADY_EXISTS                = 23,
     SECOND_PRIMARY_KEY                  = 24,
     DROP_PRIMARY_KEY                    = 25,
     INDEX_NOT_FOUND                     = 26,
     COLUMN_ALREADY_EXISTS               = 27,
     COLUMN_NOT_FOUND                    = 28,
     FILE_IO_ERROR                       = 29,
     WRONG_DATABASE_FILE_VERSION         = 30,
     DATABASE_IS_READONLY                = 31,
     DATA_IS_READONLY                    = 32,
     ACCESS_IS_DENIED                    = 33,
     INPUTSTREAM_ERROR                   = 34,
     NO_DATA_IS_AVAILABLE                = 35,
     USER_ALREADY_EXISTS                 = 36,
     USER_NOT_FOUND                      = 37,
     ASSERT_FAILED                       = 38,
     EXTERNAL_STOP                       = 39,
     GENERAL_ERROR                       = 40,
     WRONG_OUT_PARAMETER                 = 41,
     ERROR_IN_FUNCTION                   = 42,
     TRIGGER_NOT_FOUND                   = 43,
     SAVEPOINT_NOT_FOUND                 = 44,
     LABEL_REQUIRED                      = 45,
     WRONG_DEFAULT_CLAUSE                = 46,
     FOREIGN_KEY_NOT_ALLOWED             = 47,
     UNKNOWN_DATA_SOURCE                 = 48,
     BAD_INDEX_CONSTRAINT_NAME           = 49,
     DROP_FK_INDEX                       = 50,
     RESULTSET_FORWARD_ONLY              = 51,
     VIEW_ALREADY_EXISTS                 = 52,
     VIEW_NOT_FOUND                      = 53,
     NOT_A_VIEW                          = 54,
     NOT_A_TABLE                         = 55,
     SYSTEM_INDEX                        = 56,
     COLUMN_TYPE_MISMATCH                = 57,
     BAD_ADD_COLUMN_DEFINITION           = 58,
     DROP_SYSTEM_CONSTRAINT              = 59,
     CONSTRAINT_ALREADY_EXISTS           = 60,
     CONSTRAINT_NOT_FOUND                = 61,
     INVALID_JDBC_ARGUMENT               = 62,
     DATABASE_IS_MEMORY_ONLY             = 63,
     OUTER_JOIN_CONDITION                = 64,
     NUMERIC_VALUE_OUT_OF_RANGE          = 65,
     MISSING_SOFTWARE_MODULE             = 66,
     NOT_IN_AGGREGATE_OR_GROUP_BY        = 67,
     INVALID_GROUP_BY                    = 68,
     INVALID_HAVING                      = 69,
     INVALID_ORDER_BY                    = 70,
     INVALID_ORDER_BY_IN_DISTINCT_SELECT = 71,
     OUT_OF_MEMORY                       = 72,
     OPERATION_NOT_SUPPORTED             = 73,
     INVALID_IDENTIFIER                  = 74,
     TEXT_TABLE_SOURCE                   = 75,
     TEXT_FILE                           = 76,
     BAD_IDENTITY_VALUE                  = 77,
     ERROR_IN_SCRIPT_FILE                = 78,
     NULL_IN_VALUE_LIST                  = 79,

    // socket errors
    SOCKET_ERROR                = 80,
     INVALID_CHARACTER_ENCODING = 81,
     NO_CLASSLOADER_FOR_TLS     = 82,
     NO_JSSE                    = 83,
     NO_SSLSOCKETFACTORY_METHOD = 84,
     UNEXPECTED_EXCEPTION       = 85,
     TLS_ERROR                  = 86,
     MISSING_TLS_METHOD         = 87,
     TLS_SECURITY_ERROR         = 88,
     NO_TLS_DATA                = 89,
     NO_PRINCIPAL               = 90,
     INCOMPLETE_CERTIFICATE     = 91,
     TLS_HOSTNAME_MISMATCH      = 92,
     KEYSTORE_PROBLEM           = 93,

    // more errors
    DATABASE_NOT_EXISTS = 94,
     INVALID_CONVERSION = 95,

    //--- wsonic
    BinaryDatabaseScriptReader_readExistingData = 96,
     BinaryDatabaseScriptReader_readTableInit   = 97;
    ;

    //
    private static final String[] sDescription = {
        "NOT USED",                                         //
        "08001 The database is already in use by another process",
        "08003 Connection is closed", "08003 Connection is broken",
        "08003 The database is shutdown",                   //
        "21000 Column count does not match",                //
        "22012 Division by zero",                           //
        "22019 Invalid escape character",
        "23000 Integrity constraint violation",
        "23000 Violation of unique index",
        "23000 Try to insert null into a non-nullable column",
        "37000 Unexpected token",                           //
        "37000 Unexpected end of command",                  //
        "37000 Unknown function",                           //
        "37000 Need aggregate function or group by",
        "37000 Sum on non-numeric data not allowed",        //
        "37000 Wrong data type",                            //
        "37000 Single value expected",                      //
        "40001 Serialization failure",                      //
        "40001 Transfer corrupted",                         //
        "IM001 This function is not supported",             //
        "S0001 Table already exists",                       //
        "S0002 Table not found",                            //
        "S0011 Index already exists",
        "S0011 Attempt to define a second primary key",
        "S0011 Attempt to drop the primary key",            //
        "S0012 Index not found", "S0021 Column already exists",
        "S0022 Column not found",                           //
        "S1000 File input/output error",                    //
        "S1000 Wrong database file version",
        "S1000 The database is in read only mode",
        "S1000 The table data is read only",                //
        "S1000 Access is denied",                           //
        "S1000 InputStream error",                          //
        "S1000 No data is available",                       //
        "S1000 User already exists",                        //
        "S1000 User not found",                             //
        "S1000 Assert failed",                              //
        "S1000 External stop request",                      //
        "S1000 General error",                              //
        "S1009 Wrong OUT parameter",                        //
        "S1010 Error in function",                          //
        "S0002 Trigger not found",                          //
        "S1011 Savepoint not found",                        //
        "37000 Label required for value list",
        "37000 Wrong data type or data too long in DEFAULT clause",
        "S0011 Foreign key not allowed",
        "S1000 The table's data source for this connection is not known",
        "S0000 User-defined index or constraint name cannot begin with SYS_",
        "S0011 Attempt to drop a foreign key index",
        "S1000 ResultSet was set to forward only",
        "S0003 View already exists",                        //
        "S0004 View not found", "S0005 Not a View", "S0005 Not a Table",
        "S0011 Attempt to drop or rename a system index",
        "S0021 Column types do not match",
        "s0021 Column constraints are not acceptable",
        "S0011 Attempt to drop a system constraint",
        "S0011 Constraint already exists", "S0011 Constraint not found",
        "SOO10 Invalid argument in JDBC call",
        "S1000 Database is memory only",
        "37000 only one join condition on table columns allowed",
        "22003 Numeric value out of range",
        "37000 Software module not installed",
        "37000 Not contained in aggregate function or group by clause",
        "37000 Cannot be in GROUP BY clause",
        "37000 Cannot be in HAVING clause",
        "37000 Cannot be in ORDER BY clause",
        "37000 ORDER BY item does not appear in the SELECT DISTINCT list",
        "S1000 Out of Memory",                              //
        "S1000 This operation is not supported", "22019 Invalid identifier",
        "22019 Invalid TEXT table source string",
        "S1000 bad TEXT table source file",
        "23000 negative value not allowed for identity column",
        "S1000 error in script file",                       //
        "37000 NULL in value list",                         //
        "08000 socket creation error",                      //
        "37000 invalid character encoding",
        "08000 reserved for NO_CLASSLOADER_FOR_TLS",
        "08000 reserved for NO_JSSE",
        "08000 reserved for NO_SSLSOCKETFACTORY_METHOD",
        "08000 reserved for UNEXPECTED_EXCEPTION",
        "08000 reserved for TLS_ERROR",
        "08000 reserved for MISSING_TLS_METHOD",
        "08000 reserved for TLS_SECURITY_ERROR",
        "08000 reserved for NO_TLS_DATA",                   //
        "08000 reserved for NO_PRINCIPAL",
        "08000 reserved for INCOMPLETE_CERTIFICATE",
        "08000 reserved for TLS_HOSTNAME_MISMATCH",
        "08000 reserved for KEYSTORE_PROBLEM",

        //
        "08003 Database does not exists",                   //
        "22003 Type Conversion not supported",

        //--- wsonic
        "table $$ row count error : $$ read, needed $$",    // BinaryDatabaseScriptReader_readExistingData
        "wrong data for insert operation",                  // BinaryDatabaseScriptReader_readTableInit
    };

    static {
        try {
            TRACE          = Boolean.getBoolean("hsqldb.trace");
            TRACESYSTEMOUT = Boolean.getBoolean("hsqldb.tracesystemout");
        } catch (Exception e) {}
    }

    /**
     * Compose error message by inserting the strings in the add parameters
     * in placeholders within the error message. The message string contains
     * $$ markers for each context variable. Context variables are supplied in
     * the add parameters.
     *
     * @param code      main error code
     * @param subCode   sub error code (if 0 => no subMessage!)
     * @param   add     optional parameters
     *
     * @return an <code>HsqlException</code>
     */
    static HsqlException error(int code, int subCode, final Object[] add) {

        // in case of negative code
        code = Math.abs(code);

        String       mainErrorMessage = getMessage(code);
        final String state            = mainErrorMessage.substring(0, 5);

        mainErrorMessage = mainErrorMessage.substring(6);

        if (subCode != 0) {
            mainErrorMessage += getMessage(Math.abs(subCode));
        }

        StringBuffer sb = new StringBuffer(mainErrorMessage.length() + 32);
        int          lastIndex = 0;
        int          escIndex  = mainErrorMessage.length();

        if (add != null) {

            // removed test: i < add.length
            // because if mainErrorMessage is equal to "blabla $$"
            // then the statement escIndex = mainErrorMessage.length();
            // is never reached!  ???
            for (int i = 0; i < add.length ; i++) {
                escIndex = mainErrorMessage.indexOf("$$", lastIndex);

                if (escIndex == -1) {
                    escIndex = mainErrorMessage.length();

                    break;
                }

                sb.append(mainErrorMessage.substring(lastIndex, escIndex));
                sb.append(add[i].toString());

                lastIndex = escIndex + "$$".length();
            }
        }

        sb.append(mainErrorMessage.substring(lastIndex, escIndex));

        return new HsqlException(sb.toString(), state, -code);
    }

    /**
     * Compose error message by inserting the strings in the add parameters
     * in placeholders within the error message. The message string contains
     * $$ markers for each context variable. Context variables are supplied in
     * the add parameters.
     *
     * @param code      main error code
     * @param   add     optional parameters
     *
     * @return an <code>HsqlException</code>
     */
    static HsqlException error(int code, final Object[] add) {
        return error(code, 0, add);
    }

    /**
     * Method declaration
     *
     *
     * @param code
     * @param add
     *
     * @return
     */
    static HsqlException getError(int code, Object add) {

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
        code = Math.abs(code);

        String s = getMessage(code);

        if (add != null) {
            s += ": " + add.toString();
        }

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
        return new HsqlException(s.substring(6), s.substring(0, 5), -code);

        //return getError(s);
    }

    /**
     * Creates a HsqlException useing given message and code.  The status is
     * filled based on the code.
     * <p>
     * Note use the given msg as error message, not as "add" argument.
     *
     *
     * @param msg
     * @param code
     *
     * @return a HsqlException created from the given message and code.
     */

// tony_lai@users 20020820 - patch 595073
    static HsqlException getError(String msg, int code) {

        code = Math.abs(code);

        String s = getMessage(code);

        return new HsqlException(msg, s.substring(0, 5), -code);
    }

    /**
     * Return an error message given an error code.
     *
     * @param code  error code
     *
     * @return  the error message associated with the error code
     */
    static String getMessage(final int code) {

        // todo check for bounds
        return sDescription[code];
    }

    /**
     * Method declaration
     *
     * @param e
     *
     * @return
     */
    static String getMessage(HsqlException e) {
        return e.state + " " + e.message;
    }

    /**
     * Method declaration
     *
     *
     * @param msg
     *
     * @return
     */
    static HsqlException getError(String msg) {
        return new HsqlException(msg.substring(6), msg.substring(0, 5),
                                 -GENERAL_ERROR);
    }

    /**
     * Method declaration
     *
     *
     * @param code
     *
     * @return
     */
    public static HsqlException error(int code) {
        return getError(code, null);
    }

    /**
     * Method declaration
     *
     *
     * @param code
     * @param s
     *
     * @return
     */
    public static HsqlException error(int code, String s) {
        return getError(code, s);
    }

    /**
     * Method declaration
     *
     *
     * @param code
     * @param i
     *
     * @return
     */
    public static HsqlException error(int code, int i) {
        return getError(code, String.valueOf(i));
    }

    /**
     * Method declaration
     *
     *
     * @param condition
     *
     * @throws HsqlException
     */
    static void doAssert(boolean condition) throws HsqlException {
        doAssert(condition, null);
    }

    /**
     * Method declaration
     *
     *
     * @param condition
     * @param error
     *
     * @throws HsqlException
     */
    static void doAssert(boolean condition,
                         String error) throws HsqlException {

        if (!condition) {
            printStack();

            throw getError(ASSERT_FAILED, error);
        }
    }

    /**
     * Method declaration
     *
     *
     * @param condition
     * @param code
     *
     * @throws HsqlException
     */
    static void check(boolean condition, int code) throws HsqlException {
        check(condition, code, null);
    }

    /**
     * Method declaration
     *
     *
     * @param condition
     * @param code
     * @param add
     *
     * @throws HsqlException
     */
    static void check(boolean condition, int code,
                      Object add) throws HsqlException {

        if (!condition) {
            throw getError(code, add);
        }
    }

    /**
     * Method declaration
     *
     *
     * @param code
     * @param add
     *
     * @throws HsqlException
     */
    static void throwerror(int code, Object add) throws HsqlException {
        throw getError(code, add);
    }

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
// for the PrinterWriter interface

    /**
     * Method declaration
     *
     *
     * @param c
     */
    public void println(char c[]) {

        synchronized (Trace.class) {
            String s = new String(c);

            if (sTrace.length() > 0 && (s.indexOf("hsqldb.Trace") == -1)
                    && (s.indexOf("hsqldb") != -1)) {
                int i = s.indexOf('.');

                if (i != -1) {
                    s = s.substring(i + 1);
                }

                i = s.indexOf('(');

                if (i != -1) {
                    s = s.substring(0, i);
                }

                sTrace = s;
            }
        }
    }

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
    public void println(String s) {

        synchronized (Trace.class) {
            if (sTrace.length() > 0 && (s.indexOf("hsqldb.Trace") == -1)
                    && (s.indexOf("hsqldb") != -1)) {
                int i = s.indexOf('.');

                if (i != -1) {
                    s = s.substring(i + 1);
                }

                i = s.indexOf('(');

                if (i != -1) {
                    s = s.substring(0, i);
                }

                sTrace = s;
            }
        }
    }

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
    public void write(String s) {
        ;
    }

    /**
     * Constructor declaration
     *
     */
    Trace() {
        super(System.out);
    }

    /**
     * Used to print messages to System.out
     *
     *
     * @param message message to print
     */
    static void printSystemOut(String message) {
        System.out.println(message);
    }

    /**
     * Method declaration
     *
     *
     * @param l
     */
    static void trace(long l) {
        traceCaller(String.valueOf(l));
    }

    /**
     * Method declaration
     *
     *
     * @param i
     */
    static void trace(int i) {
        traceCaller(String.valueOf(i));
    }

    /**
     * Method declaration
     *
     */
    static void trace() {
        traceCaller("");
    }

    /**
     * Method declaration
     *
     *
     * @param s
     */
    static void trace(String s) {
        traceCaller(s);
    }

    /**
     * Method declaration
     *
     *
     * @throws HsqlException
     */
    static void stop() throws HsqlException {
        stop(null);
    }

    /**
     * Method declaration
     *
     *
     * @param s
     *
     * @throws HsqlException
     */
    static synchronized void stop(String s) throws HsqlException {

        if (iStop++ % 10000 != 0) {
            return;
        }

        if (FileUtil.exists("trace.stop")) {
            printStack();

            throw getError(EXTERNAL_STOP, s);
        }
    }

// fredt@users 20010701 - patch 418014 by deforest@users

    /**
     *  With trace enabled, it is sometimes hard to figure out
     *  what a true exception is versus an exception generated
     *  by the tracing engine. These two methods define
     *  specialized versions Exceptions that are thrown during
     *  tracing so you can more easily differentiate between a
     *  Exception and a TraceException.
     */
    static class TraceException extends Exception {

        TraceException() {
            super("Trace");
        }
    }

    private static void printStack() {

        Exception e = new TraceException();

        e.printStackTrace();
    }

    static class TraceCallerException extends Exception {

        TraceCallerException() {
            super("TraceCaller");
        }
    }

    private static synchronized void traceCaller(String s) {

        Exception e = new TraceCallerException();

        sTrace = "";

        e.printStackTrace(tTracer);

        s = sTrace + "\t" + s;

// fredt@users 20010701 - patch 418014 by deforest@users
// trace to System.out is handy if only trace messages of hsql are required
        if (TRACESYSTEMOUT) {
            System.out.println(s);
        } else {
            DriverManager.println(s);
        }
    }

    public static void main(String[] args) {

        System.out.println(
            Trace.error(
                Trace.ERROR_IN_SCRIPT_FILE,
                Trace.BinaryDatabaseScriptReader_readExistingData,
                new Object[] {
            "tablename", new Integer(3), new Integer(2)
        }));
    }
}
