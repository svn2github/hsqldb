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

namespace System.Data.Hsqldb.Client.MetaData
{
    #region HsqlMetaDataColumnNames

    /// <summary>
    /// <para>
    /// Provides <c>MetaDataCollection</c> column name constants.
    /// </para>
    /// <img src="/ClassDiagrams/System.Data.Hsqldb.Client.MetaData.HsqlMetaDataColumnNames.png" 
    ///      alt="HsqlMetaDataColumnNames Class Diagram"/>
    /// </summary>
    /// <author name="boucherb@users"/>
    public static class HsqlMetaDataColumnNames
    {
        #region Constants

        #region RestrictionName
        /// <summary>
        /// The name of the column describing a named restriction.
        /// </summary>
        public const string RestrictionName = "RestrictionName";
        #endregion

        #region ParameterName

        /// <summary>
        /// The name of the column describing a named parameter.
        /// </summary>
        public const string ParameterName = "ParameterName";

        #endregion

        #region RestrictionDefault

        /// <summary>
        /// The name of the column describing the default value of a
        /// named restriction.
        /// </summary>
        public const string RestrictionDefault = "RestrictionDefault";

        #endregion

        #region RestrictionNumber

        /// <summary>
        /// The name of the column describing the ordinal of a
        /// named restriction.
        /// </summary>
        public const string RestrictionNumber = "RestrictionNumber";

        #endregion 

        #endregion
    }

    #endregion
}
