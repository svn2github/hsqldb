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
using System.Collections.Specialized;
using System.Globalization;
using System.Text;
//
using java.io;
using System.Data.Hsqldb.Common.Enumeration;

using DefaultValueOf = System.Data.Hsqldb.Client.HsqlConnectionStringBuilder.DefaultValueOf;
using HDP = org.hsqldb.persist.HsqlDatabaseProperties;
using Key = System.Data.Hsqldb.Client.HsqlConnectionStringBuilder.ConnectionStringKey;
using Keyword = System.Data.Hsqldb.Common.Enumeration.ConnectionStringKeyword;
using Util = System.Data.Hsqldb.Client.HsqlConnectionStringBuilder.Util;
using System.Data.Hsqldb.Common;

#endregion

namespace System.Data.Hsqldb.Client.Internal
{
    #region HsqlConnectionSettings

    /// <summary>
    /// Represents a collection of HSQLDB connection settings.
    /// </summary>
    /// <author name="boucherb@users"/>
    internal sealed class HsqlConnectionSettings
    {
        #region Constants

        private const string __sql_enforce_strict_size = "sql.enforce_strict_size";
        private const string __get_column_name = "get_column_name";
        private const string __hsqldb_catalogs = "hsqldb.catalogs";
        private const string __auto_shutdown = "shutdown";
        private const string __write_delay = "WRITE_DELAY";

        #endregion

        #region Fields

        private HsqlConnectionStringBuilder m_builder;

        #region Property Backing Fields

        internal bool m_autoShutdown;
        internal byte m_cacheFileScale;
        internal byte m_cacheScale;
        internal byte m_cacheSizeScale;
        internal DatabaseAppLogLevel m_databaseAppLogLevel;
        internal DatabaseScriptFormat m_databaseScriptFormat;
        internal DefaultTableType m_defaultTableType;
        internal bool m_defaultSchemaQualification;
        internal ushort m_defragLimit;
        internal bool m_enforceColumnSize;
#if SYSTRAN
        internal bool m_enlist;
#endif
        internal string m_host;
        internal bool m_ifExists;
        internal string m_initialSchema;
        internal bool m_memoryMappedDataFile;
        internal string m_password;
        internal string m_path;
        internal ushort m_port;
        internal ConnectionProtocol m_protocol;
        internal bool m_readOnlySession;
        internal bool m_reportBaseColumnName;
        internal bool m_reportCatalogs;
        internal bool m_textDBAllowFullPath;
        internal bool m_textDbAllQuoted;
        internal byte m_textDbCacheScale;
        internal byte m_textDbCacheSizeScale;
        internal string m_textDbEncoding;
        internal string m_textDbFieldSeparator;
        internal bool m_textDbIgnoreFirst;
        internal string m_textDbLongVarcharFieldSeparator;
        internal bool m_textDbQuoted;
        internal string m_textDbVarcharFieldSeparator;
        internal uint m_transactionLogMaxSize;
        internal ushort m_transactionLogMaxSyncDelay;
        internal bool m_transactionNoMultRewrite;
        internal string m_userId;

        #endregion

        #endregion Instance Variables

        #region Constructors

        #region HsqlConnectionSettings()

        /// <summary>
        /// Constructs a new <c>HsqlConnectionSettings</c> instance.
        /// </summary>
        internal HsqlConnectionSettings () : this(null)
        {
        }

        #endregion

        #region HsqlConnectionSettings(HsqlConnectionStringBuilder)

        /// <summary>
        /// Constructs a new <c>HsqlConnectionSettings</c> instance
        /// with the given builder.
        /// </summary>
        /// <param name="builder">The builder.</param>
        internal HsqlConnectionSettings (HsqlConnectionStringBuilder builder)
        {
            m_builder = builder;

            Reset ();
        }

        #endregion

        #endregion

        #region Instance Methods

        #region Collection Methods

        #region GetValue(string)

        internal object GetValue (string key)
        {
            Keyword? keyword = Util.GetKeyword(key);

            return (keyword == null)
                ? null 
                : GetValue ((Keyword) keyword);
        }

        #endregion

        #region GetValue(Keyword)

        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Maintainability", "CA1502:AvoidExcessiveComplexity")]
        internal object GetValue (Keyword keyword)
        {
            #region switch (keyword)
            switch (keyword) {
                case Keyword.AutoShutdown:
                {
                    return m_autoShutdown;
                }
                case Keyword.CacheFileScale:
                {
                    return m_cacheFileScale;
                }
                case Keyword.CacheScale:
                {
                    return m_cacheScale;
                }
                case Keyword.CacheSizeScale:
                {
                    return m_cacheSizeScale;
                }
                case Keyword.DefaultSchemaQualification:
                {
                    return m_defaultSchemaQualification;
                }
                case Keyword.DefaultTableType:
                {
                    return m_defaultTableType;
                }
                case Keyword.DefragLimit:
                {
                    return m_defragLimit;
                }
                case Keyword.DatabaseAppLogLevel:
                {
                    return m_databaseAppLogLevel;
                }
                case Keyword.DatabaseScriptFormat:
                {
                    return m_databaseScriptFormat;
                }
                case Keyword.DataSource:
                {
                    return GetDataSource ();
                }
                case Keyword.EnforceColumnSize:
                {
                    return m_enforceColumnSize;
                }
#if SYSTRAN
                case Keyword.Enlist:
                {
                    return m_enlist;
                }
#endif
                case Keyword.Host:
                {
                    return m_host;
                }
                case Keyword.IfExists:
                {
                    return m_ifExists;
                }
                case Keyword.InitialSchema:
                {
                    return m_initialSchema;
                }
                case Keyword.JdbcURL:
                {
                    return GetJdbcUrl ();
                }
                case Keyword.MemoryMappedDataFile:
                {
                    return m_memoryMappedDataFile;
                }
                case Keyword.Password:
                {
                    return m_password;
                }
                case Keyword.Path:
                {
                    return m_path;
                }
                case Keyword.Port:
                {
                    return GetPort();
                }
                case Keyword.Protocol:
                {
                    return m_protocol;
                }
                case Keyword.ReadOnlySession:
                {
                    return m_readOnlySession;
                }
                case Keyword.ReportBaseColumnName:
                {
                    return m_reportBaseColumnName;
                }
                case Keyword.ReportCatalogs:
                {
                    return m_reportCatalogs;
                }
                case Keyword.StartupCommands:
                {
                    return GetStartupCommands ();
                }
                case Keyword.TextDbAllowFullPath:
                {
                    return m_textDBAllowFullPath;
                }
                case Keyword.TextDbAllQuoted:
                {
                    return m_textDbAllQuoted;
                }
                case Keyword.TextDbCacheScale:
                {
                    return m_textDbCacheScale;
                }
                case Keyword.TextDbCacheSizeScale:
                {
                    return m_textDbCacheSizeScale;
                }
                case Keyword.TextDbEncoding:
                {
                    return m_textDbEncoding;
                }
                case Keyword.TextDbFieldSeparator:
                {
                    return m_textDbFieldSeparator;
                }
                case Keyword.TextDbIgnoreFirst:
                {
                    return m_textDbIgnoreFirst;
                }
                case Keyword.TextDbLongVarcharFieldSeparator:
                {
                    return m_textDbLongVarcharFieldSeparator;
                }
                case Keyword.TextDbQuoted:
                {
                    return m_textDbQuoted;
                }
                case Keyword.TextDbVarcharFieldSeparator:
                {
                    return m_textDbVarcharFieldSeparator;
                }
                case Keyword.TransactionLogMaxSize:
                {
                    return m_transactionLogMaxSize;
                }
                case Keyword.TransactionLogMaxSyncDelay:
                {
                    return m_transactionLogMaxSyncDelay;
                }
                case Keyword.TransactionNoMultiRewrite:
                {
                    return m_transactionNoMultRewrite;
                }
                case Keyword.UserId:
                {
                    return m_userId;
                }
                default :
                {
                    return null; // never happens.
                }
            }
            #endregion
        }

