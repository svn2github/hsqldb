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

using System.ComponentModel;
using System.Data;
using System.Data.Common;
using System.Data.Hsqldb.Common.Enumeration;
using System.Data.Hsqldb.Client.Internal;
using System.Data.Hsqldb.Client.MetaData;
using System.Data.Hsqldb.Common.Sql;
using System.Text;
using System.Threading;

#if W32DESIGN
using System.Drawing;
#endif

using ParameterMetaData = org.hsqldb.Result.ResultMetaData;
using PMD = java.sql.ParameterMetaData.__Fields;
using Result = org.hsqldb.Result;
using ResultConstants = org.hsqldb.ResultConstants.__Fields;
using HsqlTypes = org.hsqldb.Types;
using System.Data.Hsqldb.Client.Sql;
using System.Data.Hsqldb.Common;
using System.Collections.Generic;

#endregion

namespace System.Data.Hsqldb.Client
{
    #region HsqlCommand

    public sealed partial class HsqlCommand
    {
        #region Fields

        // Facilitates a quick test for named parameters.
        private static readonly char[] m_ParmeterChars = new char[] { '@', ':' };
        // Optimization to avoid burning through empty object[] instances
        private static readonly object[] m_NoParameters = new object[0];
        //
        // Backs the CommandText property.
        private string m_commandText = String.Empty;
        // computed from m_commandText.
        private bool m_commandTextHasParameters;
        private string m_storedProcedureCommandText;
        private string m_tableDirectCommandText;
        // Backs the CommandTimeout property.
        private int m_commandTimeout = 30;
        // Backs the CommandType property.
        private CommandType m_commandType = CommandType.Text;
        // Backs the Connection property.
        private HsqlConnection m_dbConnection;
        // Backs the Transaction property.
        private HsqlTransaction m_dbTransaction;
        // Backs the Parameters property.
        private HsqlParameterCollection m_dbParameterCollection;
        // Backs the DesignTimeVisible property.
        private bool m_designTimeVisible = true;
        // Backs the UpdatedRowSource property.
        private UpdateRowSource m_updateRowSource = UpdateRowSource.Both;
        // Represents the prepared form of this command.
        private HsqlStatement m_statement;
        // Used to reconcile objects in the Parameters collection
        // and/or parameter metadata about the prepared form of this
        // command against parameter tokens in the CommandText.
        private TokenList m_tokenList;
        // Backs the SyncRoot property.
        private readonly object m_syncRoot = new object();
        List<string> m_commandTextBatch;

        #endregion

        #region Constructors

        #region HsqlCommand(HsqlCommand)
        /// <summary>
        /// Constructs a new <c>HsqlCommand</c> instance that
        /// is a copy of the given command object.
        /// </summary>
        /// <param name="srcCommand">The source command.</param>
        private HsqlCommand(HsqlCommand srcCommand)
            : this()
        {
            this.m_commandTextHasParameters
                = srcCommand.m_commandTextHasParameters;

            if (srcCommand.m_commandTextHasParameters && 
                (srcCommand.m_tokenList != null))
            {
                this.m_tokenList = srcCommand.m_tokenList.Clone();
            }

            this.CommandText = srcCommand.CommandText;
            this.CommandTimeout = srcCommand.CommandTimeout;
            this.CommandType = srcCommand.CommandType;
            this.Connection = srcCommand.Connection;
            this.DesignTimeVisible = srcCommand.DesignTimeVisible;
            this.Transaction = srcCommand.Transaction; // CHECKME
            this.UpdatedRowSource = srcCommand.UpdatedRowSource;

            HsqlParameterCollection parameters = this.Parameters;

            foreach (HsqlParameter parameter in srcCommand.Parameters)
            {
                parameters.Add(parameter.Clone());
            }
        }
        #endregion

        #endregion

        #region Instance Members

        #region Internal Methods

        #region ApplyParameters()
        /// <summary>
        /// See <see cref="ApplyParameters(bool)"/>
        /// </summary>
        /// <remarks>
        /// Equivalent to invoking <c>ApplyParameters(false)</c>
        /// </remarks>
        internal void ApplyParameters()
        {
            this.ApplyParameters(false);
        }
        #endregion

