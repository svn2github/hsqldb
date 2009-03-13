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

namespace System.Data.Hsqldb.Common.Enumeration
{
    #region ConnectionStringKeyword

    /// <summary>
    /// <para>
    /// Specifies the allowable connection keywords.
    /// </para>
    /// <img src="../Documentation/ClassDiagrams/System.Data.Hsqldb.Common.Enumeration.ConnectionStringKeyword.png"
    ///      alt="ConnectionStringKeyword Class Diagram"/>
    /// </summary>
    /// <author name="boucherb@users"/>
    public enum ConnectionStringKeyword
    {
        #region AutoShutdown
        /// <summary>
        /// Specifies the <c>AutoShutdown</c> connection property.
        /// </summary>
        AutoShutdown,
        #endregion
        #region DatabaseAppLogLevel
        /// <summary>
        /// Specifies the <c>Database App Log Level</c> connection property.
        /// </summary>
        DatabaseAppLogLevel,
        #endregion
        #region DatabaseScriptFormat
        /// <summary>
        /// Specifies the <c>Database Script Format</c> connection property.
        /// </summary>
        DatabaseScriptFormat,
        #endregion
        #region DataSource
        /// <summary>
        /// Specifies the <c>DataSource</c> derived connection property.
        /// </summary>
        DataSource,
        #endregion
        #region CacheFileScale
        /// <summary>
        /// Specifies the <c>Cache File Scale</c> connection property.
        /// </summary>
        CacheFileScale,
        #endregion
        #region CacheScale
        /// <summary>
        /// Specifies the <c>Cache Scale</c> connection property.
        /// </summary>
        CacheScale,
        #endregion
        #region CacheSizeScale
        /// <summary>
        /// Specifies the <c>Cache Size Scale</c> connection property.
        /// </summary>
        CacheSizeScale,
        #endregion
        #region DefaultSchemaQualification
        /// <summary>
        /// Specifies the <c>Default Schema Qualification</c> connection property.
        /// </summary>
        DefaultSchemaQualification,
        #endregion
        #region DefaultTableType
        /// <summary>
        /// Specifies the <c>Default Table Type</c> connection property.
        /// </summary>
        DefaultTableType,
        #endregion
        #region DefragLimit
        /// <summary>
        /// Specifies the <c>Defrag Limit</c> connection property.
        /// </summary>
        DefragLimit,
        #endregion
        #region EnforceColumnSize
        /// <summary>
        /// Specifies the <c>EnforceColumnSize</c> connection property.
        /// </summary>
        EnforceColumnSize,
        #endregion
#if SYSTRAN        
        #region Enlist
        /// <summary>
        /// Specifies the <c>Enlist</c> connection property.
        /// </summary>
        Enlist,
        #endregion
#endif
        #region Host
        /// <summary>
        /// Specifies the <c>Host</c> connection property.
        /// </summary>
        Host,
        #endregion
        #region IfExists
        /// <summary>
        /// Specifies the <c>If Exists</c> connection property.
        /// </summary>
        IfExists,
        #endregion
        #region InitialSchema
        /// <summary>
        /// Specifies the <c>Initial Schema</c> connection property.
        /// </summary>
        InitialSchema,
        #endregion
        #region JdbcURL
        /// <summary>
        /// Specifies the <c>JdbcURL</c> derived connection property.
        /// </summary>
        JdbcURL,
        #endregion
        #region MemoryMappedDataFile
        /// <summary>
        /// Specifies the <c>Memory Mapped Data File</c> connection property.
        /// </summary>
        MemoryMappedDataFile,
        #endregion
        #region Password
        /// <summary>
        /// Specifies the <c>Password</c> connection property.
        /// </summary>
        Password,
        #endregion
        #region Path
        /// <summary>
        /// Specifies the <c>Path</c> connection property.
        /// </summary>
        Path,
        #endregion
        #region Port
        /// <summary>
        /// Specifies the <c>Port</c> connection property.
        /// </summary>
        Port,
        #endregion
        #region Protocol
        /// <summary>
        /// Specifies the <c>Protocol</c> connection property.
        /// </summary>
        Protocol,
        #endregion
        #region ReadOnlySession
        /// <summary>
        /// Specifies the <c>ReadOnlySession</c> connection property.
        /// </summary>
        ReadOnlySession,
        #endregion
        #region ReportBaseColumnName
        /// <summary>
        /// Specifies the <c>Use Column Name</c> connection property.
        /// </summary>
        ReportBaseColumnName,
        #endregion
        #region ReportCatalogs
        /// <summary>
        /// Specifies the <c>Report Catalogs</c> connection property.
        /// </summary>
        ReportCatalogs,
        #endregion
        #region StartupCommands
        /// <summary>
        /// Specifies the <c>StartupCommands</c> connection property.
        /// </summary>
        StartupCommands,
        #endregion
        #region TextDbAllowFullPath
        /// <summary>
        /// Specifies the <c>Text DB Allow Full Path</c> connection property.
        /// </summary>
        TextDbAllowFullPath,
        #endregion
        #region TextDbAllQuoted
        /// <summary>
        /// Specifies the <c>Text DB All Quoted</c> connection property.
        /// </summary>
        TextDbAllQuoted,
        #endregion
        #region TextDbCacheScale
        /// <summary>
        /// Specifies the <c>Text DB Cache Size Scale</c> connection property.
        /// </summary>
        TextDbCacheScale,
        #endregion
        #region TextDbCacheSizeScale
        /// <summary>
        /// Specifies the <c>Text DB Cache Size Scale</c> connection property.
        /// </summary>
        TextDbCacheSizeScale,
        #endregion
        #region TextDbEncoding
        /// <summary>
        /// Specifies the <c>Text DB Encoding</c> connection property.
        /// </summary>
        TextDbEncoding,
        #endregion
        #region TextDbFieldSeparator
        /// <summary>
        /// Specifies the <c>Text DB Field Separator</c> connection property.
        /// </summary>
        TextDbFieldSeparator,
        #endregion
        #region TextDbIgnoreFirst
        /// <summary>
        /// Specifies the <c>Text DB Ignore First</c> connection property.
        /// </summary>
        TextDbIgnoreFirst,
        #endregion
        #region TextDbLongVarcharFieldSeparator
        /// <summary>
        /// Specifies the <c>Text DB Long Varchar Separator</c> connection property.
        /// </summary>
        TextDbLongVarcharFieldSeparator,
        #endregion
        #region TextDbQuoted
        /// <summary>
        /// Specifies the <c>Text DB Quoted</c> connection property.
        /// </summary>
        TextDbQuoted,
        #endregion
        #region TextDbVarcharFieldSeparator
        /// <summary>
        /// Specifies the <c>Text DB Varchar Separator</c> connection property.
        /// </summary>
        TextDbVarcharFieldSeparator,
        #endregion
        #region TransactionLogMaxSize
        /// <summary>
        /// Specifies the <c>Transaction Log Max Size</c> connection property.
        /// </summary>
        TransactionLogMaxSize,
        #endregion
        #region TransactionLogMaxSyncDelay
        /// <summary>
        /// Specifies the <c>Transaction Log Max Sync Delay</c> connection property.
        /// </summary>
        TransactionLogMaxSyncDelay,
        #endregion
        #region TransactionNoMultiRewrite
        /// <summary>
        /// Specifies the <c>Transaction No Multi Rewrite</c> connection property.
        /// </summary>
        TransactionNoMultiRewrite,
        #endregion
        #region UserId
        /// <summary>
        /// Specifies the <c>UserId</c> connection property.
        /// </summary>
        UserId
        #endregion
    }

    #endregion
}