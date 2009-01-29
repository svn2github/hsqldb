#region Using
using System.Data.Hsqldb.Client;
using System.Data.Hsqldb.Client.Design.Component; 
#endregion

namespace System.Data.Hsqldb.Client.Design.Control
{
    partial class ConnectionPropertiesControl
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

        #region Component Designer generated code

        /// <summary> 
        /// Required method for Designer support - do not modify 
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.m_propertiesGrid = new ConnectionPropertiesGrid();
            this.m_connectionStringPreviewTextBox = new System.Windows.Forms.TextBox();
            this.m_tableLayoutPanel = new System.Windows.Forms.TableLayoutPanel();
            this.m_tableLayoutPanel.SuspendLayout();
            this.SuspendLayout();
            // 
            // m_propertiesGrid
            // 
            this.m_propertiesGrid.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom)
                        | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.m_propertiesGrid.Location = new System.Drawing.Point(3, 3);
            this.m_propertiesGrid.Name = "m_propertiesGrid";
            this.m_propertiesGrid.Size = new System.Drawing.Size(409, 392);
            this.m_propertiesGrid.TabIndex = 0;
            this.m_propertiesGrid.PropertyValueChanged += new System.Windows.Forms.PropertyValueChangedEventHandler(this.OnPropertyValueChanged);
            // 
            // m_connectionStringPreviewTextBox
            // 
            this.m_connectionStringPreviewTextBox.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Left)
                        | System.Windows.Forms.AnchorStyles.Right)));
            this.m_connectionStringPreviewTextBox.CausesValidation = false;
            this.m_connectionStringPreviewTextBox.Location = new System.Drawing.Point(3, 401);
            this.m_connectionStringPreviewTextBox.Name = "m_connectionStringPreviewTextBox";
            this.m_connectionStringPreviewTextBox.ReadOnly = true;
            this.m_connectionStringPreviewTextBox.Size = new System.Drawing.Size(409, 20);
            this.m_connectionStringPreviewTextBox.TabIndex = 1;
            // 
            // m_tableLayoutPanel
            // 
            this.m_tableLayoutPanel.AutoSize = true;
            this.m_tableLayoutPanel.ColumnCount = 1;
            this.m_tableLayoutPanel.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.m_tableLayoutPanel.Controls.Add(this.m_propertiesGrid, 0, 0);
            this.m_tableLayoutPanel.Controls.Add(this.m_connectionStringPreviewTextBox, 0, 1);
            this.m_tableLayoutPanel.Dock = System.Windows.Forms.DockStyle.Fill;
            this.m_tableLayoutPanel.Location = new System.Drawing.Point(0, 0);
            this.m_tableLayoutPanel.Name = "m_tableLayoutPanel";
            this.m_tableLayoutPanel.RowCount = 2;
            this.m_tableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.m_tableLayoutPanel.RowStyles.Add(new System.Windows.Forms.RowStyle());
            this.m_tableLayoutPanel.Size = new System.Drawing.Size(415, 424);
            this.m_tableLayoutPanel.TabIndex = 2;
            // 
            // AdvancedConnectionPropertiesControl
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.AutoSizeMode = System.Windows.Forms.AutoSizeMode.GrowAndShrink;
            this.Controls.Add(this.m_tableLayoutPanel);
            this.Name = "AdvancedConnectionPropertiesControl";
            this.Size = new System.Drawing.Size(415, 424);
            this.m_tableLayoutPanel.ResumeLayout(false);
            this.m_tableLayoutPanel.PerformLayout();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private ConnectionPropertiesGrid m_propertiesGrid;
        private System.Windows.Forms.TextBox m_connectionStringPreviewTextBox;
        private System.Windows.Forms.TableLayoutPanel m_tableLayoutPanel;        
    }
}

