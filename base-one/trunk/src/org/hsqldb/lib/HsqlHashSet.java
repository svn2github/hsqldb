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

import java.lang.reflect.Array;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.NoSuchElementException;

/** This class implements the <code>HsqlSet</code> interface, backed by a
 * <code>Hashtable</code>. It makes no guarantees as to the elements()
 * or toArray() order of the set; in particular, it does not guarantee
 * that the order will remain constant over time. This class permits
 * the <code>null</code> element.
 *
 * This class offers constant time performance for the basic operations
 * (add, remove, contains and size), assuming the hash function disperses
 * the elements properly among the buckets. Enumerating or converting
 * this set to an array requires time proportional to the sum of the
 * backing Hashtable's size (the number of elements) plus the "capacity"
 * of the backing Hashtable (the number of buckets).
 *
 * Note that this implementation is not synchronized. If multiple threads
 * access a set concurrently, and at least one of the threads modifies the
 * set, it must be synchronized externally if consistent and predicatble
 * results are required. This is typically accomplished by synchronizing
 * on some object that naturally encapsulates the set. <p>
 *
 * @author boucherb@users.sourceforge.net
 * @version 1.7.2
 * @since 1.7.2
 */
public final class HsqlHashSet implements HsqlSet {

    /** the <code>Hashtable</code> that backs this <code>HashSet</code>
     * implementation.
     */
    private Hashtable map;

    /** Dummy value to associate with a set element Object key in the
     * backing Map.
     */
    private static final Object PRESENT = new Object();
    
    /** hash code calculation in progress? 
     * - guards against cycles of containment
     */
    private boolean hcip = false;

    /** Constructs a new HsqlHashSet object. */
    public HsqlHashSet() {
        map = new Hashtable();
    }

    /** Adds the specified element to this set, if it is not already present.
     * @param o element to be added to this set.
     * @return <tt>true</tt> if the set did not already contain
     *        the specified element.
     */
    public boolean add(Object o) {
        return null == map.put(((o == null) ? PRESENT : o), PRESENT);
    }

    /** Adds all of the elements in the specified <code>HsqlSet</code> object
     * to this set if they're not already present. This operation effectively
     * modifies this set so that its value is the union of the two sets. The
     * behavior of this operation is unspecified if either set is modified
     * while the operation is in progress.
     * @param hs HsqlSet whose elements are to be
     *        added to this set
     * @return <code>true</code> if this set changed as a
     *        result of the call.
     */
    public boolean addAll(HsqlSet hs) {
        boolean changed = false;
        for (Enumeration e = hs.elements(); e.hasMoreElements();) {
            if (add(e.nextElement()))  {
                changed = true;
            }
        }
        return changed;
    }

    /** Removes the specified element from this set, if it is present.
     * @param o object to be removed from this set,
     *        if present.
     * @return <tt>true</tt> if the set contained
     *        the specified element.
     */
    public boolean remove(Object o) {
        return PRESENT == map.remove(((o == null) ? PRESENT : o));
    }

    /**
     * Removes all of the elements from this set.
     */
    public void clear() {
        map.clear();
    }

    /** Retrieves whether this set contains the specified object
     * @param o the object to test for containment.
     * @return true if this HashSet contains o, else false.
     */
    public boolean contains(Object o) {
        return map.containsKey(((o == null) ? PRESENT : o));
    }

    /** Retrieves whether this object is the empty set.  That is
     * it returns <code>true</code> iff <code>size() == 0</code>.
     * @return true if this HashSet object contains no elements,
     *        else false.
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /** Retrieve the number of elements this set contains.
     * @return the number of elements this HashSet contains
     */
    public int size() {
        return map.size();
    }

    /** Retreives an Enumeration of the elements in this set.
     * @return an Enumeration of the elements in this set.
     */
    public Enumeration elements() {

        return new Enumeration() {

            Enumeration e = map.keys();

            public boolean hasMoreElements() {
                return e.hasMoreElements();
            }

            public Object nextElement() throws NoSuchElementException{
                Object o = e.nextElement();
                return (o == PRESENT)
                       ? null
                       : o;
            }
        };
    }

    /** Retrieves an array containing all of the elements in this set.
     * @return an array containing all of the elements in this set.
     */
    public Object[] toArray() {
        int count = 0;
        Object[] out = new Object[size()];
        Enumeration e = elements();
        while (e.hasMoreElements()) {
            out[count++] = e.nextElement();
        }
        return out;
    }

