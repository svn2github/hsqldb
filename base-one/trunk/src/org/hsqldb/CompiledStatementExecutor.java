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

import org.hsqldb.lib.HsqlLinkedList;
import org.hsqldb.lib.StopWatch;
import org.hsqldb.lib.StringUtil;

/**
 * Provides execution of CompiledStatement objects.
 *
 * If multiple threads access a CompiledStatementExecutor concurrently,
 * and at least one of the threads calls an executeXXX method other than
 * execute(CompiledStatement cs), it must be synchronized externally,
 * relative to both this object's Session and the Session's Database.
 * Internally, this is accomplished by synchronizing on the Session
 * object's Database object.
 *
 * @author  boucherb@users.sourceforge.net
 * @vesrion 1.7.2
 * @since HSQLDB 1.7.2
 */
public class CompiledStatementExecutor {

    Session     session;
    Database    database;
    Result      updateResult;
    Result      emptyResult;

    /**
     * Creates a new instance of CompiledStatementExecutor.
     *
     * @param session the context in which to perform the execution
     */
    CompiledStatementExecutor(Session session) {

        this.session = session;
        database     = session.getDatabase();
        updateResult = new Result(ResultConstants.UPDATECOUNT);
        emptyResult  = new Result(ResultConstants.UPDATECOUNT);
//        runtime      = HsqlRuntime.getHsqlRuntime();
    }

    /**
     * Executes a generic CompiledStatement in a thread-safe manner.
     *
     * @return the result of executing the statement
     * @param cs any valid CompiledStatement
     */
    Result execute(CompiledStatement cs) {

        DatabaseManager.gc();

        // can be made more granular later, e.g. table-level locking
        synchronized (database) {
            Result result = null;

            try {
                result = executeImpl(cs);
            } catch (Throwable t) {
                result = new Result(t, cs.sql);
            }

            if (result == null) {
                result = emptyResult;
            }

            return result;
        }
    }

    /**
     * Executes a generic CompiledStatement with no synchronization.
     *
     * @param cs any valid CompiledStatement
     * @throws HsqlException if a database access error occurs
     * @return the result of executing the statement
     */
    Result executeImpl(CompiledStatement cs) throws HsqlException {

        switch (cs.type) {

            case CompiledStatement.SELECT :
                return executeSelectStatement(cs);

            case CompiledStatement.INSERT_SELECT :
                return executeInsertSelectStatement(cs);

            case CompiledStatement.INSERT_VALUES :
                return executeInsertValuesStatement(cs);

            case CompiledStatement.UPDATE :
                return executeUpdateStatement(cs);

            case CompiledStatement.DELETE :
                return executeDeleteStatement(cs);

            case CompiledStatement.CALL :
                return executeCallStatement(cs);

            default :
                throw Trace.error(Trace.OPERATION_NOT_SUPPORTED, cs.type);
        }
    }

    /**
     * Executes a CALL statement.  It is assumed that the argument is
     * of the correct type.
     *
     * @param cs a CompiledStatement of type CompiledStatement.CALL
     * @throws HsqlException if a database access error occurs
     * @return the result of executing the statement
     */
    Result executeCallStatement(CompiledStatement cs) throws HsqlException {

        Expression e;    // representing CALL
        Object     o;    // expression return value
        Result     r;
        Object[]   row;

        e = cs.expression;

        e.resolve(null);

        o = e.getValue();

        if (o instanceof Result) {
            return (Result) o;
        }

        if (o instanceof jdbcResultSet) {
            return ((jdbcResultSet) o).rResult;
        }

// boucherb@users patch 1.7.x - returning values from remote data sources
//        if (o instanceof ResultSet) {
//            return Result.newResult((ResultSet)o);
//        }
//        if (o instanceof Statement) {
//            return Result.newResult(((Statement)o).getResultSet());
//        }
        r            = Result.newSingleColumnResult("",e.getDataType());
        r.sTable[0]  = "";
        row          = new Object[1];
        row[0]       = o;

        r.add(row);

        return r;
    }

