/* Copyright (c) 2001-2004, The HSQL Development Group
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

import java.sql.DatabaseMetaData;
import java.util.Locale;

import org.hsqldb.resources.BundleHandler;
import org.hsqldb.store.ValuePool;

/**
 * Provides information intrinsic to each standard data type known to
 * HSQLDB.  This includes all types for which standard type codes are known
 * at the time of writing and thus includes types that may not yet be
 * supported as table or procedure columns. <p>
 *
 * @author  boucherb@users.sourceforge.net
 * @version 1.7.2
 * @since HSQLDB 1.7.2
 */
public final class DITypeInfo {

    /** BundleHandler id for create params resource bundle. */
    private int hnd_create_params = -1;

    /** BundleHandler id for local names resource bundle. */
    private int hnd_local_names = -1;

    /** BundleHandler id for data type remarks resource bundle. */
    private int hnd_remarks = -1;

    /** The SQL type code on which this object is reporting. */
    private int type = Types.NULL;

    /** The HSQLDB subtype code on which this object is reporting. */
    private int typeSub = Types.TYPE_SUB_DEFAULT;

    /** Creates a new DITypeInfo object having the default Locale. */
    public DITypeInfo() {
        setLocale(Locale.getDefault());
    }

    /**
     * Retrieves the maximum Integer.MAX_VALUE bounded length, in bytes, for
     * character types. <p>
     *
     * @return the maximum Integer.MAX_VALUE bounded length, in
     *    bytes, for character types
     */
    public Integer getCharOctLen() {
        return null;
    }

    /**
     * Retrieves the maximum Long.MAX_VALUE bounded length, in bytes, for
     * character types. <p>
     *
     * @return the maximum Long.MAX_VALUE bounded length, in
     *    bytes, for character types
     */
    public Long getCharOctLenAct() {

        switch (type) {

            case Types.CHAR :
            case Types.LONGVARCHAR :
            case Types.VARCHAR :
                return ValuePool.getLong(2L * Integer.MAX_VALUE);

            case Types.CLOB :
                return ValuePool.getLong(Long.MAX_VALUE);

            default :
                return null;
        }
    }

    /**
     * Retrieves the fully-qualified name of the Java class whose instances
     * are manufactured by HSQLDB to store table column values in
     * memory. <p>
     *
     * This is also typically the fully-qualified name of the Java class whose
     * instances are manufactured by HSQLDB in response to
     * jdbcResultSet.getObject(int). <p>
     *
     * @return the fully-qualified name of the Java class whose
     *    instances are manufactured by HSQLDB to store
     *    table column values in memory
     */
    public String getColStClsName() {

        switch (type) {

            case Types.BIGINT :
                return "java.lang.Long";

            case Types.BINARY :
            case Types.LONGVARBINARY :
            case Types.VARBINARY :

                // but wrapped by org.hsqldb.Binary
                return "[B";

            case Types.OTHER :

                // but wrapped by org.hsqldb.JavaObject
                return "java.lang.Object";

            case Types.BIT :
            case Types.BOOLEAN :
                return "java.lang.Boolean";

            case Types.CHAR :
            case Types.LONGVARCHAR :
            case Types.VARCHAR :
            case Types.XML :    //?
                return "java.lang.String";

            case Types.DATALINK :
                return "java.net.URL";

            case Types.DATE :
                return "java.sql.Date";

            case Types.DECIMAL :
            case Types.NUMERIC :
                return "java.math.BigDecimal";

            case Types.DOUBLE :
            case Types.FLOAT :
            case Types.REAL :
                return "java.lang.Double";

            case Types.INTEGER :
            case Types.SMALLINT :
            case Types.TINYINT :
                return "java.lang.Integer";

            case Types.TIME :
                return "java.sql.Time";

            case Types.TIMESTAMP :
                return "java.sql.Timestamp";

            default :
                return null;
        }
    }

    /**
     * Retrieves a character sequence representing a CSV list, in
     * DDL declaraion order, of the create parameters for the type. <p>
     *
     * @return a character sequence representing a CSV
     *    list, in DDL declaraion order, of the create
     *    parameters for the type.
     */
    public String getCreateParams() {

        String names;

        switch (type) {

            case Types.CHAR :
            case Types.VARCHAR :
                names = "LENGTH";
                break;

            case Types.DECIMAL :
            case Types.NUMERIC :
                names = "PRECISION,SCALE";
                break;

            default :
                names = null;
                break;
        }

        return names;
    }

