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
#if W32DESIGN
using System.ComponentModel;
using System.Data;
using System.Drawing;
#endif
using System;
using System.Data.Common;
using System.Data.Hsqldb.Client.Design;
using System.Data.Hsqldb.Client.Design.Attribute;
using System.Data.Hsqldb.Client.Internal;
#endregion

namespace System.Data.Hsqldb.Client
{
    #region HsqlRowUpdatingEventHandler
    /// <summary>
    /// Represents the method that will handle the 
    /// <see cref="E:HsqlDataAdapter.RowUpdating"/> event of
    /// a <see cref="HsqlDataAdapter"></see>.
    /// </summary>
    public delegate void HsqlRowUpdatingEventHandler(
        object sender,
        HsqlRowUpdatingEventArgs e); 
    #endregion

    #region HsqlRowUpdatedEventHandler
    /// <summary>Represents the method that will handle the 
    /// <see cref="E:HsqlDataAdapter.RowUpdated"/> event of
    /// a <see cref="HsqlDataAdapter"/>.
    /// </summary>
    public delegate void HsqlRowUpdatedEventHandler(
        object sender,
        HsqlRowUpdatedEventArgs e); 
    #endregion

    #region HsqlDataAdapter
    /// <summary>
    /// <para>
    /// The HSQLDB <see cref="DbDataAdapter">DbDataAdapter</see> implementation.
    /// </para>
    /// <img src="/ClassDiagrams/System.Data.Hsqldb.Client.HsqlDataAdapter.png"
    ///      alt="HsqlDataAdapter Class Diagram"/>
    /// </summary>
    /// <remarks>
    /// Implements a set of functions to provide strong typing,
    /// but inherits most of the functionality needed to fully
    /// implement a <c>DataAdapter</c>.
    /// </remarks>
#if W32DESIGN
    [ToolboxBitmap(typeof(resfinder), "System.Data.Hsqldb.Images.Bmp.HsqlDataAdapter.bmp")]
    [DefaultEvent("RowUpdated")]
#endif
    public sealed class HsqlDataAdapter : DbDataAdapter, IDbDataAdapter, ICloneable
    {
        #region Constants
		        private const string CommandEditorTypeName 
            = "Microsoft.VSDesigner.Data.Design.DBCommandEditor, Microsoft.VSDesigner, Version=8.0.0.0, Culture=neutral, PublicKeyToken=b03f5f7f11d50a3a";
        private const string CommandEditorBaseTypeName
            = "System.Drawing.Design.UITypeEditor, System.Drawing, Version=2.0.0.0, Culture=neutral, PublicKeyToken=b03f5f7f11d50a3a";
         
	    #endregion

        #region Fields
        // base replacements
        private HsqlCommand m_selectCommand;
        private HsqlCommand m_insertCommand;
        private HsqlCommand m_updateCommand;
        private HsqlCommand m_deleteCommand;

        // batch execution support
        private HsqlCommandSet m_commandSet;
        private int m_updateBatchSize;

        private static readonly object EventRowUpdating;
        private static readonly object EventRowUpdated;
        #endregion

        #region Static Initializer
        static HsqlDataAdapter()
        {
            EventRowUpdating = new object();
            EventRowUpdated = new object();
        } 
        #endregion

        #region Constructors
        
        #region HsqlDataAdapter()
        /// <summary>
        /// Initializes a new instance of the
        /// <see cref="HsqlDataAdapter"/> class.
        /// </summary>
        public HsqlDataAdapter() : base()
        {
            m_updateBatchSize = 1;
            GC.SuppressFinalize(this);
        }
        #endregion

        #region HsqlDataAdapter(HsqlCommand)
        /// <summary>
        /// Initializes a new instance of the
        /// <see cref="HsqlDataAdapter"/> class.
        /// </summary>
        /// <param name="selectCommand">The select command.</param>
        public HsqlDataAdapter(HsqlCommand selectCommand) : this()
        {
            this.SelectCommand = selectCommand;
        }
        #endregion

        #region HsqlDataAdapter(string,HsqlConnection)
        /// <summary>
        /// Initializes a new instance of the
        /// <see cref="HsqlDataAdapter"/> class.
        /// </summary>
        /// <param name="selectCommandText">The select command text.</param>
        /// <param name="selectConnection">The select connection.</param>
        public HsqlDataAdapter(string selectCommandText, HsqlConnection selectConnection)
            : this()
        {
            SelectCommand = new HsqlCommand(selectConnection, selectCommandText);
        }
        #endregion

