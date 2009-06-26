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

using BinaryFormatter = System.Runtime.Serialization.Formatters.Binary.BinaryFormatter;
using Stream = System.IO.Stream;
using MemoryStream = System.IO.MemoryStream;
using StreamReader = System.IO.StreamReader;
using TextReader = System.IO.TextReader;
using ArgumentException = System.ArgumentException;
using HsqlException = org.hsqldb.HsqlException;
using HsqlProviderType = System.Data.Hsqldb.Common.Enumeration.HsqlProviderType;
using HsqlIsolationLevel = System.Data.Hsqldb.Common.Enumeration.HsqlIsolationLevel;
using ParameterMode = System.Data.Hsqldb.Common.Enumeration.ParameterMode;
using ParameterDirection = System.Data.ParameterDirection;
using IsolationLevel = System.Data.IsolationLevel;
using DbType = System.Data.DbType;
using Trace = org.hsqldb.Trace;
using Types = org.hsqldb.Types;

using JavaBigDecimal = java.math.BigDecimal;
using JavaBigInteger = java.math.BigInteger;
using JavaByte = java.lang.Byte;
using JavaBoolean = java.lang.Boolean;
using JavaCalendar = java.util.Calendar;
using JavaDouble = java.lang.Double;
using JavaShort = java.lang.Short;
using JavaInteger = java.lang.Integer;
using JavaLong = java.lang.Long;
using JavaNumber = java.lang.Number;
using JavaFloat = java.lang.Float;
using JavaTime = java.sql.Time;
using JavaTimestamp = java.sql.Timestamp;


using StringConverter = org.hsqldb.lib.StringConverter;
using ValuePool = org.hsqldb.store.ValuePool;
using Binary = org.hsqldb.types.Binary;
using JavaObject = org.hsqldb.types.JavaObject;
using TypeCode = System.TypeCode;
using IConvertible = System.IConvertible;
using Convert = System.Convert;
using Type = System.Type;
using IBlob = System.Data.Hsqldb.Common.Lob.IBlob;
using IClob = System.Data.Hsqldb.Common.Lob.IClob;

using DateTime = System.DateTime;
using ISerializable = System.Runtime.Serialization.ISerializable;
using Serializable = java.io.Serializable;
using TimeSpan = System.TimeSpan;
using Array = System.Array;

using DotNetCalendar = System.Globalization.GregorianCalendar;
using JavaGregorianCalendar = java.util.GregorianCalendar;
using Library = org.hsqldb.Library;
using HsqlDateTime = org.hsqldb.HsqlDateTime;

using System.Text;
using System.Data.Hsqldb.Common.Sql.Types;
using System.Data.SqlTypes;

#endregion

namespace System.Data.Hsqldb.Common
{
    public static partial class HsqlConvert
    {
        #region FromDotNet
        /// <summary>
        /// Provides conversions from the .NET type system to the HSQLDB Java internal type mapping for SQL.
        /// </summary>
        /// <remarks>
        /// This facility is intented primarily 
        /// </remarks>
        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Design", "CA1034:NestedTypesShouldNotBeVisible")]
        public static class FromDotNet
        {
            #region ToBoolean

            #region ToBoolean(bool)
            /// <summary>
            /// Converts the given <c>System.Boolean</c>
            /// value to an <c>SQL BOOLEAN</c> value.
            /// </summary>
            /// <param name="boolValue">
            /// To convert to a <c>java.lang.Boolean</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Boolean</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaBoolean ToBoolean(bool boolValue)
            {
                return boolValue ? TRUE : FALSE;
            }
            #endregion

            #region ToBoolean(string)
            /// <summary>
            /// Converts the given <c>System.String</c>
            /// value to an <c>SQL BOOLEAN</c> value.
            /// </summary>
            /// <remarks>
            /// Converts "TRUE" to <c>java.lang.Boolean.TRUE</c>
            /// using case-insensitive comparison; converts other
            /// values to <c>java.lang.Boolean.FALSE</c>.
            /// </remarks>
            /// <param name="stringValue">
            /// To convert to a <c>java.lang.Boolean</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Boolean</c> value.
            /// </returns>
            /// <exception cref="NullReferenceException">
            /// When <c>stringValue</c> is <c>null</c>.
            /// </exception>
            [CLSCompliant(false)]
            public static JavaBoolean ToBoolean(string stringValue)
            {
                if (stringValue == null)
                {
                    throw new NullReferenceException("stringValue");
                }

                return (stringValue.Length == 4 && stringValue.Equals("TRUE", 
                    IgnoreCase)) ? JavaBoolean.TRUE : JavaBoolean.FALSE;
            }
            #endregion

            #region ToBoolean(byte)
            /// <summary>
            /// Converts the given <c>System.Byte</c>
            /// value to an <c>SQL BOOLEAN</c> value.
            /// </summary>
            /// <param name="byteValue">
            /// To convert to a <c>java.lang.Boolean</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Boolean</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaBoolean ToBoolean(byte byteValue)
            {
                return (byteValue == 0) ? FALSE : TRUE;
            }
            #endregion

            #region ToBoolean(sbyte)
            /// <summary>
            /// Converts the given <c>System.SByte</c>
            /// value to an <c>SQL BOOLEAN</c> value.
            /// </summary>
            /// <param name="sbyteValue">
            /// To convert to a <c>java.lang.Boolean</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Boolean</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaBoolean ToBoolean(sbyte sbyteValue)
            {
                return (sbyteValue == 0) ? FALSE : TRUE;
            }
            #endregion

            #region ToBoolean(char)
            /// <summary>
            /// Converts the given <c>System.Char</c>
            /// value to an <c>SQL BOOLEAN</c> value.
            /// </summary>
            /// <param name="charValue">
            /// To convert to a <c>java.lang.Boolean</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Boolean</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaBoolean ToBoolean(char charValue)
            {
                return (charValue == 0) ? FALSE : TRUE;
            }
            #endregion

            #region ToBoolean(short)
            /// <summary>
            /// Converts the given <c>System.Int16</c>
            /// value to an <c>SQL BOOLEAN</c> value.
            /// </summary>
            /// <param name="shortValue">
            /// To convert to a <c>java.lang.Boolean</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Boolean</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaBoolean ToBoolean(short shortValue)
            {
                return (shortValue == 0) ? FALSE : TRUE;
            }
            #endregion

            #region ToBoolean(ushort)
            /// <summary>
            /// Converts the given <c>System.UInt16</c>
            /// value to an <c>SQL BOOLEAN</c> value.
            /// </summary>
            /// <param name="ushortValue">
            /// To convert to a <c>java.lang.Boolean</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Boolean</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaBoolean ToBoolean(ushort ushortValue)
            {
                return (ushortValue == 0) ? FALSE : TRUE;
            }
            #endregion

            #region ToBoolean(int)
            /// <summary>
            /// Converts the given <c>System.Int32</c>
            /// value to an <c>SQL BOOLEAN</c> value.
            /// </summary>
            /// <param name="intValue">
            /// To convert to a <c>java.lang.Boolean</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Boolean</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaBoolean ToBoolean(int intValue)
            {
                return (intValue == 0) ? FALSE : TRUE;
            }
            #endregion

