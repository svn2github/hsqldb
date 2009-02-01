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
using System.Collections;
using System.Globalization;
using System.Collections.Generic;
using System.Data.Hsqldb.Common.Enumeration;
using DefaultValueOf = System.Data.Hsqldb.Client.HsqlConnectionStringBuilder.DefaultValueOf;
using Keyword = System.Data.Hsqldb.Common.Enumeration.ConnectionStringKeyword;
using Key = System.Data.Hsqldb.Client.HsqlConnectionStringBuilder.ConnectionStringKey;
using SWB = org.hsqldb.scriptio.ScriptWriterBase;
using HDP = org.hsqldb.persist.HsqlDatabaseProperties;
using SL = org.hsqldb.lib.SimpleLog;

#endregion

namespace System.Data.Hsqldb.Client
{
    public sealed partial class HsqlConnectionStringBuilder
    {
        #region Util

        /// <summary>
        /// Misc. utility methods in support of 
        /// <see cref="HsqlConnectionStringBuilder"/>.
        /// </summary>
        internal static class Util
        {
            #region Constants

            internal const string __hsqldb_catalogs = "hsqldb.catalogs";
            internal const string __sql_enforce_strict_size = "sql.enforce_strict_size";
            internal const string __ifexists = "ifexists";
            internal const string __shutdown = "shutdown";
            internal const string __get_column_name = "get_column_name";
            internal const string __default_schema = "default_schema";
            //
            internal const string __datasource = "Data Source";
            internal const string __jdbcurl = "JdbcURL";
            internal const string __startupcommands = "StartupCommands";

            #endregion

            #region Fields
            private static readonly Dictionary<string, Keyword> m_keywordMap 
                = NewKeywordMap();
            #endregion

            #region Methods

