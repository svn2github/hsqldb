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

import org.hsqldb.lib.Iterator;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.HashMap;
import org.hsqldb.store.ValuePool;

// fredt@users 20020215 - patch 1.7.0 by fredt
// to preserve column size etc. when SELECT INTO TABLE is used
// tony_lai@users 20021020 - patch 1.7.2 - improved aggregates and HAVING
// fredt@users 20021112 - patch 1.7.2 by Nitin Chauhan - use of switch
// rewrite of the majority of multiple if(){}else{} chains with switch(){}
// vorburger@users 20021229 - patch 1.7.2 - null handling

/**
 * Expression class declaration
 *
 *
 * @version    1.7.0
 */
class Expression {

    // leaf types
    static final int VALUE     = 1,
                     COLUMN    = 2,
                     QUERY     = 3,
                     TRUE      = 4,
                     VALUELIST = 5,
                     ASTERIX   = 6,
                     FUNCTION  = 7;

// boucherb@users 20020410 - parametric compiled statements
    // new leaf type
    static final int PARAM = 8;

// --
    // operations
    static final int NEGATE   = 9,
                     ADD      = 10,
                     SUBTRACT = 11,
                     MULTIPLY = 12,
                     DIVIDE   = 14,
                     CONCAT   = 15;

    // logical operations
    static final int NOT           = 20,
                     EQUAL         = 21,
                     BIGGER_EQUAL  = 22,
                     BIGGER        = 23,
                     SMALLER       = 24,
                     SMALLER_EQUAL = 25,
                     NOT_EQUAL     = 26,
                     LIKE          = 27,
                     AND           = 28,
                     OR            = 29,
                     IN            = 30,
                     EXISTS        = 31;

    // aggregate functions
    static final int COUNT = 40,
                     SUM   = 41,
                     MIN   = 42,
                     MAX   = 43,
                     AVG   = 44;

// TODO: Standard Deviation and maybe some other statistical aggregate functions
//                   STDDEV  = 45;
    // system functions
    static final int IFNULL   = 60,
                     CONVERT  = 61,
                     CASEWHEN = 62;

    // temporary used during paring
    static final int         PLUS            = 100,
                             OPEN            = 101,
                             CLOSE           = 102,
                             SELECT          = 103,
                             COMMA           = 104,
                             STRINGCONCAT    = 105,
                             BETWEEN         = 106,
                             CAST            = 107,
                             END             = 108,
                             IS              = 109;
    private static final int AGGREGATE_SELF  = -1;
    private static final int AGGREGATE_NONE  = 0;
    private static final int AGGREGATE_LEFT  = 1;
    private static final int AGGREGATE_RIGHT = 2;
    private static final int AGGREGATE_BOTH  = 3;
    int                      iType;

    // nodes
    private Expression eArg, eArg2;
    private int        aggregateSpec = AGGREGATE_NONE;

    // VALUE, VALUELIST
    Object          oData;
    private HashMap hList;
    private boolean hListIsUpper;
    private int     iDataType;

    // QUERY (correlated subquery)
    Select sSelect;

    // FUNCTION
    private Function fFunction;

    // LIKE
    private char cLikeEscape;

    // COLUMN
    private String      sTable;
    private String      sColumn;
    private TableFilter tFilter;        // null if not yet resolved
    private int         iColumn;
    private boolean     columnQuoted;
    private int         iColumnSize;
    private int         iColumnScale;
    private String      sAlias;         // if it is a column of a select column list
    private boolean     aliasQuoted;
    private boolean     bDescending;    // if it is a column in a order by

// rougier@users 20020522 - patch 552830 - COUNT(DISTINCT)
    // {COUNT|SUM|MIN|MAX|AVG}(distinct ...)
    private boolean      isDistinctAggregate;
    static final Integer INTEGER_0 = ValuePool.getInt(0);
    static final Integer INTEGER_1 = ValuePool.getInt(1);

    // PARAM
    private boolean isParam;

    /**
     * Creates a new FUNCTION expression
     * @param f
     */
    Expression(Function f) {
        iType     = FUNCTION;
        fFunction = f;
    }

    /**
     * Copy Constructor
     * @param e source expression
     */
    Expression(Expression e) {

        iType       = e.iType;
        iDataType   = e.iDataType;
        eArg        = e.eArg;
        eArg2       = e.eArg2;
        cLikeEscape = e.cLikeEscape;
        sSelect     = e.sSelect;
        fFunction   = e.fFunction;

        checkAggregate();
    }

    /**
     * Creates a new QUERY expression
     * @param s
     */
    Expression(Select s) {
        iType   = QUERY;
        sSelect = s;
    }

    /**
     * Creates a new VALUELIST expression
     * @param v
     */
    Expression(HsqlArrayList v) {

        iType     = VALUELIST;
        iDataType = Types.VARCHAR;

        int size = v.size();

        hList = new HashMap(size);

        for (int i = 0; i < size; i++) {
            Object o = v.get(i);

            hList.put(o, Expression.INTEGER_1);
        }
    }

    /**
     * Creates a new binary (or unary) operation expression
     *
     * @param type operator type
     * @param e operand 1
     * @param e2 operand 2
     */
    Expression(int type, Expression e, Expression e2) {

        iType = type;
        eArg  = e;
        eArg2 = e2;

        checkAggregate();
    }

    /**
     * Creates a new ASTERIX or COLUMN expression
     * @param table
     * @param column
     */
    Expression(String table, String column) {

        sTable = table;

        if (column == null) {
            iType = ASTERIX;
        } else {
            iType   = COLUMN;
            sColumn = column;
        }
    }

    /**
     * Creates a new ASTERIX or possibly quoted COLUMN expression
     * @param table
     * @param column
     */
    Expression(String table, String column, boolean isquoted) {

        sTable = table;

        if (column == null) {
            iType = ASTERIX;
        } else {
            iType        = COLUMN;
            sColumn      = column;
            columnQuoted = isquoted;
        }
    }

