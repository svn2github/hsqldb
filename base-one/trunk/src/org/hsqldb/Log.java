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
import java.io.File;
import java.sql.SQLException;
import java.util.Enumeration;

//import java.util.zip.
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.HsqlHashMap;
import org.hsqldb.lib.HsqlStringBuffer;
import org.hsqldb.lib.FileUtil;
import org.hsqldb.lib.StopWatch;

// fredt@users 20020215 - patch 1.7.0 by fredt
// to move operations on the database.properties files to new
// class HsqlDatabaseProperties
// fredt@users 20020220 - patch 488200 by xclayl@users - throw exception
// throw addded to all methods relying on file io
// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
// fredt@users 20020405 - patch 1.7.0 by fredt - no change in db location
// because important information about the database is now stored in the
// *.properties file, all database files should be in the same folder as the
// *.properties file
// tony_lai@users 20020820 - export hsqldb.log_size to .properties file
// tony_lai@users 20020820 - changes to shutdown compact to save memory
// fredt@users 20020910 - patch 1.7.1 by Nitin Chauhan - code improvements
// fredt@users 20021208 - ongoing revamp
// fredt@users 20021212 - do not rewrite the *.backup file if the *.data
// file has not been updated in the current seesion.
/*
todo - when a *.script file that has no properties file is opened, it is
always assumed that the cache_version is 1.6.0, which may not be true;
- when both *.script and *.backup exist, there is a problem openning. the
backup does not get deflated into *.data and an attemp is made to read the
index roots from the empty *.data file.

*/

/**
 *  This class is responsible for most file handling. An HSQLDB database
 *  consists of a .properties file, a .script file (contains an SQL script),
 *  a .data file (contains data of cached tables) and a .backup file
 *  (contains the compressed .data file) <p>
 *
 *  This is an example of the .properties file. The version and the modified
 *  properties are automatically created by the database and should not be
 *  changed manually: <pre>
 *  modified=no
 *  version=1.43
 *  </pre>
 *  The following lines are optional, this means they are not created
 *  automatically by the database, but they are interpreted if they exist in
 *  the .script file. They have to be created manually if required. If they
 *  don't exist the default is used. This are the defaults of the database
 *  'test':
 *  <pre>
 *  readonly=false
 *  </pre>
 *
 * @version 1.7.2
 */
class Log implements Runnable {

    // block size for copying data
    private static final int       COPY_BLOCK_SIZE = 1 << 16;
    private HsqlDatabaseProperties pProperties;
    private String                 sName;
    private Database               dDatabase;
    private DatabaseScriptWriter   dbScriptWriter;
    private String                 sFileScript;
    private String                 sFileCache;
    private String                 sFileBackup;
    private String                 sFileLog;
    private boolean                bRestoring;
    private boolean                filesReadOnly;

    // metadata visibilty when not private
    int            maxLogSize;
    int            iLogCount;
    int            logType;
    private Thread tRunner;
    volatile int   writeDelay = 60;
    private Cache  cCache;

    // used for tracing
    private StopWatch defaultTimer = new StopWatch();

    /**
     *  Constructor declaration
     *
     * @param  db
     * @param  system
     * @param  name
     * @exception  SQLException  Description of the Exception
     */
    Log(Database db, String name) throws SQLException {

        dDatabase   = db;
        sName       = name;
        pProperties = db.getProperties();

        if (!db.filesReadOnly) {
            tRunner = new Thread(this,
                                 "HSQLDB " + jdbcDriver.VERSION + " logger");

            tRunner.start();
        }
    }

    /**
     *  Method declaration
     */
    public void run() {

        int count = 0;

        while (tRunner != null) {
            try {
                tRunner.sleep(1000);

                if (++count >= writeDelay && dbScriptWriter != null) {
                    synchronized (dbScriptWriter) {
                        dbScriptWriter.sync();
                    }

                    count = 0;
                }

                // todo: try to do Cache.cleanUp() here, too
            } catch (Exception e) {

                // ignore exceptions
                // may be InterruptedException or IOException
            }
        }
    }

