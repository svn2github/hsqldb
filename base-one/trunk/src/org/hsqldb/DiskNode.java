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
import java.io.IOException;

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
// fredt@users 20020920 - path 1.7.1 by fredt - refactoring to cut mamory footprint

/**
 *  Cached table Node implementation.<br>
 *  Only integral references to left, right and parent nodes in the AVL tree
 *  are held and used as offsets to disk data.<br>
 *
 *  tTable is a reference to the table for this node and is used to get a
 *  linked node in the tree.<br>
 *  iId is a reference to the Index object that contains this node.<br>
 *  These fields can be eliminated in the future, by changing the
 *  method signatures to take a Table parameter from Index.java (fredt@users)
 *
 *
 * @version    1.7.1
 */
class DiskNode extends Node {

    private int   iLeft   = NO_POS;
    private int   iRight  = NO_POS;
    private int   iParent = NO_POS;
    private int   iId;    // id of Index object for this Node
    private int   iData = NO_POS;
    private Table tTable;

    DiskNode(CachedRow r, DatabaseRowInputInterface in,
             int id) throws IOException, SQLException {

        iId      = id;
        tTable   = r.getTable();
        rData    = r;
        iData    = r.iPos;
        iBalance = in.readIntData();
        iLeft    = in.readIntData();

        if (iLeft <= 0) {
            iLeft = NO_POS;
        }

        iRight = in.readIntData();

        if (iRight <= 0) {
            iRight = NO_POS;
        }

        iParent = in.readIntData();

        if (iParent <= 0) {
            iParent = NO_POS;
        }

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }
    }

    DiskNode(CachedRow r, int id) {

        iId    = id;
        tTable = r.getTable();

        if (r.iPos == CachedRow.NO_POS) {
            rData = r;
        } else {
            iData = r.iPos;
        }
    }

    void delete() {

        iBalance = -2;
        iLeft    = iRight = iParent = 0;
        tTable   = null;
    }

    int getKey() {

        if (rData != null) {
            return ((CachedRow) rData).iPos;
        }

        return iData;
    }

    void setKey(int pos) {
        iData = pos;
        rData = null;
    }

    Row getRow() throws SQLException {

        if (rData != null) {
            return rData;
        }

        if (iData == NO_POS) {
            return null;
        }

        return tTable.getRow(iData);
    }

    private Node findNode(int pos, int id) throws SQLException {

        Node ret = null;
        Row  r   = tTable.getRow(pos);

        if (r != null) {
            ret = r.getNode(id);
        }

        return ret;
    }

    Node getLeft() throws SQLException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }

        if (iLeft == NO_POS) {
            return (null);
        }

        return (findNode(iLeft, iId));
    }

    void setLeft(Node n) throws SQLException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }

        ((CachedRow) getRow()).changed();

        iLeft = NO_POS;

        if (n != null) {
            iLeft = n.getKey();
        }
    }

    Node getRight() throws SQLException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }

        if (iRight == NO_POS) {
            return null;
        }

        return (findNode(iRight, iId));
    }

    /**
     *  Used with PointerNode objects only
     *
     * @param  pos file offset of node
     */
    Node getRightPointer() throws SQLException {
        throw Trace.error(Trace.GENERAL_ERROR, "wrong method access");
    }

    void setRight(Node n) throws SQLException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }

        ((CachedRow) getRow()).changed();

        iRight = NO_POS;

        if (n != null) {
            iRight = n.getKey();
        }
    }

    Node getParent() throws SQLException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }

        if (iParent == NO_POS) {
            return null;
        }

        return findNode(iParent, iId);
    }

    boolean isRoot() {
        return iParent == Node.NO_POS;
    }

    void setParent(Node n) throws SQLException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }

        ((CachedRow) getRow()).changed();

        iParent = NO_POS;

        if (n != null) {
            iParent = n.getKey();
        }
    }

    void setBalance(int b) throws SQLException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }

        if (iBalance != b) {
            ((CachedRow) getRow()).changed();

            iBalance = b;
        }
    }

    boolean from() throws SQLException {

        if (this.isRoot()) {
            return true;
        }

        if (Trace.DOASSERT) {
            Trace.doAssert(getParent() != null);
        }

        DiskNode parent = (DiskNode) getParent();

        if (parent.iLeft != Node.NO_POS) {
            return (getKey() == parent.iLeft);
        } else {
            return equals(parent.getLeft());
        }
    }

    Object[] getData() throws SQLException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }

        return getRow().getData();
    }

    boolean equals(Node n) throws SQLException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);

            if (n != this) {
                Trace.doAssert((getKey() == NO_POS) || (n == null)
                               || (n.getKey() != getKey()));
            } else {
                Trace.doAssert(n.getKey() == getKey());
            }
        }

        return n == this;
    }

    void write(DatabaseRowOutputInterface out)
    throws IOException, SQLException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }

        out.writeIntData(iBalance);
        out.writeIntData((iLeft == NO_POS) ? 0
                                           : iLeft);
        out.writeIntData((iRight == NO_POS) ? 0
                                            : iRight);
        out.writeIntData((iParent == NO_POS) ? 0
                                             : iParent);
    }

    void writeTranslate(DatabaseRowOutputInterface out,
                        org.hsqldb.lib.UnifiedTable lookup)
                        throws IOException, SQLException {

        out.writeIntData(iBalance);
        writeTranslatePointer(iLeft, out, lookup);
        writeTranslatePointer(iRight, out, lookup);
        writeTranslatePointer(iParent, out, lookup);
    }

    private void writeTranslatePointer(int pointer,
                                       DatabaseRowOutputInterface out,
                                       org.hsqldb.lib.UnifiedTable lookup)
                                       throws IOException, SQLException {

        int newPointer = 0;

        if (pointer != Node.NO_POS) {
            int i = lookup.search(pointer);

            if (i == -1) {
                throw new SQLException();
            }

            newPointer = lookup.getIntCell(i, 1);
        }

        out.writeIntData(newPointer);
    }
}
