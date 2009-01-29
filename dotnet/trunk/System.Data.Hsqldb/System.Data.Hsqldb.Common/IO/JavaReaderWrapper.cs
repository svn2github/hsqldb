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

using System;
using System.IO;
using java.io;
using java.lang;
using Exception=System.Exception;
using IOException=System.IO.IOException;
using StringBuilder=System.Text.StringBuilder;

#endregion

namespace System.Data.Hsqldb.Common.IO
{
    #region JavaReaderWrapper

    /// <summary>
    /// <para>
    /// Wraps a <c>java.io.Reader</c>,
    /// exposing it as a <c>System.IO.TextReader</c>.
    /// </para>
    /// <img src="/ClassDiagrams/System.Data.Hsqldb.Common.IO.JavaReaderWrapper.png" 
    ///      alt="JavaReaderWrapper Class Diagram"/>
    /// </summary>
    /// <author name="boucherb@users"/>
    public sealed class JavaReaderWrapper : TextReader
    {
        #region Private Members

        #region Fields

        private volatile java.io.Reader m_in;
        private volatile java.io.PushbackReader m_reader;
        private volatile java.io.BufferedReader m_lineReader;

        #endregion

        #region Reader

        /// <summary>
        /// Gets the reader.
        /// </summary>
        /// <value>The reader.</value>
        ///
        private java.io.PushbackReader Reader
        {
            get
            {
                lock(this)
                {
                    java.io.PushbackReader reader = m_reader;

                    if (reader == null)
                    {
                        throw new ObjectDisposedException("Reader is closed.");
                    }

                    return reader;
                }
            }
        }

        #region LineReader
        private java.io.BufferedReader LineReader
        {
            get
            {
                lock (this)
                {
                    java.io.BufferedReader reader = m_lineReader;

                    if (reader == null)
                    {
                        throw new ObjectDisposedException("Reader is closed.");
                    }

                    return reader;
                }
            }
        } 
        #endregion

        #endregion

        #endregion

        #region Contructors

        #region JavaReaderWrapper(java.io.Reader)

        /// <summary>
        /// Construct a new JavaReaderWrapper instance wrapping the given <c>reader</c>.
        /// </summary>
        /// <param name="reader">
        /// The <c>java.io.Reader</c> to wrap.
        /// </param>
        [CLSCompliant(false)]
        public JavaReaderWrapper(java.io.Reader reader)
        {
            if (reader == null)
            {
                throw new NullReferenceException("reader");
            }

            try
            {
                m_reader = new PushbackReader(reader, 1);
                m_lineReader = new BufferedReader(m_reader);
            }
            catch (Exception e)
            {
                throw new IOException(e.ToString(), e);
            }

            m_in = reader;
        }

        #endregion

        #endregion

        #region Method Overrides

        #region Dispose(bool)

