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


package org.hsqldb.store;

/**
 * A chained bucket hash index implementationl
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
     * @param hashTableSize
     * @param capacity
     */
    void reset(int hashTableSize, int capacity) {

        hashTable = new int[hashTableSize];

        int   to       = hashTable.length;
        int[] intArray = hashTable;

        while (--to >= 0) {
            intArray[to] = -1;
        }

        linkTable      = new int[capacity];
        newNodePointer = 0;
        elementCount   = 0;
    }

    /**
     * @param hash
     */
    int getHashIndex(int hash) {
        return (hash & 0x7fffffff) % hashTable.length;
    }

    /**
     * @param hash the hash value used for indexing
     * @return either -1 or the first node for this hash value
     */
    int getLookup(int hash) {

        int index = (hash & 0x7fffffff) % hashTable.length;

        return hashTable[index];
    }

    /**
     * @param valid lookup node to look from
     * @return either -1 or the next node from this node
     */
    int getNextLookup(int lookup) {
        return linkTable[lookup];
    }

    /**
     * reset the index as empty
     */
    void clear() {

        int   to       = hashTable.length;
        int[] intArray = hashTable;

        while (--to >= 0) {
            intArray[to] = -1;
        }

        to       = linkTable.length;
        intArray = linkTable;

        while (--to >= 0) {
            intArray[to] = 0;
        }

        elementCount   = 0;
        newNodePointer = 1;
    }

    /**
     * link a new node to the end of the linked for a hash index
     * @param index an index into hashTable
     * @param lastLookup either 0 or the node to which the new node will be linked
     * @return the new node
     */
    int linkNode(int index,
                 int lastLookup) throws ArrayIndexOutOfBoundsException {

        // get the first reclaimed slot
        int lookup = this.reclaimedNodePointer;

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
     * unlink a node a linked list and link into the reclaimed list
     * @param index an index into hashTable
     * @param lastLookup either 0 or the node to which the target node is linked
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
     * remove a node that has already been unlinked
     * @param lookup the node to remove
     * @return true if node found in unlinked state
     */
    boolean removeEmptyNode(int lookup) {

        boolean found      = false;
        int     lastLookup = -1;

        for (int i = this.reclaimedNodePointer; i >= 0;
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