    /**
     * Retrieves the fully-qualified name of the HSQLDB-provided java.sql
     * interface implementation class whose instances would be manufactured
     * by HSQLDB to retrieve column values of this type, if the
     * the type does not have a standard Java mapping.  <p>
     *
     * This value is simply the expected class name, regardless of whether
     * HSQLDB, the specific HSQLDB distribution instance or the hosting JVM
     * actually provide or support such implementations. That is, as of a
     * specific release, HSQLDB may not yet provide such an implementation
     * or may not automatically map to it or may not support it as a table
     * column type, the version of java.sql may not define the interface to
     * implement and the HSQLDB jar may not contain the implementation
     * classes, even if they are defined in the corresponding release
     * and build options and are supported under the hosting JVM's java.sql
     * version.<p>
     *
     * @return the fully-qualified name of the HSQLDB-provided java.sql
     *    interface implementation class whose instances would
     *    be manufactured by HSQLDB to retrieve column values of
     *    this type, given that the the type does not have a
     *    standard Java mapping and regardless of whether a class
     *    with the indicated name is actually implemented or
     *    available on the class path
     */
    public String getCstMapClsName() {

        switch (type) {

            case Types.ARRAY :
                return "org.hsqldb.jdbc.jdbcArray";

            case Types.BLOB :
                return "org.hsqldb.jdbc.jdbcBlob";

            case Types.CLOB :
                return "org.hsqldb.jdbc.jdbcClob";

            case Types.DISTINCT :
                return "org.hsqldb.jdbc.jdbcDistinct";

            case Types.REF :
                return "org.hsqldb.jdbc.jdbcRef";

            case Types.STRUCT :
                return "org.hsqldb.jdbc.jdbcStruct";

            default :
                return null;
        }
    }

    /**
     * Retrieves the maximum length that a String representation of
     * the type may have.  For character and datetime types, this is the
     * same as the maximum length/precision, repectively. For numeric
     * types, this is the precision, plus the length of the negation
     * character (1), plus the maximum number of characters that may occupy
     * the exponent character sequence.  For bit/boolean types, it is the
     * length of the character sequence "false", the longer of the two
     * boolean value String representations.  For any other types, the
     * value is the result of whatever calculation must be performed to
     * determine the maximum length of its String representation. If
     * the size is unknown, unknowable or inapplicable, zero is returned. <p>
     *
     * @return the maximum length that a String representation of
     *      the type may have
     */
    public int getMaxDisplaySize() {

        switch (type) {

            case Types.BINARY :
            case Types.CHAR :
            case Types.LONGVARBINARY :
            case Types.LONGVARCHAR :
            case Types.OTHER :
            case Types.VARBINARY :
            case Types.VARCHAR :
            case Types.XML :
                return Integer.MAX_VALUE;    // same as precision

            case Types.BIGINT :
                return 20;                   // precision + "-".length();

            case Types.BIT :
            case Types.BOOLEAN :
                return 5;                    // Math.max("true".length(),"false".length);

            case Types.DATALINK :
                return 20004;                // same as precision

            case Types.DECIMAL :
            case Types.NUMERIC :
                return 646456995;            // precision + "-.".length()

            case Types.DATE :
                return 10;                   // same as precision

            case Types.INTEGER :
                return 11;                   // precision + "-".length();

            case Types.FLOAT :
            case Types.REAL :
            case Types.DOUBLE :
                return 23;                   // String.valueOf(-Double.MAX_VALUE).length();

            case Types.TIME :
                return 8;                    // same as precision

            case Types.SMALLINT :
                return 6;                    // precision + "-".length();

            case Types.TIMESTAMP :
                return 29;                   // same as precision

            case Types.TINYINT :
                return 4;                    // precision + "-".length();

            default :
                return 0;                    // unknown
        }
    }

    /**
     * Retrieves the data type, as an Integer. <p>
     *
     * @return the data type, as an Integer.
     */
    public Integer getDataType() {
        return ValuePool.getInt(type);
    }

    /**
     * Retrieves the data subtype, as an Integer. <p>
     *
     * @return the data subtype, as an Integer
     */
    Integer getDataTypeSub() {
        return ValuePool.getInt(typeSub);
    }

    /**
     * Retrieves the implied or single permitted scale for exact numeric
     * types, when no scale is declared or no scale declaration is
     * permitted. <p>
     *
     * @return the implied or single permitted scale for exact numeric
     *    types, when no scale is declared or no scale declaration
     *    is permitted
     */
    public Integer getDefaultScale() {

        switch (type) {

            case Types.BIGINT :
            case Types.INTEGER :
            case Types.SMALLINT :
            case Types.TINYINT :
                return ValuePool.getInt(0);

            default :
                return null;
        }
    }