    /**
     * Executes a DELETE statement.  It is assumed that the argument is
     * of the correct type.
     *
     * @param cs a CompiledStatement of type CompiledStatement.DELETE
     * @throws HsqlException if a database access error occurs
     * @return the result of executing the statement
     */
    Result executeDeleteStatement(CompiledStatement cs) throws HsqlException {

        Table          t;
        TableFilter    f;
        Expression     c;      // delete condition
        HsqlLinkedList del;    // tuples to delete
        int            count;

        t     = cs.targetTable;
        f     = cs.tf;
        c     = cs.condition;
        count = 0;

        if (f.findFirst()) {
            del = new HsqlLinkedList();

            if (c == null) {
                do {
                    del.add(f.currentRow);
                } while (f.next());

                count = t.delete(del, session);
            } else {
                do {
                    if (c.test()) {
                        del.add(f.currentRow);
                    }
                } while (f.next());

                count = t.delete(del, session);
            }
        }

        updateResult.iUpdateCount = count;

        return updateResult;
    }

    /**
     * Executes an INSERT_XXX statement.  The argument is
     * checked to be of the correct type.
     *
     * @param cs a CompiledStatement of type CompiledStatement.INSERT_XXX
     * @throws HsqlException if a database access error occurs
     * @return the result of executing the statement
     */
    Result executeInsertStatement(CompiledStatement cs) throws HsqlException {

        switch (cs.type) {

            case CompiledStatement.INSERT_SELECT : {
                return executeInsertSelectStatement(cs);
            }
            case CompiledStatement.INSERT_VALUES : {
                return executeInsertValuesStatement(cs);
            }
            default :
                throw Trace.error(Trace.UNEXPECTED_EXCEPTION);
        }
    }

    /**
     * Executes an INSERT_SELECT statement.  It is assumed that the argument
     * is of the correct type.
     *
     * @param cs a CompiledStatement of type CompiledStatement.INSERT_SELECT
     * @throws HsqlException if a database access error occurs
     * @return the result of executing the statement
     */
    Result executeInsertSelectStatement(CompiledStatement cs)
    throws HsqlException {

        Table     t;
        Select    s;
        Result    r;
        Record    rc;
        int[]     cm;     // column map
        boolean[] ccl;    // column check list
        int[]     ct;     // column types
        Object[]  row;
        int       len;
        int       count;

        t   = cs.targetTable;
        s   = cs.select;
        ct  = t.getColumnTypes();
        r   = s.getResult(session.getMaxRows());
        rc  = r.rRoot;
        cm  = cs.columnMap;
        ccl = cs.checkColumns;
        len = cm.length;

        session.beginNestedTransaction();

        try {
            while (rc != null) {
                row = t.getNewRow(ccl);

                for (int i = 0; i < len; i++) {
                    int j = cm[i];

                    if (ct[j] != r.colType[i]) {
                        row[j] = Column.convertObject(rc.data[i], ct[j]);
                    } else {
                        row[j] = rc.data[i];
                    }
                }

                rc.data = row;
                rc      = rc.next;
            }

            count = t.insert(r, session);

            session.endNestedTransaction(false);
        } catch (HsqlException se) {

            // insert failed (violation of primary key)
            session.endNestedTransaction(true);

            throw se;
        }

        updateResult.iUpdateCount = count;

        return updateResult;
    }

    /**
     * Executes an INSERT_VALUES statement.  It is assumed that the argument
     * is of the correct type.
     *
     * @param cs a CompiledStatement of type CompiledStatement.INSERT_VALUES
     * @throws HsqlException if a database access error occurs
     * @return the result of executing the statement
     */
    Result executeInsertValuesStatement(CompiledStatement cs)
    throws HsqlException {

        Object[]     row;
        int[]        cm;    // column map
        Table        t;
        Expression[] acve;
        Expression   cve;
        int[]        ct;    // column types
        int          ci;    // column index
        int          len;

        t    = cs.targetTable;
        row  = t.getNewRow(cs.checkColumns);
        cm   = cs.columnMap;
        acve = cs.columnValues;
        ct   = t.getColumnTypes();
        len  = acve.length;

        for (int i = 0; i < len; i++) {
            cve     = acve[i];
            ci      = cm[i];
            row[ci] = cve.getValue(ct[ci]);
        }

        t.insert(row, session);

        updateResult.iUpdateCount = 1;

        return updateResult;
    }

