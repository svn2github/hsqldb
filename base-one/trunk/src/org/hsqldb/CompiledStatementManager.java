/* Copyright (c) 2001-2002, The HSQL Development Group
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

import java.util.Hashtable;
import org.hsqldb.lib.Iterator;
import org.hsqldb.lib.IntKeyHashMap;
import org.hsqldb.store.ValuePool;

/**
 * Manages the registration, lookup and validation of CompiledStatement
 * objects for a Database instance.
 *
 * @author boucher@users.sourceforge.net
 * @since HSQLDB 1.7.2
 * @version 1.7.2
 */
public class CompiledStatementManager {

    /**
     * The Database for which this object is managing
     * CompiledStatementObjects.
     */
    Database database;

    /** Map:  SQL String => CompiledStatement */
    Hashtable sqlMap;

    /** Map: compiled statment id (int) => CompiledStatement object. */
    IntKeyHashMap csidMap;

    /** Map: Session id (int) => Map: compiled statement id (int) => SCN (Long); */
    IntKeyHashMap validationMap;

    /**
     * Monotonically increasing counter used to assign unique ids to compiled
     * statements.
     */
    private int next_cs_id;

    /**
     * Constructs a new instance of <code>CompiledStatementManager</code>.
     *
     * @param database the Database instance for which this object is to
     *      manage compiled statement objects.
     */
    CompiledStatementManager(Database database) {

        this.database = database;
        sqlMap        = new Hashtable();
        csidMap       = new IntKeyHashMap();
        validationMap = new IntKeyHashMap();
        next_cs_id    = 0;
    }

    void reset() {

        sqlMap.clear();
        csidMap.clear();
        validationMap.clear();

        next_cs_id = 0;
    }

    /**
     * Retrieves the next compiled statement identifier in the sequence.
     *
     * @return the next compiled statement identifier in the sequence.
     */
    private int nextID() {

        next_cs_id++;

        return next_cs_id;
    }

    /**
     * Associates the specified system change number with the specified
     * compiled statement identifier, relative to the specified session
     * identifier, returning the old scn associated with the (csid,sid)
     * pair, or a value less than zero if there was no such association.
     *
     * @param csid the compiled statement identifier
     * @param sid the session identifier
     * @param scn the system change number
     * @return the old scn associated with the (csid, sid) pair
     */
    private long setSCN(int csid, int sid, long scn) {

        IntKeyHashMap scsMap;
        Long          oldscn;

        scsMap = (IntKeyHashMap) validationMap.get(sid);

        if (scsMap == null) {
            scsMap = new IntKeyHashMap();

            validationMap.put(sid, scsMap);
        }

        oldscn = (Long) scsMap.put(csid, ValuePool.getLong(scn));

        return (oldscn == null) ? Long.MIN_VALUE
                                : oldscn.longValue();
    }

    /**
     * Retrieves the system change number associated with the specified
     * compiled statement identifier, in the context of the specified
     * session identifier.
     *
     * @param csid the compiled statement identifier
     * @param sid the session identifier
     * @return the system change number associated with the specified
     *        compiled statement identifier, in the context of
     *        the specified session identifier.
     */
    private long getSCN(int csid, int sid) {

        IntKeyHashMap scsMap;
        Long          scn;

        scsMap = (IntKeyHashMap) validationMap.get(sid);

        if (scsMap == null) {
            return Long.MIN_VALUE;
        }

        scn = (Long) scsMap.get(csid);

        return (scn == null) ? Long.MIN_VALUE
                             : scn.longValue();
    }

    /**
     * Retrieves the compiled statement identifier associated with the
     * specified SQL String, or a value less than zero, if no such
     * association exists.
     *
     * @param sql the SQL String
     * @return the compiled statement identifier associated with the
     *      specified SQL String
     */
    synchronized int getStatementID(String sql) {

        CompiledStatement cs;

        cs = (CompiledStatement) sqlMap.get(sql);

        return cs == null ? Integer.MIN_VALUE
                          : cs.id;
    }

    /**
     * Retrieves the CompiledStatement object having the specified compiled
     * statement identifier, or null if no such CompiledStatement object
     * exists.
     *
     * @param csid the identifier of the requested CompiledStatement object
     * @return the requested CompiledStatement object
     */
    synchronized CompiledStatement getStatement(int csid) {
        return (CompiledStatement) csidMap.get(csid);
    }