        #endregion

        #region Remove(string)

        internal bool Remove (string key)
        {
            Keyword? keyword = Util.GetKeyword (key);

            return (keyword == null) 
                ? false 
                : Remove ((Keyword) keyword);
        }

        #endregion

        #region Remove(Keyword)

        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Maintainability", "CA1502:AvoidExcessiveComplexity")]
        internal bool Remove (Keyword keyword)
        {
            #region switch (keyword)
            switch (keyword) {
                case Keyword.AutoShutdown:
                {
                    SetAutoShutdown (DefaultValueOf.AutoShutdown);
                    break;
                }
                case Keyword.CacheFileScale:
                {
                    SetCacheFileScale (DefaultValueOf.CacheFileScale);
                    break;
                }
                case Keyword.CacheScale:
                {
                    SetCacheScale (DefaultValueOf.CacheScale);
                    break;
                }
                case Keyword.CacheSizeScale:
                {
                    SetCacheSizeScale (DefaultValueOf.CacheSizeScale);
                    break;
                }
                case Keyword.DatabaseAppLogLevel:
                {
                    SetDatabaseAppLogLevel (
                        DefaultValueOf.DatabaseAppLogLevel);
                    break;
                }
                case Keyword.DatabaseScriptFormat:
                {
                    SetDatabaseScriptFormat (
                        DefaultValueOf.DatabaseScriptFormat);
                    break;
                }
                case Keyword.DefaultSchemaQualification:
                {
                    SetDefaultSchemaQualification (
                        DefaultValueOf.DefaultSchemaQualification);
                    break;
                }
                case Keyword.DefaultTableType:
                {
                    SetDefaultTableType (DefaultValueOf.DefaultTableType);
                    break;
                }
                case Keyword.DefragLimit:
                {
                    SetDefragLimit (DefaultValueOf.DefragLimit);
                    break;
                }
                case Keyword.EnforceColumnSize:
                {
                    SetEnforceColumnSize (DefaultValueOf.EnforceColumnSize);
                    break;
                }
#if SYSTRAN
                case Keyword.Enlist:
                {
                    SetEnlist (DefaultValueOf.Enlist);
                    break;
                }
#endif
                case Keyword.Host:
                {
                    SetHost (DefaultValueOf.Host);
                    break;
                }
                case Keyword.IfExists:
                {
                    SetIfExists (DefaultValueOf.IfExists);
                    break;
                }
                case Keyword.InitialSchema:
                {
                    SetInitialSchema (DefaultValueOf.InitialSchema);
                    break;
                }
                case Keyword.MemoryMappedDataFile:
                {
                    SetMemoryMappedDataFile (
                        DefaultValueOf.MemoryMappedDataFile);
                    break;
                }
                case Keyword.Password:
                {
                    SetPassword (DefaultValueOf.Password);
                    break;
                }
                case Keyword.Path:
                {
                    SetPath (DefaultValueOf.Path);
                    break;
                }
                case Keyword.Port:
                {
                    SetPort (DefaultValueOf.Port (m_protocol));
                    break;
                }
                case Keyword.Protocol:
                {
                    SetProtocol (DefaultValueOf.Protocol);
                    break;
                }
                case Keyword.ReadOnlySession:
                {
                    SetReadOnlySession(DefaultValueOf.ReadOnlySession);
                    break;
                }
                case Keyword.ReportBaseColumnName:
                {
                    SetReportBaseColumnName (
                        DefaultValueOf.ReportBaseColumnName);
                    break;
                }
                case Keyword.ReportCatalogs:
                {
                    SetReportCatalogs (DefaultValueOf.ReportCatalogs);
                    break;
                }
                case Keyword.TextDbAllowFullPath:
                {
                    SetTextDbAllowFullPath (
                        DefaultValueOf.TextDbAllowFullPath);
                    break;
                }
                case Keyword.TextDbAllQuoted:
                {
                    SetTextDbAllQuoted (DefaultValueOf.TextDbAllQuoted);
                    break;
                }
                case Keyword.TextDbCacheScale:
                {
                    SetTextDbCacheScale (DefaultValueOf.TextDbCacheScale);
                    break;
                }
                case Keyword.TextDbCacheSizeScale:
                {
                    SetTextDbCacheSizeScale (
                        DefaultValueOf.TextDbCacheSizeScale);
                    break;
                }
                case Keyword.TextDbEncoding:
                {
                    SetTextDbEncoding (DefaultValueOf.TextDbEncoding);
                    break;
                }
                case Keyword.TextDbFieldSeparator:
                {
                    SetTextDbFieldSeparator (
                        DefaultValueOf.TextDbFieldSeparator);
                    break;
                }
                case Keyword.TextDbIgnoreFirst:
                {
                    SetTextDbIgnoreFirst (DefaultValueOf.TextDbIgnoreFirst);
                    break;
                }
                case Keyword.TextDbLongVarcharFieldSeparator:
                {
                    SetTextDbLongVarcharFieldSeparator (
                        DefaultValueOf.TextDbLongVarcharFieldSeparator);
                    break;
                }
                case Keyword.TextDbQuoted:
                {
                    SetTextDbQuoted (DefaultValueOf.TextDbQuoted);
                    break;
                }
                case Keyword.TextDbVarcharFieldSeparator:
                {
                    SetTextDbVarcharFieldSeparator (
                        DefaultValueOf.TextDbVarcharFieldSeparator);
                    break;
                }
                case Keyword.TransactionLogMaxSize:
                {
                    SetTransactionLogMaxSize (
                        DefaultValueOf.TransactionLogMaxSize);
                    break;
                }
                case Keyword.TransactionLogMaxSyncDelay:
                {
                    SetTransactionLogMaxSyncDelay (
                        DefaultValueOf.TransactionLogMaxSyncDelay);
                    break;
                }
                case Keyword.TransactionNoMultiRewrite:
                {
                    SetTransactionNoMultiRewrite (
                        DefaultValueOf.TransactionNoMultiRewrite);
                    break;
                }
                case Keyword.UserId:
                {
                    SetUserId (DefaultValueOf.UserId);
                    break;
                }
                default :
                {
                    break;
                }
            }
            #endregion switch ((Keyword)keyword)

            return false;
        }

        #endregion

        #region Reset()

        internal void Reset ()
        {
            SetAutoShutdown (DefaultValueOf.AutoShutdown);
            SetCacheFileScale (DefaultValueOf.CacheFileScale);
            SetCacheScale (DefaultValueOf.CacheScale);
            SetCacheSizeScale (DefaultValueOf.CacheSizeScale);
            SetDatabaseAppLogLevel (DefaultValueOf.DatabaseAppLogLevel);
            SetDatabaseScriptFormat (DefaultValueOf.DatabaseScriptFormat);
            SetDefaultSchemaQualification (
                DefaultValueOf.DefaultSchemaQualification);
            SetDefaultTableType (DefaultValueOf.DefaultTableType);
            SetDefragLimit (DefaultValueOf.DefragLimit);
            SetEnforceColumnSize (DefaultValueOf.EnforceColumnSize);
#if SYSTRAN
            SetEnlist (DefaultValueOf.Enlist);
#endif
            SetHost (DefaultValueOf.Host);
            SetIfExists (DefaultValueOf.IfExists);
            SetInitialSchema (DefaultValueOf.InitialSchema);
            SetMemoryMappedDataFile (DefaultValueOf.MemoryMappedDataFile);
            SetPassword (DefaultValueOf.Password);
            SetPath (DefaultValueOf.Path);
            SetPort (DefaultValueOf.Port (DefaultValueOf.Protocol));
            SetProtocol (DefaultValueOf.Protocol);
            SetReadOnlySession (DefaultValueOf.ReadOnlySession);
            SetReportCatalogs (DefaultValueOf.ReportCatalogs);
            SetTextDbAllowFullPath (DefaultValueOf.TextDbAllowFullPath);
            SetTextDbAllQuoted (DefaultValueOf.TextDbAllQuoted);
            SetTextDbCacheScale (DefaultValueOf.TextDbCacheScale);
            SetTextDbCacheSizeScale (DefaultValueOf.TextDbCacheSizeScale);
            SetTextDbEncoding (DefaultValueOf.TextDbEncoding);
            SetTextDbFieldSeparator (DefaultValueOf.TextDbFieldSeparator);
            SetTextDbIgnoreFirst (DefaultValueOf.TextDbIgnoreFirst);
            SetTextDbLongVarcharFieldSeparator (
                DefaultValueOf.TextDbLongVarcharFieldSeparator);
            SetTextDbQuoted (DefaultValueOf.TextDbQuoted);
            SetTextDbVarcharFieldSeparator (
                DefaultValueOf.TextDbVarcharFieldSeparator);
            SetTransactionLogMaxSize (DefaultValueOf.TransactionLogMaxSize);
            SetTransactionLogMaxSyncDelay (
                DefaultValueOf.TransactionLogMaxSyncDelay);
            SetTransactionNoMultiRewrite (
                DefaultValueOf.TransactionNoMultiRewrite);
            SetReportBaseColumnName (DefaultValueOf.ReportBaseColumnName);
            SetUserId (DefaultValueOf.UserId);
        }

