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
import org.hsqldb.lib.HashSet;
import org.hsqldb.store.ValuePool;

// fredt@users 20020215 - patch 1.7.0 by fredt
// to preserve column size etc. when SELECT INTO TABLE is used
// tony_lai@users 20021020 - patch 1.7.2 - improved aggregates and HAVING
// fredt@users 20021112 - patch 1.7.2 by Nitin Chauhan - use of switch
// rewrite of the majority of multiple if(){}else{} chains with switch(){}
// vorburger@users 20021229 - patch 1.7.2 - null handling
// boucherb@users 200307?? - patch 1.7.2 - resolve param nodes
// boucherb@users 200307?? - patch 1.7.2 - compress constant expr during resolve
// boucherb@users 200307?? - patch 1.7.2 - eager pmd and rsmd
// boucherb@users 20031005 - patch 1.7.2 - optimised LIKE
// boucherb@users 20031005 - patch 1.7.2 - improved IN value lists
// fredt@users 20031012 - patch 1.7.2 - better OUTER JOIN implementation

/**
 * Expression class declaration
 *
 * @version    1.7.2
 */

/** @todo fredt - move error string literals to Trace */
class Expression {

    // leaf types
    static final int VALUE     = 1,
                     COLUMN    = 2,
                     QUERY     = 3,
                     TRUE      = 4,
                     FALSE     = -4,    // arbitrary
                     VALUELIST = 5,
                     ASTERIX   = 6,
                     FUNCTION  = 7,
                     LIMIT     = 8;

// boucherb@users 20020410 - parametric compiled statements
    // new leaf type
    static final int PARAM = 9;

// --
    // operations
    static final int NEGATE   = 10,
                     ADD      = 11,
                     SUBTRACT = 12,
                     MULTIPLY = 13,
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

// TODO: Variance, std. deviation and maybe other stat. aggregate functions
//                   VARIANCE = 45;
//                   STDDEV   = 46;
    // system functions
    static final int IFNULL      = 60,
                     CONVERT     = 61,
                     CASEWHEN    = 62,
                     EXTRACT     = 63,
                     POSITION    = 64,
                     TRIM        = 65,
                     SUBSTRING   = 66,
                     NULLIF      = 67,
                     CASE        = 68,
                     COALESCE    = 69,
                     ALTERNATIVE = 70;

    // temporary used during parsing
    static final int PLUS         = 100,
                     OPEN         = 101,
                     CLOSE        = 102,
                     SELECT       = 103,
                     COMMA        = 104,
                     STRINGCONCAT = 105,
                     BETWEEN      = 106,
                     CAST         = 107,
                     END          = 108,
                     IS           = 109,
                     WHEN         = 110,
                     THEN         = 111,
                     ELSE         = 112,
                     ENDWHEN      = 113;

    // used inside brackets for system functions
    static final int     AS                      = 122,
                         FOR                     = 123,
                         FROM                    = 124,
                         BOTH                    = 125,
                         LEADING                 = 126,
                         TRAILING                = 127,
                         YEAR                    = 128,
                         MONTH                   = 129,
                         DAY                     = 130,
                         HOUR                    = 131,
                         MINUTE                  = 132,
                         SECOND                  = 133,
                         TIMEZONE_HOUR           = 134,
                         T_TIMEZONE_MINUTE       = 135;
    static final HashSet SQL_EXTRACT_FIELD_NAMES = new HashSet();
    static final HashSet SQL_TRIM_SPECIFICATION  = new HashSet();

    static {
        SQL_EXTRACT_FIELD_NAMES.addAll(new Object[] {
            Token.T_YEAR, Token.T_MONTH, Token.T_DAY, Token.T_HOUR,
            Token.T_MINUTE, Token.T_SECOND, Token.T_TIMEZONE_HOUR,
            Token.T_TIMEZONE_MINUTE
        });
        SQL_TRIM_SPECIFICATION.addAll(new Object[] {
            Token.T_LEADING, Token.T_TRAILING, Token.T_BOTH
        });
    }

    private static final int AGGREGATE_SELF     = -1;
    private static final int AGGREGATE_NONE     = 0;
    private static final int AGGREGATE_LEFT     = 1;
    private static final int AGGREGATE_RIGHT    = 2;
    private static final int AGGREGATE_BOTH     = 3;
    private static final int AGGREGATE_FUNCTION = 4;

    // type
    int         exprType;
    private int aggregateSpec = AGGREGATE_NONE;

    // nodes
    private Expression eArg, eArg2;

    // VALUE, VALUELIST
    Object          valueData;
    private HashSet hList;
    private int     dataType;

    // VALUE LIST NEW
    private Expression[] valueList;
    private boolean      isFixedConstantValueList;
    private boolean      isFixedConstantValueListChecked;

    // QUERY - in single value selects, IN or EXISTS predicates
    Select  subSelect;
    boolean isCorrelated;                     // correlated subquery
    Table   subTable;                         // if not correlated

    // FUNCTION
    private Function function;

    // LIKE
    private Like likeObject;

    // COLUMN
    private String      catalog;
    private String      schema;
    private String      tableName;
    private String      columnName;
    private TableFilter tableFilter;          // null if not yet resolved
    TableFilter         outerFilter;          // defined if this is part of an OUTER JOIN condiiton tree

    //
    private int     columnIndex;
    private boolean columnQuoted;
    private int     columnSize;
    private int     columnScale;
    private String  columnAlias;              // if it is a column of a select column list
    private boolean aliasQuoted;

    //
    private boolean isDescending;             // if it is a column in a order by
    int             orderColumnIndex = -1;    // when it is a column index in an order by

// rougier@users 20020522 - patch 552830 - COUNT(DISTINCT)
    // {COUNT|SUM|MIN|MAX|AVG}(distinct ...)
    private boolean isDistinctAggregate;

    // PARAM
    private boolean isParam;

    // does Expression stem from a JOIN <table> ON <expression>
    boolean isInJoin;

    //
    static final Integer INTEGER_0 = ValuePool.getInt(0);
    static final Integer INTEGER_1 = ValuePool.getInt(1);

    /**
     * Creates a new boolean expression
     * @param f
     */
    Expression(boolean b) {
        exprType = b ? TRUE
                     : FALSE;
    }

    /**
     * Creates a new FUNCTION expression
     * @param f
     */
    Expression(Function f) {

        exprType = FUNCTION;
        function = f;

        if (f.hasAggregate) {
            aggregateSpec = AGGREGATE_FUNCTION;
        }
    }

    /**
     * Copy Constructor. Used by TableFilter to move a condition to a filter.
     * @param e source expression
     */
    Expression(Expression e) {

        exprType = e.exprType;
        dataType = e.dataType;
        eArg     = e.eArg;
        eArg2    = e.eArg2;
        isInJoin = e.isInJoin;

        //
        likeObject = e.likeObject;
        subSelect  = e.subSelect;
        function   = e.function;

        checkAggregate();
    }

    /**
     * Creates a new QUERY expression
     * @param s
     */
    Expression(Select s, Table t, boolean correlated) {

        exprType     = QUERY;
        subSelect    = s;
        subTable     = t;
        isCorrelated = correlated;
    }

    /**
     * Creates a new VALUELIST expression
     * @param v
     */
    Expression(Expression[] valueList) {
        exprType       = VALUELIST;
        this.valueList = valueList;
    }

    /**
     * Creates a new binary (or unary) operation expression
     *
     * @param type operator type
     * @param e operand 1
     * @param e2 operand 2
     */
    Expression(int type, Expression e, Expression e2) {

        exprType = type;
        eArg     = e;
        eArg2    = e2;

        checkAggregate();
    }

    /**
     * Creates a new LIKE expression
     *
     * @param type operator type
     * @param e operand 1
     * @param e2 operand 2
     */
    Expression(Expression e, Expression e2, Character escape) {

        exprType   = LIKE;
        eArg       = e;
        eArg2      = e2;
        likeObject = new Like(escape);

        checkAggregate();
    }

