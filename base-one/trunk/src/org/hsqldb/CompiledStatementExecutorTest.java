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


package org.hsqldb;

import org.hsqldb.lib.*;
import java.sql.*;

/**
 *
 * @author  boucherb@users.sourceforge.net
 */
public class CompiledStatementExecutorTest {

    static CompiledStatement csInsert;
    static CompiledStatement csUpdate;
    static CompiledStatement csSelect;
    static CompiledStatement csDelete;
    static String[]          asInsert;
    static String[]          asUpdate;
    static String[]          asDelete;
    static String[]          asSelect;
    static Session           session;
    static Parser            parser;
    static Tokenizer         tokenizer;
    static Database          database;
    static int               rows;
    static int               run;
    static long              parsedTot;
    static long              compiledTot;
    static StopWatch         sw;
    static Integer[]         idValues;
    static int[]             iTypes  = new int[] {
        DITypes.INTEGER, DITypes.VARCHAR, DITypes.VARCHAR, DITypes.INTEGER
    };
    static Object[]          iValues = new Object[] {
        null, "Mary", "Peterson-Clancy", null
    };

    static void init() throws Exception {

        asInsert = new String[rows];
        asUpdate = new String[rows];
        asDelete = new String[rows];
        asSelect = new String[rows];

        System.out.println("----------------------------------");
        System.out.println("CompiledStatementExecutorTest");
        System.out.println("Initializing...");

        sw        = new StopWatch();
        database  = new Database("/cstest/test");
        session   = database.sessionManager.getSysSession();
        tokenizer = new Tokenizer();
        parser    = new Parser(database, tokenizer, session);

        session.execute("drop table test if exits");
        session.execute("create cached table test(id int primary key, "
                        + "fname varchar, lname varchar, zip int)");
        tokenizer.reset("insert into test values(?,?,?,?)");

        csInsert = parser.compileStatement(null);

        tokenizer.reset("update test set lname = 'Johnson' where id = ?");

        csUpdate = parser.compileStatement(null);

        tokenizer.reset("delete from test where id = ?");

        csDelete = parser.compileStatement(null);

        tokenizer.reset("select * from test where id = ?");

        csSelect = parser.compileStatement(null);
        idValues = new Integer[rows];

        for (int i = 0; i < rows; i++) {
            asInsert[i] = "insert into test values(" + i
                          + ",'Mary','Peterson-Clancy'," + i + ")";
            asUpdate[i] = "update test set lname = 'Johnson' where id = " + i;
            asDelete[i] = "delete from test where id = " + i;
            asSelect[i] = "select * from test where id = " + i;
            idValues[i] = new Integer(i);
        }

        System.out.println("#rows to insert, update, select, delete: "
                           + rows);
        System.out.println(sw.elapsedTimeToMessage("Finished initializing"));

        parsedTot   = 0;
        compiledTot = 0;
        run         = 1;
    }

    static void test() throws Exception {

        System.out.println("Test Run #" + run);
        System.out.println("----------------------------------");
        System.out.println("compiled statement execution...");
        testCompiled();
        System.out.println("----------------------------------");
        System.out.println("parsed statement execution...");
        testParsed();
        System.out.println("----------------------------------");
        printSummary();
        System.out.println("----------------------------------");
        session.execute("checkpoint");
        System.gc();

        parsedTot   = 0;
        compiledTot = 0;

        run++;

        System.out.flush();
    }

    static void printSummary() {

        System.out.println("compiled : " + compiledTot + " ms");
        System.out.println("parsed   : " + parsedTot + " ms");
        System.out.println("ratio    : " + parsedTot / (double) compiledTot);
    }

    /**
     * A quick test of functionality and performance advantage for
     * compiled statements.
     *
     * @param args ignored
     * @throws Exception if a database access error occurs
     *
     */
    public static void main(String[] args) throws Exception {

        rows = Integer.parseInt(args[0]);

        init();

        while (true) {
            test();
        }
    }

    static void testParsed() throws Exception {

        sw.zero();

        for (int i = 0; i < rows; i++) {
            session.execute(asInsert[i]);
        }

        for (int i = 0; i < rows; i++) {
            session.execute(asUpdate[i]);
        }

        for (int i = 0; i < rows; i++) {
            session.execute(asSelect[i]);
        }

        for (int i = 0; i < rows; i++) {
            session.execute(asDelete[i]);
        }

        parsedTot = sw.elapsedTime();
    }

    static void testCompiled() throws Exception {

        sw.zero();

        for (int i = 0; i < rows; i++) {
            iValues[0] = iValues[3] = idValues[i];

            csInsert.bind(iValues, iTypes);
            session.execute(csInsert);
        }

        for (int i = 0; i < rows; i++) {
            iValues[0] = idValues[i];

            csUpdate.bind(iValues, iTypes);
            session.execute(csUpdate);
        }

        for (int i = 0; i < rows; i++) {
            iValues[0] = idValues[i];

            csSelect.bind(iValues, iTypes);
            session.execute(csSelect);
        }

        for (int i = 0; i < rows; i++) {
            iValues[0] = idValues[i];

            csDelete.bind(iValues, iTypes);
            session.execute(csDelete);
        }

        compiledTot = sw.elapsedTime();
    }
}
