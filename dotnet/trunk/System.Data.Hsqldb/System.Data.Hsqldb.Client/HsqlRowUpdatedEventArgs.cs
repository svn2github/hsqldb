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
using System.Data.Common;
using System.Data; 
#endregion

namespace System.Data.Hsqldb.Client
{
    #region HsqlRowUpdatedEventArgs
    /// <summary>
    /// Provides data for the <see cref="HsqlDataAdapter.RowUpdated"/> event.
    /// </summary>
    public sealed class HsqlRowUpdatedEventArgs : RowUpdatedEventArgs
    {
        #region HsqlRowUpdatedEventArgs(DataRow,IDbCommand,StatementType,DataTableMapping)
        /// <summary>
        /// Initializes a new <c>HsqlRowUpdatedEventArgs</c> instance.
        /// </summary>
        /// <param name="statementType">
        /// Specifies the type of query executed.
        /// </param>
        /// <param name="row">
        /// The <see cref="DataRow"/> sent through a 
        /// <see cref="DbDataAdapter.Update(DataSet)"/>.
        /// </param>
        /// <param name="command">
        /// The <see cref="IDbCommand"/> executed when 
        /// <see cref="DbDataAdapter.Update(DataSet)"/> is called. </param>
        /// <param name="tableMapping">
        /// The <see cref="DataTableMapping"/> sent through a
        /// <see cref="DbDataAdapter.Update(DataSet)"/>.
        /// </param>        
        public HsqlRowUpdatedEventArgs(
            DataRow row,
            IDbCommand command,
            StatementType statementType,
            DataTableMapping tableMapping)
            : base(row, command, statementType, tableMapping)
        {
        }
        #endregion

        #region Command
        /// <summary>
        /// Gets or sets the <see cref="HsqlCommand"/> executed 
        /// when <see cref="DbDataAdapter.Update(DataSet)"></see>
        /// is called.
        /// </summary>
        /// <returns>The <see cref="HsqlCommand"/> executed when 
        /// <see cref="DbDataAdapter.Update(DataSet)"></see> is called.
        /// </returns>
        public new HsqlCommand Command
        {
            get { return (HsqlCommand)base.Command; }
        }
        #endregion
    } 
    #endregion
}
