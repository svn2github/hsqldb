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
using System.Data.Common;
using System.Threading;
using System.Data.Hsqldb.Client.Internal;
using System.Data.Hsqldb.Common;

#endregion

namespace System.Data.Hsqldb.Client
{
    public sealed partial class HsqlTransaction : DbTransaction
    {
        #region Fields

        internal bool m_valid;
        internal HsqlConnection m_connection;
        internal IsolationLevel m_isolationLevel;

        private object m_syncRoot = new object();

        #endregion

        #region Enumerations

        #region SavepointRequestType
        /// <summary>
        /// Supported savepoint request types.
        /// </summary>
        internal enum SavepointRequestType
        {
            Savepoint,
            ReleaseSavepoint,
            RollbackToSavepoint,
        } 
        #endregion

        #endregion

        #region Constructors

        #region HsqlTransaction(HsqlConnection,IsolationLevel)

        /// <summary>
        /// Initializes a new instance of the
        /// <see cref="HsqlTransaction"/> class.
        /// </summary>
        /// <param name="dbConnection">The db connection.</param>
        /// <param name="isolationLevel">The isolation level.</param>
        internal HsqlTransaction(
            HsqlConnection dbConnection,
            IsolationLevel isolationLevel)
            : base()
        {
            // PRE: Caller ensures connection is non-null and open.
            m_connection = dbConnection;
            // PRE: Caller ensures IsolationLevel is supported.
            m_isolationLevel = isolationLevel;
            m_valid = true;
        }

        #endregion

        #endregion Constructors

        #region Methods

        #region CheckSavepointName(string)
        /// <summary>
        /// Checks a savepoint name, throwing if it is invalid.
        /// </summary>
        /// <param name="savePointName">The name to check.</param>
        private void CheckSavepointName(string savePointName)
        {
            if (savePointName == null)
            {
                throw new ArgumentNullException("savePointName");
            }

            savePointName = savePointName.Trim();

            if (savePointName.Length == 0)
            {
                throw new ArgumentException(
                    "Empty savepoint name",
                    "savePointName");
            }

            int count = savePointName.Length;

            if (count > 128)
            {
                throw new ArgumentException(
                    "Savepoint name is too long: " + count,
                    "savePointName");
            }

            if (0 > LegalSavepointNameStart.IndexOf(savePointName[0]))
            {
                throw new ArgumentException(
                "Illegal Savepoint Name Start: " + savePointName,
                "savePointName");
            }

            for (int i = 1; i < count; i++)
            {
                if (0 > LegalSavepointNamePart.IndexOf(savePointName[i]))
                {
                    throw new ArgumentException(
                    "Illegal Savepoint Name Part: " + savePointName,
                    "savePointName");
                }
            }
        }
        #endregion

        #region CheckValid()

        /// <summary>
        /// Checks whether this transaction is valid.
        /// </summary>
        private void CheckValid()
        {
            if (!m_valid)
            {
                // transaction ended normally (programatically via ADO.NET)
                throw new InvalidOperationException(
                    "Transaction is no longer valid"); // NOI18N
            }
            // TODO
            //else if (m_tid != m_dbConnection.Session.TransactionId)
            //{               
            //    // transaction was ended abnormally, for
            //    // example by directly executing "COMMIT", "ROLLBACK"
            //    // "SET AUTOCOMMIT TRUE" or DDL
            //    throw new InvalidOperationException(
            //        "Transaction was terminated externally,"
            //      + " most likely by directly executing"
            //      + " \"COMMIT\", \"ROLLBACK\", \"SET AUTOCOMMIT TRUE\""
            //      | " or some data definition langague (DDL)."); // NOI18N
            //}
        }

        #endregion

        #region DisposeInternal(bool)
        /// <summary>
        /// Releases the resources used by this object and
        /// optionally causes the underlying database transaction to
        /// be rolled back.
        /// </summary>
        /// <param name="rollback">
        /// When <c>true</c>, the transaction is rolled back;
        /// otherwise, the transaction is left in its current state.
        /// </param>
        internal void DisposeInternal(bool rollback)
        {
            lock (m_syncRoot)
            {
                if (m_valid)
                {
                    if (rollback)
                    {
                        m_connection.EndTransactionInternal(this, true);
                    }

                    m_valid = false;
                    m_connection = null;
                }
            }
        } 
        #endregion

        #region EndTransaction(bool)
        /// <summary>
        /// Ends the transaction.
        /// </summary>
        /// <param name="commit">
        /// When <c>true</c>, then the transaction is committed;
        /// otherwise, it is rolled back.
        /// </param>
        private void EndTransaction(bool commit)
        {
            lock (m_syncRoot)
            {
                CheckValid();

                m_connection.EndTransactionInternal(this, commit);        
            }
        } 
        #endregion

        #region HandleSavepointRequest(SavepointRequestType,string)
        /// <summary>
        /// Handles a savepoint request.
        /// </summary>
        /// <param name="type">
        /// The request type.
        /// </param>
        /// <param name="savePointName">
        /// The name of the save point.
        /// </param>
        internal void HandleSavepointRequest(SavepointRequestType type,
            string savePointName)
        {
            CheckValid();
            CheckSavepointName(savePointName);

            HsqlSession session = m_connection.Session;

            switch (type)
            {
                case SavepointRequestType.Savepoint:
                    {
                        session.ExecuteDirect(string.Concat("SAVEPOINT ",
                            savePointName));
                        break;
                    }
                case SavepointRequestType.ReleaseSavepoint:
                    {
                        session.ExecuteDirect(string.Concat(
                            "RELEASE SAVEPOINT ", savePointName));
                        break;
                    }
                case SavepointRequestType.RollbackToSavepoint:
                    {
                        session.ExecuteDirect(string.Concat(
                            "ROLLBACK TO SAVEPOINT ",savePointName));
                        break;
                    }
                default:
                    {
                        // paranoia mode: never happens in practice.
                        throw new HsqlDataSourceException(string.Concat(
                            "Unhandled SavepointRequestType: ",type),
                            org.hsqldb.Trace.GENERAL_ERROR, "S1000");
                    }
            }
        }
        #endregion

        #endregion
    }
}