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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.hsqldb.lib.HsqlTimer;

/**
 * Provides the interface with the HSQLDB environment in which an application
 * is running. The current HsqlRuntime can be obtained through the public,
 * static getHsqlRuntime() method. An application cannot create its own
 * instance of this class.
 *
 * @author boucher@users.sourceforge.net
 * @version 1.7.2
 * @since HSQLDB 1.7.2
 */
public final class HsqlRuntime {

//------------------------- Static fields/initializers -------------------------

    /** Temp attribute for debugging until released */
    private static final boolean TRACE = false;

    /** Root HsqlRuntime thread group name */
    private static final String rtgName = "hsqldb-system";

    /** Database placeholder thread group name */
    private static final String dbtgName = "hsqldb-databases";

    /** Server thread group name */
    private static final String stgName = "hsqldb-servers";

    /** Web server thread group name */
    private static final String    wstgName   = "hsqldb-webservers";
    private static final Hashtable dbMutexMap = new Hashtable();

    /** Reference to the java.lang.Runtime instance for this JVM */
    private static final Runtime javaRuntime = Runtime.getRuntime();
    private static Database      memoryDatabase;

    /** The one and only instance of HsqlRuntime in this class loader context */
    private static HsqlRuntime instance;

    /** true if instance has been created */
    private static boolean initialized;

    /** The HsqlRuntime root thread group */
    private static ThreadGroup rtg;

    /** The HsqlRuntime database placeholder thread group */
    private static ThreadGroup dbThreadGroup;

    /** The HsqlRuntime server thread group */
    private static ThreadGroup serverThreadGroup;

    /** The HsqlRuntime web server thread group */
    private static ThreadGroup webServerThreadGroup;

    /** MAP: database path (String) => DatabasePlaceholderThread instance */
    private static Hashtable dbThreadMap;

    /** MAP: database path (String) => Database instance */
    private static Hashtable dbMap;

// TODO: why not just make useCount a primitive attribute of Database,
// eliminating need for one Hashtable, lookups, Integer object creations
// etc.?

    /** MAP: database instance => (Integer) database use count instance */
    private static Hashtable dbUseCountMap;

    /** The HsqlRuntime timed task scheduler. */
    private static HsqlTimer timer;

    /** The java runtime addShutdownHook method */
    private static Method addShutdownHook;

    /** The java runtime availableProcessors method */
    private static Method availableProcessors;

    /** The java runtime maxMemory method */
    private static Method maxMemory;

    /** The java runtime removeShutdownHook method */
    private static Method removeShutdownHook;

//--------------------------------- Constructors -------------------------------

    /**
     * External Construction disabled
     */
    private HsqlRuntime() {
        init();
    }

//----------------------------------- Methods ----------------------------------
// --------------------------- Public Static Methods ---------------------------

    /**
     * Retieves null if path is null, the the absolute path of the database
     * indicated by path, or the special path '.' indicating the memory database
     * instance, if '.'.equals(path.trim()).
     *
     * @param path proposed database path
     * @return corresponding absolute database path
     */
    public static String absoluteDatabasePath(String path) {

        if (path == null) {
            return null;
        }

        path = path.trim();

        return ".".equals(path) ? "."
                                : (new File(path)).getAbsolutePath();
    }

    /**
     * Retrieves the Class object associated with the class or interface with
     * the given string name. In order of preference, invoking this method
     * is equivalent to one of:
     *
     * <ol>
     * <li> Thread.currentThread().getContextClassLoader().loadClass(name)
     * <li> HsqlRuntime.class.getClassLoader().loadClass(name)
     * <li> Class.forName(name)
     * </ol>
     *
     * Because HSQLDB aims at JDK 1.1 compliance, Thread.getContextClassLoader()
     * may not be available at runtime, hence a reflective invocation is used.
     * Also, even if it is available, a SecurityException may be thrown.  This
     * is also the case for point 2, above.  Thus, the full sequence is
     * provided, eventually resulting the the default behaviour of a
     * standard Class.forName(name) call, if no more prefenced way is possible.
     *
     * @param name the fully qualified name of the desired class
     * @return the Class object for the class with the specified name
     * @throws LinkageError - if the linkage fails
     * @throws ExceptionInInitializerError - if the initialization provoked
     *      by this method fails
     * @throws ClassNotFoundException - if the class cannot be located
     */
    public static Class classForName(String name)
    throws LinkageError, ExceptionInInitializerError, ClassNotFoundException {

        ClassLoader cl;

        cl = null;

        try {
            cl = getContextClassLoader(Thread.currentThread());
        } catch (Exception e) {
            try {
                cl = HsqlRuntime.class.getClassLoader();
            } catch (Exception e2) {}
        }

        return (cl == null) ? Class.forName(name)
                            : cl.loadClass(name);
    }

    /**
     * Retrieves, if possible, the Class object associated with the class or
     * interface with the given string name, using the class loader context
     * of the specified context object, ctx, as determined by a call to
     * getContextClassLoader(ctx).  This is a convenience method equivalent
     * to getContextClassLoader(ctx).loadClass(name). <p>
     *
     * @param name the fully qualified name of the desired class
     * @param ctx the Object from which to retrieve the ClassLoader
     * @throws Exception if getContextClassLoader(ctx) fails or
     *      loadClass(name) fails
     * @return the Class object for the class with the specified name
     * @see #getContextClassLoader(Object)
     */
    public static Class classForName(String name,
                                     Object ctx) throws Exception {
        return getContextClassLoader(ctx).loadClass(name);
    }

    /**
     * Retrieves, if  possible, the class loader associated with the
     * specified context object, ctx.  This is a convenience method
     * equivalent to: <p>
     *
     * ctx.getClass()
     *      .getMethod("getContextClassLoader", new Class[]{})
     *          .invoke(ctx, new Object[]{})
     *
     * @param ctx the Object providing context in which to retrieve
     *      a ClassLoader
     * @throws Exception if an exception occurs during the retrieval
     * @return the ClassLoader associated with the specified context
     *      Object, ctx
     */
    public static ClassLoader getContextClassLoader(Object ctx)
    throws Exception {

        Class  c;
        Method m;

        if (ctx == null) {
            return null;
        }

        c = ctx.getClass();
        m = c.getMethod("getContextClassLoader", new Class[]{});

        return (ClassLoader) m.invoke(ctx, new Object[]{});
    }

