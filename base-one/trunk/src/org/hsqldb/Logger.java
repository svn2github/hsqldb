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

import java.net.URL;
import org.hsqldb.HsqlNameManager.HsqlName;

// boucherb@users 20030510 - patch 1.7.2 - added cooperative file locking

/**
 *  Transitional interface for log and cache management. In the future,
 *  this will form the basis for the public interface of logging and cache
 *  classes.<p>
 *
 *  Implements a storage manager wrapper that provides a consistent,
 *  always available interface to storage management for the Database
 *  class, despite the fact not all Database objects actually use file
 *  storage.<p>
 *
 *  The Logger class makes it possible avoid the necessity to test for a
 *  null Log Database attribute again and again, in many different places,
 *  and generally avoids tight coupling between Database and Log, opening
 *  the doors for multiple logs/caches in the future. In this way, the
 *  Database class does not need to know the details of the Logging/Cache
 *  implementation, lowering its breakability factor and promoting
 *  long-term code flexibility.
 */
class Logger {

    /**
     *  The Log object this Logger object wraps
     */
    Log lLog;

    /**
     *  The LockFile object this Logger uses to cooperatively lock
     *  the database files
     */
    LockFile lf;

    /**
     *  Opens the specified Database object's database files and starts up
     *  the logging process. <p>
     *
     *  If the specified Database object is a new database, its database
     *  files are first created.
     *
     * @param  db the Database
     * @throws  HsqlException if there is a problem, such as the case when
     *      the specified files are in use by another process
     */
    void openLog(Database db) throws HsqlException {

        String path;

        // NOTE:
        // this method never gets called unless path is non-null
        // so no check here
        path = db.getPath();

        if (db.isFilesInJar()) {
            checkFilesInJar(path);
        } else {
            acquireLock(path);
        }

        lLog = new Log(db, path);

        lLog.open();
    }

// fredt@users 20020130 - patch 495484 by boucherb@users

    /**
     *  Shuts down the logging process using the specified mode. <p>
     *
     * @param  closemode The mode in which to shut down the logging
     *      process
     *      <OL>
     *        <LI> closemode -1 performs SHUTDOWN IMMEDIATELY, equivalent
     *        to  a poweroff or crash.
     *        <LI> closemode 0 performs a normal SHUTDOWN that
     *        checkpoints the database normally.
     *        <LI> closemode 1 performs a shutdown compact that scripts
     *        out the contents of any CACHED tables to the log then
     *        deletes the existing *.data file that contains the data
     *        for all CACHED table before the normal checkpoint process
     *        which in turn creates a new, compact *.data file.
     *      </OL>
     *
     * @return  true if closed with no problems or false if a problem was
     *        encountered.
     */
    boolean closeLog(int closemode) {

        if (lLog == null) {
            return true;
        }

        try {
            lLog.stop();

            switch (closemode) {

                case Database.CLOSEMODE_IMMEDIATELY :
                    lLog.shutdown();
                    break;

                case Database.CLOSEMODE_NORMAL :
                    lLog.close(false, true);
                    break;

                case Database.CLOSEMODE_COMPACT :
                case Database.CLOSEMODE_SCRIPT :
                    lLog.close(true, true);
                    break;
            }
        } catch (Throwable e) {
            lLog = null;

            return false;
        }

        lLog = null;

        return true;
    }

    /**
     *  Determines if the logging process actually does anything. <p>
     *
     *  In-memory Database objects do not need to log anything. This
     *  method is essentially equivalent to testing whether this logger's
     *  database is an in-memory mode database.
     *
     * @return  true if this object encapsulates a non-null Log instance,
     *      else false
     */
    boolean hasLog() {
        return lLog != null;
    }

    /**
     *  Returns the Cache object or null if one doesn't exist.
     */
    Cache getCache() throws HsqlException {

        if (lLog != null) {
            return lLog.getCache();
        } else {
            return null;
        }
    }

    /**
     *  Records a Log entry representing a new connection action on the
     *  specified Session object.
     *
     * @param  session the Session object for which to record the log
     *      entry
     * @param  username the name of the User, as known to the database
     * @param  password the password of the user, as know to the database
     * @throws  HsqlException if there is a problem recording the Log
     *      entry
     */
    void logConnectUser(Session session, String username,
                        String password) throws HsqlException {

        if (lLog != null) {
            lLog.write(session,
                       "CONNECT USER " + username + " PASSWORD \"" + password
                       + "\"");
        }
    }

