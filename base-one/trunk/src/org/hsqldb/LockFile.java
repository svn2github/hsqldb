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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import org.hsqldb.lib.HsqlTimer;

/**
 * Base cooperative file-based locking implementation and factory. <p>
 *
 * Here is the way this class operates: <p>
 *
 * <ol>
 * <li>A lock file with a well-known path relative to each database instance
 *     is used to implement cooperative locking of database files across
 *     process boundaries (database instances running in different JVM
 *     host processes).<p>
 *
 * <li>A background thread in a timer object executes a Runnable so as
 *     to write the current time as a <code>long</code> to the first 8 bytes
 *     of this object's lock file at {@link #HEARTBEAT_INTERVAL} millisecond
 *     intervals, which acts as a heartbeat to indicate that a lock is still
 *     held.<p>
 *
 * <li>The generic lock attempt rules are: <p>
 *    <ul>
 *    <li>If a lock condition is already held by this object, do nothing and
 *        signify that this lock attempt was successful, else...<p>
 *
 *    <li>If no lock file exists, go ahead and create one, silently issue the
 *        {@link java.io.File#deleteOnExit File.deleteOnExit()} directive via
 *        refelective method invocation (i.e. inside a try-catch block with no
 *        handler), add thread and signify that this lock attempt
 *        was successful, else...<p>
 *
 *    <li>The file to use already exists, so try to read the first 8 bytes.
 *        If the read fails, assume the file is locked by another process
 *        and signify that this attempt failed, else if the read in value is
 *        less than <code>HEARTBEAT_INTERVAL</code> milliseconds previous to
 *        the current time, assume that the file is locked by another
 *        process and signify that this lock attempt failed, else assume
 *        that the file is not in use, shedule the periodic heartbeat task
 *        with a global timed task queue and signify that this lock attempt
 *        was successful.<p>
 *
 *    </ul>
 * <li>The generic release attempt rules are:<p>
 *    <ul>
 *    <li>If a lock condition is not currently held, do nothing and signify that
 *        this release attempt was successful, else...<p>
 *
 *    <li>A lock condition is currently held, so try to release it.  If the
 *        release is successful, cancel the periodic heartbeat task and signify
 *        that the release succeeded, else signify that the release attempt
 *        failed.<p>
 *
 *    </ul>
 * </ol> <p>
 *
 * In addition to the generic locking rules, the protected methods
 * {@link #lockImpl() lockImpl()} and {@link #releaseImpl() releaseImpl()}
 * are called during lock and release attempts, respectively.  This allows
 * the transaprent integration of extended strategies/capabilities for
 * locking and releasing, based on reflective construction of specializations
 * in the factory method {@link #getInstance getInstance()}, determined
 * by information gathered at run-time. <p>
 *
 * In particular, if it is available at runtime, then this class retrieves
 * instances of {@link NIOLockFile  NIOLockFile} to capitalize, when possible,
 * on the existence of the {@link java.nio.channels.FileLock FileLock} class.
 * If the <code>NIOLockFile</code> class does not exist at run-time or the
 * java.nio classes it uses are not supported under the run-time JVM, then
 * getInstance() produces vanilla LockFile instances, meaning that only
 * purely cooperative locking takes place, as opposed to possibly O/S-enforced
 * file locking which, theoretically, is made available through the
 * {@link java.nio.channels} package). <p>
 *
 * <b>Note:</b> <p>
 *
 * The <code>NIOLockFile</code> descendent exists because it has been determined
 * though experimenatation that <code>java.nio.channels.FileLock</code>
 * does not always exhibit the correct/desired behaviour under reflective
 * method invocation. That is, it has been discovered that under some operating
 * system/JVM combinations, after calling <code>FileLock.release()</code>
 * via a reflective method invocation, deletion of the lock file is not possible
 * from the owning object (this) and it is impossible for a different
 * <code>LockFile</code> instances to successfully obtain a lock
 * condition on (read from or write to) the same file, despite the fact of the
 * <code>FileLock</code> object reporting that its lock is invalid (released
 * successfully), until the JVM in which the <code>FileLock.tryLock()</code>
 * method was reflectively invoked is shut down. <p>
 *
 * To solve this, the original <code>LockFile</code> class was split in two and
 * instead reflection-based class instantiation is now performed at the level
 * of the <code>newLockFile()</code> factory method. Similarly, the HSQLDB ANT
 * build script detects the presence or abscence of JDK 1.4 features such as
 * java.nio and only attempts to build and deploy <code>NIOLockFile</code> to
 * the hsqldb.jar if such features are present. </p>
 *
 * @author boucherb@users.sourceforge.net
 * @version 1.7.2
 * @since HSQLDB 1.7.2
 */
