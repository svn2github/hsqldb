#region licence

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

#endregion

#region Using
using System;
using System.Collections.Generic;
using System.Data;
using System.Data.Common;
using System.Text;

using System.Data.Hsqldb.Common.Enumeration;

using HsqlTypes = org.hsqldb.Types;
using Result = org.hsqldb.Result;
using ResultMetaData = org.hsqldb.Result.ResultMetaData;
using STC = System.Data.Common.SchemaTableColumn;
using STOC = System.Data.Common.SchemaTableOptionalColumn;
using StringConverter = org.hsqldb.lib.StringConverter;
using System.Data.Hsqldb.Common;
#endregion

namespace System.Data.Hsqldb.Client.MetaData
{
    #region HsqlResultSetMetaData

    /// <summary>
    /// <para>
    /// Provides metadata regarding a result set.
    /// </para>
    /// <img src="/ClassDiagrams/System.Data.Hsqldb.Client.MetaData.HsqlResultSetMetaData.png" 
    ///      alt="HsqlResultSetMetaData Class Diagram"/>
    /// </summary>
    /// <seealso cref="DbDataReader.GetSchemaTable()"/>
    /// <seealso cref="HsqlDataReader.GetSchemaTable()"/>
    /// <author name="boucherb@users"/>
    public static class HsqlResultSetMetaData
    {
        #region Inner Classes

        #region TableIdentifier
        /// <summary>
        /// A dictionary key representing an SQL table identifier.
        /// </summary>
        internal class TableIdentifier
        {
            #region Fields
            internal string m_schema;
            internal string m_table;
            private int m_hashcode; 
            #endregion

            #region TableIdentifier(string,string)
            /// <summary>
            /// Constructs a new <c>TableIdentifier</c> instance
            /// with the given schema and table names.
            /// </summary>
            /// <param name="schema">The schema.</param>
            /// <param name="table">The table.</param>
            internal TableIdentifier(
                string schema,
                string table)
            {
                m_schema = schema;
                m_table = table;
            } 
            #endregion

            #region Equals(object)
            /// <summary>
            /// Determines whether the specified object is equal
            /// to this one.
            /// </summary>
            /// <param name="obj">
            /// The object to which to compare.</param>
            /// <returns>
            /// <c>true</c> if the specified object is equal to this one;
            /// otherwise, <c>false</c>.
            /// </returns>
            public override bool Equals(object obj)
            {
                TableIdentifier other = obj as TableIdentifier;

                return (other != null) && Equals(other);
            } 
            #endregion

            #region Equals(TableIdentifier)
            /// <summary>
            /// Determines whether the specified <c>TableIdentifier</c> is equal
            /// to this one.
            /// </summary>
            /// <param name="other">
            /// The <c>TableIdentifier</c> to which to compare.</param>
            /// <returns>
            /// <c>true</c> if the specified <c>TableIdentifier</c> is
            /// equal to this one; otherwise, <c>false</c>.
            /// </returns>
            internal bool Equals(TableIdentifier other)
            {
                return object.Equals(m_schema, other.m_schema)
                    && object.Equals(m_table, other.m_table);
            } 
            #endregion

            #region GetHashCode()
            /// <summary>
            /// Retreives the hash code for this <c>TableIdentifier</c>
            /// </summary>
            /// <remarks>
            /// Suitable for use in hashing algorithms and data
            /// structures like a hash table.
            /// </remarks>
            /// <returns>
            /// The hash code for this <c>TableIdentifier</c>.
            /// </returns>
            public override int GetHashCode()
            {
                int h = m_hashcode;

                if (h == 0)
                {
                    unchecked
                    {
                        if (m_schema != null)
                        {
                            h = 29 * m_schema.GetHashCode();
                        }

                        h = 29 * (h + m_table.GetHashCode());                        
                    }

                    m_hashcode = h; 
                }

                return h;
            } 
            #endregion

            #region ToString()
            /// <summary>
            /// Retrieves a string representation of this object.
            /// </summary>
            /// <returns>The string representation.</returns>
            public override string ToString()
            {
                return string.Format("{schema: {0}, table: {1}}", m_schema, m_table);
            } 
            #endregion
        }
        #endregion

        #region ColumnIdentifier
        /// <summary>
        /// A dictionary key representing an SQL table column identifier.
        /// </summary>
        internal class ColumnIdentifier
        {
            #region Fields
            internal string m_schema;
            internal string m_table;
            internal string m_column;

