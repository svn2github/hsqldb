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

import java.math.BigDecimal;
import org.hsqldb.lib.HashMap;
import org.hsqldb.lib.IntValueHashMap;
import org.hsqldb.store.ValuePool;

// fredt@users 20020218 - patch 455785 by hjbusch@users - large DECIMAL inserts
// also Long.MIM_VALUE (bug 473388) inserts - applied to different parts
// fredt@users 20020408 - patch 1.7.0 by fredt - exact integral types
// integral values are cast into the smallest type that can hold them
// fredt@users 20020501 - patch 550970 by boucherb@users - fewer StringBuffers
// fredt@users 20020611 - patch 1.7.0 by fredt - correct statement logging
// changes to the working of getLastPart() to return the correct statement for
// logging in the .script file.
// also restructuring to reduce use of objects and speed up tokenising of
// strings and quoted identifiers
// fredt@users 20021112 - patch 1.7.2 by Nitin Chauhan - use of switch
// rewrite of the majority of multiple if(){}else{} chains with switch(){}
// fredt@users 20030610 - patch 1.7.2 - no StringBuffers

/**
 * Tokenizer class declaration
 *
 *
 * @version 1.7.0
 */

/** @todo fredt - move error and assert string literals to Trace */
class Tokenizer {

    private static final int NO_TYPE   = 0,
                             NAME      = 1,
                             LONG_NAME = 2,
                             SPECIAL   = 3,
                             NUMBER    = 4,
                             FLOAT     = 5,
                             STRING    = 6,
                             LONG      = 7,
                             DECIMAL   = 8,
                             BOOLEAN   = 9,
                             DATE      = 10,
                             TIME      = 11,
                             TIMESTAMP = 12,
                             NULL      = 13;

    // used only internally
    private static final int QUOTED_IDENTIFIER = 14,
                             REMARK_LINE       = 15,
                             REMARK            = 16;
    private String           sCommand;
    private int              iLength;
    private Object           oValue;
    private int              iIndex;
    private int              tokenIndex;
    private int              nextTokenIndex;
    private int              beginIndex;
    private int              iType;
    private String           sToken;
    private String           sLongNameFirst;

//    private String           sLongNameLast;
    private boolean        bWait;
    private static HashMap hKeyword;
    static IntValueHashMap valueTokens;

    static {

        // both maps are used as sets only
        // literals not allowed as table / column names
        hKeyword = new HashMap(67);

        // fredt - if we add MONTH, DAY, YEAR etc. MONTH(), DAY() et al will no longer work
        // following tokens are values

        /* "FALSE",*/
        /* "TRUE",*/
        /* "NULL", */

        /** @todo perhaps rename LEFT() */

        // following token is excluded to allow LEFT() function to work

        /* "LEFT" ,*/
        String keyword[] = {
            Token.T_AS, Token.T_AND, Token.T_ALL, Token.T_AVG, Token.T_BY,
            Token.T_BETWEEN, Token.T_BOTH, Token.T_CALL, Token.T_CASE,
            Token.T_CASEWHEN, Token.T_CAST, Token.T_CONVERT, Token.T_CONCAT,
            Token.T_COUNT, Token.T_COALESCE, Token.T_DISTINCT, Token.T_ELSE,
            Token.T_END, Token.T_EXISTS, Token.T_EXCEPT, Token.T_EXTRACT,
            Token.T_FOR, Token.T_FROM, Token.T_GROUP, Token.T_IF,
            Token.T_INTO, Token.T_IFNULL, Token.T_IS, Token.T_IN,
            Token.T_JOIN, Token.T_INTERSECT, Token.T_INNER, Token.T_LEADING,
            Token.T_LIKE, Token.T_MAX, Token.T_MIN, Token.T_NULLIF,
            Token.T_NOT, Token.T_MINUS, Token.T_ON, Token.T_ORDER, Token.T_OR,
            Token.T_OUTER, Token.T_POSITION, Token.T_PRIMARY, Token.T_SELECT,
            Token.T_SET, Token.T_SUBSTRING, Token.T_SUM, Token.T_THEN,
            Token.T_TO, Token.T_TRAILING, Token.T_TRIM, Token.T_UNIQUE,
            Token.T_UNION, Token.T_VALUES, Token.T_WHEN, Token.T_WHERE,
            Token.T_HAVING
        };

        for (int i = 0; i < keyword.length; i++) {
            hKeyword.put(keyword[i], hKeyword);
        }

        // literals that are values
        valueTokens = new IntValueHashMap(17);

        valueTokens.put(Token.T_NULL, NULL);
        valueTokens.put(Token.T_TRUE, BOOLEAN);
        valueTokens.put(Token.T_FALSE, BOOLEAN);
        valueTokens.put(Token.T_CURRENT_DATE, DATE);
        valueTokens.put(Token.T_CURRENT_TIME, TIME);
        valueTokens.put(Token.T_CURRENT_TIMESTAMP, TIMESTAMP);
        valueTokens.put(Token.T_SYSDATE, DATE);
        valueTokens.put(Token.T_NOW, TIMESTAMP);
        valueTokens.put(Token.T_TODAY, DATE);
    }

