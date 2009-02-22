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
using System.Collections;
using System.Runtime.InteropServices;
using System.Collections.Generic;
using System.Globalization;
using System.Data.Hsqldb.Common.Enumeration;
using System.Threading;

namespace System.Data.Hsqldb.Client.Design
{
    /// <summary>
    /// 
    /// </summary>
    public class ComputerCollection : ICollection, IDisposable
    {
        #region Fields
        private ComputerInfo[] m_computers;
        private string m_lastError = "";
        private object m_syncRoot;
        #endregion

        #region Constructors

        #region ComputerCollection(ServerType,string)
        /// <summary>
        /// Converts ServerType to its underlying value
        /// </summary>
        /// <param name="serverType">One of the ServerType values</param>
        /// <param name="domain">The domain to search for computers in</param>
        [CLSCompliant(false)]
        public ComputerCollection(WindowsServerType serverType, string domain)
            : this(UInt32.Parse(Enum.Format(typeof(WindowsServerType), serverType, "x"), NumberStyles.HexNumber), domain)
        {
        }
        #endregion

        #region ComputerCollection(uint,string)
        /// <summary>
        /// Populates with broadcasting computers.
        /// </summary>
        /// <param name="serverType">Server type filter</param>
        /// <param name="domain">The domain to search for computers in</param>
        [CLSCompliant(false)]
        public ComputerCollection(uint serverType, string domain)
        {
            m_computers = ComputerInfo.GetComputerInfo(
                serverType,
                domain,
                out m_lastError);
        }
        #endregion

        #endregion

        #region Methods

        #region CopyTo(Array,int)
        /// <summary>
        /// See <see cref="ICollection.CopyTo(Array,int)"/>.
        /// </summary>
        /// <param name="array">The destination array.</param>
        /// <param name="index">The destination index.</param>
        public void CopyTo(Array array, int index)
        {
            CheckDisposed();

            Array.Copy(m_computers, 0, array, index, m_computers.Length);
        }
        #endregion

        #region CheckDisposed()
        private void CheckDisposed()
        {
            if (m_computers == null)
            {
                throw new ObjectDisposedException(GetType().FullName);
            }
        }
        #endregion

        #region  Dispose()
        /// <summary>
        /// Cleanup internal resources.
        /// </summary>
        public void Dispose()
        {
            m_computers = null;
        }
        #endregion

        #region GetEnumerator()
        /// <summary>
        /// Obtains the enumerator for ComputerEnumerator class
        /// </summary>
        /// <returns>IEnumerator</returns>
        public IEnumerator GetEnumerator()
        {
            CheckDisposed();

            return m_computers.GetEnumerator();
        }
        #endregion 
        
        #endregion

        #region Indexers
        
        #region this[int]
        /// <summary>
        /// Gets the <see cref="ComputerInfo"/> at the specified index.
        /// </summary>
        /// <value>
        /// the <see cref="ComputerInfo"/> at the specified index.
        /// </value>
        public ComputerInfo this[int index]
        {
            get
            {
                CheckDisposed();

                return m_computers[index];
            }
        }                
        #endregion 

        #endregion        

        #region Properties
        
        #region Count
        /// <summary>
        /// Total computers described by this collection
        /// </summary>
        public int Count
        {
            get { return (m_computers == null) ? 0 : m_computers.Length; }
        }
                #endregion

        #region LastError
        /// <summary>
        /// Last error message
        /// </summary>
        public string LastError
        {
            get { return m_lastError; }
        }
        #endregion

        #region IsSynchronized
        /// <summary>
        /// See <see cref="ICollection.IsSynchronized"/>.
        /// </summary>
        public bool IsSynchronized
        {
            get { return false; }
        }
        #endregion

        #region SyncRoot
        /// <summary>
        /// See <see cref="ICollection.SyncRoot"/>.
        /// </summary>
        public object SyncRoot
        {
            get
            {
                if (m_syncRoot == null)
                {
                    Interlocked.CompareExchange(ref m_syncRoot, new object(), null);
                }

                return m_syncRoot;
            }
        }
        #endregion 

        #endregion

        #region Inner Structures

        #region ComputerInfo
        /// <summary>
        /// Holds computer info.
        /// </summary>
        /// <remarks>
        /// Internally, encapsulates access to the
        /// Netapi32.dll NetServerEnum function, performing
        /// the real work of enumerating computers on a
        /// Windows network.
        /// </remarks>
        public struct ComputerInfo
        {
            #region Constants
            private const int ERROR_ACCESS_DENIED = 5;
            private const int ERROR_MORE_DATA = 234;
            #endregion

            #region Fields
            SERVER_INFO_101 m_info;
            #endregion

            #region Constructors

            #region ComputerInfo(SERVER_INFO_101)
            internal ComputerInfo(SERVER_INFO_101 info)
            {
                m_info = info;
            }
            #endregion

            #endregion

            #region Properties

            #region Name
            /// <summary>
            /// Gets the computer name.
            /// </summary>
            /// <value>The computer name.</value>
            public string Name
            {
                get { return m_info.sv101_name; }
            }
            #endregion

