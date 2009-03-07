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
using System.Data.Hsqldb.Common.Sql.Types;
using System.Data.SqlTypes;
using System.IO;
using System.Runtime.Serialization;
using System.Security;
using System.Text;
using System.Xml;

using Trace = org.hsqldb.Trace;
using System.ComponentModel;
using System.Data.Hsqldb.Common;

#endregion

namespace System.Data.Hsqldb.Client
{
    #region HsqlDataReader

    public partial class HsqlDataReader : DbDataReader
    {
        #region IDataReader Members

        #region Method Overrides

        #region Close()
        /// <summary>
        /// Closes this data reader.
        /// </summary>
        public override void Close()
        {
            if (!m_isClosed)
            {
                m_isClosed = true;

                // free up any resoures dedicated to representing row data 
                DisposeResult();
                //DisposeColumnMap();
                //DisposeSchemaTable();
                BeforeFirst();

                // TODO: check interaction with System.Transaction enlistment
                if (HasCommandBehavior(CommandBehavior.CloseConnection))
                {
                    DbConnection connection = m_originatingConnection;

                    if (connection != null)
                    {
                        try { connection.Close(); }
                        catch (Exception) 
                        { 
                            // CHECKME:  rethrow or swallow?
                        }
                    }
                }

                m_originatingCommand = null;
                m_originatingConnection = null;
            }
        }
        #endregion

        #region GetSchemaTable()
        /// <summary>
        /// Returns a <c>DataTable</c> that describes the column
        /// metadata of the current result.
        /// </summary>
        /// <returns>
        /// A <c>DataTable</c> that describes the column metadata
        /// of the current result.
        /// </returns>
        public override DataTable GetSchemaTable()
        {
            DataTable schemaTable;
            WeakReference stRef = m_schemaTableReference;

            if (!stRef.IsAlive || null == (schemaTable = (DataTable) stRef.Target))
            {
                CheckClosed();

                schemaTable = HsqlResultSetMetaData.CreateSchemaTable(this);

                stRef.Target = schemaTable;
            }

            return schemaTable;
        }
        #endregion

        #region NextResult()
        /// <summary>
        /// Advances this reader to the next result when reading the
        /// results of a batch of statements.
        /// </summary>
        /// <returns>
        /// <c>true</c> if there are more results;
        /// otherwise <c>false</c>.
        /// </returns>
        /// <remarks>
        /// This method allows you to process multiple results
        /// returned when a batch is submitted to the
        /// data provider.
        /// </remarks>
        public override bool NextResult()
        {
            CheckClosed();

            if (m_recordsAffectedCounts == null
                || m_recordsAffectedIndex >= m_recordsAffectedCounts.Length)
            {
                return false;
            }

            m_recordsAffectedIndex++;

            m_recordsAffected = m_recordsAffectedCounts[m_recordsAffectedIndex];

            return true;
        }
        #endregion

        #region Read()
        /// <summary>
        /// Advances this reader to the next record in the current result set.
        /// </summary>
        /// <returns>
        /// <c>true</c> if there are more rows; otherwise <c>false</c>.
        /// </returns>
        /// <remarks>
        /// The default position of a data reader is before the first
        /// record. Therefore, one must call <c>Read</c> to begin accessing
        /// data.
        /// </remarks>
        public override bool Read()
        {
            // clear any data cached to accelerate GetChars(...) 
            // access against the previous row.
            m_stringValues = null;

            // no (more) records; exit with false
            if (m_result == null || m_result.isEmpty())
            {
                return false;
            }

            if (!m_isInitialized)
            {
                // before first;
                // set cursor to first row (1)
                m_currentRecord = m_result.rRoot;
                m_isInitialized = true;
                m_currentRow = 1;
            }
            else
            {
                // result has been fully traversed; return false
                if (m_currentRecord == null)
                {
                    return false;
                }

                // On a valid row; traverse next
                m_currentRecord = m_currentRecord.next;

                m_currentRow++;
            }

            // test if after last
            if (m_currentRecord == null)
            {
                // after last; exit with false
                m_currentRow = m_result.getSize() + 1;

                DisposeResult();

                return false;
            }
            else
            {
                // on a valid row
                return true;
            }
        }
        #endregion        

        #endregion

        #region Property Overrides

        #region Depth
        /// <summary>
        /// Gets a value indicating the depth of nesting for the result.
        /// </summary>
        /// <value>
        /// The depth of nesting for the current row.
        /// </value>
        public override int Depth
        {
            get { CheckClosed(); return 0; }
        }
        #endregion

        #region IsClosed
        /// <summary>
        /// Gets a value indicating whether this reader is closed.
        /// </summary>
        /// <value>
        /// <c>true</c> if this reader is closed; otherwise <c>false</c>.
        /// </value>
        public override bool IsClosed
        {
            get { return m_isClosed; }
        }
        #endregion

        #region RecordsAffected
        /// <summary>
        /// Gets the number of rows changed, inserted, or deleted
        /// by execution of the SQL statement.
        /// </summary>
        /// <value>
        /// The number of rows changed, inserted, or deleted.
        /// -1 for SELECT statements; 
        /// 0 if no rows were affected or the statement failed.
        /// </value>
        public override int RecordsAffected
        {
            get { CheckClosed(); return m_recordsAffected; }
        }
        #endregion
        
        #endregion

        #endregion

        #region IDataRecord Members

        #region Method Overrides

        #region GetBoolean(int)
        /// <summary>
        /// Gets the value of the specified column as a Boolean value.
        /// </summary>
        /// <param name="ordinal">The zero-based column ordinal.</param>
        /// <returns>The value of the specified column.</returns>
        public override bool GetBoolean(int ordinal)
        {
            CheckClosed();
            CheckAvailable();

            object value = m_currentRecord.data[ordinal];

            if (value == null)
            {
                throw new NullReferenceException();
            }

            switch (m_metaData.colTypes[ordinal])
            {
                case (int)HsqlProviderType.TinyInt:
                case (int)HsqlProviderType.SmallInt:
                case (int)HsqlProviderType.Integer:
                    {
                        return ((java.lang.Integer)value).intValue() != 0;
                    }
                case (int)HsqlProviderType.BigInt:
                    {
                        return ((java.lang.Long)value).longValue() != 0L;
                    }
                case (int)HsqlProviderType.Float:
                case (int)HsqlProviderType.Real:
                case (int)HsqlProviderType.Double:
                    {
                        return ((java.lang.Double)value).doubleValue() != 0.0D;
                    }
                case (int)HsqlProviderType.Boolean:
                    {
                        return ((java.lang.Boolean)value).booleanValue();
                    }
                default:
                    {
                        return HsqlConvert.FromJava.ToBoolean(value);
                    }
            }
        }
        #endregion

