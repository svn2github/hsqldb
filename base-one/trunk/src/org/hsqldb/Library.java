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
 * Copyright (c) 2001-2002, The HSQL Development Group
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


package org.hsqldb;

import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.NoSuchElementException;
import java.util.Random;
import java.text.SimpleDateFormat;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.HashMap;
import org.hsqldb.store.ValuePool;
import org.hsqldb.lib.IntValueHashMap;
import java.sql.Date;
import java.sql.Time;
import org.hsqldb.store.ValuePool;

// fredt@users 20020210 - patch 513005 by sqlbob@users (RMP) - ABS function
// fredt@users 20020305 - patch 1.7.0 - change to 2D string arrays
// sqlbob@users 20020420- patch 1.7.0 - added HEXTORAW and RAWTOHEX.
// boucherb@user 20020918 - doc 1.7.2 - added JavaDoc  and code comments
// fredt@user 20021021 - doc 1.7.2 - modified JavaDoc
// boucherb@users 20030201 - patch 1.7.2 - direct calls for org.hsqldb.Library

/**
 * Provides the HSQLDB implementation of standard Open Group SQL CLI
 * <em>Extended Scalar Functions</em> and other public HSQLDB SQL functions.
 *
 * @version 1.7.2
 */
public class Library {

    static final String sNumeric[][] = {
        {
            "ABS", "org.hsqldb.Library.abs"
        }, {
            "ACOS", "java.lang.Math.acos"
        }, {
            "ASIN", "java.lang.Math.asin"
        }, {
            "ATAN", "java.lang.Math.atan"
        }, {
            "ATAN2", "java.lang.Math.atan2"
        }, {
            "CEILING", "java.lang.Math.ceil"
        }, {
            "COS", "java.lang.Math.cos"
        }, {
            "COT", "org.hsqldb.Library.cot"
        }, {
            "DEGREES", "java.lang.Math.toDegrees"
        }, {
            "EXP", "java.lang.Math.exp"
        }, {
            "FLOOR", "java.lang.Math.floor"
        }, {
            "LOG", "java.lang.Math.log"
        }, {
            "LOG10", "org.hsqldb.Library.log10"
        }, {
            "MOD", "org.hsqldb.Library.mod"
        }, {
            "PI", "org.hsqldb.Library.pi"
        }, {
            "POWER", "java.lang.Math.pow"
        }, {
            "RADIANS", "java.lang.Math.toRadians"
        }, {
            "RAND", "java.lang.Math.random"
        }, {
            "ROUND", "org.hsqldb.Library.round"
        }, {
            "SIGN", "org.hsqldb.Library.sign"
        }, {
            "SIN", "java.lang.Math.sin"
        }, {
            "SQRT", "java.lang.Math.sqrt"
        }, {
            "TAN", "java.lang.Math.tan"
        }, {
            "TRUNCATE", "org.hsqldb.Library.truncate"
        }, {
            "BITAND", "org.hsqldb.Library.bitand"
        }, {
            "BITOR", "org.hsqldb.Library.bitor"
        }, {
            "ROUNDMAGIC", "org.hsqldb.Library.roundMagic"
        }
    };

// fredt@users 20010701 - patch 418023 by deforest@users
// the definition for SUBSTR was added
    static final String sString[][]   = {
        {
            "ASCII", "org.hsqldb.Library.ascii"
        }, {
            "CHAR", "org.hsqldb.Library.character"
        }, {
            "CONCAT", "org.hsqldb.Library.concat"
        }, {
            "DIFFERENCE", "org.hsqldb.Library.difference"
        }, {
            "HEXTORAW", "org.hsqldb.Library.hexToRaw"
        }, {
            "INSERT", "org.hsqldb.Library.insert"
        }, {
            "LCASE", "org.hsqldb.Library.lcase"
        }, {
            "LEFT", "org.hsqldb.Library.left"
        }, {
            "LENGTH", "org.hsqldb.Library.length"
        }, {
            "LOCATE", "org.hsqldb.Library.locate"
        }, {
            "LTRIM", "org.hsqldb.Library.ltrim"
        }, {
            "RAWTOHEX", "org.hsqldb.Library.rawToHex"
        }, {
            "REPEAT", "org.hsqldb.Library.repeat"
        }, {
            "REPLACE", "org.hsqldb.Library.replace"
        }, {
            "RIGHT", "org.hsqldb.Library.right"
        }, {
            "RTRIM", "org.hsqldb.Library.rtrim"
        }, {
            "SOUNDEX", "org.hsqldb.Library.soundex"
        }, {
            "SPACE", "org.hsqldb.Library.space"
        }, {
            "SUBSTR", "org.hsqldb.Library.substring"
        }, {
            "SUBSTRING", "org.hsqldb.Library.substring"
        }, {
            "UCASE", "org.hsqldb.Library.ucase"
        }, {
            "LOWER", "org.hsqldb.Library.lcase"
        }, {
            "UPPER", "org.hsqldb.Library.ucase"
        }
    };
    static final String sTimeDate[][] = {
        {
            "CURDATE", "org.hsqldb.Library.curdate"
        }, {
            "CURTIME", "org.hsqldb.Library.curtime"
        }, {
            "DAYNAME", "org.hsqldb.Library.dayname"
        }, {
            "DAYOFMONTH", "org.hsqldb.Library.dayofmonth"
        }, {
            "DAYOFWEEK", "org.hsqldb.Library.dayofweek"
        }, {
            "DAYOFYEAR", "org.hsqldb.Library.dayofyear"
        }, {
            "HOUR", "org.hsqldb.Library.hour"
        }, {
            "MINUTE", "org.hsqldb.Library.minute"
        }, {
            "MONTH", "org.hsqldb.Library.month"
        }, {
            "MONTHNAME", "org.hsqldb.Library.monthname"
        }, {
            "NOW", "org.hsqldb.Library.now"
        }, {
            "QUARTER", "org.hsqldb.Library.quarter"
        }, {
            "SECOND", "org.hsqldb.Library.second"
        }, {
            "WEEK", "org.hsqldb.Library.week"
        }, {
            "YEAR", "org.hsqldb.Library.year"
        }
    };
    static final String sSystem[][]   = {
        {
            "DATABASE", "org.hsqldb.Library.database"
        }, {
            "USER", "org.hsqldb.Library.user"
        }, {
            "IDENTITY", "org.hsqldb.Library.identity"
        }
    };

    static HashMap getAliasMap() {

        HashMap h = new HashMap(83, 1);

        register(h, sNumeric);
        register(h, sString);
        register(h, sTimeDate);
        register(h, sSystem);

        return h;
    }

    private static void register(HashMap h, String s[][]) {

        for (int i = 0; i < s.length; i++) {
            h.put(s[i][0], s[i][1]);
        }
    }

    private static final Random rRandom = new Random();

    // NUMERIC FUNCTIONS
// fredt@users 20020220 - patch 489184 by xclayl@users - thread safety