        #endregion

        #region SetValue(string,object)

        internal void SetValue (string key, object value)
        {
            Keyword? keyword = Util.GetKeyword (key);

            if (keyword != null) {
                SetValue ((Keyword) keyword, value);
            }
        }

        #endregion

        #region SetValue(Keyword,object)

        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Maintainability", "CA1502:AvoidExcessiveComplexity")]
        internal void SetValue (Keyword keyword, object value)
        {
            #region switch(keyword)

            switch (keyword) {
                case Keyword.AutoShutdown:
                {
                    SetAutoShutdown (value);
                    break;
                }
                case Keyword.CacheFileScale:
                {
                    SetCacheFileScale (value);
                    break;
                }
                case Keyword.CacheScale:
                {
                    SetCacheScale (value);
                    break;
                }
                case Keyword.CacheSizeScale:
                {
                    SetCacheSizeScale (value);
                    break;
                }
                case Keyword.DatabaseAppLogLevel:
                {
                    SetDatabaseAppLogLevel (value);
                    break;
                }
                case Keyword.DatabaseScriptFormat:
                {
                    SetDatabaseScriptFormat (value);
                    break;
                }
                case Keyword.DataSource:
                {
                    SetDataSource(value);
                    break;
                }
                case Keyword.DefaultSchemaQualification:
                {
                    SetDefaultSchemaQualification (value);
                    break;
                }
                case Keyword.DefaultTableType:
                {
                    SetDefaultTableType (value);
                    break;
                }
                case Keyword.DefragLimit:
                {
                    SetDefragLimit (value);
                    break;
                }
                case Keyword.EnforceColumnSize:
                {
                    SetEnforceColumnSize (value);
                    break;
                }
#if SYSTRAN
                case Keyword.Enlist:
                {
                    SetEnlist (value);
                    break;
                }
#endif
                case Keyword.Host:
                {
                    SetHost (value);
                    break;
                }
                case Keyword.IfExists:
                {
                    SetIfExists (value);
                    break;
                }
                case Keyword.InitialSchema:
                {
                    SetInitialSchema (value);
                    break;
                }
                case Keyword.MemoryMappedDataFile:
                {
                    SetMemoryMappedDataFile (value);
                    break;
                }
                case Keyword.Password:
                {
                    SetPassword (value);
                    break;
                }
                case Keyword.Path:
                {
                    SetPath (value);
                    break;
                }
                case Keyword.Port:
                {
                    SetPort (value);
                    break;
                }
                case Keyword.Protocol:
                {
                    SetProtocol (value);
                    break;
                }
                case Keyword.ReadOnlySession:
                {
                    SetReadOnlySession(value);
                    break;
                }
                case Keyword.ReportBaseColumnName:
                {
                    SetReportBaseColumnName (value);
                    break;
                }
                case Keyword.ReportCatalogs:
                {
                    SetReportCatalogs (value);
                    break;
                }
                case Keyword.TextDbAllowFullPath:
                {
                    SetTextDbAllowFullPath (value);
                    break;
                }
                case Keyword.TextDbAllQuoted:
                {
                    SetTextDbAllQuoted (value);
                    break;
                }
                case Keyword.TextDbCacheScale:
                {
                    SetTextDbCacheScale (value);
                    break;
                }
                case Keyword.TextDbCacheSizeScale:
                {
                    SetTextDbCacheSizeScale (value);
                    break;
                }
                case Keyword.TextDbEncoding:
                {
                    SetTextDbEncoding (value);
                    break;
                }
                case Keyword.TextDbFieldSeparator:
                {
                    SetTextDbFieldSeparator (value);
                    break;
                }
                case Keyword.TextDbIgnoreFirst:
                {
                    SetTextDbIgnoreFirst (value);
                    break;
                }
                case Keyword.TextDbLongVarcharFieldSeparator:
                {
                    SetTextDbLongVarcharFieldSeparator (value);
                    break;
                }
                case Keyword.TextDbQuoted:
                {
                    SetTextDbQuoted (value);
                    break;
                }
                case Keyword.TextDbVarcharFieldSeparator:
                {
                    SetTextDbVarcharFieldSeparator (value);
                    break;
                }
                case Keyword.TransactionLogMaxSize:
                {
                    SetTransactionLogMaxSize (value);
                    break;
                }
                case Keyword.TransactionLogMaxSyncDelay:
                {
                    SetTransactionLogMaxSyncDelay (value);
                    break;
                }
                case Keyword.TransactionNoMultiRewrite:
                {
                    SetTransactionNoMultiRewrite (value);
                    break;
                }
                case Keyword.UserId:
                {
                    SetUserId (value);
                    break;
                }
                default :
                {
                    // never happens?.
                    break;
                }
            }

            #endregion switch(keyword)
        }

        #endregion

