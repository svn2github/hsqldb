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
using System.Transactions;
using System.Data.Hsqldb.Common;
#endregion

namespace System.Data.Hsqldb.Client.Internal
{
    #region HsqlEnlistment

    /// <summary>
    /// <see cref="HsqlConnection.EnlistTransaction(Transaction)"/> support.
    /// </summary>
    /// <remarks>
    /// Acts as a commit delegate for a non-distributed transaction internal
    /// to a resource manager and describes a delegated transaction for an
    /// existing transaction that can be escalated to be managed by the
    /// MSDTC when needed.
    /// </remarks>
    internal sealed class HsqlEnlistment : IPromotableSinglePhaseNotification, ISinglePhaseNotification
    {
        #region Fields
        internal Guid m_rmid;
        internal CommittableTransaction m_ctx;
        internal HsqlConnection m_dbConnection;
        internal bool m_disposeConnection;
        internal HsqlTransaction m_dbTransaction;
        internal Transaction m_systemTransaction;
        #endregion

        #region Constructor
        /// <summary>
        /// Initializes a new instance of the
        /// <see cref="HsqlEnlistment"/> class.
        /// </summary>
        /// <param name="connection">The connection.</param>
        /// <param name="transaction">The transaction.</param>
        public HsqlEnlistment(HsqlConnection connection, Transaction transaction)
        {
            m_dbConnection = connection;
            m_systemTransaction = transaction;
        }
        #endregion

        #region Properties

        #region Transaction

        /// <summary>
        /// Gets the <c>Transaction</c> object with which this object was constructed.
        /// </summary>
        /// <value>The system transaction.</value>
        public Transaction Transaction
        {
            get { return m_systemTransaction; }
        }

        #endregion

        #region Rmid
        internal Guid Rmid
        {
            get
            {
                if (m_rmid == Guid.Empty)
                {
                    m_rmid = Guid.NewGuid();
                }

                return m_rmid;
            }
        } 
        #endregion

        #endregion

        #region SinglePhaseCommit(SinglePhaseEnlistment)
        /// <summary>
        /// Notifies an enlisted object that the transaction is being committed.
        /// </summary>
        /// <param name="singlePhaseEnlistment">A <see cref="SinglePhaseEnlistment"/>
        /// interface used to send a response to the transaction manager.
        /// </param>
        public void SinglePhaseCommit(SinglePhaseEnlistment singlePhaseEnlistment)
        {
            m_dbTransaction.Commit();
            singlePhaseEnlistment.Committed();
            m_dbConnection.Enlistment = null;

            if (m_disposeConnection)
            {
                m_dbConnection.Dispose();
            }
        } 
        #endregion

        #region IPromotableSinglePhaseNotification Members

        #region Initialize()

        /// <summary>
        /// Notifies the transaction participant that the enlistment has completed successfully.
        /// </summary>
        /// <exception cref="TransactionException">
        /// When an attempt to enlist or serialize the transaction fails.
        /// </exception>
        void IPromotableSinglePhaseNotification.Initialize()
        {
            m_dbTransaction = m_dbConnection.BeginTransaction(
                HsqlConvert.ToIsolationLevel(m_systemTransaction.IsolationLevel));
        }

        #endregion

        #region Rollback(SinglePhaseEnlistment)

        /// <summary>
        /// Notifies this object that the transaction is being rolled back.
        /// </summary>
        /// <param name="singlePhaseEnlistment">
        /// Interfacet used to send a response to  the transaction manager.
        /// </param>
        void IPromotableSinglePhaseNotification.Rollback(SinglePhaseEnlistment singlePhaseEnlistment)
        {
            m_dbTransaction.Rollback();
            singlePhaseEnlistment.Aborted();
            m_dbConnection.Enlistment = null;
            
            if (m_disposeConnection)
            {
                m_dbConnection.Dispose();
            }
        }

        #endregion

        #region SinglePhaseCommit(SinglePhaseEnlistment)

        #region SinglePhaseCommit(SinglePhaseEnlistment)
        /// <summary>
        /// Notifies this object that the transaction is being committed.
        /// </summary>
        /// <param name="singlePhaseEnlistment">
        /// Interface used to send a response to the transaction manager.
        /// </param>
        void IPromotableSinglePhaseNotification.SinglePhaseCommit(SinglePhaseEnlistment singlePhaseEnlistment)
        {
            this.SinglePhaseCommit(singlePhaseEnlistment);
        } 
        #endregion

