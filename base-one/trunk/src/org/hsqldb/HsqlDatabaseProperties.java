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

import org.hsqldb.lib.HashSet;

/**
 * Manages a .properties file for a database.
 *
 * @version 1.7.0
 */
class HsqlDatabaseProperties extends org.hsqldb.HsqlProperties {

    private static HashSet nonSetProperties   = new HashSet();
    private static HashSet booleanProperties  = new HashSet();
    private static HashSet integralProperties = new HashSet();

    static {
        String[] nonSetPropertiesNames = {
            "version", "hsqldb.compatible_version", "hsqldb.cache_version",
            "hsqldb.log_size", "hsqldb.original_version",
            "hsqldb.script_format", "hsqldb.files_readonly", "readonly",
            "modified", "sql.compare_in_locale"
        };

        nonSetProperties.addAll(nonSetPropertiesNames);

        String[] booleanPropertiesNames = {
            "hsqldb.schemas", "hsqldb.catalogs", "sql.enforce_size",
            "sql.enforce_strict_size"
        };

        booleanProperties.addAll(booleanPropertiesNames);

        String[] integralPropertiesNames = {
            "runtime.gc_interval", "hsqldb.cache_file_scale",
            "hsqldb.cache_scale", "hsqldb.first_identity"
        };

        integralProperties.addAll(integralPropertiesNames);
    }

    private Database database;

    HsqlDatabaseProperties(Database db) {

        super(db.getPath(), db.isFilesInJar());

        database = db;

        // char trimming and padding to size and varchar trimming to size
        setProperty("sql.enforce_size", false);

        // char and padding to size and exception if data is too long
        setProperty("sql.enforce_strict_size", false);

        // char and varchar sorting in charset of the current jvm Locale
        setProperty("sql.compare_in_locale", false);

        // removed from 1.7.2 - sql.month is always true (1-12)
        // removed from 1.7.2 - sql.strict_fk is always enforced
        // if true, requires a pre-existing unique index for foreign key
        // referenced column and returns an error if index does not exist
        // 1.61 creates a non-unique index if no index exists
        // setProperty("sql.strict_fk", false);
        // removed from 1.7.2
        // has no effect if sql_strict_fk is true, otherwise if true,
        // creates a unique index for foreign keys instead of non-unique
        // setProperty("sql.strong_fk", true);
        // the two properties below are meant for attempting to open an
        // existing database with all its files *.properties, *script and
        // *.data.
        // the earliest version that can open this database
        // this is set to 1.7.2 when the db is written to
        setProperty("hsqldb.compatible_version", "1.6.0");

        // data format of the cache file
        // this is set to 1.7.0 when a new *.data file is created
        setProperty("hsqldb.cache_version", "1.7.0");

        // the version that created this database
        // once created, this won't change if db is used with a future version
        setProperty("hsqldb.original_version", "1.7.2");
        /*
                garbage collection with gc_interval
                Setting this value can be useful when HSQLDB is used as an
                in-process part of an application. The minimum practical
                amount is probably "10000" and the maximum "1000000"

                In some versions of Java, such as 1.3.1_02 on windows,
                when the application runs out of memory it runs the gc AND
                requests more memory from the OS. Setting this property
                forces the DB to live inside its memory budget but the
                maximum amount of memory can still be set with the
                java -Xmx argument to provide the memory needed by other
                parts of the app to do graphics and networking.

                Of course there is a speed penalty for setting the value
                too low and doing garbage collection too often.

                This was introduced as a result of tests by Karl Meissner
                (meissnersd@users)
        */

        // garbage collect per Record or Cache Row objects created
        // the default, "0" means no garbage collection is forced by
        // hsqldb (the Java Runtime will do it's own garbage collection
        // in any case).
        setProperty("runtime.gc_interval", "0");

        // this property is either 1 or 8
        setProperty("hsqldb.cache_file_scale", "1");

        // number of rows from CACHED tables kept constantly in memory
        // the number of rows in up to 3 * (2 to the power of
        // cache_scale value).
        // reduce the default 14 (3*16K rows) if memory is limited and rows
        // are large.
        // values between 8-16 are allowed
        setProperty("hsqldb.cache_scale", "14");

        // maximum size of .log file in megabytes
        setProperty("hsqldb.log_size", "200");

        // type of logging (0 : text , 1 : binary)
        setProperty("hsqldb.script_format", "0");

        // initial value of idendity columns
        setProperty("hsqldb.first_identity", "0");
        setProperty("readonly", false);
        setProperty("modified", "no");

        // the property "version" is also set to the current version
        //
        // the following properties can be set by the user as defaults for
        // text tables. the default values are shown.
        // "textdb.fs", ","
        // "textdb.vs", ",";
        // "textdb.lvs", ","
        // "textdb.ignore_first", false
        // "textdb.quoted", true
        // "textdb.all_quoted", false
        // "textdb.encoding", "ASCII"
        // "textdb.cache_scale", 10  -- allowed range 8-16
        // "textdb.cache_size_scale", 12  -- allowed range 8-20
        setSystemVariables();
        setDatabaseVariables();
    }

