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

import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.HsqlStringBuffer;
import java.io.*;
import java.sql.SQLException;

/**
 * @author fredt@users
 * @version 1.7.2
 */

// todo - can lock the database engine as readonly in a wrapper for this when
// used at checkpoint
class DatabaseScriptWriter {

    Database          db;
    String            outFile;
    FileOutputStream  fileStreamOut;
    DatabaseRowOutput binaryOut = new BinaryServerRowOutputTest();
    int               tableRowCount;
    boolean           includeCachedData;
    long              count;
    boolean           noWriteDelay = true;
    boolean           needsFlush   = false;
    static final int  INSERT       = 0;
    static byte[]     lineSep;

    static {
        String sLineSep = System.getProperty("line.separator", "\n");

        lineSep = new byte[sLineSep.length()];

        for (int i = 0; i < sLineSep.length(); i++) {
            lineSep[i] = (byte) sLineSep.charAt(i);
        }
    }

    DatabaseScriptWriter(Database db, String file, boolean includeCachedData,
                         boolean newFile) throws SQLException {

        File newFileFile = new File(file);

        if (newFileFile.exists()) {
            if (newFile) {
                throw Trace.error(Trace.FILE_IO_ERROR, file);
            } else {
                count = newFileFile.length();
            }
        }

        this.db                = db;
        this.includeCachedData = includeCachedData;
        outFile                = file;

        try {
            fileStreamOut = new FileOutputStream(file, true);
        } catch (IOException e) {
            throw Trace.error(Trace.FILE_IO_ERROR, file);
        }
    }

    void setWriteDelay(boolean delay) {
        noWriteDelay = !delay;
    }

/**
 *  Only use externally.
 */
    void flush() throws IOException {

        if (needsFlush) {
            fileStreamOut.flush();
        }

        needsFlush = false;
    }

    void close() throws IOException {
        fileStreamOut.flush();
        fileStreamOut.close();
    }

    long size() {
        return count;
    }

    void writeAll() throws SQLException {

        try {
            writeDDL();
            writeExistingData();
            fileStreamOut.flush();
        } catch (IOException e) {
            throw Trace.error(Trace.FILE_IO_ERROR);
        }
    }

    protected void writeDDL() throws IOException, SQLException {

        Result ddlPart = DatabaseScript.getScript(db, false, false,
            !includeCachedData);

        writeSingleColumnResult(ddlPart);
    }

    protected void writeExistingData() throws SQLException, IOException {

        boolean       wroteTable = false;
        HsqlArrayList tables     = db.getTables();

        for (int i = 0; i < tables.size(); i++) {
            Table t = (Table) tables.get(i);

            // write all memory table data
            // write cached table data unless index roots have been written
            // write all text table data apart from readonly text tables
            if (t.tableType == Table
                    .MEMORY_TABLE || (t.tableType == Table
                        .CACHED_TABLE && includeCachedData) || (t
                        .tableType == Table.TEXT_TABLE &&!t.isReadOnly)) {
                writeTableInit(t);

                Index primary = t.getPrimaryIndex();
                Node  x       = primary.first();

                while (x != null) {
                    writeRow(x.getData(), t);

                    x = primary.next(x);
                }

                writeTableTerm(t);
            }
        }

        writeDataTerm();
    }

    protected void writeTableInit(Table t) throws SQLException, IOException {}

    protected void writeTableTerm(Table t) throws SQLException, IOException {

        if (t.isDataReadOnly() &&!t.isTemp() &&!t.isText()) {
            HsqlStringBuffer a = new HsqlStringBuffer("SET TABLE ");

            a.append(t.getName().statementName);
            a.append(" READONLY TRUE");
            writeLogStatement(a.toString());
        }
    }

    protected void writeSingleColumnResult(Result r)
    throws SQLException, IOException {

        Record n = r.rRoot;

        while (n != null) {
            writeLogStatement((String) n.data[0]);

            n = n.next;
        }
    }

    protected void writeRow(Object[] data,
                            Table t) throws SQLException, IOException {
        writeLogStatement(t.getInsertStatement(data));
    }

    protected void writeDataTerm() throws IOException {}

    void writeLogStatement(String s) throws IOException, SQLException {

        binaryOut.reset();
        StringConverter.unicodeToAscii(binaryOut, s);
        binaryOut.write(lineSep);
        fileStreamOut.write(binaryOut.getBuffer(), 0, binaryOut.size());

        count += binaryOut.size();

        if (noWriteDelay) {
            fileStreamOut.flush();
        } else {
            needsFlush = true;
        }
    }
}
