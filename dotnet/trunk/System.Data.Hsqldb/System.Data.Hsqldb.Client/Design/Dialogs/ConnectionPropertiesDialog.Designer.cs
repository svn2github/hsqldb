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

using System.Data.Hsqldb.Client.Design.Control;

namespace System.Data.Hsqldb.Client.Design.Dialog
{
    /// <summary>
    /// 
    /// </summary>
    partial class ConnectionPropertiesDialog
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(ConnectionPropertiesDialog));
            this.m_connectionPropertiesControl = new ConnectionPropertiesControl();
            this.m_tableLayoutPanel = new System.Windows.Forms.TableLayoutPanel();
            this.m_flowLayoutPanel = new System.Windows.Forms.FlowLayoutPanel();
            this.m_okButton = new System.Windows.Forms.Button();
            this.m_cancelButton = new System.Windows.Forms.Button();
            this.m_tableLayoutPanel.SuspendLayout();
            this.m_flowLayoutPanel.SuspendLayout();
            this.SuspendLayout();
            // 
            // m_advancedPropertiesControl
            // 
            this.m_connectionPropertiesControl.AutoSizeMode = System.Windows.Forms.AutoSizeMode.GrowAndShrink;
            this.m_connectionPropertiesControl.Dock = System.Windows.Forms.DockStyle.Fill;
            this.m_connectionPropertiesControl.Location = new System.Drawing.Point(3, 3);
            this.m_connectionPropertiesControl.Name = "m_advancedPropertiesControl";
            this.m_connectionPropertiesControl.Size = new System.Drawing.Size(414, 375);
            this.m_connectionPropertiesControl.TabIndex = 0;
            // 
            // m_tableLayoutPanel
            // 
            this.m_tableLayoutPanel.ColumnCount = 1;
            this.m_tableLayoutPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.m_tableLayoutPanel.Controls.Add(this.m_connectionPropertiesControl, 0, 0);
            this.m_tableLayoutPanel.Controls.Add(this.m_flowLayoutPanel, 0, 1);
            this.m_tableLayoutPanel.Dock = System.Windows.Forms.DockStyle.Fill;
            this.m_tableLayoutPanel.Location = new System.Drawing.Point(0, 0);
            this.m_tableLayoutPanel.Name = "m_tableLayoutPanel";
            this.m_tableLayoutPanel.RowCount = 2;
            this.m_tableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.m_tableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.m_tableLayoutPanel.Size = new System.Drawing.Size(420, 416);
            this.m_tableLayoutPanel.TabIndex = 1;
            // 
            // m_flowLayoutPanel
            // 
            this.m_flowLayoutPanel.AutoSize = true;
            this.m_flowLayoutPanel.Controls.Add(this.m_cancelButton);
            this.m_flowLayoutPanel.Controls.Add(this.m_okButton);
            this.m_flowLayoutPanel.Dock = System.Windows.Forms.DockStyle.Fill;
            this.m_flowLayoutPanel.Location = new System.Drawing.Point(3, 384);
            this.m_flowLayoutPanel.Name = "m_flowLayoutPanel";
            this.m_flowLayoutPanel.RightToLeft = System.Windows.Forms.RightToLeft.Yes;
            this.m_flowLayoutPanel.Size = new System.Drawing.Size(414, 29);
            this.m_flowLayoutPanel.TabIndex = 1;
            // 
            // m_okButton
            // 
            this.m_okButton.Location = new System.Drawing.Point(255, 3);
            this.m_okButton.Name = "m_okButton";
            this.m_okButton.Size = new System.Drawing.Size(75, 23);
            this.m_okButton.TabIndex = 1;
            this.m_okButton.Text = "OK";
            this.m_okButton.UseVisualStyleBackColor = true;
            // 
            // m_cancelButton
            // 
            this.m_cancelButton.Location = new System.Drawing.Point(336, 3);
            this.m_cancelButton.Name = "m_cancelButton";
            this.m_cancelButton.Size = new System.Drawing.Size(75, 23);
            this.m_cancelButton.TabIndex = 2;
            this.m_cancelButton.Text = "Cancel";
            this.m_cancelButton.UseVisualStyleBackColor = true;
            // 
            // AdvancedConnectionPropertiesDialog
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(420, 416);
            this.Controls.Add(this.m_tableLayoutPanel);
            this.Icon = ((System.Drawing.Icon)(resources.GetObject("$this.Icon")));
            this.Name = "AdvancedConnectionPropertiesDialog";
            this.Text = "HSQLDB Advanced Connection Properties";
            this.m_tableLayoutPanel.ResumeLayout(false);
            this.m_tableLayoutPanel.PerformLayout();
            this.m_flowLayoutPanel.ResumeLayout(false);
            this.ResumeLayout(false);

        }

        #endregion

        private ConnectionPropertiesControl m_connectionPropertiesControl;
        private System.Windows.Forms.TableLayoutPanel m_tableLayoutPanel;
        private System.Windows.Forms.FlowLayoutPanel m_flowLayoutPanel;
        private System.Windows.Forms.Button m_okButton;
        private System.Windows.Forms.Button m_cancelButton;
    }
}