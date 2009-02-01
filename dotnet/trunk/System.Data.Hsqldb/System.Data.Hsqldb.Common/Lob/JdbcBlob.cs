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
using System.Data.Hsqldb.Common.IO;

#endregion

namespace System.Data.Hsqldb.Common.Lob
{
    #region JdbcBlob

    /// <summary>
    /// <para>
    /// An <c>IBlob</c> implementation that wraps a <c>java.sql.Blob</c>.
    /// </para>
    /// <img src="/ClassDiagrams/System.Data.Hsqldb.Common.Lob.JdbcBlob.png"
    ///      alt="JdbcBlob Class Diagram"/>
    /// </summary>
    /// <author name="boucherb@users"/>
    public sealed class JdbcBlob : IBlob
    {
        #region Fields

        /// <summary>
        /// The <c>java.sql.Blob</c> that this object wraps.
        /// </summary>
        private volatile java.sql.Blob m_blob;

        #endregion

        #region Contructors

        #region JdbcBlob()

        /// <summary>
        /// Contructs a new <c>JdbcBlob</c> instance that is initially in the
        /// <see cref="IClob.Free()"/>d state.
        /// </summary>
        /// <remarks>
        /// It is subsequently required to invoke <see cref="IClob.Wrap(Object)"/>
        /// in order to arrive at a valid (read-only or read/write) state. 
        /// </remarks>
        public JdbcBlob(){ }

        #endregion

        #region JdbcBlob(java.sql.Blob)

        /// <summary>
        /// Constructs a new <c>JdbcBlob</c> instance that wraps the given
        /// <c>java.sql.Blob</c> object.
        /// </summary>
        /// <param name="blob"></param>
        /// <exception cref="ArgumentNullException">
        /// If the given <c>java.sql.Blob</c> is <c>null</c>.
        /// </exception>
        [CLSCompliant(false)]
        public JdbcBlob(java.sql.Blob blob)
        {
            if (blob == null)
            {
                throw new ArgumentNullException("blob");
            }

            m_blob = blob;
        }

        #endregion

        #region JdbcBlob(byte[])

        /// <summary>
        /// Constructs a new <c>JdbcBlob</c> instance that wraps an internally
        /// constructed <c>java.sql.Blob</c> object that, in turn, represents
        /// the given <c>data</c>.
        /// </summary>
        /// <remarks>
        /// Implementation Note: in the interest of efficiency, the specified
        /// <c>data</c> is not currently cloned to ensure isolation; special
        /// care should be taken to avoid modifying the <c>data</c> externally
        /// after being used to construct this object.
        /// </remarks>
        /// <param name="data">
        /// A byte array representing the <c>BLOB</c> data.
        /// </param>
        /// <exception cref="ArgumentNullException">
        /// If the given <c>data</c> is a <c>null</c> reference.
        /// </exception>
        /// <exception cref="HsqlDataSourceException">
        /// If internal construction of the wrapped <c>java.sql.Blob</c>
        /// instance fails for any reason.
        /// </exception>
        public JdbcBlob(byte[] data)
        {
            if (data == null)
            {
                throw new ArgumentNullException("data");
            }

            try
            {
                m_blob = new org.hsqldb.jdbc.jdbcBlob(data);
            }
            catch (java.sql.SQLException ex)
            {
                throw new HsqlDataSourceException(ex);
            }
            catch (Exception e)
            {
                throw new HsqlDataSourceException(e.Message, e);
            }
        }

        #endregion

        #region JdbcBlob(Stream,int)

        /// <summary>
        /// Constructs a new <c>JdbcBlob</c> instance initialized with up to
        /// <c>length</c> octets obtained from the given <c>stream</c>.
        /// </summary>
        /// <param name="stream">
        /// The <c>System.IO.Stream</c> from which to obtain octets.
        /// </param>
        /// <param name="length">
        /// The maximum number of octets to obtain from the <c>stream</c>.
        /// </param>
        public JdbcBlob(Stream stream, int length)
        {
            Initialize(stream, length);
        }