    /**
     * Returns the next pseudorandom, uniformly distributed <code>double</code> value
     * between 0.0 and 1.0 from a single, system-wide random number generator's
     * sequence, optionally re-seeding (and thus resetting) the generator sequence.
     *
     * If the seed value is <code>null</code>, then the underlying random number
     * generator retrieves the next value in its current sequence, else the seed
     * alters the state of the generator object so as to be in exactly the same state
     * as if it had just been created with the seed value.
     * @param seed an optional parameter with which to reseed the underlying
     * pseudorandom number generator
     * @return the next pseudorandom, uniformly distributed <code>double</code> value between
     *      0.0 and 1.0
     */
    public static synchronized double rand(Integer seed) {

        // boucherb@users 20020918
        // CHECKME: perhaps rRandom should be a member of Session,
        // since otherwise connections are *not* guranteed to get the
        // same pseudorandom sequence, given the same set of calls to this
        // SQL function.  This makes comparitive analysis difficult.
        // In fact, rRandom will be shared across multiple in-process
        // database instances, so it is not even guaranteed that the
        // sole connection to one instance will get the same sequence given
        // the same set of calls to this SQL function.
        if (seed != null) {
            rRandom.setSeed(seed.intValue());
        }

        return rRandom.nextDouble();
    }

    /**
     * Returns the absolute value of the given <code>double</code> value.
     * @param d the number for which to determine the absolute value
     * @return the absolute value of <code>d</code>, as a <code>double</code>
     */
    public static double abs(double d) {
        return Math.abs(d);
    }

    // this magic number works for 100000000000000; but not for 0.1 and 0.01
    private static final double LOG10_FACTOR = 0.43429448190325183;

    /**
     * Returns the base 10 logarithm of the given <code>double</code> value.
     * @param x the value for which to calculate the base 10 logarithm
     * @return the base 10 logarithm of <code>x</code>, as a <code>double</code>
     */
    public static double log10(double x) {
        return roundMagic(Math.log(x) * LOG10_FACTOR);
    }

    /**
     * Retrieves a <em>magically</em> rounded </code>double</code> value produced
     * from the given <code>double</code> value.  This method provides special
     * handling for numbers close to zero and performs rounding only for
     * numbers within a specific range, returning  precisely the given value
     * if it does not lie in this range. <p>
     *
     * Special handling includes: <p>
     *
     * <UL>
     * <LI> input in the interval -0.0000000000001..0.0000000000001 returns 0.0
     * <LI> input outside the interval -1000000000000..1000000000000 returns
     *      input unchanged
     * <LI> input is converted to String form
     * <LI> input with a <code>String</code> form length greater than 16 returns
     *      input unchaged
     * <LI> <code>String</code> form with last four characters of '...000x' where
     *      x != '.' is converted to '...0000'
     * <LI> <code>String</code> form with last four characters of '...9999' is
     *      converted to '...999999'
     * <LI> the <code>java.lang.Double.doubleValue</code> of the <code>String</code>
     *      form is returned
     * </UL>
     * @param d the double value for which to retrieve the <em>magically</em>
     *      rounded value
     * @return the <em>magically</em> rounded value produced
     */
    public static double roundMagic(double d) {

        // this function rounds numbers in a good way but slow:
        // - special handling for numbers around 0
        // - only numbers <= +/-1000000000000
        // - convert to a string
        // - check the last 4 characters:
        // '000x' becomes '0000'
        // '999x' becomes '999999' (this is rounded automatically)
        if ((d < 0.0000000000001) && (d > -0.0000000000001)) {
            return 0.0;
        }

        if ((d > 1000000000000.) || (d < -1000000000000.)) {
            return d;
        }

        StringBuffer s = new StringBuffer();

        s.append(d);

        int len = s.length();

        if (len < 16) {
            return d;
        }

        char cx = s.charAt(len - 1);
        char c1 = s.charAt(len - 2);
        char c2 = s.charAt(len - 3);
        char c3 = s.charAt(len - 4);

        if ((c1 == '0') && (c2 == '0') && (c3 == '0') && (cx != '.')) {
            s.setCharAt(len - 1, '0');
        } else if ((c1 == '9') && (c2 == '9') && (c3 == '9') && (cx != '.')) {
            s.setCharAt(len - 1, '9');
            s.append('9');
            s.append('9');
        }

        return Double.valueOf(s.toString()).doubleValue();
    }

    /**
     * Returns the cotangent of the given <code>double</code> value
     *  expressed in radians.
     * @param d the angle, expressed in radians
     * @return the cotangent
     */
    public static double cot(double d) {
        return 1. / Math.tan(d);
    }

    /**
     * Returns the remainder (modulus) of the first given integer divided
     * by the second. <p>
     *
     * @param i1 the numerator
     * @param i2 the divisor
     * @return <code>i1</code> % <code>i2</code>, as an <code>int</code>
     */
    public static int mod(int i1, int i2) {
        return i1 % i2;
    }

    /**
     * Returns the constant value, pi.
     * @return pi as a <code>double</code> value
     */
    public static double pi() {
        return Math.PI;
    }

    /**
     * Returns the given <code>double</code> value, rounded to the given
     * <code>int</code> places right of the decimal point. If
     * the supplied rounding place value is negative, rounding is performed
     * to the left of the decimal point, using its magnitude (absolute value).
     * @param d the value to be rounded
     * @param p the rounding place value
     * @return <code>d</code> rounded
     */
    public static double round(double d, int p) {

        double f = Math.pow(10., p);

        return Math.round(d * f) / f;
    }

    /**
     * Returns an indicator of the sign of the given <code>double</code>
     * value. If the value is less than zero, -1 is returned. If the value
     * equals zero, 0 is returned. If the value is greater than zero, 1 is
     * returned.
     * @param d the value
     * @return the sign of <code>d</code>
     */
    public static int sign(double d) {

        return (d < 0) ? -1
                       : ((d > 0) ? 1
                                  : 0);
    }

    /**
     * Returns the given <code>double</code> value, truncated to
     * the given <code>int</code> places right of the decimal point.
     * If the given place value is negative, the given <code>double</code>
     * value is truncated to the left of the decimal point, using the
     * magnitude (aboslute value) of the place value.
     * @param d the value to truncate
     * @param p the places left or right of the decimal point at which to
     *          truncate
     * @return <code>d</code>, truncated
     */
    public static double truncate(double d, int p) {

        double f = Math.pow(10., p);
        double g = d * f;

        return ((d < 0) ? Math.ceil(g)
                        : Math.floor(g)) / f;
    }

    /**
     * Returns the bit-wise logical <em>and</em> of the given
     * integer values.
     * @param i the first value
     * @param j the second value
     * @return he bit-wise logical <em>and</em> of
     *      <code>i</code> and <code>j</code>
     */
    public static int bitand(int i, int j) {
        return i & j;
    }

    /**
     * Returns the bit-wise logical <em>or</em> of the given
     * integer values.
     *
     * @param i the first value
     * @param j the second value
     * @return he bit-wise logical <em>and</em> of
     *      <code>i</code> and <code>j</code>
     */
    public static int bitor(int i, int j) {
        return i | j;
    }

    // STRING FUNCTIONS

    /**
     * Returns the Unicode code value of the leftmost character of
     * <code>s</code> as an <code>int</code>.  This is the same as the
     * ASCII value if the string contains only ASCII characters.
     * @param s the <code>String</code> to evaluate
     * @return the integer Unicode value of the
     *    leftmost character
     */
    public static Integer ascii(String s) {

        if ((s == null) || (s.length() == 0)) {
            return null;
        }

        return ValuePool.getInt(s.charAt(0));
    }

