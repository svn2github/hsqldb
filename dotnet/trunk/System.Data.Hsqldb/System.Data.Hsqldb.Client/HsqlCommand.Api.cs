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
using System.ComponentModel;
using System.Data.Common;
using System.Data.Hsqldb.Common;
using System.Data.Hsqldb.Common.Attribute;

#if W32DESIGN
using System.Drawing;
#endif

#endregion

namespace System.Data.Hsqldb.Client
{
    #region HsqlCommand

    public sealed partial class HsqlCommand : DbCommand, ICloneable
    {
        #region Events
        /// <summary>
        /// Occurs when a member access action raises a warning condition.
        /// </summary>
        /// <remarks>
        /// This may occur, for example, to indicate that an invalid value
        /// used to set a property has been silently converted to a default
        /// valid value.
        /// </remarks>
        public event HsqlWarningEventHandler Warning;

        #region StatementCompleted
        /// <summary>
        /// Occurs when the execution of an <see cref="HsqlCommand"/> completes.
        /// </summary>
        /// <filterpriority>2</filterpriority>
        [ResCategory(ResCategoryAttribute.ResKey.ForData), ResDescription("DbCommand_StatementCompletedEventHandler")]
        public event StatementCompletedEventHandler StatementCompleted; 
        #endregion
        #endregion

        #region DbCommand Members

        #region Instance Method Overrides

        #region Cancel()

        /// <summary>
        /// Attempts to cancel the execution of this command.
        /// </summary>
        /// <remarks>
        /// <para>
        /// Currently ignored (does nothing).
        /// </para>
        /// <para>
        /// When implemented, should only signal intent and 
        /// should never throw an exception.
        /// </para>
        /// </remarks>
        public override void Cancel()
        {
            // TODO
        }

        #endregion

        #region CreateDbParameter()

        /// <summary>
        /// Creates a new <see cref="HsqlParameter"/> object.
        /// </summary>
        /// <remarks>
        /// Simply delegates to <see cref="CreateParameter()"/>.
        /// </remarks>
        /// <returns>
        /// A new <see cref="HsqlParameter"/> object.
        /// </returns>
        protected override DbParameter CreateDbParameter()
        {
            return CreateParameter();
        }

        #endregion

        #region CreateParameter()