        #region ShouldSerialize(string)

        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Maintainability", "CA1502:AvoidExcessiveComplexity")]
        internal bool ShouldSerialize (string key)
        {
            Keyword? keyword = Util.GetKeyword (key);

            if (keyword == null) {
                return false;
            }

            bool isFile = (m_protocol == ConnectionProtocol.File);
            bool isMem = (m_protocol == ConnectionProtocol.Mem);
            bool isRes = (m_protocol == ConnectionProtocol.Res);
            bool isNetwork = !(isFile || isMem || isRes);

            #region switch ((Keyword)keyword)
            switch ((Keyword) keyword) {
                case Keyword.AutoShutdown:
                {
                    return (!isNetwork)
                        && (m_autoShutdown != DefaultValueOf.AutoShutdown);
                }
                case Keyword.CacheFileScale:
                {
                    return isFile
                        && (m_cacheFileScale != DefaultValueOf.CacheFileScale);
                }
                case Keyword.CacheScale:
                {
                    return isFile && (m_cacheScale != DefaultValueOf.CacheScale);
                }
                case Keyword.CacheSizeScale:
                {
                    return isFile
                        && (m_cacheSizeScale != DefaultValueOf.CacheSizeScale);
                }
                case Keyword.DatabaseAppLogLevel:
                {
                    return isFile
                        && (m_databaseAppLogLevel
                                != DefaultValueOf.DatabaseAppLogLevel);
                }
                case Keyword.DatabaseScriptFormat:
                {
                    return isFile
                        && (m_databaseScriptFormat
                                != DefaultValueOf.DatabaseScriptFormat);
                }
                case Keyword.DefaultSchemaQualification:
                {
                    return (m_defaultSchemaQualification
                            != DefaultValueOf.DefaultSchemaQualification);
                }
                case Keyword.DefaultTableType:
                {
                    return isFile
                        && (m_defaultTableType != DefaultValueOf.DefaultTableType);
                }
                case Keyword.DefragLimit:
                {
                    return isFile && (m_defragLimit != DefaultValueOf.DefragLimit);
                }
                case Keyword.EnforceColumnSize:
                {
                    return (!isNetwork)
                        && (m_enforceColumnSize != DefaultValueOf.EnforceColumnSize);
                }
#if SYSTRAN
                case Keyword.Enlist:
                {
                    return (m_enlist != DefaultValueOf.Enlist);
                }
#endif
                case Keyword.Host:
                {
                    return isNetwork;
                }
                case Keyword.IfExists:
                {
                    return (isFile || isMem)
                        && (m_ifExists != DefaultValueOf.IfExists);
                }
                case Keyword.InitialSchema:
                {
                    return (m_initialSchema != DefaultValueOf.InitialSchema);
                }
                case Keyword.MemoryMappedDataFile:
                {
                    return isFile
                        && (m_memoryMappedDataFile
                                != DefaultValueOf.MemoryMappedDataFile);
                }
                case Keyword.Password:
                {
                    return (m_password != DefaultValueOf.Password);
                }
                case Keyword.Path:
                {
                    return true;
                }
                case Keyword.Port:
                {
                    return isNetwork
                        && (m_port != DefaultValueOf.Port (m_protocol));
                }
                case Keyword.Protocol:
                {
                    return true;
                }
                case Keyword.ReadOnlySession:
                {
                    return (m_readOnlySession
                        != DefaultValueOf.ReadOnlySession);
                }
                case Keyword.ReportBaseColumnName:
                {
                    return (m_reportBaseColumnName
                            != DefaultValueOf.ReportBaseColumnName);
                }
                case Keyword.ReportCatalogs:
                {
                    return (!isNetwork)
                        && (m_reportCatalogs != DefaultValueOf.ReportCatalogs);
                }
                case Keyword.TextDbAllowFullPath:
                {
                    return isFile
                        && (m_textDBAllowFullPath
                                != DefaultValueOf.TextDbAllowFullPath);
                }
                case Keyword.TextDbAllQuoted:
                {
                    return isFile
                        && (m_textDbAllQuoted != DefaultValueOf.TextDbAllQuoted);
                }
                case Keyword.TextDbCacheScale:
                {
                    return isFile
                        && (m_textDbCacheScale != DefaultValueOf.TextDbCacheScale);
                }
                case Keyword.TextDbCacheSizeScale:
                {
                    return isFile
                        && (m_textDbCacheSizeScale
                                != DefaultValueOf.TextDbCacheSizeScale);
                }
                case Keyword.TextDbEncoding:
                {
                    return isFile
                        && (m_textDbEncoding != DefaultValueOf.TextDbEncoding);
                }
                case Keyword.TextDbFieldSeparator:
                {
                    return isFile
                        && (m_textDbFieldSeparator
                                != DefaultValueOf.TextDbFieldSeparator);
                }
                case Keyword.TextDbIgnoreFirst:
                {
                    return isFile
                        && (m_textDbIgnoreFirst != DefaultValueOf.TextDbIgnoreFirst);
                }
                case Keyword.TextDbLongVarcharFieldSeparator:
                {
                    return isFile
                        && (m_textDbLongVarcharFieldSeparator
                                != DefaultValueOf.TextDbLongVarcharFieldSeparator);
                }
                case Keyword.TextDbQuoted:
                {
                    return isFile
                        && (m_textDbQuoted != DefaultValueOf.TextDbQuoted);
                }
                case Keyword.TextDbVarcharFieldSeparator:
                {
                    return isFile
                        && (m_textDbVarcharFieldSeparator
                                != DefaultValueOf.TextDbVarcharFieldSeparator);
                }
                case Keyword.TransactionLogMaxSize:
                {
                    return isFile
                        && (m_transactionLogMaxSize
                                != DefaultValueOf.TransactionLogMaxSize);
                }
                case Keyword.TransactionLogMaxSyncDelay:
                {
                    return isFile
                        && (m_transactionLogMaxSyncDelay
                                != DefaultValueOf.TransactionLogMaxSyncDelay);
                }
                case Keyword.TransactionNoMultiRewrite:
                {
                    return (!isNetwork)
                        && (m_transactionNoMultRewrite
                                != DefaultValueOf.TransactionNoMultiRewrite);
                }
                case Keyword.UserId:
                {
                    return true;
                }
                default :
                {
                    return false;
                }
            }

            #endregion switch ((Keyword)keyword)
        }

        #endregion

        #endregion

        #region Property Setter Methods

        #region SetAutoShutdown(object)

        internal void SetAutoShutdown (object value)
        {
            if (value == null) {
                m_autoShutdown = DefaultValueOf.AutoShutdown;
            } else if (value is bool) {
                m_autoShutdown = (bool) value;
            } else {
                m_autoShutdown = Util.ConvertToBool (value);
            }

            if (m_builder != null) {
                m_builder.SetBaseValue (Key.AutoShutdown, m_autoShutdown);
            }
        }

        #endregion

        #region SetCacheFileScale(object)

        internal void SetCacheFileScale (object value)
        {
            if (value == null) {
                m_cacheFileScale = DefaultValueOf.CacheFileScale;
            } else if (value is byte) {
                m_cacheFileScale = (byte) value;
            } else {
                m_cacheFileScale = Convert.ToByte (
                    value, CultureInfo.InvariantCulture);
            }

            if (m_cacheFileScale != 8) {
                m_cacheFileScale = 1;
            }

            m_builder.SetBaseValue (Key.CacheFileScale, m_cacheFileScale);
        }

        #endregion

        #region SetCacheScale(object)

        internal void SetCacheScale (object value)
        {
            if (value == null) {
                m_cacheScale = DefaultValueOf.CacheScale;
            } else if (value is byte) {
                m_cacheScale = (byte) value;
            } else {
                m_cacheScale = Convert.ToByte (
                    value, CultureInfo.InvariantCulture);
            }

            if (m_cacheScale < 8) 
            {
                m_cacheScale = 8;
            }
            else if (m_cacheScale > 18)
            {
                m_cacheScale = 18;
            }

            if (m_builder != null) {
                m_builder.SetBaseValue (Key.CacheScale, m_cacheScale);
            }
        }

        #endregion

        #region SetCacheSizeScale(object)

        internal void SetCacheSizeScale (object value)
        {
            if (value == null) {
                m_cacheSizeScale = DefaultValueOf.CacheSizeScale;
            } else if (value is byte) {
                m_cacheSizeScale = (byte) value;
            } else {
                m_cacheSizeScale = Convert.ToByte (
                    value, CultureInfo.InvariantCulture);
            }

            if (m_cacheSizeScale < 6) {
                m_cacheSizeScale = 6;
            } else if (m_cacheSizeScale > 20) {
                m_cacheSizeScale = 20;
            }

            if (m_builder != null) {
                m_builder.SetBaseValue (Key.CacheSizeScale, m_cacheSizeScale);
            }
        }

        #endregion

        #region SetDatabaseAppLogLevel(object)

        internal void SetDatabaseAppLogLevel (object value)
        {
            if (value == null) {
                m_databaseAppLogLevel = DefaultValueOf.DatabaseAppLogLevel;
            } else if (value is DatabaseAppLogLevel) {
                m_databaseAppLogLevel = (DatabaseAppLogLevel) value;
            } else {
                m_databaseAppLogLevel = Util.ConvertToDatabaseAppLogLevel (
                    value);
            }

            if (m_builder != null) {
                m_builder.SetBaseValue (
                    Key.DatabaseAppLogLevel, m_databaseAppLogLevel);
            }
        }

        #endregion

        #region SetDatabaseScriptFormat(object)

