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
using System.IO;
#endregion

namespace System.Data.Hsqldb.Common.Lob
{
    #region IClob

    /// <summary>
    /// <para>
    /// The representation (mapping) of an SQL <c>CLOB</c> value.
    /// </para>
    /// <img src="/ClassDiagrams/System.Data.Hsqldb.Common.Lob.IClob.png" 
    ///      alt="IClob Class Diagram"/>
    /// </summary>
    /// <remarks>
    /// <para>
    /// An SQL <c>CLOB</c> is a built-in type that stores a Character Large Object
    /// as a column value in a row of a database table. By default, drivers
    /// implement an <c>IClob</c> object using an SQL locator(CLOB), which
    /// means that an <c>IClob</c> object typically contains a logical pointer
    /// to the SQL <c>CLOB</c> data rather than the data itself.
    /// </para>
    /// <para>
    /// In general, an <c>IClob</c> object obtained from an SQL statement
    /// execution is valid only for the duration of the transaction in which
    /// it was created.  For other <c>IClob</c> sources, the lifetime is
    /// implementation-dependent.
    /// </para>
    /// </remarks>
    /// <author name="boucherb@users"/>
    public interface IClob
    {
        #region GetAsciiStream()

        /// <summary>
        /// Retrieves the <c>CLOB</c> value designated by this
        /// <c>IClob</c> object as an ascii stream.
        /// </summary>
        /// <remarks>
        /// Note that multi-byte characters contained in the <c>CLOB</c> data
        /// are UTF-8 encoded in the stream.
        /// </remarks>
        /// <returns>
        /// A <see cref="System.IO.Stream"/> containing the <c>CLOB</c> data.
        /// </returns>
        /// <exception cref="System.Data.Common.DbException">
        /// If there is an error accessing the <c>CLOB</c> value.
        /// </exception>
        Stream GetAsciiStream();

        #endregion

        #region GetCharacterStream()

        /// <summary>
        /// Retrieves the <c>CLOB</c> value designated by this <c>IClob</c> object
        /// as a <see cref="System.IO.TextReader"/> object.
        /// </summary>
        /// <returns>
        /// A <see cref="System.IO.TextReader"/> containing the <c>CLOB</c> data
        /// </returns>
        /// <exception cref="System.Data.Common.DbException">
        /// If there is an error accessing the <c>CLOB</c> value.
        /// </exception>
        TextReader GetCharacterStream();

        #endregion

        #region GetSubString(long,int)

        /// <summary>
        /// Retrieves a copy of the specified substring in the <c>CLOB</c> value
        /// designated by this <c>IClob</c> object. The substring begins
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
        /// A String that is the specified substring in the <c>CLOB</c> value
        /// designated by this <c>IClob</c> object.
        /// </returns>
        /// <exception cref="System.Data.Common.DbException">
        /// If there is an error accessing the <c>CLOB</c> value.
        /// </exception>
        string GetSubString(long pos, int length);

        #endregion

        #region Length

        /// <summary>
        /// The number of characters in the <c>CLOB</c> value
        /// designated by this <c>IClob</c> object.
        /// </summary>
        /// <exception cref="System.Data.Common.DbException">
        /// If there is an error accessing the <c>CLOB</c> value.
        /// </exception>
        long Length
        {
            get;
        }

        #endregion

        #region Position(IClob,long)

        /// <summary>
        /// Retrieves the character position at which the specified
        /// <c>IClob</c> object <c>searchString</c> appears in this
        /// <c>IClob</c> object. The search begins at position
        /// <c>start</c>.
        /// </summary>
        /// <param name="searchString">
        ///  The <c>IClob</c> object for which to search.
        /// </param>
        /// <param name="start">
        /// The position at which to begin searching;
        /// the first position is 1
        /// </param>
        /// <returns>
        /// The position at which the Clob object appears or -1
        /// if it is not present; the first position is 1.
        /// </returns>
        /// <exception cref="System.Data.Common.DbException">
        /// If there is an error accessing the <c>CLOB</c> value.
        /// </exception>
        long Position(IClob searchString, long start);

        #endregion

        #region Position(string,start)

        /// <summary>
        /// Retrieves the character position at which the specified
        /// <c>searchString</c> appears in the SQL <c>CLOB</c> value represented
        /// by this <c>IClob</c> object; the search begins at position
        /// <c>start</c>.
        /// </summary>
        /// <param name="searchString">
        /// The character sequece for which to search.
        /// </param>
        /// <param name="start">
        /// The position in the <c>IClob</c> from which to start the search;
        /// the first position is 1.
        /// </param>
        /// <returns>
        /// The position at which the character sequence appears or -1
        /// if it is not present; the first position is 1.
        /// </returns>
        /// <exception cref="System.Data.Common.DbException">
        /// If there is an error accessing the <c>CLOB</c> value.
        /// </exception>
        long Position(string searchString, long start);

        #endregion

        #region SetAsciiStream(long)