        /// <summary>
        /// Creates a new <see cref="HsqlParameter"/> object having the
        /// default property values and no initial association with a parameter
        /// collection, for example, the parameter collection of this command
        /// object.
        /// </summary>
        /// <remarks>
        /// <para>
        /// Although not explicitly stated in the ADO.NET API docmentation for
        /// either <see cref="IDbCommand"/> or <see cref="DbParameter"/>, after
        /// a careful review of the history of the ADO.NET API and the current
        /// behavior of <see cref="System.Data.Odbc.OdbcCommand"/>, 
        /// <see cref="System.Data.OleDb.OleDbCommand"/>
        /// and <see cref="System.Data.SqlClient.SqlCommand"/>, it appears that
        /// the interface methods
        /// <see cref="IDbConnection.CreateCommand()"/> and 
        /// <see cref="IDbCommand.CreateParameter"/> were intended to do no
        /// more than to form a factory method hierarchy for obtaining
        /// vendor-neutral default instances of <see cref="IDbCommand"/> and 
        /// <see cref="IDbDataParameter"/>, respectively, under the requirement
        /// of a fully 'bean-style' OO approach to parameterized command execution
        /// within a disconnected data API supporting reflection-enabled component
        /// design surfaces.
        /// </para>
        /// <para>
        /// Of course, the fatal flaw in the ADO.NET 1.0 scheme is that one is
        /// still required to directly construct a provider-specific concrete
        /// implementation of <see cref="IDbConnection"/> before proceeding
        /// with the task of interacting with a data source, despite the
        /// inclusion of provider-netural factory methods in the interfaces
        /// that form the internal nodes of the access hierarchy.  That is,
        /// the ADO.NET 1.0 scheme quite literally fails to address the 'root'
        /// of the problem, and this failure is still not adequately addressed
        /// in ADO.NET 2.0... ;-)
        /// </para>
        /// <para>
        /// Adequate or otherwise, it *was* addressed, as it seems almost
        /// certain that the just-discussed failing of ADO.NET 1.0 is, at least
        /// in part, what prompted the introduction of
        /// <see cref="System.Data.Common.DbProviderFactory"/> in ADO.NET 2.0,
        /// making it possible to obtain concrete implementations of each of
        /// the core ADO.NET <c>System.Data</c> interfaces, without necessarily
        /// resorting to direct, concrete constuction of a root,
        /// provider-specific <c>IDbConnection</c> instance from which all else
        /// flows. 
        /// </para>
        /// <para>
        /// With the introduction of <c>DbProviderFactory</c>, however, the only
        /// real remaining advantage of the <c>IDbConnection.CreateCommand()</c>
        /// and <c>IDbCommand.CreateParameter()</c> methods is that they support
        /// class libraries, both legacy and future, that need only be
        /// initialized with (or take as method arguments) connection and/or
        /// command interface implementation instances, whereas the implicit
        /// restriction of the purist approach to utilizing 
        /// <c>DbProviderFactory</c> is that, under the existing state of the
        /// surrounding API design, it must be possible at runtime to obtain
        /// a provider factory implementation that is compatible with the
        /// runtime type of a dependent <c>System.Data</c> interface instance,
        /// for example to obtain a new command instance that is compatible with
        /// a given connection instance or a new parameter instance that is
        /// compatible with a given command instance.
        /// </para>
        /// <para>
        /// Whether this truely is an advantage or is rather a hinderance,
        /// however, is not clear, as it is a simple thing to use a
        /// provider-specific default class constructor or an explicit cast
        /// in addition to a provider-neutral factory method invocation in
        /// order to access the provider-specific features of any
        /// <c>IDbDataParameter</c>, and it can only be assumed that it was
        /// precisely the desire to provide developers with a better and
        /// more obvious way of writing provider-neutral code that prompted
        /// the introduction of <see cref="DbProviderFactory"/>.
        /// </para>
        /// <para>
        /// Indeed, with the addition of <c>DbProviderFactory</c>, it is now
        /// also possible to guaratee, in a provider-neutral fashion, a supply
        /// of <c>System.Data</c> interface implementations that are also
        /// highly compatible with the system component model by virtue of
        /// inheriting from <c>Component</c> or <c>MarshalByRefObject</c>.
        /// </para>
        /// <para>
        /// So it should serve as a bit of a red-flag that the
        /// <c>IDbCommand.CreateParameter()</c> method remains, has not been
        /// marked with the <c>[Obsolete("reason")]</c> attribute and, instead,
        /// the <c>System.Data.Common.DbCommand</c> base class introduced in
        /// ADO.NET 2.0 exposes a new, abstract 
        /// <see cref="System.Data.Common.DbConnection.CreateDbCommand()"/>
        /// method that only further duplicates/obsfucates the purpose of
        /// <c>DbProviderFactory.CreateParameter()</c>.
        /// </para>
        /// <para>
        /// Indeed, it seems to highlight that the ADO.NET 1.0 API was
        /// not thought out sufficiently beyond the immediate scope of 
        /// attempting to shift away from a connected data API toward a
        /// disconnected data API and serves as a reminder that, even
        /// in ADO.NET 2.0, the updated design still represents a
        /// somewhat leaky abstraction, for instance via the evidently only
        /// half-hearted 'beanification' of data access that, as a result,
        /// introduced things like writable parent association properties.
        /// For example, should changing a prepared command object's parent
        /// connection invalidate the command's prepared state, or should a
        /// command attempt to maintain it's prepared state in response to
        /// changing it's parent connection?  If it does not attempt to
        /// maintain its prepared state, should it not at least attempt
        /// to deallocate the resources used to attain the prepared state
        /// that has been invalidated? If that is so, should best
        /// practice and known issues not be stated somewhere clearly, rather
        /// than left to inference and guesswork?.  And if a command does not
        /// attempt to maintain its prepared state, is this not failing to
        /// provide a side-effect free programming envronment, violating one
        /// of the most primary system programming precepts? Also, what does
        /// it mean to assign to a <see cref="DbCommand"/> a 
        /// <see cref="DbTransaction"/> instance that was not obtained from
        /// the command object's <see cref="Connection"/>.  And what does it
        /// mean to assign a different connection after a transaction derived
        /// from that connection has been assigned? Nowhere, it seems, are the
        /// answers to these questions documented, yet these undeniably present
        /// and troubling unresolved issues leak out as a natural consequence
        /// of a command's parent connection and transaction being designed as
        /// writable properties, presumably choices that were deemed to be
        /// required as part of moving to a disconnected data access API,
        /// without adequately considering or at least adequately documenting
        /// the wider implications.
        /// </para>
        /// <para>
        /// Sadly, the list of design descisions that lead to such consequences is
        /// still quite large in ADO.NET 2.0, and is certainly too large to warrant
        /// enumerating further here.
        /// </para>
        /// <para>
        /// In an ideal world, on the other hand, each object instance that
        /// can be obtained from a <c>DbProviderFactory</c> would expose a
        /// property whose value is the <c>DbProviderFactory</c> instance
        /// from which it was obtained.
        /// </para>
        /// <para>
        /// Certainly this would at least neatly solve the problem of how to
        /// develop future class libraries that can deal directly with objects
        /// obtained through a <c>DbProviderFactory</c> instance, since it
        /// transparently provides the opportunity to reaquire the factory
        /// in order to subsequently produce ADO.NET object instances whose
        /// runtime type is compatible with that of any object previously
        /// obtained through the <c>DbProviderFactory</c> instance.
        /// </para>
        /// <para>
        /// As a side benefit, this would clear up a remaining problem,
        /// which is that even the 2.0 design does not yet allow for
        /// completely context-free handling of ADO.NET object instances
        /// in the event that an application must connect to multiple data
        /// sources which vary by provider type.  That is, there is no
        /// allowance yet made for obtaining a compatible provider factory
        /// instance, given some arbitrary ADO.NET object instance. As such,
        /// either an application must be carefully partitioned into modules
        /// that access only instances of a single kind of data source at a
        /// time, or it must retrict itself to accessing only data sources
        /// exposed through a single, application-scope factory, or the
        /// application code must resort to invoking the
        /// <c>IDbConnection.CreateCommand()</c> and
        /// <c>IDbCommand.CreateParameter()</c> methods, which pretty much
        /// invalidates the usefulness or even the presence of the
        /// <c>DbProviderFactory.CreateCommand()</c> and 
        /// <c>DbProviderFactory.CreateParameter</c> methods.
        /// </para>
        /// <para>
        /// In the real world, sadly, the idealistic solution described above
        /// does nothing to help legacy class libraries, as the described scheme
        /// requires existing <c>System.Data</c> interfaces to be retrofitted
        /// with the factory property, meaning that all affected legacy class
        /// libraries would need to be updated (that is, at least
        /// partially rewritten, gasp!) and then redeployed.
        /// </para>
        /// </remarks>
        /// <returns>
        /// A new <see cref="HsqlParameter"/> object.
        /// </returns>
        public new HsqlParameter CreateParameter()
        {
            return new HsqlParameter();
        }

        #endregion

        #region Dispose(bool)
        /// <summary>
        /// Releases the unmanaged resources used by this object and
        /// optionally releases the managed resources.
        /// </summary>
        /// <remarks>
        /// When invoked, if this object is <see cref="Prepare()"/>d, and
        /// <c>disposing</c> is <c>true</c> to indicate disposal of
        /// managed resources is requested, then this has the side-effect
        /// of effectively invoking <see cref="UnPrepare()"/>, which,
        /// if successful, closes the underlying compiled statement,
        /// releasing both any local resources, as well as any remote reources
        /// allocated to its representation and management.
        /// </remarks>
        /// <param name="disposing">
        /// <c>true</c> to release both managed and unmanaged resources; 
        /// <c>false</c> to release only unmanaged resources.
        /// </param>
        /// <exception cref="HsqlDataSourceException">
        /// If disposal requires invalidation of the prepared
        /// state of this command, then any exception raised as
        /// a result of attempting to close the underlying compiled
        /// statement is rethrown here.
        /// </exception>
        protected override void Dispose(bool disposing)
        {
            if (disposing)
            {
                InvalidateStatement();
            }
            
            base.Dispose(disposing);
        }
        #endregion

        #region ExecuteDbDataReader(CommandBehavior)

        /// <summary>
        /// Executes this command against its connection.
        /// </summary>
        /// <remarks>
        /// Simply delegates to 
        /// <see cref="HsqlCommand.ExecuteReader(CommandBehavior)"/>.
        /// </remarks>
        /// <param name="behavior">
        /// Specifies a number of behavioral constraints upon the execution.
        /// </param>
        /// <returns>
        /// The result of execution as an <see cref="HsqlDataReader"/>.
        /// </returns>
        /// <exception cref="InvalidOperationException">
        /// When the <see cref="Connection"/> is not set.
        /// -or- 
        /// when the <see cref="Connection"/> is not <see cref="HsqlConnection.Open()"/>.
        /// </exception>
        /// <exception cref="HsqlDataSourceException">
        /// If a database access error occurs; the <see cref="CommandText"/>
        /// contains a syntax error or violates some other constraint on
        /// well-formedness; the <c>CommandText</c> does not meet the
        /// requirments imposed by the current <see cref="CommandType"/>
        /// value.
        /// </exception>
        /// <seealso cref="HsqlCommand.ExecuteReader(CommandBehavior)"/>
        protected override DbDataReader ExecuteDbDataReader(
            CommandBehavior behavior)
        {
            return ExecuteReader(behavior);
        }

