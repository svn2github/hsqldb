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
using System.IO;
using System.Net.Security;
using System.Security.Cryptography.X509Certificates;
using System.Text;
using System.Threading;

using IHsqlProtocol = org.hsqldb.IHsqlProtocol;
using HsqlProtocol = org.hsqldb.HsqlProtocol;
using HsqlException = org.hsqldb.HsqlException;
using HsqlTypes = org.hsqldb.Types;
using JavaBoolean = java.lang.Boolean;
using ColumnMetaData = org.hsqldb.jdbc.jdbcColumnMetaData;
using Iterator = org.hsqldb.lib.Iterator;
using Record = org.hsqldb.Record;
using Request = org.hsqldb.Result;
using Response = org.hsqldb.Result;
using ResultMetaData = org.hsqldb.Result.ResultMetaData;
using RequestType = org.hsqldb.ResultConstants.__Fields;
using ResponseType = org.hsqldb.ResultConstants.__Fields;
using ServerConstants = org.hsqldb.ServerConstants.__Fields;
#endregion

namespace System.Data.Hsqldb.Client.Internal
{
    #region HsqlDiagnostics

    /// <summary>
    /// Internal server diagnostics implementation
    /// </summary>
    /// <author name="boucherb@users"/>
    internal static class HsqlDiagnostics
    {
        private static readonly IHsqlProtocol m_Protocol = HsqlProtocol.GetInstance();

        #region Internal Static Methods

        #region Debug(string)
        /// <summary>
        /// 
        /// </summary>
        /// <param name="message">The message.</param>
        internal static void Debug(string message)
        {
            #if DEBUG
            Debug0(message, new object[0]);
            #endif
        } 
        #endregion

        #region Debug(string,object[])
        /// <summary>
        /// Debugs the specified format.
        /// </summary>
        /// <param name="format">The format.</param>
        /// <param name="args">The args.</param>
        internal static void Debug(string format, params object[] args)
        {
            #if DEBUG
            Debug0(format, args); 
            #endif
        } 
        #endregion

        #region DebugCert(X509Certificate)
        internal static void DebugCert(X509Certificate cert)
        {
           #if DEBUG
            //System.Diagnostics.Debug.Print(
            //    CertificateToString(cert)); 
            Console.WriteLine(CertificateToString(cert));
            #endif
        } 
        #endregion

        #region DebugRequest(Request)
        internal static void DebugRequest(Request request)
        {
#if DEBUG
            Debug0("request: {0}", RequestToString(request));
#endif
        } 
        #endregion

        #region DebugResponse(Response)
        internal static void DebugResponse(Response response)
        {
#if DEBUG            
            int responseType = m_Protocol.GetType(response);

            switch (responseType)
            {
                case ResponseType.UPDATECOUNT:
                    {
                        Debug0("response: updatecount: {0}",
                            m_Protocol.GetUpdateCount(response));
                        break;
                    }
                case ResponseType.DATA:
                    {
                        Debug0("response: data:\n {0}",
                            ResultMetaDataToString(response.metaData));
                        break;
                    }
                case ResponseType.ERROR:
                    {
                        HsqlException hex = new HsqlException(response);

                        int errorCode = hex.getErrorCode();
                        string sqlState = hex.getSQLState();
                        string message = hex.getMessage();

                        Debug0("response: error: [{0}] [{1}] {2}",
                            errorCode, sqlState, message);
                        break;
                    }
                case ResponseType.MULTI:
                    {
                        Debug0("response: multi...");

                        Record record = response.rRoot;

                        int count = 0;

                        while (record != null)
                        {
                            response = record.data[0] as Response;

                            if (response != null)
                            {
                                count++;

                                Debug0("multi response {0}:", count);
                                DebugResponse(response);
                            }
                        }

                        break;
                    }
                case ResponseType.SQLEXECUTE:
                    {
                        Debug0("response: sqlexecute");
                        // TODO:
                        //
                        // Basically, we need to know the responseType of
                        // request to which this is a response, before we
                        // can interpret the rest of the response
                        // properly.  The request could have been
                        // to prepare a statement, to execute a batch,
                        // etc.
                        break;
                    }
                default:
                    {
                        Debug0("response: responseType {0}", responseType);

                        break;
                    }
            }
#endif
        } 
        #endregion


