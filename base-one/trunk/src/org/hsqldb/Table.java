/* Copyright (c) 1995-2000, The Hypersonic SQL Group.
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
 * Neither the name of the Hypersonic SQL Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE HYPERSONIC SQL GROUP, 
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software consists of voluntary contributions made by many individuals 
 * on behalf of the Hypersonic SQL Group.
 *
 *
 * For work added by the HSQL Development Group:
 *
 * Copyright (c) 2001-2005, The HSQL Development Group
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

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.hsqldb.HsqlNameManager.HsqlName;
import org.hsqldb.index.RowIterator;
import org.hsqldb.lib.ArrayUtil;
import org.hsqldb.lib.HashMappedList;
import org.hsqldb.lib.HashSet;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.Iterator;
import org.hsqldb.lib.StringUtil;
import org.hsqldb.persist.CachedObject;
import org.hsqldb.persist.DataFileCache;
import org.hsqldb.persist.PersistentStore;
import org.hsqldb.rowio.RowInputInterface;
import org.hsqldb.store.ValuePool;

// fredt@users 20020130 - patch 491987 by jimbag@users - made optional
// fredt@users 20020405 - patch 1.7.0 by fredt - quoted identifiers
// for sql standard quoted identifiers for column and table names and aliases
// applied to different places
// fredt@users 20020225 - patch 1.7.0 - restructuring
// some methods moved from Database.java, some rewritten
// changes to several methods
// fredt@users 20020225 - patch 1.7.0 - ON DELETE CASCADE
// fredt@users 20020225 - patch 1.7.0 - named constraints
// boucherb@users 20020225 - patch 1.7.0 - multi-column primary keys
// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
// tony_lai@users 20020820 - patch 595099 - user defined PK name
// tony_lai@users 20020820 - patch 595172 - drop constraint fix
// kloska@users 20021030 - patch 1.7.2 - ON UPDATE CASCADE | SET NULL | SET DEFAULT
// kloska@users 20021112 - patch 1.7.2 - ON DELETE SET NULL | SET DEFAULT
// fredt@users 20021210 - patch 1.7.2 - better ADD / DROP INDEX for non-CACHED tables
// fredt@users 20030901 - patch 1.7.2 - allow multiple nulls for UNIQUE columns
// fredt@users 20030901 - patch 1.7.2 - reworked IDENTITY support
// achnettest@users 20040130 - patch 878288 - bug fix for new indexes in memory tables by Arne Christensen
// boucherb@users 20040327 - doc 1.7.2 - javadoc updates
// boucherb@users 200404xx - patch 1.7.2 - proper uri for getCatalogName
// fredt@users 20050000 - 1.8.0 updates in several areas
// fredt@users 20050220 - patch 1.8.0 enforcement of DECIMAL precision/scale

/**
 *  Holds the data structures and methods for creation of a database table.
 *
 *
 * @version 1.8.0
 */
public class Table extends BaseTable {

    // types of table
    public static final int SYSTEM_TABLE    = 0;
    public static final int SYSTEM_SUBQUERY = 1;
    public static final int TEMP_TABLE      = 2;
    public static final int MEMORY_TABLE    = 3;
    public static final int CACHED_TABLE    = 4;
    public static final int TEMP_TEXT_TABLE = 5;
    public static final int TEXT_TABLE      = 6;
    public static final int VIEW            = 7;

// boucherb@users - for future implementation of SQL standard INFORMATION_SCHEMA
    static final int SYSTEM_VIEW = 8;

    // name of the column added to tables without primary key
    static final String DEFAULT_PK = "";

    // main properties
// boucherb@users - access changed in support of metadata 1.7.2
    public HashMappedList columnList;                 // columns in table
    Index[]               indexList;                  // vIndex(0) is the primary key index
    private int[]         primaryKeyCols;             // column numbers for primary key
    private int[]         primaryKeyTypes;            // types for primary key
    private int[]         primaryKeyColsSequence;     // {0,1,2,...}
    int[]                 bestRowIdentifierCols;      // column set for best index
    boolean               bestRowIdentifierStrict;    // true if it has no nullable column
    int[]                 bestIndexForColumn;         // index of the 'best' index for each column
    Index                 bestIndex;                  // the best index overall - null if there is no user-defined index
    boolean        needsRowID;
    int[]          nullRowIDCols;
    int            identityColumn;                    // -1 means no such row
    NumberSequence identitySequence;                  // next value of identity column
    NumberSequence rowIdSequence;                     // next value of optional rowid

// -----------------------------------------------------------------------
    Constraint[]      constraintList;                 // constrainst for the table
    HsqlArrayList[]   triggerLists;                   // array of trigger lists
    private int[]     colTypes;                       // fredt - types of columns
    private int[]     colSizes;                       // fredt - copy of SIZE values for columns
    private int[]     colScales;                      // fredt - copy of SCALE values for columns
    private boolean[] colNullable;                    // fredt - modified copy of isNullable() values
    private Expression[] colDefaults;                 // fredt - expressions of DEFAULT values
    private int[]        defaultColumnMap;            // fred - holding 0,1,2,3,...
    private boolean      hasDefaultValues;            //fredt - shortcut for above
    private boolean      isText;
    private boolean      isView;
    boolean              sqlEnforceSize;              // inherited for the database -
    boolean              sqlEnforceStrictSize;        // inherited for the database -

    // properties for subclasses
    protected int           columnCount;              // inclusive the hidden primary key
    protected int           visibleColumnCount;       // exclusive of hidden primary key
    public Database         database;
    protected DataFileCache cache;
    protected HsqlName      tableName;                // SQL name
    private int             tableType;
    protected int           ownerSessionId;           // fredt - set for temp tables only
    protected boolean       isReadOnly;
    protected boolean       isTemp;
    protected boolean       isCached;
    protected int           indexType;                // fredt - type of index used

    //
    PersistentStore rowStore;

    /**
     *  Constructor
     *
     * @param  db
     * @param  name
     * @param  type
     * @param  sessionid
     * @exception  HsqlException
     */
    Table(Database db, HsqlName name, int type,
            int sessionid) throws HsqlException {

        database             = db;
        sqlEnforceSize       = db.sqlEnforceSize;
        sqlEnforceStrictSize = db.sqlEnforceStrictSize;
        identitySequence     = new NumberSequence(null, 0, 1, Types.BIGINT);
        rowIdSequence        = new NumberSequence(null, 0, 1, Types.BIGINT);

        switch (type) {

            case SYSTEM_SUBQUERY :
            case SYSTEM_TABLE :
                isTemp = true;
                break;

            case TEMP_TABLE :
                isTemp         = true;
                ownerSessionId = sessionid;
                break;

            case CACHED_TABLE :
                if (!db.logger.hasLog()) {
                    type = MEMORY_TABLE;

                    break;
                }

                cache    = db.logger.getCache();
                isCached = true;
                break;

            case TEMP_TEXT_TABLE :
                if (!db.logger.hasLog()) {
                    throw Trace.error(Trace.DATABASE_IS_MEMORY_ONLY);
                }

                isTemp         = true;
                isText         = true;
                isReadOnly     = true;
                isCached       = true;
                ownerSessionId = sessionid;
                break;

            case TEXT_TABLE :
                if (!db.logger.hasLog()) {
                    throw Trace.error(Trace.DATABASE_IS_MEMORY_ONLY);
                }

                isText   = true;
                isCached = true;
                break;

            case VIEW :
            case SYSTEM_VIEW :
                isView = true;
                break;
        }

        if (isText || isCached) {
            rowStore = new RowStore();
        }

        if (isText) {
            indexType = Index.POINTER_INDEX;
        } else if (isCached) {
            indexType = Index.DISK_INDEX;
        }

        // type may have changed above for CACHED tables
        tableType       = type;
        tableName       = name;
        primaryKeyCols  = null;
        primaryKeyTypes = null;
        identityColumn  = -1;
        columnList      = new HashMappedList();
        indexList       = new Index[0];
        constraintList  = new Constraint[0];
        triggerLists    = new HsqlArrayList[TriggerDef.NUM_TRIGS];

// ----------------------------------------------------------------------------
// akede@users - 1.7.2 patch Files readonly
        // Changing the mode of the table if necessary
        if (db.isFilesReadOnly() && checkTableFileBased()) {
            this.isReadOnly = true;
        }

// ----------------------------------------------------------------------------
    }

    boolean equals(Session session, String name) {

        if (isTemp && (session != null
                       && session.getId() != ownerSessionId)) {
            return false;
        }

        return (tableName.name.equals(name));
    }

    boolean equals(String name) {
        return (tableName.name.equals(name));
    }

    public final boolean isText() {
        return isText;
    }

    public final boolean isTemp() {
        return isTemp;
    }

    public final boolean isReadOnly() {
        return isReadOnly;
    }

    final boolean isSystem() {
        return tableType == SYSTEM_TABLE || tableType == SYSTEM_SUBQUERY
               || tableType == SYSTEM_VIEW;
    }

    final boolean isView() {
        return isView;
    }

    final int getIndexType() {
        return indexType;
    }

    public final int getTableType() {
        return tableType;
    }

    public final boolean isDataReadOnly() {
        return isReadOnly;
    }

    /**
     * Used by INSERT, DELETE, UPDATE operations
     */
    void checkDataReadOnly() throws HsqlException {

        if (isReadOnly) {
            throw Trace.error(Trace.DATA_IS_READONLY);
        }
    }

// ----------------------------------------------------------------------------
// akede@users - 1.7.2 patch Files readonly
    void setDataReadOnly(boolean value) throws HsqlException {

        // Changing the Read-Only mode for the table is only allowed if the
        // the database can realize it.
        if (!value && database.isFilesReadOnly() && checkTableFileBased()) {
            throw Trace.error(Trace.DATA_IS_READONLY);
        }

        isReadOnly = value;
    }

    /**
     * Text or Cached Tables are normally file based
     */
    boolean checkTableFileBased() {
        return isCached || isText;
    }

    /**
     * Retuns the session id for the owner of the table. Significant for temp
     * tables only.
     */
    int getOwnerSessionId() {
        return ownerSessionId;
    }

    /**
     * For text tables
     */
    protected void setDataSource(Session s, String source, boolean isDesc,
                                 boolean newFile) throws HsqlException {
        throw (Trace.error(Trace.TABLE_NOT_FOUND));
    }

    /**
     * For text tables
     */
    protected String getDataSource() {
        return null;
    }

    /**
     * For text tables.
     */
    protected boolean isDescDataSource() {
        return false;
    }

    /**
     * For text tables.
     */
    public void setHeader(String header) throws HsqlException {
        throw Trace.error(Trace.TEXT_TABLE_HEADER);
    }

    /**
     * For text tables.
     */
    public String getHeader() {
        return null;
    }

    /**
     *  Adds a constraint.
     */
    void addConstraint(Constraint c) {

        constraintList =
            (Constraint[]) ArrayUtil.toAdjustedArray(constraintList, c,
                constraintList.length, 1);
    }

