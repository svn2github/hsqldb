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
using java.sql;
using System.Data.Hsqldb.Client.MetaData;
using CN = System.Data.Common.DbMetaDataCollectionNames;
using HCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataCollectionNames;
using MDCN = System.Data.Common.DbMetaDataColumnNames;
using HMDCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataColumnNames;
#endregion

namespace System.Data.Hsqldb.Client.MetaData.Collection
{
    #region UserDefinedTypesCollection

    /// <summary>
    /// <para>
    /// Provides the <see cref="HCN.UserDefinedTypes"/> collection.
    /// </para>
    /// <img src="/ClassDiagrams/System.Data.Hsqldb.Client.MetaData.Collection.UserDefinedTypesCollection.png"
    ///      alt="UserDefinedTypesCollection Class Diagram"/>
    /// </summary>
    public class UserDefinedTypesCollection : Base.MetaDataCollection
    {
        #region UserDefinedTypesCollection(HsqlConnection)

        /// <summary>
        /// Constructs a new <c>UserDefinedTypesCollection</c> instance
        /// with the given connection.
        /// </summary>
        public UserDefinedTypesCollection() : base() { }

        #endregion

        #region CreateTable()

        /// <summary>
        /// Creates the user defined types table.
        /// </summary>
        /// <returns>The table.</returns>
        public override DataTable CreateTable()
        {
            DataTable table = new DataTable(HCN.UserDefinedTypes);
            DataColumnCollection cols = table.Columns;

            AddColumn(cols, null, "assembly_name", typeof (string));
            AddColumn(cols, null, "udt_name", typeof (string));
            AddColumn(cols, null, "version_major", typeof (object));
            AddColumn(cols, null, "version_minor", typeof (object));
            AddColumn(cols, null, "version_build", typeof (object));
            AddColumn(cols, null, "version_revision", typeof (object));
            AddColumn(cols, null, "culture_info", typeof (object));
            AddColumn(cols, null, "public_key", typeof (object));
            AddColumn(cols, null, "is_fixed_length", typeof (bool));
            AddColumn(cols, null, "max_length", typeof (short));
            AddColumn(cols, null, "Create_Date", typeof (DateTime));
            AddColumn(cols, null, "Permission_set_desc", typeof (string));

            return table;
        }

        #endregion

        #region FillTable(DataTable,string[])

        /// <summary>
        /// Fills the user defined types table.
        /// </summary>
        /// <param name="connection">The connection.</param>
        /// <param name="table">The table.</param>
        /// <param name="restrictions">The restrictions.</param>
        public override void FillTable(HsqlConnection connection,
            DataTable table, string[] restrictions)
        {
            // TODO
        }

        #endregion

        #region AddRow(...)

        /// <summary>
        /// Adds the row.
        /// </summary>
        /// <param name="table">The table.</param>
        /// <param name="assemblyName">Name of the assembly.</param>
        /// <param name="udtName">Name of the udt.</param>
        /// <param name="versionMajor">The version major.</param>
        /// <param name="versionMinor">The version minor.</param>
        /// <param name="versionBuild">The version build.</param>
        /// <param name="versionRevision">The version revision.</param>
        /// <param name="cultureInfo">The culture info.</param>
        /// <param name="publicKey">The public key.</param>
        /// <param name="isFixedLength">if set to <c>true</c> [is fixed length].</param>
        /// <param name="maxLength">Length of the max.</param>
        /// <param name="createDate">The create date.</param>
        /// <param name="permissionSetDesc">The permission set desc.</param>
        public static void AddRow(
            DataTable table,
            string assemblyName,
            string udtName,
            object versionMajor,
            object versionMinor,
            object versionBuild,
            object versionRevision,
            object cultureInfo,
            object publicKey,
            bool isFixedLength,
            short maxLength,
            DateTime createDate,
            string permissionSetDesc)
        {
            DataRow row = table.NewRow();

            row["assembly_name"] = assemblyName;
            row["udt_name"] = udtName;
            row["version_major"] = versionMajor;
            row["version_minor"] = versionMinor;
            row["version_build"] = versionBuild;
            row["version_revision"] = versionRevision;
            row["culture_info"] = cultureInfo;
            row["public_key"] = publicKey;
            row["is_fixed_length"] = isFixedLength;
            row["max_length"] = maxLength;
            row["Create_Date"] = createDate;
            row["Permission_set_desc"] = permissionSetDesc;

            table.Rows.Add(row);
        }

        #endregion
    }

    #endregion
}