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

import java.io.File;
import java.sql.DatabaseMetaData;
import java.util.Enumeration;
import java.util.Locale;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.ValuePool;
import org.hsqldb.resources.BundleHandler;

/**
 * Provides extended information about HSQLDB tables and their columns/indices
 * @author  boucherb@users.sourceforge.net
 * @version 1.7.2
 * @since HSQLDB 1.7.2
 */
final class DITableInfo implements DITypes {

    private static final int        HALF_MAX_INT = Integer.MAX_VALUE >>> 1;
    private int                     hnd_column_remarks = -1;
    private int                     hnd_table_remarks  = -1;
    private Table                   table;
    private static final DITypeInfo ti = new DITypeInfo();

    public DITableInfo() {
        setLocale(Locale.getDefault());
    }

    void setLocale(Locale l) {

        Locale oldLocale;

        synchronized (BundleHandler.class) {
            oldLocale = BundleHandler.getLocale();

            BundleHandler.setLocale(l);

            hnd_column_remarks =
                BundleHandler.getBundleHandle("column-remarks", null);
            hnd_table_remarks = BundleHandler.getBundleHandle("table-remarks",
                    null);

            BundleHandler.setLocale(oldLocale);
        }
    }

    Integer getBRIPseudo() {
        return ValuePool.getInt(DatabaseMetaData.bestRowNotPseudo);
    }

    Integer getBRIScope() {

        return (table.dDatabase.databaseReadOnly || (table.isTemp() && table.tableType != Table.SYSTEM_TABLE))
               ? ValuePool.getInt(DatabaseMetaData.bestRowSession)
               : ValuePool.getInt(DatabaseMetaData.bestRowTemporary);
    }

    Integer getCacheHash() {

        return (table.cCache == null) ? null
                                      : ValuePool.getInt(
                                          table.cCache.hashCode());
    }

    String getCachePath() {

        return (table.cCache == null) ? null
                                      : new File(table.cCache.sName)
                                          .getAbsolutePath();
    }

    Integer getColBufLen(int i) {

        int    size;
        int    type;
        Column column;

        column = table.getColumn(i);
        type   = column.getType();

        switch (type) {

            case CHAR :
            case CLOB :
            case LONGVARCHAR :
            case VARCHAR : {
                size = column.getSize();

                if (size == 0) {}
                else if (size > HALF_MAX_INT) {
                    size = 0;
                } else {
                    size = 2 * size;
                }

                break;
            }
            case BINARY :
            case BLOB :
            case LONGVARBINARY :
            case VARBINARY : {
                size = column.getSize();

                break;
            }
            case BIGINT :
            case DOUBLE :
            case FLOAT :
            case DATE :
            case REAL :
            case TIME :
            case TIMESTAMP : {
                size = 8;

                break;
            }
            case INTEGER :
            case SMALLINT :
            case TINYINT : {
                size = 4;

                break;
            }
            case BIT : {
                size = 1;

                break;
            }
            default : {
                size = 0;

                break;
            }
        }

        return (size > 0) ? ValuePool.getInt(size)
                          : null;
    }

    Integer getColCharOctLen(int i) {

        int    size;
        int    type;
        Column column;

        column = table.getColumn(i);
        type   = column.getType();

        switch (type) {

            case CHAR :
            case CLOB :
            case LONGVARCHAR :
            case VARCHAR : {
                size = column.getSize();

                if (size == 0) {}
                else if (size > HALF_MAX_INT) {
                    size = 0;
                } else {
                    size = 2 * size;
                }

                break;
            }
            default : {
                size = 0;

                break;
            }
        }

        return (size == 0) ? null
                           : ValuePool.getInt(size);
    }

    Integer getColDataType(int i) {
        return ValuePool.getInt(table.getColumn(i).getType());
    }

    String getColDataTypeName(int i) {
        return Column.getTypeString(table.getColumn(i).getType());
    }

