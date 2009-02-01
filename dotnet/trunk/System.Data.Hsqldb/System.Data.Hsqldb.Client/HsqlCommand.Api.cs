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

#if W32DESIGN
using System.ComponentModel;
using System.Drawing;
#endif

using System.Data;
using System.Data.Common;
using System.Text;
using System.Threading;

using System.Data.Hsqldb.Common.Enumeration;
using System.Data.Hsqldb.Client.Internal;
using System.Data.Hsqldb.Client.MetaData;
using System.Data.Hsqldb.Common.Sql;

using ParameterMetaData = org.hsqldb.Result.ResultMetaData;
using PMD = java.sql.ParameterMetaData.__Fields;
using Result = org.hsqldb.Result;
using ResultConstants = org.hsqldb.ResultConstants.__Fields;
using HsqlTypes = org.hsqldb.Types;
using System.Data.Hsqldb.Common;

#endregion

namespace System.Data.Hsqldb.Client
{
    #region HsqlCommand

    public sealed partial class HsqlCommand : DbCommand, ICloneable
    {
        /// <summary>Occurs when the execution of a Transact-SQL statement completes.</summary>
        /// <filterpriority>2</filterpriority>
        [Category("Data"), Description("Statement Completed Event")]
        public event StatementCompletedEventHandler StatementCompleted;
 
        #region DbCommand Members

        #region Instance Method Overrides

        #region Cancel()

        /// <summary>
        /// Attempts to cancel the execution of this command.
        /// </summary>
        /// <remarks>
        /// Currently ignored (does nothing).
        /// </remarks>
        public override void Cancel()
        {
            // throw new InvalidOperationException("Not Yet supported");
        }

        #endregion

        #region CreateDbParameter()

        /// <summary>
        /// Creates a new <see cref="HsqlParameter"/> object.
        /// </summary>
        /// <remarks>
        /// The new parameter is not initially associated with any parameter
        /// collection.
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
        /// Creates a new <see cref="HsqlParameter"/> object.
        /// </summary>
        /// <remarks>
        /// The new parameter is not initially associated with any parameter
        /// collection.
        /// </remarks>
        /// <returns>
        /// A new <see cref="HsqlParameter"/> object.
        /// </returns>
        public new HsqlParameter CreateParameter()
        {
            return new HsqlParameter();
        }

        #endregion

        // TODO:
        //protected override void Dispose(bool disposing)
        //{
        //    if (disposing)
        //    {
        //        InvalidateStatement();
        //    }
        //    base.Dispose(disposing);
        //}

        #region ExecuteDbDataReader(CommandBehavior)

        /// <summary>
        /// Executes this command against its connection.
        /// </summary>
        /// <param name="behavior">
        /// Specifies a number of behavioral constraints upon the execution.
        /// </param>
        /// <returns>
        /// The result of execution as a <see cref="HsqlDataReader"/>.
        /// </returns>
        protected override DbDataReader ExecuteDbDataReader(
            CommandBehavior behavior)
        {
            return ExecuteReader(behavior);
        }

        #endregion

        #region ExecuteReader()

        /// <summary>
        /// Executes this command against its connection using
        /// <see cref="CommandBehavior.Default"/>.
        /// </summary>
        /// <returns>An <see cref="HsqlDataReader"/>.</returns>
        public new HsqlDataReader ExecuteReader()
        {
            return ExecuteReaderInternal(CommandBehavior.Default);
        }

        #endregion

        #region ExecuteReader(CommandBehavior)

