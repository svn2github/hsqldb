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

import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.StringUtil;
import java.sql.SQLException;
import java.sql.Types;

// fredt@users 20020130 - patch 491987 by jimbag@users - made optional
// changes applied to different parts of this method
// fredt@users 20020215 - patch 1.7.0 by fredt - quoted identifiers
// support for sql standard quoted identifiers for column and table names
// fredt@users 20020218 - patch 1.7.0 by fredt - DEFAULT keyword
// support for default values for table columns
// fredt@users 20020425 - patch 548182 by skitt@users - DEFAULT enhancement
// thertz@users 20020320 - patch 473613 by thertz - outer join condition bug
// fredt@users 20020420 - patch 523880 by leptipre@users - VIEW support
// fredt@users 20020525 - patch 559914 by fredt@users - SELECT INTO logging
// tony_lai@users 20021020 - patch 1.7.2 - improved aggregates and HAVING
// aggregate functions can now be used in expressions - HAVING supported
// kloska@users 20021030 - PATCH 1.7.2 - ON UPDATE CASCADE
// fredt@users 20021112 - patch 1.7.2 by Nitin Chauhan - use of switch
// rewrite of the majority of multiple if(){}else{} chains with switch(){}

/**
 *  Class declaration
 *
 *@version    1.7.0
 */
class Parser {

    private Database  dDatabase;
    private Tokenizer tTokenizer;
    private Session   cSession;
    private String    sTable;
    private String    sToken;
    private Object    oData;
    private int       iType;
    private int       iToken;

    /**
     *  Constructor declaration
     *
     * @param  db
     * @param  t
     * @param  session
     */
    Parser(Database db, Tokenizer t, Session session) {

        dDatabase  = db;
        tTokenizer = t;
        cSession   = session;
    }

    /**
     *  Method declaration
     *
     * @return
     * @throws  SQLException
     */
    Result processSelect() throws SQLException {

        Select select = parseSelect();

        if (select.sIntoTable == null) {
            return select.getResult(cSession.getMaxRows());
        } else {

            // session level user rights
            cSession.checkReadWrite();

// fredt@users 20020215 - patch 497872 by Nitin Chauhan
// to require column labels in SELECT INTO TABLE
            for (int i = 0; i < select.eColumn.length; i++) {
                if (select.eColumn[i].getAlias().length() == 0) {
                    throw Trace.error(Trace.LABEL_REQUIRED);
                }
            }

            if (dDatabase.findUserTable(select.sIntoTable.name, cSession)
                    != null) {
                throw Trace.error(Trace.TABLE_ALREADY_EXISTS,
                                  select.sIntoTable.name);
            }

            Result r = select.getResult(0);

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
            Table t;

            if (select.intoType == Table.TEXT_TABLE) {
                t = new TextTable(dDatabase, select.sIntoTable,
                                  select.intoType, cSession);
            } else {
                t = new Table(dDatabase, select.sIntoTable, select.intoType,
                              cSession);
            }

            t.addColumns(r);
            t.createPrimaryKey();
            dDatabase.linkTable(t);

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
            if (select.intoType == Table.TEXT_TABLE) {
                try {

                    // Use default lowercase name "<table>.csv" (with invalid
                    // char's converted to underscores):
                    String src =
                        StringUtil.toLowerSubset(select.sIntoTable.name, '_')
                        + ".csv";

                    t.setDataSource(src, false, cSession);
                    logTableDDL(t);
                    t.insertNoCheck(r, cSession);
                } catch (SQLException e) {
                    dDatabase.dropTable(select.sIntoTable.name, false, false,
                                        cSession);

                    throw (e);
                }
            } else {
                logTableDDL(t);

                // SELECT .. INTO can't fail because of constraint violation
                t.insertNoCheck(r, cSession);
            }

            int i = r.getSize();

            r              = new Result();
            r.iUpdateCount = i;

            return r;
        }
    }

    /**
     *  Logs the DDL for a table created with INTO.
     *  Uses three dummy arguments for getTableDDL() as the new table has no
     *  FK constraints.
     *
     * @throws  SQLException
     */
    void logTableDDL(Table t) throws SQLException {

        if (t.isTemp()) {
            return;
        }

        StringBuffer tableDDL = new StringBuffer();

        DatabaseScript.getTableDDL(dDatabase, t, 0, null, null, tableDDL);

        String sourceDDL = DatabaseScript.getDataSource(t);

        dDatabase.logger.writeToLog(cSession, tableDDL.toString());

        if (sourceDDL != null) {
            dDatabase.logger.writeToLog(cSession, sourceDDL);
        }
    }

    /**
     *  Method declaration
     *
     * @return
     * @throws  SQLException
     */
    Result processCall() throws SQLException {

        Expression e = parseExpression();

        e.resolve(null);

        int    type = e.getDataType();
        Object o    = e.getValue();
        Result r    = new Result(1);

        r.sTable[0]  = "";
        r.colType[0] = type;
        r.sLabel[0]  = "";
        r.sName[0]   = "";

        Object row[] = new Object[1];

        row[0] = o;

        r.add(row);

        return r;
    }

