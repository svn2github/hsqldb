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

namespace System.Data.Hsqldb.Client
{
    #region HsqlConnectionStringBuilder

    public sealed partial class HsqlConnectionStringBuilder
    {
        #region ConnectionStringKey

        /// <summary>
        /// The Connection String Keys recognized by
        /// <see cref="HsqlConnectionStringBuilder"/>.
        /// </summary>
        /// <remarks>
        /// These are the keys that can be used to build a connection string
        /// with which to connection to an HSQLDB database instance.
        /// </remarks>
        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Design", "CA1034:NestedTypesShouldNotBeVisible")]
        public static class ConnectionStringKey
        {
            #region Constants

            #region AutoShutdown
            /// <summary>
            /// Key for the property
            /// <see cref="HsqlConnectionStringBuilder.AutoShutdown"/>.
            /// </summary>
            /// <value>
            /// <c>"Auto Shutdown"</c>.
            /// </value>
            public const string AutoShutdown = "Auto Shutdown";
            #endregion
            #region DatabaseAppLogLevel
            /// <summary>
            /// Key for the property
            /// <see cref="HsqlConnectionStringBuilder.DatabaseAppLogLevel"/>.
            /// </summary>
            /// <value>
            /// <c>"Database App Log Level"</c>.
            /// </value>
            public const string DatabaseAppLogLevel = "Database App Log Level";
            #endregion
            #region DatabaseScriptFormat
            /// <summary>
            /// Key for the property
            /// <see cref="HsqlConnectionStringBuilder.DatabaseScriptFormat"/>.
            /// </summary>
            /// <value>
            /// <c>"Database Script Format"</c>.
            /// </value>
            public const string DatabaseScriptFormat = "Database Script Format";
            #endregion
            #region CacheFileScale
            /// <summary>
            /// Key for the property
            /// <see cref="HsqlConnectionStringBuilder.CacheFileScale"/>.
            /// </summary>
            /// <value>
            /// <c>"Cache File Scale"</c>.
            /// </value>
            public const string CacheFileScale = "Cache File Scale";
            #endregion
            #region CacheScale
            /// <summary>
            /// Key for the property
            /// <see cref="HsqlConnectionStringBuilder.CacheScale"/>.
            /// </summary>
            /// <value>
            /// <c>"Cache Scale"</c>.
            /// </value>
            public const string CacheScale = "Cache Scale";
            #endregion
            #region CacheSizeScale
            /// <summary>
            /// Key for the property
            /// <see cref="HsqlConnectionStringBuilder.CacheSizeScale"/>.
            /// </summary>
            /// <value>
            /// <c>"Cache Size Scale"</c>.
            /// </value>
            public const string CacheSizeScale = "Cache Size Scale";
            #endregion
            #region DefaultSchemaQualification
            /// <summary>
            /// Key for the property
            /// <see cref="HsqlConnectionStringBuilder.DefaultSchemaQualification"/>.
            /// </summary>
            /// <value>
            /// <c>"Default Schema Qualification"</c>.
            /// </value>
            //[System.Obsolete()]
            public const string DefaultSchemaQualification = "Default Schema Qualification";
            #endregion
            #region DefaultTableType
            /// <summary>
            /// Key for the property
            /// <see cref="HsqlConnectionStringBuilder.DefaultTableType"/>.            
            /// </summary>
            /// <value>
            /// <c>"Default Table Type"</c>.
            /// </value>
            public const string DefaultTableType = "Default Table Type";
            #endregion
            #region DefragLimit
            /// <summary>
            /// Key for the property
            /// <see cref="HsqlConnectionStringBuilder.DefragLimit"/>.
            /// </summary>
            /// <value>
            /// <c>"Defrag Limit"</c>.
            /// </value>
            public const string DefragLimit = "Defrag Limit";
            #endregion
            #region EnforceColumnSize
            /// <summary>
            /// Key for the property
            /// <see cref="HsqlConnectionStringBuilder.EnforceColumnSize"/>.
            /// </summary>
            /// <value>
            /// <c>"Enforce Column Size"</c>.
            /// </value>
            public const string EnforceColumnSize = "Enforce Column Size";
            #endregion
#if SYSTRAN
            #region Enlist
            /// <summary>
            /// Key for the property
            /// <see cref="HsqlConnectionStringBuilder.Enlist"/>.
            /// </summary>
            /// <value>
            /// <c>"Enlist"</c>.
            /// </value>
            public const string Enlist = "Enlist";
            #endregion
#endif
            #region Host
            /// <summary>
            /// Key for the property
            /// <see cref="HsqlConnectionStringBuilder.Host"/>.
            /// </summary>
            /// <value>
            /// <c>"Host"</c>.
            /// </value>
            public const string Host = "Host";
            #endregion
            #region IfExists
            /// <summary>
            /// Key for the property
            /// <see cref="HsqlConnectionStringBuilder.IfExists"/>.
            /// </summary>
            /// <value>
            /// <c>"If Exists"</c>.
            /// </value>
            public const string IfExists = "If Exists";
            #endregion
            #region InitialSchema
            /// <summary>
            /// Key for the property
            /// <see cref="HsqlConnectionStringBuilder.InitialSchema"/>.
            /// </summary>
            /// <value>
            /// <c>"Initial Schema"</c>.
            /// </value>
            public const string InitialSchema = "Initial Schema";
            #endregion
            #region MemoryMappedDataFile
            /// <summary>
            /// Key for the property
            /// <see cref="HsqlConnectionStringBuilder.MemoryMappedDataFile"/>.
            /// </summary>
            /// <value>
            /// <c>"Memory Mapped Data File"</c>.
            /// </value>
            public const string MemoryMappedDataFile = "Memory Mapped Data File";
            #endregion
            #region Password
            /// <summary>
            /// Key for the property
            /// <see cref="HsqlConnectionStringBuilder.Password"/>.
            /// </summary>
            /// <value>
            /// <c>"Password"</c>.
            /// </value>
            public const string Password = "Password";
            #endregion
            #region Path
            /// <summary>
            /// Key for the property
            /// <see cref="HsqlConnectionStringBuilder.Path"/>.
            /// </summary>
            /// <value>
            /// <c>"Path"</c>.
            /// </value>
            public const string Path = "Path";
            #endregion
            #region Port
            /// <summary>
            /// Key for the property
            /// <see cref="HsqlConnectionStringBuilder.Port"/>.
            /// </summary>
            /// <value>
            /// <c>"Port"</c>.
            /// </value>
            public const string Port = "Port";
            #endregion
            #region Protocol
            /// <summary>
            /// Key for the property
            /// <see cref="HsqlConnectionStringBuilder.Protocol"/>.
            /// </summary>
            /// <value>
            /// <c>"Protocol"</c>.
            /// </value>
            public const string Protocol = "Protocol";
            #endregion
            #region ReadOnlySession
            /// <summary>
            /// Key for the property
            /// <see cref="HsqlConnectionStringBuilder.ReadOnlySession"/>.
            /// </summary>
            /// <value>
            /// <c>"Protocol"</c>.
            /// </value>
            public const string ReadOnlySession = "Read Only Session";
            #endregion
            #region ReportBaseColumnName
            /// <summary>
            /// Key for the property
            /// <see cref="HsqlConnectionStringBuilder.ReportBaseColumnName"/>.
            /// </summary>
            /// <value>
            /// <c>"Report Base Column Name"</c>.
            /// </value>
            public const string ReportBaseColumnName = "Report Base Column Name";
            #endregion
            #region ReportCatalogs
            /// <summary>
            /// Key for the property
            /// <see cref="HsqlConnectionStringBuilder.ReportCatalogs"/>.
            /// </summary>
            /// <value>
            /// <c>"Report Catalogs"</c>.
            /// </value>
            public const string ReportCatalogs = "Report Catalogs";
            #endregion
            #region TextDbAllowFullPath
            /// <summary>
            /// Key for the property
            /// <see cref="HsqlConnectionStringBuilder.TextDbAllowFullPath"/>.
            /// </summary>
            /// <value>
            /// <c>"Text DB Allow Full Path"</c>.
            /// </value>
            [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1706:ShortAcronymsShouldBeUppercase", MessageId = "Member")]
            public const string TextDbAllowFullPath = "Text DB Allow Full Path";
            #endregion
            #region TextDbAllQuoted
            /// <summary>
            /// Key for the property
            /// <see cref="HsqlConnectionStringBuilder.TextDbAllQuoted"/>.
            /// </summary>
            /// <value>
            /// <c>"Text DB All Quoted"</c>.
            /// </value>
            [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1706:ShortAcronymsShouldBeUppercase", MessageId = "Member")]
            public const string TextDbAllQuoted = "Text DB All Quoted";
            #endregion
            #region TextDbCacheScale
            /// <summary>
            /// Key for the property
            /// <see cref="HsqlConnectionStringBuilder.TextDbCacheScale"/>.
            /// </summary>
            /// <value>
            /// <c>"Text DB Cache Scale"</c>.
            /// </value>
            [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1706:ShortAcronymsShouldBeUppercase", MessageId = "Member")]
            public const string TextDbCacheScale = "Text DB Cache Scale";
            #endregion
            #region TextDbCacheSizeScale
            /// <summary>
            /// Key for the property
            /// <see cref="HsqlConnectionStringBuilder.TextDbCacheSizeScale"/>.
            /// </summary>
            /// <value>
            /// <c>"Text DB Cache Size Scale"</c>
            /// </value>
            [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1706:ShortAcronymsShouldBeUppercase", MessageId = "Member")]
            public const string TextDbCacheSizeScale = "Text DB Cache Size Scale";
            #endregion
            #region TextDbEncoding
            /// <summary>
            /// Key for the property
            /// <see cref="HsqlConnectionStringBuilder.TextDbEncoding"/>.
            /// </summary>
            /// <value>
            /// <c>"Text DB Encoding"</c>.
            /// </value>
            [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1706:ShortAcronymsShouldBeUppercase", MessageId = "Member")]
            public const string TextDbEncoding = "Text DB Encoding";
            #endregion
            #region TextDbFieldSeparator
            /// <summary>
            /// Key for the property
            /// <see cref="HsqlConnectionStringBuilder.TextDbFieldSeparator"/>.
            /// </summary>
            /// <value>
            /// <c>"Text DB Field Separator"</c>.
            /// </value>
            [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1706:ShortAcronymsShouldBeUppercase", MessageId = "Member")]
            public const string TextDbFieldSeparator = "Text DB Field Separator";
            #endregion
            #region TextDbIgnoreFirst
            /// <summary>
            /// Key for the property
            /// <see cref="HsqlConnectionStringBuilder.TextDbIgnoreFirst"/>.
            /// </summary>
            /// <value>
            /// <c>"Text DB Ignore First"</c>.
            /// </value>
            [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1706:ShortAcronymsShouldBeUppercase", MessageId = "Member")]
            public const string TextDbIgnoreFirst = "Text DB Ignore First";
            #endregion
            #region TextDbLongVarcharFieldSeparator
            /// <summary>
            /// Key for the property
            /// <see cref="HsqlConnectionStringBuilder.TextDbLongVarcharFieldSeparator"/>.
            /// </summary>
            /// <value>
            /// <c>"Text DB Long Varchar Field Separator"</c>.
            /// </value>
            [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1706:ShortAcronymsShouldBeUppercase", MessageId = "Member")]
            public const string TextDbLongVarcharFieldSeparator = "Text DB Long Varchar Field Separator";
            #endregion
            #region TextDbQuoted
            /// <summary>
            /// Key for the property
            /// <see cref="HsqlConnectionStringBuilder.TextDbQuoted"/>.
            /// </summary>
            /// <value>
            /// <c>"Text DB Quoted"</c>.
            /// </value>
            [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1706:ShortAcronymsShouldBeUppercase", MessageId = "Member")]
            public const string TextDbQuoted = "Text DB Quoted";
            #endregion
            #region TextDbVarcharFieldSeparator
            /// <summary>
            /// Key for the property
            /// <see cref="HsqlConnectionStringBuilder.TextDbVarcharFieldSeparator"/>.
            /// </summary>
            /// <value>
            /// <c>"Text DB Varchar Separator"</c>.
            /// </value>
            [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1706:ShortAcronymsShouldBeUppercase", MessageId = "Member")]
            public const string TextDbVarcharFieldSeparator = "Text DB Varchar Field Separator";
            #endregion
            #region TransactionLogMaxSize
            /// <summary>
            /// Key for the property
            /// <see cref="HsqlConnectionStringBuilder.TransactionLogMaxSize"/>.
            /// </summary>
            /// <value>
            /// <c>"Transaction Log Max Size"</c>.
            /// </value>
            public const string TransactionLogMaxSize = "Transaction Log Max Size";
            #endregion
            #region TransactionLogMaxSynchDelay
            /// <summary>
            /// Key for the property
            /// <see cref="HsqlConnectionStringBuilder.TransactionLogMaxSyncDelay"/>.
            /// </summary>
            /// <value>
            /// <c>"Transaction Log Max Sync Delay"</c>.
            /// </value>
            public const string TransactionLogMaxSyncDelay = "Transaction Log Max Sync Delay";
            #endregion
            #region TransactionNoMultiRewrite
            /// <summary>
            /// Key for the property
            /// <see cref="HsqlConnectionStringBuilder.TransactionNoMultiRewrite"/>.
            /// </summary>
            /// <value>
            /// <c>"Transaction No Multi Rewrite"</c>.
            /// </value>
            public const string TransactionNoMultiRewrite = "Transaction No Multi Rewrite";
            #endregion
            #region UserId
            /// <summary>
            /// Key for the property
            /// <see cref="HsqlConnectionStringBuilder.UserId"/>.
            /// </summary>
            /// <value>
            /// <c>"User ID"</c>.
            /// </value>
            public const string UserId = "User ID";
            #endregion

            #endregion
        }

        #endregion
    }

    #endregion
}
