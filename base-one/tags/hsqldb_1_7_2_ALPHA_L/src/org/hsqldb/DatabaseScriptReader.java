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
import org.hsqldb.lib.StringConverter;

/**
 * Handles operations involving reading back a log file already written
 * out by DatabaseScriptWriter. This implementation and its subclasses
 * correspond to DatabaseScriptWriter and its subclasses for the supported
 * formats.
 *
 * @author fredt@users
 * @version 1.7.2
 */
class DatabaseScriptReader {

    InputStream dataStreamIn;
    Database    db;
    int         lineCount;

//    int         byteCount;
    // this is used only to enable reading one logged line at a time
    BufferedReader d;
    String         fileName;

    static DatabaseScriptReader newDatabaseScriptReader(Database db,
            String file, int scriptType) throws SQLException, IOException {

        if (scriptType == DatabaseScriptWriter.SCRIPT_TEXT_170) {
            return new DatabaseScriptReader(db, file);
        } else if (scriptType == DatabaseScriptWriter.SCRIPT_BINARY_172) {
            return new BinaryDatabaseScriptReader(db, file);
        } else {
            return new ZippedDatabaseScriptReader(db, file);
        }
    }

    DatabaseScriptReader(Database db,
                         String file) throws SQLException, IOException {

        this.db  = db;
        fileName = file;

        openFile();
    }

    void readAll(Session session) throws IOException, SQLException {}

    int getLineNumber(){
        return lineCount;
    }

    protected void openFile() throws IOException {
        dataStreamIn = new DataInputStream(new FileInputStream(fileName));
        d = new BufferedReader(new InputStreamReader(dataStreamIn));
    }

    protected String readLoggedStatement() {

        try {

            //fredt temporary solution - should read bytes directly from buffer
            String s = d.readLine();
            lineCount++;

            return StringConverter.asciiToUnicode(s);
        } catch (IOException e) {
            return null;
        }
    }

    void close() throws IOException {
        d.close();
        dataStreamIn.close();
    }
}
