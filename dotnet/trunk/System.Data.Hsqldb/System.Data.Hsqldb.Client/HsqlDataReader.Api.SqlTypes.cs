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

using System.Collections;
using System.Data;
using System.Data.Common;
using System.Data.Hsqldb.Common.IO;
using System.Data.Hsqldb.Common.Enumeration;
using System.Data.Hsqldb.Client.MetaData;
using System.Data.Hsqldb.Common.Sql.Type;
using System.Data.SqlTypes;
using System.IO;
using System.Runtime.Serialization;
using System.Security;
using System.Text;
using System.Xml;

using Trace = org.hsqldb.Trace;
using System.Data.Hsqldb.Common;
using System.IO.Compression;

#endregion

namespace System.Data.Hsqldb.Client
{
    #region HsqlDataReader

    public partial class HsqlDataReader : DbDataReader
    {
        #region System.Data.SqlTypes Getter Methods

        #region GetSqlBinary(int)
        /// <summary>
        /// Gets the value of the specified column as a 
        /// <see cref="SqlBinary">SqlBinary</see>.
        /// </summary>
        /// <returns>
        /// An <see cref="SqlBinary">SqlBinary</see>.
        /// </returns>
        /// <param name="ordinal">
        /// The zero-based column ordinal.
        /// </param>
        public SqlBinary GetSqlBinary(int ordinal)
        {
            CheckClosed();
            CheckAvailable();

            object value = m_currentRecord.data[ordinal];

            return (value == null)
                ? SqlBinary.Null
                : (value is org.hsqldb.types.Binary)
                ? ((org.hsqldb.types.Binary)value).getBytes()
                : HsqlConvert.FromJava.ToBinary(value);
        }
        #endregion

        #region GetSqlBoolean(int)
        /// <summary>
        /// Gets the value of the specified column as an 
        /// <see cref="SqlBoolean">SqlBoolean</see>.
        /// </summary>
        /// <returns>
        /// An <see cref="SqlBoolean">SqlBoolean</see>
        /// </returns>
        /// <param name="ordinal">
        /// The zero-based column ordinal.
        /// </param>
        public SqlBoolean GetSqlBoolean(int ordinal)
        {
            CheckClosed();
            CheckAvailable();

            object value = m_currentRecord.data[ordinal];

            return (value == null)
                ? SqlBoolean.Null
                : (value is java.lang.Boolean)
                ? ((java.lang.Boolean)value).booleanValue()
                : HsqlConvert.FromJava.ToBoolean(value);
        }
        #endregion

        #region GetSqlByte(int)
        /// <summary>
        /// Gets the value of the specified column as an
        /// <see cref="SqlByte">SqlByte</see>.
        /// </summary>
        /// <returns>
        /// An <see cref="SqlByte">SqlByte</see>.
        /// </returns>
        /// <param name="ordinal">The zero-based column ordinal. </param>
        public SqlByte GetSqlByte(int ordinal)
        {
            CheckClosed();
            CheckAvailable();

            object value = m_currentRecord.data[ordinal];

            checked
            {
                return (value == null)
                    ? SqlByte.Null
                    : (value is java.lang.Integer)
                    ? (byte)((java.lang.Integer)value).intValue()
                    : (byte)HsqlConvert.FromJava.ToSmallInt(value);
            }
        }
        #endregion

        #region GetSqlBytes(int)
        /// <summary>
        /// Gets the value of the specified column as 
        /// <see cref="SqlBytes"/>.
        /// </summary>
        /// <returns>A <see cref="SqlBytes"/>.</returns>
        /// <param name="ordinal">The zero-based column ordinal.</param>
        public SqlBytes GetSqlBytes(int ordinal)
        {
            CheckClosed();
            CheckAvailable();

            object value = m_currentRecord.data[ordinal];

            return (value == null)
                ? SqlBytes.Null
                : (value is org.hsqldb.types.Binary)
                ? new SqlBytes(((org.hsqldb.types.Binary)value).getBytes())
                // TODO:
                //: (value is IBlob) 
                //? new SqlBytes(((IBlob)value).GetBinaryStream())
                : new SqlBytes(HsqlConvert.FromJava.ToBinary(value));
        }
        #endregion

