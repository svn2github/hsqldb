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

import org.hsqldb.lib.HashMappedList;
import org.hsqldb.lib.HashSet;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.IntKeyHashMap;
import org.hsqldb.lib.IntValueHashMap;
import org.hsqldb.lib.Iterator;
import org.hsqldb.lib.StringUtil;
import org.hsqldb.lib.Collection;

/**
 * Contains a set of Grantee objects, and supports operations for creating,
 * finding, modifying and deleting Grantee objects for a Database; plus
 * Administrative privileges.
 *
 * @version  1.8.0
 * @sincd 1.8.0
 * @see  Grantee
 */
class GranteeManager implements GrantConstants {

    /*
     * Our map here has the same keys as the UserManager + RoleManager maps
     * EXCEPT that we include the SYSTEM_AUTHORIZATION_NAME because we need
     * to keep track of those permissions, but not his identity.
     * I.e., our list here is all-inclusive, whether the User or Role is
     * visible to database users or not.
     */

    /** Only needed to link to the RoleManager later on. */
    private Database database;

    /**
     * Used to provide access to the RoleManager for Grantee.isAccessible()
     * lookups
     */
    private RoleManager roleManager = null;

    /**
     * Used to provide access to the RoleManager for Grantee.isAccessible()
     * lookups
     */
    RoleManager getRoleManager() {
        return roleManager;
    }

    void linkRoleManager() throws HsqlException {

        roleManager = database.getRoleManager();

        Trace.doAssert(roleManager != null,
                       Trace.getMessage(Trace.MISSING_ROLEMANAGER));
    }

    /**
     * Construct the GranteeManager for a Database.
     *
     * Construct special Grantee objects for PUBLIC and SYS, and add them
     * to the Grantee map.
     * We depend on the corresponding User accounts being created
     * independently so as to remove a dependency to the UserManager class.
     *
     * @param inDatabase Only needed to link to the RoleManager later on.
     */
    public GranteeManager(Database inDatabase) throws HsqlException {
        database = inDatabase;
    }

    /**
     * Map of String-to-Grantee-objects.<p>
     * Primary object maintained by this class
     */
    private HashMappedList map = new HashMappedList();

    //
    static final IntValueHashMap rightsStringLookup = new IntValueHashMap(7);

    static {
        rightsStringLookup.put(S_R_ALL, ALL);
        rightsStringLookup.put(S_R_SELECT, SELECT);
        rightsStringLookup.put(S_R_UPDATE, UPDATE);
        rightsStringLookup.put(S_R_DELETE, DELETE);
        rightsStringLookup.put(S_R_INSERT, INSERT);
    }

    /**
     * Grants the rights represented by the rights argument on
     * the database object identified by the dbobject argument
     * to the Grantee object identified by name argument.<p>
     *
     *  Note: For the dbobject argument, Java Class objects are identified
     *  using a String object whose value is the fully qualified name
     *  of the Class, while Table objects are
     *  identified by an HsqlName object.  A Table
     *  object identifier must be precisely the one obtained by calling
     *  table.getName(); if a different HsqlName
     *  object with an identical name attribute is specified, then
     *  rights checks and tests will fail, since the HsqlName
     *  class implements its {@link HsqlName#hashCode hashCode} and
     *  {@link HsqlName#equals equals} methods based on pure object
     *  identity, rather than on attribute values. <p>
     */
    void grant(String name, Object dbobject,
               int rights) throws HsqlException {

        Grantee g = get(name);

        Trace.check(g != null, Trace.NO_SUCH_GRANTEE, name);
        get(name).grant(dbobject, rights);
    }

    /**
     * Grant a role to this Grantee.
     */
    void grant(String name, String role) throws HsqlException {

        Trace.check(!role.equals(name), Trace.CIRCULAR_GRANT, role);

        Grantee g = get(name);

        Trace.check(g != null, Trace.NO_SUCH_GRANTEE, name);
        Trace.check(!g.getDirectRoles().contains(role),
                    Trace.ALREADY_HAVE_ROLE, role);
        g.grant(role);
    }

    /**
     * Revoke a role from this Grantee
     */
    void revoke(String name, String role) throws HsqlException {

        Grantee g = get(name);

        Trace.check(g != null, Trace.NO_SUCH_GRANTEE, name);
        g.revoke(role);
    }

    /**
     * Revokes the rights represented by the rights argument on
     * the database object identified by the dbobject argument
     * from the User object identified by the name
     * argument.<p>
     * @see #grant
     */
    void revoke(String name, Object dbobject,
                int rights) throws HsqlException {
        get(name).revoke(dbobject, rights);
    }

    /**
     * Returns true if named Grantee object exists.
     * This will return true for reserved Grantees
     * SYSTEM_AUTHORIZATION_NAME, ADMIN_ROLE_NAME, PUBLIC_USER_NAME.
     */
    boolean isGrantee(String name) {
        return (map.containsKey(name));
    }

    /**
     * Removes all rights mappings for the database object identified by
     * the dbobject argument from all Grantee objects in the set.
     */
    void removeDbObject(Object dbobject) {

        Iterator it = map.values().iterator();

        for (; it.hasNext(); ) {
            ((Grantee) it.next()).revokeDbObject(dbobject);
        }
    }

    /**
     * Removes a role from all members
     */
    public void removeRoleFromMembers(String name) throws HsqlException {

        Iterator it = map.values().iterator();
        Grantee  g;

        for (; it.hasNext(); ) {
            g = (Grantee) it.next();

            if (g.hasRoleDirect(name)) {
                g.revoke(name);
            }
        }
    }

