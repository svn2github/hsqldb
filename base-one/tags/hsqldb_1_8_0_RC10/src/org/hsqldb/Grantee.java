/* Copyright (c) 2001-2005, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
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

import org.hsqldb.HsqlNameManager.HsqlName;
import org.hsqldb.lib.HashSet;
import org.hsqldb.lib.IntValueHashMap;
import org.hsqldb.lib.Iterator;
import org.hsqldb.lib.Set;

/**
 * A Grantee Object holds the name, access and administrative rights for a
 * particular grantee.<p>
 * It supplies the methods used to grant, revoke, test
 * and check a grantee's access rights to other database objects.
 * It also holds a reference to the common PUBLIC User Object,
 * which represent the special user refered to in
 * GRANT ... TO PUBLIC statements.<p>
 * The check(), isAccessible() and getGrantedClassNames() methods check the
 * rights granted to the PUBLIC User Object, in addition to individually
 * granted rights, in order to decide which rights exist for the user.
 *
 * Method names ending in Direct indicate methods which do not recurse
 * to look through Roles which "this" object is a member of.
 * @author boucherb@users
 * @author fredt@usrs
 * @author unsaved@users
 *
 * @version 1.8.0
 * @since 1.8.0
 */
public class Grantee {

    /* All recursion is isolated into the addGranteeAndRoles() method.  */

    /** true if this user has database administrator role. */
    private boolean isAdministrator = false;

    /**
     * Grantee name.
     * This is redundant, since User and Role already store name.
     */
    private String sName;

    /** map with database object identifier keys and access privileges values */
    private IntValueHashMap rightsMap;

    /**
     * The special PUBLIC Grantee object. <p>
     *
     * Note: All Grantee objects except the special
     * SYS and PUBLIC Grantee objects contain a reference to this object
     */
    private Grantee pubGrantee;

    /** Needed only to give access to the roles for this database */
    private GranteeManager granteeManager;

    /**
     * Constructor, with a argument reference to the PUBLIC User Object which
     * is null if this is the SYS or PUBLIC user.
     *
     * The dependency upon a GranteeManager is undesirable.  Hopefully we
     * can get rid of this dependency with an IOC or Listener re-design.
     */
    Grantee(String name, Grantee inGrantee,
            GranteeManager man) throws HsqlException {

        rightsMap      = new IntValueHashMap();
        sName          = name;
        granteeManager = man;
        pubGrantee     = inGrantee;
    }

    String getName() {
        return sName;
    }

    /**
     * Retrieves the map object that represents the rights that have been
     * granted on database objects.  <p>
     *
     * The map has keys and values with the following interpretation: <P>
     *
     * <UL>
     * <LI> The keys are generally (but not limited to) objects having
     *      an attribute or value equal to the name of an actual database
     *      object.
     *
     * <LI> Specifically, the keys act as database object identifiers.
     *
     * <LI> The values are always Integer objects, each formed by combining
     *      a set of flags, one for each of the access rights defined in
     *      UserManager: {SELECT, INSERT, UPDATE and DELETE}.
     * </UL>
     */
    IntValueHashMap getRights() {

        // necessary to create the script
        return rightsMap;
    }

    /** These are the DIRECT roles.  Each of these may contain nested roles */
    private HashSet roles = new HashSet();

    /**
     * Grant a role
     */
    public void grant(String role) throws HsqlException {
        roles.add(role);
    }

    /**
     * Revoke a direct role
     */
    public void revoke(String role) throws HsqlException {
        Trace.check(hasRoleDirect(role), Trace.DONT_HAVE_ROLE, role);
        roles.remove(role);
    }

    /**
     * Gets direct roles, not roles nested within them.
     */
    public HashSet getDirectRoles() {
        return roles;
    }

    String getDirectRolesString() {
        return setToString(roles);
    }

    String getAllRolesString() {
        return setToString(getAllRoles());
    }

    public String setToString(Set set) {

        // Should be sorted
        // Iterator it = (new java.util.TreeSet(roles)).iterator();
        Iterator     it = set.iterator();
        StringBuffer sb = new StringBuffer();

        while (it.hasNext()) {
            if (sb.length() > 0) {
                sb.append(',');
            }

            sb.append(it.next());
        }

        return sb.toString();
    }

    /**
     * Gets direct and nested roles.
     */
    public HashSet getAllRoles() {

        HashSet newSet = new HashSet();

        addGranteeAndRoles(newSet);

        // Since we added "Grantee" in addition to Roles, need to remove self.
        newSet.remove(sName);

        return newSet;
    }

