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
    static ValuePoolHashMap intPool;
    static ValuePoolHashMap longPool;
    static ValuePoolHashMap doublePool;
    static ValuePoolHashMap bigdecimalPool;
    static ValuePoolHashMap stringPool;
    static ValuePoolHashMap datePool;

    //
    static final int[] defaultPoolLookupSize  = new int[] {
        10000, 10000, 10000, 10000, 10000, 10000
    };
    static final int   defaultSizeFactor      = 2;
    static final int   defaultMaxStringLength = 16;

    //
    static ValuePoolHashMap[] poolList;

    //
    static int maxStringLength;

    //
    static {
        initPool();
    }

    private static void initPool() {

        int sizeArray[] = defaultPoolLookupSize;
        int sizeFactor  = defaultSizeFactor;

        synchronized (ValuePool.class) {
            maxStringLength = defaultMaxStringLength;
            poolList        = new ValuePoolHashMap[6];

            for (int i = 0; i < poolList.length; i++) {
                int size = sizeArray[i];

                poolList[i] = new ValuePoolHashMap(size, size * sizeFactor,
                                                   BaseHashMap.PURGE_HALF);
            }

            intPool        = poolList[0];
            longPool       = poolList[1];
            doublePool     = poolList[2];
            bigdecimalPool = poolList[3];
            stringPool     = poolList[4];
            datePool       = poolList[5];
        }
    }

    public static void resetPool(int[] sizeArray, int sizeFactor) {

        for (int i = 0; i < poolList.length; i++) {
            poolList[i].resetCapacity(sizeArray[i] * sizeFactor,
                                      BaseHashMap.PURGE_HALF);
        }
    }

    public static void resetPool() {
        resetPool(defaultPoolLookupSize, defaultSizeFactor);
    }

    public static void clearPool() {

        for (int i = 0; i < poolList.length; i++) {
            poolList[i].clear();
        }
    }

    public static synchronized Integer getInt(int val) {
        return intPool.getOrAddInteger(val);
    }

    public static synchronized Long getLong(long val) {
        return longPool.getOrAddLong(val);
    }

    public static synchronized Double getDouble(long val) {
        return doublePool.getOrAddDouble(val);
    }

    public static synchronized String getString(String val) {

        if (val == null || val.length() > maxStringLength) {
            return val;
        }

        return stringPool.getOrAddString(val);
    }

    public static synchronized java.sql.Date getDate(long val) {
        return datePool.getOrAddDate(val);
    }

    public static synchronized java.math.BigDecimal getBigDecimal(
            java.math.BigDecimal val) {

        if (val == null) {
            return val;
        }

        return (java.math.BigDecimal) bigdecimalPool.getOrAddObject(val);
    }

    public static Boolean getBoolean(boolean b) {
        return b ? Boolean.TRUE
                 : Boolean.FALSE;
    }
    
//    public static Boolean getBoolean(String s) {
//        return "true".equalsIgnoreCase(s) ? Boolean.TRUE
//                                          : Boolean.FALSE;
//    }
    
//    public static Boolean getBoolean(Number n) {
//        return n == null ? null : getBoolean(n.doubleValue() == 1.0);
//    }
    
//    public static Boolean getBoolean(Object o) {
//       return o == null ? null 
//                        : o instanceof Boolean 
//                              ? (Boolean) o 
//                              : o instanceof Number
//                                    ? getBoolean((Number)o)
//                                    : getBoolean(String.valueOf(o);
//     }

    public static Object getObject(Object o) {
        if (o == null) {
            return null;
        } else if (o instanceof Boolean) {
            return ((java.lang.Boolean)o).booleanValue() ? Boolean.TRUE
                                                         : Boolean.FALSE;
        } else if (o instanceof java.math.BigDecimal) {
            return getBigDecimal((java.math.BigDecimal)o);
        } else if (o instanceof java.sql.Date) {
            return getDate(((java.sql.Date)o).getTime());
        } else if (o instanceof Double) {
            return getDouble(Double.doubleToLongBits(((Double)o).doubleValue()));
        } else if (o instanceof Integer) {
            return getInt(((Integer)o).intValue());
        } else if (o instanceof Long) {
            return getLong(((Long)o).longValue());            
        } else if (o instanceof String) {
            return getString((String)o);
        }
        return o;        
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