        /// <summary>
        /// Executes this command against its connection, returning a data reader.
        /// </summary>
        /// <remarks>
        /// <para>
        /// The current implementation does not necessarily support or follow
        /// to the letter the documented terms of every behavioral constraint
        /// enumerated in <see cref="CommandBehavior">
        /// System.Data.CommandBehavior</see>.
        /// </para>
        /// <para>
        /// The following list describes the current state of affairs as noted
        /// at November 12, 2007:
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
        /// invoking <see cref="DbDataReader.GetSchemaTable()"/> may imply a
        /// performance overhead, in that multiple round-trips to the back-end
        /// may be required. Currently, this is precisely the case for the
        /// HSQLDB 1.8.0.7 ADO.NET 2.0 data provider implementation. 
        /// </para>
        /// </description>        
        /// </item>
        /// <item><term><see cref="CommandBehavior.SchemaOnly"/></term>
        /// <description>
        /// <para>Indicates that client interest extends only as far as
        /// invoking <see cref="HsqlDataReader.GetSchemaTable()"/>
        /// upon the returned data reader.
        /// </para>
        /// <para>
        /// This is fully supported.
        /// </para>
        /// <para>
        /// When this flag is set, this command is simply
        /// <see cref="Prepare()"/>d rather than executed.
        /// As such, while it is subsequently possible to invoke 
        /// <see cref="HsqlDataReader.GetSchemaTable()"/> to describe
        /// the expected column metadata of the result of execution, the
        /// returned data reader accurately reports that, since no
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
        /// The present transport mechanism retrieves a snapshot of all result
        /// set rows as part of the execute call, before the representative
        /// data reader is returned.
        /// </para>
        /// <para>
        /// When set, indicates the intent that result set rows will contain
        /// columns with large binary or large character values. Behviourally,
        /// this indicates that, rather than loading an entire row at a time,
        /// the returned data reader should load the data for each field
        /// individually, on-demand and precisely in the manner that it is
        /// requested. Because data may be delivered across the network
        /// on a single connection via a single stream, it also indicates to
        /// the client that the most efficent data access pattern is likely to
        /// be, as the name suggests, strictly sequential access, where each
        /// field of each row is accessed strictly by ascending ordinal and
        /// that, similarly, chunks of data within each large binary or large
        /// character field are accessed strictly in ascending order within
        /// the containing value. Although this behaviour may actually be quite
        /// inefficient (overly chatty) when most field values are small, it
        /// certianly affords the returned data reader the opportunity to
        /// minimize the resources consumed when the primary modes of field
        /// access are <see cref="HsqlDataReader.GetBytes(int,long,byte[],int,int)">
        /// HsqlDataReader.GetBytes</see> and
        /// <see cref="HsqlDataReader.GetChars(int,long,char[],int,int)">
        /// HsqlDataReader.GetChars
        /// </see>.
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
        /// optionally use this information to optimize performance.
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
        public new HsqlDataReader ExecuteReader(CommandBehavior behavior)
        {            
            return ExecuteReaderInternal(behavior);
        }

        #endregion

        #region ExecuteNonQuery()

        /// <summary>
        /// Executes this command against its connection
        /// with the assumption that the command does
        /// generate query results.
        /// </summary>
        /// <returns>The number of rows affected.</returns>
        public override int ExecuteNonQuery()
        {
            lock (SyncRoot)
            {
                return ExecuteNonQueryInternal();
            }
        }

        #endregion

        #region ExecuteScalar

