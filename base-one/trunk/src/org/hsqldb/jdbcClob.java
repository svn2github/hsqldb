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


package org.hsqldb;

import java.sql.*;
import java.io.StringReader;
import org.hsqldb.lib.AsciiStringInputStream;

/**
 * Provides methods for getting the length of an SQL CLOB (Character Large
 * Object) value, for materializing a CLOB value on the client, and for
 * searching for a substring or CLOB object within a CLOB value.
 *
 * @author  boucherb@users
 * @version 1.7.2
 * @since 1.7.2
 */
public class jdbcClob implements Clob {

    private String data;

    public jdbcClob(String datum) throws SQLException {
        this.data = datum;
    }

    /**
     * Retrieves the <code>CLOB</code> value designated by this
     * <code>Clob</code> object as an ascii stream.
     *
     * @return a <code>java.io.InputStream</code> object containing the
     *         <code>CLOB</code> data
     * @exception SQLException if there is an error accessing the
     *            <code>CLOB</code> value
     * @see #setAsciiStream
     * @since 1.2
     *
     */
    public java.io.InputStream getAsciiStream() throws SQLException {
        return new AsciiStringInputStream(data);
    }

    /**
     * Retrieves the <code>CLOB</code> value designated by this
     * <code>Clob</code> object as a <code>java.io.Reader</code> object
     * (or as a stream of characters).
     *
     * @return a <code>java.io.Reader</code> object containing the
     *         <code>CLOB</code> data
     * @exception SQLException if there is an error accessing the
     *            <code>CLOB</code> value
     * @see #setCharacterStream
     * @since 1.2
     *
     */
    public java.io.Reader getCharacterStream() throws SQLException {
        return new StringReader(data);
    }

    /**
     * Retrieves a copy of the specified substring
     * in the <code>CLOB</code> value
     * designated by this <code>Clob</code> object.
     * The substring begins at position
     * <code>pos</code> and has up to <code>length</code> consecutive
     * characters.
     *
     * @param pos the first character of the substring to be extracted.
     *            The first character is at position 1.
     * @param length the number of consecutive characters to be copied
     * @return a <code>String</code> that is the specified substring in
     *         the <code>CLOB</code> value designated by this
     *         <code>Clob</code> object
     * @exception SQLException if there is an error accessing the
     *            <code>CLOB</code> value
     * @since 1.2
     *
     */
    public String getSubString(long pos, int length) throws SQLException {

        pos--;

        try {
            Trace.check(pos >= 0 && pos <= data.length(),
                        Trace.INVALID_JDBC_ARGUMENT, "pos: " + (pos + 1L));
            Trace.check(length >= 0 && length <= Integer.MAX_VALUE,
                        Trace.INVALID_JDBC_ARGUMENT, "length: " + length);

            int end = (int) pos + length;

            Trace.check(end <= data.length(), Trace.INVALID_JDBC_ARGUMENT,
                        "length: " + length);

            return data.substring((int) pos, end);
        } catch (HsqlException he) {
            throw jdbcDriver.sqlException(he);
        } catch (Throwable t) {
            throw jdbcDriver.sqlException(new HsqlException(new Result(t,
                    null)));
        }
    }

    /**
     * Retrieves the character position at which the specified substring
     * <code>searchstr</code> appears in the SQL <code>CLOB</code> value
     * represented by this <code>Clob</code> object.  The search
     * begins at position <code>start</code>.
     *
     * @param searchstr the substring for which to search
     * @param start the position at which to begin searching; the
     *          first position is 1
     * @return the position at which the substring appears or -1 if it is not
     *          present; the first position is 1
     * @exception SQLException if there is an error accessing the
     *          <code>CLOB</code> value
     * @since 1.2
     *
     */
    public long position(String searchstr, long start) throws SQLException {

        start--;

        try {
            Trace.check(start >= 0 && start <= data.length(),
                        Trace.INVALID_JDBC_ARGUMENT,
                        "start: " + (start + 1L));

            int pos = data.indexOf(searchstr, (int) start);

            return pos >= 0 ? pos + 1
                            : -1;
        } catch (HsqlException he) {
            throw jdbcDriver.sqlException(he);
        } catch (Throwable t) {
            throw jdbcDriver.sqlException(new HsqlException(new Result(t,
                    null)));
        }
    }

