/* Copyright (c) 2001-2002, The HSQL Development Group
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
import junit.framework.*;

/**
 * Test sql statements via jdbc against in-memory database
 * @author fredt@users.sourceforge.net
 */
public class TestSql extends TestCase {

//    protected String url = "jdbc:hsqldb:hsql://localhost";
    String     url = "jdbc:hsqldb:.";
    String     user;
    String     password;
    Statement  sStatement;
    Connection cConnection;

    public TestSql(String name) {
        super(name);
    }

    protected void setUp() {

        user        = "sa";
        password    = "";
        sStatement  = null;
        cConnection = null;

        try {
            Class.forName("org.hsqldb.jdbcDriver");

            cConnection = DriverManager.getConnection(url, user, password);
            sStatement  = cConnection.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("TestSql.setUp() error: " + e.getMessage());
        }
    }

    public void testMetaData() {

        String ddl1 =
            "CREATE TABLE USER(USER_ID INTEGER NOT NULL PRIMARY KEY,LOGIN_ID VARCHAR(128) NOT NULL,USER_NAME VARCHAR(254) DEFAULT ' ' NOT NULL,CREATE_DATE TIMESTAMP DEFAULT 'CURRENT_TIMESTAMP' NOT NULL,UPDATE_DATE TIMESTAMP DEFAULT 'CURRENT_TIMESTAMP' NOT NULL,LAST_ACCESS_DATE TIMESTAMP,CONSTRAINT IXUQ_LOGIN_ID0 UNIQUE(LOGIN_ID))";
        String ddl2 =
            "CREATE TABLE ADDRESSBOOK_CATEGORY(USER_ID INTEGER NOT NULL,CATEGORY_ID INTEGER DEFAULT 0 NOT NULL,CATEGORY_NAME VARCHAR(60) DEFAULT '' NOT NULL,CONSTRAINT SYS_PK_ADDRESSBOOK_CATEGORY PRIMARY KEY(USER_ID,CATEGORY_ID),CONSTRAINT FK_ADRBKCAT1 FOREIGN KEY(USER_ID) REFERENCES USER(USER_ID) ON DELETE CASCADE)";
        String ddl3 =
            "CREATE TABLE ADDRESSBOOK(USER_ID INTEGER NOT NULL,ADDRESSBOOK_ID INTEGER NOT NULL,CATEGORY_ID INTEGER DEFAULT 0 NOT NULL,FIRST VARCHAR(64) DEFAULT '' NOT NULL,LAST VARCHAR(64) DEFAULT '' NOT NULL,NOTE VARCHAR(128) DEFAULT '' NOT NULL,CONSTRAINT SYS_PK_ADDRESSBOOK PRIMARY KEY(USER_ID,ADDRESSBOOK_ID),CONSTRAINT FK_ADRBOOK1 FOREIGN KEY(USER_ID,CATEGORY_ID) REFERENCES ADDRESSBOOK_CATEGORY(USER_ID,CATEGORY_ID) ON DELETE CASCADE)";
        String result1 = "1";
        String result2 = "2";
        String result3 = "3";
        String result4 = "4";

        try {
            sStatement.execute(ddl1);
            sStatement.execute(ddl2);
            sStatement.execute(ddl3);

            DatabaseMetaData md = cConnection.getMetaData();

            {
                ResultSet rs;

                rs = md.getPrimaryKeys(null, null, "USER");

                ResultSetMetaData rsmd    = rs.getMetaData();
                String            result0 = "";

                for (; rs.next(); ) {
                    for (int i = 0; i < rsmd.getColumnCount(); i++) {
                        result0 += rs.getString(i + 1) + ":";
                    }

                    result0 += "\n";
                }

                System.out.println(result0);
            }

            {
                ResultSet rs;

                rs = md.getBestRowIdentifier(null, null, "USER", 0, true);

                ResultSetMetaData rsmd    = rs.getMetaData();
                String            result0 = "";

                for (; rs.next(); ) {
                    for (int i = 0; i < rsmd.getColumnCount(); i++) {
                        result0 += rs.getString(i + 1) + ":";
                    }

                    result0 += "\n";
                }

                System.out.println(result0);
            }

            {
                ResultSet rs = md.getImportedKeys(null, null, "ADDRESSBOOK");
                ResultSetMetaData rsmd = rs.getMetaData();

                result1 = "";

                for (; rs.next(); ) {
                    for (int i = 0; i < rsmd.getColumnCount(); i++) {
                        result1 += rs.getString(i + 1) + ":";
                    }

                    result1 += "\n";
                }

                System.out.println(result1);
            }

            {
                ResultSet rs = md.getCrossReference(null, null,
                                                    "ADDRESSBOOK_CATEGORY",
                                                    null, null,
                                                    "ADDRESSBOOK");
                ResultSetMetaData rsmd = rs.getMetaData();

                result2 = "";

                for (; rs.next(); ) {
                    for (int i = 0; i < rsmd.getColumnCount(); i++) {
                        result2 += rs.getString(i + 1) + ":";
                    }

                    result2 += "\n";
                }

                System.out.println(result2);
            }

            {
                ResultSet         rs = md.getExportedKeys(null, null, "USER");
                ResultSetMetaData rsmd = rs.getMetaData();

                result3 = "";

                for (; rs.next(); ) {
                    for (int i = 0; i < rsmd.getColumnCount(); i++) {
                        result3 += rs.getString(i + 1) + ":";
                    }

                    result3 += "\n";
                }

                System.out.println(result3);
            }

            {
                ResultSet rs = md.getCrossReference(null, null, "USER", null,
                                                    null,
                                                    "ADDRESSBOOK_CATEGORY");
                ResultSetMetaData rsmd = rs.getMetaData();

                result4 = "";

                for (; rs.next(); ) {
                    for (int i = 0; i < rsmd.getColumnCount(); i++) {
                        result4 += rs.getString(i + 1) + ":";
                    }

                    result4 += "\n";
                }

                System.out.println(result4);
            }
        } catch (SQLException e) {
            fail(e.getMessage());
        }

        // assert equality of exported and imported with xref
        assertEquals(result1, result2);
        assertEquals(result3, result4);
    }