    /**
     *  Returns the list of constraints.
     */
    Constraint[] getConstraints() {
        return constraintList;
    }

/** @todo fredt - this can be improved to ignore order of columns in
     * multi-column indexes */

    /**
     *  Returns the index supporting a constraint with the given column signature.
     *  Only Unique constraints are considered.
     */
    Index getConstraintIndexForColumns(int[] col) {

        if (ArrayUtil.areEqual(getPrimaryIndex().getColumns(), col,
                               col.length, true)) {
            return getPrimaryIndex();
        }

        for (int i = 0, size = constraintList.length; i < size; i++) {
            Constraint c = constraintList[i];

            if (c.getType() != Constraint.UNIQUE) {
                continue;
            }

            if (ArrayUtil.areEqual(c.getMainColumns(), col, col.length,
                                   true)) {
                return c.getMainIndex();
            }
        }

        return null;
    }

    /**
     *  Returns any foreign key constraint equivalent to the column sets
     */
    Constraint getConstraintForColumns(Table tablemain, int[] colmain,
                                       int[] colref) {

        for (int i = 0, size = constraintList.length; i < size; i++) {
            Constraint c = constraintList[i];

            if (c.isEquivalent(tablemain, colmain, this, colref)) {
                return c;
            }
        }

        return null;
    }

    /**
     *  Returns any unique Constraint using this index
     *
     * @param  index
     * @return
     */
    Constraint getConstraintForIndex(Index index) {

        for (int i = 0, size = constraintList.length; i < size; i++) {
            Constraint c = constraintList[i];

            if (c.getMainIndex() == index
                    && c.getType() == Constraint.UNIQUE) {
                return c;
            }
        }

        return null;
    }

    /**
     *  Returns the next constraint of a given type
     *
     * @param  from
     * @param  type
     * @return
     */
    int getNextConstraintIndex(int from, int type) {

        for (int i = from, size = constraintList.length; i < size; i++) {
            Constraint c = constraintList[i];

            if (c.getType() == type) {
                return i;
            }
        }

        return -1;
    }

    /**
     *  Low level method to add a column
     *
     * @param  name
     * @param  type
     * @throws  HsqlException
     */
    void addColumn(String name, int type) throws HsqlException {

        Column column =
            new Column(database.nameManager.newHsqlName(name, false), true,
                       type, 0, 0, false, 0, 0, false, null);

        addColumn(column);
    }

// fredt@users 20020220 - patch 475199 - duplicate column

    /**
     *  Performs the table level checks and adds a column to the table at the
     *  DDL level. Only used at table creation, not at alter column.
     */
    void addColumn(Column column) throws HsqlException {

        if (searchColumn(column.columnName.name) >= 0) {
            throw Trace.error(Trace.COLUMN_ALREADY_EXISTS);
        }

        if (column.isIdentity()) {
            Trace.check(
                column.getType() == Types.INTEGER
                || column.getType() == Types.BIGINT, Trace.WRONG_DATA_TYPE,
                    column.columnName.name);
            Trace.check(identityColumn == -1, Trace.SECOND_PRIMARY_KEY,
                        column.columnName.name);

            identityColumn = columnCount;
        }

        Trace.doAssert(primaryKeyCols == null, "Table.addColumn");
        columnList.add(column.columnName.name, column);

        columnCount++;
        visibleColumnCount++;
    }

    /**
     *  Add a set of columns based on a ResultMetaData
     */
    void addColumns(Result.ResultMetaData metadata,
                    int count) throws HsqlException {

        for (int i = 0; i < count; i++) {
            Column column = new Column(
                database.nameManager.newHsqlName(
                    metadata.colLabels[i], metadata.isLabelQuoted[i]), true,
                        metadata.colTypes[i], metadata.colSizes[i],
                        metadata.colScales[i], false, 0, 0, false, null);

            addColumn(column);
        }
    }

    /**
     *  Adds a set of columns based on a compiled Select
     */
    void addColumns(Select select) throws HsqlException {

        int colCount = select.iResultLen;

        for (int i = 0; i < colCount; i++) {
            Expression e = select.exprColumns[i];
            Column column = new Column(
                database.nameManager.newHsqlName(
                    e.getAlias(), e.isAliasQuoted()), true, e.getDataType(),
                        e.getColumnSize(), e.getColumnScale(), false, 0, 0,
                        false, null);

            addColumn(column);
        }
    }

    /**
     *  Returns the HsqlName object fo the table
     */
    public HsqlName getName() {
        return tableName;
    }

    /**
     * Changes table name. Used by 'alter table rename to'.
     * Essential to use the existing HsqlName as this is is referenced by
     * intances of Constraint etc.
     */
    void renameTable(String newname, boolean isquoted) throws HsqlException {

        String oldname = tableName.name;

        tableName.rename(newname, isquoted);

        if (HsqlName.isReservedIndexName(getPrimaryIndex().getName().name)) {
            getPrimaryIndex().getName().rename("SYS_PK", newname, isquoted);
        }

        renameTableInCheckConstraints(oldname, newname);
    }

    /**
     *  Returns total column counts, including hidden ones.
     */
    int getInternalColumnCount() {
        return columnCount;
    }

    /**
     * returns a basic duplicate of the table without the data structures.
     */
    protected Table duplicate() throws HsqlException {

        Table t = (new Table(database, tableName, tableType, ownerSessionId));

        return t;
    }

    /**
     * Match two columns arrays for length and type of columns
     *
     * @param col column array from this Table
     * @param other the other Table object
     * @param othercol column array from the other Table
     * @throws HsqlException if there is a mismatch
     */
    void checkColumnsMatch(int[] col, Table other,
                           int[] othercol) throws HsqlException {

        if (col.length != othercol.length) {
            throw Trace.error(Trace.COLUMN_COUNT_DOES_NOT_MATCH);
        }

        for (int i = 0; i < col.length; i++) {

            // integrity check - should not throw in normal operation
            if (col[i] >= columnCount || othercol[i] >= other.columnCount) {
                throw Trace.error(Trace.COLUMN_COUNT_DOES_NOT_MATCH);
            }

            if (getColumn(col[i]).getType()
                    != other.getColumn(othercol[i]).getType()) {
                throw Trace.error(Trace.COLUMN_TYPE_MISMATCH);
            }
        }
    }

// fredt@users 20020405 - patch 1.7.0 by fredt - DROP and CREATE INDEX bug

    /**
     * DROP INDEX and CREATE INDEX on non empty tables both recreate the table
     * and the data to reflect the new indexing structure. The new structure
     * should be reflected in the DDL script, otherwise if a
     * SHUTDOWN IMMEDIATELY occures, the following will happen:<br>
     *
     * <ul>
     * <li>If the table is cached, the index roots will be different from what
     *     is specified in SET INDEX ROOTS. <p>
     *
     * <li>If the table is memory, the old index will be used until the script
     *     reaches drop index etc. and data is recreated again. <p>
     *
     * <ul>
     *
     * The fix avoids scripting the row insert and delete ops. <p>
     *
     * Constraints that need removing are removed outside this method.<br>
     * withoutindex is the name of an index to be removed <br>
     * adjust {-1 | 0 | +1} indicates if a column {removed | no change | added}
     */
    Table moveDefinition(String withoutindex, Column newcolumn, int colindex,
                         int adjust) throws HsqlException {

        Table tn = duplicate();

        // loop beyond the end in order to be able to add a column to the end
        // of the list
        for (int i = 0; i < visibleColumnCount + 1; i++) {
            if (i == colindex) {
                if (adjust > 0) {
                    tn.addColumn(newcolumn);
                } else if (adjust < 0) {
                    continue;
                }
            }

            if (i == visibleColumnCount) {
                break;
            }

            tn.addColumn(getColumn(i));
        }

        // treat it the same as new table creation and
        int[] primarykey = primaryKeyCols.length == 0 ? null
                                                      : primaryKeyCols;

        if (primarykey != null) {
            int[] newpk = ArrayUtil.toAdjustedColumnArray(primarykey,
                colindex, adjust);

            // fredt - we don't drop pk column
            // although we _can_ drop single column pk wih no fk reference
            if (primarykey.length != newpk.length) {
                throw Trace.error(Trace.DROP_PRIMARY_KEY);
            } else {
                primarykey = newpk;
            }
        }

// tony_lai@users - 20020820 - patch 595099 - primary key names
        tn.createPrimaryKey(getIndex(0).getName(), primarykey, false);

        tn.constraintList = constraintList;

        for (int i = 1; i < getIndexCount(); i++) {
            Index idx = getIndex(i);

            if (withoutindex != null
                    && idx.getName().name.equals(withoutindex)) {
                continue;
            }

            Index newidx = tn.createAdjustedIndex(idx, colindex, adjust);

            if (newidx == null) {

                // column to remove is part of an index
                throw Trace.error(Trace.Table_moveDefinition);
            }
        }

        tn.triggerLists = triggerLists;

        return tn;
    }

    /**
     * cols == null means drop
     */
    Table moveDefinitionPK(HsqlName name, int[] pkCols) throws HsqlException {

        // some checks
        if ((hasPrimaryKey() && pkCols != null)
                || (!hasPrimaryKey() && pkCols == null)) {
            throw Trace.error(Trace.DROP_PRIMARY_KEY);
        }

        Table tn = duplicate();

        for (int i = 0; i < visibleColumnCount; i++) {
            tn.addColumn(getColumn(i).duplicate());
        }

        tn.createPrimaryKey(name, pkCols, true);

        tn.constraintList = constraintList;

        for (int i = 1; i < getIndexCount(); i++) {
            Index idx = getIndex(i);

            tn.createAdjustedIndex(idx, -1, 0);
        }

        tn.triggerLists = triggerLists;

        return tn;
    }

    /**
     * Updates the constraint and replaces references to the old table with
     * the new one, adjusting column index arrays by the given amount.
     */
    void updateConstraintsTables(Table old, int colindex,
                                 int adjust) throws HsqlException {

        for (int i = 0, size = constraintList.length; i < size; i++) {
            Constraint c = constraintList[i];

            c.replaceTable(old, this, colindex, adjust);

            if (c.constType == Constraint.CHECK) {
                recompileCheckConstraint(c);
            }
        }
    }

    private void recompileCheckConstraints() throws HsqlException {

        for (int i = 0, size = constraintList.length; i < size; i++) {
            Constraint c = constraintList[i];

            if (c.constType == Constraint.CHECK) {
                recompileCheckConstraint(c);
            }
        }
    }

    /**
     * Used after adding columns or indexes to the table.
     */
    private void recompileCheckConstraint(Constraint c) throws HsqlException {

        String    ddl       = c.core.check.getDDL();
        Tokenizer tokenizer = new Tokenizer(ddl);
        Parser parser =
            new Parser(database.getSessionManager().getSysSession(),
                       database, tokenizer);
        Expression condition = parser.parseExpression();

        c.core.check = condition;

        // this workaround is here to stop LIKE optimisation (for proper scripting)
        condition.setLikeOptimised();

        Select s = Expression.getCheckSelect(this, condition);

        c.core.checkFilter = s.tFilter[0];

        c.core.checkFilter.setAsCheckFilter();

        c.core.mainTable = this;
    }

