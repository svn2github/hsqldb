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
 * Copyright (c) 2001-2004, The HSQL Development Group
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


package org.hsqldb.rowio;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.hsqldb.Binary;
import org.hsqldb.CachedRow;
import org.hsqldb.HsqlException;
import org.hsqldb.JavaObject;
import org.hsqldb.Trace;
import org.hsqldb.Types;
import org.hsqldb.lib.StringConverter;

/**
 *  Provides methods for writing the data for a row to a
 *  byte array. The new format of data consists of mainly binary values
 *  and is not compatible with v.1.6.x databases.
 *
 * @author sqlbob@users (RMP)
 * @author fredt@users
 * @version 1.7.2
 * @since 1.7.0
 */
public class RowOutputBinary extends RowOutputBase {

    private static Method unscaledValueMethod = null;

    static {
        try {
            unscaledValueMethod =
                java.math.BigInteger.class.getMethod("unscaledValue", null);
        } catch (NoSuchMethodException e) {}
        catch (SecurityException e) {}
    }

    int storageSize;

    public RowOutputBinary() {
        super();
    }

    public RowOutputBinary(int initialSize) {
        super(initialSize);
    }

    /**
     *  Constructor used for network transmission of result sets
     *
     * @exception  IOException when an IO error is encountered
     */
    public RowOutputBinary(byte[] buffer) {
        super(buffer);
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

        // fredt - this value is used in 1.7.0 when reading back, for a
        // 'data integrity' check
        // has been removed in 1.7.2 as compatibility is no longer necessary
        // writeInt(pos);
        for (; count < storageSize; ) {
            this.write(0);
        }
    }

    public void writeSize(int size) throws IOException {

        storageSize = size;

        writeInt(size);
    }

    public void writeType(int type) throws IOException {
        writeShort(type);
    }

    public void writeString(String s) throws IOException {

        int temp = count;

        writeInt(0);

        int writecount = StringConverter.writeUTF(s, this);

        writeIntData(count - temp - 4, temp);
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
        int    cols   = row.getTable().getColumnCount();

        return getSize(data, cols, type);
    }

// fredt@users - comment - methods used for writing each SQL type
    protected void writeFieldType(int type) throws IOException {
        write(1);
    }

    protected void writeNull(int type) throws IOException {
        write(0);
    }

    protected void writeChar(String s, int t) throws IOException {
        writeString(s);
    }

    protected void writeSmallint(Number o) throws IOException, HsqlException {
        writeShort(o.intValue());
    }

    protected void writeInteger(Number o) throws IOException, HsqlException {
        writeInt(o.intValue());
    }

    protected void writeBigint(Number o) throws IOException, HsqlException {
        writeLong(o.longValue());
    }

    protected void writeReal(Double o,
                             int type) throws IOException, HsqlException {
        writeLong(Double.doubleToLongBits((o.doubleValue())));
    }

    protected void writeDecimal(BigDecimal o)
    throws IOException, HsqlException {

        int        scale  = o.scale();
        BigInteger bigint = null;

        if (unscaledValueMethod == null) {
            bigint = o.movePointRight(scale).toBigInteger();
        } else {
            try {
                bigint = (BigInteger) unscaledValueMethod.invoke(o, null);
            } catch (java.lang.reflect.InvocationTargetException e) {}
            catch (IllegalAccessException e) {}
        }

        byte[] bytearr = bigint.toByteArray();

        writeByteArray(bytearr);
        writeInt(scale);
    }

    protected void writeBit(Boolean o) throws IOException, HsqlException {
        write(o.booleanValue() ? 1
                               : 0);
    }

    protected void writeDate(java.sql.Date o)
    throws IOException, HsqlException {
        writeLong(o.getTime());
    }

    protected void writeTime(java.sql.Time o)
    throws IOException, HsqlException {
        writeLong(o.getTime());
    }

    protected void writeTimestamp(java.sql.Timestamp o)
    throws IOException, HsqlException {
        writeLong(o.getTime());
        writeInt(o.getNanos());
    }

    protected void writeOther(JavaObject o)
    throws IOException, HsqlException {
        writeByteArray(o.getBytes());
    }

    protected void writeBinary(Binary o,
                               int t) throws IOException, HsqlException {
        writeByteArray(o.getBytes());
    }

// fredt@users - comment - helper and conversion methods
    protected void writeByteArray(byte[] b) throws IOException {
        writeInt(b.length);
        write(b, 0, b.length);
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

            s += 1;    // type or null

            if (o != null) {
                switch (type[i]) {

                    case Types.NULL :
                    case Types.CHAR :
                    case Types.VARCHAR :
                    case Types.VARCHAR_IGNORECASE :
                    case Types.LONGVARCHAR :
                        s += 4;
                        s += StringConverter.getUTFSize((String) o);
                        break;

                    case Types.TINYINT :
                    case Types.SMALLINT :
                        s += 2;
                        break;

                    case Types.INTEGER :
                        s += 4;
                        break;

                    case Types.BIGINT :
                    case Types.REAL :
                    case Types.FLOAT :
                    case Types.DOUBLE :
                        s += 8;
                        break;

                    case Types.NUMERIC :
                    case Types.DECIMAL :
                        s += 8;

                        if (unscaledValueMethod == null) {
                            BigDecimal bigdecimal = (BigDecimal) o;
                            int        scale      = bigdecimal.scale();
                            BigInteger bigint = bigdecimal.movePointRight(
                                scale).toBigInteger();

                            s += bigint.toByteArray().length;
                        } else {
                            try {
                                BigInteger bigint =
                                    (BigInteger) unscaledValueMethod.invoke(o,
                                        null);

                                s += bigint.toByteArray().length;
                            } catch (java.lang.reflect
                                    .InvocationTargetException e) {}
                            catch (IllegalAccessException e) {}
                        }
                        break;

                    case Types.BIT :
                        s += 1;
                        break;

                    case Types.DATE :
                    case Types.TIME :
                        s += 8;
                        break;

                    case Types.TIMESTAMP :
                        s += 12;
                        break;

                    case Types.BINARY :
                    case Types.VARBINARY :
                    case Types.LONGVARBINARY :
                        s += 4;
                        s += ((Binary) o).getBytesLength();
                        break;

                    case Types.OTHER :
                        JavaObject jo = (JavaObject) o;

                        s += 4;
                        s += jo.getBytesLength();
                        break;

                    default :
                        throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED,
                                          Types.getTypeString(type[i]));
                }
            }
        }

        return s;
    }

    /**
     *  Calculate the size of byte array required to store a string in utf8.
     *
     * @param  s - string to convert
     * @return size of the utf8 string
     */
    public void ensureRoom(int extra) {
        super.ensureRoom(extra);
    }

    public void reset(int newSize) {
        super.reset(newSize);
    }

    public void setBuffer(byte[] buffer) {

        buf = buffer;

        reset();
    }
}