            #region ToBoolean(INullable)
            /// <summary>
            /// Converts the given <c>INullable</c>
            /// value to an <c>SQL BOOLEAN</c> value.
            /// </summary>
            /// <param name="nullable">
            /// To convert to a <c>java.lang.Boolean</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Boolean</c> value.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the concrete type of the given <c>INullable</c> is not handled
            /// </exception>
            [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Maintainability", "CA1502:AvoidExcessiveComplexity")]
            [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Performance", "CA1800:DoNotCastUnnecessarily")]
            [CLSCompliant(false)]
            public static JavaBoolean ToBoolean(INullable nullable)
            {
                if (nullable == null || nullable.IsNull)
                {
                    return null;
                }
                //if (nullable is SqlBinary)
                //{
                //    throw HsqlConvert.WrongDataType(nullable);
                //}
                else if (nullable is SqlBoolean)
                {
                    return ((SqlBoolean)nullable).Value
                        ? TRUE
                        : FALSE;
                }
                else if (nullable is SqlByte)
                {
                    return (((SqlByte)nullable).Value == 0)
                        ? FALSE
                        : TRUE;
                }
                //else if (nullable is SqlBytes)
                //{
                //    throw HsqlConvert.WrongDataType(nullable);
                //}
                else if (nullable is SqlChars)
                {
                    SqlChars chars = (SqlChars)nullable;

                    return ((chars.Length == 5)
                    && "TRUE".Equals(new string(chars.Value), IgnoreCase))
                           ? TRUE
                           : FALSE;
                }
                //else if (nullable is SqlDateTime)
                //{
                //    throw HsqlConvert.WrongDataType(nullable);
                //}
                else if (nullable is SqlDecimal)
                {
                    return (((SqlDecimal)nullable).Value == 0M)
                        ? FALSE
                        : TRUE;
                }
                else if (nullable is SqlDouble)
                {
                    return (((SqlDouble)nullable).Value == 0D)
                        ? FALSE
                        : TRUE;
                }
                else if (nullable is SqlGuid)
                {
                    return (((SqlGuid)nullable).Value == Guid.Empty)
                        ? FALSE
                        : TRUE;
                }
                else if (nullable is SqlInt16)
                {
                    return (((SqlInt16)nullable).Value == 0)
                        ? FALSE
                        : TRUE;
                }
                else if (nullable is SqlInt32)
                {
                    return (((SqlInt32)nullable).Value == 0)
                        ? FALSE
                        : TRUE;
                }
                else if (nullable is SqlInt64)
                {
                    return (((SqlInt64)nullable).Value == 0L)
                        ? FALSE
                        : TRUE;
                }
                else if (nullable is SqlMoney)
                {
                    return (((SqlMoney)nullable).Value == 0M)
                        ? FALSE
                        : TRUE;
                }
                else if (nullable is SqlSingle)
                {
                    return (((SqlSingle)nullable).Value == 0F)
                        ? FALSE
                        : TRUE;
                }
                else if (nullable is SqlString)
                {
                    return "TRUE".Equals(((SqlString)nullable).Value, IgnoreCase)
                        ? TRUE
                        : FALSE;
                }
                //else if (nullable is SqlXml)
                //{
                //    throw HsqlConvert.WrongDataType(nullable);
                //}

                throw HsqlConvert.WrongDataType(nullable);

            }
            #endregion

            #region ToBoolean(uint)
            /// <summary>
            /// Converts the given <c>System.UInt32</c>
            /// value to an <c>SQL BOOLEAN</c> value.
            /// </summary>
            /// <param name="uintValue">
            /// To convert to a <c>java.lang.Boolean</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Boolean</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaBoolean ToBoolean(uint uintValue)
            {
                return (uintValue == 0) ? FALSE : TRUE;
            }
            #endregion

            #region ToBoolean(long)
            /// <summary>
            /// Converts the given <c>System.Int64</c>
            /// value to an <c>SQL BOOLEAN</c>
            /// </summary>
            /// <param name="longValue">
            ///To convert to a <c>java.lang.Boolean</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Boolean</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaBoolean ToBoolean(long longValue)
            {
                return (longValue == 0L) ? FALSE : TRUE;
            }
            #endregion

            #region ToBoolean(ulong)
            /// <summary>
            /// Converts the given <c>System.UInt64</c>
            /// value to an <c>SQL BOOLEAN</c> value.
            /// </summary>
            /// <param name="ulongValue">
            ///  To convert to a <c>java.lang.Boolean</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Boolean</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaBoolean ToBoolean(ulong ulongValue)
            {
                return (ulongValue == 0L) ? FALSE : TRUE;
            }
            #endregion

            #region ToBoolean(float)
            /// <summary>
            /// Converts the given <see cref="System.Single"/>
            /// value to an <c>SQL BOOLEAN</c> value.
            /// </summary>
            /// <param name="floatValue">
            /// To convert to a <c>java.lang.Boolean</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Boolean</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaBoolean ToBoolean(float floatValue)
            {
                return (floatValue == 0.0F) ? FALSE : TRUE;
            }
            #endregion

            #region ToBoolean(double)
            /// <summary>
            /// Converts the given <see cref="System.Double"/>
            /// value to an <c>SQL BOOLEAN</c> value.
            /// </summary>
            /// <param name="doubleValue">
            /// To convert to a <c>java.lang.Boolean</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Boolean</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaBoolean ToBoolean(double doubleValue)
            {
                return (doubleValue == 0.0D) ? FALSE : TRUE;
            }
            #endregion

            #region ToBoolean(decimal)
            /// <summary>
            /// Converts the given <see cref="System.Decimal"/>
            /// value to an <c>SQL BOOLEAN</c> value.
            /// </summary>
            /// <param name="decimalValue">
            /// To convert to a <c>java.lang.Boolean</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Boolean</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaBoolean ToBoolean(decimal decimalValue)
            {
                return (decimalValue == 0.0M) ? FALSE : TRUE;
            }
            #endregion

            #region ToBoolean(object)
            /// <summary>
            /// Converts the given .NET <see cref="System.Object"/>
            /// value to an <c>SQL BOOLEAN</c> value.
            /// </summary>
            /// <remarks>
            /// This method is not fully generalized; it is intended
            /// for conversions proceeding from the .NET type system to
            /// the HSQLDB type system, specifically from boxed
            /// <c>System.ValueType</c> instances and character sequences.
            /// For instance, passing an <c>java.lang.Boolean</c>
            /// value will result in an invalid conversion exception,
            /// rather than the identity conversion.  This is in the
            /// interest of keeping this method as short as possible
            /// for its intended purpose.
            /// </remarks>
            /// <param name="objectValue">
            /// To convert to a <c>java.lang.Boolean</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Boolean</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaBoolean ToBoolean(object objectValue)
            {
                if (objectValue == null)
                {
                    return null;
                }

                TypeCode typeCode = Type.GetTypeCode(objectValue.GetType());

                switch (typeCode)
                {
                    case TypeCode.Boolean:
                        {
                            return ToBoolean((bool)objectValue);
                        }
                    case TypeCode.Byte:
                        {
                            return ToBoolean((byte)objectValue);
                        }
                    case TypeCode.Char:
                        {
                            return ToBoolean((char)objectValue);
                        }
                    case TypeCode.DateTime:
                        {
                            throw InvalidConversion(Types.TIMESTAMP);
                        }
                    case TypeCode.DBNull:
                        {
                            return null;
                        }
                    case TypeCode.Decimal:
                        {
                            return ToBoolean((decimal)objectValue);
                        }
                    case TypeCode.Double:
                        {
                            return ToBoolean((double)objectValue);
                        }
                    case TypeCode.Empty:
                        {
                            return null;
                        }
                    case TypeCode.Int16:
                        {
                            return ToBoolean((short)objectValue);
                        }
                    case TypeCode.Int32:
                        {
                            return ToBoolean((int)objectValue);
                        }
                    case TypeCode.Int64:
                        {
                            return ToBoolean((long)objectValue);
                        }
                    case TypeCode.Object:
                        {
                            IConvertible convertible = objectValue as IConvertible;

                            if (convertible != null)
                            {
                                return ToBoolean(Convert.ToBoolean(convertible));
                            }

                            if (objectValue is INullable)
                            {
                                return ToBoolean(((INullable)objectValue));
                            }

                            throw HsqlConvert.InvalidConversion(Types.OTHER);
                        }
                    case TypeCode.SByte:
                        {
                            return ToBoolean((sbyte)objectValue);
                        }
                    case TypeCode.Single:
                        {
                            return ToBoolean((float)objectValue);
                        }
                    case TypeCode.String:
                        {
                            return ToBoolean((string)objectValue);
                        }
                    case TypeCode.UInt16:
                        {
                            return ToBoolean((ushort)objectValue);
                        }
                    case TypeCode.UInt32:
                        {
                            return ToBoolean((uint)objectValue);
                        }
                    case TypeCode.UInt64:
                        {
                            return ToBoolean((ulong)objectValue);
                        }
                }

                throw WrongDataType(objectValue);
            }
            #endregion

            #endregion

            #region ToBigInt

            #region ToBigInt(bool)
            /// <summary>
            /// Converts the given <see cref="System.Boolean"/> value
            /// to an <c>SQL BIGINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="boolValue">
            /// To convert to a <c>java.lang.Long</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Long</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaLong ToBigInt(bool boolValue)
            {
                return ValuePool.getLong(boolValue ? 1L : 0L);
            }
            #endregion

            #region ToBigInt(byte)
            /// <summary>
            /// Converts the given <see cref="System.Byte"/> value
            /// to an <c>SQL BIGINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="byteValue">
            /// To convert to a <c>java.lang.Long</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Long</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaLong ToBigInt(byte byteValue)
            {
                return ValuePool.getLong(byteValue);
            }
            #endregion

            #region ToBigInt(sbyte)
            /// <summary>
            /// Converts the given <see cref="System.SByte"/> value
            /// to an <c>SQL BIGINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="sbyteValue">
            /// To convert to a <c>java.lang.Long</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Long</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaLong ToBigInt(sbyte sbyteValue)
            {
                return ValuePool.getLong(sbyteValue);
            }
            #endregion

            #region ToBigInt(short)
            /// <summary>
            /// Converts the given <see cref="System.Int16"/> value
            /// to an <c>SQL BIGINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="shortValue">
            /// To convert to a <c>java.lang.Long</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Long</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaLong ToBigInt(short shortValue)
            {
                return ValuePool.getLong(shortValue);
            }
            #endregion

            #region ToBigInt(ushort)
            /// <summary>
            /// Converts the given <see cref="System.UInt16"/> value
            /// to an <c>SQL BIGINT</c> value.
            /// </summary>
            /// <param name="ushortValue">
            /// To convert to a <c>java.lang.Long</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Long</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaLong ToBigInt(ushort ushortValue)
            {
                return ValuePool.getLong(ushortValue);
            }
            #endregion

            #region ToBigInt(int)
            /// <summary>
            /// Converts the given <see cref="System.Int32"/> value
            /// to an <c>SQL BIGINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="intValue">
            /// To convert to a <c>java.lang.Long</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Long</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaLong ToBigInt(int intValue)
            {
                return ValuePool.getLong(intValue);
            }
            #endregion

            #region ToBigInt(INullable)
            /// <summary>
            /// Converts the given <see cref="INullable"/>
            /// value to an SQL BIGINT value.
            /// </summary>
            /// <param name="nullable">
            /// To convert to a <c>java.lang.Long</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Long</c>value.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the concrete type of the given <c>INullable</c> is not handled
            /// -or-
            /// When a number format exception is encountered
            /// -or-
            /// When the result of the conversion does not lie in
            /// the range of <c>SQL REAL</c>.
            /// </exception>
            [CLSCompliant(false)]
            public static JavaLong ToBigInt(INullable nullable)
            {
                if (nullable == null || nullable.IsNull)
                {
                    return null;
                }
                else if (nullable is SqlInt64) // most likely first
                {
                    return ToBigInt(((SqlInt64)nullable).Value);
                }
                if (nullable is SqlInt32)
                {
                    return ToBigInt(((SqlInt32)nullable).Value);
                }
                if (nullable is SqlInt16)
                {
                    return ToBigInt(((SqlInt16)nullable).Value);
                }
                if (nullable is SqlByte)
                {
                    return ToBigInt(((SqlByte)nullable).Value);
                }
                if (nullable is SqlSingle)
                {
                    return ToBigInt(((SqlSingle)nullable).Value);
                }
                if (nullable is SqlDouble)
                {
                    return ToBigInt(((SqlDouble)nullable).Value);
                }
                if (nullable is SqlDecimal)
                {
                    return ToBigInt(((SqlDecimal)nullable).Value);
                }
                if (nullable is SqlMoney)
                {
                    return ToBigInt(((SqlMoney)nullable).Value);
                }
                if (nullable is SqlBoolean)
                {
                    return ToBigInt(((SqlBoolean)nullable).Value);
                }
                if (nullable is SqlString)
                {
                    return ToBigInt(((SqlString)nullable).Value);
                }
                if (nullable is SqlChars)
                {
                    return ToBigInt(((SqlChars)nullable).ToSqlString());
                }

                throw HsqlConvert.WrongDataType(nullable);
            }
            #endregion

            #region ToBigInt(uint)
            /// <summary>
            /// Converts the given <see cref="System.UInt32"/> value
            /// to an <c>SQL BIGINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="unitValue">
            /// To convert to a <c>java.lang.Long</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Long</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaLong ToBigInt(uint unitValue)
            {
                return ValuePool.getLong(unitValue);
            }
            #endregion

            #region ToBigInt(long)
            /// <summary>
            /// Converts the given <see cref="System.Int64"/> value
            /// to an <c>SQL BIGINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="longValue">
            /// To convert to a <c>java.lang.Long</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Long</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaLong ToBigInt(long longValue)
            {
                return ValuePool.getLong(longValue);
            }
            #endregion

            #region ToBigInt(ulong)
            /// <summary>
            /// Converts the given <see cref="System.UInt64"/> value
            /// to an <c>SQL BIGINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="ulongValue">
            /// To convert to a <c>java.lang.Long</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Long</c> value.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the given value lies outside the range of <c>SQL BIGINT</c>.
            /// </exception>
            [CLSCompliant(false)]
            public static JavaLong ToBigInt(ulong ulongValue)
            {
                if (long.MaxValue < ulongValue)
                {
                    throw NumericValueOutOfRange(ulongValue);
                }

                return ValuePool.getLong((long)ulongValue);
            }
            #endregion

            #region ToBigInt(float)
            /// <summary>
            /// Converts the given <see cref="System.Single"/> value
            /// to an <c>SQL BIGINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="floatValue">
            /// To convert to a <c>java.lang.Long</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Long</c> value.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the given value does not lie in the range of <c>SQL BIGINT</c>.
            /// </exception>
            [CLSCompliant(false)]
            public static JavaLong ToBigInt(float floatValue)
            {
                if (long.MaxValue < floatValue || floatValue < long.MinValue)
                {
                    throw NumericValueOutOfRange(floatValue);
                }

                return ValuePool.getLong((long)floatValue);
            }
            #endregion

            #region ToBigInt(double)
            /// <summary>
            /// Converts the given <see cref="System.Double"/> value
            /// to an <c>SQL BIGINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="doubleValue">
            /// To convert to a <c>java.lang.Long</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Long</c> value.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the given value does not lie in the range of <c>SQL BIGINT</c>.
            /// </exception>
            [CLSCompliant(false)]
            public static JavaLong ToBigInt(double doubleValue)
            {
                if (long.MaxValue < doubleValue || doubleValue < long.MinValue)
                {
                    throw NumericValueOutOfRange(doubleValue);
                }

                return ValuePool.getLong((long)doubleValue);
            }
            #endregion

            #region ToBigInt(decimal)
            /// <summary>
            /// Converts the given <see cref="System.Decimal"/> value
            /// to an <c>SQL BIGINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="decimalValue">
            /// To convert to a <c>java.lang.Long</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Long</c> value.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the given value does not lie in the range of <c>SQL BIGINT</c>.
            /// </exception>
            [CLSCompliant(false)]
            public static JavaLong ToBigInt(decimal decimalValue)
            {
                if (long.MaxValue < decimalValue || decimalValue < long.MinValue)
                {
                    throw NumericValueOutOfRange(decimalValue);
                }

                return ValuePool.getLong((long)decimalValue);
            }
            #endregion

            #region ToBigInt(string)
            /// <summary>
            /// Converts the given <see cref="System.String"/> value
            /// to an <c>SQL BIGINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="stringValue">
            /// To convert to a <c>java.lang.Long</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Long</c> value.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When a number format exception is encountered
            /// -or-
            /// When the result of the conversion does not lie in the range of <c>SQL BIGINT</c>.
            /// </exception>
            /// <exception cref="NullReferenceException">
            /// When <c>stringValue</c> is null.
            /// </exception>
            [CLSCompliant(false)]
            public static JavaLong ToBigInt(string stringValue)
            {
                return ToBigInt(ParseBigInt(stringValue));
            }
            #endregion

            #region ToBigInt(object)
            /// <summary>
            /// Converts the given .NET <see cref="System.Object"/> value
            /// to an <c>SQL BIGINT</c> value.
            /// </summary>
            /// <remarks>
            /// <para>
            /// This method is not fully generalized; it is intended specifically
            /// for conversions proceeding from the .NET type system to the HSQLDB
            /// type system.  For instance, passing a <c>java.lang.Long</c>
            /// value will result in an invalid conversion exception, rather than
            /// the identity conversion.  This is in the interest of keeping
            /// this method as short as possible for its intended purpose.
            /// </para>
            /// <para>
            /// Note also that in most cases, this method is backed by a value pool
            /// in order to reduce memory consumption.
            /// </para>
            /// </remarks>
            /// <param name="objectValue">
            /// To convert to a <c>java.lang.Long</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Long</c> value.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the result of the conversion does not lie in the range of <c>SQL BIGINT</c>
            /// -or-
            /// When there does not exist a conversion for the type of the given object.
            /// </exception>
            [CLSCompliant(false)]
            public static JavaLong ToBigInt(object objectValue)
            {
                if (objectValue == null)
                {
                    return null;
                }

                TypeCode typeCode = Type.GetTypeCode(objectValue.GetType());

                switch (typeCode)
                {
                    case TypeCode.Boolean:
                        {
                            return ToBigInt((bool)objectValue);
                        }
                    case TypeCode.Byte:
                        {
                            return ToBigInt((byte)objectValue);
                        }
                    case TypeCode.Char:
                        {
                            return ToBigInt((ushort)objectValue);
                        }
                    case TypeCode.DateTime:
                        {
                            throw InvalidConversion(Types.TIMESTAMP);
                        }
                    case TypeCode.DBNull:
                        {
                            return null;
                        }
                    case TypeCode.Decimal:
                        {
                            return ToBigInt((decimal)objectValue);
                        }
                    case TypeCode.Double:
                        {
                            return ToBigInt((double)objectValue);
                        }
                    case TypeCode.Empty:
                        {
                            return null;
                        }
                    case TypeCode.Int16:
                        {
                            return ToBigInt((short)objectValue);
                        }
                    case TypeCode.Int32:
                        {
                            return ToBigInt((int)objectValue);
                        }
                    case TypeCode.Int64:
                        {
                            return ToBigInt((long)objectValue);
                        }
                    case TypeCode.Object:
                        {
                            IConvertible convertible = objectValue as IConvertible;

                            if (convertible != null)
                            {
                                return ToBigInt(Convert.ToInt64(convertible));
                            }

                            if (objectValue is INullable)
                            {
                                return ToBigInt((INullable)objectValue);
                            }

                            char[] characterValue = objectValue as char[];

                            if (characterValue != null)
                            {
                                return ToBigInt(new string(characterValue));
                            }

                            throw HsqlConvert.WrongDataType(objectValue);
                        }
                    case TypeCode.SByte:
                        {
                            return ToBigInt((sbyte)objectValue);
                        }
                    case TypeCode.Single:
                        {
                            return ToBigInt((float)objectValue);
                        }
                    case TypeCode.String:
                        {
                            return ToBigInt((string)objectValue);
                        }
                    case TypeCode.UInt16:
                        {
                            return ToBigInt((ushort)objectValue);
                        }
                    case TypeCode.UInt32:
                        {
                            return ToBigInt((uint)objectValue);
                        }
                    case TypeCode.UInt64:
                        {
                            return ToBigInt((ulong)objectValue);
                        }
                }

                throw WrongDataType(objectValue);
            }
            #endregion

            #endregion

            #region ToBinary

            #region Fields
            private static readonly byte[] binaryTrue = new byte[] { 1 };
            private static readonly byte[] binaryFalse = new byte[] { 0 }; 
            #endregion

            #region ToBinary(byte[])
            /// <summary>
            /// Converts the given array of <see cref="System.Byte"/>
            /// to a <c>org.hsqldb.types.Binary</c> value.
            /// </summary>
            /// <param name="bytes">
            /// To convert to a <c>org.hsqldb.types.Binary</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>org.hsqldb.types.Binary</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static Binary ToBinary(byte[] bytes)
            {
                return (bytes == null) ? null : new Binary(bytes, /*clone*/true);
            }
            #endregion

            #region ToBinary(bool)
            /// <summary>
            /// Converts the given <see cref="System.Boolean"/> value 
            /// to an <c>SQL BINARY</c> value.
            /// </summary>
            /// <remarks>
            /// <c>true</c> is converted to <c>{0x01}</c>;
            /// <c>false</c> is converted to <c>{0x00}</c>.
            /// </remarks>
            /// <param name="boolValue">
            /// To convert to a <c>org.hsqldb.types.Binary</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>SQL BINARY</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static Binary ToBinary(bool boolValue)
            {
                return new Binary(
                    boolValue ? binaryTrue : binaryFalse,
                    /*clone*/ true);
            }
            #endregion

            #region ToBinary(byte)
            /// <summary>
            /// Converts the given <see cref="System.Byte"/> value
            /// to an <c>SQL BINARY</c> value.
            /// value.
            /// </summary>
            /// <param name="byteValue">
            /// To convert to a <c>org.hsqldb.types.Binary</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>org.hsqldb.types.Binary</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static Binary ToBinary(byte byteValue)
            {
                return new Binary(new byte[] { byteValue }, /*clone*/false);
            }
            #endregion

            #region ToBinary(sbyte)
            /// <summary>
            /// Converts the given <see cref="System.SByte"/> value
            /// to an <c>SQL BINARY</c> value.
            /// </summary>
            /// <param name="sbyteValue">
            /// To convert to a <c>org.hsqldb.types.Binary</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>org.hsqldb.types.Binary</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static Binary ToBinary(sbyte sbyteValue)
            {
                //if (sbyteValue < 0)
                //{
                //    throw NumericValueOutOfRange(sbyteValue);
                //}
                // checkme
                return new Binary(new byte[] { (byte)sbyteValue }, /*clone*/false);
            }
            #endregion

            #region ToBinary(short)
            /// <summary>
            /// Converts the given <see cref="System.Int16"/> value 
            /// to an <c>SQL BINARY</c> value.
            /// </summary>
            /// <remarks>
            /// The conversion proceeds using <c>BigEndian</c> byte order.
            /// </remarks>
            /// <param name="shortValue">
            /// For which to retrieve the <c>org.hsqldb.types.Binary</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>org.hsqldb.types.Binary</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static Binary ToBinary(short shortValue)
            {
                return new Binary(
                    new byte[]
                    {
                        (byte) ((shortValue >> 8) & 0xff),
                        (byte) ((shortValue >> 0) & 0xff)
                    },
                    /*clone*/false);
            }
            #endregion

            #region ToBinary(ushort)
            /// <summary>
            /// Converts the given <see cref="System.UInt16"/> value 
            /// to an <c>SQL BINARY</c> value.
            /// </summary>
            /// <remarks>
            /// The conversion proceeds using <c>BigEndian</c> byte order.
            /// </remarks>
            /// <param name="ushortValue">
            /// For which to retrieve the <c>org.hsqldb.types.Binary</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>org.hsqldb.types.Binary</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static Binary ToBinary(ushort ushortValue)
            {
                return new Binary(
                    new byte[]
                    {
                        (byte) ((ushortValue >> 8) & 0xff),
                        (byte) ((ushortValue >> 0) & 0xff)
                    },
                    /*clone*/false);
            }
            #endregion

            #region ToBinary(int)
            /// <summary>
            /// Converts the given <see cref="System.Int32"/>
            /// value to an <c>SQL BINARY</c> value.
            /// </summary>
            /// <remarks>
            /// The conversion proceeds using <c>BigEndian</c> byte order.
            /// </remarks>
            /// <param name="intValue">
            /// To convert to a <c>org.hsqldb.types.Binary</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>org.hsqldb.types.Binary</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static Binary ToBinary(int intValue)
            {
                return new Binary(
                    new byte[]
                    {
                        (byte) ((intValue >> 24) & 0xff),
                        (byte) ((intValue >> 16) & 0xff),
                        (byte) ((intValue >>  8) & 0xff),
                        (byte) ((intValue >>  0) & 0xff)
                    },
                    /*clone*/false);
            }
            #endregion

            #region ToBinary(INullable)
            /// <summary>
            /// Converts the given <see cref="INullable"/>
            /// value to an <c>SQL BINARY</c> value.
            /// </summary>
            /// <param name="nullable">
            /// For which to retrieve the <c>org.hsqldb.types.Binary</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>org.hsqldb.types.Binary</c> value.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the concrete type of the given <c>INullable</c> is not handled
            /// </exception>
            [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Performance", "CA1800:DoNotCastUnnecessarily")]
            [CLSCompliant(false)]
            public static Binary ToBinary(INullable nullable)
            {
                if (nullable == null || nullable.IsNull)
                {
                    return null;
                }
                // most likely first ...
                else if (nullable is SqlBytes)
                {
                    return new Binary(((SqlBytes)nullable).Value, /*clone*/false);
                }
                else if (nullable is SqlBinary)
                {
                    return new Binary(((SqlBinary)nullable).Value, /*clone*/false);
                }
                else if (nullable is SqlGuid)
                {
                    return new Binary(((SqlGuid)nullable).ToByteArray(), /*clone*/false);
                }
                // then, in alphabetical order...
                else if (nullable is SqlBoolean)
                {
                    return ToBinary(((SqlBoolean)nullable).Value);
                }
                else if (nullable is SqlByte)
                {
                    return ToBinary(((SqlByte)nullable).Value);
                }
                else if (nullable is SqlChars)
                {
                    ToBinary(((SqlChars)nullable).ToSqlString());
                }
                else if (nullable is SqlDecimal)
                {
                    return ToBinary(((SqlDecimal)nullable).Value);
                }
                else if (nullable is SqlDouble)
                {
                    return ToBinary(((SqlDouble)nullable).Value);
                }
                if (nullable is SqlInt16)
                {
                    return ToBinary(((SqlInt16)nullable).Value);
                }
                else if (nullable is SqlInt32)
                {
                    return ToBinary(((SqlInt32)nullable).Value);
                }
                else if (nullable is SqlInt64)
                {
                    return ToBinary(((SqlInt64)nullable).Value);
                }
                else if (nullable is SqlMoney)
                {
                    return ToBinary(((SqlMoney)nullable).Value);
                }
                else if (nullable is SqlSingle)
                {
                    return ToBinary(((SqlSingle)nullable).Value);
                }
                else if (nullable is SqlString)
                {
                    return ToBinary(((SqlString)nullable).Value);
                }

                throw HsqlConvert.WrongDataType(nullable);
            }
            #endregion

            #region ToBinary(uint)
            /// <summary>
            /// Converts the given <see cref="System.UInt16"/> value
            /// to an <c>SQL BINARY</c> value.
            /// </summary>
            /// <remarks>
            /// The conversion proceeds using <c>BigEndian</c> byte order.
            /// </remarks>
            /// <param name="uintValue">
            /// To convert to a <c>org.hsqldb.types.Binary</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>org.hsqldb.types.Binary</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static Binary ToBinary(uint uintValue)
            {
                return new Binary(
                    new byte[]
                    {
                        (byte) ((uintValue >> 24) & 0xff),
                        (byte) ((uintValue >> 16) & 0xff),
                        (byte) ((uintValue >>  8) & 0xff),
                        (byte) ((uintValue >>  0) & 0xff)
                    },
                    /*clone*/false);
            }
            #endregion

            #region ToBinary(long)
            /// <summary>
            /// Converts the given <see cref="System.Int64"/> value
            /// to an <c>SQL BINARY</c> value.
            /// </summary>
            /// <remarks>
            /// The conversion proceeds using <c>BigEndian</c> byte order.
            /// </remarks>
            /// <param name="longValue">
            /// To convert to a <c>org.hsqldb.types.Binary</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>org.hsqldb.types.Binary</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static Binary ToBinary(long longValue)
            {
                return new Binary(
                    new byte[]
                    {
                        (byte) ((longValue >> 56) & 0xff),
                        (byte) ((longValue >> 48) & 0xff),
                        (byte) ((longValue >> 40) & 0xff),
                        (byte) ((longValue >> 32) & 0xff),
                        (byte) ((longValue >> 24) & 0xff),
                        (byte) ((longValue >> 16) & 0xff),
                        (byte) ((longValue >> 8)  & 0xff),
                        (byte) ((longValue >> 0)  & 0xff)
                    },
                    /*clone*/false);
            }
            #endregion

            #region ToBinary(ulong)
            /// <summary>
            /// Converts the given <c>ulong</c> value to an SQL BINARY value.
            /// </summary>
            /// <remarks>
            /// The conversion proceeds using <c>BigEndian</c> byte order.
            /// </remarks>
            /// <param name="ulongValue">
            /// For which to retrieve the <c>org.hsqldb.types.Binary</c> value.
            /// </param>
            /// <returns>
            /// The corresponding SQL BINARY value.
            /// </returns>
            [CLSCompliant(false)]
            public static Binary ToBinary(ulong ulongValue)
            {
                return new Binary(
                    new byte[]
                    {
                        (byte) ((ulongValue >> 56) & 0xff),
                        (byte) ((ulongValue >> 48) & 0xff),
                        (byte) ((ulongValue >> 40) & 0xff),
                        (byte) ((ulongValue >> 32) & 0xff),
                        (byte) ((ulongValue >> 24) & 0xff),
                        (byte) ((ulongValue >> 16) & 0xff),
                        (byte) ((ulongValue >>  8) & 0xff),
                        (byte) ((ulongValue >>  0) & 0xff)
                    },
                    /*clone*/false);
            }
            #endregion

            #region ToBinary(float)
            /// <summary>
            /// Converts the given <see cref="System.Single"/> value
            /// to an <c>SQL BINARY</c> value.
            /// </summary>
            /// <remarks>
            /// The conversion proceeds by applying
            /// <c>java.lang.Float.floatToRawIntBits(floatValue)</c>
            /// and converting the resulting value
            /// using <see cref="ToBinary(int)"/>.
            /// </remarks>
            /// <param name="floatValue">
            /// To convert to a <c>org.hsqldb.types.Binary</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>org.hsqldb.types.Binary</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static Binary ToBinary(float floatValue)
            {
                return ToBinary(JavaFloat.floatToRawIntBits(floatValue));
            }
            #endregion

            #region ToBinary(double)
            /// <summary>
            /// Converts the given <see cref="System.Double"/> value
            /// to an <c>SQL BINARY</c> value.
            /// </summary>
            /// <remarks>
            /// The conversion proceeds by applying
            /// <c>java.lang.Double.doubleToRawLongBits(doubleValue)</c>
            /// and converting the resulting value
            /// using <see cref="ToBinary(long)"/>.
            /// </remarks>
            /// <param name="doubleValue">
            /// To convert to a <c>org.hsqldb.types.Binary</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>org.hsqldb.types.Binary</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static Binary ToBinary(double doubleValue)
            {
                return ToBinary(JavaDouble.doubleToRawLongBits(doubleValue));
            }
            #endregion

            #region ToBinary(decimal)
            /// <summary>
            /// Converts the given <c>decimal</c> value to an SQL BINARY value.
            /// </summary>
            /// <remarks>
            /// The conversion proceeds by applying
            /// <see cref="System.Decimal.GetBits(decimal)"/>
            /// and processing the resulting <c>int[]</c> value
            /// in <c>BigEndian</c> byte order.
            /// </remarks>
            /// <param name="decimalValue">
            /// To convert to a <c>org.hsqldb.types.Binary</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>org.hsqldb.types.Binary</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static Binary ToBinary(decimal decimalValue)
            {
                int[] bits = decimal.GetBits(decimalValue);

                int i0 = bits[0];
                int i1 = bits[1];
                int i2 = bits[2];
                int i3 = bits[3];

                return new Binary(
                    new byte[]
                    {
                        (byte) ((i0 >> 24) & 0xff),
                        (byte) ((i0 >> 16) & 0xff),
                        (byte) ((i0 >>  8) & 0xff),
                        (byte) ((i0 >>  0) & 0xff),

                        (byte) ((i1 >> 24) & 0xff),
                        (byte) ((i1 >> 16) & 0xff),
                        (byte) ((i1 >>  8) & 0xff),
                        (byte) ((i1 >>  0) & 0xff),

                        (byte) ((i2 >> 24) & 0xff),
                        (byte) ((i2 >> 16) & 0xff),
                        (byte) ((i2 >>  8) & 0xff),
                        (byte) ((i2 >>  0) & 0xff),

                        (byte) ((i3 >> 24) & 0xff),
                        (byte) ((i3 >> 16) & 0xff),
                        (byte) ((i3 >>  8) & 0xff),
                        (byte) ((i3 >>  0) & 0xff)
                    },
                    /*clone*/false);
            }
            #endregion

            #region ToBinary(string)
            /// <summary>
            /// Converts the given hexadedimal character sequence
            /// to an <c>SQL BINARY</c> value.
            /// </summary>
            /// <remarks>
            /// The character sequence must consist of an even number
            /// of hexadecimal digits. 
            /// </remarks>
            /// <param name="stringValue">
            /// To convert to a <c>org.hsqldb.types.Binary</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>org.hsqldb.types.Binary</c> value.
            /// </returns>
            /// <exception cref="ArgumentException">
            /// When the given character sequence is null;
            /// has an odd number of characters;
            /// contains a non-hexadecimal digit;
            /// </exception>
            [CLSCompliant(false)]
            public static Binary ToBinary(string stringValue)
            {
                try
                {
                    return new Binary(StringConverter.hexToByte(stringValue), /*clone*/false);
                }
                catch (Exception ex)
                {
                    throw new ArgumentException(ex.Message, "value");
                }
            }
            #endregion

            #region ToBinary(object)
            /// <summary>
            /// Converts the given .NET <see cref="System.Object"/> value
            /// to an <c>SQL BINARY</c> value.
            /// </summary>
            /// <remarks>
            /// This method is not fully generalized; it is intended specifically
            /// for conversions proceeding from the .NET type system to the HSQLDB
            /// type system.  For instance, passing an <c>org.hsqldb.types.Binary</c>
            /// value will result in an invalid conversion exception, rather than
            /// the identity conversion.  This is in the interest of keeping
            /// this method as short as possible for its intended purpose.
            /// </remarks>
            /// <param name="objectValue">
            /// To convert to an <c>org.hsqldb.types.Binary</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>org.hsqldb.types.Binary</c> value.
            /// </returns>
            [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Maintainability", "CA1502:AvoidExcessiveComplexity")]
            [CLSCompliant(false)]
            public static Binary ToBinary(object objectValue)
            {
                if (objectValue == null)
                {
                    return null;
                }

                TypeCode typeCode = Type.GetTypeCode(objectValue.GetType());

                switch (typeCode)
                {
                    case TypeCode.Boolean:
                        {
                            return ToBinary((bool)objectValue);
                        }
                    case TypeCode.Byte:
                        {
                            return ToBinary((byte)objectValue);
                        }
                    case TypeCode.Char:
                        {
                            return ToBinary((char)objectValue);
                        }
                    case TypeCode.DateTime:
                        {
                            throw InvalidConversion(Types.TIMESTAMP);
                        }
                    case TypeCode.DBNull:
                        {
                            return null;
                        }
                    case TypeCode.Decimal:
                        {
                            return ToBinary((decimal)objectValue);
                        }
                    case TypeCode.Double:
                        {
                            return ToBinary((double)objectValue);
                        }
                    case TypeCode.Empty:
                        {
                            return null;
                        }
                    case TypeCode.Int16:
                        {
                            return ToBinary((short)objectValue);
                        }
                    case TypeCode.Int32:
                        {
                            return ToBinary((int)objectValue);
                        }
                    case TypeCode.Int64:
                        {
                            return ToBinary((long)objectValue);
                        }
                    case TypeCode.Object:
                        {
                            byte[] byteArrayValue = objectValue as byte[];

                            if (byteArrayValue != null)
                            {
                                return new Binary(byteArrayValue,/*clone*/true);
                            }

                            if (objectValue is INullable)
                            {
                                return ToBinary((INullable)objectValue);
                            }

                            char[] characterValue = objectValue as char[];

                            if (characterValue != null)
                            {
                                return ToBinary(new string(characterValue));
                            }

                            if (objectValue is Guid)
                            {                                
                                return new Binary(((Guid)objectValue).ToByteArray(),/*clone*/false);
                            }

                            Stream stream = objectValue as Stream;

                            if (stream != null)
                            {
                                using (MemoryStream ms = new MemoryStream())
                                {
                                    int bytesRead;
                                    byte[] buffer = new byte[1024];

                                    while (0 != (bytesRead = stream.Read(buffer, 0, 1024)))
                                    {
                                        ms.Write(buffer, 0, bytesRead);
                                    }

                                    return new Binary(ms.ToArray(),/*clone*/false);
                                }
                            }

                            throw InvalidConversion(Types.OTHER);
                        }
                    case TypeCode.SByte:
                        {
                            return ToBinary((sbyte)objectValue);
                        }
                    case TypeCode.Single:
                        {
                            return ToBinary((float)objectValue);
                        }
                    case TypeCode.String:
                        {
                            return ToBinary((string)objectValue);
                        }
                    case TypeCode.UInt16:
                        {
                            return ToBinary((ushort)objectValue);
                        }
                    case TypeCode.UInt32:
                        {
                            return ToBinary((uint)objectValue);
                        }
                    case TypeCode.UInt64:
                        {
                            return ToBinary((ulong)objectValue);
                        }
                }

                throw WrongDataType(objectValue);
            }
            #endregion

            #endregion

            #region ToString

            #region ToDateTimeString(...)
            /// <summary>
            /// Retrieves the SQL date-time character sequence
            /// corresponding to the given date-time component values.
            /// </summary>
            /// <param name="year">The year component.</param>
            /// <param name="month">The month component.</param>
            /// <param name="day">The day component.</param>
            /// <param name="hour">The hour component.</param>
            /// <param name="minute">The minute component.</param>
            /// <param name="second">The second component.</param>
            /// <param name="nanosecond">The nanosecond component.</param>
            /// <param name="checkRanges">
            /// When <c>true</c>, a range check is performed for
            /// each date-time component: year in [0..9999], month
            /// in [1..12], day in [1..31], hour in [0..23],
            /// minute in [0..59], second in [0..59], nanosecond
            /// in [0..999999].
            /// </param>
            /// <returns>
            /// The corresponding SQL date-time character sequence.
            /// </returns>
            [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Maintainability", "CA1502:AvoidExcessiveComplexity")]
            public static string ToDateTimeString(
                int year,
                int month,
                int day,
                int hour,
                int minute,
                int second,
                int nanosecond,
                bool checkRanges)
            {
                if (checkRanges)
                {
                    if (year < 0 || year > 9999)
                    {
                        throw new ArgumentOutOfRangeException("year",
                            year.ToString());
                    }
                    if (month < 1 || month > 12)
                    {
                        throw new ArgumentOutOfRangeException("month",
                            month.ToString());
                    }
                    if (day < 1 || day > 31)
                    {
                        throw new ArgumentOutOfRangeException("day",
                            day.ToString());
                    }
                    if (hour < 0 || hour > 23)
                    {
                        throw new ArgumentOutOfRangeException("hour",
                            hour.ToString());
                    }
                    if (minute < 0 || minute > 59)
                    {
                        throw new ArgumentOutOfRangeException("minute",
                            minute.ToString());
                    }
                    if (second < 0 || minute > 59)
                    {
                        throw new ArgumentOutOfRangeException("second",
                            second.ToString());
                    }
                    if (nanosecond < 0 || nanosecond > 999999)
                    {
                        throw new ArgumentOutOfRangeException("nanosecond",
                            nanosecond.ToString());
                    }
                }

                StringBuilder sb = new StringBuilder();

                sb.Append(year);

                sb.Append('-');

                if (month < 10)
                {
                    sb.Append('0');
                }

                sb.Append(month);

                sb.Append('-');

                if (day < 10)
                {
                    sb.Append('0');
                }

                sb.Append(day);

                if (hour == 0 && minute == 0 && second == 0 && nanosecond == 0)
                {
                    return ValuePool.getString(sb.ToString());
                }

                sb.Append(' ');

                if (hour < 10)
                {
                    sb.Append('0');
                }

                sb.Append(hour);

                sb.Append(':');

                if (minute < 10)
                {
                    sb.Append('0');
                }

                sb.Append(minute);

                sb.Append(':');

                if (second < 10)
                {
                    sb.Append('0');
                }

                sb.Append(second);

                if (nanosecond == 0)
                {
                    return ValuePool.getString(sb.ToString());
                }

                sb.Append('.');

                if (nanosecond < 9)
                {
                    sb.Append("00000");
                }
                else if (nanosecond < 99)
                {
                    sb.Append("0000");
                }
                else if (nanosecond < 999)
                {
                    sb.Append("000");
                }
                else if (nanosecond < 9999)
                {
                    sb.Append("00");
                }
                else if (nanosecond < 99999)
                {
                    sb.Append('0');
                }

                sb.Append(nanosecond);

                return ValuePool.getString(sb.ToString());
            }
            #endregion

            #region ToDateString(...)
            /// <summary>
            /// Retrieves the SQL date <see cref="System.String"/>
            /// corresponding to the given values.
            /// </summary>
            /// <param name="year">The year component.</param>
            /// <param name="month">The month compnent.</param>
            /// <param name="day">The day component.</param>
            /// <param name="checkRanges">if set to <c>true</c>, check ranges.</param>
            /// <returns>
            /// The corresponding SQL date string value.
            /// </returns>
            public static string ToDateString(
                int year,
                int month,
                int day,
                bool checkRanges)
            {
                if (checkRanges)
                {
                    if (year < 0 || year > 9999)
                    {
                        throw new ArgumentOutOfRangeException("year",
                            year.ToString());
                    }
                    if (month < 1 || month > 12)
                    {
                        throw new ArgumentOutOfRangeException("month",
                            month.ToString());
                    }
                    if (day < 1 || day > 31)
                    {
                        throw new ArgumentOutOfRangeException("day",
                            day.ToString());
                    }
                }

                StringBuilder sb = new StringBuilder();

                sb.Append(year);

                sb.Append('-');

                if (month < 10)
                {
                    sb.Append('0');
                }

                sb.Append(month);

                sb.Append('-');

                if (day < 10)
                {
                    sb.Append('0');
                }

                sb.Append(day);

                return sb.ToString();
            }
            #endregion

            #region ToTimeString(...)
            /// <summary>
            /// Retrieves the SQL time <see cref="System.String"/> value
            /// corresponding to the given values.
            /// </summary>
            /// <param name="hour">The hour component.</param>
            /// <param name="minute">The minute component.</param>
            /// <param name="second">The second component.</param>
            /// <param name="checkRanges">if set to <c>true</c>, check ranges.</param>
            /// <returns>
            /// The corresponding value.
            /// </returns>
            public static string ToTimeString(
                int hour,
                int minute,
                int second,
                bool checkRanges)
            {
                if (checkRanges)
                {
                    if (hour < 0 || hour > 23)
                    {
                        throw new ArgumentOutOfRangeException("hour",
                            hour.ToString());
                    }
                    if (minute < 0 || minute > 59)
                    {
                        throw new ArgumentOutOfRangeException("minute",
                            minute.ToString());
                    }
                    if (second < 0 || minute > 59)
                    {
                        throw new ArgumentOutOfRangeException("second",
                            second.ToString());
                    }
                }

                StringBuilder sb = new StringBuilder();

                if (hour < 10)
                {
                    sb.Append('0');
                }

                sb.Append(hour);

                sb.Append(':');

                if (minute < 10)
                {
                    sb.Append('0');
                }

                sb.Append(minute);

                sb.Append(':');

                if (second < 10)
                {
                    sb.Append('0');
                }

                sb.Append(second);

                return sb.ToString();
            }
            #endregion

            #region ToString(DateTime)
            /// <summary>
            /// Retrieves the SQL date-time character sequence
            /// corresponding to the given <see cref="System.DateTime"/>
            /// value.
            /// </summary>
            /// <param name="dateTimeValue">
            /// For which to retrieve the SQL date-time
            /// character sequence.
            /// </param>
            /// <returns>
            /// The corresponding value.
            /// </returns>
            public static string ToString(DateTime dateTimeValue)
            {
                int year = dateTimeValue.Year;
                int month = dateTimeValue.Month;
                int day = dateTimeValue.Day;
                int hour = dateTimeValue.Hour;
                int minute = dateTimeValue.Minute;
                int second = dateTimeValue.Second;
                int millisecond = dateTimeValue.Millisecond;

                return ToDateTimeString(
                    year,
                    month,
                    day,
                    hour,
                    minute,
                    second,
                    1000 * millisecond,
                    false);
            }
            #endregion

            #region ToString(INullable)
            /// <summary>
            /// Converts the given <see cref="INullable"/> value to an equivalent
            /// HSQLDB SQL literal value character sequence.
            /// <remarks>
            /// <para>
            /// Note that, to be parsed as part of an HSQLDB SQL commad
            /// text character sequence, the returned character sequence may
            /// need to be converted to the SQL string literal form (i.e.
            /// augmented with leading and trailing single quotes, and
            /// with embedded single quotes escaped by doubling).
            /// </para>
            /// <para>
            /// This is currently always true for <see cref="SqlBinary"/> and
            /// <see cref="SqlBytes"/>, which are both converted to
            /// hexadecimal-encoded character sequences.
            /// </para>
            /// <para>
            /// More obviously, this is always true for <see cref="SqlChars"/>,
            /// <see cref="SqlString"/> and <see cref="SqlXml"/>.  Additionally,
            /// embedded CR/LF characters need to be encoded to hexadecimal
            /// unicode escape form (i.e. sequences of the form: \uhhhh, where
            /// hhhh is the hexadecimal representation of the 16-bit unicode
            /// character code point).
            /// </para>
            /// <para>
            /// Less obviously, because there exist a myriad of forms
            /// and conventions regarding UUID/GUID handling, special
            /// treatment is typically required for <see cref="SqlGuid"/>,
            /// even if first converted to an equivalent <see cref="SqlBinary"/>
            /// or <see cref="SqlBytes"/> instance.
            /// </para>
            /// </remarks>
            /// </summary>
            /// <param name="nullable">
            /// To convert to an SQL literal value character sequence.
            /// </param>
            /// <returns>
            /// The SQL literal value character sequence corresponding to the given
            /// <c>nullable</c> value.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the concrete type of the given <c>INullable</c> is not handled
            /// </exception>
            public static string ToString(INullable nullable)
            {
                if (nullable == null || nullable.IsNull)
                {
                    /* CHECKME: "NULL" in this context?*/ 
                    return null;
                }
                else if (nullable is SqlBinary)
                {
                    return ValuePool.getString(StringConverter.byteToHex(
                        ((SqlBinary)nullable).Value));
                }
                else if (nullable is SqlBoolean)
                {
                    return ((SqlBoolean)nullable).Value ? "TRUE" : "FALSE";
                }
                else if (nullable is SqlByte)
                {
                    return ValuePool.getString(
                        ((SqlByte)nullable).Value.ToString());
                }
                else if (nullable is SqlBytes)
                {
                    return ValuePool.getString(StringConverter.byteToHex(
                        ((SqlBytes)nullable).Value));
                }
                else if (nullable is SqlChars)
                {
                    return ValuePool.getString(
                        new string(((SqlChars)nullable).Value));
                }
                else if (nullable is SqlDateTime)
                {
                    return ToString(((SqlDateTime)nullable).Value);
                }
                else if (nullable is SqlDecimal)
                {
                    return ValuePool.getString(
                        ((SqlDecimal)nullable).Value.ToString());
                }
                else if (nullable is SqlDouble)
                {
                    return ValuePool.getString(
                        ((SqlDouble)nullable).Value.ToString());
                }
                else if (nullable is SqlGuid)
                {
                    return ValuePool.getString(
                        ((SqlGuid)nullable).Value.ToString());
                }
                else if (nullable is SqlInt16)
                {
                    return ValuePool.getString(
                        ((SqlInt16)nullable).Value.ToString());
                }
                else if (nullable is SqlInt32)
                {
                    return ValuePool.getString(
                        ((SqlInt32)nullable).Value.ToString());
                }
                else if (nullable is SqlInt64)
                {
                    ValuePool.getString(
                        ((SqlInt64)nullable).Value.ToString());
                }
                else if (nullable is SqlMoney)
                {
                    return ValuePool.getString(
                        ((SqlMoney)nullable).Value.ToString());
                }
                else if (nullable is SqlSingle)
                {
                    return ValuePool.getString(
                        ((SqlSingle)nullable).Value.ToString());
                }
                else if (nullable is SqlString)
                {
                    return ValuePool.getString(
                        ((SqlString)nullable).Value);
                }
                else if (nullable is SqlXml)
                {
                    return ValuePool.getString(
                        ((SqlXml)nullable).Value);
                }

                throw HsqlConvert.WrongDataType(nullable);
            }
            #endregion

            #region ToString(TimeSpan)
            /// <summary>
            /// Converts the <c>TotalMilliseconds</c> property of the
            /// given <see cref="System.TimeSpan"/> to an SQL
            /// date-time character sequence representing
            /// the number of milliseconds since
            /// <c>January 1, 1970, 00:00:00 GMT</c>.
            /// </summary>
            /// <param name="timeSpanValue">
            /// To convert to an SQL date-time character sequence.
            /// </param>
            /// <returns>
            /// The corresponding SQL date-time character sequence.
            /// </returns>
            public static string ToString(TimeSpan timeSpanValue)
            {
                JavaGregorianCalendar cal = HsqlConvert.GetJavaGregorianCalendar();

                double totalMillis = timeSpanValue.TotalMilliseconds;

                if (totalMillis < long.MinValue || totalMillis > long.MaxValue)
                {
                    throw HsqlConvert.NumericValueOutOfRange(totalMillis);
                }

                cal.setTimeInMillis(java.lang.Math.round(totalMillis));

                int year = cal.get(JavaCalendar.YEAR);
                int month = cal.get(JavaCalendar.MONTH) + 1;
                int day = cal.get(JavaCalendar.DAY_OF_MONTH);
                int hour = cal.get(JavaCalendar.HOUR_OF_DAY);
                int minute = cal.get(JavaCalendar.MINUTE);
                int second = cal.get(JavaCalendar.SECOND);
                int nanosecond = 1000 * cal.get(JavaCalendar.MILLISECOND);

                return ToDateTimeString(
                    year,
                    month,
                    day,
                    hour,
                    minute,
                    second,
                    nanosecond,
                    false);
            }
            #endregion

            #region ToString(object)
            /// <summary>
            /// Converts the given <see cref="System.Object"/>
            /// value to an equivalent SQL character sequence
            /// </summary>
            /// <param name="value">
            /// To convert to an SQL character sequence.
            /// </param>
            /// <returns>
            /// The corresponding SQL character sequence.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the concrete type of the given <c>object</c> is not handled
            /// -or-
            /// When a maximum string length exceeded condition is encountered
            /// </exception>
            /// <exception cref="ArgumentOutOfRangeException">
            /// When the given object is a <c>TextReader</c> or wrapped in a
            /// <c>TextReader</c> in order to perform the conversion and, while 
            /// attempting to perform a <c>TextReader.ReadToEnd()</c> operation,
            /// the number of characters in the next line is larger than
            /// <see cref="Int32.MaxValue"/>.
            /// </exception>
            /// <exception cref="System.IO.IOException">
            /// When the given object is a TextReader or wrapped in a
            /// TextReader in order to perform the conversion and, while 
            /// attempting to perform a <c>TextReader.ReadToEnd()</c> operation,
            /// an I/O error occurs.
            /// </exception>
            /// <exception cref="OutOfMemoryException">
            /// When there is insufficient memory to allocate a buffer for
            /// the returned string.
            /// </exception>
            /// <exception cref="System.Data.Common.DbException">
            /// When the given object is an <c>IClob</c> and an error
            /// occurs while accessing the <c>SQL CLOB</c> value it
            /// represents.
            /// </exception>
            public static string ToString(object value)
            {
                if (value == null)
                {
                    return null;
                }

                TypeCode typeCode = Type.GetTypeCode(value.GetType());

                switch (typeCode)
                {
                    case TypeCode.Boolean:
                        {
                            return ValuePool.getString(((bool)value)
                                ? "TRUE"
                                : "FALSE");
                        }
                    case TypeCode.Byte:
                        {
                            return ValuePool.getString(value.ToString());
                        }
                    case TypeCode.Char:
                        {
                            return ValuePool.getString(value.ToString());
                        }
                    case TypeCode.DateTime:
                        {
                            return ToString((DateTime)value);
                        }
                    case TypeCode.DBNull:
                        {
                            return null;
                        }
                    case TypeCode.Decimal:
                        {
                            return ValuePool.getString(value.ToString());
                        }
                    case TypeCode.Double:
                        {
                            return ValuePool.getString(value.ToString());
                        }
                    case TypeCode.Empty:
                        {
                            return null;
                        }
                    case TypeCode.Int16:
                        {
                            return ValuePool.getString(value.ToString());
                        }
                    case TypeCode.Int32:
                        {
                            return ValuePool.getString(value.ToString());
                        }
                    case TypeCode.Int64:
                        {
                            return ValuePool.getString(value.ToString());
                        }
                    case TypeCode.Object:
                        {
                            if (value is INullable)
                            {
                                return ToString((INullable)value);
                            }
                            else if (value is TimeSpan)
                            {
                                return ToString((TimeSpan)value);
                            }
                            else if (value is byte[])
                            {
                                return ValuePool.getString(
                                    StringConverter.byteToHex((byte[])value));
                            }
                            else if (value is char[])
                            {
                                return ValuePool.getString(new string((char[])value));
                            }
                            else if (value is Guid)
                            {
                                return ValuePool.getString(
                                    ((Guid)value).ToString());
                            }
                            else if (value is Stream)
                            {
                                TextReader reader = new StreamReader((Stream)value);

                                return ValuePool.getString(reader.ReadToEnd());
                            }
                            else if (value is TextReader)
                            {
                                return ((TextReader)value).ReadToEnd();
                            }
                            else if (value is IBlob)
                            {
                                // Can't do length check here, as encoding determines 
                                // bytes / character, which may be as many as 4 (e.g. UTF32)
                                // or may vary from character to character (e.g. up to 3 for
                                // UTF8)
                                bool detectEndcodingFromByteOrderMarks = true;

                                using (Stream stream = ((IBlob)value).GetBinaryStream())
                                using (StreamReader reader = new StreamReader(stream, detectEndcodingFromByteOrderMarks))
                                {
                                    return ValuePool.getString(reader.ReadToEnd());
                                }
                            }
                            else if (value is IClob)
                            {
                                IClob clob = (IClob)value;
                                long length = clob.Length;

                                if (length <= int.MaxValue)
                                {
                                    return ValuePool.getString(
                                        clob.GetSubString(1, (int)length));
                                }

                                throw new HsqlDataSourceException(
                                    "Max String Length Exceeded: "
                                    + length); // NOI18N
                            }
                            else
                            {
                                java.lang.Object jobj = (value as java.lang.Object);

                                if (jobj != null)
                                {
                                    return ValuePool.getString(jobj.toString());
                                }

                                return ValuePool.getString(Convert.ToString(value));
                            }
                        }
                    case TypeCode.SByte:
                        {
                            return ValuePool.getString(value.ToString());
                        }
                    case TypeCode.Single:
                        {
                            return ValuePool.getString(value.ToString());
                        }
                    case TypeCode.String:
                        {
                            return ValuePool.getString(value.ToString());
                        }
                    case TypeCode.UInt16:
                        {
                            return ValuePool.getString(value.ToString());
                        }
                    case TypeCode.UInt32:
                        {
                            return ValuePool.getString(value.ToString());
                        }
                    case TypeCode.UInt64:
                        {
                            return ValuePool.getString(value.ToString());
                        }
                }

                throw HsqlConvert.WrongDataType(value);
            }
            #endregion

            #endregion

            #region ToDate

            #region ToDateInMillis(TimeSpan)
            /// <summary>
            /// Retrieves the absolute duration, in whole number of days,
            /// of the given <see cref="System.TimeSpan"/> value as
            /// the number of milliseconds since
            /// <c>January 1, 1970, 00:00:00 GMT</c>.
            /// </summary>
            /// <param name="timeSpanValue">
            /// To convert to an <c>Int64</c> date in milliseconds value.
            /// </param>
            /// <returns>
            /// The corresponding <c>Int64</c> value.
            /// </returns>
            public static long ToDateInMillis(TimeSpan timeSpanValue)
            {
                timeSpanValue = timeSpanValue.Duration();

                JavaGregorianCalendar cal = HsqlConvert.GetJavaGregorianCalendar();

                cal.clear();
                cal.add(JavaCalendar.DAY_OF_YEAR, timeSpanValue.Days);

                return cal.getTimeInMillis();
            }
            #endregion

            #region ToDateInMillis(DateTime)
            /// <summary>
            /// Retrieves the date portion (<c>yyyy-mm-dd</c>)
            /// of the given <see cref="System.DateTime"/> value
            /// as the number of milliseconds since 
            /// <c>January 1, 1970, 00:00:00 GMT</c>.
            /// </summary>
            /// <param name="dateTimeValue">
            /// To convert to an <c>Int64</c> date in milliseconds value.
            /// </param>
            /// <returns>
            /// The corresponding <c>Int64</c> value.
            /// </returns>
            public static long ToDateInMillis(DateTime dateTimeValue)
            {
                JavaGregorianCalendar cal = HsqlConvert.GetJavaGregorianCalendar();

                cal.clear();
                cal.set(JavaCalendar.YEAR, dateTimeValue.Year);
                cal.set(JavaCalendar.MONTH, dateTimeValue.Month - 1);
                cal.set(JavaCalendar.DATE, dateTimeValue.Day);

                return cal.getTimeInMillis();
            }
            #endregion

            #region ToDate(DateTime)
            /// <summary>
            /// Converts the given <see cref="System.DateTime"/>
            /// value to an <c>SQL DATE</c> value.
            /// </summary>
            /// <param name="dateTimeValue">
            /// To convert to a <c>java.sql.Date</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.sql.Date</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static java.sql.Date ToDate(DateTime dateTimeValue)
            {
                long millis = ToDateInMillis(dateTimeValue);

                return new java.sql.Date(millis);
            }
            #endregion

            #region ToDate(TimeSpan)
            /// <summary>
            /// Converts the given <see cref="System.TimeSpan"/>
            /// value to an <c>SQL DATE</c> value.
            /// </summary>
            /// <remarks>
            /// The conversion proceeds by constucting a new
            /// <c>java.sql.Date</c> object from the <c>long</c> 
            /// value returned by invoking <see cref="ToDateInMillis(TimeSpan)"/>
            /// with the given <c>System.TimeSpan</c> value.
            /// </remarks>
            /// <param name="timeSpanValue">
            /// To convert to a <c>java.sql.Date</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.sql.Date</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static java.sql.Date ToDate(TimeSpan timeSpanValue)
            {
                long millis = FromDotNet.ToDateInMillis(timeSpanValue);

                return new java.sql.Date(millis);
            }
            #endregion

            #region ToDate(string)
            /// <summary>
            /// Converts the given <see cref="System.String"/>
            /// to an <c>SQL DATE</c> value.
            /// </summary>
            /// <remarks>
            /// The given <c>stringValue</c> must be in a form
            /// accepted by <c>org.hsqldb.HsqlDateTime.dateValue(string)</c>
            /// </remarks>
            /// <param name="stringValue">
            /// To convert to a <c>java.sql.Date</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.sql.Date</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static java.sql.Date ToDate(string stringValue)
            {
                return org.hsqldb.HsqlDateTime.dateValue(stringValue);
            }
            #endregion

            #region ToDate(INullable)
            /// <summary>
            /// Converts the given <see cref="INullable"/>
            /// to an <c>SQL DATE</c> value.
            /// value.
            /// </summary>
            /// <remarks>
            /// Handles <c>SqlDateTime</c>, <c>SqlString</c> and
            /// <c>SlqChars</c> types.
            /// </remarks>
            /// <param name="nullable">
            /// To convert to a <c>java.sql.Date</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.sql.Date</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static java.sql.Date ToDate(INullable nullable)
            {
                if (nullable == null || nullable.IsNull)
                {
                    return null;
                }
                else if (nullable is SqlDateTime)
                {
                    return ToDate(((SqlDateTime)nullable).Value);
                }
                else if (nullable is SqlString)
                {
                    return ToDate(((SqlString)nullable).Value);
                }
                else if (nullable is SqlChars)
                {
                    return ToDate(((SqlChars)nullable).ToSqlString());
                }

                throw WrongDataType(nullable);
            } 
            #endregion

            #region ToDate(object)
            /// <summary>
            /// Converts the given .NET <see cref="System.Object"/>
            /// value to an <c>SQL DATE</c> value.
            /// </summary>
            /// <remarks>
            /// <c>null</c>, <c>TypeCode.DBNull</c> and
            /// <c>TypeCode.Empty</c> values result in <c>null</c>.
            /// <c>char[]</c>, <c>DateTime</c>, <c>string</c>
            /// and <c>TimeSpan</c> are processed according to type.
            /// <c>char[]</c> and <c>string</c> must be in a form
            /// accepted by <see cref="ToDate(string)"/>.
            /// Values of any other type raise a wrong data type
            /// exception.
            /// </remarks>
            /// <param name="objectValue">
            /// To convert to a <c>java.sql.Date</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.sql.Date</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static java.sql.Date ToDate(object objectValue)
            {
                if (objectValue == null)
                {
                    return null;
                }

                TypeCode typeCode = Type.GetTypeCode(objectValue.GetType());

                switch (typeCode)
                {
                    case TypeCode.DateTime:
                        {
                            return ToDate((DateTime)objectValue);
                        }
                    case TypeCode.DBNull:
                        {
                            return null;
                        }
                    case TypeCode.Empty:
                        {
                            return null;
                        }
                    case TypeCode.Object:
                        {
                            if (objectValue is TimeSpan)
                            {
                                return ToDate((TimeSpan)objectValue);
                            }
                            else if (objectValue is char[])
                            {
                                char[] chars = (char[])objectValue;

                                if (chars.Length < 10)
                                {
                                    throw HsqlConvert.InvalidConversion(Types.DATE);
                                }

                                return ToDate(new string(chars, 0, 10));
                            }
                            else if (objectValue is INullable)
                            {
                                return ToDate((INullable)objectValue);
                            }

                            break;
                        }
                    case TypeCode.String:
                        {
                            return ToDate((string)objectValue);
                        }
                }

                throw WrongDataType(objectValue);
            }
            #endregion

            #endregion

            #region ToTime

            #region ToTimeInMillis(TimeSpan)
            /// <summary>
            /// Retrieves the number of milliseconds since 
            /// <c>January 1, 1970, 00:00:00 GMT</c> represented
            /// by the <c>Hours</c>, <c>Minutes</c> and <c>Seconds</c>
            /// portions of the given <see cref="TimeSpan"/>,
            /// interpreted as an absolute duration.
            /// </summary>
            /// <param name="value">
            /// For which to retrieve the <c>Int64</c> value.
            /// </param>
            /// <returns>
            /// The corresponding value.
            /// </returns>
            public static long ToTimeInMillis(TimeSpan value)
            {
                value = value.Duration();

                JavaCalendar cal = HsqlConvert.GetJavaGregorianCalendar();

                cal.clear();
                cal.set(JavaCalendar.HOUR_OF_DAY, value.Hours);
                cal.set(JavaCalendar.MINUTE, value.Minutes);
                cal.set(JavaCalendar.SECOND, value.Seconds);

                return cal.getTimeInMillis();
            }
            #endregion

            #region ToTimeInMillis(DateTime)
            /// <summary>
            /// Retrieves the number of milliseconds since 
            /// <c>January 1, 1970, 00:00:00 GMT</c> represented
            /// by the time portion (<c>hh:mm:ss:</c>)
            /// of the given <see cref="System.DateTime"/>
            /// value.
            /// </summary>
            /// <param name="value">
            /// For which to retrieve the <c>Int64</c> value.
            /// </param>
            /// <returns>
            /// The corresponding value.
            /// </returns>
            public static long ToTimeInMillis(DateTime value)
            {
                JavaCalendar cal = HsqlConvert.GetJavaGregorianCalendar();

                cal.clear();
                cal.set(JavaCalendar.HOUR_OF_DAY, value.Hour);
                cal.set(JavaCalendar.MINUTE, value.Minute);
                cal.set(JavaCalendar.SECOND, value.Second);

                return cal.getTimeInMillis();
            }
            #endregion

            #region ToTime(DateTime)
            /// <summary>
            /// Retrieves the <see cref="java.sql.Time"/> value
            /// corresponding to the given <see cref="System.DateTime"/>
            /// value.
            /// </summary>
            /// <param name="value">
            /// For which to retrieve the <c>java.sql.Time</c> value.
            /// </param>
            /// <returns>
            /// The corresponding value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaTime ToTime(DateTime value)
            {
                long millis = ToTimeInMillis(value);

                return new JavaTime(millis);
            }
            #endregion

            #region ToTime(TimeSpan)
            /// <summary>
            /// Retrieves the <see cref="java.sql.Time"/> value
            /// corresponding to the given <see cref="System.TimeSpan"/>
            /// value.
            /// </summary>
            /// <param name="value">
            /// For which to retrieve the <c>java.sql.Time</c> value.
            /// </param>
            /// <returns>
            /// The corresponding value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaTime ToTime(TimeSpan value)
            {

                long millis = ToTimeInMillis(value);

                return new JavaTime(millis);
            }
            #endregion

            #region ToTime(string)
            /// <summary>            
            /// Converts the given <see cref="System.String"/>
            /// to an <c>SQL TIME</c> represented by
            /// a <c>java.sql.Time</c> instance.
            /// </summary>
            /// <remarks>
            /// The given <c>string</c> must be in a format
            /// accepted by 
            /// <c>org.hsqldb.HsqlDateTime.timeValue(string)</c>.
            /// </remarks>
            /// <param name="stringValue">
            /// To convert to a <c>java.sql.Time</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.sql.Time</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaTime ToTime(string stringValue)
            {
                return org.hsqldb.HsqlDateTime.timeValue(stringValue);
            }
            #endregion

            #region ToTime(INullable)
            /// <summary>
            /// Converts the given <see cref="INullable"/>
            /// to an <c>SQL TIME</c> value.
            /// value.
            /// </summary>
            /// <remarks>
            /// Handles <c>SqlDateTime</c>, <c>SqlString</c> and
            /// <c>SlqChars</c> types.
            /// </remarks>
            /// <param name="nullable">
            /// To convert to a <c>java.sql.Time</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.sql.Time</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaTime ToTime(INullable nullable)
            {
                if (nullable == null || nullable.IsNull)
                {
                    return null;
                }
                else if (nullable is SqlDateTime)
                {
                    return ToTime(((SqlDateTime)nullable).Value);
                }
                else if (nullable is SqlString)
                {
                    return ToTime(((SqlString)nullable).Value);
                }
                else if (nullable is SqlChars)
                {
                    return ToTime(((SqlChars)nullable).ToSqlString());
                }

                throw WrongDataType(nullable);
            } 
            #endregion

            #region ToTime(object)
            /// <summary>
            /// Converts the given <see cref="System.Object"/>
            /// value to an <c>SQL TIME</c> represented by
            /// a <see cref="java.sql.Time"/> instance.
            /// </summary>
            /// <remarks>
            /// <c>null</c>, <c>TypeCode.DBNull</c> and
            /// <c>TypeCode.Empty</c> values result in <c>null</c>.
            /// <c>char[]</c>, <c>DateTime</c>, <c>string</c>
            /// and <c>TimeSpan</c> are processed according to type.
            /// Values of any other type raise a wrong data type
            /// exception.
            /// </remarks>
            /// <param name="value">
            /// To convert to a <c>java.sql.Time</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.sql.Time</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaTime ToTime(object value)
            {
                if (value == null)
                {
                    return null;
                }

                TypeCode typeCode = Type.GetTypeCode(value.GetType());

                switch (typeCode)
                {
                    case TypeCode.DateTime:
                        {
                            return ToTime((DateTime)value);
                        }
                    case TypeCode.DBNull:
                        {
                            return null;
                        }
                    case TypeCode.Empty:
                        {
                            return null;
                        }
                    case TypeCode.Object:
                        {
                            if (value is TimeSpan)
                            {
                                return ToTime((TimeSpan)value);
                            }
                            else if (value is char[])
                            {
                                char[] chars = (char[])value;

                                if (chars.Length < 8)
                                {
                                    throw HsqlConvert.InvalidConversion(Types.TIME);
                                }

                                return ToTime(new string((char[])value, 0, 8));
                            }

                            break;
                        }
                    case TypeCode.String:
                        {
                            return ToTime((string)value);
                        }
                }

                throw WrongDataType(value);
            }
            #endregion

            #endregion

            #region ToTimestamp

            #region ToTimestampInMillis(TimeSpan)
            /// <summary>
            /// Converts the absolute duration represented by
            /// the given <see cref="System.TimeSpan"/>
            /// value to an <see cref="System.Int64"/> value
            /// representing the number of milliseconds since
            /// <c>January 1, 1970 00:00:00 GMT</c>.
            /// </summary>
            /// <param name="timeSpanValue">
            /// For which to retrieve the <c>Int64</c> value.
            /// </param>
            /// <returns>
            /// The corresponding value.
            /// </returns>
            public static long ToTimestampInMillis(TimeSpan timeSpanValue)
            {
                timeSpanValue = timeSpanValue.Duration();

                JavaGregorianCalendar cal = HsqlConvert.GetJavaGregorianCalendar();

                cal.clear();
                cal.add(JavaCalendar.DAY_OF_YEAR, timeSpanValue.Days);
                cal.add(JavaCalendar.HOUR_OF_DAY, timeSpanValue.Hours);
                cal.add(JavaCalendar.MINUTE, timeSpanValue.Minutes);
                cal.add(JavaCalendar.SECOND, timeSpanValue.Seconds);
                cal.add(JavaCalendar.MILLISECOND, timeSpanValue.Milliseconds);

                return cal.getTimeInMillis();
            }
            #endregion

            #region ToTimestampInMillis(DateTime)
            /// <summary>
            /// Converts the given <see cref="System.DateTime"/>
            /// value to an <see cref="System.Int64"/> value
            /// representing the number of milliseconds since
            /// <c>January 1, 1970 00:00:00 GMT</c>.
            /// </summary>
            /// <param name="dateTimeValue">
            /// To convert to an <c>Int64</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>Int64</c> value.
            /// </returns>
            public static long ToTimestampInMillis(DateTime dateTimeValue)
            {
                JavaGregorianCalendar cal = HsqlConvert.GetJavaGregorianCalendar();

                cal.clear();
                cal.set(JavaCalendar.YEAR, dateTimeValue.Year);
                cal.set(JavaCalendar.MONTH, dateTimeValue.Month - 1);
                cal.set(JavaCalendar.DATE, dateTimeValue.Day);
                cal.set(JavaCalendar.HOUR_OF_DAY, dateTimeValue.Hour);
                cal.set(JavaCalendar.MINUTE, dateTimeValue.Minute);
                cal.set(JavaCalendar.SECOND, dateTimeValue.Second);
                cal.set(JavaCalendar.MILLISECOND, dateTimeValue.Millisecond);

                return cal.getTimeInMillis();
            }
            #endregion

            #region ToTimestamp(DateTime)
            /// <summary>
            /// Converts the given <see cref="System.DateTime"/>
            /// value to an <c>SQL TIMESTAMP</c> value.
            /// </summary>
            /// <param name="dateTimeValue">
            /// To convert to a <c>java.sql.Timestamp</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.sql.Timestamp</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaTimestamp ToTimestamp(DateTime dateTimeValue)
            {
                long millis = ToTimestampInMillis(dateTimeValue);

                return new JavaTimestamp(millis);
            }
            #endregion

            #region ToTimestamp(TimeSpan)
            /// <summary>
            /// Converts the absolute duration represented by
            /// the given <see cref="System.TimeSpan"/>
            /// value to an <c>SQL TIMESTAMP</c> value,
            /// where Zero (0) duration is interpreted as
            /// <c>January 1, 1970, 00:00:00 GMT</c>
            /// </summary>
            /// <param name="timeSpanValue">
            /// To convert to a <c>java.sql.Timestamp</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.sql.Timestamp</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaTimestamp ToTimestamp(TimeSpan timeSpanValue)
            {
                long millis = ToTimestampInMillis(timeSpanValue);

                return new JavaTimestamp(millis);
            }
            #endregion

            #region ToTimestamp(string)
            /// <summary>
            /// Converts the given <see cref="System.String"/>
            /// value to an <c>SQL TIMESTAMP</c> represented
            /// by a <c>java.sql.Timestamp</c> instance.
            /// </summary>            
            /// <remarks>
            /// The given <c>stringValue</c> must be in a format
            /// accepted by 
            /// <c>org.hsqldb.HsqlDateTime.timestampValue(string)</c>
            /// </remarks>
            /// <param name="stringValue">
            /// To convert to a <c>java.sql.Timestamp</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.sql.Timestamp</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaTimestamp ToTimestamp(string stringValue)
            {
                return org.hsqldb.HsqlDateTime.timestampValue(stringValue);
            }
            #endregion

            #region ToTimestamp(INullable)
            /// <summary>
            /// Converts the given <see cref="INullable"/>
            /// to an <c>SQL TIMESTAMP</c> value.
            /// value.
            /// </summary>
            /// <remarks>
            /// Handles <c>SqlDateTime</c>, <c>SqlString</c> and
            /// <c>SlqChars</c> types.
            /// </remarks>
            /// <param name="nullable">
            /// To convert to a <c>java.sql.Timestamp</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.sql.Timestamp</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaTimestamp ToTimestamp(INullable nullable)
            {
                if (nullable == null || nullable.IsNull)
                {
                    return null;
                }
                else if (nullable is SqlDateTime)
                {
                    return ToTimestamp(((SqlDateTime)nullable).Value);
                }
                else if (nullable is SqlString)
                {
                    return ToTimestamp(((SqlString)nullable).Value);
                }
                else if (nullable is SqlChars)
                {
                    return ToTimestamp(((SqlChars)nullable).ToSqlString());
                }

                throw WrongDataType(nullable);
            } 
            #endregion

            #region ToTimestamp(object)
            /// <summary>
            /// Retrieves the <see cref="java.sql.Timestamp"/> value
            /// corresponding to the given <see cref="System.Object"/>
            /// value.
            /// </summary>
            /// <param name="value">
            /// For which to retrieve the <c>Timestamp</c> value.
            /// </param>
            /// <returns>
            /// The corresponding value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaTimestamp ToTimestamp(object value)
            {
                if (value == null)
                {
                    return null;
                }

                TypeCode typeCode = Type.GetTypeCode(value.GetType());

                switch (typeCode)
                {
                    case TypeCode.DateTime:
                        {
                            return ToTimestamp((DateTime)value);
                        }
                    case TypeCode.DBNull:
                        {
                            return null;
                        }
                    case TypeCode.Empty:
                        {
                            return null;
                        }
                    case TypeCode.Object:
                        {
                            if (value is TimeSpan)
                            {
                                return ToTimestamp((TimeSpan)value);
                            }
                            else if (value is char[])
                            {
                                return ToTimestamp(new string((char[])value));
                            }

                            break;
                        }
                    case TypeCode.String:
                        {
                            return ToTimestamp((string)value);
                        }
                }

                throw WrongDataType(value);
            }
            #endregion

            #endregion

            #region ToDecimal

            #region Fields
            private static readonly org.hsqldb.lib.IntKeyHashMap decimalLookup
                = new org.hsqldb.lib.IntKeyHashMap();
            #endregion

            #region ToDecimal(bool)
            /// <summary>
            /// Converts the given <see cref="System.Boolean"/>
            /// value to an <c>SQL DECIMAL</c> value.
            /// </summary>
            /// <remarks>
            /// This method is optimized so that it does not
            /// construct new <c>java.math.BigDecimal</c> instances.
            /// </remarks>
            /// <param name="boolValue">
            /// To convert to a <c>java.math.BigDecimal</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.math.BigDecimal</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaBigDecimal ToDecimal(bool boolValue)
            {
                return boolValue ? BIG_DECIMAL_1 : BIG_DECIMAL_0;
            }
            #endregion

            #region ToDecimal(byte)
            /// <summary>
            /// Converts the given <see cref="System.Byte"/>
            /// value to an <c>SQL DECIMAL</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in
            /// order to reduce memory consumption.
            /// </remarks>
            /// <param name="byteValue">
            /// To convert to a <c>java.math.BigDecimal</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.math.BigDecimal</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaBigDecimal ToDecimal(byte byteValue)
            {
                switch (byteValue)
                {
                    case 0:
                        {
                            return BIG_DECIMAL_0;
                        }
                    case 1:
                        {
                            return BIG_DECIMAL_1;
                        }
                    default:
                        {
                            JavaBigDecimal result = decimalLookup.get(byteValue) as JavaBigDecimal;

                            if (result == null)
                            {
                                result = new JavaBigDecimal(byteValue);

                                decimalLookup.put(byteValue, result);
                            }

                            return result;
                        }
                }
            }
            #endregion

            #region ToDecimal(sbyte)
            /// <summary>
            /// Converts the given <see cref="System.SByte"/>
            /// value to an <c>SQL DECIMAL</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in
            /// order to reduce memory consumption.
            /// </remarks>
            /// <param name="sbyteValue">
            /// To convert to a <c>java.math.BigDecimal</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.math.BigDecimal</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaBigDecimal ToDecimal(sbyte sbyteValue)
            {
                switch (sbyteValue)
                {
                    case 0:
                        {
                            return BIG_DECIMAL_0;
                        }
                    case 1:
                        {
                            return BIG_DECIMAL_1;
                        }
                    default:
                        {
                            JavaBigDecimal result
                                = decimalLookup.get(sbyteValue) as JavaBigDecimal;

                            if (result == null)
                            {
                                result = new JavaBigDecimal(sbyteValue);

                                decimalLookup.put(sbyteValue, result);
                            }

                            return result;
                        }
                }
            }
            #endregion

            #region ToDecimal(short)
            /// <summary>
            /// Converts the given <see cref="System.Int16"/>
            /// value to an <c>SQL DECIMAL</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in
            /// order to reduce memory consumption.
            /// </remarks>
            /// <param name="shortValue">
            /// To convert to a <c>java.math.BigDecimal</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.math.BigDecimal</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaBigDecimal ToDecimal(short shortValue)
            {
                switch (shortValue)
                {
                    case 0:
                        {
                            return BIG_DECIMAL_0;
                        }
                    case 1:
                        {
                            return BIG_DECIMAL_1;
                        }
                    default:
                        {
                            JavaBigDecimal result = decimalLookup.get(shortValue) as JavaBigDecimal;

                            if (result == null)
                            {
                                result = new JavaBigDecimal(shortValue);

                                decimalLookup.put(shortValue, result);
                            }

                            return result;
                        }
                }
            }
            #endregion

            #region ToDecimal(ushort)
            /// <summary>
            /// Converts the given <see cref="System.UInt16"/>
            /// value to an <c>SQL DECIMAL</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in
            /// order to reduce memory consumption.
            /// </remarks>
            /// <param name="ushortValue">
            /// To convert to a <c>java.math.BigDecimal</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.math.BigDecimal</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaBigDecimal ToDecimal(ushort ushortValue)
            {
                switch (ushortValue)
                {
                    case 0:
                        {
                            return BIG_DECIMAL_0;
                        }
                    case 1:
                        {
                            return BIG_DECIMAL_1;
                        }
                    default:
                        {
                            JavaBigDecimal result = decimalLookup.get(ushortValue) as JavaBigDecimal;

                            if (result == null)
                            {
                                result = new JavaBigDecimal(ushortValue);

                                decimalLookup.put(ushortValue, result);
                            }

                            return result;
                        }
                }
            }
            #endregion

            #region ToDecimal(int)
            /// <summary>
            /// Converts the given <see cref="System.Int32"/>
            /// value to an <c>SQL DECIMAL</c> value.
            /// </summary>
            /// <remarks>
            /// For values in the range <c>short.MinValue</c> to 
            /// <c>ushort.MaxValue</c>, this method is backed by
            /// a value pool in order to reduce memory consumption.
            /// </remarks>
            /// <param name="intValue">
            /// To convert to a <c>java.math.BigDecimal</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.math.BigDecimal</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaBigDecimal ToDecimal(int intValue)
            {
                switch (intValue)
                {
                    case 0:
                        {
                            return BIG_DECIMAL_0;
                        }
                    case 1:
                        {
                            return BIG_DECIMAL_1;
                        }
                    default:
                        {
                            if (intValue < short.MinValue
                                || intValue > ushort.MaxValue)
                            {
                                return new JavaBigDecimal(intValue);
                            }

                            JavaBigDecimal result = decimalLookup.get(intValue) as JavaBigDecimal;

                            if (result == null)
                            {
                                result = new JavaBigDecimal(intValue);

                                decimalLookup.put(intValue, result);
                            }

                            return result;
                        }
                }
            }
            #endregion

            #region ToDecimal(uint)
            /// <summary>
            /// Converts the given <see cref="System.UInt32"/>
            /// value to an <c>SQL DECIMAL</c> value.
            /// </summary>
            /// <remarks>
            /// For values in the range <c>0</c> to 
            /// <c>ushort.MaxValue</c>, this method is backed by
            /// a value pool in order to reduce memory consumption.
            /// </remarks>
            /// <param name="uintValue">
            /// To convert to a <c>java.math.BigDecimal</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.math.BigDecimal</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaBigDecimal ToDecimal(uint uintValue)
            {
                switch (uintValue)
                {
                    case 0:
                        {
                            return BIG_DECIMAL_0;
                        }
                    case 1:
                        {
                            return BIG_DECIMAL_1;
                        }
                    default:
                        {
                            if (uintValue > ushort.MaxValue)
                            {
                                return new JavaBigDecimal(uintValue);
                            }

                            int intValue = (int)uintValue;

                            JavaBigDecimal result
                                = decimalLookup.get(intValue) as JavaBigDecimal;

                            if (result == null)
                            {
                                result = new JavaBigDecimal(intValue);

                                decimalLookup.put(intValue, result);
                            }

                            return result;
                        }
                }
            }
            #endregion

            #region ToDecimal(long)
            /// <summary>
            /// Converts the given <see cref="System.Int64"/>
            /// value to an <c>SQL DECIMAL</c> value.
            /// </summary>
            /// <remarks>
            /// For values in the range <c>short.MinValue</c> to 
            /// <c>ushort.MaxValue</c>, this method is backed by
            /// a value pool in order to reduce memory consumption.
            /// </remarks>
            /// <param name="longValue">
            /// To convert to a <c>java.math.BigDecimal</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.math.BigDecimal</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaBigDecimal ToDecimal(long longValue)
            {
                switch (longValue)
                {
                    case 0L:
                        {
                            return BIG_DECIMAL_0;
                        }
                    case 1L:
                        {
                            return BIG_DECIMAL_1;
                        }
                    default:
                        {
                            if (longValue < short.MinValue
                                || longValue > ushort.MaxValue)
                            {
                                return new JavaBigDecimal(longValue);
                            }

                            int intValue = (int)longValue;

                            JavaBigDecimal result =
                                decimalLookup.get(intValue) as JavaBigDecimal;

                            if (result == null)
                            {
                                result = new JavaBigDecimal(intValue);

                                decimalLookup.put(intValue, result);
                            }

                            return result;
                        }
                }
            }
            #endregion

            #region ToDecimal(ulong)
            /// <summary>
            /// Converts the given <see cref="System.UInt64"/>
            /// value to an <c>SQL DECIMAL</c> value.
            /// </summary>
            /// <remarks>
            /// For values in the range <c>0</c> to 
            /// <c>ushort.MaxValue</c>, this method is backed by
            /// a value pool in order to reduce memory consumption.
            /// </remarks>
            /// <param name="ulongValue">
            /// To convert to a <c>java.math.BigDecimal</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.math.BigDecimal</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaBigDecimal ToDecimal(ulong ulongValue)
            {
                switch (ulongValue)
                {
                    case 0L:
                        {
                            return BIG_DECIMAL_0;
                        }
                    case 1L:
                        {
                            return BIG_DECIMAL_1;
                        }
                    default:
                        {
                            if (((ulong)short.MaxValue) < ulongValue)
                            {
                                return (ulong.MaxValue >= ulongValue)
                                           ? new JavaBigDecimal((long)ulongValue)
                                           : new JavaBigDecimal(ulongValue.ToString());
                            }

                            int intValue = (int)ulongValue;

                            JavaBigDecimal result =
                                decimalLookup.get(intValue) as JavaBigDecimal;

                            if (result == null)
                            {
                                result = new JavaBigDecimal(intValue);

                                decimalLookup.put(intValue, result);
                            }

                            return result;
                        }
                }
            }
            #endregion

            #region ToDecimal(float)
            /// <summary>
            /// Converts the given <see cref="System.Single"/>
            /// value to an <c>SQL DECIMAL</c> value.
            /// </summary>
            /// <param name="floatValue">
            /// To convert to a <c>java.math.BigDecimal</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.math.BigDecimal</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaBigDecimal ToDecimal(float floatValue)
            {
                if (floatValue == 0.0F)
                {
                    return BIG_DECIMAL_0;
                }
                else if (floatValue == 1.0F)
                {
                    return BIG_DECIMAL_1;
                }
                else
                {
                    return new JavaBigDecimal(floatValue);
                }
            }
            #endregion

            #region ToDecimal(double)
            /// <summary>
            /// Converts the given <see cref="System.Double"/>
            /// value to an <c>SQL DECIMAL</c> value.
            /// </summary>
            /// <param name="doubleValue">
            /// To convert to a <c>java.math.BigDecimal</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.math.BigDecimal</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaBigDecimal ToDecimal(double doubleValue)
            {
                if (doubleValue == 0.0D)
                {
                    return BIG_DECIMAL_0;
                }
                else if (doubleValue == 1.0D)
                {
                    return BIG_DECIMAL_1;
                }
                else
                {
                    return new JavaBigDecimal(doubleValue);
                }
            }
            #endregion

            #region ToDecimal(decimal)
            /// <summary>
            /// Converts the given <see cref="System.Decimal"/>
            /// value to an <c>SQL DECIMAL</c> value.
            /// </summary>
            /// <param name="decimalValue">
            /// To convert to a <c>java.math.BigDecimal</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.math.BigDecimal</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaBigDecimal ToDecimal(decimal decimalValue)
            {
                if (decimalValue == 0.0M)
                {
                    return BIG_DECIMAL_0;
                }
                else if (decimalValue == 1.0M)
                {
                    return BIG_DECIMAL_1;
                }
                else
                {
                    return new JavaBigDecimal(decimalValue.ToString());
                }
            }
            #endregion

            #region ToDecimal(string)
            /// <summary>
            /// Converts the given <see cref="System.String"/>
            /// value to an <c>SQL DECIMAL</c> value.
            /// </summary>
            /// <param name="stringValue">
            /// To convert to a <c>java.math.BigDecimal</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.math.BigDecimal</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaBigDecimal ToDecimal(string stringValue)
            {
                return new JavaBigDecimal(stringValue);
            }
            #endregion

            #region ToDecimal(char[])
            /// <summary>
            /// Converts the given character array value
            /// to an <c>SQL DECIMAL</c>
            /// </summary>
            /// <param name="charsValue">
            /// To convert to a <c>java.math.BigDecimal</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.math.BigDecimal</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaBigDecimal ToDecimal(char[] charsValue)
            {
                return new JavaBigDecimal(charsValue);
            }
            #endregion

            #region ToDecimal(INullable)
            /// <summary>
            /// Converts the given <see cref="INullable"/>
            /// to an <c>SQL DECIMAL</c> value.
            /// value.
            /// </summary>
            /// <remarks>
            /// Handles Sql Number, <c>SqlString</c>
            /// <c>SlqChars</c> and <c>SqlBoolean</c> types.
            /// </remarks>
            /// <param name="nullable">
            /// To convert to a <c>java.math.BigDecimal</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.math.BigDecimal</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaBigDecimal ToDecimal(INullable nullable)
            {
                if (nullable == null || nullable.IsNull)
                {
                    return null;
                }
                // most likely first
                if (nullable is SqlDecimal)
                {
                    return ToDecimal(((SqlDecimal)nullable).Value);
                }
                if (nullable is SqlDouble)
                {
                    return ToDecimal(((SqlDouble)nullable).Value);
                }
                if (nullable is SqlSingle)
                {
                    return ToDecimal(((SqlSingle)nullable).Value);
                }
                if (nullable is SqlMoney)
                {
                    return ToDecimal(((SqlMoney)nullable).Value);
                }
                // then the rest
                if (nullable is SqlInt64)
                {
                    return ToDecimal(((SqlInt64)nullable).Value);
                }
                if (nullable is SqlInt32)
                {
                    return ToDecimal(((SqlInt32)nullable).Value);
                }
                if (nullable is SqlInt16)
                {
                    return ToDecimal(((SqlInt16)nullable).Value);
                }
                if (nullable is SqlByte)
                {
                    return ToDecimal(((SqlByte)nullable).Value);
                }
                if (nullable is SqlBoolean)
                {
                    return ToDecimal(((SqlBoolean)nullable).Value);
                }
                if (nullable is SqlString)
                {
                    return ToDecimal(((SqlString)nullable).Value);
                }
                if (nullable is SqlChars)
                {
                    return ToDecimal(((SqlChars)nullable).Value);
                }

                throw HsqlConvert.WrongDataType(nullable);
            } 
            #endregion

            #region ToDecimal(object)
            /// <summary>
            /// Converts the given .NET <see cref="System.Object"/>
            /// value to an <c>SQL DECIMAL</c>
            /// </summary>
            /// <remarks>
            /// This method is not fully generalized; it is intended
            /// for conversions proceeding from the .NET type system to
            /// the HSQLDB type system, specifically from boxed
            /// <c>System.ValueType</c> instances and character sequences.
            /// For instance, passing an <c>java.math.BigDecimal</c>
            /// value will result in an invalid conversion exception,
            /// rather than the identity conversion.  This is in the
            /// interest of keeping this method as short as possible
            /// for its intended purpose.
            /// </remarks>
            /// <param name="objectValue">
            /// To convert to a <c>java.math.BigDecimal</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.math.BigDecimal</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaBigDecimal ToDecimal(object objectValue)
            {
                if (objectValue == null)
                {
                    return null;
                }

                TypeCode typeCode = Type.GetTypeCode(objectValue.GetType());

                switch (typeCode)
                {
                    case TypeCode.Boolean:
                        {
                            return ToDecimal((bool)objectValue);
                        }
                    case TypeCode.Byte:
                        {
                            return ToDecimal((byte)objectValue);
                        }
                    case TypeCode.Char:
                        {
                            return ToDecimal((ushort)objectValue);
                        }
                    case TypeCode.DateTime:
                        {
                            throw InvalidConversion(Types.TIMESTAMP);
                        }
                    case TypeCode.DBNull:
                        {
                            return null;
                        }
                    case TypeCode.Decimal:
                        {
                            return ToDecimal((decimal)objectValue);
                        }
                    case TypeCode.Double:
                        {
                            return ToDecimal((double)objectValue);
                        }
                    case TypeCode.Empty:
                        {
                            return null;
                        }
                    case TypeCode.Int16:
                        {
                            return ToDecimal((short)objectValue);
                        }
                    case TypeCode.Int32:
                        {
                            return ToDecimal((int)objectValue);
                        }
                    case TypeCode.Int64:
                        {
                            return ToDecimal((long)objectValue);
                        }
                    case TypeCode.Object:
                        {
                            char[] charsValue = objectValue as char[];

                            if (charsValue != null)
                            {
                                return ToDecimal(charsValue);
                            }

                            INullable nullable = objectValue as INullable;

                            if (nullable != null)
                            {
                                return ToDecimal(nullable);
                            }

                            break;
                        }
                    case TypeCode.SByte:
                        {
                            return ToDecimal((sbyte)objectValue);
                        }
                    case TypeCode.Single:
                        {
                            return ToDecimal((float)objectValue);
                        }
                    case TypeCode.String:
                        {
                            return ToDecimal((string)objectValue);
                        }
                    case TypeCode.UInt16:
                        {
                            return ToDecimal((ushort)objectValue);
                        }
                    case TypeCode.UInt32:
                        {
                            return ToDecimal((uint)objectValue);
                        }
                    case TypeCode.UInt64:
                        {
                            return ToDecimal((ulong)objectValue);
                        }
                }

                throw WrongDataType(objectValue);
            }
            #endregion

            #endregion

            #region ToDouble

            #region ToDouble(bool)
            /// <summary>
            /// Converts the given <see cref="System.Boolean"/>
            /// value to an <c>SQL DOUBLE</c> value.
            /// </summary>
            /// <param name="boolValue">
            /// To convert to a <c>java.lang.Double</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Double</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaDouble ToDouble(bool boolValue)
            {
                return new java.lang.Double(boolValue ? 1.0D : 0.0D);
            }
            #endregion

            #region ToDouble(byte)
            /// <summary>
            /// Converts the given <see cref="System.Byte"/>
            /// value to an <c>SQl DOUBLE</c> value.
            /// </summary>
            /// <param name="byteValue">
            /// To convert to a <c>java.lang.Double</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Double</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaDouble ToDouble(byte byteValue)
            {
                return new java.lang.Double(byteValue);
            }
            #endregion

            #region ToDouble(sbyte)
            /// <summary>
            /// Converts the given <see cref="System.SByte"/>
            /// value to an <c>SQL DOUBLE</c> value.
            /// </summary>
            /// <param name="sbyteValue">
            /// To convert to a <c>java.lang.Double</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Double</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaDouble ToDouble(sbyte sbyteValue)
            {
                return new java.lang.Double(sbyteValue);
            }
            #endregion

            #region ToDouble(short)
            /// <summary>
            /// Converts the given <see cref="System.Int16"/>
            /// value to an <c>SQl DOUBLE</c> value.
            /// </summary>
            /// <param name="shortValue">
            /// To convert to a <c>java.lang.Double</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Double</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaDouble ToDouble(short shortValue)
            {
                return new java.lang.Double(shortValue);
            }
            #endregion

            #region ToDouble(ushort)
            /// <summary>
            /// Converts the given <see cref="System.UInt16"/>
            /// value to an <c>SQL DOUBLE</c> value.
            /// </summary>
            /// <param name="ushortValue">
            /// To convert to a <c>java.lang.Double</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Double</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaDouble ToDouble(ushort ushortValue)
            {
                return new java.lang.Double(ushortValue);
            }
            #endregion

            #region ToDouble(int)
            /// <summary>
            /// Converts the given <see cref="System.Int32"/>
            /// value to an <c>SQL DOUBLE</c> value.
            /// </summary>
            /// <param name="intValue">
            /// To convert to a <c>java.lang.Double</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Double</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaDouble ToDouble(int intValue)
            {
                return new java.lang.Double(intValue);
            }
            #endregion

            #region ToDouble(uint)
            /// <summary>
            /// Converts the given <see cref="System.UInt32"/>
            /// value to an <c>SQL DOUBLE</c> value.
            /// </summary>
            /// <param name="uintValue">
            /// To convert to a <c>java.lang.Double</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Double</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaDouble ToDouble(uint uintValue)
            {
                return new java.lang.Double(uintValue);
            }
            #endregion

            #region ToDouble(long)
            /// <summary>
            /// Converts the given <see cref="System.Int64"/>
            /// value to an <c>SQL DOUBLE</c> value.
            /// </summary>
            /// <param name="longValue">
            /// To convert to a <c>java.lang.Double</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Double</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaDouble ToDouble(long longValue)
            {
                return new java.lang.Double(longValue);
            }
            #endregion

            #region ToDouble(ulong)
            /// <summary>
            /// Converts the given <see cref="System.UInt64"/>
            /// value to an <c>SQL DOUBLE</c> value.
            /// </summary>
            /// <param name="ulongValue">
            /// To convert to a <c>java.lang.Double</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Double</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaDouble ToDouble(ulong ulongValue)
            {
                try
                {
                    checked
                    {
                        return new java.lang.Double((double)ulongValue);
                    }
                }
                catch (Exception)
                {
                    throw NumericValueOutOfRange(ulongValue);
                }
            }
            #endregion

            #region ToDouble(decimal)
            /// <summary>
            /// Converts the given <see cref="System.Decimal"/>
            /// value to an <c>SQL DOUBLE</c> value.
            /// </summary>
            /// <param name="decimalValue">
            /// To convert to a <c>java.lang.Double</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Double</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaDouble ToDouble(decimal decimalValue)
            {
                try
                {
                    checked
                    {
                        return new java.lang.Double((double)decimalValue);
                    }
                }
                catch (Exception)
                {
                    throw NumericValueOutOfRange(decimalValue);
                }
            }
            #endregion

            #region ToDouble(string)
            /// <summary>
            /// Converts the given <see cref="System.String"/>
            /// value to an <c>SQL DOUBLE</c> value.
            /// </summary>
            /// <param name="stringValue">
            /// To convert to a <c>java.lang.Double</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Double</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaDouble ToDouble(string stringValue)
            {
                return new java.lang.Double(stringValue);
            }
            #endregion

            #region ToDouble(INullable)
            /// <summary>
            /// Converts the given <see cref="INullable"/>
            /// to an <c>SQL DOUBLE</c> value.
            /// </summary>
            /// <remarks>
            /// Handles Sql Number, <c>SqlString</c>
            /// <c>SlqChars</c> and <c>SqlBoolean</c> types.
            /// </remarks>
            /// <param name="nullable">
            /// To convert to a <c>java.lang.Double</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Double</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaDouble ToDouble(INullable nullable)
            {
                if (nullable == null || nullable.IsNull)
                {
                    return null;
                }
                // most likely first
                if (nullable is SqlDouble)
                {
                    return ToDouble(((SqlDouble)nullable).Value);
                }
                if (nullable is SqlSingle)
                {
                    return ToDouble(((SqlSingle)nullable).Value);
                }
                if (nullable is SqlMoney)
                {
                    return ToDouble(((SqlMoney)nullable).Value);
                }
                if (nullable is SqlDecimal)
                {
                    return ToDouble(((SqlDecimal)nullable).Value);
                }
                // then the rest
                if (nullable is SqlInt64)
                {
                    return ToDouble(((SqlInt64)nullable).Value);
                }
                if (nullable is SqlInt32)
                {
                    return ToDouble(((SqlInt32)nullable).Value);
                }
                if (nullable is SqlInt16)
                {
                    return ToDouble(((SqlInt16)nullable).Value);
                }
                if (nullable is SqlByte)
                {
                    return ToDouble(((SqlByte)nullable).Value);
                }
                if (nullable is SqlBoolean)
                {
                    return ToDouble(((SqlBoolean)nullable).Value);
                }
                if (nullable is SqlString)
                {
                    return ToDouble(((SqlString)nullable).Value);
                }
                if (nullable is SqlChars)
                {
                    return ToDouble(((SqlChars)nullable).Value);
                }

                throw HsqlConvert.WrongDataType(nullable);
            } 
            #endregion

            #region ToDouble(object)
            /// <summary>
            /// Converts the given .NET <see cref="System.Object"/>
            /// value to an <c>SQL DOUBLE</c> value.
            /// </summary>
            /// <remarks>
            /// This method is not fully generalized; it is intended
            /// for conversions proceeding from the .NET type system to
            /// the HSQLDB type system, specifically from boxed
            /// <c>System.ValueType</c> instances and character sequences.
            /// For instance, passing an <c>java.lang.Double</c>
            /// value will result in an invalid conversion exception,
            /// rather than the identity conversion.  This is in the
            /// interest of keeping this method as short as possible
            /// for its intended purpose.
            /// </remarks>
            /// <param name="objectValue">
            /// To convert to a <c>java.lang.Double</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Double</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaDouble ToDouble(object objectValue)
            {
                if (objectValue == null)
                {
                    return null;
                }

                TypeCode typeCode = Type.GetTypeCode(objectValue.GetType());

                switch (typeCode)
                {                        
                    case TypeCode.Boolean:
                        {
                            return ToDouble((bool)objectValue);
                        }
                    case TypeCode.Byte:
                        {
                            return ToDouble((byte)objectValue);
                        }
                    case TypeCode.Char:
                        {
                            return ToDouble((ushort)objectValue);
                        }
                    case TypeCode.DateTime:
                        {
                            throw InvalidConversion(Types.TIMESTAMP);
                        }
                    case TypeCode.DBNull:
                        {
                            return null;
                        }
                    case TypeCode.Decimal:
                        {
                            return ToDouble((decimal)objectValue);
                        }
                    case TypeCode.Empty:
                        {
                            return null;
                        }
                    case TypeCode.Int16:
                        {
                            return ToDouble((short)objectValue);
                        }
                    case TypeCode.Int32:
                        {
                            return ToDouble((int)objectValue);
                        }
                    case TypeCode.Int64:
                        {
                            return ToDouble((long)objectValue);
                        }
                    case TypeCode.Object:
                        {
                            IConvertible convertible = objectValue as IConvertible;

                            if (convertible != null)
                            {
                                return ToDouble(Convert.ToDouble(convertible));
                            }

                            INullable nullable = objectValue as INullable;

                            if (nullable != null)
                            {
                                return ToDouble(nullable);
                            }

                            char[] charsValue = objectValue as char[];

                            if (charsValue != null)
                            {
                                return ToDouble(new string(charsValue));
                            }

                            throw InvalidConversion(Types.OTHER);
                        }
                    case TypeCode.SByte:
                        {
                            return ToDouble((sbyte)objectValue);
                        }
                    case TypeCode.Single:
                        {
                            return ToDouble((float)objectValue);
                        }
                    case TypeCode.String:
                        {
                            return ToDouble((string)objectValue);
                        }
                    case TypeCode.UInt16:
                        {
                            return ToDouble((ushort)objectValue);
                        }
                    case TypeCode.UInt32:
                        {
                            return ToDouble((uint)objectValue);
                        }
                    case TypeCode.UInt64:
                        {
                            return ToDouble((ulong)objectValue);
                        }
                }

                throw WrongDataType(objectValue);
            }
            #endregion

            #endregion

            #region ToInteger

            #region ToInteger(bool)
            /// <summary>
            /// Converts the given <see cref="System.Boolean"/>
            /// value to an <c>SQL INTEGER</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="boolValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaInteger ToInteger(bool boolValue)
            {
                return ValuePool.getInt(boolValue ? 1 : 0);
            }
            #endregion

            #region ToInteger(byte)
            /// <summary>
            /// Converts the given <see cref="System.Byte"/>
            /// value to an <c>SQL INTEGER</c> value..
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="byteValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>jav.lang.Integer</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaInteger ToInteger(byte byteValue)
            {
                return ValuePool.getInt(byteValue);
            }
            #endregion

            #region ToInteger(sbyte)
            /// <summary>
            /// Converts the given <see cref="System.SByte"/>
            /// value to an <c>SQL INTEGER</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="sbyteValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaInteger ToInteger(sbyte sbyteValue)
            {
                return ValuePool.getInt(sbyteValue);
            }
            #endregion

            #region ToInteger(short)
            /// <summary>
            /// Converts the given <see cref="System.Int16"/>
            /// value to an <c>SQL INTEGER</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="shortValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaInteger ToInteger(short shortValue)
            {
                return ValuePool.getInt(shortValue);
            }
            #endregion

            #region ToInteger(ushort)
            /// <summary>
            /// Converts the given <see cref="System.UInt16"/>
            /// value to an <c>SQL INTEGER</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="ushortValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaInteger ToInteger(ushort ushortValue)
            {
                return ValuePool.getInt(ushortValue);
            }
            #endregion

            #region ToInteger(int)
            /// <summary>
            /// Converts the given <see cref="System.Int32"/>
            /// value to an <c>SQL INTEGER</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="intValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaInteger ToInteger(int intValue)
            {
                return ValuePool.getInt(intValue);
            }
            #endregion

            #region ToInteger(uint)
            /// <summary>
            /// Converts the given <see cref="System.UInt32"/>
            /// value to an <c>SQL INTEGER</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="uintValue">
            /// FTo convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaInteger ToInteger(uint uintValue)
            {
                if (uintValue <= (uint)int.MaxValue)
                {
                    return ValuePool.getInt((int)uintValue);
                }

                throw NumericValueOutOfRange(uintValue);
            }
            #endregion

            #region ToInteger(long)
            /// <summary>
            /// Converts the given <see cref="System.Int64"/>
            /// value to an <c>SQL INTEGER</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="longValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaInteger ToInteger(long longValue)
            {
                if (longValue <= int.MaxValue
                    && longValue >= int.MinValue)
                {
                    return ValuePool.getInt((int)longValue);
                }

                throw NumericValueOutOfRange(longValue);
            }
            #endregion

            #region ToInteger(ulong)
            /// <summary>
            /// Converts the given <see cref="System.UInt64"/>
            /// value to an <c>SQL INTEGER</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="ulongValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaInteger ToInteger(ulong ulongValue)
            {
                if (ulongValue <= (ulong)int.MaxValue)
                {
                    return ValuePool.getInt((int)ulongValue);
                }

                throw NumericValueOutOfRange(ulongValue);
            }
            #endregion

            #region ToInteger(float)
            /// <summary>
            /// Converts the given <see cref="System.Single"/>
            /// value to an <c>SQL INTEGER</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="floatValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaInteger ToInteger(float floatValue)
            {
                if (floatValue <= int.MaxValue
                    && floatValue >= int.MinValue)
                {
                    return ValuePool.getInt((int)floatValue);
                }

                throw NumericValueOutOfRange(floatValue);
            }
            #endregion

            #region ToInteger(double)
            /// <summary>
            /// Converts the given <see cref="System.Double"/>
            /// value to an <c>SQL INTEGER</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="doubleValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>           
            [CLSCompliant(false)]
            public static JavaInteger ToInteger(double doubleValue)
            {
                if (doubleValue <= int.MaxValue
                    && doubleValue >= int.MinValue)
                {
                    return ValuePool.getInt((int)doubleValue);
                }

                throw NumericValueOutOfRange(doubleValue);
            }
            #endregion

            #region ToInteger(decimal)
            /// <summary>
            /// Converts the given <see cref="System.Decimal"/>
            /// value to an <c>SQL INTEGER</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="decimalValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaInteger ToInteger(decimal decimalValue)
            {
                if (decimalValue <= int.MaxValue
                    && decimalValue >= int.MinValue)
                {
                    return ValuePool.getInt((int)decimalValue);
                }

                throw NumericValueOutOfRange(decimalValue);
            }
            #endregion

            #region ToInteger(string)
            /// <summary>
            /// Converts the given <see cref="System.String"/>
            /// value to an <c>SQL INTEGER</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="stringValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaInteger ToInteger(string stringValue)
            {
                return ParseInteger(stringValue);
            }
            #endregion

            #region ToInteger(INullable)
            /// <summary>
            /// Converts the given <see cref="INullable"/>
            /// to an <c>SQL INTEGER</c> value.
            /// </summary>
            /// <remarks>
            /// Handles Sql Number, <c>SqlString</c>
            /// <c>SlqChars</c> and <c>SqlBoolean</c> types.
            /// </remarks>
            /// <param name="nullable">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaInteger ToInteger(INullable nullable)
            {
                if (nullable == null || nullable.IsNull)
                {
                    return null;
                }
                // most likely first
                if (nullable is SqlInt32)
                {
                    return ToInteger(((SqlInt32)nullable).Value);
                }
                if (nullable is SqlInt64)
                {
                    return ToInteger(((SqlInt64)nullable).Value);
                }
                if (nullable is SqlInt16)
                {
                    return ToInteger(((SqlInt16)nullable).Value);
                }
                if (nullable is SqlByte)
                {
                    return ToInteger(((SqlByte)nullable).Value);
                }
                // then the rest
                if (nullable is SqlDouble)
                {
                    return ToInteger(((SqlDouble)nullable).Value);
                }
                if (nullable is SqlSingle)
                {
                    return ToInteger(((SqlSingle)nullable).Value);
                }
                if (nullable is SqlMoney)
                {
                    return ToInteger(((SqlMoney)nullable).Value);
                }
                if (nullable is SqlDecimal)
                {
                    return ToInteger(((SqlDecimal)nullable).Value);
                }
                if (nullable is SqlBoolean)
                {
                    return ToInteger(((SqlBoolean)nullable).Value);
                }
                if (nullable is SqlString)
                {
                    return ToInteger(((SqlString)nullable).Value);
                }
                if (nullable is SqlChars)
                {
                    return ToInteger(((SqlChars)nullable).Value);
                }

                throw HsqlConvert.WrongDataType(nullable);
            } 
            #endregion

            #region ToInteger(object)
            /// <summary>
            /// Converts the given .NET <see cref="System.Object"/>
            /// value to an <c>SQL INTEGER</c> value.
            /// </summary>
            /// <remarks>
            /// This method is not fully generalized; it is intended
            /// for conversions proceeding from the .NET type system to
            /// the HSQLDB type system, specifically from boxed
            /// <c>System.ValueType</c> instances and character sequences.
            /// For instance, passing an <c>java.lang.Integer</c>
            /// value will result in an invalid conversion exception,
            /// rather than the identity conversion.  This is in the
            /// interest of keeping this method as short as possible
            /// for its intended purpose.
            /// </remarks>
            /// <param name="objectValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaInteger ToInteger(object objectValue)
            {
                if (objectValue == null)
                {
                    return null;
                }

                TypeCode typeCode = Type.GetTypeCode(objectValue.GetType());

                switch (typeCode)
                {
                    case TypeCode.Boolean:
                        {
                            return ToInteger((bool)objectValue);
                        }
                    case TypeCode.Byte:
                        {
                            return ToInteger((byte)objectValue);
                        }
                    case TypeCode.Char:
                        {
                            return ToInteger((ushort)objectValue);
                        }
                    case TypeCode.DateTime:
                        {
                            throw HsqlConvert.InvalidConversion(Types.TIMESTAMP);
                        }
                    case TypeCode.DBNull:
                        {
                            return null;
                        }
                    case TypeCode.Decimal:
                        {
                            return ToInteger((decimal)objectValue);
                        }
                    case TypeCode.Empty:
                        {
                            return null;
                        }
                    case TypeCode.Int16:
                        {
                            return ToInteger((short)objectValue);
                        }
                    case TypeCode.Int32:
                        {
                            return ToInteger((int)objectValue);
                        }
                    case TypeCode.Int64:
                        {
                            return ToInteger((long)objectValue);
                        }
                    case TypeCode.Object:
                        {
                            IConvertible convertible = objectValue as IConvertible;

                            if (convertible != null)
                            {
                                return ToInteger(Convert.ToUInt32(convertible));
                            }

                            INullable nullable = objectValue as INullable;

                            if (nullable != null)
                            {
                                return ToInteger(nullable);
                            }

                            char[] charsValue = objectValue as char[];

                            if (charsValue != null)
                            {
                                return ToInteger(new string(charsValue));
                            }

                            throw HsqlConvert.InvalidConversion(Types.OTHER);
                        }
                    case TypeCode.SByte:
                        {
                            return ToInteger((sbyte)objectValue);
                        }
                    case TypeCode.Single:
                        {
                            return ToInteger((float)objectValue);
                        }
                    case TypeCode.String:
                        {
                            return ToInteger((string)objectValue);
                        }
                    case TypeCode.UInt16:
                        {
                            return ToInteger((ushort)objectValue);
                        }
                    case TypeCode.UInt32:
                        {
                            return ToInteger((uint)objectValue);
                        }
                    case TypeCode.UInt64:
                        {
                            return ToInteger((ulong)objectValue);
                        }
                }

                throw HsqlConvert.WrongDataType(objectValue);
            }
            #endregion

            #endregion

            #region ToReal

            #region ToReal(bool)
            /// <summary>
            /// Converts the given <see cref="System.Boolean"/>
            /// value to a <c>SQL REAL</c> value.
            /// </summary>
            /// <param name="boolValue">
            /// To convert to a <c>java.lang.Double</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Double</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaDouble ToReal(bool boolValue)
            {
                return new java.lang.Double(boolValue ? 1.0D : 0.0D);
            }
            #endregion

            #region ToReal(byte)
            /// <summary>
            /// Converts the given <see cref="System.Byte"/>
            /// value to an <c>SQL REAL</c> value.
            /// </summary>
            /// <param name="byteValue">
            /// T oconver to a <c>java.lang.Double</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Double</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaDouble ToReal(byte byteValue)
            {
                return new java.lang.Double(byteValue);
            }
            #endregion

            #region ToReal(sbyte)
            /// <summary>
            /// Converts the given <see cref="System.SByte"/>
            /// value to an <c>SQL REAL</c> value.
            /// </summary>
            /// <param name="sbyteValue">
            /// To conver to a <c>java.lang.Double</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Double</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaDouble ToReal(sbyte sbyteValue)
            {
                return new java.lang.Double(sbyteValue);
            }
            #endregion

            #region ToReal(short)
            /// <summary>
            /// Converts the given <see cref="System.Int16"/>
            /// value to an <c>SQL REAL</c> value.
            /// </summary>
            /// <param name="shortValue">
            /// To conver to a <c>java.lang.Double</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Double</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaDouble ToReal(short shortValue)
            {
                return new java.lang.Double(shortValue);
            }
            #endregion

            #region ToReal(ushort)
            /// <summary>
            /// Converts the given <see cref="System.UInt16"/>
            /// value to an <c>SQL REAL</c> value.
            /// </summary>
            /// <param name="ushortValue">
            /// To convert to a <c>java.lang.Double</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Double</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaDouble ToReal(ushort ushortValue)
            {
                return new java.lang.Double(ushortValue);
            }
            #endregion

            #region ToReal(int)
            /// <summary>
            /// Converts the given <see cref="System.Int32"/>
            /// value to an <c>SQL REAL</c> value.
            /// </summary>
            /// <param name="intValue">
            /// To convert to a <c>java.lang.Double</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Double</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaDouble ToReal(int intValue)
            {
                return new java.lang.Double(intValue);
            }
            #endregion

            #region ToReal(uint)
            /// <summary>
            /// Converts the given <see cref="System.UInt32"/>
            /// value to an <c>SQL REAL</c>.
            /// </summary>
            /// <param name="uintValue">
            /// To convert to a <c>java.lang.Double</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Double</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaDouble ToReal(uint uintValue)
            {
                return new java.lang.Double(uintValue);
            }
            #endregion

            #region ToReal(long)
            /// <summary>
            /// Converts the given <see cref="System.Int64"/>
            /// value to an <c>SQL REAL</c> value.
            /// </summary>
            /// <param name="longValue">
            /// To convert to a <c>java.lang.Double</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Double</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaDouble ToReal(long longValue)
            {
                return new java.lang.Double(longValue);
            }
            #endregion

            #region ToReal(ulong)
            /// <summary>
            /// Converts the given <see cref="System.UInt64"/>
            /// value to an <c>SQL REAL</c> value.
            /// </summary>
            /// <param name="value">
            /// To convert to a <c>java.lang.Double</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Double</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaDouble ToReal(ulong value)
            {
                return new java.lang.Double(value);
            }
            #endregion

            #region ToReal(float)
            /// <summary>
            /// Converts the given <see cref="System.Single"/>
            /// value to an <c>SQL REAL</c> value.
            /// </summary>
            /// <param name="floatValue">
            /// To convert to a <c>java.lang.Double</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Double</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaDouble ToReal(float floatValue)
            {
                return new java.lang.Double(floatValue);
            }
            #endregion

            #region ToReal(double)
            /// <summary>
            /// Converts the given <see cref="System.Double"/>
            /// value to an <c>SQL REAL</c> value.
            /// </summary>
            /// <param name="doubleValue">
            /// To convert to a <c>java.lang.Double</c> value in
            /// the range of <c>SQL REAL</c>.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Double</c> value.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the given value does not lie in the range of <c>SQl REAL</c>.
            /// </exception>
            [CLSCompliant(false)]
            public static JavaDouble ToReal(double doubleValue)
            {
                if (doubleValue <= float.MaxValue
                    && doubleValue >= float.MinValue)
                {
                    return new java.lang.Double(doubleValue);
                }
                else if (double.IsInfinity(doubleValue)
                         || double.IsNaN(doubleValue))
                {
                    return new java.lang.Double(doubleValue);
                }

                throw NumericValueOutOfRange(doubleValue);
            }
            #endregion

            #region ToReal(decimal)
            /// <summary>
            /// Converts the given <see cref="System.Decimal"/>
            /// value to an <c>SQL REAL</c> value.
            /// </summary>
            /// <param name="decimalValue">
            /// To convert to a <c>java.lang.Double</c> value
            /// in the rangte of <c>SQL REAL</c>.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Double</c>value.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the given value does not lie in the range of <c>SQL REAL</c>.
            /// </exception>
            [CLSCompliant(false)]
            public static JavaDouble ToReal(decimal decimalValue)
            {
                try
                {
                    checked
                    {
                        return new java.lang.Double((double)decimalValue);
                    }
                }
                catch (Exception)
                {
                    throw NumericValueOutOfRange(decimalValue);
                }
            }
            #endregion

            #region ToReal(string)
            /// <summary>
            /// Converts the given <see cref="System.String"/>
            /// value to an <c>SQL REAL</c> value.
            /// </summary>
            /// <param name="stringValue">
            /// To convert to a <c>java.lang.Double</c> value
            /// in the range of <c>SQL REAL</c>.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Double</c>value.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When a number format exception is encountered
            /// -or-
            /// When the result of the conversion does not lie in
            /// the range of <c>SQL REAL</c>.
            /// </exception>
            /// <exception cref="NullReferenceException">
            /// When <c>stringValue</c> is null.
            /// </exception>
            [CLSCompliant(false)]
            public static JavaDouble ToReal(string stringValue)
            {
                return ToReal(ParseDouble(stringValue));
            }
            #endregion

            #region ToReal(INullable)
            /// <summary>
            /// Converts the given <see cref="INullable"/>
            /// to an <c>SQL REAL</c> value.
            /// </summary>
            /// <remarks>
            /// Handles Sql Number, <c>SqlString</c>
            /// <c>SlqChars</c> and <c>SqlBoolean</c> types.
            /// </remarks>
            /// <param name="nullable">
            /// To convert to a <c>java.lang.Double</c> value
            /// in the range of <c>SQL REAL</c>.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Double</c> value
            /// in the range of <c>SQL REAL</c>.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the concrete type of the given <c>INullable</c> is not handled
            /// -or-
            /// When a number format exception is encountered
            /// -or-
            /// When the result of the conversion does not lie in
            /// the range of <c>SQL REAL</c>.
            /// </exception>
            [CLSCompliant(false)]
            public static JavaDouble ToReal(INullable nullable)
            {
                if (nullable == null || nullable.IsNull)
                {
                    return null;
                }
                // most likely first
                if (nullable is SqlSingle)
                {
                    return ToReal(((SqlSingle)nullable).Value);
                }
                if (nullable is SqlDouble)
                {
                    return ToReal(((SqlDouble)nullable).Value);
                }
                if (nullable is SqlMoney)
                {
                    return ToReal(((SqlMoney)nullable).Value);
                }
                if (nullable is SqlDecimal)
                {
                    return ToReal(((SqlDecimal)nullable).Value);
                }
                // then the rest
                if (nullable is SqlInt64)
                {
                    return ToReal(((SqlInt64)nullable).Value);
                }
                if (nullable is SqlInt32)
                {
                    return ToReal(((SqlInt32)nullable).Value);
                }
                if (nullable is SqlInt16)
                {
                    return ToReal(((SqlInt16)nullable).Value);
                }
                if (nullable is SqlByte)
                {
                    return ToReal(((SqlByte)nullable).Value);
                }
                if (nullable is SqlBoolean)
                {
                    return ToReal(((SqlBoolean)nullable).Value);
                }
                if (nullable is SqlString)
                {
                    return ToReal(((SqlString)nullable).Value);
                }
                if (nullable is SqlChars)
                {
                    return ToReal(((SqlChars)nullable).Value);
                }

                throw HsqlConvert.WrongDataType(nullable);
            }
            #endregion

            #region ToReal(object)
            /// <summary>
            /// Converts the given <see cref="System.Object"/>
            /// value to an <c>SQL REAL</c> value.
            /// </summary>
            /// <remarks>
            /// This method is not fully generalized; it is intended
            /// for conversions proceeding from the .NET type system to
            /// the HSQLDB type system, specifically from boxed
            /// <c>System.ValueType</c> instances and character sequences.
            /// For instance, passing an <c>java.lang.Double</c>
            /// value will result in an invalid conversion exception,
            /// rather than the identity conversion.  This is in the
            /// interest of keeping this method as short as possible
            /// for its intended purpose.
            /// </remarks>
            /// <param name="objectValue">
            /// To convert to a <c>java.lang.Double</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Double</c> value.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the result of the conversion does not lie in the range of <c>SQL REAL</c>
            /// -or-
            /// When there does not exist a conversion for the type of the given object.
            /// </exception>
            [CLSCompliant(false)]
            public static JavaDouble ToReal(object objectValue)
            {
                if (objectValue == null)
                {
                    return null;
                }

                TypeCode typeCode = Type.GetTypeCode(objectValue.GetType());

                switch (typeCode)
                {
                    case TypeCode.Boolean:
                        {
                            return ToReal((bool)objectValue);
                        }
                    case TypeCode.Byte:
                        {
                            return ToReal((byte)objectValue);
                        }
                    case TypeCode.Char:
                        {
                            return ToReal((ushort)objectValue);
                        }
                    case TypeCode.DateTime:
                        {
                            throw InvalidConversion(Types.TIMESTAMP);
                        }
                    case TypeCode.DBNull:
                        {
                            return null;
                        }
                    case TypeCode.Decimal:
                        {
                            return ToReal((decimal)objectValue);
                        }
                    case TypeCode.Empty:
                        {
                            return null;
                        }
                    case TypeCode.Int16:
                        {
                            return ToReal((short)objectValue);
                        }
                    case TypeCode.Int32:
                        {
                            return ToReal((int)objectValue);
                        }
                    case TypeCode.Int64:
                        {
                            return ToReal((long)objectValue);
                        }
                    case TypeCode.Object:
                        {
                            IConvertible convertible = objectValue as IConvertible;

                            if (convertible != null)
                            {
                                return ToReal(Convert.ToSingle(convertible));
                            }
                            else if (objectValue is char[])
                            {
                                return ToReal(new string((char[])objectValue));
                            }

                            throw InvalidConversion(Types.OTHER);
                        }
                    case TypeCode.SByte:
                        {
                            return ToReal((sbyte)objectValue);
                        }
                    case TypeCode.Single:
                        {
                            return ToReal((float)objectValue);
                        }
                    case TypeCode.String:
                        {
                            return ToReal((string)objectValue);
                        }
                    case TypeCode.UInt16:
                        {
                            return ToReal((ushort)objectValue);
                        }
                    case TypeCode.UInt32:
                        {
                            return ToReal((uint)objectValue);
                        }
                    case TypeCode.UInt64:
                        {
                            return ToReal((ulong)objectValue);
                        }
                }

                throw WrongDataType(objectValue);
            }
            #endregion

            #endregion

            #region ToSmallInt

            #region ToSmallInt(bool)
            /// <summary>
            /// Converts the given <see cref="System.Boolean"/>
            /// value to an <c>SQL SMALLINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="boolValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaInteger ToSmallInt(bool boolValue)
            {
                return ValuePool.getInt(boolValue ? 1 : 0);
            }
            #endregion

            #region ToSmallInt(byte)
            /// <summary>
            /// Converts the given <see cref="System.Byte"/>
            /// value to an <c>SQL SMALLINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="byteValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaInteger ToSmallInt(byte byteValue)
            {
                return ValuePool.getInt(byteValue);
            }
            #endregion

            #region ToSmallInt(sbyte)
            /// <summary>
            /// Converts the given <see cref="System.SByte"/>
            /// value to an <c>SQL SMALLINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="sbyteValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaInteger ToSmallInt(sbyte sbyteValue)
            {
                return ValuePool.getInt(sbyteValue);
            }
            #endregion

            #region ToSmallInt(short)
            /// <summary>
            /// Converts the given <see cref="System.Int16"/>
            /// value to an <c>SQL SMALLINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="shortValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaInteger ToSmallInt(short shortValue)
            {
                return ValuePool.getInt(shortValue);
            }
            #endregion

            #region ToSmallInt(ushort)
            /// <summary>
            /// Converts the given <see cref="System.UInt16"/>
            /// value to an <c>SQL SMALLINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="ushortValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the given value does not lie in 
            /// the range of <c>SQL SMALLINT</c>.
            /// </exception>
            [CLSCompliant(false)]
            public static JavaInteger ToSmallInt(ushort ushortValue)
            {
                if (ushortValue <= short.MaxValue)
                {
                    return ValuePool.getInt(ushortValue);
                }

                throw NumericValueOutOfRange(ushortValue);
            }
            #endregion

            #region ToSmallInt(int)
            /// <summary>
            /// Converts the given <see cref="System.Int32"/>
            /// value to an <c>SQL SMALLINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="intValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the given value does not lie
            /// in the range of <c>SQL SMALLINT</c>.
            /// </exception>
            [CLSCompliant(false)]
            public static JavaInteger ToSmallInt(int intValue)
            {
                if (intValue <= short.MaxValue
                    && intValue >= short.MinValue)
                {
                    return ValuePool.getInt(intValue);
                }

                throw NumericValueOutOfRange(intValue);
            }
            #endregion

            #region ToSmallInt(uint)
            /// <summary>
            /// Converts the given <see cref="System.UInt32"/>
            /// value to an <c>SQL SMALLINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="uintValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the given value does not lie
            /// in the range of <c>SQL SMALLINT</c>.
            /// </exception>
            [CLSCompliant(false)]
            public static JavaInteger ToSmallInt(uint uintValue)
            {
                if (uintValue <= short.MaxValue)
                {
                    return ValuePool.getInt((int)uintValue);
                }

                throw NumericValueOutOfRange(uintValue);
            }
            #endregion

            #region ToSmallInt(long)
            /// <summary>
            /// Converts the given <see cref="System.Int64"/>
            /// value to an <c>SQL SMALLINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="longValue">
            /// to convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the given value does not lie
            /// in the range of <c>SQL SMALLINT</c>.
            /// </exception>
            [CLSCompliant(false)]
            public static JavaInteger ToSmallInt(long longValue)
            {
                if (longValue <= short.MaxValue
                    && longValue >= short.MinValue)
                {
                    return ValuePool.getInt((int)longValue);
                }

                throw NumericValueOutOfRange(longValue);
            }
            #endregion

            #region ToSmallInt(ulong)
            /// <summary>
            /// Converts the given <see cref="System.UInt64"/>
            /// value to an <c>SQL SMALLINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="ulongValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the given value does not lie
            /// in the range of <c>SQL SMALLINT</c>.
            /// </exception>
            [CLSCompliant(false)]
            public static JavaInteger ToSmallInt(ulong ulongValue)
            {
                if (ulongValue <= (ulong)short.MaxValue)
                {
                    return ValuePool.getInt((int)ulongValue);
                }

                throw NumericValueOutOfRange(ulongValue);
            }
            #endregion

            #region ToSmallInt(float)
            /// <summary>
            /// Converts the given <see cref="System.Single"/>
            /// value to an <c>SQL SMALLINT</c>.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="floatValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the given value does not lie
            /// in the range of <c>SQL SMALLINT</c>.
            /// </exception>
            [CLSCompliant(false)]
            public static JavaInteger ToSmallInt(float floatValue)
            {
                if (floatValue <= short.MaxValue
                    && short.MinValue <= floatValue
                    && !(JavaFloat.isInfinite(floatValue) || JavaFloat.isNaN(floatValue)))
                {
                    return ValuePool.getInt((int)floatValue);
                }

                throw NumericValueOutOfRange(floatValue);
            }
            #endregion

            #region ToSmallInt(double)
            /// <summary>
            /// Converts the given <see cref="System.Double"/>
            /// value to an <c>SQL SMALLINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="doubleValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the given value does not lie
            /// in the range of <c>SQL SMALLINT</c>.
            /// </exception>
            [CLSCompliant(false)]
            public static JavaInteger ToSmallInt(double doubleValue)
            {
                if (doubleValue <= short.MaxValue
                    && short.MinValue <= doubleValue
                    && !(JavaDouble.isInfinite(doubleValue)
                        || JavaDouble.isNaN(doubleValue)))
                {
                    return ValuePool.getInt((int)doubleValue);
                }

                throw NumericValueOutOfRange(doubleValue);
            }
            #endregion

            #region ToSmallInt(decimal)
            /// <summary>
            /// Converts the given <see cref="System.Decimal"/>
            /// value to an <c>SQL SMALLINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="decimalValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the given value does not lie
            /// in the range of <c>SQL SMALLINT</c>.
            /// </exception>
            [CLSCompliant(false)]
            public static JavaInteger ToSmallInt(decimal decimalValue)
            {
                if (decimalValue <= short.MaxValue
                    && short.MinValue <= decimalValue)
                {
                    return ValuePool.getInt((int)decimalValue);
                }

                throw NumericValueOutOfRange(decimalValue);
            }
            #endregion

            #region ToSmallInt(string)
            /// <summary>
            /// Converts given <see cref="System.String"/>
            /// value to an <c>SQL SMALLINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="stringValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When a number format exception is encountered
            /// -or-
            /// When the result of the conversion does not lie in
            /// the range of <c>SQL SMALLINT</c>.
            /// </exception>
            /// <exception cref="NullReferenceException">
            /// When <c>stringValue</c> is null.
            /// </exception>
            [CLSCompliant(false)]
            public static JavaInteger ToSmallInt(string stringValue)
            {
                return ToSmallInt(FromDotNet.ParseInteger(stringValue).intValue());
            }
            #endregion

            #region ToSmallInt(object)
            /// <summary>
            /// Converts the given .NET <see cref="System.Object"/>
            /// value to an <c>SQL SMALLINT</c>.
            /// </summary>
            /// <remarks>
            /// This method is not fully generalized; it is intended
            /// for conversions proceeding from the .NET type system to
            /// the HSQLDB type system, specifically from boxed
            /// <c>System.ValueType</c> instances and character sequences.
            /// For instance, passing an <c>java.lang.Integer</c>
            /// value will result in an invalid conversion exception,
            /// rather than the identity conversion.  This is in the
            /// interest of keeping this method as short as possible
            /// for its intended purpose.
            /// </remarks>
            /// <param name="objectValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the result of the conversion does not lie in the range of <c>SQL SMALLINT</c>
            /// -or-
            /// When there does not exist a conversion for the type of the given object.
            /// </exception>
            [CLSCompliant(false)]
            public static JavaInteger ToSmallInt(object objectValue)
            {
                if (objectValue == null)
                {
                    return null;
                }

                TypeCode typeCode = Type.GetTypeCode(objectValue.GetType());

                switch (typeCode)
                {
                    case TypeCode.Boolean:
                        {
                            return ToSmallInt((bool)objectValue);
                        }
                    case TypeCode.Byte:
                        {
                            return ToSmallInt((byte)objectValue);
                        }
                    case TypeCode.Char:
                        {
                            return ToSmallInt((ushort)objectValue);
                        }
                    case TypeCode.DateTime:
                        {
                            throw InvalidConversion(Types.TIMESTAMP);
                        }
                    case TypeCode.DBNull:
                        {
                            return null;
                        }
                    case TypeCode.Decimal:
                        {
                            return ToSmallInt((decimal)objectValue);
                        }
                    case TypeCode.Double:
                        {
                            return ToSmallInt((double)objectValue);
                        }
                    case TypeCode.Empty:
                        {
                            return null;
                        }
                    case TypeCode.Int16:
                        {
                            return ToSmallInt((short)objectValue);
                        }
                    case TypeCode.Int32:
                        {
                            return ToSmallInt((int)objectValue);
                        }
                    case TypeCode.Int64:
                        {
                            return ToSmallInt((long)objectValue);
                        }
                    case TypeCode.Object:
                        {
                            IConvertible convertible = objectValue as IConvertible;

                            if (convertible != null)
                            {
                                return ToSmallInt(Convert.ToInt16(convertible));
                            }

                            char[] characterValue = objectValue as char[];


                            if (characterValue != null)
                            {
                                return ToSmallInt(new string(characterValue));
                            }

                            throw InvalidConversion(Types.OTHER);
                        }
                    case TypeCode.SByte:
                        {
                            return ToSmallInt((sbyte)objectValue);
                        }
                    case TypeCode.Single:
                        {
                            return ToSmallInt((float)objectValue);
                        }
                    case TypeCode.String:
                        {
                            return ToSmallInt((string)objectValue);
                        }
                    case TypeCode.UInt16:
                        {
                            return ToSmallInt((ushort)objectValue);
                        }
                    case TypeCode.UInt32:
                        {
                            return ToSmallInt((uint)objectValue);
                        }
                    case TypeCode.UInt64:
                        {
                            return ToSmallInt((ulong)objectValue);
                        }
                }

                throw WrongDataType(objectValue);
            }
            #endregion

            #endregion

            #region ToTinyInt

            #region ToTinyInt(bool)
            /// <summary>
            /// Converts the given <see cref="System.Boolean"/>
            /// value to an <c>SQL TINYINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="boolValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaInteger ToTinyInt(bool boolValue)
            {
                return ValuePool.getInt(boolValue ? 1 : 0);
            }
            #endregion

            #region ToTinyInt(byte)
            /// <summary>
            /// Converts the given <see cref="System.Byte"/>
            /// value to an <c>SQL TINEYINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="byteValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// If the given value does not lie in the range of <c>SQL TINYINT</c>.
            /// </exception>
            [CLSCompliant(false)]
            public static JavaInteger ToTinyInt(byte byteValue)
            {
                if (byteValue <= sbyte.MaxValue)
                {
                    return ValuePool.getInt(byteValue);
                }

                throw NumericValueOutOfRange(byteValue);
            }
            #endregion

            #region ToTinyInt(sbyte)
            /// <summary>
            /// Converts the given <see cref="System.SByte"/>
            /// value to an <c>SQL TINYINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="value">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaInteger ToTinyInt(sbyte value)
            {
                return ValuePool.getInt(value);
            }
            #endregion

            #region ToTinyInt(short)
            /// <summary>
            /// Converts the given <see cref="System.Int16"/>
            /// value to an <c>SQL TINYINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="shortValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// If the given value does not lie in the range of <c>SQL TINYINT</c>.
            /// </exception>
            [CLSCompliant(false)]
            public static JavaInteger ToTinyInt(short shortValue)
            {
                if (shortValue <= sbyte.MaxValue
                    && shortValue >= sbyte.MinValue)
                {
                    return ValuePool.getInt(shortValue);
                }

                throw NumericValueOutOfRange(shortValue);
            }
            #endregion

            #region ToTinyInt(ushort)
            /// <summary>
            /// Converts the given <see cref="System.UInt16"/>
            /// value to an <c>SQL TINYINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="ushortValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// If the given value does not lie in the range of <c>SQL TINYINT</c>.
            /// </exception>            
            [CLSCompliant(false)]
            public static JavaInteger ToTinyInt(ushort ushortValue)
            {
                if (ushortValue <= sbyte.MaxValue)
                {
                    return ValuePool.getInt(ushortValue);
                }

                throw NumericValueOutOfRange(ushortValue);
            }
            #endregion

            #region ToTinyInt(int)
            /// <summary>
            /// Converts the given <see cref="System.Int32"/>
            /// value to an <c>SQL TINYINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="intValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// If the given value does not lie in the range of <c>SQL TINYINT</c>.
            /// </exception>
            [CLSCompliant(false)]
            public static JavaInteger ToTinyInt(int intValue)
            {
                if (intValue <= sbyte.MaxValue
                    && intValue >= sbyte.MinValue)
                {
                    return ValuePool.getInt(intValue);
                }

                throw NumericValueOutOfRange(intValue);
            }
            #endregion

            #region ToTinyInt(uint)
            /// <summary>
            /// Converts the given <see cref="System.UInt32"/>
            /// value to an <c>SQL TINYINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="uintValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// If the given value does not lie in the range of <c>SQL TINYINT</c>.
            /// </exception>
            [CLSCompliant(false)]
            public static JavaInteger ToTinyInt(uint uintValue)
            {
                if (uintValue <= sbyte.MaxValue)
                {
                    return ValuePool.getInt((int)uintValue);
                }

                throw NumericValueOutOfRange(uintValue);
            }
            #endregion

            #region ToTinyInt(long)
            /// <summary>
            /// Converts the given <see cref="System.Int64"/>
            /// value to an <c>SQL TINYINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="longValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// If the given value does not lie in the range of <c>SQL TINYINT</c>.
            /// </exception>
            [CLSCompliant(false)]
            public static JavaInteger ToTinyInt(long longValue)
            {
                if (longValue <= sbyte.MaxValue
                    && longValue >= sbyte.MinValue)
                {
                    return ValuePool.getInt((int)longValue);
                }

                throw NumericValueOutOfRange(longValue);
            }
            #endregion

            #region ToTinyInt(ulong)
            /// <summary>
            /// Converts the given <see cref="System.UInt64"/>
            /// value to an <c>SQL TINYINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="ulongValue">
            /// To convert to an <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding value.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// If the given value does not lie in the range of <c>SQL TINYINT</c>.
            /// </exception>
            [CLSCompliant(false)]
            public static JavaInteger ToTinyInt(ulong ulongValue)
            {
                if (ulongValue <= (ulong)sbyte.MaxValue)
                {
                    return ValuePool.getInt((int)ulongValue);
                }

                throw NumericValueOutOfRange(ulongValue);
            }
            #endregion

            #region ToTinyInt(float)
            /// <summary>
            /// Converts the given <see cref="System.Single"/>
            /// value to an <c>SQL TINYINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="floatValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// If the given value does not lie in the range of <c>SQL TINYINT</c>.
            /// </exception>
            [CLSCompliant(false)]
            public static JavaInteger ToTinyInt(float floatValue)
            {
                if (floatValue <= sbyte.MaxValue
                    && floatValue >= sbyte.MinValue
                    && !(JavaFloat.isInfinite(floatValue) || JavaFloat.isNaN(floatValue)))
                {
                    return ValuePool.getInt((int)floatValue);
                }

                throw NumericValueOutOfRange(floatValue);
            }
            #endregion

            #region ToTinyInt(double)
            /// <summary>
            /// Converts the given <see cref="System.Double"/>
            /// value to an <c>SQL TINYINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="doubleValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// If the given value does not lie in the range of <c>SQL TINYINT</c>.
            /// </exception>
            [CLSCompliant(false)]
            public static JavaInteger ToTinyInt(double doubleValue)
            {
                if (doubleValue <= sbyte.MaxValue
                    && doubleValue >= sbyte.MinValue
                    && !(java.lang.Double.isInfinite(doubleValue) 
                        || java.lang.Double.isNaN(doubleValue)))
                {
                    return ValuePool.getInt((int)doubleValue);
                }

                throw NumericValueOutOfRange(doubleValue);
            }
            #endregion

            #region ToTinyInt(decimal)
            /// <summary>
            /// Converts the given <see cref="System.Decimal"/>
            /// value to an <c>SQL TINYINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="decimalValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// If the given value does not lie in the range of <c>SQL TINYINT</c>.
            /// </exception>
            [CLSCompliant(false)]
            public static JavaInteger ToTinyInt(decimal decimalValue)
            {
                if (decimalValue <= sbyte.MaxValue
                    && decimalValue >= sbyte.MinValue)
                {
                    return ValuePool.getInt((int)decimalValue);
                }

                throw NumericValueOutOfRange(decimalValue);
            }
            #endregion

            #region ToTinyInt(string)
            /// <summary>
            /// Converts the given <see cref="System.String"/>
            /// value to an <c>SQL TINYINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is backed by a value pool in order
            /// to reduce memory consumption.
            /// </remarks>
            /// <param name="stringValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When a number format exception is encountered
            /// -or-
            /// When the result of the conversion does not lie in
            /// the range of <c>SQL TINYINT</c>.
            /// </exception>
            /// <exception cref="NullReferenceException">
            /// When <c>stringValue</c> is null.
            /// </exception>
            [CLSCompliant(false)]
            public static JavaInteger ToTinyInt(string stringValue)
            {
                return ToTinyInt(JavaInteger.parseInt(stringValue));
            }
            #endregion

            #region ToTinyInt(object)
            /// <summary>
            /// Converts the given .NET <see cref="System.Object"/>
            /// value to an <c>SQL TINYINT</c> value.
            /// </summary>
            /// <remarks>
            /// This method is not fully generalized; it is intended
            /// for conversions proceeding from the .NET type system to
            /// the HSQLDB type system, specifically from boxed
            /// <c>System.ValueType</c> instances and character sequences.
            /// For instance, passing an <c>java.lang.Integer</c>
            /// value will result in an invalid conversion exception,
            /// rather than the identity conversion.  This is in the
            /// interest of keeping this method as short as possible
            /// for its intended purpose.
            /// </remarks>
            /// <param name="objectValue">
            /// To convert to a <c>java.lang.Integer</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>java.lang.Integer</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static JavaInteger ToTinyInt(object objectValue)
            {
                if (objectValue == null)
                {
                    return null;
                }

                TypeCode typeCode = Type.GetTypeCode(objectValue.GetType());

                switch (typeCode)
                {
                    case TypeCode.Boolean:
                        {
                            return ToTinyInt((bool)objectValue);
                        }
                    case TypeCode.Byte:
                        {
                            return ToTinyInt((byte)objectValue);
                        }
                    case TypeCode.Char:
                        {
                            return ToTinyInt((ushort)objectValue);
                        }
                    case TypeCode.DateTime:
                        {
                            throw InvalidConversion(Types.TIMESTAMP);
                        }
                    case TypeCode.DBNull:
                        {
                            return null;
                        }
                    case TypeCode.Decimal:
                        {
                            return ToTinyInt((decimal)objectValue);
                        }
                    case TypeCode.Empty:
                        {
                            return null;
                        }
                    case TypeCode.Int16:
                        {
                            return ToTinyInt((short)objectValue);
                        }
                    case TypeCode.Int32:
                        {
                            return ToTinyInt((int)objectValue);
                        }
                    case TypeCode.Int64:
                        {
                            return ToTinyInt((long)objectValue);
                        }
                    case TypeCode.Object:
                        {
                            IConvertible convertible = objectValue as IConvertible;

                            if (convertible != null)
                            {
                                return ToTinyInt(Convert.ToSByte(convertible));
                            }
                            else if (objectValue is char[])
                            {
                                return ToTinyInt(new string((char[])objectValue));
                            }

                            throw InvalidConversion(Types.OTHER);
                        }
                    case TypeCode.SByte:
                        {
                            return ToTinyInt((sbyte)objectValue);
                        }
                    case TypeCode.Single:
                        {
                            return ToTinyInt((float)objectValue);
                        }
                    case TypeCode.String:
                        {
                            return ToTinyInt((string)objectValue);
                        }
                    case TypeCode.UInt16:
                        {
                            return ToTinyInt((ushort)objectValue);
                        }
                    case TypeCode.UInt32:
                        {
                            return ToTinyInt((uint)objectValue);
                        }
                    case TypeCode.UInt64:
                        {
                            return ToTinyInt((ulong)objectValue);
                        }
                }

                throw WrongDataType(objectValue);
            }
            #endregion

            #endregion

            #region ToOther

            #region ToOther(bool)
            /// <summary>
            /// Converts the given <see cref="System.Boolean"/>
            /// value to an <c>SQL OBJECT</c> value.
            /// </summary>
            /// <param name="boolValue">
            /// To convert to an <c>SQL OBJECT</c> value.
            /// </param>
            /// <returns>
            /// The <c>SQL OBJECT</c> representation of value.
            /// </returns>
            [CLSCompliant(false)]
            public static SqlObject ToOther(bool boolValue)
            {
                return new SqlObject(boolValue ? TRUE : FALSE);
            }
            #endregion

            #region ToOther(byte)
            /// <summary>
            /// Converts the given <see cref="System.Byte"/>
            /// value to an <c>SQL OBJECT</c> value.
            /// </summary>
            /// <param name="byteValue">
            /// To convert to an <c>SQL OBJECT</c> value.
            /// </param>
            /// <returns>
            /// The <c>SQL OBJECT</c> representation of the value.
            /// </returns>
            [CLSCompliant(false)]
            public static SqlObject ToOther(byte byteValue)
            {
                return new SqlObject(ToSmallInt(byteValue));
            }
            #endregion

            #region ToOther(sbyte)
            /// <summary>
            /// Converts the given <see cref="System.SByte"/>
            /// value to an <c>SQL OBJECT</c> value.
            /// </summary>
            /// <param name="sbyteValue">
            /// To convert to an <c>SQL OBJECT</c> value.
            /// </param>
            /// <returns>
            /// The <c>SQL OBJECT</c> representation of the value.
            /// </returns>
            [CLSCompliant(false)]
            public static SqlObject ToOther(sbyte sbyteValue)
            {
                return new SqlObject(ToTinyInt(sbyteValue));
            }
            #endregion

            #region ToOther(short)
            /// <summary>
            /// Converts the given <see cref="System.Int16"/>
            /// value to an <c>SQL OBJECT</c> value.
            /// </summary>
            /// <param name="shortValue">
            /// To convert to an an <c>SQL OBJECT</c> value.
            /// </param>
            /// <returns>
            /// The <c>SQL OBJECT</c> representation of the value.
            /// </returns>
            [CLSCompliant(false)]
            public static SqlObject ToOther(short shortValue)
            {
                return new SqlObject(ToSmallInt(shortValue));
            }
            #endregion

            #region ToOther(ushort)
            /// <summary>
            /// Converts the given <see cref="System.UInt16"/>
            /// value to an <c>SQL OBJECT</c> value.
            /// </summary>
            /// <param name="ushortValue">
            /// To convert to an an <c>SQL OBJECT</c> value.
            /// </param>
            /// <returns>
            /// The <c>SQL OBJECT</c> representation of the value.
            /// </returns>
            [CLSCompliant(false)]
            public static SqlObject ToOther(ushort ushortValue)
            {
                return new SqlObject(ToInteger(ushortValue));
            }
            #endregion

            #region ToOther(int)
            /// <summary>
            /// Converts the given <see cref="System.Int32"/>
            /// value to an <c>SQL OBJECT</c> value.
            /// </summary>
            /// <param name="intValue">
            /// To convert to an an <c>SQL OBJECT</c> value.
            /// </param>
            /// <returns>
            /// The <c>SQL OBJECT</c> representation of the value.
            /// </returns>
            [CLSCompliant(false)]
            public static SqlObject ToOther(int intValue)
            {
                return new SqlObject(ToInteger(intValue));
            }
            #endregion

            #region ToOther(uint)
            /// <summary>
            /// Converts the given <see cref="System.UInt32"/>
            /// value to an <c>SQL OBJECT</c> value.
            /// </summary>
            /// <param name="uintValue">
            /// To convert to an an <c>SQL OBJECT</c> value.
            /// </param>
            /// <returns>
            /// The <c>SQL OBJECT</c> representation of the value.
            /// </returns>
            [CLSCompliant(false)]
            public static SqlObject ToOther(uint uintValue)
            {
                return new SqlObject(ToBigInt(uintValue));
            }
            #endregion

            #region ToOther(long)
            /// <summary>
            /// Converts the given <see cref="System.Int64"/>
            /// value to an <c>SQL OBJECT</c> value.
            /// </summary>
            /// <param name="longValue">
            /// To convert to an an <c>SQL OBJECT</c> value.
            /// </param>
            /// <returns>
            /// The <c>SQL OBJECT</c> representation of the value.
            /// </returns>
            [CLSCompliant(false)]
            public static SqlObject ToOther(long longValue)
            {
                return new SqlObject(ToBigInt(longValue));
            }
            #endregion

            #region ToOther(ulong)
            /// <summary>
            /// Converts the given <see cref="System.UInt64"/>
            /// value to an <c>SQL OBJECT</c> value.
            /// </summary>
            /// <param name="ulongValue">
            /// To convert to an an <c>SQL OBJECT</c> value.
            /// </param>
            /// <returns>
            /// The <c>SQL OBJECT</c> representation of the value.
            /// </returns>
            [CLSCompliant(false)]
            public static SqlObject ToOther(ulong ulongValue)
            {
                return new SqlObject(ToDecimal(ulongValue));
            }
            #endregion

            #region ToOther(float)
            /// <summary>
            /// Converts the given <see cref="System.Single"/>
            /// value to an <c>SQL OBJECT</c> value.
            /// </summary>
            /// <param name="floatValue">
            /// To convert to an an <c>SQL OBJECT</c> value.
            /// </param>
            /// <returns>
            /// The <c>SQL OBJECT</c> representation of the value.
            /// </returns>
            [CLSCompliant(false)]
            public static SqlObject ToOther(float floatValue)
            {
                return new SqlObject(ToReal(floatValue));
            }
            #endregion

            #region ToOther(double)
            /// <summary>
            /// Converts the given <see cref="System.Double"/>
            /// value to an <c>SQL OBJECT</c> value.
            /// </summary>
            /// <param name="doubleValue">
            /// To convert to an an <c>SQL OBJECT</c> value.
            /// </param>
            /// <returns>
            /// The <c>SQL OBJECT</c> representation of the value.
            /// </returns>
            [CLSCompliant(false)]
            public static SqlObject ToOther(double doubleValue)
            {
                return new SqlObject(ToDouble(doubleValue));
            }
            #endregion

            #region ToOther(decimal)
            /// <summary>
            /// Converts the given <see cref="System.Decimal"/>
            /// value to an <c>SQL OBJECT</c>.
            /// </summary>
            /// <param name="decimalValue">
            /// To convert to an an <c>SQL OBJECT</c> value.
            /// </param>
            /// <returns>
            /// The <c>SQL OBJECT</c> representation of the value.
            /// </returns>
            [CLSCompliant(false)]
            public static SqlObject ToOther(decimal decimalValue)
            {
                return new SqlObject(ToDecimal(decimalValue));
            }
            #endregion

            #region ToOther(string,bool)
            /// <summary>
            /// Converts the given <see cref="System.String"/> value
            /// to an <c>SQL OBJECT</c> value.
            /// </summary>
            /// <param name="stringValue">
            /// To convert to an an <c>SQL OBJECT</c> value.
            /// **must be non null**
            /// </param>
            /// <param name="isSerialForm">
            /// If set to <c>true</c>, treat the given string as a Base16
            /// (hex encoded) character sequence representing valid serial
            /// form; otherwise, treat as a serializable object.
            /// </param>
            /// <returns>
            /// The <c>SQL OBJECT</c> representation of the value.
            /// </returns>
            [CLSCompliant(false)]
            public static SqlObject ToOther(string stringValue, bool isSerialForm)
            {
                if (isSerialForm)
                {
                    byte[] bytes = StringConverter.hexToByte(stringValue);                    
                    
                    return new SqlObject(bytes);
                }
                else
                {
                    return new SqlObject(stringValue);
                }
            }
            #endregion

            #region ToOther(byte[],bool)
            /// <summary>
            /// Converts the given byte array value to an <c>SQL OBJECT</c> value.
            /// </summary>
            /// <param name="byteArrayValue">
            /// To convert to an an <c>SQL OBJECT</c> value.
            /// **must be non null**
            /// </param>
            /// <param name="isSerialForm">
            /// If set to <c>true</c>, treat the given byte array at though it
            /// already represents a valid serial form; otherwise, treat as a
            /// serializable object.
            /// </param>
            /// <returns>
            /// The <c>SQL OBJECT</c> representation of the value.
            /// </returns>
            [CLSCompliant(false)]
            public static SqlObject ToOther(byte[] byteArrayValue, bool isSerialForm)
            {
                return (isSerialForm)
                    ? new SqlObject((byte[])byteArrayValue)
                    : new SqlObject((object)byteArrayValue);
            }
            #endregion

            #region ToOther(DateTime)
            /// <summary>
            /// Converts the given <see cref="System.DateTime"/>
            /// value to an <c>SQL OBJECT</c> value.
            /// </summary>
            /// <param name="dateTimeValue">
            /// To convert to an an <c>SQL OBJECT</c> value.
            /// </param>
            /// <returns>
            /// The <c>SQL OBJECT</c> representation of the value.
            /// </returns>
            [CLSCompliant(false)]
            public static SqlObject ToOther(DateTime dateTimeValue)
            {
                return new SqlObject(ToTimestamp(dateTimeValue));
            }
            #endregion

            #region ToOther(TimeSpan)
            /// <summary>
            /// Converts the given <see cref="System.TimeSpan"/>
            /// value to an <c>SQL OBJECT</c> value.
            /// </summary>
            /// <param name="timeSpanValue">
            /// To convert to an an <c>SQL OBJECT</c> value.
            /// </param>
            /// <returns>
            /// The <c>SQL OBJECT</c> representation of the value.
            /// </returns>
            [CLSCompliant(false)]
            public static SqlObject ToOther(TimeSpan timeSpanValue)
            {
                return new SqlObject(ToTimestamp(timeSpanValue));
            }
            #endregion

            #region ToOther(Serializable)
            /// <summary>
            /// Converts the given <see cref="java.io.Serializable"/>
            /// value to an <c>SQL OBJECT</c> value.
            /// </summary>
            /// <param name="serValue">
            /// To convert to an an <c>SQL OBJECT</c> value.
            /// </param>
            /// <returns>
            /// The <c>SQL OBJECT</c> representation of the value.
            /// </returns>
            [CLSCompliant(false)]
            public static SqlObject ToOther(Serializable serValue)
            {
                object value = serValue.ToObject();

                return (value == null) ? null : new SqlObject(value);
            }
            #endregion

            #region ToOther(object)
            /// <summary>
            /// Converts the given .NET <see cref="System.Object"/>
            /// value to an <c>SQL OBJECT</c> value.
            /// </summary>
            /// <param name="value">
            /// To convert to an <c>SQL OBJECT</c> value.
            /// </param>
            /// <returns>
            /// The <c>SQL OBJECT</c> representation of the value.
            /// </returns>
            [CLSCompliant(false)]
            public static SqlObject ToOther(object value)
            {
                if (value == null)
                {
                    return null;
                }

                SqlObject dnoValue = value as SqlObject;

                if (dnoValue != null)
                {
                    return dnoValue;
                }

                JavaObject joValue = value as JavaObject;

                if (joValue != null)
                {
                    return new SqlObject(joValue.getBytes());
                }

                TypeCode typeCode = Type.GetTypeCode(value.GetType());

                switch (typeCode)
                {
                    case TypeCode.Boolean:
                        {
                            return ToOther((bool)value);
                        }
                    case TypeCode.Byte:
                        {
                            return ToOther((byte)value);
                        }
                    case TypeCode.Char:
                        {
                            return ToOther((ushort)value);
                        }
                    case TypeCode.DateTime:
                        {
                            return ToOther((DateTime)value);
                        }
                    case TypeCode.DBNull:
                        {
                            return null;
                        }
                    case TypeCode.Decimal:
                        {
                            return ToOther((decimal)value);
                        }
                    case TypeCode.Empty:
                        {
                            return null;
                        }
                    case TypeCode.Int16:
                        {
                            return ToOther((short)value);
                        }
                    case TypeCode.Int32:
                        {
                            return ToOther((int)value);
                        }
                    case TypeCode.Int64:
                        {
                            return ToOther((long)value);
                        }
                    case TypeCode.Object:
                        {
                            return  (value is TimeSpan)
                                ? ToOther((TimeSpan)value)
                                : (value is byte[])
                                ? ToOther((byte[])value, false)                            
                                : new SqlObject(value);
                        }
                    case TypeCode.SByte:
                        {
                            return ToOther((sbyte)value);
                        }
                    case TypeCode.Single:
                        {
                            return ToOther((float)value);
                        }
                    case TypeCode.String:
                        {
                            return ToOther((string)value, false);
                        }
                    case TypeCode.UInt16:
                        {
                            return ToOther((ushort)value);
                        }
                    case TypeCode.UInt32:
                        {
                            return ToOther((uint)value);
                        }
                    case TypeCode.UInt64:
                        {
                            return ToOther((ulong)value);
                        }
                }

                throw HsqlConvert.WrongDataType(value);
            }
            #endregion

            #endregion

            #region ToObject(object,int)
            /// <summary>
            /// Converts the given .NET <see cref="System.Object"/> value
            /// to a <c>java.lang.Object</c> value mapping to the given 
            /// HSQLDB SQL data type.
            /// </summary>
            /// <param name="objectValue">
            /// The .NET object value.
            /// </param>
            /// <param name="sqlType">
            /// The <c>org.hsqldb.Types</c> SQL data type code.
            /// </param>
            /// <returns>
            /// The corresponding SQL value as a <c>java.lang.Object</c> instance.
            /// </returns>
            public static object ToObject(object objectValue, int sqlType)
            {
                switch (sqlType)
                {
                    case Types.ARRAY:
                        {
                            throw HsqlConvert.InvalidConversion(Types.ARRAY);
                        }
                    case Types.BIGINT:
                        {
                            return FromDotNet.ToBigInt(objectValue);
                        }
                    case Types.BINARY:
                        {
                            return FromDotNet.ToBinary(objectValue);
                        }
                    case Types.BLOB:
                        {
                            throw HsqlConvert.InvalidConversion(Types.BLOB);
                        }
                    case Types.BOOLEAN:
                        {
                            return FromDotNet.ToBoolean(objectValue);
                        }
                    case Types.CHAR:
                        {
                            return FromDotNet.ToString(objectValue);
                        }
                    case Types.CLOB:
                        {
                            throw HsqlConvert.InvalidConversion(Types.CLOB);
                        }
                    case Types.DATALINK:
                        {
                            throw HsqlConvert.InvalidConversion(Types.DATALINK);
                        }
                    case Types.DATE:
                        {
                            return FromDotNet.ToDate(objectValue);
                        }
                    case Types.DECIMAL:
                        {
                            return FromDotNet.ToDecimal(objectValue);
                        }
                    case Types.DISTINCT:
                        {
                            throw HsqlConvert.InvalidConversion(Types.DISTINCT);
                        }
                    case Types.DOUBLE:
                        {
                            return FromDotNet.ToDouble(objectValue);
                        }
                    case Types.FLOAT:
                        {
                            return FromDotNet.ToDouble(objectValue);
                        }
                    case Types.INTEGER:
                        {
                            return FromDotNet.ToInteger(objectValue);
                        }
                    case Types.JAVA_OBJECT:
                        {
                            throw HsqlConvert.InvalidConversion(Types.JAVA_OBJECT);
                        }
                    case Types.LONGVARBINARY:
                        {
                            return FromDotNet.ToBinary(objectValue);
                        }
                    case Types.LONGVARCHAR:
                        {
                            return FromDotNet.ToString(objectValue);
                        }
                    case Types.NULL:
                        {
                            return null;
                        }
                    case Types.NUMERIC:
                        {
                            return FromDotNet.ToDecimal(objectValue);
                        }
                    case Types.OTHER:
                        {
                            return FromDotNet.ToOther(objectValue);
                        }
                    case Types.REAL:
                        {
                            return FromDotNet.ToReal(objectValue);
                        }
                    case Types.REF:
                        {
                            throw HsqlConvert.InvalidConversion(Types.REF);
                        }
                    case Types.SMALLINT:
                        {
                            return FromDotNet.ToSmallInt(objectValue);
                        }
                    case Types.STRUCT:
                        {
                            throw HsqlConvert.InvalidConversion(Types.STRUCT);
                        }
                    case Types.TIME:
                        {
                            return FromDotNet.ToTime(objectValue);
                        }
                    case Types.TIMESTAMP:
                        {
                            return FromDotNet.ToTimestamp(objectValue);
                        }
                    case Types.TINYINT:
                        {
                            return FromDotNet.ToTinyInt(objectValue);
                        }
                    case Types.VARBINARY:
                        {
                            return FromDotNet.ToBinary(objectValue);
                        }
                    case Types.VARCHAR:
                    case Types.VARCHAR_IGNORECASE:
                        {
                            return FromDotNet.ToString(objectValue);
                        }
                    case Types.XML:
                        {
                            throw HsqlConvert.InvalidConversion(Types.XML);
                        }
                }

                return objectValue;
            }
            #endregion

            #region Number Parsing

            #region ParseInteger(string)
            /// <summary>
            /// Parses the given value using <see cref="System.Int32.Parse(string)"/>,
            /// returning an equivalent <c>java.lang.Integer</c> instance retrieved
            /// from the HSQLDB value pool.
            /// </summary>
            /// <param name="value">The value to parse.</param>
            /// <returns>The number value parsed from the given string value</returns>
            /// <exception cref="HsqlDataSourceException">
            /// When a number format exception is encountered.
            /// </exception>
            [CLSCompliant(false)]
            public static JavaInteger ParseInteger(string value)
            {
                try
                {
                    return ValuePool.getInt(Int32.Parse(value));
                }
                catch (Exception e)
                {
                    throw new HsqlDataSourceException(Trace.error(
                        Trace.INVALID_CONVERSION, e.Message));
                }
            }
            #endregion

            #region ParseBigInt(string)
            /// <summary>
            /// Parses the given value using <see cref="System.Int64.Parse(string)"/>,
            /// returning an equivalent <c>java.lang.Long</c> instance retrieved
            /// from the HSQLDB value pool.
            /// </summary>
            /// <param name="value">The value to parse.</param>
            /// <returns>The number value parsed from the given string value</returns>
            /// <exception cref="HsqlDataSourceException">
            /// When a number format exception is encountered.
            /// </exception>
            [CLSCompliant(false)]
            public static JavaLong ParseBigInt(string value)
            {
                try
                {
                    return ValuePool.getLong(Int64.Parse(value));
                }
                catch (Exception e)
                {
                    throw new HsqlDataSourceException(Trace.error(
                        Trace.INVALID_CONVERSION, e.Message));
                }
            }
            #endregion

            #region ParseDouble(string)
            /// <summary>
            /// Parses the given value using <see cref="System.Double.Parse(string)"/>,
            /// returning an equivalent <c>java.lang.Double</c> instance retrieved
            /// from the HSQLDB value pool.
            /// </summary>
            /// <remarks>
            /// The legal input formats are those supported by
            /// <c>java.lang.Double.ParseDouble</c>
            /// </remarks>
            /// <param name="value">The value to parse.</param>
            /// <returns>The number value parsed from the given string value</returns>
            /// <exception cref="HsqlDataSourceException">
            /// When a number format exception is encountered.
            /// </exception>
            [CLSCompliant(false)]
            public static JavaDouble ParseDouble(string value)
            {
                try
                {
                    double dval = System.Double.Parse(value);
                    long lval = JavaDouble.doubleToLongBits(dval);
                    
                    return ValuePool.getDouble(lval);
                }
                catch (Exception e)
                {
                    throw new HsqlDataSourceException(Trace.error(
                        Trace.INVALID_CONVERSION, e.Message));
                }
            }
            #endregion

            #region ParseDecimal(string)
            /// <summary>
            /// Parses the given value using the constructor,
            /// <a href="http://java.sun.com/javase/6/docs/api/java/math/BigDecimal.html#BigDecimal(java.lang.String)">
            /// java.math.BigDecimal(string)</a>
            /// returning an equivalent <c>java.math.BigDecimal</c> instance retrieved
            /// from the HSQLDB value pool.
            /// </summary>
            /// <param name="value">The value to parse.</param>
            /// <returns>The number value parsed from the given string value</returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the underlying call <c>System.Decimal.Parse(string)</c>
            /// raises an exception.
            /// </exception>
            [CLSCompliant(false)]
            public static JavaBigDecimal ParseDecimal(string value)
            {
                try
                {
                    return ValuePool.getBigDecimal(new JavaBigDecimal(value));
                }
                catch (Exception e)
                {
                    throw new HsqlDataSourceException(
                        Trace.error(Trace.INVALID_CONVERSION, e.ToString()));
                }
            }
            #endregion

            #endregion
        }
        #endregion
    }
}