    /**
     * Retrieves the HSQLDB runtime object associated with the current Java
     * application. Most of the methods of the HsqlRuntime class are instance
     * methods and must be invoked with respect to the current HSQLDB runtime
     * object.
     *
     * @return the HsqlRuntime object associated with the current Java
     *      application.
     * @throws IllegalStateException If an unexpected internal state is
     *      encountered
     */
    public static HsqlRuntime getHsqlRuntime() throws IllegalStateException {

        synchronized (HsqlRuntime.class) {
            if (instance == null) {

                // There was a previous init and it failed.
                // Should never happen except under recoverable
                // Error rather than Exception conditions.
                // TODO: maybe try to catch anything (Throwable).
                if (initialized) {
                    throw new IllegalStateException();
                }

                initialized = true;
                instance    = new HsqlRuntime();
            }

            return instance;
        }
    }

    /**
     * Retrieves whether the stipulated path is the special HSQLDB memory
     * database path.
     *
     * @return true iff path is memory database path
     */
    public static boolean isMemoryDatabasePath(String path) {
        return path != null && ".".equals(path.trim());
    }

//-------------------------- Public Instance Methods ---------------------------
// TODO: maybe offer shutdown hooks for database instances if available
//       in runtime env.

    /**
     * Registers a new virtual-machine shutdown hook, iff the underlying java
     * runtime permits.
     *
     * @param hook An initialized but unstarted Thread object
     * @throws SecurityException If a security manager is present and it
     *      denies RuntimePermission("shutdownHooks")
     * @throws IllegalArgumentException If the specified hook has already
     *      been registered, or if it can be determined that the hook is
     *      already running or has already been run
     * @throws IllegalStateException If the virtual machine is already in
     *      the process of shutting down
     * @throws RuntimeException If any error state is encountered that is not
     *      covered by the other thrown exceptions.  The RuntimeException
     *      thrown wraps the underlying Throwable.
     * @return false if the underlying java runtime does not support
     *      shutdown hooks, else true if the hook was added
     *
     * @see #supportsShutdownHooks()
     * @see java.lang.Runtime#addShutdownHook
     */
    public boolean addShutdownHook(Thread hook)
    throws SecurityException, IllegalArgumentException,
           IllegalStateException, RuntimeException {

        if (addShutdownHook == null) {
            return false;
        }

        try {
            addShutdownHook.invoke(javaRuntime, new Object[]{ hook });
        } catch (Throwable t) {
            if (t instanceof InvocationTargetException) {
                t = ((InvocationTargetException) t).getTargetException();

                if (t instanceof SecurityException) {
                    throw (SecurityException) t;
                } else if (t instanceof IllegalArgumentException) {
                    throw (IllegalArgumentException) t;
                } else if (t instanceof IllegalStateException) {
                    throw (IllegalStateException) t;
                } else {
                    throw new RuntimeException(t.toString());
                }
            } else {
                throw new RuntimeException(t.toString());
            }
        }

        return true;
    }

    /**
     * Retrieves the difference between maxMemory() and usedMemory()
     *
     * @return the difference between maxMemory() and usedMemory()
     * @see #maxMemory()
     * @see #usedMemory()
     */
    public long availableMemory() {
        return maxMemory() - usedMemory();
    }

    /**
     * Retrieves the number of processors available to the Java virtual machine.
     * This value may change during a particular invocation of the virtual
     * machine. Applications that are sensitive to the number of available
     * processors should therefore occasionally poll this property and adjust
     * their resource usage appropriately.  If the Java runtime does not
     * support determining this value, one processor is assumed and reported.
     *
     * @return the maximum number of processors available to the virtual
     *         machine; never smaller than one
     *
     * @see java.lang.Runtime#availableProcessors()
     * @see #supportsAvailableProcessors()
     */
    public int availableProcessors() {

        if (availableProcessors == null) {
            return 1;
        }

        try {
            return ((Integer) availableProcessors.invoke(javaRuntime,
                    new Object[0])).intValue();
        } catch (Exception e) {
            if (Trace.TRACE) {
                Trace.trace(e.toString());
            }
        }

        return 1;
    }

    /**
     * Performs conditional garbage collection, predicated upon how many Record
     * objects have been created since the last gc() met the condition.  The
     * condition is: <p>
     *
     * Record.gcFrequency > 0 && Record.memoryRecords > Record.gcFrequency
     */
    public void gc() {

        synchronized (Record.class) {
            if ((Record.gcFrequency > 0)
                    && (Record.memoryRecords > Record.gcFrequency)) {
                if (Trace.TRACE) {
                    Trace.trace("gc at " + Record.memoryRecords);
                }

                Record.memoryRecords = 0;

                System.gc();
            }
        }
    }

    /**
     * Retrieves the number of HsqlRuntime registrations recorded in this JVM
     * against the database with the specified path. Currently, if the database
     * is registered in an incompatible class loader context, the value returned
     * is '1', regardless of the actual number of registrations recorded in
     * the incompatible context.  This case will be reported more accurately
     * as refinements are made to DatabasePlaceHolderThread.
     *
     * @param path Database path
     * @return The number of registrations against the specified database
     * @throws IllegalStateException If an unexpected internal error occurs
     */
    public int getDatabaseRegistrationCount(String path)
    throws IllegalStateException {

        Integer useCount;
        Object  database;

        path = absoluteDatabasePath(path);

        synchronized (findOrCreateDatabaseMutexImpl(path)) {
            database = dbMap.get(path);

            if (database == null) {

                // For the time being, from our perspecitive,
                // if an incompatible class loader context has this
                // database registered, then the database
                // use count is "1" (we can't know yet what its real use
                // count is.  This will become more accurate as
                // DatabasePlaceHolderThread is refined.
                return isRegisteredDatabase(path) ? 1
                                                  : 0;
            }

            useCount = (Integer) dbUseCountMap.get(database);

            if (useCount == null) {

                // this should never be the case, but is
                // left here for the time being, until all work
                // is throughly tested
                throw new IllegalStateException();
            }

            return useCount.intValue();
        }
    }

