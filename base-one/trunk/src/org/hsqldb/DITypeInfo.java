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


package org.hsqldb;

import java.sql.DatabaseMetaData;
import java.util.Locale;
import org.hsqldb.lib.ValuePool;
import org.hsqldb.resources.BundleHandler;

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
final class DITypeInfo implements DITypes {

    private int hnd_create_params = -1;
    private int hnd_local_names   = -1;
    private int hnd_remarks       = -1;
    private int type              = NULL;
    private int typeSub           = TYPE_SUB_DEFAULT;

    public DITypeInfo() {
        setLocale(Locale.getDefault());
    }

    Integer getCharOctLen() {
        return null;
    }

    Long getCharOctLenAct() {

        switch (type) {

            case CHAR :
            case LONGVARCHAR :
            case VARCHAR :
                return ValuePool.getLong(2L * Integer.MAX_VALUE);

            case CLOB :
                return ValuePool.getLong(Long.MAX_VALUE);

            default :
                return null;
        }
    }

    String getColStClsName() {

        switch (type) {

            case BIGINT :
                return "java.lang.Long";

            case BINARY :
            case LONGVARBINARY :
            case OTHER :
            case VARBINARY :
                return "[B";

            case BIT :
            case BOOLEAN :
                return "java.lang.Boolean";

            case CHAR :
            case LONGVARCHAR :
            case VARCHAR :
            case XML :    //?
                return "java.lang.String";

            case DATALINK :
                return "java.net.URL";

            case DATE :
                return "java.sql.Date";

            case DECIMAL :
            case NUMERIC :
                return "java.math.BigDecimal";

            case DOUBLE :
            case FLOAT :
            case REAL :
                return "java.lang.Double";

            case INTEGER :
            case SMALLINT :
            case TINYINT :
                return "java.lang.Integer";

            case TIME :
                return "java.sql.Time";

            case TIMESTAMP :
                return "java.sql.Timestamp";

            default :
                return null;
        }
    }

    String getCreateParams() {

        String key;

        switch (type) {

            case BINARY :
            case BLOB :
            case CHAR :
            case CLOB :
            case LONGVARBINARY :
            case LONGVARCHAR :
            case VARBINARY :
            case VARCHAR :
                key = "SIZED";
                break;

            case DECIMAL :
            case NUMERIC :
                key = "DECIMAL";
                break;

            default :
                key = null;
                break;
        }

        return BundleHandler.getString(hnd_create_params, key);
    }

    String getCstMapClsName() {

        switch (type) {

            case ARRAY :
                return "org.hsqldb.jdbcArray";

            case BLOB :
                return "org.hsqldb.jdbcBlob";

            case CLOB :
                return "org.hsqldb.jdbcClob";

            case DISTINCT :
                return "org.hsqldb.jdbcDistinct";

            case REF :
                return "org.hsqldb.jdbcRef";

            case STRUCT :
                return "org.hsqldb.jdbcStruct";

            default :
                return null;
        }
    }

    Integer getDataType() {
        return ValuePool.getInt(type);
    }

    Integer getDataTypeSub() {
        return ValuePool.getInt(typeSub);
    }

    Integer getDefaultScale() {

        switch (type) {

            case BIGINT :
            case INTEGER :
            case SMALLINT :
            case TINYINT :
                return ValuePool.getInt(0);

            default :
                return null;
        }
    }

    Integer getIntervalPrecision() {
        return null;
    }

    String getLiteralPrefix() {

        switch (type) {

            case BINARY :
            case BLOB :
            case CHAR :
            case CLOB :
            case LONGVARBINARY :
            case LONGVARCHAR :
            case VARBINARY :
            case VARCHAR :
                return "'";

            case DATALINK :
                return "{url '";

            case DATE :
                return "{d '";

            case OTHER :
                return "{o '";

            case TIME :
                return "{t '";

            case TIMESTAMP :
                return "{ts '";

            case XML :
                return "{xml '";

            default :
                return null;
        }
    }

    String getLiteralSuffix() {

        switch (type) {

            case BINARY :
            case BLOB :
            case CHAR :
            case CLOB :
            case LONGVARBINARY :
            case LONGVARCHAR :
            case VARBINARY :
            case VARCHAR :
                return "'";

            case DATALINK :
            case DATE :
            case OTHER :
            case TIME :
            case TIMESTAMP :
            case XML :
                return "'}";

            default :
                return null;
        }
    }

    String getLocalName() {

        String key = this.getTypeName();

        if (typeSub == TYPE_SUB_IDENTITY) {
            key = key.replace(' ', '_');
        }

        return BundleHandler.getString(hnd_local_names, key);
    }