        #region ApplyParameters(bool)
        /// <summary>
        /// Applies the current values in this object's parameter collection
        /// to the internal statement object, if any, that represents the 
        /// prepared form of this command.
        /// </summary>
        /// <remarks>
        /// <para>
        /// If this command is not presently prepared, no action is taken.
        /// </para>
        /// <para>
        /// On the other hand, this operation is invoked internally in whenever
        /// an <c>ExecuteXXX</c> operation is invoked on a prepared
        /// <c>HsqlCommand</c> instance.
        /// </para>
        /// </remarks>
        /// <param name="batch"><c>true</c> to apply the parameters toward batch execution</param>
        /// <exception cref="HsqlDataSourceException">
        /// When unbound parameters exist.  An unbound parameter condition
        /// occurs when a parameter's existence is declared in the command
        /// text using a marker or can be inferred from
        /// the signature of the stored proceduce to be executed, and
        /// the parameter collection of this <c>HsqlCommand</c> contains
        /// no corresponding <c>HsqlParameter</c> instance or the
        /// corresponding <c>HsqlParameter</c> instance exists, but either
        /// it represents an explicitly required (non-defaulted) value binding
        /// site and the value has not explicitly been set or it represents a
        /// non-nullable binding site and its present value is either
        /// implicitly null or has explicily been set null.
        /// </exception>
        internal void ApplyParameters(bool batch)
        {
            HsqlStatement l_statement = m_statement;

            if (l_statement == null)
            {
                return;
            }

            HsqlParameterCollection parameters = m_dbParameterCollection;

            if (parameters == null || parameters.Count == 0)
            {
                int expectedCount = l_statement.ParameterCount;

                if (expectedCount == 0)
                {
                    l_statement.SetParameterValues(m_NoParameters);

                    return;
                }

                throw new HsqlDataSourceException(string.Format(
                    "{0} unbound Parameters Exist.",
                    expectedCount)); // NOI18N
            }

            TokenList tokenList = TokenList;
            int[] bindTypes = l_statement.ParameterTypes;
            object[] values = new object[tokenList.ParameterCount];

            int boundValueCount = 0;

            foreach (HsqlParameter parameter in parameters)
            {
                switch (parameter.Direction)
                {
                    case ParameterDirection.Input:
                    case ParameterDirection.InputOutput:
                        {
                            string name = parameter.ParameterName;

                            int[] bindPositions = tokenList
                                .GetNamedParameterBindPositionsInternal(name);

                            for (int i = 0; i < bindPositions.Length; i++)
                            {
                                int bindPosition = bindPositions[i];
                                int bindType = bindTypes[bindPosition];

                                object value = HsqlConvert.FromDotNet.ToObject(
                                    parameter.Value, bindType);

                                values[bindPosition] = value;

                                boundValueCount++;
                            }

                            break;
                        }
                }
            }

            if (boundValueCount < values.Length)
            {
                int unboundCount = values.Length - boundValueCount;

                throw new HsqlDataSourceException(string.Format(
                    "{0} unbound Parameters Exist.", unboundCount)); // NOI18N
            }

            if (batch)
            {
                l_statement.AddBatch(values);
            }
            else
            {
                l_statement.SetParameterValues(values);
            }
        }
        #endregion

        internal void AddBatchInternal()
        {
            if (IsPrepared)
            {
                ApplyParameters(true);
            }
            else
            {
                List<string> commandTextBatch = m_commandTextBatch;

                if (commandTextBatch == null)
                {
                    commandTextBatch = new List<string>();
                }
                
                commandTextBatch.Add(StaticallyBoundCommandText);

                m_commandTextBatch = commandTextBatch;
            }
        }

        internal int[] ExecuteBatchInternal()
        {
            if (IsPrepared)
            {
                return m_statement.ExecuteBatch(Session);
            }
            else
            {
                List<string> commandTextBatch = m_commandTextBatch;

                if (commandTextBatch == null)
                {
                    throw new HsqlBatchUpdateException(
                        "No commands have been added to the batch", 
                        new InvalidOperationException());
                }

                m_commandTextBatch = null;
                
                return Session.ExecuteNonQueryBatchDirect(commandTextBatch.ToArray());
            }
        }

        #region ExecuteScalarInternal()
        /// <summary>
        /// Provides the core logic for the <see cref="ExecuteScalar()"/> method.
        /// </summary>
        /// <returns>The resulting scalar value</returns>
        internal object ExecuteScalarInternal()
        {
            if (IsPrepared)
            {
                ApplyParameters();

                return m_statement.ExecuteScalar(Session);
            }
            else
            {
                return Session.ExecuteScalarDirect(
                    StaticallyBoundCommandText);
            }
        }
        #endregion

