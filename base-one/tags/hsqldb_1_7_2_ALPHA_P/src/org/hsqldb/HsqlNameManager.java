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

import org.hsqldb.lib.HsqlStringBuffer;
import org.hsqldb.lib.StringConverter;

/**
 * Name Manager for an SQL object<p>
 *
 * This class now includes the HsqlName inroduced in 1.7.1 and improves
 * auto-naming with several database in the engine.<p>
 *
 * Methods check user defined names and issue system generated names
 * for SQL objects.<p>
 *
 * This class does not deal with the type of the SQL object for which it
 * is used.<p>
 *
 * Some names beginning with SYS_ are reserved for system generated names.
 * These are defined in isReserveName(String name) and created by the
 * makeAutoName(String type) factory method<p>
 *
 * sysNumber is used to generate system generated names. It is
 * set to the largest integer encountered in names that use the
 * SYS_xxxxxxx_INTEGER format. As the DDL is processed before any ALTER
 * command, any new system generated name will have a larger integer suffix
 * than all the existing names.
 *
 * @author fredt@users
 * @version 1.7.2
 * @since 1.7.2
 */
class HsqlNameManager {

    private static HsqlNameManager staticManager = new HsqlNameManager();
    private static int             serialNumber  = 0;
    private int                    sysNumber     = 0;

    static HsqlName newHsqlSystemTableName(String name) {
        return new HsqlName(staticManager, name);
    }

    HsqlName newHsqlName(String name, boolean isquoted) throws HsqlException {
        return new HsqlName(this, name, isquoted);
    }

    HsqlName newHsqlName(String prefix, String name,
                         boolean isquoted) throws HsqlException {
        return new HsqlName(this, prefix, name, isquoted);
    }

    HsqlName newHsqlName(String name) {
        return new HsqlName(this, name);
    }

    /**
     * Auto names are used for autogenerated indexes or anonymous constraints.
     * Also the name of a pseudo-column is the autoname ""
     */
    HsqlName newAutoName(String type) {
        return newAutoName(type, null);
    }

    /**
     * Auto names are used for autogenerated indexes or anonymous constraints.
     * Also the name of a pseudo-column is the autoname ""
     */
    HsqlName newAutoName(String type, String namepart) {

        HsqlStringBuffer sbname = new HsqlStringBuffer();

        if (type != null) {
            if (type.length() != 0) {
                sbname.append("SYS_");
                sbname.append(type);
                sbname.append('_');

                if (namepart != null) {
                    sbname.append(namepart);
                    sbname.append('_');
                }

                sbname.append(++sysNumber);
            }
        } else {
            sbname.append(namepart);
        }

        return new HsqlName(this, sbname.toString());
    }

    void resetNumbering() {
        sysNumber    = 0;
        serialNumber = 0;
    }

    static class HsqlName {

        HsqlNameManager   manager;
        String            name;
        boolean           isNameQuoted;
        String            statementName;
        private final int hashCode;

        private HsqlName(HsqlNameManager man) {
            manager  = man;
            hashCode = HsqlNameManager.serialNumber++;
        }

        private HsqlName(HsqlNameManager man, String name,
                         boolean isquoted) throws HsqlException {

            this(man);

            rename(name, isquoted);
        }

        private HsqlName(HsqlNameManager man, String prefix, String name,
                         boolean isquoted) throws HsqlException {

            this(man);

            rename(prefix, name, isquoted);
        }

        private HsqlName(HsqlNameManager man, String name) {

            this(man);

            this.name = this.statementName = name;
        }

        void rename(String name, boolean isquoted) throws HsqlException {

            this.name          = name;
            this.statementName = name;
            this.isNameQuoted  = isquoted;

            if (name == null || name.length() == 0) {
                throw Trace.error(Trace.INVALID_IDENTIFIER);
            }

            if (isNameQuoted) {
                statementName = StringConverter.toQuotedString(name, '"',
                        true);
            }

            if (name.startsWith("SYS_")) {
                int index = name.lastIndexOf('_') + 1;

                try {
                    int temp = Integer.parseInt(name.substring(index));

                    if (temp > manager.sysNumber) {
                        manager.sysNumber = temp;
                    }
                } catch (NumberFormatException e) {}
            }
        }

        void rename(String prefix, String name,
                    boolean isquoted) throws HsqlException {

            HsqlStringBuffer sbname = new HsqlStringBuffer(prefix);

            sbname.append('_');
            sbname.append(name);
            rename(sbname.toString(), isquoted);
        }

        public boolean equals(HsqlName other) {

            if (Trace.TRACE) {
                Trace.trace("HsqlName.equals()");
            }

            return hashCode == other.hashCode;
        }

        /**
         * hash code for this object is its unique serial number.
         */
        public int hashCode() {
            return hashCode;
        }

        /**
         * "SYS_IDX_" is used for auto-indexes on referring FK columns or
         * unique constraints.
         * "SYS_PK_" is for the primary key indexes.
         * "SYS_REF_" is for FK constraints in referenced tables
         *
         */
        static boolean isReservedIndexName(String name) {
            return (name.startsWith("SYS_IDX_") || name.startsWith("SYS_PK_")
                    || name.startsWith("SYS_REF_"));
        }

        boolean isReservedIndexName() {
            return isReservedIndexName(name);
        }

        public String toString() {

            return getClass().getName() + super.hashCode()
                   + "[this.hashCode()=" + this.hashCode + ", name=" + name
                   + ", name.hashCode()=" + name.hashCode()
                   + ", isNameQuoted=" + isNameQuoted + "]";
        }

        public int compareTo(Object o) {
            return hashCode - o.hashCode();
        }
    }
}