    /**
     * Retieves the number of Record constructions (since the last conditional
     * HSQLDB garbage collection) that will cause a new HSQLDB garbage
     * collection to occur.
     *
     * @return the current threshold value.  Zero implies never.
     */
    public int getRecordCreateCountGcThreshold() {

        synchronized (Record.class) {
            return Record.gcFrequency;
        }
    }

    /**
     * Retrieves whether the database with the specified path is currently
     * registered with any HsqlRuntime instance in this JVM, regardless of
     * class loader context, subject to lookup across context security
     * restrictions, based on ThreadGroup access rights.
     *
     * @param path the path of the desired database
     *
     * @return true iff the indicated database is registered with some
     *      HsqlRuntime instance in this JVM
     */
    public boolean isRegisteredDatabase(String path) {

        Vector list;
        int    size;

        path = absoluteDatabasePath(path);

        // We wait till any in-process free/new against this path is done,
        // and we lock out any following operations against this
        // path until we are done
        synchronized (findOrCreateDatabaseMutexImpl(path)) {
            if (dbMap.containsKey(path)) {

                // open in *this* class loader context
                return true;
            }

            list = listRegisteredDatabasePaths();
            size = list.size();

            for (int i = 0; i < size; i++) {
                if (path.equals(list.get(i))) {
                    return true;
                }
            }

            // TODO?:
            // We can also check using lock file here if we want,
            // in order to make a determination even across process
            // boundaries.
            // SIMPLIFIED EXAMPLE:
            // LockFile lf = new LockFile(path + ".lck")
            // boolean locked = false;
            // try {
            //     lf.tryLock();
            //     locked = false
            // } catch (Exception e) {
            //     locked = true;
            // }
            // try {
            //      lf.tryRelease();
            // } catch (Exception e) {}
            //
            // return locked;
            // Not registered either in this class loader context
            // or any other accessible one in this JVM (assuming we
            // have unrestricted access to JVM root thread group).
            return false;
        }
    }

    /**
     * Retieves a Vector of String objects describing all database
     * instances registered in this JVM, regardless of class loader
     * context, subject to lookup across context security restrictions,
     * based on ThreadGroup access rights.  The String objects describe
     * the DatabasePlaceHolderThread, Database instance and its state.
     *
     * @return a Vector of String objects describing all database
     *      instances registered in this JVM
     */
    public Vector listRegisteredDatabaseDescriptors() {

        int      count;
        Thread[] threads;
        Vector   list;

        // HsqlRuntime constructor is properly synchronized, and this
        // attribute is initialized there, so this test is thread-safe
        if (dbThreadGroup == null) {
            return new Vector();
        }

        synchronized (dbThreadGroup) {
            count   = dbThreadGroup.activeCount();
            threads = new Thread[count];
            list    = new Vector(count);

            try {
                dbThreadGroup.enumerate(threads);
            } catch (SecurityException e) {}
        }

        for (int i = 0; i < count; i++) {
            if (threads[i] != null) {
                list.add(threads[i].toString());
            }
        }

        return list;
    }

    /**
     * Retieves a Vector of String objects representing the paths of all
     * database instances registered in this JVM, regardless of class loader
     * context, subject to lookup across context security restrictions,
     * based on ThreadGroup access rights.
     *
     * @return a Vector of String objects representing the paths of all
     *      database instances registered in this JVM
     */
    public Vector listRegisteredDatabasePaths() {

        int      count;
        Thread[] threads;
        Vector   list;

        // HsqlRuntime constructor is properly synchronized, and this
        // attribute is initialized there, so this test is thread-safe
        if (dbThreadGroup == null) {
            return new Vector();
        }

        synchronized (dbThreadGroup) {
            count   = dbThreadGroup.activeCount();
            threads = new Thread[count];
            list    = new Vector(count);

            try {
                dbThreadGroup.enumerate(threads);
            } catch (SecurityException e) {
                if (Trace.TRACE) {
                    Trace.trace(e.toString());
                }
            }
        }

        for (int i = 0; i < count; i++) {
            if (threads[i] != null) {
                list.add(threads[i].getName());
            }
        }

        return list;
    }

    /**
     * Retieves a Vector of String objects describing all running HSQL
     * protocol server instances registered in this JVM, regardless of
     * class loader context, subject to lookup across context security
     * restrictions, based on ThreadGroup access rights.  Each String
     * object describes a ServerThread, Server and ServerSocket.
     *
     * @return a Vector of String objects describing all running HSQL
     *      protocol server instances registered in this JVM
     */
    public Vector listRegisteredServerDescriptors() {

        int      count;
        Thread[] threads;
        Vector   list;

        // HsqlRuntime constructor is properly synchronized, and this
        // attribute is initialized there, so this test is thread-safe
        if (serverThreadGroup == null) {
            return new Vector();
        }

        synchronized (serverThreadGroup) {
            count   = serverThreadGroup.activeCount();
            threads = new Thread[count];
            list    = new Vector(count);

            try {
                serverThreadGroup.enumerate(threads);
            } catch (SecurityException e) {}
        }

        for (int i = 0; i < count; i++) {
            if (threads[i] != null) {
                list.add(threads[i].toString());
            }
        }

        return list;
    }

    /**
     * Retieves a Vector of String objects describing all running HTTP
     * protocol server instances registered in this JVM, regardless of
     * class loader context. Each String object describes a ServerThread,
     * Server and its ServerSocket.
     *
     * @return a Vector of String objects describing all running HSQL
     *      protocol server instances registered in this JVM
     */
    public Vector listRegisteredWebServerDescriptors() {

        int      count;
        Thread[] threads;
        Vector   nameList;

        // HsqlRuntime constructor is properly synchronized, and this
        // attribute is initialized there, so this test is thread-safe
        if (webServerThreadGroup == null) {
            return new Vector();
        }

        synchronized (webServerThreadGroup) {
            count    = webServerThreadGroup.activeCount();
            threads  = new Thread[count];
            nameList = new Vector(count);

            try {
                webServerThreadGroup.enumerate(threads);
            } catch (SecurityException e) {}
        }

        for (int i = 0; i < count; i++) {
            if (threads[i] != null) {
                nameList.add(threads[i].toString());
            }
        }

        return nameList;
    }