public class LockFile {

    /** References this object's lock file. */
    protected File f;

    /** Cached value of the lock file's absolute path. */
    private String apath = null;

    /**
     * A RandomAccessFile constructed from this object's reference, f, to its
     * lock file.
     *
     * This RandomAccessFile is used to periodically write the first 8 bytes
     * of this object's lock file with the current time, as a long.
     */
    protected RandomAccessFile raf;

    /**
     * Number of milliseconds to wait between writing the the current
     * time, as a long, to the first 8 bytes of this object's lock file.
     */
    public static final long   HEARTBEAT_INTERVAL = 10000;
    public static final byte[] MAGIC              = "HSQLLOCK".getBytes();

    /** Indicates whether this object has a lock condition on its lock file. */
    protected boolean                locked;
    protected static final HsqlTimer timer = HsqlMasterRepository.getTimer();
    private Object                   timerTask;

    /**
     * Attempts to read the first 8 bytes of this object's lock file as a
     * <code>long</code> and compare it with the current time.  <p>
     *
     * An exception is thrown if it must be presumned that another process has
     * locked the file, using the following rules: <p>
     *
     * <ol>
     * <li>If the file does not exist, this method returns immediately.
     * <li>If an exception is raised reading the file, then an exeption is thrown.
     * <li>If the read is successful and the time read is less than
     *     <code>HEARTBEAT_INTERVAL</code> milliseconds before the current time,
     *      then an exception is thrown.
     * <li>If no exception is thrown in 2.) or 3.), this method simply returns.
     * </ol>
     * @throws Exception if it must be presumed that another process
     *        currently has a lock condition on
     *        this object's lock file
     * @see #readTouchTime
     */
    private void checkHeartbeat() throws Exception {

        long   lastHeartbeat;
        String mn;
        String path;

        mn   = "checkHeartbeat(): ";
        path = "lock file [" + getAbsolutePath() + "]";

        trace(mn + "entered.");

        if (!f.exists()) {
            trace(mn + path + " does not exist. Check OK.");

            return;
        }

        if (f.length() != 16) {
            trace(mn + path
                  + " length != 16; not an HSQLDB lock file. Check OK.");

            return;
        }

        try {
            lastHeartbeat = System.currentTimeMillis() - readHeartbeat();
        } catch (Exception e) {

            // e.printStackTrace();
            throw new Exception(e.getMessage() + " : " + getAbsolutePath());
        }

        trace(mn + path + " last heartbeat " + lastHeartbeat + " ms ago.");

        if (lastHeartbeat < HEARTBEAT_INTERVAL) {
            throw new Exception(
                mn + path + " is presumably locked by another process.");
        }
    }

    /**
     * Closes this object's {@link #raf RandomAccessFile}.
     *
     * @throws Exception if an IOException occurs
     */
    private void closeRAF() throws Exception {

        String mn;

        mn = "closeRAF(): ";

        trace(mn + "entered.");

        if (raf == null) {
            trace(mn + "raf was null upon entry. Exiting immediately.");
        } else {
            trace(mn + "closing " + raf);
            raf.close();
            trace(mn + raf + " closed successfully. Setting raf null");

            raf = null;
        }
    }