    /**
     * Creates a new ASTERIX or COLUMN expression
     * @param table
     * @param column
     */
    Expression(String table, String column) {

        tableName = table;

        if (column == null) {
            exprType = ASTERIX;
        } else {
            exprType   = COLUMN;
            columnName = column;
        }
    }

    /**
     * Creates a new ASTERIX or possibly quoted COLUMN expression
     * @param table
     * @param column
     */
    Expression(String table, String column, boolean isquoted) {

        tableName = table;

        if (column == null) {
            exprType = ASTERIX;
        } else {
            exprType     = COLUMN;
            columnName   = column;
            columnQuoted = isquoted;
        }
    }

    Expression(String table, Column column) {

        tableName = table;

        if (column == null) {
            exprType = ASTERIX;
        } else {
            exprType     = COLUMN;
            columnName   = column.columnName.name;
            columnQuoted = column.columnName.isNameQuoted;
            dataType     = column.getType();
        }
    }

    /**
     * Creates a new VALUE expression
     *
     * @param datatype
     * @param o
     */
    Expression(int datatype, Object o) {

        exprType  = VALUE;
        dataType  = datatype;
        valueData = o;
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

        if (isParam) {
            paramMode = PARAM_IN;
        }
    }

    private void checkAggregate() {

        if (isAggregate(exprType)) {
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

    /**
     * For use with CHECK constraints. Under development.
     *
     * Currently supports a subset of expressions and is suitable for CHECK
     * search conditions that refer only to the inserted/updated row.
     *
     * For full DDL reporting of VIEW select queries and CHECK search
     * conditions, future improvements here are dependent upon improvements to
     * SELECT query parsing, so that it is performed in a number of passes.
     * An early pass should result in the query turned into an Expression tree
     * that contains the information in the original SQL without any
     * alterations, and with tables and columns all resolved. This Expression
     * can then be preserved for future use. Table and column names that
     * are not user-defined aliases should be kept as the HsqlName structures
     * so that table or column renaming is reflected in the precompiled
     * query.
     */
    String getDDL() throws HsqlException {

        StringBuffer buf   = new StringBuffer(64);
        String       left  = null;
        String       right = null;

        if (eArg != null) {
            left = eArg.getDDL();

            if (eArg.exprType != VALUE && eArg.exprType != COLUMN
                    && eArg.exprType != FUNCTION
                    && eArg.exprType != ALTERNATIVE
                    && eArg.exprType != CASEWHEN
                    && eArg.exprType != CONVERT) {
                StringBuffer temp = new StringBuffer();

                left = temp.append('(').append(left).append(')').toString();
            }
        }

        if (eArg2 != null) {
            right = eArg2.getDDL();

            if (eArg2.exprType != VALUE && eArg2.exprType != COLUMN
                    && eArg2.exprType != FUNCTION
                    && eArg2.exprType != ALTERNATIVE
                    && eArg2.exprType != CASEWHEN
                    && eArg2.exprType != CONVERT) {
                StringBuffer temp = new StringBuffer();

                right = temp.append('(').append(right).append(')').toString();
            }
        }

        switch (exprType) {

            case FUNCTION :
                return function.getDLL();

            case VALUE :
                try {
                    return Column.createSQLString(valueData, dataType);
                } catch (HsqlException e) {}

                return buf.toString();

            case COLUMN :

                // this is a limited solution
                Table  table = tableFilter.getTable();
                String ddlName;

                if (tableName != null) {
                    buf.append(table.tableName.statementName);
                    buf.append('.');
                }

                buf.append(
                    table.getColumn(columnIndex).columnName.statementName);

                return buf.toString();

            case TRUE :
                return Token.T_TRUE;

            case FALSE :
                return Token.T_FALSE;

            case VALUELIST :
                for (int i = 0; i < valueList.length; i++) {
                    buf.append(valueList[i].getDDL());

                    if (i < valueList.length - 1) {
                        buf.append(',');
                    }
                }

                return buf.toString();

            case ASTERIX :
                buf.append('*');

                return buf.toString();

            case NEGATE :
                buf.append('-').append(left);

                return buf.toString();

            case ADD :
                buf.append(left).append('+').append(right);

                return buf.toString();

            case SUBTRACT :
                buf.append(left).append('-').append(right);

                return buf.toString();

            case MULTIPLY :
                buf.append(left).append('*').append(right);

                return buf.toString();

            case DIVIDE :
                buf.append(left).append('/').append(right);

                return buf.toString();

            case CONCAT :
                buf.append(left).append("||").append(right);

                return buf.toString();

            case NOT :
                buf.append(Token.T_NOT).append(' ').append(left);

                return buf.toString();

            case EQUAL :
                buf.append(left).append('=').append(right);

                return buf.toString();

            case BIGGER_EQUAL :
                buf.append(left).append(">=").append(right);

                return buf.toString();

            case BIGGER :
                buf.append(left).append('>').append(right);

                return buf.toString();

            case SMALLER :
                buf.append(left).append('<').append(right);

                return buf.toString();

            case SMALLER_EQUAL :
                buf.append(left).append("<=").append(right);

                return buf.toString();

            case NOT_EQUAL :
                buf.append(left).append("!=").append(right);

                return buf.toString();

            case LIKE :
                buf.append(left).append(' ').append(Token.T_LIKE).append(' ');
                buf.append(right);

                /** @todo fredt - scripting of non-ascii escapes needs changes to general script logging */
                if (likeObject.escapeChar != null) {
                    buf.append(' ').append(Token.T_ESCAPE).append(' ').append(
                        '\'');
                    buf.append(likeObject.escapeChar.toString()).append('\'');
                    buf.append(' ');
                }

                return buf.toString();

            case AND :
                buf.append(left).append(' ').append(Token.T_AND).append(
                    ' ').append(right);

                return buf.toString();

            case OR :
                buf.append(left).append(' ').append(Token.T_OR).append(
                    ' ').append(right);

                return buf.toString();

            case IN :
                buf.append(left).append(' ').append(Token.T_IN).append(
                    ' ').append(right);

                return buf.toString();

            case CONVERT :
                buf.append(' ').append(Token.T_CONVERT).append('(');
                buf.append(left).append(',');
                buf.append(Types.getTypeString(dataType));
                buf.append(')');

                return buf.toString();

            case CASEWHEN :
                buf.append(' ').append(Token.T_CASEWHEN).append('(');
                buf.append(left).append(',').append(right).append(')');

                return buf.toString();

            case ALTERNATIVE :
                buf.append(left).append(',').append(right);

                return buf.toString();

            case QUERY :
/*
                buf.append('(');
                buf.append(subSelect.getDDL());
                buf.append(')');
*/
                break;

            case EXISTS :
                buf.append(' ').append(Token.T_EXISTS).append(' ');
                break;

            case COUNT :
                buf.append(' ').append(Token.T_COUNT).append('(');
                break;

            case SUM :
                buf.append(' ').append(Token.T_SUM).append('(');
                buf.append(left).append(')');
                break;

            case MIN :
                buf.append(' ').append(Token.T_MIN).append('(');
                buf.append(left).append(')');
                break;

            case MAX :
                buf.append(' ').append(Token.T_MAX).append('(');
                buf.append(left).append(')');
                break;

            case AVG :
                buf.append(' ').append(Token.T_AVG).append('(');
                buf.append(left).append(')');
                break;
        }

        throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED,
                          "check constraint expression");
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

        lIType = oldIType == -1 ? exprType
                                : oldIType;

        switch (lIType) {

            case FUNCTION :
                buf.append("FUNCTION ");
                buf.append(function);

                return buf.toString();

            case VALUE :
                if (isParam) {
                    buf.append("PARAM ");
                }

                buf.append("VALUE = ").append(valueData);
                buf.append(", TYPE = ").append(Types.getTypeString(dataType));

                return buf.toString();

            case COLUMN :
                buf.append("COLUMN ");

                if (tableName != null) {
                    buf.append(tableName);
                    buf.append('.');
                }

                buf.append(columnName);

                return buf.toString();

            case QUERY :
                buf.append("QUERY ");
                buf.append(subSelect);

                return buf.toString();

            case TRUE :
                buf.append("TRUE ");
                break;

            case FALSE :
                buf.append("FALSE ");
                break;

            case VALUELIST :
                buf.append("VALUELIST ");
                buf.append(" TYPE = ").append(Types.getTypeString(dataType));

                if (valueList != null) {
                    for (int i = 0; i < valueList.length; i++) {
                        buf.append(valueList[i].toString(blanks + blanks));
                        buf.append(' ');
                    }
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

            case CONVERT :
                buf.append("CONVERT ");
                buf.append(Types.getTypeString(dataType));
                buf.append(' ');
                break;

            case CASEWHEN :
                buf.append("CASEWHEN ");
                break;
        }

        if (isInJoin) {
            buf.append(" join");
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
     * @param type
     */
    void setDataType(int type) {
        dataType = type;
    }

    int oldIType = -1;

    /**
     * When an Expression is assigned to a TableFilter, a copy is made for use
     * there and the original is set to Expression.TRUE
     *
     */
    void setTrue() {

        if (oldIType == -1) {
            oldIType = exprType;
        }

        exprType = TRUE;
    }

    /**
     * Check if the given expression defines similar operation as this
     * expression. This method is used for ensuring an expression in
     * the ORDER BY clause has a matching column in the SELECT list. This check
     * is necessary with a SELECT DISTINCT query.<br>
     *
     * In the future we may perform the test when evaluating the search
     * condition to get a more accurate match.
     */
    public boolean similarTo(Expression exp) {

        if (exp == null) {
            return false;
        }

        if (exp == this) {
            return true;
        }

        return (exprType == exp.exprType) && similarTo(eArg, exp.eArg)
               && similarTo(eArg2, exp.eArg2)
               && equals(valueData, exp.valueData)
               && equals(valueList, exp.valueList)
               && dataType == exp.dataType
               && equals(subSelect, exp.subSelect)
               && equals(function, exp.function)
               && equals(tableName, exp.tableName)
               && equals(columnName, exp.columnName)
               && dataType == exp.dataType;
    }

    static boolean equals(Object o1, Object o2) {
        return (o1 == null) ? o2 == null
                            : o1.equals(o2);
    }

    static boolean equals(Expression[] ae1, Expression[] ae2) {

        if (ae1 == ae2) {
            return true;
        }

        if (ae1.length != ae2.length) {
            return false;
        }

        int     len    = ae1.length;
        boolean equals = true;

        for (int i = 0; i < len; i++) {
            Expression e1 = ae1[i];
            Expression e2 = ae2[i];

            equals = (e1 == null) ? e2 == null
                                  : e1.equals(e2);
        }

        return equals;
    }

    static boolean similarTo(Expression e1, Expression e2) {
        return (e1 == null) ? e2 == null
                            : e1.similarTo(e2);
    }

/** @todo fredt - workaround for functions in ORDER BY and GROUP BY needs
 *  checking the argument of the function to ensure they are valid. */

    /**
     * Check if this expression can be included in a group by clause.
     * <p>
     * It can, if itself is a column expression, and it is not an aggregate
     * expression.
     */
    boolean canBeInGroupBy() {

        if (exprType == FUNCTION) {
            return true;
        }

        return isColumn() && (!(isAggregate()));
    }

    /**
     * Check if this expression can be included in an order by clause.
     * <p>
     * It can, if itself is a column expression.
     */
    boolean canBeInOrderBy() {
        return exprType == FUNCTION || orderColumnIndex != -1 || isColumn()
               || isAggregate();
    }

    /**
     * Check if this expression defines at least one column.
     * <p>
     * It is, if itself is a column expression, or any the argument
     * expressions is a column expression.
     */
    private boolean isColumn() {

        switch (exprType) {

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
    boolean collectColumnName(HashSet columnNames) {

        if (exprType == COLUMN) {
            columnNames.add(columnName);
        }

        return exprType == COLUMN;
    }

    /**
     * Collect all column names used in this expression or any of nested
     * expression.
     */
    void collectAllColumnNames(HashSet columnNames) {

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

        switch (exprType) {

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

        switch (exprType) {

            case TRUE :
            case FALSE :
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
        isDescending = true;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    boolean isDescending() {
        return isDescending;
    }

    /**
     * Method declaration
     *
     *
     * @param s
     */
    void setAlias(String s, boolean isquoted) {
        columnAlias = s;
        aliasQuoted = isquoted;
    }

    String getDefinedAlias() {
        return columnAlias;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    String getAlias() {

        if (columnAlias != null) {
            return columnAlias;
        }

        if (exprType == VALUE) {
            return "";
        }

        if (exprType == COLUMN) {
            return columnName;
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

        if (columnAlias != null) {
            return aliasQuoted;
        }

        if (exprType == COLUMN) {
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
        return exprType;
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
        return tableFilter;
    }

    /**
     * Final check for all expressions.
     *
     * @throws HsqlException
     */
    boolean checkResolved(boolean check) throws HsqlException {

        boolean result = true;

        if (eArg != null) {
            result = result && eArg.checkResolved(check);
        }

        if (eArg2 != null) {
            result = result && eArg2.checkResolved(check);
        }

        if (subSelect != null) {
            result = result && subSelect.checkResolved(check);
        }

        if (function != null) {
            result = result && function.checkResolved(check);
        }

        if (valueList != null) {
            for (int i = 0; i < valueList.length; i++) {
                result = result && valueList[i].checkResolved(check);
            }
        }

        if (exprType == COLUMN && tableFilter == null) {

            // if an order by column alias
            result = orderColumnIndex != -1;

            if (!result && check) {
                String err = tableName == null ? columnName
                                               : tableName + "." + columnName;

                throw Trace.error(Trace.COLUMN_NOT_FOUND, err);
            }
        }

        return result;
    }

    /**
     * Resolve the table names for columns and throws if a column remains
     * unresolved.
     *
     * @param fa
     *
     * @throws HsqlException
     */
    void checkTables(HsqlArrayList fa) throws HsqlException {

        if (fa == null || exprType == Expression.VALUE) {
            return;
        }

        if (eArg != null) {
            eArg.checkTables(fa);
        }

        if (eArg2 != null) {
            eArg2.checkTables(fa);
        }

        switch (exprType) {

            case COLUMN :
                boolean found = false;
                int     len   = fa.size();

                for (int j = 0; j < len; j++) {
                    TableFilter filter     = (TableFilter) fa.get(j);
                    String      filterName = filter.getName();

                    if (tableName == null || filterName.equals(tableName)) {
                        Table table = filter.getTable();
                        int   i     = table.searchColumn(columnName);

                        if (i != -1) {
                            if (tableName == null) {
                                if (found) {
                                    throw Trace.error(
                                        Trace.AMBIGUOUS_COLUMN_REFERENCE,
                                        columnName);
                                }

                                //
                                found = true;
                            } else {
                                return;
                            }
                        }
                    }
                }

                if (found) {
                    return;
                }

                throw Trace.error(Trace.COLUMN_NOT_FOUND, columnName);
            case QUERY :

                // fredt - subquery in join condition !
                break;

            case FUNCTION :
                if (function != null) {
                    function.checkTables(fa);
                }
                break;

            case IN :
                if (eArg2.exprType != QUERY) {
                    Expression[] vl = eArg2.valueList;

                    for (int i = 0; i < vl.length; i++) {
                        vl[i].checkTables(fa);
                    }
                }
                break;

            default :
        }
    }

    /**
     * Workaround for CHECK constraints.
     */
    void setLikeOptimised() throws HsqlException {

        if (eArg != null) {
            eArg.setLikeOptimised();
        }

        if (eArg2 != null) {
            eArg2.setLikeOptimised();
        }

        if (exprType == LIKE) {
            likeObject.optimised = true;
        }
    }

    /**
     * Removes table filter resolution from an Expression tree.
     */
/*
    void removeFilters() throws HsqlException {

        if (eArg != null) {
            eArg.removeFilters();
        }

        if (eArg2 != null) {
            eArg2.removeFilters();
        }

        switch (exprType) {

            case COLUMN :
                tableFilter = null;

                return;

            case QUERY :
                if (subSelect != null) {
                    subSelect.removeFilters();
                }
                break;

            case FUNCTION :
                if (function != null) {
                    function.removeFilters();
                }
                break;

            case IN :
                if (eArg2.exprType != QUERY) {
                    Expression[] vl = eArg2.valueList;

                    for (int i = 0; i < vl.length; i++) {
                        vl[i].removeFilters();
                    }
                }
                break;

            default :
        }
    }
*/
    void getEquiJoinColumns(TableFilter filter, boolean[] columns,
                            Expression[] elist) {

        if (eArg != null) {
            eArg.getEquiJoinColumns(filter, columns, elist);
        }

        if (eArg2 != null) {
            eArg2.getEquiJoinColumns(filter, columns, elist);
        }

        if (exprType == EQUAL) {
            if (eArg.tableFilter == eArg2.tableFilter) {
                return;
            }

            // an elist element may be set more than once - OK
            if (eArg.tableFilter == filter) {
                if (eArg2.exprType == COLUMN || eArg2.exprType == VALUE) {
                    columns[eArg.columnIndex] = true;
                    elist[eArg.columnIndex]   = eArg2;
                }

                return;
            }

            if (eArg2.tableFilter == filter) {
                if (eArg.exprType == COLUMN || eArg.exprType == VALUE) {
                    columns[eArg2.columnIndex] = true;
                    elist[eArg2.columnIndex]   = eArg;
                }
            }
        }
    }

    /**
     * Resolve the table names for columns
     *
     *
     * @param f
     *
     * @throws HsqlException
     */
    void resolveTables(TableFilter f) throws HsqlException {

        if (isParam || f == null || exprType == Expression.VALUE) {
            return;
        }

        if (eArg != null) {
            eArg.resolveTables(f);
        }

        if (eArg2 != null) {
            eArg2.resolveTables(f);
        }

        switch (exprType) {

            case COLUMN :
                if (tableFilter != null) {
                    break;
                }

                String filterName = f.getName();

                if (tableName == null || tableName.equals(filterName)) {
                    Table table = f.getTable();
                    int   i     = table.searchColumn(columnName);

                    if (i != -1) {
/*
// fredt@users 20011110 - fix for 471711 - subselects
                        boolean repeat = tableFilter != null && !tableFilter.getName().equals(filterName);
                        if ( repeat){
                             throw Trace.error(Trace.AMBIGUOUS_COLUMN_REFERENCE, columnName);
                        }
*/
                        tableFilter = f;
                        columnIndex = i;
                        tableName   = filterName;

                        setTableColumnAttributes(table, i);

                        // COLUMN is leaf; we are done
                        return;
                    }
                }
                break;

            case QUERY :

                // we now (1_7_2_ALPHA_R) resolve independently first, then
                // resolve in the enclosing context
                if (subSelect != null) {
                    subSelect.resolveTables(f);
                }
                break;

            case FUNCTION :
                if (function != null) {
                    function.resolveTables(f);
                }
                break;

            case IN :
                if (eArg2.exprType != QUERY) {
                    Expression[] vl = eArg2.valueList;

                    for (int i = 0; i < vl.length; i++) {
                        vl[i].resolveTables(f);
                    }
                }
                break;

            default :
        }
    }

    void resolveTypes() throws HsqlException {

        if (isParam || exprType == Expression.VALUE) {
            return;
        }

        if (eArg != null) {
            eArg.resolveTypes();
        }

        if (eArg2 != null) {
            eArg2.resolveTypes();
        }

        switch (exprType) {

            case FUNCTION :
                function.resolveType();

                dataType = function.getReturnType();
                break;

            case QUERY : {
                subSelect.resolveTypes();

                dataType = subSelect.exprColumns[0].dataType;

                break;
            }
            case NEGATE :
                Trace.check(
                    !eArg.isParam, Trace.COLUMN_TYPE_MISMATCH,
                    "it is ambiguous for a parameter marker to be the operand of a unary negation operation");

                dataType = eArg.dataType;

                if (isFixedConstant()) {
                    valueData = getValue(dataType);
                    eArg      = null;
                    exprType  = VALUE;
                }
                break;

            case ADD :
            case SUBTRACT :
            case MULTIPLY :
            case DIVIDE :
                Trace.check(
                    !(eArg.isParam && eArg2.isParam),
                    Trace.COLUMN_TYPE_MISMATCH,
                    "it is ambiguous for both operands of a binary aritmetic operator to be parameter markers");

                if (isFixedConstant()) {
                    dataType = Column.getCombinedNumberType(eArg.dataType,
                            eArg2.dataType, exprType);
                    valueData = getValue(dataType);
                    eArg      = null;
                    eArg2     = null;
                    exprType  = VALUE;
                } else {
                    if (eArg.isParam) {
                        eArg.dataType = eArg2.dataType;
                    } else if (eArg2.isParam) {
                        eArg2.dataType = eArg.dataType;
                    }

                    // fredt@users 20011010 - patch 442993 by fredt
                    dataType = Column.getCombinedNumberType(eArg.dataType,
                            eArg2.dataType, exprType);
                }
                break;

            case CONCAT :
                dataType = Types.VARCHAR;

                if (isFixedConstant()) {
                    valueData = getValue(dataType);
                    eArg      = null;
                    eArg2     = null;
                    exprType  = VALUE;
                } else {
                    if (eArg.isParam) {
                        eArg.dataType = Types.VARCHAR;
                    }

                    if (eArg2.isParam) {
                        eArg2.dataType = Types.VARCHAR;
                    }
                }
                break;

            case EQUAL :
            case BIGGER_EQUAL :
            case BIGGER :
            case SMALLER :
            case SMALLER_EQUAL :
            case NOT_EQUAL :
                Trace.check(
                    !(eArg.isParam && eArg2.isParam),
                    Trace.COLUMN_TYPE_MISMATCH,
                    "it is ambiguous for both expressions of a comparison-predicate to be parameter markers");

                if (isFixedConditional()) {
                    exprType = test() ? TRUE
                                      : FALSE;
                    eArg     = null;
                    eArg2    = null;
                } else if (eArg.isParam) {
                    eArg.dataType = eArg2.dataType;

                    if (eArg2.exprType == COLUMN) {
                        eArg.setTableColumnAttributes(eArg2);
                    }
                } else if (eArg2.isParam) {
                    eArg2.dataType = eArg.dataType;

                    if (eArg.exprType == COLUMN) {
                        eArg2.setTableColumnAttributes(eArg);
                    }
                }

                dataType = Types.BIT;
                break;

            case LIKE :
                resolveTypeForLike();

                dataType = Types.BIT;
                break;

            case AND : {
                boolean eArgFixed  = eArg.isFixedConditional();
                boolean eArg2Fixed = eArg2.isFixedConditional();

                if (eArgFixed && eArg2Fixed) {
                    exprType = test() ? TRUE
                                      : FALSE;
                    eArg     = null;
                    eArg2    = null;
                } else if ((eArgFixed &&!eArg.test())
                           || (eArg2Fixed &&!eArg2.test())) {
                    exprType = FALSE;
                    eArg     = null;
                    eArg2    = null;
                } else {
                    if (eArg.isParam) {
                        eArg.dataType = Types.BIT;
                    }

                    if (eArg2.isParam) {
                        eArg2.dataType = Types.BIT;
                    }
                }

                dataType = Types.BIT;

                break;
            }
            case OR : {
                boolean eArgFixed  = eArg.isFixedConditional();
                boolean eArg2Fixed = eArg2.isFixedConditional();

                if (eArgFixed && eArg2Fixed) {
                    exprType = test() ? TRUE
                                      : FALSE;
                    eArg     = null;
                    eArg2    = null;
                } else if ((eArgFixed && eArg.test())
                           || (eArg2Fixed && eArg2.test())) {
                    exprType = TRUE;
                    eArg     = null;
                    eArg2    = null;
                } else {
                    if (eArg.isParam) {
                        eArg.dataType = Types.BIT;
                    }

                    if (eArg2.isParam) {
                        eArg2.dataType = Types.BIT;
                    }
                }

                dataType = Types.BIT;

                break;
            }
            case NOT :
                if (isFixedConditional()) {
                    exprType = test() ? TRUE
                                      : FALSE;
                    eArg     = null;
                } else if (eArg.isParam) {
                    eArg.dataType = Types.BIT;
                }

                dataType = Types.BIT;
                break;

            case IN :
                resolveTypeForIn();

                dataType = Types.BIT;
                break;

            case EXISTS :

                // NOTE: no such thing as a param arg if expression is EXISTS
                // Also, cannot detect if result is fixed value
                dataType = Types.BIT;
                break;

            /** @todo fredt - set the correct return type */
            case COUNT :
                Trace.check(
                    !eArg.isParam, Trace.COLUMN_TYPE_MISMATCH,
                    "it is ambiguous for a parameter marker to be the argument of a set-function-reference");

                dataType = Types.INTEGER;
                break;

            case MAX :
            case MIN :
            case SUM :
            case AVG :
                Trace.check(
                    !eArg.isParam, Trace.COLUMN_TYPE_MISMATCH,
                    "it is ambiguous for a parameter marker to be the argument of a set-function-reference");

                dataType = SetFunction.getType(exprType, eArg.dataType);
                break;

            case CONVERT :

                // NOTE: both iDataType for this expr and for eArg (if isParm)
                // are already set in Parser during read
                if (eArg.isFixedConstant() || eArg.isFixedConditional()) {
                    valueData = getValue(dataType);
                    exprType  = VALUE;
                    eArg      = null;
                }
                break;

            case CASEWHEN :

                // We use CASEWHEN as both parent type.
                // In the parent, eArg is the condition, and eArg2 is
                // the leaf, tagged as type ALTERNATIVE, and its eArg is
                // case 1 (how to get the value when the condition in
                // the parent evaluates to true) and its eArg2 is case 2
                // (how to get the value when the condition in
                // the parent evaluates to true)
                if (eArg.isParam) {

                    // condition is a paramter marker,
                    // as in casewhen(?, v1, v1)
                    eArg.dataType = Types.BIT;
                }

                dataType = eArg2.dataType;
                break;

            case ALTERNATIVE : {
                Expression case1 = eArg;
                Expression case2 = eArg2;

                Trace.check(
                    !(case1.isParam && case2.isParam),
                    Trace.COLUMN_TYPE_MISMATCH,
                    "it is ambiguous for both the alternative operands of a CASE operation to be parameter markers");

                if (case1.isParam || case1.dataType == Types.NULL) {
                    case1.dataType = case2.dataType;
                } else if (case2.isParam || case2.dataType == Types.NULL) {
                    case2.dataType = case1.dataType;
                }

                if (case1.dataType == Types.NULL
                        && case2.dataType == Types.NULL) {
                    dataType = Types.NULL;
                }

                if (Types.isNumberType(case1.dataType)
                        && Types.isNumberType(case2.dataType)) {
                    dataType = Column.getCombinedNumberType(case1.dataType,
                            case2.dataType, ALTERNATIVE);
                } else if (Types.isCharacterType(case1.dataType)
                           && Types.isCharacterType(case2.dataType)) {

                    // Good enough for now?
                    dataType = Types.LONGVARCHAR;
                } else {
                    Trace.check(
                        case1.dataType == case2.dataType,
                        Trace.COLUMN_TYPE_MISMATCH,
                        "the output data type of a CASE operation is ambiguous when the alternative operand types are ",
                        Types.getTypeString(case1.dataType), " and ",
                        Types.getTypeString(case2.dataType));
                }

                break;
            }
        }
    }

    void resolveTypeForLike() throws HsqlException {

        Trace.check(
            !(eArg.isParam && eArg2.isParam), Trace.COLUMN_TYPE_MISMATCH,
            "it is ambiguous for both expressions of a LIKE comparison-predicate to be parameter markers");

        if (isFixedConditional()) {
            exprType = test() ? TRUE
                              : FALSE;
            eArg     = null;
            eArg2    = null;
        } else if (eArg.isParam) {
            eArg.dataType = Types.VARCHAR;
        } else if (eArg2.isParam) {
            eArg2.dataType = Types.VARCHAR;
        }

// boucherb@users 2003-09-25 - patch 1.7.2 Alpha P
//
// Some optimizations for LIKE
//
// TODO:
//
// See if the same optimizations can be done dynamically at execute time when
// eArg2 is PARAM.  Unfortunately, this currently requires re-resolving from
// the root any expression containing at least one parameterized LIKE in the
// compiled statement and reseting conditions on any involved table filters,
// so the answer is: probably not, at least not under the current code.
//
// CHECKME:
//
// Test for correct results under all XXXCHAR types (padding, etc.?)
//
// NOTE:
//
// For the old behaviour, simply comment out the block below
        if (likeObject.optimised) {
            return;
        }

        boolean isRightArgFixedConstant = eArg2.isFixedConstant();
        String likeStr = isRightArgFixedConstant
                         ? (String) eArg2.getValue(Types.VARCHAR)
                         : null;
        boolean ignoreCase = eArg.dataType == Types.VARCHAR_IGNORECASE;

        likeObject.setParams(likeStr, ignoreCase);

        if (!isRightArgFixedConstant) {

            // Then we are done here, since it's impossible
            // to determine at this point if the right expression
            // will have a fixed prefix that can be used to optimize
            // any involved table filters
            return;
        }

        if (likeObject.isEquivalentToFalsePredicate()) {
            exprType   = FALSE;
            eArg       = null;
            eArg2      = null;
            likeObject = null;
        } else if (likeObject.isEquivalentToEqualsPredicate()) {
            exprType   = EQUAL;
            eArg2 = new Expression(Types.VARCHAR, likeObject.getRangeLow());
            likeObject = null;
        } else if (likeObject.isEquivalentToNotNullPredicate()) {

            // X LIKE '%' <=>  X IS NOT NULL
            exprType   = NOT_EQUAL;
            eArg2      = new Expression(Types.NULL, null);
            likeObject = null;
        } else {
            if (eArg.exprType != Expression.COLUMN) {

                // Then we are done here, since range predicates are
                // not picked up for use to optimize table filters
                // unless the predicate is on the first column of
                // an index.
                // TODO:
                // We might go one step further here and check if the
                // column is elligible (is the first column of some
                // index on its table).  If it is not, it may be that
                // substituting/inserting range predicate below
                // can actually lower performance.
                // Indeed, we might better consider delaying the
                // optimizations below till the TableFilter.setConditions()
                // phase.
                return;
            }

            if (!Types.isCharacterType(eArg.dataType)) {

                // TODO:
                // correct range low / range high generation for
                // types other than XXXCHAR
                return;
            }

            if (likeObject.isEquivalentToBetweenPredicate()) {

                // X LIKE 'abc%' <=> X >= 'abc' AND X <= 'abc' || max_collation_char
                Expression eArgOld = eArg;
                Expression eFirst = new Expression(Types.VARCHAR,
                                                   likeObject.getRangeLow());
                Expression eLast = new Expression(Types.VARCHAR,
                                                  likeObject.getRangeHigh());

                eArg     = new Expression(BIGGER_EQUAL, eArgOld, eFirst);
                eArg2    = new Expression(SMALLER_EQUAL, eArgOld, eLast);
                exprType = AND;

                //
                likeObject = null;
            } else if (likeObject
                    .isEquivalentToBetweenPredicateAugmentedWithLike()) {

                // X LIKE 'abc%...' <=> X >= 'abc' AND X <= 'abc' || max_collation_char AND X LIKE 'abc%...'
                Expression eFirst = new Expression(Types.VARCHAR,
                                                   likeObject.getRangeLow());
                Expression eLast = new Expression(Types.VARCHAR,
                                                  likeObject.getRangeHigh());
                Expression gte = new Expression(BIGGER_EQUAL, eArg, eFirst);
                Expression lte = new Expression(SMALLER_EQUAL, eArg, eLast);

                eArg2 = new Expression(eArg, eArg2, likeObject.escapeChar);
                eArg2.likeObject = likeObject;
                eArg             = new Expression(AND, gte, lte);
                exprType         = AND;
                likeObject       = null;
            }
        }
    }

    void resolveTypeForIn() throws HsqlException {

// PARAM Resolution rules:
//
// Expression used with IN:    Same as the first value or the result column
//                             of the subquery
//
// A value used with IN:       Same as the expression or the first value if
//                             there is a parameter marker in the expression
// Implies ambiguity if:       A parameter marker is both the expression and
//                             the first value of an IN operation, from which
//                             follows that it is ambiguous for the expression
//                             to be a parameter marker if the list is empty.
// CHECKME:
// Is an empty IN list legal?  Why would anyone ever use it?
        if (eArg2.exprType == QUERY) {
            if (eArg.isParam) {
                eArg.dataType = eArg2.dataType;
            }
        } else {    // eArg2.exprType == VALUELIST
            Expression[] vl = eArg2.valueList;

            if (eArg.isParam) {
                Trace.check(
                    vl.length > 0, Trace.COLUMN_TYPE_MISMATCH,
                    "it is ambiguous for the expression of an IN operation to be a parameter marker when the value list is empty");
                Trace.check(
                    !vl[0].isParam, Trace.COLUMN_TYPE_MISMATCH,
                    "it is ambiguous for both the expression and the first value list entry of an IN operation to be parameter markers");

                Expression e  = vl[0];
                int        dt = e.dataType;

                // PARAM datatype same as first value list expression
                // should never be Types.NULL when all is said and done
                if (dt == Types.NULL) {

                    // do nothing...
                } else {
                    if (eArg.dataType == Types.NULL) {
                        eArg.dataType = dt;
                    }

                    if (eArg2.dataType == Types.NULL) {
                        eArg2.dataType = dt;
                    }
                }

                for (int i = 1; i < vl.length; i++) {
                    e = vl[i];

                    if (e.isParam) {
                        if (e.dataType == Types.NULL && dt != Types.NULL) {
                            e.dataType = dt;
                        }
                    } else {
                        e.resolveTypes();
                    }
                }
            } else {
                int dt = eArg.dataType;

                if (eArg2.dataType == Types.NULL && dt != Types.NULL) {
                    eArg2.dataType = dt;
                }

                for (int i = 0; i < vl.length; i++) {
                    Expression e = vl[i];

                    if (e.isParam) {
                        if (e.dataType == Types.NULL && dt != Types.NULL) {
                            e.dataType = dt;
                        }
                    } else {
                        e.resolveTypes();
                    }
                }
            }
        }
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    boolean isResolved() {

        switch (exprType) {

            case VALUE :
            case NEGATE :
                return true;

            case COLUMN :
                return tableFilter != null && tableFilter.isAssigned;

            case QUERY :
                return true;
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

        if (exprType == ASTERIX) {
            return tableName;
        }

        if (exprType == COLUMN) {
            if (tableFilter == null) {
                return tableName;
            } else {
                return tableFilter.getTable().getName().name;
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

        if (exprType == COLUMN) {
            if (tableFilter == null) {
                return columnName;
            } else {
                return tableFilter.getTable().getColumn(
                    columnIndex).columnName.name;
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
        return columnIndex;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    int getColumnSize() {
        return columnSize;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    int getColumnScale() {
        return columnScale;
    }

    /**
     * Method declaration
     *
     * @param type
     */
    void setDistinctAggregate(boolean type) {

        isDistinctAggregate = type && (eArg.exprType != ASTERIX);

        if (exprType == COUNT) {
            dataType = type ? dataType
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

        switch (exprType) {

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

        exprType = i;

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
        return dataType;
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

        if ((o == null) || (dataType == type)) {
            return o;
        }

        return Column.convertObject(o, type);
    }

/** @todo fredt - should be rewritten to handle only set function operation,
     *  with other operations handled in the normal way */

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

        // handles results of aggregates plus NEGATE and CONVERT
        switch (exprType) {

            case COUNT :
                if (currValue == null) {
                    return INTEGER_0;
                }

                return ((SetFunction) currValue).getValue();

            case MAX :
            case MIN :
            case SUM :
            case AVG :
                if (currValue == null) {
                    return null;
                }

                return ((SetFunction) currValue).getValue();

            case NEGATE :
                return Column.negate(eArg.getAggregatedValue(currValue),
                                     dataType);

            case CONVERT :
                return Column.convertObject(
                    eArg.getAggregatedValue(currValue), dataType);
        }

        // handle expressions
        Object leftValue  = null,
               rightValue = null;

        switch (aggregateSpec) {

            case AGGREGATE_LEFT :
                leftValue  = eArg.getAggregatedValue(currValue);
                rightValue = eArg2 == null ? null
                                           : eArg2.getValue();
                break;

            case AGGREGATE_RIGHT :
                leftValue  = eArg == null ? null
                                          : eArg.getValue();
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

        // handle other operations
        switch (exprType) {

// tony_lai@users having >>>
            case TRUE :
                return Boolean.TRUE;

            case FALSE :
                return Boolean.FALSE;

            case NOT :
                Trace.doAssert(eArg2 == null,
                               "Expression.getAggregatedValue.NOT");

                return ((Boolean) leftValue).booleanValue() ? Boolean.FALSE
                                                            : Boolean.TRUE;

            case AND :
                return ((Boolean) leftValue).booleanValue()
                       && ((Boolean) rightValue).booleanValue() ? Boolean.TRUE
                                                                : Boolean
                                                                .FALSE;

            case OR :
                return ((Boolean) leftValue).booleanValue()
                       || ((Boolean) rightValue).booleanValue() ? Boolean.TRUE
                                                                : Boolean
                                                                .FALSE;

            case LIKE :
                String s = (String) Column.convertObject(rightValue,
                    Types.VARCHAR);

                if (eArg2.isParam || eArg2.exprType != VALUE) {
                    likeObject.resetPattern(s);
                }

                String c = (String) Column.convertObject(leftValue,
                    Types.VARCHAR);

                return likeObject.compare(c) ? Boolean.TRUE
                                             : Boolean.FALSE;

            case IN :
                return eArg2.testValueList(leftValue, eArg.dataType)
                       ? Boolean.TRUE
                       : Boolean.FALSE;

            case EXISTS :
                if (eArg.isCorrelated) {
                    Result r = eArg.subSelect.getResult(1);    // 1 is already enough

                    return r.rRoot == null ? Boolean.FALSE
                                           : Boolean.TRUE;
                } else {
                    return subTable.isEmpty() ? Boolean.FALSE
                                              : Boolean.TRUE;
                }
            case CASEWHEN :
                if (leftValue instanceof SetFunction) {
                    leftValue = Column.convertObject(
                        ((SetFunction) leftValue).getValue(), Types.BOOLEAN);
                } else {
                    leftValue = Column.convertObject(leftValue,
                                                     Types.BOOLEAN);
                }

                boolean test   = ((Boolean) leftValue).booleanValue();
                Object  result = test ? ((Object[]) rightValue)[0]
                                      : ((Object[]) rightValue)[1];

                if (result instanceof SetFunction) {
                    return Column.convertObject(
                        ((SetFunction) result).getValue(), dataType);
                } else {
                    return Column.convertObject(result, dataType);
                }
            case ALTERNATIVE :
                if (leftValue instanceof SetFunction) {
                    leftValue = Column.convertObject(
                        ((SetFunction) leftValue).getValue(), dataType);
                } else {
                    leftValue = Column.convertObject(leftValue, dataType);
                }

                if (rightValue instanceof SetFunction) {
                    rightValue = Column.convertObject(
                        ((SetFunction) rightValue).getValue(), dataType);
                } else {
                    rightValue = Column.convertObject(rightValue, dataType);
                }

                Object[] objectPair = new Object[2];

                objectPair[0] = leftValue;
                objectPair[1] = rightValue;

                return objectPair;

            case FUNCTION :
                return function.getAggregatedValue(currValue);
        }

        // handle comparisons
        // convert vals
        if (isCompare(exprType)) {
            int valueType = eArg.isColumn() ? eArg.dataType
                                            : eArg2.dataType;

            return compareValues(leftValue, rightValue, valueType, exprType)
                   ? Boolean.TRUE
                   : Boolean.FALSE;
        }

        // handle arithmetic and concat operations
        if (leftValue != null) {
            leftValue = Column.convertObject(leftValue, dataType);
        }

        if (rightValue != null) {
            rightValue = Column.convertObject(rightValue, dataType);
        }

        switch (exprType) {

            case ADD :
                return Column.add(leftValue, rightValue, dataType);

            case SUBTRACT :
                return Column.subtract(leftValue, rightValue, dataType);

            case MULTIPLY :
                return Column.multiply(leftValue, rightValue, dataType);

            case DIVIDE :
                return Column.divide(leftValue, rightValue, dataType);

            case CONCAT :
                return Column.concat(leftValue, rightValue);

            default :
                Trace.check(false, Trace.NEED_AGGREGATE, this.toString());

                return null;    // Not reachable.
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
    Object updateAggregatingValue(Object currValue) throws HsqlException {

        if (!isAggregate()) {
            return getValue();
        }

        switch (aggregateSpec) {

            case AGGREGATE_SELF : {
                if (currValue == null) {
                    currValue = new SetFunction(exprType, eArg.dataType,
                                                isDistinctAggregate);
                }

                Object newValue = eArg.exprType == ASTERIX ? INTEGER_1
                                                           : eArg.getValue();

                ((SetFunction) currValue).add(newValue);

                return currValue;
            }
            case AGGREGATE_BOTH : {
                Object[] valuePair = (Object[]) currValue;

                if (valuePair == null) {
                    valuePair = new Object[2];
                }

                valuePair[0] = eArg.updateAggregatingValue(valuePair[0]);
                valuePair[1] = eArg2.updateAggregatingValue(valuePair[1]);

                return valuePair;
            }
            case AGGREGATE_LEFT :
                return eArg.updateAggregatingValue(currValue);

            case AGGREGATE_RIGHT :
                return eArg2.updateAggregatingValue(currValue);

            case AGGREGATE_FUNCTION :
                return function.updateAggregatingValue(currValue);

            default :

                // never gets here
                return currValue;
        }
    }

    Object getValue() throws HsqlException {

        switch (exprType) {

            case VALUE :
                return valueData;

            case COLUMN :
                try {
                    return tableFilter.currentData[columnIndex];
                } catch (NullPointerException e) {
                    throw Trace.error(Trace.COLUMN_NOT_FOUND, columnName);
                }
            case FUNCTION :
                return function.getValue();

            case QUERY :
                return subSelect.getValue(dataType);

            case NEGATE :
                return Column.negate(eArg.getValue(dataType), dataType);

            case EXISTS :
                return test() ? Boolean.TRUE
                              : Boolean.FALSE;

            case CONVERT :
                return eArg.getValue(dataType);

            case CASEWHEN :
                if (eArg.test()) {
                    return eArg2.eArg.getValue(dataType);
                } else {
                    return eArg2.eArg2.getValue(dataType);
                }

            // gets here from getAggregatedValue()
            case ALTERNATIVE :
                return new Object[] {
                    eArg.getValue(dataType), eArg2.getValue(dataType)
                };
        }

        // todo: simplify this
        Object a = null,
               b = null;

        if (eArg != null) {
            a = eArg.getValue(dataType);
        }

        if (eArg2 != null) {
            b = eArg2.getValue(dataType);
        }

        switch (exprType) {

            case ADD :
                return Column.add(a, b, dataType);

            case SUBTRACT :
                return Column.subtract(a, b, dataType);

            case MULTIPLY :
                return Column.multiply(a, b, dataType);

            case DIVIDE :
                return Column.divide(a, b, dataType);

            case CONCAT :
                return Column.concat(a, b);

            default :

                // must be comparion
                // todo: make sure it is
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

        switch (exprType) {

            case TRUE :
                return true;

            case FALSE :
                return false;

            case NOT :
                Trace.doAssert(eArg2 == null, "Expression.test");

                return !eArg.test();

            case AND :
                return eArg.test() && eArg2.test();

            case OR :
                return eArg.test() || eArg2.test();

            case LIKE :
/*
                // todo: now for all tests a new 'like' object required!
                String s    = (String) eArg2.getValue(Types.VARCHAR);
                int    type = eArg.dataType;
                Like l = new Like(s, likeEscapeChar,
                                  type == Types.VARCHAR_IGNORECASE);
                String c = (String) eArg.getValue(Types.VARCHAR);

                return l.compare(c);
*/
                String s = (String) eArg2.getValue(Types.VARCHAR);

                if (eArg2.isParam || eArg2.exprType != VALUE) {
                    likeObject.resetPattern(s);
                }

                String c = (String) eArg.getValue(Types.VARCHAR);

                return likeObject.compare(c);

            case IN :
                return eArg2.testValueList(eArg.getValue(), eArg.dataType);

            case EXISTS :
                Result r = eArg.subSelect.getResult(1);    // 1 is already enough

                return r.rRoot != null;
        }

        Trace.check(eArg != null, Trace.GENERAL_ERROR);

/*
        int    type = eArg2.likeOptimized ? Types.VARCHAR
                                          : eArg.dataType;
*/
        int    type = eArg.dataType;
        Object o    = eArg.getValue(type);

        Trace.check(eArg2 != null, Trace.GENERAL_ERROR);

        Object o2 = eArg2.getValue(type);

        if (o == null || o2 == null) {
/*
 TableFilter.swapCondition() ensures that with LEFT OUTER, eArg is the
 column expression for the table on the right hand side.
 We do not join tables on nulls apart from outer joins
 Any comparison operator can exist in WHERE or JOIN conditions
*/
            if (eArg.tableFilter != null && eArg2.tableFilter != null
                    &&!eArg.tableFilter.isOuterJoin) {

                // here we should have (eArg.iType == COLUMN && eArg2.iType == COLUMN)
                return false;
            }

            if (eArg.tableFilter != null && eArg.tableFilter.isOuterJoin) {
                if (isInJoin) {
                    if (eArg.tableFilter.isCurrentOuter || o == null) {
                        return true;
                    }
                } else {

                    // this is used in WHERE <OUTER JOIN COL> IS [NOT] NULL
                    eArg.tableFilter.nonJoinIsNull = o2 == null;
                }
            }

            return testNull(o, o2, exprType);
        }

        return compareValues(o, o2, type, exprType);
    }

    private static boolean compareValues(Object o, Object o2, int valueType,
                                         int exprType) throws HsqlException {

        int result = Column.compare(o, o2, valueType);

        switch (exprType) {

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

            default :
                throw Trace.error(Trace.GENERAL_ERROR,
                                  Trace.Expression_compareValues);
        }
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
// boucherb@users - 2003-09-25 - patch 1.7.2 Alpha Q - parametric IN lists
//                  and correlated IN list expressions
// fredt - catch type conversion exception due to narrowing
    private boolean testValueList(Object o,
                                  int datatype) throws HsqlException {

        if (o == null) {
            return false;
        }

        if (exprType == VALUELIST) {
            if (datatype != this.dataType) {
                try {
                    o = Column.convertObject(o, this.dataType);
                } catch (HsqlException e) {
                    return false;
                }
            }

            if (!isFixedConstantValueListChecked) {
                int len = valueList.length;

                if (!isFixedConstantValueList) {
                    isFixedConstantValueList = true;

                    for (int i = 0; i < len; i++) {
                        if (!valueList[i].isFixedConstant()) {
                            isFixedConstantValueList = false;

                            break;
                        }
                    }
                }

                if (isFixedConstantValueList) {
                    hList = new HashSet();

                    for (int i = 0; i < len; i++) {
                        Object value = valueList[i].getValue(datatype);

                        hList.add(value);
                    }
                }

                isFixedConstantValueListChecked = true;
            }

            if (isFixedConstantValueList) {
                return hList.contains(o);
            }

            final int len = valueList.length;

            for (int i = 0; i < len; i++) {
                Object o2 = valueList[i].getValue(datatype);

                if (Column.compare(o, o2, datatype) == 0) {
                    return true;
                }
            }

            return false;
        } else if (exprType == QUERY) {

            /** @todo fredt - convert to join */
            if (subTable != null) {
                try {
                    o = Column.convertObject(o, subTable.getColumnTypes()[0]);
                } catch (HsqlException e) {
                    return false;
                }

                return subTable.getPrimaryIndex().findFirst(
                    o, Expression.EQUAL) != null;
            }

            Result r = subSelect.getResult(0);

            // fredt - reduce the size if possible
            r.removeDuplicates();

            Record n    = r.rRoot;
            int    type = r.metaData.colType[0];

            try {
                o = Column.convertObject(o, type);
            } catch (HsqlException e) {
                return false;
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
     * Marks all the expressions in the tree for a condition that is part
     * of a JOIN .. ON ....<br>
     *
     * For LEFT OUTER joins, also tests the expression tree for the join
     * condition to ensure only permitted expression types are there.
     *
     * If we want to exapand the expressions to include arithmetic operations
     * or functions ...
     *
     * (fredt@users)
     */
    boolean setForJoin(TableFilter tf, boolean outer) {

        isInJoin = outer;

        if (outer) {
            outerFilter = tf;
        }

        if (eArg != null) {
            if (eArg.setForJoin(tf, outer) == false) {
                return false;
            }
        }

        if (eArg2 != null) {
            if (eArg2.setForJoin(tf, outer) == false) {
                return false;
            }
        }

        return !outer
               || (exprType == Expression.AND || exprType == Expression.OR
                   || exprType == Expression.COLUMN
                   || exprType == Expression.VALUE
                   || exprType == Expression.EQUAL
                   || exprType == Expression.NOT_EQUAL
                   || exprType == Expression.BIGGER
                   || exprType == Expression.BIGGER_EQUAL
                   || exprType == Expression.SMALLER
                   || exprType == Expression.SMALLER_EQUAL);
    }

    /**
     * Returns a Select object that can be used for checking the contents
     * of an existing table against the given CHECK search condition.
     *
     */
    static Select getCheckSelect(Table t, Expression e) throws HsqlException {

        Select s = new Select();

        s.exprColumns    = new Expression[1];
        s.exprColumns[0] = new Expression(VALUE, Boolean.TRUE);
        s.tFilter        = new TableFilter[1];
        s.tFilter[0]     = new TableFilter(t, null, false);

        Expression condition = new Expression(NOT, e, null);

        s.queryCondition = condition;

        s.resolveAll(true);

        return s;
    }

    /**
     * Sets the left leaf.
     */
    void setLeftExpression(Expression e) {
        eArg = e;
    }

// boucherb@users 20030417 - patch 1.7.2 - compiled statement support
//-------------------------------------------------------------------
//    void bind(Object o, int type) throws HsqlException {
//
//        oData = o;
//
//        // TODO:  now that PARAM expressions are resolved to their
//        // correct data type, this stuff needs to be cleaned up
//        // depends on client and protocol, so I leave the first stages
//        // to you, Fred.  Basically, we either do not need this sig
//        // any more, of the semantics should change
//        //iDataType = type;
//    }
//-------------------------------------------------------------------
//-------------------------------------------------------------------
    void bind(Object o) throws HsqlException {
        valueData = o;
    }

//-------------------------------------------------------------------
    boolean isParam() {
        return isParam;
    }

    boolean isFixedConstant() {

        switch (exprType) {

            case VALUE :
                return !isParam;

            case NEGATE :
                return eArg.isFixedConstant();

            case ADD :
            case SUBTRACT :
            case MULTIPLY :
            case DIVIDE :
            case CONCAT :
                return eArg.isFixedConstant() && eArg2.isFixedConstant();
        }

        return false;
    }

    boolean isFixedConditional() {

        switch (exprType) {

            case TRUE :
            case FALSE :
                return true;

            case EQUAL :
            case BIGGER_EQUAL :
            case BIGGER :
            case SMALLER :
            case SMALLER_EQUAL :
            case NOT_EQUAL :
            case LIKE :

                //case IN : TODO
                return eArg.isFixedConstant() && eArg2.isFixedConstant();

            case NOT :
                return eArg.isFixedConditional();

            case AND :
            case OR :
                return eArg.isFixedConditional()
                       && eArg2.isFixedConditional();

            default :
                return false;
        }
    }

    void setTableColumnAttributes(Expression e) {

        columnSize  = e.columnSize;
        columnScale = e.columnScale;
        isIdentity  = e.isIdentity;
        nullability = e.nullability;
        isWritable  = e.isWritable;
        catalog     = e.catalog;
        schema      = e.schema;
    }

    void setTableColumnAttributes(Table t, int i) {

        Column c;

        c           = t.getColumn(i);
        dataType    = c.getType();
        columnSize  = c.getSize();
        columnScale = c.getScale();
        isIdentity  = c.isIdentity();

        // IDENTITY columns are not nullable but NULLs are accepted
        // and converted into the next identity value for the table
        nullability = c.isNullable() &&!isIdentity ? NULLABLE
                                                   : NO_NULLS;
        isWritable  = t.isWritable();
        catalog     = t.getCatalogName();
        schema      = t.getSchemaName();
    }

    String getValueClassName() {

        int        ditype;
        int        ditypesub;
        DITypeInfo ti;

        if (valueClassName != null) {
            return valueClassName;
        }

        if (function != null) {
            valueClassName = function.getReturnClass().getName();

            return valueClassName;
        }

        if (dataType == Types.VARCHAR_IGNORECASE) {
            ditype    = Types.VARCHAR;
            ditypesub = Types.TYPE_SUB_IGNORECASE;
        } else {
            ditype    = dataType;
            ditypesub = Types.TYPE_SUB_DEFAULT;
        }

        ti = new DITypeInfo();

        ti.setTypeCode(ditype);
        ti.setTypeSub(ditypesub);

        valueClassName = ti.getColStClsName();

        return valueClassName;
    }

    // parameter modes
    static final int PARAM_UNKNOWN = 0;
    static final int PARAM_IN      = 1;
    static final int PARAM_IN_OUT  = 2;
    static final int PARAM_OUT     = 4;

    // result set (output column value) or parameter expression nullability
    static final int NO_NULLS         = 0;
    static final int NULLABLE         = 1;
    static final int NULLABLE_UNKNOWN = 2;

    // output column and parameter expression metadata values
    boolean isIdentity;        // = false
    int     nullability = NULLABLE_UNKNOWN;
    boolean isWritable;        // = false; true if column of writable table
    int     paramMode = PARAM_UNKNOWN;
    String  valueClassName;    // = null
}