    String getColDefault(int i) {
        return table.getColumn(i).getDefaultString();
    }

    String getColIsNullable(int i) {

        Column column = table.getColumn(i);

        return (column.isNullable() ||!column.isIdentity()) ? "YES"
                                                            : "NO";
    }

    String getColName(int i) {
        return table.getColumn(i).columnName.name;
    }

    Integer getColNullability(int i) {

        Column column = table.getColumn(i);

        return (column.isNullable() &&!column.isIdentity())
               ? ValuePool.getInt(DatabaseMetaData.columnNullable)
               : ValuePool.getInt(DatabaseMetaData.columnNoNulls);
    }

    Integer getColPrecRadix(int i) {

        ti.setTypeCode(table.getColumn(i).getType());

        return ti.getNumPrecRadix();
    }

    String getColRemarks(int i) {

        String key;

        if (table.tableType != Table.SYSTEM_TABLE) {
            return null;
        }

        key = getName() + "_" + getColName(i);

        return BundleHandler.getString(hnd_column_remarks, key);
    }

    Integer getColScale(int i) {

        Column column;
        int    type;

        column = table.getColumn(i);
        type   = column.getType();

        switch (type) {

            case DECIMAL :
            case NUMERIC : {
                return ValuePool.getInt(column.getScale());
            }
            default :
                ti.setTypeCode(type);

                return ti.getDefaultScale();
        }
    }

    String getColScopeCat(int i) {
        return null;
    }

    String getColScopeSchem(int i) {
        return null;
    }

    String getColScopeTable(int i) {
        return null;
    }

    Integer getColSize(int i) {

        Column column;
        int    type;
        int    size;

        column = table.getColumn(i);
        type   = column.getType();

        switch (type) {

            // sized or decimal types
            case BINARY :
            case BLOB :
            case CHAR :
            case CLOB :
            case DECIMAL :
            case LONGVARBINARY :
            case LONGVARCHAR :
            case NUMERIC :
            case VARBINARY :
            case VARCHAR : {
                size = column.getSize();

                break;
            }
            default : {
                size = 0;

                break;
            }
        }

        if (size == 0) {
            ti.setTypeCode(type);

            return ti.getPrecision();
        }

        return ValuePool.getInt(size);
    }

    Integer getColSqlDataType(int i) {

        ti.setTypeCode(table.getColumn(i).getType());

        return ti.getSqlDataType();
    }

    Integer getColSqlDateTimeSub(int i) {

        ti.setTypeCode(table.getColumn(i).getType());

        return ti.getSqlDateTimeSub();
    }

    String getDataSource() {
        return table.getDataSource();
    }

    String getHsqlType() {

        switch (table.tableType) {

            case Table.MEMORY_TABLE :
            case Table.TEMP_TABLE : {
                return "MEMORY";
            }
            case Table.CACHED_TABLE :
            case Table.SYSTEM_TABLE : {
                return table.isCached() ? "CACHED"
                                        : "MEMORY";
            }
            case Table.TEMP_TEXT_TABLE :
            case Table.TEXT_TABLE : {
                return "TEXT";
            }
            case Table.VIEW :
            default : {
                return null;
            }
        }
    }

    Integer getIndexCardinality(int i) {

        // not supported yet: need cardinality attribute
        // in Index objects first, or system statistics table
        // and ANALYZE command to update values in system
        // statistics table
        return null;
    }

    String getIndexColDirection(int i, int columnPosition) {

        // so far, hsqldb only supports completely ascending indexes
        return "A";
    }

    int[] getIndexColumns(int i) {
        return table.getIndex(i).getColumns();
    }

    String getIndexName(int i) {

        HsqlArrayList vIndex = table.vIndex;
        Index         index  = (vIndex == null) ? null
                                                : (Index) vIndex.get(i);

        return (index == null) ? null
                               : index.getName().name;
    }

    Integer getIndexPages(int i) {

        // not supported yet: hsqldb does not even know what a "page" is
        return null;
    }

