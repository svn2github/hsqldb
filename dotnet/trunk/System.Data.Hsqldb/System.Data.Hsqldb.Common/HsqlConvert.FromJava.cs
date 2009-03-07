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

using System.Text;
using System.Data.SqlTypes;

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
using HsqlTypes = org.hsqldb.Types;

using DotNetObject = System.Data.Hsqldb.Common.Sql.Types.SqlObject;
using HsqlBinary = org.hsqldb.types.Binary;
using JavaObject = org.hsqldb.types.JavaObject;
using JavaBigDecimal = java.math.BigDecimal;
using JavaBigInteger = java.math.BigInteger;
using JavaBoolean = java.lang.Boolean;
using JavaByte = java.lang.Byte;
using JavaCalendar = java.util.Calendar;
using JavaDouble = java.lang.Double;
using JavaFloat = java.lang.Float;
using JavaInteger = java.lang.Integer;
using JavaLong = java.lang.Long;
using JavaNumber = java.lang.Number;
using JavaShort = java.lang.Short;
using JavaDate = java.sql.Date;
using JavaTime = java.sql.Time;
using JavaTimestamp = java.sql.Timestamp;

using StringConverter = org.hsqldb.lib.StringConverter;
using ValuePool = org.hsqldb.store.ValuePool;

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
using System.Data.Hsqldb.Common.Sql;


#endregion

namespace System.Data.Hsqldb.Common
{
    #region HsqlConvert
    public static partial class HsqlConvert
    {
        #region From Java
        /// <summary>
        /// Provides conversions from HSQLDB Java type system
        /// representation to the corresponding CLR type system
        /// representation.
        /// </summary>
        public static class FromJava
        {
            #region ToTinyInt

            #region ToTinyInt(java.lang.Number)
            /// <summary>
            /// Converts the given Java <c>Number</c> object to a
            /// <see cref="System.SByte"/> representation in the range
            /// of <c>SQL TINYINT</c>.
            /// </summary>
            /// <param name="n">The Java <c>Number</c> to convert.</param>
            /// <returns>
            /// A <c>System.SByte</c> representation in the range of <c>SQL TINYINT</c>.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the given <c>Number</c> does not lie in the range of <c>SQL TINYINT</c>.
            /// </exception>
            /// <exception cref="NullReferenceException">
            /// When <c>n</c> is <c>null</c>.
            /// </exception>
            [CLSCompliant(false)]
            public static sbyte ToTinyInt(JavaNumber n)
            {
                if (n is JavaByte)
                {
                    return (sbyte) n.intValue();
                }

                const int max = 127;
                const int min = -128;

                if (n is JavaShort
                    || n is JavaInteger)
                {
                    int value = n.intValue();

                    if (max < value || value < min)
                    {
                        throw HsqlConvert.NumericValueOutOfRange(n);
                    }

                    return (sbyte) value;
                }
                else if (n is JavaLong)
                {
                    long value = n.longValue();

                    if (max < value || value < min)
                    {
                        throw HsqlConvert.NumericValueOutOfRange(n);
                    }

                    return (sbyte)value;
                }
                else if (n is JavaDouble || n is JavaFloat)
                {
                    double value = n.doubleValue();

                    if (JavaDouble.isNaN(value)
                        || JavaDouble.isInfinite(value)
                        || max < value
                        || value < min)
                    {
                        throw NumericValueOutOfRange(n);
                    }

                    return (sbyte)value;
                }

                JavaBigDecimal bigDecimalValue = n as JavaBigDecimal;

                if (bigDecimalValue != null)
                {
                    JavaBigInteger bi = bigDecimalValue.toBigInteger();

                    if (bi.compareTo(MAX_TINYINT) > 0
                        || bi.compareTo(MIN_TINYINT) < 0)
                    {
                        throw HsqlConvert.NumericValueOutOfRange(n);
                    }

                    return (sbyte) bi.intValue();
                }

                long longVaue = n.longValue();

                if (max < longVaue || longVaue < min)
                {
                    throw HsqlConvert.NumericValueOutOfRange(n);
                }

                return (sbyte)longVaue;
            }
            #endregion

            #region ToTinyInt(object)
            /// <summary>
            /// Converts the given Java <c>Object</c> to a
            /// <see cref="System.SByte"/> representation in the
            /// range of <c>SQL TINYINT</c>.
            /// </summary>
            /// <param name="o">The Java <c>Object</c> to convert.</param>
            /// <returns>
            /// A <c>System.SByte</c> representation in the range of <c>SQL TINYINT</c>.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the result of the conversion does not lie in the range of <c>SQL TINYINT</c>
            /// -or-
            /// When there does not exist a conversion for the type of given object.
            /// </exception>
            /// <exception cref="NullReferenceException">
            /// When <c>o</c> is <c>null</c>.
            /// </exception>
            [CLSCompliant(false)]
            public static sbyte ToTinyInt(object o)
            {
                JavaNumber numberValue = o as JavaNumber;

                if (numberValue != null)
                {
                    return ToTinyInt(numberValue);
                }

                JavaBoolean booleanValue = o as JavaBoolean;

                if (booleanValue != null)
                {
                    return (sbyte) (booleanValue.booleanValue() ? 1 : 0);
                }

                JavaObject objectValue = o as JavaObject;

                if (objectValue != null)
                {
                    bool isJavaObject;

                    object unwrapped = FromJava
                        .UnWrap(objectValue, out isJavaObject);

                    return (isJavaObject)
                        ? FromJava.ToTinyInt(unwrapped)
                        : (sbyte) FromDotNet.ToTinyInt(unwrapped).intValue();
                }

                if (o is java.util.Date
                    || o is HsqlBinary
                    || o is Array)
                {
                    throw HsqlConvert.WrongDataType(o);
                }

                string stringValue = o as string;

                if (stringValue == null)
                {
                    java.lang.Object jobj = o as java.lang.Object;

                    stringValue = (jobj == null) ? o.ToString() : jobj.toString();
                }

                int i = FromJava.ParseInteger(stringValue.Trim());

                if (127 < i || i < -128)
                {
                    throw HsqlConvert.NumericValueOutOfRange(i);
                }

                return (sbyte) i;
            }

            #endregion

            #endregion

            #region ToSmallInt

            #region ToSmallInt(java.lang.Number)
            /// <summary>
            /// Converts the given Java <c>Number</c> object to
            /// a <see cref="System.Int16"/> representation in the range
            /// of <c>SQL SMALLINT</c>.
            /// </summary>
            /// <param name="n">The Java <c>Number</c> to convert.</param>
            /// <returns>
            /// A <c>System.Int16</c> representation in the range of <c>SQL SMALLINT</c>.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the given <c>Number</c> does not lie in the range of <c>SQL SMALLINT</c>.
            /// </exception>
            /// <exception cref="NullReferenceException">
            /// When <c>n</c> is <c>null</c>.
            /// </exception>
            [CLSCompliant(false)]
            public static short ToSmallInt(JavaNumber n)
            {
                if (n is JavaByte
                    || n is JavaShort)
                {
                    return n.shortValue();
                }

                const int max = JavaShort.MAX_VALUE;
                const int min = JavaShort.MIN_VALUE;

                if (n is JavaInteger)
                {
                    int value = n.intValue();

                    if (max < value || value < min)
                    {
                        throw HsqlConvert.NumericValueOutOfRange(n);
                    }

                    return (short) value;
                }

                if (n is JavaLong)
                {
                    long value = n.longValue();

                    if (max < value || value < min)
                    {
                        throw HsqlConvert.NumericValueOutOfRange(n);
                    }

                    return (short) value;
                }

                if (n is JavaDouble || n is JavaFloat)
                {
                    double value = n.doubleValue();

                    if (JavaDouble.isNaN(value)
                        || JavaDouble.isInfinite(value)
                        || max < value
                        || value < min)
                    {
                        throw HsqlConvert.NumericValueOutOfRange(n);
                    }

                    return (short) value;
                }

                JavaBigDecimal bigDecimalValue = n as JavaBigDecimal;

                if (bigDecimalValue != null)
                {
                    JavaBigInteger bi = bigDecimalValue.toBigInteger();

                    if (bi.compareTo(MAX_SMALLINT) > 0
                        || bi.compareTo(MIN_SMALLINT) < 0)
                    {
                        throw HsqlConvert.NumericValueOutOfRange(n);
                    }

                    return bi.shortValue();
                }

                long l = n.longValue();

                if (max < l || l < min)
                {
                    throw HsqlConvert.NumericValueOutOfRange(n);
                }

                return (short) l;
            }
            #endregion

