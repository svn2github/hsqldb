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
 * An <code>Enumeration</code> class that can be used to represent an
 * enumeration with no elements. <p>
 *
 * This class follows the Singleton pattern.  The single instance must
 * be accessed via the <code>instance</code> member variable.
 *
 * @author Campbell Boucher-Burnet, Camco & Associates
 */
public final class EmptyEnumeration implements Enumeration {

    /** the single instance of this class, to be used by all clients. */
    public static final Enumeration instance = new EmptyEnumeration();

    /**
     * Constructs a new EmptyEnumeration instance. <p>
     *
     * This constructor is private since only one instance is ever
     * required, and it can be obtained through the <code>instance</code>
     * member.
     */
    private EmptyEnumeration() {}

    /**
     * Tests if this enumeration contains a next element, which it never does.
     *
     * @return <code>false</code> always
     */
    public final boolean hasMoreElements() {
        return false;
    }

    /**
     * Always throws NoSuchElementException.
     *
     * @return     never
     * @exception  NoSuchElementException  always
     */
    public final Object nextElement() throws NoSuchElementException {
        throw new NoSuchElementException();
    }
}