    /**
     *  Method declaration
     *
     * @param  delay
     */
    void setWriteDelay(int delay) {

        writeDelay = delay;

        if (dbScriptWriter != null) {
            dbScriptWriter.setWriteDelay(delay);
        }
    }

    /**
     * When opening a database, the hsqldb.compatible_version property is
     * used to determine if this version of the engine is equal to or greater
     * than the earliest version of the engine capable of opening that
     * database.<p>
     *
     * @return
     * @throws  SQLException
     */
    void open() throws SQLException {

        // Allows the user to set log size in the properties file.
        maxLogSize = pProperties.getIntegerProperty("hsqldb.log_size", 0);
        maxLogSize = maxLogSize * 1024 * 1024;
        logType = pProperties.getIntegerProperty("hsqldb.log_type",
                DatabaseScriptWriter.SCRIPT_TEXT_170);
        filesReadOnly = dDatabase.filesReadOnly;;
        sFileScript   = sName + ".script";
        sFileLog      = sName + ".log";
        sFileCache    = sName + ".data";
        sFileBackup   = sName + ".backup";

        if (filesReadOnly) {
            if (cCache != null) {
                cCache.open(true);
            }

            reopenAllTextCaches();

            bRestoring = true;

            try {
                DatabaseScriptReader scr =
                    DatabaseScriptReader.newDatabaseScriptReader(dDatabase,
                        sFileScript, logType);

                scr.readAll(dDatabase.sessionManager.getSysSession());
                scr.close();
            } catch (IOException e) {

                // must throw here
            }

            bRestoring = false;

            return;
        }

        boolean needbackup = false;
        String  state      = pProperties.getProperty("modified");

        if (state.equals("yes-new-files")) {
            FileUtil.renameOverwrite(sFileScript + ".new", sFileScript);
            FileUtil.renameOverwrite(sFileBackup + ".new", sFileBackup);
            FileUtil.delete(sFileLog);
        } else if (state.equals("yes")) {
/*
            if (pProperties.isFileOpen()) {
                throw Trace.error(Trace.DATABASE_ALREADY_IN_USE);
            }
*/

            // recovering after a crash (or forgot to close correctly)
            restoreBackup();

            needbackup = true;
        }

        pProperties.setProperty("modified", "yes");
        pProperties.save();

        if (cCache != null) {
            cCache.open(false);
        }

        reopenAllTextCaches();

        bRestoring = true;

        try {
            DatabaseScriptReader scr =
                DatabaseScriptReader.newDatabaseScriptReader(dDatabase,
                    sFileScript, logType);

            scr.readAll(dDatabase.sessionManager.getSysSession());
            scr.close();
        } catch (IOException e) {

            // must throw here
        }

        ScriptRunner.runScript(dDatabase, sFileLog,
                               DatabaseScriptWriter.SCRIPT_TEXT_170);

        bRestoring = false;

        if (needbackup) {
            close(false);
            pProperties.setProperty("modified", "yes");
            pProperties.save();

            if (cCache != null) {
                cCache.open(false);
            }

            reopenAllTextCaches();
        }

        openLog();
/*
        if (newdb) {
            dbScriptWriter.writeAll();
        }

        return newdb;
*/
    }

    Cache getCache() throws SQLException {

        if (dDatabase.filesInJar) {
            return null;
        }

        if (cCache == null) {
            cCache = new Cache(sFileCache, this.dDatabase);

            cCache.open(filesReadOnly);
        }

        return cCache;
    }

    /**
     *  Method declaration
     */
    void stop() {

        if (tRunner != null) {
            tRunner.stop();
        }

        tRunner = null;
    }

    /**
     *  Method declaration
     *
     * @param  compact
     * @throws  SQLException
     */
    void close(boolean compact) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        boolean needsbackup = false;

        if (filesReadOnly) {
            return;
        }

        // no more logging
        closeLog();

        // create '.script.new' (for this the cache may be still required)
        writeScript(compact);

        // flush the cache (important: after writing the script)
        if (cCache != null) {
            needsbackup = cCache.fileModified;

            cCache.flush();
        }

        closeAllTextCaches(compact);

        // create '.backup.new' using the '.data'
        if (needsbackup &&!compact) {
            backup();
        }

