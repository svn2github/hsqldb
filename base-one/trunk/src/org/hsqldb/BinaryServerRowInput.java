/* Copyright (c) 2001-2002, The HSQL Development Group
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


package org.hsqldb;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.hsqldb.lib.StringConverter;

/**
 *  Provides methods for reading the data for a row from a
 *  byte array. The format of data is that used for storage of cached
 *  tables by v.1.6.x databases, apart from strings.
 *
 * @version  1.7.0
 */
class BinaryServerRowInput extends org.hsqldb.DatabaseRowInput
implements org.hsqldb.DatabaseRowInputInterface {

    BinaryServerRowOutput out;

    public BinaryServerRowInput() {
        super();
    }

    public BinaryServerRowInput(byte buf[]) {
        super(buf);
    }

    /**
     * uses the byte[] buffer from out. At each reset, the buffer is set
     * to the current one for out.
     */

    public BinaryServerRowInput(BinaryServerRowOutput out) {
        super(out.getBuffer());
        this.out = out;
    }

    byte[] readByteArray() throws IOException {

        byte[] b = new byte[readInt()];

        readFully(b);

        return b;
    }

    public int readType() throws IOException {
        return readShort();
    }

    public int readIntData() throws IOException {
        return readInt();
    }

    public String readString() throws IOException {

        int    length = readInt();
        String s      = StringConverter.readUTF(buf, pos, length);

// fredt - memory opt tests
        s   = org.hsqldb.store.ValuePool.getString(s);
        pos += length;

        return s;
    }

    protected boolean checkNull() throws IOException {

        int b = readByte();

        return b == 0 ? true
                      : false;
    }

    protected String readChar(int type) throws IOException {
        return readString();
    }

    protected Integer readSmallint() throws IOException, HsqlException {

// fredt - memory opt tests
//        return new Integer(readShort());
        return org.hsqldb.store.ValuePool.getInt(readShort());
    }

    protected Integer readInteger() throws IOException, HsqlException {

// fredt - memory opt tests
//        return new Integer(readInt());
        return org.hsqldb.store.ValuePool.getInt(readInt());
    }

    protected Long readBigint() throws IOException, HsqlException {

// fredt - memory opt tests
//        return new Long(readLong());
        return org.hsqldb.store.ValuePool.getLong(readLong());
    }

    protected Double readReal(int type) throws IOException, HsqlException {

// fredt - memory opt tests
//        return new Double(Double.longBitsToDouble(readLong()));
        return org.hsqldb.store.ValuePool.getDouble(readLong());
    }

    protected BigDecimal readDecimal() throws IOException, HsqlException {

        byte[]     bytes  = readByteArray();
        int        scale  = readInt();
        BigInteger bigint = new BigInteger(bytes);

// fredt - memory opt tests
//        return new BigDecimal(bigint, scale);
        return org.hsqldb.store.ValuePool.getBigDecimal(new BigDecimal(bigint,
                scale));
    }

    protected Boolean readBit() throws IOException, HsqlException {
        return readBoolean() ? Boolean.TRUE
                             : Boolean.FALSE;
    }

    protected java.sql.Time readTime() throws IOException, HsqlException {
        return new java.sql.Time(readLong());
    }

    protected java.sql.Date readDate() throws IOException, HsqlException {

// fredt - memory opt tests
//        return new java.sql.Date(readLong());
        return org.hsqldb.store.ValuePool.getDate(readLong());
    }

    protected java.sql.Timestamp readTimestamp()
    throws IOException, HsqlException {
        return HsqlDateTime.timestampValue(readLong(), readInt());
    }

    protected Object readOther() throws IOException, HsqlException {

// fredt@users 20020328 -  patch 482109 by fredt - OBJECT handling
// objects are / were stored as serialized byte[]
// now they are deserialized before retrieval
        byte[] o = readByteArray();

        return Column.deserialize(o);
    }

    protected byte[] readBinary(int type) throws IOException, HsqlException {
        return readByteArray();
    }

        /**
     *  Used to reset the row, ready for a new row to be written into the
     *  byte[] buffer by an external routine.
     *
     */
    public void resetRow(int filepos, int rowsize) throws IOException {

        if (out != null ){
            out.reset();
            out.ensureRoom(rowsize);
        }
        super.resetRow(filepos,rowsize);
    }


}
