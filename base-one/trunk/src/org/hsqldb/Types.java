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

import org.hsqldb.lib.IntValueHashMap;
import org.hsqldb.lib.IntKeyHashMap;

/**
 * Defines the constants that are used to identify SQL types for HSQLDB JDBC
 * inteface type reporting. The actual type constant values are equivalent
 * to those defined in the latest java.sql.Types, where available,
 * or those defined by ansi/iso SQL 200n otherwise. A type sub-identifer
 * has been added to differentiate HSQLDB-specific type specializations.
 *
 * @author  boucherb@users.sourceforge.net
 * @version 1.7.2
 * @since HSQLDB 1.7.2
 */
class Types {

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>ARRAY</code>.
     *
     * @since JDK 1.2
     */
    static final int ARRAY = 2003;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>BIGINT</code>.
     */
    static final int BIGINT = -5;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>BINARY</code>.
     */
    static final int BINARY = -2;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>BIT</code>.
     */
    static final int BIT = -7;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>BLOB</code>.
     *
     * @since JDK 1.2
     */
    static final int BLOB = 2004;

    /**
     * The constant in the Java programming language, somtimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>BOOLEAN</code>.
     *
     * @since JDK 1.4
     */
    static final int BOOLEAN = 16;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>CHAR</code>.
     */
    static final int CHAR = 1;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>CLOB</code>
     *
     * @since JDK 1.2
     */
    static final int CLOB = 2005;

    /**
     * The constant in the Java programming language, somtimes referred to
     * as a type code, that identifies the generic SQL type <code>DATALINK</code>.
     *
     * @since JDK 1.4
     */
    static final int DATALINK = 70;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>DATE</code>.
     */
    static final int DATE = 91;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>DECIMAL</code>.
     */
    static final int DECIMAL = 3;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>DISTINCT</code>.
     *
     * @since JDK 1.2
     */
    static final int DISTINCT = 2001;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>DOUBLE</code>.
     */
    static final int DOUBLE = 8;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>FLOAT</code>.
     */
    static final int FLOAT = 6;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>INTEGER</code>.
     */
    static final int INTEGER = 4;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>JAVA_OBJECT</code>.
     *
     * @since JDK 1.2
     */
    static final int JAVA_OBJECT = 2000;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>LONGVARBINARY</code>.
     */
    static final int LONGVARBINARY = -4;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>LONGVARCHAR</code>.
     */
    static final int LONGVARCHAR = -1;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>NULL</code>.
     */
    static final int NULL = 0;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>NUMERIC</code>.
     */
    static final int NUMERIC = 2;

    /**
     * The constant in the Java programming language that indicates
     * that the SQL type is database-specific and
     * gets mapped to a Java object that can be accessed via
     * the methods <code>getObject</code> and <code>setObject</code>.
     */
    static final int OTHER = 1111;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>REAL</code>.
     */
    static final int REAL = 7;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>REF</code>.
     *
     * @since JDK 1.2
     */
    static final int REF = 2006;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>SMALLINT</code>.
     */
    static final int SMALLINT = 5;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>STRUCT</code>.
     *
     * @since JDK 1.2
     */
    static final int STRUCT = 2002;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>TIME</code>.
     */
    static final int TIME = 92;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>TIMESTAMP</code>.
     */
    static final int TIMESTAMP = 93;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>TINYINT</code>.
     */
    static final int TINYINT = -6;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>VARBINARY</code>.
     */
    static final int VARBINARY = -3;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>VARCHAR</code>.
     */
    static final int VARCHAR = 12;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the recent SQL 200n SQL type
     * <code>XML</code>.
     *
     * @since SQL 200n
     */
    static final int XML = 137;

    /**
     * The default HSQLODB type sub-identifier. This indicates that an
     * HSQLDB type with this sub-type, if supported, is the very closest
     * thing HSQLDB offerers to the JDBC/SQL200n type
     */
    static final int TYPE_SUB_DEFAULT = 1;

