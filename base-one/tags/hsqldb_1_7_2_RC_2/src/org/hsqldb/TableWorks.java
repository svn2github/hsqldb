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
 * Copyright (c) 2001-2004, The HSQL Development Group
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

import org.hsqldb.HsqlNameManager.HsqlName;
import org.hsqldb.lib.HashSet;
import org.hsqldb.lib.HsqlArrayList;

// fredt@users 20020520 - patch 1.7.0 - ALTER TABLE support
// tony_lai@users 20020820 - patch 595172 - drop constraint fix

/**
 * The methods in this class perform alterations to the structure of an
 * existing table which may result in a new Table object
 *
 * @version 1.7.2
 * @since 1.7.0
 */
class TableWorks {

    private Table table;

    TableWorks(Table table) {
        this.table = table;
    }

    Table getTable() {
        return table;
    }

// boucherb@users 20030402 - patch 1.7.2 added for reuse of TableWorks object
// under command interpreter support
    void setTable(Table table) {
        this.table = table;
    }

// fredt@users 20020225 - patch 1.7.0 - require existing index for foreign key
// fredt@users 20030309 - patch 1.7.2 - more rigorous rules

    /**
     *  Creates a foreign key according to current sql.strict_fk or
     *  sql.strong_fk settings. Foreign keys are enforced via indexes on both
     *  the referencing (child) and referenced (parent) tables.
     *  <p>
     *  In versions 1.7.0 and 1.7.1 some non-standard features were supported
     *  for compatibility with older databases. These allowed foreign keys
     *  to be created without the prior existence of a unique constraint on
     *  the referenced columns.
     *  <p>
     *  In version 1.7.2, a unique constraint on the referenced columns must
     *  exist.
     *
     *  The non-unique index on the referencing table is now always created
     *  whether or not a PK or unique constraint index on the columns exist.
     *  This closes the loopholes opened by the introduction of ALTER TABLE
     *  for adding foreign keys.
     *
     *  Foriegn keys on temp tables can reference other temp tables with the
     *  same rules above. Foreign keys on permanent tables cannot reference
     *  temp tables.
     *
     *  Duplicate foreign keys are now disallowed.
     *
     *  -- The unique index on the referenced table must always belong to a
     *  constraint (PK or UNIQUE). Otherwise after a SHUTDOWN and restart the
     *  index will not exist at the time of creation of the foreign key when
     *  the foreign key is referencing the  same table.
     *
     *  -- The non-unique index on the referencing table is always created
     *  regardless of any existing index. This allows the foreign key
     *  constraint to be dropped when required.
     *
     *
     *  (fred@users)
     *
     * @param  fkcol
     * @param  expcol
     * @param  fkname         foreign key name
     * @param  expTable
     * @param  deleteAction
     * @param  updateAction
     * @throws HsqlException
     */
    void createForeignKey(int fkcol[], int expcol[], HsqlName name,
                          Table expTable, int deleteAction,
                          int updateAction) throws HsqlException {

        if (table.database.constraintNameList.containsName(name.name)) {
            throw Trace.error(Trace.CONSTRAINT_ALREADY_EXISTS, name.name);
        }

        // name check
        if (table.getConstraint(name.name) != null) {
            throw Trace.error(Trace.CONSTRAINT_ALREADY_EXISTS);
        }

        // existing FK check
        if (table.getConstraintForColumns(expTable, expcol, fkcol) != null) {
            throw Trace.error(Trace.CONSTRAINT_ALREADY_EXISTS);
        }

        if (expTable.isTemp() != table.isTemp()) {
            throw Trace.error(Trace.FOREIGN_KEY_NOT_ALLOWED);
        }

        boolean isforward = table.database.getTableIndex(table)
                            < table.database.getTableIndex(expTable);
        Index exportindex = expTable.getConstraintIndexForColumns(expcol);

        if (exportindex == null) {
            throw Trace.error(Trace.SQL_CONSTRAINT_REQUIRED,
                              expTable.getName().statementName);
        }

        // existing rows, value checks
        Constraint.checkReferencedRows(table, fkcol, exportindex);

        // create
        HsqlName iname   = table.database.nameManager.newAutoName("IDX");
        Index    fkindex = createIndex(fkcol, iname, false, true, isforward);
        HsqlName pkname = table.database.nameManager.newAutoName("REF",
            name.name);
        Constraint c = new Constraint(pkname, name, expTable, table, expcol,
                                      fkcol, exportindex, fkindex,
                                      deleteAction, updateAction);

        table.addConstraint(c);
        expTable.addConstraint(new Constraint(pkname, c));
        table.database.constraintNameList.addName(name.name, table.getName());
    }

// fredt@users 20020315 - patch 1.7.0 - create index bug
// this method would break existing foreign keys as the table order in the DB
// was changed. Instead, we now link in place of the old table

