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

import junit.framework.TestCase;
import junit.framework.TestResult;

import java.sql.*;
import java.util.*;

public class TestDatabaseMetaData extends TestBase {

    public TestDatabaseMetaData(String name) {
        super(name);
    }

    public void test() throws Exception {

        Connection        conn = newConnection();
        PreparedStatement pstmt;
        int               updateCount;

        try {
            pstmt = conn.prepareStatement(
                "CREATE TABLE t1 (cha CHARACTER, dec DECIMAL, doub DOUBLE, lon BIGINT, in INTEGER, sma SMALLINT, tin TINYINT, "
                + "dat DATE DEFAULT CURRENT_DATE, tim TIME DEFAULT CURRENT_TIME, timest TIMESTAMP DEFAULT CURRENT_TIMESTAMP );");
            updateCount = pstmt.executeUpdate();

            assertTrue("expected update count of zero", updateCount == 0);

            DatabaseMetaData dbmd = conn.getMetaData();
            ResultSet rs = dbmd.getTables(null, null, null,
                                          new String[]{ "TABLE" });
            ArrayList tablesarr = new ArrayList();
            int       i;

            for (i = 0; rs.next(); i++) {
                String tempstr =
                    rs.getString("TABLE_NAME").trim().toLowerCase();

                tablesarr.add(tempstr);
            }

            rs.close();
            assertTrue("expected table count of 1", i == 1);

            Iterator it = tablesarr.iterator();

            for (; it.hasNext(); ) {

                // create new ArrayList and HashMap for the table
                String tablename = ((String) it.next()).trim();
                List   collist   = new ArrayList(30);

                rs = dbmd.getColumns(null, null, tablename.toUpperCase(),
                                     null);

                for (i = 0; rs.next(); i++) {
                    collist.add(
                        rs.getString("COLUMN_NAME").trim().toLowerCase());
                }

                rs.close();
            }

            pstmt = conn.prepareStatement(
                "CREATE TABLE t_1 (cha CHARACTER, dec DECIMAL, doub DOUBLE, lon BIGINT, in INTEGER, sma SMALLINT, tin TINYINT, "
                + "dat DATE DEFAULT CURRENT_DATE, tim TIME DEFAULT CURRENT_TIME, timest TIMESTAMP DEFAULT CURRENT_TIMESTAMP );");
            updateCount = pstmt.executeUpdate();

            assertTrue("expected update count of zero", updateCount == 0);

            rs = dbmd.getTables(null, null, "T\\_1", new String[]{ "TABLE" });

            for (i = 0; rs.next(); i++) {
                String tempstr =
                    rs.getString("TABLE_NAME").trim().toLowerCase();

                tablesarr.add(tempstr);
            }

            rs.close();
            assertTrue("expected table count of 1", i == 1);
            conn.close();
        } catch (Exception e) {
            assertTrue("unable to prepare or execute DDL", false);
        } finally {
            conn.close();
        }
    }

    public static void main(String[] args) throws Exception {

        TestResult            result;
        TestCase              test;
        java.util.Enumeration failures;
        int                   count;

        result = new TestResult();
        test   = new TestDatabaseMetaData("test");

        test.run(result);

        count = result.failureCount();

        System.out.println("TestDatabaseMetaData failure count: " + count);

        failures = result.failures();

        while (failures.hasMoreElements()) {
            System.out.println(failures.nextElement());
        }
    }
}
