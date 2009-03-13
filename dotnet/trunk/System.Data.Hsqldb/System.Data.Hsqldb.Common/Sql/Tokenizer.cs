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
using CultureInfo = System.Globalization.CultureInfo;
using Exception = System.Exception;

using JavaBigDecimal = java.math.BigDecimal;
using JavaBoolean = java.lang.Boolean;
using JavaCharacter = java.lang.Character;
using JavaDouble = java.lang.Double;
using JavaError = java.lang.Error;
using JavaInteger = java.lang.Integer;
using JavaLong = java.lang.Long;
using JavaNumber = java.lang.Number;
using JavaSystem = org.hsqldb.lib.java.JavaSystem;

using GenericTokenEnumerable = System.Collections.Generic.IEnumerable<System.Data.Hsqldb.Common.Sql.Token>;
using GenericTokenList = System.Collections.Generic.List<System.Data.Hsqldb.Common.Sql.Token>;

using HsqlDateTime = org.hsqldb.HsqlDateTime;
using HsqlException = org.hsqldb.HsqlException;
using HsqlProviderType = System.Data.Hsqldb.Common.Enumeration.HsqlProviderType;
using HsqlStringConverter = org.hsqldb.lib.StringConverter;
using HsqlTrace = org.hsqldb.Trace;
using HsqlTypes = org.hsqldb.Types;
using HsqlValuePool = org.hsqldb.store.ValuePool;

using SqlTokenType = System.Data.Hsqldb.Common.Enumeration.SqlTokenType;
using Regex = System.Text.RegularExpressions.Regex;
#endregion

namespace System.Data.Hsqldb.Common.Sql
{
    #region HsqlTokenizer

    /// <summary>
    /// <para>
    /// Supports the lexographic analysis and subsequent parsing of syntactic
    /// productions in the HSQLDB dialect of SQL.
    /// </para>
    /// <img src="../Documentation/ClassDiagrams/System.Data.Hsqldb.Common.Sql.Tokenizer.png"
    ///      alt="Tokenizer Class Diagram"/>
    /// </summary>
    public sealed class Tokenizer
    {
        #region Static Fields
        static readonly Regex DateLiteralRegex = 
            new Regex(@"^(\d{4})-(\d{2})-(\d{2})$");
        static readonly Regex TimeLiteralRegex = 
            new Regex(@"^(\d{2}):(\d{2}):(\d{2})$");
        static readonly Regex TimestampLiteralRegex = 
            new Regex(@"^(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})(\.\d{1,9})?$");
        #endregion

        #region Instance Fields

        private bool m_acceptParameterMarkers = true;
        private bool m_acceptNamedParameters = true;
        private int m_beginIndex;
        private string m_chars;
        private int m_charsLength;
        private int m_currentIndex;
        private bool m_enforceTwoPartIdentifierChain;
        private System.Collections.Generic.List<Token> m_identifierChain;
        private string m_identifierChainPredecessor = null;
        private SqlTokenType m_identifierChainPredecessorType = SqlTokenType.None;
        private bool m_inIdentifierChain;
        private int m_nextTokenIndex;
        private string m_parameterName = string.Empty;
        private char m_parameterNamePrefix = ' ';
        private string m_token;
        private int m_tokenIndex;
        private SqlTokenType m_tokenType = SqlTokenType.None;
        private bool m_wait;
        private bool m_wasLastTokenDelimited;

        #endregion

        #region Constructors

        #region Tokenizer()

        /// <summary>
        /// Initializes a new instance of the <see cref="Tokenizer"/> class.
        /// </summary>
        public Tokenizer() : this(string.Empty)
        {
        }

        #endregion

        #region Tokenizer(string)

        /// <summary>
        /// Initializes a new instance of the
        /// <see cref="Tokenizer"/> class.
        /// </summary>
        /// <param name="chars">The character sequence to be tokenized.</param>
        public Tokenizer(string chars)
        {
            if (chars == null)
            {
                throw new ArgumentNullException("chars");
            }

            m_chars = chars;
            m_charsLength = chars.Length;
        }

        #endregion

        #endregion

        #region Instance Methods

        #region Back()

        /// <summary>
        /// Causes the current token to be effectively pushed
        /// back onto the token stream.
        /// </summary>
        private void Back()
        {
            if (m_wait)
            {
                throw Tokenizer.IllegalWaitState();
            }

            m_nextTokenIndex = m_currentIndex;
            m_currentIndex = m_tokenIndex;
            m_wait = true;
        }

        #endregion

        #region GetAsQuotedString(char)

        /// <summary>
        /// Gets the last read token, as the actual value of a
        /// quote delimited SQL string.
        /// </summary>
        /// <param name="quoteChar">The quote char.</param>
        /// <returns>A quote delimited SQL string</returns>
        private string GetAsQuotedString(char quoteChar)
        {
            try
            {
                int nextIndex = m_currentIndex;
                bool quoteInside = false;

                for (; ; )
                {
                    nextIndex = m_chars.IndexOf(quoteChar, nextIndex);

                    if (nextIndex < 0)
                    {
                        throw Tokenizer.UnexpectedEndOfCommand();
                    }

                    if (nextIndex < (m_charsLength - 1)
                        && (m_chars[nextIndex + 1] == quoteChar))
                    {
                        quoteInside = true;
                        nextIndex += 2;

                        continue;
                    }

                    break;
                }

                int buffLen = (nextIndex - m_currentIndex);
                char[] chBuffer = new char[buffLen];

                m_chars.CopyTo(m_currentIndex, chBuffer, 0, buffLen);

                int j = buffLen;

                if (quoteInside)
                {
                    j = 0;

                    // PRE:
                    // - Assumes all occurences of quoteChar are paired
                    // - Has already been checked by the preprocessing loop
                    for (int i = 0; i < chBuffer.Length; i++, j++)
                    {
                        if (chBuffer[i] == quoteChar)
                        {
                            i++;
                        }

                        chBuffer[j] = chBuffer[i];
                    }
                }

                m_currentIndex = ++nextIndex;

                return new string(chBuffer, 0, j);
            }
            catch (HsqlDataSourceException)
            {
                throw;
            }
            catch (OutOfMemoryException)
            {
                throw;
            }
            catch (JavaError)
            {
                throw;
            }
            catch (Exception)
            {
                // TODO: do not swallow others 
            }

            return null;
        }

        #endregion

        #region GetNextAsBigint()

        /// <summary>
        /// Gets the next token as an SQL BIGINT literal.
        /// </summary>
        /// <returns>
        /// The SQL BIGINT literal denoted by the next token.
        /// </returns>
        public long GetNextAsBigint()
        {
            bool isNegative = false;

            ReadToken();

            if ("-" == m_token)
            {
                isNegative = true;

                ReadToken();
            }

            object objectValue = LiteralValue;
            HsqlProviderType tokenDataType = LiteralValueDataType;

            switch (tokenDataType)
            {
                case HsqlProviderType.Integer:
                case HsqlProviderType.BigInt:
                    {
                        break;
                    }
                case HsqlProviderType.Decimal:
                    {
                        // only Long.MAX_VALUE + 1 together with
                        // minus is acceptable
                        if (isNegative && m_LongMaxValuePlusOne.equals(
                            objectValue))
                        {
                            return long.MinValue;
                        }

                        throw Tokenizer.WrongDataType(tokenDataType);
                    }
                default:
                    {
                        throw Tokenizer.WrongDataType(tokenDataType);
                    }
            }

            long longValue = ((JavaNumber)objectValue).longValue();

            return isNegative ? -longValue : longValue;
        }