            private int m_hashcode; 
            #endregion

            #region ColumnIdentifier(string,string,string)
            /// <summary>
            /// Constructs a new <c>ColumnIdentifier</c>
            /// instance with the given schema, table and column names.
            /// </summary>
            /// <param name="schema">The schema.</param>
            /// <param name="table">The table.</param>
            /// <param name="column">The column.</param>
            internal ColumnIdentifier(
                string schema,
                string table,
                string column)
            {
                m_schema = schema;
                m_table = table;
                m_column = column;
            } 
            #endregion

            #region Equals(object)
            /// <summary>
            /// Determines whether the specified object is equal
            /// to this one.
            /// </summary>
            /// <param name="obj">
            /// The object to which to compare.</param>
            /// <returns>
            /// <c>true</c> if the specified object is equal to this one;
            /// otherwise, <c>false</c>.
            /// </returns>
            public override bool Equals(object obj)
            {
                ColumnIdentifier other = obj as ColumnIdentifier;

                return (other != null) && Equals(other);
            } 
            #endregion

            #region Equals(ColumnIdentifier)
            /// <summary>
            /// Determines whether the specified <c>ColumnIdentifier</c> is
            /// equal to this one.
            /// </summary>
            /// <param name="other">
            /// The <c>ColumnIdentifier</c> to which to compare.</param>
            /// <returns>
            /// <c>true</c> if the specified <c>ColumnIdentifier</c> is
            /// equal to this one; otherwise, <c>false</c>.
            /// </returns>
            internal bool Equals(ColumnIdentifier other)
            {
                return object.Equals(m_schema, other.m_schema)
                    && object.Equals(m_table, other.m_table)
                    && object.Equals(m_column, other.m_column);
            } 
            #endregion

            #region GetHashCode()
            /// <summary>
            /// Retreives the hash code for this <c>ColumnIdentifier</c>
            /// </summary>
            /// <remarks>
            /// Suitable for use in hashing algorithms and data
            /// structures like a hash table.
            /// </remarks>
            /// <returns>
            /// The hash code for this <c>ColumnIdentifier</c>.
            /// </returns>
            public override int GetHashCode()
            {
                int h = m_hashcode;

                if (h == 0)
                {
                    unchecked
                    {
                        if (m_schema != null)
                        {
                            h = 29 * m_schema.GetHashCode();
                        }

                        if (m_table != null)
                        {
                            h = 29 * (h + m_table.GetHashCode());
                        }

                        h = 29 * (h + m_column.GetHashCode());
                    }

                    m_hashcode = h;
                }

                return h;
            } 
            #endregion

            #region ToString()
            /// <summary>
            /// Retrieves the <see cref="String"/> representation
            /// of this object.
            /// </summary>
            /// <returns>
            /// The <see cref="String"/> representation.
            /// </returns>
            public override string ToString()
            {
                return string.Format("{schema: {0}, table: {1}, column: {2}}",
                    m_schema, m_table, m_column);
            } 
            #endregion
        }
        #endregion

        #region KeyInfo
        /// <summary>
        /// Represents key information for an SQL table column.
        /// </summary>
        internal class KeyInfo
        {
            internal bool m_isKey;
            internal bool m_isUnique;
        }
        #endregion

        #endregion

        #region Constants

        private const string KeyInfoQuery =
@"-- ADO.NET KeyInfo Query --
SELECT table_schem
      ,table_name
      ,column_name
      ,CASE (SELECT COUNT(*)
               FROM information_schema.system_primarykeys pk
              WHERE (bri.table_schem = pk.table_schem)
                AND (bri.table_name = pk.table_name)
                AND (bri.column_name = pk.column_name))
       WHEN 1 THEN
        (1 = 1)
       ELSE CASE (SELECT COUNT(*)
                    FROM information_schema.system_indexinfo io
                   WHERE (bri.table_schem = io.table_schem)
                     AND (bri.table_name = io.table_name)
                     AND (bri.column_name = io.column_name)
                     AND (io.non_unique = false)
                     AND NOT EXISTS (SELECT 1
                                       FROM information_schema.system_indexinfo io2
                                           ,information_schema.system_columns   sc
                                      WHERE (io2.table_schem = io.table_schem)
                                        AND (io2.table_name = io.table_name)
                                        AND (io2.index_name = io.index_name)
                                        AND (io2.table_schem = sc.table_schem)
                                        AND (io2.table_name = sc.table_name)
                                        AND (io2.column_name = sc.column_name)
                                        AND (sc.nullable <> 0)))
            WHEN 0 THEN
             (1 = 0)
            ELSE
             (1 = 1)
            END
       END AS ""IsKey""
  FROM information_schema.system_bestrowidentifier bri
 WHERE {0}";

