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

import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.HsqlIntKeyHashMap;
import java.util.Enumeration;

/**
 * Container that maintains a map of session id's to Session objects.
 * Responsible for managing opening and closing of sessions.
 */
public class SessionManager {

    int                       sessionIdCount;
    private HsqlArrayList     sessionList = new HsqlArrayList();
    private HsqlIntKeyHashMap sessionMap  = new HsqlIntKeyHashMap();
    Session                   sysSession;

// TODO:
//
// Eliminate the Database-centric nature of SessionManager.
// e.g. Sessions should be able to migrate from one Database instance
// to another using session control language moderated by 
// SessionManager interacting with HsqlRuntime (a.k.a. Connection.setCatalog()).
// Possibly, make SessionManager an attribute of HsqlRuntime, rather than of
// Database.

    /**
     * Constructs an new SessionManager handling the specified Database using
     * the specified SYS User
     */
    public SessionManager(Database db, User sysUser) {
        sysSession = newSession(db, sysUser, false);
    }

// TODO:  
//
// It should be possible to create an initially 'disconnected' Session that
// can execute general commands using a SessionCommandInterpreter.
//    
// EXAMPLES: Open a Session to start a Server, add/remove
//           databases hosted by an existing Server, connect to a 
//           Database...
//
// REQUIRES:  HsqlRuntime auth scheme independent of any particular
//            Database instance 
//            e.g. provide service to use /etc/passwd and /etc/groups, 
//                 JAAS-plugin, etc.

    /**
     *  Binds the specified Session object into this SessionManager's active
     *  Session registry. This method is typically called internally from
     * {@link
     *  Database#connect(String,String) Database.connect(username,password)}
     *  as the final step, when a successful connection has been made.
     *
     * @param db the database to which the new Session is initially connected
     * @param user the initial Session User
     * @param readonly the initial ReadOnly attribute for the new Session
     */
    Session newSession(Database db, User user, boolean readonly) {

        Session s = new Session(db, user, true, readonly, sessionIdCount++);

        sessionMap.put(sessionIdCount, s);

        return s;
    }

// TODO:  
// sig change should be either:  getSysSession(Database) or getSysSession(dbID)

    /**
     * Retrieves the special SYS Session.
     *
     * @return the special SYS Session
     */
    Session getSysSession() {
        return sysSession;
    }

// TODO:  
// sig change should be either:  closeAllSessions(Database) or closeAllSessions(dbID)    

    /**
     * Closes all Sessions registered with this SessionManager.
     */
    void closeAllSessions() {

        // don't disconnect system user; need it to save database
        Enumeration en = sessionMap.elements();

        for (; en.hasMoreElements(); ) {
            Session s = (Session) en.nextElement();

            if (s != sysSession) {
                s.disconnect();
            }
        }
    }

    /**
     *  Handles the work requested by specified Session as a rewult of
     *  having issued the DISCONNECT SQL statement.
     *
     * @param  session to disconnect
     * @return the result of disconnecting the specified Session
     */
    Result processDisconnect(Session session) {

        sessionMap.remove(session.getId());

        if (!session.isClosed()) {
            session.disconnect();
        }

        return new Result();
    }

    /**
     * Removes all Sessions registered with this SessionManager.
     */
    void clearAll() {
        sessionMap.clear();
    }

    /**
     * Retrieves a list of the Sessions in this container that
     * are visible to the specified Session, given the access rights of
     * the Session User.
     *
     * @param session The Session determining visibility
     * @return the Sessions visible to the specified Session
     */
    HsqlArrayList listVisibleSessions(Session session) {

        HsqlArrayList out = new HsqlArrayList();
        Session       observed;
        boolean       isObserverAdmin = session.isAdmin();
        int           observerId      = session.getId();
        Enumeration   en              = sessionMap.elements();

        for (; en.hasMoreElements(); ) {
            observed = (Session) en.nextElement();

            if (observed == null) {

                // do nothing
            } else if (isObserverAdmin || observed.getId() == observerId) {
                out.add(observed);
            }
        }

        return out;
    }

    /**
     * Retrieves the Session with the specified Session identifier or null
     * if no such Session is registered with this SessionManager.
     */
    Session getSession(int id) {
        return (Session) sessionMap.get(id);
    }
}
