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

/*
 * originally created as DIProcedureInfo.java on February 23, 2003, 12:56 AM
 */

import java.io.Externalizable;
import java.io.Serializable;

import java.lang.reflect.Method;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.HsqlHashMap;
import org.hsqldb.lib.ValuePool;

import org.hsqldb.resources.BundleHandler;

/**
 *
 * @author  boucherb@users.sourceforge.net
 * @version 1.7.2
 * @since HSQLDB 1.7.2
 */
final class DIProcedureInfo implements DITypes {

    private Class clazz;

    private Class[] colClasses;

    private int[] colTypes;

    private int colOffset;

    private int colCount;

    private boolean colsResolved;

    private String csig;

    private String fqn;

    private int hnd_remarks;

    private Method method;

    private String sig;

    private DINameSpace nameSpace;

    private final HsqlHashMap typeMap = new HsqlHashMap();

    public DIProcedureInfo(DINameSpace ns) throws SQLException {
        setNameSpace(ns);
    }

    private int colOffset() {
        if (!colsResolved) {
            resolveCols();
        }
        return colOffset;
    }

    HsqlArrayList getAliases() {
        return (HsqlArrayList) nameSpace.getInverseAliasMap().get(getFQN());
    }

    String getCanonicalSignature() {
        if (csig == null) {
            csig = method.toString();
        }
        return csig;
    }

    Class getColClass(int i) {
        if (!colsResolved) {
            resolveCols();
        }
        return colClasses[i + colOffset()];
    }

    int getColCount() {
        if (!colsResolved) {
            resolveCols();
        }
        return colCount;
    }

    Integer getColDataType(int i) {
        return ValuePool.getInt(getColTypeCode(i));
    }

    Integer getColLen(int i) {

        int size;
        int type;

        type = getColDataType(i).intValue();

        switch (type) {

            case BINARY :
            case LONGVARBINARY :
            case VARBINARY : {
                size = Integer.MAX_VALUE;

                break;
            }
            case BIGINT :
            case DOUBLE :
            case DATE :
            case TIME :
            case TIMESTAMP : {
                size = 8;

                break;
            }
            case FLOAT :
            case REAL :
            case INTEGER : {
                size = 4;

                break;
            }
            case SMALLINT : {
                size = 2;

                break;
            }
            case TINYINT :
            case BIT : {
                size = 1;

                break;
            }
            default :
                size = 0;
        }

        return (size == 0) ? null : ValuePool.getInt(size);
    }

    String getColName(int i) {
        return "@" + (i + colOffset());
    }

    Integer getColNullability(int i) {
        int cn;

        cn =  getColClass(i).isPrimitive()
        ? DatabaseMetaData.procedureNoNulls
        : DatabaseMetaData.procedureNullable;

        return ValuePool.getInt(cn);
    }

    String getColRemark(int i) {

        String          key;
        StringBuffer    sb;

        sb = new StringBuffer();

        key = sb.append(getSignature()).append(getColName(i)).toString();
        return BundleHandler.getString(hnd_remarks, key);
    }

    int getColTypeCode(int i) {
        i+= colOffset();
        return colTypes[i];
    }

    Integer getColUsage(int i) {
        i += colOffset();
        return i == 0
        ? ValuePool.getInt(DatabaseMetaData.procedureColumnReturn)
        : ValuePool.getInt(DatabaseMetaData.procedureColumnIn);
    }

    Class getDeclaringClass() {
        return this.clazz;
    }

    String getFQN() {

        StringBuffer sb;

        if (fqn == null) {
            sb = new StringBuffer();
            fqn = sb
            .append(clazz.getName())
            .append('.')
            .append(method.getName())
            .toString();
        }

        return fqn;
    }

    Integer getInputParmCount() {
        return ValuePool.getInt(method.getParameterTypes().length);
    }

    Method getMethod() {
        return this.method;
    }

    String getOrigin(String srcType) {
        return
        (nameSpace.isBuiltin(clazz) ? "BUILTIN " : "USER DEFINED ") + srcType;
    }

    Integer getOutputParmCount() {
        // no support for IN OUT or OUT columns yet
        return ValuePool.getInt(0);
    }

    String getRemark() {
        return BundleHandler.getString(hnd_remarks, getSignature());
    }

    Integer getResultSetCount() {
        return (method.getReturnType() == Void.TYPE)
        ? ValuePool.getInt(0)
        : ValuePool.getInt(1);
    }

    Integer getResultType(String origin) {

        int type;

        type = !"ROUTINE".equals(origin)
        ? DatabaseMetaData.procedureResultUnknown
        : method.getReturnType() == Void.TYPE
            ? DatabaseMetaData.procedureNoResult
            : DatabaseMetaData.procedureReturnsResult;


        return ValuePool.getInt(type);
    }