        #endregion

        #region GetKeyInfo(HsqlDataReader)
        /// <summary>
        /// Computes the key info dictionary for the column metadata of the
        /// given data reader.
        /// </summary>
        /// <remarks>
        /// Depending upon the column metadata already present in the data
        /// reader, it may be required to perform further access to the
        /// originating data source using the reader's
        /// <c>OriginatingConnection</c>.  This in turn implies that the
        /// <c>OriginatingConnection</c> must be open and must still
        /// represent the originating session on the originating data source;
        /// otherwise, the reported key info may be incorrect or the attempt
        /// access the data source may simply fail.
        /// </remarks>
        /// <param name="reader">
        /// The reader for which to compute the column metadata key info map.
        /// </param>
        /// <returns>
        /// Map {ColumnIdentifier=&gt;KeyInfo}
        /// </returns>
        /// <exception cref="HsqlDataSourceException">
        /// If a data access error occurs.
        /// </exception>        
        internal static Dictionary<ColumnIdentifier, KeyInfo> GetKeyInfo(
            HsqlDataReader reader)
        {
            ResultMetaData metaData = reader.m_result.metaData;
            Dictionary<TableIdentifier, object> tableSet
                = new Dictionary<TableIdentifier, object>();
            object placeholder = new object();
            string[] schemaNames = metaData.schemaNames;
            string[] tableNames = metaData.tableNames;
            string[] columnNames = metaData.colNames;
            int count = columnNames.Length;

            for (int i = 0; i < count; i++)
            {
                string tableName = tableNames[i];
                string columnName = columnNames[i];

                if (string.IsNullOrEmpty(tableName)
                    || string.IsNullOrEmpty(columnName))
                {   // not a table column
                    continue;
                }

                string schemaName = schemaNames[i];
                TableIdentifier tableIdentifier = new TableIdentifier(
                    schemaName, tableName);

                tableSet[tableIdentifier] = placeholder;
            }

            Dictionary<ColumnIdentifier, KeyInfo> columnMap
                = new Dictionary<ColumnIdentifier, KeyInfo>();

            if (tableSet.Count == 0)
            {
                return columnMap;
            }

            StringBuilder sb = new StringBuilder('(');
            count = 0;

            foreach (TableIdentifier tableIdentifier in tableSet.Keys)
            {
                if (count > 0)
                {
                    sb.Append(" OR ");
                }

                count++;

                sb.Append("(bri.table_schem");

                string schemaName = tableIdentifier.m_schema;

                if (string.IsNullOrEmpty(schemaName))
                {
                    sb.Append(" IS NULL ");
                }
                else
                {
                    sb.Append(" = ").Append(StringConverter.toQuotedString(
                        schemaName, '\'', /*escape inner quotes*/ true));
                }

                string tableName = tableIdentifier.m_table;

                sb.Append(" AND bri.table_name = ").Append(
                    StringConverter.toQuotedString(tableName, '\'', 
                    /*escape inner quotes*/ true));

                sb.Append(')');
            }

            sb.Append(')');

            string predicate = sb.ToString();

            using (HsqlCommand command = 
                reader.OriginatingConnection.CreateCommand())
            {
                command.CommandText = string.Format(KeyInfoQuery, predicate);
                command.CommandType = CommandType.Text;

                using (HsqlDataReader keyInfoReader = command.ExecuteReader())
                {
                    while (keyInfoReader.Read())
                    {
                        bool isKey = keyInfoReader.GetBoolean(3);

                        if (!isKey)
                        {
                            continue;
                        }

                        string schema = keyInfoReader.GetString(0);
                        string table = keyInfoReader.GetString(1);
                        string column = keyInfoReader.GetString(2);

                        ColumnIdentifier key = new ColumnIdentifier(schema,
                            table, column);

                        if (!columnMap.ContainsKey(key))
                        {
                            KeyInfo keyInfo = new KeyInfo();

                            keyInfo.m_isKey = true;
                            keyInfo.m_isUnique = false;

                            columnMap.Add(key, keyInfo);
                        }
                    }
                }
            }

            return columnMap;
        }
        #endregion

