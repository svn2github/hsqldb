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

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

// fredt@users 20020130 - patch 1.7.0 by fredt - new class
// replaces patch by deforest@users
// fredt@users 20020414 - patch 517028 by peterhudson@users - use of calendar

/**
 *  collection of static methods to convert Date, Time and Timestamp strings
 *  into corresponding Java objects. Also accepts SQL literals such as NOW,
 *  TODAY as valid strings and returns the current date / time / datetime.
 *  Compatible with jdk 1.1.x
 *
 * @author  fredt@users
 * @version 1.7.0
 */

/**
 * fredt - 20030103 - currently under review for 1.7.2
 * There is a fundamental SQL compatibility problem as SQL stores
 * DATETIME values as their separate fields, wheras Java stores them
 * all as a long representing milliseconds.
 * Currently, this causes issues in comparison between DATETIME values
 * if the db is accessed from different time zones.
 *
 */
class HsqlDateTime {

    /**
     * A reusable static value for today's date. Should only be accessed
     * by getToday()
     */
    private static Date today    = new Date(0);
    static Date         tempDate = new Date(0);

    /**
     *  Converts a string in JDBC timestamp escape format to a
     *  <code>Timestamp</code> value.
     *
     * @param s timestamp in format <code>yyyy-mm-dd hh:mm:ss.fffffffff</code>
     *      where end part can be omitted, or "NOW" (case insensitive)
     * @return  corresponding <code>Timestamp</code> value
     * @exception java.lang.IllegalArgumentException if the given argument
     * does not have the format <code>yyyy-mm-dd hh:mm:ss.fffffffff</code>
     */
    static Timestamp timestampValue(String s) {

        if (s == null) {
            throw new java.lang.IllegalArgumentException("null string");
        }

        if (s.indexOf('-') == -1) {
            s = s.toUpperCase();

            if (s.toUpperCase().equals("NOW")
                    || s.toUpperCase().equals("CURRENT_TIMESTAMP")) {
                return new Timestamp(System.currentTimeMillis());
            }

            // fredt - treat Date as full days only
            if (s.toUpperCase().equals("CURRENT_DATE")
                    || s.toUpperCase().equals("TODAY")
                    || s.toUpperCase().equals("SYSDATE")) {
                return new Timestamp(getToday().getTime());
            }

            throw new java.lang.IllegalArgumentException("invalid timestamp");
        }

        final String zerodatetime = "1970-01-01 00:00:00.000000000";

        s = s + zerodatetime.substring(s.length());

        return Timestamp.valueOf(s);
    }

    /**
     * @param  time milliseconds
     * @param  nano nanoseconds
     * @return  Timestamp object
     */
    static Timestamp timestampValue(long time, int nano) {

        Timestamp ts = new Timestamp(time);

        ts.setNanos(nano);

        return ts;
    }

    /**
     *  Converts a string in JDBC date escape format to a <code>Date</code>
     *  value. Also accepts Timestamp values.
     *
     * @param s date in format <code>yyyy-mm-dd</code>,
     *  'TODAY', 'NOW', 'CURRENT_DATE', 'SYSDATE' (case independent)
     * @return  corresponding <code>Date</code> value
     * @exception java.lang.IllegalArgumentException if the given argument
     * does not have the format <code>yyyy-mm-dd</code>
     */
    static Date dateValue(String s) {

        if (s == null) {
            throw new java.lang.IllegalArgumentException("null date");
        }

        if (s.indexOf('-') == -1) {
            s = s.toUpperCase();

            // fredt - treat Date as full days only
            if (s.equals("TODAY") || s.equals("CURRENT_DATE")
                    || s.equals("SYSDATE")) {
                return getToday();
            }

            throw new java.lang.IllegalArgumentException("invalid date");
        }

        if (s.length() > sdfdPattern.length()) {
            return Date.valueOf(s.substring(0, sdfdPattern.length()));
        }

        return Date.valueOf(s);    // Compatiblity...if no leading zero in mm/dd!
    }