        /// <summary>
        /// Releases the unmanaged resources used by this
        /// <see cref="T:System.IO.TextReader"/> and optionally
        /// releases the managed resources.
        /// </summary>
        /// <param name="disposing">
        /// Indicates whether disposale is occuring in the manageable code space.
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
                        throw new IOException(ex.toString(), ex);
                    }
                    finally
                    {
                        m_in = null;
                        m_reader = null;
                        m_lineReader = null;

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

        #region Peek()

        /// <summary>
        /// Reads the next character without changing the state of the reader
        /// or the character source. Returns the next available character without
        /// actually reading it from the input stream.
        /// </summary>
        /// <returns>
        /// The next character to be read, or -1 if no more characters are available
        /// or the stream does not support seeking.
        /// </returns>
        /// <exception cref="T:System.IO.IOException">
        /// When an I/O error occurs.</exception>
        /// <exception cref="T:System.ObjectDisposedException">
        /// When this <see cref="T:System.IO.TextReader"></see> is closed.
        /// </exception>
        public override int Peek()
        {
            try
            {
                lock(this)
                {
                    java.io.PushbackReader reader = Reader;

                    int peekValue = reader.read();

                    if (peekValue != -1)
                    {
                        reader.unread(peekValue);
                    }

                    return peekValue;
                }
            }
            catch (java.io.IOException ex)
            {
                throw new IOException(ex.toString(), ex);
            }
        }

        #endregion

        #region Read()

        /// <summary>
        /// Reads the next character from this input stream and advances the
        /// character position by one character.
        /// </summary>
        /// <returns>
        /// The next character from the input stream, or -1 if no more characters
        /// are available.
        /// </returns>
        /// <exception cref="T:System.IO.IOException">
        /// When an I/O error occurs.
        /// </exception>
        /// <exception cref="T:System.ObjectDisposedException">
        /// When this <see cref="T:System.IO.TextReader"></see> is closed.
        /// </exception>
        public override int Read()
        {
            try
            {
                return Reader.read();
            }
            catch (java.io.IOException ex)
            {
                throw new IOException(ex.ToString(), ex);
            }
        }

        #endregion

        #region Read(char[],int,int)

        /// <summary>
        /// See <see cref="System.IO.TextReader.Read(char[],int,int)"/>.
        /// </summary>
        /// <param name="buffer">
        /// The buffer into which to copy characters from the stream.
        /// </param>
        /// <param name="index">
        /// The offset into the buffer at which to start copying characters.
        /// </param>
        /// <param name="count">
        /// The number of characters to copy from the stream.
        /// </param>
        /// <returns>
        /// The number of characters actually copied.
        /// This will be -1 if the end of the stream has been reached.
        /// </returns>
        /// <exception cref="T:System.IO.IOException">
        /// When an I/O error occurs.
        /// </exception>
        /// <exception cref="T:System.ArgumentOutOfRangeException">
        /// When index or count is negative.
        /// </exception>
        /// <exception cref="T:System.ArgumentException">
        /// When the <c>buffer</c> length minus <c>index</c> is less than <c>count</c>.
        /// </exception>
        /// <exception cref="T:System.ArgumentNullException">
        /// When <c>buffer</c> is null.
        /// </exception>
        /// <exception cref="T:System.ObjectDisposedException">
        /// When this <see cref="T:System.IO.TextReader"/> is closed.
        /// </exception>
        public override int Read(char[] buffer, int index, int count)
        {
            if(buffer == null)
            {
                throw new ArgumentNullException("buffer");
            }
            else if (index < 0)
            {
                throw new ArgumentOutOfRangeException(
                    "Illegal negative value: " + index,
                    "index");
            }
            else if (count < 0)
            {
                throw new ArgumentOutOfRangeException(
                    "Illegal negative value: " + count,
                    "count");
            }
            else if (buffer.Length - index < count)
            {
                throw new ArgumentException(
                    "buffer length minus index is less than count");
            }

            try
            {
                return Reader.read(buffer, index, count);
            }
            catch (java.io.IOException ex)
            {
                throw new IOException(ex.ToString(), ex);
            }
        }

        #endregion

        #region ReadLine()

        /// <summary>
        /// See <see cref="System.IO.TextReader.ReadLine()"/>.
        /// </summary>
        /// <returns>
        /// A line's worth of characters read from the stream.
        /// This will be null if the end of the stream has been reached.
        /// </returns>
        /// <exception cref="T:System.ArgumentOutOfRangeException">
        /// The number of characters in the next line is larger than
        /// <see cref="F:System.Int32.MaxValue"/>
        /// </exception>
        /// <exception cref="T:System.IO.IOException">
        /// When an I/O error occurs.
        /// </exception>
        /// <exception cref="T:System.OutOfMemoryException">
        /// When there is insufficient memory to allocate a buffer for the returned string.
        /// </exception>
        /// <exception cref="T:System.ObjectDisposedException">
        /// When this <see cref="T:System.IO.TextReader"></see> is closed.
        /// </exception>
        public override string ReadLine()
        {
            try
            {
                return LineReader.readLine();
            }
            catch (OutOfMemoryError e)
            {
                throw new OutOfMemoryException(e.toString(), e);
            }
            catch (Exception ex)
            {
                throw new IOException(ex.ToString(), ex);
            }
        }

        #endregion

        #region ReadToEnd()

        /// <summary>
        /// Reads all characters from the current position to the end of
        /// this TextReader and returns them as one string.
        /// </summary>
        /// <returns>
        /// All of the characters from the current stream position to the end.
        /// This will be null if the end of the stream has already been reached.
        /// </returns>
        /// <exception cref="T:System.ArgumentOutOfRangeException">
        /// When the number of characters in the next line is larger than
        /// <see cref="F:System.Int32.MaxValue"/>
        /// </exception>
        /// <exception cref="T:System.IO.IOException">
        /// When an I/O error occurs.
        /// </exception>
        /// <exception cref="T:System.OutOfMemoryException">
        /// When there is insufficient memory to allocate a buffer for the returned string.
        /// </exception>
        /// <exception cref="T:System.ObjectDisposedException">
        /// When this <see cref="T:System.IO.TextReader"/> is closed.
        /// </exception>
        public override string ReadToEnd()
        {
            StringBuilder sb;

            lock (this)
            {
                Reader l_reader = Reader;
                char[] buff = new char[1024];
                int count;

                sb = new StringBuilder();

                try
                {
                    while (-1 != (count = l_reader.read(buff, 0, 1024)))
                    {
                        sb.Append(buff, 0, count);
                    }
                }
                catch (java.lang.OutOfMemoryError e)
                {
                    throw new OutOfMemoryException(e.toString(), e);
                }
                catch (java.io.IOException ex)
                {
                    throw new IOException(ex.ToString(), ex);
                }
            }

            return (sb.Length == 0)
                ? null
                : sb.ToString();
        }

        #endregion

        #endregion
    }

    #endregion
}
