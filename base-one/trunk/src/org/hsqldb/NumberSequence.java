/* Copyrights and Licenses
 *
 * This product includes Hypersonic SQL.
 * Originally developed by Thomas Mueller and the Hypersonic SQL Group. 
 *
 * Copyright (c) 1995-2000 by the Hypersonic SQL Group. All rights reserved. 
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met: 
 *     -  Redistributions of source code must retain the above copyright notice, this list of conditions
 *         and the following disclaimer. 
 *     -  Redistributions in binary form must reproduce the above copyright notice, this list of
 *         conditions and the following disclaimer in the documentation and/or other materials
 *         provided with the distribution. 
 *     -  All advertising materials mentioning features or use of this software must display the
 *        following acknowledgment: "This product includes Hypersonic SQL." 
 *     -  Products derived from this software may not be called "Hypersonic SQL" nor may
 *        "Hypersonic SQL" appear in their names without prior written permission of the
 *         Hypersonic SQL Group. 
 *     -  Redistributions of any form whatsoever must retain the following acknowledgment: "This
 *          product includes Hypersonic SQL." 
 * This software is provided "as is" and any expressed or implied warranties, including, but
 * not limited to, the implied warranties of merchantability and fitness for a particular purpose are
 * disclaimed. In no event shall the Hypersonic SQL Group or its contributors be liable for any
 * direct, indirect, incidental, special, exemplary, or consequential damages (including, but
 * not limited to, procurement of substitute goods or services; loss of use, data, or profits;
 * or business interruption). However caused any on any theory of liability, whether in contract,
 * strict liability, or tort (including negligence or otherwise) arising in any way out of the use of this
 * software, even if advised of the possibility of such damage. 
 * This software consists of voluntary contributions made by many individuals on behalf of the
 * Hypersonic SQL Group.
 *
 *
 * For work added by the HSQL Development Group:
 *
 * Copyright (c) 2001-2002, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer, including earlier
 * license statements (above) and comply with all above license conditions.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution, including earlier
 * license statements (above) and comply with all above license conditions.
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


package org.hsqldb;

import org.hsqldb.HsqlNameManager.HsqlName;

/**
 * Maintains a sequence of numbers.
 *
 * @author  fredt@users
 * @since HSQLDB 1.7.2
 * @version 1.7.2
 */
public class NumberSequence {

    private HsqlName name;
    private long     currValue;
    private long     markValue;
    private long     increment;
    private int      dataType;

    /**
     * constructor with initial value and increment;
     */
    NumberSequence(HsqlName name, long value, long increment, int type) {

        this.name      = name;
        currValue      = markValue = value;
        this.increment = increment;
        dataType       = type;
    }

    /**
     * principal getter for the next sequence value
     */
    long getValue() {

        long value = currValue;

        currValue += increment;

        return value;
    }

    /**
     * getter for a given value
     */
    long getValue(long value) {

        if (value >= currValue) {
            currValue = value;
            currValue += increment;

            return value;
        } else {
            return value;
        }
    }

    /** @todo fredt - check against max value of type */
    Object getValueObject() {

        long value = currValue;

        currValue += increment;

        Object result;

        if (dataType == Types.INTEGER) {
            result = new Integer((int) value);
        } else {
            result = new Long(value);
        }

        return result;
    }

    /**
     * marks current value for a future reset()
     */
    void mark() {
        markValue = currValue;
    }

    /**
     * reset to marked value, works in conjunction with mark()
     */
    void reset() {

        // no change if called before getValue() or called twice
        currValue = markValue;
    }

    /**
     * get next value without incrementing
     */
    long peek() {
        return currValue;
    }

    /**
     * reset to new initial value
     */
    void reset(long value) {
        markValue = currValue = value;
    }

    void reset(long value, long increment) {
        markValue      = currValue = value;
        this.increment = increment;
    }

    int getType() {
        return dataType;
    }

    HsqlName getName() {
        return name;
    }

    long getIncrement() {
        return increment;
    }
}
