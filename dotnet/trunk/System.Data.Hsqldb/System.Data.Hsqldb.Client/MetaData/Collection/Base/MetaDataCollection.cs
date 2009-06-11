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
using System.Text;
using System.Data.Hsqldb.Common.Enumeration;
using StringUtil = org.hsqldb.lib.StringUtil;
using HsqlTypes = org.hsqldb.Types;
#endregion

namespace System.Data.Hsqldb.Client.MetaData.Collection.Base
{
    #region MetaDataCollection
    /// <summary>
    /// <para>
    /// The ancestor of all HSQLDB MetaData collections.
    /// </para>
    /// <img src="../Documentation/ClassDiagrams/System.Data.Hsqldb.Client.MetaData.Collection.BaseCollection.png"
    /// alt="BaseCollection Class Diagram"/>
    /// </summary>
    /// <author name="boucherb@users"/>
    public abstract class MetaDataCollection
    {
        #region Constants
        static readonly string DefaultSchemaQuery = string.Format(
@"-- {0}.DefaultSchemaQuery 
SELECT SCHEMA_NAME
  FROM INFORMATION_SCHEMA.SYSTEM_SCHEMAS 
 WHERE (IS_DEFAULT=TRUE)",
            typeof(MetaDataCollection).FullName);
        #endregion

        #region Fields

        #region Comma
        /// <summary>
        /// Small optimization.
        /// </summary>
        /// <remarks>
        /// Avoids creating a new char[] object every time
        /// <c>string.Split(comma)</c> is invoked.
        /// </remarks>
        protected static readonly char[] Comma = new char[] { ',' }; 
        #endregion

        #endregion

        #region Constructors

        #region MetaDataCollection()
        /// <summary>
        /// For subclasses.
        /// </summary>
        protected MetaDataCollection() : base(){}
        #endregion

        #endregion Constructors

        #region Public Instance Methods

        #region GetSchema(HsqlConnection,string[])
        /// <summary>
        /// Produces a schema table filled with data rows
        /// satisfying the given <c>restrictions</c>.
        /// </summary>
        /// <remarks>
        /// By default, invokes <see cref="CreateTable()"/> to create
        /// a table with the expected column collection, invokes
        /// <see cref="FillTable(HsqlConnection,DataTable,string[])"/>
        /// to populate the table's row collection, and then sets
        /// every data column in the table's column collection to 
        /// <see cref="DataColumn.ReadOnly"/>.
        /// </remarks>
        /// <param name="connection">The connection.</param>
        /// <param name="restrictions">The restrictions.</param>
        /// <returns></returns>
        public virtual DataTable GetSchema(HsqlConnection connection,
            string[] restrictions)
        {
            DataTable table = CreateTable();

            FillTable(connection, table, restrictions);

            foreach (DataColumn column in table.Columns)
            {
                column.ReadOnly = true;
            }

            return table;
        }
        #endregion

        #region CreateTable()
        /// <summary>
        /// Produces a metadata collection table .
        /// </summary>
        /// <remarks>
        /// The returned object should be an empty 
        /// data table whose columns collection has
        /// been populated with the data columns required
        /// to represent the specifically intended
        /// collection of metadata.
        /// </remarks>
        /// <returns>
        /// The table.
        /// </returns>
        public abstract DataTable CreateTable();
        #endregion

        #region FillTable(HsqlConnection,DataTable,string[])
        /// <summary>
        /// Fills the metadata collection table.
        /// </summary>
        /// <remarks>
        /// It is expected that the <c>table</c> was obtained by
        /// invoking <see cref="CreateTable"/>.  Otherwise, it is
        /// the responsibility of the caller to correctly populate
        /// the table's column collection before passing the table
        /// to this method.
        /// </remarks>
        /// <param name="connection">The connection.</param>
        /// <param name="table">The table.</param>
        /// <param name="restrictions">The restrictions.</param>
        public abstract void FillTable(
            HsqlConnection connection,
            DataTable table,
            string[] restrictions);
        #endregion

        #endregion

        #region Protected Instance Methods

        #region Execute(HsqlConnection,string)
        /// <summary>
        /// Executes the specified SQL query.
        /// </summary>
        /// <param name="connection">The connection.</param>
        /// <param name="sql">The SQL query to execute.</param>
        /// <returns>A new data reader holding the result</returns>
        protected HsqlDataReader Execute(
            HsqlConnection connection, 
            string sql)
        {
            return new HsqlDataReader(connection.Session.ExecuteDirect(sql));
        }
        #endregion

