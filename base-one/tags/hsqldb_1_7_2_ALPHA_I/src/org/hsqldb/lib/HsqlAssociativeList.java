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

/**
 * An interface that represents a list of items that can be accessed using
 * either the index of the item within the list or a key that is mapped to the
 * item within the list.  Once a key is in use, it cannot be mapped to a
 * different index unless it and its Entry are removed from the the associative
 * list.  Standard enumerations should be in the order of the list.
 *
 * @author  jcpeck@users
 * @since 05/31/2002
 * @version 05/31/2002
 */
public interface HsqlAssociativeList {

    public int size();

    public Object keyOf(int index);

    public int indexOf(Object key);

    public Entry add(Object key, Object value) throws KeyInUseException;

    public Entry insert(int index, Object key,
                        Object value) throws KeyInUseException;

    public Entry set(int index, Object key,
                     Object value) throws KeyInUseException;

    public Entry get(int index);

    public Entry get(Object key);

    public Entry remove(int index);

    public Entry remove(Object key);

    public Enumeration elements();

    /**
     * Class that represents and entry in an associative list.  It is merely a
     * triple containing the index, key, and value of the entry.  This class is
     * immutable.
     *
     * @author  jcpeck@users
     * @since 06/11/2002
     * @version 06/11/2002
     */
    public class Entry {

        //These fields are public because they are final.  They must remain final
        //to guarantee that this class is immutable.
        public final int    index;
        public final Object key;
        public final Object value;

        /**
         * Creates a new instance of HSQLAssociativeListEntry using the given
         * index, key, and value.
         */
        public Entry(int index, Object key, Object value) {

            this.index = index;
            this.key   = key;
            this.value = value;
        }

        public String toString() {
            return ("(" + index + ", " + key + ", " + value + ")");
        }
    }
}
