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

import java.io.IOException;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.util.NoSuchElementException;
import org.hsqldb.lib.Iterator;

// fredt@users 20020130 - patch 1.7.0 by fredt
// to ensure consistency of r.rTail r.iSize in all operations
// methods for set operations moved here from Select.java
// tony_lai@users 20020820 - patch 595073 - duplicated exception msg
// fredt@users 20030801 - patch 1.7.2 - separate metadata and polymophic serialisation

/**
 *  Class declaration
 *
 * @version    1.7.2
 */
class Result {

    // record list
    Record         rRoot;
    private Record rTail;
    private int    iSize;

    // transient - number of significant columns
    private int significantColumns;

    // type of result
    int iMode;

//    boolean isMulti;
    // database ID
    int databaseID;

    // session ID
    int sessionID;

    // user / password or error strings
    String mainString;
    String subString;

    // database name
    String subSubString;

    // prepared statement id / error vendor code
    int statementID;

    // max rows (out) or update count (in)
    int            iUpdateCount;
    ResultMetaData metaData;

    static class ResultMetaData {

        // always resolved
        String  sLabel[];
        String  sTable[];
        String  sName[];
        boolean isLabelQuoted[];
        int     colType[];
        int     colSize[];
        int     colScale[];

        // extra attrs, sometimes resolved
        String    sCatalog[];
        String    sSchema[];
        int       nullability[];
        boolean   isIdentity[];
        boolean[] isWritable;
        int       paramMode[];

        // It's possible to do better than java.lang.Object
        // for type OTHER if the expression generating the value
        // is of type FUNCTION.  This applies to result set columns
        // whose value is the result of a SQL function call and
        // especially to the arguments and return value of a CALL
        String  sClassName[];
        boolean isParameterDescription;

        ResultMetaData() {}

        ResultMetaData(int n) {
            prepareData(n);
        }

        /**
         *  Method declaration
         *
         * @param  columns
         */
        private void prepareData(int columns) {

            sLabel        = new String[columns];
            sTable        = new String[columns];
            sName         = new String[columns];
            isLabelQuoted = new boolean[columns];
            colType       = new int[columns];
            colSize       = new int[columns];
            colScale      = new int[columns];
            sCatalog      = new String[columns];
            sSchema       = new String[columns];
            nullability   = new int[columns];
            isIdentity    = new boolean[columns];
            isWritable    = new boolean[columns];
            sClassName    = new String[columns];
        }

        int[] getParameterTypes() {
            return colType;
        }

        boolean isTableColumn(int i) {
            return sTable[i] != null && sTable[i].length() > 0
                   && sName[i] != null && sName[i].length() > 0;
        }

        private void decodeTableColumnAttrs(int in, int i) {

            nullability[i] = in & 0x0000000f;
            isIdentity[i]  = (in & 0x00000010) != 0;
            isWritable[i]  = (in & 0x00000020) != 0;
        }

        private void writeTableColumnAttrs(DatabaseRowOutputInterface out,
                                           int i)
                                           throws IOException, HsqlException {

            // Currently, HSQLDB accepts and logs (precision, scale)
            // for all types, which is not to the spec.
            // HSQLDB also ignores precision and scale for all types except
            // XXXCHAR, for which it may (or may not) perform some trimming/padding.
            // All in all, it's currently meaningless (indeed misleading) to
            // transmit and report the values, as the data typically will
            // not be constrained accordingly.
//        switch(colType[i]) {
//            // As early as SQL 92, these are allowed to have a scale.
//            // However, DatabaseCommandInterpreter.processCreateColumn
//            // does not currently handle this correctly and will assign
//            // a precision instead of a scale if TIME(s) or TIMESTAMP(s)
//            // is specified
//            case Types.TIME :
//            case Types.TIMESTAMP :
//                  out.writeIntData(colScale[i]);
//                  break;
//            case Types.DECIMAL :
//            case Types.NUMERIC : {
//                out.writeIntData(colScale[i]);
//            } // fall through
//            // Apparently, SQL 92 specifies that FLOAT can have
//            // a declared precision, which is typically the number of
//            // bits (not binary digits).  In any case, this is somewhat
//            // meaningless under HSQLDB/Java, in that we use java.lang.Double
//            // to represent SQL FLOAT
//            case Types.FLOAT :
//            // It's legal to declare precision for these, although HSQLDB
//            // currently does not use it to constrain values
//            case Types.BINARY :
//            case Types.VARBINARY :
//            case Types.LONGVARBINARY :
//            // possibly, but not universally acted upon (trimmming/padding)
//            case Types.CHAR  :
//            case Types.VARCHAR :
//            case Types.LONGVARCHAR : {
//                out.writeIntData(colSize[i]);
//            }
//        }
            out.writeIntData(encodeTableColumnAttrs(i));
            out.writeString(sCatalog[i] == null ? ""
                                                : sCatalog[i]);
            out.writeString(sSchema[i] == null ? ""
                                               : sSchema[i]);
        }

