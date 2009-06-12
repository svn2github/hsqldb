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
using System.Data.Hsqldb.Client.MetaData;
using StringBuilder = System.Text.StringBuilder;
using CN = System.Data.Common.DbMetaDataCollectionNames;
using HCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataCollectionNames;
using MDCN = System.Data.Common.DbMetaDataColumnNames;
using HMDCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataColumnNames;

#endregion

namespace System.Data.Hsqldb.Client.MetaData.Collection
{
    #region ColumnsCollection
    /// <summary>
    /// <para>
    /// Provides the <see cref="HCN.Columns"/> metadata collection.
    /// </para>
    /// <img src="../Documentation/ClassDiagrams/System.Data.Hsqldb.Client.MetaData.Collection.ColumnsCollection.png"
    ///      alt="ColumnsCollection Class Diagram"/>
    /// </summary>
    /// <remarks>
    /// <para>
    /// Identifies the columns of tables defined in the database that are accessible
    /// within the context of the SQL user and enabled roles associated with the
    /// connecton to the data source:
    /// </para>
    /// <para>
    ///<list type="table">
    ///   <listheader>
    ///     <term>Column</term>
    ///     <description>Description</description>
    ///   </listheader>
    ///     <item><term>TABLE_CATALOG</term><description>Uri of the database containing the table</description></item>
    ///     <item><term>TABLE_SCHEMA</term><description>Unqualified name of schema containing the table</description></item>
    ///     <item><term>TABLE_NAME</term><description>Unqualified name of table containing the column</description></item>
    ///     <item><term>COLUMN_NAME</term><description>Unqualified name of column being described</description></item>
    ///     <item><term>ORDINAL_POSITION</term><description>One-based position of column within the table</description></item>
    ///     <item><term>COLUMN_DEFAULT</term><description>The default value assigned to the column when there is no explicit assigment</description></item>
    ///     <item><term>IS_NULLABLE</term><description>Whether the column accepts the null value: {'YES' | 'NO'}</description></item>
    ///     <item><term>DATA_TYPE</term><description>Name of the column's data type (possibly fully qualified), as known to the database</description></item>
    ///     <item><term>CHARACTER_MAXIMUM_LENGTH</term><description>Length or maximum length in characters; null if not applicable to the data type</description></item>
    ///     <item><term>CHARACTER_OCTET_LENGTH</term><description>Length or maximum length in octets; null if not applicable to the data type</description></item>
    ///     <item><term>NUMERIC_PRECISION</term><description>Precision of the numeric type; null if not applicable to the data type</description></item>
    ///     <item><term>NUMERIC_PRECISION_RADIX</term><description>Radix of the precision of the numeric type; null if not applicable to the data type</description></item>
    ///     <item><term>NUMERIC_SCALE</term><description>Scale of the numeric type; null if not applicable to the data type</description></item>
    ///     <item><term>DATETIME_PRECISION</term><description>Fractional seconds precision of the datetime or interval type; null if not applicable to the data type</description></item>
    ///     <item><term>INTERVAL_TYPE</term><description>Value for &lt;interval qualifier&gt; from table 27 of ISO/IEC 9075-2; null if not applicable to the data type</description></item>
    ///     <item><term>INTERVAL_PRECISION</term><description>Leading field precision of the interval type; null if not applicable to the data type</description></item>
    ///     <item><term>CHARACTER_SET_CATALOG</term><description>Uri of the database containing the character set; null if not applicable to the data type, which must be a character type</description></item>
    ///     <item><term>CHARACTER_SET_SCHEMA</term><description>Unqualified name of schema containing the character set; null if not applicable to the data type, which must be a character type</description></item>
    ///     <item><term>CHARACTER_SET_NAME</term><description>Unqualified name of the character set; null if not applicable to the data type, which must be a character type</description></item>
    ///     <item><term>COLLATION_CATALOG</term><description>Uri of the database containing the character collation; null if not applicable to the data type, which must be a character type</description></item>
    ///     <item><term>COLLATION_SCHEMA</term><description>Unqualified name of schema containing the character collation; null if not applicable to the data type, which must be a character type</description></item>
    ///     <item><term>COLLATION_NAME</term><description>Unqualified name of the character collation; null if not applicable to the data type, which must be a character type</description></item>
    ///     <item><term>DOMAIN_CATALOG</term><description>Uri of the database containing the domain data type; null if column is not a domain data type</description></item>
    ///     <item><term>DOMAIN_SCHEMA</term><description>Unqualified name of schema containing the domain data type; null if column is not a domain data type</description></item>
    ///     <item><term>DOMAIN_NAME</term><description>Unqualified name of the domain data type; null if column is not a domain data type</description></item>
    ///     <item><term>UDT_CATALOG</term><description>Uri of the database containing the user defined data type; null if column is not a user defined data type</description></item>
    ///     <item><term>UDT_SCHEMA</term><description>Unqualified name of schema containing the user defined data type; null if column is not a user defined data type</description></item>
    ///     <item><term>UDT_NAME</term><description>Unqualified name of the user defined data type; null if column is not a user defined data type</description></item>
    ///     <item><term>SCOPE_CATALOG</term><description>Uri of the database containing the referenceable table; null if column is not a REF type</description></item>
    ///     <item><term>SCOPE_SCHEMA</term><description>Unqualified name of schema containing the referenceable table; null if column is not a REF type</description></item>
    ///     <item><term>SCOPE_NAME</term><description>Unqualified name of the referenceable table; null if column is not a REF type</description></item>
    ///     <item><term>MAXIMUM_CARDINALITY</term><description>the maximum cardinality of the array type being described. null if the column is not an ARRAY type</description></item>
    ///     <item><term>DTD_IDENTIFIER</term><description>implementation-dependent value that uniquely identifies the data type descriptor among all data type descriptors</description></item>
    ///     <item><term>IS_SELF_REFERENCING</term><description>Denotes whether the value stored in the column uniquely identifies the containing row in the entire database: {'YES' | 'NO'}</description></item>
    ///     <item><term>IS_IDENTITY</term><description>Denotes whether the column is an identity column: {'YES' | 'NO'}</description></item>
    ///     <item><term>IDENTITY_GENERATION</term><description>Describes how identity value are generated: {'ALWAYS' | 'BY DEFAULT' | NULL (if not an identity column)}</description></item>
    ///     <item><term>IDENTITY_START</term><description>Character representation of the start value of the column; null not an identity column</description></item>
    ///     <item><term>IDENTITY_INCREMENT</term><description>Character representation of the increment value of the column; null not an identity column</description></item>
    ///     <item><term>IDENTITY_MAXIMUM</term><description>Character representation of the maximum value of the column; null not an identity column</description></item>
    ///     <item><term>IDENTITY_MINIMUM</term><description>Character representation of the minimum value of the column; null not an identity column</description></item>
    ///     <item><term>IDENTITY_CYCLE</term><description>Denotes whether modular arithemtic is used to generate the identity value from the increment, maximum and minimum: {'YES' | 'NO' | NULL (if not an identity column)}</description></item>
    ///     <item><term>IS_GENERATED</term><description>Denotes whether column value is generated from a declared expression: {'ALWAYS', 'NEVER'}</description></item>
    ///     <item><term>GENERATION_EXPRESSION</term><description>Text of the &lt;generation expression&gt; specified in the column definition; null if IS_GENERATED is 'NEVER'</description></item>
    ///     <item><term>IS_UPDATABLE</term><description>Denotes whether the column is updateable: {'YES', 'NO'}</description></item>
    ///     <item><term>DECLARED_DATA_TYPE</term><description>Textual value of the &lt;data type&gt; specified in the column definition, which may be different from the effective &lt;data type&gt; of the column</description></item>
    ///     <item><term>DECLARED_NUMERIC_PRECISION</term><description>Integral value of the &lt;numeric precision&gt; specified in the column definition, which may be different from the effective &lt;numeric precision&gt; of the column; null if not applicable to the data type</description></item>
    ///     <item><term>DECLARED_NUMERIC_SCALE</term><description>Integral value of the &lt;numeric scale&gt; specified in the column definition, which may be different from the effective &lt;numeric scale&gt; of the column; null if not applicable to the data type</description></item>
    ///     <item><term>PRIMARY_KEY</term><description>boolean value that denotes whether the column belongs to the primary key of the table</description></item>
    /// </list>
    /// </para>
    /// </remarks>
    /// <author name="boucherb@users"/>
    public sealed class ColumnsCollection : Base.MetaDataCollection
    {
        #region Constants