        #region GetByte(int)
        /// <summary>
        /// Gets the value of the specified column as a byte.
        /// </summary>
        /// <param name="ordinal">The zero-based column ordinal.</param>
        /// <returns>The value of the specified column.</returns>
        public override byte GetByte(int ordinal)
        {
            CheckClosed();
            CheckAvailable();

            object value = m_currentRecord.data[ordinal];

            if (value == null)
            {
                throw new NullReferenceException();
            }

            checked
            {
                return (byte)HsqlConvert.FromJava.ToSmallInt(value);
            }
        }
        #endregion

        #region GetBytes(int)
        /// <summary>
        /// Reads a stream of bytes from the specified column,
        /// starting at location indicated by dataIndex, into the buffer,
        /// starting at the location indicated by bufferIndex.
        /// </summary>
        /// <param name="ordinal">
        /// The zero-based column ordinal.
        /// </param>
        /// <param name="dataOffset">
        /// The index within the row from which to begin the read operation.
        /// </param>
        /// <param name="buffer">
        /// The buffer into which to copy the data.
        /// </param>
        /// <param name="bufferOffset">
        /// The index with the buffer to which the data will be copied.
        /// </param>
        /// <param name="length">
        /// The maximum number of characters to read.
        /// </param>
        /// <returns>
        /// The actual number of bytes read.
        /// </returns>
        public override long GetBytes(
            int ordinal,
            long dataOffset,
            byte[] buffer,
            int bufferOffset,
            int length)
        {
            if (0 > dataOffset || dataOffset > int.MaxValue)
            {
                throw new ArgumentException(
                    "dataOffset value out of range: " + dataOffset,
                    "dataOffset");
            }

            CheckClosed();
            CheckAvailable();

            object value = m_currentRecord.data[ordinal];

            if (value == null)
            {
                throw new NullReferenceException();
            }

            byte[] bytes = HsqlConvert.FromJava.ToBinary(value);

            int buflen = buffer.Length;
            int bytlen = bytes.Length;
            int dataofs = (int)dataOffset;
            int count = 0;

            for (; count < length; count++)
            {
                int bufofs = bufferOffset + count;
                int bytofs = dataofs + count;

                if (bufofs >= buflen || bytofs >= bytlen)
                {
                    break;
                }

                buffer[bufofs] = bytes[bytofs];
            }

            return count;
        }
        #endregion

        #region GetChar(int)

        /// <summary>
        /// Gets the value of the specified column as a single character.
        /// </summary>
        /// <param name="ordinal">The zero-based column ordinal.</param>
        /// <returns>The value of the specified column.</returns>
        public override char GetChar(int ordinal)
        {
            CheckClosed();
            CheckAvailable();

            object value = m_currentRecord.data[ordinal];

            if (value == null)
            {
                throw new NullReferenceException();
            }

            org.hsqldb.types.JavaObject wrapper
                = value as org.hsqldb.types.JavaObject;

            bool isJavaObject = true;

            if (wrapper != null)
            {
                value = SqlObject.Deserialize(wrapper.getBytes(), out isJavaObject);
            }

            java.lang.Number n = isJavaObject
                ? (value as java.lang.Number)
                : null;

            if (n != null)
            {
                int i = HsqlConvert.FromJava.ToInteger(n);

                if (char.MinValue > i || i > char.MinValue)
                {
                    throw HsqlConvert.NumericValueOutOfRange(n);
                }

                return (char)i;
            }

            java.lang.Boolean b = isJavaObject
                ? (value as java.lang.Boolean)
                : null;

            if (b != null)
            {
                return b.booleanValue() ? '1' : '0';
            }

            byte[] bytes = value as byte[];

            if (bytes == null)
            {

                org.hsqldb.types.Binary bval = value as org.hsqldb.types.Binary;

                if (bval != null)
                {
                    bytes = bval.getBytes();
                }

            }

            if (bytes != null)
            {
                switch (bytes.Length)
                {
                    case 1:
                        {
                            return (char)bytes[0];
                        }
                    case 2:
                        {
                            int i = (bytes[1] << 8) & (bytes[0] << 0);

                            return (char)i;
                        }
                    default:
                        {
                            throw HsqlConvert
                                .InvalidConversion(m_columnTypes[ordinal]);
                        }
                }
            }

            if (value is java.util.Date)
            {
                throw HsqlConvert.InvalidConversion(
                    m_columnTypes[ordinal]);
            }

            string stringValue = value as string;

            if (stringValue == null)
            {
                stringValue = HsqlConvert.FromJava.ToString(value);
            }

            if (stringValue.Length < 1)
            {
                throw new InvalidCastException(
                    "from zero-length character sequence.");
            }

            return stringValue[0];
        }

        #endregion

        #region GetChars(int)

