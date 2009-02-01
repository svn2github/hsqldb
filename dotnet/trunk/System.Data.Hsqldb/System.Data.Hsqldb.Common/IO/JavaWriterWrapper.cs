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
#endregion

namespace System.Data.Hsqldb.Common.IO
{
    /// <summary>
    /// <para>
    /// Wraps a <c>java.io.Writer</c>,
    /// exposing it as a <c>System.IO.TextWriter</c>.
    /// </para>
    /// <img src="/ClassDiagrams/System.Data.Hsqldb.Common.Wrapper.JavaWriterWrapper.png" 
    ///      alt="JavaWriterWrapper Class Diagram"/>
    /// </summary>
    /// <author name="boucherb@users"/>
    public sealed class JavaWriterWrapper : TextWriter
    {
        #region Private Members

        /// <summary>
        /// The wrapped java.io.Writer
        /// </summary>
        private volatile java.io.Writer m_writer;

        /// <summary>
        /// Gets the wrapped java.io.Writer.
        /// </summary>
        /// <value>The writer.</value>
        /// <exception cref="T:System.ObjectDisposedException">
        /// When this <see cref="T:System.IO.TextWriter"></see> is closed.
        /// </exception>
        private java.io.Writer Writer
        {
            get
            {
                java.io.Writer writer = m_writer;

                if (writer == null)
                {
                    throw new ObjectDisposedException("Writer is closed.");
                }

                return writer;
            }
        }

        #endregion

        #region Constructors

        /// <summary>
        /// Initializes a new instance of the <see cref="JavaWriterWrapper"/> class.
        /// </summary>
        /// <remarks>
        /// <summary>
        /// The new <c>JavaWriterWrapper</c> instance wraps the given <c>java.io.Writer</c>.
        /// </summary>
        /// </remarks>
        /// <param name="writer">The <c>java.io.Writer</c> to wrap.</param>
        [CLSCompliant(false)]
        public JavaWriterWrapper(java.io.Writer writer)
        {
            if (writer == null)
            {
                throw new ArgumentNullException("writer");
            }

            m_writer = writer;
        }

        #endregion

        #region Overrides

        #region Methods

        #region Dispose(bool)

