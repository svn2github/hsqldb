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

import org.hsqldb.lib.ArrayUtil;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.Iterator;
import org.hsqldb.lib.HashMap;
import org.hsqldb.lib.HashSet;
import org.hsqldb.lib.HashMappedList;
import org.hsqldb.lib.HsqlLinkedList;
import org.hsqldb.lib.HsqlStringBuffer;
import org.hsqldb.lib.StringUtil;
import org.hsqldb.store.ValuePool;
import org.hsqldb.HsqlNameManager.HsqlName;

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

/**
 *  Holds the data structures and methods for creation of a database table.
 *
 *
 * @version 1.7.0
 */
class Table {

    // types of table
    static final int SYSTEM_TABLE    = 0;
    static final int TEMP_TABLE      = 1;
    static final int MEMORY_TABLE    = 2;
    static final int CACHED_TABLE    = 3;
    static final int TEMP_TEXT_TABLE = 4;
    static final int TEXT_TABLE      = 5;
    static final int VIEW            = 6;

// boucherb@users - added in antcipation of special (not created via SQL) system
// view objects to implement a SQL9n or 200n INFORMATION_SCHEMA
    static final int SYSTEM_VIEW = 7;

    // name of the column added to tables without primary key
    static final String DEFAULT_PK = "";

    // main properties
// boucherb@users - access changed in support of metadata 1.7.2
    HashMappedList vColumn;                    // columns in table
    HsqlArrayList  vIndex;                     // vIndex(0) is the primary key index
    int[]          iPrimaryKey;                // column numbers for primary key
    int            iIndexCount;                // size of vIndex
    int[]          bestRowIdentifierCols;      // column set for best index
    boolean        bestRowIdentifierStrict;    // true if it has no nullable column
    int[]          bestIndexForColumn;         // index of the 'best' index for each column
    boolean        needsRowID;
    int[]          nullRowIDCols;
    int            identityColumn;             // -1 means no such row
    NumberSequence identitySequence;           // next value of identity column
    NumberSequence rowIdSequence;              // next value of optional rowid

// -----------------------------------------------------------------------
    HsqlArrayList     vConstraint;             // constrainst for the table
    HsqlArrayList     vTrigs[];                // array of trigger lists
    private int[]     colTypes;                // fredt - types of columns
    private int[]     colSizes;                // fredt - copy of SIZE values for columns
    private boolean[] colNullable;             // fredt - modified copy of isNullable() values
    private String[] colDefaults;              // fredt - copy of DEFAULT values
    private int[]    defaultColumnMap;         // fred - holding 0,1,2,3,...
    private boolean  hasDefaultValues;         //fredt - shortcut for above
    private boolean  isSystem;
    private boolean  isText;
    private boolean  isView;
    boolean          sqlEnforceSize;           // inherited for the database -
    boolean          sqlEnforceStrictSize;     // inherited for the database -

    // properties for subclasses
// boucherb@users - access changes in support of metadata 1.7.2
    protected int      iColumnCount;           // inclusive the hidden primary key
    protected int      iVisibleColumns;        // exclusive of hidden primary key
    protected Database database;
    protected Cache    cache;
    protected HsqlName tableName;              // SQL name
    protected int      tableType;
    protected int      ownerSessionId;         // fredt - set for temp tables only
    protected boolean  isReadOnly;
    protected boolean  isTemp;
    protected boolean  isCached;
    protected int      indexType;              // fredt - type of index used

    /**
     *  Constructor declaration
     *
     * @param  db
     * @param  isTemp
     * @param  name
     * @param  cached
     * @param  nameQuoted        Description of the Parameter
     * @exception  HsqlException  Description of the Exception
     */
    Table(Database db, HsqlName name, int type,
            int sessionid) throws HsqlException {

        database             = db;
        sqlEnforceSize       = db.sqlEnforceSize;
        sqlEnforceStrictSize = db.sqlEnforceStrictSize;
        identitySequence     = new NumberSequence(db.firstIdentity);
        rowIdSequence        = new NumberSequence(0);

        switch (type) {

            case SYSTEM_TABLE :
                isTemp = true;
                break;

            case TEMP_TABLE :
                isTemp         = true;
                ownerSessionId = sessionid;
                break;

            case CACHED_TABLE :
                cache = db.logger.getCache();

                if (cache != null) {
                    isCached = true;
                } else {
                    type = MEMORY_TABLE;
                }
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
                isView = true;
                break;
        }

        if (isText) {
            indexType = Index.POINTER_INDEX;
        } else if (isCached) {
            indexType = Index.DISK_INDEX;
        }

        // type may have changed above for CACHED tables
        tableType      = type;
        tableName      = name;
        iPrimaryKey    = null;
        identityColumn = -1;
        vColumn        = new HashMappedList();
        vIndex         = new HsqlArrayList();
        vConstraint    = new HsqlArrayList();
        vTrigs         = new HsqlArrayList[TriggerDef.numTrigs()];    // defer init...should be "pay to use"

        for (int vi = 0; vi < TriggerDef.numTrigs(); vi++) {
            vTrigs[vi] = new HsqlArrayList();    // defer init...should be "pay to use"
        }

// ----------------------------------------------------------------------------
// akede@users - 1.7.2 patch Files readonly
        // Changing the mode of the table if necessary
        if (db.filesReadOnly && checkTableFileBased()) {
            this.isReadOnly = true;
        }

// ----------------------------------------------------------------------------
    }

    boolean equals(String other, Session c) {

        if (isTemp && c.getId() != ownerSessionId) {
            return false;
        }

        return (tableName.name.equals(other));
    }

    boolean equals(String other) {
        return (tableName.name.equals(other));
    }

    final boolean isText() {
        return isText;
    }

    final boolean isTemp() {
        return isTemp;
    }

    final boolean isView() {
        return isView;
    }

    final int getIndexType() {
        return indexType;
    }

    final boolean isDataReadOnly() {
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
        if (!value && database.filesReadOnly && checkTableFileBased()) {
            throw Trace.error(Trace.DATA_IS_READONLY);
        }

        isReadOnly = value;
    }

    /**
     * Text or Cached Tables are normally file based
     */
    boolean checkTableFileBased() {
        return isCached | isText;
    }

// ----------------------------------------------------------------------------
    int getOwnerSessionId() {
        return ownerSessionId;
    }

    protected void setDataSource(String source, boolean isDesc,
                                 Session s) throws HsqlException {

        // Same exception as setIndexRoots.
        throw (Trace.error(Trace.TABLE_NOT_FOUND));
    }

    protected String getDataSource() {
        return null;
    }

    protected boolean isDescDataSource() {
        return false;
    }

    /**
     *  Method declaration
     *
     * @param  c
     */
    void addConstraint(Constraint c) {
        vConstraint.add(c);
    }

    /**
     *  Method declaration
     *
     * @return
     */
    HsqlArrayList getConstraints() {
        return vConstraint;
    }

/** @todo fredt - this can be improved to ignore order of columns in
     * multi-column indexes */

    /**
     *  Get the index supporting a constraint that can be used as an index
     *  of the given type and index column signature. Only Unique constraints
     *  are considered (FK constraints' main index is actually on a different
     *  table - one time bug)
     *
     * @param  col column list array
     * @param  unique for the index
     * @return
     */
    Index getConstraintIndexForColumns(int[] col, boolean unique) {

        if (ArrayUtil.areEqual(getPrimaryIndex().getColumns(), col,
                               col.length, unique)) {
            return getPrimaryIndex();
        }

        for (int i = 0, size = vConstraint.size(); i < size; i++) {
            Constraint c = (Constraint) vConstraint.get(i);

            if (c.getType() != Constraint.UNIQUE) {
                continue;
            }

            if (ArrayUtil.areEqual(c.getMainColumns(), col, col.length,
                                   unique)) {
                return c.getMainIndex();
            }
        }

        return null;
    }

