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
import org.hsqldb.lib.HsqlHashMap;
import org.hsqldb.lib.UnifiedTable;

/**
 *  Wrapper for disk image of CathecRow and its CachedNodes used for direct
 *  file defragmentation and housekeeping tasks. Corresponds to the disk data
 *  for a database row, including Row and its Node objects.
 *
 *  Designed so that each object is reusable for many rows.
 *
 * @version    1.7.2
 * @author     frest@users
 */
class RawDiskRow {

    int           storageSize;
    RawDiskNode[] diskNodes  = new RawDiskNode[0];
    int           indexCount = 0;    // count of utilised elements of diskNodes
    byte[]        rowData    = new byte[0];
    int           rowDataLength;
    long          filePosition;

    void write(DatabaseFile file) throws IOException {

        writeNodes(file);
        file.write(rowData, 0,
                   storageSize - 4 - indexCount * RawDiskNode.storageSize);

//        file.writeInteger((int) filePosition);
    }

    void write(RandomAccessFile file) throws IOException {

        writeNodes(file);
        file.write(rowData, 0,
                   storageSize - 4 - indexCount * RawDiskNode.storageSize);

// sort this one out
//        file.writeInt((int) filePosition);
    }

    void writeNodes(DatabaseFile file) throws IOException {

        file.writeInteger(storageSize);

        for (int i = 0; i < indexCount; i++) {
            diskNodes[i].write(file);
        }
    }

    void writeNodes(RandomAccessFile file) throws IOException {

        file.writeInt(storageSize);

        for (int i = 0; i < indexCount; i++) {
            diskNodes[i].write(file);
        }

// sort this one out
//        file.writeInteger((int) filePosition);
    }

    void read(DatabaseFile file, int indcount) throws IOException {

        readNodes(file, indcount);

        // fredt change to size of raw node
        file.read(rowData, 0,
                  storageSize - 4 - indcount * RawDiskNode.storageSize);
    }

    void readNodes(DatabaseFile file, int indcount) throws IOException {

        filePosition = file.pos;
        indexCount   = indcount;

        if (indexCount > diskNodes.length) {
            diskNodes = new RawDiskNode[indexCount];

            for (int i = 0; i < indexCount; i++) {
                diskNodes[i] = new RawDiskNode();
            }
        }

        storageSize = file.readInteger();

        for (int i = 0; i < indexCount; i++) {
            diskNodes[i].read(file);
        }

        if (storageSize > rowData.length) {
            rowData = new byte[storageSize];
        }
    }

    void readNodes(RandomAccessFile file, int indcount) throws IOException {

        filePosition = file.getFilePointer();
        indexCount   = indcount;

        if (indexCount > diskNodes.length) {
            diskNodes = new RawDiskNode[indexCount];

            for (int i = 0; i < indexCount; i++) {
                diskNodes[i] = new RawDiskNode();
            }
        }

        storageSize = file.readInt();

        for (int i = 0; i < indexCount; i++) {
            diskNodes[i].read(file);
        }

        if (storageSize > rowData.length) {
            rowData = new byte[storageSize];
        }
    }

    void replacePointers(UnifiedTable lookup) throws SQLException {

        for (int i = 0; i < indexCount; i++) {
            diskNodes[i].replacePointers(lookup);
        }
    }
}
