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
using System.Data.Common;
using CN = System.Data.Common.DbMetaDataCollectionNames;
using HCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataCollectionNames;
using MDCN = System.Data.Common.DbMetaDataColumnNames;
using HMDCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataColumnNames;
#endregion

namespace System.Data.Hsqldb.Client.MetaData.Collection
{
    #region RestrictionsCollection

    /// <summary>
    /// <para>
    /// Provides the <see cref="CN.Restrictions"/> collection.
    /// </para>
    /// <img src="../Documentation/ClassDiagrams/System.Data.Hsqldb.Client.MetaData.Collection.RestrictionsCollection.png"
    ///      alt="RestrictionsCollection Class Diagram"/>
    /// </summary>
    public class RestrictionsCollection : Base.CachedMetadataCollection
    {
        #region RestrictionsCollection(HsqlConnection

        /// <summary>
        /// Initializes a new instance of the <see cref="RestrictionsCollection"/> class.
        /// </summary>
        public RestrictionsCollection() : base()
        {
        }

        #endregion

        #region CreateTable()

        /// <summary>
        /// Creates the Restrictions metadata collection table.
        /// </summary>
        /// <returns></returns>
        public override DataTable CreateTable()
        {
            DataTable table = new DataTable(CN.Restrictions);

            table.MinimumCapacity = 40;

            DataColumnCollection cols = table.Columns;

            AddColumn(cols, null, MDCN.CollectionName, typeof (string));
            AddColumn(cols, null, HMDCN.RestrictionName, typeof (string));
            AddColumn(cols, null, HMDCN.ParameterName, typeof (string));
            AddColumn(cols, null, HMDCN.RestrictionDefault, typeof (string));
            AddColumn(cols, null, HMDCN.RestrictionNumber, typeof (int));

            return table;
        }

        #endregion

        #region FillTable(DataTable,string[])

