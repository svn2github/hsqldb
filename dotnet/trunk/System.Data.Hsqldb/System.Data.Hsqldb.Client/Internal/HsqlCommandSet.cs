using System;
using System.Collections.Generic;
using System.Data;
using System.Data.Common;
using System.Text;
using System.Text.RegularExpressions;
using System.Data.Hsqldb.Client.MetaData.Collection;
using System.Data.Hsqldb.Common;

namespace System.Data.Hsqldb.Client.Internal
{
    internal sealed class HsqlCommandSet
    {
        private HsqlCommand m_batchCommand = new HsqlCommand();
        private List<LocalCommand> m_commandList = new List<LocalCommand>();
        private static int m_objectTypeCount;
        private const string IdentifierPattern 
            = DataSourceInformationCollection.ValueOf.ParameterMarkerPattern;
        private static readonly Regex IdentifierParser = new Regex(
            IdentifierPattern,
            RegexOptions.Singleline | RegexOptions.ExplicitCapture);
        
        bool m_isDisposed;

        internal HsqlCommandSet()
        {
        }

        internal void Append(HsqlCommand command)
        {
            HsqlParameterCollection parameters;

            if (command == null)
            {
                throw new ArgumentNullException(
                    "command");
            }

            string commandText = command.CommandText;

            if (string.IsNullOrEmpty(commandText))
            {
                throw new ArgumentException("Command Text Required",
                    "command.CommandText");
            }

            CommandType commandType = command.CommandType;

            switch (commandType)
            {
                case CommandType.Text:
                case CommandType.StoredProcedure:
                    {
                        parameters = null;
                        HsqlParameterCollection commandParameters = command.Parameters;
                        int parameterCount = commandParameters.Count;

                        if (parameterCount > 0)
                        {
                            parameters = new HsqlParameterCollection();

                            for (int i = 0; i < parameterCount; i++)
                            {
                                HsqlParameter destination = commandParameters[i].Clone();

                                parameters.Add(destination);

                                if (!IdentifierParser.IsMatch(destination
                                    .ParameterName))
                                {
                                    throw new HsqlDataSourceException(
                                        "Bad Parameter Name",
                                        org.hsqldb.Trace.GENERAL_ERROR,"S1000");
                                }
                            }
                        }
                        break;
                    }
                case CommandType.TableDirect:
                    {
                        throw new ArgumentOutOfRangeException(
                            "command.CommandType", commandType,
                            "Enumeration Value Not Supported.");
                    }
                default:
                    {
                        throw new ArgumentOutOfRangeException(
                            "command.CommandType", commandType,
                            "Invalid Enumeration Value");
                    }
            }

            int returnParameterIndex = -1;

            for (int j = 0; j < parameters.Count; j++)
            {
                if (ParameterDirection.ReturnValue == parameters[j].Direction)
                {
                    returnParameterIndex = j;
                    break;
                }
            }

            LocalCommand item = new LocalCommand(
                commandText,
                parameters,
                returnParameterIndex,
                commandType);

            m_commandList.Add(item);
        }

        internal void Clear()
        {
            DbCommand command = m_batchCommand;

            if (command != null)
            {
                command.Parameters.Clear();
                command.CommandText = null;
            }

            List<LocalCommand> list = this.m_commandList;

            if (list != null)
            {
                list.Clear();
            }
        }

        internal void Dispose()
        {
            m_isDisposed = true;

            HsqlCommand command = m_batchCommand;            
            
            m_commandList = null;
            m_batchCommand = null;
            
            if (command != null)
            {
                command.Dispose();
            }
        }

        internal int ExecuteNonQuery()
        {
            int updateCount = 0;
            
            for (int i = 0; i < this.m_commandList.Count; i++)
            {
                LocalCommand command = m_commandList[i];
            }

            return updateCount;
        }

        internal HsqlParameter GetParameter(
            int commandIndex, 
            int parameterIndex)
        {
            return m_commandList[commandIndex].Parameters[parameterIndex];
        }

        internal int GetParameterCount(int commandIndex)
        {
            return m_commandList[commandIndex].Parameters.Count;
        }

        private HsqlCommand BatchCommand
        {
            get
            {
                HsqlCommand command = m_batchCommand;
                
                if (m_isDisposed || command == null)
                {
                    throw new ObjectDisposedException(GetType().Name);
                }
                
                return command;
            }
        }

        internal bool GetBatchedAffected(int commandIdentifier, out int recordsAffected, out Exception error)
        {
            //error = this.BatchCommand.GetErrors(commandIdentifier);
            //int? nullable = this.BatchCommand.GetRecordsAffected(commandIdentifier);
            //recordsAffected = nullable.GetValueOrDefault();
            //return nullable.HasValue;

            throw new InvalidOperationException();
        }

        internal int CommandCount
        {
            get
            {
                return this.CommandList.Count;
            }
        }

        private List<LocalCommand> CommandList
        {
            get
            {
                List<LocalCommand> list = m_commandList;
                
                if (m_isDisposed || list == null)
                {
                    throw new ObjectDisposedException(GetType().Name);
                }
                
                return list;
            }
        }

        internal int CommandTimeout
        {
            set { this.BatchCommand.CommandTimeout = value;  }
        }

        internal HsqlConnection Connection
        {
            get { return this.BatchCommand.Connection;  }
            set { this.BatchCommand.Connection = value; }
        }

        internal HsqlTransaction Transaction
        {
            set { this.BatchCommand.Transaction = value; }
        }

        private sealed class LocalCommand
        {
            internal readonly CommandType CmdType;
            internal readonly string CommandText;
            internal readonly HsqlParameterCollection Parameters;
            internal readonly int ReturnParameterIndex;

            internal LocalCommand(
                string commandText,
                HsqlParameterCollection parameters,
                int returnParameterIndex,
                CommandType cmdType)
            {
                this.CommandText = commandText;
                this.Parameters = parameters;
                this.ReturnParameterIndex = returnParameterIndex;
                this.CmdType = cmdType;
            }
        }
    }
}
