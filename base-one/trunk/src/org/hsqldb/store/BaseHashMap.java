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


package org.hsqldb.store;

import java.util.NoSuchElementException;

public class BaseHashMap {

/*
   array of primitive | array of objects
   objects support : compare, equals

   map:
   hashTable: fixed size int[] array for hash lookup into keyTable
   objectKeyTable: variable size Object[] array for values OR
   intKeyTable: variable size int[] for values
   linkTable: pointer to the next key ; same size as keyTable
   objectValueTable: Object[] array for values
   intValueTable: int[] array for values

   variable size int[] array for access count
*/

    //
    boolean isIntKey;
    boolean isLongKey;
    boolean isObjectKey;
    boolean isNoValue;
    boolean isIntValue;
    boolean isObjectValue;

    //
    protected int[]     intKeyTable;
    protected Object[]  objectKeyTable;
    protected long[]    longKeyTable;
    protected int[]     intValueTable;
    protected Object[]  objectValueTable;
    int[]               accessTable;
    float               loadFactor;
    int                 threshold;
    boolean             hasZeroKey;
    int                 zeroKeyIndex;
    protected HashIndex hashIndex;

    // experimental
    protected boolean clearWhenFull;

    public interface keyOrValueTypes {

        int noKeyOrValue     = 0;
        int intKeyOrValue    = 1;
        int longKeyOrValue   = 2;
        int objectKeyOrValue = 3;
    }

    public static final int noPurge = 0;
    public static final int purgeAll = 1;
    public static final int purgeAlternateHalf = 2;
    public static final int purgeOldAccessHalf = 3;

    protected BaseHashMap(int initialCapacity, int maxCapacity, int purgePolicy) throws IllegalArgumentException {
        this(initialCapacity, 1,
        BaseHashMap.keyOrValueTypes.objectKeyOrValue,
        BaseHashMap.keyOrValueTypes.noKeyOrValue, purgePolicy);
    }


    protected BaseHashMap(int initialCapacity, float loadFactor, int keyType,
                          int valueType, int purgePolicy) throws IllegalArgumentException {

        if (initialCapacity <= 0 || loadFactor <= 0.0) {
            throw new IllegalArgumentException();
        }

        this.loadFactor = loadFactor;
        threshold       = (int) (initialCapacity * loadFactor);
        hashIndex       = new HashIndex(initialCapacity, threshold, true);

        int arraySize = threshold;

        if (keyType == keyOrValueTypes.intKeyOrValue) {
            isIntKey    = true;
            intKeyTable = new int[arraySize];
        } else if (keyType == keyOrValueTypes.objectKeyOrValue) {
            isObjectKey    = true;
            objectKeyTable = new Object[arraySize];
        } else {
            isLongKey    = true;
            longKeyTable = new long[arraySize];
        }

        if (valueType == keyOrValueTypes.intKeyOrValue) {
            isIntValue    = true;
            intValueTable = new int[arraySize];
        } else if (valueType == keyOrValueTypes.objectKeyOrValue) {
            isObjectValue    = true;
            objectValueTable = new Object[arraySize];
        } else {
            isNoValue = true;
        }

        this.clearWhenFull = (purgePolicy == purgeAll);
    }

    protected Object getObject(int key) {

        int lookup = getLookup(key);

        if (lookup != -1) {
            return objectValueTable[lookup];
        }

        return null;
    }

    protected Object getObject(long key) {

        int lookup = getLookup(key);

        if (lookup != -1) {
            return objectValueTable[lookup];
        }

        return null;
    }

    protected Object getObject(Object key) {

        int hash   = key.hashCode();
        int lookup = getLookup(key, hash);

        if (lookup != -1) {
            return objectValueTable[lookup];
        }

        return null;
    }

    protected int getInt(int key) throws NoSuchElementException {

        int lookup = getLookup(key);

        if (lookup != -1) {
            return intValueTable[lookup];
        }

        throw new NoSuchElementException();
    }

    protected int getInt(int key, int defaultValue) {

        int lookup = getLookup(key);

        if (lookup != -1) {
            return intValueTable[lookup];
        }

        return defaultValue;
    }