        private int encodeTableColumnAttrs(int i) {

            int out = nullability[i];    // always between 0x00 and 0x02

            if (isIdentity[i]) {
                out |= 0x00000010;
            }

            if (isWritable[i]) {
                out |= 0x00000020;
            }

            return out;
        }

        private void readTableColumnAttrs(DatabaseRowInputInterface in,
                                          int i)
                                          throws IOException, HsqlException {

// no point in transmitting these yet
// if ever implemented, must follow logic of switch as outlined in comments
// for corresponding write method
//        colScale[i] = in.readIntData();
//        colSize[i] = in.readIntData();
            decodeTableColumnAttrs(in.readIntData(), i);

            sCatalog[i] = in.readString();
            sSchema[i]  = in.readString();
        }

        void read(DatabaseRowInputInterface in)
        throws HsqlException, IOException {

            int l = in.readIntData();

            prepareData(l);

            if (isParameterDescription) {
                paramMode = new int[l];
            }

            for (int i = 0; i < l; i++) {
                colType[i]    = in.readType();
                sLabel[i]     = in.readString();
                sTable[i]     = in.readString();
                sName[i]      = in.readString();
                sClassName[i] = in.readString();

                if (isTableColumn(i)) {
                    readTableColumnAttrs(in, i);
                }

                if (isParameterDescription) {
                    paramMode[i] = in.readIntData();
                }
            }
        }

        void write(DatabaseRowOutputInterface out,
                   int colCount) throws HsqlException, IOException {

            out.writeIntData(colCount);

            for (int i = 0; i < colCount; i++) {
                out.writeType(colType[i]);

                // CAREFUL: writeString will throw NPE if passed NULL
                // There is no guarantee that these will all be non-null
                // and there's no point in hanging network communications
                // or doing a big rewrite for null-safety over something
                // like this.  We could explicitly do a writeNull here if
                // detected null, but, frankly, readString on the other
                // end will simply turn it into a zero-length string
                // anyway, as nulls are only handled "properly" by
                // readData(...), not by the individual readXXX methods.
                out.writeString(sLabel[i] == null ? ""
                                                  : sLabel[i]);
                out.writeString(sTable[i] == null ? ""
                                                  : sTable[i]);
                out.writeString(sName[i] == null ? ""
                                                 : sName[i]);
                out.writeString(sClassName[i] == null ? ""
                                                      : sClassName[i]);

                if (isTableColumn(i)) {
                    writeTableColumnAttrs(out, i);
                }

                if (isParameterDescription) {
                    out.writeIntData(paramMode[i]);
                }
            }
        }
    }

    /**
     *  General constructor
     */
    Result(int type) {

        iMode = type;

/*
        if (type == ResultConstants.MULTI) {
            isMulti = true;
        }
*/
        if (type == ResultConstants.DATA
                || type == ResultConstants.PARAM_META_DATA
                || type == ResultConstants.SQLEXECUTE
                || type == ResultConstants.SETSESSIONATTR) {
            metaData = new ResultMetaData();
        }
    }