        internal void SetDatabaseScriptFormat (object value)
        {
            if (value == null) {
                m_databaseScriptFormat = DefaultValueOf.DatabaseScriptFormat;
            } else if (value is DatabaseScriptFormat) {
                m_databaseScriptFormat = (DatabaseScriptFormat) value;
            } else {
                m_databaseScriptFormat = Util.ConvertToDatabaseScriptFormat (
                    value);
            }

            if (m_builder != null) {
                m_builder.SetBaseValue (
                    Key.DatabaseScriptFormat, m_databaseScriptFormat);
            }
        }

        #endregion

        #region SetDefaultSchema(object)

        internal void SetDefaultSchemaQualification (object value)
        {
            if (value == null) {
                m_defaultSchemaQualification
                    = DefaultValueOf.DefaultSchemaQualification;
            } else if (value is bool) {
                m_defaultSchemaQualification = (bool) value;
            } else {
                m_defaultSchemaQualification
                    = Convert.ToBoolean (value, CultureInfo.InvariantCulture);
            }

            if (m_builder != null) {
                m_builder.SetBaseValue (
                    Key.DefaultSchemaQualification,
                    m_defaultSchemaQualification);
            }
        }

        #endregion

        #region SetDefaultTableType(object)

        internal void SetDefaultTableType (object value)
        {
            if (value == null) {
                m_defaultTableType = DefaultValueOf.DefaultTableType;
            } else if (value is DefaultTableType) {
                m_defaultTableType = (DefaultTableType) value;
            } else {
                m_defaultTableType = Util.ConvertToDefaultTableType (value);
            }

            if (m_builder != null) {
                m_builder.SetBaseValue (
                    Key.DefaultTableType, m_defaultTableType);
            }
        }

        #endregion

        #region SetDefragLimit(object)

        internal void SetDefragLimit (object value)
        {
            if (value == null) {
                m_defragLimit = DefaultValueOf.DefragLimit;
            } else if (value is ushort) {
                m_defragLimit = (ushort) value;
            } else {
                m_defragLimit = Util.ConvertToUShort (value);
            }

            if (m_defragLimit <= 0) {
                m_defragLimit = 1;
            } else if (m_defragLimit > 1024) {
                m_defragLimit = 1024;
            }

            if (m_builder != null) {
                m_builder.SetBaseValue (Key.DefragLimit, m_defragLimit);
            }
        }

        #endregion

        #region SetEnforceColumnSize(object)

        internal void SetEnforceColumnSize (object value)
        {
            if (value == null) {
                m_enforceColumnSize = DefaultValueOf.EnforceColumnSize;
            } else if (value is bool) {
                m_enforceColumnSize = (bool) value;
            } else {
                m_enforceColumnSize = Util.ConvertToBool (value);
            }

            if (m_builder != null) {
                m_builder.SetBaseValue (
                    Key.EnforceColumnSize, m_enforceColumnSize);
            }
        }

        #endregion

#if SYSTRAN
        #region SetEnlist(object)

        internal void SetEnlist (object value)
        {
            if (value == null) {
                m_enlist = DefaultValueOf.Enlist;
            } else if (value is bool) {
                m_enlist = (bool) value;
            } else {
                m_enlist = Util.ConvertToBool (value);
            }

            if (m_builder != null) {
                m_builder.SetBaseValue (Key.Enlist, m_enlist);
            }
        }

        #endregion
#endif

        #region SetHost(object)

        internal void SetHost (object value)
        {
            string stringValue;

            if (value == null) {
                m_host = DefaultValueOf.Host;
            } else if (null != (stringValue = (value as string))) {
                m_host = stringValue;
            } else {
                m_host = Convert.ToString (value, CultureInfo.InvariantCulture);
            }

            if (m_builder != null) {
                m_builder.SetBaseValue (Key.Host, m_host);
            }
        }

        #endregion

        #region SetIfExists(object)

        internal void SetIfExists (object value)
        {
            if (value == null) {
                m_ifExists = DefaultValueOf.IfExists;
            } else if (value is bool) {
                m_ifExists = (bool) value;
            } else {
                m_ifExists = Util.ConvertToBool (value);
            }

            if (m_builder != null) {
                m_builder.SetBaseValue (Key.IfExists, m_ifExists);
            }
        }

        #endregion

        #region SetInitialSchema(object)

        internal void SetInitialSchema (object value)
        {
            string stringValue;

            if (value == null) {
                m_initialSchema = DefaultValueOf.InitialSchema;
            } else if (null != (stringValue = (value as string))) {
                m_initialSchema = stringValue;
            } else {
                m_initialSchema = Convert.ToString (
                    value, CultureInfo.InvariantCulture);
            }

            if (m_builder != null) {
                m_builder.SetBaseValue (Key.InitialSchema, m_initialSchema);
            }
        }

        #endregion

        #region SetMemoryMappedDataFile(object)

        internal void SetMemoryMappedDataFile (object value)
        {
            if (value == null) {
                m_memoryMappedDataFile = DefaultValueOf.MemoryMappedDataFile;
            } else if (value is bool) {
                m_memoryMappedDataFile = (bool) value;
            } else {
                m_memoryMappedDataFile = Util.ConvertToBool (value);
            }

            if (m_builder != null) {
                m_builder.SetBaseValue (
                    Key.MemoryMappedDataFile, m_memoryMappedDataFile);
            }
        }

        #endregion

        #region SetPassword(object)

        internal void SetPassword (object value)
        {
            string stringValue;

            if (value == null) {
                m_password = DefaultValueOf.Password;
            } else if (null != (stringValue = (value as string))) {
                m_password = stringValue;
            } else {
                m_password = Convert.ToString (
                    value, CultureInfo.InvariantCulture);
            }

            if (m_builder != null) {
                m_builder.SetBaseValue (Key.Password, m_password);
            }
        }

        #endregion

        #region SetPath(object)

        internal void SetPath (object value)
        {
            string stringValue;

            if (value == null) {
                m_path = DefaultValueOf.Path;
            } else if (null != (stringValue = (value as string))) {
                m_path = stringValue;
            } else {
                m_path = Convert.ToString (value, CultureInfo.InvariantCulture);
            }

            if (m_builder != null) {
                m_builder.SetBaseValue (Key.Path, m_path);
            }
        }

        #endregion

        #region SetPort(object)

        internal void SetPort (object value)
        {
            if (value == null) {
                m_port = DefaultValueOf.Port (m_protocol);
            } else if (value is ushort) {
                m_port = (ushort) value;
            } else {
                m_port = Util.ConvertToUShort (value);
            }

            if (m_builder != null) {
                m_builder.SetBaseValue (Key.Port, m_port);
            }
        }

        #endregion

        #region SetProtocol(object)

        internal void SetProtocol (object value)
        {
            if (value == null) {
                m_protocol = DefaultValueOf.Protocol;
            } else if (value is ConnectionProtocol) {
                m_protocol = (ConnectionProtocol) value;
            } else {
                m_protocol = Util.ConvertToConnectionProtocol (value);
            }

            if (m_builder != null) {
                m_builder.SetBaseValue (Key.Protocol, m_protocol);
            }
        }

        #endregion

        #region SetReadOnlySession(object)

        internal void SetReadOnlySession(object value)
        {
            if (value == null)
            {
                m_readOnlySession = DefaultValueOf.ReadOnlySession;
            }
            else if (value is bool)
            {
                m_readOnlySession = (bool)value;
            }
            else
            {
                m_readOnlySession = Util.ConvertToBool(value);
            }

            if (m_builder != null)
            {
                m_builder.SetBaseValue(
                    Key.ReadOnlySession, m_readOnlySession);
            }
        }

        #endregion

        #region SetReportBaseColumnName(object)