    /**
     * Returns the character string corresponding to the given ASCII
     * (or Unicode) value.
     *
     * <b>Note:</b> <p>
     *
     * In some SQL CLI
     * implementations, a <code>null</code> is returned if the range is outside 0..255.
     * In HSQLDB, the corresponding Unicode character is returned
     * unchecked.
     * @param code the character code for which to return a String
     *      representation
     * @return the String representation of the character
     */
    public static String character(int code) {
        return String.valueOf((char) code);
    }

    /**
     * Returns a <code>String</code> object that is the result of an
     * <em>SQL-style</em> concatenation of the given <code>String</code> objects. <p>
     *
     * <b>Note:</b> by <em>SQL-style</em>, it is meant:
     *
     * <UL>
     * <LI> if both <code>String</code> objects are <code>null</code>, return
     *      <code>null</code>
     * <LI> if only one string is <code>null</code>, return the other
     * <LI> if both <code>String</code> objects are non-null, return as a
     *      <code>String</code> object the character sequence obtained by listing,
     *      in left to right order, the characters of the first string followed by
     *      the characters of the second
     * </UL>
     * @param s1 the first <code>String</code>
     * @param s2 the second <code>String</code>
     * @return <code>s1</code> concatentated with <code>s2</code>
     */
    public static String concat(String s1, String s2) {

        if (s1 == null) {
            if (s2 == null) {
                return null;
            }

            return s2;
        }

        if (s2 == null) {
            return s1;
        }

        return s1 + s2;
    }

    /**
     * Returns a count of the characters that do not match when comparing
     * the 4 digit numeric SOUNDEX character sequences for the
     * given <code>String</code> objects.  If either <code>String</code> object is
     * <code>null</code>, zero is returned.
     * @param s1 the first <code>String</code>
     * @param s2 the second <code>String</code>
     * @return the number of differences between the <code>SOUNDEX</code> of
     *      <code>s1</code> and the <code>SOUNDEX</code> of <code>s2</code>
     */

// fredt@users 20020305 - patch 460907 by fredt - soundex
    public static int difference(String s1, String s2) {

        // todo: check if this is the standard algorithm
        if ((s1 == null) || (s2 == null)) {
            return 0;
        }

        s1 = soundex(s1);
        s2 = soundex(s2);

        int e = 0;

        for (int i = 0; i < 4; i++) {
            if (s1.charAt(i) != s2.charAt(i)) {
                e++;
            }
        }

        return e;
    }

    /**
     * Converts a <code>String</code> of hexidecimal digit characters to a raw
     * binary value, represented as a <code>String</code>.<p>
     *
     * The given <code>String</code> object must consist of a sequence of
     * 4 digit hexidecimal character substrings.<p> If its length is not
     * evenly divisible by 4, <code>null</code> is returned.  If any any of
     * its 4 character subsequences cannot be parsed as a
     * 4 digit, base 16 value, then a NumberFormatException is thrown.
     *
     * This conversion has the effect of reducing the character count 4:1.
     *
     * @param s a <code>String</code> of hexidecimal digit characters
     * @return an equivalent raw binary value, represented as a
     *      <code>String</code>
     */
    public static String hexToRaw(String s) {

        char         raw;
        StringBuffer to  = new StringBuffer();
        int          len = s.length();

        if (len % 4 != 0) {
            return null;
        }

        for (int i = 0; i < len; i += 4) {
            raw = (char) Integer.parseInt(s.substring(i, i + 4), 16);

            to.append(raw);
        }

        return (to.toString());
    }

    /**
     * Returns a character sequence which is the result of writing the
     * first <code>length</code> number of characters from the second
     * given <code>String</code> over the first string. The start position
     * in the first string where the characters are overwritten is given by
     * <code>start</code>.<p>
     *
     * <b>Note:</b> In order of precedence, boundry conditions are handled as
     * follows:<p>
     *
     * <UL>
     * <LI>if either supplied <code>String</code> is null, then the other is
     *      returned; the check starts with the first given <code>String</code>.
     * <LI>if <code>start</code> is less than one, <code>s1</code> is returned
     * <LI>if <code>length</code> is less than or equal to zero,
     *     <code>s1</code> is returned
     * <LI>if the length of <code>s2</code> is zero, <code>s1</code> is returned
     * <LI>if <code>start</code> is greater than the length of <code>s1</code>,
     *      <code>s1</code> is returned
     * <LI>if <code>length</code> is such that, taken together with
     *      <code>start</code>, the indicated interval extends
     *      beyond the end of <code>s1</code>, then the insertion is performed
     *      precisely as if upon a copy of <code>s1</code> extended in length
     *      to just include the indicated interval
     * </UL>
     * @param s1 the <code>String</code> into which to insert <code>s2</code>
     * @param start the position, with origin one, at which to start the insertion
     * @param length the number of characters in <code>s1</code> to replace
     * @param s2 the <code>String</code> to insert into <code>s1</code>
     * @return <code>s2</code> inserted into <code>s1</code>, as indicated
     *      by <code>start</code> and <code>length</code> and adjusted for
     *      boundry conditions
     */
    public static String insert(String s1, int start, int length, String s2) {

        if (s1 == null) {
            return s2;
        }

        if (s2 == null) {
            return s1;
        }

        int len1 = s1.length();
        int len2 = s2.length();

        start--;

        if (start < 0 || length <= 0 || len2 == 0 || start > len1) {
            return s1;
        }

        if (start + length > len1) {
            length = len1 - start;
        }

        return s1.substring(0, start) + s2 + s1.substring(start + length);
    }

    /**
     * Returns a copy of the given <code>String</code>, with all upper case
     * characters converted to lower case. This uses the default Java String
     * conversion.
     * @param s the <code>String</code> from which to produce a lower case
     *      version
     * @return a lower case version of <code>s</code>
     */
    public static String lcase(String s) {
        return (s == null) ? null
                           : s.toLowerCase();
    }

    /**
     * Returns the leftmost <code>count</code> characters from the given
     * <code>String</code>. <p>
     *
     * <b>Note:</b> boundry conditions are handled in the following order of
     * precedence:
     *
     * <UL>
     *  <LI> if <code>s</code> is <code>null</code>, then <code>null</code>
     *      is returned
     *  <LI> if <code>count</code> is less than 1, then a zero-length
     *       <code>String</code> is returned
     *  <LI> if <code>count</code> is greater than the length of <code>s</code>,
     *      then a copy of <code>s</code> is returned
     * </UL>
     * @param s the <code>String</code> from which to retrieve the leftmost
     *      characters
     * @param count the count of leftmost characters to retrieve
     * @return the leftmost <code>count</code> characters of <code>s</code>
     */
    public static String left(String s, int count) {

        if (s == null) {
            return null;
        }

        return s.substring(0, ((count < 0) ? 0
                                           : (count < s.length()) ? count
                                                                  : s.length()));
    }

// fredt@users - 20020819 - patch 595854 by thomasm@users

    /**
     * Returns the number of characters in the given <code>String</code>.
     * This includes trailing blanks.
     *
     * @param s the <code>String</code> for which to determine length
     * @return the length of <code>s</code>, including trailing blanks
     */
    public static Integer length(String s) {
        return s == null ? null
                         : ValuePool.getInt(s.length());
    }

