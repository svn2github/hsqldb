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
using System.Net.Security;
using System.Net.Sockets;
using System.Security.Cryptography.X509Certificates;

using System.Data.Hsqldb.Common.IO;

using BufferedInputStream = java.io.BufferedInputStream;
using BufferedOutputStream = java.io.BufferedOutputStream;
using Console = System.Console;
using DataInputStream = java.io.DataInputStream;
using InputStream = java.io.InputStream;
using OutputStream = java.io.OutputStream;

using DatabaseUtils = org.hsqldb.DatabaseUtils;
using HsqlException = org.hsqldb.HsqlException;
using HsqlProtocol = org.hsqldb.HsqlProtocol;
using IHsqlProtocol = org.hsqldb.IHsqlProtocol;
using ISession = org.hsqldb.SessionInterface;
using Request = org.hsqldb.Result;
using Response = org.hsqldb.Result;
using RequestType = org.hsqldb.ResultConstants.__Fields;
using ResponseType = org.hsqldb.ResultConstants.__Fields;
using SessionInfo = org.hsqldb.SessionInterface.__Fields;
using Trace = org.hsqldb.Trace;

using ArrayUtil = org.hsqldb.lib.ArrayUtil;
using RowInputBinary = org.hsqldb.rowio.RowInputBinary;
using RowOutputBinary = org.hsqldb.rowio.RowOutputBinary;

using Stream = System.IO.Stream;
using System.Threading;
#endregion

namespace System.Data.Hsqldb.Client.Internal
{
    #region HsqlTcpClient

    /// <summary>
    /// A proxy for a remote database session.
    /// </summary>
    /// <remarks>
    /// Utilizes native HSQL protocol over TCP/IP; supports SSL.
    /// </remarks>
    /// <author name="boucherb@users"/>
    internal class HsqlTcpClient : ISession
    {
        #region Constants
        const int BUFFER_SIZE = 0x1000; // 4k bytes
        #endregion

        #region Static Fields
        // protocol helper - a work around for some 1.8.0.x hsqldb.jar package-privacy issues
        private static readonly IHsqlProtocol m_Protocol = HsqlProtocol.GetInstance();
        #endregion

        #region Instance Fields

        // I/O
        private readonly byte[] m_buffer = new byte[HsqlTcpClient.BUFFER_SIZE];
        private TcpClient m_tcpClient;
        private OutputStream m_dataOutput;
        private DataInputStream m_dataInput;
        private RowOutputBinary m_rowOutput;
        private RowInputBinary m_rowInput;
        private Request m_request;

        // flags
        private bool m_closed;
        private bool m_readOnly;
        private bool m_autoCommit;
        private bool m_tls;

        // property backing
        private string m_host;
        private int m_port;
        private string m_path;
        private string m_database;
        private int m_databaseId;
        private int m_sessionId;

        #endregion

        #region Constructor
        /// <summary>
        /// Constructs a new <c>HsqlTcpClientSession</c> instance
        /// with the given host, port, etc.
        /// </summary>
        /// <param name="host">The host.</param>
        /// <param name="port">The port.</param>
        /// <param name="path">The path. (presently unused)</param>
        /// <param name="database">The database.</param>
        /// <param name="tls">The Transport Layer Security setting</param>
        /// <param name="user">The user.</param>
        /// <param name="password">The password.</param>
        /// <exception cref="HsqlException">
        /// </exception>
        internal HsqlTcpClient(
            string host,
            int port,
            string path,
            string database,
            bool tls,
            string user,
            string password)
        {
            m_host = host;
            m_port = port;
            m_path = path;
            m_database = database;
            m_tls = tls;

            InitializeInstance();
            InitializeConnection(host, port, tls);

            Request loginRequest = m_Protocol.CreateTcpClientLoginRequest(
                user,
                password,
                database);

            Response loginResponse = Session.execute(loginRequest);

            if (loginResponse.isError())
            {
                throw Trace.error(loginResponse);
            }

            m_sessionId = m_Protocol.GetSessionId(loginResponse);
            m_databaseId = m_Protocol.GetDatabaseId(loginResponse);
        }
        #endregion

        #region Instance Methods

        #region InitializeInstance()
        /// <summary>
        /// Performs common instance-scope initialization.
        /// </summary>
        protected void InitializeInstance()
        {
            HsqlDiagnostics.Debug("...");

            m_rowOutput = new RowOutputBinary(m_buffer);
            m_rowInput = new RowInputBinary(m_rowOutput);
            m_request = m_Protocol.CreateAttributeRequest();
            m_autoCommit = true;
        }
        #endregion