            #region ToSmallInt(object)
            /// <summary>
            /// Converts the given Java <c>Object</c> to a
            /// <see cref="System.Int16"/> representation in the
            /// range of <c>SQL SMALLINT</c>.
            /// </summary>
            /// <param name="o">
            /// The Java <c>Object</c> to convert.
            /// </param>
            /// <returns>
            /// A <c>System.Int16</c> representation in the range of <c>SQL SMALLINT</c>.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the result of the conversion does not lie in the range of <c>SQL SMALLINT</c>
            /// -or-
            /// When there does not exist a conversion for the type of given object.
            /// </exception>
            /// <exception cref="NullReferenceException">
            /// When <c>o</c> is <c>null</c>.
            /// </exception>
            public static short ToSmallInt(object o)
            {
                JavaNumber numberValue = o as JavaNumber;

                if (numberValue != null)
                {
                    return ToSmallInt(numberValue);
                }

                JavaBoolean booleanValue = o as JavaBoolean;

                if (booleanValue != null)
                {
                    return (short) (booleanValue.booleanValue() ? 1 : 0);
                }

                JavaObject objectValue = o as JavaObject;

                if (objectValue != null)
                {
                    bool isJavaObject;

                    object unwrapped = FromJava
                        .UnWrap(objectValue, out isJavaObject);

                    return (isJavaObject) ? FromJava.ToSmallInt(unwrapped)
                        : FromDotNet.ToSmallInt(unwrapped).shortValue();
                }

                if (o is java.util.Date || o is HsqlBinary || o is Array)
                {
                    throw HsqlConvert.WrongDataType(o);
                }

                string stringValue = o as string;

                if (stringValue == null)
                {
                    java.lang.Object jobj = o as java.lang.Object;

                    stringValue = (jobj == null) ? o.ToString() : jobj.toString();
                }

                int i = FromJava.ParseInteger(stringValue.Trim());

                if (JavaShort.MAX_VALUE < i || i < JavaShort.MIN_VALUE)
                {
                    throw HsqlConvert.NumericValueOutOfRange(i);
                }

                return (short) i;
            }
            #endregion

            #endregion

            #region ToInteger

            #region ToInteger(java.lang.Number)
            /// <summary>
            /// Converts the given Java <c>Number</c> object to
            /// a <see cref="System.Int32"/> representation in the range
            /// of <c>SQL INTEGER</c>.
            /// </summary>
            /// <param name="n">
            /// The Java <c>Number</c> to convert.
            /// </param>
            /// <returns>
            /// A <c>System.Int32</c> representation in the range of <c>SQL INTEGER</c>.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the given <c>Number</c> does not lie in the range of <c>SQL INTEGER</c>.
            /// </exception>
            /// <exception cref="NullReferenceException">
            /// When <c>n</c> is <c>null</c>.
            /// </exception>
            [CLSCompliant(false)]
            public static int ToInteger(JavaNumber n)
            {
                if (n is JavaInteger
                    || n is JavaShort
                    || n is JavaByte)
                {
                    return n.intValue();
                }

                const int max = JavaInteger.MAX_VALUE;
                const int min = JavaInteger.MIN_VALUE;

                if (n is JavaLong)
                {
                    long value = n.longValue();

                    if (max < value || value < min)
                    {
                        throw HsqlConvert.NumericValueOutOfRange(n);
                    }

                    return (int)value;
                }

                if (n is JavaDouble || n is JavaFloat)
                {
                    double value = n.doubleValue();

                    if (JavaDouble.isNaN(value)
                        || JavaDouble.isInfinite(value)
                        || max < value
                        || value < min)
                    {
                        throw HsqlConvert.NumericValueOutOfRange(n);
                    }

                    return (int)value;
                }

                JavaBigDecimal bigDecimalValue = n as JavaBigDecimal;

                if (bigDecimalValue != null)
                {
                    JavaBigInteger bi = bigDecimalValue.toBigInteger();

                    if (bi.compareTo(MAX_INTEGER) > 0
                        || bi.compareTo(MIN_INTEGER) < 0)
                    {
                        throw NumericValueOutOfRange(n);
                    }

                    return bi.intValue();
                }

                long l = n.longValue();

                if (max < l || l < min)
                {
                    throw HsqlConvert.NumericValueOutOfRange(n);
                }

                return (int)l;
            }
            #endregion

            #region ToInteger(object)
            /// <summary>
            /// Converts the given Java <c>Object</c> to
            /// a <see cref="System.Int32"/> representation in
            /// the range of <c>SQL INTEGER</c>.
            /// </summary>
            /// <param name="o">
            /// The Java <c>Object</c> to convert.
            /// </param>
            /// <returns>
            /// A <c>System.Int32</c> representation in the range of <c>SQL INTEGER</c>.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the result of the conversion does not lie in the range of <c>SQL INTEGER</c>
            /// -or-
            /// When there does not exist a conversion for the type of given object.
            /// </exception>
            /// <exception cref="NullReferenceException">
            /// /// When <c>o</c> is <c>null</c>.
            /// </exception>
            public static int ToInteger(object o)
            {
                JavaNumber numberValue = o as JavaNumber;

                if (numberValue != null)
                {
                    return ToInteger(numberValue);
                }

                JavaBoolean booleanValue = o as JavaBoolean;

                if (booleanValue != null)
                {
                    return booleanValue.booleanValue() ? 1 : 0;
                }

                JavaObject objectValue = o as JavaObject;

                if (objectValue != null)
                {
                    bool isJavaObject;

                    object unwrapped = FromJava.UnWrap(objectValue,
                        out isJavaObject);

                    return (isJavaObject) ? FromJava.ToInteger(unwrapped)
                        : FromDotNet.ToInteger(unwrapped).intValue();
                }

                if (o is java.util.Date || o is HsqlBinary || o is Array)
                {
                    throw HsqlConvert.WrongDataType(o);
                }

                string stringValue = o as string;

                if (stringValue == null)
                {
                    java.lang.Object jobj = o as java.lang.Object;

                    stringValue = (jobj == null) ? o.ToString() : jobj.toString();
                }

                return FromJava.ParseInteger(stringValue.Trim());
            }

            #endregion

            #endregion

            #region ToBigInt

            #region ToBigInt(java.lang.Number)
            /// <summary>
            /// Converts the given Java <c>Number</c> to a 
            /// <see cref="System.Int64"/> representation in
            /// the range of <c>SQL BIGINT</c>.
            /// </summary>
            /// <param name="n">The Java <c>Number</c> to convert.</param>
            /// <returns>
            /// A <c>System.Int64</c> representation in the range of <c>SQL BIGINT</c>.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the given <c>Number</c> does not lie in the range of
            /// <c>SQL BIGINT</c>.
            /// </exception>
            /// <exception cref="NullReferenceException">
            /// When <c>n</c> is <c>null</c>.
            /// </exception>
            [CLSCompliant(false)]
            public static long ToBigInt(JavaNumber n)
            {
                if (n is JavaByte
                    || n is JavaShort
                    || n is JavaInteger
                    || n is JavaLong)
                {
                    return n.longValue();
                }

                const long max = JavaLong.MAX_VALUE;
                const long min = JavaLong.MIN_VALUE;

                if (n is JavaDouble || n is JavaFloat)
                {
                    double value = n.doubleValue();

                    if (JavaDouble.isNaN(value)
                        || JavaDouble.isInfinite(value)
                        || max < value
                        || value < min)
                    {
                        throw HsqlConvert.NumericValueOutOfRange(n);
                    }

                    return (long)value;
                }

                JavaBigDecimal bigDecimalValue = n as JavaBigDecimal;

                if (bigDecimalValue != null)
                {
                    JavaBigInteger bi = bigDecimalValue.toBigInteger();

                    if (bi.compareTo(MAX_BIGINT) > 0
                        || bi.compareTo(MIN_BIGINT) < 0)
                    {
                        throw HsqlConvert.NumericValueOutOfRange(n);
                    }

                    return bi.longValue();
                }

                return n.longValue();
            }
            #endregion

