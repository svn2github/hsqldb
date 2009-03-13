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
using System.Data.Common;
using CN = System.Data.Common.DbMetaDataCollectionNames;
using HCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataCollectionNames;
using MDCN = System.Data.Common.DbMetaDataColumnNames;
using HMDCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataColumnNames;
using System.Data.Hsqldb.Common;
#endregion

namespace System.Data.Hsqldb.Client.MetaData.Collection
{
    #region DataTypesCollection
    /// <summary>
    /// <para>
    /// Provides the <see cref="CN.DataTypes"/> collection.
    /// </para>
    /// <img src="../Documentation/ClassDiagrams/System.Data.Hsqldb.Client.MetaData.Collection.DataTypesCollection.png"
    ///      alt="DataTypesCollection Class Diagram"/>
    /// </summary>
    /// <author name="boucherb@users"/>
    public class DataTypesCollection : Base.CachedMetadataCollection
    {
        #region Constants

        private const string sql =
@"select type_name
       ,data_type
       ,precision
       ,literal_prefix
       ,literal_suffix
       ,create_params
       ,nullable
       ,case_sensitive
       ,searchable
       ,unsigned_attribute
       ,fixed_prec_scale
       ,auto_increment
       ,local_type_name
       ,minimum_scale
       ,maximum_scale
       ,sql_data_type
       ,sql_datetime_sub
       ,num_prec_radix
       ,type_sub
   from information_schema.system_typeinfo";

        #endregion

        #region DataTypesCollection(DatabaseMetaData)
        /// <summary>
        /// Initializes a new instance of the <see cref="DataTypesCollection"/> class.
        /// </summary>
        public DataTypesCollection() : base() { }
        #endregion

        #region CreateTable()
        /// <summary>
        /// Creates a <c>DataTypes</c> metadata collection table.
        /// </summary>
        /// <returns>The <c>DataTable</c>.</returns>
        public override DataTable CreateTable()
        {
            DataTable table = new DataTable(CN.DataTypes);
            DataColumnCollection cols = table.Columns;

            AddColumn(cols, null, MDCN.TypeName, typeof (string));
            AddColumn(cols, null, MDCN.ProviderDbType, typeof (int));
            AddColumn(cols, null, MDCN.ColumnSize, typeof (long));
            AddColumn(cols, null, MDCN.CreateFormat, typeof (string));
            AddColumn(cols, null, MDCN.CreateParameters, typeof (string));
            AddColumn(cols, null, MDCN.DataType, typeof (string));
            AddColumn(cols, null, MDCN.IsAutoIncrementable, typeof (bool));
            AddColumn(cols, null, MDCN.IsBestMatch, typeof (bool));
            AddColumn(cols, null, MDCN.IsCaseSensitive, typeof (bool));
            AddColumn(cols, null, MDCN.IsFixedLength, typeof (bool));
            AddColumn(cols, null, MDCN.IsFixedPrecisionScale, typeof (bool));
            AddColumn(cols, null, MDCN.IsLong, typeof (bool));
            AddColumn(cols, null, MDCN.IsNullable, typeof (bool));
            AddColumn(cols, null, MDCN.IsSearchable, typeof (bool));
            AddColumn(cols, null, MDCN.IsSearchableWithLike, typeof (bool));
            AddColumn(cols, null, MDCN.IsUnsigned, typeof (bool));
            AddColumn(cols, null, MDCN.MaximumScale, typeof (short));
            AddColumn(cols, null, MDCN.MinimumScale, typeof (short));
            AddColumn(cols, null, MDCN.IsConcurrencyType, typeof (bool));
            AddColumn(cols, null, MDCN.IsLiteralSupported, typeof (bool));
            AddColumn(cols, null, MDCN.LiteralPrefix, typeof (string));
            AddColumn(cols, null, MDCN.LiteralSuffix, typeof (string));

            return table;
        }
        #endregion