    private void setSystemVariables() {

        Column.setCompareInLocal(isPropertyTrue("sql.compare_in_locale"));

        Record.gcFrequency = getIntegerProperty("runtime.gc_interval", 0);
    }

    /**
     * Creates file with defaults if it didn't exist.
     * Returns false if file already existed.
     */
    public boolean load() throws HsqlException {

        boolean exists;

        if (database.getType() == DatabaseManager.S_MEM) {
            return true;
        }

        try {
            exists = super.load();
        } catch (Exception e) {
            throw Trace.error(Trace.FILE_IO_ERROR,
                              Trace.HsqlDatabaseProperties_load,
                              new Object[] {
                fileName, e
            });
        }

        if (!exists) {
            setProperty("version", org.hsqldb.jdbc.jdbcUtil.VERSION);
            setProperty("hsqldb.cache_version", "1.7.0");
            setProperty("hsqldb.compatible_version", "1.7.2");
            save();

            return false;
        }

        String version = getProperty("hsqldb.compatible_version");

        // do not open if the database belongs to a later (future) version
        int check = version.substring(0, 5).compareTo(
            org.hsqldb.jdbc.jdbcUtil.VERSION);

        Trace.check(check <= 0, Trace.WRONG_DATABASE_FILE_VERSION);

        version = getProperty("version");

        if (version.charAt(2) == '6') {
            setProperty("hsqldb.cache_version", "1.6.0");
        }

        // change to the current version
        setProperty("hsqldb.version", org.hsqldb.jdbc.jdbcUtil.VERSION);
        setSystemVariables();
        setDatabaseVariables();

        return true;
    }

    private void setDatabaseVariables() {

        if (isPropertyTrue("readonly")) {
            database.setReadOnly();
        }

        if (isPropertyTrue("hsqldb.files_readonly")) {
            database.setFilesReadOnly();
        }

        database.sqlEnforceSize = isPropertyTrue("sql.enforce_size");
        database.sqlEnforceStrictSize =
            isPropertyTrue("sql.enforce_strict_size");
        database.firstIdentity = getIntegerProperty("hsqldb.first_identity",
                0);
    }

    public void save() throws HsqlException {

        if (database.getType() == DatabaseManager.S_MEM
                || database.isFilesReadOnly() || database.isFilesInJar()) {
            return;
        }

        try {
            super.save();
        } catch (Exception e) {
            throw Trace.error(Trace.FILE_IO_ERROR,
                              Trace.HsqlDatabaseProperties_save,
                              new Object[] {
                fileName, e
            });
        }
    }

    boolean isSetPropertyAllowed(String property) {
        return !nonSetProperties.contains(property);
    }

    boolean isBoolean(String property) {
        return booleanProperties.contains(property);
    }

    boolean isIntegral(String property) {
        return integralProperties.contains(property);
    }

    public String setProperty(String key, String value) {

        value = super.setProperty(key, value);

        setDatabaseVariables();

        return value;
    }
}
