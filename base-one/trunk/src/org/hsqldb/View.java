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


package org.hsqldb;

import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.HsqlNameManager.HsqlName;

// fredt@users 20020420 - patch523880 by leptipre@users - VIEW support - modified
// fredt - todo - disallow dropping tables used in views

/**
 * Implementation of SQL VIEWS based on a SELECT query.
 *
 * @author leptipre@users
 * @version 1.7.0
 */
class View extends Table {

    Table          workingTable;
    Select         viewSelect;
    SubQuery       viewSubQuery;
    private String sStatement;

    /**
     * List of subqueries in this view in order of materialization. Last
     * element is the view itself.
     */
    SubQuery[] viewSubqueries;

    View(Database db, HsqlName name, String definition,
            HsqlArrayList colList) throws HsqlException {

        super(db, name, VIEW, 0);

        isReadOnly = true;

        setStatement(definition, colList);
    }

    /**
     * Tokenize the SELECT statement to get rid of any comment line that
     * may exist at the end. Store the result for crating the Select and
     * logging the DDL at checkpoints.<p>
     *
     * Create the SYSTEM_SUBQUERY working table and the Select object used.
     *
     * @param s
     * @param colList
     *
     * @throws HsqlException
     */
    void setStatement(String s, HsqlArrayList colList) throws HsqlException {

        int       position;
        String    str;
        Tokenizer tokenizer = new Tokenizer(s);

        // fredt@users - this establishes the end of the actual statement
        // to get rid of any end semicolon or comment line after the end
        // of statement
        do {
            position = tokenizer.getPosition();
            str      = tokenizer.getString();
        } while (str.length() != 0 || tokenizer.wasValue());

        sStatement = s.substring(0, position).trim();

        // create the working table
        tokenizer.reset(sStatement);
        tokenizer.getThis(Token.T_SELECT);

        Parser p = new Parser(this.database, tokenizer,
                              database.sessionManager.getSysSession());

        viewSubQuery = p.parseSubquery(null, true, Expression.QUERY);

        p.setAsView(this);

        viewSubqueries = p.getSortedSubqueries();
        workingTable   = viewSubQuery.table;
        viewSelect     = viewSubQuery.select;

        viewSelect.prepareResult();

        Result.ResultMetaData metadata = viewSelect.resultMetaData;
        int                   columns  = viewSelect.iResultLen;

        if (colList != null) {
            if (colList.size() != columns) {
                throw Trace.error(Trace.COLUMN_COUNT_DOES_NOT_MATCH);
            }

            for (int i = 0; i < columns; i++) {
                HsqlName name = (HsqlName) colList.get(i);

                metadata.sLabel[i]        = name.name;
                metadata.isLabelQuoted[i] = name.isNameQuoted;

                viewSelect.exprColumns[i].setAlias(name.name,
                                                   name.isNameQuoted);
                workingTable.renameColumn(workingTable.getColumn(i),
                                          name.name, name.isNameQuoted);
            }
        }

        super.addColumns(metadata, columns);

        iVisibleColumns = iColumnCount;
    }

    String getStatement() {
        return sStatement;
    }

    void setDataReadOnly(boolean value) throws HsqlException {
        throw Trace.error(Trace.NOT_A_TABLE);
    }
}
