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

import org.hsqldb.HsqlProperties;
import java.io.*;
import java.sql.*;
import junit.framework.*;

/**
 * Test large cached tables by setting up a cached table of 100000 records
 * or more and a much smaller memory table with about 1/100th row count.
 * Populate both tables so that an indexed column of the cached table has a
 * foreign key reference to the the main table.
 *
 * This database can be used to demonstrate efficient queries to retrieve
 * the data from the cached table.
 *
 *
 * @author fredt@users
 */
public class TestCacheSize extends TestCase {

    protected String url      = "jdbc:hsqldb:";
    protected String filepath = "/hsql/test/testcachesize";
    String           user;
    String           password;
    Statement        sStatement;
    Connection       cConnection;

    public TestCacheSize(String name) {
        super(name);
    }

    protected void setUp() {

        user        = "sa";
        password    = "";
        sStatement  = null;
        cConnection = null;

        try {
            HsqlProperties props      = new HsqlProperties(filepath);
            boolean        fileexists = props.checkFileExists();

            Class.forName("org.hsqldb.jdbcDriver");
            System.out.println("connect");
            System.out.println(
                new java.util.Date(System.currentTimeMillis()));

            cConnection = DriverManager.getConnection(url + filepath, user,
                    password);

            System.out.println("connected");
            System.out.println(
                new java.util.Date(System.currentTimeMillis()));

            if (fileexists == false) {
                sStatement = cConnection.createStatement();

                sStatement.execute("SHUTDOWN");
                cConnection.close();
                props.load();
                props.setProperty("hsqldb.cache_scale", "12");
                props.save();

                cConnection = DriverManager.getConnection(url + filepath,
                        user, password);
                sStatement = cConnection.createStatement();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("TestSql.setUp() error: " + e.getMessage());
        }
    }

    /**
     * Fill up the cache
     *
     *
     */
    public void testFillUp() {

        int    bigrows   = 100000;
        int    smallrows = 0xfff;
        double value     = 0;
        String ddl1      = "DROP TABLE zip IF EXISTS;";
        String ddl2      = "CREATE TABLE zip( zip INT IDENTITY );";
        String ddl3 = "DROP TABLE test IF EXISTS;"
                      + "CREATE CACHED TABLE test( id INT IDENTITY,"
                      + " firstname VARCHAR, " + " lastname VARCHAR, "
                      + " zip INTEGER, " + " filler VARCHAR, "
                      + " FOREIGN KEY (zip) REFERENCES zip(zip) );";
        String filler =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZ"
            + "ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZ";

        try {
            java.util.Random randomgen = new java.util.Random();

            sStatement.execute(ddl1);
            sStatement.execute(ddl2);
            sStatement.execute(ddl3);

            int i;

            for (i = 0; i <= smallrows; i++) {
                sStatement.execute("INSERT INTO zip VALUES(null);");
            }

            PreparedStatement ps = cConnection.prepareStatement(
                "INSERT INTO test (firstname,lastname,zip,filler) VALUES (?,?,?,?)");

            ps.setString(1, "Julia");
            ps.setString(2, "Clancy");

            for (i = 0; i < bigrows; i++) {
                ps.setInt(3, randomgen.nextInt() & smallrows);
                ps.setString(4, randomgen.nextLong() + filler);
                ps.execute();

                if (i % 10000 == 0) {
                    System.out.println(i);
                    System.out.println(
                        new java.util.Date(System.currentTimeMillis()));
                }
            }

            System.out.println(i);
            System.out.println(
                new java.util.Date(System.currentTimeMillis()));
            sStatement.execute("SHUTDOWN");
            System.out.println("shutdown");
            System.out.println(
                new java.util.Date(System.currentTimeMillis()));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        assertEquals(true, true);
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

        TestCase testC = new TestCacheSize("testFillUp");

        testC.run();
    }
}
