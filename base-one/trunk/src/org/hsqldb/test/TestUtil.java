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


package org.hsqldb.test;

import java.sql.*;
import java.io.*;
import java.util.Vector;

/**
 * Utility class providing methodes for submitting test statements or
 * scripts to the database, comparing the results returned with
 * the expected results. The test script format is compatible with existing
 * scripts.
 *
 * @author Ewan Slater
 * @author fredt@users
 */
public class TestUtil {

    /**
     * Runs a preformatted script.<p>
     *
     * Where a result set is required, each line in the script will
     * be interpreted as a seperate expected row in the ResultSet
     * returned by the query.  Within each row, fields should be delimited
     * using either comma (the default), or a user defined delimiter
     * which should be specified in the System property TestUtilFieldDelimiter
     * @param cConnection Connection object for the database
     * @param path Path of the script file to be tested
     */
    static void testScript(Connection connection, String path) {

        try {
            Statement statement = connection.createStatement();
            File      testfile  = new File(path);
            LineNumberReader reader =
                new LineNumberReader(new FileReader(testfile));
            Vector section = null;

            print("Opened test script file: " + testfile.getAbsolutePath());

            /**
             * we read the lines from the start of one section of the script "/*"
             *  until the start of the next section, collecting the lines
             *  in the Vector lines.
             *  When a new section starts, we will pass the vector of lines
             *  to the test method to be processed.
             */
            int startLine = 1;

            while (true) {
                boolean startSection = false;
                String  line         = reader.readLine();

                if (line == null) {
                    break;
                }

                line = line.substring(
                    0, org.hsqldb.lib.StringUtil.rTrimSize(line));;

                if (line.length() == 0) {
                    continue;
                }

                //...check if we're starting a new section...
                if (line.startsWith("/*")) {
                    startSection = true;
                }

                if (line.charAt(0) != ' ' && line.charAt(0) != '*') {
                    startSection = true;
                }

                if (startSection) {

                    //...if we are, test the previous section (if it exists)...
                    if (section != null) {
                        testSection(statement, section, startLine);
                    }

                    //...and then start a new section...
                    section   = new Vector();
                    startLine = reader.getLineNumber();
                }

                section.add(line);
            }

            //send the last section for testing
            if (section != null) {
                testSection(statement, section, startLine);
            }

            statement.close();
            print("Processed lines: " + reader.getLineNumber());
        } catch (Exception e) {
            print("test script file error: " + e.getMessage());
        }
    }

    /**
     * Performs a preformatted statement or group of statements and throws
     *  if the result does not match the expected one.
     * @param line start line in the script file for this test
     * @param stat Statement object used to access the database
     * @param s Contains the type, expected result and SQL for the test
     */
    static void test(Statement stat, String s, int line) {

        //maintain the interface for this method
        Vector section = new Vector();

        section.add(s);
        testSection(stat, section, line);
    }

    /**
     * Method to save typing ;-)
     * @param s String to be printed
     */
    static void print(String s) {
        System.out.println(s);
    }