    /**
     * Retrieves the maximum amount of memory that the Java virtual machine
     * will attempt to use. If there is no inherent limit or the limit cannot
     * be determined because the Java Runtime does not suppport the operation,
     * then the value Long.MAX_VALUE is returned.
     *
     * @return the maximum amount of memory that the virtual machine will
     *      attempt to use, measured in bytes.
     */
    public long maxMemory() {

        if (maxMemory != null) {
            try {
                return ((Long) maxMemory.invoke(javaRuntime,
                                                new Object[0])).longValue();
            } catch (Exception e) {
                if (Trace.TRACE) {
                    Trace.trace(e.toString());
                }
            }
        }

        // - if we get here, then not supported by JVM
        // - assume and report the max possible
        // - the worst case is client will then also assume max and
        //   maybe get OOME at some point because of this (decide not
        //   to gc or decide to allocate more than really available,
        //   etc, which is undecidable pre JDK 1.4 anyway, so this is OK.
        return Long.MAX_VALUE;
    }

    /**
     * Retrieves the number of Record objects constructed since the last
     * conditional HsqlRuntime garbage collection.
     *
     * @return the number of Record objects constructed since the last
     *      conditional HsqlRumtime garbage collection.
     */
    public int recordCreateCount() {

        synchronized (Record.class) {
            return Record.memoryRecords;
        }
    }

    /**
     * De-registers a previously-registered virtual-machine shutdown hook.
     *
     * @param hook the hook to remove
     * @throws SecurityException If a security manager is present and it denies
     *      RuntimePermission("shutdownHooks")
     * @throws IllegalStateException If the virtual machine is already in the
     *      process of shutting down
     * @return true iff the hook was removed.  If any other exception occurs
     *      beside those listed for this method, false is returned.  If tracing
     *      is turned on, then exceptions not thrown are logged to the trace
     *      stream.
     */
    public boolean removeShutdownHook(Thread hook)
    throws SecurityException, IllegalStateException {

        if (removeShutdownHook == null) {
            return false;
        }

        try {
            removeShutdownHook.invoke(javaRuntime, new Object[]{ hook });
        } catch (Throwable t) {
            if (t instanceof InvocationTargetException) {
                t = ((InvocationTargetException) t).getTargetException();

                if (t instanceof SecurityException) {
                    throw (SecurityException) t;
                } else if (t instanceof IllegalStateException) {
                    throw (IllegalStateException) t;
                }
            }

            if (Trace.TRACE) {
                Trace.trace(t.toString());
            }

            return false;
        }

        return true;
    }

    /**
     * Enable or disable finalization on exit; doing so specifies that the
     * finalizers of all objects that have finalizers that have not yet been
     * automatically invoked are to be run before the Java runtime exits.
     * By default, finalization on exit is disabled. <p>
     *
     * This method wraps the java Runtime method of the same name, and
     * never throws an exception, instead returning the supplied value
     * to indicate success or its negation to indicate failure, possible
     * due to a SecurityException.
     *
     * @deprectated This method is inherently unsafe. It may result in
     *      finalizers being called on live objects while other threads
     *      are concurrently manipulating those objects, resulting in
     *      erratic behavior or deadlock.
     * @param b true to enable finalization on exit, false to disable
     * @return the value of b if no SecurityException is thrown by the
     *      Java Runtime, else !b
     *
     * @see java.lang.Runtime#runFinalizersOnExit()
     */
    public boolean runFinalizersOnExit(boolean b) {

        try {
            javaRuntime.runFinalizersOnExit(b);

            return b;
        } catch (SecurityException se) {
            if (Trace.TRACE) {
                Trace.trace(se.toString());
            }

            return !b;
        }
    }

    /**
     * Sets the number of Record object constructions that trigger
     * a conditional HsqlRuntime garbage collection.  If zero, then
     * conditional garbage collection is never performed.
     *
     * @param n the new threshold value
     */
    public void setRecordCreateCountGcThreshold(int n) {

        synchronized (Record.class) {
            Record.gcFrequency = n;
        }
    }

// TODO:  enumerate threadgoups and custom print them instead of relying on
// substituting System.out and doing an rtg.list();

    /**
     * Retrieves a string representation of the state of this HsqlRuntime.
     *
     * @return a string representation of the state of this HsqlRuntime
     */
    public String stateDescriptor() {

        ByteArrayOutputStream baos;
        PrintStream           ps;
        PrintStream           tempps;
        String                stateDescriptor;

        if (rtg == null) {
            return null;
        }

        baos = new ByteArrayOutputStream();
        ps   = new PrintStream(baos);

        synchronized (System.out) {
            tempps = System.out;

            try {
                System.setOut(ps);
            } catch (SecurityException e) {
                if (Trace.TRACE) {
                    Trace.trace(e.toString());
                }
            }

            if (rtg != null) {
                rtg.list();
            }

            try {
                System.setOut(tempps);
            } catch (SecurityException e) {
                if (Trace.TRACE) {
                    Trace.trace(e.toString());
                }
            }
        }

        ps.flush();

        stateDescriptor = baos.toString();

        return stateDescriptor.length() == 0 ? rtg.toString()
                                             : stateDescriptor;
    }

    /**
     * Retrieves whether the Java Runtime supports the
     * availableProcessors() method.
     *
     * @return true if the method is supported
     */
    public boolean supportsAvailableProcessors() {
        return availableProcessors != null;
    }

    /**
     * Retrieves whether the Java Runtime supports the
     * maxMemory() method.
     *
     * @return true if the method is supported
     */
    public boolean supportsMaxMemory() {
        return maxMemory != null;
    }

