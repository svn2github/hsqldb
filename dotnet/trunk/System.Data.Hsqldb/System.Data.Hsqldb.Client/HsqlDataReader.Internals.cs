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

#endregion

namespace System.Data.Hsqldb.Client
{
    #region HsqlDataReader

    public partial class HsqlDataReader : DbDataReader
    {
        #region Fields

        private static readonly int[] m_NoUpdateCounts = new int[0];

        internal org.hsqldb.Result m_result;
        internal CommandBehavior m_commandBehavior;
        internal HsqlCommand m_originatingCommand;
        internal HsqlConnection m_originatingConnection;
        
        private org.hsqldb.Record m_currentRecord;
        private int m_currentRow;
        private int m_recordsAffectedIndex;
        private int m_recordsAffected;
        private int[] m_recordsAffectedCounts;
        private string[] m_stringValues;
        private bool m_isInitialized;
        private int m_fieldCount;
        private bool m_isClosed;
        private int[] m_columnTypes;
        private DataTable m_schemaTable;
        private org.hsqldb.Result.ResultMetaData m_metaData;
        private org.hsqldb.lib.IntValueHashMap m_columnMap;

        #endregion

        #region Internal Methods

        #region BeforeFirst()
        /// <summary>
        /// Resets this reader to before the first row.
        /// </summary>
        internal void BeforeFirst()
        {
            m_isInitialized = false;
            m_currentRecord = null;
            m_stringValues = null;
            m_currentRow = 0;
        }
        #endregion

        #region CheckClosed()
        /// <summary>
        /// Checks if this reader is closed.
        /// </summary>
        internal void CheckClosed()
        {
            if (m_isClosed)
            {
                throw new HsqlDataSourceException("DataReader is closed.",
                    Trace.JDBC_RESULTSET_IS_CLOSED, "S1000"); // NOI18N
            }
        }
        #endregion

        #region CheckAvailable()
        /// <summary>
        /// Checks if row data is available.
        /// </summary>
        internal void CheckAvailable()
        {
            if (!m_isInitialized || 
                (m_currentRecord == null) || 
                (m_result == null))
            {
                throw new HsqlDataSourceException((org.hsqldb.HsqlException)Trace.error(
                    Trace.NO_DATA_IS_AVAILABLE));
            }
        }
        #endregion

        #region DisposeResult()
        /// <summary>
        /// Releases all result row storage, making it elligible for garbage collection.
        /// </summary>
        internal void DisposeResult()
        {
            org.hsqldb.Result r = m_result;

            if (r != null)
            {
                r.clear();
            }

            m_recordsAffectedCounts = m_NoUpdateCounts;
            m_recordsAffectedIndex = 0;
        }
        #endregion

        #region DisposeSchemaTable
        /// <summary>
        /// 
        /// </summary>
        internal void DisposeSchemaTable()
        {
            m_schemaTable = null;
        } 
        #endregion

        #region DisposeColumnMap
        /// <summary>
        /// 
        /// </summary>
        internal void DisposeColumnMap()
        {
            m_columnMap = null;
        } 
        #endregion

        #region GetValueInternal(int)

        /// <summary>
        /// Gets the standard or default representation of the value of the
        /// field with the given column ordinal in the current row of the
        /// result set.
        /// </summary>
        /// <remarks>
        /// Because this method does not check if this reader
        /// is open or if row data is available, it should be invoked
        /// *only* when it is already known that this reader is open and
        /// row data is available.
        /// </remarks>
        /// <param name="ordinal">The zero-based column ordinal.</param>
        /// <returns>The value of the specified column.</returns>
        internal object GetValueInternal(int ordinal)
        {
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

        #region GetSqlValueInternal(int)
        /// <summary>
        /// Gets the <c>System.Data.SqlTypes</c> representation
        /// of the value of the field with the given column ordinal
        /// in the current row of the result set.
        /// </summary>
        /// <remarks>
        /// Because this method does not check if this reader
        /// is open or if row data is available, it should be invoked
        /// *only* when it is already known that this reader is open and
        /// row data is available.
        /// </remarks>
        /// <param name="ordinal">The column ordinal.</param>
        /// <returns>The <c>System.Data.SqlTypes</c> representation</returns>
        internal object GetSqlValueInternal(int ordinal)
        {
            object value = m_currentRecord.data[ordinal];

            bool isNull = (value == null);

            int type = m_metaData.colTypes[ordinal];

