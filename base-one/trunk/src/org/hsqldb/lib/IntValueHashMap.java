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

import java.util.NoSuchElementException;
import org.hsqldb.store.BaseHashMap;

public class IntValueHashMap extends BaseHashMap {

    Set        keySet;
    public IntValueHashMap() {
        this(16, 0.75f);
    }

    public IntValueHashMap(int initialCapacity)
    throws IllegalArgumentException {
        this(initialCapacity, 0.75f);
    }

    public IntValueHashMap(int initialCapacity,
                           float loadFactor) throws IllegalArgumentException {
        super(initialCapacity, loadFactor, keyOrValueTypes.objectKeyOrValue,
              keyOrValueTypes.intKeyOrValue);
    }

    public int get(Object key) throws NoSuchElementException {
        return super.getInt(key);
    }

    public int get(Object key, int defaultValue){
        return super.getInt(key, defaultValue);
    }

    public boolean get(Object key, int[] value){
        return super.getInt(key, value);
    }

    public void put(Object key, int value) {
        super.addOrRemove(0, value, key, null, false);
    }

    public void remove(Object key) {
        super.addOrRemove(0, 0, key, null, true);
    }

    public boolean containsKey(Object key) {
        return super.containsKey(key);
    }

    public boolean containsValue(int value) {
        throw new RuntimeException();
    }
    public Set keySet() {

        if (keySet == null) {
            keySet = new KeySet();
        }

        return keySet;
    }

    class KeySet implements Set {

        public Iterator iterator() {
            return IntValueHashMap.this.new BaseHashIterator(true);
        }

        public int size() {
            return IntValueHashMap.this.size();
        }

        public boolean contains(Object o) {
            return containsKey(o);
        }

        public boolean add(Object value) {
            throw new RuntimeException();
        }

        public boolean remove(Object o) {

            int oldSize = size();

            IntValueHashMap.this.remove(o);

            return size() != oldSize;
        }

        public boolean isEmpty() {
            return size() == 0;
        }

        public void clear() {
            IntValueHashMap.this.clear();
        }
    }
}
