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

import java.util.Hashtable;
import java.util.Enumeration;

/**
 * This is a hash table based implementation of HsqlMap.
 * <p>
 * All keys must have used in this map must have well-defined hashCode and
 * equals methods.  This means that all objects of the key data-type that are
 * equal according to the equals method must have the same hash code.  Also the
 * hash codes themselves should be as evenly distributed as possible.
 * <p>
 * This type of map has two parameters required to create an instance:
 * the initial capacity and the load factor.  Initial capacity is simply the
 * capacity at the time the map is created where capacity is defined as the
 * number of buckets in the table.
 * <p>
 * The load factor has to do with the type of hash table implementation.  This
 * map uses an open hash table.  That means each bucket stores a list of
 * elements that hash to that bucket.  This prevents the need for a rehash
 * method, but introduces a performance problem if many elements hash to the
 * same bucket.  The load factor is used to determine when the capacity should
 * be increased.  If the number of entries in the map exceeds the product of the
 * load factor and the current capacity, the capacity is increased.
 * <p>
 * The default value for load factor is 0.75 and the default initial capacity is
 * 11.
 * <p>
 * This temporary implementation is simply backed by a java.util.Hashtable.
 * This will allow the HsqlMap interface to be integrated into the HSQLDB
 * code, and at a later point this class can be replaced by a non-synchronized
 * hash table implementation.
 *
 * @author  jcpeck@users
 * @since 05/31/2002
 * @version 05/31/2002
 */
public class HsqlHashMap implements HsqlMap {

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
            System.out.println("HsqlHashMap init count: " + initCounter);
            System.out.println("HsqlHashMap update count: " + updateCounter);
        }
    }
    ;

    private static final int   DEFAULT_INITIAL_CAPACITY = 11;
    private static final float DEFAULT_LOAD_FACTOR      = 0.75f;

    /** A java Hashtable that backs this implementation of HsqlHashMap */
    private Hashtable table;

    /** Flag that indicates whether or not the null key is in use */
    private boolean nullKeyExists;

    /** The value that is mapped to the null key */
    private Object nullKeyMapping;

    /** Creates a new instance of HsqlHashMap */
    public HsqlHashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Creates a new instance of HsqlHashMap with the given initial capacity.
     * @param initialCapacity The initial number of buckets in the table.
     */
    public HsqlHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Creates a new instance of HsqlHashMap with the given initial capacity.
     * @param initialCapacity The initial number of buckets in the table.
     * @param loadFactor The load factor for this table, which is used to
     * control incrementing of the capacity.
     */
    public HsqlHashMap(int initialCapacity, float loadFactor) {

        reporter.initCounter++;

        table          = new Hashtable(initialCapacity, loadFactor);
        nullKeyExists  = false;
        nullKeyMapping = null;
    }

    /**
     * Removes all mappings and all keys from this map.  Effectively making it
     * an empty map.
     */
    public void clear() {

        table.clear();

        nullKeyExists  = false;
        nullKeyMapping = null;
    }

    /**
     * Tests if this map contains a mapping for the given key.
     * @param The key to test.
     * @return Whether or not this map contains a mapping for <code>key</code>.
     */
    public boolean containsKey(Object key) {

        if (key == null) {
            return nullKeyExists;
        }

        return table.containsKey(key);
    }

    /**
     * Tests if this map contains a mapping for the given value.
     * @param The value to test.
     * @return Whether or not this map contains a mapping for <code>key</code>.
     */
    public boolean containsValue(Object key) {
        return table.contains(key);
    }

    /**
     * Returns the object mapped to the given key.
     * @param key The <code>key</code> to look up in this map.
     * @return The element mapped to <code>key</code> or null if <code>key
     * </code> is not contained in the Map.
     */
    public Object get(Object key) {

        if (key == null) {
            return nullKeyMapping;
        }

        return table.get(key);
    }

    /**
     * Creates or updates a mapping in this map.  If <code>key</code> already
     * exists in the map then it is remapped to <code>value</code> and the
     * element for the old mapping is returned.  Otherwise a new mapping is
     * created that maps <code>key</code> to <code>value</code> and null is
     * returned.
     */
    public Object put(Object key, Object value) {

        reporter.updateCounter++;

        if (key == null) {
            nullKeyExists  = true;
            nullKeyMapping = value;
        }

        return table.put(key, value);
    }

    /**
     * Removes a mapping from this map.  The key and its corresponding element
     * are taken out of the map.
     * @param key The key of the mapping to remove.
     * @return The value, mapped to <code>key</code> that is being removed.
     */
    public Object remove(Object key) {

        reporter.updateCounter++;

        if (key == null) {
            nullKeyExists  = false;
            nullKeyMapping = null;
        }

        return table.remove(key);
    }

    /**
     * Accessor for the number of mappings in this map.
     * @return The numbr of mappings in this map.
     */
    public int size() {
        return (nullKeyExists ? table.size() + 1
                              : table.size());
    }

//fredt@users - temp stuff
//when the hybrid map/list is ready, we can use that where iteration is
//required so we probably won't need these in the end
    public Enumeration keys() {
        return table.keys();
    }

    public Enumeration elements() {
        return table.elements();
    }

    public String toString() {
        return table.toString();
    }
}
