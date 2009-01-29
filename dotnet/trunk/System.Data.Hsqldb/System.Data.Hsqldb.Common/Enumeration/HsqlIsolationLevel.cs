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
using IsolationConstants = java.sql.Connection.__Fields;
#endregion

namespace System.Data.Hsqldb.Common.Enumeration
{
    #region HsqlIsolationLevel

    /// <summary>
    /// <para>
    /// Transaction isolation levels recognized by the HSQLDB API.
    /// </para>
    /// <img src="/ClassDiagrams/System.Data.Hsqldb.Common.Enumeration.HsqlIsolationLevel.png"
    ///      alt="HsqlIsolationLevel Class Diagram"/>
    /// </summary>
    /// <author name="boucherb@users"/>
    public enum HsqlIsolationLevel
    {
        #region None
        /// <summary>
        /// Indicates that transactions are not supported.
        /// </summary>
        None = IsolationConstants.TRANSACTION_NONE,
        #endregion
        #region ReadUncomitted
        /// <summary>
        /// Indicates that dirty reads, non-repeatable reads
        /// and phantom reads can occur. This level allows a
        /// row changed by one transaction to be read by another
        /// transaction before any changes in that row have been
        /// committed (a "dirty read"). If any of the changes
        /// are rolled back, the second transaction will have
        /// retrieved an invalid row.
        /// </summary>
        ReadUncommited = IsolationConstants.TRANSACTION_READ_UNCOMMITTED,
        #endregion
        #region ReadComitted
        /// <summary>
        /// Indicates that dirty reads are prevented; non-repeatable
        /// reads and phantom reads can occur. This level only
        /// prohibits a transaction from reading a row with
        /// uncommitted changes in it.
        /// </summary>
        ReadCommited = IsolationConstants.TRANSACTION_READ_COMMITTED,
        #endregion
        #region RepeatableRead
        /// <summary>
        /// Indicates that dirty reads and non-repeatable reads are prevented;
        /// phantom reads can occur. This level prohibits a transaction from
        /// reading a row with uncommitted changes in it, and it also
        /// prohibits the situation where one transaction reads a row, a
        /// second transaction alters the row, and the first transaction
        /// rereads the row, getting different values the second time
        /// (a "non-repeatable read").
        /// </summary>
        RepeatableRead = IsolationConstants.TRANSACTION_REPEATABLE_READ,
        #endregion
        #region Serializable
        /// <summary>
        /// Indicates that dirty reads, non-repeatable reads and phantom reads
        /// are prevented. This level includes the prohibitions in
        /// TRANSACTION_REPEATABLE_READ and further prohibits the situation
        /// where one transaction reads all rows that satisfy a WHERE condition,
        /// a second transaction inserts a row that satisfies that WHERE
        /// condition, and the first transaction rereads for the same condition,
        /// retrieving the additional "phantom" row in the second read.
        /// </summary>
        Serializable = IsolationConstants.TRANSACTION_SERIALIZABLE
        #endregion
    }

    #endregion
}