    /**
     * Creates a new VALUE expression
     *
     * @param datatype
     * @param o
     */
    Expression(int datatype, Object o) {

        iType     = VALUE;
        iDataType = datatype;
        oData     = o;
    }

    /**
     * Creates a new (possibly PARAM) VALUE expression
     *
     * @param datatype initial datatype
     * @param o initial value
     * @param isParam true if this is to be a PARAM VALUE expression
     */
    Expression(int datatype, Object o, boolean isParam) {

        this(datatype, o);

        this.isParam = isParam;
    }

    private void checkAggregate() {

        if (isAggregate(iType)) {
            aggregateSpec = AGGREGATE_SELF;
        } else {
            aggregateSpec = AGGREGATE_NONE;

            if ((eArg != null) && eArg.isAggregate()) {
                aggregateSpec += AGGREGATE_LEFT;
            }

            if ((eArg2 != null) && eArg2.isAggregate()) {
                aggregateSpec += AGGREGATE_RIGHT;
            }
        }
    }

    public String toString() {
        return toString(0);
    }

    private String toString(int blanks) {

        int          lIType;
        StringBuffer buf = new StringBuffer(64);

        buf.append('\n');

        for (int i = 0; i < blanks; i++) {
            buf.append(' ');
        }

        if (oldIType != -1) {
            buf.append("SET TRUE, WAS: ");
        }

        lIType = oldIType == -1 ? iType
                                : oldIType;

        switch (lIType) {

            case FUNCTION :
                buf.append("FUNCTION ");
                buf.append(fFunction);

                return buf.toString();

            case VALUE :
                if (isParam) {
                    buf.append("PARAM ");
                }

                buf.append("VALUE = ");
                buf.append(oData);

                if (isParam) {
                    buf.append(", TYPE = " + Types.getTypeString(iDataType));
                }

                return buf.toString();

            case COLUMN :
                buf.append("COLUMN ");

                if (sTable != null) {
                    buf.append(sTable);
                    buf.append('.');
                }

                buf.append(sColumn);

                return buf.toString();

            case QUERY :
                buf.append("QUERY ");
                buf.append(sSelect);

                return buf.toString();

            case TRUE :
                buf.append("TRUE ");
                break;

            case VALUELIST :
                buf.append("VALUELIST ");

                if (hList != null) {
                    buf.append(hList);
                    buf.append(' ');
                }
                break;

            case ASTERIX :
                buf.append("* ");
                break;

            case NEGATE :
                buf.append("NEGATE ");
                break;

            case ADD :
                buf.append("ADD ");
                break;

            case SUBTRACT :
                buf.append("SUBTRACT ");
                break;

            case MULTIPLY :
                buf.append("MULTIPLY ");
                break;

            case DIVIDE :
                buf.append("DIVIDE ");
                break;

            case CONCAT :
                buf.append("CONCAT ");
                break;

            case NOT :
                buf.append("NOT ");
                break;

            case EQUAL :
                buf.append("EQUAL ");
                break;

            case BIGGER_EQUAL :
                buf.append("BIGGER_EQUAL ");
                break;

            case BIGGER :
                buf.append("BIGGER ");
                break;

            case SMALLER :
                buf.append("SMALLER ");
                break;

            case SMALLER_EQUAL :
                buf.append("SMALLER_EQUAL ");
                break;

            case NOT_EQUAL :
                buf.append("NOT_EQUAL ");
                break;

            case LIKE :
                buf.append("LIKE ");
                break;

            case AND :
                buf.append("AND ");
                break;

            case OR :
                buf.append("OR ");
                break;

            case IN :
                buf.append("IN ");
                break;

            case EXISTS :
                buf.append("EXISTS ");
                break;

            case COUNT :
                buf.append("COUNT ");
                break;

            case SUM :
                buf.append("SUM ");
                break;

            case MIN :
                buf.append("MIN ");
                break;

            case MAX :
                buf.append("MAX ");
                break;

            case AVG :
                buf.append("AVG ");
                break;

            case IFNULL :
                buf.append("IFNULL ");
                break;

            case CONVERT :
                buf.append("CONVERT ");
                buf.append(Types.getTypeString(iDataType));
                buf.append(' ');
                break;

            case CASEWHEN :
                buf.append("CASEWHEN ");
                break;
        }

        if (eArg != null) {
            buf.append(" arg1=[");
            buf.append(eArg.toString(blanks + 1));
            buf.append(']');
        }

        if (eArg2 != null) {
            buf.append(" arg2=[");
            buf.append(eArg2.toString(blanks + 1));
            buf.append(']');
        }

        return buf.toString();
    }

    /**
     * Method declaration
     *
     *
     * @param c
     */
    void setLikeEscape(char c) {
        cLikeEscape = c;
    }

    /**
     * Method declaration
     *
     *
     * @param type
     */
    void setDataType(int type) {
        iDataType = type;
    }

// NOTES: boucherb@users.sourceforge.net 20030601
// setTrue()  is bad.  It is a destructive operation that
// affects the ability to resolve an expression more than once.
// the related methods below are useful only for now and only for toString()
// under EXPLAIN PLAN FOR on CompiledStatement objects containg Select objects.
// In the future, this all needs to be changed around to
// support clean reparameterization and reresolution of
// expression trees.
    int oldIType = -1;

    /**
     * Method declaration
     *
     */
    void setTrue() {

        if (oldIType == -1) {
            oldIType = iType;
        }

        iType = TRUE;
    }

    /**
     * Method declaration
     *
     */
    void unsetTrue() {

        if (oldIType != -1) {
            iType = oldIType;
        }
    }

