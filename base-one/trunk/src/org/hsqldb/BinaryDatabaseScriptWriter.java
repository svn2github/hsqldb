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

import java.io.*;
import java.sql.SQLException;

/**
 * @version 1.7.2
 */
class BinaryDatabaseScriptWriter extends DatabaseScriptWriter {

    BinaryDatabaseScriptWriter(Database db, String file,
                               boolean includeCached,
                               boolean newFile) throws SQLException {
        super(db, file, includeCached, newFile);
    }

    protected void writeSingleColumnResult(Result r)
    throws IOException, SQLException {

        byte[] bytes = r.getBytes();

        binaryOut.reset();
        binaryOut.writeSize(bytes.length);
        fileStreamOut.write(binaryOut.getBuffer(), 0, binaryOut.size());
        fileStreamOut.write(bytes);
    }

    // int : row size (0 if no more rows) ,
    // BinaryServerRowInput : row (column values)
    protected void writeRow(Object[] data,
                            Table t) throws IOException, SQLException {

        binaryOut.reset();
        binaryOut.writeSize(0);
        binaryOut.writeData(data, t);
        binaryOut.writeIntData(binaryOut.size(), 0);
        fileStreamOut.write(binaryOut.getBuffer(), 0, binaryOut.size());

        tableRowCount++;
    }

    // int : headersize (0 if no more tables), String : tablename, int : operation,
    protected void writeTableInit(Table t) throws SQLException, IOException {

        tableRowCount = 0;

        binaryOut.reset();
        binaryOut.writeSize(0);
        binaryOut.writeString(t.getName().name);
        binaryOut.writeIntData(INSERT);
        binaryOut.writeIntData(binaryOut.size(), 0);
        fileStreamOut.write(binaryOut.getBuffer(), 0, binaryOut.size());
    }

    protected void writeTableTerm(Table t) throws IOException {

        binaryOut.reset();
        binaryOut.writeSize(0);
        binaryOut.writeIntData(this.tableRowCount);
        fileStreamOut.write(binaryOut.getBuffer(), 0, binaryOut.size());
    }

    protected void writeDataTerm() throws IOException {

        binaryOut.reset();
        binaryOut.writeSize(0);
        fileStreamOut.write(binaryOut.getBuffer(), 0, binaryOut.size());
    }
}
