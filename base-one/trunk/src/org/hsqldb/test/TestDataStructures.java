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


package org.hsqldb.test;

import java.util.*;
import org.hsqldb.lib.*;
import junit.framework.*;

/**
 * Randomly excutes methods on the HsqlList data structures and compares the
 * results with equivalent calls on the java vector class.
 *
 * @author dnordahl@users
 */
public class TestDataStructures extends TestCase {

    private final static int NUMBER_OF_TEST_RUNS          = 100000;
    private final static int NUMBER_OF_ITERATIONS_PER_RUN = 80;
    private Random           randomGenerator;

    //Commands
    private final static int ADD        = 1;
    private final static int ADD_AT     = 2;
    private final static int GET        = 3;
    private final static int REMOVE     = 4;
    private final static int SET        = 5;
    private final static int OPTIMIZE   = 6;
    private final static int REMOVE_ALL = 7;
    private Vector           listCommandsCalled;

    /** Creates a new instance of TestDataStructures */
    public TestDataStructures() {
        randomGenerator    = new Random(System.currentTimeMillis());
        listCommandsCalled = new Vector(NUMBER_OF_ITERATIONS_PER_RUN);
    }

    /** Runs a test on the hsqldb lists */
    public void testLists() {

        HsqlArrayList  arrayList  = new HsqlArrayList();
        HsqlLinkedList linkedList = new HsqlLinkedList();
        Vector         vector     = new Vector();
        Vector listCommandsCalled = new Vector(NUMBER_OF_ITERATIONS_PER_RUN);
        Integer        tempInt    = null;
        int            tempCommandCode;
        int            tempPosition;
        boolean        arrayListException  = false;
        boolean        linkedListException = false;
        boolean        vectorException     = false;
        Object         arrayListObject     = null;
        Object         linkedListObject    = null;
        Object         vectorObject        = null;

        for (int i = 0; i < getRandomInt(3, 12); i++) {    // prime the contents with a couple items
            tempInt = getRandomInteger();

            arrayList.add(tempInt);
            linkedList.add(tempInt);
            vector.addElement(tempInt);
            listCommandsCalled.addElement("Add");
        }

        compareLists(arrayList, linkedList, vector);

        for (int j = 0; j < NUMBER_OF_ITERATIONS_PER_RUN; j++) {
            tempCommandCode = getRandomInt(0, 15);    // 0 and 8 are ignored or used for a special op

            switch (tempCommandCode) {

                case ADD :
                    tempInt = getRandomInteger();

                    listCommandsCalled.addElement("Add");
                    arrayList.add(tempInt);
                    linkedList.add(tempInt);
                    vector.addElement(tempInt);
                    break;

                case ADD_AT :
                    tempInt      = getRandomInteger();
                    tempPosition = getRandomInt(0, vector.size() + 1);

                    listCommandsCalled.addElement("Add at " + tempPosition);

                    try {
                        arrayList.add(tempPosition, tempInt);
                    } catch (Exception ex) {
                        arrayListException = true;
                    }

                    try {
                        linkedList.add(tempPosition, tempInt);
                    } catch (Exception ex) {
                        linkedListException = true;
                    }

                    try {
                        vector.insertElementAt(tempInt, tempPosition);
                    } catch (Exception ex) {
                        vectorException = true;
                    }
                    break;

                case GET :
                    tempPosition = getRandomInt(0, vector.size() + 1);

                    listCommandsCalled.addElement("Get " + tempPosition);

                    try {
                        arrayListObject = arrayList.get(tempPosition);
                    } catch (Exception ex) {
                        arrayListException = true;
                    }

                    try {
                        linkedListObject = linkedList.get(tempPosition);
                    } catch (Exception ex) {
                        linkedListException = true;
                    }

                    try {
                        vectorObject = vector.elementAt(tempPosition);
                    } catch (Exception ex) {
                        vectorException = true;
                    }
                    break;

                case REMOVE :
                    tempPosition = getRandomInt(0, vector.size() + 1);

                    listCommandsCalled.addElement("Remove " + tempPosition);

                    try {
                        arrayListObject = arrayList.remove(tempPosition);
                    } catch (Exception ex) {
                        arrayListException = true;
                    }

                    try {
                        linkedListObject = linkedList.remove(tempPosition);
                    } catch (Exception ex) {
                        linkedListException = true;
                    }

                    try {
                        vectorObject = vector.elementAt(tempPosition);

                        vector.removeElementAt(tempPosition);
                    } catch (Exception ex) {
                        vectorException = true;
                    }
                    break;

                case SET :
                    tempInt      = getRandomInteger();
                    tempPosition = getRandomInt(0, vector.size() + 1);

                    listCommandsCalled.addElement("Set " + tempPosition);

                    try {
                        arrayList.set(tempPosition, tempInt);
                    } catch (Exception ex) {
                        arrayListException = true;
                    }

                    try {
                        linkedList.set(tempPosition, tempInt);
                    } catch (Exception ex) {
                        linkedListException = true;
                    }

                    try {
                        vector.setElementAt(tempInt, tempPosition);
                    } catch (Exception ex) {
                        vectorException = true;
                    }
                    break;

                case OPTIMIZE :
                    listCommandsCalled.addElement("Optimize");
                    arrayList.trim();
                    vector.trimToSize();
                    break;

                case REMOVE_ALL :
                    if (getRandomInt(0, 5) == 4) {    // to limit the frequency of this call
                        listCommandsCalled.addElement("Remove all");

                        if (vector.size() == 0) {
                            break;
                        }

                        for (int k = arrayList.size() - 1; k >= 0; k--) {
                            arrayList.remove(k);
                            linkedList.remove(k);
                        }

                        vector.removeAllElements();
                    }
                    break;

                default :
            }

            if (arrayListException || linkedListException
                    || vectorException) {

                // if an exception is thrown in vector but not one of the lists or vice versa
                if (!(arrayListException && linkedListException
                        && vectorException)) {
                    if (!(arrayListException && vectorException)) {
                        System.out.println(
                            "Exception descrepancy with vector and arraylist");
                    } else if (!(linkedListException && vectorException)) {
                        System.out.println(
                            "Exception descrepancy with vector and linkedlist");
                    } else {
                        System.out.println("Error in TestDataStructures");
                    }

                    this.printListCommandsCalled(listCommandsCalled);
                    fail("test failed");

                    //System.exit(0);
                }

                return;
            }

            if (!objectEquals(linkedListObject, arrayListObject,
                              vectorObject)) {
                System.out.println("Objects returned inconsistent");
                this.printListCommandsCalled(listCommandsCalled);
                fail("test failed");

                //System.exit(0);
            }

            compareLists(arrayList, linkedList, vector);
        }
    }

