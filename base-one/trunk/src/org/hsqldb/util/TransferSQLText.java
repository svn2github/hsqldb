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
import java.io.*;

/**
 * Title:        Database Transfer Tool
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      INGENICO
 * @author Nicolas BAZIN
 * @version 1.7.0
 */
public class TransferSQLText extends DataAccessPoint {

    String         sFileName = null;
    BufferedWriter WText     = null;

    public TransferSQLText(String _FileName,
                           Traceable t) throws DataAccessPointException {

        super(t);

        sFileName = _FileName;

        if (sFileName == null) {
            throw new DataAccessPointException("File name not initialized");
        }

        if (WText == null) {
            try {
                WText = new BufferedWriter(new FileWriter(sFileName));
            } catch (IOException e) {
                throw new DataAccessPointException(e.getMessage());
            }
        }
    }

    boolean execute(String statement) throws DataAccessPointException {

        try {
            WText.write(statement + "\n");
            WText.flush();
        } catch (IOException e) {
            throw new DataAccessPointException(e.getMessage());
        }

        return true;
    }

    void putData(String statement, ResultSet r,
                 int iMaxRows) throws DataAccessPointException {

        int i = 0;

        if (r == null) {
            return;
        }

        try {
            while (r.next()) {
                if (i == 0) {
                    WText.write(statement + "\n");
                    WText.flush();
                }

                transferRow(r);

                if (iMaxRows != 0 && i == iMaxRows) {
                    break;
                }

                i++;

                if (iMaxRows != 0 || i % 100 == 0) {
                    tracer.trace("Transfered " + i + " rows");
                }
            }
        } catch (Exception e) {
            throw new DataAccessPointException(e.getMessage());
        } finally {
            try {
                if (i > 0) {
                    WText.write("\tNumber of Rows=" + i + "\n\n");
                    WText.flush();
                }
            } catch (IOException e) {
                throw new DataAccessPointException(e.getMessage());
            }
        }
    }

    void close() throws DataAccessPointException {

        if (WText != null) {
            try {
                WText.flush();
                WText.close();
            } catch (IOException e) {}
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
    private void transferRow(ResultSet r) throws Exception {

        String sLast = "";
        int    len   = r.getMetaData().getColumnCount();

        for (int i = 0; i < len; i++) {
            int t = r.getMetaData().getColumnType(i + 1);

            sLast = "column=" + r.getMetaData().getColumnName(i + 1)
                    + " datatype="
                    + (String) helper.getSupportedTypes().get(new Integer(t));

            Object o = r.getObject(i + 1);

            if (o == null) {
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
                        sLast += " value=\'" + o.toString() + "\'";
                        break;

                    default :

                        //java.sql.Types.NULL
                        sLast += " value=undefined";

                        WText.write(sLast);
                        WText.flush();

                        throw new DataAccessPointException(
                            "Object type unknown for:" + sLast);
                }
            }

            WText.write("\t" + sLast + "\n");
            WText.flush();
        }

        WText.write("\n");
        WText.flush();

        sLast = "";
    }
}
