/* Copyright (c) 2001-2009, The HSQL Development Group
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


package org.hsqldb.navigator;

import java.io.IOException;

import org.hsqldb.HsqlException;
import org.hsqldb.QueryExpression;
import org.hsqldb.QuerySpecification;
import org.hsqldb.Row;
import org.hsqldb.Session;
import org.hsqldb.SortAndSlice;
import org.hsqldb.TableBase;
import org.hsqldb.index.Index;
import org.hsqldb.lib.ArrayUtil;
import org.hsqldb.persist.PersistentStore;
import org.hsqldb.result.ResultMetaData;
import org.hsqldb.rowio.RowInputInterface;
import org.hsqldb.rowio.RowOutputInterface;
import org.hsqldb.types.Type;

/**
 * Implementation or RowSetNavigator using a table as the data store.
 *
 * @author Fred Toussi (fredt@users dot sourceforge.net)
 * @version 1.9.0
 * @since 1.9.0
 */
public class RowSetNavigatorData extends RowSetNavigator {

    final Session          session;
    public TableBase       table;
    public PersistentStore store;
    RowIterator            iterator;
    Row                    currentRow;
    int                    maxMemoryRowCount;
    boolean                isDiskBased;
    boolean                isClosed;
    int                    visibleColumnCount;

    //
    boolean reindexTable;

    //
    private Index mainIndex;
    private Index fullIndex;
    private Index orderIndex;
    private Index groupIndex;

    public RowSetNavigatorData(Session session,
                               QuerySpecification select)
                               throws HsqlException {

        this.session       = session;
        maxMemoryRowCount  = session.getResultMemoryRowCount();
        visibleColumnCount = select.indexLimitVisible;

        if (select.resultTable.getTableType() == TableBase.SYSTEM_SUBQUERY) {
            table               = select.resultTable;
            table.persistenceId = select.resultTable.getPersistenceId();
            store = session.sessionData.getSubqueryRowStore(table, false);
        } else {
            table = select.resultTable.duplicate();
            store = session.sessionData.getNewResultRowStore(table,
                    !select.isAggregated);
        }

        reindexTable = select.isGrouped;
        mainIndex    = select.mainIndex;
        fullIndex    = select.fullIndex;
        orderIndex   = select.orderIndex;
        groupIndex   = select.groupIndex;
    }

    public RowSetNavigatorData(Session session,
                               QueryExpression queryExpression)
                               throws HsqlException {

        this.session       = session;
        maxMemoryRowCount  = session.getResultMemoryRowCount();
        table              = queryExpression.resultTable.duplicate();
        visibleColumnCount = table.getColumnCount();
        store = session.sessionData.getNewResultRowStore(table, true);
        mainIndex          = queryExpression.mainIndex;
        fullIndex          = queryExpression.fullIndex;
    }

    public RowSetNavigatorData(Session session,
                               TableBase table) throws HsqlException {

        this.session       = session;
        maxMemoryRowCount  = session.getResultMemoryRowCount();
        this.table         = table;
        visibleColumnCount = table.getColumnCount();
        store              = session.sessionData.getRowStore(table);
        mainIndex          = table.getPrimaryIndex();
        fullIndex          = table.getFullIndex();
        this.size          = mainIndex.size(store);
    }

    public void sortFull() throws HsqlException {

        if (reindexTable) {
            table.indexRows(store);
        }

        mainIndex = fullIndex;

        reset();
    }

    public void sortOrder() throws HsqlException {

        if (orderIndex != null) {
            if (reindexTable) {
                table.indexRows(store);
            }

            mainIndex = orderIndex;

            reset();
        }
    }

    public void sortUnion(SortAndSlice sortAndSlice) throws HsqlException {

        if (sortAndSlice.index != null) {
            mainIndex = sortAndSlice.index;

            reset();
        }
    }

    public void sortGroup() throws HsqlException {

        mainIndex = groupIndex;

        reset();
    }

