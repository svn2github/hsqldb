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
using System.Reflection;
using CN = System.Data.Common.DbMetaDataCollectionNames;
using HCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataCollectionNames;
using MDCN = System.Data.Common.DbMetaDataColumnNames;
using HMDCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataColumnNames;
#endregion

namespace System.Data.Hsqldb.Client.MetaData.Collection
{
    #region ReservedWordsCollection

    /// <summary>
    /// <para>
    /// Provides the <see cref="CN.ReservedWords"/> collection.
    /// </para>
    /// <img src="/ClassDiagrams/System.Data.Hsqldb.Client.MetaData.Collection.ReservedWordsCollection.png"
    ///      alt="ReservedWordsCollection Class Diagram"/>
    /// </summary>
    public class ReservedWordsCollection : Base.CachedMetadataCollection
    {
        #region Fields

        private static readonly string[] ReservedWords;
        private static readonly Dictionary<string, string> ReservedWordMap;

        #endregion

        #region Static Initializer

        /// <summary>
        /// Initializes reserved word lookup.
        /// </summary>
        static ReservedWordsCollection()
        {
            FieldInfo[] fields = typeof(ReservedWord).GetFields();

            List<string> values = new List<string>();

            Dictionary<string, string> map =
                new Dictionary<string, string>(
                StringComparer.InvariantCultureIgnoreCase);

            foreach (FieldInfo field in fields)
            {
                string value = (string)field.GetValue(null);

                values.Add(value);
                map.Add(value, value);
            }

            string[] result = new string[values.Count];

            values.CopyTo(result, 0);

            ReservedWords = result;
            ReservedWordMap = map;
        }

        #endregion

        #region ReservedWordsCollection()

        /// <summary>
        /// Initializes a new <see cref="ReservedWordsCollection"/> instance.
        /// </summary>
        public ReservedWordsCollection() : base() {}

        #endregion

        #region CreateTable()

        /// <summary>
        /// Creates a new <c>ReservedWords</c> metadata collection table.
        /// </summary>
        /// <returns>The table.</returns>
        public override DataTable CreateTable()
        {
            DataTable table = new DataTable(CN.ReservedWords);

            table.MinimumCapacity = ReservedWords.Length;

            DataColumnCollection cols = table.Columns;

            AddColumn(cols, null, MDCN.ReservedWord, typeof (string));

            return table;
        }

        #endregion

        #region FillTable(DataTable,string[])

        /// <summary>
        /// Fills a ReservedWords metadata collection table.
        /// </summary>
        /// <param name="connection">The connection.</param>
        /// <param name="table">The table.</param>
        /// <param name="restrictions">The restrictions.</param>
        public override void FillTable(HsqlConnection connection,
            DataTable table, string[] restrictions)
        {
            DataRowCollection rows = table.Rows;

            for (int i = 0; i < ReservedWords.Length; i++)
            {
                DataRow row = table.NewRow();

                row[MDCN.ReservedWord] = ReservedWords[i];

                rows.Add(row);

            }
        }

        #endregion

        #region AddRow(...)

        /// <summary>
        /// Adds a new row to the given ReservedWords metadata collection table.
        /// </summary>
        /// <param name="table">
        /// The table to which to add the new row.
        /// </param>
        /// <param name="reservedWord">
        /// The reserved word.
        /// </param>
        public static void AddRow(
            DataTable table,
            string reservedWord)
        {
            DataRow row = table.NewRow();

            row[MDCN.ReservedWord] = reservedWord;

            table.Rows.Add(row);
        }

        #endregion

        #region IsReservedWord(string)

        /// <summary>
        /// Determines whether the given <c>word</c> is an HSQLDB reserved word.
        /// </summary>
        /// <param name="word"></param>
        /// <returns>
        /// <c>true</c> if <c>word</c> is reserved;
        /// owtherwise <c>false</c>.
        /// </returns>
        public static bool IsReservedWord(string word)
        {
            return (string.IsNullOrEmpty(word))
                ? false
                : ReservedWordMap.ContainsKey(word.Trim());
        }

        #endregion

        #region ReservedWord

        /// <summary>
        /// SQL 92/99/2003 reserved words + HSQLDB-specific reserved words.
        /// </summary>
        public static class ReservedWord
        {
            /// <summary>
            /// Reserved in SQL 92/99.
            /// </summary>
            public const string ABSOLUTE = "ABSOLUTE";

            /// <summary>
            /// Reserved in SQL 92/99.
            /// </summary>
            public const string ACTION = "ACTION";

            /// <summary>
            /// Reserved in SQL 92/99/2003.
            /// </summary>
            public const string ADD = "ADD";

