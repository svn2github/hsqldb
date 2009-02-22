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

using System.Data.Hsqldb.Common.Sql.Type;
using System.Data.SqlTypes;
using System.Text;

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

using JavaBigDecimal = java.math.BigDecimal;
using JavaBigInteger = java.math.BigInteger;
using JavaBoolean = java.lang.Boolean;
using JavaCalendar = java.util.Calendar;
using Binary = org.hsqldb.types.Binary;
using JavaByte = java.lang.Byte;
using DateTime = System.DateTime;
using JavaFloat = java.lang.Float;
using JavaDouble = java.lang.Double;
using JavaInteger = java.lang.Integer;
using JavaLong = java.lang.Long;
using JavaNumber = java.lang.Number;
using JavaShort = java.lang.Short;
using JavaTime = java.sql.Time;
using TimeSpan = System.TimeSpan;
using JavaTimestamp = java.sql.Timestamp;

using StringConverter = org.hsqldb.lib.StringConverter;
using ValuePool = org.hsqldb.store.ValuePool;

using JavaObject = org.hsqldb.types.JavaObject;
using TypeCode = System.TypeCode;
using IConvertible = System.IConvertible;
using Convert = System.Convert;
using Type = System.Type;
using IBlob = System.Data.Hsqldb.Common.Lob.IBlob;
using IClob = System.Data.Hsqldb.Common.Lob.IClob;


using ISerializable = System.Runtime.Serialization.ISerializable;
using Serializable = java.io.Serializable;

using Array = System.Array;

using DotNetGregorianCalendar = System.Globalization.GregorianCalendar;
using JavaGregorianCalendar = java.util.GregorianCalendar;

using HsqlDateTime = org.hsqldb.HsqlDateTime;

#endregion

namespace System.Data.Hsqldb.Common
{
    /// <summary>
    /// <para>
    /// Bridges the CLR and HSQLDB type systems.
    /// </para>
    /// <img src="/ClassDiagrams/System.Data.Hsqldb.Common.HsqlConvert.png"
    ///      alt="HsqlConvert Class Diagram"/>
    /// </summary>
    /// <author name="boucherb@users"/>    
    public static partial class HsqlConvert
    {
        #region Constants

        const System.StringComparison IgnoreCase
            = System.StringComparison.InvariantCultureIgnoreCase;

        #endregion

        #region Static Read-only Fields

        private static readonly JavaBoolean TRUE = JavaBoolean.TRUE;
        private static readonly JavaBoolean FALSE = JavaBoolean.FALSE;

        private static readonly JavaBigDecimal MAX_DECIMAL = new JavaBigDecimal("79228162514264337593543950335.5");
        private static readonly JavaBigDecimal MIN_DECIMAL = new JavaBigDecimal("-79228162514264337593543950335.5");
        //
        private static readonly JavaBigDecimal MAX_DOUBLE = JavaBigDecimal.valueOf(JavaDouble.MAX_VALUE);
        private static readonly JavaBigDecimal MIN_DOUBLE = JavaBigDecimal.valueOf(JavaDouble.MIN_VALUE);
        //
        private static readonly JavaBigDecimal MAX_REAL = JavaBigDecimal.valueOf(JavaFloat.MAX_VALUE);
        private static readonly JavaBigDecimal MIN_REAL = JavaBigDecimal.valueOf(JavaFloat.MIN_VALUE);
        //
        private static readonly JavaBigInteger MAX_BIGINT = JavaBigInteger.valueOf(JavaLong.MAX_VALUE);
        private static readonly JavaBigInteger MIN_BIGINT = JavaBigInteger.valueOf(JavaLong.MIN_VALUE);
        //
        private static readonly JavaBigInteger MAX_INTEGER = JavaBigInteger.valueOf(JavaInteger.MAX_VALUE);
        private static readonly JavaBigInteger MIN_INTEGER = JavaBigInteger.valueOf(JavaInteger.MIN_VALUE);
        //
        private static readonly JavaBigInteger MAX_SMALLINT = JavaBigInteger.valueOf(JavaShort.MAX_VALUE);
        private static readonly JavaBigInteger MIN_SMALLINT = JavaBigInteger.valueOf(JavaShort.MIN_VALUE);
        //
        private static readonly JavaBigInteger MAX_TINYINT = JavaBigInteger.valueOf(127);
        private static readonly JavaBigInteger MIN_TINYINT = JavaBigInteger.valueOf(-128);
        //
        private static readonly JavaBigDecimal BIG_DECIMAL_1 = new JavaBigDecimal(1.0);
        private static readonly JavaBigDecimal BIG_DECIMAL_0 = new JavaBigDecimal(0.0);
        //
        private static readonly int MIN_YEAR = System.DateTime.MinValue.Year;
        private static readonly int MIN_MONTH = System.DateTime.MinValue.Month;
        private static readonly int MIN_DAY = System.DateTime.MinValue.Day;
        private static readonly int MIN_MILLISECOND = System.DateTime.MinValue.Millisecond;
        //
        private static readonly DotNetGregorianCalendar dotNetGregorianCalendar = new DotNetGregorianCalendar();

        // So that each thread gets its own instance, meaning we don't have to synchonize access.
        [System.ThreadStatic]
        private static JavaGregorianCalendar m_javaGregorianCalendar;

        #endregion

        #region GetJavaCalendar()
        /// <summary>
        /// Lazy init.
        /// </summary>
        /// <returns>
        /// Instance per thread.
        /// </returns>
        internal static JavaGregorianCalendar GetJavaGregorianCalendar()
        {
            if (m_javaGregorianCalendar == null)
            {
                m_javaGregorianCalendar = (JavaGregorianCalendar)
                    JavaGregorianCalendar.getInstance();

                m_javaGregorianCalendar.clear();
            }

            return m_javaGregorianCalendar;
        }
        #endregion

        #region Code-To-Code Conversion Methods

