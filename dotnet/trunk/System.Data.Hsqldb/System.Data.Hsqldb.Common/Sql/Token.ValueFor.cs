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

namespace System.Data.Hsqldb.Common.Sql
{
    public partial class Token
    {
        /// <summary>
        /// String constants corresponding to <c>special</c>, 
        /// <c>SQL:200n reserved word</c> and <c>other</c> 
        /// common HSQLDB language tokens.
        /// </summary>
        /// <remarks>
        /// Note that the classification of each token value is provided
        /// in its summary documentation section.
        /// </remarks>
        public static class ValueFor
        {
            #region Constants

            #region Special Tokens
            // SPECIAL
            /// <summary>Token classification: special</summary>
            /// <value>"*"</value>
            public const string ASTERISK = "*";
            /// <summary>Token classification: special</summary>
            /// <value>","</value>
            public const string COMMA = ",";
            /// <summary>Token classification: special</summary>
            /// <value>")"</value>
            public const string CLOSEBRACKET = ")";
            /// <summary>Token classification: special</summary>
            /// <value>"="</value>
            [CLSCompliant(false)]
            public const string EQUALS = "=";
            /// <summary>Token classification: special</summary>
            /// <value>"/"</value>
            public const string DIVIDE = "/";
            /// <summary>Token classification: special</summary>
            /// <value>"("</value>
            public const string OPENBRACKET = "(";
            /// <summary>Token classification: special</summary>
            /// <value>";"</value>
            public const string SEMICOLON = ";";
            /// <summary>Token classification: special</summary>
            /// <value>"*"</value>
            public const string MULTIPLY = "*";
            /// <summary>Token classification: special</summary>
            /// <value>"%"</value>
            public const string PERCENT = "%";
            /// <summary>Token classification: special</summary>
            /// <value>"+"</value>
            public const string PLUS = "+";
            /// <summary>Token classification: special</summary>
            /// <value>"?"</value>
            public const string QUESTION = "?";
            /// <summary>Token classification: special</summary>
            /// <value>"."</value>
            public const string PERIOD = ".";
            #endregion

