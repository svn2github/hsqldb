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
using ArgumentNullException = System.ArgumentNullException;
using Exception = System.Exception;
using IDisposable = System.IDisposable;
using Stream = System.IO.Stream;
using InputStream = java.io.InputStream;
using IOException = java.io.IOException;
using System; 
#endregion

namespace System.Data.Hsqldb.Common.IO
{
    #region JavaInputStreamAdapter
    /// <summary>
    /// Provides a <c>java.io.InputStream</c> implemention
    /// that delegates to a <see cref="System.IO.Stream"/>.
    /// </summary>
    /// <author name="boucherb@users"/>
    [CLSCompliant(false)]
    public class JavaInputStreamAdapter : InputStream, IDisposable
    {
        #region Fields
        private Stream m_stream;
        #endregion

        #region JavaInputStreamAdapter(Stream)
        /// <summary>
        /// Constructs a new <c>JavaInputStreamAdapter</c> instance
        /// with the given stream.
        /// </summary>
        /// <param name="stream">The stream delegate.</param>
        public JavaInputStreamAdapter(Stream stream)
        {
            if (stream == null)
            {
                throw new ArgumentNullException("stream");
            }

            m_stream = stream;
        }
        #endregion

        #region Method Overrides
        
        #region read()
        /// <summary>
        /// Reads the next byte of data from this input stream. 
        /// </summary>
        /// <remarks>
        /// The byte value is returned as an <c>int</c> in
        /// the range <c>0</c> to <c>255</c>. If no byte
        /// is available because the end of the stream has been reached,
        /// the value <c>-1</c> is returned. This method blocks
        /// until input data is available, the end of the stream is
        /// detected, or an exception is thrown.
        /// </remarks>
        /// <returns>    
        /// The next byte of data, or <c>-1</c> if the end of the
        /// stream is reached.
        /// </returns>
        /// <exception cref="java.io.IOException">
        /// if an I/O error occurs.
        /// </exception>
        public override int read()
        {
            try
            {
                return m_stream.ReadByte();
            }
            catch (Exception ex)
            {
                IOException ioe = new IOException(ex.Message);

                ioe.initCause(ex);

                throw ioe;
            }
        }
        #endregion

        #region read(byte[],int,int)

        /// <summary>
        /// Reads up to <c>length</c> bytes of data from the input/ stream
        /// into an array of bytes.
        /// </summary>
        /// <remarks>
        /// <para>
        /// An attempt is made to read as many as <c>length</c> bytes, but
        /// a smaller number may be read. The number of bytes actually
        /// read is returned as an integer.
        /// </para>
        /// <para>
        /// This method blocks until input data is available, end of file
        /// is detected, or an exception is thrown.
        /// </para>
        /// <para>
        /// If <c>length</c> is zero, then no bytes are read and <c>0</c> is
        /// returned; otherwise, there is an attempt to read at least one byte.
        /// If no byte is available because the stream is at end of file, the
        /// value <c>-1</c> is returned; otherwise, at least one byte is read
        /// and stored into <c>buffer</c>.
        /// </para>
        /// <para>
        /// The first byte read is stored into element <c>buffer[offset]</c>,
        /// the next one into <c>buffer[offset+1]</c>, and so on. The number
        /// of bytes read is, at most, equal to <c>length</c>. Let <i>k</i>
        /// be the number of bytes actually read; these bytes will be stored
        /// in elements <c>buffer[offset]</c> through <c>buffer[offset+</c>
        /// <i>k</i><c>-1]</c>, leaving elements <c>buffer[offset+</c><i>k</i>
        /// <c>]</c> through <c>buffer[offset+length-1]</c> unaffected.
        /// </para>
        /// <para>
        /// In every case, elements <c>buffer[0]</c> through
        /// <c>buffer[offset]</c> and elements <c>buffer[offset+length]</c>
        /// through <c>buffer[b.length-1]</c> are unaffected.
        /// </para>
        /// </remarks>
        /// <param name="buffer">
        /// The buffer into which the data is read.
        /// </param>
        /// <param name="offset">
        /// The start offset in array <c>buffer</c> at which the data
        /// is written.
        /// </param>
        /// <param name="length">
        /// The maximum number of bytes to read.
        /// </param>
        /// <returns>
        /// The total number of bytes read into the buffer, or <c>-1</c> if
        /// there is no more data because the end of the stream has been
        /// reached.
        /// </returns>
        /// <exception cref="java.io.IOException">
        /// If the first byte cannot be read for any reason other than end of
        /// file, or if the input stream has been closed, or if some other
        /// I/O error occurs.
        /// </exception>
        /// <exception cref="java.lang.NullPointerException">
        /// If <c>buffer</c> is <c>null</c>.
        /// </exception>
        /// <exception cref="java.lang.IndexOutOfBoundsException">
        /// If <c>offset</c> is negative,  <c>length</c> is negative,
        /// or <c>length</c> is greater than  <c>b.length - offset</c>.
        /// </exception>
        public override int read(
            byte[] buffer,
            int offset,
            int length)
        {
            try
            {
                int bytesRead = m_stream.Read(buffer, offset, length);

                return (bytesRead == 0)
                    ? -1
                    : bytesRead;
            }
            catch (Exception ex)
            {
                IOException ioe = new IOException(ex.Message);
                
                ioe.initCause(ex);
                
                throw ioe;
            }
        }
        #endregion

        #region close()
        /// <summary>
        /// Closes this input stream and releases any system
        /// resources associated with it.
        /// </summary>
        /// <exception cref="Exception">
        /// If an I/O error occurs.
        /// </exception>
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
