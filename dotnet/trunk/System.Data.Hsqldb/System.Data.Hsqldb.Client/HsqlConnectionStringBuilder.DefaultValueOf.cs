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
using System.Data.Hsqldb.Common.Enumeration;
using ServerConstants = org.hsqldb.ServerConstants.__Fields;
using System;
#endregion

namespace System.Data.Hsqldb.Client
{
    public sealed partial class HsqlConnectionStringBuilder
    {
        #region DefaultValueOf

        /// <summary>
        /// Provides the default values of the 
        /// <see cref="HsqlConnectionStringBuilder"/> properties.
        /// </summary>
        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Design", "CA1034:NestedTypesShouldNotBeVisible")]
        public static class DefaultValueOf
        {
            #region Public Constants

            #region AutoShutdown
            /// <summary>Default value for the property <see cref="HsqlConnectionStringBuilder.AutoShutdown"/>.</summary>
            /// <value>false</value>
            public const bool AutoShutdown = false;

            #endregion
            #region CacheFileScale
            /// <summary>Default value for the property <see cref="HsqlConnectionStringBuilder.CacheFileScale"/>.</summary>
            /// <value>1</value>
            public const byte CacheFileScale = 1;

            #endregion
            #region CacheScale
            /// <summary>Default value for the property <see cref="HsqlConnectionStringBuilder.CacheScale"/>.</summary>
            /// <value>14</value>
            public const byte CacheScale = 14;

            #endregion
            #region CacheSizeScale
            /// <summary>Default value for the property <see cref="HsqlConnectionStringBuilder.CachesizeScale"/>.</summary>
            /// <value>8</value>
            public const byte CacheSizeScale = 8;

            #endregion
            #region ConnectionProtocol
            /// <summary>Default value for the property <see cref="HsqlConnectionStringBuilder.Protocol"/>.</summary>
            /// <value><see cref="System.Data.Hsqldb.Common.Enumeration.ConnectionProtocol.File"/></value>
            public const ConnectionProtocol Protocol
                = ConnectionProtocol.File;

            #endregion
            #region DatabaseAppLogLevel
            /// <summary>Default value for the property <see cref="HsqlConnectionStringBuilder.DatabaseAppLogLevel"/>.</summary>
            /// <value><see cref="System.Data.Hsqldb.Common.Enumeration.DatabaseAppLogLevel.Normal"/></value>
            public const DatabaseAppLogLevel DatabaseAppLogLevel
                = System.Data.Hsqldb.Common.Enumeration.DatabaseAppLogLevel.Normal;

            #endregion
            #region DatabaseScriptFormat
            /// <summary>Default value for the property <see cref="HsqlConnectionStringBuilder.DatabaseScriptFormat"/>.</summary>
            /// <value><see cref="System.Data.Hsqldb.Common.Enumeration.DatabaseScriptFormat.Text"/></value>
            public const DatabaseScriptFormat DatabaseScriptFormat
                = System.Data.Hsqldb.Common.Enumeration.DatabaseScriptFormat.Text;

            #endregion
            #region DefaultSchemaQualification
            /// <summary>Default value for the property <see cref="HsqlConnectionStringBuilder.DefaultSchemaQualification"/>.</summary>
            /// <value>false</value>
            public const bool DefaultSchemaQualification = false;

            #endregion
            #region DefaultTableType
            /// <summary>Default value for the property <see cref="HsqlConnectionStringBuilder.DefaultTableType"/>.</summary>
            /// <value><see cref="System.Data.Hsqldb.Common.Enumeration.DefaultTableType.Memory"/></value>
            public const DefaultTableType DefaultTableType
                = System.Data.Hsqldb.Common.Enumeration.DefaultTableType.Memory;

            #endregion
            #region DefragLimit
            /// <summary>Default value for the property <see cref="HsqlConnectionStringBuilder.DefragLimit"/>.</summary>
            /// <value>200</value>
            [CLSCompliant(false)]            
            public const ushort DefragLimit = 200;

            #endregion
            #region EnforceColumnSize
            /// <summary>Default value for the property <see cref="HsqlConnectionStringBuilder.EnforceColumnSize"/>.</summary>
            /// <value>true</value>
            public const bool EnforceColumnSize = true;

