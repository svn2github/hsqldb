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

import java.util.Vector;
import java.util.StringTokenizer;

/** Provides a collection of convenience methods for processing and
 * creating objects with <code>String</code> value components.
 * @version 1.7.0
 * @since HSQLDB 1.7.0
 * @author fredt@users
 * @author boucherb@users.sourceforge.net
 */
public class StringUtil {

    /**
     * Used by {@link #_leftZeroPad _leftZeroPad} to produce, from primitive integral
     * number values, <code>String</code> objects that have the correct collation
     * values for numerical ordering
     */
    private static final char[] CA_ZEROS = {
        '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
        '0', '0', '0', '0', '0', '0'
    };

    /** External construction disabled - this is a pure utility class. */
    private StringUtil() {}

/**
 * Returns a string with non alphanumeric chars converted to the
 * substitute character. A digit first character is also converted.
 * By sqlbob@users
 * @param source string to convert
 * @param substitute character to use
 * @return converted string
 */
    public static String toLowerSubset(String source, char substitute) {

        int          len = source.length();
        StringBuffer src = new StringBuffer(len);
        char         ch;

        for (int i = 0; i < len; i++) {
            ch = source.charAt(i);

            if (!Character.isLetterOrDigit(ch)) {
                src.append(substitute);
            } else if ((i == 0) && Character.isDigit(ch)) {
                src.append(substitute);
            } else {
                src.append(Character.toLowerCase(ch));
            }
        }

        return src.toString();
    }

    /**
     * Builds a CSV list from the specified String[], separator string and
     * quote string. <p>
     *
     * <ul>
     * <li>All arguments are assumed to be non-null.
     * <li>Separates each list element with the value of the
     * <code>separator</code> argument.
     * <li>Prepends and appends each element with the value of the
     *     <code>quote</code> argument.
     * <li> No attempt is made to escape the quote character sequence if it is
     *      found internal to a list element.
     * <ul>
     * @return a CSV list
     * @param separator the <code>String</code> to use as the list element separator
     * @param quote the <code>String</code> with which to quote the list elements
     * @param s array of <code>String</code> objects
     */
    public static String getList(String[] s, String separator, String quote) {

        StringBuffer b = new StringBuffer(s.length * 16);

        for (int i = 0; i < s.length; i++) {
            b.append(quote);
            b.append(s[i]);
            b.append(quote);
            b.append(separator);
        }

        b.setLength(b.length() - separator.length());

        return b.toString();
    }

    /**
     * Builds a CSV list from the specified int[], <code>separator</code>
     * <code>String</code> and <code>quote</code> <code>String</code>. <p>
     *
     * <ul>
     * <li>All arguments are assumed to be non-null.
     * <li>Separates each list element with the value of the
     * <code>separator</code> argument.
     * <li>Prepends and appends each element with the value of the
     *     <code>quote</code> argument.
     * <ul>
     * @return a CSV list
     * @param s the array of int values
     * @param separator the <code>String</code> to use as the separator
     * @param quote the <code>String</code> with which to quote the list elements
     */
    public static String getList(int[] s, String separator, String quote) {

        StringBuffer b = new StringBuffer(s.length * 8);

        for (int i = 0; i < s.length; i++) {
            b.append(quote);
            b.append(s[i]);
            b.append(quote);
            b.append(separator);
        }

        b.setLength(b.length() - separator.length());

        return b.toString();
    }

    /**
     * Builds a CSV list from the specified String[][], separator string and
     * quote string. <p>
     *
     * <ul>
     * <li>All arguments are assumed to be non-null.
     * <li>Uses only the first element in each subarray.
     * <li>Separates each list element with the value of the
     * <code>separator</code> argument.
     * <li>Prepends and appends each element with the value of the
     *     <code>quote</code> argument.
     * <li> No attempt is made to escape the quote character sequence if it is
     *      found internal to a list element.
     * <ul>
     * @return a CSV list
     * @param separator the <code>String</code> to use as the list element separator
     * @param quote the <code>String</code> with which to quote the list elements
     * @param s the array of <code>String</code> array objects
     */
    public static String getList(String[][] s, String separator,
                                 String quote) {

        StringBuffer b = new StringBuffer(s.length * 16);

        for (int i = 0; i < s.length; i++) {
            b.append(quote);
            b.append(s[i][0]);
            b.append(quote);
            b.append(separator);
        }

        b.setLength(b.length() - separator.length());

        return b.toString();
    }