    /**
     * Initializes this object with the specified <code>File</code>
     * object. <p>
     *
     * The file argument is a reference to this object's lock file. <p>
     *
     * This action has the effect of attempting to release any existing
     * lock condition and reinitializing all lock-related member attributes
     * @param file a reference to the file this object is to use as its
     *      lock file
     */
    private void setFile(File file) {

        if (isLocked()) {
            try {
                tryRelease();
            } catch (Exception e) {
                trace(e);
            }
        }

        f      = file;
        apath  = f.getAbsolutePath();
        raf    = null;
        locked = false;
    }

    /**
     * Provides any specialized locking actions for the
     * {@link #tryLock() tryLock()} method. <p>
     *
     * Desendents are free to provide additional functionality here,
     * using the following rules:
     *
     * <pre>
     * PRE:
     *
     * This method is only called if tryLock() thinks it needs to get a lock
     * condition, so it can be assumed the locked == false upon entry, raf is
     * a non-null instance that can be used to get a FileChannel if desired,
     * and the lock file is at the very least readable.  Further, this object's
     * heatbeat task is definitely cancelled and/or has not yet been scheduled,
     * so whatever is in the first 8 bytes of the lock file, if it exists, is
     * what was left by a previous writer, if any.
     *
     * POST:
     *
     * This method must return false if any additional locking work fails,
     * else true.
     * </pre>
     *
     * The default implementation of this method reflectively (for JDK1.1
     * compliance) invokes f.deleteOnExit() in a silent manner (in a try
     * catch block with an empty handler) and always returns true. <p>
     *
     * @throws Exception if a situation is encountered that absolutely
     *        prevents the status of the lock condtion
     *        to be determined. (e.g. an IO exception
     *        occurs here)
     * @return <code>true</code> if no extended locking
     *        actions are taken or the actions succeed,
     *        else <code>false</code>.
     */
    protected boolean lockImpl() throws Exception {

        String mn;
        Method m;

        mn = "lockImpl(): ";

        trace(mn + "entered.");

        try {
            m = File.class.getMethod("deleteOnExit", new Class[]{});

            m.invoke(f, new Object[]{});
            trace(mn + "sucess for deleteOnExit: [" + getAbsolutePath()
                  + "]");
        } catch (Exception e) {
            trace(mn + e);
        }

        return true;
    }

    /**
     * Opens this object's {@link #raf RandomAccessFile}. <p>
     *
     * @throws Exception if an IOException occurs
     */
    private void openRAF() throws Exception {

        trace("openRAF(): entered.");

        raf = new RandomAccessFile(f, "rw");

        trace("openRAF(): got new 'rw' mode " + raf);
    }

    /**
     * Retrieves the first 8 bytes from this object's lock file,
     * as a <code>long</code> value.  If this object's lock file
     * does not exist, <code>Long.MIN_VALUE</code> (the earliest
     * time representable as a long in Java) is retrieved. <p>
     *
     * @throws Exception if an error occurs while reading the first 8
     *      bytes of this object's lock file.
     * @return the first 8 bytes from this object's lock file, as a
     *      <code>long</code> value, or Long.MIN_VALUE, the earliest
     *      time representable as a long in Java, if this object's lock
     *      file does not exist.
     */
    private long readHeartbeat() throws Exception {

        DataInputStream dis;
        long            heartbeat;

        heartbeat = Long.MIN_VALUE;

        String mn   = "readHeartbeat(): ";
        String path = "lock file [" + getAbsolutePath() + "]";

        trace(mn + "entered.");

        if (!f.exists()) {
            trace(mn + path + " does not exist.  returning '" + heartbeat
                  + "'");

            return heartbeat;
        }

        dis = new DataInputStream(new FileInputStream(f));

        trace(mn + " got new " + dis);

        for (int i = 0; i < MAGIC.length; i++) {
            if (MAGIC[i] != dis.readByte()) {
                trace(mn + path + " is not an HSQLDB lock file.  returning '"
                      + heartbeat + "'");

                return heartbeat;
            }
        }

        heartbeat = dis.readLong();

        trace(mn + " read: " + heartbeat + " ["
              + new java.sql.Timestamp(heartbeat) + "]");
        dis.close();
        trace(mn + " closed " + dis);

        return heartbeat;
    }

