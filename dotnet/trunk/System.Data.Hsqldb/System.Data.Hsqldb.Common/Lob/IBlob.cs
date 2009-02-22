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
    #region IBlob

    /// <summary>
    /// <para>
    /// The representation (mapping) of an SQL <c>BLOB</c> value.
    /// </para>
    /// <img src="/ClassDiagrams/System.Data.Hsqldb.Common.Lob.IBlob.png" 
    ///      alt="IBlob Class Diagram"/>
    /// </summary>
    /// <remarks>
    /// <para>
    /// An SQL <c>BLOB</c> is a built-in type that stores a Binary Large
    /// Object as a column value in a row of a database table. By default,
    /// data providers implement <c>IBlob</c> using an SQL locator(<c>BLOB</c>),
    /// which means that an <c>IBlob</c> object typically contains a logical
    /// pointer to the SQL <c>BLOB</c> data rather than the data itself.
    /// </para>
    /// <para>
    /// In general, an <c>IBlob</c> object obtained from an SQL statement
    /// execution is valid only for the duration of the transaction in which
    /// it was created.  For other <c>IBlob</c> sources, the lifetime is
    /// implementation-dependent.
    /// </para>
    /// </remarks>
    /// <author name="boucherb@users"/>
    public interface IBlob
    {
        #region CanSearch
        /// <summary>
        /// Retrieves whether the implementation supports the <c>Position</c>
        /// search operations.
        /// </summary>
        /// <remarks>
        /// The retrieved value may vary from one invocation to the next, 
        /// depending on a number of factors, such as the type of object
        /// wrapped by this <c>IClob</c> or changes in the state of the
        /// underlying data source.
        /// </remarks>
        /// <value>
        /// <c>true</c> if search is supported; <c>false</c> otherwise.
        /// </value>
        bool CanSearch { get; } 
        #endregion

        #region CanWrap
        /// <summary>
        /// Retrieves whether the implementation supports wrapping other
        /// objects to adapt them to the <c>IClob</c> interface.
        /// </summary>
        bool CanWrap { get; } 
        #endregion

        #region CanWrapType(Type)
        /// <summary>
        /// Retrieves whether the implementation supports wrapping the given
        /// type of object.
        /// </summary>
        /// <param name="type">to test</param>
        /// <returns>
        /// <c>true</c> if the implementation supports wrapping the given type;
        /// otherwise <c>false</c>.
        /// </returns>
        bool CanWrapType(Type type); 
        #endregion

        #region CanWrite
        /// <summary>
        /// Retrieves whether the implementation supports the
        /// <c>SetBinaryStream</c>, <c>SetBytes</c> and <c>Truncate</c>
        /// operations.
        /// </summary>
        /// <remarks>
        /// The retrieved value may vary from one invocation to the next, 
        /// depending on a number of factors, such as the type of object
        /// wrapped by this <c>IClob</c> or changes in the state of the
        /// underlying data source.
        /// </remarks>
        bool CanWrite { get; } 
        #endregion

        #region GetBinaryStream()

        /// <summary>
        /// Retrieves the <c>BLOB</c> value designated by this
        /// <c>IBlob</c> instance as a stream.
        /// </summary>
        /// <returns>
        /// A read-only <see cref="System.IO.Stream"/> containing
        /// the <c>BLOB</c> data.
        /// </returns>
        /// <exception cref="System.Data.Common.DbException">
        /// If there is an error accessing the <c>BLOB</c> value.
        /// </exception>
        /// <exception cref="System.InvalidOperationException">
        /// When this object is in the <see cref="Free()">Freed</see> state.
        /// </exception>
        Stream GetBinaryStream();

        #endregion

        #region GetBytes(long,int)

        /// <summary>
        /// Retrieves all or part of the <c>BLOB</c> value that this
        /// <c>IBlob</c> object represents, as an array of bytes.
        /// This byte array contains up to <c>length</c> consecutive
        /// bytes starting at position <c>pos</c>.
        /// </summary>
        /// <param name="pos">
        /// The ordinal position of the first byte in the <c>BLOB</c>
        /// value to be extracted; the first byte is at position 1.
        /// </param>
        /// <param name="length">
        /// The number of consecutive bytes to be copied.
        /// </param>
        /// <returns>
        /// A byte array containing up to <c>length</c> consecutive bytes
        /// from the <c>BLOB</c> value designated by this <c>IBlob</c>
        /// object, starting with the byte at position <c>pos</c>.
        /// </returns>
        /// <exception cref="System.Data.Common.DbException">
        /// If there is an error accessing the <c>BLOB</c> value.
        /// </exception>
        /// <exception cref="System.InvalidOperationException">
        /// When this object is in the <see cref="Free()">Freed</see> state.
        /// </exception>
        byte[] GetBytes(long pos, int length);

        #endregion

        #region Length

        /// <summary>
        /// The number of bytes in the <c>BLOB</c> value designated by
        /// this <c>IBlob</c> object.
        /// </summary>
        /// <exception cref="System.Data.Common.DbException">
        /// If there is an error accessing the <c>BLOB</c> value.
        /// </exception>
        /// <exception cref="System.InvalidOperationException">
        /// When this object is in the <see cref="Free()">Freed</see> state.
        /// </exception>
        /// <exception cref="System.InvalidOperationException">
        /// When this object is in the <see cref="Free()">Freed</see> state.
        /// </exception>
        long Length { get; }

        #endregion

        #region Position(IBlob,long)

        /// <summary>
        /// Retrieves the octet position in the <c>BLOB</c> value designated
        /// by this <c>IBlob</c> object at which <c>pattern</c> begins. The
        /// search begins at position <c>start</c>.
        /// </summary>
        /// <remarks>
        /// Support for this method is optional.
        /// </remarks>
        /// <param name="pattern">
        /// The <c>IBlob</c> object designating the <c>BLOB</c> value for
        /// which to search
        /// </param>
        /// <param name="start">
        /// The position in the <c>BLOB</c> value designated by this
        /// <c>IBlob</c> object at which to begin searching; the first
        /// position is 1.
        /// </param>
        /// <returns>
        /// The position at which <c>pattern</c> begins, else -1
        /// </returns>
        /// <exception cref="System.Data.Common.DbException">
        /// If there is an error accessing the <c>BLOB</c> value.
        /// </exception>
        /// <exception cref="System.InvalidOperationException">
        /// When this object is in the <see cref="Free()">Freed</see> state.
        /// </exception>
        /// <exception cref="System.NotSupportedException">
        /// When this method is not supported by the underlying implementation.
        /// </exception>
        long Position(IBlob pattern, long start);

        #endregion

        #region Position(byte[],long)

        /// <summary>
        /// Retrieves the octet position at which the specified <c>pattern</c>
        /// begins within the <c>BLOB</c> value designated by this <c>IBlob</c>
        /// object. The search for <c>pattern</c> begins at position
        /// <c>start</c>.
        /// </summary>
        /// <remarks>
        /// Support for this method is optional.
        /// </remarks>
        /// <param name="pattern">
        /// The octet pattern for which to search.
        /// </param>
        /// <param name="start">
        /// The position in the <c>BLOB</c> value at which to begin
        /// searching; the first position is 1
        /// </param>
        /// <returns>
        /// The position at which the pattern begins, else -1.
        /// </returns>
        /// <exception cref="System.Data.Common.DbException">
        /// If there is an error accessing the <c>BLOB</c> value.
        /// </exception>
        /// <exception cref="System.InvalidOperationException">
        /// When this object is in the <see cref="Free()">Freed</see> state.
        /// </exception>
        /// <exception cref="System.NotSupportedException">
        /// When this method is not supported by the underlying implementation.
        /// </exception>
        long Position(byte[] pattern, long start);

        #endregion

        #region SetBinaryStream(long)

        /// <summary>
        /// Retrieves a write-only octet stream that can be used to write to
        /// the <c>BLOB</c> value designated by this <c>IBlob</c> object. The stream
        /// begins at position <c>pos</c>.
        /// </summary>
        /// <remarks>
        /// Support for this method is optional.
        /// </remarks>
        /// <param name="pos">
        /// The position in the <c>BLOB</c> value at which to start writing;
        /// The first position is 1.
        /// </param>
        /// <returns>
        /// a <see cref="Stream"/> to which octet data can be written
        /// </returns>
        /// <exception cref="System.Data.Common.DbException">
        /// If there is an error accessing the <c>BLOB</c> value.
        /// </exception>
        /// <exception cref="System.InvalidOperationException">
        /// When this object is in the <see cref="Free()">Freed</see> state.
        /// </exception>
        /// <exception cref="System.NotSupportedException">
        /// When this method is not supported by the underlying implementation.
        /// </exception>
        Stream SetBinaryStream(long pos);

        #endregion

        #region SetBytes(long,byte[],int,int)

        /// <summary>
        /// Attempts to write all or part of the given <c>bytes</c> to the
        /// <c>BLOB</c> value designated by this <c>IBlob</c> object and returns
        /// the number of octets actually written. Writing starts at position
        /// <c>pos</c> in the <c>BLOB</c> value; Up to <c>length</c> octets from
        /// <c>bytes</c> are written, starting at <c>offset</c> within
        /// <c>bytes</c>.
        /// </summary>
        /// <remarks>
        /// Support for this method is optional.
        /// </remarks>
        /// <param name="pos">
        /// The position in the <c>BLOB</c> value at which to start writing;
        /// the first position is 1.
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
        /// <exception cref="System.Data.Common.DbException">
        /// If there is an error accessing the <c>BLOB</c> value.
        /// </exception>
        /// <exception cref="System.InvalidOperationException">
        /// When this object is in the <see cref="Free()">Freed</see> state.
        /// </exception>
        /// <exception cref="System.NotSupportedException">
        /// When this method is not supported by the underlying implementation.
        /// </exception>
        int SetBytes(long pos, byte[] bytes, int offset, int length);

        #endregion

        #region SetBytes(long,byte[])

        /// <summary>
        /// Attempts to write the given <c>bytes</c> to the <c>BLOB</c> value
        /// designated by this <c>IBlob</c> object, starting at position
        /// <c>pos</c>, and returns the actual number of octets written.
        /// </summary>
        /// <remarks>
        /// Support for this method is optional.
        /// </remarks>
        /// <param name="pos">
        /// The position in the <c>BLOB</c> value at which to start writing;
        /// the first position is 1.
        /// </param>
        /// <param name="bytes">
        /// The octets to be written to the <c>BLOB</c> value that this
        /// <c>IBlob</c> object represents.
        /// </param>
        /// <returns>
        /// The actual number of octets written.
        /// </returns>
        /// <exception cref="System.Data.Common.DbException">
        /// If there is an error accessing the <c>BLOB</c> value.
        /// </exception>
        /// <exception cref="System.InvalidOperationException">
        /// When this object is in the <see cref="Free()">Freed</see> state.
        /// </exception>
        /// <exception cref="System.NotSupportedException">
        /// When this method is not supported by the underlying implementation.
        /// </exception>
        int SetBytes(long pos, byte[] bytes);

        #endregion

        #region Truncate(long)

        /// <summary>
        /// Truncates, to <c>length</c> octets, the <c>BLOB</c> value
        /// designated by this <c>IBlob</c> object.
        /// </summary>
        /// <remarks>
        /// Support for this method is optional.
        /// </remarks>
        /// <param name="length">
        /// The length, in octets, to which to truncate the <c>BLOB</c> value
        /// designated by this <c>IBlob</c> object.
        /// </param>
        /// <exception cref="System.Data.Common.DbException">
        /// If there is an error accessing the <c>BLOB</c> value.
        /// </exception>
        /// <exception cref="System.InvalidOperationException">
        /// When this object is in the <see cref="Free()">Freed</see> state.
        /// </exception>
        /// <exception cref="System.NotSupportedException">
        /// When this method is not supported by the underlying implementation.
        /// </exception>
        void Truncate(long length);

        #endregion

        #region Free()

        /// <summary>
        /// Frees the <c>BLOB</c> reference that this <c>IBlob</c> object
        /// represents, releasing any resources that this object holds to
        /// maintain the reference.
        /// </summary>
        /// <remarks>
        /// An <c>IBlob</c> is invalid while in a freed state; any attempt to
        /// invoke a method other than <c>Free</c>, <c>Wrap(object)</c> or
        /// <c>Unwrap()</c> while in this state should result in raising a
        /// <c>System.InvalidOperationException</c>. While in a freed state,
        /// subsequent calls to <c>Free</c> should simply be ignored. After
        /// calling <c>Free</c>, an <c>IBlob</c> may subsequently transition
        /// out of the freed state and back into a valid state as the result
        /// of calling <see cref="Wrap(object)"/>
        /// </remarks>
        /// <exception cref="System.Data.Common.DbException">
        /// If there is an error accessing the <c>BLOB</c> value.
        /// </exception>
        void Free();

        #endregion

        #region Wrap(object)

        /// <summary>
        /// Wraps the given object, exposing it as an <c>IBlob</c>
        /// through this object.
        /// </summary>
        /// <remarks>
        /// <para>
        /// This is a completely optional operation.
        /// </para>
        /// <para>
        /// Althogh not an absolute requirment, invoking this operation while
        /// this object is not in a freed state should typically raise a
        /// <see cref="System.InvalidOperationException"/>.
        /// </para>
        /// </remarks>
        /// <param name="obj">
        /// The object to wrap.
        /// </param>
        /// <exception cref="System.ArgumentNullException">
        /// When <c>obj</c> is <c>null</c>.
        /// </exception>
        /// <exception cref="System.ArgumentException">
        /// When this <c>IBlob</c> implementation does not know how to wrap
        /// objects of the runtime <see cref="System.Type"/> of the given
        /// <c>obj</c> instance.
        /// </exception>
        /// <exception cref="System.InvalidOperationException">
        /// When this <c>IBlob</c> object disallows the operation,
        /// for instance because it is not presently in a freed state.
        /// </exception>
        /// <exception cref="System.NotSupportedException">
        /// When this method is not supported by the underlying implementation.
        /// </exception>
        void Wrap(object obj);

        #endregion

        #region UnWrap()

        /// <summary>
        /// Retrieves the object that this <c>IBlob</c> wraps.
        /// </summary>
        /// <remarks>
        /// If an <c>IBlob</c> implementation does not wrap another object,
        /// it should return a self-reference rather than <c>null</c>.
        /// </remarks>
        /// <returns>
        /// The object that this <c>IBlob</c> wraps.
        /// </returns>
        object UnWrap();

        #endregion
    }

    #endregion IBlob
}