    protected boolean getInt(int key, int[] value) {

        int lookup = getLookup(key);

        if (lookup != -1) {
            value[0] = intValueTable[lookup];

            return true;
        }

        return false;
    }

    protected int getInt(Object key) throws NoSuchElementException {

        int hash   = key.hashCode();
        int lookup = getLookup(key, hash);

        if (lookup != -1) {
            return intValueTable[lookup];
        }

        throw new NoSuchElementException();
    }

    protected int getInt(Object key, int defaultValue) {

        int hash   = key.hashCode();
        int lookup = getLookup(key, hash);

        if (lookup != -1) {
            return intValueTable[lookup];
        }

        return defaultValue;
    }

    protected boolean getInt(Object key, int[] value) {

        int hash   = key.hashCode();
        int lookup = getLookup(key, hash);

        if (lookup != -1) {
            value[0] = intValueTable[lookup];

            return true;
        }

        return false;
    }

    protected int getLookup(Object key, int hash) {

        int    lookup = hashIndex.getLookup(hash);
        Object tempKey;

        for (; lookup >= 0; lookup = hashIndex.getNextLookup(lookup)) {
            tempKey = objectKeyTable[lookup];

            if (key.equals(tempKey)) {
                return lookup;
            }
        }

        return lookup;
    }

    protected int getLookup(int key) {

        int lookup = hashIndex.getLookup(key);
        int tempKey;

        for (; lookup >= 0; lookup = hashIndex.getNextLookup(lookup)) {
            tempKey = intKeyTable[lookup];

            if (key == tempKey) {
                return lookup;
            }
        }

        return lookup;
    }

    protected int getLookup(long key) {

        int  lookup = hashIndex.getLookup((int) key);
        long tempKey;

        for (; lookup >= 0; lookup = hashIndex.getNextLookup(lookup)) {
            tempKey = longKeyTable[lookup];

            if (key == tempKey) {
                return lookup;
            }
        }

        return lookup;
    }

    protected Object addOrRemove(long longKey, int intValue,
                                 Object objectKey, Object objectValue,
                                 boolean remove) {

        int    hash        = isObjectKey ? objectKey.hashCode()
                                         : (int) longKey;
        int    index       = hashIndex.getHashIndex(hash);
        int    lookup      = hashIndex.hashTable[index];
        int    lastLookup  = -1;
        Object returnValue = null;

        for (; lookup >= 0;
                lastLookup = lookup,
                lookup = hashIndex.getNextLookup(lookup)) {
            if (isObjectKey) {
                if (objectKeyTable[lookup].equals(objectKey)) {
                    break;
                }
            } else if (isIntKey) {
                if (longKey == intKeyTable[lookup]) {
                    break;
                }
            } else if (isLongKey) {
                if (longKey == longKeyTable[lookup]) {
                    break;
                }
            }
        }

        if (lookup >= 0) {
            if (remove) {
                if (isObjectKey) {
                    objectKeyTable[lookup] = null;
                } else if (longKey == 0) {
                    hasZeroKey = false;
                } else if (isIntKey) {
                    intKeyTable[lookup] = 0;
                } else {
                    longKeyTable[lookup] = 0;
                }

                if (isObjectValue) {
                    returnValue              = objectValueTable[lookup];
                    objectValueTable[lookup] = null;
                } else if (isIntValue) {
                    intValueTable[lookup] = 0;
                }

                hashIndex.unlinkNode(index, lastLookup, lookup);

                return returnValue;
            }

            if (isObjectValue) {
                returnValue              = objectValueTable[lookup];
                objectValueTable[lookup] = objectValue;
            } else if (isIntValue) {
                intValueTable[lookup] = intValue;
            }

            return returnValue;
        }

        // not found
        if (remove) {
            return returnValue;
        }

        if (hashIndex.elementCount >= threshold) {
            if (clearWhenFull) {
                clear();
            } else {
                rehash(hashIndex.hashTable.length * 2);
            }

            return addOrRemove(longKey, intValue, objectKey, objectValue,
                               remove);
        }

        lookup = hashIndex.linkNode(index, lastLookup);

        // type dependent block
        if (isObjectKey) {
            objectKeyTable[lookup] = objectKey;
        } else if (isIntKey) {
            intKeyTable[lookup] = (int) longKey;

            if (longKey == 0) {
                hasZeroKey   = true;
                zeroKeyIndex = lookup;
            }
        } else if (isLongKey) {
            longKeyTable[lookup] = longKey;

            if (longKey == 0) {
                hasZeroKey   = true;
                zeroKeyIndex = lookup;
            }
        }

        if (isObjectValue) {
            objectValueTable[lookup] = objectValue;
        } else if (isIntValue) {
            intValueTable[lookup] = intValue;
        }

        //
        return returnValue;
    }