            switch (type)
            {
                case org.hsqldb.Types.ARRAY:
                    {
                        throw HsqlConvert.InvalidConversion(type);
                    }
                case org.hsqldb.Types.BIGINT:
                    {
                        return (isNull)
                            ? SqlInt64.Null
                            : new SqlInt64(((java.lang.Long)value).longValue());
                    }
                case org.hsqldb.Types.BINARY:
                    {
                        return (isNull)
                            ? SqlBinary.Null
                            : new SqlBinary(((org.hsqldb.types.Binary)value).getBytes());
                    }
                case org.hsqldb.Types.BLOB:
                    {
                        throw HsqlConvert.InvalidConversion(type);
                    }
                case org.hsqldb.Types.BOOLEAN:
                    {
                        return (isNull)
                            ? SqlBoolean.Null
                            : ((java.lang.Boolean)value).booleanValue()
                                ? SqlBoolean.True
                                : SqlBoolean.False;
                    }
                case org.hsqldb.Types.CHAR:
                    {
                        return (isNull)
                            ? SqlString.Null
                            : new SqlString((string)value);
                    }
                case org.hsqldb.Types.CLOB:
                    {
                        throw HsqlConvert.InvalidConversion(type);
                    }
                case org.hsqldb.Types.DATALINK:
                    {
                        throw HsqlConvert.InvalidConversion(type);
                    }
                case org.hsqldb.Types.DATE:
                    {
                        return (isNull)
                            ? SqlDateTime.Null
                            : new SqlDateTime(
                                HsqlConvert.FromJava.ToDate(((java.sql.Date)value).getTime()));
                    }
                case org.hsqldb.Types.DECIMAL:
                    {
                        return (isNull)
                            ? SqlDecimal.Null
                            : new SqlDecimal(HsqlConvert.FromJava.ToDecimal((java.math.BigDecimal)value));
                    }
                case org.hsqldb.Types.DISTINCT:
                    {
                        throw HsqlConvert.InvalidConversion(type);
                    }
                case org.hsqldb.Types.DOUBLE:
                    {
                        return (isNull)
                            ? SqlDouble.Null
                            : new SqlDouble(((java.lang.Double)value).doubleValue());
                    }
                case org.hsqldb.Types.FLOAT:
                    {
                        return (isNull)
                            ? SqlDouble.Null
                            : new SqlDouble(((java.lang.Double)value).doubleValue());
                    }
                case org.hsqldb.Types.INTEGER:
                    {
                        return (isNull)
                            ? SqlInt32.Null
                            : new SqlInt32(((java.lang.Integer)value).intValue());
                    }
                case org.hsqldb.Types.JAVA_OBJECT:
                    {
                        throw HsqlConvert.InvalidConversion(type);
                    }
                case org.hsqldb.Types.LONGVARBINARY:
                    {
                        return (isNull)
                            ? SqlBinary.Null
                            : new SqlBinary(((org.hsqldb.types.Binary)value).getBytes());
                    }
                case org.hsqldb.Types.LONGVARCHAR:
                    {
                        return (isNull)
                            ? SqlString.Null
                            : new SqlString((string)value);
                    }
                case org.hsqldb.Types.NULL:
                    {
                        throw HsqlConvert.InvalidConversion(type);
                    }
                case org.hsqldb.Types.NUMERIC:
                    {
                        return (isNull)
                            ? SqlDecimal.Null
                            : new SqlDecimal(HsqlConvert.FromJava.ToDecimal((java.math.BigDecimal)value));
                    }
                case org.hsqldb.Types.OTHER:
                    {
                        throw HsqlConvert.InvalidConversion(type);
                    }
                case org.hsqldb.Types.REAL:
                    {
                        return (isNull)
                            ? SqlSingle.Null
                            : new SqlSingle(((java.lang.Double)value).floatValue());
                    }
                case org.hsqldb.Types.REF:
                    {
                        throw HsqlConvert.InvalidConversion(type);
                    }
                case org.hsqldb.Types.SMALLINT:
                    {
                        return (isNull)
                            ? SqlInt16.Null
                            : new SqlInt16(((java.lang.Integer)value).shortValue());
                    }
                case org.hsqldb.Types.STRUCT:
                    {
                        throw HsqlConvert.InvalidConversion(type);
                    }
                case org.hsqldb.Types.TIME:
                    {
                        return (isNull)
                            ? SqlDateTime.Null
                            : new SqlDateTime(
                                HsqlConvert.FromJava.ToTime(((java.sql.Time)value).getTime()));
                    }
                case org.hsqldb.Types.TIMESTAMP:
                    {
                        return (isNull)
                            ? SqlDateTime.Null
                            : new SqlDateTime(
                                HsqlConvert.FromJava.ToTimestamp(((java.sql.Timestamp)value).getTime()));
                    }
                case org.hsqldb.Types.TINYINT:
                    {
                        return (isNull)
                            ? SqlInt16.Null
                            : new SqlInt16(((java.lang.Integer)value).shortValue());
                    }
                case org.hsqldb.Types.VARBINARY:
                    {
                        return (isNull)
                            ? SqlBinary.Null
                            : new SqlBinary(((org.hsqldb.types.Binary)value).getBytes());
                    }
                case org.hsqldb.Types.VARCHAR:
                    {
                        return (isNull)
                            ? SqlString.Null
                            : new SqlString((string)value);
                    }
                case org.hsqldb.Types.VARCHAR_IGNORECASE:
                    {
                        return (isNull)
                            ? SqlString.Null
                            : new SqlString((string)value);
                    }
                case org.hsqldb.Types.XML:
                    {
                        throw HsqlConvert.InvalidConversion(type);
                    }
                default:
                    {
                        throw HsqlConvert.InvalidConversion(type);
                    }
            }
        }
        #endregion

