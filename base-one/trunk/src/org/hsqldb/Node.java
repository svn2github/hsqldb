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
 *  The parent for all AVL node implementations, features factory methods for
 *  its subclasses. Subclasses of Node vary in the way they hold
 *  references to other nodes in the AVL tree.<br>
 *
 *  nNext links the Node objects belonging to different indexes for each
 *  table row. It is used solely by Row to locate the node belonging to a
 *  particular index. (fredt@users)
 *
 *
 * @version 1.7.1
 */
abstract class Node {

    static final int TYPE_MEMORY  = 0;
    static final int TYPE_DISK    = 1;
    static final int TYPE_POINTER = 2;
    static final int NO_POS       = CachedRow.NO_POS;
    protected int    iBalance;    // currently, -2 means 'deleted'
    protected Row    rData;
    Node             nNext;       // node of next index (nNext==null || nNext.iId=iId+1)

    static final Node newNode(Row r, int id, Table t) {

        switch (t.getIndexType()) {

            case Index.MEMORY_INDEX :
                return new MemoryNode(r);

            case Index.POINTER_INDEX :
                return new PointerNode((CachedRow) r, id);

            case Index.DISK_INDEX :
            default :
                return new DiskNode((CachedRow) r, id);
        }
    }

    static final Node newNode(Row r, DatabaseRowInputInterface in, int id,
                              Table t) throws IOException, SQLException {

        switch (t.getIndexType()) {

            case Index.MEMORY_INDEX :
                return new MemoryNode(r);

            case Index.POINTER_INDEX :
                return new PointerNode((CachedRow) r, id);

            case Index.DISK_INDEX :
            default :
                return new DiskNode((CachedRow) r, in, id);
        }
    }

    /**
     *  Method declaration
     */
    abstract void delete();

    /**
     *  File offset of Node. Used with CachedRow objects only
     *
     * @return file offset
     */
    abstract int getKey();

    /**
     *  Used with CachedRow objects only
     *
     * @param  pos file offset of node
     */
    abstract void setKey(int pos);

    abstract Row getRow() throws SQLException;

    abstract Node getLeft() throws SQLException;

    abstract void setLeft(Node n) throws SQLException;

    abstract Node getRight() throws SQLException;

    /**
     *  Used with PointerNode objects only
     *
     * @param  pos file offset of node
     */
    abstract Node getRightPointer() throws SQLException;

    abstract void setRight(Node n) throws SQLException;

    abstract Node getParent() throws SQLException;

    abstract boolean isRoot();

    abstract void setParent(Node n) throws SQLException;

    final int getBalance() throws SQLException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }

        return iBalance;
    }

    abstract void setBalance(int b) throws SQLException;

    /**
     * Method declaration
     *
     *
     * @param x
     *
     * @return
     *
     * @throws SQLException
     */
    abstract boolean from() throws SQLException;

    /**
     *  Returns the data held in the table Row for this Node
     *
     */
    abstract Object[] getData() throws SQLException;

    abstract boolean equals(Node n) throws SQLException;

    /**
     *  Writes out the node in an implementation dependent way.
     *
     * @param  out interface providing the different write methods
     */
    abstract void write(DatabaseRowOutputInterface out)
    throws IOException, SQLException;
}