    String getSignature() {

        StringBuffer    sb;
        Class[]         parmTypes;
        int             len;

        if (sig == null) {

            sb          = new StringBuffer();
            parmTypes   = method.getParameterTypes();
            len         = parmTypes.length;

            try {
                sb.append(method.getName()).append('(');

                for (int i = 0; i < len; i++) {
                    sb.append(parmTypes[i].getName());
                    sb.append(',');
                }
                if (len > 0) {
                    sb.setLength(sb.length() -1);
                }
                sb.append(')');

                sig = sb.toString();
            } catch (Exception e) {
                sig = null;
            }
        }

        return sig;
    }

    DINameSpace getNameSpace() {
        return nameSpace;
    }

    void setNameSpace(DINameSpace ns) throws SQLException {

        Trace.doAssert(ns != null, "name space is null");
        nameSpace = ns;

        Class   c;
        Integer type;


        // can only speed up test significantly for java.lang.Object,
        // final classes, primitive types and hierachy parents.
        // Must still check later if assignable from candidate classes, where
        // hierarchy parent is not final.
        //ARRAY
        try {
            c = nameSpace.classForName("org.hsqldb.jdbcArray");

            typeMap.put(c, ValuePool.getInt(ARRAY));
        } catch (Exception e) {}

        // BIGINT
        type = ValuePool.getInt(BIGINT);

        typeMap.put(Long.TYPE, type);
        typeMap.put(Long.class, type);

        // BIT
        type = ValuePool.getInt(BIT);

        typeMap.put(Boolean.TYPE, type);
        typeMap.put(Boolean.class, type);

        // BLOB
        type = ValuePool.getInt(BLOB);

        try {
            c = nameSpace.classForName("org.hsqldb.jdbcBlob");

            typeMap.put(c, type);
        } catch (Exception e) {}

        // CHAR
        type = ValuePool.getInt(CHAR);

        typeMap.put(Character.TYPE, type);
        typeMap.put(Character.class, type);
        typeMap.put(Character[].class, type);
        typeMap.put(char[].class, type);

        // CLOB
        type = ValuePool.getInt(CLOB);

        try {
            c = nameSpace.classForName("org.hsqldb.jdbcClob");

            typeMap.put(c, type);
        } catch (Exception e) {}

        // DATALINK
        type = ValuePool.getInt(DATALINK);

        typeMap.put(java.net.URL.class, type);

        // DATE
        type = ValuePool.getInt(DATE);

        typeMap.put(java.util.Date.class, type);
        typeMap.put(java.sql.Date.class, type);

        // DECIMAL
        type = ValuePool.getInt(DECIMAL);

        try {
            c = nameSpace.classForName("java.math.BigDecimal");

            typeMap.put(c, type);
        } catch (Exception e) {}

        // DISTINCT
        try {
            c = nameSpace.classForName("org.hsqldb.jdbcDistinct");

            typeMap.put(c, ValuePool.getInt(DISTINCT));
        } catch (Exception e) {}

        // DOUBLE
        type = ValuePool.getInt(DOUBLE);

        typeMap.put(Double.TYPE, type);
        typeMap.put(Double.class, type);

        // FLOAT
        type = ValuePool.getInt(FLOAT);

        typeMap.put(Float.TYPE, type);
        typeMap.put(Float.class, type);

        // INTEGER
        type = ValuePool.getInt(INTEGER);

        typeMap.put(Integer.TYPE, type);
        typeMap.put(Integer.class, type);

        // JAVA_OBJECT
        type = ValuePool.getInt(JAVA_OBJECT);

        typeMap.put(Object.class, type);

        // LONGVARBINARY
        type = ValuePool.getInt(LONGVARBINARY);

        typeMap.put(byte[].class, type);

        // LONGVARCHAR
        type = ValuePool.getInt(LONGVARCHAR);

        typeMap.put(String.class, type);

        // NULL
        type = ValuePool.getInt(NULL);

        typeMap.put(Void.TYPE, type);
        typeMap.put(Void.class, type);

        // REF
        type = ValuePool.getInt(REF);

        try {
            c = nameSpace.classForName("org.hsqldb.jdbcRef");

            typeMap.put(c, type);
        } catch (Exception e) {}

        // SMALLINT
        type = ValuePool.getInt(SMALLINT);

        typeMap.put(Short.TYPE, type);
        typeMap.put(Short.class, type);

        // STRUCT
        type = ValuePool.getInt(STRUCT);

        try {
            c = nameSpace.classForName("org.hsqldb.jdbcStruct");

            typeMap.put(c, type);
        } catch (Exception e) {}

        // TIME
        type = ValuePool.getInt(TIME);

        typeMap.put(java.sql.Time.class, type);

        // TIMESTAMP
        type = ValuePool.getInt(TIMESTAMP);

        typeMap.put(java.sql.Timestamp.class, type);

        // TINYINT
        type = ValuePool.getInt(TINYINT);

        typeMap.put(Byte.TYPE, type);
        typeMap.put(Byte.class, type);

        // XML
        type = ValuePool.getInt(XML);

        try {
            c = nameSpace.classForName("org.w3c.dom.Document");

            typeMap.put(c, type);

            c = nameSpace.classForName("org.w3c.dom.DocumentFragment");

            typeMap.put(c, type);
        } catch (Exception e) {}

    }