            #region NewKeywordMap()
            /// <summary>
            /// Creates and initializes a new keyword map.
            /// </summary>
            /// <returns>The new map</returns>
            private static Dictionary<string, Keyword> NewKeywordMap()
            {
                Dictionary<string, Keyword> map = new Dictionary<string, Keyword>(StringComparer.InvariantCultureIgnoreCase);
                //
                map[Key.AutoShutdown] = Keyword.AutoShutdown;
                map[Key.CacheFileScale] = Keyword.CacheFileScale;
                map[Key.CacheScale] = Keyword.CacheScale;
                map[Key.CacheSizeScale] = Keyword.CacheSizeScale;
                map[Key.DatabaseAppLogLevel] = Keyword.DatabaseAppLogLevel;
                map[Key.DatabaseScriptFormat] = Keyword.DatabaseScriptFormat;
                map[__datasource] = Keyword.DataSource;
                map[Key.DefaultSchemaQualification] = Keyword.DefaultSchemaQualification;
                map[Key.DefaultTableType] = Keyword.DefaultTableType;
                map[Key.DefragLimit] = Keyword.DefragLimit;
                map[Key.EnforceColumnSize] = Keyword.EnforceColumnSize;
#if SYSTRAN
                map[Key.Enlist] = Keyword.Enlist;
#endif
                map[Key.Host] = Keyword.Host;
                map[Key.IfExists] = Keyword.IfExists;
                map[Key.InitialSchema] = Keyword.InitialSchema;
                map[__jdbcurl] = Keyword.JdbcURL;
                map[Key.MemoryMappedDataFile] = Keyword.MemoryMappedDataFile;
                map[Key.Password] = Keyword.Password;
                map[Key.Path] = Keyword.Path;
                map[Key.Port] = Keyword.Port;
                map[Key.Protocol] = Keyword.Protocol;
                map[Key.ReadOnlySession] = Keyword.ReadOnlySession;
                map[Key.ReportBaseColumnName] = Keyword.ReportBaseColumnName;
                map[Key.ReportCatalogs] = Keyword.ReportCatalogs;
                map[__startupcommands] = Keyword.StartupCommands;
                map[Key.TextDbAllowFullPath] = Keyword.TextDbAllowFullPath;
                map[Key.TextDbAllQuoted] = Keyword.TextDbAllQuoted;
                map[Key.TextDbCacheScale] = Keyword.TextDbCacheScale;
                map[Key.TextDbCacheSizeScale] = Keyword.TextDbCacheSizeScale;
                map[Key.TextDbEncoding] = Keyword.TextDbEncoding;
                map[Key.TextDbFieldSeparator] = Keyword.TextDbFieldSeparator;
                map[Key.TextDbIgnoreFirst] = Keyword.TextDbIgnoreFirst;
                map[Key.TextDbLongVarcharFieldSeparator] = Keyword.TextDbLongVarcharFieldSeparator;
                map[Key.TextDbQuoted] = Keyword.TextDbQuoted;
                map[Key.TextDbVarcharFieldSeparator] = Keyword.TextDbVarcharFieldSeparator;
                map[Key.TransactionLogMaxSize] = Keyword.TransactionLogMaxSize;
                map[Key.TransactionLogMaxSyncDelay] = Keyword.TransactionLogMaxSyncDelay;
                map[Key.TransactionNoMultiRewrite] = Keyword.TransactionNoMultiRewrite;
                map[Key.UserId] = Keyword.UserId;

                // org.hsqldb.persist.HsqlDatabaseProperties public usage keys.
                map[HDP.hsqldb_applog] = Keyword.DatabaseAppLogLevel;
                map[HDP.hsqldb_cache_file_scale] = Keyword.CacheFileScale;
                map[HDP.hsqldb_cache_scale] = Keyword.CacheScale;
                map[HDP.hsqldb_cache_size_scale] = Keyword.CacheSizeScale;
                map[HDP.hsqldb_default_table_type] = Keyword.DefaultTableType;
                map[HDP.hsqldb_defrag_limit] = Keyword.DefragLimit;
                map[HDP.hsqldb_log_size] = Keyword.TransactionLogMaxSize;
                map[HDP.hsqldb_nio_data_file] = Keyword.MemoryMappedDataFile;
                map[HDP.hsqldb_script_format] = Keyword.DatabaseScriptFormat;
                map[HDP.sql_tx_no_multi_write] = Keyword.TransactionNoMultiRewrite;
                map[HDP.textdb_all_quoted] = Keyword.TextDbAllQuoted;
                map[HDP.textdb_allow_full_path] = Keyword.TextDbAllowFullPath;
                map[HDP.textdb_cache_scale] = Keyword.TextDbCacheScale;
                map[HDP.textdb_cache_size_scale] = Keyword.TextDbCacheSizeScale;
                map[HDP.textdb_encoding] = Keyword.TextDbEncoding;
                map[HDP.textdb_fs] = Keyword.TextDbFieldSeparator;
                map[HDP.textdb_ignore_first] = Keyword.TextDbIgnoreFirst;
                map[HDP.textdb_lvs] = Keyword.TextDbLongVarcharFieldSeparator;
                map[HDP.textdb_quoted] = Keyword.TextDbQuoted;
                map[HDP.textdb_vs] = Keyword.TextDbVarcharFieldSeparator;

                // org.hsqldb.persist.HsqlDatabaseProperties private usage keys.
                map[__hsqldb_catalogs] = Keyword.ReportCatalogs;
                map[__sql_enforce_strict_size] = Keyword.EnforceColumnSize;

                // org.hsqldb.jdbc.jdbcConnection Properties keys
                map[__ifexists] = Keyword.IfExists;
                map[__shutdown] = Keyword.AutoShutdown;
                map[__get_column_name] = Keyword.ReportBaseColumnName;
                map[__default_schema] = Keyword.DefaultSchemaQualification;

                return map;
            }
            #endregion

            #region ConvertToXXX(object)

            #region ConvertToUShort(object)

