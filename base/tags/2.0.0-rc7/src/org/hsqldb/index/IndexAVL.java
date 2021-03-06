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
 * Copyright (c) 2001-2009, The HSQL Development Group
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


package org.hsqldb.index;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.hsqldb.HsqlNameManager;
import org.hsqldb.HsqlNameManager.HsqlName;
import org.hsqldb.OpTypes;
import org.hsqldb.Row;
import org.hsqldb.RowAVL;
import org.hsqldb.SchemaObject;
import org.hsqldb.Session;
import org.hsqldb.Table;
import org.hsqldb.TableBase;
import org.hsqldb.Tokens;
import org.hsqldb.TransactionManager;
import org.hsqldb.error.Error;
import org.hsqldb.error.ErrorCode;
import org.hsqldb.lib.ArrayUtil;
import org.hsqldb.lib.OrderedHashSet;
import org.hsqldb.navigator.RowIterator;
import org.hsqldb.persist.PersistentStore;
import org.hsqldb.rights.Grantee;
import org.hsqldb.types.Type;
import org.hsqldb.Constraint;

// fredt@users 20020221 - patch 513005 by sqlbob@users - corrections
// fredt@users 20020225 - patch 1.7.0 - changes to support cascading deletes
// tony_lai@users 20020820 - patch 595052 - better error message
// fredt@users 20021205 - patch 1.7.2 - changes to method signature
// fredt@users - patch 1.8.0 - reworked the interface and comparison methods
// fredt@users - patch 1.8.0 - improved reliability for cached indexes
// fredt@users - patch 1.9.0 - iterators and concurrency

/**
 * Implementation of an AVL tree with parent pointers in nodes. Subclasses
 * of Node implement the tree node objects for memory or disk storage. An
 * Index has a root Node that is linked with other nodes using Java Object
 * references or file pointers, depending on Node implementation.<p>
 * An Index object also holds information on table columns (in the form of int
 * indexes) that are covered by it.<p>
 *
 *  New class derived from Hypersonic SQL code and enhanced in HSQLDB. <p>
 *
 * @author Thomas Mueller (Hypersonic SQL Group)
 * @author Fred Toussi (fredt@users dot sourceforge.net)
 * @version 1.9.0
 * @since Hypersonic SQL
 */
public class IndexAVL implements Index {

    // fields
    private final long       persistenceId;
    protected final HsqlName name;
    private final boolean[]  colCheck;
    final int[]              colIndex;
    private final int[]      defaultColMap;
    final Type[]             colTypes;
    private final boolean[]  colDesc;
    private final boolean[]  nullsLast;
    final boolean            isSimpleOrder;
    final boolean            isSimple;
    protected final boolean  isPK;        // PK with or without columns
    protected final boolean  isUnique;    // DDL uniqueness
    protected final boolean  isConstraint;
    private final boolean    isForward;
    private int              depth;
    private static final IndexRowIterator emptyIterator =
        new IndexRowIterator(null, (PersistentStore) null, null, null, false);
    protected TableBase table;
    int                 position;

    //
    ReadWriteLock lock      = new ReentrantReadWriteLock();
    Lock          readLock  = lock.readLock();
    Lock          writeLock = lock.writeLock();

    /**
     * Constructor declaration
     *
     * @param name HsqlName of the index
     * @param id persistnece id
     * @param table table of the index
     * @param columns array of column indexes
     * @param descending boolean[]
     * @param nullsLast boolean[]
     * @param colTypes array of column types
     * @param pk if index is for a primary key
     * @param unique is this a unique index
     * @param constraint does this index belonging to a constraint
     * @param forward is this an auto-index for an FK that refers to a table
     *   defined after this table
     */
    public IndexAVL(HsqlName name, long id, TableBase table, int[] columns,
                    boolean[] descending, boolean[] nullsLast,
                    Type[] colTypes, boolean pk, boolean unique,
                    boolean constraint, boolean forward) {

        persistenceId  = id;
        this.name      = name;
        this.colIndex  = columns;
        this.colTypes  = colTypes;
        this.colDesc   = descending == null ? new boolean[columns.length]
                                            : descending;
        this.nullsLast = nullsLast == null ? new boolean[columns.length]
                                           : nullsLast;
        isPK           = pk;
        isUnique       = unique;
        isConstraint   = constraint;
        isForward      = forward;
        this.table     = table;
        colCheck       = table.getNewColumnCheckList();

        ArrayUtil.intIndexesToBooleanArray(colIndex, colCheck);

        defaultColMap = new int[columns.length];

        ArrayUtil.fillSequence(defaultColMap);

        boolean simpleOrder = colIndex.length > 0;

        for (int i = 0; i < colDesc.length; i++) {
            if (this.colDesc[i] || this.nullsLast[i]) {
                simpleOrder = false;
            }
        }

        isSimpleOrder = simpleOrder;
        isSimple      = isSimpleOrder && colIndex.length == 1;
    }