        #region HandleEventProcessingException(Exception)
        /// <summary>
        /// Handles the event processing exception.
        /// </summary>
        /// <param name="exception">The exception.</param>
        internal static bool MustRethrowEventProcessingException(Exception exception)
        {
            Type exceptionType = exception.GetType();

            if (exceptionType == typeof(StackOverflowException) ||
                exceptionType == typeof(OutOfMemoryException) ||
                exceptionType == typeof(ThreadAbortException) ||
                exceptionType == typeof(NullReferenceException) ||
                exceptionType == typeof(AccessViolationException) ||
                typeof(System.Security.SecurityException).IsAssignableFrom(exceptionType))
            {
                return true;
            }

            else
            {
#if TRACE
                System.Diagnostics.Trace.TraceWarning(exception.ToString());
#endif
                return false;
            }
        }
        #endregion

        #region ThreadToString(Thread)
        /// <summary>
        /// Retrieves the <c>string</c> representation of
        /// the given <c>thread</c>.
        /// </summary>
        /// <param name="thread">
        /// For which to retrieve the <c>string</c> representation.
        /// </param>
        /// <returns>
        /// The <c>string</c> representation.
        /// </returns>
        internal static string ThreadToString(Thread thread)
        {
            StringBuilder sb = new StringBuilder("Thread");

            if (thread == null)
            {
                sb.Append("[null]");
            }
            else
            {
                sb.AppendFormat(
                    "[{0},{1},{2}]",
                    thread.ManagedThreadId,
                    thread.Name,
                    thread.Priority);
            }

            return sb.ToString();
        }
        #endregion

        #region ResultMetaDataToString(ResultMetaData)
        internal static string ResultMetaDataToString(ResultMetaData rmd)
        {
            StringBuilder sb = new StringBuilder();
            int columnCount = rmd.colTypes.Length;

            for (int i = 0; i < columnCount; i++)
            {
                if (i > 0)
                {
                    sb.AppendLine(",");
                }

                sb.AppendFormat("{0}: ", i);

                ColumnMetaData cmd = new ColumnMetaData();
                int type = rmd.colTypes[i];

                cmd.catalogName = (rmd.catalogNames[i] == null) ?
                    "" : rmd.catalogNames[i];
                cmd.schemaName = (rmd.schemaNames[i] == null) ?
                    "" : rmd.schemaNames[i];
                cmd.tableName = rmd.tableNames[i] == null ?
                    "" : rmd.tableNames[i];
                cmd.columnName = rmd.colNames[i] == null ?
                    "" : rmd.colNames[i];
                cmd.columnLabel = rmd.colLabels[i] == null ?
                    "" : rmd.colLabels[i];
                cmd.columnType = type;
                cmd.columnTypeName = HsqlTypes.getTypeString(type);
                cmd.isWritable = rmd.isWritable[i];
                cmd.isReadOnly = !cmd.isWritable;
                cmd.isAutoIncrement = rmd.isIdentity[i];
                cmd.isNullable = rmd.colNullable[i];

                cmd.columnClassName = rmd.classNames[i];

                if (string.IsNullOrEmpty(cmd.columnClassName))
                {
                    cmd.columnClassName = HsqlTypes.getColStClsName(type);
                }

                if (HsqlTypes.acceptsPrecisionCreateParam(type))
                {
                    if (rmd.colSizes[i] == 0)
                    {
                        cmd.columnDisplaySize = HsqlTypes.getMaxDisplaySize(type);
                    }
                    else
                    {
                        cmd.columnDisplaySize = rmd.colSizes[i];

                        if (HsqlTypes.acceptsScaleCreateParam(type))
                        {
                            if (rmd.colScales[i] != 0)
                            {
                                cmd.columnDisplaySize += (1 + rmd.colScales[i]);
                            }
                        }
                    }
                }
                else
                {
                    cmd.columnDisplaySize = HsqlTypes.getMaxDisplaySize(type);
                }

                if (HsqlTypes.isNumberType(type)
                        && HsqlTypes.acceptsPrecisionCreateParam(type))
                {
                    cmd.precision = rmd.colSizes[i];

                    if (cmd.precision == 0)
                    {
                        cmd.precision = HsqlTypes.getPrecision(type);
                    }
                }
                else
                {
                    cmd.precision = HsqlTypes.getPrecision(type);
                }

                if (HsqlTypes.acceptsScaleCreateParam(type))
                {
                    cmd.scale = rmd.colScales[i];
                }

                JavaBoolean iua = HsqlTypes.isUnsignedAttribute(type);

                cmd.isSigned = ((iua != null) && !iua.booleanValue());

                JavaBoolean ics = HsqlTypes.isCaseSensitive(type);

                cmd.isCaseSensitive = ((ics != null) && ics.booleanValue());
                cmd.isSearchable = HsqlTypes.isSearchable(type);

                sb.Append(cmd.toString());
            }

            return sb.ToString();
        } 
        #endregion