        /// <summary>
        /// Reads a stream of characters from the specified column,
        /// starting at location indicated by dataIndex, into the buffer,
        /// starting at the location indicated by bufferIndex.
        /// </summary>
        /// <param name="ordinal">
        /// The zero-based column ordinal.
        /// </param>
        /// <param name="dataOffset">
        /// The index within the row from which
        /// to begin the read operation.</param>
        /// <param name="buffer">
        /// The buffer into which to copy the data.
        /// </param>
        /// <param name="bufferOffset">
        /// The index within the buffer to which the data will be copied.
        /// </param>
        /// <param name="length">
        /// The maximum number of characters to read.
        /// </param>
        /// <returns>
        /// The actual number of characters read.
        /// </returns>
        /// <exception cref="ArgumentNullException">
        /// When <c>buffer</c> is null.
        /// </exception>
        /// <exception cref="ArgumentException">
        /// When the given values of <c>dataOffset</c>, <c>bufferOffset</c>
        /// or <c>length</c> would result in an out of range index while accessing
        /// either <c>buffer</c> or the column value.
        /// </exception>
        /// <exception cref="NullReferenceException">
        /// When the specified column value is <c>null</c>.
        /// </exception>
        /// <exception cref="IndexOutOfRangeException">
        /// When the specified <c>ordinal</c> is less than Zero (<c>0</c>)
        /// or greater than or equal to <see cref="FieldCount">FieldCount</see>.
        /// </exception>
        /// <exception cref="HsqlDataSourceException">
        /// When the value of the specified column cannot be represented
        /// as a character sequence.
        /// </exception>
        public override long GetChars(
            int ordinal,
            long dataOffset,
            char[] buffer,
            int bufferOffset,
            int length)
        {
            if (0 > dataOffset || dataOffset > int.MaxValue)
            {
                throw new ArgumentException(
                    "Range violation: " + dataOffset,
                    "dataOffset");
            }
            else if (buffer == null)
            {
                throw new ArgumentNullException("buffer");
            }
            else if (0 > bufferOffset)
            {
                throw new ArgumentException(
                    "Range violation: " + bufferOffset,
                    "bufferOffset");
            }
            else if (0 > length)
            {
                throw new ArgumentException(
                    "Range violation: " + length,
                    "length");
            }
            else if (buffer.Length - bufferOffset < length)
            {
                throw new ArgumentException(
                    "buffer.Length - bufferOffset < length");
            }

            CheckClosed();
            CheckAvailable();

            object value = m_currentRecord.data[ordinal];

            if (value == null)
            {
                throw new NullReferenceException("Column value");
            }

            string str = value as string;

            if (str == null)
            {
                if (m_stringValues != null)
                {
                    str = (string)m_stringValues[ordinal];
                }

                if (str == null)
                {
                    str = HsqlConvert.FromJava.ToString(value);

                    if (m_stringValues == null)
                    {
                        m_stringValues = new string[m_fieldCount];
                    }

                    m_stringValues[ordinal] = str;
                }
            }

            int strLen = str.Length;

            // this is OK, as we've already checked
            // that it is between 0 and int.MaxValue.
            int start = (int)dataOffset;

            if (start >= strLen)
            {
                return 0;
            }

            length = Math.Min(length, buffer.Length - bufferOffset);
            length = Math.Min(length, strLen - start);

            str.CopyTo(start, buffer, bufferOffset, length);

            return length;
        }

        #endregion

        #region GetDataTypeName(int)

        /// <summary>
        /// Gets name of the SQL data type of the specified column.
        /// </summary>
        /// <param name="ordinal">The zero-based column ordinal.</param>
        /// <returns>
        /// A string representing the name of the SQL data type.
        /// </returns>
        /// <exception cref="IndexOutOfRangeException">
        /// When the specified <c>ordinal</c> is less than Zero (<c>0</c>)
        /// or greater than or equal to <see cref="FieldCount">FieldCount</see>.
        /// </exception>
        public override string GetDataTypeName(int ordinal)
        {
            return HsqlConvert.ToSqlDataTypeName(m_metaData.colTypes[ordinal]);
        }

        #endregion

        #region GetDateTime(int)

        /// <summary>
        /// Gets the value of the specified column
        /// as a <see cref="DateTime"/> object.
        /// </summary>
        /// <param name="ordinal">The zero-based column ordinal.</param>
        /// <returns>The value of the specified column.</returns>
        /// <exception cref="NullReferenceException">
        /// When the value of the specified column is <c>SQL NULL</c>.
        /// </exception>
        /// <exception cref="IndexOutOfRangeException">
        /// When the specified <c>ordinal</c> is less than Zero (<c>0</c>)
        /// or greater than or equal to <see cref="FieldCount">FieldCount</see>.
        /// </exception>
        /// <exception cref="HsqlDataSourceException">
        /// When the value of the specified column cannot be represented
        /// as a a <see cref="DateTime"/> object.
        /// </exception>
        public override DateTime GetDateTime(int ordinal)
        {
            CheckClosed();
            CheckAvailable();

            object value = m_currentRecord.data[ordinal];

            if (value == null)
            {
                throw new NullReferenceException();
            }

            return HsqlConvert.FromJava.ToTimestamp(value);
        }

        #endregion

        #region GetDecimal(int)

        /// <summary>
        /// Gets the value of the specified column as a
        /// <see cref="Decimal"/> object.
        /// </summary>
        /// <param name="ordinal">The zero-based column ordinal.</param>
        /// <returns>The value of the specified column.</returns>
        /// <exception cref="NullReferenceException">
        /// When the value of the specified column is <c>SQL NULL</c>.
        /// </exception>
        /// <exception cref="IndexOutOfRangeException">
        /// When the specified <c>ordinal</c> is less than Zero (<c>0</c>)
        /// or greater than or equal to <see cref="FieldCount">FieldCount</see>.
        /// </exception>
        /// <exception cref="HsqlDataSourceException">
        /// When the value of the specified column cannot be represented
        /// as a <see cref="Decimal"/> object.
        /// </exception>
        public override decimal GetDecimal(int ordinal)
        {
            CheckClosed();
            CheckAvailable();

            object value = m_currentRecord.data[ordinal];

            if (value == null)
            {
                throw new NullReferenceException();
            }

            return HsqlConvert.FromJava.ToDecimal(value);
        }

        #endregion

        #region GetDouble(int)

        /// <summary>
        /// Gets the value of the specified column as a
        /// double-precision floating point number.
        /// </summary>
        /// <param name="ordinal">The zero-based column ordinal.</param>
        /// <returns>The value of the specified column.</returns>
        /// <exception cref="NullReferenceException">
        /// When the value of the specified column is <c>SQL NULL</c>.
        /// </exception>
        /// <exception cref="IndexOutOfRangeException">
        /// When the specified <c>ordinal</c> is less than Zero (<c>0</c>)
        /// or greater than or equal to <see cref="FieldCount">FieldCount</see>.
        /// </exception>
        /// <exception cref="HsqlDataSourceException">
        /// When the value of the specified column cannot be represented
        /// as a double-precision floating point number.
        /// </exception>
        public override double GetDouble(int ordinal)
        {
            CheckClosed();
            CheckAvailable();

            object value = m_currentRecord.data[ordinal];

            if (value == null)
            {
                throw new NullReferenceException();
            }

            java.lang.Double doubleValue = value as java.lang.Double;

            return (doubleValue == null)
                       ? HsqlConvert.FromJava.ToDouble(value)
                       : doubleValue.doubleValue();
        }

        #endregion

        #region GetFieldType(int)

        /// <summary>
        /// Gets the <c>System.Type</c> that is the standard type
        /// used to represent to a client the values of the field
        /// with the specified column ordinal.
        /// </summary>
        /// <param name="ordinal">
        /// The zero-based column ordinal for which to retrieve 
        /// the corresponding field type.
        /// </param>
        /// <returns>
        /// The field type corresponding to the specified column ordinal.
        /// </returns>
        /// <exception cref="IndexOutOfRangeException">
        /// When the specified <c>ordinal</c> is less than Zero (<c>0</c>)
        /// or greater than or equal to <see cref="FieldCount">FieldCount</see>.
        /// </exception>
        public override Type GetFieldType(int ordinal)
        {
            return HsqlConvert.ToDataType(m_columnTypes[ordinal]);
        }

