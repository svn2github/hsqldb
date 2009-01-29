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

using System.Collections;
using System.Data;
using System.Data.Common;
using System.Data.Hsqldb.Common.IO;
using System.Data.Hsqldb.Common.Enumeration;
using System.Data.Hsqldb.Client.MetaData;
using System.Data.Hsqldb.Common.Sql.Type;
using System.Data.SqlTypes;
using System.IO;
using System.Runtime.Serialization;
using System.Security;
using System.Text;
using System.Xml;

using Trace = org.hsqldb.Trace;
using System.Data.Hsqldb.Common;

#endregion

namespace System.Data.Hsqldb.Client
{
    #region HsqlDataReader

    /// <summary>
    /// <para>
    /// The HSQLDB <see cref="DbDataReader">DbDataReader</see> implementation.
    /// </para>
    /// <img src="/ClassDiagrams/System.Data.Hsqldb.Client.HsqlDataReader.png"
    ///      alt="HsqlDataReader Class Diagram"/>
    /// </summary>
    /// <remarks>
    /// <para>
    /// Provides a means of reading one or more forward-only result sets
    /// obtained by executing a command at a data source.
    /// </para>
    /// <para>
    /// Changes made to a result set by another process or thread while data
    /// is being read may be visible to the user. However, the precise
    /// behavior is both provider and timing dependent.
    /// </para>
    /// </remarks>
    /// <author name="boucherb@users"/>
    public partial class HsqlDataReader : DbDataReader
    {
        #region Public Constructors

        #region HsqlDataReader(Result)
        /// <summary>
        /// Constructs a new <c>HsqlDataReader</c>
        /// instance encapsulating the given result.
        /// </summary>
        /// <param name="result">
        /// The root <c>Result</c> object.
        /// </param>
        /// <remarks>
        /// This constructor allows a programatically built
        /// (i.e. "detached") <c>Result</c> to be presented
        /// as a data reader without the requirement of having
        /// an originating command or connection.
        /// </remarks>
        [CLSCompliant(false)]
        public HsqlDataReader(org.hsqldb.Result result)
        {
            if (result == null)
            {
                throw new ArgumentNullException("result");
            }
            else if (result.isError())
            {
                throw new HsqlDataSourceException(result);
            }
            else if (result.isUpdateCount())
            {
                m_recordsAffected = result.getUpdateCount();
            }
            else if (result.isData())
            {
                m_recordsAffected = -1;
                m_result = result;
                m_fieldCount = result.getColumnCount();
                m_metaData = result.metaData;
                m_columnTypes = m_metaData.colTypes;
            }
            else
            {
                throw new InvalidOperationException(
                    "Unhandled Result Mode: " + result.mode);
            }
        } 
        #endregion

        #region HsqlDataReader(Result,HsqlCommand,CommandBehaviour,bool,HsqlConnection)
        /// <summary>
        /// Constructs a new <c>HsqlDataReader</c> instance encapsulating the
        /// given <c>org.hsqldb.Result</c>.
        /// </summary>
        /// <param name="result">
        /// The root <c>org.hsqldb.Result</c> object.
        /// </param>
        /// <param name="commandBehavior">
        /// The <c>CommandBehavior</c>, as would be specified
        /// to a generating
        /// <see cref="HsqlCommand.ExecuteReader(CommandBehavior)"/>
        /// method.
        /// </param>        
        /// <param name="originatingCommand">
        /// The originating command.
        /// </param>     
        /// <param name="originatingConnection">
        /// The originating connection.
        /// </param>
        [CLSCompliant(false)]
        public HsqlDataReader(
            org.hsqldb.Result result,
            CommandBehavior commandBehavior,
            HsqlCommand originatingCommand,
            HsqlConnection originatingConnection)
            : this(result)
        {
            m_commandBehavior = commandBehavior;
            m_originatingCommand = originatingCommand;
            m_originatingConnection = originatingConnection;
        } 
        #endregion

        #region HsqlDataReader(int[])
        /// <summary>
        /// Constructs a new <c>HsqlDataReader</c>
        /// instance encapsulating the given counts.
        /// </summary>
        /// <param name="recordsAffectedCounts">
        /// The counts.
        /// </param>
        /// <exception cref="ArgumentNullException">
        /// When <c>recordsAffectedCounts</c> is <c>null</c>.
        /// </exception>
        /// <exception cref="ArgumentException">
        /// When <c>recordsAffectedCounts.Length</c> is less than one.
        /// </exception>
        public HsqlDataReader(int[] recordsAffectedCounts)
        {
            if (recordsAffectedCounts == null)
            {
                throw new ArgumentNullException(
                    "recordsAffectedCounts");
            }

            if (recordsAffectedCounts.Length < 1)
            {
                throw new ArgumentException(
                    "recordsAffectedCounts.Length < 1");
            }

            m_recordsAffectedCounts = (int[]) recordsAffectedCounts.Clone();
            m_recordsAffected = recordsAffectedCounts[0];
        } 
        #endregion

