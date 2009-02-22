namespace System.Data.Hsqldb.Client.Design.Control
{
    partial class ConnectionControl
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
            this.m_dataSourceLabel = new System.Windows.Forms.Label();
            this.m_dataSourceTextBox = new System.Windows.Forms.TextBox();
            this.m_userPasswordGroupBox = new System.Windows.Forms.GroupBox();
            this.m_savePasswordButton = new System.Windows.Forms.CheckBox();
            this.m_passwordTextBox = new System.Windows.Forms.TextBox();
            this.m_userTextBox = new System.Windows.Forms.TextBox();
            this.m_passwordLabel = new System.Windows.Forms.Label();
            this.m_userNameLabel = new System.Windows.Forms.Label();
            this.m_advancedButton = new System.Windows.Forms.Button();
            this.m_separatorLabel = new System.Windows.Forms.Label();
            this.m_testConnectionButton = new System.Windows.Forms.Button();
            this.m_okButton = new System.Windows.Forms.Button();
            this.m_cancelButton = new System.Windows.Forms.Button();
            this.m_userPasswordGroupBox.SuspendLayout();
            this.SuspendLayout();
            // 
            // m_dataSourceLabel
            // 
            this.m_dataSourceLabel.AutoSize = true;
            this.m_dataSourceLabel.Location = new System.Drawing.Point(3, 11);
            this.m_dataSourceLabel.Name = "m_dataSourceLabel";
            this.m_dataSourceLabel.Size = new System.Drawing.Size(67, 13);
            this.m_dataSourceLabel.TabIndex = 0;
            this.m_dataSourceLabel.Text = "&Data Source";
            // 
            // m_dataSourceTextBox
            // 
            this.m_dataSourceTextBox.Location = new System.Drawing.Point(6, 27);
            this.m_dataSourceTextBox.Name = "m_dataSourceTextBox";
            this.m_dataSourceTextBox.Size = new System.Drawing.Size(359, 20);
            this.m_dataSourceTextBox.TabIndex = 1;
            // 
            // m_userPasswordGroupBox
            // 
            this.m_userPasswordGroupBox.Controls.Add(this.m_savePasswordButton);
            this.m_userPasswordGroupBox.Controls.Add(this.m_passwordTextBox);
            this.m_userPasswordGroupBox.Controls.Add(this.m_userTextBox);
            this.m_userPasswordGroupBox.Controls.Add(this.m_passwordLabel);
            this.m_userPasswordGroupBox.Controls.Add(this.m_userNameLabel);
            this.m_userPasswordGroupBox.Location = new System.Drawing.Point(6, 53);
            this.m_userPasswordGroupBox.Name = "m_userPasswordGroupBox";
            this.m_userPasswordGroupBox.Size = new System.Drawing.Size(359, 87);
            this.m_userPasswordGroupBox.TabIndex = 2;
            this.m_userPasswordGroupBox.TabStop = false;
            this.m_userPasswordGroupBox.Text = "Log on to database";
            // 
            // m_savePasswordButton
            // 
            this.m_savePasswordButton.AutoSize = true;
            this.m_savePasswordButton.Location = new System.Drawing.Point(70, 66);
            this.m_savePasswordButton.Name = "m_savePasswordButton";
            this.m_savePasswordButton.Size = new System.Drawing.Size(113, 17);
            this.m_savePasswordButton.TabIndex = 4;
            this.m_savePasswordButton.Text = "save my password";
            this.m_savePasswordButton.UseVisualStyleBackColor = true;
            // 
            // m_passwordTextBox
            // 
            this.m_passwordTextBox.Location = new System.Drawing.Point(70, 40);
            this.m_passwordTextBox.Name = "m_passwordTextBox";
            this.m_passwordTextBox.Size = new System.Drawing.Size(276, 20);
            this.m_passwordTextBox.TabIndex = 3;
            // 
            // m_userTextBox
            // 
            this.m_userTextBox.Location = new System.Drawing.Point(70, 14);
            this.m_userTextBox.Name = "m_userTextBox";
            this.m_userTextBox.Size = new System.Drawing.Size(276, 20);
            this.m_userTextBox.TabIndex = 2;
            // 
            // m_passwordLabel
            // 
            this.m_passwordLabel.AutoSize = true;
            this.m_passwordLabel.Location = new System.Drawing.Point(6, 43);
            this.m_passwordLabel.Name = "m_passwordLabel";
            this.m_passwordLabel.Size = new System.Drawing.Size(56, 13);
            this.m_passwordLabel.TabIndex = 1;
            this.m_passwordLabel.Text = "&Password:";
            // 
            // m_userNameLabel
            // 
            this.m_userNameLabel.AutoSize = true;
            this.m_userNameLabel.Location = new System.Drawing.Point(6, 17);
            this.m_userNameLabel.Name = "m_userNameLabel";
            this.m_userNameLabel.Size = new System.Drawing.Size(61, 13);
            this.m_userNameLabel.TabIndex = 0;
            this.m_userNameLabel.Text = "&User name:";
            // 
            // m_advancedButton
            // 
            this.m_advancedButton.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.m_advancedButton.Location = new System.Drawing.Point(285, 146);
            this.m_advancedButton.Name = "m_advancedButton";
            this.m_advancedButton.Size = new System.Drawing.Size(80, 23);
            this.m_advancedButton.TabIndex = 3;
            this.m_advancedButton.Text = "Advanced...";
            this.m_advancedButton.UseVisualStyleBackColor = true;
            this.m_advancedButton.Click += new System.EventHandler(this.m_advancedButton_Click);
            // 
            // m_separatorLabel
            // 
            this.m_separatorLabel.AutoSize = true;
            this.m_separatorLabel.Location = new System.Drawing.Point(4, 172);
            this.m_separatorLabel.Name = "m_separatorLabel";
            this.m_separatorLabel.Size = new System.Drawing.Size(361, 13);
            this.m_separatorLabel.TabIndex = 5;
            this.m_separatorLabel.Text = "___________________________________________________________";
            // 
            // m_testConnectionButton
            // 
            this.m_testConnectionButton.Location = new System.Drawing.Point(7, 188);
            this.m_testConnectionButton.Name = "m_testConnectionButton";
            this.m_testConnectionButton.Size = new System.Drawing.Size(99, 23);
            this.m_testConnectionButton.TabIndex = 6;
            this.m_testConnectionButton.Text = "Test Connection";
            this.m_testConnectionButton.UseVisualStyleBackColor = true;
            this.m_testConnectionButton.Click += new System.EventHandler(this.m_testConnectionButton_Click);
            // 
            // m_okButton
            // 
            this.m_okButton.Location = new System.Drawing.Point(209, 188);
            this.m_okButton.Name = "m_okButton";
            this.m_okButton.Size = new System.Drawing.Size(75, 23);
            this.m_okButton.TabIndex = 7;
            this.m_okButton.Text = "Ok";
            this.m_okButton.UseVisualStyleBackColor = true;
            this.m_okButton.Click += new System.EventHandler(this.m_okButton_Click);
            // 
            // m_cancelButton
            // 
            this.m_cancelButton.Location = new System.Drawing.Point(290, 188);
            this.m_cancelButton.Name = "m_cancelButton";
            this.m_cancelButton.Size = new System.Drawing.Size(75, 23);
            this.m_cancelButton.TabIndex = 8;
            this.m_cancelButton.Text = "Cancel";
            this.m_cancelButton.UseVisualStyleBackColor = true;
            this.m_cancelButton.Click += new System.EventHandler(this.m_cancelButton_Click);
            // 
            // ConnectionControl
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.Controls.Add(this.m_cancelButton);
            this.Controls.Add(this.m_okButton);
            this.Controls.Add(this.m_testConnectionButton);
            this.Controls.Add(this.m_advancedButton);
            this.Controls.Add(this.m_userPasswordGroupBox);
            this.Controls.Add(this.m_dataSourceTextBox);
            this.Controls.Add(this.m_dataSourceLabel);
            this.Controls.Add(this.m_separatorLabel);
            this.Name = "ConnectionControl";
            this.Size = new System.Drawing.Size(373, 217);
            this.m_userPasswordGroupBox.ResumeLayout(false);
            this.m_userPasswordGroupBox.PerformLayout();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Label m_dataSourceLabel;
        private System.Windows.Forms.TextBox m_dataSourceTextBox;
        private System.Windows.Forms.GroupBox m_userPasswordGroupBox;
        private System.Windows.Forms.TextBox m_userTextBox;
        private System.Windows.Forms.Label m_passwordLabel;
        private System.Windows.Forms.Label m_userNameLabel;
        private System.Windows.Forms.TextBox m_passwordTextBox;
        private System.Windows.Forms.CheckBox m_savePasswordButton;
        private System.Windows.Forms.Button m_advancedButton;
        private System.Windows.Forms.Label m_separatorLabel;
        private System.Windows.Forms.Button m_testConnectionButton;
        private System.Windows.Forms.Button m_okButton;
        private System.Windows.Forms.Button m_cancelButton;
    }
}