        #region GetSqlChars(int)
        /// <summary>
        /// Gets the value of the specified column as 
        /// <see cref="SqlChars"/>.
        /// </summary>
        /// <returns>
        /// A <see cref="SqlChars"/>.
        /// </returns>
        /// <param name="ordinal">
        /// The zero-based column ordinal.
        /// </param>
        public SqlChars GetSqlChars(int ordinal)
        {
            CheckClosed();
            CheckAvailable();

            object value = m_currentRecord.data[ordinal];

            return (value == null)
                ? SqlChars.Null
                : (value is string)
                ? new SqlChars(((string)value).ToCharArray())
                : new SqlChars(
                    HsqlConvert.FromJava.ToString(value).ToCharArray());
        }
        #endregion

        #region GetSqlDateTime(int)
        /// <summary>
        /// Gets the value of the specified column as a 
        /// <see cref="SqlDateTime"/>.
        /// </summary>
        /// <returns>
        /// A <see cref="SqlDateTime"/>.
        /// </returns>
        /// <param name="ordinal">
        /// The zero-based column ordinal.
        /// </param>
        public SqlDateTime GetSqlDateTime(int ordinal)
        {
            CheckClosed();
            CheckAvailable();

            object value = m_currentRecord.data[ordinal];

            return (value == null)
                ? SqlDateTime.Null
                : HsqlConvert.FromJava.ToTimestamp(value);
        }
        #endregion

        #region GetSqlDecimal(int)
        /// <summary>
        /// Gets the value of the specified column as a 
        /// <see cref="SqlDecimal"/>.
        /// </summary>
        /// <returns>
        /// A <see cref="SqlDecimal"/>.
        /// </returns>
        /// <param name="ordinal">
        /// The zero-based column ordinal.
        /// </param>
        public SqlDecimal GetSqlDecimal(int ordinal)
        {
            CheckClosed();
            CheckAvailable();

            object value = m_currentRecord.data[ordinal];

            return (value == null)
                ? SqlDecimal.Null
                : HsqlConvert.FromJava.ToDecimal(value);
        }
        #endregion

        #region GetSqlDouble(int)
        /// <summary>
        /// Gets the value of the specified column as a 
        /// <see cref="SqlDouble"/>.
        /// </summary>
        /// <returns>
        /// A <see cref="SqlDouble"/>.
        /// </returns>
        /// <param name="ordinal">
        /// The zero-based column ordinal.
        /// </param>
        public SqlDouble GetSqlDouble(int ordinal)
        {
            CheckClosed();
            CheckAvailable();

            object value = m_currentRecord.data[ordinal];

            return (value == null)
                ? SqlDouble.Null
                : HsqlConvert.FromJava.ToDouble(value);
        }
        #endregion

        #region GetSqlGuid(int)
        /// <summary>
        /// Gets the value of the specified column as a 
        /// <see cref="SqlGuid"/>.
        /// </summary>
        /// <returns>
        /// A <see cref="SqlGuid"/>.
        /// </returns>
        /// <param name="ordinal">
        /// The zero-based column ordinal. 
        /// </param>
        public SqlGuid GetSqlGuid(int ordinal)
        {
            return (IsDBNull(ordinal))
                ? SqlGuid.Null
                : GetGuid(ordinal);
        }
        #endregion

        #region GetSqlInt16(int)
        /// <summary>
        /// Gets the value of the specified column as a 
        /// <see cref="SqlInt16"/>.
        /// </summary>
        /// <returns>
        /// A <see cref="SqlInt16"/>.
        /// </returns>
        /// <param name="ordinal">
        /// The zero-based column ordinal.
        /// </param>
        public SqlInt16 GetSqlInt16(int ordinal)
        {
            CheckClosed();
            CheckAvailable();

            object value = m_currentRecord.data[ordinal];

            return (value == null)
                ? SqlInt16.Null
                : new SqlInt16((short)HsqlConvert.FromJava.ToSmallInt(value));
        }
        #endregion

