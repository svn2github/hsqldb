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
using System.Data.Hsqldb.Client.MetaData;
using CN = System.Data.Common.DbMetaDataCollectionNames;
using HCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataCollectionNames;
using MDCN = System.Data.Common.DbMetaDataColumnNames;
using HMDCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataColumnNames;
#endregion

namespace System.Data.Hsqldb.Client.MetaData.Collection
{
    /// <summary>
    /// <para>
    /// Provides the <see cref="HCN.ForeignKeys"/> collection.
    /// </para>
    /// <img src="/ClassDiagrams/System.Data.Hsqldb.Client.MetaData.Collection.ForeignKeysCollection.png"
    ///      alt="System.Data.Hsqldb.MetaData.Collections Class Diagram"/>
    /// </summary>
    public class ForeignKeysCollection : Base.MetaDataCollection
    {
        #region Constants

        private const string sql =
@"SELECT tc.constraint_catalog
      ,tc.constraint_schema
      ,tc.constraint_name
      ,tc.constraint_type
      ,tc.table_catalog
      ,tc.table_schema
      ,tc.table_name
      ,SELECT DISTINCT cr.pktable_cat
              FROM information_schema.system_crossreference cr
             WHERE (tc.table_schema = cr.fktable_schem)
               AND (tc.constraint_name = cr.fk_name)
       AS unique_table_catalog
      ,SELECT DISTINCT cr.pktable_schem
              FROM information_schema.system_crossreference cr
             WHERE (tc.table_schema = cr.fktable_schem)
               AND (tc.constraint_name = cr.fk_name)
       AS unique_table_schema
      ,SELECT DISTINCT cr.pktable_name
              FROM information_schema.system_crossreference cr
             WHERE (tc.table_schema = cr.fktable_schem)
               AND (tc.constraint_name = cr.fk_name)
       AS unique_table_name
      ,tc.is_deferrable
      ,tc.initially_deferred
      ,'FULL' AS match_option
      ,CASE SELECT DISTINCT cr.update_rule
              FROM information_schema.system_crossreference cr
             WHERE (tc.table_schema = cr.fktable_schem)
               AND (tc.constraint_name = cr.fk_name)
           WHEN 0 THEN 'CASCADE'
           WHEN 1 THEN 'RESTRICT'
           WHEN 2 THEN 'SET NULL'
           WHEN 3 THEN 'NO ACTION'
           WHEN 4 THEN 'SET DEFAULT'
       END AS update_rule
      ,CASE SELECT DISTINCT cr.delete_rule
              FROM information_schema.system_crossreference cr
             WHERE (tc.table_schema = cr.fktable_schem)
               AND (tc.constraint_name = cr.fk_name)
           WHEN 0 THEN 'CASCADE'
           WHEN 1 THEN 'RESTRICT'
           WHEN 2 THEN 'SET NULL'
           WHEN 3 THEN 'NO ACTION'
           WHEN 4 THEN 'SET DEFAULT'
       END AS delete_rule
  FROM information_schema.system_table_constraints tc
 WHERE (tc.constraint_type = 'FOREIGN KEY') ";
        
        #endregion

        #region ForeignKeysCollection(HsqlConnection)
        /// <summary>
        /// Initializes a new instance of the
        /// <see cref="ForeignKeysCollection"/> class.
        /// </summary>
        public ForeignKeysCollection()
            : base()
        {
        } 
        #endregion

        #region CreateTable()
        /// <summary>
        /// Creates a <c>ForeignKeys</c> metadata collection table.
        /// </summary>
        /// <returns>The <c>DataTable</c>.</returns>
        public override DataTable CreateTable()
        {
            DataTable table = new DataTable(HCN.ForeignKeys);
            DataColumnCollection cols = table.Columns;

            AddColumn(cols, null, "CONSTRAINT_CATALOG", typeof(string));
            AddColumn(cols, null, "CONSTRAINT_SCHEMA", typeof(string));
            AddColumn(cols, null, "CONSTRAINT_NAME", typeof(string));
            AddColumn(cols, null, "CONSTRAINT_TYPE", typeof(string));
            AddColumn(cols, null, "TABLE_CATALOG", typeof(string));
            AddColumn(cols, null, "TABLE_SCHEMA", typeof(string));
            AddColumn(cols, null, "TABLE_NAME", typeof(string));
            AddColumn(cols, null, "UNIQUE_TABLE_CATALOG", typeof(string));
            AddColumn(cols, null, "UNIQUE_TABLE_SCHEMA", typeof(string));
            AddColumn(cols, null, "UNIQUE_TABLE_NAME", typeof(string));
            AddColumn(cols, null, "IS_DEFERRABLE", typeof(string));
            AddColumn(cols, null, "INITIALLY_DEFERRED", typeof(string));
            AddColumn(cols, null, "MATCH_OPTION", typeof(string));
            AddColumn(cols, null, "UPDATE_RULE", typeof(string));
            AddColumn(cols, null, "DELETE_RULE", typeof(string));

            return table;
        } 
        #endregion

