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

import java.sql.SQLException;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.UnifiedTable;

/**
 *  Experimental routine to defrag the *.data file.
 *  This method iterates through the primary index of a table to find the
 *  disk position for each row and stores it, together with the new position
 *  in an array. Simulatneously, the disk record is copied from the old file
 *  into the new.
 *  A second pass over the new disk image translates the old pointers to the
 *  new.
 *
 * @version    1.7.2
 * @author     frest@users
 */
class DataFileDefrag1 {

    HsqlArrayList defrag(Database db, DatabaseFile source,
                     String filename) throws IOException, SQLException {

        HsqlArrayList                    rootsList = new HsqlArrayList();
        org.hsqldb.lib.HsqlArrayList tTable    = db.getTables();
        RandomAccessFile dest = new RandomAccessFile(filename + ".new", "rw");

        dest.seek(Cache.INITIAL_FREE_POS);

        for (int i = 0, tSize = tTable.size(); i < tSize; i++) {
            Table t = (Table) tTable.get(i);

            if (t.tableType == Table.CACHED_TABLE) {
                int[] rootsArray = writeTableToDataFile(t, source, dest);

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

        for (int i = 0; i < rootsList.size(); i++) {
            int[] roots = (int[]) rootsList.get(i);

            if (roots != null) {
                Trace.printSystemOut(org.hsqldb.lib.StringUtil.getList(roots,
                        ",", ""));
            }
        }

        return rootsList;
    }

    static void updateTableIndexRoots(org.hsqldb.lib.HsqlArrayList tTable,
                                      HsqlArrayList rootsList)
                                      throws SQLException {

        for (int i = 0, tSize = tTable.size(); i < tSize; i++) {
            Table t = (Table) tTable.get(i);

            if (t.tableType == Table.CACHED_TABLE) {
                int[] rootsArray = (int[]) rootsList.get(i);

                t.setIndexRoots(rootsArray);
            }
        }
    }

    int[] writeTableToDataFile(Table table, DatabaseFile source,
                               RandomAccessFile dest)
                               throws IOException, SQLException {

        UnifiedTable pointerLookup = new UnifiedTable(int.class, 2, 1000000,
            100000);
        int[]      rootsArray  = table.getIndexRootsArray();
        Index      index       = table.getPrimaryIndex();
        Node       n           = index.first();
        RawDiskRow readRow     = new RawDiskRow();
        long       pos         = dest.getFilePointer();
        int        count       = 0;
        int[]      pointerPair = new int[2];

        System.out.println("lookup begins: "
                           + new java.util.Date(System.currentTimeMillis()));

        for (; n != null; count++) {
            CachedRow row        = (CachedRow) n.getRow();
            int       oldPointer = row.iPos;

            source.readSeek(oldPointer);
            readRow.read(source, rootsArray.length);

            int newPointer = (int) dest.getFilePointer();

            readRow.write(dest);

            pointerPair[0] = oldPointer;
            pointerPair[1] = newPointer;

            pointerLookup.addRow(pointerPair);

            if (count % 50000 == 0) {
                Trace.printSystemOut("pointer pair for row " + oldPointer
                                     + " " + newPointer);
            }

            n = index.next(n);
        }

// todo - put the new freepos
        Trace.printSystemOut(table.getName().name + " transfered");
        dest.seek(pos);
        System.out.println("sort begins: "
                           + new java.util.Date(System.currentTimeMillis()));
        pointerLookup.sort(0, true);
        System.out.println("sort ends: "
                           + new java.util.Date(System.currentTimeMillis()));

        for (int i = 0; i < count; i++) {
            readRow.readNodes(dest, rootsArray.length);
            readRow.replacePointers(pointerLookup);
            dest.seek(readRow.filePosition);
            readRow.writeNodes(dest);
            dest.seek(readRow.filePosition + readRow.storageSize);

            if (i != 0 && i % 50000 == 0) {
                System.out.println(
                    i + " rows "
                    + new java.util.Date(System.currentTimeMillis()));
            }
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