        #region GetSqlInt32(int)
        /// <summary>
        /// Gets the value of the specified column as a 
        /// <see cref="SqlInt32"/>.
        /// </summary>
        /// <returns>
        /// A <see cref="SqlInt32"/>.
        /// </returns>
        /// <param name="ordinal">
        /// The zero-based column ordinal. 
        /// </param>
        public SqlInt32 GetSqlInt32(int ordinal)
        {
            CheckClosed();
            CheckAvailable();

            object value = m_currentRecord.data[ordinal];

            return (value == null)
                ? SqlInt16.Null
                : new SqlInt32(HsqlConvert.FromJava.ToInteger(value));
        }
        #endregion

        #region GetSqlInt64(int)
        /// <summary>
        /// Gets the value of the specified column as a 
        /// <see cref="SqlInt64"/>.
        /// </summary>
        /// <returns>
        /// A <see cref="SqlInt64"/>.
        /// </returns>
        /// <param name="ordinal">
        /// The zero-based column ordinal. 
        /// </param>
        public SqlInt64 GetSqlInt64(int ordinal)
        {
            CheckClosed();
            CheckAvailable();

            object value = m_currentRecord.data[ordinal];

            return (value == null)
                ? SqlInt64.Null
                : new SqlInt64(HsqlConvert.FromJava.ToBigInt(value));
        }
        #endregion

        #region GetSqlMoney(int)
        /// <summary>
        /// Gets the value of the specified column as a 
        /// <see cref="SqlMoney"/>.
        /// </summary>
        /// <returns>
        /// A <see cref="SqlMoney"/>.
        /// </returns>
        /// <param name="ordinal">
        /// The zero-based column ordinal. 
        /// </param>
        public SqlMoney GetSqlMoney(int ordinal)
        {
            CheckClosed();
            CheckAvailable();

            object value = m_currentRecord.data[ordinal];

            return (value == null) ? SqlMoney.Null
                : new SqlMoney(HsqlConvert.FromJava.ToDecimal(value));
        }
        #endregion

        #region GetSqlSingle(int)
        /// <summary>
        /// Gets the value of the specified column as a 
        /// <see cref="SqlSingle"/>.
        /// </summary>
        /// <returns>
        /// A <see cref="SqlSingle"/>.
        /// </returns>
        /// <param name="ordinal">
        /// The zero-based column ordinal. 
        /// </param>
        public SqlSingle GetSqlSingle(int ordinal)
        {
            CheckClosed();
            CheckAvailable();

            object value = m_currentRecord.data[ordinal];

            return (value == null)
                ? SqlSingle.Null
                : new SqlSingle(HsqlConvert.FromJava.ToReal(value));
        }
        #endregion

        #region GetSqlString(int)
        /// <summary>
        /// Gets the value of the specified column as a 
        /// <see cref="SqlString"/>.
        /// </summary>
        /// <returns>
        /// A <see cref="SqlString"/>.
        /// </returns>
        /// <param name="ordinal">
        /// The zero-based column ordinal.
        /// </param>
        public SqlString GetSqlString(int ordinal)
        {
            CheckClosed();
            CheckAvailable();

            object value = m_currentRecord.data[ordinal];

            return (value == null)
                ? SqlString.Null
                : new SqlString(HsqlConvert.FromJava.ToString(value));
        }
        #endregion