    /**
     * The IDENTITY type sub-identifier. This indicates that an HSQLDB type
     * with this sub-type, if supported, is the closest thing HSQLDB offerers
     * to the JDBC/SQL200n type, except that it also provides autoincrement
     * behaviour
     */
    static final int TYPE_SUB_IDENTITY = TYPE_SUB_DEFAULT << 1;

    /**
     * The IGNORECASE type sub-identifier. This indicates that an HSQLDB type
     * with this sub-type, if supported,  is the closest thing HSQLDB offerers
     * to the JDBC/SQL200n type, except that case is ignored in comparisons
     */
    static final int TYPE_SUB_IGNORECASE = TYPE_SUB_DEFAULT << 2;

    /**
     * Every (type,type-sub) combination known in the HSQLDB context.
     * Not every combination need be supported as a table or procedure
     * column type -- such determinations are handled in DITypeInfo.
     */
    static final int[][] ALL_TYPES = {
        {
            ARRAY, TYPE_SUB_DEFAULT
        }, {
            BIGINT, TYPE_SUB_DEFAULT
        }, {
            BIGINT, TYPE_SUB_IDENTITY
        }, {
            BINARY, TYPE_SUB_DEFAULT
        }, {
            BIT, TYPE_SUB_DEFAULT
        }, {
            BLOB, TYPE_SUB_DEFAULT
        }, {
            BOOLEAN, TYPE_SUB_DEFAULT
        }, {
            CHAR, TYPE_SUB_DEFAULT
        }, {
            CLOB, TYPE_SUB_DEFAULT
        }, {
            DATALINK, TYPE_SUB_DEFAULT
        }, {
            DATE, TYPE_SUB_DEFAULT
        }, {
            DECIMAL, TYPE_SUB_DEFAULT
        }, {
            DISTINCT, TYPE_SUB_DEFAULT
        }, {
            DOUBLE, TYPE_SUB_DEFAULT
        }, {
            FLOAT, TYPE_SUB_DEFAULT
        }, {
            INTEGER, TYPE_SUB_DEFAULT
        }, {
            INTEGER, TYPE_SUB_IDENTITY
        }, {
            JAVA_OBJECT, TYPE_SUB_DEFAULT
        }, {
            LONGVARBINARY, TYPE_SUB_DEFAULT
        }, {
            LONGVARCHAR, TYPE_SUB_DEFAULT
        }, {
            NULL, TYPE_SUB_DEFAULT
        }, {
            NUMERIC, TYPE_SUB_DEFAULT
        }, {
            OTHER, TYPE_SUB_DEFAULT
        }, {
            REAL, TYPE_SUB_DEFAULT
        }, {
            REF, TYPE_SUB_DEFAULT
        }, {
            SMALLINT, TYPE_SUB_DEFAULT
        }, {
            STRUCT, TYPE_SUB_DEFAULT
        }, {
            TIME, TYPE_SUB_DEFAULT
        }, {
            TIMESTAMP, TYPE_SUB_DEFAULT
        }, {
            TINYINT, TYPE_SUB_DEFAULT
        }, {
            VARBINARY, TYPE_SUB_DEFAULT
        }, {
            VARCHAR, TYPE_SUB_DEFAULT
        }, {
            VARCHAR, TYPE_SUB_IGNORECASE
        }, {
            XML, TYPE_SUB_DEFAULT
        }
    };
/*
 SQL specifies predefined data types named by the following <key word>s:
 CHARACTER, CHARACTER VARYING, CHARACTER LARGE OBJECT, BINARY LARGE OBJECT,
 NUMERIC, DECIMAL, SMALLINT, INTEGER, BIGINT, FLOAT, REAL, DOUBLE PRECISION,
 BOOLEAN, DATE, TIME, TIMESTAMP, and INTERVAL.
 SQL 200n adds DATALINK in Part 9: Management of External Data (SQL/MED)
 and adds XML in Part 14: XML-Related Specifications (SQL/XML)
*/