            #endregion
#if SYSTRAN
            #region Enlist
            /// <summary>Default value for the property <see cref="HsqlConnectionStringBuilder.Enlist"/>.</summary>
            /// <value>false</value>
            public const bool Enlist = false;
            #endregion
#endif
            #region Host
            /// <summary>Default value for the property <see cref="HsqlConnectionStringBuilder.Host"/>.</summary>
            /// <value>"localhost"</value>
            public const string Host = "localhost";

            #endregion
            #region IfExists
            /// <summary>Default value for the property <see cref="HsqlConnectionStringBuilder.IfExists"/>.</summary>
            /// <value>false</value>
            public const bool IfExists = false;

            #endregion
            #region InitialSchema
            /// <summary>Default value for the property <see cref="HsqlConnectionStringBuilder.InitialSchema"/>.</summary>
            /// <value>"PUBLIC"</value>
            public const string InitialSchema = "PUBLIC";

            #endregion
            #region MemoryMappedDataFile
            /// <summary>Default value for the property <see cref="HsqlConnectionStringBuilder.MemoryMappedDataFile"/>.</summary>
            /// <value>true</value>
            public const bool MemoryMappedDataFile = true;

            #endregion
            #region Password
            /// <summary>Default value for the property <see cref="HsqlConnectionStringBuilder.Password"/>.</summary>
            /// <value>"" (the empty string)</value>
            public const string Password = "";

            #endregion
            #region Path
            /// <summary>Default value for the property <see cref="HsqlConnectionStringBuilder.Path"/>.</summary>
            /// <value>"" (the empty string)</value>
            public const string Path = "";

            #endregion
            #region ReadOnlySession
            /// <summary>Default value for the property <see cref="HsqlConnectionStringBuilder.ReadOnlySession"/>.</summary>
            /// <value>false</value>
            public const bool ReadOnlySession = false;
            #endregion
            #region ReportBaseColumnName
            /// <summary>Default value for the property <see cref="HsqlConnectionStringBuilder.ReportBaseColumnName"/>.</summary>
            /// <value>true</value>
            public const bool ReportBaseColumnName = true;

            #endregion
            #region ReportCatalogs
            /// <summary>Default value for the property <see cref="HsqlConnectionStringBuilder.ReportCatalogs"/>.</summary>
            /// <value>false</value>
            public const bool ReportCatalogs = false;