        #region RequestToString(Request)
        /// <summary>
        /// Retrieves a <see cref="System.String"/> value
        /// describing the given <c>request</c>.
        /// </summary>
        /// <param name="request">
        /// For which to retrieve the <c>String</c> value.
        /// </param>
        /// <returns>
        /// The corresponding value.
        /// </returns>
        internal static string RequestToString(Request request)
        {
            StringBuilder sb = new StringBuilder();

            int requestType = m_Protocol.GetType(request);

            switch (requestType)
            {
                case RequestType.SQLPREPARE:
                    {
                        sb.AppendFormat(
                            "SQLCLI:SQLPREPARE {0}",
                            m_Protocol.GetCommandText(request));

                        break;
                    }
                case RequestType.SQLEXECDIRECT:
                    {
                        if (request.getSize() <= 1)
                        {
                            sb.Append(m_Protocol.GetCommandText(request));
                        }
                        else
                        {
                            sb.AppendLine("SQLCLI:SQLEXECDIRECT:BATCHMODE");

                            Iterator it = request.iterator();

                            while (it.hasNext())
                            {
                                object[] data = (object[])it.next();

                                sb.Append(data[0]).Append('\n');
                            }
                        }

                        break;
                    }
                case RequestType.SQLEXECUTE:
                    {
                        sb.Append("SQLCLI:SQLEXECUTE:");

                        if (request.getSize() > 1)
                        {
                            sb.Append("BATCHMODE:");
                        }

                        sb.Append(m_Protocol.GetStatementId(request));

                        break;
                    }
                case RequestType.SQLFREESTMT:
                    {
                        sb.Append("SQLCLI:SQLFREESTMT:")
                          .Append(m_Protocol.GetStatementId(request));

                        break;
                    }
                case RequestType.GETSESSIONATTR:
                    {
                        sb.Append("HSQLCLI:GETSESSIONATTR");

                        break;
                    }
                case RequestType.SETSESSIONATTR:
                    {
                        // TODO - make this clean
                        object autoCommit = request.rRoot.data[4];
                        object readOnly = request.rRoot.data[6];

                        sb.Append("HSQLCLI:SETSESSIONATTR:");

                        if (autoCommit != null)
                        {
                            sb.AppendFormat(
                                " AUTOCOMMIT {0}",
                                autoCommit);
                        }

                        if (readOnly != null)
                        {
                            sb.AppendFormat(
                                " CONNECTION_READONLY {0}",
                                readOnly);
                        }

                        break;
                    }
                case RequestType.SQLENDTRAN:
                    {
                        sb.Append("SQLCLI:SQLENDTRAN:");

                        int endTranType = m_Protocol.GetEndTranType(
                            request);

                        switch (endTranType)
                        {
                            case RequestType.COMMIT:
                                {
                                    sb.Append("COMMIT");
                                    break;
                                }
                            case RequestType.ROLLBACK:
                                {
                                    sb.Append("ROLLBACK");
                                    break;
                                }
                            case RequestType.SAVEPOINT_NAME_RELEASE:
                                {
                                    sb.AppendFormat(
                                        "SAVEPOINT_NAME_RELEASE {0}",
                                        m_Protocol.GetSavepointName(request));
                                    break;
                                }
                            case RequestType.SAVEPOINT_NAME_ROLLBACK:
                                {
                                    sb.AppendFormat(
                                        "SAVEPOINT_NAME_ROLLBACK {0}",
                                        m_Protocol.GetSavepointName(request));
                                    break;
                                }
                            case RequestType.COMMIT_AND_CHAIN:
                                {
                                    sb.Append("COMMIT_AND_CHAIN");
                                    break;
                                }
                            case RequestType.ROLLBACK_AND_CHAIN:
                                {
                                    sb.Append("ROLLBACK_AND_CHAIN");
                                    break;
                                }
                            default:
                                {
                                    sb.Append(endTranType);
                                    break;
                                }
                        }

                        break;
                    }
                case RequestType.SQLSTARTTRAN:
                    {
                        sb.Append("SQLCLI:SQLSTARTTRAN");

                        break;
                    }
                case RequestType.SQLDISCONNECT:
                    {
                        sb.Append("SQLCLI:SQLDISCONNECT");

                        break;
                    }
                case RequestType.SQLSETCONNECTATTR:
                    {
                        sb.Append("SQLCLI:SQLSETCONNECTATTR:");

                        int attributeType = m_Protocol.GetAttributeType(
                            request);

                        switch (attributeType)
                        {
                            case RequestType.SQL_ATTR_SAVEPOINT_NAME:
                                {
                                    sb.AppendFormat(
                                        "SQL_ATTR_SAVEPOINT_NAME {0}",
                                        m_Protocol.GetSavepointName(request));
                                    break;
                                }
                            default:
                                {
                                    sb.Append(attributeType);
                                    break;
                                }
                        }

                        break;
                    }
                default:
                    {
                        sb.AppendFormat("SQLCLI:MODE:{0}",
                            requestType);
                        break;
                    }
            }

            return sb.ToString();
        }
        #endregion

