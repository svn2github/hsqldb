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
import java.io.*;

/**
 * Conversions between different databases
 *
 * @version 1.7.0
 */

// fredt@users 20020215 - patch 516309 by Nicolas Bazin - enhancements
// sqlbob@users 20020401 - patch 1.7.0 - reengineering
// nicolas BAZIN 20020430 - add support of Catalog and mckoi db helper
public class TransferDb extends DataAccessPoint {

    Connection          conn;
    DatabaseMetaData    meta;
    String              databaseToConvert;
    protected Statement srcStatement = null;

    public TransferDb(Connection c,
                      Traceable t) throws DataAccessPointException {

        super(t);

        conn = c;

        if (c != null) {
            String productLowerName;

            try {
                meta              = c.getMetaData();
                databaseToConvert = c.getCatalog();
                productLowerName  = meta.getDatabaseProductName();

                if (productLowerName == null) {
                    productLowerName = "";
                } else {
                    productLowerName = productLowerName.toLowerCase();
                }

                helper = HelperFactory.getHelper(productLowerName);

                helper.set(this, t, meta.getIdentifierQuoteString());
            } catch (SQLException e) {
                throw new DataAccessPointException(e.getMessage());
            }
        }
    }

    boolean isConnected() {
        return (conn != null);
    }

    boolean getAutoCommit() throws DataAccessPointException {

        boolean result = false;

        try {
            result = conn.getAutoCommit();
        } catch (SQLException e) {
            throw new DataAccessPointException(e.getMessage());
        }

        return result;
    }

    void commit() throws DataAccessPointException {

        if (srcStatement != null) {
            try {
                srcStatement.close();
            } catch (SQLException e) {}

            srcStatement = null;
        }

        try {
            conn.commit();
        } catch (SQLException e) {
            throw new DataAccessPointException(e.getMessage());
        }
    }

    void rollback() throws DataAccessPointException {

        if (srcStatement != null) {
            try {
                srcStatement.close();
            } catch (SQLException e) {}

            srcStatement = null;
        }

        try {
            conn.rollback();
        } catch (SQLException e) {
            throw new DataAccessPointException(e.getMessage());
        }
    }

    void setAutoCommit(boolean flag) throws DataAccessPointException {

        try {
            conn.setAutoCommit(flag);
        } catch (SQLException e) {
            throw new DataAccessPointException(e.getMessage());
        }
    }

    boolean execute(String statement) throws DataAccessPointException {

        boolean   result = false;
        Statement stmt   = null;

        try {
            stmt   = conn.createStatement();
            result = stmt.execute(statement);
        } catch (SQLException e) {
            throw new DataAccessPointException(e.getMessage());
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {}
            }
        }

