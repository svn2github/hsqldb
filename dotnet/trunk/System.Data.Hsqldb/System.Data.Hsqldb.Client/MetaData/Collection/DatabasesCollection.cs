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
using System.Data.Hsqldb.Client.MetaData;
using Vector = java.util.Vector;
using org.hsqldb;
using CN = System.Data.Common.DbMetaDataCollectionNames;
using HCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataCollectionNames;
using MDCN = System.Data.Common.DbMetaDataColumnNames;
using HMDCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataColumnNames;
using System.Data.Hsqldb.Common.Enumeration;
using System.Text;
#endregion

namespace System.Data.Hsqldb.Client.MetaData.Collection
{
    #region DatabasesCollection

    /// <summary>
    /// <para>
    /// Provides the <see cref="HCN.Databases"/> collection.
    /// </para>
    /// <img src="/ClassDiagrams/System.Data.Hsqldb.Client.MetaData.Collection.DatabasesCollection.png"
    ///      alt="DatabasesCollection Class Diagram"/>
    /// </summary>
    /// <author name="boucherb@users"/>
    public class DatabasesCollection : Base.MetaDataCollection
    {
        #region Constants
        private const string sql =
@"select value, null, null 
    from information_schema.system_sessioninfo
   where key = 'DATABASE'";
        #endregion

        #region DatabasesCollection(HsqlConnection)

        /// <summary>
        /// Initializes a new instance of the <see cref="DatabasesCollection"/> class.
        /// </summary>
        public DatabasesCollection()
            : base()
        {
        }

        #endregion

        #region CreateTable()

        /// <summary>
        /// Creates a new <c>Databases</c> metadata collection table.
        /// </summary>
        /// <returns>The table.</returns>
        public override DataTable CreateTable()
        {
            DataTable table = new DataTable(HCN.Databases);
            DataColumnCollection cols = table.Columns;

            AddColumn(cols, null, "database_name", typeof (object));
            AddColumn(cols, null, "dbid", typeof (int));
            AddColumn(cols, null, "create_date", typeof (DateTime));

            return table;
        }

        #endregion

        #region FillTable(DataTable,string[])

        /// <summary>
        /// Fills a <c>Databases</c> metadata collection table.
        /// </summary>
        /// <param name="connection">The connection.</param>
        /// <param name="table">The table.</param>
        /// <param name="restrictions">The restrictions.</param>
        public override void FillTable(HsqlConnection connection, 
            DataTable table, string[] restrictions)
        {
            string databaseNamePattern = GetRestrictions(restrictions, 1)[0];

            if (WantsIsNull(databaseNamePattern))
            {
                return;
            }

            StringBuilder query = new StringBuilder(sql);

            query.Append(And("value", "LIKE", databaseNamePattern));

            using (HsqlDataReader reader = Execute(connection, query.ToString()))
            {
                object[] values = new object[reader.FieldCount];

                while (reader.Read())
                {
                    reader.GetValues(values);

                    string databaseName = (string)values[0];

                    AddRow(table, databaseName, null, null);
                }
            }            

            //Like predicate;

            //if (databaseNamePattern == null)
            //{
            //    predicate = null;
            //}
            //else
            //{
            //    predicate = new Like(false, null);

            //    predicate.SetPattern(databaseNamePattern);
            //}

            //switch (m_connection.m_settings.Protocol)
            //{
            //    case ConnectionProtocol.File:
            //    case ConnectionProtocol.Res:
            //    case ConnectionProtocol.Mem:
            //        {
            //            Vector v = DatabaseManager.getDatabaseURIs();
            //            int count = v.size();

            //            for (int i = 0; i < count; i++)
            //            {
            //                string databaseName = (string)v.elementAt(i);

            //                if ((predicate == null) || predicate.Matches(databaseName) == true);                                
            //                {
            //                    AddRow(table, databaseName, (short)i, null);
            //                }
            //            }

            //            break;
            //        }
            //    default:
            //        {
            //            HsqlSession session = m_connection.Session;
            //            string databaseName = (string)session
            //                .ExecuteScalarDirect(sql);

            //            if (predicate == null || predicate.Matches(databaseName) == true)
            //            {
            //                AddRow(table, databaseName, 0, null);
            //            }

            //            break;
            //        }
            //}
        }

        #endregion

        #region AddRow(...)

        /// <summary>
        /// Adds a new row the given Databases metadata collection table.
        /// </summary>
        /// <param name="table">The table.</param>
        /// <param name="databaseName">Name of the database.</param>
        /// <param name="dbid">The database id.</param>
        /// <param name="createDate">The database creation date.</param>
        public static void AddRow(
            DataTable table,
            object databaseName,
            short? dbid,
            DateTime? createDate)
        {
            DataRow row = table.NewRow();

            row["database_name"] = databaseName;

            if (dbid != null)
            {
                row["dbid"] = dbid;
            }

            if (createDate != null)
            {
                row["create_date"] = createDate;
            }

            table.Rows.Add(row);
        }

        #endregion
    }

    #endregion
}