            #region SQL 200n Reserved Word Tokens
            // SQL 200n reserved word tokens
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"ADD"</value>
            public const string ADD = "ADD"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"ALL"</value>
            public const string ALL = "ALL"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"ALLOCATE"</value>
            public const string ALLOCATE = "ALLOCATE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"ALTER"</value>
            public const string ALTER = "ALTER"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"AND"</value>
            public const string AND = "AND"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"ANY"</value>
            public const string ANY = "ANY"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"ARE"</value>
            public const string ARE = "ARE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"ARRAY"</value>
            public const string ARRAY = "ARRAY"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"AS"</value>
            public const string AS = "AS"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"ASENSITIVE"</value>
            public const string ASENSITIVE = "ASENSITIVE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"ASYMMETRIC"</value>
            public const string ASYMMETRIC = "ASYMMETRIC"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"AT"</value>
            public const string AT = "AT"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"ATOMIC"</value>
            public const string ATOMIC = "ATOMIC"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"AUTHORIZATION"</value>
            public const string AUTHORIZATION = "AUTHORIZATION"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"BEGIN"</value>
            public const string BEGIN = "BEGIN"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"BETWEEN"</value>
            public const string BETWEEN = "BETWEEN"; // reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"BIGINT"</value>
            public const string BIGINT = "BIGINT"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"BINARY"</value>
            public const string BINARY = "BINARY"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"BLOB"</value>
            public const string BLOB = "BLOB"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"BOOLEAN"</value>
            public const string BOOLEAN = "BOOLEAN"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"BOTH"</value>
            public const string BOTH = "BOTH"; // reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"BY"</value>
            public const string BY = "BY"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"CALL"</value>
            public const string CALL = "CALL"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"CALLED"</value>
            public const string CALLED = "CALLED"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"CASCADED"</value>
            public const string CASCADED = "CASCADED"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"CASE"</value>
            public const string CASE = "CASE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"CAST"</value>
            public const string CAST = "CAST"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"CHAR"</value>
            public const string CHAR = "CHAR"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"CHARACTER"</value>
            public const string CHARACTER = "CHARACTER"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"CHECK"</value>
            public const string CHECK = "CHECK"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"CLOB"</value>
            public const string CLOB = "CLOB"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"CLOSE"</value>
            public const string CLOSE = "CLOSE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"COLLATE"</value>
            public const string COLLATE = "COLLATE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"COLUMN"</value>
            public const string COLUMN = "COLUMN"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"COMMIT"</value>
            public const string COMMIT = "COMMIT"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"CONDIITON"</value>
            public const string CONDITION = "CONDIITON"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"CONNECT"</value>
            public const string CONNECT = "CONNECT"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"CONSTRAINT"</value>
            public const string CONSTRAINT = "CONSTRAINT"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"CONTINUE"</value>
            public const string CONTINUE = "CONTINUE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"CORRESPONDING"</value>
            public const string CORRESPONDING = "CORRESPONDING"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"CREATE"</value>
            public const string CREATE = "CREATE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"CROSS"</value>
            public const string CROSS = "CROSS"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"CUBE"</value>
            public const string CUBE = "CUBE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"CURRENT"</value>
            public const string CURRENT = "CURRENT"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"CURRENT_DATE"</value>
            public const string CURRENT_DATE = "CURRENT_DATE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"CURRENT_DEFAULT_TRANSFORM_GROUP"</value>
            public const string CURRENT_DEFAULT_TRANSFORM_GROUP
                = "CURRENT_DEFAULT_TRANSFORM_GROUP"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"CURRENT_PATH"</value>
            public const string CURRENT_PATH = "CURRENT_PATH"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"CURRENT_ROLE"</value>
            public const string CURRENT_ROLE = "CURRENT_ROLE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"CURRENT_TIME"</value>
            public const string CURRENT_TIME = "CURRENT_TIME"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"CURRENT_TIMESTAMP"</value>
            public const string CURRENT_TIMESTAMP = "CURRENT_TIMESTAMP"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"CURRENT_TRANSFORM_GROUP_FOR_TYPE"</value>
            public const string CURRENT_TRANSFORM_GROUP_FOR_TYPE =
                "CURRENT_TRANSFORM_GROUP_FOR_TYPE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"CURRENT_USER"</value>
            public const string CURRENT_USER = "CURRENT_USER"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"CURSOR"</value>
            public const string CURSOR = "CURSOR"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"CYCLE"</value>
            public const string CYCLE = "CYCLE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"DATE"</value>
            public const string DATE = "DATE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"DAY"</value>
            public const string DAY = "DAY"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"DEALLOCATE"</value>
            public const string DEALLOCATE = "DEALLOCATE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"DEC"</value>
            public const string DEC = "DEC"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"DECIMAL"</value>
            public const string DECIMAL = "DECIMAL"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"DECLARE"</value>
            public const string DECLARE = "DECLARE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"DEFAULT"</value>
            public const string DEFAULT = "DEFAULT"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"DELETE"</value>
            public const string DELETE = "DELETE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"DEREF"</value>
            public const string DEREF = "DEREF"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"DESCRIBE"</value>
            public const string DESCRIBE = "DESCRIBE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"DETERMINISTIC"</value>
            public const string DETERMINISTIC = "DETERMINISTIC"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"DISCONNECT"</value>
            public const string DISCONNECT = "DISCONNECT"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"DISTINCT"</value>
            public const string DISTINCT = "DISTINCT"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"DO"</value>
            public const string DO = "DO"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"DOUBLE"</value>
            public const string DOUBLE = "DOUBLE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"DAYOFWEEK"</value>
            public const string DAYOFWEEK = "DAYOFWEEK"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"DROP"</value>
            public const string DROP = "DROP"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"DYNAMIC"</value>
            public const string DYNAMIC = "DYNAMIC"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"EACH"</value>
            public const string EACH = "EACH"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"ELEMENT"</value>
            public const string ELEMENT = "ELEMENT"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"ELSE"</value>
            public const string ELSE = "ELSE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"ELSEIF"</value>
            public const string ELSEIF = "ELSEIF"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"END"</value>
            public const string END = "END"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"ESCAPE"</value>
            public const string ESCAPE = "ESCAPE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"EXCEPT"</value>
            public const string EXCEPT = "EXCEPT"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"EXEC"</value>
            public const string EXEC = "EXEC"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"EXECUTE"</value>
            public const string EXECUTE = "EXECUTE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"EXISTS"</value>
            public const string EXISTS = "EXISTS"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"EXIT"</value>
            public const string EXIT = "EXIT"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"EXTERNAL"</value>
            public const string EXTERNAL = "EXTERNAL"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"FALSE"</value>
            public const string FALSE = "FALSE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"FETCH"</value>
            public const string FETCH = "FETCH"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"FILTER"</value>
            public const string FILTER = "FILTER"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"FLOAT"</value>
            public const string FLOAT = "FLOAT"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"FOR"</value>
            public const string FOR = "FOR"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"FOREIGN"</value>
            public const string FOREIGN = "FOREIGN"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"FREE"</value>
            public const string FREE = "FREE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"FROM"</value>
            public const string FROM = "FROM"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"FULL"</value>
            public const string FULL = "FULL"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"FUNCTION"</value>
            public const string FUNCTION = "FUNCTION"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"GET"</value>
            public const string GET = "GET"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"GLOBAL"</value>
            public const string GLOBAL = "GLOBAL"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"GRANT"</value>
            public const string GRANT = "GRANT"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"GROUP"</value>
            public const string GROUP = "GROUP"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"GROUPING"</value>
            public const string GROUPING = "GROUPING"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"HANDLER"</value>
            public const string HANDLER = "HANDLER"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"HAVING"</value>
            public const string HAVING = "HAVING"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"HEADER"</value>
            public const string HEADER = "HEADER"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"HOLD"</value>
            public const string HOLD = "HOLD"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"HOUR"</value>
            public const string HOUR = "HOUR"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"IDENTITY"</value>
            public const string IDENTITY = "IDENTITY"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"IF"</value>
            public const string IF = "IF"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"IMMEDIATE"</value>
            public const string IMMEDIATE = "IMMEDIATE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"IN"</value>
            public const string IN = "IN"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"INDICATOR"</value>
            public const string INDICATOR = "INDICATOR"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"INNER"</value>
            public const string INNER = "INNER"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"INOUT"</value>
            public const string INOUT = "INOUT"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"INPUT"</value>
            public const string INPUT = "INPUT"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"INSENSITIVE"</value>
            public const string INSENSITIVE = "INSENSITIVE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"INSERT"</value>
            public const string INSERT = "INSERT"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"INT"</value>
            public const string INT = "INT"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"INTEGER"</value>
            public const string INTEGER = "INTEGER"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"INTERSECT"</value>
            public const string INTERSECT = "INTERSECT"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"INTERVAL"</value>
            public const string INTERVAL = "INTERVAL"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"INTO"</value>
            public const string INTO = "INTO"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"IS"</value>
            public const string IS = "IS"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"ITERATE"</value>
            public const string ITERATE = "ITERATE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"JOIN"</value>
            public const string JOIN = "JOIN"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"LANGUAGE"</value>
            public const string LANGUAGE = "LANGUAGE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"LARGE"</value>
            public const string LARGE = "LARGE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"LATERAL"</value>
            public const string LATERAL = "LATERAL"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"LEADING"</value>
            public const string LEADING = "LEADING"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"LEAVE"</value>
            public const string LEAVE = "LEAVE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"LEFT"</value>
            public const string LEFT = "LEFT"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"LIKE"</value>
            public const string LIKE = "LIKE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"LOCAL"</value>
            public const string LOCAL = "LOCAL"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"LOCALTIME"</value>
            public const string LOCALTIME = "LOCALTIME"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"LOCALTIMESTAMP"</value>
            public const string LOCALTIMESTAMP = "LOCALTIMESTAMP"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"LOOP"</value>
            public const string LOOP = "LOOP"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"MATCH"</value>
            public const string MATCH = "MATCH"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"MEMBER"</value>
            public const string MEMBER = "MEMBER"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"METHOD"</value>
            public const string METHOD = "METHOD"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"MINUTE"</value>
            public const string MINUTE = "MINUTE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"MODIFIES"</value>
            public const string MODIFIES = "MODIFIES"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"MODULE"</value>
            public const string MODULE = "MODULE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"MONTH"</value>
            public const string MONTH = "MONTH"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"MULTISET"</value>
            public const string MULTISET = "MULTISET"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"NATIONAL"</value>
            public const string NATIONAL = "NATIONAL"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"NAUTRAL"</value>
            public const string NATURAL = "NAUTRAL"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"NCHAR"</value>
            public const string NCHAR = "NCHAR"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"NCLOB"</value>
            public const string NCLOB = "NCLOB"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"NEW"</value>
            public const string NEW = "NEW"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"NEXT"</value>
            public const string NEXT = "NEXT"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"NO"</value>
            public const string NO = "NO"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"NONE"</value>
            public const string NONE = "NONE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"NOT"</value>
            public const string NOT = "NOT"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"NULL"</value>
            public const string NULL = "NULL"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"NUMERIC"</value>
            public const string NUMERIC = "NUMERIC"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"OF"</value>
            public const string OF = "OF"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"OLD"</value>
            public const string OLD = "OLD"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"ON"</value>
            public const string ON = "ON"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"ONLY"</value>
            public const string ONLY = "ONLY"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"OPEN"</value>
            public const string OPEN = "OPEN"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"OR"</value>
            public const string OR = "OR"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"ORDER"</value>
            public const string ORDER = "ORDER"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"OUT"</value>
            public const string OUT = "OUT"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"OUTER"</value>
            public const string OUTER = "OUTER"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"OUTPUT"</value>
            public const string OUTPUT = "OUTPUT"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"OVER"</value>
            public const string OVER = "OVER"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"OVERLAPS"</value>
            public const string OVERLAPS = "OVERLAPS"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"PARAMETER"</value>
            public const string PARAMETER = "PARAMETER"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"PARTITION"</value>
            public const string PARTITION = "PARTITION"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"PRECISION"</value>
            public const string PRECISION = "PRECISION"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"PREPARE"</value>
            public const string PREPARE = "PREPARE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"PRIMARY"</value>
            public const string PRIMARY = "PRIMARY"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"PROCEDURE"</value>
            public const string PROCEDURE = "PROCEDURE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"RANGE"</value>
            public const string RANGE = "RANGE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"READS"</value>
            public const string READS = "READS"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"REAL"</value>
            public const string REAL = "REAL"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"RECURSIVE"</value>
            public const string RECURSIVE = "RECURSIVE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"REF"</value>
            public const string REF = "REF"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"REFERENCES"</value>
            public const string REFERENCES = "REFERENCES"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"REFERENCING"</value>
            public const string REFERENCING = "REFERENCING"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"RELEASE"</value>
            public const string RELEASE = "RELEASE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"REPEAT"</value>
            public const string REPEAT = "REPEAT"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"RESIGNAL"</value>
            public const string RESIGNAL = "RESIGNAL"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"RESULT"</value>
            public const string RESULT = "RESULT"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"RETURN"</value>
            public const string RETURN = "RETURN"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"RETURNS"</value>
            public const string RETURNS = "RETURNS"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"REVOKE"</value>
            public const string REVOKE = "REVOKE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"RIGHT"</value>
            public const string RIGHT = "RIGHT"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"ROLLBACK"</value>
            public const string ROLLBACK = "ROLLBACK"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"ROLLUP"</value>
            public const string ROLLUP = "ROLLUP"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"ROW"</value>
            public const string ROW = "ROW"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"ROWS"</value>
            public const string ROWS = "ROWS"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"SAVEPOINT"</value>
            public const string SAVEPOINT = "SAVEPOINT"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"SCOPE"</value>
            public const string SCOPE = "SCOPE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"SCROLL"</value>
            public const string SCROLL = "SCROLL"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"SECOND"</value>
            public const string SECOND = "SECOND"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"SEARCH"</value>
            public const string SEARCH = "SEARCH"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"SELECT"</value>
            public const string SELECT = "SELECT"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"SENSITIVE"</value>
            public const string SENSITIVE = "SENSITIVE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"SESSION_USER"</value>
            public const string SESSION_USER = "SESSION_USER"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"SET"</value>
            public const string SET = "SET"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"SIGNAL"</value>
            public const string SIGNAL = "SIGNAL"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"SIMILAR"</value>
            public const string SIMILAR = "SIMILAR"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"SMALLINT"</value>
            public const string SMALLINT = "SMALLINT"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"SOME"</value>
            public const string SOME = "SOME"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"SPECIFIC"</value>
            public const string SPECIFIC = "SPECIFIC"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"SPECIFICTYPE"</value>
            public const string SPECIFICTYPE = "SPECIFICTYPE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"SQL"</value>
            public const string SQL = "SQL"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"SQLEXCEPTION"</value>
            public const string SQLEXCEPTION = "SQLEXCEPTION"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"SQLSTATE"</value>
            public const string SQLSTATE = "SQLSTATE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"SQLWARNING"</value>
            public const string SQLWARNING = "SQLWARNING"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"START"</value>
            public const string START = "START"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"STATIC"</value>
            public const string STATIC = "STATIC"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"SUBMULTISET"</value>
            public const string SUBMULTISET = "SUBMULTISET"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"SYMMETRIC"</value>
            public const string SYMMETRIC = "SYMMETRIC"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"SYSTEM"</value>
            public const string SYSTEM = "SYSTEM"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"SYSTEM_USER"</value>
            public const string SYSTEM_USER = "SYSTEM_USER"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"TABLE"</value>
            public const string TABLE = "TABLE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"TABLESAMPLE"</value>
            public const string TABLESAMPLE = "TABLESAMPLE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"THEN"</value>
            public const string THEN = "THEN"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"TIME"</value>
            public const string TIME = "TIME"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"TIMESTAMP"</value>
            public const string TIMESTAMP = "TIMESTAMP"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"TIMEZONE_HOUR"</value>
            public const string TIMEZONE_HOUR = "TIMEZONE_HOUR"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"TIMEZONE_MINUTE"</value>
            public const string TIMEZONE_MINUTE = "TIMEZONE_MINUTE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"TO"</value>
            public const string TO = "TO"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"TRAILING"</value>
            public const string TRAILING = "TRAILING"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"TRANSLATION"</value>
            public const string TRANSLATION = "TRANSLATION"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"TREAT"</value>
            public const string TREAT = "TREAT"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"TRIGGER"</value>
            public const string TRIGGER = "TRIGGER"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"TRUE"</value>
            public const string TRUE = "TRUE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"UNDO"</value>
            public const string UNDO = "UNDO"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"UNION"</value>
            public const string UNION = "UNION"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"UNIQUE"</value>
            public const string UNIQUE = "UNIQUE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"UNKNOWN"</value>
            public const string UNKNOWN = "UNKNOWN"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"UNNEST"</value>
            public const string UNNEST = "UNNEST"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"UNTIL"</value>
            public const string UNTIL = "UNTIL"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"UPDATE"</value>
            public const string UPDATE = "UPDATE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"USER"</value>
            public const string USER = "USER"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"USING"</value>
            public const string USING = "USING"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"VALUE"</value>
            public const string VALUE = "VALUE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"VALUES"</value>
            public const string VALUES = "VALUES"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"VARCHAR"</value>
            public const string VARCHAR = "VARCHAR"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"VARYING"</value>
            public const string VARYING = "VARYING"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"WHEN"</value>
            public const string WHEN = "WHEN"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"WHENEVER"</value>
            public const string WHENEVER = "WHENEVER"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"WHERE"</value>
            public const string WHERE = "WHERE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"WHILE"</value>
            public const string WHILE = "WHILE"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"WINDOW"</value>
            public const string WINDOW = "WINDOW"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"WITH"</value>
            public const string WITH = "WITH"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"WITHIN"</value>
            public const string WITHIN = "WITHIN"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"WITHOUT"</value>
            public const string WITHOUT = "WITHOUT"; //reserved
            ///<summary>Token classification: SQL 200n reserved word</summary>
            /// <value>"YEAR"</value>
            public const string YEAR = "YEAR"; //reserved 
            #endregion