    /**
     * Retrieves null (not implemented). <p>
     *
     * @return null (not implemented)
     */
    public Integer getIntervalPrecision() {
        return null;
    }

    /**
     * Retrieves the character(s) prefixing a literal of this type. <p>
     *
     * @return the character(s) prefixing a literal of this type.
     */
    public String getLiteralPrefix() {

        switch (type) {

            case Types.BINARY :
            case Types.BLOB :
            case Types.CHAR :
            case Types.CLOB :
            case Types.LONGVARBINARY :
            case Types.LONGVARCHAR :
            case Types.VARBINARY :
            case Types.VARCHAR :
                return "'";

            case Types.DATALINK :
                return "'";    // hypothetically: "{url '";

            case Types.DATE :
                return "'";    // or JDBC escape: "{d '";

            case Types.OTHER :
                return "'";    // hypothetically: "{o '"; or new "pkg.cls"(...)

            case Types.TIME :
                return "'";    // or JDBC escape: "{t '";

            case Types.TIMESTAMP :
                return "'";    // or JDBC escape: "{ts '";

            case Types.XML :
                return "'";    // hypothetically: "{xml '";

            default :
                return null;
        }
    }

    /**
     * Retrieves the character(s) terminating a literal of this type. <p>
     *
     * @return the character(s) terminating a literal of this type
     */
    public String getLiteralSuffix() {

        switch (type) {

            case Types.BINARY :
            case Types.BLOB :
            case Types.CHAR :
            case Types.CLOB :
            case Types.LONGVARBINARY :
            case Types.LONGVARCHAR :
            case Types.VARBINARY :
            case Types.VARCHAR :
                return "'";

            case Types.DATALINK :
            case Types.DATE :
            case Types.OTHER :
            case Types.TIME :
            case Types.TIMESTAMP :
            case Types.XML :
                return "'";    // or JDBC close escape: "'}";

            default :
                return null;
        }
    }

    /**
     * Retrieves a localized representation of the type's name, for human
     * consumption only. <p>
     *
     * @return a localized representation of the type's name
     */
    public String getLocalName() {

        String key = this.getTypeName();

        if (typeSub == Types.TYPE_SUB_IDENTITY) {
            key = key.replace(' ', '_');
        }

        return BundleHandler.getString(hnd_local_names, key);
    }

    /**
     * Retrieves the maximum Short.MAX_VALUE bounded scale supported
     * for the type. <p>
     *
     * @return the maximum Short.MAX_VALUE bounded scale supported
     *    for the type
     */
    public Integer getMaxScale() {

        switch (type) {

            case Types.BIGINT :
            case Types.DATE :
            case Types.INTEGER :
            case Types.SMALLINT :
            case Types.TINYINT :
                return ValuePool.getInt(0);

            case Types.DECIMAL :
            case Types.NUMERIC :
                return ValuePool.getInt(Short.MAX_VALUE);

            case Types.FLOAT :
            case Types.REAL :
            case Types.DOUBLE :
                return ValuePool.getInt(306);

//            case Types.FLOAT :
//            case Types.REAL :
//                return ValuePool.getInt(38);
            default :
                return null;
        }
    }

    /**
     * Retrieves the maximum Integer.MAX_VALUE bounded scale supported for
     * the type. <p>
     *
     * @return the maximum Integer.MAX_VALUE bounded scale supported
     *    for the type
     */
    public Integer getMaxScaleAct() {

        switch (type) {

            case Types.DECIMAL :
            case Types.NUMERIC :
                return ValuePool.getInt(Integer.MAX_VALUE);

            default :
                return getMaxScale();
        }
    }

    /**
     * Retrieves the minumum Short.MIN_VALUE bounded scale supported for
     * the type. <p>
     *
     * @return the minumum Short.MIN_VALUE bounded scale
     *    supported for the type
     */
    public Integer getMinScale() {

        switch (type) {

            case Types.BIGINT :
            case Types.DATE :
            case Types.INTEGER :
            case Types.SMALLINT :
            case Types.TINYINT :
                return ValuePool.getInt(0);

            case Types.DECIMAL :
            case Types.NUMERIC :
                return ValuePool.getInt(Short.MIN_VALUE);

            case Types.FLOAT :
            case Types.REAL :
            case Types.DOUBLE :
                return ValuePool.getInt(-324);

//            case Types.FLOAT :
//            case Types.REAL :
//                return ValuePool.getInt(-45);
            default :
                return null;
        }
    }