    /**
     * Used for drop column.
     */
    void checkColumnInCheckConstraint(String colname) throws HsqlException {

        for (int i = 0, size = constraintList.length; i < size; i++) {
            Constraint c = constraintList[i];

            if (c.constType == Constraint.CHECK) {
                if (c.hasColumn(this, colname)) {
                    throw Trace.error(Trace.COLUMN_IS_REFERENCED,
                                      c.getName());
                }
            }
        }
    }

    /**
     * Used for rename column.
     */
    private void renameColumnInCheckConstraints(String oldname,
            String newname, boolean isquoted) throws HsqlException {

        for (int i = 0, size = constraintList.length; i < size; i++) {
            Constraint c = constraintList[i];

            if (c.constType == Constraint.CHECK) {
                Expression.Collector coll = new Expression.Collector();

                coll.addAll(c.core.check, Expression.COLUMN);

                Iterator it = coll.iterator();

                for (; it.hasNext(); ) {
                    Expression e = (Expression) it.next();

                    if (e.getColumnName() == oldname) {
                        e.setColumnName(newname, isquoted);
                    }
                }
            }
        }
    }

    /**
     * Used for drop column.
     */
    private void renameTableInCheckConstraints(String oldname,
            String newname) throws HsqlException {

        for (int i = 0, size = constraintList.length; i < size; i++) {
            Constraint c = constraintList[i];

            if (c.constType == Constraint.CHECK) {
                Expression.Collector coll = new Expression.Collector();

                coll.addAll(c.core.check, Expression.COLUMN);

                Iterator it = coll.iterator();

                for (; it.hasNext(); ) {
                    Expression e = (Expression) it.next();

                    if (e.getTableName() == oldname) {
                        e.setTableName(newname);
                    }
                }
            }
        }

        recompileCheckConstraints();
    }

    /**
     *  Returns the count of user defined columns.
     */
    public int getColumnCount() {
        return visibleColumnCount;
    }

    /**
     *  Returns the count of all columns.
     */
    public int getInvisibleColumnCount() {
        return columnCount;
    }

    /**
     *  Returns the count of indexes on this table.
     */
    public int getIndexCount() {
        return indexList.length;
    }

    /**
     *  Returns the identity column or null.
     */
    int getIdentityColumn() {
        return identityColumn;
    }

    /**
     *  Returns the index of given column name or throws if not found
     */
    int getColumnNr(String c) throws HsqlException {

        int i = searchColumn(c);

        if (i == -1) {
            throw Trace.error(Trace.COLUMN_NOT_FOUND, c);
        }

        return i;
    }

    /**
     *  Returns the index of given column name or -1 if not found.
     */
    int searchColumn(String c) {

        int index = columnList.getIndex(c);

        return index;
    }

    /**
     *  Returns the primary index (user defined or system defined)
     */
    public Index getPrimaryIndex() {
        return getIndex(0);
    }

    /**
     *  Return the user defined primary key column indexes, or empty array for system PK's.
     */
    public int[] getPrimaryKey() {
        return primaryKeyCols;
    }

    public int[] getPrimaryKeyTypes() {
        return primaryKeyTypes;
    }

    public boolean hasPrimaryKey() {
        return !(primaryKeyCols.length == 0);
    }

    int[] getBestRowIdentifiers() {
        return bestRowIdentifierCols;
    }

    boolean isBestRowIdentifiersStrict() {
        return bestRowIdentifierStrict;
    }

    /**
     * This method is called whenever there is a change to table structure and
     * serves two porposes: (a) to reset the best set of columns that identify
     * the rows of the table (b) to reset the best index that can be used
     * to find rows of the table given a column value.
     *
     * (a) gives most weight to a primary key index, followed by a unique
     * address with the lowest count of nullable columns. Otherwise there is
     * no best row identifier.
     *
     * (b) finds for each column an index with a corresponding first column.
     * It uses any type of visible index and accepts the first one (it doesn't
     * make any difference to performance).
     *
     * bestIndex is the user defined, primary key, the first unique index, or
     * the first non-unique index. NULL if there is no user-defined index.
     *
     */
    private void setBestRowIdentifiers() {

        int[]   briCols      = null;
        int     briColsCount = 0;
        boolean isStrict     = false;
        int     nNullCount   = 0;

        // ignore if called prior to completion of primary key construction
        if (colNullable == null) {
            return;
        }

        bestIndex          = null;
        bestIndexForColumn = new int[columnList.size()];
        nullRowIDCols      = new int[columnList.size()];

        for (int i = 0; i < bestIndexForColumn.length; i++) {
            nullRowIDCols[i] = bestIndexForColumn[i] = -1;
        }

        for (int i = 0; i < indexList.length; i++) {
            Index index     = indexList[i];
            int[] cols      = index.getColumns();
            int   colsCount = index.getVisibleColumns();

            if (i == 0) {

                // ignore system primary keys
                if (hasPrimaryKey()) {
                    isStrict = true;
                } else {
                    continue;
                }
            }

            if (bestIndexForColumn[cols[0]] == -1) {
                bestIndexForColumn[cols[0]] = i;
            }

            if (!index.isUnique()) {
                if (bestIndex == null) {
                    bestIndex = index;
                }

                continue;
            }

            int nnullc = 0;

            for (int j = 0; j < colsCount; j++) {
                if (!colNullable[cols[j]]) {
                    nnullc++;
                } else {

                    // set the nullable column of unique address
                    nullRowIDCols[cols[j]] = cols[j];
                }
            }

            if (bestIndex != null) {
                bestIndex = index;
            }

            if (nnullc == colsCount) {
                if (briCols == null || briColsCount != nNullCount
                        || colsCount < briColsCount) {

                    //  nothing found before ||
                    //  found but has null columns ||
                    //  found but has more columns than this index
                    briCols      = cols;
                    briColsCount = colsCount;
                    nNullCount   = colsCount;
                    isStrict     = true;
                }

                continue;
            } else if (isStrict) {
                continue;
            } else if (briCols == null || colsCount < briColsCount
                       || nnullc > nNullCount) {

                //  nothing found before ||
                //  found but has more columns than this index||
                //  found but has fewer not null columns than this index
                briCols      = cols;
                briColsCount = colsCount;
                nNullCount   = nnullc;
            }
        }

        // remove rowID column from bestRowIdentiferCols
        bestRowIdentifierCols = briCols == null
                                || briColsCount == briCols.length ? briCols
                                                                  : ArrayUtil
                                                                  .arraySlice(briCols,
                                                                      0, briColsCount);
        bestRowIdentifierStrict = isStrict;

        // not used
        // make array of column indexes for nullable columns in UNIQUE indexes
        ArrayUtil.sortArray(nullRowIDCols);

        int skip = ArrayUtil.findNot(nullRowIDCols, -1);

        nullRowIDCols = skip == -1 ? null
                                   : ArrayUtil.arraySlice(nullRowIDCols, 0,
                                   skip);

        // always needs rowID if there is no primary key
        needsRowID = !hasPrimaryKey();

        if (hasPrimaryKey()) {
            bestIndex = getPrimaryIndex();
        }
    }

    /**
     * Sets the SQL default value for a columm.
     */
    void setDefaultExpression(int columnIndex, Expression def) {

        Column column = getColumn(columnIndex);

        column.setDefaultExpression(def);
        resetDefaultValues();
    }

    void resetDefaultValues() {

        hasDefaultValues = false;

        for (int i = 0; i < visibleColumnCount; i++) {
            Column column = getColumn(i);

            hasDefaultValues = hasDefaultValues
                               || column.getDefaultExpression() != null;
            colDefaults[i] = column.getDefaultExpression();
        }
    }

    DataFileCache getCache() {
        return cache;
    }

    /**
     *  Used in TableFilter to get an index for the column.
     *  An index is created automatically for system tables or subqueries.
     */
    Index getIndexForColumn(int column) {

        int i = bestIndexForColumn[column];

        if (i == -1
                && (tableType == Table.SYSTEM_SUBQUERY
                    || tableType == Table.SYSTEM_TABLE)) {
            try {
                createIndex(new int[]{ column }, null, false, false, false);

                i = bestIndexForColumn[column];
            } catch (Exception e) {}
        }

        return i == -1 ? null
                       : getIndex(i);
    }

    /**
     *  Used for TableFilter to get an index for the columns
     */
    Index getIndexForColumns(boolean[] columnCheck) {

        Index indexChoice = null;
        int   colCount    = 0;

        for (int i = 0; i < indexList.length; i++) {
            Index index = indexList[i];
            boolean result = ArrayUtil.containsAllTrueElements(columnCheck,
                index.colCheck);

            if (result && index.getVisibleColumns() > colCount) {
                colCount    = index.getVisibleColumns();
                indexChoice = index;
            }
        }

        return indexChoice;
    }

    /**
     *  Finds an existing index for a foreign key column group
     */
    Index getIndexForColumns(int[] col, boolean unique) throws HsqlException {

        for (int i = 0, count = getIndexCount(); i < count; i++) {
            Index currentindex = getIndex(i);
            int[] indexcol     = currentindex.getColumns();

            if (ArrayUtil.haveEqualArrays(indexcol, col, col.length)) {
                if (!unique || currentindex.isUnique()) {
                    return currentindex;
                }
            }
        }

        return null;
    }

    /**
     *  Return the list of file pointers to root nodes for this table's
     *  indexes.
     */
    public int[] getIndexRootsArray() {

        int[] roots = new int[getIndexCount()];

        for (int i = 0; i < getIndexCount(); i++) {
            Node f = indexList[i].getRoot();

            roots[i] = (f != null) ? f.getKey()
                                   : -1;
        }

        return roots;
    }

    /**
     * Returns the string consisting of file pointers to roots of indexes
     * plus the next identity value (hidden or user defined). This is used
     * with CACHED tables.
     */
    String getIndexRoots() {

        String roots   = StringUtil.getList(getIndexRootsArray(), " ", "");
        StringBuffer s = new StringBuffer(roots);

        s.append(' ');
        s.append(identitySequence.peek());

        return s.toString();
    }

    /**
     *  Sets the index roots of a cached/text table to specified file
     *  pointers. If a
     *  file pointer is -1 then the particular index root is null. A null index
     *  root signifies an empty table. Accordingly, all index roots should be
     *  null or all should be a valid file pointer/reference.
     */
    public void setIndexRoots(int[] roots) throws HsqlException {

        Trace.check(isCached, Trace.TABLE_NOT_FOUND);

        for (int i = 0; i < getIndexCount(); i++) {
            int p = roots[i];
            Row r = null;

            if (p != -1) {
                r = (CachedRow) rowStore.get(p);
            }

            Node f = null;

            if (r != null) {
                f = r.getNode(i);
            }

            indexList[i].setRoot(f);
        }
    }