        #endregion

        #endregion

        #region Internal Methods

        #region Initialize(Stream,int)

        /// <summary>
        /// Initializes this object with up to <c>length</c> octets obtained
        /// from the given <c>stream</c>.
        /// </summary>
        /// <param name="stream">
        /// The <c>System.IO.Stream</c> from which to obtain octets.
        /// </param>
        /// <param name="length">
        /// The maximum number of octets to obtain from the <c>stream</c>.
        /// </param>
        internal void Initialize(Stream stream, int length)
        {
            if (stream == null)
            {
                throw new ArgumentNullException(
                    "stream");
            }
            else if (length < 0)
            {
                throw new ArgumentOutOfRangeException(
                    "length", 
                    "value: " + length);
            }

            try
            {
                using (MemoryStream ms = new MemoryStream())
                {
                    int bytesRead = 0;
                    const int bufferSize = 1024;
                    byte[] buffer = new byte[bufferSize];

                    while (bytesRead < length)
                    {
                        int count = stream.Read(
                            buffer, 
                            0, 
                            Math.Min(bufferSize, length - bytesRead));

                        if (count == 0)
                        {
                            break;
                        }

                        ms.Write(buffer, 0, count);
                        
                        bytesRead += count;
                    }

                    m_blob = new org.hsqldb.jdbc.jdbcBlob(ms.ToArray());
                }
            }
            catch (java.sql.SQLException ex)
            {
                throw new HsqlDataSourceException(ex);
            }
            catch (Exception e)
            {
                throw new HsqlDataSourceException(e.ToString(), e);
            }
        }

        #endregion

        #region CheckFree()

        /// <summary>
        /// Checks if this object holds a reference to a <b>BLOB</b> value,
        /// throwing an exception if it does not.
        /// </summary>
        /// <exception cref="InvalidOperationException">
        /// When this object does not hold a reference to a <c>BLOB</c> value.
        /// </exception>
        internal void CheckFree()
        {
            if (m_blob == null)
            {
                throw new InvalidOperationException("Currently In Freed State.");
            }
        }

        #endregion

        #region CheckNotFree()

        /// <summary>
        /// Checks if this object holds a reference to a <b>BLOB</b> value,
        /// throwing an exception if it does.
        /// </summary>
        /// <exception cref="InvalidOperationException">
        /// When this object holds a reference to a <c>BLOB</c> value.
        /// </exception>
        internal void CheckNotFree()
        {
            // PRE:  only call this from inside a lock(this) block
            if (m_blob != null)
            {
                throw new InvalidOperationException(
                    "Not currently In Freed State.");
            }
        }
        #endregion

        #endregion Internal Methods

        #region IBlob Explicit Interface Implementation

        #region IBlob.Length

        /// <summary>
        /// The number of octets in the <c>BLOB</c> value designated by
        /// this <c>IBlob</c> object.
        /// </summary>
        /// <value>The number of octets.</value>
        /// <exception cref="HsqlDataSourceException">
        /// If there is an error accessing the <c>BLOB</c> value.
        /// </exception>
        long IBlob.Length
        {
            get
            {
                lock (this)
                {
                    CheckFree();

                    try
                    {
                        return m_blob.length();
                    }
                    catch (java.sql.SQLException ex)
                    {
                        throw new HsqlDataSourceException(ex);
                    }
                }
            }
        }

        #endregion

        #region IBlob.GetBytes(long,int)