    // CLI type list from Table 37
    static final int SQL_CHARACTER                 = 1;
    static final int SQL_CHAR                      = 1;
    static final int SQL_NUMERIC                   = 2;
    static final int SQL_DECIMAL                   = 3;
    static final int SQL_DEC                       = 3;
    static final int SQL_INTEGER                   = 4;
    static final int SQL_INT                       = 4;
    static final int SQL_SMALLINT                  = 5;
    static final int SQL_FLOAT                     = 6;
    static final int SQL_REAL                      = 7;
    static final int SQL_DOUBLE                    = 8;
    static final int SQL_CHARACTER_VARYING         = 12;
    static final int SQL_CHAR_VARYING              = 12;
    static final int SQL_VARCHAR                   = 12;
    static final int SQL_BOOLEAN                   = 16;
    static final int SQL_USER_DEFINED_TYPE         = 17;
    static final int SQL_ROW                       = 19;
    static final int SQL_REF                       = 20;
    static final int SQL_BIGINT                    = 25;
    static final int SQL_BINARY_LARGE_OBJECT       = 30;
    static final int SQL_BLOB                      = 30;
    static final int SQL_CHARACTER_LARGE_OBJECT    = 40;
    static final int SQL_CLOB                      = 40;
    static final int SQL_ARRAY                     = 50;     // not predefined
    static final int SQL_MULTISET                  = 55;     //
    static final int SQL_DATE                      = 91;
    static final int SQL_TIME                      = 92;
    static final int SQL_TIMESTAMP                 = 93;     //
    static final int SQL_TIME_WITH_TIME_ZONE       = 94;
    static final int SQL_TIMESTAMP_WITH_TIME_ZONE  = 95;     //
    static final int SQL_INTERVAL_YEAR             = 101;    //
    static final int SQL_INTERVAL_MONTH            = 102;
    static final int SQL_INTERVAL_DAY              = 103;
    static final int SQL_INTERVAL_HOUR             = 104;
    static final int SQL_INTERVAL_MINUTE           = 105;
    static final int SQL_INTERVAL_SECOND           = 106;
    static final int SQL_INTERVAL_YEAR_TO_MONTH    = 107;
    static final int SQL_INTERVAL_DAY_TO_HOUR      = 108;
    static final int SQL_INTERVAL_DAY_TO_MINUTE    = 109;
    static final int SQL_INTERVAL_DAY_TO_SECOND    = 110;
    static final int SQL_INTERVAL_HOUR_TO_MINUTE   = 111;
    static final int SQL_INTERVAL_HOUR_TO_SECOND   = 112;
    static final int SQL_INTERVAL_MINUTE_TO_SECOND = 113;

    // These values are not in table 37 of the SQL CLI 200n FCD, but some
    // are found in tables 6-9 and some are found in Annex A1:
    // c Header File SQLCLI.H and/or addendums in other documents, 
    // such as:
    // SQL 200n Part 9: Management of External Data (SQL/MED) : DATALINK
    // SQL 200n Part 14: XML-Related Specifications (SQL/XML) : XML
    static final int SQL_BIT_VARYING      = 15;              // is in SQL99 but removed from 2002
    static final int SQL_DATALINK         = 70;
    static final int SQL_UDT              = 17;
    static final int SQL_UDT_LOCATOR      = 18;
    static final int SQL_BLOB_LOCATOR     = 31;
    static final int SQL_CLOB_LOCATOR     = 41;
    static final int SQL_ARRAY_LOCATOR    = 51;
    static final int SQL_MULTISET_LOCATOR = 56;
    static final int SQL_ALL_TYPES        = 0;
    static final int SQL_DATETIME         = 9;               // collective name
    static final int SQL_INTERVAL         = 10;              // collective name
    static final int SQL_XML              = 137;

    // SQL_UDT subcodes
    static final int SQL_DISTINCT    = 1;
    static final int SQL_SCTRUCTURED = 2;

    // non-standard type not in JDBC or SQL CLI
    static final int VARCHAR_IGNORECASE = 100;

// lookup for types
// boucherb@users - access changed for metadata 1.7.2
    static IntValueHashMap typeAliases;
    static IntKeyHashMap   typeNames;
    static IntValueHashMap javaTypeNames;

//  boucherb@users - We can't handle method invocations in 
//                   Function.java whose number class is
//                   narrower than the corresponding internal
//                   wrapper    
    private static org.hsqldb.lib.HashSet illegalParameterClasses;

