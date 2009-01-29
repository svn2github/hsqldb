#region licence

/* Copyright (c) 2001-2008, The HSQL Development Group
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
#endregion

namespace System.Data.Hsqldb.Client.MetaData.Collection.Base
{
    #region CachedMetaDataCollection
    /// <summary>
    /// <para>
    /// <c>BaseCollection</c> variant that lazily loads and
    /// caches the metadata collection it produces, returning
    /// the cached collection.
    /// </para>
    /// <img src="/ClassDiagrams/System.Data.Hsqldb.Client.MetaData.Collection.BaseCachedCollection.png" 
    ///      alt="BaseCachedCollection Class Diagram"/>
    /// </summary>
    /// <author name="boucherb@users"/>
    public abstract class CachedMetadataCollection : MetaDataCollection
    {
        #region Fields
        /// <summary>
        /// The cached metadata collection.
        /// </summary>
        private DataTable m_table;
        #endregion

        #region CachedMetadataCollection()
        /// <summary>
        /// Initializes a new instance of the
        /// <see cref="CachedMetadataCollection"/> class.
        /// </summary>
        public CachedMetadataCollection()
            : base()
        {
        }
        #endregion

        #region GetSchema(HsqlConnection, string[])
        /// <summary>
        /// Gets the schema.
        /// </summary>
        /// <param name="connection">Representing data source</param>
        /// <param name="restrictions">
        /// The restrictions; typically ignored.
        /// </param>
        /// <returns>
        /// A cached version of the underlying metadata collection.
        /// The underlying collection is lazily loaded and cached
        /// for reuse for the lifetime of this object.
        /// </returns>
        public override DataTable GetSchema(
            HsqlConnection connection,
            string[] restrictions)
        {
            if (m_table == null)
            {
                m_table = CreateTable();

                FillTable(connection, m_table, restrictions);

                foreach (DataColumn column in m_table.Columns)
                {
                    column.ReadOnly = true;
                }
            }

            return m_table;
        }
        #endregion
    }
    #endregion
}