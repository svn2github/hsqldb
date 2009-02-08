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
using System.Collections;
using System.Collections.Generic;
using System.Data.Hsqldb.Common.Enumeration;
using System.Text;

using Trace = org.hsqldb.Trace;
using System.Data.Hsqldb.Common;
using System.Data.Hsqldb.Common.Sql;

#endregion

namespace System.Data.Hsqldb.Client.Sql
{

        #region TokenList

        /// <summary>
        /// <para>
        /// Represents an ordered sequence of strongly-typed [SQL] tokens.
        /// </para>
        /// <img src="/ClassDiagrams/System.Data.Hsqldb.Client.HsqlTokenList.png"
        ///      alt="HsqlTokenList Class Diagram"/>
        /// </summary>
        public class TokenList : IEnumerable<Token>, ICloneable
        {
            #region Fields

            private static readonly int[] m_NoOrdinals = new int[0];

            private List<Token> m_list = new List<Token>();
            private int[] m_parameterMarkerTokenPositions;
            private int[] m_namedParameterTokenPositions;
            private int[] m_parameterTokenPositions;
            private Dictionary<string, int[]> m_nameToTokenOrdinals;
            private Dictionary<string, int[]> m_nameToBindOrdinals;
            private int m_normalizedCapacity;

            #endregion

            #region Constructors

            #region List(string)
            /// <summary>
            /// Creates a new list from the given SQL character sequence.
            /// </summary>
            /// <param name="sql">The SQL character sequence.</param>
            public TokenList(string sql)
            {
                Tokenizer tokenizer = new Tokenizer(sql);

                int parameterMarkerCount = 0;
                int namedParameterCount = 0;
                string tokenValue;

                while (!string.IsNullOrEmpty(tokenValue = tokenizer.GetNextAsString()))
                {
                    string normalizedTokenValue = tokenizer.NormalizedToken;
                    TokenType type = tokenizer.TokenType;

                    if (Token.ValueFor.COMMA != normalizedTokenValue)
                    {
                        // each non-comma token is space-separated
                        // in the normalized form.
                        m_normalizedCapacity++;
                    }

                    m_normalizedCapacity += normalizedTokenValue.Length;

                    switch (type)
                    {
                        case TokenType.ParameterMarker:
                            {
                                parameterMarkerCount++;
                                break;
                            }
                        case TokenType.NamedParameter:
                            {
                                namedParameterCount++;
                                break;
                            }
                    }

                    switch (type)
                    {
                        case TokenType.IdentifierChain:
                            {
                                string identifierChainFirst =tokenizer.IdentifierChainFirst;
                                string identifierChainLast = tokenValue;

                                m_list.Add(new Token(normalizedTokenValue, type,
                                    identifierChainFirst, identifierChainLast));
                                break;
                            }
                        default:
                            {
                                m_list.Add(new Token(normalizedTokenValue, type));
                                break;
                            }
                    }
                }

                m_namedParameterTokenPositions = new int[namedParameterCount];
                m_parameterMarkerTokenPositions = new int[parameterMarkerCount];

                m_parameterTokenPositions
                    = new int[namedParameterCount + parameterMarkerCount];
                Dictionary<string, List<int>> nameToTokenOrdinals
                    = new Dictionary<string, List<int>>(namedParameterCount);
                Dictionary<string, List<int>> nameToBindOrdinals
                    = new Dictionary<string, List<int>>(namedParameterCount);

                int bindOrdinal = 0;
                int namedParameterIndex = 0;
                int parameterMarkerIndex = 0;
                int parameterIndex = 0;
                int count = m_list.Count;

                for (int i = 0; i < count; i++)
                {
                    Token tokenValue = m_list[i];
                    TokenType type = tokenValue.Type;

                    switch (type)
                    {
                        case TokenType.NamedParameter:
                            {
                                List<int> tokenOrdinals;
                                List<int> bindOrdinals = null;

                                string key = tokenValue.Value;

                                if (!nameToTokenOrdinals.TryGetValue(
                                        key,
                                        out tokenOrdinals))
                                {
                                    tokenOrdinals = new List<int>();
                                    bindOrdinals = new List<int>();

                                    nameToTokenOrdinals[key] = tokenOrdinals;
                                    nameToBindOrdinals[key] = bindOrdinals;
                                }

                                tokenOrdinals.Add(i);
                                bindOrdinals.Add(bindOrdinal++);

                                m_namedParameterTokenPositions[
                                    namedParameterIndex++] = i;
                                m_parameterTokenPositions[
                                    parameterIndex++] = i;

                                break;
                            }
                        case TokenType.ParameterMarker:
                            {
                                m_parameterMarkerTokenPositions[
                                    parameterMarkerIndex++] = i;
                                m_parameterTokenPositions[parameterIndex++] = i;

                                break;
                            }
                    }
                }

                m_nameToTokenOrdinals = new Dictionary<string, int[]>();

                foreach (string parameterName in nameToTokenOrdinals.Keys)
                {
                    m_nameToTokenOrdinals[parameterName]
                        = nameToTokenOrdinals[parameterName].ToArray();
                }

                m_nameToBindOrdinals = new Dictionary<string, int[]>();

                foreach (string parameterName in nameToBindOrdinals.Keys)
                {
                    m_nameToBindOrdinals[parameterName]
                        = nameToBindOrdinals[parameterName].ToArray();
                }
            }
            #endregion