    /**
     * Retrieves an array of <code>String</code> objects from the argument
     * <code>s</code>. <p>
     *
     * The elements of the retreived array are, in order, all of
     * the tokens extracted from the argument <code>s</code> using a
     * <code>java.util.StringTokenizer</code> constructed thus: <p>
     *
     * <code>new StringTokenizer(s,",",false);</code> <p>
     *
     * This method is useful for things such as converting CSV lists to
     * the form required to invoke the special
     * <code>public static void main(String[] args)</code> method of a class.
     * @param s A list of comma-separated values to convert
     *    to a <code>String</code> array
     * @return an array of <code>String</code> objects that are, in order,
     *    all of the tokens extracted from the argument
     *    <code>s</code> using a <code>java.util.StringTokenizer</code>
     *    constructed thus: <p>
     *
     *    <code>new StringTokenizer(s,",",false)</code>
     */
    public static String[] csvToArray(String s) {

        if (s == null) {
            return new String[]{};
        }

        StringTokenizer st    = new StringTokenizer(s, ",", false);
        int             count = st.countTokens();
        String[]        out   = new String[count];

        for (int i = 0; i < count; i++) {
            out[i] = st.nextToken();
        }

        return out;
    }

    /**
     * Retrieves a <code>String</code> of <code>maxDigits</code> characters
     * where the right-most characters are the digits of the specified value
     * argument and the remaining left-most characters, if any, are the
     * zero (&quot;0&quot;) character.  <p>
     *
     * This method provides the functionality common to all public leftZeroPad method
     * signatures.  This functionality exists to allow creating <code>String</code>
     * objects that have a collation order similar to there numeric counterparts.
     *
     * It is assumed that <code>maxDigits</code> is no smaller than the number
     * of digits required to represent the specified value argument.
     * @param value the value to left zero pad
     * @param maxDigits the length of the resulting string
     * @return a <code>String</code> of <code>maxDigits</code>
     *        characters where the right-most characters
     *        are the digits of the specified value argument
     *        and the remaining left-most characters, if any,
     *        are the zero (&quot;0&quot;) character.
     */
    private static String _leftZeroPad(long value, int maxDigits) {

        String sValue       = String.valueOf(value);
        int    sValueLength = sValue.length();
        int    zeroPad      = (maxDigits - sValueLength);
        char[] padded       = new char[maxDigits];

        System.arraycopy(CA_ZEROS, 0, padded, 0, zeroPad);
        sValue.getChars(0, sValueLength, padded, zeroPad);

        return new String(padded);
    }

    /**
     * Retrieves a 19 character <code>String</code> representation of the specified
     * long value where the left-most characters not used to represent the value
     * are set to the &quote;0&quote; character.
     * @param value the value for which to retrieve the left zero
     *    padded <code>String</code> representation.
     * @return a 19 character <code>String</code> representation of the specified
     * long value where the left-most characters not used to represent the value
     * are set to the &quote;0&quote; character.
     */
    public static String leftZeroPad(long value) {
        return _leftZeroPad(value, 19);
    }

    /**
     * Retrieves a 10 character <code>String</code> representation of the specified
     * int value where the left-most characters not used to represent the value
     * are set to the &quote;0&quote; character.
     * @param value the value for which to retrieve the left zero
     *    padded <code>String</code> representation.
     * @return a 10 character <code>String</code> representation of the specified
     * int value where the left-most characters not used to represent the value
     * are set to the &quote;0&quote; character.
     */
    public static String leftZeroPad(int value) {
        return _leftZeroPad(value, 10);
    }

    /**
     * Retrieves a 5 character <code>String</code> representation of the specified
     * short value where the left-most characters not used to represent the value
     * are set to the &quote;0&quote; character.
     * @param value the value for which to retrieve the left zero
     *    padded <code>String</code> representation.
     * @return a 5 character <code>String</code> representation of the specified
     * short value where the left-most characters not used to represent the value
     * are set to the &quote;0&quote; character.
     */
    public static String leftZeroPad(short value) {
        return _leftZeroPad(value, 5);
    }

    /**
     * Retrieves a 3 character <code>String</code> representation of the specified
     * byte value where the left-most characters not used to represent the value
     * are set to the &quote;0&quote; character.
     * @param value the value for which to retrieve the left zero
     *    padded <code>String</code> representation.
     * @return a 3 character <code>String</code> representation of the specified
     * byte value where the left-most characters not used to represent the value
     * are set to the &quote;0&quote; character.
     */
    public static String leftZeroPad(byte value) {
        return _leftZeroPad(value, 3);
    }
}
