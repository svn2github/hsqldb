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

// fredt@users 20020130 - patch 1.7.0 by fredt
// to ensure consistency of r.rTail r.iSize in all operations
// methods for set operations moved here from Select.java
// fredt@users 20020130 - patch 1.7.0 by fredt
// rewrite of LIMIT n m to apply to each select statement separately
// tony_lai@users 20020820 - patch 595073 - duplicated exception msg

/**
 *  Class declaration
 *
 * @version    1.7.0
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

    // database ID
    int databaseID;

    // session ID
    int sessionID;

    // user / password or error strings
    String mainString;
    String subString;

    // database name
    String subSubString;

    // parepared statement id
    int statementID;

    // max rows (out) or update count (in)
    int     iUpdateCount;
    String  sLabel[];
    String  sTable[];
    String  sName[];
    boolean isLabelQuoted[];
    int     colType[];
    int     colSize[];
    int     colScale[];

    /**
     *  Constructor declaration
     */
    Result(int type) {
        iMode = type;
    }

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)

    /**
     *  Constructor declaration
     *
     * @param  error
     * @param  code   Description of the Parameter
     */
    Result(String error, String state, int code) {

        iMode        = ResultConstants.ERROR;
        mainString   = error;
        subString    = state;
        statementID  = code;
        subSubString = "";
    }

    /**
     *  Constructor declaration
     *
     * @param  columns
     */
    Result(int type, int columns) {

        prepareData(columns);

        iMode              = type;
        significantColumns = columns;
    }

    Result(int type, int types[], int id) {

        iMode              = type;
        colType            = types;
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
            iMode      = in.readIntData();
            databaseID = in.readIntData();
            sessionID  = in.readIntData();

            switch (iMode) {

                case ResultConstants.SQLGETSESSIONINFO :
                case ResultConstants.SQLDISCONNECT :
                case ResultConstants.SQLENDTRAN:
                case ResultConstants.SQLSTARTTRAN:
                    break;

                case ResultConstants.SQLPREPARE :
                    mainString = in.readString();
                    break;

                case ResultConstants.SQLEXECDIRECT :
                    iUpdateCount = in.readIntData();
                    statementID  = in.readIntData();
                    mainString   = in.readString();
                    break;

                case ResultConstants.ERROR :
                case ResultConstants.SQLCONNECT :
                    mainString   = in.readString();
                    subString    = in.readString();
                    subSubString = in.readString();

//                    throw Trace.getError(string, code);
                    break;

                case ResultConstants.UPDATECOUNT :
                    iUpdateCount = in.readIntData();
                    break;

                case ResultConstants.SQLEXECUTE :
                case ResultConstants.SQLSETENVATTR : {
                    iUpdateCount = in.readIntData();
                    statementID  = in.readIntData();

                    int l = in.readIntData();

                    prepareData(l);

                    significantColumns = l;

                    for (int i = 0; i < l; i++) {
                        colType[i] = in.readType();
                    }

                    while (in.available() != 0) {
                        add(in.readData(colType));
                    }

                    break;
                }
                case ResultConstants.DATA : {
                    int l = in.readIntData();

                    prepareData(l);

                    significantColumns = l;

                    for (int i = 0; i < l; i++) {
                        colType[i] = in.readType();
                        sLabel[i]  = in.readString();
                        sTable[i]  = in.readString();
                        sName[i]   = in.readString();
                    }

                    while (in.available() != 0) {
                        add(in.readData(colType));
                    }

                    break;
                }
                default :
                    throw new HsqlException(
                        "trying to use unsuppoted result mode", "", 0);
            }
        } catch (IOException e) {
            throw Trace.error(Trace.TRANSFER_CORRUPTED);
        }
    }

    static Result newSingleColumnResult(String colName, int colType) {

        Result result = new Result(ResultConstants.DATA, 1);

        result.sName[0]   = colName;
        result.sLabel     = result.sName;
        result.colType[0] = colType;

        return result;
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

        rRoot = a.rRoot;
        rTail = a.rTail;
        iSize = a.iSize;
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
     *  Method declaration
     *
     * @throws  HsqlException
     */

// fredt@users 20020130 - patch 1.7.0 by fredt
// to ensure consistency of r.rTail r.iSize in all set operations
    void removeDuplicates() throws HsqlException {

        if (rRoot == null) {
            return;
        }

        int len     = getColumnCount();
        int order[] = new int[len];
        int way[]   = new int[len];

        for (int i = 0; i < len; i++) {
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

            if (compareRecord(n.data, next.data, len) == 0) {
                n.next = next.next;

                iSize--;
            } else {
                n = next;
            }
        }

        rTail = n;

        Trace.doAssert(rTail.next == null,
                       "rTail not correct in Result.removeDuplicates iSise ="
                       + iSize);
    }

    /**
     *  Method declaration
     *
     * @param  minus
     * @throws  HsqlException
     */
    void removeSecond(Result minus) throws HsqlException {

        removeDuplicates();
        minus.removeDuplicates();

        int     len   = getColumnCount();
        Record  n     = rRoot;
        Record  last  = rRoot;
        boolean rootr = true;    // checking rootrecord
        Record  n2    = minus.rRoot;
        int     i     = 0;

        while (n != null && n2 != null) {
            i = compareRecord(n.data, n2.data, len);

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
            "rTail not correct in Result.removeSecond iSise =" + iSize);
    }

    /**
     *  Method declaration
     *
     * @param  r2
     * @throws  HsqlException
     */
    void removeDifferent(Result r2) throws HsqlException {

        removeDuplicates();
        r2.removeDuplicates();

        int     len   = getColumnCount();
        Record  n     = rRoot;
        Record  last  = rRoot;
        boolean rootr = true;    // checking rootrecord
        Record  n2    = r2.rRoot;
        int     i     = 0;

        iSize = 0;

        while (n != null && n2 != null) {
            i = compareRecord(n.data, n2.data, len);

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
            "rTail not correct in Result.removeDifference iSise =" + iSize);
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
                       "rTail not correct in Result.sortResult iSise ="
                       + iSize);
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

        int i = Column.compare(a[order[0]], b[order[0]], colType[order[0]]);

        if (i == 0) {
            for (int j = 1; j < order.length; j++) {
                i = Column.compare(a[order[j]], b[order[j]],
                                   colType[order[j]]);

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
            int i = Column.compare(a[j], b[j], colType[j]);

            if (i != 0) {
                return i;
            }
        }

        return 0;
    }

    // fredt - todo can allow reuse of the byte[] via changes in Server.java
    // WebServer.java etc.
    void getBytes(DatabaseRowOutputInterface out) {

        // to be implmented and called from Server.java etc.
    }

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)

    /**
     *  Method declaration
     *
     * @return
     * @throws  HsqlException
     */
    byte[] getBytes() throws HsqlException {

        try {
            DatabaseRowOutputInterface out = new BinaryServerRowOutput();

            write(out);

            return out.getOutputStream().toByteArray();
        } catch (IOException e) {
            throw Trace.error(Trace.TRANSFER_CORRUPTED);
        }
    }

    void write(DatabaseRowOutputInterface out)
    throws IOException, HsqlException {

        out.writeSize(0);
        out.writeIntData(iMode);
        out.writeIntData(databaseID);
        out.writeIntData(sessionID);

        switch (iMode) {

            case ResultConstants.SQLGETSESSIONINFO :
            case ResultConstants.SQLDISCONNECT :
            case ResultConstants.SQLENDTRAN:
            case ResultConstants.SQLSTARTTRAN:
                break;

            case ResultConstants.SQLPREPARE :
                out.writeString(mainString);
                break;

            case ResultConstants.SQLEXECDIRECT :
                out.writeIntData(iUpdateCount);
                out.writeIntData(statementID);
                out.writeString(mainString);
                break;

            case ResultConstants.ERROR :
            case ResultConstants.SQLCONNECT :
                out.writeString(mainString);
                out.writeString(subString);
                out.writeString(subSubString);
                break;

            case ResultConstants.UPDATECOUNT :
                out.writeIntData(iUpdateCount);
                break;

            case ResultConstants.SQLEXECUTE :
            case ResultConstants.SQLSETENVATTR : {
                out.writeIntData(iUpdateCount);
                out.writeIntData(statementID);

                int l = significantColumns;

                out.writeIntData(l);

                for (int i = 0; i < l; i++) {
                    out.writeType(colType[i]);
                }

                Record n = rRoot;

                while (n != null) {
                    out.writeData(l, colType, n.data);

                    n = n.next;
                }

                break;
            }
            case ResultConstants.DATA : {
                int l = significantColumns;

                out.writeIntData(l);

                for (int i = 0; i < l; i++) {
                    out.writeType(colType[i]);
                    out.writeString(sLabel[i]);
                    out.writeString(sTable[i]);
                    out.writeString(sName[i]);
                }

                Record n = rRoot;

                while (n != null) {
                    out.writeData(l, colType, n.data);

                    n = n.next;
                }

                break;
            }
            default :
                throw new HsqlException(
                    "trying to use unsuppoted result mode", "", 0);
        }

        out.writeIntData(out.size(), 0);
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
    }

