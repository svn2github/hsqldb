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


package org.hsqldb.util;

import java.sql.*;
import java.util.*;

/**
 * Base class for conversion from a different databases
 *
 * @author sqlbob@users
 * @version 1.7.0
 */
public class TransferHelper {

    protected TransferDb db;
    protected Traceable  tracer;
    protected String     sSchema;
    private String       quote;

    public TransferHelper() {

        db     = null;
        tracer = null;
        quote  = "\'";
    }

    public TransferHelper(TransferDb database, Traceable t, String q) {

        db     = database;
        tracer = t;
        quote  = q;
    }

    public void set(TransferDb database, Traceable t, String q) {

        db     = database;
        tracer = t;
        quote  = q;
    }

    String formatIdentifier(String id) {

        if (id == null) {
            return id;
        }

        if (id.equals("")) {
            return id;
        }

        if (!Character.isLetter(id.charAt(0)) || (id.indexOf(' ') != -1)) {
            return (quote + id + quote);
        }

        return id;
    }

    void setSchema(String _Schema) {
        sSchema = _Schema;
    }

    String formatName(String t) {

        String Name = "";

        if ((sSchema != null) && (sSchema.length() > 0)) {
            Name = sSchema + ".";
        }

        Name += formatIdentifier(t);

        return Name;
    }

    int convertFromType(int type) {
        return (type);
    }

    int convertToType(int type) {
        return (type);
    }

    Hashtable getSupportedTypes() {

        Hashtable JDBCtypes = new Hashtable();

//#ifdef JAVA2
        JDBCtypes.put(new Integer(java.sql.Types.ARRAY), "ARRAY");
        JDBCtypes.put(new Integer(java.sql.Types.BLOB), "BLOB");
        JDBCtypes.put(new Integer(java.sql.Types.CLOB), "CLOB");
        JDBCtypes.put(new Integer(java.sql.Types.DISTINCT), "DISTINCT");
        JDBCtypes.put(new Integer(java.sql.Types.JAVA_OBJECT), "JAVA_OBJECT");
        JDBCtypes.put(new Integer(java.sql.Types.REF), "REF");
        JDBCtypes.put(new Integer(java.sql.Types.STRUCT), "STRUCT");

//#endif JAVA2
        JDBCtypes.put(new Integer(java.sql.Types.BIGINT), "BIGINT");
        JDBCtypes.put(new Integer(java.sql.Types.BINARY), "BINARY");
        JDBCtypes.put(new Integer(java.sql.Types.BIT), "BIT");
        JDBCtypes.put(new Integer(java.sql.Types.CHAR), "CHAR");
        JDBCtypes.put(new Integer(java.sql.Types.DATE), "DATE");
        JDBCtypes.put(new Integer(java.sql.Types.DECIMAL), "DECIMAL");
        JDBCtypes.put(new Integer(java.sql.Types.DOUBLE), "DOUBLE");
        JDBCtypes.put(new Integer(java.sql.Types.FLOAT), "FLOAT");
        JDBCtypes.put(new Integer(java.sql.Types.INTEGER), "INTEGER");
        JDBCtypes.put(new Integer(java.sql.Types.LONGVARBINARY),
                      "LONGVARBINARY");
        JDBCtypes.put(new Integer(java.sql.Types.LONGVARCHAR), "LONGVARCHAR");
        JDBCtypes.put(new Integer(java.sql.Types.NULL), "NULL");
        JDBCtypes.put(new Integer(java.sql.Types.NUMERIC), "NUMERIC");
        JDBCtypes.put(new Integer(java.sql.Types.OTHER), "OTHER");
        JDBCtypes.put(new Integer(java.sql.Types.REAL), "REAL");
        JDBCtypes.put(new Integer(java.sql.Types.SMALLINT), "SMALLINT");
        JDBCtypes.put(new Integer(java.sql.Types.TIME), "TIME");
        JDBCtypes.put(new Integer(java.sql.Types.TIMESTAMP), "TIMESTAMP");
        JDBCtypes.put(new Integer(java.sql.Types.TINYINT), "TINYINT");
        JDBCtypes.put(new Integer(java.sql.Types.VARBINARY), "VARBINARY");
        JDBCtypes.put(new Integer(java.sql.Types.VARCHAR), "VARCHAR");

        Hashtable hTypes = new Hashtable();

        if (db != null) {
            try {
                ResultSet result = db.meta.getTypeInfo();

                while (result.next()) {
                    Integer intobj = new Integer(result.getShort(2));

                    if (hTypes.get(intobj) == null) {
                        hTypes.put(intobj, JDBCtypes.get(intobj));
                    }
                }

                result.close();
            } catch (SQLException e) {}
        } else {
            hTypes = JDBCtypes;
        }

        return hTypes;
    }

    String fixupColumnDefRead(TransferTable t, ResultSetMetaData meta,
                              String columnType, ResultSet columnDesc,
                              int columnIndex) throws SQLException {
        return (columnType);
    }

    String fixupColumnDefWrite(TransferTable t, ResultSetMetaData meta,
                               String columnType, ResultSet columnDesc,
                               int columnIndex) throws SQLException {
        return (columnType);
    }

    boolean needTransferTransaction() {
        return (false);
    }

    Object convertColumnValue(Object value, int column, int type) {
        return (value);
    }

    void beginDataTransfer() {}

    void endDataTransfer() {}
}