    /**
     * Adds to given Set this.sName plus all roles and nested roles.
     *
     * @return Given role with new elements added.
     */
    private HashSet addGranteeAndRoles(HashSet set) {

        RoleManager rm = getRoleManager();
        String      candidateRole;

        set.add(sName);

        Iterator it = roles.iterator();

        while (it.hasNext()) {
            candidateRole = (String) it.next();

            if (!set.contains(candidateRole)) {
                try {
                    rm.getGrantee(candidateRole).addGranteeAndRoles(set);
                } catch (HsqlException he) {
                    throw new RuntimeException(he.getMessage());
                }
            }
        }

        return set;
    }

    public boolean hasRoleDirect(String role) {
        return roles.contains(role);
    }

    public boolean hasRole(String role) {
        return getAllRoles().contains(role);
    }

    public String allRolesString() {

        HashSet allRoles = getAllRoles();

        if (allRoles.size() < 1) {
            return null;
        }

        Iterator     it = getAllRoles().iterator();
        StringBuffer sb = new StringBuffer();

        while (it.hasNext()) {
            if (sb.length() > 1) {
                sb.append(',');
            }

            sb.append((String) it.next());
        }

        return sb.toString();
    }

    /**
     * Grants the specified rights on the specified database object. <p>
     *
     * Keys stored in rightsMap for database tables are their HsqlName
     * attribute. This allows rights to persist when a table is renamed. <p>
     */
    void grant(Object dbobject, int rights) {

        if (rights == 0) {
            return;
        }

        int n = rightsMap.get(dbobject, 0);

        n |= rights;

        rightsMap.put(dbobject, n);
    }

    /**
     * Revokes the specified rights on the specified database object. <p>
     *
     * If, after removing the specified rights, no rights remain on the
     * database object, then the key/value pair for that object is removed
     * from the rights map
     */
    void revoke(Object dbobject, int rights) {

        if (rights == 0) {
            return;
        }

        int n = rightsMap.get(dbobject, 0);

        if (n == 0) {
            return;
        }

        rights = n & (GranteeManager.ALL - rights);

        if (rights == 0) {
            rightsMap.remove(dbobject);
        } else {
            rightsMap.put(dbobject, rights);
        }
    }

    /**
     * Revokes all rights on the specified database object.<p>
     *
     * This method removes any existing mapping from the rights map
     */
    void revokeDbObject(Object dbobject) {
        rightsMap.remove(dbobject);
    }

    /**
     * Revokes all rights from this Grantee object.  The map is cleared and
     * the database administrator role attribute is set false.
     */
    void clearPrivileges() {

        roles.clear();
        rightsMap.clear();

        isAdministrator = false;
    }

    /**
     * Checks if any of the rights represented by the rights
     * argument have been granted on the specified database object. <p>
     *
     * This is done by checking that a mapping exists in the rights map
     * from the dbobject argument for at least one of the rights
     * contained in the rights argument. Otherwise, it throws.
     */
    void check(Object dbobject, int rights) throws HsqlException {
        Trace.check(isAccessible(dbobject, rights), Trace.ACCESS_IS_DENIED);
    }