    /**
     * Executes a SELECT statement.  It is assumed that the argument
     * is of the correct type and that it does not represent a
     * SELECT ... INTO statement.
     *
     * @param cs a CompiledStatement of type CompiledStatement.SELECT
     * @throws HsqlException if a database access error occurs
     * @return the result of executing the statement
     */
    Result executeSelectStatement(CompiledStatement cs) throws HsqlException {
        return cs.select.getResult(session.getMaxRows());
    }

    /**
     * Executes an UPDATE statement.  It is assumed that the argument
     * is of the correct type.
     *
     * @param cs a CompiledStatement of type CompiledStatement.UPDATE
     * @throws HsqlException if a database access error occurs
     * @return the result of executing the statement
     */
    Result executeUpdateStatement(CompiledStatement cs) throws HsqlException {

        Table          t;
        TableFilter    f;
        int[]          cm;    // column map
        Expression[]   acve;
        Expression     cve;
        Expression     c;     // update condition
        int[]          ct;    // column types
        int            ci;    // column index
        int            len;
        HsqlLinkedList del;
        Result         ins;
        int            size;
        int            count;
        Row            row;
        Object[]       ni;

        t    = cs.targetTable;
        cm   = cs.columnMap;
        acve = cs.columnValues;
        c    = cs.condition;
        len  = acve.length;

        // new TableFilter(t, null, false);
        f = cs.tf;

//        for (int i = 0; i < len; i++) {
//            cve = acve[i];
//            cve.resolve(f);
//        }
//
//        if (c != null) {
//            c.resetTrue();
//            c.resolve(f);
//            f.setCondition(c);
//        }
        count = 0;

        if (f.findFirst()) {
            del  = new HsqlLinkedList();
            ins  = new Result(ResultConstants.UPDATECOUNT);
            size = t.getColumnCount();
            len  = cm.length;
            ct   = t.getColumnTypes();

            if (c == null) {
                do {
                    row = f.currentRow;

                    del.add(row);

                    ni = t.getNewRow();

                    System.arraycopy(row.getData(), 0, ni, 0, size);

                    for (int i = 0; i < len; i++) {
                        ci     = cm[i];
                        ni[ci] = acve[i].getValue(ct[ci]);
                    }

                    ins.add(ni);
                } while (f.next());
            } else {
                do {
                    if (c.test()) {
                        row = f.currentRow;

                        del.add(row);

                        ni = t.getNewRow();

                        System.arraycopy(row.getData(), 0, ni, 0, size);

                        for (int i = 0; i < len; i++) {
                            ci     = cm[i];
                            ni[ci] = acve[i].getValue(ct[ci]);
                        }

                        ins.add(ni);
                    }
                } while (f.next());
            }

            session.beginNestedTransaction();

            try {
                count = t.update(del, ins, cm, session);

                session.endNestedTransaction(false);
            } catch (HsqlException se) {

                // update failed (constraint violation)
                session.endNestedTransaction(true);

                throw se;
            }
        }

        updateResult.iUpdateCount = count;

        return updateResult;
    }

// Test Results 2003-04-18
// 500 MHz Athlon, NT4 Workstation, 500 MB PC133 DRAM, using MEMORY Table
// java -server -Xms128m -Xmx128m -XX:NewRatio=2
// ----------------------------------
// parsed   : 4996 ms (4003 rows/sec)
// compiled : 942 ms (21231 rows/sec)
// ratio    : 5.3036093418259025
// ----------------------------------
// java -client -Xms128m -Xmx128m -XX:NewRatio=2
// ----------------------------------
// parsed   : 7771 ms (2573 rows/sec)
// compiled : 2093 ms (9555 rows/sec)
// ratio    : 3.712852365026278
// ----------------------------------
}