    /**
     * Returns the starting position of the first occurrence of
     * the given <code>search</code> <code>String</code> object within
     * the given <code>String</code> object, <code>s</code>.
     *
     * The search for the first occurrence of <code>search</code> begins with
     * the first character position in <code>s</code>, unless the optional
     * argument, <code>start</code>, is specified (non-null). If
     * <code>start</code> is specified, the search begins with the character
     * position indicated by the value of <code>start</code>, where the
     * first character position in <code>s</code> is indicated by the value 1.
     * If <code>search</code> is not found within <code>s</code>, the
     * value 0 is returned.
     * @param search the <code>String</code> occurence to find in <code>s</code>
     * @param s the <code>String</code> within which to find the first
     *      occurence of <code>search</code>
     * @param start the optional character position from which to start
     *      looking in <code>s</code>
     * @return the one-based starting position of the first occurrence of
     *      <code>search</code> within <code>s</code>, or 0 if not found
     */
    public static int locate(String search, String s, Integer start) {

        if (s == null || search == null) {
            return 0;
        }

        int i = (start == null) ? 0
                                : start.intValue() - 1;

        return s.indexOf(search, (i < 0) ? 0
                                         : i) + 1;
    }

    /**
     * Returns the characters of the given <code>String</code>, with the
     * leading spaces removed. Characters such as TAB are not removed.
     *
     * @param s the <code>String</code> from which to remove the leading blanks
     * @return the characters of the given <code>String</code>, with the leading
     *      spaces removed
     */
    public static String ltrim(String s) {

        if (s == null) {
            return s;
        }

        int len = s.length(),
            i   = 0;

        while (i < len && s.charAt(i) <= ' ') {
            i++;
        }

        return (i == 0) ? s
                        : s.substring(i);
    }

    /**
     * Converts a raw binary value, as represented by the given
     * <code>String</code>, to the equivalent <code>String</code>
     * of hexidecimal digit characters. <p>
     *
     * This conversion has the effect of expanding the character count 1:4.
     *
     * @param s the raw binary value, as a <code>String</code>
     * @return an equivalent <code>String</code> of hexidecimal digit characters
     */
    public static String rawToHex(String s) {

        if (s == null) {
            return null;
        }

        char         from[] = s.toCharArray();
        String       hex;
        StringBuffer to = new StringBuffer(4 * s.length());

        for (int i = 0; i < from.length; i++) {
            hex = Integer.toHexString(from[i] & 0xffff);

            for (int j = hex.length(); j < 4; j++) {
                to.append('0');
            }

            to.append(hex);
        }

        return (to.toString());
    }

    /**
     * Returns a <code>String</code> composed of the given <code>String</code>,
     * repeated  <code>count</code> times.
     *
     * @param s the <code>String</code> to repeat
     * @param count the number of repetitions
     * @return the given <code>String</code>, repeated <code>count</code> times
     */
    public static String repeat(String s, Integer count) {

        if (s == null || count == null || count.intValue() < 0) {
            return null;
        }

        int          i = count.intValue();
        StringBuffer b = new StringBuffer(s.length() * i);

        while (i-- > 0) {
            b.append(s);
        }

        return b.toString();
    }

// fredt@users - 20020903 - patch 1.7.1 - bug fix to allow multiple replaces

    /**
     * Replaces all occurrences of <code>replace</code> in <code>s</code>
     * with the <code>String</code> object: <code>with</code>
     * @param s the target for replacement
     * @param replace the substring(s), if any, in <code>s</code> to replace
     * @param with the value to substitute for <code>replace</code>
     * @return <code>s</code>, with all occurences of <code>replace</code>
     *      replaced by <code>with</code>
     */
    public static String replace(String s, String replace, String with) {

        if (s == null || replace == null) {
            return s;
        }

        if (with == null) {
            with = "";
        }

        StringBuffer b          = new StringBuffer();
        int          start      = 0;
        int          lenreplace = replace.length();

        while (true) {
            int i = s.indexOf(replace, start);

            if (i == -1) {
                b.append(s.substring(start));

                break;
            }

            b.append(s.substring(start, i));
            b.append(with);

            start = i + lenreplace;
        }

        return b.toString();
    }

    /**
     * Returns the rightmost <code>count</code> characters of the given
     * <code>String</code>, <code>s</code>.
     *
     * <b>Note:</b> boundry conditions are handled in the following order of
     * precedence:
     *
     * <UL>
     *  <LI> if <code>s</code> is <code>null</code>, <code>null</code> is returned
     *  <LI> if <code>count</code> is less than one, a zero-length
     *      <code>String</code> is returned
     *  <LI> if <code>count</code> is greater than the length of <code>s</code>,
     *      a copy of <code>s</code> is returned
     * </UL>
     * @param s the <code>String</code> from which to retrieve the rightmost
     *      <code>count</code> characters
     * @param count the number of rightmost characters to retrieve
     * @return the rightmost <code>count</code> characters of <code>s</code>
     */
    public static String right(String s, int count) {

        if (s == null) {
            return null;
        }

        count = s.length() - count;

        return s.substring((count < 0) ? 0
                                       : (count < s.length()) ? count
                                                              : s.length());
    }

// fredt@users 20020530 - patch 1.7.0 fredt - trim only the space character

    /**
     * Returns the characters of the given <code>String</code>, with trailing
     * spaces removed.
     * @param s the <code>String</code> from which to remove the trailing blanks
     * @return the characters of the given <code>String</code>, with the
     * trailing spaces removed
     */
    public static String rtrim(String s) {

        if (s == null) {
            return s;
        }

        int endindex = s.length() - 1;
        int i        = endindex;

        for (; i >= 0 && s.charAt(i) == ' '; i--) {}

        return i == endindex ? s
                             : s.substring(0, i + 1);
    }

// fredt@users 20011010 - patch 460907 by fredt - soundex

    /**
     * Returns a four character code representing the sound of the given
     * <code>String</code>. Non-ASCCI characters in the
     * input <code>String</code> are ignored. <p>
     *
     * This method was
     * rewritten for HSQLDB by fredt@users to comply with the description at
     * <a href="http://www.nara.gov/genealogy/coding.html">
     * http://www.nara.gov/genealogy/coding.html</a>.<p>
     * @param s the <code>String</code> for which to calculate the 4 character
     *      <code>SOUNDEX</code> value
     * @return the 4 character <code>SOUNDEX</code> value for the given
     *      <code>String</code>
     */
    public static String soundex(String s) {

        if (s == null) {
            return s;
        }

        s = s.toUpperCase();

        int  len       = s.length();
        char b[]       = new char[] {
            '0', '0', '0', '0'
        };
        char lastdigit = '0';

        for (int i = 0, j = 0; i < len && j < 4; i++) {
            char c = s.charAt(i);
            char newdigit;

            if ("AEIOUY".indexOf(c) != -1) {
                newdigit = '7';
            } else if (c == 'H' || c == 'W') {
                newdigit = '8';
            } else if ("BFPV".indexOf(c) != -1) {
                newdigit = '1';
            } else if ("CGJKQSXZ".indexOf(c) != -1) {
                newdigit = '2';
            } else if (c == 'D' || c == 'T') {
                newdigit = '3';
            } else if (c == 'L') {
                newdigit = '4';
            } else if (c == 'M' || c == 'N') {
                newdigit = '5';
            } else if (c == 'R') {
                newdigit = '6';
            } else {
                continue;
            }

            if (j == 0) {
                b[j++]    = c;
                lastdigit = newdigit;
            } else if (newdigit <= '6') {
                if (newdigit != lastdigit) {
                    b[j++]    = newdigit;
                    lastdigit = newdigit;
                }
            } else if (newdigit == '7') {
                lastdigit = newdigit;
            }
        }

        return new String(b, 0, 4);
    }

