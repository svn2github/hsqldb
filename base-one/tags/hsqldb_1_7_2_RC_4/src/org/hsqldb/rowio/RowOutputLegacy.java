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

import java.io.DataOutputStream;
import java.io.IOException;

import org.hsqldb.Binary;
import org.hsqldb.CachedRow;
import org.hsqldb.HsqlException;
import org.hsqldb.JavaObject;
import org.hsqldb.Types;
import org.hsqldb.lib.StringConverter;

/**
 *  Provides methods for writing the data for a row to a byte array. The
 *  format of data is that used for storage of cached tables by v.1.6.x
 *  databases.
 * @author sqlbob@users (RMP)
 * @author fredt@users
 * @version 1.7.2
 * @since 1.7.0
 */
public class RowOutputLegacy extends RowOutputBase {

    int              storageSize;
    DataOutputStream dout;

    /**
     *  Constructor used for persistent storage of a Table row
     *
     * @param  size no of bytes of storage used
     * @exception  IOException when an IO error is encountered
     */
    public RowOutputLegacy() {

        super();

        dout = new DataOutputStream(this);
    }

// fredt@users - comment - methods for writing column type, name and data size
    public void writeIntData(int i) throws IOException {
        writeInt(i);
    }

    public void writeIntData(int i, int position) throws IOException {

        int temp = count;

        count = position;

        writeInt(i);

        if (count < temp) {
            count = temp;
        }
    }

    public void writePos(int pos) throws IOException {

        writeInt(pos);

        for (; count < storageSize; ) {
            this.write(0);
        }
    }

    public void writeSize(int size) throws IOException {

        storageSize = size;

        writeInt(size);
    }

    public void writeType(int type) throws IOException {
        writeInt(type);
    }

    public void writeString(String s) throws IOException {
        dout.writeUTF(s);
    }

// fredt@users - comment - methods used for writing each SQL type
    protected void writeFieldType(int type) throws IOException {
        writeInt(type);
    }

    protected void writeNull(int type) throws IOException {
        writeType(Types.NULL);
    }

    protected void writeChar(String s, int t) throws IOException {
        dout.writeUTF(s);
    }

    //fredt: REAL, TINYINT and SMALLINT are written in the old format
    // for compatibility
    protected void writeSmallint(Number o) throws IOException, HsqlException {
        writeString(o.toString());
    }

    protected void writeInteger(Number o) throws IOException, HsqlException {
        writeInt(o.intValue());
    }

    protected void writeBigint(Number o) throws IOException, HsqlException {
        writeString(o.toString());
    }

    protected void writeReal(Double o,
                             int type) throws IOException, HsqlException {

        if (type == Types.REAL) {
            writeString(o.toString());
        } else {

            // some JDKs have a problem with this:
            // out.writeDouble(((Double)o).doubleValue());
            writeLong(Double.doubleToLongBits(o.doubleValue()));
        }
    }

    protected void writeDecimal(java.math.BigDecimal o)
    throws IOException, HsqlException {
        writeString(o.toString());
    }

    protected void writeBit(Boolean o) throws IOException, HsqlException {
        writeString(o.toString());
    }

    protected void writeDate(java.sql.Date o)
    throws IOException, HsqlException {
        writeString(o.toString());
    }

    protected void writeTime(java.sql.Time o)
    throws IOException, HsqlException {
        writeString(o.toString());
    }

    protected void writeTimestamp(java.sql.Timestamp o)
    throws IOException, HsqlException {
        writeString(o.toString());
    }

    protected void writeOther(JavaObject o)
    throws IOException, HsqlException {

        byte[] ba = o.getBytes();

        writeByteArray(ba);
    }

    protected void writeBinary(Binary o,
                               int t) throws IOException, HsqlException {
        writeByteArray(o.getBytes());
    }

// fredt@users - comment - helper and conversion methods
    protected void writeByteArray(byte b[]) throws IOException {

        writeString("**");    //new format flag
        writeInt(b.length);
        write(b, 0, b.length);
    }

    /**
     *  Calculate the size of byte array required to store a row.
     *
     * @param  row - a database row
     * @return  size of byte array
     * @exception  HsqlException When data is inconsistent
     */
    public int getSize(CachedRow row) throws HsqlException {

        Object data[] = row.getData();
        int    type[] = row.getTable().getColumnTypes();

        return getSize(data, data.length, type);
    }

    /**
     *  Calculate the size of byte array required to store a row.
     *
     * @param  data - the row data
     * @param  l - number of data[] elements to include in calculation
     * @param  type - array of java.sql.Types values
     * @return size of byte array
     * @exception  HsqlException when data is inconsistent
     */
    private static int getSize(Object data[], int l,
                               int type[]) throws HsqlException {

        int s = 0;

        for (int i = 0; i < l; i++) {
            Object o = data[i];

            s += 4;                // type

            if (o != null) {
                switch (type[i]) {

                    case Types.INTEGER :
                        s += 4;
                        break;

                    case Types.FLOAT :
                    case Types.DOUBLE :
                        s += 8;
                        break;

                    case Types.BINARY :
                    case Types.VARBINARY :
                    case Types.LONGVARBINARY :
                        s += 2;    //new format flag "**"
                        s += 4;    //length
                        s += ((Binary) o).getBytesLength();
                        break;

                    case Types.OTHER :
                        s += 2;    //new format flag "**"
                        s += 4;    //length
                        s += ((JavaObject) o).getBytesLength();
                        break;

                    default :
                        s += 2;    //length
                        s += StringConverter.getUTFSize(o.toString());
                }
            }
        }

        return s;
    }
}