        #endregion

        #region GetNextAsInt()

        /// <summary>
        /// Gets the next token as an SQL INT literal.
        /// </summary>
        /// <returns>
        /// The SQL INT literal denoted by the next token.
        /// </returns>
        public int GetNextAsInt()
        {
            long value = GetNextAsBigint();

            if (value > int.MaxValue || value < int.MinValue)
            {
                throw Tokenizer.WrongDataType(LiteralValueDataType);
            }

            return (int)value;
        }

        #endregion

        #region GetNextAsLiteralValue(HsqlProviderType)

        /// <summary>
        /// Gets the next token as a literal value of the requested
        /// SQL data type.
        /// </summary>
        /// <remarks>
        /// The <see cref="System.Type"/> of the returned object is
        /// value return by <see cref="HsqlConvert.ToProviderSpecificDataType(int)"/>
        /// when passed the <c>requestedDataType</c>, cast to an <c>int</c>.
        /// </remarks>
        /// <param name="requestedDataType">
        /// The requested data type.
        /// </param>
        /// <returns>
        /// A literal value of the requested data type,
        /// as denoted by the next token.
        /// </returns>
        public object GetNextAsLiteralValue(HsqlProviderType requestedDataType)
        {
            ReadToken();

            object value = LiteralValue;
            HsqlProviderType dataType = LiteralValueDataType;

            if (dataType != requestedDataType)
            {
                try
                {
                    value = HsqlConvert.FromJava.ToObject(value,
                        (int)requestedDataType);
                    value = HsqlConvert.FromDotNet.ToObject(value,
                        (int)requestedDataType);
                }
                catch (Exception)
                {
                    throw Tokenizer.WrongDataType(dataType);
                }
            }

            return value;
        }

        #endregion

        #region GetNextAsName()

        /// <summary>
        /// Gets the next token, throwing if it is not an SQL name token.
        /// </summary>
        /// <returns>
        /// The next token.
        /// </returns>
        public string GetNextAsName()
        {
            ReadToken();

            if (WasName)
            {
                return m_token;
            }

            throw Tokenizer.UnexpectedToken(m_token);
        }

        #endregion

        #region GetNextAsSimpleName()

        /// <summary>
        /// Gets the next token, throwing if it is not a simple name.
        /// </summary>
        /// <returns>
        /// The next token.
        /// </returns>
        public string GetNextAsSimpleName()
        {
            ReadToken();

            if (WasSimpleName)
            {
                return m_token;
            }

            string token = (m_tokenType == SqlTokenType.IdentifierChain)
                               ? m_identifierChainPredecessor
                               : m_token;

            throw Tokenizer.UnexpectedToken(token);
        }

        #endregion

        #region GetNextAsSimpleToken()

        /// <summary>
        /// Gets the next token, throwing if it is not simple.
        /// </summary>
        /// <returns>
        /// The next simple token.
        /// </returns>
        public string GetNextAsSimpleToken()
        {
            ReadToken();

            if (WasSimpleToken)
            {
                return m_token;
            }

            string token = (m_tokenType == SqlTokenType.IdentifierChain)
                               ? m_identifierChainPredecessor
                               : m_token;

            throw Tokenizer.UnexpectedToken(token);
        }

        #endregion

        #region GetNextAsString()

        /// <summary>
        /// Gets the next token as a string.
        /// </summary>
        /// <returns>
        /// The string represetnation of the next token.
        /// </returns>
        public string GetNextAsString()
        {
            ReadToken();

            return m_token;
        }

        #endregion

        #region GetPart(int,int)

        /// <summary>
        /// Gets a substring from the character sequence being tokenized.
        /// </summary>
        /// <remarks>
        /// The substring begins at the specified <c>startIndex</c> and extends 
        /// to the character at <c>endIndex - 1</c>. Thus the length of 
        /// the substring is <c>endIndex-startIndex</c>.
        /// </remarks>
        /// <param name="startIndex">The starting index.</param>
        /// <param name="endIndex">The ending index.</param>
        /// <returns>The requested substring.</returns>
        public string GetPart(int startIndex, int endIndex)
        {
            return m_chars.Substring(startIndex, endIndex - startIndex);
        }

        #endregion

        #region GetThis(string)

        /// <summary>
        /// Gets the next token, throwing if it does not
        /// equal the specified <c>match</c> value.
        /// </summary>
        /// <param name="match">The character sequence to match.</param>
        /// <returns>
        /// The string representation of the next token.
        /// </returns>
        /// <remarks>
        /// Used for commands and simple non-delimited identifiers only.
        /// </remarks>
        /// <exception cref="HsqlException">
        /// When the next token does not equal the given <c>match</c> value.
        /// </exception>
        public string GetThis(string match)
        {
            ReadToken();
            MatchThis(match);

            return m_token;
        }

        #endregion

        #region IsGetThis(string)

        /// <summary>
        /// Determines whether the next token equals the
        /// specified <c>match</c> value. When it does not,
        /// it is effectively pushed back on to front of the
        /// token stream.
        /// </summary>
        /// <remarks>
        /// Used for commands only.
        /// </remarks>
        /// <param name="match">The value to match.</param>
        /// <returns>
        /// <c>true</c> if the next token equals the
        /// specified <c>match</c> value;
        /// otherwise, <c>false</c>.
        /// </returns>
        public bool IsGetThis(string match)
        {
            ReadToken();

            if ((m_tokenType != SqlTokenType.DelimitedIdentifier)
                && (m_tokenType != SqlTokenType.IdentifierChain)
                && m_token.Equals(match))
            {
                return true;
            }
            else
            {
                Back();

                return false;
            }
        }

        #endregion

        #region MatchThis(string)

        /// <summary>
        /// Checks if the current token is equal to
        /// <c>match</c>, throwing if it is not.
        /// </summary>
        /// <remarks>
        /// Used for commands and simple non-delimited identifiers only.
        /// </remarks>
        /// <param name="match">The value to match.</param>
        private void MatchThis(string match)
        {
            if (m_wait)
            {
                throw Tokenizer.IllegalWaitState();
            }

            if (!m_token.Equals(match)
                || m_tokenType == SqlTokenType.DelimitedIdentifier
                || m_tokenType == SqlTokenType.IdentifierChain)
            {
                string token = (m_tokenType == SqlTokenType.IdentifierChain)
                                   ? m_identifierChainPredecessor
                                   : m_token;

                throw Tokenizer.MatchFailed(token, match);
            }
        }

        #endregion

        #region ReadIdentifierChain()
        /// <summary>
        /// Reads an entire identifier chain.
        /// </summary>
        private void ReadIdentifierChain()
        {
            if (m_identifierChain == null)
            {
                m_identifierChain = new GenericTokenList();
            }

            if (m_inIdentifierChain && m_enforceTwoPartIdentifierChain)
            {
                throw Tokenizer.IdentiferChainLengthExceeded();
            }

            m_identifierChainPredecessor = m_token;
            m_identifierChainPredecessorType = m_tokenType;
            m_inIdentifierChain = true;

            m_currentIndex++;

            m_identifierChain.Add(new Token(m_token, m_tokenType));

            // TODO: avoid recursion
            // Also, this has problems when there is whitespace
            // after the dot; same with NAME
            ReadToken();
        }
        #endregion

