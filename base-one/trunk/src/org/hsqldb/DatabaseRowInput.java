/* Copyrights and Licenses
 *
 * This product includes Hypersonic SQL.
 * Originally developed by Thomas Mueller and the Hypersonic SQL Group. 
 *
 * Copyright (c) 1995-2000 by the Hypersonic SQL Group. All rights reserved. 
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met: 
 *     -  Redistributions of source code must retain the above copyright notice, this list of conditions
 *         and the following disclaimer. 
 *     -  Redistributions in binary form must reproduce the above copyright notice, this list of
 *         conditions and the following disclaimer in the documentation and/or other materials
 *         provided with the distribution. 
 *     -  All advertising materials mentioning features or use of this software must display the
 *        following acknowledgment: "This product includes Hypersonic SQL." 
 *     -  Products derived from this software may not be called "Hypersonic SQL" nor may
 *        "Hypersonic SQL" appear in their names without prior written permission of the
 *         Hypersonic SQL Group. 
 *     -  Redistributions of any form whatsoever must retain the following acknowledgment: "This
 *          product includes Hypersonic SQL." 
 * This software is provided "as is" and any expressed or implied warranties, including, but
 * not limited to, the implied warranties of merchantability and fitness for a particular purpose are
 * disclaimed. In no event shall the Hypersonic SQL Group or its contributors be liable for any
 * direct, indirect, incidental, special, exemplary, or consequential damages (including, but
 * not limited to, procurement of substitute goods or services; loss of use, data, or profits;
 * or business interruption). However caused any on any theory of liability, whether in contract,
 * strict liability, or tort (including negligence or otherwise) arising in any way out of the use of this
 * software, even if advised of the possibility of such damage. 
 * This software consists of voluntary contributions made by many individuals on behalf of the
 * Hypersonic SQL Group.
 *
 *
 * For work added by the HSQL Development Group:
 *
 * Copyright (c) 2001-2002, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer, including earlier
 * license statements (above) and comply with all above license conditions.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution, including earlier
 * license statements (above) and comply with all above license conditions.
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


package org.hsqldb;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Base class for reading the data for a database row in different formats.
 * Defines the methods that are independent of storage format and declares
 * the format-dependent methods that subclasses should define.
 *
 * @author sqlbob@users (RMP)
 * @author fredt@users
 * @version 1.7.0
 */
