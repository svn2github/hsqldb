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
using System.Data.Hsqldb.Client.MetaData;
using StringBuilder = System.Text.StringBuilder;
using CN = System.Data.Common.DbMetaDataCollectionNames;
using HCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataCollectionNames;
using MDCN = System.Data.Common.DbMetaDataColumnNames;
using HMDCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataColumnNames;
#endregion

namespace System.Data.Hsqldb.Client.MetaData.Collection
{
    #region TablesCollection

    /// <summary>
    /// <para>
    /// Provides the <see cref="HCN.Tables"/> collection.
    /// </para>
    /// <img src="/ClassDiagrams/System.Data.Hsqldb.Client.MetaData.Collection.TablesCollection.png"
    ///      alt="TablesCollection Class Diagram"/>
    /// </summary>
    public class TablesCollection : Base.MetaDataCollection
    {
        #region Constants

        private const string sql =
@"SELECT  TABLE_CAT
         ,TABLE_SCHEM
         ,TABLE_NAME
         ,TABLE_TYPE
    FROM INFORMATION_SCHEMA.SYSTEM_TABLES
   WHERE TABLE_TYPE NOT IN ('VIEW', 'SYSTEM VIEW')";

        #endregion

        #region TablesCollection(HsqlConnection)

        /// <summary>
        /// Initializes a new instance of the <see cref="TablesCollection"/> class.
        /// </summary>
        public TablesCollection() : base() { }

        #endregion

        #region CreateTable()

        /// <summary>
        /// Creates a new Tables metadata collection table.
        /// </summary>
        /// <returns></returns>
        public override DataTable CreateTable()
        {
            DataTable table = new DataTable(HCN.Tables);
            DataColumnCollection cols = table.Columns;

            AddColumn(cols, null, "TABLE_CATALOG", typeof (string));
            AddColumn(cols, null, "TABLE_SCHEMA", typeof (string));
            AddColumn(cols, null, "TABLE_NAME", typeof (string));
            AddColumn(cols, null, "TABLE_TYPE", typeof (string));

            return table;
        }

        #endregion

        #region FillTable(DataTable,string[])

        /// <summary>
        /// Fills the tables table.
        /// </summary>
        /// <param name="connection">The connection.</param>
        /// <param name="table">The table.</param>
        /// <param name="restrictions">The restrictions.</param>
        public override void FillTable(HsqlConnection connection, 
            DataTable table, string[] restrictions)
        {
            restrictions = GetRestrictions(restrictions, 4);

            string catalogName = restrictions[0];
            string schemaPattern = restrictions[1];
            string tablePattern = restrictions[2];
            string typePattern = restrictions[3];

            if (schemaPattern != null && schemaPattern.StartsWith("\"") && schemaPattern.EndsWith("\""))
            {
                schemaPattern = schemaPattern.Substring(1, schemaPattern.Length - 2);
            }

            StringBuilder query = new StringBuilder(sql);

            query
                //.Append(And("TABLE_CAT", "=", catalogName))
                .Append(And("TABLE_SCHEM", "LIKE", schemaPattern))
                .Append(And("TABLE_NAME", "LIKE", tablePattern));

            if (typePattern != null)
            {
                string[] types = typePattern.Split(Comma);

                if (types.Length == 1)
                {
                    query.Append(And("TABLE_TYPE", "LIKE", types[0]));
                }
                else
                {
                    query.Append(And("TABLE_TYPE", "IN", ToSQLInList(types)));
                }
            }

            using (HsqlDataReader reader = Execute(connection, query.ToString()))
            {
                object[] values = new object[reader.FieldCount];

                while (reader.Read())
                {
                    reader.GetValues(values);

                    string tableCatalog = (string) values[0];
                    string tableSchema = (string)values[1];
                    string tableName = (string)values[2];
                    string tableType = (string)values[3];

                    AddRow(
                        table,
                        tableCatalog,
                        tableSchema,
                        tableName,
                        tableType);
                }
            }
        }

        #endregion

        #region AddRow(...)

        /// <summary>
        /// Adds a new row to the given Tables metadata collection table.
        /// </summary>
        /// <param name="table">The table to which to add the new row.</param>
        /// <param name="tableCatalog">The described table'a catalog.</param>
        /// <param name="tableSchema">The described table's schema.</param>
        /// <param name="tableName">The simple name of the table.</param>
        /// <param name="tableType">
        /// The type of the table (i.e. TABLE, VIEW, GLOBAL TEMPORARY, etc...)
        /// </param>
        public static void AddRow(
            DataTable table,
            string tableCatalog,
            string tableSchema,
            string tableName,
            string tableType)
        {
            DataRow row = table.NewRow();

            row["TABLE_CATALOG"] = tableCatalog;
            row["TABLE_SCHEMA"] = tableSchema;
            row["TABLE_NAME"] = tableName;
            row["TABLE_TYPE"] = tableType;

            table.Rows.Add(row);
        }

        #endregion
    }

    #endregion
}