        #region ReadToken()

        /// <summary>
        /// Reads the next token from the character
        /// sequence being tokenized.
        /// </summary>
        private void ReadToken()
        {
            if (m_wait)
            {
                m_wait = false;
                m_currentIndex = m_nextTokenIndex;

                return;
            }

            if (!m_inIdentifierChain)
            {
                m_identifierChain = null;
                m_identifierChainPredecessor = null;
                m_identifierChainPredecessorType = SqlTokenType.None;
            }

            while (m_currentIndex < m_charsLength
                   && JavaCharacter.isWhitespace(m_chars[m_currentIndex]))
            {
                m_currentIndex++;
            }

            m_token = string.Empty;
            m_parameterName = string.Empty;
            m_parameterNamePrefix = ' ';
            m_tokenIndex = m_currentIndex;

            if (m_currentIndex >= m_charsLength)
            {
                // character sequence has been exhausted.
                m_tokenType = SqlTokenType.None;

                return;
            }

            char c = m_chars[m_currentIndex];
            bool point = false;
            bool digit = false;
            bool exp = false;
            bool afterexp = false;
            bool end = false;
            char cfirst = (char)0;

            m_wasLastTokenDelimited = false;

            if (JavaCharacter.isJavaIdentifierStart(c))
            {
                m_tokenType = SqlTokenType.Name;
            }
            else if (JavaCharacter.isDigit(c))
            {
                m_tokenType = SqlTokenType.NumberLiteral;
                digit = true;
            }
            else
            {
                switch (c)
                {
                    case '(':
                        {
                            m_token = Token.ValueFor.OPENBRACKET;
                            m_tokenType = SqlTokenType.Special;

                            m_currentIndex++;

                            return;
                        }
                    case ')':
                        {
                            m_token = Token.ValueFor.CLOSEBRACKET;
                            m_tokenType = SqlTokenType.Special;

                            m_currentIndex++;

                            return;
                        }
                    case ',':
                        {
                            m_token = Token.ValueFor.COMMA;
                            m_tokenType = SqlTokenType.Special;

                            m_currentIndex++;

                            return;
                        }
                    case '*':
                        {
                            m_token = Token.ValueFor.MULTIPLY;
                            m_tokenType = SqlTokenType.Special;

                            m_currentIndex++;

                            return;
                        }
                    case '=':
                        {
                            m_token = Token.ValueFor.EQUALS;
                            m_tokenType = SqlTokenType.Special;

                            m_currentIndex++;

                            return;
                        }
                    case ';':
                        {
                            m_token = Token.ValueFor.SEMICOLON;
                            m_tokenType = SqlTokenType.Special;

                            m_currentIndex++;

                            return;
                        }
                    case '+':
                        {
                            m_token = Token.ValueFor.PLUS;
                            m_tokenType = SqlTokenType.Special;

                            m_currentIndex++;

                            return;
                        }
                    case '%':
                        {
                            m_token = Token.ValueFor.PERCENT;
                            m_tokenType = SqlTokenType.Special;

                            m_currentIndex++;

                            return;
                        }
                    case '?':
                        {
                            if (!m_acceptParameterMarkers)
                            {
                                throw Tokenizer.UnexpectedToken(c);
                            }

                            m_token = Token.ValueFor.QUESTION;
                            m_tokenType = SqlTokenType.Special;

                            m_currentIndex++;

                            return;
                        }
                    case ':':
                        {
                            if (!m_acceptNamedParameters)
                            {
                                throw UnexpectedToken(c);
                            }

                            if (!(++m_currentIndex < m_charsLength))
                            {
                                throw Tokenizer.UnexpectedEndOfCommand();
                            }

                            c = m_chars[m_currentIndex];

                            if (!JavaCharacter.isJavaIdentifierStart(c))
                            {
                                throw Tokenizer.InvalidIdentifier(":" + c);
                            }

                            m_tokenType = SqlTokenType.NamedParameter;
                            m_parameterNamePrefix = ':';

                            break;
                        }
                    case '@':
                        {
                            if (!m_acceptNamedParameters)
                            {
                                throw UnexpectedToken(c);
                            }

                            if (!(++m_currentIndex < m_charsLength))
                            {
                                throw Tokenizer.UnexpectedEndOfCommand();
                            }

                            c = m_chars[m_currentIndex];

                            if (!JavaCharacter.isJavaIdentifierStart(c))
                            {
                                throw InvalidIdentifier(":" + c);
                            }

                            m_tokenType = SqlTokenType.NamedParameter;
                            m_parameterNamePrefix = '@';

                            break;
                        }
                    case '"':
                        {
                            m_wasLastTokenDelimited = true;
                            m_tokenType = SqlTokenType.DelimitedIdentifier;

                            m_currentIndex++;

                            m_token = GetAsQuotedString('"');

                            if (m_currentIndex == m_chars.Length)
                            {
                                if (m_inIdentifierChain)
                                {
                                    m_identifierChain.Add(new Token(m_token, m_tokenType));
                                    m_inIdentifierChain = false;
                                    m_tokenType = SqlTokenType.IdentifierChain;
                                }
                                
                                return;
                            }

                            c = m_chars[m_currentIndex];

                            if (c == '.')
                            {
                                ReadIdentifierChain();
                            } 
                            else if (m_inIdentifierChain)
                            {
                                m_identifierChain.Add(new Token(m_token, m_tokenType));
                                m_inIdentifierChain = false;
                                m_tokenType = SqlTokenType.IdentifierChain;
                            }

                            return;
                        }
                    case '\'':
                        {
                            m_tokenType = SqlTokenType.StringLiteral;

                            m_currentIndex++;

                            m_token = GetAsQuotedString('\'');

                            return;
                        }
                    case '!':
                    case '<':
                    case '>':
                    case '|':
                    case '/':
                    case '-':
                        {
                            cfirst = c;
                            m_tokenType = SqlTokenType.Special;

                            break;
                        }
                    case '.':
                        {
                            m_tokenType = SqlTokenType.DecimalLiteral;
                            point = true;

                            break;
                        }
                    default:
                        {
                            throw UnexpectedToken(c);
                        }
                }
            }

            int start = m_currentIndex++;

            while (true)
            {
                if (m_currentIndex >= m_charsLength)
                {
                    c = ' ';
                    end = true;

                    switch (m_tokenType)
                    {
                        case SqlTokenType.StringLiteral:
                        case SqlTokenType.DelimitedIdentifier:
                            {
                                throw Tokenizer.UnexpectedEndOfCommand();
                            }
                    }
                }
                else
                {
                    c = m_chars[m_currentIndex];
                }

                switch (m_tokenType)
                {
                    case SqlTokenType.NamedParameter:
                    case SqlTokenType.Name:
                        {
                            if (JavaCharacter.isJavaIdentifierPart(c))
                            {
                                break;
                            }

                            m_token = m_chars.Substring(start, m_currentIndex
                                - start).ToUpper(m_EnglishCulture);

                            if (m_tokenType == SqlTokenType.NamedParameter)
                            {
                                m_parameterName = m_chars.Substring(start,
                                        (m_currentIndex - start));

                                return;
                            }

                            // only for Name, not for NamedParameter
                            if (c == '.')
                            {
                                ReadIdentifierChain();
                            }
                            else if (m_inIdentifierChain /*&& c!= '.'*/)
                            {
                                m_identifierChain.Add(new Token(m_token, m_tokenType));
                                m_inIdentifierChain = false;
                                m_tokenType = SqlTokenType.IdentifierChain;
                            }
                            else if (c == '(')
                            {
                                // no-op
                                // potentially a function call, SQL operator, etc.
                            }                            
                            else
                            {
                                // if in value list then it is a value
                                int type = m_ValueTokens.get(m_token, -1);

                                if (type != -1)
                                {
                                    m_tokenType = (SqlTokenType)type;
                                }
                            }

                            return;
                        }
                    case SqlTokenType.DelimitedIdentifier:
                    case SqlTokenType.StringLiteral:
                        {
                            // should never get here
                            break;
                        }
                    case SqlTokenType.Remark:
                        {
                            if (end)
                            {
                                // unfinished remark
                                // TODO: log/trace error condition here
                                m_tokenType = SqlTokenType.None;

                                return;
                            }
                            else if (c == '*')
                            {
                                m_currentIndex++;

                                if (m_currentIndex < m_charsLength
                                    && m_chars[m_currentIndex] == '/')
                                {
                                    // TODO: eliminate recursion
                                    m_currentIndex++;

                                    ReadToken();

                                    return;
                                }
                            }

                            break;
                        }
                    case SqlTokenType.RemarkLine:
                        {
                            if (end)
                            {
                                m_tokenType = SqlTokenType.None;

                                return;
                            }
                            else if (c == '\r' || c == '\n')
                            {
                                // TODO: eliminate recursion
                                ReadToken();

                                return;
                            }

                            break;
                        }
                    case SqlTokenType.Special:
                        {
                            if (c == '/' && cfirst == '/')
                            {
                                m_tokenType = SqlTokenType.RemarkLine;

                                break;
                            }
                            else if (c == '-' && cfirst == '-')
                            {
                                m_tokenType = SqlTokenType.RemarkLine;

                                break;
                            }
                            else if (c == '*' && cfirst == '/')
                            {
                                m_tokenType = SqlTokenType.Remark;

                                break;
                            }
                            else if (c == '>' || c == '=' || c == '|')
                            {
                                break;
                            }

                            m_token
                                = m_chars.Substring(start,
                                                    m_currentIndex - start);

                            return;
                        }
                    case SqlTokenType.NumberLiteral:
                    case SqlTokenType.FloatLiteral:
                    case SqlTokenType.DecimalLiteral:
                        {
                            if (JavaCharacter.isDigit(c))
                            {
                                digit = true;
                            }
                            else if (c == '.')
                            {
                                if (point)
                                {
                                    throw Tokenizer.UnexpectedToken(c);
                                }

                                m_tokenType = SqlTokenType.DecimalLiteral;

                                point = true;
                            }
                            else if (c == 'E' || c == 'e')
                            {
                                if (exp)
                                {
                                    throw UnexpectedToken(c);
                                }

                                m_tokenType = SqlTokenType.FloatLiteral;

                                // first character after exp may be + or -
                                afterexp = true;
                                point = true;
                                exp = true;
                            }
                            else if (c == '-' && afterexp)
                            {
                                afterexp = false;
                            }
                            else if (c == '+' && afterexp)
                            {
                                afterexp = false;
                            }
                            else
                            {
                                //afterexp = false;

                                if (!digit)
                                {
                                    if (point
                                        && (start == (m_currentIndex - 1)))
                                    {
                                        m_token = Token.ValueFor.PERIOD;
                                        m_tokenType = SqlTokenType.Special;

                                        return;
                                    }

                                    throw UnexpectedToken(c);
                                }

                                m_token = m_chars.Substring(start,
                                    (m_currentIndex - start));

                                return;
                            }

                            break;
                        }
                }

                m_currentIndex++;
            }
        }

