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
            /// <summary>special</summary>
            /// <value>"*"</value>
            public const string ASTERISK = "*";
            /// <summary>special</summary>
            /// <value>","</value>
            public const string COMMA = ",";
            /// <summary>special</summary>
            /// <value>")"</value>
            public const string CLOSEBRACKET = ")";
            /// <summary>special</summary>
            /// <value>"="</value>
            [CLSCompliant(false)]
            public const string EQUALS = "=";
            /// <summary>special</summary>
            /// <value>"/"</value>
            public const string DIVIDE = "/";
            /// <summary>special</summary>
            /// <value>"("</value>
            public const string OPENBRACKET = "(";
            /// <summary>special</summary>
            /// <value>";"</value>
            public const string SEMICOLON = ";";
            /// <summary>special</summary>
            /// <value>"*"</value>
            public const string MULTIPLY = "*";
            /// <summary>special</summary>
            /// <value>"%"</value>
            public const string PERCENT = "%";
            /// <summary>special</summary>
            /// <value>"+"</value>
            public const string PLUS = "+";
            /// <summary>special</summary>
            /// <value>"?"</value>
            public const string QUESTION = "?";
            /// <summary>special</summary>
            /// <value>"."</value>
            public const string PERIOD = ".";
            #endregion

            #region SQL 200n Reserved Word Tokens
            // SQL 200n reserved word tokens
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"ADD"</value>
            public const string ADD = "ADD"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"ALL"</value>
            public const string ALL = "ALL"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"ALLOCATE"</value>
            public const string ALLOCATE = "ALLOCATE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"ALTER"</value>
            public const string ALTER = "ALTER"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"AND"</value>
            public const string AND = "AND"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"ANY"</value>
            public const string ANY = "ANY"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"ARE"</value>
            public const string ARE = "ARE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"ARRAY"</value>
            public const string ARRAY = "ARRAY"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"AS"</value>
            public const string AS = "AS"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"ASENSITIVE"</value>
            public const string ASENSITIVE = "ASENSITIVE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"ASYMMETRIC"</value>
            public const string ASYMMETRIC = "ASYMMETRIC"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"AT"</value>
            public const string AT = "AT"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"ATOMIC"</value>
            public const string ATOMIC = "ATOMIC"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"AUTHORIZATION"</value>
            public const string AUTHORIZATION = "AUTHORIZATION"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"BEGIN"</value>
            public const string BEGIN = "BEGIN"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"BETWEEN"</value>
            public const string BETWEEN = "BETWEEN"; // reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"BIGINT"</value>
            public const string BIGINT = "BIGINT"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"BINARY"</value>
            public const string BINARY = "BINARY"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"BLOB"</value>
            public const string BLOB = "BLOB"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"BOOLEAN"</value>
            public const string BOOLEAN = "BOOLEAN"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"BOTH"</value>
            public const string BOTH = "BOTH"; // reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"BY"</value>
            public const string BY = "BY"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"CALL"</value>
            public const string CALL = "CALL"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"CALLED"</value>
            public const string CALLED = "CALLED"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"CASCADED"</value>
            public const string CASCADED = "CASCADED"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"CASE"</value>
            public const string CASE = "CASE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"CAST"</value>
            public const string CAST = "CAST"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"CHAR"</value>
            public const string CHAR = "CHAR"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"CHARACTER"</value>
            public const string CHARACTER = "CHARACTER"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"CHECK"</value>
            public const string CHECK = "CHECK"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"CLOB"</value>
            public const string CLOB = "CLOB"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"CLOSE"</value>
            public const string CLOSE = "CLOSE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"COLLATE"</value>
            public const string COLLATE = "COLLATE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"COLUMN"</value>
            public const string COLUMN = "COLUMN"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"COMMIT"</value>
            public const string COMMIT = "COMMIT"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"CONDIITON"</value>
            public const string CONDITION = "CONDIITON"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"CONNECT"</value>
            public const string CONNECT = "CONNECT"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"CONSTRAINT"</value>
            public const string CONSTRAINT = "CONSTRAINT"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"CONTINUE"</value>
            public const string CONTINUE = "CONTINUE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"CORRESPONDING"</value>
            public const string CORRESPONDING = "CORRESPONDING"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"CREATE"</value>
            public const string CREATE = "CREATE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"CROSS"</value>
            public const string CROSS = "CROSS"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"CUBE"</value>
            public const string CUBE = "CUBE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"CURRENT"</value>
            public const string CURRENT = "CURRENT"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"CURRENT_DATE"</value>
            public const string CURRENT_DATE = "CURRENT_DATE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"CURRENT_DEFAULT_TRANSFORM_GROUP"</value>
            public const string CURRENT_DEFAULT_TRANSFORM_GROUP
                = "CURRENT_DEFAULT_TRANSFORM_GROUP"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"CURRENT_PATH"</value>
            public const string CURRENT_PATH = "CURRENT_PATH"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"CURRENT_ROLE"</value>
            public const string CURRENT_ROLE = "CURRENT_ROLE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"CURRENT_TIME"</value>
            public const string CURRENT_TIME = "CURRENT_TIME"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"CURRENT_TIMESTAMP"</value>
            public const string CURRENT_TIMESTAMP = "CURRENT_TIMESTAMP"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"CURRENT_TRANSFORM_GROUP_FOR_TYPE"</value>
            public const string CURRENT_TRANSFORM_GROUP_FOR_TYPE =
                "CURRENT_TRANSFORM_GROUP_FOR_TYPE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"CURRENT_USER"</value>
            public const string CURRENT_USER = "CURRENT_USER"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"CURSOR"</value>
            public const string CURSOR = "CURSOR"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"CYCLE"</value>
            public const string CYCLE = "CYCLE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"DATE"</value>
            public const string DATE = "DATE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"DAY"</value>
            public const string DAY = "DAY"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"DEALLOCATE"</value>
            public const string DEALLOCATE = "DEALLOCATE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"DEC"</value>
            public const string DEC = "DEC"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"DECIMAL"</value>
            public const string DECIMAL = "DECIMAL"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"DECLARE"</value>
            public const string DECLARE = "DECLARE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"DEFAULT"</value>
            public const string DEFAULT = "DEFAULT"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"DELETE"</value>
            public const string DELETE = "DELETE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"DEREF"</value>
            public const string DEREF = "DEREF"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"DESCRIBE"</value>
            public const string DESCRIBE = "DESCRIBE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"DETERMINISTIC"</value>
            public const string DETERMINISTIC = "DETERMINISTIC"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"DISCONNECT"</value>
            public const string DISCONNECT = "DISCONNECT"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"DISTINCT"</value>
            public const string DISTINCT = "DISTINCT"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"DO"</value>
            public const string DO = "DO"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"DOUBLE"</value>
            public const string DOUBLE = "DOUBLE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"DAYOFWEEK"</value>
            public const string DAYOFWEEK = "DAYOFWEEK"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"DROP"</value>
            public const string DROP = "DROP"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"DYNAMIC"</value>
            public const string DYNAMIC = "DYNAMIC"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"EACH"</value>
            public const string EACH = "EACH"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"ELEMENT"</value>
            public const string ELEMENT = "ELEMENT"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"ELSE"</value>
            public const string ELSE = "ELSE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"ELSEIF"</value>
            public const string ELSEIF = "ELSEIF"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"END"</value>
            public const string END = "END"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"ESCAPE"</value>
            public const string ESCAPE = "ESCAPE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"EXCEPT"</value>
            public const string EXCEPT = "EXCEPT"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"EXEC"</value>
            public const string EXEC = "EXEC"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"EXECUTE"</value>
            public const string EXECUTE = "EXECUTE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"EXISTS"</value>
            public const string EXISTS = "EXISTS"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"EXIT"</value>
            public const string EXIT = "EXIT"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"EXTERNAL"</value>
            public const string EXTERNAL = "EXTERNAL"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"FALSE"</value>
            public const string FALSE = "FALSE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"FETCH"</value>
            public const string FETCH = "FETCH"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"FILTER"</value>
            public const string FILTER = "FILTER"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"FLOAT"</value>
            public const string FLOAT = "FLOAT"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"FOR"</value>
            public const string FOR = "FOR"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"FOREIGN"</value>
            public const string FOREIGN = "FOREIGN"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"FREE"</value>
            public const string FREE = "FREE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"FROM"</value>
            public const string FROM = "FROM"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"FULL"</value>
            public const string FULL = "FULL"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"FUNCTION"</value>
            public const string FUNCTION = "FUNCTION"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"GET"</value>
            public const string GET = "GET"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"GLOBAL"</value>
            public const string GLOBAL = "GLOBAL"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"GRANT"</value>
            public const string GRANT = "GRANT"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"GROUP"</value>
            public const string GROUP = "GROUP"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"GROUPING"</value>
            public const string GROUPING = "GROUPING"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"HANDLER"</value>
            public const string HANDLER = "HANDLER"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"HAVING"</value>
            public const string HAVING = "HAVING"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"HEADER"</value>
            public const string HEADER = "HEADER"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"HOLD"</value>
            public const string HOLD = "HOLD"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"HOUR"</value>
            public const string HOUR = "HOUR"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"IDENTITY"</value>
            public const string IDENTITY = "IDENTITY"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"IF"</value>
            public const string IF = "IF"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"IMMEDIATE"</value>
            public const string IMMEDIATE = "IMMEDIATE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"IN"</value>
            public const string IN = "IN"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"INDICATOR"</value>
            public const string INDICATOR = "INDICATOR"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"INNER"</value>
            public const string INNER = "INNER"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"INOUT"</value>
            public const string INOUT = "INOUT"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"INPUT"</value>
            public const string INPUT = "INPUT"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"INSENSITIVE"</value>
            public const string INSENSITIVE = "INSENSITIVE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"INSERT"</value>
            public const string INSERT = "INSERT"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"INT"</value>
            public const string INT = "INT"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"INTEGER"</value>
            public const string INTEGER = "INTEGER"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"INTERSECT"</value>
            public const string INTERSECT = "INTERSECT"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"INTERVAL"</value>
            public const string INTERVAL = "INTERVAL"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"INTO"</value>
            public const string INTO = "INTO"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"IS"</value>
            public const string IS = "IS"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"ITERATE"</value>
            public const string ITERATE = "ITERATE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"JOIN"</value>
            public const string JOIN = "JOIN"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"LANGUAGE"</value>
            public const string LANGUAGE = "LANGUAGE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"LARGE"</value>
            public const string LARGE = "LARGE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"LATERAL"</value>
            public const string LATERAL = "LATERAL"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"LEADING"</value>
            public const string LEADING = "LEADING"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"LEAVE"</value>
            public const string LEAVE = "LEAVE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"LEFT"</value>
            public const string LEFT = "LEFT"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"LIKE"</value>
            public const string LIKE = "LIKE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"LOCAL"</value>
            public const string LOCAL = "LOCAL"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"LOCALTIME"</value>
            public const string LOCALTIME = "LOCALTIME"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"LOCALTIMESTAMP"</value>
            public const string LOCALTIMESTAMP = "LOCALTIMESTAMP"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"LOOP"</value>
            public const string LOOP = "LOOP"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"MATCH"</value>
            public const string MATCH = "MATCH"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"MEMBER"</value>
            public const string MEMBER = "MEMBER"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"METHOD"</value>
            public const string METHOD = "METHOD"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"MINUTE"</value>
            public const string MINUTE = "MINUTE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"MODIFIES"</value>
            public const string MODIFIES = "MODIFIES"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"MODULE"</value>
            public const string MODULE = "MODULE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"MONTH"</value>
            public const string MONTH = "MONTH"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"MULTISET"</value>
            public const string MULTISET = "MULTISET"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"NATIONAL"</value>
            public const string NATIONAL = "NATIONAL"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"NAUTRAL"</value>
            public const string NATURAL = "NAUTRAL"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"NCHAR"</value>
            public const string NCHAR = "NCHAR"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"NCLOB"</value>
            public const string NCLOB = "NCLOB"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"NEW"</value>
            public const string NEW = "NEW"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"NEXT"</value>
            public const string NEXT = "NEXT"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"NO"</value>
            public const string NO = "NO"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"NONE"</value>
            public const string NONE = "NONE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"NOT"</value>
            public const string NOT = "NOT"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"NULL"</value>
            public const string NULL = "NULL"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"NUMERIC"</value>
            public const string NUMERIC = "NUMERIC"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"OF"</value>
            public const string OF = "OF"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"OLD"</value>
            public const string OLD = "OLD"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"ON"</value>
            public const string ON = "ON"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"ONLY"</value>
            public const string ONLY = "ONLY"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"OPEN"</value>
            public const string OPEN = "OPEN"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"OR"</value>
            public const string OR = "OR"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"ORDER"</value>
            public const string ORDER = "ORDER"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"OUT"</value>
            public const string OUT = "OUT"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"OUTER"</value>
            public const string OUTER = "OUTER"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"OUTPUT"</value>
            public const string OUTPUT = "OUTPUT"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"OVER"</value>
            public const string OVER = "OVER"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"OVERLAPS"</value>
            public const string OVERLAPS = "OVERLAPS"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"PARAMETER"</value>
            public const string PARAMETER = "PARAMETER"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"PARTITION"</value>
            public const string PARTITION = "PARTITION"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"PRECISION"</value>
            public const string PRECISION = "PRECISION"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"PREPARE"</value>
            public const string PREPARE = "PREPARE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"PRIMARY"</value>
            public const string PRIMARY = "PRIMARY"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"PROCEDURE"</value>
            public const string PROCEDURE = "PROCEDURE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"RANGE"</value>
            public const string RANGE = "RANGE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"READS"</value>
            public const string READS = "READS"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"REAL"</value>
            public const string REAL = "REAL"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"RECURSIVE"</value>
            public const string RECURSIVE = "RECURSIVE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"REF"</value>
            public const string REF = "REF"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"REFERENCES"</value>
            public const string REFERENCES = "REFERENCES"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"REFERENCING"</value>
            public const string REFERENCING = "REFERENCING"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"RELEASE"</value>
            public const string RELEASE = "RELEASE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"REPEAT"</value>
            public const string REPEAT = "REPEAT"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"RESIGNAL"</value>
            public const string RESIGNAL = "RESIGNAL"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"RESULT"</value>
            public const string RESULT = "RESULT"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"RETURN"</value>
            public const string RETURN = "RETURN"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"RETURNS"</value>
            public const string RETURNS = "RETURNS"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"REVOKE"</value>
            public const string REVOKE = "REVOKE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"RIGHT"</value>
            public const string RIGHT = "RIGHT"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"ROLLBACK"</value>
            public const string ROLLBACK = "ROLLBACK"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"ROLLUP"</value>
            public const string ROLLUP = "ROLLUP"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"ROW"</value>
            public const string ROW = "ROW"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"ROWS"</value>
            public const string ROWS = "ROWS"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"SAVEPOINT"</value>
            public const string SAVEPOINT = "SAVEPOINT"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"SCOPE"</value>
            public const string SCOPE = "SCOPE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"SCROLL"</value>
            public const string SCROLL = "SCROLL"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"SECOND"</value>
            public const string SECOND = "SECOND"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"SEARCH"</value>
            public const string SEARCH = "SEARCH"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"SELECT"</value>
            public const string SELECT = "SELECT"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"SENSITIVE"</value>
            public const string SENSITIVE = "SENSITIVE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"SESSION_USER"</value>
            public const string SESSION_USER = "SESSION_USER"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"SET"</value>
            public const string SET = "SET"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"SIGNAL"</value>
            public const string SIGNAL = "SIGNAL"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"SIMILAR"</value>
            public const string SIMILAR = "SIMILAR"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"SMALLINT"</value>
            public const string SMALLINT = "SMALLINT"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"SOME"</value>
            public const string SOME = "SOME"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"SPECIFIC"</value>
            public const string SPECIFIC = "SPECIFIC"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"SPECIFICTYPE"</value>
            public const string SPECIFICTYPE = "SPECIFICTYPE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"SQL"</value>
            public const string SQL = "SQL"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"SQLEXCEPTION"</value>
            public const string SQLEXCEPTION = "SQLEXCEPTION"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"SQLSTATE"</value>
            public const string SQLSTATE = "SQLSTATE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"SQLWARNING"</value>
            public const string SQLWARNING = "SQLWARNING"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"START"</value>
            public const string START = "START"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"STATIC"</value>
            public const string STATIC = "STATIC"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"SUBMULTISET"</value>
            public const string SUBMULTISET = "SUBMULTISET"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"SYMMETRIC"</value>
            public const string SYMMETRIC = "SYMMETRIC"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"SYSTEM"</value>
            public const string SYSTEM = "SYSTEM"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"SYSTEM_USER"</value>
            public const string SYSTEM_USER = "SYSTEM_USER"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"TABLE"</value>
            public const string TABLE = "TABLE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"TABLESAMPLE"</value>
            public const string TABLESAMPLE = "TABLESAMPLE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"THEN"</value>
            public const string THEN = "THEN"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"TIME"</value>
            public const string TIME = "TIME"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"TIMESTAMP"</value>
            public const string TIMESTAMP = "TIMESTAMP"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"TIMEZONE_HOUR"</value>
            public const string TIMEZONE_HOUR = "TIMEZONE_HOUR"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"TIMEZONE_MINUTE"</value>
            public const string TIMEZONE_MINUTE = "TIMEZONE_MINUTE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"TO"</value>
            public const string TO = "TO"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"TRAILING"</value>
            public const string TRAILING = "TRAILING"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"TRANSLATION"</value>
            public const string TRANSLATION = "TRANSLATION"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"TREAT"</value>
            public const string TREAT = "TREAT"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"TRIGGER"</value>
            public const string TRIGGER = "TRIGGER"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"TRUE"</value>
            public const string TRUE = "TRUE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"UNDO"</value>
            public const string UNDO = "UNDO"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"UNION"</value>
            public const string UNION = "UNION"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"UNIQUE"</value>
            public const string UNIQUE = "UNIQUE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"UNKNOWN"</value>
            public const string UNKNOWN = "UNKNOWN"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"UNNEST"</value>
            public const string UNNEST = "UNNEST"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"UNTIL"</value>
            public const string UNTIL = "UNTIL"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"UPDATE"</value>
            public const string UPDATE = "UPDATE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"USER"</value>
            public const string USER = "USER"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"USING"</value>
            public const string USING = "USING"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"VALUE"</value>
            public const string VALUE = "VALUE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"VALUES"</value>
            public const string VALUES = "VALUES"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"VARCHAR"</value>
            public const string VARCHAR = "VARCHAR"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"VARYING"</value>
            public const string VARYING = "VARYING"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"WHEN"</value>
            public const string WHEN = "WHEN"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"WHENEVER"</value>
            public const string WHENEVER = "WHENEVER"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"WHERE"</value>
            public const string WHERE = "WHERE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"WHILE"</value>
            public const string WHILE = "WHILE"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"WINDOW"</value>
            public const string WINDOW = "WINDOW"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"WITH"</value>
            public const string WITH = "WITH"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"WITHIN"</value>
            public const string WITHIN = "WITHIN"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"WITHOUT"</value>
            public const string WITHOUT = "WITHOUT"; //reserved
            ///<summary>SQL 200n reserved word</summary>
            /// <value>"YEAR"</value>
            public const string YEAR = "YEAR"; //reserved 
            #endregion

            #region Other Tokens
            /// <summary>other</summary>
            /// <value>"ALWAYS"</value>
            public const string ALWAYS = "ALWAYS";
            /// <summary>other</summary>
            /// <value>"ACTION"</value>
            public const string ACTION = "ACTION";
            /// <summary>other</summary>
            /// <value>"ADMIN"</value>
            public const string ADMIN = "ADMIN";
            /// <summary>other</summary>
            /// <value>"AFTER"</value>
            public const string AFTER = "AFTER";
            /// <summary>other</summary>
            /// <value>"ALIAS"</value>
            public const string ALIAS = "ALIAS";
            /// <summary>other</summary>
            /// <value>"ASC"</value>
            public const string ASC = "ASC";
            /// <summary>other</summary>
            /// <value>"AUTOCOMMIT"</value>
            public const string AUTOCOMMIT = "AUTOCOMMIT";
            /// <summary>other</summary>
            /// <value>"AVG"</value>
            public const string AVG = "AVG";
            /// <summary>other</summary>
            /// <value>"BACKUP"</value>
            public const string BACKUP = "BACKUP";
            /// <summary>other</summary>
            /// <value>"BEFORE"</value>
            public const string BEFORE = "BEFORE";
            /// <summary>other</summary>
            /// <value>"CACHED"</value>
            public const string CACHED = "CACHED";
            /// <summary>other</summary>
            /// <value>"CASCADE"</value>
            public const string CASCADE = "CASCADE";
            /// <summary>other</summary>
            /// <value>"CASEWHEN"</value>
            public const string CASEWHEN = "CASEWHEN";
            /// <summary>other</summary>
            /// <value>"CHECKPOINT"</value>
            public const string CHECKPOINT = "CHECKPOINT";
            /// <summary>other</summary>
            /// <value>"CLASS"</value>
            public const string CLASS = "CLASS";
            /// <summary>other</summary>
            /// <value>"COALESCE"</value>
            public const string COALESCE = "COALESCE";
            /// <summary>other</summary>
            /// <value>"COLLATION"</value>
            public const string COLLATION = "COLLATION";
            /// <summary>other</summary>
            /// <value>"COMPACT"</value>
            public const string COMPACT = "COMPACT";
            /// <summary>other</summary>
            /// <value>"COMPRESSED"</value>
            public const string COMPRESSED = "COMPRESSED";
            /// <summary>other</summary>
            /// <value>"CONVERT"</value>
            public const string CONVERT = "CONVERT";
            /// <summary>other</summary>
            /// <value>"COUNT"</value>
            public const string COUNT = "COUNT";
            /// <summary>other</summary>
            /// <value>"DATABASE"</value>
            public const string DATABASE = "DATABASE";
            /// <summary>other</summary>
            /// <value>"DEFRAG"</value>
            public const string DEFRAG = "DEFRAG";
            /// <summary>other</summary>
            /// <value>"DESC"</value>
            public const string DESC = "DESC";
            /// <summary>other</summary>
            /// <value>"EVERY"</value>
            public const string EVERY = "EVERY";
            /// <summary>other</summary>
            /// <value>"EXPLAIN"</value>
            public const string EXPLAIN = "EXPLAIN";
            /// <summary>other</summary>
            /// <value>"EXTRACT"</value>
            public const string EXTRACT = "EXTRACT";
            /// <summary>other</summary>
            /// <value>"GENERATED"</value>
            public const string GENERATED = "GENERATED";
            /// <summary>other</summary>
            /// <value>"IFNULL"</value>
            public const string IFNULL = "IFNULL";
            /// <summary>other</summary>
            /// <value>"IGNORECASE"</value>
            public const string IGNORECASE = "IGNORECASE";
            /// <summary>other</summary>
            /// <value>"IMMEDIATELY"</value>
            public const string IMMEDIATELY = "IMMEDIATELY";
            /// <summary>other</summary>
            /// <value>"INCREMENT"</value>
            public const string INCREMENT = "INCREMENT";
            /// <summary>other</summary>
            /// <value>"INDEX"</value>
            public const string INDEX = "INDEX";
            /// <summary>other</summary>
            /// <value>"INITIAL"</value>
            public const string INITIAL = "INITIAL";
            /// <summary>other</summary>
            /// <value>"KEY"</value>
            public const string KEY = "KEY";
            /// <summary>other</summary>
            /// <value>"LIMIT"</value>
            public const string LIMIT = "LIMIT";
            /// <summary>other</summary>
            /// <value>"LOGSIZE"</value>
            public const string LOGSIZE = "LOGSIZE";
            /// <summary>other</summary>
            /// <value>"MATCHED"</value>
            public const string MATCHED = "MATCHED";
            /// <summary>other</summary>
            /// <value>"MAX"</value>
            public const string MAX = "MAX";
            /// <summary>other</summary>
            /// <value>"MAXROWS"</value>
            public const string MAXROWS = "MAXROWS";
            /// <summary>other</summary>
            /// <value>"MEMORY"</value>
            public const string MEMORY = "MEMORY";
            /// <summary>other</summary>
            /// <value>"MERGE"</value>
            public const string MERGE = "MERGE";
            /// <summary>other</summary>
            /// <value>"MIN"</value>
            public const string MIN = "MIN";
            /// <summary>other</summary>
            /// <value>"MINUS"</value>
            public const string MINUS = "MINUS";
            /// <summary>other</summary>
            /// <value>"NOW"</value>
            public const string NOW = "NOW";
            /// <summary>other</summary>
            /// <value>"NOWAIT"</value>
            public const string NOWAIT = "NOWAIT";
            /// <summary>other</summary>
            /// <value>"NULLIF"</value>
            public const string NULLIF = "NULLIF";
            /// <summary>other</summary>
            /// <value>"NVL"</value>
            public const string NVL = "NVL";
            /// <summary>other</summary>
            /// <value>"OFFSET"</value>
            public const string OFFSET = "OFFSET";
            /// <summary>other</summary>
            /// <value>"PASSWORD"</value>
            public const string PASSWORD = "PASSWORD";
            /// <summary>other</summary>
            /// <value>"SCHEMA"</value>
            public const string SCHEMA = "SCHEMA";
            /// <summary>other</summary>
            /// <value>"PLAN"</value>
            public const string PLAN = "PLAN";
            /// <summary>other</summary>
            /// <value>"PRESERVE"</value>
            public const string PRESERVE = "PRESERVE";
            /// <summary>other</summary>
            /// <value>"PRIVILEGES"</value>
            public const string PRIVILEGES = "PRIVILEGES";
            /// <summary>other</summary>
            /// <value>"POSITION"</value>
            public const string POSITION = "POSITION";
            /// <summary>other</summary>
            /// <value>"PROPERTY"</value>
            public const string PROPERTY = "PROPERTY";
            /// <summary>other</summary>
            /// <value>"PUBLIC"</value>
            public const string PUBLIC = "PUBLIC";
            /// <summary>other</summary>
            /// <value>"QUEUE"</value>
            public const string QUEUE = "QUEUE";
            /// <summary>other</summary>
            /// <value>"READONLY"</value>
            public const string READONLY = "READONLY";
            /// <summary>other</summary>
            /// <value>"REFERENTIAL_INTEGRITY"</value>
            public const string REFERENTIAL_INTEGRITY = "REFERENTIAL_INTEGRITY";
            /// <summary>other</summary>
            /// <value>"RENAME"</value>
            public const string RENAME = "RENAME";
            /// <summary>other</summary>
            /// <value>"RESTART"</value>
            public const string RESTART = "RESTART";
            /// <summary>other</summary>
            /// <value>"RESTRICT"</value>
            public const string RESTRICT = "RESTRICT";
            /// <summary>other</summary>
            /// <value>"ROLE"</value>
            public const string ROLE = "ROLE";
            /// <summary>other</summary>
            /// <value>"SCRIPT"</value>
            public const string SCRIPT = "SCRIPT";
            /// <summary>other</summary>
            /// <value>"SCRIPTFORMAT"</value>
            public const string SCRIPTFORMAT = "SCRIPTFORMAT";
            /// <summary>other</summary>
            /// <value>"SEQUENCE"</value>
            public const string SEQUENCE = "SEQUENCE";
            /// <summary>other</summary>
            /// <value>"SHUTDOWN"</value>
            public const string SHUTDOWN = "SHUTDOWN";
            /// <summary>other</summary>
            /// <value>"SOURCE"</value>
            public const string SOURCE = "SOURCE";
            /// <summary>other</summary>
            /// <value>"STDDEV_POP"</value>
            public const string STDDEV_POP = "STDDEV_POP";
            /// <summary>other</summary>
            /// <value>"STDDEV_SAMP"</value>
            public const string STDDEV_SAMP = "STDDEV_SAMP";
            /// <summary>other</summary>
            /// <value>"SUBSTRING"</value>
            public const string SUBSTRING = "SUBSTRING";
            /// <summary>other</summary>
            /// <value>"SUM"</value>
            public const string SUM = "SUM";
            /// <summary>other</summary>
            /// <value>"SYSDATE"</value>
            public const string SYSDATE = "SYSDATE";
            /// <summary>other</summary>
            /// <value>"TEMP"</value>
            public const string TEMP = "TEMP";
            /// <summary>other</summary>
            /// <value>"TEMPORARY"</value>
            public const string TEMPORARY = "TEMPORARY";
            /// <summary>other</summary>
            /// <value>"TEXT"</value>
            public const string TEXT = "TEXT";
            /// <summary>other</summary>
            /// <value>"TODAY"</value>
            public const string TODAY = "TODAY";
            /// <summary>other</summary>
            /// <value>"TOP"</value>
            public const string TOP = "TOP";
            /// <summary>other</summary>
            /// <value>"TRIM"</value>
            public const string TRIM = "TRIM";
            /// <summary>other</summary>
            /// <value>"VAR_POP"</value>
            public const string VAR_POP = "VAR_POP";
            /// <summary>other</summary>
            /// <value>"VAR_SAMP"</value>
            public const string VAR_SAMP = "VAR_SAMP";
            /// <summary>other</summary>
            /// <value>"VIEW"</value>
            public const string VIEW = "VIEW";
            /// <summary>other</summary>
            /// <value>"VIEW"</value>
            public const string WORK = "WORK";
            /// <summary>other</summary>
            /// <value>"WRITE_DELAY"</value>
            public const string WRITE_DELAY = "WRITE_DELAY";
            #endregion

            #endregion
        }
    }
}