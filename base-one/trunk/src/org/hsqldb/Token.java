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

import org.hsqldb.lib.IntValueHashMap;

/**
 * Provides an enumeration of the token types commonly encountered
 * while processing database commands. <p>
 *
 * @author  Nitin Chauhan
 * @since HSQLDB 1.7.2
 * @version 1.7.2
 */
class Token {

    private static IntValueHashMap commandSet;

    //
    static final String T_ASTERISK     = "*";
    static final String T_COMMA        = ",";
    static final String T_CLOSEBRACKET = ")";
    static final String T_EQUALS       = "=";
    static final String T_OPENBRACKET  = "(";
    static final String T_SEMICOLON    = ";";

    //
    static final String T_ADD                   = "ADD";
    static final String T_ADMIN                 = "ADMIN";
    static final String T_ALIAS                 = "ALIAS";
    static final String T_ALL                   = "ALL";
    static final String T_ALTER                 = "ALTER";
    static final String T_AS                    = "AS";
    static final String T_ASC                   = "ASC";
    static final String T_AUTOCOMMIT            = "AUTOCOMMIT";
    static final String T_BEFORE                = "BEFORE";
    static final String T_BY                    = "BY";
    static final String T_CACHED                = "CACHED";
    static final String T_CALL                  = "CALL";
    static final String T_CASCADE               = "CASCADE";
    static final String T_CHECKPOINT            = "CHECKPOINT";
    static final String T_CLASS                 = "CLASS";
    static final String T_COLUMN                = "COLUMN";
    static final String T_COMMIT                = "COMMIT";
    static final String T_COMPACT               = "COMPACT";
    static final String T_CONNECT               = "CONNECT";
    static final String T_CONSTRAINT            = "CONSTRAINT";
    static final String T_CREATE                = "CREATE";
    static final String T_DEFAULT               = "DEFAULT";
    static final String T_DEFRAG                = "DEFRAG";
    static final String T_DELETE                = "DELETE";
    static final String T_DESC                  = "DESC";
    static final String T_DISCONNECT            = "DISCONNECT";
    static final String T_DISTINCT              = "DISTINCT";
    static final String T_DROP                  = "DROP";
    static final String T_EACH                  = "EACH";
    static final String T_ESCAPE                = "ESCAPE";
    static final String T_EXCEPT                = "EXCEPT";
    static final String T_EXISTS                = "EXISTS";
    static final String T_EXPLAIN               = "EXPLAIN";
    static final String T_FALSE                 = "FALSE";
    static final String T_FOR                   = "FOR";
    static final String T_FOREIGN               = "FOREIGN";
    static final String T_FROM                  = "FROM";
    static final String T_GRANT                 = "GRANT";
    static final String T_GROUP                 = "GROUP";
    static final String T_HAVING                = "HAVING";
    static final String T_IDENTITY              = "IDENTITY";
    static final String T_IF                    = "IF";
    static final String T_IGNORECASE            = "IGNORECASE";
    static final String T_IMMEDIATELY           = "IMMEDIATELY";
    static final String T_INDEX                 = "INDEX";
    static final String T_INNER                 = "INNER";
    static final String T_INSERT                = "INSERT";
    static final String T_INTERSECT             = "INTERSECT";
    static final String T_INTO                  = "INTO";
    static final String T_JOIN                  = "JOIN";
    static final String T_KEY                   = "KEY";
    static final String T_LEFT                  = "LEFT";
    static final String T_LIMIT                 = "LIMIT";
    static final String T_LOGSIZE               = "LOGSIZE";
    static final String T_LOGTYPE               = "LOGTYPE";
    static final String T_MAXROWS               = "MAXROWS";
    static final String T_MEMORY                = "MEMORY";
    static final String T_MINUS                 = "MINUS";
    static final String T_NOT                   = "NOT";
    static final String T_NOWAIT                = "NOWAIT";
    static final String T_NULL                  = "NULL";
    static final String T_ON                    = "ON";
    static final String T_ORDER                 = "ORDER";
    static final String T_OUTER                 = "OUTER";
    static final String T_PASSWORD              = "PASSWORD";
    static final String T_PLAN                  = "PLAN";
    static final String T_PRECISION             = "PRECISION";
    static final String T_PRIMARY               = "PRIMARY";
    static final String T_PROPERTY              = "PROPERTY";
    static final String T_QUEUE                 = "QUEUE";
    static final String T_READONLY              = "READONLY";
    static final String T_REFERENCES            = "REFERENCES";
    static final String T_REFERENTIAL_INTEGRITY = "REFERENTIAL_INTEGRITY";
    static final String T_RENAME                = "RENAME";
    static final String T_REVOKE                = "REVOKE";
    static final String T_ROLLBACK              = "ROLLBACK";
    static final String T_ROW                   = "ROW";
    static final String T_SAVEPOINT             = "SAVEPOINT";
    static final String T_SCRIPT                = "SCRIPT";
    static final String T_SELECT                = "SELECT";
    static final String T_SET                   = "SET";
    static final String T_SHUTDOWN              = "SHUTDOWN";
    static final String T_SOURCE                = "SOURCE";
    static final String T_TABLE                 = "TABLE";
    static final String T_TEMP                  = "TEMP";
    static final String T_TEXT                  = "TEXT";
    static final String T_TO                    = "TO";
    static final String T_TOP                   = "TOP";
    static final String T_TRIGGER               = "TRIGGER";
    static final String T_TRUE                  = "TRUE";
    static final String T_UNION                 = "UNION";
    static final String T_UNIQUE                = "UNIQUE";
    static final String T_UPDATE                = "UPDATE";
    static final String T_USER                  = "USER";
    static final String T_VALUES                = "VALUES";
    static final String T_VIEW                  = "VIEW";
    static final String T_WHERE                 = "WHERE";
    static final String T_WORK                  = "WORK";
    static final String T_WRITE_DELAY           = "WRITE_DELAY";

//
    static final int UNKNOWN               = -1;
    static final int ADD                   = 1;
    static final int ALIAS                 = 2;
    static final int ALTER                 = 3;
    static final int AUTOCOMMIT            = 4;
    static final int CACHED                = 5;
    static final int CALL                  = 6;
    static final int CHECKPOINT            = 7;
    static final int COLUMN                = 8;
    static final int COMMIT                = 9;
    static final int CONNECT               = 10;
    static final int CONSTRAINT            = 11;
    static final int CREATE                = 12;
    static final int DELETE                = 13;
    static final int DISCONNECT            = 14;
    static final int DROP                  = 15;
    static final int EXPLAIN               = 16;
    static final int FOREIGN               = 17;
    static final int GRANT                 = 18;
    static final int IGNORECASE            = 19;
    static final int INDEX                 = 20;
    static final int INSERT                = 21;
    static final int LOGSIZE               = 22;
    static final int LOGTYPE               = 23;
    static final int MAXROWS               = 24;
    static final int MEMORY                = 25;
    static final int NOT                   = 26;
    static final int PASSWORD              = 27;
    static final int PLAN                  = 28;
    static final int PRIMARY               = 29;
    static final int PROPERTY              = 30;
    static final int READONLY              = 31;
    static final int REFERENTIAL_INTEGRITY = 32;
    static final int RENAME                = 33;
    static final int REVOKE                = 34;
    static final int ROLLBACK              = 35;
    static final int SAVEPOINT             = 36;
    static final int SCRIPT                = 37;
    static final int SELECT                = 38;
    static final int SEMICOLON             = 39;
    static final int SET                   = 40;
    static final int SHUTDOWN              = 41;
    static final int SOURCE                = 42;
    static final int TABLE                 = 43;
    static final int TEXT                  = 44;
    static final int TRIGGER               = 45;
    static final int UNIQUE                = 46;
    static final int UPDATE                = 47;
    static final int USER                  = 48;
    static final int VIEW                  = 49;
    static final int WRITE_DELAY           = 50;