        #region InitializeConnection(string,int,bool)
        /// <summary>
        /// Initializes the connection.
        /// </summary>
        /// <param name="host">The host.</param>
        /// <param name="port">The port.</param>
        /// <param name="tls">
        /// if set to <c>true</c>, use transport layer security.
        /// </param>
        /// <exception cref="HsqlException"></exception>
        protected virtual void InitializeConnection(
            string host,
            int port,
            bool tls)
        {
            HsqlDiagnostics.Debug("...");

            OpenConnection(host, port, tls);
        }
        #endregion

        #region OpenConnection(string,int,bool)
        /// <summary>
        /// Opens the connection.
        /// </summary>
        /// <param name="host">The host.</param>
        /// <param name="port">The port.</param>
        /// <param name="tls">
        /// if set to <c>true</c>, use transport layer security.
        /// </param>
        /// <exception cref="HsqlException">
        /// </exception>
        protected virtual void OpenConnection(
            string host,
            int port,
            bool tls)
        {
            if (m_tcpClient != null)
            {
                throw new System.InvalidOperationException(
                    "The connection is already open.");
            }

            HsqlDiagnostics.Debug("...");

            try
            {
                HsqlDiagnostics.Debug("Entered with arguments ({0},{1},{2})", 
                    host, port, tls);

                m_tcpClient = new TcpClient(host, port);

                HsqlDiagnostics.Debug("Created TcpClient({0},{1})", host, port);

                Stream stream = m_tcpClient.GetStream();

                HsqlDiagnostics.Debug("Got client stream from TcpClient");

                if (m_tls)
                {
                    HsqlDiagnostics.Debug("Initializing Client TLS...");

                    SslStream sslStream = new SslStream(
                        stream,
                        false,
                        ValidateRemoteCertificate,
                        null);

                    HsqlDiagnostics.Debug("Invoking sslStream.AuthenticateAsClient({0})",
                        host);

                    sslStream.AuthenticateAsClient(host);

                    stream = sslStream;
                }

                JavaInputStreamAdapter input = new JavaInputStreamAdapter(stream);
                JavaOutputStreamAdapter output = new JavaOutputStreamAdapter(stream);

                m_dataInput = new DataInputStream(new BufferedInputStream(input));
                m_dataOutput = new BufferedOutputStream(output);
            }
            catch (System.Exception e)
            {
                throw Trace.error(Trace.SOCKET_ERROR, e);
            }
        }
        #endregion

        #region CloseConnection()
        /// <summary>
        /// Closes the connection.
        /// </summary>
        protected virtual void CloseConnection()
        {
            HsqlDiagnostics.Debug("...");

            TcpClient tcpClient = m_tcpClient;

            if (tcpClient != null)
            {
                try
                {
                    tcpClient.Client.Close();
                }
                catch { }

                try
                {
                    tcpClient.Close();
                }
                catch { }

                m_tcpClient = null;
            }
        }
        #endregion

        #region ValidateRemoteCertificate(...)
        /// <summary>
        /// Implements the remote certificate validation callback.
        /// </summary>
        /// <param name="sender">The sender.</param>
        /// <param name="certificate">The certificate.</param>
        /// <param name="chain">The chain.</param>
        /// <param name="sslPolicyErrors">The SSL policy errors.</param>
        /// <returns>
        /// <c>true</c> if validation succeeds;
        /// <c>false</c> otherwise.
        /// </returns>
        protected virtual bool ValidateRemoteCertificate(
            object sender,
            X509Certificate certificate,
            X509Chain chain,
            SslPolicyErrors sslPolicyErrors)
        {
            HsqlDiagnostics.Debug("...");

            if (sslPolicyErrors == SslPolicyErrors.None)
            {
                // its all good.
                return true;
            }
            else
            {
                HsqlDiagnostics.Debug("Certificate error: {0}",
                    sslPolicyErrors);

                return false;
            }

        }
        #endregion