    /**
     * Retrieves the minumum Integer.MIN_VALUE bounded scale supported
     * for the type. <p>
     *
     * @return the minumum Integer.MIN_VALUE bounded scale supported
     *    for the type
     */
    public Integer getMinScaleAct() {

        switch (type) {

            case Types.DECIMAL :
            case Types.NUMERIC :
                return ValuePool.getInt(Integer.MIN_VALUE);

            default :
                return getMinScale();
        }
    }

    /**
     * Retrieves the DatabaseMetaData default nullability code for
     * the type. <p>
     *
     * @return the DatabaseMetaData nullability code for the type.
     */
    public Integer getNullability() {

        return (typeSub == Types.TYPE_SUB_IDENTITY)
               ? ValuePool.getInt(DatabaseMetaData.columnNoNulls)
               : ValuePool.getInt(DatabaseMetaData.columnNullable);
    }

    /**
     * Retrieves the number base which is to be used to interpret the value
     * reported by getPrecision(). <p>
     *
     * @return the number base which is to be used to interpret the
     *    value reported by getPrecision()
     */
    public Integer getNumPrecRadix() {

        switch (type) {

            case Types.BIGINT :
            case Types.DECIMAL :
            case Types.DOUBLE :
            case Types.FLOAT :
            case Types.INTEGER :
            case Types.NUMERIC :
            case Types.REAL :
            case Types.SMALLINT :
            case Types.TINYINT :
                return ValuePool.getInt(10);

            default :
                return null;
        }
    }

    /**
     * Retrieves the maximum Integer.MAX_VALUE bounded length or precision for
     * the type. <p>
     *
     * @return the maximum Integer.MAX_VALUE bounded length or
     *    precision for the type
     */
    public Integer getPrecision() {

        switch (type) {

            case Types.BINARY :
            case Types.CHAR :
            case Types.LONGVARBINARY :
            case Types.LONGVARCHAR :
            case Types.OTHER :
            case Types.VARBINARY :
            case Types.VARCHAR :
            case Types.XML :
                return ValuePool.getInt(Integer.MAX_VALUE);

            case Types.BIGINT :
                return ValuePool.getInt(19);

            case Types.BIT :
            case Types.BOOLEAN :
                return ValuePool.getInt(1);

            case Types.DATALINK :

                // from SQL CLI spec.  TODO:  Interpretation?
                return ValuePool.getInt(20004);

            case Types.DECIMAL :
            case Types.NUMERIC :

// Integer.MAX_VALUE bit 2's complement number:
// (Integer.MAX_VALUE-1) / ((ln(10)/ln(2)) bits per decimal digit)
// See:  java.math.BigInteger
// - the other alternative is that we could report the numprecradix as 2 and
// report Integer.MAX_VALUE here
                return ValuePool.getInt(646456993);

            case Types.DATE :
            case Types.INTEGER :
                return ValuePool.getInt(10);

            case Types.FLOAT :
            case Types.REAL :
            case Types.DOUBLE :
                return ValuePool.getInt(17);

//            case Types.FLOAT :
//            case Types.REAL :
            case Types.TIME :
                return ValuePool.getInt(8);

            case Types.SMALLINT :
                return ValuePool.getInt(5);

            case Types.TIMESTAMP :
                return ValuePool.getInt(29);

            case Types.TINYINT :
                return ValuePool.getInt(3);

            default :
                return null;
        }
    }

    /**
     * Retrieves the maximum Long.MAX_VALUE bounded length or precision for
     * the type. <p>
     *
     * @return the maximum Long.MAX_VALUE bounded length or
     *    precision for the type
     */
    public Long getPrecisionAct() {

        Integer temp = getPrecision();

        if (temp == null) {
            return ValuePool.getLong(Long.MAX_VALUE);
        } else {
            return ValuePool.getLong(temp.longValue());
        }
    }

    /**
     * Retrieves the localized remarks (if any) on the type. <p>
     *
     * @return the localized remarks on the type.
     */
    public String getRemarks() {

        String key = this.getTypeName();

        if (typeSub == Types.TYPE_SUB_IDENTITY) {
            key = key.replace(' ', '_');
        }

        return BundleHandler.getString(hnd_remarks, key);
    }

    /**
     * Retrieves the DatabaseMetaData searchability code for the type. <p>
     *
     * @return the DatabaseMetaData searchability code for the type
     */
    public Integer getSearchability() {

        switch (type) {

            case Types.ARRAY :
            case Types.BLOB :
            case Types.CLOB :
            case Types.JAVA_OBJECT :
            case Types.STRUCT :
                return ValuePool.getInt(DatabaseMetaData.typePredNone);

            case Types.OTHER :    // CHECK ME:
                return ValuePool.getInt(DatabaseMetaData.typePredChar);

            default :
                return ValuePool.getInt(DatabaseMetaData.typeSearchable);
        }
    }

