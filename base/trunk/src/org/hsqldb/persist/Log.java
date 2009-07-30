/* Copyright (c) 1995-2000, The Hypersonic SQL Group.
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
 * Neither the name of the Hypersonic SQL Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE HYPERSONIC SQL GROUP,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Hypersonic SQL Group.
 *
 *
 * For work added by the HSQL Development Group:
 *
 * Copyright (c) 2001-2009, The HSQL Development Group
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


package org.hsqldb.persist;

import java.io.File;
import java.io.IOException;

import org.hsqldb.Database;
import org.hsqldb.Error;
import org.hsqldb.ErrorCode;
import org.hsqldb.HsqlException;
import org.hsqldb.NumberSequence;
import org.hsqldb.Session;
import org.hsqldb.Table;
import org.hsqldb.lib.FileAccess;
import org.hsqldb.lib.FileArchiver;
import org.hsqldb.lib.FrameworkLogger;
import org.hsqldb.lib.HashMap;
import org.hsqldb.lib.Iterator;
import org.hsqldb.scriptio.ScriptReaderBase;
import org.hsqldb.scriptio.ScriptWriterBase;

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
// file has not been updated in the current session.
// boucherb@users 20030510 - patch 1.7.2 consolidated all periodic database
// tasks in one timed task queue
// fredt@users - 20050102 patch 1.8.0 - refactoring and clearer separation of concerns
/*
    - if props.modified, use .backup file - .data file is ready
    - read .script file and set index roots


    - if .data file is modified, use .backup with .data file flag for increment backup - .data file is ready
*/

/**
 *  This class is responsible for managing the database files. An HSQLDB database
 *  consists of a .properties file, a .script file (contains an SQL script),
 *  a .data file (contains data of cached tables) a .backup file
 *  and a .log file.<p>
 *  When using TEXT tables, a data source for each table is also present.<p>
 *
 *  Notes on OpenOffice.org integration.
 *
 *  A Storage API is used when HSQLDB is integrated into OpenOffice.org. All
 *  file operations on the 4 main files are performed by OOo, which integrates
 *  the contents of these files into its database file. The script format is
 *  always TEXT in this case.
 *
 * Extensively rewritten and extended in successive versions of HSQLDB.
 *
 * @author Thomas Mueller (Hypersonic SQL Group)
 * @author Fred Toussi (fredt@users dot sourceforge.net)
 * @version 1.9.0
 * @since Hypersonic SQL
 */
public class Log {

    private HsqlDatabaseProperties properties;
    private String                 fileName;
    private Database               database;
    private FileAccess             fa;
    private ScriptWriterBase       dbLogWriter;
    private String                 scriptFileName;
    private String                 logFileName;
    private boolean                filesReadOnly;
    private long                   maxLogSize;
    private int                    writeDelay;
    private int                    scriptFormat;
    private DataFileCache          cache;
    private FrameworkLogger fwLogger;
      // We are using persist.Logger-instance-specific FrameworkLogger
      // because it is Database-instance specific.
      // If add any static level logging, should instantiate a standard,
      // context-agnostic FrameworkLogger for that purpose.

    Log(Database db) {
        fwLogger = FrameworkLogger.getLog(
                Log.class, db.getContextString());
        // Set fwLogger as first thing, so it can capture all errors.

        database   = db;
        fa         = db.logger.getFileAccess();
        fileName   = db.getPath();
        properties = db.getProperties();
    }

    void initParams() {

        // Allows the user to set log size in the properties file.
        maxLogSize     = database.logger.propLogSize * 1024L * 1024;
        scriptFormat   = 0;
        writeDelay     = database.logger.propWriteDelay;
        filesReadOnly  = database.isFilesReadOnly();
        scriptFileName = fileName + ".script";
        logFileName    = fileName + ".log";
    }