            #region ToBigInt(object)
            /// <summary>
            /// Converts the given Java <c>Object</c> to a 
            /// <see cref="System.Int64"/> representation in
            /// the range of <c>SQL BIGINT</c>.
            /// </summary>
            /// <param name="o">The Java <c>Object</c> to convert.</param>
            /// <returns>
            /// A <c>System.Int64</c> representation in the range of <c>SQL BIGINT</c>.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the result of the conversion will not lie in the range of <c>SQL BIGINT</c>
            /// -or-
            /// When there does not exist a conversion for the type of given object.
            /// </exception>
            /// <exception cref="NullReferenceException">
            /// When <c>o</c> is <c>null</c>.
            /// </exception>
            public static long ToBigInt(object o)
            {
                JavaNumber numberValue = o as JavaNumber;

                if (numberValue != null)
                {
                    return ToBigInt(numberValue);
                }

                JavaBoolean booleanValue = o as JavaBoolean;

                if (booleanValue != null)
                {
                    return booleanValue.booleanValue() ? 1L : 0L;
                }

                JavaObject objectValue = o as JavaObject;

                if (objectValue != null)
                {
                    bool isJavaObject;

                    object unwrapped = FromJava.UnWrap(objectValue,
                        out isJavaObject);

                    return (isJavaObject) ? FromJava.ToBigInt(unwrapped)
                        : FromDotNet.ToBigInt(unwrapped).longValue();
                }

                if (o is java.util.Date || o is HsqlBinary || o is Array)
                {
                    throw HsqlConvert.WrongDataType(o);
                }

                string stringValue = o as string;

                if (stringValue == null)
                {
                    java.lang.Object jobj = o as java.lang.Object;

                    stringValue = (jobj == null) ? o.ToString() : jobj.toString();
                }

                return FromJava.ParseBigInt(stringValue.Trim());
            }
            #endregion

            #endregion

            #region ToDouble

            #region ToDouble(java.lang.Number)
            /// <summary>
            /// Converts the given Java <c>Number</c> to a
            /// <see cref="System.Double"/>
            /// representation in the range of <c>SQL DOUBLE</c>.
            /// </summary>
            /// <param name="n">The Java <c>Number</c> to convert.</param>
            /// <returns>
            /// A <c>System.Double</c> representation in the range of <c>SQL DOUBLE</c>.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the given <c>Number</c> does not lie in the range of
            /// <c>SQL DOUBLE</c>.
            /// </exception>
            /// <exception cref="NullReferenceException">
            /// When <c>n</c> is <c>null</c>.
            /// </exception>
            [CLSCompliant(false)]
            public static double ToDouble(JavaNumber n)
            {
                JavaBigDecimal bigDecimalValue = n as JavaBigDecimal;

                if (bigDecimalValue != null)
                {
                    if (bigDecimalValue.compareTo(MAX_DOUBLE) > 0
                        || bigDecimalValue.compareTo(MIN_DOUBLE) < 0)
                    {
                        throw HsqlConvert.NumericValueOutOfRange(n);
                    }

                    return bigDecimalValue.doubleValue();
                }

                return n.doubleValue();
            }
            #endregion

            #region ToDouble(object)
            /// <summary>
            /// Converts the given Java <c>Object</c> to a
            /// <see cref="System.Double"/> representation
            /// in the range of SQL DOUBLE.
            /// </summary>
            /// <param name="o">The Java <c>Object</c> to convert.</param>
            /// <returns>
            /// A <c>System.Double</c> representation in the range of <c>SQL DOUBLE</c>.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the result of the conversion does not lie in the range of <c>SQL DOUBLE</c>
            /// -or-
            /// When there does not exist a conversion for the type of the given object.
            /// </exception>
            /// <exception cref="NullReferenceException">
            /// When <c>o</c> is <c>null</c>.
            /// </exception>
            public static double ToDouble(object o)
            {
                JavaNumber numberValue = o as JavaNumber;

                if (numberValue != null)
                {
                    return ToDouble(numberValue);
                }

                JavaBoolean booleanValue = o as JavaBoolean;

                if (booleanValue != null)
                {
                    return booleanValue.booleanValue() ? 1D : 0D;
                }

                JavaObject objectValue = o as JavaObject;

                if (objectValue != null)
                {
                    bool isJavaObject;

                    object unwrapped = FromJava.UnWrap(objectValue,
                        out isJavaObject);

                    return (isJavaObject) ? FromJava.ToDouble(unwrapped)
                        : FromDotNet.ToDouble(unwrapped).doubleValue();
                }

                if (o is java.util.Date || o is HsqlBinary || o is Array)
                {
                    throw HsqlConvert.WrongDataType(o);
                }

                string stringValue = o as string;

                if (stringValue == null)
                {
                    java.lang.Object jobj = o as java.lang.Object;

                    stringValue = (jobj == null) ? o.ToString() : jobj.toString();
                }

                return FromJava.ParseDouble(stringValue.Trim());
            }
            #endregion

            #endregion

            #region ToReal

            #region ToReal(java.lang.Number)
            /// <summary>
            /// Converts the given Java <c>Number</c> object to a 
            /// <see cref="System.Single"/> representation
            /// in the range of <c>SQL REAL</c>.
            /// </summary>
            /// <param name="n">The Java <c>Number</c> to convert.</param>
            /// <returns>
            /// A <c>System.Single</c> representation in the range of <c>SQL REAL</c>.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the given Number does not lie in the range of <c>SQL REAL</c>.
            /// </exception>
            /// <exception cref="NullReferenceException">
            /// When <c>n</c> is <c>null</c>.
            /// </exception>
            [CLSCompliant(false)]
            public static float ToReal(JavaNumber n)
            {
                JavaBigDecimal bigDecimalValue = n as JavaBigDecimal;

                if (bigDecimalValue != null)
                {
                    if (bigDecimalValue.compareTo(MAX_REAL) > 0
                        || bigDecimalValue.compareTo(MIN_REAL) < 0)
                    {
                        throw NumericValueOutOfRange(n);
                    }

                    return bigDecimalValue.floatValue();
                }

                double d = n.doubleValue();

                if (JavaFloat.MAX_VALUE < d
                    || JavaFloat.MIN_VALUE > d)
                {
                    throw HsqlConvert.NumericValueOutOfRange(n);
                }

                return (float) d;
            }
            #endregion

            #region ToReal(object)
            /// <summary>
            /// Converts the given Java <c>Object</c> to a
            /// <see cref="System.Single"/> representation
            /// in the range of <c>SQL REAL</c>.
            /// </summary>
            /// <param name="o">The Java <c>Object</c> to convert.</param>
            /// <returns>
            /// A <c>System.Single</c> representation in the range of <c>SQL REAL</c>.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the result of the conversion does not lie in the range of SQL REAL
            /// -or-
            /// When there does not exist a conversion for the type of given object.
            /// </exception>
            /// <exception cref="NullReferenceException">
            /// When <c>o</c> is <c>null</c>.
            /// </exception>
            public static float ToReal(object o)
            {
                JavaNumber numberValue = o as JavaNumber;

                if (numberValue != null)
                {
                    return ToReal(numberValue);
                }

                JavaBoolean booleanValue = o as JavaBoolean;

                if (booleanValue != null)
                {
                    return booleanValue.booleanValue() ? 1F : 0F;
                }

                JavaObject objectValue = o as JavaObject;

                if (objectValue != null)
                {
                    bool isJavaObject;

                    object unwrapped = FromJava
                        .UnWrap(objectValue, out isJavaObject);

                    return (isJavaObject)
                        ? FromJava.ToReal(unwrapped)
                        : FromDotNet.ToReal(unwrapped).floatValue();
                }

                if (o is java.util.Date
                    || o is HsqlBinary
                    || o is Array)
                {
                    throw WrongDataType(o);
                }

                string stringValue = o as string;

                if (stringValue == null)
                {
                    stringValue = o.ToString();
                }

                double doubleValue = ParseDouble(stringValue.Trim());

                if (JavaFloat.MAX_VALUE < doubleValue
                    || JavaFloat.MIN_VALUE > doubleValue)
                {
                    throw NumericValueOutOfRange(doubleValue);
                }

                return (float) doubleValue;
            }
            #endregion

            #endregion

            #region ToDecimal