            #region TokenList(List)
            /// <summary>
            /// Constructs a copy of the given <c>HsqlTokenList</c>.
            /// </summary>
            /// <param name="from">which to copy.</param>
            public TokenList(TokenList from)
            {
                m_list = new List<Token>(from);
                m_parameterMarkerTokenPositions
                    = (int[])from.m_parameterMarkerTokenPositions.Clone();
                m_namedParameterTokenPositions
                    = (int[])from.m_namedParameterTokenPositions.Clone();
                m_parameterTokenPositions
                    = (int[])from.m_parameterTokenPositions.Clone();
                m_nameToTokenOrdinals
                    = new Dictionary<string, int[]>(from.m_nameToTokenOrdinals);
                m_normalizedCapacity = from.m_normalizedCapacity;
            }
            #endregion

            #endregion

            #region Public Properties

            #region this[int]

            /// <summary>
            /// Gets the <see cref="Token"/> at the specified index.
            /// </summary>
            /// <value>
            /// The token at the specified index.
            /// </value>
            public Token this[int index]
            {
                get { return m_list[index]; }
            }

            #endregion

            #region Count

            /// <summary>
            /// Gets the count of the tokens in this list.
            /// </summary>
            /// <value>The token count.</value>
            public int Count
            {
                get { return m_list.Count; }
            }

            #endregion

            #region NamedParameterCount

            /// <summary>
            /// Gets the named parameter count.
            /// </summary>
            /// <value>The named parameter count.</value>
            public int NamedParameterCount
            {
                get { return m_namedParameterTokenPositions.Length; }
            }

            #endregion

            #region NamedParameterPositions

            /// <summary>
            /// Gets a cloned copy of the named parameter positions array.
            /// </summary>
            /// <value>A cloned copy of the named parameter positions array.</value>
            public int[] NamedParameterTokenPositions
            {
                get { return (int[])m_namedParameterTokenPositions.Clone(); }
            }

            #endregion

            #region ParameterCount

            /// <summary>
            /// Gets the total parameter token count.
            /// </summary>
            /// <value>The total parameter token count.</value>
            public int ParameterCount
            {
                get { return m_parameterTokenPositions.Length; }
            }

            #endregion

            #region ParameterMarkerCount

            /// <summary>
            /// Gets the parameter marker count.
            /// </summary>
            /// <value>The parameter marker count.</value>
            public int ParameterMarkerCount
            {
                get { return m_parameterMarkerTokenPositions.Length; }
            }

            #endregion

            #region ParameterMarkerPositions

            /// <summary>
            /// Gets a cloned copy of the internal
            /// parameter marker positions array.
            /// </summary>
            /// <value>
            /// A cloned copy of the internal
            /// parameter marker positions array.
            /// </value>
            public int[] ParameterMarkerTokenPositions
            {
                get { return (int[])m_parameterMarkerTokenPositions.Clone(); }
            }

            #endregion

            #region ParameterPositions

            /// <summary>
            /// Gets a cloned copy of the parameter positions array.
            /// </summary>
            /// <value>A cloned copy of the parameter positions array.</value>
            public int[] ParameterTokenPositions
            {
                get { return (int[])m_parameterTokenPositions.Clone(); }
            }

            #endregion

            #endregion

            #region Internal Properties

            #region NamedParameterPositionsInternal

            /// <summary>
            /// Gets a direct reference to the named parameter positions array.
            /// </summary>
            /// <value>The named parameter positions array.</value>
            internal int[] NamedParameterTokenPositionsInternal
            {
                get { return m_namedParameterTokenPositions; }
            }

            #endregion

            #region ParameterMarkerPositionsInternal