    /**
     * Returns a <code>String</code> consisting of <code>count</code> spaces, or
     * <code>null</code> if <code>count</code> is less than zero. <p>
     *
     * @param count the number of spaces to produce
     * @return a <code>String</code> of <code>count</code> spaces
     */
    public static String space(int count) {

        if (count < 0) {
            return null;
        }

        char c[] = new char[count];

        while (count > 0) {
            c[--count] = ' ';
        }

        return new String(c);
    }

    /**
     * Returns the characters from the given <code>String</code>, staring at
     * the indicated one-based <code>start</code> position and extending the
     * (optional) indicated <code>length</code>. If <code>length</code> is not
     * specified (is <code>null</code>), the remainder of <code>s</code> is
     * implied.
     *
     * @param s the <code>String</code> from which to produce the indicated
     *      substring
     * @param start the starting position of the desired substring
     * @param length the length of the desired substring
     * @return the indicted substring of <code>s</code>.
     */

// fredt@users 20020210 - patch 500767 by adjbirch@users - modified
    public static String substring(String s, int start, Integer length) {

        if (s == null) {
            return null;
        }

        int len = s.length();

        start--;

        start = (start > len) ? len
                              : start;

        int l = len;

        if (length != null) {
            l = length.intValue();
        }

        if (start + l > len) {
            l = len - start;
        }

        return s.substring(start, start + l);
    }

    /**
     * Returns a copy of the given <code>String</code>, with all lower case
     * characters converted to upper case using the default Java method.
     * @param s the <code>String</code> from which to produce an upper case
     *      version
     * @return an upper case version of <code>s</code>
     */
    public static String ucase(String s) {
        return (s == null) ? null
                           : s.toUpperCase();
    }

    // TIME AND DATE

    /**
     * Returns the current date as a date value.
     *
     * @return a date value representing the current date
     */
    public static java.sql.Date curdate() {

        return HsqlDateTime.getToday();

//        return new java.sql.Date(System.currentTimeMillis());
    }

    /**
     * Returns the current local time as a time value.
     * @return a time value representing the current local time
     */
    public static java.sql.Time curtime() {
        return new java.sql.Time(System.currentTimeMillis());
    }

    /**
     * Returns a character string containing the name of the day
     * (Sunday, Monday, Tuesday, Wednesday, Thursday, Friday, Saturday )
     * for the day portion of the given <code>java.sql.Date</code>.
     * @param d the date value from which to extract the day name
     * @return the name of the day corresponding to the given
     * <code>java.sql.Date</code>
     */
    public static String dayname(java.sql.Date d) {

        SimpleDateFormat f = new SimpleDateFormat("EEEE");

        return f.format(d).toString();
    }

    /**
     * Returns an integral value representing the indicated
     * part of the given date object, using a <code>GregorianCalendar</code>
     * object.
     * @param d the <code>Date</code> object from which to extract the indicated part
     * @param part an integer code corresponding to the desired date part
     * @return the indicated part of the given <code>java.util.Date</code> object
     */
    static int getDateTimePart(java.util.Date d, int part) {

        Calendar c = new GregorianCalendar();

        c.setTime(d);

        return c.get(part);
    }

    /**
     * Returns an integral value representing the indicated
     * part of the given time object, using a <code>GregorianCalendar</code>
     * object.
     * @param t the Time object from which to extract the indicated part
     * @param part an integer code corresponding to the desired time part
     * @return he indicated part of the given <code>java.sql.Time</code> object
     */
    private static int getTimePart(java.sql.Time t, int part) {

        Calendar c = new GregorianCalendar();

        c.setTime(t);

        return c.get(part);
    }

    /**
     * Returns the day of the month from the given date value, as an integer
     * value in the range of 1-31.
     *
     * @param d the date value from which to extract the day of month
     * @return the day of the month from the given date value
     */
    public static int dayofmonth(java.sql.Date d) {
        return getDateTimePart(d, Calendar.DAY_OF_MONTH);
    }

    /**
     * Returns the day of the week from the given date value, as an integer
     * value in the range 1-7, where 1 represents Sunday.
     *
     * @param d the date value from which to extract the day of week
     * @return the day of the week from the given date value
     */
    public static int dayofweek(java.sql.Date d) {
        return getDateTimePart(d, Calendar.DAY_OF_WEEK);
    }

    /**
     * Returns the day of the year from the given date value, as an integer
     * value in the range 1-366.
     *
     * @param d the date value from which to extract the day of year
     * @return the day of the year from the given date value
     */
    public static int dayofyear(java.sql.Date d) {
        return getDateTimePart(d, Calendar.DAY_OF_YEAR);
    }

    /**
     * Returns the hour from the given time value, as an integer value in
     * the range of 0-23.
     *
     * @param t the time value from which to extract the hour of day
     * @return the hour of day from the given time value
     */

// fredt@users 20020210 - patch 513005 by sqlbob@users (RMP) - hour
    public static int hour(java.sql.Time t) {
        return getDateTimePart(t, Calendar.HOUR_OF_DAY);
    }

    /**
     * Returns the minute from the given time value, as integer value in
     * the range of 0-59.
     *
     * @param t the time value from which to extract the minute value
     * @return the minute value from the given time value
     */
    public static int minute(java.sql.Time t) {
        return getDateTimePart(t, Calendar.MINUTE);
    }

    /**
     * Returns the month from the given date value, as an integer value in the
     * range of 1-12 or 0-11. <p>
     *
     * If the sql_month database property is set <code>true</code>, then the
     * range is 1-12, else 0-11
     *
     * @param d the date value from which to extract the month value
     * @return the month value from the given date value
     */
    public static int month(java.sql.Date d) {
        return 0;
    }

    /**
     * Returns a character string containing the name of month
     * (January, February, March, April, May, June, July, August,
     * September, October, November, December) for the month portion of
     * the given date value.
     *
     * @param d the date value from which to extract the month name
     * @return a String representing the month name from the given date value
     */
    public static String monthname(java.sql.Date d) {

        SimpleDateFormat f = new SimpleDateFormat("MMMM");

        return f.format(d).toString();
    }

    /**
     * Returns the current date and time as a timestamp value.
     *
     * @return a timestamp value representing the current date and time
     */
    public static Timestamp now() {
        return new Timestamp(System.currentTimeMillis());
    }

    /**
     * Returns the quarter of the year in the given date value, as an integer
     * value in the range of 1-4.
     *
     * @param d the date value from which to extract the quarter of the year
     * @return an integer representing the quater of the year from the given
     *      date value
     */
    public static int quarter(java.sql.Date d) {
        return (getDateTimePart(d, Calendar.MONTH) / 3) + 1;
    }

    /**
     * Returns the second of the given time value, as an integer value in
     * the range of 0-59.
     *
     * @param d the date value from which to extract the second of the hour
     * @return an integer representing the second of the hour from the
     *      given time value
     */
    public static int second(java.sql.Time d) {
        return getDateTimePart(d, Calendar.SECOND);
    }

