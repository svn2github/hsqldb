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


package org.hsqldb.test;

import java.util.Vector;
import java.util.Random;
import org.hsqldb.lib.HsqlStringBuffer;

/**
 * Tests the HSB string buffer class
 *
 * @author dnordahl@users
 */
public class TestHSB {

    private final static int NUMBER_OF_TEST_RUNS          = 50000;
    private final static int NUMBER_OF_ITERATIONS_PER_RUN = 80;
    private Random           randomGenerator;
    private Vector           commandsCalled;
    private Timer            timer;

    //Commands
    private final static int APPEND_CHAR       = 1;
    private final static int APPEND_STRING     = 2;
    private final static int APPEND_HSB        = 3;
    private final static int INSERT_CHAR       = 4;
    private final static int INSERT_STRING     = 5;
    private final static int INSERT_HSB        = 6;
    private final static int COMPARE_TO_HSB    = 7;
    private final static int COMPARE_TO_STRING = 8;
    private final static int EQUALS            = 9;
    private final static int SET_LENGTH        = 10;
    private final static int TO_QUOTED_STRING  = 11;

    /** Creates a new instance of TestHSB */
    public TestHSB() {

        randomGenerator = new Random(System.currentTimeMillis());
        commandsCalled  = new Vector(NUMBER_OF_ITERATIONS_PER_RUN);
        timer           = new Timer(this);

        timer.start();
    }

    public void doTest() {

        HsqlStringBuffer hsb = new HsqlStringBuffer();
        StringBuffer     jsb = new StringBuffer();

        commandsCalled = new Vector(NUMBER_OF_ITERATIONS_PER_RUN);

        int              tempInt = 0;
        int              tempCommandCode;
        int              tempPosition;
        boolean          hsbException = false;
        boolean          sbException  = false;
        char             tempChar;
        String           tempString      = "";
        HsqlStringBuffer tempHSB         = null;
        boolean          equalsTrue      = true,
                         equalsFalse     = false;
        int              compareZero     = 0,
                         compareNotZero1 = 0,
                         compareNotZero2 = 0;
        int              stop            = getRandomInt(3, 12);

        for (int i = 0; i < stop; i++) {    // prime the contents with a couple items
            tempChar = getRandomChar();

            hsb.append(tempChar);
            jsb.append(tempChar);
            commandsCalled.addElement("Append char");
        }

        compare(jsb, hsb);

        for (int j = 0; j < NUMBER_OF_ITERATIONS_PER_RUN; j++) {
            tempCommandCode = getRandomInt(0, 12);    // 0 and 12 are ignored or used for a special op

            switch (tempCommandCode) {

                case APPEND_CHAR :
                    tempChar = getRandomChar();

                    commandsCalled.addElement("Append char");
                    jsb.append(tempChar);
                    hsb.append(tempChar);
                    break;

                case APPEND_STRING :
                    tempString = getRandomString("");

                    commandsCalled.addElement("Append String " + tempString);
                    jsb.append(tempString);
                    hsb.append(tempString);

                    tempString = "";
                    break;

                case APPEND_HSB :
                    tempString = getRandomString("");
                    tempHSB    = new HsqlStringBuffer(tempString);

                    commandsCalled.addElement("Append HSB "
                                              + tempHSB.toString());
                    jsb.append(tempString);
                    hsb.append(tempHSB);

                    tempString = "";
                    break;

                case INSERT_CHAR :
                    tempChar     = getRandomChar();
                    tempPosition = getRandomInt(0, jsb.length() + 1);

                    commandsCalled.addElement("Insert char " + tempPosition);

                    try {
                        jsb.insert(tempPosition, tempChar);
                    } catch (Exception ex) {
                        sbException = true;
                    }

                    try {
                        hsb.insert(tempPosition, tempChar);
                    } catch (Exception ex) {
                        hsbException = true;
                    }
                    break;

                case INSERT_STRING :
                    tempString   = getRandomString("");
                    tempPosition = getRandomInt(0, jsb.length() + 1);

                    commandsCalled.addElement("Insert string " + tempString
                                              + " at " + tempPosition);

                    try {
                        jsb.insert(tempPosition, tempString);
                    } catch (Exception ex) {
                        sbException = true;
                    }

                    try {
                        hsb.insert(tempPosition, tempString);
                    } catch (Exception ex) {
                        hsbException = true;
                    }

                    tempString = "";
                    break;

/*
                case INSERT_HSB :
                    tempString   = getRandomString("");
                    tempHSB      = new HsqlStringBuffer(tempString);
                    tempPosition = getRandomInt(0, jsb.length() + 1);

                    commandsCalled.addElement("Insert hsb "
                                              + tempHSB.toString() + " at "
                                              + tempPosition);

                    try {
                        jsb.insert(tempPosition, tempString);
                    } catch (Exception ex) {
                        sbException = true;
                    }

                    try {
                        hsb.insert(tempPosition, tempHSB);
                    } catch (Exception ex) {
                        hsbException = true;
                    }

                    tempString = "";
                    break;
*/
                case COMPARE_TO_HSB :
                    commandsCalled.addElement("Compare To HSB");

                    tempString = "";

                    while (tempString.length() == 0
                            || tempString.equals(jsb.toString())) {
                        tempString = getRandomString("");
                    }

                    compareZero =
                        hsb.compareTo(new HsqlStringBuffer(jsb.toString()));
                    compareNotZero1 =
                        hsb.compareTo(new HsqlStringBuffer(tempString));
                    compareNotZero2 = jsb.toString().compareTo(tempString);
                    tempString      = "";
                    break;

                case COMPARE_TO_STRING :
                    tempString = "";

                    commandsCalled.addElement("Compare To String");

                    while (tempString.length() == 0
                            || tempString.equals(jsb.toString())) {
                        tempString = getRandomString("");
                    }

                    compareZero     = hsb.compareTo(jsb.toString());
                    compareNotZero1 = hsb.compareTo(tempString);
                    compareNotZero2 = jsb.toString().compareTo(tempString);
                    tempString      = "";
                    break;

                case EQUALS :
                    commandsCalled.addElement("Equals");

                    tempString = "";

                    while (tempString.length() == 0
                            || tempString.equals(jsb.toString())) {
                        tempString = getRandomString("");
                    }

                    commandsCalled.addElement("Compare To String");

                    equalsTrue =
                        hsb.equals(new HsqlStringBuffer(jsb.toString()));
                    equalsFalse =
                        hsb.equals(new HsqlStringBuffer(tempString));
                    tempString = "";
                    break;

                case SET_LENGTH :
                    if (getRandomInt(0, 1) == 1) {    // set length to zero every other time set length is called
                        tempInt = 0;
                    } else {
                        tempInt = getRandomInt(1, (int) (hsb.length()
                                                         * 1.25));
                    }

                    commandsCalled.addElement("Set length " + tempInt);
                    jsb.setLength(tempInt);
                    hsb.setLength(tempInt);

                    if (!jsb.toString().equals(hsb.toString())) {
                        equalsTrue = false;
                    }
                    break;

/*
                case TO_QUOTED_STRING :

                    commandsCalled.addElement("To quoted string");

                    if (!hsb.toQuotedString().equals('\'' + jsb.toString()
                                                     + '\'')) {
                        equalsTrue = false;
                    }
                    break;
*/
                default :
                    continue;
            }

            if (sbException || hsbException) {
                if (!(sbException && hsbException)) {
                    System.out.println(
                        "Exception descrepancy with HSB and StringBuffer");
                    this.printCommandsCalled(commandsCalled);

                    //System.exit(0);
                }

                return;
            }

            if (equalsTrue == false || equalsFalse == true
                    || compareZero != 0
                    || compareNotZero1 != compareNotZero2) {
                System.out.println("Bug encountered in HSB");
                this.printCommandsCalled(commandsCalled);

                return;

                //System.exit(0);
            }

            compare(jsb, hsb);
        }
    }