    /**
     *  Sets the index roots and next identity.
     */
    void setIndexRoots(String s) throws HsqlException {

        // the user may try to set this; this is not only internal problem
        Trace.check(isCached &&!isText, Trace.TABLE_NOT_FOUND);

        Tokenizer t     = new Tokenizer(s);
        int[]     roots = new int[getIndexCount()];

        for (int i = 0; i < getIndexCount(); i++) {
            int v = t.getInt();

            roots[i] = v;
        }

        setIndexRoots(roots);

        long v = t.getBigint();

        identitySequence.reset(v);
    }

    /**
     *  Shortcut for creating system table PK's.
     */
    void createPrimaryKey(int[] cols) throws HsqlException {
        createPrimaryKey(null, cols, false);
    }

    /**
     *  Shortcut for creating default PK's.
     */
    void createPrimaryKey() throws HsqlException {
        createPrimaryKey(null, null, false);
    }

    /**
     *  Creates a single or multi-column primary key and index. sets the
     *  colTypes array. Finalises the creation of the table. (fredt@users)
     */

// tony_lai@users 20020820 - patch 595099
    void createPrimaryKey(HsqlName pkName, int[] columns,
                          boolean columnsNotNull) throws HsqlException {

        Trace.doAssert(primaryKeyCols == null,
                       "Table.createPrimaryKey(column)");

        if (columns == null) {
            columns = new int[0];

            columnCount++;
        } else {
            for (int i = 0; i < columns.length; i++) {
                if (columnsNotNull) {
                    getColumn(columns[i]).setNullable(false);
                }

                getColumn(columns[i]).setPrimaryKey(true);
            }
        }

        primaryKeyCols   = columns;
        colTypes         = new int[visibleColumnCount];
        colDefaults      = new Expression[visibleColumnCount];
        colSizes         = new int[visibleColumnCount];
        colScales        = new int[visibleColumnCount];
        colNullable      = new boolean[visibleColumnCount];
        defaultColumnMap = new int[visibleColumnCount];

        for (int i = 0; i < visibleColumnCount; i++) {
            Column column = getColumn(i);

            colTypes[i]         = column.getType();
            colSizes[i]         = column.getSize();
            colScales[i]        = column.getScale();
            colNullable[i]      = column.isNullable();
            defaultColumnMap[i] = i;

            if (column.isIdentity()) {
                identitySequence.reset(column.identityStart,
                                       column.identityIncrement);
            }
        }

        primaryKeyTypes = new int[primaryKeyCols.length];

        ArrayUtil.copyColumnValues(colTypes, primaryKeyCols, primaryKeyTypes);

        primaryKeyColsSequence = new int[primaryKeyCols.length];

        ArrayUtil.fillSequence(primaryKeyColsSequence);
        resetDefaultValues();

        // tony_lai@users 20020820 - patch 595099
        HsqlName name = pkName != null ? pkName
                                       : makeSysPKName(this);

        createPrimaryIndex(columns, name);
        setBestRowIdentifiers();
    }

    static HsqlName makeSysPKName(Table table) throws HsqlException {
        return table.database.nameManager.newHsqlName("SYS_PK",
                table.tableName.name, table.tableName.isNameQuoted);
    }

    void createPrimaryIndex(int[] pkcols,
                            HsqlName name) throws HsqlException {

        int[] pkcoltypes;
        int   pkviscolumns;

        if (pkcols.length == 0) {
            pkcols       = new int[]{ visibleColumnCount };
            pkcoltypes   = new int[]{ Types.INTEGER };
            pkviscolumns = 0;
        } else {
            pkcoltypes = new int[pkcols.length];

            for (int j = 0; j < pkcols.length; j++) {
                pkcoltypes[j] = colTypes[pkcols[j]];
            }

            pkviscolumns = pkcols.length;
        }

        Index newindex = new Index(name, this, pkcols, pkcoltypes, true,
                                   true, false, null, null, pkviscolumns);

        addIndex(newindex);
    }

    /**
     *  Create new index taking into account removal or addition of a column
     *  to the table.
     */
    private Index createAdjustedIndex(Index index, int colindex,
                                      int adjust) throws HsqlException {

        int[] indexcolumns = (int[]) ArrayUtil.resizeArray(index.getColumns(),
            index.getVisibleColumns());
        int[] colarr = ArrayUtil.toAdjustedColumnArray(indexcolumns,
            colindex, adjust);

        // if a column to remove is one of the Index columns
        if (colarr.length != index.getVisibleColumns()) {
            return null;
        }

        return createIndexStructure(colarr, index.getName(),
                                    index.isUnique(), index.isConstraint,
                                    index.isForward);
    }

    /**
     *  Create new memory-resident index. For MEMORY and TEXT tables.
     */
    Index createIndex(int[] column, HsqlName name, boolean unique,
                      boolean constraint,
                      boolean forward) throws HsqlException {

        int newindexNo = createIndexStructureGetNo(column, name, unique,
            constraint, forward);
        Index       newindex     = indexList[newindexNo];
        Index       primaryindex = getPrimaryIndex();
        RowIterator it           = primaryindex.firstRow();
        int         rowCount     = 0;
        int         error        = 0;

        try {
            while (it.hasNext()) {
                Row  row      = it.next();
                Node backnode = row.getNode(newindexNo - 1);
                Node newnode  = Node.newNode(row, newindexNo, this);

                newnode.nNext  = backnode.nNext;
                backnode.nNext = newnode;

                // count before inserting
                rowCount++;

                newindex.insert(newnode);
            }

            return newindex;
        } catch (java.lang.OutOfMemoryError e) {
            error = Trace.OUT_OF_MEMORY;
        } catch (HsqlException e) {
            error = Trace.VIOLATION_OF_UNIQUE_INDEX;
        }

        // backtrack on error
        // rowCount rows have been modified
        it = primaryindex.firstRow();

        for (int i = 0; i < rowCount; i++) {
            Row  row      = it.next();
            Node backnode = row.getNode(0);
            int  j        = newindexNo;

            while (--j > 0) {
                backnode = backnode.nNext;
            }

            backnode.nNext = backnode.nNext.nNext;
        }

        indexList = (Index[]) ArrayUtil.toAdjustedArray(indexList, null,
                newindexNo, -1);

        setBestRowIdentifiers();

        throw Trace.error(error);
    }

    /**
     * Creates the internal structures for an index.
     */
    Index createIndexStructure(int[] columns, HsqlName name, boolean unique,
                               boolean constraint,
                               boolean forward) throws HsqlException {

        int i = createIndexStructureGetNo(columns, name, unique, constraint,
                                          forward);

        return indexList[i];
    }

    int createIndexStructureGetNo(int[] column, HsqlName name,
                                  boolean unique, boolean constraint,
                                  boolean forward) throws HsqlException {

        Trace.doAssert(primaryKeyCols != null, "createIndex");

        int[] pkcols  = indexList[0].getColumns();
        int[] pktypes = indexList[0].getColumnTypes();
        int   s       = column.length;
        int   t       = pkcols.length;

        // The primary key fields are added for all indexes except primary
        // key thus making all indexes unique
        int[] col  = new int[s + t];
        int[] type = new int[s + t];

        for (int j = 0; j < s; j++) {
            col[j]  = column[j];
            type[j] = colTypes[col[j]];
        }

        for (int j = 0; j < t; j++) {
            col[s + j]  = pkcols[j];
            type[s + j] = pktypes[j];
        }

        Index newindex = new Index(name, this, col, type, unique, constraint,
                                   forward, null, null, s);
        int indexNo = addIndex(newindex);

        setBestRowIdentifiers();

        return indexNo;
    }

    private int addIndex(Index index) {

        int i = 0;

        for (; i < indexList.length; i++) {
            Index current = indexList[i];
            int order = index.getIndexOrderValue()
                        - current.getIndexOrderValue();

            if (order < 0) {
                break;
            }
        }

        indexList = (Index[]) ArrayUtil.toAdjustedArray(indexList, index, i,
                1);

        return i;
    }

    /**
     * returns false if the table has to be recreated in order to add / drop
     * indexes. Only CACHED tables return false.
     */
    boolean isIndexingMutable() {
        return !isIndexCached();
    }

    /**
     *  Checks for use of a named index in table constraints,
     *  while ignorring a given set of constraints.
     * @throws  HsqlException if index is used in a constraint
     */
    void checkDropIndex(String indexname, HashSet ignore,
                        boolean dropPK) throws HsqlException {

        Index index = this.getIndex(indexname);

        if (index == null) {
            throw Trace.error(Trace.INDEX_NOT_FOUND, indexname);
        }

        if (!dropPK && index.equals(getIndex(0))) {
            throw Trace.error(Trace.DROP_PRIMARY_KEY, indexname);
        }

        for (int i = 0, size = constraintList.length; i < size; i++) {
            Constraint c = constraintList[i];

            if (ignore != null && ignore.contains(c)) {
                continue;
            }

            if (c.isIndexFK(index)) {
                throw Trace.error(Trace.DROP_FK_INDEX, indexname);
            }

            if (c.isIndexUnique(index)) {
                throw Trace.error(Trace.SYSTEM_INDEX, indexname);
            }
        }

        return;
    }

    /**
     *  Returns true if the table has any rows at all.
     */
    boolean isEmpty() {

        if (getIndexCount() == 0) {
            return true;
        }

        return getIndex(0).isEmpty();
    }

    /**
     * Returns direct mapping array.
     */
    int[] getColumnMap() {
        return defaultColumnMap;
    }

    /**
     * Returns empty mapping array.
     */
    int[] getNewColumnMap() {
        return new int[visibleColumnCount];
    }

    /**
     * Returns empty boolean array.
     */
    boolean[] getNewColumnCheckList() {
        return new boolean[visibleColumnCount];
    }

    /**
     * Returns empty Object array for a new row.
     */
    Object[] getEmptyRowData() {
        return new Object[columnCount];
    }

    /**
     * Returns array for a new row with SQL DEFAULT value for each column n
     * where exists[n] is false. This provides default values only where
     * required and avoids evaluating these values where they will be
     * overwritten.
     */
    Object[] getNewRowData(Session session,
                           boolean[] exists) throws HsqlException {

        Object[] data = new Object[columnCount];
        int      i;

        if (exists != null && hasDefaultValues) {
            for (i = 0; i < visibleColumnCount; i++) {
                Expression def = colDefaults[i];

                if (exists[i] == false && def != null) {
                    data[i] = def.getValue(session, colTypes[i]);
                }
            }
        }

        return data;
    }