    /**
     * Retrieves whether the Java Runtime supports the
     * addShutdownHook() and removeShutdownHook() methods.
     *
     * @return true if the methods are supported
     */
    public boolean supportsShutdownHooks() {
        return addShutdownHook != null && removeShutdownHook != null;
    }

    /**
     * Retrieves totalMemory() - freeMemory()
     *
     * @return totalMemory() - freeMemory()
     */
    public long usedMemory() {
        return javaRuntime.totalMemory() - javaRuntime.freeMemory();
    }

//------------------------ Package Instance Methods ----------------------------

    /**
     * Retrieves an object against which synchronization will guarantee
     * exclusive HsqlRuntime access regarding the database with the
     * specified path.  The path argument is absolutized before use.
     *
     * @param the stipulated database path, possibly in relative form
     * @return the distinct mutex object for the specified database path
     */
    Object findOrCreateDatabaseMutex(String path) {
        return findOrCreateDatabaseMutexImpl(absoluteDatabasePath(path));
    }

    /**
     * Retrieves a Database instance given a database path.  If a
     * corresponding Database instance is already registered, it is
     * returned, else a new instance is constructed, registered and returned,
     * if possible.
     *
     * @param path Database path
     * @param requestor object requesting instance (optional - may be null)
     * @throws SQLException if a database access error occurs
     * @return a database instance corresponding to the specified
     *      path.
     */
    Database getDatabase(String path, Object requestor) throws SQLException {

        Database                  database;
        DatabasePlaceHolderThread placeholder;
        Integer                   useCount;
        int                       threadCount;
        Thread[]                  threads;
        Thread                    thread;

        path = absoluteDatabasePath(path);

//        trace("getDatabase(): entered with path: " + path);
        synchronized (findOrCreateDatabaseMutexImpl(path)) {

//            trace("getDatabase(): entered database mutex sync block");
            if (isMemoryDatabasePath(path)) {
                return getOrCreateMemoryDatabase();
            }

//            trace("getDatabase(): dbMap is: " + dbMap);
            database = (Database) dbMap.get(path);

//            trace("getDatabase(): dbMap.get(path) is: " + database);
            if (database != null) {

//                trace("getDatabase(): got non-null database");
//                trace("getDatabase(): incrementing use count");
                useCount = (Integer) dbUseCountMap.get(database);

//                trace("getDatabase(): previous use count: " + useCount);
                // debug - should never happen
                if (useCount == null) {
                    throw new IllegalStateException("null useCount!!!");
                }

                useCount = new Integer(useCount.intValue() + 1);

//                trace("getDatabase(): updated use count: " + useCount);
                dbUseCountMap.put(database, useCount);

//                trace("getDatabase(): returning database from dbMap");
                return database;
            }

//            trace("getDatabase(): database not found in dbMap");
            // If dbThreadGroup is null at this point, we have
            // a restrictive security policy and it is unlikely
            // that a file-based database instance can be created
            // without throwing a SecurityException
            //
            // However, it is also possible that the security policy
            // restricts ThreadGroup access in an incompatible way,
            // but not access to the specified path.  Hence, for an
            // HSQLDB release verion,  we might simply forgo the
            // ThreadGroup-based lookup for cross class loader context
            // registration detection, just creating, mapping
            // and returning a Database instance right here, like:
            //
            // database = new Database(path);
            // dbMap.put(path,database);
            // dbUseCountMap.put(database, new Integer(1));
            // return database;
//            trace("getDatabase(): scanning dbThreadGroup...");
            // Under a non-restrictive security policy where creation
            // of the dbThreadGroup is possible, this will block threads
            // in incompatible class loader contexts until the creation
            // and registration is done.  OTOH, this sync attempt will
            // throw an NPE if the sandbox of the calling application
            // disallowed creation of the root HsqlRuntime thread group at
            // init time and thus its subordinates, such as dbThreadGroup
            synchronized (dbThreadGroup) {

//                trace("getDatabase(): inside dbThreadGroup sync block");
                threadCount = dbThreadGroup.activeCount();

//                trace("getDatabase(): dbThreadGroup.activeCount(): " + threadCount);
                threads = new Thread[threadCount];

                try {
                    dbThreadGroup.enumerate(threads);
                } catch (SecurityException e) {}

//                trace("getDatabase(): dbThreadGroup enumerated");
                for (int i = 0; i < threadCount; i++) {
                    thread = threads[i];

//                    trace("getDatabase(): considering enumerated thread: " + thread);
                    if ((thread != null) && path.equals(thread.getName())) {

//                        trace("getDatabase(): unreachable class loader context.");
                        throw Trace.error(
                            Trace.DATABASE_ALREADY_IN_USE,
                            "incompatible class loader context for: " + path);
                    }
                }

//                trace("getDatabase(): path is not registered: " + path);
//                trace("getDatabase(): constructing new Database instance for path");
                database = new Database(path);

//                trace("getDatabase(): new Database constructed: " + database);
//                trace("getDatabase(): adding database to dbMap");
                dbMap.put(path, database);

//                trace("getDatabase(): dbMap is now: " + dbMap);
//                trace("getDatabase(): constructing placeholder thread...");
                placeholder = new DatabasePlaceHolderThread(dbThreadGroup,
                        database);

//                trace("getDatabase(): placeholder is : " + placeholder);
//                trace("getDatabase(): dbThreadMap was: " + dbThreadMap);
                if (dbThreadMap.containsKey(path)) {
                    throw new IllegalStateException();
                }

//                trace("getDatabase(): adding placeholder to dbThreadMap");
                dbThreadMap.put(path, placeholder);

//                trace("getDatabase(): dbThreadMap is now: " + dbThreadMap);
//                trace("getDatabase(): starting placeholder thread");
                placeholder.start();

//                trace("getDatabase(): dbUseCountMap was: " + dbUseCountMap);
//                trace("getDatabase(): putting use count 1 for : " + database);
                dbUseCountMap.put(database, new Integer(1));

//                trace("getDatabase(): dbUseCountMap is: " + dbUseCountMap);
//                trace("getDatabase(): returning database: " + database);
                return database;
            }
        }
    }

