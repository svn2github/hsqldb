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

/**
 * A asynchronous string buffer for use in the hsqldb package. The buffer
 * maintains an empty slot and the beginning and end to allow for the quick
 * addition of single quotation marks around the string.
 *
 * @author  dnordahl@users
 */
public final class HsqlStringBuffer {

    private static Reporter reporter = new Reporter();

    private static class Reporter {

        private static int initCounter         = 0;
        private static int updateCounter       = 0;
        private static int charAppendCounter   = 0;
        private static int stringAppendCounter = 0;
        private static int wasteCounter        = 0;
        private static int toStringCounter     = 0;

        Reporter() {

            try {
                System.runFinalizersOnExit(true);
            } catch (SecurityException e) {}
        }

        protected void finalize() {

            System.out.println("HsqlStringBuffer init count: " + initCounter);
            System.out.println("HsqlStringBuffer update count: "
                               + updateCounter);
            System.out.println("HsqlStringBuffer append(char) count: "
                               + charAppendCounter);
            System.out.println("HsqlStringBuffer append(String) count: "
                               + stringAppendCounter);
            System.out.println("HsqlStringBuffer waste count: "
                               + wasteCounter);
            System.out.println("HsqlStringBuffer toString count: "
                               + toStringCounter);
        }
    }

//    private int traceUpdated = 0;
/*
// fredt - this was used with TestSelf - there doesn't seem to be more than a handful of unused instances
    protected void finalize(){
        // trivial test
        if ( traceUpdated == 0 ){
            reporter.wasteCounter++;
        }
    }
*/

    // The offsets give an extra slot at the begining and end so single quotes
    // can be efficiently added.
    private final static int   BEGINNING_OFFSET         = 1;
    private final static int   END_OFFSET               = 1;
    private final static int   DEFAULT_INITIAL_CAPACITY = 16;
    private final static float DEFAULT_RESIZE_FACTOR    = 2.0f;
    private char[]             data;
    private int                charCount;

    /** Creates a new instance of HsqlStringBuffer */
    public HsqlStringBuffer() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    /** Creates a new instance of HsqlStringBuffer with the initial length provided */
    public HsqlStringBuffer(int length) {

//        reporter.initCounter ++;
        if (length < 0) {
            throw new NegativeArraySizeException("Invalid length passed");
        }

        if (length == 0) {    // this prevents the capacity increase amount being

            // multiplied by zero
            data = new char[BEGINNING_OFFSET + 1 + END_OFFSET];
        } else {
            data = new char[BEGINNING_OFFSET + length + END_OFFSET];
        }

        charCount = 0;
    }

    /** Creates a new instance of HsqlStringBuffer starting with the initial string provided */
    public HsqlStringBuffer(String string) {

//        reporter.initCounter ++;
        data = new char[BEGINNING_OFFSET + string.length() + DEFAULT_INITIAL_CAPACITY + END_OFFSET];
        charCount = 0;

//        traceUpdated--;
        append(string);
    }

    /** Appends a char to the end of the string buffer */
    public HsqlStringBuffer append(char ch) {

//        reporter.charAppendCounter++;
        makeRoom(charCount, 1);

        data[BEGINNING_OFFSET + charCount] = ch;

        charCount++;

        return this;
    }

    /** Appends a String to the end of the string buffer */
    public HsqlStringBuffer append(String string) {

//        reporter.stringAppendCounter++;
        int len = string.length();

        makeRoom(charCount, len);
        string.getChars(0, len, data, charCount + BEGINNING_OFFSET);

        charCount += len;

        return this;
    }

    /** Appends another HsqlStringBuffer to the end of the string buffer */
    public HsqlStringBuffer append(HsqlStringBuffer sBuf) {

        makeRoom(charCount, sBuf.charCount);
        System.arraycopy(sBuf.data, BEGINNING_OFFSET, data,
                         BEGINNING_OFFSET + charCount, sBuf.charCount);

        charCount += sBuf.charCount;

        return this;
    }

    /** Inserts a character at the given position */
    public HsqlStringBuffer insert(int pos, char ch) {

        checkInsertPosition(pos);
        makeRoom(pos, 1);

        data[pos + BEGINNING_OFFSET] = ch;

        charCount++;

        return this;
    }

    /** Insert a String into the current string buffer */
    public HsqlStringBuffer insert(int pos, String string) {

        checkInsertPosition(pos);

        int len = string.length();

        makeRoom(pos, len);
        string.getChars(0, len, data, BEGINNING_OFFSET + pos);

        charCount += len;

        return this;
    }

