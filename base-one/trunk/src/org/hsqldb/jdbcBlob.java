package org.hsqldb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.sql.*;

/**
 * @author james house jhouse@part.net
 */
public class jdbcBlob implements Blob {

    byte[] blobData = null;

    jdbcBlob(Object data) {

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream    out  = new ObjectOutputStream(baos);

            out.writeObject(data);
            out.flush();

            this.blobData = baos.toByteArray();
        } catch (IOException io) {
            io.printStackTrace();
        }
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
        throw new SQLException("Not implemented");
    }

    public long position(byte[] pattern, long start) throws SQLException {
        throw new SQLException("Not implemented");
    }

    public int setBytes(long pos, byte[] bytes) throws SQLException {
        throw new SQLException("Not implemented");
    }

    public int setBytes(long pos, byte[] bytes, int offset,
                        int len) throws SQLException {
        throw new SQLException("Not implemented");
    }

    public OutputStream setBinaryStream(long pos) throws SQLException {
        throw new SQLException("Not implemented");
    }

    public void truncate(long len) throws SQLException {
        throw new SQLException("Not implemented");
    }
}