    /**
     *  Method declaration
     *
     * @return
     * @throws  SQLException
     */
    Result processUpdate() throws SQLException {

        String token = tTokenizer.getString();
        Table  table = dDatabase.getTable(token, cSession);

        checkTableWriteAccess(table, UserManager.UPDATE);

        TableFilter filter = new TableFilter(table, null, false);

        tTokenizer.getThis("SET");

        HsqlArrayList vColumn = new HsqlArrayList();
        HsqlArrayList eColumn = new HsqlArrayList();
        int           len     = 0;

        token = null;

        do {
            len++;

            int i = table.getColumnNr(tTokenizer.getString());

            vColumn.add(new Integer(i));
            tTokenizer.getThis("=");

            Expression e = parseExpression();

            e.resolve(filter);
            eColumn.add(e);

            token = tTokenizer.getString();
        } while (token.equals(","));

        Expression eCondition = null;

        if (token.equals("WHERE")) {
            eCondition = parseExpression();

            eCondition.resolve(filter);
            filter.setCondition(eCondition);
        } else {
            tTokenizer.back();
        }

        Expression exp[] = new Expression[len];

        eColumn.toArray(exp);

        int col[]  = new int[len];
        int type[] = new int[len];

        //int csize[] = new int[len];
        for (int i = 0; i < len; i++) {
            col[i] = ((Integer) vColumn.get(i)).intValue();

            Column column = table.getColumn(col[i]);

            type[i] = column.getType();

            //csize[i] = column.getSize();
        }

        int count = 0;

        if (filter.findFirst()) {
            Result del  = new Result();    // don't need column count and so on
            Result ins  = new Result();
            int    size = table.getColumnCount();

            do {
                if (eCondition == null || eCondition.test()) {
                    Object nd[] = filter.oCurrentData;

                    del.add(nd);

                    Object ni[] = table.getNewRow();

                    System.arraycopy(nd, 0, ni, 0, size);

                    for (int i = 0; i < len; i++) {
                        ni[col[i]] = exp[i].getValue(type[i]);
                    }

                    ins.add(ni);
                }
            } while (filter.next());

            cSession.beginNestedTransaction();

            try {
                count = table.update(del, ins, col, cSession);

                cSession.endNestedTransaction(false);
            } catch (SQLException e) {

                // update failed (constraint violation)
                cSession.endNestedTransaction(true);

                throw e;
            }
        }

        Result r = new Result();

        r.iUpdateCount = count;

        return r;
    }

    void checkTableWriteAccess(Table table,
                               int userRight) throws SQLException {

        // session level user rights
        cSession.checkReadWrite();

        // object level user rights
        cSession.check(table.getName(), userRight);

        // object type
        if (table.isView()) {
            throw Trace.error(Trace.NOT_A_TABLE, table.getName().name);
        }

        // object readonly
        table.checkDataReadOnly();
    }

    /**
     *  Method declaration
     *
     * @return
     * @throws  SQLException
     */
    Result processDelete() throws SQLException {

        tTokenizer.getThis("FROM");

        String token = tTokenizer.getString();
        Table  table = dDatabase.getTable(token, cSession);

        checkTableWriteAccess(table, UserManager.DELETE);

        TableFilter filter = new TableFilter(table, null, false);

        token = tTokenizer.getString();

        Expression eCondition = null;

        if (token.equals("WHERE")) {
            eCondition = parseExpression();

            eCondition.resolve(filter);
            filter.setCondition(eCondition);
        } else {
            tTokenizer.back();
        }

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
        Trace.check(!table.isDataReadOnly(), Trace.DATA_IS_READONLY);

        int count = 0;

        if (filter.findFirst()) {
            Result del = new Result();    // don't need column count and so on

            do {
                if (eCondition == null || eCondition.test()) {
                    del.add(filter.oCurrentData);
                }
            } while (filter.next());

            count = table.delete(del, cSession);
        }

        Result r = new Result();

        r.iUpdateCount = count;

        return r;
    }