        #region ExecuteReaderInternal(CommandBehavior)
        /// <summary>
        /// Provides the core logic for the <see cref="ExecuteReader(CommandBehavior)"/> method.
        /// </summary>
        /// <param name="behavior">The requested behavior.</param>
        /// <returns>
        /// The result generated by executing the query.
        /// </returns>
        internal HsqlDataReader ExecuteReaderInternal(
            CommandBehavior behavior)
        {
            if (Behavior.IsSchemaOnly(behavior))
            {
                bool wasAlreadyPrepared = IsPrepared;
                
                if (!wasAlreadyPrepared)
                {
                    Prepare(); // already correctly locked.
                }

                HsqlDataReader reader0 = new HsqlDataReader(
                    m_statement.ResultDescriptor, behavior, this,
                    this.m_dbConnection);

                if (Behavior.IsKeyInfo(behavior))
                {
                    // Do it now, so that it does not fail later if
                    // originating connection is closed before first
                    // client invocation of reader.GetSchemaTable().
                    reader0.GetSchemaTable();
                }

                if (!wasAlreadyPrepared)
                {
                    UnPrepare();
                }

                return reader0;
            }

            Result result;

            int maxRows = (Behavior.IsSingleRow(behavior)) ? 1 : 0;

            if (IsPrepared)
            {
                ApplyParameters();

                HsqlSession session = Session;

                session.MaxRows = maxRows;

                result = m_statement.Execute(session);
            }
            else
            {
                HsqlSession session = Session;

                session.MaxRows = maxRows;

                result = session.ExecuteDirect(StaticallyBoundCommandText);
            }

            HsqlDataReader reader = new HsqlDataReader(result, behavior, this,
                this.m_dbConnection);

            if (Behavior.IsKeyInfo(behavior))
            {
                // Do it now, so that it does not fail later if
                // originating connection is closed before first
                // client invocation of reader.GetSchemaTable().
                reader.GetSchemaTable();
            }

            return reader;
        }
        #endregion

        #region ExecuteNonQueryInternal()
        /// <summary>
        /// Executes the non-query.
        /// </summary>
        /// <returns>
        /// The number of rows affected.
        /// </returns>
        internal int ExecuteNonQueryInternal()
        {
            if (IsPrepared)
            {
                ApplyParameters();

                return m_statement.ExecuteNonQuery(Session);
            }
            else
            {
                return Session.ExecuteNonQueryDirect(StaticallyBoundCommandText);
            }
        }
        #endregion

        #region InvalidateStatement()
        /// <summary>
        /// Releases, if present, the underlying <c>HsqlStatement</c> and
        /// makes eligible for garbage collection any related resources.
        /// </summary>
        internal void InvalidateStatement()
        {
            try
            {
                // localize member references to minimize
                // potential race conditions.
                HsqlConnection connection = m_dbConnection;
                HsqlStatement statement = m_statement;

                // Don't leak compiled statement handles
                if (connection != null &&
                    connection.State == ConnectionState.Open &&
                    statement != null)
                {
                    statement.Free(Session);
                }
            }
            finally
            {
                m_statement = null;
                m_tokenList = null;
                m_storedProcedureCommandText = null;
                m_tableDirectCommandText = null;
            }
        }
        #endregion

        #region OnStatementCompleted(int)
        /// <summary>
        /// 
        /// </summary>
        /// <param name="recordCount"></param>
        internal void OnStatementCompleted(int recordCount)
        {
            if (0 <= recordCount)
            {
                StatementCompletedEventHandler handler = this.StatementCompleted;

                if (handler != null)
                {
                    try
                    {
                        handler(this, new StatementCompletedEventArgs(recordCount));
                    }
                    catch (Exception exception)
                    {
                        Type type = exception.GetType();


                        if ((type == typeof(StackOverflowException)) ||
                            (type == typeof(OutOfMemoryException)) ||
                            (type == typeof(ThreadAbortException)) ||
                            (type == typeof(NullReferenceException)) ||
                            (type == typeof(AccessViolationException)))
                        {

                            throw;
                        }

#if TRACE
                        System.Diagnostics.Trace.WriteLine(exception);
#endif
                    }
                }
            }
        } 
        #endregion

