/* Copyright (c) 2001-2005, The HSQL Development Group
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


package org.hsqldb.lib;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * Class for debugging OOo file access issues
 *
 * @author fredt@users
 */
public class SimpleLog {

    private PrintWriter writer;
    private int         level;

    public SimpleLog(String path, int level, boolean useFile) {

        this.level = level;

        if (level != 0) {
            if (useFile) {
                File file = new File(path);

                makeLog(file);
            } else {
                writer = new PrintWriter(System.out);
            }
        }
    }

    private void makeLog(File file) {

        try {
            FileUtil.makeParentDirectories(file);

            writer = new PrintWriter(new FileWriter(file.getPath(), true),
                                     true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getLevel() {
        return level;
    }

/*
    public void setLevel(int level) {
        this.level = level;
    }
*/
    public PrintWriter getPrintWriter() {
        return writer;
    }

    public synchronized void sendLine(String message) {

        if (level != 0) {
            writer.println(message);
        }
    }

    public synchronized void logContext(String message) {

        if (level == 0) {
            return;
        }

//#ifdef JDBC3
        Throwable           t        = new Throwable();
        StackTraceElement[] elements = t.getStackTrace();
        String info = elements[1].getClassName() + "."
                      + elements[1].getMethodName();

        writer.println(info + " " + message);

//#else
/*
*/

//#endif
    }

    public synchronized void logContext(Throwable t) {

        if (level == 0) {
            return;
        }

//#ifdef JDBC3
        StackTraceElement[] elements = t.getStackTrace();
        String info = elements[0].getClassName() + "."
                      + elements[0].getMethodName();

        writer.println(info + " " + t.getMessage());

//#else
/*
*/

//#endif
    }

    public void close() {

        if (writer != null) {
            writer.close();
        }
    }
}