    /**
     * Retrieves the SQL CLI data type code for the type. <p>
     *
     * @return the SQL CLI data type code for the type
     */
    public Integer getSqlDataType() {

        // values from SQL200n SQL CLI spec, or DITypes (which in turn borrows
        // first from java.sql.Types and then SQL200n SQL CLI spec) if there
        // was no corresponding value in SQL CLI
        switch (type) {

            case Types.ARRAY :
                return ValuePool.getInt(Types.SQL_ARRAY);        // SQL_ARRAY

            case Types.BIGINT :
                return ValuePool.getInt(Types.SQL_BIGINT);       // SQL_BIGINT

            case Types.BINARY :
                return ValuePool.getInt(Types.SQL_BLOB);         // fredt- was SQL_BIT_VARYING

            case Types.BIT :
            case Types.BOOLEAN :
                return ValuePool.getInt(Types.SQL_BOOLEAN);      // SQL_BOOLEAN

            case Types.BLOB :
                return ValuePool.getInt(Types.SQL_BLOB);         // SQL_BLOB

            case Types.CHAR :
                return ValuePool.getInt(Types.SQL_CHAR);         // SQL_CHAR

            case Types.CLOB :
                return ValuePool.getInt(Types.SQL_CLOB);         // SQL_CLOB

            case Types.DATALINK :
                return ValuePool.getInt(Types.SQL_DATALINK);     // SQL_DATALINK

            case Types.DATE :

                // NO:  This is the _concise_ code, whereas what we want to
                //      return here is the Data Type Code column value from
                //      Table 38 in the SQL 200n FCD.  This method is used
                //      by DatabaseInformationMain to list the sql type
                //      column in the SYSTEM_TYPEINFO table, which is specified
                //      by JDBC as the sql data type code, not the concise code.
                //      That is why there is a sql datetime sub column
                //      specified as well.
                // return ValuePool.getInt(Types.SQL_DATE);         // fredt - was SQL_DATETIME
                return ValuePool.getInt(Types.SQL_DATETIME);

            case Types.DECIMAL :
                return ValuePool.getInt(Types.SQL_DECIMAL);      // SQL_DECIMAL

            case Types.DISTINCT :
                return ValuePool.getInt(Types.SQL_UDT);          // SQL_UDT

            case Types.DOUBLE :
                return ValuePool.getInt(Types.SQL_DOUBLE);       // SQL_DOUBLE

            case Types.FLOAT :
                return ValuePool.getInt(Types.SQL_FLOAT);        // SQL_FLOAT

            case Types.INTEGER :
                return ValuePool.getInt(Types.SQL_INTEGER);      // SQL_INTEGER

            case Types.JAVA_OBJECT :
                return ValuePool.getInt(Types.JAVA_OBJECT);      // N/A - maybe SQL_UDT?

            case Types.LONGVARBINARY :
                return ValuePool.getInt(Types.SQL_BLOB);         // was SQL_BIT_VARYING

            case Types.LONGVARCHAR :
                return ValuePool.getInt(Types.SQL_CLOB);         //

            case Types.NULL :
                return ValuePool.getInt(Types.SQL_ALL_TYPES);    // SQL_ALL_TYPES

            case Types.NUMERIC :
                return ValuePool.getInt(Types.SQL_NUMERIC);      // SQL_NUMERIC

            case Types.OTHER :
                return ValuePool.getInt(Types.OTHER);            // N/A - maybe SQL_UDT?

            case Types.REAL :
                return ValuePool.getInt(Types.SQL_REAL);         // SQL_REAL

            case Types.REF :
                return ValuePool.getInt(Types.SQL_REF);          // SQL_REF

            case Types.SMALLINT :
                return ValuePool.getInt(Types.SQL_SMALLINT);     // SQL_SMALLINTEGER

            case Types.STRUCT :
                return ValuePool.getInt(Types.SQL_UDT);          // SQL_UDT

            case Types.TIME :

                // NO:  This is the _concise_ code, whereas what we want to
                //      return here is the Data Type Code column value from
                //      Table 38 in the SQL 200n FCD.  This method is used
                //      by DatabaseInformationMain to list the sql type
                //      column in the SYSTEM_TYPEINFO table, which is specified
                //      by JDBC as the sql data type code, not the concise code.
                //      That is why there is a sql datetime sub column
                //      specified as well.
                // return ValuePool.getInt(Types.SQL_TIME);         // fredt - was SQL_DATETIME
                return ValuePool.getInt(Types.SQL_DATETIME);

            case Types.TIMESTAMP :

                // NO:  This is the _concise_ code, whereas what we want to
                //      return here is the Data Type Code column value from
                //      Table 38 in the SQL CLI 200n FCD.  This method is used
                //      by DatabaseInformationMain to list the sql type
                //      column in the SYSTEM_TYPEINFO table, which is specified
                //      by JDBC as the sql data type code, not the concise code.
                //      That is why there is a sql datetime sub column
                //      specified as well.
                // return ValuePool.getInt(Types.SQL_TIMESTAMP);    // fredt - was SQL_DATETIME
                return ValuePool.getInt(Types.SQL_DATETIME);

            case Types.TINYINT :
                return ValuePool.getInt(Types.TINYINT);          // N/A

            case Types.VARBINARY :
                return ValuePool.getInt(Types.SQL_BLOB);         // SQL_BIT_VARYING

            case Types.VARCHAR :
                return ValuePool.getInt(Types.SQL_VARCHAR);      // SQL_VARCHAR

            case Types.XML :
                return ValuePool.getInt(Types.SQL_XML);          // SQL_XML

            default :
                return null;
        }
    }