    static {
        typeAliases = new IntValueHashMap(67, 1);

        typeAliases.put("INTEGER", Types.INTEGER);
        typeAliases.put("INT", Types.INTEGER);
        typeAliases.put("int", Types.INTEGER);
        typeAliases.put("java.lang.Integer", Types.INTEGER);
        typeAliases.put("IDENTITY", Types.INTEGER);
        typeAliases.put("DOUBLE", Types.DOUBLE);
        typeAliases.put("double", Types.DOUBLE);
        typeAliases.put("java.lang.Double", Types.DOUBLE);
        typeAliases.put("FLOAT", Types.FLOAT);
        typeAliases.put("REAL", Types.REAL);
        typeAliases.put("VARCHAR", Types.VARCHAR);
        typeAliases.put("java.lang.String", Types.VARCHAR);
        typeAliases.put("CHAR", Types.CHAR);
        typeAliases.put("CHARACTER", Types.CHAR);
        typeAliases.put("LONGVARCHAR", Types.LONGVARCHAR);
        typeAliases.put("VARCHAR_IGNORECASE", VARCHAR_IGNORECASE);
        typeAliases.put("DATE", Types.DATE);
        typeAliases.put("java.sql.Date", Types.DATE);
        typeAliases.put("TIME", Types.TIME);
        typeAliases.put("java.sql.Time", Types.TIME);
        typeAliases.put("TIMESTAMP", Types.TIMESTAMP);
        typeAliases.put("java.sql.Timestamp", Types.TIMESTAMP);
        typeAliases.put("DATETIME", Types.TIMESTAMP);
        typeAliases.put("DECIMAL", Types.DECIMAL);
        typeAliases.put("java.math.BigDecimal", Types.DECIMAL);
        typeAliases.put("NUMERIC", Types.NUMERIC);
        typeAliases.put("BIT", Types.BIT);
        typeAliases.put("boolean", Types.BIT);
        typeAliases.put("java.lang.Boolean", Types.BIT);
        typeAliases.put("TINYINT", Types.TINYINT);
        typeAliases.put("byte", Types.TINYINT);
        typeAliases.put("java.lang.Byte", Types.TINYINT);
        typeAliases.put("SMALLINT", Types.SMALLINT);
        typeAliases.put("short", Types.SMALLINT);
        typeAliases.put("java.lang.Short", Types.SMALLINT);
        typeAliases.put("BIGINT", Types.BIGINT);
        typeAliases.put("long", Types.BIGINT);
        typeAliases.put("java.lang.Long", Types.BIGINT);
        typeAliases.put("BINARY", Types.BINARY);
        typeAliases.put("[B", Types.BINARY);
        typeAliases.put("VARBINARY", Types.VARBINARY);
        typeAliases.put("LONGVARBINARY", Types.LONGVARBINARY);
        typeAliases.put("OTHER", Types.OTHER);
        typeAliases.put("OBJECT", Types.OTHER);
        typeAliases.put("java.lang.Object", Types.OTHER);
        typeAliases.put("NULL", Types.NULL);
        typeAliases.put("void", Types.NULL);
        typeAliases.put("java.lang.Void", Types.NULL);

        //
        typeNames = new IntKeyHashMap(37);

        typeNames.put(Types.NULL, "NULL");
        typeNames.put(Types.INTEGER, "INTEGER");
        typeNames.put(Types.DOUBLE, "DOUBLE");
        typeNames.put(VARCHAR_IGNORECASE, "VARCHAR_IGNORECASE");
        typeNames.put(Types.VARCHAR, "VARCHAR");
        typeNames.put(Types.CHAR, "CHAR");
        typeNames.put(Types.LONGVARCHAR, "LONGVARCHAR");
        typeNames.put(Types.DATE, "DATE");
        typeNames.put(Types.TIME, "TIME");
        typeNames.put(Types.DECIMAL, "DECIMAL");
        typeNames.put(Types.BIT, "BIT");
        typeNames.put(Types.TINYINT, "TINYINT");
        typeNames.put(Types.SMALLINT, "SMALLINT");
        typeNames.put(Types.BIGINT, "BIGINT");
        typeNames.put(Types.REAL, "REAL");
        typeNames.put(Types.FLOAT, "FLOAT");
        typeNames.put(Types.NUMERIC, "NUMERIC");
        typeNames.put(Types.TIMESTAMP, "TIMESTAMP");
        typeNames.put(Types.BINARY, "BINARY");
        typeNames.put(Types.VARBINARY, "VARBINARY");
        typeNames.put(Types.LONGVARBINARY, "LONGVARBINARY");
        typeNames.put(Types.OTHER, "OBJECT");

        //
        illegalParameterClasses = new org.hsqldb.lib.HashSet(13);

        illegalParameterClasses.add(Byte.TYPE);
        illegalParameterClasses.add(Short.TYPE);
        illegalParameterClasses.add(Float.TYPE);
        illegalParameterClasses.add(Byte.class);
        illegalParameterClasses.add(Short.class);
        illegalParameterClasses.add(Float.class);
    }