        #region HsqlDataAdapter(string,string)
        /// <summary>
        /// Initializes a new instance of the
        /// <see cref="HsqlDataAdapter"/> class.
        /// </summary>
        /// <param name="selectCommandText">The select command text.</param>
        /// <param name="selectConnectionString">The select connection string.</param>
        public HsqlDataAdapter(string selectCommandText, string selectConnectionString)        
        {
            HsqlConnection connection = new HsqlConnection(selectConnectionString);
            this.SelectCommand = new HsqlCommand(connection, selectCommandText);

        }
        #endregion 

        #region HsqlDataAdapter(HsqlDataAdapter)
        /// <summary>
        /// Constructs a new <c>HsqlDataAdapter</c> instance that
        /// is a clone of the given adapter.
        /// </summary>
        /// <param name="toClone">
        /// The adapter from which to construct this adapter.
        ///</param>
        private HsqlDataAdapter(HsqlDataAdapter toClone)
            : base(toClone)
        {
            m_updateBatchSize = 1;
            GC.SuppressFinalize(this);
        }
        #endregion
        
        #endregion

        #region Public Instance Properties
        
        #region DeleteCommand
        /// <summary>
        /// Gets or sets a command for deleting records
        /// from the data set.
        /// </summary>
        /// <value>
        /// The <see cref="IDbCommand"/> used during
        /// <see cref="DbDataAdapter.Update(DataSet)"/> to delete records
        /// in the data source for deleted rows in the data set.
        /// </value>
        [Editor(CommandEditorTypeName, CommandEditorBaseTypeName)]
        [DefaultValue(null)]
        [ResDescription("DbDataAdapter_DeleteCommand")]
        [Category("Update")]
        public new HsqlCommand DeleteCommand
        {
            get { return m_deleteCommand; }
            set { m_deleteCommand = value; }
        }
        #endregion

        #region InsertCommand
        /// <summary>
        /// Gets or sets the command used to insert new records
        /// into the data source.
        /// </summary>
        /// <value>The <see cref="IDbCommand"/> used during
        /// <see cref="DbDataAdapter.Update(DataSet)"/> to insert records
        /// in the data source for new rows in the data set.
        /// </value>
        [Editor(CommandEditorTypeName, CommandEditorBaseTypeName)]
        [DefaultValue(null)]
        [Category("Update")]
        [ResDescription("DbDataAdapter_InsertCommand")]
        public new HsqlCommand InsertCommand
        {
            get { return m_insertCommand; }
            set { m_insertCommand = value; }
        }
        #endregion

        #region SelectCommand
        /// <summary>
        /// Gets or sets the command used to select records
        /// in the data source.
        /// </summary>
        /// <value>
        /// The <see cref="IDbCommand"/> that is used during
        /// <see cref="DbDataAdapter.Update(DataSet)"></see> to select records
        /// from the data source for placement in the data set.
        /// </value>
        [Category("Fill")]
        [DefaultValue(null)]
        [Description("DbDataAdapter_SelectCommand")]
        [Editor(CommandEditorTypeName, CommandEditorBaseTypeName)]
        public new HsqlCommand SelectCommand
        {
            get { return m_selectCommand; }
            set { m_selectCommand = value; }
        }
        #endregion

        #region UpdateCommand
        /// <summary>
        /// Gets or sets the command used to update records
        /// in the data source.
        /// </summary>
        /// <value>
        /// The <see cref="IDbCommand"/> used during
        /// <see cref="DbDataAdapter.Update(DataSet)"/> to update
        /// records in the data source for modified
        /// rows in the data set.
        /// </value>     
        [Category("Update")]
        [DefaultValue(null)]
        [Description("DbDataAdapter_UpdateCommand")]
        [Editor(CommandEditorTypeName, CommandEditorBaseTypeName)]
        public new HsqlCommand UpdateCommand
        {
            get { return m_updateCommand; }
            set { m_updateCommand = (HsqlCommand)value; }
        }
        #endregion 

