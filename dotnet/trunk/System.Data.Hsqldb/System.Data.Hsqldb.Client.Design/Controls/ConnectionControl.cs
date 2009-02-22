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
using System.Collections.Generic;
using System.ComponentModel;
using System.Drawing;
using System.Data;
using System.Text;
using System.Windows.Forms;
using System.Data.Hsqldb.Client;
using System.Data.Hsqldb.Client.Design.Dialog; 
#endregion

namespace System.Data.Hsqldb.Client.Design.Control
{
    #region ConnectionControl
    /// <summary>
    /// 
    /// </summary>
    public partial class ConnectionControl : UserControl
    {
        #region Fields
        HsqlConnectionStringBuilder m_builder;
        ConnectionPropertiesDialog m_dialog = new ConnectionPropertiesDialog(); 
        #endregion

        #region ConnectionControl()
        /// <summary>
        /// Constructs a new <c>ConnectionControl</c> instance.
        /// </summary>
        public ConnectionControl()
        {
            InitializeComponent();
        } 
        #endregion

        #region Methods
        
        #region m_advancedButton_Click(object,EventArgs)
        /// <summary>
        /// Handles the Click event of the m_advancedButton control.
        /// </summary>
        /// <param name="sender">
        /// The source of the event.
        /// </param>
        /// <param name="e">
        /// The <see cref="System.EventArgs"/> instance containing the event data.
        /// </param>
        private void m_advancedButton_Click(object sender, EventArgs e)
        {
            switch (m_dialog.ShowDialog())
            {
                case DialogResult.OK:
                    {
                        break;
                    }
                default:
                    {
                        break;
                    }
            }
        }
                #endregion

        #region m_testConnectionButton_Click(object,EventArgs)
        /// <summary>
        /// Handles the Click event of the m_testConnectionButton control.
        /// </summary>
        /// <param name="sender">
        /// The source of the event.
        /// </param>
        /// <param name="e">
        /// The <see cref="System.EventArgs"/> instance containing the event data.
        /// </param>
        private void m_testConnectionButton_Click(object sender, EventArgs e)
        {

        }
        #endregion

        #region m_okButton_Click(object,EventArgs)
        /// <summary>
        /// Handles the Click event of the m_okButton control.
        /// </summary>
        /// <param name="sender">
        /// The source of the event.
        /// </param>
        /// <param name="e">
        /// The <see cref="System.EventArgs"/> instance containing the event data.
        /// </param>
        private void m_okButton_Click(object sender, EventArgs e)
        {

        }
        #endregion

        #region m_cancelButton_Click(object,EventArgs)
        /// <summary>
        /// Handles the Click event of the m_cancelButton control.
        /// </summary>
        /// <param name="sender">
        /// The source of the event.
        /// </param>
        /// <param name="e">
        /// The <see cref="System.EventArgs"/> instance containing the event data.
        /// </param>
        private void m_cancelButton_Click(object sender, EventArgs e)
        {

        }
        #endregion 
        
        #endregion
    } 
    #endregion
}