        #region FillTable(DataTable,string[])
        /// <summary>
        /// Fills a DataTypes metadata collection table.
        /// </summary>
        /// <param name="connection">The connection.</param>
        /// <param name="table">The table.</param>
        /// <param name="restrictions">The restrictions.</param>
        public override void FillTable(HsqlConnection connection,
            DataTable table, string[] restrictions)
        {
            using (HsqlDataReader reader = Execute(connection, sql))
            {
                object[] values = new object[reader.FieldCount];

                while(reader.Read())
                {
                    reader.GetValues(values);

                    string typeName = (string) values[0];
                    int jdbcType = (int) values[1];
                    int? columnSize = (int?) values[2];
                    string literalPrefix = (string) values[3];
                    string literalSuffix = (string)values[4];
                    string createParameters = (string)values[5];
                    short nullability = (short)(int)values[6];
                    bool? isCaseSensitive = (bool?)values[7];
                    short searchability = (short)(int)values[8];
                    bool? isUnsigned = (bool?)values[9];
                    bool? isFixedPrecisionScale = (bool?)values[10];
                    bool? isAutoIncrementable = (bool?)values[11];
                    //string localTypeName = (string) values[12];
                    short? minimumScale = (short?)(int?)values[13];
                    short? maximumScale = (short?)(int?)values[14];
                    //int? sqlDataType = (int?) values[15];
                    //int? dateTimeSub = (int?)values[16];
                    //int? numPrecRadix = (int?)values[17];
                    //int? typeSub = (int?)values[18];

                    string createFormat = ToCreateFormat(typeName, createParameters);
                    string dataType = Convert.ToString(HsqlConvert.ToDataType(jdbcType));
                    bool isBestMatch = IsBestMatch(typeName);
                    bool isFixedLength = IsFixedLength(jdbcType);
                    bool isLong = IsLong(jdbcType);
                    bool isNullable = IsNullable(nullability);
                    bool isSearchable = IsSearchable(searchability);
                    bool isSearchableWithLike = IsSearchableWithLike(searchability);
                    bool isConcurrencyType = false;
                    bool isLiteralSupported = true;

                    AddRow(table,
                           typeName, jdbcType,
                           columnSize,
                           createFormat, createParameters,
                           dataType, isAutoIncrementable, isBestMatch,
                           isCaseSensitive, isFixedLength, isFixedPrecisionScale, isLong,
                           isNullable, isSearchable, isSearchableWithLike, isUnsigned,
                           maximumScale, minimumScale, isConcurrencyType,
                           isLiteralSupported, literalPrefix, literalSuffix);
                }
            }
        }
        #endregion