            /// <summary>
            /// Reserved in SQL 99.
            /// </summary>
            public const string AFTER = "AFTER";

            /// <summary>
            /// Reserved in SQL 92/99/2003.
            /// </summary>
            public const string ALL = "ALL";

            /// <summary>
            /// Reserved in SQL 92/99/2003.
            /// </summary>
            public const string ALLOCATE = "ALLOCATE";

            /// <summary>
            /// Reserved in SQL 92/99/2003.
            /// </summary>
            public const string ALTER = "ALTER";

            /// <summary>
            /// Reserved in SQL 92/99/2003.
            /// </summary>
            public const string AND = "AND";

            /// <summary>
            /// Reserved in SQL 92/99/2003.
            /// </summary>
            public const string ANY = "ANY";

            /// <summary>
            /// Reserved in SQL 92/99/2003.
            /// </summary>
            public const string ARE = "ARE";

            /// <summary>
            /// Reserved in SQL 99/2003.
            /// </summary>
            public const string ARRAY = "ARRAY";

            /// <summary>
            /// Reserved in SQL 92/99/2003.
            /// </summary>
            public const string AS = "AS";

            /// <summary>
            /// Reserved in SQL 92/99.
            /// </summary>
            public const string ASC = "ASC";

            /// <summary>
            /// Reserved in SQL 99/2003.
            /// </summary>
            public const string ASENSITIVE = "ASENSITIVE";

            /// <summary>
            /// Reserved in SQL 99/2003.
            /// </summary>
            public const string ASSERTION = "ASSERTION";

            /// <summary>
            /// Reserved in SQL 99/2003.
            /// </summary>
            public const string ASYMMETRIC = "ASYMMETRIC";

            /// <summary>
            /// Reserved in SQL 92/99/2003.
            /// </summary>
            public const string AT = "AT";

            /// <summary>
            /// Reserved in SQL 99/2003.
            /// </summary>
            public const string ATOMIC = "ATOMIC";

            /// <summary>
            /// Reserved in SQL 92/99/2003.
            /// </summary>
            public const string AUTHORIZATION = "AUTHORIZATION";

            /// <summary>
            /// Reserved in SQL 92.
            /// </summary>
            public const string AVG = "AVG";

            /// <summary>
            /// Reserved in SQL 99.
            /// </summary>
            public const string BEFORE = "BEFORE";

            /// <summary>
            /// Reserved in SQL 92/99/2003.
            /// </summary>
            public const string BEGIN = "BEGIN";

            /// <summary>
            /// Reserved in SQL 92/99/2003.
            /// </summary>
            public const string BETWEEN = "BETWEEN";

            /// <summary>
            /// Reserved in 2003.
            /// </summary>
            public const string BIGINT = "BIGINT";

            /// <summary>
            /// Reserved in SQL 99/2003.
            /// </summary>
            public const string BINARY = "BINARY";

            /// <summary>
            /// Reserved in SQL 92/99 (removed from 2003).
            /// </summary>
            public const string BIT = "BIT";

            /// <summary>
            /// Reserved in SQL 92.
            /// </summary>
            public const string BIT_LENGTH = "BIT_LENGTH";

            /// <summary>
            /// Reserved in SQL 99/2003.
            /// </summary>
            public const string BLOB = "BLOB";

            /// <summary>
            /// Reserved in SQL 99/2003.
            /// </summary>
            public const string BOOLEAN = "BOOLEAN";

            /// <summary>
            /// Reserved in SQL 92/99/2003.
            /// </summary>
            public const string BOTH = "BOTH";

            /// <summary>
            /// Reserved in SQL 99.
            /// </summary>
            public const string BREADTH = "BREADTH";

            /// <summary>
            /// Reserved in SQL 92/99/2003.
            /// </summary>
            public const string BY = "BY";

            /// <summary>
            /// Reserved in HSQLDB.
            /// </summary>
            public const string CACHED = "CACHED"; // HSQLDB-specific.

            /// <summary>
            /// Reserved in SQL 92/99/2003.
            /// </summary>
            public const string CALL = "CALL";

            /// <summary>
            /// Reserved in SQL 99/2003.
            /// </summary>
            public const string CALLED = "CALLED";

            /// <summary>
            /// Reserved in SQL 92/99.
            /// </summary>
            public const string CASCADE = "CASCADE";

            /// <summary>
            /// Reserved in SQL 92/99/2003.
            /// </summary>
            public const string CASCADED = "CASCADED";

            /// <summary>
            /// Reserved in SQL 92/99/2003.
            /// </summary>
            public const string CASE = "CASE";

