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
 * @version 1.7.2
 */
public class StringUtil {

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
     * Builds a generalised separated list from the String[].
     *
     * @param s array of string elements of the list
     * @param separator string to use as separator
     * @param quote string to use for quoting
     * @return a separated list
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
     * Builds a CSV list from the int[].
     *
     * @param i array of int
     * @return a CSV list
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
     * Builds a CSV list from the specified String[][].
     * Uses only the first element in each subarray.
     *
     *
     * @param s the array of array of String objects
     * @return a CSV list
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
}