    /**
     *  Get any foreign key constraint equivalent to the column sets
     *
     * @param  col column list array
     * @param  unique for the index
     * @return
     */
    Constraint getConstraintForColumns(Table tablemain, int[] colmain,
                                       int[] colref) {

        for (int i = 0, size = vConstraint.size(); i < size; i++) {
            Constraint c = (Constraint) vConstraint.get(i);

            if (c.isEquivalent(tablemain, colmain, this, colref)) {
                return c;
            }
        }

        return null;
    }

    /**
     *  To support alter table ops. Finds any autogenerated FK index that
     *  fits into the colref[] index columns. Returns the FK constraint that
     *  contains the index.
     *
     * @param  colref column list array
     * @return constraint or null
     */
    Constraint getAutoIndexConstraintForColumns(int[] colref) {

        for (int i = 0, size = vConstraint.size(); i < size; i++) {
            Constraint c = (Constraint) vConstraint.get(i);

            if (c.getType() == Constraint.FOREIGN_KEY) {
                Index index = c.getRefIndex();

                if (!index.isUnique()
                        && index.getName().isReservedIndexName()) {
                    if (ArrayUtil.areEqual(colref, index.getColumns(),
                                           index.getVisibleColumns(),
                                           false)) {
                        return c;
                    }
                }
            }
        }

        return null;
    }

    /**
     *  Method declaration
     *
     * @param  from
     * @param  type
     * @return
     */
    int getNextConstraintIndex(int from, int type) {

        for (int i = from, size = vConstraint.size(); i < size; i++) {
            Constraint c = (Constraint) vConstraint.get(i);

            if (c.getType() == type) {
                return i;
            }
        }

        return -1;
    }

    /**
     *  Method declaration
     *
     * @param  name
     * @param  type
     * @throws  HsqlException
     */
    void addColumn(String name, int type) throws HsqlException {

        Column column =
            new Column(database.nameManager.newHsqlName(name, false), true,
                       type, 0, 0, false, false, null);

        addColumn(column);
    }

// fredt@users 20020220 - patch 475199 - duplicate column

    /**
     *  Performs the table level checks and adds a column to the table at the
     *  DDL level.
     *
     * @param  column new column to add
     * @throws  HsqlException when table level checks fail
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

            identityColumn = iColumnCount;
        }

        Trace.doAssert(iPrimaryKey == null, "Table.addColumn");
        vColumn.add(column.columnName.name, column);

        iColumnCount++;
        iVisibleColumns++;
    }

    /**
     *  Method declaration
     *
     * @param  result
     * @throws  HsqlException
     */
    void addColumns(Result result) throws HsqlException {

        int colCount = result.getColumnCount();

        for (int i = 0; i < colCount; i++) {
            Column column = new Column(
                database.nameManager.newHsqlName(
                    result.sLabel[i], result.isLabelQuoted[i]), true,
                        result.colType[i], result.colSize[i],
                        result.colScale[i], false, false, null);

            addColumn(column);
        }
    }

    /**
     *  Method declaration
     *
     * @param  result
     * @throws  HsqlException
     */
    void addColumns(Select select) throws HsqlException {

        int colCount = select.iResultLen;

        for (int i = 0; i < colCount; i++) {
            Expression e = select.eColumn[i];
            Column column = new Column(
                database.nameManager.newHsqlName(
                    e.getAlias(), e.isAliasQuoted()), true, e.getDataType(),
                        e.getColumnSize(), e.getColumnScale(), false, false,
                        null);

            addColumn(column);
        }
    }

    /**
     *  Method declaration
     *
     * @return
     */
    HsqlName getName() {
        return tableName;
    }

    /**
     * Changes table name. Used by 'alter table rename to'.
     * Essential to use the existing HsqlName as this is is referenced by
     * intances of Constraint etc.
     *
     * @param name
     * @param isquoted
     * @throws  HsqlException
     */
    void setName(String name, boolean isquoted) throws HsqlException {

        tableName.rename(name, isquoted);

        if (HsqlName.isReservedIndexName(getPrimaryIndex().getName().name)) {
            getPrimaryIndex().getName().rename("SYS_PK", name, isquoted);
        }
    }

    /**
     *  Method declaration
     *
     * @return
     */
    int getInternalColumnCount() {

        // todo: this is a temporary solution;
        // the the hidden column is not really required
        return iColumnCount;
    }

    protected Table duplicate() throws HsqlException {

        Table t = (new Table(database, tableName, tableType, ownerSessionId));

        return t;
    }

    /**
     * Match two columns arrays for length and type of coluns
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
            if (col[i] >= iColumnCount || othercol[i] >= other.iColumnCount) {
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
     * SHUTDOWN IMMEDIATE occures, the following will happen:<br>
     * If the table is cached, the index roots will be different from what
     * is specified in SET INDEX ROOTS. <br>
     * If the table is memory, the old index will be used until the script
     * reaches drop index etc. and data is recreated again.<b>
     *
     * The fix avoids scripting the row insert and delete ops.
     *
     * Constraints that need removing are removed outside this (fredt@users)
     * @param  withoutindex
     * @param  newcolumn
     * @param  colindex
     * @param  adjust -1 or 0 or +1
     * @return
     * @throws  HsqlException
     */
    Table moveDefinition(String withoutindex, Column newcolumn, int colindex,
                         int adjust) throws HsqlException {

        Table tn = duplicate();

        for (int i = 0; i < iVisibleColumns + 1; i++) {
            if (i == colindex) {
                if (adjust > 0) {
                    tn.addColumn(newcolumn);
                } else if (adjust < 0) {
                    continue;
                }
            }

            if (i == iVisibleColumns) {
                break;
            }

            tn.addColumn(getColumn(i));
        }

        // treat it the same as new table creation and
        // take account of the a hidden column
        int[] primarykey = (iPrimaryKey[0] == iVisibleColumns) ? null
                                                               : iPrimaryKey;

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

        tn.vConstraint = vConstraint;

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

        tn.vTrigs = vTrigs;

        return tn;
    }

    void updateConstraints(Table to, int colindex,
                           int adjust) throws HsqlException {

        for (int j = 0, size = vConstraint.size(); j < size; j++) {
            Constraint c = (Constraint) vConstraint.get(j);

            c.replaceTable(to, this, colindex, adjust);
        }
    }

    /**
     *  Method declaration
     *
     * @return
     */
    int getColumnCount() {
        return iVisibleColumns;
    }

    /**
     *  Method declaration
     *
     * @return
     */
    int getIndexCount() {
        return iIndexCount;
    }

    /**
     *  Method declaration
     *
     * @return
     */
    int getIdentityColumn() {
        return identityColumn;
    }

    /**
     *  Method declaration
     *
     * @param  c
     * @return
     * @throws  HsqlException
     */
    int getColumnNr(String c) throws HsqlException {

        int i = searchColumn(c);

        if (i == -1) {
            throw Trace.error(Trace.COLUMN_NOT_FOUND, c);
        }

        return i;
    }

    /**
     *  Method declaration
     *
     * @param  c
     * @return
     */
    int searchColumn(String c) {

        int index = vColumn.getIndex(c);

        return index == iVisibleColumns ? -1
                                        : index;
/*
        for (int i = 0; i < this.iVisibleColumns; i++) {
            if (c.equals(((Column) vColumn.get(i)).columnName.name)) {
                return i;
            }
        }

        return -1;
*/
    }

    /**
     *  Method declaration
     *
     * @return
     * @throws  HsqlException
     */
    Index getPrimaryIndex() {

        if (iPrimaryKey == null) {
            return null;
        }

        return getIndex(0);
    }

