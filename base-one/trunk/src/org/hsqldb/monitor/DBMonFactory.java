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


package org.hsqldb.monitor;

//#ifdef JAMON
import com.jamonapi.*;

//#endif JAMON

/**
 * DBMonFactory is a front for the JAMon performance tuning api. It reduces
 * hsqldb's dependency on JAMon to this one class. hsqldb code calls methods
 * from this class and not JAMon. If JAMon.jar is in the class path then by
 * default monitoring is enabled/will gather statistics. If JAMon.jar is not
 * in the classpath then monitoring is not enabled. In addition the
 * DBMonFactory can be disabled even if JAMon is in the class path.  After
 * calling DBMonFacatory.start("label"); the label will show up in the
 * performance report along with various collected stats such as hits, avg
 * time, total time, max time, min time among other stats. See
 * http://www.jamonapi.com for more information on the gathered statistics.
 * Sample invocations can be seen in the main method. Note hsqldb can be
 * compiled to have jamon references in the code or not. By default jamon code
 * will be compiled in and jamon.jar needs to be available at compile time.
 */
public class DBMonFactory {

    /** enabled or disabled (noop) factory */
    private static DBMonFactoryInt monitor;
    private static String JAMonNotAvailableMsg =
        "JAMon is not available, so monitoring will not occur."
        + "To enable monitoring put JAMon.jar in your classpath (http://www.jamonapi.com).";

    /** true if jamon was found in the classpath */
    private static boolean isJAMonAvailable;

    // If JAMon classes are available then enable monitoring else
    // disable monitoring.
    static {

//#ifdef JAMON
/*
        if (loadJAMon()) {
            enableMonitoring();
        } else
*/

//#endif JAMON
        {
            disableMonitoring();
        }
    }

    /**
     * load jamon if it is available.  It must be available for monitoring to
     * occur, however it is not an error if JAMon is not there
     */
    private static boolean loadJAMon() {

        try {
            Class.forName("com.jamonapi.MonitorFactory");

            isJAMonAvailable = true;
        } catch (ClassNotFoundException e) {
            isJAMonAvailable = false;
        }

        return isJAMonAvailable;
    }

    /**
     * Method that returns true if the JAMon jar is in the classpath.  For
     * monitoring to occur this must be true.  However, monitoring can be
     * explicitly disabled even if this is true.
     */
    public static boolean isJAMonAvailable() {
        return isJAMonAvailable;
    }

    /**
     * Method to start a monitor. stop() must later be called on the returned
     * monitor.  The label will show up in the performance monitor report.
     */
    public static DBMon start(String label) {
        return monitor.start(label);
    }

    /**
     * Return an HTML table of the Monitoring performance report.   The report
     * will be sorted on the specified column (first index is 0) and in the
     * specfied sort order (asc, desc).
     */
    public static String getPerformanceReport(int sortCol,
            String sortOrder) throws Exception {

        if (isJAMonAvailable) {    // i.e. JAMon jar was in classpath
            return monitor.getPerformanceReport(sortCol, sortOrder);
        } else {
            return JAMonNotAvailableMsg;
        }
    }

    /**
     * Return an HTML table of the Monitoring performance report sorted in
     * default order by label.
     */
    public static String getPerformanceReport() throws Exception {
        return getPerformanceReport(1, "asc");
    }

    /**
     * disable collection of monitoring statistics.
     */
    public static void disableMonitoring() {
        monitor = new MonitorDisabled();
    }

    /**
     * enable collection of monitoring statistics. If JAMon is not available
     * then monitoring will remain disabled. You must have JAMon in your
     * classpath for monitoring to work/be enabled.
     */
    public static void enableMonitoring() {

//#ifdef JAMON
/*
        if (isJAMonAvailable) {
            monitor = new MonitorEnabled();
        }
*/

//#endif JAMON
    }

    /**
     *  The DBMonFactoryInt interface is a nested interface that has 2 nested
     *  class implementations MonitorEnabled, and MonitorDisabled.  The interface
     *  is used to simplify enabling and disabling monitoring in the static
     *  methods above.
     */
    private interface DBMonFactoryInt extends DBMon {

        // Start the monitor
        public DBMon start(String label);