        #region ToHsqlProviderType(DbType)

        /// <summary>
        /// Retrieves the <see cref="HsqlProviderType"/>
        /// corresponding to the given <see cref="System.Data.DbType"/>.
        /// </summary>
        /// <remarks>
        /// The mapping is not 1-1.
        /// <list type="table">
        ///   <listheader>
        ///     <term>DbType</term>
        ///     <description>HsqlProviderType</description>
        ///   </listheader>
        ///   <item>
        ///     <term>DbType.AnsiString - A variable-length stream of non-Unicode characters ranging between 1 and 8,000 characters.</term>
        ///     <description>HsqlProviderType.VarChar</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.AnsiStringFixedLength - A fixed-length stream of non-Unicode characters.</term>
        ///     <description>HsqlProviderType.Char</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.Binary - A variable-length stream of binary data ranging between 1 and 8,000 bytes.</term>
        ///     <description>HsqlProviderType.Binary</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.Boolean A simple type representing Boolean values of true or false.</term>
        ///     <description>HsqlProviderType.Boolean</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.Byte An 8-bit unsigned integer ranging in value from 0 to 255.</term>
        ///     <description>HsqlProviderType.SmallInt</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.Currency - A currency value ranging from -2 63 (or -922,337,203,685,477.5808) 
        ///           to 2 63 -1 (or +922,337,203,685,477.5807) with an accuracy to a ten-thousandth of a
        ///           currency unit.</term>
        ///     <description>HsqlProviderType.Decimal</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.Date - Date and time data ranging in value from January 1, 1753 to December 31, 9999 to an accuracy of 3.33 milliseconds.</term>
        ///     <description>HsqlProviderType.TimeStamp</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.DateTime - A type representing a date and time value.</term>
        ///     <description>HsqlProviderType.TimeStamp</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.DateTime2 - A type representing a date that is combined with a time of day that is based on 
        ///     24-hour clock. datetime2 can be considered as an extension of the existing datetime type that has a 
        ///     larger date range, a larger default fractional precision, and optional user-specified precision.
        ///     </term>
        ///     <description>HsqlProviderType.Timestamp</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.DateTime2 - A type representing a date that is combined with a time of a day that has time
        ///           zone awareness and is based on a 24-hour clock.
        ///     </term>
        ///     <description>HsqlProviderType.Timestamp</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.Decimal - A simple type representing values ranging from 1.0 x 10 -28 to approximately 7.9 x 10 28 with 28-29 significant digits.</term>
        ///     <description>HsqlProviderType.Decimal</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.Double - A floating point type representing values ranging from approximately 5.0 x 10 -324 to 1.7 x 10 308 with a precision of 15-16 digits.</term>
        ///     <description>HsqlProviderType.Double</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.Guid - A globally unique identifier (or GUID).</term>
        ///     <description>HsqlProviderType.Binary</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.Int16 - An integral type representing signed 16-bit integers with values between -32768 and 32767.</term>
        ///     <description>HsqlProviderType.SmallInt</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.Int32 - An integral type representing signed 32-bit integers with values between -2147483648 and 2147483647.</term>
        ///     <description>HsqlProviderType.Integer</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.Int64 - An integral type representing signed 64-bit integers with values between -9223372036854775808 and 9223372036854775807.</term>
        ///     <description>HsqlProviderType.BigInt</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.Object - A general type representing any reference or value type not explicitly represented by another DbType value.</term>
        ///     <description>HsqlProviderType.Object</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.SByte - An integral type representing signed 8-bit integers with values between -128 and 127.</term>
        ///     <description>HsqlProviderType.TinyInt</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.Single - A floating point type representing values ranging from approximately 1.5 x 10 -45 to 3.4 x 10 38 with a precision of 7 digits.</term>
        ///     <description>HsqlProviderType.Real</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.String - A type representing Unicode character strings.</term>
        ///     <description>HsqlProviderType.VarChar</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.StringFixedLength - A type representing fixed-length Unicode character strings.</term>
        ///     <description>HsqlProviderType.Char</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.Time - Date and time data ranging in value from January 1, 1753 to December 31, 9999 to an accuracy of 3.33 milliseconds.</term>
        ///     <description>HsqlProviderType.TimeStamp</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.UInt16 - An integral type representing unsigned 16-bit integers with values between 0 and 65535.</term>
        ///     <description>HsqlProviderType.Integer</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.UInt32 - An integral type representing unsigned 32-bit integers with values between 0 and 4294967295.</term>
        ///     <description>HsqlProviderType.BigInt</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.UInt64 An integral type representing unsigned 64-bit integers with values between 0 and 18446744073709551615.</term>
        ///     <description>HsqlProviderType.Numeric</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.VarNumeric - A variable-length numeric value.</term>
        ///     <description>HsqlProviderType.Numeric</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.Xml - A parsed representation of an XML document or fragment.</term>
        ///     <description>HsqlProviderType.Xml</description>
        ///   </item>
        ///   <item>
        ///     <term>Others</term>
        ///     <description>HsqlProviderType.Object</description>
        ///   </item>
        /// </list>
        /// </remarks>
        /// <param name="type">
        /// The <c>DbType</c> for which to retrieve the
        /// corresponding <c>HsqlProviderType</c>.
        /// </param>
        /// <returns>
        /// The <c>HsqlProviderType</c> corresponding to the given <c>DbType</c>.
        /// </returns>
        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1706:ShortAcronymsShouldBeUppercase", MessageId = "Member")]
        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Maintainability", "CA1502:AvoidExcessiveComplexity")]
        public static HsqlProviderType ToHsqlProviderType(DbType type)
        {
            switch (type)
            {
                case DbType.AnsiString:
                    {
                        return HsqlProviderType.VarChar;
                    }
                case DbType.AnsiStringFixedLength:
                    {
                        return HsqlProviderType.Char;
                    }
                case DbType.Binary:
                    {
                        return HsqlProviderType.Binary;
                    }
                case DbType.Boolean:
                    {
                        return HsqlProviderType.Boolean;
                    }
                case DbType.Byte:
                    {
                        return HsqlProviderType.SmallInt;
                    }
                case DbType.Currency:
                    {
                        return HsqlProviderType.Decimal;
                    }
                case DbType.Date:
                    {
                        return HsqlProviderType.TimeStamp;
                    }
                case DbType.DateTime:
                    {
                        return HsqlProviderType.TimeStamp;
                    }
                case DbType.DateTime2:
                    {
                        return HsqlProviderType.TimeStamp;
                    }
                case DbType.DateTimeOffset:
                    {
                        return HsqlProviderType.Char;
                    }
                case DbType.Decimal:
                    {
                        return HsqlProviderType.Decimal;
                    }
                case DbType.Double:
                    {
                        return HsqlProviderType.Double;
                    }
                case DbType.Guid:
                    {
                        return HsqlProviderType.Binary;
                    }
                case DbType.Int16:
                    {
                        return HsqlProviderType.SmallInt;
                    }
                case DbType.Int32:
                    {
                        return HsqlProviderType.Integer;
                    }
                case DbType.Int64:
                    {
                        return HsqlProviderType.BigInt;
                    }
                case DbType.Object:
                    {
                        return HsqlProviderType.Object;
                    }
                case DbType.SByte:
                    {
                        return HsqlProviderType.TinyInt;
                    }
                case DbType.Single:
                    {
                        return HsqlProviderType.Real;
                    }
                case DbType.String:
                    {
                        return HsqlProviderType.VarChar;
                    }
                case DbType.StringFixedLength:
                    {
                        return HsqlProviderType.Char;
                    }
                case DbType.Time:
                    {
                        return HsqlProviderType.TimeStamp;
                    }
                case DbType.UInt16:
                    {
                        return HsqlProviderType.Integer;
                    }
                case DbType.UInt32:
                    {
                        return HsqlProviderType.BigInt;
                    }
                case DbType.UInt64:
                    {
                        return HsqlProviderType.Numeric;
                    }
                case DbType.VarNumeric:
                    {
                        return HsqlProviderType.Numeric;
                    }
                case DbType.Xml:
                    {
                        return HsqlProviderType.Xml;
                    }
                default:
                    {
                        return HsqlProviderType.Object;
                    }
            }
        }

