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

// fredt@users 20030810 - patch 1.7.2 - OUTER JOIN rewrite
// fredt@users 20030813 - patch 1.7.2 - fix for column comparison within same table bugs #572075 and 722443

/**
 * This class iterates over table elements to perform a join between two
 * tables, or a self-join, using indexes if they are availabe.
 *
 * @version 1.7.2
 */
class TableFilter {

    static final int CONDITION_NONE      = -1;     // not a condition expression
    static final int CONDITION_UNORDERED = 0;      // not candidate for eStart or eEnd
    static final int   CONDITION_START_END = 1;    // candidate for eStart and eEnd
    static final int   CONDITION_START     = 2;    // candidate for eStart
    static final int   CONDITION_END       = 3;    // candidate for eEnd
    private Table      tTable;
    Select             sSelect;
    private String     sAlias;
    private Index      iIndex;
    private Node       nCurrent;
    private Object     oEmptyData[];
    private Expression eStart, eEnd;

    //
    Expression eAnd;
    boolean    isOuterJoin;
    Object     oCurrentData[];
    Row        currentRow;

    // addendum to the result of findFirst() and next() with isOuterJoin==true
    // when the result is false, it indicates if a non-join condition caused the failure
    boolean nonJoinIsNull;

    // indicates current data is empty data produced for an outer join
    boolean isCurrentOuter;