    /**
     *  Because of the way indexes and column data are held in memory and
     *  on disk, it is necessary to recreate the table when an index is added
     *  to a non-empty table cached table.<p>
     *
     *  With empty tables, Index objects are simply added<p>
     *
     *  With MEOMRY and TEXT tables, a new index is built up and nodes for
     *  earch row are interlinked (fredt@users)
     *
     * @param  col
     * @param  name
     * @param  unique
     * @param constraint
     * @param forward
     * @return  new index
     * @throws  HsqlException normally for lack of resources
     */
    Index createIndex(int col[], HsqlName name, boolean unique,
                      boolean constraint,
                      boolean forward) throws HsqlException {

        Index newindex;

        if (table.isEmpty() || table.isIndexingMutable()) {
            newindex = table.createIndex(col, name, unique, constraint,
                                         forward);
        } else {
            Table tn = table.moveDefinition(null, null,
                                            table.getColumnCount(), 0);

            newindex = tn.createIndexStructure(col, name, false, unique,
                                               constraint, forward);

            tn.moveData(table, table.getColumnCount(), 0);
            tn.updateConstraints(table, table.getColumnCount(), 0);

            int index = table.database.getTableIndex(table);

            table.database.getTables().set(index, tn);

            table = tn;
        }

        table.database.indexNameList.addName(newindex.getName().name,
                                             table.getName());

        return newindex;
    }

// fredt@users 20020225 - avoid duplicate constraints

    /**
     *  A unique constraint relies on a unique indexe on the table. It can
     *  cover a single column or multiple columns.
     *  <p>
     *  All unique constraint names are generated by Database.java as unique
     *  within the database. Duplicate constraints (more than one unique
     *  constriant on the same set of columns are still allowed but the
     *  names will be different. (fredt@users)
     *
     * @param  col
     * @param  name
     * @throws  HsqlException
     */
    void createUniqueConstraint(int[] col,
                                HsqlName name) throws HsqlException {

        if (table.database.constraintNameList.containsName(name.name)) {
            throw Trace.error(Trace.CONSTRAINT_ALREADY_EXISTS, name.name);
        }

        HsqlArrayList constraints = table.getConstraints();

        for (int i = 0, size = constraints.size(); i < size; i++) {
            Constraint c = (Constraint) constraints.get(i);

            if (c.isEquivalent(col, Constraint.UNIQUE)
                    || c.getName().name.equals(name.name)) {
                throw Trace.error(Trace.CONSTRAINT_ALREADY_EXISTS);
            }
        }

        // create an autonamed index
        HsqlName indexname = table.database.nameManager.newAutoName("IDX",
            name.name);
        Index      index = createIndex(col, indexname, true, true, false);
        Constraint newconstraint = new Constraint(name, table, index);

        table.addConstraint(newconstraint);
        table.database.constraintNameList.addName(name.name, table.getName());
    }