            /// <summary>
            /// Converts the given object to an unsigned short.
            /// </summary>
            /// <param name="value">The object to convert.</param>
            /// <returns>the result of the conversion.</returns>
            /// <exception cref="FormatException">
            /// When <c>value</c> has an unsupported format.
            /// </exception>
            internal static ushort ConvertToUShort(object value)
            {
                try
                {
                    ushort uValue = (value as IConvertible)
                        .ToUInt16(CultureInfo.InvariantCulture);
                    return uValue;
                }
                catch (Exception ex)
                {
                    throw new FormatException(
                        "Value has an unsupported format: "
                        + Convert.ToString(value), ex);
                }
            }

            #endregion

            #region ConvertToUInt(object)

            /// <summary>
            /// Converts the given object to an unsigned int.
            /// </summary>
            /// <param name="value">The object to convert.</param>
            /// <returns>the result of the conversion.</returns>
            /// <exception cref="FormatException">
            /// When <c>value</c> has an unsupported format.
            /// </exception>
            internal static uint ConvertToUInt(object value)
            {
                try
                {
                    uint uValue = (value as IConvertible)
                        .ToUInt32(CultureInfo.InvariantCulture);
                    return uValue;
                }
                catch (Exception ex)
                {
                    throw new FormatException(
                        "Value has an unsupported format: "
                        + Convert.ToString(value), ex);
                }
            }

            #endregion

            #region ConvertToBool(object)

            /// <summary>
            /// Converts the given object to a bool.
            /// </summary>
            /// <param name="value">The object to convert.</param>
            /// <returns>the result of the conversion.</returns>
            /// <exception cref="FormatException">
            /// When <c>value</c> has an unsupported format.
            /// </exception>
            internal static bool ConvertToBool(object value)
            {
                if (value is string)
                {
                    string s = value.ToString().ToLowerInvariant();

                    if (s == "1" || s == "yes" || s == "true" || s == "on")
                    {
                        return true;
                    }
                    else if (s == "0" || s == "no" || s == "false" || s == "off")
                    {
                        return false;
                    }

                    throw new FormatException(
                        "Value has an unsupported format: "
                        + Convert.ToString(value, CultureInfo.InvariantCulture));
                }
                else
                {
                    try
                    {
                        return (value as IConvertible).ToBoolean(
                            CultureInfo.InvariantCulture);
                    }
                    catch (Exception ex)
                    {
                        throw new FormatException(
                            "Value has an unsupported format: "
                            + Convert.ToString(value, CultureInfo.InvariantCulture),
                            ex);
                    }
                }
            }

            #endregion

            #region ConvertToConnectionProtocol(object)

            /// <summary>
            /// Converts the given object to a <c>ConnectionProtocol</c>.
            /// </summary>
            /// <param name="value">The object to convert.</param>
            /// <returns>The result of the conversion.</returns>
            /// <exception cref="FormatException">
            /// When <c>value</c> has an unsupported format.
            /// </exception>
            internal static ConnectionProtocol ConvertToConnectionProtocol(
                object value)
            {
                try
                {
                    if (value == null)
                    {
                        return DefaultValueOf.Protocol;
                    }
                    else if (value is ConnectionProtocol)
                    {
                        return (ConnectionProtocol)value;
                    }
                    else if (value is string)
                    {
                        return (ConnectionProtocol)Enum.Parse(
                            typeof(ConnectionProtocol), value.ToString(), true);                                                               
                    }
                    else if (value is IConvertible)
                    {
                        byte b = ((IConvertible)value).ToByte(CultureInfo.InvariantCulture);
                        Type t = typeof(ConnectionProtocol);

                        return (ConnectionProtocol)Enum.ToObject(t, b);
                    }
                }
                catch (Exception ex)
                {
                    if (value is string)
                    {
                        string lowerString = (value as string).Trim().ToLower();

                        if (lowerString.StartsWith("jdbc:"))
                        {
                            lowerString = lowerString.Substring("jdbc:".Length);
                        }
                        if (lowerString.StartsWith("hsqldb:"))
                        {
                            lowerString = lowerString.Substring("hsqldb:".Length);
                        }

                        if (lowerString == String.Empty
                            || lowerString == "file:"
                            || lowerString == "file://")
                        {
                            return ConnectionProtocol.File;
                        }
                        else if (lowerString == "mem:"
                                 || lowerString == "mem://")
                        {
                            return ConnectionProtocol.Mem;
                        }
                        else if (lowerString == "res:"
                                 || lowerString == "res://")
                        {
                            return ConnectionProtocol.Res;
                        }
                        else if (lowerString == "http:"
                                 || lowerString == "http://")
                        {
                            return ConnectionProtocol.Http;
                        }
                        else if (lowerString == "https:"
                                 || lowerString == "https://")
                        {
                            return ConnectionProtocol.Https;
                        }
                        else if (lowerString == "hsql:"
                                 || lowerString == "hsql://")
                        {
                            return ConnectionProtocol.Hsql;
                        }
                        else if (lowerString == "hsqls:"
                                 || lowerString == "hsqls://")
                        {
                            return ConnectionProtocol.Hsqls;
                        }
                    }

                    throw new FormatException(
                        "Value has an unsupported format: "
                        + Convert.ToString(value), ex);
                }