    /**
     *  Method declaration
     *
     * @return
     * @throws  SQLException
     */
    Result processInsert() throws SQLException {

        tTokenizer.getThis("INTO");

        String token = tTokenizer.getString();
        Table  t     = dDatabase.getTable(token, cSession);

        checkTableWriteAccess(t, UserManager.INSERT);

        token = tTokenizer.getString();

        HsqlArrayList vcolumns       = null;
        boolean       checkcolumns[] = null;

        if (token.equals("(")) {
            vcolumns     = new HsqlArrayList();
            checkcolumns = t.getNewColumnCheckList();

            int i = 0;

            while (true) {
                vcolumns.add(tTokenizer.getString());

                i++;

                token = tTokenizer.getString();

                if (token.equals(",")) {
                    continue;
                }

                if (token.equals(")")) {
                    break;
                }

                throw Trace.error(Trace.UNEXPECTED_TOKEN, token);
            }

            int len = vcolumns.size();

            for (i = 0; i < len; i++) {
                int colindex = t.getColumnNr((String) vcolumns.get(i));

                checkcolumns[colindex] = true;
            }

            token = tTokenizer.getString();
        }

        int count = 0;
        int len;

        if (vcolumns == null) {
            len = t.getColumnCount();
        } else {
            len = vcolumns.size();
        }

// fredt@users 20020218 - patch 1.7.0 by fredt - DEFAULT keyword
        if (token.equals("VALUES")) {
            tTokenizer.getThis("(");

            Object  row[]    = t.getNewRow(checkcolumns);
            boolean enclosed = false;
            int     i        = 0;

            for (; i < len; i++) {
                int colindex;

                if (vcolumns == null) {
                    colindex = i;
                } else {
                    colindex = t.getColumnNr((String) vcolumns.get(i));
                }

                Column column = t.getColumn(colindex);

                row[colindex] = getValue(column.getType());
                token         = tTokenizer.getString();

                if (token.equals(",")) {
                    continue;
                }

                if (token.equals(")")) {
                    enclosed = true;

                    break;
                }

                throw Trace.error(Trace.UNEXPECTED_TOKEN, token);
            }

            if (!enclosed || i != len - 1) {
                throw Trace.error(Trace.COLUMN_COUNT_DOES_NOT_MATCH);
            }

            t.insert(row, cSession);

            count = 1;
        } else if (token.equals("SELECT")) {
            Result result = processSelect();
            Record r      = result.rRoot;

            Trace.check(len == result.getColumnCount(),
                        Trace.COLUMN_COUNT_DOES_NOT_MATCH);

            int col[]  = new int[len];
            int type[] = new int[len];

            for (int i = 0; i < len; i++) {
                int j;

                if (vcolumns == null) {
                    j = i;
                } else {
                    j = t.getColumnNr((String) vcolumns.get(i));
                }

                col[i]  = j;
                type[i] = t.getColumn(j).getType();
            }

            cSession.beginNestedTransaction();

            try {
                while (r != null) {
                    Object row[] = t.getNewRow(checkcolumns);

                    for (int i = 0; i < len; i++) {
                        if (type[i] != result.colType[i]) {
                            row[col[i]] = Column.convertObject(r.data[i],
                                                               type[i]);
                        } else {
                            row[col[i]] = r.data[i];
                        }
                    }

                    r.data = row;
                    r      = r.next;
                }

                count = t.insert(result, cSession);

                cSession.endNestedTransaction(false);
            } catch (SQLException e) {

                // insert failed (violation of primary key)
                cSession.endNestedTransaction(true);

                throw e;
            }
        } else {
            throw Trace.error(Trace.UNEXPECTED_TOKEN, token);
        }

        Result r = new Result();

        r.iUpdateCount = count;

        return r;
    }