            #region ToDecimal(java.math.BigDecimal)
            /// <summary>
            /// Converts the given Java <c>BigDecimal</c>
            /// to a <c>System.Decimal</c> representation.
            /// </summary>
            /// <remarks>
            /// This conversion is not ONTO. For <c>BigDecimal</c> values that may lie
            /// outside the range of <c>System.Decimal</c>, it is recommended
            /// to submit them in <c>string</c> form, perform operations within
            /// the database engine and report the results by retrieving the
            /// <c>string</c> representation.
            /// </remarks>
            /// <param name="bigDecimalValue">
            /// The Java <c>BigDecimal</c> value to convert.
            /// </param>
            /// <returns>
            /// A <c>System.Decimal</c> representation of the given value.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the <c>BigDecimal</c> value does not lie in the range
            /// of <c>System.Decimal</c>
            /// </exception>
            /// <exception cref="NullReferenceException">
            /// When <c>bigDecimalValue</c> is <c>null</c>.
            /// </exception>
            [CLSCompliant(false)]
            public static decimal ToDecimal(JavaBigDecimal bigDecimalValue)
            {
                if (bigDecimalValue.compareTo(MAX_DECIMAL) > 0
                    || bigDecimalValue.compareTo(MIN_DECIMAL) < 0)
                {
                    throw HsqlConvert.NumericValueOutOfRange(bigDecimalValue);
                }

                return decimal.Parse(bigDecimalValue.toPlainString());
            }
            #endregion

            #region ToDecimal(java.lang.Number)
            /// <summary>
            /// Converts the given Java <c>Number</c> object to a 
            /// <c>System.Decimal</c> representation.
            /// </summary>
            /// <remarks>
            /// This conversion is not ONTO. For Java <c>Number</c> values that may lie
            /// outside the range of <c>System.Decimal</c>, it is recommended
            /// to submit them in <c>string</c> form, perform operations within
            /// the database engine and report the results by retrieving the
            /// <c>string</c> representation.
            /// </remarks>
            /// <param name="n">
            /// The Java <c>Number</c> to convert.
            /// </param>
            /// <returns>
            /// A <c>System.Decimal</c> representation.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the value of the given <c>Number</c> does not lie
            /// in the range of <c>System.Decimal</c>;
            /// </exception>
            /// <exception cref="NullReferenceException">
            /// When <c>n</c> is <c>null</c>.
            /// </exception>
            [CLSCompliant(false)]
            public static decimal ToDecimal(JavaNumber n)
            {
                if (n is JavaByte || n is JavaShort || n is JavaInteger)
                {
                    return new decimal(n.intValue());
                }
                else if (n is JavaLong)
                {
                    return new decimal(n.longValue());
                }
                else if (n is JavaFloat
                         || n is JavaDouble)
                {
                    return new decimal(n.doubleValue());
                }

                JavaBigDecimal bigDecimalValue = n as JavaBigDecimal;

                if (bigDecimalValue != null)
                {
                    if (bigDecimalValue.compareTo(MAX_DECIMAL) > 0
                        || bigDecimalValue.compareTo(MIN_DECIMAL) < 0)
                    {
                        throw HsqlConvert.NumericValueOutOfRange(n);
                    }

                    return decimal.Parse(bigDecimalValue.toPlainString());
                }

                return decimal.Parse(n.toString());
            }
            #endregion

            #region ToDecimal(object)
            /// <summary>
            /// Converts the given Java <c>Object</c> to a 
            /// <c>System.Decimal</c> representation.
            /// </summary>
            /// <remarks>
            /// This conversion is not ONTO. For values that may lie
            /// outside the range of <c>System.Decimal</c>, it is recommended
            /// to submit them in <c>string</c> form, perform operations within
            /// the database engine and report the results by retrieving the
            /// <c>string</c> representation.
            /// </remarks>
            /// <param name="o">
            /// The Java <c>Object</c> to convert.
            /// </param>
            /// <returns>
            /// A <c>System.Decimal</c> representation.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the result of the conversion does not lie in the range of <c>System.Decimal</c>
            /// -or-
            /// When there does not exist a conversion for the type of given object.
            /// </exception>
            /// <exception cref="NullReferenceException">
            /// When <c>o</c> is <c>null</c>.
            /// </exception>
            public static decimal ToDecimal(object o)
            {
                JavaNumber numberValue = o as JavaNumber;

                if (numberValue != null)
                {
                    return ToDecimal(numberValue);
                }

                JavaBoolean booleanValue = o as JavaBoolean;

                if (booleanValue != null)
                {
                    return booleanValue.booleanValue() ? 1M : 0M;
                }

                JavaObject objectValue = o as JavaObject;

                if (objectValue != null)
                {
                    bool isJavaObject;

                    object unwrapped = FromJava.UnWrap(objectValue, 
                        out isJavaObject);

                    return (isJavaObject) ? FromJava.ToDecimal(unwrapped)
                        : FromJava.ToDecimal(FromDotNet.ToDecimal(unwrapped));
                }

                if (o is java.util.Date || o is HsqlBinary || o is Array)
                {
                    throw WrongDataType(o);
                }

                string stringValue = o as string;

                if (stringValue == null)
                {
                    java.lang.Object jobj = o as java.lang.Object;

                    stringValue = (jobj == null) ? o.ToString() : jobj.toString();
                }

                return FromJava.ParseDecimal(stringValue.Trim());
            }
            #endregion

            #endregion

            #region ToBoolean

            #region ToBoolean(java.lang.Number)
            /// <summary>
            /// Casts the given Java <c>Number</c> object to an <c>SQL BOOLEAN</c>, returning
            /// the corresponding <see cref="System.Boolean"/> representation.
            /// </summary>
            /// <remarks>
            /// The conversion maps zero values to <c>false</c> and all other values
            /// to <c>true</c>.
            /// </remarks>
            /// <param name="n">The Java <c>Number</c> to convert.</param>
            /// <returns>
            /// A <c>System.Boolean</c> representation.
            /// </returns>
            /// <exception cref="NullReferenceException">
            /// When <c>n</c> is <c>null</c>.
            /// </exception>
            [CLSCompliant(false)]
            public static bool ToBoolean(JavaNumber n)
            {
                if (n is JavaInteger
                    || n is JavaShort
                    || n is JavaByte)
                {
                    return (n.intValue() != 0);
                }
                else if (n is JavaLong)
                {
                    return (n.longValue() != 0L);
                }
                else if (n is JavaDouble
                         || n is JavaFloat)
                {
                    return (n.doubleValue() != 0.0D);
                }

                JavaBigDecimal bigDecimalValue = n as JavaBigDecimal;

                if (bigDecimalValue != null)
                {
                    return !bigDecimalValue.equals(BIG_DECIMAL_0);
                }

                return (n.intValue() != 0);
            }
            #endregion

            #region ToBoolean(object)
            /// <summary>
            /// Casts the given Java <c>Object</c> to a <c>SQL BOOLEAN</c>, returning
            /// the corresponding <see cref="System.Boolean"/> representation.
            /// </summary>
            /// <remarks>
            /// This method is primarily intended for conversion of
            /// <c>java.lang.Boolean</c> and <c>java.lang.Number</c> objects.
            /// For <c>org.hsqldb.types.JavaObject</c>, the wrapped object is
            /// extracted and passed again to this method. <c>java.util.Date</c>
            /// descendents, <c>org.hsqldb.types.Binary</c> and <c>System.Array</c>
            /// types are considered illegal inputs. When none of the above cases apply,
            /// the result is produced by testing the string representation
            /// of the given object for case-insensitive equality with "TRUE".
            /// </remarks>
            /// <param name="o">The Java <c>Object</c> to convert.</param>
            /// <returns>
            /// The <c>SQL BOOLEAN</c> representation the given Java <c>Object</c>, as
            /// a <c>System.Boolean</c> value.
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When a valid conversion does not exist for the type of the given object.
            /// </exception>
            /// <exception cref="NullReferenceException">
            /// When <c>o</c> is <c>null</c>;
            /// </exception>
            public static bool ToBoolean(object o)
            {
                JavaBoolean booleanValue = o as JavaBoolean;

                if (booleanValue != null)
                {
                    return booleanValue.booleanValue();
                }

                JavaNumber numberValue = o as JavaNumber;

                if (numberValue != null)
                {
                    return ToBoolean(numberValue);
                }

                JavaObject objectValue = o as JavaObject;

                if (objectValue != null)
                {
                    bool isJavaObject;

                    object unwrapped = FromJava
                        .UnWrap(objectValue, out isJavaObject);

                    return (isJavaObject)
                        ? FromJava.ToBoolean(unwrapped)
                        : FromDotNet.ToBoolean(unwrapped).booleanValue();
                }

                if (o is java.util.Date
                    || o is HsqlBinary
                    || o is Array)
                {
                    throw WrongDataType(o);
                }

                string stringValue = o as string;

                if (stringValue == null)
                {
                    java.lang.Object jobj = o as java.lang.Object;

                    stringValue = (jobj == null) ? o.ToString() : jobj.toString();
                }

                return stringValue.Trim().Equals("TRUE", IgnoreCase);
            }
            #endregion

