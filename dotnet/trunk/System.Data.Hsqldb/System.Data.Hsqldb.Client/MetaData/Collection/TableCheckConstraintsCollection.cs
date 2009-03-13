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
using System.Text;
using HCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataCollectionNames;
#endregion

namespace System.Data.Hsqldb.Client.MetaData.Collection
{
    #region TableCheckConstraintsCollection
    
    /// <summary>
    /// <para>
    /// Provides the <see cref="HCN.TableCheckConstraints"/> collection.
    /// </para>
    /// <img src="../Documentation/ClassDiagrams/System.Data.Hsqldb.Client.MetaData.Collection.TableCheckConstraintsCollection.png"
    ///      alt="TableCheckConstraintsCollection Class Diagram"/>
    /// </summary>
    public class TableCheckConstraintsCollection
        : Base.MetaDataCollection
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
      ,tc.is_deferrable
      ,tc.initially_deferred
      ,cc.check_clause
 FROM information_schema.system_table_constraints tc
 JOIN information_schema.system_check_constraints cc
   ON (    (    (tc.constraint_catalog = cc.constraint_catalog)
             OR (    (tc.constraint_catalog is null)
                 AND (cc.constraint_catalog is null)
                )
           )
       AND (tc.constraint_schema = cc.constraint_schema)
       AND (tc.constraint_name = cc.constraint_name)
      )
WHERE tc.constraint_type = 'CHECK'";

        #endregion

        #region TableCheckConstraintsCollection(HsqlConnection)
        /// <summary>
        /// Constructs a new <c>TableCheckConstraintsCollection</c> instance
        /// with the given connection.
        /// </summary>
        public TableCheckConstraintsCollection() : base() { }
        #endregion

        #region CreateTable()
        /// <summary>
        /// Creates the table.
        /// </summary>
        /// <returns>The table.</returns>
        public override DataTable CreateTable()
        {
            DataTable table = new DataTable(HCN.TableCheckConstraints);
            DataColumnCollection cols = table.Columns;

            AddColumn(cols, null, "CONSTRAINT_CATALOG", typeof(string));
            AddColumn(cols, null, "CONSTRAINT_SCHEMA", typeof(string));
            AddColumn(cols, null, "CONSTRAINT_NAME", typeof(string));
            AddColumn(cols, null, "CONSTRAINT_TYPE", typeof(string));
            AddColumn(cols, null, "TABLE_CATALOG", typeof(string));
            AddColumn(cols, null, "TABLE_SCHEMA", typeof(string));
            AddColumn(cols, null, "TABLE_NAME", typeof(string));
            AddColumn(cols, null, "IS_DEFERRABLE", typeof(string));
            AddColumn(cols, null, "INITIALLY_DEFERRED", typeof(string));
            AddColumn(cols, null, "CHECK_CLAUSE", typeof(string));

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
                    string isDeferrable = (string)values[7];
                    string initiallyDeferred = (string)values[8];
                    string checkClause = (string)values[9];

                    AddRow(
                        table,
                        constraintCatalog,
                        constraintSchema,
                        constraintName,
                        constraintType,
                        tableCatalog,
                        tableSchema,
                        tableName,
                        isDeferrable,
                        initiallyDeferred,
                        checkClause
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
        /// <param name="constraintName">Simple name of the constraint.</param>
        /// <param name="constraintType">Type of the constraint.</param>
        /// <param name="tableCatalog">The table's catalog.</param>
        /// <param name="tableSchema">The table's schema.</param>
        /// <param name="tableName">Name of the table.</param>
        /// <param name="isDeferrable">Whether the constraint is deferrable.</param>
        /// <param name="initiallyDeferred">Whether the constraint is initially deferred.</param>
        /// <param name="checkClause">The check clause.</param>
        public static void AddRow(
            DataTable table,
            string constraintCatalog,
            string constraintSchema,
            string constraintName,
            string constraintType,
            string tableCatalog,
            string tableSchema,
            string tableName,
            string isDeferrable,
            string initiallyDeferred,
            string checkClause)
        {
            DataRow row = table.NewRow();

            row["CONSTRAINT_CATALOG"] = constraintCatalog;
            row["CONSTRAINT_SCHEMA"] = constraintSchema;
            row["CONSTRAINT_NAME"] = constraintName;
            row["CONSTRAINT_TYPE"] = constraintType;
            row["TABLE_CATALOG"] = tableCatalog;
            row["TABLE_SCHEMA"] = tableSchema;
            row["TABLE_NAME"] = tableName;
            row["IS_DEFERRABLE"] = isDeferrable;
            row["INITIALLY_DEFERRED"] = initiallyDeferred;
            row["CHECK_CLAUSE"] = checkClause;

            table.Rows.Add(row);
        }
        #endregion
    } 
    
    #endregion
}
