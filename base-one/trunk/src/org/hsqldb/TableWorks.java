/* Copyright (c) 2001-2005, The HSQL Development Group
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

import org.hsqldb.HsqlNameManager.HsqlName;
import org.hsqldb.index.RowIterator;
import org.hsqldb.lib.ArrayUtil;
import org.hsqldb.lib.HashSet;

/**
 * The methods in this class perform alterations to the structure of an
 * existing table which may result in a new Table object
 *
 * @author fredt@users
 * @version 1.8.0
 * @since 1.7.0
 */
class TableWorks {

    private Table   table;
    private Session session;

    TableWorks(Session session, Table table) {
        this.table   = table;
        this.session = session;
    }

    Table getTable() {
        return table;
    }

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
     * @param  name         foreign key name
     * @param  expTable
     * @param  deleteAction
     * @param  updateAction
     * @throws HsqlException
     */
    void createForeignKey(int[] fkcol, int[] expcol, HsqlName name,
                          Table mainTable, int deleteAction,
                          int updateAction) throws HsqlException {

        table.database.schemaManager.checkConstraintExists(name.name,
                table.getSchemaName(), false);

        // name check
        if (table.getConstraint(name.name) != null) {
            throw Trace.error(Trace.CONSTRAINT_ALREADY_EXISTS);
        }

        // existing FK check
        if (table.getConstraintForColumns(mainTable, expcol, fkcol) != null) {
            throw Trace.error(Trace.CONSTRAINT_ALREADY_EXISTS);
        }

        if (mainTable.isTemp() != table.isTemp()) {
            throw Trace.error(Trace.FOREIGN_KEY_NOT_ALLOWED);
        }

        boolean isSelf = table == mainTable;
        int     offset = table.database.schemaManager.getTableIndex(table);
        boolean isforward =
            offset != -1
            && offset < table.database.schemaManager.getTableIndex(mainTable);
        Index exportindex =
            mainTable.getUniqueConstraintIndexForColumns(expcol);

        if (exportindex == null) {
            throw Trace.error(Trace.SQL_CONSTRAINT_REQUIRED,
                              mainTable.getName().statementName);
        }

        // existing rows, value checks
        Constraint.checkReferencedRows(session, table, fkcol, exportindex);

        // create
        HsqlName iname   = table.database.nameManager.newAutoName("IDX");
        Index    fkindex = createIndex(fkcol, iname, false, true, isforward);
        HsqlName pkname = table.database.nameManager.newAutoName("REF",
            name.name);

        if (isSelf) {

            // in case createIndex resulted in new Table object
            mainTable = table;
        }

        Constraint c = new Constraint(pkname, name, mainTable, table, expcol,
                                      fkcol, exportindex, fkindex,
                                      deleteAction, updateAction);

        table.addConstraint(c);
        mainTable.addConstraint(new Constraint(pkname, c));
        table.database.schemaManager.registerConstraintName(name.name,
                table.getName());
    }

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
    Index createIndex(int[] col, HsqlName name, boolean unique,
                      boolean constraint,
                      boolean forward) throws HsqlException {

        Index newindex;

        if (table.isEmpty(session) || table.isIndexingMutable()) {
            newindex = table.createIndex(session, col, name, unique,
                                         constraint, forward);

            table.database.schemaManager.clearTempTables(session, table);
        } else {
            Table tn = table.moveDefinition(null, null, -1, 0);

            newindex = tn.createIndexStructure(col, name, unique, constraint,
                                               forward);

            tn.moveData(session, table, -1, 0);
            tn.updateConstraintsTables(session, table, -1, 0);

            int index = table.database.schemaManager.getTableIndex(table);

            table.database.schemaManager.setTable(index, tn);

            table = tn;
        }

        table.database.schemaManager.registerIndexName(
            newindex.getName().name, table.getName());
        table.database.schemaManager.recompileViews(table);

        return newindex;
    }

    void addPrimaryKey(int[] cols, HsqlName name) throws HsqlException {

        boolean keepname = false;

        if (name == null) {
            name = table.makeSysPKName();
        }

        if (table.indexList[0].getName().name.equals(name.name)) {
            keepname = true;
        } else {
            table.database.schemaManager.checkConstraintExists(name.name,
                    table.getSchemaName(), false);
        }

        addOrDropPrimaryKey(cols, name, false);

        if (!keepname) {
            table.database.schemaManager.registerConstraintName(name.name,
                    table.getName());
        }
    }