        private const string sql =
@"-- System.Data.Hsqldb.Client.MetaData.Collection.ColumnsCollection
 select sc.table_cat as table_catalog
       ,sc.table_schem as table_schema
       ,sc.table_name
       ,sc.column_name
       ,sc.ordinal_position
       ,sc.column_def as column_default
       ,sc.is_nullable
       ,sc.type_name as data_type
       ,case when sc.data_type in (-1,1,12,2005)
             then sc.column_size
             else null
        end as character_maximum_length
       ,case when sc.data_type in (-1,1,12,2005)
             then sc.char_octet_length
             else null
        end as character_octet_length
       ,case sc.data_type
            when -6 then 3
            when -5 then 19
            when 2 then (case when sc.column_size > 0
                              then sc.column_size
                              else 646456993
                         end)
            when 3 then (case when sc.column_size > 0
                              then sc.column_size
                              else 646456993
                         end)
            when 4 then 10
            when 5 then 5
            when 6 then 52
            when 7 then 24
            else null
       end as numeric_precision
      ,case when sc.data_type in(-6,-5,2,3,4,5)
            then 10
            when sc.data_type in (6,7)
            then 2
            else null
       end as numeric_precision_radix
      ,sc.decimal_digits as numeric_scale
      ,case sc.data_type
           when 93
           then (case when sc.column_size > 6
                      then 6
                      else sc.column_size
                 end)
           else null
       end as datetime_precision
      ,null as interval_type
      ,null as interval_precision
      ,case when sc.data_type in (-1,1,12,2005)
            then sc.table_cat
            else null
       end as character_set_catalog
      ,case when sc.data_type in (-1,1,12,2005)
            then 'PUBLIC'
            else null
       end as character_set_schema
      ,case when sc.data_type in (-1,1,12,2005)
            then 'UNICODE'
            else null
       end as character_set_name
      ,case when sc.data_type in (-1,1,12,2005)
            then sc.table_cat
            else null
            end as collation_catalog
      ,case when sc.data_type in (-1,1,12,2005)
            then 'PUBLIC'
            else null
       end as collation_schema
      ,null as collation_name
      ,null as domain_catalog
      ,null as domain_schema
      ,null as domain_name
      ,null as udt_catalog
      ,null as udt_schema
      ,null as udt_name
      ,sc.scope_catlog as scope_catalog
      ,sc.scope_schema as scope_schema
      ,sc.scope_table as scope_name
      ,null as maximum_cardinality
      ,null as dtd_identifier
      ,'NO' as is_self_referencing
      ,'NO' is_identity
      ,null as identity_generation
      ,null as identity_start
      ,null as identity_increment
      ,null as identity_maximum
      ,null as identity_minimum
      ,null as identity_cycle
      ,'NEVER' as is_generated
      ,null as generation_expression
      ,'YES' as is_updatable
      ,sc.type_name as declared_data_type
      ,case when (sc.data_type in (2,3) and sc.column_size < 646456993)
                 then sc.column_size
                 else null
       end as declared_numeric_precision
      ,case when sc.data_type in (2,3) and sc.column_size < 646456993
                 then sc.decimal_digits
                 else null
       end as declared_numeric_scale
      ,(select distinct
               case when (count(column_name) < 1)
               then (1=0)
               else (1=1)
               end
             from information_schema.system_primarykeys spk
            where (sc.table_cat = spk.table_cat or (sc.table_cat is null and spk.table_cat is null))
              and (sc.table_schem = spk.table_schem)
              and (sc.table_name = spk.table_name)
              and (sc.column_name = spk.column_name)
       ) as primary_key
  from information_schema.system_columns sc
 where 1=1";

