/* Copyright (c) 2001-2004, The HSQL Development Group
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
import org.hsqldb.lib.ArrayCounter;

public class BaseHashMap {

/**
 * Base class for hash tables or sets. The exact type of the structure is
 * defined by the constructor. Each instance has at least a keyTable array
 * and a HashIndex instance for looking up the keys into this table. Instances
 * that are maps also have a valueTable the same size as the keyTable.
 *
 * Special getOrAddXXX() methods are used for object maps.
 *
 * @author fredt@users
 * @version 1.7.2
 * @since 1.7.2
 */
/*

    data store:
    keys: {array of primitive | array of object}
    values: {none | array of primitive | array of object} same size as keys
    objects support : hashCode(), equals()

    implemented types of keyTable:
    {objectKeyTable: variable size Object[] array for keys |
    intKeyTable: variable size int[] for keys |
    longKeyTable: variable size long[] for keys }

    implemented types of valueTable:
    {objectValueTable: variable size Object[] array for values |
    intValueTable: variable size int[] for values }

    valueTable does not exist for sets or for object pools

    hash index:
    hashTable: fixed size int[] array for hash lookup into keyTable
    linkTable: pointer to the next key ; size equal or larger than hashTable
    but equal to the valueTable

    access count table:
    {none |
    variable size int[] array for access count} same size as keyTable
*/

    //
    boolean isIntKey;
    boolean isLongKey;
    boolean isObjectKey;
    boolean isNoValue;
    boolean isIntValue;
    boolean isObjectValue;

    //
    protected HashIndex hashIndex;

    //
    protected int[]    intKeyTable;
    protected Object[] objectKeyTable;
    protected long[]   longKeyTable;

    //
    protected int[]    intValueTable;
    protected Object[] objectValueTable;

    //
    int   accessMin;
    int   accessCount;
    int[] accessTable;

    //
    float         loadFactor;
    int           threshold;
    int           maxCapacity;
    protected int purgePolicy = NO_PURGE;

    //
    boolean hasZeroKey;
    int     zeroKeyIndex;

    // keyOrValueTypes
    protected static final int noKeyOrValue     = 0;
    protected static final int intKeyOrValue    = 1;
    protected static final int longKeyOrValue   = 2;
    protected static final int objectKeyOrValue = 3;

    // purgePolicy
    protected static final int NO_PURGE      = 0;
    protected static final int PURGE_ALL     = 1;
    protected static final int PURGE_HALF    = 2;
    protected static final int PURGE_QUARTER = 3;

    protected BaseHashMap(int initialCapacity, float loadFactor, int keyType,
                          int valueType) throws IllegalArgumentException {

        if (initialCapacity <= 0 || loadFactor <= 0.0) {
            throw new IllegalArgumentException();
        }

        this.loadFactor = loadFactor;
        threshold       = initialCapacity;

        if (threshold < 3) {
            threshold = 3;
        }

        int hashtablesize = (int) (initialCapacity * loadFactor);

        if (hashtablesize < 3) {
            hashtablesize = 3;
        }

        hashIndex = new HashIndex(hashtablesize, initialCapacity, true);

        int arraySize = threshold;

        if (keyType == BaseHashMap.intKeyOrValue) {
            isIntKey    = true;
            intKeyTable = new int[arraySize];
        } else if (keyType == BaseHashMap.objectKeyOrValue) {
            isObjectKey    = true;
            objectKeyTable = new Object[arraySize];
        } else {
            isLongKey    = true;
            longKeyTable = new long[arraySize];
        }

        if (valueType == BaseHashMap.intKeyOrValue) {
            isIntValue    = true;
            intValueTable = new int[arraySize];
        } else if (valueType == BaseHashMap.objectKeyOrValue) {
            isObjectValue    = true;
            objectValueTable = new Object[arraySize];
        } else {
            isNoValue = true;
        }
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
            reset();

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

    protected Object removeObject(Object objectKey) {

        int    hash        = objectKey.hashCode();
        int    index       = hashIndex.getHashIndex(hash);
        int    lookup      = hashIndex.hashTable[index];
        int    lastLookup  = -1;
        Object returnValue = null;

        for (; lookup >= 0;
                lastLookup = lookup,
                lookup = hashIndex.getNextLookup(lookup)) {
            if (objectKeyTable[lookup].equals(objectKey)) {
                objectKeyTable[lookup] = null;

                hashIndex.unlinkNode(index, lastLookup, lookup);

                if (isObjectValue) {
                    returnValue              = objectValueTable[lookup];
                    objectValueTable[lookup] = null;
                }

                return returnValue;
            }
        }

        // not found
        return returnValue;
    }

    protected boolean reset() {

        if (maxCapacity == 0 || maxCapacity > threshold) {
            rehash(hashIndex.hashTable.length * 2);

            return true;
        } else if (purgePolicy == PURGE_ALL) {
            clear();

            return true;
        } else if (purgePolicy == PURGE_QUARTER) {
            clear(threshold / 4);

            return true;
        } else if (purgePolicy == PURGE_HALF) {
            clear(threshold / 2);

            return true;
        } else if (purgePolicy == NO_PURGE) {
            return false;
        }

        return false;
    }

    /**
     * rehash uses existing key and element arrays. key / value pairs are
     * put back into the arrays from the top, removing any gaps. any redundant
     * key / value pairs duplicated at the end of the array are then cleared.
     *
     * newCapacity must be larger or equal to existing number of elements.
     */
    protected void rehash(int newCapacity) {

        int     limitLookup     = hashIndex.newNodePointer;
        boolean oldZeroKey      = hasZeroKey;
        int     oldZeroKeyIndex = zeroKeyIndex;

        hashIndex.reset((int) (newCapacity * loadFactor), newCapacity);

        hasZeroKey   = false;
        zeroKeyIndex = 0;
        threshold    = newCapacity;

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

            if (accessTable != null) {
                accessTable[hashIndex.elementCount - 1] = accessTable[lookup];
            }
        }

        resizeElementArrays(hashIndex.newNodePointer, newCapacity);
    }

    /**
     * resize the arrays contianing the key / value data
     */
    private void resizeElementArrays(int dataLength, int newLength) {

        Object temp;
        int    usedLength = newLength > dataLength ? dataLength
                                                   : newLength;

        if (isIntKey) {
            temp        = intKeyTable;
            intKeyTable = new int[newLength];

            System.arraycopy(temp, 0, intKeyTable, 0, usedLength);
        }

        if (isIntValue) {
            temp          = intValueTable;
            intValueTable = new int[newLength];

            System.arraycopy(temp, 0, intValueTable, 0, usedLength);
        }

        if (isObjectKey) {
            temp           = objectKeyTable;
            objectKeyTable = new Object[newLength];

            System.arraycopy(temp, 0, objectKeyTable, 0, usedLength);
        }

        if (isObjectValue) {
            temp             = objectValueTable;
            objectValueTable = new Object[newLength];

            System.arraycopy(temp, 0, objectValueTable, 0, usedLength);
        }

        if (accessTable != null) {
            temp        = accessTable;
            accessTable = new int[newLength];

            System.arraycopy(temp, 0, accessTable, 0, usedLength);
        }
    }

    /**
     * clear all the key / value data in a range.
     */
    private void clearElementArrays(final int from, final int to) {

        if (isIntKey) {
            int counter = to;

            while (--counter >= from) {
                intKeyTable[counter] = 0;
            }
        }

        if (isLongKey) {
            int counter = to;

            while (--counter >= from) {
                longKeyTable[counter] = 0;
            }
        }

        if (isObjectKey) {
            int counter = to;

            while (--counter >= from) {
                objectKeyTable[counter] = null;
            }
        }

        if (isIntValue) {
            int counter = to;

            while (--counter >= from) {
                intValueTable[counter] = 0;
            }
        }

        if (isObjectValue) {
            int counter = to;

            while (--counter >= from) {
                objectValueTable[counter] = null;
            }
        }

        if (accessTable != null) {
            int counter = to;

            while (--counter >= from) {
                accessTable[counter] = 0;
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

    protected Object removeLookup(int lookup) {

        if (isObjectKey) {
            return addOrRemove(0, 0, objectKeyTable[lookup], null, true);
        } else {
            return addOrRemove(intKeyTable[lookup], 0, null, null, true);
        }
    }

    /**
     * Clear the map completely.
     */
    public void clear() {

        accessCount  = 0;
        accessMin    = accessCount;
        hasZeroKey   = false;
        zeroKeyIndex = 0;

        clearElementArrays(0, hashIndex.linkTable.length);
        hashIndex.clear();
    }

    /**
     * Clear approximately count elements from the map, starting with
     * those with low accessTable ranking.
     */
    protected void clear(int count) {

        int margin = threshold >> 8;

        if (margin < 64) {
            margin = 64;
        }

        int accessBase = ArrayCounter.rank(accessTable, count, accessMin,
                                           accessCount, margin);
        int maxlookup = hashIndex.newNodePointer;

        for (int lookup = 0; lookup < maxlookup; lookup++) {
            Object o = objectKeyTable[lookup];

            if (o != null && accessTable[lookup] < accessBase) {
                removeObject(o);
            }
        }

        accessMin = accessBase;
    }

    void resetAccessCount() {

        if (accessCount < Integer.MAX_VALUE) {
            return;
        }

        accessMin   >>= 2;
        accessCount >>= 2;

        int i = accessTable.length;

        while (--i >= 0) {
            accessTable[i] >>= 2;
        }
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

    protected class BaseHashIterator implements org.hsqldb.lib.Iterator {

        boolean keys;
        int     lookup = -1;
        int     counter;
        boolean removed;

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

            removed = false;

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

            removed = false;

            if (hasNext()) {
                counter++;

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

            removed = false;

            if (hasNext()) {
                counter++;

                lookup = nextLookup(lookup);

                if (keys) {
                    return longKeyTable[lookup];
                }
            }

            throw new NoSuchElementException("Hash Iterator");
        }

        public void remove() throws NoSuchElementException {

            if (removed) {
                throw new NoSuchElementException("Hash Iterator");
            }

            counter--;

            removed = true;

            BaseHashMap.this.removeLookup(lookup);
        }
    }
}