        #region CreateSchemaTable(HsqlDataReader)
        /// <summary>
        /// Retrieves a <c>DataTable</c> object representing the column
        /// metadata of the given data reader's current result.
        /// </summary>
        /// <param name="reader">
        /// A reader object for which to retrieve the column metadata.
        /// </param>
        /// <returns>
        /// A <c>DataTable</c> object representing the column metadata of the
        /// given data reader's current result.
        /// </returns>
        /// <exception cref="HsqlDataSourceException">
        /// If a data access error occurs.
        /// </exception>
        public static DataTable CreateSchemaTable(HsqlDataReader reader)
        {
            Result result = reader.m_result;
            int columnCount = result.getColumnCount();
            ResultMetaData metaData = result.metaData;
            DataTable table = CreateTable(columnCount);
            bool includeKeyInfo = reader.HasCommandBehavior(CommandBehavior.KeyInfo);
            Dictionary<ColumnIdentifier, KeyInfo> keyInfoMap = (includeKeyInfo)
                ? HsqlResultSetMetaData.GetKeyInfo(reader)
                : null;

            string catalogName = reader.OriginatingConnection.Database;

            for (int i = 0; i < columnCount; i++)
            {
                bool isAutoIncrement = metaData.isIdentity[i];
                string columnName = metaData.colLabels[i];
                int columnOrdinal = i;
                int columnSize = metaData.colSizes[i];
                int numericPrecision = metaData.colSizes[i];
                int numericScale = metaData.colScales[i];
                bool isUnique = false; // isAutoIncrement;
                bool isKey = isAutoIncrement;
                string baseServerName = null;
                string baseCatalogName = catalogName;//metaData.catalogNames[i];
                string baseColumnName = metaData.colNames[i];
                string baseSchemaName = metaData.schemaNames[i];
                string baseTableName = metaData.tableNames[i];
                int providerType = metaData.colTypes[i];
                Type dataType = HsqlConvert.ToDataType(providerType);
                int nullability = metaData.colNullable[i];
                bool allowDBNull = isAutoIncrement || (nullability != 0);
                bool isAliased = (columnName != baseColumnName);
                bool isExpression = string.IsNullOrEmpty(baseTableName);
                bool isIdentity = isAutoIncrement;
                bool isRowVersion = false;
                bool isHidden = false;
                bool isLong = HsqlConvert.ToIsLongProviderType(providerType);
                bool isReadOnly = !metaData.isWritable[i];

                if ((columnSize == 0)
                    && HsqlTypes.isCharacterType(providerType))
                {
                    columnSize = HsqlTypes.getPrecision(providerType);
                }

                if ((numericPrecision == 0) 
                    && HsqlTypes.isNumberType(providerType))
                {
                    numericPrecision = HsqlTypes.getPrecision(providerType);
                }
                
                if (includeKeyInfo)
                {
                    if (!(string.IsNullOrEmpty(baseTableName) 
                        || string.IsNullOrEmpty(baseColumnName)))
                    {
                        ColumnIdentifier key = new ColumnIdentifier(
                            baseSchemaName, baseTableName, baseColumnName);
                        KeyInfo keyInfo;

                        if (keyInfoMap.TryGetValue(key, out keyInfo))
                        {
                            isKey = keyInfo.m_isKey;
                            isUnique = keyInfo.m_isUnique;
                        }
                    }
                }

                HsqlResultSetMetaData.AddRow(table, columnName, columnOrdinal,
                    columnSize, numericPrecision, numericScale, isUnique,
                    isKey, baseServerName, baseCatalogName, baseColumnName,
                    baseSchemaName, baseTableName, dataType, allowDBNull,
                    providerType, isAliased, isExpression, isIdentity,
                    isAutoIncrement, isRowVersion, isHidden, isLong,
                    isReadOnly);
            }

            DataColumnCollection columns = table.Columns;
            int count = columns.Count;

            for (int i = 0; i < count; i++)
            {
                columns[i].ReadOnly = true;
            }

            return table;
        }

        #endregion

        #region CreateTable(DataTable,int)