        #endregion

        #region ExecuteReader()

        /// <summary>
        /// Executes this command against its connection using
        /// the <see cref="CommandBehavior.Default"/> 
        /// <c>CommandBehavior</c>.
        /// </summary>
        /// <returns>An <see cref="HsqlDataReader"/>.</returns>
        /// <exception cref="InvalidOperationException">
        /// When the <see cref="Connection"/> is not set.
        /// -or- 
        /// when the <see cref="Connection"/> is not <see cref="HsqlConnection.Open()"/>.
        /// </exception>
        /// <exception cref="HsqlDataSourceException">
        /// If a database access error occurs; the <see cref="CommandText"/>
        /// contains a syntax error or violates some other constraint on
        /// well-formedness; the <c>CommandText</c> does not meet the
        /// requirments imposed by the current <see cref="CommandType"/>
        /// value.
        /// </exception>
        /// <seealso cref="HsqlCommand.ExecuteReader(CommandBehavior)"/>
        public new HsqlDataReader ExecuteReader()
        {
            lock (SyncRoot)
            {
                HsqlDataReader reader = ExecuteReaderInternal(CommandBehavior.Default);

                OnStatementCompleted(reader.RecordsAffected);

                return reader;
            }
        }

        #endregion

        #region ExecuteReader(CommandBehavior)

        /// <summary>
        /// Using the given <see cref="CommandBehavior"/>, executes this
        /// command against its connection, returning a data reader to
        /// respresent the result.
        /// </summary>
        /// <remarks>
        /// <para>
        /// The current <c>HsqlCommand</c> implementation does not necessarily
        /// support or follow to the letter the documented terms of every behavioral
        /// constraint enumerated in <see cref="CommandBehavior"/>.
        /// </para>
        /// <para>
        /// The following list describes the current state of affairs:
        /// </para>
        /// <list type="table">
        /// <listheader>
        /// <term>Command Behavior</term>
        /// <description>Level Of Support</description>
        /// </listheader>
        /// <item><term><see cref="CommandBehavior.CloseConnection"/></term>
        /// <description>
        /// <para>
        /// When it is closed, the resulting <c>HsqlDataReader</c> also
        /// attempts to close its originating <c>HsqlConnection</c> object. 
        /// </para>
        /// <para>
        /// Note that the originating <c>HsqlConnection</c> object is the one
        /// that is associated with this command at the time of execution, not
        /// neccessarily the one that is associated with this command at the
        /// time the resulting <c>HsqlDataReader</c> is closed.  Also note
        /// that the attempt to close the originating <c>HsqlConnection</c>
        /// object is made regardless of whether it has been closed and
        /// reopened any number of times subsequent to the time of
        /// execution.
        /// </para>
        /// </description>
        /// </item>
        /// <item><term><see cref="CommandBehavior.Default"/></term>
        /// <description>
        /// <para>
        /// Contrary to documentation for <c>CommandBehavior.Default</c>,
        /// standard HSQLDB command execution presently generates at most one
        /// top-level result set, although it may be possible to retrieve
        /// records of the result set themselves as data reader objects, and
        /// so on, to arbitrary depth.  As such, <c>CommandBehavior.Default</c>
        /// is currently equivalent to <c>CommandBehavior.SingleResult</c>.
        /// </para>
        /// </description>
        /// </item>
        /// <item><term><see cref="CommandBehavior.KeyInfo"/></term>
        /// <description>
        /// <para>
        /// Requests that primary key information is included in the column
        /// metadata retrieved by invoking 
        /// <see cref="HsqlDataReader.GetSchemaTable()"/> upon the returned
        /// data reader instance.
        /// </para>
        /// <para>
        /// This is fully supported, but requesting it implies some overhead, as
        /// described in detail below.
        /// </para>
        /// <para>
        /// It is quite plausible to characterize the presence of this
        /// behavioural flag as a 'kludge' to compensate for the differences
        /// between the disconnected (<c>DataSet</c>/<c>DataAdapter</c>)
        /// approach taken by ADO.NET 2.0 and the connected (i.e. updatable
        /// result set) approach taken by a number of the underlying legacy
        /// technologies for which ADO.NET 2.0 data provider adapters exist.
        /// In particular, the ODBC (hence JDBC) and OLEDB APIs are defined
        /// in such a way that primary key information is typically not
        /// directly available when using the standard API routines to retieve
        /// query result column metadata. Hence, depending on the ADO.NET 2.0
        /// data provider, inclusion of primary key information when invoking
        /// <see cref="DbDataReader.GetSchemaTable()"/> may imply a performance
        /// overhead, in that multiple round-trips to the back-end may be
        /// required. Currently, this is precisely the case for the HSQLDB
        /// 1.8.0 ADO.NET 2.0 data provider implementation. 
        /// </para>
        /// </description>        
        /// </item>
        /// <item><term><see cref="CommandBehavior.SchemaOnly"/></term>
        /// <description>
        /// <para>Indicates that client interest extends only as far as
        /// invoking <see cref="HsqlDataReader.GetSchemaTable()"/>
        /// upon the returned data reader to determine the structure
        /// of the result that would be generated if the command
        /// were actually executed.
        /// </para>
        /// <para>
        /// This is fully supported.
        /// </para>
        /// <para>
        /// When this flag is set, this command is simply
        /// <see cref="Prepare()"/>d rather than actually executed.
        /// As such, while it is subsequently possible to invoke 
        /// <see cref="HsqlDataReader.GetSchemaTable()"/> to describe
        /// the expected column metadata of the result of execution, the
        /// returned data reader also accurately reports that, since no
        /// execution actually occurred, it has no data rows and that
        /// zero records have been affected.
        /// </para>
        /// </description>
        /// </item>
        /// <item><term><see cref="CommandBehavior.SequentialAccess"/></term>
        /// <description>
        /// <para>
        /// Currently Ignored.
        /// </para> 
        /// <para>
        /// The present HSQLDB command execution result transport mechanism
        /// retrieves a snapshot of all result rows as part of the execute
        /// call, before the representative data reader is returned to the
        /// caller.
        /// </para>
        /// <para>
        /// When set, indicates the intent that result set rows will contain
        /// columns with large binary or large character values. Behviourally,
        /// this indicates that, rather than loading an entire row at a time,
        /// the returned data reader should load the data for each field
        /// individually, on-demand and precisely in the manner and order
        /// that it is requested. Because data may be delivered across the
        /// network on a single connection via a single stream, it also
        /// indicates to the client that the most efficent data access pattern
        /// is likely to be, as the name suggests, strictly sequential access,
        /// where each field of each row is accessed strictly by ascending
        /// ordinal and that, similarly, chunks of data within each large binary
        /// or large character field are accessed strictly in ascending order
        /// within the containing value. Although this behaviour may actually
        /// be quite inefficient (overly chatty) when most field values are
        /// small, it certainly affords the returned data reader the opportunity
        /// to minimize the resources consumed when the primary modes of field
        /// access are <see cref="HsqlDataReader.GetBytes(int,long,byte[],int,int)">
        /// HsqlDataReader.GetBytes</see> and
        /// <see cref="HsqlDataReader.GetChars(int,long,char[],int,int)">
        /// HsqlDataReader.GetChars</see>.
        /// </para>
        /// </description>
        /// </item>
        /// <item><term><see cref="CommandBehavior.SingleResult"/></term>
        /// <description>
        /// <para>Currently Ignored.</para>
        /// <para>
        /// Standard HSQLDB command execution presently generates at most one
        /// top-level result set, although it may be possible to retrieve
        /// records of the result set themselves as data reader objects, and
        /// so on, to arbitrary depth.  As such, <c>CommandBehavior.SingleResult</c>
        /// is currently equivalent to <c>CommandBehavior.Default</c>.
        /// </para>
        /// </description>
        /// </item>
        /// <item><term><see cref="CommandBehavior.SingleRow"/></term>
        /// <description>
        /// <para>
        /// Indicates that each generated result set is expected to contain no
        /// more than a single row and hence is to be restricted to contain no
        /// more than a single row. Also indicates that a data provider may
        /// optionally use this information to optimize execution and fetch
        /// performance.
        /// </para>
        /// </description>
        /// </item>
        /// </list>
        /// </remarks>
        /// <param name="behavior">
        /// Specifies a number of behavioral constraints upon the execution.
        /// </param>
        /// <returns>
        /// The result of execution as a <see cref="HsqlDataReader"/>.
        /// </returns>
        /// <exception cref="InvalidOperationException">
        /// When the <see cref="Connection"/> is not set.
        /// -or- 
        /// when the <see cref="Connection"/> is not <see cref="HsqlConnection.Open()"/>.
        /// </exception>
        /// <exception cref="HsqlDataSourceException">
        /// If a database access error occurs; the <see cref="CommandText"/>
        /// contains a syntax error or violates some other constraint on
        /// well-formedness; the <c>CommandText</c> does not meet the
        /// requirments imposed by the current <see cref="CommandType"/>
        /// value.
        /// </exception>
        public new HsqlDataReader ExecuteReader(CommandBehavior behavior)
        {
            lock (SyncRoot)
            {
                HsqlDataReader reader = ExecuteReaderInternal(behavior);
                
                OnStatementCompleted(reader.RecordsAffected);
                
                return reader;
            }
        }

