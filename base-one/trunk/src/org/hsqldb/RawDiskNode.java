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

import java.sql.SQLException;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.hsqldb.lib.HsqlHashMap;
import org.hsqldb.lib.UnifiedTable;

/**
 *  Wrapper for disk image of CachedNode used for direct file defragmentation
 *  and housekeeping tasks. Corresponds to the disk data for a DiskNode
 *  object.
 *
 *  Designed so that the object can be reused for many rows.
 *
 * @version    1.7.2
 * @author     frest@users
 */
class RawDiskNode {

    int              iBalance;
    int              iLeft;
    int              iRight;
    int              iParent;
    final static int storageSize = 16;

    void write(DatabaseFile file) throws IOException {

        file.writeInteger(iBalance);
        file.writeInteger(iLeft);
        file.writeInteger(iRight);
        file.writeInteger(iParent);
    }

    void write(RandomAccessFile file) throws IOException {

        file.writeInt(iBalance);
        file.writeInt(iLeft);
        file.writeInt(iRight);
        file.writeInt(iParent);
    }

    void read(DatabaseFile file) throws IOException {

        iBalance = file.readInteger();
        iLeft    = file.readInteger();
        iRight   = file.readInteger();
        iParent  = file.readInteger();
    }

    void read(RandomAccessFile file) throws IOException {

        iBalance = file.readInt();
        iLeft    = file.readInt();
        iRight   = file.readInt();
        iParent  = file.readInt();
    }

    void replacePointers(UnifiedTable lookup) throws SQLException {

        int lookupIndex;

        if (iLeft != 0) {
            lookupIndex = lookup.search(iLeft);

            if (lookupIndex == -1) {
                throw new SQLException();
            }

            iLeft = lookup.getIntCell(lookupIndex, 1);
        }

        if (iRight != 0) {
            lookupIndex = lookup.search(iRight);

            if (lookupIndex == -1) {
                throw new SQLException();
            }

            iRight = lookup.getIntCell(lookupIndex, 1);
        }

        if (iParent != 0) {
            lookupIndex = lookup.search(iParent);

            if (lookupIndex == -1) {
                throw new SQLException();
            }

            iParent = lookup.getIntCell(lookupIndex, 1);
        }
    }
}