            #region Other Tokens
            /// <summary>Token classification: other</summary>
            /// <value>"ALWAYS"</value>
            public const string ALWAYS = "ALWAYS";
            /// <summary>Token classification: other</summary>
            /// <value>"ACTION"</value>
            public const string ACTION = "ACTION";
            /// <summary>Token classification: other</summary>
            /// <value>"ADMIN"</value>
            public const string ADMIN = "ADMIN";
            /// <summary>Token classification: other</summary>
            /// <value>"AFTER"</value>
            public const string AFTER = "AFTER";
            /// <summary>Token classification: other</summary>
            /// <value>"ALIAS"</value>
            public const string ALIAS = "ALIAS";
            /// <summary>Token classification: other</summary>
            /// <value>"ASC"</value>
            public const string ASC = "ASC";
            /// <summary>Token classification: other</summary>
            /// <value>"AUTOCOMMIT"</value>
            public const string AUTOCOMMIT = "AUTOCOMMIT";
            /// <summary>Token classification: other</summary>
            /// <value>"AVG"</value>
            public const string AVG = "AVG";
            /// <summary>Token classification: other</summary>
            /// <value>"BACKUP"</value>
            public const string BACKUP = "BACKUP";
            /// <summary>Token classification: other</summary>
            /// <value>"BEFORE"</value>
            public const string BEFORE = "BEFORE";
            /// <summary>Token classification: other</summary>
            /// <value>"CACHED"</value>
            public const string CACHED = "CACHED";
            /// <summary>Token classification: other</summary>
            /// <value>"CASCADE"</value>
            public const string CASCADE = "CASCADE";
            /// <summary>Token classification: other</summary>
            /// <value>"CASEWHEN"</value>
            public const string CASEWHEN = "CASEWHEN";
            /// <summary>Token classification: other</summary>
            /// <value>"CHECKPOINT"</value>
            public const string CHECKPOINT = "CHECKPOINT";
            /// <summary>Token classification: other</summary>
            /// <value>"CLASS"</value>
            public const string CLASS = "CLASS";
            /// <summary>Token classification: other</summary>
            /// <value>"COALESCE"</value>
            public const string COALESCE = "COALESCE";
            /// <summary>Token classification: other</summary>
            /// <value>"COLLATION"</value>
            public const string COLLATION = "COLLATION";
            /// <summary>Token classification: other</summary>
            /// <value>"COMPACT"</value>
            public const string COMPACT = "COMPACT";
            /// <summary>Token classification: other</summary>
            /// <value>"COMPRESSED"</value>
            public const string COMPRESSED = "COMPRESSED";
            /// <summary>Token classification: other</summary>
            /// <value>"CONVERT"</value>
            public const string CONVERT = "CONVERT";
            /// <summary>Token classification: other</summary>
            /// <value>"COUNT"</value>
            public const string COUNT = "COUNT";
            /// <summary>Token classification: other</summary>
            /// <value>"DATABASE"</value>
            public const string DATABASE = "DATABASE";
            /// <summary>Token classification: other</summary>
            /// <value>"DEFRAG"</value>
            public const string DEFRAG = "DEFRAG";
            /// <summary>Token classification: other</summary>
            /// <value>"DESC"</value>
            public const string DESC = "DESC";
            /// <summary>Token classification: other</summary>
            /// <value>"EVERY"</value>
            public const string EVERY = "EVERY";
            /// <summary>Token classification: other</summary>
            /// <value>"EXPLAIN"</value>
            public const string EXPLAIN = "EXPLAIN";
            /// <summary>Token classification: other</summary>
            /// <value>"EXTRACT"</value>
            public const string EXTRACT = "EXTRACT";
            /// <summary>Token classification: other</summary>
            /// <value>"GENERATED"</value>
            public const string GENERATED = "GENERATED";
            /// <summary>Token classification: other</summary>
            /// <value>"IFNULL"</value>
            public const string IFNULL = "IFNULL";
            /// <summary>Token classification: other</summary>
            /// <value>"IGNORECASE"</value>
            public const string IGNORECASE = "IGNORECASE";
            /// <summary>Token classification: other</summary>
            /// <value>"IMMEDIATELY"</value>
            public const string IMMEDIATELY = "IMMEDIATELY";
            /// <summary>Token classification: other</summary>
            /// <value>"INCREMENT"</value>
            public const string INCREMENT = "INCREMENT";
            /// <summary>Token classification: other</summary>
            /// <value>"INDEX"</value>
            public const string INDEX = "INDEX";
            /// <summary>Token classification: other</summary>
            /// <value>"INITIAL"</value>
            public const string INITIAL = "INITIAL";
            /// <summary>Token classification: other</summary>
            /// <value>"KEY"</value>
            public const string KEY = "KEY";
            /// <summary>Token classification: other</summary>
            /// <value>"LIMIT"</value>
            public const string LIMIT = "LIMIT";
            /// <summary>Token classification: other</summary>
            /// <value>"LOGSIZE"</value>
            public const string LOGSIZE = "LOGSIZE";
            /// <summary>Token classification: other</summary>
            /// <value>"MATCHED"</value>
            public const string MATCHED = "MATCHED";
            /// <summary>Token classification: other</summary>
            /// <value>"MAX"</value>
            public const string MAX = "MAX";
            /// <summary>Token classification: other</summary>
            /// <value>"MAXROWS"</value>
            public const string MAXROWS = "MAXROWS";
            /// <summary>Token classification: other</summary>
            /// <value>"MEMORY"</value>
            public const string MEMORY = "MEMORY";
            /// <summary>Token classification: other</summary>
            /// <value>"MERGE"</value>
            public const string MERGE = "MERGE";
            /// <summary>Token classification: other</summary>
            /// <value>"MIN"</value>
            public const string MIN = "MIN";
            /// <summary>Token classification: other</summary>
            /// <value>"MINUS"</value>
            public const string MINUS = "MINUS";
            /// <summary>Token classification: other</summary>
            /// <value>"NOW"</value>
            public const string NOW = "NOW";
            /// <summary>Token classification: other</summary>
            /// <value>"NOWAIT"</value>
            public const string NOWAIT = "NOWAIT";
            /// <summary>Token classification: other</summary>
            /// <value>"NULLIF"</value>
            public const string NULLIF = "NULLIF";
            /// <summary>Token classification: other</summary>
            /// <value>"NVL"</value>
            public const string NVL = "NVL";
            /// <summary>Token classification: other</summary>
            /// <value>"OFFSET"</value>
            public const string OFFSET = "OFFSET";
            /// <summary>Token classification: other</summary>
            /// <value>"PASSWORD"</value>
            public const string PASSWORD = "PASSWORD";
            /// <summary>Token classification: other</summary>
            /// <value>"SCHEMA"</value>
            public const string SCHEMA = "SCHEMA";
            /// <summary>Token classification: other</summary>
            /// <value>"PLAN"</value>
            public const string PLAN = "PLAN";
            /// <summary>Token classification: other</summary>
            /// <value>"PRESERVE"</value>
            public const string PRESERVE = "PRESERVE";
            /// <summary>Token classification: other</summary>
            /// <value>"PRIVILEGES"</value>
            public const string PRIVILEGES = "PRIVILEGES";
            /// <summary>Token classification: other</summary>
            /// <value>"POSITION"</value>
            public const string POSITION = "POSITION";
            /// <summary>Token classification: other</summary>
            /// <value>"PROPERTY"</value>
            public const string PROPERTY = "PROPERTY";
            /// <summary>Token classification: other</summary>
            /// <value>"PUBLIC"</value>
            public const string PUBLIC = "PUBLIC";
            /// <summary>Token classification: other</summary>
            /// <value>"QUEUE"</value>
            public const string QUEUE = "QUEUE";
            /// <summary>Token classification: other</summary>
            /// <value>"READONLY"</value>
            public const string READONLY = "READONLY";
            /// <summary>Token classification: other</summary>
            /// <value>"REFERENTIAL_INTEGRITY"</value>
            public const string REFERENTIAL_INTEGRITY = "REFERENTIAL_INTEGRITY";
            /// <summary>Token classification: other</summary>
            /// <value>"RENAME"</value>
            public const string RENAME = "RENAME";
            /// <summary>Token classification: other</summary>
            /// <value>"RESTART"</value>
            public const string RESTART = "RESTART";
            /// <summary>Token classification: other</summary>
            /// <value>"RESTRICT"</value>
            public const string RESTRICT = "RESTRICT";
            /// <summary>Token classification: other</summary>
            /// <value>"ROLE"</value>
            public const string ROLE = "ROLE";
            /// <summary>Token classification: other</summary>
            /// <value>"SCRIPT"</value>
            public const string SCRIPT = "SCRIPT";
            /// <summary>Token classification: other</summary>
            /// <value>"SCRIPTFORMAT"</value>
            public const string SCRIPTFORMAT = "SCRIPTFORMAT";
            /// <summary>Token classification: other</summary>
            /// <value>"SEQUENCE"</value>
            public const string SEQUENCE = "SEQUENCE";
            /// <summary>Token classification: other</summary>
            /// <value>"SHUTDOWN"</value>
            public const string SHUTDOWN = "SHUTDOWN";
            /// <summary>Token classification: other</summary>
            /// <value>"SOURCE"</value>
            public const string SOURCE = "SOURCE";
            /// <summary>Token classification: other</summary>
            /// <value>"STDDEV_POP"</value>
            public const string STDDEV_POP = "STDDEV_POP";
            /// <summary>Token classification: other</summary>
            /// <value>"STDDEV_SAMP"</value>
            public const string STDDEV_SAMP = "STDDEV_SAMP";
            /// <summary>Token classification: other</summary>
            /// <value>"SUBSTRING"</value>
            public const string SUBSTRING = "SUBSTRING";
            /// <summary>Token classification: other</summary>
            /// <value>"SUM"</value>
            public const string SUM = "SUM";
            /// <summary>Token classification: other</summary>
            /// <value>"SYSDATE"</value>
            public const string SYSDATE = "SYSDATE";
            /// <summary>Token classification: other</summary>
            /// <value>"TEMP"</value>
            public const string TEMP = "TEMP";
            /// <summary>Token classification: other</summary>
            /// <value>"TEMPORARY"</value>
            public const string TEMPORARY = "TEMPORARY";
            /// <summary>Token classification: other</summary>
            /// <value>"TEXT"</value>
            public const string TEXT = "TEXT";
            /// <summary>Token classification: other</summary>
            /// <value>"TODAY"</value>
            public const string TODAY = "TODAY";
            /// <summary>Token classification: other</summary>
            /// <value>"TOP"</value>
            public const string TOP = "TOP";
            /// <summary>Token classification: other</summary>
            /// <value>"TRIM"</value>
            public const string TRIM = "TRIM";
            /// <summary>Token classification: other</summary>
            /// <value>"VAR_POP"</value>
            public const string VAR_POP = "VAR_POP";
            /// <summary>Token classification: other</summary>
            /// <value>"VAR_SAMP"</value>
            public const string VAR_SAMP = "VAR_SAMP";
            /// <summary>Token classification: other</summary>
            /// <value>"VIEW"</value>
            public const string VIEW = "VIEW";
            /// <summary>Token classification: other</summary>
            /// <value>"WORK"</value>
            public const string WORK = "WORK";
            /// <summary>Token classification: other</summary>
            /// <value>"WRITE_DELAY"</value>
            public const string WRITE_DELAY = "WRITE_DELAY";
            #endregion

            #endregion
        }
    }
}