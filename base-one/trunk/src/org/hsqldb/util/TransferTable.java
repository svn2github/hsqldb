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


package org.hsqldb.util;

import java.sql.*;
import java.util.*;
import java.io.Serializable;

/**
 * Transfers data from one database to another
 *
 * @version 1.7.0
 */

// fredt@users 20011220 - patch 481239 by xponsard@users - enhancements
// enhancements to support saving and loading of transfer settings,
// transfer of blobs, and catalog and schema names in source db
// changes by fredt to allow saving and loading of transfer settings
// fredt@users 20020215 - patch 516309 by Nicolas Bazin - enhancements
// sqlbob@users 20020325 - patch 1.7.0 - reengineering
public class TransferTable implements Serializable {

    Hashtable       hTypes;
    TransferDb      sourceDb;
    DataAccessPoint destDb;
    String          sSchema, sType;
    String          sDatabaseToConvert;
    String          sSourceTable, sDestTable;
    String          sDestDrop, sDestCreate, sDestInsert, sDestDelete;
    String sDestDropIndex, sDestCreateIndex, sDestAlter, sSourceSelect;
    boolean         bTransfer    = true;
    boolean         bCreate      = true;
    boolean         bDelete      = true;
    boolean         bDrop        = true;
    boolean         bCreateIndex = true;
    boolean         bDropIndex   = true;
    boolean         bInsert      = true;
    boolean         bAlter       = true;
    boolean         bFKForced    = false;
    boolean         bIdxForced   = false;
    Traceable       tracer;

    public TransferTable(TransferDb src, String name, String schema,
                         String type, Traceable t) {

        sourceDb = src;
        sSchema  = "";

        if (schema != null && schema.length() > 0) {
            sSchema = schema;
        }

        sType              = type;
        sDatabaseToConvert = src.databaseToConvert;
        sSourceTable       = sDestTable = name;
        tracer             = t;

        if (sType.compareTo("TABLE") == 0) {
            sSourceSelect = "SELECT * FROM "
                            + src.helper.formatName(sSourceTable) + ";";
        } else if (sType.compareTo("VIEW") == 0) {
            sSourceSelect = "";
        }
    }

    void setDest(String _Schema, DataAccessPoint dest) throws Exception {

        destDb = dest;

        dest.helper.setSchema(_Schema);

        sDestDrop = "DROP " + sType + " "
                    + dest.helper.formatName(sDestTable) + ";";

        if (sType.compareTo("TABLE") == 0) {
            sDestDelete = "DELETE FROM " + dest.helper.formatName(sDestTable)
                          + ";";
        } else if (sType.compareTo("VIEW") == 0) {
            bDelete     = false;
            sDestDelete = "";
        }

        sDestCreateIndex = "";
        sDestDropIndex   = "";
        sDestAlter       = "";

        initTypes();
    }

