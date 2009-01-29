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
using Types = org.hsqldb.Types;

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
        private static readonly JavaBigDecimal MIN_DOUBLE = java.math.BigDecimal.valueOf(JavaDouble.MIN_VALUE);
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
        ///     <term>DbType.AnsiString</term>
        ///     <description>HsqlProviderType.Binary</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.AnsiStringFixedLength</term>
        ///     <description>HsqlProviderType.Binary</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.Binary</term>
        ///     <description>HsqlProviderType.Binary</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.Boolean</term>
        ///     <description>HsqlProviderType.Boolean</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.Byte</term>
        ///     <description>HsqlProviderType.SmallInt</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.Currency</term>
        ///     <description>HsqlProviderType.Decimal</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.Date</term>
        ///     <description>HsqlProviderType.TimeStamp</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.DateTime</term>
        ///     <description>HsqlProviderType.TimeStamp</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.Decimal</term>
        ///     <description>HsqlProviderType.Decimal</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.Double</term>
        ///     <description>HsqlProviderType.Double</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.Guid</term>
        ///     <description>HsqlProviderType.Binary</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.Int16</term>
        ///     <description>HsqlProviderType.SmallInt</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.Int32</term>
        ///     <description>HsqlProviderType.Integer</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.Int64</term>
        ///     <description>HsqlProviderType.BigInt</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.Object</term>
        ///     <description>HsqlProviderType.Object</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.SByte</term>
        ///     <description>HsqlProviderType.TinyInt</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.Single</term>
        ///     <description>HsqlProviderType.Real</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.String</term>
        ///     <description>HsqlProviderType.VarChar</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.StringFixedLength</term>
        ///     <description>HsqlProviderType.Char</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.Time</term>
        ///     <description>HsqlProviderType.TimeStamp</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.UInt16</term>
        ///     <description>HsqlProviderType.Integer</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.UInt32</term>
        ///     <description>HsqlProviderType.BigInt</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.UInt64</term>
        ///     <description>HsqlProviderType.Numeric</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.VarNumeric</term>
        ///     <description>HsqlProviderType.Numeric</description>
        ///   </item>
        ///   <item>
        ///     <term>DbType.Xml</term>
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
            switch(type)
            {
                case DbType.AnsiString :
                    {
                        return HsqlProviderType.Binary;
                    }
                case DbType.AnsiStringFixedLength :
                    {
                        return HsqlProviderType.Binary;
                    }
                case DbType.Binary :
                    {
                        return HsqlProviderType.Binary;
                    }
                case DbType.Boolean :
                    {
                        return HsqlProviderType.Boolean;
                    }
                case DbType.Byte :
                    {
                        return HsqlProviderType.SmallInt;
                    }
                case DbType.Currency :
                    {
                        return HsqlProviderType.Decimal;
                    }
                case DbType.Date :
                    {
                        return HsqlProviderType.TimeStamp;
                    }
                case DbType.DateTime :
                    {
                        return HsqlProviderType.TimeStamp;
                    }
                case DbType.Decimal :
                    {
                        return HsqlProviderType.Decimal;
                    }
                case DbType.Double :
                    {
                        return HsqlProviderType.Double;
                    }
                case DbType.Guid :
                    {
                        return HsqlProviderType.Binary;
                    }
                case DbType.Int16 :
                    {
                        return HsqlProviderType.SmallInt;
                    }
                case DbType.Int32 :
                    {
                        return HsqlProviderType.Integer;
                    }
                case DbType.Int64 :
                    {
                        return HsqlProviderType.BigInt;
                    }
                case DbType.Object :
                    {
                        return HsqlProviderType.Object;
                    }
                case DbType.SByte :
                    {
                        return HsqlProviderType.TinyInt;
                    }
                case DbType.Single :
                    {
                        return HsqlProviderType.Real;
                    }
                case DbType.String :
                    {
                        return HsqlProviderType.VarChar;
                    }
                case DbType.StringFixedLength :
                    {
                        return HsqlProviderType.Char;
                    }
                case DbType.Time :
                    {
                        return HsqlProviderType.TimeStamp;
                    }
                case DbType.UInt16 :
                    {
                        return HsqlProviderType.Integer;
                    }
                case DbType.UInt32 :
                    {
                        return HsqlProviderType.BigInt;
                    }
                case DbType.UInt64 :
                    {
                        return HsqlProviderType.Numeric;
                    }
                case DbType.VarNumeric :
                    {
                        return HsqlProviderType.Numeric;
                    }
                case DbType.Xml :
                    {
                        return HsqlProviderType.Xml;
                    }
                    default :
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
        ///     <term>HsqlProviderType.Array</term>
        ///     <description>DbType.Object</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.BigInt</term>
        ///     <description>DbType.Int64</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.Binary</term>
        ///     <description>DbType.Binary</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.Blob</term>
        ///     <description>DbType.Object</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.Boolean</term>
        ///     <description>DbType.Boolean</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.Char</term>
        ///     <description>DbType.StringFixedLength</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.Clob</term>
        ///     <description>DbType.String</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.DataLink</term>
        ///     <description>DbType.Object</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.Date</term>
        ///     <description>DbType.DateTime</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.Decimal</term>
        ///     <description>DbType.Decimal</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.Distinct</term>
        ///     <description>return DbType.Object</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.Double</term>
        ///     <description>DbType.Double</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.Float</term>
        ///     <description>DbType.Double</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.Integer</term>
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
        ///     <term>HsqlProviderType.SmallInt</term>
        ///     <description>DbType.Int16</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.Struct</term>
        ///     <description>DbType.Object</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.Time</term>
        ///     <description>DbType.DateTime</description>
        ///   </item>
        ///   <item>
        ///     <term>HsqlProviderType.TimeStamp</term>
        ///     <description>DbType.DateTime</description>
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
            switch(type)
            {
                case HsqlProviderType.Array :
                    {
                        return DbType.Object;
                    }
                case HsqlProviderType.BigInt :
                    {
                        return DbType.Int64;
                    }
                case HsqlProviderType.Binary :
                    {
                        return DbType.Binary;
                    }
                case HsqlProviderType.Blob :
                    {
                    return DbType.Object;
                    }
                case HsqlProviderType.Boolean :
                    {
                        return DbType.Boolean;
                    }
                case HsqlProviderType.Char :
                    {
                        return DbType.StringFixedLength;
                    }
                case HsqlProviderType.Clob :
                    {
                        return DbType.String;
                    }
                case HsqlProviderType.DataLink :
                    {
                        return DbType.Object;
                    }
                case HsqlProviderType.Date :
                    {
                        return DbType.DateTime;
                    }
                case HsqlProviderType.Decimal :
                    {
                        return DbType.Decimal;
                    }
                case HsqlProviderType.Distinct :
                    {
                        return DbType.Object;
                    }
                case HsqlProviderType.Double :
                    {
                        return DbType.Double;
                    }
                case HsqlProviderType.Float :
                    {
                        return DbType.Double;
                    }
                case HsqlProviderType.Integer :
                    {
                        return DbType.Int32;
                    }
                case HsqlProviderType.JavaObject :
                    {
                        return DbType.Object;
                    }
                case HsqlProviderType.LongVarBinary :
                    {
                        return DbType.Object;
                    }
                case HsqlProviderType.LongVarChar :
                    {
                        return DbType.String;
                    }
                case HsqlProviderType.Numeric :
                    {
                        return DbType.VarNumeric;
                    }
                case HsqlProviderType.Object :
                    {
                        return DbType.Object;
                    }
                case HsqlProviderType.Real :
                    {
                        return DbType.Single;
                    }
                case HsqlProviderType.Ref :
                    {
                        return DbType.Object;
                    }
                case HsqlProviderType.SmallInt :
                    {
                        return DbType.Int16;
                    }
                case HsqlProviderType.Struct :
                    {
                        return DbType.Object;
                    }
                case HsqlProviderType.Time :
                    {
                        return DbType.DateTime;
                    }
                case HsqlProviderType.TimeStamp :
                    {
                        return DbType.DateTime;
                    }
                case HsqlProviderType.TinyInt :
                    {
                        return DbType.SByte;
                    }
                case HsqlProviderType.VarBinary :
                    {
                        return DbType.Object;
                    }
                case HsqlProviderType.VarChar :
                    {
                        return DbType.String;
                    }
                case HsqlProviderType.Xml :
                    {
                        return DbType.Xml;
                    }
                default :
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
            switch(isolationLevel)
            {
                case IsolationLevel.ReadCommitted :
                case IsolationLevel.Unspecified:
                    {
                        return HsqlIsolationLevel.ReadCommited;
                    }
                case IsolationLevel.ReadUncommitted :
                    {
                        return HsqlIsolationLevel.ReadUncommited;
                    }
                case IsolationLevel.RepeatableRead :
                    {
                        return HsqlIsolationLevel.RepeatableRead;
                    }
                case IsolationLevel.Serializable :
                    {
                        return HsqlIsolationLevel.Serializable;
                    }
                case IsolationLevel.Chaos :
                case IsolationLevel.Snapshot :
                default :
                    {
                        throw new ArgumentException("Unsupported: (0) "
                            + isolationLevel, "isolationLevel");
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
            switch(value)
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

        #endregion

        #region Code To System.Type Conversion Methods

        #region ToDataType(HsqlProviderType)

        /// <summary>
        /// Retrieves the <see cref="System.Type"/> that the driver uses
        /// to represent values of the given HSQLDB SQL data type.
        /// </summary>
        /// <param name="dbType">
        /// The data type code returned by the HSQLDB driver.
        /// </param>
        /// <returns>
        /// The default <c>System.Type</c> that the driver uses to
        /// represent values of the given SQL data type.
        /// </returns>
        public static Type ToDataType(HsqlProviderType dbType)
        {
            return ToDataType((int)dbType);
        }

        #endregion

        #region ToDataType(int)

        /// <summary>
        /// Retrieves the <see cref="System.Type"/> that the driver uses
        /// to represent values of the given HSQLDB SQL data type.
        /// </summary>
        /// <param name="type">The HSQLDB SQL data type code.</param>
        /// <returns>
        /// The default <c>System.Type</c> that the driver uses to
        /// represent values of the given SQL data type.
        /// </returns>
        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Maintainability", "CA1502:AvoidExcessiveComplexity")]
        public static Type ToDataType(int type)
        {
            switch( type )
            {
                case (int)HsqlProviderType.Array :
                    {
                        return typeof(object);
                    }
                case (int)HsqlProviderType.BigInt :
                    {
                        return typeof(long);
                    }
                case (int)HsqlProviderType.Blob :
                    {
                        return typeof(object);
                    }
                case (int)HsqlProviderType.Binary :
                case (int)HsqlProviderType.VarBinary :
                case (int)HsqlProviderType.LongVarBinary :
                    {
                        return typeof(byte[]);
                    }
                case (int)HsqlProviderType.Boolean :
                    {
                        return typeof(bool);
                    }
                case (int)HsqlProviderType.Clob :
                    {
                        return typeof(object);
                    }
                case (int)HsqlProviderType.Char :
                case (int)HsqlProviderType.VarChar :
                case (int)HsqlProviderType.LongVarChar :
                    {
                        return typeof(string);
                    }
                case (int)HsqlProviderType.Date :
                case (int)HsqlProviderType.Time :
                case (int)HsqlProviderType.TimeStamp :
                    {
                        return typeof(DateTime);
                    }
                case (int)HsqlProviderType.Decimal :
                case (int)HsqlProviderType.Numeric :
                    {
                        return typeof(decimal);
                    }
                case (int)HsqlProviderType.Double :
                case (int)HsqlProviderType.Float :
                    {
                        return typeof(double);
                    }
                case (int)HsqlProviderType.Integer :
                    {
                        return typeof(int);
                    }
                case (int)HsqlProviderType.Null :
                    {
                        return typeof(object);
                    }
                case (int)HsqlProviderType.Real :
                    {
                        return typeof(double);
                    }
                case (int)HsqlProviderType.SmallInt :
                    {
                        return typeof(int);
                    }
                case (int)HsqlProviderType.TinyInt :
                    {
                        return typeof(int);
                    }
                default:
                    {
                        return typeof(object);
                    }
            }
        }

        #endregion

        #region ToProviderSpecificDataType(int)
        /// <summary>
        /// Retrieves the provider-specific type, given the SQL data type code.
        /// </summary>
        /// <param name="type">The SQL data type code.</param>
        /// <returns>The provider-specific type</returns>
        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Maintainability", "CA1502:AvoidExcessiveComplexity")]
        public static Type ToProviderSpecificDataType(int type)
        {
            switch (type)
            {
                case Types.ARRAY:
                    {
                        return typeof(java.sql.Array);
                    }
                case Types.BIGINT:
                    {
                        return typeof(java.lang.Long);
                    }
                case Types.BINARY:
                    {
                        return typeof(byte[]);
                    }
                case Types.BLOB:
                    {
                        return typeof(IBlob);
                    }
                case Types.BOOLEAN:
                    {
                        return typeof(java.lang.Boolean);
                    }
                case Types.CHAR:
                    {
                        return typeof(string);
                    }
                case Types.CLOB:
                    {
                        return typeof(IClob);
                    }
                case Types.DATALINK:
                    {
                        return typeof(java.net.URL);
                    }
                case Types.DATE:
                    {
                        return typeof(java.sql.Date);
                    }
                case Types.DECIMAL:
                    {
                        return typeof(java.math.BigDecimal);
                    }
                case Types.DISTINCT:
                    {
                        return typeof(object);
                    }
                case Types.DOUBLE:
                    {
                        return typeof(java.lang.Double);
                    }
                case Types.FLOAT:
                    {
                        return typeof(java.lang.Double);
                    }
                case Types.INTEGER:
                    {
                        return typeof(java.lang.Integer);
                    }
                case Types.JAVA_OBJECT:
                    {
                        return typeof(java.lang.Object);
                    }
                case Types.LONGVARBINARY:
                    {
                        return typeof(byte[]);
                    }
                case Types.LONGVARCHAR:
                    {
                        return typeof(string);
                    }
                case Types.NULL:
                    {
                        return typeof(void);
                    }
                case Types.NUMERIC:
                    {
                        return typeof(java.math.BigDecimal);
                    }
                case Types.OTHER:
                    {
                        return typeof(object);
                    }
                case Types.REAL:
                    {
                        return typeof(java.lang.Double);
                    }
                case Types.REF:
                    {
                        return typeof(java.sql.Ref);
                    }
                case Types.SMALLINT:
                    {
                        return typeof(java.lang.Integer);
                    }
                case Types.STRUCT:
                    {
                        return typeof(java.sql.Struct);
                    }
                case Types.TIME:
                    {
                        return typeof(java.sql.Time);
                    }
                case Types.TIMESTAMP:
                    {
                        return typeof(java.sql.Timestamp);
                    }
                case Types.TINYINT:
                    {
                        return typeof(java.lang.Integer);
                    }
                case Types.VARBINARY:
                    {
                        return typeof(byte[]);
                    }
                case Types.VARCHAR:
                    {
                        return typeof(string);
                    }
                case Types.VARCHAR_IGNORECASE:
                    {
                        return typeof(string);
                    }
                case Types.XML:
                    {
                        return typeof(object);
                    }
                default:
                    {
                        return typeof(object);
                    }
            }
        } 
        #endregion

        #endregion

        #region ToSqlLiteral(HsqlParameter)
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
        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Design", "CA1011:ConsiderPassingBaseTypesAsParameters")]
        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Maintainability", "CA1502:AvoidExcessiveComplexity")]
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

                        return sb.Append("CAST('")
                            .Append(hex).Append("' AS BINARY)").ToString();
                    }
                case DbType.Boolean:
                    {
                        return FromDotNet.ToBoolean(objectValue)
                            .booleanValue() ? "TRUE" : "FALSE";
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

                        return sb.Append("CAST('")
                            .Append(FromDotNet.ToDate(objectValue))
                            .Append("' AS DATE)").ToString();
                    }
                case DbType.DateTime:
                    {
                        StringBuilder sb = new StringBuilder(45);

                        return sb.Append("CAST('")
                            .Append(FromDotNet.ToTimestamp(objectValue))
                            .Append("' AS TIMESTAMP)").ToString();
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

                        return sb.Append('\'').Append(stringValue)
                            .Append('\'').ToString();
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

                        return sb.Append("CAST('")
                            .Append(FromDotNet.ToTime(objectValue))
                            .Append("' AS TIME)")
                            .ToString();
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

        #region Number Parsing

        #region ParseInteger(string)
        /// <summary>
        /// Parses the given value as an HSQLDB SQL INTEGER.
        /// </summary>
        /// <remarks>
        /// The legal input formats are those supported by
        /// <c>java.lang.Integer.ParseInt</c>
        /// </remarks>
        /// <param name="value">The value to parse.</param>
        /// <returns>an SQL INTEGER representation of the string</returns>
        /// <exception cref="HsqlDataSourceException">
        /// When a number format exception is encountered.
        /// </exception>
        public static int ParseInteger(string value)
        {
            try
            {
                return JavaInteger.parseInt(value);
            }
            catch (java.lang.NumberFormatException nfe)
            {
                throw new HsqlDataSourceException(Trace.error(
                    Trace.INVALID_CONVERSION, nfe.toString()));
            }
        }
        #endregion

        #region ParseBigInt(string)
        /// <summary>
        /// Parses the given value as an HSQLDB SQL BIGINT.
        /// </summary>
        /// <remarks>
        /// The legal input formats are those supported by
        /// <c>java.lang.Long.ParseLong</c>
        /// </remarks>
        /// <param name="value">The value to parse.</param>
        /// <returns>An SQL BIGINT representation of the given value.</returns>
        /// <exception cref="HsqlDataSourceException">
        /// When a number format exception is encountered.
        /// </exception>
        public static long ParseBigInt(string value)
        {
            try
            {
                return JavaLong.parseLong(value);
            }
            catch (java.lang.NumberFormatException nfe)
            {
                throw new HsqlDataSourceException(Trace.error(
                    Trace.INVALID_CONVERSION, nfe.toString()));
            }
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
            try
            {
                return JavaDouble.parseDouble(value);
            }
            catch (java.lang.NumberFormatException nfe)
            {
                throw new HsqlDataSourceException(Trace.error(
                    Trace.INVALID_CONVERSION,nfe.toString()));
            }
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
                Types.getTypeName(type));

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
            string format
                = "Unknown target SQL data type: {0} for source type: {1}"; // NOI18N
            string sourceType = (o == null) 
                ? "null"
                : o.GetType().FullName;
            HsqlException hex = Trace.error(Trace.INVALID_CONVERSION,
                string.Format(format,targetType,sourceType));

            return new HsqlDataSourceException(hex);
        }

        #endregion

    }
}
