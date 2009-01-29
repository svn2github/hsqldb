namespace System.Data.Hsqldb.Common.Sql
{
    public partial class Token
    {
        /// <summary>
        /// String constants corresponding to common HSQLDB and SQL:200n language tokens.
        /// </summary>
        public static class ValueFor
        {
            #region Constants

            #region Special Tokens
            // SPECIAL
            /// <value>"*"</value>
            public const string ASTERISK = "*";
            /// <value>","</value>
            public const string COMMA = ",";
            /// <value>")"</value>
            public const string CLOSEBRACKET = ")";
            /// <value>"="</value>
            [CLSCompliant(false)]
            public const string EQUALS = "=";
            /// <value>"/"</value>
            public const string DIVIDE = "/";
            /// <value>"("</value>
            public const string OPENBRACKET = "(";
            /// <value>";"</value>
            public const string SEMICOLON = ";";
            /// <value>"*"</value>
            public const string MULTIPLY = "*";
            /// <value>"%"</value>
            public const string PERCENT = "%";
            /// <value>"+"</value>
            public const string PLUS = "+";
            /// <value>"?"</value>
            public const string QUESTION = "?";
            /// <value>"."</value>
            public const string PERIOD = ".";
            #endregion

            #region SQL 200n Reserved Word Tokens
            // SQL 200n reserved word tokens
            /// <value>"ADD"</value>
            public const string ADD = "ADD"; //reserved
            /// <value>"ALL"</value>
            public const string ALL = "ALL"; //reserved
            /// <value>"ALLOCATE"</value>
            public const string ALLOCATE = "ALLOCATE"; //reserved
            /// <value>"ALTER"</value>
            public const string ALTER = "ALTER"; //reserved
            /// <value>"AND"</value>
            public const string AND = "AND"; //reserved
            /// <value>"ANY"</value>
            public const string ANY = "ANY"; //reserved
            /// <value>"ARE"</value>
            public const string ARE = "ARE"; //reserved
            /// <value>"ARRAY"</value>
            public const string ARRAY = "ARRAY"; //reserved
            /// <value>"AS"</value>
            public const string AS = "AS"; //reserved
            /// <value>"ASENSITIVE"</value>
            public const string ASENSITIVE = "ASENSITIVE"; //reserved
            /// <value>"ASYMMETRIC"</value>
            public const string ASYMMETRIC = "ASYMMETRIC"; //reserved
            /// <value>"AT"</value>
            public const string AT = "AT"; //reserved
            /// <value>"ATOMIC"</value>
            public const string ATOMIC = "ATOMIC"; //reserved
            /// <value>"AUTHORIZATION"</value>
            public const string AUTHORIZATION = "AUTHORIZATION"; //reserved
            /// <value>"BEGIN"</value>
            public const string BEGIN = "BEGIN"; //reserved
            /// <value>"BETWEEN"</value>
            public const string BETWEEN = "BETWEEN";
            /// <value>"BIGINT"</value>
            public const string BIGINT = "BIGINT"; //reserved
            /// <value>"BINARY"</value>
            public const string BINARY = "BINARY"; //reserved
            /// <value>"BLOB"</value>
            public const string BLOB = "BLOB"; //reserved
            /// <value>"BOOLEAN"</value>
            public const string BOOLEAN = "BOOLEAN"; //reserved
            /// <value>"BOTH"</value>
            public const string BOTH = "BOTH";
            /// <value>"BY"</value>
            public const string BY = "BY"; //reserved
            /// <value>"CALL"</value>
            public const string CALL = "CALL"; //reserved
            /// <value>"CALLED"</value>
            public const string CALLED = "CALLED"; //reserved
            /// <value>"CASCADED"</value>
            public const string CASCADED = "CASCADED"; //reserved
            /// <value>"CASE"</value>
            public const string CASE = "CASE"; //reserved
            /// <value>"CAST"</value>
            public const string CAST = "CAST"; //reserved
            /// <value>"CHAR"</value>
            public const string CHAR = "CHAR"; //reserved
            /// <value>"CHARACTER"</value>
            public const string CHARACTER = "CHARACTER"; //reserved
            /// <value>"CHECK"</value>
            public const string CHECK = "CHECK"; //reserved
            /// <value>"CLOB"</value>
            public const string CLOB = "CLOB"; //reserved
            /// <value>"CLOSE"</value>
            public const string CLOSE = "CLOSE"; //reserved
            /// <value>"COLLATE"</value>
            public const string COLLATE = "COLLATE"; //reserved
            /// <value>"COLUMN"</value>
            public const string COLUMN = "COLUMN"; //reserved
            /// <value>"COMMIT"</value>
            public const string COMMIT = "COMMIT"; //reserved
            /// <value>"CONDIITON"</value>
            public const string CONDITION = "CONDIITON"; //reserved
            /// <value>"CONNECT"</value>
            public const string CONNECT = "CONNECT"; //reserved
            /// <value>"CONSTRAINT"</value>
            public const string CONSTRAINT = "CONSTRAINT"; //reserved
            /// <value>"CONTINUE"</value>
            public const string CONTINUE = "CONTINUE"; //reserved
            /// <value>"CORRESPONDING"</value>
            public const string CORRESPONDING = "CORRESPONDING"; //reserved
            /// <value>"CREATE"</value>
            public const string CREATE = "CREATE"; //reserved
            /// <value>"CROSS"</value>
            public const string CROSS = "CROSS"; //reserved
            /// <value>"CUBE"</value>
            public const string CUBE = "CUBE"; //reserved
            /// <value>"CURRENT"</value>
            public const string CURRENT = "CURRENT"; //reserved
            /// <value>"CURRENT_DATE"</value>
            public const string CURRENT_DATE = "CURRENT_DATE"; //reserved
            /// <value>"CURRENT_DEFAULT_TRANSFORM_GROUP"</value>
            public const string CURRENT_DEFAULT_TRANSFORM_GROUP
                = "CURRENT_DEFAULT_TRANSFORM_GROUP"; //reserved
            /// <value>"CURRENT_PATH"</value>
            public const string CURRENT_PATH = "CURRENT_PATH"; //reserved
            /// <value>"CURRENT_ROLE"</value>
            public const string CURRENT_ROLE = "CURRENT_ROLE"; //reserved
            /// <value>"CURRENT_TIME"</value>
            public const string CURRENT_TIME = "CURRENT_TIME"; //reserved
            /// <value>"CURRENT_TIMESTAMP"</value>
            public const string CURRENT_TIMESTAMP = "CURRENT_TIMESTAMP"; //reserved
            /// <value>"CURRENT_TRANSFORM_GROUP_FOR_TYPE"</value>
            public const string CURRENT_TRANSFORM_GROUP_FOR_TYPE =
                "CURRENT_TRANSFORM_GROUP_FOR_TYPE"; //reserved
            /// <value>"CURRENT_USER"</value>
            public const string CURRENT_USER = "CURRENT_USER"; //reserved
            /// <value>"CURSOR"</value>
            public const string CURSOR = "CURSOR"; //reserved
            /// <value>"CYCLE"</value>
            public const string CYCLE = "CYCLE"; //reserved
            /// <value>"DATE"</value>
            public const string DATE = "DATE"; //reserved
            /// <value>"DAY"</value>
            public const string DAY = "DAY"; //reserved
            /// <value>"DEALLOCATE"</value>
            public const string DEALLOCATE = "DEALLOCATE"; //reserved
            /// <value>"DEC"</value>
            public const string DEC = "DEC"; //reserved
            /// <value>"DECIMAL"</value>
            public const string DECIMAL = "DECIMAL"; //reserved
            /// <value>"DECLARE"</value>
            public const string DECLARE = "DECLARE"; //reserved
            /// <value>"DEFAULT"</value>
            public const string DEFAULT = "DEFAULT"; //reserved
            /// <value>"DELETE"</value>
            public const string DELETE = "DELETE"; //reserved
            /// <value>"DEREF"</value>
            public const string DEREF = "DEREF"; //reserved
            /// <value>"DESCRIBE"</value>
            public const string DESCRIBE = "DESCRIBE"; //reserved
            /// <value>"DETERMINISTIC"</value>
            public const string DETERMINISTIC = "DETERMINISTIC"; //reserved
            /// <value>"DISCONNECT"</value>
            public const string DISCONNECT = "DISCONNECT"; //reserved
            /// <value>"DISTINCT"</value>
            public const string DISTINCT = "DISTINCT"; //reserved
            /// <value>"DO"</value>
            public const string DO = "DO"; //reserved
            /// <value>"DOUBLE"</value>
            public const string DOUBLE = "DOUBLE"; //reserved
            /// <value>"DAYOFWEEK"</value>
            public const string DAYOFWEEK = "DAYOFWEEK"; //reserved
            /// <value>"DROP"</value>
            public const string DROP = "DROP"; //reserved
            /// <value>"DYNAMIC"</value>
            public const string DYNAMIC = "DYNAMIC"; //reserved
            /// <value>"EACH"</value>
            public const string EACH = "EACH"; //reserved
            /// <value>"ELEMENT"</value>
            public const string ELEMENT = "ELEMENT"; //reserved
            /// <value>"ELSE"</value>
            public const string ELSE = "ELSE"; //reserved
            /// <value>"ELSEIF"</value>
            public const string ELSEIF = "ELSEIF"; //reserved
            /// <value>"END"</value>
            public const string END = "END"; //reserved
            /// <value>"ESCAPE"</value>
            public const string ESCAPE = "ESCAPE"; //reserved
            /// <value>"EXCEPT"</value>
            public const string EXCEPT = "EXCEPT"; //reserved
            /// <value>"EXEC"</value>
            public const string EXEC = "EXEC"; //reserved
            /// <value>"EXECUTE"</value>
            public const string EXECUTE = "EXECUTE"; //reserved
            /// <value>"EXISTS"</value>
            public const string EXISTS = "EXISTS"; //reserved
            /// <value>"EXIT"</value>
            public const string EXIT = "EXIT"; //reserved
            /// <value>"EXTERNAL"</value>
            public const string EXTERNAL = "EXTERNAL"; //reserved
            /// <value>"FALSE"</value>
            public const string FALSE = "FALSE"; //reserved
            /// <value>"FETCH"</value>
            public const string FETCH = "FETCH"; //reserved
            /// <value>"FILTER"</value>
            public const string FILTER = "FILTER"; //reserved
            /// <value>"FLOAT"</value>
            public const string FLOAT = "FLOAT"; //reserved
            /// <value>"FOR"</value>
            public const string FOR = "FOR"; //reserved
            /// <value>"FOREIGN"</value>
            public const string FOREIGN = "FOREIGN"; //reserved
            /// <value>"FREE"</value>
            public const string FREE = "FREE"; //reserved
            /// <value>"FROM"</value>
            public const string FROM = "FROM"; //reserved
            /// <value>"FULL"</value>
            public const string FULL = "FULL"; //reserved
            /// <value>"FUNCTION"</value>
            public const string FUNCTION = "FUNCTION"; //reserved
            /// <value>"GET"</value>
            public const string GET = "GET"; //reserved
            /// <value>"GLOBAL"</value>
            public const string GLOBAL = "GLOBAL"; //reserved
            /// <value>"GRANT"</value>
            public const string GRANT = "GRANT"; //reserved
            /// <value>"GROUP"</value>
            public const string GROUP = "GROUP"; //reserved
            /// <value>"GROUPING"</value>
            public const string GROUPING = "GROUPING"; //reserved
            /// <value>"HANDLER"</value>
            public const string HANDLER = "HANDLER"; //reserved
            /// <value>"HAVING"</value>
            public const string HAVING = "HAVING"; //reserved
            /// <value>"HEADER"</value>
            public const string HEADER = "HEADER"; //reserved
            /// <value>"HOLD"</value>
            public const string HOLD = "HOLD"; //reserved
            /// <value>"HOUR"</value>
            public const string HOUR = "HOUR"; //reserved
            /// <value>"IDENTITY"</value>
            public const string IDENTITY = "IDENTITY"; //reserved
            /// <value>"IF"</value>
            public const string IF = "IF"; //reserved
            /// <value>"IMMEDIATE"</value>
            public const string IMMEDIATE = "IMMEDIATE"; //reserved
            /// <value>"IN"</value>
            public const string IN = "IN"; //reserved
            /// <value>"INDICATOR"</value>
            public const string INDICATOR = "INDICATOR"; //reserved
            /// <value>"INNER"</value>
            public const string INNER = "INNER"; //reserved
            /// <value>"INOUT"</value>
            public const string INOUT = "INOUT"; //reserved
            /// <value>"INPUT"</value>
            public const string INPUT = "INPUT"; //reserved
            /// <value>"INSENSITIVE"</value>
            public const string INSENSITIVE = "INSENSITIVE"; //reserved
            /// <value>"INSERT"</value>
            public const string INSERT = "INSERT"; //reserved
            /// <value>"INT"</value>
            public const string INT = "INT"; //reserved
            /// <value>"INTEGER"</value>
            public const string INTEGER = "INTEGER"; //reserved
            /// <value>"INTERSECT"</value>
            public const string INTERSECT = "INTERSECT"; //reserved
            /// <value>"INTERVAL"</value>
            public const string INTERVAL = "INTERVAL"; //reserved
            /// <value>"INTO"</value>
            public const string INTO = "INTO"; //reserved
            /// <value>"IS"</value>
            public const string IS = "IS"; //reserved
            /// <value>"ITERATE"</value>
            public const string ITERATE = "ITERATE"; //reserved
            /// <value>"JOIN"</value>
            public const string JOIN = "JOIN"; //reserved
            /// <value>"LANGUAGE"</value>
            public const string LANGUAGE = "LANGUAGE"; //reserved
            /// <value>"LARGE"</value>
            public const string LARGE = "LARGE"; //reserved
            /// <value>"LATERAL"</value>
            public const string LATERAL = "LATERAL"; //reserved
            /// <value>"LEADING"</value>
            public const string LEADING = "LEADING"; //reserved
            /// <value>"LEAVE"</value>
            public const string LEAVE = "LEAVE"; //reserved
            /// <value>"LEFT"</value>
            public const string LEFT = "LEFT"; //reserved
            /// <value>"LIKE"</value>
            public const string LIKE = "LIKE"; //reserved
            /// <value>"LOCAL"</value>
            public const string LOCAL = "LOCAL"; //reserved
            /// <value>"LOCALTIME"</value>
            public const string LOCALTIME = "LOCALTIME"; //reserved
            /// <value>"LOCALTIMESTAMP"</value>
            public const string LOCALTIMESTAMP = "LOCALTIMESTAMP"; //reserved
            /// <value>"LOOP"</value>
            public const string LOOP = "LOOP"; //reserved
            /// <value>"MATCH"</value>
            public const string MATCH = "MATCH"; //reserved
            /// <value>"MEMBER"</value>
            public const string MEMBER = "MEMBER"; //reserved
            /// <value>"METHOD"</value>
            public const string METHOD = "METHOD"; //reserved
            /// <value>"MINUTE"</value>
            public const string MINUTE = "MINUTE"; //reserved
            /// <value>"MODIFIES"</value>
            public const string MODIFIES = "MODIFIES"; //reserved
            /// <value>"MODULE"</value>
            public const string MODULE = "MODULE"; //reserved
            /// <value>"MONTH"</value>
            public const string MONTH = "MONTH"; //reserved
            /// <value>"MULTISET"</value>
            public const string MULTISET = "MULTISET"; //reserved
            /// <value>"NATIONAL"</value>
            public const string NATIONAL = "NATIONAL"; //reserved
            /// <value>"NAUTRAL"</value>
            public const string NATURAL = "NAUTRAL"; //reserved
            /// <value>"NCHAR"</value>
            public const string NCHAR = "NCHAR"; //reserved
            /// <value>"NCLOB"</value>
            public const string NCLOB = "NCLOB"; //reserved
            /// <value>"NEW"</value>
            public const string NEW = "NEW"; //reserved
            /// <value>"NEXT"</value>
            public const string NEXT = "NEXT"; //reserved
            /// <value>"NO"</value>
            public const string NO = "NO"; //reserved
            /// <value>"NONE"</value>
            public const string NONE = "NONE"; //reserved
            /// <value>"NOT"</value>
            public const string NOT = "NOT"; //reserved
            /// <value>"NULL"</value>
            public const string NULL = "NULL"; //reserved
            /// <value>"NUMERIC"</value>
            public const string NUMERIC = "NUMERIC"; //reserved
            /// <value>"OF"</value>
            public const string OF = "OF"; //reserved
            /// <value>"OLD"</value>
            public const string OLD = "OLD"; //reserved
            /// <value>"ON"</value>
            public const string ON = "ON"; //reserved
            /// <value>"ONLY"</value>
            public const string ONLY = "ONLY"; //reserved
            /// <value>"OPEN"</value>
            public const string OPEN = "OPEN"; //reserved
            /// <value>"OR"</value>
            public const string OR = "OR"; //reserved
            /// <value>"ORDER"</value>
            public const string ORDER = "ORDER"; //reserved
            /// <value>"OUT"</value>
            public const string OUT = "OUT"; //reserved
            /// <value>"OUTER"</value>
            public const string OUTER = "OUTER"; //reserved
            /// <value>"OUTPUT"</value>
            public const string OUTPUT = "OUTPUT"; //reserved
            /// <value>"OVER"</value>
            public const string OVER = "OVER"; //reserved
            /// <value>"OVERLAPS"</value>
            public const string OVERLAPS = "OVERLAPS"; //reserved
            /// <value>"PARAMETER"</value>
            public const string PARAMETER = "PARAMETER"; //reserved
            /// <value>"PARTITION"</value>
            public const string PARTITION = "PARTITION"; //reserved
            /// <value>"PRECISION"</value>
            public const string PRECISION = "PRECISION"; //reserved
            /// <value>"PREPARE"</value>
            public const string PREPARE = "PREPARE"; //reserved
            /// <value>"PRIMARY"</value>
            public const string PRIMARY = "PRIMARY"; //reserved
            /// <value>"PROCEDURE"</value>
            public const string PROCEDURE = "PROCEDURE"; //reserved
            /// <value>"RANGE"</value>
            public const string RANGE = "RANGE"; //reserved
            /// <value>"READS"</value>
            public const string READS = "READS"; //reserved
            /// <value>"REAL"</value>
            public const string REAL = "REAL"; //reserved
            /// <value>"RECURSIVE"</value>
            public const string RECURSIVE = "RECURSIVE"; //reserved
            /// <value>"REF"</value>
            public const string REF = "REF"; //reserved
            /// <value>"REFERENCES"</value>
            public const string REFERENCES = "REFERENCES"; //reserved
            /// <value>"REFERENCING"</value>
            public const string REFERENCING = "REFERENCING"; //reserved
            /// <value>"RELEASE"</value>
            public const string RELEASE = "RELEASE"; //reserved
            /// <value>"REPEAT"</value>
            public const string REPEAT = "REPEAT"; //reserved
            /// <value>"RESIGNAL"</value>
            public const string RESIGNAL = "RESIGNAL"; //reserved
            /// <value>"RESULT"</value>
            public const string RESULT = "RESULT"; //reserved
            /// <value>"RETURN"</value>
            public const string RETURN = "RETURN"; //reserved
            /// <value>"RETURNS"</value>
            public const string RETURNS = "RETURNS"; //reserved
            /// <value>"REVOKE"</value>
            public const string REVOKE = "REVOKE"; //reserved
            /// <value>"RIGHT"</value>
            public const string RIGHT = "RIGHT"; //reserved
            /// <value>"ROLLBACK"</value>
            public const string ROLLBACK = "ROLLBACK"; //reserved
            /// <value>"ROLLUP"</value>
            public const string ROLLUP = "ROLLUP"; //reserved
            /// <value>"ROW"</value>
            public const string ROW = "ROW"; //reserved
            /// <value>"ROWS"</value>
            public const string ROWS = "ROWS"; //reserved
            /// <value>"SAVEPOINT"</value>
            public const string SAVEPOINT = "SAVEPOINT"; //reserved
            /// <value>"SCOPE"</value>
            public const string SCOPE = "SCOPE"; //reserved
            /// <value>"SCROLL"</value>
            public const string SCROLL = "SCROLL"; //reserved
            /// <value>"SECOND"</value>
            public const string SECOND = "SECOND"; //reserved
            /// <value>"SEARCH"</value>
            public const string SEARCH = "SEARCH"; //reserved
            /// <value>"SELECT"</value>
            public const string SELECT = "SELECT"; //reserved
            /// <value>"SENSITIVE"</value>
            public const string SENSITIVE = "SENSITIVE"; //reserved
            /// <value>"SESSION_USER"</value>
            public const string SESSION_USER = "SESSION_USER"; //reserved
            /// <value>"SET"</value>
            public const string SET = "SET"; //reserved
            /// <value>"SIGNAL"</value>
            public const string SIGNAL = "SIGNAL"; //reserved
            /// <value>"SIMILAR"</value>
            public const string SIMILAR = "SIMILAR"; //reserved
            /// <value>"SMALLINT"</value>
            public const string SMALLINT = "SMALLINT"; //reserved
            /// <value>"SOME"</value>
            public const string SOME = "SOME"; //reserved
            /// <value>"SPECIFIC"</value>
            public const string SPECIFIC = "SPECIFIC"; //reserved
            /// <value>"SPECIFICTYPE"</value>
            public const string SPECIFICTYPE = "SPECIFICTYPE"; //reserved
            /// <value>"SQL"</value>
            public const string SQL = "SQL"; //reserved
            /// <value>"SQLEXCEPTION"</value>
            public const string SQLEXCEPTION = "SQLEXCEPTION"; //reserved
            /// <value>"SQLSTATE"</value>
            public const string SQLSTATE = "SQLSTATE"; //reserved
            /// <value>"SQLWARNING"</value>
            public const string SQLWARNING = "SQLWARNING"; //reserved
            /// <value>"START"</value>
            public const string START = "START"; //reserved
            /// <value>"STATIC"</value>
            public const string STATIC = "STATIC"; //reserved
            /// <value>"SUBMULTISET"</value>
            public const string SUBMULTISET = "SUBMULTISET"; //reserved
            /// <value>"SYMMETRIC"</value>
            public const string SYMMETRIC = "SYMMETRIC"; //reserved
            /// <value>"SYSTEM"</value>
            public const string SYSTEM = "SYSTEM"; //reserved
            /// <value>"SYSTEM_USER"</value>
            public const string SYSTEM_USER = "SYSTEM_USER"; //reserved
            /// <value>"TABLE"</value>
            public const string TABLE = "TABLE"; //reserved
            /// <value>"TABLESAMPLE"</value>
            public const string TABLESAMPLE = "TABLESAMPLE"; //reserved
            /// <value>"THEN"</value>
            public const string THEN = "THEN"; //reserved
            /// <value>"TIME"</value>
            public const string TIME = "TIME"; //reserved
            /// <value>"TIMESTAMP"</value>
            public const string TIMESTAMP = "TIMESTAMP"; //reserved
            /// <value>"TIMEZONE_HOUR"</value>
            public const string TIMEZONE_HOUR = "TIMEZONE_HOUR"; //reserved
            /// <value>"TIMEZONE_MINUTE"</value>
            public const string TIMEZONE_MINUTE = "TIMEZONE_MINUTE"; //reserved
            /// <value>"TO"</value>
            public const string TO = "TO"; //reserved
            /// <value>"TRAILING"</value>
            public const string TRAILING = "TRAILING"; //reserved
            /// <value>"TRANSLATION"</value>
            public const string TRANSLATION = "TRANSLATION"; //reserved
            /// <value>"TREAT"</value>
            public const string TREAT = "TREAT"; //reserved
            /// <value>"TRIGGER"</value>
            public const string TRIGGER = "TRIGGER"; //reserved
            /// <value>"TRUE"</value>
            public const string TRUE = "TRUE"; //reserved
            /// <value>"UNDO"</value>
            public const string UNDO = "UNDO"; //reserved
            /// <value>"UNION"</value>
            public const string UNION = "UNION"; //reserved
            /// <value>"UNIQUE"</value>
            public const string UNIQUE = "UNIQUE"; //reserved
            /// <value>"UNKNOWN"</value>
            public const string UNKNOWN = "UNKNOWN"; //reserved
            /// <value>"UNNEST"</value>
            public const string UNNEST = "UNNEST"; //reserved
            /// <value>"UNTIL"</value>
            public const string UNTIL = "UNTIL"; //reserved
            /// <value>"UPDATE"</value>
            public const string UPDATE = "UPDATE"; //reserved
            /// <value>"USER"</value>
            public const string USER = "USER"; //reserved
            /// <value>"USING"</value>
            public const string USING = "USING"; //reserved
            /// <value>"VALUE"</value>
            public const string VALUE = "VALUE"; //reserved
            /// <value>"VALUES"</value>
            public const string VALUES = "VALUES"; //reserved
            /// <value>"VARCHAR"</value>
            public const string VARCHAR = "VARCHAR"; //reserved
            /// <value>"VARYING"</value>
            public const string VARYING = "VARYING"; //reserved
            /// <value>"WHEN"</value>
            public const string WHEN = "WHEN"; //reserved
            /// <value>"WHENEVER"</value>
            public const string WHENEVER = "WHENEVER"; //reserved
            /// <value>"WHERE"</value>
            public const string WHERE = "WHERE"; //reserved
            /// <value>"WHILE"</value>
            public const string WHILE = "WHILE"; //reserved
            /// <value>"WINDOW"</value>
            public const string WINDOW = "WINDOW"; //reserved
            /// <value>"WITH"</value>
            public const string WITH = "WITH"; //reserved
            /// <value>"WITHIN"</value>
            public const string WITHIN = "WITHIN"; //reserved
            /// <value>"WITHOUT"</value>
            public const string WITHOUT = "WITHOUT"; //reserved
            /// <value>"YEAR"</value>
            public const string YEAR = "YEAR"; //reserved 
            #endregion

            #region Other Tokens
            /// <value>"ALWAYS"</value>
            public const string ALWAYS = "ALWAYS";
            /// <value>"ACTION"</value>
            public const string ACTION = "ACTION";
            /// <value>"ADMIN"</value>
            public const string ADMIN = "ADMIN";
            /// <value>"AFTER"</value>
            public const string AFTER = "AFTER";
            /// <value>"ALIAS"</value>
            public const string ALIAS = "ALIAS";
            /// <value>"ASC"</value>
            public const string ASC = "ASC";
            /// <value>"AUTOCOMMIT"</value>
            public const string AUTOCOMMIT = "AUTOCOMMIT";
            /// <value>"AVG"</value>
            public const string AVG = "AVG";
            /// <value>"BACKUP"</value>
            public const string BACKUP = "BACKUP";
            /// <value>"BEFORE"</value>
            public const string BEFORE = "BEFORE";
            /// <value>"CACHED"</value>
            public const string CACHED = "CACHED";
            /// <value>"CASCADE"</value>
            public const string CASCADE = "CASCADE";
            /// <value>"CASEWHEN"</value>
            public const string CASEWHEN = "CASEWHEN";
            /// <value>"CHECKPOINT"</value>
            public const string CHECKPOINT = "CHECKPOINT";
            /// <value>"CLASS"</value>
            public const string CLASS = "CLASS";
            /// <value>"COALESCE"</value>
            public const string COALESCE = "COALESCE";
            /// <value>"COLLATION"</value>
            public const string COLLATION = "COLLATION";
            /// <value>"COMPACT"</value>
            public const string COMPACT = "COMPACT";
            /// <value>"COMPRESSED"</value>
            public const string COMPRESSED = "COMPRESSED";
            /// <value>"CONVERT"</value>
            public const string CONVERT = "CONVERT";
            /// <value>"COUNT"</value>
            public const string COUNT = "COUNT";
            /// <value>"DATABASE"</value>
            public const string DATABASE = "DATABASE";
            /// <value>"DEFRAG"</value>
            public const string DEFRAG = "DEFRAG";
            /// <value>"DESC"</value>
            public const string DESC = "DESC";
            /// <value>"EVERY"</value>
            public const string EVERY = "EVERY";
            /// <value>"EXPLAIN"</value>
            public const string EXPLAIN = "EXPLAIN";
            /// <value>"EXTRACT"</value>
            public const string EXTRACT = "EXTRACT";
            /// <value>"GENERATED"</value>
            public const string GENERATED = "GENERATED";
            /// <value>"IFNULL"</value>
            public const string IFNULL = "IFNULL";
            /// <value>"IGNORECASE"</value>
            public const string IGNORECASE = "IGNORECASE";
            /// <value>"IMMEDIATELY"</value>
            public const string IMMEDIATELY = "IMMEDIATELY";
            /// <value>"INCREMENT"</value>
            public const string INCREMENT = "INCREMENT";
            /// <value>"INDEX"</value>
            public const string INDEX = "INDEX";
            /// <value>"INITIAL"</value>
            public const string INITIAL = "INITIAL";
            /// <value>"KEY"</value>
            public const string KEY = "KEY";
            /// <value>"LIMIT"</value>
            public const string LIMIT = "LIMIT";
            /// <value>"LOGSIZE"</value>
            public const string LOGSIZE = "LOGSIZE";
            /// <value>"MATCHED"</value>
            public const string MATCHED = "MATCHED";
            /// <value>"MAX"</value>
            public const string MAX = "MAX";
            /// <value>"MAXROWS"</value>
            public const string MAXROWS = "MAXROWS";
            /// <value>"MEMORY"</value>
            public const string MEMORY = "MEMORY";
            /// <value>"MERGE"</value>
            public const string MERGE = "MERGE";
            /// <value>"MIN"</value>
            public const string MIN = "MIN";
            /// <value>"MINUS"</value>
            public const string MINUS = "MINUS";
            /// <value>"NOW"</value>
            public const string NOW = "NOW";
            /// <value>"NOWAIT"</value>
            public const string NOWAIT = "NOWAIT";
            /// <value>"NULLIF"</value>
            public const string NULLIF = "NULLIF";
            /// <value>"NVL"</value>
            public const string NVL = "NVL";
            /// <value>"OFFSET"</value>
            public const string OFFSET = "OFFSET";
            /// <value>"PASSWORD"</value>
            public const string PASSWORD = "PASSWORD";
            /// <value>"SCHEMA"</value>
            public const string SCHEMA = "SCHEMA";
            /// <value>"PLAN"</value>
            public const string PLAN = "PLAN";
            /// <value>"PRESERVE"</value>
            public const string PRESERVE = "PRESERVE";
            /// <value>"PRIVILEGES"</value>
            public const string PRIVILEGES = "PRIVILEGES";
            /// <value>"POSITION"</value>
            public const string POSITION = "POSITION";
            /// <value>"PROPERTY"</value>
            public const string PROPERTY = "PROPERTY";
            /// <value>"PUBLIC"</value>
            public const string PUBLIC = "PUBLIC";
            /// <value>"QUEUE"</value>
            public const string QUEUE = "QUEUE";
            /// <value>"READONLY"</value>
            public const string READONLY = "READONLY";
            /// <value>"REFERENTIAL_INTEGRITY"</value>
            public const string REFERENTIAL_INTEGRITY = "REFERENTIAL_INTEGRITY";
            /// <value>"RENAME"</value>
            public const string RENAME = "RENAME";
            /// <value>"RESTART"</value>
            public const string RESTART = "RESTART";
            /// <value>"RESTRICT"</value>
            public const string RESTRICT = "RESTRICT";
            /// <value>"ROLE"</value>
            public const string ROLE = "ROLE";
            /// <value>"SCRIPT"</value>
            public const string SCRIPT = "SCRIPT";
            /// <value>"SCRIPTFORMAT"</value>
            public const string SCRIPTFORMAT = "SCRIPTFORMAT";
            /// <value>"SEQUENCE"</value>
            public const string SEQUENCE = "SEQUENCE";
            /// <value>"SHUTDOWN"</value>
            public const string SHUTDOWN = "SHUTDOWN";
            /// <value>"SOURCE"</value>
            public const string SOURCE = "SOURCE";
            /// <value>"STDDEV_POP"</value>
            public const string STDDEV_POP = "STDDEV_POP";
            /// <value>"STDDEV_SAMP"</value>
            public const string STDDEV_SAMP = "STDDEV_SAMP";
            /// <value>"SUBSTRING"</value>
            public const string SUBSTRING = "SUBSTRING";
            /// <value>"SUM"</value>
            public const string SUM = "SUM";
            /// <value>"SYSDATE"</value>
            public const string SYSDATE = "SYSDATE";
            /// <value>"TEMP"</value>
            public const string TEMP = "TEMP";
            /// <value>"TEMPORARY"</value>
            public const string TEMPORARY = "TEMPORARY";
            /// <value>"TEXT"</value>
            public const string TEXT = "TEXT";
            /// <value>"TODAY"</value>
            public const string TODAY = "TODAY";
            /// <value>"TOP"</value>
            public const string TOP = "TOP";
            /// <value>"TRIM"</value>
            public const string TRIM = "TRIM";
            /// <value>"VAR_POP"</value>
            public const string VAR_POP = "VAR_POP";
            /// <value>"VAR_SAMP"</value>
            public const string VAR_SAMP = "VAR_SAMP";
            /// <value>"VIEW"</value>
            public const string VIEW = "VIEW";
            /// <value>"VIEW"</value>
            public const string WORK = "WORK";
            /// <value>"WRITE_DELAY"</value>
            public const string WRITE_DELAY = "WRITE_DELAY";
            #endregion

            #endregion
        }
    }
}