    /**
     * Check if the given expression defines similar operation as this
     * expression.
     */
    public boolean similarTo(Expression exp) {

        if (exp == null) {
            return false;
        }

        if (exp == this) {
            return true;
        }

        return (iType == exp.iType) && similarTo(eArg, exp.eArg)
               && similarTo(eArg2, exp.eArg2) && equals(oData, exp.oData)
               && equals(hList, exp.hList) && iDataType == exp.iDataType
               && equals(sSelect, exp.sSelect)
               && equals(fFunction, exp.fFunction)
               && cLikeEscape == exp.cLikeEscape
               && equals(sTable, exp.sTable) && equals(sColumn, exp.sColumn)
               && iDataType == exp.iDataType;
    }

    static boolean equals(Object o1, Object o2) {
        return (o1 == null) ? o2 == null
                            : o1.equals(o2);
    }

    static boolean similarTo(Expression e1, Expression e2) {
        return (e1 == null) ? e2 == null
                            : e1.similarTo(e2);
    }

    /**
     * Check if this expression can be included in a group by clause.
     * <p>
     * It can, if itself is a column expression, and it is not an aggregate
     * expression.
     */
    boolean canBeInGroupBy() {
        return isColumn() && (!(isAggregate()));
    }

    /**
     * Check if this expression can be included in an order by clause.
     * <p>
     * It can, if itself is a column expression.
     */
    boolean canBeInOrderBy() {
        return isColumn() || isAggregate();
    }

    /**
     * Check if this expression defines at least one column.
     * <p>
     * It is, if itself is a column expression, or any the argument
     * expressions is a column expression.
     */
    boolean isColumn() {

        switch (iType) {

            case COLUMN :
                return true;

            case NEGATE :
                return eArg.isColumn();

            case ADD :
            case SUBTRACT :
            case MULTIPLY :
            case DIVIDE :
            case CONCAT :
                return eArg.isColumn() || eArg2.isColumn();
        }

        return false;
    }

    /**
     * Collect column name used in this expression.
     * @return if a column name is used in this expression
     */
    boolean collectColumnName(HashMap columnNames) {

        if (iType == COLUMN) {
            columnNames.put(sColumn, sColumn);
        }

        return iType == COLUMN;
    }

    /**
     * Collect all column names used in this expression or any of nested
     * expression.
     */
    void collectAllColumnNames(HashMap columnNames) {

        if (!collectColumnName(columnNames)) {
            if (eArg != null) {
                eArg.collectAllColumnNames(columnNames);
            }

            if (eArg2 != null) {
                eArg2.collectAllColumnNames(columnNames);
            }
        }
    }

    /**
     * Check if this expression defines a constant value.
     * <p>
     * It does, if it is a constant value expression, or all the argument
     * expressions define constant values.
     */
    boolean isConstant() {

        switch (iType) {

            case VALUE :
                return true;

            case NEGATE :
                return eArg.isConstant();

            case ADD :
            case SUBTRACT :
            case MULTIPLY :
            case DIVIDE :
            case CONCAT :
                return eArg.isConstant() && eArg2.isConstant();
        }

        return false;
    }

    /**
     * Check if this expression can be included as a result column in an
     * aggregated select statement.
     * <p>
     * It can, if itself is an aggregate expression, or it results a constant
     * value.
     */
    boolean canBeInAggregate() {
        return isAggregate() || isConstant();
    }

    /**
     *  Method declaration
     *
     *
     *  @return
     */
    boolean isAggregate() {
        return aggregateSpec != AGGREGATE_NONE;
    }

    /**
     *  Method declaration
     *
     *
     *  @return
     */
    boolean isSelfAggregate() {
        return aggregateSpec == AGGREGATE_SELF;
    }

    static boolean isAggregate(int type) {

        switch (type) {

            case COUNT :
            case MAX :
            case MIN :
            case SUM :
            case AVG :
                return true;
        }

        return false;
    }

// tony_lai@users having

    /**
     *  Checks for conditional expression.
     *
     *
     *  @return
     */
    boolean isConditional() {

        switch (iType) {

            case TRUE :
            case EQUAL :
            case BIGGER_EQUAL :
            case BIGGER :
            case SMALLER :
            case SMALLER_EQUAL :
            case NOT_EQUAL :
            case LIKE :
            case IN :
            case EXISTS :
                return true;

            case NOT :
                return eArg.isConditional();

            case AND :
            case OR :
                return eArg.isConditional() && eArg2.isConditional();

            default :
                return false;
        }
    }

    /**
     * Collects all expressions that must be in the GROUP BY clause, for a
     * grouped select statement.
     */
    void collectInGroupByExpressions(HsqlArrayList colExps) {

        if (!(isConstant() || isSelfAggregate())) {
            if (isColumn()) {
                colExps.add(this);
            } else {
                if (eArg != null) {
                    eArg.collectInGroupByExpressions(colExps);     // TODO use loop instead
                }

                if (eArg2 != null) {
                    eArg2.collectInGroupByExpressions(colExps);    // TODO use loop instead
                }
            }
        }
    }