    /**
     * Returns the week of the year from the given date value, as an integer
     * value in the range of 1-53.
     *
     * @param d the date value from which to extract the week of the year
     * @return an integer representing the week of the year from the given
     *      date value
     */
    public static int week(java.sql.Date d) {
        return getDateTimePart(d, Calendar.WEEK_OF_YEAR);
    }

    /**
     * Returns the year from the given date value, as an integer value in
     * the range of 1-9999.
     *
     * @param d the date value from which to extract the year
     * @return an integer value representing the year from the given
     *      date value
     */
    public static int year(java.sql.Date d) {
        return getDateTimePart(d, Calendar.YEAR);
    }

    // SYSTEM
    /*
     * All system functions that return Session dependent information are
     * dummies here.
     */

    /**
     * Returns the name of the database corresponding to this connection.
     *
     * @param conn the connection for which to retrieve the database name
     * @return the name of the database for the given connection
     * @throws HsqlException if a database access error occurs
     */
    public static String database(Connection conn) throws HsqlException {
        return null;
    }

    /**
     * Returns the user's authorization name (the user's name as known to this
     * database).
     *
     * @param conn the connection for which to retrieve the user name
     * @return the user's name as known to the database
     * @throws HsqlException if a database access error occurs
     */
    public static String user(Connection conn) throws HsqlException {
        return null;
    }

    /**
     * Retrieves the last auto-generated integer indentity value used
     * by this connection.
     *
     * @return the connection's the last generated integer identity value
     * @throws HsqlException if a database access error occurs
     */
    public static int identity() throws HsqlException {
        return 0;
    }

    // JDBC SYSTEM

    /**
     * Retrieves the autocommit status of this connection. <p>
     *
     * @param conn the <code>Connection</code> object for which to retrieve
     *      the current autocommit status
     * @return a boolean value representing the connection's autocommit status
     * @since HSQLDB 1.7.0
     */
    public static boolean getAutoCommit(Connection conn) {
        return false;
    }

    /**
     * Retrieves the name of this database product. <p>
     *
     * @return database product name as a <code>String</code> object
     * @since HSQLDB 1.7.2
     */
    public static String getDatabaseProductName() {
        return "HSQL Database Engine";
    }

    /**
     * Retrieves the version number of this database product. <p>
     *
     * @return database version number as a <code>String</code> object
     * @since HSQLDB 1.7.2
     */
    public static String getDatabaseProductVersion() {
        return "1.7.2";
    }

    /**
     * Retrieves the major version number of this database. <p>
     *
     * @return the database's major version as an <code>int</code> value
     * @since HSQLDB 1.7.2
     */
    public static int getDatabaseMajorVersion() {
        return 1;
    }

    /**
     * Retrieves the major version number of this database. <p>
     *
     * @return the database's major version as an <code>int</code> value
     * @since HSQLDB 1.7.2
     */
    public static int getDatabaseMinorVersion() {
        return 7;
    }

    /**
     * Retrieves whether this connection is in read-only mode. <p>
     *
     * @param conn the <code>Connection</code> object for which to retrieve
     *      the current read-only status
     * @return  <code>true</code> if connection is read-only and
     *      <code>false</code> otherwise
     * @since HSQLDB 1.7.2
     */
    public static boolean isReadOnlyConnection(Connection conn) {
        return false;
    }

    /**
     * Retrieves whether this database is in read-only mode. <p>
     *
     * @param c the <code>Connection</code> object for which to retrieve
     *      the current database read-only status
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @since HSQLDB 1.7.2
     */
    public static boolean isReadOnlyDatabase(Connection c) {
        return false;
    }

    /**
     * Retrieves whether the files of this database are in read-only mode. <p>
     *
     * @param c the <code>Connection</code> object for which to retrieve
     *      the current database files read-only status
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @since HSQLDB 1.7.2
     */
    public static boolean isReadOnlyDatabaseFiles(Connection c) {
        return false;
    }

// boucherb@users - patch 1.7.2 - ResultSetMetaData
// TODO: encode data type subcode and table column nullability in
// Result.colType array values in order to avoid database
// call back for certain ResultSetMetaData values.
//
// One possible encoding might be:
// nullability:    (colType[i] & 0x00f00000) >>> 20
// data type sub:  (colType[i] & 0x000f0000) >>> 16
// data type code: (colType[i] & 0x0000ffff)
//
// An even denser encoding might be:
// decl. size:      (colType[i] & 0xff000000) >>> 24
// - allows transmitting decl. length/precision up to 255...
// - for types with create params, zero might mean: call back to db required
// ext:             (colType[i] & 0x00ff0000)
// read-only:       (ext & 0x00800000) != 0
// autoincrement:   (ext & 0x00400000) != 0
// nullability:     (ext & 0x00300000) >>> 20
// data type sub:   (ext & 0x000f0000) >>> 16
// data type code:  (colType[i] & 0x0000ffff)