        #region SelectLocalCertificate(...)
        /// <summary>
        /// Implements the local certificate selection callback.
        /// </summary>
        /// <param name="sender">The sender.</param>
        /// <param name="targetHost">The target host.</param>
        /// <param name="localCertificates">The local certificates.</param>
        /// <param name="remoteCertificate">The remote certificate.</param>
        /// <param name="acceptableIssuers">The acceptable issuers.</param>
        /// <returns></returns>
        protected virtual X509Certificate SelectLocalCertificate(
            object sender,
            string targetHost,
            X509CertificateCollection localCertificates,
            X509Certificate remoteCertificate,
            string[] acceptableIssuers)
        {
            HsqlDiagnostics.Debug("Selection started....");

            if (localCertificates == null)
            {
                HsqlDiagnostics.Debug("localCertificates == null");
            }
            else
            {
                if (acceptableIssuers == null)
                {
                    HsqlDiagnostics.Debug("acceptableIssuers == null");
                }
                else
                {
                    // Use the first certificate that is from an acceptable issuer.
                    HsqlDiagnostics.Debug(
                        "Searching {0} local certificates for acceptable issuer...",
                        localCertificates.Count);

                    for (int i = 0; i < localCertificates.Count; i++)
                    {
                        X509Certificate certificate = localCertificates[i];

                        string issuer = certificate.Issuer;

                        if (Array.IndexOf(acceptableIssuers, issuer) != -1)
                        {
                            HsqlDiagnostics.Debug("Acceptable issuer found.");
                            HsqlDiagnostics.Debug("Returning localCertificates[{0}]:", i);
                            HsqlDiagnostics.DebugCert(certificate);

                            return certificate;
                        }
                    }

                    HsqlDiagnostics.Debug(
                        "Did not find a local certificate with an acceptable issuer.");
                }

                if (localCertificates.Count > 0)
                {
                    HsqlDiagnostics.Debug("returning localCertificates[0]:");
                    HsqlDiagnostics.DebugCert(localCertificates[0]);

                    return localCertificates[0];
                }
            }

            HsqlDiagnostics.Debug("returning null.");

            return null;
        }
        #endregion

        #region GetAttribute(int)
        /// <summary>
        /// Gets the value of the session attribute with the given identifier.
        /// </summary>
        /// <param name="attributeId">The attribute identifier.</param>
        /// <returns>The attribute value</returns>
        private object GetAttribute(int attributeId)
        {
            m_Protocol.SetType(m_request, RequestType.GETSESSIONATTR);

            Response response = Session.execute(m_request);

            if (response.isError())
            {
                throw Trace.error(response);
            }

            switch (attributeId)
            {
                case SessionInfo.INFO_AUTOCOMMIT:
                    {
                        return m_Protocol.GetAttributeAutoCommit(response);
                    }
                case SessionInfo.INFO_CONNECTION_READONLY:
                    {
                        return m_Protocol.GetAttributeConnectionReadOnly(
                            response);
                    }
                case SessionInfo.INFO_DATABASE:
                    {
                        return m_Protocol.GetAttributeDatabase(response);
                    }
                case SessionInfo.INFO_DATABASE_READONLY:
                    {
                        return m_Protocol.GetAttributeDatabaseReadOnly(
                            response);
                    }
                case SessionInfo.INFO_ISOLATION:
                    {
                        return m_Protocol.GetAttributeIsolation(response);
                    }
                case SessionInfo.INFO_USER:
                    {
                        return m_Protocol.GetAttributeUser(response);
                    }
                default:
                    {
                        throw new ArgumentException(
                            "attributeId",
                            "Unknown Attribute Id: " + attributeId);
                    }
            }
        }
        #endregion

        #region SetAttribute(object,int)
        /// <summary>
        /// Sets the value of the attribute denoted by the given identifier.
        /// </summary>
        /// <param name="value">The attribute value.</param>
        /// <param name="attributeId">The attribute identifier.</param>
        /// <exception cref="HsqlException">
        /// </exception>
        private void SetAttribute(object value, int attributeId)
        {
            m_Protocol.SetType(m_request, RequestType.SETSESSIONATTR);
            m_Protocol.ClearAttributes(m_request);

            switch (attributeId)
            {
                case SessionInfo.INFO_AUTOCOMMIT:
                    {
                        m_Protocol.SetAttributeAutoCommit(
                            m_request,
                            (bool)value);
                        break;
                    }
                case SessionInfo.INFO_CONNECTION_READONLY:
                    {
                        m_Protocol.SetAttributeConnectionReadOnly(
                            m_request,
                            (bool)value);
                        break;
                    }
                case SessionInfo.INFO_ISOLATION:
                    {
                        m_Protocol.SetAttributeIsolation(
                            m_request,
                            (int)value);
                        break;
                    }
                default:
                    {
                        throw new System.ArgumentException(
                            "attributeId",
                            "Invalid Attribute Id: "
                            + attributeId);
                    }
            }

            Response response = Session.execute(m_request);

            if (response.isError())
            {
                throw Trace.error(response);
            }
        }
        #endregion