        /// <summary>
        /// Fills a <c>Restrictions</c> metadata collection table.
        /// </summary>
        /// <param name="connection">The connection.</param>
        /// <param name="table">The table.</param>
        /// <param name="restrictions">The restrictions.</param>
        public override void FillTable(HsqlConnection connection,
            DataTable table, string[] restrictions)
        {
            //
            AddRow(table, HCN.Users, "User_Name", "@p1", null, 1);
            //
            AddRow(table, HCN.Databases, "Name", "@p1", null, 1);
            //
            AddRow(table, HCN.Tables, "Database", "@p1", null, 1);
            AddRow(table, HCN.Tables, "Schema", "@p2", null, 2);
            AddRow(table, HCN.Tables, "Table", "@p3", null, 3);
            AddRow(table, HCN.Tables, "TableType", "@p4", null, 4);
            //
            AddRow(table, HCN.Columns, "Database", "@p1", null, 1);
            AddRow(table, HCN.Columns, "Schema", "@p2", null, 2);
            AddRow(table, HCN.Columns, "Table", "@p3", null, 3);
            AddRow(table, HCN.Columns, "Column", "@p4", null, 4);
            //
            AddRow(table, HCN.ViewColumns, "Database", "@p1", null, 1);
            AddRow(table, HCN.ViewColumns, "Schema", "@p2", null, 2);
            AddRow(table, HCN.ViewColumns, "Table", "@p3", null, 3);
            AddRow(table, HCN.ViewColumns, "Column", "@p4", null, 4);
            //
            AddRow(table, HCN.Views, "Database", "@p1", null, 1);
            AddRow(table, HCN.Views, "Schema", "@p2", null, 2);
            AddRow(table, HCN.Views, "Table", "@p3", null, 3);
            //
            AddRow(table, HCN.ProcedureParameters, "Database", "@p1", null, 1);
            AddRow(table, HCN.ProcedureParameters, "Schema", "@p2", null, 2);
            AddRow(table, HCN.ProcedureParameters, "Name", "@p3", null, 3);
            AddRow(table, HCN.ProcedureParameters, "Parameter", "@p4", null, 4);
            //
            AddRow(table, HCN.Procedures, "Database", "@p1", null, 1);
            AddRow(table, HCN.Procedures, "Schema", "@p2", null, 2);
            AddRow(table, HCN.Procedures, "Name", "@p3", null, 3);
            AddRow(table, HCN.Procedures, "Type", "@p4", null, 4);
            //
            AddRow(table, HCN.IndexColumns, "Database", "@p1", null, 1);
            AddRow(table, HCN.IndexColumns, "Schema", "@p2", null, 2);
            AddRow(table, HCN.IndexColumns, "Table", "@p3", null, 3);
            AddRow(table, HCN.IndexColumns, "IndexName", "@p4", null, 4);
            AddRow(table, HCN.IndexColumns, "Column", "@p5", null, 5);
            //
            AddRow(table, HCN.Indexes, "Database", "@p1", null, 1);
            AddRow(table, HCN.Indexes, "Schema", "@p2", null, 2);
            AddRow(table, HCN.Indexes, "Table", "@p3", null, 3);
            AddRow(table, HCN.Indexes, "Name", "@p4", null, 4);
            AddRow(table, HCN.Indexes, "IsUnique", "@p5", null, 5);
            AddRow(table, HCN.Indexes, "IsPrimary", "@p6", null, 6);
            //
            AddRow(table, HCN.UserDefinedTypes, "assembly_name", "@p1", null, 1);
            AddRow(table, HCN.UserDefinedTypes, "udt_name", "@p2", null, 2);
            //
            AddRow(table, HCN.PrimaryKeyColumns, "Database", "@p1", null, 1);
            AddRow(table, HCN.PrimaryKeyColumns, "Schema", "@p2", null, 2);
            AddRow(table, HCN.PrimaryKeyColumns, "Table", "@p3", null, 3);
            AddRow(table, HCN.PrimaryKeyColumns, "Constraint", "@p4", null, 4);
            AddRow(table, HCN.PrimaryKeyColumns, "Column", "@p5", null, 5);
            //
            AddRow(table, HCN.PrimaryKeys, "Database", "@p1", null, 1);
            AddRow(table, HCN.PrimaryKeys, "Schema", "@p2", null, 2);
            AddRow(table, HCN.PrimaryKeys, "Table", "@p3", null, 3);
            AddRow(table, HCN.PrimaryKeys, "Name", "@p4", null, 4);
            //
            AddRow(table, HCN.ForeignKeyColumns, "Database", "@p1", null, 1);
            AddRow(table, HCN.ForeignKeyColumns, "Schema", "@p2", null, 2);
            AddRow(table, HCN.ForeignKeyColumns, "Table", "@p3", null, 3);
            AddRow(table, HCN.ForeignKeyColumns, "Constraint", "@p4", null, 4);
            AddRow(table, HCN.ForeignKeyColumns, "Column", "@p5", null, 5);
            //
            AddRow(table, HCN.ForeignKeys, "Database", "@p1", null, 1);
            AddRow(table, HCN.ForeignKeys, "Schema", "@p2", null, 2);
            AddRow(table, HCN.ForeignKeys, "Table", "@p3", null, 3);
            AddRow(table, HCN.ForeignKeys, "Name", "@p4", null, 4);
            //
            AddRow(table, HCN.TableCheckConstraints, "Database", "@p1", null, 1);
            AddRow(table, HCN.TableCheckConstraints, "Schema", "@p2", null, 2);
            AddRow(table, HCN.TableCheckConstraints, "Table", "@p3", null, 3);
            AddRow(table, HCN.TableCheckConstraints, "Name", "@p4", null, 4);
            //
            AddRow(table, HCN.TableTriggers, "Database", "@p1", null, 1);
            AddRow(table, HCN.TableTriggers, "Schema", "@p2", null, 2);
            AddRow(table, HCN.TableTriggers, "Table", "@p3", null, 3);
            AddRow(table, HCN.TableTriggers, "Name", "@p4", null, 4);
            //
            AddRow(table, HCN.Sequences, "Database", "@p1", null, 1);
            AddRow(table, HCN.Sequences, "Schema", "@p2", null, 2);
            AddRow(table, HCN.Sequences, "Name", "@p3", null, 3);
            //
            AddRow(table, HCN.UniqueConstraints, "Database", "@p1", null, 1);
            AddRow(table, HCN.UniqueConstraints, "Schema", "@p2", null, 2);
            AddRow(table, HCN.UniqueConstraints, "Table", "@p3", null, 3);
            AddRow(table, HCN.UniqueConstraints, "Name", "@p4", null, 4);

            //
            AddRow(table, HCN.UniqueConstraintColumns, "Database", "@p1", null, 1);
            AddRow(table, HCN.UniqueConstraintColumns, "Schema", "@p2", null, 2);
            AddRow(table, HCN.UniqueConstraintColumns, "Table", "@p3", null, 3);
            AddRow(table, HCN.UniqueConstraintColumns, "Constraint", "@p4", null, 4);
            AddRow(table, HCN.UniqueConstraintColumns, "Column", "@p5", null, 5);

            //
            AddRow(table, HCN.Schemas, "Database", "@p1", null, 1);
            AddRow(table, HCN.Schemas, "Name", "@p2", null, 2);
            AddRow(table, HCN.Schemas, "Owner", "@p3", null, 3);
        }

        #endregion

        #region AddRow(...)

        /// <summary>
        /// Adds a row to a <c>Restrictions</c> metadata collection table.
        /// </summary>
        /// <param name="table">The table.</param>
        /// <param name="collectionName">Name of the collection.</param>
        /// <param name="restrictionName">Name of the restriction.</param>
        /// <param name="parameterName">Name of the parameter.</param>
        /// <param name="restrictionDefault">The restriction default.</param>
        /// <param name="restrictionNumber">The restriction number.</param>
        public static void AddRow(
            DataTable table,
            string collectionName,
            string restrictionName,
            string parameterName,
            string restrictionDefault,
            int restrictionNumber)
        {
            DataRow row = table.NewRow();

            row[MDCN.CollectionName] = collectionName;
            row[HMDCN.RestrictionName] = restrictionName;
            row[HMDCN.ParameterName] = parameterName;
            row[HMDCN.RestrictionDefault] = restrictionDefault;
            row[HMDCN.RestrictionNumber] = restrictionNumber;

            table.Rows.Add(row);
        }

        #endregion
    }

    #endregion
}