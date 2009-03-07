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
using System.Collections.Generic;
using System.Diagnostics;
using System.Reflection;

using System.Data.Hsqldb.Common.Enumeration;
using System.Data.Hsqldb.Common.Sql.Types;
using System.Data.Hsqldb.Common;
#endregion

namespace System.Data.Hsqldb.Client.Internal
{
    /// <summary>
    /// Internal factory for obtaining <c>HsqlSession</c> instances by
    /// connection string or connection string builder instance.
    /// </summary>
    internal static class HsqlDriver
    {
        /// <summary>
        /// Parses the given connection URL.
        /// </summary>
        /// <param name="url">The connection URL.</param>
        /// <returns>
        /// The collection of connection properties implied by the given URL.
        /// </returns>
        internal static org.hsqldb.persist.HsqlProperties ParseURL(string url)
        {
            org.hsqldb.persist.HsqlProperties properties 
                = org.hsqldb.DatabaseURL.parseURL(url, true);

            if (properties == null || properties.isEmpty())
            {
                throw new HsqlDataSourceException("Invalid Connection URL: "
                    + url, org.hsqldb.Trace.GENERAL_ERROR, "S1000");
            }

            return properties;
        }

        /// <summary>
        /// Gets an HsqlSession corresponding to the given connection string.
        /// </summary>
        /// <param name="connectionString">
        /// The connection string for which to retrieve an HsqlSession.
        /// </param>
        /// <returns>
        /// An HsqlSession corresponding to the given connection string.
        /// </returns>
        internal static HsqlSession GetSession(string connectionString)
        {
            return GetSession(new HsqlConnectionStringBuilder(connectionString));
        }

        /// <summary>
        /// Gets an <c>HsqlSession</c> corresponding to the given connection 
        /// string builder.
        /// </summary>
        /// <param name="builder">The builder.</param>
        /// <returns>
        /// An HsqlSession corresponding to the given connection string builder.
        /// </returns>
        internal static HsqlSession GetSession(HsqlConnectionStringBuilder builder)
        {
            string url = builder.JdbcUrl;

            org.hsqldb.persist.HsqlProperties properties = HsqlDriver.ParseURL(url);

            switch (builder.Protocol)
            {
                case ConnectionProtocol.File:
                case ConnectionProtocol.Mem:
                    {
                        return HsqlSession.Factory.NewEmbeddedSession(properties);
                    }
                case ConnectionProtocol.Res:
                    {
                        return HsqlSession.Factory.NewEmbeddedResSession(properties);
                    }
                case ConnectionProtocol.Hsql:
                    {
                        return HsqlSession.Factory.NewHsqlClientSession(properties, /*tls*/false);
                    }
                case ConnectionProtocol.Hsqls:
                    {
                        return HsqlSession.Factory.NewHsqlClientSession(properties, /*tls*/true);
                    }
                case ConnectionProtocol.Http:
                    {
                        return HsqlSession.Factory.NewHttpClientSession(properties, /*tls*/false);
                    }
                case ConnectionProtocol.Https:
                    {
                        return HsqlSession.Factory.NewHttpClientSession(properties, /*tls*/true);
                    }
                default:
                    {
                        throw new HsqlDataSourceException("Unknown Connection URL: "
                            + url, org.hsqldb.Trace.GENERAL_ERROR, "S1000");
                    }
            }
        }
    }
}