        #region FillTable(DataTable,string[])
        /// <summary>
        /// Fills the given ForeignKeys metadata collection table.
        /// </summary>
        /// <param name="connection">The connection.</param>
        /// <param name="table">The table.</param>
        /// <param name="restrictions">The restrictions.</param>
        public override void FillTable(HsqlConnection connection,
            DataTable table, string[] restrictions)
        {
            restrictions = GetRestrictions(restrictions, 4);

            string catalogName = restrictions[0];
            string schemaNamePattern = restrictions[1];
            string tableNamePattern = restrictions[2];
            string constraintNamePattern = restrictions[3];

            if (WantsIsNull(schemaNamePattern)
                || WantsIsNull(tableNamePattern)
                || WantsIsNull(constraintNamePattern))
            {
                return;
            }

            StringBuilder query = new StringBuilder(sql);

            query
                //.Append(And("TABLE_CATALOG", "=", catalogName))
                .Append(And("TABLE_SCHEMA", "LIKE", schemaNamePattern))
                .Append(And("TABLE_NAME", "LIKE", tableNamePattern))
                .Append(And("CONSTRAINT_NAME", "LIKE", constraintNamePattern));

            using (HsqlDataReader reader = Execute(connection, query.ToString()))
            {
                object[] values = new object[reader.FieldCount];

                while (reader.Read())
                {
                    reader.GetValues(values);

                    string constraintCatalog = (string)values[0];
                    string constraintSchema = (string)values[1];
                    string constraintName = (string)values[2];
                    string constraintType = (string)values[3];
                    string tableCatalog = (string)values[4];
                    string tableSchema = (string)values[5];
                    string tableName = (string)values[6];
                    string uniqueTableCatalog = (string)values[7];
                    string uniqueTableSchema = (string)values[8];
                    string uniqueTableName = (string)values[9];
                    string isDeferrable = (string)values[10];
                    string initiallyDeferred = (string)values[11];
                    string matchOption = (string)values[12];
                    string updateRule = (string)values[13];
                    string deleteRule = (string)values[14];

                    AddRow(
                        table,
                        constraintCatalog,
                        constraintSchema,
                        constraintName,
                        constraintType,
                        tableCatalog,
                        tableSchema,
                        tableName,
                        uniqueTableCatalog,
                        uniqueTableSchema,
                        uniqueTableName,
                        isDeferrable,
                        initiallyDeferred,
                        matchOption,
                        updateRule,
                        deleteRule
                        );
                }
            }
        } 
        #endregion

        #region AddRow(...)
        /// <summary>
        /// Adds the row.
        /// </summary>
        /// <param name="table">The table to which to add the row.</param>
        /// <param name="constraintCatalog">The constraint's catalog.</param>
        /// <param name="constraintSchema">The constraint's schema.</param>
        /// <param name="constraintName">
        /// Simple name of the constraint.</param>
        /// <param name="constraintType">Type of the constraint.</param>
        /// <param name="tableCatalog">The table's catalog.</param>
        /// <param name="tableSchema">The table's schema.</param>
        /// <param name="tableName">Name of the table.</param>
        /// <param name="uniqueTableCatalog">The unique table catalog.</param>
        /// <param name="uniqueTableSchema">The unique table schema.</param>
        /// <param name="uniqueTableName">Name of the unique table.</param>
        /// <param name="isDeferrable">
        /// Whether the constraint is deferrable.</param>
        /// <param name="initiallyDeferred">
        /// Whether the constraint is initially deferred.</param>
        /// <param name="matchOption">The match option.</param>
        /// <param name="updateRule">The update rule.</param>
        /// <param name="deleteRule">The delete rule.</param>
        public static void AddRow(
            DataTable table,
            string constraintCatalog, string constraintSchema,
            string constraintName, string constraintType,
            string tableCatalog, string tableSchema, string tableName,
            string uniqueTableCatalog, string uniqueTableSchema,
            string uniqueTableName,
            string isDeferrable, string initiallyDeferred,
            string matchOption, string updateRule, string deleteRule)
        {
            DataRow row = table.NewRow();

            row["CONSTRAINT_CATALOG"] = constraintCatalog;
            row["CONSTRAINT_SCHEMA"] = constraintSchema;
            row["CONSTRAINT_NAME"] = constraintName;
            row["CONSTRAINT_TYPE"] = constraintType;
            row["TABLE_CATALOG"] = tableCatalog;
            row["TABLE_SCHEMA"] = tableSchema;
            row["TABLE_NAME"] = tableName;
            row["UNIQUE_TABLE_CATALOG"] = uniqueTableCatalog;
            row["UNIQUE_TABLE_SCHEMA"] = uniqueTableSchema;
            row["UNIQUE_TABLE_NAME"] = uniqueTableName;
            row["IS_DEFERRABLE"] = isDeferrable;
            row["INITIALLY_DEFERRED"] = initiallyDeferred;
            row["MATCH_OPTION"] = matchOption;
            row["UPDATE_RULE"] = updateRule;
            row["DELETE_RULE"] = deleteRule;

            table.Rows.Add(row);
        } 
        #endregion
    }
}