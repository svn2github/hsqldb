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

import java.sql.SQLException;
import org.hsqldb.lib.UnifiedTable;

/**
 * Transitional container for object names that are unique across the
 * DB instance but are owned by different DB objects. Currently names for
 * Index and Trigger objects.
 * @author fredt@users
 * @version 1.7.2
 * @since 1.7.2
 */
class DatabaseObjectNames {

    UnifiedTable nameList = new UnifiedTable(Object.class, 2);
    Object[]     tempName = new Object[2];

    DatabaseObjectNames() {
        nameList.sort(0, true);
    }

    boolean containsName(String name) {
        return nameList.search(name) != -1;
    }

    HsqlName getOwner(String name) {

        int i = nameList.search(name);

        if (i == -1) {
            return null;
        }

        return (HsqlName) nameList.getCell(i, 1);
    }

    void addName(String name, Object owner) throws SQLException {

        // should not contain name
        if (containsName(name)) {
            throw Trace.error(Trace.GENERAL_ERROR);
        }

        tempName[0] = name;
        tempName[1] = owner;

        nameList.addRow(tempName);
        nameList.sort(0, true);
    }

    void rename(String name, String newname) throws SQLException {

        int i = nameList.search(name);

        if (i != -1) {
            nameList.setCell(i, 0, newname);
            nameList.sort(0, true);
        }
    }

    void removeName(String name) throws SQLException {

        int i = nameList.search(name);

        if (i != -1) {
            nameList.removeRow(i);
        } else {

            // should contain name
            throw Trace.error(Trace.GENERAL_ERROR);
        }
    }

    void removeOwner(Object name) {

        int i = nameList.size();

        while (i-- > 0) {
            Object owner = nameList.getCell(i, 1);

            if (owner == name || (owner != null && owner.equals(name))) {
                nameList.removeRow(i);
            }
        }
    }
}
