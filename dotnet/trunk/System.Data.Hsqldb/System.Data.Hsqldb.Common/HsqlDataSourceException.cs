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
using System.Data.Common;
using System.Runtime.Serialization;
using org.hsqldb;
using SQLException = java.sql.SQLException;
using HsqlException = org.hsqldb.HsqlException;

#endregion

namespace System.Data.Hsqldb.Common
{
    #region HsqlDataSourceException
    /// <summary>
    /// <para>
    /// The HSQLDB implementation of <see cref="DbException">DbException</see>.
    /// </para>
    /// <img src="/ClassDiagrams/System.Data.Hsqldb.Common.HsqlDataSourceException.png"
    ///      alt="HsqlDataSourceException Class Diagram"/>
    /// </summary>
    /// <author name="boucherb@users"/>
    [Serializable]
    public sealed class HsqlDataSourceException : DbException
    {
        #region Fields
        private string m_state;
        private int m_code;
        private HsqlDataSourceExceptionCollection m_exceptions;
        #endregion

        #region Constructors

        #region HsqlDataSourceException()
        /// <summary>
        /// Constructs a new <c>HsqlDataSourceException</c> instance.
        /// </summary>
        /// <remarks>
        /// <para>
        /// In this case, <c>Message</c> and <c>SQLState</c> will
        /// be <c>null</c>, while <c>ErrorCode</c> will be
        /// <c>System.Int32.MinValue</c>.
        /// </para>
        /// <para>
        /// Use of this constructor signature should be strictly limited
        /// to situations that are in no way related to an exceptional
        /// SQL state.
        /// </para>
        /// </remarks>       
        public HsqlDataSourceException()
            : this("An HSQLDB exception occured.") { }
        #endregion

        #region HsqlDataSourceException(string)
        /// <summary>
        /// Constructs a new <c>HsqlDataSourceException</c>
        /// instance with the specified error message.
        /// </summary>
        /// <remarks>
        /// <para>
        /// In this case <c>SQLState</c> will be <c>null</c>,
        /// while <c>ErrorCode</c> will be
        /// <c>System.Int32.MinValue</c>.
        /// </para>
        /// <para>
        /// Use of this constructor signature should be strictly limited
        /// to situations that are in no way related to an exceptional
        /// SQL state.
        /// </para>
        /// </remarks>       
        /// <param name="message">
        /// The message to display for this exception.
        /// </param>
        public HsqlDataSourceException(string message)
            : this(message, Trace.GENERAL_ERROR, "S1000") { }
        #endregion

        #region HsqlDataSourceException(SerializationInfo,StreamingContext)
        /// <summary>
        /// Constructs a new <c>HsqlDataSourceException</c> instance
        /// from the specified serialization information and context.
        /// </summary>
        /// <param name="info">
        /// The <see cref="SerializationInfo"/> that holds the serialized
        /// object data from which to construct the exception.
        /// </param>
        /// <param name="context">
        /// The <see cref="StreamingContext"/> that contains contextual
        /// information about the source or destination.
        ///</param>
        public HsqlDataSourceException(
            SerializationInfo info,
            StreamingContext context)
            : base(info, context)
        {
            m_code = (int)info.GetValue("m_code", typeof(int));
            m_state = (string)info.GetValue("m_state", typeof(string));
            m_exceptions = (HsqlDataSourceExceptionCollection)info.GetValue(
                "m_exceptions", typeof(HsqlDataSourceExceptionCollection));
        }
        #endregion

        #region HsqlDataSourceException(string,Exception)
        /// <summary>
        /// Constructs a new <c>HsqlDataSourceException</c>
        /// instance with the specified error message and
        /// cause.
        /// </summary>
        /// <remarks>
        /// <para>
        /// In this case, <c>SQLState</c> will be <c>null</c>
        /// and <c>ErrorCode</c> will be <c>System.Int32.MinValue</c>.
        /// </para>
        /// <para>
        /// Use of this constructor signature should be strictly limited
        /// to situations where the source of the <c>innerException</c>
        /// is in no way related to an exceptional SQL state.
        /// </para>
        /// </remarks>
        /// <param name="message">
        /// The error message string.
        /// </param>
        /// <param name="innerException">
        /// The cause.
        /// </param>
        public HsqlDataSourceException(
            string message,
            Exception innerException)
            : base(message, innerException)
        {
            m_code = Trace.GENERAL_ERROR;
            m_state = "S1000";
        }
        #endregion

        #region HsqlDataSourceException(string,int)
        /// <summary>
        /// Constructs a new <c>HsqlDataSourceException</c> instance
        /// with the specified error message and code.
        /// </summary>
        /// <remarks>
        /// If possible, <c>SQLState</c> is derived from <c>errorCode</c>;
        /// otherwise, <c>SQLState</c> will be <c>null</c>.
        /// </remarks>
        /// <param name="message">
        /// The error message that explains the reason for the exception.
        /// </param>
        /// <param name="errorCode">
        /// The error code for the exception.
        /// </param>
        public HsqlDataSourceException(string message, int errorCode)
            : this(Trace.error(errorCode, message)) { }
        #endregion

