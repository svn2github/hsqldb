using System.Data.Hsqldb.Client.Design.Control;

namespace System.Data.Hsqldb.Client.Design.Dialog
{
    /// <remarks/>
    partial class ConnectionDialog
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
            this.connectionControl1 = new ConnectionControl();
            this.SuspendLayout();
            // 
            // connectionControl1
            // 
            this.connectionControl1.Dock = System.Windows.Forms.DockStyle.Fill;
            this.connectionControl1.Location = new System.Drawing.Point(0, 0);
            this.connectionControl1.Name = "connectionControl1";
            this.connectionControl1.Size = new System.Drawing.Size(379, 225);
            this.connectionControl1.TabIndex = 0;
            // 
            // ConnectionDialog
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(379, 225);
            this.Controls.Add(this.connectionControl1);
            this.Name = "ConnectionDialog";
            this.Text = "ConnectionDialog";
            this.ResumeLayout(false);

        }

        #endregion

        private ConnectionControl connectionControl1;
    }
}