            /// <summary>
            /// Reserved in SQL 92/99/2003.
            /// </summary>
            public const string CAST = "CAST";

            /// <summary>
            /// Reserved in SQL 92/99.
            /// </summary>
            public const string CATALOG = "CATALOG";

            /// <summary>
            /// Reserved in SQL 92/99/2003.
            /// </summary>
            public const string CHAR = "CHAR";

            /// <summary>
            /// Reserved in SQL 92.
            /// </summary>
            public const string CHAR_LENGTH = "CHAR_LENGTH";

            /// <summary>
            /// Reserved in SQL 92/99/2003.
            /// </summary>
            public const string CHARACTER = "CHARACTER";

            /// <summary>
            /// Reserved in SQL 92.
            /// </summary>
            public const string CHARACTER_LENGTH = "CHARACTER_LENGTH";

            /// <summary>
            /// Reserved in SQL 92/99/2003.
            /// </summary>
            public const string CHECK = "CHECK";

            /// <summary>
            /// Reserved in SQL 99/2003.
            /// </summary>
            public const string CLOB = "CLOB";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string CLOSE = "CLOSE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string COALESCE = "COALESCE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string COLLATE = "COLLATE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string COLLATION = "COLLATION";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string COLUMN = "COLUMN";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string COMMIT = "COMMIT";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string CONDITION = "CONDITION";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string CONNECT = "CONNECT";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string CONNECTION = "CONNECTION";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string CONSTRAINT = "CONSTRAINT";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string CONSTRAINTS = "CONSTRAINTS";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string CONSTRUCTOR = "CONSTRUCTOR";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string CONTAINS = "CONTAINS";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string CONTINUE = "CONTINUE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string CONVERT = "CONVERT";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string CORRESPONDING = "CORRESPONDING";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string COUNT = "COUNT";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string CREATE = "CREATE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string CROSS = "CROSS";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string CUBE = "CUBE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string CURRENT = "CURRENT";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string CURRENT_DATE = "CURRENT_DATE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string CURRENT_DEFAULT_TRANSFORM_GROUP = "CURRENT_DEFAULT_TRANSFORM_GROUP";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string CURRENT_PATH = "CURRENT_PATH";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string CURRENT_ROLE = "CURRENT_ROLE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string CURRENT_TIME = "CURRENT_TIME";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string CURRENT_TIMESTAMP = "CURRENT_TIMESTAMP";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string CURRENT_TRANSFORM_GROUP_FOR_TYPE = "CURRENT_TRANSFORM_GROUP_FOR_TYPE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string CURRENT_USER = "CURRENT_USER";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string CURSOR = "CURSOR";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string CYCLE = "CYCLE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string DATA = "DATA";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string DATE = "DATE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string DATETIME = "DATETIME"; // HSQLDB-specific.

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string DAY = "DAY";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string DEALLOCATE = "DEALLOCATE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string DEC = "DEC";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string DECIMAL = "DECIMAL";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string DECLARE = "DECLARE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string DEFAULT = "DEFAULT";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string DEFERRABLE = "DEFERRABLE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string DEFERRED = "DEFERRED";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string DELETE = "DELETE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string DEPTH = "DEPTH";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string DEREF = "DEREF";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string DESC = "DESC";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string DESCRIBE = "DESCRIBE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string DESCRIPTOR = "DESCRIPTOR";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string DETERMINISTIC = "DETERMINISTIC";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string DIAGNOSTICS = "DIAGNOSTICS";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string DISCONNECT = "DISCONNECT";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string DISTINCT = "DISTINCT";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string DO = "DO";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string DOMAIN = "DOMAIN";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string DOUBLE = "DOUBLE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string DROP = "DROP";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string DYNAMIC = "DYNAMIC";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string EACH = "EACH";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string ELEMENT = "ELEMENT";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string ELSE = "ELSE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string ELSEIF = "ELSEIF";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string END = "END";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            [CLSCompliant(false)]
            public const string EQUALS = "EQUALS";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string ESCAPE = "ESCAPE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string EXCEPT = "EXCEPT";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string EXCEPTION = "EXCEPTION";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string EXEC = "EXEC";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string EXECUTE = "EXECUTE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string EXISTS = "EXISTS";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string EXIT = "EXIT";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string EXTERNAL = "EXTERNAL";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string EXTRACT = "EXTRACT";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string FALSE = "FALSE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string FETCH = "FETCH";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string FILTER = "FILTER";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string FIRST = "FIRST";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string FLOAT = "FLOAT";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string FOR = "FOR";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string FOREIGN = "FOREIGN";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string FOUND = "FOUND";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string FREE = "FREE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string FROM = "FROM";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string FULL = "FULL";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string FUNCTION = "FUNCTION";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string GENERAL = "GENERAL";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string GET = "GET";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string GLOBAL = "GLOBAL";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string GO = "GO";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string GOTO = "GOTO";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string GRANT = "GRANT";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string GROUP = "GROUP";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string GROUPING = "GROUPING";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string HANDLER = "HANDLER";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string HAVING = "HAVING";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string HOLD = "HOLD";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string HOUR = "HOUR";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string IDENTITY = "IDENTITY";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string IF = "IF";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string IMMEDIATE = "IMMEDIATE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string IN = "IN";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string INDICATOR = "INDICATOR";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string INITIALLY = "INITIALLY";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string INNER = "INNER";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string INOUT = "INOUT";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string INPUT = "INPUT";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string INSENSITIVE = "INSENSITIVE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string INSERT = "INSERT";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string INT = "INT";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string INTEGER = "INTEGER";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string INTERSECT = "INTERSECT";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string INTERVAL = "INTERVAL";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string INTO = "INTO";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string IS = "IS";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string ISOLATION = "ISOLATION";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string ITERATE = "ITERATE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string JOIN = "JOIN";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string KEY = "KEY";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string LANGUAGE = "LANGUAGE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string LARGE = "LARGE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string LAST = "LAST";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string LATERAL = "LATERAL";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string LEADING = "LEADING";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string LEAVE = "LEAVE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string LEFT = "LEFT";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string LEVEL = "LEVEL";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string LIKE = "LIKE";