        #region PrepareInternal()
        /// <summary>
        /// Provides the core logic for the <see cref="Prepare()"/> method.
        /// </summary>
        internal void PrepareInternal()
        {
            if (!IsPrepared)
            {
                string sql;
                HsqlSession session = this.Session;

                switch (m_commandType)
                {
                    case CommandType.StoredProcedure:
                        {
                            sql = StoredProcedureCommandText;

                            break;
                        }
                    case CommandType.Text:
                        {
                            sql = DefaultParameterMarkerCommandText;

                            break;
                        }
                    case CommandType.TableDirect:
                        {
                            sql = TableDirectCommandText;
                            break;
                        }
                    default:
                        {
                            throw new HsqlDataSourceException(
                                "CommandType not supported: "
                                + m_commandType); // NOI18N
                        }
                }

                m_statement = session.PrepareStatement(sql);
                m_commandTextBatch = null;
            }
        }
        #endregion

        #endregion

        #region Private Methods

        #region ConnectionStateChanged(object,StateChangeEventArgs)

        /// <summary>
        /// Signals that the state of this object's associated connection
        /// has changed.
        /// </summary>
        /// <param name="sender">The sender.</param>
        /// <param name="e">
        /// The <see cref="StateChangeEventArgs"/>
        /// containing the event data.
        /// </param>
        /// <exception cref="HsqlDataSourceException">
        /// If transition to the new value invalidates the prepared
        /// state of this command, then any exception raised as
        /// a result is rethrown here.
        /// </exception>
        private void ConnectionStateChanged(object sender, 
            StateChangeEventArgs e)
        {
            if (sender == m_dbConnection)
            {
                switch (e.CurrentState)
                {
                    case ConnectionState.Broken:
                    case ConnectionState.Closed:
                        {
                            InvalidateStatement();

                            break;
                        }
                }
            }
        }

        #endregion

        #endregion

        #region Private Properties

        #region StaticallyBoundCommandText
        /// <summary>
        /// Computes and retrieves a character sequence that is the
        /// representation of the current set of <b>Parameters</b>
        /// statically bound to the current <b>CommandText</b>.
        /// </summary>
        /// <value>The statically bound command text.</value>
        private string StaticallyBoundCommandText
        {
            get
            {
                if (!m_commandTextHasParameters)
                {
                    return m_commandText;
                }

                HsqlParameterCollection parameters = m_dbParameterCollection;
                int parameterTokenCount = TokenList.ParameterCount;

                bool unboundParameterTokensExist = (parameterTokenCount > 0) &&
                    ((null == parameters) || (parameters.Count == 0));

                if (unboundParameterTokensExist)
                {
                    throw new HsqlDataSourceException(string.Format(
                        "{0} unbound parameter tokens exist.",
                        parameterTokenCount),
                        org.hsqldb.Trace.JDBC_PARAMETER_NOT_SET,
                        "00000");
                }
                                
                return TokenList.ToStaticallyBoundForm(parameters, /*strict*/ true);
            }
        }
        #endregion

        #region DefaultParameterMarkerCommandText
        /// <summary>
        /// Gets the default parameter marker form command text.
        /// </summary>
        /// <value>The default parameter marker form command text.</value>
        private string DefaultParameterMarkerCommandText
        {
            get
            {
                return (m_commandTextHasParameters)
                    ? TokenList.ToDefaultParameterMarkerForm() : m_commandText;
            }
        }
        #endregion