    protected Integer getOrAddInteger(int intKey) {

        Integer testValue;
        int     index      = hashIndex.getHashIndex(intKey);
        int     lookup     = hashIndex.hashTable[index];
        int     lastLookup = -1;

        for (; lookup >= 0;
                lastLookup = lookup,
                lookup = hashIndex.getNextLookup(lookup)) {
            testValue = (Integer) objectKeyTable[lookup];

            if (testValue.intValue() == intKey) {
                return testValue;
            }
        }

        if (hashIndex.elementCount >= threshold) {
            if (clearWhenFull) {
                clear();
            } else {
                rehash(hashIndex.hashTable.length * 2);
            }

            return getOrAddInteger(intKey);
        }

        lookup                 = hashIndex.linkNode(index, lastLookup);
        testValue              = new Integer(intKey);
        objectKeyTable[lookup] = testValue;

        return testValue;
    }

    protected Long getOrAddLong(long longKey) {

        Long testValue;
        int     index      = hashIndex.getHashIndex((int)longKey);
        int     lookup     = hashIndex.hashTable[index];
        int     lastLookup = -1;

        for (; lookup >= 0;
                lastLookup = lookup,
                lookup = hashIndex.getNextLookup(lookup)) {
            testValue = (Long) objectKeyTable[lookup];

            if (testValue.longValue() == longKey) {
                return testValue;
            }
        }

        if (hashIndex.elementCount >= threshold) {
            if (clearWhenFull) {
                clear();
            } else {
                rehash(hashIndex.hashTable.length * 2);
            }

            return getOrAddLong(longKey);
        }

        lookup                 = hashIndex.linkNode(index, lastLookup);
        testValue              = new Long(longKey);
        objectKeyTable[lookup] = testValue;

        return testValue;
    }

    /**
     * This is dissimilar to normal hash map get() methods. The key Object
     * should hava an equals(String) method which should return true if the
     * key.toString.equals(String) is true. Also the key.hasCode() method
     * must return the same value as key.toString.hashCode()
     */

    protected String getOrAddString(Object key) {

        String testValue;
        int     index      = hashIndex.getHashIndex(key.hashCode());
        int     lookup     = hashIndex.hashTable[index];
        int     lastLookup = -1;

        for (; lookup >= 0;
                lastLookup = lookup,
                lookup = hashIndex.getNextLookup(lookup)) {
            testValue = (String) objectKeyTable[lookup];

            if (key.equals(testValue)) {
                return testValue;
            }
        }

        if (hashIndex.elementCount >= threshold) {
            if (clearWhenFull) {
                clear();
            } else {
                rehash(hashIndex.hashTable.length * 2);
            }

            return getOrAddString(key);
        }

        testValue = key.toString();
        lookup                 = hashIndex.linkNode(index, lastLookup);
        objectKeyTable[lookup] = testValue;

        return testValue;
    }