    /**
     * Demonstration of a reported bug.<p>
     * Because all values were turned into strings with toString before
     * PreparedStatement.executeQuery() was called, special values such as
     * NaN were not accepted. In 1.7.0 these values are inserted as nulls
     * (fredt)<b>
     *
     * This test can be extended to cover various conversions through JDBC
     *
     */
    public void testDoubleNaN() {

        double  value   = 0;
        boolean wasNull = false;
        String  message = "DB operation completed";
        String ddl1 =
            "DROP TABLE t1 IF EXISTS;"
            + "CREATE TABLE t1 ( d DECIMAL, f DOUBLE, i INTEGER, s SMALLINT, t TINYINT );";

        try {
            sStatement.execute(ddl1);

            PreparedStatement ps = cConnection.prepareStatement(
                "INSERT INTO t1 VALUES (?,?,?,?,?)");

            /* some unrelated test stuff
            ps.setTime(1,new java.sql.Time(1000000));

            ps.setDate(1,new java.sql.Date(1000000));

            ps.setTimestamp(1,new java.sql.Timestamp(1000000));
            */
            ps.setString(1, "0.2");
            ps.setDouble(2, 0.2);
            ps.setInt(3, Integer.MAX_VALUE);
            ps.setInt(4, Short.MAX_VALUE);
            ps.setInt(5, 0);
            ps.execute();
            ps.setInt(1, 0);
            ps.setDouble(2, java.lang.Double.NaN);
            ps.setInt(3, Integer.MIN_VALUE);
            ps.setInt(4, Short.MIN_VALUE);
            ps.setInt(5, 0);
            ps.execute();

            ResultSet rs =
                sStatement.executeQuery("SELECT d, f, i, s*2, t FROM t1");
            boolean result = rs.next();

            value = rs.getDouble(2);

//            int smallintValue = rs.getShort(3);
            int integerValue = rs.getInt(3);

            if (rs.next()) {
                value        = rs.getDouble(2);
                wasNull      = rs.wasNull();
                integerValue = rs.getInt(3);
            }

            rs = sStatement.executeQuery("SELECT MAX(i) FROM t1");

            if (rs.next()) {
                int max = rs.getInt(1);

                System.out.println("Max value for i: " + max);
            }

            rs = sStatement.executeQuery(
                "CREATE TABLE cdType (ID INTEGER NOT NULL, name VARCHAR(50), PRIMARY KEY(ID))");
            rs = sStatement.executeQuery("SELECT MAX(ID) FROM cdType");

            if (rs.next()) {
                int max = rs.getInt(1);

                System.out.println("Max value for ID: " + max);
            }
        } catch (SQLException e) {
            message = e.getMessage();
        }

        // assert new behaviour
        assertEquals(true, wasNull);
    }

    protected void tearDown() {

        try {
            cConnection.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("TestSql.tearDown() error: " + e.getMessage());
        }
    }

    public static void main(String argv[]) {

        TestResult result = new TestResult();
        TestCase   testA  = new TestSql("testMetaData");
        TestCase   testB  = new TestSql("testDoubleNaN");

        testA.run(result);
        testB.run(result);
        System.out.println("TestSql error count: " + result.failureCount());
    }
}
