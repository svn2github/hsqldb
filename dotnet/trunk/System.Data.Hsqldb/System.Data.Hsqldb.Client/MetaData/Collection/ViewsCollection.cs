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
using MDCN = System.Data.Common.DbMetaDataColumnNames;
using HCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataCollectionNames;
using HMDCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataColumnNames;
#endregion

namespace System.Data.Hsqldb.Client.MetaData.Collection
{
    #region ViewsCollection

    /// <summary>
    /// <para>
    /// Provides the <see cref="HCN.Views"/> collection.
    /// </para>
    /// <img src="/ClassDiagrams/System.Data.Hsqldb.Client.MetaData.Collection.ViewsCollection.png"
    ///      alt="ViewsCollection Class Diagram"/>
    /// </summary>
    public class ViewsCollection : Base.MetaDataCollection
    {
        #region ViewsCollection(HsqlConnection

        /// <summary>
        /// Initializes a new instance of the <see cref="ViewsCollection"/> class.
        /// </summary>
        public ViewsCollection() : base() { }

        #endregion

        #region CreateTable()

        /// <summary>
        /// Creates a new Views metdata collection table.
        /// </summary>
        /// <returns>The table.</returns>
        public override DataTable CreateTable()
        {
            DataTable table = new DataTable(HCN.Views);
            DataColumnCollection cols = table.Columns;

            AddColumn(cols, null, "TABLE_CATALOG", typeof (string));
            AddColumn(cols, null, "TABLE_SCHEMA", typeof (string));
            AddColumn(cols, null, "TABLE_NAME", typeof (string));
            AddColumn(cols, null, "VIEW_DEFINITION", typeof (string));
            AddColumn(cols, null, "CHECK_OPTION", typeof (string));
            AddColumn(cols, null, "IS_UPDATABLE", typeof (string));

            return table;
        }

        #endregion

        #region FillTable(DataTable,string[])

        /// <summary>
        /// Fills the views table.
        /// </summary>
        /// <param name="connection">The connection.</param>
        /// <param name="table">The table.</param>
        /// <param name="restrictions">The restrictions.</param>
        public override void FillTable(HsqlConnection connection, 
            DataTable table, string[] restrictions)
        {
            restrictions = GetRestrictions(restrictions, 3);

            string catalogName = restrictions[0];
            string schemaNamePattern = restrictions[1];
            string tableNamePattern = restrictions[2];

            if (WantsIsNull(schemaNamePattern)
                || WantsIsNull(tableNamePattern))
            {
                return;
            }

            StringBuilder query = ToQueryPrefix("INFORMATION_SCHEMA.SYSTEM_VIEWS");

            query
                //.Append(And("TABLE_CATALOG", "=", catalogName))
                .Append(And("TABLE_SCHEMA", "LIKE", schemaNamePattern))
                .Append(And("TABLE_NAME", "LIKE", tableNamePattern));


            using (HsqlDataReader reader = Execute(connection, query.ToString()))
            {
                object[] values = new object[reader.FieldCount];

                while (reader.Read())
                {
                    reader.GetValues(values);

                    AddRow(
                        table,
                        (string)values[0],
                        (string)values[1],
                        (string)values[2],
                        (string)values[3],
                        (string)values[4],
                        (string)values[5]);
                }
            }
        }

        #endregion

        #region AddRow(...)

        /// <summary>
        /// Adds the views row.
        /// </summary>
        /// <param name="table">The table.</param>
        /// <param name="tableCatalog">The table catalog.</param>
        /// <param name="tableSchema">The table schema.</param>
        /// <param name="tableName">Name of the table.</param>
        /// <param name="viewDefinition">The view definition.</param>
        /// <param name="checkOption">The check option.</param>
        /// <param name="isUpdatable">The is updatable.</param>
        public static void AddRow(
            DataTable table,
            string tableCatalog,
            string tableSchema,
            string tableName,
            string viewDefinition,
            string checkOption,
            string isUpdatable)
        {
            DataRow row = table.NewRow();

            row["TABLE_CATALOG"] = tableCatalog;
            row["TABLE_SCHEMA"] = tableSchema;
            row["TABLE_NAME"] = tableName;
            row["VIEW_DEFINITION"] = viewDefinition;
            row["CHECK_OPTION"] = checkOption;
            row["IS_UPDATABLE"] = isUpdatable;

            table.Rows.Add(row);
        }

        #endregion
    }

    #endregion
}