    /**
     *  Performs Table structure modification and changes to the index nodes
     *  to remove a given index from a MEMORY or TEXT table.
     *
     */
    void dropIndex(String indexname) throws HsqlException {

        // find the array index for indexname and remove
        int todrop = 1;

        for (; todrop < getIndexCount(); todrop++) {
            Index tempidx = getIndex(todrop);

            if (tempidx.getName().name.equals(indexname)) {
                indexList = (Index[]) ArrayUtil.toAdjustedArray(indexList,
                        null, todrop, -1);

                setBestRowIdentifiers();

                break;
            }
        }

        Index       primaryindex = getPrimaryIndex();
        RowIterator it           = primaryindex.firstRow();

        while (it.hasNext()) {
            Row  row      = it.next();
            int  i        = todrop - 1;
            Node backnode = row.getNode(0);

            while (i-- > 0) {
                backnode = backnode.nNext;
            }

            backnode.nNext = backnode.nNext.nNext;
        }
    }

    /**
     * Moves the data from table to table.
     * The colindex argument is the index of the column that was
     * added or removed. The adjust argument is {-1 | 0 | +1}
     */
    void moveData(Session session, Table from, int colindex,
                  int adjust) throws HsqlException {

        Object colvalue = null;

        if (adjust > 0) {
            Column column = getColumn(colindex);

            colvalue = column.getDefaultValue(session);
        }

        RowIterator it = from.rowIterator();

        while (it.hasNext()) {
            Row      row  = it.next();
            Object[] o    = row.getData();
            Object[] data = getEmptyRowData();

            ArrayUtil.copyAdjustArray(o, data, colvalue, colindex, adjust);
            updateIdentityValue(data);
            enforceNullConstraints(data);

            Row newrow = newRow(data);

            indexRow(newrow);
        }

        from.drop();
    }

    /**
     *  Highest level multiple row insert method. Corresponds to an SQL
     *  INSERT INTO ... SELECT ... statement.
     */
    int insert(Session session, Result ins) throws HsqlException {

        Record ni    = ins.rRoot;
        int    count = 0;

        fireAll(session, Trigger.INSERT_BEFORE);

        while (ni != null) {
            insertRow(session, ni.data);

            ni = ni.next;

            count++;
        }

        fireAll(session, Trigger.INSERT_AFTER);

        return count;
    }

    /**
     *  Highest level method for inserting a single row. Corresponds to an
     *  SQL INSERT INTO .... VALUES(,,) statement.
     *  fires triggers.
     */
    void insert(Session session, Object[] data) throws HsqlException {

        fireAll(session, Trigger.INSERT_BEFORE);
        insertRow(session, data);
        fireAll(session, Trigger.INSERT_AFTER);
    }

    /**
     *  Mid level method for inserting rows. Performs constraint checks and
     *  fires row level triggers.
     */
    private void insertRow(Session session,
                           Object[] data) throws HsqlException {

        if (triggerLists[Trigger.INSERT_BEFORE_ROW] != null) {
            fireAll(session, Trigger.INSERT_BEFORE_ROW, null, data);
        }

        setIdentityColumn(session, data);
        checkRowDataInsert(session, data);
        insertNoCheck(session, data);

        if (triggerLists[Trigger.INSERT_AFTER_ROW] != null) {
            fireAll(session, Trigger.INSERT_AFTER_ROW, null, data);
            checkRowDataInsert(session, data);
        }
    }

    /**
     * Multi-row insert method. Used for SELECT ... INTO tablename queries.
     * These tables are new, empty tables, with no constraints, triggers
     * column default values, column size enforcement whatsoever.
     *
     * Not used for INSERT INTO .... SELECT ... FROM queries
     */
    void insertIntoTable(Session session,
                         Result result) throws HsqlException {

        insert(result);

        if (isTemp || isText ||!database.logger.hasLog()) {
            return;
        }

        Record r = result.rRoot;

        while (r != null) {
            database.logger.writeInsertStatement(session, this, r.data);

            r = r.next;
        }
    }

    /**
     *  Low level method for row insert.
     *  UNIQUE or PRIMARY constraints are enforced by attempting to
     *  add the row to the indexes.
     */
    private Row insertNoCheck(Session session,
                              Object[] data) throws HsqlException {

        Row r = newRow(data);

        // this handles the UNIQUE constraints
        indexRow(r);

        if (session != null) {
            session.addTransactionInsert(this, r);
        }

        if (!isTemp &&!isText &&!isReadOnly && database.logger.hasLog()) {
            database.logger.writeInsertStatement(session, this, data);
        }

        return r;
    }

    /**
     *
     */
    public void insertNoCheckFromLog(Session session,
                                     Object[] data) throws HsqlException {

        Row r = newRow(data);

        updateIdentityValue(data);
        indexRow(r);

        if (session != null) {
            session.addTransactionInsert(this, r);
        }
    }

    /**
     *  Low level method for row insert.
     *  UNIQUE or PRIMARY constraints are enforced by attempting to
     *  add the row to the indexes.
     */
    void insertNoCheckRollback(Session session, Object[] data,
                               boolean log) throws HsqlException {

        Row row = newRow(data);

        indexRow(row);

        if (log &&!isTemp &&!isText &&!isReadOnly) {
            database.logger.writeInsertStatement(session, this, data);
        }
    }

    /**
     * Used for subquery and system table inserts. No checks. No identity
     * columns.
     */
    int insert(Result ins) throws HsqlException {

        Record ni    = ins.rRoot;
        int    count = 0;

        while (ni != null) {
            insert(ni.data);

            ni = ni.next;

            count++;
        }

        return count;
    }

    /**
     * Not for general use.
     * Used by ScriptReader to unconditionally insert a row into
     * the table when the .script file is read.
     */
    public void insertFromScript(Object[] data) throws HsqlException {
        updateIdentityValue(data);
        insert(data);
    }

    /**
     * Used by the methods above. To avoid unnecessary
     * creation of arrays The Object[] for data in the Result rows is inserted
     * into the table if it has the same length as table row data.
     */
    public void insert(Object[] data) throws HsqlException {

        if (data.length != columnCount) {
            Object[] newdata = getEmptyRowData();

            ArrayUtil.copyArray(data, newdata, visibleColumnCount);

            data = newdata;
        } else {
            for (int i = visibleColumnCount; i < columnCount; i++) {
                data[i] = null;
            }
        }

        Row r = newRow(data);

        indexRow(r);
    }

    /**
     * Used by TextCache to insert a row into the indexes when the source
     * file is first read.
     */
    protected void insertNoChange(CachedRow row) throws HsqlException {

        Object[] data = row.getData();

        updateIdentityValue(data);
        enforceFieldValueLimits(data, defaultColumnMap);
        enforceNullConstraints(data);
        indexRow(row);
    }

    /**
     * Checks a row against NOT NULL constraints on columns.
     */
    protected void enforceNullConstraints(Object[] data)
    throws HsqlException {

        for (int i = 0; i < visibleColumnCount; i++) {
            if (data[i] == null &&!colNullable[i]) {
                Trace.throwerror(Trace.TRY_TO_INSERT_NULL,
                                 "column: " + getColumn(i).columnName.name
                                 + " table: " + tableName.name);
            }
        }
    }

    /**
     * If there is an identity column (visible or hidden) on the table, sets
     * the value and/or adjusts the iIdentiy value for the table.
     */
    protected void setIdentityColumn(Session session,
                                     Object[] data) throws HsqlException {

        if (identityColumn != -1) {
            Number id = (Number) data[identityColumn];

            if (id == null) {
                if (colTypes[identityColumn] == Types.INTEGER) {
                    id = ValuePool.getInt((int) identitySequence.getValue());
                } else {
                    id = ValuePool.getLong(identitySequence.getValue());
                }

                data[identityColumn] = id;
            } else {
                identitySequence.getValue(id.longValue());
            }

            if (session != null) {
                session.setLastIdentity(id);
            }
        }
    }

    /**
     * If there is an identity column (visible or hidden) on the table, sets
     * the max identity value.
     */
    protected void updateIdentityValue(Object[] data) throws HsqlException {

        if (identityColumn != -1) {
            Number id = (Number) data[identityColumn];

            if (id != null) {
                identitySequence.getValue(id.longValue());
            }
        }
    }

    /**
     *  Enforce max field sizes according to SQL column definition.
     *  SQL92 13.8
     */
    void enforceFieldValueLimits(Object[] data,
                                 int[] cols) throws HsqlException {

        int i;
        int colindex;

        if (sqlEnforceSize || sqlEnforceStrictSize) {
            if (cols == null) {
                cols = defaultColumnMap;
            }

            for (i = 0; i < cols.length; i++) {
                colindex = cols[i];

                if (colSizes[colindex] != 0 && data[colindex] != null) {
                    data[colindex] = Column.enforceSize(data[colindex],
                                                        colTypes[colindex],
                                                        colSizes[colindex],
                                                        colScales[colindex],
                                                        sqlEnforceStrictSize);
                }
            }
        }
    }

// fredt@users 20020130 - patch 491987 by jimbag@users - modified

    /**
     *  Fires all row-level triggers of the given set (trigger type)
     *
     */
    void fireAll(Session session, int trigVecIndx, Object[] oldrow,
                 Object[] newrow) {

        if (!database.isReferentialIntegrity()) {

            // isReferentialIntegrity is false when reloading db
            return;
        }

        HsqlArrayList trigVec = triggerLists[trigVecIndx];

        if (trigVec == null) {
            return;
        }

        for (int i = 0, size = trigVec.size(); i < size; i++) {
            TriggerDef td = (TriggerDef) trigVec.get(i);

            td.pushPair(session, oldrow, newrow);    // tell the trigger thread to fire with this row
        }
    }

    /**
     *  Statement level triggers.
     */
    void fireAll(Session session, int trigVecIndex) {

        if (triggerLists[trigVecIndex] != null) {
            fireAll(session, trigVecIndex, null, null);
        }
    }

    /**
     * Adds a trigger.
     */
    void addTrigger(TriggerDef trigDef) {

        if (triggerLists[trigDef.vectorIndex] == null) {
            triggerLists[trigDef.vectorIndex] = new HsqlArrayList();
        }

        triggerLists[trigDef.vectorIndex].add(trigDef);
    }

    /**
     * Drops a trigger.
     */
    void dropTrigger(String name) {

        // look in each trigger list of each type of trigger
        int numTrigs = TriggerDef.NUM_TRIGS;

        for (int tv = 0; tv < numTrigs; tv++) {
            HsqlArrayList v = triggerLists[tv];

            if (v == null) {
                continue;
            }

            for (int tr = v.size() - 1; tr >= 0; tr--) {
                TriggerDef td = (TriggerDef) v.get(tr);

                if (td.name.name.equals(name)) {
                    v.remove(tr);
                    td.terminate();
                }
            }

            if (v.isEmpty()) {
                triggerLists[tv] = null;
            }
        }
    }

    /**
     * Drops all triggers.
     */
    void dropTriggers() {

        // look in each trigger list of each type of trigger
        int numTrigs = TriggerDef.NUM_TRIGS;

        for (int tv = 0; tv < numTrigs; tv++) {
            HsqlArrayList v = triggerLists[tv];

            if (v == null) {
                continue;
            }

            for (int tr = v.size() - 1; tr >= 0; tr--) {
                TriggerDef td = (TriggerDef) v.get(tr);

                td.terminate();
            }

            triggerLists[tv] = null;
        }
    }

