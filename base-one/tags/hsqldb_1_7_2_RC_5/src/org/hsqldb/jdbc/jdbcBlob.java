/* Copyright (c) 2001-2004, The HSQL Development Group
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


package org.hsqldb.jdbc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;

/**
 * @author james house jhouse@part.net
 * @version 1.7.2
 * @since 1.7.2
 */
public class jdbcBlob implements Blob {

    private byte[] blobData;

    public jdbcBlob(byte[] data) {
        this.blobData = data;
    }

    /**
     * Returns blob data as a stream
     */
    public InputStream getBinaryStream() throws SQLException {
        return new ByteArrayInputStream(blobData);
    }

    /**
     * Returns blob data as an array of bytes
     */
    public byte[] getBytes(long pos, int length) throws SQLException {

        byte[] newData = new byte[length];

        System.arraycopy(blobData, (int) (pos - 1), newData, 0, length);

        return newData;
    }

    /**
     * Returns the length of the blob data
     */
    public long length() throws SQLException {
        return this.blobData.length;
    }

    public long position(Blob pattern, long start) throws SQLException {
        throw jdbcUtil.notSupported;
    }

    public long position(byte[] pattern, long start) throws SQLException {
        throw jdbcUtil.notSupported;
    }

    public int setBytes(long pos, byte[] bytes) throws SQLException {
        throw jdbcUtil.notSupported;
    }

    public int setBytes(long pos, byte[] bytes, int offset,
                        int len) throws SQLException {
        throw jdbcUtil.notSupported;
    }

    public OutputStream setBinaryStream(long pos) throws SQLException {
        throw jdbcUtil.notSupported;
    }

    public void truncate(long len) throws SQLException {
        throw jdbcUtil.notSupported;
    }
}