            /// <summary>
            /// Gets a direct reference to the parameter marker positions array.
            /// </summary>
            /// <value>The parameter marker positions array.</value>
            internal int[] ParameterMarkerTokenPositionsInternal
            {
                get { return m_parameterMarkerTokenPositions; }
            }

            #endregion

            #region ParameterPositionsInternal

            /// <summary>
            /// Gets a direct referernce to the parameter positions.
            /// </summary>
            /// <value>The parameter positions.</value>
            internal int[] ParameterTokenPositionsInternal
            {
                get { return m_parameterTokenPositions; }
            }

            #endregion

            #endregion

            #region Public Instance Methods

            #region AppendParameterMarkerForm(StringBuilder,string)

            /// <summary>
            /// Appends to the given <c>StringBuilder</c> the string representation
            /// of this list in which all named parameter tokens (@name or :name)
            /// are replaced with the given <c>marker</c> value.
            /// </summary>
            /// <param name="sb">
            /// The <c>StringBuilder</c> to which to append.
            /// </param>
            /// <param name="marker">
            /// The marker value for named parameter tokens. 
            /// </param>
            public StringBuilder AppendParameterMarkerForm(
                StringBuilder sb, string marker)
            {
                if (ParameterCount == 0)
                {
                    return AppendNormalizedForm(sb);
                }

                int count = m_list.Count;
                int capacity = count;

                for (int i = 0; i < count; i++)
                {
                    Token token = m_list[i];

                    switch (token.Type)
                    {
                        case TokenType.NamedParameter:
                            {
                                capacity++;
                                break;
                            }
                        default:
                            {
                                string value = token.Value;

                                if (Token.ValueFor.COMMA == value)
                                {
                                    capacity++;
                                }
                                else
                                {
                                    capacity += token.Value.Length;
                                }
                                break;
                            }
                    }
                }

                if (sb == null)
                {
                    sb = new StringBuilder(capacity);
                }
                else
                {
                    sb.EnsureCapacity(sb.Length + capacity);
                }

                for (int i = 0; i < count; i++)
                {
                    Token token = m_list[i];

                    string value = token.Value;

                    if (i > 0 && (Token.ValueFor.COMMA != value))
                    {
                        sb.Append(' ');
                    }

                    if (token.Type == TokenType.NamedParameter)
                    {
                        sb.Append(marker);
                    }
                    else
                    {
                        sb.Append(value);
                    }
                }

                return sb;
            }

            #endregion

            #region AppendNormalizedForm(StringBuilder)

            /// <summary>
            /// Appends the normalized string representation of this
            /// list to the given <c>StringBuilder</c>.
            /// </summary>
            /// <param name="sb">
            /// The <c>StringBuilder</c> to which to append
            /// </param>
            public StringBuilder AppendNormalizedForm(StringBuilder sb)
            {
                if (sb == null)
                {
                    sb = new StringBuilder(m_normalizedCapacity);
                }
                else
                {
                    sb.EnsureCapacity(sb.Length + m_normalizedCapacity);
                }

                int count = m_list.Count;

                for (int i = 0; i < count; i++)
                {
                    string value = m_list[i].Value;

                    if (i > 0 && (Token.ValueFor.COMMA != value))
                    {
                        sb.Append(' ');
                    }

                    sb.Append(value);
                }

                return sb;
            }

            #endregion

            #region ToStaticallyBoundForm(HsqlParameterCollection,bool)

            /// <summary>
            /// Gets the statically-bound representation of this list.
            /// </summary>
            /// <remarks>
            /// The statically-bound representation of this list is the
            /// one in which each named parameter list element whose <c>Value</c>
            /// property equals the <c>ParameterName</c> property of an
            /// <c>HsqlParameter</c> object in the given collection is
            /// replaced by an SQL literal character sequence representating the
            /// <c>Value</c> property of the matching <c>HsqlParameter</c>
            /// object.
            /// </remarks>
            /// <param name="parameters">The parameters to bind.</param>
            /// <param name="strict">
            /// When <c>true</c>, an exception is raised if there exist
            /// any unbound named parameter or parameter marker tokens.
            /// </param>
            /// <returns>
            /// The statically-bound string representation of this list.
            /// </returns>
            public string ToStaticallyBoundForm(HsqlParameterCollection parameters, bool strict)
            {
                return AppendStaticallyBoundForm(null, parameters, strict).ToString();
            }

            #endregion

            #region AppendStaticallyBoundForm(StringBuilder,HsqlParameterCollection,bool)

