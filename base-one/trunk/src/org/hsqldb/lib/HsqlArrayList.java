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


package org.hsqldb.lib;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * Intended as an asynchronous alternative to Vector.  Use HsqlLinkedList
 * instead if its better suited.
 *
 * @author dnordahl@users
 * @version 6/11/2002
 */
public class HsqlArrayList implements HsqlList {

//fredt@users
    private static Reporter reporter = new Reporter();

    private static class Reporter {

        private static int initCounter   = 0;
        private static int updateCounter = 0;

        Reporter() {

            try {
                System.runFinalizersOnExit(true);
            } catch (SecurityException e) {}
        }

        protected void finalize() {

            System.out.println("HsqlArrayList init count: " + initCounter);
            System.out.println("HsqlArrayList update count: "
                               + updateCounter);
        }
    }

    private static final int   DEFAULT_INITIAL_CAPACITY = 10;
    private static final float DEFAULT_RESIZE_FACTOR    = 2.0f;
    private Object[]           elementData;
    private int                elementCount;

    /** Creates a new instance of HsqlArrayList */
    public HsqlArrayList() {

        reporter.initCounter++;

        elementCount = 0;
        elementData  = new Object[DEFAULT_INITIAL_CAPACITY];
    }

    /** Creates a new instance with the given initial capacity */
    public HsqlArrayList(int initialCapacity) {

        reporter.initCounter++;

        if (initialCapacity < 0) {
            throw new NegativeArraySizeException(
                "Invalid initial capacity given");
        }

        if (initialCapacity == 0) {
            elementData = new Object[1];
        } else {
            elementData = new Object[initialCapacity];
        }

        elementCount = 0;
    }

    /** Inserts an element at the given index */
    public void add(int index, Object element) {

        reporter.updateCounter++;

        if (index > elementCount) {
            throw new IndexOutOfBoundsException("Index out of bounds: "
                                                + index + ">" + elementCount);
        }

        if (index < 0) {
            throw new IndexOutOfBoundsException("Index out of bounds: "
                                                + index + " < 0");
        }

        if (elementCount >= elementData.length) {
            increaseCapacity();
        }

        for (int i = elementCount; i > index; i--) {
            elementData[i] = elementData[i - 1];
        }

        elementData[index] = element;

        elementCount++;
    }

    /** Appends an element to the end of the list */
    public boolean add(Object element) {

        reporter.updateCounter++;

        if (elementCount >= elementData.length) {
            increaseCapacity();
        }

        elementData[elementCount] = element;

        elementCount++;

        return true;
    }

    /** Gets the element at given position */
    public Object get(int index) {

        if (index >= elementCount) {
            throw new IndexOutOfBoundsException("Index out of bounds: "
                                                + index + " >= "
                                                + elementCount);
        }

        if (index < 0) {
            throw new IndexOutOfBoundsException("Index out of bounds: "
                                                + index + " < 0");
        }

        return elementData[index];
    }

    /** Removes and returns the element at given position */
    public Object remove(int index) {

        if (index >= elementCount) {
            throw new IndexOutOfBoundsException("Index out of bounds: "
                                                + index + " >= "
                                                + elementCount);
        }

        if (index < 0) {
            throw new IndexOutOfBoundsException("Index out of bounds: "
                                                + index + " < 0");
        }

        Object removedObj = elementData[index];

        for (int i = index; i < elementCount - 1; i++) {
            elementData[i] = elementData[i + 1];
        }

        elementCount--;

        elementData[elementCount] = null;

        return removedObj;
    }

    /** Replaces the element at given position */
    public Object set(int index, Object element) {

        if (index >= elementCount) {
            throw new IndexOutOfBoundsException("Index out of bounds: "
                                                + index + " >= "
                                                + elementCount);
        }

        if (index < 0) {
            throw new IndexOutOfBoundsException("Index out of bounds: "
                                                + index + " < 0");
        }

        Object replacedObj = elementData[index];

        elementData[index] = element;

        return replacedObj;
    }

    /** Returns the number of elements in the array list */
    public final int size() {
        return elementCount;
    }

    private void increaseCapacity() {

        Object[] newArray =
            new Object[(int) (elementData.length * DEFAULT_RESIZE_FACTOR)];

        System.arraycopy(elementData, 0, newArray, 0, elementData.length);

        elementData = newArray;
        newArray    = null;
    }

    /**
     * Returns an Enumeration of the elements of the list.  The Enumerator will
     * NOT throw a concurrent modification exception if the list is modified
     * during the enumeration.
     */
    public Enumeration elements() {

        Enumeration enum = new Enumeration() {

            private int pos = 0;

            public Object nextElement() {

                if (!hasMoreElements()) {
                    throw new NoSuchElementException("Enumeration complete");
                }

                pos++;

                return elementData[pos - 1];
            }

            public boolean hasMoreElements() {
                return (pos < elementCount);
            }
        };

        return enum;
    }

    /** Trims the array to be the same size as the number of elements. */
    public void trim() {

        Object[] newArray = null;

        if (elementCount == 0) {
            newArray = new Object[1];
        } else {
            newArray = new Object[elementCount];
        }

        System.arraycopy(elementData, 0, newArray, 0, elementCount);

        elementData = newArray;
        newArray    = null;
    }

    /** Returns a string representation */
    public String toString() {

        StringBuffer sb = new StringBuffer(32);

        sb.append("HsqlArrayList : size=");
        sb.append(elementCount);
        sb.append(' ');
        sb.append('[');

        Enumeration enum = elements();

        while (enum.hasMoreElements()) {
            sb.append(enum.nextElement());

            if (enum.hasMoreElements()) {
                sb.append(',');
                sb.append(' ');
            }
        }

        sb.append(']');

        return sb.toString();
    }

    // fredt@users - no tests etc.
    public boolean isEmpty() {
        return elementCount == 0;
    }

    public void clear() {

        for (int i = 0; i < elementCount; i++) {
            elementData[i] = null;
        }

        elementCount = 0;
    }

    public void setSize(int newSize) {

        if (newSize < elementCount) {
            for (int i = newSize; i < elementCount; i++) {
                elementData[i] = null;
            }
        }

        elementCount = newSize;

        for (; elementCount > elementData.length; ) {
            increaseCapacity();
        }
    }

// fredt@users - temp - straight copy from java.util.ArrayList
    public Object[] toArray(Object a[]) {

        if (a.length < elementCount) {
            a = (Object[]) java.lang.reflect.Array.newInstance(
                a.getClass().getComponentType(), elementCount);
        }

        System.arraycopy(elementData, 0, a, 0, elementCount);

        if (a.length > elementCount) {
            a[elementCount] = null;
        }

        return a;
    }

// fredt@users - temp - won't need this when we have the hybrid map / list
    public void remove(Object obj) {

        for (int i = 0; i < elementCount; i++) {
            if ((obj == null && elementData[i] == null)
                    || obj.equals(elementData[i])) {
                remove(i);

                return;
            }
        }
    }
}
