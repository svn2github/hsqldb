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
 * Copyright (c) 2001-2004, The HSQL Development Group
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

import org.hsqldb.rowio.RowInputInterface;
import org.hsqldb.rowio.RowOutputInterface;

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
// fredt@users 20020920 - path 1.7.1 - refactoring to cut mamory footprint
// fredt@users 20021205 - path 1.7.2 - enhancements
// fredt@users 20021215 - doc 1.7.2 - javadoc comments

/**
 *  In-memory representation of a disk-based database row object with  methods
 *  for serialization and de-serialization.<br>
 *
 *  A CachedRow is normally part of a circular double linked list which
 *  contians all the Rows currently in the Cache for the database. It is
 *  unlinked from this list when it is freed from the Cache to make way for
 *  other rows.
 *
 * @version 1.7.2
 */
public class CachedRow extends Row {

    static final int NO_POS = -1;
    protected Table  tTable;
    int              iLastAccess;
    CachedRow        rLast, rNext;
    int              iPos = NO_POS;
    int              storageSize;

    /**
     *  Flag indicating any change to the Nodes or table row data.
     */
    protected boolean hasChanged;

    /**
     *  Flag indicating the row data has changed too.
     */
    protected boolean hasDataChanged;

    /**
     *  Default constructor used only in subclasses.
     */
    CachedRow() {}

    /**
     *  Constructor for new Rows. This is currently the only place where
     *  hasDataChanged is set to true as the current implementation of
     *  database row updates performs a delete followed by an insert. This
     *  means that once a row is created its data cannot change.
     *  (correct as of version 1_7_2_alpha_n)
     */
    public CachedRow(Table t, Object o[]) throws HsqlException {

        tTable = t;

        int indexcount = t.getIndexCount();

        nPrimaryNode = Node.newNode(this, 0, t);

        Node n = nPrimaryNode;

        for (int i = 1; i < indexcount; i++) {
            n.nNext = Node.newNode(this, i, t);
            n       = n.nNext;
        }

        oData      = o;
        hasChanged = hasDataChanged = true;

        t.addRowToStore(this);
    }

    /**
     *  constructor when read from the disk into the Cache
     */
    public CachedRow(Table t,
                     RowInputInterface in) throws IOException, HsqlException {

        tTable      = t;
        iPos        = in.getPos();
        storageSize = in.getSize();

        int indexcount = t.getIndexCount();

        nPrimaryNode = Node.newNode(this, in, 0, t);

        Node n = nPrimaryNode;

        for (int i = 1; i < indexcount; i++) {
            n.nNext = Node.newNode(this, in, i, t);
            n       = n.nNext;
        }

        oData = in.readData(tTable.getColumnTypes());

        setPos(iPos);

        // change from 1.7.0 format - the check is no longer read or written
        // Trace.check(in.readIntData() == iPos, Trace.INPUTSTREAM_ERROR);
    }

    /**
     *  This method is called only when the Row is deleted from the database
     *  table. The links with all the other objects are removed.
     */
    void delete() throws HsqlException {

        Record.memoryRecords++;

        hasChanged = false;

        tTable.removeRow(this);

        rNext        = null;
        rLast        = null;
        tTable       = null;
        oData        = null;
        nPrimaryNode = null;
    }

    void setPos(int pos) {

        iPos = pos;

        tTable.registerRow(this);
    }

    void setChanged() {
        hasChanged = true;
    }

    boolean hasChanged() {
        return hasChanged;
    }

    void setDataChanged() {
        hasDataChanged = true;
    }

    boolean hasDataChanged() {
        return hasDataChanged;
    }

    /**
     * Returns the table which this Row belongs to.
     */
    public Table getTable() {
        return tTable;
    }

    /**
     * Returns true if any of the Index Nodes for this row is a root node.
     * Used only in Cache.java to avoid removing the row from the cache.
     */
    boolean isRoot() throws HsqlException {

        Node n = nPrimaryNode;

        while (n != null) {
            if (Trace.DOASSERT) {
                Trace.doAssert(n.getBalance() != -2);
            }

            if (n.isRoot()) {
                return true;
            }

            n = n.nNext;
        }

        return false;
    }

    /**
     *  Using the internal reference to the Table, returns the current valid
     *  Row that represents the database row for this Object. Valid for
     *  deleted rows only before any subsequent insert or update on any
     *  cached table.
     */
    Row getUpdatedRow() throws HsqlException {
        return tTable == null ? null
                              : tTable.getRow(iPos, null);
    }

    /**
     *  Used exclusively by Cache to save the row to disk. New implementation
     *  in 1.7.2 writes out only the Node data if the table row data has not
     *  changed. This situation accounts for the majority of invocations as
     *  for each row deleted or inserted, the Nodes for several other rows
     *  will change.
     */
    void write(RowOutputInterface out) throws IOException, HsqlException {

        writeNodes(out);

        if (hasDataChanged) {
            out.writeData(oData, tTable);
            out.writePos(iPos);
        }

        hasDataChanged = false;
    }

    /**
     *  The Nodes are stored first, immediately after the row size. This
     *  methods writes this information out.
     */
    private void writeNodes(RowOutputInterface out)
    throws IOException, HsqlException {

        out.writeSize(storageSize);

        Node n = nPrimaryNode;

        while (n != null) {
            n.write(out);

            n = n.nNext;
        }

        hasChanged = false;
    }

    /**
     * Used to insert the Row into the linked list that includes all the rows
     * currently in the Cache.
     */
    void insert(CachedRow before) {

        Record.memoryRecords++;

        if (before == null) {
            rNext = this;
            rLast = this;
        } else {
            rNext        = before;
            rLast        = before.rLast;
            before.rLast = this;
            rLast.rNext  = this;
        }
    }

    /**
     *  Removes the Row from the linked list of Rows in the Cache.
     */
    CachedRow free() throws HsqlException {

        CachedRow nextrow = rNext;

        rLast.rNext = rNext;
        rNext.rLast = rLast;
        rNext       = rLast = null;

        if (nextrow == this) {
            return null;
        }

        return nextrow;
    }

    /**
     * Lifetime scope of this method depends on the operations performed on
     * any cached tables since this row or the parameter were constructed.
     * If only deletes or only inserts have been performed, this method
     * remains valid. Otherwise it can return invalid results.
     *
     * @param obj the reference object with which to compare.
     * @return <code>true</code> if this object is the same as the obj argument;
     *   <code>false</code> otherwise.
     * @todo Implement this java.lang.Object method
     */
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }

        if (obj == null ||!(obj instanceof CachedRow)) {
            return false;
        }

        return ((CachedRow) obj).iPos == iPos;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object.
     * @todo Implement this java.lang.Object method
     */
    public int hashCode() {
        return iPos;
    }
}
