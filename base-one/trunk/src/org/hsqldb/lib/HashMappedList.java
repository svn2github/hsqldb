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

public class HashMappedList extends HashMap {

    public HashMappedList() {
        this(16, 0.75f);
    }

    public HashMappedList(int initialCapacity)
    throws IllegalArgumentException {
        this(initialCapacity, 0.75f);
    }

    public HashMappedList(int initialCapacity,
                          float loadFactor) throws IllegalArgumentException {
        super(initialCapacity, loadFactor);
    }

    public Object get(int index) throws IndexOutOfBoundsException {

        checkRange(index);

        return objectValueTable[index];
    }

    public Object remove(Object key) {

        int lookup = getLookup(key, key.hashCode());

        if (lookup < 0) {
            return null;
        }

        Object returnValue = super.remove(key);

        removeRow(lookup);

        return returnValue;
    }

    public Object remove(int index) throws IndexOutOfBoundsException {

        checkRange(index);

        Object returnValue = objectValueTable[index];

        removeRow(index);

        return returnValue;
    }

    public boolean add(Object key, Object value) {

        if (keySet().contains(key)) {
            return false;
        }

        super.put(key, value);

        return true;
    }

    public Object put(Object key, Object value) {
        return super.put(key, value);
    }

    public Object set(int index,
                      Object value) throws IndexOutOfBoundsException {

        checkRange(index);

        Object returnValue = objectKeyTable[index];

        objectKeyTable[index] = value;

        return returnValue;
    }

    public boolean set(int index, Object key,
                       Object value) throws IndexOutOfBoundsException {

        checkRange(index);

        if (keySet().contains(key) && getIndex(key) != index) {
            return false;
        }

        super.remove(objectKeyTable[index]);
        super.put(key, value);

        return true;
    }

    public boolean setKey(int index,
                          Object key) throws IndexOutOfBoundsException {

        checkRange(index);

        Object value = objectValueTable[index];

        return set(index, key, value);
    }

    public Object getKey(int index) throws IndexOutOfBoundsException {

        checkRange(index);

        return objectKeyTable[index];
    }

    public int getIndex(Object key) {
        return getLookup(key, key.hashCode());
    }

    private void checkRange(int i) {

        if (i < 0 || i >= size()) {
            throw new IndexOutOfBoundsException();
        }
    }
}
