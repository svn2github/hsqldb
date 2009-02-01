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
using org.hsqldb.lib;
using System.Reflection;
#endregion

namespace System.Data.Hsqldb.Common.Sql
{
    /// <summary>
    /// <para>
    /// Defines the statically known SQL:200n lexographic elements, together
    /// with their numeric identifiers.
    /// </para>
    /// <img src="/ClassDiagrams/System.Data.Hsqldb.Common.HsqlTokens.png"
    ///      alt="HsqlTokens Class Diagram"/>
    /// </summary>
    /// <remarks>
    /// Includes those used presently as well as those reserved for future use
    /// as dictated by the ANSI/ISO SQL standard. Also includes those used 
    /// presently in HSQLDB that are not currently defined in an external
    /// standard.
    /// </remarks>
    public partial class Token
    {
        /// <summary>
        /// Exposes token classification and invertible numeric identifier mappings.
        /// </summary>
        public static class Map
        {
            #region Delegates
            
            #region V Getter<K, V>(K)
            /// <summary>
            /// Contract for indexed property supplier.
            /// </summary>
            /// <typeparam name="K"></typeparam>
            /// <typeparam name="V"></typeparam>
            /// <param name="k"></param>
            /// <returns></returns>
            internal delegate V Getter<K, V>(K k);
            #endregion

            #endregion

            #region GenericIndexer<K, V>
            /// <summary>
            /// Contract for an indexed property.
            /// </summary>
            /// <typeparam name="K">The key type</typeparam>
            /// <typeparam name="V">The value type</typeparam>
            public class GenericIndexer<K, V>
            {

                private Getter<K, V> m_getter;

                internal GenericIndexer(Getter<K, V> getter)
                {
                    m_getter = getter;
                }

                /// <summary>
                /// Gets the value with the specified key.
                /// </summary>
                /// <value>the value with the specified key</value>
                public V this[K key]
                {
                    get { return m_getter(key); }
                }
            } 
            #endregion

            #region Fields
            private static readonly IntValueHashMap m_TokenValueMap;
            private static readonly IntKeyHashMap m_TokenIdentifierMap;
            private static readonly IntValueHashMap m_CommandTokenIdentifierMap;
            private static readonly HashSet m_KeywordSet;
            //private static readonly IntValueHashMap m_ValueTokens; 

            private static readonly GenericIndexer<string, int> m_TokenIdentiferIndexer
                = new GenericIndexer<string, int>(delegate(string value)
            {
                return Map.GetTokenIdForValue(value);
            });

            private static readonly GenericIndexer<int, string> m_TokenValueIndexer
                = new GenericIndexer<int, string>(delegate(int id)
            {
                return Map.GetTokenValueForId(id);
            });
            #endregion

            #region Static Initializer
            /// <summary>
            /// Initializes the <see cref="Map"/> class.
            /// </summary>
            static Map()
            {
                m_CommandTokenIdentifierMap = Map.NewCommandTokenIdentifierMap();
                m_TokenValueMap = Map.NewTokenValueMap();
                m_TokenIdentifierMap = Map.NewTokenIdentifierMap();
                // literals not allowed as table / column names
                m_KeywordSet = Map.NewKeywordSet();
            }
            #endregion

            #region NewTokenValueMap()
            /// <summary>
            /// Retrieves a map that associates with each declared string token
            /// its corresponding declared numeric identifier.
            /// </summary>
            /// <returns>
            /// A new map from each declared string token to its numeric identifier.
            /// </returns>
            private static IntValueHashMap NewTokenValueMap()
            {
                IntValueHashMap map = new IntValueHashMap(347, 1.0F);

                FieldInfo[] fields = typeof(Token.ValueFor).GetFields(
                    BindingFlags.DeclaredOnly |
                    BindingFlags.Static |
                    BindingFlags.Public);

                foreach (FieldInfo keyField in fields)
                {
                    if (keyField.FieldType != typeof(string))
                    {
                        continue;
                    }
                    if (0 == (keyField.Attributes & (FieldAttributes.HasDefault |
                        FieldAttributes.Literal)))
                    {
                        continue;
                    }

                    FieldInfo valueField = (typeof(Token.IdFor)).GetField(
                        keyField.Name);

                    if (valueField == null)
                    {
                        continue;
                    }

                    map.put(keyField.GetValue(null), (int)valueField.GetValue(null));
                }

                return map;
            }
            #endregion