                throw new FormatException(
                    "Value has an unsupported format :"
                    + Convert.ToString(value));
            }

            #endregion

            #region ConvertToDatabaseAppLogLevel(object)

            /// <summary>
            /// Converts the given object to a <c>DatabaseAppLogLevel</c>
            /// </summary>
            /// <param name="value">The object to convert.</param>
            /// <returns>The result of the conversion.</returns>
            /// <exception cref="FormatException">
            /// When <c>value</c> has an unsupported format.
            /// </exception>
            internal static DatabaseAppLogLevel ConvertToDatabaseAppLogLevel(object value)
            {
                try
                {
                    if (value == null)
                    {
                        return DefaultValueOf.DatabaseAppLogLevel;
                    }
                    else if (value is DatabaseAppLogLevel)
                    {
                        return (DatabaseAppLogLevel)value;
                    }
                    else if (value is string)
                    {
                        return (DatabaseAppLogLevel)Enum.Parse(
                            typeof(DatabaseAppLogLevel),
                            (value as string), 
                            true);
                    }
                    else if (value is IConvertible)
                    {
                        byte b = ((IConvertible)value).ToByte(
                            CultureInfo.InvariantCulture);
                        Type t = typeof(DatabaseAppLogLevel);

                        return (DatabaseAppLogLevel)Enum.ToObject(t, b);
                    }
                }
                catch (Exception ex)
                {
                    throw new FormatException(
                        "Value has an unsupported format: "
                        + Convert.ToString(value), ex);
                }

                throw new FormatException(
                    "Value has an unsupported format: "
                    + Convert.ToString(value));
            }

            #endregion

            #region ConvertToDatabaseScriptFormat(object)
            /// <summary>
            /// Converts the given object to a <c>DatabaseScriptFormat</c>
            /// </summary>
            /// <param name="value">The object to convert.</param>
            /// <returns>The result of the conversion.</returns>
            /// <exception cref="FormatException">
            /// When <c>value</c> has an unsupported format.
            /// </exception>
            internal static DatabaseScriptFormat ConvertToDatabaseScriptFormat(
                object value)
            {
                try
                {
                    if (value == null)
                    {
                        return DefaultValueOf.DatabaseScriptFormat;
                    }
                    else if (value is DatabaseScriptFormat)
                    {
                        return (DatabaseScriptFormat)value;
                    }
                    else if (value is string)
                    {
                        return (DatabaseScriptFormat)Enum.Parse(
                                                          typeof(DatabaseScriptFormat),
                                                          (value as string), true);
                    }
                    else if (value is IConvertible)
                    {
                        byte b = ((IConvertible)value)
                            .ToByte(CultureInfo.InvariantCulture);
                        Type t = typeof(DatabaseScriptFormat);

                        return (DatabaseScriptFormat)Enum.ToObject(t, b);
                    }
                }
                catch (Exception ex)
                {
                    throw new FormatException(
                        "Value has an unsupported format: "
                        + Convert.ToString(value), ex);
                }

