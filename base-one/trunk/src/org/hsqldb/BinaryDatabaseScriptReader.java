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

/**
 * Reader corresponding to BinaryDatabaseScritReader.
 *
 * @author fredt@users
 * @version 1.7.2
 * @since 1.7.2
 */
class BinaryDatabaseScriptReader extends DatabaseScriptReader {

    BinaryServerRowInput rowIn;

    BinaryDatabaseScriptReader(Database db,
                               String file)
                               throws HsqlException, IOException {

        super(db, file);

        rowIn = new BinaryServerRowInput();
    }

    protected void readDDL(Session session)
    throws IOException, HsqlException {
        readSingleColumnResult(session);
    }

    protected void readSingleColumnResult(Session session)
    throws IOException, HsqlException {

        Result r = HSQLClientConnection.read(rowIn, dataStreamIn);
/*
        readRow(rowIn, 0, dataStreamIn);
        Result r = new Result(rowIn);
*/
        Record n = r.rRoot;
        String s;

        while (n != null) {
            s = (String) n.data[0];

            session.sqlExecuteDirect(s);

            n = n.next;
        }
    }

    protected void readExistingData(Session session)
    throws IOException, HsqlException {

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
                throw Trace.error(Trace.ERROR_IN_SCRIPT_FILE,
                                  Trace.BinaryDatabaseScriptReader_readExistingData,
                                  new Object[]{ s, new Integer( j ), new Integer( checkCount ) } );
            }
        }
    }

    // int : row size (0 if no more rows) ,
    // BinaryServerRowInput : row (column values)
    protected boolean readRow(Table t) throws IOException, HsqlException {

        boolean more = readRow(rowIn, 0, dataStreamIn);

        if (!more) {
            return false;
        }

        Object[] row = rowIn.readData(t.getColumnTypes());

        t.insertNoCheck(row, null, false);

        return true;
    }

    // int : rowcount
    protected int readTableTerm() throws IOException, HsqlException {

        rowIn.reset();

        int count  = 0;
        int length = 4;

        while (dataStreamIn.available() > 0 && count < length) {
            count += dataStreamIn.read(rowIn.getBuffer(), count,
                                       length - count);
        }

        return rowIn.readInt();
    }

    // int : headersize (0 if no more tables), String : tablename, int : operation,
    protected String readTableInit() throws IOException, HsqlException {

        boolean more = readRow(rowIn, 0, dataStreamIn);

        if (!more) {
            return null;
        }

        String s = rowIn.readString();

        // operation is always INSERT
        int checkOp = rowIn.readIntData();

        if (checkOp != DatabaseScriptWriter.INSERT) {
            throw Trace.error(Trace.ERROR_IN_SCRIPT_FILE,
                              Trace.BinaryDatabaseScriptReader_readTableInit,
                              null);
        }

        return s;
    }

    boolean readRow(DatabaseRowInput rowIn, int pos,
                    InputStream streamIn) throws IOException {

        rowIn.reset();

        int count  = 0;
        int length = 4;

        while (streamIn.available() > 0 && count < length) {
            count += dataStreamIn.read(rowIn.getBuffer(), count,
                                       length - count);
        }

        length = rowIn.readInt();

        if (length == 0) {
            return false;
        }

        rowIn.resetRow(pos, length);

        while (streamIn.available() > 0 && count < length) {
            count += dataStreamIn.read(rowIn.getBuffer(), count,
                                       length - count);
        }

        // problem if count != length
        return true;
    }
}