    /** @todo fredt - reused structures to be reviewed for multi-threading */

    /**
     * Reusable set of all FK constraints that have so far been enforced while
     * a cascading insert or delete is in progress. This is emptied and passed
     * with the first call to checkCascadeDelete or checkCascadeUpdate. During
     * recursion, if an FK constraint is encountered and is already present
     * in the set, the recursion stops.
     */
    HashSet constraintPath;

    /**
     * Current list of updates on this table. This is emptied once a cascading
     * operation is over.
     */
    HashMappedList tableUpdateList;

// fredt@users 20020225 - patch 1.7.0 - CASCADING DELETES

    /**
     *  Method is called recursively on a tree of tables from the current one
     *  until no referring foreign-key table is left. In the process, if a
     *  non-cascading foreign-key referring table contains data, an exception
     *  is thrown. Parameter delete indicates whether to delete refering rows.
     *  The method is called first to check if the row can be deleted, then to
     *  delete the row and all the refering rows.<p>
     *
     *  Support added for SET NULL and SET DEFAULT by kloska@users involves
     *  switching to checkCascadeUpdate(,,,,) when these rules are encountered
     *  in the constraint.(fredt@users)
     *
     * @table  table table to update
     * @param  tableUpdateLists list of update lists
     * @param  row row to delete
     * @param  session
     * @param  delete
     * @param  path
     * @throws  HsqlException
     */
    static void checkCascadeDelete(Session session, Table table,
                                   HashMappedList tableUpdateLists, Row row,
                                   boolean delete,
                                   HashSet path) throws HsqlException {

        for (int i = 0, size = table.constraintList.length; i < size; i++) {
            Constraint c = table.constraintList[i];

            if (c.getType() != Constraint.MAIN || c.getRef() == null) {
                continue;
            }

            RowIterator refiterator = c.findFkRef(row.getData(), delete);

            if (refiterator.hasNext()) {
                if (c.core.deleteAction == Constraint.NO_ACTION) {
                    if (c.core.mainTable == c.core.refTable) {
                        Row refrow = refiterator.next();

                        // fredt - it's the same row
                        // this supports deleting a single row
                        // in future we can iterate over and check against
                        // the full delete row list to enable multi-row
                        // with self-referencing FK's deletes
                        if (row.equals(refrow)) {
                            continue;
                        }
                    }

                    throw Trace.error(Trace.INTEGRITY_CONSTRAINT_VIOLATION,
                                      Trace.Constraint_violation,
                                      new Object[] {
                        c.core.fkName.name, c.core.refTable.getName().name
                    });
                }
            } else {

                // no referencing row found
                continue;
            }

            Table reftable = c.getRef();

            // shortcut when deltable has no imported constraint
            boolean hasref =
                reftable.getNextConstraintIndex(0, Constraint.MAIN) != -1;

            // if (reftable == this) we don't need to go further and can return ??
            if (delete == false && hasref == false) {
                continue;
            }

            Index    refindex  = c.getRefIndex();
            int[]    m_columns = c.getMainColumns();
            int[]    r_columns = c.getRefColumns();
            Object[] mdata     = row.getData();
            boolean isUpdate = c.getDeleteAction() == Constraint.SET_NULL
                               || c.getDeleteAction()
                                  == Constraint.SET_DEFAULT;

            // -- list for records to be inserted if this is
            // -- a 'ON DELETE SET [NULL|DEFAULT]' constraint
            HashMappedList rowSet = null;

            if (isUpdate) {
                rowSet = (HashMappedList) tableUpdateLists.get(reftable);

                if (rowSet == null) {
                    rowSet = new HashMappedList();

                    tableUpdateLists.add(reftable, rowSet);
                }
            }

            // walk the index for all the nodes that reference delnode
            for (;;) {
                Row refrow = refiterator.next();

                if (refrow == null || refrow.isDeleted()
                        || refindex.compareRowNonUnique(
                            mdata, m_columns, refrow.getData()) != 0) {
                    break;
                }

                // -- if the constraint is a 'SET [DEFAULT|NULL]' constraint we have to keep
                // -- a new record to be inserted after deleting the current. We also have to
                // -- switch over to the 'checkCascadeUpdate' method below this level
                if (isUpdate) {
                    Object[] rnd = reftable.getEmptyRowData();

                    System.arraycopy(refrow.getData(), 0, rnd, 0, rnd.length);

                    if (c.getDeleteAction() == Constraint.SET_NULL) {
                        for (int j = 0; j < r_columns.length; j++) {
                            rnd[r_columns[j]] = null;
                        }
                    } else {
                        for (int j = 0; j < r_columns.length; j++) {
                            Column col = reftable.getColumn(r_columns[j]);

                            rnd[r_columns[j]] = col.getDefaultValue(session);
                        }
                    }

                    if (hasref && path.add(c)) {

                        // fredt - avoid infinite recursion on circular references
                        // these can be rings of two or more mutually dependent tables
                        // so only one visit per constraint is allowed
                        checkCascadeUpdate(session, reftable, null, refrow,
                                           rnd, r_columns, null, path);
                        path.remove(c);
                    }

                    if (delete) {

                        //  foreign key referencing own table - do not update the row to be deleted
                        if (reftable != table ||!refrow.equals(row)) {
                            mergeUpdate(rowSet, refrow, rnd, r_columns);
                        }
                    }
                } else if (hasref) {
                    if (reftable != table) {
                        if (path.add(c)) {
                            checkCascadeDelete(session, reftable,
                                               tableUpdateLists, refrow,
                                               delete, path);
                            path.remove(c);
                        }
                    } else {

                        // fredt - we avoid infinite recursion on the fk's referencing the same table
                        // but chained rows can result in very deep recursion and StackOverflowError
                        if (refrow != row) {
                            checkCascadeDelete(session, reftable,
                                               tableUpdateLists, refrow,
                                               delete, path);
                        }
                    }
                }

                if (delete &&!isUpdate &&!refrow.isDeleted()) {
                    reftable.deleteNoRefCheck(session, refrow);
                }
            }

            refiterator.release();
        }
    }

    /**
     * Check or perform an update cascade operation on a single row.
     * Check or cascade an update (delete/insert) operation.
     * The method takes a pair of rows (new data,old data) and checks
     * if Constraints permit the update operation.
     * A boolean arguement determines if the operation should
     * realy take place or if we just have to check for constraint violation.
     * fredt - cyclic conditions are now avoided by checking for second visit
     * to each constraint. The set of list of updates for all tables is passed
     * and filled in recursive calls.
     *
     *   @param session current database session
     *   @param table
     *   @param tableUpdateLists lists of updates
     *   @param orow old row data to be deleted.
     *   @param nrow new row data to be inserted.
     *   @param cols indices of the columns actually changed.
     *   @param ref This should be initialized to null when the
     *   method is called from the 'outside'. During recursion this will be the
     *   current table (i.e. this) to indicate from where we came.
     *   Foreign keys to this table do not have to be checked since they have
     *   triggered the update and are valid by definition.
     *
     *   @short Check or perform and update cascade operation on a single row.
     *
     *
     */
    static void checkCascadeUpdate(Session session, Table table,
                                   HashMappedList tableUpdateLists, Row orow,
                                   Object[] nrow, int[] cols, Table ref,
                                   HashSet path) throws HsqlException {

        // -- We iterate through all constraints associated with this table
        // --
        for (int i = 0, size = table.constraintList.length; i < size; i++) {
            Constraint c = table.constraintList[i];

            if (c.getType() == Constraint.FOREIGN_KEY && c.getRef() != null) {

                // -- (1) If it is a foreign key constraint we have to check if the
                // --     main table still holds a record which allows the new values
                // --     to be set in the updated columns. This test however will be
                // --     skipped if the reference table is the main table since changes
                // --     in the reference table triggered the update and therefor
                // --     the referential integrity is guaranteed to be valid.
                // --
                if (ref == null || c.getMain() != ref) {

                    // -- common indexes of the changed columns and the main/ref constraint
                    if (ArrayUtil.countCommonElements(cols, c.getRefColumns())
                            == 0) {

                        // -- Table::checkCascadeUpdate -- NO common cols; reiterating
                        continue;
                    }

                    c.hasMainRef(nrow);
                }
            } else if (c.getType() == Constraint.MAIN && c.getRef() != null) {

                // -- (2) If it happens to be a main constraint we check if the slave
                // --     table holds any records refering to the old contents. If so,
                // --     the constraint has to support an 'on update' action or we
                // --     throw an exception (all via a call to Constraint.findFkRef).
                // --
                // -- If there are no common columns between the reference constraint
                // -- and the changed columns, we reiterate.
                int[] common = ArrayUtil.commonElements(cols,
                    c.getMainColumns());

                if (common == null) {

                    // -- NO common cols between; reiterating
                    continue;
                }

                int[] m_columns = c.getMainColumns();
                int[] r_columns = c.getRefColumns();

                // fredt - find out if the FK columns have actually changed
                boolean nochange = true;

                for (int j = 0; j < m_columns.length; j++) {
                    if (!orow.getData()[m_columns[j]].equals(
                            nrow[m_columns[j]])) {
                        nochange = false;

                        break;
                    }
                }

                if (nochange) {
                    continue;
                }

                // there must be no record in the 'slave' table
                // sebastian@scienion -- dependent on forDelete | forUpdate
                RowIterator refiterator = c.findFkRef(orow.getData(), false);

                if (refiterator.hasNext()) {
                    if (c.core.updateAction == Constraint.NO_ACTION) {
                        throw Trace.error(
                            Trace.INTEGRITY_CONSTRAINT_VIOLATION,
                            Trace.Constraint_violation, new Object[] {
                            c.core.fkName.name, c.core.refTable.getName().name
                        });
                    }
                } else {

                    // no referencing row found
                    continue;
                }

                Table reftable = c.getRef();

                // -- unused shortcut when update table has no imported constraint
                boolean hasref =
                    reftable.getNextConstraintIndex(0, Constraint.MAIN) != -1;
                Index refindex = c.getRefIndex();

                // -- walk the index for all the nodes that reference update node
                HashMappedList rowSet =
                    (HashMappedList) tableUpdateLists.get(reftable);

                if (rowSet == null) {
                    rowSet = new HashMappedList();

                    tableUpdateLists.add(reftable, rowSet);
                }

                for (Row refrow = refiterator.next(); ;
                        refrow = refiterator.next()) {
                    if (refrow == null
                            || refindex.compareRowNonUnique(
                                orow.getData(), m_columns,
                                refrow.getData()) != 0) {
                        break;
                    }

                    Object[] rnd = reftable.getEmptyRowData();

                    System.arraycopy(refrow.getData(), 0, rnd, 0, rnd.length);

                    // -- Depending on the type constraint we are dealing with we have to
                    // -- fill up the forign key of the current record with different values
                    // -- And handle the insertion procedure differently.
                    if (c.getUpdateAction() == Constraint.SET_NULL) {

                        // -- set null; we do not have to check referential integrity any further
                        // -- since we are setting <code>null</code> values
                        for (int j = 0; j < r_columns.length; j++) {
                            rnd[r_columns[j]] = null;
                        }
                    } else if (c.getUpdateAction()
                               == Constraint.SET_DEFAULT) {

                        // -- set default; we check referential integrity with ref==null; since we manipulated
                        // -- the values and referential integrity is no longer guaranteed to be valid
                        for (int j = 0; j < r_columns.length; j++) {
                            Column col = reftable.getColumn(r_columns[j]);

                            rnd[r_columns[j]] = col.getDefaultValue(session);
                        }

                        if (path.add(c)) {
                            checkCascadeUpdate(session, reftable,
                                               tableUpdateLists, refrow, rnd,
                                               r_columns, null, path);
                            path.remove(c);
                        }
                    } else {

                        // -- cascade; standard recursive call. We inherit values from the foreign key
                        // -- table therefor we set ref==this.
                        for (int j = 0; j < m_columns.length; j++) {
                            rnd[r_columns[j]] = nrow[m_columns[j]];
                        }

                        if (path.add(c)) {
                            checkCascadeUpdate(session, reftable,
                                               tableUpdateLists, refrow, rnd,
                                               common, table, path);
                            path.remove(c);
                        }
                    }

                    mergeUpdate(rowSet, refrow, rnd, r_columns);
                }
            }
        }
    }