        /// <summary>
        /// Retrieves a stream to be used to write Ascii characters to
        /// the <c>CLOB</c> value that this <c>IClob</c> object represents,
        /// starting at position <c>pos</c>.
        /// </summary>
        /// <param name="pos">
        /// The position at which to start writing to this <c>CLOB</c> object
        /// </param>
        /// <returns>
        /// A <see cref="System.IO.Stream"/> to which ASCII encoded
        /// characters can be written.
        /// </returns>
        /// <exception cref="System.Data.Common.DbException">
        /// If there is an error accessing the <c>CLOB</c> value.
        /// </exception>
        /// <exception cref="System.Data.Common.DbException">
        /// If there is an error accessing the <c>CLOB</c> value.
        /// </exception>
        Stream SetAsciiStream(long pos);

        #endregion

        #region SetCharacterStream(long)

        /// <summary>
        /// Retrieves a stream to be used to write a stream of Unicode
        /// characters to the <c>CLOB</c> value that this <c>IClob</c>
        /// object represents, at position <c>pos</c>.
        /// </summary>
        /// <param name="pos">
        /// The position at which to start writing to the <c>CLOB</c> value.
        /// </param>
        /// <returns>
        /// A <see cref="System.IO.Stream"/> to which Unicode encoded
        /// characters can be written
        /// </returns>
        /// <exception cref="System.Data.Common.DbException">
        /// If there is an error accessing the <c>CLOB</c> value.
        /// </exception>
        TextWriter SetCharacterStream(long pos);

        #endregion

        #region SetString(long,string,int,int)

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
        /// <returns></returns>
        /// <exception cref="System.Data.Common.DbException">
        /// If there is an error accessing the <c>CLOB</c> value.
        /// </exception>
        int SetString(long pos, string str, int offset, int length);

        #endregion

        #region SetString(long,string)

        /// <summary>
        /// Writes the given String to the <c>CLOB</c> value that this
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
        /// The number of characters written.
        /// </returns>
        /// <exception cref="System.Data.Common.DbException">
        /// If there is an error accessing the <c>CLOB</c> value.
        /// </exception>
        int SetString(long pos, string str);

        #endregion

        #region Truncate(long)

        /// <summary>
        /// Truncates the <c>CLOB</c> value that this <c>IClob</c> designates
        /// to have a length of len characters.
        /// </summary>
        /// <param name="length">
        /// The length, in characters, to which the <c>CLOB</c> value should
        /// be truncated.
        /// </param>
        /// <exception cref="System.Data.Common.DbException">
        /// If there is an error accessing the <c>CLOB</c> value.
        /// </exception>
        void Truncate(long length);

        #endregion

        #region Free()

        /// <summary>
        /// Frees the <c>CLOB</c> reference that this <c>IClob</c> object
        /// represents, releasing any resources that this object holds to
        /// maintain the reference.
        /// </summary>
        /// <remarks>
        /// This object is invalid while in a freed state; any attempt to
        /// invoke a method other than <c>Free</c>, <c>Wrap(object)</c>
        /// and <c>Unwrap()</c> while in this state should result in raising
        /// a <c>DbException</c>. While in a freed state, subsequent calls to
        /// <c>Free</c> should simply be ignored. After calling <c>Free</c>,
        /// an <c>IClob</c> may subsequently transition out of a freed state
        /// as the result of calling <c>Wrap(object)</c>.
        /// </remarks>
        /// <exception cref="System.Data.Common.DbException">
        /// If there is an error accessing the <c>CLOB</c> value.
        /// </exception>
        void Free();

        #endregion

        #region Wrap(object)

        /// <summary>
        /// Wraps the given object, exposing it as an <c>IClob</c>
        /// through this object.
        /// </summary>
        /// <remarks>
        /// <para>
        /// This is a completely optional operation.
        /// </para>
        /// <para>
        /// Invoking this operation while this object is not in a freed
        /// state should typically raise a
        /// <see cref="System.Data.Common.DbException"/>.
        /// </para>
        /// </remarks>
        /// <param name="o">
        /// The object to wrap.
        /// </param>
        /// <exception cref="System.NotImplementedException">
        /// When this <c>IClob</c> does not implement this operation.
        /// </exception>
        /// <exception cref="System.Data.Common.DbException">
        /// When this <c>IClob</c> object disallows the operation
        /// because it is not in a <c>Free</c>d state.
        /// </exception>
        void Wrap(object o);

        #endregion

        #region UnWrap()

        /// <summary>
        /// Retrieves the object that this <c>IClob</c> wraps.
        /// </summary>
        /// <remarks>
        /// If this <c>IClob</c> does not wrap another object, it should
        /// return a self-reference rather than <c>null</c>.
        /// </remarks>
        /// <returns>
        /// The the object wrapped by this <c>IClob</c> object.
        /// </returns>
        object UnWrap();

        #endregion
    }

    #endregion IClob
}
