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

/**
 * This should be reimplemented properly.
 *
 * Only getXXX methods are used for retrival of values. If a value is not in
 * the pool, it should be added to the pool and returned. When the pool gets
 * full, the older / least-accessed values should be purged. (fredt@users)
 */
public class ValuePool {

    static IntegerPool intPool = new IntegerPool(16000,1,true);
    static int testCounter;
    public static Short getShort(short val) {
        return new Short(val);
    }

    public static Integer getInt(int val) {
/*
        testCounter++;
        if (testCounter%10000 == 0){
            System.out.println("Integer Pool Size at access " + testCounter + "  " +  intPool.size());
        }
*/
        return intPool.get(val);
    }

    public static Long getLong(long val) {
        return new Long(val);
    }

    public static Long getLong(Number n) {
        return n == null
            ? null
            : (n instanceof Long)
                ? (Long) n
                : getLong(n.longValue());
    }

    public static Float getFloat(float val) {
        return new Float(val);
    }

    public static Double getDouble(double val) {
        return new Double(val);
    }

    public static Boolean getBoolean(boolean b) {
        return b ? Boolean.TRUE : Boolean.FALSE;
    }

}
