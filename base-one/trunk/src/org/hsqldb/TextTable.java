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

import java.sql.SQLException;

// tony_lai@users 20020820 - patch 595099 - user define PK name

/**
 *  Class declaration
 * @author sqlbob@users (RMP)
 * @version    1.7.0
 */
class TextTable extends org.hsqldb.Table {

    private String  dataSource = "";
    private String  firstLine  = "";
    private boolean isReversed = false;

    /**
     *  Constructor declaration
     *
     * @param  db
     * @param  isTemp is a temp text table
     * @param  name
     * @exception  SQLException  Description of the Exception
     */
    TextTable(Database db, HsqlName name, int type,
              Session session) throws SQLException {
        super(db, name, type, session);
    }

    private void openCache(String source, boolean isDesc,
                           boolean isRdOnly) throws SQLException {

        if (source == null) {
            source = "";
        }

        // Close old cache:
        if (dataSource.length() > 0) {
            dDatabase.logger.closeTextCache(tableName.name);
        }

        cCache = null;

        int count = getIndexCount();

        for (int i = 0; i < count; i++) {
            getIndex(i).setRoot(null);
        }

        // Open new cache:
        if (source.length() > 0) {
            try {
                cCache = dDatabase.logger.openTextCache(tableName.name,
                        source, isRdOnly, isDesc);
                iIdentityId = cCache.getFreePos();

                // all zero
                int[] roots = new int[iIndexCount];

                setIndexRoots(roots);

                // fredt - moved here from TableFilter
                // build the indexes
                Node readAll = this.getPrimaryIndex().getRoot();

                while (readAll != null) {
                    readAll = readAll.getRight();
                }
            } catch (SQLException e) {
                if (!dataSource.equals(source) || isDesc != isReversed
                        || isRdOnly != isReadOnly) {

                    // Restore old cache.
                    openCache(dataSource, isReversed, isReadOnly);
                } else {
                    if (cCache != null) {
                        cCache.closeFile();
                    }

                    dataSource = "";
                    isReversed = false;
                }

                throw (e);
            }
        }

        dataSource = source;
        isReversed = (isDesc && source.length() > 0);
    }

    boolean equals(String other, Session c) {

        boolean isEqual = super.equals(other, c);

        if (isEqual && isReversed) {
            try {
                openCache(dataSource, isReversed, isReadOnly);
            } catch (SQLException e) {
                return false;
            }
        }

        return (isEqual);
    }

    boolean equals(String other) {

        boolean isEqual = super.equals(other);

        if (isEqual && isReversed) {
            try {
                openCache(dataSource, isReversed, isReadOnly);
            } catch (SQLException e) {
                return false;
            }
        }

        return (isEqual);
    }

    protected void setDataSource(String source, boolean isDesc,
                                 Session s) throws SQLException {

        if (isTemp) {
            Trace.check(s.getId() == ownerSession.getId(),
                        Trace.ACCESS_IS_DENIED);
        } else {
            s.checkAdmin();
        }

        //-- Open if descending, direction changed, or file changed.
        if (isDesc || (isDesc != isReversed) ||!dataSource.equals(source)) {
            openCache(source, isDesc, isReadOnly);
        }

        if (isReversed) {
            isReadOnly = true;
        }
    }

    protected String getDataSource() throws SQLException {
        return dataSource;
    }

    protected boolean isDescDataSource() throws SQLException {
        return isReversed;
    }

    void setDataReadOnly(boolean value) throws SQLException {

        if (isReversed && value == true) {
            throw Trace.error(Trace.DATA_IS_READONLY);
        }

        openCache(dataSource, isReversed, value);

        isReadOnly = value;
    }

    boolean isIndexCached() {
        return false;
    }

    protected Table duplicate() throws SQLException {
        return (new TextTable(dDatabase, tableName, tableType, ownerSession));
    }

    void indexRow(Row r, boolean inserted) throws SQLException {

        if (inserted) {
            super.indexRow(r, true);
        } else {
            Node n       = r.getNextNode(null);
            Node primary = getPrimaryIndex().insertUncached(n);

            if (primary == n) {

                // Not already indexed.
                n = r.getNextNode(n);

                for (int i = 1; n != null; i++) {
                    getIndex(i).insertUncached(n);

                    n = r.getNextNode(n);
                }
            } else {
                r.setPrimaryNode(primary);
            }
        }
    }

    void checkUpdate(int col[], Result deleted,
                     Result inserted) throws SQLException {

        if (dataSource.length() == 0) {
            Trace.check(false, Trace.UNKNOWN_DATA_SOURCE);
        }

        super.checkUpdate(col, deleted, inserted);
    }

    void insert(Object row[], Session c) throws SQLException {

        if (dataSource.length() == 0) {
            Trace.check(false, Trace.UNKNOWN_DATA_SOURCE);
        }

        super.insert(row, c);
    }

    void delete(Object row[], Session c) throws SQLException {

        if (dataSource.length() == 0) {
            Trace.check(false, Trace.UNKNOWN_DATA_SOURCE);
        }

        super.delete(row, c);
    }

// tony_lai@users 20020820 - patch 595099
    void createPrimaryKey(String pkName, int[] columns) throws SQLException {

        if ((columns == null)
                || ((columns.length == 1)
                    && getColumn(columns[0]).columnName.name.equals(
                        DEFAULT_PK))) {
            super.createPrimaryKey(null, columns);
        } else {
            throw (Trace.error(Trace.SECOND_PRIMARY_KEY));
        }
    }

    void drop() throws SQLException {
        openCache("", false, false);
    }
}
