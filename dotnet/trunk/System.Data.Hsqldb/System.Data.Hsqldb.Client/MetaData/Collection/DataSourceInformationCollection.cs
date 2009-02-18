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
using CN = System.Data.Common.DbMetaDataCollectionNames;
using HCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataCollectionNames;
using MDCN = System.Data.Common.DbMetaDataColumnNames;
using HMDCN = System.Data.Hsqldb.Client.MetaData.HsqlMetaDataColumnNames;
using System;
#endregion

namespace System.Data.Hsqldb.Client.MetaData.Collection
{
    #region DataSourceInformationCollection

    /// <summary>
    /// <para>
    /// Provides the <see cref="CN.DataSourceInformation"/> collection.
    /// </para>
    /// <img src="/ClassDiagrams/System.Data.Hsqldb.Client.MetaData.Collection.DataSourceInformationCollection.png"
    ///      alt="DataSourceInformationCollection Class Diagram"/>
    /// </summary>
    /// <author name="boucherb@users"/>
    public class DataSourceInformationCollection : Base.CachedMetadataCollection
    {
        #region DataSourceInformationCollection(HsqlConnection

        /// <summary>
        /// Constructs a new <c>DataSourceInformationCollection</c> instance
        /// with the given connection.
        /// </summary>
        public DataSourceInformationCollection()
            : base()
        {
        }

        #endregion

        #region CreateTable()

        /// <summary>
        /// Creates the table.
        /// </summary>
        /// <returns></returns>
        public override DataTable CreateTable()
        {
            DataTable table = new DataTable(CN.DataSourceInformation);
            DataColumnCollection cols = table.Columns;
            Type st = typeof(string);
            Type gbbt = typeof(GroupByBehavior);
            Type bt = typeof(bool);
            Type it = typeof(int);
            Type ict = typeof(IdentifierCase);
            Type sjot = typeof(SupportedJoinOperators);

            AddColumn(cols, null, MDCN.CompositeIdentifierSeparatorPattern, st);
            AddColumn(cols, null, MDCN.DataSourceProductName, st);
            AddColumn(cols, null, MDCN.DataSourceProductVersion, st);
            AddColumn(cols, null, MDCN.DataSourceProductVersionNormalized, st);
            AddColumn(cols, null, MDCN.GroupByBehavior, gbbt);
            AddColumn(cols, null, MDCN.IdentifierPattern, st);
            AddColumn(cols, null, MDCN.IdentifierCase, ict);
            AddColumn(cols, null, MDCN.OrderByColumnsInSelect, bt);
            AddColumn(cols, null, MDCN.ParameterMarkerFormat, st);
            AddColumn(cols, null, MDCN.ParameterMarkerPattern, st);
            AddColumn(cols, null, MDCN.ParameterNameMaxLength, it);
            AddColumn(cols, null, MDCN.ParameterNamePattern, st);
            AddColumn(cols, null, MDCN.QuotedIdentifierPattern, st);
            AddColumn(cols, null, MDCN.QuotedIdentifierCase, ict);
            AddColumn(cols, null, MDCN.StatementSeparatorPattern, st);
            AddColumn(cols, null, MDCN.StringLiteralPattern, st);
            AddColumn(cols, null, MDCN.SupportedJoinOperators, sjot);

            return table;
        }

        #endregion

        #region FillTable(DataTable,string[])

        /// <summary>
        /// Fills the table.
        /// </summary>
        /// <param name="connection">The connection.</param>
        /// <param name="table">The table.</param>
        /// <param name="restrictions">The restrictions.</param>
        public override void FillTable(HsqlConnection connection,
            DataTable table, string[] restrictions)
        {
            AddRow(
                table,
                ValueOf.CompositeIndentiferSeparatorPattern,
                ValueOf.DataSourceProductName,
                ValueOf.DataSourceProductVersion,
                ValueOf.DataSourceProductVersionNormalized,
                ValueOf.HsqlGroupByBehavior,
                ValueOf.IdentifierPattern,
                ValueOf.HsqlIdentifierCase,
                ValueOf.OrderByColumnsInSelect,
                ValueOf.ParameterMarkerFormat,
                ValueOf.ParameterMarkerPattern,
                ValueOf.ParameterNameMaxLength,
                ValueOf.ParameterNamePattern,
                ValueOf.QuotedIdentifierPattern,
                ValueOf.QuotedIdentifierCase,
                ValueOf.StatementSeparatorPattern,
                ValueOf.StringLiteralPattern,
                ValueOf.HsqlSupportedJoinOperators);
        }

        #endregion

        #region AddRow(...)