                throw new FormatException(
                    "Value has an unsupported format: "
                    + Convert.ToString(value));
            }

            #endregion

            #region ConvertToDefaultTableType(object)

            /// <summary>
            /// Converts the given object to a <c>DefaultTableType</c>
            /// </summary>
            /// <param name="value">The object to convert.</param>
            /// <returns>The result of the conversion.</returns>
            /// <exception cref="FormatException">
            /// When <c>value</c> has an unsupported format.
            /// </exception>
            internal static DefaultTableType ConvertToDefaultTableType(object value)
            {
                try
                {
                    if (value == null)
                    {
                        return DefaultValueOf.DefaultTableType;
                    }

                    string stringValue = value as string;

                    if (stringValue != null)
                    {
                        return (DefaultTableType)Enum.Parse(
                                                      typeof(DefaultTableType),
                                                      stringValue,
                                                      true);
                    }

                    IConvertible convertible = value as IConvertible;

                    if (convertible != null)
                    {
                        byte b = convertible.ToByte(CultureInfo.InvariantCulture);
                        Type t = typeof(DefaultTableType);

                        return (DefaultTableType)Enum.ToObject(t, b);
                    }
                }
                catch (Exception ex)
                {
                    throw new FormatException(
                        "Value has an unsupported format: "
                        + Convert.ToString(value), ex);
                }

                throw new FormatException(
                    "Value has an unsupported format: "
                    + Convert.ToString(value));
            }

            #endregion

            #endregion ConvertToXXX

            #region ToXXX(...)

            #region ToScriptWriterFormat(DatabaseScriptFormat)
            /// <summary>
            /// Retrieves the numeric database script format identifier
            /// corresponding to the given value.
            /// </summary>
            /// <param name="value">
            /// For which to retrieve the numeric database script format identifier.
            /// </param>
            /// <returns>
            /// The corresponding value.
            /// </returns>
            public static int ToScriptWriterFormat(DatabaseScriptFormat value)
            {
                int scriptFormat;

                switch (value)
                {
                    case DatabaseScriptFormat.Binary:
                        {
                            scriptFormat = SWB.SCRIPT_BINARY_172;

                            break;
                        }
                    case DatabaseScriptFormat.CompressedBinary:
                        {
                            scriptFormat = SWB.SCRIPT_ZIPPED_BINARY_172;

                            break;
                        }
                    case DatabaseScriptFormat.Text:
                    default:
                        {
                            scriptFormat = SWB.SCRIPT_TEXT_170;

                            break;
                        }
                }

                return scriptFormat;
            }
            #endregion

            #region ToScriptWriterFormatString(DatabaseScriptFormat)
            /// <summary>
            /// Retrieves the database script format specifier character sequence
            /// corresponding to the given value.
            /// </summary>
            /// <param name="value">
            /// For which to retrieve the database script format specifier character sequence.
            /// </param>
            /// <returns>
            /// The corresponding database script format specifier character sequence.
            /// </returns>
            public static string ToScriptWriterFormatString(DatabaseScriptFormat value)
            {
                string stringValue;

                switch (value)
                {
                    case DatabaseScriptFormat.Binary:
                        {
                            stringValue = "BINARY";

                            break;
                        }
                    case DatabaseScriptFormat.CompressedBinary:
                        {
                            stringValue = "COMPRESSED_BINARY";

                            break;
                        }
                    case DatabaseScriptFormat.Text:
                    default:
                        {
                            stringValue = "TEXT";

                            break;
                        }
                }

                return stringValue;
            }
            #endregion

