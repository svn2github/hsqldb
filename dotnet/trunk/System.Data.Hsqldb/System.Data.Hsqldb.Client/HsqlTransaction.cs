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
using System.Data.Hsqldb.Client.Internal;

#endregion

namespace System.Data.Hsqldb.Client
{
    /// <summary>
    /// <para>
    /// Implements <see cref="DbTransaction">DbTransaction</see>.
    /// </para>
    /// <img src="../Documentation/ClassDiagrams/System.Data.Hsqldb.Client.HsqlTransaction.png"
    ///      alt="HsqlTransaction Class Diagram"/>
    /// </summary>
    /// <remarks>
    /// <para>
    /// <b>Warning</b>: because the HSQLDB 1.8 database engine does not
    /// support the notion of transaction identifiers, it is impossible to
    /// query whether a specific transaction is in progress or has been
    /// terminated. Hence, it is currently to be considered a programming
    /// error to mix execution of explicit SQL transaction control (e.g.
    /// COMMIT, ROLLBACK, SET AUTOCOMIT...) or data definition language
    /// (e.g. CREATE, ALTER, DROP) commands with programmatic transaction
    /// control.
    /// </para>
    /// </remarks>
    /// <author name="boucherb@users"/>
    public sealed partial class HsqlTransaction : DbTransaction {}
}