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


package org.hsqldb.rowio;

import java.io.DataInputStream;
import java.io.IOException;

import org.hsqldb.Binary;
import org.hsqldb.Column;
import org.hsqldb.HsqlException;
import org.hsqldb.JavaObject;
import org.hsqldb.Types;
import org.hsqldb.store.ValuePool;

/**
 *  Provides methods for reading the data for a row from a
 *  byte array. The format of data is that used for storage of cached
 *  tables by v.1.6.x databases.
 *
 * @author sqlbob@users (RMP)
 * @author fredt@users
 * @version 1.7.2
 * @since 1.7.0
 */
public class RowInputLegacy extends RowInputBase
implements org.hsqldb.rowio.RowInputInterface {

    public RowInputLegacy() throws IOException {
        super();
    }

    public int readType() throws IOException {
        return readInt();
    }

    public int readIntData() throws IOException {
        return readInt();
    }

    public String readString() throws IOException {
        return DataInputStream.readUTF(this);
    }

    private String readNumericString() throws IOException {
        return (readString());
    }

    byte[] readByteArray() throws IOException {

        byte[] b = new byte[readInt()];

        readFully(b);

        return b;
    }

    protected boolean checkNull() throws IOException {
        return readInt() == 0 ? true
                              : false;
    }

    protected String readChar(int type) throws IOException {
        return readUTF();
    }

    protected Integer readSmallint() throws IOException, HsqlException {

        int val = Integer.parseInt(readNumericString());

        return ValuePool.getInt(val);
    }

    protected Integer readInteger() throws IOException, HsqlException {
        return ValuePool.getInt(readInt());
    }

    protected Long readBigint() throws IOException, HsqlException {

//        return Long.valueOf(readNumericString());
        long l = Long.parseLong(readNumericString());

        return ValuePool.getLong(l);
    }

    protected Double readReal(int type) throws IOException, HsqlException {

        if (type == Types.REAL) {

//            return Double.valueOf(readNumericString());
//            double d = Double.parseDouble(readNumericString());
            double d = new Double(readNumericString()).doubleValue();
            long   l = Double.doubleToLongBits(d);

            return ValuePool.getDouble(l);

            // some JDKs have a problem with this:
            // o=new Double(readDouble());
        } else {

//            return new Double(Double.longBitsToDouble(readLong()));
            long l = readLong();

            return ValuePool.getDouble(l);
        }
    }

    protected java.math.BigDecimal readDecimal()
    throws IOException, HsqlException {
        return ValuePool.getBigDecimal(
            new java.math.BigDecimal(readNumericString()));
    }

    protected Boolean readBit() throws IOException, HsqlException {
        return org.hsqldb.lib.BooleanConverter.getBoolean(readString());
    }

/** @todo fredt - get time and data longs then normalise before fetching value */
    protected java.sql.Time readTime() throws IOException, HsqlException {
        return java.sql.Time.valueOf(readString());
    }

    protected java.sql.Date readDate() throws IOException, HsqlException {
        return java.sql.Date.valueOf(readString());
    }

    protected java.sql.Timestamp readTimestamp()
    throws IOException, HsqlException {
        return java.sql.Timestamp.valueOf(readString());
    }

    protected Object readOther() throws IOException, HsqlException {

// fredt@users 20020328 -  patch 482109 by fredt - OBJECT handling
// objects are / were stored as serialized byte[]
// now they are deserialized before retrieval
        byte[] data;
        String binarystring = readString();

        if (binarystring.equals("**")) {

            // hsql - new format
            data = readByteArray();
        } else {

            // hsql - old format
            data = Column.hexToByteArray(binarystring);
        }

        return new JavaObject(data, true);
    }

    protected Binary readBinary(int type) throws IOException, HsqlException {

        String hexstring = readString();

        if (hexstring.equals("**")) {

            // hsql - new format
            return new Binary(readByteArray(), true);
        } else {

            // hsql - old format
            return new Binary(Column.hexToByteArray(hexstring));
        }
    }
}
