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

//import java.util.Enumeration;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.HashMappedList;
import org.hsqldb.lib.HashMap;
import org.hsqldb.lib.IntValueHashMap;
import org.hsqldb.lib.Iterator;
import org.hsqldb.lib.HsqlStringBuffer;
import org.hsqldb.HsqlNameManager.HsqlName;

/**
 * Script generation.
 *
 * @version 1.7.2
 */
class DatabaseScript {

    /**
     * Returns the DDL and all other statements for the database excluding
     * INSERT and SET <tablename> READONLY statements.
     * bCached == true indicates that SET <tablenmae> INDEX statements should
     * also be included.
     *
     * This class should not have any dependencies on metadata reporting.
     */
    static Result getScript(Database dDatabase, boolean bCached) {

        HsqlArrayList tTable          = dDatabase.getTables();
        HsqlArrayList forwardFK       = new HsqlArrayList();
        HsqlArrayList forwardFKSource = new HsqlArrayList();
        Result r = Result.newSingleColumnResult("COMMAND", Types.VARCHAR);

        r.metaData.sTable[0] = "SYSTEM_SCRIPT";

        HsqlStringBuffer a;

        // tables
        for (int i = 0, tSize = tTable.size(); i < tSize; i++) {
            Table t = (Table) tTable.get(i);

            if (t.isTemp() || t.isView()) {
                continue;
            }

            a = new HsqlStringBuffer(128);

            getTableDDL(dDatabase, t, i, forwardFK, forwardFKSource, a);
            addRow(r, a.toString());

            // indexes for table
            for (int j = 1; j < t.getIndexCount(); j++) {
                Index index = t.getIndex(j);

                if (HsqlName.isReservedIndexName(index.getName().name)) {

                    // the following are autocreated with the table
                    // indexes for primary keys
                    // indexes for unique constraints
                    // own table indexes for foreign keys
                    continue;
                }

                a = new HsqlStringBuffer(64);

                a.append("CREATE ");

                if (index.isUnique()) {
                    a.append("UNIQUE ");
                }

                a.append("INDEX ");
                a.append(index.getName().statementName);
                a.append(" ON ");
                a.append(t.getName().statementName);

                int col[] = index.getColumns();
                int len   = index.getVisibleColumns();

                getColumnList(t, col, len, a);
                addRow(r, a.toString());
            }

            // readonly for TEXT tables only
            if (t.isText() && t.isDataReadOnly()) {
                a = new HsqlStringBuffer("SET TABLE ");

                a.append(t.getName().statementName);
                a.append(" READONLY TRUE");
                addRow(r, a.toString());
            }

            // data source
            String dataSource = getDataSource(t);

            if (dataSource != null) {
                addRow(r, dataSource);
            }

            // triggers
            int numTrigs = TriggerDef.NUM_TRIGS;

            for (int tv = 0; tv < numTrigs; tv++) {
                HsqlArrayList trigVec = t.vTrigs[tv];
                int           trCount = trigVec.size();

                for (int k = 0; k < trCount; k++) {
                    a = ((TriggerDef) trigVec.get(k)).toBuf();

                    addRow(r, a.toString());
                }
            }
        }

        // forward referencing foreign keys
        for (int i = 0, tSize = forwardFK.size(); i < tSize; i++) {
            Constraint c = (Constraint) forwardFK.get(i);

            a = new HsqlStringBuffer(128);

            a.append("ALTER TABLE ");
            a.append(c.getRef().getName().statementName);
            a.append(" ADD ");
            getFKStatement(c, a);
            addRow(r, a.toString());
        }

        // SET <tablename> INDEX statements
        for (int i = 0, tSize = tTable.size(); i < tSize; i++) {
            Table t = (Table) tTable.get(i);

            if (bCached && t.isIndexCached() &&!t.isEmpty()) {
                addRow(r, getIndexRootsDDL((Table) tTable.get(i)));
            }
        }

        // ignorecase for future CREATE TABLE statements
        if (dDatabase.isIgnoreCase()) {
            addRow(r, "SET IGNORECASE TRUE");
        }

        // aliases
        HashMap  h       = dDatabase.getAlias();
        HashMap  builtin = Library.getAliasMap();
        Iterator e       = h.keySet().iterator();

        while (e.hasNext()) {
            String alias  = (String) e.next();
            String java   = (String) h.get(alias);
            String biJava = (String) builtin.get(alias);

            if (biJava != null && biJava.equals(java)) {
                continue;
            }

            HsqlStringBuffer buffer = new HsqlStringBuffer(64);

            buffer.append("CREATE ALIAS ");
            buffer.append(alias);
            buffer.append(" FOR \"");
            buffer.append(java);
            buffer.append('"');
            addRow(r, buffer.toString());
        }

        // views
        for (int i = 0, tSize = tTable.size(); i < tSize; i++) {
            Table t = (Table) tTable.get(i);

            if (t.isView()) {
                View v = (View) tTable.get(i);

                a = new HsqlStringBuffer(128);

                a.append("CREATE ");
                a.append("VIEW ");
                a.append(v.getName().statementName);
                a.append(" AS ");
                a.append(v.getStatement());
                addRow(r, a.toString());
            }
        }

        // rights for classes, tables and views
        addRightsStatements(dDatabase, r);

        return r;
    }