            /// <summary>
            /// Reserved in HSQLDB.
            /// </summary>
            public const string LIMIT = "LIMIT"; // HSQLDB-specific.

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string LOCAL = "LOCAL";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string LOCALTIME = "LOCALTIME";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string LOCALTIMESTAMP = "LOCALTIMESTAMP";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string LOCATOR = "LOCATOR";

            /// <summary>
            /// Reserved in HSQLDB.
            /// </summary>
            public const string LONGVARBINARY = "LONGVARBINARY"; // HSQLDB-specific.

            /// <summary>
            /// Reserved in HSQLDB.
            /// </summary>
            public const string LONGVARCHAR = "LONGVARCHAR"; // HSQLDB-specific.

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string LOOP = "LOOP";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string LOWER = "LOWER";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string MAP = "MAP";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string MATCH = "MATCH";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string MAX = "MAX";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string MEMBER = "MEMBER";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string MERGE = "MERGE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string METHOD = "METHOD";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string MIN = "MIN";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string MINUTE = "MINUTE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string MODIFIES = "MODIFIES";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string MODULE = "MODULE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string MONTH = "MONTH";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string MULTISET = "MULTISET";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string NAMES = "NAMES";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string NATIONAL = "NATIONAL";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string NATURAL = "NATURAL";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string NCHAR = "NCHAR";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string NCLOB = "NCLOB";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string NEW = "NEW";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string NEXT = "NEXT";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string NO = "NO";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string NONE = "NONE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string NOT = "NOT";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string NULL = "NULL";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string NULLIF = "NULLIF";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string NUMERIC = "NUMERIC";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string OBJECT = "OBJECT";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string OCTET_LENGTH = "OCTET_LENGTH";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string OF = "OF";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string OLD = "OLD";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string ON = "ON";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string ONLY = "ONLY";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string OPEN = "OPEN";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string OPTION = "OPTION";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string OR = "OR";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string ORDER = "ORDER";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string ORDINALITY = "ORDINALITY";