    /**
     *  Method declaration
     *
     * @return
     * @throws  SQLException
     */
    Select parseSelect() throws SQLException {

        Select select = new Select();

// fredt@users 20011010 - patch 471710 by fredt - LIMIT rewritten
// SELECT LIMIT n m DISTINCT ... queries and error message
// "SELECT LIMIT n m ..." creates the result set for the SELECT statement then
// discards the first n rows and returns m rows of the remaining result set
// "SELECT LIMIT 0 m" is equivalent to "SELECT TOP m" or "SELECT FIRST m"
// in other RDBMS's
// "SELECT LIMIT n 0" discards the first n rows and returns the remaining rows
// fredt@users 20020225 - patch 456679 by hiep256 - TOP keyword
        String token = tTokenizer.getString();

        if (token.equals("LIMIT")) {
            String limStart = tTokenizer.getString();
            String limEnd   = tTokenizer.getString();

            try {
                select.limitStart = new Integer(limStart).intValue();
                select.limitCount = new Integer(limEnd).intValue();
            } catch (NumberFormatException ex) {

                // todo: add appropriate error type and message to Trace.java
                throw Trace.error(Trace.WRONG_DATA_TYPE, "LIMIT n m");
            }

            token = tTokenizer.getString();
        } else if (token.equals("TOP")) {
            String limEnd = tTokenizer.getString();

            try {
                select.limitStart = 0;
                select.limitCount = new Integer(limEnd).intValue();
            } catch (NumberFormatException ex) {

                // todo: add appropriate error type and message to Trace.java
                throw Trace.error(Trace.WRONG_DATA_TYPE, "TOP m");
            }

            token = tTokenizer.getString();
        }

        if (token.equals("DISTINCT")) {
            select.isDistinctSelect = true;
        } else {
            tTokenizer.back();
        }

        // parse column list
        HsqlArrayList vcolumn = new HsqlArrayList();

        do {
            Expression e = parseExpression();

            token = tTokenizer.getString();

            if (token.equals("AS")) {
                e.setAlias(tTokenizer.getName(),
                           tTokenizer.wasQuotedIdentifier());

                token = tTokenizer.getString();
            } else if (tTokenizer.wasName()) {
                e.setAlias(token, tTokenizer.wasQuotedIdentifier());

                token = tTokenizer.getString();
            }

            vcolumn.add(e);
        } while (token.equals(","));

        if (token.equals("INTO")) {

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
            token = tTokenizer.getString();

            if (token.equals("CACHED")) {
                select.intoType = Table.CACHED_TABLE;
                select.sIntoTable =
                    new HsqlName(tTokenizer.getString(),
                                 tTokenizer.wasQuotedIdentifier());
            } else if (token.equals("TEMP")) {
                select.intoType = Table.TEMP_TABLE;
                select.sIntoTable =
                    new HsqlName(tTokenizer.getString(),
                                 tTokenizer.wasQuotedIdentifier());
            } else if (token.equals("TEXT")) {
                select.intoType = Table.TEXT_TABLE;
                select.sIntoTable =
                    new HsqlName(tTokenizer.getString(),
                                 tTokenizer.wasQuotedIdentifier());
            } else {
                select.sIntoTable =
                    new HsqlName(token, tTokenizer.wasQuotedIdentifier());
            }

            token = tTokenizer.getString();
        }

        if (!token.equals("FROM")) {
            throw Trace.error(Trace.UNEXPECTED_TOKEN, token);
        }

        Expression condition = null;

        // parse table list
        HsqlArrayList vfilter = new HsqlArrayList();

        vfilter.add(parseTableFilter(false));

        while (true) {
            token = tTokenizer.getString();

            if (token.equals("LEFT")) {
                token = tTokenizer.getString();

                if (token.equals("OUTER")) {
                    token = tTokenizer.getString();
                }

                Trace.check(token.equals("JOIN"), Trace.UNEXPECTED_TOKEN,
                            token);
                vfilter.add(parseTableFilter(true));
                tTokenizer.getThis("ON");

// thertz@users 20020320 - patch 473613 - outer join condition bug
// we now call parseJoinCondition() because a limitation of HSQLDB results
// in incorrect results for OUTER JOINS that have anything other than
// tableA.colA=tableB.colB type expressions
                //condition = addCondition(condition, parseExpression());
                condition = addCondition(condition,
                                         parseOuterJoinCondition());
            } else if (token.equals("INNER")) {
                tTokenizer.getThis("JOIN");
                vfilter.add(parseTableFilter(false));
                tTokenizer.getThis("ON");

                condition = addCondition(condition, parseExpression());
            } else if (token.equals(",")) {
                vfilter.add(parseTableFilter(false));
            } else {
                break;
            }
        }

        tTokenizer.back();

        int         len      = vfilter.size();
        TableFilter filter[] = new TableFilter[len];

        vfilter.toArray(filter);

        select.tFilter = filter;

        // expand [table.]* columns
        len = vcolumn.size();

        for (int i = 0; i < len; i++) {
            Expression e = (Expression) (vcolumn.get(i));

            if (e.getType() == Expression.ASTERIX) {
                int    current = i;
                Table  table   = null;
                String n       = e.getTableName();

                for (int t = 0; t < filter.length; t++) {
                    TableFilter f = filter[t];

                    e.resolve(f);

                    if (n != null &&!n.equals(f.getName())) {
                        continue;
                    }

                    table = f.getTable();

                    int col = table.getColumnCount();

                    for (int c = 0; c < col; c++) {
                        Expression ins = new Expression(
                            f.getName(), table.getColumn(c).columnName.name,
                            table.getColumn(c).columnName.isNameQuoted);

                        vcolumn.add(current++, ins);

                        // now there is one element more to parse
                        len++;
                    }
                }

                Trace.check(table != null, Trace.TABLE_NOT_FOUND, n);

                // minus the asterix element
                len--;

                vcolumn.remove(current);
            } else if (e.getType() == Expression.COLUMN) {
                if (e.getTableName() == null) {
                    for (int filterIndex = 0; filterIndex < filter.length;
                            filterIndex++) {
                        e.resolve(filter[filterIndex]);
                    }
                }
            }
        }

        select.iResultLen = len;

        // where
        token = tTokenizer.getString();

        if (token.equals("WHERE")) {
            condition = addCondition(condition, parseExpression());
            token     = tTokenizer.getString();
        }

        select.eCondition = condition;

// fredt@users 20020215 - patch 1.7.0 by fredt
// to support GROUP BY with more than one column
        if (token.equals("GROUP")) {
            tTokenizer.getThis("BY");

            len = 0;

            do {
                Expression e = parseExpression();

                // tony_lai@users having support:
                // "group by" does not allow refering to other columns alias.
                //e = doOrderGroup(e, vcolumn);
                vcolumn.add(e);

                token = tTokenizer.getString();

                len++;
            } while (token.equals(","));

            select.iGroupLen = len;
        }

        // tony_lai@users - having support
        if (token.equals("HAVING")) {
            select.iHavingIndex    = vcolumn.size();
            select.havingCondition = parseExpression();
            token                  = tTokenizer.getString();

            vcolumn.add(select.havingCondition);
        }

        if (token.equals("ORDER")) {
            tTokenizer.getThis("BY");

            len = 0;

            do {
                Expression e = parseExpression();

                e     = checkOrderByColumns(e, vcolumn);
                token = tTokenizer.getString();

                if (token.equals("DESC")) {
                    e.setDescending();

                    token = tTokenizer.getString();
                } else if (token.equals("ASC")) {
                    token = tTokenizer.getString();
                }

                vcolumn.add(e);

                len++;
            } while (token.equals(","));

            select.iOrderLen = len;
        }

        len            = vcolumn.size();
        select.eColumn = new Expression[len];

        vcolumn.toArray(select.eColumn);

        if (token.equals("UNION")) {
            token = tTokenizer.getString();

            if (token.equals("ALL")) {
                select.iUnionType = Select.UNIONALL;
            } else {
                select.iUnionType = Select.UNION;

                tTokenizer.back();
            }

            tTokenizer.getThis("SELECT");

            select.sUnion = parseSelect();
        } else if (token.equals("INTERSECT")) {
            tTokenizer.getThis("SELECT");

            select.iUnionType = Select.INTERSECT;
            select.sUnion     = parseSelect();
        } else if (token.equals("EXCEPT") || token.equals("MINUS")) {
            tTokenizer.getThis("SELECT");

            select.iUnionType = Select.EXCEPT;
            select.sUnion     = parseSelect();
        } else {
            tTokenizer.back();
        }

        return select;
    }