            #endregion

            #region ToString(object)
            /// <summary>
            /// Converts the given Java <c>Object</c> to a <c>System.String</c>
            /// </summary>
            /// <remarks>
            /// If the given object is a <c>string</c> or <c>null</c>, this
            /// is the identity conversion.
            /// <c>java.lang.Boolean</c>, <c>org.hsqldb.types.Binary</c>, <b>byte[]</b>,
            /// <c>java.lang.Number</c>, <c>java.sql.Date</c>, <c>java.sql.Time</c>
            /// and <c>java.sql.Timestamp</c> values are converted
            /// to their SQL literal form; otherwise, the string representation of
            /// the object is retured by invoking its <c>ToString()</c> method.
            /// </remarks>
            /// <param name="o">The Java <c>Object</c> to convert.</param>
            /// <returns>
            /// The SQL literal representation of the given Java <c>Object</c>,
            /// as a <c>System.String</c>
            /// </returns>
            public static string ToString(object o)
            {
                string s = o as string;

                if (s != null)
                {
                    return s;
                }

                JavaBigDecimal bigDecimalValue = o as JavaBigDecimal;

                if (bigDecimalValue != null)
                {
                    return bigDecimalValue.toPlainString();
                }

                JavaNumber numberValue = o as JavaNumber;

                if (numberValue != null)
                {
                    return numberValue.toString();
                }

                JavaTime timeValue = o as JavaTime;

                if (timeValue != null)
                {
                    return HsqlDateTime.getTimeString(timeValue, null);
                }

                JavaTimestamp timestampValue = o as JavaTimestamp;

                if (timestampValue != null)
                {
                    return HsqlDateTime.getTimestampString(timestampValue, null);
                }

                JavaDate dateValue = o as JavaDate;

                if (dateValue != null)
                {
                    return HsqlDateTime.getDateString(dateValue, null);
                }

                byte[] byteArrayValue = o as byte[];

                if (byteArrayValue != null)
                {
                    return StringConverter.byteToHex(byteArrayValue);
                }

                JavaObject objectValue = o as JavaObject;

                if (objectValue != null)
                {
                    bool isJavaObject;

                    object unwrapped = FromJava.UnWrap(objectValue, 
                        out isJavaObject);

                    return (isJavaObject)? FromJava.ToString(unwrapped)
                        : FromDotNet.ToString(unwrapped);
                }

                java.lang.Object jobj = o as java.lang.Object;

                return (jobj == null) ? o.ToString() : jobj.toString();
            }
            #endregion

            #region ToTime

            #region ToTime(long)
            /// <summary>
            /// Converts the given Java time in milliseconds to an SQL TIME
            /// value (hh:mm:ss) and returns the corresponding
            /// <c>System.DateTime</c> representation.
            /// </summary>
            /// <param name="javaTimeInMillis">
            /// For which to retrieve the corresponding <c>DateTime</c> value.
            /// </param>
            /// <returns>
            /// The <c>System.DateTime</c> representation of the given
            /// SQL TIME specified as a Java time in milliseconds.
            /// </returns>
            public static DateTime ToTime(long javaTimeInMillis)
            {
                JavaGregorianCalendar cal = HsqlConvert.GetJavaGregorianCalendar();

                cal.setTimeInMillis(javaTimeInMillis);
                // CHECKME:  is this part actually required?
                cal.clear(JavaCalendar.YEAR);
                cal.clear(JavaCalendar.MONTH);
                cal.clear(JavaCalendar.DATE);
                cal.clear(JavaCalendar.MILLISECOND);

                DateTime dt = new DateTime(MIN_YEAR,
                                           MIN_MONTH,
                                           MIN_DAY,
                                           cal.get(JavaCalendar.HOUR),
                                           cal.get(JavaCalendar.MINUTE),
                                           cal.get(JavaCalendar.SECOND),
                                           MIN_MILLISECOND,
                                           HsqlConvert.dotNetGregorianCalendar);
                return dt;
            }
            #endregion

            #region ToTime(object)
            /// <summary>
            /// Casts the given Java <c>Object</c> to an SQL TIME value (hh:mm:ss)
            /// and returns the corresponding <see cref="System.DateTime"/>
            /// representation.
            /// </summary>
            /// <remarks>
            /// This method is intended primarily for converting 
            /// <c>java.sql.Time</c>, <c>java.sql.Timestamp</c>
            /// and <c>string</c> values. <c>java.sql.Date</c>
            /// is considered illegal because it is known apriori to
            /// have no time component. <c>java.lang.Number</c>,
            /// <c>org.hsqldb.types.Binary</c> and <c>System.Array</c> types
            /// are considered illegal also. For
            /// <c>org.hsqldb.types.JavaObject</c>, the wrapped object is
            /// extracted and passed again to this method;
            /// otherwise, an attempt is made to parse the string
            /// representation of the object as an SQL TIME literal.
            /// </remarks>
            /// <param name="o">
            /// To cast to an SQL TIME value in order to return
            /// the corresponding <c>System.DateTime</c> representation.
            /// </param>
            /// <returns>
            /// The <c>System.DateTime</c> representation of the
            /// SQL TIME value derived from the given Java <c>Object</c>. 
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When no coversion exists for the type of the given object;
            /// When a time format exception is encountered.
            /// </exception>
            /// <exception cref="NullReferenceException">
            /// When <c>o</c> is <c>null</c>.
            /// </exception>
            public static DateTime ToTime(object o)
            {
                JavaTime timeValue = o as JavaTime;

                if (timeValue != null)
                {
                    return FromJava.ToTime(timeValue.getTime());
                }

                JavaTimestamp timestampValue = o as JavaTimestamp;

                if (timestampValue != null)
                {
                    return FromJava.ToTime(timestampValue.getTime());
                }

                string s = o as string;

                if (s != null)
                {
                    return ToTime(HsqlDateTime.timeValue(s).getTime());
                }

                JavaObject objectValue = o as JavaObject;

                if (objectValue != null)
                {
                    bool isJavaObject;

                    object unwrapped = FromJava.UnWrap(objectValue, 
                        out isJavaObject);

                    return (isJavaObject) ? FromJava.ToTime(unwrapped)
                        : FromJava.ToTime(FromDotNet.ToTime(unwrapped).getTime());
                }

                if (o is java.sql.Date)
                {
                    throw InvalidConversion(HsqlTypes.DATE);
                }

                if (o is HsqlBinary || o is Array || o is JavaNumber)
                {
                    throw WrongDataType(o);
                }

                // last ditch effort
                java.lang.Object jobj = o as java.lang.Object;

                s = (jobj == null) ? o.ToString() : jobj.toString();

                return ToTime(HsqlDateTime.timeValue(s.Trim()).getTime());
            }
            #endregion

            #endregion

            #region ToTimestamp

            #region ToTimestamp(long)
            /// <summary>
            /// Converts the given Java time in milliseconds to an SQL TIMESTAMP
            /// value (yyyy-mm-dd hh:mm:ss.fff) and returns the corresponding
            /// <c>System.DateTime</c> representation.
            /// </summary>
            /// <param name="javaTimeInMillis">
            /// To convert to the SQL TIMESTAMP value for which
            /// to retrieve the corresponding <c>DateTime</c> value.
            /// </param>
            /// <returns>
            /// The <c>System.DateTime</c> representation of the SQL TIMESTAMP
            /// value corresponding to the given Java time in milliseconds.
            /// </returns>
            public static DateTime ToTimestamp(long javaTimeInMillis)
            {
                JavaGregorianCalendar cal = HsqlConvert.GetJavaGregorianCalendar();

                cal.setTimeInMillis(javaTimeInMillis);

                return new DateTime(cal.get(JavaCalendar.YEAR),
                                    cal.get(JavaCalendar.MONTH) + 1,
                                    cal.get(JavaCalendar.DATE),
                                    cal.get(JavaCalendar.HOUR_OF_DAY),
                                    cal.get(JavaCalendar.MINUTE),
                                    cal.get(JavaCalendar.SECOND),
                                    cal.get(JavaCalendar.MILLISECOND),
                                    HsqlConvert.dotNetGregorianCalendar);

            }
            #endregion