        #region CertificateToString(X509Certificate)
        /// <summary>
        /// Retrieves a <see cref="String"/> value
        /// describing the given <see cref="X509Certificate"/>
        /// value.
        /// </summary>
        /// <param name="cert">
        /// For which to retrieve the <c>String</c> value.
        /// </param>
        /// <returns>
        /// The corresponding value.
        /// </returns>
        internal static string CertificateToString(X509Certificate cert)
        {
            X509Certificate2 cert2 = cert as X509Certificate2;

            using (StringWriter writer = new StringWriter())
            {

                writer.WriteLine("------------------------- X509 Certificate -----------------------");
                if (cert2 != null)
                {
                    writer.WriteLine("Friendly Name            : {0}", cert2.FriendlyName);
                }
                writer.WriteLine("Certificat Hash String   : {0}", cert.GetCertHashString());
                writer.WriteLine("Effective Date           : {0}", cert.GetEffectiveDateString());
                writer.WriteLine("Expiration Date          : {0}", cert.GetExpirationDateString());
                writer.WriteLine("Format                   : {0}", cert.GetFormat());
                writer.WriteLine("Key Algorithm            : {0}", cert.GetKeyAlgorithm());
                writer.WriteLine("Key Algorithm Parameters : {0}", cert.GetKeyAlgorithmParametersString());
                writer.WriteLine("Public Key               : {0}", cert.GetPublicKeyString());
                writer.WriteLine("Raw Cert Data            : {0}", cert.GetRawCertDataString());
                writer.WriteLine("Serial Number            : {0}", cert.GetSerialNumberString());
                writer.WriteLine("Issuer                   : {0}", cert.Issuer);
                writer.WriteLine("Subject                  : {0}", cert.Subject);
                if (cert2 != null)
                {
                    writer.WriteLine("Signature Algorithm      : {0} : {1}", cert2.SignatureAlgorithm.FriendlyName, cert2.SignatureAlgorithm.Value);
                    writer.WriteLine("Thumbprint               : {0}", cert2.Thumbprint);
                    writer.WriteLine("Version                  : {0}", cert2.Version);
                    X509ExtensionCollection extensions = cert2.Extensions;
                    if (extensions.Count > 0)
                    {
                        writer.WriteLine("Extensions               : ");
                        foreach (X509Extension extension in cert2.Extensions)
                        {
                            writer.WriteLine("\t{0}", extension.Format(false));
                        }
                    }
                }

                writer.WriteLine();
                writer.Flush();

                return writer.ToString();
            }
        }
        #endregion

