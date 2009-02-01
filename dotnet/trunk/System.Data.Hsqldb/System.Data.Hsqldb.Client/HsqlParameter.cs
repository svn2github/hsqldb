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
using System.Data;
using System.Data.Common;
using System.Threading;
using System.Data.Hsqldb.Common.Enumeration;
using System.ComponentModel;
using System.Data.SqlTypes;
using System.Data.Hsqldb.Client.Design;
using System.Drawing.Design;
using System.ComponentModel.Design;
using System.Data.Hsqldb.Client.Design.Converter;
using System.Data.Hsqldb.Common.Converter;
using System.Data.Hsqldb.Common;

#endregion

namespace System.Data.Hsqldb.Client
{
    /// <summary>
    /// <para>
    /// The HSQLDB <see cref="DbParameter">DbParameter</see> implementation.
    /// </para>
    /// <img src="/ClassDiagrams/System.Data.Hsqldb.Client.HsqlParameter.png"
    ///      alt="HsqlParameter Class Diagram"/>
    /// </summary>
    /// <author name="boucherb@users"/>
    /// <seealso cref="HsqlCommand"/>
    public sealed class HsqlParameter 
        : DbParameter, IDbDataParameter, IDataParameter, ICloneable
    {
        #region Fields

        // Fields
        private string m_name;
        private object m_value;
        private ParameterDirection m_direction = ParameterDirection.Input;

        private bool m_nullable;

        private int m_size;
        private byte m_precision;
        private byte m_scale;
        
        private string m_sourceColumn;
        
        private DataRowVersion m_dataRowVersion = DataRowVersion.Current;
        private DbType m_dbType = DbType.String;
        private HsqlProviderType m_hsqlDbType = HsqlProviderType.VarChar;
        private bool m_sourceColumnNullMapping;
        private int m_offset;

        internal object m_parent;
        private bool m_suppressed;
        private bool m_assigned;
        private bool m_forceSize;
        private bool m_inferDbType = false;
        private bool m_inferProviderType = false;

        #endregion

        #region Public Constructors

        #region HsqlParameter()

        /// <summary>
        /// Default constructor.
        /// </summary>
        public HsqlParameter()
            : base()
        {
        }

        #endregion

        #region HsqlParameter(string,DbType)

        /// <summary>
        /// Constructor setting the parameter data type.
        /// </summary>
        /// <param name="parameterName">Name of the parameter.</param>
        /// <param name="dbType">The <c>DbType</c> of the parameter.</param>
        public HsqlParameter(string parameterName, HsqlProviderType dbType)
            : this()
        {
            this.ParameterName = parameterName;
            this.ProviderType = dbType;
        }

        #endregion

        #region HsqlParameter(string,object)

        /// <summary>
        /// Constructor setting the parameter name/value pair.
        /// </summary>
        /// <remarks>
        /// Uses automatic inference to determine the SQL data type.
        /// </remarks>
        /// <param name="parameterName">
        /// Name of the parameter.
        /// </param>
        /// <param name="value">
        /// The value.
        /// </param>
        public HsqlParameter(
            string parameterName, 
            object value)
            : this()
        {
            ParameterName = parameterName;
            Value = value;
        }

        #endregion

        #region HsqlParameter(string,DbType,int)

        /// <summary>
        /// Constructor setting the data type and size.
        /// </summary>
        /// <param name="parameterName">The name of the parameter to map</param>
        /// <param name="dbType">The <c>HsqlProviderType</c> of the parameter.</param>
        /// <param name="size">The length of the parameter.</param>
        public HsqlParameter(
            string parameterName, 
            HsqlProviderType dbType,
            int size)
            : this()
        {
            ParameterName = parameterName;
            ProviderType = dbType;
            Size = size;
        }

        #endregion

        #region HsqlParameter(string,DbType,int,string)

        /// <summary>
        /// Constructor setting the data type, size and source column.
        /// </summary>
        /// <param name="parameterName">The name of the parameter to map.</param>
        /// <param name="dbType">The DbType of the parameter.</param>
        /// <param name="size">The length of the parameter.</param>
        /// <param name="sourceColumn">The name of the source column.</param>
        public HsqlParameter(
            string parameterName, 
            DbType dbType,                               
            int size, 
            string sourceColumn)
            : this()
        {
            ParameterName = parameterName;
            DbType = dbType;
            Size = size;
            SourceColumn = sourceColumn;
        }

        #endregion

        #region HsqlParameter(string,DbType,int,ParameterDirection,bool,byte,byte,string,DataRowVersion,object)

        /// <summary>
        /// Constructor setting the data type, size, direction,
        /// nullability, precision, scale, source column,
        /// row version and value.
        /// </summary>
        /// <param name="parameterName">
        /// The name of the parameter to map.
        /// </param>
        /// <param name="dbType">
        /// The <c>HsqlProviderType</c> of the parameter.
        /// </param>
        /// <param name="size">
        /// The length of the parameter.
        /// </param>
        /// <param name="direction">
        /// One of the <see cref="ParameterDirection"/> values
        /// </param>
        /// <param name="isNullable">
        /// <c>true</c> if the value of the field can be <c>null</c>; 
        /// otherwise <c>false</c>.
        /// </param>
        /// <param name="precision">
        /// The total number of digits to the left and right of the decimal 
        /// point to which <see cref="Value"/> is resolved.
        /// </param>
        /// <param name="scale">
        /// The total number of decimal places to which <see cref="Value"/>
        /// is resolved. 
        /// </param>
        /// <param name="sourceColumn">
        /// The name of the source column.
        /// </param>
        /// <param name="sourceVersion">
        /// One of the <see cref="DataRowVersion"/> values.
        /// </param>
        /// <param name="value">
        /// An object that is the value of this parameter.
        /// </param>
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        public HsqlParameter(
            string parameterName, 
            HsqlProviderType dbType, 
            int size,                               
            ParameterDirection direction, 
            bool isNullable,                               
            byte precision, 
            byte scale, 
            string sourceColumn,                               
            DataRowVersion sourceVersion, 
            object value)
            : this()
        {
            ParameterName = parameterName;
            ProviderType = dbType;
            Size = size;
            Direction = direction;
            IsNullable = isNullable;
            Precision = precision;
            Scale = scale;
            SourceColumn = sourceColumn;
            SourceVersion = sourceVersion;
            Value = value;
        }

        #endregion

        #endregion

        #region IDbDataParameter Implementation Members

        #region Precision

        /// <summary>
        /// Indicates the precision of numeric parameters.
        /// </summary>
        /// <value>
        /// The maximum number of digits used to represent
        /// the <c>Value</c> property of this parameter.
        /// The default value is 0, which indicates that
        /// the HSQLDB driver sets the precision.
        /// </value>
        [Category("Data")]
        [DefaultValue((byte) 0)]
        public byte Precision
        {
            get { return m_precision; }
            set 
            {
                switch (ProviderType)
                {
                    case HsqlProviderType.Float:
                        {
                            if (value > 53)
                            {
                                throw new ArgumentException(
                                    "Invalid Value : " + value,
                                    "Precision");
                            }
                            break;
                        }
                    case HsqlProviderType.Real:
                        {
                            if (value > 24)
                            {
                                throw new ArgumentException(
                                    "Invalid Value : " + value,
                                    "Precision");
                            }
                            break;
                        }                        
                }
                if (m_precision != value)
                {
                    m_precision = value;
                }
            }
        }

        #endregion

        #region Scale

        /// <summary>
        /// Indicates the scale of numeric parameters.
        /// </summary>
        /// <value>
        /// The number of decimal places to which <see cref="Value"/> is resolved.
        /// The default is 0.
        /// </value>
        [Category("Data")]
        [DefaultValue((byte)0)]
        public byte Scale
        {
            get { return m_scale; }
            set { m_scale = value; }
        }

        #endregion

        #region Size

        /// <summary>
        /// Get or set the parameter size.
        /// </summary>
        [Category("Data")]
        [DefaultValue((int)0)]
        public override int Size
        {
            get { return m_size; }
            set { m_size = value; }
        }

        #endregion

        #endregion

        #region IDataParameter Implementation Members

        #region DbType

        /// <summary>
        /// Get or set the parameter <see cref="DbType"/>.
        /// </summary>        
        [Category("Data")]
        [Browsable(false)]        
        [Description("The generic parameter type.")]
        [DesignerSerializationVisibility(DesignerSerializationVisibility.Hidden)]
        [DefaultValue(DbType.String)]
        [RefreshProperties(RefreshProperties.All)]
        public override DbType DbType
        {
            get
            {
                if (m_inferDbType)
                {
                    m_dbType = HsqlConvert.ToDbType(m_hsqlDbType);
                    m_inferDbType = false;
                }

                return m_dbType;
            }
            set
            {
                if (m_dbType != value)
                {
                    if (Enum.IsDefined(typeof(DbType), value))
                    {
                        m_dbType = value;

                        HsqlProviderType providerType = HsqlConvert.ToHsqlProviderType(value);

                        if (m_hsqlDbType != providerType)
                        {
                            m_hsqlDbType = providerType;
                        }
                    }
                    else
                    {
                        throw new ArgumentOutOfRangeException(
                            "DbType",
                            value,
                            string.Format(
                            "{0} is an invalid DbType enumeration value.",
                            value));
                    }
                }
            }
        }

        #endregion

        #region Direction

        /// <summary>
        /// Get or set the parameter direction.
        /// <seealso cref="ParameterDirection"/>
        /// </summary>
        [Category("Data")]
        [Description("Input, output, or bidirectional parameter.")]
        [RefreshProperties(RefreshProperties.All)]
        public override ParameterDirection Direction
        {
            get { return m_direction; }
            set
            {
                if (m_direction != value)
                {
                    switch (value)
                    {
                        case ParameterDirection.Input:
                            {
                                m_direction = value;
                                break;
                            }
                        case ParameterDirection.InputOutput:
                        case ParameterDirection.Output:
                        case ParameterDirection.ReturnValue:
                            {
                                throw new ArgumentException(
                                    string.Format(
                                    "The ParameterDirection enumeration value,"
                                    + " {0}, is not yet supported.", value),
                                    "ParameterDirection");
                            }
                        default:
                            {
                                throw new ArgumentException(
                                    string.Format(
                                    "The ParameterDirection enumeration value,"
                                    + " {0}, is invalid.", value),
                                    "ParameterDirection");
                            }
                    }

                }
            }
        }

        #endregion

        #region IsNullable

        /// <summary>
        /// Gets or sets a value that indicates whether 
        /// the parameter accepts null values.
        /// </summary>
        /// <returns>
        /// true if null values are accepted; otherwise false. 
        /// The default is false.</returns>
        [Browsable(false)]
        [DesignOnly(true)]
        [EditorBrowsable(EditorBrowsableState.Never)]
        public override bool IsNullable
        {
            get { return m_nullable; }
            set { m_nullable = value; }
        }

        #endregion

        #region ParameterName

        /// <summary>
        /// Get or set the parameter name.
        /// </summary>
        [Category("Data")]
        [Description("Name of the parameter, like '@p1'")]
        public override string ParameterName
        {
            get { return (m_name == null) ? string.Empty : m_name; }
            set 
            {
                if (!string.IsNullOrEmpty(value) && value.Length > 128)
                {
                    throw new ArgumentException(
                        "Invalid Length: " + value.Length, 
                        "ParameterName");
                }
                if (m_name != value)
                {
                    m_name = value;
                }
            }
        }

        #endregion

        #region SourceColumn

        /// <summary>
        /// Get or set the parameter source column name.
        /// </summary>
        [Category("Update")]
        [Description("When used by a DataAdapter.Update, the source column name that is used to find the DataSetColumn name in the ColumnMappings. This is to copy a value between the parameter and a data row")]
        public override string SourceColumn
        {
            get
            {
                return (m_sourceColumn == null)
                    ? string.Empty
                    : m_sourceColumn;
            }
            set { m_sourceColumn = value; }
        }

        #endregion

        #region SourceVersion

        /// <summary>
        /// Gets or sets the <see cref="DataRowVersion"/> to use 
        /// when you load <see cref="Value"/>.
        /// </summary>
        /// <value>
        /// One of the <see cref="DataRowVersion"/> values. 
        /// The default is <c>DataRowVersion.Current</c>.
        /// </value>
        [Category("Update")]
        [DefaultValue(DataRowVersion.Current)]
        [Description("When used by a DataAdapter.Update (UpdateCommand only), the version of the DataRow value that is used to update the data source.")]
        public override DataRowVersion SourceVersion
        {
            get {return m_dataRowVersion; }
            set
            {
                switch (value)
                {
                    case DataRowVersion.Current:
                    case DataRowVersion.Default:
                    case DataRowVersion.Original:
                    case DataRowVersion.Proposed:
                        {
                            break;
                        }
                    default:
                        {
                            throw new ArgumentException(
                                "Invalid Value: " + value, 
                                "SourceVersion");
                        }

                }
                if (m_dataRowVersion != value)
                {
                    m_dataRowVersion = value;
                }
            }
        }

        #endregion

        #region Value

        /// <summary>
        /// Get or set the parameter value.
        /// </summary>        
        [Category("Data")]
        [DefaultValue((string)null)]
        [RefreshProperties(RefreshProperties.All)]
        [Editor(typeof(BinaryEditor),typeof(UITypeEditor))]
        //[TypeConverter(typeof(ArrayConverter))]
        public override object Value
        {
            get { return m_value; }
            set
            {
                m_value = value;
                
                if (!m_assigned)
                {
                    m_assigned = true;

                    if (!m_inferProviderType)
                    {
                        m_inferProviderType = true;
                    }
                }
            }
        }

        #endregion

        #endregion

        #region DbParameter Implementation Members

        #region SourceColumnNullMapping

        /// <summary>
        /// Specifies whether the source column is nullable;
        /// allows <see cref="DbCommandBuilder"/> to correctly generate
        /// Update statements for nullable columns.
        /// </summary>
        /// <value>
        /// <c>true</c> if the source column is nullable;
        /// <c>false</c> if it is not.
        /// </value>
        [Category("Update")]
        [Description("When used by DataAdapter.Update, the parameter value is changed from DBNull.Value into (Int32)1 or (Int32)0 if non-null.")]
        public override bool SourceColumnNullMapping
        {
            get { return m_sourceColumnNullMapping; }
            set { m_sourceColumnNullMapping = value; }
        }

        #endregion

        #region ResetDbType

        /// <summary>
        /// Resets the <see cref="DbType"/> property to its original settings.
        /// </summary>
        public override void ResetDbType()
        {
            m_inferDbType = true;
        }

        #endregion

        #endregion

        #region Public Provider-Specific Properties

        #region ProviderType

        /// <summary>
        /// Gets or sets the HSQLDB provider type.
        /// </summary>
        /// <value>The HSQLDB provider type.</value>        
        [DbProviderSpecificTypeProperty(true)]
        [DefaultValue(HsqlProviderType.VarChar)]
        [Description("The native parameter type.")]
        [DisplayName("HsqlProviderType")]        
        [Category("Data")]
        [RefreshProperties(RefreshProperties.All)]
        [TypeConverter(typeof(LexographicEnumConverter))]
        public HsqlProviderType ProviderType
        {
            get
            {
                return m_hsqlDbType;
            }
            set
            {
                if (m_hsqlDbType != value)
                {
                    if (Enum.IsDefined(typeof(HsqlProviderType), value))
                    {
                        m_hsqlDbType = value;
                        m_dbType = HsqlConvert.ToDbType(value);

                        m_inferProviderType = false;
                        m_inferDbType = false;
                    }
                    else
                    {
                        throw new ArgumentOutOfRangeException(
                            "ProviderType",
                            value,
                            string.Format(
                            "{0} is an invalid HsqlProviderType enumeration value.",
                            value));
                    }
                }
            }
        }

        #endregion

        #endregion

        #region Internal Methods & Properties

        #region Suppress

        /// <summary>
        /// Flag that indicates if this parameter must be excluded.
        /// </summary>
        /// <value>
        /// <c>true</c> if this parameter must be excluded;
        /// otherwise, <c>false</c>.
        /// </value>
        internal bool Suppress
        {
            get { return m_suppressed; }
            set { m_suppressed = value; }
        }

        #endregion

        #region CompareExchangeParent(object, object)

        internal object CompareExchangeParent(object value, object comparand)
        {
            object parent = m_parent;

            if (comparand == parent)
            {
                m_parent = value;
            }

            return parent;
        }

        /// <summary>
        /// Gets or sets the offset to the Value property.
        /// </summary>
        /// <value>The parameter offset.</value>
        /// <remarks>
        /// The Offset property is used for client-side chunking of binary and string data.
        /// For example, in order to insert 10MB of text into a column on a server, a user
        /// might execute 10 parameterized inserts of 1MB chunks, shifting the value of
        /// Offset on each iteration by 1MB.
        /// Offset specifies the number of bytes for binary types, and the number of
        /// characters for strings. The count for strings does not include the terminating
        /// character.
        /// </remarks>
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        [Browsable(false)]
        [Category("Data")]
        [Description("Offset in variable length data types.")]
        public int Offset
        {
            get { return m_offset; }
            set
            {
                if (value < 0)
                {
                    throw new ArgumentException("Invalid Value: " + value, "Offset");
                }

                m_offset = value;
            }
        }

        #endregion

        #region SetProperties

        ///// <summary>
        ///// Sets the parameter properties.
        ///// </summary>
        ///// <param name="parameterName">Name of the parameter.</param>
        ///// <param name="sourceColumn">The source column.</param>
        ///// <param name="sourceVersion">The source version.</param>
        ///// <param name="precision">The precision.</param>
        ///// <param name="scale">The scale.</param>
        ///// <param name="size">The size.</param>
        ///// <param name="forceSize">if set to <c>true</c>, force size.</param>
        ///// <param name="offset">The offset.</param>
        ///// <param name="parameterDirection">The parameter direction.</param>
        ///// <param name="value">The value.</param>
        ///// <param name="dbType">The data type</param>
        ///// <param name="suppress">if set to <c>true</c>, suppress.</param>
        ///// <param name="inferType">if set to <c>true</c>, infer type.</param>
        //internal void SetProperties(
        //    string parameterName, 
        //    string sourceColumn,   
        //    DataRowVersion sourceVersion,
        //    byte precision,
        //    byte scale,
        //    int size,
        //    bool forceSize,
        //    int offset,
        //    ParameterDirection parameterDirection,
        //    object value,
        //    DbType dbType, 
        //    bool suppress,
        //    bool inferType)
        //{
        //    m_name = parameterName;
        //    m_sourceColumn = sourceColumn;
        //    SourceVersion = sourceVersion;
        //    m_precision = precision;
        //    m_scale = scale;
        //    m_size = size;
        //    m_forceSize = forceSize;
        //    m_offset = offset;
        //    m_direction = parameterDirection;

        //    ICloneable cloneable = value as ICloneable;

        //    m_value = (cloneable == null) ? value : cloneable.Clone();
        //    m_dbType = dbType;
        //    m_suppressed = suppress;
        //    m_inferType = inferType;
        //    m_assigned = true;
        //}

        #endregion

        #region ToSqlLiteral()
        /// <summary>
        /// Gets the value as an SQL literal string.
        /// </summary>
        /// 
        /// <returns>
        /// The value as an SQL literal string.
        /// </returns>
        public string ToSqlLiteral()
        {
            return HsqlConvert.ToSqlLiteral(this);
        }
        #endregion

        #region ToString()
        /// <summary>
        /// Retrieves the parameter name.
        /// </summary>
        /// <returns>
        /// The parameter name.
        /// </returns>
        public override string ToString()
        {
            return this.ParameterName;
        } 
        #endregion

        #region IsNull(object)
        /// <summary>
        /// Determines whether the specified value is null.
        /// </summary>
        /// <param name="value">The value.</param>
        /// <returns>
        /// <c>true</c> if the specified value is null; otherwise, <c>false</c>.
        /// </returns>
        internal static bool IsNull(object value)
        {
            return
            (
                   (value == null)
                || (Convert.IsDBNull(value))
                || ((value is INullable) && ((INullable)value).IsNull)
            );
        } 
        #endregion

        #endregion

        #region ICloneable Implementation Members

        #region Clone()
        /// <summary>
        /// Returns a new cloned instance of this parameter.
        /// </summary>
        /// <remarks>
        /// <para>
        /// Special handling is provided when <c>Value</c> is
        /// of type <c>byte[]</c>, <c>char[]</c> or <c>ICloneable</c>.
        /// </para>
        /// <para>
        /// Case 1:
        /// </para>
        /// <para>
        /// When the source <c>Value</c> is of type <c>byte[]</c> or
        /// <c>char[]</c>, then the source <c>Offset</c> and <c>Size</c>
        /// properties are taken into consideration.  Specifically, the
        /// destination <c>Offset</c> is assigned zero (0) and the destination
        /// <c>Value</c> is assigned a copy of the source <c>Value</c> that
        /// starts from the source <c>Offset</c>.  When the source <c>Size</c>
        /// is zero (0), then <c>Value.Length - Offset</c> elements are
        /// copied; otherwise, the lower of <c>Size</c> or <c>Value.Length
        /// - Offset</c> elements are copied.
        /// </para>
        /// <para>
        /// Case 2:
        /// </para>
        /// <para>
        /// When the source <c>Value</c> is <c>ICloneable</c> then the target
        /// <c>Value</c> is assigned a clone of the source <c>Value</c>.
        /// </para>
        /// <para>
        /// Case 3:
        /// </para>
        /// <para>
        /// The target <c>Value</c> is assigned directly from the source
        /// <c>Value</c>.
        /// </para>
        /// </remarks>
        /// <returns>
        /// The cloned <see cref="HsqlParameter"/> instance.
        /// </returns>
        public HsqlParameter Clone()
        {
            HsqlParameter parameter = new HsqlParameter();

            parameter.m_assigned = m_assigned;
            parameter.m_dataRowVersion = m_dataRowVersion;
            parameter.m_dbType = m_dbType;
            parameter.m_direction = m_direction;
            parameter.m_forceSize = m_forceSize;
            parameter.m_hsqlDbType = m_hsqlDbType;
            parameter.m_inferProviderType = m_inferProviderType;
            parameter.m_name = m_name;
            parameter.m_nullable = m_nullable;
            parameter.m_precision = m_precision;
            parameter.m_scale = m_scale;
            parameter.m_size = m_size;
            parameter.m_sourceColumn = m_sourceColumn;
            parameter.m_sourceColumnNullMapping = m_sourceColumnNullMapping;
            parameter.m_suppressed = m_suppressed;

            byte[] bytes;
            char[] chars;

            if (null != (bytes = m_value as byte[]))
            {
                int targetLength = bytes.Length - m_offset;

                if ((m_size != 0) && (m_size < targetLength))
                {
                    targetLength = m_size;
                }

                byte[] targetValue = new byte[Math.Max(targetLength, 0)];

                Buffer.BlockCopy(
                    bytes,
                    m_offset,
                    targetValue,
                    0,
                    targetValue.Length);

                parameter.m_offset = 0;
                parameter.m_value = targetValue;
            }
            else if (null != (chars = m_value as char[]))
            {
                int targetLength = chars.Length - m_offset;

                if ((m_size != 0) && (m_size < targetLength))
                {
                    targetLength = m_size;
                }

                char[] targetValue = new char[Math.Max(targetLength, 0)];

                Buffer.BlockCopy(
                    chars,
                    m_offset,
                    targetValue,
                    0,
                    targetValue.Length * 2);

                parameter.m_offset = 0;
                parameter.m_value = targetValue;
            }
            else
            {
                parameter.m_offset = m_offset;

                ICloneable cloneable = m_value as ICloneable;

                parameter.m_value = (cloneable == null)
                    ? m_value
                    : cloneable.Clone();
            }

            return parameter;
        } 
        #endregion

        #region ICloneable.Clone()
        /// <summary>
        /// Returns a new cloned instance of this parameter.
        /// </summary>
        /// <returns>
        /// The cloned <see cref="HsqlParameter"/> instance.
        /// </returns>
        object ICloneable.Clone()
        {
            return Clone();
        } 
        #endregion

        #endregion
    }
}