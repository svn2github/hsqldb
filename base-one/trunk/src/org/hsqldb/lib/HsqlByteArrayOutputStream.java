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


package org.hsqldb.lib;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.DataOutput;

/**
 * This class is both a replacement for java.io.ByteArrayOuputStream
 * without synchronization and a partial implementation of java.io.DataOutput
 * interface.
 */
public class HsqlByteArrayOutputStream extends java.io.OutputStream {

    protected byte buf[];
    protected int  count;

    public HsqlByteArrayOutputStream() {
        this(32);
    }

    public HsqlByteArrayOutputStream(int size) {
        buf = new byte[size];
    }

// methods that implement dataOutput
    public final void writeShort(int v) {

        ensureRoom(2);

        buf[count++] = (byte) (v >>> 8);
        buf[count++] = (byte) v;
    }

    public final void writeInt(int v) {

        ensureRoom(4);

        buf[count++] = (byte) (v >>> 24);
        buf[count++] = (byte) (v >>> 16);
        buf[count++] = (byte) (v >>> 8);
        buf[count++] = (byte) v;
    }

    public final void writeLong(long v) {
        writeInt((int) (v >>> 32));
        writeInt((int) v);
    }

    public final void writeBytes(String s) {

        int len = s.length();

        for (int i = 0; i < len; i++) {
            write((byte) s.charAt(i));
        }
    }

    public final void writeFloat(float v) {
        writeInt(Float.floatToIntBits(v));
    }

    public final void writeDouble(double v) {
        writeLong(Double.doubleToLongBits(v));
    }

// methods that extend java.io.OutputStream
    public void write(int b) {

        ensureRoom(1);

        buf[count] = (byte) b;

        count++;
    }

    public void write(byte b[], int off, int len) {

        ensureRoom(len);
        System.arraycopy(b, off, buf, count, len);

        count += len;
    }

    public void writeTo(OutputStream out) throws IOException {
        out.write(buf, 0, count);
    }

    public void reset() {
        count = 0;
    }

    public byte toByteArray()[] {

        byte newbuf[] = new byte[count];

        System.arraycopy(buf, 0, newbuf, 0, count);

        return newbuf;
    }

    public int size() {
        return count;
    }

    public String toString() {
        return new String(buf, 0, count);
    }

    public String toString(String enc) throws UnsupportedEncodingException {
        return new String(buf, 0, count, enc);
    }

    public String toString(int hibyte) {
        return new String(buf, hibyte, 0, count);
    }

    public void close() throws IOException {}

    private void ensureRoom(int extra) {

        int newcount = count + extra;

        if (newcount > buf.length) {
            byte newbuf[] = new byte[newcount];

            System.arraycopy(buf, 0, newbuf, 0, count);

            buf = newbuf;
        }
    }
}