        #endregion

        #region Reset(string)

        /// <summary>
        /// Resets this tokenizer with the given character sequence.
        /// </summary>
        /// <param name="chars">
        /// The new character sequence to be tokenized.
        /// </param>
        /// <exception cref="ArgumentNullException">
        /// When <c>chars</c> is <c>null</c>.
        /// </exception>
        public void Reset(string chars)
        {
            if (chars == null)
            {
                throw new ArgumentNullException("chars");
            }

            m_chars = chars;
            m_charsLength = chars.Length;
            m_currentIndex = 0;
            m_tokenIndex = 0;
            m_nextTokenIndex = 0;
            m_beginIndex = 0;
            m_tokenType = SqlTokenType.None;
            m_identifierChainPredecessorType = SqlTokenType.None;
            m_token = null;
            m_parameterName = null;
            m_parameterNamePrefix = ' ';
            m_identifierChainPredecessor = null;
            m_wait = false;
            m_wasLastTokenDelimited = false;
            m_inIdentifierChain = false;
        }

        #endregion

        #region SetPartMarker()

        /// <summary>
        /// Sets the part marker to the current position.
        /// </summary>
        public void SetPartMarker()
        {
            m_beginIndex = m_currentIndex;
        }

        #endregion


        #region ToLiteralValue(SqlTokenType,string)
        /// <summary>
        /// Retrieves the SQL literal value of the given token value.
        /// </summary>
        /// <remarks>
        /// Note that, as a ref parameter, the tokenType may be
        /// re-assigned as part of computing the literal value.
        /// </remarks>
        /// <param name="tokenType">
        /// Type of the token.
        /// </param>
        /// <param name="tokenValue">
        /// The token value, as a character sequence
        /// </param>
        /// <returns>
        /// the token literal value, as an object representing the SQL literal
        /// numeric, temporal, boolean or character value.
        /// </returns>
        internal static object ToLiteralValue(ref SqlTokenType tokenType,
            string tokenValue)
        {
            switch (tokenType)
            {
                case SqlTokenType.Null:
                    {
                        return null;
                    }
                case SqlTokenType.StringLiteral:
                    {
                        if (tokenValue.Length <= 10 && 
                            Tokenizer.DateLiteralRegex.IsMatch(tokenValue))
                        {
                            try
                            {
                                object rval = HsqlDateTime.dateValue(tokenValue);

                                tokenType = SqlTokenType.DateLiteral;

                                return rval;
                            }
                            catch (Exception)
                            {
                            }
                        }
                        else if (tokenValue.Length <= 10 && 
                            Tokenizer.TimeLiteralRegex.IsMatch(tokenValue))
                        {
                            try
                            {
                                object rval = HsqlDateTime.timeValue(tokenValue);

                                tokenType = SqlTokenType.TimeLiteral;

                                return rval;
                            }
                            catch (Exception)
                            {
                            }
                        }
                        else if (Tokenizer.TimestampLiteralRegex.IsMatch(tokenValue))
                        {
                            try
                            {
                                object rval = HsqlDateTime.timestampValue(tokenValue);

                                tokenType = SqlTokenType.TimestampLiteral;

                                return rval;
                            }
                            catch (Exception)
                            {
                            }
                        }

                        return tokenValue;
                    }
                case SqlTokenType.BigIntLiteral:
                    {
                        return HsqlValuePool.getLong(JavaLong.parseLong(tokenValue));
                    }
                case SqlTokenType.NumberLiteral:
                    {
                        // Returns unsigned values which may later need to be negated.
                        // As a result, Integer.MIN_VALUE or Long.MIN_VALUE are promoted
                        // to a wider type.
                        if (tokenValue.Length < 11)
                        {
                            try
                            {
                                object rval = HsqlValuePool.getInt(
                                    JavaInteger.parseInt(tokenValue));

                                tokenType = SqlTokenType.IntegerLiteral;

                                return rval;
                            }
                            catch (Exception)
                            {
                            }
                        }

                        if (tokenValue.Length < 20)
                        {
                            try
                            {
                                object rval = HsqlValuePool.getLong(
                                    JavaLong.parseLong(tokenValue));

                                tokenType = SqlTokenType.BigIntLiteral;

                                return rval;
                            }
                            catch (Exception ex)
                            {
                            }
                        }

                        tokenType = SqlTokenType.DecimalLiteral;

                        return new JavaBigDecimal(tokenValue);
                    }
                case SqlTokenType.FloatLiteral:
                    {
                        double d = JavaSystem.parseDouble(tokenValue);
                        long l = JavaDouble.doubleToLongBits(d);

                        return HsqlValuePool.getDouble(l);
                    }
                case SqlTokenType.DecimalLiteral:
                    {
                        return new JavaBigDecimal(tokenValue);
                    }
                case SqlTokenType.BooleanLiteral:
                    {
                        return Token.ValueFor.TRUE.Equals(tokenValue, 
                            InvariantCultureIgnoreCase) ? JavaBoolean.TRUE
                            : JavaBoolean.FALSE;
                    }
                case SqlTokenType.DateLiteral:
                    {
                        return HsqlDateTime.dateValue(tokenValue);
                    }
                case SqlTokenType.TimeLiteral:
                    {
                        return HsqlDateTime.timeValue(tokenValue);
                    }
                case SqlTokenType.TimestampLiteral:
                    {
                        return HsqlDateTime.timestampValue(tokenValue);
                    }
                default:
                    {
                        return tokenValue;
                    }
            }
        }
        #endregion

