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
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */
class BinaryDatabaseScriptReader extends DatabaseScriptReader {

    BinaryServerRowInput rowIn;

    BinaryDatabaseScriptReader(Database db,
                               String file) throws SQLException, IOException {
        super(db, file);
        rowIn = new BinaryServerRowInputTest();
    }

    void readAll(Session session) throws IOException, SQLException {
        readDDL(session);
        readExistingData(session);
    }

    protected void readDDL(Session session) throws IOException, SQLException {
        readSingleColumnResult(session);
    }

    protected void readSingleColumnResult(Session session) throws IOException, SQLException {

        byte[] bytes = new byte[dataStreamIn.readInt()];

        dataStreamIn.read(bytes);

        Result r = new Result(bytes);
        Record n = r.rRoot;
        String s;

        while (n != null) {
            s = (String) n.data[0];

            db.execute(s, session);

            n = n.next;
        }
    }

    protected void readExistingData(Session session) throws IOException, SQLException {

        for (int i = 0; ; i++) {
            String s = readTableInit();

            if (s == null) {
                break;
            }

            Table t = db.getTable(s, session);
            int   j = 0;

            for (j = 0; ; j++) {
                if (readRow(t) == false) {
                    break;
                }
            }

            int checkCount = readTableTerm();

            if (j != checkCount) {
                System.out.println("table " + s + " row count error : " + j
                                   + " read, needed " + checkCount);

                // error
            }
        }
    }

    // int : row size (0 if no more rows) ,
    // BinaryServerRowInput : row (column values)
    protected boolean readRow(Table t) throws IOException, SQLException {

        int size = dataStreamIn.readInt();

        if (size == 0) {
            return false;
        }

        rowIn.resetRow(0, size);
        dataStreamIn.read(rowIn.getBuffer(), 4, size - 4);

        Object[] row = rowIn.readData(t.getColumnTypes());

        t.insertNoCheck(row, null, false);

        return true;
    }

    // int : rowcount
    protected int readTableTerm() throws IOException, SQLException {

        rowIn.resetRow(0, 8);
        dataStreamIn.read(rowIn.getBuffer(), 4, 4);

        return rowIn.readIntData();
    }

    // int : headersize (0 if no more tables), String : tablename, int : operation,
    protected String readTableInit() throws IOException, SQLException {

        int size = dataStreamIn.readInt();

        if (size == 0) {
            return null;
        }

        rowIn.resetRow(0, size);
        dataStreamIn.read(rowIn.getBuffer(), 4, size - 4);

        String s = rowIn.readString();

        // operation is always INSERT
        int checkOp = rowIn.readIntData();

        if (checkOp != DatabaseScriptWriter.INSERT) {

            // error
        }

        return s;
    }
}