    static String getIndexRootsDDL(Table t) {

        HsqlStringBuffer a = new HsqlStringBuffer(128);

        a.append("SET TABLE ");
        a.append(t.getName().statementName);
        a.append(" INDEX '");
        a.append(t.getIndexRoots());
        a.append('\'');

        return a.toString();
    }

    static void getTableDDL(Database dDatabase, Table t, int i,
                            HsqlArrayList forwardFK,
                            HsqlArrayList forwardFKSource,
                            HsqlStringBuffer a) {

        a.append("CREATE ");

        if (t.isText()) {
            a.append("TEXT ");
        } else if (t.isCached()) {
            a.append("CACHED ");
        }

        a.append("TABLE ");
        a.append(t.getName().statementName);
        a.append('(');

        int   columns = t.getColumnCount();
        Index pki     = t.getIndex(0);
        int   pk[]    = pki.getColumns();

        for (int j = 0; j < columns; j++) {
            Column column  = t.getColumn(j);
            String colname = column.columnName.statementName;

            a.append(colname);
            a.append(' ');

            String sType = Types.getTypeString(column.getType());

            a.append(sType);

            // append the size and scale if > 0
            if (column.getSize() > 0) {
                a.append('(');
                a.append(column.getSize());

                if (column.getScale() > 0) {
                    a.append(',');
                    a.append(column.getScale());
                }

                a.append(')');
            }

            String defaultString = column.getDefaultString();

            if (defaultString != null) {
                boolean quote = false;

                switch (column.getType()) {

                    // do not quote CURRENT_DATE etc.
                    case Types.DATE :
                    case Types.TIMESTAMP :
                        quote = defaultString.indexOf('-') != -1;
                        break;

                    case Types.TIME :
                        quote = defaultString.indexOf(':') != -1;
                        break;

                    case Types.CHAR :
                    case Types.VARCHAR :
                    case Types.VARCHAR_IGNORECASE :
                    case Types.LONGVARCHAR :
                    case Types.BINARY :
                    case Types.VARBINARY :
                    case Types.LONGVARBINARY :
                        quote = true;
                    default :
                }

                if (quote) {
                    defaultString = Column.createSQLString(defaultString);
                }

                a.append(" DEFAULT ");
                a.append(defaultString);
            }

            if (j == t.getIdentityColumn()) {
                a.append(" GENERATED BY DEFAULT AS IDENTITY(START WITH ");
                a.append(column.identityStart);
                a.append(") ");
            }

            if (!column.isNullable()) {
                a.append(" NOT NULL");
            }

            if ((pk.length == 1) && (j == pk[0])
                    && pki.getName().isReservedIndexName()) {
                a.append(" PRIMARY KEY");
            }

            if (j < columns - 1) {
                a.append(',');
            }
        }

        if (pk.length > 1 ||!pki.getName().isReservedIndexName()) {
            a.append(",CONSTRAINT ");
            a.append(pki.getName().statementName);
            a.append(" PRIMARY KEY");
            getColumnList(t, pk, pk.length, a);
        }

        HsqlArrayList v = t.getConstraints();

        for (int j = 0, vSize = v.size(); j < vSize; j++) {
            Constraint c = (Constraint) v.get(j);

            switch (c.getType()) {

                case Constraint.UNIQUE :
                    a.append(",CONSTRAINT ");
                    a.append(c.getName().statementName);
                    a.append(" UNIQUE");

                    int col[] = c.getMainColumns();

                    getColumnList(c.getMain(), col, col.length, a);
                    break;

                case Constraint.FOREIGN_KEY :

                    // forward referencing FK
                    Table maintable      = c.getMain();
                    int   maintableindex = dDatabase.getTableIndex(maintable);

                    if (maintableindex > i) {
                        if (i >= forwardFKSource.size()) {
                            forwardFKSource.setSize(i + 1);
                        }

                        forwardFKSource.set(i, c);
                        forwardFK.add(c);
                    } else {
                        a.append(',');
                        getFKStatement(c, a);
                    }
            }
        }

        a.append(')');
    }

