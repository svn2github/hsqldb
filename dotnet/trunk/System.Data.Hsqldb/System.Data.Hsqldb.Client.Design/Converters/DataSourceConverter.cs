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
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Data.Hsqldb.Client;
using System.Data.Hsqldb.Client.MetaData.Collection;
using System.Text;
// aliases
using DSEC = System.Data.Hsqldb.Client.MetaData.Collection.DataSourceEnumerationCollection;
#endregion

namespace System.Data.Hsqldb.Client.Design.Converter
{
    #region DataSourceConverter
    /// <summary>
    /// A <c>StringConverter</c> whose standard values collection is populated
    /// using the HSQLDB <see cref="DataSourceEnumerationCollection"/>.
    /// </summary>
    public class DataSourceConverter : StringConverter
    {
        #region GetStandardValues(ITypeDescriptorContext)
        /// <summary>
        /// Returns the collection of standard values for 
        /// the <see cref="HsqlConnectionStringBuilder.DataSource"/>
        /// property.
        /// </summary>
        /// <param name="context">Which may hold</param>
        /// <returns>
        /// The standard set of valid values.
        /// </returns>
        public override TypeConverter.StandardValuesCollection GetStandardValues(
            ITypeDescriptorContext context)
        {
            DataTable dataSources = DSEC.GetDataSources();
            DataRowCollection rows = dataSources.Rows;
            int rowCount = rows.Count;
            string[] array;

            if (context != null && context.Instance != null)
            {
                // might be a connection string builder or wrapper for one.
                try
                {
                    HsqlConnectionStringBuilder csb
                        = new HsqlConnectionStringBuilder(
                        Convert.ToString(context.Instance));

                    array = new string[rowCount + 1];

                    array[rowCount] = csb.DataSource;
                }
                catch (Exception)
                {
                    array = new string[rowCount];
                }
            }
            else
            {
                array = new string[rowCount];
            }

            for (int i = 0; i < rowCount; i++)
            {
                DataRow row = rows[i];

                string prefix = row[DSEC.ServerNameColumnOrdinal] as string;
                string suffix = row[DSEC.InstanceNameColumnOrdinal] as string;
                string version = row[DSEC.VersionColumnOrdinal] as string;

                if (string.IsNullOrEmpty(version) ||
                    version.StartsWith("1.8.0", StringComparison.Ordinal))
                {
                    array[i] = (string.IsNullOrEmpty(suffix))
                        ? prefix.Replace('\\', '/')
                        : new StringBuilder(prefix)
                        .Replace('\\', '/')
                        .Append('/')
                        .Append(suffix.Replace('\\', '/').TrimStart('/'))
                        .ToString();
                }
                else
                {
                    array[i] = string.Empty;
                }
            }

            org.hsqldb.lib.HashSet set = new org.hsqldb.lib.HashSet();

            set.addAll(array);

            set.remove(string.Empty);

            if (array.Length != set.size())
            {
                array = new string[set.size()];
            }

            set.toArray(array);

            Array.Sort<string>(array);

            return new TypeConverter.StandardValuesCollection(array);
        } 
        #endregion

        #region GetStandardValuesExclusive(ITypeDescriptorContext)
        /// <summary>
        /// Returns whether the collection of standard values 
        /// returned from <see cref="GetStandardValues"/> is an
        /// exclusive list of possible values, using the specified
        /// context.
        /// </summary>
        /// <remarks>
        /// In other words, controls whether a control using this converter to provide the drop-down list 
        /// disables (return <c>true</c>) or disables (return <c>false</c>) manual data entry  or enables manual  as well as list item selection.
        /// </remarks>
        /// <param name="context">
        /// An <see cref="ITypeDescriptorContext"></see> that 
        /// provides a format context.
        /// </param>
        /// <returns>
        /// false
        /// </returns>
        public override bool GetStandardValuesExclusive(ITypeDescriptorContext context)
        {
            return false;
        }
        #endregion

        #region GetStandardValuesSupported(ITypeDescriptorContext)
        /// <summary>
        /// Returns whether this object supports a standard set of values that can 
        /// be picked from a list, using the specified context.
        /// </summary>
        /// <param name="context">
        /// An <see cref="ITypeDescriptorContext"/> that provides a format context.
        /// </param>
        /// <returns>
        /// true.
        /// </returns>
        public override bool GetStandardValuesSupported(ITypeDescriptorContext context)
        {
            return true;
        }
        #endregion
    } 
    #endregion
}