    public void add(Object data) {

        try {
            Row row = (Row) store.getNewCachedObject(session, data);

            table.indexRow(store, row);

            size++;
        } catch (HsqlException e) {}
    }

    private void addAdjusted(Object[] data, int[] columnMap) {

        try {
            if (columnMap == null) {
                data = (Object[]) ArrayUtil.resizeArrayIfDifferent(data,
                        table.getColumnCount());
            } else {
                Object[] newData = new Object[visibleColumnCount];

                ArrayUtil.projectRow(data, columnMap, newData);

                data = newData;
            }

            Row row = (Row) store.getNewCachedObject(session, data);

            table.indexRow(store, row);

            size++;
        } catch (HsqlException e) {}
    }

    public void clear() {

        table.clearAllData(store);

        size = 0;

        reset();
    }

    public Object getCurrent() {
        return currentRow.getData();
    }

    public boolean next() {

        boolean result = super.next();

        currentRow = iterator.getNextRow();

        return result;
    }

    public void remove() throws HsqlException {

        if (currentRow != null) {
            iterator.remove();

            currentRow = null;

            currentPos--;
            size--;
        }
    }

    public void reset() {

        super.reset();

        iterator = mainIndex.firstRow(store);
    }

    public void close() {

        if (isClosed) {
            return;
        }

        iterator.release();
        store.release();

        isClosed = true;
    }

    public boolean isDiskBased() {
        return isDiskBased;
    }

    public void read(RowInputInterface in,
                     ResultMetaData meta) throws HsqlException, IOException {}

    public void write(RowOutputInterface out,
                      ResultMetaData meta) throws HsqlException, IOException {

        reset();
        out.writeLong(id);
        out.writeInt(size);
        out.writeInt(0);    // offset
        out.writeInt(size);

        while (hasNext()) {
            Object[] data = (Object[]) getNext();

            out.writeData(meta.getExtendedColumnCount(), meta.columnTypes,
                          data, null, null);
        }

        reset();
    }

    public void copy(RowSetNavigatorData other,
                     int[] rightColumnIndexes) throws HsqlException {

        while (other.hasNext()) {
            other.getNext();

            Object[] currentData = other.currentRow.getData();

            addAdjusted(currentData, rightColumnIndexes);
        }

        other.close();
    }

    public void union(RowSetNavigatorData other,
                      int[] rightColumnIndexes) throws HsqlException {

        removeDuplicates();
        reset();

        while (other.hasNext()) {
            other.getNext();

            Object[] currentData = other.currentRow.getData();
            RowIterator it = fullIndex.findFirstRowIterator(session, store,
                currentData, rightColumnIndexes);

            if (!it.hasNext()) {
                addAdjusted(currentData, rightColumnIndexes);
            }
        }

        other.close();
    }

    public void unionAll(RowSetNavigatorData other,
                         int[] rightColumnIndexes) throws HsqlException {

        other.reset();

        while (other.hasNext()) {
            other.getNext();

            Object[] currentData = other.currentRow.getData();

            addAdjusted(currentData, rightColumnIndexes);
        }

        other.close();
    }

    public void intersect(RowSetNavigatorData other) throws HsqlException {

        removeDuplicates();
        reset();
        other.sortFull();

        while (hasNext()) {
            getNext();

            Object[] currentData = currentRow.getData();
            RowIterator it = other.fullIndex.findFirstRow(session,
                other.store, currentData);

            if (!it.hasNext()) {
                remove();
            }
        }

        other.close();
    }

