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

import java.sql.SQLException;
import java.io.*;

/**
 * Class declaration
 *
 *
 * @version 1.0.0.1
 */
class Result {

    private Record   rTail;
    private int      iSize;
    private int      iColumnCount;
    final static int UPDATECOUNT = 0,
                     ERROR       = 1,
                     DATA        = 2;
    int              iMode;
    String           sError;
    int              iUpdateCount;
    Record           rRoot;
    String           sLabel[];
    String           sTable[];
    String           sName[];
    int              iType[];

    /**
     * Constructor declaration
     *
     */
    Result() {
        iMode        = UPDATECOUNT;
        iUpdateCount = 0;
    }

    /**
     * Constructor declaration
     *
     *
     * @param error
     */
    Result(String error) {
        iMode  = ERROR;
        sError = error;
    }

    /**
     * Constructor declaration
     *
     *
     * @param columns
     */
    Result(int columns) {

        prepareData(columns);

        iColumnCount = columns;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    int getSize() {
        return iSize;
    }

    /**
     * Method declaration
     *
     *
     * @param columns
     */
    void setColumnCount(int columns) {
        iColumnCount = columns;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    int getColumnCount() {
        return iColumnCount;
    }

    /**
     * Method declaration
     *
     *
     * @param a
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

    /**
     * Method declaration
     *
     *
     * @param d
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
     * Constructor declaration
     *
     *
     * @param b
     */
    Result(byte b[]) throws SQLException {

        ByteArrayInputStream bin = new ByteArrayInputStream(b);
        DataInputStream      in  = new DataInputStream(bin);

        try {
            iMode = in.readInt();

            if (iMode == ERROR) {
                throw Trace.getError(in.readUTF());
            } else if (iMode == UPDATECOUNT) {
                iUpdateCount = in.readInt();
            } else if (iMode == DATA) {
                int l = in.readInt();

                prepareData(l);

                iColumnCount = l;

                for (int i = 0; i < l; i++) {
                    iType[i]  = in.readInt();
                    sLabel[i] = in.readUTF();
                    sTable[i] = in.readUTF();
                    sName[i]  = in.readUTF();
                }

                while (in.available() != 0) {
                    add(Column.readData(in, l));
                }
            }
        } catch (IOException e) {
            Trace.error(Trace.TRANSFER_CORRUPTED);
        }
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @throws SQLException
     */
    byte[] getBytes() throws SQLException {

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream      out  = new DataOutputStream(bout);

        try {
            out.writeInt(iMode);

            if (iMode == UPDATECOUNT) {
                out.writeInt(iUpdateCount);
            } else if (iMode == ERROR) {
                out.writeUTF(sError);
            } else {
                int l = iColumnCount;

                out.writeInt(l);

                Record n = rRoot;

                for (int i = 0; i < l; i++) {
                    out.writeInt(iType[i]);
                    out.writeUTF(sLabel[i]);
                    out.writeUTF(sTable[i]);
                    out.writeUTF(sName[i]);
                }

                while (n != null) {
                    Column.writeData(out, l, iType, n.data);

                    n = n.next;
                }
            }

            return bout.toByteArray();
        } catch (IOException e) {
            throw Trace.error(Trace.TRANSFER_CORRUPTED);
        }
    }

    /**
     * Method declaration
     *
     *
     * @param columns
     */
    private void prepareData(int columns) {

        iMode  = DATA;
        sLabel = new String[columns];
        sTable = new String[columns];
        sName  = new String[columns];
        iType  = new int[columns];
    }
}