    void getTableStructure() throws Exception {

        String create = "CREATE " + sType + " "
                        + destDb.helper.formatName(sDestTable);
        String    insert         = "";
        ResultSet ImportedKeys   = null;
        boolean   importedkeys   = false;
        String    alterCreate    = new String("");
        String    alterDrop      = new String("");
        String    ConstraintName = new String("");
        String    RefTableName   = new String("");
        String    foreignKeyName = new String("");
        String    columnName     = new String("");

        if (sType.compareTo("TABLE") == 0) {
            create += "(";
        } else if (sType.compareTo("VIEW") == 0) {
            create += " AS SELECT ";
        }

        if (sType.compareTo("TABLE") == 0) {
            insert = "INSERT INTO " + destDb.helper.formatName(sDestTable)
                     + " VALUES(";
        } else if (sType.compareTo("VIEW") == 0) {
            bInsert = false;
            insert  = "";
        }

        if (sType.compareTo("VIEW") == 0) {
            /*
            ** Don't know how to retrieve the underlying select so we leave here.
            ** The user will have to edit the rest of the create statement.
            */
            bTransfer    = false;
            bCreate      = true;
            bDelete      = false;
            bDrop        = true;
            bCreateIndex = false;
            bDropIndex   = false;
            bInsert      = false;
            bAlter       = false;

            return;
        }

        ImportedKeys = null;

        try {
            ImportedKeys = sourceDb.meta.getImportedKeys(sDatabaseToConvert,
                    sSchema, sSourceTable);
        } catch (SQLException e) {
            ImportedKeys = null;
        }

        if (ImportedKeys != null) {
            while (ImportedKeys.next()) {
                importedkeys = true;

                if (!ImportedKeys.getString(12).equals(ConstraintName)) {
                    if (!ConstraintName.equals("")) {
                        alterCreate +=
                            destDb.helper.formatIdentifier(columnName.substring(0, columnName.length() - 1))
                            + ") REFERENCES "
                            + destDb.helper.formatName(RefTableName);

                        if (foreignKeyName.length() > 0) {
                            alterCreate +=
                                " ("
                                + destDb.helper.formatIdentifier(
                                    foreignKeyName.substring(
                                        0, foreignKeyName.length()
                                        - 1)) + ")";
                        }

                        alterCreate += ";";
                        alterDrop =
                            alterDrop.substring(0, alterDrop.length() - 1)
                            + ";";
                        foreignKeyName = "";
                        columnName     = "";
                    }

                    RefTableName   = ImportedKeys.getString(3);
                    ConstraintName = ImportedKeys.getString(12);
                    alterCreate += "ALTER TABLE "
                                   + destDb.helper.formatName(sDestTable)
                                   + " ADD CONSTRAINT ";

                    if ((bFKForced) && (!ConstraintName.startsWith("FK_"))) {
                        alterCreate +=
                            destDb.helper.formatIdentifier(
                                "FK_" + ConstraintName) + " ";
                    } else {
                        alterCreate +=
                            destDb.helper.formatIdentifier(ConstraintName)
                            + " ";
                    }

                    alterCreate += "FOREIGN KEY (";
                    alterDrop += "ALTER TABLE "
                                 + destDb.helper.formatName(sDestTable)
                                 + " DROP CONSTRAINT ";

                    if ((bFKForced) && (!ConstraintName.startsWith("FK_"))) {
                        alterDrop +=
                            destDb.helper.formatIdentifier(
                                "FK_" + ConstraintName) + " ";
                    } else {
                        alterDrop +=
                            destDb.helper.formatIdentifier(ConstraintName)
                            + " ";
                    }
                }

                columnName     += ImportedKeys.getString(8) + ",";
                foreignKeyName += ImportedKeys.getString(4) + ",";
            }

            ImportedKeys.close();
        }

        if (importedkeys) {
            alterCreate += columnName.substring(0, columnName.length() - 1)
                           + ") REFERENCES "
                           + destDb.helper.formatName(RefTableName);

            if (foreignKeyName.length() > 0) {
                alterCreate +=
                    " ("
                    + destDb.helper.formatIdentifier(
                        foreignKeyName.substring(
                            0, foreignKeyName.length() - 1)) + ")";
            }

            alterCreate += ";";
            alterDrop = alterDrop.substring(0, alterDrop.length() - 1) + ";";
            sDestDrop   = alterDrop + sDestDrop;
        }

        boolean primarykeys           = false;
        String  PrimaryKeysConstraint = new String();

        PrimaryKeysConstraint = "";

        ResultSet PrimaryKeys = null;

        try {
            PrimaryKeys = sourceDb.meta.getPrimaryKeys(sDatabaseToConvert,
                    sSchema, sSourceTable);
        } catch (SQLException e) {
            PrimaryKeys = null;
        }

        if (PrimaryKeys != null) {
            while (PrimaryKeys.next()) {
                if (primarykeys) {
                    PrimaryKeysConstraint += ", ";
                } else {
                    if (PrimaryKeys.getString(6) != null) {
                        PrimaryKeysConstraint =
                            " CONSTRAINT "
                            + destDb.helper.formatIdentifier(
                                PrimaryKeys.getString(6));
                    }

                    PrimaryKeysConstraint += " PRIMARY KEY (";
                }

                PrimaryKeysConstraint +=
                    destDb.helper.formatIdentifier(PrimaryKeys.getString(4));
                primarykeys = true;
            }

            PrimaryKeys.close();

            if (primarykeys) {
                PrimaryKeysConstraint += ") ";
            }
        }

        boolean   indices     = false;
        ResultSet Indices     = null;
        String    IndiceName  = new String("");
        String    CreateIndex = new String("");
        String    DropIndex   = new String("");

        try {
            Indices = sourceDb.meta.getIndexInfo(sDatabaseToConvert, sSchema,
                                                 sSourceTable, false, false);
        } catch (SQLException e) {
            Indices = null;
        }

        if (Indices != null) {
            while (Indices.next()) {
                String tmpIndexName = null;

                try {
                    tmpIndexName = Indices.getString(6);
                } catch (SQLException e) {
                    tmpIndexName = null;
                }

                if (tmpIndexName == null) {
                    continue;
                }

                if (!tmpIndexName.equals(IndiceName)) {
                    if (!IndiceName.equals("")) {
                        CreateIndex =
                            CreateIndex.substring(0, CreateIndex.length() - 1)
                            + ");";
                        DropIndex += ";";
                    }

                    IndiceName = tmpIndexName;
                    DropIndex  += "DROP INDEX ";

                    if ((bIdxForced) && (!IndiceName.startsWith("Idx_"))) {
                        DropIndex += destDb.helper.formatIdentifier("Idx_"
                                + IndiceName);
                    } else {
                        DropIndex +=
                            destDb.helper.formatIdentifier(IndiceName);
                    }

                    CreateIndex += "CREATE ";

                    if (!Indices.getBoolean(4)) {
                        CreateIndex += "UNIQUE ";
                    }

                    CreateIndex += "INDEX ";

                    if ((bIdxForced) && (!IndiceName.startsWith("Idx_"))) {
                        CreateIndex += destDb.helper.formatIdentifier("Idx_"
                                + IndiceName);
                    } else {
                        CreateIndex +=
                            destDb.helper.formatIdentifier(IndiceName);
                    }

                    CreateIndex += " ON "
                                   + destDb.helper.formatName(sDestTable)
                                   + "(";
                }

                CreateIndex +=
                    destDb.helper.formatIdentifier(Indices.getString(9))
                    + ",";
                indices = true;
            }

            Indices.close();

            if (indices) {
                CreateIndex =
                    CreateIndex.substring(0, CreateIndex.length() - 1) + ");";
                DropIndex += ";";
            }
        }

        Vector v = new Vector();

        tracer.trace("Reading source columns for table " + sSourceTable);

        ResultSet         col            = null;
        int               colnum         = 1;
        Statement         stmt           = sourceDb.conn.createStatement();
        ResultSet         select_rs      = stmt.executeQuery(sSourceSelect);
        ResultSetMetaData select_rsmdata = select_rs.getMetaData();
        int               maxcol         = select_rsmdata.getColumnCount();

        try {
            col = sourceDb.meta.getColumns(sDatabaseToConvert, sSchema,
                                           sSourceTable, null);
        } catch (SQLException eSchema) {

            // fredt - second try with null schema
            if (sSchema.equals("")) {
                col = sourceDb.meta.getColumns(sDatabaseToConvert, null,
                                               sSourceTable, null);
            }
        }

        while (col.next()) {
            String name = destDb.helper.formatIdentifier(col.getString(4));
            int    type        = col.getShort(5);
            String source      = col.getString(6);
            int    column_size = col.getShort(7);
            String DefaultVal  = col.getString(13);
            boolean rsmdata_NoNulls =
                (select_rsmdata.isNullable(colnum)
                 == java.sql.DatabaseMetaData.columnNoNulls);
            boolean rsmdata_isAutoIncrement = false;

            try {
                rsmdata_isAutoIncrement =
                    select_rsmdata.isAutoIncrement(colnum);
            } catch (SQLException e) {
                rsmdata_isAutoIncrement = false;
            }

            int rsmdata_precision = select_rsmdata.getPrecision(colnum);
            int rsmdata_scale     = select_rsmdata.getScale(colnum);

            type = sourceDb.helper.convertFromType(type);
            type = destDb.helper.convertToType(type);

            Integer inttype  = new Integer(type);
            String  datatype = (String) hTypes.get(inttype);

            if (datatype == null) {
                datatype = source;

                tracer.trace("No mapping for type: " + name + " type: "
                             + type + " source: " + source);
            }

            if (type == Types.NUMERIC) {
                datatype += "(" + Integer.toString(rsmdata_precision);

                if (rsmdata_scale > 0) {
                    datatype += "," + Integer.toString(rsmdata_scale);
                }

                datatype += ")";
            } else if (type == Types.CHAR) {
                datatype += "(" + Integer.toString(column_size) + ")";
            } else if (rsmdata_isAutoIncrement) {
                datatype = "SERIAL";
            }

            if (rsmdata_NoNulls) {
                datatype += " NOT NULL ";
            }

            if (DefaultVal != null) {
                if ((type == Types.CHAR) || (type == Types.LONGVARCHAR)) {
                    DefaultVal = "\'" + DefaultVal + "\'";
                }

                datatype += " Default " + DefaultVal;
            }

            v.addElement(inttype);

            datatype = sourceDb.helper.fixupColumnDefRead(this,
                    select_rsmdata, datatype, col, colnum);
            datatype = destDb.helper.fixupColumnDefWrite(this,
                    select_rsmdata, datatype, col, colnum);
            create += name + " " + datatype + ",";
            insert += "?,";

            colnum++;
        }

        select_rs.close();
        stmt.close();
        col.close();

        if (primarykeys) {
            create += PrimaryKeysConstraint + ",";
        }

        sDestCreate = create.substring(0, create.length() - 1) + ")";
        sDestInsert = insert.substring(0, insert.length() - 1) + ")";

        if (importedkeys) {
            bAlter     = true;
            sDestAlter = alterCreate;
        } else {
            bAlter = false;
        }

        if (indices) {
            bCreateIndex     = true;
            bDropIndex       = true;
            sDestCreateIndex = CreateIndex;
            sDestDropIndex   = DropIndex;
        } else {
            bCreateIndex = false;
            bDropIndex   = false;
        }

        //iColumnType = new int[v.size()];
        //for (int j = 0; j < v.size(); j++) {
        //    iColumnType[j] = ((Integer) v.elementAt(j)).intValue();
        //}
    }

