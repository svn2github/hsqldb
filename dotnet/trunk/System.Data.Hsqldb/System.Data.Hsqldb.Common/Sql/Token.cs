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
using System.Data.Hsqldb.Common.Enumeration;
#endregion

namespace System.Data.Hsqldb.Common.Sql
{
    #region HsqlToken
    /// <summary>
    /// <para>
    /// Represents an SQL lexographic element.
    /// </para>
    /// <img src="/ClassDiagrams/System.Data.Hsqldb.Common.HsqlToken.png"
    ///      alt="HsqlToken Class Diagram"/>
    /// </summary>
    /// <author name="boucherb@users"/>
    public sealed partial class Token
    {
        #region Private Fields
        private string m_value;
        private string m_identifierChainFirst;
        private string m_identifierChainLast;
        private TokenType m_type;
        private int m_hashCode;
        #endregion

        #region Constructors

        #region Token(string, TokenType)
        /// <summary>
        /// Initializes a new instance of the <see cref="Token"/> class.
        /// </summary>
        /// <param name="value">The string value.</param>
        /// <param name="type">Type of the token.</param>
        public Token(string value, TokenType type)
        {
            if (string.IsNullOrEmpty(value))
            {
                throw new ArgumentNullException("value");
            }
            if (type == TokenType.None)
            {
                throw new ArgumentOutOfRangeException(
                    "type", type, "Not a valid token type");
            }

            m_value = value;
            m_type = type;
        }
        #endregion

        #region Token(String,TokenType,string,string)
        /// <summary>
        /// Initializes a new instance of an <c>IdentifierChain</c> <see cref="Token"/.
        /// </summary>
        /// <param name="value">normalized string representation</param>
        /// <param name="identifierChainFirst">first part - usually a simple schema name</param>
        /// <param name="identifierChainLast">secnd part - usually a simple SQl object name</param>
		public Token(String value, string identifierChainFirst,
            string identifierChainLast) : this(value, TokenType.IdentifierChain) {
            if (string.IsNullOrEmpty(identifierChainFirst))
            {
                throw new ArgumentNullException("idenifierChainFirst");
            }
            if (string.IsNullOrEmpty(identifierChainLast))
            {
                throw new ArgumentNullException("identifierChainLast");
            }
            m_identifierChainFirst = identifierChainFirst;
            m_identifierChainLast = identifierChainLast;
        }
	    #endregion

        #endregion

        #region Public Properties

        #region IdentifierChainFirst
        /// <summary>
        /// Gets the first identifier chain component.
        /// </summary>
        /// <value>The first identifier chain component.</value>
        public string IdentifierChainFirst
        {
            get
            {
                switch (m_type)
                {
                    case TokenType.IdentifierChain:
                        {
                            return m_identifierChainFirst;
                        }
                    default:
                        {
                            throw new InvalidOperationException(
                                "Wrong token type " + m_type);
                        }
                }
            }
        } 
        #endregion

        #region IdentifierChainLast
        /// <summary>
        /// Gets the last identifier chain component.
        /// </summary>
        /// <value>The last identifier chain component.</value>
        public string IdentifierChainLast
        {
            get
            {
                switch (m_type)
                {
                    case TokenType.IdentifierChain:
                        {
                            return m_identifierChainLast;
                        }
                    default:
                        {
                            throw new InvalidOperationException(
                                "Wrong token type " + m_type);
                        }
                }
            }
        } 
        #endregion

        #region LiteralValue
        /// <summary>
        /// Gets the literal value.
        /// </summary>       
        /// <value>The literal value.</value>
        public object LiteralValue
        {
            get
            {
                switch (m_type)
                {
                    case TokenType.BigIntLiteral:
                        {
                            return Convert.ToInt64(m_value);
                        }
                    case TokenType.BooleanLiteral:
                        {
                            return (m_value == Token.ValueFor.TRUE);
                        }
                    case TokenType.DateLiteral:
                        {
                            return java.sql.Date.valueOf(m_value);
                        }
                    case TokenType.DecimalLiteral:
                        {
                            return Convert.ToDecimal(m_value);
                        }
                    case TokenType.FloatLiteral:
                        {
                            return Convert.ToDouble(m_value);
                        }
                    case TokenType.NumberLiteral:
                        {
                            return new java.math.BigDecimal(m_value);
                        }
                    case TokenType.StringLiteral:
                        {
                            return m_value.Trim(new char[] { '\'' }).Replace("''", "'");
                        }
                    case TokenType.TimeLiteral:
                        {
                            return java.sql.Time.valueOf(m_value);
                        }
                    case TokenType.TimestampLiteral:
                        {
                            return java.sql.Timestamp.valueOf(m_value);
                        }
                    default:
                        {
                            return m_value;
                        }
                }
            }
        }
        #endregion

        #region Value
        /// <summary>
        /// Gets the string value.
        /// </summary>
        /// <value>The string value.</value>
        public string Value
        {
            get { return m_value; }
        }
        #endregion

        #region Type
        /// <summary>
        /// Gets the type of the token.
        /// </summary>
        /// <value>The type of the token.</value>
        public TokenType Type
        {
            get { return m_type; }
        }
        #endregion

        #endregion

        #region System.Object Method Overrides
        
        #region Equals(object)
        /// <summary>
        /// Determines whether the specified object equals this object.
        /// </summary>
        /// <param name="obj">
        /// The <see cref="T:System.Object"></see> to 
        /// compare with the current <see cref="T:System.Object"></see>.
        /// </param>
        /// <returns>
        /// <c>true</c> if the specified object equals this object;
        /// otherwise, <c>false</c>.
        /// </returns>
        public override bool Equals(object obj)
        {
            return this.Equals(obj as Token);
        }
        #endregion

        #region Equals(HsqlToken)
        /// <summary>
        /// Determines whether the specified <c>HsqlToken</c> equals
        /// this <c>HsqlToken</c>.
        /// </summary>
        /// <param name="token">The token to test.</param>
        /// <returns>
        /// <c>true</c> if the specified <c>HsqlToken</c> equals
        /// this <c>HsqlToken</c>; otherwise, <c>false</c>.
        /// </returns>
        public bool Equals(Token token)
        {
            return (token != null) &&
                   (m_type == token.m_type) &&
                   (m_value == token.m_value);
        }
        #endregion

        #region GetHashCode()
        /// <summary>
        /// Serves as the hash function for this type.
        /// This method is suitable for use in hashing algorithms
        /// and data structures like a hash table.
        /// </summary>
        /// <returns>
        /// A hash code for this <see cref="Token"/>.
        /// </returns>
        public override int GetHashCode()
        {
            int h = m_hashCode;

            if (h == 0)
            {
                unchecked
                {
                    h = 29 * (m_value.GetHashCode() + (29 * m_type.GetHashCode()));
                }

                m_hashCode = h;
            }

            return h;
        }
        #endregion 
        
        #endregion
    }
    #endregion
}
