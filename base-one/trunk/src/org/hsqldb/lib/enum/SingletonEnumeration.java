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


package org.hsqldb.lib.enum;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * An <code>Enumeration</code> that enumerates a single object.<p>
 *
 * @author Campbell Boucher-Burnet, Cmaco & Associates
 */
public final class SingletonEnumeration implements Enumeration {

    /** the single object to be enumerated */
    private Object element;

    /**
     * Constructs a new SingletonEnumeration using the specified object
     *
     * @param element the single object to enumerate
     */
    public SingletonEnumeration(Object element) {
        this.element = element;
    }

    /**
     * Tests if this enumeration contains next element.
     *
     * @return  <code>true</code> if this enumeration contains it
     *          <code>false</code> otherwise.
     */
    public boolean hasMoreElements() {
        return element != null;
    }

    /**
     * Returns the next element of this enumeration.
     *
     * @return the next element
     * @throws NoSuchElementException if there is no next element
     */
    public Object nextElement() throws NoSuchElementException {

        if (element == null) {
            throw new NoSuchElementException();
        }

        Object tmp = element;

        element = null;

        return tmp;
    }
}