        #endregion

        #region ExecuteNonQuery()

        /// <summary>
        /// Executes this command against its connection with the assumption
        /// that the command does not generate any result that is more
        /// suited to representation via an <c>HsqlDatatReader</c> than it
        /// is suited to being represented solely by a returned update count.
        /// </summary>
        /// <returns>
        /// The number of rows affected by the execution; 
        /// -1 if a query was executed.
        /// </returns>
        /// <exception cref="InvalidOperationException">
        /// When the <see cref="Connection"/> is not set.
        /// -or- 
        /// when the <see cref="Connection"/> is not <see cref="HsqlConnection.Open()"/>.
        /// </exception>
        /// <exception cref="HsqlDataSourceException">
        /// If a database access error occurs; the <see cref="CommandText"/>
        /// contains a syntax error or violates some other constraint on
        /// well-formedness; the <c>CommandText</c> does not meet the
        /// requirments imposed by the current <see cref="CommandType"/>
        /// value.
        /// </exception>
        public override int ExecuteNonQuery()
        {
            lock (SyncRoot)
            {
                int rowsArrected = ExecuteNonQueryInternal();

                OnStatementCompleted(rowsArrected);

                return rowsArrected;
            }
        }

        #endregion

        #region ExecuteScalar

        /// <summary>
        /// Executes the routine or query determined by the 
        /// <see cref="CommandText"/> and <see cref="CommandType"/>
        /// and retrieves, in order of precedence, either an SQL-invoked
        /// routine's declared or implied return value, the value of an
        /// output parameter implicitly or explicity bound to the routine
        /// return value role or, when no declared or implied return value
        /// or output-parameter-value-to-return-value binding is encountered,
        /// then the value of the first column of the first row in the initial
        /// result set (should execution actually generate at least one, 
        /// initial result set), else <c>null</c>.
        /// </summary>
        /// <remarks>
        /// <para>
        /// Note that, in comparison to <c>ExecuteReader(...)</c>,
        /// the code required fetch a single value is substantially reduced.
        /// Note further that this also avoids the necessity of creating
        /// and subsequently closing or disposing an <see cref="HsqlDataReader"/>
        /// instance. Finally note that selection an optimized execution and/or
        /// fetch strategy may be possible when it is known that only a single
        /// value must be retrieved.
        /// </para>
        /// <para>
        /// For the reasons listed above, this method is absolutely the most
        /// efficient way to retrieve a single value from an HSQLDB database
        /// instance.
        /// </para>
        /// </remarks>
        /// <returns>
        /// A single, scalar value, obtained as described in the summary above.
        /// </returns>
        /// <exception cref="InvalidOperationException">
        /// When the <see cref="Connection"/> is not set.
        /// -or- 
        /// when the <see cref="Connection"/> is not <see cref="HsqlConnection.Open()"/>.
        /// </exception>
        /// <exception cref="HsqlDataSourceException">
        /// If a database access error occurs; the <see cref="CommandText"/>
        /// contains a syntax error or violates some other constraint on
        /// well-formedness; the <c>CommandText</c> does not meet the
        /// requirments imposed by the current <see cref="CommandType"/>
        /// value.
        /// </exception>
        public override object ExecuteScalar()
        {
            lock (SyncRoot)
            {
                object result = ExecuteScalarInternal();

                OnStatementCompleted(0);

                return result;
            }
        }

        #endregion

        #region Prepare