        #region WasThis(string)

        /// <summary>
        /// Determines whether the token that was last read is equal to
        /// the specified <c>match</c> value.
        /// </summary>
        /// <remarks>
        /// Note that this method is *not* intended for use with delimited
        /// or chained identifiers.
        /// </remarks>
        /// <param name="match">The value to match.</param>
        /// <returns>
        /// <c>true</c> if the token that was last read is equal to the
        /// specified <c>match</c> value; otherwise, <c>false</c>.
        /// </returns>
        public bool WasThis(string match)
        {
            switch (m_tokenType)
            {
                case SqlTokenType.DelimitedIdentifier:
                case SqlTokenType.IdentifierChain:
                    {
                        return false;
                    }
                default:
                    {
                        return object.Equals(m_token, match);
                    }
            }
        }

        #endregion

        #endregion

        #region Instance Properties

        /// <summary>
        /// Gets or sets a value indicating whether to enforce two part identifier chains.
        /// </summary>
        /// <value>
        /// 	<c>true</c> to enforce two part identifier chains; otherwise, <c>false</c>.
        /// </value>
        public bool EnforceTwoPartIdentifierChain
        {
            get { return m_enforceTwoPartIdentifierChain; }
            set { m_enforceTwoPartIdentifierChain = value; }
        }

        #region IdentifierChain
        /// <summary>
        /// Gets the identifier chain.
        /// </summary>
        /// <value>The identifier chain.</value>
        public GenericTokenEnumerable IdentifierChain
        {
            get { return m_identifierChain; }
        }
        #endregion

        #region IdentifierChainPredecessor

        /// <summary>
        /// Gets the component of the identifier chain immediately preceding 
        /// the subject [right-most] component.
        /// </summary>
        /// <value>The subject predecessor component of the identifier chain.</value>
        /// <remarks>
        /// Note: no token type check is performed; if the last read
        /// token is not an identifier chain, the value is <c>null</c>.
        /// </remarks>
        public string IdentifierChainPredecessor
        {
            get
            {
                if (m_wait)
                {
                    throw Tokenizer.IllegalWaitState();
                }

                return m_identifierChainPredecessor;
            }
        }

        #endregion

        #region LastPart

        /// <summary>
        /// Gets a substring of length (<see cref="Position"/>
        /// - <c>PartMarker</c>) starting at offset <see cref="PartMarker"/>
        /// into the character sequence being tokenized.
        /// </summary>
        /// <value>
        /// The substring denoted by the current values of
        /// <c>PartMarker</c> and <c>Position</c>.
        /// </value>
        public string LastPart
        {
            get
            {
                return m_chars.Substring(m_beginIndex,
                                         m_currentIndex - m_beginIndex);
            }
        }

        #endregion

        #region Length

        /// <summary>
        /// Gets the length of the character sequence being tokenized.
        /// </summary>
        /// <value>The length.</value>
        public int Length
        {
            get { return m_charsLength; }
        }

        #endregion

        #region LiteralValue

        /// <summary>
        /// Gets the last read token as an SQL literal value.
        /// </summary>
        /// <remarks>
        /// The data type of the literal is inferred.
        /// </remarks>
        /// <value>
        /// The SQL literal value denoted by the last read token.
        /// </value>
        private object LiteralValue
        {
            get
            {
                if (!WasLiteralValue)
                {
                    throw Tokenizer.UnexpectedToken(m_token);
                }

                return Tokenizer.ToLiteralValue(ref m_tokenType, m_token);
            }
        }

        #endregion

        #region LiteralValueDataType

        /// <summary>
        /// Gets the inferred data type of the SQL literal value
        /// denoted by the last read token.
        /// </summary>
        /// <remarks>
        /// Note: no token type check is performed; if the last
        /// read token is not an SQL literal, the value is
        /// <c>HsqlProviderType.Null</c>.
        /// </remarks>
        /// <value>
        /// The inferred data type of the SQL literal value
        /// denoted by the last read token.
        /// </value>
        public HsqlProviderType LiteralValueDataType
        {
            get
            {
                if (m_wait)
                {
                    throw Tokenizer.IllegalWaitState();
                }

                // TODO: eliminate this inefficiency.
                Tokenizer.ToLiteralValue(ref m_tokenType, m_token);

                // TODO: make sure this is used only for valued tokens.
                switch (m_tokenType)
                {
                    case SqlTokenType.StringLiteral:
                        {
                            return HsqlProviderType.VarChar;
                        }
                    case SqlTokenType.NumberLiteral:
                        {
                            return HsqlProviderType.Decimal;
                        }
                    case SqlTokenType.BigIntLiteral:
                        {
                            return HsqlProviderType.BigInt;
                        }
                    case SqlTokenType.IntegerLiteral:
                        {
                            return HsqlProviderType.Integer;
                        }
                    case SqlTokenType.FloatLiteral:
                        {
                            return HsqlProviderType.Double;
                        }
                    case SqlTokenType.DecimalLiteral:
                        {
                            return HsqlProviderType.Decimal;
                        }
                    case SqlTokenType.BooleanLiteral:
                        {
                            return HsqlProviderType.Boolean;
                        }
                    case SqlTokenType.DateLiteral:
                        {
                            return HsqlProviderType.Date;
                        }
                    case SqlTokenType.TimeLiteral:
                        {
                            return HsqlProviderType.Time;
                        }
                    case SqlTokenType.TimestampLiteral:
                        {
                            return HsqlProviderType.TimeStamp;
                        }
                    default:
                        {
                            return HsqlProviderType.Null;
                        }
                }
            }
        }