        /// <summary>
        /// Adds the row.
        /// </summary>
        /// <param name="table">
        /// The table.
        /// </param>
        /// <param name="compositeIdentifierSeparatorPattern">
        /// The composite identifier separator pattern.
        /// </param>
        /// <param name="dataSourceProductName">
        /// Name of the data source product.
        /// </param>
        /// <param name="dataSourceProductVersion">
        /// The data source product version.
        /// </param>
        /// <param name="dataSourceProductVersionNormalized">
        /// The data source product version normalized.
        /// </param>
        /// <param name="groupByBehavior">
        /// The group by behavior.
        /// </param>
        /// <param name="identifierPattern">
        /// The identifier pattern.
        /// </param>
        /// <param name="identifierCase">
        /// The identifier case.
        /// </param>
        /// <param name="orderByColumnsInSelect">
        /// if set to <c>true</c> [order by columns in select].
        /// </param>
        /// <param name="parameterMarkerFormat">
        /// The parameter marker format.
        /// </param>
        /// <param name="parameterMarkerPattern">
        /// The parameter marker pattern.
        /// </param>
        /// <param name="parameterNameMaxLength">
        /// Length of the parameter name max.
        /// </param>
        /// <param name="parameterNamePattern">
        /// The parameter name pattern.
        /// </param>
        /// <param name="quotedIdentifierPattern">
        /// The quoted identifier pattern.
        /// </param>
        /// <param name="quotedIdentifierCase">
        /// The quoted identifier case.
        /// </param>
        /// <param name="statementSeparatorPattern">
        /// The statement separator pattern.
        /// </param>
        /// <param name="stringLiteralPattern">
        /// The string literal pattern.
        /// </param>
        /// <param name="supportedJoinOperators">
        /// The supported join operators.
        /// </param>
        public static void AddRow(
            DataTable table,
            string compositeIdentifierSeparatorPattern,
            string dataSourceProductName,
            string dataSourceProductVersion,
            string dataSourceProductVersionNormalized,
            GroupByBehavior groupByBehavior,
            string identifierPattern,
            IdentifierCase identifierCase,
            bool orderByColumnsInSelect,
            string parameterMarkerFormat,
            string parameterMarkerPattern,
            int parameterNameMaxLength,
            string parameterNamePattern,
            string quotedIdentifierPattern,
            IdentifierCase quotedIdentifierCase,
            string statementSeparatorPattern,
            string stringLiteralPattern,
            SupportedJoinOperators supportedJoinOperators)
        {
            DataRow row = table.NewRow();

            row[MDCN.CompositeIdentifierSeparatorPattern]
                = compositeIdentifierSeparatorPattern;
            row[MDCN.DataSourceProductName] = dataSourceProductName;
            row[MDCN.DataSourceProductVersion] = dataSourceProductVersion;
            row[MDCN.DataSourceProductVersionNormalized]
                = dataSourceProductVersionNormalized;
            row[MDCN.GroupByBehavior] = groupByBehavior;
            row[MDCN.IdentifierPattern] = identifierPattern;
            row[MDCN.IdentifierCase] = identifierCase;
            row[MDCN.OrderByColumnsInSelect] = orderByColumnsInSelect;
            row[MDCN.ParameterMarkerFormat] = parameterMarkerFormat;
            row[MDCN.ParameterMarkerPattern] = parameterMarkerPattern;
            row[MDCN.ParameterNameMaxLength] = parameterNameMaxLength;
            row[MDCN.ParameterNamePattern] = parameterNamePattern;
            row[MDCN.QuotedIdentifierPattern] = quotedIdentifierPattern;
            row[MDCN.QuotedIdentifierCase] = quotedIdentifierCase;
            row[MDCN.StatementSeparatorPattern] = statementSeparatorPattern;
            row[MDCN.StringLiteralPattern] = stringLiteralPattern;
            row[MDCN.SupportedJoinOperators] = supportedJoinOperators;

            table.Rows.Add(row);
        }

        #endregion

        #region ValueOf

        /// <summary>
        /// Provides the data source information constants.
        /// </summary>
        public static class ValueOf
        {
            #region CompositeIndentiferSeparatorPattern

            /// <summary>
            /// The HSQLDB composite identifier separator pattern
            /// </summary>
            /// <value>@"\."</value>
            public const string CompositeIndentiferSeparatorPattern = @"\.";

            #endregion

            #region DataSourceProductName

            /// <summary>
            /// The HSQLDB data source product name.
            /// </summary>
            /// <value>"HSQL Database Engine"</value>
            public const string DataSourceProductName
                = "HSQL Database Engine";

            #endregion

            #region DataSourceProductVersion

            /// <summary>
            /// The HSQLDB data source product version.
            /// </summary>
            /// <value>"1.8.0.10"</value>
            public const string DataSourceProductVersion = "1.8.0.10";

            #endregion

            #region DataSourceProductVersionNormalized

            /// <summary>
            /// The normalized HSQLDB data source product version.
            /// </summary>
            /// <value>"01.80.7000"</value>
            public const string DataSourceProductVersionNormalized
                = "01.80.0010";

            #endregion

            #region HsqlGroupByBehavior

            /// <summary>
            /// The HSLQDB SQL <c>GROUP BY</c> behaviour.
            /// </summary>
            /// <value><see cref="GroupByBehavior.MustContainAll"/></value>
            public const GroupByBehavior HsqlGroupByBehavior
                = GroupByBehavior.MustContainAll;