    public void intersectAll(RowSetNavigatorData other) throws HsqlException {

        Object[]    compareData = null;
        RowIterator it;
        Row         otherRow  = null;
        Object[]    otherData = null;

        sortFull();
        reset();
        other.sortFull();

        it = other.fullIndex.emptyIterator();

        while (hasNext()) {
            getNext();

            Object[] currentData = currentRow.getData();
            boolean newGroup =
                compareData == null
                || fullIndex.compareRowNonUnique(
                    currentData, compareData, fullIndex.getColumnCount()) != 0;

            if (newGroup) {
                compareData = currentData;
                it = other.fullIndex.findFirstRow(session, other.store,
                                                  currentData);
            }

            otherRow  = it.getNextRow();
            otherData = otherRow == null ? null
                                         : otherRow.getData();

            if (otherData != null
                    && fullIndex.compareRowNonUnique(
                        currentData, otherData,
                        fullIndex.getColumnCount()) == 0) {
                continue;
            }

            remove();
        }

        other.close();
    }

    public void except(RowSetNavigatorData other) throws HsqlException {

        removeDuplicates();
        reset();
        other.sortFull();

        while (hasNext()) {
            getNext();

            Object[] currentData = currentRow.getData();
            RowIterator it = other.fullIndex.findFirstRow(session,
                other.store, currentData);

            if (it.hasNext()) {
                remove();
            }
        }

        other.close();
    }

    public void exceptAll(RowSetNavigatorData other) throws HsqlException {

        Object[]    compareData = null;
        RowIterator it;
        Row         otherRow  = null;
        Object[]    otherData = null;

        sortFull();
        reset();
        other.sortFull();

        it = other.fullIndex.emptyIterator();

        while (hasNext()) {
            getNext();

            Object[] currentData = currentRow.getData();
            boolean newGroup =
                compareData == null
                || fullIndex.compareRowNonUnique(
                    currentData, compareData, fullIndex.getColumnCount()) != 0;

            if (newGroup) {
                compareData = currentData;
                it = other.fullIndex.findFirstRow(session, other.store,
                                                  currentData);
            }

            otherRow  = it.getNextRow();
            otherData = otherRow == null ? null
                                         : otherRow.getData();

            if (otherData != null
                    && fullIndex.compareRowNonUnique(
                        currentData, otherData,
                        fullIndex.getColumnCount()) == 0) {
                remove();
            }
        }

        other.close();
    }

    public boolean hasUniqueNotNullRows() throws HsqlException {

        sortFull();
        reset();

        Object[] lastRowData = null;

        while (hasNext()) {
            getNext();

            Object[] currentData = currentRow.getData();

            if (hasNull(currentData)) {
                continue;
            }

            if (lastRowData != null && equals(lastRowData, currentData)) {
                return false;
            } else {
                lastRowData = currentData;
            }
        }

        return true;
    }

    public void removeDuplicates() throws HsqlException {

        sortFull();
        reset();

        Object[] lastRowData = null;

        while (hasNext()) {
            getNext();

            Object[] currentData = currentRow.getData();

            if (lastRowData != null && equals(lastRowData, currentData)) {
                remove();
            } else {
                lastRowData = currentData;
            }
        }
    }

    public void trim(int limitstart, int limitcount) throws HsqlException {

        if (size == 0) {
            return;
        }

        if (limitstart >= size) {
            clear();

            return;
        }

        if (limitstart != 0) {
            reset();

            for (int i = 0; i < limitstart; i++) {
                next();
                remove();
            }
        }

        if (limitcount == 0 || limitcount >= size) {
            return;
        }

        reset();

        for (int i = 0; i < limitcount; i++) {
            next();
        }

        while (hasNext()) {
            next();
            remove();
        }
    }

    private boolean hasNull(Object[] data) {

        for (int i = 0; i < visibleColumnCount; i++) {
            if (data[i] == null) {
                return true;
            }
        }

        return false;
    }

    private boolean equals(Object[] data1, Object[] data2) {

        Type[] types = table.getColumnTypes();

        for (int i = 0; i < visibleColumnCount; i++) {
            if (types[i].compare(data1[i], data2[i]) != 0) {
                return false;
            }
        }

        return true;
    }

    public Row getGroupRow(Object[] data) throws HsqlException {

        RowIterator it = groupIndex.findFirstRow(session, store, data);

        if (it.hasNext()) {
            Row row = it.getNextRow();

            return row;
        }

        return null;
    }
}