    /**
     * Converts a string in JDBC date escape format to a
     * <code>Time</code> value.
     *
     * @param s date in format <code>hh:mm:ss</code>
     * 'CURRENT_TIME' or 'NOW' (case independent)
     * @return  corresponding <code>Time</code> value
     * @exception java.lang.IllegalArgumentException if the given argument
     * does not have the format <code>hh:mm:ss</code>
     */
    static Time timeValue(String s) {

        if (s == null) {
            throw new java.lang.IllegalArgumentException("null string");
        }

        if (s.toUpperCase().equals("NOW")
                || s.toUpperCase().equals("CURRENT_TIME")) {
            return new Time(System.currentTimeMillis()
                            - getToday().getTime());
        }

        return Time.valueOf(s);
    }

    private static final String sdftPattern  = "HH:mm:ss";
    private static final String sdfdPattern  = "yyyy-MM-dd";
    private static final String sdftsPattern = "yyyy-MM-dd HH:mm:ss.";

    static java.sql.Date getDate(String dateString,
                                 Calendar cal) throws Exception {

        java.text.SimpleDateFormat sdfd = new SimpleDateFormat(sdfdPattern);

        sdfd.setCalendar(cal);

        java.util.Date d = sdfd.parse(dateString);

        return new java.sql.Date(d.getTime());
    }

    static Time getTime(String timeString, Calendar cal) throws Exception {

        java.text.SimpleDateFormat sdft = new SimpleDateFormat(sdftPattern);

        sdft.setCalendar(cal);

        java.util.Date d = sdft.parse(timeString);

        return new java.sql.Time(d.getTime());
    }

    static Timestamp getTimestamp(String dateString,
                                  Calendar cal) throws Exception {

        java.text.SimpleDateFormat sdfts = new SimpleDateFormat(sdftsPattern);

        sdfts.setCalendar(cal);

        java.util.Date d = sdfts.parse(dateString.substring(0,
            sdftsPattern.length()));
        String nanostring = dateString.substring(sdftsPattern.length(),
            dateString.length());
        java.sql.Timestamp ts = new java.sql.Timestamp(d.getTime());

        ts.setNanos(Integer.parseInt(nanostring));

        return ts;
    }

    static java.text.SimpleDateFormat sdfts;

    static String getTimestampString(Timestamp x,
                                     Calendar cal) throws Exception {

        SimpleDateFormat sdfts = new SimpleDateFormat(sdftsPattern);

        sdfts.setCalendar(cal);

        return sdfts.format(new java.util.Date(x.getTime()
                                               + x.getNanos() / 1000000));
    }

    static String getTimeString(Time x, Calendar cal) throws Exception {

        final SimpleDateFormat sdft = new SimpleDateFormat(sdftPattern);

        sdft.setCalendar(cal);

        return sdft.format(x);
    }

    static String getDateString(Date x, Calendar cal) throws Exception {

        SimpleDateFormat sdfd = new SimpleDateFormat(sdfdPattern);

        sdfd.setCalendar(cal);

        return sdfd.format(x);
    }

/*
    // experimental stuff
    static SimpleDateFormat sdfd = new SimpleDateFormat(sdfdPattern);

    static {
        sdfd.setCalendar(Calendar.getInstance(TimeZone.getTimeZone("GMT")));
    }

    static String getGMTDateString(Date x, Calendar cal) throws Exception {
        return sdfd.format(x);
    }
*/

    /**
     * Returns the same Date Object. This object should be treated as
     * read-only.
     */
    static Date getToday() {

        long now = System.currentTimeMillis();

        if (now - today.getTime() > 24 * 3600 * 1000) {
            resetToday();
        }

        return today;
    }

    static void resetToDate(Calendar cal) {

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    static void resetToTime(Calendar cal) {

        cal.set(Calendar.YEAR, 0);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DATE, 0);
    }

    /**
     * resets the static reusable value today
     */
    synchronized private static void resetToday() {

        long now = System.currentTimeMillis();

// fredt - this needs more work
//        Calendar c   = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        Calendar c = new GregorianCalendar();

        c.setTime(new Date(now));
        resetToDate(c);

        today = new Date(c.getTime().getTime());
    }
    /*
    public static void main(String[] args) {
        String tests[] = { "2000-1-1", "2000-1-1 12:13", "2000-1-1 12:13:14",
                           "2000-1-1 12:13:14.15" };
        for (int i = 0; i < tests.length; i++) {
            String test = tests[i];
            System.out.print("test " + test + " = ");
            try {
                System.out.println(HsqlDateTime.timestampValue(test));
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }
*/
}