    /**
     * Compare contents of lists to the vector.  Print out stuff if they are
     * inconsistent and exit.
     */
    public void compareLists(HsqlArrayList arrayList,
                             HsqlLinkedList linkedList, Vector vector) {

        boolean arrayListError  = false;
        boolean linkedListError = false;

        if (!equalsVector(arrayList, vector)) {
            System.out.println("Error in array list implementation");

            arrayListError = true;
        }

        if (!equalsVector(linkedList, vector)) {
            System.out.println("Error in linked list implementation");

            linkedListError = true;
        }

        if (arrayListError || linkedListError) {
            this.printListCommandsCalled(listCommandsCalled);
            System.out.flush();
            fail("test failed");
            System.exit(0);
        }
    }

    /** Prints the list of commands called so far */
    public void printListCommandsCalled(Vector commands) {

        int commandCode = 0;

        for (int i = 0; i < commands.size(); i++) {
            System.out.println((String) commands.elementAt(i));
        }

        System.out.flush();
    }

    /** Returns whether three objects are equal */
    private boolean objectEquals(Object lObject, Object aObject,
                                 Object vObject) {

        if (lObject == null && aObject == null && vObject == null) {
            return true;
        }

        try {
            if (!lObject.equals(vObject)) {
                System.out.println("LinkList object returned inconsistent");

                return false;
            } else if (!aObject.equals(vObject)) {
                System.out.println("ArrayList object returned inconsistent");

                return false;
            } else {
                return true;
            }
        } catch (NullPointerException ex) {
            return false;
        }
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

    /** Tells whether the given list contains the same data as the vector */
    private boolean equalsVector(HsqlList list, Vector vector) {

        if (list.size() != vector.size()) {
            return false;
        }

        org.hsqldb.lib.Iterator listElements   = list.iterator();
        Enumeration             vectorElements = vector.elements();
        Object                  listObj        = null;
        Object                  vectorObj      = null;

        while (listElements.hasNext()) {
            listObj   = listElements.next();
            vectorObj = vectorElements.nextElement();

            if (!listObj.equals(vectorObj)) {
                return false;
            }
        }

        return true;
    }

    public static void main(String[] args) {

        TestDataStructures test = new TestDataStructures();

        for (int i = 0; i < NUMBER_OF_TEST_RUNS; i++) {
            test.testLists();

            if (i % 1000 == 0) {
                System.out.println("Finished " + i + " tests");
                System.out.flush();
            }
        }

        System.out.println(
            "After " + NUMBER_OF_TEST_RUNS + " tests of a maximum of "
            + NUMBER_OF_ITERATIONS_PER_RUN
            + " list commands each test, the list tests passed");
        test.testGrowth();
    }

    public void testGrowth() {

        HsqlArrayList d = new HsqlArrayList();

        for (int i = 0; i < 12; i++) {
            d.add(new Integer(i));
        }

        for (int i = 0; i < d.size(); i++) {
            System.out.println(d.get(i));
        }

        d = new HsqlArrayList();

        for (int i = 0; i < 12; i++) {
            d.add(new Integer(i));
        }

        d.set(11, new Integer(11));

        for (int i = 0; i < d.size(); i++) {
            System.out.println(d.get(i));
        }

        org.hsqldb.lib.Iterator it = d.iterator();

        for (int i = 0; it.hasNext(); i++) {
            Integer value = (Integer) it.next();

            System.out.println(value);
            assertEquals(i, value.intValue());
        }

        //-
        assertEquals(12, d.size());
    }
}
