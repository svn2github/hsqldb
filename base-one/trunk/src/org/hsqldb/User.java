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
import org.hsqldb.lib.HsqlHashMap;

// fredt@users 20021103 - patch 1.7.2 - fix bug in revokeAll()
// fredt@users 20021103 - patch 1.7.2 - allow for drop table, etc.
// when tables are dropped or renamed, changes are reflected in the
// permissions held in User objects.

/**
 * A User objects holds the rights for a particular user, plus a reference
 * to the special uPublic. checkXXX() and getXXX() methods use the uPublic
 * rights as well individually granted rights to decide which rights exist
 * for the user.
 *
 *
 * @version 1.7.2
 */
class User {

    private boolean     bAdministrator;
    private HsqlHashMap rightsMap;
    private String      sName, sPassword;
    private User        uPublic;

    /**
     * Constructor declaration
     *
     *
     * @param name
     * @param password
     * @param admin
     * @param pub
     */
    User(String name, String password, boolean admin, User pub) {

        rightsMap = new HsqlHashMap();
        sName     = name;

        setPassword(password);

        bAdministrator = admin;
        uPublic        = pub;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    String getName() {
        return sName;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    String getPassword() {

        // necessary to create the script
        return sPassword;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    HsqlHashMap getRights() {

        // necessary to create the script
        return rightsMap;
    }

    /**
     * Method declaration
     *
     *
     * @param password
     */
    void setPassword(String password) {
        sPassword = password;
    }

    /**
     * Method declaration
     *
     *
     * @param test
     *
     * @throws SQLException
     */
    void checkPassword(String test) throws SQLException {
        Trace.check(test.equals(sPassword), Trace.ACCESS_IS_DENIED);
    }

    /**
     * changes made to keys stored in rightsMap to use HsqlName objects for
     * tables. This allows rights to persist when a table is renamed.
     *
     * @param object
     * @param right
     */
    void grant(Object dbobject, int right) {

        Integer n = (Integer) rightsMap.get(dbobject);

        if (n == null) {
            n = new Integer(right);
        } else {
            n = new Integer(n.intValue() | right);
        }

        rightsMap.put(dbobject, n);
    }

    /**
     * Method declaration
     *
     *
     * @param object
     * @param right
     */
    void revoke(Object dbobject, int right) {

        Integer n = (Integer) rightsMap.get(dbobject);

        if (n == null) {
            n = new Integer(right);
        } else {
            n = new Integer(n.intValue() & (UserManager.ALL - right));
        }

        rightsMap.put(dbobject, n);
    }

    /**
     * Revokes all righs for an object. Used when tables are dropped.
     */
    void revokeDbObject(Object dbobject) {
        rightsMap.remove(dbobject);
    }

    /**
     * Revokes all rights for this user.
     *
     */
    void revokeAll() {

        rightsMap.clear();

        bAdministrator = false;
    }

    /**
     * Method declaration
     *
     *
     * @param object
     * @param right
     *
     * @throws SQLException
     */
    void check(Object dbobject, int right) throws SQLException {

        if (bAdministrator) {
            return;
        }

        Integer n;

        n = (Integer) rightsMap.get(dbobject);

        if ((n != null) && (n.intValue() & right) != 0) {
            return;
        }

        if (uPublic != null) {
            n = (Integer) (uPublic.rightsMap).get(dbobject);

            if ((n != null) && (n.intValue() & right) != 0) {
                return;
            }
        }

        throw Trace.error(Trace.ACCESS_IS_DENIED);
    }

    /**
     * Method declaration
     *
     *
     * @throws SQLException
     */
    void checkAdmin() throws SQLException {
        Trace.check(isAdmin(), Trace.ACCESS_IS_DENIED);
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    boolean isAdmin() {
        return bAdministrator;
    }
}