    /**
     *  Records a Log entry for the specified SQL statement, on behalf of
     *  the specified Session object.
     *
     * @param  session the Session object for which to record the Log
     *      entry
     * @param  statement the SQL statement to Log
     * @throws  HsqlException if there is a problem recording the entry
     */
    void writeToLog(Session session, String statement) throws HsqlException {

        if (lLog != null) {
            lLog.write(session, statement);
        }
    }

    void writeToLog(Session session, Table table,
                    Object[] row) throws HsqlException {

        if (lLog != null) {
            lLog.writeRow(session, table, row);
        }
    }

    void writeDeleteStatement(Session c, Table t,
                              Object[] row) throws HsqlException {

        if (lLog != null) {
            lLog.writeDeleteStatement(c, t, row);
        }
    }

    /**
     *  Checkpoints the database. <p>
     *
     *  The most important effect of calling this method is to cause the
     *  log file to be rewritten in the most efficient form to
     *  reflect the current state of the database, i.e. only the DDL and
     *  insert DML required to recreate the database in its present state.
     *  Other house-keeping duties are performed w.r.t. other database
     *  files, in order to ensure as much as possible the ACID properites
     *  of the database.
     *
     * @throws  HsqlException if there is a problem checkpointing the
     *      database
     */
    void checkpoint(boolean mode) throws HsqlException {

        if (lLog != null) {
            lLog.checkpoint(mode);
        }
    }

    /**
     *  Sets the maximum size to which the log file can grow
     *  before being automatically checkpointed.
     *
     * @param  i The size, in MB
     */
    void setLogSize(int i) {

        if (lLog != null) {
            lLog.setLogSize(i);
        }
    }

    /**
     *  Sets the type of script file, currently 0 for text (default)
     *  1 for binary and 3 for compressed
     *
     * @param  i The type
     */
    void setScriptType(int i) throws HsqlException {

        if (lLog != null) {
            lLog.setScriptType(i);
        }
    }

    /**
     *  Sets the log write delay mode on or off. When write delay mode is
     *  switched on, the strategy is that executed commands are written to
     *  the log at most 1 second after they are executed. This may
     *  improve performance for applications that execute a large number
     *  of short running statements in a short period of time, but risks
     *  failing to log some possibly large number of statements in the
     *  event of a crash. When switched off, the strategy is that all SQL
     *  commands are written to the log immediately after they
     *  are executed, resulting in possibly slower execution but with the
     *  maximum risk being the loss of at most one statement in the event
     *  of a crash.
     *
     * @param  delay if true, used a delayed write strategy, else use an
     *      immediate write strategy
     */
    void setWriteDelay(int delay) {

        if (lLog != null) {
            lLog.setWriteDelay(delay);
        }
    }

    /**
     *  Opens the TextCache object.
     */
    Cache openTextCache(HsqlName tablename, String source,
                        boolean readOnlyData,
                        boolean reversed) throws HsqlException {
        return lLog.openTextCache(tablename, source, readOnlyData, reversed);
    }

    /**
     *  Closes the TextCache object.
     */
    void closeTextCache(HsqlName name) throws HsqlException {
        lLog.closeTextCache(name);
    }

    /**
     * Attempts to aquire a cooperative lock condition on the database files
     */
    void acquireLock(String path) throws HsqlException {

        boolean locked;
        String  msg;

        if (lf != null) {
            return;
        }

        lf     = LockFile.newLockFile(path + ".lck");
        locked = false;
        msg    = "";

        try {
            locked = lf.tryLock();
        } catch (Exception e) {

            // e.printStackTrace();
            msg = e.toString();
        }

        if (!locked) {
            throw Trace.error(Trace.DATABASE_ALREADY_IN_USE, lf + ": " + msg);
        }
    }

    void releaseLock() {

        try {
            if (lf != null) {
                lf.tryRelease();
            }
        } catch (Exception e) {
            if (Trace.TRACE) {
                Trace.printSystemOut(e.toString());
            }
        }

        lf = null;
    }

    private void checkFilesInJar(String path) throws HsqlException {

        URL    url;
        String sScript;

        Trace.check(path != null, Trace.FILE_IO_ERROR, "path is null");

        sScript = path + ".script";
        url     = getClass().getResource(sScript);

        Trace.check(url != null, Trace.FILE_IO_ERROR, sScript,
                    " does not exist");
        Trace.check("jar".equalsIgnoreCase(url.getProtocol()),
                    Trace.ACCESS_IS_DENIED, "wrong resource protocol: ",
                    url.getProtocol());
    }
}