        #endregion

        #region ColumnsCollection()
        /// <summary>
        /// Initializes a new instance of the <see cref="ColumnsCollection"/> class.
        /// </summary>
        public ColumnsCollection() : base() { }
        #endregion

        #region CreateTable()
        /// <summary>
        /// Creates a new <c>Columns</c> metadata collection table.
        /// </summary>
        /// <returns>
        /// A new <see cref="DataTable"/> initialized with the columns
        /// required to hold the metadata about the columns of the 
        /// tables defined in the database.
        /// </returns>
        public override DataTable CreateTable()
        {
            DataTable table = new DataTable(HsqlMetaDataCollectionNames.Columns);
            DataColumnCollection cols = table.Columns;

            AddColumn(cols, null, "TABLE_CATALOG", typeof(string));
            AddColumn(cols, null, "TABLE_SCHEMA", typeof(string));
            AddColumn(cols, null, "TABLE_NAME", typeof(string));
            AddColumn(cols, null, "COLUMN_NAME", typeof(string));
            AddColumn(cols, null, "ORDINAL_POSITION", typeof(int));
            AddColumn(cols, null, "COLUMN_DEFAULT", typeof(string));
            AddColumn(cols, null, "IS_NULLABLE", typeof(string));
            AddColumn(cols, null, "DATA_TYPE", typeof(string));
            AddColumn(cols, null, "CHARACTER_MAXIMUM_LENGTH", typeof(int));
            AddColumn(cols, null, "CHARACTER_OCTET_LENGTH", typeof(int));
            AddColumn(cols, null, "NUMERIC_PRECISION", typeof(int));
            AddColumn(cols, null, "NUMERIC_PRECISION_RADIX", typeof(int));
            AddColumn(cols, null, "NUMERIC_SCALE", typeof(int));
            AddColumn(cols, null, "DATETIME_PRECISION", typeof(int));
            AddColumn(cols, null, "INTERVAL_TYPE", typeof(string));
            AddColumn(cols, null, "INTERVAL_PRECISION", typeof(int));
            AddColumn(cols, null, "CHARACTER_SET_CATALOG", typeof(string));
            AddColumn(cols, null, "CHARACTER_SET_SCHEMA", typeof(string));
            AddColumn(cols, null, "CHARACTER_SET_NAME", typeof(string));
            AddColumn(cols, null, "COLLATION_CATALOG", typeof(string));
            AddColumn(cols, null, "COLLATION_SCHEMA", typeof(string));
            AddColumn(cols, null, "COLLATION_NAME", typeof(string));
            AddColumn(cols, null, "DOMAIN_CATALOG", typeof(string));
            AddColumn(cols, null, "DOMAIN_SCHEMA", typeof(string));
            AddColumn(cols, null, "DOMAIN_NAME", typeof(string));
            AddColumn(cols, null, "UDT_CATALOG", typeof(string));
            AddColumn(cols, null, "UDT_SCHEMA", typeof(string));
            AddColumn(cols, null, "UDT_NAME", typeof(string));
            AddColumn(cols, null, "SCOPE_CATALOG", typeof(string));
            AddColumn(cols, null, "SCOPE_SCHEMA", typeof(string));
            AddColumn(cols, null, "SCOPE_NAME", typeof(string));
            AddColumn(cols, null, "MAXIMUM_CARDINALITY", typeof(int));
            AddColumn(cols, null, "DTD_IDENTIFIER", typeof(string));
            AddColumn(cols, null, "IS_SELF_REFERENCING", typeof(string));
            AddColumn(cols, null, "IS_IDENTITY", typeof(string));
            AddColumn(cols, null, "IDENTITY_GENERATION", typeof(string));
            AddColumn(cols, null, "IDENTITY_START", typeof(string));
            AddColumn(cols, null, "IDENTITY_INCREMENT", typeof(string));
            AddColumn(cols, null, "IDENTITY_MAXIMUM", typeof(string));
            AddColumn(cols, null, "IDENTITY_MINIMUM", typeof(string));
            AddColumn(cols, null, "IDENTITY_CYCLE", typeof(string));
            AddColumn(cols, null, "IS_GENERATED", typeof(string));
            AddColumn(cols, null, "GENERATION_EXPRESSION", typeof(string));
            AddColumn(cols, null, "IS_UPDATABLE", typeof(string));
            AddColumn(cols, null, "DECLARED_DATA_TYPE", typeof(string));
            AddColumn(cols, null, "DECLARED_NUMERIC_PRECISION", typeof(int));
            AddColumn(cols, null, "DECLARED_NUMERIC_SCALE", typeof(int));
            AddColumn(cols, null, "PRIMARY_KEY", typeof(bool));

            return table;
        }
        #endregion

