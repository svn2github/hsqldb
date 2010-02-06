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
using System.Runtime.Serialization.Formatters.Binary;
using System.Runtime.Serialization;

using JavaObject = org.hsqldb.types.JavaObject;
using Serializable = java.io.Serializable;
#endregion

namespace System.Data.Hsqldb.Common.Sql.Types
{
    #region SqlObject
    /// <summary>
    /// <para>
    /// The representation (mapping) of an <c>SQL OBJECT</c> value.
    /// </para>
    /// <img src="../Documentation/ClassDiagrams/System.Data.Hsqldb.Common.Sql.Type.SqlObject.png"
    ///      alt="DotNetObject Class Diagram"/>
    /// </summary>
    /// <remarks>
    /// <para>
    /// An <c>SQL OBJECT</c> is a built-in type that stores the serialized
    /// form of a value from the client's type system as an octet sequence
    /// column value in a row of a database table.
    /// </para>
    /// <para>
    /// Because the engine uses a simple octet sequence to store each 
    /// <c>SQL OBJECT</c> value, it is possible to store and retrieve values
    /// of <em>any</em> client type, so long as it is possible within the
    /// client type system to automatically convert values of a type to and
    /// from serial form.
    /// </para>
    /// <para>
    /// However, please note that in order to use an <c>SQL OBJECT</c> value
    /// with an SQL operator or SQL-invoked routine, it is required in most
    /// cases that the hosting database instance <em>also</em> be able to
    /// automatically convert <c>SQL OBJECT</c> values to and from serial
    /// form.  This in turn implies that the runtime hosting the database
    /// instance must support the type of each original value, as well as the
    /// transitive closure of the types involved in converting each
    /// original value to serial form, as is consistent with the serialization
    /// constraints imposed by Java, .NET and other serialization-capable
    /// runtimes.
    /// </para>
    /// <para>
    /// From Java, this means ensuring that the database instance can find all
    /// of the classes it needs at runtime, for example by configuring the
    /// <c>CLASSPATH</c> environment variable.  From .NET, this means ensuring
    /// that the database instance can load all of the assemblies it needs at
    /// runtime, which likely implies installing assemblies in the global
    /// assembly cache (GAC), co-locating them in the directory containing
    /// the executable with which the database instance is started, or registering
    /// and alternative assembly resolution provider with the app domain hosting
    /// the database instance.
    /// </para>
    /// </remarks>
    /// <author name="boucherb@users"/>
    [CLSCompliant(false)]
    public sealed class SqlObject : JavaObject
    {
        #region Fields

        #region SerializationHeader
        /// <summary>
        /// The header written to a serialization stream to indicate that
        /// <see cref="BinaryFormatter.Serialize(Stream,Object)"/> was used to
        /// produce the serialiazed form.
        /// </summary>
        /// <value>{EABED6CD-8225-4ac9-A57E-E3587465760B}</value>
        public static readonly Guid SerializationHeader = new Guid(
            "EABED6CD-8225-4ac9-A57E-E3587465760B"); 
        #endregion

        #region m_HeaderBytes
        /// <summary>
        /// Private copy of <c>SerializationHeader</c>, in a ready-to-use form.
        /// </summary>
        private static readonly byte[] m_HeaderBytes = SqlObject
            .SerializationHeader.ToByteArray(); 
        #endregion

        #endregion

        #region Public Static Methods
        
        #region AddSerializationHeader(byte[])
        /// <summary>
        /// Creates and returns a new <c>byte[]</c> that is the concatenation
        /// of the <c>SerializationHeader</c> and the given <c>bytes</c>.
        /// </summary>
        /// <remarks>
        /// Typically, there is no need to use this method directly. The
        /// exception is when an external interface requires that an object
        /// graph in serial form is delivered as a byte array, such that the
        /// array may then be sent back here for deserialization, without some
        /// separate flag indicating which serialization mechanism was used:
        /// <c>java.io.ObjectOutputStream</c> or <see cref="BinaryFormatter"/>
        /// </remarks>
        /// <param name="bytes">To which to add the serialization header.</param>
        /// <returns>
        /// The concatenation of the <c>SerializationHeader</c> and the given
        /// <c>bytes</c>.
        /// </returns>
        public static byte[] AddSerializationHeader(byte[] bytes)
        {
            if (bytes == null)
            {
                throw new ArgumentNullException("bytes");
            }

            byte[] buffer = new byte[16 + bytes.Length];

            m_HeaderBytes.CopyTo(buffer, 0);
            bytes.CopyTo(buffer, 16);

            return buffer;
        }
        #endregion