            #region NewTokenIdentifierMap()
            /// <summary>
            /// Retrieves a map that associates with each declared numeric token
            /// identifier its corresponding declared character sequence value.
            /// </summary>
            /// <returns>
            /// A new map from each declared numeric identifier to its character sequence value.
            /// </returns>
            private static IntKeyHashMap NewTokenIdentifierMap()
            {
                IntKeyHashMap map = new IntKeyHashMap(347, 1.0F);

                FieldInfo[] fields = typeof(Token.IdFor).GetFields(
                    BindingFlags.DeclaredOnly |
                    BindingFlags.Static |
                    BindingFlags.Public);

                foreach (FieldInfo keyField in fields)
                {
                    if (keyField.FieldType != typeof(int))
                    {
                        continue;
                    }
                    if (0 == (keyField.Attributes & (FieldAttributes.HasDefault |
                        FieldAttributes.Literal)))
                    {
                        continue;
                    }

                    FieldInfo valueField = (typeof(Token.ValueFor)).GetField(
                        keyField.Name);

                    if (valueField == null)
                    {
                        continue;
                    }

                    map.put((int)keyField.GetValue(null), valueField.GetValue(null));
                }

                return map;
            }
            #endregion

            #region NewCommandTokenIdentifierMap()
            /// <summary>
            /// Constructs and returns a new map that associates with each
            /// commonly encountered database command token a corresponding
            /// numeric identifier.
            /// </summary>
            /// <returns>
            /// A new map for the database command token set
            /// </returns>
            private static IntValueHashMap NewCommandTokenIdentifierMap()
            {
                IntValueHashMap map = new IntValueHashMap(67);

                map.put(Token.ValueFor.ADD, Token.IdFor.ADD);
                map.put(Token.ValueFor.ALIAS, Token.IdFor.ALIAS);
                map.put(Token.ValueFor.ALTER, Token.IdFor.ALTER);
                map.put(Token.ValueFor.AUTOCOMMIT, Token.IdFor.AUTOCOMMIT);
                map.put(Token.ValueFor.CACHED, Token.IdFor.CACHED);
                map.put(Token.ValueFor.CALL, Token.IdFor.CALL);
                map.put(Token.ValueFor.CHECK, Token.IdFor.CHECK);
                map.put(Token.ValueFor.CHECKPOINT, Token.IdFor.CHECKPOINT);
                map.put(Token.ValueFor.COLUMN, Token.IdFor.COLUMN);
                map.put(Token.ValueFor.COMMIT, Token.IdFor.COMMIT);
                map.put(Token.ValueFor.CONNECT, Token.IdFor.CONNECT);
                map.put(Token.ValueFor.CONSTRAINT, Token.IdFor.CONSTRAINT);
                map.put(Token.ValueFor.CREATE, Token.IdFor.CREATE);
                map.put(Token.ValueFor.DATABASE, Token.IdFor.DATABASE);
                map.put(Token.ValueFor.DELETE, Token.IdFor.DELETE);
                map.put(Token.ValueFor.DEFRAG, Token.IdFor.DEFRAG);
                map.put(Token.ValueFor.DISCONNECT, Token.IdFor.DISCONNECT);
                map.put(Token.ValueFor.DROP, Token.IdFor.DROP);
                map.put(Token.ValueFor.EXCEPT, Token.IdFor.EXCEPT);
                map.put(Token.ValueFor.EXPLAIN, Token.IdFor.EXPLAIN);
                map.put(Token.ValueFor.FOREIGN, Token.IdFor.FOREIGN);
                map.put(Token.ValueFor.GRANT, Token.IdFor.GRANT);
                map.put(Token.ValueFor.IGNORECASE, Token.IdFor.IGNORECASE);
                map.put(Token.ValueFor.INCREMENT, Token.IdFor.INCREMENT);
                map.put(Token.ValueFor.INDEX, Token.IdFor.INDEX);
                map.put(Token.ValueFor.INITIAL, Token.IdFor.INITIAL);
                map.put(Token.ValueFor.INSERT, Token.IdFor.INSERT);
                map.put(Token.ValueFor.INTERSECT, Token.IdFor.INTERSECT);
                map.put(Token.ValueFor.LOGSIZE, Token.IdFor.LOGSIZE);
                map.put(Token.ValueFor.MAXROWS, Token.IdFor.MAXROWS);
                map.put(Token.ValueFor.MEMORY, Token.IdFor.MEMORY);
                map.put(Token.ValueFor.MINUS, Token.IdFor.MINUS);
                map.put(Token.ValueFor.NEXT, Token.IdFor.NEXT);
                map.put(Token.ValueFor.NOT, Token.IdFor.NOT);
                map.put(Token.ValueFor.OPENBRACKET, Token.IdFor.OPENBRACKET);
                map.put(Token.ValueFor.PASSWORD, Token.IdFor.PASSWORD);
                map.put(Token.ValueFor.PLAN, Token.IdFor.PLAN);
                map.put(Token.ValueFor.PRIMARY, Token.IdFor.PRIMARY);
                map.put(Token.ValueFor.PROPERTY, Token.IdFor.PROPERTY);
                map.put(Token.ValueFor.READONLY, Token.IdFor.READONLY);
                map.put(Token.ValueFor.REFERENTIAL_INTEGRITY, Token.IdFor.REFERENTIAL_INTEGRITY);
                map.put(Token.ValueFor.RELEASE, Token.IdFor.RELEASE);
                map.put(Token.ValueFor.RENAME, Token.IdFor.RENAME);
                map.put(Token.ValueFor.RESTART, Token.IdFor.RESTART);
                map.put(Token.ValueFor.REVOKE, Token.IdFor.REVOKE);
                map.put(Token.ValueFor.ROLE, Token.IdFor.ROLE);
                map.put(Token.ValueFor.ROLLBACK, Token.IdFor.ROLLBACK);
                map.put(Token.ValueFor.SAVEPOINT, Token.IdFor.SAVEPOINT);
                map.put(Token.ValueFor.SCRIPT, Token.IdFor.SCRIPT);
                map.put(Token.ValueFor.SCRIPTFORMAT, Token.IdFor.SCRIPTFORMAT);
                map.put(Token.ValueFor.SELECT, Token.IdFor.SELECT);
                map.put(Token.ValueFor.SEMICOLON, Token.IdFor.SEMICOLON);
                map.put(Token.ValueFor.SEQUENCE, Token.IdFor.SEQUENCE);
                map.put(Token.ValueFor.SET, Token.IdFor.SET);
                map.put(Token.ValueFor.SHUTDOWN, Token.IdFor.SHUTDOWN);
                map.put(Token.ValueFor.SOURCE, Token.IdFor.SOURCE);
                map.put(Token.ValueFor.TABLE, Token.IdFor.TABLE);
                map.put(Token.ValueFor.TEMP, Token.IdFor.TEMP);
                map.put(Token.ValueFor.TEXT, Token.IdFor.TEXT);
                map.put(Token.ValueFor.TRIGGER, Token.IdFor.TRIGGER);
                map.put(Token.ValueFor.UNIQUE, Token.IdFor.UNIQUE);
                map.put(Token.ValueFor.UPDATE, Token.IdFor.UPDATE);
                map.put(Token.ValueFor.UNION, Token.IdFor.UNION);
                map.put(Token.ValueFor.USER, Token.IdFor.USER);
                map.put(Token.ValueFor.VALUES, Token.IdFor.VALUES);
                map.put(Token.ValueFor.VIEW, Token.IdFor.VIEW);
                map.put(Token.ValueFor.WRITE_DELAY, Token.IdFor.WRITE_DELAY);
                map.put(Token.ValueFor.SCHEMA, Token.IdFor.SCHEMA);

                return map;
            }
            #endregion

