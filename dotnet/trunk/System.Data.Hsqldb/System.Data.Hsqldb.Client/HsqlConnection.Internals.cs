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
using System.ComponentModel;
using System.Data;
using System.Data.Common;
using System.Data.Hsqldb.Client.Design;
using System.Data.Hsqldb.Client.Design.Attribute;
using System.Data.Hsqldb.Common.Enumeration;
using System.Data.Hsqldb.Client.Internal;
using System.Data.Hsqldb.Client.MetaData;

#if W32DESIGN
using System.Drawing;
#endif

using System.Threading;
using System.Transactions;
using System.Data.Hsqldb.Common;

#endregion

namespace System.Data.Hsqldb.Client
{
    #region HsqlConnection
    public sealed partial class HsqlConnection {
        #region Constants
        private const string DatabaseQuery =
@"select value 
   from information_schema.system_sessioninfo
  where key = 'DATABASE'";

        private const string SchemaQuery =
@"select value
   from information_schema.system_sessioninfo
  where key = 'SCHEMA'";
        #endregion

        #region Fields

        private static int idseq = 0;
        private int m_id;

        private HsqlSession m_session;
        private HsqlTransaction m_transaction;
        private HsqlEnlistment m_enlistment;
        private ConnectionState m_connectionState = ConnectionState.Closed;
        private string m_connectionString;
        internal HsqlConnectionStringBuilder m_settings;
        private HsqlDatabaseMetaData m_dbMetaData;
        private readonly object m_syncRoot = new object();

        #endregion

        #region Constructors

        #region HsqlConnection(HsqlConnection)

        /// <summary>
        /// Contructs a new <c>HsqlConnection</c> with the
        /// connection string of the other
        /// <c>HsqlConnection</c> object.
        /// </summary>
        /// <param name="other">
        /// From which to obtain the connection string.
        /// </param>
        internal HsqlConnection(HsqlConnection other)
            : this(other.ConnectionString)
        {
        }

        #endregion
        #endregion

        #region Internal Methods

        #region BeginTransactionInternal(IsolationLevel)

        /// <summary>
        /// Begins a new transaction with the specified isolation level.
        /// </summary>
        /// <param name="isolationLevel">
        /// The isolation level.
        /// </param>
        /// <returns>
        /// An object representing the new transaction.
        /// </returns>
        /// <exception cref="HsqlDataSourceException">
        /// When database access error occurs, sql session is closed
        /// or isolation level not supported.
        /// </exception>
        internal HsqlTransaction BeginTransactionInternal(
            IsolationLevel isolationLevel)
        {
            HsqlTransaction transaction = m_transaction;

            if (transaction != null)
            {
                throw new HsqlDataSourceException(
                    "A transaction is already in progress."); // NOI18N
            }

            // Note: property read access includes closed check.
            HsqlSession session = Session;

            if (isolationLevel == IsolationLevel.Unspecified)
            {
                isolationLevel = IsolationLevel.ReadUncommitted;
            }

            // Note: property write access includes validation check 
            session.IsolationLevel = isolationLevel;
            session.AutoCommit = false;

            transaction = new HsqlTransaction(this, isolationLevel);

            m_transaction = transaction;

            return transaction;
        }

        #endregion

        #region CloseInternal()

        /// <summary>
        /// Closes this connection.
        /// </summary>
        internal void CloseInternal()
        {
            ConnectionState state = m_connectionState;

            if (state == ConnectionState.Closed)
            {
                return;
            }

            HsqlSession session = m_session;

            if (m_session == null)
            {
                // Sanity-Check: Should never happen.
                throw new InvalidOperationException(
                    "HsqlSession is null"); // NOI18N
            }

            // Handle dispose/close while enlisted in a system transaction.

            HsqlTransaction transaction = m_transaction;
            HsqlEnlistment enlistment = m_enlistment;

            bool enlisted = (enlistment != null);
            bool preserveEnlistment = (enlisted && !enlistment.m_disposeConnection);

            if (preserveEnlistment)
            {
                // ...then until it ceases to participate in a
                // System.Transactions.Transaction, the enlistment
                // needs a valid local transaction to commit or
                // rollback

                HsqlConnection connection = new HsqlConnection(this);

                connection.m_connectionState = ConnectionState.Open;
                connection.m_session = session;
                connection.m_enlistment = enlistment;
                connection.m_transaction = transaction;

                enlistment.m_dbConnection = connection;
                enlistment.m_disposeConnection = true;

                if (transaction != null)
                {
                    transaction.m_connection = connection;
                }
            }

            SetStateInternal(ConnectionState.Closed);

            m_session = null;
            m_transaction = null;
            m_enlistment = null;
            m_dbMetaData = null;
            m_settings = null;

            if (!enlisted)
            {
                // No need to roll back here. This will happend automatically
                // on the back end in response to the session.Close() call
                // below.
                if (transaction != null)
                {
                    transaction.DisposeInternal(/*rollback*/false);
                }

                // release the back-end session and any associated resources,
                // such as network sockets, etc.
                session.Close();
            }
        }

