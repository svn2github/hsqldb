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

/**
 * Class declaration
 *
 *
 * @version 1.0.0.1
 */
class Constraint {

    final static int FOREIGN_KEY = 0,
                     MAIN        = 1,
                     UNIQUE      = 2;
    private int      iType;
    private int      iLen;

    // Main is the table that is referenced
    private Table  tMain;
    private int    iColMain[];
    private Index  iMain;
    private Object oMain[];

    // Ref is the table that has a reference to the main table
    private Table  tRef;
    private int    iColRef[];
    private Index  iRef;
    private Object oRef[];

    /**
     * Constructor declaration
     *
     *
     * @param type
     * @param t
     * @param col
     */
    Constraint(int type, Table t, int col[]) {

        iType    = type;
        tMain    = t;
        iColMain = col;
        iLen     = col.length;
    }

    /**
     * Constructor declaration
     *
     *
     * @param type
     * @param main
     * @param ref
     * @param cmain
     * @param cref
     */
    Constraint(int type, Table main, Table ref, int cmain[],
               int cref[]) throws SQLException {

        iType    = type;
        tMain    = main;
        tRef     = ref;
        iColMain = cmain;
        iColRef  = cref;
        iLen     = cmain.length;

        if (Trace.ASSERT) {
            Trace.assert(cmain.length == cref.length);
        }

        oMain = tMain.getNewRow();
        oRef  = tRef.getNewRow();
        iMain = tMain.getIndexForColumns(cmain);
        iRef  = tRef.getIndexForColumns(cref);
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    int getType() {
        return iType;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    Table getMain() {
        return tMain;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    Table getRef() {
        return tRef;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    int[] getMainColumns() {
        return iColMain;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    int[] getRefColumns() {
        return iColRef;
    }

    /**
     * Method declaration
     *
     *
     * @param old
     * @param n
     *
     * @throws SQLException
     */
    void replaceTable(Table old, Table n) throws SQLException {

        if (old == tMain) {
            tMain = n;
        } else if (old == tRef) {
            tRef = n;
        } else {
            Trace.assert(false, "could not replace");
        }
    }

    /**
     * Method declaration
     *
     *
     * @param row
     *
     * @throws SQLException
     */
    void checkInsert(Object row[]) throws SQLException {

        if (iType == MAIN || iType == UNIQUE) {

            // inserts in the main table are never a problem
            // unique constraints are checked by the unique index
            return;
        }

        // must be called synchronized because of oMain
        for (int i = 0; i < iLen; i++) {
            Object o = row[iColRef[i]];

            if (o == null) {

                // if one column is null then integrity is not checked
                return;
            }

            oMain[iColMain[i]] = o;
        }

        // a record must exist in the main table
        Trace.check(iMain.find(oMain) != null,
                    Trace.INTEGRITY_CONSTRAINT_VIOLATION);
    }

    /**
     * Method declaration
     *
     *
     * @param row
     *
     * @throws SQLException
     */
    void checkDelete(Object row[]) throws SQLException {

        if (iType == FOREIGN_KEY || iType == UNIQUE) {

            // deleting references are never a problem
            // unique constraints are checked by the unique index
            return;
        }

        // must be called synchronized because of oRef
        for (int i = 0; i < iLen; i++) {
            Object o = row[iColMain[i]];

            if (o == null) {

                // if one column is null then integrity is not checked
                return;
            }

            oRef[iColRef[i]] = o;
        }

        // there must be no record in the 'slave' table
        Trace.check(iRef.find(oRef) == null,
                    Trace.INTEGRITY_CONSTRAINT_VIOLATION);
    }

    /**
     * Method declaration
     *
     *
     * @param col
     * @param deleted
     * @param inserted
     *
     * @throws SQLException
     */
    void checkUpdate(int col[], Result deleted,
                     Result inserted) throws SQLException {

        if (iType == UNIQUE) {

            // unique constraints are checked by the unique index
            return;
        }

        if (iType == MAIN) {
            if (!isAffected(col, iColMain, iLen)) {
                return;
            }

            // check deleted records
            Record r = deleted.rRoot;

            while (r != null) {

                // if a identical record exists we don't have to test
                if (iMain.find(r.data) == null) {
                    checkDelete(r.data);
                }

                r = r.next;
            }
        } else if (iType == FOREIGN_KEY) {
            if (!isAffected(col, iColMain, iLen)) {
                return;
            }

            // check inserted records
            Record r = inserted.rRoot;

            while (r != null) {
                checkInsert(r.data);

                r = r.next;
            }
        }
    }

    /**
     * Method declaration
     *
     *
     * @param col
     * @param col2
     * @param len
     *
     * @return
     */
    private boolean isAffected(int col[], int col2[], int len) {

        if (iType == UNIQUE) {

            // unique constraints are checked by the unique index
            return false;
        }

        for (int i = 0; i < col.length; i++) {
            int c = col[i];

            for (int j = 0; j < len; j++) {
                if (c == col2[j]) {
                    return true;
                }
            }
        }

        return false;
    }
}