    Result(ResultMetaData md) {

        iMode              = ResultConstants.DATA;
        significantColumns = md.colType.length;
        metaData           = md;
    }

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)

    /**
     *  Constructor for errors
     *
     * @param  error error message
     * @param  state   sql state
     * @param  code   vendor code
     */
    Result(String error, String state, int code) {

        iMode        = ResultConstants.ERROR;
        mainString   = error;
        subString    = state;
        statementID  = code;
        subSubString = "";
    }

    /**
     *  Only used with DATA and PARAM_META_DATA results
     *
     * @param  columns
     */
    Result(int type, int columns) {

        metaData = new ResultMetaData();

        metaData.prepareData(columns);

        if (type == ResultConstants.PARAM_META_DATA) {
            metaData.isParameterDescription = true;
            metaData.paramMode              = new int[columns];
        }

        iMode              = type;
        significantColumns = columns;
    }

    /**
     * For BATCHEXECUTE and BATCHEXECDIRECT
     */
    Result(int type, int types[], int id) {

        iMode              = type;
        metaData           = new ResultMetaData();
        metaData.colType   = types;
        significantColumns = types.length;
        statementID        = id;
    }

    /**
     *  Constructor declaration
     *
     * @param  b
     * @exception  HsqlException  Description of the Exception
     */
    Result(DatabaseRowInputInterface in) throws HsqlException {

        try {
            iMode = in.readIntData();

            if (iMode == ResultConstants.MULTI) {
                readMultiResult(in);

                return;
            }

            databaseID = in.readIntData();
            sessionID  = in.readIntData();

            switch (iMode) {

                case ResultConstants.GETSESSIONATTR :
                case ResultConstants.SQLDISCONNECT :
                case ResultConstants.SQLSTARTTRAN :
                    break;

                case ResultConstants.SQLPREPARE :
                    setStatementType(in.readIntData());

                    mainString = in.readString();
                    break;

                case ResultConstants.PREPARE_ACK :
                case ResultConstants.SQLFREESTMT :
                    statementID = in.readIntData();
                    break;

                case ResultConstants.SQLEXECDIRECT :
                    statementID = in.readIntData();
                    mainString  = in.readString();
                    break;

                case ResultConstants.ERROR :
                case ResultConstants.SQLCONNECT :
                    mainString   = in.readString();
                    subString    = in.readString();
                    subSubString = in.readString();
                    statementID  = in.readIntData();

//                    throw Trace.getError(string, code);
                    break;

                case ResultConstants.UPDATECOUNT :
                    iUpdateCount = in.readIntData();
                    break;

                case ResultConstants.SQLENDTRAN : {
                    int type = in.readIntData();

                    setEndTranType(type);                    // endtran type

                    switch (type) {

                        case ResultConstants.SAVEPOINT_NAME_RELEASE :
                        case ResultConstants.SAVEPOINT_NAME_ROLLBACK :
                            mainString = in.readString();    // savepoint name

                        //  default: throw - case never happens
                    }

                    break;
                }
                case ResultConstants.BATCHEXECUTE :
                case ResultConstants.BATCHEXECDIRECT :
                case ResultConstants.SQLEXECUTE :
                case ResultConstants.SETSESSIONATTR : {
                    if (iMode == ResultConstants.SQLEXECUTE
                            || iMode == ResultConstants.BATCHEXECUTE) {
                        statementID = in.readIntData();
                    } else {
                        iUpdateCount = in.readIntData();
                    }

                    int l = in.readIntData();

                    metaData           = new ResultMetaData(l);
                    significantColumns = l;

                    for (int i = 0; i < l; i++) {
                        metaData.colType[i] = in.readType();
                    }

                    int count = in.readIntData();

                    while (count-- > 0) {
                        add(in.readData(metaData.colType));
                    }

                    break;
                }
                case ResultConstants.DATA :
                case ResultConstants.PARAM_META_DATA : {
                    metaData = new ResultMetaData();
                    metaData.isParameterDescription =
                        iMode == ResultConstants.PARAM_META_DATA;

                    metaData.read(in);

                    significantColumns = metaData.sLabel.length;

                    int count = in.readIntData();

                    while (count-- > 0) {
                        add(in.readData(metaData.colType));
                    }

                    break;
                }
                case ResultConstants.SQLSETCONNECTATTR : {
                    int type = in.readIntData();             // attr type

                    setConnectionAttrType(type);

                    switch (type) {

                        case ResultConstants.SQL_ATTR_SAVEPOINT_NAME :
                            mainString = in.readString();    // savepoint name

                        //  case ResultConstants.SQL_ATTR_AUTO_IPD :
                        //      - always true
                        //  default: throw - case never happens
                    }

                    break;
                }
                default :
                    throw new HsqlException(
                        Trace.getMessage(
                            Trace.Result_Result, true, new Object[]{
                                new Integer(iMode) }), null, 0);
            }
        } catch (IOException e) {
            throw Trace.error(Trace.TRANSFER_CORRUPTED);
        }
    }

    static Result newSingleColumnResult(String colName, int colType) {

        Result result = new Result(ResultConstants.DATA, 1);

        result.metaData.sName[0]   = colName;
        result.metaData.sLabel[0]  = colName;
        result.metaData.sTable[0]  = "";
        result.metaData.colType[0] = colType;

        return result;
    }

    static Result newPrepareResponse(int csid, Result rsmd, Result pmd) {

        Result out;
        Result pack;

        out = new Result(ResultConstants.MULTI);

//        out.isMulti      = true;
        pack             = new Result(ResultConstants.PREPARE_ACK);
        pack.statementID = csid;

        out.add(new Object[]{ pack });
        out.add(new Object[]{ rsmd });
        out.add(new Object[]{ pmd });

        return out;
    }

    static Result newParameterDescriptionResult(int len) {

        Result r = new Result(ResultConstants.PARAM_META_DATA, len);

        r.metaData.isParameterDescription = true;
        r.metaData.paramMode              = new int[len];

        return r;
    }

    static Result newFreeStmtRequest(int statementID) {

        Result r = new Result(ResultConstants.SQLFREESTMT);

        r.statementID = statementID;

        return r;
    }

    static Result newExecuteDirectRequest(String sql) {

        Result out;

        out = new Result(ResultConstants.SQLEXECDIRECT);

        out.setMainString(sql);

        return out;
    }

    static Result newReleaseSavepointRequest(String name) {

        Result out;

        out = new Result(ResultConstants.SQLENDTRAN);

        out.setMainString(name);
        out.setEndTranType(ResultConstants.SAVEPOINT_NAME_RELEASE);

        return out;
    }

    static Result newRollbackToSavepointRequest(String name) {

        Result out;

        out = new Result(ResultConstants.SQLENDTRAN);

        out.setMainString(name);
        out.setEndTranType(ResultConstants.SAVEPOINT_NAME_ROLLBACK);

        return out;
    }

    static Result newSetSavepointRequest(String name) {

        Result out;

        out = new Result(ResultConstants.SQLSETCONNECTATTR);

        out.setConnectionAttrType(ResultConstants.SQL_ATTR_SAVEPOINT_NAME);
        out.setMainString(name);

        return out;
    }

    /**
     *  Method declaration
     *
     * @return
     */
    int getSize() {
        return iSize;
    }

    /**
     *  Method declaration
     *
     * @param  columns
     */
    void setColumnCount(int columns) {
        significantColumns = columns;
    }

    /**
     *  Method declaration
     *
     * @return
     */
    int getColumnCount() {
        return significantColumns;
    }

    /**
     *  Method declaration
     *
     * @param  a
     */
    void append(Result a) {

        if (rRoot == null) {
            rRoot = a.rRoot;
        } else {
            rTail.next = a.rRoot;
        }

        rTail = a.rTail;
        iSize += a.iSize;
    }

    void addAll(Result r) {

        if (r == null) {
            return;
        }

        Record from = r.rRoot;

        while (from != null) {
            add(from.data);

            from = from.next;
        }
    }

    /**
     *  Method declaration
     *
     * @param  a
     */
    void setRows(Result a) {

        if (a == null) {
            rRoot = null;
            rTail = null;
            iSize = 0;
        } else {
            rRoot = a.rRoot;
            rTail = a.rTail;
            iSize = a.iSize;
        }
    }

    /**
     *  Method declaration
     *
     * @param  d
     */
    void add(Object d[]) {

        Record r = new Record();

        r.data = d;

        if (rRoot == null) {
            rRoot = r;
        } else {
            rTail.next = r;
        }

        rTail = r;

        iSize++;
    }

    /**
     *  Method declaration
     *
     * @param  limitstart  number of records to discard at the head
     * @param  limitcount  number of records to keep, all the rest if 0
     */

