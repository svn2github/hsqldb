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


package org.hsqldb;

import java.io.IOException;

/**
 * Representation of an instance of OTHER field data.<p>
 *
 * Prior to 1.7.0 there were problems storing Objets of normal column types
 * in columns of the type OTHER. In 1.7.0 changes were made to allow this,
 * but as all the conversion took place inside the engine, it introduced a
 * requirement for all classes for objects stored in OTHER columns to be
 * available in the class path of the engine.<p>
 * In 1.7.2, the introduction of real preprared statement support allows us
 * revert to the pre 1.7.0 behaviour without the artificial limitations.<b>
 *
 * The classes for stored objects need not be available to open and operate
 * on the database in general. The classes need to be available only if a
 * conversion from one of these objects to another type is performed inside
 * the engine while operating the database.
 *
 * Current limitation is that in SQL statements, values of type String
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
     * This constructor is from classes implementing the JDBC interfaces.<b>
     * Inside the engine, it is used to convert a value into an object
     * of type OTHER.
     */
    JavaObject(Object o) {
        object = o;
    }

    /**
     * This constructor is used inside the engine when an already serialized
     * Object is read from a file (.log, .script, .data or text table source).
     *
     * fromfile is a marker argument to fully distinguish this from the other
     * constructor
     */
    JavaObject(byte[] data, boolean fromfile) {
        this.data = data;
    }

    byte[] getBytes() throws HsqlException {

        if (data == null) {
            data = Column.serialize(object);
        }

        return data;
    }

    int getBytesLength() throws HsqlException {

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
    Object getObject() throws HsqlException {

        if (object == null) {
            object = Column.deserialize(data);
        }

        return object;
    }
}