            #region ToTimestamp(object)
            /// <summary>
            /// Casts the given Java <c>Object</c> to an <c>SQL TIMESTAMP</c> value
            /// and returns the corresponding <see cref="System.DateTime"/>
            /// representation.
            /// </summary>
            /// <remarks>
            /// This method is intended primarily for converting 
            /// <c>java.sql.Date</c>, <c>java.sql.Time</c>,
            /// <c>java.sql.Timestamp</c> and <c>string</c> values.
            /// <c>java.lang.Number</c>, <c>org.hsqldb.types.Binary</c>
            /// and <c>System.Array</c> types are considered illegal.
            /// For <c>org.hsqldb.types.JavaObject</c>, the wrapped
            /// object is extracted and passed again to this method;
            /// otherwise, an attempt is made to parse the string
            /// representation of the object as an SQL TIMESTAMP
            /// literal.
            /// </remarks>
            /// <param name="o">
            /// To cast to an SQL TIMESPAMP value in order to return
            /// the corresponding <c>System.DateTime</c> representation.
            /// </param>
            /// <returns>
            /// The <c>System.DateTime</c> representation of the
            /// SQL TIMESPAMP value derived from the given Java <c>Object</c>. 
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When no coversion exists for the type of the given object;
            /// When a timestamp format exception is encountered.
            /// </exception>
            /// <exception cref="NullReferenceException">
            /// When <c>o</c> is <c>null</c>.
            /// </exception>
            public static DateTime ToTimestamp(object o)
            {
                JavaTimestamp timestampValue = o as JavaTimestamp;

                if (timestampValue != null)
                {
                    return ToTimestamp(timestampValue.getTime());
                }

                JavaTime timeValue = o as JavaTime;

                if (timeValue != null)
                {
                    return ToTime(timeValue.getTime());
                }

                java.sql.Date dateValue = o as java.sql.Date;

                if (dateValue != null)
                {
                    return ToDate(dateValue.getTime());
                }

                JavaObject objectValue = o as JavaObject;

                if (objectValue != null)
                {
                    bool isJavaObject;

                    object unwrapped = FromJava
                        .UnWrap(objectValue, out isJavaObject);

                    return (isJavaObject)
                        ? FromJava.ToTimestamp(unwrapped)
                        : FromJava.ToTimestamp(FromDotNet.ToTimestamp(unwrapped).getTime());
                }

                string s = o as string;

                if (s != null)
                {
                    return ToTimestamp(HsqlDateTime.timestampValue(s.Trim()).getTime());
                }

                if (o is HsqlBinary
                    || o is Array
                    || o is JavaNumber)
                {
                    throw WrongDataType(o);
                }

                // last ditch effort
                java.lang.Object jobj = o as java.lang.Object;

                s = (jobj == null) ? o.ToString() : jobj.toString();

                return ToTimestamp(HsqlDateTime.timestampValue(s.Trim()).getTime());
            }
            #endregion

            #endregion

            #region ToDate

            #region ToDate(long)

            /// <summary>
            /// Converts the given Java time in milliseconds to an SQL DATE
            /// value (yyyy-mm-dd) and returns the corresponding
            /// <c>System.DateTime</c> representation.
            /// </summary>
            /// <param name="javaTimeInMillis">
            /// To convert to the SQL DATE value for which
            /// to retrieve the corresponding <c>System.DateTime</c> value.
            /// </param>
            /// <returns>
            /// The <c>System.DateTime</c> representation of the SQL DATE
            /// value corresponding to the given Java time in milliseconds.
            /// </returns>
            public static DateTime ToDate(long javaTimeInMillis)
            {
                JavaGregorianCalendar cal = HsqlConvert.GetJavaGregorianCalendar();

                cal.setTimeInMillis(javaTimeInMillis);

                cal.clear(JavaCalendar.HOUR_OF_DAY);
                cal.clear(JavaCalendar.MINUTE);
                cal.clear(JavaCalendar.SECOND);
                cal.clear(JavaCalendar.MILLISECOND);

                return new DateTime(cal.get(JavaCalendar.YEAR),
                                    cal.get(JavaCalendar.MONTH) + 1,
                                    cal.get(JavaCalendar.DATE),
                                    HsqlConvert.dotNetGregorianCalendar);
            }

            #endregion

            #region ToDate(object)
            /// <summary>
            /// Casts the given Java <c>Object</c> to an SQL DATE value
            /// and returns the corresponding <see cref="System.DateTime"/>
            /// representation.
            /// </summary>
            /// <remarks>
            /// Intended primarily for use with the <c>java.util.Date</c> variants
            /// <c>java.sql.Date</c> and <c>java.sql.Timestamp</c>),
            /// but also supports object instances whose string representation
            /// corresponds to a valid SQL datetime character sequence.
            /// <c>java.sql.Time</c>,<c>org.hsqldb.types.Binary</c>,
            /// <c>java.lang.Number</c> and <c>System.Array</c> types are
            /// considered invalid and will raise a wrong data type exception.
            /// </remarks>
            /// <param name="o">
            /// To cast to an SQL DATE value in order to return
            /// the corresponding <c>System.DateTime</c> representation.
            /// </param>
            /// <returns>
            /// The <c>System.DateTime</c> representation of the
            /// SQL DATE value derived from the given Java <c>Object</c>. 
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When no coversion exists for the type of the given object;
            /// When a date format exception is encountered.
            /// </exception>
            /// <exception cref="NullReferenceException">
            /// When <c>o</c> is <c>null</c>.
            /// </exception>
            public static DateTime ToDate(object o)
            {
                JavaTimestamp timestampValue = o as JavaTimestamp;

                if (timestampValue != null)
                {
                    return ToDate(timestampValue.getTime());
                }

                JavaDate dateValue = o as JavaDate;

                if (dateValue != null)
                {
                    return ToDate(dateValue.getTime());
                }

                string s = o as string;

                if (s != null)
                {
                    return ToDate(HsqlDateTime.dateValue(s).getTime());
                }

                JavaObject objectValue = o as JavaObject;

                if (objectValue != null)
                {
                    bool isJavaObject;

                    object unwrapped = FromJava
                        .UnWrap(objectValue, out isJavaObject);

                    if (isJavaObject)
                    {
                        return FromJava.ToDate(objectValue);
                    }

                    long javaTimeInMillis = FromDotNet
                        .ToDate(unwrapped).getTime();

                    return FromJava.ToDate(javaTimeInMillis);
                }

                if (o is JavaTime)
                {
                    throw InvalidConversion(HsqlTypes.TIME);
                }

                if (o is HsqlBinary
                    || o is Array
                    || o is JavaNumber)
                {
                    throw WrongDataType(o);
                }

                java.lang.Object jobj = o as java.lang.Object;

                s = (jobj == null) ? o.ToString() : jobj.toString();

                return ToDate(HsqlDateTime.dateValue(s.Trim()).getTime());
            }
            #endregion

            #endregion