        internal void SetReportBaseColumnName (object value)
        {
            if (value == null) {
                m_reportBaseColumnName = DefaultValueOf.ReportBaseColumnName;
            } else if (value is bool) {
                m_reportBaseColumnName = (bool) value;
            } else {
                m_reportBaseColumnName = Util.ConvertToBool(value);
            }

            if (m_builder != null)
            {
                m_builder.SetBaseValue (
                    Key.ReportBaseColumnName, m_reportBaseColumnName);
            }
        }

        #endregion

        #region SetReportCatalogs(object)

        internal void SetReportCatalogs (object value)
        {
            if (value == null) {
                m_reportCatalogs = DefaultValueOf.ReportCatalogs;
            } else if (value is bool) {
                m_reportCatalogs = (bool) value;
            } else {
                m_reportCatalogs = Util.ConvertToBool (value);
            }

            if (m_builder != null) {
                m_builder.SetBaseValue (Key.ReportCatalogs, m_reportCatalogs);
            }
        }

        #endregion

        #region SetTextDbAllowFullPath(object)

        internal void SetTextDbAllowFullPath (object value)
        {
            if (value == null) {
                m_textDBAllowFullPath = DefaultValueOf.TextDbAllowFullPath;
            } else if (value is bool) {
                m_textDBAllowFullPath = (bool) value;
            } else {
                m_textDBAllowFullPath = Util.ConvertToBool (value);
            }

            if (m_builder != null) {
                m_builder.SetBaseValue (
                    Key.TextDbAllowFullPath, m_textDBAllowFullPath);
            }
        }

        #endregion

        #region SetTextDbAllQuoted(object)

        internal void SetTextDbAllQuoted (object value)
        {
            if (value == null) {
                m_textDbAllQuoted = DefaultValueOf.TextDbAllQuoted;
            } else if (value is bool) {
                m_textDbAllQuoted = (bool) value;
            } else {
                m_textDbAllQuoted = Util.ConvertToBool (value);
            }

            if (m_builder != null) {
                m_builder.SetBaseValue (Key.TextDbAllQuoted, m_textDbAllQuoted);
            }
        }

        #endregion

        #region SetTextDbCacheScale(object)

        internal void SetTextDbCacheScale (object value)
        {
            if (value == null) {
                m_textDbCacheScale = DefaultValueOf.TextDbCacheScale;
            } else if (value is byte) {
                m_textDbCacheScale = (byte) value;
            } else {
                m_textDbCacheScale = Convert.ToByte (
                    value, CultureInfo.InvariantCulture);
            }

            if (m_textDbCacheScale < 8) {
                m_textDbCacheScale = 8;
            } else if (m_textDbCacheScale > 16) {
                m_textDbCacheScale = 16;
            }

            if (m_builder != null) {
                m_builder.SetBaseValue (
                    Key.TextDbCacheScale, m_textDbCacheScale);
            }
        }

        #endregion

        #region SetTextDbCacheSizeScale(object)

        internal void SetTextDbCacheSizeScale (object value)
        {
            if (value == null) {
                m_textDbCacheSizeScale = DefaultValueOf.TextDbCacheSizeScale;
            } else if (value is byte) {
                m_textDbCacheSizeScale = (byte) value;
            } else {
                m_textDbCacheSizeScale = Convert.ToByte (
                    value, CultureInfo.InvariantCulture);
            }

            if (m_textDbCacheSizeScale < 8) {
                m_textDbCacheSizeScale = 8;
            } else if (m_textDbCacheSizeScale > 20) {
                m_textDbCacheSizeScale = 20;
            }

            if (m_builder != null) {
                m_builder.SetBaseValue (
                    Key.TextDbCacheSizeScale, m_textDbCacheSizeScale);
            }
        }

        #endregion

        #region SetTextDbEncoding(object)

        internal void SetTextDbEncoding (object value)
        {
            if (value == null) {
                m_textDbEncoding = DefaultValueOf.TextDbEncoding;
            } else if (value is string) {
                m_textDbEncoding = (string) value;
            } else {
                m_textDbEncoding = Convert.ToString (
                    value, CultureInfo.InvariantCulture);
            }

            if (m_builder != null) {
                m_builder.SetBaseValue (Key.TextDbEncoding, m_textDbEncoding);
            }
        }

        #endregion

        #region SetTextDbFieldSeparator(object)

        internal void SetTextDbFieldSeparator (object value)
        {
            if (value == null) {
                m_textDbFieldSeparator = DefaultValueOf.TextDbFieldSeparator;
            } else if (value is string) {
                m_textDbFieldSeparator = (string) value;
            } else {
                m_textDbFieldSeparator = Convert.ToString (
                    value, CultureInfo.InvariantCulture);
            }

            if (m_builder != null) {
                m_builder.SetBaseValue (
                    Key.TextDbFieldSeparator, m_textDbFieldSeparator);
            }
        }

        #endregion

        #region SetTextDbIgnoreFirst(object)

        internal void SetTextDbIgnoreFirst (object value)
        {
            if (value == null) {
                m_textDbIgnoreFirst = DefaultValueOf.TextDbIgnoreFirst;
            } else if (value is bool) {
                m_textDbIgnoreFirst = (bool) value;
            } else {
                m_textDbIgnoreFirst = Util.ConvertToBool (value);
            }

            if (m_builder != null) {
                m_builder.SetBaseValue (
                    Key.TextDbIgnoreFirst, m_textDbIgnoreFirst);
            }
        }

        #endregion

        #region SetTextDbLongVarcharFieldSeparator(object)

        internal void SetTextDbLongVarcharFieldSeparator (object value)
        {
            if (value == null) {
                m_textDbLongVarcharFieldSeparator
                    = DefaultValueOf.TextDbLongVarcharFieldSeparator;
            } else if (value is string) {
                m_textDbLongVarcharFieldSeparator = (string) value;
            } else {
                m_textDbLongVarcharFieldSeparator
                    = Convert.ToString (value, CultureInfo.InvariantCulture);
            }

            if (m_builder != null) {
                m_builder.SetBaseValue (
                    Key.TextDbLongVarcharFieldSeparator,
                    m_textDbLongVarcharFieldSeparator);
            }
        }

        #endregion

        #region SetTextDbQuoted(object)

        internal void SetTextDbQuoted (object value)
        {
            if (value == null) {
                m_textDbQuoted = DefaultValueOf.TextDbQuoted;
            } else if (value is bool) {
                m_textDbQuoted = (bool) value;
            } else {
                m_textDbQuoted = Util.ConvertToBool (value);
            }

            if (m_builder != null) {
                m_builder.SetBaseValue (Key.TextDbQuoted, m_textDbQuoted);
            }
        }

        #endregion

        #region SetTextDbVarcharFieldSeparator(object)

        internal void SetTextDbVarcharFieldSeparator (object value)
        {
            if (value == null) {
                m_textDbVarcharFieldSeparator
                    = DefaultValueOf.TextDbVarcharFieldSeparator;
            } else if (value is string) {
                m_textDbVarcharFieldSeparator = (string) value;
            } else {
                m_textDbVarcharFieldSeparator
                    = Convert.ToString (value, CultureInfo.InvariantCulture);
            }

            if (m_builder != null) {
                m_builder.SetBaseValue (
                    Key.TextDbVarcharFieldSeparator, m_textDbVarcharFieldSeparator);
            }
        }

        #endregion

        #region SetTransactionLogMaxSize(object)

        internal void SetTransactionLogMaxSize (object value)
        {
            if (value == null) {
                m_transactionLogMaxSize = DefaultValueOf.TransactionLogMaxSize;
            } else if (value is uint) {
                m_transactionLogMaxSize = (uint) value;
            } else {
                m_transactionLogMaxSize = Util.ConvertToUInt (value);
            }

            if (m_transactionLogMaxSize > int.MaxValue) {
                m_transactionLogMaxSize = int.MaxValue;
            }

            if (m_builder != null) {
                m_builder.SetBaseValue (
                    Key.TransactionLogMaxSize, m_transactionLogMaxSize);
            }
        }

        #endregion