        #endregion

        #region NormalizedToken

        /// <summary>
        /// Gets the character sequence denoted by last read token, as it 
        /// would actually appear in a cannonical SQL statement, for instance, by
        /// augmentation of string literal or name tokens with leading and 
        /// trailing single or double quote delimiter characters, respectively,
        /// and escaping, as required, any internal delimiter characters.
        /// </summary>
        /// <remarks>
        /// The normalized form excludes leading whitespace and
        /// trailing whitespace, including leading and/or trailing
        /// whitespace internal to identifier chains, for example, when 
        /// <c>"foo"  .  "bar"</c> is read, then <c>TokenType</c> is 
        /// <c>IdentifierChain</c>, <c>IdentifierChainFirst</c> is <c>foo</c>,
        /// and <c>NormalizedToken</c> is <c>"foo"."bar"</c>.
        /// </remarks>
        /// <value>
        /// The normalized character sequence denoted by the token that was last read.
        /// </value>
        public string NormalizedToken
        {
            get
            {
                if (m_wait)
                {
                    throw Tokenizer.IllegalWaitState();
                }

                string token;

                if (WasIdentifierChain)
                {
                    string qualifierPart = IdentifierChainPredecessor;

                    if (WasIdentifierChainPredecessorDelimited)
                    {
                        qualifierPart = HsqlStringConverter.toQuotedString(
                            qualifierPart, '"', true);
                    }

                    if (WasDelimitedIdentifier)
                    {
                        token = HsqlStringConverter.toQuotedString(
                            m_token, '"', true);
                    }
                    else
                    {
                        token = m_token;
                    }

                    token = qualifierPart + '.' + token;
                }
                else if (WasDelimitedIdentifier)
                {
                    token = HsqlStringConverter.toQuotedString(
                        m_token, '"', true);
                }
                else if (m_tokenType == SqlTokenType.StringLiteral)
                {
                    token = HsqlStringConverter.toQuotedString(
                        m_token, '\'', true);
                }
                else if (m_tokenType == SqlTokenType.NamedParameter)
                {
                    token = m_parameterNamePrefix + m_parameterName;
                }
                else
                {
                    token = m_token;
                }

                return token;
            }
        }

        #endregion

        #region ParameterName

        /// <summary>
        /// Gets the bare parameter name denoted by the last
        /// read named parameter token.
        /// </summary>
        /// <remarks>
        /// Note: no token type check is performed; if the last
        /// read token is not an SQL named parameter, the value is
        /// <c>null</c>.
        /// </remarks>
        /// <value>
        /// The parameter name, excluding its leading marker character.
        /// </value>
        public string ParameterName
        {
            get
            {
                if (m_wait)
                {
                    throw Tokenizer.IllegalWaitState();
                }

                return m_parameterName;
            }
        }

        #endregion

        #region ParameterNamePrefix

        /// <summary>
        /// Gets the prefix character used to denote the
        /// last read named parameter token.
        /// </summary>
        /// <remarks>
        /// Note: no token type check is performed; if the last
        /// read token is not an SQL named parameter, the value is
        /// the US-ASCII space character (' ').
        /// </remarks>
        /// <value>The named parameter prefix.</value>
        public char ParameterNamePrefix
        {
            get
            {
                if (m_wait)
                {
                    throw Tokenizer.IllegalWaitState();
                }

                return m_parameterNamePrefix;
            }
        }

        #endregion

        #region PartMarker

        /// <summary>
        /// Specifies the present value of the part marker.
        /// </summary>
        /// <remarks>
        /// <see cref="LastPart"/> interprets this value as the start offset.
        /// </remarks>
        /// <value>The part marker.</value>
        public int PartMarker
        {
            get { return m_beginIndex; }
            set { m_beginIndex = value; }
        }

        #endregion

        #region Position

        /// <summary>
        /// Gets the offset into the token stream of the current
        /// character under consideration.
        /// </summary>
        /// <remarks>
        /// <see cref="LastPart"/> uses this value to compute its length.
        /// </remarks>
        /// <value>The current position.</value>
        public int Position
        {
            get { return m_currentIndex; }
        }

        #endregion

        #region TokenType

        /// <summary>
        /// Gets the type of the last read token.
        /// </summary>
        /// <remarks></remarks>
        /// <value>The type of the last read token.</value>
        public SqlTokenType TokenType
        {
            get
            {
                if (m_wait)
                {
                    throw IllegalWaitState();
                }

                switch (m_tokenType)
                {
                    case SqlTokenType.Special:
                        {
                            return (m_token == "?") ? SqlTokenType.ParameterMarker
                                : SqlTokenType.Special;
                        }
                    case SqlTokenType.NumberLiteral:
                    case SqlTokenType.StringLiteral:
                        {
                            // TODO - eliminate this inefficiency
                            Tokenizer.ToLiteralValue(ref m_tokenType, m_token);
                            break;
                        }
                }

                return m_tokenType;
            }
        }

        #endregion

        #region TokenTypeName

        /// <summary>
        /// Gets the type name of the last read token.
        /// </summary>
        /// <value>The type name of the last read token.</value>
        public string TokenTypeName
        {
            get
            {
                if (m_wait)
                {
                    throw IllegalWaitState();
                }

                switch (m_tokenType)
                {
                    case SqlTokenType.NumberLiteral:
                    case SqlTokenType.StringLiteral:
                        {
                            // TODO - eliminate this inefficiecy.
                            Tokenizer.ToLiteralValue(ref m_tokenType, m_token);
                            break;
                        }
                }

                switch (m_tokenType)
                {
                    case SqlTokenType.BooleanLiteral:
                        {
                            return "Boolean";
                        }
                    case SqlTokenType.DateLiteral:
                        {
                            return "Date";
                        }
                    case SqlTokenType.DecimalLiteral:
                        {
                            return "Decimal";
                        }
                    case SqlTokenType.FloatLiteral:
                        {
                            return "Float";
                        }
                    case SqlTokenType.IntegerLiteral:
                        {
                            return "Integer";
                        }
                    case SqlTokenType.BigIntLiteral:
                        {
                            return "BigInt";
                        }
                    case SqlTokenType.IdentifierChain:
                        {
                            return "Identifier Chain";
                        }
                    case SqlTokenType.Name:
                        {
                            return "Name";
                        }
                    case SqlTokenType.NamedParameter:
                        {
                            return "Named Parameter";
                        }
                    case SqlTokenType.None:
                        {
                            return "None";
                        }
                    case SqlTokenType.Null:
                        {
                            return "NULL";
                        }
                    case SqlTokenType.NumberLiteral:
                        {
                            return "Number";
                        }
                    case SqlTokenType.DelimitedIdentifier:
                        {
                            return "Delimited Identifier";
                        }
                    case SqlTokenType.Remark:
                        {
                            return "Remark";
                        }
                    case SqlTokenType.RemarkLine:
                        {
                            return "Remark Line";
                        }
                    case SqlTokenType.Special:
                        {
                            return (m_token == "?")
                                       ? "Parameter Marker"
                                       : "Special";
                        }
                    case SqlTokenType.ParameterMarker:
                        {
                            return "Parameter Marker";
                        }
                    case SqlTokenType.StringLiteral:
                        {
                            return "String";
                        }
                    case SqlTokenType.TimeLiteral:
                        {
                            return "Time";
                        }
                    case SqlTokenType.TimestampLiteral:
                        {
                            return "Timestamp";
                        }
                    default:
                        {
                            return "Unknown";
                        }
                }
            }
        }