    /**
     * Takes a discrete section of the test script, contained in the
     * section vector, splits this into the expected result(s) and
     * submits the statement to the database, comparing the results
     * returned with the expected results.
     * If the actual result differs from that expected, or an
     * exception is thrown, then the appropriate message is printed.
     * @param stat Statement object used to access the database
     * @param aSection Vector of script lines containing a discrete
     * section of script (i.e. test type, expected results,
     * SQL for the statement).
     * @param line line of the script file where this section started
     */
    private static void testSection(Statement stat, Vector section,
                                    int line) {

        String[] rows = new String[section.size()];

        //loop over the Vector, and pull all the rows out as Strings
        for (int i = 0; i < section.size(); i++) {
            rows[i] = (String) section.get(i);
        }

        //get the type of the test query from row[0]...
        char type = ' ';

        if (rows[0].startsWith("/*")) {
            type = rows[0].charAt(2);
        }

        switch (type) {

            case ' ' :

            //section is not a test -- it should not throw any exception though
            case 'e' :

                //section must throw an exception
                break;

            case 'r' :
            case 'u' :

            //section should return an update count that matches
            case 'c' :

                //section should return a result set that matches
                rows[0] = rows[0].substring(3);
                break;

            //section should return a result set with row count that matches
        }

        int resStartRow = 0;

        //if the rest of row[0] is empty then the result set starts at rows[1]
        if (rows[0].trim().length() == 0) {
            resStartRow = 1;

            //then loop over rows[], array, starting from the back, first pulling out the
            //statement to be run aginst the database, and then the result set
            //which can go into a new String array (for which we will know the dimensions).
        }

        StringBuffer sqlBuff = new StringBuffer();
        int          endIndex;
        int          resEndRow = 0;
        int          k         = rows.length - 1;

        do {

            //check to see if the row contains the end of the result set
            if ((endIndex = rows[k].indexOf("*/")) != -1) {

                //then this is the end of the result set
                sqlBuff.insert(0, rows[k].substring(endIndex + 2));

                rows[k] = rows[k].substring(0, endIndex);

                if (rows[k].length() == 0) {
                    resEndRow = k - 1;
                } else {
                    resEndRow = k;
                }

                break;
            } else {
                sqlBuff.insert(0, rows[k]);
            }

            k--;
        } while (k >= 0);

        //now we'll put the result rows into their own array
        String[] resRows = new String[((resEndRow - resStartRow) + 1)];

        for (int i = 0; i <= (resEndRow - resStartRow); i++) {
            resRows[i] = rows[(resStartRow + i)];
        }

        String sqlStr = sqlBuff.toString();    //since we shouldn't manipulate it from here on

        /**
         * now we have the SQL statement and the expected results
         * and the type of the transaction, it's time to call the database
         */

        //let's get the delimiter for results
        String delim = System.getProperty("TestUtilFieldDelimiter", ",");
        StringBuffer errMsg =
            new StringBuffer("Section starting at Line: ").append(line);

        errMsg.append("\nSQL: ").append(sqlStr).append("\nExpected ");

        try {

            //make the call
            stat.execute(sqlStr);

            int       updateCount = stat.getUpdateCount();
            ResultSet resultSet   = stat.getResultSet();

            //check we wanted an update count if we got one, and if it was correct
            if (updateCount != -1) {
                if (type == 'u') {
                    if (updateCount != Integer.parseInt(resRows[0])) {
                        errMsg.append("update count = ").append(resRows[0]);
                        errMsg.append(" but update count was ").append(
                            updateCount);

                        throw new Exception(errMsg.toString());
                    }
                } else if (type == ' ') {

                    // section is to be executed only
                } else if (type == 'e') {

                    // should have returned an error
                    errMsg.append(
                        "error was expected but update count was ").append(
                        updateCount);
                } else {

                    //then we expected a resultSet, but got an update
                    errMsg.append("ResultSet, but update count was ");
                    errMsg.append(updateCount);

                    throw new Exception(errMsg.toString());
                }
            } else {

                //from here on in we're expecting a ResultSet so prefix error message accordingly
                errMsg.append("ResultSet ");

                //...and if type r, the correct content...
                int count = 0;

                while (resultSet.next()) {
                    if ((type == 'r') && (count < resRows.length)) {

                        //split the result row into individual fields
                        String[] expected = resRows[count].split(delim);

                        //check that we've got the number of columns expected...
                        if (resultSet.getMetaData().getColumnCount()
                                == expected.length) {

                            //...if so, load column values into a new String array...
                            String[] columns = new String[expected.length];

                            for (int i = 0; i < expected.length; i++) {
                                columns[i] = resultSet.getString(i + 1);

                                //...check that the contents of the two arrays are the same...
                            }

                            for (int j = 0; j < columns.length; j++) {
                                if (columns[j] == null) {
                                    columns[j] = "NULL";
                                }

                                if (!columns[j].equals(expected[j].trim())) {

                                    //...if not, better build errMsg and throw an exception...
                                    StringBuffer partR = new StringBuffer(
                                        "containing value(s):\n");
                                    StringBuffer partC = new StringBuffer(
                                        "but got value(s):\n");

                                    for (int i = 0; i < columns.length; i++) {
                                        partR.append(expected[i]);
                                        partC.append(columns[i]);

                                        if (i < columns.length - 1) {
                                            partR.append(delim);
                                            partC.append(delim);
                                            partR.append(" ");
                                            partC.append(" ");
                                        } else {
                                            partR.append("\n");
                                            partC.append("\n");
                                        }
                                    }

                                    errMsg.append(partR);
                                    errMsg.append(partC);

                                    throw new Exception(errMsg.toString());
                                }
                            }
                        } else {

                            //...otherwise throw an exception...
                            errMsg.append("with ");
                            errMsg.append(expected.length);
                            errMsg.append(" columns ");
                            errMsg.append("but got ResultSet with ");
                            errMsg.append(
                                resultSet.getMetaData().getColumnCount());
                            errMsg.append(" columns.");

                            throw new Exception(errMsg.toString());
                        }
                    }

                    count++;
                }

                //check to see if our result set had the expected number of rows
                if (((type == 'c') && (count != Integer.parseInt(resRows[0])))
                        || ((type == 'r') && (count != resRows.length))) {
                    errMsg.append("containing ");

                    if (type == 'c') {
                        errMsg.append(Integer.parseInt(resRows[0]));
                    } else {
                        errMsg.append(resRows.length);
                    }

                    errMsg.append(" rows but got a ResultSet containing ");
                    errMsg.append(count);
                    errMsg.append(" rows.");

                    throw new Exception(errMsg.toString());
                }
            }
        } catch (SQLException sqlX) {
            if (type != 'e') {
                errMsg.append(type);
                errMsg.append(" with result");

                if (type == 'r') {
                    errMsg.append("s: \n");

                    for (int j = 0; j < resRows.length; j++) {
                        errMsg.append(resRows[j]).append("\n");
                    }
                } else {
                    errMsg.append(": ").append(resRows[0]);
                }

                errMsg.append("but got SQLException ").append(
                    sqlX.getMessage());
                print("error " + errMsg.toString());
            }
        } catch (Exception x) {
            print(x.getMessage());
        }
    }
}
