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


package org.hsqldb.test;

import org.hsqldb.lib.UnifiedTable;
import org.hsqldb.lib.HashSet;
import org.hsqldb.lib.IntKeyIntValueHashMap;
import org.hsqldb.lib.IntValueHashMap;
import org.hsqldb.lib.IntKeyHashMap;
import org.hsqldb.store.ValuePool;
import org.hsqldb.lib.StopWatch;
import java.util.NoSuchElementException;

public class TestLibSpeed {

    static final String sNumeric[][] = {
        {
            "ABS", "org.hsqldb.Library.abs"
        }, {
            "ACOS", "java.lang.Math.acos"
        }, {
            "ASIN", "java.lang.Math.asin"
        }, {
            "ATAN", "java.lang.Math.atan"
        }, {
            "ATAN2", "java.lang.Math.atan2"
        }, {
            "CEILING", "java.lang.Math.ceil"
        }, {
            "COS", "java.lang.Math.cos"
        }, {
            "COT", "org.hsqldb.Library.cot"
        }, {
            "DEGREES", "java.lang.Math.toDegrees"
        }, {
            "EXP", "java.lang.Math.exp"
        }, {
            "FLOOR", "java.lang.Math.floor"
        }, {
            "LOG", "java.lang.Math.log"
        }, {
            "LOG10", "org.hsqldb.Library.log10"
        }, {
            "MOD", "org.hsqldb.Library.mod"
        }, {
            "PI", "org.hsqldb.Library.pi"
        }, {
            "POWER", "java.lang.Math.pow"
        }, {
            "RADIANS", "java.lang.Math.toRadians"
        }, {
            "RAND", "java.lang.Math.random"
        }, {
            "ROUND", "org.hsqldb.Library.round"
        }, {
            "SIGN", "org.hsqldb.Library.sign"
        }, {
            "SIN", "java.lang.Math.sin"
        }, {
            "SQRT", "java.lang.Math.sqrt"
        }, {
            "TAN", "java.lang.Math.tan"
        }, {
            "TRUNCATE", "org.hsqldb.Library.truncate"
        }, {
            "BITAND", "org.hsqldb.Library.bitand"
        }, {
            "BITOR", "org.hsqldb.Library.bitor"
        }, {
            "ROUNDMAGIC", "org.hsqldb.Library.roundMagic"
        }
    };
    static UnifiedTable uniTableSet = new UnifiedTable(Object.class, 1,
        sNumeric.length);
    static HashSet hashSet = new HashSet();
    static UnifiedTable uniTableLookup = new UnifiedTable(int.class, 2,
        sNumeric.length);
    static IntKeyIntValueHashMap hashLookup = new IntKeyIntValueHashMap();
    static IntValueHashMap mapLookup = new IntValueHashMap(sNumeric.length);
    static IntKeyHashMap         hashObjectLookup = new IntKeyHashMap();

    static {
        java.util.Random randomgen = new java.util.Random();

        uniTableSet.setCount(sNumeric.length);

        int[] row = new int[2];

        for (int i = 0; i < sNumeric.length; i++) {
            hashSet.add(sNumeric[i][0]);
            uniTableSet.setCell(i, 0, sNumeric[i][0]);
            hashLookup.put(randomgen.nextInt(sNumeric.length), i);
            hashObjectLookup.put(i, new Integer(i));

            row[0] = randomgen.nextInt(sNumeric.length);
            row[1] = i;

            uniTableLookup.addRow(row);
            mapLookup.put(sNumeric[i][0], randomgen.nextInt(sNumeric.length));
        }

        uniTableSet.sort(0, true);
        uniTableLookup.sort(0, true);
    }

    public TestLibSpeed() {

        java.util.Random randomgen = new java.util.Random();
        StopWatch        sw        = new StopWatch();
        int              dummy     = 0;

        System.out.println("set lookup ");

        for (int k = 0; k < 3; k++) {
            sw.zero();

            for (int j = 0; j < 100000; j++) {
                for (int i = 0; i < sNumeric.length; i++) {
                    int r = randomgen.nextInt(sNumeric.length);

                    hashSet.contains(sNumeric[r][0]);

                    dummy += r;
                }
            }

            System.out.println("HashSet contains " + sw.elapsedTime());
            sw.zero();

            for (int j = 0; j < 100000; j++) {
                for (int i = 0; i < sNumeric.length; i++) {
                    int r = randomgen.nextInt(sNumeric.length);

                    uniTableSet.search(sNumeric[r][0]);

                    dummy += r;
                }
            }

            System.out.println("UnifiedTable contains " + sw.elapsedTime());
            sw.zero();

            for (int j = 0; j < 100000; j++) {
                for (int i = 0; i < sNumeric.length; i++) {
                    int r = randomgen.nextInt(sNumeric.length);

                    dummy += r;
                }
            }

            System.out.println("emptyOp " + sw.elapsedTime());
        }

        System.out.println("integer lookup ");

        for (int k = 0; k < 3; k++) {
            sw.zero();

            for (int j = 0; j < 100000; j++) {
                for (int i = 0; i < sNumeric.length; i++) {
                    int r = randomgen.nextInt(sNumeric.length);

                    hashLookup.get(r, -1);

                    dummy += r;
                }
            }

            System.out.println("IntToIntHashMap Lookup with array "
                               + sw.elapsedTime());
            sw.zero();

            for (int j = 0; j < 100000; j++) {
                for (int i = 0; i < sNumeric.length; i++) {
                    int r = randomgen.nextInt(sNumeric.length);

                    hashObjectLookup.get(r);

                    dummy += r;
                }
            }

            System.out.println("IntKeyHashMap Lookup " + sw.elapsedTime());
            sw.zero();

            for (int j = 0; j < 100000; j++) {
                for (int i = 0; i < sNumeric.length; i++) {
                    int r = randomgen.nextInt(sNumeric.length);

                    uniTableLookup.search(r);

                    dummy += r;
                }
            }

            System.out.println("UnifiedTable Lookup " + sw.elapsedTime());
            sw.zero();

            for (int j = 0; j < 100000; j++) {
                for (int i = 0; i < sNumeric.length; i++) {
                    int r = randomgen.nextInt(sNumeric.length);

                    mapLookup.get(sNumeric[r][0]);

                    dummy += r;
                }
            }

            System.out.println("HsqlObjectToIntMap Lookup "
                               + sw.elapsedTime());
            sw.zero();

            for (int j = 0; j < 100000; j++) {
                for (int i = 0; i < sNumeric.length; i++) {
                    int r = randomgen.nextInt(sNumeric.length);

                    dummy += r;
                }
            }

            System.out.println("emptyOp " + sw.elapsedTime());
            System.out.println("IntKeyHashMap Lookup " + sw.elapsedTime());
            sw.zero();

            for (int j = 0; j < 100000; j++) {
                for (int i = 0; i < sNumeric.length; i++) {
                    int r = randomgen.nextInt(sNumeric.length);

                    uniTableLookup.search(r);

                    dummy += r;
                }
            }

            System.out.println("UnifiedTable Lookup " + sw.elapsedTime());
            sw.zero();
            System.out.println("Object Cache Test " + sw.elapsedTime());
        }
    }

    public static void main(String argv[]) {
        TestLibSpeed ls = new TestLibSpeed();
    }
}