        #endregion

        #region WasDelimitedIdentifier

        /// <summary>
        /// Retrieves whether the last read token was an SQL delimited identifier.
        /// </summary>
        /// <remarks>
        /// This value also applies to the subject [right-most] part of an identifier chain.
        /// </remarks>
        /// <returns>
        /// <c>true</c> if the last read token was an SQL delimited identifier;
        /// otherwise, <c>false</c>.
        /// </returns>
        public bool WasDelimitedIdentifier
        {
            get
            {
                if (m_wait)
                {
                    throw Tokenizer.IllegalWaitState();
                }

                return m_wasLastTokenDelimited;
            }
        }

        #endregion

        #region WasIdentifierChain

        /// <summary>
        /// Retrieves whether the last read token was an identifier chain.
        /// </summary>
        /// <returns></returns>
        public bool WasIdentifierChain
        {
            get
            {
                if (m_wait)
                {
                    throw Tokenizer.IllegalWaitState();
                }

                return (m_tokenType == SqlTokenType.IdentifierChain);
            }
        }

        #endregion

        #region WasIdentifierChainFirstDelimited

        /// <summary>
        /// Determines whether the immediate predecessor of
        /// the subject [right-most part] of the last read
        /// chained identifier token is a delimited identifier
        /// token.
        /// </summary>
        /// <remarks>
        /// Note: no token type check is performed;
        /// if the last read token was not an identifier
        /// chain, the value is <c>false</c>.
        /// </remarks>
        /// <returns>
        /// <c>true</c> if the immediate predecessor of
        /// the subject [right-most part] of the last read
        /// multi-part identifier token was delimited;
        /// otherwise, <c>false</c>.
        /// </returns>
        public bool WasIdentifierChainPredecessorDelimited
        {
            get
            {
                if (m_wait)
                {
                    throw Tokenizer.IllegalWaitState();
                }

                return (m_identifierChainPredecessorType
                    == SqlTokenType.DelimitedIdentifier);
            }
        }

        #endregion

        #region WasLiteralValue

        /// <summary>
        /// Retrieves whether the last read token was an SQL literal value.
        /// </summary>
        /// <value>
        /// <c>true</c> if the last read token denoted an SQL
        /// literal value; otherwise, <c>false</c>.
        /// </value>
        /// <remarks>
        /// Called before other WasXXX methods and takes precedence.
        /// </remarks>
        private bool WasLiteralValue
        {
            get
            {
                if (m_wait)
                {
                    throw Tokenizer.IllegalWaitState();
                }

                switch (m_tokenType)
                {
                    case SqlTokenType.BigIntLiteral:
                    case SqlTokenType.BooleanLiteral:
                    case SqlTokenType.DateLiteral:
                    case SqlTokenType.DecimalLiteral:
                    case SqlTokenType.FloatLiteral:
                    case SqlTokenType.IntegerLiteral:
                    case SqlTokenType.NumberLiteral:
                    case SqlTokenType.StringLiteral:
                    case SqlTokenType.TimeLiteral:
                    case SqlTokenType.TimestampLiteral:
                    case SqlTokenType.Null: // checkme
                        {
                            return true;
                        }
                    default:
                        {
                            return false;
                        }
                }
            }
        }

        #endregion

        #region WasName

        /// <summary>
        /// Retrieves whether the last read token was
        /// a valid SQL name.
        /// </summary>
        /// <value>
        /// <c>true</c> if the last read token was a
        /// valid SQL name; otherwise, <c>false</c>.
        /// </value>
        /// <remarks>
        /// In this context, a valid SQL name is defined as
        /// either an SQL delimited identifier, chained identifier
        /// whose subject is either delimited or not an SQL keyword
        /// or an SQL simple identifier that is not also an SQL keyword.
        /// </remarks>
        private bool WasName
        {
            get
            {
                if (m_wait)
                {
                    throw Tokenizer.IllegalWaitState();
                }

                if (WasDelimitedIdentifier)
                {
                    return true;
                }

                if (!((m_tokenType == SqlTokenType.Name)
                      || (m_tokenType == SqlTokenType.IdentifierChain)))
                {
                    return false;
                }

                return !Token.Map.IsKeyword(m_token);
            }
        }

        #endregion

        #region WasNamedParameter

        /// <summary>
        /// Retrives whether the last read token was a named parameter.
        /// </summary>
        /// <returns>
        /// <c>true</c> if the last read token was a named parameter;
        /// otherwise, <c>false</c>.
        /// </returns>
        /// <remarks>
        /// Checks whether the last read token was a named parameter
        /// </remarks>
        public bool WasNamedParameter
        {
            get
            {
                if (m_wait)
                {
                    throw Tokenizer.IllegalWaitState();
                }

                return (m_tokenType == SqlTokenType.NamedParameter);
            }
        }

        #endregion

        #region WasParameterMarker

        /// <summary>
        /// Determines whether the last read token was
        /// a parameter marker (i.e. the '?' token).
        /// </summary>
        /// <value>
        /// <c>true</c> if the last read token was a parameter marker;
        /// otherwise, <c>false</c>.</value>
        public bool WasParameterMarker
        {
            get
            {
                if (m_wait)
                {
                    throw Tokenizer.IllegalWaitState();
                }

                return ((m_tokenType == SqlTokenType.Special)
                        && (m_token == Token.ValueFor.QUESTION));
            }
        }

        #endregion

        #region WasSimpleName

        /// <summary>
        /// Retrieves whether the last read token was an SQL simple name.
        /// </summary>
        /// <value><c>true</c> if last read token was an SQL simple name;
        /// otherwise, <c>false</c>.</value>
        /// <remarks>
        /// In this context, an SQL simple name is a non-keyword character
        /// sequence that is a regular (i.e. non-chained) SQL Identifier.
        /// </remarks>
        private bool WasSimpleName
        {
            get
            {
                if (m_wait)
                {
                    throw Tokenizer.IllegalWaitState();
                }

                if ((m_tokenType == SqlTokenType.DelimitedIdentifier)
                    && (m_token.Length != 0))
                {
                    return true;
                }

                if (m_tokenType != SqlTokenType.Name)
                {
                    return false;
                }

                return !Token.Map.IsKeyword(m_token);
            }
        }