    // SchemaObject implementation
    public int getType() {
        return SchemaObject.INDEX;
    }

    public HsqlName getName() {
        return name;
    }

    public HsqlName getCatalogName() {
        return name.schema.schema;
    }

    public HsqlName getSchemaName() {
        return name.schema;
    }

    public Grantee getOwner() {
        return name.schema.owner;
    }

    public OrderedHashSet getReferences() {
        return new OrderedHashSet();
    }

    public OrderedHashSet getComponents() {
        return null;
    }

    public void compile(Session session, SchemaObject parentObject) {}

    public String getSQL() {

        StringBuffer sb = new StringBuffer();

        sb = new StringBuffer(64);

        sb.append(Tokens.T_CREATE).append(' ');

        if (isUnique()) {
            sb.append(Tokens.T_UNIQUE).append(' ');
        }

        sb.append(Tokens.T_INDEX).append(' ');
        sb.append(getName().statementName);
        sb.append(' ').append(Tokens.T_ON).append(' ');
        sb.append(((Table) table).getName().getSchemaQualifiedStatementName());

        int[] col = getColumns();
        int   len = getVisibleColumns();

        sb.append(((Table) table).getColumnListSQL(col, len));

        return sb.toString();
    }

    public long getChangeTimestamp() {
        return 0;
    }

