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

/**
 * A simple structure class for holding the products of
 * statement compilation for later execution.
 *
 * @author  boucherb@users.sourceforge.net
 * @vesrion 1.7.2
 * @since HSQLDB 1.7.2
 */
public class CompiledStatement {

    // enumeration of CompiledStatement types
    static final int UNKNOWN       = 0;
    static final int INSERT_VALUES = 1;
    static final int INSERT_SELECT = 2;
    static final int UPDATE        = 3;
    static final int DELETE        = 4;
    static final int SELECT        = 5;
    static final int CALL          = 6;

    /** target table for INSERT_XXX, UPDATE and DELETE */
    Table       targetTable;
    TableFilter tf;

    /** condition expression for UPDATE and DELETE */
    Expression condition;

    /** column map for INSERT_XXX, UPDATE */
    int[] columnMap;

    /** Column value Expressions for INSERT_VALUES and UPDATE. */
    Expression[] columnValues;

    /**
     * Flags indicating which columns' values will/will not be
     * explicitly set.
     */
    boolean[] checkColumns;

    /** Expression to be evaluated when this is a CALL statement. */
    Expression expression;

    /**
     * Select to be evaluated when this is an INSERT_SELECT or
     * SELECT statement
     */
    Select select;

    /**
     * Parse-order array of Expression objects, all of iType == PARAM ,
     * involved in some way in any INSERT_XXX, UPDATE, DELETE, SELECT or
     * CALL CompiledStatement
     */
    Expression[] parameters;

    /**
     * int[] contains type of each parameter
     */
    int[] paramTypes;

    /**
     * Subqueries in heaped inverse parse depth order
     */
    Parser.SubQuery[] subqueries;

    /**
     * The type of this CompiledStatement. <p>
     *
     * One of: <p>
     *
     * <ol>
     *  <li>UNKNOWN
     *  <li>INSERT_VALUES
     *  <li>INSERT_SELECT
     *  <li>UPDATE
     *  <li>DELETE
     *  <li>SELECT
     *  <li>CALL
     * </ol>
     */
    int type;

    /**
     * The id of this compiled statement
     */
    int id;

    /**
     * The use count for this compiled statement
     */
    int use;

    /**
     * The SQL string that produced this compiled statement
     */
    String sql;

    /** Creates a new instance of CompiledStatement */
    CompiledStatement() {
        clearAll();
    }

    void bind(Object[] values, int[] types) throws HsqlException {

        int len;

        Trace.check(parameters != null, Trace.COLUMN_COUNT_DOES_NOT_MATCH);

        len = parameters.length;

        Trace.check(values.length >= len && types.length >= len,
                    Trace.COLUMN_COUNT_DOES_NOT_MATCH);

        for (int i = 0; i < len; i++) {
            parameters[i].bind(values[i], types[i]);
        }
    }

    void clearAll() {

        checkColumns = null;
        columnMap    = null;
        columnValues = null;
        condition    = null;
        parameters   = null;
        paramTypes   = null;
        select       = null;
        targetTable  = null;
        type         = UNKNOWN;
        id           = UNKNOWN;
    }

    /**
     * Initializes this as a DELETE statement
     *
     * @param targetTable
     * @param deleteCondition
     * @param parameters
     */
    void setAsDelete(Table targetTable, Expression deleteCondition,
                     Expression[] parameters) throws HsqlException {

        clearAll();

        this.targetTable = targetTable;

        setParameters(parameters);

        tf = new TableFilter(targetTable, null, false);

        if (deleteCondition != null) {
            condition = new Expression(deleteCondition);

            condition.resolve(tf);
            tf.setCondition(condition);
        }

        type = DELETE;
    }

    /**
     * Initializes this as an UPDATE statement.
     *
     * @param targetTable
     * @param columnMap
     * @param columnValues
     * @param updateCondition
     * @param parameters
     */
    void setAsUpdate(Table targetTable, int[] columnMap,
                     Expression[] columnValues, Expression updateCondition,
                     Expression[] parameters) throws HsqlException {

        clearAll();

        this.targetTable  = targetTable;
        this.columnMap    = columnMap;
        this.columnValues = columnValues;
        this.checkColumns = checkColumns;

        setParameters(parameters);

        tf = new TableFilter(targetTable, null, false);

        for (int i = 0; i < columnValues.length; i++) {
            Expression cve = columnValues[i];

            cve.resolve(tf);
        }

        if (updateCondition != null) {
            condition = new Expression(updateCondition);

            condition.resolve(tf);
            tf.setCondition(condition);
        }

        type = UPDATE;
    }

    /**
     * Initializes this as an INSERT_VALUES statement.
     *
     * @param targetTable
     * @param columnMap
     * @param columnValues
     * @param checkColumns
     * @param parameters
     */
    void setAsInsertValues(Table targetTable, int[] columnMap,
                           Expression[] columnValues, boolean[] checkColumns,
                           Expression[] parameters) {

        clearAll();

        this.targetTable  = targetTable;
        this.columnMap    = columnMap;
        this.checkColumns = checkColumns;
        this.columnValues = columnValues;

        setParameters(parameters);

        type = INSERT_VALUES;
    }