    Tokenizer() {}

    Tokenizer(String s) {

        sCommand = s;
        iLength  = s.length();
        iIndex   = 0;
    }

    void reset(String s) {

        sCommand       = s;
        iLength        = s.length();
        iIndex         = 0;
        oValue         = null;
        tokenIndex     = 0;
        nextTokenIndex = 0;
        beginIndex     = 0;
        iType          = NO_TYPE;
        sToken         = null;
        sLongNameFirst = null;

//        sLongNameLast  = null;
        bWait = false;
    }

    /**
     * Method declaration
     *
     *
     * @throws HsqlException
     */
    void back() throws HsqlException {

        Trace.doAssert(!bWait, "back");

        nextTokenIndex = iIndex;
        iIndex         = tokenIndex;
        bWait          = true;
    }

    /**
     * Method declaration
     *
     *
     * @param match
     *
     * @throws HsqlException
     */
    String getThis(String match) throws HsqlException {

        getToken();

        if (!sToken.equals(match)) {
            throw Trace.error(Trace.UNEXPECTED_TOKEN, Trace.TOKEN_REQUIRED,
                              new Object[] {
                sToken, match
            });
        }

        return sToken;
    }

    /**
     * Method declaration
     *
     *
     * @param match
     */
    boolean isGetThis(String match) throws HsqlException {

        getToken();

        if (sToken.equals(match)) {
            return true;
        } else {
            back();

            return false;
        }
    }

    /**
     * This is used solely for user name and password
     *
     *
     * @return
     *
     * @throws HsqlException
     */
    String getStringToken() throws HsqlException {

        getToken();

        switch (iType) {

            case STRING :

                // fred - no longer including first quote in sToken
                return sToken.toUpperCase();

            case NAME :
                return sToken;

            case QUOTED_IDENTIFIER :
                return sToken.toUpperCase();
        }

        throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
    }

    /**
     * this methode is called before other wasXXX methods and takes
     * precedence
     */
    boolean wasValue() {

        switch (iType) {

            case STRING :
            case NUMBER :
            case LONG :
            case FLOAT :
            case DECIMAL :
            case BOOLEAN :
            case DATE :
            case TIME :
            case TIMESTAMP :
            case NULL :
                return true;

            default :
                return false;
        }
    }