        #region AddRow(...)
        /// <summary>
        /// Adds the row.
        /// </summary>
        /// <param name="table">
        /// The table to fill.
        /// </param>
        /// <param name="typeName">
        /// The SQL data type name.
        /// </param>
        /// <param name="jdbcType">
        /// The JDBC data type code.
        /// </param>
        /// <param name="columnSize">
        /// The maximum size or precision of the SQL data type.
        /// </param>
        /// <param name="createFormat">
        /// The data type's SQL create format.
        /// </param>
        /// <param name="createParameters">
        /// The data type's SQL create parameters.
        /// </param>
        /// <param name="dataType">
        /// The System.Type used by the driver to represent values of the given SQL data type.
        /// </param>
        /// <param name="isAutoIncrementable">
        /// When <c>true</c>, the SQL data type is auto-incrementable.
        /// </param>
        /// <param name="isBestMatch">
        /// When <c>true</c>, the SQL data type name is the best match (i.e. the canonical
        /// SQL data type name) corresponding to given jdbc type.
        /// </param>
        /// <param name="isCaseSensitive">
        /// When <c>true</c>, the SQL data type is case sensitive.
        /// </param>
        /// <param name="isFixedLength">
        /// When <c>true</c>, the SQL data type is fixed length.
        /// </param>
        /// <param name="isFixedPrecisionScale">
        /// When <c>true</c> the SQL data type is fixed precision scale.
        /// </param>
        /// <param name="isLong">
        /// When <c>true</c>, the SQL data type is the is a long variant
        /// some corresponding base data type.
        /// </param>
        /// <param name="isNullable">
        /// When <c>true</c> the SQL data type is nullable.
        /// </param>
        /// <param name="isSearchable">
        /// When <c>true</c>, the SQL data type is searchable in some way.
        /// </param>
        /// <param name="isSearchableWithLike">
        /// When <c>true</c> the SQL data type is searchable using the
        /// SQL LIKE predicate.
        /// </param>
        /// <param name="isUnsigned">
        /// When <c>true</c>, the values of the SQL data type are unsigned.
        /// </param>
        /// <param name="maximumScale">
        /// The SQL data type's maximum scale.
        /// </param>
        /// <param name="minimumScale">
        /// The SQL data type's minimum scale.
        /// </param>
        /// <param name="isConcurrencyType">
        /// When <c>true</c>, the SQL data type's role is that of a concurrency type.
        /// </param>
        /// <param name="isLiteralSupported">
        /// When <c>true</c>, it is possible express values of the SQL data type
        /// in literal form.
        /// </param>
        /// <param name="literalPrefix">
        /// The prefix character(s) used to express literal values of the SQL data type.
        /// </param>
        /// <param name="literalSuffix">
        /// The suffix character(s) used to express literal values of the SQL data type.
        /// </param>
        public static void AddRow(
            DataTable table,
            string typeName,
            int jdbcType,
            long? columnSize,
            string createFormat,
            string createParameters,
            string dataType,
            bool? isAutoIncrementable,
            bool isBestMatch,
            bool? isCaseSensitive,
            bool isFixedLength,
            bool? isFixedPrecisionScale,
            bool isLong,
            bool isNullable,
            bool isSearchable,
            bool isSearchableWithLike,
            bool? isUnsigned,
            short? maximumScale,
            short? minimumScale,
            bool isConcurrencyType,
            bool isLiteralSupported,
            string literalPrefix,
            string literalSuffix)
        {
            DataRow row = table.NewRow();

            row[MDCN.TypeName] = typeName;
            row[MDCN.ProviderDbType] = jdbcType;
            if (columnSize != null)
            {
                row[MDCN.ColumnSize] = columnSize;
            }
            row[MDCN.CreateFormat] = createFormat;
            row[MDCN.CreateParameters] = createParameters;
            row[MDCN.DataType] = dataType;
            if (isAutoIncrementable != null)
            {
                row[MDCN.IsAutoIncrementable] = isAutoIncrementable;
            }
            row[MDCN.IsBestMatch] = isBestMatch;
            if (isCaseSensitive != null)
            {
                row[MDCN.IsCaseSensitive] = isCaseSensitive;
            }
            row[MDCN.IsFixedLength] = isFixedLength;
            if (isFixedPrecisionScale != null)
            {
                row[MDCN.IsFixedPrecisionScale] = isFixedPrecisionScale;
            }
            row[MDCN.IsLong] = isLong;
            row[MDCN.IsNullable] = isNullable;
            row[MDCN.IsSearchable] = isSearchable;
            row[MDCN.IsSearchableWithLike] = isSearchableWithLike;
            if (isUnsigned != null)
            {
                row[MDCN.IsUnsigned] = isUnsigned;
            }
            if (maximumScale != null)
            {
                row[MDCN.MaximumScale] = maximumScale;
            }
            if (minimumScale != null)
            {
                row[MDCN.MinimumScale] = minimumScale;
            }
            row[MDCN.IsConcurrencyType] = isConcurrencyType;
            row[MDCN.IsLiteralSupported] = isLiteralSupported;
            row[MDCN.LiteralPrefix] = literalPrefix;
            row[MDCN.LiteralSuffix] = literalSuffix;

            table.Rows.Add(row);
        }
        #endregion
    }
    #endregion
}