        #region ExecuteSelect(HsqlConnection,string,string)
        /// <summary>
        /// Executes an SQL SELECT of the form
        /// <c>SELECT * FROM &lt;table&gt; [WHERE &lt;where*gt;]</c>.
        /// </summary>
        /// <param name="connection">The connection.</param>
        /// <param name="table">The table name.</param>
        /// <param name="where">The where clause.</param>
        /// <returns>A new data reader holding the result.</returns>
        protected HsqlDataReader ExecuteSelect(
            HsqlConnection connection, 
            string table, 
            string where)
        {
            StringBuilder select = new StringBuilder("SELECT * FROM ");

            select.Append(table);

            if (!string.IsNullOrEmpty(where))
            {
                select.Append(" WHERE ").Append(where);
            }

            return Execute(connection, select.ToString());
        }
        #endregion

        #region TranslateSchema(HsqlConnection,string)
        /// <summary>
        /// Translates the given schema name value.
        /// </summary>
        /// <remarks>
        /// 
        /// </remarks>
        /// <param name="connection">The connection.</param>
        /// <param name="schemaName">Name of the schema.</param>
        /// <returns></returns>
        protected string TranslateSchema(HsqlConnection connection, 
            string schemaName)
        {
            if (connection.Settings.DefaultSchemaQualification
                && (WantsIsNull(schemaName)))
            {
                string defaultSchema = connection.Session.ExecuteScalarDirect(
                    DefaultSchemaQuery) as string;

                return (string.IsNullOrEmpty(defaultSchema)) 
                    ? schemaName : defaultSchema;
            }
            else
            {
                return schemaName;
            }
        }
        #endregion

        #endregion Protected Instance Methods

        #region Public Static Methods

        #region GetRestrictions(string[],int)
        /// <summary>
        /// Gets the given number of values from the given array.
        /// </summary>
        /// <remarks>
        /// null input array and array length / count mismatch cases are
        /// handled such that an array of the requested length is <c>always</c>
        /// retrieved sucessfully.
        /// </remarks>
        /// <param name="restrictions">The candidate restrictions.</param>
        /// <param name="count">The number of values to retrieve.</param>
        /// <returns>
        /// An array containing the given number of values from the given array.
        /// </returns>
        public static string[] GetRestrictions(string[] restrictions, int count)
        {
            string[] l_restrictions = new string[count];

            if (restrictions != null)
            {
                count = Math.Min(count, restrictions.Length);

                for (int i = 0; i < count; i++)
                {
                    l_restrictions[i] = restrictions[i];
                }
            }

            return l_restrictions;
        }
        #endregion

        #region AddColumn(DataColumnCollection,object,string,Type)
        /// <summary>
        /// Constructs a new <c>DataColumn</c> with the given
        /// <c>name</c>, <c>type</c> and <c>defaultValue</c>,
        /// adding it to the given collection.
        /// </summary>
        /// <param name="columns">
        /// The collection to which to add the new column.
        /// </param>
        /// <param name="defaultValue">
        /// The new column's default value
        /// </param>
        /// <param name="name">
        /// The name of the new column.  If a column with the
        /// same name pre-exists in the collection, this operation
        /// is a no-op.
        /// </param>
        /// <param name="type">
        /// The System.Type of the new column's values.
        /// </param>
        public static void AddColumn(DataColumnCollection columns,
                                     object defaultValue,
                                     string name,
                                     Type type)
        {
            DataColumn column = new DataColumn(name, type);

            if (defaultValue != null)
            {
                column.DefaultValue = defaultValue;
            }

            columns.Add(column);
        }
        #endregion