            #endregion

            #region JavaIdentifierPattern

            /// <summary>
            /// The Java Identifier pattern.
            /// </summary>
            /// <value>^[\p{JavaIdentifierStart}][\p{JavaIdentifierPart}]*$</value>
            public const string JavaIdentifierPattern =
                "^[\\p{Ll}\\p{Lm}\\p{Lo}\\p{Lt}\\p{Lu}\\p{Nl}\\p{Pc}\\p{Sc}][\\p{Cf}\\p{Ll}\\p{Lm}\\p{Lo}\\p{Lt}\\p{Lu}\\p{Mc}\\p{Mn}\\p{Nd}\\p{Nl}\\p{Pc}\\p{Sc}\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\u0008\u000e\u000f\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001a\u001b\u007f\u0080\u0081\u0082\u0083\u0084\u0085\u0086\u0087\u0088\u0089\u008a\u008b\u008c\u008d\u008e\u008f\u0090\u0091\u0092\u0093\u0094\u0095\u0096\u0097\u0098\u0099\u009a\u009b\u009c\u009d\u009e\u009f]*$";

            #endregion

            #region IdentifierPattern

            /// <summary>
            /// The HSQLDB Identifier pattern.
            /// </summary>
            /// <value>^[A-Z][A-Z0-9_]*$</value>
            /// <seealso cref="JavaIdentifierPattern"/>
            public const string IdentifierPattern ="^[A-Z][A-Z0-9_]*$";

            #endregion

            #region HsqlIdentifierCase

            /// <summary>
            /// The HSQLDB IDentifier case.
            /// </summary>
            /// <value><see cref="IdentifierCase.Insensitive"/></value>
            public const IdentifierCase HsqlIdentifierCase
                = IdentifierCase.Insensitive;

            #endregion

            #region OrderByColumnsInSelect

            /// <summary>
            /// Whether SQL <c>ORDER BY</c> columns can be in the select list.
            /// </summary>
            /// <value>true</value>
            public const bool OrderByColumnsInSelect = true;

            #endregion

            #region ParameterMarkerFormat

            /// <summary>
            /// The HSQLDB parameter marker format string.
            /// </summary>
            /// <value>"{0}"</value>
            public const string ParameterMarkerFormat = "{0}";

            #endregion

            #region ParameterMarkerPattern

            /// <summary>
            /// The HSLQDB parameter marker pattern.
            /// </summary>
            /// <value>@(^[\p{JavaIdentifierStart}][\p{JavaIdentifierPart}]*$)</value>
            public const string ParameterMarkerPattern = 
                "@(" + JavaIdentifierPattern + ")";

            #endregion

            #region ParameterNameMaxLength

            /// <summary>
            /// The maximum HSQLDB parameter name length.
            /// </summary>
            /// <remarks>
            /// Not actually enforced.
            /// </remarks>
            /// <value>128</value>
            public const int ParameterNameMaxLength = 128;

            #endregion

            #region ParameterNamePattern

            /// <summary>
            /// The HSQLDB parameter name pattern.
            /// </summary>
            /// <value>^[\p{JavaIdentifierStart}][\p{JavaIdentifierPart}]*$</value>
            public const string ParameterNamePattern = JavaIdentifierPattern;

            #endregion

            #region QuotedIdentifierPattern

            /// <summary>
            /// The HSQLDB quoted identifier pattern.
            /// </summary>
            /// <value>"\"^(([^\"]|\"\")*)$\""</value>
            public const string QuotedIdentifierPattern
                = "\"^(([^\"]|\"\")*)$\"";

            #endregion

            #region QuotedIdentifierCase

            /// <summary>
            /// The HSQLDB quoted identifier case-sensitivity.
            /// </summary>
            /// <value><see cref="IdentifierCase.Sensitive"/></value>
            public const IdentifierCase QuotedIdentifierCase
                = IdentifierCase.Sensitive;

            #endregion

            #region StatementSeparatorPattern

            /// <summary>
            /// The HSQLDB statement separator pattern.
            /// </summary>
            /// <value>";"</value>
            public const string StatementSeparatorPattern = ";";

            #endregion

            #region StringLiteralPattern

            /// <summary>
            /// The HSQLDB string literal pattern.
            /// </summary>
            /// <value>"'(([^']|'')*)'"</value>
            public const string StringLiteralPattern = "'(([^']|'')*)'";

            #endregion

            #region HsqlSupportedJoinOperators

            /// <summary>
            /// The supported HSLQDB join operators
            /// </summary>
            /// <value>
            /// <see cref="SupportedJoinOperators.Inner"/>
            /// | <see cref="SupportedJoinOperators.LeftOuter"/>
            /// | <see cref="SupportedJoinOperators.RightOuter"/>
            /// </value>
            public const SupportedJoinOperators HsqlSupportedJoinOperators
                = SupportedJoinOperators.Inner
                  | SupportedJoinOperators.LeftOuter
                  | SupportedJoinOperators.RightOuter;

            #endregion
        }

        #endregion
    }

    #endregion
}