    void createCheckConstraint(Constraint c,
                               HsqlName name) throws HsqlException {

        if (table.database.constraintNameList.containsName(name.name)) {
            throw Trace.error(Trace.CONSTRAINT_ALREADY_EXISTS, name.name);
        }

        // check the existing rows
        Expression e = c.core.check;

        // this workaround is here to stop LIKE optimisation (for proper scripting)
        e.setLikeOptimised();

        Select s = Expression.getCheckSelect(table, e);
        Result r = s.getResult(1);

        c.core.checkFilter = s.tFilter[0];
        c.core.mainTable   = table;

        if (r.getSize() != 0) {
            throw Trace.error(Trace.CHECK_CONSTRAINT_VIOLATION);
        }

        // getDDL() is here to ensure no subselects etc. are in condition
        e.getDDL();
        table.addConstraint(c);
        table.database.constraintNameList.addName(name.name, table.getName());
    }

/** @todo
     * before a column is dropped, all CHECK constraints must be reset to
     * make sure they do not reference the column
     *
     * when a new Table object is created as a result of structural changes
     * the check constraint expression must be renewed to hold references
     * to the new table (otherwise there will still remain references to the
     * old Table object, resulting in memory leaks
     */
    void resetCheckConstraint(Constraint c) throws HsqlException {}

// fredt@users 20020315 - patch 1.7.0 - drop index bug

    /**
     *  Because of the way indexes and column data are held in memory and
     *  on disk, it is necessary to recreate the table when an index is added
     *  to a non-empty table.<p>
     *  Originally, this method would break existing foreign keys as the
     *  table order in the DB was changed. The new table is now linked
     *  in place of the old table (fredt@users)
     *
     * @param  indexname
     * @throws  HsqlException
     */
    void dropIndex(String indexname) throws HsqlException {

        if (table.isIndexingMutable()) {
            table.dropIndex(indexname);
        } else {
            Table tn = table.moveDefinition(indexname, null,
                                            table.getColumnCount(), 0);

            tn.moveData(table, table.getColumnCount(), 0);
            tn.updateConstraints(table, table.getColumnCount(), 0);

            int i = table.database.getTableIndex(table);

            table.database.getTables().set(i, tn);

            table = tn;
        }

        table.database.indexNameList.removeName(indexname);
    }

    /**
     *
     * @param  column
     * @param  colindex
     * @param  adjust +1 or -1
     * @throws  HsqlException
     */
    void addOrDropColumn(Column column, int colindex,
                         int adjust) throws HsqlException {

        if (table.isText()) {
            throw Trace.error(Trace.OPERATION_NOT_SUPPORTED);
        }

        // only allow add column at the end if referenced in a view
        if (colindex != table.getColumnCount()) {
            table.database.checkTableIsInView(table);
        }

        Table tn = table.moveDefinition(null, column, colindex, adjust);

        tn.moveData(table, colindex, adjust);
        tn.updateConstraints(table, colindex, adjust);

        int i = table.database.getTableIndex(table);

        table.database.getTables().set(i, tn);

        table = tn;
    }

    /**
     *  Method declaration
     *
     */
    void dropConstraint(String name) throws HsqlException {

        int        j = table.getConstraintIndex(name);
        Constraint c = table.getConstraint(name);

        if (c == null) {
            throw Trace.error(Trace.CONSTRAINT_NOT_FOUND,
                              Trace.TableWorks_dropConstraint, new Object[] {
                name, table.getName().name
            });
        }

        if (c.getType() == Constraint.MAIN) {
            throw Trace.error(Trace.DROP_SYSTEM_CONSTRAINT);
        }

        if (c.getType() == Constraint.FOREIGN_KEY) {
            Table mainTable = c.getMain();
            int   k         = mainTable.getConstraintIndex(c.getPkName());

            // drop the reference index, which is automatic and unused elsewhere
            Index refIndex = c.getRefIndex();

            // all is well if dropIndex throws for lack of resources
            dropIndex(refIndex.getName().name);
            mainTable.vConstraint.remove(k);
            table.vConstraint.remove(j);
        } else if (c.getType() == Constraint.UNIQUE) {
            HashSet cset = new HashSet();

            cset.add(c);

            // throw if the index for unique constraint is shared
            table.checkDropIndex(c.getMainIndex().getName().name, cset);

            // all is well if dropIndex throws for lack of resources
            dropIndex(c.getMainIndex().getName().name);
            table.vConstraint.remove(j);
        } else if (c.getType() == Constraint.CHECK) {
            table.vConstraint.remove(j);
        }

        table.database.constraintNameList.removeName(name);
    }
}
