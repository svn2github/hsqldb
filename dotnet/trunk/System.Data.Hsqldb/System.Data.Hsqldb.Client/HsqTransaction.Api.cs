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
using System.Data;
using System.Data.Common;
using System.Threading;
using System.Data.Hsqldb.Client.Internal;

#endregion

namespace System.Data.Hsqldb.Client
{
    #region HsqlTransaction
    
    public sealed partial class HsqlTransaction : DbTransaction
    {
        #region Constants

        #region MaxSavepointNameLength
        /// <summary>
        /// The maximum length of a legal savepoint name.
        /// </summary>
        public const int MaxSavepointNameLength = 128; 
        #endregion

        #region LegalSavepointNameStart
        /// <summary>
        /// The characters with which a savepoint name legally start
        /// </summary>
        public const string LegalSavepointNameStart
            = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"; 
        #endregion

        #region LegalSavepointNamePart
        /// <summary>
        /// The characters that may legally be part of a savepoint name,
        /// excluding the start character.
        /// </summary>
        public const string LegalSavepointNamePart
            = "_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"; 
        #endregion

        #endregion
        
        #region DbTransaction Member Overrides

        #region Methods

        #region Commit()

        /// <summary>
        /// Commits the database transaction this object represents.
        /// </summary>
        /// <exception cref="InvalidOperationException">
        /// When the transaction has already been committed or rolled back;
        /// when the connection is broken.
        /// </exception>
        /// <exception cref="Exception">
        /// When an error occurrs while trying to commit the transaction.
        /// </exception>
        public override void Commit()
        {
            EndTransaction(true);
        }

        #endregion

        #region Dispose(bool)

        /// <summary>
        /// Releases the unmanaged resources used by this object and
        /// optionally releases the managed resources.
        /// </summary>
        /// <param name="disposing">
        /// When <c>true</c>, this method releases all resources
        /// held by any managed objects that this
        /// <see cref="HsqlTransaction"/> references.
        /// </param>
        protected override void Dispose(bool disposing)
        {
            if (disposing)
            {
                if (m_valid)
                {
                    DisposeInternal(/*rollback*/true);
                }
            }
        }

        #endregion

        #region Rollback()

        /// <summary>
        /// Rolls back from a pending state the database
        /// transaction this object represents.
        /// </summary>
        /// <exception cref="InvalidOperationException">
        /// When this transaction has already been committed or
        /// rolled back; when the connection is broken.
        /// </exception>
        /// <exception cref="T:System.Exception">
        /// When an error occurrs while trying to commit the
        /// transaction.
        /// </exception>
        public override void Rollback()
        {
            EndTransaction(false);
        }

        #endregion

        #endregion Methods

        #region Properties

        #region Connection

        /// <summary>
        /// Gets the <see cref="HsqlConnection"/> object
        /// associated with this transaction.
        /// </summary>
        /// <remarks>
        /// Retrieves a null reference (Nothing in Visual Basic)
        /// if this transaction is no longer valid.
        /// </remarks>
        /// <value>
        /// The <see cref="HsqlConnection"/> object associated
        /// with this transaction.
        /// </value>
        public new HsqlConnection Connection
        {
            get
            {
                lock (m_syncRoot)
                {
                    return (m_valid) ? m_connection : null;
                }
            }
        }

        #endregion

        #region DbConnection

        /// <summary>
        /// Gets the <see cref="HsqlConnection"/> object
        /// associated with this transaction.
        /// </summary>
        /// <remarks>
        /// Retrieves a null reference (Nothing in Visual Basic)
        /// if this transaction is no longer valid.
        /// </remarks>
        /// <value>
        /// The <see cref="HsqlConnection"/> object associated
        /// with this transaction.
        /// </value>
        protected override DbConnection DbConnection
        {
            get { return Connection; }
        }

        #endregion

        #region IsolationLevel

        /// <summary>
        /// Specifies the <see cref="IsolationLevel"/> for this transaction.
        /// </summary>
        /// <value>
        /// The <see cref="IsolationLevel"/> for this transaction.
        /// </value>
        public override IsolationLevel IsolationLevel
        {
            get { lock (m_syncRoot) { CheckValid(); return m_isolationLevel; } }
        }

        #endregion

        #endregion Properties

        #endregion DbTransaction Member Overrides

        #region Public Savepoint Methods

        #region Save(string)
        /// <summary>
        /// Creates a new savepoint with the given name.
        /// </summary>
        /// <remarks>
        /// Implicitly releases any previously existing savepoint with the given name.
        /// </remarks>
        /// <param name="savePointName">
        /// The name of the new savepoint.
        /// </param>
        /// <exception cref="InvalidOperationException">
        /// When this transaction has already been committed or rolled back.
        /// </exception>
        /// <exception cref="ArgumentNullException">
        /// When a null savepoint name is specified.
        /// </exception>
        /// <exception cref="ArgumentException">
        /// When an empty or illegal savepoint name is specified. 
        /// </exception>  
        /// <exception cref="System.Data.Hsqldb.Common.HsqlDataSourceException">
        /// When the connection is broken.
        /// </exception>
        public void Save(string savePointName)
        {
            lock (m_syncRoot)
            {
                HandleSavepointRequest(SavepointRequestType.Savepoint,
                    savePointName);
            }
        }
        #endregion

        #region Release(string)
        /// <summary>
        /// Releases the savepoint with the given name.
        /// </summary>
        /// <param name="savePointName">
        /// The name of the savepoint to release.
        /// </param>
        /// <exception cref="InvalidOperationException">
        /// When this transaction has already been committed or rolled back.
        /// </exception>
        /// <exception cref="ArgumentNullException">
        /// When a null savepoint name is specified.
        /// </exception>
        /// <exception cref="ArgumentException">
        /// When an empty or illegal savepoint name is specified. 
        /// </exception>  
        /// <exception cref="System.Data.Hsqldb.Common.HsqlDataSourceException">
        /// When there is no savepoint with the given name
        /// -or-
        /// When the connection is broken.
        /// </exception>
        public void Release(string savePointName)
        {
            lock (m_syncRoot)
            {
                HandleSavepointRequest(
                    SavepointRequestType.ReleaseSavepoint,
                    savePointName);
            }
        }
        #endregion

        #region Rollback(string)
        /// <summary>
        /// Undoes work performed in this transaction subsequent to the given
        /// named savepoint.
        /// </summary>
        /// <param name="savePointName">
        /// The name of the savepoint to which to roll back.
        /// </param>
        /// <exception cref="InvalidOperationException">
        /// When this transaction has already been committed or rolled back.
        /// </exception>
        /// <exception cref="ArgumentNullException">
        /// When a null savepoint name is specified.
        /// </exception>
        /// <exception cref="ArgumentException">
        /// When an empty or illegal savepoint name is specified. 
        /// </exception>  
        /// <exception cref="System.Data.Hsqldb.Common.HsqlDataSourceException">
        /// When there is no savepoint with the given name
        /// -or-
        /// When the connection is broken.
        /// </exception>
        public void Rollback(string savePointName)
        {
            lock (m_syncRoot)
            {
                HandleSavepointRequest(
                    SavepointRequestType.RollbackToSavepoint,savePointName);
            }
        }
        #endregion

        #endregion
    } 
    #endregion
}