    /**
     * Returns true if any of the rights represented by the
     * rights argument has been granted on the database object identified
     * by the dbobject argument. <p>
     *
     * This is done by checking that a mapping exists in the rights map
     * from the dbobject argument for at least one of the rights
     * contained in the rights argument.
     *
     * Only does one level of recursion into isDirectlyAccessible.
     */
    boolean isAccessible(Object dbobject, int rights) throws HsqlException {

        /*
         * The deep recusion is all done in getAllRoles().  This method
         * only recurses one level into isDirectlyAccessible().
         */
        if (isDirectlyAccessible(dbobject, rights)) {
            return true;
        }

        if (pubGrantee != null && pubGrantee.isAccessible(dbobject, rights)) {
            return true;
        }

        RoleManager rm = getRoleManager();
        Iterator    it = getAllRoles().iterator();

        while (it.hasNext()) {
            if (((Grantee) rm.getGrantee(
                    (String) it.next())).isDirectlyAccessible(
                        dbobject, rights)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if any of the rights represented by the
     * rights argument has been granted on the database object identified
     * by the dbobject argument. <p>
     *
     * This is done by checking that a mapping exists in the rights map
     * from the dbobject argument for at least one of the rights
     * contained in the rights argument.
     *
     * Does not consider pubGrantee nor nested roles.  I.e., if "this"
     * is a role, then it will be checked, but roles that this user
     * or role are a member of will not be checked.
     */
    boolean isDirectlyAccessible(Object dbobject,
                                 int rights) throws HsqlException {

        if (isAdministrator) {
            return true;
        }

        if (dbobject instanceof String) {
            if (((String) dbobject).startsWith("org.hsqldb.Library")
                    || ((String) dbobject).startsWith("java.lang.Math")) {
                return true;
            }
        }

        int n = rightsMap.get(dbobject, 0);

        if (n != 0) {
            return (n & rights) != 0;
        }

        return false;
    }

    /**
     * Returns true if any right at all has been granted to this User object
     * on the database object identified by the dbobject argument.
     */
    boolean isAccessible(Object dbobject) throws HsqlException {
        return isAccessible(dbobject, GranteeManager.ALL);
    }

    /**
     * Checks that this User object is for a user with the
     * database administrator role. Otherwise it throws.
     */
    void checkAdmin() throws HsqlException {
        Trace.check(isAdmin(), Trace.ACCESS_IS_DENIED);
    }

    /**
     * Returns true if this User object is for a user with the
     * database administrator privileges.
     * Recurses through nested roles.
     */
    boolean isAdmin() {

        if (isAdminDirect()) {
            return true;
        }

        RoleManager rm = getRoleManager();
        Iterator    it = getAllRoles().iterator();

        while (it.hasNext()) {
            try {
                if (((Grantee) rm.getGrantee(
                        (String) it.next())).isAdminDirect()) {
                    return true;
                }
            } catch (HsqlException he) {
                throw Trace.runtimeError(Trace.RETRIEVE_NEST_ROLE_FAIL,
                                         he.getMessage());
            }
        }

        return false;
    }

    /**
     * Returns true if this User object is for a user with Direct
     * database administrator privileges.
     * I.e., if thise User/Role has Admin priv. directly, not via a
     * nested Role.
     */
    boolean isAdminDirect() {
        return isAdministrator;
    }

    /**
     * Retrieves the distinct set of Java <code>Class</code> FQNs
     * for which this <code>User</code> object has been
     * granted <code>ALL</code> (the Class execution privilege). <p>
     * @param andToPublic if <code>true</code>, then the set includes the
     *        names of classes accessible to this <code>User</code> object
     *        through grants to its Roles + <code>PUBLIC</code>
     *        <code>User</code> object attribute, else only role grants
     *        + direct grants are included.
     * @return the distinct set of Java Class FQNs for which this
     *        this <code>User</code> object has been granted
     *        <code>ALL</code>.
     * @since HSQLDB 1.7.2
     *
     */
    HashSet getGrantedClassNames(boolean andToPublic) throws HsqlException {

        IntValueHashMap rights;
        Object          key;
        int             right;
        Iterator        i;

        rights = rightsMap;

        HashSet out = getGrantedClassNamesDirect();

        if (andToPublic && pubGrantee != null) {
            rights = pubGrantee.rightsMap;
            i      = rights.keySet().iterator();

            while (i.hasNext()) {
                key = i.next();

                if (key instanceof String) {
                    right = rights.get(key, 0);

                    if (right == GranteeManager.ALL) {
                        out.add(key);
                    }
                }
            }
        }

        Grantee     g;
        RoleManager rm = getRoleManager();
        Iterator    it = getAllRoles().iterator();

        while (it.hasNext()) {
            out.addAll(
                ((Grantee) rm.getGrantee(
                    (String) it.next())).getGrantedClassNamesDirect());
        }

        return out;
    }

    /**
     * Retrieves the distinct set of Java <code>Class</code> FQNs
     * for which this <code>User</code> object has directly been
     * granted <code>ALL</code> (the Class execution privilege).
     *
     * Does NOT check nested the pubGrantee nor nested roles.
     * @return the distinct set of Java Class FQNs for which this
     *        this <code>User</code> object has been granted
     *        <code>ALL</code>.
     * @since HSQLDB 1.7.2
     *
     */
    HashSet getGrantedClassNamesDirect() throws HsqlException {

        IntValueHashMap rights;
        HashSet         out;
        Object          key;
        int             right;
        Iterator        i;

        rights = rightsMap;
        out    = new HashSet();
        i      = rightsMap.keySet().iterator();

        while (i.hasNext()) {
            key = i.next();

            if (key instanceof String) {
                right = rights.get(key, 0);

                if (right == GranteeManager.ALL) {
                    out.add(key);
                }
            }
        }

        return out;
    }

    /**
     * Retrieves a string[] whose elements are the names of the rights
     * explicitly granted with the GRANT command to this <code>User</code>
     * object on the <code>Table</code> object identified by the
     * <code>name</code> argument.
     * * @return array of Strings naming the rights granted to this
     *        <code>User</code> object on the <code>Table</code> object
     *        identified by the <code>name</code> argument.
     * @param name a <code>Table</code> object identifier
     * @since HSQLDB 1.7.2
     *
     */
    String[] listGrantedTablePrivileges(HsqlName name) {
        return GranteeManager.getRightsArray(rightsMap.get(name, 0));
    }

    private RoleManager getRoleManager() {
        return granteeManager.getRoleManager();
    }

    void setAdmin(boolean b) {
        isAdministrator = b;
    }
}