    /**
     * Retrieves the SQL CLI datetime subcode for the type. <p>
     *
     * @return the SQL CLI datetime subcode for the type
     */
    public Integer getSqlDateTimeSub() {

        switch (type) {

            case Types.DATE :
                return ValuePool.getInt(1);

            case Types.TIME :
                return ValuePool.getInt(2);

            case Types.TIMESTAMP :
                return ValuePool.getInt(3);

            default :
                return null;
        }
    }

    /**
     * Retrieve the fully qualified name of the recommended standard Java
     * primitive, class or java.sql interface mapping for the type. <p>
     *
     * @return the fully qualified name of the recommended standard Java
     *    primitive, class or java.sql interface mapping for
     *    the type
     */
    public String getStdMapClsName() {

        switch (type) {

            case Types.ARRAY :
                return "java.sql.Array";

            case Types.BIGINT :
                return "long";

            case Types.BINARY :
            case Types.LONGVARBINARY :
            case Types.VARBINARY :
                return "[B";

            case Types.BIT :
            case Types.BOOLEAN :
                return "boolean";

            case Types.BLOB :
                return "java.sql.Blob";

            case Types.CHAR :
            case Types.LONGVARCHAR :
            case Types.VARCHAR :
                return "java.lang.String";

            case Types.CLOB :
                return "java.sql.Clob";

            case Types.DATALINK :
                return "java.net.URL";

            case Types.DATE :
                return "java.sql.Date";

            case Types.DECIMAL :
            case Types.NUMERIC :
                return "java.math.BigDecimal";

            case Types.DISTINCT :
            case Types.JAVA_OBJECT :
            case Types.OTHER :
            case Types.XML :    // ???
                return "java.lang.Object";

            case Types.FLOAT :
            case Types.REAL :
            case Types.DOUBLE :
                return "double";

//            case Types.FLOAT :
//            case Types.REAL :
//                return "float";
            case Types.INTEGER :
                return "int";

            case Types.NULL :
                return "null";

            case Types.REF :
                return "java.sql.Ref";

            case Types.SMALLINT :
                return "short";

            case Types.STRUCT :
                return "java.sql.Struct";

            case Types.TIME :
                return "java.sql.Time";

            case Types.TIMESTAMP :
                return "java.sql.Timestamp";

            case Types.TINYINT :
                return "byte";

            default :
                return null;
        }
    }

    /**
     * Retrieves the data type as an int. <p>
     *
     * @return the data type as an int
     */
    public int getTypeCode() {
        return type;
    }

