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

import java.lang.reflect.Array;

/**
 * Collection of static methods for operations on arrays
 *
 * @author fredt@users
 * @version 1.7.2
 */
public class ArrayUtil {

    final public static int        CLASS_CODE_BYTE    = 'B';
    final public static int        CLASS_CODE_CHAR    = 'C';
    final public static int        CLASS_CODE_DOUBLE  = 'D';
    final public static int        CLASS_CODE_FLOAT   = 'F';
    final public static int        CLASS_CODE_INT     = 'I';
    final public static int        CLASS_CODE_LONG    = 'J';
    final public static int        CLASS_CODE_OBJECT  = 'L';
    final public static int        CLASS_CODE_SHORT   = 'S';
    final public static int        CLASS_CODE_BOOLEAN = 'Z';
    private static IntValueHashMap classCodeMap       = new IntValueHashMap();

    static {
        classCodeMap.put(byte.class, ArrayUtil.CLASS_CODE_BYTE);
        classCodeMap.put(char.class, ArrayUtil.CLASS_CODE_SHORT);
        classCodeMap.put(short.class, ArrayUtil.CLASS_CODE_SHORT);
        classCodeMap.put(int.class, ArrayUtil.CLASS_CODE_INT);
        classCodeMap.put(long.class, ArrayUtil.CLASS_CODE_LONG);
        classCodeMap.put(float.class, ArrayUtil.CLASS_CODE_FLOAT);
        classCodeMap.put(double.class, ArrayUtil.CLASS_CODE_DOUBLE);
        classCodeMap.put(boolean.class, ArrayUtil.CLASS_CODE_BOOLEAN);
        classCodeMap.put(Object.class, ArrayUtil.CLASS_CODE_OBJECT);
    }

    public static int getClassCode(Class cla) {

        if (!cla.isPrimitive()) {
            return ArrayUtil.CLASS_CODE_OBJECT;
        }

        return classCodeMap.get(cla, -1);
    }

    public static void clearArray(int type, Object data, int from, int to) {

        switch (type) {

            case ArrayUtil.CLASS_CODE_BYTE : {
                byte[] array = (byte[]) data;

                while (--to >= from) {
                    array[to] = 0;
                }

                return;
            }
            case ArrayUtil.CLASS_CODE_CHAR : {
                byte[] array = (byte[]) data;

                while (--to >= from) {
                    array[to] = 0;
                }

                return;
            }
            case ArrayUtil.CLASS_CODE_SHORT : {
                short[] array = (short[]) data;

                while (--to >= from) {
                    array[to] = 0;
                }

                return;
            }
            case ArrayUtil.CLASS_CODE_INT : {
                int[] array = (int[]) data;

                while (--to >= from) {
                    array[to] = 0;
                }

                return;
            }
            case ArrayUtil.CLASS_CODE_LONG : {
                long[] array = (long[]) data;

                while (--to >= from) {
                    array[to] = 0;
                }

                return;
            }
            case ArrayUtil.CLASS_CODE_FLOAT : {
                float[] array = (float[]) data;

                while (--to >= from) {
                    array[to] = 0;
                }

                return;
            }
            case ArrayUtil.CLASS_CODE_DOUBLE : {
                double[] array = (double[]) data;

                while (--to >= from) {
                    array[to] = 0;
                }

                return;
            }
            case ArrayUtil.CLASS_CODE_BOOLEAN : {
                boolean[] array = (boolean[]) data;

                while (--to >= from) {
                    array[to] = false;
                }

                return;
            }
            default : {
                Object[] array = (Object[]) data;

                while (--to >= from) {
                    array[to] = null;
                }

                return;
            }
        }
    }

    /**
     * Handles both addition and removal of rows
     */
    public static void adjustArray(int type, Object array, int usedElements,
                                   int index, int count) {

        if (index >= usedElements) {
            return;
        }

        int newCount = usedElements + count;
        int source;
        int target;
        int size;

        if (count >= 0) {
            source = index;
            target = index + count;
            size   = usedElements - index;
        } else {
            source = index - count;
            target = index;
            size   = usedElements - index + count;
        }

        if (size > 0) {
            System.arraycopy(array, source, array, target, size);
        }

        if (count < 0) {
            clearArray(type, array, newCount, usedElements);
        }
    }