        /**
         * Returns a sorted report of all org.hsqldb performance monitors. This
         * method should be called from DBMonFactory.
         */
        public String getPerformanceReport(int sortCol,
                                           String sortOrder) throws Exception;

        /**
         * Returns a report of all org.hsqldb performance monitors sorted by the
         * monitor label. This method should be called from DBMonFactory.
         */
        public String getPerformanceReport() throws Exception;
    }


    /**
     * Factory used when JAMon is available,and monitoring is enabled.
     * MonitorEnabled can be conditionally compiled into hsqldb.
     */

//#ifdef JAMON
/*
    private static class MonitorEnabled implements DBMonFactoryInt {

        private Monitor             mon;
        private static final String PACKAGE = "org.hsqldb.";

        public MonitorEnabled() {}

        public MonitorEnabled(Monitor mon) {
            this.mon = mon;
        }

        public DBMon start(String label) {
            return new MonitorEnabled(MonitorFactory.start(label));
        }

        public DBMon stop() {

            mon.stop();

            return this;
        }

        public String toString() {
            return mon.toString();
        }

        public String getPerformanceReport() throws Exception {
            return getPerformanceReport(1, "asc");
        }

        public String getPerformanceReport(int sortCol,
                                           String sortOrder)
                                           throws Exception {
            return MonitorFactory.getComposite(PACKAGE).getReport(sortCol,
                                               sortOrder);
        }
    }
*/

//#endif JAMON

    /**
     * Factory used when JAMon is not available, or monitoring is explicitly
     * disabled. The following disabled class is the equivalent of the Null
     * Object refactoring. It performs a noop on all methods.  It also returns
     * a Singleton as there is no reason to create this noop stateless object
     * with every invocation.  The effect is to be able to turn monitoring off
     * at any time.
     */
    private static class MonitorDisabled implements DBMonFactoryInt {

        private static DBMonFactoryInt SINGLETON = new MonitorDisabled();

        public DBMon start(String label) {
            return SINGLETON;
        }

        public DBMon stop() {
            return this;
        }

        public String toString() {
            return "";
        }

        public String getPerformanceReport() throws Exception {
            return "Monitoring is not enabled.";
        }

        public String getPerformanceReport(int sortCol,
                                           String sortOrder)
                                           throws Exception {
            return "Monitoring is not enabled.";
        }
    }

    /**
     * Test code for this class.  To test this should be run once with jamon.jar
     * in the classpath and once with it not in the classpath.
     */
    public static void main(String[] args) throws Exception {

        System.out.println(
            "monitoring is enabled by default if jamon is available.");
        System.out.println(
            DBMonFactory.start(
                "org.hsqldb.jdbc.jdbcConnection.getConnection()").stop());
        System.out.println(
            DBMonFactory.start(
                "org.hsqldb.jdbc.jdbcConnection.createStatement()").stop());
        System.out.println(
            DBMonFactory.start(
                "org.hsqldb.jdbc.jdbcConnection.prepareStatement()").stop());
        System.out.println(
            "disable monitoring. note toString, and display of report are noops");
        DBMonFactory.disableMonitoring();

        // toString won't display anything now, stats won't gather and report
        // won't display.
        System.out.println(
            DBMonFactory.start(
                "org.hsqldb.jdbc.jdbcConnection.getConnection()").stop());
        System.out.println(
            DBMonFactory.start(
                "org.hsqldb.jdbc.jdbcConnection.createStatement()").stop());
        System.out.println(
            DBMonFactory.start(
                "org.hsqldb.jdbc.jdbcConnection.prepareStatement()").stop());
        System.out.println(DBMonFactory.getPerformanceReport());
        System.out.println(
            "reenable Monitoring i.e. stats gathering, and displaying");

        // reenable statistics
        DBMonFactory.enableMonitoring();
        System.out.println(
            DBMonFactory.start(
                "org.hsqldb.jdbc.jdbcConnection.getConnection()").stop());
        System.out.println(
            DBMonFactory.start(
                "org.hsqldb.jdbc.jdbcConnection.createStatement()").stop());
        System.out.println(
            DBMonFactory.start(
                "org.hsqldb.jdbc.jdbcConnection.prepareStatement()").stop());
        System.out.println(DBMonFactory.getPerformanceReport());
    }
}