    /**
     * Retrieves the canonical data type name HSQLDB associates with
     * the type.  <p>
     *
     * This typically matches the designated JDBC name, with one or
     * two exceptions. <p>
     *
     * @return the canonical data type name HSQLDB associates with the type
     */
    public String getTypeName() {

        switch (type) {

            case Types.ARRAY :
                return "ARRAY";

            case Types.BIGINT :
                return (typeSub == Types.TYPE_SUB_IDENTITY)
                       ? "BIGINT IDENTITY"
                       : "BIGINT";

            case Types.BINARY :
                return "BINARY";

            case Types.BIT :
                return "BIT";

            case Types.BLOB :
                return "BLOB";

            case Types.BOOLEAN :
                return "BOOLEAN";

            case Types.CHAR :
                return "CHAR";

            case Types.CLOB :
                return "CLOB";

            case Types.DATALINK :
                return "DATALINK";

            case Types.DATE :
                return "DATE";

            case Types.DECIMAL :
                return "DECIMAL";

            case Types.DISTINCT :
                return "DISTINCT";

            case Types.DOUBLE :
                return "DOUBLE";

            case Types.FLOAT :
                return "FLOAT";

            case Types.INTEGER :
                return (typeSub == Types.TYPE_SUB_IDENTITY)
                       ? "INTEGER IDENTITY"
                       : "INTEGER";

            case Types.JAVA_OBJECT :
                return "JAVA_OBJECT";

            case Types.LONGVARBINARY :
                return "LONGVARBINARY";

            case Types.LONGVARCHAR :
                return "LONGVARCHAR";

            case Types.NULL :
                return "NULL";

            case Types.NUMERIC :
                return "NUMERIC";

            case Types.OTHER :
                return "OTHER";

            case Types.REAL :
                return "REAL";

            case Types.REF :
                return "REF";

            case Types.SMALLINT :
                return "SMALLINT";

            case Types.STRUCT :
                return "STUCT";

            case Types.TIME :
                return "TIME";

            case Types.TIMESTAMP :
                return "TIMESTAMP";

            case Types.TINYINT :
                return "TINYINT";

            case Types.VARBINARY :
                return "VARBINARY";

            case Types.VARCHAR :
                return (typeSub == Types.TYPE_SUB_IGNORECASE)
                       ? "VARCHAR_IGNORECASE"
                       : "VARCHAR";

            case Types.XML :
                return "XML";

            default :
                return null;
        }
    }

    /**
     * Retrieves the HSQLDB data subtype as an int. <p>
     *
     * @return the HSQLDB data subtype as an int
     */
    public int getTypeSub() {
        return this.typeSub;
    }

    /**
     * Retrieves whether the type is an IDENTITY type. <p>
     *
     * @return whether the type is an IDENTITY type.
     */
    public Boolean isAutoIncrement() {

        switch (type) {

            case Types.DECIMAL :
            case Types.DOUBLE :
            case Types.FLOAT :
            case Types.NUMERIC :
            case Types.REAL :
            case Types.SMALLINT :
            case Types.TINYINT :
                return Boolean.FALSE;

            case Types.BIGINT :
            case Types.INTEGER :
                return ValuePool.getBoolean(typeSub
                                            == Types.TYPE_SUB_IDENTITY);

            default :
                return null;
        }
    }

    /**
     * Retrieves whether the type is case-sensitive in collations and
     * comparisons. <p>
     *
     * @return whether the type is case-sensitive in collations and
     *    comparisons.
     */
    public Boolean isCaseSensitive() {

        switch (type) {

            case Types.ARRAY :
            case Types.BLOB :
            case Types.CLOB :
            case Types.DISTINCT :
            case Types.JAVA_OBJECT :
            case Types.NULL :
            case Types.REF :
            case Types.STRUCT :
                return null;

            case Types.CHAR :
            case Types.DATALINK :
            case Types.LONGVARCHAR :
            case Types.OTHER :
            case Types.XML :
                return Boolean.TRUE;

            case Types.VARCHAR :
                return ValuePool.getBoolean(typeSub
                                            != Types.TYPE_SUB_IGNORECASE);

            default :
                return Boolean.FALSE;
        }
    }

    /**
     * Retrieves whether, under the current release, class path and hosting
     * JVM, HSQLDB supports storing this type in table columns. <p>
     *
     * This value also typically represents whether HSQLDB supports retrieving
     * values of this type in the columns of ResultSets.
     * @return whether, under the current release, class path
     *    and hosting JVM, HSQLDB supports storing this
     *    type in table columns
     */
    public Boolean isColStClsSupported() {

        return ValuePool.getBoolean(type == Types.NULL ? true
                                                       : getColStClsName()
                                                       != null);
    }

    /**
     * Retrieves whether values of this type have a fixed precision and
     * scale. <p>
     *
     * @return whether values of this type have a fixed
     *    precision and scale.
     */
    public Boolean isFixedPrecisionScale() {

        switch (type) {

            case Types.BIGINT :
            case Types.DECIMAL :
            case Types.DOUBLE :
            case Types.FLOAT :
            case Types.INTEGER :
            case Types.NUMERIC :
            case Types.REAL :
            case Types.SMALLINT :
            case Types.TINYINT :
                return Boolean.FALSE;

            default :
                return null;
        }
    }

