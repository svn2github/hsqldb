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
 * Copyright (c) 2001-2005, The HSQL Development Group
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
import org.hsqldb.HsqlException;
import org.hsqldb.Trace;
import org.hsqldb.lib.FileAccess;
import org.hsqldb.lib.FileUtil;
import org.hsqldb.lib.StopWatch;
import org.hsqldb.lib.Storage;
import org.hsqldb.lib.ZipUnzipFile;
import org.hsqldb.rowio.RowInputBinary;
import org.hsqldb.rowio.RowInputInterface;
import org.hsqldb.rowio.RowOutputBinary;
import org.hsqldb.rowio.RowOutputInterface;
import org.hsqldb.store.BitMap;

/**
 * Acts as a manager for CACHED table persistence.<p>
 *
 * This contains the top level functionality. Provides file management services
 * and access.<p>
 *
 * Rewritten for 1.8.0 together with Cache.
 *
 * @author fredt@user
 * @version 1.8.0
 * @since 1.7.2
 */
public class DataFileCache {

    private FileAccess fa;

    // flags
    public static final int FLAG_ISSAVED = 2;
    public static final int FLAG_ROWINFO = 3;

    // file format fields
    static final int LONG_EMPTY_SIZE   = 4;     // empty space size
    static final int LONG_FREE_POS_POS = 12;    // where iFreePos is saved
    static final int FLAGS_POS         = 28;
    static final int INITIAL_FREE_POS  = 32;

    //
    DataFileBlockManager     freeBlocks;
    private static final int FREE_BLOCKS_COUNT = 512;

    //
    protected String   fileName;
    protected String   backupFileName;
    protected Database database;

    // this flag is used externally to determine if a backup is required
    protected boolean fileModified;
    protected int     cacheFileScale;

    // post openning constant fields
    protected boolean cacheReadonly;

    // cache operation mode
    protected boolean storeOnInsert;

    //
    protected int     cachedRowPadding = 8;
    protected boolean hasRowInfo       = false;

    // reusable input / output streams
    protected RowInputInterface  rowIn;
    protected RowOutputInterface rowOut;

    //
    public long maxDataFileSize;

    //
    protected Storage dataFile;
    protected long    fileFreePosition;
    protected int     maxCacheSize;             // number of Rows
    protected long    maxCacheBytes;            // number of bytes
    protected Cache   cache;

    public DataFileCache(Database db, String fileName,
                         String backupName) throws HsqlException {

        this.fileName       = fileName;
        this.backupFileName = backupName;
        database            = db;
        fa                  = database.getFileAccess();

        initParams();

        cache = new Cache(this);
    }

    /**
     * initial external parameters are set here.
     */
    protected void initParams() throws HsqlException {

        int cacheScale =
            database.getProperties().getIntegerProperty("hsqldb.cache_scale",
                14, 8, 18);
        int cacheSizeScale = database.getProperties().getIntegerProperty(
            "hsqldb.cache_size_scale", 10, 6, 20);

        cacheFileScale = database.getProperties().getIntegerProperty(
            HsqlDatabaseProperties.CACHE_FILE_SCALE, 1);

        Trace.printSystemOut("cache_scale: " + cacheScale);
        Trace.printSystemOut("cache_size_scale: " + cacheSizeScale);

        cacheReadonly = database.isFilesReadOnly();

        int lookupTableLength = 1 << cacheScale;
        int avgRowBytes       = 1 << cacheSizeScale;

        maxCacheSize    = lookupTableLength * 3;
        maxCacheBytes   = maxCacheSize * avgRowBytes;
        maxDataFileSize = cacheFileScale == 1 ? Integer.MAX_VALUE
                                              : (long) Integer.MAX_VALUE * 8;
        dataFile        = null;
    }