    /**
     * Checks Order By columns, and substitutes order by columns that is
     * refering to select columns by alias or column index.
     *
     * @param  e                          Description of the Parameter
     * @param  vcolumn                    Description of the Parameter
     * @return                            Description of the Return Value
     * @exception  java.sql.SQLException  Description of the Exception
     */
    private Expression checkOrderByColumns(Expression e,
                                           HsqlArrayList vcolumn)
                                           throws java.sql.SQLException {

        if (e.getType() == Expression.VALUE) {

            // order by 1,2,3
            if (e.getDataType() == Types.INTEGER) {
                int i = ((Integer) e.getValue()).intValue();

                e = (Expression) vcolumn.get(i - 1);
            }
        } else if (e.getType() == Expression.COLUMN
                   && e.getTableName() == null) {

            // this could be an alias column
            String s = e.getColumnName();

            for (int i = 0, size = vcolumn.size(); i < size; i++) {
                Expression ec = (Expression) vcolumn.get(i);

                // We can only substitute alias defined in the select clause,
                // since there may be more that one result column with the
                // same column name.  For example:
                //   "select 500-column1, column1 from table 1 order by column2"
                if (s.equals(ec.getDefinedAlias())) {
                    e = ec;

                    break;
                }
            }
        }

        return e;
    }

    /**
     *  Method declaration
     *
     * @param  outerjoin
     * @return
     * @throws  SQLException
     */
    private TableFilter parseTableFilter(boolean outerjoin)
    throws SQLException {

        String token = tTokenizer.getString();
        Table  t     = null;

        if (token.equals("(")) {
            tTokenizer.getThis("SELECT");

            Select s = parseSelect();
            Result r = s.getResult(0);

            // it's not a problem that this table has not a unique name
            t = new Table(dDatabase, new HsqlName("SYSTEM_SUBQUERY", false),
                          Table.SYSTEM_TABLE, null);

            tTokenizer.getThis(")");
            t.addColumns(r);
            t.createPrimaryKey();

            // subquery creation can't fail because constraint violation
            t.insertNoCheck(r, cSession);
        } else {
            t = dDatabase.getTable(token, cSession);

            cSession.check(t.getName(), UserManager.SELECT);

// fredt@users 20020420 - patch523880 by leptipre@users - VIEW support
            if (t.isView()) {
                String Viewname    = token;
                int    CurrentPos  = tTokenizer.getPosition();
                int    sLength     = tTokenizer.getLength();
                int    TokenLength = token.length();
                int    NewCurPos   = CurrentPos;

                token = tTokenizer.getString();

                if (token.equals("AS")) {
                    Viewname  = tTokenizer.getName();
                    NewCurPos = tTokenizer.getPosition();
                } else if (tTokenizer.wasName()) {
                    Viewname  = token;
                    NewCurPos = tTokenizer.getPosition();
                } else {
                    tTokenizer.back();
                }

                String sLeft = tTokenizer.getPart(0, CurrentPos
                                                  - TokenLength);
                String       sRight = tTokenizer.getPart(NewCurPos, sLength);
                View         v         = (View) t;
                String       sView     = v.getStatement();
                StringBuffer sFromView = new StringBuffer(128);

                sFromView.append(sLeft);
                sFromView.append('(');
                sFromView.append(sView);
                sFromView.append(") ");
                sFromView.append(Viewname);
                sFromView.append(sRight);
                tTokenizer.setString(sFromView.toString(),
                                     CurrentPos - TokenLength + 1);
                tTokenizer.getThis("SELECT");

                Select s = parseSelect();
                Result r = s.getResult(0);

                // it's not a problem that this table has not a unique name
                t = new Table(dDatabase,
                              new HsqlName("SYSTEM_SUBQUERY", false),
                              Table.SYSTEM_TABLE, null);

                tTokenizer.getThis(")");
                t.addColumns(r);
                t.createPrimaryKey();

                // subquery creation can't fail because constraint violation
                t.insertNoCheck(r, cSession);
            }
        }

        String sAlias = null;

        token = tTokenizer.getString();

        if (token.equals("AS")) {
            sAlias = tTokenizer.getName();
        } else if (tTokenizer.wasName()) {
            sAlias = token;
        } else {
            tTokenizer.back();
        }

        return new TableFilter(t, sAlias, outerjoin);
    }

    /**
     *  Method declaration
     *
     * @param  e1
     * @param  e2
     * @return
     */
    private Expression addCondition(Expression e1, Expression e2) {

        if (e1 == null) {
            return e2;
        } else if (e2 == null) {
            return e1;
        } else {
            return new Expression(Expression.AND, e1, e2);
        }
    }