    /**
     * Retrieves a Result object describing the connection-dependent column
     * metadata for each requested (table,column) pair. <p>
     *
     * @param c the <code>Connection</code> object on which to retrieve
     *      the connection-dependent column metadata
     * @param columns a character sequence whose content is a CSV list of
     *    SQL strings representing requests for conection-dependent
     *    column metadata regarding (table-name, column-name) pairs. <p>
     *
     *    Example 1 (as the content of Java String): <p>
     *
     *    'table-name-1','column-name-1','table-name-2','column-name-2'<p>
     *
     *    Example 2 (on the SQL command line): <p>
     *
     *    call "org.hsqldb.Library.getCDColumnMetaData"(
     *      '''table-name-1'',''column-name-1'',
     *      ''table-name-2'',''column-name-2''') <p>
     *
     *    <b>Note:</b> since it may be difficult to tell using certain viewers,
     *    text encodings or fonts, please realize that, in the second example,
     *    the parameter string contains no double quote characters,
     *    only single-quote characters.<p>
     *
     * @throws HsqlException if a database access error occurs
     * @return A Result object describing the
     *    connection-dependent column metadata
     *    for each requested (table-name,column-name) pair
     */
    public static Object getCDColumnMetaData(Connection c,
            String columns) throws HsqlException {

        Session             session;
        Database            database;
        DatabaseInformation dInfo;
        HsqlProperties      props;
        HsqlArrayList       columnList;
        Tokenizer           tokenizer;
        String              token;
        String              catalog;
        String              tableName;
        String              columnName;
        int                 len;
        boolean             reportCatalogs;
        boolean             reportSchemas;
        Result              result;
        Table               table;
        Column              column;
        Object[]            row;

        tokenizer  = new Tokenizer(columns);
        len        = tokenizer.getLength();
        columnList = new HsqlArrayList();

        while (tokenizer.getPosition() < len) {
            token = tokenizer.getString();

            columnList.add(token);

            if (tokenizer.getPosition() < len) {
                tokenizer.getThis(",");
            }
        }

        len = columnList.size();

        Trace.doAssert((len & 0x1) == 0,
                       "must be a valid list of table/column name pairs");

        session           = ((jdbcConnection) c).sessionProxy.getSession();
        database          = session.getDatabase();
        dInfo             = database.dInfo;
        props             = database.getProperties();
        reportCatalogs    = props.isPropertyTrue("hsqldb.catalogs");
        reportSchemas     = props.isPropertyTrue("hsqldb.schemas");
        catalog           = reportCatalogs ? database.getPath()
                                           : "";
        result            = new Result(ResultConstants.DATA, 5);
        result.colType[0] = Types.VARCHAR;
        result.colType[1] = Types.VARCHAR;
        result.colType[2] = Types.BIT;
        result.colType[3] = Types.BIT;
        result.colType[4] = Types.BIT;
        result.sTable[0]  = "";
        result.sTable[1]  = "";
        result.sTable[2]  = "";
        result.sTable[3]  = "";
        result.sTable[4]  = "";
        result.sName      = result.sTable;
        result.sLabel[0]  = "CATALOG";
        result.sLabel[1]  = "SCHEMA";
        result.sLabel[2]  = "IS_IDENTITY";
        result.sLabel[3]  = "IS_NULLABLE";
        result.sLabel[4]  = "IS_READONLY";

        for (int i = 0; i < len; ) {
            tableName  = (String) columnList.get(i++);
            columnName = (String) columnList.get(i++);
            row        = new Object[5];

            if (dInfo.isSystemTable(tableName)) {
                try {
                    table = dInfo.getSystemTable(tableName, session);
                } catch (Exception e) {
                    table = null;
                }
            } else {

                // TODO:  non-admin users can create TEMP tables but
                //        we need to update access checks to allow
                //        non-admin users to update temp tables they
                //        create
                table = database.findUserTable(tableName);
            }

            if (table == null ||!session.isAccessible(table.getName())) {
                row[0] = "";
                row[1] = "";
                row[2] = Boolean.FALSE;
                row[3] = null;
                row[4] = Boolean.TRUE;
            } else {
                try {
                    column = table.getColumn(table.getColumnNr(columnName));
                } catch (Exception e) {
                    column = null;
                }

                if (column == null) {
                    row[0] = "";
                    row[1] = "";
                    row[2] = Boolean.FALSE;
                    row[3] = null;
                    row[4] = Boolean.TRUE;
                } else {
                    row[0] = catalog;

                    if (reportSchemas) {

                        // REMIND: Maybe use/reference DINameSpace here?
                        //         Maybe break out constants to interface?
                        // This seems like a good code reuse opportunity,
                        // but is avoided because of suspected overhead
                        // and the fact that there is no guarantee yet that
                        // DINameSpace is CORE (always included in jar)
                        if (table.tableType == Table.SYSTEM_TABLE) {
                            row[1] = "DEFINITION_SCHEMA";
                        } else if (table.equals(tableName, session)) {
                            row[1] = session.getUsername();
                        } else {
                            row[1] = "PUBLIC";
                        }
                    } else {
                        row[1] = "";
                    }

                    row[2] = ValuePool.getBoolean(column.isIdentity());
                    row[3] = ValuePool.getBoolean(column.isNullable()
                                                  &&!column.isIdentity());
                    row[4] =
                        ValuePool.getBoolean(database.databaseReadOnly
                                             || table.isDataReadOnly()
                                             || (database.filesReadOnly
                                                 && (table.isCached()
                                                     || table.isText())));
                }
            }

            result.add(row);
        }

        return result;
    }

/*
// test for soundex
    public static void main (String argv[]){
        String [] names = {"Yyhiokkk","Washington","Lee","Gutierrez","Pfister","Jackson","Tymczak","Ashcraft","VanDeusen","Deusen","Van Deusen"};
        for (int i = 0 ; i < names.length; i++ ){
            System.out.print( names[i] + " : " + soundex(names[i] + "\n"));
        }
    }
*/
    static final int abs                       = 0;
    static final int ascii                     = 1;
    static final int bitand                    = 2;
    static final int bitor                     = 3;
    static final int character                 = 4;
    static final int concat                    = 5;
    static final int cot                       = 6;
    static final int curdate                   = 7;
    static final int curtime                   = 8;
    static final int database                  = 9;
    static final int dayname                   = 10;
    static final int dayofmonth                = 11;
    static final int dayofweek                 = 12;
    static final int dayofyear                 = 13;
    static final int difference                = 14;
    static final int getAutoCommit             = 15;
    static final int getDatabaseMajorVersion   = 16;
    static final int getDatabaseMinorVersion   = 17;
    static final int getDatabaseProductName    = 18;
    static final int getDatabaseProductVersion = 19;
    static final int hexToRaw                  = 20;
    static final int hour                      = 21;
    static final int identity                  = 22;
    static final int insert                    = 23;
    static final int isReadOnlyConnection      = 24;
    static final int isReadOnlyDatabase        = 25;
    static final int lcase                     = 26;
    static final int left                      = 27;
    static final int length                    = 28;
    static final int locate                    = 29;
    static final int log10                     = 30;
    static final int ltrim                     = 31;
    static final int minute                    = 32;
    static final int mod                       = 33;
    static final int month                     = 34;
    static final int monthname                 = 35;
    static final int now                       = 36;
    static final int pi                        = 37;
    static final int quarter                   = 38;
    static final int rand                      = 39;
    static final int rawToHex                  = 40;
    static final int repeat                    = 41;
    static final int replace                   = 42;
    static final int right                     = 43;
    static final int round                     = 44;
    static final int roundMagic                = 45;
    static final int rtrim                     = 46;
    static final int second                    = 47;
    static final int sign                      = 48;
    static final int soundex                   = 49;
    static final int space                     = 50;
    static final int substring                 = 51;
    static final int truncate                  = 52;
    static final int ucase                     = 53;
    static final int user                      = 54;
    static final int week                      = 55;
    static final int year                      = 56;
    static final int getCDColumnMetaData       = 57;
    static final int isReadOnlyDatabaseFiles   = 58;
    private static final IntValueHashMap functionMap =
        new IntValueHashMap(117);
    static final Double piValue = new Double(Library.pi());

    static {
        functionMap.put("abs", abs);
        functionMap.put("ascii", ascii);
        functionMap.put("bitand", bitand);
        functionMap.put("bitor", bitor);
        functionMap.put("character", character);
        functionMap.put("concat", concat);
        functionMap.put("cot", cot);
        functionMap.put("curdate", curdate);
        functionMap.put("curtime", curtime);
        functionMap.put("database", database);
        functionMap.put("dayname", dayname);
        functionMap.put("dayofmonth", dayofmonth);
        functionMap.put("dayofweek", dayofweek);
        functionMap.put("dayofyear", dayofyear);
        functionMap.put("difference", difference);
        functionMap.put("getAutoCommit", getAutoCommit);
        functionMap.put("getDatabaseMajorVersion", getDatabaseMajorVersion);
        functionMap.put("getDatabaseMinorVersion", getDatabaseMinorVersion);
        functionMap.put("getDatabaseProductName", getDatabaseProductName);
        functionMap.put("getDatabaseProductVersion",
                        getDatabaseProductVersion);
        functionMap.put("hexToRaw", hexToRaw);
        functionMap.put("hour", hour);
        functionMap.put("identity", identity);
        functionMap.put("insert", insert);
        functionMap.put("isReadOnlyConnection", isReadOnlyConnection);
        functionMap.put("isReadOnlyDatabase", isReadOnlyDatabase);
        functionMap.put("lcase", lcase);
        functionMap.put("left", left);
        functionMap.put("length", length);
        functionMap.put("locate", locate);
        functionMap.put("log10", log10);
        functionMap.put("ltrim", ltrim);
        functionMap.put("minute", minute);
        functionMap.put("mod", mod);
        functionMap.put("month", month);
        functionMap.put("monthname", monthname);
        functionMap.put("now", now);
        functionMap.put("pi", pi);
        functionMap.put("quarter", quarter);
        functionMap.put("rand", rand);
        functionMap.put("rawToHex", rawToHex);
        functionMap.put("repeat", repeat);
        functionMap.put("replace", replace);
        functionMap.put("right", right);
        functionMap.put("round", round);
        functionMap.put("roundMagic", roundMagic);
        functionMap.put("rtrim", rtrim);
        functionMap.put("second", second);
        functionMap.put("sign", sign);
        functionMap.put("soundex", soundex);
        functionMap.put("space", space);
        functionMap.put("substring", substring);
        functionMap.put("truncate", truncate);
        functionMap.put("ucase", ucase);
        functionMap.put("user", user);
        functionMap.put("week", week);
        functionMap.put("year", year);
        functionMap.put("getCDColumnMetaData", getCDColumnMetaData);
        functionMap.put("isReadOnlyDatabaseFiles", isReadOnlyDatabaseFiles);
    }

