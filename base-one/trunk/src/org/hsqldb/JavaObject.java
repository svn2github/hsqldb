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

import org.hsqldb.lib.StringUtil;

/**
 * Represents of an instance of an OTHER field value. <p>
 *
 * Prior to 1.7.0 there were problems storing Objects of normal column types
 * in columns of the type OTHER. In 1.7.0 changes were made to allow this,
 * but as all the conversion took place inside the engine, it introduced a
 * requirement for all classes for objects stored in OTHER columns to be
 * available on the runtime class path of the engine. <p>
 *
 * In 1.7.2, the introduction of real preprared statement support allows us
 * revert to the pre 1.7.0 behaviour without the artificial limitations. <p>
 *
 * The classes for stored objects need not be available to open and operate
 * the database in general. The classes need to be available only if a
 * conversion from one of these objects to another type is performed inside
 * the engine while operating the database.
 *
 * The current limitation is that in SQL statements, values of type String
 * (CHARACTER and related SQL types) cannot be stored in columns of type
 * OTHER. This limitation does not exist for String values assigned to
 * PreparedStatement variables.
 *
 * @author fredt@users
 * @version 1.7.2
 * @since 1.7.2
 */
public class JavaObject {

    private byte[] data;
    private Object object;

    /**
     * This constructor is used inside the engine when an already serialized
     * Object is read from a file (.log, .script, .data or text table source).
     */
    public JavaObject(byte[] data) {
        this.data = data;
    }

    /**
     * This constructor is from classes implementing the JDBC interfaces.<b>
     * Inside the engine, it is used to convert an object into an object
     * of type OTHER.
     */
    public JavaObject(Object o, boolean serialise) throws HsqlException {

        if (serialise) {
            data = Column.serialize(o);
        } else {
            object = o;
        }
    }

    public byte[] getBytes() throws HsqlException {

        if (data == null) {
            data = Column.serialize(object);
        }

        return data;
    }

    public int getBytesLength() throws HsqlException {

        if (data == null) {
            data = Column.serialize(object);
        }

        return data.length;
    }

    /**
     * This method is called from classes implementing the JDBC
     * interfaces. Inside the engine it is used for conversion from a value of
     * type OTHER to another type. It will throw if the OTHER is an instance
     * of a classe that is not available.
     */
    public Object getObject() throws HsqlException {

        if (object == null) {
            object = Column.deserialize(data);
        }

        return object;
    }

    /**
     * Retrieves the deserialized (Object) form of the OTHER value this
     * object represents, or null if the OTHER value is not already available
     * for retrieval in deserialized form.
     *
     * The primary client of this method is
     * CompiledStatementExecutor.executeCallStatement, wherein an attempt is
     * made to determine if this object wraps a Result, in which case the
     * Result is unwrapped and and used directly to present results through
     * the JDBC client interfaces. <p>
     *
     * @return the deserialized form of the OTHER value this object represents,
     *      or null if the OTHER value is not already available
     *      for retrieval in deserialized form
     */
    Object getObjectNoDeserialize() {
        return object;
    }

    /**
     * Retrieves a String repsentation of this object.
     *
     * @return a String represntation of this object.
     */
    public String toString() {

        String s = super.toString() + "[";

        try {
            return s + "getObject()=[" + String.valueOf(getObject()) + "]]";
        } catch (Exception e) {
            return s + "getObject()_exception=[" + e.toString() + "]]";
        }
    }
}