        /// <summary>
        /// Releases the unmanaged resources used by this
        /// <see cref="T:System.IO.TextWriter"/>
        /// and optionally releases the managed resources.
        /// </summary>
        /// <param name="disposing">
        /// <c>true</c> to release both managed and unmanaged resources;
        /// <c>false</c> to release only unmanaged resources.
        /// </param>
        protected override void Dispose(bool disposing)
        {
            if (disposing)
            {
                lock (this)
                {
                    try
                    {
                        if (m_writer != null)
                        {
                            m_writer.close();
                        }
                    }
                    catch (java.io.IOException ex)
                    {
                        throw new IOException(ex.toString(), ex);
                    }
                    finally
                    {
                        m_writer = null;

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

        #region Flush()

        /// <summary>
        /// Clears all buffers for the current writer and causes any
        /// buffered data to be written to the underlying device.
        /// </summary>
        /// <exception cref="T:System.IO.IOException">
        /// When an I/O error occurs.
        /// </exception>
        /// <exception cref="T:System.ObjectDisposedException">
        /// When this <see cref="T:System.IO.TextWriter"></see> is closed.
        /// </exception>
        public override void Flush()
        {
            try
            {
                Writer.flush();
            }
            catch (java.io.IOException ex)
            {
                throw new IOException(ex.ToString(), ex);
            }
        }

        #endregion

        #region Write(char[],int,int)

        /// <summary>
        /// Writes a subarray of characters to the text stream.
        /// </summary>
        /// <param name="buffer">The character array to write data from.</param>
        /// <param name="index">Starting index in the buffer.</param>
        /// <param name="count">The number of characters to write.</param>
        /// <exception cref="T:System.IO.IOException">
        /// When an I/O error occurs.
        /// </exception>
        /// <exception cref="T:System.ArgumentOutOfRangeException">
        /// When <c>index</c> or <c>count</c> is negative.
        /// </exception>
        /// <exception cref="T:System.ArgumentException">
        /// When the <c>buffer</c> length minus <c>index</c> is less than <c>count</c>.
        /// </exception>
        /// <exception cref="T:System.ArgumentNullException">
        /// When the <c>buffer</c> parameter is null.
        /// </exception>
        /// <exception cref="T:System.ObjectDisposedException">
        /// When this <see cref="T:System.IO.TextWriter"></see> is closed.
        /// </exception>
        public override void Write(char[] buffer, int index, int count)
        {
            if (buffer == null)
            {
                throw new ArgumentNullException("buffer");
            }
            else if (index < 0)
            {
                throw new ArgumentException(
                    "Illegal negative value: " + index,
                    "index");
            }
            else if (count < 0)
            {
                throw new ArgumentException(
                    "Illegal negative value: " + count,
                    "count");
            }
            else if (buffer.Length - index < count)
            {
                throw new ArgumentOutOfRangeException(
                    "buffer length minus index is less than count");
            }
            try
            {
                Writer.write(buffer, index, count);
            }
            catch (Exception ex)
            {
                throw new IOException(ex.ToString(), ex);
            }
        }

        #endregion

        #region Write(char[])

        /// <summary>
        /// Writes the given character array to this text stream.
        /// </summary>
        /// <param name="buffer">
        /// The character array to write to this text stream.
        /// </param>
        /// <exception cref="T:System.ArgumentNullException">
        /// When the <c>buffer</c> parameter is null.
        /// </exception>
        /// <exception cref="T:System.IO.IOException">
        /// When an I/O error occurs.
        /// </exception>
        /// <exception cref="T:System.ObjectDisposedException">
        /// When this <see cref="T:System.IO.TextWriter"/> is closed.
        /// </exception>
        public override void Write(char[] buffer)
        {
            if (buffer == null)
            {
                throw new ArgumentNullException("buffer");
            }

            try
            {
                Writer.write(buffer);
            }
            catch (java.io.IOException ex)
            {
                throw new IOException(ex.ToString(), ex);
            }
        }

        #endregion

        #region Write(char)

        /// <summary>
        /// Writes a character to this text stream.
        /// </summary>
        /// <param name="value">
        /// The character to write to this text stream.
        /// </param>
        /// <exception cref="T:System.IO.IOException">
        /// When an I/O error occurs.
        /// </exception>
        /// <exception cref="T:System.ObjectDisposedException">
        /// When this <see cref="T:System.IO.TextWriter"/> is closed.
        /// </exception>
        public override void Write(char value)
        {
            try
            {
                Writer.write(value);
            }
            catch (java.io.IOException ex)
            {
                throw new IOException(ex.ToString(), ex);
            }
        }

        #endregion

        #region Write(string)

        /// <summary>
        /// Writes a string to the text stream.
        /// </summary>
        /// <param name="value">
        /// The string to write.
        /// </param>
        /// <exception cref="T:System.ArgumentNullException">
        /// When the <c>value</c> parameter is null.
        /// </exception>
        /// <exception cref="T:System.IO.IOException">
        /// When an I/O error occurs.
        /// </exception>
        /// <exception cref="T:System.ObjectDisposedException">
        /// When this <see cref="T:System.IO.TextWriter"/> is closed.
        /// </exception>
        public override void Write(string value)
        {
            if (value == null)
            {
                throw new ArgumentNullException("value");
            }

            try
            {
                Writer.write(value);
            }
            catch (java.io.IOException ex)
            {
                throw new IOException(ex.ToString(), ex);
            }
        }

        #endregion

        #endregion Methods

        #region Properties

        #region Encoding

        /// <summary>
        /// Specifies the <see cref="T:System.Text.Encoding"/>
        /// in which the output is written.
        /// </summary>
        /// <value>
        /// The <see cref="T:System.Text.Encoding"/> in which
        /// the output is written; this is aways
        /// <c>BigEndianUnicode</c>.
        /// </value>
        public override Encoding Encoding
        {
            get { return Encoding.BigEndianUnicode; }
        }

        #endregion

        #endregion Properties

        #endregion Overrides
    }
}