    /**
     * Method declaration
     *
     */
    void setDescending() {
        bDescending = true;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    boolean isDescending() {
        return bDescending;
    }

    /**
     * Method declaration
     *
     *
     * @param s
     */
    void setAlias(String s, boolean isquoted) {
        sAlias      = s;
        aliasQuoted = isquoted;
    }

    String getDefinedAlias() {
        return sAlias;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    String getAlias() {

        if (sAlias != null) {
            return sAlias;
        }

        if (iType == VALUE) {
            return "";
        }

        if (iType == COLUMN) {
            return sColumn;
        }

// fredt@users 20020130 - patch 497872 by Nitin Chauhan - modified
// return column name for aggregates without alias
        if (eArg != null) {
            String name = eArg.getColumnName();

            if (name.length() > 0) {
                return name;
            }
        }

        return eArg2 == null ? ""
                             : eArg2.getAlias();
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    boolean isAliasQuoted() {

        if (sAlias != null) {
            return aliasQuoted;
        }

        if (iType == COLUMN) {
            return columnQuoted;
        }

        if (eArg != null) {
            String name = eArg.getColumnName();

            if (name.length() > 0) {
                return eArg.columnQuoted;
            }
        }

        return eArg2 == null ? false
                             : eArg2.columnQuoted;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    int getType() {
        return iType;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    Expression getArg() {
        return eArg;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    Expression getArg2() {
        return eArg2;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    TableFilter getFilter() {
        return tFilter;
    }

    /**
     * Method declaration
     *
     *
     * @throws HsqlException
     */
    void checkResolved() throws HsqlException {

        Trace.check((iType != COLUMN) || (tFilter != null),
                    Trace.COLUMN_NOT_FOUND, sColumn);

        if (eArg != null) {
            eArg.checkResolved();
        }

        if (eArg2 != null) {
            eArg2.checkResolved();
        }

        if (sSelect != null) {
            sSelect.checkResolved();
        }

        if (fFunction != null) {
            fFunction.checkResolved();
        }
    }

    /**
     * Method declaration
     *
     *
     * @param f
     *
     * @throws HsqlException
     */
    void resolve(TableFilter f) throws HsqlException {

        if (isParam) {
            return;
        }

        if ((f != null) && (iType == COLUMN)) {
            String tableName = f.getName();

            if ((sTable == null) || tableName.equals(sTable)) {
                Table table = f.getTable();
                int   i     = table.searchColumn(sColumn);

                if (i != -1) {

// fredt@users 20011110 - fix for 471711 - subselects
                    // todo: other error message: multiple tables are possible
                    Trace.check(
                        tFilter == null
                        || tFilter.getName().equals(
                            tableName), Trace.COLUMN_NOT_FOUND, sColumn);

                    Column col = table.getColumn(i);

                    tFilter      = f;
                    iColumn      = i;
                    sTable       = tableName;
                    iDataType    = col.getType();
                    iColumnSize  = col.getSize();
                    iColumnScale = col.getScale();
                }
            }
        }

        // currently sets only data type
        // todo: calculate fixed expressions if possible
        if (eArg != null) {
            eArg.resolve(f);
        }

        if (eArg2 != null) {
            eArg2.resolve(f);
        }

        if (sSelect != null) {
            sSelect.resolve(f, false);
            sSelect.resolve();
        }

        if (fFunction != null) {
            fFunction.resolve(f);
        }

        if (iDataType != Types.NULL) {
            return;
        }

        switch (iType) {

            case FUNCTION :
                iDataType = fFunction.getReturnType();
                break;

            case QUERY :
                iDataType = sSelect.eColumn[0].iDataType;
                break;

            case NEGATE :
                Trace.check(!eArg.isParam, Trace.COLUMN_TYPE_MISMATCH,
                            "it is ambiguous for a parameter marker to be "
                            + " the operand of a unary negation operation");

                iDataType = eArg.iDataType;
                break;

            case ADD :
            case SUBTRACT :
            case MULTIPLY :
            case DIVIDE :
                Trace.check(!(eArg.isParam && eArg2.isParam),
                            Trace.COLUMN_TYPE_MISMATCH,
                            "it is ambiguous for both operands of a binary "
                            + "aritmentic operator to be parameter markers");

                if (eArg.isParam) {
                    eArg.iDataType = eArg2.iDataType;
                } else if (eArg2.isParam) {
                    eArg2.iDataType = eArg.iDataType;
                }

// fredt@users 20011010 - patch 442993 by fredt
                iDataType = Column.getCombinedNumberType(eArg.iDataType,
                        eArg2.iDataType, iType);
                break;

            case CONCAT :
                iDataType = Types.VARCHAR;

                if (eArg.isParam) {
                    eArg.iDataType = Types.VARCHAR;
                }

                if (eArg2.isParam) {
                    eArg2.iDataType = Types.VARCHAR;
                }
                break;

            case EQUAL :
            case BIGGER_EQUAL :
            case BIGGER :
            case SMALLER :
            case SMALLER_EQUAL :
            case NOT_EQUAL :
                Trace.check(!(eArg.isParam && eArg2.isParam),
                            Trace.COLUMN_TYPE_MISMATCH,
                            "it is ambiguous for both expressions of a "
                            + "comparison-predicate to be parameter markers");

                if (eArg.isParam) {
                    eArg.iDataType = eArg2.iDataType;
                } else if (eArg2.isParam) {
                    eArg2.iDataType = eArg.iDataType;
                }

                iDataType = Types.BIT;
                break;

            case LIKE :
                Trace.check(!(eArg.isParam && eArg2.isParam),
                            Trace.COLUMN_TYPE_MISMATCH,
                            "it is ambiguous for both expressions of a LIKE "
                            + "comparison-predicate to be parameter markers");

                if (eArg.isParam) {
                    eArg.iDataType = Types.VARCHAR;
                } else if (eArg2.isParam) {
                    eArg2.iDataType = Types.VARCHAR;
                }

                iDataType = Types.BIT;
                break;

            case AND :
            case OR :
                if (eArg.isParam) {
                    eArg.iDataType = Types.BIT;
                }

                if (eArg2.isParam) {
                    eArg2.iDataType = Types.BIT;
                }

                iDataType = Types.BIT;
                break;

            case NOT :
                if (eArg.isParam) {
                    eArg.iDataType = Types.BIT;
                }

                iDataType = Types.BIT;
                break;

            case IN :
                if (eArg.isParam) {
                    eArg.iDataType = eArg2.iDataType;
                }

                iDataType = Types.BIT;
                break;

            case EXISTS :

                // note: no such thing as a param arg if expression is EXISTS
                iDataType = Types.BIT;
                break;

            case COUNT :
                Trace.check(
                    !eArg.isParam, Trace.COLUMN_TYPE_MISMATCH,
                    "it is ambiguous for a parameter marker to be the "
                    + "argument of a set-function-reference");

                iDataType = Types.INTEGER;
                break;

            case MAX :
            case MIN :
            case SUM :
            case AVG :
                Trace.check(
                    !eArg.isParam, Trace.COLUMN_TYPE_MISMATCH,
                    "it is ambiguous for a parameter marker to be the "
                    + "argument of a set-function-reference");

                iDataType = eArg.iDataType;
                break;

            case CONVERT :

                // iDataType for this and for eArg (if isParm)
                // is already set in Parser
                break;

            case IFNULL :
                Trace.check(!(eArg.isParam && eArg2.isParam),
                            Trace.COLUMN_TYPE_MISMATCH,
                            "it is ambiguous for both operands of an IFNULL "
                            + "operation to be parameter markers");

                if (eArg.isParam) {
                    eArg.iDataType = eArg2.iDataType;
                } else if (eArg2.isParam) {
                    eArg2.iDataType = eArg.iDataType;
                }

                // checkme: fishy...
                // if eArg.getValue() != null, then this.getValue(this.iType)
                // might try to make an unecessary (even disallowed) conversion?
                iDataType = eArg2.iDataType;
                break;

            case CASEWHEN :
                if (eArg2.eArg == null) {

                    // We use CASEWHEN as both parent and leaf type.
                    // In the parent, eArg is the condition, and eArg2 is
                    // the leaf, also tagged as type CASEWHEN, but its eArg is
                    // case 1 (how to get the value when the condition in
                    // the parent evaluates to true) and its eArg2 is case 2
                    // (how to get the value when the condition in
                    // the parent evaluates to true)
                    break;
                }

                if (eArg.isParam) {

                    // condition is a paramter marker,
                    // as in casewhen(?, v1, v1)
                    eArg.iDataType = Types.BIT;
                }

                Expression case1 = eArg2.eArg;
                Expression case2 = eArg2.eArg2;

                Trace.check(
                    !(case1.isParam && case2.isParam),
                    Trace.COLUMN_TYPE_MISMATCH,
                    "it is ambiguous for both the second and third "
                    + "operands of a CASEWHEN operation to be parameter "
                    + "markers");

                if (case1.isParam) {
                    iDataType       = case2.iDataType;
                    case1.iDataType = iDataType;
                } else if (case2.isParam) {
                    iDataType       = case1.iDataType;
                    case2.iDataType = iDataType;
                } else {

                    // boucherb@users 20030705
                    //
                    // CHECKME:
                    //
                    // - Did this ever work properly?
                    //
                    // - Does it matter?
                    //
                    //   Well, in general, the output of a CASEWHEN (indeed,
                    //   any expression) is automatically cast or converted to
                    //   the correct type (or fails in the process) when
                    //   performing inserts, updates, comparisons, arithmetic
                    //   operations, etc.,  so it was not absolutely crucial
                    //   for this to be strictly correct (completely
                    //   unambiguous) before parametric statements became
                    //   a reality.
                    //
                    //   However, the answer is now: Yes, since it may be
                    //   required to determine a param expression's data type
                    //   by inference.
                    //
                    //    Example:
                    //
                    //       ... WHERE ? = CASEWHEN(condition, expr1, expr2)
                    //
                    //    If expr1 and expr2 have different data types, then
                    //    how to infer the data type of the paramter marker?
                    //    Maybe it should be illegal for expr1 and expr2 to
                    //    have "incompatible" data types?  Maybe not?
                    //    For instance, although is is legal, does this really
                    //    make sense:
                    //
                    //    UPDATE T SET C1 = CASEWHEN(condition, 'hello', 1)
                    //
                    //    OTOH, if it is strictly disallowed, then something
                    //    like this (silly but easy to understand) example is
                    //    prohibited, yet could make sense:
                    //
                    //    UPDATE T SET C1 = CAST(CASEWHEN(c, '2', 1) AS VARCHAR)
                    //
                    //    OTOOH ;-), its just as easy to require:
                    //
                    //    UPDATE T SET C1 = CASEWHEN(c, '2', CAST(1 AS VARCHAR))
                    //
                    //    So maybe imposing a restriction that both cases of a
                    //    CASEWHEN must have the same data type is not that bad
                    //    an idea.  Thoughts?
                    /* fredt -
                          ... WHERE ? = CASEWHEN(condition, expr1, expr2)
                          this must require both expr1 and expr2 to have the
                          same type as otherwise the context is ambiguous

                          UPDATE T SET C1 = CASEWHEN(condition, 'hello', 1)
                          type of the expression must be the the same as C1 so
                          it should be resolved in the context of
                          C1 = expression

                          UPDATE T SET C1 = CASEWHEN(c, '2', CAST(1 AS VARCHAR))
                          this is the form that should be accepted if instead
                          of '2' there was a ?

                         in normal statements, there is no need to require both
                         arguments to CASEWHEN to have the same type, but in
                         parameterized statemnents they should
                    */

                    //
                    // TODO:
                    //
                    // Either enforce that both cases must be the same
                    // data type or perhaps think about the the implications
                    // of a more flexible policy, something like:
                    //
                    // 1.) Column.combinedNumberType if both case1 and case2
                    //     have number data types
                    //
                    // 2.) The data type shared by case1 and case2, if they have
                    //     the same type
                    //
                    // 3.) Throw if not 1 or 2.), or, worst case, set as the
                    //     more flexible (wider) of the two data types, when
                    //     case1 and case2 do not share the same type.
                    //
                    //     Of course, the big qeustion is:
                    //
                    //          What is more flexible type?
                    // NO:
                    // AFAICT, under both the old and current resolution
                    // scheme, this will always be zero, which is the same as
                    // Types.NULL, which I really doubt this is what we want.
                    // iDataType = eArg2.iDataType;
                    // best effort until CHECKME/TODO is resolved
                    iDataType = (case1.iDataType == Types.NULL)
                                ? case2.iDataType
                                : case1.iDataType;
                }
                break;
        }
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    boolean isResolved() {

        switch (iType) {

            case VALUE :
            case NEGATE :
                return true;

            case COLUMN :
                return tFilter != null;
        }

        // todo: could recurse here, but never miss a 'false'!
        return false;
    }

    /**
     * Method declaration
     *
     *
     * @param i
     *
     * @return
     */
    static boolean isCompare(int i) {

        switch (i) {

            case EQUAL :
            case BIGGER_EQUAL :
            case BIGGER :
            case SMALLER :
            case SMALLER_EQUAL :
            case NOT_EQUAL :
                return true;
        }

        return false;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    String getTableName() {

        if (iType == ASTERIX) {
            return sTable;
        }

        if (iType == COLUMN) {
            if (tFilter == null) {
                return sTable;
            } else {
                return tFilter.getTable().getName().name;
            }
        }

        // todo
        return "";
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    String getColumnName() {

        if (iType == COLUMN) {
            if (tFilter == null) {
                return sColumn;
            } else {
                return tFilter.getTable().getColumn(iColumn).columnName.name;
            }
        }

        return getAlias();
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    int getColumnNr() {
        return iColumn;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    int getColumnSize() {
        return iColumnSize;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    int getColumnScale() {
        return iColumnScale;
    }

    /**
     * Method declaration
     *
     * @param type
     */
    void setDistinctAggregate(boolean type) {

        isDistinctAggregate = type && (eArg.iType != ASTERIX);

        if (iType == COUNT) {
            iDataType = type ? iDataType
                             : Types.INTEGER;
        }
    }

    /**
     * Method declaration
     *
     *
     * @throws HsqlException
     */
    void swapCondition() throws HsqlException {

        int i = EQUAL;

        switch (iType) {

            case BIGGER_EQUAL :
                i = SMALLER_EQUAL;
                break;

            case SMALLER_EQUAL :
                i = BIGGER_EQUAL;
                break;

            case SMALLER :
                i = BIGGER;
                break;

            case BIGGER :
                i = SMALLER;
                break;

            case EQUAL :
                break;

            default :
                Trace.doAssert(false, "Expression.swapCondition");
        }

        iType = i;

        Expression e = eArg;

        eArg  = eArg2;
        eArg2 = e;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    int getDataType() {
        return iDataType;
    }

    /**
     * Method declaration
     *
     *
     * @param type
     *
     * @return
     *
     * @throws HsqlException
     */
    Object getValue(int type) throws HsqlException {

        Object o = getValue();

        if ((o == null) || (iDataType == type)) {
            return o;
        }

        return Column.convertObject(o, type);
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @throws HsqlException
     */
    Object getAggregatedValue(Object currValue) throws HsqlException {

        if (!isAggregate()) {
            return currValue;
        }

        switch (iType) {

            case COUNT :
                if (currValue == null) {
                    return INTEGER_0;
                }

                return ((AggregatingValue) currValue).currentValue;

            case MAX :
            case MIN :
            case SUM :
                if (currValue == null) {
                    return null;
                }

                return ((AggregatingValue) currValue).currentValue;

            case AVG :
                if (currValue == null) {
                    return null;
                }

                return Column.avg(
                    ((AggregatingValue) currValue).currentValue, iDataType,
                    ((AggregatingValue) currValue).acceptedValueCount);

            case NEGATE :
                return Column.negate(eArg.getAggregatedValue(currValue),
                                     iDataType);

            case CONVERT :
                return Column.convertObject(
                    eArg.getAggregatedValue(currValue), iDataType);
        }

        Object leftValue  = null,
               rightValue = null;

        switch (aggregateSpec) {

            case AGGREGATE_LEFT :
                leftValue  = eArg.getAggregatedValue(currValue);
                rightValue = eArg2 == null ? null
                                           : eArg2.getValue(eArg.iDataType);
                break;

            case AGGREGATE_RIGHT :
                leftValue  = eArg == null ? null
                                          : eArg.getValue(eArg2.iDataType);
                rightValue = eArg2.getAggregatedValue(currValue);
                break;

            case AGGREGATE_BOTH :
                if (currValue == null) {
                    currValue = new Object[2];
                }

                leftValue =
                    eArg.getAggregatedValue(((Object[]) currValue)[0]);
                rightValue =
                    eArg2.getAggregatedValue(((Object[]) currValue)[1]);
                break;
        }

        switch (iType) {

// tony_lai@users having >>>
            case TRUE :
                return Boolean.TRUE;

            case NOT :
                Trace.doAssert(eArg2 == null, "Expression.test");

                return new Boolean(!((Boolean) leftValue).booleanValue());

            case AND :
                return new Boolean(((Boolean) leftValue).booleanValue()
                                   && ((Boolean) rightValue).booleanValue());

            case OR :
                return new Boolean(((Boolean) leftValue).booleanValue()
                                   || ((Boolean) rightValue).booleanValue());

            case LIKE :

                // todo: now for all tests a new 'like' object required!
                String s = (String) Column.convertObject(rightValue,
                    Types.VARCHAR);
                int type = eArg.iDataType;
                Like l = new Like(s, cLikeEscape,
                                  type == Types.VARCHAR_IGNORECASE);
                String c = (String) Column.convertObject(leftValue,
                    Types.VARCHAR);

                return l.compare(c) ? Boolean.TRUE
                                    : Boolean.FALSE;

            case IN :
                return eArg2.testValueList(leftValue, eArg.iDataType)
                       ? Boolean.TRUE
                       : Boolean.FALSE;

            case EXISTS :
                Result r = eArg.sSelect.getResult(1);    // 1 is already enough

                return r.rRoot != null ? Boolean.TRUE
                                       : Boolean.FALSE;

// tony_lai@users having <<<
            case ADD :
                return Column.add(leftValue, rightValue, iDataType);

            case SUBTRACT :
                return Column.subtract(leftValue, rightValue, iDataType);

            case MULTIPLY :
                return Column.multiply(leftValue, rightValue, iDataType);

            case DIVIDE :
                return Column.divide(leftValue, rightValue, iDataType);

            case CONCAT :
                return Column.concat(leftValue, rightValue);

// tony_lai@users having >>>
        }

        int valueType = eArg.isColumn() ? eArg.iDataType
                                        : eArg2.iDataType;
        int result    = Column.compare(leftValue, rightValue, valueType);

        switch (iType) {

            case EQUAL :
                return result == 0 ? Boolean.TRUE
                                   : Boolean.FALSE;

            case BIGGER :
                return result > 0 ? Boolean.TRUE
                                  : Boolean.FALSE;

            case BIGGER_EQUAL :
                return result >= 0 ? Boolean.TRUE
                                   : Boolean.FALSE;

            case SMALLER_EQUAL :
                return result <= 0 ? Boolean.TRUE
                                   : Boolean.FALSE;

            case SMALLER :
                return result < 0 ? Boolean.TRUE
                                  : Boolean.FALSE;

            case NOT_EQUAL :
                return result != 0 ? Boolean.TRUE
                                   : Boolean.FALSE;

            default :
                Trace.check(false, Trace.NEED_AGGREGATE, this.toString());

                return null;    // Not reachable.
        }

// tony_lai@users having <<<
    }

    Object getSelfAggregatingValue(Object currValue) throws HsqlException {

        AggregatingValue aggValue =
            AggregatingValue.getAggregatingValue(currValue,
                isDistinctAggregate);
        Object newValue = eArg.iType == ASTERIX ? INTEGER_1
                                                : eArg.getValue();

        if (aggValue.isValueAcceptable(newValue)) {
            switch (iType) {

                case COUNT :
                    aggValue.currentValue = Column.sum(aggValue.currentValue,
                                                       newValue == null
                                                       ? INTEGER_0
                                                       : INTEGER_1, iDataType);
                    break;

                case AVG :
                case SUM :
                    aggValue.currentValue = Column.sum(aggValue.currentValue,
                                                       newValue, iDataType);
                    break;

                case MAX :
                    aggValue.currentValue = Column.max(aggValue.currentValue,
                                                       newValue, iDataType);
                    break;

                case MIN :
                    aggValue.currentValue = Column.min(aggValue.currentValue,
                                                       newValue, iDataType);
                    break;
            }
        }

        return aggValue;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @throws HsqlException
     */
    Object getAggregatingValue(Object currValue) throws HsqlException {

        if (!isAggregate()) {
            return getValue();
        }

        if (aggregateSpec == AGGREGATE_SELF) {
            return getSelfAggregatingValue(currValue);
        }

        Object leftCurrValue  = currValue;
        Object rightCurrValue = currValue;

        if (aggregateSpec == AGGREGATE_BOTH) {
            if (currValue == null) {
                currValue = new Object[2];
            }

            leftCurrValue  = ((Object[]) currValue)[0];
            rightCurrValue = ((Object[]) currValue)[1];
        }

        if (eArg.isAggregate()) {
            leftCurrValue = eArg.getAggregatingValue(leftCurrValue);
        }

        if (eArg2.isAggregate()) {
            rightCurrValue = eArg2.getAggregatingValue(rightCurrValue);
        }

        switch (aggregateSpec) {

            case AGGREGATE_LEFT :
                currValue = leftCurrValue;
                break;

            case AGGREGATE_RIGHT :
                currValue = rightCurrValue;
                break;

            case AGGREGATE_BOTH :
                ((Object[]) currValue)[0] = leftCurrValue;
                ((Object[]) currValue)[1] = rightCurrValue;
                break;
        }

        return currValue;
    }

    Object getValue() throws HsqlException {

        switch (iType) {

            case VALUE :
                return oData;

            case COLUMN :
                try {
                    return tFilter.oCurrentData[iColumn];
                } catch (NullPointerException e) {
                    throw Trace.error(Trace.COLUMN_NOT_FOUND, sColumn);
                }
            case FUNCTION :
                return fFunction.getValue();

            case QUERY :
                return sSelect.getValue(iDataType);

            case NEGATE :
                return Column.negate(eArg.getValue(iDataType), iDataType);

            case EXISTS :
                return new Boolean(test());

            case CONVERT :
                return eArg.getValue(iDataType);

            case CASEWHEN :
                if (eArg.test()) {
                    return eArg2.eArg.getValue();
                } else {
                    return eArg2.eArg2.getValue();
                }
        }

        // todo: simplify this
        Object a = null,
               b = null;

        if (eArg != null) {
            a = eArg.getValue(iDataType);
        }

        if (eArg2 != null) {
            b = eArg2.getValue(iDataType);
        }

        switch (iType) {

            case ADD :
                return Column.add(a, b, iDataType);

            case SUBTRACT :
                return Column.subtract(a, b, iDataType);

            case MULTIPLY :
                return Column.multiply(a, b, iDataType);

            case DIVIDE :
                return Column.divide(a, b, iDataType);

            case CONCAT :
                return Column.concat(a, b);

            case IFNULL :
                return (a == null) ? b
                                   : a;

            default :

                // must be comparisation
                // todo: make sure it is
                // boucherb@users 20030704 - ack!
                // a new Boolean for each test is a _huge_ waste
                // return new Boolean(test());
                return test() ? Boolean.TRUE
                              : Boolean.FALSE;
        }
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @throws HsqlException
     */
    boolean test() throws HsqlException {

        switch (iType) {

            case TRUE :
                return true;

            case NOT :
                Trace.doAssert(eArg2 == null, "Expression.test");

                return !eArg.test();

            case AND :
                return eArg.test() && eArg2.test();

            case OR :
                return eArg.test() || eArg2.test();

            case LIKE :

                // todo: now for all tests a new 'like' object required!
                String s    = (String) eArg2.getValue(Types.VARCHAR);
                int    type = eArg.iDataType;
                Like l = new Like(s, cLikeEscape,
                                  type == Types.VARCHAR_IGNORECASE);
                String c = (String) eArg.getValue(Types.VARCHAR);

                return l.compare(c);

            case IN :
                return eArg2.testValueList(eArg.getValue(), eArg.iDataType);

            case EXISTS :
                Result r = eArg.sSelect.getResult(1);    // 1 is already enough

                return r.rRoot != null;
        }

        Trace.check(eArg != null, Trace.GENERAL_ERROR);

        Object o    = eArg.getValue();
        int    type = eArg.iDataType;

        Trace.check(eArg2 != null, Trace.GENERAL_ERROR);

        Object o2 = eArg2.getValue(type);

        if (o == null || o2 == null) {
            return testNull(o, o2, iType);
        }

        int result = Column.compare(o, o2, type);

        switch (iType) {

            case EQUAL :
                return result == 0;

            case BIGGER :
                return result > 0;

            case BIGGER_EQUAL :
                return result >= 0;

            case SMALLER_EQUAL :
                return result <= 0;

            case SMALLER :
                return result < 0;

            case NOT_EQUAL :
                return result != 0;
        }

        Trace.doAssert(false, "Expression.test2");

        return false;
    }

// vorburger@users 20021229 - patch 1.7.2 - null handling

    /**
     * Special test to perform comparison with correct handling of SQL
     * null values. Called by test() only if a logical operation with at
     * least one of the operands being null is invoked.<p>
     *
     * Works according to the following logic:
     *
     * <pre>
     *                 Both a and b null      Either a or b null
     * EQUAL                 true                   false
     * NOT_EQUAL             false                  true
     * BIGGER                false                  false
     * BIGGER_EQUAL          true                   false
     * SMALLER               false                  false
     * SMALLER_EQUAL         true                   false
     * </pre>
     *
     * @return
     * @throws HsqlException
     */
    boolean testNull(Object a, Object b,
                     int logicalOperation) throws HsqlException {

        switch (logicalOperation) {

            case NOT_EQUAL :
                return !(a == null && b == null);

            case EQUAL :
            case BIGGER_EQUAL :
            case SMALLER_EQUAL :
                return a == null && b == null;

            case BIGGER :
            case SMALLER :
            default :
                return false;
        }
    }

    /**
     * Method declaration
     *
     *
     * @param o
     * @param datatype
     *
     * @return
     *
     * @throws HsqlException
     */

// fredt - in the future testValueList can be turned into a join query
    private boolean testValueList(Object o,
                                  int datatype) throws HsqlException {

        if (iType == VALUELIST) {
            if (datatype != iDataType) {
                o = Column.convertObject(o, iDataType);
            }

            if (o != null && datatype == Types.VARCHAR_IGNORECASE) {
                if (!hListIsUpper) {
                    HashMap  newMap = new HashMap(hList.size(), 1);
                    Iterator it     = hList.keySet().iterator();

                    while (it.hasNext()) {
                        Object key = it.next();

                        newMap.put(key.toString().toUpperCase(),
                                   this.INTEGER_1);
                    }

                    hList        = newMap;
                    hListIsUpper = true;
                }

                return hList.containsKey(o.toString().toUpperCase());
            }

            return hList.containsKey(o);
        } else if (iType == QUERY) {

            // todo: convert to valuelist before if everything is resolvable
            Result r = sSelect.getResult(0);

            // fredt - reduce the size if possible
            r.removeDuplicates();

            Record n    = r.rRoot;
            int    type = r.colType[0];

            if (datatype != type) {
                o = Column.convertObject(o, type);
            }

            while (n != null) {
                Object o2 = n.data[0];

                if (o2 != null && Column.compare(o2, o, type) == 0) {
                    return true;
                }

                n = n.next;
            }

            return false;
        }

        throw Trace.error(Trace.WRONG_DATA_TYPE);
    }

    /**
     * Tests the join condition Expression (that has been built by parsing
     * the query) for the existence of any OR clause which is not permitted
     * in OUTER joins in HSQLDB.<p>
     *
     * There are still expressions (e.g. arithmetic) that should not be used
     * in an OUTER join because they change it into an inner join but which
     * are not caught by this method.(fredt@users)
     */
    boolean canBeInOuterJoin() {

        if (eArg2 != null) {
            if (eArg2.canBeInOuterJoin() == false) {
                return false;
            }
        }

        return iType != Expression.OR;
    }

// boucherb@users 20030417 - patch 1.7.2 - compiled statement support
//-------------------------------------------------------------------
    void bind(Object o, int type) throws HsqlException {

        oData = o;

        // TODO:  now that PARAM expressions are resolved to their
        // correct data type, this stuff needs to be cleaned up
        // depends on client and protocol, so I leave the first stages
        // to you, Fred.  Basically, we either do not need this sig
        // any more, of the semantics should change
        //iDataType = type;
    }

//-------------------------------------------------------------------
//-------------------------------------------------------------------
    void bind(Object o) throws HsqlException {
        oData = o;
    }

//-------------------------------------------------------------------
    boolean isParam() {
        return isParam;
    }
}
