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

import org.hsqldb.lib.StopWatch;

/**
 * A quick test of the new CompiledStatement and batch execution facilities.
 *
 * @author boucher@users.sourceforge.net
 * @since HSQLDB 1.7.2
 * @version 1.7.2
 */
public class BatchExecutionTest {

    static final String drop_table_sql = "drop table test if exists";
    static final String create_cached  = "create cached ";
    static final String create_memory  = "create memory ";
    static final String create_temp    = "create temp ";
    static final String table_sql = "table test(id int primary key,"
                                    + "fname varchar, lname "
                                    + "varchar, zip int)";
    static final String insert_sql    = "insert into test values(?,?,?,?)";
    static final int[]  insert_ptypes = new int[] {
        Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.INTEGER
    };
    static final String update_sql =
        "update test set fname = 'Hans' where id = ?";
    static final int[]  update_ptypes = new int[]{ Types.INTEGER };
    static final String select_sql    = "select * from test where id = ?";
    static final int[]  select_ptypes = new int[]{ Types.INTEGER };
    static final String delete_sql    = "delete from test where id = ?";
    static final int[]  delete_ptypes = new int[]{ Types.INTEGER };
    static final String def_db_path   = ".";
    static final int    def_runs      = 3;
    static final int    rows          = 10000;

//    static final HsqlRuntime runtime       = HsqlRuntime.getHsqlRuntime();
    static Database database;
    static Session  session;

    static void checkResult(Result r) throws Exception {

        if (r.iMode == ResultConstants.ERROR) {
            throw Trace.error(r.getStatementID(), r.getMainString());
        }
    }

    static void print(String s) {
        System.out.print(s);
    }

    static void println(String s) {
        System.out.println(s);
    }

    static void printCommandStats(StopWatch sw, String cmd) {

        long et = sw.elapsedTime();

        print(sw.elapsedTimeToMessage(rows + " " + cmd));
        println(" " + ((1000 * rows) / et) + " rows/s.");
    }

/*
    static void printMemoryStats() {

        System.gc();
        println("used memory      : " + runtime.usedMemory());
        println("available memory : " + runtime.availableMemory());
    }
*/
    static Result prepareStatement(Session session, String sql,
                                   int[] types) throws Exception {

        Result p;
        Result r;
        Result o;

        p = new Result(ResultConstants.SQLPREPARE);

        p.setMainString(sql);

        r = session.execute(p);

        checkResult(r);

        o         = new Result(ResultConstants.SQLEXECUTE);
        o.colType = types;

        o.setStatementID(r.getStatementID());
        println("prepared: " + sql);

        return o;
    }

    public static void main(String[] args) throws Exception {

        int    runs;
        String db_path;

        runs    = def_runs;
        db_path = def_db_path;

        try {
            runs = Integer.parseInt(args[0]);
        } catch (Exception e) {}

        try {
            db_path = args[1];
        } catch (Exception e) {}

        // get the database and its sys session
        database = DatabaseManager.getDatabase(DatabaseManager.S_FILE,
                                               db_path, false);
        session = database.sessionManager.getSysSession();

        println("---------------------------------------");
        println("featuring cached table");
        println("---------------------------------------");

        // drop and recreate the test table
        println(drop_table_sql);
        session.sqlExecuteDirect(drop_table_sql);
        println(create_cached + table_sql);
        session.sqlExecuteDirect(create_cached + table_sql);
        test(runs);

        // drop the test table and shut down database
        println(drop_table_sql);
        session.sqlExecuteDirect(drop_table_sql);
        println("---------------------------------------");
        println("shutting down database");
        session.sqlExecuteDirect("shutdown");
        println("---------------------------------------");

        // get the database and its sys session
        database = DatabaseManager.getDatabase(DatabaseManager.S_FILE,
                                               db_path, false);
        session = database.sessionManager.getSysSession();

        println("---------------------------------------");
        println("featuring memory table");
        println("---------------------------------------");

        // drop and recreate the test table
        println(drop_table_sql);
        session.sqlExecuteDirect(drop_table_sql);
        println(create_memory + table_sql);
        session.sqlExecuteDirect(create_memory + table_sql);
        test(runs);

        // drop the test table and shut down database
        println(drop_table_sql);
        println("---------------------------------------");
        println("shutting down database");
        session.sqlExecuteDirect("shutdown");
        println("---------------------------------------");

        // get the database and its sys session
        database = DatabaseManager.getDatabase(DatabaseManager.S_FILE,
                                               db_path, false);
        session = database.sessionManager.getSysSession();

        println("---------------------------------------");
        println("featuring temp table");
        println("---------------------------------------");

        // drop and recreate the test table
        println(drop_table_sql);
        session.sqlExecuteDirect(drop_table_sql);
        println(create_memory + table_sql);
        session.sqlExecuteDirect(create_memory + table_sql);
        test(runs);

        // drop the test table
        println(drop_table_sql);
        println("---------------------------------------");
        println("shutting down database");
        session.sqlExecuteDirect("shutdown");
        println("---------------------------------------");
    }

    public static void test(int runs) throws Exception {

        Result    insertStmnt;
        Result    updateStmnt;
        Result    selectStmnt;
        Result    deleteStmnt;
        Integer   ival;
        Object[]  row;
        Object[]  rowid;
        StopWatch sw;

        sw = new StopWatch();

        // prepare the statements
        insertStmnt = prepareStatement(session, insert_sql, insert_ptypes);
        updateStmnt = prepareStatement(session, update_sql, update_ptypes);
        selectStmnt = prepareStatement(session, select_sql, select_ptypes);
        deleteStmnt = prepareStatement(session, delete_sql, delete_ptypes);

        println("---------------------------------------");
        println(sw.elapsedTimeToMessage("statements prepared"));
        println("---------------------------------------");
        sw.zero();

        // set up the batch data
        for (int i = 0; i < rows; i++) {
            ival  = new Integer(i);
            row   = new Object[] {
                ival, "Julia", "Peterson-Clancy", ival
            };
            rowid = new Object[]{ ival };

            insertStmnt.add(row);
            updateStmnt.add(rowid);
            selectStmnt.add(rowid);
            deleteStmnt.add(rowid);
        }

        println("---------------------------------------");
        println(
            sw.elapsedTimeToMessage(
                "" + 4 * rows
                + " parameter items created and added to batch"));
        sw.zero();

        // do the test loop forever
        for (int i = 0; i < runs; i++) {
            println("---------------------------------------");

            // inserts
            sw.zero();
            session.execute(insertStmnt);
            printCommandStats(sw, "inserts");

            // updates
            sw.zero();
            session.execute(updateStmnt);
            printCommandStats(sw, "updates");

            // selects
            sw.zero();
            session.execute(selectStmnt);
            printCommandStats(sw, "selects");

            // deletes
            sw.zero();
            session.execute(deleteStmnt);
            printCommandStats(sw, "deletes");
/*
            // memory stats
            printMemoryStats();
*/
        }
    }
}