    /**
     *  Method declaration
     *
     * @param  type
     * @return
     * @throws  SQLException
     */
    private Object getValue(int type) throws SQLException {

        Expression r = parseExpression();

        r.resolve(null);

        return r.getValue(type);
    }

// thertz@users 20020320 - patch 473613 - outer join condition bug

    /**
     * parses the expression that can be used behind a
     * [..] JOIN table ON (exp).
     * This expression should always be in the form "tab.col=tab2.col"
     * with optional brackets (to support automated query tools).<br>
     * this method is used from the parseSelect method
     *
     * @return the expression
     * @throws  SQLException if the syntax was not correct
     */
    private Expression parseOuterJoinCondition() throws SQLException {

        boolean parens = false;

        read();

        if (iToken == Expression.OPEN) {
            parens = true;

            read();
        }

        Trace.check(iToken == Expression.COLUMN, Trace.OUTER_JOIN_CONDITION);

        Expression left = new Expression(sTable, sToken);

        read();
        Trace.check(iToken == Expression.EQUAL, Trace.OUTER_JOIN_CONDITION);
        read();
        Trace.check(iToken == Expression.COLUMN, Trace.OUTER_JOIN_CONDITION);

        Expression right = new Expression(sTable, sToken);

        if (parens) {
            read();
            Trace.check(iToken == Expression.CLOSE,
                        Trace.OUTER_JOIN_CONDITION);
        }

        return new Expression(Expression.EQUAL, left, right);
    }

    /**
     *  Method declaration
     *
     * @return
     * @throws  SQLException
     */
    private Expression parseExpression() throws SQLException {

        read();

        Expression r = readOr();

        tTokenizer.back();

        return r;
    }

    private Expression readAggregate() throws SQLException {

        boolean distinct = false;
        int     type     = iToken;

        read();

        if (tTokenizer.getString().equals("DISTINCT")) {
            distinct = true;
        } else {
            tTokenizer.back();
        }

        readThis(Expression.OPEN);

        Expression s = readOr();

        readThis(Expression.CLOSE);

        Expression aggregateExp = new Expression(type, s, null);

        aggregateExp.setDistinctAggregate(distinct);

        return aggregateExp;
    }

    /**
     *  Method declaration
     *
     * @return
     * @throws  SQLException
     */
    private Expression readOr() throws SQLException {

        Expression r = readAnd();

        while (iToken == Expression.OR) {
            int        type = iToken;
            Expression a    = r;

            read();

            r = new Expression(type, a, readAnd());
        }

        return r;
    }

    /**
     *  Method declaration
     *
     * @return
     * @throws  SQLException
     */
    private Expression readAnd() throws SQLException {

        Expression r = readCondition();

        while (iToken == Expression.AND) {
            int        type = iToken;
            Expression a    = r;

            read();

            r = new Expression(type, a, readCondition());
        }

        return r;
    }

    /**
     *  Method declaration
     *
     * @return
     * @throws  SQLException
     */
    private Expression readCondition() throws SQLException {

        switch (iToken) {

            case Expression.NOT : {
                int type = iToken;

                read();

                return new Expression(type, readCondition(), null);
            }
            case Expression.EXISTS : {
                int type = iToken;

                read();
                readThis(Expression.OPEN);
                Trace.check(iToken == Expression.SELECT,
                            Trace.UNEXPECTED_TOKEN);

                Expression s = new Expression(parseSelect());

                read();
                readThis(Expression.CLOSE);

                return new Expression(type, s, null);
            }
            default : {
                Expression a   = readConcat();
                boolean    not = false;

                if (iToken == Expression.NOT) {
                    not = true;

                    read();
                }

                switch (iToken) {

                    case Expression.LIKE : {
                        read();

                        Expression b      = readConcat();
                        char       escape = 0;

                        if (sToken.equals("ESCAPE")) {
                            read();

                            Expression c = readTerm();

                            Trace.check(c.getType() == Expression.VALUE,
                                        Trace.INVALID_ESCAPE);

                            String s = (String) c.getValue(Types.VARCHAR);

                            if (s == null || s.length() < 1) {
                                throw Trace.error(Trace.INVALID_ESCAPE, s);
                            }

                            escape = s.charAt(0);
                        }

                        a = new Expression(Expression.LIKE, a, b);

                        a.setLikeEscape(escape);

                        break;
                    }
                    case Expression.BETWEEN : {
                        read();

                        Expression l = new Expression(Expression.BIGGER_EQUAL,
                                                      a, readConcat());

                        readThis(Expression.AND);

                        Expression h =
                            new Expression(Expression.SMALLER_EQUAL, a,
                                           readConcat());

                        a = new Expression(Expression.AND, l, h);

                        break;
                    }
                    case Expression.IN : {
                        int type = iToken;

                        read();
                        readThis(Expression.OPEN);

                        Expression b = null;

                        if (iToken == Expression.SELECT) {
                            b = new Expression(parseSelect());

                            read();
                        } else {
                            tTokenizer.back();

                            HsqlArrayList v = new HsqlArrayList();

                            while (true) {
                                v.add(getValue(Types.VARCHAR));
                                read();

                                if (iToken != Expression.COMMA) {
                                    break;
                                }
                            }

                            b = new Expression(v);
                        }

                        readThis(Expression.CLOSE);

                        a = new Expression(type, a, b);

                        break;
                    }
                    default : {
                        Trace.check(!not, Trace.UNEXPECTED_TOKEN);

                        if (Expression.isCompare(iToken)) {
                            int type = iToken;

                            read();

                            return new Expression(type, a, readConcat());
                        }

                        return a;
                    }
                }

                if (not) {
                    a = new Expression(Expression.NOT, a, null);
                }

                return a;
            }
        }
    }