    /**
     * Method declaration
     *
     *
     * @param t
     *
     * @throws SQLException
     */
    void transferStructure() throws Exception {

        String Statement = new String("");

        if (destDb.helper.needTransferTransaction()) {
            try {
                destDb.setAutoCommit(false);
            } catch (Exception e) {}
        }

        if (bTransfer == false) {
            tracer.trace("Table " + sSourceTable + " not transfered");

            return;
        }

        tracer.trace("Table " + sSourceTable + ": start transfer");

        try {
            if (bDropIndex) {
                if (sDestDropIndex.charAt(sDestDropIndex.length() - 1)
                        != ';') {
                    sDestDropIndex += ";";
                }

                int lastsemicolon = 0;
                int nextsemicolon = sDestDropIndex.indexOf(';');

                while (nextsemicolon > lastsemicolon) {
                    Statement = sDestDropIndex.substring(lastsemicolon,
                                                         nextsemicolon);

                    if (Statement.charAt(Statement.length() - 1) != ';') {
                        Statement += ";";
                    }

                    try {
                        tracer.trace("Executing " + Statement);
                        destDb.execute(Statement);
                    } catch (Exception e) {
                        tracer.trace("Ignoring error " + e.getMessage());
                    }

                    lastsemicolon = nextsemicolon + 1;
                    nextsemicolon = lastsemicolon
                                    + sDestDropIndex.substring(
                                        lastsemicolon).indexOf(';');
                }
            }

            if (bDelete) {
                if (sDestDelete.charAt(sDestDelete.length() - 1) != ';') {
                    sDestDelete += ";";
                }

                int lastsemicolon = 0;
                int nextsemicolon = sDestDelete.indexOf(';');

                while (nextsemicolon > lastsemicolon) {
                    Statement = sDestDelete.substring(lastsemicolon,
                                                      nextsemicolon);

                    if (Statement.charAt(Statement.length() - 1) != ';') {
                        Statement += ";";
                    }

                    try {
                        tracer.trace("Executing " + Statement);
                        destDb.execute(Statement);
                    } catch (Exception e) {
                        tracer.trace("Ignoring error " + e.getMessage());
                    }

                    lastsemicolon = nextsemicolon + 1;
                    nextsemicolon =
                        lastsemicolon
                        + sDestDelete.substring(lastsemicolon).indexOf(';');
                }

                if (sDestDrop.charAt(sDestDrop.length() - 1) != ';') {
                    sDestDrop += ";";
                }

                lastsemicolon = 0;
                nextsemicolon = sDestDrop.indexOf(';');

                while (nextsemicolon > lastsemicolon) {
                    Statement = sDestDrop.substring(lastsemicolon,
                                                    nextsemicolon);

                    if (Statement.charAt(Statement.length() - 1) != ';') {
                        Statement += ";";
                    }

                    try {
                        tracer.trace("Executing " + Statement);
                        destDb.execute(Statement);
                    } catch (Exception e) {
                        tracer.trace("Ignoring error " + e.getMessage());
                    }

                    lastsemicolon = nextsemicolon + 1;
                    nextsemicolon =
                        lastsemicolon
                        + sDestDrop.substring(lastsemicolon).indexOf(';');
                }
            }

            if (bCreate) {
                if (sDestCreate.charAt(sDestCreate.length() - 1) != ';') {
                    sDestCreate += ";";
                }

                int lastsemicolon = 0;
                int nextsemicolon = sDestCreate.indexOf(';');

                while (nextsemicolon > lastsemicolon) {
                    Statement = sDestCreate.substring(lastsemicolon,
                                                      nextsemicolon);

                    if (Statement.charAt(Statement.length() - 1) != ';') {
                        Statement += ";";
                    }

                    tracer.trace("Executing " + Statement);
                    destDb.execute(Statement);

                    lastsemicolon = nextsemicolon + 1;
                    nextsemicolon =
                        lastsemicolon
                        + sDestCreate.substring(lastsemicolon).indexOf(';');
                }
            }
        } catch (Exception e) {
            try {
                if (!destDb.getAutoCommit()) {
                    destDb.rollback();
                }
            } catch (Exception e1) {}

            throw (e);
        }

        if (!destDb.getAutoCommit()) {
            destDb.commit();

            try {
                destDb.setAutoCommit(true);
            } catch (Exception e) {}
        }
    }