        /// <summary>
        /// Retrieves all or part of the <c>BLOB</c> value that this
        /// <c>IBlob</c> object represents, as an array of octets. The array
        /// contains up to <c>length</c> consecutive octets starting at
        /// position <c>pos</c>.
        /// </summary>
        /// <param name="pos">
        /// The ordinal position of the first octet in the <c>BLOB</c> value to
        /// be extracted; the first octet is at position 1.
        /// </param>
        /// <param name="length">
        /// The number of consecutive octets to be copied.
        /// </param>
        /// <returns>
        /// An array containing up to <c>length</c> consecutive octets from
        /// the <c>BLOB</c> value designated by this <c>IBlob</c> object,
        /// starting with the octet at position <c>pos</c>.
        /// </returns>
        /// <exception cref="HsqlDataSourceException">
        /// If there is an error accessing the <c>BLOB</c> value.
        /// </exception>
        byte[] IBlob.GetBytes(long pos, int length)
        {
            lock (this)
            {
                CheckFree();

                try
                {
                    return m_blob.getBytes(pos, length);
                }
                catch (java.sql.SQLException ex)
                {
                    throw new HsqlDataSourceException(ex);
                }
            }
        }

        #endregion

        #region IBlob.GetBinaryStream()

        /// <summary>
        /// Retrieves, as a stream, the <c>BLOB</c> value designated by this
        /// <c>IBlob</c> instance.
        /// </summary>
        /// <returns>
        /// A read-only <see cref="System.IO.Stream"/> containing the
        /// <c>BLOB</c> data.
        /// </returns>
        /// <exception cref="HsqlDataSourceException">
        /// If there is an error accessing the <c>BLOB</c> value.
        /// </exception>
        Stream IBlob.GetBinaryStream()
        {
            lock (this)
            {
                CheckFree();

                try
                {
                    return new JavaInputStreamWrapper(
                        m_blob.getBinaryStream());
                }
                catch (java.sql.SQLException se)
                {
                    throw new HsqlDataSourceException(se);
                }
            }
        }

        #endregion

        #region IBlob.Position(byte[],long)

        /// <summary>
        /// Retrieves the octet position at which the specified <c>pattern</c>
        /// begins within the <c>BLOB</c> value that this <c>IBlob</c> object
        /// represents. The search for <c>pattern</c> begins at position
        /// <c>start</c>.
        /// </summary>
        /// <param name="pattern">
        /// The octet sequence for which to search.
        /// </param>
        /// <param name="start">
        /// The position in the <c>BLOB</c> value at which to begin searching;
        /// the first position is 1.
        /// </param>
        /// <returns>
        /// The position at which <c>pattern</c> begins, else -1.
        /// </returns>
        /// <exception cref="HsqlDataSourceException">
        /// If there is an error accessing the <c>BLOB</c> value.
        /// </exception>
        long IBlob.Position(byte[] pattern, long start)
        {
            lock (this)
            {
                CheckFree();

                try
                {
                    return m_blob.position(pattern, start);
                }
                catch (java.sql.SQLException ex)
                {
                    throw new HsqlDataSourceException(ex);
                }
            }
        }

        #endregion

        #region IBlob.Position(IBlob,long)

        /// <summary>
        /// Retrieves the octet position in the <c>BLOB</c> value designated
        /// by this <c>IBlob</c> object at which <c>pattern</c> begins. The
        /// search begins at position <c>start</c>.
        /// </summary>
        /// <param name="pattern">
        /// The <c>IBlob</c> object designating the <c>BLOB</c> value for
        /// which to search.
        /// </param>
        /// <param name="start">
        /// The position in the <c>BLOB</c> value at which to begin searching;
        /// the first position is 1.
        /// </param>
        /// <returns>
        /// The position at which the pattern begins, else -1.
        /// </returns>
        /// <exception cref="HsqlDataSourceException">
        /// If there is an error accessing the <c>BLOB</c> value.
        /// </exception>
        long IBlob.Position(IBlob pattern, long start)
        {
            lock (this)
            {
                CheckFree();

                try
                {
                    java.sql.Blob wrapped = pattern.UnWrap() as java.sql.Blob;

                    if (wrapped == null)
                    {
                        long length = pattern.Length;

                        if (length > int.MaxValue)
                        {
                            throw new ArgumentException(
                                "Maximum input length exceeded: " + length,
                                "pattern");
                        }

                        byte[] bytes = pattern.GetBytes(0, (int)length);

                        return ((IBlob)this).Position(bytes, start);
                    }
                    else
                    {
                        return m_blob.position(wrapped, start);
                    }
                }
                catch (java.sql.SQLException ex)
                {
                    throw new HsqlDataSourceException(ex);
                }
            }
        }

