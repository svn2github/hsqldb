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

using System;
using System.Collections.Generic;
using System.Globalization;
using System.Text;

namespace System.Data.Hsqldb.Common.Sql.Predicate
{
    #region Like
    /// <summary>
    /// Implements SQL <c>LIKE</c> predicate functionality. 
    /// </summary>
    /// <remarks>
    /// This is a quick 'n dirty so that it is possible to filter
    /// transient collections locally, using SQL LIKE matching,
    /// without digging into the HSQLDB engine internals or puttering
    /// around translating from '%' and '_' wild cards with
    /// wild card escape character support to equivalent regular
    /// expressions.
    /// </remarks>
    public class Like
    {
        #region Constants
        private const int GeneralCharacter = 0;
        private const int UnderscoreCharacter = 1;
        private const int PercentCharacter = 2;
        #endregion

        #region Fields
        private char[] m_pattern;
        private int m_patternLength;
        private bool m_patternIsNull;
        private int[] m_patternCharacterType;

        private bool m_ignoreCase;
        private int m_firstWildCardPosition;

        private char? m_escapeCharacter;
        #endregion

        #region Constructors

        #region Like(bool,char?)
        /// <summary>
        /// Constructs a new <c>Like</c> predicate instance with the
        /// given ignore case and wildcard escape behaviour.
        /// </summary>
        /// <param name="ignoreCase">
        /// if set to <c>true</c> ignore case.
        /// </param>
        /// <param name="escapeCharacter">
        /// The wildcard escape character; use <c>null</c> to denote that no
        /// wildcard character escaping is to be performed.
        /// </param>
        public Like(bool ignoreCase, char? escapeCharacter)
        {
            m_ignoreCase = ignoreCase;
            m_escapeCharacter = escapeCharacter;
        }
        #endregion

        #endregion

        #region Public Methods

        #region SetPattern(string)
        /// <summary>
        /// Sets the <c>LIKE</c> pattern.
        /// </summary>
        /// <param name="pattern">The <c>LIKE</c> pattern.</param>
        public void SetPattern(string pattern)
        {
            m_patternIsNull = (pattern == null);

            if (!m_patternIsNull && m_ignoreCase)
            {
                pattern = pattern.ToUpper(CultureInfo.CurrentCulture);
            }

            m_patternLength = 0;
            m_firstWildCardPosition = -1;

            int arrayLength = (m_patternIsNull) ? 0 : pattern.Length;

            m_pattern = new char[arrayLength];
            m_patternCharacterType = new int[arrayLength];

            bool isEscaping = false;
            bool inPercent = false;

            for (int i = 0; i < arrayLength; i++)
            {
                char ch = pattern[i];

                if (!isEscaping)
                {
                    if (m_escapeCharacter != null && ch == (char)m_escapeCharacter)
                    {
                        isEscaping = true;

                        continue;
                    }
                    else if (ch == '_')
                    {
                        m_patternCharacterType[m_patternLength] = UnderscoreCharacter;

                        if (m_firstWildCardPosition == -1)
                        {
                            m_firstWildCardPosition = m_patternLength;
                        }
                    }
                    else if (ch == '%')
                    {
                        if (inPercent)
                        {
                            continue;
                        }

                        inPercent = true;
                        m_patternCharacterType[m_patternLength] = PercentCharacter;

                        if (m_firstWildCardPosition == -1)
                        {
                            m_firstWildCardPosition = m_patternLength;
                        }
                    }
                    else
                    {
                        inPercent = false;
                    }
                }
                else
                {
                    inPercent = false;
                    isEscaping = false;
                }

                m_pattern[m_patternLength++] = ch;
            }

            for (int i = 0; i < m_patternLength - 1; i++)
            {
                if ((m_patternCharacterType[i] == PercentCharacter)
                        && (m_patternCharacterType[i + 1] == UnderscoreCharacter))
                {
                    m_patternCharacterType[i] = UnderscoreCharacter;
                    m_patternCharacterType[i + 1] = PercentCharacter;
                }
            }
        }
        #endregion

        #region Matches(string)
        /// <summary>
        /// Tests if the given value matches this <c>LIKE</c> pattern.
        /// </summary>
        /// <param name="value">The value.</param>
        /// <returns>
        /// <c>true</c> if the given value matches; <c>false</c> otherwise.
        /// </returns>
        public bool? Matches(string value)
        {
            return (value == null)
                ? (bool?)null
                : m_patternIsNull
                ? (bool?)false
                : Matches(
                    (m_ignoreCase
                        ? (value = value.ToUpper(CultureInfo.CurrentCulture))
                        : value),
                /*patternIndex*/0,
                /*valueStartIndex*/0,
                /*valueEndIndex*/value.Length);
        }
        #endregion

        #endregion

        #region Private Methods

        #region Matches(string,int,int,int)
        /// <summary>
        /// Tests if the specified range of the specified value matches this <c>LIKE</c>
        /// pattern, starting from the given character in this pattern.
        /// </summary>
        /// <param name="value">The value to match.</param>
        /// <param name="patternIndex">Pattern start index.</param>
        /// <param name="valueStartIndex">Value start index.</param>
        /// <param name="valueStopIndex">Value stop index.</param>
        /// <returns>
        /// <c>true</c> if the given value matches; <c>false</c> otherwise.
        /// </returns>
        private bool Matches(string value, int patternIndex, int valueStartIndex, int valueStopIndex)
        {

            for (; patternIndex < m_patternLength; patternIndex++)
            {
                switch (m_patternCharacterType[patternIndex])
                {
                    case GeneralCharacter:
                        {
                            if ((valueStartIndex >= valueStopIndex)
                                || (m_pattern[patternIndex] != value[valueStartIndex++]))
                            {
                                return false;
                            }
                            break;
                        }
                    case UnderscoreCharacter:
                        {
                            // match any single character
                            if (valueStartIndex++ >= valueStopIndex)
                            {
                                return false;
                            }
                            break;
                        }
                    case PercentCharacter:
                        {
                            // percent: match any (zero or more) characters
                            if (++patternIndex >= m_patternLength)
                            {
                                return true;
                            }

                            while (valueStartIndex < valueStopIndex)
                            {
                                if ((m_pattern[patternIndex] == value[valueStartIndex])
                                        && Matches(value, patternIndex, valueStartIndex, valueStopIndex))
                                {
                                    return true;
                                }

                                valueStartIndex++;
                            }

                            return false;
                        }
                }
            }

            return (valueStartIndex == valueStopIndex);
        }
        #endregion

        #endregion
    }
    #endregion
}