    /**
     * Provides any specialized releasing actions for the tryRelease() method. <p>
     * @return true if there are no specialized releasing
     *        actions performed or they succeed,
     *        else false
     * @throws Exception if a situation is encountered that absolutely
     *        prevents the status of the lock condtion
     *        to be determined. (e.g. an IO exception
     *        occurs here).
     */
    protected boolean releaseImpl() throws Exception {

        trace("releaseImpl(): no action: returning true");

        return true;
    }

    /** Schedules the lock heartbeat task. */
    private void startHeartbeat() {

        Runnable r;

        trace("startHeartbeat(): entered.");

        if (timerTask == null || timer.isCancelled(timerTask)) {
            r = new HeartbeatRunner();

            // now, periodic at HEARTBEAT_INTERVAL, runing this, fixed rate
            timerTask = timer.schedulePeriodicallyAfter(0,
                    HEARTBEAT_INTERVAL, r, true);

            trace("startHeartbeat(): heartbeat task scheduled.");
        }

        trace("startHeartbeat(): exited.");
    }

    /** Cancels the lock heartbeat task. */
    private void stopHeartbeat() {

        String mn = "stopHeartbeat(): ";

        trace(mn + "entered");

        if (timerTask != null &&!timer.isCancelled(timerTask)) {
            timer.cancel(timerTask);

            timerTask = null;
        }

        trace(mn + "exited");
    }

    private void writeMagic() throws Exception {

        String mn   = "writeMagic(): ";
        String path = "lock file [" + apath + "]";

        trace(mn + "entered.");
        trace(mn + "raf.seek(0)");
        raf.seek(0);
        trace(mn + "raf.write(byte[])");
        raf.write(MAGIC);
        trace(mn + "wrote [\"HSQLLOCK\".getBytes()] to " + path);
    }

    /**
     * Writes the current time as a long to the first 8 bytes of this
     * object's lock file. <p>
     *
     * @throws Exception if an IOException occurs
     */
    private void writeHeartbeat() throws Exception {

        long   time;
        String mn   = "writeHeartbeat(): ";
        String path = "lock file [" + apath + "]";

        trace(mn + "entered.");

        time = System.currentTimeMillis();

        trace(mn + "raf.seek(" + MAGIC.length + ")");
        raf.seek(MAGIC.length);
        trace(mn + "raf.writeLong(" + time + ")");
        raf.writeLong(time);
        trace(mn + "wrote [" + time + "] to " + path);
    }

    /**
     * Retrieves a <code>LockFile</code> instance initialized with a
     * <code>File</code> object whose path is the one specified by
     * the <code>path</code> argument. <p>
     *
     * @return an <code>LockFile</code> instance initialized with a
     *        <code>File</code> object whose path is the one specified
     *        by the <code>path</code> argument.
     * @param path the path of the <code>File</code> object with
     *        which the retrieved <code>LockFile</code>
     *        object is to be initialized
     */
    public static LockFile newLockFile(String path) {

        File     f;
        LockFile lf;
        Class    c;

        c = null;

        try {
            Class.forName("java.nio.channels.FileLock");

            c  = Class.forName("org.hsqldb.NIOLockFile");
            lf = (LockFile) c.newInstance();
        } catch (Exception e) {
            lf = new LockFile();
        }

        lf.setFile(new File(path));

        return lf;
    }

    /**
     * Tests whether some other object is "equal to" this one.
     *
     * An object is considered equal to a <code>LockFile</code> object iff it
     * is not null, it is an instance of <code>LockFile</code> and either it's
     * the identical instance or it has the same lock file.  More  formally,
     * is is considered equal iff it is not null, it is an instance of
     * <code>LockFile</code>, and the expression: <p>
     *
     * <pre>
     * this == other ||
     * this.f == null ? other.f == null : this.f.equals(other.f);
     * </pre>
     *
     * yeilds true. <p>
     *
     * @param obj the reference object with which to compare.
     * @return <code>true</code> if this object is equal to
     *        the <code>obj</code> argument;
     *        <code>false</code> otherwise.
     * @see #hashCode
     */
    public boolean equals(Object obj) {

        // do faster tests first
        if (this == obj) {
            return true;
        } else if (obj != null && obj instanceof LockFile) {
            LockFile that = (LockFile) obj;

            return (f == null) ? that.f == null
                               : f.equals(that.f);
        } else {
            return false;
        }
    }