        #endregion

        #region IBlob.SetBytes(long,byte[])

        /// <summary>
        /// Attempts to write the given <c>bytes</c> to the <c>BLOB</c> value that
        /// this <c>IBlob</c> object represents, starting at position
        /// <c>pos</c>, and returns the actual number of octets written.
        /// </summary>
        /// <param name="pos">
        /// The position in the <c>BLOB</c> value at which to start writing.
        /// </param>
        /// <param name="bytes">
        /// The octets to be written to the <c>BLOB</c> value that this
        /// <c>IBlob</c> object represents.
        /// </param>
        /// <returns>
        /// The actual number of octets written.
        /// </returns>
        /// <exception cref="HsqlDataSourceException">
        /// If there is an error accessing the <c>BLOB</c> value.
        /// </exception>
        int IBlob.SetBytes(long pos, byte[] bytes)
        {
            lock (this)
            {
                CheckFree();

                try
                {
                    return m_blob.setBytes(pos, bytes);
                }
                catch (java.sql.SQLException ex)
                {
                    throw new HsqlDataSourceException(ex);
                }
            }
        }

        #endregion

        #region IBlob.SetBytes(long,byte[],int,int)

        /// <summary>
        /// Attempts to write all or part of the given <c>bytes</c> to the
        /// <c>BLOB</c> value that this <c>IBlob</c> object represents and returns
        /// the number of octets actually written. Writing starts at position
        /// <c>pos</c> in the <c>BLOB</c> value; Up to <c>length</c> octets from
        /// <c>bytes</c> are written, starting at <c>offset</c> within
        /// <c>bytes</c>.
        /// </summary>
        /// <param name="pos">
        /// The position in the <c>BLOB</c> value at which to start writing.
        /// </param>
        /// <param name="bytes">
        /// The array contining the source octets.
        /// </param>
        /// <param name="offset">
        /// The starting offset into <c>bytes</c> from which <c>length</c>
        /// octets are obtained for writing to the <c>BLOB</c> value.
        /// </param>
        /// <param name="length">
        /// Starting at <c>offset</c>, the number of octets from <c>bytes</c>
        /// to be written to the <c>BLOB</c> value.
        /// </param>
        /// <returns>
        /// The actual number of octets written.
        /// </returns>
        /// <exception cref="HsqlDataSourceException">
        /// If there is an error accessing the <c>BLOB</c> value.
        /// </exception>
        int IBlob.SetBytes(long pos, byte[] bytes, int offset, int length)
        {
            lock (this)
            {
                CheckFree();

                try
                {
                    return m_blob.setBytes(pos, bytes, offset, length);
                }
                catch (java.sql.SQLException ex)
                {
                    throw new HsqlDataSourceException(ex);
                }
            }
        }

        #endregion

        #region IBlob.SetBinaryStream(long)

        /// <summary>
        /// Retrieves a binary output stream that can be used to write to the
        /// <c>BLOB</c> value that this <c>IBlob</c> object represents. The
        /// stream begins at octet position <c>pos</c>.
        /// </summary>
        /// <param name="pos">
        /// The octet position in the <c>BLOB</c> value at which to start writing
        /// </param>
        /// <returns>
        /// A <see cref="System.IO.Stream"/> to which octet data can be
        /// written.
        /// </returns>
        /// <exception cref="HsqlDataSourceException">
        /// If there is an error accessing the <c>BLOB</c> value.
        /// </exception>
        Stream IBlob.SetBinaryStream(long pos)
        {
            lock (this)
            {
                CheckFree();

                try
                {
                    return new JavaOutputStreamWrapper(
                        m_blob.setBinaryStream(pos));
                }
                catch (java.sql.SQLException se)
                {                    
                    throw new HsqlDataSourceException(se);
                }
            }
        }

