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

/**
 *  Memory table node implementation.
 *
 * @version    1.7.1
 */
class MemoryNode extends Node {

    protected Node nLeft;
    protected Node nRight;
    protected Node nParent;

    MemoryNode() {}

    /**
     *  Constructor declaration
     *
     * @param  r
     * @param  id
     */
    MemoryNode(Row r) {
        rData = r;
    }

    /**
     *  Method declaration
     */
    void delete() {
        iBalance = -2;
        nLeft    = nRight = nParent = null;
    }

    int getKey() {
        return 0;
    }

    void setKey(int pos) {}

    Row getRow() throws SQLException {
        return rData;
    }

    Node getLeft() throws SQLException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }

        return nLeft;
    }

    void setLeft(Node n) throws SQLException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }

        nLeft = n;
    }

    Node getRight() throws SQLException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }

        return nRight;
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

        nRight = n;
    }

    Node getParent() throws SQLException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }

        return nParent;
    }

    boolean isRoot() {
        return nParent == null;
    }

    void setParent(Node n) throws SQLException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }

        nParent = n;
    }

    void setBalance(int b) throws SQLException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }

        iBalance = b;
    }

    boolean from() throws SQLException {

        if (this.isRoot()) {
            return true;
        }

        if (Trace.DOASSERT) {
            Trace.doAssert(getParent() != null);
        }

        Node parent = getParent();

        return equals(parent.getLeft());
    }

    Object[] getData() throws SQLException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }

        return rData.getData();
    }

    boolean equals(Node n) throws SQLException {

        if (Trace.DOASSERT) {
            Trace.doAssert(iBalance != -2);
        }

        return n == this;
    }

    /**
     *  Method declaration
     *
     * @param  out
     * @throws  IOException
     * @throws  SQLException
     */
    void write(DatabaseRowOutputInterface out)
    throws IOException, SQLException {}
}