        #endregion

        #region GetFloat(int)

        /// <summary>
        /// Gets the value of the specified column as a single-precision
        /// floating point number.
        /// </summary>
        /// <param name="ordinal">The zero-based column ordinal.</param>
        /// <returns>The value of the specified column.</returns>
        /// <exception cref="NullReferenceException">
        /// When the value of the specified column is <c>SQL NULL</c>.
        /// </exception>
        /// <exception cref="IndexOutOfRangeException">
        /// When the specified <c>ordinal</c> is less than Zero (<c>0</c>)
        /// or greater than or equal to <see cref="FieldCount">FieldCount</see>.
        /// </exception>
        /// <exception cref="HsqlDataSourceException">
        /// When the value of the specified column cannot be represented
        /// as a single-precision floating point number.
        /// </exception>
        public override float GetFloat(int ordinal)
        {
            CheckClosed();
            CheckAvailable();

            object value = m_currentRecord.data[ordinal];

            if (value == null)
            {
                throw new NullReferenceException();
            }

            return HsqlConvert.FromJava.ToReal(value);
        }

        #endregion

        #region GetGuid(int)

        /// <summary>
        /// Gets the value of the specified column as a globally-unique
        /// identifier (<c>GUID</c>).
        /// </summary>
        /// <param name="ordinal">The zero-based column ordinal.</param>
        /// <returns>The value of the specified column.</returns>
        /// <exception cref="NullReferenceException">
        /// When the value of the specified column is <c>SQL NULL</c>.
        /// </exception>
        /// <exception cref="IndexOutOfRangeException">
        /// When the specified <c>ordinal</c> is less than Zero (<c>0</c>)
        /// or greater than or equal to <see cref="FieldCount">FieldCount</see>.
        /// </exception>
        /// <exception cref="HsqlDataSourceException">
        /// When the value of the specified column cannot be represented
        /// as a globally-unique identifier (<c>GUID</c>).
        /// </exception>
        public override Guid GetGuid(int ordinal)
        {
            CheckClosed();
            CheckAvailable();

            object value = m_currentRecord.data[ordinal];

            if (value == null)
            {
                throw new NullReferenceException();
            }

            // TODO:  simplify and consider migrating to HsqlConvert.FromJava

            org.hsqldb.types.Binary binaryValue
                = value as org.hsqldb.types.Binary;

            if (binaryValue != null)
            {
                try
                {
                    return new Guid(binaryValue.getBytes());
                }
                catch (Exception ex)
                {
                    throw new HsqlDataSourceException(HsqlConvert
                        .InvalidConversion(m_columnTypes[ordinal])
                        .Message, ex);
                }
            }

            string stringValue = value as string;

            if (stringValue != null)
            {
                try
                {
                    return new Guid(stringValue);
                }
                catch (Exception ex)
                {
                    throw new HsqlDataSourceException(
                        HsqlConvert
                        .InvalidConversion(m_columnTypes[ordinal])
                        .Message,
                        ex);
                }
            }

            org.hsqldb.types.JavaObject javaObjectValue
                = value as org.hsqldb.types.JavaObject;


            if (javaObjectValue != null)
            {
                bool isJavaObject;

                try
                {
                    value = SqlObject
                        .Deserialize(javaObjectValue.getBytes(),
                        out isJavaObject);
                }
                catch (Exception ex)
                {
                    throw new HsqlDataSourceException(
                        HsqlConvert
                        .InvalidConversion(m_columnTypes[ordinal])
                        .Message,
                        ex);
                }

                if (!isJavaObject)
                {
                    if (value is Guid)
                    {
                        return (Guid)value;
                    }
                    else if (value is byte[])
                    {
                        try
                        {
                            return new Guid((byte[])value);
                        }
                        catch (Exception ex)
                        {
                            throw new HsqlDataSourceException(
                                HsqlConvert
                                .InvalidConversion(m_columnTypes[ordinal])
                                .Message,
                                ex);
                        }
                    }
                    else if (value is string)
                    {
                        try
                        {
                            return new Guid((string)value);
                        }
                        catch (Exception ex)
                        {
                            throw new HsqlDataSourceException(
                                HsqlConvert
                                .InvalidConversion(m_columnTypes[ordinal])
                                .Message,
                                ex);
                        }
                    }
                    else
                    {
                        byte[] bytes = HsqlConvert
                            .FromDotNet
                            .ToBinary(value)
                            .getBytes();

                        try
                        {
                            return new Guid((byte[])value);
                        }
                        catch (Exception ex)
                        {
                            throw new HsqlDataSourceException(
                                HsqlConvert
                                .InvalidConversion(m_columnTypes[ordinal])
                                .Message,
                                ex);
                        }
                    }
                }
                else
                {
                    java.util.UUID uuid = value as java.util.UUID;

                    if (uuid != null)
                    {
                        return HsqlConvert.FromJava.ToGuid(uuid);
                    }

                    if (value is byte[])
                    {
                        try
                        {
                            return new Guid((byte[])value);
                        }
                        catch (Exception ex)
                        {
                            throw new HsqlDataSourceException(
                                HsqlConvert
                                .InvalidConversion(m_columnTypes[ordinal])
                                .Message,
                                ex);
                        }
                    }
                    else if (value is string)
                    {
                        try
                        {
                            return new Guid((string)value);
                        }
                        catch (Exception ex)
                        {
                            throw new HsqlDataSourceException(
                                HsqlConvert
                                .InvalidConversion(m_columnTypes[ordinal])
                                .Message,
                                ex);
                        }
                    }
                    else
                    {
                        byte[] bytes = HsqlConvert.FromJava.ToBinary(value);

                        try
                        {
                            return new Guid((byte[])value);
                        }
                        catch (Exception ex)
                        {
                            throw new HsqlDataSourceException(
                                HsqlConvert.InvalidConversion(
                                m_columnTypes[ordinal]).Message, ex);
                        }
                    }
                }
            }
            else
            {
                byte[] bytes = HsqlConvert.FromJava.ToBinary(value);

                try
                {
                    return new Guid((byte[])value);
                }
                catch (Exception ex)
                {
                    throw new HsqlDataSourceException(HsqlConvert
                        .InvalidConversion(m_columnTypes[ordinal])
                        .Message, ex);
                }
            }
        }

        #endregion