            #region NewKeywordSet()
            /// <summary>
            /// News the keyword set.
            /// </summary>
            /// <returns></returns>
            private static HashSet NewKeywordSet()
            {
                HashSet set = new HashSet(67);

                // Can't add MONTH, DAY, YEAR etc.
                // because MONTH(), DAY(), ... functions will no longer
                // parse correctly

                // Further, the following tokens are values:

                // "FALSE"
                // "TRUE"
                // "NULL"

                // Finally , the following token is excluded to
                // allow the LEFT() function to work

                // "LEFT"

                String[] keyword =
                {
                    Token.ValueFor.AS, 
                    Token.ValueFor.AND, 
                    Token.ValueFor.ALL, 
                    Token.ValueFor.ANY, 
                    Token.ValueFor.AVG,
                    Token.ValueFor.BY, 
                    Token.ValueFor.BETWEEN, 
                    Token.ValueFor.BOTH, 
                    Token.ValueFor.CALL,
                    Token.ValueFor.CASE, 
                    Token.ValueFor.CASEWHEN, 
                    Token.ValueFor.CAST, 
                    Token.ValueFor.CONVERT,
                    Token.ValueFor.COUNT, 
                    Token.ValueFor.COALESCE, 
                    Token.ValueFor.DISTINCT,
                    Token.ValueFor.ELSE, 
                    Token.ValueFor.END, 
                    Token.ValueFor.EVERY, 
                    Token.ValueFor.EXISTS,
                    Token.ValueFor.EXCEPT, 
                    Token.ValueFor.EXTRACT, 
                    Token.ValueFor.FOR, 
                    Token.ValueFor.FROM, 
                    Token.ValueFor.GROUP,
                    Token.ValueFor.HAVING, 
                    Token.ValueFor.IF, 
                    Token.ValueFor.INTO, 
                    Token.ValueFor.IFNULL,
                    Token.ValueFor.IS, 
                    Token.ValueFor.IN, 
                    Token.ValueFor.INTERSECT, 
                    Token.ValueFor.JOIN,
                    Token.ValueFor.INNER, 
                    Token.ValueFor.LEADING, 
                    Token.ValueFor.LIKE, 
                    Token.ValueFor.MAX,
                    Token.ValueFor.MIN, 
                    Token.ValueFor.NEXT, 
                    Token.ValueFor.NULLIF, 
                    Token.ValueFor.NOT,
                    Token.ValueFor.NVL, 
                    Token.ValueFor.MINUS, 
                    Token.ValueFor.ON, 
                    Token.ValueFor.ORDER, 
                    Token.ValueFor.OR,
                    Token.ValueFor.OUTER, 
                    Token.ValueFor.POSITION, 
                    Token.ValueFor.PRIMARY, 
                    Token.ValueFor.SELECT,
                    Token.ValueFor.SET, 
                    Token.ValueFor.SOME, 
                    Token.ValueFor.STDDEV_POP,
                    Token.ValueFor.STDDEV_SAMP, 
                    Token.ValueFor.SUBSTRING, 
                    Token.ValueFor.SUM, 
                    Token.ValueFor.THEN,
                    Token.ValueFor.TO, 
                    Token.ValueFor.TRAILING, 
                    Token.ValueFor.TRIM, 
                    Token.ValueFor.UNIQUE,
                    Token.ValueFor.UNION, 
                    Token.ValueFor.VALUES, 
                    Token.ValueFor.VAR_POP, 
                    Token.ValueFor.VAR_SAMP,
                    Token.ValueFor.WHEN, 
                    Token.ValueFor.WHERE
                };

                for (int i = 0; i < keyword.Length; i++)
                {
                    set.add(keyword[i]);
                }

                return set;
            }
            #endregion