    Integer getMaxScale() {

        switch (type) {

            case BIGINT :
            case DATE :
            case INTEGER :
            case SMALLINT :
            case TINYINT :
                return ValuePool.getInt(0);

            case DECIMAL :
            case NUMERIC :
                return ValuePool.getInt(Short.MAX_VALUE);

            case FLOAT :
            case REAL :
            case DOUBLE :
                return ValuePool.getInt(306);

//            case FLOAT :
//            case REAL :
//                return ValuePool.getInt(38);
            default :
                return null;
        }
    }

    Integer getMaxScaleAct() {

        switch (type) {

            case DECIMAL :
            case NUMERIC :
                return ValuePool.getInt(Integer.MAX_VALUE);

            default :
                return getMaxScale();
        }
    }

    Integer getMinScale() {

        switch (type) {

            case BIGINT :
            case DATE :
            case INTEGER :
            case SMALLINT :
            case TINYINT :
                return ValuePool.getInt(0);

            case DECIMAL :
            case NUMERIC :
                return ValuePool.getInt(Short.MIN_VALUE);

            case FLOAT :
            case REAL :
            case DOUBLE :
                return ValuePool.getInt(-324);

//            case FLOAT :
//            case REAL :
//                return ValuePool.getInt(-45);
            default :
                return null;
        }
    }

    Integer getMinScaleAct() {

        switch (type) {

            case DECIMAL :
            case NUMERIC :
                return ValuePool.getInt(Integer.MIN_VALUE);

            default :
                return getMinScale();
        }
    }

    Integer getNullability() {

        return (typeSub == TYPE_SUB_IDENTITY)
               ? ValuePool.getInt(DatabaseMetaData.columnNoNulls)
               : ValuePool.getInt(DatabaseMetaData.columnNullable);
    }

