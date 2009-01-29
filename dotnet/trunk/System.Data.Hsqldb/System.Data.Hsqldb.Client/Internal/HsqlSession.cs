#region licence

/* Copyright (c) 2001-2008, The HSQL Development Group
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
using System.Data;
using System.Data.Hsqldb.Common.Enumeration;
using EmbeddedSession = org.hsqldb.Session;
using DatabaseManager = org.hsqldb.DatabaseManager;
using HSQLClientConnection = org.hsqldb.HSQLClientConnection;
using HsqlException = org.hsqldb.HsqlException;
using HTTPClientConnection = org.hsqldb.HTTPClientConnection;
using Request = org.hsqldb.Result;
using Response = org.hsqldb.Result;
using RequestType = org.hsqldb.ResultConstants.__Fields;
using ResponseType = org.hsqldb.ResultConstants.__Fields;
using PrepareAck = org.hsqldb.Result;
using ParameterDescriptor = org.hsqldb.Result;
using ResultDescriptor = org.hsqldb.Result;
using ISession = org.hsqldb.SessionInterface;
using System.Data.Hsqldb.Common;
#endregion

namespace System.Data.Hsqldb.Client.Internal
{
    #region HsqlSession
    /// <summary>
    /// Represents an SQL Sesssion with a database instance.
    /// </summary>
    /// <remarks>
    /// Encapsulates an <c>org.hsqldb.SessionInterface</c>
    /// instance, exposing a slightly more ADO.NET-centric
    /// API. In the majority of cases, this class simply delegates
    /// to the underlying <c>SessionInterface</c>, rethrowing
    /// any raised <c>org.hsqldb.HsqlException</c> object as 
    /// <c>HsqlDataSourceException</c> object.
    /// </remarks>
    internal sealed partial class HsqlSession
    {
        #region Constants
        private const string ServerVersionQuery =
@"call ""org.hsqldb.Library.getDatabaseProductName""() 
    || ' ' 
    || ""org.hsqldb.Library.getDatabaseProductVersion""()";
        #endregion

        #region Static Members

        #region Fields

        private static readonly org.hsqldb.IHsqlProtocol m_Protocol
            = org.hsqldb.HsqlProtocol.GetInstance();
        private static readonly string m_EmbeddedServerVersion
            = string.Format("{0} {1}",
                            org.hsqldb.persist.HsqlDatabaseProperties.PRODUCT_NAME,
                            org.hsqldb.persist.HsqlDatabaseProperties.THIS_FULL_VERSION);
        private static readonly int[] m_NoParameterTypes = new int[0];
        private static readonly object[] m_NoParameterValues = new object[0];

        #endregion

        #region Properties

        #region EmbeddedServerVersion
        /// <summary>
        /// Gets the embedded server version.
        /// </summary>
        /// <value>The embedded server version.</value>
        internal static string EmbeddedServerVersion
        {
            get { return m_EmbeddedServerVersion; }
        }
        #endregion

        #endregion

        #endregion

        #region Instance Members

        #region Fields
        /// <summary>
        /// The underlying <c>SessionInterface</c> instance.
        /// </summary>
        private ISession m_session;

        /// <summary>
        /// The maximum number of rows in a result set.
        /// Zero (0) currently means "unlimited".
        /// </summary>
        private int m_maxRows = 0;

        /// <summary>
        /// The server version.
        /// </summary>
        private string m_serverVersion;
        #endregion

        #region Constructors

        #region HsqlSession(SessionInterface)
        /// <summary>
        /// Constructs a new <c>HsqlSession</c>
        /// instance that delegates to the given 
        /// interface implementation.
        /// </summary>
        /// <param name="session">
        /// The interface implementation to which to delegate.
        /// </param>
        /// <exception cref="ArgumentNullException">
        /// When <c>session</c> is <c>null</c>.
        /// </exception>
        internal HsqlSession(ISession session)
        {
            if (session == null)
            {
                throw new ArgumentNullException("session");
            }         

            m_session = session;

            if (m_session is EmbeddedSession)
            {
                m_serverVersion = m_EmbeddedServerVersion;
            }
        }
        #endregion

        #endregion

        #region Methods

        #region Close()
        /// <summary>
        /// Closes this session.
        /// </summary>
        internal void Close()
        {
            try
            {
                m_session.close();
            }
            catch (HsqlException e)
            {
                throw new HsqlDataSourceException(e);
            }
        }
        #endregion

        #region Commit()
        /// <summary>
        /// Commits this session.
        /// </summary>
        internal void Commit()
        {
            try
            {
                m_session.commit();
            }
            catch (HsqlException e)
            {
                throw new HsqlDataSourceException(e);
            }
        }
        #endregion

        #region Execute(Request)
        /// <summary>
        /// Executes the specified request.
        /// </summary>
        /// <param name="request">The request.</param>
        /// <returns>
        /// The result of executing the specified request.
        /// </returns>
        internal Response Execute(Request request)
        {
            try
            {
                Response response = m_session.execute(request);

                if (response.isError())
                {
                    throw new HsqlDataSourceException(response);
                }

                return response;
            }
            catch (HsqlException e)
            {
                throw new HsqlDataSourceException(e);
            }
        }
        #endregion

        #region Direct Execution Methods

        #region ExecuteDirect(string)
        /// <summary>
        /// Executes the given SQL character sequence directly.
        /// </summary>
        /// <param name="sql">
        /// The SQL character sequence to execute.
        /// </param>
        /// <returns>
        /// The result of the execution.
        /// </returns>
        internal Response ExecuteDirect(string sql)
        {
            return Execute(m_Protocol.CreateExecuteDirectRequest(sql));
        }
        #endregion

        #region ExecuteNonQueryDirect(string)
        /// <summary>
        /// Executes the given SQL character sequence directly,
        /// returning a count of the rows affected.
        /// </summary>
        /// <param name="sql">
        /// The SQL character sequence to execute.
        /// </param>
        /// <returns>
        /// The count of the rows affected.
        /// </returns>
        internal int ExecuteNonQueryDirect(string sql)
        {
            Request request = m_Protocol.CreateExecuteDirectRequest(sql);

            m_Protocol.SetMaxRows(request, 1);

            Response response = Execute(request);

            return m_Protocol.GetUpdateCount(response);
        }
        #endregion

        #region ExecuteNonQueryBatchDirect(string[])
        /// <summary>
        /// Executes the given batch of non-query SQL
        /// character sequences directly.
        /// </summary>
        /// <param name="sql">
        /// The batch of non-query SQL character sequences.
        /// </param>
        /// <returns>
        /// An array representing the number of rows
        /// affected by each element of the batch
        /// execution.
        /// </returns>
        internal int[] ExecuteNonQueryBatchDirect(string[] sql)
        {            
            int batchSize = sql.Length;
            Request request = m_Protocol.CreateExecuteBatchDirectRequest();

            for (int i = 0; i < batchSize; i++)
            {
                object[] batchItem = new object[] { sql[i] };

                request.add(batchItem);
            }

            return ExecuteNonQueryBatchDirect(request, batchSize);
        }
        #endregion

        #region ExecuteNonQueryBatchDirect(Request,int)
        /// <summary>
        /// Executes the given direct non-query batch request.
        /// </summary>
        /// <param name="request">
        /// The direct non-query batch request.
        /// </param>
        /// <param name="expectedUpdateCounts">
        /// The expected number of update counts.
        /// </param>
        /// <returns>
        /// An array representing the number of rows
        /// affected by each element of the batch
        /// execution.
        /// </returns>
        internal int[] ExecuteNonQueryBatchDirect(Request request,
            int expectedUpdateCounts)
        {
            Response response = Execute(request);

            int[] updateCounts = m_Protocol.GetUpdateCounts(response);

            if (updateCounts.Length != expectedUpdateCounts)
            {
                throw new HsqlBatchUpdateException(updateCounts);
            }

            return updateCounts;
        }

        #endregion

        #region ExecuteScalarDirect(string)
        /// <summary>
        /// Executes the given SQL character sequence directly,
        /// returning a scalar value.
        /// </summary>
        /// <param name="sql">
        /// The SQL character sequence to execute.
        /// </param>
        /// <returns>
        /// A scalar value representing the result of the execution.
        /// </returns>
        internal object ExecuteScalarDirect(string sql)
        {
            Request request = m_Protocol.CreateExecuteDirectRequest(sql);

            m_Protocol.SetMaxRows(request, 1);

            Response response = Execute(request);

            if (response.isUpdateCount()
                || 0 >= response.getColumnCount()
                || response.isEmpty())
            {
                return null;
            }

            object value = response.rRoot.data[0];

            if (value == null)
            {
                return null;
            }
            else if (value is string)
            {
                return value;
            }
            else
            {
                int type = response.metaData.colTypes[0];
                
                return HsqlConvert.FromJava.ToObject(value,type);
            }
        }
        #endregion

        #endregion

        #region Prepared Execution Methods

        #region ExecuteNonQueryPrepared(int)
        /// <summary>
        /// Executes the prepared statement with the given identifier,
        /// returning the count of the rows affected.
        /// </summary>
        /// <param name="statementId">
        /// The statement identifier.
        /// </param>
        /// <returns>
        /// The count of the rows affected.
        /// </returns>
        internal int ExecuteNonQueryPrepared(int statementId)
        {
            return ExecuteNonQueryPrepared(statementId,
                                           m_NoParameterTypes,
                                           m_NoParameterValues);
        }
        #endregion

        #region ExecuteNonQueryPrepared(int,int[],object[])
        /// <summary>
        /// Executes the prepared statement with the given identifier,
        /// parameter types and parameter values, returning the count of
        /// the rows affected.
        /// </summary>
        /// <param name="statementId">The statement identifier.</param>
        /// <param name="parameterTypes">The parameter types.</param>
        /// <param name="parameterData">The parameter data.</param>
        /// <returns>The count of the rows affected.</returns>
        internal int ExecuteNonQueryPrepared(
            int statementId,
            int[] parameterTypes,
            object[] parameterData)
        {
            Request request = new Request(RequestType.SQLEXECUTE,
                                          parameterTypes,
                                          statementId);

            request.setParameterData(parameterData);

            return ExecuteNonQueryPrepared(request);
        }
        #endregion

        #region ExecuteNonQueryPrepared(Request)
        /// <summary>
        /// Executes the prepared non query request.
        /// </summary>
        /// <param name="request">The request.</param>
        /// <returns>
        /// The number of rows affected.
        /// </returns>
        internal int ExecuteNonQueryPrepared(Request request)
        {
            m_Protocol.SetMaxRows(request,1);

            Response response = Execute(request);

            return m_Protocol.GetUpdateCount(response);
        }
        #endregion

        #region ExecuteNonQueryBatchPrepared(Request,int)
        /// <summary>
        /// Executes the prepared non-query batch.
        /// </summary>
        /// <param name="request">
        /// The prepared batch request.
        /// </param>
        /// <param name="expectedUpdateCounts">
        /// The expected number of update counts.
        /// </param>
        /// <returns>
        /// An array representing the number of rows
        /// affected by each element of the batch
        /// execution.
        /// </returns>
        internal int[] ExecuteNonQueryBatchPrepared(Request request,
            int expectedUpdateCounts)
        {
            Response response = Execute(request);

            int[] updateCounts = m_Protocol.GetUpdateCounts(response);

            if (updateCounts.Length != expectedUpdateCounts)
            {
                throw new HsqlBatchUpdateException(updateCounts);
            }

            return updateCounts;
        }
        #endregion

        #region ExecuteScalarPrepared(int)
        /// <summary>
        /// Convenience method, equivalent to:
        /// <pre>
        /// ExecuteScalarPrepared(
        ///     statementId,
        ///     m_NoParameterTypes,
        ///     m_NoParameterValues)</pre>
        /// </summary>
        /// <param name="statementId">The statement identifier</param>
        /// <returns>
        /// First column of first row; <c>null</c> when the
        /// result has no columns or no rows.
        /// </returns>
        internal object ExecuteScalarPrepared(int statementId)
        {
            return ExecuteScalarPrepared(
                statementId,
                m_NoParameterTypes,
                m_NoParameterValues);
        }

        #endregion

        #region ExecuteScalarPrepared(int,int[],object[])
        /// <summary>
        /// Executes the given prepared statement.
        /// </summary>
        /// <param name="statementId">The statement identifier.</param>
        /// <param name="parameterTypes">The parameter types.</param>
        /// <param name="parameterData">The parameter data.</param>
        /// <returns></returns>
        internal object ExecuteScalarPrepared(int statementId,
            int[] parameterTypes, object[] parameterData)
        {
            Request request = new Request(RequestType.SQLEXECUTE,
                parameterTypes, statementId);

            m_Protocol.SetParameterData(request, parameterData);

            return ExecuteScalarPrepared(request);
        }
        #endregion

        #region ExecuteScalarPrepared(Request)
        /// <summary>
        /// Executes the given prepared request, returning a scalar value.
        /// </summary>
        /// <param name="request">The prepared request.</param>
        /// <returns>
        /// The scalar value. This is the first column of first row;
        /// <c>null</c> when the result has no columns or no rows.
        /// </returns>
        internal object ExecuteScalarPrepared(Request request)
        {
            m_Protocol.SetMaxRows(request, 1);

            Response response = Execute(request);

            if (response.isUpdateCount()
                || (0 >= response.getColumnCount())
                || response.isEmpty())
            {
                return null;
            }

            // No check for null pointers or array bounds violation.
            // We cannot get this far and still
            // have a rRoot == null, rRoot.data == null or 
            // rRoot.data.Length < 1 condition, unless
            // there is an actual (and serious) bug in the
            // underlying libraries.
            object value = response.rRoot.data[0];

            if (value == null)
            {
                return null;
            }
            else if (value is string)
            {
                return value;
            }
            else
            {
                int type = response.metaData.colTypes[0];

                return HsqlConvert.FromJava.ToObject(value, type);
            }
        }
        #endregion

        #endregion

        #region PrepareStatement(string)
        /// <summary>
        /// Prepares the specified SQL character sequence,
        /// returning an <see cref="HsqlStatement"/>
        /// object encapsulating the compiled form.
        /// </summary>
        /// <param name="sql">The SQL character sequence to prepare.</param>
        /// <returns>
        /// The compiled form of the given SQL character sequence.
        /// </returns>
        internal HsqlStatement PrepareStatement(string sql)
        {
            Request request = m_Protocol.CreatePrepareStatementRequest(sql);
            Response response = Execute(request);

            // TODO: A bit messy to sit here on the .NET side?
            //       Perhaps encapsulate some of this in HsqlProtocol

            org.hsqldb.Record root = response.rRoot;

            PrepareAck pAck = (PrepareAck)root.data[0];
            int statementId = m_Protocol.GetStatementId(pAck);

            ResultDescriptor resultDescriptor
                = (ResultDescriptor)root.next.data[0];

            ParameterDescriptor parameterDescriptor
                = (ParameterDescriptor)root.next.next.data[0];

            return new HsqlStatement(statementId, resultDescriptor,
                parameterDescriptor);
        }
        #endregion

        #region FreeStatement(int)
        /// <summary>
        /// Frees the prepared statement denoted
        /// by the given <c>statementId</c>.
        /// </summary>
        /// <param name="statementId">
        /// The statement identifier.
        /// </param>
        internal void FreeStatement(int statementId)
        {
            Execute(m_Protocol.CreateFreeStatementRequest(statementId));
        }
        #endregion

        #region IsClosed()
        /// <summary>
        /// Determines whether this instance is closed.
        /// </summary>
        /// <returns>
        /// <c>true</c> if this instance is closed;
        /// otherwise, <c>false</c>.
        /// </returns>
        internal bool IsClosed()
        {
            try
            {
                return m_session.isClosed();
            }
            catch (HsqlException e)
            {
                throw new HsqlDataSourceException(e);
            }
        }
        #endregion

        #region PrepareCommit()
        /// <summary>
        /// Prepares the commit.
        /// </summary>
        internal void PrepareCommit()
        {
            try
            {
                m_session.prepareCommit();
            }
            catch (HsqlException e)
            {
                throw new HsqlDataSourceException(e);
            }
        }
        #endregion

        #region Reset()
        /// <summary>
        /// Resets this instance.
        /// </summary>
        internal void Reset()
        {
            try
            {
                m_session.resetSession();
            }
            catch (HsqlException e)
            {
                throw new HsqlDataSourceException(e);
            }
        }
        #endregion

        #region Rollback()
        /// <summary>
        /// Rolls back the current transaction.
        /// </summary>
        internal void Rollback()
        {
            try
            {
                m_session.rollback();
            }
            catch (HsqlException e)
            {
                throw new HsqlDataSourceException(e);
            }
        }
        #endregion

        #region StartPhasedTransaction()
        /// <summary>
        /// Starts a phased transaction.
        /// </summary>
        /// <remarks>
        /// Currently no more than a stub method.
        /// </remarks>
        internal void StartPhasedTransaction()
        {
            try
            {
                m_session.startPhasedTransaction();
            }
            catch (HsqlException e)
            {
                throw new HsqlDataSourceException(e);
            }
        }
        #endregion

        #endregion

        #region Properties

        #region AutoCommit
        /// <summary>
        /// Gets or sets a value indicating whether each statement
        /// is automatically committed.
        /// </summary>
        /// <remarks>
        /// Changing this value may implicitly commit the current
        /// transaction or throw an exception if it is currently
        /// illegal to implicitly commit (such as when participating
        /// in a distributed transaction)
        /// </remarks>
        /// <value>
        /// <c>true</c> if each statement is automatically commited;
        /// otherwise, <c>false</c>.
        /// </value>
        internal bool AutoCommit
        {
            get
            {
                try
                {
                    return m_session.isAutoCommit();
                }
                catch (HsqlException e)
                {
                    throw new HsqlDataSourceException(e);
                }
            }
            set
            {
                try
                {
                    m_session.setAutoCommit(value);
                }
                catch (HsqlException e)
                {
                    throw new HsqlDataSourceException(e);
                }
            }
        }
        #endregion

        #region MaxRows
        /// <summary>
        /// The maximum number of rows in a result set.
        /// </summary>
        internal int MaxRows
        {
            get { return m_maxRows; }
            set { m_maxRows = value; }
        } 
        #endregion

        #region SessionId
        /// <summary>
        /// Gets the session identifier.
        /// </summary>
        /// <value>The session identifier.</value>
        internal int SessionId
        {
            get { return m_session.getId(); }
        }
        #endregion

        #region IsolationLevel
        /// <summary>
        /// Gets or sets the transaction isolation level.
        /// </summary>
        /// <remarks>
        /// Changing this value may implicitly commit the current
        /// transaction or throw an exception if it is currently
        /// illegal to implicitly commit (such as when participating
        /// in a distributed transaction)
        /// </remarks>
        /// <value>The transaction isolation level.</value>
        internal IsolationLevel IsolationLevel
        {
            get
            {
                try
                {
                    HsqlIsolationLevel hsqlIsolationLevel
                        = (HsqlIsolationLevel)m_session.getIsolation();

                    return HsqlConvert.ToIsolationLevel(hsqlIsolationLevel);
                }
                catch (HsqlException e)
                {
                    throw new HsqlDataSourceException(e);
                }
            }
            set
            {
                try
                {
                    int hsqlIsolationLevel
                        = (int)HsqlConvert.ToHsqlIsolationLevel(value);

                    m_session.setIsolation(hsqlIsolationLevel);
                }
                catch (HsqlException e)
                {
                    throw new HsqlDataSourceException(e);
                }
            }
        }
        #endregion

        #region ReadOnly
        /// <summary>
        /// Gets or sets a value indicating whether this session
        /// is currently read only.
        /// </summary>
        /// <remarks>
        /// Changing this value may implicitly commit the current
        /// transaction or throw an exception if it is currently
        /// illegal to implicitly commit (such as when participating
        /// in a distributed transaction)
        /// </remarks>
        /// <value>
        /// <c>true</c> if this session is currently read only;
        /// otherwise, <c>false</c>.
        /// </value>
        internal bool ReadOnly
        {
            get
            {
                try
                {
                    return m_session.isReadOnly();
                }
                catch (HsqlException e)
                {
                    throw new HsqlDataSourceException(e);
                }
            }
            set
            {
                try
                {
                    m_session.setReadOnly(value);
                }
                catch (HsqlException e)
                {
                    throw new HsqlDataSourceException(e);
                }
            }
        }
        #endregion

        #region ServerVersion
        internal string ServerVersion
        {
            get
            {
                if (m_serverVersion == null)
                {
                    m_serverVersion
                        = ExecuteScalarDirect(ServerVersionQuery) as string;
                }

                return m_serverVersion;
            }
        }
        #endregion

        #endregion

        #endregion
    }
    #endregion
}