        #region And(string,string,string)
        /// <summary>
        /// Constructs an SQL predicate of the form <c>" AND ident oper
        /// value"</c> with special rules applied based on <c>value</c>
        /// and the SQL operation represented by <c>oper</c>.
        /// </summary>
        /// <remarks>
        /// In particular, if <c>value</c> is <c>null</c>, then
        /// <c>String.Empty</c> is returned (corresponding to the empty
        /// SQL predicate); else if <c>value</c> is String.Empty,
        /// then <c>" AND ident IS NULL</c> is returned; else
        /// if <c>value</c>is a string object, then it is converted
        /// to the SQL string literal form; if the case-insensitive
        /// value of <c>oper</c> is "LIKE", then if <c>value</c> contains
        /// no wildcard characters, <c>oper</c> is conterted to "=";
        /// otherwise, if <c>value</c> contains at least one wildcard
        /// character escape sequence, then the resulting expression is
        /// appended with "ESCAPE '\'".
        /// </remarks>
        /// <param name="ident">The SQL identifier.</param>
        /// <param name="oper">The SQL operator.</param>
        /// <param name="value">The SQL value term.</param>
        /// <returns></returns>
        public static string And(string ident, string oper, object value)
        {
            // - pass null to mean ignore (do not include in query),
            // - pass "" to mean filter on <column-ident> IS NULL,
            // - pass "%" to filter on <column-ident> IS NOT NULL.
            // - pass sequence with "%" and "_" for wildcard matches
            // - @"\%"  and @"\_" values escape wildcard matching
            // - when searching on values reported directly from HsqlDatabaseMetaData
            //   results, typically an exact match is desired.  In this case, it
            //   is the client's responsibility to escape any reported "%" and "_"
            //   characters using whatever HsqlDatabaseMetaData returns for
            //   search escape string. In our case, this is the standard escape
            //   character: '\'. Typically, '%' will rarely be encountered, but
            //   certainly '_' is to be expected on a regular basis.
            // - What about the (silly) case where an identifier
            //   has been declared such as:  'create table "xxx\_yyy"(...)'?
            //   Must the client still escape the string like this:
            //   @"xxx\\_yyy"?
            //   Yes: because otherwise the driver is expected to
            //   construct something like:
            //   select ... where ... like 'xxx\_yyy' escape '\'
            //   which will try to match 'xxx_yyy', not 'xxx\_yyy'
            //   Testing indicates that indeed, higher quality popular
            //   database browsers do the escapes "properly."
            if (value == null)
            {
                return String.Empty;
            }

            StringBuilder sb = new StringBuilder();
            bool isString = (value is string);

            if (isString && WantsIsNull((string)value))
            {
                return sb.Append(" AND ")
                    .Append(ident)
                    .Append(" IS NULL")
                    .ToString();
            }

            string stringValue = isString
                                     ? ToSQLString((string)value)
                                     : Convert.ToString(value);

            sb.Append(" AND ").Append(ident).Append(' ');

            if (isString && "LIKE".Equals(oper,
                                          StringComparison.InvariantCultureIgnoreCase))
            {
                if (stringValue.IndexOf('_') < 0
                    && stringValue.IndexOf('%') < 0)
                {
                    // then we can optimize.
                    sb.Append("= ").Append(stringValue);
                }
                else
                {
                    sb.Append("LIKE ").Append(stringValue);

                    if ((stringValue.IndexOf(@"\_") >= 0)
                        || (stringValue.IndexOf(@"\%") >= 0))
                    {
                        // then client has requested at least one escape.
                        sb.Append(@" ESCAPE '\'");
                    }
                }
            }
            else
            {
                sb.Append(oper).Append(' ').Append(stringValue);
            }

            return sb.ToString();
        }
        #endregion

        #region ToSQLString(string)
        /// <summary>
        /// Surrounds the given string with single quotes,
        /// escaping internal single quotes in the SQL
        /// string literal fashion (by doubling them).
        /// </summary>
        /// <param name="s">
        /// The character sequence to convert to
        /// SQL string literal form.
        /// </param>
        /// <returns>
        /// The given string as an SQL string literal.
        /// </returns>
        public static string ToSQLString(string s)
        {
            StringBuilder sb = new StringBuilder();

            sb.Append('\'');

            char[] chars = s.ToCharArray();

            for (int i = 0; i < chars.Length; i++)
            {
                char ch = chars[i];

                sb.Append(ch);

                if (ch == '\'')
                {
                    sb.Append('\'');
                }
            }

            sb.Append('\'');

            return sb.ToString();
        }
        #endregion

        #region ToSQLInList(string[])
        /// <summary>
        /// Retrieves a parenthesized, comma-separated list of the form expected as
        /// the right argument of an SQL IN predicate (i.e. the character sequence:
        /// <c>('terms[0]', ..., 'terms[n]')</c>).
        /// </summary>
        /// <remarks>
        /// Each character sequence in terms is first converted to SQL string literal
        /// form before it is appended to the return value.
        /// </remarks>
        /// <param name="terms">
        /// The list of values with which to construct the SQL IN list
        /// </param>
        /// <returns>
        /// A parenthesized, comma-separated list of SQL string literals
        /// </returns>
        public static string ToSQLInList(string[] terms)
        {
            int length = terms.Length;
            StringBuilder sb = new StringBuilder('(');

            for (int i = 0; i < length; i++)
            {
                if (i != 0)
                {
                    sb.Append(',');
                }

                sb.Append(ToSQLString(terms[i]));
            }

            sb.Append(')');

            return sb.ToString();
        }
        #endregion

