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
using System.Collections.Generic;
using System.Diagnostics;
using System.Data.Hsqldb.Client;
using System.Data.Hsqldb.Common.Enumeration; 
#endregion

namespace System.Data.Hsqldb.Client.Design.Converter
{
    #region InitialSchemaConverter
    /// <summary>
    /// A <c>StringConverter</c> whose standard values collection is populated
    /// by executing an SQL query against the database connection denoted by the
    /// <see cref="HsqlConnectionStringBuilder"/> supplied through the <c>Instance</c> 
    /// property of the given <see cref="ITypeDescriptorContext"/>.
    /// </summary>
    public class InitialSchemaConverter : StringConverter
    {
        #region Constants
        const string SchemaQuery =
@"SELECT TABLE_SCHEM 
    FROM INFORMATION_SCHEMA.SYSTEM_SCHEMAS
ORDER BY 1";
        #endregion

        #region TypeConverter Method Overrides

        #region GetStandardValues(ITypeDescriptorContext)
        /// <summary>
        /// Returns the collection of currently valid initial schema names, 
        /// given the specified context.
        /// </summary>
        /// <param name="context">
        /// An <see cref="ITypeDescriptorContext"></see> whose <c>Instance</c> 
        /// property supplies the <c>HsqlConnectionStringBuilder</c> use to 
        /// connect to a data source to retrieve the currently valid initial 
        /// schema names.
        /// </param>
        /// <returns>
        /// A <see cref="TypeConverter.StandardValuesCollection"/> that holds 
        /// collection of currently valid initial schema names.
        /// </returns>
        public override TypeConverter.StandardValuesCollection GetStandardValues(
            ITypeDescriptorContext context)
        {
            if (!IsStandardValuesSupported(context))
            {
                return null;
            }

            List<string> values = new List<string>();

            try
            {
                HsqlConnectionStringBuilder builder
                    = (HsqlConnectionStringBuilder)context.Instance;

                // TODO:  this is sub-optimal, but is currently the best (only?)
                // solution to the problem of how to avoid creating and/or
                // leaving open embedded database instances.
                if (IsEmbeddedProtocol(builder))
                {
                    builder = new HsqlConnectionStringBuilder(
                        builder.ConnectionString);

                    builder.AutoShutdown = true;
                    builder.IfExists = true;
                }

                using (HsqlConnection connection = new HsqlConnection())
                {
                    connection.ConnectionString = builder.ConnectionString;

                    using (HsqlCommand command = new HsqlCommand(
                        connection,
                        SchemaQuery))
                    {
                        connection.Open();

                        using (HsqlDataReader reader = command.ExecuteReader())
                        {
                            while (reader.Read())
                            {
                                values.Add(reader.GetString(0));
                            }
                        }
                    }
                }
            }
            catch (Exception exception)
            {
#if DEBUG
                Debug.WriteLine(exception);
#endif
            }

            return new TypeConverter.StandardValuesCollection(values);
        }
        #endregion

        #region GetStandardValuesExclusive(ITypeDescriptorContext)
        /// <summary>
        /// Returns whether the collection of standard values returned from 
        /// <see cref="GetStandardValues"/> is an exclusive list of possible values, 
        /// using the specified context.
        /// </summary>
        /// <param name="context">
        /// An <see cref="ITypeDescriptorContext"/> that provides a format context.
        /// </param>
        /// <returns>
        /// Always <c>false</c>, so that a user can type in whatever they want.
        /// </returns>
        public override bool GetStandardValuesExclusive(ITypeDescriptorContext context)
        {
            return false;
        }
        #endregion

        #region GetStandardValuesSupported(ITypeDescriptorContext)
        /// <summary>
        /// Returns whether this object can populate the the list of valid schema names
        /// using the specified context.
        /// </summary>
        /// <param name="context">
        /// An <see cref="ITypeDescriptorContext"/> that provides a format context.
        /// </param>
        /// <returns>
        /// <c>true</c> if <see cref="GetStandardValues"/> should be called to find 
        /// the list of valid schema names; otherwise, <c>false</c>.
        /// </returns>
        public override bool GetStandardValuesSupported(ITypeDescriptorContext context)
        {
            return IsStandardValuesSupported(context);
        }
        #endregion 
        
        #endregion

        #region Static Helper Methods

        #region IsStandardValuesSupported(ITypeDescriptorContext)
        /// <summary>
        /// Retrieves whether population of the standard values collection
        /// is supported for the given context.
        /// </summary>
        /// <param name="context">The context.</param>
        /// <returns>
        /// <c>true</c> if <see cref="GetStandardValues"/> should be called to find 
        /// the list of valid schema names; otherwise, <c>false</c>.
        /// </returns>
        public static bool IsStandardValuesSupported(ITypeDescriptorContext context)
        {
            return (context != null)
                && (context.Instance is HsqlConnectionStringBuilder);
        }
        #endregion

        #region IsEmbeddedProtocol(HsqlConnectionStringBuilder)
        /// <summary>
        /// Determines whether the given builder specifies an 
        /// embedded connection protocol
        /// </summary>
        /// <param name="builder">The builder.</param>
        /// <returns>
        /// <c>true</c> if the builder specified an embedded connection protocol;
        /// otherwise, <c>false</c>.
        /// </returns>
        public static bool IsEmbeddedProtocol(HsqlConnectionStringBuilder builder)
        {
            switch (builder.Protocol)
            {
                case ConnectionProtocol.File:
                case ConnectionProtocol.Mem:
                case ConnectionProtocol.Res:
                    {
                        return true;
                    }
                default:
                    {
                        return false;
                    }
            }
        }
        #endregion

        #endregion
    } 
    #endregion
}