    /**
     *  Method declaration
     *
     * @param  type
     * @throws  SQLException
     */
    private void readThis(int type) throws SQLException {
        Trace.check(iToken == type, Trace.UNEXPECTED_TOKEN);
        read();
    }

    /**
     *  Method declaration
     *
     * @return
     * @throws  SQLException
     */
    private Expression readConcat() throws SQLException {

        Expression r = readSum();

        while (iToken == Expression.STRINGCONCAT) {
            int        type = Expression.CONCAT;
            Expression a    = r;

            read();

            r = new Expression(type, a, readSum());
        }

        return r;
    }

    /**
     *  Method declaration
     *
     * @return
     * @throws  SQLException
     */
    private Expression readSum() throws SQLException {

        Expression r = readFactor();

        while (true) {
            int type;

            if (iToken == Expression.PLUS) {
                type = Expression.ADD;
            } else if (iToken == Expression.NEGATE) {
                type = Expression.SUBTRACT;
            } else {
                break;
            }

            Expression a = r;

            read();

            r = new Expression(type, a, readFactor());
        }

        return r;
    }

    /**
     *  Method declaration
     *
     * @return
     * @throws  SQLException
     */
    private Expression readFactor() throws SQLException {

        Expression r = readTerm();

        while (iToken == Expression.MULTIPLY || iToken == Expression.DIVIDE) {
            int        type = iToken;
            Expression a    = r;

            read();

            r = new Expression(type, a, readTerm());
        }

        return r;
    }

    /**
     *  Method declaration
     *
     * @return
     * @throws  SQLException
     */
    private Expression readTerm() throws SQLException {

        Expression r = null;

        switch (iToken) {

            case Expression.COLUMN : {
                String name = sToken;

                r = new Expression(sTable, sToken);

                read();

                if (iToken == Expression.OPEN) {
                    Function f = new Function(dDatabase.getAlias(name),
                                              cSession);
                    int len = f.getArgCount();
                    int i   = 0;

                    read();

                    if (iToken != Expression.CLOSE) {
                        while (true) {
                            f.setArgument(i++, readOr());

                            if (iToken != Expression.COMMA) {
                                break;
                            }

                            read();
                        }
                    }

                    readThis(Expression.CLOSE);

                    r = new Expression(f);
                }

                break;
            }
            case Expression.NEGATE : {
                int type = iToken;

                read();

                r = new Expression(type, readTerm(), null);

                break;
            }
            case Expression.PLUS : {
                read();

                r = readTerm();

                break;
            }
            case Expression.OPEN : {
                read();

                r = readOr();

                if (iToken != Expression.CLOSE) {
                    throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
                }

                read();

                break;
            }
            case Expression.VALUE : {
                r = new Expression(iType, oData);

                read();

                break;
            }
            case Expression.SELECT : {
                r = new Expression(parseSelect());

                read();

                break;
            }
            case Expression.MULTIPLY : {
                r = new Expression(sTable, null);

                read();

                break;
            }
            case Expression.IFNULL :
            case Expression.CONCAT : {
                int type = iToken;

                read();
                readThis(Expression.OPEN);

                r = readOr();

                readThis(Expression.COMMA);

                r = new Expression(type, r, readOr());

                readThis(Expression.CLOSE);

                break;
            }
            case Expression.CASEWHEN : {
                int type = iToken;

                read();
                readThis(Expression.OPEN);

                r = readOr();

                readThis(Expression.COMMA);

                Expression thenelse = readOr();

                readThis(Expression.COMMA);

                // thenelse part is never evaluated; only init
                thenelse = new Expression(type, thenelse, readOr());
                r        = new Expression(type, r, thenelse);

                readThis(Expression.CLOSE);

                break;
            }
            case Expression.CONVERT : {
                int type = iToken;

                read();
                readThis(Expression.OPEN);

                r = readOr();

                readThis(Expression.COMMA);

                int t = Column.getTypeNr(sToken);

                r = new Expression(type, r, null);

                r.setDataType(t);
                read();
                readThis(Expression.CLOSE);

                break;
            }
            case Expression.CAST : {
                read();
                readThis(Expression.OPEN);

                r = readOr();

                Trace.check(sToken.equals("AS"), Trace.UNEXPECTED_TOKEN,
                            sToken);
                read();

                int t = Column.getTypeNr(sToken);

                r = new Expression(Expression.CONVERT, r, null);

                r.setDataType(t);
                read();
                readThis(Expression.CLOSE);

                break;
            }
            default : {
                if (Expression.isAggregate(iToken)) {
                    r = readAggregate();
                } else {
                    throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
                }

                break;
            }
        }

        return r;
    }