        #region ToQueryPrefix(String)
        /// <summary>
        /// Retrieves "SELECT * FROM <c>tableName</c> WHERE 1=1"
        /// as a <c>StringBuilder</c> object.
        /// </summary>
        /// <remarks>
        /// The "1=1" (Always TRUE) predicate is appended as a
        /// convenience so that further predicates may be appended
        /// without having to keep an accounting of which is the first
        /// so that its otherwise illegal preceeding "AND" can be
        /// programatically excluded.  This will not reduce performance
        /// because the database engine recognizes the that the "1=1"
        /// predicate is always true and simply removes it at parse time.
        /// </remarks>
        /// <param name="tableName">Name of the table.</param>
        /// <returns>
        /// A StringBuilder containing "SELECT * FROM <c>tableName</c> WHERE 1=1"
        /// </returns>
        public static StringBuilder ToQueryPrefix(String tableName)
        {
            return new StringBuilder(255)
                .Append("SELECT * FROM ")
                .Append(tableName)
                .Append(" WHERE 1=1");
        }
        #endregion

        #region WantsIsNull(string)
        /// <summary>
        /// Determines whether the given restriction value
        /// is effectively an SQL <c>IS NULL</c> predicate against the
        /// corresponding values in the corresponding metadata
        /// collection.
        /// </summary>
        /// <param name="restriction">The restriction.</param>
        /// <returns>
        /// <c>true</c> when <c>restriction</c> is <c>String.Empty</c>;
        /// else <c>false</c>.
        /// </returns>
        public static bool WantsIsNull(string restriction)
        {
            return (String.Empty == restriction);
        }
        #endregion

        #region IsBestMatchProviderTypeName(string)
        /// <summary>
        /// Determines whether the characteristics of the provider-specific
        /// SQL data type with the given type name most closely match the
        /// characteristics expected provider-specific data type code.
        /// </summary>
        /// <remarks>
        /// The test is simple: <c>return ("VARCHAR_IGNORECASE" != typeName);</c>
        /// No check for valid <c>typeName</c> is performed.
        /// </remarks>
        /// <param name="typeName">
        /// Name of the data type.
        /// </param>
        /// <returns>
        /// <c>true</c> if the specified data type name is the best match;
        /// otherwise, <c>false</c>.
        /// </returns>
        public static bool IsBestMatchProviderTypeName(string typeName)
        {
            return ("VARCHAR_IGNORECASE" != typeName);
        }
        #endregion


        #region MyRegion
        /// <summary>
        /// Determines whether the indicated SQL data type is long.
        /// </summary>
        /// <param name="jdbcType">A JDBC type code denoting an SQL data type.</param>
        /// <returns>
        /// <c>true</c> if the indicated SQL data type is a
        /// long variant of an intrinsic type;
        /// otherwise, <c>false</c>.
        /// </returns>
        public static bool IsLongProviderType(int type)
        {
            return (type == (int)HsqlProviderType.LongVarChar)
                   || (type == (int)HsqlProviderType.LongVarBinary)
                   || (type == (int)HsqlProviderType.Clob)
                   || (type == (int)HsqlProviderType.Blob);
        } 
        #endregion


        #region IsNullable(int)
        /// <summary>
        /// Determines whether the specified <see cref="DataTypeNullability"/>
        /// code denotes that the corresponding data element is nullable.
        /// </summary>
        /// <param name="nullability">The JDBC nullability code.</param>
        /// <returns>
        /// <c>true</c> if the specified JDBC nullability code
        /// indicates that the corresponding data element is nullable;
        /// otherwise, <c>false</c>.
        /// </returns>
        public static bool IsNullable(int nullability)
        {
            return (nullability != (int)DataTypeNullability.NoNulls);
        }
        #endregion

        #region IsSearchable(int)
        /// <summary>
        /// Determines whether the specified <see cref="DataTypeSearchability"/>
        /// code denotes that the corresponding data element is searchable in
        /// any way.
        /// </summary>
        /// <param name="searchability">
        /// An <c>Int32</c> representation of the data type searchability code.
        /// </param>
        /// <returns>
        /// <c>true</c> if the specified data type searchability code denotes
        /// that the corresponding data element is searchable in any way;
        /// otherwise, <c>false</c>.
        /// </returns>
        public static bool IsSearchable(int searchability)
        {
            return (searchability != (int)DataTypeSearchability.PredNone);
        }
        #endregion

        #region IsSearchableWithLike(int)
        /// <summary>
        /// Determines whether the specified searchability code
        /// indicates the corresponding data element is
        /// searchable with an SQL LIKE predicate.
        /// </summary>
        /// <param name="searchability">
        /// The JDBC searchability code.
        /// </param>
        /// <returns>
        /// <c>true</c> if the specified searchability indicates the
        /// corresponding data item is searchable with like;
        /// otherwise, <c>false</c>.
        /// </returns>
        public static bool IsSearchableWithLike(int searchability)
        {
            return ((searchability == (int)DataTypeSearchability.Searchable)
                    || (searchability == (int)DataTypeSearchability.PredChar));
        }
        #endregion

