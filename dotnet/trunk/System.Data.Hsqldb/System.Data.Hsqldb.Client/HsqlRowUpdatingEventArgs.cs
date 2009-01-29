
#region Using
using System.Data;
using System.Data.Common; 
#endregion

namespace System.Data.Hsqldb.Client
{
    #region HsqlRowUpdatingEventArgs

    /// <summary>
    /// Provides data for the <see cref="HsqlDataAdapter.RowUpdating"/> event.
    /// </summary>
    /// <author name="boucherb@users"/>
    public sealed class HsqlRowUpdatingEventArgs : RowUpdatingEventArgs
    {
        #region HsqlRowUpdatingEventArgs(DataRow,IDbCommand,StatementType,DataTableMapping)
        /// <summary>
        /// Initializes a new <c>HsqlRowUpdatingEventArgs</c> instance.
        /// </summary>
        /// <param name="statementType">
        /// Specifies the type of query executed.
        /// </param>
        /// <param name="row">
        /// The <see cref="DataRow"/> to 
        /// <see cref="DbDataAdapter.Update(DataSet)"/>.
        /// </param>
        /// <param name="command">
        /// The <see cref="IDbCommand"/> to execute during 
        /// <see cref="DbDataAdapter.Update(DataSet)"/>.
        /// </param>
        /// <param name="tableMapping">
        /// The <see cref="DataTableMapping"></see> sent through
        /// an <see cref="DbDataAdapter.Update(DataSet)"/>.
        /// </param>
        public HsqlRowUpdatingEventArgs(
            DataRow row,
            IDbCommand command,
            StatementType statementType,
            DataTableMapping tableMapping)
            : base(row, command, statementType, tableMapping)
        {
        }
        #endregion

        #region BaseCommand
        /// <summary>
        /// Gets or sets the <see cref="IDbCommand"/> object for
        /// an instance of this class.
        /// </summary>
        /// <value>
        /// The <see cref="IDbCommand"/> to execute during the 
        /// <see cref="DbDataAdapter.Update(DataSet)"/>. 
        /// </value>
        protected override IDbCommand BaseCommand
        {
            get { return base.BaseCommand; }
            set { base.BaseCommand = value as HsqlCommand; }
        }
        #endregion

        #region Command
        /// <summary>
        /// Gets or sets the <see cref="HsqlCommand"/> to execute 
        /// when performing the <see cref="DbDataAdapter.Update(DataSet)"/>.
        /// </summary>
        /// <returns>
        /// The <c>HsqlCommand"</c> to execute when performing the update.
        /// </returns>
        public new HsqlCommand Command
        {
            get { return (base.Command as HsqlCommand); }
            set { base.Command = value; }
        }
        #endregion
    } 

    #endregion
}



