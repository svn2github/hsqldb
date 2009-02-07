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
using System.IO;
using System.Text;
using System.Data.Hsqldb.Common.IO;

#endregion

namespace System.Data.Hsqldb.Common.Lob
{
    /// <summary>
    /// <para>
    /// An <c>IClob</c> implementation that wraps a <c>java.sql.Clob</c>.
    /// </para>
    /// <img src="/ClassDiagrams/System.Data.Hsqldb.Common.Lob.JdbcClob.png"
    ///      alt="JdbcClob Class Diagram"/>
    /// </summary>
    /// <author name="boucherb@users"/>
    public sealed class JdbcClob : IClob
    {
        #region Fields
        /// <summary>
        /// The <c>java.sql.Clob</c> that this object wraps.
        /// </summary>
        private java.sql.Clob m_clob;
        #endregion

        #region Contructors

        #region JdbcClob()
        /// <summary>
        /// Contructs a new <c>JdbcClob</c> instance that is initially
        /// in the <see cref="IClob.Free()"/>d state.
        /// </summary>
        /// <remarks>
        /// It is subsequently required to invoke <see cref="IClob.Wrap(Object)"/>
        /// in order to arrive at a valid (read-only or read/write) state. 
        /// </remarks>        
        public JdbcClob(){ }
        #endregion

        #region JdbcClob(java.sql.Clob)
        /// <summary>
        /// Constructs a new <c>JdbcClob</c> wrapping the given
        /// <c>java.sql.Clob</c>.
        /// </summary>
        /// <param name="clob">
        /// The <c>java.sql.Clob</c> to wrap.
        /// </param>
        /// <exception cref="ArgumentNullException">
        /// When the given <c>clob</c> is null.
        /// </exception>
        internal JdbcClob(java.sql.Clob clob)
        {
            if (clob == null)
            {
                throw new ArgumentNullException("clob");
            }

            m_clob = clob;
        }
        #endregion

        #region JdbcClob(string)
        /// <summary>
        /// Constructs a new <c>JdbcClob</c> instance that wraps an internally
        /// constructed <c>java.sql.Clob</c> object that, in turn, represents
        /// the given <c>CLOB</c> <c>data</c>.
        /// </summary>
        /// <param name="data">
        /// A character sequence representing the <c>CLOB</c> data.        
        /// </param>
        /// <exception cref="ArgumentNullException">
        /// When <c>data</c> is null.
        /// </exception>
        /// <exception cref="HsqlDataSourceException">
        /// When internal construction of the wrapped <c>java.sql.Clob</c>
        /// instance fails for any reason.
        /// </exception>
        public JdbcClob(string data)
        {
            if (data == null)
            {
                throw new ArgumentNullException("data");
            }

            try
            {
                m_clob = new org.hsqldb.jdbc.jdbcClob(data);
            }
            catch(java.sql.SQLException ex)
            {
                throw new HsqlDataSourceException(ex);
            }
        }
        #endregion

        #region JdbcClob(TextReader,int)
        /// <summary>
        /// Constructs a new <c>JdbcClob</c> intialized with
        /// up to <c>length</c> characters obtained from the
        /// given <c>reader</c>.
        /// </summary>
        /// <param name="reader">
        /// The <c>TextReader</c> from which to obtain the characters.
        /// </param>
        /// <param name="length">
        /// The maximum number of characters to obtain from the given reader.
        /// </param>
        /// <exception cref="ArgumentNullException">
        /// When <c>reader</c> is null.
        /// </exception>
        /// <exception cref="ArgumentOutOfRangeException">
        /// When <c>length</c> is less than zero (0).
        /// </exception>
        /// <exception cref="IOException">
        /// When access to reader throws an <c>IOException</c>.
        /// </exception>
        /// <exception cref="HsqlDataSourceException">
        /// When internal construction of the wrapped <c>java.sql.Clob</c>
        /// instance fails for any reason.
        /// </exception>
        public JdbcClob(TextReader reader, int length)
        {
            Initialize(reader, length);
        }
        #endregion

        #endregion Constructors

        #region Internal Methods

