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

import org.hsqldb.lib.ArrayUtil;
import java.sql.SQLException;

// fredt@users 20020225 - patch 1.7.0 by boucherb@users - named constraints
// fredt@users 20020320 - doc 1.7.0 - update

/**
 *  Implementation of a table constraint with references to the indexes used
 *  by the contraint.
 *
 * @version    1.7.0
 */
class Constraint {

    static final int FOREIGN_KEY = 0,
                     MAIN        = 1,
                     UNIQUE      = 2;

    // fkName and pkName are for foreign keys only
    private HsqlName constName;
    private HsqlName fkName;
    private HsqlName pkName;
    private int      iType;
    private int      iLen;

    // Main is the table that is referenced
    private Table    tMain;
    private int[]    iColMain;
    private Index    iMain;
    private Object[] oMain;

    // Ref is the table that has a reference to the main table
    private Table    tRef;
    private int[]    iColRef;
    private Index    iRef;
    private Object[] oRef;
    private Object[] oColRef;
    private boolean  bCascade;

    /**
     *  Constructor declaration
     *
     * @param  type
     * @param  t
     * @param  col
     */
    Constraint(HsqlName name, Table t, Index index) {

        constName = name;
        iType     = UNIQUE;
        tMain     = t;
        iMain     = index;
        iColMain  = index.getColumns();
        iLen      = iColMain.length;
    }

    /**
     *  Constructor declaration
     *
     * @param  type
     * @param  main
     * @param  ref
     * @param  cmain
     * @param  cref
     * @exception  SQLException  Description of the Exception
     */
    Constraint(HsqlName name, HsqlName linkedname, int type, Table main,
               Table ref, int colmain[], int colref[], Index imain,
               Index iref, boolean cascade) throws SQLException {

        constName = name;

        if (type == MAIN) {
            pkName = name;
            fkName = linkedname;
        } else if (type == FOREIGN_KEY) {
            pkName = linkedname;
            fkName = name;
        }

        iType    = type;
        tMain    = main;
        tRef     = ref;
        iColMain = colmain;
        iLen     = iColMain.length;
        iColRef  = colref;
        oColRef  = new Object[iColRef.length];
        iMain    = imain;
        iRef     = iref;
        bCascade = cascade;

        setTableRows();
    }

    private Constraint() {}

    Constraint duplicate() {

        Constraint c = new Constraint();

        c.constName = constName;
        c.fkName    = fkName;
        c.pkName    = pkName;
        c.iType     = iType;
        c.iLen      = iLen;
        c.tMain     = tMain;
        c.iColMain  = iColMain;
        c.iMain     = iMain;
        c.oMain     = oMain;
        c.tRef      = tRef;
        c.iColRef   = iColRef;
        c.iRef      = iRef;
        c.oRef      = oRef;
        c.oColRef   = oColRef;
        c.bCascade  = bCascade;

        return c;
    }

    private void setTableRows() throws SQLException {

        oMain = tMain.getNewRow();

        if (tRef != null) {
            oRef = tRef.getNewRow();
        }

        if (Trace.DOASSERT) {
            Trace.doAssert(iColMain.length == iColRef.length);
        }
    }

    HsqlName getName() {
        return constName;
    }

    /**
     * Changes constraint name.
     *
     * @param name
     * @param isquoted
     */
    private void setName(String name, boolean isquoted) {
        constName.rename(name, isquoted);
    }

    /**
     *  probably a misnomer, but DatabaseMetaData.getCrossReference specifies
     *  it this way (I suppose because most FKs are declared against the PK of
     *  another table)
     *
     *  @return name of the index refereneced by a foreign key
     */
    String getPkName() {
        return pkName == null ? null
                              : pkName.name;
    }

    /**
     *  probably a misnomer, but DatabaseMetaData.getCrossReference specifies
     *  it this way (I suppose because most FKs are declared against the PK of
     *  another table)
     *
     *  @return name of the index for the referencing foreign key
     */
    String getFkName() {
        return fkName == null ? null
                              : fkName.name;
    }

    /**
     *  Method declaration
     *
     * @return name of the index for the foreign key column (child)
     */
    int getType() {
        return iType;
    }

    /**
     *  Method declaration
     *
     * @return
     */
    Table getMain() {
        return tMain;
    }

    Index getMainIndex() {
        return iMain;
    }

    /**
     *  Method declaration
     *
     * @return
     */
    Table getRef() {
        return tRef;
    }

    Index getRefIndex() {
        return iRef;
    }

    /**
     *  Does (foreign key) constraint cascade on delete
     *
     * @return
     */
    boolean isCascade() {
        return bCascade;
    }

    /**
     *  Method declaration
     *
     * @return
     */
    int[] getMainColumns() {
        return iColMain;
    }

    /**
     *  Method declaration
     *
     * @return
     */
    int[] getRefColumns() {
        return iColRef;
    }