// fredt@users 20020130 - patch 1.7.0 by fredt
// rewritten and moved from Select.java
    void trimResult(int limitstart, int limitcount) {

        Record n = rRoot;

        if (n == null) {
            return;
        }

        if (limitstart >= iSize) {
            iSize = 0;
            rRoot = rTail = null;

            return;
        }

        iSize -= limitstart;

        for (int i = 0; i < limitstart; i++) {
            n = n.next;

            if (n == null) {

                // if iSize is consistent this block will never be reached
                iSize = 0;
                rRoot = rTail = n;

                return;
            }
        }

        rRoot = n;

        if (limitcount == 0 || limitcount >= iSize) {
            return;
        }

        for (int i = 1; i < limitcount; i++) {
            n = n.next;

            if (n == null) {

                // if iSize is consistent this block will never be reached
                return;
            }
        }

        iSize  = limitcount;
        n.next = null;
        rTail  = n;
    }

    /**
     * Removes duplicate rows on the basis of comparing the singificant
     * columns of the rows in the result.
     *
     * @throws  HsqlException
     */
    void removeDuplicates() throws HsqlException {
        removeDuplicates(significantColumns);
    }

    /**
     * Removes duplicate rows on the basis of comparing the first columnCount
     * columns of rows in the result.
     *
     * @throws  HsqlException
     */