        #region SslStreamPropertiesToString(SslStream)
        /// <summary>
        /// Retrieves a <see cref="String"/> value
        /// describing the given <see cref="SslStream"/>
        /// value.
        /// </summary>
        /// <param name="stream">
        /// For which to retrieve the <c>String</c> value.
        /// </param>
        /// <returns>
        /// The corresponding value.
        /// </returns>
        internal static string SslStreamPropertiesToString(SslStream stream)
        {
            using (StringWriter writer = new StringWriter())
            {
                writer.WriteLine("------------------------- SSL Stream -----------------------");
                writer.WriteLine("Check Cert Revocation Status : {0}", stream.CheckCertRevocationStatus);
                writer.WriteLine("Cipher Algorithm             : {0}", stream.CipherAlgorithm);
                writer.WriteLine("Cipher Strength              : {0}", stream.CipherStrength);
                writer.WriteLine("Hash Algorithm               : {0}", stream.HashAlgorithm);
                writer.WriteLine("Hash Strength                : {0}", stream.HashStrength);
                writer.WriteLine("Is Authenticated             : {0}", stream.IsAuthenticated);
                writer.WriteLine("Is Encrypted                 : {0}", stream.IsEncrypted);
                writer.WriteLine("Is MutuallyAuthenticated     : {0}", stream.IsMutuallyAuthenticated);
                writer.WriteLine("Key Exchange Algorithm       : {0}", stream.KeyExchangeAlgorithm);
                writer.WriteLine("Key Exchange Strength        : {0}", stream.KeyExchangeStrength);
                writer.WriteLine("Local Certificate            :");
                writer.WriteLine(HsqlDiagnostics.CertificateToString(stream.LocalCertificate));
                writer.WriteLine("Read Timeout                 : {0}", stream.ReadTimeout);
                writer.WriteLine("Remote Certificate           :");
                writer.WriteLine(HsqlDiagnostics.CertificateToString(stream.RemoteCertificate));
                writer.WriteLine("Ssl Protocol                 : {0}", stream.SslProtocol);
                writer.WriteLine("Write Timeout                : {0}", stream.WriteTimeout);
                writer.WriteLine();
                writer.Flush();

                return writer.ToString();
            }
        }
        #endregion

        #endregion

        #region Private Static Methods

        #region Debug0(string,object[])
        private static void Debug0(string format, params object[] args)
        {
#if DEBUG
            System.Diagnostics.StackFrame[] frames
                = new System.Diagnostics.StackTrace().GetFrames();
            System.Reflection.MethodBase method
                = frames[Math.Min(2, frames.Length - 1)].GetMethod();

            string prefix = string.Format(
                            "{0}:{1}:{2}.{3}: ",
                            DateTime.Now,
                            ThreadToString(Thread.CurrentThread),
                            method.DeclaringType.Name,
                            method.Name);

            //System.Diagnostics.Debug.Print(prefix + format, args);

            System.Console.WriteLine(prefix + format, args); 
#endif
        }
        #endregion 
        
        #endregion
    }
    #endregion
}
