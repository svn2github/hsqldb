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
using System.Collections.Generic;
using System.Data;
using System.Text;
using HCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataCollectionNames;
#endregion

namespace System.Data.Hsqldb.Client.MetaData.Collection
{
    /// <summary>
    /// 
    /// </summary>
    public class SchemasCollection : Base.MetaDataCollection
    {
        private const string sql =
@"select catalog_name 
        ,schema_name 
        ,schema_owner
        ,default_character_set_catalog
        ,default_character_set_schema
        ,default_character_set_name
        ,sql_path 
   from information_schema.system_schemata
  where 1=1";

        /// <summary>
        /// Constructs a new <c>SchemasCollection</c> instance with the given connection.
        /// </summary>
        public SchemasCollection() : base()
        {
        }

        #region CreateTable()

        /// <summary>
        /// Creates a new <c>Schemas</c> metadata collection table.
        /// </summary>
        /// <returns>The table.</returns>
        public override DataTable CreateTable()
        {
            DataTable table = new DataTable(HCN.Schemas);
            DataColumnCollection cols = table.Columns;

            AddColumn(cols, null, "catalog_name", typeof(string));
            AddColumn(cols, null, "schema_name", typeof(string));
            AddColumn(cols, null, "schema_owner", typeof(string));
            AddColumn(cols, null, "default_character_set_catalog", typeof(string));
            AddColumn(cols, null, "default_character_set_schema", typeof(string));
            AddColumn(cols, null, "default_character_set_name", typeof(string));
            AddColumn(cols, null, "sql_path", typeof(string));

            return table;
        }

        #endregion


        #region FillTable(DataTable,string[])

        /// <summary>
        /// Fills the given data table.
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
            string schemaOwnerPattern = restrictions[2];

            StringBuilder query = new StringBuilder(sql);

            query
                //.Append(And("catalog_name", "=", catalogName))
                .Append(And("schema_name", "LIKE", schemaNamePattern))
                .Append(And("schema_owner", "LIKE", schemaOwnerPattern));

            using (HsqlDataReader reader = Execute(connection, query.ToString()))
            {
                object[] values = new object[reader.FieldCount];

                while (reader.Read())
                {
                    reader.GetValues(values);

                           catalogName = (string)values[0];
                    string schemaName = (string)values[1];
                    string schemaOwner = (string)values[2];
                    string dcsCatalog = (string)values[3];
                    string dcsSchema = (string)values[4];
                    string dcsName = (string)values[5];
                    string sqlPath = (string)values[6];

                    AddRow(
                        table,
                        catalogName,
                        schemaName,
                        schemaOwner,
                        dcsCatalog,
                        dcsSchema,
                        dcsName,
                        sqlPath);
                }
            }
        }

        /// <summary>
        /// Adds the specified row to the given data table.
        /// </summary>
        /// <param name="table">The data table to which to add the row.</param>
        /// <param name="catalogName">Name of the catalog containing the schema.</param>
        /// <param name="schemaName">Name of the schema.</param>
        /// <param name="schemaOwner">The schema's owner.</param>
        /// <param name="dcsCatalog">The schema's default character set catalog.</param>
        /// <param name="dcsSchema">The schema's default character set schema.</param>
        /// <param name="dcsName">Simple name of the schema's default character set.</param>
        /// <param name="sqlPath">The schema's SQL path.</param>
        public static void AddRow(
            DataTable table, 
            string catalogName, 
            string schemaName, 
            string schemaOwner, 
            string dcsCatalog, 
            string dcsSchema, 
            string dcsName,
            string sqlPath)
        {
            DataRow row = table.NewRow();

            row["catalog_name"] = catalogName;
            row["schema_name"] = schemaName;
            row["schema_owner"] = schemaOwner;
            row["default_character_set_catalog"] = dcsCatalog;
            row["default_character_set_schema"] = dcsSchema;
            row["default_character_set_name"] = dcsName;
            row["sql_path"] = sqlPath;
        }

        #endregion
    }
}