        #region WriteRequest(Request)
        /// <summary>
        /// Writes the specified request.
        /// </summary>
        /// <param name="request">The request.</param>
        /// <exception cref="java.io.IOException"></exception>
        /// <exception cref="org.hsqldb.HsqlException"></exception>
        //[CLSCompliant(false)]
        protected void WriteRequest(Request request)
        {
            HsqlDiagnostics.DebugRequest(request);
            
            Request.write(request, m_rowOutput, m_dataOutput);
        }
        #endregion

        #region ReadResponse()
        /// <summary>
        /// Reads a response.
        /// </summary>
        /// <returns>An object representing the response.</returns>
        /// <exception cref="java.io.IOException"></exception>
        /// <exception cref="org.hsqldb.HsqlException"></exception>
        //[CLSCompliant(false)]
        protected Response ReadResponse()
        {
            Response response = Response.read(m_rowInput, m_dataInput);

            m_rowOutput.setBuffer(m_buffer);
            m_rowInput.resetRow(m_buffer.Length);

            return response;
        }
        #endregion

        #endregion

        #region Instance Properties

        #region Session
        /// <summary>
        /// Gets the session interface implemented by this instance.
        /// </summary>
        /// <value>The session interface.</value>
        //[CLSCompliant(false)]
        public ISession Session
        {
            get { return (ISession)this; }
        }
        #endregion

        #endregion

        #region Explicit ISession Members

        #region ISession.execute(Request)
        /// <summary>
        /// Executes the specified request.
        /// </summary>
        /// <param name="request">The request.</param>
        /// <returns></returns>
        /// <exception cref="HsqlException">
        /// </exception>
        Response ISession.execute(Request request)
        {
            lock (this)
            {
                try
                {
                    m_Protocol.SetSessionId(request, m_sessionId);
                    m_Protocol.SetDatabaseId(request, m_databaseId);

                    WriteRequest(request);

                    return ReadResponse();
                }
                catch (System.Exception e)
                {
                    throw Trace.error(
                        Trace.CONNECTION_IS_BROKEN,
                        e.ToString());
                }
            }
        }
        #endregion

        #region ISession.close()
        /// <summary>
        /// Closes this instance.
        /// </summary>
        void ISession.close()
        {
            bool closed = m_closed;

            if (closed)
            {
                return;
            }

            m_closed = true;

            try
            {
                m_Protocol.SetType(m_request, RequestType.SQLDISCONNECT);

                Session.execute(m_request);
            }
            catch { }

            try
            {
                CloseConnection();
            }
            catch { }
        }
        #endregion

        #region ISession.isReadOnly()
        /// <summary>
        /// Determines whether this Session is read only.
        /// </summary>
        /// <returns></returns>
        /// <exception cref="HsqlException">
        /// </exception>
        bool ISession.isReadOnly()
        {
            return m_readOnly = (bool)GetAttribute(
                SessionInfo.INFO_CONNECTION_READONLY);
        }
        #endregion

        #region ISession.setReadOnly(bool)
        /// <summary>
        /// Sets this Session read only.
        /// </summary>
        /// <param name="value">
        /// if set <c>true</c>, then this Session
        /// is read-only; otherwise read-write.
        /// </param>
        /// <exception cref="HsqlException">
        /// </exception>
        void ISession.setReadOnly(bool value)
        {
            if (value != m_readOnly)
            {
                SetAttribute(
                    value,
                    SessionInfo.INFO_CONNECTION_READONLY);

                m_readOnly = value;
            }
        }
        #endregion

        #region ISession.isAutoCommit()
        /// <summary>
        /// Determines whether this Session is in auto commit mode.
        /// </summary>
        /// <returns>
        /// <c>true</c> if in auto commit mode; otherwise, <c>false</c>.
        /// </returns>
        /// <exception cref="HsqlException">
        /// </exception>
        bool ISession.isAutoCommit()
        {
            return m_autoCommit = (bool)GetAttribute(
                SessionInfo.INFO_AUTOCOMMIT);
        }
        #endregion