    /**
     * Constructor declaration
     *
     *
     * @param t
     * @param alias
     * @param outerjoin
     */
    TableFilter(Table t, String alias, boolean outerjoin) {

        tTable      = t;
        sAlias      = (alias != null) ? alias
                                      : t.getName().name;
        isOuterJoin = outerjoin;
        oEmptyData  = tTable.getNewRow();
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    String getName() {
        return sAlias;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    Table getTable() {
        return tTable;
    }

    /**
     * Retrieves a CONDITION_XXX code indicating the possible use for an
     * expression of the given type, relative to a TableFilter.
     *
     * @param exprType an expression type code
     * @return
     */
    static final int toConditionType(int exprType) {

        switch (exprType) {

            case Expression.NOT_EQUAL :
            case Expression.LIKE :    // todo: maybe use index
            case Expression.IN : {
                return CONDITION_UNORDERED;
            }
            case Expression.EQUAL : {
                return CONDITION_START_END;
            }
            case Expression.BIGGER :
            case Expression.BIGGER_EQUAL : {
                return CONDITION_START;
            }
            case Expression.SMALLER :
            case Expression.SMALLER_EQUAL : {
                return CONDITION_END;
            }
            default : {

                // not a condition so forget it
                return CONDITION_NONE;
            }
        }
    }

    // TODO: Optimize
    //
    // The current way always chooses eStart, eEnd conditions
    // using first encountered eligible index
    //
    // We should check if current index offers better selectivity/access
    // path than previously assigned iIndex.
    //
    // EXAMPLE 1:
    //
    // CREATE TABLE t (c1 int, c2 int primary key)
    // CREATE INDEX I1 ON t(c1)
    // SELECT
    //      *
    // FROM
    //      t
    // WHERE
    //     c1 = | < | <= | >= | > ...
    // AND
    //     c2 = | < | <= | >= | > ...
    //
    // currently always chooses iIndex / condition (c1/I1), over
    // index / condition (c2/pk), whereas index / condition (c2/pk)
    // may well be better, especially if condition on c2 is equality
    // (condition_start_end) and conditionon(s) on c1 involve range
    // (condition_start, condition_end, or some composite).
    //
    // Currently, the developer/client software must somehow know facts
    // both about the table, the query and the way HSQLDB forms its
    // plans and, based on this knowlege, perhaps decide to reverse
    // order by explicitly issuing instead:
    //
    // SELECT
    //      *
    // FROM
    //      t
    // WHERE
    //     c2 = | < | <= | >= | > ...
    // AND
    //     c1 = | < | <= | >= | > ...
    //
    // to get optimal index choice.
    //
    // The same thing applies to and is even worse for joins.
    //
    // Consider the following (highly artificial, but easy to
    // understand) case:
    //
    // CREATE TABLE T1(ID INTEGER PRIMARY KEY, C1 INTEGER)
    // CREATE INDEX I1 ON T1(C1)
    // CREATE TABLE T2(ID INTEGER PRIMARY KEY, C1 INTEGER)
    // CREATE INDEX I2 ON T2(C1)
    //
    // select * from t1, t2 where t1.c1 = t2.c1 and t1.id = t2.id
    //
    // Consider the worst value distribution where t1 and t2 are both
    // 10,000 rows, c1 selectivity is nil (all values are identical)
    // for both tables, and, say, id values span the range 0..9999
    // for both tables.
    //
    // Then time to completion on 500 MHz Athlon testbed using memory
    // tables is:
    //
    // 10000 row(s) in 309114 ms
    //
    // whereas for:
    //
    // select * from t1, t2 where t1.id = t2.id and t1.c1 = t2.c1
    //
    // time to completion is:
    //
    // 10000 row(s) in 471 ms
    //
    // Hence, the unoptimized query takes 656 times as long as the
    // optimized one!!!
    //
    // EXAMPLE 2:
    //
    // If there are, say, two non-unique candidate indexes,
    // and some range or equality predicates against
    // them, preference should be given to the one with
    // better selectivity (if the total row count of the
    // table is large, otherwise the overhead of making
    // the choice is probably large w.r.t. any possible
    // savings).  Might require maintaining some basic
    // statistics or performing appropriate index probes
    // at the time the plan is being generated.

    /**
     * Chooses certain query conditions and assigns a copy of them to this
     * filter. The original condition is set to Expression.TRUE once assigned.
     *
     * @param e
     *
     * @throws HsqlException
     */
    void setCondition(Expression e) throws HsqlException {

        int        type = e.getType();
        Expression e1   = e.getArg();
        Expression e2   = e.getArg2();

        if (type == Expression.AND) {
            setCondition(e1);
            setCondition(e2);

            return;
        }

        int conditionType = toConditionType(type);

        if (conditionType == CONDITION_NONE) {

            // not a condition expression
            return;
        }

// fredt@users 20030813 - patch 1.7.2 - fix for column comparison within same table bugs #572075 and 722443
        if (e1.getFilter() == this && e2.getFilter() == this) {
            conditionType = CONDITION_UNORDERED;
        } else if (e1.getFilter() == this) {    // ok include this
        } else if ((e2.getFilter() == this)
                   && (conditionType != CONDITION_UNORDERED)) {

            // swap and try again to allow index usage
            e.swapCondition();
            setCondition(e);

            return;
        } else {

            // unrelated: don't include
            return;
        }

        Trace.doAssert(e1.getFilter() == this, "setCondition");

        if (!e2.isResolved()) {
            return;
        }

        if (conditionType == CONDITION_UNORDERED) {
            addAndCondition(e);

            return;
        }

        int   i     = e1.getColumnNr();
        Index index = tTable.getIndexForColumn(i);

        if (index == null || (iIndex != index && iIndex != null)) {
            addAndCondition(e);

            return;
        }

        iIndex = index;

        switch (conditionType) {

            case CONDITION_START_END : {

                // candidate for both start & end
                if ((eStart != null) || (eEnd != null)) {
                    addAndCondition(e);

                    return;
                }

                eStart = new Expression(e);
                eEnd   = eStart;

                break;
            }
            case CONDITION_START : {

                // candidate for start
                if (eStart != null) {
                    addAndCondition(e);

                    return;
                }

                eStart = new Expression(e);

                break;
            }
            case CONDITION_END : {

                // candidate for end
                if (eEnd != null) {
                    addAndCondition(e);

                    return;
                }

                eEnd = new Expression(e);

                break;
            }
        }

        e.setTrue();
    }

    /**
     * This simply finds the first row in the table (using
     * an index if there is one) and checks it against the eEnd (range) and
     * eAnd (other conditions) Expression objects. (fredt)
     *
     * @return true if row was found
     */
    boolean findFirst() throws HsqlException {

        nonJoinIsNull  = false;
        isCurrentOuter = false;

        if (iIndex == null) {
            iIndex = tTable.getPrimaryIndex();
        }

        if (eStart == null) {
            nCurrent = iIndex.first();
        } else {
            int    type = eStart.getArg().getDataType();
            Object o    = eStart.getArg2().getValue(type);

            nCurrent = iIndex.findFirst(o, eStart.getType());
        }

        while (nCurrent != null) {
            oCurrentData = nCurrent.getData();
            currentRow   = nCurrent.getRow();

            if (!(eEnd == null || eEnd.test())) {
                break;
            }

            if (eAnd == null || eAnd.test()) {
                return true;
            }

            nCurrent = iIndex.next(nCurrent);
        }

        oCurrentData = oEmptyData;
        currentRow   = null;

        return false;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @throws HsqlException
     */
    boolean next() throws HsqlException {

        nonJoinIsNull  = false;
        isCurrentOuter = false;
        nCurrent       = iIndex.next(nCurrent);

        while (nCurrent != null) {
            oCurrentData = nCurrent.getData();
            currentRow   = nCurrent.getRow();

            if (!(eEnd == null || eEnd.test())) {
                break;
            }

            if (eAnd == null || eAnd.test()) {
                return true;
            }

            nCurrent = iIndex.next(nCurrent);
        }

        oCurrentData = oEmptyData;
        currentRow   = null;

        return false;
    }

    boolean nextOuter() throws HsqlException {

        nonJoinIsNull  = false;
        isCurrentOuter = true;
        oCurrentData   = oEmptyData;
        currentRow     = null;

        return eAnd == null || eAnd.test();
    }

    /**
     * Method declaration
     *
     *
     * @param e
     */
    private void addAndCondition(Expression e) {

        Expression e2 = new Expression(e);

        if (eAnd == null) {
            eAnd = e2;
        } else {
            Expression and = new Expression(Expression.AND, eAnd, e2);

            eAnd = and;
        }

        e.setTrue();
    }

// boucheb@users 20030415 - added for debugging support
    public String toString() {

        StringBuffer sb;
        Index        index;
        Index        primaryIndex;
        int[]        primaryKey;
        boolean      hidden;
        boolean      fullScan;

        sb           = new StringBuffer();
        index        = iIndex;
        primaryIndex = tTable.getPrimaryIndex();
        primaryKey   = tTable.getPrimaryKey();
        hidden       = false;
        fullScan     = (eStart == null && eEnd == null);

        if (index == null) {
            index = primaryIndex;
        }

        if (index == primaryIndex && primaryKey == null) {
            hidden   = true;
            fullScan = true;
        }

        sb.append(super.toString()).append('\n');
        sb.append("table=[").append(tTable.getName().name).append("]\n");
        sb.append("alias=[").append(sAlias).append("]\n");
        sb.append("access=[").append(fullScan ? "FULL SCAN"
                                              : "INDEX PRED").append("]\n");
        sb.append("index=[");
        sb.append(index == null ? null
                                : index.getName().name);
        sb.append(hidden ? "[HIDDEN]]\n"
                         : "]\n");
        sb.append("bOuterJoin=[").append(isOuterJoin).append("]\n");
        sb.append("eStart=[").append(eStart).append("]\n");
        sb.append("eEnd=[").append(eEnd).append("]\n");
        sb.append("eAnd=[").append(eAnd).append("]\n");
        sb.append("sSelect=[").append(sSelect).append("]");

        return sb.toString();
    }
}