    /**
     * Retrieves the HsqlRuntime ThreadGroup in which are run the background
     * threads of server instances providing the specified network protocol.
     *
     * @param protocol protocol code, from ServerConstants
     *
     * @return The ThreadGroup corresponding to the specified protocol
     */
    ThreadGroup getServerThreadGroup(int protocol) {

        switch (protocol) {

            case ServerConstants.SC_PROTOCOL_HTTP :
                return webServerThreadGroup;

            case ServerConstants.SC_PROTOCOL_HSQL :
            default :
                return serverThreadGroup;
        }
    }

    /**
     * Retrieves the HsqlRuntime timer responsible for periodically
     * scheduling background Log.LogSyncRunner and LockFile.HearbeatRunner
     * tasks on behalf of open, file-based database instances.
     *
     * @return The HsqlRuntime timer instance
     */
    HsqlTimer getTimer() {
        return timer;
    }

    /**
     * Called to indicate to HsqlRuntime that an object previously holding
     * a reference to the specified database instance no longer requires
     * its use. A caller should NEVER release its reference more
     * than once and should always nullify its reference in the same step as
     * calling this method.
     *
     * @param database The instance to release
     * @param requestor The object requesting the release
     *      (optional - may be null)
     */
    void releaseDatabase(Database database, Object requestor) {

        int      count;
        Thread[] threads;
        Thread   placeholder;
        String   path;
        Integer  useCount;
        Session  sysSession;

//        trace("releaseDatabase(): entered with " + database);
        if (database == null) {

//            trace("releaseDatabase(): datbase is null; exiting immediately");
            // should never happen
            return;
        }

        path = absoluteDatabasePath(database.getName());

        synchronized (findOrCreateDatabaseMutexImpl(path)) {
            if (database.isShutdown()) {

                // Presumably, the database has already removed
                // itself from HsqlRuntime registration.
                // Given that a database is only shutdown by processing
                // the "shutdown" SQL command and that all SQL commands
                // are currently processed in a serial fashion by virtue of
                // synchronization on the concerned Database instance, this
                // case should never actually occur, but is included so that
                // enforcement is centralized here
//                trace("releaseDatabase(): database is shutdown; exiting immediately");
                return;
            }

            if (database != dbMap.get(path)) {

                // Again, this should never happen under the new scheme.
//                trace("releaseDatabase(): database not mapped; exiting immediately");
                return;
            }

            useCount = (Integer) dbUseCountMap.get(database);

            if (useCount == null) {

                // This should _definitely_ never happen
                // We'd get an NPE below anyway
//                trace("releaseDatabase(): useCount == null; IllegalStateException next");
                throw new IllegalStateException("null use count for: "
                                                + path);
            }

//            trace("releaseDatabase(): useCount: " + useCount);
            if (useCount.intValue() > 1) {

//                trace("releaseDatabase(): useCount > 1; decrement and exit immediately");
                useCount = new Integer(useCount.intValue() - 1);

                dbUseCountMap.put(database, useCount);

                return;
            }

//            trace("releaseDatabase(): useCount == 0; attempt to shutdown database");
            try {
                database.sessionManager.getSysSession().sqlExecuteDirect(
                    "shutdown");
            } catch (Exception e) {
                if (Trace.TRACE) {
                    Trace.trace(e.toString());
                }
            }

//            trace("releaseDatabase(): database shutdown; exiting");
        }
    }

    /**
     * Called to indicate to HsqlRuntime that the specified database instance
     * has shutdown and thus needs to be removed immediately from the
     * database registry.
     *
     * <b>!!!IMPORTANT!!!</b> <p>
     *
     * This method is not synchronized on any object and must only be called
     * from Database.close() inside the block synchronized on
     * findOrCreateDatabaseMutext, using the path of that database.
     *
     * @param database The instance to remove
     */
    void removeDatabase(Database database) {

        String path;
        Thread placeholder;

//        trace("removeDatabase(): entered with " + database);
        path = absoluteDatabasePath(database.getName());

        // applet use
        if (isMemoryDatabasePath(path)) {
            removeMemoryDatabase();
        }

        if (database != dbMap.get(path)) {

            // This should not happen happen under the new scheme because
            // Database.finalize() is now set up properly (does not try to
            // close the database unless it is online).  This test is left
            // here as a simple precaution anyway, as it is a breach of
            // repository consistency to remove an instance with the same path
            // if it is not the identical instance.
//            trace("removeDatabase(): database not mapped; exiting immediately");
            return;
        }

        dbUseCountMap.remove(database);
        dbMap.remove(path);

//        trace("removeDatabase(): getting placeholder for: " + path);
//        trace("removeDatabase(): dbThreadMap is: " + dbThreadMap);
        placeholder = (Thread) dbThreadMap.remove(path);

//        trace("removeDatabase(): placeholder is: " + placeholder);
        if (placeholder != null) {

//            trace("removeDatabase(): placeholder is non-null");
//            trace("removeDatabase(): interrupting and joining placeholder");
            placeholder.interrupt();

            try {
                placeholder.join();
            } catch (Exception e) {}

//            trace("removeDatabase(): placeholder joined");
        }

        if (dbMap.size() == 0) {

//            trace("removeDatabase(): dbMap.size() == 0; shutting down timer");
            // Exit timer background thread, allowing VM to exit if there are
            // no other running threads.  Timer thread should not be a daemon
            // as it's better for it to continue process database tasks until
            // the queue is empty than to stop as soon as there are no other
            // running threads in the VM.
            timer.shutDown();
        }

//        trace("removeDatabase(): exiting");
    }

// --------------------------- private static methods --------------------------

