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

import java.io.File;
import java.io.IOException;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.FileUtil;

public class DataFileCache extends Cache {

    private static final int MAX_FREE_COUNT = 1024;

    DataFileCache(String name, Database db) throws HsqlException {
        super(name, db);
    }

    /**
     * Opens the *.data file for this cache, setting the variables that
     * allow accesse to the particular database version of the *.data file.
     */
    void open(boolean readonly) throws HsqlException {

        try {
            boolean exists = false;
            File    f      = new File(sName);

            if (f.exists() && f.length() > FREE_POS_POS) {
                exists = true;
            }

            rFile = ScaledRAFile.newScaledRAFile(sName, readonly, 1,
                                                 ScaledRAFile.DATA_FILE_NIO);

            if (exists) {
                rFile.seek(FREE_POS_POS);

                iFreePos = rFile.readInt();
            } else {

// erik - iFreePos = INITIAL_FREE_POS / cacheFileScale;
                iFreePos = INITIAL_FREE_POS;

                dbProps.setProperty("hsqldb.cache_version", "1.7.0");
            }

            String cacheVersion = dbProps.getProperty("hsqldb.cache_version",
                "1.6.0");

            if (cacheVersion.equals("1.7.0")) {
                cachedRowType = DatabaseRowOutput.CACHED_ROW_170;
            }

            initBuffers();
        } catch (Exception e) {
            Trace.throwerror(Trace.FILE_IO_ERROR,
                             "error " + e + " opening file " + sName);
        }
    }

/**
 *  Writes out all cached rows that have been modified and the free
 *  position pointer for the *.data file and then closes the file.
 */
    void flush() throws HsqlException {

        if (rFile == null || rFile.readOnly) {
            return;
        }

        try {
            rFile.seek(FREE_POS_POS);
            rFile.writeInt(iFreePos);
            saveAll();
            rFile.close();

            rFile = null;

            boolean empty = new File(sName).length() < INITIAL_FREE_POS;

            if (empty) {
                new File(sName).delete();
            }
        } catch (Exception e) {
            Trace.throwerror(Trace.FILE_IO_ERROR,
                             "error " + e + " closing file " + sName);
        }
    }

/**
 *  Writes out all the rows to a new file without fragmentation and
 *  returns an ArrayList containing new positions for index roots.
 */
    HsqlArrayList defrag() throws HsqlException {

        HsqlArrayList indexRoots = null;

        try {
            flush();

            if (!FileUtil.exists(sName)) {
                return null;
            }

            open(true);

            DataFileDefrag dfd = new DataFileDefrag();

            indexRoots = dfd.defrag(dDatabase, rFile, sName);

            closeFile();
            Trace.printSystemOut("closed source");
            new File(sName).delete();
            new File(sName + ".new").renameTo(new File(sName));
            init();
            open(cacheReadonly);
            Trace.printSystemOut("opened new file");
        } catch (Exception e) {
            e.printStackTrace();
            Trace.throwerror(Trace.FILE_IO_ERROR,
                             "error " + e + " defrag file " + sName);
        }

        return indexRoots;
    }

/**
 *  Closes this object's database file without flushing pending writes.
 */
    void closeFile() throws HsqlException {

        System.out.println("DataFileCache.closeFile()");

        if (rFile == null) {
            return;
        }

        try {
            rFile.close();

            rFile = null;
        } catch (Exception e) {
            Trace.throwerror(Trace.FILE_IO_ERROR,
                             "error " + e + " in shutdown file " + sName);
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
    void free(CachedRow r) throws HsqlException {

        fileModified = true;

        iFreeCount++;

        CacheFree n = new CacheFree();

        n.iPos    = r.iPos;
        n.iLength = r.storageSize;

        if (iFreeCount > MAX_FREE_COUNT) {
            iFreeCount = 0;
        } else {
            n.fNext = fRoot;
        }

        fRoot = n;

        // it's possible to remove roots too
        remove(r);
    }

/**
 * Allocates file space for the row. <p>
 *
 * A Row is added by walking the list of CacheFree objects to see if
 * there is available space to store it, reusing space if it exists.
 * Otherwise the file is grown to accommodate it.
 */
    int setFilePos(CachedRow r) throws HsqlException {

        int       rowSize = r.storageSize;
        int       size    = rowSize;
        CacheFree f       = fRoot;
        CacheFree last    = null;
        int       i       = iFreePos;

        while (f != null) {
            if (Trace.TRACE) {
                Trace.stop();
            }

            // first that is long enough
            if (f.iLength >= size) {
                i    = f.iPos;
                size = f.iLength - size;

                if (size < 32) {

                    // remove almost empty blocks
                    if (last == null) {
                        fRoot = f.fNext;
                    } else {
                        last.fNext = f.fNext;
                    }

                    iFreeCount--;
                } else {
                    f.iLength = size;

// erik  f.iPos += rowSize / cacheFileScale
                    f.iPos += rowSize;
                }

                break;
            }

            last = f;
            f    = f.fNext;
        }

        if (i == iFreePos) {

// erik  iFreePs += size / cacheFileScale
            iFreePos += size;
        }

        r.setPos(i);

        return i;
    }

/**
 * Constructs a new Row for the specified table, using row data read
 * at the specified position (pos) in this object's database file.
 */
    protected CachedRow makeRow(int pos, Table t) throws HsqlException {

        CachedRow r = null;

        makeRowCount++;

        try {

// erik -  rFile.readSeek(pos*cacheFileScale);
            rFile.seek(pos);

            int size = rFile.readInt();

            rowIn.resetRow(pos, size);
            rFile.read(rowIn.getBuffer(), 4, size - 4);

            r = new CachedRow(t, rowIn);
        } catch (IOException e) {
            e.printStackTrace();
            Trace.throwerror(Trace.FILE_IO_ERROR, "reading file : " + e);
        }

        return r;
    }

/**
 * Writes out the specified Row. Will write only the Nodes or both Nodes
 * and table row data depending on what is not already persisted to disk.
 */
    protected void saveRow(CachedRow r) throws IOException, HsqlException {

        rowOut.reset();

// erik - multiply position by cacheFileScale   rFile.seek(r.iPos * cacheFileScale);
        rFile.seek(r.iPos);
        r.write(rowOut);
        rFile.write(rowOut.getOutputStream().getBuffer(), 0,
                    rowOut.getOutputStream().size());
    }
}