    Integer getIndexType(int i) {
        return ValuePool.getInt(DatabaseMetaData.tableIndexOther);
    }

    int getIndexVisibleColumns(int i) {
        return table.getIndex(i).getVisibleColumns();
    }

    String getName() {
        return table.getName().name;
    }

    Long getNextIdentity() {

        Index pi;

        if (table.iIdentityColumn < 0) {
            return null;
        }

        pi = table.getPrimaryIndex();

        return (pi != null && pi.getVisibleColumns() > 0)
               ? ValuePool.getLong(table.iIdentityId)
               : null;
    }

    String getRemark() {

        return (table.tableType == Table.SYSTEM_TABLE)
               ? BundleHandler.getString(hnd_table_remarks, getName())
               : null;
    }

    String getStandardType() {

        switch (table.tableType) {

            case Table.VIEW :
                return "VIEW";

            case Table.TEMP_TABLE :
            case Table.TEMP_TEXT_TABLE :
                return "GLOBAL TEMPORARY";

            case Table.SYSTEM_TABLE :
                return "SYSTEM TABLE";

            default :
                return "TABLE";
        }
    }

    Table getTable() {
        return this.table;
    }

    Boolean isDataSourceDescending() {
        return ValuePool.getBoolean(table.isDescDataSource());
    }

    Boolean isIndexNonUnique(int i) {
        return ValuePool.getBoolean(!table.getIndex(i).isUnique());
    }

    boolean isPrimaryIndexPrimaryKey() {

        Index index;
        int[] icols;
        int   vcols;

        index = table.getPrimaryIndex();

        if (index == null) {
            return false;
        }

        vcols = index.getVisibleColumns();

        if (vcols < 1) {
            return false;
        }

        icols = index.getColumns();

        for (int i = 0; i < vcols; i++) {
            if (table.getColumn(icols[i]).isNullable()) {
                return false;
            }
        }

        return true;
    }

    Boolean isReadOnly() {
        return ValuePool.getBoolean(table.isDataReadOnly() || table.isView());
    }

    HsqlArrayList listVisibleIndicies() {

        HsqlArrayList vIndex;
        HsqlArrayList list;
        Index         primaryIndex;
        Index         index;

        primaryIndex = table.getPrimaryIndex();

        if (primaryIndex != null && primaryIndex.getVisibleColumns() > 0) {
            list = table.vIndex;
        } else if (table.vIndex.size() == 0) {
            list = new HsqlArrayList();
        } else {
            vIndex = table.vIndex;
            list   = new HsqlArrayList(vIndex.size() - 1);

            for (int i = 0; i < vIndex.size(); i++) {
                index = (Index) vIndex.get(i);

                if (index != primaryIndex) {
                    list.add(index);
                }
            }
        }

        return list;
    }

    void setTable(Table table) {
        this.table = table;
    }

    /**
     * Retrieves a new <code>Result</code> object whose metadata matches that
     * of the specified table. <p>
     *
     * @param t The table from which to construct the new Result object
     * @return a new <code>Result</code> object whose metadata matches that
     * of the specified table.
     *
     */
    static Result createResultProto(Table t) {

        Column   column;
        HsqlName columnHsqlName;
        String   columnName;
        int      columnCount;
        String   tableName;
        Result   r;

        tableName   = t.getName().name;
        columnCount = t.getColumnCount();
        r           = new Result(columnCount);

        for (int i = 0; i < columnCount; i++) {
            r.sTable[i]        = tableName;
            column             = t.getColumn(i);
            columnHsqlName     = column.columnName;
            columnName         = columnHsqlName.name;
            r.sName[i]         = columnName;
            r.isLabelQuoted[i] = columnHsqlName.isNameQuoted;
            r.colType[i]       = column.getType();
            r.colSize[i]       = column.getSize();
            r.colScale[i]      = column.getScale();
        }

        r.sLabel = r.sName;

        return r;
    }
}