        #endregion

        #region WasSimpleToken

        /// <summary>
        /// Determines whether last read token was simple.
        /// </summary>
        /// <value>
        /// <c>true</c> if last read token was simple;
        /// otherwise, <c>false</c>.
        /// </value>
        /// <remarks>
        /// In this context, a simple token is defined as any
        /// token that is not an SQL identifier chain, SQL
        /// delimited identifier, SQL string literal or SQL named
        /// parameter.
        /// </remarks>
        private bool WasSimpleToken
        {
            get
            {
                switch (m_tokenType)
                {
                    case SqlTokenType.DelimitedIdentifier:
                    case SqlTokenType.IdentifierChain:
                    case SqlTokenType.StringLiteral:
                    case SqlTokenType.NamedParameter:
                        {
                            return false;
                        }
                    default:
                        {
                            return true;
                        }
                }
            }
        }

        #endregion

        #endregion

        #region Constants

        private const StringComparison InvariantCultureIgnoreCase
            = StringComparison.InvariantCultureIgnoreCase;

        #endregion

        #region Static Fields

        private static readonly CultureInfo m_EnglishCulture;
        // literals that are values
        private static readonly org.hsqldb.lib.IntValueHashMap m_ValueTokens;
        private static readonly java.math.BigDecimal m_LongMaxValuePlusOne;

        #endregion

        #region Static Initializer

        /// <summary>
        /// Initializes the <see cref="Tokenizer"/> class.
        /// </summary>
        static Tokenizer()
        {
            m_ValueTokens = new org.hsqldb.lib.IntValueHashMap();

            m_ValueTokens.put(Token.ValueFor.NULL,
                (int)SqlTokenType.Null);
            m_ValueTokens.put(Token.ValueFor.TRUE,
                (int)SqlTokenType.BooleanLiteral);
            m_ValueTokens.put(Token.ValueFor.FALSE,
                (int)SqlTokenType.BooleanLiteral);

            m_EnglishCulture = CultureInfo.GetCultureInfo("en");

            m_LongMaxValuePlusOne
                = JavaBigDecimal.valueOf(long.MaxValue)
                    .add(JavaBigDecimal.valueOf(1));
        }

        #endregion

        #region Static Methods

        #region WrongDataType(HsqlProviderType)
        /// <summary>
        /// Retreives an <c>HsqlDataSourceException</c> indicating that
        /// the data type specified by the given code is incorrect.
        /// </summary>
        /// <param name="type">The data type code.</param>
        /// <returns>a new <c>HsqlDataSourceException</c>.</returns>
        public static HsqlDataSourceException WrongDataType(HsqlProviderType type)
        {
            HsqlException he = HsqlTrace.error(HsqlTrace.WRONG_DATA_TYPE,
                                           HsqlTypes.getTypeString((int)type));

            return new HsqlDataSourceException(he);
        }
        #endregion

        #region IdentiferChainLengthExceeded()

        /// <summary>
        /// Retreives an <c>HsqlDataSourceException</c> indicating that
        /// the maximum legal identifier chain length has been exceeded.
        /// </summary>
        /// <returns>a new <c>HsqlDataSourceException</c>.</returns>
        public static HsqlDataSourceException IdentiferChainLengthExceeded()
        {
            HsqlException he = HsqlTrace.error(HsqlTrace.THREE_PART_IDENTIFIER);
            HsqlDataSourceException ex = new HsqlDataSourceException(he);

            return ex;
        }

        #endregion

        #region UnexpectedEndOfCommand()
        /// <summary>
        /// Retreives an <c>HsqlDataSourceException</c> indicating that
        /// the end of the token stream was reached in an unexpected
        /// fashion.
        /// </summary>
        /// <returns>a new <c>HsqlDataSourceException</c>.</returns>
        public static HsqlDataSourceException UnexpectedEndOfCommand()
        {
            HsqlException he = HsqlTrace.error(HsqlTrace.UNEXPECTED_END_OF_COMMAND);
            HsqlDataSourceException ex = new HsqlDataSourceException(he);

            return ex;
        }

        #endregion

        #region InvalidIdentifier()

        /// <summary>
        /// Retreives an <c>HsqlDataSourceException</c> indicating that
        /// an invalid identifier token was encountered
        /// </summary>
        /// <param name="token">The token.</param>
        /// <returns>a new <c>HsqlDataSourceException</c>.</returns>
        public static HsqlDataSourceException InvalidIdentifier(object token)
        {
            HsqlException he = HsqlTrace.error(HsqlTrace.INVALID_IDENTIFIER, token);
            HsqlDataSourceException ex = new HsqlDataSourceException(he);

            return ex;
        }

        #endregion

        #region IllegalWaitState()

        /// <summary>
        /// Retreives an <c>HsqlDataSourceException</c> indicating that
        /// an invalid wait state was encountered.
        /// </summary>
        /// <remarks>
        /// An invalid wait state occurs when an attempt is made
        /// to query the state of the last read token after it has
        /// been pushed back on to front of the token stream but
        /// before a subsequent ReadToken() invocation has occured.
        /// Presently, this can happen only between an invocation of
        /// <see cref="IsGetThis(string)"/> that returns <c>false</c>
        /// and a subsequenct invocation of <see cref="ReadToken()"/>,
        /// typically via <c>GetNextXXX(...)</c> or <c>GetThis(string)</c>.
        /// </remarks>
        /// <returns>a new <c>HsqlDataSourceException</c>.</returns>
        public static HsqlDataSourceException IllegalWaitState()
        {
            HsqlException he
                = HsqlTrace.error(HsqlTrace.ASSERT_FAILED,
                              "Querying state when in Wait mode");
            HsqlDataSourceException ex = new HsqlDataSourceException(he);

            return ex;
        }

        #endregion

        #region UnexpectedToken(object)

        /// <summary>
        /// Retreives an <c>HsqlDataSourceException</c> indicating that
        /// an unexpected token was encountered
        /// </summary>
        /// <param name="token">The token.</param>
        /// <returns>a new <c>HsqlDataSourceException</c>.</returns>
        public static HsqlDataSourceException UnexpectedToken(object token)
        {
            HsqlException he = HsqlTrace.error(HsqlTrace.UNEXPECTED_TOKEN, token);
            HsqlDataSourceException ex = new HsqlDataSourceException(he);

            return ex;
        }

        #endregion

        #region MatchFailed(object,object)

        /// <summary>
        /// Retreives an <c>HsqlDataSourceException</c> indicating that
        /// the specified <c>match</c> value did not match the
        /// specified <c>token</c>.
        /// </summary>
        /// <param name="token">The token.</param>
        /// <param name="match">The value to match.</param>
        /// <returns>a new <c>HsqlDataSourceException</c>.</returns>
        public static HsqlDataSourceException MatchFailed(object token,
            object match)
        {
            object[] parms = new object[] { token, match };
            HsqlException he = HsqlTrace.error(HsqlTrace.UNEXPECTED_TOKEN,
                HsqlTrace.TOKEN_REQUIRED, parms);
            HsqlDataSourceException ex = new HsqlDataSourceException(he);

            return ex;
        }

        #endregion

        #endregion
    }

    #endregion
}