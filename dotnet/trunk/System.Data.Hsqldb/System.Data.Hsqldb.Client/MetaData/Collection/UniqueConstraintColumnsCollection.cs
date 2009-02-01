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

// Aliases
using System.Data.Hsqldb.Client.MetaData;
using StringBuilder = System.Text.StringBuilder;
using CN = System.Data.Common.DbMetaDataCollectionNames;
using HCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataCollectionNames;
using MDCN = System.Data.Common.DbMetaDataColumnNames;
using HMDCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataColumnNames;
#endregion

namespace System.Data.Hsqldb.Client.MetaData.Collection
{
    /// <summary>
    /// <para>
    /// Provides the <see cref="HCN.UniqueConstraintColumns"/> collection.
    /// </para>
    /// <img src="/ClassDiagrams/System.Data.Hsqldb.Client.MetaData.Collection.UniqueConstraintColumnsCollection.png"
    ///      alt="IndexColumnsCollection Class Diagram"/>
    /// </summary>
    public class UniqueConstraintColumnsCollection : Base.MetaDataCollection
    {
        #region Constants

        private const string sql =
@"SELECT tc.constraint_catalog
        ,tc.constraint_schema
        ,tc.constraint_name
        ,io.table_cat AS table_catalog
        ,io.table_schem AS table_schema
        ,io.table_name AS table_name
        ,io.column_name as column_name
        ,io.ordinal_position as ordinal_position
        ,sc.data_type as key_type
        ,io.index_name
    FROM information_schema.system_table_constraints tc,
         information_schema.system_indexinfo io
    JOIN information_schema.system_columns sc
     ON (    (sc.table_name = io.table_name)
         AND (sc.column_name = io.column_name)
         AND (sc.table_schem = io.table_schem)
         AND (    (    (sc.table_cat IS NULL)
                   AND (io.table_cat IS NULL)
                  )
               OR (sc.table_cat = io.table_cat)
             )
        )       
   WHERE (tc.constraint_type = 'UNIQUE')
     AND (    (    (io.table_cat IS NULL)
               AND (tc.table_catalog IS NULL)
               )
          OR (io.table_cat = tc.table_catalog)
         )
     AND (io.table_schem = tc.table_schema)
     AND (io.table_name = tc.table_name)
     AND (io.index_name LIKE 'SYS\_IDX\_' 
          || REPLACE(tc.constraint_name, '_', '\_') 
          || '\_%' ESCAPE '\')";

        #endregion

        #region UniqueConstraintColumnsCollection(HsqlConnection)

        /// <summary>
        /// Initializes a new instance of the
        /// <see cref="UniqueConstraintColumnsCollection"/> class.
        /// </summary>
        public UniqueConstraintColumnsCollection() 
            : base()
        {
        }

        #endregion

        #region CreateTable()

        /// <summary>
        /// Creates the <c>UniqueConstraintColumns</c> metadata collection table.
        /// </summary>
        /// <returns>The <c>DataTable</c>.</returns>
        public override DataTable CreateTable()
        {
            DataTable table = new DataTable(HCN.UniqueConstraintColumns);
            DataColumnCollection cols = table.Columns;

            AddColumn(cols, null, "constraint_catalog", typeof (string));
            AddColumn(cols, null, "constraint_schema", typeof (string));
            AddColumn(cols, null, "constraint_name", typeof (string));
            AddColumn(cols, null, "table_catalog", typeof (string));
            AddColumn(cols, null, "table_schema", typeof (string));
            AddColumn(cols, null, "table_name", typeof (string));
            AddColumn(cols, null, "column_name", typeof (string));
            AddColumn(cols, null, "ordinal_position", typeof (int));
            AddColumn(cols, null, "KeyType", typeof (short));
            AddColumn(cols, null, "index_name", typeof (string));

            return table;
        }

        #endregion

        #region FillTable(DataTable,string[])

        /// <summary>
        /// Fills the <c>UniqueConstraintColumns</c> metadata collection table.
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

            if (WantsIsNull(tableNamePattern)
                || WantsIsNull(constraintNamePattern)
                || WantsIsNull(columnNamePattern))
            {
                return;
            }

            StringBuilder query = new StringBuilder(sql);

            query.Append(And("TABLE_CATALOG", "=", catalogName))
                .Append(And("TABLE_SCHEMA", "LIKE", schemaNamePattern))
                .Append(And("TABLE_NAME", "LIKE", tableNamePattern))
                .Append(And("CONSTRAINT_NAME", "LIKE", constraintNamePattern))
                .Append(And("COLUMN_NAME", "LIKE", columnNamePattern))
                .Append(" ORDER BY 1, 2, 3, 8");
            // constraint_catalog, constraint_schema, constraint_name, ordinal_position

            using (HsqlDataReader reader = Execute(connection, query.ToString()))
            {
                object[] values = new object[reader.FieldCount];

                while (reader.Read())
                {
                    reader.GetValues(values);

                    string constraintCatalog = (string) values[0];
                    string constraintSchema = (string) values[1];
                    string constraintName = (string) values[2];
                    string tableCatalog = (string) values[3];
                    string tableSchema = (string) values[4];
                    string tableName = (string) values[5];
                    string columnName = (string) values[6];
                    int ordinalPosition = (int) values[7];
                    short keyType = (short) (int) values[8];
                    string indexName = (string) values[9];

                    AddRow(
                        table,
                        constraintCatalog,
                        constraintSchema,
                        constraintName,
                        tableCatalog,
                        tableSchema,
                        tableName,
                        columnName,
                        ordinalPosition,
                        keyType,
                        indexName);
                }
            }
        }

        #endregion

        #region AddRow(...)

        /// <summary>
        /// Adds the index columns row.
        /// </summary>
        /// <param name="table">
        /// The table.
        /// </param>
        /// <param name="constraintCatalog">
        /// The constraint catalog.
        /// </param>
        /// <param name="constraintSchema">
        /// The constraint schema.
        /// </param>
        /// <param name="constraintName">
        /// Name of the constraint.
        /// </param>
        /// <param name="tableCatalog">
        /// The table catalog.
        /// </param>
        /// <param name="tableSchema">
        /// The table schema.
        /// </param>
        /// <param name="tableName">
        /// Name of the table.
        /// </param>
        /// <param name="columnName">
        /// Name of the column.
        /// </param>
        /// <param name="ordinalPosition">
        /// The ordinal position.
        /// </param>
        /// <param name="keyType">
        /// Provider Data Type of column.
        /// </param>
        /// <param name="indexName">
        /// Name of the index.
        /// </param>
        public static void AddRow(
            DataTable table,
            string constraintCatalog,
            string constraintSchema,
            string constraintName,
            string tableCatalog,
            string tableSchema,
            string tableName,
            string columnName,
            int ordinalPosition,
            short keyType,
            string indexName)
        {
            DataRow row = table.NewRow();

            row["constraint_catalog"] = constraintCatalog;
            row["constraint_schema"] = constraintSchema;
            row["constraint_name"] = constraintName;
            row["table_catalog"] = tableCatalog;
            row["table_schema"] = tableSchema;
            row["table_name"] = tableName;
            row["column_name"] = columnName;
            row["ordinal_position"] = ordinalPosition;
            row["KeyType"] = keyType;
            row["index_name"] = indexName;

            table.Rows.Add(row);
        }

        #endregion
    }
}
