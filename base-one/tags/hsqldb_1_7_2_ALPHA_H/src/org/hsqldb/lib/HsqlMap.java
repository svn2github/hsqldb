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

/**
 * Interface that represents a generic mapping between keys and elements.  Both
 * the keys and the elements are stored as Objects.  Keys are mapped to one and
 * only one element, and an HsqlMap cannot contain duplicate keys.  No
 * guarantees are made about what will happen if a mutable object is used as a
 * key and it is changed after being inserted into the Map.
 *
 * @author  jcpeck@users
 * @since 05/30/2002
 * @version 05/30/2002
 */
public interface HsqlMap {

    /**
     * Removes all mappings and all keys from this map.  Effectively making it
     * an empty map.
     */
    public void clear();

    /**
     * Tests if this map contains a mapping for the given key.
     * @param The key to test.
     * @returns Whether or not this map contains a mapping for <code>key</code>.
     */
    public boolean containsKey(Object key);

    /**
     * Returns the object mapped to the given key.
     * @param key The <code>key</code> to look up in this map.
     * @returns The element mapped to <code>key</code> or null if <code>key
     * </code> is not contained in the Map.
     */
    public Object get(Object key);

    /**
     * Creates or updates a mapping in this map.  If <code>key</code> already
     * exists in the map then it is remapped to <code>value</code> and the
     * element for the old mapping is returned.  Otherwise a new mapping is
     * created that maps <code>key</code> to <code>value</code> and null is
     * returned.
     */
    public Object put(Object key, Object value);

    /**
     * Removes a mapping from this map.  The key and its corresponding element
     * are taken out of the map.
     * @param key The key of the mapping to remove.
     * @returns The value, mapped to <code>key</code> that is being removed.
     */
    public Object remove(Object key);

    /**
     * Accessor for the number of mappings in this map.
     * @returns The numbr of mappings in this map.
     */
    public int size();
}