        #endregion

        #region ToDbType(HsqlProviderType)

        /// <summary>
        /// Retrieves the <see cref="System.Data.DbType"/>
        /// corresponding to the given <see cref="HsqlProviderType"/>.
        /// </summary>
        /// <remarks>
        /// The mapping is not 1-1.
        /// <list type="table">
        ///   <listheader>
        ///     <term>HsqlProviderType</term>
        ///     <description>DbType</description>
        ///   </listheader>
        ///   <item>
        ///     <term>
        ///      HsqlProviderType.Array - an SQL ARRAY value type, which
        ///      is a fixed-length vector of some homogenous element type.
        ///     </term>
        ///     <description>DbType.Object</description>
        ///   </item>
        ///   <item>
        ///     <term>
        ///      HsqlProviderType.BigInt - an SQL BIGINT value type, which 
        ///      is an integral number in the range 2^63 (-9,223,372,036,854,775,808)
        ///      to 2^63-1 (9,223,372,036,854,775,807).</term>
        ///     <description>DbType.Int64</description>
        ///   </item>
        ///   <item>
        ///     <term>
        ///      HsqlProviderType.Binary - an SQL BINARY value type, which is
        ///      an octet sequence of length 0 to 2^31.
        ///     </term>
        ///     <description>DbType.Binary</description>
        ///   </item>
        ///   <item>
        ///     <term>
        ///      HsqlProviderType.Blob - an SQL BLOB value type, which is a 
        ///      locator for an octet sequence of length 0 to 2^63-1.
        ///      </term>
        ///     <description>DbType.Object</description>
        ///   </item>
        ///   <item>
        ///     <term>
        ///      HsqlProviderType.Boolean - an SQL BOOLEAN value type, which
        ///      is a value from the set {TRUE, FALSE, UNKNOWN (i.e. NULL)}.
        ///     </term>
        ///     <description>DbType.Boolean</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.Char - an SQL CHAR value type, which is a fixed length Unicode character sequence of length 0 to 2^31</term>
        ///     <description>DbType.StringFixedLength</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.Clob - an SQL CLOB value type, which is a locator for an SQL Unicode character sequence of length 0 to 2^63-1.</term>
        ///     <description>DbType.Object</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.DataLink - an SQL DATALINK value type, which is an SQL MED URL for a managed external data item.</term>
        ///     <description>DbType.Object</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.Date - an SQL DATE value type, which is a temporal value in format YYYY-MM-DD</term>
        ///     <description>DbType.DateTime</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.Decimal - an SQL DECIMAL value type, which is an exact number that consists of an arbitrary precision integer unscaled value and a 32-bit integer scale.</term>
        ///     <description>DbType.Decimal</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.Distinct - an SQL DISTINCT value type, which is a user defined type (UDT) that is a named variation on a built-in scalar type, such as SHORT_NAME IS CHAR(12) or POSITIVE_INT IS INTEGER CHECK(value >= 0)</term>
        ///     <description>return DbType.Object</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.Double - an SQL DOUBLE PRECISION type which may be conceptually associated with the double-precision 64-bit format IEEE 754 values and operations specified in ANSI/IEEE Standard 754-1985.</term>
        ///     <description>DbType.Double</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.Float an SQL DOUBLE PRECISION type which may be conceptually associated with the double-precision 64-bit format IEEE 754 values and operations specified in ANSI/IEEE Standard 754-1985.</term>
        ///     <description>DbType.Double</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.Integer - an SQL INTEGER which is an integral number in the range -2^31 (-2,147,483,648) to 2^31-1 (2,147,483,647)</term>
        ///     <description>DbType.Int32</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.JavaObject</term>
        ///     <description>DbType.Object</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.LongVarBinary</term>
        ///     <description>DbType.Object</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.LongVarChar</term>
        ///     <description>DbType.String</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.Numeric</term>
        ///     <description>DbType.VarNumeric</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.Object</term>
        ///     <description>DbType.Object</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.Real</term>
        ///     <description>DbType.Single</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.Ref</term>
        ///     <description>DbType.Object</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.SmallInt - an SQL SMALLINT - 2^15 (-32,768) to 2^15-1 (32,767)</term>
        ///     <description>DbType.Int16</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.Struct</term>
        ///     <description>DbType.Object</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.Time - an SQL TIME value in format HH:MM.SS</term>
        ///     <description>DbType.DateTime</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.TimeStamp - an SQL TIMESTAMP value in format YYYY-MM-DD HH:MM:SS.ffffff</term>
        ///     <description>DbType.DateTime2</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.TinyInt</term>
        ///     <description>DbType.SByte</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.VarBinary</term>
        ///     <description>DbType.Object</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.VarChar</term>
        ///     <description>DbType.String</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.Xml</term>
        ///     <description>DbType.Xml</description>
        ///   </item>
        ///   <item>
        ///     <term>Others</term>
        ///     <description>DbType.Object</description>
        ///   </item>
        /// </list>
        /// </remarks>
        /// <param name="type">
        /// The <c>HsqlProviderType</c> for which to retrieve the
        /// corresponding <c>DbType</c>.
        /// </param>
        /// <returns>
        /// The <c>DbType</c> corresponding to the given <c>HsqlProviderType</c>.
        /// </returns>
        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1706:ShortAcronymsShouldBeUppercase", MessageId = "Member")]
        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Maintainability", "CA1502:AvoidExcessiveComplexity")]
        public static DbType ToDbType(HsqlProviderType type)
        {
            switch (type)
            {
                case HsqlProviderType.Array:
                    {
                        return DbType.Object;
                    }
                case HsqlProviderType.BigInt:
                    {
                        return DbType.Int64;
                    }
                case HsqlProviderType.Binary:
                    {
                        return DbType.Binary;
                    }
                case HsqlProviderType.Blob:
                    {
                        return DbType.Object;
                    }
                case HsqlProviderType.Boolean:
                    {
                        return DbType.Boolean;
                    }
                case HsqlProviderType.Char:
                    {
                        return DbType.StringFixedLength;
                    }
                case HsqlProviderType.Clob:
                    {
                        return DbType.Object;
                    }
                case HsqlProviderType.DataLink:
                    {
                        return DbType.Object;
                    }
                case HsqlProviderType.Date:
                    {
                        return DbType.DateTime;
                    }
                case HsqlProviderType.Decimal:
                    {
                        return DbType.Decimal;
                    }
                case HsqlProviderType.Distinct:
                    {
                        return DbType.Object;
                    }
                case HsqlProviderType.Double:
                    {
                        return DbType.Double;
                    }
                case HsqlProviderType.Float:
                    {
                        return DbType.Double;
                    }
                case HsqlProviderType.Integer:
                    {
                        return DbType.Int32;
                    }
                case HsqlProviderType.JavaObject:
                    {
                        return DbType.Object;
                    }
                case HsqlProviderType.LongVarBinary:
                    {
                        return DbType.Object;
                    }
                case HsqlProviderType.LongVarChar:
                    {
                        return DbType.String;
                    }
                case HsqlProviderType.Numeric:
                    {
                        return DbType.VarNumeric;
                    }
                case HsqlProviderType.Object:
                    {
                        return DbType.Object;
                    }
                case HsqlProviderType.Real:
                    {
                        return DbType.Single;
                    }
                case HsqlProviderType.Ref:
                    {
                        return DbType.Object;
                    }
                case HsqlProviderType.SmallInt:
                    {
                        return DbType.Int16;
                    }
                case HsqlProviderType.Struct:
                    {
                        return DbType.Object;
                    }
                case HsqlProviderType.Time:
                    {
                        return DbType.DateTime;
                    }
                case HsqlProviderType.TimeStamp:
                    {
                        return DbType.DateTime2;
                    }
                case HsqlProviderType.TinyInt:
                    {
                        return DbType.SByte;
                    }
                case HsqlProviderType.VarBinary:
                    {
                        return DbType.Object;
                    }
                case HsqlProviderType.VarChar:
                    {
                        return DbType.String;
                    }
                case HsqlProviderType.Xml:
                    {
                        return DbType.Xml;
                    }
                default:
                    {
                        return DbType.Object;
                    }
            }
        }

