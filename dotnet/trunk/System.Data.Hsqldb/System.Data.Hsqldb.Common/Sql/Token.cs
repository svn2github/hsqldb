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
using System.Text;
#endregion

namespace System.Data.Hsqldb.Common.Sql
{
    #region HsqlToken
    /// <summary>
    /// <para>
    /// Represents an SQL lexographic element.
    /// </para>
    /// <img src="../Documentation/ClassDiagrams/System.Data.Hsqldb.Common.HsqlToken.png"
    ///      alt="HsqlToken Class Diagram"/>
    /// </summary>
    /// <author name="boucherb@users"/>
    public sealed partial class Token
    {
        #region Private Fields
        private string m_value;
        private string m_qualifierPart;
        private string m_subjectPart;
        private SqlTokenType m_type;
        private int m_hashCode;
        #endregion

        #region Constructors

        #region Token(string, TokenType)
        /// <summary>
        /// Initializes a new instance of the <see cref="Token"/> class.
        /// </summary>
        /// <param name="value">
        /// The token value, which must be in its normalized character sequence form.
        /// </param>
        /// <param name="type">
        /// Type of the token, which allows the givne token value to be correctly converted
        /// to a <see cref="LiteralValue"/>, etc.
        /// </param>
        public Token(string value, SqlTokenType type)
        {
            if (string.IsNullOrEmpty(value))
            {
                throw new ArgumentNullException("value");
            }
            if (type == SqlTokenType.None)
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
        /// Initializes a new instance of a two-part <c>IdentifierChain</c> 
        /// <see cref="Token"/>.
        /// </summary>
        /// <remarks>the <see cref="TokenType"/> is inferred when using the
        /// constructor form and hence is not included in the signature.
        /// </remarks>
        /// <param name="value">
        /// The token value, which must be in its normalized character
        /// sequence form.
        /// </param>
        /// <param name="qualifierPart">
        /// usually an unqualified schema name
        /// </param>
        /// <param name="subjectPart">
        /// usually a simple (unqualified) SQL object name
        /// </param>
		public Token(String value, string qualifierPart, string subjectPart)
            : this(value, SqlTokenType.IdentifierChain) {
            //
            if (string.IsNullOrEmpty(qualifierPart))
            {
                throw new ArgumentNullException("qualifierPart");
            }
            //
            if (string.IsNullOrEmpty(subjectPart))
            {
                throw new ArgumentNullException("subjectPart");
            }
            //
            m_qualifierPart = qualifierPart;
            m_subjectPart = subjectPart;
        }
	    #endregion

        #endregion

        #region Public Properties

        #region QualifierPart
        /// <summary>
        /// Gets the qualifier part of the identifier chain.
        /// </summary>
        /// <value>The qualifier part of the identifier chain.</value>
        public string QualifierPart
        {
            get
            {
                switch (m_type)
                {
                    case SqlTokenType.IdentifierChain:
                        {
                            return m_qualifierPart;
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

        #region SubjectPart
        /// <summary>
        /// Gets the terminal (right-most) identifier chain component.
        /// </summary>
        /// <value>The terminal (right-most) identifier chain component.</value>
        public string SubjectPart
        {
            get
            {
                switch (m_type)
                {
                    case SqlTokenType.IdentifierChain:
                        {
                            return m_subjectPart;
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
                return Tokenizer.ToLiteralValue(ref m_type, m_value);
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
        public SqlTokenType Type
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

        #region ToString()
        /// <summary>
        /// Retrieves a <see cref="T:System.String"></see> representation of this object.
        /// </summary>
        /// <returns>
        /// A value of the form: 
        /// "System.Data.Hsqldb.Common.Sql.Token[value=this.Value,type=this.Type[,qualifierPart=this.QualifierPart,subjectPart=this.SubjectPart]]"
        /// </returns>
        public override string ToString()
        {
            StringBuilder sb = new StringBuilder(base.ToString());

            sb.Append("[value=").Append(Value);
            sb.Append(",type=").Append(Type);

            if (Type == SqlTokenType.IdentifierChain)
            {
                sb.Append(",qualifierPart=").Append(QualifierPart);
                sb.Append(",subjectPart=").Append(SubjectPart);
            }

            sb.Append(']');

            return sb.ToString();
        } 
        #endregion
        
        #endregion
    }
    #endregion
}