    /** Insert another HsqlStringBuffer into the current string buffer */
/*
    public HsqlStringBuffer insert(int pos, HsqlStringBuffer sBuf) {

        checkInsertPosition(pos);
        makeRoom(pos, sBuf.charCount);
        System.arraycopy(sBuf.data, BEGINNING_OFFSET, data,
                         BEGINNING_OFFSET + pos, sBuf.charCount);

        charCount += sBuf.charCount;

        return this;
    }
*/
    /**
     * Gives the contents of the string buffer as a String without being
     * encapsulated in single quotes
     */
    public String toString() {

        reporter.toStringCounter++;

        return new String(data, BEGINNING_OFFSET, charCount);
    }

    /**
     * Returns the contents of the string buffer in single quotes.
     */
/*
    public String toQuotedString() {

        data[0] = data[charCount + BEGINNING_OFFSET] = '\'';

        String str = new String(data, 0,
                                charCount + BEGINNING_OFFSET + END_OFFSET);

        data[0] = data[charCount + BEGINNING_OFFSET] = '\0';

        return str;
    }
*/
    /**
     * Returns false if the object being compared is not a HsqlStringBuffer.
     */
/*
    public boolean equals(Object obj) {

        if (!(obj instanceof HsqlStringBuffer)) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        HsqlStringBuffer hsb = (HsqlStringBuffer) obj;

        if (hsb.charCount != this.charCount) {
            return false;
        } else {
            for (int i = 0; i < this.charCount; i++) {
                if (hsb.data[i] != this.data[i]) {
                    return false;
                }
            }

            return true;
        }
    }
*/
    /**
     * Compares the current HsqlStringBuffer to another returning results consistent with
     * String.compareTo
     */
    public int compareTo(HsqlStringBuffer hsb) {

        if (this == hsb) {
            return 0;
        }

        int stop = Math.min(this.charCount, hsb.charCount) + BEGINNING_OFFSET;

        for (int i = BEGINNING_OFFSET; i < stop; i++) {
            if (this.data[i] != hsb.data[i]) {
                return this.data[i] - hsb.data[i];
            }
        }

        return this.charCount - hsb.charCount;
    }

    /**
     * Compares the current HsqlStringBuffer to a String returning results consistent with
     * String.compareTo
     */
    public int compareTo(String str) {

        char[] strData = str.toCharArray();
        int stop = Math.min(this.charCount, strData.length)
                   + BEGINNING_OFFSET;

        for (int i = BEGINNING_OFFSET; i < stop; i++) {
            if (this.data[i] != strData[i - BEGINNING_OFFSET]) {
                return this.data[i] - strData[i - BEGINNING_OFFSET];
            }
        }

        return this.charCount - strData.length;
    }

    /**
     * Allows HsqlStringBuffer to be used by data structures requiring implementation of
     * Comparable such as maps.
     */
/*
    public int compareTo(Object obj) {

        if (obj instanceof HsqlStringBuffer) {
            return compareTo((HsqlStringBuffer) obj);
        } else if (obj instanceof String) {
            return compareTo((String) obj);
        } else {
            throw new ClassCastException("Unsupported comparison attempted");
        }
    }
*/
    /**
     * Returns the length of the buffer not including the empty slots for the
     * optional single quotes.
     */
    public final int length() {
        return charCount;
    }

    /**
     * Sets the length of the string buffer, cutting off any characters at
     * positions greater than or equal to the new length.
     */
    public void setLength(int length) {

        if (length < 0) {
            throw new StringIndexOutOfBoundsException(
                "Length given is less than zero: " + length);
        }

        if (length > charCount) {
            makeRoom(charCount, length - charCount);

            while (charCount < length) {
                data[charCount + BEGINNING_OFFSET] = '\0';

                charCount++;
            }
        } else {
            charCount = length;
        }
    }

    /**
     * Makes sure a valid position was passed in for the insert operation.
     * @throws IndexOutOfBoundsException if the position is found to be invalid
     */
    private void checkInsertPosition(int pos) {

        if (pos > charCount) {
            throw new IndexOutOfBoundsException("Invalid position passed: "
                                                + pos + ">" + charCount);
        }

        if (pos < 0) {
            throw new IndexOutOfBoundsException("Invalid position passed: "
                                                + pos + "<0");
        }
    }

