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

#endregion

namespace System.Data.Hsqldb.Common.IO
{
    #region JavaInputStreamWrapper

    /// <summary>
    /// <para>
    /// Wraps a <c>java.io.InputStream</c>,
    /// exposing it as a <c>System.IO.Stream</c>.
    /// </para>
    /// <img src="../Documentation/ClassDiagrams/System.Data.Hsqldb.Common.Wrapper.JavaInputStreamWrapper.png"
    ///      alt="JavaInputStreamWrapper Class Diagram"/>
    /// </summary>
    /// <author name="boucherb@users"/>
    public sealed class JavaInputStreamWrapper : Stream, IDisposable
    {
        #region Fields

        /// <summary>
        /// The <c>java.io.InputStream</c> that this object wraps.
        /// </summary>
        private java.io.InputStream m_in;

        #endregion

        #region Constructors

        /// <summary>
        /// Constructs a new <c>JavaInputStreamWrapper</c> instance
        /// with the given input stream.
        /// </summary>
        /// <param name="inputStream">
        /// The <c>java.io.InputStream</c> to which this
        /// <c>JavaInputStreamWrapper</c> delegates.
        /// </param>
        /// <remarks>
        /// The new <c>JavaInputStreamWrapper</c> wraps the given
        /// <c>java.io.InputStream</c>.
        /// </remarks>
        /// <exception cref="ArgumentNullException">
        /// When the given <c>inputStream</c> is <c>null</c>.
        /// </exception>
        [CLSCompliant(false)]
        public JavaInputStreamWrapper(java.io.InputStream inputStream)
        {
            if (inputStream == null)
            {
                throw new ArgumentNullException("inputStream");
            }

            m_in = inputStream;
        }

        #endregion

        #region Public Member Overrides

        #region Methods

        #region Flush()

        /// <summary>
        /// Ignored.
        /// </summary>
        public override void Flush()
        {
            // no-op
        }

        #endregion

        #region Read(byte[],int,int)

        /// <summary>
        /// Reads a sequence of bytes from the current stream and advances
        /// the position within the stream by the number of bytes read.
        /// </summary>
        /// <returns>
        /// The total number of bytes read into the buffer.
        /// This can be less than the number of bytes requested if
        /// that many bytes are not currently available, or zero (0)
        /// if <c>count</c> is zero or the end of the stream has been
        /// reached.
        /// </returns>
        /// <param name="offset">
        /// The zero-based byte offset in <c>buffer</c> at which to begin
        /// storing the data read from the current stream.
        /// </param>
        /// <param name="count">
        /// The maximum number of bytes to be read from the current stream.
        /// </param>
        /// <param name="buffer">
        /// An array of bytes. When this method returns, the buffer contains
        /// the specified byte array with the values between offset
        /// and (offset + count - 1) replaced by the bytes read from the
        /// current source.
        /// </param>
        /// <exception cref="System.ArgumentException">
        /// When the sum of <c>offset</c> and <c>count</c> is larger than
        /// the <c>buffer</c> length.
        /// </exception>
        /// <exception cref="System.ObjectDisposedException">
        /// When methods are called after this stream is closed.
        /// </exception>
        /// <exception cref="System.ArgumentNullException">
        /// When <c>buffer</c> is null.
        /// </exception>
        /// <exception cref="System.IO.IOException">
        /// When an I/O error occurs.
        /// </exception>
        /// <exception cref="System.ArgumentOutOfRangeException">
        /// When <c>offset</c> or <c>count</c> is negative.
        /// </exception>
        public override int Read(byte[] buffer, int offset, int count)
        {
            if (buffer == null)
            {
                throw new ArgumentNullException("buffer");
            }
            else if (offset < 0)
            {
                throw new ArgumentOutOfRangeException(
                    "Illegal negative value: " + offset,
                    "offset");
            }
            else if (count < 0)
            {
                throw new ArgumentOutOfRangeException(
                    "Illegal negative value: " + count,
                    "count");
            }
            else if (buffer.Length < (offset + count))
            {
                throw new ArgumentException(
                    "The sum of offset and count is larger than the buffer length");
            }
            try
            {
                int len = InputStream.read(buffer, offset, count);

                return (len < 0) ? 0 : len;
            }
            catch (java.io.IOException ioe)
            {
                throw new IOException(ioe.toString(), ioe);
            }
        }

        #endregion

        #region Seek(long, SeekOrigin)