            /// <summary>
            /// Appends the statically-bound representation of this
            /// list to the given <c>StringBuilder</c>.
            /// </summary>
            /// <param name="sb">
            /// The <c>StringBuilder</c> to which to append.
            /// </param>
            /// <param name="parameters">The parameters to bind.</param>
            /// <param name="strict">
            /// When <c>true</c>, an exception is raised if there exist
            /// any unbound named parameter or parameter marker tokens.
            /// </param>
            /// <returns>
            /// The <c>StringBuilder</c> to which has been appended
            /// the statically-bound representation of this list.
            /// </returns>
            /// <remarks>
            /// The statically-bound representation of this list is the
            /// one in which each named parameter list element whose <c>Value</c>
            /// property equals the <c>ParameterName</c> property of an
            /// <c>HsqlParameter</c> object in the given collection is
            /// replaced by an SQL literal character sequence representating the
            /// <c>Value</c> property of the matching <c>HsqlParameter</c>
            /// object.
            /// </remarks>
            public StringBuilder AppendStaticallyBoundForm(StringBuilder sb,
                             HsqlParameterCollection parameters,
                             bool strict)
            {
                if (ParameterCount == 0)
                {
                    return AppendNormalizedForm(sb);
                }
                else if (strict && ParameterMarkerCount > 0)
                {
                    throw new HsqlDataSourceException(
                        "Cannot statically bind parameter markers"
                        + " ('?' tokens) by name.");
                }

                // Not perfectly accurate, but better than nothing.
                if (sb == null)
                {
                    sb = new StringBuilder(m_normalizedCapacity);
                }
                else
                {
                    sb.EnsureCapacity(sb.Length + m_normalizedCapacity);
                }

                int count = m_list.Count;

                for (int i = 0; i < count; i++)
                {
                    Token token = m_list[i];
                    string value = token.Value;
                    TokenType type = token.Type;

                    if (i > 0 && (Token.ValueFor.COMMA != value))
                    {
                        sb.Append(' ');
                    }

                    if (type == TokenType.NamedParameter)
                    {
                        int index = parameters.IndexOf(value);

                        if (index >= 0)
                        {
                            HsqlParameter parameter
                                = (HsqlParameter)parameters[index];

                            sb.Append(parameter.ToSqlLiteral());
                        }
                        else if (strict)
                        {
                            throw new HsqlDataSourceException(
                                "No binding for named parameter: "
                                + value); // NOI18N
                        }
                        else // (index < 0 && strict == false)
                        {
                            // Effectively a named parameter pass through...
                            // may be that we want to do multi-pass binding.
                            sb.Append(value);
                        }
                    }
                    // Currently, this never happens, due to the
                    // (strict && ParameterMarkerCount > 0) check above.
                    // stubbed in as a reminder that we might have to deal
                    // with this case differently, for instance using
                    // a bind-by-parameter-ordinal policy.
                    else if (strict && (Token.ValueFor.QUESTION == value))
                    {
                        throw new HsqlDataSourceException(
                                "No binding for parameter marker: " + value); //NOI18N
                    }
                    else
                    {
                        sb.Append(value);
                    }
                }

                return sb;
            }

            #endregion

            #region CopyTo(int,HsqlToken[],int,int)

            /// <summary>
            /// Copies a range of elements to the given array.
            /// </summary>
            /// <param name="index">The index.</param>
            /// <param name="array">The array.</param>
            /// <param name="arrayIndex">Index of the array.</param>
            /// <param name="count">The count.</param>
            public void CopyTo(
                int index,
                Token[] array,
                int arrayIndex,
                int count)
            {
                m_list.CopyTo(index, array, arrayIndex, count);
            }

            #endregion

            #region GetNamedParameterTokenPositions(string)

            /// <summary>
            /// Gets an array clone containing the list ordinals
            /// at which the named parameter token occurs.
            /// </summary>
            /// <param name="parameterName">Name of the parameter.</param>
            /// <returns>
            /// an array clone containing the ordinal positions at which
            /// the named parameter occurs in this list.
            /// </returns>
            public int[] GetNamedParameterTokenPositions(
                string parameterName)
            {
                int[] positions
                    = GetNamedParameterTokenPositionsInternal(parameterName);

                return (positions.Length == 0)
                    ? positions
                    : (int[])positions.Clone();
            }

            #endregion

            #region GetNamedParameterBindPositions(string)