        /// <summary>
        /// Creates a prepared (or compiled) version of this command on the
        /// data source.
        /// </summary>
        /// <remarks>
        /// <para>
        /// If this command is short-running (e.g. involves only a small
        /// number of rows) and is to be (re)executed a relatively large
        /// number of times (more than once or twice), preparation may result
        /// in significant performance gains
        /// </para>
        /// <para>
        /// For example, based on varying JVM heap and garbage collection
        /// settings, benchmark software settings and database instance 
        /// settings, the Java version of the engine has been benchmarked
        /// at between 160% to 600% faster for OLTP-style primary-key
        /// predicated single row access to an embedded data source.
        /// </para>
        /// <para>
        /// Similar gains for network access may be expected
        /// only when performing batch execution or bulk copy. This is due to
        /// network round trip latency, which otherwise typically adds at least
        /// one or two orders of magnitude to the time taken to perform each
        /// short-running parse/execute/fetch cycle.
        /// </para>
        /// <para>
        /// Of course, the actual average speedup that can be reasonably
        /// expected from exclusive use of prepared commands depends
        /// on the relative overhead of re-parsing command text at each
        /// execution.
        /// </para>
        /// <para>
        /// And this is governed by the relative cost of static binding
        /// under parameterization, which unavoidably involves the high
        /// expense of first encoding parameter values to SQL literal 
        /// character sequences, replacing the parameter markers found in
        /// the command text with the corresponding SQL literal values,
        /// transmitting the resulting statically bound command text to
        /// the database engine and finally reparsing to derive the
        /// command execution plan and also to recapture the binary versions
        /// of the data values embedded in the command text.
        /// </para>
        /// <para>
        /// Other factors that may affect the measured relative overhead of
        /// re-parsing command text include the use of different table
        /// persistence engines (<c>Memory</c>, <c>Text</c>, <c>Cached</c>),
        /// database operation modes (<c>File</c>, <c>Mem</c> and <c>Res</c>)
        /// and other configuration settings, such as whether NIO file access
        /// is enabled.
        /// </para>
        /// <para>
        /// In general, if parsing and/or static binding dominates the time to
        /// execute a command, and especially if such a command is to be
        /// executed regularly, then it is best practice to prepare it
        /// before execution.
        /// </para>
        /// <para>
        /// On the other hand, if a command is long-running (e.g. is
        /// computationally complex, involves a large number of rows
        /// and/or involves time consuming disk access), then little 
        /// or no performance difference should be expected between
        /// prepared and unprepared execution. And in some less common
        /// cases, performace may actually be significantly better if
        /// preparation is *not* performed, for instance because the engine
        /// may be able to create a better plan when the values of all
        /// condition expressions are statically bound at parse time and
        /// the bound values lend themselves to the task.
        /// </para>
        /// <para>
        /// As hinted in the previous description of static binding
        /// overhead, the major exception to the long-running command
        /// rule-of-thumb is when inserting or updating large binary,
        /// character, numeric or decimal values, where parameterization
        /// combined with preparation should always be preferred, even in
        /// the case whera a command will only be executed once.
        /// </para>
        /// <para>
        /// This is because parameterization combined with preparation avoids
        /// the massive overhead required to produce a statically bound UTF16
        /// representation of the command together with its parameter values,
        /// parse the resulting command, convert large value tokens to internal
        /// binary representation and possibly back again to character sequence
        /// representation in order to record changes in the database transaction
        /// log.
        /// </para>
        /// <para>
        /// Indeed, without any consideration to the inefficiency of
        /// static binding in terms CPU usage, under parameterization, 
        /// one must take into consideration that it may require allocation
        /// of temporary buffers totalling many times the memory consumed by
        /// the in-memory, native binary form of the parameter values
        /// themselves.
        /// </para>
        /// <para>
        /// For example, it is easy to argue that inserting or updating a
        /// single 4 MB BINARY field value in an embedded database instance
        /// using an unprepared parameterized command may easily require
        /// temporary local memory allocation of up to 26 (or more) times
        /// that used just to represent the value itself (i.e. 104 MB or more).
        /// </para>
        /// <para>
        /// First, in both .Net or Java, the most common way to submit
        /// character sequence data across an API boundary is via the immutable
        /// <c>System.String</c> (or <c>java.lang.String</c>).  And under this
        /// constaint, to submit a statically bound SQL character sequence
        /// representing the insert or update, each byte of the BINARY value
        /// must be encoded to UTF16 hexadecimal form.  That is, each octet
        /// must be converted to two UTF16 characters, yielding 4 + (4 * 2
        /// characers * 2 bytes per character) = (4 + 16) =  20 MB, which
        /// is already bordering on unacceptable.
        /// </para>
        /// <para>
        /// Second, if dynamically resized character buffers are used to
        /// build strings while traveling the SQL processing pipeline (e.g.
        /// using <c>System.Text.StringBuilder</c> or <c>java.util.StringBuffer</c>
        /// objects), then in the worst case there may be over-allocation by
        /// a factor of two each time a string object is built.  If this
        /// occurs no more than once, precisely at this stage in the pipeline,
        /// this yeilds 4 + 2*16 = 36 MB, or a terrifying 9 times that of the
        /// raw value.
        /// </para>
        /// <para>
        /// Third, in the worst case, when the resulting SQL character sequence
        /// is submitted to the engine for execution, it may be copied to a
        /// raw character array in one pass (e.g. for faster character-at-a-time
        /// tokenizing), yielding 4 + 2*32 = 68 MB.
        /// </para>
        /// <para>
        /// Fourth, again in the worst case, the token representing the BINARY
        /// literal may be extracted as a UTF16 hexadecimal string, yeilding
        /// 4 + 2*32 + 16 = 84 MB.
        /// </para>
        /// <para>
        /// Fifth, the BINARY literal token typically must be converted to its
        /// native binary representation to store in the table, yeilding
        /// 4 + 2*32 + 16 + 4 = 88 MB.
        /// </para>
        /// <para>
        /// Finally, the native binary representation of the value may be
        /// converted back to a hexadecimal character sequence in a lower
        /// layer in order to record the insert or update in the transaction
        /// log. If this is done in one pass (again, the worst case), we get
        /// 4 + 2*32 + 16 + 4 + 16 = 104MB.
        /// </para>
        /// <para>
        /// Of course this is a somewhat overstated argument; it is highly
        /// unlikely that worst case over-allocation occurs or that an SQL
        /// processing pipeline implements every worst case algorithm described
        /// above.
        /// </para>
        /// <para>
        /// However, the argument does drive the point home: unless an SQL
        /// execution pipeline adheres strictly to end-to-end UTF-8 string
        /// encoding and/or streaming patterns aimed at eliminating superfluous
        /// intermediate memory allocation, using unprepared commands with large
        /// parameter values is very likely to cause significant memory and CPU
        /// load internal to the database engine.
        /// </para>
        /// <para>
        /// And regardless of back-end design or implementation, passing large
        /// parameter values via static  binding to command text within the ADO.NET
        /// data provider implementation or, much worse, developers being forced to
        /// handle the same task in client code, inevitably leads to excessive memory
        /// and CPU pressure conditions in comparison to using prepared commands.
        /// </para>
        /// </remarks>
        /// <exception cref="InvalidOperationException">
        /// When the <see cref="Connection"/> is not set.
        /// -or- 
        /// when the <see cref="Connection"/> is not <see cref="HsqlConnection.Open()"/>.
        /// </exception>
        /// <exception cref="HsqlDataSourceException">
        /// If a database access error occurs; the <see cref="CommandText"/>
        /// contains a syntax error or violates some other constraint on
        /// well-formedness; the <c>CommandText</c> does not meet the
        /// requirments imposed by the current <see cref="CommandType"/>
        /// value.
        /// </exception>
        public override void Prepare()
        {
            lock (SyncRoot)
            {
                PrepareInternal();
            }
        }

