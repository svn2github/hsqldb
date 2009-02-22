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


#endregion

namespace System.Data.Hsqldb.Client
{
    #region HsqlCommand

    /// <summary>
    /// <para>
    /// The HSQLDB <see cref="DbCommand">DbCommand</see> implementation.
    /// </para>
    /// <img src="../Documentation/ClassDiagrams/System.Data.Hsqldb.Client.HsqlCommand.png"
    ///      alt="HsqlCommand Class Diagram"/>
    /// </summary>
    /// <author name="boucherb@users"/>
    [DefaultEvent("RecordsAffected")]
    [Designer("Microsoft.VSDesigner.Data.VS.SqlCommandDesigner, Microsoft.VSDesigner, Version=8.0.0.0, Culture=neutral, PublicKeyToken=b03f5f7f11d50a3a")]
    [ToolboxItem(true)]
#if W32DESIGN  
    [ToolboxBitmap(typeof(resfinder), "System.Data.Hsqldb.Client.Image.Bmp.HsqlCommand.bmp")]
#endif
    public sealed partial class HsqlCommand : DbCommand, ICloneable
    {
        #region Constructors

        #region HsqlCommand()

        /// <summary>
        /// Constructs a new <c>HsqlCommand</c> instance that not initially
        /// associated with any connection.
        /// </summary>
        /// <remarks>
        /// <para>
        /// The initial <c>CommandType</c> is 
        /// <see cref="System.Data.CommandType.Text"/>
        /// and the initial <c>CommandText</c> is empty.
        /// </para>
        /// <para>
        /// An instance constructed in such a fashion must
        /// first be associated with an open connection and
        /// have meaningful <c>CommandText</c> assigned the
        /// before it can be executed successfully.
        /// </para>
        /// </remarks>
        public HsqlCommand() : base()
        {
            GC.SuppressFinalize(this);
        }

        #endregion

        #region HsqlCommand(HsqlConnection)

        /// <summary>
        /// Constructs a new <c>HsqlCommand</c> instance that is initially
        /// associated with the given connection.
        /// </summary>
        /// <remarks>
        /// The initial <c>CommandType</c> is <see cref="System.Data.CommandType.Text"/>
        /// and the initial <c>CommandText</c> is empty.
        /// </remarks>
        /// <param name="connection">
        /// The connection with which this command is initially associated.
        /// </param>
        public HsqlCommand(HsqlConnection connection) : this()
        {
            Connection = connection;
        }

        #endregion

        #region HsqlCommand(HsqlConnection,string)

        /// <summary>
        /// Constructs a new <c>HsqlCommand</c> instance with the given
        /// connection and command text.
        /// </summary>
        /// <remarks>
        /// The initial <c>CommandType</c> is <see cref="System.Data.CommandType.Text"/>
        /// </remarks>
        /// <param name="connection">
        /// The connection with which this command is initially associated.
        /// </param>
        /// <param name="commandText">
        /// The initial command text.
        /// </param>
        public HsqlCommand(HsqlConnection connection, string commandText)
            : this(connection)
        {
            CommandText = commandText;
        }

        #endregion

        #region HsqlCommand(HsqlConnection,string,CommandType)

        /// <summary>
        /// Constructs a new <c>HsqlCommand</c> instance with the given
        /// connection, command text and command type.
        /// </summary>
        /// <param name="connection">
        /// The connection with which this command is initially associated.
        /// </param>
        /// <param name="commandText">
        /// The initial command text to execute.
        /// </param>
        /// <param name="commandType">
        /// The way in which to initially interpret the command text.
        /// </param>
        public HsqlCommand(HsqlConnection connection, String commandText,
            CommandType commandType)
            : this(connection, commandText)
        {
            m_commandType = HsqlCommand.ToSupportedCommandType(CommandType);
        }

        #endregion

        #endregion

        #region Inner Classes
        
        #region Behavior
        /// <summary>
        /// 
        /// </summary>
        public static class Behavior
        {
            #region IsCloseConnection(CommandBehavior)
            /// <summary>
            /// Determines whether the <c>CloseConnection</c> flag is set in the
            /// specified <c>commandBehavior</c>.
            /// </summary>
            /// <param name="commandBehavior">The value to test.</param>
            /// <returns>
            /// <c>true</c> if <c>CloseConnection</c> flag is set; otherwise, <c>false</c>.
            /// </returns>
            public static bool IsCloseConnection(CommandBehavior commandBehavior)
            {
                return 0 != (commandBehavior & CommandBehavior.CloseConnection);
            }
            #endregion

            #region IsKeyInfo(CommandBehavior)
            /// <summary>
            /// Determines whether the <c>KeyInfo</c> flag is set in the
            /// specified <c>commandBehavior</c>.
            /// </summary>
            /// <param name="commandBehavior">The value to test.</param>
            /// <returns>
            /// <c>true</c> if <c>KeyInfo</c> flag is set; otherwise, <c>false</c>.
            /// </returns>
            public static bool IsKeyInfo(CommandBehavior commandBehavior)
            {
                return 0 != (commandBehavior & CommandBehavior.KeyInfo);
            }
            #endregion

            #region IsSchemaOnly(CommandBehavior)
            /// <summary>
            /// Determines whether the <c>SchemaOnly</c> flag is set in the
            /// specified <c>commandBehavior</c>.
            /// </summary>
            /// <param name="commandBehavior">The value to test.</param>
            /// <returns>
            /// <c>true</c> if <c>SchemaOnly</c> flag is set; otherwise, <c>false</c>.
            /// </returns>
            public static bool IsSchemaOnly(CommandBehavior commandBehavior)
            {
                return 0 != (commandBehavior & CommandBehavior.SchemaOnly);
            }
            #endregion

            #region IsSequentialAccess(CommandBehavior)
            /// <summary>
            /// Determines whether the <c>SequentialAccess</c> flag is set in the
            /// specified <c>commandBehavior</c>.
            /// </summary>
            /// <param name="commandBehavior">The value to test.</param>
            /// <returns>
            /// <c>true</c> if <c>SequentialAccess</c> flag is set; otherwise, <c>false</c>.
            /// </returns>
            public static bool IsSequentialAccess(CommandBehavior commandBehavior)
            {
                return 0 != (commandBehavior & CommandBehavior.SchemaOnly);
            }
            #endregion

            #region IsSingleResult(CommandBehavior)
            /// <summary>
            /// Determines whether the <c>SingleResult</c> flag is set in the
            /// specified <c>commandBehavior</c>.
            /// </summary>
            /// <param name="commandBehavior">The value to test.</param>
            /// <returns>
            /// <c>true</c> if <c>SingleResult</c> flag is set; otherwise, <c>false</c>.
            /// </returns>
            public static bool IsSingleResult(CommandBehavior commandBehavior)
            {
                return 0 != (commandBehavior & CommandBehavior.SingleResult);
            }
            #endregion

            #region IsSingleRow(CommandBehavior)
            /// <summary>
            /// Determines whether the <c>SingleRow</c> flag is set in the
            /// specified <c>commandBehavior</c>.
            /// </summary>
            /// <param name="commandBehavior">The value to test.</param>
            /// <returns>
            /// <c>true</c> if <c>SingleRow</c> flag is set; otherwise, <c>false</c>.
            /// </returns>
            public static bool IsSingleRow(CommandBehavior commandBehavior)
            {
                return 0 != (commandBehavior & CommandBehavior.SingleRow);
            }
            #endregion
        }
        #endregion 
        
        #endregion
    }

    #endregion
}