        #endregion

        #region ToHsqlIsolationLevel(System.Data.IsolationLevel)

        /// <summary>
        /// Retrieves the <c>HsqlIsolationLevel</c> corresponding
        /// to the given <see cref="System.Data.IsolationLevel"/>.
        /// </summary>
        /// <param name="isolationLevel">
        /// The <c>System.Data.IsolationLevel</c> for which to retrieve the
        /// corresponding <c>HsqlIsolationLevel</c>.
        /// </param>
        /// <returns>
        /// The <c>HsqlIsolationLevel</c> corresponding
        /// to the given <c>System.Data.IsolationLevel</c>.
        /// </returns>
        /// <exception cref="System.ArgumentException">
        /// When there is no <c>HsqlIsolationLevel</c> corresponding
        /// to the given <c>System.Data.IsolationLevel</c>.
        /// </exception>
        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1706:ShortAcronymsShouldBeUppercase", MessageId = "Member")]
        public static HsqlIsolationLevel ToHsqlIsolationLevel(IsolationLevel isolationLevel)
        {
            switch (isolationLevel)
            {
                case IsolationLevel.ReadCommitted:
                case IsolationLevel.Unspecified:
                    {
                        return HsqlIsolationLevel.ReadCommited;
                    }
                case IsolationLevel.ReadUncommitted:
                    {
                        return HsqlIsolationLevel.ReadUncommited;
                    }
                case IsolationLevel.RepeatableRead:
                    {
                        return HsqlIsolationLevel.RepeatableRead;
                    }
                case IsolationLevel.Serializable:
                    {
                        return HsqlIsolationLevel.Serializable;
                    }
                case IsolationLevel.Chaos:
                case IsolationLevel.Snapshot:
                default:
                    {
                        throw new ArgumentException("Unsupported: (0) "
                            + isolationLevel, "isolationLevel");
                    }
            }
        }