    /**
     * When opening a database, the hsqldb.compatible_version property is
     * used to determine if this version of the engine is equal to or greater
     * than the earliest version of the engine capable of opening that
     * database.<p>
     *
     * @throws  HsqlException
     */
    void open() {

        initParams();

        int state = properties.getDBModified();

        switch (state) {

            case HsqlDatabaseProperties.FILES_MODIFIED :
                deleteNewAndOldFiles();
                processScript();
                processDataFile();
                processLog();
                close(false);

                if (cache != null) {
                    cache.open(filesReadOnly);
                }

                reopenAllTextCaches();
                break;

            case HsqlDatabaseProperties.FILES_NEW :
                try {
                    deleteBackup();
                    backupData();
                    renameNewBackup();
                    renameNewScript();
                    deleteLog();
                    properties.setDBModified(
                        HsqlDatabaseProperties.FILES_NOT_MODIFIED);
                } catch (IOException e) {
                    fwLogger.severe("Failed to open Log for Catalog",  e);
                }

            // continue as non-modified files
            // fall through
            case HsqlDatabaseProperties.FILES_NOT_MODIFIED :

                /**
                 * if startup is after a SHUTDOWN SCRIPT and there are CACHED
                 * or TEXT tables, perform a checkpoint so that the .script
                 * file no longer contains CACHED or TEXT table rows.
                 */
                processScript();

                if (isAnyCacheModified()) {
                    properties.setDBModified(
                        HsqlDatabaseProperties.FILES_MODIFIED);
                    close(false);

                    if (cache != null) {
                        cache.open(filesReadOnly);
                    }

                    reopenAllTextCaches();
                }
                break;
        }

        openLog();

        if (!filesReadOnly) {
            properties.setDBModified(HsqlDatabaseProperties.FILES_MODIFIED);
        }
    }

    /**
     * Close all the database files. If script argument is true, no .data
     * or .backup file will remain and the .script file will contain all the
     * data of the cached tables as well as memory tables.
     *
     * This is not used for filesReadOnly databases which use shutdown.
     */
    void close(boolean script) {

        closeLog();
        deleteNewAndOldFiles();
        writeScript(script);
        closeAllTextCaches(script);

        if (cache != null) {
            cache.close(true);
        }

        // set this one last to save the props
        properties.setDBModified(HsqlDatabaseProperties.FILES_NEW);
        deleteLog();

        if (script) {
            deleteBackup();
            deleteData();
        } else {
            try {
                backupData();
                renameNewBackup();
            } catch (IOException e) {}
        }

        renameNewScript();
        properties.setDBModified(HsqlDatabaseProperties.FILES_NOT_MODIFIED);
    }

    /**
     * Fast counterpart to close(). Does not perform a checkpoint or a backup
     * of the .data file.
     */
    void shutdown() {

        synchLog();

        if (cache != null) {
            cache.close(false);
        }

        closeAllTextCaches(false);
        closeLog();
    }

    /**
     * Deletes the leftovers from any previous unfinished operations.
     */
    void deleteNewAndOldFiles() {

        fa.removeElement(fileName + ".data" + ".old");
        fa.removeElement(fileName + ".data" + ".new");
        fa.removeElement(fileName + ".backup" + ".new");
        fa.removeElement(scriptFileName + ".new");
    }

    void deleteBackup() {
        fa.removeElement(fileName + ".backup");
    }

    void deleteData() {
        fa.removeElement(fileName + ".data");
    }

    void backupData() throws IOException {

        if (database.logger.propIncrementBackup) {
            fa.removeElement(fileName + ".backup");

            return;
        }

        if (fa.isStreamElement(fileName + ".data")) {
            FileArchiver.archive(fileName + ".data", fileName + ".backup.new",
                                 database.logger.getFileAccess(),
                                 FileArchiver.COMPRESSION_ZIP);
        }
    }

    void renameNewBackup() {

        if (fa.isStreamElement(fileName + ".backup.new")) {
            fa.renameElement(fileName + ".backup.new", fileName + ".backup");
        }
    }

    void renameNewScript() {

        if (fa.isStreamElement(scriptFileName + ".new")) {
            fa.renameElement(scriptFileName + ".new", scriptFileName);
        }
    }

    void deleteNewScript() {
        fa.removeElement(scriptFileName + ".new");
    }

    void deleteNewBackup() {
        fa.removeElement(scriptFileName + ".backup.new");
    }

    void deleteLog() {
        fa.removeElement(logFileName);
    }