            #region GetCommandTokenId(string)
            /// <summary>
            /// Gets the numeric code associated with the given command token.
            /// </summary>
            /// <remarks>
            /// <para>
            /// Please note that character sequence matching uses the
            /// case-insensitive invariant culture.
            /// </para>
            /// <para>
            /// A <c>command token</c> is deemed to be an SQL token whose presence
            /// or abscense, while parsing or interpreting a charater sequence
            /// denoting an HSQLDB database command, is so commonly tested along
            /// with a long chain of other values that the complexity of an O(1)
            /// hash lookup combined with a numeric switch statement has been
            /// deemed worth the potential performance gain (avoid O(n) string
            /// equality tests), not to mention less cumbersome to maintain and
            /// debug than long equivalent if-then-else chains.
            /// </para>
            /// <para>
            /// In part, since C# provides the ability to switch on string values,
            /// this is somewhat a legacy concern from Java. However, the
            /// complexity of switching on string values versus switching on
            /// integral values is, although at worst O(n) in both cases, very
            /// likely to be higher by some constant term when switching on string
            /// equality, regardless of language compiler optimizations or the
            /// presence or absence of machine instructions designed to optimize
            /// switch-like conditional constructs to O(1) complexity when possible.
            /// As such, this classification is exposed to support efficient O(1)
            /// lookup of an integral command token code in combination with an
            /// integral switch statement when parsing or interpreting a character
            /// sequence denoting an SQL command. 
            /// </para>
            /// </remarks>
            /// <param name="token">The token.</param>
            /// <returns>
            /// The numeric code for the given character sequence;
            /// -1 if the given value is not a command token.
            /// </returns>
            public static int GetCommandTokenIdForValue(string token)
            {
                return m_CommandTokenIdentifierMap.get(token, -1);
            }
            #endregion