        #endregion

        #region ToIsLongProviderType(int)
        /// <summary>
        /// Determines whether the provider-specific data type code
        /// corresponds to a long SQL type, such as an SQL LONGVARBINARY or
        /// LONGVARCHAR type.
        /// </summary>
        /// <param name="type">
        /// The provider-specific data type code for which to make the
        /// determination.
        /// </param>
        /// <returns>
        /// <c>true</c> if the specified data type code corresponds to
        /// a long provider type; otherwise, <c>false</c>.
        /// </returns>
        public static bool ToIsLongProviderType(int type)
        {
            switch (type)
            {
                case HsqlTypes.LONGVARBINARY:
                case HsqlTypes.LONGVARCHAR:
                case HsqlTypes.BLOB:
                case HsqlTypes.CLOB:
                    {
                        return true;
                    }
                default:
                    {
                        return false;
                    }
            }
        }
        #endregion

        #region ToIsolationLevel(HsqlIsolationLevel)
        /// <summary>
        /// Retreives the <c>System.Data.IsolationLevel</c> corresponding
        /// to the given <c>HsqlIsolationLevel</c>
        /// </summary>
        /// <param name="value">The value to convert.</param>
        /// <returns><c>System.Data.IsolationLevel</c></returns>
        public static Data.IsolationLevel ToIsolationLevel(HsqlIsolationLevel value)
        {
            switch (value)
            {
                case HsqlIsolationLevel.ReadCommited:
                    {
                        return IsolationLevel.ReadCommitted;
                    }
                case HsqlIsolationLevel.ReadUncommited:
                    {
                        return IsolationLevel.ReadUncommitted;
                    }
                case HsqlIsolationLevel.RepeatableRead:
                    {
                        return IsolationLevel.RepeatableRead;
                    }
                case HsqlIsolationLevel.Serializable:
                    {
                        return IsolationLevel.Serializable;
                    }
                case HsqlIsolationLevel.None:
                    {
                        return IsolationLevel.Unspecified;
                    }
                default:
                    {
                        string message = "Unsupported Isolation Level: " + value;
                        throw new ArgumentException(message,
                                                    "value");
                    }
            }
        }
        #endregion

        #region ToIsolationLevel(System.Transactions.IsolationLevel)
        /// <summary>
        /// Retreives the <c>System.Data.IsolationLevel</c> corresponding
        /// to the given <c>System.Transactions.IsolationLevel</c>.
        /// </summary>
        /// <param name="value">The value to convert.</param>
        /// <returns><c>System.Data.IsolationLevel</c></returns>
        public static System.Data.IsolationLevel ToIsolationLevel(System.Transactions.IsolationLevel value)
        {
            switch (value)
            {
                case Transactions.IsolationLevel.Chaos:
                    {
                        return Data.IsolationLevel.Chaos;
                    }
                case Transactions.IsolationLevel.ReadCommitted:
                    {
                        return Data.IsolationLevel.ReadCommitted;
                    }
                case Transactions.IsolationLevel.ReadUncommitted:
                    {
                        return Data.IsolationLevel.ReadUncommitted;
                    }
                case Transactions.IsolationLevel.RepeatableRead:
                    {
                        return Data.IsolationLevel.RepeatableRead;
                    }
                case Transactions.IsolationLevel.Serializable:
                    {
                        return Data.IsolationLevel.Serializable;
                    }
                case Transactions.IsolationLevel.Snapshot:
                    {
                        return Data.IsolationLevel.Snapshot;
                    }
                case Transactions.IsolationLevel.Unspecified:
                default:
                    {
                        return Data.IsolationLevel.Unspecified;
                    }
            }
        }
        #endregion

        #region ToParameterDirection(ParameterMode)
        /// <summary>
        /// Retrieves the <c>ParameterDirection</c> corresponding
        /// to the given <c>ParameterMode</c>.
        /// </summary>
        /// <param name="mode">
        /// The <c>ParameterMode</c> for which to retrieve the <c>ParameterDirection</c>.
        /// </param>
        /// <returns>
        /// The <c>ParameterDirection</c> corresponding to the given <c>ParameterMode</c>;
        /// <c>null</c> if there is no such <c>ParameterDirection</c>.
        /// </returns>
        public static ParameterDirection? ToParameterDirection(ParameterMode mode)
        {
            switch (mode)
            {
                case ParameterMode.In:
                    {
                        return ParameterDirection.Input;
                    }
                case ParameterMode.InOut:
                    {
                        return ParameterDirection.InputOutput;
                    }
                case ParameterMode.Out:
                    {
                        return ParameterDirection.Output;
                    }
                case ParameterMode.Unknown:
                default:
                    {
                        return null;
                    }
            }
        }
        #endregion

        /// <summary>
        /// 
        /// </summary>
        /// <param name="type"></param>
        /// <returns></returns>
        public static string ToSqlDataTypeName(int type)
        {
            return HsqlTypes.getTypeName(type);
        }

        #endregion

        #region Code To System.Type Conversion Methods

        #region ToDataType(HsqlProviderType)

