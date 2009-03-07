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
using System.Runtime.Serialization;
using System.Security.Permissions;
using StatementBatchConstants = java.sql.Statement.__Fields;
using HsqlTrace = org.hsqldb.Trace;
#endregion

namespace System.Data.Hsqldb.Common
{
    #region HsqlBatchUpdateException

    /// <summary>
    /// <para>
    /// Thrown when an error occurs during a batch update operation.
    /// </para>
    /// <img src="/ClassDiagrams/System.Data.Hsqldb.Common.HsqlBatchUpdateException.png"
    ///      alt="HsqlBatchUpdateException Class Diagram"/>
    /// </summary>
    [Serializable]
    public class HsqlBatchUpdateException : DbException
    {
        #region Constants

        #region SuccessNoInfo
        /// <summary>
        /// Indicates a successful statement exection for which no count
        /// of the number of rows it affected is available or applicable.
        /// </summary>
        /// <remarks>
        /// An item in the <see cref="UpdateCounts"/> array property is set to
        /// this value to denote the described condition for the i'th statement
        /// execution in a batch.
        /// </remarks>
        /// <value>-2</value>
        public const int SuccessNoInfo = StatementBatchConstants.SUCCESS_NO_INFO;
        #endregion

        #region ExecuteFailed
        /// <summary>
        /// Indicates that an error occured while executing a batch statement.
        /// </summary>
        /// <remarks>
        /// <para>
        /// An item in the <see cref="UpdateCounts"/> array property is set
        /// to this value to denote the described condition for the i'th
        /// statement execution in a batch.
        /// </para>
        /// <para>
        /// There are two possible batch execution behaviours, the first being
        /// to execute all statements without regard to errors and mark each
        /// failed execution in the <see cref="UpdateCounts"/> array, and the
        /// second being to terminate execution upon the first encountered
        /// error.
        /// </para>
        /// <para>
        /// When only the last item in the array has this value, this is a
        /// strong, although not perfect indication that the batch execution
        /// policy is to terminate on the first error encountered.
        /// </para>
        /// <para>
        /// When, in addition to only the last item in the array having this
        /// value, the array length is also less than the total number of
        /// statements known to be in the batch, then this is an 
        /// incontravertible indication that the batch execution policy is to
        /// terminate on the first error encountered.
        /// </para>
        /// <para>
        /// On the other hand, when an item other than the last has this value,
        /// then the the batch execution policy must be to execute all
        /// statements in a batch, without regard to errors.
        /// </para>
        /// <para>
        /// The HSQLDB 1.8.0 policy is to terminate execution upon the
        /// first encountered error, but it is possible the policy encountered
        /// at runtime may be different, for instance when connected to a more
        /// recently released back-end or when the update count is passed
        /// through from execution on a foreign server or from execution of a
        /// user-defined stored proceedure.
        /// </para>
        /// </remarks>
        /// <value>-3</value>
        public const int ExecuteFailed = StatementBatchConstants.EXECUTE_FAILED;
        #endregion

        #region VendorCode
        /// <summary>
        /// The error code value reported by all instances of 
        /// <c>HsqlBatchUpdateException</c> 
        /// </summary>
        /// <value>-40</value>
        public const int VendorCode = -HsqlTrace.GENERAL_ERROR; 
        #endregion

        #endregion

        #region Fields

        // classifier-scope

        private static readonly string m_Message = HsqlTrace.getMessage(
            -VendorCode) + " - Batch update failed";
        private static readonly string m_SqlState = HsqlTrace.error(
            -VendorCode).getSQLState();
        private static readonly int[] m_NoUpdateCount = new int[0];

        // instance-scope
        private int[] m_updateCounts;

        #endregion

        #region Constructors

        #region HsqlBatchUpdateException()
        /// <summary>
        /// Constructs a new <c>HsqlBatchUpdateException</c> instance.
        /// </summary>
        public HsqlBatchUpdateException()
            : this(m_NoUpdateCount)
        { } 
        #endregion

        #region HsqlBatchUpdateException(string)
        /// <summary>
        /// Constructs a new <c>HsqlBatchUpdateException</c> instance 
        /// with the given message.
        /// </summary>
        /// <param name="message">The message.</param>
        public HsqlBatchUpdateException(string message)
            : base(m_Message + " : " + message)
        {
            m_updateCounts = m_NoUpdateCount;
        } 
        #endregion

        #region HsqlBatchUpdateException(string,Exception)
        /// <summary>
        /// Constructs a new <c>HsqlBatchUpdateException</c> instance 
        /// with the given message and inner exception.
        /// </summary>
        /// <param name="message">The message.</param>
        /// <param name="innerException">The inner exception.</param>
        public HsqlBatchUpdateException(string message, Exception innerException)
            : base(m_Message + " : " + message, innerException)
        {
            m_updateCounts = m_NoUpdateCount;
        } 
        #endregion

        #region HsqlBatchUpdateException(int[])
        /// <summary>
        /// Constructs a new <c>HsqlBatchException</c> with
        /// the specified update counts.
        /// </summary>
        /// <param name="updateCounts"><para>
        /// The update counts.
        /// </para><para>
        /// <c>null</c> may be used to indicate no update count,
        /// and, for efficiency reasons, is to be generally peferred
        /// over submitting a zero-length array.
        /// </para></param>
        public HsqlBatchUpdateException(int[] updateCounts)
            : base(m_Message)
        {
            m_updateCounts = (updateCounts == null) 
                ? m_NoUpdateCount : updateCounts;
        }
        #endregion

