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
    #region JavaOutputStreamWrapper

    /// <summary>
    /// <para>
    /// Wraps a <c>java.io.OutputStream</c>,
    /// exposing it as a <c>System.IO.Stream</c>.
    /// </para>
    /// <img src="/ClassDiagrams/System.Data.Hsqldb.Common.IO.JavaOutputStreamWrapper.png"
    ///      alt="JavaOutputStreamWrapper Class Diagram"/>
    /// </summary>
    /// <author name="boucherb@users"/>
    public sealed class JavaOutputStreamWrapper : Stream
    {
        #region Fields

        /// <summary>
        /// The <c>java.io.OutputStream</c> that this object wraps.
        /// </summary>
        private volatile java.io.OutputStream m_out;

        #endregion

        #region Constructors

        #region JavaOutputStreamWrapper(java.io.OutputStream)
        /// <summary>
        /// Constructs a new <c>JavaOuptuStream</c> wrapping
        /// the given <c>java.io.OutputStream</c>.
        /// </summary>
        /// <param name="outputStream">
        /// The <c>java.io.OutputStream</c> to which this
        /// <c>JavaOuptuStream</c> delegates.
        /// </param>
        /// <exception cref="ArgumentNullException">
        /// When the given <c>outputStream</c> is <c>null</c>.
        /// </exception>
        [CLSCompliant(false)]
        public JavaOutputStreamWrapper(java.io.OutputStream outputStream)
        {
            if (outputStream == null)
            {
                throw new ArgumentNullException("outputStream");
            }

            m_out = outputStream;
        } 
        #endregion

        #endregion

        #region Public Member Overrides

        #region Methods

        #region Flush()

        /// <summary>
        /// Clears all buffers for this stream and causes
        /// any buffered data to be written to the underlying
        /// device
        /// </summary>
        /// <exception cref="IOException">
        /// When An I/O error occurs on the wrapped java.io.OutputStream.
        /// </exception>
        public override void Flush()
        {
            try
            {
                OutputStream.flush();
            }
            catch (java.io.IOException ex)
            {
                throw new IOException(ex.ToString(), ex);
            }
        }

        #endregion

        #region Read(byte[],int,int)

        /// <summary>
        /// Not Supported.
        /// </summary>
        /// <param name="buffer">
        /// Ignored.
        /// </param>
        /// <param name="offset">
        /// Ignored.
        /// </param>
        /// <param name="count">
        /// Ignored.
        /// </param>
        /// <returns>
        /// Never.
        /// </returns>
        /// <exception cref="System.NotSupportedException">
        /// Always.
        /// </exception>
        public override int Read(byte[] buffer, int offset, int count)
        {
            throw new NotSupportedException();
        }

        #endregion

        #region Seek(long,SeekOrigin)

        /// <summary>
        /// Not Supported.
        /// </summary>
        /// <param name="offset">
        /// Ignored.
        /// </param>
        /// <param name="origin">
        /// Ignored.
        /// </param>
        /// <returns>
        /// Never.
        /// </returns>
        /// <exception cref="System.NotSupportedException">
        /// Always.
        /// </exception>
        public override long Seek(long offset, SeekOrigin origin)
        {
            throw new NotSupportedException();
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
        /// Writes a sequence of bytes to the current stream and
        /// advances the current position within this stream by the
        /// number of bytes written.
        /// </summary>
        /// <param name="buffer">
        /// An array of bytes. This method copies count bytes from
        /// buffer to the current stream.
        /// </param>
        /// <param name="offset">
        /// The zero-based byte offset in buffer at which to begin
        /// copying bytes to the wrapped java.io.OutputStream.
        /// </param>
        /// <param name="count">
        /// The number of bytes to be written to the current stream.
        /// </param>
        /// <exception cref="System.IO.IOException">
        /// When an I/O error occurs.
        /// </exception>
        /// <exception cref="System.ObjectDisposedException">
        /// When called after the stream is closed.
        /// </exception>
        /// <exception cref="System.ArgumentNullException">
        /// When buffer is null. </exception>
        /// <exception cref="System.ArgumentException">
        /// When the sum of offset and count is greater
        /// than the buffer length.
        /// </exception>
        /// <exception cref="System.ArgumentOutOfRangeException">
        /// When offset or count is negative.
        /// </exception>
        public override void Write(byte[] buffer, int offset, int count)
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
                OutputStream.write(buffer, offset, count);
            }
            catch (java.io.IOException ex)
            {
                throw new IOException(ex.ToString(), ex);
            }
        }

        #endregion

        #endregion

        #region Properties

        #region CanRead

        /// <summary>
        /// Always <c>false</c>.
        /// </summary>
        public override bool CanRead
        {
            get { return false; }
        }

        #endregion

        #region CanSeek

        /// <summary>
        /// Always <c>false</c>.
        /// </summary>
        public override bool CanSeek
        {
            get { return false; }
        }

        #endregion

        #region CanWrite

        /// <summary>
        /// Always <c>true</c>.
        /// </summary>
        public override bool CanWrite
        {
            get { return true; }
        }

        #endregion

        #region Length

        /// <summary>
        /// Not Supported.
        /// </summary>
        /// <exception cref="System.NotSupportedException">
        /// Always.
        /// </exception>
        public override long Length
        {
            get { throw new NotSupportedException(); }
        }

        #endregion

        #region Position

        /// <summary>
        /// Not Supported.
        /// </summary>
        /// <exception cref="System.NotSupportedException">
        /// Always.
        /// </exception>
        public override long Position
        {
            get { throw new NotSupportedException(); }
            set { throw new NotSupportedException(); }
        }

        #endregion
        
        #endregion

        #endregion

        #region Protected Methods

        #region Dispose(true)

        /// <summary>
        /// Releases the unmanaged resources used by this
        /// <see cref="System.IO.Stream"/> and optionally
        /// releases the managed resources.
        /// </summary>
        /// <remarks>
        /// Calling this method with <c>true</c> effectively
        /// closes and nullifies this object's underlying
        /// java.io.OutputStream.  However, calling Close()
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
                        if (m_out != null)
                        {
                            m_out.close();
                        }
                    }
                    catch (java.io.IOException ex)
                    {
                        throw new IOException(ex.ToString(), ex);
                    }
                    finally
                    {
                        m_out = null;

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

        #region OutputStream

        private java.io.OutputStream OutputStream
        {
            get
            {
                lock (this)
                {
                    if (m_out == null)
                    {
                        throw new ObjectDisposedException("Output stream is closed.");
                    }

                    return m_out;
                }
            }
        }

        #endregion

        #endregion
    }

    #endregion
}
