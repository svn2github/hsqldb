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
using System.Globalization;
using System.Data.Hsqldb.Common.Enumeration;
using BigDecimal = java.math.BigDecimal;
using Boolean = java.lang.Boolean;
using Character = java.lang.Character;
using Double = java.lang.Double;
using Exception = System.Exception;
using Integer = java.lang.Integer;
using JavaSystem = org.hsqldb.lib.java.JavaSystem;
using HsqlDateTime = org.hsqldb.HsqlDateTime;
using HsqlException = org.hsqldb.HsqlException;
using Long = java.lang.Long;
using Number = java.lang.Number;
using StringConverter = org.hsqldb.lib.StringConverter;
using Trace = org.hsqldb.Trace;
using HsqlTypes = org.hsqldb.Types;
using ValuePool = org.hsqldb.store.ValuePool;
#endregion

namespace System.Data.Hsqldb.Common.Sql
{
    #region HsqlTokenizer

    /// <summary>
    /// <para>
    /// Supports lexographic analysis of the HSQLDB SQL dialect.
    /// </para>
    /// <img src="/ClassDiagrams/System.Data.Hsqldb.Common.HsqlTokenizer.png"
    ///      alt="HsqlTokenizer Class Diagram"/>
    /// </summary>
    public sealed class Tokenizer
    {
        #region Instance Fields

        private bool m_acceptParameterMarkers = true;
        private bool m_acceptNamedParameters = true;
        private int m_beginIndex;
        private string m_chars;
        private int m_charsLength;
        private int m_currentIndex;
        // NOTE: ReadToken() will clear m_identifierChainFirst unless
        // m_retainIdentifierChainFirst is set true.
        // TODO: implement handling of arbitrary length chained identifiers
        //       as soon as similar handling is backported to 1.8.0.x series
        //       engine.
        private string m_identifierChainFirst = null;
        private TokenType m_identifierChainFirstType = TokenType.None;
        private int m_nextTokenIndex;
        private string m_parameterName;
        private char m_parameterNamePrefix = ' ';
        private bool m_retainIdentifierChainFirst = false;
        private string m_token;
        private int m_tokenIndex;
        private TokenType m_tokenType = TokenType.None;
        private bool m_wait;
        private bool m_wasLastTokenDelimited;

        #endregion

        #region Constructors

        #region HsqlTokenizer()

        /// <summary>
        /// Initializes a new instance of the <see cref="Tokenizer"/> class.
        /// </summary>
        public Tokenizer()
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
                throw IllegalWaitState();
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
                        throw UnexpectedEndOfCommand();
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
            catch (Exception)
            {
                //e.ToString();
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

            if (m_token.Equals("-"))
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
                        if (isNegative
                            && m_LongMaxValuePlusOne.equals(objectValue))
                        {
                            return long.MinValue;
                        }

                        throw WrongDataType(tokenDataType);
                    }
                default:
                    {
                        throw WrongDataType(tokenDataType);
                    }
            }

            long longValue = ((Number)objectValue).longValue();