    /**
     *
     * @param  SQL type string
     * @return java.sql.Types int value
     * @throws  HsqlException
     */
    static int getTypeNr(String type) throws HsqlException {

        int i = typeAliases.get(type, Integer.MIN_VALUE);

        Trace.check(i != Integer.MIN_VALUE, Trace.WRONG_DATA_TYPE, type);

        return i;
    }

    /**
     * Returns SQL type string for a java.sql.Types int value
     */
    static String getTypeString(int type) {
        return (String) typeNames.get(type);
    }

    /**
     * Retieves the type number corresponding to the class
     * of an IN, IN OUT or OUT parameter.  <p>
     *
     * This method extends getTypeNr to return OTHER for
     * primitive arrays, classes that directly implement
     * java.io.Serializable and non-primitive arrays whose
     * base component implements java.io.Serializable,
     * allowing, for instance, arguments and return types of
     * primitive arrays, Serializable objects and arrays,
     * of Serializable objects.  Direct primitive types
     * other than those mapping directly to the internal
     * wrapper form are not yet handled.  That is, HSQLDB
     * cannot yet properly deal with CALLs involving methods
     * with primitive byte, short, float or their
     * corresponding wrappers, due to the way internal
     * conversion works and lack of detection and narrowing
     * code in Function to allow this.  In other words,
     * passing in or retrieving any of the mentioned types
     * always causes conversion to a wider internal wrapper
     * which is genrally incompatible under reflective
     * invocation, resulting in an IllegalArgumentException.
     *
     * @param  c a Class instance
     * @return java.sql.Types int value
     * @throws  HsqlException
     */
    static int getParameterTypeNr(Class c) throws HsqlException {

        String name;
        String msg;
        int    type;

        Trace.doAssert(c != null, "c is null");

        if (Void.TYPE.equals(c)) {
            return Types.NULL;
        }

        name = c.getName();
        msg  = "Unsupported parameter/return value class: ";

        if (illegalParameterClasses.contains(c)) {
            throw Trace.error(Trace.WRONG_DATA_TYPE, msg + name);
        }

        type = typeAliases.get(name, Integer.MIN_VALUE);

        if (type == Integer.MIN_VALUE) {

            // byte[] is already covered as BINARY in typeAliases
            if (c.isArray()) {
                while (c.isArray()) {
                    c = c.getComponentType();
                }

                if (c.isPrimitive()
                        || java.io.Serializable.class.isAssignableFrom(c)) {
                    type = OTHER;
                }
            } else if (java.io.Serializable.class.isAssignableFrom(c)) {
                type = OTHER;
            }
        }

        Trace.check(type != Integer.MIN_VALUE, Trace.WRONG_DATA_TYPE,
                    msg + name);

        return type;
    }

