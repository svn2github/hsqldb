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
import org.hsqldb.lib.Set;

/**
 * Contains a set of mappings from role String to
 * creating, finding, modifying and deleting Roles for a Database.
 * @author unsaved@users
 * @version  1.8.0
 * @since 1.8.0
 */
class RoleManager implements GrantConstants {

    /*
     * May want to change the map values to sets of Users instead of Strings.
     * I fear that because this class may need to initialize before Users.
     * Strings are working fine for current requirements.
     */

    /** The role name reserved for ADMIN users. */
    static final String ADMIN_ROLE_NAME = "DBA";

    /**
     * This object's set of Role objects. <p>
     * role-Strings-to-Grantee-object
     */
    private HashMappedList roleMap = new HashMappedList();

    /**
     * Construction happens once for each Database object.
     *
     * Creates special role ADMIN_ROLE_NAME.
     * Sets up association with the GranteeManager for this database.
     *
     */
    RoleManager(Database database) throws HsqlException {

        granteeManager = database.getGranteeManager();

        createRole(ADMIN_ROLE_NAME);
        getGrantee(ADMIN_ROLE_NAME).setAdmin(true);
    }

    private GranteeManager granteeManager;

    /**
     * Creates a new Role object under management of this object. <p>
     *
     *  A set of constraints regarding user creation is imposed: <p>
     *
     *  <OL>
     *    <LI>Can't create a role with name same as any right.
     *
     *    <LI>If the specified name is null, then an
     *        ASSERTION_FAILED exception is thrown stating that
     *        the name is null.
     *
     *    <LI>If this object's collection already contains an element whose
     *        name attribute equals the name argument, then
     *        a GRANTEE_ALREADY_EXISTS or ROLE_ALREADY_EXISTS Trace
     *        is thrown.
     *        (This will catch attempts to create Reserved grantee names).
     *  </OL>
     */
    String createRole(String name) throws HsqlException {

        /*
         * Role names can't be right names because that would cause
         * conflicts with "GRANT name TO...".  This doesn't apply to
         * User names or Grantee names in general, since you can't
         * "GRANT username TO...".  That's why this check is only in
         * this RoleManager class.
         */
        if (name == null) {
            Trace.doAssert(false, Trace.getMessage(Trace.NULL_NAME));
        }

        Grantee g = null;

        if (GranteeManager.validRightString(name)) {
            throw Trace.error(Trace.ILLEGAL_ROLE_NAME, name);
        }

        g = granteeManager.addGrantee(name, false);

        boolean result = roleMap.add(name, g);

        if (!result) {
            throw Trace.error(Trace.ROLE_ALREADY_EXISTS, name);
        }

        // I don't think can get this trace since every roleMap element
        // will have a Grantee element which was already verified
        // above.  Easier to leave this check here than research it.
        return name;
    }

    /**
     * Attempts to drop a Role with the specified name
     *  from this object's set. <p>
     *
     *  A successful drop action consists of: <p>
     *
     *  <UL>
     *
     *    <LI>removing the User object with the specified name
     *        from the set.
     *
     *    <LI>revoking all rights from the removed object<br>
     *        (this ensures that in case there are still references to the
     *        just dropped User object, those references
     *        cannot be used to erronously access database objects).
     *
     *  </UL> <p>
     *
     */
    void dropRole(String name) throws HsqlException {

        if (name.equals(ADMIN_ROLE_NAME)) {
            throw Trace.error(Trace.ACCESS_IS_DENIED);
        }

        granteeManager.removeRoleFromMembers(name);

        boolean result = granteeManager.removeGrantee(name);

        if (!result) {
            throw Trace.error(Trace.NO_SUCH_ROLE, name);
        }

        Grantee g = (Grantee) roleMap.remove(name);

        if (g == null) {
            throw Trace.error(Trace.NO_SUCH_GRANTEE, name);
        }
    }

    public Set getRoleNames() {
        return roleMap.keySet();
    }

    /**
     * Returns Grantee for the for the named Role
     */
    Grantee getGrantee(String name) throws HsqlException {

        if (!exists(name)) {
            Trace.doAssert(false, "No role '" + name + "'");
        }

        Grantee g = (Grantee) roleMap.get(name);

        if (g == null) {
            throw Trace.error(Trace.MISSING_GRANTEE, name);
        }

        return g;
    }

    boolean exists(String name) throws HsqlException {
        return roleMap.containsKey(name);
    }

    // Legacy wrappers
    static String[] getRightsArray(int rights) {
        return GranteeManager.getRightsArray(rights);
    }

    /**
     * Removes all rights mappings for the database object identified by
     * the dbobject argument from all Grantee objects in the set.
     */
    void removeDbObject(Object dbobject) {
        granteeManager.removeDbObject(dbobject);
    }
}