    /**
     * Checks all the caches and returns true if the modified flag is set for any
     */
    boolean isAnyCacheModified() {

        if (cache != null && cache.isFileModified()) {
            return true;
        }

        return isAnyTextCacheModified();
    }

    /**
     * Performs checkpoint including pre and post operations. Returns to the
     * same state as before the checkpoint.
     */
    void checkpoint(boolean defrag) {

        if (filesReadOnly) {
            return;
        }

        fwLogger.info("Checkpoint start");
        deleteNewAndOldFiles();
        writeScript(false);

        if (cache != null) {
            if (forceDefrag()) {
                defrag = true;
            }

            if (defrag) {
                try {
                    cache.defrag();
                } catch (Exception e) {}
            } else {
                cache.close(true);

                try {
                    cache.backupFile();
                } catch (Exception e1) {
                    deleteNewBackup();
                    cache.open(false);

                    return;
                }

                cache.open(false);
            }
        }

        properties.setDBModified(HsqlDatabaseProperties.FILES_NEW);
        closeLog();
        deleteLog();
        renameNewScript();
        renameNewBackup();
        properties.setDBModified(HsqlDatabaseProperties.FILES_MODIFIED);

        if (dbLogWriter == null) {
            return;
        }

        openLog();

        Session[] sessions = database.sessionManager.getAllSessions();

/*
        try {
            for (int i = 0; i < sessions.length; i++) {
                Session session = sessions[i];

                if (session.isAutoCommit() == false) {
                    dbLogWriter.writeLogStatement(session, session.getAutoCommitStatement());
                }
            }
        } catch (IOException e) {
            throw Error.error(ErrorCode.FILE_IO_ERROR, logFileName);
        }
*/
        fwLogger.info("Checkpoint end");
    }

    /**
     * Performs checkpoint including pre and post operations. Returns to the
     * same state as before the checkpoint.
     */
    boolean closeForBackup() {

        if (filesReadOnly) {
            return false;
        }

        deleteNewAndOldFiles();

        try {
            writeScript(false);
        } catch (HsqlException e) {
            deleteNewScript();

            return false;
        }

        if (cache != null) {
            try {
                cache.close(true);
                cache.backupFile();
            } catch (Exception e) {

                // backup failed perhaps due to lack of disk space
                deleteNewScript();
                deleteNewBackup();

                try {
                    if (!cache.isFileOpen()) {
                        cache.open(false);
                    }
                } catch (Exception e1) {}

                return false;
            }
        }

        try {
            properties.setDBModified(HsqlDatabaseProperties.FILES_NEW);
            closeLog();
        } catch (Exception e) {}

        deleteLog();
        renameNewScript();
        renameNewBackup();

        try {
            properties.setDBModified(
                HsqlDatabaseProperties.FILES_NOT_MODIFIED);
        } catch (Exception e) {}

        return true;
    }

