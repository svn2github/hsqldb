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

import java.lang.reflect.Method;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.HsqlTimer;

/**
 *
 * @author  campbell
 */
public final class HsqlRuntime {

    private static final String rtgName  = "hsqldb-system";
    private static final String dbtgName = "hsqldb-databases";
    private static final String stgName  = "hsqldb-servers";
    private static final String wstgName = "hsqldb-webservers";
    private static ThreadGroup  rtg;
    private static ThreadGroup  dbThreadGroup;
    private static ThreadGroup  serverThreadGroup;
    private static ThreadGroup  webServerThreadGroup;
    private static Hashtable    dbThreadMap;
    private static Hashtable    dbMap;
    private static Hashtable    dbUseCountMap;
    private static Database     memoryDatabase;
    private static HsqlTimer    timer;

    /** Construction disabled */
    private HsqlRuntime() {}

    private static void initStatic() {

        ThreadGroup root;

        dbThreadMap   = new Hashtable();
        dbMap         = new Hashtable();
        dbUseCountMap = new Hashtable();

        try {
            root = findRootThreadGroup();
            rtg  = findRootHsqldbThreadGroup(root);

            if (rtg == null) {
                rtg = new ThreadGroup(root, rtgName);

                rtg.setDaemon(false);
            }
        } catch (Exception e1) {
            e1.printStackTrace();

            try {
                rtg = new ThreadGroup(rtgName);
            } catch (Exception e2) {
                e2.printStackTrace();

                rtg = null;
            }
        }

        if (rtg != null) {
            try {
                dbThreadGroup        = new ThreadGroup(rtg, dbtgName);
                serverThreadGroup    = new ThreadGroup(rtg, stgName);
                webServerThreadGroup = new ThreadGroup(rtg, wstgName);

                dbThreadGroup.setDaemon(false);
                serverThreadGroup.setDaemon(false);
                webServerThreadGroup.setDaemon(false);

                if (Trace.TRACE) {
                    Trace.trace("HsqlMasterRepository.initStatic()");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        timer = new HsqlTimer();
    }

    static {
        initStatic();
    }

    public static String getAbsoluteDatabaseName(String name) {

        return name == null ? null
                            : ".".equals(name.trim()) ? name.trim()
                                                      : (new File(name
                                                      .trim()))
                                                          .getAbsolutePath();
    }

    static HsqlTimer getTimer() {
        return timer;
    }

    static synchronized Database getDatabase(String name,
            Object requestor) throws SQLException {

        Database          database;
        PlaceHolderThread t;
        Integer           useCount;

        name     = getAbsoluteDatabaseName(name);
        database = findDatabase(name);

        if (database != null) {
            useCount = (Integer) dbUseCountMap.get(database);
            useCount = new Integer(useCount.intValue() + 1);

            dbUseCountMap.put(database, useCount);

            return database;
        }

        if (isOpenDatabase(name)) {
            throw Trace.error(Trace.DATABASE_ALREADY_IN_USE, name);
        }

        database = new Database(name);

        dbMap.put(name, database);

        t = new PlaceHolderThread(dbThreadGroup, name);

        t.start();
        dbThreadMap.put(name, t);
        dbUseCountMap.put(database, new Integer(1));

        return database;
    }

    private static synchronized ThreadGroup findRootThreadGroup()
    throws Exception {

        ThreadGroup tg;

        tg = Thread.currentThread().getThreadGroup();

        while (tg != null && tg.getParent() != null) {
            tg = tg.getParent();
        }

        return tg;
    }

    private static synchronized ThreadGroup findRootHsqldbThreadGroup(
            ThreadGroup tg) throws Exception {

        ThreadGroup[] rgs;

        if (tg != null) {
            rgs = new ThreadGroup[(tg.activeGroupCount())];

            tg.enumerate(rgs);

            for (int i = 0; i < rgs.length; i++) {
                tg = rgs[i];

                if (tg != null && rtgName.equals(tg.getName())) {
                    return tg;
                }
            }
        }

        return null;
    }

    public static synchronized String toDebugString() {

        ByteArrayOutputStream baos;
        PrintStream           ps;
        PrintStream           tempps;

        if (rtg == null) {
            return null;
        }

        baos = new ByteArrayOutputStream();
        ps   = new PrintStream(baos);

        synchronized (System.out) {
            tempps = System.out;

            System.setOut(ps);
            rtg.list();
            System.setOut(tempps);
        }

        ps.flush();

        return baos.toString();
    }

    public static synchronized HsqlArrayList listDatabases() {

        int           count;
        Thread[]      threads;
        HsqlArrayList nameList;

        count    = dbThreadGroup.activeCount();
        threads  = new Thread[count];
        nameList = new HsqlArrayList(count);

        dbThreadGroup.enumerate(threads);

        for (int i = 0; i < count; i++) {
            if (threads[i] != null) {
                nameList.add(threads[i].getName());
            }
        }

        return nameList;
    }

    public static synchronized HsqlArrayList listServers() {

        int           count;
        Thread[]      threads;
        HsqlArrayList nameList;

        count    = serverThreadGroup.activeCount();
        threads  = new Thread[count];
        nameList = new HsqlArrayList(count);

        serverThreadGroup.enumerate(threads);

        for (int i = 0; i < count; i++) {
            if (threads[i] != null) {
                nameList.add(threads[i]);
            }
        }

        return nameList;
    }

    public static synchronized HsqlArrayList listWebServers() {

        int           count;
        Thread[]      threads;
        HsqlArrayList nameList;

        count    = webServerThreadGroup.activeCount();
        threads  = new Thread[count];
        nameList = new HsqlArrayList(count);

        webServerThreadGroup.enumerate(threads);

        for (int i = 0; i < count; i++) {
            if (threads[i] != null) {
                nameList.add(threads[i].getName());
            }
        }

        return nameList;
    }

    static synchronized Database findDatabase(String name) {
        return (Database) dbMap.get(getAbsoluteDatabaseName(name));
    }

    public static synchronized boolean isOpenDatabase(String name) {

        HsqlArrayList list;
        int           size;

        name = getAbsoluteDatabaseName(name);

        // open in *this* class loader context
        if (dbMap.containsKey(name)) {
            return true;
        }

        list = listDatabases();
        size = list.size();

        for (int i = 0; i < size; i++) {
            if (name.equals(list.get(i))) {

                // open in some other class loader context
                return true;
            }
        }

        // not open either in this class loader context or any other
        return false;
    }

    static synchronized void removeDatabase(Database database) {

        int      count;
        Thread[] threads;
        String   name;
        Integer  useCount;

        if (database == null) {
            return;
        }

        name = getAbsoluteDatabaseName(database.getName());

        if (database != dbMap.get(name)) {
            return;
        }

        useCount = (Integer) dbUseCountMap.get(database);

        if (useCount.intValue() == 1) {
            Thread t = (Thread) dbThreadMap.get(name);

            t.interrupt();

            try {
                t.join();
            } catch (Exception e) {}

            dbUseCountMap.remove(database);
            dbMap.remove(name);

            if (!database.isShutdown()) {
                try {
                    database.sessionManager.getSysSession().execute(
                        "shutdown");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static void testAll() throws Exception {

        String name    = "/test";
        String sep = "----------------------------------------------------";
        String absName = getAbsoluteDatabaseName(name);

        System.out.println(sep);
        System.out.print("getDatabase() test; should be ");
        System.out.print("'org.hsqldb.Database@xxx': ");

        Database database = getDatabase("/test", null);

        System.out.println(database);
        System.out.println(sep);
        System.out.println("toDebugString() test.");
        System.out.println(
            "Next lines should be list of hsqldb thread groups");
        System.out.println(", showing place holder thread for " + absName
                           + ":");
        System.out.println(sep);
        System.out.println(toDebugString());
        System.out.println(sep);
        System.out.print("listDatabases() test; should be ");
        System.out.print("'HsqlArrayList : size=1 [" + absName + "]': ");
        System.out.println(listDatabases());
        System.out.println(sep);
        System.out.print("findDatabase() test; should be '" + database
                         + "': ");
        System.out.println(findDatabase("/test"));
        System.out.println(sep);
        System.out.print("isOpenDatabase() test; should be 'true': ");
        System.out.println(isOpenDatabase("/test"));
        System.out.println(sep);
        System.out.println("removeDatabase() test;");
        System.out.println("Next lines should a message that thread is ");
        System.out.println("exiting, followed by thread group listing NOT ");
        System.out.println("showing place holder thread for " + absName);
        System.out.println(sep);
        removeDatabase(database);
        System.out.println(toDebugString());
        System.out.println(sep);
        System.out.print("isOpenDatabase() test; should be 'false': ");
        System.out.println(isOpenDatabase("/test"));
        System.out.println(sep);
        System.out.println("database should now be shut down");
        System.runFinalization();
        System.out.println(sep);
        System.out.println("DatabaseReference.newReference() test");

        DatabaseReference ref = DatabaseReference.newReference(name);

        System.out.println(ref);
        System.out.println(sep);
        System.out.println("toDebugString() test.");
        System.out.println(
            "Next lines should be list of hsqldb thread groups");
        System.out.println(", showing place holder thread for " + absName
                           + ":");
        System.out.println(sep);
        System.out.println(toDebugString());
        System.out.println(sep);
        System.out.println("DatabaseReference.clear() test");
        ref.clear();
        System.out.println(ref);
        System.out.println(sep);
        System.out.println("toDebugString() test.");
        System.out.println(
            "Next lines should be list of hsqldb thread groups");
        System.out.println(" NOT showing place holder thread for " + absName
                           + ":");
        System.out.println(sep);
        System.out.println("database should now be shut down");
        System.runFinalization();
        System.out.println(sep);
    }

    public static void main(String[] args) throws Exception {
        testAll();
    }

    private static synchronized Class classForName(String name)
    throws Exception {

        ClassLoader cl;
        Class       c;
        Method      m;
        Thread      t;

        cl = null;

        try {
            t  = Thread.currentThread();
            c  = t.getClass();
            m  = c.getMethod("getContextClassLoader", new Class[]{});
            cl = (ClassLoader) m.invoke(t, new Object[]{});
        } catch (Exception e) {
            try {
                c  = Class.forName("org.hsqldb.SocketFactory");
                cl = c.getClassLoader();
            } catch (Exception e2) {}
        }

        return (cl == null) ? Class.forName(name)
                            : Class.forName(name, true, cl);
    }

    static class PlaceHolderThread extends Thread {

        PlaceHolderThread(ThreadGroup tg, String name) {
            super(tg, name);
        }

        public void run() {

            try {
                while (true) {
                    this.sleep(Integer.MAX_VALUE);
                }
            } catch (Exception e) {}

            System.out.println(this + " exiting.");
        }

        public String toString() {

            return "PlaceHolderThread[database=\"" + this.getName() + "\","
                   + this.getPriority() + ","
                   + this.getThreadGroup().getName() + "]";
        }
    }

    public static class DatabaseReference {

        Database database;

        private DatabaseReference(String name) throws SQLException {
            database = HsqlRuntime.getDatabase(name, this);
        }

        public static synchronized DatabaseReference newReference(String name)
        throws SQLException {
            return new DatabaseReference(name);
        }

        public synchronized void clear() {

            if (database != null) {
                HsqlRuntime.removeDatabase(database);

                database = null;

                System.runFinalization();
            }
        }

        public String toString() {
            return super.toString() + "[" + database + "]";
        }
    }
}