        #region UpdateBatchSize
        /// <summary>
        /// Gets or sets a value that enables or disables batch processing
        /// support, and specifies the number of commands that can be
        /// executed in a batch.
        /// </summary>
        /// <remarks>
        /// When setting this to a value other than 1, all the commands
        /// associated with this adapter must have their 
        /// <see cref="HsqlCommand.UpdatedRowSource"/> property set to
        /// <see cref="UpdateRowSource.None"/> or
        /// <see cref="UpdateRowSource.OutputParameters"/>.
        /// An exception will be thrown otherwise.
        /// </remarks>
        /// <value>
        /// <para>The number of rows to process per batch:</para>
        /// <list type="table">
        /// <listheader>
        /// <term>Value</term><description>Description</description>
        /// </listheader>
        /// <item><term>0</term>
        /// <description>There is no limit on the batch size.</description>
        /// </item>
        /// <item><term>1</term>
        /// <description>Disables batch updating.</description>
        /// </item>
        /// <item><term>&gt; 1 </term>
        /// <description>
        /// Changes are sent using batches of <see cref="UpdateBatchSize"/>
        /// operations at a time.
        /// </description>
        /// </item>
        /// </list>
        /// </value>
        public override int UpdateBatchSize
        {
            get { return m_updateBatchSize; }
            set
            {
                if (value < 0)
                {
                    throw new ArgumentOutOfRangeException(
                        "UpdateBatchSize",
                        "Value too small: " + value.ToString());
                }

                m_updateBatchSize = value;
            }
        } 
        #endregion

        #endregion

        #region Public Events
        
        #region RowUpdating
        /// <summary>
        /// Row updating event handler
        /// </summary>
        [Description("DbDataAdapter_RowUpdated")]
        [Category("Update")]
        public event HsqlRowUpdatingEventHandler RowUpdating
        {
            add
            {
                HsqlRowUpdatingEventHandler mcd
                    = (HsqlRowUpdatingEventHandler)base.Events[EventRowUpdating];

                if ((mcd != null) && (value.Target is DbCommandBuilder))
                {
                    HsqlRowUpdatingEventHandler handler = null;

                    Delegate[] invocationList = mcd.GetInvocationList();

                    if (invocationList != null)
                    {
                        for (int i = 0; i < invocationList.Length; i++)
                        {
                            if (invocationList[i].Target is DbCommandBuilder)
                            {
                                handler = (HsqlRowUpdatingEventHandler)invocationList[i];
                                break;
                            }
                        }
                    }

                    if (handler != null)
                    {
                        base.Events.RemoveHandler(EventRowUpdating, handler);
                    }
                }
                base.Events.AddHandler(EventRowUpdating, value);
            }
            remove { base.Events.RemoveHandler(EventRowUpdating, value); }
        }
                #endregion

        #region RowUpdated
        /// <summary>
        /// Row updated event handler
        /// </summary>
        public event EventHandler<HsqlRowUpdatedEventArgs> RowUpdated
        {
            add { base.Events.AddHandler(EventRowUpdated, value); }
            remove { base.Events.RemoveHandler(EventRowUpdated, value); }
        }
        #endregion 

        #endregion

        #region DbDataAdapter Method Overrides

        #region Batch Update Support

        #region AddToBatch(IDbCommand)
        /// <summary>
        /// Adds an <see cref="IDbCommand"/> to the current batch.
        /// </summary>
        /// <param name="command">
        /// The command to add to the batch.
        /// </param>
        /// <returns>
        /// The number of commands in the batch before adding the given command.
        /// </returns>
        protected override int AddToBatch(IDbCommand command)
        {
            int commandCount = m_commandSet.CommandCount;

            m_commandSet.Append((HsqlCommand)command);

            return commandCount;
        }
        #endregion

        #region ClearBatch()
        /// <summary>
        /// Removes all command objects from the batch.
        /// </summary>
        protected override void ClearBatch()
        {
            m_commandSet.Clear();
        }
        #endregion

        #region ExecuteBatch()
        /// <summary>
        /// Executes the current batch.
        /// </summary>
        /// <returns>
        /// The return value from the last command in the batch.
        /// </returns>
        protected override int ExecuteBatch()
        {
            return m_commandSet.ExecuteNonQuery();
        }
        #endregion