    /** Compares a StringBuffer to an HSB */
    private void compare(StringBuffer jsb, HsqlStringBuffer hsb) {

        if (!jsb.toString().equals(hsb.toString())) {
            System.out.println("Error in HSB class");
            this.printCommandsCalled(commandsCalled);
            System.exit(0);
        }
    }

    /** Prints the list of commands called so far */
    public void printCommandsCalled(Vector commands) {

        int commandCode = 0;

        for (int i = 0; i < commands.size(); i++) {
            System.out.println((String) commands.elementAt(i));
        }

        System.out.flush();
    }

    /** Returns a random integer in the range of the lowBound and highBound */
    private int getRandomInt(int lowBound, int highBound) {

        double random = randomGenerator.nextDouble();

        return ((int) (((highBound - lowBound) * random) + .5)) + lowBound;
    }

    /**
     * Returns an Integer object with a value between Integer.MIN_VALUE and
     * Integer.MAX_VALUE
     */
    private Integer getRandomInteger() {
        return new Integer(getRandomInt(0, (int) (Integer.MAX_VALUE
                / 100.0)));
    }

    /** Gives a random character */
    private char getRandomChar() {
        return (char) getRandomInt(32, 122);
    }

    /** Returns a random string of chars with a length greater than 3 */
    private String getRandomString(String str) {

        String tmpString = str;

        if (str.length() > 75) {
            return str;
        }

        switch (getRandomInt(0, 1)) {

            case 1 :
                if (str.length() > 0) {
                    return getRandomString(tmpString);
                }
            case 0 :
                int stop = getRandomInt(3, 12);

                for (int i = 0; i < stop; i++) {
                    str = str + getRandomChar();
                }

                return str;

            default :
                System.out.println("Error in TestHSB");

                return null;
        }
    }

    public void resetTimer() {
        timer.resetCounter();
    }

    public void stopTimer() {
        timer.stopTimer();
    }

    public static void main(String[] args) {

        TestHSB test = new TestHSB();

        for (int i = 0; i < NUMBER_OF_TEST_RUNS; i++) {
            test.doTest();
            test.resetTimer();

            if (i % 1000 == 0) {
                if (i != 0) {
                    System.out.println("Finished " + i + " tests");
                    System.out.flush();
                }
            }
        }

        test.stopTimer();
        System.out.println(
            "After " + NUMBER_OF_TEST_RUNS + " tests of a maximum of "
            + NUMBER_OF_ITERATIONS_PER_RUN
            + " string buffer commands each test, the HSB tests passed");
    }

    private class Timer extends Thread {

        int     counter;
        TestHSB test;

        public Timer(TestHSB testHsb) {
            counter = 0;
            test    = testHsb;
        }

        public void run() {

            while (true) {
                try {
                    sleep(100);
                } catch (InterruptedException ex) {}

                synchronized (this) {
                    if (counter == -1) {
                        return;
                    }

                    counter += 100;
                }

                if (counter > 4000) {
                    System.out.println("Counter timout -------- ");
                    System.out.println("Command set last called: ");
                    printCommandsCalled(commandsCalled);
                    System.exit(0);
                }
            }
        }

        public synchronized void resetCounter() {
            counter = 0;
        }

        public synchronized void stopTimer() {
            counter = -1;
        }
    }
}
