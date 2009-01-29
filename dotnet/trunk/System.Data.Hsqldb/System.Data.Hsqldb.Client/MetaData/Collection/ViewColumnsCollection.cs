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
    #region ViewColumnsCollection

    /// <summary>
    /// <para>
    /// Provides the <see cref="HCN.ViewColumns"/> collection.
    /// </para>
    /// <img src="/ClassDiagrams/System.Data.Hsqldb.Client.MetaData.Collection.ViewColumnsCollection.png"
    ///      alt="ViewColumnsCollection Class Diagram"/>
    /// </summary>
    public class ViewColumnsCollection : Base.MetaDataCollection
    {
        #region ViewColumnsCollection(HsqlConnection)

        /// <summary>
        /// Initializes a new instance of the <see cref="ViewColumnsCollection"/> class.
        /// </summary>
        public ViewColumnsCollection() : base()
        {
        }

        #endregion

        #region CreateTable()

        /// <summary>
        /// Creates the view columns table.
        /// </summary>
        /// <returns>The table.</returns>
        public override DataTable CreateTable()
        {
            DataTable table = new DataTable(HCN.ViewColumns);
            DataColumnCollection cols = table.Columns;

            AddColumn(cols, null, "VIEW_CATALOG", typeof (string));
            AddColumn(cols, null, "VIEW_SCHEMA", typeof (string));
            AddColumn(cols, null, "VIEW_NAME", typeof (string));
            AddColumn(cols, null, "TABLE_CATALOG", typeof (string));
            AddColumn(cols, null, "TABLE_SCHEMA", typeof (string));
            AddColumn(cols, null, "TABLE_NAME", typeof (string));
            AddColumn(cols, null, "COLUMN_NAME", typeof (string));

            return table;
        }

        #endregion

        #region FillTable(DataTable,string[])

        /// <summary>
        /// Fills the view columns table.
        /// </summary>
        /// <param name="connection">The connection.</param>
        /// <param name="table">The table.</param>
        /// <param name="restrictions">The restrictions.</param>
        public override void FillTable(HsqlConnection connection, 
            DataTable table, string[] restrictions)
        {
            restrictions = GetRestrictions(restrictions, 4);

            string catalogName = restrictions[0];
            string viewSchemaPattern = restrictions[1];
            string viewNamePattern = restrictions[2];
            string columnNamePattern = restrictions[3];

            if(WantsIsNull(viewSchemaPattern)
               ||WantsIsNull(viewNamePattern)
               ||WantsIsNull(columnNamePattern))
            {
                return;
            }

            StringBuilder query = ToQueryPrefix(
                "INFORMATION_SCHEMA.SYSTEM_VIEW_COLUMN_USAGE");

            query
                //.Append(And("VIEW_CATALOG", "=", catalogName))
                .Append(And("VIEW_SCHEMA", "LIKE", viewSchemaPattern))
                .Append(And("VIEW_NAME", "LIKE", viewNamePattern))
                .Append(And("COLUMN_NAME", "LIKE", columnNamePattern));

            using (HsqlDataReader reader = Execute(connection, query.ToString()))
            {
                object[] values = new object[reader.FieldCount];

                while (reader.Read())
                {
                    reader.GetValues(values);

                    string viewCatalog = (string) values[0];
                    string viewSchema = (string) values[1];
                    string viewName = (string)values[2];
                    string tableCatalog = (string)values[3];
                    string tableSchema = (string)values[4];
                    string tableName = (string)values[5];
                    string columnName = (string)values[6];

                    AddRow(
                        table,
                        viewCatalog,
                        viewSchema,
                        viewName,
                        tableCatalog,
                        tableSchema,
                        tableName,
                        columnName);
                }
            }
        }

        #endregion

        #region AddRow(...)

        /// <summary>
        /// Adds the row.
        /// </summary>
        /// <param name="table">The table.</param>
        /// <param name="viewCatalog">The view catalog.</param>
        /// <param name="viewSchema">The view schema.</param>
        /// <param name="viewName">Name of the view.</param>
        /// <param name="tableCatalog">The table catalog.</param>
        /// <param name="tableSchema">The table schema.</param>
        /// <param name="tableName">Name of the table.</param>
        /// <param name="columnName">Name of the column.</param>
        public static void AddRow(
            DataTable table,
            string viewCatalog,
            string viewSchema,
            string viewName,
            string tableCatalog,
            string tableSchema,
            string tableName,
            string columnName)
        {
            DataRow row = table.NewRow();

            row["VIEW_CATALOG"] = viewCatalog;
            row["VIEW_SCHEMA"] = viewSchema;
            row["VIEW_NAME"] = viewName;
            row["TABLE_CATALOG"] = tableCatalog;
            row["TABLE_SCHEMA"] = tableSchema;
            row["TABLE_NAME"] = tableName;
            row["COLUMN_NAME"] = columnName;

            table.Rows.Add(row);
        }

        #endregion
    }

    #endregion
}