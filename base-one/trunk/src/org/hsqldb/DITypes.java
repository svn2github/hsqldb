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

/**
 *
 * @author  boucherb@users.sourceforge.net
 */
public interface DITypes {
    
    /** The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>ARRAY</code>.
     * @since 1.2
     *
     */
    int ARRAY = 2003;
    
    /** <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>BIGINT</code>.
     *
     */
    int BIGINT = -5;
    
    /** <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>BINARY</code>.
     *
     */
    int BINARY = -2;
    
    /** <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>BIT</code>.
     *
     */
    int BIT = -7;
    
    /** The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>BLOB</code>.
     * @since 1.2
     *
     */
    int BLOB = 2004;
    
    /** The constant in the Java programming language, somtimes referred to
     * as a type code, that identifies the generic SQL type <code>BOOLEAN</code>.
     *
     * @since 1.4
     *
     */
    int BOOLEAN = 16;
    
    /** <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>CHAR</code>.
     *
     */
    int CHAR = 1;
    
    /** The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>CLOB</code>.
     * @since 1.2
     *
     */
    int CLOB = 2005;
    
    /** The constant in the Java programming language, somtimes referred to
     * as a type code, that identifies the generic SQL type <code>DATALINK</code>.
     *
     * @since 1.4
     *
     */
    int DATALINK = 70;
    
    /** <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>DATE</code>.
     *
     */
    int DATE = 91;
    
    /** <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>DECIMAL</code>.
     *
     */
    int DECIMAL = 3;
    
    /** The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>DISTINCT</code>.
     * @since 1.2
     *
     */
    int DISTINCT = 2001;
    
    /** <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>DOUBLE</code>.
     *
     */
    int DOUBLE = 8;
    
    /** <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>FLOAT</code>.
     *
     */
    int FLOAT = 6;
    
    /** <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>INTEGER</code>.
     *
     */
     int INTEGER = 4;
    
    /** The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>JAVA_OBJECT</code>.
     * @since 1.2
     *
     */
     int JAVA_OBJECT = 2000;
    
    /** <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>LONGVARBINARY</code>.
     *
     */
     int LONGVARBINARY = -4;
    
    /** <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>LONGVARCHAR</code>.
     *
     */
     int LONGVARCHAR = -1;
    
    /** <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>NULL</code>.
     *
     */
     int NULL = 0;
    
    /** <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>NUMERIC</code>.
     *
     */
     int NUMERIC = 2;
    
    /** The constant in the Java programming language that indicates
     * that the SQL type is database-specific and
     * gets mapped to a Java object that can be accessed via
     * the methods <code>getObject</code> and <code>setObject</code>.
     *
     */
     int OTHER = 1111;
    
    /** <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>REAL</code>.
     *
     */
     int REAL = 7;
    
    /** The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>REF</code>.
     * @since 1.2
     *
     */
     int REF = 2006;
    
    /** <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>SMALLINT</code>.
     *
     */
     int SMALLINT = 5;
    
    /** The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>STRUCT</code>.
     * @since 1.2
     *
     */
     int STRUCT = 2002;
    
    /** <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>TIME</code>.
     *
     */
     int TIME = 92;
    
    /** <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>TIMESTAMP</code>.
     *
     */
     int TIMESTAMP = 93;
    
    /** <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>TINYINT</code>.
     *
     */
     int TINYINT = -6;
    
    /** <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>VARBINARY</code>.
     *
     */
     int VARBINARY = -3;
    
    /** <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>VARCHAR</code>.
     *
     */
     int VARCHAR = 12;
    
    /** <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the recent SQL 200n SQL type
     * <code>XML</code>.
     *
     */
     int XML = 137; 
           
    int TYPE_SUB_DEFAULT = 1;
    int TYPE_SUB_IDENTITY = TYPE_SUB_DEFAULT<<1;
    int TYPE_SUB_IGNORECASE = TYPE_SUB_DEFAULT<<2;
    
    int[][] ALL_TYPES = {
        {ARRAY,         TYPE_SUB_DEFAULT},
        {BIGINT,        TYPE_SUB_DEFAULT},
        {BIGINT,        TYPE_SUB_IDENTITY},
        {BINARY,        TYPE_SUB_DEFAULT},
        {BIT,           TYPE_SUB_DEFAULT},
        {BLOB,          TYPE_SUB_DEFAULT},
        {BOOLEAN,       TYPE_SUB_DEFAULT},
        {CHAR,          TYPE_SUB_DEFAULT},
        {CLOB,          TYPE_SUB_DEFAULT},
        {DATALINK,      TYPE_SUB_DEFAULT},
        {DATE,          TYPE_SUB_DEFAULT},
        {DECIMAL,       TYPE_SUB_DEFAULT},
        {DISTINCT,      TYPE_SUB_DEFAULT},
        {DOUBLE,        TYPE_SUB_DEFAULT},
        {FLOAT,         TYPE_SUB_DEFAULT},
        {INTEGER,       TYPE_SUB_DEFAULT},
        {INTEGER,       TYPE_SUB_IDENTITY},
        {JAVA_OBJECT,   TYPE_SUB_DEFAULT},
        {LONGVARBINARY, TYPE_SUB_DEFAULT},
        {LONGVARCHAR,   TYPE_SUB_DEFAULT},
        {NULL,          TYPE_SUB_DEFAULT},
        {NUMERIC,       TYPE_SUB_DEFAULT},
        {OTHER,         TYPE_SUB_DEFAULT},
        {REAL,          TYPE_SUB_DEFAULT},
        {REF,           TYPE_SUB_DEFAULT},
        {SMALLINT,      TYPE_SUB_DEFAULT},
        {STRUCT,        TYPE_SUB_DEFAULT},
        {TIME,          TYPE_SUB_DEFAULT},
        {TIMESTAMP,     TYPE_SUB_DEFAULT},
        {TINYINT,       TYPE_SUB_DEFAULT},
        {VARBINARY,     TYPE_SUB_DEFAULT},
        {VARCHAR,       TYPE_SUB_DEFAULT},
        {VARCHAR,       TYPE_SUB_IGNORECASE},
        {XML,           TYPE_SUB_DEFAULT}
    };
    
}