    /**
     * Retrieves the SQL data type corrsponding to the
     * Class of the "widest" (least restrictive) internal
     * represention in which the specified object can fit
     * without a non-trivial conversion via
     * Column.convertObject(Object,int). <p>
     *
     * By trivial, it is meant converting an Object, o,
     * to a JavaObject holder for o. <p>
     *
     * An optimization is added that narrows the output value
     * if the input object is Integer and its value would fit
     * in the range of either TINYINT or SMALLINT, both of which
     * are also represented interally as Integer objects.
     *
     */
    static int getWidestTypeNrNoConvert(Object o) {

        int type;

        if (o == null) {
            return NULL;
        }

        type = getWidestTypeNrNoConvert(o.getClass());

        // To optimize result of promotesWithoutConversion(t1,t2)
        if (type == INTEGER) {
            int val = ((Number) o).intValue();

            if (val >= Byte.MIN_VALUE && val <= Byte.MAX_VALUE) {
                type = TINYINT;
            } else if (val >= Short.MIN_VALUE && val <= Short.MAX_VALUE) {
                type = SMALLINT;
            }
        }

        return type;
    }

    /**
     * Retrieves the SQL data type corrsponding to the
     * Class of the "widest" internal represention in
     * which instances of the specified Class can fit
     * without a non-trivial conversion via
     * Column.convertObject(Object,int). <p>
     *
     * By trivial, it is meant converting an Object, o,
     * to a JavaObject holder for o.
     */
    static int getWidestTypeNrNoConvert(Class c) {

        if (c == null || Void.TYPE.equals(c)) {
            return NULL;
        }

        if (Boolean.class.equals(c)) {
            return BIT;
        } else if (String.class.equals(c)) {
            return LONGVARCHAR;
        } else if (Binary.class.equals(c)) {
            return LONGVARBINARY;
        } else if (Integer.class.equals(c)) {
            return INTEGER;
        } else if (Long.class.equals(c)) {
            return BIGINT;
        } else if (Double.class.equals(c)) {
            return DOUBLE;
        } else if (java.sql.Date.class.isAssignableFrom(c)) {
            return DATE;
        } else if (java.sql.Time.class.isAssignableFrom(c)) {
            return TIME;
        } else if (java.sql.Timestamp.class.isAssignableFrom(c)) {
            return TIMESTAMP;
        } else if (java.math.BigDecimal.class.equals(c)) {
            return DECIMAL;
        }

        // Others, including JavaObject, must be converted.
        // We could check for Serializable.class.isAssignableFrom(c)
        // here and throw if not, but that will be picked up in the
        // conversion, so it's redundant here.
        // In other words, please note the NoConvert suffix
        // of the method name.  It's there for a reason.
        return OTHER;
    }

    static boolean areSimilar(int t1, int t2) {

        if (t1 == t2) {
            return true;
        }

        if (isNumberType(t1)) {
            return isNumberType(t2);
        }

        if (isCharacterType(t1)) {
            return isCharacterType(t2);
        }

        if (isBinaryType(t1)) {
            return isBinaryType(t2);
        }

        return false;
    }

    static boolean haveSameInternalRepresentation(int t1, int t2) {

        if (t1 == t2) {
            return true;
        }

        if (isCharacterType(t1)) {
            return isCharacterType(t2);
        }

        if (isBinaryType(t1)) {
            return isBinaryType(t2);
        }

        switch (t1) {

            case TINYINT :
            case SMALLINT :
            case INTEGER : {
                switch (t2) {

                    case TINYINT :
                    case SMALLINT :
                    case INTEGER : {
                        return true;
                    }
                    default : {
                        return false;
                    }
                }
            }
            case FLOAT :
            case REAL :
            case DOUBLE : {
                switch (t2) {

                    case FLOAT :
                    case REAL :
                    case DOUBLE : {
                        return true;
                    }
                    default : {
                        return false;
                    }
                }
            }
            case DECIMAL :
            case NUMERIC : {
                switch (t2) {

                    case DECIMAL :
                    case NUMERIC : {
                        return true;
                    }
                    default : {
                        return false;
                    }
                }
            }
            default : {
                return false;
            }
        }
    }