        #region GetBatchedParameter(int,int)
        /// <summary>
        /// Returns a <see cref="T:System.Data.IDataParameter"></see> from one of the commands in the current batch.
        /// </summary>
        /// <param name="commandIdentifier">The index of the command to retrieve the parameter from.</param>
        /// <param name="parameterIndex">The index of the parameter within the command.</param>
        /// <returns>
        /// The <see cref="T:System.Data.IDataParameter"></see> specified.
        /// </returns>
        /// <exception cref="T:System.NotSupportedException">The adapter does not support batches. </exception>
        protected override IDataParameter GetBatchedParameter(
            int commandIdentifier,
            int parameterIndex)
        {
            return m_commandSet.GetParameter(commandIdentifier, parameterIndex);

        }
        #endregion

        #region GetBatchedRecordsAffected(int,out int,out Exception)
        /// <summary>
        /// Gets the batched records affected.
        /// </summary>
        /// <param name="commandIdentifier">The command identifier.</param>
        /// <param name="recordsAffected">The records affected.</param>
        /// <param name="error">The error.</param>
        /// <returns></returns>
        protected override bool GetBatchedRecordsAffected(
            int commandIdentifier,
            out int recordsAffected,
            out Exception error)
        {
            return m_commandSet.GetBatchedAffected(commandIdentifier, out recordsAffected, out error);
        }
        #endregion

        #region InitializeBatching()
        /// <summary>
        /// Initializes batching.
        /// </summary>
        protected override void InitializeBatching()
        {
            m_commandSet = new HsqlCommandSet();

            HsqlCommand command = SelectCommand;

            if (command == null)
            {
                command = InsertCommand;

                if (command == null)
                {
                    command = UpdateCommand;

                    if (command == null)
                    {
                        command = DeleteCommand;
                    }
                }
            }

            if (command != null)
            {
                m_commandSet.Connection = command.Connection;
                m_commandSet.Transaction = command.Transaction;
                m_commandSet.CommandTimeout = command.CommandTimeout;
            }

        }
        #endregion

        #region TerminateBatching()
        /// <summary>
        /// Ends batching.
        /// </summary>
        protected override void TerminateBatching()
        {
            HsqlCommandSet commandSet = m_commandSet;

            if (commandSet != null)
            {
                commandSet.Dispose();

                m_commandSet = null;
            }
        }
        #endregion 

        #endregion

        #region Event Support

        #region CreateRowUpdatedEvent(DataRow,IDbCommand,StatementType,DataTableMapping)
        /// <summary>
        /// Creates and returns a new <see cref="HsqlRowUpdatedEventArgs"/> instance.
        /// </summary>
        /// <param name="dataRow">
        /// The <see cref="DataRow"/> used to update the data source.
        /// </param>
        /// <param name="command">
        /// The <see cref="IDbCommand"/> executed during the 
        /// <see cref="IDataAdapter.Update(DataSet)"/>.
        /// </param>
        /// <param name="statementType">
        /// Whether the command is an UPDATE, INSERT, DELETE, or SELECT statement.
        /// </param>
        /// <param name="tableMapping">
        /// A <see cref="DataTableMapping"/> object.
        /// </param>
        /// <returns>
        /// A new <see cref="HsqlRowUpdatedEventArgs"/> instance.
        /// </returns>
        protected override RowUpdatedEventArgs CreateRowUpdatedEvent(
            DataRow dataRow,
            IDbCommand command,
            StatementType statementType,
            DataTableMapping tableMapping)
        {
            return new HsqlRowUpdatedEventArgs(dataRow, command, statementType,
                tableMapping);
        }
                #endregion

        #region CreateRowUpdatingEvent(DataRow,IDbCommand,StatementType,DataTableMapping)
        /// <summary>
        /// Creates and returns a new <see cref="HsqlRowUpdatingEventArgs"/> instance.
        /// </summary>
        /// <param name="dataRow">
        /// The <see cref="DataRow"/> used to update the data source.
        /// </param>
        /// <param name="command">
        /// The <see cref="IDbCommand"/> executed during the 
        /// <see cref="IDataAdapter.Update(DataSet)"/>.
        /// </param>
        /// <param name="statementType">
        /// Whether the command is an UPDATE, INSERT, DELETE, or SELECT statement.
        /// </param>
        /// <param name="tableMapping">
        /// A <see cref="DataTableMapping"/> object.
        /// </param>
        /// <returns>
        /// A new <see cref="HsqlRowUpdatingEventArgs"/> instance.
        /// </returns>
        protected override RowUpdatingEventArgs CreateRowUpdatingEvent(
            DataRow dataRow,
            IDbCommand command,
            StatementType statementType,
            DataTableMapping tableMapping)
        {
            return new HsqlRowUpdatingEventArgs(dataRow, command, statementType,
                tableMapping);
        }
        #endregion

