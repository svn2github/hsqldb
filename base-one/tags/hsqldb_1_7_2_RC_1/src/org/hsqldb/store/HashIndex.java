/* Copyright (c) 2001-2003, The HSQL Development Group
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


package org.hsqldb.store;

/**
 * A chained bucket hash index implementation.
 *
 * hashTable and linkTable are arrays of signed integral types. This
 * implementation uses int as the type but short or byte can be used for
 * smaller index sizes (cardinality).
 *
 * hashTable[index] contains the pointer to the first node with
 * (index == hash modulo hashTable.length) or -1 if there is no corresponding
 * node. linkTable[{0,newNodePointer}] (the range between 0 and newNodePointer)
 * contains either the pointer to the next node or -1 if there is no
 * such node. reclaimedNodeIndex contains a pointer to an element
 * of linkTable which is the first element in the list of reclaimed nodes
 * (nodes no longer in index) or -1 if there is no such node.
 *
 * elemenet at and above linkTable[newNodePointer] have never been used
 * as a node and their contents is not significant.
 *
 * @author fredt@users
 * @version 1.7.2
 * @since 1.7.2
 */
class HashIndex {

    int[]   hashTable;
    int[]   linkTable;
    int     newNodePointer;
    int     elementCount;
    int     reclaimedNodePointer = -1;
    boolean fixedSize;

    HashIndex(int hashTableSize, int capacity, boolean fixedSize) {

        reset(hashTableSize, capacity);

        this.fixedSize = fixedSize;
    }

    /**
     * Reset the structure with a new size as empty.
     *
     * @param hashTableSize
     * @param capacity
     */
    void reset(int hashTableSize, int capacity) {

        int[] newHT = new int[hashTableSize];
        int[] newLT = new int[capacity];

        // allocate memory before assigning
        hashTable = newHT;
        linkTable = newLT;

        resetTables();
    }

    void resetTables() {

        int   to       = hashTable.length;
        int[] intArray = hashTable;

        while (--to >= 0) {
            intArray[to] = -1;
        }

        newNodePointer       = 0;
        elementCount         = 0;
        reclaimedNodePointer = -1;
    }

    /**
     * Reset the index as empty.
     */
    void clear() {

        int   to       = linkTable.length;
        int[] intArray = linkTable;

        while (--to >= 0) {
            intArray[to] = 0;
        }

        resetTables();
    }

    /**
     * @param hash
     */
    int getHashIndex(int hash) {
        return (hash & 0x7fffffff) % hashTable.length;
    }

    /**
     * Return the array index for a hash.
     *
     * @param hash the hash value used for indexing
     * @return either -1 or the first node for this hash value
     */
    int getLookup(int hash) {

        int index = (hash & 0x7fffffff) % hashTable.length;

        return hashTable[index];
    }

    /**
     * This looks from a given node, so the parameter is always > -1.
     *
     * @param valid lookup node to look from
     * @return either -1 or the next node from this node
     */
    int getNextLookup(int lookup) {
        return linkTable[lookup];
    }

    /**
     * Link a new node to the end of the linked for a hash index.
     *
     * @param index an index into hashTable
     * @param lastLookup either -1 or the node to which the new node will be linked
     * @return the new node
     */
    int linkNode(int index, int lastLookup) {

        // get the first reclaimed slot
        int lookup = reclaimedNodePointer;

        if (lookup == -1) {
            lookup = newNodePointer++;
        } else {

            // reset the first reclaimed slot
            reclaimedNodePointer = linkTable[lookup];
        }

        // link the node
        if (lastLookup == -1) {
            hashTable[index] = lookup;
        } else {
            linkTable[lastLookup] = lookup;
        }

        linkTable[lookup] = -1;

        elementCount++;

        return lookup;
    }

    /**
     * Unlink a node from a linked list and link into the reclaimed list.
     *
     * @param index an index into hashTable
     * @param lastLookup either -1 or the node to which the target node is linked
     * @param lookup the node to remove
     */
    void unlinkNode(int index, int lastLookup, int lookup) {

        // unlink the node
        if (lastLookup == -1) {
            hashTable[index] = linkTable[lookup];
        } else {
            linkTable[lastLookup] = linkTable[lookup];
        }

        // add to reclaimed list
        linkTable[lookup]    = reclaimedNodePointer;
        reclaimedNodePointer = lookup;

        elementCount--;
    }

    /**
     * Remove a node that has already been unlinked. This is not required
     * for index operations. It is used only when the row needs to be removed
     * from the data structures that store the actual indexed data and the
     * nodes need to be contiguous.
     *
     * @param lookup the node to remove
     * @return true if node found in unlinked state
     */
    boolean removeEmptyNode(int lookup) {

        boolean found      = false;
        int     lastLookup = -1;

        for (int i = reclaimedNodePointer; i >= 0;
                lastLookup = i, i = linkTable[i]) {
            if (i == lookup) {
                if (lastLookup == -1) {
                    reclaimedNodePointer = linkTable[lookup];
                } else {
                    linkTable[lastLookup] = linkTable[lookup];
                }

                found = true;

                break;
            }
        }

        if (!found) {
            return false;
        }

        for (int i = 0; i < newNodePointer; i++) {
            if (linkTable[i] > lookup) {
                linkTable[i]--;
            }
        }

        System.arraycopy(linkTable, lookup + 1, linkTable, lookup,
                         newNodePointer - lookup - 1);

        linkTable[newNodePointer - 1] = 0;

        newNodePointer--;

        for (int i = 0; i < hashTable.length; i++) {
            if (hashTable[i] > lookup) {
                hashTable[i]--;
            }
        }

        return true;
    }
}