    /**
     * Retrieves whether the CompiledStatement object with the specified
     * identifier was definitely set as validated at or after the time of the
     * last potentially invalidating system change, relative to the specified
     * session identifier.
     *
     * @param csid the compiled statement identifier
     * @param sid the session identifier
     * @return true if the specified compiled statement was set valid
     *      after the last invalidation system change number
     */
    synchronized boolean isValid(int csid, int sid) {
        return getSCN(csid, sid) >= database.getDDLSCN();
    }

    /**
     * Retrieves the system change number last associated with the specified
     * compiled statement, relative to the specified session identifier.  If
     * no such association exists, a value less than zero is returned.
     *
     * @param csid the compiled statement identifier
     * @param sid the session identifier
     * @return the system change number last associated with the specified
     *      compiled statement, relative to the specified session identifier.
     */
    synchronized long getValidated(int csid, int sid) {
        return csid > 0 ? getSCN(csid, sid)
                        : Long.MIN_VALUE;
    }

    /**
     * Causes the specified system change number to be associated with the
     * specified compiled statement identifier as the last time the compiled
     * statement was determined to be valid, relative to the Session specified
     * by the session identifier.  If this is the first such operation relative
     * to the (csid,sid) pair, the use count of the compiled statement is
     * incremented.
     *
     * @param csid the compiled statement identifier
     * @param sid the session identifier
     * @param scn the system change number
     * @return the last scn at which the indicated compiled statement was set
     *      valid, relative to the indicated Session.  A value less than zero
     *      indicates that no such compiled statement is registered. A value of
     *      0 indicates that this is the first time the registered compiled
     *      statement has been set valid, relative to the specified Session.
     */
    synchronized long setValidated(int csid, int sid, long scn) {

        CompiledStatement cs;
        long              oldscn;

        cs = (CompiledStatement) csidMap.get(csid);

        if (cs == null) {
            return Long.MIN_VALUE;
        }

        oldscn = setSCN(cs.id, sid, scn);

        if (oldscn < 0) {
            cs.use++;
        }

        return oldscn < 0 ? 0
                          : oldscn;
    }

    /**
     * Binds the specified CompiledStatement object into this object's active
     * compiled statement registry.  It is trusted completely that the caller
     * is actually registering a previously unregistered CompiledStatement
     * object; no checks are done in the interest of performance. Typically,
     * the only caller should be a Session that is attempting to perform a
     * prepare and has discovered that this CompiledStatementManager has
     * no such statement registered, as indicated by a negative return value
     * from {@link #getStatementID(String) getStatementID()}.
     *
     * @param cs The CompiledStatement to add
     * @return The compiled statement id assigned to the freshly bound
     *      CompiledStatement object
     */
    synchronized int registerStatement(CompiledStatement cs) {

        cs.id  = nextID();
        cs.use = 0;

        sqlMap.put(cs.sql, cs);
        csidMap.put(cs.id, cs);

        return cs.id;
    }

    /**
     * Releases any claim that the session with the specified session
     * identifier may have on the compiled statement with the specified
     * compiled statement identifier.  This includes releasing the asscociated
     * validation resources, as well as possibly dropping the compiled
     * statement itself and any resources associated with it, if it is
     * determined that no other claims exists.
     *
     * @param csid the compiled statment identifier
     * @param sid the session identifier
     * @return true if the statement was actually deallocated
     */
    synchronized boolean freeStatement(int csid, int sid) {

        CompiledStatement cs;
        IntKeyHashMap     scsMap;

        cs     = (CompiledStatement) csidMap.get(csid);
        scsMap = (IntKeyHashMap) validationMap.get(sid);

        if (cs == null || scsMap == null || scsMap.remove(csid) == null) {
            return false;
        }

        cs.use--;

        if (cs.use < 1) {
            sqlMap.remove(cs.sql);
            csidMap.remove(cs.id);
        }

        return true;
    }

    /**
     * Releases the claim on any registered compiled statement objects
     * held by the session with the specified session identifier. This
     * includes releasing the asscociated validation resources, as well as
     * possibly dropping each or some of the compiled statements themselves
     * and any resources associated with them, if it is determined that no
     * other claims are held be different sessions.
     *
     * @param sid the session identifier
     */
    synchronized void processDisconnect(int sid) {

        IntKeyHashMap     scsMap;
        CompiledStatement cs;
        int               csid;
        Iterator          i;

        scsMap = (IntKeyHashMap) validationMap.remove(sid);

        if (scsMap == null) {
            return;
        }

        i = scsMap.keySet().iterator();

        while (i.hasNext()) {
            csid = i.nextInt();
            cs   = (CompiledStatement) csidMap.get(csid);

            cs.use--;

            if (cs.use < 1) {
                sqlMap.remove(cs.sql);
                csidMap.remove(cs.id);
            }
        }
    }
}