    // IndexInterface
    public RowIterator emptyIterator() {
        return emptyIterator;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public long getPersistenceId() {
        return persistenceId;
    }

    /**
     * Returns the count of visible columns used
     */
    public int getVisibleColumns() {
        return colIndex.length;
    }

    /**
     * Returns the count of visible columns used
     */
    public int getColumnCount() {
        return colIndex.length;
    }

    /**
     * Is this a UNIQUE index?
     */
    public boolean isUnique() {
        return isUnique;
    }

    /**
     * Does this index belong to a constraint?
     */
    public boolean isConstraint() {
        return isConstraint;
    }

    /**
     * Returns the array containing column indexes for index
     */
    public int[] getColumns() {
        return colIndex;
    }

    /**
     * Returns the array containing column indexes for index
     */
    public Type[] getColumnTypes() {
        return colTypes;
    }

    public boolean[] getColumnDesc() {
        return colDesc;
    }

    /**
     * Returns a value indicating the order of different types of index in
     * the list of indexes for a table. The position of the groups of Indexes
     * in the list in ascending order is as follows:
     *
     * primary key index
     * unique constraint indexes
     * autogenerated foreign key indexes for FK's that reference this table or
     *  tables created before this table
     * user created indexes (CREATE INDEX)
     * autogenerated foreign key indexes for FK's that reference tables created
     *  after this table
     *
     * Among a group of indexes, the order is based on the order of creation
     * of the index.
     *
     * @return ordinal value
     */
    public int getIndexOrderValue() {

        if (isPK) {
            return 0;
        }

        if (isConstraint) {
            return isForward ? 4
                             : isUnique ? 0
                                        : 1;
        } else {
            return 2;
        }
    }

    public boolean isForward() {
        return isForward;
    }

    public void setTable(TableBase table) {
        this.table = table;
    }

    /**
     * Returns the node count.
     */
    public int size(PersistentStore store) {

        int count = 0;

        readLock.lock();

        try {
            RowIterator it = firstRow(null, store);

            while (it.hasNext()) {
                it.getNextRow();

                count++;
            }

            return count;
        } finally {
            readLock.unlock();
        }
    }

    public int sizeEstimate(PersistentStore store) {
        return (int) (1L << depth);
    }

    public boolean isEmpty(PersistentStore store) {

        readLock.lock();

        try {
            return getAccessor(store) == null;
        } finally {
            readLock.unlock();
        }
    }

    public void checkIndex(PersistentStore store) {

        readLock.lock();

        try {
            NodeAVL p = getAccessor(store);
            NodeAVL f = null;

            while (p != null) {
                f = p;

                checkNodes(store, p);

                p = p.getLeft(store);
            }

            p = f;

            while (f != null) {
                checkNodes(store, f);

                f = next(store, f);
            }
        } finally {
            readLock.unlock();
        }
    }

    void checkNodes(PersistentStore store, NodeAVL p) {

        NodeAVL l = p.getLeft(store);
        NodeAVL r = p.getRight(store);

        if (l != null && l.getBalance(store) == -2) {
            System.out.print("broken index - deleted");
        }

        if (r != null && r.getBalance(store) == -2) {
            System.out.print("broken index -deleted");
        }

        if (l != null && !p.equals(l.getParent(store))) {
            System.out.print("broken index - no parent");
        }

        if (r != null && !p.equals(r.getParent(store))) {
            System.out.print("broken index - no parent");
        }
    }

    /**
     * Insert a node into the index
     */
    public void insert(Session session, PersistentStore store, Row row) {

        NodeAVL n;
        NodeAVL x;
        boolean isleft       = true;
        int     compare      = -1;
        boolean compareRowId = !isUnique || hasNulls(row.getData());

        writeLock.lock();
        store.lock();

        try {
            n = getAccessor(store);
            x = n;

            if (n == null) {
                store.setAccessor(this, ((RowAVL) row).getNode(position));

                return;
            }

            while (true) {
                Row currentRow = n.getRow(store);

                compare = compareRowForInsertOrDelete(session, row,
                                                      currentRow,
                                                      compareRowId, 0);

                // after the first match and check, all compares are with row id
                if (compare == 0 && session != null
                        && session.database.txManager.isMVRows()
                        && !isEqualReadable(session, store, n)) {
                    compareRowId = true;
                    compare = compareRowForInsertOrDelete(session, row,
                                                          currentRow,
                                                          compareRowId,
                                                          colIndex.length);
                }

                if (compare == 0) {
                    if (isConstraint) {
                        Constraint c =
                            ((Table) table).getUniqueConstraintForIndex(this);

                        throw c.getException(row.getData());
                    } else {
                        throw Error.error(ErrorCode.X_23505,
                                          name.statementName);
                    }
                }

                isleft = compare < 0;
                x      = n;
                n      = x.child(store, isleft);

                if (n == null) {
                    break;
                }
            }

            x = x.set(store, isleft, ((RowAVL) row).getNode(position));

            balance(store, x, isleft);
        } finally {
            store.unlock();
            writeLock.unlock();
        }
    }

    public void delete(PersistentStore store, Row row) {

        if (!row.isInMemory()) {
            row = (Row) store.get(row, false);
        }

        NodeAVL node = ((RowAVL) row).getNode(position);

        delete(store, node);
    }

    public void delete(PersistentStore store, NodeAVL x) {

        if (x == null) {
            return;
        }

        NodeAVL n;

        writeLock.lock();
        store.lock();

        try {
            if (x.getLeft(store) == null) {
                n = x.getRight(store);
            } else if (x.getRight(store) == null) {
                n = x.getLeft(store);
            } else {
                NodeAVL d = x;

                x = x.getLeft(store);

                while (true) {
                    NodeAVL temp = x.getRight(store);

                    if (temp == null) {
                        break;
                    }

                    x = temp;
                }

                // x will be replaced with n later
                n = x.getLeft(store);

                // swap d and x
                int b = x.getBalance(store);

                x = x.setBalance(store, d.getBalance(store));
                d = d.setBalance(store, b);

                // set x.parent
                NodeAVL xp = x.getParent(store);
                NodeAVL dp = d.getParent(store);

                if (d.isRoot(store)) {
                    store.setAccessor(this, x);
                }

                x = x.setParent(store, dp);

                if (dp != null) {
                    if (dp.isRight(d)) {
                        dp = dp.setRight(store, x);
                    } else {
                        dp = dp.setLeft(store, x);
                    }
                }

                // relink d.parent, x.left, x.right
                if (d.equals(xp)) {
                    d = d.setParent(store, x);

                    if (d.isLeft(x)) {
                        x = x.setLeft(store, d);

                        NodeAVL dr = d.getRight(store);

                        x = x.setRight(store, dr);
                    } else {
                        x = x.setRight(store, d);

                        NodeAVL dl = d.getLeft(store);

                        x = x.setLeft(store, dl);
                    }
                } else {
                    d  = d.setParent(store, xp);
                    xp = xp.setRight(store, d);

                    NodeAVL dl = d.getLeft(store);
                    NodeAVL dr = d.getRight(store);

                    x = x.setLeft(store, dl);
                    x = x.setRight(store, dr);
                }

                x.getRight(store).setParent(store, x);
                x.getLeft(store).setParent(store, x);

                // set d.left, d.right
                d = d.setLeft(store, n);

                if (n != null) {
                    n = n.setParent(store, d);
                }

                d = d.setRight(store, null);
                x = d;
            }

            boolean isleft = x.isFromLeft(store);

            x.replace(store, this, n);

            n = x.getParent(store);

            x.delete();

            while (n != null) {
                x = n;

                int sign = isleft ? 1
                                  : -1;

                switch (x.getBalance(store) * sign) {

                    case -1 :
                        x = x.setBalance(store, 0);
                        break;

                    case 0 :
                        x = x.setBalance(store, sign);

                        return;

                    case 1 :
                        NodeAVL r = x.child(store, !isleft);
                        int     b = r.getBalance(store);

                        if (b * sign >= 0) {
                            x.replace(store, this, r);

                            NodeAVL child = r.child(store, isleft);

                            x = x.set(store, !isleft, child);
                            r = r.set(store, isleft, x);

                            if (b == 0) {
                                x = x.setBalance(store, sign);
                                r = r.setBalance(store, -sign);

                                return;
                            }

                            x = x.setBalance(store, 0);
                            r = r.setBalance(store, 0);
                            x = r;
                        } else {
                            NodeAVL l = r.child(store, isleft);

                            x.replace(store, this, l);

                            b = l.getBalance(store);
                            r = r.set(store, isleft, l.child(store, !isleft));
                            l = l.set(store, !isleft, r);
                            x = x.set(store, !isleft, l.child(store, isleft));
                            l = l.set(store, isleft, x);
                            x = x.setBalance(store, (b == sign) ? -sign
                                                                : 0);
                            r = r.setBalance(store, (b == -sign) ? sign
                                                                 : 0);
                            l = l.setBalance(store, 0);
                            x = l;
                        }
                }

                isleft = x.isFromLeft(store);
                n      = x.getParent(store);
            }
        } finally {
            store.unlock();
            writeLock.unlock();
        }
    }

    public boolean existsParent(Session session, PersistentStore store,
                                Object[] rowdata, int[] rowColMap,
                                boolean isNew) {

        NodeAVL node = findNode(session, store, rowdata, rowColMap,
                                rowColMap.length,
                                TransactionManager.ACTION_REF);

        return node != null;
    }

    /**
     * Return the first node equal to the indexdata object. The rowdata has
     * the same column mapping as this index.
     *
     * @param session session object
     * @param store store object
     * @param rowdata array containing index column data
     * @param match count of columns to match
     * @return iterator
     */
    public RowIterator findFirstRow(Session session, PersistentStore store,
                                    Object[] rowdata, int match) {

        NodeAVL node = findNode(session, store, rowdata, defaultColMap, match,
                                TransactionManager.ACTION_READ);

        return getIterator(session, store, node, false);
    }

    /**
     * Return the first node equal to the rowdata object.
     * The rowdata has the same column mapping as this table.
     *
     * @param session session object
     * @param store store object
     * @param rowdata array containing table row data
     * @return iterator
     */
    public RowIterator findFirstRow(Session session, PersistentStore store,
                                    Object[] rowdata) {

        NodeAVL node = findNode(session, store, rowdata, colIndex,
                                colIndex.length,
                                TransactionManager.ACTION_READ);

        return getIterator(session, store, node, false);
    }

    /**
     * Return the first node equal to the rowdata object.
     * The rowdata has the column mapping privided in rowColMap.
     *
     * @param session session object
     * @param store store object
     * @param rowdata array containing table row data
     * @return iterator
     */
    public RowIterator findFirstRow(Session session, PersistentStore store,
                                    Object[] rowdata, int[] rowColMap) {

        NodeAVL node = findNode(session, store, rowdata, rowColMap,
                                rowColMap.length,
                                TransactionManager.ACTION_READ);

        return getIterator(session, store, node, false);
    }

    /**
     * Finds the first node that is larger or equal to the given one based
     * on the first column of the index only.
     *
     * @param session session object
     * @param store store object
     * @param value value to match
     * @param compare comparison Expression type
     *
     * @return iterator
     */
    public RowIterator findFirstRow(Session session, PersistentStore store,
                                    Object value, int compare) {

        readLock.lock();

        try {
            if (compare == OpTypes.SMALLER
                    || compare == OpTypes.SMALLER_EQUAL) {
                return findFirstRowNotNull(session, store);
            }

            boolean isEqual = compare == OpTypes.EQUAL
                              || compare == OpTypes.IS_NULL;
            NodeAVL x     = getAccessor(store);
            int     iTest = 1;

            if (compare == OpTypes.GREATER) {
                iTest = 0;
            }

            if (value == null && !isEqual) {
                return emptyIterator;
            }

            // this method returns the correct node only with the following conditions
            boolean check = compare == OpTypes.GREATER
                            || compare == OpTypes.EQUAL
                            || compare == OpTypes.GREATER_EQUAL;

            if (!check) {
                Error.runtimeError(ErrorCode.U_S0500, "IndexAVL");
            }

            while (x != null) {
                boolean t = colTypes[0].compare(
                    session, value, x.getData(store)[colIndex[0]]) >= iTest;

                if (t) {
                    NodeAVL r = x.getRight(store);

                    if (r == null) {
                        break;
                    }

                    x = r;
                } else {
                    NodeAVL l = x.getLeft(store);

                    if (l == null) {
                        break;
                    }

                    x = l;
                }
            }

/*
        while (x != null
                && Column.compare(value, x.getData()[colIndex_0], colType_0)
                   >= iTest) {
            x = next(x);
        }
*/
            while (x != null) {
                Object colvalue = x.getData(store)[colIndex[0]];
                int    result = colTypes[0].compare(session, value, colvalue);

                if (result >= iTest) {
                    x = next(store, x);
                } else {
                    if (isEqual) {
                        if (result != 0) {
                            x = null;
                        }
                    } else if (colvalue == null) {
                        x = next(store, x);

                        continue;
                    }

                    break;
                }
            }

// MVCC
            if (session == null || x == null) {
                return getIterator(session, store, x, false);
            }

            while (x != null) {
                Row row = x.getRow(store);

                if (compare == OpTypes.EQUAL
                        && colTypes[0].compare(
                            session, value, row.rowData[colIndex[0]]) != 0) {
                    x = null;

                    break;
                }

                if (session.database.txManager.canRead(
                        session, row, TransactionManager.ACTION_READ)) {
                    break;
                }

                x = next(store, x);
            }

            return getIterator(session, store, x,
                               isUnique && this.colIndex.length == 1
                               && compare == OpTypes.EQUAL);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Finds the first node where the data is not null.
     *
     * @return iterator
     */
    public RowIterator findFirstRowNotNull(Session session,
                                           PersistentStore store) {

        readLock.lock();

        try {
            NodeAVL x = getAccessor(store);

            while (x != null) {
                boolean t = colTypes[0].compare(
                    session, null, x.getData(store)[colIndex[0]]) >= 0;

                if (t) {
                    NodeAVL r = x.getRight(store);

                    if (r == null) {
                        break;
                    }

                    x = r;
                } else {
                    NodeAVL l = x.getLeft(store);

                    if (l == null) {
                        break;
                    }

                    x = l;
                }
            }

            while (x != null) {
                Object colvalue = x.getData(store)[colIndex[0]];

                if (colvalue == null) {
                    x = next(store, x);
                } else {
                    break;
                }
            }

// MVCC
            while (session != null && x != null) {
                Row row = x.getRow(store);

                if (session.database.txManager.canRead(
                        session, row, TransactionManager.ACTION_READ)) {
                    break;
                }

                x = next(store, x);
            }

            return getIterator(session, store, x, false);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Returns the row for the first node of the index
     *
     * @return Iterator for first row
     */
    public RowIterator firstRow(Session session, PersistentStore store) {

        int tempDepth = 0;

        readLock.lock();

        try {
            NodeAVL x = getAccessor(store);
            NodeAVL l = x;

            while (l != null) {
                x = l;
                l = x.getLeft(store);

                tempDepth++;
            }

            while (session != null && x != null) {
                Row row = x.getRow(store);

                if (session.database.txManager.canRead(
                        session, row, TransactionManager.ACTION_READ)) {
                    break;
                }

                x = next(store, x);
            }

            return getIterator(session, store, x, false);
        } finally {
            depth = tempDepth;

            readLock.unlock();
        }
    }

    public RowIterator firstRow(PersistentStore store) {

        int tempDepth = 0;

        readLock.lock();

        try {
            NodeAVL x = getAccessor(store);
            NodeAVL l = x;

            while (l != null) {
                x = l;
                l = x.getLeft(store);

                tempDepth++;
            }

            return getIterator(null, store, x, false);
        } finally {
            depth = tempDepth;

            readLock.unlock();
        }
    }

    /**
     * Returns the row for the last node of the index
     *
     * @return last row
     */
    public Row lastRow(Session session, PersistentStore store) {

        readLock.lock();

        try {
            NodeAVL x = getAccessor(store);
            NodeAVL l = x;

            while (l != null) {
                x = l;
                l = x.getRight(store);
            }

            while (session != null && x != null) {
                Row row = x.getRow(store);

                if (session.database.txManager.canRead(
                        session, row, TransactionManager.ACTION_READ)) {
                    break;
                }

                x = last(store, x);
            }

            return x == null ? null
                             : x.getRow(store);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Returns the node after the given one
     *
     * @param x node
     *
     * @return next node
     */
    NodeAVL next(Session session, PersistentStore store, NodeAVL x) {

        if (x == null) {
            return null;
        }

        readLock.lock();

        try {
            while (true) {
                x = next(store, x);

                if (x == null) {
                    return x;
                }

                if (session == null) {
                    return x;
                }

                Row row = x.getRow(store);

                if (session.database.txManager.canRead(
                        session, row, TransactionManager.ACTION_READ)) {
                    return x;
                }
            }
        } finally {
            readLock.unlock();
        }
    }

    NodeAVL next(PersistentStore store, NodeAVL x) {

        NodeAVL r = x.getRight(store);

        if (r != null) {
            x = r;

            NodeAVL l = x.getLeft(store);

            while (l != null) {
                x = l;
                l = x.getLeft(store);
            }

            return x;
        }

        NodeAVL ch = x;

        x = x.getParent(store);

        while (x != null && ch.equals(x.getRight(store))) {
            ch = x;
            x  = x.getParent(store);
        }

        return x;
    }

    NodeAVL last(PersistentStore store, NodeAVL x) {

        if (x == null) {
            return null;
        }

        NodeAVL left = x.getLeft(store);

        if (left != null) {
            x = left;

            NodeAVL right = x.getRight(store);

            while (right != null) {
                x     = right;
                right = x.getRight(store);
            }

            return x;
        }

        NodeAVL ch = x;

        x = x.getParent(store);

        while (x != null && ch.equals(x.getLeft(store))) {
            ch = x;
            x  = x.getParent(store);
        }

        return x;
    }

    boolean isEqualReadable(Session session, PersistentStore store,
                            NodeAVL node) {

        NodeAVL  c = node;
        Object[] data;
        Object[] nodeData;

        if (session.database.txManager.canRead(
                session, node.getRow(store), TransactionManager.ACTION_DUP)) {
            return true;
        }

        data = node.getData(store);

        while (true) {
            c = last(store, c);

            if (c == null) {
                break;
            }

            nodeData = c.getData(store);

            if (compareRow(session, data, nodeData) == 0) {
                Row row = c.getRow(store);

                if (session.database.txManager.canRead(
                        session, row, TransactionManager.ACTION_DUP)) {
                    return true;
                }

                continue;
            }

            break;
        }

        while (true) {
            c = next(session, store, node);

            if (c == null) {
                break;
            }

            nodeData = c.getData(store);

            if (compareRow(session, data, nodeData) == 0) {
                Row row = c.getRow(store);

                if (session.database.txManager.canRead(
                        session, row, TransactionManager.ACTION_DUP)) {
                    return true;
                }

                continue;
            }

            break;
        }

        return false;
    }

    /**
     * Compares two table rows based on the columns of this index. The rowColMap
     * parameter specifies which columns of the other table are to be compared
     * with the colIndex columns of this index. The rowColMap can cover all
     * or only some columns of this index.
     *
     * @param a row from another table
     * @param rowColMap column indexes in the other table
     * @param b a full row in this table
     *
     * @return comparison result, -1,0,+1
     */
    public int compareRowNonUnique(Session session, Object[] a,
                                   int[] rowColMap, Object[] b) {

        int fieldcount = rowColMap.length;

        for (int j = 0; j < fieldcount; j++) {
            int i = colTypes[j].compare(session, a[rowColMap[j]],
                                        b[colIndex[j]]);

            if (i != 0) {
                return i;
            }
        }

        return 0;
    }

    public int compareRowNonUnique(Session session, Object[] a,
                                   int[] rowColMap, Object[] b,
                                   int fieldCount) {

        for (int j = 0; j < fieldCount; j++) {
            int i = colTypes[j].compare(session, a[rowColMap[j]],
                                        b[colIndex[j]]);

            if (i != 0) {
                return i;
            }
        }

        return 0;
    }

    /**
     * As above but use the index column data
     */
    public int compareRowNonUnique(Session session, Object[] a, Object[] b,
                                   int fieldcount) {

        for (int j = 0; j < fieldcount; j++) {
            int i = colTypes[j].compare(session, a[j], b[colIndex[j]]);

            if (i != 0) {
                return i;
            }
        }

        return 0;
    }

    public int compareRow(Session session, Object[] a, Object[] b) {

        for (int j = 0; j < colIndex.length; j++) {
            int i = colTypes[j].compare(session, a[colIndex[j]],
                                        b[colIndex[j]]);

            if (i != 0) {
                if (isSimpleOrder) {
                    return i;
                }

                boolean nulls = a[colIndex[j]] == null
                                || b[colIndex[j]] == null;

                if (colDesc[j] && !nulls) {
                    i = -i;
                }

                if (nullsLast[j] && nulls) {
                    i = -i;
                }

                return i;
            }
        }

        return 0;
    }

    /**
     * Compare two rows of the table for inserting rows into unique indexes
     * Supports descending columns.
     *
     * @param newRow data
     * @param existingRow data
     * @return comparison result, -1,0,+1
     */
    int compareRowForInsertOrDelete(Session session, Row newRow,
                                    Row existingRow, boolean useRowId,
                                    int start) {

        Object[] a = newRow.rowData;
        Object[] b = existingRow.rowData;

        for (int j = start; j < colIndex.length; j++) {
            int i = colTypes[j].compare(session, a[colIndex[j]],
                                        b[colIndex[j]]);

            if (i != 0) {
                if (isSimpleOrder) {
                    return i;
                }

                boolean nulls = a[colIndex[j]] == null
                                || b[colIndex[j]] == null;

                if (colDesc[j] && !nulls) {
                    i = -i;
                }

                if (nullsLast[j] && nulls) {
                    i = -i;
                }

                return i;
            }
        }

        if (useRowId) {
            return newRow.getPos() - existingRow.getPos();
        }

        return 0;
    }

    boolean hasNulls(Object[] rowData) {

        for (int j = 0; j < colIndex.length; j++) {
            if (rowData[colIndex[j]] == null) {
                return true;
            }
        }

        return false;
    }

    /**
     * Finds a match with a row from a different table
     *
     * @param rowdata array containing data for the index columns
     * @param rowColMap map of the data to columns
     * @param first true if the first matching node is required, false if any node
     * @return matching node or null
     */
    NodeAVL findNode(Session session, PersistentStore store, Object[] rowdata,
                     int[] rowColMap, int fieldCount, int readMode) {

        readLock.lock();

        try {
            NodeAVL x = getAccessor(store);
            NodeAVL n;
            NodeAVL result = null;

            while (x != null) {
                int i = this.compareRowNonUnique(session, rowdata, rowColMap,
                                                 x.getData(store), fieldCount);

                if (i == 0) {
                    result = x;
                    n      = x.getLeft(store);
                } else if (i > 0) {
                    n = x.getRight(store);
                } else {
                    n = x.getLeft(store);
                }

                if (n == null) {
                    break;
                }

                x = n;
            }

            // MVCC 190
            if (session == null) {
                return result;
            }

            while (result != null) {
                Row row = result.getRow(store);

                if (compareRowNonUnique(
                        session, rowdata, rowColMap, row.rowData,
                        fieldCount) != 0) {
                    result = null;

                    break;
                }

                if (session.database.txManager.canRead(session, row,
                                                       readMode)) {
                    break;
                }

                result = next(store, result);
            }

            return result;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Balances part of the tree after an alteration to the index.
     */
    void balance(PersistentStore store, NodeAVL x, boolean isleft) {

        while (true) {
            int sign = isleft ? 1
                              : -1;

            switch (x.getBalance(store) * sign) {

                case 1 :
                    x = x.setBalance(store, 0);

                    return;

                case 0 :
                    x = x.setBalance(store, -sign);
                    break;

                case -1 :
                    NodeAVL l = x.child(store, isleft);

                    if (l.getBalance(store) == -sign) {
                        x.replace(store, this, l);

                        x = x.set(store, isleft, l.child(store, !isleft));
                        l = l.set(store, !isleft, x);
                        x = x.setBalance(store, 0);
                        l = l.setBalance(store, 0);
                    } else {
                        NodeAVL r = l.child(store, !isleft);

                        x.replace(store, this, r);

                        l = l.set(store, !isleft, r.child(store, isleft));
                        r = r.set(store, isleft, l);
                        x = x.set(store, isleft, r.child(store, !isleft));
                        r = r.set(store, !isleft, x);

                        int rb = r.getBalance(store);

                        x = x.setBalance(store, (rb == -sign) ? sign
                                                              : 0);
                        l = l.setBalance(store, (rb == sign) ? -sign
                                                             : 0);
                        r = r.setBalance(store, 0);
                    }

                    return;
            }

            if (x.isRoot(store)) {
                return;
            }

            isleft = x.isFromLeft(store);
            x      = x.getParent(store);
        }
    }

    NodeAVL getAccessor(PersistentStore store) {

        NodeAVL node = (NodeAVL) store.getAccessor(this);

        return node;
    }

    IndexRowIterator getIterator(Session session, PersistentStore store,
                                 NodeAVL x, boolean single) {

        if (x == null) {
            return emptyIterator;
        } else {
            IndexRowIterator it = new IndexRowIterator(session, store, this,
                x, single);

            return it;
        }
    }

    public static final class IndexRowIterator implements RowIterator {

        final Session         session;
        final PersistentStore store;
        final IndexAVL        index;
        NodeAVL               nextnode;
        Row                   lastrow;
        boolean               single;
        IndexRowIterator      last;
        IndexRowIterator      next;
        IndexRowIterator      lastInSession;
        IndexRowIterator      nextInSession;

        /**
         * When session == null, rows from all sessions are returned
         */
        public IndexRowIterator(Session session, PersistentStore store,
                                IndexAVL index, NodeAVL node, boolean single) {

            this.session = session;
            this.store   = store;
            this.index   = index;
            this.single  = single;

            if (index == null) {
                return;
            }

            nextnode = node;
        }

        public boolean hasNext() {
            return nextnode != null;
        }

        public Row getNextRow() {

            if (nextnode == null) {
                release();

                return null;
            }

            lastrow  = nextnode.getRow(store);
            nextnode = single ? null
                              : index.next(session, store, nextnode);

            return lastrow;
        }

        public Object[] getNext() {

            Row row = getNextRow();

            return row == null ? null
                               : row.getData();
        }

        public void remove() {
            store.delete(lastrow);
            store.remove(lastrow.getPos());
        }

        public void release() {}

        public boolean setRowColumns(boolean[] columns) {
            return false;
        }

        public long getRowId() {
            return nextnode.getPos();
        }
    }
}
