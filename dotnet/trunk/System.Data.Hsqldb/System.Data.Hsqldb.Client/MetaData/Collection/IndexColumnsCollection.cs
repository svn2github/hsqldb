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
using System.Data.Hsqldb.Client.MetaData;
using CN = System.Data.Common.DbMetaDataCollectionNames;
using HCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataCollectionNames;
using MDCN = System.Data.Common.DbMetaDataColumnNames;
using HMDCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataColumnNames;
#endregion

namespace System.Data.Hsqldb.Client.MetaData.Collection
{
    #region IndexColumnsCollection

    /// <summary>
    /// <para>
    /// Provides the <see cref="HCN.IndexColumns"/> collection.
    /// </para>
    /// <img src="/ClassDiagrams/System.Data.Hsqldb.Client.MetaData.Collection.IndexColumnsCollection.png"
    ///      alt="IndexColumnsCollection Class Diagram"/>
    /// </summary>
    public class IndexColumnsCollection : Base.MetaDataCollection
    {
        #region Constants

        private const string sql =
@"SELECT *
  FROM (SELECT DISTINCT tc.constraint_catalog
                       ,tc.constraint_schema
                       ,tc.constraint_name
                       ,tc.constraint_type
                       ,ii.table_cat as table_catalog
                       ,ii.table_schem as table_schema
                       ,ii.table_name
                       ,ii.index_name
                       ,ii.column_name
                       ,ii.ordinal_position
                       ,(SELECT data_type
                           FROM information_schema.system_columns sc
                          WHERE (sc.table_name = ii.table_name)
                            AND (sc.column_name = ii.column_name)
                            AND (sc.table_schem = ii.table_schem)
                            AND (((sc.table_cat IS NULL) AND
                                (ii.table_cat IS NULL)) OR
                                (sc.table_cat = ii.table_cat))) AS data_type
          FROM information_schema.system_indexinfo         ii
              ,information_schema.system_table_constraints tc
         WHERE (tc.constraint_type = 'UNIQUE')
           AND (ii.non_unique = FALSE)
           AND (((ii.table_cat IS NULL) AND (tc.table_catalog IS NULL)) OR
               (ii.table_cat = tc.table_catalog))
           AND (ii.table_schem = tc.table_schema)
           AND (ii.table_name = tc.table_name)
           AND (ii.index_name LIKE
               'SYS\_IDX\_' || REPLACE(tc.constraint_name
                                       ,'_'
                                       ,'\_') || '\_%' ESCAPE '\')
        UNION
        SELECT DISTINCT tc.constraint_catalog
                       ,tc.constraint_schema
                       ,tc.constraint_name
                       ,tc.constraint_type
                       ,ii.table_cat as table_catalog
                       ,ii.table_schem as table_schema
                       ,ii.table_name
                       ,ii.index_name
                       ,ii.column_name
                       ,ii.ordinal_position
                       ,(SELECT data_type
                           FROM information_schema.system_columns sc
                          WHERE (sc.table_name = ii.table_name)
                            AND (sc.column_name = ii.column_name)
                            AND (sc.table_schem = ii.table_schem)
                            AND (((sc.table_cat IS NULL) AND
                                (ii.table_cat IS NULL)) OR
                                (sc.table_cat = ii.table_cat))) AS key_type
          FROM information_schema.system_indexinfo         ii
              ,information_schema.system_table_constraints tc
              ,information_schema.system_primarykeys       pk
         WHERE (tc.constraint_type = 'PRIMARY KEY')
           AND (ii.non_unique = FALSE)
           AND (((ii.table_cat IS NULL) AND (tc.table_catalog IS NULL)) OR
               (ii.table_cat = tc.table_catalog))
           AND (tc.table_schema = pk.table_schem)
           AND (tc.table_name = pk.table_name)
           AND (tc.constraint_name = pk.pk_name)
           AND (ii.table_schem = pk.table_schem)
           AND (ii.table_name = pk.table_name)
           AND (ii.column_name = pk.column_name)
           AND (ii.ordinal_position = pk.key_seq)
           AND (ii.index_name LIKE 'SYS\_IDX\_%' ESCAPE '\')
           AND (ii.index_name NOT LIKE 'SYS\_IDX\_SYS\_CT\_%' ESCAPE '\')
           AND (SELECT MAX(ii2.ordinal_position)
                  FROM information_schema.system_indexinfo ii2
                 WHERE (((ii.table_cat IS NULL) AND (ii2.table_cat IS NULL)) OR
                       (ii.table_cat = ii2.table_cat))
                   AND (ii2.table_schem = ii.table_schem)
                   AND (ii2.table_name = ii.table_name)
                   AND (ii2.index_name = ii.index_name)) =
               (SELECT MAX(pk2.key_seq)
                  FROM information_schema.system_primarykeys pk2
                 WHERE (((pk.table_cat IS NULL) AND (pk2.table_cat IS NULL)) OR
                       (pk.table_cat = pk2.table_cat))
                   AND (pk2.table_schem = pk.table_schem)
                   AND (pk2.table_name = pk.table_name)
                   AND (pk2.pk_name = pk.pk_name))
        UNION
        SELECT NULL AS constraint_catalog
              ,NULL AS constraint_schema
              ,NULL AS constraint_name
              ,NULL AS constraint_type
              ,a.table_cat as table_catalog
              ,a.table_schem as table_schema
              ,a.table_name
              ,a.index_name
              ,a.column_name
              ,a.ordinal_position
              ,(SELECT data_type
                  FROM information_schema.system_columns sc
                 WHERE (sc.table_name = a.table_name)
                   AND (sc.column_name = a.column_name)
                   AND (sc.table_schem = a.table_schem)
                   AND (((sc.table_cat IS NULL) AND (a.table_cat IS NULL)) OR
                       (sc.table_cat = a.table_cat))) AS data_type
          FROM information_schema.system_indexinfo a
              ,(SELECT ii.table_cat
                      ,ii.table_schem
                      ,ii.table_name
                      ,ii.index_name
                  FROM information_schema.system_indexinfo ii
                MINUS (SELECT DISTINCT ii.table_cat
                                     ,ii.table_schem
                                     ,ii.table_name
                                     ,ii.index_name
                        FROM information_schema.system_indexinfo         ii
                            ,information_schema.system_table_constraints tc
                       WHERE (tc.constraint_type = 'UNIQUE')
                         AND (ii.non_unique = FALSE)
                         AND (((ii.table_cat IS NULL) AND
                             (tc.table_catalog IS NULL)) OR
                             (ii.table_cat = tc.table_catalog))
                         AND (ii.table_schem = tc.table_schema)
                         AND (ii.table_name = tc.table_name)
                         AND (ii.index_name LIKE
                             'SYS\_IDX\_' || REPLACE(tc.constraint_name
                                                     ,'_'
                                                     ,'\_') || '\_%' ESCAPE '\')
                      UNION
                      SELECT DISTINCT ii.table_cat
                                     ,ii.table_schem
                                     ,ii.table_name
                                     ,ii.index_name
                        FROM information_schema.system_indexinfo         ii
                            ,information_schema.system_table_constraints tc
                            ,information_schema.system_primarykeys       pk
                       WHERE (tc.constraint_type = 'PRIMARY KEY')
                         AND (ii.non_unique = FALSE)
                         AND (((ii.table_cat IS NULL) AND
                             (tc.table_catalog IS NULL)) OR
                             (ii.table_cat = tc.table_catalog))
                         AND (tc.table_schema = pk.table_schem)
                         AND (tc.table_name = pk.table_name)
                         AND (tc.constraint_name = pk.pk_name)
                         AND (ii.table_schem = pk.table_schem)
                         AND (ii.table_name = pk.table_name)
                         AND (ii.column_name = pk.column_name)
                         AND (ii.ordinal_position = pk.key_seq)
                         AND (ii.index_name LIKE 'SYS\_IDX\_%' ESCAPE '\')
                         AND (ii.index_name NOT LIKE 'SYS\_IDX\_SYS\_CT\_%'
                              ESCAPE '\')
                         AND (SELECT MAX(ii2.ordinal_position)
                                FROM information_schema.system_indexinfo ii2
                               WHERE (((ii.table_cat IS NULL) AND
                                     (ii2.table_cat IS NULL)) OR
                                     (ii.table_cat = ii2.table_cat))
                                 AND (ii2.table_schem = ii.table_schem)
                                 AND (ii2.table_name = ii.table_name)
                                 AND (ii2.index_name = ii.index_name)) =
                             (SELECT MAX(pk2.key_seq)
                                FROM information_schema.system_primarykeys pk2
                               WHERE (((pk.table_cat IS NULL) AND
                                     (pk2.table_cat IS NULL)) OR
                                     (pk.table_cat = pk2.table_cat))
                                 AND (pk2.table_schem = pk.table_schem)
                                 AND (pk2.table_name = pk.table_name)
                                 AND (pk2.pk_name = pk.pk_name)))) AS b
         WHERE (((a.table_cat IS NULL) AND (b.table_cat IS NULL)) OR
               (a.table_cat = b.table_cat))
           AND (a.table_schem = b.table_schem)
           AND (a.table_name = b.table_name)
           AND (a.index_name = b.index_name)) c
 WHERE (1 = 1)";

        #endregion

        #region IndexColumnsCollection(HsqlConnection

        /// <summary>
        /// Initializes a new instance of the <see cref="IndexColumnsCollection"/> class.
        /// </summary>
        public IndexColumnsCollection() : base() { }

        #endregion

        #region CreateTable()

        /// <summary>
        /// Creates the <c>IndexColumns</c> metadata collection table.
        /// </summary>
        /// <returns>The <c>DataTable</c>.</returns>
        public override DataTable CreateTable()
        {
            DataTable table = new DataTable(HCN.IndexColumns);
            DataColumnCollection cols = table.Columns;

            AddColumn(cols, null, "constraint_catalog", typeof (string));
            AddColumn(cols, null, "constraint_schema", typeof (string));
            AddColumn(cols, null, "constraint_name", typeof (string));
            AddColumn(cols, null, "constraint_type", typeof(string));
            AddColumn(cols, null, "table_catalog", typeof (string));
            AddColumn(cols, null, "table_schema", typeof (string));
            AddColumn(cols, null, "table_name", typeof (string));
            AddColumn(cols, null, "index_name", typeof(string));
            AddColumn(cols, null, "column_name", typeof (string));
            AddColumn(cols, null, "ordinal_position", typeof (int));
            AddColumn(cols, null, "KeyType", typeof (short));            

            return table;
        }

        #endregion

        #region FillTable(DataTable,string[])

        /// <summary>
        /// Fills the index columns table.
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
            string indexNamePattern = restrictions[3];
            string columnNamePattern = restrictions[4];

            if (WantsIsNull(tableNamePattern)
                || WantsIsNull(indexNamePattern)
                || WantsIsNull(columnNamePattern))
            {
                return;
            }

            StringBuilder query = new StringBuilder(sql);

            query.Append(And("TABLE_CATALOG", "=", catalogName))
                .Append(And("TABLE_SCHEMA", "LIKE", schemaNamePattern))
                .Append(And("TABLE_NAME", "LIKE", tableNamePattern))
                .Append(And("INDEX_NAME", "LIKE", indexNamePattern))
                .Append(And("COLUMN_NAME", "LIKE", columnNamePattern))
                .Append(" ORDER BY 5, 6, 7, 8, 10");
            // table_catalog, table_schema, table_name, index_name, ordinal_position

            using (HsqlDataReader reader = Execute(connection, query.ToString()))
            {
                object[] values = new object[reader.FieldCount];

                while (reader.Read())
                {
                    reader.GetValues(values);

                    string constraintCatalog = (string) values[0];
                    string constraintSchema = (string) values[1];
                    string constraintName = (string) values[2];
                    string constraintType = (string)values[3];
                    string tableCatalog = (string) values[4];
                    string tableSchema = (string) values[5];
                    string tableName = (string) values[6];
                    string indexName = (string)values[7];
                    string columnName = (string) values[8];
                    int ordinalPosition = (int) values[9];
                    short keyType = (short) (int) values[10];                    

                    AddRow(
                        table,
                        constraintCatalog,
                        constraintSchema,
                        constraintName,
                        constraintType,
                        tableCatalog,
                        tableSchema,
                        tableName,
                        indexName,
                        columnName,
                        ordinalPosition,
                        keyType);
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
        /// <param name="constraintType">
        /// The type of the constraint.
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
        /// Type of the key.
        /// </param>
        /// <param name="indexName">
        /// Name of the index.
        /// </param>
        public static void AddRow(
            DataTable table,
            string constraintCatalog,
            string constraintSchema,
            string constraintName,
            string constraintType,
            string tableCatalog,
            string tableSchema,
            string tableName,
            string indexName,
            string columnName,
            int ordinalPosition,
            short keyType)
        {
            DataRow row = table.NewRow();

            row["constraint_catalog"] = constraintCatalog;
            row["constraint_schema"] = constraintSchema;
            row["constraint_name"] = constraintName;
            row["constraint_type"] = constraintType;
            row["table_catalog"] = tableCatalog;
            row["table_schema"] = tableSchema;
            row["table_name"] = tableName;
            row["index_name"] = indexName;
            row["column_name"] = columnName;
            row["ordinal_position"] = ordinalPosition;
            row["KeyType"] = keyType;            

            table.Rows.Add(row);
        }

        #endregion
    }

    #endregion
}