        // we have the new files
        pProperties.setProperty("modified", "yes-new-files");
        pProperties.save();

        // old files can be removed and new files renamed
        FileUtil.renameOverwrite(sFileScript + ".new", sFileScript);
        FileUtil.delete(sFileLog);

        if (needsbackup &&!compact) {
            FileUtil.renameOverwrite(sFileBackup + ".new", sFileBackup);
        }

        // now its done completely
        pProperties.setProperty("modified", "no");
        pProperties.setProperty("version", jdbcDriver.VERSION);
        pProperties.setProperty("hsqldb.compatible_version", "1.7.2");
        pProperties.save();

        if (compact) {

            // stop the runner thread of this process (just for security)
            stop();

            // delete the .data so then a new file is created
            FileUtil.delete(sFileCache);
            FileUtil.delete(sFileBackup);

            // tony_lai@users 20020820
            // The database re-open and close has been moved to
            // Database#close(int closemode) for saving memory usage.
        }
    }

    /**
     *  Method declaration
     *
     * @throws  SQLException
     */
    void checkpoint(boolean defrag) throws SQLException {

        if (defrag) {
            HsqlArrayList rootsArray = cCache.defrag();

            for (int i = 0; i < rootsArray.size(); i++) {
                int[] roots = (int[]) rootsArray.get(i);

                if (roots != null) {
                    Trace.printSystemOut(
                        org.hsqldb.lib.StringUtil.getList(roots, " ", ""));
                }
            }

            DataFileDefrag.updateTableIndexRoots(dDatabase.getTables(),
                                                 rootsArray);
        }

        close(false);
        pProperties.setProperty("modified", "yes");
        pProperties.save();

        if (cCache != null) {
            cCache.open(false);
        }

        reopenAllTextCaches();
        openLog();
    }

    /**
     *  Method declaration
     *
     * @param  mb
     */
    void setLogSize(int newsize) {

        pProperties.setProperty("hsqldb.log_size", newsize);

        maxLogSize = newsize;
    }

    /**
     *  Method declaration
     *
     * @param  mb
     */
    void setLogType(int type) throws SQLException {

        boolean needsCheckpoint = false;

        if (logType != type) {
            needsCheckpoint = true;
        }

        logType = type;

        pProperties.setProperty("hsqldb.log_type", logType);

        if (needsCheckpoint) {
            checkpoint(false);
        }
    }

    /**
     *  Method declaration
     *
     * @param  c
     * @param  s
     * @throws  SQLException
     */
    void write(Session c, String s) throws SQLException {

        if (filesReadOnly || bRestoring || s == null || s.length() == 0) {
            return;
        }

        int id = (c == null) ? 0
                             : c.getId();

        try {
            dbScriptWriter.writeLogStatement(s, id);
        } catch (IOException e) {
            throw Trace.error(Trace.FILE_IO_ERROR, sFileLog);
        }

        if (maxLogSize > 0 && dbScriptWriter.size() > maxLogSize) {
            checkpoint(false);
        }
    }

    /**
     *  Method declaration
     *
     * @throws  SQLException
     */
    void shutdown() throws SQLException {

        if (tRunner != null) {
            tRunner.stop();
        }

        tRunner = null;

        if (cCache != null) {
            cCache.closeFile();

            cCache = null;
        }

        shutdownAllTextCaches();
        closeLog();
    }

    /**
     *  Saves the *.data file as compressed *.backup.
     *
     * @throws  SQLException
     */
    private void backup() throws SQLException {

        try {
            if (Trace.TRACE) {
                defaultTimer.zero();
            }

            // create a '.new' file; rename later
            FileUtil.compressFile(sFileCache, sFileBackup + ".new");

            if (Trace.TRACE) {
                Trace.trace(defaultTimer.elapsedTime());
            }
        } catch (Exception e) {
            throw Trace.error(Trace.FILE_IO_ERROR, sFileBackup);
        }
    }

    /**
     *  Method declaration
     *
     * @throws  SQLException
     */
    private void restoreBackup() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace("not closed last time!");
        }

        // the cache file must be deleted anyway
        // the backup may not exist because it was never made or is empty
        FileUtil.delete(sFileCache);

        try {
            if (Trace.TRACE) {
                defaultTimer.zero();
            }

            FileUtil.decompressFile(sFileBackup, sFileCache);

            if (Trace.TRACE) {
                Trace.trace(defaultTimer.elapsedTime());
            }
        } catch (Exception e) {
            throw Trace.error(Trace.FILE_IO_ERROR, sFileBackup);
        }
    }

    /**
     *  Method declaration
     *
     * @throws  SQLException
     */
    private void openLog() throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        try {
            dbScriptWriter =
                DatabaseScriptWriter.newDatabaseScriptWriter(this.dDatabase,
                    sFileLog, false, false,
                    DatabaseScriptWriter.SCRIPT_TEXT_170);

            dbScriptWriter.setWriteDelay(writeDelay);

            HsqlArrayList sessions =
                dDatabase.sessionManager.listVisibleSessions(
                    dDatabase.sessionManager.getSysSession());
            Enumeration en = sessions.elements();

            for (; en.hasMoreElements(); ) {
                Session session = (Session) en.nextElement();

                if (session.getAutoCommit() == false) {
                    dbScriptWriter.writeLogStatement(
                        session.getAutoCommitStatement(false),
                        session.getId());
                }
            }
        } catch (Exception e) {
            throw Trace.error(Trace.FILE_IO_ERROR, sFileScript);
        }
    }

    /**
     *  Method declaration
     *
     * @throws  SQLException
     */
    private void closeLog() throws SQLException {

        if (dbScriptWriter != null) {
            dbScriptWriter.close();

            dbScriptWriter = null;
        }
    }

    /**
     *  Method declaration
     *
     * @param  full
     * @throws  SQLException
     */
    private void writeScript(boolean full) throws SQLException {

        if (Trace.TRACE) {
            Trace.trace();
        }

        FileUtil.delete(sFileScript + ".new");

        // script; but only positions of cached tables, not full
        //fredt - to do - flag for chache set index
        DatabaseScriptWriter scw =
            DatabaseScriptWriter.newDatabaseScriptWriter(dDatabase,
                sFileScript + ".new", full, true, logType);

        scw.writeAll();
        scw.close();
    }

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP) - text tables
    private HsqlHashMap textCacheList = new HsqlHashMap();

    Cache openTextCache(HsqlName tablename, String source,
                        boolean readOnlyData,
                        boolean reversed) throws SQLException {

        closeTextCache(tablename);

        if (!pProperties.isPropertyTrue("textdb.allow_full_path")) {
            if (source.indexOf("..") != -1) {
                throw (Trace.error(Trace.ACCESS_IS_DENIED, source));
            }

            String path =
                new File(new File(sName).getAbsolutePath()).getParent();

            if (path != null) {
                source = path + File.separator + source;
            }
        }

        TextCache c;

        if (reversed) {
            c = new ReverseTextCache(source, dDatabase);
        } else {
            c = new TextCache(source, dDatabase);
        }

        c.open(readOnlyData || filesReadOnly);
        textCacheList.put(tablename, c);

        return c;
    }

    void closeTextCache(HsqlName table) throws SQLException {

        TextCache c = (TextCache) textCacheList.remove(table);

        if (c != null) {
            c.flush();
        }
    }

    void closeAllTextCaches(boolean compact) throws SQLException {

        Enumeration e = textCacheList.elements();

        while (e.hasMoreElements()) {
            if (compact) {
                ((TextCache) e.nextElement()).purge();
            } else {
                ((TextCache) e.nextElement()).flush();
            }
        }
    }

    void reopenAllTextCaches() throws SQLException {

        Enumeration e = textCacheList.elements();

        while (e.hasMoreElements()) {
            ((TextCache) e.nextElement()).reopen();
        }
    }

    void shutdownAllTextCaches() throws SQLException {

        Enumeration e = textCacheList.elements();

        while (e.hasMoreElements()) {
            ((TextCache) e.nextElement()).closeFile();
        }

        textCacheList = new HsqlHashMap();
    }
}
