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
using System.Data;
using System.Text;

// Aliases
using CN = System.Data.Common.DbMetaDataCollectionNames;
using HCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataCollectionNames;
using MDCN = System.Data.Common.DbMetaDataColumnNames;
using HMDCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataColumnNames;
#endregion

namespace System.Data.Hsqldb.Client.MetaData.Collection
{
    /// <summary>
    /// <para>
    /// Provides the <see cref="HCN.ForeignKeyColumns"/> collection.
    /// </para>
    /// <img src="/ClassDiagrams/System.Data.Hsqldb.Client.MetaData.Collection.ForeignKeyColumnsCollection.png"
    ///      alt="ForeignKeyColumnsCollection Class Diagram"/>
    /// </summary>
    public class ForeignKeyColumnsCollection : Base.MetaDataCollection
    {
        #region Constants
        private const string sql =
@"SELECT fktable_cat as constraint_catalog
        ,fktable_schem as constraint_schema
        ,fk_name as constraint_name
        ,fktable_cat as table_catalog
        ,fktable_schem as table_schema
        ,fktable_name as table_name
        ,fkcolumn_name as column_name
        ,pktable_cat as unique_table_catalog
        ,pktable_schem as unique_table_schema
        ,pktable_name as unique_table_name
        ,pkcolumn_name as unique_column_name
        ,key_seq as ordinal_position
   FROM information_schema.system_crossreference
  WHERE 1=1";
        #endregion

        #region ForeignKeyColumnsCollection(HsqlConnection)
        /// <summary>
        /// Initializes a new instance of the <see cref="ForeignKeyColumnsCollection"/> class.
        /// </summary>
        public ForeignKeyColumnsCollection() : base() { }
        #endregion

        #region CreateTable()
        /// <summary>
        /// Creates a <c>ForeignKeyColumns</c> metadata collection table.
        /// </summary>
        /// <returns>The <c>DataTable</c>.</returns>
        public override DataTable CreateTable()
        {
            DataTable table = new DataTable(HCN.ForeignKeyColumns);
            DataColumnCollection cols = table.Columns;

            AddColumn(cols, null, "CONSTRAINT_CATALOG", typeof(string));
            AddColumn(cols, null, "CONSTRAINT_SCHEMA", typeof(string));
            AddColumn(cols, null, "CONSTRAINT_NAME", typeof(string));
            AddColumn(cols, null, "TABLE_CATALOG", typeof(string));
            AddColumn(cols, null, "TABLE_SCHEMA", typeof(string));
            AddColumn(cols, null, "TABLE_NAME", typeof(string));
            AddColumn(cols, null, "COLUMN_NAME", typeof(string));
            AddColumn(cols, null, "UNIQUE_TABLE_CATALOG", typeof(string));
            AddColumn(cols, null, "UNIQUE_TABLE_SCHEMA", typeof(string));
            AddColumn(cols, null, "UNIQUE_TABLE_NAME", typeof(string));
            AddColumn(cols, null, "UNIQUE_COLUMN_NAME", typeof(string));
            AddColumn(cols, null, "ORDINAL_POSITION", typeof(int));

            return table;
        } 
        #endregion