        #endregion

        #endregion

        #region Public Instance Properties

        #region Connection

        /// <summary>
        /// The <c>HsqlConnection</c> used to execute this command.
        /// </summary>
        /// <remarks>
        /// If this object is <see cref="Prepare"/>d and the
        /// new <c>Connection</c> is distinct from the current
        /// <c>Connection</c>, then actions equivalent to invoking
        /// <see cref="UnPrepare()"/> are first performed using the
        /// current <c>Connection</c>, in order to release the
        /// underlying compiled statement, before such a task is
        /// made otherwise impossible by replacing by current
        /// <c>Connection</c> with the new one.
        /// </remarks>
        /// <value>
        /// Represents a physical connection to the underlying data source
        /// together with a specific logical session context in which SQL
        /// actions take place.
        /// </value>
        /// <exception cref="HsqlDataSourceException">
        /// If assignment of a new value invalidates the prepared
        /// state of this command, then any exception raised as
        /// a result is rethrown here.
        /// </exception>
        [Category("Data")]
        [Description("Connection used to execute this command")]
        [DefaultValue(null)]
        [Editor("Microsoft.VSDesigner.Data.Design.DbConnectionEditor, Microsoft.VSDesigner, Version=8.0.0.0, Culture=neutral, PublicKeyToken=b03f5f7f11d50a3a", "System.Drawing.Design.UITypeEditor, System.Drawing, Version=2.0.0.0, Culture=neutral, PublicKeyToken=b03f5f7f11d50a3a")]
        public new HsqlConnection Connection
        {
            get { return m_dbConnection; }
            set
            {
                if (m_dbConnection == value)
                {
                    return;
                }

                InvalidateStatement();

                m_dbTransaction = null;

                if (m_dbConnection != null)
                {
                    m_dbConnection.StateChange -= ConnectionStateChanged;
                }

                if (value != null)
                {
                    value.StateChange += ConnectionStateChanged;
                }

                m_dbConnection = value;
            }
        }

        #endregion

        #region CommandText

        /// <summary>
        /// Specifies the text of the command to run against the data source.
        /// </summary>
        /// <value>
        /// The text of the command to execute.
        /// </value>
        /// <remarks>
        /// <para>
        /// The default value is an empty string ("").
        /// </para>
        /// <para>
        /// Note that setting a new value has a side effect
        /// equivalent to invoking <see cref="ClearBatch()"/>
        /// and <see cref="UnPrepare()"/>.
        /// </para>
        /// </remarks>
        /// <exception cref="HsqlDataSourceException">
        /// If assignment of a new value invalidates the prepared
        /// state of this command, which effectively invokes 
        /// <see cref="UnPrepare()"/>, then any exception raised
        /// as a result is rethrown here.
        /// </exception>
        [Category("Data")]
        [Description("Command text to execute")]
        [DefaultValue("")]
        [RefreshProperties(RefreshProperties.All)]
        public override String CommandText
        {
            get { return m_commandText; }
            set
            {
                if (value == null)
                {
                    value = string.Empty;
                }

                if (m_commandText != value)
                {
                    InvalidateStatement();

                    m_commandText = value;
                    // Not a perfect optimization, but behavior
                    // is not incorrect under false positives,
                    // so a perfect test is not required and a better
                    // (but slow / complex) test defeats the purpose
                    // of introducing an optimization in the first
                    // place.
                    m_commandTextHasParameters = 
                        (value.IndexOfAny(s_parameterChars) >= 0);
                }
            }
        }

        #endregion

        #region CommandTimeout

        /// <summary>
        /// Specifies the time to wait before terminating an 
        /// in-progress attempt to execute this command and 
        /// generating an error.
        /// </summary>
        /// <remarks>
        /// Although stored and retrieved accurately, the value is
        /// presently ignored; attempts to execute this command are
        /// never terminated in response to elapsed execution time
        /// exceeding the command timeout value.
        /// </remarks>
        /// <value>
        /// The time in seconds to wait for this command to execute;
        /// 30 by default.
        /// </value>
        /// <exception cref="ArgumentException">
        /// When an attempt is made to set a value that is less than zero (0).
        /// </exception>
        [Category("Data")]
        [Description("Time to wait for the command to execute")]
        [DefaultValue(30)]
        public override int CommandTimeout
        {
            get { return m_commandTimeout; }
            set
            {
                if (value < 0)
                {
                    throw new ArgumentException(string.Format(
                        "Invalid Command Timeout: {0}", 
                        value),
                        "value");
                }
                if (value != m_commandTimeout)
                {
                    // onProperyChanging();
                    m_commandTimeout = value;
                }
            }
        }

        #endregion

        #region CommandType

        /// <summary>
        /// Specifies how <see cref="CommandText"/> is interpreted.
        /// </summary>
        /// <remarks>
        /// Rather than raise an <see cref="ArgumentException"/>,
        /// assignment of an unsupported value results in a silent
        /// conversion to <see cref="System.Data.CommandType.Text"/>.
        /// </remarks>
        /// <value>
        /// One of the <see cref="CommandType"/> values.
        /// The default is <c>CommandType.Text</c>.
        /// </value>
        /// <exception cref="HsqlDataSourceException">
        /// If assignment of a new value invalidates the prepared
        /// state of this command, which effectively invokes 
        /// <see cref="UnPrepare()"/>, then any exception raised
        /// as a result is rethrown here.
        /// </exception>
        [Category("Data")]
        [DefaultValue(CommandType.Text)]
        [Description("Specifies how the CommandText is interpreted.")]
        [RefreshProperties(RefreshProperties.All)]
        public override CommandType CommandType
        {
            get { return m_commandType; }
            set
            {
                if (value != m_commandType)
                {
                    InvalidateStatement();

                    m_commandType = HsqlCommand.ToSupportedCommandType(value, this);
                }
            }
        }

        #endregion

        #region DesignTimeVisible