    /**
     *  Merges a triggered change with a previous triggered change, or adds to
     * list.
     */
    static void mergeUpdate(HashMappedList rowSet, Row row, Object[] newData,
                            int[] cols) {

        Object[] data = (Object[]) rowSet.get(row);

        if (data != null) {
            for (int j = 0; j < cols.length; j++) {
                data[cols[j]] = newData[cols[j]];
            }
        } else {
            rowSet.add(row, newData);
        }
    }

    /**
     * Merge the full triggered change with the updated row, or add to list.
     * Return false if changes conflict.
     */
    static boolean mergeKeepUpdate(HashMappedList rowSet, int[] cols,
                                   int[] colTypes, Row row,
                                   Object[] newData) throws HsqlException {

        Object[] data = (Object[]) rowSet.get(row);

        if (data != null) {
            if (Index.compareRows(row
                    .getData(), newData, cols, colTypes) != 0 && Index
                        .compareRows(newData, data, cols, colTypes) != 0) {
                return false;
            }

            for (int j = 0; j < cols.length; j++) {
                newData[cols[j]] = data[cols[j]];
            }

            rowSet.put(row, newData);
        } else {
            rowSet.add(row, newData);
        }

        return true;
    }

    static void clearUpdateLists(HashMappedList tableUpdateList) {

        for (int i = 0; i < tableUpdateList.size(); i++) {
            HashMappedList updateList =
                (HashMappedList) tableUpdateList.get(i);

            updateList.clear();
        }
    }

    /**
     *  Highest level multiple row delete method. Corresponds to an SQL
     *  DELETE.
     */
    int delete(Session session,
               HsqlArrayList deleteList) throws HsqlException {

        HashSet path = constraintPath == null ? new HashSet()
                                              : constraintPath;

        constraintPath = null;

        HashMappedList tUpdateList = tableUpdateList == null
                                     ? new HashMappedList()
                                     : tableUpdateList;

        tableUpdateList = null;

        if (database.isReferentialIntegrity()) {
            for (int i = 0; i < deleteList.size(); i++) {
                Row row = (Row) deleteList.get(i);

                path.clear();
                checkCascadeDelete(session, this, tUpdateList, row, false,
                                   path);
            }
        }

        fireAll(session, Trigger.DELETE_BEFORE);

        if (database.isReferentialIntegrity()) {
            for (int i = 0; i < deleteList.size(); i++) {
                Row row = (Row) deleteList.get(i);

                path.clear();
                checkCascadeDelete(session, this, tUpdateList, row, true,
                                   path);
            }
        }

        for (int i = 0; i < deleteList.size(); i++) {
            Row row = (Row) deleteList.get(i);

            if (!row.isDeleted()) {
                deleteNoRefCheck(session, row);
            }
        }

        for (int i = 0; i < tUpdateList.size(); i++) {
            Table          table      = (Table) tUpdateList.getKey(i);
            HashMappedList updateList = (HashMappedList) tUpdateList.get(i);

            table.updateRowSet(session, updateList, null, false);
            updateList.clear();
        }

        fireAll(session, Trigger.DELETE_AFTER);
        path.clear();

        constraintPath  = path;
        tableUpdateList = tUpdateList;

        return deleteList.size();
    }

    /**
     *  Mid level row delete method. Fires triggers but no integrity
     *  constraint checks.
     */
    private void deleteNoRefCheck(Session session,
                                  Row row) throws HsqlException {

        Object[] data = row.getData();

        fireAll(session, Trigger.DELETE_BEFORE_ROW, data, null);
        deleteNoCheck(session, row, true);

        // fire the delete after statement trigger
        fireAll(session, Trigger.DELETE_AFTER_ROW, data, null);
    }

    /**
     * Low level row delete method. Removes the row from the indexes and
     * from the Cache.
     */
    private void deleteNoCheck(Session session, Row row,
                               boolean log) throws HsqlException {

        if (row.isDeleted()) {
            return;
        }

        Object[] data = row.getData();

        row = row.getUpdatedRow();

        for (int i = getIndexCount() - 1; i >= 0; i--) {
            Node node = row.getNode(i);

            indexList[i].delete(node);
        }

        row.delete();

        if (session != null) {
            session.addTransactionDelete(this, row);
        }

        if (log &&!isTemp &&!isText &&!isReadOnly) {
            database.logger.writeDeleteStatement(session, this, data);
        }
    }

    /**
     * For log statements.
     */
    public void deleteNoCheckFromLog(Session session,
                                     Object[] data) throws HsqlException {

        Row row = null;

        if (hasPrimaryKey()) {
            RowIterator it = getPrimaryIndex().findFirstRow(data,
                primaryKeyColsSequence);

            row = it.next();
        } else if (bestIndex == null) {
            RowIterator it = rowIterator();

            while (true) {
                row = it.next();

                if (row == null) {
                    break;
                }

                if (Index.compareRows(
                        row.getData(), data, defaultColumnMap,
                        colTypes) == 0) {
                    break;
                }
            }
        } else {
            RowIterator it = bestIndex.findFirstRow(data);

            while (true) {
                row = it.next();

                if (row == null) {
                    break;
                }

                Object[] rowdata = row.getData();

                // reached end of range
                if (bestIndex.compareRowNonUnique(
                        data, bestIndex.getColumns(), rowdata) != 0) {
                    row = null;

                    break;
                }

                if (Index.compareRows(
                        rowdata, data, defaultColumnMap, colTypes) == 0) {
                    break;
                }
            }
        }

        if (row == null) {
            return;
        }

        for (int i = indexList.length - 1; i >= 0; i--) {
            Node node = row.getNode(i);

            indexList[i].delete(node);
        }

        row.delete();

        if (session != null) {
            session.addTransactionDelete(this, row);
        }
    }

    /**
     * Low level row delete method. Removes the row from the indexes and
     * from the Cache. Used by rollback.
     */
    void deleteNoCheckRollback(Session session, Object[] data,
                               boolean log) throws HsqlException {

        Row row = getIndex(0).findRow(data);

        for (int i = indexList.length - 1; i >= 0; i--) {
            Node node = row.getNode(i);

            indexList[i].delete(node);
        }

        row.delete();

        if (log &&!isTemp &&!isText &&!isReadOnly
                && database.logger.hasLog()) {
            database.logger.writeDeleteStatement(session, this, data);
        }
    }

    /**
     * Highest level multiple row update method. Corresponds to an SQL
     * UPDATE. To DEAL with unique constraints we need to perform all
     * deletes at once before the inserts. If there is a UNIQUE constraint
     * violation limited only to the duration of updating multiple rows,
     * we don't want to abort the operation. Example:
     * UPDATE MYTABLE SET UNIQUECOL = UNIQUECOL + 1
     * After performing each cascade update, delete the main row.
     * After all cascade ops and deletes have been performed, insert new
     * rows.
     *
     * The following clauses from SQL Standard section 11.8 are enforced
     * 9) Let ISS be the innermost SQL-statement being executed.
     * 10) If evaluation of these General Rules during the execution of ISS
     * would cause an update of some site to a value that is distinct from the
     * value to which that site was previously updated during the execution of
     * ISS, then an exception condition is raised: triggered data change
     * violation.
     * 11) If evaluation of these General Rules during the execution of ISS
     * would cause deletion of a row containing a site that is identified for
     * replacement in that row, then an exception condition is raised:
     * triggered data change violation.
     *
     *  (fredt)
     */
    int update(Session session, HashMappedList updateList,
               int[] cols) throws HsqlException {

        HashSet path = constraintPath == null ? new HashSet()
                                              : constraintPath;

        constraintPath = null;

        HashMappedList tUpdateList = tableUpdateList == null
                                     ? new HashMappedList()
                                     : tableUpdateList;

        tableUpdateList = null;

        // set identity column where null and check columns
        for (int i = 0; i < updateList.size(); i++) {
            Object[] data = (Object[]) updateList.get(i);

            // this means the identity column can be set to null to force
            // creation of a new identity value
            setIdentityColumn(session, data);
            enforceFieldValueLimits(data, cols);
            enforceNullConstraints(data);
        }

        // perform check/cascade operations
        if (database.isReferentialIntegrity()) {
            for (int i = 0; i < updateList.size(); i++) {
                Object[] data = (Object[]) updateList.get(i);
                Row      row  = (Row) updateList.getKey(i);

                checkCascadeUpdate(session, this, tUpdateList, row, data,
                                   cols, null, path);
            }
        }

        fireAll(session, Trigger.UPDATE_BEFORE);

        // merge any triggered change to this table with the update list
        HashMappedList triggeredList = (HashMappedList) tUpdateList.get(this);

        if (triggeredList != null) {
            for (int i = 0; i < triggeredList.size(); i++) {
                Row      row  = (Row) triggeredList.getKey(i);
                Object[] data = (Object[]) triggeredList.get(i);

                mergeKeepUpdate(updateList, cols, colTypes, row, data);
            }

            triggeredList.clear();
        }

        for (int i = 0; i < tUpdateList.size(); i++) {
            Table          table       = (Table) tUpdateList.getKey(i);
            HashMappedList updateListT = (HashMappedList) tUpdateList.get(i);

            table.updateRowSet(session, updateListT, null, false);
            updateListT.clear();
        }

        // update main list
        updateRowSet(session, updateList, cols, true);
        fireAll(session, Trigger.UPDATE_AFTER);
        path.clear();

        constraintPath  = path;
        tableUpdateList = tUpdateList;

        clearUpdateLists(tableUpdateList);

        return updateList.size();
    }