    /**
     * Retreives, as a String, the absolute path of this object's lock file.
     *
     * @return the absolute path of this object's lock file.
     */
    public String getAbsolutePath() {
        return apath;
    }

    /**
     * Retrieves the hash code value for this object.
     *
     * The value is zero if the <code>File</code> object attribute
     * <code>f</code> is <code>null</code>, else it is the <code>hashCode</code>
     * of <code>f</code>. That is, two <code>LockFile</code>
     * objects have the same <code>hashCode</code> value if they have the
     * same lock file. <p>
     *
     * @return a hash code value for this object.
     * @see #equals(java.lang.Object)
     */
    public int hashCode() {
        return f == null ? 0
                         : f.hashCode();
    }

    /**
     * Retrieves whether this object has successfully obtained and is
     * still currently holding (has not yet released) a cooperative
     * lock condition on its lock file. <p>
     *
     * <b>Note:</b>  Due to the retrictions placed on the JVM by
     * platform-independence, it is very possible to successfully
     * obtain and hold a cooperative lock on a lock file and yet for
     * the lock to become invalid while held.  <p>
     *
     * For instance, under JVMs with no <code>java.nio</code> package or
     * operating systems that cannot live up to the contracts set forth for
     * {@link java.nio.channels.FileLock FileLock}, it is quite possible
     * for another process or even an uncooperative bit of code running
     * in the same JVM to delete or overwrite the lock file while
     * this object holds a lock on it. <p>
     *
     * Because of this, the isValid() method is provided in the public
     * interface in order to allow clients to detect such situations. <p>
     *
     * @return true iff this object has successfully obtained
     *        and is currently holding (has not yet released)
     *        a lock on its lock file
     * @see #isValid
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Retrieves whether this object holds a valid lock on its lock file. <p>
     *
     * More formally, this method retrieves true iff: <p>
     *
     * <pre>
     * isLocked() &&
     * f != null &&
     * f.exists() &&
     * raf != null
     * </pre>
     *
     * @return true iff this object holds a valid lock on its
     *        lock file.
     */
    public boolean isValid() {
        return isLocked() && f != null && f.exists() && raf != null;
    }

    /**
     * For internal use only. <p>
     *
     * This Runnable class provides the implementation for the timed task
     * that periodically writes out a heartbeat value to the lock file.<p>
     */
    protected class HeartbeatRunner implements Runnable {

        public void run() {

            try {
                trace("HeartbeatRunner.run(): writeHeartbeat()");
                writeHeartbeat();
            } catch (Throwable t) {
                trace("HeartbeatRunner.run(): caught Throwable: " + t);
            }
        }
    }

    /**
     * Retrieves a String representation of this object. <p>
     *
     * The String is of the form: <p>
     *
     * <pre>
     * super.toString() +
     * "[file=" + getAbsolutePath() +
     * ", exists=" + f.exists() +
     * ", locked=" + isLocked() +
     * ", valid=" + isValid() +
     * ", " + toStringImpl() +
     * "]";
     * </pre>
     * @return a String representation of this object.
     * @see #toStringImpl
     */
    public String toString() {

        return super.toString() + "[file =" + apath + ", exists="
               + f.exists() + ", locked=" + isLocked() + ", valid="
               + isValid() + ", " + toStringImpl() + "]";
    }

    /**
     * Retreives an implementation-specific tail value for the
     * toString() method. <p>
     *
     * The default implementation returns the empty string.
     * @return an implementation-specific tail value for the toString() method
     * @see #toString
     */
    protected String toStringImpl() {
        return "";
    }