        #region Initialize(TextReader,int)
        /// <summary>
        /// Initializes this object with up to <c>length</c> characters
        /// obtained from the specified <c>reader</c>.
        /// </summary>
        /// <param name="reader">
        /// The source of characters.
        /// </param>
        /// <param name="length">
        /// The maximum number of characters to obtain.
        /// </param>
        /// <exception cref="ArgumentNullException">
        /// When <c>reader</c> is null.
        /// </exception>
        /// <exception cref="ArgumentOutOfRangeException">
        /// When <c>length</c> is less than zero (0).
        /// </exception>
        /// <exception cref="IOException">
        /// When access to reader throws an <c>IOException</c>.
        /// </exception>
        /// <exception cref="HsqlDataSourceException">
        /// When construction of the wrapped <c>java.sql.Clob</c>
        /// instance fails for any reason.
        /// </exception>        
        internal void Initialize(TextReader reader, int length)
        {
            if (reader == null)
            {
                throw new ArgumentNullException(
                    "reader");
            }
            else if (length < 0)
            {
                throw new ArgumentOutOfRangeException(
                    "length",
                    "value: " + length);
            }

            int capacity = Math.Min(1024, length);
            StringBuilder sb = new StringBuilder(capacity, length);

            int charsRead = 0;
            char[] buffer = new char[capacity];

            while (charsRead < length)
            {
                int count = reader.Read(
                    buffer, 
                    0, 
                    Math.Min(capacity, (length - charsRead)));

                if (count == 0)
                {
                    break;
                }

                sb.Append(buffer, 0, count);

                charsRead += count;
            }

            try
            {
                m_clob = new org.hsqldb.jdbc.jdbcClob(sb.ToString());
            }
            catch (java.sql.SQLException ex)
            {
                throw new HsqlDataSourceException(ex);
            }
        }
        #endregion

        #region CheckFree()
        /// <summary>
        /// Checks if this object holds a reference to a <b>CLOB</b> value,
        /// throwing an exception if it does not.
        /// </summary>
        /// <exception cref="InvalidOperationException">
        /// When this object does not hold a reference to a <c>CLOB</c> value.
        /// </exception>
        internal void CheckFree()
        {
            // PRE:  only call this from inside a lock(this) block
            if (m_clob == null)
            {
                throw new InvalidOperationException(
                    "Currently In Freed State.");
            }
        }
        #endregion

        #region CheckNotFree()
        /// <summary>
        /// Checks if this object holds a reference to a <b>CLOB</b> value,
        /// throwing an exception if it does.
        /// </summary>
        /// <exception cref="InvalidOperationException">
        /// When this object holds a reference to a <c>CLOB</c> value.
        /// </exception>
        internal void CheckNotFree()
        {
            // PRE:  only call this from inside a lock(this) block
            if (m_clob != null)
            {
                throw new InvalidOperationException(
                    "Not currently In Freed State.");
            }
        }
        #endregion

        #endregion Internal Methods

        #region Explicit Interface Implementation

        #region IClob.CanSearch
        bool IClob.CanSearch
        {
            get { return true; }
        } 
        #endregion

        #region IClob.CanWrap
        bool IClob.CanWrap
        {
            get { return true; }
        } 
        #endregion

        #region IClob.CanWrapType(Type)
        bool IClob.CanWrapType(Type type)
        {
            return typeof(java.sql.Clob).IsAssignableFrom(type) ||
                typeof(string).IsAssignableFrom(type) ||
                typeof(Stream).IsAssignableFrom(type) ||
                typeof(TextReader).IsAssignableFrom(type);
        } 
        #endregion

        bool IClob.CanWrite
        {
            get
            {
                return (m_clob != null) && 
                    !typeof(org.hsqldb.jdbc.jdbcClob).IsAssignableFrom(m_clob.GetType());
            }
        }

        #region IClob.GetAsciiStream()
        /// <summary>
        /// Retrieves the <c>CLOB</c> value designated by this <c>IClob</c> object as
        /// an ASCII stream.
        /// </summary>
        /// <returns>
        /// A <see cref="System.IO.Stream"/> containing the <c>CLOB</c> data.
        /// </returns>
        /// <remarks>
        /// Note that multi-byte characters contained in the <c>CLOB</c> data
        /// are UTF-8 encoded in the stream.
        /// </remarks>
        /// <exception cref="System.Data.Common.DbException">
        /// If there is an error accessing the <c>CLOB</c> value.
        /// </exception>
        Stream IClob.GetAsciiStream()
        {
            lock (this)
            {
                CheckFree();

                return new JavaInputStreamWrapper(m_clob.getAsciiStream());
            }
        }
        #endregion

        #region IClob.GetCharacterStream()
        /// <summary>
        /// Retrieves the <c>CLOB</c> value designated by this <c>IClob</c>
        /// object as a <see cref="TextReader"/> object.
        /// </summary>
        /// <returns>
        /// A <see cref="TextReader"/> containing the <c>CLOB</c> data.
        /// </returns>
        /// <exception cref="HsqlDataSourceException">
        /// If there is an error accessing the <c>CLOB</c> value.
        /// </exception>
        TextReader IClob.GetCharacterStream()
        {
            lock (this)
            {
                CheckFree();

                try
                {
                    return new JavaReaderWrapper(m_clob.getCharacterStream());
                }
                catch (java.sql.SQLException se)
                {
                    throw new HsqlDataSourceException(se);
                }
            }
        }
        #endregion

