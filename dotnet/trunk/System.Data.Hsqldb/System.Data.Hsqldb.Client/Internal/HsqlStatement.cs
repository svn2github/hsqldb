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
using ParameterBatch = org.hsqldb.Record;
using Request = org.hsqldb.Result;
using Response = org.hsqldb.Result;
using ParameterDescriptor = org.hsqldb.Result;
using ResultDescriptor = org.hsqldb.Result;
using RequestType = org.hsqldb.ResultConstants.__Fields;
#endregion

namespace System.Data.Hsqldb.Client.Internal
{
    #region HsqlStatement

    /// <summary>
    /// Client-side proxy for an HSQLDB prepared or callable statement.
    /// </summary>
    internal sealed class HsqlStatement
    {
        #region Fields
        private int m_statementId;
        private ResultDescriptor m_resultDescriptor;
        private ParameterDescriptor m_parameterDescriptor;
        private Request m_request;
        private Request m_batchRequest;
        //private int m_maxRows = 0; 
        #endregion

        #region Constructors

        #region HsqlStatement(int,Result,Result)
        /// <summary>
        /// Initializes a new instance of the
        /// <see cref="HsqlStatement"/> class.
        /// </summary>
        /// <param name="statementId">
        /// The statement identifier.
        /// </param>
        /// <param name="resultDescriptor">
        /// The result meta data descriptor.
        /// </param>
        /// <param name="parameterDescriptor">
        /// The parameter meta data descriptor.
        /// </param>
        internal HsqlStatement(
            int statementId,
            ResultDescriptor resultDescriptor,
            ParameterDescriptor parameterDescriptor)
        {
            m_statementId = statementId;
            m_resultDescriptor = resultDescriptor;
            m_parameterDescriptor = parameterDescriptor;

            m_request = new Request(
                RequestType.SQLEXECUTE,
                m_parameterDescriptor.metaData.colTypes,
                m_statementId);

            m_batchRequest = new Request(
                RequestType.SQLEXECUTE,
                m_parameterDescriptor.metaData.colTypes,
                m_statementId);
        }
        #endregion

        #endregion

        #region Methods

        #region AddBatch(object[])
        /// <summary>
        /// Adds the given parameters to the batch request.
        /// </summary>
        /// <param name="parameters">The parameters.</param>
        internal void AddBatch(params object[] parameters)
        {
            m_batchRequest.add(parameters);
        } 
        #endregion

        #region ClearBatch()
        /// <summary>
        /// Clears any parameters previously added to the batch request.
        /// </summary>
        internal void ClearBatch()
        {
            m_batchRequest.clear();
        } 
        #endregion

        #region ClearParameters()
        /// <summary>
        /// Clears the parameters.
        /// </summary>
        internal void ClearParameters()
        {
            m_request.clear();
        }
        #endregion

        #region Execute(HsqlSession)
        /// <summary>
        /// Executes this statement in the context of the specified session.
        /// </summary>
        /// <param name="session">The session.</param>
        /// <returns>
        /// A response encapsulating the result of execution.
        /// </returns>
        internal Response Execute(HsqlSession session)
        {
            return session.Execute(m_request);
        } 
        #endregion

        #region ExecuteBatch(HsqlSession)
        /// <summary>
        /// Executes a batch request in the context of the specified session.
        /// </summary>
        /// <param name="session">The session.</param>
        /// <returns>
        /// </returns>
        internal int[] ExecuteBatch(HsqlSession session)
        {
            return session.Execute(m_batchRequest).getUpdateCounts();
        } 
        #endregion

        #region ExecuteNonQuery(HsqlSession)
        /// <summary>
        /// Executes this statement in the context of the given session,
        /// returning the number of rows affected.
        /// </summary>
        /// <param name="session">The session.</param>
        /// <returns>The number of rows affected.</returns>
        internal int ExecuteNonQuery(HsqlSession session)
        {
            return session.ExecuteNonQueryPrepared(m_request);
        } 
        #endregion

        #region ExecuteScalar(HsqlSession)
        /// <summary>
        /// Executes this statement in the context of the given session,
        /// returing a scalar values.
        /// </summary>
        /// <param name="session">The session.</param>
        /// <returns>
        /// a scalar value.
        /// </returns>
        internal object ExecuteScalar(HsqlSession session)
        {
            return session.ExecuteScalarPrepared(m_request);
        } 
        #endregion

        #region Free(HsqlSession)

        internal void Free(HsqlSession session)
        {
            session.FreeStatement(m_statementId);
        }

        #endregion

        #region SetParameters(object[])
        /// <summary>
        /// Sets the parameters.
        /// </summary>
        /// <param name="parameters">The parameters.</param>
        internal void SetParameters(params object[] parameters)
        {
            m_request.setParameterData(parameters);
        }
        #endregion

        #endregion

        #region Properties

        #region MaxRows
        /// <summary>
        /// Sets the max rows.
        /// </summary>
        /// <value>The max rows.</value>
        internal int MaxRows
        {
            set { m_request.setMaxRows(value); }
        } 
        #endregion

        #region ParameterCount
        /// <summary>
        /// Gets the parameter count.
        /// </summary>
        /// <value>The parameter count.</value>
        internal int ParameterCount
        {
            get { return m_parameterDescriptor.metaData.colTypes.Length; }
        } 
        #endregion

        #region ParameterDescriptor
        /// <summary>
        /// Gets the parameter meta data descriptor for this statement.
        /// </summary>
        /// <value>The parameter meta data descriptor.</value>
        internal ParameterDescriptor ParameterDescriptor
        {
            get { return m_parameterDescriptor; }
        }
        #endregion

        #region ParameterTypes
        /// <summary>
        /// Gets the parameter types.
        /// </summary>
        /// <value>The parameter types.</value>
        internal int[] ParameterTypes
        {
            get { return m_parameterDescriptor.metaData.colTypes; }
        } 
        #endregion       

        #region ResultDescriptor
        /// <summary>
        /// Gets the result meta data descriptor for this statement.
        /// </summary>
        /// <value>The result meta data descriptor.</value>
        internal ResultDescriptor ResultDescriptor
        {
            get { return m_resultDescriptor; }
        }
        #endregion

        #region StatementId
        /// <summary>
        /// Gets the numeric identifier of this statement.
        /// </summary>
        /// <value>The numeric identifier of this statement.</value>
        internal int StatementId
        {
            get { return m_statementId; }
        }
        #endregion

        #endregion
    }

    #endregion
}
