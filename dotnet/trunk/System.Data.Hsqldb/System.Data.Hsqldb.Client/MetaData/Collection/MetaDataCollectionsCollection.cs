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
using CN = System.Data.Common.DbMetaDataCollectionNames;
using HCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataCollectionNames;
using MDCN = System.Data.Common.DbMetaDataColumnNames;
#endregion

namespace System.Data.Hsqldb.Client.MetaData.Collection
{
    #region MetaDataCollectionsCollection

    /// <summary>
    /// <para>
    /// Provides the <see cref="CN.MetaDataCollections"/> collection.
    /// </para>
    /// <img src="../Documentation/ClassDiagrams/System.Data.Hsqldb.Client.MetaData.Collection.MetaDataCollectionsCollection.png"
    ///      alt="MetaDataCollectionsCollection Class Diagram"/>
    /// </summary>
    public class MetaDataCollectionsCollection : Base.CachedMetadataCollection
    {
        #region MetaDataCollectionsCollection()

        /// <summary>
        /// Initializes a new instance of the
        /// <see cref="MetaDataCollectionsCollection"/> class.
        /// </summary>
        public MetaDataCollectionsCollection() : base() { }

        #endregion

        #region CreateTable()

        /// <summary>
        /// Creates a new <c>IMetaDataCollections</c> metadata collection table.
        /// </summary>
        /// <returns>
        /// A new <see cref="DataTable"/> initialized with the columns
        /// required to hold the metadata about the metadata collections
        /// supported by the HSQLDB ADO.NET data provider.
        /// </returns>
        public override DataTable CreateTable()
        {
            DataTable table = new DataTable(CN.MetaDataCollections);
            DataColumnCollection cols = table.Columns;

            AddColumn(cols, null, MDCN.CollectionName, typeof (string));
            AddColumn(cols, null, MDCN.NumberOfRestrictions, typeof (int));
            AddColumn(cols, null, MDCN.NumberOfIdentifierParts, typeof (int));

            return table;
        }

        #endregion

        #region FillTable(DataTable,string[])

        /// <summary>
        /// Fills the table.
        /// </summary>
        /// <param name="connection">The connection.</param>
        /// <param name="table">The table.</param>
        /// <param name="restrictions">The restrictions.</param>
        public override void FillTable(HsqlConnection connection, 
            DataTable table, string[] restrictions)
        {
            AddRow(table, CN.MetaDataCollections, 0, 0);
            AddRow(table, CN.DataSourceInformation, 0, 0);
            AddRow(table, CN.DataTypes, 0, 0);
            AddRow(table, CN.Restrictions, 0, 0);
            AddRow(table, CN.ReservedWords, 0, 0);
            AddRow(table, HCN.Users, 1, 1);
            AddRow(table, HCN.Databases, 1, 1);
            AddRow(table, HCN.Tables, 4, 3);
            AddRow(table, HCN.Columns, 4, 4);
            AddRow(table, HCN.Views, 3, 3);
            AddRow(table, HCN.ViewColumns, 4, 4);
            AddRow(table, HCN.ProcedureParameters, 4, 4);
            AddRow(table, HCN.Procedures, 4, 3);
            AddRow(table, HCN.PrimaryKeyColumns, 5, 5);
            AddRow(table, HCN.PrimaryKeys, 4, 4);
            AddRow(table, HCN.ForeignKeyColumns, 5, 5);
            AddRow(table, HCN.ForeignKeys, 4, 4);
            AddRow(table, HCN.TableCheckConstraints, 4, 4);
            AddRow(table, HCN.TableTriggers, 4, 4);
            AddRow(table, HCN.IndexColumns, 5, 5);
            AddRow(table, HCN.Indexes, 6, 4);
            AddRow(table, HCN.Sequences, 3, 3);
            AddRow(table, HCN.UserDefinedTypes, 2, 1);
            AddRow(table, HCN.UniqueConstraints, 4, 4);
            AddRow(table, HCN.UniqueConstraintColumns, 5, 5);
            AddRow(table, HCN.Schemas, 3, 2);
        }

        #endregion

        #region AddRow(...)

        /// <summary>
        /// Adds the row.
        /// </summary>
        /// <param name="table">The table.</param>
        /// <param name="collectionName">Name of the collection.</param>
        /// <param name="numberOfRestrictions">The number of restrictions.</param>
        /// <param name="numberOfIdentifierParts">The number of identifier parts.</param>
        public void AddRow(
            DataTable table,
            string collectionName,
            int numberOfRestrictions,
            int numberOfIdentifierParts)
        {
            DataRow row = table.NewRow();

            row[MDCN.CollectionName] = collectionName;
            row[MDCN.NumberOfRestrictions] = numberOfRestrictions;
            row[MDCN.NumberOfIdentifierParts] = numberOfIdentifierParts;

            table.Rows.Add(row);
        }

        #endregion
    }

    #endregion
}