        #region SetTransactionLogMaxSyncDelay(object)

        internal void SetTransactionLogMaxSyncDelay (object value)
        {
            if (value == null) {
                m_transactionLogMaxSyncDelay
                    = DefaultValueOf.TransactionLogMaxSyncDelay;
            } else if (value is ushort) {
                m_transactionLogMaxSyncDelay = (ushort) value;
            } else {
                m_transactionLogMaxSyncDelay = Util.ConvertToUShort (value);
            }

            if (m_builder != null) {
                m_builder.SetBaseValue (
                    Key.TransactionLogMaxSyncDelay,
                    m_transactionLogMaxSyncDelay);
            }
        }

        #endregion

        #region SetTransactionNoMultiRewrite(object)

        internal void SetTransactionNoMultiRewrite (object value)
        {
            if (value == null) {
                m_transactionNoMultRewrite
                    = DefaultValueOf.TransactionNoMultiRewrite;
            } else if (value is bool) {
                m_transactionNoMultRewrite = (bool) value;
            } else {
                m_transactionNoMultRewrite = Util.ConvertToBool (value);
            }

            if (m_builder != null) {
                m_builder.SetBaseValue (
                    Key.TransactionNoMultiRewrite, m_transactionNoMultRewrite);
            }
        }

        #endregion

        #region SetUserId(object)

        internal void SetUserId (object value)
        {
            if (value == null) {
                m_userId = DefaultValueOf.UserId;
            } else if (value is string) {
                m_userId = (string) value;
            } else {
                m_userId = Convert.ToString (
                    value, CultureInfo.InvariantCulture);
            }

            if (m_builder != null) {
                m_builder.SetBaseValue (Key.UserId, m_userId);
            }
        }

        #endregion

        #endregion

        #region Property Getter Methods

        #region GetJdbcURL()

        internal string GetJdbcUrl ()
        {
            StringBuilder sb = new StringBuilder ();

            sb.Append ("jdbc:hsqldb:");
            sb.Append (Util.ToJdbcProtocol (m_protocol));

            bool isEmbedded;

            switch (m_protocol) {
                case ConnectionProtocol.File:
                    {
                        sb.Append(ToFilePath(m_path));

                        isEmbedded = true;
                        break;
                    }
                case ConnectionProtocol.Mem:
                    {
                        sb.Append(ToMemPath(m_path));

                        isEmbedded = true;
                        break;
                    }
                case ConnectionProtocol.Res:
                {
                    sb.Append(ToResPath(m_path));

                    isEmbedded = true;
                    break;
                }
                default :
                {
                    sb.Append (m_host);

                    if (m_port > 0) {
                        sb.Append (':').Append (m_port);
                    }

                    if (!string.IsNullOrEmpty (m_path)) {
                        sb.Append ('/').Append (m_path);
                    }

                    isEmbedded = false;
                    break;
                }
            }

            if (m_defaultSchemaQualification
                != DefaultValueOf.DefaultSchemaQualification) {
                sb.Append (';');
                sb.Append ("default_schema");
                sb.Append ('=');
                sb.Append (m_defaultSchemaQualification.ToString ().ToLower ());
            }

            if (isEmbedded) {
                if (m_ifExists != DefaultValueOf.IfExists) {
                    sb.Append (';');                    
                    sb.Append ("ifexists");
                    sb.Append ('=');
                    sb.Append (m_ifExists.ToString ().ToLower ());
                }

                if (m_reportBaseColumnName
                    != DefaultValueOf.ReportBaseColumnName) {
                    sb.Append (';');
                    sb.Append (__get_column_name);
                    sb.Append ('=');
                    sb.Append (m_reportBaseColumnName.ToString ().ToLower ());
                }

                if (m_autoShutdown != DefaultValueOf.AutoShutdown) {
                    sb.Append (';');
                    sb.Append (__auto_shutdown);
                    sb.Append ('=');
                    sb.Append (m_autoShutdown.ToString ().ToLower ());
                }

                //if (m_reportCatalogs != DefaultValueOf.ReportCatalogs)
                //{
                //    sb.Append(';');
                //    sb.Append(__hsqldb_catalogs);
                //    sb.Append('=');
                //    sb.Append(m_reportCatalogs.ToString().ToLower());
                //}

                //if (m_enforceColumnSize != DefaultValueOf.EnforceColumnSize)
                //{
                //    sb.Append(';');
                //    sb.Append(__sql_enforce_strict_size);
                //    sb.Append('=');
                //    sb.Append(m_enforceColumnSize.ToString().ToLower());
                //}
            }

            //sb.Append(';');

            return sb.ToString ();
        }

        #endregion

        internal ushort GetPort()
        {
            switch (m_protocol)
            {
                case System.Data.Hsqldb.Common.Enumeration.ConnectionProtocol.File:
                case System.Data.Hsqldb.Common.Enumeration.ConnectionProtocol.Mem:
                case System.Data.Hsqldb.Common.Enumeration.ConnectionProtocol.Res:
                    {
                        return DefaultValueOf.Port(m_protocol);
                    }
                default:
                    {
                        return (m_port == 0)
                            ? DefaultValueOf.Port(m_protocol)
                            : m_port;
                    }
            }
        }

        #region GetStartupCommands()

        internal string[] GetStartupCommands ()
        {
            StringCollection commands = new StringCollection ();
            object[][] parms;

            if (m_initialSchema != DefaultValueOf.InitialSchema) {
                commands.Add ("SET SCHEMA " + m_initialSchema);
            }

            if (m_protocol == ConnectionProtocol.File) {
                parms = new object[][] {
                    // TODO:  Currently, this needs to be written using direct file access to
                    // the database properties file (its marked as a protected property
                    // in HDP).  Regardless, it needs to be set *before* opening the database
                    // or the database needs to be restarted for it to take effect.
                    //new object[] {
                    //    HDP.hsqldb_applog,
                    //    _ToSimpleLogLevel(m_databaseAppLogLevel)
                    //},
                    new object[] {
                        HDP.hsqldb_cache_file_scale, m_cacheFileScale },
                    new object[] { HDP.hsqldb_cache_scale, m_cacheScale },
                    new object[] {
                        HDP.hsqldb_cache_size_scale, m_cacheSizeScale },
                    new object[] {
                        HDP.hsqldb_default_table_type,
                        '\'' + Util.ToSqlTableType (m_defaultTableType) + '\''
                    },
                    // TODO:  Currently, this needs to be written using direct file access to
                    // the database properties file (its marked as a protected property
                    // in HDP).  Regardless, it needs to be set *before* opening the database
                    // or the database needs to be restarted for it to take effect.
                    //new object[] {
                    //    HDP.hsqldb_nio_data_file,
                    //    '\'' +m_memoryMappedDataFile.ToString().ToUpper() + '\''
                    //},
                    new object[] {
                        HDP.sql_tx_no_multi_write,
                        m_transactionNoMultRewrite.ToString ().ToUpper ()
                    },
                    new object[] {
                        HDP.textdb_all_quoted,
                        m_textDbAllQuoted.ToString ().ToUpper ()
                    },
                    new object[] {
                        HDP.textdb_allow_full_path,
                        m_textDBAllowFullPath.ToString ().ToUpper ()
                    },
                    new object[] { HDP.textdb_cache_scale, m_textDbCacheScale },
                    new object[] {
                        HDP.textdb_cache_size_scale, m_textDbCacheSizeScale },
                    new object[] {
                        HDP.textdb_encoding, '\'' + m_textDbEncoding + '\'' },
                    new object[] {
                        HDP.textdb_fs, '\'' + m_textDbFieldSeparator + '\'' },
                    new object[] {
                        HDP.textdb_ignore_first,
                        m_textDbIgnoreFirst.ToString ().ToUpper ()
                    },
                    new object[] {
                        HDP.textdb_lvs,
                        '\'' + m_textDbLongVarcharFieldSeparator + '\''
                    },
                    new object[] {
                        HDP.textdb_quoted,
                        m_textDbQuoted.ToString().ToUpper () },
                    new object[] {
                        HDP.textdb_vs, '\'' + m_textDbVarcharFieldSeparator + '\'' },
                    new object[] {
                        __hsqldb_catalogs,
                        m_reportCatalogs.ToString ().ToUpper ()
                    }
                };

                for (int i = 0; i < parms.Length; i++) {
                    commands.Add (
                        String.Format ("SET PROPERTY \"{0}\" {1}", parms[i]));
                }

                commands.Add ("SET LOGSIZE " + m_transactionLogMaxSize);
                commands.Add ("SET CHECKPOINT DEFRAG " + m_defragLimit);
                commands.Add (
                    "SET SCRIPTFORMAT "
                        + Util.ToScriptWriterFormatString (
                            m_databaseScriptFormat));

                if (m_transactionLogMaxSyncDelay > 0) {
                    commands.Add (
                        "SET "
                            + __write_delay
                            + " "
                            + m_transactionLogMaxSyncDelay
                            + " MILLIS");
                } else {
                    commands.Add ("SET " + __write_delay + " FALSE");
                }
            } else if (m_protocol == ConnectionProtocol.Mem
                || m_protocol == ConnectionProtocol.Res) {
                parms = new object[][] {
                    new object[] {
                        HDP.sql_tx_no_multi_write,
                        m_transactionNoMultRewrite.ToString ().ToUpper ()
                    },
                    new object[] {
                        __hsqldb_catalogs,
                        m_reportCatalogs.ToString ().ToUpper ()
                    }
                };

                for (int i = 0; i < parms.Length; i++) {
                    commands.Add (
                        String.Format ("SET PROPERTY \"{0}\" {1}", parms[i]));
                }
            }

            string[] startupCommands = new string[commands.Count];

            commands.CopyTo (startupCommands, 0);

            return startupCommands;
        }