        #region IClob.GetSubString(long,int)
        /// <summary>
        /// Retrieves a copy of the specified substring in the <c>CLOB</c>
        /// value designated by this <c>IClob</c> object. The substring begins
        /// at position <c>pos</c> and has up to <c>length</c> consecutive
        /// characters.
        /// </summary>
        /// <param name="pos">
        /// The first character of the substring to be extracted.
        /// The first character is at position 1.
        /// </param>
        /// <param name="length">
        /// The number of consecutive characters to be copied
        /// </param>
        /// <returns>
        /// A character sequence that is the specified substring in the <c>CLOB</c>
        /// value designated by this <c>IClob</c> object.
        /// </returns>
        /// <exception cref="HsqlDataSourceException">
        /// If there is an error accessing the <c>CLOB</c> value.
        /// </exception>
        /// <exception cref="ArgumentoutOfRangeException">
        /// When <c>pos</c> is less than 1; 
        /// <c>pos</c> is greater than <c>this.Length</c>;
        /// <c>length</c> is less than zero; 
        /// <c>length</c> is greater than <c>this.Length</c> - <c>pos - 1</c>
        /// </exception>
        string IClob.GetSubString(long pos, int length)
        {
            lock (this)
            {
                CheckFree();

                try
                {
                    return m_clob.getSubString(pos, length);
                }
                catch (java.sql.SQLException ex)
                {
                    throw new HsqlDataSourceException(ex);
                }
            }
        }
        #endregion

        #region IClob.Length
        /// <summary>
        /// The number of characters in the <c>CLOB</c> value
        /// designated by this <c>IClob</c> object.
        /// </summary>
        /// <value>
        /// The number of characters in the <c>CLOB</c> value.
        /// </value>
        /// <exception cref="HsqlDataSourceException">
        /// If there is an error accessing the <c>CLOB</c> value.
        /// </exception>
        long IClob.Length
        {
            get
            {
                lock (this)
                {
                    CheckFree();

                    try
                    {
                        return m_clob.length();
                    }
                    catch (java.sql.SQLException ex)
                    {
                        throw new HsqlDataSourceException(ex);
                    }
                }
            }
        }
        #endregion

        #region IClob.Position(IClob,long)
        /// <summary>
        /// Retrieves the character position at which the specified
        /// <c>IClob</c> object <c>searchString</c> appears in this
        /// <c>IClob</c> object. The search begins at position
        /// <c>start</c>.
        /// </summary>
        /// <param name="searchString">
        /// The <c>IClob</c> object for which to search.
        /// </param>
        /// <param name="start">
        /// The position at which to begin searching;
        /// the first position is 1.
        /// </param>
        /// <returns>
        /// The position at which the <c>searchString</c> appears or -1
        /// if it is not present; the first position is 1.
        /// </returns>
        /// <exception cref="HsqlDataSourceException">
        /// If there is an error accessing the <c>CLOB</c> value.
        /// </exception>
        long IClob.Position(IClob searchString, long start)
        {
            lock (this)
            {
                CheckFree();

                try
                {
                    java.sql.Clob wrapped 
                        = searchString.UnWrap() as java.sql.Clob;

                    if (wrapped == null)
                    {
                        long length = searchString.Length;

                        if (length > int.MaxValue)
                        {
                            throw new ArgumentException(
                                "Maximum input length exceeded",
                                "searchString");
                        }

                        // TODO: this is *very* inefficient for large values.
                        string s = searchString.GetSubString(0, (int)length);

                        return m_clob.position(s, start);
                    }
                    else
                    {
                        return m_clob.position(wrapped, start);
                    }
                }
                catch (java.sql.SQLException ex)
                {
                    throw new HsqlDataSourceException(ex);
                }
            }
        }
        #endregion

        #region IClob.Position(string,long)
        /// <summary>
        /// Retrieves the character position at which the specified
        /// <c>searchString</c> appears in the SQL <c>CLOB</c> value represented
        /// by this <c>IClob</c> object; the search begins at position
        /// <c>start</c>.
        /// </summary>
        /// <param name="searchString">
        /// The character sequence for which to search.
        /// </param>
        /// <param name="start">
        /// The position in this <c>IClob</c> from which to start the search;
        /// the first position is 1.
        /// </param>
        /// <returns>
        /// The position at which <c>searchString</c> appears or -1
        /// if it is not present; the first position is 1.
        /// </returns>
        /// <exception cref="HsqlDataSourceException">
        /// If there is an error accessing the <c>CLOB</c> value.
        /// </exception>
        long IClob.Position(string searchString, long start)
        {
            lock (this)
            {
                CheckFree();

                try
                {
                    return m_clob.position(searchString, start);
                }
                catch (java.sql.SQLException ex)
                {
                    throw new HsqlDataSourceException(ex);
                }
            }
        }
        #endregion

