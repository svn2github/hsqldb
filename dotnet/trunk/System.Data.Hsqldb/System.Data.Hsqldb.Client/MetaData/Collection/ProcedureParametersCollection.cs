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
    #region ProcedureParametersCollection

    /// <summary>
    /// <para>
    /// Provides the <see cref="HCN.ProcedureParameters"/> collection.
    /// </para>
    /// <img src="/ClassDiagrams/System.Data.Hsqldb.Client.MetaData.Collection.ProcedureParametersCollection.png"
    ///      alt="ProcedureParametersCollection Class Diagram"/>
    /// </summary>
    public class ProcedureParametersCollection : Base.MetaDataCollection
    {
        #region Constants

        private const string sql =
@"SELECT distinct
         spc.procedure_cat AS SPECIFIC_CATALOG
        ,spc.procedure_schem AS SPECIFIC_SCHEMA
        ,spc.specific_name AS SPECIFIC_NAME
        ,CASE spc.column_type
              WHEN 3 THEN 0
              ELSE spc.seq
         END AS ORDINAL_POSITION
        ,CASE spc.column_type
              WHEN 1 THEN 'IN'
              WHEN 2 THEN 'IN OUT'
              WHEN 3 THEN 'RESULT'
              WHEN 4 THEN 'OUT'
              WHEN 5 THEN 'RETURN'
              ELSE 'UNKNOWN'
         END AS PARAMETER_MODE
        ,CASE spc.column_type
              WHEN 3 THEN 'YES'
              ELSE 'NO'
         END AS IS_RESULT
        ,'NO' AS AS_LOCATOR
        ,spc.column_name AS PARAMETER_NAME
        ,spc.type_name AS DATA_TYPE
        ,CASE WHEN (spc.data_type = -1) THEN CAST(1073741823 AS INTEGER)
              WHEN (spc.data_type IN(1,12,2005))
              THEN CASE WHEN (spc.length IS NULL) THEN CAST(1073741823 AS INTEGER)
                        ELSE CAST(spc.length AS INTEGER)
                   END
              ELSE NULL
         END AS CHARACTER_MAXIMUM_LENGTH
        ,CASE WHEN (spc.data_type = -1) THEN CAST(2147483647 AS INTEGER)
              WHEN (spc.data_type IN(1,12,2005))
              THEN CASE WHEN (spc.length IS NULL) THEN CAST(2147483647 AS INTEGER)
                        WHEN (spc.length > 1073741823) THEN NULL
                        ELSE CAST((spc.length * 2) AS INTEGER)
                   END
              ELSE NULL
         END AS CHARACTER_OCTET_LENGTH
        ,NULL AS COLLATION_CATALOG
        ,CASE WHEN (spc.data_type IN(-1,1,12,2005))
              THEN 'INFORMATION_SCHEMA'
              ELSE NULL
         END AS COLLATION_SCHEMA
        ,CASE WHEN (spc.data_type IN(-1,1,12,2005))
              THEN 'UCS_BASIC'
              ELSE NULL
         END AS COLLATION_NAME
        ,NULL AS CHARACTER_SET_CATALOG
        ,CASE WHEN (spc.data_type IN(-1,1,12,2005))
              THEN 'INFORMATION_SCHEMA'
              ELSE NULL
         END AS CHARATER_SET_SCHEMA
        ,CASE WHEN (spc.data_type IN(-1,1,12,2005))
              THEN 'UTF-16'
              ELSE NULL
         END AS CHARATER_SET_NAME
        ,CASE WHEN (spc.data_type IN(-6,-5,2,3,4,5,6,7,8))
              THEN spc.precision
              ELSE NULL
         END AS NUMERIC_PRECISION
        ,CASE spc.radix
              WHEN 2 THEN 10 -- kludge
              ELSE spc.radix
         END AS NUMERIC_PRECISION_RADIX
        ,spc.scale AS NUMERIC_SCALE
        ,CASE spc.data_type
              WHEN 91 THEN 0
              WHEN 92 THEN 0
              WHEN 93 THEN 6
              ELSE NULL
         END AS DATETIME_PRECSISION
        ,NULL AS INTERVAL_TYPE
        ,NULL AS INTERVAL_PRECISION
        ,CASE spc.column_type
              WHEN 1 THEN 'IN'
              WHEN 2 THEN 'IN/OUT'
              WHEN 3 THEN 'RETVAL'
              WHEN 4 THEN 'OUT'
              WHEN 5 THEN 'RETVAL'
              ELSE 'UNKNOWN'
         END AS PARAMETER_DIRECTION
   FROM information_schema.system_procedurecolumns spc
  WHERE 1=1";

        #endregion

        #region ProcedureParametersCollection(HsqlConnection)

        /// <summary>
        /// Initializes a new instance of the
        /// <see cref="ProcedureParametersCollection"/> class.
        /// </summary>
        public ProcedureParametersCollection() : base() { }

        #endregion

        #region CreateTable()

        /// <summary>
        /// Creates the procedure parameters table.
        /// </summary>
        /// <returns></returns>
        public override DataTable CreateTable()
        {
            DataTable table = new DataTable(HCN.ProcedureParameters);
            DataColumnCollection cols = table.Columns;

            AddColumn(cols, null, "SPECIFIC_CATALOG", typeof (string));
            AddColumn(cols, null, "SPECIFIC_SCHEMA", typeof (string));
            AddColumn(cols, null, "SPECIFIC_NAME", typeof (string));
            AddColumn(cols, null, "ORDINAL_POSITION", typeof (int));
            AddColumn(cols, null, "PARAMETER_MODE", typeof (string));
            AddColumn(cols, null, "IS_RESULT", typeof (string));
            AddColumn(cols, null, "AS_LOCATOR", typeof (string));
            AddColumn(cols, null, "PARAMETER_NAME", typeof (string));
            AddColumn(cols, null, "DATA_TYPE", typeof (string));
            AddColumn(cols, null, "CHARACTER_MAXIMUM_LENGTH", typeof (int));
            AddColumn(cols, null, "CHARACTER_OCTET_LENGTH", typeof (int));
            AddColumn(cols, null, "COLLATION_CATALOG", typeof (string));
            AddColumn(cols, null, "COLLATION_SCHEMA", typeof (string));
            AddColumn(cols, null, "COLLATION_NAME", typeof (string));
            AddColumn(cols, null, "CHARACTER_SET_CATALOG", typeof (string));
            AddColumn(cols, null, "CHARACTER_SET_SCHEMA", typeof (string));
            AddColumn(cols, null, "CHARACTER_SET_NAME", typeof (string));
            AddColumn(cols, null, "NUMERIC_PRECISION", typeof (int));
            AddColumn(cols, null, "NUMERIC_PRECISION_RADIX", typeof (short));
            AddColumn(cols, null, "NUMERIC_SCALE", typeof (int));
            AddColumn(cols, null, "DATETIME_PRECISION", typeof (short));
            AddColumn(cols, null, "INTERVAL_TYPE", typeof (string));
            AddColumn(cols, null, "INTERVAL_PRECISION", typeof (short));
            AddColumn(cols, null, "PARAMETER_DIRECTION", typeof(string));

            return table;
        }

        #endregion

        #region FillTable(DataTable,string[])

        /// <summary>
        /// Fills the procedure parameters table.
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
            string columnNamePattern = restrictions[3];

            if (WantsIsNull(specificSchemaPattern)
                || WantsIsNull(specificNamePattern)
                || WantsIsNull(columnNamePattern))
            {
                return;
            }

            StringBuilder query = new StringBuilder(sql);

            query
                //.Append(And("SPECIFIC_CATALOG", "=", catalogName))
                .Append(And("SPECIFIC_SCHEMA", "LIKE", specificSchemaPattern))
                .Append(And("SPECIFIC_NAME", "LIKE", specificNamePattern))
                .Append(And("COLUMN_NAME", "LIKE", columnNamePattern));

            using (HsqlDataReader reader = Execute(connection, query.ToString()))
            {
                object[] values = new object[reader.FieldCount];

                while (reader.Read())
                {
                    reader.GetValues(values);

                    string specificCatalog = (string) values[0];
                    string specificSchema = (string)values[1];
                    string specificName = (string)values[2];
                    int ordinalPosition = (int)values[3];
                    string parameterMode = (string)values[4];
                    string isResult = (string)values[5];
                    string asLocator = (string)values[6];
                    string parameterName = (string)values[7];
                    string dataType = (string)values[8];
                    int? characterMaximumLength = (int?)values[9];
                    int? characterOctetLength = (int?)values[10];
                    string collationCatalog = (string)values[11];
                    string collationSchema = (string)values[12];
                    string collationName = (string)values[13];
                    string characterSetCatalog = (string)values[14];
                    string characterSetSchema = (string)values[15];
                    string characterSetName = (string)values[16];
                    int? numericPrecision = (int?)values[17];
                    short? numericPrecisionRadix = (short?)(int?) values[18];
                    int? numericScale = (int?)values[19];
                    short? datetimePrecision = (short?)(int?)values[20];
                    string intervalType = (string)values[21];
                    short? intervalPrecision = (short?)(int?)values[22];
                    string parameterDirection = (string) values[23];

                    AddRow(
                        table,
                        specificCatalog,
                        specificSchema,
                        specificName,
                        ordinalPosition,
                        parameterMode,
                        isResult,
                        asLocator,
                        parameterName,
                        dataType,
                        characterMaximumLength,
                        characterOctetLength,
                        collationCatalog,
                        collationSchema,
                        collationName,
                        characterSetCatalog,
                        characterSetSchema,
                        characterSetName,
                        numericPrecision,
                        numericPrecisionRadix,
                        numericScale,
                        datetimePrecision,
                        intervalType,
                        intervalPrecision,
                        parameterDirection);
                }
            }
        }

        #endregion

        #region AddRow(...)

        /// <summary>
        /// Adds the procedure parameters row.
        /// </summary>
        /// <param name="table">The table.</param>
        /// <param name="specificCatalog">The specific catalog.</param>
        /// <param name="specificSchema">The specific schema.</param>
        /// <param name="specificName">Name of the specific.</param>
        /// <param name="ordinalPosition">The ordinal position.</param>
        /// <param name="parameterMode">The parameter mode.</param>
        /// <param name="isResult">The is result.</param>
        /// <param name="asLocator">As locator.</param>
        /// <param name="parameterName">Name of the parameter.</param>
        /// <param name="dataType">Type of the data.</param>
        /// <param name="characterMaximumLength">Maximum length of the character.</param>
        /// <param name="characterOctetLength">Length of the character octet.</param>
        /// <param name="collationCatalog">The collation catalog.</param>
        /// <param name="collationSchema">The collation schema.</param>
        /// <param name="collationName">Name of the collation.</param>
        /// <param name="characterSetCatalog">The character set catalog.</param>
        /// <param name="characterSetSchema">The character set schema.</param>
        /// <param name="characterSetName">Name of the character set.</param>
        /// <param name="numericPrecision">The numeric precision.</param>
        /// <param name="numericPrecisionRadix">The numeric precision radix.</param>
        /// <param name="numericScale">The numeric scale.</param>
        /// <param name="datetimePrecision">The datetime precision.</param>
        /// <param name="intervalType">Type of the interval.</param>
        /// <param name="intervalPrecision">The interval precision.</param>
        /// <param name="parameterDirection">The parameter direction.</param>
        public static void AddRow(
            DataTable table,
            string specificCatalog,
            string specificSchema,
            string specificName,
            int ordinalPosition,
            string parameterMode,
            string isResult,
            string asLocator,
            string parameterName,
            string dataType,
            int? characterMaximumLength,
            int? characterOctetLength,
            string collationCatalog,
            string collationSchema,
            string collationName,
            string characterSetCatalog,
            string characterSetSchema,
            string characterSetName,
            int? numericPrecision,
            short? numericPrecisionRadix,
            int? numericScale,
            short? datetimePrecision,
            string intervalType,
            short? intervalPrecision,
            string parameterDirection)
        {
            DataRow row = table.NewRow();

            row["SPECIFIC_CATALOG"] = specificCatalog;
            row["SPECIFIC_SCHEMA"] = specificSchema;
            row["SPECIFIC_NAME"] = specificName;
            row["ORDINAL_POSITION"] = ordinalPosition;
            row["PARAMETER_MODE"] = parameterMode;
            row["IS_RESULT"] = isResult;
            row["AS_LOCATOR"] = asLocator;
            row["PARAMETER_NAME"] = parameterName;
            row["DATA_TYPE"] = dataType;
            if (characterMaximumLength != null)
            {
                row["CHARACTER_MAXIMUM_LENGTH"] = characterMaximumLength;
            }
            if (characterOctetLength != null)
            {
                row["CHARACTER_OCTET_LENGTH"] = characterOctetLength;
            }
            row["COLLATION_CATALOG"] = collationCatalog;
            row["COLLATION_SCHEMA"] = collationSchema;
            row["COLLATION_NAME"] = collationName;
            row["CHARACTER_SET_CATALOG"] = characterSetCatalog;
            row["CHARACTER_SET_SCHEMA"] = characterSetSchema;
            row["CHARACTER_SET_NAME"] = characterSetName;
            if (numericPrecision != null)
            {
                row["NUMERIC_PRECISION"] = numericPrecision;
            }
            if(numericPrecisionRadix != null)
            {
                row["NUMERIC_PRECISION_RADIX"] = numericPrecisionRadix;
            }
            if(numericScale != null)
            {
                row["NUMERIC_SCALE"] = numericScale;
            }
            if(datetimePrecision != null)
            {
                row["DATETIME_PRECISION"] = datetimePrecision;
            }
            row["INTERVAL_TYPE"] = intervalType;
            if (intervalPrecision != null)
            {
                row["INTERVAL_PRECISION"] = intervalPrecision;
            }
            row["PARAMETER_DIRECTION"] = parameterDirection;

            table.Rows.Add(row);
        }

        #endregion
    }

    #endregion
}