    /**
     * Attempts, if not already held, to obtain a cooperative lock condition
     * on this object's lock file. <p>
     *
     * @throws Exception if an error occurs that absolutely prevents the lock
     *        status of the lock condition from being determined
     *        (e.g. an unhandled file I/O error).
     * @return <code>true</code> if this object already holds a lock or
     *        the lock was obtained successfully, else
     *        <code>false</code>
     */
    public boolean tryLock() throws Exception {

        String mn = "tryLock(): ";

        trace(mn + "entered.");

        if (locked) {
            trace(mn + " lock already held. Returning true immediately.");

            return true;
        }

        checkHeartbeat();

// Alternatively, we could give ourselves a second try,
// raising our chances of success in the rare case that the
// last locker terminiated abruptly just less than
// HEARTBEAT_INTERVAL ago.
//
//        try {
//            checkHeartbeat();
//        } catch (Exception e) {
//            try {
//                Thread.sleep(HEARTBEAT_INTERVAL);
//            } catch (Exception e2) {}
//            checkHeartbeat();
//        }
        openRAF();

        locked = lockImpl();

        if (locked) {
            writeMagic();
            startHeartbeat();

            try {

                // attempt to ensure that tryRelease() gets called if/when
                // the VM shuts down, just in case this object has not yet
                // been garbage-collected or explicitly released.
                System.runFinalizersOnExit(true);
                trace(mn + "success for System.runFinalizersOnExit(true)");
            } catch (Exception e) {
                trace(mn + e.toString());
            }
        } else {
            try {
                releaseImpl();
                closeRAF();
            } catch (Exception e) {
                trace("tryLock(): " + e.toString());
            }
        }

        trace(mn + "ran to completion.  Returning " + locked);

        return locked;
    }

    /**
     * Attempts to release any cooperative lock condition this object
     * may have on its lock file. <p>
     *
     * @throws Exception if an error occurs that absolutely prevents
     *       the status of the lock condition from
     *       being determined (e.g. an unhandled file
     *       I/O exception).
     * @return <code>true</code> if this object does not hold a
     *        lock or the lock is released successfully,
     *        else <code>false</code>.
     */
    public boolean tryRelease() throws Exception {

        String mn = "tryRelease(): ";

        trace(mn + "entered.");

        boolean released = !locked;

        if (released) {
            trace(mn + "No lock held. Returning true immediately");

            return true;
        }

        try {
            released = releaseImpl();
        } catch (Exception e) {
            trace(mn + e);
        }

        if (!released) {
            trace(mn + "releaseImpl() failed. Returning false immediately.");

            return false;
        }

        trace(mn + "releaseImpl() succeeded.");
        stopHeartbeat();
        closeRAF();

        // without a small delay, the following delete may occasionally fail
        // and return false on some systems, when really it should succeed
        // and return true.
        try {
            trace(mn + "Starting Thread.sleep(100).");
            Thread.sleep(100);
        } catch (Exception e) {
            trace(mn + e);
        }

        trace(mn + "Finished Thread.sleep(100).");

        String path = "[" + getAbsolutePath() + "]";

        if (f.exists()) {
            trace(mn + path + " exists.");

            released = f.delete();

            trace(mn + path + (released ? ""
                                        : "not") + " deleted.");

            if (f.exists()) {
                trace(mn + " WARNING!: " + path + "still exists.");
            }
        }

        locked = !released;

        trace(mn + "ran to completion.  Returning " + released);

        return released;
    }

    /**
     * Prints tracing information and the value of the specified object
     *
     * @param o the value to print
     */
    protected void trace(Object o) {

//        if (Trace.TRACE) {
//            Trace.printSystemOut(super.toString() + ": " + o);
//        }
        // just for debugging until released
        System.out.println(super.toString() + ": " + o);
    }

    /**
     * Attempts to release any lock condition this object may have on its
     * lock file. <p>
     *
     * @throws Throwable if this object encounters an unhandled exception
     *        trying to release the lock condition,
     *        if any, that it has on its lock file.
     */
    protected void finalize() throws Throwable {
        trace("finalize(): calling tryRelease()");
        tryRelease();
    }
}