    protected java.sql.Date getOrAddDate(long longKey) {

        java.sql.Date testValue;
        // the 10 least significant bits are generally similar
        int     hash       = (((int) longKey) >> 10) ^ (int) (longKey >> 32);
        int     index      = hashIndex.getHashIndex((int)longKey);
        int     lookup     = hashIndex.hashTable[index];
        int     lastLookup = -1;

        for (; lookup >= 0;
                lastLookup = lookup,
                lookup = hashIndex.getNextLookup(lookup)) {
            testValue = (java.sql.Date) objectKeyTable[lookup];

            if (testValue.getTime() == longKey) {
                return testValue;
            }
        }

        if (hashIndex.elementCount >= threshold) {
            if (clearWhenFull) {
                clear();
            } else {
                rehash(hashIndex.hashTable.length * 2);
            }

            return getOrAddDate(longKey);
        }

        lookup                 = hashIndex.linkNode(index, lastLookup);
        testValue              = new java.sql.Date(longKey);
        objectKeyTable[lookup] = testValue;

        return testValue;
    }

    protected Double getOrAddDouble(long longKey) {

        Double testValue;
        int     index      = hashIndex.getHashIndex((int)longKey);
        int     lookup     = hashIndex.hashTable[index];
        int     lastLookup = -1;

        for (; lookup >= 0;
                lastLookup = lookup,
                lookup = hashIndex.getNextLookup(lookup)) {
            testValue = (Double) objectKeyTable[lookup];

            if (Double.doubleToLongBits(testValue.doubleValue()) == longKey) {
                return testValue;
            }
        }

        if (hashIndex.elementCount >= threshold) {
            if (clearWhenFull) {
                clear();
            } else {
                rehash(hashIndex.hashTable.length * 2);
            }

            return getOrAddDouble(longKey);
        }

        lookup                 = hashIndex.linkNode(index, lastLookup);
        testValue              = new Double(Double.longBitsToDouble(longKey));
        objectKeyTable[lookup] = testValue;

        return testValue;
    }

    protected java.math.BigDecimal getOrAddBigDecimal(java.math.BigDecimal key) {

        java.math.BigDecimal testValue;
        int     index      = hashIndex.getHashIndex(key.hashCode());
        int     lookup     = hashIndex.hashTable[index];
        int     lastLookup = -1;

        for (; lookup >= 0;
                lastLookup = lookup,
                lookup = hashIndex.getNextLookup(lookup)) {
            testValue = (java.math.BigDecimal) objectKeyTable[lookup];

            if (testValue.equals(key)) {
                return testValue;
            }
        }

        if (hashIndex.elementCount >= threshold) {
            if (clearWhenFull) {
                clear();
            } else {
                rehash(hashIndex.hashTable.length * 2);
            }

            return getOrAddBigDecimal(key);
        }

        lookup                 = hashIndex.linkNode(index, lastLookup);
        objectKeyTable[lookup] = key;

        return key;
    }

    /**
     * rehash uses existing key and element arrays. key / value pairs are
     * put back into the arrays from the top, removing any gaps. any redundant
     * key / value pairs duplicated at the end of the array are then cleared.
     */
    private void rehash(int newCapacity) {

        int     limitLookup     = hashIndex.newNodePointer;
        boolean oldZeroKey      = hasZeroKey;
        int     oldZeroKeyIndex = zeroKeyIndex;

        {
            hasZeroKey   = false;
            zeroKeyIndex = 0;
            threshold    = (int) (newCapacity * loadFactor);

            hashIndex.reset(newCapacity, threshold);
        }

        for (int lookup = -1;
                (lookup = nextLookup(lookup, limitLookup, oldZeroKey, oldZeroKeyIndex))
                < limitLookup; ) {
            long   longKey     = 0;
            int    intValue    = 0;
            Object objectKey   = null;
            Object objectValue = null;

            if (isObjectKey) {
                objectKey = objectKeyTable[lookup];
            } else if (isIntKey) {
                longKey = intKeyTable[lookup];
            } else {
                longKey = longKeyTable[lookup];
            }

            if (isObjectValue) {
                objectValue = objectValueTable[lookup];
            } else if (isIntValue) {
                intValue = intValueTable[lookup];
            }

            addOrRemove(longKey, intValue, objectKey, objectValue, false);
        }

        resizeElementArrays(limitLookup, threshold);
    }