        #region IClob.SetAsciiStream(long)
        /// <summary>
        /// Retrieves a stream to be used to write ASCII encoded characters to
        /// the <c>CLOB</c> value that this <c>IClob</c> object represents,
        /// starting at position <c>pos</c>.
        /// </summary>
        /// <param name="pos">
        /// The position at which to start writing to this <c>CLOB</c> object
        /// </param>
        /// <returns>
        /// A <see cref="System.IO.Stream"/> to which ASCII encoded characters
        /// can be written.
        /// </returns>
        /// <exception cref="System.Data.Common.DbException">
        /// If there is an error accessing the <c>CLOB</c> value.
        /// </exception>
        /// <exception cref="HsqlDataSourceException">
        /// If there is an error accessing the <c>CLOB</c> value.
        /// </exception>
        Stream IClob.SetAsciiStream(long pos)
        {
            lock (this)
            {
                CheckFree();

                try
                {
                    return new JavaOutputStreamWrapper(
                        m_clob.setAsciiStream(pos));
                }
                catch (java.sql.SQLException se)
                {                    
                    throw new HsqlDataSourceException(se);
                }
            }
        }
        #endregion

        #region IClob.SetCharacterStream(long)
        /// <summary>
        /// Retrieves a stream to be used to write a stream of UTF-16 encoded
        /// characters to the <c>CLOB</c> value that this <c>IClob</c>
        /// object represents, at position <c>pos</c>.
        /// </summary>
        /// <param name="pos">
        /// The position at which to start writing to the <c>CLOB</c> value.
        /// </param>
        /// <returns>
        /// A <see cref="TextWriter"/> to which UTF-16 encoded
        /// characters can be written
        /// </returns>
        /// <exception cref="HsqlDataSourceException">
        /// If there is an error accessing the <c>CLOB</c> value.
        /// </exception>
        TextWriter IClob.SetCharacterStream(long pos)
        {
            lock (this)
            {
                CheckFree();

                try
                {
                    return new JavaWriterWrapper(
                        m_clob.setCharacterStream(pos));
                }
                catch (java.sql.SQLException se)
                {
                    throw new HsqlDataSourceException(se);
                }
            }
        }
        #endregion

        #region IClob.SetString(long,string,int,int)
        /// <summary>
        /// Writes <c>len</c> characters of <c>str</c>, starting at character
        /// <c>offset</c>, to the <c>CLOB</c> value that this <c>IClob</c>
        /// represents.
        /// </summary>
        /// <param name="pos">
        /// The position at which to start writing to this <c>CLOB</c> object.
        /// </param>
        /// <param name="str">
        /// The string to be written to the <c>CLOB</c> value that this
        /// <c>IClob</c> object represents.
        /// </param>
        /// <param name="offset">
        /// The offset into <c>str</c> to start reading the characters
        /// to be written.
        /// </param>
        /// <param name="length">
        /// The number of characters to be written.
        /// </param>
        /// <returns>
        /// The number of characters written.
        /// </returns>
        /// <exception cref="HsqlDataSourceException">
        /// If there is an error accessing the <c>CLOB</c> value.
        /// </exception>
        int IClob.SetString(long pos, string str, int offset, int length)
        {
            lock (this)
            {
                CheckFree();

                try
                {
                    return m_clob.setString(pos, str, offset, length);
                }
                catch (java.sql.SQLException ex)
                {
                    throw new HsqlDataSourceException(ex);
                }
            }
        }
        #endregion

        #region IClob.SetString(long,string)
        /// <summary>
        /// Writes the given string to the <c>CLOB</c> value that this
        /// <c>IClob</c> object designates at the position <c>pos</c>.
        /// </summary>
        /// <param name="pos">
        /// The position at which to start writing to the <c>CLOB</c>
        /// value that this <c>IClob</c> object represents.
        /// </param>
        /// <param name="str">
        /// The string to be written to the <c>CLOB</c> value that this
        /// <c>IClob</c> designates.
        /// </param>
        /// <returns>
        /// The actual number of characters written.
        /// </returns>
        /// <exception cref="HsqlDataSourceException">
        /// If there is an error accessing the <c>CLOB</c> value.
        /// </exception>
        int IClob.SetString(long pos, string str)
        {
            lock (this)
            {
                CheckFree();

                try
                {
                    return m_clob.setString(pos, str);
                }
                catch (java.sql.SQLException ex)
                {
                    throw new HsqlDataSourceException(ex);
                }
            }
        }
        #endregion

