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

import java.sql.*;
import java.io.*;
import org.hsqldb.lib.*;

/**
 *
 * @author  boucherb@users
 * @version 1.7.2
 * @since 1.7.2
 */
public class jdbcClob implements java.sql.Clob {

    String data;
    static final SQLException notSupported =
        Trace.error(Trace.FUNCTION_NOT_SUPPORTED);

    jdbcClob(String datum) throws SQLException {
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

        try {
            return data.substring((int) pos, length);
        } catch (Exception e) {
            throw Trace.error(Trace.GENERAL_ERROR, e.getMessage());
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
     * @return he position at which the substring appears or -1 if it is not
     *          present; the first position is 1
     * @exception SQLException if there is an error accessing the
     *          <code>CLOB</code> value
     * @since 1.2
     *
     */
    public long position(String searchstr, long start) throws SQLException {

        try {
            return data.indexOf(searchstr, (int) start);
        } catch (Exception e) {
            throw Trace.error(Trace.GENERAL_ERROR, e.getMessage());
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

        try {
            return position(
                searchstr.getSubString(0L, (int) searchstr.length()), start);
        } catch (Exception e) {
            throw Trace.error(Trace.GENERAL_ERROR, e.getMessage());
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
        throw notSupported;
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
        throw notSupported;
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
        throw notSupported;
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
        throw notSupported;
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
            data = data.substring(0, (int) len);
        } catch (Exception e) {
            throw Trace.error(Trace.GENERAL_ERROR, e.getMessage());
        }
    }
}
