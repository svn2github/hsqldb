/*
 * $Id: SqlFile.java,v 1.1 2004/01/19 19:50:15 unsaved Exp $
 *
 * Copyright (c) 2001-2003, The HSQL Development Group
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

package org.hsqldb.util;

import java.io.IOException;
import java.io.PrintStream;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.sql.Connection;

public class SqlFile {
    private File file;
    private boolean interactive;
    private String primaryPrompt = "sql> ";
    private String contPrompt    = "  +> ";

    /**
     * @param inFile  inFile of null means to read stdin.
     * @param inInteractive  If true, print prompts and continue if errors
     *                       are encountered.
     */
    SqlFile(File inFile, boolean inInteractive) throws IOException {
        file = inFile;
        interactive = inInteractive;
System.err.println("Interactive? " + interactive);
        if (file != null && !file.canRead()) {
            throw new IOException("Can't read SQL file '" + file + "'");
        }
    }

    SqlFile(boolean inInteractive) throws IOException {
        this(null, inInteractive);
    }

    public void execute(Connection conn) throws IOException {
        execute(conn, System.out, System.err);
    }

    private String curCommand = null;
    private int curLinenum = -1;

    /**
     * Run SQL in the file through the given database connection.
     *
     * This is synchronized so that I can use object variables to keep
     * track of current line number and command.
     */
    public synchronized void execute(Connection conn, PrintStream psStd,
            PrintStream psErr) throws IOException {
        curLinenum = -1;
        BufferedReader br = new BufferedReader(new InputStreamReader(
                (file == null) ? System.in : new FileInputStream(file)));
        psErr.println("Executing '" + file + "'");
        String s;
        curLinenum = 0;
        while (true) {
            if (interactive) psStd.print(
                    (curLinenum == 0) ? primaryPrompt : contPrompt);
            s = br.readLine();
            if (s == null) break;
            curLinenum++;
            psStd.println(Integer.toString(curLinenum) + ": " + s);
        }
    }
}