        #endregion

        #region EndTransactionInternal(HsqlTransaction,bool)

        /// <summary>
        /// Ends the given <c>HsqlTransaction</c> with either
        /// a commit or rollback.
        /// </summary>
        /// <param name="transaction">The transaction to end.</param>
        /// <param name="commit">
        /// When <c>true</c> then commit; else rollback.
        /// </param>
        internal void EndTransactionInternal(HsqlTransaction transaction, bool commit)
        {
            // sanity check
            if (transaction == null)
            {
                throw new ArgumentNullException("transaction");
            }

            // We need to make a local reference now, so we can tentatively
            // set the instance field to null, but set it back if things go
            // wrong, for instance a transient network partition is in effect
            // that may revert shortly, or an integrity constraint condition
            // that cannot presently be satisfied may also shortly be rectified.
            HsqlTransaction localTransaction = m_transaction;

            // sanity check
            if (transaction != localTransaction)
            {
                throw new InvalidOperationException(
                    "Not issued on this connection: " + transaction); // NOI18N
            }

            // anticipate success
            m_transaction = null;

            try
            {
                // Important:
                //
                // Access session via the Session property, so we puke if,
                // for some reason, this connection is closed or is not in
                // a valid state 
                HsqlSession session = Session;

                if (commit)
                {
                    session.Commit();
                }
                else
                {
                    session.Rollback();
                }

                // Important: only if commit/rollback succeeds.
                session.AutoCommit = true;
            }
            catch /*(Exception ex)*/
            {
                // Failure may only be temporary, so don't leave
                // things hanging in limbo.  Instead, provide for
                // the possibility that we might want to retry the
                // operation.
                m_transaction = localTransaction;

                throw;
            }

            // Prevent further use of transaction object:
            localTransaction.DisposeInternal(/*rollback*/false);
        }

        #endregion

        #region OpenInternal()

        /// <summary>
        /// Opens this <c>HsqlConnection</c>.
        /// </summary>
        internal void OpenInternal()
        {
            if (m_connectionState == ConnectionState.Open)
            {
                throw new InvalidOperationException(
                    "Connection already open."); // NOI18N
            }

            OpenSession();
        }

        #endregion

        #region OpenSession()
        /// <summary>
        /// Opens a new session.
        /// </summary>
        private void OpenSession()
        {
            if (m_session != null)
            {
                // Sanity-Check: Should never happen.
                throw new InvalidOperationException(
                    "HsqlSession is non-null"); // NOI18N
            }

            switch (m_settings.Protocol)
            {
                case ConnectionProtocol.File:
                case ConnectionProtocol.Mem:
                case ConnectionProtocol.Res:
                    {
                        OpenSessionNoTimeout();
                        break;
                    }
                default:
                    {
                        int timeout = ConnectionTimeoutMillis;

                        if (timeout <= 0)
                        {
                            OpenSessionNoTimeout();
                        }
                        else
                        {
                            OpenSessionTimeout(timeout);
                        }
                        break;
                    }
            }
        }
        #endregion

        #region OpenSessionNoTimeout()
        private void OpenSessionNoTimeout()
        {
            SetStateInternal(ConnectionState.Connecting);

            try
            {
                m_session = HsqlDriver.GetSession(m_settings);
                SetStateInternal(ConnectionState.Open);
#if SYSTRAN
                if (m_settings.Enlist)
                {

                    Transaction tx = Transaction.Current;

                    if (tx != null)
                    {
                        EnlistTransaction(tx);
                    }
                }
#endif
            }
            catch (Exception)
            {
                m_session = null;
                SetStateInternal(ConnectionState.Closed);
                throw;
            }
        }
        #endregion

