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

import java.util.NoSuchElementException;
import org.hsqldb.lib.HsqlArrayHeap;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.HashMappedList;
import org.hsqldb.lib.HsqlLinkedList;
import org.hsqldb.lib.HsqlStringBuffer;
import org.hsqldb.lib.IntValueHashMap;
import org.hsqldb.lib.ObjectComparator;
import org.hsqldb.lib.StringUtil;
import org.hsqldb.store.ValuePool;
import java.util.Stack;
import org.hsqldb.HsqlNameManager.HsqlName;

// fredt@users 20020215 - patch 1.7.0 by fredt - quoted identifiers
// support for sql standard quoted identifiers for column and table names
// fredt@users 20020218 - patch 1.7.0 by fredt - DEFAULT keyword
// support for default values for table columns
// fredt@users 20020425 - patch 548182 by skitt@users - DEFAULT enhancement
// thertz@users 20020320 - patch 473613 by thertz - outer join condition bug
// fredt@users 20021229 - patch 473613 by fredt - new solution for above
// fredt@users 20020420 - patch 523880 by leptipre@users - VIEW support
// fredt@users 20020525 - patch 559914 by fredt@users - SELECT INTO logging
// tony_lai@users 20021020 - patch 1.7.2 - improved aggregates and HAVING
// aggregate functions can now be used in expressions - HAVING supported
// kloska@users 20021030 - PATCH 1.7.2 - ON UPDATE CASCADE
// fredt@users 20021112 - patch 1.7.2 by Nitin Chauhan - use of switch
// rewrite of the majority of multiple if(){}else{} chains with switch(){}
// fredt@users 20021228 - patch 1.7.2 - refactoring
// boucherb@users 20030705 - patch 1.7.2 - handle parameter marker ambiguity

/**
 *  This class is responsible for parsing non-DDL statements.
 *
 * @version    1.7.2
 */

/** @todo fredt - implement numeric value functions (SQL92 6.6)
 *
 * POSITION(string IN string)
 * {CHAR_LENGTH | CHARACTER_LENGTH | OCTET_LENGTH | BIT_LENGTH} (string)
 * EXTRACT({YEAR | MONTH | DAY | HOUR | MINUTE | SECOND | TIMEZONE_HOUR | TIMEZONE_MINUTE} FROM {datetime | interval})
 *
 *
 *  */
class Parser {

    private Database  database;
    private Tokenizer tokenizer;
    private Session   session;
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