    /**
     * Initializes this as an INSERT_SELECT statement.
     *
     * @param targetTable
     * @param columnMap
     * @param checkColumns
     * @param select
     * @param parameters
     */
    void setAsInsertSelect(Table targetTable, int[] columnMap,
                           boolean[] checkColumns, Select select,
                           Expression[] parameters) throws HsqlException {

        clearAll();

        this.targetTable  = targetTable;
        this.columnMap    = columnMap;
        this.checkColumns = checkColumns;
        this.select       = select;

        select.resolveAll();
        setParameters(parameters);

        type = INSERT_SELECT;
    }

    /**
     * Initializes this as a SELECT statement.
     *
     * @param select
     * @param parameters
     */
    void setAsSelect(Select select,
                     Expression[] parameters) throws HsqlException {

        this.select = select;

        select.resolveAll();
        setParameters(parameters);

        type = SELECT;
    }

    /**
     * Initializes this as a CALL statement.
     *
     * @param expression
     * @param parameters
     */
    void setAsCall(Expression expression, Expression[] parameters) {

        this.expression = expression;

        setParameters(parameters);

        type = CALL;
    }

    private void setParameters(Expression[] parameters) {

        this.parameters = parameters;

        int[] types = new int[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            types[i] = parameters[i].getType();
        }

        this.paramTypes = types;
    }

    /**
     * Retrieves a String representation of this object.
     *
     * @return  the String representation of this object
     */
    public String toString() {

        try {
            return toStringImpl();
        } catch (Exception e) {
            e.printStackTrace();

            return e.toString();
        }
    }

    /**
     * Provides the toString() implementation.
     *
     * @throws Exception if a database access or io error occurs
     * @return the String representation of this object
     */
    private String toStringImpl() throws Exception {

        StringBuffer sb;

        sb = new StringBuffer();

        switch (type) {

            case SELECT : {
                sb.append(select.toString());
                appendSubqueries(sb);

                return sb.toString();
            }
            case INSERT_VALUES : {
                sb.append("INSERT VALUES");
                sb.append('[').append('\n');
                appendColumns(sb).append('\n');
                appendTable(sb).append('\n');
                appendParms(sb).append('\n');
                appendSubqueries(sb).append(']');

                return sb.toString();
            }
            case INSERT_SELECT : {
                sb.append("INSERT SELECT");
                sb.append('[').append('\n');
                appendColumns(sb).append('\n');
                appendTable(sb).append('\n');
                sb.append(select).append('\n');
                appendParms(sb).append('\n');
                appendSubqueries(sb).append(']');

                return sb.toString();
            }
            case UPDATE : {
                sb.append("UPDATE");
                sb.append('[').append('\n');
                appendColumns(sb).append('\n');
                appendTable(sb).append('\n');
                sb.append(tf).append('\n');
                appendParms(sb).append('\n');
                appendSubqueries(sb).append(']');

                return sb.toString();
            }
            case DELETE : {
                sb.append("DELETE");
                sb.append('[').append('\n');
                appendTable(sb).append('\n');
                sb.append(tf).append('\n');
                appendParms(sb).append('\n');
                appendSubqueries(sb).append(']');

                return sb.toString();
            }
            case CALL : {
                sb = new StringBuffer();

                sb.append("CALL");
                sb.append('[');
                sb.append(expression);
                appendParms(sb).append('\n');
                appendSubqueries(sb).append(']');

                return sb.toString();
            }
            default : {
                return "UNKNOWN";
            }
        }
    }

    private StringBuffer appendSubqueries(StringBuffer sb) {

        sb.append("SUBQUERIES[");

        for (int i = 0; i < subqueries.length; i++) {
            sb.append("\n[level=").append(subqueries[i].level).append(
                '\n').append("org.hsqldb.Select@").append(
                Integer.toHexString(subqueries[i].select.hashCode())).append(
                "]");
        }

        sb.append(']');

        return sb;
    }

    private StringBuffer appendTable(StringBuffer sb) {

        sb.append("TABLE[").append(targetTable.getName().name).append(']');

        return sb;
    }

    private StringBuffer appendColumns(StringBuffer sb) {

        sb.append("COLUMNS=[");

        for (int i = 0; i < columnMap.length; i++) {
            sb.append('\n').append(columnMap[i]).append(':').append(
                ' ').append(
                targetTable.getColumn(columnMap[i]).columnName.name).append(
                '[').append(columnValues[i]).append(']');
        }

        sb.append(']');

        return sb;
    }

    private StringBuffer appendParms(StringBuffer sb) {

        if (parameters == null || parameters.length == 0) {
            return sb;
        }

        sb.append("PARAMETERS=[");

        for (int i = 0; i < parameters.length; i++) {
            sb.append('\n').append('@').append(i).append('[').append(
                parameters[i]).append(']');
        }

        sb.append(']');

        return sb;
    }
}