    /**
     * Opens the *.data file for this cache, setting the variables that
     * allow access to the particular database version of the *.data file.
     */
    public void open(boolean readonly) throws HsqlException {

        fileFreePosition = 0;

        try {
            boolean preexists = false;
            long    freesize  = 0;

            if (fa.isStreamElement(fileName)) {
                if (database.isStoredFileAccess()) {
                    preexists = true;
                } else {

                    // discard "empty" databases
                    File f = new File(fileName);

                    preexists = f.length() > INITIAL_FREE_POS;
                }
            }

            if (preexists) {
                String version = database.getProperties().getProperty(
                    HsqlDatabaseProperties.CACHE_VERSION);
                boolean v17 =
                    HsqlDatabaseProperties.VERSION_STRING_1_7_0.equals(
                        version);

                // for later versions
                boolean v18 =
                    HsqlDatabaseProperties.VERSION_STRING_1_8_0.equals(
                        version);

                if (!v17) {
                    throw Trace.error(Trace.WRONG_DATABASE_FILE_VERSION);
                }
            }

            boolean isNio = database.getProperties().isPropertyTrue(
                "hsqldb.nio_data_file");
            int fileType = isNio ? ScaledRAFile.DATA_FILE_NIO
                                 : ScaledRAFile.DATA_FILE_RAF;

            // oj@openofice.org - change to file access api
            String cname =
                database.getURLProperties().getProperty("storage_class_name");
            String skey =
                database.getURLProperties().getProperty("storage_key");

            dataFile = ScaledRAFile.newScaledRAFile(fileName, readonly,
                    fileType, cname, skey);

            if (preexists) {
                dataFile.seek(FLAGS_POS);

                int flags = dataFile.readInt();

                hasRowInfo = BitMap.isSet(flags, FLAG_ROWINFO);

                dataFile.seek(LONG_EMPTY_SIZE);

                freesize = dataFile.readLong();

                dataFile.seek(LONG_FREE_POS_POS);

                fileFreePosition = dataFile.readLong();

                if (fileFreePosition < INITIAL_FREE_POS) {
                    fileFreePosition = INITIAL_FREE_POS;
                }
            } else {
                fileFreePosition = INITIAL_FREE_POS;
            }

            resetBuffers();

            fileModified = false;
            freeBlocks = new DataFileBlockManager(FREE_BLOCKS_COUNT,
                                                  cacheFileScale, freesize);
        } catch (Exception e) {
            database.logger.appLog.logContext(e);
            close(false);

            throw Trace.error(Trace.FILE_IO_ERROR, Trace.DataFileCache_open,
                              new Object[] {
                e, fileName
            });
        }
    }

    /**
     *  Parameter write indicates either an orderly close, or a fast close
     *  without backup.
     *
     *  When false, just closes the file.
     *
     *  When true, writes out all cached rows that have been modified and the free
     *  position pointer for the *.data file and then closes the file.
     */
    public void close(boolean write) throws HsqlException {

        try {
            if (cacheReadonly) {
                dataFile.close();

                return;
            }

            StopWatch sw = new StopWatch();

            if (write) {
                cache.saveAll();
                Trace.printSystemOut("saveAll: " + sw.elapsedTime());

                // set empty
                dataFile.seek(LONG_EMPTY_SIZE);
                dataFile.writeLong(freeBlocks.getLostBlocksSize());

                // set end
                dataFile.seek(LONG_FREE_POS_POS);
                dataFile.writeLong(fileFreePosition);

                // set saved flag;
                dataFile.seek(FLAGS_POS);

                int flag = BitMap.set(0, FLAG_ISSAVED);

                if (hasRowInfo) {
                    flag = BitMap.set(flag, FLAG_ROWINFO);
                }

                dataFile.writeInt(flag);

                //
                dataFile.seek(fileFreePosition);
                Trace.printSystemOut("pos and flags: " + sw.elapsedTime());
            }

            if (dataFile != null) {
                dataFile.close();

                dataFile = null;

                Trace.printSystemOut("close: " + sw.elapsedTime());
            }

            boolean empty = fileFreePosition == INITIAL_FREE_POS;

            if (empty) {
                fa.removeElement(fileName);
                fa.removeElement(backupFileName);
            }
        } catch (Exception e) {
            database.logger.appLog.logContext(e);

            throw Trace.error(Trace.FILE_IO_ERROR, Trace.DataFileCache_close,
                              new Object[] {
                e, fileName
            });
        }
    }

    void postClose(boolean keep) throws HsqlException {

        if (cacheReadonly) {
            return;
        }

        try {
            if (keep) {
                database.getProperties().setProperty(
                    "hsqldb.cache_version",
                    HsqlDatabaseProperties.VERSION_STRING_1_7_0);
                database.getProperties().save();

                if (fileModified) {
                    backup();
                }
            } else {
                fa.removeElement(backupFileName);
                deleteOrResetFreePos(database, fileName);
            }
        } catch (IOException e) {
            throw new HsqlException(
                e, Trace.getMessage(Trace.GENERAL_IO_ERROR),
                Trace.GENERAL_IO_ERROR);
        }
    }

    protected void resetBuffers() {
        rowOut = new RowOutputBinary();
        rowIn  = new RowInputBinary();
    }

