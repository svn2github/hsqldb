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

// fredt@users 20020221 - patch 513005 by sqlbob@users - corrections
// fredt@users 20020225 - patch 1.7.0 - cascading deletes
// a number of changes to support this feature
// tony_lai@users 20020820 - patch 595052 - better error message
// fredt@users 20021205 - patch 1.7.2 - changes to method signature

/**
 * Implementation of an AVL tree with parent pointers in nodes. Subclasses
 * of Node implement the tree node objects for memory or disk storage. An
 * Index has a root Node that is linked with other nodes using Java Object
 * references or file pointers, depending on Node implementation.<p>
 * An Index object also holds information on table columns (in the form of int
 * indexes) that are covered by it.(fredt@users)
 *
 * @version 1.7.2
 */
class Index {

    // types of index
    static final int MEMORY_INDEX  = 0;
    static final int DISK_INDEX    = 1;
    static final int POINTER_INDEX = 2;

    // fields
    private HsqlName indexName;
    private int      iFields;
    private int      iColumn[];
    private int      iType[];
    private boolean  bUnique;               // DDL uniqueness
    private int      visibleColumns;
    private Node     root;
    private int      iColumn_0, iType_0;    // just for tuning

    /**
     * Constructor declaration
     *
     *
     * @param name
     * @param column
     * @param type
     * @param unique
     */
    Index(HsqlName name, Table table, int column[], int type[],
            boolean unique, int visibleColumns) {

        indexName           = name;
        iFields             = column.length;
        iColumn             = column;
        iType               = type;
        bUnique             = unique;
        iColumn_0           = iColumn[0];
        iType_0             = iType[0];
        this.visibleColumns = visibleColumns;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    Node getRoot() {
        return root;
    }

    /**
     * Method declaration
     *
     *
     * @param r
     */
    void setRoot(Node r) {
        root = r;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    HsqlName getName() {
        return indexName;
    }

    /**
     * Changes index name. Used by 'alter index rename to'
     *
     * @param name
     * @param isquoted
     */
    void setName(String name, boolean isquoted) throws SQLException {
        indexName.rename(name, isquoted);
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    int getVisibleColumns() {
        return visibleColumns;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    boolean isUnique() {
        return bUnique;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    int[] getColumns() {
        return iColumn;    // todo: this gives back also primary key field!
    }

    /**
     * Method declaration
     *
     *
     * @param c
     *
     * @return
     */

// fredt@users 20020225 - patch 1.7.0 - compare two indexes
    boolean isEquivalent(Index index) {

        if (bUnique == index.bUnique
                && iColumn.length == index.iColumn.length) {
            for (int j = 0; j < iColumn.length; j++) {
                if (iColumn[j] != index.iColumn[j]) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    /**
     * Method declaration
     *
     *
     * @param i
     *
     * @throws SQLException
     */
    void insert(Node i) throws SQLException {

        Object  data[]  = i.getData();
        Node    n       = root,
                x       = n;
        boolean isleft  = true;
        int     compare = -1;

        while (true) {
            if (Trace.STOP) {
                Trace.stop();
            }

            if (n == null) {
                if (x == null) {
                    root = i;

                    return;
                }

                set(x, isleft, i);

                break;
            }

            Object nData[] = n.getData();

            compare = compareRow(data, nData);

            if (compare == 0) {
                throw Trace.error(Trace.VIOLATION_OF_UNIQUE_INDEX,
                                  indexName.name);
            }

            isleft = compare < 0;
            x      = n;
            n      = child(x, isleft);
        }

        balance(x, isleft);
    }

    private void balance(Node x, boolean isleft) throws SQLException {

        while (true) {
            if (Trace.STOP) {
                Trace.stop();
            }

            int sign = isleft ? 1
                              : -1;

            switch (x.getBalance() * sign) {

                case 1 :
                    x.setBalance(0);

                    return;

                case 0 :
                    x.setBalance(-sign);
                    break;

                case -1 :
                    Node l = child(x, isleft);

                    if (l.getBalance() == -sign) {
                        replace(x, l);
                        set(x, isleft, child(l, !isleft));
                        set(l, !isleft, x);
                        x.setBalance(0);
                        l.setBalance(0);
                    } else {
                        Node r = child(l, !isleft);

                        replace(x, r);
                        set(l, !isleft, child(r, isleft));
                        set(r, isleft, l);
                        set(x, isleft, child(r, !isleft));
                        set(r, !isleft, x);

                        int rb = r.getBalance();

                        x.setBalance((rb == -sign) ? sign
                                                   : 0);
                        l.setBalance((rb == sign) ? -sign
                                                  : 0);
                        r.setBalance(0);
                    }

                    return;
            }

            if (x.equals(root)) {
                return;
            }

            isleft = x.isFromLeft();
            x      = x.getParent();
        }
    }

    /**
     * Method declaration
     *
     *
     * @param row
     * @param datatoo
     *
     * @throws SQLException
     */
    void delete(Node x) throws SQLException {

        if (x == null) {
            return;
        }

        Node n;

        if (x.getLeft() == null) {
            n = x.getRight();
        } else if (x.getRight() == null) {
            n = x.getLeft();
        } else {
            Node d = x;

            x = x.getLeft();

/*
            // todo: this can be improved

            while (x.getRight() != null) {
                if (Trace.STOP) {
                    Trace.stop();
                }

                x = x.getRight();
            }
*/
            for (Node temp = x; (temp = temp.getRight()) != null; ) {
                if (Trace.STOP) {
                    Trace.stop();
                }

                x = temp;
            }

            // x will be replaced with n later
            n = x.getLeft();

            // swap d and x
            int b = x.getBalance();

            x.setBalance(d.getBalance());
            d.setBalance(b);

            // set x.parent
            Node xp = x.getParent();
            Node dp = d.getParent();

            if (d == root) {
                root = x;
            }

            x.setParent(dp);

            if (dp != null) {
                if (dp.getRight().equals(d)) {
                    dp.setRight(x);
                } else {
                    dp.setLeft(x);
                }
            }

            // for in-memory tables we could use: d.rData=x.rData;
            // but not for cached tables
            // relink d.parent, x.left, x.right
            if (xp == d) {
                d.setParent(x);

                if (d.getLeft().equals(x)) {
                    x.setLeft(d);
                    x.setRight(d.getRight());
                } else {
                    x.setRight(d);
                    x.setLeft(d.getLeft());
                }
            } else {
                d.setParent(xp);
                xp.setRight(d);
                x.setRight(d.getRight());
                x.setLeft(d.getLeft());
            }

            x.getRight().setParent(x);
            x.getLeft().setParent(x);

            // set d.left, d.right
            d.setLeft(n);

            if (n != null) {
                n.setParent(d);
            }

            d.setRight(null);

            x = d;
        }

        boolean isleft = x.isFromLeft();

        replace(x, n);

        n = x.getParent();

        x.delete();

        while (n != null) {
            if (Trace.STOP) {
                Trace.stop();
            }

            x = n;

            int sign = isleft ? 1
                              : -1;

            switch (x.getBalance() * sign) {

                case -1 :
                    x.setBalance(0);
                    break;

                case 0 :
                    x.setBalance(sign);

                    return;

                case 1 :
                    Node r = child(x, !isleft);
                    int  b = r.getBalance();

                    if (b * sign >= 0) {
                        replace(x, r);
                        set(x, !isleft, child(r, isleft));
                        set(r, isleft, x);

                        if (b == 0) {
                            x.setBalance(sign);
                            r.setBalance(-sign);

                            return;
                        }

                        x.setBalance(0);
                        r.setBalance(0);

                        x = r;
                    } else {
                        Node l = child(r, isleft);

                        replace(x, l);

                        b = l.getBalance();

                        set(r, isleft, child(l, !isleft));
                        set(l, !isleft, r);
                        set(x, !isleft, child(l, isleft));
                        set(l, isleft, x);
                        x.setBalance((b == sign) ? -sign
                                                 : 0);
                        r.setBalance((b == -sign) ? sign
                                                  : 0);
                        l.setBalance(0);

                        x = l;
                    }
            }

            isleft = x.isFromLeft();
            n      = x.getParent();
        }
    }

    /**
     * for finding foreign key referencing rows (in child table)
     *
     *
     * @param data
     *
     * @return
     *
     * @throws SQLException
     */
    Node findSimple(Object indexcoldata[],
                    boolean first) throws SQLException {

        Node x      = root, n;
        Node result = null;

        if (indexcoldata[0] == null) {
            return null;
        }

        while (x != null) {
            if (Trace.STOP) {
                Trace.stop();
            }

            int i = this.comparePartialRowNonUnique(indexcoldata,
                x.getData());

            if (i == 0) {
                if (first == false) {
                    result = x;

                    break;
                } else if (result == x) {
                    break;
                }

                result = x;
                n      = x.getLeft();
            } else if (i > 0) {
                n = x.getRight();
            } else {
                n = x.getLeft();
            }

            if (n == null) {
                break;
            }

            x = n;
        }

        return result;
    }

    /**
     * Method declaration
     *
     *
     * @param data
     *
     * @return
     *
     * @throws SQLException
     */
    Node find(Object d[]) throws SQLException {

        Node x = root;

        while (x != null) {
            if (Trace.STOP) {
                Trace.stop();
            }

            int c = compareRowNonUnique(d, x.getData());

            if (c == 0) {
                return x;
            } else if (c < 0) {
                x = x.getLeft();
            } else {
                x = x.getRight();
            }
        }

        return null;
    }

    /**
     * Method declaration
     *
     *
     * @param value
     * @param compare
     *
     * @return
     *
     * @throws SQLException
     */
    Node findFirst(Object value, int compare) throws SQLException {

        Trace.doAssert(compare == Expression.BIGGER
                       || compare == Expression.EQUAL
                       || compare
                          == Expression.BIGGER_EQUAL, "Index.findFirst");

        Node x     = root;
        int  iTest = 1;

        if (compare == Expression.BIGGER) {
            iTest = 0;
        }

        while (x != null) {
            if (Trace.STOP) {
                Trace.stop();
            }

            boolean t = compareValue(value, x.getData()[iColumn_0]) >= iTest;

            if (t) {
                Node r = x.getRight();

                if (r == null) {
                    break;
                }

                x = r;
            } else {
                Node l = x.getLeft();

                if (l == null) {
                    break;
                }

                x = l;
            }
        }

        while (x != null
                && compareValue(value, x.getData()[iColumn_0]) >= iTest) {
            if (Trace.STOP) {
                Trace.stop();
            }

            x = next(x);
        }

        return x;
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @throws SQLException
     */
    Node first() throws SQLException {

        Node x = root,
             l = x;

        while (l != null) {
            if (Trace.STOP) {
                Trace.stop();
            }

            x = l;
            l = x.getLeft();
        }

        return x;
    }

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
    Node next(Node x) throws SQLException {

        if (x == null) {
            return null;
        }

        Node r = x.getRight();

        if (r != null) {
            x = r;

            Node l = x.getLeft();

            while (l != null) {
                if (Trace.STOP) {
                    Trace.stop();
                }

                x = l;
                l = x.getLeft();
            }

            return x;
        }

        Node ch = x;

        x = x.getParent();

        while (x != null && ch.equals(x.getRight())) {
            if (Trace.STOP) {
                Trace.stop();
            }

            ch = x;
            x  = x.getParent();
        }

        return x;
    }

    /**
     * Method declaration
     *
     *
     * @param x
     * @param w
     *
     * @return
     *
     * @throws SQLException
     */
    private Node child(Node x, boolean isleft) throws SQLException {
        return isleft ? x.getLeft()
                      : x.getRight();
    }

    /**
     * Method declaration
     *
     *
     * @param x
     * @param n
     *
     * @throws SQLException
     */
    private void replace(Node x, Node n) throws SQLException {

        if (x.equals(root)) {
            root = n;

            if (n != null) {
                n.setParent(null);
            }
        } else {
            set(x.getParent(), x.isFromLeft(), n);
        }
    }

    /**
     * Method declaration
     *
     *
     * @param x
     * @param w
     * @param n
     *
     * @throws SQLException
     */
    private void set(Node x, boolean isleft, Node n) throws SQLException {

        if (isleft) {
            x.setLeft(n);
        } else {
            x.setRight(n);
        }

        if (n != null) {
            n.setParent(x);
        }
    }

    /**
     * Method declaration
     *
     *
     * @param d
     *
     * @return
     *
     * @throws SQLException
     */
    Node search(Object d[]) throws SQLException {

        Node x = root;

        while (x != null) {
            if (Trace.STOP) {
                Trace.stop();
            }

            int c = compareRow(d, x.getData());

            if (c == 0) {
                return x;
            } else if (c < 0) {
                x = x.getLeft();
            } else {
                x = x.getRight();
            }
        }

        return null;
    }

    /**
     * This method is used for finding foreign key references.
     *
     * It finds a row by comparing the values set in a[] and mapping to b[].
     * a[] contains only the column values which correspond to the columns
     * of the index. It does not necessarily cover
     * all the columns of the index, only the first a.length columns.
     *
     * b[] contains all the visible columns in a row of the table.
     *
     *
     * @param a a set of column values
     * @param b a full row
     *
     * @return
     *
     * @throws SQLException
     */
    int comparePartialRowNonUnique(Object a[],
                                   Object b[]) throws SQLException {

        int i = Column.compare(a[0], b[iColumn_0], iType_0);

        if (i != 0) {
            return i;
        }

        int fieldcount = visibleColumns;

        for (int j = 1; j < a.length && j < fieldcount; j++) {
            Object o = a[j];

            if (o == null) {
                continue;
            }

            i = Column.compare(o, b[iColumn[j]], iType[j]);

            if (i != 0) {
                return i;
            }
        }

        return 0;
    }

    // todo: this is a hack

    /**
     * compares two full table rows based on the columns of the index
     *
     *
     * @param a a full row
     * @param b a full row
     *
     * @return
     *
     * @throws SQLException
     */
    private int compareRowNonUnique(Object a[],
                                    Object b[]) throws SQLException {

        int i = Column.compare(a[iColumn_0], b[iColumn_0], iType_0);

        if (i != 0) {
            return i;
        }

        int fieldcount = visibleColumns;

        for (int j = 1; j < fieldcount; j++) {
            i = Column.compare(a[iColumn[j]], b[iColumn[j]], iType[j]);

            if (i != 0) {
                return i;
            }
        }

        return 0;
    }

    /**
     * Method declaration
     *
     *
     * @param a
     * @param b
     *
     * @return
     *
     * @throws SQLException
     */
    private int compareRow(Object a[], Object b[]) throws SQLException {

        int i = Column.compare(a[iColumn_0], b[iColumn_0], iType_0);

        if (i != 0) {
            return i;
        }

        for (int j = 1; j < iFields; j++) {
            i = Column.compare(a[iColumn[j]], b[iColumn[j]], iType[j]);

            if (i != 0) {
                return i;
            }
        }

        return 0;
    }

    /**
     * Method declaration
     *
     *
     * @param a
     * @param b
     *
     * @return
     *
     * @throws SQLException
     */
    private int compareValue(Object a, Object b) throws SQLException {
        return Column.compare(a, b, iType_0);
    }
}
