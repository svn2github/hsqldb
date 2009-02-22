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

using System;
using System.Collections.Generic;
using System.Text;

namespace System.Data.Hsqldb.Common.Enumeration
{
    #region WindowsServerType
    /// <summary>
    /// Possible types of windows servers.
    /// </summary>
    [FlagsAttribute]
    [CLSCompliant(false)]
    public enum WindowsServerType : uint
    {
        /// <summary>
        /// All workstations
        /// </summary>
        SV_TYPE_WORKSTATION = 0x00000001,
        /// <summary>
        /// All computers that have the server service running
        /// </summary>
        SV_TYPE_SERVER = 0x00000002,
        /// <summary>
        /// Any server running Microsoft SQL Server
        /// </summary>
        SV_TYPE_SQLSERVER = 0x00000004,
        /// <summary>
        /// Primary domain controller
        /// </summary>
        SV_TYPE_DOMAIN_CTRL = 0x00000008,
        /// <summary>
        /// Backup domain controller
        /// </summary>
        SV_TYPE_DOMAIN_BAKCTRL = 0x00000010,
        /// <summary>
        /// Server running the Timesource service
        /// </summary>
        SV_TYPE_TIME_SOURCE = 0x00000020,
        /// <summary>
        /// Apple File Protocol servers
        /// </summary>
        SV_TYPE_AFP = 0x00000040,
        /// <summary>
        /// Novell servers
        /// </summary>
        SV_TYPE_NOVELL = 0x00000080,
        /// <summary>
        /// LAN Manager 2.x domain member
        /// </summary>
        SV_TYPE_DOMAIN_MEMBER = 0x00000100,
        /// <summary>
        /// Server sharing print queue
        /// </summary>
        SV_TYPE_PRINTQ_SERVER = 0x00000200,
        /// <summary>
        /// Server running dial-in service
        /// </summary>
        SV_TYPE_DIALIN_SERVER = 0x00000400,
        /// <summary>
        /// Xenix server
        /// </summary>
        SV_TYPE_XENIX_SERVER = 0x00000800,
        /// <summary>
        /// Windows NT workstation or server
        /// </summary>
        SV_TYPE_NT = 0x00001000,
        /// <summary>
        /// Server running Windows for Workgroups
        /// </summary>
        SV_TYPE_WFW = 0x00002000,
        /// <summary>
        /// Microsoft File and Print for NetWare
        /// </summary>
        SV_TYPE_SERVER_MFPN = 0x00004000,
        /// <summary>
        /// Server that is not a domain controller
        /// </summary>
        SV_TYPE_SERVER_NT = 0x00008000,
        /// <summary>
        /// Server that can run the browser service
        /// </summary>
        SV_TYPE_POTENTIAL_BROWSER = 0x00010000,
        /// <summary>
        /// Server running a browser service as backup
        /// </summary>
        SV_TYPE_BACKUP_BROWSER = 0x00020000,
        /// <summary>
        /// Server running the master browser service
        /// </summary>
        SV_TYPE_MASTER_BROWSER = 0x00040000,
        /// <summary>
        /// Server running the domain master browser
        /// </summary>
        SV_TYPE_DOMAIN_MASTER = 0x00080000,
        /// <summary>
        /// Windows 95 or later
        /// </summary>
        SV_TYPE_WINDOWS = 0x00400000,
        /// <summary>
        /// Root of a DFS tree
        /// </summary>
        SV_TYPE_DFS = 0x00800000,
        /// <summary>
        /// Terminal Server
        /// </summary>
        SV_TYPE_TERMINALSERVER = 0x02000000,
        /// <summary>
        /// Server clusters available in the domain
        /// </summary>
        SV_TYPE_CLUSTER_NT = 0x01000000,
        /// <summary>
        /// Cluster virtual servers available in the domain
        /// (Not supported for Windows 2000/NT)
        /// </summary>			
        SV_TYPE_CLUSTER_VS_NT = 0x04000000,
        /// <summary>
        /// IBM DSS (Directory and Security Services) or equivalent
        /// </summary>
        SV_TYPE_DCE = 0x10000000,
        /// <summary>
        /// Return list for alternate transport
        /// </summary>
        SV_TYPE_ALTERNATE_XPORT = 0x20000000,
        /// <summary>
        /// Return local list only
        /// </summary>
        SV_TYPE_LOCAL_LIST_ONLY = 0x40000000,
        /// <summary>
        /// Lists available domains
        /// </summary>
        SV_TYPE_DOMAIN_ENUM = 0x80000000
    }
    #endregion
}