        /// <summary>
        /// Hidden property used by the designers.
        /// </summary>
        /// <value>
        /// Is used internally to support the designers
        /// and is not intended to be used in code.
        /// </value>        
        [Browsable(false)]
        [DefaultValue(true)]
        [DesignOnly(true)]
        [EditorBrowsable(EditorBrowsableState.Never)]
        public override bool DesignTimeVisible
        {
            get { return m_designTimeVisible; }
            set { m_designTimeVisible = value; }
        }

        #endregion

        #region Parameters

        /// <summary>
        /// Gets the collection of <see cref="HsqlParameter"/>
        /// objects associated with this object.
        /// </summary>
        /// <value>
        /// The parameters of the SQL statement or stored procedure call.
        /// </value>
        [Category("Data")]
        [Description("The parameters collection")]
        [DesignerSerializationVisibility(DesignerSerializationVisibility.Content)]
        public new HsqlParameterCollection Parameters
        {
            get
            {
                if (m_dbParameterCollection == null)
                {
                    m_dbParameterCollection
                        = new HsqlParameterCollection();
                }

                return m_dbParameterCollection;
            }
        }

        #endregion

        #region Transaction

        /// <summary>
        /// Specifies the database transaction within which this command
        /// executes.
        /// </summary>
        /// <value>
        /// The database transaction within which this command executes.
        /// The default value is a <c>null</c> reference (Nothing in 
        /// Visual Basic).
        /// </value>
        [Browsable(false)]
        [DefaultValue(null)]
        [Description("The transaction used by the command.")]
        [DesignerSerializationVisibility(DesignerSerializationVisibility.Hidden)]
        [EditorBrowsable(EditorBrowsableState.Never)]
        public new HsqlTransaction Transaction
        {
            get
            {
                return (m_dbTransaction != null &&
                    m_dbTransaction.Connection != null) ? m_dbTransaction : null;
            }
            set
            {
                if (m_dbTransaction != value)
                {
                    m_dbTransaction = value;
                }
            }
        }

        #endregion

        #region UpdatedRowSource

        /// <summary>
        /// Specifies how command results are applied to
        /// a <see cref="DataRow"/> when used by the
        /// Update method of a <see cref="DbDataAdapter"/>.
        /// </summary>
        /// <value>
        /// One of the <see cref="UpdateRowSource"></see> values.
        /// The default is <c>Both</c> unless the command is
        /// automatically generated, in which case the default
        /// is <c>None</c>.
        /// </value>
        /// <exception cref="ArgumentOutOfRangeException">
        /// When setting a new value and it does not match
        /// one of the <c>UpdateRowSource</c> values: {<c>None</c>, 
        /// <c>OutputParameters</c>, <c>FirstReturnedRecord</c>, 
        /// <c>Both</c>}.
        /// </exception>
        [Category("Update")]
        [DefaultValue(UpdateRowSource.Both)]
        [Description("When used by a DataAdapter.Update, denotes how command results are applied to the current DataRow.")]
        public override UpdateRowSource UpdatedRowSource
        {
            get { return m_updateRowSource; }
            set
            {
                switch (value)
                {
                    case UpdateRowSource.None:
                    case UpdateRowSource.OutputParameters:
                    case UpdateRowSource.FirstReturnedRecord:
                    case UpdateRowSource.Both:
                    {
                        m_updateRowSource = value;
                        break;
                    }
                    default:
                    {
                        throw new ArgumentOutOfRangeException("value", value,
                            "Invalid UpdatedRowSource enumeration value");
                    }
                }
                
            }
        }

        #endregion

        #endregion

        #region Protected Instance Property Overrides

        #region DbConnection

        /// <summary>
        /// Specifies the <c>HsqlConnection</c> used to execute this command.
        /// </summary>
        /// <value>The connection to the data source.</value>       
        protected override DbConnection DbConnection
        {
            get { return Connection; }
            set { Connection = (HsqlConnection)value; }
        }

        #endregion

        #region DbParameterCollection

        /// <summary>
        /// Gets the collection of <see cref="HsqlParameter"/> objects
        /// associated with this command.
        /// </summary>
        /// <value>
        /// The parameters of the SQL statement or stored procedure.
        /// </value>
        protected override DbParameterCollection DbParameterCollection
        {
            get { return Parameters; }
        }

        #endregion

        #region DbTransaction

        /// <summary>
        /// Specifies the <see cref="HsqlTransaction"/> within which this
        /// command executes.
        /// </summary>
        /// <value>
        /// The transaction within which this command executes.
        /// The default value is a null reference
        /// (Nothing in Visual Basic).
        /// </value>
        protected override DbTransaction DbTransaction
        {
            get { return Transaction; }
            set { Transaction = (HsqlTransaction)value; }
        }

        #endregion

        #endregion

        #endregion

        #region ICloneable Members

