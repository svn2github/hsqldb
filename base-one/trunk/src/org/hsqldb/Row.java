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
// fredt@users 20020920 - patch 1.7.1 by fredt - refactoring to cut mamory footprint

/**
 * Base class for a database row object implementing rows for
 * memory resident tables and TEXT tables.<p>
 *
 *
 * Subclasses implement rows for CACHED tables.
 *
 * @version 1.7.1
 */
class Row {

    protected Object oData[];
    protected Node   nPrimaryNode;

    /**
     *  Factory method instantiates a Row based on table type.
     */
    static Row newRow(Table t, Object o[]) throws SQLException {

        if (t.isCached()) {
            return new CachedRow(t, o);
        } else {
            return new Row(t, o);
        }
    }

    Row() {}

    /**
     *  Constructor for memory Row
     *
     * @param  t
     * @param  o
     * @exception  SQLException  Description of the Exception
     */
    Row(Table t, Object o[]) throws SQLException {

        int index = t.getIndexCount();

        nPrimaryNode = Node.newNode(this, 0, t);

        Node n = nPrimaryNode;

        for (int i = 1; i < index; i++) {
            n.nNext = Node.newNode(this, i, t);
            n       = n.nNext;
        }

        oData = o;
    }

    void setPrimaryNode(Node primary) {
        nPrimaryNode = primary;
    }

    /**
     * Get the node for a given index.
     *
     * @param  index
     * @return the node
     */
    Node getNode(int index) {

        Node n = nPrimaryNode;

        while (index-- > 0) {
            n = n.nNext;
        }

        return n;
    }

    /**
     *  Method declaration
     *
     * @param  n
     * @return
     */
    Node getNextNode(Node n) {

        if (n == null) {
            n = nPrimaryNode;
        } else {
            n = n.nNext;
        }

        return (n);
    }

    /**
     *  Method declaration
     *
     * @return
     */
    Object[] getData() {
        return oData;
    }

    /**
     *  Method declaration
     *
     * @throws  SQLException
     */
    void delete() throws SQLException {

        Record.memoryRecords++;

        oData        = null;
        nPrimaryNode = null;
    }
}
