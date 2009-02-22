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
    public sealed partial class HsqlConnection
    {
        #region DbConnection Members
        
        #region Method Overrides

        #region BeginDbTransaction(IsolationLevel)
        /// <summary>
        /// Starts a new database transaction.
        /// </summary>
        /// <param name="isolationLevel">
        /// Specifies the isolation level for the transaction.
        /// </param>
        /// <returns>
        /// An object representing the new transaction.
        /// </returns>
        /// <exception cref="HsqlDataSourceException">
        /// When there is already a transaction associated with this
        /// connection or when the given <c>isolationLevel</c> is not
        /// supported.
        /// </exception>
        /// <remarks>
        /// Supported isolation levels are the subset of
        /// <c>IsolationLevel</c> values described by
        /// <see cref="HsqlIsolationLevel"/>.
        /// </remarks>
        protected override DbTransaction BeginDbTransaction(
            IsolationLevel isolationLevel)
        {
            lock (m_syncRoot)
            {
                return BeginTransactionInternal(isolationLevel);
            }
        }
        #endregion

        #region ChangeDatabase(string)
        /// <summary>
        /// Changes the current database for an open connection.
        /// </summary>
        /// <param name="databaseName">
        /// Specifies the name of the database for the connection to use.
        /// </param>
        /// <exception cref="HsqlDataSourceException">
        /// When <c>databaseName</c> does not correspond to a recognized
        /// database; the current connection state does not imply
        /// authorization to use the specified database; an access error
        /// occurs when attempting to communicate with the database engine.
        /// </exception>
        /// <exception cref="InvalidOperationException">
        /// When changing the current database is not supported, given
        /// the current state of this connection, for example when it
        /// is closed.
        /// </exception>
        /// <remarks>
        /// <para>
        /// Currently not supported while a connection is open.
        /// </para>
        /// <para>
        /// When supported, the value supplied in <c>databaseName</c>
        /// must correspond to a recognized database, given the current
        /// connection state.  Also, the current connection state must
        /// imply authorization to use the specified database.
        /// </para>
        /// </remarks>
        public override void ChangeDatabase(string databaseName)
        {
            lock (m_syncRoot)
            {
                if (m_connectionState == ConnectionState.Closed)
                {
                    // CHECKME: 
                    // is this legal?
                    m_settings.Path = databaseName;
                }
                else
                {
                    throw new InvalidOperationException(
                        "Not yet supported for open connections."); // NOI18N
                }
            }
        }

        #endregion

        #region Close()
        /// <summary>
        /// Closes the connection to the database.
        /// </summary>
        /// <remarks>
        /// When this connection is already closed, this method does nothing.
        /// This is the preferred method of closing any open connection.
        /// </remarks>
        /// <exception cref="HsqlDataSourceException">
        /// When an error occurs while attempting to
        /// release the underlying database resources. This
        /// can happen if, for example, a network protocol
        /// connection attempts to communicate with a server
        /// that is no longer listening at the specified host
        /// and port.
        /// </exception>
        public override void Close()
        {
            lock (m_syncRoot) { CloseInternal(); }
        }
        #endregion

        #region CreateDbCommand()
        /// <summary>
        /// Creates and returns an <c>HsqlCommand</c> object
        /// that is associated with this <c>HsqlConnection</c>.
        /// </summary>
        /// <returns>
        /// An <c>HsqlCommand</c> object.
        /// </returns>
        protected override DbCommand CreateDbCommand()
        {
            return CreateCommand();
        }
        #endregion

        #region EnlistTransaction(Transaction)
        /// <summary>
        /// Enlists in the specified system transaction
        /// as a distributed transaction.
        /// </summary>
        /// <param name="systemTransaction">
        /// A reference to an existing
        /// <see cref="System.Transactions.Transaction"/>
        /// in which to enlist.
        /// </param>
        /// <exception cref="InvalidOperationException">
        /// When an enlistment in a different transaction is in progress.
        /// </exception>
        /// <exception cref="HsqlDataSourceException">
        /// When this connection is closed, a database access error occurs or 
        /// </exception>
        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Reliability", "CA2004:RemoveCallsToGCKeepAlive")]
        public override void EnlistTransaction(Transaction systemTransaction)
        {
            lock (m_syncRoot)
            {
                CheckClosed();

                if (systemTransaction == null)
                {
                    throw new ArgumentNullException("transaction");
                }

                HsqlEnlistment enlistment = m_enlistment;

                if (enlistment == null)
                {
                    enlistment = new HsqlEnlistment(this, systemTransaction);

                    if (!systemTransaction.EnlistPromotableSinglePhase(enlistment))
                    {
                        if (m_transaction == null)
                        {
                            BeginTransaction(HsqlConvert.ToIsolationLevel(systemTransaction.IsolationLevel));
                        }

                        enlistment.m_dbTransaction = m_transaction;
                        systemTransaction.EnlistDurable(enlistment.Rmid, enlistment, EnlistmentOptions.None);
                    }

                    m_enlistment = enlistment;

                    GC.KeepAlive(this);
                }
                else if (enlistment.Transaction != systemTransaction)
                {
                    throw new InvalidOperationException(
                        "Connection currently has transaction enlisted."
                        + "  Finish current transaction and retry."); // NOI18N
                }
            }
        }
        #endregion

        #region GetSchema()

        /// <summary>
        /// Returns the
        /// <see cref="DbMetaDataCollectionNames.MetaDataCollections"/>
        /// collection for this connection's data source.
        /// </summary>
        /// <returns>
        /// A <see cref="DataTable"/> that contains the
        /// <c>MetaDataCollections</c> information.
        /// </returns>
        /// <exception cref="HsqlDataSourceException">
        /// When the current connection settings do not imply
        /// authorization to use the specified database; an access
        /// error occurs while attempting to communicate with the
        /// database engine.
        /// </exception>
        /// <exception cref="InvalidOperationException">
        /// When this connection is in a state that disallows this
        /// operation, for example when it is closed.
        /// </exception>
        /// <remarks>
        /// <para>
        /// NOTE: Contrary to the general warning in the base class
        /// documentation, invoking this method on an <c>HsqlConnection</c>
        /// object <c>does not</c> currently cause an exception to be
        /// raised when the object is associated with a transaction.
        /// </para>
        /// </remarks>
        public override DataTable GetSchema()
        {
            lock (m_syncRoot) { return MetaData.GetSchema(); }
        }

        #endregion

        #region GetSchema(string)

        /// <summary>
        /// Returns the metadata collection corresponding
        /// to the given collection name.
        /// </summary>
        /// <param name="collectionName">
        /// Specifies the name for which to return the
        /// corresponding metadata collection.
        /// </param>
        /// <returns>
        /// A <see cref="DataTable"/> that contains the
        /// requested information.
        /// </returns>
        /// <exception cref="HsqlDataSourceException">
        /// When the current connection settings do not imply
        /// authorization to use the specified database; an access
        /// error occurs while attempting to communicate with the
        /// database engine.
        /// </exception>
        /// <exception cref="InvalidOperationException">
        /// When this connection is in a state that disallows this
        /// operation, for example when it is closed.
        /// </exception>
        /// <remarks>
        /// <para>
        /// NOTE: Contrary to the general warning in the base class
        /// documentation, invoking this method on an <c>HsqlConnection</c>
        /// object <c>does not</c> currently cause an exception to be
        /// raised when the object is associated with a transaction.
        /// </para>
        /// </remarks>
        public override DataTable GetSchema(string collectionName)
        {
            lock (m_syncRoot) { return MetaData.GetSchema(collectionName); }
        }

        #endregion

        #region GetSchema(string,string[])

        /// <summary>
        /// Returns the collection corresponding to the given name,
        /// filtered using the given restrictions.
        /// </summary>
        /// <param name="collectionName">
        /// Specifies the name for which to return the
        /// corresponding collection.
        /// </param>
        /// <param name="restrictionValues">
        /// The restricitions.
        /// </param>
        /// <returns>
        /// A <see cref="DataTable"/> that contains the
        /// requested information.
        /// </returns>
        /// <exception cref="HsqlDataSourceException">
        /// When the current connection settings do not imply
        /// authorization to use the specified database; an access
        /// error occurs while attempting to communicate with the
        /// database engine.
        /// </exception>
        /// <exception cref="InvalidOperationException">
        /// When this connection is in a state that disallows this
        /// operation, for example when it is closed.
        /// </exception>
        /// <remarks>
        /// <para>
        /// When <c>collectionName</c> is specified <c>null</c>, the
        /// returned <c>DataTable</c> contains the
        /// <see cref="DbMetaDataCollectionNames.MetaDataCollections"/>
        /// collection (which implies that the <c>restrictionValues</c>
        /// parameter is ignored).
        /// </para>
        /// <para>
        /// The <c>restrictionValues</c> parameter may supply up to
        /// <c>n</c> <em>significant</em> values (plus an arbitrary
        /// number of additional, ignored values), as specified by the
        /// <see cref="DbMetaDataCollectionNames.Restrictions"/>
        /// collection row corresponding to the named collection. In
        /// order to specify an explicit value for a given restriction
        /// at zero-based offset <c>i</c>, <c>restrictionValues</c>
        /// must be of at least length <c>i + 1</c>; when it is of
        /// length <c>m</c> less than <c>n</c>, it is treated
        /// internally as though it is of length <c>n</c> having
        /// <c>null</c> values at zero-based positions <c>m-1..n-1</c>;
        /// when <c>null</c>, it is treated internally as length
        /// <c>n</c> having <c>null</c> values at positions
        /// <c>0..n-1</c>.
        /// <para>
        /// A <c>null</c> value at any (possibly virtual) position
        /// in <c>restrictionValues</c> excludes the corresponding
        /// restriction (if any) from the search. An empty (zero-length)
        /// restriction value restricts the search to rows whose
        /// corresponding column is <c>null</c>; for columns that are known
        /// to be strictly non-null, a corresponding empty (zero-length)
        /// restriction value implies an empty result, meaning that the
        /// driver may use an optimized proceedure to generate and return
        /// an empty collection without the overhead of communication with
        /// the data source.
        /// </para>
        /// In general, when a restriction corresponds to an identifier part,
        /// it must match precisely the case with which the value is stored
        /// in the data source. Also, with the exception of catalog name
        /// restrictions, the standard SQL interpretation of the wild-card
        /// characters '%' and '_' and the wild-card escape character '\' is
        /// applied.
        /// </para>
        /// <para>
        /// NOTE: Contrary to the general warning in the base class
        /// documentation, invoking this method on an <c>HsqlConnection</c>
        /// object <c>does not</c> currently cause an exception to be
        /// raised when the object is associated with a transaction.
        /// </para>
        /// </remarks>
        public override DataTable GetSchema(string collectionName,
                                            string[] restrictionValues)
        {
            lock (m_syncRoot)
            {
                return MetaData.GetSchema(collectionName, restrictionValues);
            }
        }

        #endregion

        #region Open()

        /// <summary>
        /// Opens this connection with the settings specified by
        /// the <see cref="ConnectionString"/>.
        /// </summary>
        /// <exception cref="HsqlDataSourceException">
        /// When the current connection settings do not imply
        /// authorization to use the specified database; an access
        /// error occurs while attempting to communicate with the
        /// database engine.
        /// </exception>
        /// <exception cref="InvalidOperationException">
        /// When this connection is in a state that disallows this
        /// operation, for example when it is already open.
        /// </exception>
        public override void Open()
        {
            lock (m_syncRoot) { OpenInternal(); }
        }

        #endregion Open

        #endregion

        #region Method Hiding

        #region BeginTransaction
        /// <summary>
        /// Begins a new transaction using the default <c>IsolationLevel</c>.
        /// </summary>
        /// <remarks>
        /// <b>Warning</b>: because the HSQLDB 1.8 database engine does not
        /// support the notion of transaction identifiers, it is impossible to
        /// query whether a specific transaction is in progress or has been
        /// terminated. Hence, it is currently to be considered a programming
        /// error to mix execution of explicit SQL transaction control (e.g.
        /// COMMIT, ROLLBACK, SET AUTOCOMIT...) or data definition language
        /// (e.g. CREATE, ALTER, DROP) commands with programmatic transaction
        /// control.
        /// </remarks>
        /// <returns>
        /// An object representing the new transaction.
        /// </returns>
        public new HsqlTransaction BeginTransaction()
        {
            lock (m_syncRoot)
            {
                return BeginTransactionInternal(
                    IsolationLevel.ReadUncommitted);
            }
        }
        #endregion

        #region BeginTransaction(IsolationLevel)
        /// <summary>
        /// Begins a new transaction with the specific isolation level.
        /// </summary>
        /// <remarks>
        /// <b>Warning</b>: because the HSQLDB 1.8 database engine does not
        /// support the notion of transaction identifiers, it is impossible to
        /// query whether a specific transaction is in progress or has been
        /// terminated. Hence, it is currently to be considered a programming
        /// error to mix execution of explicit SQL transaction control (e.g.
        /// COMMIT, ROLLBACK, SET AUTOCOMIT...) or data definition language
        /// (e.g. CREATE, ALTER, DROP) commands with programmatic transaction
        /// control.
        /// </remarks>
        /// <param name="isolationLevel">The isolation level.</param>
        /// <returns>
        /// An object representing the new transaction.
        /// </returns>
        public new HsqlTransaction BeginTransaction(
            IsolationLevel isolationLevel)
        {
            lock (m_syncRoot) { return BeginTransactionInternal(isolationLevel); }
        }
        #endregion

        #region CreateCommand()
        /// <summary>
        /// Creates and returns an <c>HsqlCommand</c> object
        /// associated with this <c>HsqlConection</c>.
        /// </summary>
        /// <returns>
        /// An <c>HsqlCommand</c> object associated with
        /// this <c>HsqlConection</c>.
        /// </returns>
        public new HsqlCommand CreateCommand()
        {
            return new HsqlCommand(this);
        }
        #endregion

        #endregion

        #region Property Overrides

        #region ConnectionString

#pragma warning disable 0618
        /// <summary>
        /// The settings used to open this connection.
        /// </summary>
        /// <value>
        /// Controls how an open connection is established.
        /// </value>
        [Category("Data")]
        [DefaultValue("")]
        // Obsolete
        [RecommendedAsConfigurable(true)]
        //
        [RefreshProperties(RefreshProperties.All)]
        [ResDescription("DbConnection_ConnectionString")]
        [SettingsBindable(true)]
        public override string ConnectionString
        {
            get { return m_connectionString; }
            set
            {
                ConnectionState state = m_connectionState;

                if (state != ConnectionState.Closed)
                {
                    throw new HsqlDataSourceException(string.Format(
                        "Operation is forbidden in State: {0}.",
                        state)); // NOI18N
                }

                // Constructor might throw (and maybe differently in the future?)
                // Or we might want to check validity here before assigning.
                // For now, make a copy first, and only assign m_connectionString
                // afterward, just to be safe.
                HsqlConnectionStringBuilder newSettings
                    = new HsqlConnectionStringBuilder(value);

                m_settings = newSettings;
                m_connectionString = value;
            }
        }
#pragma warning restore 0618

        #endregion

        #region ConnectionTimeout
        /// <summary>
        /// Gets the time to wait while establishing a connection before
        /// terminating the attempt and generating an error.
        /// </summary>
        /// <value>
        /// The time (in seconds) to wait for a connection to open.
        /// </value>
        public override int ConnectionTimeout
        {
            get { return base.ConnectionTimeout; }
        }
        #endregion

        #region DbProviderFactory
        /// <summary>
        /// Gets the db provider factory.
        /// </summary>
        /// <value>The db provider factory.</value>
        protected override DbProviderFactory DbProviderFactory
        {
            get { return HsqlProviderFactory.Instance; }
        } 
        #endregion

        #region Database

        /// <summary>
        /// Retrieves the database qualifier used by this connection.
        /// </summary>
        /// <remarks>
        /// This is the database name specified in the
        /// <see cref="ConnectionString"/> property, or possibly
        /// the database name specified by sucessfully invoking
        /// <see cref="ChangeDatabase(string)"/>.
        ///</remarks>
        [ResDescription("DbConnection_Database")]
        [DesignerSerializationVisibility(DesignerSerializationVisibility.Hidden)]
        public override string Database
        {
            get
            {
                return (m_connectionState == ConnectionState.Open)
                    ? (string)Session.ExecuteScalarDirect(DatabaseQuery)
                    : Settings.Path;
            }
        }

        #endregion

        #region DataSource

        /// <summary>
        /// Retrieves the name of the database server to which this
        /// object is connected when <c>Open</c>
        /// </summary>
        /// <value></value>
        /// <returns>
        /// The name of the database server to which to connect.
        /// </returns>
        [Browsable(true)]
        [DesignerSerializationVisibility(DesignerSerializationVisibility.Hidden)]
        [ResDescription("DbConnection_Datasource")]
        public override string DataSource
        {
            get { return m_settings.DataSource; }
        }

        #endregion

        #region ServerVersion

        /// <summary>
        /// Retrieves the version string reported by the
        /// underlying data source.
        /// </summary>
        /// <remarks>
        /// When disconnected and the <see cref="ConnectionString"/>
        /// specifies embedded mode access, the statically known
        /// embedded mode version string is retrieved; otherwise
        /// the empty string is retrieved.
        /// </remarks>
        [Browsable(false)]
        [DesignerSerializationVisibility(DesignerSerializationVisibility.Hidden)]
        [ResDescription("DbConnection_ServerVersion")]
        public override string ServerVersion
        {
            get
            {
                switch (m_settings.Protocol)
                {
                    case ConnectionProtocol.File:
                    case ConnectionProtocol.Mem:
                    case ConnectionProtocol.Res:
                        {
                            return HsqlSession.EmbeddedServerVersion;
                        }
                    default:
                        {
                            return (State == ConnectionState.Closed)
                                       ? String.Empty
                                       : Session.ServerVersion;
                        }
                }
            }
        }

        #endregion

        #region State

        /// <summary>
        /// Gets the present state of this connection.
        /// </summary>
        /// <value>The state of this connection.</value>
        [Browsable(false)]
        [DesignerSerializationVisibility(DesignerSerializationVisibility.Hidden)]
        [ResDescription("DbConnection_State")]
        public override ConnectionState State
        {
            get { return m_connectionState; }
        }

        #endregion

        #endregion DbConnection Property Overrides
        
        #endregion

        #region Hsqldb-Specific Properties

        #region ConnectionTimeoutMillis
        /// <summary>
        /// Gets the connection timeout in millis.
        /// </summary>
        /// <value>The connection timeout in millis.</value>
        public int ConnectionTimeoutMillis
        {
            get
            {
                int timeoutSeconds = ConnectionTimeout;
                int timeoutMillis = (timeoutSeconds <= 0)
                    ? Threading.Timeout.Infinite
                    : (1000 & timeoutSeconds);

                return timeoutMillis;
            }
        }
        #endregion

        #region DefaultSchema

        /// <summary>
        /// Gets the present default schema.
        /// </summary>
        /// <value>The present default schema.</value>
        [Category("Data")]
        [DisplayName("Default Schema")]
        [Description("The default schema used to resolve database object names")]
        public string DefaultSchema
        {
            get
            {
                return (m_connectionState == ConnectionState.Open)
                    ? (string)Session.ExecuteScalarDirect(SchemaQuery)
                    : m_settings.InitialSchema;
            }
        }

        #endregion

        #region SyncRoot
        /// <summary>
        /// Can be used to synchronize access to this object.
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

        #endregion

        #region ICloneable Implementation

        #region Clone()
        /// <summary>
        /// Creates a new <c>HsqlConnection</c> with the same
        /// <c>ConnectionString</c> value.
        /// </summary>
        /// <remarks>
        /// The new object does not receive any further state
        /// associated with this object; it is constructed
        /// by invoking <see cref="HsqlConnection(HsqlConnection)"/>.
        /// </remarks>
        /// <returns>A cloned <c>HsqlConnection</c> object</returns>
        public HsqlConnection Clone()
        {
            return new HsqlConnection(this);
        } 
        #endregion

        #region ICloneable.Clone()
        /// <summary>
        /// Creates a new object that is a copy of the current instance.
        /// </summary>
        /// <returns>
        /// A new object that is a copy of this instance.
        /// </returns>
        object ICloneable.Clone()
        {
            return this.Clone();
        } 
        #endregion

        #endregion

        #region IDisposable Implementation

        #region Dispose(bool)
        /// <summary>
        /// Releases the unmanaged resources used by this <c>HsqlConnection</c>
        /// and optionally releases the managed resources.
        /// </summary>
        /// <param name="disposing">
        /// <c>true</c> to release both managed and unmanaged resources;
        /// <c>false</c> to release only unmanaged resources.
        /// </param>
        protected override void Dispose(bool disposing)
        {
            try
            {
                if (disposing)
                {
                    Close();
                }
            }
            finally
            {
                base.Dispose(disposing);
            }
        }
        #endregion

        #endregion
    }
}