            /// <summary>
            /// Reserved in HSQLDB.
            /// </summary>
            public const string OTHER = "OTHER"; // HSQLDB-specific.

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string OUT = "OUT";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string OUTER = "OUTER";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string OUTPUT = "OUTPUT";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string OVER = "OVER";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string OVERLAPS = "OVERLAPS";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string PAD = "PAD";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string PARAMETER = "PARAMETER";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string PARTIAL = "PARTIAL";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string PARTITION = "PARTITION";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string PATH = "PATH";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string POSITION = "POSITION";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string PRECISION = "PRECISION";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string PREPARE = "PREPARE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string PRESERVE = "PRESERVE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string PRIMARY = "PRIMARY";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string PRIOR = "PRIOR";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string PRIVILEGES = "PRIVILEGES";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string PROCEDURE = "PROCEDURE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string PUBLIC = "PUBLIC";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string RANGE = "RANGE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string READ = "READ";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string READS = "READS";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string REAL = "REAL";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string RECURSIVE = "RECURSIVE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string REF = "REF";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string REFERENCES = "REFERENCES";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string REFERENCING = "REFERENCING";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string RELATIVE = "RELATIVE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string RELEASE = "RELEASE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string REPEAT = "REPEAT";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string RESIGNAL = "RESIGNAL";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string RESTRICT = "RESTRICT";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string RESULT = "RESULT";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string RETURN = "RETURN";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string RETURNS = "RETURNS";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string REVOKE = "REVOKE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string RIGHT = "RIGHT";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string ROLE = "ROLE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string ROLLBACK = "ROLLBACK";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string ROLLUP = "ROLLUP";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string ROUTINE = "ROUTINE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string ROW = "ROW";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string ROWS = "ROWS";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string SAVEPOINT = "SAVEPOINT";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string SCHEMA = "SCHEMA";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string SCOPE = "SCOPE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string SCROLL = "SCROLL";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string SEARCH = "SEARCH";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string SECOND = "SECOND";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string SECTION = "SECTION";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string SELECT = "SELECT";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string SENSITIVE = "SENSITIVE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string SESSION = "SESSION";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string SESSION_USER = "SESSION_USER";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string SET = "SET";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string SETS = "SETS";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string SIGNAL = "SIGNAL";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string SIMILAR = "SIMILAR";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string SIZE = "SIZE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string SMALLINT = "SMALLINT";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string SOME = "SOME";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string SPACE = "SPACE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string SPECIFIC = "SPECIFIC";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string SPECIFICTYPE = "SPECIFICTYPE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string SQL = "SQL";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string SQLCODE = "SQLCODE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string SQLERROR = "SQLERROR";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string SQLEXCEPTION = "SQLEXCEPTION";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string SQLSTATE = "SQLSTATE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string SQLWARNING = "SQLWARNING";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string START = "START";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string STATE = "STATE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string STATIC = "STATIC";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string SUBMULTISET = "SUBMULTISET";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string SUBSTRING = "SUBSTRING";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string SUM = "SUM";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string SYMMETRIC = "SYMMETRIC";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string SYSTEM = "SYSTEM";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string SYSTEM_USER = "SYSTEM_USER";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string TABLE = "TABLE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string TABLESAMPLE = "TABLESAMPLE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string TEMP = "TEMP"; // HSQLDB-specific

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string TEMPORARY = "TEMPORARY";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string TEXT = "TEXT"; // HSQLDB-specific

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string THEN = "THEN";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string TIME = "TIME";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string TIMESTAMP = "TIMESTAMP";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string TIMEZONE_HOUR = "TIMEZONE_HOUR";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string TIMEZONE_MINUTE = "TIMEZONE_MINUTE";

            /// <summary>
            /// Reserved in HSQLDB.
            /// </summary>
            public const string TINYINT = "TINYINT"; // HSQLDB-specific.

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string TO = "TO";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string TOP = "TOP"; // HSQLDB-specific.

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string TRAILING = "TRAILING";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string TRANSACTION = "TRANSACTION";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string TRANSLATE = "TRANSLATE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string TRANSLATION = "TRANSLATION";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string TREAT = "TREAT";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string TRIGGER = "TRIGGER";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string TRIM = "TRIM";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string TRUE = "TRUE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string UNDER = "UNDER";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string UNDO = "UNDO";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string UNION = "UNION";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string UNIQUE = "UNIQUE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string UNKNOWN = "UNKNOWN";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string UNNEST = "UNNEST";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string UNTIL = "UNTIL";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string UPDATE = "UPDATE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string UPPER = "UPPER";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string USAGE = "USAGE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string USER = "USER";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string USING = "USING";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string VALUE = "VALUE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string VALUES = "VALUES";

            /// <summary>
            /// Reserved in HSQLDB
            /// </summary>
            public const string VARBINARY = "VARBINARY"; // HSQLDB-specific.

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string VARCHAR = "VHARCHAR";

            /// <summary>
            /// Reserved in HSQLDB.
            /// </summary>
            public const string VARCHAR_IGNORECASE = "VARCHAR_IGNORECASE"; // HSQLDB-specific

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string VARYING = "VARYING";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string VIEW = "VIEW";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string WHEN = "WHEN";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string WHENEVER = "WHENEVER";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string WHERE = "WHERE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string WHILE = "WHILE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string WINDOW = "WINDOW";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string WITH = "WITH";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string WITHIN = "WITHIN";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string WITHOUT = "WITHOUT";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string WORK = "WORK";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string WRITE = "WRITE";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string YEAR = "YEAR";

            /// <summary>
            /// Reserved in SQL
            /// </summary>
            public const string ZONE = "ZONE";
        }

        #endregion
    }

    #endregion
}