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
using System.Security;
using System.Security.Permissions;
#endregion

namespace System.Data.Hsqldb.Client
{
    /// <summary>
    /// <para>
    /// The HSQLDB <see cref="DBDataPermission">DBDataPermission</see> implementation.
    /// </para>
    /// <img src="../Documentation/ClassDiagrams/System.Data.Hsqldb.Client.HsqlDataPermission.png"
    ///      alt="HsqlDataPermission Class Diagram"/>
    /// </summary>
    [Serializable]
    public class HsqlDataPermission : DBDataPermission
    {
        /// <summary>
        /// Constructs a new <c>HsqlDataPermission</c> instance.
        /// </summary>
        public HsqlDataPermission() : this(PermissionState.None)
        {
        }

        /// <summary>
        /// Initializes a new instance of the
        /// <see cref="HsqlDataPermission"/> class.
        /// </summary>
        /// <param name="permission">The permission.</param>
        private HsqlDataPermission(HsqlDataPermission permission)
            : base(permission)
        {
        }

        /// <summary>
        /// Initializes a new instance of the
        /// <see cref="HsqlDataPermission"/> class.
        /// </summary>
        /// <param name="state">
        /// One of the <see cref="PermissionState"/> values.
        /// </param>
        public HsqlDataPermission(PermissionState state)
            : base(state)
        {
        }

        /// <summary>
        /// Adds a new connection string and a set of restricted keywords
        /// to this <see cref="HsqlDataPermission"/> object.
        /// </summary>
        /// <param name="connectionString">The connection string.</param>
        /// <param name="restrictions">The key restrictions.</param>
        /// <param name="behavior">
        /// One of the <see cref="KeyRestrictionBehavior"/> enumeration
        /// values.
        /// </param>
        public override void Add(string connectionString,
                                 string restrictions,
                                 KeyRestrictionBehavior behavior)
        {
            base.Add(connectionString, restrictions, behavior);

            if (behavior == KeyRestrictionBehavior.AllowOnly)
            {
                //
            }
        }

        /// <summary>
        /// Returns the <see cref="HsqlDataPermission"/> as
        /// an <see cref="IPermission"/>.
        /// </summary>
        /// <returns>A copy of the current permission object.</returns>
        /// <PermissionSet>
        /// <IPermission class="System.Security.Permissions.SecurityPermission, mscorlib, Version=2.0.3600.0, Culture=neutral, PublicKeyToken=b77a5c561934e089" version="1" Flags="UnmanagedCode"/>
        /// </PermissionSet>
        public override IPermission Copy()
        {
            return new HsqlDataPermission(this);
        }
    }
}