    /**
     *  Method declaration
     *
     * @return
     * @throws  HsqlException
     */
    int[] getPrimaryKey() {
        return (iPrimaryKey[0] == iVisibleColumns) ? null
                                                   : iPrimaryKey;
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

        bestIndexForColumn = new int[vColumn.size()];
        nullRowIDCols      = new int[vColumn.size()];

        for (int i = 0; i < bestIndexForColumn.length; i++) {
            nullRowIDCols[i] = bestIndexForColumn[i] = -1;
        }

        for (int i = 0; i < vIndex.size(); i++) {
            Index index     = (Index) vIndex.get(i);
            int[] cols      = index.getColumns();
            int   colsCount = index.getVisibleColumns();

            // ignore system primary keys
            if (i == 0 && getPrimaryKey() == null) {
                continue;
            }

            if (bestIndexForColumn[cols[0]] == -1) {
                bestIndexForColumn[cols[0]] = i;
            }

            if (!index.isUnique()) {
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
        needsRowID = getPrimaryKey() == null;
    }

    /**
     *  Used in TableFilter to get an index for the column
     *
     * @param  column
     * @return
     * @throws  HsqlException
     */
    Index getIndexForColumn(int column) throws HsqlException {

        int i = bestIndexForColumn[column];

        return i == -1 ? null
                       : getIndex(i);
/*
        for (int i = 0; i < iIndexCount; i++) {
            Index h = getIndex(i);
            int[] cols = h.getColumns();
            if (cols[0] == column) {
                return h;
            }
        }

        return null;
*/
    }

    /**
     *  Finds an existing index for a foreign key column group
     *
     * @param  col
     * @return
     * @throws  HsqlException
     */
    Index getIndexForColumns(int col[], boolean unique) throws HsqlException {

        for (int i = 0; i < iIndexCount; i++) {
            Index currentindex = getIndex(i);
            int   indexcol[]   = currentindex.getColumns();

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
    int[] getIndexRootsArray() {

        int[] roots = new int[iIndexCount];

        for (int i = 0; i < iIndexCount; i++) {
            Node f = getIndex(i).getRoot();

            roots[i] = (f != null) ? f.getKey()
                                   : -1;
        }

        return roots;
    }

    /**
     *  Method declaration
     *
     * @return
     * @throws  HsqlException
     */
    String getIndexRoots() {

//        Trace.doAssert(isCached, "Table.getIndexRootData");
        String roots = StringUtil.getList(getIndexRootsArray(), " ", "");
        HsqlStringBuffer s = new HsqlStringBuffer(roots);

        s.append(' ');
        s.append(identitySequence.peek());

        return s.toString();
    }

    /**
     *  Currently used for text tables.
     */
    void setIndexRootsNull() {

        for (int i = 0; i < iIndexCount; i++) {
            getIndex(i).setRoot(null);
        }

        identitySequence.reset(database.firstIdentity);
    }

    /**
     *  Sets the index roots of a cached/text table to specified file
     *  pointers. If a
     *  file pointer is -1 then the particular index root is null. A null index
     *  root signifies an empty table. Accordingly, all index roots should be
     *  null or all should be a valid file pointer/reference.
     *
     * @param  s
     * @throws  HsqlException
     */
    void setIndexRoots(int[] roots) throws HsqlException {

        Trace.check(isCached, Trace.TABLE_NOT_FOUND);

        for (int i = 0; i < iIndexCount; i++) {
            int p = roots[i];
            Row r = null;

            if (p != -1) {
                r = cache.getRow(p, this);
            }

            Node f = null;

            if (r != null) {
                f = r.getNode(i);
            }

            getIndex(i).setRoot(f);
        }
    }

    /**
     *  Method declaration
     *
     * @param  s
     * @throws  HsqlException
     */
    void setIndexRoots(String s) throws HsqlException {

        // the user may try to set this; this is not only internal problem
        Trace.check(isCached, Trace.TABLE_NOT_FOUND);

        int[] roots = new int[iIndexCount];
        int   j     = 0;

        for (int i = 0; i < iIndexCount; i++) {
            int n = s.indexOf(' ', j);
            int p = Integer.parseInt(s.substring(j, n));

            roots[i] = p;
            j        = n + 1;
        }

        setIndexRoots(roots);
        identitySequence.reset(Long.parseLong(s.substring(j)));
    }

    /**
     *  Method declaration
     *
     * @param  index
     * @return
     */
    Index getNextIndex(Index index) {

        int i = 0;

        if (index != null) {
            for (; i < iIndexCount && getIndex(i) != index; i++) {
                ;
            }

            i++;
        }

        if (i < iIndexCount) {
            return getIndex(i);
        }

        return null;    // no more indexes
    }

    /**
     *  Shortcut for creating system table PK's
     *
     * @throws  HsqlException
     */
    void createPrimaryKey(int[] cols) throws HsqlException {
        createPrimaryKey(null, cols, false);
    }

    /**
     *  Shortcut for creating default PK's
     *
     * @throws  HsqlException
     */
    void createPrimaryKey() throws HsqlException {
        createPrimaryKey(null, null, false);
    }

    /**
     *  Adds the SYSTEM_ID column if no primary key is specified in DDL.
     *  Creates a single or multi-column primary key and index. sets the
     *  colTypes array. Finalises the creation of the table. (fredt@users)
     *
     * @param columns primary key column(s) or null if no primary key in DDL
     * @throws  HsqlException
     */

// tony_lai@users 20020820 - patch 595099
    void createPrimaryKey(HsqlName pkName, int[] columns,
                          boolean columnsNotNull) throws HsqlException {

        Trace.doAssert(iPrimaryKey == null, "Table.createPrimaryKey(column)");

        Column column =
            new Column(database.nameManager.newAutoName(DEFAULT_PK), false,
                       Types.INTEGER, 0, 0, false, columns == null, null);

        addColumn(column);

        iVisibleColumns--;

        if (columns == null) {
            columns = new int[]{ iVisibleColumns };
        } else {
            for (int i = 0; i < columns.length; i++) {
                if (columnsNotNull) {
                    getColumn(columns[i]).setNullable(false);
                }

                getColumn(columns[i]).setPrimaryKey(true);
            }
        }

        iPrimaryKey = columns;

// tony_lai@users 20020820 - patch 595099
        HsqlName name = pkName != null ? pkName
                                       : database.nameManager.newHsqlName(
                                           "SYS_PK", tableName.name,
                                           tableName.isNameQuoted);

        createIndexStructure(columns, name, true, true);

        colTypes         = new int[iColumnCount];
        colDefaults      = new String[iVisibleColumns];
        colSizes         = new int[iVisibleColumns];
        colNullable      = new boolean[iVisibleColumns];
        defaultColumnMap = new int[iVisibleColumns];

        for (int i = 0; i < iColumnCount; i++) {
            column      = getColumn(i);
            colTypes[i] = column.getType();

            if (i < iVisibleColumns) {
                hasDefaultValues = hasDefaultValues
                                   || column.getDefaultString() != null;
                colDefaults[i] = column.getDefaultString();
                colSizes[i]    = column.getSize();

                // when insert or update values are processed, IDENTITY column can be null
                colNullable[i] = column.isNullable() || column.isIdentity();
                defaultColumnMap[i] = i;
            }
        }

        setBestRowIdentifiers();
    }

    /**
     *  Create new index taking into account removal or addition of a column
     *  to the table.
     *
     * @param  index
     * @param  colindex
     * @param  ajdust -1 or 0 or 1
     * @return new index or null if a column is removed from index
     * @throws  HsqlException
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
                                    index.isUnique(), false);
    }

    /**
     *  Create new memory resident index. For MEMORY and TEXT tables.
     *
     * @param  column
     * @param  name
     * @param  unique
     */
    Index createIndex(int column[], HsqlName name,
                      boolean unique) throws HsqlException {

        Index newindex = createIndexStructure(column, name, unique, false);
        Index primaryindex = getPrimaryIndex();
        Node  n            = primaryindex.first();
        int   error        = 0;

        try {
            while (n != null) {
                if (Trace.STOP) {
                    Trace.stop();
                }

                Row  row     = n.getRow();
                Node newnode = Node.newNode(row, iIndexCount - 1, this);
                Node endnode = row.getNode(iIndexCount - 2);

                endnode.nNext = newnode;

                newindex.insert(newnode);

                n = primaryindex.next(n);
            }

            return newindex;
        } catch (java.lang.OutOfMemoryError e) {
            error = Trace.OUT_OF_MEMORY;
        } catch (HsqlException e) {
            error = Trace.VIOLATION_OF_UNIQUE_INDEX;
        }

        // backtrack on error
        Node lastnode = n;

        n = primaryindex.first();

        while (n != lastnode) {
            int  i        = iIndexCount - 2;
            Node backnode = n;

            while (i-- > 0) {
                backnode = backnode.nNext;
            }

            backnode.nNext = null;
            n              = primaryindex.next(n);
        }

        vIndex.remove(iIndexCount - 1);

        iIndexCount = vIndex.size();

        setBestRowIdentifiers();

        throw Trace.error(error);
    }

    /**
     *  Method declaration
     *
     * @param  column
     * @param  name
     * @param  unique
     * @return                Description of the Return Value
     * @throws  HsqlException
     */
    Index createIndexStructure(int column[], HsqlName name, boolean unique,
                               boolean pk) throws HsqlException {

        Trace.doAssert(iPrimaryKey != null, "createIndex");

        int s = column.length;
        int t = pk ? 0
                   : iPrimaryKey.length;

        // The primary key fields are added for all indexes except primary
        // key thus making all indexes unique
        int col[]  = new int[s + t];
        int type[] = new int[s + t];

        for (int j = 0; j < s; j++) {
            col[j]  = column[j];
            type[j] = getColumn(col[j]).getType();
        }

        if (!pk) {
            for (int j = 0; j < t; j++) {
                col[s + j]  = iPrimaryKey[j];
                type[s + j] = getColumn(iPrimaryKey[j]).getType();
            }
        }

        // fredt - visible columns of index is 0 for system generated PK
        if (col[0] == iVisibleColumns) {
            s = 0;
        }

        Index newindex = new Index(name, this, col, type, unique, s);

        vIndex.add(newindex);

        iIndexCount = vIndex.size();

        setBestRowIdentifiers();

        return newindex;
    }

    /**
     * returns false if the table has to be recreated in order to add / drop
     * indexes. Only CACHED tables return false.
     */
    boolean isIndexingMutable() {
        return !isIndexCached();
    }

// fredt@users 20020315 - patch 1.7.0 - drop index bug
// don't drop an index used for a foreign key

    /**
     *  Checks for use of a named index in table constraints
     *
     * @param  indexname
     * @param ignore null or a set of constraints that should be ignored in checks
     * @throws  HsqlException if index is used in a constraint
     */
    void checkDropIndex(String indexname,
                        HashSet ignore) throws HsqlException {

        Index index = this.getIndex(indexname);

        if (index == null) {
            throw Trace.error(Trace.INDEX_NOT_FOUND, indexname);
        }

        if (index.equals(getIndex(0))) {
            throw Trace.error(Trace.DROP_PRIMARY_KEY, indexname);
        }

        for (int i = 0, size = vConstraint.size(); i < size; i++) {
            Constraint c = (Constraint) vConstraint.get(i);

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
     *  Method declaration
     *
     * @return
     */
    boolean isEmpty() {

        if (iIndexCount == 0) {
            return true;
        }

        return getIndex(0).getRoot() == null;
    }

    /**
     * Returns direct mapping array
     */
    int[] getColumnMap() {
        return defaultColumnMap;
    }

    /**
     * Returns empty mapping array.
     */
    int[] getNewColumnMap() {
        return new int[iVisibleColumns];
    }

    /**
     * Returns empty boolean array.
     */
    boolean[] getNewColumnCheckList() {
        return new boolean[iVisibleColumns];
    }

    /**
     * Returns empty Object array for a new row.
     */
    Object[] getNewRow() {
        return new Object[iColumnCount];
    }

    /**
     * Returns array for a new row with SQL DEFAULT value for each column n
     * where exists[n] is false. This provides default values only where
     * required and avoids evaluating these values where they will be
     * overwritten.
     */
    Object[] getNewRow(boolean[] exists) throws HsqlException {

        Object[] row = new Object[iColumnCount];
        int      i;

        if (exists != null && hasDefaultValues) {
            for (i = 0; i < iVisibleColumns; i++) {
                String def = colDefaults[i];

                if (exists[i] == false && def != null) {
                    row[i] = Column.convertObject(def, colTypes[i]);
                }
            }
        }

        return row;
    }

    /**
     *  Performs Table structure modification and changes to the index nodes
     *  to remove a given index from a MEMORY or TEXT table.
     *
     * @return
     */
    void dropIndex(String indexname) throws HsqlException {

        // find the array index for indexname and remove
        int todrop = 1;

        for (; todrop < getIndexCount(); todrop++) {
            Index tempidx = getIndex(todrop);

            if (tempidx.getName().name.equals(indexname)) {
                vIndex.remove(todrop);

                iIndexCount = vIndex.size();

                setBestRowIdentifiers();

                break;
            }
        }

        Index primaryindex = getPrimaryIndex();
        Node  n            = primaryindex.first();

        while (n != null) {
            if (Trace.STOP) {
                Trace.stop();
            }

            int  i        = todrop - 1;
            Node backnode = n;

            while (i-- > 0) {
                backnode = backnode.nNext;
            }

            backnode.nNext = backnode.nNext.nNext;
            n              = primaryindex.next(n);
        }
    }

    /**
     *  Method declaration
     *
     * @param  from
     * @param  colindex index of the column that was added or removed
     * @throws  HsqlException normally for lack of resources
     */
    void moveData(Table from, int colindex, int adjust) throws HsqlException {

        Object colvalue = null;

        if (adjust > 0) {
            Column column = getColumn(colindex);

            colvalue = Column.convertObject(column.getDefaultString(),
                                            column.getType());
        }

        Index index = from.getPrimaryIndex();
        Node  n     = index.first();

        while (n != null) {
            if (Trace.STOP) {
                Trace.stop();
            }

            Object o[]      = n.getData();
            Object newrow[] = this.getNewRow();

            ArrayUtil.copyAdjustArray(o, newrow, colvalue, colindex, adjust);
            insertNoCheck(newrow, null, false);

            n = index.next(n);
        }

        // fredt - this is replaced with drop()
/*
        index = from.getPrimaryIndex();
        n     = index.first();

        while (n != null) {
            if (Trace.STOP) {
                Trace.stop();
            }

            Node   nextnode = index.next(n);
            Object o[]      = n.getData();

            from.deleteNoCheck(o, null, false);

            n = nextnode;
        }
*/
        from.drop();
    }

    /**
     *  Highest level multiple row insert method. Corresponds to an SQL
     *  INSERT INTO statement.
     */
    int insert(Result ins, Session c) throws HsqlException {

        Record ni    = ins.rRoot;
        int    count = 0;

        while (ni != null) {
            checkNullColumns(ni.data);

            ni = ni.next;
        }

        ni = ins.rRoot;

        fireAll(TriggerDef.INSERT_BEFORE);

        while (ni != null) {
            insertRow(ni.data, c);

            ni = ni.next;

            count++;
        }

        fireAll(TriggerDef.INSERT_AFTER);

        return count;
    }

    /**
     *  Highest level method for inserting a single row. Corresponds to an
     *  SQL INSERT INTO .... VALUES(,,) statement.
     *  fires triggers.
     */
    void insert(Object row[], Session c) throws HsqlException {

        checkNullColumns(row);
        fireAll(TriggerDef.INSERT_BEFORE);
        insertRow(row, c);
        fireAll(TriggerDef.INSERT_AFTER);
    }

    /**
     *  High level method for inserting rows. Performs constraint checks and
     *  fires triggers.
     */
    private void insertRow(Object row[], Session c) throws HsqlException {

        fireAll(TriggerDef.INSERT_BEFORE_ROW, null, row);

        if (database.isReferentialIntegrity()) {
            for (int i = 0, size = vConstraint.size(); i < size; i++) {
                ((Constraint) vConstraint.get(i)).checkInsert(row);
            }
        }

        insertNoCheck(row, c, true);
        fireAll(TriggerDef.INSERT_AFTER_ROW, null, row);
    }

    /**
     * Multi-row insert method. Used for SELECT ... INTO tablename queries
     * also for creating temporary tables from subqueries. These tables are
     * new, empty tables with no constraints, triggers
     * column default values, column size enforcement whatsoever.
     *
     * Not used for INSERT INTO .... SELECT ... FROM queries
     */
    void insertNoCheck(Result result, Session c) throws HsqlException {

        // if violation of constraints can occur, insert must be rolled back
        // outside of this function!
        Record r   = result.rRoot;
        int    len = result.getColumnCount();

        while (r != null) {
            Object row[] = getNewRow();

            for (int i = 0; i < len; i++) {
                row[i] = r.data[i];
            }

            insertNoCheck(row, c, true);

            r = r.next;
        }
    }

    /**
     *  Low level method for row insert.
     *  UNIQUE or PRIMARY constraints are enforced by attempting to
     *  add the row to the indexes.
     */
    void insertNoCheck(Object row[], Session c,
                       boolean log) throws HsqlException {

        // this is necessary when rebuilding from the *.script but not
        // for transaction rollback
        setIdentityColumn(row, c);

        // this step is not necessary for rebuilding from the *.script file
        // or transaction rollback - use the c parameters to determine
        if (c != null) {
            enforceFieldValueLimits(row);
        }

        Row r = Row.newRow(this, row);

        // this handles the UNIQUE constraints
        indexRow(r);

        if (c != null) {
            c.addTransactionInsert(this, row);
        }

        if (log &&!isTemp &&!isText &&!isReadOnly
                && database.logger.hasLog()) {
            database.logger.writeToLog(c, getInsertStatement(row));
        }
    }

    /**
     *  Low level method for row insert.
     *  UNIQUE or PRIMARY constraints are enforced by attempting to
     *  add the row to the indexes.
     */
    void insertNoCheckRollback(Object row[], Session c,
                               boolean log) throws HsqlException {

        Row r = Row.newRow(this, row);

        indexRow(r);

        if (log &&!isTemp &&!isText &&!isReadOnly
                && database.logger.hasLog()) {
            database.logger.writeToLog(c, getInsertStatement(row));
        }
    }

    /**
     * Used by TextCache to insert a row into the indexes when the source
     * file is first read.
     */
    protected void insertNoChange(CachedDataRow r) throws HsqlException {

        Object[] row = r.getData();

        checkNullColumns(row);
        setIdentityColumn(row, null);
        indexRow(r);
    }

    /**
     * Checks a row against NOT NULL constraints on columns.
     */
    protected void checkNullColumns(Object[] row) throws HsqlException {

        for (int i = 0; i < iVisibleColumns; i++) {
            if (row[i] == null &&!colNullable[i]) {
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
    protected void setIdentityColumn(Object[] row,
                                     Session c) throws HsqlException {

        if (identityColumn != -1) {
            Number id = (Number) row[identityColumn];

            if (id == null) {
                if (colTypes[identityColumn] == Types.INTEGER) {
                    id = ValuePool.getInt((int) identitySequence.getValue());
                } else {
                    id = ValuePool.getLong(identitySequence.getValue());
                }

                row[identityColumn] = id;
            } else {
                identitySequence.getValue(id.longValue());
            }

            // only do this if id is for a visible column
            if (c != null) {
                c.setLastIdentity(id);
            }
        }
    }

    /**
     *  Enforce max field sizes according to SQL column definition.
     *  SQL92 13.8
     */
    void enforceFieldValueLimits(Object[] row) throws HsqlException {

        int colindex;

        if (sqlEnforceSize || sqlEnforceStrictSize) {
            for (colindex = 0; colindex < iVisibleColumns; colindex++) {
                if (colSizes[colindex] != 0 && row[colindex] != null) {
                    row[colindex] = enforceSize(row[colindex],
                                                colTypes[colindex],
                                                colSizes[colindex], true,
                                                sqlEnforceStrictSize);
                }
            }
        }
    }

    /**
     *  As above but for a limited number of columns used for UPDATE queries.
     */
    void enforceFieldValueLimits(Object[] row,
                                 int col[]) throws HsqlException {

        int i;
        int colindex;

        if (sqlEnforceSize) {
            for (i = 0; i < col.length; i++) {
                colindex = col[i];

                if (colSizes[colindex] != 0 && row[colindex] != null) {
                    row[colindex] = enforceSize(row[colindex],
                                                colTypes[colindex],
                                                colSizes[colindex], true,
                                                sqlEnforceStrictSize);
                }
            }
        }
    }

// fredt@users 20020130 - patch 491987 by jimbag@users - modified

    /**
     *  Check an object for type CHAR and VARCHAR and truncate/pad based on
     *  the  size
     *
     * @param  obj   object to check
     * @param  type  the object type
     * @param  size  size to enforce
     * @param  pad   pad strings
     * @return       the altered object if the right type, else the object
     *      passed in unaltered
     * @throws HsqlException if data too long
     */
    static Object enforceSize(Object obj, int type, int size, boolean pad,
                              boolean raise) throws HsqlException {

        if (size == 0) {
            return obj;
        }

        // todo: need to handle BINARY like this as well
        switch (type) {

            case Types.CHAR :
                return padOrTrunc((String) obj, size, pad, raise);

            case Types.VARCHAR :
                return padOrTrunc((String) obj, size, false, raise);

            default :
                return obj;
        }
    }

    /**
     *  Pad or truncate a string to len size
     *
     * @param  s    the string to pad to truncate
     * @param  len  the len to make the string
     * @param pad   pad the string
     * @return      the string of size len
     */
    static String padOrTrunc(String s, int len, boolean pad,
                             boolean raise) throws HsqlException {

        if (raise && StringUtil.rTrimSize(s) > len) {
            throw Trace.error(Trace.STRING_DATA_TRUNCATION);
        }

        if (s.length() == len) {
            return s;
        }

        if (s.length() > len) {
            return s.substring(0, len);
        }

        HsqlStringBuffer b = new HsqlStringBuffer(len);

        b.append(s);

        if (pad) {
            for (int i = s.length(); i < len; i++) {
                b.append(' ');
            }
        }

        return b.toString();
    }

    /**
     *  Row level triggers
     *
     * @param  trigVecIndx
     * @param  row
     */
    void fireAll(int trigVecIndx, Object oldrow[], Object newrow[]) {

        if (!database.isReferentialIntegrity()) {    // reloading db
            return;
        }

        HsqlArrayList trigVec = vTrigs[trigVecIndx];

        for (int i = 0, size = trigVec.size(); i < size; i++) {
            TriggerDef td = (TriggerDef) trigVec.get(i);

            td.push(oldrow, newrow);    // tell the trigger thread to fire with this row
        }
    }

    /**
     *  Statement level triggers
     *
     * @param  trigVecIndx
     */
    void fireAll(int trigVecIndx) {

        Object row[] = new Object[1];

        row[0] = "Statement-level";

        fireAll(trigVecIndx, row, null);
    }

    /**
     *  Method declaration
     *
     * @param  trigDef
     */
    void addTrigger(TriggerDef trigDef) {

        if (Trace.TRACE) {
            Trace.trace("Trigger added "
                        + String.valueOf(trigDef.vectorIndx));
        }

        vTrigs[trigDef.vectorIndx].add(trigDef);
    }

    /** @todo fredt - reused structure to be reviewed for multi-threading */
    HashSet constraintPath = new HashSet();

// fredt@users 20020225 - patch 1.7.0 - CASCADING DELETES

    /**
     *  Method is called recursively on a tree of tables from the current one
     *  until no referring foreign-key table is left. In the process, if a
     *  non-cascading foreign-key referring table contains data, an exception
     *  is thrown. Parameter doIt indicates whether to delete refering rows.
     *  The method is called first to check if the row can be deleted, then to
     *  delete the row and all the refering rows.<p>
     *
     *  Support added for SET NULL and SET DEFAULT by kloska@users involves
     *  switching to checkCascadeUpdate(,,,,) when these rules are encountered
     *  in the constraint.(fredt@users)
     *
     * @param  row
     * @param  session
     * @param  delete
     * @throws  HsqlException
     */
    void checkCascadeDelete(Row row, Session session, boolean doIt,
                            HashSet path) throws HsqlException {

        for (int i = 0, cSize = vConstraint.size(); i < cSize; i++) {
            Constraint c = (Constraint) vConstraint.get(i);

            if (c.getType() != Constraint.MAIN || c.getRef() == null) {
                continue;
            }

            Node refnode = c.findFkRef(row.getData(), true);

            if (refnode == null) {

                // no referencing row found
                continue;
            }

            Table reftable = c.getRef();

            // shortcut when deltable has no imported constraint
            boolean hasref =
                reftable.getNextConstraintIndex(0, Constraint.MAIN) != -1;

            // if (reftable == this) we don't need to go further and can return ??
            if (doIt == false && hasref == false) {
                return;
            }

            // -- result set for records to be inserted if this is
            // -- a 'ON DELETE SET [NULL|DEFAULT]' constraint
            Result   ri          = new Result(ResultConstants.DATA);
            Index    refindex    = c.getRefIndex();
            int      m_columns[] = c.getMainColumns();
            int      r_columns[] = c.getRefColumns();
            Object[] m_objects   = new Object[m_columns.length];

            ArrayUtil.copyColumnValues(row.getData(), m_columns, m_objects);

            // walk the index for all the nodes that reference delnode
            for (Node n = refnode;
                    !n.isDeleted() && refindex.comparePartialRowNonUnique(
                        m_objects, n.getData()) == 0; ) {

                // deleting rows can free n out of the cache so we
                // make sure it is loaded with up-to-date left-right-parent
                n = n.getUpdatedNode();

                // get the next node before n is deleted
                Node nextn = refindex.next(n);

                // -- if the constraint is a 'SET [DEFAULT|NULL]' constraint we have to remember
                // -- a new record to be inserted after deleting the current. We also have to
                // -- switch over to the 'checkCascadeUpdate' method afterwards
                if (c.getDeleteAction() == Constraint.SET_NULL
                        || c.getDeleteAction() == Constraint.SET_DEFAULT) {
                    Object[] rnd = reftable.getNewRow();

                    System.arraycopy(n.getData(), 0, rnd, 0, rnd.length);

                    if (c.getDeleteAction() == Constraint.SET_NULL) {
                        for (int j = 0; j < r_columns.length; j++) {
                            rnd[r_columns[j]] = null;
                        }
                    } else {
                        for (int j = 0; j < r_columns.length; j++) {
                            rnd[r_columns[j]] =
                                Column.convertObject(reftable
                                    .getColumn(r_columns[j])
                                    .getDefaultString(), reftable
                                    .getColumn(r_columns[j]).getType());
                        }
                    }

                    if (hasref) {

                        // fredt - avoid infinite recursion on same table
                        if (path.add(c)) {
                            reftable.checkCascadeUpdate(n.getRow(), rnd,
                                                        session, r_columns,
                                                        null, doIt, path);
                            path.remove(c);

                            // get updated node in case they moved out of cache
                            n     = n.getUpdatedNode();
                            nextn = nextn == null ? null
                                                  : nextn.getUpdatedNode();
                        }
                    }

                    if (doIt) {

                        //  foreign key referencing own table - do not update the row to be deleted
                        if (reftable != this
                                || n.getRow() != row.getUpdatedRow()) {
                            ri.add(rnd);
                        }
                    }
                } else if (hasref) {

                    // fredt - avoid infinite recursion on same table
                    if (path.add(c)) {
                        reftable.checkCascadeDelete(n.getRow(), session,
                                                    doIt, path);
                        path.remove(c);

                        // get updated node in case they moved out of cache
                        n     = n.getUpdatedNode();
                        nextn = nextn == null ? null
                                              : nextn.getUpdatedNode();
                    }
                }

                if (doIt) {
                    if (!n.isDeleted()) {
                        reftable.deleteNoRefCheck(n.getRow(), session);
                    }

                    //  foreign key referencing own table
/*
                    if (reftable == this) {
                        nextn = c.findFkRef(row.getData(), true);
                    }
*/
                }

                if (nextn == null) {
                    break;
                }

                n = nextn;
            }

            if (doIt) {
                Record r = ri.rRoot;

                while (r != null) {
                    reftable.insertNoCheck(r.data, session, true);

                    r = r.next;
                }
            }
        }
    }

    /**
     * Check or perform an update cascade operation on a single row.
     *   Check or cascade an update (delete/insert) operation.
     *   The method takes a pair of rows (new data,old data) and checks
     *   if Constraints permit the update operation.
     *   A boolean arguement determines if the operation should
     *   realy take place or if we just have to check for constraint violation.
     *
     *
     *
     *   @param orow Object[]; old row data to be deleted.
     *   @param nrow Object[]; new row data to be inserted.
     *   @param session Session; current database session
     *   @param cols int[]; indices of the columns actually changed.
     *   @param ref Table; This should be initialized to null when the
     *   method is called from the 'outside'. During recursion this will be the
     *   current table (i.e. this) to indicate from where we came.
     *   Foreign keys to this table do not have to be checked since they have
     *   triggered the update and are valid by definition.
     *
     *   @param update boolean; if true the update will take place. Otherwise
     *   we just check for referential integrity.
     *
     *   @see #checkCascadeDelete(Object[],Session,boolean)
     *
     *   @short Check or perform and update cascade operation on a single row.
     *
     *
     */

// fredt - cyclic conditions are now avoided by checking for second visit
// to each constraint
    void checkCascadeUpdate(Row orow, Object[] nrow, Session session,
                            int[] cols, Table ref, boolean update,
                            HashSet path) throws HsqlException {

        // -- We iterate through all constraints associated with this table
        // --
        for (int i = 0; i < vConstraint.size(); i++) {
            Constraint c = (Constraint) vConstraint.get(i);

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
                    int[] common;

                    if ((common = ArrayUtil.commonElements(
                            cols, c.getRefColumns())) == null) {

                        // -- Table::checkCascadeUpdate -- NO common cols; reiterating
                        continue;
                    }

                    Node n = c.findMainRef(nrow);
                }
            } else if (c.getType() == Constraint.MAIN && c.getRef() != null) {

                // -- (2) If it happens to be a main constraint we check if the slave
                // --     table holds any records refering to the old contents. If so
                // --     the constraint has to support an 'on update' action or we
                // --     throw an exception (all via a call to Constraint.findFkRef).
                // --
                // -- if there are no common columns between the reference constraint
                // -- and the changed columns we reiterate.
                int[] common;

                if ((common = ArrayUtil.commonElements(
                        cols, c.getMainColumns())) == null) {

                    // -- NO common cols between; reiterating
                    continue;
                }

                int m_columns[] = c.getMainColumns();
                int r_columns[] = c.getRefColumns();

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

                Node refnode = c.findFkRef(orow.getData(), false);

                if (refnode == null) {

                    // no referencing row found
                    continue;
                }

                Table reftable = c.getRef();

                // -- unused shortcut when update table has no imported constraint
                boolean hasref =
                    reftable.getNextConstraintIndex(0, Constraint.MAIN) != -1;
                Index    refindex    = c.getRefIndex();
                Object[] mainobjects = new Object[m_columns.length];
                Object[] refobjects  = new Object[r_columns.length];

                ArrayUtil.copyColumnValues(orow.getData(), m_columns,
                                           mainobjects);
                ArrayUtil.copyColumnValues(nrow, r_columns, refobjects);

                // -- walk the index for all the nodes that reference update node
                Result ri = new Result(ResultConstants.DATA);

                for (Node n = refnode;
                        refindex.comparePartialRowNonUnique(
                            mainobjects, n.getData()) == 0; ) {

                    // deleting rows can free n out of the cache so we
                    // make sure it is loaded with up-to-date left-right-parent
                    n = n.getUpdatedNode();

                    // -- get the next node before n is deleted
                    Node     nextn = refindex.next(n);
                    Object[] rnd   = reftable.getNewRow();

                    System.arraycopy(n.getData(), 0, rnd, 0, rnd.length);

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
                            rnd[r_columns[j]] =
                                Column.convertObject(reftable
                                    .getColumn(r_columns[j])
                                    .getDefaultString(), reftable
                                    .getColumn(r_columns[j]).getType());
                        }

                        if (path.add(c)) {
                            reftable.checkCascadeUpdate(n.getRow(), rnd,
                                                        session, r_columns,
                                                        null, update, path);
                        }
                    } else {

                        // -- cascade; standard recursive call. We inherit values from the foreign key
                        // -- table therefor we set ref==this.
                        for (int j = 0; j < m_columns.length; j++) {
                            rnd[r_columns[j]] = nrow[m_columns[j]];
                        }

                        if (path.add(c)) {
                            reftable.checkCascadeUpdate(n.getRow(), rnd,
                                                        session, common,
                                                        this, update, path);
                        }
                    }

                    if (update) {
                        ri.add(rnd);
                        reftable.deleteNoRefCheck(n.getData(), session);

                        if (reftable == this) {
                            nextn = c.findFkRef(orow.getData(), false);
                        }
                    }

                    if (nextn == null) {
                        break;
                    }

                    n = nextn;
                }

                if (update) {
                    Record r = ri.rRoot;

                    while (r != null) {
                        reftable.insertNoCheck(r.data, session, true);

                        r = r.next;
                    }
                }
            }
        }
    }

    /**
     *  Highest level multiple row delete method. Corresponds to an SQL
     *  DELETE.
     */
    int delete(HsqlLinkedList del, Session c) throws HsqlException {

        Iterator it    = del.iterator();
        int      count = 0;
        Row      r;

        while (it.hasNext()) {
            r = (Row) it.next();

            delete(r, c, false);
        }

        fireAll(TriggerDef.DELETE_BEFORE);

        it = del.iterator();

        while (it.hasNext()) {
            r = (Row) it.next();

            delete(r, c, true);
        }

        fireAll(TriggerDef.DELETE_AFTER);

        return del.size();
    }

    /**
     *  High level row delete method. Fires triggers and performs integrity
     *  constraint checks.
     */
    private void delete(Row r, Session session,
                        boolean doit) throws HsqlException {

        if (database.isReferentialIntegrity()) {
            constraintPath.clear();
            checkCascadeDelete(r, session, doit, constraintPath);
        }

        if (doit &&!r.isDeleted()) {
            deleteNoRefCheck(r, session);
        }
    }

    /**
     *  Mid level row delete method. Fires triggers but no integrity
     *  constraint checks.
     */
    private void deleteNoRefCheck(Object row[],
                                  Session session) throws HsqlException {

        fireAll(TriggerDef.DELETE_BEFORE_ROW, row, null);
        deleteNoCheck(row, session, true);

        // fire the delete after statement trigger
        fireAll(TriggerDef.DELETE_AFTER_ROW, row, null);
    }

    /**
     *  Mid level row delete method. Fires triggers but no integrity
     *  constraint checks.
     */
    private void deleteNoRefCheck(Row r,
                                  Session session) throws HsqlException {

        fireAll(TriggerDef.DELETE_BEFORE_ROW, r.getData(), null);
        deleteNoCheck(r, session, true);

        // fire the delete after statement trigger
        fireAll(TriggerDef.DELETE_AFTER_ROW, r.getData(), null);
    }

    /**
     * Low level row delete method. Removes the row from the indexes and
     * from the Cache.
     */
    private void deleteNoCheck(Row r, Session c,
                               boolean log) throws HsqlException {

        Node     node;
        Object[] row = r.getData();

        r = r.getUpdatedRow();

        for (int i = iIndexCount - 1; i >= 0; i--) {
            node = r.getNode(i);

            getIndex(i).delete(node);
        }

        r = r.getUpdatedRow();

        r.delete();

        if (c != null) {
            c.addTransactionDelete(this, row);
        }

        if (log &&!isTemp &&!isText &&!isReadOnly
                && database.logger.hasLog()) {
            database.logger.writeToLog(c, getDeleteStatement(row));
        }
    }

    /**
     * Low level row delete method. Removes the row from the indexes and
     * from the Cache.
     */
    void deleteNoCheck(Object row[], Session c,
                       boolean log) throws HsqlException {

        Node node = getIndex(0).search(row);
        Row  r    = node.getRow();

        for (int i = iIndexCount - 1; i >= 0; i--) {
            node = r.getNode(i);

            getIndex(i).delete(node);
        }

        r = r.getUpdatedRow();

        r.delete();

        if (c != null) {
            c.addTransactionDelete(this, row);
        }

        if (log &&!isTemp &&!isText &&!isReadOnly
                && database.logger.hasLog()) {
            database.logger.writeToLog(c, getDeleteStatement(row));
        }
    }

    /**
     * Low level row delete method. Removes the row from the indexes and
     * from the Cache.
     */
    void deleteNoCheckRollback(Object row[], Session c,
                               boolean log) throws HsqlException {

        Node node = getIndex(0).search(row);
        Row  r    = node.getRow();

        for (int i = iIndexCount - 1; i >= 0; i--) {
            node = r.getNode(i);

            getIndex(i).delete(node);
        }

        r = r.getUpdatedRow();

        r.delete();

        if (log &&!isTemp &&!isText &&!isReadOnly
                && database.logger.hasLog()) {
            database.logger.writeToLog(c, getDeleteStatement(row));
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
     * rows. (fredt)
     */
    int update(HsqlLinkedList del, Result ins, int[] col,
               Session c) throws HsqlException {

        Iterator it = del.iterator();
        Record   ni = ins.rRoot;

        while (it.hasNext() && ni != null) {
            Row row = (Row) it.next();

            enforceFieldValueLimits(ni.data, col);

            // this means the identity column can be set to null to force
            // creation of a new identity value
            setIdentityColumn(ni.data, null);

            if (database.isReferentialIntegrity()) {
                constraintPath.clear();
                checkCascadeUpdate(row, ni.data, c, col, null, false,
                                   constraintPath);
            }

            ni = ni.next;
        }

        fireAll(TriggerDef.UPDATE_BEFORE);

        it = del.iterator();
        ni = ins.rRoot;

        while (it.hasNext() && ni != null) {
            Row row = (Row) it.next();

            constraintPath.clear();
            checkCascadeUpdate(row, ni.data, c, col, null, true,
                               constraintPath);
            deleteNoCheck(row, c, true);

            ni = ni.next;
        }

        it = del.iterator();
        ni = ins.rRoot;

        while (it.hasNext() && ni != null) {
            Row row = (Row) it.next();

            fireAll(TriggerDef.UPDATE_BEFORE_ROW, row.getData(), ni.data);
            insertNoCheck(ni.data, c, true);
            fireAll(TriggerDef.UPDATE_AFTER_ROW, row.getData(), ni.data);

            ni = ni.next;
        }

        fireAll(TriggerDef.UPDATE_AFTER);

        return del.size();
    }

    /**
     *  Method declaration
     *
     * @return
     */
    boolean isCached() {
        return isCached;
    }

    /**
     *  Method declaration
     *
     * @return
     */
    boolean isIndexCached() {
        return isCached;
    }

    /**
     *  Method declaration
     *
     * @param  s
     * @return
     */
    Index getIndex(String s) {

        for (int i = 0; i < iIndexCount; i++) {
            Index h = getIndex(i);

            if (s.equals(h.getName().name)) {
                return h;
            }
        }

        // no such index
        return null;
    }

    /**
     *  Return the position of the constraint within the list
     *
     * @param  s
     * @return
     */
    int getConstraintIndex(String s) {

        for (int j = 0, size = vConstraint.size(); j < size; j++) {
            Constraint tempc = (Constraint) vConstraint.get(j);

            if (tempc.getName().name.equals(s)) {
                return j;
            }
        }

        return -1;
    }

    /**
     *  return the named constriant
     *
     * @param  s
     * @return
     */
    Constraint getConstraint(String s) {

        int j = getConstraintIndex(s);

        if (j >= 0) {
            return (Constraint) vConstraint.get(j);
        } else {
            return null;
        }
    }

    /**
     *  Method declaration
     *
     * @param  i
     * @return
     */
    Column getColumn(int i) {
        return (Column) vColumn.get(i);
    }

    /**
     *  Method declaration
     *
     * @return
     */
    int[] getColumnTypes() {
        return colTypes;
    }

    /**
     *  Method declaration
     *
     * @param  i
     * @return
     */
    protected Index getIndex(int i) {
        return (Index) vIndex.get(i);
    }

    /**
     *  Method declaration
     *
     * @param  row
     * @return
     * @throws  HsqlException
     */
    String getInsertStatement(Object row[]) throws HsqlException {

        HsqlStringBuffer a = new HsqlStringBuffer(128);

        a.append("INSERT INTO ");
        a.append(tableName.statementName);
        a.append(" VALUES(");

        for (int i = 0; i < iVisibleColumns; i++) {
            a.append(Column.createSQLString(row[i], getColumn(i).getType()));
            a.append(',');
        }

        a.setCharAt(a.length() - 1, ')');

        return a.toString();
    }

    /**
     *  Method declaration
     *
     * @param  row
     * @return
     * @throws  HsqlException
     */
    private String getDeleteStatement(Object row[]) throws HsqlException {

        HsqlStringBuffer a = new HsqlStringBuffer(128);

        a.append("DELETE FROM ");
        a.append(tableName.statementName);
        a.append(" WHERE ");

        if (iPrimaryKey[0] == iVisibleColumns) {
            for (int i = 0; i < iVisibleColumns; i++) {
                Column c = getColumn(i);

                a.append(c.columnName.statementName);
                a.append('=');
                a.append(Column.createSQLString(row[i], c.getType()));

                if (i < iVisibleColumns - 1) {
                    a.append(" AND ");
                }
            }
        } else {
            for (int i = 0; i < iPrimaryKey.length; i++) {
                Column c = getColumn(iPrimaryKey[i]);

                a.append(c.columnName.statementName);
                a.append('=');
                a.append(Column.createSQLString(row[iPrimaryKey[i]],
                                                c.getType()));

                if (i < iPrimaryKey.length - 1) {
                    a.append(" AND ");
                }
            }
        }

        return a.toString();
    }

    /**
     *  Used by CACHED tables to fetch a Row from the Cache, resulting in the
     *  Row being read from disk if it is not in the Cache.
     *
     *  TEXT tables pass the memory resident Node parameter so that the Row
     *  and its index Nodes can be relinked.
     *
     * @param  pos
     * @return
     * @throws  HsqlException
     */
    CachedRow getRow(int pos, Node primarynode) throws HsqlException {

        if (isCached) {
            return cache.getRow(pos, this);
        }

        return null;
    }

    void addRowToStore(Row r) throws HsqlException {

        if (isCached && cache != null) {
            cache.add((CachedRow) r);

//            r.getData()[iVisibleColumns] = new Integer(((CachedRow)r).iPos);
//            r.getData()[iVisibleColumns] = ValuePool.getInt(((CachedRow)r).iPos);
        } else if (needsRowID) {

            // fredt - this is required when there is a non-primary index
            // and a user defined pk - should reduce the cases where it is
            // necessary
//            r.getData()[iVisibleColumns] = new Integer((int)rowIdSequence.getValue());
            r.getData()[iVisibleColumns] =
                ValuePool.getInt((int) rowIdSequence.getValue());
        }
    }

    void registerRow(Row r) {

        if (isCached && cache != null && needsRowID) {
            r.getData()[iVisibleColumns] = new Integer(((CachedRow) r).iPos);
        }
    }

    void removeRow(CachedRow r) throws HsqlException {

        if (cache != null) {
            cache.free(r);
        }
    }

    void indexRow(Row r) throws HsqlException {

        int i = 0;

        try {
            Node n = null;

            for (; i < iIndexCount; i++) {
                n = r.getNextNode(n);

                getIndex(i).insert(n);
            }
        } catch (HsqlException e) {

            // unique index violation - rollback insert
            for (--i; i >= 0; i--) {
                Node n = r.getNode(i);

                getIndex(i).delete(n);
            }

            r.delete();

            throw e;    // and throw error again
        }
    }

    /**
     * Currently only for temp system tables.
     */
    void clearAllRows() throws HsqlException {
        Trace.check(isTemp, Trace.OPERATION_NOT_SUPPORTED);
        setIndexRootsNull();
    }

    void drop() throws HsqlException {

        if (cache != null &&!isEmpty()) {
            cache.remove(this);
        }
    }

    boolean isWritable() {
        return !isReadOnly &&!database.databaseReadOnly
               &&!(database.filesReadOnly && (isCached || isText));
    }

    String getCatalogName() {

        if (database == null
                || database.getProperties().isPropertyTrue(
                    "hsqldb.catalogs")) {
            return null;
        }

        return database.getPath();
    }

    String getSchemaName() {

        if (database == null
                || database.getProperties().isPropertyTrue(
                    "hsqldb.schemas")) {
            return null;
        }

        if (tableType == SYSTEM_TABLE) {
            return "DEFINITION_SCHEMA";
        } else if (tableType == SYSTEM_VIEW) {
            return "INFORMATION_SCHEMA";
        } else if (isTemp) {
            Session s = database.sessionManager.getSession(ownerSessionId);

            return (s != null && s.getId() == ownerSessionId)
                   ? s.getUsername()
                   : null;
        } else {
            return "PUBLIC";
        }
    }
}