        #region StartsWithSerializationHeader(byte[])
        /// <summary>
        /// Determines whether the specified value starts with the
        /// <see cref="SerializationHeader"/>.
        /// </summary>
        /// <param name="value">The value to test.</param>
        /// <returns>
        /// <c>true</c> if the specified value starts with the
        /// <c>SerializationHeader</c>; otherwise, <c>false</c>.
        /// </returns>
        /// <exception cref="ArgumentNullException">
        /// When <c>value</c> is <c>null</c>.
        /// </exception>
        public static bool StartsWithSerializationHeader(byte[] value)
        {
            if (value == null)
            {
                throw new ArgumentNullException("value");
            }

            bool startsWithHeader = true; // maybe
            byte[] headerBytes = m_HeaderBytes;
            int headerBytesLength = headerBytes.Length;

            if (value.Length < headerBytesLength)
            {
                startsWithHeader = false;
            }
            else
            {
                for (int i = 0; i < headerBytesLength; i++)
                {
                    if (value[i] != headerBytes[i])
                    {
                        startsWithHeader = false;
                        break;
                    }
                }
            }

            return startsWithHeader;
        }
        #endregion

        #region Serialize(object)
        /// <summary>
        /// Serializes the specified value.
        /// </summary>
        /// <remarks>
        /// If the given value is <c>java.io.Serializable</c>, then it is
        /// serialed using a <c>java.io.ObjectOutputStream</c>; otherwise, it
        /// is serialized using a .NET <c>BinaryFormatter</c> and the
        /// resulting array of octets starts with the 
        /// <see cref="SerializationHeader"/>.
        /// </remarks>
        /// <param name="value">The value to serialize.</param>
        /// <returns>The serialized form of the value.</returns>
        public static byte[] Serialize(object value)
        {
            if (value == null)
            {
                throw new ArgumentNullException("value");
            }

            bool isArray = value.GetType().IsArray;
            int rank = (isArray ? ((Array)value).Rank : 0);
            bool isJavaSerializable = (isArray)
                ? Serializable.IsInstanceArray(value, rank)
                : Serializable.IsInstance(value);

            byte[] bytes;

            if (isJavaSerializable)
            {
                bytes = org.hsqldb.lib.InOutUtil.serialize(Serializable.Cast(value));
            }
            else
            {
                using (MemoryStream stream = new MemoryStream())
                {
                    BinaryFormatter formatter = new BinaryFormatter();

                    stream.Write(m_HeaderBytes, 0, m_HeaderBytes.Length);
                    formatter.Serialize(stream, value);

                    bytes = stream.ToArray();
                }
            }

            return bytes;            
        }
        #endregion