abstract class DatabaseRowInput extends ByteArrayInputStream
implements DataInput {

    static final int NO_POS  = -1;
    protected int    filePos = NO_POS;
    protected int    nextPos = NO_POS;
    protected int    size;

    // the last column is a SYSTEM_ID that has to be created at read time
    protected boolean makeSystemId = false;

    public DatabaseRowInput() {
        this(new byte[4]);
    }

    /**
     * Constructor takes a complete row
     */
    public DatabaseRowInput(byte[] buf) {

        super(buf);

        size = buf.length;
    }

    public int getPos() throws IOException {

        if (filePos == NO_POS) {
            throw (new IOException("No position specified"));
        }

        return (filePos);
    }

    public int getNextPos() throws IOException {

        if (nextPos == NO_POS) {
            throw (new IOException("No next position specified"));
        }

        return (nextPos);
    }

    public int getSize() {
        return size;
    }

// fredt@users - comment - methods used for node and type data
    public abstract int readIntData() throws IOException;

    public abstract int readType() throws IOException;

    public abstract String readString() throws IOException;

// fredt@users - comment - methods used for SQL types
    protected abstract boolean checkNull() throws IOException;

    protected abstract String readChar(int type)
    throws IOException, SQLException;

    protected abstract Integer readSmallint()
    throws IOException, SQLException;

    protected abstract Integer readInteger() throws IOException, SQLException;

    protected abstract Long readBigint() throws IOException, SQLException;

    protected abstract Double readReal(int type)
    throws IOException, SQLException;

    protected abstract BigDecimal readDecimal()
    throws IOException, SQLException;

    protected abstract Boolean readBit() throws IOException, SQLException;

    protected abstract java.sql.Time readTime()
    throws IOException, SQLException;

    protected abstract java.sql.Date readDate()
    throws IOException, SQLException;

    protected abstract java.sql.Timestamp readTimestamp()
    throws IOException, SQLException;

    protected abstract Object readOther() throws IOException, SQLException;

    protected abstract byte[] readBinary(int type)
    throws IOException, SQLException;

// fredt@users - comment - methods used for reading java primitive types
    public final void readFully(byte b[]) throws IOException {
        readFully(b, 0, b.length);
    }

    public final void readFully(byte b[], int off,
                                int len) throws IOException {

        if (len < 0) {
            throw new IndexOutOfBoundsException();
        }

        int n = 0;

        while (n < len) {
            int count = read(b, off + n, len - n);

            if (count < 0) {
                throw new EOFException();
            }

            n += count;
        }
    }

    public final boolean readBoolean() throws IOException {

        int ch = read();

        if (ch < 0) {
            throw new EOFException();
        }

        return (ch != 0);
    }

    public final byte readByte() throws IOException {

        int ch = read();

        if (ch < 0) {
            throw new EOFException();
        }

        return (byte) ch;
    }

    public final int readUnsignedByte() throws IOException {

        int ch = read();

        if (ch < 0) {
            throw new EOFException();
        }

        return ch;
    }

    public final short readShort() throws IOException {

        int ch1 = read();
        int ch2 = read();

        if ((ch1 | ch2) < 0) {
            throw new EOFException();
        }

        return (short) ((ch1 << 8) + (ch2 << 0));
    }

    public final int readUnsignedShort() throws IOException {

        int ch1 = read();
        int ch2 = read();

        if ((ch1 | ch2) < 0) {
            throw new EOFException();
        }

        return (ch1 << 8) + (ch2 << 0);
    }

    public final char readChar() throws IOException {

        int ch1 = read();
        int ch2 = read();

        if ((ch1 | ch2) < 0) {
            throw new EOFException();
        }

        return (char) ((ch1 << 8) + (ch2 << 0));
    }

    public final int readInt() throws IOException {

        int ch1 = read();
        int ch2 = read();
        int ch3 = read();
        int ch4 = read();

        if ((ch1 | ch2 | ch3 | ch4) < 0) {
            throw new EOFException();
        }

        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }

    public final long readLong() throws IOException {
        return ((long) (readInt()) << 32) + (readInt() & 0xFFFFFFFFL);
    }

    public final float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    public final double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    /**
     *  reads row data from a stream using the JDBC types in colTypes
     *
     * @param  colTypes currently only the length is used
     * @return
     * @throws  IOException
     * @throws  SQLException
     */
    public Object[] readData(int[] colTypes)
    throws IOException, SQLException {

        int    l      = colTypes.length;
        Object data[] = new Object[l];

        if (makeSystemId) {
            l--;
        }

        Object o;
        int    type;

        for (int i = 0; i < l; i++) {
            if (checkNull()) {

//                data[i] = null;
                continue;
            }

            o    = null;
            type = colTypes[i];

            switch (type) {

                case Types.CHAR :
                case Types.VARCHAR :
                case Column.VARCHAR_IGNORECASE :
                case Types.LONGVARCHAR :
                    o = readChar(type);
                    break;

                case Types.TINYINT :
                case Types.SMALLINT :
                    o = readSmallint();
                    break;

                case Types.INTEGER :
                    o = readInteger();
                    break;

                case Types.BIGINT :
                    o = readBigint();
                    break;

                //fredt although REAL is now Double, it is read / written in
                //the old format for compatibility
                case Types.REAL :
                case Types.FLOAT :
                case Types.DOUBLE :
                    o = readReal(type);
                    break;

                case Types.NUMERIC :
                case Types.DECIMAL :
                    o = readDecimal();
                    break;

                case Types.DATE :
                    o = readDate();
                    break;

                case Types.TIME :
                    o = readTime();
                    break;

                case Types.TIMESTAMP :
                    o = readTimestamp();
                    break;

                case Types.BIT :
                    o = readBit();
                    break;

                case Types.OTHER :
                    o = readOther();
                    break;

                case Types.BINARY :
                case Types.VARBINARY :
                case Types.LONGVARBINARY :
                    o = readBinary(type);
                    break;

                default :
                    throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED, type);
            }

            data[i] = o;
        }

        if (makeSystemId) {
            data[l] = new Integer(getPos());
        }

        return data;
    }

    /**
     *  Used to reset the row, ready for a new row to be read into the byte[]
     *
     */
    public void resetRow(int filepos, int rowsize) throws IOException {

        reset();

        if (buf.length < rowsize) {
            buf = new byte[rowsize];
        }

        filePos = filepos;
        size    = count = rowsize;
        pos     = 4;
        buf[0]  = (byte) ((rowsize >>> 24) & 0xFF);
        buf[1]  = (byte) ((rowsize >>> 16) & 0xFF);
        buf[2]  = (byte) ((rowsize >>> 8) & 0xFF);
        buf[3]  = (byte) ((rowsize >>> 0) & 0xFF);
    }

    public byte[] getBuffer() {
        return buf;
    }

    public int skipBytes(int n) throws IOException {
        throw new java.lang.UnsupportedOperationException(
            "Method skipBytes() not yet implemented.");
    }

    public String readLine() throws IOException {
        throw new java.lang.UnsupportedOperationException(
            "Method readLine() not yet implemented.");
    }

    public String readUTF() throws IOException {
        throw new java.lang.UnsupportedOperationException(
            "Method readUTF() not yet implemented.");
    }
}