        #region FillTable(HsqlConnection,DataTable,string[])
        /// <summary>
        /// Fills the given <c>Columns</c> metadata collection table
        /// using the given connection and restrictions.
        /// </summary>
        /// <param name="connection">The connection.</param>
        /// <param name="table">The table to fill.</param>
        /// <param name="restrictions">The restrictions to apply.</param>
        public override void FillTable(HsqlConnection connection,
            DataTable table, string[] restrictions)
        {
            restrictions = GetRestrictions(restrictions, 4);

            string catalog = restrictions[0];
            string schemaNamePattern = restrictions[1];
            string tableNamePattern = restrictions[2];
            string columnNamePattern = restrictions[3];

            if (WantsIsNull(tableNamePattern)
                || WantsIsNull(columnNamePattern))
            {
                return;
            }

            schemaNamePattern = TranslateSchema(connection, schemaNamePattern);

            StringBuilder query = new StringBuilder(sql)
#if CATALOG_RESTRICTIONS
                .Append(And("TABLE_CAT", "=", catalog))
#endif
                .Append(And("TABLE_SCHEM", "LIKE", schemaNamePattern))
                .Append(And("TABLE_NAME", "LIKE", tableNamePattern))
                .Append(And("COLUMN_NAME", "LIKE", columnNamePattern));

            HsqlDataReader reader;

            string collation = "UCS_BASIC";

            const int icatalog = 0;
            const int ischema = 1;
            const int itable = 2;
            const int icolumn = 3;
            const int iordinal = 4;
            //
            const int idefault = 5;
            const int iis_nullable = 6;
            const int idatatype = 7;
            //
            const int icharmaxlen = 8;
            const int icharoctlen = 9;
            //
            const int inumprec = 10;
            const int inumprecrad = 11;
            const int inumscale = 12;
            //
            const int idatetimeprec = 13;
            const int iintervaltype = 14;
            const int iintervalprec = 15;
            //
            const int icharsetcat = 16;
            const int icharsetschem = 17;
            const int icharsetname = 18;
            //
            const int icollationcat = 19;
            const int icollationschem = 20;
            //const int icollationname = 21;
            //
            const int idomaincat = 22;
            const int idomainschem = 23;
            const int idomainname = 24;
            //
            const int iudtcat = 25;
            const int iudtschem = 26;
            const int iudtname = 27;
            //
            const int iscopecat = 28;
            const int iscopeschem = 29;
            const int iscopename = 30;
            //
            const int imaxcardinality = 31;
            //
            const int idtdidentifier = 32;
            //
            const int iis_selfref = 33;
            //
            const int iis_identity = 34;
            const int iidentitygen = 35;
            const int iidentitystart = 36;
            const int iidentityinc = 37;
            const int iidentitymax = 38;
            const int iidentitymin = 39;
            const int iidentitycycle = 40;
            //
            const int iis_generated = 41;
            const int igenerationexp = 42;
            //
            const int iis_updatable = 43;
            //
            const int idecldatatype = 44;
            const int ideclnumprec = 45;
            const int ideclnumscale = 46;
            //
            const int iprimarykey = 47;

            using (reader = Execute(connection, query.ToString()))
            {
                object[] values = new object[reader.FieldCount];

                while (reader.Read())
                {
                    reader.GetValues(values);

                    string catalogName = (string)values[icatalog];
                    string schemaName = (string)values[ischema];
                    string tableName = (string)values[itable];
                    string columnName = (string)values[icolumn];
                    int ordinalPosition = (int)values[iordinal];
                    //
                    string defaultValue = (string)values[idefault];
                    string isNullable = (string)values[iis_nullable];
                    string dataType = (string)values[idatatype];
                    //
                    int? charMaxLength = (int?)values[icharmaxlen];
                    int? charOctetLength = (int?)values[icharoctlen];
                    //
                    int? ni;
                    int? numPrecision = (int?)values[inumprec];
                    short? numPrecRadix = (short?)(int?)values[inumprecrad];
                    int? numScale = (int?)values[inumscale];
                    //
                    int? dateTimePrecision = (int?)values[idatetimeprec];
                    //
                    string intervalType = (string)values[iintervaltype];
                    short? intervalPrecision = (short?)(int?)values[iintervalprec];
                    //
                    string characterSetCatalog = (string)values[icharsetcat];
                    string characterSetSchema = (string)values[icharsetschem];
                    string characterSetName = (string)values[icharsetname];
                    //
                    string collationCatalog = (string)values[icollationcat];
                    string collationSchema = (string)values[icollationschem];
                    string collationName = collation; // values[21];
                    // TODO
                    string domainCatalog = (string)values[idomaincat];
                    string domainSchema = (string)values[idomainschem];
                    string domainName = (string)values[idomainname];
                    // TODO
                    string udtCatalog = (string)values[iudtcat];
                    string udtSchema = (string)values[iudtschem];
                    string udtName = (string)values[iudtname];
                    // TODO
                    string scopeCatalog = (string)values[iscopecat];
                    string scopeSchema = (string)values[iscopeschem];
                    string scopeName = (string)values[iscopename];
                    //
                    int? maximumCardinality = (int?)values[imaxcardinality];
                    //
                    string dtdIdentifier = (string)values[idtdidentifier];
                    //
                    string isSelfReferencing = (string)values[iis_selfref];

                    // TODO:  Efficient implementation requires a new HQLDB system table.
                    //        Otherwise, we need to execute an empty select against the
                    //        column's table and read the ResultSetMetaData to tell if
                    //        autoIncrement is true for the column...ugh.
                    string isIdentity = (string)values[iis_identity];
                    string identityGeneration = (string)values[iidentitygen];
                    string identityStart = (string)values[iidentitystart];
                    string identityIncrement = (string)values[iidentityinc];
                    string identityMaximum = (string)values[iidentitymax];
                    string identityMinimum = (string)values[iidentitymin];
                    string identityCycle = (string)values[iidentitycycle];
                    //
                    string isGenerated = (string)values[iis_generated];
                    string generationExpression = (string)values[igenerationexp];
                    string isUpdatable = (string)values[iis_updatable];
                    string declaredDataType = (string)values[idecldatatype];
                    int? declaredNumericPrecision = (int?)values[ideclnumprec];
                    int? declaredNumericScale = (int?)values[ideclnumscale];
                    bool primaryKey = (bool)values[iprimarykey];

                    AddRow(table,
                           catalogName,
                           schemaName,
                           tableName,
                           columnName,
                           ordinalPosition,
                           defaultValue,
                           isNullable,
                           dataType,
                           charMaxLength,
                           charOctetLength,
                           numPrecision,
                           numPrecRadix,
                           numScale,
                           dateTimePrecision,
                           intervalType,
                           intervalPrecision,
                           characterSetCatalog,
                           characterSetSchema,
                           characterSetName,
                           collationCatalog,
                           collationSchema,
                           collationName,
                           domainCatalog,
                           domainSchema,
                           domainName,
                           udtCatalog,
                           udtSchema,
                           udtName,
                           scopeCatalog,
                           scopeSchema,
                           scopeName,
                           maximumCardinality,
                           dtdIdentifier,
                           isSelfReferencing,
                           isIdentity,
                           identityGeneration,
                           identityStart,
                           identityIncrement,
                           identityMaximum,
                           identityMinimum,
                           identityCycle,
                           isGenerated,
                           generationExpression,
                           isUpdatable,
                           declaredDataType,
                           declaredNumericPrecision,
                           declaredNumericScale,
                           primaryKey);
                }
            }
        }
        #endregion

