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

import org.hsqldb.lib.UnifiedTable;
import java.sql.SQLException;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.StopWatch;

/**
 *  Experimental routine to defrag the *.data file.
 *  This method iterates through the primary index of a table to find the
 *  disk position for each row and stores it, together with the new position
 *  in an array.
 *  A second pass over the primary index writes each row to the new disk
 *  image after translating the old pointers to the
 *  new.
 *
 * @version    1.7.2
 * @author     frest@users
 */
class DataFileDefrag {

    StopWatch stopw = new StopWatch();

    HsqlArrayList defrag(Database db, DatabaseFile sourcenotused,
                         String filename) throws IOException, SQLException {

        System.out.println("Transfer begins");

        HsqlArrayList    rootsList = new HsqlArrayList();
        HsqlArrayList    tTable    = db.getTables();
        RandomAccessFile dest = new RandomAccessFile(filename + ".new", "rw");

        dest.seek(Cache.INITIAL_FREE_POS);

        for (int i = 0, tSize = tTable.size(); i < tSize; i++) {
            Table t = (Table) tTable.get(i);

            if (t.tableType == Table.CACHED_TABLE) {
                int[] rootsArray = writeTableToDataFile(t, dest);

                rootsList.add(rootsArray);
            } else {
                rootsList.add(null);
            }

            Trace.printSystemOut(t.getName().name + " complete");
        }

        int pos = (int) dest.getFilePointer();

        dest.seek(Cache.FREE_POS_POS);
        dest.writeInt(pos);
        dest.close();

        for (int i = 0, size = rootsList.size(); i < size; i++) {
            int[] roots = (int[]) rootsList.get(i);

            if (roots != null) {
                Trace.printSystemOut(org.hsqldb.lib.StringUtil.getList(roots,
                        ",", ""));
            }
        }

        System.out.println("Transfer complete: " + stopw.elapsedTime());

        return rootsList;
    }

    static void updateTableIndexRoots(org.hsqldb.lib.HsqlArrayList tTable,
                                      HsqlArrayList rootsList)
                                      throws SQLException {

        for (int i = 0, size = tTable.size(); i < size; i++) {
            Table t = (Table) tTable.get(i);

            if (t.tableType == Table.CACHED_TABLE) {
                int[] rootsArray = (int[]) rootsList.get(i);

                t.setIndexRoots(rootsArray);
            }
        }
    }

    int[] writeTableToDataFile(Table table,
                               RandomAccessFile destFile)
                               throws IOException, SQLException {

        BinaryServerRowOutput rowOut = new BinaryServerRowOutput();
        UnifiedTable pointerLookup = new UnifiedTable(int.class, 2, 1000000,
            100000);
        int[] rootsArray  = table.getIndexRootsArray();
        Index index       = table.getPrimaryIndex();
        long  pos         = destFile.getFilePointer();
        int[] pointerPair = new int[2];
        int   count       = 0;

        System.out.println("lookup begins: " + stopw.elapsedTime());

        for (Node n = index.first(); n != null; count++) {
            CachedRow row = (CachedRow) n.getRow();

            pointerPair[0] = row.iPos;
            pointerPair[1] = (int) pos;

            pointerLookup.addRow(pointerPair);

            pos += row.storageSize;

            if (count % 50000 == 0) {

//                System.gc();
                Trace.printSystemOut("pointer pair for row " + count + " "
                                     + pointerPair[0] + " " + pointerPair[1]);
            }

            n = index.next(n);
        }

        System.out.println(table.getName().name + " list done");
        System.out.println("sort begins: " + stopw.elapsedTime());
        pointerLookup.sort(0, true);
        System.out.println("sort ends: " + stopw.elapsedTime());

        count = 0;

        for (Node n = index.first(); n != null; count++) {
            CachedRow row        = (CachedRow) n.getRow();
            int       rowPointer = (int) destFile.getFilePointer();

            rowOut.reset();

// should go to CachedRow
            rowOut.writeSize(row.storageSize);

            Node rownode = row.nPrimaryNode;

            while (rownode != null) {
                ((DiskNode) rownode).writeTranslate(rowOut, pointerLookup);

                rownode = rownode.nNext;
            }

            rowOut.writeData(row.getData(), row.getTable());
            rowOut.writePos(rowPointer);

// end
            destFile.write(rowOut.getOutputStream().getBuffer(), 0,
                           rowOut.size());

/*
            if (rowOut.size() != row.storageSize) {
                System.out.println("MISMATCH AT " + count);
            }
*/
            if ((count + 1) % 50000 == 0) {

//                System.gc();
                System.out.println(count + " rows " + stopw.elapsedTime());
            }

            n = index.next(n);
        }

        for (int i = 0; i < rootsArray.length; i++) {
            int lookupIndex = pointerLookup.search(rootsArray[i]);

            if (lookupIndex == -1) {
                throw new SQLException();
            }

            rootsArray[i] = pointerLookup.getIntCell(lookupIndex, 1);
        }

        Trace.printSystemOut(table.getName().name + " : table converted");

        return rootsArray;
    }
}