            #endregion
            #region TextDbAllowFullPath
            /// <summary>Default value for the property <see cref="HsqlConnectionStringBuilder.TextDbAllowFullPath"/>.</summary>
            /// <value>false</value>
            [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1706:ShortAcronymsShouldBeUppercase", MessageId = "Member")]
            public const bool TextDbAllowFullPath = false;

            #endregion
            #region TextDbAllQuoted
            /// <summary>Default value for the property <see cref="HsqlConnectionStringBuilder.TextDbAllQuoted"/>.</summary>
            /// <value>false</value>
            [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1706:ShortAcronymsShouldBeUppercase", MessageId = "Member")]
            public const bool TextDbAllQuoted = false;

            #endregion
            #region TextDbCacheScale
            /// <summary>Default value for the property <see cref="HsqlConnectionStringBuilder.TextDbCacheScale"/>.</summary>
            /// <value>10</value>
            [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1706:ShortAcronymsShouldBeUppercase", MessageId = "Member")]
            public const byte TextDbCacheScale = 10;

            #endregion
            #region TextDbCacheSizeScale
            /// <summary>Default value for the property <see cref="HsqlConnectionStringBuilder.TextDbCacheSizeScale"/>.</summary>
            /// <value>10</value>
            [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1706:ShortAcronymsShouldBeUppercase", MessageId = "Member")]
            public const byte TextDbCacheSizeScale = 10;

            #endregion
            #region TextDbEncoding
            /// <summary>Default value for the property <see cref="HsqlConnectionStringBuilder.TextDbEncoding"/>.</summary>
            /// <value>"US-ASCII"</value>
            [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1706:ShortAcronymsShouldBeUppercase", MessageId = "Member")]
            public const string TextDbEncoding = "US-ASCII";

            #endregion
            #region TextDbFieldSeparator
            /// <summary>Default value for the property <see cref="HsqlConnectionStringBuilder.TextDbFieldSeparator"/>.</summary>
            /// <value>","</value>
            [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1706:ShortAcronymsShouldBeUppercase", MessageId = "Member")]
            public const string TextDbFieldSeparator = ",";

            #endregion
            #region TextDbIgnoreFirst
            /// <summary>Default value for the property <see cref="HsqlConnectionStringBuilder.TextDbIgnoreFirst"/>.</summary>
            /// <value>false</value>
            [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1706:ShortAcronymsShouldBeUppercase", MessageId = "Member")]
            public const bool TextDbIgnoreFirst = false;

            #endregion
            #region TextDbLongVarcharFieldSeparator
            /// <summary>Default value for the property <see cref="HsqlConnectionStringBuilder.TextDbLongVarcharFieldSeparator"/>.</summary>
            /// <value>","</value>
            [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1706:ShortAcronymsShouldBeUppercase", MessageId = "Member")]
            public const string TextDbLongVarcharFieldSeparator = ",";

            #endregion
            #region TextDbQuoted
            /// <summary>Default value for the property <see cref="HsqlConnectionStringBuilder.TextDbQuoted"/>.</summary>
            /// <value>true</value>
            [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1706:ShortAcronymsShouldBeUppercase", MessageId = "Member")]
            public const bool TextDbQuoted = true;

            #endregion
            #region TextDbVarcharFieldSeparator
            /// <summary>Default value for the property <see cref="HsqlConnectionStringBuilder.TextDbVarcharFieldSeparator"/>.</summary>
            /// <value>","</value>
            [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1706:ShortAcronymsShouldBeUppercase", MessageId = "Member")]
            public const string TextDbVarcharFieldSeparator = ",";

            #endregion
            #region TransactionLogMaxSize
            /// <summary>Default value for the property <see cref="HsqlConnectionStringBuilder.TransactionLogMaxSize"/>.</summary>
            /// <value>200</value>
            [CLSCompliant(false)]
            public const uint TransactionLogMaxSize = 200;

            #endregion
            #region TransactionLogMaxSyncDelay
            /// <summary>Default value for the property <see cref="HsqlConnectionStringBuilder.TransactionLogMaxSyncDelay"/>.</summary>
            /// <value>25</value>
            [CLSCompliant(false)]
            public const ushort TransactionLogMaxSyncDelay = 25;

            #endregion
            #region TransactionNoMultiRewrite
            /// <summary>Default value for the property <see cref="HsqlConnectionStringBuilder.TransactionLogMaxSyncDelay"/>.</summary>
            /// <value>false</value>
            public const bool TransactionNoMultiRewrite = false;

            #endregion
            #region UserId
            /// <summary>Default value for the property <see cref="HsqlConnectionStringBuilder.UserId"/>.</summary>
            /// <value>"SA"</value>
            public const string UserId = "SA";

            #endregion

            #endregion

            #region Public Static Methods

            #region Port(ConnectionProtocol)

            /// <summary>
            /// Retrieves the default port value corresponding to
            /// the given connection protocol.
            /// </summary>
            /// <remarks>
            /// Zero (0) is returned for non-network connection protocols.
            /// </remarks>
            /// <param name="protocol">
            /// The value for which to retrieve the corresponding
            /// default port value.
            /// </param>
            /// <returns>
            /// The default port for the given connection protocol.
            /// </returns>
            [CLSCompliant(false)]
            public static ushort Port(ConnectionProtocol protocol)
            {
                switch (protocol)
                {
                    case ConnectionProtocol.File:
                    case ConnectionProtocol.Mem:
                    case ConnectionProtocol.Res:
                    default:
                        {
                            return 0;
                        }
                    case ConnectionProtocol.Hsql:
                        {
                            return 
                                ServerConstants.SC_DEFAULT_HSQL_SERVER_PORT;
                        }
                    case ConnectionProtocol.Hsqls:
                        {
                            return 
                                ServerConstants.SC_DEFAULT_HSQLS_SERVER_PORT;
                        }
                    case ConnectionProtocol.Http:
                        {
                            return
                                ServerConstants.SC_DEFAULT_HTTP_SERVER_PORT;
                        }
                    case ConnectionProtocol.Https:
                        {
                            return
                                ServerConstants.SC_DEFAULT_HTTPS_SERVER_PORT;
                        }
                }
            }

            #endregion

            #endregion
        }

        #endregion
    }
}