    /**
     *   Basic sort for small arrays.
     */
    public static void sortArray(int array[]) {

        boolean swapped;

        do {
            swapped = false;

            for (int i = 0; i < array.length - 1; i++) {
                if (array[i] > array[i + 1]) {
                    int temp = array[i + 1];

                    array[i + 1] = array[i];
                    array[i]     = temp;
                    swapped      = true;
                }
            }
        } while (swapped);
    }

    /**
     *  Basic find for small arrays.
     */
    public static int find(Object array[], Object object) {

        for (int i = 0; i < array.length; i++) {
            if (array[i] == object) {

                // hadles both nulls
                return i;
            }

            if (object != null && object.equals(array[i])) {
                return i;
            }
        }

        return -1;
    }

    /**
     *  Basic find for small arrays.
     */
    public static int find(int array[], int value) {

        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }

        return -1;
    }

    /**
     *  Basic find for small arrays.
     */
    public static int findNot(int array[], int value) {

        for (int i = 0; i < array.length; i++) {
            if (array[i] != value) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Returns true if a and be contain the same set of integers, not
     * necessarily in the same order. This implies the arrays are of the same
     * length.
     */
    public static boolean areEqualSets(int[] arra, int[] arrb) {
        return arra.length == arrb.length
               && ArrayUtil.haveEqualSets(arra, arrb, arra.length);
    }

    /**
     * For full == true returns true if arra and arrb are identical (have the
     * same length and contain the same integers in the same sequence).
     *
     * For full == false returns the result
     * of haveEqualArrays(arra,arrb,count)
     *
     * For full == true, the array lengths must be the same as count
     *
     */
    public static boolean areEqual(int[] arra, int[] arrb, int count,
                                   boolean full) {

        if (ArrayUtil.haveEqualArrays(arra, arrb, count)) {
            if (full) {
                return arra.length == arrb.length && count == arra.length;
            }

            return true;
        }

        return false;
    }

    /**
     * Returns true if the first count elements of arra and arrb are identical
     * sets of integers (not necessarily in the same order).
     *
     */
    public static boolean haveEqualSets(int[] arra, int[] arrb, int count) {

        if (count > arra.length || count > arrb.length) {
            return false;
        }

        if (count == 1) {
            return arra[0] == arrb[0];
        }

        int[] tempa = (int[]) resizeArray(arra, count);
        int[] tempb = (int[]) resizeArray(arrb, count);

        sortArray(tempa);
        sortArray(tempb);

        for (int j = 0; j < count; j++) {
            if (tempa[j] != tempb[j]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns true if the first count elements of arra and arrb are identical
     * subarrays of integers
     *
     */
    public static boolean haveEqualArrays(int[] arra, int[] arrb, int count) {

        if (count > arra.length || count > arrb.length) {
            return false;
        }

        for (int j = 0; j < count; j++) {
            if (arra[j] != arrb[j]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns true if the first count elements of arra and arrb are identical
     * subarrays of Objects
     *
     */
    public static boolean haveEqualArrays(Object[] arra, Object[] arrb,
                                          int count) {

        if (count > arra.length || count > arrb.length) {
            return false;
        }

        for (int j = 0; j < count; j++) {
            if (arra[j] != arrb[j]) {
                if (arra[j] == null ||!arra[j].equals(arrb[j])) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Returns true if a and the first bcount elements of arrb share any element.
     *
     * Used for checks for any overlap between two arrays of column indexes.
     */
    public static boolean haveCommonElement(int[] arra, int[] arrb,
            int bcount) {

        for (int i = 0; i < arra.length; i++) {
            int c = arra[i];

            for (int j = 0; j < bcount; j++) {
                if (c == arrb[j]) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns an int[] containing elements shared between the two arrays
     * arra and arrb. The arrays contain sets (no value is repeated).
     *
     * Used to find the overlap between two arrays of column indexes.
     * Ordering of the result arrays will be the same as in array
     * a. The method assumes that each index is only listed
     * once in the two input arrays.
     * <p>
     * e.g.
     * </p>
     * <code>
     * <table width="90%" bgcolor="lightblue">
     * <tr><td colspane="3">The arrays</td></tr>
     * <tr><td>int []arra</td><td>=</td><td>{2,11,5,8}</td></tr>
     * <tr><td>int []arrb</td><td>=</td><td>{20,8,10,11,28,12}</td></tr>
     * <tr><td colspane="3">will result in:</td></tr>
     * <tr><td>int []arrc</td><td>=</td><td>{11,8}</td></tr>
     * </table>
     *
     * @param arra int[]; first column indexes
     *
     * @param arrb int[]; second column indexes
     *
     * @return int[] common indexes or <code>null</code> if there is no overlap.
     *
     * @short Return the overlap between two arrays of column indexes.
     */
    public static int[] commonElements(int[] arra, int[] arrb) {

        int[] c = null;
        int   n = countCommonElements(arra, arrb);

        if (n > 0) {
            c = new int[n];

            int k = 0;

            for (int i = 0; i < arra.length; i++) {
                for (int j = 0; j < arrb.length; j++) {
                    if (arra[i] == arrb[j]) {
                        c[k++] = arra[i];
                    }
                }
            }
        }

        return c;
    }

    /**
     * Returns the number of elements shared between the two arrays containing
     * sets.<p>
     *
     * Return the number of elements shared by two column index arrays.
     * This method assumes that each of these arrays contains a set (each
     * element index is listed only once in each index array). Otherwise the
     * returned number will NOT represent the number of unique column indexes
     * shared by both index array.
     *
     * @param arra int[]; first array of column indexes.
     *
     * @param arrb int[]; second array of column indexes
     *
     * @return int; number of elements shared by <code>a</code> and <code>b</code>
     */
    public static int countCommonElements(int[] arra, int[] arrb) {

        int k = 0;

        for (int i = 0; i < arra.length; i++) {
            for (int j = 0; j < arrb.length; j++) {
                if (arra[i] == arrb[j]) {
                    k++;
                }
            }
        }

        return k;
    }

    /**
     * Returns the count of elements in arra from position start that are
     * sequentially equal to the elements of arrb.
     */
    public static int countSameElements(byte[] arra, int start, byte[] arrb) {

        int k     = 0;
        int limit = arra.length - start;

        if (limit > arrb.length) {
            limit = arrb.length;
        }

        for (int i = 0; i < limit; i++) {
            if (arra[i + start] == arrb[i]) {
                k++;
            } else {
                break;
            }
        }

        return k;
    }

    /**
     * Returns true if arra from position start contains all elements of arrb
     * in sequential order.
     */
    public static boolean containsAt(byte[] arra, int start, byte arrb[]) {
        return countSameElements(arra, start, arrb) == arrb.length;
    }

    /**
     * Returns the count of elements in arra from position start that are
     * among the elements of arrb. Stop at any element not in arrb
     *
     */
    public static int countStartElementsAt(byte[] arra, int start,
                                           byte arrb[]) {

        int k = 0;

        mainloop:
        for (int i = start; i < arra.length; i++) {
            for (int j = 0; j < arrb.length; j++) {
                if (arra[i] == arrb[j]) {
                    k++;

                    continue mainloop;
                }
            }

            break;
        }

        return k;
    }

    /**
     * Returns the count of elements in arra from position start that are not
     * among the the elements of arrb.
     *
     */
    public static int countNonStartElementsAt(byte[] arra, int start,
            byte arrb[]) {

        int k = 0;

        mainloop:
        for (int i = start; i < arra.length; i++) {
            for (int j = 0; j < arrb.length; j++) {
                if (arra[i] == arrb[j]) {
                    break mainloop;
                }
            }

            k++;
        }

        return k;
    }

    /**
     * Convenience wrapper for System.arraycopy()
     */
    public static void copyArray(Object source, Object dest, int count) {
        System.arraycopy(source, 0, dest, 0, count);
    }

    /**
     * Returns a range of elements of source from start to end of the array.
     */
    public static int[] arraySlice(int[] source, int start, int count) {

        int[] slice = new int[count];

        System.arraycopy(source, start, slice, 0, count);

        return slice;
    }

    /**
     * Fills the array with a value
     */
    public static void fillArray(Object[] array, Object value) {

        int to = array.length;

        while (--to >= 0) {
            array[to] = value;
        }
    }

    /**
     * Returns a duplicates of an array.
     */
    public static Object duplicateArray(Object source) {

        int size = Array.getLength(source);
        Object newarray =
            Array.newInstance(source.getClass().getComponentType(), size);

        System.arraycopy(source, 0, newarray, 0, size);

        return newarray;
    }

    /**
     * Returns a new array of given size, containing as many elements of
     * the original array as it can hold. N.B. Always returns a new array
     * even if newsize parameter is the same as the old size.
     */
    public static Object resizeArray(Object source, int newsize) {

        Object newarray =
            Array.newInstance(source.getClass().getComponentType(), newsize);
        int oldsize = Array.getLength(source);

        if (oldsize < newsize) {
            newsize = oldsize;
        }

        System.arraycopy(source, 0, newarray, 0, newsize);

        return newarray;
    }

    /**
     *  Copies elements of source to dest. If adjust is -1 the element at
     *  colindex is not copied. If adjust is +1 that element is filled with
     *  the Object addition. All the rest of the elements in source are
     *  shifted left or right accordingly when they are copied.
     *
     *  No checks are perfomed on array sizes and an exception is thrown
     *  if they are not consistent with the other arguments.
     *
     * @param  source
     * @param  dest
     * @param  addition
     * @param colindex
     * @param  adjust +1 or 0 or -1
     * return new, adjusted array or null if an element is removed
     */
    public static void copyAdjustArray(Object[] source, Object[] dest,
                                       Object addition, int colindex,
                                       int adjust) {

        int i;

        for (i = 0; i < colindex; i++) {
            dest[i] = source[i];
        }

        if (i == dest.length) {
            return;
        }

        if (adjust < 0) {
            i++;
        } else {
            dest[i] = addition;
        }

        for (; i < source.length; i++) {
            dest[i + adjust] = source[i];
        }
    }

    /**
     * Returns a new array with the elements in collar adjusted to reflect
     * changes at colindex.
     *
     * Each element in collarr represents an index into another array
     * otherarr.
     * colindex is the index at which an element is added or removed form
     * otherarr. Each element in the result array represents the new,
     * adjusted index to otherarr.
     * For each element of collarr that represents an index equal to
     * colindex and adjust is -1, the result will not contain that element
     * and will be shorter than collar by one element.
     *
     *
     * @param  colarr
     * @param  colindex
     * @param  adjust +1 or 0 or -1
     * @return new, adjusted array
     */
    public static int[] toAdjustedColumnArray(int[] colarr, int colindex,
            int adjust) {

        if (colarr == null) {
            return null;
        }

        int[] intarr = new int[colarr.length];
        int   j      = 0;

        for (int i = 0; i < colarr.length; i++) {
            if (colarr[i] > colindex) {
                intarr[j] = colarr[i] + adjust;

                j++;
            } else if (colarr[i] == colindex) {
                if (adjust < 0) {

                    // skip an element from colarr
                } else {
                    intarr[j] = colarr[i] + adjust;

                    j++;
                }
            } else {
                intarr[j] = colarr[i];

                j++;
            }
        }

        if (colarr.length != j) {
            int[] newarr = new int[j];

            copyArray(intarr, newarr, j);

            return newarr;
        }

        return intarr;
    }

    /**
     *  Copies some elements of row into colobject by using colindex as the
     *  list of indexes into row. colindex and colobject are of equal length
     *  and normally shorter than row;
     *
     */
    public static void copyColumnValues(Object row[], int colindex[],
                                        Object colobject[]) {

        for (int i = 0; i < colindex.length; i++) {
            colobject[i] = row[colindex[i]];
        }
    }
/*
    public static void main(String[] args) {

        int[] a = new int[] {
            23, 11, 37, 7, 1, 5
        };
        int[] b = new int[] {
            1, 3, 7, 11, 13, 17, 19, 3, 1
        };
        int[] c = toAdjustedColumnArray(a, 7, -1);
        int[] d = toAdjustedColumnArray(b, 11, 1);
        int[] e = new int[a.length];

        copyArray(a, e, a.length);
        sortArray(e);

        int[] f = new int[b.length];

        copyArray(b, f, b.length);
        sortArray(f);

        boolean x = haveEqualSets(a, e, a.length);
        boolean y = haveEqualSets(b, f, b.length);

        System.out.print("test passed: ");
        System.out.print(x == true && y == true && c.length == a.length - 1
                         && d.length == b.length);
    }
*/
}
