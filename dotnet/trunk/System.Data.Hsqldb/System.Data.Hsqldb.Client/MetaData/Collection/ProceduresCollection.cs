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
using System.Data.Hsqldb.Client.MetaData;
using CN = System.Data.Common.DbMetaDataCollectionNames;
using HCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataCollectionNames;
using MDCN = System.Data.Common.DbMetaDataColumnNames;
using HMDCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataColumnNames;
#endregion

namespace System.Data.Hsqldb.Client.MetaData.Collection
{
    #region ProceduresCollection

    /// <summary>
    /// <para>
    /// Provides the <see cref="HCN.Procedures"/> collection.
    /// </para>
    /// <img src="../Documentation/ClassDiagrams/System.Data.Hsqldb.Client.MetaData.Collection.ProceduresCollection.png"
    ///      alt="ProceduresCollection Class Diagram"/>
    /// </summary>
    public class ProceduresCollection : Base.MetaDataCollection
    {
        #region Constants

        private const string sql =
@"SELECT DISTINCT
         p.procedure_cat AS specific_catalog
        ,p.procedure_schem as specific_schema
        ,p.specific_name
        ,p.procedure_cat AS routine_catalog
        ,p.procedure_schem AS routine_schema
        ,p.procedure_name as routine_name
        ,CASE p.procedure_type
             WHEN 1 THEN 'PROCEDURE'
             WHEN 2 THEN 'FUNCTION'
             ELSE 'UNKNOWN'
         END AS routine_type
        ,NULL AS created
        ,NULL AS last_altered
    FROM information_schema.system_procedures p
   WHERE origin = 'ALIAS'";

        #endregion

        #region ProceduresCollection(HsqlConnection)

        /// <summary>
        /// Initializes a new instance of the <see cref="ProceduresCollection"/> class.
        /// </summary>
        public ProceduresCollection() : base() { }

        #endregion

        #region CreateTable()

        /// <summary>
        /// Creates the procedures table.
        /// </summary>
        /// <returns></returns>
        public override DataTable CreateTable()
        {
            DataTable table = new DataTable(HCN.Procedures);
            DataColumnCollection cols = table.Columns;

            AddColumn(cols, null, "SPECIFIC_CATALOG", typeof (string));
            AddColumn(cols, null, "SPECIFIC_SCHEMA", typeof (string));
            AddColumn(cols, null, "SPECIFIC_NAME", typeof (string));
            AddColumn(cols, null, "ROUTINE_CATALOG", typeof (string));
            AddColumn(cols, null, "ROUTINE_SCHEMA", typeof (string));
            AddColumn(cols, null, "ROUTINE_NAME", typeof (string));
            AddColumn(cols, null, "ROUTINE_TYPE", typeof (string));
            AddColumn(cols, null, "CREATED", typeof (DateTime));
            AddColumn(cols, null, "LAST_ALTERED", typeof (DateTime));

            return table;
        }

        #endregion

        #region FillTable(DataTable,string[])

        /// <summary>
        /// Fills the procedures table.
        /// </summary>
        /// <param name="connection">The connection.</param>
        /// <param name="table">The table.</param>
        /// <param name="restrictions">The restrictions.</param>
        public override void FillTable(HsqlConnection connection, 
            DataTable table, string[] restrictions)
        {
            restrictions = GetRestrictions(restrictions, 4);

            string catalogName = restrictions[0];
            string specificSchemaPattern = restrictions[1];
            string specificNamePattern = restrictions[2];
            string typePattern = restrictions[3];

            if (WantsIsNull(specificSchemaPattern)
                ||WantsIsNull(specificNamePattern)
                ||WantsIsNull(typePattern))
            {
                return;
            }

            StringBuilder query = new StringBuilder(sql);

            query
                //.Append(And("SPECIFIC_CATALOG", "=", catalogName))
                .Append(And("SPECIFIC_SCHEMA", "LIKE", specificSchemaPattern))
                .Append(And("SPECIFIC_NAME", "LIKE", specificNamePattern))
                .Append(And("ROUTINE_TYPE", "LIKE", typePattern))
                .Append(" ORDER BY 1, 2, 3");

            using (HsqlDataReader reader = Execute(connection, query.ToString()))
            {
                object[] values = new object[reader.FieldCount];

                while (reader.Read())
                {
                    reader.GetValues(values);

                    string specificCatalog = (string) values[0];
                    string specificSchema = (string)values[1];
                    string specifcName = (string)values[2];
                    string routineCatalog = (string)values[3];
                    string routineSchema = (string)values[4];
                    string routineName = (string)values[5];
                    string routineType = (string)values[6];
                    DateTime? created = (DateTime?)values[7];
                    DateTime? lastAltered = (DateTime?)values[8];

                    AddRow(
                        table,
                        specificCatalog,
                        specificSchema,
                        specifcName,
                        routineCatalog,
                        routineSchema,
                        routineName,
                        routineType,
                        created,
                        lastAltered);
                }
            }
        }

        #endregion

        #region AddRow(...)

        /// <summary>
        /// Adds the procedures row.
        /// </summary>
        /// <param name="table">The table.</param>
        /// <param name="specificCatalog">The specific catalog.</param>
        /// <param name="specificSchema">The specific schema.</param>
        /// <param name="specificName">Name of the specific.</param>
        /// <param name="routineCatalog">The routine catalog.</param>
        /// <param name="routineSchema">The routine schema.</param>
        /// <param name="routineName">Name of the routine.</param>
        /// <param name="routineType">Type of the routine.</param>
        /// <param name="created">The created.</param>
        /// <param name="lastAltered">The last altered.</param>
        public static void AddRow(
            DataTable table,
            string specificCatalog,
            string specificSchema,
            string specificName,
            string routineCatalog,
            string routineSchema,
            string routineName,
            string routineType,
            DateTime? created,
            DateTime? lastAltered)
        {
            DataRow row = table.NewRow();

            row["SPECIFIC_CATALOG"] = specificCatalog;
            row["SPECIFIC_SCHEMA"] = specificSchema;
            row["SPECIFIC_NAME"] = specificName;
            row["ROUTINE_CATALOG"] = routineCatalog;
            row["ROUTINE_SCHEMA"] = routineSchema;
            row["ROUTINE_NAME"] = routineName;
            row["ROUTINE_TYPE"] = routineType;
            if (created != null)
            {
                row["CREATED"] = created;
            }
            if(lastAltered != null)
            {
                row["LAST_ALTERED"] = lastAltered;
            }

            table.Rows.Add(row);
        }

        #endregion
    }

    #endregion
}