        #region StoredProcedureCommandText
        /// <summary>
        /// Gets a character sequence representing the
        /// SQL statement that is executed when the
        /// <c>CommandType</c> is <c>StoredProcedure</c>
        /// </summary>
        /// <value>
        /// An SQL statement that calls the stored procedure
        /// identified by <c>CommandText</c>.
        /// </value>
        private string StoredProcedureCommandText
        {
            get
            {
                if (m_storedProcedureCommandText != null)
                {
                    return m_storedProcedureCommandText;
                }

                string commandText = CommandText;

                if (string.IsNullOrEmpty(commandText))
                {
                    throw new HsqlDataSourceException(string.Format(
                        "Invalid StoredProcedure Command Text: '{0}'",
                        commandText)); // NOI18N
                }

                TokenList tokenList = new TokenList(
                    commandText);

                if (tokenList.Count == 0)
                {
                    throw new HsqlDataSourceException(string.Format(
                        "Invalid StoredProcedure Command Text: '{0}'",
                        commandText)); // NOI18N
                }

                if (tokenList.Count > 1)
                {
                    // Assume the full call syntax was
                    // provided in CommandText.
                    return commandText;
                }

                Token token = tokenList[0];
                string schema;
                string spName;

                switch (token.Type)
                {
                    case SqlTokenType.IdentifierChain:
                        {
                            schema = token.QualifierPart;
                            spName = token.SubjectPart;
                            break;

                        }
                    case SqlTokenType.DelimitedIdentifier:
                    case SqlTokenType.Name:
                        {
                            schema = Connection.DefaultSchema;
                            spName = token.Value;
                            break;
                        }
                    default:
                        {
                            throw new HsqlDataSourceException(string.Format(
                                "Invalid StoredProcedure Command Text: '{0}'",
                                commandText)); // NOI18N
                        }
                }

                string query =
@"-- System.Data.Hsqldb.Client.HsqlCommand.StoredProcedureCommandText:
SELECT DISTINCT p.specific_name
  FROM information_schema.system_procedures p
 WHERE (p.procedure_schem = ?)
   AND ((p.procedure_name = ?) OR (p.specific_name = ?))";

                HsqlSession session = Session;
                HsqlStatement stmt = null;
                Result result = null;
                HsqlDataReader reader = null;                

                try
                {
                    stmt = session.PrepareStatement(query);
                    
                    stmt.SetParameterValues(schema, spName, spName);
                    
                    result = stmt.Execute(session);
                    reader = new HsqlDataReader(result);
                }
                finally
                {
                    if (stmt != null)
                    {
                        stmt.Free(session);
                    }
                }

                if (!reader.Read())
                {
                    throw new HsqlDataSourceException(string.Format(
                        "No such stored procedure: '{0}'",
                        commandText)); // NOI18N
                }

                string specificName = reader.GetString(0);

                if (reader.Read())
                {
                    string secondSpecificName = reader.GetString(0);

                    throw new HsqlDataSourceException(string.Format(
                        "Ambiguous stored procedure name specified: '{0}'. " 
                      + "At least two corresponding specific names exist: '{1}', '{2}'",
                        commandText, 
                        specificName, 
                        secondSpecificName)); // NOI18N
                }

                DataTable table = Connection.MetaData.GetSchema(
                    HsqlMetaDataCollectionNames.ProcedureParameters,
                    new string[] { null, schema, specificName, null });

                int parameterCount = table.Rows.Count;

                StringBuilder sb = new StringBuilder();

                sb.Append("CALL ").Append(token.Value).Append('(');

                for (int i = 0; i < parameterCount; i++)
                {
                    if (i > 0)
                    {
                        sb.Append(',');
                    }

                    sb.Append('?');
                }

                sb.Append(')');

                m_storedProcedureCommandText = sb.ToString();

                return m_storedProcedureCommandText;
            }
        }
        #endregion

        #region TableDirectCommandText
        /// <summary>
        /// Gets a character sequence representing the
        /// SQL statement that is executed when the
        /// <c>CommandType</c> is <c>TableDirect</c>
        /// </summary>
        /// <value>
        /// An SQL statement that selects all rows and
        /// columns from the table identified by
        /// <c>CommandText</c>.
        /// </value>
        private string TableDirectCommandText
        {
            get
            {
                if (m_tableDirectCommandText != null)
                {
                    return m_tableDirectCommandText;
                }

                string commandText = CommandText;

                if (string.IsNullOrEmpty(commandText))
                {
                    throw new HsqlDataSourceException(string.Format(
                        "Invalid TableDirect Command Text: '{0}'",
                        commandText)); // NOI18N
                }

                TokenList tokenList = new TokenList(commandText);

                if (tokenList.Count != 1)
                {
                    throw new HsqlDataSourceException(string.Format(
                        "Invalid TableDirect Command Text: '{0}'",
                        CommandText)); // NOI18N
                }

                Token token = tokenList[0];
                string schema;
                string tableName;

                switch (token.Type)
                {
                    case SqlTokenType.IdentifierChain:
                        {
                            schema = token.QualifierPart;
                            tableName = token.SubjectPart;
                            break;

                        }
                    case SqlTokenType.DelimitedIdentifier:
                    case SqlTokenType.Name:
                        {
                            schema = Connection.DefaultSchema;
                            tableName = token.Value;
                            break;
                        }
                    default:
                        {
                            throw new HsqlDataSourceException(string.Format(
                                "Invalid TableDirect Command Text: '{0}'",
                                CommandText)); // NOI18N
                        }
                }

                StringBuilder sb = new StringBuilder();

                string delim = "\"";
                string idsep = ".";
                string delimesc = delim + delim;

                m_tableDirectCommandText = sb.Append("SELECT * FROM ")
                    .Append(delim)
                    .Append(schema.Replace(delim, delimesc))
                    .Append(delim)
                    .Append(idsep)
                    .Append(delim)
                    .Append(tableName.Replace(delim, delimesc))
                    .Append(delim)
                    .ToString();

                return m_tableDirectCommandText;
            }
        }
        #endregion

