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
using System.Diagnostics;
using System.Reflection;

using HsqlProperties = org.hsqldb.persist.HsqlProperties;
using ISession = org.hsqldb.SessionInterface;
using System.Data.Hsqldb.Common;
#endregion

namespace System.Data.Hsqldb.Client.Internal
{
    #region HsqlSession

    internal sealed partial class HsqlSession
    {
        #region Factory

        internal static class Factory
        {
            #region Fields
            private static readonly object m_ResLock;
            #endregion

            #region Static Initializer
            /// <summary>
            /// Initializes the <see cref="Factory"/> class.
            /// </summary>
            static Factory()
            {
                m_ResLock = new object();
            }
            #endregion

            #region Factory Methods

            #region NewEmbeddedSession(HsqlProperties)
            /// <summary>
            /// Retrieves an embedded session using the given properties.
            /// </summary>
            /// <param name="properties">The properties.</param>
            /// <returns>an embedded session</returns>
            internal static HsqlSession NewEmbeddedSession(
                HsqlProperties properties)
            {
                if (properties == null)
                {
                    throw new ArgumentNullException("properties");
                }

                TranslateProperties(properties);

                string user = properties.getProperty("user");
                string password = properties.getProperty("password");
                string scheme = properties.getProperty("connection_type");
                string path = properties.getProperty("database");

                try
                {
                    ISession session = org.hsqldb.DatabaseManager.newSession(
                        scheme,
                        path,
                        user,
                        password,
                        properties);

                    return new HsqlSession(session);
                }
                catch (org.hsqldb.HsqlException e)
                {
                    throw new HsqlDataSourceException(e);
                }
            }
            #endregion

            #region NewEmbeddedResSession(HsqlProperties)
            /// <summary>
            /// Gets an embedded session using the given properties.
            /// </summary>
            /// <remarks>
            /// It is assumed (but not checked) that the given properties
            /// object requests a session with a res: protocol database
            /// instance; work is performed toward configuring and locking
            /// the ambient class loading environment to correctly handle
            /// searching the transitive closure of <c>ikvmres:</c>
            /// protocol resources reachable by comile-time reference, 
            /// starting with the assemblies referenced on the call stack,
            /// as well as the entry level, calling and executing assemblies,
            /// as well as their related satellite assemblies.
            /// </remarks>
            /// <param name="properties">The properties.</param>
            /// <returns>an embedded session</returns>
            internal static HsqlSession NewEmbeddedResSession(
                HsqlProperties properties)
            {
                StackTrace trace = new StackTrace();
                java.util.Set set = new java.util.HashSet();

                foreach (StackFrame frame in trace.GetFrames())
                {
                    set.add(frame.GetMethod().DeclaringType.Assembly.FullName);
                }

                List<Assembly> startingList = new List<Assembly>();

                foreach (string name in set.toArray())
                {
                    try
                    {
                        startingList.Add(Assembly.Load(name));
                    }
                    catch { }
                }

                startingList.Add(Assembly.GetExecutingAssembly());
                startingList.Add(Assembly.GetCallingAssembly());

                if (Assembly.GetEntryAssembly() != null)
                {
                    startingList.Add(Assembly.GetEntryAssembly());
                }

                java.lang.ClassLoader loader
                    = IkvmResourceLoaderFactory.CreateLoader(startingList);

                lock (m_ResLock)
                {
                    org.hsqldb.lib.ResourceStreamProvider.setLoader(loader);

                    return HsqlSession.Factory.NewEmbeddedSession(properties);
                }
            }
            #endregion

            #region NewHsqlClientSession(HsqlProperties,bool)
            /// <summary>
            /// Gets an HSQL protocol client session using the given properties.
            /// </summary>
            /// <param name="properties">The properties.</param>
            /// <param name="tls">
            /// If set to <c>true</c>, the session uses transport layer security.
            /// </param>
            /// <returns>an HSQL protocol client session</returns>
            internal static HsqlSession NewHsqlClientSession(
                HsqlProperties properties,
                bool tls)
            {
                if (properties == null)
                {
                    throw new ArgumentNullException("properties");
                }

                TranslateProperties(properties);

                string user = properties.getProperty("user");
                string password = properties.getProperty("password");
                string host = properties.getProperty("host");
                int port = properties.getIntegerProperty("port", 0);
                string path = properties.getProperty("path");
                string database = properties.getProperty("database");

                try
                {

                    //Console.WriteLine(
                    //    "GetHsqlClientSession : host {0}, port {1}, database {2}, tls {3}",
                    //    host, 
                    //    port, 
                    //    database, 
                    //    tls);

                    ISession session = new HsqlTcpClient(
                        host,
                        port,
                        path,
                        database,
                        tls,
                        user,
                        password);

                    return new HsqlSession(session);
                }
                catch (org.hsqldb.HsqlException e)
                {
                    throw new HsqlDataSourceException(e);
                }
            }
            #endregion

            #region NewHttpClientSession(HsqlProperties,bool)
            /// <summary>
            /// Gets an HTTP protocol client session using the given properties.
            /// </summary>
            /// <param name="properties">The properties.</param>
            /// <param name="tls">
            /// If set to <c>true</c>, the session uses transport layer security.
            /// </param>
            /// <returns>an HTTP protocol client session</returns>
            internal static HsqlSession NewHttpClientSession(
                HsqlProperties properties, 
                bool tls)
            {
                if (properties == null)
                {
                    throw new ArgumentNullException("properties");
                }

                TranslateProperties(properties);

                string user = properties.getProperty("user");
                string password = properties.getProperty("password");
                string host = properties.getProperty("host");
                int port = properties.getIntegerProperty("port", 0);
                string path = properties.getProperty("path");
                string database = properties.getProperty("database");

                try
                {
                    ISession session = new org.hsqldb.HTTPClientConnection(
                        host,
                        port,
                        path,
                        database,
                        tls,
                        user,
                        password);

                    return new HsqlSession(session);
                }
                catch (org.hsqldb.HsqlException e)
                {
                    throw new HsqlDataSourceException(e);
                }
            }
            #endregion

            #endregion

            #region Utility Methods

            #region TranslateProperties(HsqlProperties)
            /// <summary>
            /// Translates the given properties object.
            /// </summary>
            /// <param name="properties">The properties.</param>
            private static void TranslateProperties(HsqlProperties properties)
            {
                string user = properties.getProperty("user");
                string password = properties.getProperty("password");
                int port = properties.getIntegerProperty("port", 0);

                if (user == null)
                {
                    user = "SA";
                }
                else
                {
                    user = user.ToUpperInvariant();
                }

                if (password == null)
                {
                    password = "";
                }
                else
                {
                    password = password.ToUpperInvariant();
                }

                properties.setProperty("user", user);
                properties.setProperty("password", password);
                properties.setProperty("port", port);
            }
            #endregion

            #endregion
        }

        #endregion
    } 

    #endregion
}
