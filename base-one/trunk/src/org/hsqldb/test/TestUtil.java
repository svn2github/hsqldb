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


package org.hsqldb.test;

import java.sql.*;
import java.io.*;

public class TestUtil {

    /**
     * Runs the preformatted script.
     */
    static void testScript(Connection cConnection, String path) {

        try {
            Statement sStatement = cConnection.createStatement();
            File      testfile   = new File(path);
            LineNumberReader read =
                new LineNumberReader(new FileReader(testfile));
            String s = "";

            print("Opened test script file: " + testfile.getAbsolutePath());

            int startlineno = 1;
            int lineno      = 1;

            for (; ; lineno++) {
                String line = read.readLine();

                if (line == null && s == null) {
                    break;
                }

                if (line != null && line.startsWith(" ")) {
                    s += line;
                } else {
                    try {
                        test(sStatement, s, startlineno);
                    } catch (Exception e) {
                        e.printStackTrace();
                        print("test script file error: " + e.getMessage());
                    }

                    startlineno = lineno;
                    s           = line;
                }
            }

            sStatement.close();
            print("Processed lines: " + --lineno);
        } catch (Exception e) {
            e.printStackTrace();
            print("test script file error: " + e.getMessage());
        }
    }

    /**
     *  Performs a preformatted statement or group of statements and throws
     *  if the result does not match the expected one.
     *
     * @param  stat
     * @param  s
     * @throws  Exception
     */
    static void test(Statement stat, String s, int line) throws Exception {

        String result = "";
        char   type   = ' ';

        if (s.trim().length() == 0) {
            return;
        }

        if (s.startsWith("/*")) {
            type = s.charAt(2);

            int end = s.indexOf("*/");

            result = s.substring(3, end);
        }

        try {
            stat.execute(s);

            int       u = stat.getUpdateCount();
            int       i = 0;
            ResultSet r;

            switch (type) {

                case ' ' :
                    break;

                case 'u' :
                    if (u != Integer.parseInt(result)) {
                        throw new Exception("Line: " + line + " "
                                            + "Expected update count="
                                            + result
                                            + " but update count was " + u
                                            + " / " + s);
                    }
                    break;

                case 'r' :
                    if (u != -1) {
                        throw new Exception("Line: " + line + " "
                                            + "Expected ResultSet"
                                            + " but update count was " + u
                                            + " / " + s);
                    }

                    r = stat.getResultSet();

                    r.next();

                    String col = r.getString(1);

                    if (r.wasNull() || col == null) {
                        if (!result.equals("")) {
                            throw new Exception("Line: " + line + " "
                                                + "Expected " + result
                                                + " but got null / " + s);
                        }
                    } else if (!col.equals(result)) {
                        throw new Exception("Line: " + line + " "
                                            + "Expected >" + result + "<"
                                            + " but got >" + col + "< / "
                                            + s);
                    }
                    break;

                case 'c' :
                    if (u != -1) {
                        throw new Exception("Line: " + line + " "
                                            + "Expected ResultSet"
                                            + " but update count was " + u
                                            + " / " + s);
                    }

                    r = stat.getResultSet();

                    while (r.next()) {
                        i++;
                    }

                    if (i != Integer.parseInt(result)) {
                        throw new Exception("Line: " + line + " "
                                            + "Expected " + result + " rows "
                                            + " but got " + i + " rows / "
                                            + s);
                    }
                    break;

                case 'e' :
                    throw new Exception("Line: " + line + " "
                                        + "Expected error "
                                        + "but got no error / " + s);
            }
        } catch (SQLException e) {
            if (type != 'e') {
                throw new Exception("Line: " + line + " " + "Expected "
                                    + type + "/" + result + " but got error "
                                    + e.getMessage() + " / " + s);
            }
        }
    }

    static void print(String s) {
        System.out.println(s);
    }
}