    /**
     * resize the arrays contianing the key / value data
     */
    private void resizeElementArrays(int oldLength, int newLength) {

        Object temp;

        if (isIntKey) {
            temp        = intKeyTable;
            intKeyTable = new int[newLength];

            System.arraycopy(temp, 0, intKeyTable, 0, oldLength);
        }

        if (isIntValue) {
            temp          = intValueTable;
            intValueTable = new int[newLength];

            System.arraycopy(temp, 0, intValueTable, 0, oldLength);
        }

        if (isObjectKey) {
            temp           = objectKeyTable;
            objectKeyTable = new Object[newLength];

            System.arraycopy(temp, 0, objectKeyTable, 0, oldLength);
        }

        if (isObjectValue) {
            temp             = objectValueTable;
            objectValueTable = new Object[newLength];

            System.arraycopy(temp, 0, objectValueTable, 0, oldLength);
        }
    }

    /**
     * clear all the key / value data
     */
    private void clearElementArrays(int from, int to) {

        if (isIntKey) {
            while (--to >= from) {
                intKeyTable[to] = 0;
            }
        }

        if (isLongKey) {
            while (--to >= from) {
                longKeyTable[to] = 0;
            }
        }

        if (isObjectKey) {
            while (--to >= from) {
                objectKeyTable[to] = null;
            }
        }

        if (isIntValue) {
            while (--to >= from) {
                intValueTable[to] = 0;
            }
        }

        if (isObjectValue) {
            while (--to >= from) {
                objectValueTable[to] = null;
            }
        }
    }

    /**
     * move the elements after a removed key / value pair to fill the gap
     */
    void removeFromElementArrays(int lookup) {

        int arrayLength = hashIndex.linkTable.length;

        if (isIntKey) {
            Object array = intKeyTable;

            System.arraycopy(array, lookup + 1, array, lookup,
                             arrayLength - lookup - 1);

            intKeyTable[arrayLength - 1] = 0;
        }

        if (isLongKey) {
            Object array = longKeyTable;

            System.arraycopy(array, lookup + 1, array, lookup,
                             arrayLength - lookup - 1);

            longKeyTable[arrayLength - 1] = 0;
        }

        if (isObjectKey) {
            Object array = objectKeyTable;

            System.arraycopy(array, lookup + 1, array, lookup,
                             arrayLength - lookup - 1);

            objectKeyTable[arrayLength - 1] = null;
        }

        if (isIntValue) {
            Object array = intValueTable;

            System.arraycopy(array, lookup + 1, array, lookup,
                             arrayLength - lookup - 1);

            intValueTable[arrayLength - 1] = 0;
        }

        if (isObjectValue) {
            Object array = objectValueTable;

            System.arraycopy(array, lookup + 1, array, lookup,
                             arrayLength - lookup - 1);

            objectValueTable[arrayLength - 1] = null;
        }
    }

    /**
     * find the next lookup in the key/value tables with an entry
     * allows the use of old limit and zero int key attributes
     */
    int nextLookup(int lookup, int limitLookup, boolean hasZeroKey,
                   int zeroKeyIndex) {

        for (++lookup; lookup < limitLookup; lookup++) {
            if (isObjectKey) {
                if (objectKeyTable[lookup] != null) {
                    return lookup;
                }
            } else if (isIntKey) {
                if (intKeyTable[lookup] != 0) {
                    return lookup;
                } else if (hasZeroKey && lookup == zeroKeyIndex) {
                    return lookup;
                }
            } else {
                if (longKeyTable[lookup] != 0) {
                    return lookup;
                } else if (hasZeroKey && lookup == zeroKeyIndex) {
                    return lookup;
                }
            }
        }

        return lookup;
    }

    /**
     * find the next lookup in the key/value tables with an entry
     * uses current limits and zero integer key state
     */
    int nextLookup(int lookup) {

        for (++lookup; lookup < hashIndex.newNodePointer; lookup++) {
            if (isObjectKey) {
                if (objectKeyTable[lookup] != null) {
                    return lookup;
                }
            } else if (isIntKey) {
                if (intKeyTable[lookup] != 0) {
                    return lookup;
                } else if (hasZeroKey && lookup == zeroKeyIndex) {
                    return lookup;
                }
            } else {
                if (longKeyTable[lookup] != 0) {
                    return lookup;
                } else if (hasZeroKey && lookup == zeroKeyIndex) {
                    return lookup;
                }
            }
        }

        return lookup;
    }