            return isNegative
                       ? -longValue
                       : longValue;
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
                throw WrongDataType(LiteralValueDataType);
            }

            return (int)value;
        }

        #endregion

        #region GetNextAsLiteralValue(HsqlProviderType)

        /// <summary>
        /// Gets the next token as a literal value of
        /// the requested SQL data type.
        /// </summary>
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
                    value = HsqlConvert
                            .FromJava
                            .ToObject(value, (int)requestedDataType);
                    value = HsqlConvert
                            .FromDotNet
                            .ToObject(value, (int)requestedDataType);
                }
                catch (Exception)
                {
                    throw WrongDataType(dataType);
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

            throw UnexpectedToken(m_token);
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

            string token = (m_tokenType == TokenType.IdentifierChain)
                               ? m_identifierChainFirst
                               : m_token;

            throw UnexpectedToken(token);
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

            string token = (m_tokenType == TokenType.IdentifierChain)
                               ? m_identifierChainFirst
                               : m_token;

            throw UnexpectedToken(token);
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
        /// Gets a substring from the character sequence being tokenized
        /// </summary>
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

            if ((m_tokenType != TokenType.DelimitedIdentifier)
                && (m_tokenType != TokenType.IdentifierChain)
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
                throw IllegalWaitState();
            }

            if (!m_token.Equals(match)
                || m_tokenType == TokenType.DelimitedIdentifier
                || m_tokenType == TokenType.IdentifierChain)
            {
                string token = (m_tokenType == TokenType.IdentifierChain)
                                   ? m_identifierChainFirst
                                   : m_token;

                throw MatchFailed(token, match);
            }
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

            if (!m_retainIdentifierChainFirst)
            {
                m_identifierChainFirst = null;
                m_identifierChainFirstType = TokenType.None;
            }

            while (m_currentIndex < m_charsLength
                   && Character.isWhitespace(m_chars[m_currentIndex]))
            {
                m_currentIndex++;
            }

            m_token = "";
            m_parameterName = "";
            m_parameterNamePrefix = ' ';
            m_tokenIndex = m_currentIndex;

            if (m_currentIndex >= m_charsLength)
            {
                // character sequence has been exhausted.
                m_tokenType = TokenType.None;

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

            if (Character.isJavaIdentifierStart(c))
            {
                m_tokenType = TokenType.Name;
            }
            else if (Character.isDigit(c))
            {
                m_tokenType = TokenType.NumberLiteral;
                digit = true;
            }
            else
            {
                switch (c)
                {
                    case '(':
                        {
                            m_token = Token.ValueFor.OPENBRACKET;
                            m_tokenType = TokenType.Special;

                            m_currentIndex++;

                            return;
                        }
                    case ')':
                        {
                            m_token = Token.ValueFor.CLOSEBRACKET;
                            m_tokenType = TokenType.Special;

                            m_currentIndex++;

                            return;
                        }
                    case ',':
                        {
                            m_token = Token.ValueFor.COMMA;
                            m_tokenType = TokenType.Special;

                            m_currentIndex++;

                            return;
                        }
                    case '*':
                        {
                            m_token = Token.ValueFor.MULTIPLY;
                            m_tokenType = TokenType.Special;

                            m_currentIndex++;

                            return;
                        }
                    case '=':
                        {
                            m_token = Token.ValueFor.EQUALS;
                            m_tokenType = TokenType.Special;

                            m_currentIndex++;

                            return;
                        }
                    case ';':
                        {
                            m_token = Token.ValueFor.SEMICOLON;
                            m_tokenType = TokenType.Special;

                            m_currentIndex++;

                            return;
                        }
                    case '+':
                        {
                            m_token = Token.ValueFor.PLUS;
                            m_tokenType = TokenType.Special;

                            m_currentIndex++;

                            return;
                        }
                    case '%':
                        {
                            m_token = Token.ValueFor.PERCENT;
                            m_tokenType = TokenType.Special;

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
                            m_tokenType = TokenType.Special;

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
                            //Trace.check(++m_currentIndex < m_charsLength,
                            //            Trace.UNEXPECTED_END_OF_COMMAND);

                            c = m_chars[m_currentIndex];

                            if (!Character.isJavaIdentifierStart(c))
                            {
                                throw InvalidIdentifier(":" + c);
                            }
                            //Trace.check(Character.isJavaIdentifierStart(c),
                            //            Trace.INVALID_IDENTIFIER, ":" + c);

                            m_tokenType = TokenType.NamedParameter;
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
                            //Trace.check(++m_currentIndex < m_charsLength,
                            //            Trace.UNEXPECTED_END_OF_COMMAND);

                            c = m_chars[m_currentIndex];

                            if (!Character.isJavaIdentifierStart(c))
                            {
                                throw InvalidIdentifier(":" + c);
                            }
                            //Trace.check(Character.isJavaIdentifierStart(c),
                            //            Trace.INVALID_IDENTIFIER, "@" + c);

                            m_tokenType = TokenType.NamedParameter;
                            m_parameterNamePrefix = '@';

                            break;
                        }
                    case '"':
                        {
                            m_wasLastTokenDelimited = true;
                            m_tokenType = TokenType.DelimitedIdentifier;

                            m_currentIndex++;

                            m_token = GetAsQuotedString('"');

                            if (m_currentIndex == m_chars.Length)
                            {
                                return;
                            }

                            c = m_chars[m_currentIndex];

                            if (c == '.')
                            {
                                m_identifierChainFirst = m_token;
                                m_identifierChainFirstType = m_tokenType;

                                m_currentIndex++;

                                if (m_retainIdentifierChainFirst)
                                {
                                    //throw IdentiferChainLengthExceeded();
                                }

                                // TODO: avoid recursion
                                // This has problems when there is whitespace
                                // after the dot; same with NAME
                                m_retainIdentifierChainFirst = true;

                                ReadToken();

                                m_retainIdentifierChainFirst = false;
                                m_tokenType = TokenType.IdentifierChain;
                            }

                            return;
                        }
                    case '\'':
                        {
                            m_tokenType = TokenType.StringLiteral;

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
                            m_tokenType = TokenType.Special;

                            break;
                        }
                    case '.':
                        {
                            m_tokenType = TokenType.DecimalLiteral;
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

                    if (m_tokenType == TokenType.StringLiteral
                        || m_tokenType == TokenType.DelimitedIdentifier)
                    {
                        throw UnexpectedEndOfCommand();
                    }
                }
                else
                {
                    c = m_chars[m_currentIndex];
                }

                switch (m_tokenType)
                {
                    case TokenType.NamedParameter:
                    case TokenType.Name:
                        {
                            if (Character.isJavaIdentifierPart(c))
                            {
                                break;
                            }

                            m_token = m_chars
                                .Substring(start, m_currentIndex - start)
                                .ToUpper(m_EnglishCulture);

                            if (m_tokenType == TokenType.NamedParameter)
                            {
                                m_parameterName
                                    = m_chars.Substring(
                                        start,
                                        (m_currentIndex - start));

                                return;
                            }

                            // only for Name, not for NamedParameter
                            if (c == '.')
                            {
                                m_identifierChainFirstType = m_tokenType;
                                m_identifierChainFirst = m_token;

                                m_currentIndex++;

                                if (m_retainIdentifierChainFirst)
                                {
                                    //throw IdentiferChainLengthExceeded();
                                }

                                m_retainIdentifierChainFirst = true;

                                // TODO: eliminate recursion
                                ReadToken();

                                m_retainIdentifierChainFirst = false;
                                m_tokenType = TokenType.IdentifierChain;
                            }
                            else if (c == '(')
                            {
                                // potentially a function call or SQL operator
                            }
                            else
                            {
                                // if in value list then it is a value
                                int type = m_ValueTokens.get(m_token, -1);

                                if (type != -1)
                                {
                                    m_tokenType = (TokenType)type;
                                }
                            }

                            return;
                        }
                    case TokenType.DelimitedIdentifier:
                    case TokenType.StringLiteral:
                        {
                            // shouldn't get here
                            break;
                        }
                    case TokenType.Remark:
                        {
                            if (end)
                            {
                                // unfinished remark
                                // TODO: log/trace error condition here
                                m_tokenType = TokenType.None;

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
                    case TokenType.RemarkLine:
                        {
                            if (end)
                            {
                                m_tokenType = TokenType.None;

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
                    case TokenType.Special:
                        {
                            if (c == '/' && cfirst == '/')
                            {
                                m_tokenType = TokenType.RemarkLine;

                                break;
                            }
                            else if (c == '-' && cfirst == '-')
                            {
                                m_tokenType = TokenType.RemarkLine;

                                break;
                            }
                            else if (c == '*' && cfirst == '/')
                            {
                                m_tokenType = TokenType.Remark;

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
                    case TokenType.NumberLiteral:
                    case TokenType.FloatLiteral:
                    case TokenType.DecimalLiteral:
                        {
                            if (Character.isDigit(c))
                            {
                                digit = true;
                            }
                            else if (c == '.')
                            {
                                m_tokenType = TokenType.DecimalLiteral;

                                if (point)
                                {
                                    throw UnexpectedToken(c);
                                }

                                point = true;
                            }
                            else if (c == 'E' || c == 'e')
                            {
                                if (exp)
                                {
                                    throw UnexpectedToken(c);
                                }

                                m_tokenType = TokenType.FloatLiteral;

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
                                        m_tokenType = TokenType.Special;

                                        return;
                                    }

                                    throw UnexpectedToken(c);
                                }

                                m_token
                                    = m_chars.Substring(
                                        start,
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
        /// The character sequence to be tokenized.
        /// </param>
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
            m_tokenType = TokenType.None;
            m_identifierChainFirstType = TokenType.None;
            m_token = null;
            m_parameterName = null;
            m_parameterNamePrefix = ' ';
            m_identifierChainFirst = null;
            m_wait = false;
            m_wasLastTokenDelimited = false;
            m_retainIdentifierChainFirst = false;
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

        #region WasThis(string)

        /// <summary>
        /// Determines whether the last read token is equal to
        /// the specified <c>match</c> value.
        /// </summary>
        /// <param name="match">The value to match.</param>
        /// <returns>
        /// <c>true</c> if the last token was equal to the
        /// specified <c>match</c> value; otherwise, <c>false</c>.
        /// </returns>
        public bool WasThis(string match)
        {
            return m_token.Equals(match)
                   && (m_tokenType != TokenType.DelimitedIdentifier)
                   && (m_tokenType != TokenType.IdentifierChain);
        }

        #endregion

        #endregion

        #region Instance Properties

        #region IdentifierChainFirst

        /// <summary>
        /// Gets the first component of the identifier chain.
        /// </summary>
        /// <remarks>
        /// Note: no token type check is performed; if the last read
        /// token is not an identifier chain, the value is <c>null</c>.
        /// </remarks>
        /// <value>The first component of the identifier chain.</value>
        public string IdentifierChainFirst
        {
            get
            {
                if (m_wait)
                {
                    throw IllegalWaitState();
                }

                return m_identifierChainFirst;
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
            get {return m_chars.Substring(m_beginIndex,
                                          m_currentIndex - m_beginIndex); }
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
                    throw UnexpectedToken(m_token);
                }

                switch (m_tokenType)
                {
                    case TokenType.Null:
                        {
                            return null;
                        }
                    case TokenType.StringLiteral:
                        {
                            return m_token;
                        }
                    case TokenType.BigIntLiteral:
                        {
                            return ValuePool.getLong(
                                Long.parseLong(m_token));
                        }
                    case TokenType.NumberLiteral:
                        {
                            // Returns unsigned values which
                            // are later negated. As a result,
                            // Integer.MIN_VALUE or Long.MIN_VALUE
                            // are promoted to a wider type.
                            if (m_token.Length < 11)
                            {
                                try
                                {
                                    return ValuePool.getInt(
                                        Integer.parseInt(m_token));
                                }
                                catch (Exception)
                                {
                                }
                            }

                            if (m_token.Length < 20)
                            {
                                try
                                {
                                    m_tokenType = TokenType.BigIntLiteral;

                                    return ValuePool.getLong(
                                        Long.parseLong(m_token));
                                }
                                catch (Exception)
                                {
                                }
                            }

                            m_tokenType = TokenType.DecimalLiteral;

                            return new BigDecimal(m_token);
                        }
                    case TokenType.FloatLiteral:
                        {
                            double d = JavaSystem.parseDouble(m_token);
                            long l = java.lang.Double.doubleToLongBits(d);

                            return ValuePool.getDouble(l);
                        }
                    case TokenType.DecimalLiteral:
                        {
                            return new BigDecimal(m_token);
                        }
                    case TokenType.BooleanLiteral:
                        {
                            return m_token.Equals("TRUE", m_IgnoreCase)
                                       ? java.lang.Boolean.TRUE
                                       : java.lang.Boolean.FALSE;
                        }
                    case TokenType.DateLiteral:
                        {
                            return HsqlDateTime.dateValue(m_token);
                        }
                    case TokenType.TimeLiteral:
                        {
                            return HsqlDateTime.timeValue(m_token);
                        }
                    case TokenType.TimestampLiteral:
                        {
                            return HsqlDateTime.timestampValue(m_token);
                        }
                    default:
                        {
                            return m_token;
                        }
                }
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
                    throw IllegalWaitState();
                }

                // TODO: make sure this is used only for valued tokens.
                switch (m_tokenType)
                {
                    case TokenType.StringLiteral:
                        return HsqlProviderType.VarChar;

                    case TokenType.NumberLiteral:
                        return HsqlProviderType.Integer;

                    case TokenType.BigIntLiteral:
                        return HsqlProviderType.BigInt;

                    case TokenType.FloatLiteral:
                        return HsqlProviderType.Double;

                    case TokenType.DecimalLiteral:
                        return HsqlProviderType.Decimal;

                    case TokenType.BooleanLiteral:
                        return HsqlProviderType.Boolean;

                    case TokenType.DateLiteral:
                        return HsqlProviderType.Date;

                    case TokenType.TimeLiteral:
                        return HsqlProviderType.Time;

                    case TokenType.TimestampLiteral:
                        return HsqlProviderType.TimeStamp;

                    default:
                        return HsqlProviderType.Null;
                }
            }
        }

        #endregion

        #region NormalizedToken

        /// <summary>
        /// Gets the last read token as it would actually
        /// appear in an SQL statement.
        /// </summary>
        /// <remarks>
        /// The normalized form excludes leading, internal
        /// and trailing whitespace.
        /// </remarks>
        /// <value>
        /// The normalized character sequence denoted by the last read token.
        /// </value>
        public string NormalizedToken
        {
            get
            {
                if (m_wait)
                {
                    throw IllegalWaitState();
                }

                string token;

                if (WasIdentifierChain)
                {
                    string first = IdentifierChainFirst;

                    if (WasIdentifierChainFirstDelimited)
                    {
                        first = StringConverter.toQuotedString(
                            first, '"', true);
                    }

                    if (WasDelimitedIdentifier)
                    {
                        token = StringConverter.toQuotedString(
                            m_token, '"', true);
                    }
                    else
                    {
                        token = m_token;
                    }

                    token = first + '.' + token;
                }
                else if (WasDelimitedIdentifier)
                {
                    token = StringConverter.toQuotedString(
                        m_token, '"', true);
                }
                else if (m_tokenType == TokenType.StringLiteral)
                {
                    token = StringConverter.toQuotedString(
                        m_token, '\'', true);
                }
                else if (m_tokenType == TokenType.NamedParameter)
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
                    throw IllegalWaitState();
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
                    throw IllegalWaitState();
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
        /// <value>The type of the last read token.</value>
        public TokenType TokenType
        {
            get
            {
                if (m_wait)
                {
                    throw IllegalWaitState();
                }

                return (m_tokenType == TokenType.Special)
                           ? (m_token == "?")
                                 ? TokenType.ParameterMarker
                                 : m_tokenType
                           : m_tokenType;
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
                    case TokenType.BooleanLiteral:
                        {
                            return "Boolean";
                        }
                    case TokenType.DateLiteral:
                        {
                            return "Date";
                        }
                    case TokenType.DecimalLiteral:
                        {
                            return "Decimal";
                        }
                    case TokenType.FloatLiteral:
                        {
                            return "Float";
                        }
                    case TokenType.BigIntLiteral:
                        {
                            return "BigInt";
                        }
                    case TokenType.IdentifierChain:
                        {
                            return "Identifier Chain";
                        }
                    case TokenType.Name:
                        {
                            return "Name";
                        }
                    case TokenType.NamedParameter:
                        {
                            return "Named Parameter";
                        }
                    case TokenType.None:
                        {
                            return "None";
                        }
                    case TokenType.Null:
                        {
                            return "NULL";
                        }
                    case TokenType.NumberLiteral:
                        {
                            return "Number";
                        }
                    case TokenType.DelimitedIdentifier:
                        {
                            return "Delimited Identifier";
                        }
                    case TokenType.Remark:
                        {
                            return "Remark";
                        }
                    case TokenType.RemarkLine:
                        {
                            return "Remark Line";
                        }
                    case TokenType.Special:
                        {
                            return (m_token == "?")
                                       ? "Parameter Marker"
                                       : "Special";
                        }
                    case TokenType.StringLiteral:
                        {
                            return "String";
                        }
                    case TokenType.TimeLiteral:
                        {
                            return "Time";
                        }
                    case TokenType.TimestampLiteral:
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
        /// Retrieves whether the last read token
        /// was an SQL delimited identifier.
        /// </summary>
        /// <returns>
        /// <c>true</c> if the last read token was
        /// an SQL delimited identifier;
        /// otherwise, <c>false</c>.
        /// </returns>
        public bool WasDelimitedIdentifier
        {
            get
            {
                if (m_wait)
                {
                    throw IllegalWaitState();
                }

                return m_wasLastTokenDelimited;

                // m_tokenType won't help for LONG_NAMEs.
                //return (m_tokenType == QUOTED_IDENTIFIER);
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
                    throw IllegalWaitState();
                }

                return (m_tokenType == TokenType.IdentifierChain);
            }
        }

        #endregion

        #region WasIdentifierChainFirstDelimited

        /// <summary>
        /// Determines whether the first part of
        /// the last read chained identifier
        /// token is a delimited identifier.
        /// </summary>
        /// <remarks>
        /// Note: no token type check is performed;
        /// if the last read token was not an identifier
        /// chain, the value is <c>false</c>.
        /// </remarks>
        /// <returns>
        /// <c>true</c> if the first part of the last read
        /// multi-part identifier token was delimited;
        /// otherwise, <c>false</c>.
        /// </returns>
        public bool WasIdentifierChainFirstDelimited
        {
            get
            {
                if (m_wait)
                {
                    throw IllegalWaitState();
                }

                return (m_identifierChainFirstType
                    == TokenType.DelimitedIdentifier);
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
                    throw IllegalWaitState();
                }

                switch (m_tokenType)
                {
                    case TokenType.StringLiteral:
                    case TokenType.NumberLiteral:
                    case TokenType.BigIntLiteral:
                    case TokenType.FloatLiteral:
                    case TokenType.DecimalLiteral:
                    case TokenType.BooleanLiteral:
                    case TokenType.Null: // checkme
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
        /// an SQL identifier chain, an SQL delimited identifier
        /// or an SQL simple identifier that is not also defined
        /// as an SQL keyword.
        /// </remarks>
        private bool WasName
        {
            get
            {
                if (m_wait)
                {
                    throw IllegalWaitState();
                }

                if (m_tokenType == TokenType.DelimitedIdentifier)
                {
                    return true;
                }

                if (!((m_tokenType == TokenType.Name)
                      || (m_tokenType == TokenType.IdentifierChain)))
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
                    throw IllegalWaitState();
                }

                return (m_tokenType == TokenType.NamedParameter);
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
                    throw IllegalWaitState();
                }

                return ((m_tokenType == TokenType.Special)
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
                    throw IllegalWaitState();
                }

                if ((m_tokenType == TokenType.DelimitedIdentifier)
                    && (m_token.Length != 0))
                {
                    return true;
                }

                if (m_tokenType != TokenType.Name)
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
                    case TokenType.DelimitedIdentifier:
                    case TokenType.IdentifierChain:
                    case TokenType.StringLiteral:
                    case TokenType.NamedParameter:
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

        private const StringComparison m_IgnoreCase
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
                (int)TokenType.Null);
            m_ValueTokens.put(Token.ValueFor.TRUE,
                (int)TokenType.BooleanLiteral);
            m_ValueTokens.put(Token.ValueFor.FALSE,
                (int)TokenType.BooleanLiteral);

            m_EnglishCulture = CultureInfo.GetCultureInfo("en");

            m_LongMaxValuePlusOne
                = BigDecimal.valueOf(long.MaxValue)
                    .add(BigDecimal.valueOf(1));
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
            HsqlException he = Trace.error(Trace.WRONG_DATA_TYPE,
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
            HsqlException he = Trace.error(Trace.THREE_PART_IDENTIFIER);
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
            HsqlException he = Trace.error(Trace.UNEXPECTED_END_OF_COMMAND);
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
            HsqlException he = Trace.error(Trace.INVALID_IDENTIFIER, token);
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
                = Trace.error(Trace.ASSERT_FAILED,
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
            HsqlException he = Trace.error(Trace.UNEXPECTED_TOKEN, token);
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
        public static HsqlDataSourceException MatchFailed(
            object token,
            object match)
        {
            object[] parms = new object[] { token, match };
            HsqlException he = Trace.error(Trace.UNEXPECTED_TOKEN,
                                           Trace.TOKEN_REQUIRED,
                                           parms);
            HsqlDataSourceException ex = new HsqlDataSourceException(he);

            return ex;
        }

        #endregion

        #endregion
    }

    #endregion
}