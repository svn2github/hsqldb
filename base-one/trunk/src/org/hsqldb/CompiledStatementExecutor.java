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

import org.hsqldb.lib.HsqlArrayList;

/**
 * Provides execution of CompiledStatement objects. <p>
 *
 * If multiple threads access a CompiledStatementExecutor.execute()
 * concurrently, they must be synchronized externally, relative to both
 * this object's Session and the Session's Database object. Internally, this
 * is accomplished in Session.execute() by synchronizing on the Session
 * object's Database object.
 *
 * @author  boucherb@users.sourceforge.net
 * @version 1.7.2
 * @since HSQLDB 1.7.2
 */
final class CompiledStatementExecutor {

    private Session  session;
    private Database database;
    private Result   updateResult;
    private Result   emptyResult;

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
    }

    /**
     * Executes a generic CompiledStatement. Execution includes first building
     * any subquery result dependencies and clearing them after the main result
     * is built.
     *
     * @return the result of executing the statement
     * @param cs any valid CompiledStatement
     */
    Result execute(CompiledStatement cs) {

        Result result = null;

        DatabaseManager.gc();

        try {
            cs.materializeSubQueries();

            result = executeImpl(cs);
        } catch (Throwable t) {

            //t.printStackTrace();
            result = new Result(t, cs.sql);
        }

        // clear redundant data
        cs.dematerializeSubQueries();

        if (result == null) {
            result = emptyResult;
        }

        return result;
    }

    /**
     * Executes a generic CompiledStatement. Execution excludes building
     * subquery result dependencies and clearing them after the main result
     * is built.
     *
     * @param cs any valid CompiledStatement
     * @throws HsqlException if a database access error occurs
     * @return the result of executing the statement
     */
    private Result executeImpl(CompiledStatement cs) throws HsqlException {

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
                String msg = "Unknown compiled statement type: " + cs.type;

                throw Trace.error(Trace.OPERATION_NOT_SUPPORTED, msg);
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
    private Result executeCallStatement(CompiledStatement cs)
    throws HsqlException {

        Expression e = cs.expression;    // representing CALL
        Object     o = e.getValue();     // expression return value
        Result     r;
        Object[]   row;

        if (o instanceof Result) {
            return (Result) o;
        } else if (o instanceof jdbcResultSet) {
            return ((jdbcResultSet) o).rResult;
        }

        // NO:
// boucherb@users patch 1.7.x - returning values from remote data sources
//        if (o instanceof ResultSet) {
//            return Result.newResult((ResultSet)o);
//        }
//        if (o instanceof Statement) {
//            return Result.newResult(((Statement)o));
//        }
        r = Result.newSingleColumnResult("@0", e.getDataType());
        row                      = new Object[1];
        row[0]                   = o;
        r.metaData.sClassName[0] = e.getValueClassName();

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
    private Result executeDeleteStatement(CompiledStatement cs)
    throws HsqlException {

        Table       table  = cs.targetTable;
        TableFilter filter = cs.tf;
        int         count  = 0;

        if (filter.findFirst()) {
            Expression    c = cs.condition;
            HsqlArrayList del;

            del = new HsqlArrayList();

            if (c == null) {
                do {
                    del.add(filter.currentRow);
                } while (filter.next());

                count = table.delete(del, session);
            } else {
                do {
                    if (c.test()) {
                        del.add(filter.currentRow);
                    }
                } while (filter.next());

                count = table.delete(del, session);
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
    private Result executeInsertStatement(CompiledStatement cs)
    throws HsqlException {

        switch (cs.type) {

            case CompiledStatement.INSERT_SELECT : {
                return executeInsertSelectStatement(cs);
            }
            case CompiledStatement.INSERT_VALUES : {
                return executeInsertValuesStatement(cs);
            }
            default :
                String msg = "Unexpected compiled statement type: " + cs.type;

                throw Trace.error(Trace.UNEXPECTED_EXCEPTION, msg);
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
    private Result executeInsertSelectStatement(CompiledStatement cs)
    throws HsqlException {

        Table     t   = cs.targetTable;
        Select    s   = cs.select;
        int[]     ct  = t.getColumnTypes();    // column types
        Result    r   = s.getResult(session.getMaxRows());
        Record    rc  = r.rRoot;
        int[]     cm  = cs.columnMap;          // column map
        boolean[] ccl = cs.checkColumns;       // column check list
        int       len = cm.length;
        Object[]  row;
        int       count;

        session.beginNestedTransaction();

        try {
            while (rc != null) {
                row = t.getNewRow(ccl);

                for (int i = 0; i < len; i++) {
                    int j = cm[i];

                    if (ct[j] != r.metaData.colType[i]) {
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
        } catch (HsqlException he) {

            // insert failed (violation of primary key)
            session.endNestedTransaction(true);

            throw he;
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
    private Result executeInsertValuesStatement(CompiledStatement cs)
    throws HsqlException {

        Table        t    = cs.targetTable;
        Object[]     row  = t.getNewRow(cs.checkColumns);
        int[]        cm   = cs.columnMap;        // column map
        Expression[] acve = cs.columnValues;
        Expression   cve;
        int[]        ct = t.getColumnTypes();    // column types
        int          ci;                         // column index
        int          len = acve.length;

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
    private Result executeSelectStatement(CompiledStatement cs)
    throws HsqlException {
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
    private Result executeUpdateStatement(CompiledStatement cs)
    throws HsqlException {

        Table       table  = cs.targetTable;
        TableFilter filter = cs.tf;
        int         count  = 0;

        if (filter.findFirst()) {
            int[]         colmap    = cs.columnMap;    // column map
            Expression[]  colvalues = cs.columnValues;
            Expression    cve;
            Expression    c = cs.condition;            // update condition
            int           ci;                          // column index
            int           len = colvalues.length;
            Object[]      ni;
            HsqlArrayList del  = new HsqlArrayList();
            Result        ins  = new Result(ResultConstants.UPDATECOUNT);
            int           size = table.getColumnCount();

            len = colmap.length;

            int[] coltypes = table.getColumnTypes();

            if (c == null) {
                do {
                    Row row = filter.currentRow;

                    del.add(row);

                    ni = table.getNewRow();

                    System.arraycopy(row.getData(), 0, ni, 0, size);

                    for (int i = 0; i < len; i++) {
                        ci     = colmap[i];
                        ni[ci] = colvalues[i].getValue(coltypes[ci]);
                    }

                    ins.add(ni);
                } while (filter.next());
            } else {
                do {
                    if (c.test()) {
                        Row row = filter.currentRow;

                        del.add(row);

                        ni = table.getNewRow();

                        System.arraycopy(row.getData(), 0, ni, 0, size);

                        for (int i = 0; i < len; i++) {
                            ci     = colmap[i];
                            ni[ci] = colvalues[i].getValue(coltypes[ci]);
                        }

                        ins.add(ni);
                    }
                } while (filter.next());
            }

            session.beginNestedTransaction();

            try {
                count = table.update(del, ins, colmap, session);

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
}