        /// <summary>
        /// Executes the query represented by this command and returns
        /// the first column of the first row in the initial result set.
        /// </summary>
        /// <remarks>
        /// All other columns and rows are ignored. If the command execution
        /// does not generate a result set or if the result set is empty,
        /// returns <c>null</c>.
        /// </remarks>
        /// <returns>
        /// The first column of the first row in the result set or <c>null</c>.
        /// </returns>
        public override object ExecuteScalar()
        {
            lock (SyncRoot)
            {
                return ExecuteScalarInternal();
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
        /// in significant performance gains (e.g. has been benchmarked at
        /// between 160% to 600% faster for OLTP-style access to an embedded
        /// data source), with actual speedup depending on the relative
        /// overhead of reparsing on each execution, as governed by different
        /// table persistence engines (<c>Memory</c>, <c>Text</c>,
        /// <c>Cached</c>), database operation modes (<c>File</c>,
        /// <c>Mem</c> and <c>Res</c>) and other configuration settings,
        /// such as whether NIO file access enabled. Similar gains for
        /// network access are experienced only when performing batch
        /// execution or bulk copy. This is due to network round trip latency,
        /// which otherwise typically adds at least one or two orders of
        /// magnitude to the time taken to perform each short-running
        /// parse/execute/fetch cycle.
        /// </para>
        /// <para>
        /// On the other hand if this command is long-running (e.g. is
        /// computationally complex, involves a large number of rows
        /// and/or involves time consuming disk access), then at worst the
        /// performance will be the same if preparation is not performed.
        /// And in some cases performace may actually be significantly better,
        /// for instance because the engine can generally create a better plan
        /// when the values of all condition expressions are statically bound
        /// at parse time.
        /// </para>
        /// <para>
        /// The major exception to the long-running command rule-of-thumb above
        /// is when inserting or updating large binary, character, numeric or
        /// decimal values (i.e. values that consume more than a few hundred bytes
        /// each).
        /// </para>
        /// <para>
        /// Although such commands may be long running when the values are
        /// very large, preparation should always be preferred because it
        /// avoids the massive overhead required to produce a statically bound
        /// UTF16 representation of the command together with its parameter
        /// values, parse the resulting command, convert large value tokens
        /// to internal binary representation and possibly back to character
        /// sequence representation in order to record changes in the
        /// transaction log; Not to mention how inefficient this is in terms
        /// CPU usage, this may require allocation of temporary buffers
        /// totalling many times the memory consumed by the parameter values
        /// themselves.
        /// </para>
        /// <para>
        /// For example, it is easy to argue that inserting or updating a
        /// single 4 MB BINARY field value in an embedded database instance
        /// using an unprepared command may easily require temporary local
        /// memory allocation of up to 26 (or more) times that used just
        /// to represent the value itself (i.e. 104 MB or more):
        /// </para>
        /// <para>
        /// First, in both .Net or Java, the most common contract choice
        /// for submission of a character sequence to an API is to require
        /// immutability, as in submission of a <c>System.String</c> or
        /// <c>java.lang.String</c> object.  If the common contract is the
        /// case, then in order to submit a statically bound SQL character
        /// sequence representing the insert or update, each byte of the
        /// BINARY value must be encoded to UTF16 hexadecimal form.  That
        /// is, each octet must be converted to two UTF16 characters, yielding
        /// 4 + (4 * 2 characers * 2 bytes per character) = (4 + 16)
        /// =  20 MB.
        /// </para>
        /// <para>
        /// Second, if dynamically resized character buffers are used to
        /// build strings while traveling the SQL processing pipeline (e.g.
        /// using <c>System.Text.StringBuilder</c> or <c>java.util.StringBuffer</c>
        /// objects), then in the worst case there may be over-allocation by
        /// a factor of two each time a string object is built.  If this
        /// occurs only once, precisely at this stage in the pipeline, this
        /// yeilds 4 + 2*16 = 36 MB.
        /// </para>
        /// <para>
        /// Third, in the worst case, when the resulting SQL character sequence
        /// is submitted to the engine for execution,  it may be copied to a
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
        /// Of course this is an overstated argument; it is highly unlikely
        /// that worst case over-allocation occurs or that an SQL processing
        /// pipeline implements every worst case algorithm described above.
        /// However, the argument does drive the point home: unless an SQL
        /// execution pipeline adheres strictly to end-to-end UTF-8 encoding
        /// and/or streaming patterns aimed at eliminating superfluous memory
        /// allocation, using unprepared commands with large parameter values
        /// is very likely to cause significant memory and CPU load.
        /// </para>
        /// </remarks>
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
        /// Gets or sets the <c>HsqlConnection</c>
        /// used to execute this command.
        /// </summary>
        /// <value>
        /// The connection to the data source.
        /// </value>
#if W32DESIGN
        [Category("Data")]
        [Description("Connection used to execute this command")]
        [DefaultValue(null)]
        [Editor("Microsoft.VSDesigner.Data.Design.DbConnectionEditor, Microsoft.VSDesigner, Version=8.0.0.0, Culture=neutral, PublicKeyToken=b03f5f7f11d50a3a", "System.Drawing.Design.UITypeEditor, System.Drawing, Version=2.0.0.0, Culture=neutral, PublicKeyToken=b03f5f7f11d50a3a")]
#endif
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
        /// The default value is an empty string ("").
        /// </remarks>
        ///
#if W32DESIGN
        [Category("Data")]
        [Description("Command text to execute")]
        [DefaultValue("")]
        [RefreshProperties(RefreshProperties.All)]
#endif
        public override String CommandText
        {
            get { return m_commandText; }
            set
            {
                if (value == null)
                {
                    // CHECKME:  throw ArgumentNullException?
                    value = string.Empty;
                }

                if (m_commandText != value)
                {

                    InvalidateStatement();

                    m_commandText = value;
                    m_commandTextHasParameters = 
                        (value.IndexOfAny(m_ParmeterChars) >= 0);
                }
            }
        }

        #endregion

        #region CommandTimeout

        /// <summary>
        /// Specifies the time to wait before terminating an attempt
        /// to execute this command and generating an error.
        /// </summary>
        /// <value>
        /// The time in seconds to wait for this command to execute.
        /// </value>
#if W32DESIGN
        [Category("Data")]
        [Description("Time to wait for the command to execute")]
        [DefaultValue(30)]
#endif
        public override int CommandTimeout
        {
            get { return m_commandTimeout; }
            set
            {
                if (value < 0)
                {
                    throw new ArgumentException(
                        "Invalid Command Timeout: " + value,
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
        /// Specifies how the <see cref="CommandText"/>
        /// property is interpreted.
        /// </summary>
        /// <value>
        /// One of the <see cref="CommandType"/> values.
        /// The default is <c>CommandType.Text</c>.
        /// </value>
#if W32DESIGN
        [Category("Data")]
        [DefaultValue(CommandType.Text)]
        [Description("Specifies how the CommandText is interpreted.")]
        [RefreshProperties(RefreshProperties.All)]
#endif
        public override CommandType CommandType
        {
            get { return m_commandType; }
            set
            {
                if (value != m_commandType)
                {
                    InvalidateStatement();

                    switch (value)
                    {
                        case CommandType.StoredProcedure:
                        case CommandType.TableDirect:
                        case CommandType.Text:
                            {
                                m_commandType = value;
                                break;
                            }
                        default:
                            {
                                m_commandType = CommandType.Text;
                                break;
                            }
                    }
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
#if W32DESIGN        
        [Browsable(false)]
        [DefaultValue(true)]
        [DesignOnly(true)]
        [EditorBrowsable(EditorBrowsableState.Never)]
#endif
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
#if W32DESIGN
        [Category("Data")]
        [Description("The parameters collection")]
        [DesignerSerializationVisibility(DesignerSerializationVisibility.Content)]
#endif
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
        /// Specifies the database transaction
        /// within which this command executes.
        /// </summary>
        /// <value>
        /// The database transaction within which
        /// this command executes. The default value
        /// is a <c>null</c> reference
        /// (Nothing in Visual Basic).
        /// </value>
#if W32DESIGN
        [Browsable(false)]
        [DefaultValue(null)]
        [Description("The transaction used by the command.")]
        [DesignerSerializationVisibility(DesignerSerializationVisibility.Hidden)]
        [EditorBrowsable(EditorBrowsableState.Never)]
#endif
        public new HsqlTransaction Transaction
        {
            get { return m_dbTransaction; }
            set { m_dbTransaction = value; }
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
                        throw new ArgumentOutOfRangeException(
                            "value", 
                            value, 
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
        /// Specifies the <c>HsqlConnection</c> used to
        /// execute this command.
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
        /// Specifies the <see cref="HsqlTransaction"/>
        /// within which command executes.
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
        /// for <c>Text</c> and <c>TableDirect</c> too.
        /// </para>
        /// <para>
        /// At present, however, an exception is thrown if
        /// <c>CommandType.StoredProcedure</c> is not the current command type.
        /// </para>
        /// <para>
        /// Currently, a side effect of this method is to
        /// <see cref="Prepare()"/> this command.  This policy is used because
        /// it is typically more efficient in the long run.  If the intent is
        /// to minimize the number of open prepared statements, simply call
        /// <see cref="UnPrepare()"/> immediately after invoking this method;
        /// otherwise, if it is to be executed several times, then leaving this
        /// command prepared is likely to result in better performance, both in
        /// terms of improved speed and reduced memory footprint.
        /// </para>
        /// </remarks>
        /// <exception cref="InvalidOperationException">
        /// When <c>CommandType.StoredProcedure</c> is not the current command
        /// type.
        /// </exception>
        public void DeriveParameters()
        {
            if (CommandType != CommandType.StoredProcedure)
            {
                throw new InvalidOperationException(string.Format(
                   "Operation not supported for CommandType: " 
                   + CommandType.ToString()));
            }

            Prepare();

            HsqlStatement l_statement = m_statement;
            ParameterMetaData pmd = l_statement.ParameterDescriptor.metaData;

            string[] parameterNames = pmd.colNames;
            int count = parameterNames.Length;

            HsqlParameter[] parameters = new HsqlParameter[count];

            for (int i = 0; i < count; i++)
            {
                string name = parameterNames[i];
                ParameterMode mode = (ParameterMode)pmd.paramMode[i];
                int type = pmd.colTypes[i];
                int precision = pmd.colSizes[i];
                int scale = pmd.colScales[i];
                int nullability = pmd.colNullable[i];

                HsqlProviderType providerType = (HsqlProviderType)type;
                DbType dbType = HsqlConvert.ToDbType(providerType);
                ParameterDirection? direction = HsqlConvert.ToParameterDirection(mode);
                bool? isNullable = IsNullable(nullability);
                bool isCharacter = IsCharacterType(type);
                bool isNumber = (!isCharacter) && IsNumberType(type);
                bool isTemporal = !(isCharacter || isNumber) && IsTemporalType(type);
                int size = (precision == 0) ? ToDefaultSize(type) : precision;

                if (isCharacter)
                {
                    precision = 0;
                    scale = 0;
                }
                else if (isNumber || isTemporal)
                {
                    if (precision == 0)
                    {
                        precision = ToDefaultPrecision(type);
                    }
                }

                HsqlParameter parameter = new HsqlParameter();

                parameter.DbType = dbType;

                if (direction != null)
                {
                    parameter.Direction = direction.Value;
                }

                if (isNullable != null)
                {
                    parameter.IsNullable = isNullable.Value;
                }

                parameter.ParameterName = name;
                parameter.Precision = (byte)Math.Min(byte.MaxValue, precision);
                parameter.ProviderType = providerType;
                parameter.Scale = (byte)Math.Min(byte.MaxValue, scale);
                parameter.Size = size;
                parameter.SourceVersion = DataRowVersion.Default;

                parameters[i] = parameter;
            }

            HsqlParameterCollection pc = Parameters;

            pc.Clear();

            foreach (HsqlParameter parameter in parameters)
            {
                pc.Add(parameter);
            }
        }
        #endregion

        #region UnPrepare()
        /// <summary>
        /// Releases any resources associated with maintaining
        /// this command object in a prepared state.
        /// </summary>
        /// <remarks>
        /// After this call, <c>IsPrepared</c> will be <c>false</c>. 
        ///</remarks>
        public void UnPrepare()
        {
            InvalidateStatement();
        }
        #endregion

        #endregion

        #region  Public Instance Properties

        #region IsPrepared

        /// <summary>
        /// Indicates whether this command is prepared.
        /// </summary>
        /// <value>
        /// <c>true</c> if this command is prepared;
        /// otherwise, <c>false</c>.
        /// </value>
        public bool IsPrepared
        {
            get { return m_statement != null; }
        }

        #endregion

        #region SyncRoot

        /// <summary>
        /// Gets an object that can be used to synchronize access to this
        /// object.
        /// </summary>
        /// <remarks>
        /// Use instead of lock(this) due to FxCop check CA2002:
        /// DoNotLockOnObjectsWithWeakIdentity (System.MarshalByRefObject)
        /// </remarks>
        /// <value>
        /// An object that can be used to synchronize access to this object.
        /// </value>
        public object SyncRoot
        {
            get
            {
                if (m_syncRoot == null)
                {
                    Interlocked.CompareExchange(ref m_syncRoot, new object(),
                        null);
                }

                return m_syncRoot;
            }
        }

        #endregion

        #endregion

        #endregion
    }

    #endregion
}