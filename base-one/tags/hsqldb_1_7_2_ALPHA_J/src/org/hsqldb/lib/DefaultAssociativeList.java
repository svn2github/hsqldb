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
import org.hsqldb.lib.HsqlAssociativeList.Entry;    //Needed to make javadoc work

/**
 * This is the default implementation of HsqlAssociativelist
 *
 * @author  jcpeck@users
 * @since 05/30/2002
 * @version 05/30/2002
 */
public class DefaultAssociativeList extends java.lang.Object
implements HsqlAssociativeList {

    /** Maps keys to their indexes within the entry list */
    private HsqlMap indexMap;

    /** Stores the entries in this */
    private HsqlList entryList;

    /**
     * Creates a new instance of HsqlMappedList.
     */
    public DefaultAssociativeList() {
        indexMap  = new HsqlHashMap();
        entryList = new HsqlArrayList();
    }

    public int size() {

        if (entryList.size() != indexMap.size()) {
            throw new RuntimeException("Uh oh");
        }

        return entryList.size();
    }

    /**
     * Returns the key within the map that corresponds to <code>index</code>.
     * @param index The index for which the coresponding key should be returned.
     * @return The index of a value mapped to the given key or -1 if the key is
     * not mapped to an index.
     */
    public Object keyOf(int index) {
        return (((Entry) entryList.get(index)).key);
    }

    /**
     * Returns the index within the list of the value that corresponds to <code>
     * key</code>.  If the key is not found in the map then -1 is returned.
     * @param key The that is to be looked up in the AssociativeList.
     * @return The index of a value mapped to the given key or -1 if the key is
     * not mapped to an index.
     */
    public int indexOf(Object key) {

        if (indexMap.containsKey(key)) {
            return ((Integer) indexMap.get(key)).intValue();
        } else {
            return -1;
        }
    }

    /**
     * Adds <code>value</code> to this at the end of the list and maps <code>
     * key</code> to the new entry.
     * @param key The key to which value should be mapped.
     * @param value The value to add.
     * @return The new Entry that was added to this.
     */
    public Entry add(Object key, Object value) throws KeyInUseException {
        return insert(entryList.size(), key, value);
    }

    /**
     * Inserts <code>value</code> at <code>index</code> and maps <code>key
     * </code> to the new entry.
     * @param key The key to which value should be mapped.
     * @param value The value to add.
     * @return The new Entry that was added to this.
     */
    public Entry insert(int index, Object key,
                        Object value) throws KeyInUseException {

        if (indexMap.containsKey(key)) {
            throw new KeyInUseException(key);
        }

        Entry entry = new Entry(index, key, value);

        entryList.add(index, entry);
        indexMap.put(key, new Integer(index));

        //The new entry has been inserted into the list.  So now remap
        //everything after the insertion index.
        remapIndexes(index + 1);

        return entry;
    }

    /**
     * Sets the <code>value</code> at <code>index</code> and maps <code>key
     * </code> to the new entry.  This will overwrite any entry that exists at
     * the given index.  The key mapped previously mapped to <code>index<code>
     * is removed.  If <code>key</code> is already in use in the map for any
     * index other than <code>index</code>, a KeyInUseException is thrown.
     * @param key The key to which value should be mapped.
     * @param value The value to add.
     * @return The new Entry that was added to this.
     */
    public Entry set(int index, Object key,
                     Object value) throws KeyInUseException {

        //Remove the key that previously mapped to the given index.
        indexMap.remove(((Entry) entryList.get(index)).key);

        //We just removed the key mapped to the given index, so if the given
        //key is in the map, it is mapped to the wrong index.
        if (indexMap.containsKey(key)) {
            throw new KeyInUseException(key);
        }

        Entry entry = new Entry(index, key, value);

        entryList.set(index, entry);
        indexMap.put(key, new Integer(index));

        return entry;
    }

    /**
     * Gets the Entry that is at <code>index</code>.
     * @param index The index of the entry to get.
     */
    public Entry get(int index) {
        return ((Entry) entryList.get(index));
    }

    /**
     * Returns the Entry at the index that is mapped to <code>key</code>.  If no
     * value is mapped to <code>key</code> null is returned.
     * @param key The key for which the corresponding Entry should be returned.
     * @return The Entry mapped to the given key or null if the key is not
     * mapped to an Entry.
     */
    public Entry get(Object key) {

        Integer index = (Integer) indexMap.get(key);

        return ((index == null) ? null
                                : (Entry) entryList.get(index.intValue()));
    }

    /**
     * Removes the Entry at <code>index</code> and returns it.
     * @param index The index of the entry to remove.
     * @return The Entry at <code>index</code>.
     */
    public Entry remove(int index) {

        Entry entry = (Entry) entryList.remove(index);

        indexMap.remove(entry.key);

        //Now that the element has been removed from the list.  So remap
        //everything after it
        remapIndexes(index);

        return entry;
    }

    /**
     * Removes the Entry mapped to <code>key</code> and returns it.
     * @param index The key of the entry to remove.
     * @return The Entry mapped to <code>key</code>.
     */
    public Entry remove(Object key) {

        Integer index = (Integer) indexMap.get(key);

        return (index == null ? null
                              : remove(index.intValue()));
    }

    /**
     * Returns an enumeration of the Entry objects in this in the order that
     * they were placed into the list.
     * @return An Enumeration of this in list order.
     */
    public Enumeration elements() {
        return entryList.elements();
    }

    public String toString() {
        return ("[DefaultAssociativeList | List:  " + entryList + ", Map:  "
                + indexMap + "]");
    }

    /**
     * Helper that remaps all indexes from the given index to the end of the
     * list.
     * @param index The index from which to begin the remapping process.
     */
    private void remapIndexes(int startIndex) {

        Entry entry;

        for (int i = entryList.size() - 1; i >= startIndex; i--) {
            entry = (Entry) entryList.get(i);

            entryList.set(i, new Entry(i, entry.key, entry.value));
            indexMap.put(entry.key, new Integer(i));
        }
    }
}
