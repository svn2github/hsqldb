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
using System.Data.Common;
using CN = System.Data.Common.DbMetaDataCollectionNames;
using HCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataCollectionNames;
using MDCN = System.Data.Common.DbMetaDataColumnNames;
using HMDCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataColumnNames;
using System.Data.Hsqldb.Client.MetaData.Collection;
using System.Data.Hsqldb.Client.MetaData.Collection.Base;

#endregion

namespace System.Data.Hsqldb.Client.MetaData
{
    #region HsqlDatabaseMetaData

    /// <summary>
    /// <para>
    /// Implements the ADO.NET metadata collections contract.
    /// </para>
    /// <img src="/ClassDiagrams/System.Data.Hsqldb.Client.MetaData.HsqlDatabaseMetaData.png"
    ///      alt="HsqlDatabaseMetaData Class Diagram"/>
    /// </summary>
    /// <author name="boucherb@users"/>
    public class HsqlDatabaseMetaData
    {
        #region Fields

        // static, read-only
        private static readonly Dictionary<string, Collection.Base.MetaDataCollection> m_collectionMap;

        // instance
        private HsqlConnection m_connection;

        #endregion

        #region Constructors

        #region Static Initializer

        /// <summary>
        /// Initializes this instance.
        /// </summary>
        static HsqlDatabaseMetaData()
        {
            Dictionary<string, MetaDataCollection> map
                = new Dictionary<string, MetaDataCollection>();

            //
            map[CN.DataSourceInformation]
                = new DataSourceInformationCollection();

            map[CN.DataTypes]
                = new DataTypesCollection();
            map[CN.MetaDataCollections]
                = new MetaDataCollectionsCollection();
            map[CN.ReservedWords]
                = new ReservedWordsCollection();
            map[CN.Restrictions]
                = new RestrictionsCollection();

            //
            map[HCN.Columns]
                = new ColumnsCollection();
            map[HCN.Databases]
                = new DatabasesCollection();
            map[HCN.ForeignKeyColumns]
                = new ForeignKeyColumnsCollection();
            map[HCN.ForeignKeys]
                = new ForeignKeysCollection();
            map[HCN.IndexColumns]
                = new IndexColumnsCollection();
            map[HCN.Indexes]
                = new IndexesCollection();
            map[HCN.PrimaryKeyColumns]
                = new PrimaryKeyColumnsCollection();
            map[HCN.PrimaryKeys]
                = new PrimaryKeysCollection();
            map[HCN.ProcedureParameters]
                = new ProcedureParametersCollection();
            map[HCN.Procedures]
                = new ProceduresCollection();
            map[HCN.Sequences]
                = new SequencesCollection();
            map[HCN.Tables]
                = new TablesCollection();
            map[HCN.TableCheckConstraints]
                = new TableCheckConstraintsCollection();
            map[HCN.TableTriggers]
                = new TableTriggersCollection();
            map[HCN.UserDefinedTypes]
                = new UserDefinedTypesCollection();
            map[HCN.Users]
                = new UsersCollection();
            map[HCN.ViewColumns]
                = new ViewColumnsCollection();
            map[HCN.Views]
                = new ViewsCollection();

            map[HCN.UniqueConstraints]
                = new UniqueConstraintsCollection();
            map[HCN.UniqueConstraintColumns]
                = new UniqueConstraintColumnsCollection();

            map[HCN.Schemas] = new SchemasCollection();

            m_collectionMap = map;
        }

        #endregion

        #region HsqlDatabaseMetaData(HsqlConnection)

        /// <summary>
        /// Constructs a new <c>HsqlDatabaseMetaData</c> instance
        /// with the given connection.
        /// </summary>
        /// <param name="connection">The connection.</param>
        public HsqlDatabaseMetaData(HsqlConnection connection)
        {
            if (connection == null)
            {
                throw new ArgumentNullException("connection");
            }

            m_connection = connection;
        }

        #endregion

        #endregion

        #region Methods

        #region GetSchema()

        /// <summary>
        /// Gets the
        /// <see cref="DbMetaDataCollectionNames.MetaDataCollections"/>
        /// collection.
        /// </summary>
        /// <returns>DataTable</returns>
        public DataTable GetSchema()
        {
            return GetSchema(CN.MetaDataCollections);
        }

        #endregion

        #region GetSchema(string)

        /// <summary>
        /// Gets the named collection.
        /// </summary>
        /// <param name="collectionName">Name of the collection.</param>
        /// <returns>DataTable</returns>
        public DataTable GetSchema(string collectionName)
        {
            return GetSchema(collectionName, null);
        }

        #endregion

        #region GetSchema(string,string[])

        /// <summary>
        /// Gets the named collection filtered by the given restrictions.
        /// </summary>
        /// <param name="collectionName">Name of the collection.</param>
        /// <param name="restrictions">The restrictions.</param>
        /// <returns>DataTable</returns>
        public DataTable GetSchema(string collectionName,
            string[] restrictions)
        {
            if (collectionName == null)
            {
                collectionName = CN.MetaDataCollections;
            }

            Collection.Base.MetaDataCollection collection;

            if (!m_collectionMap.TryGetValue(collectionName, out collection))
            {
                throw new ArgumentException(
                    "Unknown collection: " + collectionName,
                    "collectionName"); // NOI18N
            }

            return collection.GetSchema(m_connection, restrictions);
        }

        #endregion 
        
        #endregion
    }

    #endregion
}