        /// <summary>
        /// Retrieves a <c>DataTable</c> whose column collection is
        /// the one required to represent the result of invoking
        /// <see cref="DbDataReader.GetSchemaTable()"/>.
        /// </summary>
        /// <param name="capacity">
        /// Initial row capacity; values &lt;=0 are ignored.
        /// </param>
        /// <returns>A new <c>DataTable</c> suitable for holding the
        /// result of invoking <see cref="DbDataReader.GetSchemaTable()"/>.
        /// </returns>
        public static DataTable CreateTable(int capacity)
        {
            DataTable table = new DataTable("SchemaTable");

            if (capacity > 0)
            {
                table.MinimumCapacity = capacity;
            }

            DataColumnCollection collection = table.Columns;

            AddColumn(collection, null, STC.ColumnName, typeof(string));
            AddColumn(collection, 0, STC.ColumnOrdinal, typeof(int));
            AddColumn(collection, null, STC.ColumnSize, typeof(int));
            AddColumn(collection, null, STC.NumericPrecision, typeof(int));
            AddColumn(collection, null, STC.NumericScale, typeof(int));
            AddColumn(collection, null, STC.IsUnique, typeof(bool));
            AddColumn(collection, null, STC.IsKey, typeof(bool));
            AddColumn(collection, null, STOC.BaseServerName, typeof(string));
            AddColumn(collection, null, STOC.BaseCatalogName, typeof(string));
            AddColumn(collection, null, STC.BaseColumnName, typeof(string));
            AddColumn(collection, null, STC.BaseSchemaName, typeof(string));
            AddColumn(collection, null, STC.BaseTableName, typeof(string));
            AddColumn(collection, null, STC.DataType, typeof(object));
            AddColumn(collection, null, STC.AllowDBNull, typeof(bool));
            AddColumn(collection, null, STC.ProviderType, typeof(int));
            AddColumn(collection, null, STC.IsAliased, typeof(bool));
            AddColumn(collection, null, STC.IsExpression, typeof(bool));
            AddColumn(collection, false, "IsIdentity", typeof(bool));
            AddColumn(collection, false, STOC.IsAutoIncrement, typeof(bool));
            AddColumn(collection, false, STOC.IsRowVersion, typeof(bool));
            AddColumn(collection, false, STOC.IsHidden, typeof(bool));
            AddColumn(collection, false, STC.IsLong, typeof(bool));
            AddColumn(collection, null, STOC.IsReadOnly, typeof(bool));
            AddColumn(collection, null, STOC.ProviderSpecificDataType, typeof(object));
            AddColumn(collection, null, "DataTypeName", typeof(string));
            AddColumn(collection, null, "XmlSchemaCollectionDatabase", typeof(string));
            AddColumn(collection, null, "XmlSchemaCollectionOwningSchema", typeof(string));
            AddColumn(collection, null, "XmlSchemaCollectionName", typeof(string));
            AddColumn(collection, null, "UdtAssemblyQualifiedName", typeof(string));
            AddColumn(collection, null, STC.NonVersionedProviderType, typeof(int));
            
            return table;
        }

        #endregion

        #region AddColumn(DataColumnCollection,object,string,Type)

        /// <summary>
        /// Adds a new <c>DataColumn</c> to the given collection.
        /// </summary>
        /// <remarks>
        /// The new <c>DataColumn</c> is contructed from the remaining
        /// arguments.
        /// </remarks>
        /// <param name="columns">
        /// The collection to which to add the new column.
        /// </param>
        /// <param name="defaultValue">
        /// The new column's default value
        /// </param>
        /// <param name="name">
        /// The name of the new column.  If a column with the same name
        /// pre-exists in the collection, this operation does nothing.
        /// </param>
        /// <param name="type">
        /// The System.Type of the new column's values.
        /// </param>
        internal static void AddColumn(
            DataColumnCollection columns,
            object defaultValue,
            string name,
            Type type)
        {
            if (columns.Contains(name))
            {
                return;
            }

            DataColumn column = new DataColumn(name, type);

            if (defaultValue != null)
            {
                column.DefaultValue = defaultValue;
            }

            columns.Add(column);
        }

        #endregion

        #region AddRow(...)