        #region  Deserialize(byte[],out bool)
        /// <summary>
        /// Deserializes the specified array of octets.
        /// </summary>
        /// <remarks>
        /// If the given <c>value</c> starts with the
        /// <see cref="SerializationHeader"/>, then the value is deserialized
        /// using a .NET <c>BinaryFomatter</c>; otherwise, it is deserialized
        /// using a <c>java.io.ObjectInputStream</c>.
        /// </remarks>
        /// <param name="value">
        /// The array of octets to deserialize.
        /// </param>
        /// <param name="isJavaObject">
        /// <c>true</c> when the given <c>value</c> is the <em>serialed
        /// form</em> of a <c>java.lang.Object</c>; <c>false</c> when the
        /// given value is the <em>serialized form</em> of a
        /// <c>System.Object</c>.
        /// </param>
        /// <returns>
        /// The object graph obtained by deserializing the given array of
        /// octets.
        /// </returns>
        public static object Deserialize(
            byte[] value,
            out bool isJavaObject)
        {
            bool hasHeader = StartsWithSerializationHeader(value);
            object obj;

            if (hasHeader)
            {
                isJavaObject = false;
                BinaryFormatter formatter = new BinaryFormatter();

                using (MemoryStream stream
                    = new MemoryStream(value, 16, value.Length - 16))
                {
                    obj = formatter.Deserialize(stream);
                }
            }
            else
            {
                isJavaObject = true;

                Serializable serializable = org.hsqldb.lib.InOutUtil.deserialize(value);

                obj = serializable.ToObject();
            }

            return obj;
        }
        #endregion 
        
        #endregion

        #region Constructors

        #region SqlObject(byte[])
        /// <summary>
        /// Constructs a new <c>SqlObject</c> instance encapsulating the
        /// given octet sequence, which represents an object graph in
        /// serialized form.
        /// </summary>
        /// <param name="data">
        /// An object graph in serialized form.
        /// </param>
        public SqlObject(byte[] data)
            : base(data)
        {
        }
        #endregion

        #region SqlObject(byte[], bool)
        /// <summary>
        /// Constructs a new <c>SqlObject</c> instance encapsulating the
        /// given octet sequence, which represents an object graph in
        /// serialized form.
        /// </summary>
        /// <param name="data">
        /// An object graph in serialized form.
        /// </param>
        /// <param name="isJavaObject">
        /// Use <c>true</c> to indicate that the serialized form was produced
        /// by writing a <c>java.io.Serializable</c> object graph to a
        /// <c>java.io.ObjectOutputStream</c>; otherwise, the
        /// <see cref="SerializationHeader"/> is automatically added because
        /// it must be assumed that the serialized form  was produced using
        /// <see cref="BinaryFormatter.Serialize(Stream,Object)"/>.
        /// </param>
        public SqlObject(byte[] data, bool isJavaObject)
            : base(isJavaObject ? data : AddSerializationHeader(data))
        {
        }
        #endregion

        #region SqlObject(object)
        /// <summary>
        /// Constructs a new <c>SqlObject</c> instance encapsulating the
        /// given value.
        /// </summary>
        /// <remarks>
        /// This is a convenience constructor and is equivalent to:
        /// <code lang="cs">
        /// new SqlObject(SqlObject.Serialize(value))
        /// </code>
        /// </remarks>
        /// <param name="value">The wrapped object</param>
        public SqlObject(object value)
            : base(SqlObject.Serialize(value))
        {
        }

        #endregion

        #endregion

        #region getObject()
        /// <summary>
        /// Retreives the <c>SQL OBJECT</c> value represented by this instance.
        /// </summary>
        /// <remarks>
        /// To ensure transactional isolation, the retrieved value is actually a
        /// deep clone of the <c>SQL OBJECT</c> value represented by this
        /// instance.
        /// </remarks>
        /// <returns>
        /// The <c>SQL OBJECT</c> value represented by this instance.
        /// </returns>
        public new object getObject()
        {
            bool isJavaObject; // dummy to satisfy UnWrap signature.
            
            return UnWrap(out isJavaObject);
        }
        #endregion

        #region UnWrap(out bool)
        /// <summary>
        /// Retrieves a deep clone of value represented by this object.
        /// </summary>
        /// <param name="isJavaObject">
        /// <c>true</c> indicates the returned object is a
        /// <c>java.lang.Object</c> that implements
        /// <c>java.io.Serializable</c>; otherwise, it is a
        /// <c>System.Object</c>.
        /// </param>
        /// <returns>
        /// A deep clone of the serializable object encapsulated by this
        /// object.
        /// </returns>
        public object UnWrap(out bool isJavaObject)
        {
            return SqlObject.Deserialize(base.getBytes(), out isJavaObject);
        }
        #endregion
    } 
    #endregion
}