        #region Session
        private HsqlSession Session
        {
            get
            {
                HsqlConnection connection = m_dbConnection;

                if (connection == null)
                {
                    throw new InvalidOperationException(
                        "Connection Not Set."); // NOI18N
                }

                return connection.Session;
            }
        }
        #endregion

        #region TokenList
        /// <summary>
        /// Gets a token list representing the current
        /// <c>CommandText</c> value.
        /// </summary>
        /// <value>
        /// The current <c>CommandText</c> as a token list.
        /// </value>
        private TokenList TokenList
        {
            get
            {               
                if (m_tokenList == null)
                {
                    string commandText = m_commandText;

                    if (commandText == null)
                    {
                        commandText = string.Empty;
                    }

                    m_tokenList = new TokenList(commandText);
                }

                return m_tokenList;
            }
        }
        #endregion

        #endregion

        #endregion

        #region Static Members

        #region Private Methods
        
        #region IsNumberType(int)
        private static bool IsNumberType(int type)
        {
            return HsqlTypes.isNumberType(type);
        }
        #endregion

        #region IsCharacterType(int)
        private static bool IsCharacterType(int type)
        {
            return HsqlTypes.isCharacterType(type);
        }
        #endregion

        #region IsTemporalType(int)
        private static bool IsTemporalType(int type)
        {
            return (type == HsqlTypes.DATE)
                   || (type == HsqlTypes.TIME)
                   || (type == HsqlTypes.TIMESTAMP);
        }
        #endregion

        #region IsFixedLengthType(int)
        //private static bool IsFixedLengthType(int type)
        //{
        //    switch (type)
        //    {
        //        case Types.ARRAY:
        //        case Types.BLOB:
        //        case Types.CLOB:
        //        case Types.DATALINK:
        //        case Types.DISTINCT:
        //        case Types.JAVA_OBJECT:
        //        case Types.LONGVARBINARY:
        //        case Types.LONGVARCHAR:
        //        case Types.OTHER:
        //        case Types.REF:
        //        case Types.STRUCT:
        //        case Types.VARBINARY:
        //        case Types.VARCHAR:
        //        case Types.XML:
        //            {
        //                return false;
        //            }
        //        default:
        //            {
        //                return true;
        //            }
        //    }
        //} 
        #endregion

        #region ToParameterDirection(int)
        //private static ParameterDirection? ToParameterDirection(ParameterMode mode)
        //{
        //    switch (mode)
        //    {
        //        case ParameterMode.In:
        //            {
        //                return ParameterDirection.Input;
        //            }
        //        case ParameterMode.InOut:
        //            {
        //                return ParameterDirection.InputOutput;
        //            }
        //        case ParameterMode.Out:
        //            {
        //                return ParameterDirection.Output;
        //            }
        //        case ParameterMode.Unknown:
        //        default:
        //            {
        //                return null;
        //            }
        //    }
        //}
        #endregion

        #region IsNullable(int)
        private static bool? IsNullable(int nullability)
        {
            switch (nullability)
            {
                case PMD.parameterNoNulls:
                    {
                        return false;
                    }
                case PMD.parameterNullable:
                    {
                        return true;
                    }
                default:
                    {
                        return null;
                    }
            }
        }
        #endregion

        #region  HasPrecision(int)
        //private static bool HasPrecision(int type)
        //{
        //    return Types.acceptsPrecisionCreateParam(type);
        //} 
        #endregion

        #region HasScale(int)
        //private static bool HasScale(int type)
        //{
        //    return Types.acceptsScaleCreateParam(type);
        //} 
        #endregion

        #region ToDefaultSize(int)
        private static int ToDefaultSize(int type)
        {
            return HsqlTypes.getMaxDisplaySize(type);
        }
        #endregion

        #region ToDefaultPrecision(int)
        private static int ToDefaultPrecision(int type)
        {
            return HsqlTypes.getPrecision(type);
        }
        #endregion 

        static CommandType ToSupportedCommandType(CommandType commandType)
        {
            switch (commandType)
            {
                case CommandType.StoredProcedure:
                case CommandType.TableDirect:
                case CommandType.Text:
                    {
                        return commandType;
                    }
                default:
                    {
                        return CommandType.Text;
                    }
            }
        }

        #endregion

        #endregion
    }

    #endregion
}