    void transferData(int iMaxRows) throws Exception, SQLException {

        if (destDb.helper.needTransferTransaction()) {
            try {
                destDb.setAutoCommit(false);
            } catch (Exception e) {}
        }

        try {
            if (bInsert) {
                if (destDb.helper.needTransferTransaction()) {
                    try {
                        destDb.setAutoCommit(false);
                    } catch (Exception e) {}
                }

                tracer.trace("Executing " + sSourceSelect);

                ResultSet r = sourceDb.getData(sSourceSelect);

                tracer.trace("Start transfering data...");
                destDb.beginDataTransfer();
                tracer.trace("Executing " + sDestInsert);
                destDb.putData(sDestInsert, r, iMaxRows);
                destDb.endDataTransfer();
                tracer.trace("Finished");

                if (!destDb.getAutoCommit()) {
                    destDb.commit();

                    try {
                        destDb.setAutoCommit(true);
                    } catch (Exception e) {}
                }
            }
        } catch (Exception e) {
            try {
                if (!destDb.getAutoCommit()) {
                    destDb.rollback();
                }
            } catch (Exception e1) {}

            throw (e);
        }

        if (!destDb.getAutoCommit()) {
            destDb.commit();

            try {
                destDb.setAutoCommit(true);
            } catch (Exception e) {}
        }
    }

