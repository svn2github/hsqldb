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
using ArgumentNullException = System.ArgumentNullException;
using Exception = System.Exception;
using IDisposable = System.IDisposable;
using Stream = System.IO.Stream;
using OutputStream = java.io.OutputStream;
using IOException = java.io.IOException;
using System;
#endregion

namespace System.Data.Hsqldb.Common.IO
{
    #region OutputStreamWrapper
    /// <summary>
    /// Provides <c>java.io.OutputStream</c> implemention
    /// that delegates to a <see cref="System.IO.Stream"/>.
    /// </summary>
    /// <author name="boucherb@users"/>
    [CLSCompliant(false)]
    public class JavaOutputStreamAdapter : OutputStream, IDisposable
    {
        #region Fields
        Stream m_stream; 
        #endregion

        #region Constructors

        #region JavaOutputStreamAdapter(Stream)
        /// <summary>
        /// Constructs a new <c>JavaOutputStreamAdapter</c>
        /// instance with the given stream.
        /// </summary>
        /// <param name="stream">The stream delegate.</param>
        public JavaOutputStreamAdapter(Stream stream)
        {
            if (stream == null)
            {
                throw new ArgumentNullException("stream");
            }

            m_stream = stream;
        }
        #endregion 
        
        #endregion

        #region Method Overrides

        #region flush()
        /// <summary>
        /// Flushes this output stream and forces any buffered output bytes 
        /// to be written out.
        /// </summary>
        /// <exception cref="java.io.IOException">
        /// If an I/O error occurs.
        /// </exception>
        public override void flush()
        {
            try
            {
                m_stream.Flush();
            }
            catch (Exception ex)
            {
                IOException ioe = new IOException(ex.Message);

                ioe.initCause(ex);

                throw ioe;
            }
        }
        #endregion

        #region write(int)
        /// <summary>
        /// Writes the specified byte to this output stream.
        /// </summary>
        /// <param name="b">The <c>byte</c>.</param>
        /// <remarks>
        /// The general contract for <c>write</c> is that one
        /// byte is written  to the output stream. The byte to be
        /// written is the eight low-order bits of the argument
        /// <c>b</c>. The 24 high-order bits of <c>b</c>
        /// are ignored.
        /// </remarks>
        /// <exception cref="Exception">
        /// If an I/O error occurs. In particular, an
        /// <code>IOException</code> may be thrown if the
        /// output stream has been closed.
        /// </exception>
        public override void write(int b)
        {
            try
            {
                m_stream.WriteByte((byte)(b & 0xff));
            }
            catch (Exception ex)
            {
                IOException ioe = new IOException(ex.Message);

                ioe.initCause(ex);

                throw ioe;
            }
        }
        #endregion

        #region write(byte[],int,int)
        /// <summary>
        /// Writes <c>length</c> bytes from the specified byte array
        /// starting at <c>offset</c> to this output stream.
        /// </summary>
        /// <param name="buffer">The buffer.</param>
        /// <param name="offset">The offset.</param>
        /// <param name="length">The length.</param>
        /// <remarks>
        /// The general contract is that some of the bytes in the array
        /// <code>buffer</code> are written to the output stream in order;
        /// element <code>buffer[offset]</code> is the first byte written
        /// and <code>buffer[offset+length-1]</code> is the last byte written
        /// by this operation.
        /// </remarks>
        /// <exception cref="java.io.IOException">
        /// If an I/O error occurs. In particular, an exception is thrown
        /// if the output stream is closed.
        /// </exception>
        public override void write(byte[] buffer, int offset, int length)
        {
            try
            {
                m_stream.Write(buffer, offset, length);
            }
            catch (Exception ex)
            {
                IOException ioe =  new IOException(ex.Message);

                ioe.initCause(ex);

                throw ioe;
            }
        }
        #endregion

        #region close()
        /// <summary>
        /// Closes this output stream and releases any system resources 
        /// associated it.
        /// </summary>
        /// <remarks>
        /// The general contract of this method is that it closes the
        /// output stream such that a closed stream cannot perform output
        /// operations and cannot be reopened.
        /// </remarks>
        public override void close()
        {
            try
            {
                m_stream.Close();
            }
            catch (Exception ex)
            {
                 IOException ioe = new IOException(ex.Message);

                 ioe.initCause(ex);

                 throw ioe;
            }
        }
        #endregion 
        
        #endregion

        #region IDisposable Members

        /// <summary>
        /// Performs application-defined tasks associated with freeing,
        /// releasing, or resetting unmanaged resources.
        /// </summary>
        public void Dispose()
        {
            m_stream.Dispose();
        }

        #endregion
    } 
    #endregion
}