    /**
     * Retrieve whether the fully qualified name reported by getStdMapClsName()
     * is supported as a jdbcResultSet.getXXX return type under the current
     * HSQLDB release, class path and hosting JVM. <p>
     *
     * @return whether the fully qualified name reported by getStdMapClsName()
     * is supported as a jdbcResultSet.getXXX return type under the current
     * HSQLDB release, class path and hosting JVM.
     */
    public Boolean isStdMapClsSupported() {

        // its ok to use Class.forName here instead of nameSpace.classForName,
        // because all standard map classes are loaded by the boot loader
        boolean isSup = false;

        switch (type) {

            case Types.ARRAY : {
                try {
                    Class.forName("java.sql.Array");

                    isSup = true;
                } catch (Exception e) {
                    isSup = false;
                }

                break;
            }
            case Types.BLOB : {
                try {
                    Class.forName("java.sql.Blob");

                    isSup = true;
                } catch (Exception e) {
                    isSup = false;
                }

                break;
            }
            case Types.CLOB : {
                try {
                    Class.forName("java.sql.Clob");

                    isSup = true;
                } catch (Exception e) {
                    isSup = false;
                }

                break;
            }
            case Types.DISTINCT : {
                isSup = false;

                break;
            }
            case Types.REF : {
                try {
                    Class.forName("java.sql.Ref");

                    isSup = true;
                } catch (Exception e) {
                    isSup = false;
                }

                break;
            }
            case Types.STRUCT : {
                try {
                    Class.forName("java.sql.Struct");

                    isSup = true;
                } catch (Exception e) {
                    isSup = false;
                }

                break;
            }
            default : {
                isSup = (getStdMapClsName() != null);

                break;
            }
        }

        return ValuePool.getBoolean(isSup);
    }

    /**
     * Retrieves whether, under the current release, class path and
     * hosting JVM, HSQLDB supports passing or receiving this type as
     * the value of a procedure column. <p>
     *
     * @return whether, under the current release, class path and
     *    hosting JVM, HSQLDB supports passing or receiving
     *    this type as the value of a procedure column.
     */
    public Boolean isSupportedAsPCol() {

        switch (type) {

            case Types.NULL :           // - for void return type
            case Types.JAVA_OBJECT :    // - for Connection as first parm and

            //   Object for return type
            case Types.ARRAY :          // - for Object[] row of Trigger.fire()
                return Boolean.TRUE;

            default :
                return isSupportedAsTCol();
        }
    }

    /**
     * Retrieves whether, under the current release, class path and
     * hosting JVM, HSQLDB supports this as the type of a table
     * column. <p>
     *
     * @return whether, under the current release, class path
     *    and hosting JVM, HSQLDB supports this type
     *    as the values of a table column
     */
    public Boolean isSupportedAsTCol() {

        String columnTypeName;

        if (type == Types.NULL) {
            return Boolean.FALSE;
        }

        columnTypeName = Types.getTypeString(type);

        return ValuePool.getBoolean(columnTypeName != null);
    }

    /**
     * Retrieves whether values of this type are unsigned. <p>
     *
     * @return whether values of this type are unsigned
     */
    public Boolean isUnsignedAttribute() {

        switch (type) {

            case Types.BIGINT :
            case Types.DECIMAL :
            case Types.DOUBLE :
            case Types.FLOAT :
            case Types.INTEGER :
            case Types.NUMERIC :
            case Types.REAL :
            case Types.SMALLINT :
            case Types.TINYINT :
                return Boolean.FALSE;

            default :
                return null;
        }
    }

    /**
     * Assigns the Locale object used to retrieve this object's
     * resource bundle dependent values. <p>
     *
     * @param l the Locale object used to retrieve this object's resource
     *      bundle dependent values
     */
    public void setLocale(Locale l) {

        Locale oldLocale;

        synchronized (BundleHandler.class) {
            oldLocale = BundleHandler.getLocale();

            BundleHandler.setLocale(l);

            hnd_create_params =
                BundleHandler.getBundleHandle("data-type-create-parameters",
                                              null);
            hnd_local_names = BundleHandler.getBundleHandle("data-type-names",
                    null);
            hnd_remarks = BundleHandler.getBundleHandle("data-type-remarks",
                    null);

            BundleHandler.setLocale(oldLocale);
        }
    }

    /**
     * Assigns the SQL data type code on which this object is to report. <p>
     *
     * @param type the SQL data type code on which this object is to report
     */
    public void setTypeCode(int type) {
        this.type = type;
    }

    /**
     * Assigns the HSQLDB data subtype code on which this object is
     * to report. <p>
     *
     * @param typeSub the HSQLDB data subtype code on which this object
     *      is to report
     */
    public void setTypeSub(int typeSub) {
        this.typeSub = typeSub;
    }
}
