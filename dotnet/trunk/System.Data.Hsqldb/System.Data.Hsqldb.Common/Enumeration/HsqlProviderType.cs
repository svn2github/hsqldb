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
using Types = org.hsqldb.Types;
using System.ComponentModel;
using System.Collections;
using System.Collections.Generic;
using System;
//using System.Data.Hsqldb.Common.Design;
//using System.Drawing.Design;
using System.Data.Hsqldb.Common.Converter;
#endregion

namespace System.Data.Hsqldb.Common.Enumeration
{
    #region HsqlProviderType

    /// <summary>
    /// <para>
    /// HSQLDB-specific data type codes.
    /// </para>
    /// <img src="..Documentation//ClassDiagrams/System.Data.Hsqldb.Common.Enumeration.HsqlProviderType.png"
    ///      alt="HsqlProviderType Class Diagram"/>
    /// </summary>
    /// <remarks>
    /// Enumerates the native data types of field, property or parameter
    /// objects of the HSQLDB ADO.NET data provider.
    /// </remarks>
    /// <author name="boucherb@users"/>
    [TypeConverter(typeof(LexographicEnumConverter))]
    public enum HsqlProviderType
    {
        #region Array
        /// <summary>
        /// Identifies the generic SQL type <c>ARRAY</c>.
        /// </summary>
        /// <remarks>
        /// Although not implemented in HSQLDB 1.8.0, the SQL ARRAY
        /// type denotes an indexable fixed-length vector of some
        /// homogenous element type.
        /// </remarks>
        [Browsable(false)]
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        Array = Types.ARRAY,
        #endregion
        #region BigInt
        /// <summary>
        /// Identifies the generic SQL type <c>BIGINT</c>.
        /// </summary>
        /// <remarks>
        /// The HSQL BIGINT data type denotes an integral number in the 
        /// range 2^63 (-9,223,372,036,854,775,808) to 2^63-1 
        /// (9,223,372,036,854,775,807).
        /// </remarks>
        [Description("Identifies the generic SQL type BIGINT")]
        [EditorBrowsable(EditorBrowsableState.Always)]
        BigInt = Types.BIGINT,
        #endregion
        #region Binary
        /// <summary>
        /// Identifies the generic SQL type <c>BINARY</c>.
        /// </summary>
        [Description("Identifies the generic SQL type BINARY")]
        [EditorBrowsable(EditorBrowsableState.Always)]
        Binary = Types.BINARY,
        #endregion
        #region Blob
        /// <summary>
        /// Identifies the generic SQL type <c>BLOB</c>.
        /// </summary>
        [Browsable(false)]
        [Description("Identifies the generic SQL type BLOB")]
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        Blob = Types.BLOB,
        #endregion
        #region Boolean
        /// <summary>
        /// Identifies the generic SQL type <c>BOOLEAN</c>.
        /// </summary>
        Boolean = Types.BOOLEAN,
        #endregion
        #region Char
        /// <summary>
        /// Identifies the generic SQL type <c>CHAR</c>.
        /// </summary>
        Char = Types.CHAR,
        #endregion
        #region Clob
        /// <summary>
        /// Identifies the generic SQL type <c>CLOB</c>.
        /// </summary>
        [Browsable(false)]
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        Clob = Types.CLOB,
        #endregion
        #region DataLink
        /// <summary>
        /// Identifies the generic SQL type <c>DATALINK</c>.
        /// </summary>
        [Browsable(false)]
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        DataLink = Types.DATALINK,
        #endregion
        #region Date
        /// <summary>
        /// Identifies the generic SQL type <c>DATE</c>.
        /// </summary>
        Date = Types.DATE,
        #endregion
        #region Decimal
        /// <summary>
        /// Identifies the generic SQL type <c>DECIMAL</c>.
        /// </summary>
        Decimal = Types.DECIMAL,
        #endregion
        #region Distinct
        /// <summary>
        /// Identifies the generic SQL type <c>DISTINCT</c>.
        /// </summary>
        [Browsable(false)]
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        Distinct = Types.DISTINCT,
        #endregion
        #region Double
        /// <summary>
        /// Identifies the generic SQL type <c>DOUBLE</c>.
        /// </summary>
        Double = Types.DOUBLE,
        #endregion
        #region Float
        /// <summary>
        /// Identifies the generic SQL type <c>FLOAT</c>.
        /// </summary>
        Float = Types.FLOAT,
        #endregion
        #region Integer
        /// <summary>
        /// Identifies the generic SQL type <c>INTEGER</c>.
        /// </summary>
        Integer = Types.INTEGER,
        #endregion
        #region JavaObject
        /// <summary>
        /// Identifies the generic SQL type <c>JAVA_OBJECT</c>.
        /// </summary>
        [Browsable(false)]
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        JavaObject = Types.JAVA_OBJECT,
        #endregion
        #region LongVarBinary
        /// <summary>
        /// Identifies the generic SQL type <c>LONGVARBINARY</c>.
        /// </summary>
        LongVarBinary = Types.LONGVARBINARY,
        #endregion
        #region LongVarChar
        /// <summary>
        /// Identifies the generic SQL type <c>LONGVARCHAR</c>.
        /// </summary>
        LongVarChar = Types.LONGVARCHAR,
        #endregion
        #region Numeric
        /// <summary>
        /// Identifies the generic SQL type <c>NUMERIC</c>.
        /// </summary>
        Numeric = Types.NUMERIC,
        #endregion
        #region Null
        /// <summary>
        /// Identifies the generic SQL type <c>NULL</c>.
        /// </summary>
        [Browsable(false)]
        [EditorBrowsable(EditorBrowsableState.Never)]
        Null = Types.NULL,
        #endregion
        #region Object
        /// <summary>
        /// Indicates that the SQL type maps to a
        /// serializable <c>System.Type</c>.
        /// </summary>
        Object = Types.OTHER,
        #endregion
        #region Real
        /// <summary>
        /// Identifies the generic SQL type <c>REAL</c>.
        /// </summary>
        Real = Types.REAL,
        #endregion
        #region Ref
        /// <summary>
        /// Identifies the generic SQL type <c>REF</c>.
        /// </summary>
        [Browsable(false)]
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        Ref = Types.REF,
        #endregion
        #region Smallint
        /// <summary>
        /// Identifies the generic SQL type <c>SMALLINT</c>.
        /// </summary>
        SmallInt = Types.SMALLINT,
        #endregion
        #region Struct
        /// <summary>
        /// Identifies the generic SQL type <c>STRUCT</c>.
        /// </summary>
        [Browsable(false)]
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        Struct = Types.STRUCT,
        #endregion
        #region Time
        /// <summary>
        /// Identifies the generic SQL type <c>TIME</c>.
        /// </summary>
        Time = Types.TIME,
        #endregion
        #region TimeStamp
        /// <summary>
        /// Identifies the generic SQL type <c>TIMESTAMP</c>.
        /// </summary>
        TimeStamp = Types.TIMESTAMP,
        #endregion
        #region TinyInt
        /// <summary>
        /// Identifies the generic SQL type <c>TINYINT</c>.
        /// </summary>
        TinyInt = Types.TINYINT,
        #endregion
        #region VarBinary
        /// <summary>
        /// Identifies the generic SQL type <c>VARBINARY</c>.
        /// </summary>
        VarBinary = Types.VARBINARY,
        #endregion
        #region VarChar
        /// <summary>
        /// Identifies the generic SQL type <c>VARCHAR</c>.
        /// </summary>
        VarChar = Types.VARCHAR,
        #endregion
        #region Xml
        /// <summary>
        /// Identifies the generic SQL type <c>XML</c>.
        /// </summary>
        [Browsable(false)]
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        Xml = Types.XML
        #endregion
    }