            #region GetTokenIdForValue(string)
            /// <summary>
            /// Gets the numeric identifier associated with the given character
            /// sequence.
            /// </summary>
            /// <remarks>
            /// Character sequence matching uses the case-insensitive
            /// invariant culture.
            /// </remarks>
            /// <param name="token">
            /// The character sequence for which to retrieve the associated
            /// numeric identifier.
            /// </param>
            /// <returns>
            /// The numeric identifier associated with the given character sequence;
            /// -1 if a match is not found.
            /// </returns>
            public static int GetTokenIdForValue(string token)
            {
                return m_TokenValueMap.get(token, -1);
            }
            #endregion

            #region GetTokenValueForId(int)
            /// <summary>
            /// Gets the token value for the given token id.
            /// </summary>
            /// <param name="id">The token id.</param>
            /// <returns>
            /// The token value for the given token id;
            /// <c>null</c> if there is no such token value.
            /// </returns>
            public static string GetTokenValueForId(int id)
            {
                return m_TokenIdentifierMap.get(id) as string;
            } 
            #endregion

            #region IsKeyword(string)
            /// <summary>
            /// Determines whether the specified character sequence is an SQL keyword.
            /// </summary>
            /// <param name="token">The character sequence.</param>
            /// <returns>
            /// <c>true</c> if the specified character sequence is an SQL keyword;
            /// otherwise, <c>false</c>.
            /// </returns>
            public static bool IsKeyword(string token)
            {
                return m_KeywordSet.contains(token);
            }
            #endregion

            #region TokenValue[int]
            /// <summary>
            /// Gets the numeric identifier associated with the
            /// specified character sequence.
            /// </summary>
            /// <value></value>
            public static GenericIndexer<int, string> ValueFor
            {
                get { return m_TokenValueIndexer; }
            }
            #endregion

            #region TokenId[string]
            /// <summary>
            /// Gets the character sequence identified by the given numeric value.
            /// </summary>
            /// <value>The character sequence identified by the given number.</value>
            public static GenericIndexer<string, int> IdFor
            {
                get { return m_TokenIdentiferIndexer; }
            }
            #endregion
        }        
    }    
}