        /// <summary>
        /// Sets the position within the current stream.
        /// </summary>
        /// <returns>
        /// Zero (0); Because this is a read-only Stream, 
        /// the current position is always at the start of
        /// the remaining content.
        /// </returns>
        /// <param name="offset">
        /// A byte offset relative to the origin parameter.
        /// </param>
        /// <param name="origin">
        /// A value of type <see cref="System.IO.SeekOrigin"/>
        /// indicating the reference point used to obtain the
        /// new position.
        /// </param>
        /// <exception cref="System.IO.IOException">
        /// When an I/O error occurs.
        /// </exception>
        /// <exception cref="System.NotSupportedException">
        /// When not called with <see cref="SeekOrigin.Current"/>.
        /// </exception>
        /// <exception cref="System.ObjectDisposedException">
        /// When called after this stream is closed.
        /// </exception>
        public override long Seek(long offset, SeekOrigin origin)
        {
            if (origin != SeekOrigin.End)
            {
                try
                {
                    InputStream.skip(offset);

                    return 0;
                }
                catch (java.io.IOException ex)
                {
                    throw new IOException(ex.ToString(), ex);
                }
            }

            throw new NotSupportedException(
                "SeekOrigin." + origin,
                new ArgumentException(
                    "Must be SeekOrigin.Begin or SeekOrigin.Current",
                    "origin"));
        }

        #endregion

        #region SetLength(long)

        /// <summary>
        /// Not Supported.
        /// </summary>
        /// <param name="value">
        /// Ignored.
        /// </param>
        /// <exception cref="System.NotSupportedException">
        /// Always.
        /// </exception>
        public override void SetLength(long value)
        {
            throw new NotSupportedException();
        }

        #endregion

        #region Write(byte[],int,int)

        /// <summary>
        /// This operation is not supported.
        /// </summary>
        /// <param name="offset">
        /// Ignored.
        /// </param>
        /// <param name="count">
        /// Ignored.
        /// </param>
        /// <param name="buffer">
        /// Ignored.
        /// </param>
        /// <exception cref="System.NotSupportedException">
        /// Always.
        /// </exception>
        public override void Write(byte[] buffer, int offset, int count)
        {
            throw new NotSupportedException();
        }

        #endregion

        #endregion

        #region Properties

        #region CanWrite

        /// <summary>
        /// Always true.
        /// </summary>
        public override bool CanWrite 
        {
            get { return false; }
        }

        #endregion

        #region CanSeek

        /// <summary>
        /// Always true.
        /// </summary>
        public override bool CanSeek 
        {
            get { return true; }
        }

        #endregion

        #region CanRead

        /// <summary>
        /// Always true.
        /// </summary>
        public override bool CanRead 
        {
            get { return true; }
        }

        #endregion

        #region Length

        /// <summary>
        /// Not Supported.
        /// </summary>
        /// <exception cref="NotSupportedException">
        /// Always.
        /// </exception>
        public override long Length
        {
            get { throw new NotSupportedException(); }
        }

        #endregion

        #region Position

        /// <summary>
        /// Always returns Zero (0); Assignment is not Supported.
        /// </summary>
        /// <exception cref="NotSupportedException">
        /// Upon assignment.
        /// </exception>
        public override long Position
        {
            get { return 0; }
            set { throw new NotSupportedException(); }
        }

        #endregion

        #endregion

        #endregion

        #region Protected Methods

        #region Dispose(bool)
        /// <summary>
        /// Releases the unmanaged resources used by this
        /// <see cref="System.IO.Stream"/> and optionally
        /// releases the managed resources.
        /// </summary>
        /// <remarks>
        /// Calling this method with <c>true</c> effectively
        /// closes and nullifies this object's underlying
        /// java.io.InputStream.  However, calling Close()
        /// is the preferred method.
        /// </remarks>
        /// <param name="disposing">
        /// True to release both managed and unmanaged resources;
        /// false to release only unmanaged resources.
        /// </param>
        protected override void Dispose(bool disposing)
        {
            if (disposing)
            {
                lock (this)
                {
                    try
                    {
                        if (m_in != null)
                        {
                            m_in.close();
                        }
                    }
                    catch (java.io.IOException ex)
                    {
                        throw new IOException(ex.ToString(), ex);
                    }
                    finally
                    {
                        m_in = null;

                        base.Dispose(disposing);
                    }
                }
            }
            else
            {
                base.Dispose(disposing);
            }
        } 
        #endregion

        #endregion

        #region Private Properties

        /// <summary>
        /// Retrieves the underlying java.io.InputStream, first
        /// checking if this stream is closed.
        /// </summary>
        /// <exception cref="ObjectDisposedException">
        /// If this stream is closed.
        /// </exception>
        private java.io.InputStream InputStream
        {
            get
            {
                lock (this)
                {
                    if (m_in == null)
                    {
                        throw new ObjectDisposedException(
                            "Input stream is closed.");
                    }

                    return m_in;
                }
            }
        }

        #endregion
    }

    #endregion
}