        /// <summary>
        /// Retrieves the default <see cref="System.Type"/> used
        /// to represent values of the given <c>HsqlProviderType</c>.
        /// </summary>
        /// <remarks>
        /// This is simply a convenience method that is equivalent to invoking
        /// <see cref="ToDataType(int)"/> upon the result of casting the given
        /// <c>HsqlProviderType</c> value to <c>int</c>.
        /// </remarks>
        /// <param name="dbType">
        /// An HSQLDB-specifc data type code.
        /// </param>
        /// <returns>
        /// The default <c>System.Type</c> that the driver uses to
        /// expose values of the given SQL data type to client software.
        /// </returns>
        public static Type ToDataType(HsqlProviderType dbType)
        {
            return ToDataType((int)dbType);
        }

        #endregion

        #region ToDataType(int)

        /// <summary>
        /// Retrieves the default <see cref="System.Type"/> used
        /// to represent values of the given HSQLDB data type.
        /// </summary>
        /// <remarks>
        /// The retrieved value applies in particular to HSQLDB ADO.NET
        /// data provider methods having return type of object or object[],
        /// such as <c>HsqlDataReader.GetValue(System.Int32)</c> and its
        /// variations, e.g. <c>HsqlDataReader.GetValues(object[])</c>.
        /// </remarks>
        /// <param name="type">The HSQLDB data type code.</param>
        /// <returns>
        /// The default <c>System.Type</c> used to represent values of the
        /// given HSQLDB data type.
        /// </returns>
        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Maintainability", "CA1502:AvoidExcessiveComplexity")]
        public static Type ToDataType(int type)
        {
            switch (type)
            {
                case (int)HsqlProviderType.Array:
                    {
                        return typeof(object);
                    }
                case (int)HsqlProviderType.BigInt:
                    {
                        return typeof(long);
                    }
                case (int)HsqlProviderType.Blob:
                    {
                        return typeof(object);
                    }
                case (int)HsqlProviderType.Binary:
                case (int)HsqlProviderType.VarBinary:
                case (int)HsqlProviderType.LongVarBinary:
                    {
                        return typeof(byte[]);
                    }
                case (int)HsqlProviderType.Boolean:
                    {
                        return typeof(bool);
                    }
                case (int)HsqlProviderType.Clob:
                    {
                        return typeof(object);
                    }
                case (int)HsqlProviderType.Char:
                case (int)HsqlProviderType.VarChar:
                case (int)HsqlProviderType.LongVarChar:
                    {
                        return typeof(string);
                    }
                case (int)HsqlProviderType.Date:
                case (int)HsqlProviderType.Time:
                case (int)HsqlProviderType.TimeStamp:
                    {
                        return typeof(DateTime);
                    }
                case (int)HsqlProviderType.Decimal:
                case (int)HsqlProviderType.Numeric:
                    {
                        return typeof(decimal);
                    }
                case (int)HsqlProviderType.Double:
                case (int)HsqlProviderType.Float:
                    {
                        return typeof(double);
                    }
                case (int)HsqlProviderType.Integer:
                    {
                        return typeof(int);
                    }
                case (int)HsqlProviderType.Null:
                    {
                        return typeof(object);
                    }
                case (int)HsqlProviderType.Real:
                    {
                        return typeof(float);
                    }
                case (int)HsqlProviderType.SmallInt:
                    {
                        return typeof(short);
                    }
                case (int)HsqlProviderType.TinyInt:
                    {
                        return typeof(sbyte);
                    }
                case (int)HsqlProviderType.Xml:
                    {
                        return typeof(string);
                    }
                default:
                    {
                        return typeof(object);
                    }
            }
        }

        #endregion

        #region ToProviderSpecificDataType(HsqlProviderType)
        /// <summary>
        /// Retrieves the provider-specific (i.e. internal) CLR 
        /// <c>System.Type</c> used to represent values of the
        /// given provider-specific SQL type.
        /// </summary>
        /// <param name="type">The provider specific SQL type</param>
        /// <returns>
        /// The corresponding provider specific CLR <c>System.Type</c> used 
        /// to represent values of the given provider-specific SQL type.
        /// </returns>
        public static Type ToProviderSpecificDataType(HsqlProviderType type)
        {
            return HsqlConvert.ToProviderSpecificDataType((int)type);
        }
        #endregion