        #region GetProviderSpecificValueInternal(int)

        /// <summary>
        /// For the current row, gets an instance of the provider-specific
        /// representation of the value of the field with the given column
        /// ordinal.
        /// </summary>
        /// <remarks>
        /// Because this method does not check if this reader
        /// is open or if row data is available, it should be invoked
        /// *only* when it is already known that this reader is open and
        /// row data is available.
        /// </remarks>        
        /// <param name="ordinal">The ordinal.</param>
        /// <returns>The provider-specific value representation.</returns>
        internal object GetProviderSpecificValueInternal(int ordinal)
        {
            object value = m_currentRecord.data[ordinal];

            if (value == null)
            {
                return null;
            }

            int type = m_metaData.colTypes[ordinal];

            switch (type)
            {
                case (int)HsqlProviderType.Array:
                    {
                        return value;
                    }
                case (int)HsqlProviderType.BigInt:
                    {
                        return value;
                    }
                case (int)HsqlProviderType.Binary:
                    {
                        return ((org.hsqldb.types.Binary)value).getClonedBytes();
                    }
                case (int)HsqlProviderType.Blob:
                    {
                        return value;
                    }
                case (int)HsqlProviderType.Boolean:
                    {
                        return value;
                    }
                case (int)HsqlProviderType.Char:
                    {
                        return value;
                    }
                case (int)HsqlProviderType.Clob:
                    {
                        return value;
                    }
                case (int)HsqlProviderType.DataLink:
                    {
                        return value;
                    }
                case (int)HsqlProviderType.Date:
                    {
                        return (java.sql.Date)((java.sql.Date)value).clone();
                    }
                case (int)HsqlProviderType.Decimal:
                    {
                        return value;
                    }
                case (int)HsqlProviderType.Distinct:
                    {
                        return value;
                    }
                case (int)HsqlProviderType.Double:
                    {
                        return value;
                    }
                case (int)HsqlProviderType.Float:
                    {
                        return value;
                    }
                case (int)HsqlProviderType.Integer:
                    {
                        return value;
                    }
                case (int)HsqlProviderType.JavaObject:
                    {
                        return value;
                    }
                case (int)HsqlProviderType.LongVarBinary:
                    {
                        return ((org.hsqldb.types.Binary)value).getClonedBytes();
                    }
                case (int)HsqlProviderType.LongVarChar:
                    {
                        return value;
                    }
                case (int)HsqlProviderType.Null:
                    {
                        return null;
                    }
                case (int)HsqlProviderType.Numeric:
                    {
                        return value;
                    }
                case (int)HsqlProviderType.Object:
                    {
                        byte[] bytes = ((org.hsqldb.types.JavaObject)value).getBytes();
                        bool dummy;

                        return SqlObject.Deserialize(bytes, out dummy);
                    }
                case (int)HsqlProviderType.Real:
                    {
                        return value;
                    }
                case (int)HsqlProviderType.Ref:
                    {
                        return value;
                    }
                case (int)HsqlProviderType.SmallInt:
                    {
                        return value;
                    }
                case (int)HsqlProviderType.Struct:
                    {
                        return value;
                    }
                case (int)HsqlProviderType.Time:
                    {
                        return (java.sql.Time)((java.sql.Time)value).clone();
                    }
                case (int)HsqlProviderType.TimeStamp:
                    {
                        return (java.sql.Timestamp)((java.sql.Timestamp)value).clone();
                    }
                case (int)HsqlProviderType.TinyInt:
                    {
                        return value;
                    }
                case (int)HsqlProviderType.VarBinary:
                    {
                        byte[] bytes = ((org.hsqldb.types.Binary)value).getClonedBytes();

                        return new org.hsqldb.types.Binary(bytes, false);
                    }
                case (int)HsqlProviderType.VarChar:
                    {
                        return value;
                    }
                case (int)HsqlProviderType.Xml:
                    {
                        return value;
                    }
                default:
                    {
                        throw HsqlConvert.UnknownConversion(value, type);
                    }
            }
        }

        #endregion
        
        #endregion

        #region Internal Properties

        #region OriginatingCommand
        /// <summary>
        /// Gets the command whose execution produced this reader.
        /// </summary>
        /// <value>The command.</value>
        internal HsqlCommand OriginatingCommand
        {
            get { return m_originatingCommand; }
        }
        #endregion

        #region OriginatingConnection
        /// <summary>
        /// Gets the orginating connection (to close).
        /// </summary>
        /// <remarks>
        /// This is the connection that was associated with
        /// this reader's command at the time of execution.
        /// We need this because the command may be assigned
        /// a different connection at a later time, and we
        /// would not want to inadvertently close that connection.
        /// </remarks>
        /// <value>The originating connection (to close).</value>
        internal HsqlConnection OriginatingConnection
        {
            get { return m_originatingConnection; }
        }
        #endregion

        #endregion
    }

    #endregion
}