    boolean wasQuotedIdentifier() {
        return iType == QUOTED_IDENTIFIER;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    boolean wasLongName() {
        return iType == LONG_NAME;
    }

    /**
     * Name means all quoted and unquoted identifiers plus any word not in the
     * hKeyword list.
     *
     * @return
     */
    boolean wasName() {

        if (iType == QUOTED_IDENTIFIER) {
            return true;
        }

        if (iType != NAME) {
            return false;
        }

        return !hKeyword.containsKey(sToken);
    }

    boolean wasIdentifier() {

        if (iType == QUOTED_IDENTIFIER) {
            return true;
        }

        if (iType != NAME) {
            return false;
        }

        return !hKeyword.containsKey(sToken);
    }

    /**
     * Return first part of long name
     *
     *
     * @return
     */
    String getLongNameFirst() {
        return sLongNameFirst;
    }

    /**
     * Return second part of long name
     *
     *
     * @return
     */
/*
    String getLongNameLast() {
        return sLongNameLast;
    }
*/

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @throws HsqlException
     */
    String getName() throws HsqlException {

        getToken();

        if (!wasName()) {
            throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
        }

        return sToken;
    }

    String getIdentifier() throws HsqlException {

        getToken();

        if (!wasIdentifier()) {
            throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
        }

        return sToken;
    }

    /**
     * Return any token.
     *
     *
     * @return
     *
     * @throws HsqlException
     */
    String getString() throws HsqlException {

        getToken();

        return sToken;
    }

    int getInt() throws HsqlException {

        getToken();

        Object o = getAsValue();
        int    t = getType();

        if (t != Types.INTEGER) {
            throw Trace.error(Trace.WRONG_DATA_TYPE, Types.getTypeString(t));
        }

        return ((Number) o).intValue();
    }

    Object getInType(int type) throws HsqlException {

        getToken();

        Object o = getAsValue();
        int    t = getType();

        if (t != type) {
            throw Trace.error(Trace.WRONG_DATA_TYPE, Types.getTypeString(t));
        }

        return o;
    }

    /**
     *
     *
     *
     * @return
     */
    int getType() {

        // todo: make sure it's used only for Values!
        // todo: synchronize iType with hColumn
        switch (iType) {

            case STRING :
                return Types.VARCHAR;

            case NUMBER :
                return Types.INTEGER;

            case LONG :
                return Types.BIGINT;

            case FLOAT :
                return Types.DOUBLE;

            case DECIMAL :
                return Types.DECIMAL;

            case BOOLEAN :
                return Types.BIT;

            case DATE :
                return Types.DATE;

            case TIME :
                return Types.TIME;

            case TIMESTAMP :
                return Types.TIMESTAMP;

            default :
                return Types.NULL;
        }
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @throws HsqlException
     */
    Object getAsValue() throws HsqlException {

        if (!wasValue()) {
            throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
        }

        switch (iType) {

            case STRING :

                //fredt - no longer returning string with a singlequote as last char
                return sToken;

            case NUMBER :

                // fredt - this returns unsigned values which are later negated.
                // as a result Integer.MIN_VALUE or Long.MIN_VALUE are promoted
                // to a wider type.
                if (sToken.length() < 11) {
                    try {
                        return ValuePool.getInt(Integer.parseInt(sToken));
                    } catch (Exception e1) {}
                }

                if (sToken.length() < 20) {
                    try {
                        iType = LONG;

                        return ValuePool.getLong(Long.parseLong(sToken));
                    } catch (Exception e2) {}
                }

                iType = DECIMAL;

                return new BigDecimal(sToken);

            case FLOAT :

                // jdk 1.1 compat
                // double d = Double.parseDouble(sToken);
                double d = new Double(sToken).doubleValue();
                long   l = Double.doubleToLongBits(d);

                return ValuePool.getDouble(l);

//                return new Double(sToken);
            case DECIMAL :
                return new BigDecimal(sToken);

            case BOOLEAN :
                return org.hsqldb.lib.BooleanConverter.getBoolean(sToken);

            case DATE :
                return HsqlDateTime.dateValue(sToken);

            case TIME :
                return HsqlDateTime.timeValue(sToken);

            case TIMESTAMP :
                return HsqlDateTime.timestampValue(sToken);

            case NULL :
                return null;

            default :
                return sToken;
        }
    }

    /**
     * return the current position to be used for VIEW processing
     *
     * @return
     */
    int getPosition() {
        return iIndex;
    }

    /**
     * mark the current position to be used for future getLastPart() calls
     *
     * @return
     */
    String getPart(int begin, int end) {
        return sCommand.substring(begin, end);
    }

    /**
     * mark the current position to be used for future getLastPart() calls
     *
     * @return
     */
    int getPartMarker() {
        return beginIndex;
    }

    /**
     * mark the current position to be used for future getLastPart() calls
     *
     * @return
     */
    void setPartMarker() {
        beginIndex = iIndex;
    }

    /**
     * mark the position to be used for future getLastPart() calls
     *
     * @return
     */
    void setPartMarker(int position) {
        beginIndex = position;
    }

    /**
     * return part of the command string from the last marked position
     *
     * @return
     */
    String getLastPart() {
        return sCommand.substring(beginIndex, iIndex);
    }

// fredt@users 20020910 - patch 1.7.1 by Nitin Chauhan - rewrite as switch

    /**
     * Method declaration
     *
     *
     * @throws HsqlException
     */
    private void getToken() throws HsqlException {

        if (bWait) {
            bWait  = false;
            iIndex = nextTokenIndex;

            return;
        }

        while (iIndex < iLength
                && Character.isWhitespace(sCommand.charAt(iIndex))) {
            iIndex++;
        }

        sToken     = "";
        tokenIndex = iIndex;

        if (iIndex >= iLength) {
            iType = NO_TYPE;

            return;
        }

        char    c        = sCommand.charAt(iIndex);
        boolean point    = false,
                digit    = false,
                exp      = false,
                afterexp = false;
        boolean end      = false;
        char    cfirst   = 0;

        if (Character.isJavaIdentifierStart(c)) {
            iType = NAME;
        } else if (Character.isDigit(c)) {
            iType = NUMBER;
            digit = true;
        } else {
            switch (c) {

                case '(' :
                case ')' :
                case ',' :
                case '*' :
                case '=' :
                case ';' :
                case '+' :
                case '%' :
                case '?' :
                    iType = SPECIAL;

                    iIndex++;

                    sToken = String.valueOf(c);

                    return;

                case '\"' :
                    iType = QUOTED_IDENTIFIER;

                    iIndex++;

                    sToken = getString('"');

                    if (iIndex == sCommand.length()) {
                        return;
                    }

                    c = sCommand.charAt(iIndex);

                    if (c == '.') {
                        sLongNameFirst = sToken;

                        iIndex++;

// fredt - todo - avoid recursion - this has problems when there is whitespace
// after the dot - the same with NAME
                        getToken();

                        iType = LONG_NAME;
                    }

                    return;

                case '\'' :
                    iType = STRING;

                    iIndex++;

                    sToken = getString('\'');

                    return;

                case '!' :
                case '<' :
                case '>' :
                case '|' :
                case '/' :
                case '-' :
                    cfirst = c;
                    iType  = SPECIAL;
                    break;

                case '.' :
                    iType = DECIMAL;
                    point = true;
                    break;

                default :
                    throw Trace.error(Trace.UNEXPECTED_TOKEN,
                                      String.valueOf(c));
            }
        }

        int start = iIndex++;

        while (true) {
            if (iIndex >= iLength) {
                c   = ' ';
                end = true;

                Trace.check(iType != STRING && iType != QUOTED_IDENTIFIER,
                            Trace.UNEXPECTED_END_OF_COMMAND);
            } else {
                c = sCommand.charAt(iIndex);
            }

            switch (iType) {

                case NAME :
                    if (Character.isJavaIdentifierPart(c)) {
                        break;
                    }

                    // fredt - todo new char[] to back sToken
                    sToken = sCommand.substring(start, iIndex).toUpperCase();

                    if (c == '.') {
                        sLongNameFirst = sToken;

                        iIndex++;

                        getToken();    // todo: eliminate recursion

//                        sLongNameLast = sToken;
                        iType = LONG_NAME;

//                        sToken        = sLongNameFirst + "." + sLongNameLast;
                    } else if (c == '(') {

                        // it is a function call
                    } else {

                        // if in value list then it is a value
                        int type = valueTokens.get(sToken, -1);

                        if (type != -1) {
                            iType = type;
                        }
                    }

                    return;

                case QUOTED_IDENTIFIER :
                case STRING :

                    // shouldn't get here
                    break;

                case REMARK :
                    if (end) {

                        // unfinished remark
                        // maybe print error here
                        iType = NO_TYPE;

                        return;
                    } else if (c == '*') {
                        iIndex++;

                        if (iIndex < iLength
                                && sCommand.charAt(iIndex) == '/') {

                            // using recursion here
                            iIndex++;

                            getToken();

                            return;
                        }
                    }
                    break;

                case REMARK_LINE :
                    if (end) {
                        iType = NO_TYPE;

                        return;
                    } else if (c == '\r' || c == '\n') {

                        // using recursion here
                        getToken();

                        return;
                    }
                    break;

                case SPECIAL :
                    if (c == '/' && cfirst == '/') {
                        iType = REMARK_LINE;

                        break;
                    } else if (c == '-' && cfirst == '-') {
                        iType = REMARK_LINE;

                        break;
                    } else if (c == '*' && cfirst == '/') {
                        iType = REMARK;

                        break;
                    } else if (c == '>' || c == '=' || c == '|') {
                        break;
                    }

                    sToken = sCommand.substring(start, iIndex);

                    return;

                case NUMBER :
                case FLOAT :
                case DECIMAL :
                    if (Character.isDigit(c)) {
                        digit = true;
                    } else if (c == '.') {
                        iType = DECIMAL;

                        if (point) {
                            throw Trace.error(Trace.UNEXPECTED_TOKEN, ".");
                        }

                        point = true;
                    } else if (c == 'E' || c == 'e') {
                        if (exp) {
                            throw Trace.error(Trace.UNEXPECTED_TOKEN, "E");
                        }

                        // HJB-2001-08-2001 - now we are sure it's a float
                        iType = FLOAT;

                        // first character after exp may be + or -
                        afterexp = true;
                        point    = true;
                        exp      = true;
                    } else if (c == '-' && afterexp) {
                        afterexp = false;
                    } else if (c == '+' && afterexp) {
                        afterexp = false;
                    } else {
                        afterexp = false;

                        if (!digit) {
                            if (point && start == iIndex - 1) {
                                sToken = ".";
                                iType  = SPECIAL;

                                return;
                            }

                            throw Trace.error(Trace.UNEXPECTED_TOKEN,
                                              String.valueOf(c));
                        }

                        sToken = sCommand.substring(start, iIndex);

                        return;
                    }
            }

            iIndex++;
        }
    }

// fredt - strings are constructed from new char[] objects to avoid slack
// because these strings might end up as part of internal data structures
// or table elements.
// we may consider using pools to avoid recreating the strings
    private String getString(char quoteChar) throws HsqlException {

        try {
            int     nextIndex   = iIndex;
            boolean quoteInside = false;

            for (;;) {
                nextIndex = sCommand.indexOf(quoteChar, nextIndex);

                if (nextIndex < 0) {
                    throw Trace.error(Trace.UNEXPECTED_END_OF_COMMAND);
                }

                if (nextIndex < iLength - 1
                        && sCommand.charAt(nextIndex + 1) == quoteChar) {
                    quoteInside = true;
                    nextIndex   += 2;

                    continue;
                }

                break;
            }

            char[] chBuffer = new char[nextIndex - iIndex];

            sCommand.getChars(iIndex, nextIndex, chBuffer, 0);

            int j = chBuffer.length;

            if (quoteInside) {
                j = 0;

                // fredt - loop assumes all occurences of quoteChar are paired
                // this has already been checked by the preprocessing loop
                for (int i = 0; i < chBuffer.length; i++, j++) {
                    if (chBuffer[i] == quoteChar) {
                        i++;
                    }

                    chBuffer[j] = chBuffer[i];
                }
            }

            iIndex = ++nextIndex;

            return new String(chBuffer, 0, j);
        } catch (HsqlException e) {
            throw e;
        } catch (Exception e) {
            e.getMessage();
        }

        return null;
    }

// fredt@users 20020420 - patch523880 by leptipre@users - VIEW support

    /**
     * Method declaration
     *
     *
     * @param s
     */
    void setString(String s, int pos) {

        sCommand = s;
        iLength  = s.length();
        bWait    = false;
        iIndex   = pos;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    int getLength() {
        return iLength;
    }

// boucherb@users - patch 1.7.2 - convenience method to allow parser to perform
// atomic test and throw in places where parameter tokens are illegal, i.e.
// column lists items, table list items, as aliases, etc.

    /**
     * A check for whether or not a parameter token ('?') is unexpected
     * in the current tokenizing context
     *
     * @param msg to display in exception if check determines
     * @throws HsqlException if current token string value is '?' and the
     * class of the token is not SPECIAL
     */
    void checkUnexpectedParam(String msg) throws HsqlException {

        if ("?".equals(sToken)) {
            Trace.check(iType != SPECIAL, Trace.UNEXPECTED_TOKEN, msg);
        }
    }
}
