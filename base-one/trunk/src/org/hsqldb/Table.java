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

import java.sql.SQLException;
import java.sql.Types;
import org.hsqldb.lib.ArrayUtil;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.HsqlHashMap;
import org.hsqldb.lib.HsqlStringBuffer;
import org.hsqldb.lib.StringUtil;

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
// kloska@users 20021030 - PATCH 1.7.2 - ON UPDATE CASCADE | SET NULL | SET DEFAULT
// kloska@users 20021112 - PATCH 1.7.2 - ON DELETE SET NULL | SET DEFAULT

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

    // name of the column added to tables without primary key
    static final String DEFAULT_PK = "";

    // main properties
    private HsqlArrayList vColumn;            // columns in table
    private HsqlArrayList vIndex;             // vIndex(0) is the primary key index
    private int[]         iPrimaryKey;        // column numbers for primary key
    protected int         iIndexCount;        // size of vIndex
    private int           iIdentityColumn;    // -1 means no such row
    protected int         iIdentityId;        // next value of identity column
    HsqlArrayList         vConstraint;        // constrainst for the table
    HsqlArrayList         vTrigs[];           // array of trigger lists
    private int[]         colTypes;           // fredt - types of columns
    private boolean       isSystem;
    private boolean       isText;
    private boolean       isView;

    // properties for subclasses
    protected int      iColumnCount;          // inclusive the hidden primary key
    protected int      iVisibleColumns;       // exclusive of hidden primary key
    protected Database dDatabase;
    protected Cache    cCache;
    protected HsqlName tableName;             // SQL name
    protected int      tableType;
    protected Session  ownerSession;          // fredt - set for temp tables only
    protected boolean  isReadOnly;
    protected boolean  isTemp;
    protected boolean  isCached;
    protected int      indexType;             // fredt - type of index used

    /**
     *  Constructor declaration
     *
     * @param  db
     * @param  isTemp
     * @param  name
     * @param  cached
     * @param  nameQuoted        Description of the Parameter
     * @exception  SQLException  Description of the Exception
     */
    Table(Database db, HsqlName name, int type,
            Session session) throws SQLException {

        dDatabase = db;

        switch (type) {

            case SYSTEM_TABLE :
                isTemp = true;
                break;

            case TEMP_TABLE :
                Trace.doAssert(session != null);

                isTemp       = true;
                ownerSession = session;
                break;

            case CACHED_TABLE :
                cCache = db.logger.getCache();

                if (cCache != null) {
                    isCached = true;
                } else {
                    type = MEMORY_TABLE;
                }
                break;

            case TEMP_TEXT_TABLE :
                Trace.doAssert(session != null);

                if (!db.logger.hasLog()) {
                    throw Trace.error(Trace.DATABASE_IS_MEMORY_ONLY);
                }

                isTemp       = true;
                isText       = true;
                isReadOnly   = true;
                isCached     = true;
                ownerSession = session;
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
        tableType       = type;
        tableName       = name;
        iPrimaryKey     = null;
        iIdentityColumn = -1;
        vColumn         = new HsqlArrayList();
        vIndex          = new HsqlArrayList();
        vConstraint     = new HsqlArrayList();
        vTrigs          = new HsqlArrayList[TriggerDef.numTrigs()];    // defer init...should be "pay to use"

        for (int vi = 0; vi < TriggerDef.numTrigs(); vi++) {
            vTrigs[vi] = new HsqlArrayList();    // defer init...should be "pay to use"
        }
    }

    boolean equals(String other, Session c) {

        if (isTemp && c.getId() != ownerSession.getId()) {
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

    void setDataReadOnly(boolean value) throws SQLException {
        isReadOnly = value;
    }

    Session getOwnerSession() {
        return ownerSession;
    }

    protected void setDataSource(String source, boolean isDesc,
                                 Session s) throws SQLException {

        // Same exception as setIndexRoots.
        throw (Trace.error(Trace.TABLE_NOT_FOUND));
    }

    protected String getDataSource() throws SQLException {
        return null;
    }

    protected boolean isDescDataSource() throws SQLException {
        return (false);
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

    /**
     *  Get the index supporting a constraint that can be used as an index
     *  of the given type and index column signature.
     *
     * @param  col column list array
     * @param  unique for the index
     * @return
     */
    Index getConstraintIndexForColumns(int[] col, boolean unique) {

        Index currentIndex = getPrimaryIndex();

        if (ArrayUtil.haveEquality(currentIndex.getColumns(), col,
                                   col.length, unique)) {
            return currentIndex;
        }

        for (int i = 0, size = vConstraint.size(); i < size; i++) {
            Constraint c = (Constraint) vConstraint.get(i);

            currentIndex = c.getMainIndex();

            if (ArrayUtil.haveEquality(currentIndex.getColumns(), col,
                                       col.length, unique)) {
                return currentIndex;
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
     * @throws  SQLException
     */
    void addColumn(String name, int type) throws SQLException {

        Column column = new Column(new HsqlName(name, false), true, type, 0,
                                   0, false, false, null);

        addColumn(column);
    }

// fredt@users 20020220 - patch 475199 - duplicate column

    /**
     *  Performs the table level checks and adds a column to the table at the
     *  DDL level.
     *
     * @param  column new column to add
     * @throws  SQLException when table level checks fail
     */
    void addColumn(Column column) throws SQLException {

        if (searchColumn(column.columnName.name) >= 0) {
            throw Trace.error(Trace.COLUMN_ALREADY_EXISTS);
        }

        if (column.isIdentity()) {
            Trace.check(column.getType() == Types.INTEGER,
                        Trace.WRONG_DATA_TYPE, column.columnName.name);
            Trace.check(iIdentityColumn == -1, Trace.SECOND_PRIMARY_KEY,
                        column.columnName.name);

            iIdentityColumn = iColumnCount;
        }

        Trace.doAssert(iPrimaryKey == null, "Table.addColumn");
        vColumn.add(column);

        iColumnCount++;
    }

    /**
     *  Method declaration
     *
     * @param  result
     * @throws  SQLException
     */
    void addColumns(Result result) throws SQLException {

        int colCount = result.getColumnCount();

        for (int i = 0; i < colCount; i++) {
            Column column = new Column(
                new HsqlName(result.sLabel[i], result.isLabelQuoted[i]),
                true, result.colType[i], result.colSize[i],
                result.colScale[i], false, false, null);

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
     * @throws  SQLException
     */
    void setName(String name, boolean isquoted) {

        tableName.rename(name, isquoted);

        if (HsqlName.isReservedName(getPrimaryIndex().getName().name)) {
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

    protected Table duplicate() throws SQLException {

        Table t = (new Table(dDatabase, tableName, tableType, ownerSession));

        return t;
    }

    /**
     * Match two columns arrays for length and type of coluns
     *
     * @param col column array from this Table
     * @param other the other Table object
     * @param othercol column array from the other Table
     * @throws SQLException if there is a mismatch
     */
    void checkColumnsMatch(int[] col, Table other,
                           int[] othercol) throws SQLException {

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
     * @throws  SQLException
     */
    Table moveDefinition(String withoutindex, Column newcolumn, int colindex,
                         int adjust) throws SQLException {

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
            // in future we can drop signle column pk wih no fk reference
            if (primarykey.length != newpk.length) {
                throw Trace.error(Trace.DROP_PRIMARY_KEY);
            } else {
                primarykey = newpk;
            }
        }

// tony_lai@users - 20020820 - patch 595099 - primary key names
        tn.createPrimaryKey(getIndex(0).getName(), primarykey);

        tn.vConstraint = vConstraint;

        for (int i = 1; i < getIndexCount(); i++) {
            Index idx = getIndex(i);

            if (withoutindex != null
                    && idx.getName().name.equals(withoutindex)) {
                continue;
            }

            Index newidx = tn.createAdjustedIndex(idx, colindex, adjust);

            if (newidx == null) {

                // fredt - todo - better error message
                throw Trace.error(Trace.INDEX_ALREADY_EXISTS);
            }
        }

        return tn;
    }

    void updateConstraints(Table to, int colindex,
                           int adjust) throws SQLException {

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
        return iIdentityColumn;
    }

    /**
     *  Method declaration
     *
     * @param  c
     * @return
     * @throws  SQLException
     */
    int getColumnNr(String c) throws SQLException {

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

        for (int i = 0; i < iColumnCount; i++) {
            if (c.equals(((Column) vColumn.get(i)).columnName.name)) {
                return i;
            }
        }

        return -1;
    }

    /**
     *  Method declaration
     *
     * @return
     * @throws  SQLException
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
     * @param  column
     * @return
     * @throws  SQLException
     */
    Index getIndexForColumn(int column) throws SQLException {

        for (int i = 0; i < iIndexCount; i++) {
            Index h = getIndex(i);

            if (h.getColumns()[0] == column) {
                return h;
            }
        }

        return null;
    }

    /**
     *  Finds an existing index for a foreign key column group
     *
     * @param  col
     * @return
     * @throws  SQLException
     */
    Index getIndexForColumns(int col[], boolean unique) throws SQLException {

        for (int i = 0; i < iIndexCount; i++) {
            Index currentindex = getIndex(i);
            int   indexcol[]   = currentindex.getColumns();

            if (ArrayUtil.haveEquality(indexcol, col, col.length, unique)) {
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
    int[] getIndexRootsArray() throws SQLException {

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
     * @throws  SQLException
     */
    String getIndexRoots() throws SQLException {

        Trace.doAssert(isCached, "Table.getIndexRootData");

        String roots = StringUtil.getList(getIndexRootsArray(), " ", "");
        HsqlStringBuffer s = new HsqlStringBuffer(roots);

        s.append(' ');
        s.append(iIdentityId);

        return s.toString();
    }

    /**
     *  Method declaration
     *
     * @param  s
     * @throws  SQLException
     */
    void setIndexRoots(int[] roots) throws SQLException {

        Trace.check(isCached, Trace.TABLE_NOT_FOUND);

        for (int i = 0; i < iIndexCount; i++) {
            int p = roots[i];
            Row r = null;

            if (p != -1) {
                r = cCache.getRow(p, this);
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
     * @throws  SQLException
     */
    void setIndexRoots(String s) throws SQLException {

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

        iIdentityId = Integer.parseInt(s.substring(j));
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
     *  Shortcut for creating default PK's
     *
     * @throws  SQLException
     */
    void createPrimaryKey() throws SQLException {

// tony_lai@users 20020820 - patch 595099
        createPrimaryKey(null, null);
    }

    /**
     *  Adds the SYSTEM_ID column if no primary key is specified in DDL.
     *  Creates a single or multi-column primary key and index. sets the
     *  colTypes array. Finalises the creation of the table. (fredt@users)
     *
     * @param columns primary key column(s) or null if no primary key in DDL
     * @throws  SQLException
     */

// tony_lai@users 20020820 - patch 595099
    void createPrimaryKey(HsqlName pkName,
                          int[] columns) throws SQLException {

        Trace.doAssert(iPrimaryKey == null, "Table.createPrimaryKey(column)");

        iVisibleColumns = iColumnCount;

        if (columns == null) {
            columns = new int[]{ iColumnCount };

            Column column = new Column(new HsqlName(DEFAULT_PK, false),
                                       false, Types.INTEGER, 0, 0, true,
                                       true, null);

            addColumn(column);
        } else {
            for (int i = 0; i < columns.length; i++) {
                getColumn(columns[i]).setNullable(false);
                getColumn(columns[i]).setPrimaryKey(true);
            }
        }

        iPrimaryKey = columns;

// tony_lai@users 20020820 - patch 595099
        HsqlName name = pkName != null ? pkName
                                       : new HsqlName("SYS_PK",
                                           tableName.name,
                                           tableName.isNameQuoted);

        createIndexPrivate(columns, name, true);

        colTypes = new int[iColumnCount];

        for (int i = 0; i < iColumnCount; i++) {
            colTypes[i] = getColumn(i).getType();
        }
    }

    /**
     *  Create new index taking into account removal or addition a column of
     *  the table.
     *
     * @param  index
     * @param  colindex
     * @param  ajdust -1 or 0 or 1
     * @return new index or null if a column is removed from index
     * @throws  SQLException
     */
    private Index createAdjustedIndex(Index index, int colindex,
                                      int adjust) throws SQLException {

        int[] colarr = ArrayUtil.getAdjustedColumnArray(index.getColumns(),
            index.getVisibleColumns(), colindex, adjust);

        if (colarr.length != index.getVisibleColumns()) {
            return null;
        }

        return createIndexPrivate(colarr, index.getName(), index.isUnique());
    }

    /**
     *  Create new memory resident index. For MEMORY and TEXT tables.
     *
     * @param  column
     * @param  name
     * @param  unique
     */
    Index createIndex(int column[], HsqlName name,
                      boolean unique) throws SQLException {

        Index newindex     = createIndexPrivate(column, name, unique);
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
        } catch (SQLException e) {
            error = Trace.VIOLATION_OF_UNIQUE_INDEX;
        }

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

        throw Trace.error(error);
    }

    /**
     *  Method declaration
     *
     * @param  column
     * @param  name
     * @param  unique
     * @return                Description of the Return Value
     * @throws  SQLException
     */
    Index createIndexPrivate(int column[], HsqlName name,
                             boolean unique) throws SQLException {

        Trace.doAssert(iPrimaryKey != null, "createIndex");

        int s = column.length;
        int t = iPrimaryKey.length;

        // The primary key field is added for non-unique indexes
        // making all indexes unique
        int col[]  = new int[unique ? s
                                    : s + t];
        int type[] = new int[unique ? s
                                    : s + t];

        for (int j = 0; j < s; j++) {
            col[j]  = column[j];
            type[j] = getColumn(col[j]).getType();
        }

        if (!unique) {
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

// fredt@users 20020225 - comment
// in future we can avoid duplicate indexes
/*
        for (int i = 0; i < iIndexCount; i++) {
            if ( newindex.isEquivalent(getIndex(i))){
                return;
            }
        }
*/

//        Trace.doAssert(isEmpty(), "createIndex");
        vIndex.add(newindex);

        iIndexCount = vIndex.size();

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
     * @throws  SQLException if index is used in a constraint
     */
    void checkDropIndex(String indexname,
                        HsqlHashMap ignore) throws SQLException {

        Index index = this.getIndex(indexname);

        if (index == null) {
            throw Trace.error(Trace.INDEX_NOT_FOUND, indexname);
        }

        if (index.equals(getIndex(0))) {
            throw Trace.error(Trace.DROP_PRIMARY_KEY, indexname);
        }

        for (int i = 0, size = vConstraint.size(); i < size; i++) {
            Constraint c = (Constraint) vConstraint.get(i);

            if (ignore.get(c) != null) {
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
     *  Method declaration
     *
     * @return
     */
    Object[] getNewRow() {
        return new Object[iColumnCount];
    }

    void dropIndex(String indexname) throws SQLException {

        // find the array index for indexname and remove
        int todrop = 1;

        for (; todrop < getIndexCount(); todrop++) {
            Index tempidx = getIndex(todrop);

            if (tempidx.getName().name.equals(indexname)) {
                vIndex.remove(todrop);

                iIndexCount = vIndex.size();

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
     * @throws  SQLException normally for lack of resources
     */
    void moveData(Table from, int colindex, int adjust) throws SQLException {

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
    }

    /**
     *  Method declaration
     *
     * @param  col
     * @param  deleted
     * @param  inserted
     * @throws  SQLException
     */
    void checkUpdate(int col[], Result deleted,
                     Result inserted) throws SQLException {

        Trace.check(!isReadOnly, Trace.DATA_IS_READONLY);

        if (dDatabase.isReferentialIntegrity()) {
            for (int i = 0, size = vConstraint.size(); i < size; i++) {
                Constraint v = (Constraint) vConstraint.get(i);

                v.checkUpdate(col, deleted, inserted);
            }
        }
    }

    /**
     *  Method declaration
     *
     * @param  result
     * @param  c
     * @throws  SQLException
     */
    void insert(Result result, Session c) throws SQLException {

        // if violation of constraints can occur, insert must be rolled back
        // outside of this function!
        Record r   = result.rRoot;
        int    len = result.getColumnCount();

        while (r != null) {
            Object row[] = getNewRow();

            for (int i = 0; i < len; i++) {
                row[i] = r.data[i];
            }

            insert(row, c);

            r = r.next;
        }
    }

    /**
     *  Method declaration
     *
     * @param  row
     * @param  c
     * @throws  SQLException
     */
    void insert(Object row[], Session c) throws SQLException {

        Trace.check(!isReadOnly, Trace.DATA_IS_READONLY);
        fireAll(TriggerDef.INSERT_BEFORE, row);

        if (dDatabase.isReferentialIntegrity()) {
            for (int i = 0, size = vConstraint.size(); i < size; i++) {
                ((Constraint) vConstraint.get(i)).checkInsert(row);
            }
        }

        insertNoCheck(row, c, true);
        fireAll(TriggerDef.INSERT_AFTER, row);
    }

    protected void checkNullColumns(Object[] row) throws SQLException {

        for (int i = 0; i < iColumnCount; i++) {
            if (row[i] == null) {
                Column  col    = getColumn(i);
                boolean nullOK = col.isNullable() || col.isIdentity();

                if (!nullOK) {
                    Trace.throwerror(Trace.TRY_TO_INSERT_NULL,
                                     "column: " + col.columnName.name
                                     + " table: " + tableName.name);
                }
            }
        }
    }

    protected void setIdentityColumn(Object[] row) {

        if (iIdentityColumn != -1) {
            Number id = (Number) row[iIdentityColumn];

            if (id == null) {
                row[iIdentityColumn] = new Integer(iIdentityId);
            } else {
                int columnId = id.intValue();

                if (iIdentityId < columnId) {
                    iIdentityId = columnId;
                }
            }
        }
    }

    /**
     *  Method declaration
     *
     * @param  row
     * @param  c
     * @param  log
     * @throws  SQLException
     */
    void insertNoCheck(Object row[], Session c,
                       boolean log) throws SQLException {

        int nextId = iIdentityId;

        checkNullColumns(row);
        setIdentityColumn(row);

        Row r = Row.newRow(this, row);

        indexRow(r);

        if (c != null) {
            c.setLastIdentity(iIdentityId);
            c.addTransactionInsert(this, row);
        }

        if (iIdentityId >= nextId) {
            iIdentityId++;
        }

        if (log &&!isTemp &&!isText &&!isReadOnly
                && dDatabase.logger.hasLog()) {
            dDatabase.logger.writeToLog(c, getInsertStatement(row));
        }
    }

    /**
     *  Method declaration
     *
     * @param  trigVecIndx
     * @param  row
     */
    void fireAll(int trigVecIndx, Object row[]) {

        if (!dDatabase.isReferentialIntegrity()) {    // reloading db
            return;
        }

        HsqlArrayList trigVec = vTrigs[trigVecIndx];

        for (int i = 0, size = trigVec.size(); i < size; i++) {
            TriggerDef td = (TriggerDef) trigVec.get(i);

            td.push(row);    // tell the trigger thread to fire with this row
        }
    }

// statement-level triggers

    /**
     *  Method declaration
     *
     * @param  trigVecIndx
     */
    void fireAll(int trigVecIndx) {

        Object row[] = new Object[1];

        row[0] = new String("Statement-level");

        fireAll(trigVecIndx, row);
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
     *  in the a table.(fredt@users)
     *
     * @param  row
     * @param  session
     * @param  delete
     * @throws  SQLException
     */
    void checkCascadeDelete(Object[] row, Session session,
                            boolean doIt) throws SQLException {

        for (int i = 0; i < vConstraint.size(); i++) {
            Constraint c = (Constraint) vConstraint.get(i);

            if (c.getType() != Constraint.MAIN || c.getRef() == null) {
                continue;
            }

            Node refnode = c.findFkRef(row, true);

            if (refnode == null) {

                // no referencing row found
                continue;
            }

            Table reftable = c.getRef();

            // shortcut when deltable has no imported constraint
            boolean hasref =
                reftable.getNextConstraintIndex(0, Constraint.MAIN) != -1;

            // fredt - todo - to avoid infinite recursion on same table FK's
            // if (reftable == this) we don't need to go further and can return ??
            if (doIt == false && hasref == false) {
                return;
            }

            // -- result set for records to be inserted if this is
            // -- a 'ON DELETE SET [NULL|DEFAULT]' constraint
            Result   ri          = new Result();
            Index    refindex    = c.getRefIndex();
            int      m_columns[] = c.getMainColumns();
            int      r_columns[] = c.getRefColumns();
            Object[] m_objects   = new Object[m_columns.length];

            ArrayUtil.copyColumnValues(row, m_columns, m_objects);

            // walk the index for all the nodes that reference delnode
            for (Node n = refnode;
                    refindex.comparePartialRowNonUnique(
                        m_objects, n.getData()) == 0; ) {

                // get the next node before n is deleted
                Node nextn = refindex.next(n);

                // -- if the constraint is an 'SET [DEFAULT|NULL]' constraint we have to remember
                // -- a new record to be inserted after deleting the current. We also have to
                // -- switch over to the 'checkCascadeUpdate' method afterwords
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
                        reftable.checkCascadeUpdate(n.getData(), rnd,
                                                    session, r_columns, null,
                                                    doIt);
                    }

                    if (doIt) {
                        ri.add(rnd);
                    }
                } else if (hasref) {

                    // fredt - todo - to avoid infinite recursion on same table
                    // check here if n refers to same row ??
                    reftable.checkCascadeDelete(n.getData(), session, doIt);
                }

                if (doIt) {
                    reftable.deleteNoRefCheck(n.getData(), session);

                    //  foreign key referencing own table
                    if (reftable == this) {
                        nextn = c.findFkRef(row, true);
                    }
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
     * Check or perform and update cascade operation on a single row.
     *   Check or cascade an update (delete/insert) operation.
     *   The method takes a pair of rows (new data,old data) and checks
     *   if <code>Constraints</code> permit the update operation.
     *   A boolean arguement determines if the operation should
     *   realy take place or if we just have to check for constraint violation.
     *
     *
     *
     *   @param orow Object[]; old row data to be deleted.
     *   @param nrow Object[]; new row data to be inserted.
     *   @param session Session; current database session
     *   @param cols int[]; indices of the columns actually changed.
     *   @param ref Table; This should be initialized to <code>null</code> when the
     *   method is called from the 'outside'. During recursion this will be the
     *   current table (i.e. <code>this</code>) to indicate from where we came.
     *   Foreign keys to this table do not have to be checked since they have
     *   triggered the update and are valid <i>per definitionem</i>.
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
    void checkCascadeUpdate(Object[] orow, Object[] nrow, Session session,
                            int[] cols, Table ref,
                            boolean update) throws SQLException {

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

                int  m_columns[] = c.getMainColumns();
                int  r_columns[] = c.getRefColumns();
                Node refnode     = c.findFkRef(orow, false);

                if (refnode == null) {

                    // no referencing row found
                    continue;
                }

                Table reftable = c.getRef();

                // -- shortcut when update table has no imported constraint
                boolean hasref =
                    reftable.getNextConstraintIndex(0, Constraint.MAIN) != -1;
                Index    refindex    = c.getRefIndex();
                Object[] mainobjects = new Object[m_columns.length];
                Object[] refobjects  = new Object[r_columns.length];

                ArrayUtil.copyColumnValues(orow, m_columns, mainobjects);
                ArrayUtil.copyColumnValues(nrow, r_columns, refobjects);

                // -- walk the index for all the nodes that reference update node
                Result ri = new Result();

                for (Node n = refnode;
                        refindex.comparePartialRowNonUnique(
                            mainobjects, n.getData()) == 0; ) {

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

                        reftable.checkCascadeUpdate(n.getData(), rnd,
                                                    session, r_columns, null,
                                                    update);
                    } else {

                        // -- cascade; standard recursive call. We inherit values from the foreign key
                        // -- table therefor we set ref==this.
                        for (int j = 0; j < m_columns.length; j++) {
                            rnd[r_columns[j]] = nrow[m_columns[j]];
                        }

                        reftable.checkCascadeUpdate(n.getData(), rnd,
                                                    session, common, this,
                                                    update);
                    }

                    if (update) {
                        ri.add(rnd);
                        reftable.deleteNoRefCheck(n.getData(), session);

                        if (reftable == this) {
                            nextn = c.findFkRef(orow, false);
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
     *  Method declaration
     *
     * @param  row
     * @param  session        Description of the Parameter
     * @throws  SQLException
     */
    void delete(Object row[], Session session) throws SQLException {

        fireAll(TriggerDef.DELETE_BEFORE_ROW, row);

        if (dDatabase.isReferentialIntegrity()) {
            checkCascadeDelete(row, session, false);
            checkCascadeDelete(row, session, true);
        }

        deleteNoCheck(row, session, true);

        // fire the delete after statement trigger
        fireAll(TriggerDef.DELETE_AFTER_ROW, row);
    }

    /**
     *  Method declaration
     *
     * @param  row
     * @param  session        Description of the Parameter
     * @throws  SQLException
     */
    private void deleteNoRefCheck(Object row[],
                                  Session session) throws SQLException {

        fireAll(TriggerDef.DELETE_BEFORE_ROW, row);
        deleteNoCheck(row, session, true);

        // fire the delete after statement trigger
        fireAll(TriggerDef.DELETE_AFTER_ROW, row);
    }

    /**
     *  Method declaration
     *
     * @param  row
     * @param  c
     * @param  log
     * @throws  SQLException
     */
    void deleteNoCheck(Object row[], Session c,
                       boolean log) throws SQLException {

        Node node = getIndex(0).search(row);
        Row  r    = node.getRow();

        r.lockRow();

        for (int i = 0; i < iIndexCount; i++) {
            getIndex(i).delete(node);

            node = r.getNextNode(node);
        }

        r.delete();

        if (c != null) {
            c.addTransactionDelete(this, row);
        }

        if (log &&!isTemp &&!isText &&!isReadOnly
                && dDatabase.logger.hasLog()) {
            dDatabase.logger.writeToLog(c, getDeleteStatement(row));
        }
    }

    /**
     *  Method declaration
     *
     * @param  row
     * @return
     * @throws  SQLException
     */
    String getInsertStatement(Object row[]) throws SQLException {

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
     * @throws  SQLException
     */
    private String getDeleteStatement(Object row[]) throws SQLException {

        HsqlStringBuffer a = new HsqlStringBuffer(128);

        a.append("DELETE FROM ");
        a.append(tableName.statementName);
        a.append(" WHERE ");

        if (iVisibleColumns < iColumnCount) {
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
     *  Method declaration
     *
     * @param  pos
     * @return
     * @throws  SQLException
     */
    CachedRow getRow(int pos, Node primarynode) throws SQLException {

        if (isCached) {
            return cCache.getRow(pos, this);
        }

        return null;
    }

    void putRow(CachedRow r) throws SQLException {

        int size = 0;

        if (cCache != null) {
            cCache.add(r);
        }
    }

    void removeRow(CachedRow r) throws SQLException {

        if (cCache != null) {
            cCache.free(r);
        }
    }

    void indexRow(Row r) throws SQLException {

        r.lockRow();

        int i = 0;

        try {
            Node n = null;

            for (; i < iIndexCount; i++) {
                n = r.getNextNode(n);

                getIndex(i).insert(n);
            }

            r.unlockRow();
        } catch (SQLException e) {

            // unique index violation - rollback insert
            for (--i; i >= 0; i--) {
                Node n = r.getNode(i);

                getIndex(i).delete(n);
            }

            r.delete();
            r.unlockRow();

            throw e;    // and throw error again
        }
    }

    void drop() throws SQLException {

        if (cCache != null) {
            cCache.remove(this);
        }
    }
}