    /**
     * Internal method to find the HsqlRuntime root thread group
     *
     * @param tg the ThreadGroup from which to start the search.  Typically,
     *      this will be the JVM root thread group.  Exceptional cases might
     *      be under applet use or under an installed SecurityManager that
     *      does not allow access to the root JVM thread group, the parent
     *      of the calling application thread's thread group or even the
     *      thread group of the calling application's thread.
     *
     * @return the HsqlRuntime root thread group
     *
     * @throws Exception if it is impossible to find the HsqlRuntime root
     *      thread group, for instance because of too restrictive a security
     *      policy
     */
    private static ThreadGroup findRootHsqldbThreadGroup(ThreadGroup tg)
    throws Exception {

        ThreadGroup[] rgs;

        if (tg == null) {
            return null;
        }

        synchronized (tg) {
            rgs = new ThreadGroup[(tg.activeGroupCount())];

            tg.enumerate(rgs);
        }

        for (int i = 0; i < rgs.length; i++) {
            tg = rgs[i];

            if ((tg != null) && rtgName.equals(tg.getName())) {
                return tg;
            }
        }

        return null;
    }

    /**
     * Retrieves, if possible, the root JVM thread group.
     *
     * @return the root JVM thread group
     *
     * @throws Exception if it is impossible to find the root JVM
     *      thread group, for instance because of too restrictive a
     *      security policy
     */
    private static ThreadGroup findRootThreadGroup() throws Exception {

        ThreadGroup tg;

        tg = Thread.currentThread().getThreadGroup();

        while ((tg != null) && (tg.getParent() != null)) {
            tg = tg.getParent();
        }

        return tg;
    }

//-------------------------- Private Instance Methods --------------------------

    /**
     * Retrieves an object against which synchronization will guarantee
     * exclusive HsqlRuntime access regarding the database with the
     * specified path.  The path argument must already be in absolutized
     * form.
     *
     * @param the absolute database path
     * @return the distinct mutex object for the specified absolute
     *      database path
     */
    private Object findOrCreateDatabaseMutexImpl(String abspath) {

        Object mutex;

        synchronized (dbMutexMap) {
            mutex = dbMutexMap.get(abspath);

            if (mutex == null) {
                mutex = new Object();

                dbMutexMap.put(abspath, mutex);
            }
        }

        return mutex;
    }

    /**
     * Retrieves the existing or newly created memory Database instance.
     *
     * @return the memory database instance for this HsqlRuntime
     * @throws SQLException should be never; required by database Constructor
     */
    private Database getOrCreateMemoryDatabase() throws SQLException {

        // This method does not need synchronization because it is
        // only called inside a syncronized(findOrCreateDatabaseMutext(path))
        // block.
        if (memoryDatabase == null) {
            memoryDatabase = new Database(".");
        }

        return memoryDatabase;
    }

    /**
     * Removes the memory Database instance for this HsqlRuntime.
     */
    private void removeMemoryDatabase() {

        // This method does not need synchronization because it is
        // only called inside a syncronized(findOrCreateDatabaseMutext(path))
        // block.
        memoryDatabase = null;
    }

    /**
     * Initializes the HsqlRuntime state upon first getHsqlRuntime() invocation
     *
     * @throws IllegalStateException if a problem occurs while initializing the
     *      HsqlRuntime.
     */
    private void init() throws IllegalStateException {

        ThreadGroup root;
        Vector      exceptions;
        Class       c;

        dbThreadMap   = new Hashtable();
        dbMap         = new Hashtable();
        dbUseCountMap = new Hashtable();
        exceptions    = new Vector();

        try {
            root = findRootThreadGroup();
            rtg  = findRootHsqldbThreadGroup(root);

            if (rtg == null) {
                rtg = new ThreadGroup(root, rtgName);

                rtg.setDaemon(false);
            }
        } catch (Exception e1) {
            exceptions.addElement(e1);

            try {
                rtg = new ThreadGroup(rtgName);
            } catch (Exception e2) {
                exceptions.addElement(e2);
            }
        }

        if (rtg == null) {

//            trace("root hsqldb thread group is null");
            // do nothing; security dictates that we aren't allowed to have
            // our thread groups
        } else {

//            trace("got root hsqldb thread group: " + rtg);
            try {
                dbThreadGroup        = new ThreadGroup(rtg, dbtgName);
                serverThreadGroup    = new ThreadGroup(rtg, stgName);
                webServerThreadGroup = new ThreadGroup(rtg, wstgName);

                dbThreadGroup.setDaemon(false);
                serverThreadGroup.setDaemon(false);
                webServerThreadGroup.setDaemon(false);

//                trace("stateDescriptor\n" + stateDescriptor());
                if (Trace.TRACE) {
                    Trace.trace("HsqlRuntime.init() fully initialized");
                }
            } catch (Exception e) {
                exceptions.addElement(e);
            }
        }

        timer = new HsqlTimer();
        c     = Runtime.class;

        try {
            addShutdownHook = c.getMethod("addShutdownHook",
                                          new Class[]{ Thread.class });
        } catch (Exception e) {}

        try {
            availableProcessors = c.getMethod("availableProcessors",
                                              new Class[]{});
        } catch (Exception e) {}

        try {
            maxMemory = c.getMethod("maxMemory", new Class[]{});
        } catch (Exception e) {}

        try {
            removeShutdownHook = c.getMethod("removeShutdownHook",
                                             new Class[]{ Thread.class });
        } catch (Exception e) {}

        if (exceptions.size() > 0) {
            print("Restrictive security policy in effect.");
            print("Available features will be limited.");
            print("Likely only memory database instance will be available.");
            print("Exceptions follow:");

            for (int i = 0; i < exceptions.size(); i++) {
                print(exceptions.elementAt(i));
            }
        }
    }

    private void print(Object o) {
        Trace.printSystemOut("[" + this + "]:" + o);
    }

//    private void trace(Object o) {
//        if (TRACE) {
//           print("[" + Thread.currentThread() + "]: " + o);
//        }
//    }
    //~ Inner Classes ----------------------------------------------------------

    /**
     * Opaque reference to a Database instance
     */
    public static final class DatabaseReference {

        //~ Instance fields ----------------------------------------------------

        /** The reference */
        Database database;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new DatabaseReference object.
         *
         * @param path the Database path
         *
         * @throws SQLException if a database access error occurs
         */
        private DatabaseReference(String path) throws SQLException {
            database = HsqlRuntime.getHsqlRuntime().getDatabase(path, this);
        }