        #region GetSqlXml(int)
        /// <summary>Gets the value of the specified column as an XML value.</summary>
        /// <returns>
        /// A <see cref="SqlXml"/> value that contains 
        /// the XML stored within the corresponding field.
        /// </returns>
        /// <param name="ordinal">
        /// The zero-based column ordinal.
        /// </param>
        /// <exception cref="T:System.ArgumentOutOfRangeException">
        /// The index passed was outside the range of 0 to 
        /// FieldCount - 1
        /// </exception>
        /// <exception cref="T:System.InvalidCastException">
        /// The retrieved data is not compatible with the 
        /// <see cref="SqlXml"/> type.
        /// </exception>
        public SqlXml GetSqlXml(int ordinal)
        {
            CheckClosed();
            CheckAvailable();

            object value = m_currentRecord.data[ordinal];

            if (value == null)
            {
                return SqlXml.Null;
            }

            if (value is org.hsqldb.types.JavaObject)
            {
                bool isJavaObject;
                value = HsqlConvert.FromJava.UnWrap(value, out isJavaObject);
            }

            if (value is string)
            {
                string stringValue = value as string;
                MemoryStream ms = new MemoryStream();
                
                using(GZipStream gzs = new GZipStream(ms, CompressionMode.Compress))
                using (TextWriter writer = new StreamWriter(gzs, Encoding.UTF8))
                {
                    writer.Write(stringValue);
                    writer.Flush();
                }

                ms.Position = 0;

                return new SqlXml(new GZipStream(ms, CompressionMode.Decompress));
            }
            else if (value is org.hsqldb.types.Binary)
            {
                byte[] bytes = ((org.hsqldb.types.Binary)value).getBytes();

                return new SqlXml(new MemoryStream(bytes));
            }
            else if (value is byte[])
            {
                return new SqlXml(new MemoryStream(value as byte[]));
            }
            else if (value is char[])
            {
                char[] chars = value as char[];
                MemoryStream ms = new MemoryStream(1 + chars.Length/2);
                
                using(GZipStream gzs = new GZipStream(ms, CompressionMode.Compress))
                using (TextWriter writer = new StreamWriter(gzs, Encoding.UTF8))
                {
                    writer.Write(chars);
                    writer.Flush();
                }

                ms.Position = 0;

                return new SqlXml(new GZipStream(ms, CompressionMode.Decompress));
            }

            throw new InvalidCastException(GetDataTypeName(ordinal),
                HsqlConvert.WrongDataType(value));

        }
        #endregion

        #region GetSqlValue(int)

        /// <summary>
        /// Gets an object instance whose type is selected from
        /// those defined in the <c>System.Data.SqlTypes</c> namespace
        /// to best represent, for the current row in the result set,
        /// the provider-specific type and value of the field with the
        /// given column ordinal
        /// </summary>
        /// <returns>An object instance that is a <c>System.Data.SqlTypes</c>
        /// representation of the underlying provider-specific type and value.
        /// </returns>
        /// <param name="ordinal">
        /// The zero-based column ordinal.
        /// </param>
        public object GetSqlValue(int ordinal)
        {
            CheckClosed();
            CheckAvailable();

            return GetSqlValueInternal(ordinal);
        }

        #endregion

        #region GetSqlValues(object[])

        /// <summary>
        /// At each index corresponding to the column ordinal of a
        /// field in the underlying result set, copies to the given
        /// array an object instance whose type is selected from
        /// those defined in the <c>System.Data.SqlTypes</c> namespace
        /// to best represent the provider-specific type and value of
        /// that field for the current row.
        /// </summary>
        /// <param name="values">
        /// The array into which to copy the <c>System.Data.SqlTypes</c>
        /// representations of the values.
        /// </param>
        /// <returns>
        /// The largest possibly valid offset into the given array, which
        /// is the minimum of <c>values.Length</c> and <c>this.FieldCount</c>.
        /// </returns>
        /// <seealso cref="GetSqlValue(System.Int32)"/>
        public int GetSqlValues(object[] values)
        {
            if (values == null)
            {
                throw new ArgumentNullException(
                    "values");
            }

            CheckClosed();
            CheckAvailable();

            int count = Math.Min(values.Length, FieldCount);

            for (int i = 0; i < count; i++)
            {
                values[i] = GetSqlValueInternal(i);
            }

            return count;
        }

        #endregion

        #endregion
    }

    #endregion
}