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
 * An Enumeration that Composes either an enumeration of enumerations or
 * two specified enumerations into a single enumeration of the specified
 * enumerations' elements. <p>
 *
 * @author boucherb@users.sourceforge.net
 * @version 1.7.2
 * @since HSQLDB 1.7.2
 */
public final class CompositeEnumeration implements Enumeration {

    /**
     * An enumeration whose elements are themselves Enumerations
     */
    private Enumeration enum = null;

    /** current enumeration selected from enum */
    private Enumeration currentEnumeration = null;

    /**
     * Constructs a new CompositeEnumeration that enumerates, in order, the
     * elements of the enumerations enumerated by the specified enumeration.
     *
     * @param enumeration an enumeration of enumerations
     */
    public CompositeEnumeration(Enumeration enumeration) {
        enum = enumeration;
    }

    /**
     * Constructs a new CompositeEnumeration that enumerates the
     * elements enumerated by enumeration a and b, in order. <p>
     * @param a the first enumeration
     * @param b the second enumeration
     */
    public CompositeEnumeration(Enumeration a, Enumeration b) {

        this(new HsqlEnumeration(new Enumeration[] {
            a, b
        }));
    }

    /**
     * Tests if this enumeration contains next element.
     *
     * @return <code>true</code> if this enumeration contains it;
     *          <code>false</code> otherwise.
     */
    public boolean hasMoreElements() {

        if (enum == null) {
            return false;
        }

        if (currentEnumeration != null
                && currentEnumeration.hasMoreElements()) {
            return true;
        }

        while (enum.hasMoreElements()) {
            currentEnumeration = (Enumeration) enum.nextElement();

            if (currentEnumeration != null
                    && currentEnumeration.hasMoreElements()) {
                return true;
            }
        }

        // we are done: release any objects we hold for garbage collection
        enum               = null;
        currentEnumeration = null;

        return false;
    }

    /**
     * Returns the next element of this enumeration.
     *
     * @return the next element
     * @throws NoSuchElementException if there is no next element
     */
    public Object nextElement() {

        if (hasMoreElements()) {
            return currentEnumeration.nextElement();
        }

        throw new NoSuchElementException();
    }
}