    boolean openAfterBackup() {

        if (filesReadOnly) {
            return false;
        }

        try {
            if (cache != null) {
                cache.open(false);
            }

            if (dbLogWriter != null) {
                openLog();
            }

            properties.setDBModified(HsqlDatabaseProperties.FILES_MODIFIED);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    /**
     * Returns true if lost space is above the threshold percentage
     */
    boolean forceDefrag() {

        long limit = database.logger.propCacheDefragLimit
                     * cache.getFileFreePos() / 100;
        long lostSize = cache.freeBlocks.getLostBlocksSize();

        return limit > 0 && lostSize > limit;
    }

    /**
     *
     */
    boolean hasCache() {
        return cache != null;
    }

    /**
     * Responsible for creating the cache instance.
     */
    DataFileCache getCache() {

        if (cache == null) {
            cache = new DataFileCache(database, fileName);

            cache.open(filesReadOnly);
        }

        return cache;
    }

    void setLogSize(int megas) {
        maxLogSize = megas * 1024L * 1024;
    }

    int getScriptType() {
        return scriptFormat;
    }

    /**
     * Write delay specifies the frequency of FileDescriptor.sync() calls.
     */
    int getWriteDelay() {
        return writeDelay;
    }

    void setWriteDelay(int delay) {

        writeDelay = delay;

        if (dbLogWriter != null) {
            synchLog();
            dbLogWriter.setWriteDelay(delay);
        }
    }

    public void setIncrementBackup(boolean val) {

        if (cache != null) {
            cache.setIncrementBackup(val);

            cache.fileModified = true;
        }
    }

    /**
     * Various writeXXX() methods are used for logging statements.
     */
    void writeStatement(Session session, String s) {

        try {
            dbLogWriter.writeLogStatement(session, s);
        } catch (IOException e) {
            throw Error.error(ErrorCode.FILE_IO_ERROR, logFileName);
        }

        if (maxLogSize > 0 && dbLogWriter.size() > maxLogSize) {
            database.logger.checkpointRequired = true;
        }
    }

    void writeInsertStatement(Session session, Table t, Object[] row) {

        try {
            dbLogWriter.writeInsertStatement(session, t, row);
        } catch (IOException e) {
            throw Error.error(ErrorCode.FILE_IO_ERROR, logFileName);
        }

        if (maxLogSize > 0 && dbLogWriter.size() > maxLogSize) {
            database.logger.checkpointRequired = true;
        }
    }

    void writeDeleteStatement(Session session, Table t, Object[] row) {

        try {
            dbLogWriter.writeDeleteStatement(session, t, row);
        } catch (IOException e) {
            throw Error.error(ErrorCode.FILE_IO_ERROR, logFileName);
        }

        if (maxLogSize > 0 && dbLogWriter.size() > maxLogSize) {
            database.logger.checkpointRequired = true;
        }
    }

    void writeSequenceStatement(Session session, NumberSequence s) {

        try {
            dbLogWriter.writeSequenceStatement(session, s);
        } catch (IOException e) {
            throw Error.error(ErrorCode.FILE_IO_ERROR, logFileName);
        }

        if (maxLogSize > 0 && dbLogWriter.size() > maxLogSize) {
            database.logger.checkpointRequired = true;
        }
    }

    void writeCommitStatement(Session session) {

        try {
            dbLogWriter.writeCommitStatement(session);
        } catch (IOException e) {
            throw Error.error(ErrorCode.FILE_IO_ERROR, logFileName);
        }

        if (maxLogSize > 0 && dbLogWriter.size() > maxLogSize) {
            database.logger.checkpointRequired = true;
        }
    }

    void synchLog() {

        if (dbLogWriter != null) {
            dbLogWriter.sync();
        }
    }

    /**
     * Wrappers for openning-starting / stoping-closing the log file and
     * writer.
     */
    private void openLog() {

        if (filesReadOnly) {
            return;
        }

        try {
            dbLogWriter = ScriptWriterBase.newScriptWriter(database,
                    logFileName, false, false,
                    ScriptWriterBase.SCRIPT_TEXT_170);

            dbLogWriter.setWriteDelay(writeDelay);
            dbLogWriter.start();
        } catch (Exception e) {
            throw Error.error(ErrorCode.FILE_IO_ERROR, logFileName);
        }
    }

    private synchronized void closeLog() {

        if (dbLogWriter != null) {
            dbLogWriter.close();
        }
    }

    /**
     * Write the .script file as .script.new.
     */
    private void writeScript(boolean full) {

        deleteNewScript();

        //fredt - to do - flag for chache set index
        ScriptWriterBase scw = ScriptWriterBase.newScriptWriter(database,
            scriptFileName + ".new", full, true, scriptFormat);

        scw.writeAll();
        scw.close();
    }

    /**
     * Performs all the commands in the .script file.
     */
    private void processScript() {

        ScriptReaderBase scr = null;

        try {
            if (database.isFilesInJar()
                    || fa.isStreamElement(scriptFileName)) {
                scr = ScriptReaderBase.newScriptReader(database,
                                                       scriptFileName,
                                                       scriptFormat);

                Session session =
                    database.sessionManager.getSysSessionForScript(database);

                scr.readAll(session);
                scr.close();
            }
        } catch (Throwable e) {
            if (scr != null) {
                scr.close();

                if (cache != null) {
                    cache.close(false);
                }

                closeAllTextCaches(false);
            }

            fwLogger.warning("Script processing failure", e);

            if (e instanceof HsqlException) {
                throw (HsqlException) e;
            } else if (e instanceof IOException) {
                throw Error.error(ErrorCode.FILE_IO_ERROR, e.toString());
            } else if (e instanceof OutOfMemoryError) {
                throw Error.error(ErrorCode.OUT_OF_MEMORY);
            } else {
                throw Error.error(ErrorCode.GENERAL_ERROR, e.toString());
            }
        }
    }

    /**
     * Defrag large data files when the sum of .log and .data files is large.
     */
    private void processDataFile() {

        // OOo related code
        if (database.logger.isStoredFileAccess()) {
            return;
        }

        // OOo end
        if (cache == null || filesReadOnly
                || !fa.isStreamElement(logFileName)) {
            return;
        }

        File file       = new File(logFileName);
        long logLength  = file.length();
        long dataLength = cache.getFileFreePos();

        if (logLength + dataLength > cache.maxDataFileSize) {
            database.logger.checkpointRequired = true;
        }
    }

    /**
     * Performs all the commands in the .log file.
     */
    private void processLog() {

        if (!database.isFilesInJar() && fa.isStreamElement(logFileName)) {
            ScriptRunner.runScript(database, logFileName,
                                   ScriptWriterBase.SCRIPT_TEXT_170);
        }
    }

    /**
     * Restores a compressed backup or the .data file.
     */
    private void restoreBackup() {

        if (database.logger.propIncrementBackup) {
            restoreBackupIncremental();

            return;
        }

        // in case data file cannot be deleted, reset it
        DataFileCache.deleteOrResetFreePos(database, fileName + ".data");

        try {
            FileArchiver.unarchive(fileName + ".backup", fileName + ".data",
                                   database.logger.getFileAccess(),
                                   FileArchiver.COMPRESSION_ZIP);
        } catch (Exception e) {
            throw Error.error(ErrorCode.FILE_IO_ERROR,
                              ErrorCode.M_Message_Pair, new Object[] {
                fileName + ".backup", e.toString()
            });
        }
    }

    /**
     * Restores in from an incremental backup
     */
    private void restoreBackupIncremental() {

        try {
            if (fa.isStreamElement(fileName + ".backup")) {
                RAShadowFile.restoreFile(fileName + ".backup",
                                         fileName + ".data");
                deleteBackup();
            }
        } catch (IOException e) {
            throw Error.error(ErrorCode.FILE_IO_ERROR, fileName + ".backup");
        }
    }

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP) - text tables
    private HashMap textCacheList = new HashMap();

    DataFileCache openTextCache(Table table, String source,
                                boolean readOnlyData, boolean reversed) {

        closeTextCache(table);

        if (!database.logger.propTextAllowFullPath) {
            if (source.indexOf("..") != -1) {
                throw (Error.error(ErrorCode.ACCESS_IS_DENIED, source));
            }

            String path = new File(
                new File(
                    database.getPath()
                    + ".properties").getAbsolutePath()).getParent();

            if (path != null) {
                source = path + File.separator + source;
            }
        }

        TextCache c = new TextCache(table, source);

        c.open(readOnlyData || filesReadOnly);
        textCacheList.put(table.getName(), c);

        return c;
    }

    void closeTextCache(Table table) {

        TextCache c = (TextCache) textCacheList.remove(table.getName());

        if (c != null) {
            try {
                c.close(true);
            } catch (HsqlException e) {}
        }
    }

    private void closeAllTextCaches(boolean compact) {

        Iterator it = textCacheList.values().iterator();

        while (it.hasNext()) {
            if (compact) {
                ((TextCache) it.next()).purge();
            } else {
                ((TextCache) it.next()).close(true);
            }
        }
    }

    private void reopenAllTextCaches() {

        Iterator it = textCacheList.values().iterator();

        while (it.hasNext()) {
            ((TextCache) it.next()).reopen();
        }
    }

    private boolean isAnyTextCacheModified() {

        Iterator it = textCacheList.values().iterator();

        while (it.hasNext()) {
            if (((TextCache) it.next()).isFileModified()) {
                return true;
            }
        }

        return false;
    }
}