    /**
     * row must already been freed of key / element
     */
    protected void removeRow(int lookup) {
        hashIndex.removeEmptyNode(lookup);
        removeFromElementArrays(lookup);
    }

    void remove(int lookup) {

        if (isObjectKey) {
            this.addOrRemove(0, 0, objectKeyTable[lookup], null, true);
        } else {
            this.addOrRemove(intKeyTable[lookup], 0, null, null, true);
        }
    }

    public void clear() {
        clearElementArrays(0, hashIndex.linkTable.length);
        hashIndex.clear();
    }

    public int size() {
        return hashIndex.elementCount;
    }

    public boolean isEmpty() {
        return hashIndex.elementCount == 0;
    }

    protected boolean containsKey(Object key) {

        int lookup = getLookup(key, key.hashCode());

        return lookup == -1 ? false
                            : true;
    }

    protected boolean containsKey(int key) {

        int lookup = getLookup(key);

        return lookup == -1 ? false
                            : true;
    }

    protected boolean containsKey(long key) {

        int lookup = getLookup(key);

        return lookup == -1 ? false
                            : true;
    }

    protected boolean containsValue(Object value) {

        int lookup = 0;

        if (value == null) {
            for (; lookup < hashIndex.newNodePointer; lookup++) {
                if (objectValueTable[lookup] == null) {
                    if (isObjectKey) {
                        if (objectKeyTable[lookup] != null) {
                            return true;
                        }
                    } else if (isIntKey) {
                        if (intKeyTable[lookup] != 0) {
                            return true;
                        } else if (hasZeroKey && lookup == zeroKeyIndex) {
                            return true;
                        }
                    } else {
                        if (longKeyTable[lookup] != 0) {
                            return true;
                        } else if (hasZeroKey && lookup == zeroKeyIndex) {
                            return true;
                        }
                    }
                }
            }
        } else {
            for (; lookup < hashIndex.newNodePointer; lookup++) {
                if (value.equals(objectValueTable[lookup])) {
                    return true;
                }
            }
        }

        return false;
    }

    public class BaseHashIterator implements org.hsqldb.lib.Iterator {

        boolean keys;
        int     lookup = -1;
        int     counter;

        public BaseHashIterator(boolean keys) {
            this.keys = keys;
        }

        public boolean hasNext() {
            return counter < hashIndex.elementCount;
        }

        public Object next() throws NoSuchElementException {

            if ((keys &&!isObjectKey) || (!keys &&!isObjectValue)) {
                throw new NoSuchElementException("Hash Iterator");
            }

            if (hasNext()) {
                counter++;

                lookup = nextLookup(lookup);

                if (keys) {
                    return objectKeyTable[lookup];
                } else {
                    return objectValueTable[lookup];
                }
            }

            throw new NoSuchElementException("Hash Iterator");
        }

        public int nextInt() throws NoSuchElementException {

            if ((keys &&!isIntKey) || (!keys &&!isIntValue)) {
                throw new NoSuchElementException("Hash Iterator");
            }

            if (hasNext()) {
                lookup = nextLookup(lookup);

                if (keys) {
                    return intKeyTable[lookup];
                } else {
                    return intValueTable[lookup];
                }
            }

            throw new NoSuchElementException("Hash Iterator");
        }

        public long nextLong() throws NoSuchElementException {

            if ((!isLongKey ||!keys)) {
                throw new NoSuchElementException("Hash Iterator");
            }

            if (hasNext()) {
                lookup = nextLookup(lookup);

                if (keys) {
                    return longKeyTable[lookup];
                }
            }

            throw new NoSuchElementException("Hash Iterator");
        }

        public void remove() throws NoSuchElementException {
            BaseHashMap.this.remove(lookup);
        }
    }
}