    void addOrDropPrimaryKey(int[] cols, HsqlName name,
                             boolean identity) throws HsqlException {

        if (cols == null) {
            table.checkDropIndex(table.getIndexes()[0].getName().name, null,
                                 true);
        }

        Table tn = table.moveDefinitionPK(name, cols, identity);

        tn.moveData(session, table, -1, 0);
        tn.updateConstraintsTables(session, table, -1, 0);

        int index = table.database.schemaManager.getTableIndex(table);

        table.database.schemaManager.setTable(index, tn);

        table = tn;

        table.database.schemaManager.recompileViews(table);
    }

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

        table.database.schemaManager.checkConstraintExists(name.name,
                table.getSchemaName(), false);

        Constraint[] constraints = table.getConstraints();

        for (int i = 0, size = constraints.length; i < size; i++) {
            Constraint c = constraints[i];

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
        table.database.schemaManager.registerConstraintName(name.name,
                table.getName());
    }

    void createCheckConstraint(Constraint c,
                               HsqlName name) throws HsqlException {

        table.database.schemaManager.checkConstraintExists(name.name,
                table.getSchemaName(), false);

        // check the existing rows
        Expression e = c.core.check;

        // this workaround is here to stop LIKE optimisation (for proper scripting)
        e.setLikeOptimised();

        Select s = Expression.getCheckSelect(session, table, e);
        Result r = s.getResult(session, 1);

        c.core.checkFilter = s.tFilter[0];
        c.core.mainTable   = table;

        if (r.getSize() != 0) {
            throw Trace.error(Trace.CHECK_CONSTRAINT_VIOLATION);
        }

        // getDDL() is here to ensure no subselects etc. are in condition
        e.getDDL();

        // removes reference to the Index object in filter
        c.core.checkFilter.setAsCheckFilter();
        table.addConstraint(c);
        table.database.schemaManager.registerConstraintName(name.name,
                table.getName());
    }

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
            int[] removeIndex = new int[]{ table.getIndexIndex(indexname) };
            Table tn = table.moveDefinition(removeIndex, null, -1, 0);

            tn.moveData(session, table, -1, 0);
            tn.updateConstraintsTables(session, table, -1, 0);

            int i = table.database.schemaManager.getTableIndex(table);

            table.database.schemaManager.setTable(i, tn);