        #region OpenSessionTimeout(int)
        void OpenSessionTimeout(int timeout)
        {
            Exception se = null;
            Thread thread = new Thread(delegate()
            {
                try
                {
                    OpenSessionNoTimeout();
                }
                catch (Exception ex)
                {
                    se = ex;
                }
            });

            try
            {
                thread.Start();
                thread.Join(timeout);
            }
            catch (Exception)
            {
                m_session = null;
                SetStateInternal(ConnectionState.Closed);
                throw;
            }

            if (se != null)
            {
                m_session = null;
                SetStateInternal(ConnectionState.Closed);
                throw se;
            }
            else if (m_session == null)
            {
                SetStateInternal(ConnectionState.Closed);
                throw new TimeoutException(string.Format(
                    "Connection attempt aborted after {0} milliseconds",
                    (timeout == Threading.Timeout.Infinite) ? "Infinite"
                    : timeout.ToString("N")));
            }
        }
        #endregion

        #region SetStateInternal(ConnectionState)

        /// <summary>
        /// Sets the state of this <c>HsqlConnection</c>.
        /// </summary>
        /// <param name="newState">The new state.</param>
        internal void SetStateInternal(ConnectionState newState)
        {
            ConnectionState oldState = m_connectionState;

            if (newState == oldState)
            {
                return;
            }

            m_connectionState = newState;

            base.OnStateChange(new StateChangeEventArgs(oldState, newState));
        }

        #endregion

        #endregion

        #region Internal Properties

        #region Session
        /// <summary>
        /// Gets the session.
        /// </summary>
        /// <value>The session.</value>
        internal HsqlSession Session
        {
            get { lock (m_syncRoot) { CheckClosed(); return m_session; } }
        }
        #endregion

        #region SyncRoot
        /// <summary>
        /// Gets an object that can be used to synchronize access to this
        /// object.
        /// </summary>
        /// <remarks>
        /// Use lock(connection.SyncRoot) instead of lock(connection) due
        /// to FxCop check CA2002: DoNotLockOnObjectsWithWeakIdentity
        /// (System.MarshalByRefObject)
        /// </remarks>
        /// <value>
        /// An object that can be used to synchronize access to this object.
        /// </value>
        public object SyncRoot
        {
            get { return m_syncRoot; }
        }
        #endregion

        #region LocalTransaction
        /// <summary>
        /// Gets an <see cref="HsqlTransaction"/> instance representing
        /// the local database transaction for this connection.
        /// </summary>
        /// <value>The current database transaction; may be null.</value>
        internal HsqlTransaction LocalTransaction
        {
            get { lock (m_syncRoot) { CheckClosed(); return m_transaction; } }
        }
        #endregion

        #region Enlistment
        /// <summary>
        /// Gets or sets an object instance that represents the enlistment
        /// of the local database transaction, if any, in a
        /// System.Transactions.Transaction.
        /// </summary>
        /// <value>
        /// The current System.Transactions.Transaction enlistment; may be null.
        /// </value>
        internal HsqlEnlistment Enlistment
        {
            get { lock (m_syncRoot) { return m_enlistment; } }
            set { lock (m_syncRoot) { m_enlistment = value; } }
        }
        #endregion

        #region MetaData
        /// <summary>
        /// Retrieves the database metadata object for this connection.
        /// </summary>
        internal HsqlDatabaseMetaData MetaData
        {
            get
            {
                if (m_dbMetaData == null)
                {
                    m_dbMetaData = new HsqlDatabaseMetaData(this);
                }

                return m_dbMetaData;
            }
        }
        #endregion

        #region Settings

        /// <summary>
        /// Gets the settings.
        /// </summary>
        /// <value>The settings.</value>
        internal HsqlConnectionStringBuilder Settings
        {
            get { return m_settings; }
        }

        #endregion

        #endregion

        #region Private Methods

        #region CheckClosed()

        /// <summary>
        /// Tests if this connection is closed,
        /// throwing an exception if it is.
        /// </summary>
        /// <exception cref="InvalidOperationException">
        /// When this connection is closed.
        /// </exception>
        private void CheckClosed()
        {
            // normal check.
            if (m_connectionState == ConnectionState.Closed)
            {
                throw new InvalidOperationException(
                    "Connection is closed."); // NOI18N
            }

            // sanity check.
            HsqlSession session = m_session;

            if (session == null || session.IsClosed())
            {
                throw new InvalidOperationException(
                    "Connection is closed."); // NOI18N
            }
        }

        #endregion

        #endregion Private Methods
    } 
    #endregion
}