            #region Comment
            /// <summary>
            /// Gets the comment associated with the computer.
            /// </summary>
            /// <value>The computer comment.</value>
            public string Comment
            {
                get { return m_info.sv101_comment; }
            }
            #endregion

            #region OsMajorVersion
            /// <summary>
            /// Gets the operating system major version number.
            /// </summary>
            /// <value>operating system major version.</value>
            public int OsMajorVersion
            {
                get { return m_info.sv101_version_major; }
            }
            #endregion

            #region OsMinorVersion
            /// <summary>
            /// Gets the operating system minor version number.
            /// </summary>
            /// <value>operating system minor version.</value>
            public int OsMinorVersion
            {
                get { return m_info.sv101_version_minor; }
            }
            #endregion

            #endregion

            #region Static Utility Methods

            #region GetComputerInfo(unit,string)
            /// <summary>
            /// Gets an array of <c>ComputerInfo</c> structures describing
            /// computers matching the given contraints.
            /// </summary>
            /// <param name="serverType">server type filter.</param>
            /// <param name="domainName">domain to enumerate.</param>
            /// <param name="lastError">The last error.</param>
            /// <returns>
            /// An array of structures representing the computers 
            /// matching the given parameters.
            /// </returns>
            internal static ComputerInfo[] GetComputerInfo(
                uint serverType,
                string domainName, out string lastError)
            {
                int entriesread;  // number of entries actually read
                int totalentries; // total visible servers and workstations
                int result;		  // result of the call to NetServerEnum

                // Pointer to buffer that receives the data
                IntPtr pBuf = IntPtr.Zero;
                Type serverInfoType = typeof(SERVER_INFO_101);

                // structure containing info about the server
                SERVER_INFO_101 serverInfo;
                ComputerInfo[] computers = null;

                try
                {
                    result = NetServerEnum(
                        null,
                        101,
                        out pBuf,
                        -1,
                        out entriesread,
                        out totalentries,
                        serverType,
                        domainName,
                        IntPtr.Zero);

                    if (result == 0)
                    {
                        lastError = string.Empty;

                        computers = new ComputerInfo[entriesread];

                        int tmp = (int)pBuf;

                        for (int i = 0; i < entriesread; i++)
                        {
                            // fill struct
                            serverInfo = (SERVER_INFO_101)Marshal.PtrToStructure((IntPtr)tmp, serverInfoType);
                            computers[i] = new ComputerInfo(serverInfo);

                            // next struct
                            tmp += Marshal.SizeOf(serverInfoType);
                        }
                    }
                    else
                    {
                        switch (result)
                        {
                            case ERROR_MORE_DATA:
                                lastError = "More data is available";
                                break;
                            case ERROR_ACCESS_DENIED:
                                lastError = "Access was denied";
                                break;
                            default:
                                lastError = "Unknown error code " + result;
                                break;
                        }
                    }
                }
                finally
                {
                    // free the buffer
                    NetApiBufferFree(pBuf);
                    pBuf = IntPtr.Zero;
                }

                return computers;
            }
            #endregion

            #endregion

            #region Internal Structures

            #region SERVER_INFO_101
            // Holds computer information
            [StructLayoutAttribute(LayoutKind.Sequential, CharSet = CharSet.Unicode)]
            internal struct SERVER_INFO_101
            {
                public int sv101_platform_id;
                public string sv101_name;
                public int sv101_version_major;
                public int sv101_version_minor;
                public int sv101_type;
                public string sv101_comment;
            }
            #endregion

            #endregion

            #region External Methods

            #region NetServerEnum(string,int,out IntPtr,int,out int,out int,uint,string,IntPtr)
            /// <summary>
            /// enumerates network computers.
            /// </summary>
            /// <param name="servername">The server name; must be null.</param>
            /// <param name="level">The level; 100 or 101</param>
            /// <param name="bufptr">pointer to buffer receiving data.</param>
            /// <param name="prefmaxlen">max length of returned data.</param>
            /// <param name="entriesread">The number of entries read.</param>
            /// <param name="totalentries">total servers + workstations.</param>
            /// <param name="servertype">server rtype filter</param>
            /// <param name="domain">domain to enumerate.</param>
            /// <param name="resume_handle">The resume handle.</param>
            /// <returns></returns>
            [DllImport("Netapi32", CharSet = CharSet.Unicode)]
            private static extern int NetServerEnum(
                string servername,
                int level,
                out IntPtr bufptr,
                int prefmaxlen,
                out int entriesread,
                out int totalentries,
                uint servertype,
                [MarshalAs(UnmanagedType.LPWStr)]
			    string domain,
                IntPtr resume_handle);
            #endregion

            #region NetApiBufferFree(IntPtr)
            /// <summary>
            /// Frees the buffer created by NetServerEnum
            /// </summary>
            /// <param name="buf">The buf.</param>
            /// <returns>result code</returns>
            [DllImport("Netapi32.dll")]
            private extern static int NetApiBufferFree(IntPtr buf);
            #endregion

            #endregion
        } 
        #endregion

        #endregion
    }
}
