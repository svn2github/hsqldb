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

    // non-standard type not in JDBC
    static final int VARCHAR_IGNORECASE = 100;

    // java types used in engine or supported for direct conversion in
    // JDBC interface methods
    static final int JAVA_BOOLEAN = 1;
    static final int JAVA_STRING  = 2;
    static final int JAVA_BYTE    = 3;
    static final int JAVA_SHORT   = 4;
    static final int JAVA_INTEGER = 5;
    static final int JAVA_LONG    = 6;
    static final int JAVA_BIGDEC  = 7;
    static final int JAVA_DOUBLE  = 8;
    static final int JAVA_SQLDATE = 9;
    static final int JAVA_SQLTIME = 10;
    static final int JAVA_SQLTS   = 11;
    static final int JAVA_JAVAOBJECT  = 12;
    static final int JAVA_BINARY  = 13;

// lookup for types
// boucherb@users - access changed for metadata 1.7.2
    static IntValueHashMap typeAliases;
    static IntKeyHashMap   typeNames;
    static IntValueHashMap javaTypeNames;

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
/*
        javaTypeNames = new IntValueHashMap(23, 1);

        javaTypeNames.put(Boolean.class.getName(), JAVA_BOOLEAN);
        javaTypeNames.put(String.class.getName(), JAVA_STRING);
        javaTypeNames.put(Byte.class.getName(), JAVA_BYTE);
        javaTypeNames.put(Short.class.getName(), JAVA_SHORT);
        javaTypeNames.put(Integer.class.getName(), JAVA_INTEGER);
        javaTypeNames.put(Long.class.getName(), JAVA_LONG);
        javaTypeNames.put(java.math.BigDecimal.class.getName(), JAVA_BIGDEC);
        javaTypeNames.put(Double.class.getName(), JAVA_DOUBLE);
        javaTypeNames.put(java.sql.Date.class.getName(), JAVA_SQLDATE);
        javaTypeNames.put(java.sql.Time.class.getName(), JAVA_SQLTIME);
        javaTypeNames.put(java.sql.Timestamp.class.getName(), JAVA_SQLTS);
        javaTypeNames.put(org.hsqldb.JavaObject.class.getName(), JAVA_JAVAOBJECT);
        javaTypeNames.put(org.hsqldb.Binary.class.getName(), JAVA_BINARY);
*/
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
}