    /**
     * Retrieves the character position at which the specified
     * <code>Clob</code> object <code>searchstr</code> appears in this
     * <code>Clob</code> object.  The search begins at position
     * <code>start</code>.
     *
     * @param searchstr the <code>Clob</code> object for which to search
     * @param start the position at which to begin searching; the first
     *              position is 1
     * @return the position at which the <code>Clob</code> object appears
     *              or -1 if it is not present; the first position is 1
     * @exception SQLException if there is an error accessing the
     *            <code>CLOB</code> value
     * @since 1.2
     *
     */
    public long position(Clob searchstr, long start) throws SQLException {

        start--;

        try {
            Trace.check(start >= 0 && start <= Integer.MAX_VALUE,
                        Trace.INVALID_JDBC_ARGUMENT, "start: " + start);

            long sslen = searchstr.length();
            long dslen = data.length();

// This is potentially much less expensive than materializing a large
// substring from some other vendor's CLOB.  Indeed, we should probably
// do the comparison piecewise, using in-memory lists (or temp-files
// when available), if it is detected that the input CLOB is very long.
            if ((start + sslen) > dslen) {
                return -1;
            }

            // Avoid wrap-around and potential aioobe on cast to int
            Trace.check(sslen <= Integer.MAX_VALUE,
                        Trace.INVALID_JDBC_ARGUMENT,
                        "searchstr.length(): " + sslen + " > "
                        + Integer.MAX_VALUE);

            String s   = searchstr.getSubString(1L, (int) sslen);
            int    pos = data.indexOf(s, (int) start);

            return pos >= 0 ? pos + 1
                            : -1;
        } catch (SQLException e) {
            throw e;
        } catch (HsqlException he) {
            throw jdbcDriver.sqlException(he);
        } catch (Throwable t) {
            throw jdbcDriver.sqlException(new HsqlException(new Result(t,
                    null)));
        }
    }

    /**
     * Retrieves a stream to be used to write Ascii characters to the
     * <code>CLOB</code> value that this <code>Clob</code> object represents,
     * starting at position <code>pos</code>.
     *
     * @param pos the position at which to start writing to this
     *        <code>CLOB</code> object
     * @return the stream to which ASCII encoded characters can be written
     * @exception SQLException if there is an error accessing the
     *            <code>CLOB</code> value
     * @see #getAsciiStream
     *
     * @since 1.4
     *
     */
    public java.io.OutputStream setAsciiStream(long pos) throws SQLException {
        throw jdbcDriver.notSupported;
    }

    /**
     * Retrieves a stream to be used to write a stream of Unicode characters
     * to the <code>CLOB</code> value that this <code>Clob</code> object
     * represents, at position <code>pos</code>.
     *
     * @param  pos the position at which to start writing to the
     *        <code>CLOB</code> value
     *
     * @return a stream to which Unicode encoded characters can be written
     * @exception SQLException if there is an error accessing the
     *            <code>CLOB</code> value
     * @see #getCharacterStream
     *
     * @since 1.4
     *
     */
    public java.io.Writer setCharacterStream(long pos) throws SQLException {
        throw jdbcDriver.notSupported;
    }

    /**
     * Writes the given Java <code>String</code> to the <code>CLOB</code>
     * value that this <code>Clob</code> object designates at the position
     * <code>pos</code>.
     *
     * @param pos the position at which to start writing to the
     *          <code>CLOB</code> value that this <code>Clob</code> object
     *          represents
     * @param str the string to be written to the <code>CLOB</code>
     *          value that this <code>Clob</code> designates
     * @return the number of characters written
     * @exception SQLException if there is an error accessing the
     *            <code>CLOB</code> value
     *
     * @since 1.4
     *
     */
    public int setString(long pos, String str) throws SQLException {
        throw jdbcDriver.notSupported;
    }

    /**
     * Writes <code>len</code> characters of <code>str</code>, starting
     * at character <code>offset</code>, to the <code>CLOB</code> value
     * that this <code>Clob</code> represents.
     *
     * @param pos the position at which to start writing to this
     *          <code>CLOB</code> object
     * @param str the string to be written to the <code>CLOB</code>
     *          value that this <code>Clob</code> object represents
     * @param offset the offset into <code>str</code> to start reading
     *          the characters to be written
     * @param len the number of characters to be written
     * @return the number of characters written
     * @exception SQLException if there is an error accessing the
     *          <code>CLOB</code> value
     *
     * @since 1.4
     *
     */
    public int setString(long pos, String str, int offset,
                         int len) throws SQLException {
        throw jdbcDriver.notSupported;
    }

    /**
     * Retrieves the number of characters
     * in the <code>CLOB</code> value
     * designated by this <code>Clob</code> object.
     *
     * @return length of the <code>CLOB</code> in characters
     * @exception SQLException if there is an error accessing the
     *            length of the <code>CLOB</code> value
     * @since 1.2
     *
     */
    public long length() throws SQLException {
        return data.length();
    }

    /**
     * Truncates the <code>CLOB</code> value that this <code>Clob</code>
     * designates to have a length of <code>len</code>
     * characters.
     * @param len the length, in bytes, to which the <code>CLOB</code> value
     *        should be truncated
     * @exception SQLException if there is an error accessing the
     *            <code>CLOB</code> value
     *
     * @since 1.4
     *
     */
    public void truncate(long len) throws SQLException {

        try {
            Trace.check(len >= 0, Trace.INVALID_JDBC_ARGUMENT, "len: " + len);

            len = len >> 1;

            Trace.check(len <= data.length(), Trace.INVALID_JDBC_ARGUMENT,
                        "len: " + len);

            if (len == data.length()) {

                // nothing has changed, so there's no point
                // in making a copy
            } else {
                data = data.substring(0, (int) len);
            }
        } catch (HsqlException he) {
            throw jdbcDriver.sqlException(he);
        } catch (Throwable t) {
            throw jdbcDriver.sqlException(new HsqlException(new Result(t,
                    null)));
        }
    }
}
