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
using System.Data.Common;
#endregion

namespace System.Data.Hsqldb.Client.MetaData
{
    #region HsqlMetaDataCollectionNames

    /// <summary>
    /// <para>
    /// Provides metadata collection name constants.
    /// </para>
    /// <img src="../Documentation/ClassDiagrams/System.Data.Hsqldb.Client.MetaData.HsqlMetaDataCollectionNames.png" 
    ///      alt="HsqlMetaDataCollectionNames Class Diagram"/>
    /// </summary>    
    /// <author name="boucherb@users"/>
    public static class HsqlMetaDataCollectionNames
    {
        #region Users

        /// <summary>
        /// A constant for use with the <see cref="M:DbConnection.GetSchema"/>
        /// method that specifies the <c>Users</c> metadata collection.
        /// </summary>
        public const string Users = "Users";

        #endregion

        #region Databases

        /// <summary>
        /// A constant for use with the <see cref="O:System.Data.Hsqldb.DbConnection.GetSchema"/>
        /// method that specifies the <c>Databases</c> metadata collection.
        /// </summary>
        public const string Databases = "Databases";

        #endregion

        #region Tables

        /// <summary>
        /// A constant for use with the <see cref="O:System.Data.Hsqldb.DbConnection.GetSchema"/>
        /// method that specifies the <c>Tables</c> metadata collection.
        /// </summary>
        public const string Tables = "Tables";

        #endregion

        #region Columns

        /// <summary>
        /// A constant for use with the <see cref="O:System.Data.Hsqldb.DbConnection.GetSchema"/>
        /// method that specifies the <c>Columns</c> metadata collection.
        /// </summary>
        public const string Columns = "Columns";

        #endregion

        #region Views

        /// <summary>
        /// A constant for use with the <see cref="O:System.Data.Hsqldb.DbConnection.GetSchema"/>
        /// method that specifies the <c>Views</c> metadata collection.
        /// </summary>
        public const string Views = "Views";

        #endregion

        #region ViewColumns

        /// <summary>
        /// A constant for use with the <see cref="O:System.Data.Hsqldb.DbConnection.GetSchema"/>
        /// method that specifies the <c>ViewColumns</c> metadata collection.
        /// </summary>
        public const string ViewColumns = "ViewColumns";

        #endregion

        #region ProcedureParameters

        /// <summary>
        /// A constant for use with the <see cref="O:System.Data.Hsqldb.DbConnection.GetSchema"/>
        /// method that specifies the <c>ProcedureParameters</c> metadata
        /// collection.
        /// </summary>
        public const string ProcedureParameters = "ProcedureParameters";

        #endregion

        #region Procedures

        /// <summary>
        /// A constant for use with the <see cref="O:System.Data.Hsqldb.DbConnection.GetSchema"/>
        /// method that specifies the <c>Procedures</c> metadata collection.
        /// </summary>
        public const string Procedures = "Procedures";

        #endregion

        #region PrimaryKeys

        /// <summary>
        /// A constant for use with the <see cref="O:System.Data.Hsqldb.DbConnection.GetSchema"/>
        /// method that specifies the <c>PrimaryKeys</c> metadata collection.
        /// </summary>
        public const string PrimaryKeys = "PrimaryKeys";

        #endregion

        #region PrimaryKeyColumns

        /// <summary>
        /// A constant for use with the <see cref="O:System.Data.Hsqldb.DbConnection.GetSchema"/>
        /// method that specifies the <c>PrimaryKeyColumns</c> metadata
        /// collection.
        /// </summary>
        public const string PrimaryKeyColumns = "PrimaryKeyColumns";

        #endregion

        #region ForeignKeys

        /// <summary>
        /// A constant for use with the <see cref="O:System.Data.Hsqldb.DbConnection.GetSchema"/>
        /// method that specifies the <c>ForeignKeys</c> metadata collection.
        /// </summary>
        public const string ForeignKeys = "ForeignKeys";

        #endregion

        #region ForeignKeyColumns

        /// <summary>
        /// A constant for use with the <see cref="O:System.Data.Hsqldb.DbConnection.GetSchema"/>
        /// method that specifies the <c>ForeignKeyColumns</c> metadata
        /// collection.
        /// </summary>
        public const string ForeignKeyColumns = "ForeignKeyColumns";

        #endregion

        #region TableCheckConstraints
        /// <summary>
        /// A constant for use with the <see cref="O:System.Data.Hsqldb.DbConnection.GetSchema"/>
        /// method that specifies the <c>TableCheckConstraints</c> metadata
        /// collection.
        /// </summary>
        public const string TableCheckConstraints = "TableCheckConstraints";
        #endregion

        #region IndexColumns

        /// <summary>
        /// A constant for use with the <see cref="O:System.Data.Hsqldb.DbConnection.GetSchema"/>
        /// method that specifies the <c>IndexColumns</c> metadata collection.
        /// </summary>
        public const string IndexColumns = "IndexColumns";

        #endregion

        #region TableTriggers
        /// <summary>
        /// A constant for use with the <see cref="O:System.Data.Hsqldb.DbConnection.GetSchema"/>
        /// method that specifies the <c>Triggers</c> metadata collection.
        /// </summary>
        public const string TableTriggers = "TableTriggers";
        #endregion

        #region Indexes

        /// <summary>
        /// A constant for use with the <see cref="O:System.Data.Hsqldb.DbConnection.GetSchema"/>
        /// method that specifies the <c>Indexes</c> metadata collection.
        /// </summary>
        public const string Indexes = "Indexes";

        #endregion

        #region Sequences

        /// <summary>
        /// A constant for use with the <see cref="O:System.Data.Hsqldb.DbConnection.GetSchema"/>
        /// method that specifies the <c>Sequences</c> metadata collection.
        /// </summary>
        public const string Sequences = "Sequences";

        #endregion

        #region UserDefinedTypes

        /// <summary>
        /// A constant for use with the <see cref="O:System.Data.Hsqldb.DbConnection.GetSchema"/>
        /// method that specifies the <c>UserDefinedTypes</c> metadata
        /// collection.
        /// </summary>
        public const string UserDefinedTypes = "UserDefinedTypes";

        #endregion

        #region UniqueConstraints
        /// <summary>
        /// A constant for use with the <see cref="O:System.Data.Hsqldb.DbConnection.GetSchema"/>
        /// method that specifies the <c>UniqueConstraints</c> metadata
        /// collection.
        /// </summary>
        public const string UniqueConstraints = "UniqueConstraints";

        #endregion

        #region UniqueConstraintColumns

        /// <summary>
        /// A constant for use with the <see cref="O:System.Data.Hsqldb.DbConnection.GetSchema"/>
        /// method that specifies the <c>UniqueConstraints</c> metadata
        /// collection.
        /// </summary>
        public const string UniqueConstraintColumns = "UniqueConstraintColumns";

        #endregion

        #region Schemas

        /// <summary>
        /// A constant for use with the <see cref="O:System.Data.Hsqldb.DbConnection.GetSchema"/>
        /// method that specifies the <c>Schemas</c> metadata
        /// collection.
        /// </summary>
        public const string Schemas = "Schemas";

        #endregion
    }

    #endregion
}