        #region AddRow(...)
        /// <summary>
        /// Adds a new row to the given <c>Columns</c> metadata collection table.
        /// </summary>
        /// <param name="table">The table to which to add the new row.</param>
        /// <param name="tableCatalog">The table catalog.</param>
        /// <param name="tableSchema">The table schema.</param>
        /// <param name="tableName">The simple name of the table.</param>
        /// <param name="columnName">The simple name of the column.</param>
        /// <param name="ordinalPosition">The column's ordinal position.</param>
        /// <param name="columnDefault">The column's default value.</param>
        /// <param name="isNullable">Whether the column is nullable.</param>
        /// <param name="dataType">The column's data type.</param>
        /// <param name="characterMaximumLength">Maximum length of the column's character values.</param>
        /// <param name="characterOctetLength">The maximum octet (byte) length of column's values.</param>
        /// <param name="numericPrecision">The column's numeric precision.</param>
        /// <param name="numericPrecisionRadix">The column's numeric precision radix.</param>
        /// <param name="numericScale">The column's numeric scale.</param>
        /// <param name="dateTimePrecision">The column's date/time precision.</param>
        /// <param name="intervalType">The column's datetime interval type.</param>
        /// <param name="intervalPrecision">The column's datetime interval precision.</param>
        /// <param name="characterSetCatalog">The column's character set catalog.</param>
        /// <param name="characterSetSchema">The column's character set schema.</param>
        /// <param name="characterSetName">Name of the column's character set.</param>
        /// <param name="collationCatalog">The column's collation catalog.</param>
        /// <param name="collationSchema">The column's collation schema.</param>
        /// <param name="collationName">The simple name of the column's collation.</param>
        /// <param name="domainCatalog">The distinct column's domain catalog.</param>
        /// <param name="domainSchema">The distinct column's domain schema.</param>
        /// <param name="domainName">The simple name of the distinct column's domain.</param>
        /// <param name="udtCatalog">The udt column's udt catalog.</param>
        /// <param name="udtSchema">The udt column's udt schema.</param>
        /// <param name="udtName">The simple name of the udt column's user defined type.</param>
        /// <param name="scopeCatalog">The ref column's scope catalog.</param>
        /// <param name="scopeSchema">The ref column's scope schema.</param>
        /// <param name="scopeName">The simple name of the ref column's scope.</param>
        /// <param name="maximumCardinality">The maximum cardinality of the column's type.</param>
        /// <param name="dtdIdentifier">The column's DTD identifier.</param>
        /// <param name="isSelfReferencing">Whether the column is self referencing.</param>
        /// <param name="isIdentity">Whether the column is an identity.</param>
        /// <param name="identityGeneration">The column's type of identity generation.</param>
        /// <param name="identityStart">The identity start.</param>
        /// <param name="identityIncrement">The identity increment.</param>
        /// <param name="identityMaximum">The identity maximum.</param>
        /// <param name="identityMinimum">The identity minimum.</param>
        /// <param name="identityCycle">The identity cycle.</param>
        /// <param name="isGenerated">Whether the column's values are generated.</param>
        /// <param name="generationExpression">The generation expression.</param>
        /// <param name="isUpdatable">Whether the column is updatable.</param>
        /// <param name="declaredDataType">The column's declared data type.</param>
        /// <param name="declaredNumericPrecision">The column's declared numeric precision.</param>
        /// <param name="declaredNumericScale">The column's declared numeric scale.</param>
        /// <param name="primaryKey">The primary key.</param>
        public static void AddRow(
            DataTable table,
            string tableCatalog,
            string tableSchema,
            string tableName,
            string columnName,
            int ordinalPosition,
            string columnDefault,
            string isNullable,
            string dataType,
            int? characterMaximumLength,
            int? characterOctetLength,
            int? numericPrecision,
            int? numericPrecisionRadix,
            int? numericScale,
            int? dateTimePrecision,
            string intervalType,
            int? intervalPrecision,
            string characterSetCatalog,
            string characterSetSchema,
            string characterSetName,
            string collationCatalog,
            string collationSchema,
            string collationName,
            string domainCatalog,
            string domainSchema,
            string domainName,
            string udtCatalog,
            string udtSchema,
            string udtName,
            string scopeCatalog,
            string scopeSchema,
            string scopeName,
            int? maximumCardinality,
            string dtdIdentifier,
            string isSelfReferencing,
            string isIdentity,
            string identityGeneration,
            string identityStart,
            string identityIncrement,
            string identityMaximum,
            string identityMinimum,
            string identityCycle,
            string isGenerated,
            string generationExpression,
            string isUpdatable,
            string declaredDataType,
            int? declaredNumericPrecision,
            int? declaredNumericScale,
            bool primaryKey)
        {
            DataRow row = table.NewRow();

            row["TABLE_CATALOG"] = tableCatalog;
            row["TABLE_SCHEMA"] = tableSchema;
            row["TABLE_NAME"] = tableName;
            row["COLUMN_NAME"] = columnName;
            row["ORDINAL_POSITION"] = ordinalPosition;
            row["COLUMN_DEFAULT"] = columnDefault;
            row["IS_NULLABLE"] = isNullable;
            row["DATA_TYPE"] = dataType;
            if (characterMaximumLength != null)
            {
                row["CHARACTER_MAXIMUM_LENGTH"] = characterMaximumLength;
            }
            if (characterOctetLength != null)
            {
                row["CHARACTER_OCTET_LENGTH"] = characterOctetLength;
            }

            if (numericPrecision != null)
            {
                row["NUMERIC_PRECISION"] = numericPrecision;
            }
            if (numericPrecisionRadix != null)
            {
                row["NUMERIC_PRECISION_RADIX"] = (short)numericPrecisionRadix.Value;
            }
            if (numericScale != null)
            {
                row["NUMERIC_SCALE"] = numericScale;
            }
            if (dateTimePrecision != null)
            {
                row["DATETIME_PRECISION"] = (short)dateTimePrecision.Value;
            }
            row["INTERVAL_TYPE"] = intervalType;
            if (intervalPrecision != null)
            {
                row["INTERVAL_PRECISION"] = intervalPrecision;
            }
            row["CHARACTER_SET_CATALOG"] = characterSetCatalog;
            row["CHARACTER_SET_SCHEMA"] = characterSetSchema;
            row["CHARACTER_SET_NAME"] = characterSetName;
            //
            row["COLLATION_CATALOG"] = collationCatalog;
            row["COLLATION_SCHEMA"] = collationSchema;
            row["COLLATION_NAME"] = collationName;
            //
            row["DOMAIN_CATALOG"] = domainCatalog;
            row["DOMAIN_SCHEMA"] = domainSchema;
            row["DOMAIN_NAME"] = domainName;
            //
            row["SCOPE_CATALOG"] = scopeCatalog;
            row["SCOPE_SCHEMA"] = scopeSchema;
            row["SCOPE_NAME"] = scopeName;
            //
            if (maximumCardinality != null)
            {
                row["MAXIMUM_CARDINALITY"] = maximumCardinality;
            }
            //
            row["DTD_IDENTIFIER"] = dtdIdentifier;
            row["IS_SELF_REFERENCING"] = isSelfReferencing;
            //
            row["IS_IDENTITY"] = isIdentity;
            row["IDENTITY_GENERATION"] = identityGeneration;
            row["IDENTITY_START"] = identityStart;
            row["IDENTITY_INCREMENT"] = identityIncrement;
            row["IDENTITY_MAXIMUM"] = identityMaximum;
            row["IDENTITY_MINIMUM"] = identityMinimum;
            row["IDENTITY_CYCLE"] = identityCycle;
            //
            row["IS_GENERATED"] = isGenerated;
            row["GENERATION_EXPRESSION"] = generationExpression;
            //
            row["IS_UPDATABLE"] = isUpdatable;
            //
            row["DECLARED_DATA_TYPE"] = declaredDataType;
            if (declaredNumericPrecision != null)
            {
                row["DECLARED_NUMERIC_PRECISION"] = declaredNumericPrecision;
            }
            if (declaredNumericScale != null)
            {
                row["DECLARED_NUMERIC_SCALE"] = declaredNumericScale;
            }
            row["PRIMARY_KEY"] = primaryKey;

            table.Rows.Add(row);
        }
        #endregion
    }
    #endregion
}