            table = tn;
        }

        table.database.schemaManager.removeIndexName(indexname,
                table.getName());
        table.database.schemaManager.recompileViews(table);
    }

    /**
     *
     * @param  column is null if adjust is -1
     * @param  colindex
     * @param  adjust +1, 0 -1
     * @throws  HsqlException
     */
    void addDropRetypeColumnOrig(Column column, int colindex,
                                 int adjust) throws HsqlException {

        if (table.isText()) {
            throw Trace.error(Trace.OPERATION_NOT_SUPPORTED);
        }

        if (adjust == -1 || adjust == 0) {
            table.database.schemaManager.checkColumnIsInView(table,
                    table.getColumn(colindex).columnName.name);
            table.checkColumnInCheckConstraint(
                table.getColumn(colindex).columnName.name);
        }

        Table tn = table.moveDefinition(null, column, colindex, adjust);

        tn.moveData(session, table, colindex, adjust);
        tn.updateConstraintsTables(session, table, colindex, adjust);

        int i = table.database.schemaManager.getTableIndex(table);

        table.database.schemaManager.setTable(i, tn);

        table = tn;

        table.database.schemaManager.recompileViews(table);
    }

    /**
     *
     * @param  column
     * @param  colindex
     * @throws  HsqlException
     */
    void retypeColumn(Column column, int colindex) throws HsqlException {

        if (table.isText()) {
            throw Trace.error(Trace.OPERATION_NOT_SUPPORTED);
        }

        table.database.schemaManager.checkColumnIsInView(table,
                table.getColumn(colindex).columnName.name);
        table.checkColumnInCheckConstraint(
            table.getColumn(colindex).columnName.name);

        int[] dropIndexes = null;
        Table tn = table.moveDefinition(dropIndexes, column, colindex, 0);

        tn.moveData(session, table, colindex, 0);
        tn.updateConstraintsTables(session, table, colindex, 0);

        int i = table.database.schemaManager.getTableIndex(table);

        table.database.schemaManager.setTable(i, tn);

        table = tn;

        table.database.schemaManager.recompileViews(table);
    }

    /**
     *
     * @param  colindex
     * @throws  HsqlException
     */
    void dropColumn(int colindex) throws HsqlException {

        HsqlName pkNameRemove    = null;
        HsqlName constNameRemove = null;

        if (table.isText()) {
            throw Trace.error(Trace.OPERATION_NOT_SUPPORTED);
        }

        table.database.schemaManager.checkColumnIsInView(table,
                table.getColumn(colindex).columnName.name);
        table.checkColumnInCheckConstraint(
            table.getColumn(colindex).columnName.name);

        Table  tn          = table;
        int[]  dropIndexes = null;
        String colName     = tn.getColumn(colindex).columnName.name;

        tn.checkColumnInFKConstraint(colName);

        if (table.getPrimaryKey().length == 1
                && table.getPrimaryKey()[0] == colindex) {
            table.checkDropIndex(table.getIndex(0).getName().name, null,
                                 true);

            pkNameRemove = table.getIndex(0).getName();
            tn           = table.moveDefinitionPK(null, null, false);
        }

        Constraint c = tn.getUniqueConstraintForColumns(new int[]{
            colindex });

        if (c != null) {
            Index idx = c.getMainIndex();

            dropIndexes = new int[]{ tn.getIndexIndex(idx.getName().name) };
            constNameRemove = c.getName();
        }

        tn = tn.moveDefinition(dropIndexes, null, colindex, -1);

        tn.moveData(session, table, colindex, -1);

        if (constNameRemove != null) {
            tn.removeConstraint(constNameRemove.name);
        }

        tn.updateConstraintsTables(session, table, colindex, -1);

        int i = table.database.schemaManager.getTableIndex(table);

        table.database.schemaManager.setTable(i, tn);

        table = tn;

        table.database.schemaManager.recompileViews(table);

        if (pkNameRemove != null) {
            table.database.schemaManager.removeConstraintName(
                pkNameRemove.name, table.getName());
        }

        if (constNameRemove != null) {
            table.database.schemaManager.removeConstraintName(
                constNameRemove.name, table.getName());
        }
    }

    /**
     *
     * @param  column
     * @param  colindex
     * @throws  HsqlException
     */
    void addColumn(Column column, int colindex) throws HsqlException {

        HsqlName pkNameAdd = null;

        if (table.isText()) {
            throw Trace.error(Trace.OPERATION_NOT_SUPPORTED);
        }

        Table tn = table;

        tn = tn.moveDefinition(null, column, colindex, 1);

        if (column.isPrimaryKey()) {
            pkNameAdd = tn.makeSysPKName();
            tn = tn.moveDefinitionPK(pkNameAdd, new int[]{ colindex }, true);
        }

        tn.moveData(session, table, colindex, 1);
        tn.updateConstraintsTables(session, table, colindex, 1);

        int i = table.database.schemaManager.getTableIndex(table);

        table.database.schemaManager.setTable(i, tn);

        table = tn;

        table.database.schemaManager.recompileViews(table);

        if (pkNameAdd != null) {
            table.database.schemaManager.registerConstraintName(
                pkNameAdd.name, table.getName());
        }
    }

    /**
     *  Drop a named constraint
     *
     */
    void dropConstraint(String name) throws HsqlException {

        Constraint c = table.getConstraint(name);
        int        ctype;

        if (name.equals(table.getIndexes()[0].getName().name)) {
            ctype = Constraint.PRIMARY_KEY;
        } else if (c == null) {
            throw Trace.error(Trace.CONSTRAINT_NOT_FOUND,
                              Trace.TableWorks_dropConstraint, new Object[] {
                name, table.getName().name
            });
        } else {
            ctype = c.getType();
        }

        if (ctype == Constraint.MAIN) {
            throw Trace.error(Trace.DROP_SYSTEM_CONSTRAINT);
        }

        if (ctype == Constraint.PRIMARY_KEY) {
            addOrDropPrimaryKey(null, null, false);
        } else if (ctype == Constraint.FOREIGN_KEY) {
            dropFKConstraint(c);
        } else if (ctype == Constraint.UNIQUE) {
            HashSet cset = new HashSet();

            cset.add(c);

            // throw if the index for unique constraint is shared
            table.checkDropIndex(c.getMainIndex().getName().name, cset,
                                 false);

            // all is well if dropIndex throws for lack of resources
            dropIndex(c.getMainIndex().getName().name);
            table.removeConstraint(name);
        } else if (ctype == Constraint.CHECK) {
            table.removeConstraint(name);
        }

        table.database.schemaManager.removeConstraintName(name,
                table.getName());
    }

    void dropFKConstraint(Constraint c) throws HsqlException {

        // drop the reference index, which is automatic and unused elsewhere
        Index constIndex = c.getRefIndex();

        // all is well if dropIndex throws for lack of resources
        dropIndex(constIndex.getName().name);

        int   refIndex  = table.getConstraintIndex(c.getFkName());
        Table mainTable = c.getMain();

        // MAIN constraint was created after REF, so delete first
        mainTable.removeConstraint(c.getPkName());
        table.removeConstraint(c.getFkName());
    }

    void reTypeColumn(Column oldCol, Column newCol) throws HsqlException {

        boolean notallowed = false;
        int     oldtype    = oldCol.getType();
        int     newtype    = newCol.getType();

        switch (newtype) {

            case Types.BINARY :
            case Types.VARBINARY :
            case Types.LONGVARBINARY :
            case Types.OTHER :
            case Types.JAVA_OBJECT :
                notallowed = !(newtype == oldtype || table.isEmpty(session));
        }

        switch (oldtype) {

            case Types.BINARY :
            case Types.VARBINARY :
            case Types.LONGVARBINARY :
            case Types.OTHER :
            case Types.JAVA_OBJECT :
                notallowed = !(newtype == oldtype || table.isEmpty(session));
                break;

            case Types.TINYINT :
            case Types.SMALLINT :
            case Types.INTEGER :
            case Types.BIGINT :
            case Types.REAL :
            case Types.FLOAT :
            case Types.DOUBLE :
            case Types.NUMERIC :
            case Types.DECIMAL :
                switch (newtype) {

                    case Types.DATE :
                    case Types.TIME :
                    case Types.TIMESTAMP :
                        notallowed = !table.isEmpty(session);
                    default :
                }
                break;

            case Types.DATE :
            case Types.TIME :
            case Types.TIMESTAMP :
                switch (newtype) {

                    case Types.TINYINT :
                    case Types.SMALLINT :
                    case Types.INTEGER :
                    case Types.BIGINT :
                    case Types.REAL :
                    case Types.FLOAT :
                    case Types.DOUBLE :
                    case Types.NUMERIC :
                    case Types.DECIMAL :
                        notallowed = !table.isEmpty(session);
                    default :
                }
                break;
        }

        if (notallowed) {
            throw Trace.error(Trace.INVALID_CONVERSION);
        }

        int colindex = table.getColumnNr(oldCol.columnName.name);

        if (table.getPrimaryKey().length > 1) {

            // if there is a multi-column PK, do not change the PK attributes
            if (newCol.isIdentity()) {
                throw Trace.error(Trace.SECOND_PRIMARY_KEY);
            }

            newCol.setPrimaryKey(oldCol.isPrimaryKey());

            if (ArrayUtil.find(table.getPrimaryKey(), colindex) != -1) {
                newCol.setNullable(false);
            }
        } else if (table.hasPrimaryKey()) {
            if (oldCol.isPrimaryKey()) {
                newCol.setPrimaryKey(true);
                newCol.setNullable(false);
            } else if (newCol.isPrimaryKey()) {
                throw Trace.error(Trace.SECOND_PRIMARY_KEY);
            }
        } else if (newCol.isPrimaryKey()) {
            throw Trace.error(Trace.PRIMARY_KEY_NOT_ALLOWED);
        }

        table.database.schemaManager.checkColumnIsInView(table,
                table.getColumn(colindex).columnName.name);
        table.checkColumnInCheckConstraint(
            table.getColumn(colindex).columnName.name);
        table.checkColumnInFKConstraint(oldCol.columnName.name);
        checkConvertColDataType(oldCol, newCol);
        retypeColumn(newCol, colindex);
    }

    void checkConvertColDataType(Column oldCol,
                                 Column newCol) throws HsqlException {

        int         colindex = table.getColumnNr(oldCol.columnName.name);
        RowIterator it       = table.rowIterator(null);

        while (it.hasNext()) {
            Row    row = it.next();
            Object o   = row.getData()[colindex];

            Column.convertObject(session, o, newCol.getType(),
                                 newCol.getSize(), newCol.getScale());
        }
    }
}