        /// <summary>
        /// Adds a new <c>DataRow</c> to the given <c>DataTable</c>.
        /// </summary>
        /// <remarks>
        /// It is assumed the given table's column collection is
        /// compatibly pre-initialized.
        /// </remarks>
        /// <param name="dataTable">
        /// The <c>DataTable</c> to which to add the row.
        /// </param>
        /// <param name="columnName">
        /// Specifies the name of the column in the schema table.
        /// </param>
        /// <param name="columnOrdinal">
        /// Specifies the ordinal of the column.
        /// </param>
        /// <param name="columnSize">
        /// Specifies the size of the column.
        /// </param>
        /// <param name="numericPrecision">
        /// Specifies the precision of the column data, if the data is numeric.
        /// </param>
        /// <param name="numericScale">
        /// Specifies the scale of the column data, if the data is numeric.
        /// </param>
        /// <param name="isUnique">
        /// Specifies whether a unique constraint applies to this column.
        /// </param>
        /// <param name="isKey">
        /// Specifies whether this column is a key for the table.
        /// </param>
        /// <param name="baseServerName">
        /// Specifies the server name of the column.
        /// </param>
        /// <param name="baseCatalogName">
        /// The name of the catalog associated with the results of the latest
        /// query.
        /// </param>
        /// <param name="baseColumnName">
        /// Specifies the name of the column in the schema table.
        /// </param>
        /// <param name="baseSchemaName">
        /// Specifies the name of the schema in the schema table.
        /// </param>
        /// <param name="baseTableName">
        /// Specifies the name of the table in the schema table.
        /// </param>
        /// <param name="dataType">
        /// Specifies the type of data in the column.
        /// </param>
        /// <param name="allowDBNull">
        /// Specifies whether value DBNull is allowed.
        /// </param>
        /// <param name="providerType">
        /// Specifies the provider-specific data type of the column.
        /// </param>
        /// <param name="isAliased">
        /// Specifies whether this column is aliased.
        /// </param>
        /// <param name="isExpression">
        /// Specifies whether this column is an expression.
        /// </param>
        /// <param name="isIdentity">
        /// Specifies whether this column is the identity for the schema table.
        /// </param>
        /// <param name="isAutoIncrement">
        /// Specifies whether the column values in the column are automatically
        /// incremented.
        /// </param>
        /// <param name="isRowVersion">
        /// Specifies whether this column contains row version information.
        /// </param>
        /// <param name="isHidden">
        /// Specifies whether this column is hidden.
        /// </param>
        /// <param name="isLong">
        /// Specifies whether this column contains long data.
        /// </param>
        /// <param name="isReadOnly">
        /// Specifies whether this column is read-only.
        /// </param>
        public static void AddRow(
            DataTable dataTable,
            string columnName,
            int columnOrdinal,
            int columnSize,
            int numericPrecision,
            int numericScale,
            bool isUnique,
            bool isKey,
            string baseServerName,
            string baseCatalogName,
            string baseColumnName,
            string baseSchemaName,
            string baseTableName,
            Type dataType,
            bool allowDBNull,
            int providerType,
            bool isAliased,
            bool isExpression,
            bool isIdentity,
            bool isAutoIncrement,
            bool isRowVersion,
            bool isHidden,
            bool isLong,
            bool isReadOnly)
        {
            DataRow row = dataTable.NewRow();

            row[STC.ColumnName] = columnName;
            row[STC.ColumnOrdinal] = columnOrdinal;
            row[STC.ColumnSize] = columnSize;
            row[STC.NumericPrecision] = numericPrecision;
            row[STC.NumericScale] = numericScale;
            row[STC.IsUnique] = isUnique;
            row[STC.IsKey] = isKey;
            row[STOC.BaseServerName] = baseServerName;
            row[STOC.BaseCatalogName] =  baseCatalogName;
            row[STC.BaseColumnName] = baseColumnName;
            row[STC.BaseSchemaName] = baseSchemaName;
            row[STC.BaseTableName] = baseTableName;
            row[STC.DataType] = dataType;
            row[STC.AllowDBNull] = allowDBNull;
            row[STC.ProviderType] = providerType;
            row[STC.IsAliased] = isAliased;
            row[STC.IsExpression] = isExpression;
            row["IsIdentity"] = isIdentity;
            row[STOC.IsAutoIncrement] = isAutoIncrement;
            row[STOC.IsRowVersion] = isRowVersion;
            row[STOC.IsHidden] = isHidden;
            row[STC.IsLong] = isLong;
            row[STOC.IsReadOnly] = isReadOnly;
            row[STOC.ProviderSpecificDataType] = HsqlConvert.ToProviderSpecificDataType(providerType);
            row["DataTypeName"] = HsqlConvert.ToSqlDataTypeName(providerType);
            //row["XmlSchemaCollectionDatabase"] = null;
            //row["XmlSchemaCollectionOwningSchema"] = null;
            //row["XmlSchemaCollectionName"] = null;
            //row["UdtAssemblyQualifiedName"] = null;
            row[STC.NonVersionedProviderType] = providerType;

            dataTable.Rows.Add(row);
        }

        #endregion
    }

    #endregion
}
