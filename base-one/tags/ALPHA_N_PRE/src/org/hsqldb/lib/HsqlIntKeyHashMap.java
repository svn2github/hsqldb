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

/** An <code>HsqlMap</code> implementation that uses primitive
 * <code>int</code> values as the keys.
 *
 * @author boucherb@users.sourceforge.net
 * @version 1.7.2
 * @since 1.7.2
 */
public final class HsqlIntKeyHashMap implements HsqlMap, Cloneable {

    /** The type of Map entry used by <code>HsqlIntKeyHashMap</code>. */
    private final class Entry {

        /** key with which the specified value is associated */
        int key;

        /** value associated with the key. */
        Object value;

        /** hash bucket entry link */
        Entry next;

        /**
         * Retrieves a link-aware clone of this map entry.
         * @return a link-aware clone of this map entry
         */
        protected Object clone() {

            Entry entry = new Entry();

            entry.key   = key;
            entry.value = value;
            entry.next  = (next != null) ? (Entry) next.clone()
                                         : null;

            return entry;
        }
    }

    /** The Map entry enumerator. */
    static final class Enumerator implements Enumeration {

        /**
         * If <code>true</code>, then return an enumeration of keys, else
         * the values.
         */
        boolean keys;

        /** The current offset into the entry array */
        int index;

        /** Hash bucket storage */
        Entry[] table;

        /** The current map entry. */
        Entry entry = null;

        /**
         * Constructs a new <code>Enumeration</code> over either the keys or
         * the values of the enclosing map.
         *
         * @param table Hash bucket storage array over which to enumerate
         * @param keys If <code>true</code>, then return an enumeration of
         *      keys, else the values.
         */
        Enumerator(Entry[] table, boolean keys) {

            this.table = table;
            this.keys  = keys;
            this.index = table.length;
        }

        /**
         * Tests if this enumeration contains more elements.
         *
         * @return true if and only if this enumeration object contains at
         *      least one more element to provide; false otherwise
         */
        public boolean hasMoreElements() {

            if (table == null) {
                return false;
            }

            while (entry == null && index-- > 0) {
                entry = table[index];
            }

            if (entry == null) {

                // prevent memory leaks by releasing this
                // immediately for garbage collection
                table = null;

                return false;
            }

            return true;
        }