    /**
     * Method declaration
     *
     *
     * @param t
     *
     * @throws SQLException
     */
    void transferAlter() throws Exception {

        String Statement = new String("");

        if (destDb.helper.needTransferTransaction()) {
            try {
                destDb.setAutoCommit(false);
            } catch (Exception e) {}
        }

        if (bTransfer == false) {
            tracer.trace("Table " + sSourceTable + " not transfered");

            return;
        }

        tracer.trace("Table " + sSourceTable + ": start alter");

        try {
            if (bCreateIndex) {
                if (sDestCreateIndex.charAt(sDestCreateIndex.length() - 1)
                        != ';') {
                    sDestCreateIndex += ";";
                }

                int lastsemicolon = 0;
                int nextsemicolon = sDestCreateIndex.indexOf(';');

                while (nextsemicolon > lastsemicolon) {
                    Statement = sDestCreateIndex.substring(lastsemicolon,
                                                           nextsemicolon);

                    if (Statement.charAt(Statement.length() - 1) != ';') {
                        Statement += ";";
                    }

                    try {
                        tracer.trace("Executing " + sDestCreateIndex);
                        destDb.execute(Statement);
                    } catch (Exception e) {
                        tracer.trace("Ignoring error " + e.getMessage());
                    }

                    lastsemicolon = nextsemicolon + 1;
                    nextsemicolon = lastsemicolon
                                    + sDestCreateIndex.substring(
                                        lastsemicolon).indexOf(';');
                }
            }

            if (bAlter) {
                if (sDestAlter.charAt(sDestAlter.length() - 1) != ';') {
                    sDestAlter += ";";
                }

                int lastsemicolon = 0;
                int nextsemicolon = sDestAlter.indexOf(';');

                while (nextsemicolon > lastsemicolon) {
                    Statement = sDestAlter.substring(lastsemicolon,
                                                     nextsemicolon);

                    if (Statement.charAt(Statement.length() - 1) != ';') {
                        Statement += ";";
                    }

                    try {
                        tracer.trace("Executing " + Statement);
                        destDb.execute(Statement);
                    } catch (Exception e) {
                        tracer.trace("Ignoring error " + e.getMessage());
                    }

                    lastsemicolon = nextsemicolon + 1;
                    nextsemicolon =
                        lastsemicolon
                        + sDestAlter.substring(lastsemicolon).indexOf(';');
                }
            }
        } catch (Exception e) {
            try {
                if (!destDb.getAutoCommit()) {
                    destDb.rollback();
                }
            } catch (Exception e1) {}

            throw (e);
        }

        if (!destDb.getAutoCommit()) {
            destDb.commit();

            try {
                destDb.setAutoCommit(true);
            } catch (Exception e) {}
        }
    }

    private void initTypes() throws SQLException {

        if (hTypes != null) {
            return;
        }

        hTypes = destDb.helper.getSupportedTypes();
    }
}
