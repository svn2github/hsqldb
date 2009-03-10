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
using System.Data.Common;
using System.Security;
using System.Security.Permissions;

#endregion

namespace System.Data.Hsqldb.Client
{
    #region HsqlProviderFactory
    /// <summary>
    /// <para>
    /// The HSQLDB <see cref="DbProviderFactory">DbProviderFactory</see> implementation.
    /// </para>
    /// <img src="../Documentation/ClassDiagrams/System.Data.Hsqldb.Client.HsqlProviderFactory.png"
    ///      alt="HsqlProviderFactory Class Diagram"/>
    /// </summary>
    public sealed class HsqlProviderFactory : DbProviderFactory
    {
        #region Constants

        #region FactoryName
        /// <summary>
        /// The value of <c>name</c> attribute with which this factory is
        /// registered under the <c>DbProviderFactories</c> element of
        /// the <c>system.data</c> config section.
        /// </summary>
        public const string FactoryName = "HSQLDB Data Provider";
        #endregion

        #region FactoryInvariant
        /// <summary>
        /// The value of <c>invariant</c> attribute with which this factory is
        /// registered under the <c>DbProviderFactories</c> element of
        /// the <c>system.data</c> config section.
        /// </summary>
        public const string FactoryInvariant = "System.Data.Hsqldb.Client";
        #endregion

        #region FactoryDescription
        /// <summary>
        /// The value of <c>description</c> attribute with which this factory is
        /// registered under the <c>DbProviderFactories</c> element of
        /// the <c>system.data</c> config section.
        /// </summary>
        public const string FactoryDescription = ".Net Framework Data Provider for HSQLDB";
        #endregion

        #endregion

        #region Fields
        /// <summary>
        /// Required by DbProviderFactory contract.
        /// </summary>
        public static readonly HsqlProviderFactory Instance; 
        #endregion

        #region Constructors

        /// <summary>
        /// Initializes the <see cref="HsqlProviderFactory"/> class.
        /// </summary>
        static HsqlProviderFactory()
        {
            Instance = new HsqlProviderFactory();
        }

        /// <summary>
        /// Initializes a new instance of the
        /// <see cref="HsqlProviderFactory"/> class.
        /// </summary>
        private HsqlProviderFactory()
        {
            // external construction disabled.
        }
        #endregion

        #region Methods

        #region CreateCommand()
        /// <summary>
        /// Returns an object that implements the
        /// <see cref="DbCommand"/> class.
        /// </summary>
        /// <returns>
        /// A new <see cref="DbCommand"/> instance.
        /// </returns>
        public override DbCommand CreateCommand()
        {
            return new HsqlCommand();
        }
                #endregion

        #region CreateCommandBuilder()
        /// <summary>
        /// Returns an object that implements the
        /// <see cref="DbCommandBuilder"/> class.
        /// </summary>
        /// <returns>
        /// A new <see cref="DbCommandBuilder"/> instance.
        /// </returns>
        public override DbCommandBuilder CreateCommandBuilder()
        {
            return new HsqlCommandBuilder();
        }
        #endregion

        #region CreateConnection()
        /// <summary>
        /// Returns an object that implements the
        /// <see cref="DbConnection"/> class.
        /// </summary>
        /// <returns>
        /// A new <see cref="DbConnection"/> instance.
        /// </returns>
        public override DbConnection CreateConnection()
        {
            return new HsqlConnection();
        }
        #endregion

        #region CreateConnectionStringBuilder()
        /// <summary>
        /// Returns an object that implements
        /// <see cref="DbConnectionStringBuilder"/>.
        /// </summary>
        /// <returns>
        /// A new <see cref="DbConnectionStringBuilder"/> instance.
        /// </returns>
        public override DbConnectionStringBuilder CreateConnectionStringBuilder()
        {
            return new HsqlConnectionStringBuilder();
        }
        #endregion

        #region CreateDataAdapter()
        /// <summary>
        /// Returns an object that implements <see cref="DbDataAdapter"/>.
        /// </summary>
        /// <returns>
        /// A new <see cref="DbDataAdapter"/> instance.
        /// </returns>
        public override DbDataAdapter CreateDataAdapter()
        {
            return new HsqlDataAdapter();
        }
        #endregion

        #region CreateDataSourceEnumerator()
        /// <summary>
        /// Returns an object that implements the
        /// <see cref="DbDataSourceEnumerator"/> class.
        /// </summary>
        /// <returns>
        /// A <see cref="DbDataSourceEnumerator"/> instance.
        /// </returns>
        public override DbDataSourceEnumerator CreateDataSourceEnumerator()
        {
            return HsqlDataSourceEnumerator.Instance;
        }
        #endregion

        #region CreateParameter()
        /// <summary>
        /// Returns an object that implements the
        /// <see cref="DbParameter"/> class.
        /// </summary>
        /// <returns>
        /// A new <see cref="DbParameter"/> instance.
        /// </returns>
        public override DbParameter CreateParameter()
        {
            return new HsqlParameter();
        }
        #endregion

        #region CreatePermission(PermissionState)
        /// <summary>
        /// Returns an object that implements the
        /// <see cref="CodeAccessPermission"/> class.
        /// </summary>
        /// <param name="state">
        /// One of the <see cref="PermissionState"/> values.
        /// </param>
        /// <returns>
        /// A new <see cref="CodeAccessPermission"/> instance with
        /// the specified <see cref="PermissionState"/>.
        /// </returns>
        public override CodeAccessPermission CreatePermission(PermissionState state)
        {
            return new HsqlDataPermission(state);
        }
        #endregion
 
        #endregion

        #region Properties

        #region CanCreateDataSourceEnumerator
        /// <summary>
        /// Specifies whether this <see cref="DbProviderFactory"/>
        /// supports the <see cref="DbDataSourceEnumerator"/> class.
        /// </summary>
        /// <value>
        /// <c>true</c> if this <see cref="DbProviderFactory"/>
        /// supports the <see cref="DbDataSourceEnumerator"/>
        /// class; otherwise <c>false</c>.
        /// </value>
        public override bool CanCreateDataSourceEnumerator
        {
            get { return true; }
        }
        #endregion

        #endregion
    } 
    #endregion
}