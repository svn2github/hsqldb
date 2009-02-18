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
using System.Data;
using System.Data.Common;
#if W32DESIGN
using System.Drawing;
using System.ComponentModel;
#endif
using System.Data.Hsqldb.Common.Enumeration;
using System;
using System.Globalization;
using System.Text;
#endregion

namespace System.Data.Hsqldb.Client
{
    /// <summary>
    /// <para>
    /// The HSQLDB <see cref="DbCommandBuilder">DbCommandBuilder</see> implementation.
    /// </para>
    /// <img src="/ClassDiagrams/System.Data.Hsqldb.Client.HsqlCommandBuilder.png"
    ///      alt="HsqlCommandBuilder Class Diagram"/>
    /// </summary>
    /// <author name="boucherb@users"/>
#if W32DESIGN    
    [ToolboxBitmap(typeof(resfinder), "System.Data.Hsqldb.Images.Bmp.HsqlCommandBuilder.bmp")]
#endif
    public class HsqlCommandBuilder : DbCommandBuilder
    {
        #region Constants
        const string m_DQ = @"""";
        const string m_DDQ = @"""""";
        const string m_CatalogSeparator = ".";
        const string m_SchemaSeparator = "."; 
        #endregion

        #region Constructors

        #region HsqlCommandBuilder()
        /// <summary>
        /// Constructs a new <c>HsqlCommandBuilder</c> instance.
        /// </summary>
        public HsqlCommandBuilder()
            : base()
        {
            GC.SuppressFinalize(this);
            base.QuotePrefix = m_DQ;
            base.QuoteSuffix = m_DQ;
        }
        #endregion

        #region HsqlCommandBuilder(HsqlDataAdapter)
        /// <summary>
        /// Constructs a new <c>HsqlCommandBuilder</c> instance with the given adapter.
        /// </summary>
        /// <param name="adapter">The adapter.</param>
        public HsqlCommandBuilder(HsqlDataAdapter adapter)
            : this()
        {
            this.DataAdapter = adapter;
        }
        #endregion
 
        #endregion

        #region Public Static Methods

        #region DeriveParameters(HsqlCommand)
        /// <summary>
        /// Determines parameter information for the given <see cref="HsqlCommand"/>
        /// and populates its <c>Parameters</c> collection correspondingly.
        /// </summary>
        /// <param name="command">
        /// The <see cref="HsqlCommand"/> for which the parameter information is
        /// to be derived. The derived parameters are added to its <c>Parameters</c>
        /// collection.
        /// </param>
        public static void DeriveParameters(HsqlCommand command)
        {
            if (command == null)
            {
                throw new ArgumentNullException("command");
            }

            command.DeriveParameters();
        }
        #endregion

        #endregion

        #region System.Data.Common.DbCommandBuilder Member Overrides

        #region Methods
        
        #region ApplyParameterInfo(DbParameter, DataRow, StatementType, bool)
        /// <summary>
        /// Allows this <see cref="HsqlCommandBuilder"/>
        /// to handle additional parameter properties.
        /// </summary>
        /// <param name="parameter">
        /// A <see cref="DbParameter"/> to which the additional 
        /// modifications are applied.
        /// </param>
        /// <param name="row">
        /// The <see cref="DataRow"/> from the schema table
        /// provided by <see cref="DbDataReader.GetSchemaTable"/>.
        /// </param>
        /// <param name="statementType">
        /// The type of command being generated; INSERT, UPDATE or DELETE.
        /// </param>
        /// <param name="whereClause">
        /// <c>true</c> if the parameter is part of the update or delete
        /// <c>WHERE</c> clause; <c>false</c> if it is part of the insert
        /// or update values.
        /// </param>
        protected override void ApplyParameterInfo(
            DbParameter parameter,
            DataRow row,
            StatementType statementType,
            bool whereClause)
        {
            HsqlParameter parm = (HsqlParameter)parameter;

            int? providerType = row[SchemaTableColumn.ProviderType] as int?;

            if (providerType != null)
            {
                parm.ProviderType = (HsqlProviderType)providerType;
            }
        }
        #endregion

        #region GetDeleteCommand()
        /// <summary>Gets the automatically generated <see cref="HsqlCommand"/>
        /// required to perform deletions on the database.
        /// </summary>
        /// <returns>
        /// The automatically generated <see cref="HsqlCommand"/> required to 
        /// perform deletions.
        /// </returns>
        public new HsqlCommand GetDeleteCommand()
        {
            return (HsqlCommand)base.GetDeleteCommand();
        }
        #endregion

        #region GetDeleteCommand(bool)
        /// <summary>
        /// Gets the automatically generated <see cref="HsqlCommand"/> that
        /// is required to perform deletions on the database.
        /// </summary>
        /// <returns>
        /// The automatically generated <see cref="HsqlCommand"/> that is 
        /// required to perform deletions.
        /// </returns>
        /// <param name="useColumnsForParameterNames">
        /// If <c>true</c>, generate parameter names matching column names 
        /// if possible. If <c>false</c>, generate @p1, @p2, and so on.</param>
        public new HsqlCommand GetDeleteCommand(bool useColumnsForParameterNames)
        {
            return (HsqlCommand)base.GetDeleteCommand(useColumnsForParameterNames);
        }
        #endregion

        #region GetInsertCommand()
        /// <summary>
        /// Gets the automatically generated <see cref="HsqlCommand"/> that 
        /// is required to perform insertions on the database.
        /// </summary>
        /// <returns>
        /// The automatically generated <see cref="HsqlCommand"/> that is
        /// required to perform insertions.
        /// </returns>
        public new HsqlCommand GetInsertCommand()
        {
            return (HsqlCommand)base.GetInsertCommand();
        }
        #endregion

        #region GetInsertCommand(bool)
        /// <summary>
        /// Gets the automatically generated <see cref="HsqlCommand"/> that 
        /// is required to perform insertions on the database.
        /// </summary>
        /// <returns>
        /// The automatically generated <see cref="HsqlCommand"/> that is 
        /// required to perform insertions.
        /// </returns>
        /// <param name="useColumnsForParameterNames">
        /// If <c>true</c>, generate parameter names matching column names if
        /// possible. If <c>false</c>, generate @p1, @p2, and so on.
        /// </param>
        public new HsqlCommand GetInsertCommand(bool useColumnsForParameterNames)
        {
            return (HsqlCommand)base.GetInsertCommand(useColumnsForParameterNames);
        }
        #endregion

        #region GetParameterPlaceholder(int)
        /// <summary>
        /// Returns the placeholder for the parameter in the associated
        /// SQL statement.
        /// </summary>
        /// <param name="parameterOrdinal">
        /// The number to be included as part of the parameter's name.
        /// </param>
        /// <returns>
        /// The name of the parameter with the specified number appended.
        /// </returns>
        protected override string GetParameterPlaceholder(int parameterOrdinal)
        {
            return ("@p" + parameterOrdinal.ToString(CultureInfo.InvariantCulture));
        }
        #endregion

        #region GetParameterName(int)
        /// <summary>
        /// Returns the name of the specified parameter in the format of @p#;
        /// Used when building a custom command builder.
        /// </summary>
        /// <param name="parameterOrdinal">
        /// The number to be included as part of the parameter's name.
        /// </param>
        /// <returns>
        /// The name of the parameter with the specified number appended
        /// as part of the parameter name.
        /// </returns>
        protected override string GetParameterName(int parameterOrdinal)
        {
            return ("@p" + parameterOrdinal.ToString(CultureInfo.InvariantCulture));
        }
        #endregion

        #region GetParameterName(string)
        /// <summary>
        /// Returns the full parameter name, given the partial parameter name.
        /// </summary>
        /// <param name="parameterName">The partial name of the parameter.</param>
        /// <returns>
        /// The full parameter name corresponding to the partial parameter name requested.
        /// </returns>
        protected override string GetParameterName(string parameterName)
        {
            return string.Concat("@", parameterName);
        }
        #endregion

        #region GetUpdateCommand()
        /// <summary>
        /// Gets the automatically generated <see cref="HsqlCommand"/> that
        /// is required to perform updates on the database.
        /// </summary>
        /// <returns>
        /// The automatically generated <see cref="HsqlCommand"/> that is
        /// required to perform updates.
        /// </returns>
        public new HsqlCommand GetUpdateCommand()
        {
            return (HsqlCommand)base.GetUpdateCommand();
        }
        #endregion

        #region GetUpdateCommand(bool)
        /// <summary>
        /// Gets the automatically generated <see cref="HsqlCommand"/> that
        /// is required to perform updates on the database.
        /// </summary>
        /// <returns>
        /// The automatically generated <see cref="HsqlCommand"/> that is
        /// required to perform updates.
        /// </returns>
        /// <param name="useColumnsForParameterNames">
        /// If <c>true</c>, generate parameter names matching column names if 
        /// possible. If <c>false</c>, generate @p1, @p2, and so on.
        /// </param>
        public new HsqlCommand GetUpdateCommand(bool useColumnsForParameterNames)
        {
            return (HsqlCommand)base.GetUpdateCommand(useColumnsForParameterNames);
        }
        #endregion

        #region QuoteIdentifier(string)
        /// <summary>
        /// Given an unquoted identifier in the correct catalog case, returns
        /// the correct quoted form of that identifier. This includes correctly
        /// escaping any embedded quotes in the identifier.
        /// </summary>
        /// <returns>
        /// The quoted version of the identifier. Embedded quotes within the
        /// identifier are correctly escaped.
        /// </returns>
        /// <param name="unquotedIdentifier">
        /// The original unquoted identifier.
        /// </param>
        public override string QuoteIdentifier(string unquotedIdentifier)
        {
            if (unquotedIdentifier == null)
            {
                throw new ArgumentNullException(
                    "unquotedIdentifier");
            }

            return new StringBuilder().Append(m_DQ).Append(unquotedIdentifier
                .Replace(m_DQ, m_DDQ)).Append(m_DQ).ToString();
        }
        #endregion

        #region SetRowUpdatingHandler(DbDataAdapter)
        /// <summary>
        /// Registers this <see cref="HsqlCommandBuilder"/> to handle the
        /// <see cref="DbDataAdapter.OnRowUpdating"/> event for the given
        /// <see cref="DbDataAdapter"/>.
        /// </summary>
        /// <param name="adapter">
        /// The <see cref="DbDataAdapter"/> to be used for the update.
        /// </param>
        protected override void SetRowUpdatingHandler(DbDataAdapter adapter)
        {
            HsqlDataAdapter hsqlDataAdapter = ((HsqlDataAdapter)adapter);

            if (adapter == base.DataAdapter)
            {
                hsqlDataAdapter.RowUpdating -= new HsqlRowUpdatingEventHandler(
                        this.HsqlRowUpdatingHandler);
            }
            else
            {
                hsqlDataAdapter.RowUpdating += new HsqlRowUpdatingEventHandler(
                        this.HsqlRowUpdatingHandler);
            }
        }
        #endregion

        #region UnquoteIdentifier(string)
        /// <summary>
        /// Given a quoted identifier, returns the correct unquoted
        /// form of that identifier. This includes correctly unescaping
        /// any embedded quotes in the identifier.
        /// </summary>
        /// <returns>
        /// The unquoted identifier, with embedded quotes properly unescaped.
        /// </returns>
        /// <param name="quotedIdentifier">
        /// The identifier that will have its embedded quotes removed.
        /// </param>
        public override string UnquoteIdentifier(string quotedIdentifier)
        {
            string s = quotedIdentifier;

            if (s == null)
            {
                throw new ArgumentNullException("quotedIdentifier");
            }

            return (s.Length >= 2) && s.StartsWith(m_DQ) && s.EndsWith(m_DQ)
                ? s.Substring(1, s.Length - 2).Replace(m_DDQ, m_DQ) : s;
        }
        #endregion 

        #endregion

        #region Properties

        #region CatalogLocation
        /// <summary>
        /// Indicates the position of the catalog name in a qualified table name
        /// in a text command. 
        /// </summary>
        /// <remarks>This property cannot be changed.</remarks>
        /// <value>CatalogLocation.Start</value>
        [DesignerSerializationVisibility(DesignerSerializationVisibility.Hidden)]
        [Browsable(false)]
        [EditorBrowsable(EditorBrowsableState.Never)]
        public override CatalogLocation CatalogLocation
        {
            get { return CatalogLocation.Start; }
            set
            {
                if (CatalogLocation.Start != value)
                {
                    throw new ArgumentException(string.Format(
                        "Location must be CatalogLocation.Start, value: {0}",
                        value), "value");
                }
            }
        }
        #endregion

        #region CatalogSeparator
        /// <summary>
        /// Sets or gets the catalog separator.
        /// </summary>
        /// <remarks>This property cannot be changed.</remarks>
        /// <value>.</value>
        [EditorBrowsable(EditorBrowsableState.Never)]
        [DesignerSerializationVisibility(DesignerSerializationVisibility.Hidden)]
        [Browsable(false)]
        public override string CatalogSeparator
        {
            get
            {
                return m_CatalogSeparator;
            }
            set
            {
                if (m_CatalogSeparator != value)
                {
                    throw new ArgumentException(string.Format(
                        "Catalog Separator must be the period character " +
                        "[.], value: (0}", value), "value");
                }
            }
        }
        #endregion

        #region DataAdapter
        /// <summary>
        /// Gets or sets the <see cref="HsqlDataAdapter"/> object for 
        /// which SQL statements are automatically generated.
        /// </summary>
        /// <value>An <see cref="HsqlDataAdapter"/> object.</value>
        [DefaultValue((HsqlDataAdapter)null)]
        public new HsqlDataAdapter DataAdapter
        {
            get { return (HsqlDataAdapter)base.DataAdapter; }
            set { base.DataAdapter = value; }
        }
        #endregion

        #region QuotePrefix
        /// <summary>
        /// The beginning character that should be used when
        /// specifying database objects (for example, tables or columns) whose
        /// names contain characters such as spaces or reserved tokens.
        /// </summary>
        /// <remarks>This property cannot be changed.</remarks>
        /// <value>"</value>
        public override string QuotePrefix
        {
            get { return m_DQ; }
            set
            {
                if (value != m_DQ)
                {
                    throw new ArgumentException(string.Format(
                        "QuotePrefix must be double quote [\"], value: {0}",
                        value), "value");
                }
            }
        }
        #endregion

        #region QuoteSuffix
        /// <summary>
        /// The ending character that should be used when specifying
        /// database objects (for example, tables or columns) whose
        /// names contain characters such as spaces or reserved tokens.
        /// </summary>
        /// <remarks>This property cannot be changed.</remarks>
        /// <value>"</value>    
        public override string QuoteSuffix
        {
            get { return m_DQ; }
            set
            {
                if (value != m_DQ)
                {
                    throw new ArgumentException(string.Format(
                        "QuoteSuffix must be double quote [\"], value: {0}",
                        value), "value");
                }
            }
        }
        #endregion

        #region SchemaSeparator
        /// <summary>
        /// The schema separator.
        /// </summary>
        /// <remarks>This property cannot be changed.</remarks>
        /// <value>.</value>
        [EditorBrowsable(EditorBrowsableState.Never)]
        [DesignerSerializationVisibility(DesignerSerializationVisibility.Hidden)]
        [Browsable(false)]
        public override string SchemaSeparator
        {
            get
            {
                return m_SchemaSeparator;
            }
            set
            {
                if (m_SchemaSeparator != value)
                {
                    throw new ArgumentException(string.Format(
                        "SchemaSeparator must be the period character " +
                        "[.], value: {0}", value), "value");
                }
            }
        }
        #endregion

        #endregion

        #endregion

        #region Private Instance Methods

        #region HsqlRowUpdatingHandler(object,HsqlRowUpdatingEventArgs)
        /// <summary>
        /// Signature adapter.
        /// </summary>
        /// <param name="sender">The event sender.</param>
        /// <param name="evt">
        /// The event arguments instance containing the event data.
        /// </param>
        private void HsqlRowUpdatingHandler(object sender, 
            HsqlRowUpdatingEventArgs evt)
        {
            base.RowUpdatingHandler(evt);
        }
        #endregion

        #endregion
    }
}