        #endregion

        #region Static Utility Methods
        
        #region ToByteArray(HsqlDataReader)
        /// <summary>
        /// Converts all results encapsulated by the given 
        /// <see cref="HsqlDataReader"/> to a snapshot form
        /// suitable for long term persistence or network
        /// transmission.
        /// </summary>
        /// <remarks>
        /// The returned <c>System.Byte[]</c> can be converted
        /// back into a data reader using <see cref="FromByteArray(Byte[])"/>.
        /// </remarks>
        /// <param name="reader">
        /// To convert to a <c>Byte[]</c> value.
        /// </param>
        /// <returns>
        /// A new <c>System.Byte[]</c>
        /// </returns>
        public static byte[] ToByteArray(HsqlDataReader reader)
        {
            using (MemoryStream stream = new MemoryStream())
            {
                WriteToStream(stream, reader);

                return stream.ToArray();
            }
        } 
        #endregion

        #region FromByteArray(byte[])
        /// <summary>
        /// Converts the given <c>System.Byte[]</c> to a 
        /// <see cref="HsqlDataReader"/>.
        /// </summary>
        /// <remarks>
        /// The conversion proceeds by first wrapping the
        /// given <c>System.Byte[]</c> in a <b>MemoryStream</b>
        /// and then delegating to 
        /// <see cref="ReadFromStream(Stream)"/>.
        /// </remarks>
        /// <param name="bytes">
        /// To convert to a <c>HsqlDataReader</c>.</param>
        /// <returns>
        /// A new <c>HsqlDataReader</c>.
        /// </returns>
        public static HsqlDataReader FromByteArray(byte[] bytes)
        {
            using (MemoryStream stream = new MemoryStream(bytes, false))
            {
                return ReadFromStream(stream);
            }
        } 
        #endregion

        #region WriteToStream(Stream, HsqlDataReader)
        /// <summary>
        /// Writes all results encapsulated by the given 
        /// <see cref="HsqlDataReader"/> to a snapshot form
        /// suitable for long term persistence or network
        /// transmission.
        /// </summary>
        /// <param name="stream">The stream.</param>
        /// <param name="reader">The reader.</param>
        public static void WriteToStream(Stream stream, HsqlDataReader reader)
        {
            WriteResult(stream, reader.m_result);
        } 
        #endregion

        #region ReadFromStream(Stream)
        /// <summary>
        /// Reads an <c>HsqlDataReader</c> from a stream.
        /// </summary>
        /// <remarks>
        /// The content of the given <c>Stream</c> must conform to the 
        /// HSQLDB 1.8.0.7 <c>org.hsqldb.Result</c> transmission
        /// protocol. For details, follow the source starting at
        /// <b>org.hsqldb.Result.write</b>.
        /// </remarks>
        /// <param name="stream">The stream.</param>
        /// <returns>An <c>HsqlDataReader</c>.</returns>
        public static HsqlDataReader ReadFromStream(Stream stream)
        {
            return new HsqlDataReader(ReadResult(stream));
        }
        #endregion

        #region ReadResult(Stream)
        /// <summary>
        /// Reads a <c>Result</c> from a <c>Stream</c>.
        /// </summary>
        /// <remarks>
        /// The content of the given <c>Stream</c> must conform to the 
        /// HSQLDB 1.8.0.7 <c>org.hsqldb.Result</c> transmission
        /// protocol. For details, follow the source starting at
        /// <b>org.hsqldb.Result.write</b>.
        /// </remarks>
        /// <param name="stream">The stream.</param>
        /// <returns>The <c>Result</c>.</returns>
        [CLSCompliant(false)]
        public static org.hsqldb.Result ReadResult(Stream stream)
        {
            org.hsqldb.rowio.RowInputBinary rowInput
                = new org.hsqldb.rowio.RowInputBinary();
            java.io.DataInput dataInput
                = new java.io.DataInputStream(
                    new JavaInputStreamAdapter(stream));

            return org.hsqldb.Result.read(rowInput, dataInput);
        } 
        #endregion

        #region WriteResult(Stream, Result)
        /// <summary>
        /// Writes a <c>Result</c> to a <c>Stream</c> using
        /// the HSQLDB 1.8.0.7 <c>org.hsqldb.Result</c> serialization
        /// protocol.
        /// </summary>
        /// <param name="stream">The stream.</param>
        /// <param name="result">The result.</param>
        [CLSCompliant(false)]
        public static void WriteResult(Stream stream, org.hsqldb.Result result)
        {
            JavaOutputStreamAdapter outputStream
                = new JavaOutputStreamAdapter(stream);
            org.hsqldb.rowio.RowOutputBinary rowOutput
                = new org.hsqldb.rowio.RowOutputBinary();

            org.hsqldb.Result.write(
                result,
                rowOutput,
                outputStream);
        }
        #endregion
        
       #endregion
    }

    #endregion
}