        #region Clone()
        /// <summary>
        /// Creates a new <b>HsqlCommand</b> object that is a copy of this instance.
        /// </summary>
        /// <returns>
        /// A new <b>HsqlCommand</b> object that is a copy of this instance.
        /// </returns>
        public HsqlCommand Clone()
        {
            return new HsqlCommand(this);
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

        #region Other Members

        #region Public Instance Methods

        #region AddBatch()
        /// <summary>
        /// Internally creates and adds an item to be executed as part of a
        /// batch update in response to invoking <see cref="ExecuteBatch()"/>
        /// </summary>
        /// <remarks>
        /// <para>
        /// Note that the 1.8.0 engine protocol API does not yet natively 
        /// support heterogeneous prepared statement batching.  Moreover, 
        /// because the HSQLDB 1.8.0 ADO.NET data provider does not yet 
        /// attempt to emulate support for this, changing the command
        /// text while a command is in the prepared state must necessarily
        /// invalidate that state. Similarly, invoking <see cref="Prepare()"/>
        /// or <see cref="UnPrepare()"/> necessarily has a side effect 
        /// equivalent to invoking <see cref="ClearBatch()"/>
        /// </para>
        /// <para>
        /// In particular, when an <c>HsqlCommand</c> <see cref="IsPrepared"/>, 
        /// the <c>Input</c> and <c>InputOutput</c> elements of the
        /// <see cref="Parameters"/> collection are converted into an
        /// array of object values which is added to an internal list
        /// for subsequent transmission and binding to a single prepared
        /// statement and the command text must be held constant across
        /// all calls to this method that are intended to register batch
        /// items to participate together in a single future invocation of
        /// <see cref="ExecuteBatch()"/>.
        /// </para>
        /// <para>
        /// When an <c>HsqlCommand</c> is *not* in a prepared state, parameter
        /// tokens in a copy of the current command text are replaced with
        /// SQL literal values derived from the corresponding elements of the
        /// current <see cref="Parameters"/> collection, and it is the resulting
        /// statically bound command text that is retained for subsequent
        /// transmission as part of a future direct SQL batch execution.
        /// </para>        
        /// </remarks>
        public void AddBatch()
        {
            lock (SyncRoot)
            {
                AddBatchInternal();
            }
        } 
        #endregion

        #region ClearBatch()
        /// <summary>
        /// Undoes the effect of any preceeding calls to <see cref="AddBatch()"/>.
        /// </summary>
        public void ClearBatch()
        {
            lock (SyncRoot)
            {
                ClearBatchInternal();
            }
        } 
        #endregion

        #region DeriveParameters()
        /// <summary>
        /// Derives the declared and/or implicit parameters for this command,
        /// replacing all objects previously added to this command's 
        /// <see cref="Parameters"/> collection.
        /// </summary>
        /// <remarks>
        /// <para>
        /// Although intended primarily for the situatiuon where
        /// <see cref="HsqlCommand.CommandType"/> is <c>StoredProcedure</c>,
        /// it is also intended that this method will eventually work correctly
        /// for the <c>Text</c> and <c>TableDirect</c> command types as well.
        /// </para>
        /// <para>
        /// At present, an exception is thrown if <c>CommandType.StoredProcedure</c>
        /// is not the current command type.
        /// </para>
        /// <para>
        /// A side-effect of this method is to <see cref="Prepare()"/>
        /// this command.  This policy is used because it is typically more
        /// efficient in the long run.  If the intent is to minimize the number
        /// of open prepared statements, simply call <see cref="UnPrepare()"/>
        /// immediately after invoking this method; otherwise, if this command
        /// is to be executed several times, then leaving it prepared will often
        /// result in better performance, both in terms of improved speed and 
        /// in reduced memory footprint.
        /// </para>
        /// </remarks>
        /// <exception cref="InvalidOperationException">
        /// When <c>CommandType.StoredProcedure</c> is not the current command
        /// type.
        /// </exception>
        public void DeriveParameters()
        {
            DeriveParametersInternal();
        }

        #endregion

        #region ExecuteBatch()
        /// <summary>
        /// submits a batch update to the underlying data source for execution.
        /// </summary>
        /// <remarks>
        /// <para>
        /// Batch elements are executed serially (at least logically) in the
        /// order in which they were added to the batch. When all of the
        /// elements in a batch execute successfully, the returned integer
        /// array contains one entry for each element in the batch.
        /// </para>
        /// <para>
        /// The entries in the array are ordered according to the order in
        /// which the elements were processed (which, again, is the same as
        /// the order in which the elements were originally added to the
        /// batch).
        /// </para>
        /// <para>
        /// An entry in the array may have the following values: 
        /// </para>
        /// <para>
        /// 1.) If the value of an array entry is greater than or equal to
        /// zero, then the batch element was processed successfully and the
        /// value is an update count indicating the number of rows in the
        /// database that were affected by the element's execution.
        /// </para>
        /// <para>
        /// 2.) A value of <see cref="HsqlBatchUpdateException.SuccessNoInfo"/>
        /// indicates that a batch element was processed without raising an
        /// exception, but that the number of affected rows is unknown or is
        /// simply not a concept applicable to processing that specific batch
        /// element.
        /// </para>
        /// <para>
        /// The internal list of batch elements is reset to empty once this
        /// method reuturns.
        /// </para>
        /// <para><strong>Handling failures during execution</strong></para>
        /// <para>
        /// An invocation of this method may or may not continue processing
        /// the remaining elements in a batch once execution of an element in
        /// a batch fails, but the policy is consistent across all calls to a
        /// specfic back end. The HSQLDB 1.8.0 back-end policy, for instance,
        /// is to discontinue processing upon encountering the first error, 
        /// but the policy used by a later release may be different.
        /// </para>
        ///<para>
        /// When the policy is to stop processing after the first failure, the 
        /// <see cref="HsqlBatchUpdateException.UpdateCounts"/> array may 
        /// contain fewer entries than there were elements in the batch. Since
        /// elements are executed in the order that they are added to the
        /// batch, if the array contains N elements, this means that the first
        /// N-1 elements in the batch were processed successfully and the last
        /// element contains a value of 
        /// <see cref="HsqlBatchUpdateException.ExecuteFailed"/>, which is 
        /// used to indicate that a batch element failed to execute
        /// successfully.
        /// </para>
        /// <para>
        /// When the policy is to continue processing in the presence of
        /// failures, the number of elements, N, in the 
        /// <see cref="HsqlBatchUpdateException.UpdateCounts"/> array is
        /// always equal to the number of elements in the batch, and each
        /// element that failed is marked by a value of 
        /// <see cref="HsqlBatchUpdateException.ExecuteFailed"/>
        /// </para>
        /// </remarks>
        /// <returns>
        /// An array whose elements indicate the number of rows in
        /// the database that were affected by the execution of the
        /// corresponding batch element.
        /// </returns>
        public int[] ExecuteBatch()
        {
            lock (SyncRoot)
            {
                return ExecuteBatchInternal();
            }
        } 
        #endregion

        #region UnPrepare()
        /// <summary>
        /// Releases any resources associated with maintaining
        /// the prepared state of this command.
        /// </summary>
        /// <remarks>
        /// After this call, <see cref="IsPrepared"/> will be <c>false</c>. 
        ///</remarks>
        /// <exception cref="HsqlDataSourceException">
        /// When a data source access error occurs.
        /// </exception>
        public void UnPrepare()
        {
            lock (SyncRoot)
            {
                if (IsPrepared)
                {
                    InvalidateStatement();
                }
            }
        }
        #endregion

        #endregion

        #region  Public Instance Properties

        #region IsPrepared

        /// <summary>
        /// Indicates whether this command is prepared.
        /// </summary>
        /// <value>
        /// <c>true</c> if this command is prepared; otherwise, <c>false</c>.
        /// </value>
        public bool IsPrepared
        {
            get { lock (SyncRoot) { return m_statement != null; } }
        }

        #endregion

        #region SyncRoot

        /// <summary>
        /// Can be used to synchronize access to this command.
        /// </summary>
        /// <remarks>
        /// It is recommended to use <c>lock(command.SyncRoot)</c>
        /// instead of <c>lock(command)</c> due to FxCop check 
        /// <c>CA2002: DoNotLockOnObjectsWithWeakIdentity (System.MarshalByRefObject)</c>.
        /// </remarks>
        /// <value>
        /// An object instance with strong identity that can be used
        /// to synchronize access to this object.
        /// </value>
        public object SyncRoot
        {
            get { return m_syncRoot; }
        }

        #endregion

        #endregion

        #endregion
    }

    #endregion
}