        #endregion

        #region IBlob.Truncate(long)

        /// <summary>
        /// Truncates the <c>BLOB</c> value that this <c>IBlob</c> object represents
        /// to <c>length</c> octets.
        /// </summary>
        /// <param name="length">
        /// The length, in octets, to which the <c>BLOB</c> value that this
        /// <c>IBlob</c> object represents should be truncated.
        /// </param>
        /// <exception cref="HsqlDataSourceException">
        /// If there is an error accessing the <c>BLOB</c> value.
        /// </exception>
        void IBlob.Truncate(long length)
        {
            lock (this)
            {
                CheckFree();

                try
                {
                    m_blob.truncate(length);
                }
                catch (java.sql.SQLException ex)
                {
                    throw new HsqlDataSourceException(ex);
                }
            }
        }

        #endregion

        #region IBlob.Free()

        /// <summary>
        /// Frees the <c>BLOB</c> reference that this <c>IBlob</c> object
        /// represents, releasing any resources that this object holds to
        /// maintain the reference.
        /// </summary>
        /// <remarks>
        /// An <c>IBlob</c> object is invalid while in the freed state; any
        /// attempt to invoke a method other than <c>Free</c>,
        /// <c>Wrap(object)</c> or <c>Unwrap()</c> while in this state results
        /// in raising an exception. While in a freed state, subsequent calls to
        /// <c>Free()</c> are simply be ignored. After calling <c>Free()</c>,
        /// it may be possible to subsequently transition out of the
        /// freed state by calling <c>Wrap(Object)</c>.
        /// </remarks>
        void IBlob.Free()
        {
            lock (this)
            {
                m_blob = null;
            }
        }

        #endregion

        #region IBlob.Wrap(object)

        /// <summary>
        /// Wraps the given object, exposing it as an <c>IBlob</c>
        /// through this object.
        /// </summary>
        /// <param name="obj">The object to wrap.</param>
        /// <exception cref="ArgumentException">
        /// When <c>obj</c> is not a <c>java.sql.Blob</c>,
        /// <c>byte[]</c> or <see cref="Stream"/>.
        /// </exception>
        /// <exception cref="ArgumentNullException">
        /// When <c>obj</c> is <c>null</c>.
        /// </exception>
        /// <exception cref="HsqlDataSourceException">
        /// When this <c>IBlob</c> is not in the freed state.
        /// </exception>
        void IBlob.Wrap(object obj)
        {
            if (obj == null)
            {
                throw new ArgumentNullException("obj");
            }
            else if (obj is java.sql.Blob)
            {
                lock (this)
                {
                    CheckNotFree();

                    m_blob = (java.sql.Blob)obj;
                }
            }
            else if (obj is byte[])
            {
                lock (this)
                {
                    CheckNotFree();

                    try
                    {
                        m_blob = new org.hsqldb.jdbc.jdbcBlob((byte[])obj);
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

                    Initialize((Stream)obj, int.MaxValue);
                }
            }
            else
            {
                string message = "typeof(" + obj.GetType() + ")";
                throw new ArgumentException(message, "obj");
            }
        }

        #endregion

        #region IBlob.UnWrap()

        /// <summary>
        /// Retrieves the <c>java.sql.Blob</c> object wrapped by this
        /// <c>IBlob</c>.
        /// </summary>
        /// <returns>The wrapped <c>java.sql.Blob</c> object.</returns>
        /// <exception cref="InvalidOperationException">
        /// When this object is in a <c>Free</c>d state.
        /// </exception>
        object IBlob.UnWrap()
        {
            lock (this)
            {
                CheckFree();

                return m_blob;
            }
        }

        #endregion

        #endregion IBlob Explicit Interface Implementation
    }

    #endregion
}