    private void resolveCols() {

        Class       returnType;
        Class[]     parmTypes;
        Class       c;
        int         len;

        returnType      = method.getReturnType();
        parmTypes       = method.getParameterTypes();
        len             = parmTypes.length + 1;
        colClasses      = new Class[len];
        colTypes        = new int[len];
        colClasses[0]   = returnType;
        colTypes[0]     = typeForClass(returnType);

        for (int i = 1; i < len; i++) {
            c = parmTypes[i-1];
            colClasses[i] = c;
            colTypes[i] = typeForClass(c);
        }

        colOffset = 0;
        colCount =  method.getParameterTypes().length;

        if (returnType == Void.TYPE) {
            colOffset++;
        } else {
            colCount++;
        }
    }

    void setMethod(Method m) {

        String          remarkKey;

        method          = m;
        clazz           = method.getDeclaringClass();
        fqn             = null;
        sig             = null;
        csig            = null;
        colsResolved    = false;
        remarkKey       = clazz.getName().replace('.','_');
        hnd_remarks     = BundleHandler.getBundleHandle(remarkKey,null);

    }

    int typeForClass(Class c) {

        Class to;

        Integer type = (Integer) typeMap.get(c);

        if (type != null) {
            return type.intValue();
        }

        // ARRAY (dimension 1)
        // HSQLDB does not yet support ARRAY for SQL, but
        // Trigger.fire takes Object[] row, which we report.
        // Also, it's just friendly to show what "would"
        // be required if/when we support ARRAY in a broader
        // sense
        if (c.isArray() &&!c.getComponentType().isArray()) {
            return ARRAY;
        }

        try {
            to = Class.forName("java.sql.Array");

            if (to.isAssignableFrom(c)) {
                return ARRAY;
            }
        } catch (Exception e) {}

        // NUMERIC
        // All java.lang.Number impls and BigDecimal have
        // already been covered by lookup in typeMap.
        // They are all final, so this is OK.
        if (Number.class.isAssignableFrom(c)) {
            return NUMERIC;
        }

        // TIMESTAMP
        try {
            to = Class.forName("java.sql.Timestamp");

            if (to.isAssignableFrom(c)) {
                return TIMESTAMP;
            }
        } catch (Exception e) {}

        // TIME
        try {
            to = Class.forName("java.sql.Time");

            if (to.isAssignableFrom(c)) {
                return TIMESTAMP;
            }
        } catch (Exception e) {}

        // DATE
        try {
            to = Class.forName("java.sql.Date");

            if (to.isAssignableFrom(c)) {
                return DATE;
            }
        } catch (Exception e) {}

        // BLOB
        try {
            to = Class.forName("java.sql.Blob");

            if (to.isAssignableFrom(c)) {
                return BLOB;
            }
        } catch (Exception e) {}

        // CLOB
        try {
            to = Class.forName("java.sql.Clob");

            if (to.isAssignableFrom(c)) {
                return TIMESTAMP;
            }
        } catch (Exception e) {}

        // REF
        try {
            to = Class.forName("java.sql.Ref");

            if (to.isAssignableFrom(c)) {
                return REF;
            }
        } catch (Exception e) {}

        // STRUCT
        try {
            to = Class.forName("java.sql.Struct");

            if (to.isAssignableFrom(c)) {
                return STRUCT;
            }
        } catch (Exception e) {}

        // LONGVARCHAR
        try {

            // @since JDK1.4
            to = Class.forName("java.lang.CharSequence");

            if (to.isAssignableFrom(c)) {
                return LONGVARCHAR;
            }
        } catch (Exception e) {}

        // we have no standard mapping for the specified class
        // at this point...is it even storable?
        if (
            Serializable.class.isAssignableFrom(c) ||
            Externalizable.class.isAssignableFrom(c)
        ) {
            // Yes: it is storable, as an OTHER.
            return OTHER;
        }

        // No: It is not storable (by HSQLDB)...
        // but it may be possible to pass to a procedure,
        // so return the most generic type.
        return JAVA_OBJECT;
    }

}