        database     = db;
        tokenizer    = t;
        this.session = session;
    }

    void checkTableWriteAccess(Table table,
                               int userRight) throws HsqlException {

        // session level user rights
        session.checkReadWrite();

        // object level user rights
        session.check(table.getName(), userRight);

        // object type
        if (table.isView()) {
            throw Trace.error(Trace.NOT_A_TABLE, table.getName().name);
        }

        // object readonly
        table.checkDataReadOnly();
    }

    HsqlArrayList getColumnNames() throws HsqlException {

        HsqlArrayList columns = new HsqlArrayList();
        int           i       = 0;
        String        token;

        while (true) {
            columns.add(tokenizer.getString());
            tokenizer.checkUnexpectedParam("parametric column identifier");

            i++;

            token = tokenizer.getString();

            if (token.equals(Token.T_COMMA)) {
                continue;
            }

            if (token.equals(Token.T_CLOSEBRACKET)) {
                break;
            }

            throw Trace.error(Trace.UNEXPECTED_TOKEN, token);
        }

        return columns;
    }

    void getColumnValues(Table t, Object[] row, int[] columnmap,
                         int len) throws HsqlException {

        boolean enclosed = false;
        int     i        = 0;
        String  token;
        int[]   columntypes = t.getColumnTypes();

        tokenizer.getThis(Token.T_OPENBRACKET);

        for (; i < len; i++) {
            int colindex;

            colindex      = columnmap[i];
            row[colindex] = getValue(columntypes[colindex]);
            token         = tokenizer.getString();

            if (token.equals(Token.T_COMMA)) {
                continue;
            }

            if (token.equals(Token.T_CLOSEBRACKET)) {
                enclosed = true;

                break;
            }

            throw Trace.error(Trace.UNEXPECTED_TOKEN, token);
        }

        if (!enclosed || i != len - 1) {
            throw Trace.error(Trace.COLUMN_COUNT_DOES_NOT_MATCH);
        }
    }

    SubQuery parseSubquery(boolean isView) throws HsqlException {

        HsqlException se;
        Select        s;
        SubQuery      sq;

        if (subQueryHeap == null) {
            subQueryHeap = new HsqlArrayHeap(8, new SubQuery());
        }

        if (subQueryStack == null) {
            subQueryStack = new Stack();
        }

        subQueryLevel++;

        se        = null;
        s         = null;
        sq        = new SubQuery();
        sq.level  = subQueryLevel;
        sq.isView = isView;

        subQueryStack.push(sq);

        try {
            s = parseSelect();
        } catch (HsqlException e) {
            se = e;
        }

        if (se != null) {
            throw se;
        }

        subQueryStack.pop();

        sq.select = s;

        subQueryHeap.add(sq);

        subQueryLevel--;

        return sq;
    }

    /**
     *  Method declaration
     *
     * @return
     * @throws  HsqlException
     */
    Select parseSelect() throws HsqlException {

        Select select = new Select();

// fredt@users 20011010 - patch 471710 by fredt - LIMIT rewritten
// SELECT LIMIT n m DISTINCT ... queries and error message
// "SELECT LIMIT n m ..." creates the result set for the SELECT statement then
// discards the first n rows and returns m rows of the remaining result set
// "SELECT LIMIT 0 m" is equivalent to "SELECT TOP m" or "SELECT FIRST m"
// in other RDBMS's
// "SELECT LIMIT n 0" discards the first n rows and returns the remaining rows
// fredt@users 20020225 - patch 456679 by hiep256 - TOP keyword
        String token = tokenizer.getString();

        if (token.equals(Token.T_LIMIT)) {
            String limStart = tokenizer.getString();
            String limEnd   = tokenizer.getString();

            try {
                select.limitStart = Integer.parseInt(limStart);
                select.limitCount = Integer.parseInt(limEnd);
            } catch (NumberFormatException ex) {

                // todo: add appropriate error type and message to Trace.java
                throw Trace.error(Trace.WRONG_DATA_TYPE, "LIMIT n m");
            }

            token = tokenizer.getString();
        } else if (token.equals(Token.T_TOP)) {
            String limEnd = tokenizer.getString();

            try {
                select.limitStart = 0;
                select.limitCount = Integer.parseInt(limEnd);
            } catch (NumberFormatException ex) {

                // todo: add appropriate error type and message to Trace.java
                throw Trace.error(Trace.WRONG_DATA_TYPE, "TOP m");
            }

            token = tokenizer.getString();
        }

        if (token.equals(Token.T_DISTINCT)) {
            select.isDistinctSelect = true;
        } else {
            tokenizer.back();
        }

        // parse column list
        HsqlArrayList vcolumn = new HsqlArrayList();

        do {
            Expression e = parseExpression();

            checkParamAmbiguity(!e.isParam(), "as a SELECT list item");

            token = tokenizer.getString();

            if (token.equals(Token.T_AS)) {
                e.setAlias(tokenizer.getName(),
                           tokenizer.wasQuotedIdentifier());

                token = tokenizer.getString();
            } else if (tokenizer.wasName()) {
                e.setAlias(token, tokenizer.wasQuotedIdentifier());

                token = tokenizer.getString();
            }

            vcolumn.add(e);
        } while (token.equals(Token.T_COMMA));

        if (token.equals(Token.T_INTO)) {

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
            token = tokenizer.getString();

            if (token.equals(Token.T_CACHED)) {
                select.intoType = Table.CACHED_TABLE;
                select.sIntoTable = database.nameManager.newHsqlName(
                    tokenizer.getString(), tokenizer.wasQuotedIdentifier());
            } else if (token.equals(Token.T_TEMP)) {
                select.intoType = Table.TEMP_TABLE;
                select.sIntoTable = database.nameManager.newHsqlName(
                    tokenizer.getString(), tokenizer.wasQuotedIdentifier());
            } else if (token.equals(Token.T_TEXT)) {
                select.intoType = Table.TEXT_TABLE;
                select.sIntoTable = database.nameManager.newHsqlName(
                    tokenizer.getString(), tokenizer.wasQuotedIdentifier());
            } else {
                select.sIntoTable = database.nameManager.newHsqlName(token,
                        tokenizer.wasQuotedIdentifier());
            }

            tokenizer.checkUnexpectedParam("parametric table identifier");

            token = tokenizer.getString();
        }

        if (!token.equals(Token.T_FROM)) {
            throw Trace.error(Trace.UNEXPECTED_TOKEN, token);
        }

        Expression condition = null;

        // parse table list
        HsqlArrayList vfilter = new HsqlArrayList();

        vfilter.add(parseTableFilter(false));

        while (true) {
            token = tokenizer.getString();

            if (token.equals(Token.T_LEFT)) {
                token = tokenizer.getString();

                if (token.equals(Token.T_OUTER)) {
                    token = tokenizer.getString();
                }

                Trace.check(token.equals(Token.T_JOIN),
                            Trace.UNEXPECTED_TOKEN, token);
                vfilter.add(parseTableFilter(true));
                tokenizer.getThis(Token.T_ON);

                condition = addConditionOuter(condition, parseExpression());
            } else if (token.equals(Token.T_INNER)) {
                tokenizer.getThis(Token.T_JOIN);
                vfilter.add(parseTableFilter(false));
                tokenizer.getThis(Token.T_ON);

                condition = addCondition(condition, parseExpression());
            } else if (token.equals(Token.T_COMMA)) {
                vfilter.add(parseTableFilter(false));
            } else {
                break;
            }
        }

        tokenizer.back();

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
        token = tokenizer.getString();

        if (token.equals(Token.T_WHERE)) {
            condition = addCondition(condition, parseExpression());
            token     = tokenizer.getString();
        }

        select.eCondition = condition;

// fredt@users 20020215 - patch 1.7.0 by fredt
// to support GROUP BY with more than one column
        if (token.equals(Token.T_GROUP)) {
            tokenizer.getThis(Token.T_BY);

            len = 0;

            do {
                Expression e = parseExpression();

                checkParamAmbiguity(!e.isParam(), "as a GROUP BY list item");

                // tony_lai@users having support:
                // "group by" does not allow refering to other columns alias.
                //e = doOrderGroup(e, vcolumn);
                vcolumn.add(e);

                token = tokenizer.getString();

                len++;
            } while (token.equals(Token.T_COMMA));

            select.iGroupLen = len;
        }

        // tony_lai@users - having support
        if (token.equals(Token.T_HAVING)) {
            select.iHavingIndex    = vcolumn.size();
            select.havingCondition = parseExpression();
            token                  = tokenizer.getString();

            vcolumn.add(select.havingCondition);
        }

        if (token.equals(Token.T_ORDER)) {
            tokenizer.getThis(Token.T_BY);

            len = 0;

            do {
                Expression e = parseExpression();

                checkParamAmbiguity(!e.isParam(), "as an ORDER BY list item");

                e     = checkOrderByColumns(e, vcolumn);
                token = tokenizer.getString();

                if (token.equals(Token.T_DESC)) {
                    e.setDescending();

                    token = tokenizer.getString();
                } else if (token.equals(Token.T_ASC)) {
                    token = tokenizer.getString();
                }

                vcolumn.add(e);

                len++;
            } while (token.equals(Token.T_COMMA));

            select.iOrderLen = len;
        }

        len            = vcolumn.size();
        select.eColumn = new Expression[len];

        vcolumn.toArray(select.eColumn);

        if (token.equals(Token.T_UNION)) {
            token = tokenizer.getString();

            if (token.equals(Token.T_ALL)) {
                select.iUnionType = Select.UNIONALL;
            } else {
                select.iUnionType = Select.UNION;

                tokenizer.back();
            }

            tokenizer.getThis(Token.T_SELECT);

            select.sUnion = parseSelect();
        } else if (token.equals(Token.T_INTERSECT)) {
            tokenizer.getThis(Token.T_SELECT);

            select.iUnionType = Select.INTERSECT;
            select.sUnion     = parseSelect();
        } else if (token.equals(Token.T_EXCEPT)
                   || token.equals(Token.T_MINUS)) {
            tokenizer.getThis(Token.T_SELECT);

            select.iUnionType = Select.EXCEPT;
            select.sUnion     = parseSelect();
        } else {
            tokenizer.back();
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
     * @exception  java.sql.HsqlException  Description of the Exception
     */
    private static Expression checkOrderByColumns(Expression e,
            HsqlArrayList vcolumn) throws HsqlException {

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
     * @throws  HsqlException
     */
    private TableFilter parseTableFilter(boolean outerjoin)
    throws HsqlException {

        String      token = tokenizer.getString();
        Table       t     = null;
        Select      s     = null;
        SubQuery    sq;
        TableFilter tf;

        if (token.equals(Token.T_OPENBRACKET)) {
            tokenizer.getThis(Token.T_SELECT);

            sq = parseSubquery(false);

            tokenizer.getThis(Token.T_CLOSEBRACKET);

            s = sq.select;

            s.resolveAll();

            // it's not a problem that this table has not a unique name
            t = new Table(
                database,
                database.nameManager.newHsqlName("SYSTEM_SUBQUERY", false),
                Table.SYSTEM_TABLE, 0);
            sq.table = t;

            t.addColumns(s);

            // TODO:
            // We lose / do not exploit index info here.
            // Look at what, if any, indexes the query might benefit from
            // and create or carry them across here if it might speed up
            // subsequent access.
            t.createPrimaryKey();
        } else {
            tokenizer.checkUnexpectedParam("parametric table identifier");

            t = database.getTable(token, session);

            boolean checkSelectPriv = !isParsingView();

            if (checkSelectPriv) {
                session.check(t.getName(), UserManager.SELECT);
            }

// fredt@users 20020420 - patch523880 by leptipre@users - VIEW support
            if (t.isView()) {
                String Viewname    = token;
                int    CurrentPos  = tokenizer.getPosition();
                int    sLength     = tokenizer.getLength();
                int    TokenLength = token.length();
                int    NewCurPos   = CurrentPos;

                token = tokenizer.getString();

                if (token.equals(Token.T_AS)) {
                    Viewname  = tokenizer.getName();
                    NewCurPos = tokenizer.getPosition();
                } else if (tokenizer.wasName()) {
                    Viewname  = token;
                    NewCurPos = tokenizer.getPosition();
                } else {
                    tokenizer.back();
                }

                String sLeft = tokenizer.getPart(0, CurrentPos - TokenLength);
                String sRight = tokenizer.getPart(NewCurPos, sLength);
                View             v         = (View) t;
                String           sView     = v.getStatement();
                HsqlStringBuffer sFromView = new HsqlStringBuffer(128);

                sFromView.append(sLeft);
                sFromView.append('(');
                sFromView.append(sView);
                sFromView.append(") ");
                sFromView.append(Viewname);
                sFromView.append(sRight);
                tokenizer.setString(sFromView.toString(),
                                    CurrentPos - TokenLength + 1);
                tokenizer.getThis(Token.T_SELECT);

                sq = parseSubquery(true);

                tokenizer.getThis(Token.T_CLOSEBRACKET);

                s = sq.select;

                s.resolveAll();

                // it's not a problem that this table has not a unique name
                t = new Table(
                    database,
                    database.nameManager.newHsqlName(
                        "SYSTEM_SUBQUERY", false), Table.SYSTEM_TABLE, 0);
                sq.table = t;

                t.addColumns(s);

                // TODO:
                // We lose / do not exploit index info here.
                // Look at what, if any, indexes the query might benefit from
                // and create or carry them across here if it might speed up
                // subsequent access.
                t.createPrimaryKey();
            }
        }

        String sAlias = null;

// TODO:  maybe check for unexpected param as alias?
// Not very important, but there still is a distinction
// i.e. "?" is a valid alias, but ? is, in essence, a reserved word
        token = tokenizer.getString();

        if (token.equals(Token.T_LEFT)) {
            tokenizer.back();
        } else if (token.equals(Token.T_AS)) {
            sAlias = tokenizer.getName();
        } else if (tokenizer.wasName()) {
            sAlias = token;
        } else {
            tokenizer.back();
        }

        // table filter, underlying table, six of one, ... of the other
        // SYSTEM_SUBQUERY seems so generic, but is never used anyway
        // This might be of some use in debugging, though?
        // t.getName().name = t.getName().name + "[" + sAlias + "]";
        tf         = new TableFilter(t, sAlias, outerjoin);
        tf.sSelect = s;

        return tf;
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

    private Expression addConditionOuter(Expression e1,
                                         Expression e2) throws HsqlException {

        if (!e2.setForOuterJoin()) {
            throw Trace.error(Trace.OUTER_JOIN_CONDITION);
        }

        return addCondition(e1, e2);
    }

    /**
     *  Method declaration
     *
     * @param  type
     * @return
     * @throws  HsqlException
     */
    private Object getValue(int type) throws HsqlException {

        int        paramCount = parameters.size();
        Expression r          = parseExpression();

        Trace.doAssert(paramCount == parameters.size(),
                       "expression has unbound parameters");
        r.resolve(null);

        return r.getValue(type);
    }

    /**
     *  Method declaration
     *
     * @return
     * @throws  HsqlException
     */
    private Expression parseExpression() throws HsqlException {

        read();

        Expression r = readOr();

        tokenizer.back();

        return r;
    }

    private Expression readAggregate() throws HsqlException {

        boolean distinct = false;
        int     type     = iToken;

        read();

        if (tokenizer.getString().equals(Token.T_DISTINCT)) {
            distinct = true;
        } else {
            tokenizer.back();
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
     * @throws  HsqlException
     */
    private Expression readOr() throws HsqlException {

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
     * @throws  HsqlException
     */
    private Expression readAnd() throws HsqlException {

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
     * @throws  HsqlException
     */
    private Expression readCondition() throws HsqlException {

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

                        if (sToken.equals(Token.T_ESCAPE)) {
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

                        checkParamAmbiguity(
                            !(l.getArg().isParam() && l.getArg2().isParam()),
                            "for both the first and second operands of a "
                            + "BETWEEN comparison predicate");
                        checkParamAmbiguity(
                            !(h.getArg().isParam() && h.getArg2().isParam()),
                            "for both the first and third operands of a "
                            + "BETWEEN comparison predicate");

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
                            tokenizer.back();

                            HsqlArrayList v = new HsqlArrayList();

                            while (true) {

                                // part of bigger IN list TODO:
                                // allow parametric list items
                                tokenizer.checkUnexpectedParam(
                                    "parametric IN list item");

                                Object value = getValue(Types.VARCHAR);

                                if (value == null) {
                                    throw Trace.error(
                                        Trace.NULL_IN_VALUE_LIST);
                                }

                                v.add(value);
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
     * @throws  HsqlException
     */
    private void readThis(int type) throws HsqlException {
        Trace.check(iToken == type, Trace.UNEXPECTED_TOKEN);
        read();
    }

    /**
     *  Method declaration
     *
     * @return
     * @throws  HsqlException
     */
    private Expression readConcat() throws HsqlException {

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
     * @throws  HsqlException
     */
    private Expression readSum() throws HsqlException {

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
     * @throws  HsqlException
     */
    private Expression readFactor() throws HsqlException {

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
     * @throws  HsqlException
     */
    private Expression readTerm() throws HsqlException {

        Expression r = null;

        switch (iToken) {

            case Expression.COLUMN : {
                String name = sToken;

                r = new Expression(sTable, sToken);

                read();

                if (iToken == Expression.OPEN) {
                    boolean checkPrivs = !isParsingView();
                    Function f = new Function(database.getAlias(name),
                                              session, checkPrivs);
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

                    // TODO: Maybe allow AS <alias> here
                    r = new Expression(f);
                }

                break;
            }
            case Expression.NEGATE : {
                int type = iToken;

                read();

                r = new Expression(type, readTerm(), null);

                checkParamAmbiguity(!r.getArg().isParam(),
                                    "as the operand of a unary – operation");

                break;
            }
            case Expression.PLUS : {
                read();

                r = readTerm();

                checkParamAmbiguity(!r.isParam(),
                                    "as the operand of a unary + operation");

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
            case Expression.PARAM : {
                r = new Expression(Types.NULL, null, true);

                parameters.add(r);

                // NOTE:
                // Currently unused, but may be required for future work.
                // Eventually, we may also wish to build a list of the
                // parameters that apply only to a particular subquery
                if (subQueryStack != null &&!subQueryStack.isEmpty()) {
                    SubQuery sq = (SubQuery) subQueryStack.peek();

                    sq.hasParams = true;

                    // or maybe eventually sq.addParameter(r);
                }

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

                int t = Types.getTypeNr(sToken);

                // For now, parse but ignore precision and scale
                // TODO: definitely validate values (e.g. check non-neg) and
                //       maybe even enforce in Expression.getValue(), incl. 
                //       trim, pad, throw on overflow, etc.
                int p = 0;
                int s = 0;

                if (Types.acceptsPrecisionCreateParam(t)
                        && tokenizer.isGetThis(Token.T_OPENBRACKET)) {
                    p = tokenizer.getInt();

                    if (Types.acceptsScaleCreateParam(t)
                            && tokenizer.isGetThis(Token.T_COMMA)) {
                        s = tokenizer.getInt();
                    }

                    tokenizer.getThis(Token.T_CLOSEBRACKET);
                }

                if (r.isParam()) {
                    r.setDataType(t);
                }

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

                Trace.check(sToken.equals(Token.T_AS),
                            Trace.UNEXPECTED_TOKEN, sToken);
                read();

                int t = Types.getTypeNr(sToken);

                // For now, parse but ignore precision and scale
                // TODO: definitely validate values (e.g. check non-neg) and
                //       maybe even enforce in Expression.getValue(), incl. 
                //       trim, pad, throw on overflow, etc.              
                int p = 0;
                int s = 0;

                if (Types.acceptsPrecisionCreateParam(t)
                        && tokenizer.isGetThis(Token.T_OPENBRACKET)) {
                    p = tokenizer.getInt();

                    if (Types.acceptsScaleCreateParam(t)
                            && tokenizer.isGetThis(Token.T_COMMA)) {
                        s = tokenizer.getInt();
                    }

                    tokenizer.getThis(Token.T_CLOSEBRACKET);
                }

                if (r.isParam()) {
                    r.setDataType(t);
                }

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
     * @throws  HsqlException
     */

// fredt@users 20020130 - patch 497872 by Nitin Chauhan
// reordering for speed
    private void read() throws HsqlException {

        sToken = tokenizer.getString();

        if (tokenizer.wasValue()) {
            iToken = Expression.VALUE;
            oData  = tokenizer.getAsValue();
            iType  = tokenizer.getType();
        } else if (tokenizer.wasName()) {
            iToken = Expression.COLUMN;
            sTable = null;
        } else if (tokenizer.wasLongName()) {
            sTable = tokenizer.getLongNameFirst();

//            sToken = tTokenizer.getLongNameLast();
            if (sToken.equals(Token.T_ASTERISK)) {
                iToken = Expression.MULTIPLY;
            } else {
                iToken = Expression.COLUMN;
            }
        } else if (sToken.length() == 0) {
            iToken = Expression.END;
        } else {
            iToken = tokenSet.get(sToken, -1);

            if (iToken == -1) {
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
                case Expression.PARAM :
                    break;            // nothing else required, iToken initialized properly

                case Expression.MULTIPLY :
                    sTable = null;    // in case of ASTERIX
                    break;

                case Expression.IS :
                    sToken = tokenizer.getString();

                    if (sToken.equals(Token.T_NOT)) {
                        iToken = Expression.NOT_EQUAL;
                    } else {
                        iToken = Expression.EQUAL;

                        tokenizer.back();
                    }
                    break;

                default :
                    iToken = Expression.END;
            }
        }
    }

    private static IntValueHashMap tokenSet = new IntValueHashMap(37);

    static {
        tokenSet.put(",", Expression.COMMA);
        tokenSet.put("=", Expression.EQUAL);
        tokenSet.put("!=", Expression.NOT_EQUAL);
        tokenSet.put("<>", Expression.NOT_EQUAL);
        tokenSet.put("<", Expression.SMALLER);
        tokenSet.put(">", Expression.BIGGER);
        tokenSet.put("<=", Expression.SMALLER_EQUAL);
        tokenSet.put(">=", Expression.BIGGER_EQUAL);
        tokenSet.put("AND", Expression.AND);
        tokenSet.put("NOT", Expression.NOT);
        tokenSet.put("OR", Expression.OR);
        tokenSet.put("IN", Expression.IN);
        tokenSet.put("EXISTS", Expression.EXISTS);
        tokenSet.put("BETWEEN", Expression.BETWEEN);
        tokenSet.put("+", Expression.PLUS);
        tokenSet.put("-", Expression.NEGATE);
        tokenSet.put("*", Expression.MULTIPLY);
        tokenSet.put("/", Expression.DIVIDE);
        tokenSet.put("||", Expression.STRINGCONCAT);
        tokenSet.put("(", Expression.OPEN);
        tokenSet.put(")", Expression.CLOSE);
        tokenSet.put("SELECT", Expression.SELECT);
        tokenSet.put("LIKE", Expression.LIKE);
        tokenSet.put("COUNT", Expression.COUNT);
        tokenSet.put("SUM", Expression.SUM);
        tokenSet.put("MIN", Expression.MIN);
        tokenSet.put("MAX", Expression.MAX);
        tokenSet.put("AVG", Expression.AVG);
        tokenSet.put("IFNULL", Expression.IFNULL);
        tokenSet.put("CONVERT", Expression.CONVERT);
        tokenSet.put("CAST", Expression.CAST);
        tokenSet.put("CASEWHEN", Expression.CASEWHEN);
        tokenSet.put("CONCATE", Expression.CONCAT);
        tokenSet.put("IS", Expression.IS);
        tokenSet.put("?", Expression.PARAM);
    }

// boucherb@users 20030411 - patch 1.7.2 - for prepared statements
// ---------------------------------------------------------------
    HsqlArrayList                     parameters   = new HsqlArrayList();
    private static final Expression[] noParameters = new Expression[0];
    private static final SubQuery[]   noSubqueries = new SubQuery[0];

    Expression[] getParameters() {

// TODO:  when Parser is reusable (maybe even before?) this should be
// a destructive get (i.e. clears the parameters) so as to allow earier
// garbage collection and avoid memory leaks.
// Currently, parsers are created, used and thrown away immediately, typically
// in method scope, so this is not really a big an issue for now.
        return parameters.size() == 0 ? noParameters
                                      : (Expression[]) parameters.toArray(
                                          new Expression[parameters.size()]);

//      parameters.clear();
    }

    void clearParameters() {
        parameters.clear();
    }

    // destructive get, but that's OK (preferred, actually)
    SubQuery[] getSubqueries() {

        SubQuery[] subqueries;
        int        size;

        if (subQueryHeap == null) {
            return noSubqueries;
        }

        size = subQueryHeap.size();

        if (size == 0) {
            return noSubqueries;
        }

        subqueries = new SubQuery[size];

        // order matters: we want deepest subqueries first, since higher
        // level select table filters depend on the content of lower level
        // ones.  In general, order at depth n in tree is inconsequential,
        // hence the use of heap ADT, v.s. a full tree.
        for (int i = 0; i < size; i++) {
            subqueries[i] = (SubQuery) subQueryHeap.remove();
        }

        subQueryLevel = 0;

        return subqueries;
    }

    CompiledStatement compileStatement(CompiledStatement cs)
    throws HsqlException {

        String token;

        token = tokenizer.getString();

        int id = Token.get(token);

        switch (id) {

            case Token.CALL :
                return compileCallStatement(cs);

            case Token.DELETE :
                return compileDeleteStatement(cs);

            case Token.INSERT :
                return compileInsertStatement(cs);

            case Token.UPDATE :
                return compileUpdateStatement(cs);

            case Token.SELECT :
                return compileSelectStatement(cs);

            default :
                throw Trace.error(Trace.UNEXPECTED_TOKEN, token);
        }
    }

    /**
     * Retrieves a CALL-type CompiledStatement from this parse context.
     */
    CompiledStatement compileCallStatement(CompiledStatement cs)
    throws HsqlException {

        Expression expression;

        clearParameters();

        expression = parseExpression();

        if (cs == null) {
            cs = new CompiledStatement();
        }

        cs.setAsCall(expression, getParameters());

        cs.subqueries = getSubqueries();

        return cs;
    }

    /**
     * Retrieves a DELETE-type CompiledStatement from this parse context.
     */
    CompiledStatement compileDeleteStatement(CompiledStatement cs)
    throws HsqlException {

        String     token;
        Table      table;
        Expression condition;

        clearParameters();
        tokenizer.getThis(Token.T_FROM);

        token = tokenizer.getString();

        tokenizer.checkUnexpectedParam("parametric table specificiation");

        table = database.getTable(token, session);

        checkTableWriteAccess(table, UserManager.DELETE);

        token     = tokenizer.getString();
        condition = null;

        if (token.equals(Token.T_WHERE)) {
            condition = parseExpression();
        } else {
            tokenizer.back();
        }

        if (cs == null) {
            cs = new CompiledStatement();
        }

        cs.setAsDelete(table, condition, getParameters());

        cs.subqueries = getSubqueries();

        return cs;
    }

    void getColumnValueExpressions(Table t, Expression[] acve,
                                   int len) throws HsqlException {

        boolean    enclosed;
        String     token;
        Expression cve;
        int        i;

        enclosed = false;
        i        = 0;

        tokenizer.getThis(Token.T_OPENBRACKET);

        for (; i < len; i++) {
            cve = parseExpression();

// boucherb@users 20030705
// CHECKME:  Is it always correct / desirable to resolve the expressions
// here?  Or are there some cases where resolution should be delayed.
// See, for instance, CompiledStatement.setAsUpdate().
            cve.resolve(null);

            acve[i] = cve;
            token   = tokenizer.getString();

            if (token.equals(Token.T_COMMA)) {
                continue;
            }

            if (token.equals(Token.T_CLOSEBRACKET)) {
                enclosed = true;

                break;
            }

            throw Trace.error(Trace.UNEXPECTED_TOKEN, token);
        }

        if (!enclosed || i != len - 1) {
            throw Trace.error(Trace.COLUMN_COUNT_DOES_NOT_MATCH);
        }
    }

    /**
     * Retrieves an INSERT_XXX-type CompiledStatement from this parse context.
     */
    CompiledStatement compileInsertStatement(CompiledStatement cs)
    throws HsqlException {

        String        token;
        Table         t;
        HsqlArrayList cNames;
        boolean[]     ccl;
        int[]         cm;
        int           ci;
        int           len;
        Expression[]  acve;
        Select        select;

        clearParameters();
        tokenizer.getThis(Token.T_INTO);

        token = tokenizer.getString();

        tokenizer.checkUnexpectedParam("parametric table specificiation");

        t = database.getTable(token, session);

        checkTableWriteAccess(t, UserManager.INSERT);

        token  = tokenizer.getString();
        cNames = null;
        ccl    = null;
        cm     = t.getColumnMap();
        len    = t.getColumnCount();

        if (token.equals(Token.T_OPENBRACKET)) {
            cNames = getColumnNames();

            if (cNames.size() > len) {
                throw Trace.error(Trace.COLUMN_COUNT_DOES_NOT_MATCH);
            }

            len = cNames.size();
            ccl = t.getNewColumnCheckList();
            cm  = t.getNewColumnMap();

            for (int i = 0; i < len; i++) {
                ci      = t.getColumnNr((String) cNames.get(i));
                cm[i]   = ci;
                ccl[ci] = true;
            }

            token = tokenizer.getString();
        }

        if (token.equals(Token.T_VALUES)) {
            acve = new Expression[len];

            getColumnValueExpressions(t, acve, len);

            if (cs == null) {
                cs = new CompiledStatement();
            }

            cs.setAsInsertValues(t, cm, acve, ccl, getParameters());

            cs.subqueries = cs.subqueries = getSubqueries();

            return cs;
        } else if (token.equals(Token.T_SELECT)) {
            select = parseSelect();

            if (cs == null) {
                cs = new CompiledStatement();
            }

            cs.setAsInsertSelect(t, cm, ccl, select, getParameters());

            cs.subqueries = getSubqueries();

            return cs;
        } else {
            throw Trace.error(Trace.UNEXPECTED_TOKEN, token);
        }
    }

    /**
     * Retrieves a SELECT-type CompiledStatement from this parse context.
     */
    CompiledStatement compileSelectStatement(CompiledStatement cs)
    throws HsqlException {

        Select select;

        clearParameters();

        select = parseSelect();

        if (cs == null) {
            cs = new CompiledStatement();
        }

        cs.setAsSelect(select, getParameters());

        cs.subqueries = getSubqueries();

        return cs;
    }

    /**
     * Retrieves an UPDATE-type CompiledStatement from this parse context.
     */
    CompiledStatement compileUpdateStatement(CompiledStatement cs)
    throws HsqlException {

        String token;
        Table  table;

// todo: this would be more efficient as either a primitive list or
// an IntKeyIntValueHashMap
        HsqlArrayList ciList;
        HsqlArrayList cveList;
        int           len;
        Expression    cve;
        Expression    condition;
        int[]         cm;
        Expression[]  acve;

        clearParameters();

        token = tokenizer.getString();

        tokenizer.checkUnexpectedParam("parametric table identifier");

        table = database.getTable(token, session);

        checkTableWriteAccess(table, UserManager.UPDATE);
        tokenizer.getThis(Token.T_SET);

        ciList  = new HsqlArrayList();
        cveList = new HsqlArrayList();
        len     = 0;
        token   = null;

        do {
            len++;

            int ci = table.getColumnNr(tokenizer.getString());

            ciList.add(ValuePool.getInt(ci));
            tokenizer.getThis(Token.T_EQUALS);

            cve = parseExpression();

            cveList.add(cve);

            token = tokenizer.getString();
        } while (token.equals(Token.T_COMMA));

        condition = null;

        if (token.equals(Token.T_WHERE)) {
            condition = parseExpression();
        } else {
            tokenizer.back();
        }

        cm   = new int[len];
        acve = new Expression[len];

        for (int i = 0; i < len; i++) {
            cm[i]   = ((Integer) ciList.get(i)).intValue();
            acve[i] = (Expression) cveList.get(i);
        }

        if (cs == null) {
            cs = new CompiledStatement();
        }

        cs.setAsUpdate(table, cm, acve, condition, getParameters());

        cs.subqueries = getSubqueries();

        return cs;
    }

    private int           subQueryLevel = 0;
    private Stack         subQueryStack;
    private HsqlArrayHeap subQueryHeap;
    private static final String pamsg =
        "It is ambiguous to specify a parameter marker ";

    static void checkParamAmbiguity(boolean b,
                                    String msg) throws HsqlException {
        Trace.check(b, Trace.COLUMN_TYPE_MISMATCH, pamsg + msg);
    }

    boolean isParsingView() {
        return subQueryStack != null &&!subQueryStack.isEmpty()
               && ((SubQuery) subQueryStack.peek()).isView;
    }

// --
}
