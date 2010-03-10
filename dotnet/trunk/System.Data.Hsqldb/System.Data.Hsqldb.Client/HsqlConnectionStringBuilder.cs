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
using System.Collections;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data.Common;
using System.Globalization;
using System.Reflection;

// Aliases
using DefaultValueOf = System.Data.Hsqldb.Client.HsqlConnectionStringBuilder.DefaultValueOf;
using Key = System.Data.Hsqldb.Client.HsqlConnectionStringBuilder.ConnectionStringKey;
using Keyword = System.Data.Hsqldb.Common.Enumeration.ConnectionStringKeyword;
using Utils = System.Data.Hsqldb.Client.HsqlConnectionStringBuilder.Util;
using System.Data.Hsqldb.Client.Internal;
using System.Data.Hsqldb.Common.Attribute;

#endregion

namespace System.Data.Hsqldb.Client
{
    /// <summary>
    /// <para>
    /// The HSQLDB <see cref="DbConnectionStringBuilder">DbConnectionStringBuilder</see> implementation.
    /// </para>
    /// <img src="../Documentation/ClassDiagrams/System.Data.Hsqldb.Client.HsqlConnectionStringBuilder.png"
    ///      alt="HsqlConnectionStringBuilder Class Diagram"/>
    /// </summary>
    /// <remarks>
    /// <para>
    /// Provides a facility to allow developers to programmatically create syntactically 
    /// correct connection strings, and parse and rebuild existing connection strings.
    /// </para> 
    /// <para>
    /// Because this class implements <see cref="ICustomTypeDescriptor"/>, 
    /// it supports design time mechanisms such as utilized by the PropertyGrid component.
    /// </para>
    /// </remarks>
    /// <author name="boucherb@users"/>
    [DefaultProperty("DataSource")]
    [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1710:IdentifiersShouldHaveCorrectSuffix")]
    [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Design", "CA1035:ICollectionImplementationsHaveStronglyTypedMembers")]
    public sealed partial class HsqlConnectionStringBuilder
        : DbConnectionStringBuilder
    {
        #region Fields
        private HsqlConnectionSettings m_settings;
        #endregion

        #region Constructors

        #region HsqlConnectionStringBuilder()
        /// <summary>
        /// Contructs a new <c>HsqlConnectionStringBuilder</c> with
        /// the default properties.
        /// </summary>
        public HsqlConnectionStringBuilder() : base(false)
        {
            m_settings = new HsqlConnectionSettings(this);
        }
        #endregion

        #region HsqlConnectionStringBuilder(string)
        /// <summary>
        /// Contructs a new <c>HsqlConnectionStringBuilder</c> with given properties.
        /// </summary>
        /// <param name="connectionString">
        /// Specifies the initial connection properties.
        /// </param>
        public HsqlConnectionStringBuilder(string connectionString) : this()
        {
            ConnectionString = connectionString;
        }
        #endregion

        #endregion Public Constructors

        #region Internal Methods

        #region SetBaseValue(string,object)

        /// <summary>
        /// Assigns the given value to the given key on the base builder.
        /// </summary>
        /// <param name="key">The key.</param>
        /// <param name="value">The value.</param>
        internal void SetBaseValue(string key, object value)
        {
            base[key] = value;
        }

        #endregion

        #region GetBaseValue(string)

        /// <summary>
        /// Retrieves from the base builder the value corresponding to the given key.
        /// </summary>
        /// <param name="key">The key.</param>
        /// <returns>The base builder's value of the given key</returns>
        internal object GetBaseValue(string key)
        {
            return base[key];
        }

        #endregion

        #endregion

        #region Method Overrides

        #region Clear()
        /// <summary>
        /// Clears the contents of the this object.
        /// </summary>
        /// <seealso cref="DbConnectionStringBuilder.Clear()"/>
        public override void Clear()
        {
            base.Clear();
            m_settings.Reset();
        }
        #endregion

        #region ContainsKey(string)

        /// <summary>
        /// Determines whether this builder contains the specified key.
        /// </summary>
        /// <param name="keyword">The key to test.</param>
        /// <returns>
        /// <c>true</c> if this builder contains the specified key;
        /// otherwise, <c>false</c>.
        /// </returns>
        public override bool ContainsKey(string keyword)
        {
            return Util.ContainsKey(keyword);
        }

        #endregion

        #region EquivalentTo(DbConnectionStringBuilder)

        /// <summary>
        /// Compares the connection information in this
        /// <see cref="DbConnectionStringBuilder"/> object
        /// with the connection information in the supplied
        /// object.
        /// </summary>
        /// <param name="connectionStringBuilder">
        /// The <c>DbConnectionStringBuilder</c>c> to be compared
        /// with this object.
        /// </param>
        /// <return>
        /// <c>true</c> if the connection information in both of
        /// the <see cref="DbConnectionStringBuilder"/> objects
        /// causes an equivalent connection string;
        /// otherwise false.
        /// </return>
        public override bool EquivalentTo(DbConnectionStringBuilder connectionStringBuilder)
        {
            HsqlConnectionStringBuilder builder 
                = connectionStringBuilder as HsqlConnectionStringBuilder;

            return (builder != null)
                && (ConnectionString == builder.ConnectionString);
        }

        #endregion

        #region Remove(string)
        /// <summary>
        /// Removes the specified key.
        /// </summary>
        /// <param name="keyword">The key.</param>
        /// <returns>
        /// <c>true</c> if removed.
        /// </returns>
        public override bool Remove(string keyword)
        {
            return (m_settings.Remove(keyword)) ? true : base.Remove(keyword);
        }
        #endregion

        #region ShouldSerialize(string)
        /// <summary>
        /// Indicates whether a non-default value for the specified
        /// key exists in this builder.
        /// </summary>
        /// <param name="keyword">
        /// The key to locate.</param>
        /// <returns>
        /// <c>true</c> if this builder contains a non-default value for the
        /// specified key; otherwise <c>false</c>.
        /// </returns>
        public override bool ShouldSerialize(string keyword)
        {
            return m_settings.ShouldSerialize(keyword);
        }
        #endregion

        #region TryGetValue(string,object)

        /// <summary>
        /// Retrieves a value corresponding to the supplied
        /// key from this <see cref="DbConnectionStringBuilder"/>.
        /// </summary>
        /// <returns><c>true</c> if keyword was found within this builder,
        /// <c>false</c> otherwise.
        /// </returns>
        /// <param name="keyword">
        /// The key of the item to retrieve.
        /// </param>
        /// <param name="value">
        /// The value corresponding to the key.
        /// </param>
        /// <exception cref="ArgumentNullException">
        /// When keyword is the null value (Nothing in Visual Basic).
        /// </exception>
        public override bool TryGetValue(string keyword, out object value)
        {
            if (keyword == null)
            {
                throw new ArgumentNullException("keyword");
            }

            if (Util.ContainsKey(keyword))
            {
                value = m_settings.GetValue(keyword);

                return true;
            }

            value = null;
            return false;
        }

        #endregion

        #endregion

        #region Property Overrides

        #region IsFixedSize
        /// <summary>
        /// Indicates whether this builder has a fixed size.
        /// </summary>
        /// <returns>
        /// <c>true</c> in every case, because this builder supplies
        /// a fixed-size collection of key/value pairs.
        /// </returns>
        [Browsable(false)]
        public override bool IsFixedSize
        {
            get { return true; }
        } 
        #endregion

        #region Keys

        /// <summary>
        /// Gets an <see cref="ICollection{T}"/> that contains the keys in this builder.
        /// </summary>
        /// <value>
        /// The collection of keys contained by this builder.
        /// </value>
        [Browsable(false)]
        public override ICollection Keys
        {
            get { return Util.Keys; }
        }

        #endregion

        #region this[string]
        /// <summary>
        /// Gets or sets the value associated with the specified keyword.
        /// </summary>
        /// <remarks>
        /// All supported keyword entries exist in the dictionary in the
        /// "virtual" sense, in that it is always possible to retrieve the
        /// corresponding value; the value of an unrecognized keyword is
        /// always the <c>null</c> reference.
        /// </remarks>
        /// <param name="keyword">
        /// A key, as a character sequence, of the item to get or set.
        /// </param>
        /// <value>
        /// <para>
        /// The value associated with the specified key.
        /// </para>
        /// Because this class uses fixed size collection semantics, when 
        /// a specified key is not found, the meaning is that there is no
        /// such connection property; in this case a <c>null</c> reference
        /// is returned (as opposed to throwing an exception).
        /// Passing a <c>null</c> key throws an <c>ArgumentNullException</c>.
        /// Assigning <c>null</c> resets the mapped value to its default
        /// state (which is never actually <c>null</c>).
        /// </value>
        /// <returns>
        /// The value associated with the specified keyword.
        /// </returns>
        public override object this[string keyword]
        {
            get
            {
                return Convert.ToString(m_settings.GetValue(keyword));
            }
            set
            {
                m_settings.SetValue(keyword, value);
            }
        }
        #endregion

        #region Values

        /// <summary>
        /// Gets an <see cref="ICollection"/> that contains the values in this builder.
        /// </summary>
        /// <value>
        /// The collection of values contained by this builder.
        /// </value>
        [Browsable(false)]
        public override ICollection Values
        {
            get
            {

                List<object> list = new List<object>();

                foreach (string key in Keys)
                {
                    list.Add(this[key]);
                }

                return list;
            }
        }

        #endregion

        #endregion

        #region Public Properties

        #region Read-Write Properties

        #region AutoShutdown
        /// <summary>
        /// Controls whether an embedded database instance is shut down automatically.
        /// </summary>
        /// <remarks>
        /// <para>
        /// When <c>true</c>, the database instance will shut down automatically
        /// when the number of open connections transitions from one (1)
        /// to zero (0); otherwise, the database instance remains open until
        /// an explicit <c>SHUTDOWN [IMMEDIATELY|COMPACT|SCRIPT]</c>
        /// command is issued or the hosting process terminates.
        /// </para>
        /// <para>
        /// This property must be specified by the connection that
        /// opens the database; it is ignored in subsequent connections.
        /// </para>
        /// <para>
        /// Applies only to embedded mode (i.e. file:, res: and mem:
        /// protocol) connections.
        /// </para>
        /// </remarks>
        [AmbientValue(null)]
        [Category("Embedded Database")]
        [DefaultValue(DefaultValueOf.AutoShutdown)]
        [ResDescription("DbConnectionString_AutoShutdown")]
        [DisplayName(Key.AutoShutdown)]
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        [RefreshProperties(RefreshProperties.All)]        
        public bool AutoShutdown
        {
            get { return m_settings.m_autoShutdown; }
            set { m_settings.SetAutoShutdown(value); }
        }

        #endregion

        #region CacheFileScale
        /// <summary>
        /// Specifies the table data image byte alignment used on disk.
        /// </summary>
        /// <remarks>
        /// <para>
        /// Allowable values are 1 (default) or 8.
        /// </para>
        /// <para>
        /// Setting this value to 8 specifies that record offsets are measured
        /// in 8 byte rather than the default 1 byte quanta, allowing the table
        /// data image on disk to grow to a larger maximum size (~8 GB) than the
        /// default (~2 GB).
        /// </para>
        /// <para>
        /// This value must be set on a new database instance before any
        /// disk-based (CACHED) tables are created, or the affected database
        /// instance must be shut down using the "SHUTDOWN SCRIPT" command
        /// and restarted before the setting will take effect.
        /// </para>
        /// <para>
        /// This property must be specified by the connection that
        /// opens the database; it is ignored in subsequent connections.
        /// </para>
        /// <para>
        /// Applies only to file: protocol connections.
        /// </para>
        /// </remarks>
        [AmbientValue(null)]
        [Category("File Protocol")]
        [DefaultValue(DefaultValueOf.CacheFileScale)]
        [ResDescription("DbConnectionString_CacheFileScale")]
        [DisplayName(Key.CacheFileScale)]
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        [RefreshProperties(RefreshProperties.All)]
        public byte CacheFileScale
        {
            get { return m_settings.m_cacheFileScale; }
            set { m_settings.SetCacheFileScale(value); }
        }
        #endregion

        #region CacheScale
        /// <summary>
        /// Controls the maximum number of rows from disk that
        /// the buffer manager will allow to be cached in memory.
        /// </summary>
        /// <remarks>
        /// <para>
        /// The maximum number is computed as ~ 3*(cache scale**2).
        /// </para>
        /// <para>
        /// The default value is 14 (~ 49152 rows).
        /// </para>
        /// <para>
        /// The recognized range is 8-18 min/max.
        /// </para>
        /// <para>
        /// Supplied values are automatically limited to min/max.
        /// </para>
        /// <para>
        /// This property must be specified by the connection that
        /// opens the database; it is ignored in subsequent connections.
        /// </para>
        /// <para>
        /// Applies only to file: protocol connections.
        /// </para>
        /// </remarks>
        [AmbientValue(null)]
        [Category("File Protocol")]
        [DefaultValue(DefaultValueOf.CacheScale)]
        [ResDescription("DbConnectionString_CacheScale")]
        [DisplayName(Key.CacheScale)]
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        [RefreshProperties(RefreshProperties.All)]
        public byte CacheScale
        {
            get { return m_settings.m_cacheScale; }
            set { m_settings.SetCacheScale(value); }
        }

        #endregion

        #region CacheSizeScale
        /// <summary>
        /// Controls the maximum memory consumed by rows buffered in memory from disk.
        /// </summary>
        /// <remarks>
        /// <para>
        /// The value is treated as the base 2 estimate of average row size in memory.
        /// </para>
        /// <para>
        /// The maximum memory is computed as ~ (3*( cache scale**2))*(cache size scale**2).
        /// </para>
        /// <para>
        /// The default value is 8 (~ 256 bytes).
        /// </para>
        /// <para>
        /// Recognized range is 6-20 (supplied values are automatically limited to min/max).
        /// </para>
        /// <para>
        /// This property must be specified by the connection that
        /// opens the database; it is ignored in subsequent connections.
        /// </para>
        /// <para>
        /// Applies only to file: protocol connections.
        /// </para>
        /// </remarks>
        [AmbientValue(null)]
        [Category("File Protocol")]
        [DefaultValue(DefaultValueOf.CacheSizeScale)]
        [Description("(6-20) Controls the maximum memory consumed by rows buffered in memory from disk, computed as ((3*( Cache Scale**2))*(Cache Size Scale**2)")]
        [DisplayName(Key.CacheSizeScale)]
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        [RefreshProperties(RefreshProperties.All)]
        public byte CacheSizeScale
        {
            get { return m_settings.m_cacheSizeScale; }
            set { m_settings.SetCacheSizeScale(value); }
        }

        #endregion

        #region DatabaseAppLogLevel
        /// <summary>
        /// Filters which database events are written to the Hsqldb App Log.
        /// </summary>
        /// <para>
        /// This property must be specified by the connection that
        /// opens the database; it is ignored in subsequent connections.
        /// </para>
        /// <para>
        /// Applies only to file: protocol connections.
        /// </para>
        /// <seealso cref="T:System.Data.Hsqldb.Enumeration.DatabaseAppLogLevel" />
        [AmbientValue(null)]
        [Category("File Protocol")]
        [DefaultValue(DefaultValueOf.DatabaseAppLogLevel)]
        [Description("Filters which database events are written to the Hsqldb App Log.")]
        [DisplayName(Key.DatabaseAppLogLevel)]
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        [RefreshProperties(RefreshProperties.All)]
        public System.Data.Hsqldb.Common.Enumeration.DatabaseAppLogLevel DatabaseAppLogLevel
        {
            get { return m_settings.m_databaseAppLogLevel; }
            set { m_settings.SetDatabaseAppLogLevel(value); }
        }

        #endregion

        #region DatabaseScriptFormat

        /// <summary>
        /// Controls how the database initialization script is represented on disk.
        /// </summary>
        /// <value>
        /// One of Text, Binary, or CompressedBinary.
        /// </value>
        /// <remarks>
        /// Interpretation:
        /// <list>
        /// <item>
        /// Text: script is written in plain UTF-8 encoded text.
        /// </item>
        /// <item>
        /// Binary: data values are written in binary form and SQL key
        /// words are written in plain UTF-8 encoded text.
        /// </item>
        /// <item>
        /// CompressedBinary: As with binary, but gzip compression is applied to the result.
        /// </item>
        /// </list>
        /// <para>
        /// This value is significant only for the connection that first opens an embedded
        /// database instance; it is ignored otherwise.
        /// </para>
        /// <para>
        /// However, the corresponding database property may be set at any time
        /// on any database instance by issuing the command:
        /// <c>SET SCRIPTFORMAT [TEXT|BINARY|COMPRESSEDBINARY];</c>.
        /// Note that only administrative users are authorized to issue this command.
        /// Also note that the command does not change the script format on disk
        /// immediately.  Instead, the format is changed either at the next CHECKPOINT
        /// or when the database is shut down (which implies a CHECKPOINT), which
        /// ever comes first.
        /// </para>
        /// </remarks>
        [AmbientValue(null)]
        [Category("File Protocol")]
        [DefaultValue(System.Data.Hsqldb.Common.Enumeration.DatabaseScriptFormat.Text)]
        [Description("Controls how the database initialization script is represented on disk.")]
        [DisplayName(Key.DatabaseScriptFormat)]
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        [RefreshProperties(RefreshProperties.All)]
        public System.Data.Hsqldb.Common.Enumeration.DatabaseScriptFormat DatabaseScriptFormat
        {
            get { return m_settings.m_databaseScriptFormat; }
            set { m_settings.SetDatabaseScriptFormat(value); }
        }

        #endregion

        #region DataSource
        /// <summary>
        /// Gets or sets the data source.
        /// </summary>
        /// <remarks>
        /// In this context, a data source character sequence is
        /// a partial database URI, composed of protocol, host,
        /// port and path components.  When retrieved, it is
        /// synthesized from its component properties.  Similarly,
        /// when it is assigned, its component properties are analyzed
        /// and assigned correspondingly.
        /// </remarks>
        /// <value>The data source.</value>
        [Category("Connection")]
        [Description("Value representing the ADO.NET database connection URI")]
        [DisplayName("Data Source")]
        [RefreshProperties(RefreshProperties.All)]
        [TypeConverter("System.Data.Hsqldb.Client.Design.Converter.DataSourceConverter")]
        public string DataSource
        {
            get { return m_settings.GetDataSource(); }
            set { m_settings.SetDataSource(value); }
        }
        #endregion

        #region DefaultSchema

        /// <summary>
        /// Specifies whether otherwise unqualified database object names
        /// are automatically qualified with the default schema (PUBLIC).
        /// </summary>
        /// <remarks>
        /// Note:  This is a backward-compatibility feature for software that
        /// was written against versions of HSQLDB that did not support schema
        /// qualification.
        /// </remarks>
        [AmbientValue(null)]
        [Category("Connection")]
        [DefaultValue(DefaultValueOf.DefaultSchemaQualification)]
        [Description("(Backward Compatibility) Whether the default schema (PUBLIC) is used to qualify otherwise unqualified database object names.")]
        [DisplayName(Key.DefaultSchemaQualification)]
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        [RefreshProperties(RefreshProperties.All)]
        //[Obsolete()]
        public bool DefaultSchemaQualification
        {
            get { return m_settings.m_defaultSchemaQualification; }
            set { m_settings.SetDefaultSchemaQualification(value); }
        }

        #endregion

        #region DefaultTableType

        /// <summary>
        /// Specifies the table type used when issuing
        /// unqualifed table creation DDL.
        /// </summary>
        /// <remarks>
        /// <para>
        /// This does not apply to Text table creation which
        /// always requires an explicit table type qualifier.
        /// </para>
        /// <para>
        /// Default value is <see cref="System.Data.Hsqldb.Common.Enumeration.DefaultTableType.Memory"/>.
        /// </para>
        /// <para>
        /// This property must be specified by the connection that
        /// opens the database; it is ignored in subsequent connections.
        /// </para>
        /// <para>
        /// Applies only to file: protocol connections.
        /// </para>
        /// </remarks>
        /// <seealso cref="T:System.Data.Hsqldb.Enumeration.DefaultTableType"/>       
        [AmbientValue(null)]
        [Category("File Protocol")]
        [DefaultValue(DefaultValueOf.DefaultTableType)]
        [Description("Specifies the default table type used when issuing unqualifed table creation DDL.")]
        [DisplayName(Key.DefaultTableType)]
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        [RefreshProperties(RefreshProperties.All)]
        public System.Data.Hsqldb.Common.Enumeration.DefaultTableType DefaultTableType
        {
            get { return m_settings.m_defaultTableType; }
            set { m_settings.SetDefaultTableType(value); }
        }

        #endregion

        #region DefragLimit

        /// <summary>
        /// The amount (in megabytes) of data file fragementation that
        /// triggers an automatic defragmentation.
        /// </summary>
        /// <remarks>
        /// <para>
        /// The default value is 200; recognized range is 1..1024 (MB).
        /// </para>
        /// <para>
        /// This property must be specified by the connection that
        /// opens the database; it is ignored in subsequent connections.
        /// </para>
        /// <para>
        /// Applies only to file: protocol connections.
        /// </para>
        /// </remarks>        
        [AmbientValue(null)]
        [Category("File Protocol")]
        [DefaultValue(DefaultValueOf.DefragLimit)]
        [Description("The amount (in megabytes) of data file fragementation that triggers an automatic defragmentation.")]
        [DisplayName(Key.DefragLimit)]
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        [RefreshProperties(RefreshProperties.All)]
        [CLSCompliant(false)]
        public ushort DefragLimit
        {
            get { return m_settings.m_defragLimit; }
            set { m_settings.SetDefragLimit(value); }

        }

        #endregion

        #region EnforceColumnSize

        /// <summary>
        /// Controls whether ansi/iso padding, length, precision and
        /// scale declarations are honoured for new data.
        /// </summary>
        /// <remarks>
        /// <para>
        /// Changing this value from <c>false</c> to <c>true</c> does
        /// not revalidate existing data.
        /// </para>
        /// <para>
        /// This property must be specified by the connection that
        /// opens the database; it is ignored in subsequent connections.
        /// </para>
        /// <para>
        /// Applies only to file:, mem: and res: protocol connections.
        /// </para>
        /// </remarks>
        [AmbientValue(null)]
        [Category("Embedded Database")]
        [DefaultValue(DefaultValueOf.EnforceColumnSize)]
        [Description("Controls whether ansi/iso padding, length, precision and scale declarations are honoured for new data.")]
        [DisplayName(Key.EnforceColumnSize)]
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        [RefreshProperties(RefreshProperties.All)]
        public bool EnforceColumnSize
        {
            get { return m_settings.m_enforceColumnSize; }
            set { m_settings.SetEnforceColumnSize(value); }
        }

        #endregion

        #region Enlist
#if SYSTRAN
        /// <summary>
        /// Controls whether a connection is automatically enlisted in
        /// the current System.Transactions.Transaction when opened.
        /// </summary>
        [AmbientValue(null)]
        [Category("Connection")]
        [DefaultValue(DefaultValueOf.Enlist)]
        [Description("Controls whether a connection is automatically enlisted in the current System.Transactions.Transaction when opened.")]
        [DisplayName(Key.Enlist)]
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        public bool Enlist
        {
            get { return m_settings.m_enlist; }
            set { m_settings.SetEnlist(value); }
        }
#endif
        #endregion

        #region Host

        /// <summary>
        /// The target host for network protocol connections.
        /// </summary>
        [AmbientValue(null)]
        [Category("Connection")]
        [DefaultValue(DefaultValueOf.Host)]
        [Description("The target host for network protocol connections.")]
        [DisplayName(Key.Host)]
        [RefreshProperties(RefreshProperties.All)]
        public string Host
        {
            get { return m_settings.m_host; }
            set { m_settings.SetHost(value); }
        }

        #endregion

        #region IfExists

        /// <summary>
        /// Regulates automatic creation of an embedded database.
        /// </summary>
        /// <remarks>
        /// When true, file: and mem: protocol connections succeed only if the
        /// given database instance already exists.  When false, a new instance
        /// is created if one does not already exist.
        /// </remarks>
        [AmbientValue(null)]
        [Category("Embedded Database")]
        [DefaultValue(DefaultValueOf.IfExists)]
        [Description("When true, file: and mem: protocol connections succeed only if the given database instance already exists.  When false, a new instance is created if one does not already exist.")]
        [DisplayName(Key.IfExists)]
        [RefreshProperties(RefreshProperties.All)]
        public bool IfExists
        {
            get { return m_settings.m_ifExists; }
            set { m_settings.SetIfExists(value); }
        }

        #endregion

        #region InitialSchema
        /// <summary>
        /// Gets or sets the initial schema.
        /// </summary>
        /// <value>The initial schema.</value>
        [AmbientValue(null)]
        [Category("Connection")]
        [DefaultValue(DefaultValueOf.InitialSchema)]
        [Description("The Schema the connection should use, initially")]
        [DisplayName(Key.InitialSchema)]
        [TypeConverter("System.Data.Hsqldb.Client.Design.Converter.InitialSchemaConverter")]
        [RefreshProperties(RefreshProperties.All)]
        public string InitialSchema
        {
            get { return m_settings.m_initialSchema; }
            set { m_settings.SetInitialSchema(value); }
        }
        #endregion

        #region MemoryMappedDataFile

        /// <summary>
        /// Controls the use of memory-mapped data file access.
        /// </summary>
        /// <remarks>
        /// <para>
        /// When true (the default), the row buffer manager uses memory-mapped
        /// data file access.
        /// </para>
        /// <para>
        /// This property must be specified by the connection that
        /// opens the database; it is ignored in subsequent connections.
        /// </para>
        /// <para>
        /// Applies only to file: protocol connections.
        /// </para>
        /// </remarks>
        [AmbientValue(null)]
        [Category("File Protocol")]
        [DefaultValue(DefaultValueOf.MemoryMappedDataFile)]
        [Description("When true, the row buffer manager uses memory-mapped data file access.")]
        [DisplayName(Key.MemoryMappedDataFile)]
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        [RefreshProperties(RefreshProperties.All)]
        public bool MemoryMappedDataFile
        {
            get { return m_settings.m_memoryMappedDataFile; }
            set { m_settings.SetMemoryMappedDataFile(value); }
        }

        #endregion

        #region Password

        /// <summary>
        /// The password used to authenticate the connection request.
        /// </summary>
        [AmbientValue(null)]
        [Category("Connection")]
        [DefaultValue(DefaultValueOf.Password)]
        [Description("Used to authenticate the connection request.")]
        [PasswordPropertyText(true)]
        [RefreshProperties(RefreshProperties.All)]
        public string Password
        {
            get { return m_settings.m_password; }
            set { m_settings.SetPassword(value); }
        }

        #endregion

        #region Path

        /// <summary>
        /// The database instance path, i.e. the catalog identifier relative
        /// to the selected protcol.
        /// </summary>
        /// <remarks>
        /// <list>
        /// <item>
        /// For file: protocol connections, this is the relative or absolute path
        /// on disk to the database files.
        /// </item>
        /// <item>
        /// For res: protocol connections, this is the resource path inside a zip
        /// archive on the CLASSPATH environment variable.
        /// </item>
        /// <item>
        /// For mem: connections, this is logical name with which the database
        /// instance was (or will be) created.
        /// </item>
        /// </list>
        /// <item>
        /// For network protocol connections, this is the alias with which the target
        /// database instance has been registered with the server instance running on
        /// the target host at the given port.
        /// </item>
        /// </remarks>
        [AmbientValue(null)]
        [Category("Connection")]
        [DefaultValue(DefaultValueOf.Path)]
        [Description("The path of the database instance, i.e. the catalog identifier relative to the selected protcol.")]
        [DisplayName(Key.Path)]
        [RefreshProperties(RefreshProperties.All)]
        public string Path
        {
            get { return m_settings.m_path; }
            set { m_settings.SetPath(value); }
        }

        #endregion

        #region Port

        /// <summary>
        /// The port at which the target server instance is listenting for connections.
        /// </summary>
        /// <remarks>
        /// Applies only to network protocol (hsql:, hsqls:, http: and https:) connections.
        /// </remarks>
        [AmbientValue(null)]
        [Category("Connection")]
        [Description("The port at which the target server instance is listenting for connections.")]
        [DisplayName(Key.Port)]
        [RefreshProperties(RefreshProperties.All)]
        [CLSCompliant(false)]
        public ushort Port
        {
            get { return (ushort) m_settings.GetValue(Keyword.Port); }
            set { m_settings.SetPort(value); }
        }

        #endregion

        #region Protocol

        /// <summary>
        /// The connection protocol to use.
        /// </summary>
        /// <seealso cref="T:System.Data.Hsqldb.Enumeration.ConnectionProtocol"/>
        [AmbientValue(null)]
        [Category("Connection")]
        [DefaultValue(DefaultValueOf.Protocol)]
        [Description("The connection protocol - one of File, Res, Mem, Hsql, Hsqls, Http, or Https.")]
        [DisplayName(Key.Protocol)]
        [RefreshProperties(RefreshProperties.All)]
        public System.Data.Hsqldb.Common.Enumeration.ConnectionProtocol Protocol
        {
            get { return m_settings.m_protocol; }
            set { m_settings.SetProtocol(value); }
        }

        #endregion

        #region ReadOnlySession
        /// <summary>
        /// Specifies whether write operations are prohibited.
        /// </summary>
        [AmbientValue(null)]
        [Category("Connection")]
        [DefaultValue(DefaultValueOf.ReadOnlySession)]
        [Description("When true, write operations are prohibited.")]
        [DisplayName(Key.ReadOnlySession)]
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        public bool ReadOnlySession
        {
            get { return m_settings.m_readOnlySession; }
            set { m_settings.SetReadOnlySession(value); }
        }
        #endregion

        #region ReportBaseColumnName

        /// <summary>
        /// Controls BaseColumnName reporting in DataTable and DataReader metadata.
        /// </summary>
        /// <remarks>
        /// When true (the default) the base column name is reported.
        /// When false, the column label (which is possibly an alias) is
        /// reported.
        /// </remarks>
        [AmbientValue(null)]
        [Category("Connection")]
        [DefaultValue(DefaultValueOf.ReportBaseColumnName)]
        [Description("When false, BaseColumnName reports the column label (alias); when true, the standard behavior is observed.")]
        [DisplayName(Key.ReportBaseColumnName)]
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        [RefreshProperties(RefreshProperties.All)]
        public bool ReportBaseColumnName
        {
            get { return m_settings.m_reportBaseColumnName; }
            set { m_settings.SetReportBaseColumnName(value); }
        }

        #endregion

        #region ReportCatalogs

        /// <summary>
        /// Controls whether BaseCatalogName is reported in metadata queries.
        /// </summary>
        /// <remarks>
        /// When true, the Hsqldb URI of the database instance is reported as the catalog;
        /// else (the default behaviour) <c>null</c> is reported.
        /// </remarks>
        [AmbientValue(null)]
        [Category("Embedded Database")]
        [DefaultValue(DefaultValueOf.ReportCatalogs)]
        [Description("When true, the URI of the database instance is reported as the catalog in metadata queries, else null.")]
        [DisplayName(Key.ReportCatalogs)]
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        [RefreshProperties(RefreshProperties.All)]
        public bool ReportCatalogs
        {
            get { return m_settings.m_reportCatalogs; }
            set { m_settings.SetReportCatalogs(value); }
        }

        #endregion

        #region TextDbAllowFullPath

        /// <summary>
        /// Specifies whether full (absolute) path specifications are allowed
        /// when declaring TEXT table source descriptors.
        /// </summary>
        /// <remarks>
        /// <para>
        /// When false, all path specifications are interpreted relative to the
        /// database path.
        /// </para>
        /// <para>
        /// This property must be specified by the connection that
        /// opens the database; it is ignored in subsequent connections.
        /// </para>
        /// <para>
        /// Applies only to file: protocol connections.
        /// </para>
        /// </remarks>
        [AmbientValue(null)]
        [Category("File Protocol - Text DB")]
        [DefaultValue(DefaultValueOf.TextDbAllowFullPath)]
        [Description("When true, TEXT table source declarations may use absolute paths; otherwise, file paths must be relative to the database path.")]
        [DisplayName(Key.TextDbAllowFullPath)]
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        [RefreshProperties(RefreshProperties.All)]
        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1706:ShortAcronymsShouldBeUppercase", MessageId = "Member")]
        
        public bool TextDbAllowFullPath
        {
            get { return m_settings.m_textDBAllowFullPath; }
            set { m_settings.SetTextDbAllowFullPath(value); }
        }

        #endregion

        #region TextDbAllQuoted

        /// <summary>
        /// Specifies whether all TEXT table fields are, by default, delimited with double quotes.
        /// </summary>
        /// <remarks>
        /// <para>
        /// When true, the defaut TEXT table format on disk is to delimit all
        /// fields with double quotes.
        /// </para>
        /// <para>
        /// This property must be specified by the connection that
        /// opens the database; it is ignored in subsequent connections.
        /// </para>
        /// <para>
        /// Applies only to file: protocol connections.
        /// </para>
        /// </remarks>
        [AmbientValue(null)]
        [Category("File Protocol - Text DB")]
        [DefaultValue(DefaultValueOf.TextDbAllQuoted)]
        [Description("When true, the defaut TEXT table format on disk is to delimit all fields with double quotes.")]
        [DisplayName(Key.TextDbAllQuoted)]
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        [RefreshProperties(RefreshProperties.All)]
        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1706:ShortAcronymsShouldBeUppercase", MessageId = "Member")]
        
        public bool TextDbAllQuoted
        {
            get { return m_settings.m_textDbAllQuoted; }
            set { m_settings.SetTextDbAllQuoted(value); }
        }

        #endregion

        #region TextDbCacheScale

        /// <summary>
        /// Specifies the cache scale used by the TEXT table buffer manager.
        /// </summary>
        /// <remarks>
        /// <para>
        /// As per Cache Scale, but applies to the TEXT table buffer manager
        /// rather than the CACHED table buffer manager.
        /// </para>
        /// <para>
        /// Default is 10; recognized range is 8-16 min/max.
        /// </para>
        /// <para>
        /// Supplied values are automatically limited to min/max.
        /// </para>
        /// <para>
        /// This property must be specified by the connection that
        /// opens the database; it is ignored in subsequent connections.
        /// </para>
        /// <para>
        /// Applies only to file: protocol connections.
        /// </para>
        /// </remarks>
        /// <seealso cref="CacheScale"/>
        [AmbientValue(null)]
        [Category("File Protocol - Text DB")]
        [DefaultValue(DefaultValueOf.TextDbCacheScale)]
        [Description("Like Cache Scale, but applies to the TEXT table buffer manager rather than the CACHED table buffer manager.")]
        [DisplayName(Key.TextDbCacheScale)]
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        [RefreshProperties(RefreshProperties.All)]
        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1706:ShortAcronymsShouldBeUppercase", MessageId = "Member")]
        
        public byte TextDbCacheScale
        {
            get { return m_settings.m_textDbCacheScale; }
            set { m_settings.SetTextDbCacheScale(value); }
        }

        #endregion

        #region TextDbCacheSizeScale

        /// <summary>
        /// Specifies the cache size scale used by the TEXT table buffer manager.
        /// </summary>
        /// <remarks>
        /// <para>
        /// Like Cache Size Scale, but applies to the TEXT table buffer manager
        /// rather than the CACHED table buffer manager.
        /// </para>
        /// <para>
        /// Default is 10; recognized range is 8-20 min/max.
        /// </para>
        /// <para>
        /// Supplied values are automatically limited to min/max.
        /// </para>
        /// <para>
        /// This property must be specified by the connection that
        /// opens the database; it is ignored in subsequent connections.
        /// </para>
        /// <para>
        /// Applies only to file: protocol connections.
        /// </para>
        /// </remarks>
        /// <seealso cref="CacheSizeScale"/>
        [AmbientValue(null)]
        [Category("File Protocol - Text DB")]
        [DefaultValue(DefaultValueOf.TextDbCacheSizeScale)]
        [Description("Like Cache Size Scale, but applies to the TEXT table buffer manager rather than the CACHED table buffer manager.")]
        [DisplayName(Key.TextDbCacheSizeScale)]
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        [RefreshProperties(RefreshProperties.All)]
        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1706:ShortAcronymsShouldBeUppercase", MessageId = "Member")]        
        public byte TextDbCacheSizeScale
        {
            get { return m_settings.m_textDbCacheSizeScale; }
            set { m_settings.SetTextDbCacheSizeScale(value); }
        }

        #endregion

        #region TextDbEncoding

        /// <summary>
        /// Specifies the default TEXT table file encoding.
        /// </summary>
        /// <remarks>
        /// <para>
        /// The default value is "US-ASCII".
        /// </para>
        /// <para>
        /// Legal values are the standards-based encoding identifiers,
        /// such as "US-ASCII", "UTF-8", "UTF-16", etc.
        /// </para>
        /// <para>
        /// This property must be specified by the connection that
        /// opens the database; it is ignored in subsequent connections.
        /// </para>
        /// <para>
        /// Applies only to file: protocol connections.
        /// </para>
        /// </remarks>
        /// <seealso cref="CacheSizeScale"/>        
        [AmbientValue(null)]
        [Category("File Protocol - Text DB")]
        [DefaultValue(DefaultValueOf.TextDbEncoding)]
        [Description("The default TEXT table file encoding.")]
        [DisplayName(Key.TextDbEncoding)]
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        [RefreshProperties(RefreshProperties.All)]
        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1706:ShortAcronymsShouldBeUppercase", MessageId = "Member")]
        public string TextDbEncoding
        {
            get { return m_settings.m_textDbEncoding; }
            set { m_settings.SetTextDbEncoding(value); }
        }

        #endregion

        #region TextDbFieldSeparator

        /// <summary>
        /// Specifies the default TEXT table field separator.
        /// </summary>
        /// <remarks>
        /// <para>
        /// The default value is ",".
        /// </para>
        /// <para>
        /// This property must be specified by the connection that
        /// opens the database; it is ignored in subsequent connections.
        /// </para>
        /// <para>
        /// Applies only to file: protocol connections.
        /// </para>
        /// </remarks>
        [AmbientValue(null)]
        [Category("File Protocol - Text DB")]
        [DefaultValue(DefaultValueOf.TextDbFieldSeparator)]
        [Description("The default TEXT table field separator.")]
        [DisplayName(Key.TextDbFieldSeparator)]
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        [RefreshProperties(RefreshProperties.All)]
        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1706:ShortAcronymsShouldBeUppercase", MessageId = "Member")]
        
        public string TextDbFieldSeparator
        {
            get { return m_settings.m_textDbFieldSeparator; }
            set { m_settings.SetTextDbFieldSeparator(value); }
        }

        #endregion

        #region TextDbIgnoreFirst

        /// <summary>
        /// Specifies whether the first line of a TEXT table's source file is ignored.
        /// </summary>
        /// <remarks>
        /// <para>
        /// The default value is <c>false</c>.
        /// </para>
        /// <para>
        /// When <c>true</c>, the default TEXT table behaviour is to ignore
        /// the first line of the source file.
        /// </para>
        /// <para>
        /// This property must be specified by the connection that
        /// opens the database; it is ignored in subsequent connections.
        /// </para>
        /// <para>
        /// Applies only to file: protocol connections.
        /// </para>
        /// </remarks>
        [AmbientValue(null)]
        [Category("File Protocol - Text DB")]
        [DefaultValue(DefaultValueOf.TextDbIgnoreFirst)]
        [Description("When true, the default TEXT table behaviour is to ignore the first line of the table's source file.")]
        [DisplayName(Key.TextDbIgnoreFirst)]
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        [RefreshProperties(RefreshProperties.All)]
        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1706:ShortAcronymsShouldBeUppercase", MessageId = "Member")]
        public bool TextDbIgnoreFirst
        {
            get { return m_settings.m_textDbIgnoreFirst; }
            set { m_settings.SetTextDbIgnoreFirst(value); }
        }

        #endregion

        #region TextDbLongVarcharFieldSeparator

        /// <summary>
        /// Specifies the default TEXT table LONGVARCHAR field separator.
        /// </summary>
        /// <remarks>
        /// <para>
        /// The default value is ",".
        /// </para>
        /// <para>
        /// This property must be specified by the connection that
        /// opens the database; it is ignored in subsequent connections.
        /// </para>
        /// <para>
        /// Applies only to file: protocol connections.
        /// </para>
        /// </remarks>
        [AmbientValue(null)]
        [Category("File Protocol - Text DB")]
        [DefaultValue(DefaultValueOf.TextDbLongVarcharFieldSeparator)]
        [Description("The default TEXT table LONGVARCHAR field separator.")]
        [DisplayName(Key.TextDbLongVarcharFieldSeparator)]
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        [RefreshProperties(RefreshProperties.All)]
        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1706:ShortAcronymsShouldBeUppercase", MessageId = "Member")]
        
        public string TextDbLongVarcharFieldSeparator
        {
            get { return m_settings.m_textDbLongVarcharFieldSeparator; }
            set { m_settings.SetTextDbLongVarcharFieldSeparator(value); }
        }

        #endregion

        #region TextDbQuoted

        /// <summary>
        /// Specifies whether the default behaviour is to delimit
        /// character data fields with double quotes.
        /// </summary>
        /// <remarks>
        /// <para>
        /// When true, the defaut TEXT table format on disk is to delimit
        /// character data fields with double quotes.
        /// </para>
        /// <para>
        /// This property must be specified by the connection that
        /// opens the database; it is ignored in subsequent connections.
        /// </para>
        /// <para>
        /// Applies only to file: protocol connections.
        /// </para>
        /// </remarks>
        [AmbientValue(null)]
        [Category("File Protocol - Text DB")]
        [DefaultValue(DefaultValueOf.TextDbQuoted)]
        [Description("When true, the defaut TEXT table format on disk is to delimit character data fields with double quotes.")]
        [DisplayName(Key.TextDbQuoted)]
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        [RefreshProperties(RefreshProperties.All)]
        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1706:ShortAcronymsShouldBeUppercase", MessageId = "Member")]        
        public bool TextDbQuoted
        {
            get { return m_settings.m_textDbQuoted; }
            set { m_settings.SetTextDbQuoted(value); }
        }

        #endregion

        #region TextDbVarcharFieldSeparator

        /// <summary>
        /// Specifies the default TEXT table VARCHAR field separator.
        /// </summary>
        /// <remarks>
        /// <para>
        /// The default value is ",".
        /// </para>
        /// <para>
        /// This property must be specified by the connection that
        /// opens the database; it is ignored in subsequent connections.
        /// </para>
        /// <para>
        /// Applies only to file: protocol connections.
        /// </para>
        /// </remarks>        
        [AmbientValue(null)]
        [Category("File Protocol - Text DB")]
        [DefaultValue(DefaultValueOf.TextDbVarcharFieldSeparator)]
        [Description("The default TEXT table VARCHAR field separator.")]
        [DisplayName(Key.TextDbVarcharFieldSeparator)]
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        [RefreshProperties(RefreshProperties.All)]
        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Naming", "CA1706:ShortAcronymsShouldBeUppercase", MessageId = "Member")]
        public string TextDbVarcharFieldSeparator
        {
            get { return m_settings.m_textDbVarcharFieldSeparator; }
            set { m_settings.SetTextDbVarcharFieldSeparator(value); }
        }

        #endregion

        #region TransactionLogMaxSize

        /// <summary>
        /// The maxmim size to which the Hsqldb transaction log will grow, in MB.
        /// </summary>
        /// <remarks>
        /// <para>
        /// The default is 200 (MB); zero (0) is interpreted as unlimited.
        /// </para>
        /// <para>
        /// Each time the transaction log reaches this size, the database is
        /// automatically checkpointed, effectively synchronizing the current
        /// state to the control and binary data files on disk, allowing the
        /// transaction log file to be reset to zero length.
        /// In effect, this determines the maximum amount of work that will ever
        /// need to be done when recovering an abended database instance.
        /// Setting this value lower will, on overage, reduce recovery times,
        /// but at the expense of increasing checkpoint overhead during normal
        /// operation.
        /// </para>
        /// <para>
        /// Note that there are few (if any) cases for setting this value
        /// higher than the default.  One exception might be to improve the
        /// performance of a very large initial all-or-nothing database load.
        /// </para>
        /// <para>
        /// This property must be specified by the connection that
        /// opens the database; it is ignored in subsequent connections.
        /// </para>
        /// <para>
        /// Applies only to file: protocol connections.
        /// </para>
        /// </remarks>
        [AmbientValue(null)]
        [Category("File Protocol")]
        [DefaultValue(DefaultValueOf.TransactionLogMaxSize)]
        [Description("The maxmim size to which the Hsqldb transaction log will grow, in MB.")]
        [DisplayName(Key.TransactionLogMaxSize)]
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        [RefreshProperties(RefreshProperties.All)]
        [CLSCompliant(false)]
        public uint TransactionLogMaxSize
        {
            get { return m_settings.m_transactionLogMaxSize; }
            set { m_settings.SetTransactionLogMaxSize(value); }
        }

        #endregion

        #region TransactionLogMaxSyncDelay

        /// <summary>
        /// Controls the initial confidence interval regarding the durability
        /// of committed transactions under program abends and system crashes
        /// for connections to file: mode embedded database instances.
        /// </summary>
        /// <remarks>
        /// <para>
        /// Specifies the minimum period (in milliseconds) between transaction
        /// log fsyncs, where zero (0) is interpreted as an fsync per commit.
        /// The default value is 25 milliseconds.
        /// </para>
        /// <para>
        /// Setting this to a non-default value is roughly equivalent to
        /// issuing the SQL: <c>SET WRITE_DELAY ${TransactionLogMaxSynchDelay}
        /// MILLIS</c> immediately after opening a connection.
        /// </para>
        /// <para>
        /// Be aware that a zero (0) setting typically limits the maximum number
        /// of transactions/second to far less than one hundred (100) because the
        /// average fsync time on modern hardware is ~10(+/-5) milliseconds.
        /// Also be aware that fsync does not necessarily guarantee the
        /// durability of commited transactions (i.e. that the transaction
        /// log is truly sync'ed to disk after it returns:
        /// <a href="http://brad.livejournal.com/2116715.html">reference</a>).
        /// Finally, be aware that without setting the <see cref="AutoShutdown"/>
        /// connection property to true on the connection that first opens the
        /// database instance (or explicitly issuing the SQL <c>SHUTDOWN</c>
        /// or <c>CHECKPOINT</c> command when done with an embedded file: mode
        /// database instance), up to as many transactions commited in the last
        /// <c>TransactionLogMaxSynchDelay</c> milliseconds may be lost at
        /// program termination.
        /// </para>
        /// <para>
        /// NOTE: Emperical evidence suggests that, on modern hardware, the
        /// default setting represents a near-optimal trade-off between
        /// transaction durability confidence interval and maximum transaction
        /// rate, being the lowest value observed to yeild rates not drastically
        /// lower than the maximum value measured under TPC-B benchmarks, while
        /// implying a theoretical maximum of (observed transactions per second /
        /// average observed fsync time) lost commited transactions that is not
        /// drastically greater than the number observed to be lost when
        /// performing experimental cold and warm resets.  Be aware, however,
        /// that on modern hardware, the default value yeilds about half the
        /// transaction rate (3000-4000 per second) of a 500-1000 millisecond
        /// value (7000-8000 per second), when all other settings (e.g. CacheScale)
        /// are adjusted for optimal transaction rate under a given load pattern.
        /// That said, it simply may not be acceptable to risk the possibility of
        /// losing so many commited transactions under disaster conditions.
        /// </para>
        /// <para>
        /// This property must be specified by the connection that
        /// opens the database; it is ignored in subsequent connections.
        /// </para>
        /// <para>
        /// Applies only to file: protocol connections.
        /// </para>
        /// </remarks>
        [AmbientValue(null)]
        [Category("File Protocol")]
        [DefaultValue(DefaultValueOf.TransactionLogMaxSyncDelay)]
        [Description("The minimum period (in milliseconds) between transaction log fsyncs, with zero (0) interpreted as an fsync per commit.")]
        [DisplayName(Key.TransactionLogMaxSyncDelay)]
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        [RefreshProperties(RefreshProperties.All)]
        [CLSCompliant(false)]
        public ushort TransactionLogMaxSyncDelay
        {
            get { return m_settings.m_transactionLogMaxSyncDelay; }
            set { m_settings.SetTransactionLogMaxSyncDelay(value); }
        }

        #endregion

        #region TransactionNoMultiRewrite

        /// <summary>
        /// Specifies whether an uncommited transaction's write-set may be
        /// overritten by another transaction.
        /// </summary>
        /// <remarks>
        /// <para>
        /// When true, exceptions are raised in reponse to attempted writes from
        /// any transaction upon data elements that belong to a different
        /// transaction's uncommited write set.
        /// </para>
        /// <para>
        /// This property must be specified by the connection that
        /// opens the database; it is ignored in subsequent connections.
        /// </para>
        /// <para>
        /// Applies only to embedded mode (i.e. file:, mem: and res: protocol) connections.
        /// </para>
        /// </remarks>
        [AmbientValue(null)]
        [Category("Embedded Database")]
        [DefaultValue(DefaultValueOf.TransactionNoMultiRewrite)]
        [Description("When true, prevents overwriting uncommited writes in different in-progress transactions.")]
        [DisplayName(Key.TransactionNoMultiRewrite)]
        [EditorBrowsable(EditorBrowsableState.Advanced)]
        [RefreshProperties(RefreshProperties.All)]
        public bool TransactionNoMultiRewrite
        {
            get { return m_settings.m_transactionNoMultRewrite; }
            set { m_settings.SetTransactionNoMultiRewrite(value); }
        }

        #endregion

        #region UserId

        /// <summary>
        /// The user identifier used to authenticate the connection request.
        /// </summary>
        [AmbientValue(null)]
        [Category("Connection")]
        [DefaultValue("SA")]
        [Description("The user identifier used to authenticate the connection request.")]
        [DisplayName(Key.UserId)]
        [RefreshProperties(RefreshProperties.All)]
        public string UserId
        {
            get { return m_settings.m_userId; }
            set { m_settings.SetUserId(value); }
        }

        #endregion

        #endregion Read-Write

        #region Read-Only Properties

        #region JdbcUrl
        /// <summary>
        /// The JDBC URL corresponding to the content of this
        /// <c>HsqlConnectionStringBuilder</c>.
        /// </summary>
        [Category("Connection (Read-Only)")]
        [Description("Derived value representing the equivalent JDBC connection URL")]
        [DisplayName("JdbcUrl")]
        [ReadOnly(true)]
        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Design", "CA1056:UriPropertiesShouldNotBeStrings")]        
        public String JdbcUrl
        {
            get { return m_settings.GetJdbcUrl(); }
        }
        #endregion

        #region StartupCommands
        /// <summary>
        /// The SQL commands that must be run to effect the specified settings.
        /// </summary>
        [Category("Connection (Read-Only)")]
        [Description("The SQL commands that must be run to effect the specified settings")]
        [ReadOnly(true)]
        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Performance", "CA1819:PropertiesShouldNotReturnArrays")]        
        public string[] StartupCommands
        {
            get { return m_settings.GetStartupCommands(); }
        }
        #endregion

        #endregion Read-Only

        #endregion Public Properties
    }
}