            /// <summary>
            /// Gets a cloned copy of the bind positions to which
            /// the named parameter corresponds.
            /// </summary>
            /// <param name="parameterName">Name of the parameter.</param>
            /// <returns>
            /// A cloned copy of the bind positions to which
            /// the named parameter corresponds.
            /// </returns>
            public int[] GetNamedParameterBindPositions(
                string parameterName)
            {
                int[] positions
                    = GetNamedParameterBindPositionsInternal(parameterName);

                return (positions.Length == 0)
                    ? positions
                    : (int[])positions.Clone();
            }

            #endregion

            #region ToDefaultParameterMarkerForm()

            /// <summary>
            /// Gets the string representation of this list in which all named
            /// parameter tokens (@name or :name) are replaced with question
            /// mark tokens (?).
            /// </summary>
            /// <returns>
            /// This list, as a character sequence in default parameter marker form
            /// </returns>
            public string ToDefaultParameterMarkerForm()
            {
                return AppendParameterMarkerForm(null, System.Data.Hsqldb.Common.Sql.Token.ValueFor.QUESTION).ToString();
            }

            #endregion

            #region ToParameterMarkerForm(string)

            /// <summary>
            /// Gets the string representation of this list in which all named
            /// parameter markers (@name or :name) are replaced with question
            /// mark parameter markers (?).
            /// </summary>
            /// <remarks>
            /// The parameter marker binding string representation is that
            /// in which all named parameter tokens are replaced by parameter
            /// marker tokens.
            /// </remarks>
            /// <returns>
            /// This list in parameter marker binding form.
            /// </returns>
            public string ToParameterMarkerForm(string marker)
            {
                return AppendParameterMarkerForm(null, marker).ToString();
            }

            #endregion

            #region ToNormalizedForm()

            /// <summary>
            /// Gets the normalized string representation of this list.
            /// </summary>
            /// <returns>The normalized string representation.</returns>
            public string ToNormalizedForm()
            {
                return AppendNormalizedForm(null).ToString();
            }

            #endregion

            #endregion

            #region Internal Instance Methods

            #region GetNamedParameterTokenPositionsInternal(string)

            /// <summary>
            /// Gets a direct reference to the token positions for the given
            /// parameter name.
            /// </summary>
            /// <param name="parameterName">The parameter name.</param>
            /// <returns>a direct reference to the token positions.</returns>
            internal int[] GetNamedParameterTokenPositionsInternal(
                string parameterName)
            {
                int[] ordinals;

                bool found = m_nameToTokenOrdinals.TryGetValue(
                    parameterName, out ordinals);

                return (found) ? ordinals : m_NoOrdinals;
            }

            #endregion

            #region GetNamedParameterBindPositionsInternal(string)

            /// <summary>
            /// Gets a direct reference to the bind positions for the given
            /// parameter name.
            /// </summary>
            /// <param name="parameterName">The parameter name.</param>
            /// <returns>A direct reference to the bind positions.</returns>
            internal int[] GetNamedParameterBindPositionsInternal(
                string parameterName)
            {
                int[] ordinals;

                bool found = m_nameToBindOrdinals.TryGetValue(
                    parameterName, out ordinals);

                return (found) ? ordinals : m_NoOrdinals;
            }

            #endregion

            #endregion

            #region IEnumerable<Token> Members

            #region GetEnumerator()
            /// <summary>
            /// Returns a generic enumerator that iterates through this list.
            /// </summary>
            /// <returns> a generic enumerator.</returns>
            public IEnumerator<Token> GetEnumerator()
            {
                return m_list.GetEnumerator();
            }
            #endregion

            #endregion

            #region IEnumerable Members

            #region IEnumerable.GetEnumerator()
            /// <summary>
            /// Returns a non-generic enumerator that iterates through this list.
            /// </summary>
            /// <returns>A non-generic enumerator.</returns>
            IEnumerator IEnumerable.GetEnumerator()
            {
                return m_list.GetEnumerator();
            }
            #endregion

            #endregion

            #region ICloneable Members

            #region Clone()
            /// <summary>
            /// Creates a new <c>HsqlTokenList</c> that is a copy of this list.
            /// </summary>
            /// <returns>A cloned copy of this list.</returns>
            public TokenList Clone()
            {
                return new TokenList(this);
            }
            #endregion

            #region ICloneable.Clone()
            /// <summary>
            /// Creates a new <c>HsqlTokenList</c> that is a copy of this list.
            /// </summary>
            /// <returns>A cloned copy of this list.</returns>
            object ICloneable.Clone()
            {
                return Clone();
            }
            #endregion

            #endregion
        }

        #endregion
    
}