        //~ Methods ------------------------------------------------------------

        /**
         * Retrieves a new DatabaseReference for the database corresponding
         * to the specified path.
         *
         * @param path the path of the desired database
         *
         * @return a new DatabaseReference
         *
         * @throws SQLException if a database access error occurs
         */
        public static DatabaseReference newReference(String path)
        throws SQLException {
            return new DatabaseReference(path);
        }

        /**
         * Clears the internal database reference.
         */
        public synchronized void clear() {

            Database ref;

            if (database != null) {
                ref      = database;
                database = null;

                HsqlRuntime.getHsqlRuntime().releaseDatabase(ref, this);
            }
        }

        protected void finalize() throws Throwable {
            clear();
        }

        /**
         * Retreives a string representation of this object. This includes a
         * string representation of the internal database instance,
         * its absolute path and its state string.
         *
         * @return a string representation of this object
         */
        public synchronized String toString() {

            String dbname;
            String dbstate;

            dbname = (database == null) ? null
                                        : absoluteDatabasePath(
                                            database.getName());
            dbstate = (database == null) ? null
                                         : database.getStateString();

            return super.toString() + "[" + database + "[" + dbname + "]["
                   + dbstate + "]]";
        }
    }

    /**
     * Thread subclass providing global, cross class loader context
     * access to database registration information.  All successful
     * Database instance registrations, regardless of class loader context,
     * cause a DatabasePlaceHolderThread instance to be placed in the
     * HsqlRuntime database thread group, located just under the HsqlRuntime
     * root thread group, thus making database registration information
     * visible to all code running in the JVM.  This is, of course, subject
     * to any security policies in effect regarding ThreadGroup access.
     */
    static final class DatabasePlaceHolderThread extends Thread {

        //~ Instance fields ----------------------------------------------------

        /** The database instance for which this thread is a place holder */
        private Database database;

        /** This Thread's context class loader (that loaded HsqlRuntime) */
        private ClassLoader cl;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new DatabasePlaceHolderThread object.
         *
         * @param tg The HsqlRuntime database thread group
         * @param database The database instance for which this thread
         *      is a place holder
         */
        DatabasePlaceHolderThread(ThreadGroup tg, Database database) {

            super(tg, HsqlRuntime.absoluteDatabasePath(database.getName()));

            this.database = database;

            try {
                cl = HsqlRuntime.class.getClassLoader();
            } catch (Exception e) {
                cl = null;
            }
        }

        //~ Methods ------------------------------------------------------------

        /**
         * Runs forever until interrupted by HsqlRuntime to indicate that
         * the database that this thread represents has been removed from the
         * database registry (i.e. will cease to be one of the threads
         * listed when enumerating the HsqlRuntime dbThreadGroup).
         */
        public void run() {

            try {
                while (true) {
                    sleep(Integer.MAX_VALUE);
                }
            } catch (Exception e) {}

            database = null;
            cl       = null;

            if (Trace.TRACE) {
                Trace.trace(toString() + " exiting.");
            }
        }

        /**
         * Retrieves the context ClassLoader for this Thread. The context
         * ClassLoader is provided by the HsqlRuntime instance that created
         * this thread for use by client code when loading HSQLDB classes
         * and resources.
         *
         * If not set at creation (e.g. disallowed by the installed
         * SecurityManager), the default is null, causing external
         * Class.forName(name,loader) calls to use only the bootstrap class
         * loader.  When calling HsqlRuntime.classForName(name) or
         * HsqlRuntime.classForName(name,ctx), the abscense of a context class
         * loader for the current thread or ctx object, respectively, causes
         * Class.forName(name) to be used, which is equivalent to
         * Class.forName(name, currentLoader, true), where currentLoader
         * denotes the class loader that loaded the HsqlRuntime class.
         * However, since HSQLDB aims at JDK 1.1 compilance, the
         * Class.forName(name, currentLoader, true) signature is never
         * actually invoked internally. <p>
         *
         * If there is a security manager, and the caller's class loader is
         * not null and the caller's class loader is not the same as or an
         * ancestor of the context class loader for the thread whose context
         * class loader is being requested, then the security manager's
         * checkPermission method is called with a
         * RuntimePermission("getClassLoader") permission to see if it's ok to
         * get the context ClassLoader.
         *
         * @throws SecurityException if a security manager exists and its
         *      checkPermission method doesn't allow getting the context
         *      ClassLoader
         * @return the context ClassLoader for this Thread
         */
        public ClassLoader getContextClassLoader() throws SecurityException {

            checkAccess();

            return cl;
        }

        /**
         * Does not set the context ClassLoader for this Thread. The context
         * ClassLoader is automatically set by HsqlRuntime each time an
         * instance of this class is created.  Any attempt to call this
         * method will result in a SecurityException. This behaviour
         * protects the integrity of DatabasePlaceHolderThread instances,
         * becuase each one absolutely must carry a reference back to the
         * ClassLoader that loaded the originating HsqlRuntime class, as this
         * allows looking up and obtaining connections to database instances
         * across otherwise incompatible class loader contexts.
         *
         * @param cl unused
         * @throws SecurityException always
         */
        public void setContextClassLoader(ClassLoader cl)
        throws SecurityException {
            throw new SecurityException(
                "only HsqlRuntime may set class loader");
        }

        /**
         * Returns a string representation of this thread, including the
         * thread's database database instance identifier, database path,
         * database state string, thread priority and thread group name.
         *
         * @return a string representation of this thread
         */
        public String toString() {

            String       dbStateString;
            StringBuffer sb;

            dbStateString = database == null ? null
                                             : database.getStateString();
            sb            = new StringBuffer();

            sb.append("DatabasePlaceHolderThread").append('[').append(
                String.valueOf(database)).append('[').append(this.getName())    // was set to abs db path
                .append('[').append(dbStateString).append(']').append(
                    ']').append('[').append(String.valueOf(cl)).append(
                    ']').append(',').append(this.getPriority()).append(
                    ',').append(this.getThreadGroup().getName()).append(']');

            return sb.toString();
        }
    }
}