    void updateRowSet(Session session, HashMappedList rowSet, int[] cols,
                      boolean nodelete) throws HsqlException {

        for (int i = rowSet.size() - 1; i >= 0; i--) {
            Row      row  = (Row) rowSet.getKey(i);
            Object[] data = (Object[]) rowSet.get(i);

            if (row.isDeleted()) {
                if (nodelete) {
                    throw Trace.error(Trace.TRIGGERED_DATA_CHANGE);
                } else {
                    rowSet.remove(i);

                    continue;
                }
            }

            for (int j = 0; j < constraintList.length; j++) {
                Constraint c = constraintList[j];

                if (c.getType() == Constraint.CHECK) {
                    c.checkCheckConstraint(session, data);

                    continue;
                }
            }

            deleteNoCheck(session, row, true);
        }

        for (int i = 0; i < rowSet.size(); i++) {
            Row      row  = (Row) rowSet.getKey(i);
            Object[] data = (Object[]) rowSet.get(i);

            if (triggerLists[Trigger.UPDATE_BEFORE_ROW] != null) {
                fireAll(session, Trigger.UPDATE_BEFORE_ROW, row.getData(),
                        data);
                checkRowDataUpdate(session, data, cols);
            }

            insertNoCheck(session, data);

            if (triggerLists[Trigger.UPDATE_AFTER_ROW] != null) {
                fireAll(session, Trigger.UPDATE_AFTER_ROW, row.getData(),
                        data);
                checkRowDataUpdate(session, data, cols);
            }
        }
    }

    void checkRowDataInsert(Session session,
                            Object[] data) throws HsqlException {

        enforceFieldValueLimits(data, null);
        enforceNullConstraints(data);

        if (database.isReferentialIntegrity()) {
            for (int i = 0, size = constraintList.length; i < size; i++) {
                constraintList[i].checkInsert(session, data);
            }
        }
    }

    void checkRowDataUpdate(Session session, Object[] data,
                            int[] cols) throws HsqlException {

        enforceFieldValueLimits(data, cols);
        enforceNullConstraints(data);

        for (int j = 0; j < constraintList.length; j++) {
            Constraint c = constraintList[j];

            if (c.getType() == Constraint.CHECK) {
                c.checkCheckConstraint(session, data);
            }
        }
    }

    /**
     *  True if table is CACHED or TEXT
     *
     * @return
     */
    boolean isCached() {
        return isCached;
    }

    /**
     *  Returns true if table is CACHED
     */
    boolean isIndexCached() {
        return isCached &&!isText;
    }

    /**
     * Returns the Index object of the given name or null if not found.
     */
    Index getIndex(String indexName) {

        for (int i = 0; i < getIndexCount(); i++) {
            Index indexObject = getIndex(i);

            if (indexName.equals(indexObject.getName().name)) {
                return indexObject;
            }
        }

        // no such index
        return null;
    }

    /**
     *  Return the position of the constraint within the list
     */
    int getConstraintIndex(String constraintName) {

        for (int i = 0, size = constraintList.length; i < size; i++) {
            if (constraintList[i].getName().name.equals(constraintName)) {
                return i;
            }
        }

        return -1;
    }

    /**
     *  return the named constriant
     */
    Constraint getConstraint(String constraintName) {

        int i = getConstraintIndex(constraintName);

        return (i < 0) ? null
                       : (Constraint) constraintList[i];
    }

    /**
     *  Returns the Column object at the given index
     */
    Column getColumn(int i) {
        return (Column) columnList.get(i);
    }

    void renameColumn(Column column, String newName,
                      boolean isquoted) throws HsqlException {

        String oldname = column.columnName.name;
        int    i       = getColumnNr(oldname);

        columnList.setKey(i, newName);
        column.columnName.rename(newName, isquoted);
        renameColumnInCheckConstraints(oldname, newName, isquoted);
    }

    /**
     *  Returns an array of int valuse indicating the SQL type of the columns
     */
    public int[] getColumnTypes() {
        return colTypes;
    }

    /**
     *  Returns the Index object at the given index
     */
    protected Index getIndex(int i) {
        return indexList[i];
    }

    public Index[] getIndexes() {
        return indexList;
    }

    /**
     *  Used by CACHED tables to fetch a Row from the Cache, resulting in the
     *  Row being read from disk if it is not in the Cache.
     *
     *  TEXT tables pass the memory resident Node parameter so that the Row
     *  and its index Nodes can be relinked.
     */
    CachedRow getRow(int pos, Node primarynode) throws HsqlException {

        if (isText) {
            CachedDataRow row = (CachedDataRow) rowStore.get(pos);

            row.nPrimaryNode = primarynode;

            return row;
        } else if (isCached) {
            return (CachedRow) rowStore.get(pos);
        }

        return null;
    }

    void addRowToStore(Row row) throws HsqlException {

        if (isCached && cache != null) {
            try {
                cache.add((CachedRow) row);
            } catch (IOException e) {
                throw new HsqlException(
                    e, Trace.getMessage(Trace.GENERAL_IO_ERROR),
                    Trace.GENERAL_IO_ERROR);
            }
        } else if (needsRowID) {

            // fredt - this is required when there is a non-primary index
            // and a user defined pk - can reduce the cases where it is
            // necessary
            row.getData()[visibleColumnCount] =
                ValuePool.getInt((int) rowIdSequence.getValue());
        }
    }

    void registerRow(CachedRow row) {}

    void removeRow(CachedRow row) throws HsqlException {
        rowStore.remove(row.getPos());
    }

    void indexRow(Row row) throws HsqlException {

        int i = 0;

        try {
            Node n = null;

            for (; i < getIndexCount(); i++) {
                n = row.getNextNode(n);

                indexList[i].insert(n);
            }
        } catch (HsqlException e) {
            Index   index        = indexList[i];
            boolean isconstraint = index.isConstraint;

            // unique index violation - rollback insert
            for (--i; i >= 0; i--) {
                Node n = row.getNode(i);

                indexList[i].delete(n);
            }

            row.delete();

            if (isconstraint) {
                Constraint c    = getConstraintForIndex(index);
                String     name = c == null ? ""
                                            : c.getName().name;

                throw Trace.error(Trace.VIOLATION_OF_UNIQUE_CONSTRAINT, name);
            }

            throw e;
        }
    }

    /**
     * Currently only for temp system tables and TEXT tables.
     */
    void clearAllRows() {

        for (int i = 0; i < getIndexCount(); i++) {
            getIndex(i).clearAll();
        }

        identitySequence.reset();
        rowIdSequence.reset();
    }

/** @todo -- release the rows */
    void drop() throws HsqlException {}

    boolean isWritable() {
        return !isReadOnly &&!database.databaseReadOnly
               &&!(database.isFilesReadOnly() && (isCached || isText));
    }

    /**
     * Returns the catalog name or null, depending on a database property.
     */
    String getCatalogName() {

        // PRE: database is never null
        return database.getProperties().isPropertyTrue("hsqldb.catalogs")
               ? database.getURI()
               : null;
    }

    /**
     * Returns the schema name or null, depending on a database property.
     */
    String getSchemaName() {

        // PRE: database is never null
        if (!database.getProperties().isPropertyTrue("hsqldb.schemas")) {
            return null;
        }

        switch (tableType) {

            case SYSTEM_TABLE : {
                return "DEFINITION_SCHEMA";
            }
            case SYSTEM_VIEW : {
                return "INFORMATION_SCHEMA";
            }
            case MEMORY_TABLE :
            case CACHED_TABLE :
            case TEXT_TABLE :
            case VIEW : {
                return UserManager.PUBLIC_USER_NAME;
            }
            case TEMP_TABLE :
            case TEMP_TEXT_TABLE : {
                Session s =
                    database.sessionManager.getSession(ownerSessionId);

                if (s == null) {
                    return null;
                } else if (s.getId() == ownerSessionId) {
                    return s.getUsername();
                } else {
                    return null;
                }
            }
            case SYSTEM_SUBQUERY :
            default : {
                return null;
            }
        }
    }

    public int getRowCount() throws HsqlException {
        return getPrimaryIndex().size();
    }

    /**
     * Necessary when over Integer.MAX_VALUE Row objects have been generated
     * for the table.
     */
    public void resetRowId() throws HsqlException {

        if (isCached &&!isText) {
            return;
        }

        rowIdSequence = new NumberSequence(null, 0, 1, Types.BIGINT);

        RowIterator it = rowIterator();

        while (it.hasNext()) {
            Row row = it.next();
            int pos = (int) rowIdSequence.getValue();

            row.setPos(pos);

            if (needsRowID) {
                row.getData()[visibleColumnCount] = ValuePool.getInt(pos);
            }
        }
    }

    /**
     *  Factory method instantiates a Row based on table type.
     */
    Row newRow(Object[] o) throws HsqlException {

        Row row;

        try {
            if (isText) {
                row = new CachedDataRow(this, o);

                rowStore.add(row);
            } else if (isCached) {
                row = new CachedRow(this, o);

                rowStore.add(row);
            } else {
                row = new Row(this, o);

                int pos = (int) rowIdSequence.getValue();

                row.setPos(pos);

                // fredt - this is required when there is a non-primary index
                // and a user defined pk - should reduce the cases where it is
                // necessary
                if (needsRowID) {
                    row.getData()[visibleColumnCount] = ValuePool.getInt(pos);
                }
            }
        } catch (IOException e) {
            throw new HsqlException(
                e, Trace.getMessage(Trace.GENERAL_IO_ERROR),
                Trace.GENERAL_IO_ERROR);
        }

        return row;
    }

    public class RowStore implements PersistentStore {

        public CachedObject get(int i) {

            try {
                return cache.get(i, this, false);
            } catch (HsqlException e) {
                return null;
            }
        }

        public CachedObject getKeep(int i) {

            try {
                return cache.get(i, this, true);
            } catch (HsqlException e) {
                return null;
            }
        }

        public int getStorageSize(int i) {

            try {
                return cache.get(i, this, false).getStorageSize();
            } catch (HsqlException e) {
                return 0;
            }
        }

        public void add(CachedObject row) throws IOException {
            cache.add(row);
        }

        public CachedObject get(RowInputInterface in) {

            try {
                if (Table.this.isText) {
                    return new CachedDataRow(Table.this, in);
                }

                CachedObject row = new CachedRow(Table.this, in);

                return row;
            } catch (HsqlException e) {
                return null;
            } catch (IOException e1) {
                return null;
            }
        }

        public CachedObject getNewInstance(int size) {
            return null;
        }

        public void remove(int i) {

            try {
                cache.remove(i, this);
            } catch (HsqlException e) {}
        }

        public void release(int i) {
            cache.release(i);
        }
    }
}