        #region ToProviderSpecificDataType(int)
        /// <summary>
        /// Retrieves the <see cref="System.Type"/> used internally
        /// by this HSQLDB ADO.NET data provider implementation to 
        /// represent values of the SQL type identified by the given
        /// data type code.
        /// </summary>
        /// <remarks>
        /// See <see cref="HsqlProviderType"/> and <see cref="org.hsqldb.Types"/>
        /// </remarks>
        /// <param name="type">The SQL data type code.</param>
        /// <returns>
        /// The <see cref="System.Type"/> used internally by the HSQLDB
        /// ADO.NET data provider to represent values of the indicated
        /// SQL data type.
        /// </returns>
        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Maintainability", "CA1502:AvoidExcessiveComplexity")]
        public static Type ToProviderSpecificDataType(int type)
        {
            switch (type)
            {
                case (int)HsqlProviderType.Array:
                    {
                        return typeof(java.sql.Array);
                    }
                case (int)HsqlProviderType.BigInt:
                    {
                        return typeof(JavaLong);
                    }
                case (int)HsqlProviderType.Binary:
                    {
                        return typeof(byte[]);
                    }
                case (int)HsqlProviderType.Blob:
                    {
                        return typeof(IBlob);
                    }
                case (int)HsqlProviderType.Boolean:
                    {
                        return typeof(JavaBoolean);
                    }
                case (int)HsqlProviderType.Char:
                    {
                        return typeof(string);
                    }
                case (int)HsqlProviderType.Clob:
                    {
                        return typeof(IClob);
                    }
                case (int)HsqlProviderType.DataLink:
                    {
                        return typeof(java.net.URL);
                    }
                case (int)HsqlProviderType.Date:
                    {
                        return typeof(java.sql.Date);
                    }
                case (int)HsqlProviderType.Decimal:
                    {
                        return typeof(java.math.BigDecimal);
                    }
                case (int)HsqlProviderType.Distinct:
                    {
                        return typeof(object);
                    }
                case (int)HsqlProviderType.Double:
                    {
                        return typeof(java.lang.Double);
                    }
                case (int)HsqlProviderType.Float:
                    {
                        return typeof(java.lang.Double);
                    }
                case (int)HsqlProviderType.Integer:
                    {
                        return typeof(java.lang.Integer);
                    }
                case (int)HsqlProviderType.JavaObject:
                    {
                        return typeof(java.lang.Object);
                    }
                case (int)HsqlProviderType.LongVarBinary:
                    {
                        return typeof(byte[]);
                    }
                case (int)HsqlProviderType.LongVarChar:
                    {
                        return typeof(string);
                    }
                case (int)HsqlProviderType.Null:
                    {
                        return typeof(void);
                    }
                case (int)HsqlProviderType.Numeric:
                    {
                        return typeof(java.math.BigDecimal);
                    }
                case (int)HsqlProviderType.Object:
                    {
                        return typeof(SqlObject);
                    }
                case (int)HsqlProviderType.Real:
                    {
                        return typeof(java.lang.Double);
                    }
                case (int)HsqlProviderType.Ref:
                    {
                        return typeof(java.sql.Ref);
                    }
                case (int)HsqlProviderType.SmallInt:
                    {
                        return typeof(java.lang.Integer);
                    }
                case (int)HsqlProviderType.Struct:
                    {
                        return typeof(java.sql.Struct);
                    }
                case (int)HsqlProviderType.Time:
                    {
                        return typeof(java.sql.Time);
                    }
                case (int)HsqlProviderType.TimeStamp:
                    {
                        return typeof(java.sql.Timestamp);
                    }
                case (int)HsqlProviderType.TinyInt:
                    {
                        return typeof(java.lang.Integer);
                    }
                case (int)HsqlProviderType.VarBinary:
                    {
                        return typeof(byte[]);
                    }
                case (int)HsqlProviderType.VarChar:
                    {
                        return typeof(string);
                    }
                case HsqlTypes.VARCHAR_IGNORECASE:
                    {
                        return typeof(string);
                    }
                case (int)HsqlProviderType.Xml:
                    {
                        return typeof(java.sql.SQLXML);
                    }
                default:
                    {
                        return typeof(object);
                    }
            }
        }
        #endregion

        #endregion