    //
    static {
        commandSet = newCommandSet();
    }

    /**
     * Retrieves a new map from set of string tokens to numeric tokens for
     * commonly encountered database command token occurences.
     *
     * @return a new map for the database command token set
     */
    private static IntValueHashMap newCommandSet() {

        IntValueHashMap commandSet;

        commandSet = new IntValueHashMap(67);

        commandSet.put(T_SEMICOLON, SEMICOLON);
        commandSet.put(T_ADD, ADD);
        commandSet.put(T_ALIAS, ALIAS);
        commandSet.put(T_ALTER, ALTER);
        commandSet.put(T_AUTOCOMMIT, AUTOCOMMIT);
        commandSet.put(T_CACHED, CACHED);
        commandSet.put(T_CALL, CALL);
        commandSet.put(T_CHECKPOINT, CHECKPOINT);
        commandSet.put(T_COLUMN, COLUMN);
        commandSet.put(T_COMMIT, COMMIT);
        commandSet.put(T_CONNECT, CONNECT);
        commandSet.put(T_CONSTRAINT, CONSTRAINT);
        commandSet.put(T_CREATE, CREATE);
        commandSet.put(T_DELETE, DELETE);
        commandSet.put(T_DISCONNECT, DISCONNECT);
        commandSet.put(T_DROP, DROP);
        commandSet.put(T_EXPLAIN, EXPLAIN);
        commandSet.put(T_FOREIGN, FOREIGN);
        commandSet.put(T_GRANT, GRANT);
        commandSet.put(T_IGNORECASE, IGNORECASE);
        commandSet.put(T_INDEX, INDEX);
        commandSet.put(T_INSERT, INSERT);
        commandSet.put(T_LOGSIZE, LOGSIZE);
        commandSet.put(T_LOGTYPE, LOGTYPE);
        commandSet.put(T_MAXROWS, MAXROWS);
        commandSet.put(T_MEMORY, MEMORY);
        commandSet.put(T_PASSWORD, PASSWORD);
        commandSet.put(T_PLAN, PLAN);
        commandSet.put(T_PRIMARY, PRIMARY);
        commandSet.put(T_PROPERTY, PROPERTY);
        commandSet.put(T_READONLY, READONLY);
        commandSet.put(T_REFERENTIAL_INTEGRITY, REFERENTIAL_INTEGRITY);
        commandSet.put(T_RENAME, RENAME);
        commandSet.put(T_REVOKE, REVOKE);
        commandSet.put(T_ROLLBACK, ROLLBACK);
        commandSet.put(T_SAVEPOINT, SAVEPOINT);
        commandSet.put(T_SCRIPT, SCRIPT);
        commandSet.put(T_SELECT, SELECT);
        commandSet.put(T_SET, SET);
        commandSet.put(T_SHUTDOWN, SHUTDOWN);
        commandSet.put(T_SOURCE, SOURCE);
        commandSet.put(T_TABLE, TABLE);
        commandSet.put(T_TEXT, TEXT);
        commandSet.put(T_TRIGGER, TRIGGER);
        commandSet.put(T_UNIQUE, UNIQUE);
        commandSet.put(T_UPDATE, UPDATE);
        commandSet.put(T_USER, USER);
        commandSet.put(T_VIEW, VIEW);
        commandSet.put(T_WRITE_DELAY, WRITE_DELAY);

        return commandSet;
    }

    static int get(String token) {
        return commandSet.get(token, -1);
    }
}