        return result;
    }

    ResultSet getData(String statement) throws DataAccessPointException {

        ResultSet rsData = null;

        try {
            if (srcStatement != null) {
                srcStatement.close();
            }

            srcStatement = conn.createStatement();
            rsData       = srcStatement.executeQuery(statement);
        } catch (SQLException e) {
            try {
                srcStatement.close();
            } catch (Exception e1) {}

            srcStatement = null;
            rsData       = null;

            throw new DataAccessPointException(e.getMessage());
        }

        return rsData;
    }

    void putData(String statement, ResultSet r,
                 int iMaxRows) throws DataAccessPointException {

        if ((statement == null) || statement.equals("") || (r == null)) {
            return;
        }

        PreparedStatement destPrep = null;

        try {
            destPrep = conn.prepareStatement(statement);

            int i = 0;

            while (r.next()) {
                transferRow(r, destPrep);

                if (iMaxRows != 0 && i == iMaxRows) {
                    break;
                }

                i++;

                if (iMaxRows != 0 || i % 100 == 0) {
                    tracer.trace("Transfered " + i + " rows");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessPointException(e.getMessage());
        } finally {
            if (destPrep != null) {
                try {
                    destPrep.close();
                } catch (SQLException e) {}
            }
        }
    }

    /**
     * Method declaration
     *
     *
     * @param type
     * @param r
     * @param p
     *
     * @throws SQLException
     */
    private void transferRow(ResultSet r,
                             PreparedStatement p)
                             throws DataAccessPointException, SQLException {

        String sLast = "";

        if (p != null) {
            p.clearParameters();
        }

        int len = r.getMetaData().getColumnCount();

        for (int i = 0; i < len; i++) {
            int t = r.getMetaData().getColumnType(i + 1);

            sLast = "column=" + r.getMetaData().getColumnName(i + 1)
                    + " datatype="
                    + (String) helper.getSupportedTypes().get(new Integer(t));

            Object o = r.getObject(i + 1);

            if (o == null) {
                if (p != null) {
                    p.setNull(i + 1, t);
                }

                sLast += " value=<null>";
            } else {
                o = helper.convertColumnValue(o, i + 1, t);

                switch (t) {

//#ifdef JAVA2
                    case java.sql.Types.ARRAY :
                    case java.sql.Types.BLOB :
                    case java.sql.Types.CLOB :
                    case java.sql.Types.DISTINCT :
                    case java.sql.Types.JAVA_OBJECT :
                    case java.sql.Types.REF :
                    case java.sql.Types.STRUCT :

//#endif JAVA2
                    case java.sql.Types.BINARY :
                    case java.sql.Types.BIT :
                    case java.sql.Types.LONGVARBINARY :
                    case java.sql.Types.OTHER :
                    case java.sql.Types.VARBINARY :
                        p.setBytes(i + 1, r.getBytes(i + 1));

                        InputStream  Inpstr = r.getAsciiStream(i + 1);
                        StringBuffer str    = new StringBuffer();

                        try {
                            while (Inpstr.available() > 0) {
                                str.append("\\x");
                                str.append(
                                    Integer.toHexString(Inpstr.read()));
                            }
                        } catch (Exception e) {}

                        sLast += " value=\'" + str.toString() + "\'";
                        break;

                    case java.sql.Types.BIGINT :
                    case java.sql.Types.CHAR :
                    case java.sql.Types.DATE :
                    case java.sql.Types.DECIMAL :
                    case java.sql.Types.DOUBLE :
                    case java.sql.Types.FLOAT :
                    case java.sql.Types.INTEGER :
                    case java.sql.Types.LONGVARCHAR :
                    case java.sql.Types.NUMERIC :
                    case java.sql.Types.REAL :
                    case java.sql.Types.SMALLINT :
                    case java.sql.Types.TIME :
                    case java.sql.Types.TIMESTAMP :
                    case java.sql.Types.TINYINT :
                    case java.sql.Types.VARCHAR :
                        p.setObject(i + 1, o);

                        sLast += " value=\'" + o.toString() + "\'";
                        break;

                    default :

                        //java.sql.Types.NULL
                        sLast += " value=undefined";

                        throw new DataAccessPointException(
                            "Object type unknown for:" + sLast);
                }
            }
        }

        if (p != null) {
            p.execute();
        }

        sLast = "";
    }

    Vector getSchemas() throws DataAccessPointException {

        Vector    ret    = new Vector();
        ResultSet result = null;

        try {
            result = meta.getSchemas();
        } catch (SQLException e) {
            result = null;
        }

        try {
            if (result != null) {
                while (result.next()) {
                    ret.addElement(result.getString(1));
                }

                result.close();
            }
        } catch (SQLException e) {
            throw new DataAccessPointException(e.getMessage());
        }

        return (ret);
    }

    Vector getCatalog() throws DataAccessPointException {

        Vector    ret    = new Vector();
        ResultSet result = null;

        if (databaseToConvert != null && databaseToConvert.length() > 0) {
            ret.addElement(databaseToConvert);

            return (ret);
        }

        try {
            result = meta.getCatalogs();
        } catch (SQLException e) {
            result = null;
        }

        try {
            if (result != null) {
                while (result.next()) {
                    ret.addElement(result.getString(1));
                }

                result.close();
            }
        } catch (SQLException e) {
            throw new DataAccessPointException(e.getMessage());
        }

        return (ret);
    }

    void setCatalog(String sCatalog) throws DataAccessPointException {

        if (sCatalog != null && sCatalog.length() > 0) {
            try {
                conn.setCatalog(sCatalog);
            } catch (SQLException e) {
                throw new DataAccessPointException(e.getMessage());
            }
        }
    }

    Vector getTables(String sCatalog,
                     String sSchemas[]) throws DataAccessPointException {

        Vector    tTable = new Vector();
        ResultSet result = null;

        tracer.trace("Reading source tables");

        int nbloops = 1;

        if (sSchemas != null) {
            nbloops = sSchemas.length;
        }

        try {

// variations return null or emtyp result sets with informix JDBC driver 2.2
            for (int SchemaIdx = 0; SchemaIdx < nbloops; SchemaIdx++) {
                if (sSchemas != null && sSchemas[SchemaIdx] != null) {
                    result = meta.getTables(sCatalog, sSchemas[SchemaIdx],
                                            null, null);
                } else {
                    try {
                        result = meta.getTables(sCatalog, "", null, null);
                    } catch (SQLException e) {
                        result = meta.getTables(sCatalog, null, null, null);
                    }
                }

                while (result.next()) {
                    String name   = result.getString(3);
                    String type   = result.getString(4);
                    String schema = "";

                    if (sSchemas != null && sSchemas[SchemaIdx] != null) {
                        schema = sSchemas[SchemaIdx];
                    }

                    /*
                    ** we ignore the following table types:
                    **    "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY"
                    **    "ALIAS", "SYNONYM"
                    */
                    if ((type.compareTo("TABLE") == 0)
                            || (type.compareTo("VIEW") == 0)) {
                        TransferTable t = new TransferTable(this, name,
                                                            schema, type,
                                                            tracer);

                        tTable.addElement(t);
                    } else {
                        tracer.trace("Found table of type :" + type
                                     + " - this type is ignored");
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessPointException(e.getMessage());
        } finally {
            if (result != null) {
                try {
                    result.close();
                } catch (SQLException e) {}
            }
        }

        return (tTable);
    }

    void close() throws DataAccessPointException {

        if (srcStatement != null) {
            try {
                srcStatement.close();
            } catch (SQLException e) {}

            srcStatement = null;
        }

        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {}

            conn = null;
        }
    }
}