    static Object invoke(int fID, Object[] parms) throws HsqlException {

        try {
            switch (fID) {

                case abs : {
                    return new Double(
                        Library.abs(((Number) parms[0]).doubleValue()));
                }
                case ascii : {
                    return ascii((String) parms[0]);
                }
                case bitand : {
                    return ValuePool.getInt(
                        bitand(((Number) parms[0]).intValue(),
                               ((Number) parms[1]).intValue()));
                }
                case bitor : {
                    return ValuePool.getInt(
                        bitor(((Number) parms[0]).intValue(),
                              ((Number) parms[1]).intValue()));
                }
                case character : {
                    return character(((Number) parms[0]).intValue());
                }
                case concat : {
                    return concat((String) parms[0], (String) parms[1]);
                }
                case cot : {
                    return new Double(cot(((Number) parms[0]).doubleValue()));
                }
                case curdate : {
                    return curdate();
                }
                case curtime : {
                    return curtime();
                }
                case database : {
                    return null;
                }
                case dayname : {
                    return dayname((Date) parms[0]);
                }
                case dayofmonth : {
                    return ValuePool.getInt(dayofmonth((Date) parms[0]));
                }
                case dayofweek : {
                    return ValuePool.getInt(dayofweek((Date) parms[0]));
                }
                case dayofyear : {
                    return ValuePool.getInt(dayofyear((Date) parms[0]));
                }
                case difference : {
                    return ValuePool.getInt(difference((String) parms[0],
                                                       (String) parms[1]));
                }
                case getAutoCommit : {
                    return null;
                }
                case getDatabaseMajorVersion : {
                    return ValuePool.getInt(getDatabaseMajorVersion());
                }
                case getDatabaseMinorVersion : {
                    return ValuePool.getInt(getDatabaseMinorVersion());
                }
                case getDatabaseProductName : {
                    return getDatabaseProductName();
                }
                case getDatabaseProductVersion : {
                    return getDatabaseProductVersion();
                }
                case hexToRaw : {
                    return hexToRaw((String) parms[0]);
                }
                case hour : {
                    return ValuePool.getInt(hour((Time) parms[0]));
                }
                case identity : {
                    return null;
                }
                case insert : {
                    return insert((String) parms[0],
                                  ((Number) parms[1]).intValue(),
                                  ((Number) parms[2]).intValue(),
                                  (String) parms[3]);
                }
                case isReadOnlyConnection : {
                    return null;
                }
                case isReadOnlyDatabase : {
                    return null;
                }
                case lcase : {
                    return lcase((String) parms[0]);
                }
                case left : {
                    return left((String) parms[0],
                                ((Number) parms[1]).intValue());
                }
                case length : {
                    return length((String) parms[0]);
                }
                case locate : {
                    return ValuePool.getInt(locate((String) parms[0],
                                                   (String) parms[1],
                                                   (Integer) parms[2]));
                }
                case log10 : {
                    return new Double(
                        log10(((Number) parms[0]).doubleValue()));
                }
                case ltrim : {
                    return ltrim((String) parms[0]);
                }
                case minute : {
                    return ValuePool.getInt(minute((Time) parms[0]));
                }
                case mod : {
                    return ValuePool.getInt(
                        mod(
                        ((Number) parms[0]).intValue(),
                        ((Number) parms[1]).intValue()));
                }
                case month : {
                    return ValuePool.getInt(month((Date) parms[0]));
                }
                case monthname : {
                    return monthname((Date) parms[0]);
                }
                case now : {
                    return now();
                }
                case pi : {
                    return piValue;
                }
                case quarter : {
                    return ValuePool.getInt(quarter((Date) parms[0]));
                }
                case rand : {
                    return new Double(rand((Integer) parms[0]));
                }
                case rawToHex : {
                    return rawToHex((String) parms[0]);
                }
                case repeat : {
                    return repeat((String) parms[0], (Integer) parms[1]);
                }
                case replace : {
                    return replace((String) parms[0], (String) parms[1],
                                   (String) parms[2]);
                }
                case right : {
                    return right((String) parms[0],
                                 ((Number) parms[1]).intValue());
                }
                case round : {
                    return new Double(round(((Number) parms[0]).doubleValue(),
                                            ((Number) parms[1]).intValue()));
                }
                case roundMagic : {
                    return new Double(
                        roundMagic(((Number) parms[0]).doubleValue()));
                }
                case rtrim : {
                    return rtrim((String) parms[0]);
                }
                case second : {
                    return ValuePool.getInt(second((Time) parms[0]));
                }
                case sign : {
                    return ValuePool.getInt(
                        sign(((Number) parms[0]).doubleValue()));
                }
                case soundex : {
                    return soundex((String) parms[0]);
                }
                case space : {
                    return space(((Number) parms[0]).intValue());
                }
                case substring : {
                    return substring((String) parms[0],
                                     ((Number) parms[1]).intValue(),
                                     (Integer) parms[2]);
                }
                case truncate : {
                    return new Double(
                        truncate(
                            ((Number) parms[0]).doubleValue(),
                            ((Number) parms[1]).intValue()));
                }
                case ucase : {
                    return ucase((String) parms[0]);
                }
                case user : {
                    return null;
                }
                case week : {
                    return ValuePool.getInt(week((Date) parms[0]));
                }
                case year : {
                    return ValuePool.getInt(year((Date) parms[0]));
                }
                case getCDColumnMetaData : {
                    return getCDColumnMetaData((Connection) parms[0],
                                               (String) parms[1]);
                }
                case isReadOnlyDatabaseFiles : {
                    return null;
                }
                default : {
                    throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED,
                                      "org.hsqldb.Library: fID: " + fID);
                }
            }
        } catch (Exception e) {
            throw wrapException(e);
        }
    }

    static HsqlException wrapException(Exception e) {

        if (e instanceof HsqlException) {
            return (HsqlException) e;
        }

        return Trace.error(Trace.GENERAL_ERROR, e.toString());
    }

    static final String prefix       = "org.hsqldb.Library.";
    static final int    prefixLength = prefix.length();

    static int functionID(String fname) {

        return fname.startsWith(prefix)
               ? functionMap.get(fname.substring(prefixLength), -1)
               : -1;
    }
}