        #region HsqlBatchUpdateException(string, int[])
        /// <summary>
        /// Constructs a new <c>HsqlBatchException</c> with
        /// the specified message and update counts.
        /// </summary>
        /// <param name="message">The message.</param>
        /// <param name="updateCounts"><para>
        /// The update counts.
        /// </para><para>
        /// <c>null</c> may be used to indicate no update count,
        /// and, for efficiency reasons, is to be generally peferred
        /// over submitting a zero-length array.
        /// </para></param>
        public HsqlBatchUpdateException(String message, int[] updateCounts)
            : base(m_Message + " : " + message)
        {
            m_updateCounts = (updateCounts == null)
                ? m_NoUpdateCount : updateCounts;
        }
        #endregion

        #region HsqlBatchUpdateException(SerializationInfo,StreamingContext)
        /// <summary>
        /// Constructs a new <c>HsqlBatchUpdateException</c> instance 
        /// with the given <c>SerializationInfo</c> and 
        /// <c>StreamingContext</c>.
        /// </summary>
        /// <param name="info">
        /// The <see cref="SerializationInfo"/> that holds the serialized
        /// object data about the exception being thrown.
        /// </param>
        /// <param name="context">
        /// The <see cref="StreamingContext"/> that contains contextual
        /// information about the source or destination.
        /// </param>
        protected HsqlBatchUpdateException(
            SerializationInfo info,
            StreamingContext context) : base(info, context)
        {
            if (info == null)
            {
                throw new ArgumentNullException("info");
            }

            m_updateCounts = (int[])info.GetValue("UpdateCounts", typeof(int[]));
        }
        #endregion

        #endregion

        #region Properties

        #region ErrorCode
        /// <summary>
        /// The vendor-specific code associated with the error.
        /// </summary>
        /// <value>the SQL <see cref="VendorCode"/></value>
        public override int ErrorCode
        {
            get { return HsqlBatchUpdateException.VendorCode; }
        } 
        #endregion

        #region SqlState

        /// <summary>
        /// Follows the ANSI SQL standard for categorizing the source of this
        /// exception.
        /// </summary>
        /// <value>
        /// Five-character error code which identifies the source of this
        /// exception.
        /// </value>
        public string SqlState
        {
            get { return m_SqlState; }
        }

        #endregion

        #region UpdateCounts

        /// <summary>
        /// Gets the update counts.
        /// </summary>
        /// <remarks>
        /// <para>
        /// The order of elements always corresponds to the order in which the
        /// commands were added to the batch.
        /// </para>
        /// <para>
        /// The HSQLDB 1.8.0 policy is to terminate execution upon the
        /// first encountered error, but it is possible the policy encountered
        /// at runtime may be different, for instance when connected to a more
        /// recently released back-end or when the update count is passed
        /// through from execution on a foreign server or from execution of a
        /// user-defined stored proceedure.
        /// </para>
        /// <para>
        /// Under the HSQLDB 1.8.0 policy, commands subsequent to the
        /// problematic command are ignored and the array is of length
        /// one greater than the number of commands executed successfully
        /// before the error was encountered, in which case the last
        /// element (and only the last element) contains the value
        /// <see cref="ExecuteFailed"/>.
        /// </para>
        /// </remarks>
        /// <value>The update counts.</value>
        /// <seealso cref="SuccessNoInfo"/>
        [System.Diagnostics.CodeAnalysis.SuppressMessage("Microsoft.Performance", "CA1819:PropertiesShouldNotReturnArrays")]
        public int[] UpdateCounts
        {
            get { return (int[])m_updateCounts.Clone(); }
        }

        #endregion

        #endregion

        #region Method Overrides

        #region GetObjectData(SerializationInfo, StreamingContext)

        /// <summary>
        /// Sets the <see cref="SerializationInfo"/> with
        /// information about the exception.
        /// </summary>
        /// <param name="info">The <see cref="SerializationInfo"/> that holds
        /// the serialized object data about the exception
        /// being thrown.</param>
        /// <param name="context">The <see cref="StreamingContext"></see> that contains
        /// contextual information about the source or destination.</param>
        /// <exception cref="ArgumentNullException">
        /// When the info parameter is a null reference (Nothing in Visual Basic).
        /// </exception>
        /// <PermissionSet><IPermission class="System.Security.Permissions.FileIOPermission, mscorlib, Version=2.0.3600.0, Culture=neutral, PublicKeyToken=b77a5c561934e089" version="1" Read="*AllFiles*" PathDiscovery="*AllFiles*"/><IPermission class="System.Security.Permissions.SecurityPermission, mscorlib, Version=2.0.3600.0, Culture=neutral, PublicKeyToken=b77a5c561934e089" version="1" Flags="SerializationFormatter"/></PermissionSet>
        [SecurityPermission(SecurityAction.Demand, SerializationFormatter=true)] 
        public override void GetObjectData(SerializationInfo info, StreamingContext context)
        {
            if (info == null)
            {
                throw new ArgumentNullException("info");
            }

            base.GetObjectData(info, context);

            info.AddValue("UpdateCounts", m_updateCounts, typeof(int[]));
        }
        #endregion

        #endregion
    }

    #endregion
}