        #endregion

        #region GetDataSource()

        /// <summary>
        /// Gets the data source.
        /// </summary>
        /// <returns></returns>
        internal string GetDataSource ()
        {
            StringBuilder sb = new StringBuilder(
                m_protocol.ToString ().ToLowerInvariant());

            switch (m_protocol) {
                case ConnectionProtocol.File:
                {
                    return sb.Append (':')
                             .Append (ToFilePath (m_path))
                             .ToString ();
                }
                case ConnectionProtocol.Mem:
                {
                    return sb.Append (':')
                             .Append (ToMemPath (m_path))
                             .ToString ();
                }
                case ConnectionProtocol.Res:
                {
                    return sb.Append (':')
                             .Append (ToResPath (m_path))
                             .ToString ();
                }
                case ConnectionProtocol.Hsql:
                case ConnectionProtocol.Hsqls:
                case ConnectionProtocol.Http:
                case ConnectionProtocol.Https:
                {
                    sb.Append ("://").Append (m_host);

                    ushort port = GetPort();

                    if (port != DefaultValueOf.Port(m_protocol))
                    {
                        sb.Append (':').Append (port);
                    }

                    sb.Append ('/');

                    if (!string.IsNullOrEmpty (m_path)) {

                        sb.Append(m_path.TrimStart('/'));
                    }

                    return sb.ToString ();
                }
            default:
                {
                    throw new HsqlDataSourceException(string.Format(
                        "Unhandled Protocol Enumeration Value: {0}", 
                        m_protocol));
                }
                    
            }
        }

        internal void SetDataSource(object value)
        {
            string dataSource = (value == null)
                ? "" 
                : Convert.ToString(value);

            org.hsqldb.persist.HsqlProperties properties
                = org.hsqldb.DatabaseURL.parseURL(dataSource, false);

            if (properties == null)
            {
                throw new ArgumentException(string.Format(
                    "Malformed data source specification: [{0}]",dataSource),
                    "value");
            }

            string connectionType = properties.getProperty(
                "connection_type",
                m_protocol.ToString());

            switch (connectionType)
            {
                case org.hsqldb.DatabaseURL.S_FILE:
                    {
                        SetProtocol(ConnectionProtocol.File);
                        break;
                    }
                case org.hsqldb.DatabaseURL.S_RES:
                    {
                        SetProtocol(ConnectionProtocol.Res);
                        break;
                    }
                case org.hsqldb.DatabaseURL.S_MEM:
                    {
                        SetProtocol(ConnectionProtocol.Mem);
                        break;
                    }
                case org.hsqldb.DatabaseURL.S_HSQL:
                    {
                        SetProtocol(ConnectionProtocol.Hsql);
                        break;
                    }
                case org.hsqldb.DatabaseURL.S_HSQLS:
                    {
                        SetProtocol(ConnectionProtocol.Hsqls);
                        break;
                    }
                case org.hsqldb.DatabaseURL.S_HTTP:
                    {
                        SetProtocol(ConnectionProtocol.Http);
                        break;
                    }
                case org.hsqldb.DatabaseURL.S_HTTPS:
                    {
                        SetProtocol(ConnectionProtocol.Https);
                        break;
                    }
                default:
                    {
                        SetProtocol(connectionType);
                        break;
                    }
            }

            string databasePath = properties.getProperty("database", m_path);
            string hostPath = properties.getProperty("path", "");

            switch (m_protocol)
            {
                case ConnectionProtocol.File:
                    {
                        SetPath(ToFilePath(databasePath));
                        break;
                    }
                case ConnectionProtocol.Mem:
                    {
                        SetPath(ToMemPath(databasePath));
                        break;
                    }
                case ConnectionProtocol.Res:
                    {
                        SetPath(ToResPath(databasePath));
                        break;
                    }
                default:
                    {
                        string host = properties.getProperty("host", m_host);
                        string path = hostPath + databasePath;
                        int port = properties.getIntegerProperty("port", m_port);

                        SetHost(host);
                        SetPath(path);
                        SetPort(port);
                        break;
                    }
            }
        }

        #endregion

        #endregion

        #endregion Instance Methods

        #region Static Methods

        #region ToMemPath(string)

        /// <summary>
        /// Retrieves the cannonical mem: protocol path
        /// corresponding to the given mem: path.
        /// value.
        /// </summary>
        /// <param name="path">
        /// For which to retrieve the cannonical mem: path.
        /// </param>
        /// <returns>
        /// The corresponding cannonical mem: path.
        /// </returns>
        internal static string ToMemPath (string path)
        {
            return (path == null) 
                ? "" 
                : path.ToLowerInvariant ();
        }

        #endregion

        #region ToResPath(string)

        /// <summary>
        /// Retrieves the cannonical res: protocol path
        /// corresponding to the given res: path.
        /// value.
        /// </summary>
        /// <param name="path">
        /// For which to retrieve the cannonical res: path.
        /// </param>
        /// <returns>
        /// The corresponding cannonical res: path.
        /// </returns>
        internal static string ToResPath (string path)
        {
            if (path == null) 
            {
                path = "/";
            } 

            path = path.ToLowerInvariant (); 
            
            if (!path.StartsWith("/"))
            {
                path = "/" + path;
            }

            return path;
        }

        #endregion

        #region ToFilePath(string)

        /// <summary>
        /// Retrieves the cannonical or absolute file: protocol path
        /// corresponding to the given file: path.
        /// value.
        /// </summary>
        /// <param name="path">
        /// For which to retrieve the cannonical or absolute file: path.
        /// </param>
        /// <returns>
        /// The corresponding cannonical or absolute file: path.
        /// </returns>
        internal static string ToFilePath (string path)
        {
            if (string.IsNullOrEmpty(path))
            {
                path = "";
            }

            return new java.io.File(org.hsqldb.lib.FileUtil.getDefaultInstance().canonicalOrAbsolutePath(path)).toURI().toURL().getFile();
            
        }

        #endregion

        #endregion

    }

     #endregion
}