    /**
     * Checks to see if the capacity of the array needs increasing, increases
     * the capacity if necessary, and creates an offset for prepend and insert
     * operations. The pos variable shouldn't include offset.
     */
    private void makeRoom(int pos, int addSize) {

//        traceUpdated++;
//        reporter.updateCounter ++;
        boolean insert = false;

        if (pos != charCount) {    // an offset is not added because both variables are in 'user space'
            insert = true;
        }

        // data array is too small
        if (BEGINNING_OFFSET + addSize + charCount + END_OFFSET
                >= data.length) {
            int    newCapacity = (int) (data.length * DEFAULT_RESIZE_FACTOR);
            char[] newData;

            // test if the predicted new size increase will accommodate the size
            // of whats being added
            if (newCapacity
                    <= BEGINNING_OFFSET + charCount + addSize + END_OFFSET) {
                newData = new char[newCapacity + addSize];
            } else {
                newData = new char[newCapacity];
            }

            int stop = charCount + BEGINNING_OFFSET;

            // If the operation is an insert op, make room for the new stuff
            // while we're at it
            if (insert) {
                stop -= pos;

                System.arraycopy(data, pos, newData,
                                 pos + addSize - 1 + BEGINNING_OFFSET, stop);

                if (pos != 0) {    // For an insert not at the beginning, the data

                    // before the insert pos needs to be xfered also
                    System.arraycopy(data, BEGINNING_OFFSET, newData,
                                     BEGINNING_OFFSET,
                                     pos - 1 + BEGINNING_OFFSET);
                }
            } else {
                System.arraycopy(data, 0, newData, 0, stop);
            }

            data    = newData;
            newData = null;
        } else if (insert) {       // A resize was not needed - but for insert,

            // stuff needs to be shifted over
            int stop = BEGINNING_OFFSET + pos + addSize;

            for (int i = charCount - 1 + addSize + BEGINNING_OFFSET;
                    i >= stop; i--) {
                data[i] = data[i - addSize];
            }
        }
    }

    public HsqlStringBuffer append(int i) {
        return append(Integer.toString(i));
    }

    public HsqlStringBuffer append(long l) {
        return append(Long.toString(l));
    }
/*
    public HsqlStringBuffer append(double db) {
        return append(Double.toString(db));
    }

    public HsqlStringBuffer append(float fl) {
        return append(Float.toString(fl));
    }

    public HsqlStringBuffer append(short sh) {
        return append(Short.toString(sh));
    }

    public HsqlStringBuffer append(boolean b) {
        return append(b ? "true"
                        : "false");
    }

    public HsqlStringBuffer insert(int pos, int i) {
        return insert(pos, Integer.toString(i));
    }

    public HsqlStringBuffer insert(int pos, long l) {
        return insert(pos, Long.toString(l));
    }

    public HsqlStringBuffer insert(int pos, double db) {
        return insert(pos, Double.toString(db));
    }

    public HsqlStringBuffer insert(int pos, float fl) {
        return insert(pos, Float.toString(fl));
    }

    public HsqlStringBuffer insert(int pos, short sh) {
        return insert(pos, Short.toString(sh));
    }

    public HsqlStringBuffer insert(int pos, boolean b) {
        return insert(pos, b ? "true"
                             : "false");
    }
*/
//fredt@users temp methods - no tests - no error checks or proper interface and throw check against StringBuffer
    public char charAt(int pos) {
        return data[pos + BEGINNING_OFFSET];
    }

    public void setCharAt(int pos, char ch) {
        data[pos + BEGINNING_OFFSET] = ch;
    }

    public HsqlStringBuffer reverse() {

        for (int i = 0; i < this.charCount / 2; i++) {
            char temp = this.data[BEGINNING_OFFSET + i];

            data[BEGINNING_OFFSET + i] =
                data[BEGINNING_OFFSET + charCount - i - 1];
            data[BEGINNING_OFFSET + charCount - i - 1] = temp;
        }

        return this;
    }

// fredt - no check etc.

    /** Creates a new instance of HsqlStringBuffer from part of another */
/*
    public HsqlStringBuffer(char[] ch, int pos, int len) {

        reporter.initCounter++;

        data = new char[BEGINNING_OFFSET + len + DEFAULT_INITIAL_CAPACITY + END_OFFSET];
        charCount = 0;

        append(ch, pos, len);
    }
*/
    public HsqlStringBuffer append(char[] str, int offset, int len) {

        makeRoom(charCount, len);
        System.arraycopy(str, offset, data, BEGINNING_OFFSET + charCount,
                         len);

        charCount += len;

        return this;
    }
}