// fredt@users 20020130 - patch 1.7.0 by fredt
// to ensure consistency of r.rTail r.iSize in all set operations
    void removeDuplicates(int columnCount) throws HsqlException {

        if (rRoot == null) {
            return;
        }

        int order[] = new int[columnCount];
        int way[]   = new int[columnCount];

        for (int i = 0; i < columnCount; i++) {
            order[i] = i;
            way[i]   = 1;
        }

        sortResult(order, way);

        Record n = rRoot;

        for (;;) {
            Record next = n.next;

            if (next == null) {
                break;
            }

            if (compareRecord(n.data, next.data, columnCount) == 0) {
                n.next = next.next;

                iSize--;
            } else {
                n = next;
            }
        }

        rTail = n;

        Trace.doAssert(rTail.next == null,
                       "rTail not correct in Result.removeDuplicates iSize =",
                       iSize);
    }

    /**
     *  Removes duplicates then removes the contents of the second result
     *  from this one base on first columnCount of the rows in each result.
     *
     * @param  minus
     * @throws  HsqlException
     */
    void removeSecond(Result minus, int columnCount) throws HsqlException {

        removeDuplicates(columnCount);
        minus.removeDuplicates(columnCount);

        Record  n     = rRoot;
        Record  last  = rRoot;
        boolean rootr = true;    // checking rootrecord
        Record  n2    = minus.rRoot;
        int     i     = 0;

        while (n != null && n2 != null) {
            i = compareRecord(n.data, n2.data, columnCount);

            if (i == 0) {
                if (rootr) {
                    rRoot = last = n.next;
                } else {
                    last.next = n.next;
                }

                n = n.next;

                iSize--;
            } else if (i > 0) {    // r > minus
                n2 = n2.next;
            } else {               // r < minus
                last  = n;
                rootr = false;
                n     = n.next;
            }
        }

        for (; n != null; ) {
            last = n;
            n    = n.next;
        }

        rTail = last;

        Trace.doAssert(
            (rRoot == null && rTail == null) || rTail.next == null,
            "rTail not correct in Result.removeSecond iSize =", iSize);
    }

    /**
     * Removes all duplicate rows then removes all rows that are not shared
     * between this and the other result, based on comparing the first
     * columnCount columns of each result.
     *
     * @param  r2
     * @throws  HsqlException
     */
    void removeDifferent(Result r2, int columnCount) throws HsqlException {

        removeDuplicates(columnCount);
        r2.removeDuplicates(columnCount);

        Record  n     = rRoot;
        Record  last  = rRoot;
        boolean rootr = true;    // checking rootrecord
        Record  n2    = r2.rRoot;
        int     i     = 0;

        iSize = 0;

        while (n != null && n2 != null) {
            i = compareRecord(n.data, n2.data, columnCount);

            if (i == 0) {             // same rows
                if (rootr) {
                    rRoot = n;        // make this the first record
                } else {
                    last.next = n;    // this is next record in resultset
                }

                rootr = false;
                last  = n;            // this is last record in resultset
                n     = n.next;
                n2    = n2.next;

                iSize++;
            } else if (i > 0) {       // r > r2
                n2 = n2.next;
            } else {                  // r < r2
                n = n.next;
            }
        }

        if (rootr) {             // if no lines in resultset
            rRoot = null;        // then return null
            last  = null;
        } else {
            last.next = null;    // else end resultset
        }

        rTail = last;

        Trace.doAssert(
            (rRoot == null && rTail == null) || rTail.next == null,
            "rTail not correct in Result.removeDifference iSize =", iSize);
    }

    /**
     *  Method declaration
     *
     * @param  order
     * @param  way
     * @throws  HsqlException
     */
    void sortResult(int order[], int way[]) throws HsqlException {

        if (rRoot == null || rRoot.next == null) {
            return;
        }

        Record source0, source1;
        Record target[]     = new Record[2];
        Record targetlast[] = new Record[2];
        int    dest         = 0;
        Record n            = rRoot;

        while (n != null) {
            Record next = n.next;

            n.next       = target[dest];
            target[dest] = n;
            n            = next;
            dest         ^= 1;
        }

        for (int blocksize = 1; target[1] != null; blocksize <<= 1) {
            source0   = target[0];
            source1   = target[1];
            target[0] = target[1] = targetlast[0] = targetlast[1] = null;

            for (dest = 0; source0 != null; dest ^= 1) {
                int n0 = blocksize,
                    n1 = blocksize;

                while (true) {
                    if (n0 == 0 || source0 == null) {
                        if (n1 == 0 || source1 == null) {
                            break;
                        }

                        n       = source1;
                        source1 = source1.next;

                        n1--;
                    } else if (n1 == 0 || source1 == null) {
                        n       = source0;
                        source0 = source0.next;

                        n0--;
                    } else if (compareRecord(
                            source0.data, source1.data, order, way) > 0) {
                        n       = source1;
                        source1 = source1.next;

                        n1--;
                    } else {
                        n       = source0;
                        source0 = source0.next;

                        n0--;
                    }

                    if (target[dest] == null) {
                        target[dest] = n;
                    } else {
                        targetlast[dest].next = n;
                    }

                    targetlast[dest] = n;
                    n.next           = null;
                }
            }
        }

        rRoot = target[0];
        rTail = targetlast[0];

        Trace.doAssert(rTail.next == null,
                       "rTail not correct in Result.sortResult iSize =",
                       iSize);
    }

    /**
     *  Method declaration
     *
     * @param  a
     * @param  b
     * @param  order
     * @param  way
     * @return
     * @throws  HsqlException
     */
    private int compareRecord(Object a[], Object b[], int order[],
                              int way[]) throws HsqlException {

        int i = Column.compare(a[order[0]], b[order[0]],
                               metaData.colType[order[0]]);

        if (i == 0) {
            for (int j = 1; j < order.length; j++) {
                i = Column.compare(a[order[j]], b[order[j]],
                                   metaData.colType[order[j]]);

                if (i != 0) {
                    return i * way[j];
                }
            }
        }

        return i * way[0];
    }

    /**
     *  Method declaration
     *
     * @param  a
     * @param  b
     * @param  len
     * @return
     * @throws  HsqlException
     */
    private int compareRecord(Object a[], Object b[],
                              int len) throws HsqlException {

        for (int j = 0; j < len; j++) {
            int i = Column.compare(a[j], b[j], metaData.colType[j]);

            if (i != 0) {
                return i;
            }
        }

        return 0;
    }

    void write(DatabaseRowOutputInterface out)
    throws IOException, HsqlException {

//        if (isMulti) {
        if (iMode == ResultConstants.MULTI) {
            writeMulti(out);

            return;
        }

        int startPos = out.size();

        out.writeSize(0);
        out.writeIntData(iMode);
        out.writeIntData(databaseID);
        out.writeIntData(sessionID);

        switch (iMode) {

            case ResultConstants.GETSESSIONATTR :
            case ResultConstants.SQLDISCONNECT :
            case ResultConstants.SQLSTARTTRAN :
                break;

            case ResultConstants.SQLPREPARE :

                // Allows the engine side to fast-fail prepare of non-CALL
                // statement against a CallableStatement object and CALL
                // statement against PreparedStatement.
                //
                // May be useful in the future for other things
                out.writeIntData(getStatementType());
                out.writeString(mainString);
                break;

            case ResultConstants.PREPARE_ACK :
            case ResultConstants.SQLFREESTMT :
                out.writeIntData(statementID);
                break;

            case ResultConstants.SQLEXECDIRECT :
                out.writeIntData(statementID);          // currently unused
                out.writeString(mainString);
                break;

            case ResultConstants.ERROR :
            case ResultConstants.SQLCONNECT :
                out.writeString(mainString);
                out.writeString(subString);
                out.writeString(subSubString);
                out.writeIntData(statementID);
                break;

            case ResultConstants.UPDATECOUNT :
                out.writeIntData(iUpdateCount);
                break;

            case ResultConstants.SQLENDTRAN : {
                int type = getEndTranType();

                out.writeIntData(type);                 // endtran type

                switch (type) {

                    case ResultConstants.SAVEPOINT_NAME_RELEASE :
                    case ResultConstants.SAVEPOINT_NAME_ROLLBACK :
                        out.writeString(mainString);    // savepoint name

                    // default; // do nothing
                }

                break;
            }
            case ResultConstants.BATCHEXECUTE :
            case ResultConstants.BATCHEXECDIRECT :
            case ResultConstants.SQLEXECUTE :
            case ResultConstants.SETSESSIONATTR : {
                out.writeIntData(
                    iMode == ResultConstants.SQLEXECUTE
                    || iMode == ResultConstants.BATCHEXECUTE ? statementID
                                                             : iUpdateCount);

                int l = significantColumns;

                out.writeIntData(l);

                for (int i = 0; i < l; i++) {
                    out.writeType(metaData.colType[i]);
                }

                out.writeIntData(iSize);

                Record n = rRoot;

                while (n != null) {
                    out.writeData(l, metaData.colType, n.data, null, false);

                    n = n.next;
                }

                break;
            }
            case ResultConstants.DATA :
            case ResultConstants.PARAM_META_DATA : {
                metaData.write(out, significantColumns);
                out.writeIntData(iSize);

                Record n = rRoot;

                while (n != null) {
                    out.writeData(significantColumns, metaData.colType,
                                  n.data, null, false);

                    n = n.next;
                }

                break;
            }
            case ResultConstants.SQLSETCONNECTATTR : {
                int type = getConnectionAttrType();

                out.writeIntData(type);                 // attr type

                switch (type) {

                    case ResultConstants.SQL_ATTR_SAVEPOINT_NAME :
                        out.writeString(mainString);    // savepoint name

                    // case ResultConstants.SQL_ATTR_AUTO_IPD // always true
                    // default: // throw, but case never happens
                }

                break;
            }
            default :
                throw new HsqlException(
                    Trace.getMessage(
                        Trace.Result_Result, true, new Object[]{
                            new Integer(iMode) }), null, 0);
        }

        out.writeIntData(out.size(), startPos);
    }

    void readMultiResult(DatabaseRowInputInterface in)
    throws HsqlException, IOException {

        iMode      = ResultConstants.MULTI;
        databaseID = in.readIntData();
        sessionID  = in.readIntData();

        int count = in.readIntData();

        for (int i = 0; i < count; i++) {

            // Currently required for the outer result, but can simply
            // be ignored for sub-results
            in.readIntData();
            add(new Object[]{ new Result(in) });
        }
    }

    private void writeMulti(DatabaseRowOutputInterface out)
    throws IOException, HsqlException {

        int startPos = out.size();

        out.writeSize(0);
        out.writeIntData(iMode);
        out.writeIntData(databaseID);
        out.writeIntData(sessionID);
        out.writeIntData(iSize);

        Record n = rRoot;

        while (n != null) {
            ((Result) n.data[0]).write(out);

            n = n.next;
        }

        out.writeIntData(out.size(), startPos);
    }

    /**
     * Convenience method for writing, shared by Server side.
     */
    static void write(Result r, DatabaseRowOutputInterface rowout,
                      OutputStream dataout)
                      throws IOException, HsqlException {

        rowout.reset();
        r.write(rowout);
        dataout.write(rowout.getOutputStream().getBuffer(), 0,
                      rowout.getOutputStream().size());
        dataout.flush();
    }

    /**
     * Convenience method for reading, shared by Server side.
     */
    static Result read(DatabaseRowInputInterface rowin,
                       DataInputStream datain)
                       throws IOException, HsqlException {

        int length = datain.readInt();

        rowin.resetRow(0, length);

        byte[] byteArray = rowin.getBuffer();
        int    offset    = 4;

        for (; offset < length; ) {
            int count = datain.read(byteArray, offset, length - offset);

            if (count < 0) {
                throw new IOException();
            }

            offset += count;
        }

        return new Result(rowin);
    }

