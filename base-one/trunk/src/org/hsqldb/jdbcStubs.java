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

import java.sql.ResultSet;

class jdbcStubs {}

//#ifdef JAVA2
/*
*/
//#else
// surrogate for java.util.Map interface
interface Map {

    int size();

    boolean isEmpty();

    boolean containsKey(Object key);

    boolean containsValue(Object value);

    Object get(Object key);

    Object put(Object key, Object value);

    Object remove(Object key);

    void putAll(Map t);

    void clear();

//    public Set keySet();

//    public Collection values();

//    public Set entrySet();

    interface Entry {

        Object getKey();

        Object getValue();

        Object setValue(Object value);

        boolean equals(Object o);

        int hashCode();
    }

    boolean equals(Object o);

    int hashCode();
}

// surrogates for java.SQL type interfaces
interface Array {

    String getBaseTypeName() throws HsqlException;

    int getBaseType() throws HsqlException;

    Object getArray() throws HsqlException;

    Object getArray(Map map) throws HsqlException;

    Object getArray(long index, int count) throws HsqlException;

    Object getArray(long index, int count, Map map) throws HsqlException;

    ResultSet getResultSet() throws HsqlException;

    ResultSet getResultSet(Map map) throws HsqlException;

    ResultSet getResultSet(long index, int count) throws HsqlException;

    ResultSet getResultSet(long index, int count,
                           Map map) throws HsqlException;
}

interface Blob {

    long length() throws HsqlException;

    byte[] getBytes(long pos, int length) throws HsqlException;

    java.io.InputStream getBinaryStream() throws HsqlException;

    long position(byte pattern[], long start) throws HsqlException;

    long position(Blob pattern, long start) throws HsqlException;
}

interface Clob {

    long length() throws HsqlException;

    String getSubString(long pos, int length) throws HsqlException;

    java.io.Reader getCharacterStream() throws HsqlException;

    java.io.InputStream getAsciiStream() throws HsqlException;

    long position(String searchstr, long start) throws HsqlException;

    long position(Clob searchstr, long start) throws HsqlException;
}

interface Ref {
    String getBaseTypeName() throws HsqlException;
}

//#endif JAVA2
