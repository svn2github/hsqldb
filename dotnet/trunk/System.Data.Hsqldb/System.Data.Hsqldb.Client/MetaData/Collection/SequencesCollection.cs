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
using System;
using System.Data;
using System.Text;
using HCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataCollectionNames;
#endregion

namespace System.Data.Hsqldb.Client.MetaData.Collection
{
    #region SequencesCollection
    
    /// <summary>
    /// <para>
    /// Provides the <see cref="HCN.Sequences"/> collection.
    /// </para>
    /// <img src="/ClassDiagrams/System.Data.Hsqldb.Client.MetaData.Collection.SequencesCollection.png"
    ///      alt="SequencesCollection Class Diagram"/>
    /// </summary>
    public class SequencesCollection : Base.MetaDataCollection
    {
        #region Constants

        private const string sql =
@"SELECT SEQUENCE_CATALOG
      ,SEQUENCE_SCHEMA
      ,SEQUENCE_NAME
      ,DTD_IDENTIFIER
      ,MAXIMUM_VALUE
      ,MINIMUM_VALUE
      ,INCREMENT
      ,CYCLE_OPTION
      ,START_WITH
  FROM INFORMATION_SCHEMA.SYSTEM_SEQUENCES
 WHERE 1=1";

        #endregion

        #region SequencesCollection(HsqlConnection)
        /// <summary>
        /// Initializes a new instance of the <see cref="SequencesCollection"/> class.
        /// </summary>
        public SequencesCollection()
            : base()
        {
        }
        #endregion

        #region CreateTable()
        /// <summary>
        /// Creates the table.
        /// </summary>
        /// <returns>The table.</returns>
        public override DataTable CreateTable()
        {
            DataTable table = new DataTable(HCN.Sequences);
            DataColumnCollection cols = table.Columns;

            AddColumn(cols, null, "SEQUENCE_CATALOG", typeof(string));
            AddColumn(cols, null, "SEQUENCE_SCHEMA", typeof(string));
            AddColumn(cols, null, "SEQUENCE_NAME", typeof(string));
            AddColumn(cols, null, "DTD_IDENTIFIER", typeof(string));
            AddColumn(cols, null, "MAXIMUM_VALUE", typeof(string));
            AddColumn(cols, null, "MINIMUM_VALUE", typeof(string));
            AddColumn(cols, null, "INCREMENT", typeof(string));
            AddColumn(cols, null, "CYCLE_OPTION", typeof(string));
            AddColumn(cols, null, "START_WITH", typeof(string));

            return table;
        }
        #endregion

        #region FillTable(DataTable)
        /// <summary>
        /// Fills the table.
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
            string sequenceNamePattern = restrictions[2];

            if (WantsIsNull(schemaNamePattern)
                || WantsIsNull(sequenceNamePattern))
            {
                return;
            }

            StringBuilder query = new StringBuilder(sql);

            query
                //.Append(And("SEQUENCE_CATALOG", "=", catalogName))
                .Append(And("SEQUENCE_SCHEMA", "LIKE", schemaNamePattern))
                .Append(And("SEQUENCE_NAME", "LIKE", sequenceNamePattern));

            using (HsqlDataReader reader = Execute(connection, query.ToString()))
            {
                object[] values = new object[reader.FieldCount];

                while (reader.Read())
                {
                    reader.GetValues(values);

                    string sequenceCatalog = (string)values[0];
                    string sequenceSchema = (string)values[1];
                    string sequenceName = (string)values[2];
                    string dtdIdentifier = (string)values[3];
                    string maximumValue = (string)values[4];
                    string minimumValue = (string)values[5];
                    string increment = (string)values[6];
                    string cycleOption = (string)values[7];
                    string startWith = (string)values[8];

                    AddRow(
                        table,
                        sequenceCatalog,
                        sequenceSchema,
                        sequenceName,
                        dtdIdentifier,
                        maximumValue,
                        minimumValue,
                        increment,
                        cycleOption,
                        startWith
                        );
                }
            }
        }
        #endregion

        #region AddRow(...)
        /// <summary>
        /// Adds the row.
        /// </summary>
        /// <param name="table">The table.</param>
        /// <param name="sequenceCatalog">The sequence catalog.</param>
        /// <param name="sequenceSchema">The sequence schema.</param>
        /// <param name="sequenceName">Name of the sequence.</param>
        /// <param name="dtdIdentifier">The DTD identifier.</param>
        /// <param name="maximumValue">The maximum value.</param>
        /// <param name="minimumValue">The minimum value.</param>
        /// <param name="increment">The increment.</param>
        /// <param name="cycleOption">The cycle option.</param>
        /// <param name="startWith">The start with value.</param>
        public static void AddRow(
            DataTable table,
            string sequenceCatalog,
            string sequenceSchema,
            string sequenceName,
            string dtdIdentifier,
            string maximumValue,
            string minimumValue,
            string increment,
            string cycleOption,
            string startWith)
        {
            DataRow row = table.NewRow();

            row["SEQUENCE_CATALOG"] = sequenceCatalog;
            row["SEQUENCE_SCHEMA"] = sequenceSchema;
            row["SEQUENCE_NAME"] = sequenceName;
            row["DTD_IDENTIFIER"] = dtdIdentifier;
            row["MAXIMUM_VALUE"] = maximumValue;
            row["MINIMUM_VALUE"] = minimumValue;
            row["INCREMENT"] = increment;
            row["CYCLE_OPTION"] = cycleOption;
            row["START_WITH"] = startWith;

            table.Rows.Add(row);
        }
        #endregion
    } 
    
    #endregion
}