// boucerb@users 20030513
// ------------------- patch 1.7.2 --------------------
    Result(Throwable t, String statement) {

        iMode = ResultConstants.ERROR;

        if (t instanceof HsqlException) {
            HsqlException he = (HsqlException) t;

            subString  = he.state;
            mainString = he.message;

            if (statement != null) {
                mainString += " in statement [" + statement + "]";
            }

            statementID = he.code;
        } else if (t instanceof Exception) {
            t.printStackTrace();

            subString  = "S1000";
            mainString = Trace.getMessage(Trace.GENERAL_ERROR) + " " + t;

            if (statement != null) {
                mainString += " in statement [" + statement + "]";
            }

            statementID = Trace.GENERAL_ERROR;
        } else if (t instanceof OutOfMemoryError) {

            // At this point, we've nothing to lose by doing this
            System.gc();
            t.printStackTrace();

            subString   = "S1000";
            mainString  = "out of memory";
            statementID = Trace.OUT_OF_MEMORY;
        }

        subSubString = "";
    }

    int getStatementID() {
        return statementID;
    }

    void setStatementID(int id) {
        statementID = id;
    }

    String getMainString() {
        return mainString;
    }

    void setMainString(String sql) {
        mainString = sql;
    }

    String getSubString() {
        return subString;
    }

    void setMaxRows(int count) {
        this.iUpdateCount = count;
    }

    int getUpdateCount() {
        return iUpdateCount;
    }

    int getConnectionAttrType() {
        return iUpdateCount;
    }

    void setConnectionAttrType(int type) {
        iUpdateCount = type;
    }

    int getEndTranType() {
        return iUpdateCount;
    }

    void setEndTranType(int type) {
        iUpdateCount = type;
    }

    /** @todo fred - check this repurposing */
    int[] getUpdateCounts() {
        return metaData.colType;
    }

    Object[] getParameterData() {
        return (rRoot == null) ? null
                               : rRoot.data;
    }

    void setParameterData(Object[] data) {

        if (rRoot == null) {
            rRoot = new Record();
        }

        rRoot.data = data;
        rRoot.next = null;
        rTail      = rRoot;
        iSize      = 1;
    }

    void setResultType(int type) {
        iMode = type;
    }

    void setStatementType(int type) {
        iUpdateCount = type;
    }

    int getStatementType() {
        return iUpdateCount;
    }

    Iterator iterator() {
        return new ResultIterator();
    }

    private class ResultIterator implements Iterator {

        boolean removed;
        int     counter;
        Record  current = rRoot;
        Record  last;

        public boolean hasNext() {
            return counter < iSize;
        }

        public Object next() {

            if (hasNext()) {
                removed = false;

                if (counter != 0) {
                    last    = current;
                    current = current.next;
                }

                counter++;

                return current.data;
            }

            throw new NoSuchElementException();
        }

        public int nextInt() {
            throw new NoSuchElementException();
        }

        public long nextLong() {
            throw new NoSuchElementException();
        }

        public void remove() {

            if (counter <= iSize && counter != 0 &&!removed) {
                removed = true;

                if (current == rTail) {
                    rTail = last;
                }

                if (current == rRoot) {
                    current = rRoot = rRoot.next;
                } else {
                    current      = last;
                    last         = null;
                    current.next = current.next.next;
                }

                iSize--;
                counter--;

                return;
            }

            throw new NoSuchElementException();
        }
    }
}
