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


package org.hsqldb.persist;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.hsqldb.Database;
import org.hsqldb.HsqlException;
import org.hsqldb.Table;
import org.hsqldb.Trace;
import org.hsqldb.index.RowIterator;
import org.hsqldb.lib.DoubleIntIndex;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.StopWatch;
import org.hsqldb.lib.Storage;
import org.hsqldb.rowio.RowOutputBinary;

// oj@openoffice.org - changed to file access api

/**
 *  Routine to defrag the *.data file.
 *
 *  This method iterates over the primary index of a table to find the
 *  disk position for each row and stores it, together with the new position
 *  in an array.
 *
 *  A second pass over the primary index writes each row to the new disk
 *  image after translating the old pointers to the new.
 *
 * @version    1.8.0
 * @since      1.7.2
 * @author     fredt@users
 */
final class DataFileDefrag {

    BufferedOutputStream fileStreamOut;
    long                 filePos;
    StopWatch            stopw = new StopWatch();
    String               filename;
    int[][]              rootsList;
    Database             database;
    DataFileCache        cache;
    int                  scale;

    DataFileDefrag(Database db, DataFileCache cache, String filename) {

        this.database = db;
        this.cache    = cache;
        this.scale    = cache.cacheFileScale;
        this.filename = filename;
    }

    void process() throws HsqlException, IOException {

        Trace.printSystemOut("Defrag Transfer begins");

        HsqlArrayList allTables = database.schemaManager.getAllTables();

        rootsList = new int[allTables.size()][];

        Storage dest = null;

        try {
            OutputStream fos =
                database.getFileAccess().openOutputStreamElement(filename
                    + ".new");

            fileStreamOut = new BufferedOutputStream(fos, 1 << 12);

            for (int i = 0; i < DataFileCache.INITIAL_FREE_POS; i++) {
                fileStreamOut.write(0);
            }

            filePos = DataFileCache.INITIAL_FREE_POS;

            for (int i = 0, tSize = allTables.size(); i < tSize; i++) {
                Table t = (Table) allTables.get(i);

                if (t.getTableType() == Table.CACHED_TABLE) {
                    int[] rootsArray = writeTableToDataFile(t);

                    rootsList[i] = rootsArray;
                } else {
                    rootsList[i] = null;
                }

                Trace.printSystemOut(t.getName().name + " complete");
            }

            fileStreamOut.close();

            // write out the end of file position
            dest = ScaledRAFile.newScaledRAFile(
                filename
                + ".new", false, ScaledRAFile.DATA_FILE_RAF, database
                    .getURLProperties().getProperty(
                        "storage_class_name"), database.getURLProperties()
                            .getProperty("storage_key"));

            dest.seek(DataFileCache.LONG_FREE_POS_POS);
            dest.writeLong(filePos);

            for (int i = 0, size = rootsList.length; i < size; i++) {
                int[] roots = rootsList[i];

                if (roots != null) {
                    Trace.printSystemOut(
                        org.hsqldb.lib.StringUtil.getList(roots, ",", ""));
                }
            }
        } catch (IOException e) {
            throw Trace.error(Trace.FILE_IO_ERROR, filename + ".new");
        } finally {
            if (fileStreamOut != null) {
                fileStreamOut.close();
            }

            if (dest != null) {
                dest.close();
            }
        }

        //Trace.printSystemOut("Transfer complete: ", stopw.elapsedTime());
    }

    /**
     * called from outside after the complete end of defrag
     */
    void updateTableIndexRoots() throws HsqlException {

        HsqlArrayList allTables = database.schemaManager.getAllTables();

        for (int i = 0, size = allTables.size(); i < size; i++) {
            Table t = (Table) allTables.get(i);

            if (t.getTableType() == Table.CACHED_TABLE) {
                int[] rootsArray = rootsList[i];

                t.setIndexRoots(rootsArray);
            }
        }
    }

    int[] writeTableToDataFile(Table table)
    throws IOException, HsqlException {

        RowOutputBinary rowOut = new RowOutputBinary();
        DoubleIntIndex pointerLookup =
            new DoubleIntIndex(table.getPrimaryIndex().sizeEstimate(), false);
        int[] rootsArray = table.getIndexRootsArray();
        long  pos        = filePos;
        int   count      = 0;

        pointerLookup.setKeysSearchTarget();
        Trace.printSystemOut("lookup begins: " + stopw.elapsedTime());

        RowIterator it = table.rowIterator(null);

        for (; it.hasNext(); count++) {
            CachedObject row = (CachedObject) it.next();

            pointerLookup.addUnsorted(row.getPos(), (int) (pos / scale));

            if (count % 50000 == 0) {
                Trace.printSystemOut("pointer pair for row " + count + " "
                                     + row.getPos() + " " + pos);
            }

            pos += row.getStorageSize();
        }

        Trace.printSystemOut(table.getName().name + " list done ",
                             stopw.elapsedTime());

        count = 0;
        it    = table.rowIterator(null);

        for (; it.hasNext(); count++) {
            CachedObject row = it.next();

            rowOut.reset();
            row.write(rowOut, pointerLookup);
            fileStreamOut.write(rowOut.getOutputStream().getBuffer(), 0,
                                rowOut.size());

            filePos += row.getStorageSize();

            if ((count) % 50000 == 0) {
                Trace.printSystemOut(count + " rows " + stopw.elapsedTime());
            }
        }

        for (int i = 0; i < rootsArray.length; i++) {
            if (rootsArray[i] == -1) {
                continue;
            }

            int lookupIndex =
                pointerLookup.findFirstEqualKeyIndex(rootsArray[i]);

            if (lookupIndex == -1) {
                throw Trace.error(Trace.DATA_FILE_ERROR);
            }

            rootsArray[i] = pointerLookup.getValue(lookupIndex);
        }

        Trace.printSystemOut(table.getName().name + " : table converted");

        return rootsArray;
    }
}