    Integer getNumPrecRadix() {

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
                return ValuePool.getInt(10);

            default :
                return null;
        }
    }

    Integer getPrecision() {

        switch (type) {

            case BINARY :
            case CHAR :
            case LONGVARBINARY :
            case LONGVARCHAR :
            case OTHER :
            case VARBINARY :
            case VARCHAR :
            case XML :
                return ValuePool.getInt(Integer.MAX_VALUE);

            case BIGINT :
                return ValuePool.getInt(19);

            case BIT :
            case BOOLEAN :
                return ValuePool.getInt(1);

            case DATALINK :

                // from SQL CLI spec.  TODO:  Interpretation?
                return ValuePool.getInt(2004);

            case DECIMAL :
            case NUMERIC :

// Integer.MAX_VALUE bit 2's complement number:
// (Integer.MAX_VALUE-1) / ((ln(10)/ln(2)) bits per decimal digit)
// See:  java.math.BigInteger
// - the other alternative is that we could report the numprecradix as 2 and
// report Integer.MAX_VALUE here
                return ValuePool.getInt(646456993);

            case DATE :
            case INTEGER :
                return ValuePool.getInt(10);

            case FLOAT :
            case REAL :
            case DOUBLE :
                return ValuePool.getInt(17);

//            case FLOAT :
//            case REAL :
            case TIME :
                return ValuePool.getInt(8);

            case SMALLINT :
                return ValuePool.getInt(5);

            case TIMESTAMP :
                return ValuePool.getInt(29);

            case TINYINT :
                return ValuePool.getInt(3);

            default :
                return null;
        }
    }

    Long getPrecisionAct() {

        switch (type) {

            case ARRAY :
            case BLOB :
            case CLOB :
                return ValuePool.getLong(Long.MAX_VALUE);

            default :
                return ValuePool.getLong(getPrecision());
        }
    }

    String getRemarks() {

        String key = this.getTypeName();

        if (typeSub == TYPE_SUB_IDENTITY) {
            key = key.replace(' ', '_');
        }

        return BundleHandler.getString(hnd_remarks, key);
    }

    Integer getSearchability() {

        switch (type) {

            case ARRAY :
            case BLOB :
            case CLOB :
            case JAVA_OBJECT :
            case STRUCT :
                return ValuePool.getInt(DatabaseMetaData.typePredNone);

            case OTHER :    // CHECK ME:
                return ValuePool.getInt(DatabaseMetaData.typePredChar);

            default :
                return ValuePool.getInt(DatabaseMetaData.typeSearchable);
        }
    }

    Integer getSqlDataType() {

        // values from SQL200n SQL CLI spec, or DITypes (which in turn borrows
        // first from java.sql.Types and then SQL200n SQL CLI spec) if there
        // was no corresponding value in SQL CLI
        switch (type) {

            case ARRAY :
                return ValuePool.getInt(50);             // SQL_ARRAY

            case BIGINT :
                return ValuePool.getInt(25);             // SQL_BIGINT

            case BINARY :
                return ValuePool.getInt(15);             // SQL_BIT_VARYING

            case BIT :
            case BOOLEAN :
                return ValuePool.getInt(16);             // SQL_BOOLEAN

            case BLOB :
                return ValuePool.getInt(30);             // SQL_BLOB

            case CHAR :
                return ValuePool.getInt(1);              // SQL_CHAR

            case CLOB :
                return ValuePool.getInt(40);             // SQL_CLOB

            case DATALINK :
                return ValuePool.getInt(70);             // SQL_DATALINK

            case DATE :
                return ValuePool.getInt(9);              // SQL_DATETIME

            case DECIMAL :
                return ValuePool.getInt(3);              // SQL_DECIMAL

            case DISTINCT :
                return ValuePool.getInt(17);             // SQL_UDT

            case DOUBLE :
                return ValuePool.getInt(8);              // SQL_DOUBLE

            case FLOAT :
                return ValuePool.getInt(6);              // SQL_FLOAT

            case INTEGER :
                return ValuePool.getInt(4);              // SQL_INTEGER

            case JAVA_OBJECT :
                return ValuePool.getInt(JAVA_OBJECT);    // N/A - maybe SQL_UDT?

            case LONGVARBINARY :
                return ValuePool.getInt(15);             // SQL_BIT_VARYING

            case LONGVARCHAR :
                return ValuePool.getInt(LONGVARCHAR);    // N/A

            case NULL :
                return ValuePool.getInt(0);              // SQL_ALL_TYPES

            case NUMERIC :
                return ValuePool.getInt(2);              // SQL_NUMERIC

            case OTHER :
                return ValuePool.getInt(OTHER);          // N/A - maybe SQL_UDT?

            case REAL :
                return ValuePool.getInt(7);              // SQL_REAL

            case REF :
                return ValuePool.getInt(20);             // SQL_REF

            case SMALLINT :
                return ValuePool.getInt(5);              // SQL_SMALLINTEGER

            case STRUCT :
                return ValuePool.getInt(17);             // SQL_UDT

            case TIME :
                return ValuePool.getInt(9);              // SQL_DATETIME

            case TIMESTAMP :
                return ValuePool.getInt(9);              // SQL_DATETIME

            case TINYINT :
                return ValuePool.getInt(TINYINT);        // N/A

            case VARBINARY :
                return ValuePool.getInt(15);             // SQL_BIT_VARYING

            case VARCHAR :
                return ValuePool.getInt(12);             // SQL_VARCHAR

            case XML :
                return ValuePool.getInt(137);            // SQL_XML

            default :
                return null;
        }
    }

    Integer getSqlDateTimeSub() {

        switch (type) {

            case DATE :
                return ValuePool.getInt(1);

            case TIME :
                return ValuePool.getInt(2);

            case TIMESTAMP :
                return ValuePool.getInt(3);

            default :
                return null;
        }
    }

    String getStdMapClsName() {

        switch (type) {

            case ARRAY :
                return "java.sql.Array";

            case BIGINT :
                return "long";

            case BINARY :
            case LONGVARBINARY :
            case VARBINARY :
                return "[B";

            case BIT :
            case BOOLEAN :
                return "boolean";

            case BLOB :
                return "java.sql.Blob";

            case CHAR :
            case LONGVARCHAR :
            case VARCHAR :
                return "java.lang.String";

            case CLOB :
                return "java.sql.Clob";

            case DATALINK :
                return "java.net.URL";

            case DATE :
                return "java.sql.Date";

            case DECIMAL :
            case NUMERIC :
                return "java.math.BigDecimal";

            case DISTINCT :
            case JAVA_OBJECT :
            case OTHER :
            case XML :    // ???
                return "java.lang.Object";

            case FLOAT :
            case REAL :
            case DOUBLE :
                return "double";

//            case FLOAT :
//            case REAL :
//                return "float";
            case INTEGER :
                return "int";

            case NULL :
                return "null";

            case REF :
                return "java.sql.Ref";

            case SMALLINT :
                return "short";

            case STRUCT :
                return "java.sql.Struct";

            case TIME :
                return "java.sql.Time";

            case TIMESTAMP :
                return "java.sql.Timestamp";

            case TINYINT :
                return "byte";

            default :
                return null;
        }
    }

    int getTypeCode() {
        return type;
    }

    String getTypeName() {

        switch (type) {

            case ARRAY :
                return "ARRAY";

            case BIGINT :
                return (typeSub == TYPE_SUB_IDENTITY) ? "BIGINT IDENTITY"
                                                      : "BIGINT";

            case BINARY :
                return "BINARY";

            case BIT :
                return "BIT";

            case BLOB :
                return "BLOB";

            case BOOLEAN :
                return "BOOLEAN";

            case CHAR :
                return "CHAR";

            case CLOB :
                return "CLOB";

            case DATALINK :
                return "DATALINK";

            case DATE :
                return "DATE";

            case DECIMAL :
                return "DECIMAL";

            case DISTINCT :
                return "DISTINCT";

            case DOUBLE :
                return "DOUBLE";

            case FLOAT :
                return "FLOAT";

            case INTEGER :
                return (typeSub == TYPE_SUB_IDENTITY) ? "INTEGER IDENTITY"
                                                      : "INTEGER";

            case JAVA_OBJECT :
                return "JAVA_OBJECT";

            case LONGVARBINARY :
                return "LONGVARBINARY";

            case LONGVARCHAR :
                return "LONGVARCHAR";

            case NULL :
                return "NULL";

            case NUMERIC :
                return "NUMERIC";

            case OTHER :
                return "OTHER";

            case REAL :
                return "REAL";

            case REF :
                return "REF";

            case SMALLINT :
                return "SMALLINT";

            case STRUCT :
                return "STUCT";

            case TIME :
                return "TIME";

            case TIMESTAMP :
                return "TIMESTAMP";

            case TINYINT :
                return "TINYINT";

            case VARBINARY :
                return "VARBINARY";

            case VARCHAR :
                return (typeSub == TYPE_SUB_IGNORECASE) ? "VARCHAR_IGNORECASE"
                                                        : "VARCHAR";

            case XML :
                return "XML";

            default :
                return null;
        }
    }

    int getTypeSub() {
        return this.typeSub;
    }

    Boolean isAutoIncrement() {

        switch (type) {

            case DECIMAL :
            case DOUBLE :
            case FLOAT :
            case NUMERIC :
            case REAL :
            case SMALLINT :
            case TINYINT :
                return Boolean.FALSE;

            case BIGINT :
            case INTEGER :
                return ValuePool.getBoolean(typeSub == TYPE_SUB_IDENTITY);

            default :
                return null;
        }
    }

    Boolean isCaseSensitive() {

        switch (type) {

            case ARRAY :
            case BLOB :
            case CLOB :
            case DISTINCT :
            case JAVA_OBJECT :
            case NULL :
            case REF :
            case STRUCT :
                return null;

            case CHAR :
            case DATALINK :
            case LONGVARCHAR :
            case OTHER :
            case XML :
                return Boolean.TRUE;

            case VARCHAR :
                return ValuePool.getBoolean(typeSub != TYPE_SUB_IGNORECASE);

            default :
                return Boolean.FALSE;
        }
    }

    Boolean isColStClsSupported() {
        return ValuePool.getBoolean(type == NULL ? true
                                                 : getColStClsName() != null);
    }

    Boolean isFixedPrecisionScale() {

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
                return Boolean.FALSE;

            default :
                return null;
        }
    }

    Boolean isStdMapClsSupported() {

        // its ok to use Class.forName here instead of nameSpace.classForName,
        // because all standard map classes are loaded by the boot loader
        boolean isSup = false;

        switch (type) {

            case ARRAY : {
                try {
                    Class.forName("java.sql.Array");

                    isSup = true;
                } catch (Exception e) {
                    isSup = false;
                }

                break;
            }
            case BLOB : {
                try {
                    Class.forName("java.sql.Blob");

                    isSup = true;
                } catch (Exception e) {
                    isSup = false;
                }

                break;
            }
            case CLOB : {
                try {
                    Class.forName("java.sql.Clob");

                    isSup = true;
                } catch (Exception e) {
                    isSup = false;
                }

                break;
            }
            case DISTINCT : {
                isSup = false;

                break;
            }
            case REF : {
                try {
                    Class.forName("java.sql.Ref");

                    isSup = true;
                } catch (Exception e) {
                    isSup = false;
                }

                break;
            }
            case STRUCT : {
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

    Boolean isSupportedAsPCol() {

        switch (type) {

            case NULL :           // - for void return type
            case JAVA_OBJECT :    // - for Connection as first parm and

            //   Object for return type
            case ARRAY :          // - for Object[] row of Trigger.fire()
                return Boolean.TRUE;

            default :
                return isSupportedAsTCol();
        }
    }

    Boolean isSupportedAsTCol() {

        String columnTypeName;

        if (type == NULL) {
            return Boolean.FALSE;
        }

        columnTypeName = Column.getTypeString(type);

        return ValuePool.getBoolean(columnTypeName != null
                                    && columnTypeName.length() > 0);
    }

    Boolean isUnsignedAttribute() {

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
                return Boolean.FALSE;

            default :
                return null;
        }
    }

    void setLocale(Locale l) {

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

    void setTypeCode(int type) {
        this.type = type;
    }

    void setTypeSub(int typeSub) {
        this.typeSub = typeSub;
    }
}
