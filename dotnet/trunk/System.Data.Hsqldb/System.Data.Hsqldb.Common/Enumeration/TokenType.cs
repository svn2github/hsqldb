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

namespace System.Data.Hsqldb.Common.Enumeration
{
    #region TokenType

    /// <summary>
    /// <para>
    /// Classifies the lexographic role of an <see cref="Common.Sql.Token"/>.
    /// </para>
    /// <img src="/ClassDiagrams/System.Data.Hsqldb.Common.Enumeration.TokenType.png"
    ///      alt="TokenType Class Diagram"/>
    /// </summary>
    public enum TokenType
    {
        /// <summary>
        /// Specifies that the tokenizer is before the
        /// first (or after the last) token.
        /// </summary>
        None,
        /// <summary>
        /// Specifies a name token.
        /// </summary>
        Name,
        /// <summary>
        /// Specifies an identifier chain token.
        /// </summary>
        IdentifierChain,
        /// <summary>
        /// Specifies a special token.
        /// </summary>
        Special,
        /// <summary>
        /// Specifies an SQL number literal token.
        /// </summary>
        NumberLiteral,
        /// <summary>
        /// Specifies an SQL FLOAT literal token.
        /// </summary>
        FloatLiteral,
        /// <summary>
        /// Specifies an SQL STRING literal token.
        /// </summary>
        StringLiteral,
        /// <summary>
        /// Specifies an SQL BIGINT literal token.
        /// </summary>
        BigIntLiteral,
        /// <summary>
        /// Specifies an SQL DECIMAL or NUMERIC literal token.
        /// </summary>
        DecimalLiteral,
        /// <summary>
        /// Specifies an SQL BOOLEAN literal token.
        /// </summary>
        BooleanLiteral,
        /// <summary>
        /// Specifies an SQL DATE literal token.
        /// </summary>
        DateLiteral,
        /// <summary>
        /// Specifies an SQL TIME literal token.
        /// </summary>
        TimeLiteral,
        /// <summary>
        /// Specifies an SQL TIMESTAMP literal token.
        /// </summary>
        TimestampLiteral,
        /// <summary>
        /// Specifies a null-valued or unknown token.
        /// </summary>
        Null,
        /// <summary>
        /// Specifies an SQL named parameter token.
        /// </summary>
        NamedParameter,
        /// <summary>
        /// Specifies an SQL delimited identifier token.
        /// </summary>
        DelimitedIdentifier,
        /// <summary>
        /// Specifies an SQL line remark.
        /// </summary>
        RemarkLine,
        /// <summary>
        /// Specifies an SQL remark.
        /// </summary>
        Remark,
        /// <summary>
        /// Specifies an SQL parameter marker token.
        /// </summary>
        ParameterMarker       
    }

    #endregion
}