        #region GetInt16(int)

        /// <summary>
        /// Gets the value of the specified column as a
        /// 16-bit signed integer.
        /// </summary>
        /// <param name="ordinal">The zero-based column ordinal.</param>
        /// <returns>The value of the specified column.</returns>
        /// <exception cref="NullReferenceException">
        /// When the value of the specified column is <c>SQL NULL</c>.
        /// </exception>
        /// <exception cref="IndexOutOfRangeException">
        /// When the specified <c>ordinal</c> is less than Zero (<c>0</c>)
        /// or greater than or equal to <see cref="FieldCount">FieldCount</see>.
        /// </exception>
        /// <exception cref="HsqlDataSourceException">
        /// When the value of the specified column cannot be represented
        /// as a 16-bit signed integer.
        /// </exception>
        /// <exception cref="System.OverflowException">
        /// When the magnitude of the value of the specified column causes
        /// an arithmetic overflow to occur while casting the value to a 16-bit
        /// signed integer.
        /// </exception>
        public override short GetInt16(int ordinal)
        {
            CheckClosed();
            CheckAvailable();

            object value = m_currentRecord.data[ordinal];

            if (value == null)
            {
                throw new NullReferenceException();
            }

            switch (m_metaData.colTypes[ordinal])
            {
                case (int)HsqlProviderType.TinyInt:
                case (int)HsqlProviderType.SmallInt:
                    {
                        return ((java.lang.Integer)value).shortValue();
                    }
                case (int)HsqlProviderType.Integer:
                    {
                        checked
                        {
                            return (short)((java.lang.Integer)value).intValue();
                        }
                    }
                case (int)HsqlProviderType.BigInt:
                    {
                        checked
                        {
                            return (short)((java.lang.Long)value).longValue();
                        }
                    }
                case (int)HsqlProviderType.Float:
                case (int)HsqlProviderType.Real:
                case (int)HsqlProviderType.Double:
                    {
                        checked
                        {
                            return (short)((java.lang.Double)value).doubleValue();
                        }
                    }
                case (int)HsqlProviderType.Boolean:
                    {
                        return (short)(((java.lang.Boolean)value)
                            .booleanValue() ? 1 : 0);
                    }
                default:
                    {
                        return (short)HsqlConvert
                            .FromJava
                            .ToSmallInt(value);
                    }
            }
        }

        #endregion

        #region GetInt32(int)

        /// <summary>
        /// Gets the value of the specified column as a 32-bit signed integer.
        /// </summary>
        /// <param name="ordinal">The zero-based column ordinal.</param>
        /// <returns>The value of the specified column.</returns>
        /// <exception cref="NullReferenceException">
        /// When the value of the specified column is <c>SQL NULL</c>.
        /// </exception>
        /// <exception cref="IndexOutOfRangeException">
        /// When the specified <c>ordinal</c> is less than Zero (<c>0</c>)
        /// or greater than or equal to <see cref="FieldCount">FieldCount</see>.
        /// </exception>
        /// <exception cref="HsqlDataSourceException">
        /// When the value of the specified column cannot be represented
        /// as a 32-bit signed integer.
        /// </exception>
        /// <exception cref="System.OverflowException">
        /// When the magnitude of the value of the specified column causes
        /// an arithmetic overflow to occur while casting the value to a 32-bit
        /// signed integer.
        /// </exception>
        public override int GetInt32(int ordinal)
        {
            CheckClosed();
            CheckAvailable();

            object value = m_currentRecord.data[ordinal];

            if (value == null)
            {
                throw new NullReferenceException();
            }

            switch (m_metaData.colTypes[ordinal])
            {
                case (int)HsqlProviderType.TinyInt:
                case (int)HsqlProviderType.SmallInt:
                case (int)HsqlProviderType.Integer:
                    {
                        return ((java.lang.Integer)value).intValue();
                    }
                case (int)HsqlProviderType.BigInt:
                    {
                        checked
                        {
                            return (int)((java.lang.Long)value).longValue();
                        }
                    }
                case (int)HsqlProviderType.Float:
                case (int)HsqlProviderType.Real:
                case (int)HsqlProviderType.Double:
                    {
                        checked
                        {
                            return (int)((java.lang.Double)value).doubleValue();
                        }
                    }
                case (int)HsqlProviderType.Boolean:
                    {
                        return ((java.lang.Boolean)value).booleanValue() ? 1 : 0;
                    }
                default:
                    {
                        return HsqlConvert.FromJava.ToInteger(value);
                    }
            }
        }

        #endregion

        #region GetInt64(int)

        /// <summary>
        /// Gets the value of the specified column as a 64-bit signed integer.
        /// </summary>
        /// <param name="ordinal">The zero-based column ordinal.</param>
        /// <returns>The value of the specified column.</returns>
        /// <exception cref="NullReferenceException">
        /// When the value of the specified column is <c>SQL NULL</c>.
        /// </exception>
        /// <exception cref="IndexOutOfRangeException">
        /// When the specified <c>ordinal</c> is less than Zero (<c>0</c>)
        /// or greater than or equal to <see cref="FieldCount">FieldCount</see>.
        /// </exception>
        /// <exception cref="HsqlDataSourceException">
        /// When the value of the specified column cannot be represented
        /// as a 64-bit signed integer.
        /// </exception>
        /// <exception cref="System.OverflowException">
        /// When the magnitude of the value of the specified column causes
        /// an arithmetic overflow to occur while casting the value to a 64-bit
        /// signed integer.
        /// </exception>
        public override long GetInt64(int ordinal)
        {
            CheckClosed();
            CheckAvailable();

            object value = m_currentRecord.data[ordinal];

            if (value == null)
            {
                throw new NullReferenceException();
            }

            switch (m_metaData.colTypes[ordinal])
            {
                case (int)HsqlProviderType.TinyInt:
                case (int)HsqlProviderType.SmallInt:
                case (int)HsqlProviderType.Integer:
                case (int)HsqlProviderType.BigInt:
                    {
                        return ((java.lang.Number)value).longValue();
                    }
                case (int)HsqlProviderType.Float:
                case (int)HsqlProviderType.Real:
                case (int)HsqlProviderType.Double:
                    {
                        checked
                        {
                            return (long)((java.lang.Number)value).doubleValue();
                        }
                    }
                case (int)HsqlProviderType.Boolean:
                    {
                        return ((java.lang.Boolean)value).booleanValue() ? 1L : 0L;
                    }
                default:
                    {
                        return HsqlConvert.FromJava.ToBigInt(value);
                    }
            }
        }

