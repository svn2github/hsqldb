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


package org.hsqldb;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.hsqldb.lib.HashSet;

/**
 * Implementation of SQL set functions (currently only aggregate functions).
 * This reduces temporary Object creation by SUM and AVG functions for
 * INTEGER and narrower types.
 *
 * @author fredt@users
 * @version 1.7.2
 * @since 1.7.2
 *
 */
public class SetFunction {

    private HashSet distinctValues;
    private boolean isDistinct;

    //
    private int setType;
    private int type;

    //
    private int count;

    //
    private long       currentLong;
    private double     currentDouble;
    private BigDecimal currentBigDecimal;
    private Object     currentValue;
    private LongSum    longSum;

    SetFunction(int setType, int type, boolean isDistinct) {

        this.setType = setType;
        this.type    = type;

        if (isDistinct) {
            this.isDistinct = true;
            distinctValues  = new HashSet();
        }

        if (type == Types.BIGINT
                && (setType == Expression.SUM || setType == Expression.AVG)) {
            longSum = new LongSum();
        }
    }

    void add(Object item) throws HsqlException {

        if (item == null) {
            return;
        }

        if (isDistinct &&!distinctValues.add(item)) {
            return;
        }

        count++;

        switch (setType) {

            case Expression.COUNT :
                return;

            case Expression.AVG :
            case Expression.SUM : {
                switch (type) {

                    case Types.TINYINT :
                    case Types.SMALLINT :
                    case Types.INTEGER :
                        currentLong += ((Number) item).intValue();

                        return;

                    case Types.BIGINT :
                        longSum.add(((Number) item).longValue());

                        return;

                    case Types.REAL :
                    case Types.FLOAT :
                    case Types.DOUBLE :
                        currentDouble += ((Number) item).doubleValue();

                        return;

                    case Types.NUMERIC :
                    case Types.DECIMAL :
                        if (currentBigDecimal == null) {
                            currentBigDecimal = (BigDecimal) item;
                        } else {
                            currentBigDecimal =
                                currentBigDecimal.add((BigDecimal) item);
                        }

                        return;

                    default :
                        throw Trace.error(Trace.SUM_OF_NON_NUMERIC);
                }
            }
            case Expression.MIN : {
                if (currentValue == null) {
                    currentValue = item;

                    return;
                }

                if (Column.compare(currentValue, item, type) > 0) {
                    currentValue = item;
                }

                return;
            }
            case Expression.MAX : {
                if (currentValue == null) {
                    currentValue = item;

                    return;
                }

                if (Column.compare(currentValue, item, type) < 0) {
                    currentValue = item;
                }

                return;
            }
        }
    }

    Object getValue() throws HsqlException {

        if (setType == Expression.COUNT) {
            return new Integer(count);
        }

        if (count == 0) {
            return null;
        }

        switch (setType) {

            case Expression.AVG : {
                switch (type) {

                    case Types.TINYINT :
                    case Types.SMALLINT :
                    case Types.INTEGER :
                        return new Long(currentLong / count);

                    case Types.BIGINT : {
                        long value = longSum.getValue().divide(
                            BigInteger.valueOf(count)).longValue();

                        return new Long(value);
                    }
                    case Types.REAL :
                    case Types.FLOAT :
                    case Types.DOUBLE :
                        return new Double(currentDouble / count);

                    case Types.NUMERIC :
                    case Types.DECIMAL :
                        return currentBigDecimal.divide(
                            new BigDecimal(count),
                            BigDecimal.ROUND_HALF_DOWN);

                    default :
                        throw Trace.error(Trace.SUM_OF_NON_NUMERIC);
                }
            }
            case Expression.SUM : {
                switch (type) {

                    case Types.TINYINT :
                    case Types.SMALLINT :
                    case Types.INTEGER :
                        return new Long(currentLong);

                    case Types.BIGINT :
                        return new BigDecimal(longSum.getValue());

                    case Types.REAL :
                    case Types.FLOAT :
                    case Types.DOUBLE :
                        return new Double(currentDouble);

                    case Types.NUMERIC :
                    case Types.DECIMAL :
                        return currentBigDecimal;

                    default :
                        throw Trace.error(Trace.SUM_OF_NON_NUMERIC);
                }
            }
            case Expression.MIN :
            case Expression.MAX :
                return currentValue;

            default :
                throw Trace.error(Trace.INVALID_CONVERSION);
        }
    }

    /**
     * During parsing and before an instance of SetFunction is created,
     * getType is called with type parameter set to correct type when main
     * SELECT statements contain aggregates. It is called with Types.NULL
     * when SELECT statements within INSERT or UPDATE contian aggregates.
     *
     */
    static int getType(int setType, int type) throws HsqlException {

        switch (setType) {

            case Expression.COUNT :
                return Types.INTEGER;

            case Expression.AVG : {
                switch (type) {

                    case Types.TINYINT :
                    case Types.SMALLINT :
                    case Types.INTEGER :
                    case Types.BIGINT :
                        return Types.BIGINT;

                    case Types.REAL :
                    case Types.FLOAT :
                    case Types.DOUBLE :
                        return Types.DOUBLE;

                    case Types.NUMERIC :
                    case Types.DECIMAL :
                        return Types.DECIMAL;

                    default :
                        return Types.NULL;
                }
            }
            case Expression.SUM : {
                switch (type) {

                    case Types.TINYINT :
                    case Types.SMALLINT :
                    case Types.INTEGER :
                        return Types.BIGINT;

                    case Types.BIGINT :
                        return Types.DECIMAL;

                    case Types.REAL :
                    case Types.FLOAT :
                    case Types.DOUBLE :
                        return Types.DOUBLE;

                    case Types.NUMERIC :
                    case Types.DECIMAL :
                        return Types.DECIMAL;

                    default :
                        return Types.NULL;
                }
            }
            case Expression.MIN :
            case Expression.MAX :
                return type;

            default :
                throw Trace.error(Trace.INVALID_CONVERSION);
        }
    }

    /**
     * Maintain the sum of multiple long values without creating a new
     * BigInteger object for each addition.
     */
    static class LongSum {

        static BigInteger multiplier =
            BigInteger.valueOf(0x0000000100000000L);

//        BigInteger bigint = BigInteger.ZERO;
        long hi;
        long lo;

        void add(long value) {

            if (value == 0) {}
            else if (value > 0) {
                hi += value >> 32;
                lo += value & 0x00000000ffffffffL;
            } else {
                if (value == Long.MIN_VALUE) {
                    hi -= 0x000000080000000L;
                } else {
                    long temp = ~value + 1;

                    hi -= temp >> 32;
                    lo -= temp & 0x00000000ffffffffL;
                }
            }

//            bigint = bigint.add(BigInteger.valueOf(value));
        }

        BigInteger getValue() throws HsqlException {

            BigInteger biglo  = BigInteger.valueOf(lo);
            BigInteger bighi  = BigInteger.valueOf(hi);
            BigInteger result = (bighi.multiply(multiplier)).add(biglo);

/*
            if ( result.compareTo(bigint) != 0 ){
                 throw Trace.error(Trace.GENERAL_ERROR, "longSum mismatch");
            }
*/
            return result;
        }
    }
}
