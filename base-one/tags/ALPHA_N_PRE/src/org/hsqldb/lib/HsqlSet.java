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

/** A collection that contains no duplicate elements. More formally, sets contain
 * no pair of elements <code>e1</code> and <code>e2</code> such that
 * <code>e1.equals(e2)</code>, and at most one <code>null</code>
 * element. As implied by its name, this interface models the mathematical set
 * abstraction. <p>
 *
 * The HsqlSet interface places an additional stipulation, beyond those inherited
 * from the general abstraction that is a collection, upon the contracts of all
 * constructors and on the contracts of the add, equals and hashCode methods.
 *
 * The additional stipulation on constructors is, not surprisingly, that all
 * constructors must create a set that contains no duplicate elements
 * (as defined above).
 *
 * Note: Great care must be exercised if mutable objects are used as set elements.
 * The behavior of a set is not specified if the value of an object is changed in
 * a manner that affects equals comparisons while the object is an element in the
 * set. A special case of this prohibition is that it is not permissible for a
 * set to contain itself as an element.
 *
 * Some set implementations may have restrictions on the elements that they may
 * contain. For example, some implementations may prohibit null elements, and some
 * may have restrictions on the types of their elements. Attempting to add an
 * ineligible element may throw an unchecked exception, typically
 * NullPointerException or ClassCastException. Attempting to query the presence
 * of an ineligible element may throw an exception, or it may simply return
 * false; some implementations may exhibit the former behavior and some may
 * exhibit the latter. More generally, attempting an operation on an ineligible
 * element whose completion would not result in the insertion of an ineligible
 * element into the set may throw an exception or it may succeed, at the
 * option of the implementation. <p>
 *
 * @author boucherb@users.sourceforge.net
 * @version 1.7.2
 * @since 1.7.2
 */
public interface HsqlSet {

    /** Adds the specified element to this set if it is not already present.
     * More formally, adds the specified element, o, to this set if this set contains
     * no element e such that (o==null ? e==null : o.equals(e)). If this set already
     * contains the specified element, the call leaves this set unchanged and
     * returns false. In combination with the restriction on constructors, this
     * ensures that sets never contain duplicate elements.
     * The stipulation above does not imply that sets must accept all elements;
     * sets may refuse to add any particular element, including null, by throwing
     * an exception. Individual set implementations should clearly document any
     * restrictions on the the elements that they may contain.
     * @return true if this set did not already contain the specified element
     * @param obj element to be added to this set.
     * @throws UnsupportedOperationException if the add method is not supported by this set
     * @throws ClassCastException if the class of the specified element prevents it from being added to this set.
     * @throws NullPointerException if the specified element is null and this set does not support null elements.
     * @throws IllegalArgumentException if some aspect of the specified element prevents it from being added to this set.
     */
    public boolean add(Object obj)
    throws UnsupportedOperationException,
           ClassCastException,
           NullPointerException,
           IllegalArgumentException;

    /** Adds all of the elements in the specified set to this set if they're not
     * already present (optional operation). This operation effectively modifies
     * this set so that its value is the union of the two sets. The behavior
     * of this operation is unspecified if either set is modified while
     * the operation is in progress.
     * @param set the set whose elements are to be added to this set
     * @return <code>true</code> iff this set changed as a result of the call.
     *
     * @throws UnsupportedOperationException if the addAll method is not supported by this set
     * @throws ClassCastException if the class of any of the elements to add prevents them from being added
     *        to this set.
     * @throws NullPointerException if the specified set is null or any of the elements to add are null and
     * this set does not support the addition of null elements.
     * @throws IllegalArgumentException if some aspect of any of the elements to add prevents them from being added
     *        to this set.
     */
    public boolean addAll(HsqlSet set)
    throws UnsupportedOperationException,
           ClassCastException,
           NullPointerException,
           IllegalArgumentException;

    /** Removes all of the elements from this set (optional operation). <p>
     *
     * This set will be empty after this call returns (unless it throws an exception).
     * @throws UnsupportedOperationException if the clear method is not supported by this set.
     */
    public void clear() throws UnsupportedOperationException;

    /** Returns true if this set contains the specified element. More formally,
     * returns true if and only if this set contains an element e such that
     * (o==null ? e==null : o.equals(e)).
     * @return true if this set contains the specified element.
     * @param obj element whose presence in this set is to be tested.
     * @throws ClassCastException if the type of the specified element is incompatible with this set (optional).
     * @throws NullPointerException if the specified element is null and this set does not support null
     * elements (optional).
     */
    public boolean contains(Object obj)
    throws ClassCastException, NullPointerException;

