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
 * A map ADT implementation that uses primitive <code>int</code> values for
 * both the keys and the values. <p>
 *
 * This implementation uses significantly less memory than a generalized
 * map implementation and benchmarks indicate it is approximately 3x faster
 * than a generalized map implementation regarding its basic operations:
 * <code>containsKey/CODE>, <code>get</code>,
 * <code>put</code> and <code>remove</code>. <p>
 *
 * This implementation does not include the ability to retrieve Enumations
 * over its keys and values, but may include the ability to retrieve
 * its keys and values as primitive int arrays in a future release, if
 * there is a demand for such functionality. <p>
 *
 * @author boucherb@users.sourceforge.net
 * @version 1.7.2
 * @since 1.7.2
 */
public final class HsqlIntKeyIntValueHashMap {

    /** The type of Map entry used by <code>HsqlIntKeyIntValueHashMap</code>. */
    private final class Entry {

        /** key with which the specified value is associated */
        int key;

        /** value associated with the key. */
        int value;

        /** hash bucket entry link. */
        Entry next;
    }

    /** The map data. */
    private Entry table[];

    /** The total number of entries in the map. */
    private int count;

    /** Rehashes when count exceeds this threshold. */
    private int threshold;

    /** The map load factor. */
    private float loadFactor;

    /**
     * A single-element <code>int</code> array used to return values from
     * {@link #get get}, {@link #put put} and {@link #remove remove}.  <P>
     *
     * This allows the concerned methods to return <code>null</code> in
     * accordance with the genral contract of a map ADT, which otherwise
     * would be impossible if returning primitive values directly, unless
     * a sepcial primitive value were used to indicate that no mapping
     * exits. <p>
     *
     * This attribute exists to avoid the allocation overhead of creating a
     * new wrapper array each time <code>get</code>, <code>put</code> or
     * <code>remove</code> is called.  As such, special care must be taken
     * by clients of this to immediately make a copy of the value returned
     * in this wrapper for later use, if requried, rather than referencing
     * the wrapped value, since subsequent calls to <code>get</code>,
     * <code>put</code> or <code>remove</code> may change the wrapped value.
     */
    private int[] returnValueHolder = new int[1];

    /**
     * Constructs an empty map with the specified initial
     * capacity and load factor.
     * @param initialCapacity the initial capacity
     * @param loadFactor the load factor.
     * @throws IllegalArgumentException if either the initial capacity
     *      or the load factor is non-positive
     */
    public HsqlIntKeyIntValueHashMap(int initialCapacity,
                                     float loadFactor)
                                     throws IllegalArgumentException {

        if (initialCapacity <= 0 || loadFactor <= 0.0) {
            throw new IllegalArgumentException();
        }

        this.loadFactor = loadFactor;
        table           = new Entry[initialCapacity];
        threshold       = (int) (initialCapacity * loadFactor);
    }

    /**
     * Constructs an empty map with the specified initial
     * capacity and the default load factor (0.75).
     * @param initialCapacity the initial capacity
     * @throws IllegalArgumentException if the initial capacity is non-positive.
     */
    public HsqlIntKeyIntValueHashMap(int initialCapacity)
    throws IllegalArgumentException {
        this(initialCapacity, 0.75f);
    }

    /**
     * Constructs an empty map with the default initial
     *  capacity (16) and the default load factor (0.75).
     */
    public HsqlIntKeyIntValueHashMap() {
        this(16, 0.75f);
    }

    /**
     * Returns the number of key-value mappings in this map. If the map
     * contains more than <code>Integer.MAX_VALUE</code> elements, then
     * the returned value is <code>Integer.MAX_VALUE</code>.
     * @return the number of key-value mappings in this map.
     */
    public int size() {
        return count;
    }

    /**
     * Returns <code>true</code> if this map contains no key-value mappings.
     *
     * @return <code>true</code> if this map contains no key-value mappings.
     */
    public boolean isEmpty() {
        return count == 0;
    }

