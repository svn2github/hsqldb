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
 * Under development.
 *
 * Only getXXX methods are used for retrival of values. If a value is not in
 * the pool, it is added to the pool and returned. When the pool gets
 * full, the contents are purged.
 * (fredt@users)
 */
public class ValuePool {

    static int intInitCapacity        = 1000;
    static int longInitCapacity       = 1000;
    static int doubleInitCapacity     = 1000;
    static int bigdecimalInitCapacity = 1000;
    static int stringInitCapacity     = 1000;
    static int dateInitCapacity       = 1000;

    //
    static int intAccessCount;
    static int longAccessCount;
    static int doubleAccessCount;
    static int bigdecimalAccessCount;
    static int stringAccessCount;
    static int dateAccessCount;

    //
    static BaseHashMap intPool = new BaseHashMap(intInitCapacity,
        intInitCapacity * 2, BaseHashMap.purgeAll);
    static BaseHashMap longPool = new BaseHashMap(longInitCapacity,
        longInitCapacity * 2, BaseHashMap.purgeAll);
    static BaseHashMap doublePool = new BaseHashMap(doubleInitCapacity,
        longInitCapacity * 2, BaseHashMap.purgeAll);
    static BaseHashMap bigdecimalPool =
        new BaseHashMap(bigdecimalInitCapacity, bigdecimalInitCapacity * 2,
                        BaseHashMap.purgeAll);
    static BaseHashMap stringPool = new BaseHashMap(stringInitCapacity,
        stringInitCapacity * 2, BaseHashMap.purgeAll);
    static BaseHashMap datePool = new BaseHashMap(dateInitCapacity,
        dateInitCapacity * 2, BaseHashMap.purgeAll);

    public static Integer getInt(int val) {

        intAccessCount++;

        return intPool.getOrAddInteger(val);
    }

    public static Long getLong(long val) {

        longAccessCount++;

        return longPool.getOrAddLong(val);
    }

    public static Double getDouble(long val) {

        doubleAccessCount++;

        return doublePool.getOrAddDouble(val);
    }

    public static String getString(String val) {

        if (val.length() > 16) {
            return val;
        }

        stringAccessCount++;

        return stringPool.getOrAddString(val);
    }

    public static java.sql.Date getDate(long val) {

        dateAccessCount++;

        return datePool.getOrAddDate(val);
    }

    public static java.math.BigDecimal getBigDecimal(
            java.math.BigDecimal val) {

        bigdecimalAccessCount++;

        return bigdecimalPool.getOrAddBigDecimal(val);
    }

    public static Boolean getBoolean(boolean b) {
        return b ? Boolean.TRUE
                 : Boolean.FALSE;
    }

    public ValuePool(int dummy) {

//   temp     workaround
    }
}