        #endregion

        #region GetName(int)

        /// <summary>
        /// Gets the simple name of the column, given the
        /// zero-based column ordinal.
        /// </summary>
        /// <remarks>
        /// The simple name of the column is determined in the
        /// following order of precedence:
        /// <list type="">
        /// <item>
        /// The label (alias) specified in the generating query.
        /// </item>
        /// <item>
        /// When no label is specified, then the name of the
        /// underlying column, if there is one, in its containing
        /// table or view. This also applies to simple aggregate
        /// function expressions, so that the simple column name of
        /// 'SUM(COL_NAME)' without a declared SQL alias will be
        /// reported as 'COL_NAME', rather than 'SUM(COL_NAME)'.
        /// </item>
        /// <item>
        /// When no label is specified and there is no single
        /// underlying column, then the empty String.
        /// </item>
        /// </list>
        /// </remarks>
        /// <param name="ordinal">The zero-based column ordinal.</param>
        /// <returns>The simple name of the specified column.</returns>
        /// <seealso cref="GetOrdinal(System.String)"/>
        public override string GetName(int ordinal)
        {
            return m_metaData.colLabels[ordinal];
        }

        #endregion

        #region GetOrdinal(string)

        /// <summary>
        /// Gets the column ordinal, given the name of the column.
        /// </summary>
        /// <remarks>
        /// This method employs a search that is case-insensitive, proceeds
        /// from left to right and uses the following matching precedence:
        /// <list type="">
        /// <item>column label (SQL 'AS' ALIAS)</item>
        /// <item>'simple column name' (in base table or view)</item>
        /// <item>'simple-table-name.simple-column-name' (non-delimited form only)</item>
        /// <item>'schema.simple-table-name.simple-column-name' (non-delimited form only)</item>
        /// </list>
        /// </remarks>
        /// <param name="name">
        /// The character sequence for which to search
        /// </param>
        /// <returns>
        /// The zero-based ordinal of the first encountered column
        /// satisfying the search.
        /// </returns>
        /// <exception cref="ArgumentNullException">
        /// When a <c>null</c> name is specified.
        /// </exception>        
        /// <exception cref="IndexOutOfRangeException">
        /// When the search concludes without finding a match.
        /// </exception>
        public override int GetOrdinal(string name)
        {
            if (name == null)
            {
                throw new ArgumentNullException("name");
            }

            int columnIndex;

            // faster lookup for subsequent access
            if (m_columnMap != null)
            {
                columnIndex = m_columnMap.get(name, -1);

                if (columnIndex >= 0)
                {
                    return columnIndex;
                }
            }

            string[] colLabels = m_metaData.colLabels;

            columnIndex = -1;
            StringComparison comparisonType
                = StringComparison.InvariantCultureIgnoreCase;

            // column labels first, to preference column aliases
            for (int i = 0; i < m_fieldCount; i++)
            {
                if (name.Equals(colLabels[i], comparisonType))
                {
                    columnIndex = i;

                    break;
                }
            }

            string[] colNames = m_metaData.colNames;

            // then base column names, to preference simple
            // quoted column idents that *may* contain "."
            if (columnIndex < 0)
            {
                for (int i = 0; i < m_fieldCount; i++)
                {
                    if (name.Equals(colNames[i], comparisonType))
                    {
                        columnIndex = i;

                        break;
                    }
                }
            }

            string[] tabNames = m_metaData.tableNames;

            // then table-qualified column names (again, quoted
            // table idents *may* contain "."
            if (columnIndex < 0)
            {
                for (int i = 0; i < m_fieldCount; i++)
                {
                    string tabName = tabNames[i];

                    if (string.IsNullOrEmpty(tabName))
                    {
                        continue;
                    }

                    string colName = colNames[i];

                    if (string.IsNullOrEmpty(colName))
                    {
                        continue;
                    }

                    if (name.Equals(tabName + "." + colName, comparisonType))
                    {
                        columnIndex = i;

                        break;
                    }
                }
            }

            string[] schemNames = m_metaData.schemaNames;

            // As a last resort, "fully" qualified column names
            // (we don't yet support catalog qualification)
            if (columnIndex < 0)
            {
                for (int i = 0; i < m_fieldCount; i++)
                {
                    string schemName = schemNames[i];

                    if (string.IsNullOrEmpty(schemName))
                    {
                        continue;
                    }

                    string tabName = tabNames[i];

                    if (string.IsNullOrEmpty(tabName))
                    {
                        continue;
                    }

                    string colName = colNames[i];

                    if (string.IsNullOrEmpty(colName))
                    {
                        continue;
                    }

                    string match = new StringBuilder(schemName)
                        .Append('.')
                        .Append(tabName)
                        .Append('.')
                        .Append(colName)
                        .ToString();

                    if (name.Equals(match, comparisonType))
                    {
                        columnIndex = i;

                        break;
                    }
                }
            }

            if (columnIndex < 0)
            {
                org.hsqldb.HsqlException ex
                    = org.hsqldb.Trace.error(
                        org.hsqldb.Trace.COLUMN_NOT_FOUND,
                        name);

                throw new IndexOutOfRangeException(ex.getMessage(), ex);
            }

            if (m_columnMap == null)
            {
                m_columnMap = new org.hsqldb.lib.IntValueHashMap();
            }

            m_columnMap.put(name, columnIndex);

            return columnIndex;
        }

        #endregion

        #region GetString(int)

        /// <summary>
        /// Gets the value of the specified column as an
        /// instance of <see cref="String"/>.
        /// </summary>
        /// <param name="ordinal">The zero-based column ordinal.</param>
        /// <returns>The value of the specified column.</returns>
        public override string GetString(int ordinal)
        {
            CheckClosed();
            CheckAvailable();

            object value = m_currentRecord.data[ordinal];

            if (value == null)
            {
                return null;
            }

            string stringValue = value as string;

            return (stringValue == null) ? HsqlConvert.FromJava.ToString(value)
                                         : stringValue;
        }

        #endregion

        #region GetValue(int)

        /// <summary>
        /// Gets an object instance representing the value of the field
        /// with the specified column ordinal for the current row in the
        /// result set.
        /// </summary>
        /// <param name="ordinal">The zero-based column ordinal.</param>
        /// <returns>The value corresponding to the specified column ordinal.</returns>
        public override object GetValue(int ordinal)
        {
            CheckClosed();
            CheckAvailable();

            object value = m_currentRecord.data[ordinal];

            if (value == null)
            {
                return null;
            }
            else if (value is string)
            {
                return value;
            }

            return HsqlConvert.FromJava.ToObject(value, m_columnTypes[ordinal]);
        }

