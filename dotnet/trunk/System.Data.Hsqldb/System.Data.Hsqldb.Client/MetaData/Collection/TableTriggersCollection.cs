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
using System.Data;

// Aliases
using System.Text;
using HCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataCollectionNames;
#endregion

namespace System.Data.Hsqldb.Client.MetaData.Collection
{
    #region TriggersCollection

    /// <summary>
    /// <para>
    /// Provides the <see cref="HCN.TableTriggers"/> collection.
    /// </para>
    /// <img src="../Documentation/ClassDiagrams/System.Data.Hsqldb.Client.MetaData.Collection.TableTriggersCollection.png"
    ///      alt="TableTriggersCollection Class Diagram"/>
    /// </summary>
    public class TableTriggersCollection : Base.MetaDataCollection
    {
        #region Private Fields
        private const string sql =
@"SELECT tt.trigger_cat as trigger_catalog
      ,tt.trigger_schem trigger_schema
      ,tt.trigger_name
      ,tt.table_cat as table_catalog
      ,tt.table_schem as table_schema
      ,tt.base_object_type
      ,tt.table_name
      ,tt.column_name
      ,tt.referencing_names
      ,tt.when_clause
      ,tt.status
      ,tt.description
      ,tt.action_type
      ,tt.trigger_body
 FROM information_schema.system_triggers tt
WHERE base_object_type = 'TABLE'";
        #endregion

        #region TableTriggersCollection(HsqlConnection)
        /// <summary>
        /// Initializes a new instance of the
        /// <see cref="TableTriggersCollection"/> class.
        /// </summary>
        public TableTriggersCollection()
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
            DataTable table = new DataTable(HCN.TableTriggers);
            DataColumnCollection cols = table.Columns;

            AddColumn(cols, null, "TRIGGER_CATALOG", typeof(string));
            AddColumn(cols, null, "TRIGGER_SCHEMA", typeof(string));
            AddColumn(cols, null, "TRIGGER_NAME", typeof(string));
            AddColumn(cols, null, "TABLE_CATALOG", typeof(string));
            AddColumn(cols, null, "TABLE_SCHEMA", typeof(string));
            AddColumn(cols, null, "BASE_OBJECT_TYPE", typeof(string));
            AddColumn(cols, null, "TABLE_NAME", typeof(string));
            AddColumn(cols, null, "COLUMN_NAME", typeof(string));
            AddColumn(cols, null, "REFERENCING_NAMES", typeof(string));
            AddColumn(cols, null, "WHEN_CLAUSE", typeof(string));
            AddColumn(cols, null, "STATUS", typeof(string));
            AddColumn(cols, null, "DESCRIPTION", typeof(string));
            AddColumn(cols, null, "ACTION_TYPE", typeof(string));
            AddColumn(cols, null, "TRIGGER_BODY", typeof(string));

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
            restrictions = GetRestrictions(restrictions, 4);

            string catalogName = restrictions[0];
            string schemaNamePattern = restrictions[1];
            string tableNamePattern = restrictions[2];
            string triggerNamePattern = restrictions[3];

            if (WantsIsNull(schemaNamePattern)
                || WantsIsNull(tableNamePattern)
                || WantsIsNull(triggerNamePattern))
            {
                return;
            }

            StringBuilder query = new StringBuilder(sql);

            query
                //.Append(And("TABLE_CATALOG", "=", catalogName))
                .Append(And("TABLE_SCHEMA", "LIKE", schemaNamePattern))
                .Append(And("TABLE_NAME", "LIKE", tableNamePattern))
                .Append(And("TRIGGER_NAME", "LIKE", triggerNamePattern));

            using (HsqlDataReader reader = Execute(connection, query.ToString()))
            {
                object[] values = new object[reader.FieldCount];

                while (reader.Read())
                {
                    reader.GetValues(values);

                    string triggerCatalog = (string)values[0];
                    string triggerSchema = (string)values[1];
                    string triggerName = (string)values[2];
                    string tableCatalog = (string)values[3];
                    string tableSchema = (string)values[4];
                    string baseObjectType = (string)values[5];
                    string tableName = (string)values[6];
                    string columnName = (string)values[7];
                    string referencingNames = (string)values[8];
                    string whenClause = (string)values[9];
                    string status = (string)values[10];
                    string description = (string)values[11];
                    string actionType = (string)values[12];
                    string triggerBody = (string)values[13];

                    AddRow(
                        table,
                        triggerCatalog,
                        triggerSchema,
                        triggerName,
                        tableCatalog,
                        tableSchema,
                        baseObjectType,
                        tableName,
                        columnName,
                        referencingNames,
                        whenClause,
                        status,
                        description,
                        actionType,
                        triggerBody
                        );
                }
            }
        } 
        #endregion

        #region AddRow(...)
        /// <summary>
        /// Adds the row.
        /// </summary>
        /// <param name="table">The table.</param>
        /// <param name="triggerCatalog">The trigger catalog.</param>
        /// <param name="triggerSchema">The trigger schema.</param>
        /// <param name="triggerName">Name of the trigger.</param>
        /// <param name="tableCatalog">The table catalog.</param>
        /// <param name="tableSchema">The table schema.</param>
        /// <param name="baseObjectType">Type of the base object.</param>
        /// <param name="tableName">Name of the table.</param>
        /// <param name="columnName">Name of the column.</param>
        /// <param name="referencingNames">The referencing names.</param>
        /// <param name="whenClause">The when clause.</param>
        /// <param name="status">The status.</param>
        /// <param name="description">The description.</param>
        /// <param name="actionType">Type of the action.</param>
        /// <param name="triggerBody">The trigger body.</param>
        public static void AddRow(
            DataTable table,
            string triggerCatalog,
            string triggerSchema,
            string triggerName,
            string tableCatalog,
            string tableSchema,
            string baseObjectType,
            string tableName,
            string columnName,
            string referencingNames,
            string whenClause,
            string status,
            string description,
            string actionType,
            string triggerBody)
        {
            DataRow row = table.NewRow();

            row["TRIGGER_CATALOG"] = triggerCatalog;
            row["TRIGGER_SCHEMA"] = triggerSchema;
            row["TRIGGER_NAME"] = triggerName;
            row["TABLE_CATALOG"] = tableCatalog;
            row["TABLE_SCHEMA"] = tableSchema;
            row["BASE_OBJECT_TYPE"] = baseObjectType;
            row["TABLE_NAME"] = tableName;
            row["COLUMN_NAME"] = columnName;
            row["REFERENCING_NAMES"] = referencingNames;
            row["WHEN_CLAUSE"] = whenClause;
            row["STATUS"] = status;
            row["DESCRIPTION"] = description;
            row["ACTION_TYPE"] = actionType;
            row["TRIGGER_BODY"] = triggerBody;

            table.Rows.Add(row);
        } 
        #endregion
    }
    
    #endregion
}