    #endregion

    #region HsqlProviderTypeComparer
    /// <summary>
    /// 
    /// </summary>
    public sealed class HsqlProviderTypeComparer : IComparer<HsqlProviderType>
    {
        #region Fields
        /// <summary>
        /// 
        /// </summary>
        public static readonly HsqlProviderTypeComparer Instance;
        #endregion

        #region Static Initializer
        /// <summary>
        /// Initializes the <see cref="HsqlProviderTypeComparer"/> class.
        /// </summary>
        static HsqlProviderTypeComparer()
        {
            Instance = new HsqlProviderTypeComparer();
        }
        #endregion

        #region Constructor
        /// <summary>
        /// Constructs a new <c>HsqlProvidierTypeComparer</c> instance.
        /// </summary>
        private HsqlProviderTypeComparer() { }
        #endregion

        #region IComparer<HsqlProviderType> Members

        #region Compare(HsqlProviderType,HsqlProviderType)
        /// <summary>
        /// Compares two objects and returns a value indicating whether 
        /// one is less than, equal to, or greater than the other.
        /// </summary>
        /// <param name="x">The first object to compare.</param>
        /// <param name="y">The second object to compare.</param>
        /// <returns>
        /// Less than zero implies x is less than y. 
        /// Zero implies x equals y.
        /// Greater than zero implies x is greater than y.
        /// </returns>
        public int Compare(HsqlProviderType x, HsqlProviderType y)
        {
            if (!Enum.IsDefined(typeof(HsqlProviderType), x))
            {
                throw new ArgumentException("Invalid Value " + x, "x");
            }
            else if (!Enum.IsDefined(typeof(HsqlProviderType), y))
            {
                throw new ArgumentException("Invalid Value " + y, "y");
            }

            return x.ToString().CompareTo(y.ToString());
        }
        #endregion

        #endregion
    } 
    #endregion
}