// boucerb@users 20030513
// ------------------- patch 1.7.2 --------------------
/*
private    String getMode() {

        switch (iMode) {

            case DATA :
                return "DATA";

            case ERROR :
                return "ERROR";

            case UPDATECOUNT :
                return "UPDATECOUNT";

            case SQLEXECUTE :
                return "SQLEXECUTE";

            case SQLEXECDIRECT :
                return "SQLEXECUTEDIRECT";

            case SQLFREESTMT :
                return "SQLFREESTMNT";

            case SQLPREPARE :
                return "SQLPREPARE";

            default :
                return "UNKNOWN";
        }
    }
*/
    Result(Throwable t, String statement) {

        iMode = ResultConstants.ERROR;

        if (t instanceof HsqlException) {
            subString  = t.getMessage().substring(0, 5);
            mainString = t.getMessage();

            if (statement != null) {
                mainString += " in statement [" + statement + "]";
            }

            statementID = ((HsqlException) t).code;
        } else if (t instanceof Exception) {
            t.printStackTrace();

            subString  = "";
            mainString = Trace.getMessage(Trace.GENERAL_ERROR) + " " + t;

            if (statement != null) {
                mainString += " in statement [" + statement + "]";
            }

            statementID = Trace.GENERAL_ERROR;
        } else if (t instanceof OutOfMemoryError) {
            t.printStackTrace();

            subString   = "";
            mainString  = "out of memory";
            statementID = Trace.GENERAL_ERROR;
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

    Object[] getParameterData() {
        return (rRoot == null) ? null
                               : rRoot.data;
    }

    void setParameterData(Object[] data) {

        if (rRoot == null) {
            this.add(data);
        } else {
            rRoot.data = data;
        }
    }

    int[] getParameterTypes() {
        return colType;
    }

    void setResultType(int type) {
        iMode = type;
    }
}