        #region ISession.setAutoCommit(bool)
        /// <summary>
        /// Sets this session's auto commit mode.
        /// </summary>
        /// <param name="value">
        /// when <c>true</c>, this session is set to
        /// auto-commit mode; otherwise, it is set to
        /// manual-commit mode.
        /// </param>
        /// <exception cref="HsqlException">
        /// </exception>
        void ISession.setAutoCommit(bool value)
        {
            if (value != m_autoCommit)
            {
                SetAttribute(
                    value,
                    SessionInfo.INFO_AUTOCOMMIT);

                m_autoCommit = value;
            }
        }
        #endregion

        #region ISession.setIsolation(int)
        /// <summary>
        /// Sets this session's transaction isolation level.
        /// </summary>
        /// <param name="level">The level.</param>
        /// <exception cref="HsqlException">
        /// </exception>
        void ISession.setIsolation(int level)
        {
            SetAttribute(
                level,
                SessionInfo.INFO_ISOLATION);
        }
        #endregion

        #region ISession.getIsolation()
        /// <summary>
        /// Gets this session's transaction isolation level.
        /// </summary>
        /// <returns></returns>
        /// <exception cref="HsqlException">
        /// </exception>
        int ISession.getIsolation()
        {
            return (int)GetAttribute(SessionInfo.INFO_ISOLATION);
        }
        #endregion

        #region ISession.isClosed()
        /// <summary>
        /// Determines whether this instance is closed.
        /// </summary>
        /// <returns>
        /// <c>true</c> if this instance is closed; otherwise, <c>false</c>.
        /// </returns>
        bool ISession.isClosed()
        {
            return m_closed;
        }
        #endregion

        #region ISession.startPhasedTransaction()
        /// <summary>
        /// Starts a phased transaction (currently unimplemented).
        /// </summary>
        /// <exception cref="HsqlException">
        /// </exception>
        void ISession.startPhasedTransaction() { }
        #endregion

        #region ISession.prepareCommit()
        /// <summary>
        /// Prepares this session for commit.
        /// </summary>
        /// <exception cref="HsqlException">
        /// </exception>
        void ISession.prepareCommit()
        {
            m_Protocol.SetType(m_request, RequestType.SQLENDTRAN);
            m_Protocol.SetEndTranType(m_request, RequestType.HSQLPREPARECOMMIT);
            m_Protocol.SetSavepointName(m_request, "");

            Session.execute(m_request);
        }
        #endregion

        #region ISession.commit()
        /// <summary>
        /// Commits this session.
        /// </summary>
        /// <exception cref="HsqlException">
        /// </exception>
        void ISession.commit()
        {
            m_Protocol.SetType(m_request, RequestType.SQLENDTRAN);
            m_Protocol.SetEndTranType(m_request, RequestType.COMMIT);
            m_Protocol.SetSavepointName(m_request, "");

            Session.execute(m_request);
        }
        #endregion

        #region ISession.rollback()
        /// <summary>
        /// Rollbacks this session.
        /// </summary>
        /// <exception cref="HsqlException">
        /// </exception>
        void ISession.rollback()
        {
            m_Protocol.SetType(m_request, RequestType.SQLENDTRAN);
            m_Protocol.SetEndTranType(m_request, RequestType.ROLLBACK);
            m_Protocol.SetSavepointName(m_request, "");

            Session.execute(m_request);
        }
        #endregion

        #region ISession.getId()
        /// <summary>
        /// Gets the session id.
        /// </summary>
        /// <returns></returns>
        int ISession.getId()
        {
            return m_sessionId;
        }
        #endregion

        #region ISession.resetSession()
        /// <summary>
        /// Resets this session.
        /// </summary>
        /// <remarks>
        /// Used to reset the remote session object. In case of
        /// failure, the underlying TPC connection is physically
        /// closed. When a pooled database connection's close()
        /// method is called, it should delegate to this method
        /// instead of ISession.close() and return this object
        /// to the pool upon success. In this way, a remote session
        /// proxy object can be reused with no further
        /// initialisation.
        /// </remarks>
        /// <exception cref="HsqlException">
        /// </exception>
        void ISession.resetSession()
        {
            Request request = new Request(RequestType.HSQLRESETSESSION);
            Response response = Session.execute(request);

            if (response.isError())
            {
                m_closed = true;

                CloseConnection();

                throw Trace.error(response);
            }

            m_sessionId = m_Protocol.GetSessionId(response);
            m_databaseId = m_Protocol.GetDatabaseId(response);
        }
        #endregion

        #endregion
    }

    #endregion
}
