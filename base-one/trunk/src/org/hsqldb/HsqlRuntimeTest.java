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

/**
 * A quick test of HsqlRuntime functionality and correctness.
 *
 * @author boucher@users.sourceforge.net
 * @since HSQLDB 1.7.2
 * @version 1.7.2
 */
public class HsqlRuntimeTest {

    static final String path = "/test";
    static final String sep =
        "----------------------------------------------------";
    static final double onemb = 1 << 20;
    static final double onekb = 1 << 10;

    /**
     * Runs all tests.
     *
     * @param args ignored
     *
     * @throws Exception maybe
     */
    public static void main(String[] args) throws Exception {
        testAll();
    }

    static void print(Object o) {
        System.out.print(o);
    }

    static void println(Object o) {
        System.out.println(o);
    }

    static String toMB(long bytes) {
        return String.valueOf(bytes / onemb);
    }

    static String toKB(long bytes) {
        return String.valueOf(bytes / onekb);
    }

    static void testAll() throws Exception {

        HsqlRuntime                   runtime;
        Database                      database;
        String                        cpath;
        HsqlRuntime.DatabaseReference ref;

        runtime = HsqlRuntime.getHsqlRuntime();
        cpath = runtime.canonicalDatabasePath(path);

        println(sep);
        print("maxMemory() test: ");
        println(toMB(runtime.maxMemory()) + " MB");
        println(sep);
        print("availableMemory() test: ");
        println(toMB(runtime.availableMemory()) + " MB");
        println(sep);
        print("usedMemory() test: ");
        println(toKB(runtime.usedMemory()) + " KB");
        println(sep);
        print("availableProcessors() test: ");
        println("" + runtime.availableProcessors());
        println(sep);
        print("getDatabase() test; should be 'org.hsqldb.Database@xxx': ");

        database = runtime.getDatabase(path, null);

        println(database);
        println(sep);
        print("usedMemory() test: ");
        println(toKB(runtime.usedMemory()) + " KB");
        println(sep);
        println("stateDescriptor() test;");
        println("Should be list of hsqldb thread groups,");
        println("showing place holder thread for " + cpath + ":");
        println(sep);
        print(runtime.stateDescriptor());
        println(sep);
        println("listRegisteredDatabaseNames() test:");
        print("should be [" + database.getName() + "]: ");
        println(runtime.listRegisteredDatabaseNames());
        println(sep);
        print("isRegisteredDatabase() test; should be 'true': ");
        println("" + runtime.isRegisteredDatabase(path));
        println(sep);
        println("removeDatabase() test;");
        println("Next line should be a message that thread is ");
        println("exiting, followed by thread group listing NOT ");
        println("showing place holder thread for " + cpath);
        println(sep);
        database.sessionManager.getSysSession().sqlExecuteDirect("shutdown");
        print(runtime.stateDescriptor());
        println(sep);
        println("usedMemory() test: " + toKB(runtime.usedMemory()) + " KB");
        println(sep);
        print("isRegisteredDatabase() test; should be 'false': ");
        println("" + runtime.isRegisteredDatabase(path));
        println(sep);
        println("database should now be shut down");
        System.runFinalization();
        println(sep);
        println("DatabaseReference.newReference() test;");
        println("Next line should show string rep of valid ref:");
        println(sep);

        ref = HsqlRuntime.DatabaseReference.newReference(path);

        println(ref);
        println(sep);
        println("usedMemory() test: " + toKB(runtime.usedMemory()) + " KB");
        println(sep);
        println("stateDescriptor() test while holding valid reference;");
        println("Should be list of hsqldb thread groups,");
        println("showing place holder thread for " + cpath + ":");
        println(sep);
        print(runtime.stateDescriptor());
        println(sep);
        println("DatabaseReference.clear() test");
        ref.clear();
        println("Next line should show string rep of invalid ref:");
        println(ref);
        println(sep);
        println("usedMemory() test: " + toKB(runtime.usedMemory()) + " KB");
        println(sep);
        println("stateDescriptor() test while holding invalid ref;");
        println("Should be list of hsqldb thread groups");
        print("NOT showing place holder thread for ");
        println(cpath + ":");
        println(sep);
        print(runtime.stateDescriptor());
        println(sep);
        println("database should now be shut down");
        println(sep);
        print("usedMemory() test: ");
        println("" + toKB(runtime.usedMemory()) + " KB");
        println(sep);
        println("System.gc()");
        System.gc();
        println(sep);
        println("usedMemory() test: " + (runtime.usedMemory() / onekb)
                + " KB");
        println(sep);
    }
}