        /**
         * Returns the next element of this enumeration if this enumeration
         * object has at least one more element to provide.
         *
         * @return the next element of this enumeration
         * @throws NoSuchElementException if no more elements exist.
         */
        public Object nextElement() throws NoSuchElementException {

            if (hasMoreElements()) {
                Object element = keys ? ValuePool.getInt(entry.key)
                                      : entry.value;

                entry = entry.next;

                return element;
            }

            throw new NoSuchElementException(getClass().getName());
        }
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
     * Constructs a an empty map with the specified initial
     * capacity and load factor.
     *
     * @param initialCapacity the initial capacity
     * @param loadFactor the load factor.
     * @throws IllegalArgumentException if either the initial capacity
     *      or the load factor is non-positive
     */
    public HsqlIntKeyHashMap(int initialCapacity,
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
     *
     * @param initialCapacity the initial capacity
     * @throws IllegalArgumentException if the initial capacity is non-positive.
     */
    public HsqlIntKeyHashMap(int initialCapacity)
    throws IllegalArgumentException {
        this(initialCapacity, 0.75f);
    }

    /**
     * Constructs an empty map with the default initial
     *  capacity (16) and the default load factor (0.75).
     */
    public HsqlIntKeyHashMap() {
        this(16, 0.75f);
    }

    /**
     * Returns the number of key-value mappings in this map. If the map
     * contains more than <code>Integer.MAX_VALUE</code> elements, returns
     * <code>Integer.MAX_VALUE</code>.
     *
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
     * Returns an enumeration of the keys in the key-value mappings
     * of this map.
     *
     * @return an enumeration of the keys in the key-value mappings
     *      of this map.
     */
    public Enumeration keys() {
        return new Enumerator(table, true);
    }

    /**
     * Returns an enumeration of the values in the key-value mappings
     * of this map.
     *
     * @return an enumeration of the values in the key-value mappings
     *      of this map.
     */
    public Enumeration elements() {
        return new Enumerator(table, false);
    }

    /**
     * Returns <code>true</code> if this map maps one or more keys to this
     * value.
     *
     * @param value - value whose presence in this map is to be tested.
     * @return <code>true</code> if this map maps one or more keys to the
     *      specified value.
     */
    public boolean containsValue(Object value) {

        Entry tab[] = table;

        if (value == null) {
            for (int i = tab.length; i-- > 0; ) {
                for (Entry e = tab[i]; e != null; e = e.next) {
                    if (e.value == null) {
                        return true;
                    }
                }
            }
        } else {
            for (int i = tab.length; i-- > 0; ) {
                for (Entry e = tab[i]; e != null; e = e.next) {
                    if (e.value.equals(value)) {
                        return true;
                    }
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
     * this map contains a mapping for a key k such that
     * <code>(key == k)</code> (there can be at most one such mapping).
     *
     * @param key - possible key.
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
     * Returns <code>true</code> if this map contains a mapping for the
     * specified key. <p>
     *
     * More formally, this method returns <code>true</code> if and only if
     * this map contains a mapping for a key k such that
     * <code>(((Number)key).intValue() == k)</code> (there can be at most
     * one such mapping).
     *
     * @return <code>true</code> if this map contains a mapping for the
     *      specified key.
     * @param key - key whose presence in this map is to be tested.
     * @throws ClassCastException - if the supplied key is not an instance
     *      of <code>java.lang.Number</code>.
     * @throws NullPointerException - if the supplied key is <code>null</code>.
     */
    public boolean containsKey(Object key)
    throws ClassCastException, NullPointerException {
        return containsKey(((Number) key).intValue());
    }

    /**
     * Returns the value to which this map maps the specified key. Returns
     * <code>null</code> if either the map contains no mapping for this key
     * or the map explicitly maps the key to <code>null</code>. The
     * <code>containsKey</code> operation may be used to
     * distinguish these two cases. <p>
     *
     * More formally, if this map contains a mapping from a key k to a value
     * v such that <code>(key == k)</code>, then this method returns
     * <code>v</code>; otherwise it returns <code>null</code> (there can be at
     * most one such mapping).
     *
     * @return the value to which this map maps the specified key, or null if
     *        either this map either contains no mapping for this key or
     *        explicitly maps it to <code>null</code>.
     * @param key key whose associated value is to be returned.
     */
    public Object get(int key) {

        Entry tab[] = table;
        int   index = (key & 0x7fffffff) % tab.length;

        for (Entry e = tab[index]; e != null; e = e.next) {
            if (e.key == key) {
                return e.value;
            }
        }

        return null;
    }

    /**
     * Returns the value to which this map maps the specified key. Returns
     * <code>null</code> if the map either contains no mapping for this key or
     * explicitly maps it to null.  The <code>containsKey</code> operation may
     * be used to distinguish these two cases. <p>
     *
     * More formally, if this map contains a mapping from a key k to a value
     * v such that <code>(((Number)key).intValue() == k)</code>, then this
     * method returns <code>v</code>; otherwise it returns <code>null</code>
     * (there can be at most one such mapping).
     *
     * @return the value to which this map maps the specified key, or null if
     *        this map either contains no mapping for this key or explicitly
     *        maps it to null.
     * @param okey key whose associated value is to be returned.
     * @throws ClassCastException if the specified key is not an instance of
     *      <code>java.lang.Number</code>
     * @throws NullPointerException if the specified key is <code>null</code>.
     */
    public Object get(Object okey)
    throws ClassCastException, NullPointerException {
        return get(((Number) okey).intValue());
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

        // then these
        Entry newTable[]  = new Entry[table.length * 2 + 1];
        int   oldCapacity = table.length;
        int   newCapacity = oldCapacity * 2 + 1;
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
     * <code>true</code>.
     *
     * @return previous value associated with specified key, or
     *        <code>null</code> if either there was no mapping for it in
     *        this map or is was explicitly mapped to <code>null</code>.
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     */
    public Object put(int key, Object value) {

        Entry tab[] = table;
        int   index = (key & 0x07fffffff) % tab.length;

        // handle case where key already maps to a value
        for (Entry e = tab[index]; e != null; e = e.next) {
            if (e.key == key) {
                Object old = e.value;

                e.value = value;

                return old;
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

        // indicate that (maybe) no mapping existed
        return null;
    }

    /**
     * Associates the specified value with the specified key in this map. If
     * this map previously contained a mapping for this key, the old value is
     * replaced by the specified value. A map m is said to contain a mapping
     * for a key k if and only if <code>m.containsKey(k)</code> would
     * return <code>true</code>.
     * @return previous value associated with specified key, or
     *        <code>null</code> if either there was no mapping for it in
     *        this map or is was explicitly mapped to <code>null</code>.
     * @param okey key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @throws ClassCastException if the specified key is not an instance of
     *      <code>java.lang.Number</code>.
     * @throws NullPointerException if the specified key is <code>null</code>.
     */
    public Object put(Object okey,
                      Object value)
                      throws ClassCastException, NullPointerException {
        return put(((Number) okey).intValue(), value);
    }

    /**
     * Removes the mapping for this key from this map if it is present. More
     * formally, if this map contains a mapping from key <code>k</code> to value
     * <code>v</code> such that <code>key == k</code>, that mapping is removed
     * (the map can contain at most one such mapping). <p>
     *
     * Returns the value to which this map previously associated the key, or
     * <code>null</code> if this map either contained no mapping for the key
     * or previously mapped it to <code>null</code>.
     *
     * It is guaranteed that under single-threaded access, this map will not
     * contain a mapping for the specified key at the point this method returns.
     *
     * @param key key whose mapping is to be removed from this map.
     * @return previous value associated with specified key, or
     *        <code>null</code> if either there was no
     *        mapping for key or it was explicitly
     *        mapped to <code>null</code>.
     */
    public Object remove(int key) {

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

                return e.value;
            }

            prev = e;
            e    = e.next;
        }

        return null;
    }

    /**
     * Removes the mapping for this key from this map if it is present. More
     * formally, if this map contains a mapping from key <code>k</code> to value
     * <code>v</code> such that <code>(((Number)key).intValue() == k)</code>,
     * that mapping is removed (the map can contain at most one such
     * mapping). <p>
     *
     * Returns the value to which this map previously associated this key, or
     * <code>null</code> if this map either contained no mapping for the key or
     * previously mapped it to <code>null</code>.
     *
     * It is guaranteed that under single-threaded access, this map will not
     * contain a mapping for the specified key at the point this method returns.
     *
     * @return previous value associated with specified key, or
     *        <code>null</code> if either there was no
     *        mapping for key or it was explicitly mapped
     *        to <code>null</code>.
     * @param okey key whose mapping is to be removed from this map.
     * @throws ClassCastException if the specified key is not an instance of
     *        <code>java.lang.Number</code>.
     * @throws NullPointerException if the specified key is <code>null</code>.
     */
    public Object remove(Object okey)
    throws ClassCastException, NullPointerException {
        return remove(((Number) okey).intValue());
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
     * Returns a shallow copy of this map: the keys and values themselves
     *  are not cloned.
     *
     * @return a clone of this map in which the mappings are identical.
     */
    public Object clone() {

        try {
            HsqlIntKeyHashMap m = (HsqlIntKeyHashMap) super.clone();

            m.table = new Entry[table.length];

            for (int i = table.length; i-- > 0; ) {
                m.table[i] = (table[i] != null) ? (Entry) table[i].clone()
                                                : null;
            }

            return m;
        } catch (CloneNotSupportedException e) {

            // This shouldn't happen, since we are Cloneable.
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Returns a string representation of this map in the form of a set of
     * entries, enclosed in braces and separated by the ASCII characters ", "
     * (comma and space). Each entry is rendered as the key, an equals sign =,
     * and the associated element, where the <code>toString</code> method is
     * used to convert the key and element to strings.
     *
     * @return a string representation of this map
     */
    public String toString() {

        // "=, ".length() + 2*(avg String.valueOf(int).length()
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
                sb.append(entry.key).append('=').append(
                    String.valueOf(entry.value)).append(", ");

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
