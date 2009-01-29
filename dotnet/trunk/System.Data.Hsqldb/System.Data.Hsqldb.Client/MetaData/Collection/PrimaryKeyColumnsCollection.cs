#region licence

/* Copyright (c) 2001-2008, The HSQL Development Group
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
using System.Data;
using System.Text;
using HCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataCollectionNames;
#endregion

namespace System.Data.Hsqldb.Client.MetaData.Collection
{
    /// <summary>
    /// <para>
    /// Provides the <see cref="HCN.PrimaryKeyColumns"/> collection.
    /// </para>
    /// </summary>
    public class PrimaryKeyColumnsCollection : Base.MetaDataCollection
    {
        #region Constants
        
        private const string sql =
@"SELECT tc.constraint_catalog
      ,tc.constraint_schema
      ,tc.constraint_name
      ,pkc.table_cat as table_catalog
      ,pkc.table_schem as table_schema
      ,pkc.table_name
      ,pkc.column_name
      ,pkc.key_seq  as ordinal_position
  FROM information_schema.system_table_constraints tc
  JOIN information_schema.system_primarykeys pkc
    ON (    (    (tc.table_catalog = pkc.table_cat)
              OR (    (tc.table_catalog IS NULL)
                  AND (pkc.table_cat IS NULL)
                 )
            )
        AND (tc.table_schema = pkc.table_schem)
        AND (tc.table_name = pkc.table_name)
        AND (tc.constraint_name = pkc.pk_name)
        )
  WHERE tc.constraint_type = 'PRIMARY KEY'";

        #endregion

        #region PrimaryKeyColumnsCollection(HsqlConnection)
        /// <summary>
        /// Initializes a new instance of the
        /// <see cref="PrimaryKeyColumnsCollection"/> class.
        /// </summary>
        public PrimaryKeyColumnsCollection()
            : base()
        {
        } 
        #endregion

        #region CreateTable()
        /// <summary>
        /// Creates the table.
        /// </summary>
        /// <returns>The table.</returns>
        public override DataTable CreateTable()
        {
            DataTable table = new DataTable(HCN.PrimaryKeyColumns);
            DataColumnCollection cols = table.Columns;

            AddColumn(cols, null, "CONSTRAINT_CATALOG", typeof(string));
            AddColumn(cols, null, "CONSTRAINT_SCHEMA", typeof(string));
            AddColumn(cols, null, "CONSTRAINT_NAME", typeof(string));
            AddColumn(cols, null, "TABLE_CATALOG", typeof(string));
            AddColumn(cols, null, "TABLE_SCHEMA", typeof(string));
            AddColumn(cols, null, "TABLE_NAME", typeof(string));
            AddColumn(cols, null, "COLUMN_NAME", typeof(string));
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
                //.Append(And("TABLE_CATALOG", "=", catalogName))
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
                    int ordinalPosition = (int)values[7];

                    AddRow(
                        table,
                        constraintCatalog,
                        constraintSchema,
                        constraintName,
                        tableCatalog,
                        tableSchema,
                        tableName,
                        columnName,
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
            row["ORDINAL_POSITION"] = ordinalPosition;

            table.Rows.Add(row);
        } 
        #endregion
    }
}