        #endregion

        #region GetValues(object[])

        /// <summary>
        /// At each index corresponding to the column ordinal of a
        /// field in the underlying result set, copies to the given
        /// array an object instance that is the standard representation
        /// of that field's value for the current row.
        /// </summary>
        /// <param name="values">An array into which to copy the values.</param>
        /// <returns>
        /// The largest possibly valid offset into the given array, which
        /// is the minimum of <c>values.Length</c> and <c>this.FieldCount</c>.
        /// </returns>
        public override int GetValues(object[] values)
        {
            CheckClosed();
            CheckAvailable();

            if (values == null)
            {
                return 0;
            }

            int valueCount = Math.Min(values.Length, m_fieldCount);
            object[] data = m_currentRecord.data;
            int[] types = m_metaData.colTypes;

            // inlined for performance

            for (int count = 0; count < valueCount; count++)
            {
                object value = data[count];

                if (value == null)
                {
                    values[count] = null;
                    continue;
                }

                int type = types[count];

                switch (type)
                {
                    case (int)HsqlProviderType.Char:
                    case (int)HsqlProviderType.VarChar:
                    case (int)HsqlProviderType.LongVarChar:
                        {
                            values[count] = value;

                            break;
                        }
                    case (int)HsqlProviderType.BigInt:
                        {
                            values[count] = ((java.lang.Long)value).longValue();

                            break;
                        }
                    case (int)HsqlProviderType.Boolean:
                        {
                            values[count] = ((java.lang.Boolean)value)
                                .booleanValue();

                            break;
                        }
                    case (int)HsqlProviderType.Date:
                        {
                            long javaTimeInMillis = ((java.sql.Date)value)
                                .getTime();
                            values[count] = HsqlConvert.FromJava.ToDate(
                                javaTimeInMillis);

                            break;
                        }
                    case (int)HsqlProviderType.Decimal:
                    case (int)HsqlProviderType.Numeric:
                        {
                            values[count] = HsqlConvert.FromJava.ToDecimal(((
                                java.math.BigDecimal)value));

                            break;
                        }
                    case (int)HsqlProviderType.Double:
                    case (int)HsqlProviderType.Float:
                        {
                            values[count] = ((java.lang.Double)value)
                                .doubleValue();

                            break;
                        }
                    case (int)HsqlProviderType.Real:
                        {
                            values[count] = ((java.lang.Double)value)
                                .floatValue();

                            break;
                        }
                    case (int)HsqlProviderType.Integer:
                        {
                            values[count] = ((java.lang.Integer)value)
                                .intValue();

                            break;
                        }
                    case (int)HsqlProviderType.SmallInt:
                        {
                            values[count] = (short) ((java.lang.Integer)value)
                                .intValue();

                            break;
                        }
                    case (int)HsqlProviderType.TinyInt:
                        {
                            values[count] = (sbyte) ((java.lang.Integer)value)
                                .intValue();

                            break;
                        }
                    case (int)HsqlProviderType.Binary:
                    case (int)HsqlProviderType.VarBinary:
                    case (int)HsqlProviderType.LongVarBinary:
                        {
                            values[count] = ((org.hsqldb.types.Binary)value)
                                .getClonedBytes();

                            break;
                        }
                    case (int)HsqlProviderType.Null:
                        {
                            // throw? 
                            values[count] = null;

                            break;
                        }
                    case (int)HsqlProviderType.Time:
                        {
                            long javaTimeInMillis = ((java.sql.Time)value)
                                .getTime();
                            values[count] = HsqlConvert.FromJava.ToTime(
                                javaTimeInMillis);

                            break;
                        }
                    case (int)HsqlProviderType.TimeStamp:
                        {
                            long javaTimeInMillis
                                = ((java.sql.Timestamp)value).getTime();
                            values[count]
                                = HsqlConvert.FromJava.ToTimestamp(javaTimeInMillis);

                            break;
                        }
                    case (int)HsqlProviderType.Object:
                        {
                            org.hsqldb.types.JavaObject javaObject = value
                                as org.hsqldb.types.JavaObject;

                            if (javaObject == null)
                            {
                                throw HsqlConvert.WrongDataType(value);
                            }

                            bool dummy;
                            byte[] serialForm = javaObject.getBytes();

                            values[count] = SqlObject.Deserialize(serialForm, out dummy);

                            break;
                        }
                    default:
                        {
                            values[count] = HsqlConvert.FromJava.ToObject(
                                value, type);

                            break;
                        }
                }
            }

            return valueCount;
        }

        #endregion

        #region IsDbNull(int)

        /// <summary>
        /// Gets a value that indicates whether the column contains
        /// nonexistent or missing values.
        /// </summary>
        /// <param name="ordinal">The zero-based column ordinal.</param>
        /// <returns>
        /// <c>true</c> if the specified column is equivalent
        /// to <c>DBNull</c>; otherwise <c>false</c>.
        /// </returns>
        /// <remarks>
        /// Call this method to check for null column values before calling
        /// the typed get methods (for example, GetByte,
        /// GetChar, and so on) to avoid raising an error.
        /// </remarks>
        public override bool IsDBNull(int ordinal)
        {
            CheckClosed();
            CheckAvailable();

            return (m_currentRecord.data[ordinal] == null);
        }

        #endregion
        
        #endregion

        #region Property Overrides

        #region FieldCount

        /// <summary>
        /// Gets the number of columns in the current row.
        /// </summary>
        /// <value>
        /// The number of columns in the current row.
        /// </value>
        public override int FieldCount
        {
            get { CheckClosed(); return m_fieldCount; }
        }

        #endregion

        #region this[int]

        /// <summary>
        /// Gets an object instance representing the value in the current row
        /// of the field with the specified column ordinal.
        /// </summary>
        /// <param name="ordinal">The zero-based column ordinal.</param>
        /// <value>The value corresponding to the specified column ordinal.</value>
        public override object this[int ordinal]
        {
            get { return GetValue(ordinal); }
        }

        #endregion

        #region this[string]

        /// <summary>
        /// Gets an object instance representing the value in the current row
        /// of the field with the column ordinal corresponding to the specified name.
        /// </summary>
        /// <remarks>
        /// See <see cref="GetOrdinal(string)"/> and <see cref="GetValue(int)"/>.
        /// </remarks>
        /// <param name="name">The name of the column.</param>
        /// <returns>The value of the specified column.</returns>
        public override object this[string name]
        {
            get { return GetValue(GetOrdinal(name)); }
        }

