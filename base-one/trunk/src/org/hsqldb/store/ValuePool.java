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

    //
    static int maxStringLength = 16;

    //
    static int intAccessCount;
    static int longAccessCount;
    static int doubleAccessCount;
    static int bigdecimalAccessCount;
    static int stringAccessCount;
    static int dateAccessCount;

    //
    static BaseHashMap intPool;
    static BaseHashMap longPool;
    static BaseHashMap doublePool;
    static BaseHashMap bigdecimalPool;
    static BaseHashMap stringPool;
    static BaseHashMap datePool;

    //
    static final int[]   defaultPoolLookupSize = new int[] {
        1000, 1000, 1000, 1000, 1000, 1000
    };
    static final int     defaultSizeFactor     = 2;
    static BaseHashMap[] poolList              = new BaseHashMap[6];
    static int[]         poolLookupSize        = new int[6];

    //
    static {
        initPool(defaultPoolLookupSize, defaultSizeFactor);
    }

    public static void initPool(int sizeArray[], int sizeFactor) {

        synchronized (ValuePool.class) {
            for (int i = 0; i < poolList.length; i++) {
                int size = sizeArray[i];

                poolLookupSize[i] = size;
                poolList[i] = new BaseHashMap(size, size * sizeFactor,
                                              BaseHashMap.purgeAll);
            }

            intPool               = poolList[0];
            longPool              = poolList[1];
            doublePool            = poolList[2];
            bigdecimalPool        = poolList[3];
            stringPool            = poolList[4];
            datePool              = poolList[5];
            intAccessCount        = 0;
            longAccessCount       = 0;
            doubleAccessCount     = 0;
            bigdecimalAccessCount = 0;
            stringAccessCount     = 0;
            dateAccessCount       = 0;
        }
    }

    public static void resetPool(int[] sizeArray, int sizeFactor) {

        for (int i = 0; i < poolList.length; i++) {
            poolList[i].clear();
        }

        initPool(sizeArray, sizeFactor);
    }

    public static void resetPool() {
        resetPool(defaultPoolLookupSize, defaultSizeFactor);
    }

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

        if (val.length() > maxStringLength) {
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

    public static class poolSettings {

        String[] propertyStrings = new String[] {
            "runtime.pool.int_size",        //
            "runtime.pool.long_size",       //
            "runtime.pool.double_size",     //
            "runtime.pool.decimal_size",    //
            "runtime.pool.string_size",     //
            "runtime.pool.date_size",       //
            "runtime.pool.factor",          //
            "runtime.pool.string_length"    //
        };

        //
        static final int[] defaultPoolLookupSize = new int[] {
            1000, 1000, 1000, 1000, 1000, 1000
        };
    }
}