    /**
     * Generates the SET TABLE <tablename> SOURCE <string> statement for a
     * text table;
     */
    static String getDataSource(Table t) {

        String dataSource = t.getDataSource();

        if (dataSource == null) {
            return null;
        }

        boolean          isDesc = t.isDescDataSource();
        HsqlStringBuffer a      = new HsqlStringBuffer(128);

        a.append("SET TABLE ");
        a.append(t.getName().statementName);
        a.append(" SOURCE \"");
        a.append(dataSource);
        a.append('"');

        if (isDesc) {
            a.append(" DESC");
        }

        return a.toString();
    }

    /**
     * Generates the column definitions for a table.
     */
    private static void getColumnList(Table t, int col[], int len,
                                      HsqlStringBuffer a) {

        a.append('(');

        for (int i = 0; i < len; i++) {
            a.append(t.getColumn(col[i]).columnName.statementName);

            if (i < len - 1) {
                a.append(',');
            }
        }

        a.append(')');
    }

    /**
     * Generates the foreign key declaration for a given Constraint object.
     */
    private static void getFKStatement(Constraint c, HsqlStringBuffer a) {

        a.append("CONSTRAINT ");
        a.append(c.getName().statementName);
        a.append(" FOREIGN KEY");

        int col[] = c.getRefColumns();

        getColumnList(c.getRef(), col, col.length, a);
        a.append(" REFERENCES ");
        a.append(c.getMain().getName().statementName);

        col = c.getMainColumns();

        getColumnList(c.getMain(), col, col.length, a);

        if (c.getDeleteAction() != Constraint.NO_ACTION) {
            a.append(" ON DELETE ");
            a.append(getFKAction(c.getDeleteAction()));
        }

        if (c.getUpdateAction() != Constraint.NO_ACTION) {
            a.append(" ON UPDATE ");
            a.append(getFKAction(c.getUpdateAction()));
        }
    }

    /**
     * Returns the foreign key action rule.
     */
    private static String getFKAction(int action) {

        switch (action) {

            case Constraint.CASCADE :
                return "CASCADE";

            case Constraint.SET_DEFAULT :
                return "SET DEFAULT";

            case Constraint.SET_NULL :
                return "SET NULL";

            default :
                return "NO ACTION";
        }
    }

    /**
     * Adds a script line to the result.
     */
    private static void addRow(Result r, String sql) {

        String s[] = new String[1];

        s[0] = sql;

        r.add(s);
    }

    /**
     * Generates the GRANT statements for users.
     *
     * When views is true, generates rights for views only. Otherwise generates
     * rights for tables and classes.
     *
     * Does not generate script for:
     *
     * grant on builtin classes to public
     * grant select on system tables
     *
     */
    private static void addRightsStatements(Database dDatabase, Result r) {

        HsqlStringBuffer a;
        HashMappedList   uv = dDatabase.getUserManager().getUsers();
        Iterator         it = uv.values().iterator();

        for (; it.hasNext(); ) {
            User   u    = (User) it.next();
            String name = u.getName();

            if (!name.equals("PUBLIC")) {
                a = new HsqlStringBuffer(128);

                a.append("CREATE USER ");
                a.append(name);
                a.append(" PASSWORD ");
                a.append('"');
                a.append(u.getPassword());
                a.append('"');

                if (u.isAdmin()) {
                    a.append(" ADMIN");
                }

                addRow(r, a.toString());
            }

            IntValueHashMap rights = u.getRights();

            if (rights == null) {
                continue;
            }

            Iterator e = rights.keySet().iterator();

            while (e.hasNext()) {
                Object object = e.next();
                int    right  = rights.get(object, 0);

                a = new HsqlStringBuffer(64);

                a.append("GRANT ");
                a.append(UserManager.getRight(right));
                a.append(" ON ");

                if (object instanceof String) {
                    if (object.equals("java.lang.Math")
                            || object.equals("org.hsqldb.Library")) {
                        continue;
                    }

                    a.append("CLASS \"");
                    a.append((String) object);
                    a.append('\"');
                } else {

                    // either table != null or is system table
                    Table table =
                        dDatabase.findUserTable(((HsqlName) object).name);

                    // assumes all non String objects are table names
                    if (table != null) {
                        a.append(((HsqlName) object).statementName);
                    } else {
                        continue;
                    }
                }

                a.append(" TO ");
                a.append(u.getName());
                addRow(r, a.toString());
            }
        }
    }
}