    /**
     *  Method declaration
     *
     * @throws  SQLException
     */

// fredt@users 20020130 - patch 497872 by Nitin Chauhan
// reordering for speed
    private void read() throws SQLException {

        sToken = tTokenizer.getString();

        if (tTokenizer.wasValue()) {
            iToken = Expression.VALUE;
            oData  = tTokenizer.getAsValue();
            iType  = tTokenizer.getType();
        } else if (tTokenizer.wasName()) {
            iToken = Expression.COLUMN;
            sTable = null;
        } else if (tTokenizer.wasLongName()) {
            sTable = tTokenizer.getLongNameFirst();
            sToken = tTokenizer.getLongNameLast();

            if (sToken.equals("*")) {
                iToken = Expression.MULTIPLY;
            } else {
                iToken = Expression.COLUMN;
            }
        } else if (sToken.length() == 0) {
            iToken = Expression.END;
        } else {
            Integer n = (Integer) tokenTable.get(sToken);

            if (n != null) {
                iToken = n.intValue();
            } else {
                iToken = Expression.END;
            }

            switch (iToken) {

                case Expression.COMMA :
                case Expression.EQUAL :
                case Expression.NOT_EQUAL :
                case Expression.SMALLER :
                case Expression.BIGGER :
                case Expression.SMALLER_EQUAL :
                case Expression.BIGGER_EQUAL :
                case Expression.AND :
                case Expression.OR :
                case Expression.NOT :
                case Expression.IN :
                case Expression.EXISTS :
                case Expression.BETWEEN :
                case Expression.PLUS :
                case Expression.NEGATE :
                case Expression.DIVIDE :
                case Expression.STRINGCONCAT :
                case Expression.OPEN :
                case Expression.CLOSE :
                case Expression.SELECT :
                case Expression.LIKE :
                case Expression.COUNT :
                case Expression.SUM :
                case Expression.MIN :
                case Expression.MAX :
                case Expression.AVG :
                case Expression.IFNULL :
                case Expression.CONVERT :
                case Expression.CAST :
                case Expression.CASEWHEN :
                case Expression.CONCAT :
                case Expression.END :
                    break;            // nothing else required, iToken initialized properly

                case Expression.MULTIPLY :
                    sTable = null;    // in case of ASTERIX
                    break;

                case Expression.IS :
                    sToken = tTokenizer.getString();

                    if (sToken.equals("NOT")) {
                        iToken = Expression.NOT_EQUAL;
                    } else {
                        iToken = Expression.EQUAL;

                        tTokenizer.back();
                    }
                    break;

                default :
                    iToken = Expression.END;
            }
        }
    }

    private static java.util.Hashtable tokenTable =
        new java.util.Hashtable(37);

    static {
        tokenTable.put(",", new Integer(Expression.COMMA));
        tokenTable.put("=", new Integer(Expression.EQUAL));
        tokenTable.put("!=", new Integer(Expression.NOT_EQUAL));
        tokenTable.put("<>", new Integer(Expression.NOT_EQUAL));
        tokenTable.put("<", new Integer(Expression.SMALLER));
        tokenTable.put(">", new Integer(Expression.BIGGER));
        tokenTable.put("<=", new Integer(Expression.SMALLER_EQUAL));
        tokenTable.put(">=", new Integer(Expression.BIGGER_EQUAL));
        tokenTable.put("AND", new Integer(Expression.AND));
        tokenTable.put("NOT", new Integer(Expression.NOT));
        tokenTable.put("OR", new Integer(Expression.OR));
        tokenTable.put("IN", new Integer(Expression.IN));
        tokenTable.put("EXISTS", new Integer(Expression.EXISTS));
        tokenTable.put("BETWEEN", new Integer(Expression.BETWEEN));
        tokenTable.put("+", new Integer(Expression.PLUS));
        tokenTable.put("-", new Integer(Expression.NEGATE));
        tokenTable.put("*", new Integer(Expression.MULTIPLY));
        tokenTable.put("/", new Integer(Expression.DIVIDE));
        tokenTable.put("||", new Integer(Expression.STRINGCONCAT));
        tokenTable.put("(", new Integer(Expression.OPEN));
        tokenTable.put(")", new Integer(Expression.CLOSE));
        tokenTable.put("SELECT", new Integer(Expression.SELECT));
        tokenTable.put("LIKE", new Integer(Expression.LIKE));
        tokenTable.put("COUNT", new Integer(Expression.COUNT));
        tokenTable.put("SUM", new Integer(Expression.SUM));
        tokenTable.put("MIN", new Integer(Expression.MIN));
        tokenTable.put("MAX", new Integer(Expression.MAX));
        tokenTable.put("AVG", new Integer(Expression.AVG));
        tokenTable.put("IFNULL", new Integer(Expression.IFNULL));
        tokenTable.put("CONVERT", new Integer(Expression.CONVERT));
        tokenTable.put("CAST", new Integer(Expression.CAST));
        tokenTable.put("CASEWHEN", new Integer(Expression.CASEWHEN));
        tokenTable.put("CONCATE", new Integer(Expression.CONCAT));
        tokenTable.put("IS", new Integer(Expression.IS));
    }
}