    /**
     *  Writes out all the rows to a new file without fragmentation.
     */
    public void defrag() throws HsqlException {

        if (cacheReadonly) {
            return;
        }

        if (fileFreePosition == INITIAL_FREE_POS) {
            return;
        }

        try {
            boolean        wasNio = dataFile.wasNio();
            DataFileDefrag dfd = new DataFileDefrag(database, this, fileName);

            dfd.process();
            close(false);
            Trace.printSystemOut("closed old cache");

            // first attemp to delete
            fa.removeElement(fileName);

            if (wasNio) {
                System.gc();

                if (fa.isStreamElement(fileName)) {
                    fa.removeElement(fileName);

                    if (fa.isStreamElement(fileName)) {
                        fa.renameElement(fileName, fileName + ".old");

                        File oldfile = new File(fileName + ".old");

                        FileUtil.deleteOnExit(oldfile);
                    }
                }
            }

// oj@openofice.org - change to file access api
            fa.renameElement(fileName + ".new", fileName);
            backup();
            database.getProperties().setProperty(
                "hsqldb.cache_version",
                HsqlDatabaseProperties.VERSION_STRING_1_7_0);
            database.getProperties().save();
            initParams();

            cache = new Cache(this);

            open(cacheReadonly);
            dfd.updateTableIndexRoots();
            Trace.printSystemOut("opened cache");
        } catch (Exception e) {
            database.logger.appLog.logContext(e);

            throw new HsqlException(
                e, Trace.getMessage(Trace.GENERAL_IO_ERROR),
                Trace.GENERAL_IO_ERROR);
            /*
            Trace.error(Trace.FILE_IO_ERROR, Trace.DataFileCache_defrag, new Object[] {
                e, fileName
            });
            */
        }
    }

    /**
     * Used when a row is deleted as a result of some DML or DDL command.
     * Adds the file space for the row to the list of free positions.
     * If there exists more than MAX_FREE_COUNT free positions,
     * then they are probably all too small, so we start a new list. <p>
     * todo: This is wrong when deleting lots of records <p>
     * Then remove the row from the cache data structures.
     */
    public void remove(int i, PersistentStore store) throws HsqlException {

        CachedObject r    = release(i);
        int          size = r == null ? getStorageSize(i)
                                      : r.getStorageSize();

        freeBlocks.add(i, size);
    }

    /**
     * Allocates file space for the row. <p>
     *
     * A Row is added by walking the list of CacheFree objects to see if
     * there is available space to store it, reusing space if it exists.
     * Otherwise the file is grown to accommodate it.
     */
    private int setFilePos(CachedObject r) throws IOException {

        int rowSize = r.getStorageSize();
        int i       = freeBlocks == null ? -1
                                         : freeBlocks.get(rowSize);

        if (i == -1) {
            i = (int) (fileFreePosition / cacheFileScale);

            long newFreePosition = fileFreePosition + rowSize;

            if (newFreePosition > maxDataFileSize) {
                throw new IOException(
                    Trace.getMessage(Trace.DATA_FILE_IS_FULL));
            }

            fileFreePosition = newFreePosition;
        }

        r.setPos(i);

        return i;
    }

    public void add(CachedObject object) throws IOException {

        int size = object.getRealSize(rowOut);

        size = ((size + cachedRowPadding - 1) / cachedRowPadding)
               * cachedRowPadding;

        object.setStorageSize(size);

        int i = setFilePos(object);

        cache.put(i, object);

        // for text tables
        if (storeOnInsert) {
            saveRow(object);
        }
    }

    public synchronized int getStorageSize(int i) throws HsqlException {

        try {
            CachedObject value = cache.get(i);

            if (value != null) {
                return value.getStorageSize();
            }

            return readSize(i);
        } catch (IOException e) {
            database.logger.appLog.logContext(e);

            throw Trace.error(Trace.DATA_FILE_ERROR,
                              Trace.DataFileCache_makeRow, new Object[] {
                e, fileName
            });
        }
    }

    public synchronized CachedObject get(int i, PersistentStore store,
                                         boolean keep) throws HsqlException {

        if (i < 0) {
            return null;
        }

        try {
            CachedObject value = cache.get(i);

            if (value == null) {
                boolean result = readObject(i);

                if (!result) {
                    return null;
                }

                value = store.get(rowIn);

                value.setPos(i);
                cache.put(i, value);
            }

            if (keep) {
                value.keepInMemory(true);
            }

            return value;
        } catch (IOException e) {
            database.logger.appLog.logContext("" + cache + " pos: " + i);
            database.logger.appLog.logContext(e);

            throw Trace.error(Trace.DATA_FILE_ERROR,
                              Trace.DataFileCache_makeRow, new Object[] {
                e, fileName
            });
        }
    }