    /** Retreives an array containing all of the elements in this set; the
     * runtime type of the returned array is that of the specified array.
     * @param obj the array into which the elements of this set are to be
     *        stored, if it is big enough; otherwise, a new
     *        array of the same runtime type is allocated for
     *        this purpose.
     * @return an array containing the elements of this set.
     */
    public Object[] toArray(Object[] obj) {
        int size = size();
        if (obj.length < size) {
            Class c = obj.getClass().getComponentType();
            obj = (Object[]) Array.newInstance(c, size);
        }
        int count = 0;
        Enumeration e = elements();
        while (e.hasMoreElements()) {
            obj[count++] = e.nextElement();
        }
        while (count < size) {
            obj[count++] = null;
        }
        return obj;
    }

    /** Returns the hash code value for this set. The hash code of a set is
     * defined to be the sum of the hash codes of the elements in the set.
     * This ensures that s1.equals(s2) implies that
     * s1.hashCode()==s2.hashCode() for any two sets s1 and s2, as required
     * by the general contract of Object.hashCode. <p>
     * @return the hash code value for this set.
     */
    public int hashCode() {
        
// boucherb@users 20030225 - patch to comply with JDK 1.1 
// Oops.  Can't use java.util.Hashtable equals/hashCode...
// They are 1.2+ features.        
// Double Oops!  What if we are contained in our own map somehow?
// Hopefully, this prevents cycles.       
            
        int             hc;
        Enumeration     e;
        
        hc = 0;       
        
        if (hcip) {
            return 0;
        }
        
        hcip  = true;
        e     = map.keys();
        
        while (e.hasMoreElements()) {
            hc += e.nextElement().hashCode();
        }
        
        hcip = false;
        
        return hc;

    }

    /** Compares the specified object with this set for equality. <p>
     *
     * Returns <code>true</code> if the given object is also a set, the
     * two sets have the same size, and every member of the given set is
     * contained in this set. This ensures that the equals method works
     * properly across different implementations of the <code>HsqlSet</code>
     * interface.
     * @param obj <code>Object</code> to be compared for
     *        equality with this set.
     * @return true if the specified object is
     *        equal to this set.
     */
    public boolean equals(Object obj) {
        
// boucherb@users 20030225 - patch to comply with JDK 1.1
// Oops.  Can't use java.util.Hashtable equals/hashCode...
// They are 1.2+ features.
        
        if (this == obj) {
            return true;
        }
        
        if (!(obj instanceof HsqlHashSet)) {
            return false;
        }

        HsqlHashSet that = (HsqlHashSet) obj;
        
        if (this.size() != that.size()) {
            return false;
        }
        
        return this.containsAll(that);
    }

    /** Returns true if this set contains all of the elements of
     * the specified set. <p>
     * @param hs the set to be checked for containment
     *    in this set.
     * @return <code>true</code> if this set contains
     *        all of the elements of the
     *        specified set
     */
    public boolean containsAll(HsqlSet hs) {
        for (Enumeration e = hs.elements(); e.hasMoreElements();) {
            if (!this.contains(e.nextElement()))  {
                return false;
            }
        }
        return true;
    }

    /** Removes from this set all of its elements that are contained
     * in the specified set.
     * @param hs the set that defines which elements will
     * be removed from this set
     * @return <code>true</code> if this set changed
     * as a result of the call.
     */
    public boolean removeAll(HsqlSet hs) {

        boolean changed = false;

        if (size() > hs.size()) {
            for (Enumeration e = hs.elements(); e.hasMoreElements(); ) {
                changed |= remove(e.nextElement());
            }
        } else {
            for (Enumeration e = this.elements(); e.hasMoreElements(); ) {
                Object o = e.nextElement();
                if (hs.contains(o)) {
                    this.remove(o);
                    changed = true;
                }
            }
        }

        return changed;
    }

    /** Retains only the elements in this set that are contained in
     * the specified set.
     * @param hs the set that defines which elements
     *        this set will retain.
     * @return true if this collection changed as a
     *        result of the call.
     */
    public boolean retainAll(HsqlSet hs) {
        boolean changed = false;
        for (Enumeration e = this.elements(); e.hasMoreElements();) {
            Object o = e.nextElement();
            if (hs.contains(o))  {
            } else {
                this.remove(o);
                changed = true;
            }
        }
        return changed;
    }

}