    /** Returns true if this set contains all of the elements of the specified
     * set.
     * @return <code>true</code> iff this set contains all of the elements of the specified set
     * @throws ClassCastException if the types of one or more elements in the specified collection are
     * incompatible with this set (optional).
     * @throws NullPointerException if the specified set is null or the specified set contains one or
     *        more null elements and this set does not support null
     *        elements (optional).
     * @param set the set to be checked for containment in this set.
     */
    public boolean containsAll(HsqlSet set)
    throws ClassCastException, NullPointerException;

    /** Returns an enumeration of the elements in this set. The general contract for
     * the elements method is that an Enumeration is returned that will generate
     * all the elements contained in this set.
     * @return an enumeration over all of the elements in this set.
     */
    public Enumeration elements();

    /** Compares the specified object with this set for equality. Returns true if
     * the specified object is also a set, the two sets have the same size,
     * and every member of the specified set is contained in this set (or
     * equivalently, every member of this set is contained in the specified set).
     * This definition ensures that the equals method works properly across different
     * implementations of the HsqlSet interface.
     * @param obj <code>Object</code> to be compared for equality with this set.
     * @return <code>true</code> if the specified <code>Object</code> is equal to this set.
     */
    public boolean equals(Object obj);

    /** Returns the hash code value for this set. <p>
     *
     * The hash code of a set is defined to be the sum of the hash codes of the
     * elements in the set, where the hashcode of a null element is defined to be
     * zero. This ensures that s1.equals(s2) implies that s1.hashCode()==s2.hashCode()
     * for any two sets s1 and s2, as required by the general contract of the
     * Object.hashCode method.
     * @return the hash code value for this set.
     */
    public int hashCode();

    /** Returns true if this set contains no elements.
     * @return true if this set contains no elements.
     */
    public boolean isEmpty();

    /** Removes the specified element from this set if it is present
     * (optional operation). More formally, removes an element e such
     * that (o==null ? e==null : o.equals(e)), if the set contains such an
     * element. Returns true if the set contained the specified element
     * (or equivalently, if the set changed as a result of the call).
     * (The set will not contain the specified element once the call returns.)
     * @param obj <code>Object</code> to be removed from this set, if present.
     * @return true if the set contained the specified element.
     * @throws ClassCastException if the type of the specified element is incompatible with this set (optional)
     * @throws NullPointerException if the specified element is null and this set does not support null
     * elements (optional).
     * @throws UnsupportedOperationException if the remove method is not supported by this set.
     */
    public boolean remove(Object obj)
    throws ClassCastException,
           NullPointerException,
           UnsupportedOperationException;

    /** Removes from this set all of its elements that are contained in the
     * specified set (optional operation). This operation effectively modifies
     * this set so that its value is the asymmetric set difference of the two sets.
     * @param set the set that defines which elements will be removed from this set
     * @return true if this set changed as a result of the call
     * @throws UnsupportedOperationException if the removeAll method is not supported by this set.
     * @throws ClassCastException if the types of one or more elements in this set are incompatible with the
     * specified set (optional).
     * @throws NullPointerException if the specified set is null or if this set contains a null element and the
     * specified set does not support null elements (optional).
     */
    public boolean removeAll(HsqlSet set)
    throws UnsupportedOperationException,
           ClassCastException,
           NullPointerException;

    /** Retains only the elements in this set that are contained in the specified
     * set (optional operation). In other words, removes from this set all of its
     * elements that are not contained in the specified set. This operation
     * effectively modifies this set so that its value is the intersection of
     * the two sets.
     * @param set set that defines which elements this set will retain
     * @return true if this set changed as a result of the call.
     * @throws UnsupportedOperationException - if the retainAll method is not supported by this set.
     * @throws ClassCastException if the types of one or more elements in this set are incompatible with the
     * specified set (optional).
     * @throws NullPointerException if the specified set is null or this set contains a null element and the
     * specified set does not support null elements (optional).
     */
    public boolean retainAll(HsqlSet set)
    throws UnsupportedOperationException,
           ClassCastException,
           NullPointerException;

    /** Returns the number of elements in this set (its cardinality). <p>
     *
     * In the unlikely event that this set contains more than
     * Integer.MAX_VALUE elements, returns Integer.MAX_VALUE.
     * @return the number of elements in this set (its cardinality).
     */
    public int size();

    /** Returns an array containing all of the elements in this set.
     * @return an array containing all of the elements in this set.
     */
    public Object[] toArray();

    /** Returns an array containing all of the elements in this set; the runtime
     * type of the returned array is that of the specified array.
     * @param obj the array into which the elements of this set are to be stored,
     *        if it is big enough; otherwise, a new array of the same
     *        runtime type is allocated for this purpose.
     * @return an array containing the elements of this set.
     * @throws ArrayStoreException if the runtime type of a is not a supertype of the runtime type of
     * every element in this set.
     * @throws NullPointerException if the specified array is null.
     */
    public Object[] toArray(Object[] obj)
    throws ArrayStoreException, NullPointerException;

}