    static boolean promotesWithoutConversion(int t1, int t2) {

        if (t1 == t2) {

            // Probably should be: return t1 != OTHER,
            // but internally t1 == t2 is ok, so the
            // JDBC PreparedStatement implementation
            // extends the test to cover things from
            // the external viewpoint
            return true;
        }

        if (isCharacterType(t1)) {
            return isCharacterType(t2);
        }

        if (isBinaryType(t1)) {
            return isBinaryType(t2);
        }

// if (isNumberType(t1)) is a tautology at this point
        switch (t1) {

            case TINYINT : {
                switch (t2) {

//                  case TINYINT : // covered by t1 == t2
                    case SMALLINT :
                    case INTEGER :
                        return true;

                    default :
                        return false;
                }
            }
            case SMALLINT : {
                switch (t2) {

//                  case SMALLINT : // covered by t1 == t2
                    case INTEGER :
                        return true;

                    default :
                        return false;
                }
            }

// covered by t2 == t2 condition above
//          case INTEGER :
//          case BIGINT : {
//              return t2 == t1;
//          }
// semi-redundant, but simpler than writing out individual cases to
// avoid it
            case FLOAT :
            case REAL :
            case DOUBLE : {
                switch (t2) {

                    case FLOAT :
                    case REAL :
                    case DOUBLE :
                        return true;

                    default :
                        return false;
                }
            }

// semi-redundant, but simpler than writing out individual cases to
// avoid it
            case DECIMAL :
            case NUMERIC : {
                switch (t2) {

                    case DECIMAL :
                    case NUMERIC :
                        return true;

                    default :
                        return false;
                }
            }
            default : {
                return false;
            }
        }
    }

    static boolean isNumberType(int type) {

        switch (type) {

            case BIGINT :
            case DECIMAL :
            case DOUBLE :
            case FLOAT :
            case INTEGER :
            case NUMERIC :
            case REAL :
            case SMALLINT :
            case TINYINT :
                return true;

            default :
                return false;
        }
    }

    static boolean isExactNumberType(int type) {

        switch (type) {

            case BIGINT :
            case DECIMAL :
            case INTEGER :
            case NUMERIC :
            case SMALLINT :
            case TINYINT :
                return true;

            default :
                return false;
        }
    }

    static boolean isStrictlyIntegralNumberType(int type) {

        switch (type) {

            case BIGINT :
            case INTEGER :
            case SMALLINT :
            case TINYINT :
                return true;

            default :
                return false;
        }
    }

    static boolean isApproximateNumberType(int type) {

        switch (type) {

            case DOUBLE :
            case FLOAT :
            case REAL :
                return true;

            default :
                return false;
        }
    }

    static boolean isCharacterType(int type) {

        switch (type) {

            case CHAR :

// Not supported yet & would break:
// promotesWithoutConversion & haveSameInternalRepresentation                 
//          case CLOB : 
            case LONGVARCHAR :
            case VARCHAR :
            case VARCHAR_IGNORECASE :
                return true;

            default :
                return false;
        }
    }

    static boolean isBinaryType(int type) {

        switch (type) {

            case BINARY :

// Not supported yet & would break:
// promotesWithoutConversion & haveSameInternalRepresentation                
//          case BLOB :
            case LONGVARBINARY :
            case VARBINARY :
                return true;

            default :
                return false;
        }
    }

    static boolean isDatetimeType(int type) {

        switch (type) {

            case DATE :
            case TIME :
            case TIMESTAMP :
                return true;

            default :
                return false;
        }
    }

    static boolean acceptsPrecisionCreateParam(int type) {

        switch (type) {

            case BINARY :
            case BLOB :
            case CHAR :
            case CLOB :

// CHECKME:
// I suppose we do/could, but, typically, other systems do not?             
//            case LONGVARBINARY :
//            case LONGVARCHAR :
            case VARBINARY :
            case VARCHAR :
            case DECIMAL :
            case NUMERIC :
            case FLOAT :
                return true;

            default :
                return false;
        }
    }

    static int numericPrecisionCreateParamRadix(int type) {

        switch (type) {

            case Types.DECIMAL :
            case Types.NUMERIC :
                return 10;

            case FLOAT :
                return 2;

            default :

                // to mean NOT APPLICABLE (i.e. NULL)
                return 0;
        }
    }

    static boolean acceptsScaleCreateParam(int type) {

        switch (type) {

            case Types.DECIMAL :
            case Types.NUMERIC :
                return true;

            default :
                return false;
        }
    }
}
