/* Copyright (c) 2001-2010, The HSQL Development Group
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

import org.hsqldb.types.IntervalType;
import org.hsqldb.types.Type;
import org.hsqldb.types.Types;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ScannerTest extends TestCase {

    // todo - fredt - needs to use a session from a database instance

    // HACK!!
    // TODO:  Fix
    //    java.lang.reflect.InvocationTargetException
    //Caused by: java.lang.ExceptionInInitializerError
    //	at org.hsqldb.HsqlNameManager.newInfoSchemaObjectName(HsqlNameManager.java:128)
    //	at org.hsqldb.Collation.<init>(Collation.java:166)
    //	at org.hsqldb.Collation.<clinit>(Collation.java:154)
    //	at org.hsqldb.types.CharacterType.<init>(CharacterType.java:81)
    //	at org.hsqldb.types.Type.<clinit>(Type.java:451)
    //	at org.hsqldb.ScannerTest.computeTestName(ScannerTest.java:49)
    //	at org.hsqldb.ScannerTest.<init>(ScannerTest.java:69)
    //	at org.hsqldb.ScannerTest.suite(ScannerTest.java:108)
    //Caused by: java.lang.NullPointerException
    //	at org.hsqldb.types.DTIType.<init>(DTIType.java:174)
    //	at org.hsqldb.types.DateTimeType.<init>(DateTimeType.java:63)
    //	at org.hsqldb.SqlInvariants.<clinit>(SqlInvariants.java:218)
    static IntervalType sit = Type.SQL_INTERVAL_DAY;

    public static String computeTestName(
            final String toScan,
            final int dataType,
            final long precision,
            final int fractionalPrecision) {
        return "\"" +
                toScan +
                "\", " +
                IntervalType.getIntervalType(dataType, precision, fractionalPrecision).getDefinition() +
                ", (" +
                precision +
                "," +
                fractionalPrecision +
                ")";
    }
    private String m_toScan;
    private int m_dataType;
    private long m_precision;
    private int m_fractionalPrecision;

    public ScannerTest(
            final String toScan,
            final int dataType,
            final long precision,
            final int fractionalPrecision) {
        super(computeTestName(toScan,dataType,precision,fractionalPrecision));

        m_toScan = toScan;
        m_dataType = dataType;
        m_precision = precision;
        m_fractionalPrecision = fractionalPrecision;
    }
    // inherit javadocs

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }


    // inherit javadocs
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected void runTest() throws Exception {
        IntervalType t = IntervalType.getIntervalType(
                m_dataType,
                m_precision,
                m_fractionalPrecision);
        Scanner scanner = new Scanner();
        Object i = scanner.convertToDatetimeInterval(null, m_toScan, t);
    }

    /**
     * suite method automatically generated by JUnit module
     *
     * @return the JDBC test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(ScannerTest.class.getName());

        ScannerTest[] tests = new ScannerTest[]{
            new ScannerTest("200 10", Types.SQL_INTERVAL_DAY_TO_HOUR, 4, 0),
            new ScannerTest("200 10:12:12.456789", Types.SQL_INTERVAL_DAY_TO_SECOND, 3, 6),
            new ScannerTest("200 10:12:12.456789", Types.SQL_INTERVAL_DAY_TO_SECOND, 3, 7),
            new ScannerTest("INTERVAL '200 10:12:12.' DAY(4) TO SECOND(8)", Types.SQL_INTERVAL_DAY_TO_SECOND, 3, 5),
            new ScannerTest("INTERVAL '200 10:12:12.' DAY TO SECOND", Types.SQL_INTERVAL_DAY_TO_SECOND, 3, 5)
        };


        for (int i = 0; i < tests.length; i++) {
            suite.addTest(tests[i]);
        }


        /* TODO:  Fix these tests.
         * The inheritance design of the types has changed, breaking the
         * usage here.
        s = "TIME '10:12:12.'";
        t = Type.SQL_TIME;
        i = scanner.convertToDatetimeInterval(s, t);

        s = "2007-01-02 10:12:12";
        t = Type.SQL_TIMESTAMP;
        i = scanner.convertToDatetimeInterval(s, t);

        s = "200 10:00:12";
        t = IntervalType.getIntervalType(Types.SQL_INTERVAL_DAY_TO_SECOND,
        3, 5);
        i = scanner.convertToDatetimeInterval(s, t);
         */

        return suite;
    }

    public static void main(java.lang.String[] argList) {

        junit.textui.TestRunner.run(suite());
    }
}