        #region FillTable(DataTable,string[])
        /// <summary>
        /// Fills the table.
        /// </summary>
        /// <param name="connection">The connection.</param>
        /// <param name="table">The table.</param>
        /// <param name="restrictions">The restrictions.</param>
        public override void FillTable(HsqlConnection connection, 
            DataTable table, string[] restrictions)
        {
            restrictions = GetRestrictions(restrictions, 5);

            string catalogName = restrictions[0];
            string schemaNamePattern = restrictions[1];
            string tableNamePattern = restrictions[2];
            string constraintNamePattern = restrictions[3];
            string columnNamePattern = restrictions[4];

            if (WantsIsNull(schemaNamePattern)
                || WantsIsNull(tableNamePattern)
                || WantsIsNull(constraintNamePattern)
                || WantsIsNull(columnNamePattern))
            {
                return;
            }

            StringBuilder query = new StringBuilder(sql);

            query
                .Append(And("TABLE_CATALOG", "=", catalogName))
                .Append(And("TABLE_SCHEMA", "LIKE", schemaNamePattern))
                .Append(And("TABLE_NAME", "LIKE", tableNamePattern))
                .Append(And("CONSTRAINT_NAME", "LIKE", constraintNamePattern))
                .Append(And("COLUMN_NAME", "LIKE", columnNamePattern));

            using (HsqlDataReader reader = Execute(connection, query.ToString()))
            {
                object[] values = new object[reader.FieldCount];

                while (reader.Read())
                {
                    reader.GetValues(values);

                    string constraintCatalog = (string)values[0];
                    string constraintSchema = (string)values[1];
                    string constraintName = (string)values[2];
                    string tableCatalog = (string)values[3];
                    string tableSchema = (string)values[4];
                    string tableName = (string)values[5];
                    string columnName = (string)values[6];
                    string uniqueTableCatalog = (string)values[7];
                    string uniqueTableSchema = (string)values[8];
                    string uniqueTableName = (string)values[9];
                    string uniqueColumnName = (string)values[10];
                    int ordinalPosition = (int)values[11];

                    AddRow(
                        table,
                        constraintCatalog,
                        constraintSchema,
                        constraintName,
                        tableCatalog,
                        tableSchema,
                        tableName,
                        columnName,
                        uniqueTableCatalog,
                        uniqueTableSchema,
                        uniqueTableName,
                        uniqueColumnName,
                        ordinalPosition);
                }
            }
        } 
        #endregion

        #region AddRow(...)
        /// <summary>
        /// Adds the row.
        /// </summary>
        /// <param name="table">The table.</param>
        /// <param name="constraintCatalog">The constraint catalog.</param>
        /// <param name="constraintSchema">The constraint schema.</param>
        /// <param name="constraintName">Name of the constraint.</param>
        /// <param name="tableCatalog">The table catalog.</param>
        /// <param name="tableSchema">The table schema.</param>
        /// <param name="tableName">Name of the table.</param>
        /// <param name="columnName">Name of the column.</param>
        /// <param name="uniqueTableCatalog">The unique table catalog.</param>
        /// <param name="uniqueTableSchema">The unique table schema.</param>
        /// <param name="uniqueTableName">Name of the unique table.</param>
        /// <param name="uniqueColumnName">Name of the unique column.</param>
        /// <param name="ordinalPosition">The ordinal position.</param>
        public static void AddRow(
            DataTable table,
            string constraintCatalog,
            string constraintSchema,
            string constraintName,
            string tableCatalog,
            string tableSchema,
            string tableName,
            string columnName,
            string uniqueTableCatalog,
            string uniqueTableSchema,
            string uniqueTableName,
            string uniqueColumnName,
            int ordinalPosition)
        {
            DataRow row = table.NewRow();

            row["CONSTRAINT_CATALOG"] = constraintCatalog;
            row["CONSTRAINT_SCHEMA"] = constraintSchema;
            row["CONSTRAINT_NAME"] = constraintName;
            row["TABLE_CATALOG"] = tableCatalog;
            row["TABLE_SCHEMA"] = tableSchema;
            row["TABLE_NAME"] = tableName;
            row["COLUMN_NAME"] = columnName;
            row["UNIQUE_TABLE_CATALOG"] = uniqueTableCatalog;
            row["UNIQUE_TABLE_SCHEMA"] = uniqueTableSchema;
            row["UNIQUE_TABLE_NAME"] = uniqueTableName;
            row["UNIQUE_COLUMN_NAME"] = uniqueColumnName;
            row["ORDINAL_POSITION"] = ordinalPosition;

            table.Rows.Add(row);
        } 
        #endregion
    }
}