        #region IsNumberProviderType(int)
        /// <summary>
        /// Determines whether the given provider-specific data
        /// type code indicates that a corresponding data element
        /// represents some kind of number value.
        /// </summary>
        /// <param name="jdbcType">
        /// The JDBC data type code.
        /// </param>
        /// <returns>
        /// <c>true</c> if the given code denotes an SQL number data type;
        /// otherwise, <c>false</c>.
        /// </returns>
        public static bool IsNumberProviderType(int type)
        {
            return HsqlTypes.isNumberType(type);
        }
        #endregion

        #region IsTemporalProviderType(int)
        /// <summary>
        /// Determines whether the given provider-specific type code
        /// indicates that the corresponding data element represents
        /// a temporal value(i.e. has an SQL DATE, TIME, TIMESTAMP or
        /// INTERVAL value).
        /// </summary>
        /// <param name="type">
        /// A provider-specific data type code.
        /// </param>
        /// <returns>
        /// <c>true</c> if the given code denotes an SQL temporal data type;
        /// otherwise, <c>false</c>.
        /// </returns>
        public static bool IsTemporalProviderType(int type)
        {
            return (type == (int)HsqlProviderType.Date)
                   || (type == HsqlTypes.TIME)
                   || (type == HsqlTypes.TIMESTAMP);
        }
        #endregion

        #region IsProviderCharacterType(int)
        /// <summary>
        /// Determines whether the given provider-specific data type code
        /// indicates that the corresponding data element represents an
        /// SQL character value.
        /// </summary>
        /// <param name="type">
        /// A provider-specific data type code.
        /// </param>
        /// <returns>
        /// <c>true</c> if the given code denotes an SQL character data type;
        /// otherwise, <c>false</c>.
        /// </returns>
        public static bool IsProviderCharacterType(int type)
        {
            return HsqlTypes.isCharacterType(type);
        }
        #endregion

        #region ToCreateFormat(string,string)
        /// <summary>
        /// Retrieves the SQL create format character sequence corresponding to
        /// the given type name and comma-separated list of create parameters.
        /// </summary>
        /// <param name="typeName">
        /// Name of the SQL data type.
        /// </param>
        /// <param name="createParameters">
        /// The comma-separated list of SQL create parameters.
        /// </param>
        /// <returns></returns>
        public static string ToCreateFormat(string typeName, string createParameters)
        {
            int count = (createParameters == null)
                            ? 0
                            : 1 + createParameters.Split(Comma).Length;

            int last = count - 1;

            StringBuilder createFormat = new StringBuilder();

            for (int i = 0; i < count; i++)
            {
                if (i == 0)
                {
                    createFormat.Append('(');
                }
                else
                {
                    createFormat.Append(',');
                    createFormat.Append(' ');
                }

                createFormat.Append('{');
                createFormat.Append(i);
                createFormat.Append('}');

                if (i == last)
                {
                    createFormat.Append(')');
                }
            }

            return createFormat.ToString();
        }
        #endregion

        #region IsFixedLength(int)
        /// <summary>
        /// Determines whether the specified JDBC data type code represents an
        /// SQL fixed length data type.
        /// </summary>
        /// <param name="jdbcType">
        /// JDBC data type code
        /// </param>
        /// <returns>
        /// <c>true</c> if the specified JDBC type code represents an
        /// SQL fixed length data type; otherwise, <c>false</c>.
        /// </returns>
        public static bool IsFixedLength(int jdbcType)
        {

            switch (jdbcType)
            {
                case (int)HsqlProviderType.Array:
                case (int)HsqlProviderType.Blob:
                case (int)HsqlProviderType.Clob:
                case (int)HsqlProviderType.DataLink:
                case (int)HsqlProviderType.Distinct:
                case (int)HsqlProviderType.JavaObject:
                case (int)HsqlProviderType.LongVarBinary:
                case (int)HsqlProviderType.LongVarChar:
                case (int)HsqlProviderType.Object:
                case (int)HsqlProviderType.Ref:
                case (int)HsqlProviderType.Struct:
                case (int)HsqlProviderType.VarBinary:
                case (int)HsqlProviderType.VarChar:
                case (int)HsqlProviderType.Xml:
                    {
                        return false;
                    }
                default:
                    {
                        return true;
                    }
            }
        }
        #endregion

        #endregion Public Static Methods
    }
    #endregion
}