    RowInputInterface getRaw(int i) throws IOException {

        boolean result = readObject(i);

        return result ? rowIn
                      : null;
    }

    protected int readSize(int pos) throws IOException {

        dataFile.seek((long) pos * cacheFileScale);

        return dataFile.readInt();
    }

    protected boolean readObject(int pos) throws IOException {

        dataFile.seek((long) pos * cacheFileScale);

        int size = dataFile.readInt();

        rowIn.resetRow(pos, size);
        dataFile.read(rowIn.getBuffer(), 4, size - 4);

        return true;
    }

    public CachedObject release(int i) {
        return cache.release(i);
    }

    protected void saveRows(CachedObject[] rows, int offset,
                            int count) throws IOException {

        for (int i = offset; i < offset + count; i++) {
            CachedObject r = rows[i];

            saveRow(r);

            rows[i] = null;
        }
    }

    /**
     * Writes out the specified Row. Will write only the Nodes or both Nodes
     * and table row data depending on what is not already persisted to disk.
     */
    private void saveRow(CachedObject row) throws IOException {

        setFileModified();
        rowOut.reset();
        row.write(rowOut);
        dataFile.seek((long) row.getPos() * cacheFileScale);
        dataFile.write(rowOut.getOutputStream().getBuffer(), 0,
                       rowOut.getOutputStream().size());
    }

    /**
     *  Saves the *.data file as compressed *.backup.
     *
     * @throws  HsqlException
     */
    public void backup() throws IOException {

        try {
            ZipUnzipFile.compressFile(fileName, backupFileName + ".new",
                                      database.getFileAccess());
            fa.renameElement(backupFileName + ".new", backupFileName);
        } catch (IOException e) {
            database.logger.appLog.logContext(e);

            throw e;
        }
    }

    /**
     * This method deletes a data file or resets its free position.
     * this is used only for nio files - not OOo files
     */
    public static void deleteOrResetFreePos(Database database,
            String filename) {

        ScaledRAFile raFile = null;

        try {
            database.getFileAccess().removeElement(filename);
        } catch (IOException e) {
            database.logger.appLog.logContext(e);
        }

        if (database.isStoredFileAccess()) {
            return;
        }

        if (!database.getFileAccess().isStreamElement(filename)) {
            return;
        }

        try {
            raFile = new ScaledRAFile(filename, false);

            raFile.seek(LONG_FREE_POS_POS);
            raFile.writeLong(INITIAL_FREE_POS);
        } catch (IOException e) {
            database.logger.appLog.logContext(e);
        } finally {
            if (raFile != null) {
                try {
                    raFile.close();
                } catch (IOException e) {
                    database.logger.appLog.logContext(e);
                }
            }
        }
    }

    public static int getFlags(String filename) throws HsqlException {

        try {
            ScaledRAFile raFile =
                (ScaledRAFile) ScaledRAFile.newScaledRAFile(filename, true,
                    ScaledRAFile.DATA_FILE_RAF, null, null);

            raFile.seek(FLAGS_POS);

            int flags = raFile.readInt();

            raFile.close();

            return flags;
        } catch (IOException e) {
            throw Trace.error(Trace.DATA_FILE_ERROR);
        }
    }

    public int capacity() {
        return maxCacheSize;
    }

    public long bytesCapacity() {
        return maxCacheBytes;
    }

    public long getTotalCachedBlockSize() {
        return cache.getTotalCachedBlockSize();
    }

    public int getFreeBlockCount() {
        return freeBlocks.size();
    }

    public int getTotalFreeBlockSize() {
        return 0;
    }

    public long getFileFreePos() {
        return fileFreePosition;
    }

    public int getCachedObjectCount() {
        return cache.size();
    }

    public String getFileName() {
        return fileName;
    }

    public boolean hasRowInfo() {
        return hasRowInfo;
    }

    public boolean isFileModified() {
        return fileModified;
    }

    protected void setFileModified() throws IOException {

        if (!fileModified) {

            // unset saved flag;
            dataFile.seek(FLAGS_POS);

            int flag = BitMap.set(0, FLAG_ISSAVED);

            if (hasRowInfo) {
                flag = BitMap.set(flag, FLAG_ROWINFO);
            }

            dataFile.writeInt(flag);

            fileModified = true;
        }
    }
}