        #region IClob.Truncate(long)
        /// <summary>
        /// Truncates the <c>CLOB</c> value that this <c>IClob</c> designates
        /// to have a length of <c>len</c> characters.
        /// </summary>
        /// <param name="length">
        /// The length, in characters, to which the <c>CLOB</c> value should
        /// be truncated.
        /// </param>
        /// <exception cref="HsqlDataSourceException">
        /// If there is an error accessing the <c>CLOB</c> value.
        /// </exception>
        void IClob.Truncate(long length)
        {
            lock (this)
            {
                CheckFree();

                try
                {
                    m_clob.truncate(length);
                }
                catch (java.sql.SQLException ex)
                {
                    throw new HsqlDataSourceException(ex);
                }
            }
        }
        #endregion

        #region IClob.Free()
        /// <summary>
        /// Frees the <c>CLOB</c> reference that this <c>IClob</c> object
        /// represents, releasing any resources that this object holds to
        /// maintain the reference.
        /// </summary>
        /// <remarks>
        /// This object is invalid while in a freed state; any attempt to
        /// invoke a method other than <c>Free</c>, <c>Wrap(object)</c>
        /// or <c>Unwrap()</c> while in this state will result in raising
        /// an exception. While in a freed state, subsequent calls to
        /// <c>Free</c> have no further effect. After calling <c>Free</c>,
        /// an <c>IClob</c> may subsequently transition out of a freed state
        /// as the result of calling <c>Wrap(object)</c>.
        /// </remarks>
        /// <exception cref="System.Data.Common.DbException">
        /// If there is an error accessing the <c>CLOB</c> value.
        /// </exception>
        void IClob.Free()
        {
            lock (this)
            {
                CheckFree();

                m_clob = null;
            }
        }
        #endregion

        #region IClob.Wrap(object o)
        /// <summary>
        /// Wraps the given object, exposing it as an <c>IClob</c>
        /// through this object.
        /// </summary>
        /// <param name="obj">
        /// The object to wrap; instances of <c>java.sql.Clob</c>, string,
        /// <see cref="Stream"/> and <see cref="TextReader"/> are currently
        /// supported.
        /// </param>        
        /// <exception cref="ArgumentException">
        /// When <c>obj</c> is not a <c>java.sql.Clob</c>,
        /// <c>string</c>, <see cref="Stream"/> or
        /// <see cref="TextReader"/>.
        /// </exception>
        /// <exception cref="ArgumentNullException">
        /// When <c>obj</c> is <c>null</c>.
        /// </exception>
        /// <exception cref="HsqlDataSourceException">
        /// When this <c>IClob</c> is not in the freed state.
        /// </exception>
        void IClob.Wrap(object obj)
        {
            if (obj == null)
            {
                throw new ArgumentNullException("obj");
            }
            else if(obj is java.sql.Clob)
            {
                lock (this)
                {
                    CheckNotFree();

                    m_clob = (java.sql.Clob)obj;
                }
            }
            else if (obj is string)
            {
                lock (this)
                {
                    CheckNotFree();

                    try
                    {
                        m_clob = new org.hsqldb.jdbc.jdbcClob((string)obj);
                    }
                    catch (java.sql.SQLException ex)
                    {
                        throw new HsqlDataSourceException(ex);
                    }
                }
            }
            else if (obj is Stream)
            {
                lock (this)
                {
                    CheckNotFree();

                    Initialize(
                        new System.IO.StreamReader((Stream)obj),
                        int.MaxValue);
                }
            }
            else if (obj is TextReader)
            {
                lock (this)
                {
                    CheckNotFree();

                    Initialize((TextReader)obj, int.MaxValue);
                }
            }
            else
            {
                string message = "typeof(" + obj.GetType() + ")";
                throw new ArgumentException(message, "obj");
            }
        }
        #endregion

        #region IClob.UnWrap()
        /// <summary>
        /// Retrieves the <c>java.sql.Clob</c> object that this <c>IClob</c>
        /// wraps.
        /// </summary>
        /// <returns>
        /// The <c>java.sql.Clob</c> object wrapped by this <c>IClob</c>
        /// object.
        /// </returns>
        object IClob.UnWrap()
        {
            lock (this)
            {
                CheckFree();

                return m_clob;
            }
        }
        #endregion

        #endregion Explicit Interface Implementation
    }
}
