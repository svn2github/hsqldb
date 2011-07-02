/* Copyright (c) 2001-2011, The HSQL Development Group
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
package org.hsqldb.jdbc;

import org.hsqldb.Result;

/**
 * Utility for accessing extended statement information.
 *
 * @author boucherb@users
 */
public class StatementUtils {

    /**
     * Construction Disabled.
     */
    private StatementUtils() {
        throw new RuntimeException("Construction Disabled.");
    }

    /**
     * Retrieves the default result set type for the given statement object.
     *
     * @param stmt the statement object from which to retrieve the type code
     * @return the default result set type
     * @exception NullPointerException if the given statement object
     *      reference is <tt>null</tt>.
     */
    public static int GetResultSetType(jdbcStatement stmt) {
        return stmt.rsType;
    }

    /**
     * Retrieves the parameter metadata descriptor for the
     * given statement object.
     *
     * @param stmt the statement object from which to retrieve the
     *      parameter metadata descriptor.
     * @return the parameter metadata descriptor
     * @exception NullPointerException if the given statement object
     *      reference is <tt>null</tt>.
     */
    public static Result GetParameterMetaDataDescriptor(
            jdbcPreparedStatement stmt) {
        return stmt.pmdDescriptor;
    }

    /**
     * Retrieves the result set metadata descriptor for the
     * given statement object.
     *
     * @param stmt the statement object from which to retrieve the
     *      result set metadata descriptor.
     * @return the result set metadata descriptor
     * @exception NullPointerException if the given statement object
     *      reference is <tt>null</tt>.
     */
    public static Result GetResultSetMetaDataDescriptor(
            jdbcPreparedStatement stmt) {
        return stmt.rsmdDescriptor;
    }

    /**
     * Retrieves the SQL character sequence with which the given statement
     * object was constructed.
     *
     * @param stmt the statement object from which to retrieve the SQL
     *      character sequence.
     * @return the SQL character sequence with which the given statement
     *      object was constructed.
     * @exception NullPointerException if the given statement object
     *      reference is <tt>null</tt>.
     */
    public static String GetSQL(jdbcPreparedStatement stmt) {
        return stmt.sql;
    }

    /**
     * Retrieves the compiled statement identifier for the given statement object.
     *
     * @param stmt the statement object from which to retrieve the compiled
     *      statement identifier.
     * @return the compiled statement identifier for the given statement object.
     * @exception NullPointerException if the given statement object
     *      reference is <tt>null</tt>.
     */
    public static int GetStatementId(jdbcPreparedStatement stmt) {
        return stmt.statementID;
    }

    /**
     * Retrieves whether the given statement object is closed.
     *
     * @param stmt the statement object for which to make the determination.
     * @return <tt>true</tt> if the given statement object is closed;
     *      else <tt>false</tt>.
     * @exception NullPointerException if the given statement object
     *      reference is <tt>null</tt>.
     */
    public static boolean IsClosed(jdbcStatement stmt) {
        return stmt.isClosed();
    }

    /**
     * Retrieves whether execution of the given statement object
     * generates an affected row count result.
     * @param stmt the statement object for which to make the determination.
     * @return whether execution of the given statement object
     *      generates an affected row count result.
     * @exception NullPointerException if the given statement object
     *      reference is <tt>null</tt>.
     */
    public static boolean IsRowCount(jdbcPreparedStatement stmt) {
        return stmt.isRowCount;
    }
    //
    private static final String[] NO_NAMES = new String[0];

    /**
     * Retrieves the names of the parameters for the given statement object.
     *
     * @param stmt the statement object for which to retrieve
     *      the parameter names.
     * @return the names of the parameters for the given statement object.
     * @exception NullPointerException if the given statement object
     *      reference is <tt>null</tt>.
     */
    public static String[] GetParameterNames(jdbcPreparedStatement stmt) {
        return (stmt.pmdDescriptor == null
                || stmt.pmdDescriptor.metaData == null
                || stmt.pmdDescriptor.metaData.colNames == null)
                ? NO_NAMES
                : (String[]) stmt.pmdDescriptor.metaData.colNames.clone();
    }
}