    /**
     *  See if an index is part this constraint and the constraint is set for
     *  a foreign key. Used for tests before dropping an index. (fredt@users)
     *
     * @return
     */
    boolean isIndexFK(Index index) {

        if (iType == FOREIGN_KEY || iType == MAIN) {
            if (iMain == index || iRef == index) {
                return true;
            }
        }

        return false;
    }

    /**
     *  See if an index is part this constraint and the constraint is set for
     *  a unique constraint. Used for tests before dropping an index.
     *  (fredt@users)
     *
     * @return
     */
    boolean isIndexUnique(Index index) {

        if (iType == UNIQUE && iMain == index) {
            return true;
        }

        return false;
    }

// fredt@users 20020225 - patch 1.7.0 by fredt - duplicate constraints

    /**
     * Compares this with another constraint column set. This implementation
     * only checks UNIQUE constraints.
     */
    boolean isEquivalent(int col[], int type) {

        if (type == iType && iType == UNIQUE && iLen == col.length) {
            if (ArrayUtil.haveEqualSets(iColMain, col, iLen)) {
                return true;
            }
        }

        return false;
    }

    /**
     *  Method declaration
     *
     * @param  old
     * @param  n
     * @throws  SQLException
     */
    void replaceTable(Table oldt, Table newt, Index oldidx,
                      Index newidx) throws SQLException {

        if (oldt == tMain) {
            tMain = newt;

            setTableRows();
        }

        if (oldidx == iMain) {
            iMain    = newidx;
            iColMain = new int[iColMain.length];

            ArrayUtil.copyArray(newidx.getColumns(), iColMain,
                                iColMain.length);
        }

        if (oldt == tRef) {
            tRef = newt;

            setTableRows();
        }

        if (oldidx == iRef) {
            iRef    = newidx;
            iColRef = new int[iColRef.length];

            ArrayUtil.copyArray(newidx.getColumns(), iColRef, iColRef.length);
        }
    }

    /**
     *  Checks for foreign key violation when inserting a row in the child
     *  table.
     *
     * @param  row
     * @throws  SQLException
     */
    void checkInsert(Object row[]) throws SQLException {

        if ((iType == MAIN) || (iType == UNIQUE)) {

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
     *  Check if a row in the referenced (parent) table can be deleted. Used
     *  only for UPDATE table statements. Checks for DELETE FROM table
     *  statements are now handled by findFkRef() to support ON DELETE
     *  CASCADE.
     *
     * @param  row
     * @throws  SQLException
     */
    private void checkDelete(Object row[]) throws SQLException {

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
        Node node = iRef.find(oRef);

        Trace.check(node == null, Trace.INTEGRITY_CONSTRAINT_VIOLATION);
    }

// fredt@users 20020225 - patch 1.7.0 - cascading deletes

    /**
     * New method to find any referencing node (containing the row) for a
     * foreign key (finds row in child table). If ON DELETE CASCADE is
     * supported by this constraint, then the method finds the first row
     * among the rows of the table ordered by the index and doesn't throw.
     * Without ON DELETE CASCADE, the method attempts to finds any row that
     * exists, in which case it throws an exception. If no row is found,
     * null is returned.
     * (fredt@users)
     *
     * @param  array of objects for a database row
     * @return Node object or null
     * @throws  SQLException
     */
    Node findFkRef(Object row[]) throws SQLException {

        // must be called synchronized because of oRef
        for (int i = 0; i < iLen; i++) {
            Object o = row[iColMain[i]];

            if (o == null) {

                // if one column is null then integrity is not checked
                return null;
            }

            oColRef[i] = o;
        }

        // there must be no record in the 'slave' table
        Node node = iRef.findSimple(oColRef, bCascade);

        Trace.check((node == null) || bCascade,
                    Trace.INTEGRITY_CONSTRAINT_VIOLATION);

        return node;
    }

    /**
     *  Checks if updating a set of columns in a table row breaks the
     *  referential integrity constraint.
     *
     * @param  col array of column indexes for columns to check
     * @param  deleted  rows to delete
     * @param  inserted rows to insert
     * @throws  SQLException
     */
    void checkUpdate(int col[], Result deleted,
                     Result inserted) throws SQLException {

        if (iType == UNIQUE) {

            // unique constraints are checked by the unique index
            return;
        }

        if (iType == MAIN) {
            if (!ArrayUtil.haveCommonElement(col, iColMain, iLen)) {
                return;
            }

            // check deleted records
            Record r = deleted.rRoot;

            while (r != null) {

                // if an identical record exists we don't have to test
                if (iMain.find(r.data) == null) {
                    checkDelete(r.data);
                }

                r = r.next;
            }
        } else if (iType == FOREIGN_KEY) {
            if (!ArrayUtil.haveCommonElement(col, iColMain, iLen)) {
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
}
