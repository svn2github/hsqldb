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
using System.Data.Hsqldb.Client.MetaData;
using StringBuilder = System.Text.StringBuilder;
using CN = System.Data.Common.DbMetaDataCollectionNames;
using HCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataCollectionNames;
using MDCN = System.Data.Common.DbMetaDataColumnNames;
using HMDCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataColumnNames;
#endregion

namespace System.Data.Hsqldb.Client.MetaData.Collection
{
    #region UsersCollection

    /// <summary>
    /// <para>
    /// Provides the <see cref="HCN.Users"/> collection.
    /// </para>
    /// <img src="../Documentation/ClassDiagrams/System.Data.Hsqldb.Client.MetaData.Collection.UsersCollection.png"
    ///      alt="UsersCollection Class Diagram"/>
    /// </summary>
    public class UsersCollection : Base.MetaDataCollection
    {
        #region Constants

        private const string sql =
@"SELECT NULL AS UID
        ,sa.authorization_name AS USER_NAME
        ,NULL AS CREATEDATE
        ,NULL AS UPDATEDATE
        ,sa.AUTHORIZATION_TYPE
        ,CASE WHEN (su.admin IS NULL) THEN
              CASE WHEN (sa.authorization_name = 'DBA')
                   THEN TRUE
                   WHEN (0 < (SELECT COUNT(*)
                                FROM information_schema.system_role_authorization_descriptors srad
                               WHERE (srad.grantee = sa.authorization_name)
                                 AND (srad.role_name = 'DBA')))
                   THEN TRUE
                   ELSE NULL
              END
              ELSE su.admin
         END AS IS_ADMIN
    FROM information_schema.system_authorizations sa LEFT OUTER JOIN
         information_schema.system_users su
      ON (su.user = sa.authorization_name)
   WHERE 1=1";

        #endregion

        #region UsersCollection(HsqlConnection)

        /// <summary>
        /// Initializes a new instance of the <see cref="UsersCollection"/> class.
        /// </summary>
        public UsersCollection() : base() { }

        #endregion

        #region CreateTable()

        /// <summary>
        /// Creates a new Users metadata collection table.
        /// </summary>
        /// <returns></returns>
        public override DataTable CreateTable()
        {
            DataTable table = new DataTable(HCN.Users);

            DataColumnCollection cols = table.Columns;

            AddColumn(cols, null, "uid", typeof (ushort));
            AddColumn(cols, null, "user_name", typeof (string));
            AddColumn(cols, null, "createdate", typeof (DateTime));
            AddColumn(cols, null, "updatedate", typeof (DateTime));
            AddColumn(cols, null, "authorization_type", typeof(string));
            AddColumn(cols, null, "is_admin", typeof(bool));

            return table;
        }

        #endregion

        #region FillTable(DataTable,string[])

        /// <summary>
        /// Fills a Users metadata collection table.
        /// </summary>
        /// <param name="connection">The connection.</param>
        /// <param name="table">The table.</param>
        /// <param name="restrictions">The restrictions.</param>
        public override void FillTable(HsqlConnection connection, 
            DataTable table, string[] restrictions)
        {
            restrictions = GetRestrictions(restrictions, 1);

            string userNamePattern = restrictions[0];

            if(WantsIsNull(userNamePattern))
            {
                return;
            }

            StringBuilder query = new StringBuilder(sql);

            query.Append(And("SA.AUTHORIZATION_NAME", "LIKE", userNamePattern));

            using (HsqlDataReader reader = Execute(connection, query.ToString()))
            {
                object[] values = new object[reader.FieldCount];

                while (reader.Read())
                {
                    reader.GetValues(values);

                    short? uid = (short?) values[0];
                    string userName = (string) values[1];
                    DateTime? createDate = (DateTime?) values[2];
                    DateTime? updateDate = (DateTime?) values[3];
                    string authorizationType = (string)values[4];
                    bool? isAdmin = (bool?)values[5];

                    AddRow(
                        table,
                        uid,
                        userName,
                        createDate,
                        updateDate,
                        authorizationType,
                        isAdmin);
                }
            }
        }

        #endregion

        #region AddRow(...)

        /// <summary>
        /// Adds a new row to the given Users metadata collection table.
        /// </summary>
        /// <param name="table">The table.</param>
        /// <param name="uid">The user id number.</param>
        /// <param name="userName">Name of the user.</param>
        /// <param name="createDate">The user creation date.</param>
        /// <param name="updateDate">The user update date.</param>
        /// <param name="authorizationType">Type of the authorization (role or user).</param>
        /// <param name="isAdmin">Whether the grantee has the admin role.</param>
        public static void AddRow(
            DataTable table,
            short? uid,
            string userName,
            DateTime? createDate,
            DateTime? updateDate,
            string authorizationType,
            bool? isAdmin)
        {
            DataRow row = table.NewRow();

            if (uid != null)
            {
                row["uid"] = uid;
            }
            row["user_name"] = userName;
            if (createDate != null)
            {
                row["createdate"] = createDate;
            }
            if (updateDate != null)
            {
                row["updatedate"] = updateDate;
            }
            row["authorization_type"] = authorizationType;
            if(isAdmin != null)
            {
                row["is_admin"] = isAdmin;
            }

            table.Rows.Add(row);
        }

        #endregion
    }

    #endregion
}