            #region ToSimpleLogLevel(DatabaseAppLogLevel)
            /// <summary>
            /// Retrieves the numeric simple log level identifier
            /// corresponding to the given value.
            /// </summary>
            /// <param name="value">
            /// For which to retrieve the numeric simple log level identifier
            /// </param>
            /// <returns>
            /// The corresponding numeric simple log level identifier.
            /// </returns>
            internal static int ToSimpleLogLevel(DatabaseAppLogLevel value)
            {

                int logLevel;

                switch (value)
                {
                    case DatabaseAppLogLevel.Error:
                        {
                            logLevel = SL.LOG_ERROR;
                            break;
                        }
                    case DatabaseAppLogLevel.None:
                        {
                            logLevel = SL.LOG_NONE;
                            break;
                        }
                    case DatabaseAppLogLevel.Normal:
                    default:
                        {
                            logLevel = SL.LOG_NORMAL;

                            break;
                        }
                }

                //setProperty(HDP.hsqldb_applog, logLevel);

                return logLevel;
            }
            #endregion

            #region ToSqlTableType(DefaultTableType)
            /// <summary>
            /// Retrieves the SQL table type specifier character sequence
            /// corresponding to the given value.
            /// </summary>
            /// <param name="value">
            /// For which to retrieve the SQL table type specifier character sequence
            /// </param>
            /// <returns>
            /// The corresponding SQL table type specifier character sequence.
            /// </returns>
            internal static string ToSqlTableType(DefaultTableType value)
            {
                string sqlTableType;

                switch (value)
                {
                    case DefaultTableType.Cached:
                        {
                            sqlTableType = "CACHED";
                            break;
                        }
                    default:
                        {
                            sqlTableType = "MEMORY";

                            break;
                        }
                }

                return sqlTableType;
            }
            #endregion

            #region ToJdbcProtocol(ConnectionProtocol)
            /// <summary>
            /// Retrieves the JDBC connection protocol character sequence
            /// corresponding to the given value.
            /// </summary>
            /// <param name="value">
            /// For which to retrieve the the JDBC connection protocol character sequence.
            /// </param>
            /// <returns>
            /// The corresponding the JDBC connection protocol character sequence.
            /// </returns>
            internal static string ToJdbcProtocol(ConnectionProtocol value)
            {
                switch (value)
                {
                    case ConnectionProtocol.File:
                    default:
                        {
                            return "file:";
                        }
                    case ConnectionProtocol.Hsql:
                        {
                            return "hsql://";
                        }
                    case ConnectionProtocol.Hsqls:
                        {
                            return "hsqls://";
                        }
                    case ConnectionProtocol.Http:
                        {
                            return "http://";
                        }
                    case ConnectionProtocol.Https:
                        {
                            return "https://";
                        }
                    case ConnectionProtocol.Mem:
                        {
                            return "mem:";
                        }
                    case ConnectionProtocol.Res:
                        {
                            return "res:";
                        }
                }
            }
            #endregion

            #endregion

            #region GetKeyword(string)
            /// <summary>
            /// Gets the keyword associated with the given key value.
            /// </summary>
            /// <param name="key">The key value.</param>
            /// <returns>
            /// The associated keyword; 
            /// <c>null</c> if there is no such association.
            /// </returns>
            internal static Keyword? GetKeyword(string key)
            {
                Keyword value;

                if (m_keywordMap.TryGetValue(key, out value))
                {
                    return value;
                }

                return null;
            }
            #endregion

            #region ContainsKey(string)

            /// <summary>
            /// Determines whether the specified key is a recognized key.
            /// </summary>
            /// <param name="key">The key.</param>
            /// <returns>
            /// <c>true</c> if the specified key is recognized; otherwise, <c>false</c>.
            /// </returns>
            internal static bool ContainsKey(string key)
            {
                return m_keywordMap.ContainsKey(key);
            }

            #endregion

            #endregion

            #region Properties

            #region Keys

            /// <summary>
            /// Gets the recoginsed connection string keys as an <c>ICollection</c>.
            /// </summary>
            /// <value>The recognised connection string keys.</value>
            internal static ICollection Keys
            {
                get { return m_keywordMap.Keys; }
            }

            #endregion

            #endregion
        }

        #endregion
    }
}