            #region ToBinary(object)
            /// <summary>
            /// Casts the given Java <c>Object</c> to an SQL BINARY value and
            /// returns the corresponding <c>System.Byte[]</c> representation.
            /// </summary>
            /// <remarks>
            /// Supports conversion from <c>byte[]</c>, <c>string</c>,
            /// <c>org.hsqldb.types.Binary</c> and <c>org.hsqldb.types.JavaObject</c>.
            /// All other types are considered illegal.
            /// If the given object is a <c>byte[]</c>, returns a cloned version;
            /// if a <c>string</c>, must be a legal hexadecimal character sequence;
            /// if an <c>org.hsqldb.types.JavaObject</c>, the wrapped object is
            /// extracted and passed again to this method.
            /// </remarks>
            /// <param name="o">
            /// To cast to an SQL BINARY value in order to return
            /// the corresponding <c>System.Byte[]</c> representation.
            /// </param>
            /// <returns>
            /// The <c>System.Byte[]</c> representation of the
            /// SQL BINARY value derived from the given Java <c>Object</c>. 
            /// </returns>
            /// <exception cref="HsqlDataSourceException">
            /// When no coversion exists for the type of the given object;
            /// When an SQL BINARY format exception is encountered.
            /// </exception>
            /// <exception cref="NullReferenceException">
            /// When <c>o</c> is <c>null</c>.
            /// </exception>
            public static byte[] ToBinary(object o)
            {
                byte[] byteArrayValue = o as byte[];

                if (byteArrayValue != null)
                {
                    return (byte[])byteArrayValue.Clone();
                }

                HsqlBinary binaryValue = o as HsqlBinary;

                if (binaryValue != null)
                {
                    return binaryValue.getClonedBytes();
                }

                string s = o as string;

                if (s != null)
                {
                    return StringConverter.hexToByte(s);
                }

                JavaObject objectValue = o as JavaObject;

                if (objectValue != null)
                {
                    bool isJavaObject;

                    object unwrapped = FromJava
                        .UnWrap(objectValue, out isJavaObject);

                    return (isJavaObject)
                        ? FromJava.ToBinary(unwrapped)
                        : FromJava.ToBinary(FromDotNet.ToBinary(unwrapped));
                }

                if (o == null)
                {
                    throw new NullReferenceException();
                }
                else
                {
                    throw HsqlConvert.WrongDataType(o);
                }
            }
            #endregion

            #region UnWrap(object,bool)

            /// <summary>
            /// Converts the given <c>object</c> to its expected
            /// <c>System.Object</c> representation, unwrapping
            /// the given <c>object</c> in the process, if required.
            /// </summary>
            /// <remarks>
            /// <para>
            /// This is a convenience method that is intended
            /// specifically to handle conversion from <c>SQL OBJECT</c>
            /// representation (by <c>org.hsqldb.types.JavaObject</c>)
            /// but also for objects of any type, with the exception of
            /// <c>org.hsqldb.types.Binary</c>, with which the
            /// <see cref="ToBinary(object)"/> method or, more generally,
            /// the <see cref="ToObject(object,int)"/> should be used
            /// instead.
            /// </para>
            /// <para>
            /// When the runtime type of the given <c>object</c> is
            /// <c>org.hsqldb.types.JavaObject</c>, the conversion
            /// is delegated to <c>UnWrap(org.hsqldb.types.JavaObject,bool)</c>.
            /// Otherwise the given <c>object</c> is returned directly,
            /// with no conversion is performed.
            /// </para>
            /// <para>
            /// The value of <c>isJavaObject</c> <em>out</em> parameter is
            /// important when a chain of conversions is required;
            /// the value can be used to to optimize subsequent conversions,
            /// since subsequent cases involving one side or the other of
            /// the two type systems can be ignored.
            /// </para>
            /// </remarks>
            /// <param name="sourceObject">
            /// To convert to the standard <c>System.Object</c>
            /// representation.
            /// </param>
            /// <param name="isJavaObject">
            /// <c>true</c> if the returned <c>object</c> extends
            /// <c>java.lang.Object</c>; <c>false</c> otherwise.
            /// </param>
            /// <returns>
            /// The expected <c>System.Object</c> representation.
            /// </returns>
            public static object UnWrap(
                object sourceObject,
                out bool isJavaObject)
            {
                JavaObject javaObject = sourceObject as JavaObject;

                if (javaObject == null)
                {
                    isJavaObject = (sourceObject is java.lang.Object);

                    return sourceObject;
                }
                else
                {
                    // magic happens... ;-)
                    return UnWrap(javaObject, out isJavaObject);
                }
            }
            #endregion

            #region UnWrap(JavaObject,bool)
            /// <summary>
            /// Retrieves the <c>object</c> wrapped by the given
            /// <see cref="org.hsqldb.types.JavaObject"/> instance.
            /// </summary>
            /// <remarks>
            /// <para>
            /// The wrapped object may be a <c>java.io.Serializable</c>
            /// or a .NET object having the <c>SerializableAttribute</c>.
            /// </para>
            /// <para>
            /// In general, the <c>JavaObject</c> actually contains a
            /// <c>byte[]</c> representing the <em>serialized form</em>
            /// of the wrapped object. Due the inherent incompatibility
            /// between the Java and .NET serialiation mechanisms, a
            /// special <c>UUID</c> octet sequence is written at the
            /// beginning of the stream when serializing .NET objects.
            /// Before the representative <c>byte[]</c> is deserialized,
            /// it is tested to see if the leading bytes match the
            /// special sequence.
            /// </para>
            /// <para>
            /// If the leading bytes match, the <c>isJavaObject</c> parameter
            /// is set <c>false</c>, the special leading bytes are discarded,
            /// and the remaining bytes are deserialized using a <see cref=
            /// "System.Runtime.Serialization.Formatters.Binary.BinaryFormatter"/>.
            /// The result in this case is a <c>System.Object</c> graph;
            /// </para> 
            /// <para>
            /// otherwise, <c>isJavaObject</c> is set <c>true</c> and the
            /// entire content of the wrapped <c>byte[]</c> is deserialized
            /// using a <c>java.io.ObjectInputStream</c>, resulting in a
            /// <c>java.lang.Object</c> graph.
            /// </para>
            /// </remarks>
            /// <param name="javaObject">
            /// Wrapping the desired <c>object</c>.
            /// </param>
            /// <param name="isJavaObject">
            /// <c>true</c> if wrapped object extends <c>java.lang.Object</c>
            /// and implements <c>java.io.Serializable</c>;
            /// else <c>false</c>.
            /// </param>
            /// <returns>
            /// The <c>object</c> wrapped by the given
            /// <see cref="org.hsqldb.types.JavaObject"/> instance.
            /// </returns>
            [CLSCompliant(false)]
            public static object UnWrap(JavaObject javaObject, out bool isJavaObject)
            {
                if (javaObject == null)
                {
                    isJavaObject = false;
                    return null;
                }
                else
                {
                    return DotNetObject.Deserialize(javaObject.getBytes(),
                        out isJavaObject);
                }
            }
            #endregion

            #region ToObject(object, int)
            /// <summary>
            /// Converts the given Java <c>Object</c> to the 
            /// HSQLDB preferred .NET representation of
            /// the given SQL data type.
            /// </summary>
            /// <remarks>
            /// <para>
            /// Although most conversion methods of this class are
            /// public to allow short-cut conversions when the
            /// source and target types are well known or can
            /// be easily inferred, this method should be 
            /// considered the class authority.
            /// </para>
            /// <para>
            /// The other methods have been written specifically
            /// to support this method; without a detailed
            /// knowlege of the lower-level issues that have influenced
            /// the design, the optimal signature may not always be
            /// obvious. If you need to convert from the HSQLDB Java mapping
            /// for SQL to the corresponding .NET representation in a way
            /// that is general and maximally compatible with the HSQLDB 
            /// type system, please prefer this method.
            /// </para>
            /// </remarks>
            /// <param name="objectValue">
            /// The Java <c>Object</c> to convert. 
            /// <para>
            /// Typically, the source of this object will be an item
            /// from an <c>org.hsqldb.Record.data</c> array, found by
            /// traversing from the root record of an
            /// <c>org.hsqldb.Result</c>.
            /// </para>
            /// </param>
            /// <param name="type">
            /// The HSQLDB data type code.
            /// <para>
            /// Typically, the source of this value will be the HSQLDB
            /// data type code from the <c>org.hsqldb.Result.metadata.colTypes</c>
            /// array item with the same ordinal as that from which the
            /// given <c>objectValue</c> was obtained from the
            /// <c>org.hsqldb.Record.data</c> array.
            /// </para>
            /// </param>
            /// <returns>The preferred CLR representation of
            /// the given SQL data type.</returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the conversion is not supported or
            /// data truncation would occur as a result 
            /// of the conversion.
            /// </exception>
            public static object ToObject(object objectValue, int type)
            {
                if (objectValue == null)
                {
                    return null;
                }

