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
import java.sql.SQLException;

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
// fredt@users 20020920 - path 1.7.1 - refactoring to cut mamory footprint

/**
 *  In-memory representation of a disk-based database row object with storage
 *  independent methods for serialization and de-serialization.
 *
 *  A CachedRow is part of a circular double linked list which contians all
 *  the Rows currently in the Cache for the database.
 *
 * @version 1.7.1
 */
class CachedRow extends Row {

    static final int NO_POS = -1;
    protected Table  tTable;
    int              iLastAccess;
    CachedRow        rLast, rNext;
    int              iPos = NO_POS;
    int              storageSize;
    private boolean  bChanged;


    CachedRow(){

    }
    /**
     *  Constructor declaration
     *
     * @param  t
     * @param  o
     * @exception  SQLException  Description of the Exception
     */
    CachedRow(Table t, Object o[]) throws SQLException {

        tTable = t;

        int index = t.getIndexCount();

        nPrimaryNode = Node.newNode(this, 0, t);

        Node n = nPrimaryNode;

        for (int i = 1; i < index; i++) {
            n.nNext = Node.newNode(this, i, t);
            n       = n.nNext;
        }

        oData    = o;
        bChanged = true;

        t.putRow(this);
    }
    /**
     *  constructor when read from cache
     *
     * @param  t
     * @param  in
     * @exception  IOException   Description of the Exception
     * @exception  SQLException  Description of the Exception
     */
    CachedRow(Table t,
              DatabaseRowInputInterface in) throws IOException, SQLException {

        tTable      = t;
        iPos        = in.getPos();
        storageSize = in.getSize();

        int index = t.getIndexCount();

        nPrimaryNode = Node.newNode(this, in, 0, t);

        Node n = nPrimaryNode;

        for (int i = 1; i < index; i++) {
            n.nNext = Node.newNode(this, in, i, t);
            n       = n.nNext;
        }

        oData = in.readData(tTable.getColumnTypes());

        Trace.check(in.readIntData() == iPos, Trace.INPUTSTREAM_ERROR);
    }

    void setPos(int pos) {

        iPos = pos;

        Node n = nPrimaryNode;

        while (n != null) {
            n.setKey(pos);

            n = n.nNext;
        }
    }

    void changed() {
        bChanged = true;
    }

    boolean hasChanged() {
        return (bChanged);
    }

    /**
     *  Method declaration
     *
     * @return
     */
    Table getTable() {
        return tTable;
    }

    /**
     *  Method declaration
     *
     * @param  before
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

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
// method renamed

    /**
     * Used only in Cache.java to avoid removing the row from the cache
     *
     *
     * @return
     * @throws  SQLException
     */
    boolean isRoot() throws SQLException {

        Node n = nPrimaryNode;

        while (n != null) {
            if (Trace.DOASSERT) {
                Trace.doAssert(n.getBalance() != -2);
            }

            if (Trace.STOP) {
                Trace.stop();
            }

            if (n.isRoot()) {
                return true;
            }

            n = n.nNext;
        }

        return false;
    }

    /**
     *  Method declaration
     *
     * @param  out            Description of the Parameter
     * @throws  IOException
     * @throws  SQLException
     */
    void write(DatabaseRowOutputInterface out)
    throws IOException, SQLException {

        out.writeSize(storageSize);

        if (tTable.isIndexCached()) {
            Node n = nPrimaryNode;

            while (n != null) {
                n.write(out);

                n = n.nNext;
            }
        }

        out.writeData(oData, tTable);
        out.writePos(iPos);

        bChanged = false;
    }

    /**
     *  Method declaration
     *
     * @throws  SQLException
     */
    void delete() throws SQLException {

        Record.memoryRecords++;

        bChanged = false;

        tTable.removeRow(this);

        rNext        = null;
        rLast        = null;
        tTable       = null;
        oData        = null;
        nPrimaryNode = null;
    }

    /**
     *  Method declaration
     *
     * @throws  SQLException
     */
    CachedRow free() throws SQLException {

        rLast.rNext = rNext;
        rNext.rLast = rLast;

        if (rNext == this) {
            rNext = rLast = null;
        }

        return rNext;
    }

}