        #region HsqlDataSourceException(string,int,string)
        /// <summary>
        /// Constructs a new <c>HsqlDataSourceException</c> instance
        /// with the specified error message, error code and SQL state.
        /// </summary>
        /// <param name="message">
        /// The error message that explains the reason for the exception.
        /// </param>
        /// <param name="errorCode">
        /// The error code for the exception.
        /// </param>
        /// <param name="sqlState">
        /// The SQL state for the exception
        /// </param>
        public HsqlDataSourceException(
            string message,
            int errorCode,
            string sqlState)
            : base(message)
        {
            m_code = errorCode;
            m_state = sqlState;
        }
        #endregion

        #region HsqlDataSourceException(java.sql.SQLException)
        /// <summary>
        /// Constructs a new <c>HsqlDataSourceException</c> instance
        /// wrapping the specified <c>java.sql.SQLException</c>.
        /// </summary>
        /// <param name="se">
        /// The <c>java.sql.SQLException</c> to wrap.
        /// </param>
        public HsqlDataSourceException(java.sql.SQLException se)
            : base(se.getMessage(), se)
        {
            m_code = se.getErrorCode();
            m_state = se.getSQLState();

            while (null != (se = se.getNextException()))
            {
                Exceptions.Add(new HsqlDataSourceException(
                    se.getMessage(), se.getErrorCode(), se.getSQLState()));
            }
        }
        #endregion

        #region HsqlDataSourceException(HsqlException)
        /// <summary>
        /// Constructs a new <c>HsqlDataSourceException</c> instance
        /// wrapping the specified <c>org.hsqldb.HsqlException</c>.
        /// </summary>
        /// <param name="he">The <c>org.hsqldb.HsqlException</c> to wrap.</param>
        public HsqlDataSourceException(HsqlException he)
            : base(he.getMessage(), he)
        {
            m_state = he.getSQLState();
            m_code = he.getErrorCode();
        }
        #endregion

        #region HsqlDataSourceException(Result)
        /// <summary>
        /// Constructs a new <c>HsqlDataSourceException</c> instance
        /// wrapping the given result.
        /// </summary>
        /// <remarks>
        /// It is assumed the given result object actually
        /// represents an error response.
        /// </remarks>
        /// <param name="result">The result.</param>
        public HsqlDataSourceException(Result result)
            : this(new HsqlException(result)) { }
        #endregion

        #endregion

        #region Properties

        #region ErrorCode
        /// <summary>
        /// A numeric code identifying the provider-specific exception type. 
        /// </summary>
        /// <remarks>
        /// Typically, this is the HSQLDB vendor code identifying
        /// the HSQLDB-specific type of the exception.
        /// </remarks>
        /// <value>provider-specific exception type code</value>
        public override int ErrorCode
        {
            get { return m_code; }
        }
        #endregion

        #region SQLState
        /// <summary>
        /// Follows the ANSI SQL standard for categorizing
        /// the source of this exception.
        /// </summary>
        /// <value>
        /// Five-character error code which identifies the
        /// source of this exception.
        /// </value>
        public string SQLState
        {
            get { return m_state; }
        }
        #endregion

        #region Exceptions
        /// <summary>
        /// A collection of zero or more chained exceptions that give
        /// detailed information about exceptions generated by the
        /// Data Provider for the HSQLDB Database Engine.
        /// </summary>
        /// <value>
        /// Indexed collection of chained exceptions.
        /// </value>
        public HsqlDataSourceExceptionCollection Exceptions
        {
            get
            {
                if (m_exceptions == null)
                {
                    m_exceptions = new HsqlDataSourceExceptionCollection();
                }

                return m_exceptions;
            }
        }
        #endregion

        #endregion

        #region Methods

        #region GetObjectData(SerializationInfo,StreamingContext)
        /// <summary>
        /// Updates the <see cref="SerializationInfo"/> with information
        /// about the exception.
        /// </summary>
        /// <param name="info">
        /// The <see cref="SerializationInfo"/> that holds the serialized 
        /// object data used to describe the exception.
        /// </param>
        /// <param name="context">
        /// The <see cref="StreamingContext"/> that contains contextual 
        /// information about the source or destination.
        /// </param>
        /// <exception cref="ArgumentNullException">
        /// When the <c>info</c> parameter is a null reference
        /// </exception>
        /// <PermissionSet>
        ///   <IPermission class="System.Security.Permissions.FileIOPermission, mscorlib, Version=2.0.3600.0, Culture=neutral, PublicKeyToken=b77a5c561934e089" version="1" Read="*AllFiles*" PathDiscovery="*AllFiles*"/>
        ///   <IPermission class="System.Security.Permissions.SecurityPermission, mscorlib, Version=2.0.3600.0, Culture=neutral, PublicKeyToken=b77a5c561934e089" version="1" Flags="SerializationFormatter"/>
        /// </PermissionSet>
        public override void GetObjectData(SerializationInfo info, StreamingContext context)
        {
            if (info == null)
            {
                throw new ArgumentNullException("info");
            }

            info.AddValue("m_code", this.m_code, typeof(int));
            info.AddValue("m_state", this.m_state, typeof(string));
            info.AddValue("m_exceptions", this.m_exceptions,
                typeof(HsqlDataSourceExceptionCollection));

            base.GetObjectData(info, context);
        }
        #endregion

        #endregion
    } 
    #endregion
}