        #endregion

        #endregion

        #endregion

        #region DbDataReader Members

        #region Method Overrides

        #region GetData(int)
        /// <summary>
        /// Returns an <see cref="HsqlDataReader"/> object for the requested
        /// column ordinal.
        /// </summary>
        /// <returns>
        /// an <see cref="HsqlDataReader"/> object.
        /// </returns>
        /// <param name="ordinal">
        /// The zero-based column ordinal.
        /// </param>
        [EditorBrowsable(EditorBrowsableState.Never)]
        new public HsqlDataReader GetData(int ordinal)
        {
            CheckClosed();
            CheckAvailable();

            object value = m_currentRecord.data[ordinal];

            if (value == null)
            {
                throw new NullReferenceException();
            }

            if (value is org.hsqldb.types.JavaObject)
            {
                bool isJavaObject;

                value = HsqlConvert.FromJava.UnWrap(value, out isJavaObject);
            }

            byte[] bytes = value as byte[];

            if (bytes == null && value is org.hsqldb.types.Binary)
            {
                bytes = ((org.hsqldb.types.Binary)value).getBytes();
            }

            if (bytes == null)
            {
                throw HsqlConvert.WrongDataType(value);
            }

            using (MemoryStream stream = new MemoryStream(bytes, false))
            {
                org.hsqldb.Result result = HsqlDataReader.ReadResult(stream);

                return new HsqlDataReader(result, CommandBehavior.Default,
                    m_originatingCommand, m_originatingConnection);
            }
        } 
        #endregion

        #region GetDbDataReader(int)
        /// <summary>
        /// Returns a <see cref="DbDataReader"/> object for the requested
        /// column ordinal.
        /// </summary>
        /// <param name="ordinal">The zero-based column ordinal.</param>
        /// <returns>
        /// A <see cref="DbDataReader"/> object.
        /// </returns>
        protected override DbDataReader GetDbDataReader(int ordinal)
        {
            return GetData(ordinal);
        }
        #endregion

        #region GetEnumerator()

        /// <summary>
        /// Returns an <see cref="IEnumerator"/> that can be used to iterate
        /// through the rows in this data reader.
        /// </summary>
        /// <remarks>
        /// Note that the <c>System.Type</c> of the returned object's
        /// <see cref="IEnumerator.Current"/> property is 
        /// <see cref="System.Data.Common.DbDataRecord"/>.
        /// </remarks>
        /// <returns>
        /// An <see cref="IEnumerator"/> that can be used to iterate through
        /// the rows in this data reader as instances of 
        /// <see cref="System.Data.Common.DbDataRecord"/>.
        /// </returns>
        public override IEnumerator GetEnumerator()
        {
            return new DbEnumerator(this, /*closeReader*/this.HasCommandBehavior(
                CommandBehavior.CloseConnection));
        }

        #endregion

        #region GetProviderSpecificFieldType(int)

        /// <summary>
        /// Returns the provider-specific <c>System.Type</c> used to
        /// represent values of the field with the specified column
        /// ordinal.
        /// </summary>
        /// <remarks>
        /// For the sake of portability, it is usually best practice to avoid
        /// retrieving values with provider-specific type. However, there are
        /// some situations that may require it, and other situations where it
        /// may be far more performant to do so. For instance, because the
        /// allowable precision and scale of <c>SQL DECIMAL</c> and
        /// <c>NUMERIC</c> values exeeds that of <c>System.Decimal</c>, it may
        /// be required to retrieve instances of the provider-specific
        /// <c>java.math.BigDecimal</c>, so that invalid conversion exceptions
        /// do not occur.  Similarly, when processing a large result set
        /// within a client application, it may be required to achieve an
        /// optimally low response time and/or optimally low memory footprint.
        /// In either case, the loss of portability may well be overshadowed
        /// by the possibility to eliminate the memory and CPU overhead
        /// associated with conversion to a standard type representation from
        /// the provider-specific type normally reserved for internal
        /// representation of result set field values.
        /// </remarks>
        /// <param name="ordinal">
        /// The zero-based column ordinal for which to retrieve the corresponding
        /// provider-specific type used to represent the values of the field.
        /// </param>
        /// <returns>
        /// The provider-specific type user to represent values of the field
        /// corresponding to the given column ordinal.
        /// </returns>
        public override Type GetProviderSpecificFieldType(int ordinal)
        {
            return HsqlConvert.ToProviderSpecificDataType(m_columnTypes[
                ordinal]);
        }

        #endregion

        #region GetProviderSpecificValue(int)
        /// <summary>
        /// Gets an object instance that is a representation
        /// of the underlying provider-specific value for the
        /// current row and given column ordinal.
        /// </summary>
        /// <returns>
        /// A representation of the underlying provider-specific
        /// value.
        /// </returns>
        /// <param name="ordinal">
        /// An <see cref="Int32"/> denoting the zero-based offset of
        /// the desired column.
        /// </param>
        public override object GetProviderSpecificValue(int ordinal)
        {
            CheckClosed();
            CheckAvailable();

            return GetProviderSpecificValueInternal(ordinal);
        }
        #endregion

        #region GetProviderSpecificValues(object[])

        /// <summary>
        /// At each index corresponding to the column ordinal of a
        /// field in the underlying result set, copies to the given
        /// array an object instance that is a representation of
        /// the provider-specific value of that field for the
        /// current row.
        /// </summary>
        /// <param name="values">
        /// The array into which to copy the provider-specific
        /// representation of the values.
        /// </param>
        /// <returns>
        /// The largest possibly valid offset into the given array, which
        /// is the minimum of <c>values.Length</c> and <c>this.FieldCount</c>.
        /// </returns>
        public override int GetProviderSpecificValues(object[] values)
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
                values[i] = GetProviderSpecificValueInternal(i);
            }

            return count;
        }

        #endregion

        #endregion

        #region Property Overrides

        #region HasRows

        /// <summary>
        /// Gets a value that indicates whether this
        /// reader contains one or more rows.
        /// </summary>
        /// <value>
        /// <c>true</c> if this reader contains one or more rows;
        /// otherwise <c>false</c>.
        /// </value>
        public override bool HasRows
        {
            get
            {
                org.hsqldb.Result result = m_result;

                bool hasRows = (result == null || !result.isData() || result.isEmpty()) 
                    ? false 
                    : (m_isInitialized) ? (m_currentRow <= result.getSize()) : true;

                return hasRows;
            }
        }

        #endregion

        #endregion

        #endregion
    }

    #endregion
}