                switch (type)
                {
                    case HsqlTypes.ARRAY:
                        {
                            throw HsqlConvert.InvalidConversion(HsqlTypes.ARRAY);
                        }
                    case HsqlTypes.BIGINT:
                        {
                            return ToBigInt(objectValue);
                        }
                    case HsqlTypes.BINARY:
                        {
                            return ToBinary(objectValue);
                        }
                    case HsqlTypes.BLOB:
                        {
                            throw HsqlConvert.InvalidConversion(HsqlTypes.BLOB);
                        }
                    case HsqlTypes.BOOLEAN:
                        {
                            return ToBoolean(objectValue);
                        }
                    case HsqlTypes.CHAR:
                        {
                            return ToString(objectValue);
                        }
                    case HsqlTypes.DATALINK:
                        {
                            throw HsqlConvert.InvalidConversion(HsqlTypes.DATALINK);
                        }
                    case HsqlTypes.DATE:
                        {
                            return ToDate(objectValue);
                        }
                    case HsqlTypes.DECIMAL:
                        {
                            return ToDecimal(objectValue);
                        }
                    case HsqlTypes.DISTINCT:
                        {
                            throw HsqlConvert.InvalidConversion(HsqlTypes.DISTINCT);
                        }
                    case HsqlTypes.DOUBLE:
                        {
                            JavaDouble d = objectValue as JavaDouble;

                            return (d == null)
                                       ? ToDouble(objectValue)
                                       : d.doubleValue();
                        }
                    case HsqlTypes.FLOAT:
                        {
                            return ToDouble(objectValue);
                        }
                    case HsqlTypes.INTEGER:
                        {
                            JavaInteger i = objectValue as JavaInteger;
                            return (i == null)
                                       ? ToInteger(objectValue)
                                       : i.intValue();
                        }
                    case HsqlTypes.JAVA_OBJECT:
                        {
                            throw HsqlConvert.InvalidConversion(HsqlTypes.JAVA_OBJECT);
                        }
                    case HsqlTypes.LONGVARBINARY:
                        {
                            return ToBinary(objectValue);
                        }
                    case HsqlTypes.LONGVARCHAR:
                        {
                            return ToString(objectValue);
                        }
                    case HsqlTypes.NULL:
                        {
                            return null;
                        }
                    case HsqlTypes.NUMERIC:
                        {
                            return ToDecimal(objectValue);
                        }
                    case HsqlTypes.OTHER:
                        {
                            // Need a dummy variable
                            // to call this.
                            bool isJavaObject;

                            return UnWrap(objectValue, out isJavaObject);
                        }
                    case HsqlTypes.REAL:
                        {
                            return ToReal(objectValue);
                        }
                    case HsqlTypes.REF:
                        {
                            throw HsqlConvert.InvalidConversion(HsqlTypes.REF);
                        }
                    case HsqlTypes.SMALLINT:
                        {
                            return ToSmallInt(objectValue);
                        }
                    case HsqlTypes.STRUCT:
                        {
                            throw HsqlConvert.InvalidConversion(HsqlTypes.STRUCT);
                        }
                    case HsqlTypes.TIME:
                        {
                            return ToTime(objectValue);
                        }
                    case HsqlTypes.TIMESTAMP:
                        {
                            return ToTimestamp(objectValue);
                        }
                    case HsqlTypes.TINYINT:
                        {
                            return ToTinyInt(objectValue);
                        }
                    case HsqlTypes.VARBINARY:
                        {
                            return ToBinary(objectValue);
                        }
                    case HsqlTypes.VARCHAR:
                        {
                            return ToString(objectValue);
                        }
                    case HsqlTypes.XML:
                        {
                            throw HsqlConvert.InvalidConversion(HsqlTypes.XML);
                        }
                    case HsqlTypes.VARCHAR_IGNORECASE:
                        {
                            return ToString(objectValue);
                        }
                    default:
                        {
                            throw HsqlConvert.UnknownConversion(objectValue, type);
                        }
                }
            }
            #endregion

            #region ToGuid(java.util.UUID)
            /// <summary>
            /// Converts the given <see cref="java.util.UUID"/>
            /// value to a <see cref="System.Guid"/> value.
            /// </summary>
            /// <param name="uuid">
            /// To convert to a <c>System.Guid</c> value.
            /// </param>
            /// <returns>
            /// The corresponding <c>System.Guid</c> value.
            /// </returns>
            [CLSCompliant(false)]
            public static Guid ToGuid(java.util.UUID uuid)
            {
                long msb = uuid.getMostSignificantBits();
                long lsb = uuid.getMostSignificantBits();

                byte[] bytes = new byte[]
                {
                    (byte) ((msb >> 56) & 0xff),
                    (byte) ((msb >> 48) & 0xff),
                    (byte) ((msb >> 40) & 0xff),
                    (byte) ((msb >> 32) & 0xff),
                    (byte) ((msb >> 24) & 0xff),
                    (byte) ((msb >> 16) & 0xff),
                    (byte) ((msb >>  8) & 0xff),
                    (byte) ((msb >>  0) & 0xff),

                    (byte) ((lsb >> 56) & 0xff),
                    (byte) ((lsb >> 48) & 0xff),
                    (byte) ((lsb >> 40) & 0xff),
                    (byte) ((lsb >> 32) & 0xff),
                    (byte) ((lsb >> 24) & 0xff),
                    (byte) ((lsb >> 16) & 0xff),
                    (byte) ((lsb >>  8) & 0xff),
                    (byte) ((lsb >>  0) & 0xff),
                };

                return new Guid(bytes);

            }
            #endregion

            #region Number Parsing

            #region ParseInteger(string)
            /// <summary>
            /// Parses the given value by treating it as an HSQLDB SQL INTEGER literal.
            /// </summary>
            /// <remarks>
            /// The legal input formats are those supported by
            /// <c>java.lang.Integer.ParseInt</c>
            /// </remarks>
            /// <param name="value">The value to parse.</param>
            /// <returns>a <c>System.Int32</c> representation of the string</returns>
            /// <exception cref="HsqlDataSourceException">
            /// When a number format exception is encountered.
            /// </exception>
            public static int ParseInteger(string value)
            {
                try
                {
                    Tokenizer tokenizer = new Tokenizer(value);
                    
                    return tokenizer.GetNextAsInt();
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
            /// Parses the given value by treating it as an HSQLDB SQL BIGINT literal.
            /// </summary>
            /// <param name="value">The value to parse.</param>
            /// <returns>a <c>System.Int64</c> representation of the string</returns>
            /// <exception cref="HsqlDataSourceException">
            /// When a number format exception is encountered.
            /// </exception>
            public static long ParseBigInt(string value)
            {
                Tokenizer tokenizer = new Tokenizer(value);

                return tokenizer.GetNextAsBigint();
            }
            #endregion

            #region ParseDouble(string)
            /// <summary>
            /// Parses the given value as an HSQLDB SQL DOUBLE.
            /// </summary>
            /// <remarks>
            /// The legal input formats are those supported by
            /// <c>java.lang.Double.ParseDouble</c>
            /// </remarks>
            /// <param name="value">The value to parse.</param>
            /// <returns>An SQL DOUBLE representation of the given value.</returns>
            /// <exception cref="HsqlDataSourceException">
            /// When a number format exception is encountered.
            /// </exception>
            public static double ParseDouble(string value)
            {
                Tokenizer tokenizer = new Tokenizer(value);

                return ((java.lang.Number)tokenizer.GetNextAsLiteralValue(
                    HsqlProviderType.Double)).doubleValue();
            }
            #endregion

            #region ParseDecimal(string)
            /// <summary>
            /// Parses the given value as an HSQLDB SQL DECIMAL value
            /// </summary>
            /// <remarks>
            /// The legal input formats are those supported by
            /// <c>System.Decimal.Parse(string)</c>.
            /// Note that the HSQLDB SQL DECIMAL type accepts values
            /// that can have precision and scale far larger than
            /// System.Decimal.  It is recommended to submit a string
            /// value to the database when the desired SQL DECIMAL value
            /// cannot be represented as a <c>System.Decimal</c> value.
            /// </remarks>
            /// <param name="value">The value to parse.</param>
            /// <returns>An SQL DECIMAL representation of the given value.</returns>
            /// <exception cref="HsqlDataSourceException">
            /// When the underlying call <c>System.Decimal.Parse(string)</c>
            /// raises an exception.
            /// </exception>
            public static decimal ParseDecimal(string value)
            {
                try
                {
                    return decimal.Parse(value);
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
    #endregion
}