    /**
     * Returns <code>true</code> if this map maps one or more keys to this
     * value.
     * @param value - value whose presence in this map is to be tested.
     * @return <code>true</code> if this map maps one or more keys to the
     *      specified value.
     */
    public boolean containsValue(int value) {

        Entry tab[] = table;

        for (int i = tab.length; i-- > 0; ) {
            for (Entry e = tab[i]; e != null; e = e.next) {
                if (e.value == value) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns <code>true</code> if this map contains a mapping for the
     * specified key. <p>
     *
     * More formally, this method returns <code>true</code> if and only if
     * this map contains a mapping for a key <code>k</code> such that
     * <code>(key == k)</code> (there can be at most one such mapping).
     * @param key candidate key.
     * @return <code>true</code> if and only if this map contains a
     *      mapping for the given key.
     */
    public boolean containsKey(int key) {

        Entry tab[] = table;
        int   index = (key & 0x7fffffff) % tab.length;

        for (Entry e = tab[index]; e != null; e = e.next) {
            if (e.key == key) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the value to which this map maps the specified key, wrapped in a
     * single element int array. Returns <code>null</code> if the map contains
     * no mapping for this key. <p>
     *
     * More formally, if this map contains a mapping from a key <code>k</code> to
     * a value v such that <code>(key == k)</code>, then this method returns
     * <code>v</code> wrapped in a single element int array; otherwise it
     * returns <code>null</code> (there can be at most one such mapping). <p>
     *
     * <B>Note:</B> If required for later use, special care should be taken to
     * immediately make a copy of the value returned in the wrapper array rather
     * than passing about references to the wrapped value. This is because
     * the returned array is actually a single instance member of this class
     * (in order to lower allocation and garbage collection overhead)
     * and hence subsequent calls to <code>get</code>, <code>put</code> or
     * <code>remove</code> may change the wrapped value.
     * @param key <code>int</code> key whose associated value is to be returned.
     * @return the value to which this map maps the specified key,
     *        wrapped in a single element int array, or
     *        <code>null</code> if this map contains
     *        no mapping for this key.
     * @see #returnValueHolder
     */
    public int[] get(int key) {

        Entry tab[] = table;
        int   index = (key & 0x7fffffff) % tab.length;

        for (Entry e = tab[index]; e != null; e = e.next) {
            if (e.key == key) {
                returnValueHolder[0] = e.value;

                return returnValueHolder;
            }
        }

        return null;
    }

    /**
     * Increases the capacity of and internally reorganizes this map, in order
     * to accommodate and access its entries more efficiently. This method is
     * called automatically when the number of keys in this map exceeds
     * the threshold determined by its capacity and load factor.
     */
    protected void rehash() {

        // put these first to take advantage of implicit local variable slots
        int   i;
        Entry old;
        Entry oldTable[] = table;
        Entry newTable[] = new Entry[table.length * 2 + 1];

        // then these
        int   oldCapacity = table.length;
        int   newCapacity = newTable.length;
        Entry e;
        int   index;

        threshold = (int) (newCapacity * loadFactor);
        table     = newTable;

        for (i = oldCapacity; i-- > 0; ) {
            for (old = oldTable[i]; old != null; ) {
                e               = old;
                index           = (e.key & 0x7fffffff) % newCapacity;
                old             = old.next;
                e.next          = newTable[index];
                newTable[index] = e;
            }
        }
    }

    /**
     * Associates the specified value with the specified key in this map. If
     * this map previously contained a mapping for this key, the old value is
     * replaced by the specified value. A map m is said to contain a mapping
     * for a key k if and only if <code>m.containsKey(k)</code> would return
     * <code>true</code>. <p>
     *
     * <B>Note:</B> If required for later use, special care should be taken to
     * immediately make a copy of the value returned in the wrapper array rather
     * than passing about references to the wrapped value. This is because
     * the returned array is actually a single instance member of this class
     * (in order to lower allocation and garbage collection overhead)
     * and hence subsequent calls to <code>get</code>, <code>put</code> or
     * <code>remove</code> may change the wrapped value.
     * @param key <code>int</code> key with which the specified value is to be associated.
     * @param value <code>int</code> value to be associated with the specified key.
     * @return previous value associated with specified key wrapped in a
     *        single element <code>int</code> array, or
     *        <code>null</code> if there was no mapping for
     *        it in this map.
     * @see #returnValueHolder
     */
    public int[] put(int key, int value) {

        Entry tab[] = table;
        int   index = (key & 0x07fffffff) % tab.length;

        // handle case where key already maps to a value
        for (Entry e = tab[index]; e != null; e = e.next) {
            if (e.key == key) {
                returnValueHolder[0] = e.value;
                e.value              = value;

                return returnValueHolder;
            }
        }

        // need to add a new entry
        if (count >= threshold) {

            // Rehash the table if the threshold is exceeded.
            rehash();

            // now we must recalulate the hash bucket index
            tab   = table;
            index = (key & 0x7fffffff) % tab.length;
        }

        // Create a new entry.
        Entry e = new Entry();

        e.key   = key;
        e.value = value;

        // link it
        e.next     = tab[index];
        tab[index] = e;

        // do the size book keeping
        count++;

        return null;
    }

    /**
     * Removes the mapping for this key from this map if it is present. More
     * formally, if this map contains a mapping from key <code>k</code> to value
     * <code>v</code> such that <code>key == k</code>, that mapping is removed
     * (the map can contain at most one such mapping). <p>
     *
     * Returns the value to which this map previously associated the key,
     * wrapped in a single element <code>int</code> array, or <code>null</code>
     * if this map contained no mapping for the key. <p>
     *
     * It is guaranteed that under single-threaded access, this map will not
     * contain a mapping for the specified key at the point this method
     * returns. <p>
     *
     * <B>Note:</B> If required for later use, special care should be taken to
     * immediately make a copy of the value returned in the wrapper array rather
     * than passing about references to the wrapped value. This is because
     * the returned array is actually a single instance member of this class
     * (in order to lower allocation and garbage collection overhead)
     * and hence subsequent calls to <code>get</code>, <code>put</code> or
     * <code>remove</code> may change the wrapped value.
     * @param key key whose mapping is to be removed from this map.
     * @return the value to which this map previously associated the key,
     *        wrapped in a single element <code>int</code> array,
     *        or <code>null</code> if this map contained no mapping
     *        for the key.
     * @see #returnValueHolder
     */
    public int[] remove(int key) {

        Entry tab[] = table;
        int   index = (key & 0x07fffffff) % tab.length;
        Entry e     = tab[index];
        Entry prev  = null;

        while (e != null) {
            if (e.key == key) {
                if (prev != null) {
                    prev.next = e.next;
                } else {
                    tab[index] = e.next;
                }

                count--;

                returnValueHolder[0] = e.value;

                return returnValueHolder;
            }

            prev = e;
            e    = e.next;
        }

        return null;
    }

    /** Removes all mappings from this map. */
    public void clear() {

        Entry tab[] = table;

        for (int index = tab.length - 1; index >= 0; index--) {
            tab[index] = null;
        }

        count = 0;
    }

    /**
     * Returns a string representation of this map in the form of a set of
     * entries, enclosed in braces and separated by the ASCII characters ", "
     * (comma and space). Each entry is rendered as the key, an equals sign =,
     * and the associated value, where the <code>toString</code> method is
     * used to convert the key and value to strings.
     * @return a string representation of this map
     */
    public String toString() {

        // "=, ".length() + ~2*(int avg str len)
        HsqlStringBuffer sb    = new HsqlStringBuffer(12 * size());
        Entry            entry = null;
        Entry[]          tab   = this.table;

        sb.append('{');

        int i = tab.length - 1;

        while (true) {
            if (i < 0) {
                break;
            }

            entry = tab[i--];

            while (entry != null) {
                sb.append(entry.key).append('=').append(entry.value).append(
                    ", ");

                entry = entry.next;
            }
        }

        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2);
        }

        sb.append('}');

        return sb.toString();
    }
}