        #endregion

        #endregion

        #region ITransactionPromoter Members

        #region Promote()

        /// <summary>
        /// Notifies this object that an escalation of the delegated transaction has been requested.
        /// </summary>
        /// <returns>
        /// A transmitter/receiver propagation token that marshals a distributed transaction.
        /// </returns>
        /// <seealso cref="TransactionInterop.GetTransactionFromTransmitterPropagationToken(System.Byte[])"/>
        byte[] ITransactionPromoter.Promote()
        {
            try
            {                
                m_ctx = new CommittableTransaction();                
                m_ctx.EnlistDurable(this.Rmid, this, EnlistmentOptions.None);
                
                byte[] propagationToken = TransactionInterop.GetTransmitterPropagationToken(m_ctx);

                return propagationToken;
            }
            catch (Exception ex)
            {                
                throw ex;
            }
        }

        #endregion

        #endregion

        #region IEnlistmentNotification Members

        #region Commit(Enlistment)
        /// <summary>
        /// Notifies an enlisted object that a transaction is being committed.
        /// </summary>
        /// <param name="enlistment">
        /// An <see cref="T:System.Transactions.Enlistment"></see> object used to
        /// send a response to the transaction manager.
        /// </param>
        void IEnlistmentNotification.Commit(Enlistment enlistment)
        {
            m_dbTransaction.Commit();
            enlistment.Done();
            m_dbConnection.Enlistment = null;

            if (m_disposeConnection)
            {
                m_dbConnection.Dispose();
            }
        } 
        #endregion

        #region InDoubt(Enlistment)
        /// <summary>
        /// Notifies an enlisted object that the status of a transaction
        /// is in doubt.
        /// </summary>
        /// <param name="enlistment">
        /// An <see cref="T:System.Transactions.Enlistment"></see> object used
        /// to send a response to the transaction manager.</param>
        void IEnlistmentNotification.InDoubt(Enlistment enlistment)
        {
            enlistment.Done();
        } 
        #endregion

        #region Prepare(PreparingEnlistment)
        /// <summary>
        /// Notifies an enlisted object that a transaction is being prepared for
        /// commitment.
        /// </summary>
        /// <param name="preparingEnlistment">
        /// A <see cref="T:System.Transactions.PreparingEnlistment"></see> object
        /// used to send a response to the transaction manager.</param>
        void IEnlistmentNotification.Prepare(PreparingEnlistment preparingEnlistment)
        {
            if (m_dbTransaction.m_valid)
            {
                m_dbTransaction.Connection.Session.PrepareCommit();
                preparingEnlistment.Prepared();
            }
            else
            {
                preparingEnlistment.ForceRollback();
            }
        } 
        #endregion

        #region Rollback(Enlistment)
        /// <summary>
        /// Notifies an enlisted object that a transaction is being rolled
        /// back (aborted).
        /// </summary>
        /// <param name="enlistment">
        /// A <see cref="T:System.Transactions.Enlistment"/> object used to
        /// send a response to the transaction manager.
        /// </param>
        void IEnlistmentNotification.Rollback(Enlistment enlistment)
        {
            m_dbTransaction.Rollback();
            enlistment.Done();
            m_dbConnection.Enlistment = null;

            if (m_disposeConnection)
            {
                m_dbConnection.Dispose();
            }
        } 
        #endregion

        #endregion

        #region ISinglePhaseNotification Members

        #region SinglePhaseCommit(SinglePhaseEnlistment)
        /// <summary>
        /// Notifies an enlisted object that the transaction is being
        /// committed.
        /// </summary>
        /// <param name="singlePhaseEnlistment">
        /// A <see cref="T:System.Transactions.SinglePhaseEnlistment"/>
        /// interface used to send a response to the transaction manager.
        /// </param>
        void ISinglePhaseNotification.SinglePhaseCommit(SinglePhaseEnlistment singlePhaseEnlistment)
        {
            this.SinglePhaseCommit(singlePhaseEnlistment);
        } 
        #endregion

        #endregion
    }

    #endregion
}