        #region OnRowUpdating(RowUpdatingEventArgs)
        /// <summary>
        /// Raised by the underlying DbDataAdapter when a row is being updated
        /// </summary>
        /// <param name="value">The event's specifics</param>
        protected override void OnRowUpdating(RowUpdatingEventArgs value)
        {
            EventHandler<RowUpdatingEventArgs> handler = base.Events[
                EventRowUpdating] as EventHandler<RowUpdatingEventArgs>;

            if (handler != null)
            {
                handler(this, value);
            }
        }
        #endregion

        #region OnRowUpdated(RowUpdatedEventArgs)
        /// <summary>
        /// Raised after a row is updated
        /// </summary>
        /// <param name="value">The event's specifics</param>
        protected override void OnRowUpdated(RowUpdatedEventArgs value)
        {
            EventHandler<RowUpdatedEventArgs> handler = base.Events[
                EventRowUpdated] as EventHandler<RowUpdatedEventArgs>;

            if (handler != null)
            {
                handler(this, value);
            }
        }
        #endregion 

        #endregion

        #endregion

        #region Explicit IDbDataAdapter Implementation Members

        #region IDbDataAdapter.DeleteCommand
        /// <summary>
        /// Gets or sets a command for deleting records from the data set.
        /// </summary>
        /// <value>
        /// An <see cref="IDbCommand"/> used during
        /// <see cref="IDataAdapter.Update(DataSet)"/> to delete records in
        /// the data source for deleted rows in the data set.
        /// </value>
        IDbCommand IDbDataAdapter.DeleteCommand
        {
            get { return m_deleteCommand; }
            set { m_deleteCommand = (HsqlCommand)value; }
        } 
        #endregion

        #region IDbDataAdapter.InsertCommand
        /// <summary>
        /// Gets or sets a command used to insert new records into the data
        /// source.
        /// </summary>
        /// <value></value>
        /// <returns>
        /// An <see cref="IDbCommand"/> used during 
        /// <see cref="IDataAdapter.Update(DataSet)"/> to insert records in
        /// the data source for new rows in the data set.
        /// </returns>
        IDbCommand IDbDataAdapter.InsertCommand
        {
            get { return m_insertCommand; }
            set { m_insertCommand = (HsqlCommand)value; }
        } 
        #endregion

        #region IDbDataAdapter.SelectCommand
        /// <summary>
        /// Gets or sets a command used to select records in the data source.
        /// </summary>
        /// <value>
        /// An <see cref="IDbCommand"/> that is used during
        /// <see cref="DbDataAdapter.Update(DataSet)"/> to select records from
        /// the data source for placement in the data set.
        /// </value>
        IDbCommand IDbDataAdapter.SelectCommand
        {
            get { return m_selectCommand; }
            set { m_selectCommand = (HsqlCommand)value; }
        } 
        #endregion

        #region IDbDataAdapter.UpdateCommand
        /// <summary>
        /// Gets or sets a command used to update records in the data source.
        /// </summary>
        /// <value>
        /// An <see cref="IDbCommand"></see> used to
        /// <see cref="IDataAdapter.Update(DataSet)"/> records in the data
        /// source for modified rows in the data set.
        /// </value>
        IDbCommand IDbDataAdapter.UpdateCommand
        {
            get { return m_updateCommand; }
            set { m_updateCommand = (HsqlCommand)value; }
        } 
        #endregion

        #endregion

        #region ICloneable Implementation

        #region ICloneable.Clone()
        /// <summary>
        /// Creates a new object that is a copy of the current instance.
        /// </summary>
        /// <returns>
        /// A new object that is a copy of this instance.
        /// </returns>
        object System.ICloneable.Clone()
        {
            return this.Clone();
        }
        #endregion

        #region Clone()
        /// <summary>
        /// Clones this instance.
        /// </summary>
        /// <returns></returns>
        public HsqlDataAdapter Clone()
        {
            return new HsqlDataAdapter(this);
        }
        #endregion 

        #endregion

    } 
    #endregion
}