        #region ToSqlLiteral(IDataParameter)
        /// <summary>
        /// Retreives the SQL literal representation of the given parameter's value.
        /// </summary>
        /// <remarks>
        /// The resulting character sequence may contain SQL CAST expressions to
        /// ensure that the literal is interpreted as the intended SQL data type.
        /// </remarks>
        /// <param name="parameter">The parameter.</param>
        /// <returns>The SQL literal representation.</returns>
        /// <exception cref="HsqlDataSourceException">
        /// When there is no conversion for the <c>DbType</c> of the given
        /// parameter object.
        /// </exception>
        public static string ToSqlLiteral(IDataParameter parameter)
        {
            if (parameter == null)
            {
                throw new ArgumentNullException("parameter");
            }

            object objectValue = parameter.Value;

            if ((objectValue == null)
                || (Convert.IsDBNull(objectValue))
                || ((objectValue is INullable) && ((INullable)objectValue).IsNull))
            {
                return "NULL";
            }

            switch (parameter.DbType)
            {
                case DbType.AnsiString:
                case DbType.AnsiStringFixedLength:
                case DbType.String:
                case DbType.StringFixedLength:
                case DbType.Xml:
                    {
                        return StringConverter.toQuotedString(FromDotNet.
                            ToString(objectValue), '\'', true);
                    }
                case DbType.Binary:
                    {
                        byte[] bytes = FromDotNet.ToBinary(objectValue)
                            .getBytes();
                        string hex = StringConverter.byteToHex(bytes);

                        StringBuilder sb = new StringBuilder(
                            hex.Length + "CAST('' AS BINARY)".Length);

                        return sb.Append("CAST('").Append(hex).Append(
                            "' AS BINARY)").ToString();
                    }
                case DbType.Boolean:
                    {
                        return FromDotNet.ToBoolean(objectValue).booleanValue()
                            ? "TRUE" : "FALSE";
                    }
                case DbType.Byte:
                    {
                        return FromDotNet.ToSmallInt(objectValue).toString();
                    }
                case DbType.Currency:
                    {
                        return HsqlConvert.FromDotNet.ToDecimal(objectValue)
                            .setScale(4, JavaBigDecimal.ROUND_HALF_UP)
                            .toPlainString();
                    }
                case DbType.Date:
                    {
                        StringBuilder sb = new StringBuilder(26);

                        return sb.Append("CAST('").Append(FromDotNet.ToDate(
                            objectValue)).Append("' AS DATE)").ToString();
                    }
                case DbType.DateTime:
                    {
                        StringBuilder sb = new StringBuilder(45);

                        return sb.Append("CAST('").Append(
                            FromDotNet.ToTimestamp(objectValue)).Append(
                            "' AS TIMESTAMP)").ToString();
                    }
                case DbType.Decimal:
                    {
                        // Using toPlainString() to disable scientific
                        // notation so that the engine treats the value
                        // as an SQL DECIMAL, not a DOUBLE.
                        return FromDotNet.ToDecimal(objectValue).toPlainString();
                    }
                case DbType.Double:
                    {
                        double doubleValue = FromDotNet.ToDouble(objectValue)
                            .doubleValue();


                        if (doubleValue == java.lang.Double.NEGATIVE_INFINITY)
                        {
                            return "-1E0/0";
                        }

                        if (doubleValue == java.lang.Double.POSITIVE_INFINITY)
                        {
                            return "1E0/0";
                        }

                        if (java.lang.Double.isNaN(doubleValue))
                        {
                            return "0E0/0E0";
                        }

                        string stringValue = doubleValue.ToString();

                        // ensure the engine treats the value as a DOUBLE, not a DECIMAL
                        if (stringValue.IndexOf('E') < 0)
                        {
                            stringValue = string.Concat(stringValue, "E0");
                        }

                        return stringValue;
                    }
                case DbType.Guid:
                    {
                        // TODO: cast? udf?
                        string stringValue = FromDotNet.ToBinary(objectValue)
                            .toString();

                        StringBuilder sb = new StringBuilder(
                            stringValue.Length + 2);

                        return sb.Append('\'').Append(stringValue).Append(
                            '\'').ToString();
                    }
                case DbType.Int16:
                    {
                        return FromDotNet.ToSmallInt(objectValue).toString();
                    }
                case DbType.Int32:
                    {
                        return FromDotNet.ToInteger(objectValue).toString();
                    }
                case DbType.Int64:
                    {
                        return FromDotNet.ToBigInt(objectValue).toString();
                    }
                case DbType.Object:
                    {
                        // TODO: cast? udf?
                        byte[] bytes = FromDotNet.ToOther(objectValue)
                            .getBytes();
                        string hex = StringConverter.byteToHex(bytes);

                        return StringConverter.toQuotedString(hex, '\'', false);
                    }
                case DbType.SByte:
                    {
                        return FromDotNet.ToTinyInt(objectValue).toString();
                    }
                case DbType.Single:
                    {
                        float floatValue = FromDotNet.ToReal(objectValue)
                            .floatValue();

                        if (floatValue == JavaFloat.NEGATIVE_INFINITY)
                        {
                            return "-1E0/0";
                        }

                        if (floatValue == JavaFloat.POSITIVE_INFINITY)
                        {
                            return "1E0/0";
                        }

                        if (JavaFloat.isNaN(floatValue))
                        {
                            return "0E0/0E0";
                        }

                        string stringValue = floatValue.ToString();

                        // ensure the engine treats the value as a REAL,
                        // not a DECIMAL
                        if (stringValue.IndexOf('E') < 0)
                        {
                            stringValue = string.Concat(stringValue, "E0");
                        }

                        return stringValue;
                    }
                case DbType.Time:
                    {
                        StringBuilder sb = new StringBuilder(24);

                        return sb.Append("CAST('").Append(FromDotNet.ToTime(
                            objectValue)).Append("' AS TIME)").ToString();
                    }
                case DbType.UInt16:
                    {
                        return FromDotNet.ToInteger(objectValue).toString();
                    }
                case DbType.UInt32:
                    {
                        return FromDotNet.ToBigInt(objectValue).toString();
                    }
                case DbType.UInt64:
                    {
                        return FromDotNet.ToDecimal(objectValue)
                            .toBigInteger().toString();
                    }
                case DbType.VarNumeric:
                    {
                        // Using toPlainString() to disable scientific
                        // notation so that the engine treats the value
                        // as an SQL NUMERIC, not a DOUBLE.
                        return FromDotNet.ToDecimal(objectValue)
                            .toPlainString();
                    }
                default:
                    {
                        throw new HsqlDataSourceException(
                            "Unknown DbType: " + parameter.DbType); // NOI18N
                    }
            }
        }
        #endregion



        #region Exception Factory Methods

        #region NumericValueOutOfRange(object)
        /// <summary>
        /// Creates a new "numeric value out of range" exception.
        /// </summary>
        /// <param name="n">
        /// The number valued <c>object</c> that is out of range.
        /// </param>
        /// <returns><c>HsqlDataSourceException</c></returns>
        public static HsqlDataSourceException NumericValueOutOfRange(object n)
        {
            HsqlException ex = Trace.error(
                Trace.NUMERIC_VALUE_OUT_OF_RANGE, n);

            return new HsqlDataSourceException(ex);
        }
        #endregion

        #region WrongDataType(object)
        /// <summary>
        /// Creates a new "wrong data type" exception.
        /// </summary>
        /// <param name="o">
        /// The <c>object</c> that is of the wrong data type.
        /// </param>
        /// <returns><c>HsqlDataSourceException</c></returns>
        public static HsqlDataSourceException WrongDataType(object o)
        {
            HsqlException hex = Trace.error(Trace.WRONG_DATA_TYPE,
                (o == null) ? "null" : "" + o.GetType());

            return new HsqlDataSourceException(hex);
        }
        #endregion

        #region InvalidConversion(int)

        /// <summary>
        /// Creates a new "invalid conversion" exception.
        /// </summary>
        /// <param name="type">
        /// The data type code for which the conversion is invalid.
        /// </param>
        /// <returns><c>HsqlDataSourceException</c></returns>
        public static HsqlDataSourceException InvalidConversion(int type)
        {
            HsqlException hex = Trace.error(Trace.INVALID_CONVERSION,
                HsqlTypes.getTypeName(type));

            return new HsqlDataSourceException(hex);
        }
        #endregion

        /// <summary>
        /// Creates a new "invalid conversion" exception.
        /// </summary>
        /// <param name="o">The source object for which the conversion is invalid.</param>
        /// <param name="targetType">The data type code for which the conversion is invalid.</param>
        /// <returns><c>HsqlDataSourceException</c></returns>
        public static HsqlDataSourceException UnknownConversion(Object o, int targetType)
        {
            string format =
                "Unknown target SQL data type: {0} for source type: {1}";
            string sourceType = (o == null) ? "null" : o.GetType().FullName;
            HsqlException hex = Trace.error(Trace.INVALID_CONVERSION,
                string.Format(format, targetType, sourceType));

            return new HsqlDataSourceException(hex);
        }

        #endregion
    }
}