    public boolean removeGrantee(String name) {

        /*
         * Explicitly can't remove PUBLIC_USER_NAME.  Other reserveds are
         * taken care of by verifyNotReserved().
         */
        if (name.equals(UserManager.PUBLIC_USER_NAME)) {
            return false;
        }

        try {
            verifyNotReserved(name);
        } catch (HsqlException he) {
            return false;
        }

        Grantee g = (Grantee) map.remove(name);

        if (g == null) {
            return false;
        }

        g.clearPrivileges();

        return true;
    }

    /**
     * We don't have to worry about anything manually creating a reserved
     * account, because the reserved accounts are created upon DB
     * initialization.  If somebody tries to create one of these accounts
     * after that, it will fail because the account will already exist.
     * (We do prevent them from being removed, elsewhere!)
     */
    public Grantee addGrantee(String name,
                              boolean nestPublic) throws HsqlException {

        Trace.check(!map.containsKey(name), Trace.GRANTEE_ALREADY_EXISTS,
                    name);

        Grantee pubGrantee = null;

        if (nestPublic) {
            pubGrantee = get(UserManager.PUBLIC_USER_NAME);

            Trace.doAssert(pubGrantee != null,
                           Trace.getMessage(Trace.MISSING_PUBLIC_GRANTEE));
        }

        Grantee g = new Grantee(name, pubGrantee, this);

        map.put(name, g);

        return g;
    }

    static int getCheckRight(String right) throws HsqlException {

        int r = getRight(right);

        if (r != 0) {
            return r;
        }

        throw Trace.error(Trace.NO_SUCH_RIGHT, right);
    }

    /**
     * Translate a string representation or right(s) into its numeric form.
     */
    static int getRight(String right) {
        return rightsStringLookup.get(right, 0);
    }

    /**
     * Returns a comma separated list of right names corresponding to the
     * right flags set in the right argument. <p>
     */
    static String getRightsList(int rights) {

//        checkValidFlags(right);
        if (rights == 0) {
            return null;
        }

        if (rights == ALL) {
            return S_R_ALL;
        }

        return StringUtil.getList(getRightsArray(rights), ",", "");
    }

    /**
     * Retrieves the list of right names represented by the right flags
     * set in the specified <code>Integer</code> object's <code>int</code>
     * value. <p>
     *
     * @param rights An Integer representing a set of right flags
     * @return an empty list if the specified <code>Integer</code> object is
     *        null, else a list of rights, as <code>String</code> objects,
     *        represented by the rights flag bits set in the specified
     *        <code>Integer</code> object's int value.
     *
     */
    static String[] getRightsArray(int rights) {

        if (rights == 0) {
            return emptyRightsList;
        }

        String[] list = (String[]) hRightsLists.get(rights);

        if (list != null) {
            return list;
        }

        list = getRightsArraySub(rights);

        hRightsLists.put(rights, list);

        return list;
    }

    private static String[] getRightsArraySub(int right) {

//        checkValidFlags(right);
        if (right == 0) {
            return emptyRightsList;
        }

        HsqlArrayList a  = new HsqlArrayList();
        Iterator      it = rightsStringLookup.keySet().iterator();

        for (; it.hasNext(); ) {
            String rightString = (String) it.next();

            if (rightString.equals(S_R_ALL)) {
                continue;
            }

            int i = rightsStringLookup.get(rightString, 0);

            if ((right & i) != 0) {
                a.add(rightString);
            }
        }

        return (String[]) a.toArray(new String[a.size()]);
    }

    /**
     * An empty list that is returned from
     * {@link #listTablePrivileges listTablePrivileges} when
     * it is detected that neither this <code>User</code> object or
     * its <code>PUBLIC</code> <code>User</code> object attribute have been
     * granted any rights on the <code>Table</code> object identified by
     * the specified <code>HsqlName</code> object.
     *
     */
    static final String[] emptyRightsList = new String[0];

    /**
     * MAP:  int => HsqlArrayList. <p>
     *
     * This map caches the lists of <code>String</code> objects naming the rights
     * corresponding to each valid set of rights flags, as returned by
     * {@link #listRightNames listRightNames}
     *
     */
    static final IntKeyHashMap hRightsLists = new IntKeyHashMap();

    /**
     * Retrieves the set of distinct, fully qualified Java <code>Class</code>
     * names upon which any grants currently exist to elements in
     * this collection. <p>
     * @return the set of distinct, fully qualified Java Class names, as
     *        <code>String</code> objects, upon which grants currently exist
     *        to the elements of this collection
     *
     */
    HashSet getGrantedClassNames() throws HsqlException {

        int      size;
        Grantee  grantee;
        HashSet  out;
        Iterator e;

        size = map.size();
        out  = new HashSet();

        for (int i = 0; i < size; i++) {
            grantee = (Grantee) map.get(i);

            if (grantee == null) {
                continue;
            }

            e = grantee.getGrantedClassNames(false).iterator();

            while (e.hasNext()) {
                out.add(e.next());
            }
        }

        return out;
    }

    public Grantee get(String name) {
        return (Grantee) map.get(name);
    }

    public Collection getGrantees() {
        return map.values();
    }

    static public boolean validRightString(String rightString) {
        return getRight(rightString) != 0;
    }

    static public void verifyNotReserved(String name) throws HsqlException {

        boolean reservedUser =
            name.equals(UserManager.SYSTEM_AUTHORIZATION_NAME)
            || name.equals(RoleManager.ADMIN_ROLE_NAME);

        // Check for the 2 items which we prohibit grants/revokes to/from.
        Trace.check(!reservedUser, Trace.NONMOD_GRANTEE, name);
    }
}
